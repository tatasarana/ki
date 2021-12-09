package com.docotel.ki.controller.master;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.MFileType;
import com.docotel.ki.model.master.MLookup;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.master.MWorkflow;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.service.master.FileService;
import com.docotel.ki.service.master.LookupService;
import com.docotel.ki.service.master.WorkflowService;
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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class MTipePermohonanController extends BaseController {
	@Autowired
    LookupService lookupService;

	@Autowired
	private FileService fileService;

	@Autowired
	private WorkflowService workflowService;

	private static final String DIRECTORY_PAGE = "master/tipe-permohonan/";

	/*-----MFileType Section-----*/
	private static final String PAGE_LIST = DIRECTORY_PAGE + "list-tipe-permohonan";
	private static final String PAGE_TAMBAH = DIRECTORY_PAGE + "tipe-permohonan-tambah";
	private static final String PAGE_EDIT = DIRECTORY_PAGE + "tipe-permohonan-edit";

	private static final String PATH_LIST = "/list-tipe-permohonan";
	private static final String PATH_EDIT = "/edit-tipe";
	private static final String PATH_TAMBAH = "/tambah-tipe";
	private static final String PATH_AJAX_LIST = "/cari-tipe";

	private static final String REQUEST_MAPPING_AJAX_LIST = PATH_AJAX_LIST + "*";
	private static final String REQUEST_MAPPING_TAMBAH = PATH_TAMBAH + "*";
	private static final String REQUEST_MAPPING_EDIT = PATH_EDIT + "*";

	private static final String REDIRECT_TO_LIST = "redirect:" + PATH_AFTER_LOGIN + PATH_LIST;
	/*-----End of MFileType Section-----*/

	@ModelAttribute
	public void addModelAttribute(final Model model, final HttpServletRequest request) {
		model.addAttribute("menu", "master");
		model.addAttribute("subMenu", "listTipePermohonan");
        
		if (request.getRequestURI().contains(PATH_TAMBAH) || request.getRequestURI().contains(PATH_EDIT)) {
			String workflowId = null;

			if (request.getRequestURI().contains(PATH_TAMBAH)) {
				if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
					MFileType mFileType = new MFileType();
					mFileType.setmWorkflow(new MWorkflow());
					model.addAttribute("form", mFileType);
				}
			}

			if (request.getRequestURI().contains(PATH_EDIT)) {
				MFileType form = fileService.getFileTypeById(request.getParameter("no"));
				if (form != null) {
					workflowId = form.getmWorkflow() == null ? null : form.getmWorkflow().getCurrentId();
				}
				if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
					model.addAttribute("form", form);
				} else {
					model.addAttribute("existing", form);
				}
			}

			model.addAttribute("workflowList", workflowService.selectActivesOrUsed(workflowId));
			List<MLookup> listMenu = (List<MLookup>) lookupService.selectAllbyGroup("FileTypeMenuLookup");
			model.addAttribute("listMenu", listMenu);
		}
	}

	@RequestMapping(path = PATH_LIST)
	public String showPageList(@RequestParam(value = "error", required = false) String error, Model model) {
		List<MFileType> list = fileService.findAll();
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
							orderBy = "desc";
							break;
						case "3" :
							orderBy = "menu";
							break;
						case "4" :
							orderBy = "statusPaid";
							break;
						case "5" :
							orderBy = "statusFlag";
							break;
						case "6" :
							orderBy = "createdBy.username";
							break;
						case "7" :
							orderBy = "createdDate";
							break;
						default:
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

			GenericSearchWrapper<MFileType> searchResult = fileService.searchFileType(searchCriteria, orderBy, orderType, offset, limit);
			if (searchResult.getCount() > 0) {
				dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
				dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

				int no = offset;
				String menu = "";
				for (MFileType result : searchResult.getList()) {
					Date date = new Date(result.getCreatedDate().getTime());
					String pDate = new SimpleDateFormat("dd-MM-yyyy").format(date);
					String stts = ((result.isStatusFlag() == true) ? "Aktif" : "Tidak Aktif");
					String stpaid = ((result.isStatusPaid() == true) ? "Ya" : "Tidak");

					if (result.getMenu() != null) {
						menu = result.getMenu();
					}
					
					// For user role access button menu
                    MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    String button = "";
                    if(mUser.hasAccessMenu("T-UTP")) {
                    	button = "<div class=\"btn-actions\">"
                                + "<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT + "?no=" + result.getId()) + "\">Ubah</a>"
                                + "</div>";
                    }
                    
					no++;
					data.add(new String[]{
							"" + no,
							result.getCode(),
							result.getDesc(),
							menu,
							stpaid,
							stts,
							result.getCreatedBy().getUsername(),
							pDate,
							button
							/*"<div class=\"btn-actions\">" +
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
	public String showPageTambah(Model model) { return PAGE_TAMBAH;	}

	@PostMapping(REQUEST_MAPPING_TAMBAH)
	public String doProsesTambah(@ModelAttribute("form") MFileType form, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
		// validate form
		FieldValidationUtil.required(errors, "code", form.getCode(), "code");
		FieldValidationUtil.required(errors, "desc", form.getDesc(), "desc");
		FieldValidationUtil.required(errors, "menu", form.getMenu(), "menu");
		FieldValidationUtil.required(errors, "statusPaid", form.isStatusPaid(), "statusPaid");
		FieldValidationUtil.required(errors, "mWorkflow.id", form.getmWorkflow() == null ? null : form.getmWorkflow().getId(), "workflow");
		// validate form end

		if (!errors.hasErrors()) {
            form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
			form.setStatusFlag(true);

			try {				
				fileService.insertMFileType(form);

				model.asMap().clear(); 
				return REDIRECT_TO_LIST;
			} catch (DataIntegrityViolationException e) {
				logger.error(e.getMessage(), e);
				model.addAttribute("errorMessage", "Gagal menambahkan Tipe Permohonan - Kode " + form.getCode());
			}
		}

		return PAGE_TAMBAH;
	}

	@GetMapping(REQUEST_MAPPING_EDIT)
	public String showPageEdit(Model model, @RequestParam(value = "no", required = true) String no) {
		MFileType mFileType = (MFileType) model.asMap().get("form");
		if (mFileType != null) {
			model.addAttribute("form", mFileType);
			return PAGE_EDIT;
		}
		return REDIRECT_TO_LIST + "?error=Data Tipe Permohonan tidak ditemukan";
	}

	@PostMapping(REQUEST_MAPPING_EDIT)
	public String doProsesEdit(@ModelAttribute("existing") MFileType form, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
		//MFileType existing = (MFileType) model.asMap().get("existing");
		List<MWorkflow> workflowList = (List<MWorkflow>) model.asMap().get("workflowList");
		List<String> workflowIdList = new ArrayList<>();
		if (workflowList != null) {
			for (MWorkflow mWorkflow : workflowList) {
				workflowIdList.add(mWorkflow.getId());
			}
		}

		//form.setCode(existing.getCode());

		// validate form
		FieldValidationUtil.required(errors, "desc", form.getDesc(), "desc");
		FieldValidationUtil.required(errors, "menu", form.getMenu(), "menu");
		FieldValidationUtil.required(errors, "statusFlag", form.isStatusFlag(), "statusFlag");
		FieldValidationUtil.required(errors, "statusPaid", form.isStatusPaid(), "statusPaid");
		if (FieldValidationUtil.required(errors, "mWorkflow.id", form.getmWorkflow() == null ? null : form.getmWorkflow().getId(), "workflow")) {
			FieldValidationUtil.listValue(errors, "mWorkflow.id", form.getmWorkflow().getId(), "workflow", workflowIdList);
		}
		// validate form end

		if (!errors.hasErrors()) {
			form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
			form.setCreatedDate(new Timestamp(System.currentTimeMillis()));
			try {
				fileService.saveOrUpdateFileType(form);

				model.asMap().clear();
				return REDIRECT_TO_LIST;
			} catch (DataIntegrityViolationException e) {
				logger.error(e.getMessage(), e);
				model.addAttribute("errorMessage", "Gagal Mengubah Tipe permohonan - Kode " + form.getCode() + " Sudah Ada");
			}
		}
		
		return showPageEdit(model, form.getCurrentId());
		//return PAGE_EDIT + "?no=" + form.getCurrentId();
	}

}
