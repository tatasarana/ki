package com.docotel.ki.controller.master;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.MCountry;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.master.MWorkflow;
import com.docotel.ki.model.transaction.TxTmPrior;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.service.master.MenuService;
import com.docotel.ki.service.master.WorkflowService;
import com.docotel.ki.util.FieldValidationUtil;
import com.docotel.ki.model.master.*;
import com.docotel.ki.pojo.*;
import com.docotel.ki.service.master.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class MWorkflowController extends BaseController {

	@Autowired
	private WorkflowService workflowService;
	@Autowired
	private MenuService menuService;

	private static final String DIRECTORY_PAGE = "master/workflow/";

	private static final String PAGE_LIST = DIRECTORY_PAGE + "list-workflow";
	private static final String PAGE_TAMBAH = DIRECTORY_PAGE + "tambah-workflow";
	private static final String PAGE_EDIT = DIRECTORY_PAGE + "edit-workflow";


	private static final String PAGE_EDIT_WORKFLOW_DETAIL = DIRECTORY_PAGE + "workflow-detail";
	private static final String PAGE_DETAIL_WORKFLOW = DIRECTORY_PAGE + "workflow-detail";
	private static final String PAGE_TAMBAH_DETAIL = DIRECTORY_PAGE + "tambah-workflow-detail";

	private static final String PAGE_LIST_DETAIL = "/list-workflow-detail";

	private static final String PATH_LIST_WORK_FLOW = "/list-workflow";
	private static final String PATH_EDIT_WORK_FLOW = "/edit-workflow";
	private static final String PATH_TAMBAH_WORK_FLOW = "/tambah-workflow";
	private static final String PATH_REMOVE_WORK_FLOW = "/delete-workflow";

	private static final String PATH_AJAX_LIST = "/cari-workflow";


	private static final String REQUEST_MAPPING_AJAX_LIST = PATH_AJAX_LIST + "*";
	private static final String REQUEST_MAPPING_TAMBAH_WORK_FLOW = PATH_TAMBAH_WORK_FLOW + "*";
	private static final String REQUEST_MAPPING_EDIT = PATH_EDIT_WORK_FLOW + "*";
	private static final String REQUEST_MAPPING_REMOVE_WORK_FLOW = PATH_REMOVE_WORK_FLOW + "*";
	private static final String REQUEST_MAPPING_LIST_WORKFLOW_DETAIL = PAGE_LIST_DETAIL + "*";

	private static final String REQUEST_MAPPING_TAMBAH_WORK_FLOW_DETAIL = PAGE_TAMBAH_DETAIL + "*";


	private static final String REDIRECT_TO_LIST = "redirect:" + PATH_AFTER_LOGIN + PATH_LIST_WORK_FLOW;
	private static final String REDIRECT_TO_DETAIL = "redirect:" + PATH_AFTER_LOGIN + PAGE_LIST_DETAIL;


	@ModelAttribute
	public void addModelAttribute(final Model model, final HttpServletRequest request) {
		model.addAttribute("menu", "config");
		model.addAttribute("subMenu", "listWorkflow");

		if (request.getRequestURI().contains(PATH_TAMBAH_WORK_FLOW) || request.getRequestURI().contains(PAGE_LIST_DETAIL)) {
			if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
				model.addAttribute("form", new MWorkflow());
			}
		} else {

		}
	}

	@RequestMapping(path = PATH_LIST_WORK_FLOW)
	public String showPageList(@RequestParam(value = "error", required = false) String error, Model model) {
		List<MWorkflow> list = workflowService.findAllWorkflow();
		model.addAttribute("list", list);
		if (StringUtils.isNotBlank(error)) {
			model.addAttribute("errorMessage", error);
		}
		return PAGE_LIST;
	}

	@PostMapping(REQUEST_MAPPING_REMOVE_WORK_FLOW)
	public String doRemoveworkflow(@ModelAttribute("form") MWorkflow data, final Model model, final HttpServletRequest request, final HttpServletResponse response, final BindingResult errors) {
		if (!errors.hasErrors()) {
			data.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

			try {
				workflowService.removeWorkflow(data);

				model.asMap().clear();
				return REDIRECT_TO_LIST;
			} catch (DataIntegrityViolationException e) {
				logger.error(e.getMessage(), e);
				writeJsonResponse(response, 500);
			}
		}

		return PAGE_LIST;
	}

	@PostMapping(REQUEST_MAPPING_TAMBAH_WORK_FLOW)
	public String doSaveWorkflow(@ModelAttribute("form") MWorkflow data, final Model model, final HttpServletRequest request, final HttpServletResponse response, final BindingResult errors) {
		MWorkflow existingCode = workflowService.selectOneWorkflowByCode(data.getCode());
		if (FieldValidationUtil.required(errors, "code", data.getCode(), "Code workflow")) {
			if (existingCode != null) {
				errors.rejectValue("code", "error.code.exists", "Kode workflow sudah ada.");
			}
		}

		if (FieldValidationUtil.required(errors, "name", data.getCode(), "Name workflow")) {
			MWorkflow existingName = workflowService.selectOneWorkflowByName(data.getName());
			if (existingName != null) {
				errors.rejectValue("name", "error.name.exists", "Name workflow sudah ada.");
			}
		}

		if (!errors.hasErrors()) {
			data.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
			data.setStatusFlag(true);

			try {
				workflowService.insertWorkflow(data);

				model.asMap().clear();
				return REDIRECT_TO_LIST;
			} catch (DataIntegrityViolationException e) {
				model.addAttribute("errorMessage", "Gagal menambahkan Workflow");
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

			//System.out.println("SEARCHARR: " + searchByArr.length);
			//System.out.println(searchByArr[0]);

			if (searchByArr != null) {
				searchCriteria = new ArrayList<>();
				for (int i = 0; i < searchByArr.length; i++) {
					String searchBy = searchByArr[i];
					String value = null;
					try {
						value = keywordArr[i];
					} catch (ArrayIndexOutOfBoundsException e) {
						//System.out.println(e);
					}
					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (StringUtils.isNotBlank(value)) {
							searchCriteria.add(new KeyValue(searchBy, value, false));
						}
					}
				}
				//System.out.println("KRITERIA" + searchCriteria.size());
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
							orderBy = "statusFlag";
							break;
						case "4" :
							orderBy = "createdBy.username";
							break;
						case "5" :
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

			GenericSearchWrapper<MWorkflow> searchResult = workflowService.searchGeneralWorkflow(searchCriteria, orderBy, orderType, offset, limit);
			if (searchResult.getCount() > 0) {
				dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
				dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

				int no = offset;
				for (MWorkflow result : searchResult.getList()) {
					Date date = new Date(result.getCreatedDate().getTime());
					String pDate = new SimpleDateFormat("dd-MM-yyyy").format(date);
					String stts = ((result.isStatusFlag() == true) ? "Aktif" : "Tidak Aktif");
					
					// For user role access button menu
                    MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    String button = "";
                    if(mUser.hasAccessMenu("T-UWK")) {
                    	button = "<div class=\"btn-actions\">"
                                + "<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT_WORK_FLOW + "?no=" + result.getId()) + "\">Ubah</a>"
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
							/*"<div class=\"btn-actions\">" +
									"<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT_WORK_FLOW + "?no=" + result.getId()) + "\">Ubah</a>" +*/
//									"<br/>" +
//									"<a class=\"btn btn-alert btn-xs\" href=\"" + getPathURLAfterLogin(PAGE_LIST_DETAIL + "?no=" + result.getId()) + "\">Hapus</a>" +
							/*"</div>"*/
					});
				}
			}

			dataTablesSearchResult.setData(data);

			writeJsonResponse(response, dataTablesSearchResult);
		} else {
			response.setStatus(HttpServletResponse.SC_FOUND);
		}
	}

	@GetMapping(REQUEST_MAPPING_TAMBAH_WORK_FLOW)
	public String showPageTambah(final Model model) {

		return PAGE_TAMBAH;
	}

	/* --------------------------------------- DATA TABLE Workflow Detail --------------------------------------- */
	@RequestMapping(value = REQUEST_MAPPING_LIST_WORKFLOW_DETAIL, method = {RequestMethod.GET})
	public String doSearchDataTablesListWorkflowDetail(final Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		MWorkflow MWorkflow = workflowService.selectMWorkflowById(request.getParameter("no"));
		//List<MStatus> mStatusList = workflowService.findAllStep();
		List<MWorkflow> list = workflowService.findAllWorkflow();


		if (MWorkflow != null) {
			model.addAttribute("form", MWorkflow);
			// model.addAttribute("listStep", mStatusList);

		}
		//return REDIRECT_TO_DETAIL;
		return PAGE_DETAIL_WORKFLOW;
	}


    /*@GetMapping(REQUEST_MAPPING_WORKFLOW_DETAIL)
    public String showPageWorkflowDetail(Model model, @RequestParam(value = "no", required = true) String no) {
    	MWorkflow MWorkflow = workflowService.selectMWorkflowById(no);
        List<MStatus> mStepList = workflowService.findAllStep();
        
        List<MWorkflow> list = workflowService.findAllWorkflow();
        model.addAttribute("list", list);
        
    	if (MWorkflow != null) {
            model.addAttribute("form", MWorkflow);            
            model.addAttribute("listStep", mStepList);
            
            return PAGE_EDIT_WORKFLOW_DETAIL;
        }
        return REDIRECT_TO_LIST;
    }*/

	@RequestMapping(value = REQUEST_MAPPING_TAMBAH_WORK_FLOW_DETAIL, method = {RequestMethod.GET})
	public void doInsertTable(Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		String negaraId = request.getParameter("negaraId");
		String no = request.getParameter("no");
		String appNo = request.getParameter("appNo");
		List<KeyValue> searchCriteria = null;
		Timestamp tmstm = new Timestamp(System.currentTimeMillis());

		MCountry mCountry = new MCountry();

		mCountry.setId(negaraId);
		TxTmPrior txTmPrior = new TxTmPrior();

		txTmPrior.setId(txTmPrior.getId());
		txTmPrior.setNo(no);
		txTmPrior.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		txTmPrior.setCreatedDate(tmstm);
		txTmPrior.setPriorDate(tmstm);
		txTmPrior.setmCountry(mCountry);
		//txTmPrior.setTxTmGeneral(trademarkService.selectOne("applicationNo", appNo));

		//priorService.doInsert(txTmPrior);

	}
    
    
     
    /*@PostMapping(value=REQUEST_MAPPING_WORKFLOW_DETAIL)    
    public void doSaveWorkflowDetail(@ModelAttribute("form") MWorkflow form, 
    		final Model model, final HttpServletRequest request, final HttpServletResponse response, final BindingResult errors) {
    	
    	MWorkflow existing = workflowService.selectMWorkflowById(form.getId());
    	if(existing!=null) {
    		form.setCode(existing.getCode());
    		form.setName(existing.getName());
    		form.setmLaw(existing.getmLaw());
    	}
    	
    	try {
    		logger.debug("REQUEST_MAPPING_WORKFLOW_DETAIL");
            Timestamp tmstm = new Timestamp(System.currentTimeMillis());
            form.setCreatedDate(tmstm);
            form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());   
            
            for (MWorkflowDetail mWorkflowDetail : form.getmWorkflowDetails() ) {
            	mWorkflowDetail.setmWorkflow(form);        
            }
    	} catch (Exception ex) {
    		System.out.print(ex);
    	}
    }*/
    
    
    /*, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, 
    produces = {MediaType.APPLICATION_ATOM_XML_VALUE, MediaType.APPLICATION_JSON_VALUE}*/

