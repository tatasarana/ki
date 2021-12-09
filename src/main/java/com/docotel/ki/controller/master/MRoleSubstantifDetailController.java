package com.docotel.ki.controller.master;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.pojo.DataDetailSubstantif;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.service.master.EmployeeService;
import com.docotel.ki.service.master.RoleSubstantifService;
import com.docotel.ki.service.master.SectionService;
import com.docotel.ki.service.master.UserService;
import com.docotel.ki.util.FieldValidationUtil;
import com.docotel.ki.model.master.MEmployee;
import com.docotel.ki.model.master.MRoleSubstantif;
import com.docotel.ki.model.master.MRoleSubstantifDetail;
import com.docotel.ki.model.master.MSection;
import com.docotel.ki.model.master.MUser;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class MRoleSubstantifDetailController extends BaseController {
    @Autowired
    private RoleSubstantifService roleSubstantifService;
    @Autowired
    private SectionService sectionService;
    @Autowired
    private UserService userService;
    @Autowired
    private EmployeeService employeeService;

    private static final String DIRECTORY_PAGE = "master/role-substantif/";

    private static final String PAGE_LIST = DIRECTORY_PAGE + "list-role-subs-detail";
    private static final String PAGE_TAMBAH = DIRECTORY_PAGE + "tambah-role-subs-detail";

    private static final String PATH_TAMBAH = "/tambah-detail";
    private static final String PATH_TAMBAH_DETAIL = "/tambah-detail-baru";
    private static final String PATH_HAPUS_DETAIL = "/hapus-detail-baru";

    private static final String PATH_AJAX_LIST = "/cari-detail";
    private static final String PATH_AJAX_PILIH_USER = "/pilih-user";
    private static final String PATH_TAMBAH_AJAX = "/req-tambah-role-subs-detail";

    private static final String REQUEST_MAPPING_AJAX_LIST = PATH_AJAX_LIST + "*";
    private static final String REQUEST_MAPPING_TAMBAH = PATH_TAMBAH + "*";
    private static final String REQUEST_MAPPING_TAMBAH_DETAIL = PATH_TAMBAH_DETAIL + "*";
    private static final String REQUEST_MAPPING_HAPUS_DETAIL = PATH_HAPUS_DETAIL + "*";
    private static final String REQUEST_MAPPING_TAMBAH_AJAX = PATH_TAMBAH_AJAX + "*";
    private static final String REQUEST_MAPPING_AJAX_PILIH_USER = PATH_AJAX_PILIH_USER + "*";

    public static final String REDIRECT_LIST_TAMBAH_DETAIL = "redirect:" + PATH_AFTER_LOGIN + PATH_TAMBAH;
    public static final String REDIRECT_DASHBOARD = "redirect:" + PATH_AFTER_LOGIN + "/dashboard";

    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "maintenance");
        model.addAttribute("subMenu", "listRoleSubstantifDetail");

        if (request.getRequestURI().contains(PATH_TAMBAH_DETAIL)) {
            if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
                model.addAttribute("form", new MRoleSubstantifDetail());
            }
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_AJAX_PILIH_USER, method = {RequestMethod.POST})
    public void pilihUser(final HttpServletRequest request, final HttpServletResponse response) throws IOException{
        //request data ke database section
        String target = request.getParameter("target");
        List<MEmployee> mEmployeeList = new ArrayList<>();

        if(target != null) {
        	target = target.trim();
        	if(!target.equalsIgnoreCase("")) {
        		mEmployeeList = roleSubstantifService.selectAllEmployeeNotSubstantif(target);
        	}
        }

        List<MEmployee> data = new ArrayList<>();
        MEmployee mEmployee = null;
        for (MEmployee result : mEmployeeList) {
        	mEmployee = new MEmployee();
        	mEmployee.setId(result.getId());
        	mEmployee.setEmployeeName(result.getEmployeeName());
        	data.add(mEmployee);
        }

        writeJsonResponse(response, data);
    }

    @RequestMapping(path = REQUEST_MAPPING_TAMBAH)
    public String showPageList(Model model,@RequestParam(value = "no", required = true) String no) {
        try {
            MRoleSubstantif mRoleSubstantif = roleSubstantifService.findOneByIdRoleSub(no);
            List<MRoleSubstantifDetail> list = roleSubstantifService.selectAllByRoleSubs("mRoleSubstantif.id", no);
            List<MSection> listSection = sectionService.findAll();
            List<MEmployee> listEmployee = employeeService.findAllMEmployee();
            
            model.addAttribute("listEmployee", listEmployee);
            model.addAttribute("list", list);
            model.addAttribute("mRoleSubstantif",mRoleSubstantif);
            model.addAttribute("listSection", listSection);

            model.addAttribute("attRoleId",no);

            return PAGE_LIST;
        }catch (NullPointerException e){
            e.printStackTrace();
            return REDIRECT_DASHBOARD;
        }
    }

    @PostMapping(value = REQUEST_MAPPING_AJAX_LIST)
    public void doGetListDataTables(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            DataTablesSearchResult dataTablesSearchResult = new DataTablesSearchResult();
            try {
                dataTablesSearchResult.setDraw(Integer.parseInt(request.getParameter("draw")));
            } catch (NumberFormatException e) {
                dataTablesSearchResult.setDraw(0);
            }

            int offset = 0;
            int limit = 50;
            try {
                offset = Math.abs(Integer.parseInt(request.getParameter("start")));
            } catch (NumberFormatException e) {
            }
            try {
                limit = Math.abs(Integer.parseInt(request.getParameter("length")));
            } catch (NumberFormatException e) {
            }

            String[] searchByArr = request.getParameterValues("searchByArr[]");
            String[] keywordArr = request.getParameterValues("keywordArr[]");
            List<KeyValue> searchCriteria = null;

            if (searchByArr != null) {
                searchCriteria = new ArrayList<>();
                for (int i = 0; i < searchByArr.length; i++) {
                    String searchBy = searchByArr[i];
                    String value = null;
                    try {
                        value = keywordArr[i];
                    } catch (ArrayIndexOutOfBoundsException e) {
                    }
                    if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                        if (StringUtils.isNotBlank(value)) {
                            searchCriteria.add(new KeyValue(searchBy, value, false));
                        }
                    }
                }
            }

            String orderBy = request.getParameter("order[0][column]");
            if (orderBy != null) {
                orderBy = orderBy.trim();
                if (orderBy.equalsIgnoreCase("")) {
                    orderBy = null;
                } else {
                    switch (orderBy) {
                        case "2":
                            orderBy = "mEmployee.employeeName";
                            break;
                        case "3":
                            orderBy = "mEmployee.mSection.name";
                            break;
                        default:
                            orderBy = null;
                            break;
                    }
                }
            }

            String orderType = request.getParameter("order[0][dir]");
            if (orderType == null) {
                orderType = "ASC";
            } else {
                orderType = orderType.trim();
                if (!orderType.equalsIgnoreCase("DESC")) {
                    orderType = "ASC";
                }
            }


            List<String[]> data = new ArrayList<>();

            GenericSearchWrapper<MRoleSubstantifDetail> searchResult = roleSubstantifService.searchGeneral(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (MRoleSubstantifDetail result : searchResult.getList()) {
                    no++;
                    data.add(new String[]{
                    		result.getId(),
                            "" + no,
                            result.getmEmployee().getEmployeeName(),
                            result.getmEmployee().getmSection().getName(),
                    //        "<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_HAPUS_DETAIL + "?no=" + result.getId()) + "\">Hapus</a>&nbsp&nbsp"
                    });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @GetMapping(REQUEST_MAPPING_TAMBAH_DETAIL)
    public String showPageTambah(Model model,@RequestParam(value = "no", required = true) String no) {
        MRoleSubstantif mRoleSubstantif = roleSubstantifService.findOneByIdRoleSub(no);


        model.addAttribute("mRoleSubstantif", mRoleSubstantif);
        return PAGE_TAMBAH;
    }

    @PostMapping(REQUEST_MAPPING_TAMBAH_AJAX)
    public void doSaveForm(@RequestBody DataDetailSubstantif form , final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (isAjaxRequest(request)) {
        	Map<String, Object> result = new HashMap<>();
        	result.put("success", false);

        	MRoleSubstantif mRoleSubstantif = roleSubstantifService.selectOne("id", form.getRoleSubsId());
        	MEmployee mEmployee = employeeService.findOne(form.getEmployeeId());
        	MRoleSubstantifDetail m = new MRoleSubstantifDetail();
        	m.setmRoleSubstantif(mRoleSubstantif);
        	m.setmEmployee(mEmployee);

            if (result.size() == 1) {
	            m.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
	            m.setCreatedDate(new Timestamp(System.currentTimeMillis()));

	            try {
		            roleSubstantifService.insertSubDetail(m);

		            result.put("success", true);
	            } catch (DataIntegrityViolationException e) {
		            logger.error(e.getMessage(), e);
		            if (e.getMessage().startsWith(HttpStatus.ALREADY_REPORTED.getReasonPhrase())) {
		            	if (e.getMessage().endsWith("roleSubsId")) {
				            result.put("nameFieldError", getMessage(FieldValidationUtil.FIELD_ERROR_NOT_UNIQUE, "Seksi"));
			            } else {
				            result.put("codeFieldError", getMessage(FieldValidationUtil.FIELD_ERROR_NOT_UNIQUE, "Nama Karyawan"));
			            }
		            } else {
		            	result.put("error", "Gagal menyimpan karyawan.");
		            }
	            }
            }

	        writeJsonResponse(response, result);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @PostMapping(REQUEST_MAPPING_TAMBAH_DETAIL)
    public String doProsesTambah(@RequestBody MRoleSubstantifDetail form, final Model model, /*@ModelAttribute("form") MRoleSubstantifDetail form,*/ /*@RequestParam(value = "no", required = true) String no, */final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
        // validate form
    	//return "";
    	/*if (ValidationUtil.isBlank(no)) {*/
    		FieldValidationUtil.required(errors, "mRoleSubstantif.id", form.getmRoleSubstantif().getCurrentId(), "mRoleSubstantif.id");
            FieldValidationUtil.required(errors, "mEmployee", form.getmEmployee().getCurrentId(), "mEmployee.id");
            // validate form end

            if (!errors.hasErrors()) {
                form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                form.setCreatedDate(new Timestamp(System.currentTimeMillis()));

                try {
                    roleSubstantifService.insert(form);

                    return REDIRECT_LIST_TAMBAH_DETAIL+"?no="+form.getmRoleSubstantif().getCurrentId();
                } catch (DataIntegrityViolationException e) {
                    logger.error(e.getMessage(), e);
                    model.addAttribute("errorMessage", "Gagal menambahkan Jenis Permohonan");
                }
            }
            return PATH_TAMBAH;
    	/*}
        return PATH_TAMBAH+"?no="+form.getmRoleSubstantif().getCurrentId();*/
    }

    @GetMapping(REQUEST_MAPPING_HAPUS_DETAIL)
    public void doProcesHapus(@RequestParam(value = "id", required = true) String id, final Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
    	String message = "SUCCESS";
    	MRoleSubstantifDetail mRoleSubstantifDetail = roleSubstantifService.selectOneByIdRoleSubDetail(id);
    	
    	if (mRoleSubstantifDetail.getTxServDistList().size() > 0) {
    		message = "User tidak dapat di hapus, karena masih melakukan pemeriksaan permohonan";
    	} else {
    		roleSubstantifService.deleteRoleSubsDetail(id);
    	}
    	writeJsonResponse(response, message);
    }
}
