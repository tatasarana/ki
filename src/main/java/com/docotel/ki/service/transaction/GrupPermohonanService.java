package com.docotel.ki.service.transaction;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.docotel.ki.model.counter.TcGroupNo;
import com.docotel.ki.model.transaction.TxGroup;
import com.docotel.ki.model.transaction.TxGroupDetail;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.docotel.ki.repository.custom.counter.TcGroupNoCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxGroupCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxGroupDetailCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmGeneralCustomRepository;
import com.docotel.ki.repository.transaction.TxGroupDetailRepository;
import com.docotel.ki.repository.transaction.TxGroupRepository;
import com.docotel.ki.repository.transaction.TxTmClassRepository;
import com.docotel.ki.repository.transaction.TxTmGeneralRepository;
import com.docotel.ki.repository.transaction.TxTmOwnerRepository;

@Service
public class GrupPermohonanService {
	@Autowired private TxTmGeneralCustomRepository txTmGeneralCustomRepository;	
	@Autowired private TxGroupCustomRepository txGroupCustomRepository;	
	@Autowired private TxGroupDetailCustomRepository txGroupDetailCustomRepository;	
	@Autowired private TxTmGeneralRepository txTmGeneralRepository;
	@Autowired private TxGroupRepository txGroupRepository;	
	@Autowired private TxGroupDetailRepository txGroupDetailRepository;	
	@Autowired private TcGroupNoCustomRepository tcGroupNoCustomRepository;
	@Autowired private TxTmClassRepository txTmClassRepository;
	@Autowired private TxTmOwnerRepository txTmOwnerRepository;



