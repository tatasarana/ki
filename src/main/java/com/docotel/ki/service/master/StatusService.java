package com.docotel.ki.service.master;

import com.docotel.ki.model.master.MAction;
import com.docotel.ki.model.master.MDocument;
import com.docotel.ki.model.master.MStatus;
import com.docotel.ki.model.transaction.TxMonitor;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.master.MActionCustomRepository;
import com.docotel.ki.repository.custom.master.MDocumentCustomRepository;
import com.docotel.ki.repository.custom.master.MStatusCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxMonitorCustomRepository;
import com.docotel.ki.repository.master.MActionRepository;
import com.docotel.ki.repository.master.MStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StatusService {
	@Autowired
	MStatusRepository mStatusRepository;
	@Autowired
	MStatusCustomRepository mStatusCustomRepository;
	@Autowired
	TxMonitorCustomRepository txMonitorCustomRepository;
	@Autowired
	private MDocumentCustomRepository mDocumentCustomRepository;

	@Autowired
	private MActionRepository mActionRepository;
	@Autowired
	private MActionCustomRepository mActionCustomRepository;

	public List<MStatus> selectStatus() {
		return mStatusRepository.findAll();
	}

	public List<MStatus> selectStatusActive() {
		return mStatusCustomRepository.selectAll("statusFlag", true, true, null, null);
	}

	public MStatus selectOneStatus(String by, String value) {
		return mStatusCustomRepository.selectOne(by, value, true);
	}
	
	public MStatus selectOneBy(String id) {
		return mStatusCustomRepository.selectOne("id", id);
	}

	public GenericSearchWrapper<MStatus> searchGeneralStatus(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<MStatus> searchResult = new GenericSearchWrapper<MStatus>();
		searchResult.setCount(mStatusCustomRepository.count(searchCriteria));

		if (searchResult.getCount() > 0) {
			searchResult.setList(mStatusCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}

	public GenericSearchWrapper<MAction> searchGeneralAction(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<MAction> searchResult = new GenericSearchWrapper<MAction>();
		searchResult.setCount(mActionCustomRepository.count(searchCriteria));

		if (searchResult.getCount() > 0) {
			searchResult.setList(mActionCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}

	public GenericSearchWrapper<TxMonitor> searchGeneralMonitor(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<TxMonitor> searchResult = new GenericSearchWrapper<TxMonitor>();
		searchResult.setCount(txMonitorCustomRepository.countAllMonitor(searchCriteria));

		if (searchResult.getCount() > 0) {
			searchResult.setList(txMonitorCustomRepository.selectAllMonitor(searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}

	public GenericSearchWrapper<MDocument> searchGeneralDocument(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<MDocument> searchResult = new GenericSearchWrapper<MDocument>();
		searchResult.setCount(mDocumentCustomRepository.count(searchCriteria));

		if (searchResult.getCount() > 0) {
			searchResult.setList(mDocumentCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}

	public MStatus selectOneStatusByCode(String code) {
		return mStatusCustomRepository.selectOne("JOIN FETCH c.createdBy cb", "code", code, true);
	}

	public MStatus selectOneStatusByName(String name) {
		return mStatusCustomRepository.selectOne("JOIN FETCH c.createdBy cb", "name", name, true);
	}

	@Transactional
	public void removeStatus(MStatus data) {
		mStatusRepository.delete(data);
	}

	@Transactional
	public void removeStatus(MAction data) {
		mActionRepository.delete(data);
	}

	@Transactional
	public void saveStatus(MStatus mStatus) {
		mStatusRepository.save(mStatus);
	}

	public List<MAction> selectAction() {
		return mActionRepository.findAll();
	}

	public MAction selectOneActionByCode(String code) {
		return mActionRepository.findMActionByCode(code);
	}

	public MAction selectOneActionByName(String name) {
		return mActionRepository.findMActionByName(name);
	}

	public void saveAction(MAction mAction) {
		mActionRepository.save(mAction);
	}

	public MAction selectOneAction(String id) {
		return mActionRepository.findOne(id);
	}
}