//    @RequestMapping(value="/layanan/workflow-detail-save", method = RequestMethod.POST)   
//    @ResponseBody
//    public void doSaveWorkflowDetailSave(@RequestBody DataFormWorkflow form, final Model model,   		
//    		final HttpServletRequest request, final HttpServletResponse response, final BindingResult errors) {
//    	
//    	try {
//    		logger.debug(form);
//            Timestamp tmstm = new Timestamp(System.currentTimeMillis());
////            form.setCreatedDate(tmstm);
////            form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());   
//            
////            for (MWorkflowDetail mWorkflowDetail : form.getmWorkflowDetails() ) {
////            	mWorkflowDetail.setmWorkflow(form);        
////            }
//    	} catch (Exception ex) {
//    		System.out.print(ex);
//    	}
//    }


	@GetMapping(REQUEST_MAPPING_EDIT)
	public String showPageEdit(Model model, @RequestParam(value = "no", required = true) String no) {
		MWorkflow MWorkflow = workflowService.selectMWorkflowById(no);
		if (MWorkflow != null) {
			model.addAttribute("form", MWorkflow);
			return PAGE_EDIT;
		}
		return REDIRECT_TO_LIST + "?error=Data Workflow tidak ditemukan";
	}

	@PostMapping(REQUEST_MAPPING_EDIT)
	//@ResponseBody
	public String doProsesEdit(@ModelAttribute("form") MWorkflow form, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
		MWorkflow existing = workflowService.selectMWorkflowById(form.getId());
		form.setCode(existing.getCode());

		if (!errors.hasErrors()) {
			form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

			try {
				workflowService.saveOrUpdateWorkflow(form);

				model.asMap().clear();
				return REDIRECT_TO_LIST;
			} catch (DataIntegrityViolationException e) {
				if (e.getMessage().startsWith(HttpStatus.BAD_REQUEST.getReasonPhrase())) {
					if (e.getMessage().endsWith("mLaw")) {
						errors.rejectValue("mLaw.id", "field.error.invalid.value", new Object[]{"Undang-undang"}, "");
					}
				} else {
					model.addAttribute("errorMessage", "Gagal mengubah Workflow");
				}
			}
		}

		return PAGE_EDIT + "?no=" + form.getId();
	}

}
