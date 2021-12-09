package com.docotel.ki.controller.master;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.transaction.TxOnlineReg;
import com.docotel.ki.pojo.UserRole;
import com.docotel.ki.service.transaction.RegistrasiOnlineService;
import com.docotel.ki.model.master.MEmployee;
import com.docotel.ki.model.master.MRepresentative;
import com.docotel.ki.model.master.MRole;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.master.MUserRole;
import com.docotel.ki.service.master.EmployeeService;
import com.docotel.ki.service.master.RepresentativeService;
import com.docotel.ki.service.master.RoleService;
import com.docotel.ki.service.master.UserRoleService;
import com.docotel.ki.service.master.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class MUserRoleController extends BaseController {

    @Autowired
    RoleService roleService;
    @Autowired
    EmployeeService employeeService;
    @Autowired
    UserRoleService userRoleService;
    @Autowired
    RepresentativeService representativeService;
    @Autowired
    UserService userService;
    @Autowired
    RegistrasiOnlineService registrasiOnlineService;
    
    
    private static final String DIRECTORY_PAGE = "user-management/user-role/";
    private static final String PAGE_TAMBAH = DIRECTORY_PAGE + "tambah-user-role";
    private static final String PATH_TAMBAH = "/tambah-user-role";
    private static final String REQUEST_MAPPING_LIST_TAMBAH = PATH_TAMBAH + "*";
    private static final String REDIRECT_LIST_USER = "redirect:" + PATH_AFTER_LOGIN + "/list-user";

    @RequestMapping(path = REQUEST_MAPPING_LIST_TAMBAH)
    public String showPageList(Model model, @RequestParam(value = "no", required = true) String no) {
        try {        	
        	List<MRole> mRoleList = roleService.findAll();
    		List<Map<String, Object>> mRoleListMap = new ArrayList<>(mRoleList.size());
    		for(MRole mRole : mRoleList) {
    			Map<String, Object> mRoleMap = new HashMap();
    			mRoleMap.put("id", mRole.getId());
    			mRoleMap.put("name", mRole.getName());
    			mRoleListMap.add(mRoleMap);
        	}
        	model.addAttribute("mRoleList", mRoleListMap);
        	
        	List<MUserRole> mUserRoleList = userRoleService.selectAllByUser(no);
			List<String> existingRoleList = new ArrayList<>();
			for (MUserRole userRoleExisting : mUserRoleList) {
                existingRoleList.add(userRoleExisting.getmRole().getCurrentId());
            }
			model.addAttribute("existingRoleList", existingRoleList);
			
			MUser mUser = userService.getRoleByUserId(no);
        	if(mUser.isEmployee()) {        		
                UserRole userRole = new UserRole();
                userRole.setId(mUser.getId());
                userRole.setUserName(mUser.getUsername());
        		
                MEmployee mEmployee = employeeService.selectOneByUserId(no);
        		if(mEmployee!=null) {
        			userRole.setNik(mEmployee.getNik());
                    userRole.setEmployeeName(mEmployee.getEmployeeName());
        		}
                
                model.addAttribute("mEmployee", userRole);
                
        	} else if(mUser.isReprs()) {
        		MRepresentative mReprs = representativeService.selectOneByUserId(no);
                
        		UserRole userRole = new UserRole();
                userRole.setId(mReprs.getUserId().getId());
                userRole.setUserName(mReprs.getUserId().getUsername());
                userRole.setNik("-");
                userRole.setEmployeeName(mReprs.getName());                
                model.addAttribute("mEmployee", userRole);
        	} else {
        		//registrasi akun
        		UserRole userRole = new UserRole();
        		userRole.setId(mUser.getId());
        		userRole.setUserName(mUser.getUsername());
        		userRole.setNik("-");
        		TxOnlineReg txOnlineReg = registrasiOnlineService.selectByValue("email", mUser.getUsername());
        		userRole.setEmployeeName(txOnlineReg.getName());
                 
        		model.addAttribute("mEmployee", userRole);    
        	}

            return PAGE_TAMBAH;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return REDIRECT_LIST_USER;
        }
    }

    @PostMapping(path = REQUEST_MAPPING_LIST_TAMBAH)
    public String doSave(@RequestParam(value = "ckList", required = false) String[] role, @RequestParam("id") String id ) {
        try {
        	 MUser mUser = userService.getUserById(id);   
        	 mUser.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        	 
        	 if(mUser!=null) {
        		 userRoleService.save(mUser, role);
        	 }            
            return REDIRECT_LIST_USER;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return REDIRECT_LIST_USER;
        }
    }

}
