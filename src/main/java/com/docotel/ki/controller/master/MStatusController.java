package com.docotel.ki.controller.master;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.MStatus;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.service.master.StatusService;
import com.docotel.ki.util.FieldValidationUtil;
import com.docotel.ki.model.master.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class MStatusController extends BaseController {

	@Autowired
	private StatusService statusService;

	private static final String DIRECTORY_PAGE = "master/status/";

	private static final String PAGE_LIST = DIRECTORY_PAGE + "list-status";
	private static final String PAGE_TAMBAH = DIRECTORY_PAGE + "tambah-status";
	private static final String PAGE_EDIT = DIRECTORY_PAGE + "edit-status";

	private static final String PATH_LIST_STATUS = "/list-status";
	private static final String PATH_EDIT_STATUS = "/edit-status";
	private static final String PATH_TAMBAH_STATUS = "/tambah-status";
	private static final String PATH_REMOVE_STATUS = "/delete-status";

	private static final String PATH_AJAX_LIST = "/cari-status";


	private static final String REQUEST_MAPPING_AJAX_LIST = PATH_AJAX_LIST + "*";
	private static final String REQUEST_MAPPING_TAMBAH_STATUS = PATH_TAMBAH_STATUS + "*";
	private static final String REQUEST_MAPPING_EDIT = PATH_EDIT_STATUS + "*";
	private static final String REQUEST_MAPPING_REMOVE_STATUS = PATH_REMOVE_STATUS + "*";

	private static final String REDIRECT_TO_LIST = "redirect:" + PATH_AFTER_LOGIN + PATH_LIST_STATUS;


	@ModelAttribute
	public void addModelAttribute(final Model model, final HttpServletRequest request) {
		model.addAttribute("menu", "config");
		model.addAttribute("subMenu", "listStatus");


		if (request.getRequestURI().contains(PATH_TAMBAH_STATUS)) {
			if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
				MStatus mStatus = new MStatus();
				mStatus.setStatusFlag(true);
				model.addAttribute("form", mStatus);
			}
		}
	}


	@RequestMapping(path = PATH_LIST_STATUS)
	public String showPageList(@RequestParam(value = "error", required = false) String error, Model model) {
		List<MStatus> list = statusService.selectStatus();
		model.addAttribute("list", list);
		if (StringUtils.isNotBlank(error)) {
			model.addAttribute("errorMessage", error);
		}
		return PAGE_LIST;
	}

	@PostMapping(REQUEST_MAPPING_REMOVE_STATUS)
	public String doRemovestatus(@ModelAttribute("form") MStatus data, final Model model, final HttpServletRequest request, final HttpServletResponse response,
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

	@PostMapping(REQUEST_MAPPING_TAMBAH_STATUS)
	public String doSavestatus(@ModelAttribute("form") MStatus data, final Model model, final HttpServletRequest request, final HttpServletResponse response,
	                           final BindingResult errors) {

        FieldValidationUtil.required(errors, "code", data.getCode(), "Kode Status");
        FieldValidationUtil.required(errors, "name", data.getName(), "Nama Status");
        
        MStatus mStatus = statusService.selectOneStatus("code", data.getCode());

		if (!errors.hasErrors()) {
			data.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
			data.setStatusFlag(true);
			data.setLockedFlag(false);

			try {
				if(mStatus != null) {
					model.addAttribute("errorMessage", "Gagal menambahkan Status Workflow - Kode " + data.getCode() + " Sudah Ada");
				} else {
					statusService.saveStatus(data);
					model.asMap().clear();
					return REDIRECT_TO_LIST;
				}
				
			} catch (DataIntegrityViolationException e) {
				logger.error(e.getMessage(), e);
				model.addAttribute("errorMessage", "Gagal menambahkan Status Workflow - Kode " + data.getCode() + " Sudah Ada");
				model.addAttribute("errorMessage", "Gagal menambahkan Status");
				writeJsonResponse(response, 500);
			}
		}

		return PAGE_TAMBAH;
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
							orderBy = "code";
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

			GenericSearchWrapper<MStatus> searchResult = statusService.searchGeneralStatus(searchCriteria, orderBy, orderType, offset, limit);
			if (searchResult.getCount() > 0) {
				dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
				dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

				int no = offset;
				for (MStatus result : searchResult.getList()) {
					Date date = new Date(result.getCreatedDate().getTime());
					String pDate = new SimpleDateFormat("dd-MM-yyyy").format(date);
					
					// For user role access button menu
                    MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    String button = "";
                    if(mUser.hasAccessMenu("T-UST")) {
                    	button = "<div class=\"btn-actions\">"
                                + "<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT_STATUS + "?no=" + result.getId()) + "\">Ubah</a>&nbsp&nbsp"
                                + "</div>";
                    }
					
					no++;
					data.add(new String[]{
							"" + no,
							result.getCode(),
							result.getName(),
//							result.isStaticFlag()==true?"Ya":"Bukan",
							result.isStatusFlag()==true?"Aktif": "Tidak Aktif",
							result.getCreatedBy().getUsername(),
							pDate,
							button
							/*"<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT_STATUS + "?no=" + result.getId()) + "\">Ubah</a>&nbsp&nbsp"*/

					});
				}
			}

			dataTablesSearchResult.setData(data);

			writeJsonResponse(response, dataTablesSearchResult);
		} else {
			response.setStatus(HttpServletResponse.SC_FOUND);
		}
	}

	@GetMapping(REQUEST_MAPPING_TAMBAH_STATUS)
	public String showPageTambah(final Model model) {

		return PAGE_TAMBAH;
	}


	@GetMapping(REQUEST_MAPPING_EDIT)
	public String showPageEdit(Model model, @RequestParam(value = "no", required = true) String no) {

		MStatus mstatus = statusService.selectOneStatus("id", no);
		if (mstatus != null) {
			model.addAttribute("form", mstatus);
			return PAGE_EDIT;
		}
		return REDIRECT_TO_LIST + "?error=Data Status tidak ditemukan";
	}

	@PostMapping(REQUEST_MAPPING_EDIT)
	public String doProsesEdit(@ModelAttribute("form") MStatus data, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
		MStatus statusExisting = statusService.selectOneStatus("id", data.getId());
		data.setCode(statusExisting.getCode());

		if (!errors.hasErrors()) {
			try {
				data.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
				//data.setStaticFlag(true);
				//data.setStatusFlag(true);
				data.isLockedFlag();
				statusService.saveStatus(data);

				model.asMap().clear();
				return REDIRECT_TO_LIST;
			} catch (DataIntegrityViolationException e) {
				logger.error(e.getMessage(), e);
				model.addAttribute("errorMessage", "Gagal Mengubah Status");
			}
		}

		return REQUEST_MAPPING_EDIT + "?no=" + data.getId();
	}

}
