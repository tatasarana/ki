package com.docotel.ki.controller.master;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.MDocType;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.service.master.DocTypeService;
import com.docotel.ki.util.FieldValidationUtil;
import org.apache.commons.lang.StringUtils;
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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class MJenisDokumenController extends BaseController {
    
    @Autowired
    DocTypeService docTypeService;

    private static final String DIRECTORY_PAGE = "master/jenis-dokumen/";

    private static final String PAGE_LIST = DIRECTORY_PAGE + "list-jenis-dokumen";
    private static final String PAGE_TAMBAH = DIRECTORY_PAGE + "tambah-jenis-dokumen";
    private static final String PAGE_EDIT = DIRECTORY_PAGE + "edit-jenis-dokumen";

    private static final String PATH_LIST = "/list-jenis-dokumen";
    private static final String PATH_EDIT = "/edit-jenis-dokumen";

    private static final String PATH_AJAX_LIST = "/cari-jenis-dokumen";
    private static final String PATH_TAMBAH = "/tambah-jenis-dokumen";
    
    private static final String PATH_DETAIL_DOKUMEN = "/detail-dokumen";

    private static final String REQUEST_MAPPING_AJAX_LIST = PATH_AJAX_LIST + "*";
    private static final String REQUEST_MAPPING_TAMBAH = PATH_TAMBAH + "*";
    private static final String REQUEST_MAPPING_EDIT = PATH_EDIT + "*";

    private static final String REDIRECT_TO_LIST = "redirect:" + PATH_AFTER_LOGIN + PATH_LIST;

    private static final String[][] MANDATORY_TYPE_LIST = new String[][]{
            new String[] {"GENERAL", "TAB GENERAL"},
            new String[] {"OWNER", "TAB PEMOHON"},
            new String[] {"REPRS", "TAB KUASA"},
            new String[] {"PRIOR", "TAB PRIORITAS"},
            new String[] {"BRAND", "TAB MEREK"},
            new String[] {"CLASS", "TAB KELAS"},
            new String[] {"POSTRECEPT", "TAB PASCA GENERAL"},
            new String[] {"POSTOWNER", "TAB PASCA PEMOHON"},
            new String[] {"POSTREPRS", "TAB PASCA KUASA"}
    };
    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "master");
        model.addAttribute("subMenu", "listJenisDokumen");

        if (request.getRequestURI().contains(PATH_TAMBAH)) {
            if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
                model.addAttribute("form", new MDocType());
            }
        }
    }

    @RequestMapping(path = PATH_LIST)
    public String showPageList(@RequestParam(value = "error", required = false) String error, Model model) {
        List<MDocType> list = docTypeService.findAll();
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

            GenericSearchWrapper<MDocType> searchResult = docTypeService.searchGeneral(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (MDocType result : searchResult.getList()) {
                    Date date = new Date(result.getCreatedDate().getTime());
                    String pDate = new SimpleDateFormat("dd-MM-yyyy").format(date);
                    String stts = ((result.isStatusFlag() == true) ? "Aktif" : "Tidak Aktif");
                    
                    // For user role access button menu
                    MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    String button = "";
                    if(mUser.hasAccessMenu("T-UJD")) {
                    	button = "<div class=\"btn-actions\">" +
                                "<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT + "?no=" + result.getId()) + "\">Ubah</a>" +
                                "</div>";
                    } if (mUser.hasAccessMenu("T-DJD")) {
                    		button = "<div class=\"btn-actions\">" +
                    				 "<a class=\"btn btn-primary btn-xs\" href=\"" + getPathURLAfterLogin(PATH_DETAIL_DOKUMEN + "?no=" + result.getId()) + "\">Detail</a>" 
                    				 +"</div>";
                    } if (mUser.hasAccessMenu("T-UJD") && mUser.hasAccessMenu("T-DJD")) {
                    		button = "<div class=\"btn-actions\">" +
                                    "<a class=\"btn btn-primary btn-xs\" href=\"" + getPathURLAfterLogin(PATH_DETAIL_DOKUMEN + "?no=" + result.getId()) + "\">Detail</a>" +
                                    "<br />" +
                                    "<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT + "?no=" + result.getId()) + "\">Ubah</a>" +
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
                                    "<a class=\"btn btn-primary btn-xs\" href=\"" + getPathURLAfterLogin(PATH_DETAIL_DOKUMEN + "?no=" + result.getId()) + "\">Detail</a>" +
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
        model.addAttribute("listMandatoryType", MANDATORY_TYPE_LIST);
        return PAGE_TAMBAH;
    }

    @PostMapping(REQUEST_MAPPING_TAMBAH)
    public String doProsesTambah(@ModelAttribute("form") MDocType form, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
        // validate form
        FieldValidationUtil.required(errors, "code", form.getCode(), "code");
        FieldValidationUtil.required(errors, "name", form.getName(), "name");
        // validate form end

        if (!errors.hasErrors()) {
            form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            form.setCreatedDate(new Timestamp(System.currentTimeMillis()));
            form.setStatusFlag(true);
            try {
                docTypeService.insert(form);

                model.asMap().clear();
                return REDIRECT_TO_LIST;
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                model.addAttribute("errorMessage", "Gagal menambahkan Jenis Dokumen - Kode " + form.getCode() + " Sudah Ada");
            }
        }

        model.addAttribute("listMandatoryType", MANDATORY_TYPE_LIST);
        return PAGE_TAMBAH;
    }

    @GetMapping(REQUEST_MAPPING_EDIT)
    public String showPageEdit(Model model, @RequestParam(value = "no", required = true) String no) {
        MDocType MDocType = docTypeService.findOne(no);
        if (MDocType!= null){
            model.addAttribute("form", MDocType);
            model.addAttribute("listMandatoryType", MANDATORY_TYPE_LIST);
            return PAGE_EDIT;
        }
        return REDIRECT_TO_LIST+ "?error=Data Jenis Dokumen tidak ditemukan";
    }

    @PostMapping(REQUEST_MAPPING_EDIT)
    public String doProsesEdit(@ModelAttribute("form") MDocType form, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
        // validate form
        FieldValidationUtil.required(errors, "id", form.getCurrentId(), "id");
        FieldValidationUtil.required(errors, "code", form.getCode(), "code");
        FieldValidationUtil.required(errors, "name", form.getName(), "name");
        // validate form end

        if (!errors.hasErrors()) {
            form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            form.setCreatedDate(new Timestamp(System.currentTimeMillis()));
            try {
                docTypeService.saveOrUpdate(form);

                model.asMap().clear();
                return REDIRECT_TO_LIST;
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                model.addAttribute("errorMessage", "Gagal Mengubah Jenis Dokumen - Kode " + form.getCode() + " Sudah Ada");
            }
        }

        return showPageEdit(model, form.getCurrentId());
    }
}
