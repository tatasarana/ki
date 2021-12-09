package com.docotel.ki.service.master;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.master.MWorkflowProcess;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.master.MWorkflowProcessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkFlowProcessService extends BaseRepository<MWorkflowProcess> {

    @Autowired
    private MWorkflowProcessRepository mWorkflowProcessRepository;

    @Transactional
    public void insertWorkflowProcess(MWorkflowProcess data) {
        mWorkflowProcessRepository.save(data);
    }

    @Transactional
    public void removeWorkflowProcess(MWorkflowProcess data) {
        mWorkflowProcessRepository.delete(data);
    }

    @Transactional
    public void saveOrUpdateWorkflowProcess(MWorkflowProcess data) {

        mWorkflowProcessRepository.save(data);
    }

    public List<MWorkflowProcess> findAllWorkflowProcess() {
        return mWorkflowProcessRepository.findAll();
    }

    public MWorkflowProcess findById(String id) {
        return mWorkflowProcessRepository.findOne(id);
    }

    public MWorkflowProcess selectOneWorkflowProcessByName(String name){
        return mWorkflowProcessRepository.findMWorkflowProcessByName(name);
    }

    public int findNextOrder(String wfId){
        List<MWorkflowProcess> mWorkflowProcesses = mWorkflowProcessRepository.findMWorkflowProcessesByWorkflowIdOrderByOrders(wfId);
        if(mWorkflowProcesses.size()>0) {
            MWorkflowProcess wfp = mWorkflowProcesses.get(mWorkflowProcesses.size() - 1);
            return wfp.getOrders() + 1;
        }else{
            return 0;
        }
    }

    public GenericSearchWrapper<MWorkflowProcess> searchGeneralWorkflowProcess(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        GenericSearchWrapper<MWorkflowProcess> searchResult = new GenericSearchWrapper<MWorkflowProcess>();
        searchResult.setCount(this.count(searchCriteria));
        if (searchResult.getCount() > 0) {
            searchResult.setList(this.selectAll(searchCriteria, orderBy, orderType, offset, limit));
        }
        return searchResult;
    }

    @Override
    public List<MWorkflowProcess> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        return super.selectAll("LEFT JOIN FETCH c.createdBy cb ",
                searchCriteria, orderBy, orderType, offset, limit);
    }
}
