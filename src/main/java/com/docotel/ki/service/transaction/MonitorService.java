package com.docotel.ki.service.transaction;

import com.docotel.ki.enumeration.StatusEnum;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.transaction.TxDocumentParams;
import com.docotel.ki.model.transaction.TxMonitor;
import com.docotel.ki.model.transaction.TxPostReceptionDetail;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.transaction.TxMonitorCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxPostReceptionDetailCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmGeneralCustomRepository;
import com.docotel.ki.repository.transaction.TxDocumentParamRepository;
import com.docotel.ki.repository.transaction.TxMonitorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Service
public class MonitorService {

	@Autowired private TxMonitorRepository txMonitorRepository;
	@Autowired private TxMonitorCustomRepository txMonitorCustomRepository;
	@Autowired private TxDocumentParamRepository txDocumentParamRepository;
	@Autowired private TxPostReceptionDetailCustomRepository txPostReceptionDetailCustomRepository;
	@Autowired private TxTmGeneralCustomRepository txTmGeneralCustomRepository;


	@Transactional
	public TxMonitor save(TxMonitor monitor){
		return txMonitorRepository.save(monitor);
	}

	@Transactional
	public void insertPostReceptionDetail(TxPostReceptionDetail txPostReceptionDetail) {
		txPostReceptionDetailCustomRepository.saveOrUpdate(txPostReceptionDetail);
	}

	public List<TxMonitor> findAll() {
		return txMonitorRepository.findAll();
	}

	public List<Object[]> findTxMonitorByYear() {
		return txMonitorRepository.findTxMonitorByYear();
	}

	public List<TxMonitor> findTxMonitorByMWorkflowProcessActionsId(String id) {
		return txMonitorRepository.findTxMonitorByMWorkflowProcessActionsId(id);
	}

	public List<TxMonitor> findTxMonitorByMWorkflowProcessActionsId(String id, String month) {
		return txMonitorRepository.findTxMonitorByMWorkflowProcessActionsId(id, month);
	}

	@Transactional
	public void saveTxTmGeneral(TxTmGeneral txTmGeneral) {
		txTmGeneralCustomRepository.saveOrUpdate(txTmGeneral);
	}

	@Transactional
	public void saveMonitor(TxMonitor monitor, TxPostReceptionDetail txPostReceptionDetail) {
		if(txPostReceptionDetail != null) {
			if(txPostReceptionDetail.getTxPostReception().getId()!=null && (txPostReceptionDetail.getTxPostReception().getmStatus().getId().equalsIgnoreCase("TM_PASCA_PERMOHONAN")
			|| (txPostReceptionDetail.getTxPostReception().getmStatus().getId().equalsIgnoreCase("NOTYET")))) {
				txPostReceptionDetail.setmStatus(StatusEnum.DONE.value());
				txPostReceptionDetailCustomRepository.saveOrUpdate(txPostReceptionDetail);
			}
		}

		txTmGeneralCustomRepository.saveOrUpdate(monitor.getTxTmGeneral());
		txMonitorRepository.save(monitor);
	}

	public void saveTxPostInMonitor(TxMonitor monitor, TxPostReceptionDetail txPostReceptionDetail) {
		txPostReceptionDetail.setmStatus(StatusEnum.NOTYET.value());
		txPostReceptionDetailCustomRepository.saveOrUpdate(txPostReceptionDetail);

//		txTmGeneralCustomRepository.saveOrUpdate(monitor.getTxTmGeneral());
//		txMonitorRepository.save(monitor);
	}

