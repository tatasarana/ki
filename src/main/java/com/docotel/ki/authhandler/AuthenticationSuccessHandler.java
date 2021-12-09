package com.docotel.ki.authhandler;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.controller.DashboardController;
import com.docotel.ki.controller.HomeController;
import com.docotel.ki.controller.LoginController;
import com.docotel.ki.model.master.MRoleDetail;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.transaction.TxOnlineReg;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.pojo.KeyValueSelect;
import com.docotel.ki.pojo.NativeQueryModel;
import com.docotel.ki.repository.NativeQueryRepository;
import com.docotel.ki.repository.master.MUserRepository;
import com.docotel.ki.service.master.EmployeeService;
import com.docotel.ki.service.master.RepresentativeService;
import com.docotel.ki.service.master.RoleService;
import com.docotel.ki.service.transaction.RegistrasiOnlineService;
import com.docotel.ki.enumeration.OperationEnum;
import com.docotel.ki.enumeration.RegistrasiStatusEnum;
import com.docotel.ki.model.master.*;
import com.docotel.ki.service.master.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component("authenticationSuccessHandler")
public class AuthenticationSuccessHandler implements org.springframework.security.web.authentication.AuthenticationSuccessHandler {
	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	@Value("${app.mode}")
	private String appMode;

	@Value("${user.session.timeout:-1}")
	private String userSessionTimeout;

	@Autowired
	private RoleService roleService;

	@Autowired
	private EmployeeService employeeService;

	@Autowired
	private RepresentativeService representativeService;

	@Autowired
	RegistrasiOnlineService registrasiOnlineService;

	@Autowired
	RESTService restService ;

	@Autowired
	private NativeQueryRepository nativeQueryRepository ;

	@Autowired
	private MUserRepository mUserRepository ;



	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication auth) throws IOException, ServletException {
		if (response.isCommitted()) {
			return;
		}

		// Login Data
		MUser mUser = (MUser) auth.getPrincipal();

		if (mUser.getOperation().getAuthority().equalsIgnoreCase(OperationEnum.USER.name())) {
			if (!appMode.equalsIgnoreCase("all")) {
				boolean isUserAdminApp = mUser.getUserType() == null || mUser.getUserType().equalsIgnoreCase("Karyawan");

				if (appMode.equalsIgnoreCase("admin")) {
					if (!isUserAdminApp) {
						redirectStrategy.sendRedirect(request, response, LoginController.PATH_LOGIN_ERROR_NO_AUTHORITY);
						return;
					}
				} else {
					if (isUserAdminApp) {
						redirectStrategy.sendRedirect(request, response, LoginController.PATH_LOGIN_ERROR_NO_AUTHORITY);
						return;
					}
				}
			}

			/*step 1 adalah ambil data muser role dan user accessnya
					step 2 simpan list access dari step 1 ke session
					request.getSession().setAttribute();*/

			Boolean statusString = false;
			List<MRoleDetail> mRoleDetailList = roleService.selectAllRoleDetailByUser(mUser);
			if (mRoleDetailList != null && mRoleDetailList.size() > 0) {
				List<MRoleDetail> mRoleDetails = new ArrayList<>();
				for (MRoleDetail mRoleDetail : mRoleDetailList) {
					if(mRoleDetail.getmRole().isStatusFlag())
						mRoleDetails.add(new MRoleDetail(mRoleDetail));
				}
				//mUser.putMRoleDetailList(mRoleDetailList);
				mUser.putMRoleDetailList(mRoleDetails);
			}
			if (mUser.isEmployee()) {
				mUser.setmEmployee(employeeService.selectOneByUserId(mUser.getId()));
				if (mUser.getmEmployee() == null && !"super".equalsIgnoreCase(mUser.getUsername())) {
					redirectStrategy.sendRedirect(request, response, LoginController.PATH_LOGIN_ERROR_NO_AUTHORITY);
					return;
				}
			} else if (!mUser.isEmployee() && !mUser.isReprs()){
				NativeQueryModel querymodel = new NativeQueryModel() ;
				querymodel.setTable_name("M_USER");
				ArrayList<KeyValue> updateq = new ArrayList <>();
				KeyValue updateq1 = new KeyValue();
				updateq1.setKey("enabled");
				updateq1.setValue(2);

				updateq.add(updateq1);
				querymodel.setUpdateQ(updateq);
				ArrayList<KeyValueSelect> searchBy = new ArrayList <>() ;
				searchBy.add(new KeyValueSelect("USER_ID",mUser.getId(),"=", true,null));
				querymodel.setSearchBy(searchBy);

				int exe = nativeQueryRepository.updateNavite(querymodel);

			} else if (mUser.isReprs()) {
				mUser.setmRepresentative(representativeService.selectOneByUserId(mUser.getId()));
				if (mUser.getmRepresentative() == null) {
					redirectStrategy.sendRedirect(request, response, LoginController.PATH_LOGIN_ERROR_NO_AUTHORITY);
					return;
				}
				// Syncronizing with PDKKI
				String userId =  mUser.getId();
				if (userId != null) {
					MRepresentative mreps =  representativeService.selectOneByUserId(userId);
					if (mreps.getNo() != null) {
						try{
							mreps = restService.syncPDKKI(mreps);
							if (!mreps.isStatusFlag()) {
								redirectStrategy.sendRedirect(request, response, LoginController.PATH_LOGIN_ERROR_REPS_DEAD);
								return;
							}
						}
						catch (Exception e){
							// Tidak bisa mensinkronisasi
						}

					} else {
						redirectStrategy.sendRedirect(request, response, LoginController.PATH_LOGIN_ERROR_REPS_DEAD);
						return ;
					}

				} else {
					redirectStrategy.sendRedirect(request, response, LoginController.PATH_LOGIN_ERROR_REPS_DEAD);
					return ;
				}

			} else {
				//dari menu registrasi akun
				TxOnlineReg txOnlineReg = registrasiOnlineService.selectByValue("email", mUser.getUsername());

				if (txOnlineReg != null) {
					if (txOnlineReg.getmReprs() != null) {
						statusString = txOnlineReg.getmReprs().isStatusFlag();
					} else {
						statusString = RegistrasiStatusEnum.APPROVE.name().equalsIgnoreCase(txOnlineReg.getApprovalStatus());
					}
				}
				if (statusString == false) {
					redirectStrategy.sendRedirect(request, response, LoginController.PATH_LOGIN_ERROR_NO_AUTHORITY);
					return;
				}
			}

			try {
				request.getSession().setMaxInactiveInterval(Integer.parseInt(userSessionTimeout));
			} catch (NumberFormatException e) {
				request.getSession().setMaxInactiveInterval(-1);
			}
			String redirect = BaseController.PATH_AFTER_LOGIN + DashboardController.PATH_DASHBOARD;
			if (mUser.hasAccessUrl(redirect)) {
				redirectStrategy.sendRedirect(request, response, redirect);
			} else  {
				redirectStrategy.sendRedirect(request, response, BaseController.PATH_AFTER_LOGIN + HomeController.PATH_HOME);
			}
		} else {
			redirectStrategy.sendRedirect(request, response, LoginController.PATH_LOGIN_ERROR_NO_AUTHORITY);
		}
	}
}
