package com.docotel.ki.controller.master;

import com.docotel.ki.KiApplication;
import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.MProvince;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.service.master.CountryService;
import com.docotel.ki.service.master.ProvinceService;
import com.docotel.ki.util.FieldValidationUtil;
import com.docotel.ki.util.ValidationUtil;
import org.apache.commons.lang3.StringUtils;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
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
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class MProvinsiController extends BaseController {
    @Autowired
    ProvinceService provinsiService;
    @Autowired
    CountryService countryService;

    private static final String DIRECTORY_PAGE = "master/provinsi-kota/";

    private static final String PAGE_ADD = DIRECTORY_PAGE + "tambah-provinsi";
	private static final String PAGE_EDIT = DIRECTORY_PAGE + "edit-provinsi";
	private static final String PAGE_LIST = DIRECTORY_PAGE + "list-provinsi";

	private static final String PATH_ADD = "/tambah-provinsi";
	private static final String PATH_EDIT = "/edit-provinsi";
	private static final String PATH_LIST = "/list-provinsi";

	private static final String PATH_EXPORT_CLASS = "/ekspor-provinsi";
    private static final String PATH_AJAX_LIST = "/cari-provinsi";

    private static final String REQUEST_MAPPING_AJAX_LIST = PATH_AJAX_LIST + "*";
    private static final String REQUEST_MAPPING_ADD = PATH_ADD + "*";
	private static final String REQUEST_MAPPING_EDIT = PATH_EDIT + "*";

	private static final String REQUEST_EXPORT_CLASS = PATH_EXPORT_CLASS + "*";

    public static final String REDIRECT_TO_LIST = "redirect:" + PATH_AFTER_LOGIN + PATH_LIST;

    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "master");
        model.addAttribute("subMenu", "listProvinsiKota");
    }

    @RequestMapping(path = PATH_LIST)
    public String showPageList(@RequestParam(value = "error", required = false) String error,Model model) {
        List<MProvince> list = provinsiService.findAll();
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

            GenericSearchWrapper<MProvince> searchResult = provinsiService.searchGeneral(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (MProvince result : searchResult.getList()) {
                    Date date = new Date(result.getCreatedDate().getTime());
                    String pDate = new SimpleDateFormat("dd-MM-yyyy").format(date);
                    String stts = ((result.isStatusFlag() == true) ? "Aktif" : "Tidak Aktif");
                    
                    // For user role access button menu
                    MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    String button = "";
                    if(mUser.hasAccessMenu("T-UPR")) {
                    	button = "<div class=\"btn-actions\">" +
	                    		 "<a class=\"btn btn-primary btn-xs\" href=\"" + getPathURLAfterLogin(MKotaController.PATH_LIST + "?no=" + result.getId()) + "\">Daftar Kota</a> " +
                                 "<br />" +
                        		 "<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT + "?no=" + result.getId()) + "\">Ubah</a>" +
                        		 "</div>";
                    } else {
                    	button = "<div class=\"btn-actions\">" +
                                 "<a class=\"btn btn-primary btn-xs\" href=\"" + getPathURLAfterLogin(MKotaController.PATH_LIST + "?no=" + result.getId()) + "\">Daftar Kota</a> " +
                                 "</div>";
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
		                    /*"<div class=\"btn-actions\">" +
		                    		"<a class=\"btn btn-primary btn-xs\" href=\"" + getPathURLAfterLogin(MKotaController.PATH_LIST + "?no=" + result.getId()) + "\">Daftar Kota</a> " +
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

    @GetMapping(REQUEST_MAPPING_ADD)
    public String showPageAdd(Model model) {
        return PAGE_ADD;
    }

    @PostMapping(REQUEST_MAPPING_ADD)
    public void doAjaxAdd(@RequestBody MProvince form , final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (isAjaxRequest(request)) {
        	Map<String, Object> result = new HashMap<>();
        	result.put("success", false);

            if (ValidationUtil.isBlank(form.getName())) {
	            result.put("nameFieldError", getMessage(FieldValidationUtil.FIELD_ERROR_REQUIRED, "Nama provinsi"));
            } else if (ValidationUtil.isLengthGreaterThan(form.getName(), 150)) {
	            result.put("nameFieldError", getMessage(FieldValidationUtil.FIELD_ERROR_MAXLENGTH, "Nama provinsi", 150));
            }

            if (ValidationUtil.isBlank(form.getCode())) {
	            result.put("codeFieldError", getMessage(FieldValidationUtil.FIELD_ERROR_REQUIRED, "Kode provinsi"));
            } else if (ValidationUtil.isLengthGreaterThan(form.getCode(), 2)) {
	            result.put("codeFieldError", getMessage(FieldValidationUtil.FIELD_ERROR_MAXLENGTH, "Kode provinsi", 2));
            }

            if (result.size() == 1) {
	            form.setmCountry(KiApplication.INDONESIA);
	            form.setStatusFlag(true);
	            form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

	            try {
		            provinsiService.insert(form);

		            result.put("success", true);
	            } catch (DataIntegrityViolationException e) {
		            logger.error(e.getMessage(), e);
		            if (e.getMessage().startsWith(HttpStatus.ALREADY_REPORTED.getReasonPhrase())) {
		            	if (e.getMessage().endsWith("name")) {
				            result.put("nameFieldError", getMessage(FieldValidationUtil.FIELD_ERROR_NOT_UNIQUE, "Nama provinsi"));
			            } else {
				            result.put("codeFieldError", getMessage(FieldValidationUtil.FIELD_ERROR_NOT_UNIQUE, "Kode provinsi"));
			            }
		            } else {
		            	result.put("error", "Gagal menyimpan provinsi.");
		            }
	            }
            }

	        writeJsonResponse(response, result);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @GetMapping(REQUEST_MAPPING_EDIT)
    public String showPageEdit(Model model, @RequestParam(value = "no", required = false) String no) {
    	if (!ValidationUtil.isBlank(no)) {
		    MProvince mProvince = provinsiService.findOne(no);
		    if (mProvince != null) {
			    model.addAttribute("form", mProvince);
			    return PAGE_EDIT;
		    }
	    }
        return REDIRECT_TO_LIST + "?error=Data Provinsi tidak ditemukan";
    }

    @PostMapping(REQUEST_MAPPING_EDIT)
    public String doProsesEdit(@ModelAttribute("form") MProvince form, @RequestParam(value = "no", required = false) String no, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
	    if (!ValidationUtil.isBlank(no)) {
		    MProvince mProvince = provinsiService.findOne(no);
		    if (mProvince != null) {
			    // validate form
			    if (FieldValidationUtil.required(errors, "name", form.getName(), "name")) {
				    FieldValidationUtil.maxLength(errors, "name", form.getName(), "name", 255);
			    }
			    if (FieldValidationUtil.required(errors, "code", form.getCode(), "code")) {
				    FieldValidationUtil.maxLength(errors, "code", form.getCode(), "code", 2);
			    }
			    if (FieldValidationUtil.required(errors, "active", form.getActive(), "status")) {
				    FieldValidationUtil.listValue(errors, "active", form.getActive(), "status", "true", "false");
			    }
			    // validate form end

			    form.setId(mProvince.getId());
			    form.setmCountry(mProvince.getmCountry());

			    if (!errors.hasErrors()) {
				    form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
				    form.setCreatedDate(new Timestamp(System.currentTimeMillis()));

				    try {
					    provinsiService.saveOrUpdate(form);

					    model.asMap().clear();
					    return REDIRECT_TO_LIST;
				    } catch (DataIntegrityViolationException e) {
					    logger.error(e.getMessage(), e);
					    if (e.getMessage().startsWith(HttpStatus.ALREADY_REPORTED.getReasonPhrase())) {
						    if (e.getMessage().endsWith("name")) {
							    errors.rejectValue("name", FieldValidationUtil.FIELD_ERROR_NOT_UNIQUE, new Object[]{"nama provinsi"}, "");
						    } else {
							    errors.rejectValue("code", FieldValidationUtil.FIELD_ERROR_NOT_UNIQUE, new Object[]{"kode provinsi"}, "");
						    }
					    } else {
						    model.addAttribute("errorMessage", "Gagal melakukan update data provinsi");
					    }
				    }
			    }
			    return PAGE_EDIT;
		    }
	    }
	    return REDIRECT_TO_LIST + "?error=Data Provinsi tidak ditemukan";
    }
    
    @GetMapping(REQUEST_EXPORT_CLASS)
	public void doExportClass(HttpServletRequest request, HttpServletResponse response) {
		InputStream reportInputStream = null;

		String[] searchByArr = null;
		try {
			searchByArr = request.getParameter("searchByArr").split(",");
		} catch (Exception e) {
		}
		String[] keywordArr = null;
		try {
			keywordArr = request.getParameter("keywordArr").split(",");
		} catch (Exception e) {
		}
		String orderBy = request.getParameter("orderBy");
		String orderType = request.getParameter("orderType");

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
					if (searchBy.equalsIgnoreCase("desc")) {
						if (StringUtils.isNotBlank(value)) {
							searchCriteria.add(new KeyValue(searchBy, value, false));
						}
					} else if (StringUtils.isNotBlank(value)) {
						searchCriteria.add(new KeyValue(searchBy, value, true));
					}
				}
			}
		}

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

		if (orderType == null) {
			orderType = "ASC";
		} else {
			orderType = orderType.trim();
			if (!orderType.equalsIgnoreCase("DESC")) {
				orderType = "ASC";
			}
		}

		try {
			
			reportInputStream = getClass().getClassLoader().getResourceAsStream("report/list-provinsi.xls");

			List<MProvince> dataList = new ArrayList<>();

			int totalRecord = (int) provinsiService.countAll(searchCriteria);
			int retrieved = 0;
			while (retrieved < totalRecord) {
				List<MProvince> retrievedDataList = provinsiService.selectAll(searchCriteria, orderBy, orderType, retrieved, 1000);
				dataList.addAll(retrievedDataList);
				retrieved += retrievedDataList.size();
			}

			Context context = new Context();
			context.putVar("dataList", dataList);

			response.setContentType("application/vnd.ms-excel");
			response.setHeader("Content-Disposition", "attachment; filename=daftar-provinsi.xls");

			JxlsHelper.getInstance().processTemplate(reportInputStream, response.getOutputStream(), context);
			response.getOutputStream().close();
			response.flushBuffer();
		} catch (Exception ex) {
			logger.error(ex);
		} finally {
			if (reportInputStream != null) {
				try {
					reportInputStream.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
