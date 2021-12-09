package com.docotel.ki.controller.master;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.*;
import com.docotel.ki.pojo.WfProcessAction;
import com.docotel.ki.service.master.LawService;
import com.docotel.ki.service.master.WorkFlowProcessService;
import com.docotel.ki.service.master.WorkflowProcessActionsService;
import com.docotel.ki.model.master.*;
import com.docotel.ki.repository.master.MActionRepository;
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
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class MWorkflowProcessActionController extends BaseController{

    @Autowired
    private WorkflowProcessActionsService workflowProcessActionsService;
    @Autowired
    WorkFlowProcessService workFlowProcessService;
    @Autowired private LawService lawService;

    @Autowired
    private MActionRepository mActionRepository;

    private static final String DIRECTORY_PAGE = "master/workflow_process_action/";

    private static final String PATH_TAMBAH_WORKFLOW_PROCESS_ACTION = "/tambah-workflow-process";
    private static final String PAGE_LIST_DETAIL = "/list-workflow-process-action-detail";
    private static final String PATH_ADD_WORKFLOW_PROCESS_ACTION  = "/tambah-workflow-process-action";
    private static final String PATH_EDIT_WORKFLOW_PROCESS = "/edit-workflow-process";
    private static final String PATH_REMOVE_WORFLOW_PROCESS_ACTION = "/remove-workflow-process-action";
    private static final String PAGE_ADD = DIRECTORY_PAGE + "add-workflow-process-action";
    private static final String PATH_AJAX_WORKFLOW_PROCESS_ACTION = "/ajax-workflow-process-action";
    private static final String PATH_AJAX_FIND_WORKFLOW_PROCESS_ACTION = "/ajax-workflow-process-action-byid";

    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "config");
        model.addAttribute("subMenu", "listworkflow");

        List<MLaw> listMLaw = lawService.findAll();
        model.addAttribute("listMLaw", listMLaw);

        if (request.getRequestURI().contains(PATH_ADD_WORKFLOW_PROCESS_ACTION) || request.getRequestURI().contains(PAGE_LIST_DETAIL)) {
            if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
                model.addAttribute("form", new MWorkflowProcessActions());
            }
        }
    }

    @GetMapping(PATH_ADD_WORKFLOW_PROCESS_ACTION)
    public String showPageEdit(Model model, @RequestParam(value = "id", required = true) String id) {
        MWorkflowProcess mWorkflowProcess = workFlowProcessService.findById(id);
        MWorkflowProcessActions wfProcessActions = new MWorkflowProcessActions();
        wfProcessActions.setProcess(mWorkflowProcess);

        if (mWorkflowProcess != null) {
            model.addAttribute("form", wfProcessActions);
            List<MAction> actions = mActionRepository.findMActionByStatusFlag(true);
            Collections.sort(actions, (o1, o2) -> o1.getName().compareTo(o2.getName()));

            model.addAttribute("actions", actions);
            return PAGE_ADD;
        }
        return PATH_EDIT_WORKFLOW_PROCESS+"?no="+id;
    }

    @PostMapping(PATH_ADD_WORKFLOW_PROCESS_ACTION)
    public String insertProcessAction(@ModelAttribute("form") MWorkflowProcessActions data, Model model, final HttpServletRequest request, final HttpServletResponse response,
                                      final BindingResult errors){




        if (!errors.hasErrors()) {

            data.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

            try {
            	if (!errors.hasFieldErrors("action.id")) {
            		List<MWorkflowProcessActions> mWorkflowProcessActions = workflowProcessActionsService.findMWorkflowProcessActionsByActionId(data.getAction().getId(), data.getProcess());
                	if (!mWorkflowProcessActions.isEmpty()) {
                		MAction mWfActionName = mActionRepository.findMWorkflowProcessActionsByIdAction(data.getAction().getId());
                		model.addAttribute("data", data);
    	            	model.addAttribute("errorMessage", "Gagal menambah Data Aksi " + mWfActionName.getName() + " sudah ada");
    	            	
    	            	MWorkflowProcess mWorkflowProcess = workFlowProcessService.findById(data.getProcess().getId());


    	                MWorkflowProcessActions wfProcessActions = new MWorkflowProcessActions();
    	                wfProcessActions.setProcess(mWorkflowProcess);

    	                if (mWorkflowProcess != null) {
    	                    model.addAttribute("form", wfProcessActions);
    	                    List<MAction> actions = mActionRepository.findMActionByStatusFlag(true);
                            Collections.sort(actions, (o1, o2) -> o1.getName().compareTo(o2.getName()));
    	                    model.addAttribute("actions", actions);
    	                }
                	} else {
                		workflowProcessActionsService.saveOrUpdateWorkflowProcessAction(data);
                		model.asMap().clear();
                        return "redirect:"+BaseController.PATH_AFTER_LOGIN + PATH_EDIT_WORKFLOW_PROCESS+"?no="+data.getProcess().getId();
                	}
                }
                
                
            } catch (DataIntegrityViolationException e) {
                if (e.getMessage().startsWith(HttpStatus.BAD_REQUEST.getReasonPhrase())) {
                    if (e.getMessage().endsWith("mLaw")) {
                        errors.rejectValue("mLaw.id", "field.error.invalid.value", new Object[]{"Undang-undang"}, "");
                    }
                } else {
                    model.addAttribute("errorMessage", "Gagal menambahkan Workflow Process Action");
                }

                MWorkflowProcess mWorkflowProcess = workFlowProcessService.findById(data.getProcess().getId());
                MWorkflowProcessActions wfProcessActions = new MWorkflowProcessActions();
                wfProcessActions.setProcess(mWorkflowProcess);

                if (mWorkflowProcess != null) {
                    model.addAttribute("form", wfProcessActions);
                    List<MAction> actions = mActionRepository.findMActionByStatusFlag(true);
                    Collections.sort(actions, (o1, o2) -> o1.getName().compareTo(o2.getName()));
                    model.addAttribute("actions", actions);
                }
                writeJsonResponse(response, 500);
            }
           // return "redirect:" + BaseController.PATH_AFTER_LOGIN + PATH_ADD_WORKFLOW_PROCESS_ACTION+"?id=60907e7e-1900-4acf-a60e-d059ef8b3018";
        }else{
            model.addAttribute("errorMessage", "Gagal menambahkan Workflow Process Action");
        }
        return PAGE_ADD;
    }

    @GetMapping(PATH_REMOVE_WORFLOW_PROCESS_ACTION)
    public String removeData(Model model, @RequestParam(value = "id", required = true) String id,  @RequestParam(value = "no", required = true) String no) {
        //System.out.println("ID : "+id);
        MWorkflowProcessActions wfProcessActions = workflowProcessActionsService.findById(id);
        //System.out.println("WF Process Action ID: " + wfProcessActions.getId());
        if (wfProcessActions != null) {
            try {

                workflowProcessActionsService.removeWorkflowProcessAction(wfProcessActions);
            }catch(Exception ex){
                ex.printStackTrace();
                return "redirect:" + BaseController.PATH_AFTER_LOGIN + PATH_EDIT_WORKFLOW_PROCESS+"?no="+no;
            }
        }
        return "redirect:" + BaseController.PATH_AFTER_LOGIN + PATH_EDIT_WORKFLOW_PROCESS+"?no="+no;
    }

    @GetMapping(PATH_AJAX_WORKFLOW_PROCESS_ACTION)
    @ResponseBody
    public List<WfProcessAction> findAllWorkflowProcessActionByProcess(@RequestParam(value="id", required = true) String id){
        MWorkflowProcess wfProcess = workFlowProcessService.findById(id);
        return workflowProcessActionsService.findAllWorkflowProcessActionByProcess1(wfProcess);
    }

    @GetMapping(PATH_AJAX_FIND_WORKFLOW_PROCESS_ACTION)
    @ResponseBody
    public WfProcessAction findOneWorkflowProcessActionById(@RequestParam(value="id", required = true) String id){
        return workflowProcessActionsService.findById2(id);
    }
}
