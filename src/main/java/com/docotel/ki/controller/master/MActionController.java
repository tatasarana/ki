package com.docotel.ki.controller.master;


import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.MAction;
import com.docotel.ki.model.master.MDocument;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.service.master.StatusService;
import com.docotel.ki.util.FieldValidationUtil;
import com.docotel.ki.repository.master.MDocumentRepository;
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
import java.util.HashSet;
import java.util.List;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class MActionController extends BaseController {

	@Autowired
	private StatusService statusService;

	@Autowired
	private MDocumentRepository mDocumentRepository;

	private static final String DIRECTORY_PAGE = "master/aksi/";

	private static final String PAGE_LIST = DIRECTORY_PAGE + "list-aksi";
	private static final String PAGE_TAMBAH = DIRECTORY_PAGE + "tambah-aksi";
	private static final String PAGE_EDIT = DIRECTORY_PAGE + "edit-aksi";

	private static final String PATH_LIST_AKSI = "/list-aksi";
	private static final String PATH_EDIT_AKSI = "/edit-aksi";
	private static final String PATH_TAMBAH_AKSI = "/tambah-aksi";
	private static final String PATH_REMOVE_AKSI = "/delete-aksi";

	private static final String PATH_AJAX_LIST = "/cari-aksi";


	private static final String REQUEST_MAPPING_AJAX_LIST = PATH_AJAX_LIST + "*";
	private static final String REQUEST_MAPPING_TAMBAH_AKSI = PATH_TAMBAH_AKSI + "*";
	private static final String REQUEST_MAPPING_EDIT = PATH_EDIT_AKSI + "*";
	private static final String REQUEST_MAPPING_REMOVE_AKSI = PATH_REMOVE_AKSI + "*";

	private static final String REDIRECT_TO_LIST = "redirect:" + PATH_AFTER_LOGIN + PATH_LIST_AKSI;

	@ModelAttribute
	public void addModelAttribute(final Model model, final HttpServletRequest request) {
		model.addAttribute("menu", "config");
		model.addAttribute("subMenu", "listAction");


		if (request.getRequestURI().contains(PATH_TAMBAH_AKSI)) {
			if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
				MAction mAction = new MAction();
				mAction.setStatusFlag(true);
				model.addAttribute("form", mAction);
			}
		}
	}
	
	public static <T> void removeDuplicate(List <T> list) {
	    HashSet <T> h = new HashSet<T>(list);
	    list.clear();
	    list.addAll(h);
	  }


	@RequestMapping(path = PATH_LIST_AKSI)
	public String showPageList(@RequestParam(value = "error", required = false) String error, Model model) {
		List<MAction> list = statusService.selectAction();
		
		// remove duplicate action type value
		ArrayList<String> newList = new ArrayList<>();
		for(MAction lists : list) {
			newList.add(lists.getType());
		}
		removeDuplicate(newList);
		
		model.addAttribute("list", list);
		model.addAttribute("listType", newList);
		if (StringUtils.isNotBlank(error)) {
			model.addAttribute("errorMessage", error);
		}
		return PAGE_LIST;
	}

	@PostMapping(REQUEST_MAPPING_REMOVE_AKSI)
	public String doRemovestatus(@ModelAttribute("form") MAction data, final Model model, final HttpServletRequest request, final HttpServletResponse response,
	                             final BindingResult errors) {

		if (!errors.hasErrors()) {
			data.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

			try {
				statusService.removeStatus(data);

				model.asMap().clear();
				return REDIRECT_TO_LIST;
			} catch (DataIntegrityViolationException e) {
				logger.error(e.getMessage(), e);
				writeJsonResponse(response, 500);
			}
		}

		return PAGE_LIST;
	}

	@PostMapping(REQUEST_MAPPING_TAMBAH_AKSI)
	public String doSavestatus(@ModelAttribute("form") MAction data, final Model model, final HttpServletRequest request, final HttpServletResponse response,
	                           final BindingResult errors) {

		if (FieldValidationUtil.required(errors, "code", data.getCode(), "kode aksi")) {
			MAction existingCode = statusService.selectOneActionByCode(data.getCode());
			if (existingCode != null) {
				errors.rejectValue("code", "error.code.exists", "Kode aksi sudah ada.");
			}
		}
		FieldValidationUtil.maxLength(errors, "code", data.getCode(), "Code aksi", 10);

		if (FieldValidationUtil.required(errors, "name", data.getName(), "nama aksi")) {
			MAction existingName = statusService.selectOneActionByName(data.getName());
			if (existingName != null) {
				errors.rejectValue("name", "error.name.exists", "Nama aksi sudah ada.");
			}
		}
		FieldValidationUtil.maxLength(errors, "name", data.getCode(), "Nama aksi", 100);

		List<KeyValue> searchCriteria = new ArrayList<>();

		if(!data.getType().equalsIgnoreCase("Download")) { data.setDocument(null); }
		else {
			if (FieldValidationUtil.required(errors, "document.id", data.getDocument().getId(), "file dokumen")) {
				searchCriteria.add(new KeyValue("id", data.getDocument().getId(), true));
				GenericSearchWrapper<MDocument> existingDocumentList = statusService.searchGeneralDocument(searchCriteria,null,null,null,null);
				if (existingDocumentList.getCount() == 0) {
					errors.rejectValue("document.id", "error.code.exists", "Dokumen file tidak ada.");
					data.setDocument(null);
				}
			}
		}

		if(!data.getType().equalsIgnoreCase("Otomatis")) { data.setDuration(null); }
		else { FieldValidationUtil.required(errors, "duration", data.getDuration(), "durasi tunggu"); }

		if (!errors.hasErrors()) {
			data.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
			data.setStatusFlag(true);
			try {
				statusService.saveAction(data);
				model.asMap().clear();
				return REDIRECT_TO_LIST;
			} catch (DataIntegrityViolationException e) {
				logger.error(e.getMessage(), e);
				model.addAttribute("errorMessage", "Gagal menambahkan Aksi");
				writeJsonResponse(response, 500);
			}
		}

		return showPageTambah(model);
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
						if(searchBy.equalsIgnoreCase("namadokumen") && !value.isEmpty()) {
							searchCriteria.add(new KeyValue("document.name", value, false));
						} else if (StringUtils.isNotBlank(value)) {
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
							orderBy = "code";
							break;
						case "2":
							orderBy = "name";
							break;
						case "3":
							orderBy = "type";
							break;
						case "4":
							orderBy = "statusFlag";
							break;
						case "5":
							orderBy = "createdBy.username";
							break;
						case "6":
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

			GenericSearchWrapper<MAction> searchResult = statusService.searchGeneralAction(searchCriteria, orderBy, orderType, offset, limit);
			if (searchResult.getCount() > 0) {
				dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
				dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

				int no = offset;
				for (MAction result : searchResult.getList()) {
					Date date = new Date(result.getCreatedDate().getTime());
					String pDate = new SimpleDateFormat("dd-MM-yyyy").format(date);
					
					// For user role access button menu
                    MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    String button = "";
                    if(mUser.hasAccessMenu("T-UAK")) {
                    	button = "<div class=\"btn-actions\">"
                                + "<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT_AKSI + "?no=" + result.getId()) + "\">Ubah</a>&nbsp&nbsp"
                                + "</div>";
                    }
                    
					no++;
					data.add(new String[]{
							"" + no,
							result.getCode(),
							result.getName(),
							result.getType(),
							result.isStatusFlag()? "Aktif":"Tidak Aktif",
							result.getCreatedBy().getUsername(),
							pDate,
							button
							/*"<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT_AKSI + "?no=" + result.getId()) + "\">Ubah</a>&nbsp&nbsp"*/
					});
				}
			}

			dataTablesSearchResult.setData(data);

			writeJsonResponse(response, dataTablesSearchResult);
		} else {
			response.setStatus(HttpServletResponse.SC_FOUND);
		}
	}

	@GetMapping(REQUEST_MAPPING_TAMBAH_AKSI)
	public String showPageTambah(final Model model) {
		List<MDocument> documents = mDocumentRepository.findMDocumentByStatusFlag(true);
		model.addAttribute("documents", documents);
		return PAGE_TAMBAH;
	}


	@GetMapping(REQUEST_MAPPING_EDIT)
	public String showPageEdit(Model model, @RequestParam(value = "no", required = true) String no) {

		MAction maksi = statusService.selectOneAction(no);
		if (maksi != null) {
			model.addAttribute("form", maksi);
			List<MDocument> documents = mDocumentRepository.findMDocumentByStatusFlag(true);
			model.addAttribute("documents", documents);
			return PAGE_EDIT;
		}
		return REDIRECT_TO_LIST + "?error=Data Aksi tidak ditemukan";
	}

	@PostMapping(REQUEST_MAPPING_EDIT)
	public String doProsesEdit(@ModelAttribute("form") MAction data, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
		MAction statusExisting = statusService.selectOneAction(data.getId());
		data.setCode(statusExisting.getCode());

		if (!errors.hasErrors()) {
			try {
				data.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
				statusService.saveAction(data);

				model.asMap().clear();
				return REDIRECT_TO_LIST;
			} catch (DataIntegrityViolationException e) {
				logger.error(e.getMessage(), e);
				model.addAttribute("errorMessage", "Gagal Mengubah Aksi");
			}
		}

		return REQUEST_MAPPING_EDIT + "?no=" + data.getId();
	}


}
