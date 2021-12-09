package com.docotel.ki.service.master;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.master.MWorkflowProcess;
import com.docotel.ki.model.master.MWorkflowProcessActions;
import com.docotel.ki.pojo.WfProcessAction;
import com.docotel.ki.repository.master.MWorkflowProcessActionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class WorkflowProcessActionsService extends BaseRepository<MWorkflowProcessActions> {

    @Autowired
    private MWorkflowProcessActionRepository mWorkflowProcessActionRepository;


    @Transactional
    public void insertWorkflowProcessAction(MWorkflowProcessActions data) {
        mWorkflowProcessActionRepository.save(data);
    }

    @Transactional
    public void removeWorkflowProcessAction(MWorkflowProcessActions data) {
        mWorkflowProcessActionRepository.delete(data.getId());
    }

    @Transactional
    public void saveOrUpdateWorkflowProcessAction(MWorkflowProcessActions data) {
        mWorkflowProcessActionRepository.save(data);
    }

    public List<MWorkflowProcessActions> findAllWorkflowProcessActionByProcess(MWorkflowProcess process) {
       return  mWorkflowProcessActionRepository.findMWorkflowProcessActionsByProcess(process);
    }
    
    public List<MWorkflowProcessActions> findMWorkflowProcessActionsByActionId(String actionId, MWorkflowProcess process) {
        return  mWorkflowProcessActionRepository.findMWorkflowProcessActionsByActionId(actionId, process);
    }

    public List<WfProcessAction> findAllWorkflowProcessActionByProcess1(MWorkflowProcess process) {
        List<MWorkflowProcessActions> data = mWorkflowProcessActionRepository.findMWorkflowProcessActionsByProcess(process);
        List<WfProcessAction> output = new ArrayList<>();
        for(MWorkflowProcessActions ac : data){
            WfProcessAction wfp = new WfProcessAction();
            wfp.setId(ac.getId());
            wfp.setActionId(ac.getAction().getId());
            wfp.setActionName(ac.getAction().getName());
            wfp.setActionType(ac.getAction().getType());
            output.add(wfp);
        }
        return output;
    }

    @Transactional
    public MWorkflowProcessActions findById(String id) {
       return mWorkflowProcessActionRepository.findMWorkflowProcessActionsById(id);
    }

    @Transactional
    public WfProcessAction findById2(String id) {
        MWorkflowProcessActions ac = mWorkflowProcessActionRepository.findMWorkflowProcessActionsById(id);
        WfProcessAction wfp = new WfProcessAction();
        wfp.setId(ac.getId());
        wfp.setActionId(ac.getAction().getId());
        wfp.setActionName(ac.getAction().getName());
        wfp.setActionType(ac.getAction().getType());
        wfp.setActionDuration(ac.getAction().getDuration());
        return wfp;
    }

}
