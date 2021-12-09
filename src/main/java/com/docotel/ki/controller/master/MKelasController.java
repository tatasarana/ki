package com.docotel.ki.controller.master;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.MClass;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.service.master.ClassService;
import com.docotel.ki.util.FieldValidationUtil;
import com.docotel.ki.enumeration.LanguageTranslatorEnum;
import org.apache.commons.lang3.StringUtils;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
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
import java.io.*;
import java.util.*;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class MKelasController extends BaseController {

	@Autowired
	private ClassService classService;

	private static final String DIRECTORY_PAGE = "master/kelas/";

	private static final String PAGE_LIST = DIRECTORY_PAGE + "list-kelas";
	private static final String PAGE_TAMBAH = DIRECTORY_PAGE + "tambah-kelas";
	private static final String PAGE_EDIT = DIRECTORY_PAGE + "edit-kelas";

	public static final String PATH_LIST = "/list-master-kelas";
	private static final String PATH_EDIT = "/edit-kelas";
	private static final String PATH_TAMBAH = "/tambah-kelas";
	private static final String PATH_TAMBAH_KD = "/tambah-kelas-detail";

	private static final String PATH_EXPORT = "/ekspor-kelas";

	private static final String PATH_AJAX_LIST = "/cari-kelas";

	private static final String REQUEST_MAPPING_AJAX_LIST = PATH_AJAX_LIST + "*";
	private static final String REQUEST_MAPPING_TAMBAH = PATH_TAMBAH + "*";
	private static final String REQUEST_MAPPING_EDIT = PATH_EDIT + "*";

	private static final String REQUEST_EXPORT = PATH_EXPORT + "*";

	public static final String REDIRECT_TO_LIST = "redirect:" + PATH_AFTER_LOGIN + PATH_LIST;

	@ModelAttribute
	public void addModelAttribute(final Model model, final HttpServletRequest request) {

		List<MClass> listKelas = classService.findAllMClass();
		model.addAttribute("listKelas", listKelas);

		model.addAttribute("menu", "maintenance");
		model.addAttribute("subMenu", "listKelas");

		if (request.getRequestURI().contains(PATH_TAMBAH)) {
			if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
				model.addAttribute("form", new MClass());
			}
		}
	}

	@RequestMapping(path = PATH_LIST)
	public String showPageList(@RequestParam(value = "error", required = false) String error, Model model) {
		List<MClass> list = classService.findAllMClass();
		model.addAttribute("list", list);

		if (StringUtils.isNotBlank(error)) {
			model.addAttribute("errorMessage", error);
		}
		model.addAttribute("translatorList", LanguageTranslatorEnum.values());

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
                            if (searchBy.equals("id")) {
                                searchCriteria.add(new KeyValue(searchBy, value, true));
                            } else {
                                searchCriteria.add(new KeyValue(searchBy, value, false));
                            }
                        }
						/*if (searchBy.equalsIgnoreCase("desc")) {
							searchCriteria.add(new KeyValue(searchBy, value, false));
						} else if (StringUtils.isNotBlank(value)) {
							searchCriteria.add(new KeyValue(searchBy, value, true));
						}*/
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
							orderBy = "no";
							break;
						case "2" :
							orderBy = "desc";
							break;
						case "3" :
							orderBy = "edition";
							break;
						case "4" :
							orderBy = "version";
							break;
						case "5" :
							orderBy = "type";
							break;
						case "6" :
							orderBy = "statusFlag";
							break;
						case "7" :
							orderBy = "createdBy.username";
							break;
						case "8" :
							orderBy = "createdDate";
							break;
						default :
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

			GenericSearchWrapper<MClass> searchResult = classService.searchClassHeader(searchCriteria, orderBy, orderType, offset, limit); //searchClassHeader
			if (searchResult.getCount() > 0) {
				dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
				dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

				int no = offset;

				for (MClass result : searchResult.getList()) {
					
					// For user role access button menu
                    MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    String button = "";
                    if(mUser.hasAccessMenu("T-UDKLS")) {
                    	button = "<div class=\"btn-actions\">" +
	                    		 "<a class=\"btn btn-primary btn-xs\" href=\"" + getPathURLAfterLogin(PATH_TAMBAH_KD + "?no=" + result.getId()) + "\">Kelas Detail</a> " +
                                 "<br />" +
                        		 "<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT + "?no=" + result.getId()) + "\">Ubah</a>" +
                        		 "</div>";
                    } else {
                    	button = "<div class=\"btn-actions\">" +
                    			 "<a class=\"btn btn-primary btn-xs\" href=\"" + getPathURLAfterLogin(PATH_TAMBAH_KD + "?no=" + result.getId()) + "\">Kelas Detail</a> " +
                                 "</div>";
                    }
					
					no++;
					data.add(new String[]{
							"" + no,
							"" + result.getNo(),
							result.getDesc(),
							"" + result.getEdition(),
							"" + result.getVersion(),
							result.type(),
							result.status(),
							result.getCreatedBy().getUsername(),
							result.createdDate(),
							button
							/*"<div class=\"btn-actions\">" +
									"<a class=\"btn btn-primary btn-xs\" href=\"" + getPathURLAfterLogin(PATH_TAMBAH_KD + "?no=" + result.getId()) + "\">Kelas Detail</a> " +
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
	public String doProsesTambah(@ModelAttribute("form") MClass form, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
		// validate form
		FieldValidationUtil.required(errors, "no", form.getNo(), "no");
		FieldValidationUtil.required(errors, "desc", form.getDesc(), "desc");
//		FieldValidationUtil.required(errors, "descEn", form.getDescEn(), "descEn");
		FieldValidationUtil.required(errors, "edition", form.getEdition(), "edition");
		FieldValidationUtil.required(errors, "version", form.getVersion(), "version");

		FieldValidationUtil.required(errors, "type", form.getType(), "type");
		// validate form end

		if (!errors.hasErrors()) {

			MClass mClassExisting = classService.findFirstByNo(form.getNo()); 

			if (mClassExisting != null) {
				model.addAttribute("errorMessage", "Nomor Kelas Sudah Ada.");
				return PAGE_TAMBAH;
			}

			form.setStatusFlag(true);
			form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

			try {
				classService.insert(form);

				model.asMap().clear();
				return REDIRECT_TO_LIST;
			} catch (DataIntegrityViolationException e) {
				logger.error(e.getMessage(), e);
				model.addAttribute("errorMessage", "Gagal Menambahkan Kelas");
			}
		}
		return PAGE_TAMBAH;
	}

	@GetMapping(REQUEST_MAPPING_EDIT)
	public String showPageEdit(Model model, @RequestParam(value = "no", required = true) String no) {
		MClass mClass = classService.findOneMClass(no);
		if (mClass != null) {
			model.addAttribute("form", mClass);
			return PAGE_EDIT;
		}
		return REDIRECT_TO_LIST + "?error=Data Kelas tidak ditemukan";
	}

	@PostMapping(REQUEST_MAPPING_EDIT)
	public String doProsesEdit(@ModelAttribute("form") MClass form, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
		MClass existing = classService.findOneMClass(form.getId());
		form.setNo(existing.getNo());

		// validate form
		FieldValidationUtil.required(errors, "no", form.getNo(), "no");
		FieldValidationUtil.required(errors, "desc", form.getDesc(), "desc");
//		FieldValidationUtil.required(errors, "descEn", form.getDescEn(), "descEn");
		FieldValidationUtil.required(errors, "edition", form.getEdition(), "edition");
		FieldValidationUtil.required(errors, "version", form.getVersion(), "version");
		// validate form end

		if (!errors.hasErrors()) {
			form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

			try {
				classService.saveOrUpdate(form);

				model.asMap().clear();
				return REDIRECT_TO_LIST;
			} catch (DataIntegrityViolationException e) {
				logger.error(e.getMessage(), e);
				model.addAttribute("errorMessage", "Gagal menambahkan kelas");
			}
		}
		return PAGE_EDIT + "?no=" + form.getCurrentId();
	}

	@GetMapping(REQUEST_EXPORT)
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
						orderBy = "no";
						break;
					case "2" :
						orderBy = "desc";
						break;
					case "3" :
						orderBy = "edition";
						break;
					case "4" :
						orderBy = "version";
						break;
					case "5" :
						orderBy = "type";
						break;
					case "6" :
						orderBy = "statusFlag";
						break;
					case "7" :
						orderBy = "createdBy.username";
						break;
					case "8" :
						orderBy = "createdDate";
						break;
					default :
						orderBy = "no";
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
			reportInputStream = getClass().getClassLoader().getResourceAsStream("report/list-master-class.xls");

			List<MClass> dataList = new ArrayList<>();

			int totalRecord = (int) classService.countAll(searchCriteria);
			int retrieved = 0;
			while (retrieved < totalRecord) {
				List<MClass> retrievedDataList = classService.selectAll(searchCriteria, orderBy, orderType, retrieved, 1000);
				dataList.addAll(retrievedDataList);
				retrieved += retrievedDataList.size();
			}

			Context context = new Context();
			context.putVar("dataList", dataList);

			response.setContentType("application/vnd.ms-excel");
			response.setHeader("Content-Disposition", "attachment; filename=daftar-master-kelas.xls");

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
