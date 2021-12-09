package com.docotel.ki.controller.master;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.MCity;
import com.docotel.ki.model.master.MProvince;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.service.master.CityService;
import com.docotel.ki.service.master.CountryService;
import com.docotel.ki.service.master.ProvinceService;
import com.docotel.ki.util.FieldValidationUtil;
import com.docotel.ki.util.ValidationUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class MKotaController extends BaseController {
    @Autowired
    ProvinceService provinsiService;
    @Autowired
    CountryService countryService;
    @Autowired
    CityService cityService;

    private static final String DIRECTORY_PAGE = "master/provinsi-kota/";

	private static final String PAGE_EDIT = DIRECTORY_PAGE + "edit-kota";
	private static final String PAGE_LIST = DIRECTORY_PAGE + "list-kota";

	public static final String PATH_ADD = "/tambah-kota";
	public static final String PATH_EDIT = "/edit-kota";
	public static final String PATH_LIST = "/daftar-kota";

	public static final String PATH_AJAX_LIST = "/cari-kota";

    private static final String REQUEST_MAPPING_ADD = PATH_ADD + "*";
	private static final String REQUEST_MAPPING_EDIT = PATH_EDIT + "*";
	private static final String REQUEST_MAPPING_LIST = PATH_LIST + "*";
	private static final String REQUEST_MAPPING_AJAX_LIST = PATH_AJAX_LIST + "*";

    private static final String REDIRECT_LIST = "redirect:" + PATH_AFTER_LOGIN + PATH_LIST;

    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "master");
        model.addAttribute("subMenu", "listProvinsiKota");
    }

    @RequestMapping(path = REQUEST_MAPPING_LIST)
    public String showPageList(Model model, @RequestParam(value = "no", required = false) String no) {
        if (!ValidationUtil.isBlank(no)) {
            MProvince mProvince = provinsiService.findOne(no);
            if (mProvince != null) {
                model.addAttribute("mProvince", mProvince);

                return PAGE_LIST;
            }
        }
        return MProvinsiController.REDIRECT_TO_LIST;
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
						case "1" :
							orderBy = "code";
							break;
						case "2" :
							orderBy = "name";
							break;
						case "3" :
							orderBy = "createdBy.username";
							break;
						case "4" :
							orderBy = "statusFlag";
							break;
						case "5" :
							orderBy = "createdDate";
							break;
						default :
							orderBy = "code";
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

            GenericSearchWrapper<MCity> searchResult = cityService.searchGeneral(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (MCity result : searchResult.getList()) {
                    Date date = new Date(result.getCreatedDate().getTime());
                    String pDate = new SimpleDateFormat("dd-MM-yyyy").format(date);
                    String stts = ((result.isStatusFlag() == true) ? "Aktif" : "Tidak Aktif");
                    
                    // For user role access button menu
                    MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    String button = "";
                    if(mUser.hasAccessMenu("T-UKT")) {
                    	button = "<div class=\"btn-actions\">"
                                + "<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT + "?no=" + result.getId()) + "\">Ubah</a>"
                                + "</div>";
                    }

                    no++;
                    data.add(new String[]{
		                    "" + no,
                            result.getCode(),
		                    result.getName(),
		                    stts,
							result.getCreatedBy().getUsername(),
                            pDate,
                            button
                            /*"<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT + "?no=" + result.getId()) + "\">Ubah</a>"*/
                    });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @PostMapping(REQUEST_MAPPING_ADD)
    public void doAjaxAdd(@RequestBody MCity form, final HttpServletRequest request, final HttpServletResponse response) {
	    if (isAjaxRequest(request)) {
		    Map<String, Object> result = new HashMap<>();
		    result.put("success", false);

            // validate form
		    if (ValidationUtil.isBlank(form.getName())) {
			    result.put("nameFieldError", getMessage(FieldValidationUtil.FIELD_ERROR_REQUIRED, "Nama kota"));
		    } else if (ValidationUtil.isLengthGreaterThan(form.getName(), 255)) {
			    result.put("nameFieldError", getMessage(FieldValidationUtil.FIELD_ERROR_MAXLENGTH, "Nama kota", 255));
		    }

		    if (ValidationUtil.isBlank(form.getCode())) {
			    result.put("codeFieldError", getMessage(FieldValidationUtil.FIELD_ERROR_REQUIRED, "Kode kota"));
		    } else if (ValidationUtil.isLengthGreaterThan(form.getCode(), 5)) {
			    result.put("codeFieldError", getMessage(FieldValidationUtil.FIELD_ERROR_MAXLENGTH, "Kode kota", 5));
		    }
		    // validate form end

		    if (result.size() == 1) {
			    form.setStatusFlag(true);
			    form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

			    try {
				    cityService.insert(form);

				    result.put("success", true);
			    } catch (DataIntegrityViolationException e) {
				    logger.error(e.getMessage(), e);
				    logger.error(e.getMessage(), e);
				    if (e.getMessage().startsWith(HttpStatus.ALREADY_REPORTED.getReasonPhrase())) {
					    if (e.getMessage().endsWith("name")) {
						    result.put("nameFieldError", getMessage(FieldValidationUtil.FIELD_ERROR_NOT_UNIQUE, "Nama kota"));
					    } else {
						    result.put("codeFieldError", getMessage(FieldValidationUtil.FIELD_ERROR_NOT_UNIQUE, "Kode kota"));
					    }
				    } else {
					    if (e.getMessage().startsWith(HttpStatus.NOT_FOUND.getReasonPhrase())) {
						    result.put("error", "Parameter provinsi tidak ditemukan.");
					    } else {
						    result.put("error", "Gagal menyimpan kota.");
					    }
				    }
			    }
		    }
		    writeJsonResponse(response, result);
	    } else {
		    response.setStatus(HttpServletResponse.SC_FOUND);
	    }
    }

    @GetMapping(REQUEST_MAPPING_EDIT)
    public String showPageEdit(Model model, @RequestParam(value = "no", required = true) String no) {
	    if (!ValidationUtil.isBlank(no)) {
		    MCity mCity = cityService.selectOne("id", no);
		    if (mCity != null) {
			    model.addAttribute("form", mCity);
			    return PAGE_EDIT;
		    }
	    }
	    return MProvinsiController.REDIRECT_TO_LIST + "?error=Data Kota tidak ditemukan";
    }

    @PostMapping(REQUEST_MAPPING_EDIT)
    public String doProsesEdit(@ModelAttribute("form") MCity form, @RequestParam(value = "no", required = false) String no, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
	    if (!ValidationUtil.isBlank(no)) {
		    MCity mCity = cityService.findOne(no);
		    if (mCity != null) {
			    // validate form
			    if (FieldValidationUtil.required(errors, "name", form.getName(), "Nama kota")) {
				    FieldValidationUtil.maxLength(errors, "name", form.getName(), "Nama kota", 255);
			    }
			    if (FieldValidationUtil.required(errors, "code", form.getCode(), "Kode kota")) {
				    FieldValidationUtil.maxLength(errors, "code", form.getCode(), "Kode kota", 6);
			    }
			    if (FieldValidationUtil.required(errors, "active", form.getActive(), "Status")) {
				    FieldValidationUtil.listValue(errors, "active", form.getActive(), "Status", "true", "false");
			    }
			    // validate form end

			    form.setId(mCity.getId());
			    form.setmProvince(mCity.getmProvince());

			    if (!errors.hasErrors()) {
				    form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
				    form.setCreatedDate(new Timestamp(System.currentTimeMillis()));

				    try {
					    cityService.saveOrUpdate(form);

					    return REDIRECT_LIST + "?no=" + form.getmProvince().getCurrentId();
				    } catch (DataIntegrityViolationException e) {
					    logger.error(e.getMessage(), e);
					    if (e.getMessage().startsWith(HttpStatus.ALREADY_REPORTED.getReasonPhrase())) {
						    if (e.getMessage().endsWith("name")) {
							    errors.rejectValue("name", FieldValidationUtil.FIELD_ERROR_NOT_UNIQUE, new Object[]{"Nama kota"}, "");
						    } else {
							    errors.rejectValue("code", FieldValidationUtil.FIELD_ERROR_NOT_UNIQUE, new Object[]{"Kode kota"}, "");
						    }
					    } else {
						    model.addAttribute("errorMessage", "Gagal menambahkan kota");
					    }
				    }
			    }

			    return PAGE_EDIT;
		    }
	    }
	    return MProvinsiController.REDIRECT_TO_LIST + "?error=Data kota tidak ditemukan";
    }

}
