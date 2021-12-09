package com.docotel.ki.controller.master;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.MRoleSubstantif;
import com.docotel.ki.model.master.MRoleSubstantifDetail;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.service.master.RoleSubstantifService;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.util.FieldValidationUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class MRoleSubstantifController extends BaseController {

    @Autowired
    RoleSubstantifService roleSubstantifService;

    private static final String DIRECTORY_PAGE = "master/role-substantif/";

    /*-----MRoleSubstantif Section-----*/
    private static final String PAGE_LIST = DIRECTORY_PAGE + "list-role-substantif";
    private static final String PAGE_TAMBAH = DIRECTORY_PAGE + "tambah-role-substantif";
    private static final String PAGE_EDIT = DIRECTORY_PAGE + "edit-role-substantif";

    private static final String PATH_LIST = "/list-role-substantif";
    private static final String PATH_EDIT = "/edit-role-substantif";
    private static final String PATH_TAMBAH = "/tambah-role-substantif";
    private static final String PATH_TAMBAH_DETAIL = "/tambah-detail";
    private static final String PATH_AJAX_LIST = "/cari-role-substantif";

    private static final String REQUEST_MAPPING_AJAX_LIST = PATH_AJAX_LIST + "*";
    private static final String REQUEST_MAPPING_TAMBAH = PATH_TAMBAH + "*";
    private static final String REQUEST_MAPPING_EDIT = PATH_EDIT + "*";

    private static final String REDIRECT_TO_LIST = "redirect:" + PATH_AFTER_LOGIN + PATH_LIST;
    /*-----End of MRoleSubstantif Section-----*/

    public List<MRoleSubstantifDetail> mRoleSubstantifDetailList;
    public String currentName;

    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "maintenance");
        model.addAttribute("subMenu", "listRoleSubstantif");

        if (request.getRequestURI().contains(PATH_TAMBAH)) {
            if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
                model.addAttribute("form", new MRoleSubstantif());
            }
        }
    }

    @RequestMapping(path = PATH_LIST)
    public String showPageList(@RequestParam(value = "error", required = false) String error, Model model) {
        List<MRoleSubstantif> list = roleSubstantifService.findAllRoleSub();
        model.addAttribute("list", list);

        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("errorMessage", error);
        }
        return PAGE_LIST;
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
                            if (searchBy.equals("mRoleSubstantifDetailList.mEmployee.employeeName")) {
                                searchCriteria.add(new KeyValue(searchBy, value, true));
                            } else {
                                searchCriteria.add(new KeyValue(searchBy, value, false));
                            }
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
                        case "1":
                            orderBy = "name";
                            break;
                        case "2":
                            orderBy = "desc";
                            break;
                        case "3":
                            orderBy = "statusFlag";
                            break;
                        case "4":
                            orderBy = "createdBy.username";
                            break;
                        case "5":
                            orderBy = "createdDate";
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

            GenericSearchWrapper<MRoleSubstantif> searchResult = roleSubstantifService.searchRoleSubstantif(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (MRoleSubstantif result : searchResult.getList()) {
                    String stts = ((result.isStatusFlag() == true) ? "Aktif" : "Tidak Aktif");

                    // For user role access button menu
                    MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    String button = "";
                    if (mUser.hasAccessMenu("T-URLS")) {
                        button = "<div class=\"btn-actions\">" +
                                "<a class=\"btn btn-primary btn-xs\" href=\"" + getPathURLAfterLogin(PATH_TAMBAH_DETAIL + "?no=" + result.getId()) + "\">User Role Substantif</a>" +
                                "<br />" +
                                "<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT + "?no=" + result.getId()) + "\">Ubah</a>" +
                                "</div>";
                    } else {
                        button = "<div class=\"btn-actions\">" +
                                "<a class=\"btn btn-primary btn-xs\" href=\"" + getPathURLAfterLogin(PATH_TAMBAH_DETAIL + "?no=" + result.getId()) + "\">User Role Substantif</a>" +
                                "</div>";
                    } 

                    no++;
                    data.add(new String[]{
                            "" + no,
                            result.getName(),
                            result.getDesc(),
                            stts,
                            result.getCreatedBy().getUsername(),
                            DateUtil.formatDate(result.getCreatedDate(), "dd-MM-yyyy"),
                            button
                            /*"<div class=\"btn-actions\">" +
                                    "<a class=\"btn btn-primary btn-xs\" href=\"" + getPathURLAfterLogin(PATH_TAMBAH_DETAIL + "?no=" + result.getId()) + "\">Daftar Karyawan</a>" +
                                    "<br />" +
                            		"<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT + "?no=" + result.getId()) + "\">Ubah</a>" +
                            "</div>"*/
                    });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @GetMapping(REQUEST_MAPPING_TAMBAH)
    public String showPageTambah(Model model) {
        return PAGE_TAMBAH;
    }

    @PostMapping(REQUEST_MAPPING_TAMBAH)
    public String doProsesTambah(@ModelAttribute("form") MRoleSubstantif form, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
        // validate form
        FieldValidationUtil.required(errors, "name", form.getName(), "name");
        FieldValidationUtil.required(errors, "desc", form.getDesc(), "desc");
        // validate form end

        if (!errors.hasErrors()) {
            form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            form.setStatusFlag(true);

            try {
                MRoleSubstantif isExist = roleSubstantifService.selectOne("name", form.getName());
                if (isExist==null) {
                    roleSubstantifService.insertRoleSubs(form);

                    return REDIRECT_TO_LIST;
                } else {
                    model.addAttribute("errorMessage", "Nama Role " + form.getName() + " Sudah Ada");
                }

            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                model.addAttribute("errorMessage", "Gagal menambahkan tipe permohonan");
            }
        }

        return PAGE_TAMBAH;
    }

    @GetMapping(REQUEST_MAPPING_EDIT)
    public String showPageEdit(Model model, @RequestParam(value = "no", required = true) String no) {
        MRoleSubstantif mRoleSubstantif = roleSubstantifService.findOneByIdRoleSub(no);
        if (mRoleSubstantif != null) {
            mRoleSubstantifDetailList = mRoleSubstantif.getmRoleSubstantifDetailList();
            model.addAttribute("form", mRoleSubstantif);
            currentName = mRoleSubstantif.getName();
            return PAGE_EDIT;
        }
        return REDIRECT_TO_LIST + "?error=Role Substantif tidak ditemukan";
    }

    @PostMapping(REQUEST_MAPPING_EDIT)
    public String doProsesEdit(@ModelAttribute("form") MRoleSubstantif form, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
        // validate form
//        FieldValidationUtil.required(errors, "id", form.getCurrentId(), "no");
        FieldValidationUtil.required(errors, "code", form.getName(), "code");
        FieldValidationUtil.required(errors, "desc", form.getDesc(), "desc");
        // validate form end

        if (!errors.hasErrors()) {
            form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            try {
                MRoleSubstantif isExist = roleSubstantifService.selectOne("name", form.getName());
                if (isExist==null || currentName.equalsIgnoreCase(form.getName())) {
                    form.setmRoleSubstantifDetailList(mRoleSubstantifDetailList);
                    roleSubstantifService.saveOrUpdateRoleSubs(form);

                    model.asMap().clear();
                    return REDIRECT_TO_LIST;
                } else {
                    model.addAttribute("errorMessage", "Nama Role " + form.getName() + " Sudah Ada");
                }

            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                model.addAttribute("errorMessage", "Gagal Mengubah Tipe permohonan");
            }
        }

        return showPageEdit(model,form.getCurrentId());
    }

}
