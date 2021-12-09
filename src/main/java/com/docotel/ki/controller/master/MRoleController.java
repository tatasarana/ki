package com.docotel.ki.controller.master;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.*;
import com.docotel.ki.pojo.*;
import com.docotel.ki.service.master.MenuService;
import com.docotel.ki.service.master.RoleService;
import com.docotel.ki.util.FieldValidationUtil;
import com.docotel.ki.model.master.*;
import com.docotel.ki.pojo.*;
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
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class MRoleController extends BaseController {

    @Autowired
    private RoleService roleService;
    @Autowired
    private MenuService menuService;

    private static final String DIRECTORY_PAGE = "master/role/";

    private static final String PAGE_LIST = DIRECTORY_PAGE + "list-role";
    private static final String PAGE_TAMBAH = DIRECTORY_PAGE + "tambah-role";
    private static final String PAGE_EDIT = DIRECTORY_PAGE + "edit-role";
    private static final String PAGE_INPUT = DIRECTORY_PAGE + "input-role";

    private static final String PATH_LIST = "/list-role";
    private static final String PATH_EDIT = "/edit-role";
    private static final String PATH_TAMBAH = "/tambah-role";
    private static final String PATH_INPUT = "/input-role";
    private static final String PATH_ROLE_STATUS = "/tambah-role-status";

    private static final String PATH_AJAX_LIST = "/cari-role";
    private static final String PATH_AJAX_PILIH_MENU_DETAIL = "/pilih-menu";
    private static final String PATH_TAMBAH_MENU_DETAIL = "/tambah-menu-detail";

    private static final String REQUEST_MAPPING_AJAX_LIST = PATH_AJAX_LIST + "*";
    private static final String REQUEST_MAPPING_TAMBAH = PATH_TAMBAH + "*";
    private static final String REQUEST_MAPPING_EDIT = PATH_EDIT + "*";
    private static final String REQUEST_MAPPING_INPUT = PATH_INPUT + "*";
    private static final String REQUEST_MAPPING_AJAX_PILIH_MENU_DETAIL = PATH_AJAX_PILIH_MENU_DETAIL + "*";
    private static final String REQUEST_MAPPING_ADD_MENU_DETAIL = PATH_TAMBAH_MENU_DETAIL + "*";

    private static final String REDIRECT_TO_LIST = "redirect:" + PATH_AFTER_LOGIN + PATH_LIST;

    private static final String REDIRECT_TO_ADD = "redirect:" + PATH_AFTER_LOGIN + PATH_INPUT;

    private String currentName = "";

    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "usermgmnt");
        model.addAttribute("subMenu", "listRole");

        List<MMenuDetail> listMenuDetail = menuService.findAllMenuDetail();
        List<MMenu> listMenu = menuService.findMMenuByStatusFlagTrue();

        model.addAttribute("listMenuDetail", listMenuDetail);
        model.addAttribute("listMenu", listMenu);

        if (request.getRequestURI().contains(PATH_TAMBAH)) {
            if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
                model.addAttribute("form", new MRole());
            }
        }

        // For user role access button menu
        MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String buttons = "";
        if (mUser.hasAccessMenu("T-TRL")) {
            buttons = "<div class=\"btn-actions\">"
                    + "<a class=\"btn btn-info\" href=\"" + getPathURLAfterLogin(PATH_TAMBAH) + "\"><i class=\"fas fa-plus\"></i> Tambah</a> "
                    + "</div>";
        }
        model.addAttribute("button", buttons);
    }

    @RequestMapping(path = REQUEST_MAPPING_AJAX_PILIH_MENU_DETAIL)
    public void pilihMenu(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        //request data ke database m_menu_detail
        String target = request.getParameter("target");
        List<MMenuDetail> lisMenuDetail = new ArrayList<>();

        if (target != null) {
            target = target.trim();
            if (!target.equalsIgnoreCase("")) {
                lisMenuDetail = roleService.selectAllByMenu(target);
            }
        }

        List<MMenuDetail> data = new ArrayList<>();
        MMenuDetail mMenuDetail = null;
        for (MMenuDetail result : lisMenuDetail) {
            mMenuDetail = new MMenuDetail();
            mMenuDetail.setId(result.getId());
            mMenuDetail.setDesc(result.getDesc());
            data.add(mMenuDetail);
        }
        writeJsonResponse(response, data);
    }

    @RequestMapping(path = REQUEST_MAPPING_ADD_MENU_DETAIL)
    public String doSaveMenuDetail() {
        return PAGE_TAMBAH;
    }

    @RequestMapping(path = PATH_LIST)
    public String showPageList(@RequestParam(value = "error", required = false) String error, Model model) {
        //List<MRole> list = roleService.findAll();
        List<MRole> list = roleService.selectAllRole();
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
                            orderBy = "no";
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

            GenericSearchWrapper<MRole> searchResult = roleService.searchGeneral(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (MRole result : searchResult.getList()) {
                    Date date = new Date(result.getCreatedDate().getTime());
                    String pDate = new SimpleDateFormat("dd-MM-yyyy").format(date);
                    String stts = ((result.isStatusFlag() == true) ? "Aktif" : "Tidak Aktif");

                    // For user role access button menu
                    MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    String button = "";
                    if (mUser.hasAccessMenu("T-MRL")) {
                        button = "<div class=\"btn-actions\">" +
                                "<a class=\"btn btn-primary btn-xs\" href=\"" + getPathURLAfterLogin(PATH_INPUT + "?no=" + result.getId()) + "\">Menu </a>" +
                                "</div>";
                    }
                    if (mUser.hasAccessMenu("T-URL")) {
                        button = "<div class=\"btn-actions\">" +
                                "<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT + "?no=" + result.getId()) + "\">Ubah</a>" +
                                "</div>";
                    }
                    if (mUser.hasAccessMenu("T-MRL") && mUser.hasAccessMenu("T-URL")) {
                        button = "<div class=\"btn-actions\">" +
                                "<a class=\"btn btn-primary btn-xs\" href=\"" + getPathURLAfterLogin(PATH_INPUT + "?no=" + result.getId()) + "\">Menu </a>" +
                                "<br />" +
                                "<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT + "?no=" + result.getId()) + "\">Ubah</a>" +
                                "</div>";
                    }

                    no++;
                    data.add(new String[]{
                            "" + no,
                            result.getName(),
                            result.getDesc(),
                            stts,
                            result.getCreatedBy().getUsername(),
                            pDate,
                            button
                            /*"<div class=\"btn-actions\">" +
                                    "<a class=\"btn btn-primary btn-xs\" href=\"" + getPathURLAfterLogin(PATH_INPUT + "?no=" + result.getId()) + "\">Menu </a>" +
                                    "<br />" +
                            		"<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT + "?no=" + result.getId()) + "\">Ubah</a>" +

                            "</div>"*/

                    });
//                    "<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_ROLE_STATUS + "?no=" + result.getId()) + "\">Status</a>"
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @GetMapping(REQUEST_MAPPING_TAMBAH)
    public String showPageTambah(final Model model) {

        return PAGE_TAMBAH;
    }

    @PostMapping(REQUEST_MAPPING_TAMBAH)
    public String doProsesTambah(@ModelAttribute("form") MRole form, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
        // validate form
        FieldValidationUtil.required(errors, "name", form.getName(), "name");
        FieldValidationUtil.required(errors, "desc", form.getDesc(), "desc");
        // validate form end


        if (!errors.hasErrors()) {
            form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            form.setStatusFlag(true);

            try {
                MRole isExist = roleService.selectOne("name", form.getName());
                if (isExist == null) {
                    roleService.insert(form);

                    model.asMap().clear();
                    return REDIRECT_TO_LIST;
                } else {
                    model.addAttribute("errorMessage", "Nama Role " + form.getName() + " Sudah Ada");
                }

            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                model.addAttribute("errorMessage", "Gagal menambahkan Role");
            }
        }
        return PAGE_TAMBAH;
    }

    @GetMapping(REQUEST_MAPPING_EDIT)
    public String showPageEdit(Model model, @RequestParam(value = "no", required = true) String no) {
        MRole mRole = roleService.selectMroleById(no);
        if (mRole != null) {
            model.addAttribute("form", mRole);
            currentName = mRole.getName();
            return PAGE_EDIT;
        }
        return REDIRECT_TO_LIST + "?error=Role tidak ditemukan";
    }

    @PostMapping(REQUEST_MAPPING_EDIT)
    public String doProsesEdit(@ModelAttribute("form") MRole form, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
        // validate form
        FieldValidationUtil.required(errors, "id", form.getCurrentId(), "id");
        FieldValidationUtil.required(errors, "name", form.getName(), "name");
        FieldValidationUtil.required(errors, "desc", form.getDesc(), "desc");
        // validate form end

        if (!errors.hasErrors()) {
            form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            try {
                MRole isExist = roleService.selectOne("name", form.getName());
                if (isExist == null || currentName.equalsIgnoreCase(form.getName())) {
                    roleService.saveOrUpdate(form);

                    model.asMap().clear();
                    return REDIRECT_TO_LIST;
                } else {
                    model.addAttribute("errorMessage", "Nama Role " + form.getName() + " Sudah Ada");
                }

            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                model.addAttribute("errorMessage", "Gagal Mengubah Role");
            }
        }
//        return PAGE_EDIT + "?no=" + form.getCurrentId();
        return showPageEdit(model, form.getCurrentId());
    }

    @GetMapping(REQUEST_MAPPING_INPUT)
    public String showPageInput(Model model, @RequestParam(value = "no", required = true) String no) {
        MRole mRole = roleService.selectMroleById(no);
        if (mRole != null) {
            List<MRoleDetail> mRoleDetail = roleService.selectAllByRole(no);

            List<TempMenuDetaill> tempMenuDetaillList = new ArrayList<>();
            RoleDetail rd = new RoleDetail();

            for (MRoleDetail mrd : mRoleDetail) {
                MMenuDetail mMenuDetail = menuService.findOneMenuDetail(mrd.getmMenuDetail().getId());
                TempMenuDetaill tempMenuDetaill = new TempMenuDetaill();
                tempMenuDetaill.setId(mMenuDetail.getId());
                tempMenuDetaill.setNmMenu(mMenuDetail.getmMenu().getName());
                tempMenuDetaill.setNmMenuDetail(mMenuDetail.getDesc());

                tempMenuDetaillList.add(tempMenuDetaill);
            }

            rd.setIdRole(mRole.getCurrentId());
            rd.setName(mRole.getName());
            rd.setDesc(mRole.getDesc());
            rd.setRoleDetailItems(tempMenuDetaillList);
            model.addAttribute("form", rd);
            return PAGE_INPUT;
        }
        return REDIRECT_TO_LIST;
    }

    @PostMapping(REQUEST_MAPPING_INPUT)
    public String doSaveRoleDetail(@ModelAttribute("form") RoleDetail form, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
        if (form.getIdRoleDetail() != null) {
            /*String[] unique = Arrays.stream(form.getIdRoleDetail()).distinct().toArray(String[]::new);
            form.setIdRoleDetail(unique);*/
            roleService.saveRoleDetail(form);

            model.asMap().clear();
            return REDIRECT_TO_LIST;
        }
        model.asMap().clear();
        return "redirect:" + REDIRECT_TO_ADD + "?no=" + form.getIdRole();
    }
}
