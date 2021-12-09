package com.docotel.ki.controller.master;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.*;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.util.FieldValidationUtil;
import com.docotel.ki.model.master.*;
import com.docotel.ki.service.master.CityService;
import com.docotel.ki.service.master.ConsultantService;
import com.docotel.ki.service.master.CountryService;
import com.docotel.ki.service.master.ProvinceService;
import com.docotel.ki.service.master.UserService;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class MKonsultanController extends BaseController {
    @Autowired
    ConsultantService consultantService;
    @Autowired
    CountryService countryService;
    @Autowired
    ProvinceService provinceService;
    @Autowired
    CityService cityService;
    @Autowired
    UserService userService;

    private static final String DIRECTORY_PAGE = "master/konsultan/";

    private static final String PAGE_LIST = DIRECTORY_PAGE + "list-konsultan";
    private static final String PAGE_TAMBAH = DIRECTORY_PAGE + "tambah-konsultan";
    private static final String PAGE_EDIT = DIRECTORY_PAGE + "edit-konsultan";


    private static final String PATH = "/list-konsultan";
    private static final String PATH_EDIT = "/edit-konsultan";
    private static final String PATH_AJAX_LIST = "/cari-konsultan";
    private static final String PATH_TAMBAH = "/tambah-konsultan";

    public static final String REDIRECT_LOKET = "redirect:" + PATH_AFTER_LOGIN + PATH;

    private static final String REQUEST_MAPPING_AJAX_LIST = PATH_AJAX_LIST + "*";
    private static final String REQUEST_MAPPING_TAMBAH = PATH_TAMBAH + "*";
    private static final String REQUEST_MAPPING_EDIT = PATH_EDIT + "*";

    private static final String REDIRECT_TO_LIST = "redirect:" + PATH_AFTER_LOGIN + PATH;

    private String currentNomor = "";
    private String currentName = "";


    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "maintenance");
        model.addAttribute("subMenu", "listKonsultan");

        if (request.getRequestURI().contains(PATH_TAMBAH)) {
            List<MCountry> mCountryList = countryService.findByStatusFlagTrueOrderByName();
            List<MProvince> mProvinceList = provinceService.findByStatusFlagTrueOrderByName();
            List<MCity> mCityList = cityService.findByStatusFlagTrueOrderByName();
            model.addAttribute("listCountry", mCountryList);
            model.addAttribute("listProvince", mProvinceList);
            model.addAttribute("listCity", mCityList);

            if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
                model.addAttribute("form", new MRepresentative());
            }
        } else if (request.getRequestURI().contains(PATH_EDIT)) {
            List<MCountry> mCountryList = countryService.findByStatusFlagTrueOrderByName();
            List<MProvince> mProvinceList = provinceService.findByStatusFlagTrueOrderByName();
            List<MCity> mCityList = cityService.findByStatusFlagTrueOrderByName();
            
            model.addAttribute("listCountry", mCountryList);
            model.addAttribute("listProvince", mProvinceList);
            model.addAttribute("listCity", mCityList);
        }
    }

    @RequestMapping(path = PATH)
    public String showPageList(@RequestParam(value = "error", required = false) String error, Model model) {
//        List<MRepresentative> list = consultantService.findAll();
        List<MRepresentative> list = new ArrayList<>();
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
                            orderBy = "no";
                            break;
                        case "2":
                            orderBy = "name";
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

            GenericSearchWrapper<MRepresentative> searchResult = consultantService.searchGeneral(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (MRepresentative result : searchResult.getList()) {
                    Date date = new Date(result.getCreatedDate().getTime());
                    Boolean statusString = result.isStatusFlag();
                    String statusOke;
                    if (statusString == true) {
                        statusOke = "Aktif";
                    } else {
                        statusOke = "Tidak Aktif";
                    }
                    String pDate = new SimpleDateFormat("dd-MM-yyyy").format(date);

                    // For user role access button menu
                    MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    String button = "";
                    if (mUser.hasAccessMenu("T-UKUA")) {
                        button = "<div class=\"btn-actions\">"
                                + "<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT + "?no=" + result.getId()) + "\">Ubah</a>"
                                + "</div>";
                    }
                    String prov = null;
                    try {
                        prov = ", "+result.getmProvince().getName();
                    } catch (NullPointerException e) {
                    } catch (Exception e) {
                    }

                    String city = null;
                    try {
                        city = ", "+result.getmCity().getName();
                    } catch (NullPointerException e) {
                    } catch (Exception e) {
                    }

                    String zipcode = null;
                    try {
                        zipcode = "  "+result.getZipCode();
                    } catch (NullPointerException e) {
                    } catch (Exception e) {
                    }

                    no++;
                    data.add(new String[]{
                            "" + no,
                            result.getNo(),
                            result.getName(),
                            result.getOffice(),
                            result.getAddress(),
                            result.getPhone(),
                            result.getEmail(),
                            statusOke,
                            result.getCreatedBy().getUsername(),
                            pDate,
                            button
                            /*"<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT + "?no=" + result.getId()) + "\">Ubah</a>",*/
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
    public String doProsesTambah(@ModelAttribute("form") MRepresentative form, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
        // validate form 
        FieldValidationUtil.required(errors, "no", form.getNo(), "Nomor Konsultan");
        FieldValidationUtil.required(errors, "name", form.getName(), "Nama Konsultan");
        FieldValidationUtil.required(errors, "mCountry.id", form.getmCountry().getCurrentId(), "Nama Negara");
        FieldValidationUtil.required(errors, "address", form.getAddress(), "Alamat");
        FieldValidationUtil.required(errors, "phone", form.getPhone(), "Nomor Telepon");
        //FieldValidationUtil.email(errors, "email", form.getEmail(), "Email");

        if (!errors.hasFieldErrors("no")) {
            MRepresentative mReprsVal = consultantService.findReprsByNo(form.getNo());
            if (mReprsVal != null) {
                model.addAttribute("errorMessage", "Gagal menambahkan konsultan - Nomor Konsultan " + mReprsVal.getNo() + " Sudah Ada");
                return PAGE_TAMBAH;
            }
        }

        if (!errors.hasFieldErrors("name")) {
            MRepresentative mReprsVal = consultantService.findReprsByName(form.getName());
            if (mReprsVal != null) {
                model.addAttribute("errorMessage", "Gagal menambahkan konsultan - Nama Konsultan " + mReprsVal.getName() + " Sudah Ada");
                return PAGE_TAMBAH;
            }
        }
        // validate form end

        if (!errors.hasErrors()) {
            form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            form.setStatusFlag(true);
            if (form.getmCity() != null && form.getmCity().getCurrentId() == null) {
                form.setmCity(null);
            }
            if (form.getmProvince() != null && form.getmProvince().getCurrentId() == null) {
                form.setmProvince(null);
            }
            try {
                consultantService.insert(form);

                return PAGE_LIST;
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                model.addAttribute("errorMessage", "Gagal menambahkan konsultan - Nomor Konsultan " + form.getNo() + " Sudah Ada");
            }
        }

        return PAGE_TAMBAH;
    }

    @GetMapping(REQUEST_MAPPING_EDIT)
    public String doShowPageEdit(Model model, @RequestParam(value = "no", required = true) String no) {
        MRepresentative mRepresentative = consultantService.findReprsById(no);
        if (mRepresentative != null) {
            model.addAttribute("form", mRepresentative);
            currentNomor = mRepresentative.getNo();
            currentName = mRepresentative.getName();
            return PAGE_EDIT;
        }
        return REDIRECT_TO_LIST + "?error=Data Konsultan tidak ditemukan";
    }

    @PostMapping(REQUEST_MAPPING_EDIT)
    public String doProsesEdit(@ModelAttribute("form") MRepresentative form, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
        // validate form 
        FieldValidationUtil.required(errors, "no", form.getNo(), "Nomor Konsultan");
        FieldValidationUtil.required(errors, "name", form.getName(), "Nama Konsultan");
        FieldValidationUtil.required(errors, "mCountry.id", form.getmCountry().getCurrentId(), "Nama Negara");
        FieldValidationUtil.required(errors, "address", form.getAddress(), "Alamat");
        FieldValidationUtil.required(errors, "phone", form.getPhone(), "Nomor Telepon");
        //FieldValidationUtil.email(errors, "email", form.getEmail(), "Email");

//        MRepresentative mReprsVal = consultantService.findReprsByNo(form.getNo());
//        if (mReprsVal != null && !mReprsVal.getId().equals(form.getId()))
//            FieldValidationUtil.uniqueValue(errors, "no", form.getNo(), "No Konsultan", mReprsVal.getNo());
//
//        mReprsVal = consultantService.findReprsByName(form.getName());
//        if (mReprsVal != null && !mReprsVal.getId().equals(form.getId()))
//            FieldValidationUtil.uniqueValue(errors, "name", form.getName().toLowerCase(), "Nama Konsultan", mReprsVal.getName().toLowerCase());
        // validate form end

        if (!errors.hasErrors()) {
            form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            if (form.getmCity() != null && form.getmCity().getCurrentId() == null) {
                form.setmCity(null);
            }
            if (form.getmProvince() != null && form.getmProvince().getCurrentId() == null) {
                form.setmProvince(null);
            }
            try {

                MRepresentative isExistNo = consultantService.findReprsByNo(form.getNo());
                MUser selectOneUserByUsername = userService.selectUserByUsername(form.getUserId().getUsername());
                if(isExistNo.getUserId() != null) {
                	form.setUserId(selectOneUserByUsername);
                } else {
                	form.setUserId(null);
                }
                
                if ((isExistNo == null || currentNomor.equalsIgnoreCase(form.getNo()))) {
                    consultantService.update(form);
                    return PAGE_LIST;
                } else {
                    if (isExistNo == null) {
                        model.addAttribute("errorMessage", "Nomor Konsultan " + form.getNo() + " Sudah Ada");
                    }
                }

            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                model.addAttribute("errorMessage", "Gagal menambahkan konsultan - Nomor Konsultan " + form.getNo() + " Sudah Ada");
            }
        }
        return doShowPageEdit(model, form.getCurrentId());
    }
}