	// Algoritma Search nya mesti diubah
	public GenericSearchWrapper<TxGroup> searchGroup(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<TxGroup> searchResult = new GenericSearchWrapper<TxGroup>();
		searchResult.setCount(txGroupCustomRepository.countGroupPermohonan(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(txGroupCustomRepository.selectAllGroupPermohonan(searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}
	
	public GenericSearchWrapper<TxGroup> searchGroupDistribusi(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<TxGroup> searchResult = new GenericSearchWrapper<TxGroup>();
		searchResult.setCount(txGroupCustomRepository.countDistribusi(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(txGroupCustomRepository.selectDistribusi(searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}
	
	public GenericSearchWrapper<TxGroup> searchGroupPublikasi(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<TxGroup> searchResult = new GenericSearchWrapper<TxGroup>();
		searchResult.setCount(txGroupCustomRepository.countPublikasi(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(txGroupCustomRepository.selectPublikasi(searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}
	
	public GenericSearchWrapper<TxGroupDetail> searchGroupDetail(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<TxGroupDetail> searchResult = new GenericSearchWrapper<TxGroupDetail>();
		searchResult.setCount(txGroupDetailCustomRepository.count(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(txGroupDetailCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}
	
	public GenericSearchWrapper<Object[]> searchGroupDetailList(String groupId) {
		GenericSearchWrapper<Object[]> searchResult = new GenericSearchWrapper<Object[]>();
		searchResult.setList(txGroupDetailCustomRepository.searchGroupDetailByGroupId(groupId));
		return searchResult;
	}
	
	public GenericSearchWrapper<TxGroupDetail> reportEkspedisi(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<TxGroupDetail> searchResult = new GenericSearchWrapper<TxGroupDetail>();
		searchResult.setCount(txGroupDetailCustomRepository.count(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(txGroupDetailCustomRepository.selectReportEkspedisi(searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}

	public GenericSearchWrapper<TxTmGeneral> searchGeneralFromGrupPermohonan(List<KeyValue> searchCriteria, String excludeStatus[], String[] exclude, String[] include, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<TxTmGeneral> searchResult = new GenericSearchWrapper<TxTmGeneral>();
		searchResult.setCount(txTmGeneralCustomRepository.countFromGrupPermohonan(searchCriteria, excludeStatus, exclude, include));
		if (searchResult.getCount() > 0) {
			searchResult.setList(txTmGeneralCustomRepository.selectAllFromGrupPermohonan(searchCriteria, excludeStatus, exclude, include, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}
	
	@Transactional
	public void insertGroup(TxGroup txGroup) {
		Map<String, Object> findCriteria = new HashMap<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
		findCriteria.put("year", currentYear);

        TcGroupNo tcGroupNo = tcGroupNoCustomRepository.findOneBy(findCriteria, null);
        if (tcGroupNo == null) {
        	tcGroupNo = new TcGroupNo();
        	tcGroupNo.setYear(currentYear);
        }
        tcGroupNo.increaseSequence();
        tcGroupNoCustomRepository.saveOrUpdate(tcGroupNo);
		
        txGroup.setNo(tcGroupNo.toString());
		//txGroup.setStatusFlag(true);
		txGroupCustomRepository.saveOrUpdate(txGroup);
	}
	
	@Transactional
	public void deleteGroup(String group_id) {		
		txGroupRepository.delete(group_id);
	}
	
	@Transactional
	public void releaseGroup(TxGroup txGroup){	
		//txGroup.setStatusFlag(false);
		txGroupCustomRepository.saveOrUpdate(txGroup);	
	}
	
	public List<TxGroup> selectAllListTxGroup() {
		return txGroupCustomRepository.selectAllListTxGroup();
	}
	
	@Transactional
	public void insertGroupDetail(int totalPermohonan, String[] appId, String groupId, List<TxGroupDetail> txGroupDetail) {
		TxGroup txGroup = txGroupRepository.findOne(groupId);
		txGroup.setTotal(totalPermohonan);

		String[] txTmGeneralList = txTmGeneralCustomRepository.findAllTxTmGeneralByNative(appId);
		
		if(txGroup.getGroupType().getName().equals("Publikasi"))
			txTmGeneralCustomRepository.updateGroupJournalFlagTxTmGeneralByBatch(true, txTmGeneralList);
		else 
			txTmGeneralCustomRepository.updateGroupDistFlagTxTmGeneralByBatch(true, txTmGeneralList);
		
		txGroupCustomRepository.saveOrUpdate(txGroup);
		txGroupDetailCustomRepository.saveTxGroupDetailByBatch(txGroupDetail);
	}
	
	@Transactional
	public void deleteGroupDetail(TxGroupDetail txGroupDetail) {
		TxGroup txGroup = txGroupDetail.getTxGroup();
		txGroup.setTotal(txGroup.getTotal()-1);
		
		TxTmGeneral txTmGeneral = txGroupDetail.getTxTmGeneral();
		if(txGroup.getGroupType().getName().equals("Publikasi"))
			txTmGeneral.setGroupJournal(false);
		else 
			txTmGeneral.setGroupDist(false);
		
		txGroupCustomRepository.saveOrUpdate(txGroup);
		txTmClassRepository.save(txTmGeneral.getTxTmClassList());
		txTmOwnerRepository.save(txTmGeneral.getTxTmOwner());
		txTmGeneralCustomRepository.saveOrUpdate(txTmGeneral);
		
		txGroupDetailCustomRepository.delete(txGroupDetail.getId());
	}	
	
	@Transactional
	public void deleteTxGroupDetailByBatch(int totalPermohonan, String groupId, String[] groupDetailId) {
		TxGroup txGroup = txGroupRepository.findOne(groupId);
		txGroup.setTotal(totalPermohonan);
		
		String[] txTmGeneralList = txTmGeneralCustomRepository.findAllTxTmGeneralByTxGroupDetailIdNative(groupDetailId);
		
		if(txGroup.getGroupType().getName().equals("Publikasi"))
			txTmGeneralCustomRepository.updateGroupJournalFlagTxTmGeneralByBatch(false, txTmGeneralList);
		else 
			txTmGeneralCustomRepository.updateGroupDistFlagTxTmGeneralByBatch(false, txTmGeneralList);
		
		txGroupCustomRepository.saveOrUpdate(txGroup);
		txGroupDetailCustomRepository.deleteTxGroupDetailByBatch(groupDetailId);
	}
	
	@Transactional
	public void deleteGroupDetailById(String id, String groupId) {
		
		TxGroup txGroup = txGroupRepository.findOne(groupId);
		txGroup.setTotal(txGroup.getTotal() - 1);
		
		txGroupDetailCustomRepository.delete(id);
	}
	
	@Transactional
	public void releaseGroupDetail(TxGroupDetail txGroupDetail){
		TxTmGeneral txTmGeneral = txGroupDetail.getTxTmGeneral();
		
		if(txGroupDetail.getTxGroup().getGroupType().getName().equals("Publikasi"))
			txTmGeneral.setGroupJournal(false);
		else
			txTmGeneral.setGroupDist(false);	
		
		txTmGeneralCustomRepository.saveOrUpdate(txTmGeneral);		
	}
	
	public TxGroup selectOneGroupById(String id) {
		return txGroupCustomRepository.selectOne("LEFT JOIN FETCH c.groupType", "id", id, true, "id", "ASC");
	}
	
	public TxGroup findOneGroupById(String id) {
		return txGroupRepository.getOne(id);
	}
	
	public TxGroupDetail findOneGroupDetailById(String id) {
		return txGroupDetailRepository.findOne(id);
	}
	
	public TxTmGeneral findOneGeneralById(String id) {
		return txTmGeneralRepository.getOne(id);
		//return txTmGeneralRepository.findOne(id);
    }
	
	public List<TxGroupDetail> selectReportEkspedisi(List<KeyValue> searchCriteria, String orderBy, String orderType) {
		return txGroupDetailCustomRepository.selectAll(searchCriteria, orderBy, orderType, null, null);
	}
    
	public TxGroupDetail findOnebyGroupId(String groupId) {
		return txGroupDetailCustomRepository.selectOne("txGroup.id", groupId);
	}
	
	public List<TxGroupDetail> selectAllGroupDetail(String by, String value) {
		return txGroupDetailCustomRepository.selectAll("JOIN FETCH c.txGroup JOIN FETCH c.txTmGeneral", by, value, true, null, null);
	}
	
	public String[] selectAllGroupDetail(String groupId) {
		return txGroupDetailRepository.findTxGroupDetailByTxGroupId(groupId);
	}
}
