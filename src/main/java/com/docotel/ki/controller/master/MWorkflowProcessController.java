package com.docotel.ki.controller.master;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.*;
import com.docotel.ki.model.transaction.TxMonitor;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.master.*;
import com.docotel.ki.service.master.LawService;
import com.docotel.ki.service.master.MenuService;
import com.docotel.ki.service.master.WorkFlowProcessService;
import com.docotel.ki.service.master.WorkflowProcessActionsService;
import com.docotel.ki.service.transaction.MonitorService;
import com.docotel.ki.util.FieldValidationUtil;
import com.docotel.ki.model.master.*;
import com.docotel.ki.repository.custom.master.MWorkflowProcessCustomRepository;
import com.docotel.ki.repository.transaction.TxTmGeneralRepository;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class MWorkflowProcessController extends BaseController {

    @Autowired
    private TxTmGeneralRepository txTmGeneralRepository;
    @Autowired
    WorkFlowProcessService workFlowProcessService;
    @Autowired
    MenuService menuService;
    @Autowired private LawService lawService;
    @Autowired private WorkflowProcessActionsService workflowProcessActionsService;
    @Autowired
    private MWorkflowProcessRepository mWorkflowProcessRepository;
    @Autowired
    private MWorkflowProcessCustomRepository mWorkflowProcessCustomRepository;
    @Autowired
    private MWorkflowRepository mWorkflowRepository;
    @Autowired
    private MStatusRepository mStatusRepository;
    @Autowired
    private MonitorService monitorService;
    @Autowired
    private MRoleRepository mRoleRepository ;

    private static final String DIRECTORY_PAGE = "master/workflow_process/";

    private static final String PAGE_LIST = DIRECTORY_PAGE + "list-workflow-process";
    private static final String PAGE_TAMBAH = DIRECTORY_PAGE + "tambah-workflow-process";
    private static final String PAGE_EDIT = DIRECTORY_PAGE + "edit-workflow-process";


//    private static final String PAGE_EDIT_WORKFLOW_PROCESS_DETAIL = DIRECTORY_PAGE + "workflow-process-detail";
//    private static final String PAGE_DETAIL_WORKFLOW_PROCESS = DIRECTORY_PAGE + "workflow-process-detail";
//    private static final String PAGE_TAMBAH_DETAIL = DIRECTORY_PAGE + "tambah-workflow-process-detail";

    private static final String PAGE_LIST_DETAIL = "/list-workflow-process-detail";

    private static final String PATH_LIST_WORKFLOW_PROCESS  = "/list-workflow-process";
    private static final String PATH_EDIT_WORKFLOW_PROCESS  = "/edit-workflow-process";
    private static final String PATH_TAMBAH_WORKFLOW_PROCESS = "/tambah-workflow-process";
    private static final String PATH_REMOVE_WORKFLOW_PROCESS = "/delete-workflow-process";

    private static final String PATH_AJAX_LIST = "/cari-workflow-process";
    private static final String PATH_AJAX_ORDER_NUMBER = "/order_number";


    private static final String REQUEST_MAPPING_AJAX_LIST = PATH_AJAX_LIST + "*";
    private static final String REQUEST_MAPPING_TAMBAH_WORKFLOW_PROCESS = PATH_TAMBAH_WORKFLOW_PROCESS  + "*";
    private static final String REQUEST_MAPPING_EDIT = PATH_EDIT_WORKFLOW_PROCESS  + "*";
    private static final String REQUEST_MAPPING_REMOVE_WORKFLOW_PROCESS = PATH_REMOVE_WORKFLOW_PROCESS  + "*";
//    private static final String REQUEST_MAPPING_LIST_WORKFLOW_PROCESS_DETAIL = PAGE_LIST_DETAIL + "*";
//
//    private static final String REQUEST_MAPPING_TAMBAH_WORK_FLOW_DETAIL = PAGE_TAMBAH_DETAIL  + "*";


    private static final String REDIRECT_TO_LIST = "redirect:" + PATH_AFTER_LOGIN + PATH_LIST_WORKFLOW_PROCESS ;
//    private static final String REDIRECT_TO_DETAIL = "redirect:" + PATH_AFTER_LOGIN + PAGE_LIST_DETAIL ;

    private static final String PATH_AJAX_FIND_WORKFLOW_PROCESS = "/ajax-workflow-process";



    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "config");
        model.addAttribute("subMenu", "listWorkflowProcess");

        List<MLaw> listMLaw = lawService.findAll();
        model.addAttribute("listMLaw", listMLaw);

        if (request.getRequestURI().contains(PATH_TAMBAH_WORKFLOW_PROCESS) || request.getRequestURI().contains(PAGE_LIST_DETAIL)) {
            if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
                MWorkflowProcess mwf = new MWorkflowProcess();
                mwf.setStatusFlag(true);
                model.addAttribute("form", mwf);
            }
        } else {

        }
    }

    @RequestMapping(path = PATH_LIST_WORKFLOW_PROCESS )
    public String showPageList(@RequestParam(value = "error", required = false) String error, Model model) {
        List<MWorkflowProcess> list = workFlowProcessService.findAllWorkflowProcess();
        List<MWorkflow> workflows = mWorkflowRepository.findAll();
        List<MStatus> status = mStatusRepository.findAll();
        model.addAttribute("list", list);
        model.addAttribute("workflows", workflows);
        model.addAttribute("status", status);

        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("errorMessage", error);
        }
        return PAGE_LIST;
    }

    @PostMapping(REQUEST_MAPPING_REMOVE_WORKFLOW_PROCESS )
    public String doRemoveworkflowProcess(@ModelAttribute("form")  MWorkflowProcess data, final Model model, final HttpServletRequest request, final HttpServletResponse response,
                                   final BindingResult errors) {

        if (!errors.hasErrors()) {
            data.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

            try {
                workFlowProcessService.removeWorkflowProcess(data);

                model.asMap().clear();
                return REDIRECT_TO_LIST;
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                writeJsonResponse(response, 500);
            }
        }

        return PAGE_LIST;
    }

    @PostMapping(REQUEST_MAPPING_TAMBAH_WORKFLOW_PROCESS )
    public String doSaveworkflow(@ModelAttribute("form")  MWorkflowProcess data, final Model model, final HttpServletRequest request, final HttpServletResponse response,
                                 final BindingResult errors) {


        if(FieldValidationUtil.required(errors, "name", data.getName(), "Name workflow process")) {


            MWorkflowProcess existingName = workFlowProcessService.selectOneWorkflowProcessByName(data.getName());
            if(existingName!=null) {
                errors.rejectValue("name", "error.name.exists", "Name workflow process sudah ada.");
            }
        }


        if (!errors.hasErrors()) {
            data.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            data.setStatusFlag(true);
            MRole irole = new MRole();
            irole = data.getCc_role();


            try {
            	if(data.getStatus() == null && data.getPrevStatus() == null) {
            		model.addAttribute("errorMessage", "Field Status sebelumnya dan Menjadi status Tidak boleh kosong!");
        			List<MWorkflow> workflows = mWorkflowRepository.findAll();
                    model.addAttribute("workflows", workflows);
                    List<MStatus> statuses =  mStatusRepository.findAll();
                    model.addAttribute("statuses", statuses);
        		} else if(data.getPrevStatus() == null) {
        			model.addAttribute("errorMessage", "Field Menjadi status Tidak boleh kosong!");
        			List<MWorkflow> workflows = mWorkflowRepository.findAll();
                    model.addAttribute("workflows", workflows);
                    List<MStatus> statuses =  mStatusRepository.findAll();
                    model.addAttribute("statuses", statuses);
        		} else if(data.getStatus() == null) {
        			model.addAttribute("errorMessage", "Field Status sebelumnya Tidak boleh kosong!");
        			List<MWorkflow> workflows = mWorkflowRepository.findAll();
                    model.addAttribute("workflows", workflows);
                    List<MStatus> statuses =  mStatusRepository.findAll();
                    model.addAttribute("statuses", statuses);
        		/*} else if(data.getStatus().getId().equalsIgnoreCase(data.getPrevStatus().getId())) {
            		model.addAttribute("errorMessage", "Gagal menambahkan! Field Status sebelumnya Tidak boleh sama dengan Field Menjadi status");
            		List<MWorkflow> workflows = mWorkflowRepository.findAll();
                    model.addAttribute("workflows", workflows);
                    List<MStatus> statuses =  mStatusRepository.findAll();
                    model.addAttribute("statuses", statuses); STATUS BOLEH SAMA KARENA ADA CASE STATUS TIDAK BERUBAH*/
            	} else {
            		workFlowProcessService.insertWorkflowProcess(data);

                    model.asMap().clear();
                    return REDIRECT_TO_LIST;
            	}
            } catch (DataIntegrityViolationException e) {
                if (e.getMessage().startsWith(HttpStatus.BAD_REQUEST.getReasonPhrase())) {
                    if (e.getMessage().endsWith("mLaw")) {
                        errors.rejectValue("mLaw.id", "field.error.invalid.value", new Object[]{"Undang-undang"}, "");
                    }
                } else {
                    List<MWorkflow> workflows = mWorkflowRepository.findAll();
                    model.addAttribute("workflows", workflows);
                    List<MStatus> statuses =  mStatusRepository.findAll();
                    model.addAttribute("statuses", statuses);
                    model.addAttribute("errorMessage", "Gagal menambahkan Workflow Process");
                }
                writeJsonResponse(response, 500);
            }
        }else{
            List<MWorkflow> workflows = mWorkflowRepository.findAll();
            model.addAttribute("workflows", workflows);
            List<MStatus> statuses =  mStatusRepository.findAll();
            model.addAttribute("statuses", statuses);
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
                    Object value = null;
                    try {
                        if(keywordArr[i].equalsIgnoreCase("0")){
                            value = false;
                        }else if(keywordArr[i].equalsIgnoreCase("1")){
                            value = true;
                        }else {
                            value = keywordArr[i];
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
//                        System.out.println(e);
                    }
                    if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                        if (StringUtils.isNotBlank(value.toString())) {
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
                            orderBy = "orders";
                            break;
                        case "2" :
                            orderBy = "name";
                            break;
                        case "4":
                            orderBy = "status.name";
                            break;
                        case "5":
                            orderBy = "mandatory";
                            break;
                        default :
                            orderBy = "orders";
                            break;
                    }
                }
            }
            orderBy += ", c.id";

            String orderType = request.getParameter("orderType");
            if (orderType == null) {
                orderType = "ASC";
            } else {
                orderType = orderType.trim();
                if (!orderType.equalsIgnoreCase("DESC")) {
                    orderType = "ASC";
                }
            }

            List<String[]> data = new ArrayList<>();

            GenericSearchWrapper<MWorkflowProcess> searchResult = workFlowProcessService.searchGeneralWorkflowProcess(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
            	try {
            		dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                    dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                    int no = offset;
                    for (MWorkflowProcess result : searchResult.getList()) {
                        Date date = new Date(result.getCreatedDate().getTime());
                        String pDate = new SimpleDateFormat("dd-MM-yyyy").format(date);
                        
                        // For user role access button menu
                        MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                        String button = "";
                        if(mUser.hasAccessMenu("T-DWP")) {
                        	button = "<div class=\"btn-actions\">" +
                                     "<a class=\"btn btn-primary btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT_WORKFLOW_PROCESS + "?no=" + result.getId()) + "\">Detail</a>"+ "<br/>" +
                                     "</div>";
                        }

                        no++;
                        data.add(new String[]{
                                "" + no,
                                String.valueOf(result.getOrders()),
                                result.getName(),
                                result.getWorkflow().getName(),
                                result.getPrevStatus() == null ? "" : result.getPrevStatus().getName(),
                                result.getStatus().getName(),
                                result.getRole() == null ? "" : result.getRole().getName(),
                                result.isStatusFlag()? "Aktif": "Tidak Aktif",
                                button
                                /*"<div class=\"btn-actions\">" +
                                "<a class=\"btn btn-primary btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT_WORKFLOW_PROCESS + "?no=" + result.getId()) + "\">Detail</a>"+ "<br/>" +*/
//                                "<a class=\"btn btn-danger btn-xs\" href=\"" + getPathURLAfterLogin(PATH_REMOVE_WORKFLOW_PROCESS + "?no=" + result.getId()) + "\">Hapus</a>"+ "<br/>" +
                                        /*"</div>"*/
                        });
                    }
            	} catch(Exception e) {
            	}
                
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @GetMapping(REQUEST_MAPPING_TAMBAH_WORKFLOW_PROCESS )
    public String showPageTambah(final Model model) {
        List<MWorkflow> workflows = mWorkflowRepository.findAll();
        model.addAttribute("workflows", workflows);
        List<MStatus> statuses =  mStatusRepository.findMStatusByStatusFlag(true);
        List<MRole> userroles = mRoleRepository.findAll();

        Collections.sort(statuses, (o1, o2) -> o1.getName().compareTo(o2.getName()));
        Collections.sort(userroles, (o1, o2) -> o1.getName().compareTo(o2.getName()));
        model.addAttribute("userroles", userroles);
        model.addAttribute("statuses", statuses);
        return PAGE_TAMBAH;
    }

    @GetMapping(REQUEST_MAPPING_EDIT)
    public String showPageEdit(Model model, @RequestParam(value = "no", required = true) String no) {
        MWorkflowProcess mWorkflowProcess = workFlowProcessService.findById(no);
        if (mWorkflowProcess != null) {
            model.addAttribute("form", mWorkflowProcess);
            List<MWorkflow> workflows = mWorkflowRepository.findAll();
            model.addAttribute("workflows", workflows);
            List<MStatus> statuses =  mStatusRepository.findMStatusByStatusFlag(true);
            model.addAttribute("statuses", statuses);
            List<MWorkflowProcessActions> pactions = workflowProcessActionsService.findAllWorkflowProcessActionByProcess(mWorkflowProcess);
            List<MRole> userroles = mRoleRepository.findAll();
            Collections.sort(statuses, (o1, o2) -> o1.getName().compareTo(o2.getName()));
            Collections.sort(userroles, (o1, o2) -> o1.getName().compareTo(o2.getName()));
            Collections.sort(pactions, (o1, o2) -> o1.getAction().getName().compareTo(o2.getAction().getName()));

            model.addAttribute("pactions", pactions);
            model.addAttribute("userroles", userroles);
            return PAGE_EDIT;
        }
        return REDIRECT_TO_LIST+ "?error=Data Workflow Process tidak ditemukan";
    }

    @PostMapping(REQUEST_MAPPING_EDIT)
    //@ResponseBody
    public String doProsesEdit(@ModelAttribute("form") MWorkflowProcess form, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {

    	boolean errorFlag = true;
        if (!errors.hasErrors()) {
            form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

            try {
            	if(form.getStatus() == null && form.getPrevStatus() == null) {
            		model.addAttribute("errorMessage", "Field Status sebelumnya dan Menjadi status Tidak boleh kosong!");
        			List<MWorkflow> workflows = mWorkflowRepository.findAll();
                    model.addAttribute("workflows", workflows);
                    List<MStatus> statuses =  mStatusRepository.findAll();
                    model.addAttribute("statuses", statuses);
        		} else if(form.getPrevStatus() == null) {
        			model.addAttribute("errorMessage", "Field Menjadi status Tidak boleh kosong!");
        			List<MWorkflow> workflows = mWorkflowRepository.findAll();
                    model.addAttribute("workflows", workflows);
                    List<MStatus> statuses =  mStatusRepository.findAll();
                    model.addAttribute("statuses", statuses);
        		} else if(form.getStatus() == null) {
        			model.addAttribute("errorMessage", "Field Status sebelumnya Tidak boleh kosong!");
        			List<MWorkflow> workflows = mWorkflowRepository.findAll();
                    model.addAttribute("workflows", workflows);
                    List<MStatus> statuses =  mStatusRepository.findAll();
                    model.addAttribute("statuses", statuses);
        		/*} else if(form.getStatus().getId().equalsIgnoreCase(form.getPrevStatus().getId())) {
            		model.addAttribute("errorMessage", "Gagal menambahkan! Field Status sebelumnya Tidak boleh sama dengan Field Menjadi status");
            		List<MWorkflow> workflows = mWorkflowRepository.findAll();
                    model.addAttribute("workflows", workflows);
                    List<MStatus> statuses =  mStatusRepository.findAll();
                    model.addAttribute("statuses", statuses); STATUS BOLEH SAMA KARENA ADA CASE STATUS TIDAK BERUBAH*/
            	} else {
            		workFlowProcessService.saveOrUpdateWorkflowProcess(form);
            		errorFlag = false;
                    model.asMap().clear();
                    return REDIRECT_TO_LIST;
            	}
            } catch (DataIntegrityViolationException e) {
                if (e.getMessage().startsWith(HttpStatus.BAD_REQUEST.getReasonPhrase())) {
                    if (e.getMessage().endsWith("mLaw")) {
                        errors.rejectValue("mLaw.id", "field.error.invalid.value", new Object[]{"Undang-undang"}, "");
                    }
                } else {
                    model.addAttribute("errorMessage", "Gagal mengubah Workflow Process");
                }
            }
        }
        if(errorFlag) {
        	return PAGE_EDIT;
        }
        
        return PAGE_EDIT + "?no=" + form.getId();
    }

    @GetMapping(PATH_REMOVE_WORKFLOW_PROCESS)
    public String deleteWfProcess(@RequestParam(value = "no", required = true) String no) {
        MWorkflowProcess mWorkflowProcess = workFlowProcessService.findById(no);
        if (mWorkflowProcess != null) {
            try{
                workFlowProcessService.removeWorkflowProcess(mWorkflowProcess);
            }catch(Exception ex){
                return REDIRECT_TO_LIST+ "?error=Workflow Process tidak dapat dihapus";
            }
            return REDIRECT_TO_LIST;
        }
        return REDIRECT_TO_LIST+ "?error=Data Workflow Process tidak ditemukan";
    }

    @GetMapping(PATH_AJAX_ORDER_NUMBER)
    @ResponseBody
    public int getOrderNumber(@RequestParam(value="wfId", required = true) String wfId){
        return workFlowProcessService.findNextOrder(wfId);
    }

    @GetMapping(PATH_AJAX_FIND_WORKFLOW_PROCESS)
    @ResponseBody
    public List<MWorkflowProcess> findOneWorkflowProcess(@RequestParam(value="no", required = true) String no,@RequestParam(value="id", required = true) String id){
        TxTmGeneral ttg = txTmGeneralRepository.findTxTmGeneralByApplicationNo(no);
        List<TxMonitor> listCurrentMonitor = monitorService.findByTxTmGeneral(ttg);
        MWorkflow wf = ttg.getTxReception().getmWorkflow();

        List<MWorkflowProcess> listWfProcess= mWorkflowProcessCustomRepository.findMWorkflowProcessesByWorkflowAndStatusFlagOrderByOrders(wf.getId(),true, id);
        List<MWorkflowProcess> listAvailableWfProcesses = new ArrayList<>();
        for (MWorkflowProcess mWfProcess: listWfProcess) {
            boolean found = false;
            for(TxMonitor txM: listCurrentMonitor){
                if(mWfProcess.getId().equalsIgnoreCase(txM.getmWorkflowProcess().getId())){
                    found = true;
                    break;
                }
            }
            if(!found && !mWfProcess.getStatus().isStaticFlag()){
                mWfProcess.getWorkflow().setCreatedBy(null);
                mWfProcess.getStatus().setCreatedBy(null);
                mWfProcess.setCreatedBy(null);
                listAvailableWfProcesses.add(mWfProcess);

                if(mWfProcess.isMandatory()){
                    break;
                }
            }
        }
        return listAvailableWfProcesses;
    }
}
