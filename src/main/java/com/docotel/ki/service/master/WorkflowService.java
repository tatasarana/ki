package com.docotel.ki.service.master;

import com.docotel.ki.model.master.MWorkflow;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.master.MWorkflowCustomRepository;
import com.docotel.ki.repository.master.MWorkflowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkflowService {
	@Autowired
	private MWorkflowRepository mWorkflowRepository;

	@Autowired
	private MWorkflowCustomRepository mWorkflowCustomRepository;	

	public List<MWorkflow> selectActivesOrUsed(String workflowId) {
		return mWorkflowCustomRepository.selectActivesOrUsed(workflowId);
	}

	//Workflow Service
	public List<MWorkflow> findAllWorkflow() {
		return mWorkflowRepository.findAll();
	}


	@Transactional
	public void insertWorkflow(MWorkflow data) {
		mWorkflowRepository.save(data);
	}

	@Transactional
	public void removeWorkflow(MWorkflow data) {
		mWorkflowRepository.delete(data);
	}

	@Transactional
	public void saveOrUpdateWorkflow(MWorkflow data) {

		mWorkflowCustomRepository.saveOrUpdate(data);
	}

	public GenericSearchWrapper<MWorkflow> searchGeneralWorkflow(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<MWorkflow> searchResult = new GenericSearchWrapper<MWorkflow>();
		searchResult.setCount(mWorkflowCustomRepository.count(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(mWorkflowCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}

	public MWorkflow selectMWorkflowById(String s) {
		return mWorkflowRepository.findOne(s);
	}

	public MWorkflow selectOneWorkflowByCode(String code) {
		return mWorkflowCustomRepository.selectOne("JOIN FETCH c.createdBy cb", "code", code, true);
	}

	public MWorkflow selectOneWorkflowByName(String name) {
		return mWorkflowCustomRepository.selectOne("JOIN FETCH c.createdBy cb", "name", name, true);
	}
	
}