	public GenericSearchWrapper<TxMonitor> searchMonitor(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
		GenericSearchWrapper<TxMonitor> searchResult = new GenericSearchWrapper<TxMonitor>();
		searchResult.setCount(txMonitorCustomRepository.countLapRekapStatusDanAksiMingguan(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(txMonitorCustomRepository.selectAllLapRekapStatusDanAksiMingguan(searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}

	public GenericSearchWrapper<TxMonitor> searchMonitorFungsionalPemeriksa(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
		GenericSearchWrapper<TxMonitor> searchResult = new GenericSearchWrapper<>();
		searchResult.setCount(txMonitorCustomRepository.countFungsionalPemeriksa(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(txMonitorCustomRepository.selectAllFungsionalPemeriksa(searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}


	public GenericSearchWrapper<TxMonitor> searchMonitorPascaPermohonan(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
		GenericSearchWrapper<TxMonitor> searchResult = new GenericSearchWrapper<>();
		searchResult.setCount(txMonitorCustomRepository.countLapPasca(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(txMonitorCustomRepository.selectAllLapPasca(searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}

	public GenericSearchWrapper<TxMonitor> searchMonitorKbm(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
		GenericSearchWrapper<TxMonitor> searchResult = new GenericSearchWrapper<>();
		searchResult.setCount(txMonitorCustomRepository.countFungsionalPemeriksa(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(txMonitorCustomRepository.selectAllFungsionalPemeriksa(searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}


	public GenericSearchWrapper<TxMonitor> searchMonitorKasubditPemeriksa(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
		GenericSearchWrapper<TxMonitor> searchResult = new GenericSearchWrapper<>();
		searchResult.setCount(txMonitorCustomRepository.countKasubditPemeriksa(searchCriteria)); 
		if (searchResult.getCount() > 0) {
			searchResult.setList(txMonitorCustomRepository.selectAllKasubditPemeriksa(searchCriteria, orderBy, orderType, offset, limit));
		} 
		return searchResult;
	}
	
	public GenericSearchWrapper<TxMonitor> searchMonitorKeputusanDirektur(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
		GenericSearchWrapper<TxMonitor> searchResult = new GenericSearchWrapper<>();
		searchResult.setCount(txMonitorCustomRepository.countKeputusanDirektur(searchCriteria)); 
		if (searchResult.getCount() > 0) {
			searchResult.setList(txMonitorCustomRepository.selectAllKeputusanDirektur(searchCriteria, orderBy, orderType, offset, limit));
		} 
		return searchResult;
	}

	public GenericSearchWrapper<TxMonitor> searchMonitorByWfProcessActions(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
		GenericSearchWrapper<TxMonitor> searchResult = new GenericSearchWrapper<TxMonitor>();
		searchResult.setCount(txMonitorCustomRepository.countLapRekapAksi(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(txMonitorCustomRepository.selectAllLapRekapAksi(searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}

	public GenericSearchWrapper<Object[]> searchMonitorByWfProcessActionsMingguan(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
		GenericSearchWrapper<Object[]> searchResult = new GenericSearchWrapper<Object[]>();
		searchResult.setCount(txMonitorCustomRepository.countLapRekapAksi(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(txMonitorCustomRepository.selectAllLapRekapAksiMingguan(searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}

	public GenericSearchWrapper<Object[]> searchMonitorByWfProcessActionsBulanan(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
		GenericSearchWrapper<Object[]> searchResult = new GenericSearchWrapper<Object[]>();
		searchResult.setCount(txMonitorCustomRepository.countLapRekapAksi(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(txMonitorCustomRepository.selectAllLapRekapAksiBulanan(searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}

	public GenericSearchWrapper<Object[]> searchMonitorByWfProcessActionsTahunan(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
		GenericSearchWrapper<Object[]> searchResult = new GenericSearchWrapper<Object[]>();
		searchResult.setCount(txMonitorCustomRepository.countLapRekapAksi(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(txMonitorCustomRepository.selectAllLapRekapAksiTahuan(searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}

	public TxMonitor findOne(String id){
		return txMonitorRepository.findOne(id);
	}

	public long countLapRekapAksi(List<KeyValue> searchCriteria){
		return txMonitorCustomRepository.countLapRekapAksi(searchCriteria);
	}

	public List<TxMonitor> selectAllLapRekapAksi(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit){
		return txMonitorCustomRepository.selectAllLapRekapAksi(searchCriteria, orderBy, orderType, offset, limit);
	}

	public List<Object[]> selectAllLapRekapAksiBulanan(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit){
		return txMonitorCustomRepository.selectAllLapRekapAksiBulanan(searchCriteria, orderBy, orderType, offset, limit);
	}

	public List<Object[]> selectAllLapRekapAksiTahunan(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit){
		return txMonitorCustomRepository.selectAllLapRekapAksiTahuan(searchCriteria, orderBy, orderType, offset, limit);
	}

	public long countWfProcessAction(String id){
		return txMonitorRepository.countWfProcessAction(id);
	}

	public long countWfProcess(String id){
		return txMonitorRepository.countWfProcess(id);
	}

	public long countLapRekapStatusDanAksiMingguan(List<KeyValue> searchCriteria){
		return txMonitorCustomRepository.countLapRekapStatusDanAksiMingguan(searchCriteria);
	}

	public List<TxMonitor> selectAllLapRekapStatusDanAksiMingguan(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit){
		return txMonitorCustomRepository.selectAllLapRekapStatusDanAksiMingguan(searchCriteria, orderBy, orderType, offset, limit);
	}

	@Transactional
	public void remove(String id, String docid, String no, String monitorId){
		List<TxDocumentParams> txDocumentParamsList = txDocumentParamRepository.findTxDocumentParamsByApplicationNoAndDocumentId(no, docid+"-"+monitorId);
		txDocumentParamRepository.delete(txDocumentParamsList);
		txMonitorRepository.delete(id);
	}

	@Transactional
	public void approveMonitor(String idMonitor, Timestamp date) {
		TxMonitor txMonitor = txMonitorRepository.findOne(idMonitor);
		//System.out.println(txMonitor.getTxTmGeneral().getId());
		MUser user = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		txMonitor.setApproved(true);
		txMonitor.setApprovedDate(date);
//		txMonitor.setSendDate(date);
		txMonitor.setApprovedBy(user);
		txMonitorRepository.save(txMonitor);
	}

	public List<TxMonitor> findByTxTmGeneral(TxTmGeneral tx){
		return txMonitorRepository.findTxMonitorByTxTmGeneralOrderByCreatedDateDesc(tx);
	}

	public List<TxMonitor> findByIdGeneral(String generalId){
		return txMonitorCustomRepository.findByTxTmGeneralId(generalId);
	}

	public List<TxMonitor> findByIdGeneral2(String generalId){
		return txMonitorCustomRepository.findByTxTmGeneralId2(generalId);
	}

	public List<TxMonitor> findAllTxMonitorByTxTmGeneralId(String[] generalIdList){
		return txMonitorRepository.findAllTxMonitorByTxTmGeneralId(generalIdList);
	}

	public TxMonitor findTxMonitorKBM03(String appId){
		return txMonitorRepository.findTxMonitorKBM03(appId);
	}

	public GenericSearchWrapper<TxMonitor> searchProsesPermohonanMonitor(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
		GenericSearchWrapper<TxMonitor> searchResult = new GenericSearchWrapper<>();
		searchResult.setCount(txMonitorCustomRepository.countProsesPermohonanMonitor(searchCriteria)); 
		if (searchResult.getCount() > 0) {
			searchResult.setList(txMonitorCustomRepository.selectAllProsesPermohonanMonitor(searchCriteria, orderBy, orderType, offset, limit));
		} 
		return searchResult;
	}
}
