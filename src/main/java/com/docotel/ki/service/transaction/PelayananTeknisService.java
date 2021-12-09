package com.docotel.ki.service.transaction;

import java.sql.Timestamp;
import java.util.List;

import com.docotel.ki.model.master.MRoleSubstantif;
import com.docotel.ki.model.master.MRoleSubstantifDetail;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.master.MWorkflowProcess;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.enumeration.GroupDetailStatusEnum;
import com.docotel.ki.enumeration.StatusEnum;
import com.docotel.ki.repository.transaction.TxMonitorRepository;
import com.docotel.ki.repository.transaction.TxServDistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.docotel.ki.model.transaction.TxGroup;
import com.docotel.ki.model.transaction.TxGroupDetail;
import com.docotel.ki.model.transaction.TxMonitor;
import com.docotel.ki.model.transaction.TxServDist;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.repository.custom.master.MRoleSubstantifDetailCustomRepository;
import com.docotel.ki.repository.custom.master.MWorkflowProcessCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxGroupDetailCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxServDistCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmGeneralCustomRepository;
import com.docotel.ki.repository.master.MRoleSubstantifDetailRepository;
import com.docotel.ki.repository.master.MRoleSubstantifRepository;

@Service
public class PelayananTeknisService {
	@Autowired private MRoleSubstantifRepository mRoleSubstantifRepository;
	@Autowired private MRoleSubstantifDetailRepository mRoleSubstantifDetailRepository;
	@Autowired private TxServDistRepository txServDistRepository;
	@Autowired private TxMonitorRepository txMonitorRepository;
	
	@Autowired private MRoleSubstantifDetailCustomRepository mRoleSubstantifDetailCustomRepository;
	@Autowired private MWorkflowProcessCustomRepository mWorkflowProcessCustomRepository;
	@Autowired private TxServDistCustomRepository txServDistCustomRepository;
	@Autowired private TxTmGeneralCustomRepository txTmGeneralCustomRepository;
	@Autowired private TxGroupDetailCustomRepository txGroupDetailCustomRepository;
	
	@Autowired private GrupPermohonanService groupService;
	
	public GenericSearchWrapper<TxServDist> searchDistribution(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
		GenericSearchWrapper<TxServDist> searchResult = new GenericSearchWrapper<TxServDist>();
		searchResult.setCount(txServDistCustomRepository.countAll(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(txServDistCustomRepository.selectAll(
					"JOIN FETCH c.txGroup tg " +
					"JOIN FETCH c.mRoleSubstantifDetail rsd " + 
					"JOIN FETCH rsd.mRoleSubstantif rs " + 
					"LEFT JOIN FETCH rsd.mEmployee ", 
					searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}
	
	public GenericSearchWrapper<TxGroupDetail> searchGroupDetail(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<TxGroupDetail> searchResult = new GenericSearchWrapper<TxGroupDetail>();
		searchResult.setCount(txGroupDetailCustomRepository.count(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(txGroupDetailCustomRepository.selectAll(
					"JOIN FETCH c.txGroup tg " +
					"JOIN FETCH c.txTmGeneral ttg " +
					"LEFT JOIN FETCH ttg.txTmBrand ttb " +
					"LEFT JOIN FETCH ttg.txTmClassList ttc " +
					"LEFT JOIN FETCH ttc.mClass mc " +
					"LEFT JOIN FETCH ttg.txTmOwner tto " +
					"LEFT JOIN FETCH c.mUser1 mu1 " +
					"LEFT JOIN FETCH mu1.mEmployee me1 " +
					"LEFT JOIN FETCH c.mUser2 mu2 " +
					"LEFT JOIN FETCH mu1.mEmployee me2 ",
					searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}
	
	public List<MRoleSubstantif> findmRoleSubstantif(){
		return mRoleSubstantifRepository.findAll();
	}
	
	public List<MRoleSubstantifDetail> selectmRoleSubstantifDetail(){
		return mRoleSubstantifDetailCustomRepository.selectAll(
				"LEFT JOIN FETCH c.txServDistList tsd " +
				"JOIN FETCH c.mEmployee me " +
				"JOIN FETCH me.userId mu " +
				"JOIN FETCH c.mRoleSubstantif ms", null, null, true, "mRoleSubstantif.name, c.mEmployee.employeeName", "ASC", null, null);
	}
	
	public MRoleSubstantifDetail selectOnemRoleSubstantifDetail(String value){
		return mRoleSubstantifDetailCustomRepository.selectOne(
				"LEFT JOIN FETCH c.txServDistList tsd " +
				"JOIN FETCH c.mEmployee me " +
				"JOIN FETCH me.userId mu " +
				"JOIN FETCH c.mRoleSubstantif ms", "id", value, true);
	}
	
	@Transactional
	public void insertDistribution(TxServDist txServDist) {
		TxGroup txGroup = groupService.selectOneGroupById(txServDist.getTxGroup().getId());
		for(TxGroupDetail txGroupDetail : txGroup.getTxGroupDetailList()) {
			MRoleSubstantifDetail mRoleSubstantifDetail = mRoleSubstantifDetailRepository.findOne(txServDist.getmRoleSubstantifDetail().getId());
			
			txGroupDetail.setmUser1(mRoleSubstantifDetail.getmEmployee().getUserId());
			txGroupDetail.setmUserCurrent(mRoleSubstantifDetail.getmEmployee().getUserId());
			txGroupDetail.setStatus(GroupDetailStatusEnum.P1.name());
			txGroupDetailCustomRepository.saveOrUpdate(txGroupDetail);
			
			TxTmGeneral txTmGeneral = txGroupDetail.getTxTmGeneral();
			txTmGeneral.setmStatus(StatusEnum.TM_DISTRIBUSI_DOKUMEN.value());
			txTmGeneralCustomRepository.saveOrUpdate(txTmGeneral);
			
			MWorkflowProcess mWorkflowProcess = mWorkflowProcessCustomRepository.selectOne("status.id", txTmGeneral.getmStatus().getId(), true);
			
			TxMonitor txMonitor = new TxMonitor();
			txMonitor.setTxTmGeneral(txTmGeneral);
			txMonitor.setCreatedDate(new Timestamp(System.currentTimeMillis()));
			txMonitor.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
			txMonitor.setmWorkflowProcess(mWorkflowProcess);
			txMonitor.setNotes("Nomor Grup: "+txGroup.getNo() +"; Distribusi ke Pemeriksa: "+mRoleSubstantifDetail.getmEmployee().getEmployeeName()+"; "+ mRoleSubstantifDetail.getmRoleSubstantif().getName());
			txMonitorRepository.save(txMonitor);
		}
		
/*		txServDist.setmStatus(StatusEnum.TM_DISTRIBUSI_DOKUMEN.value());*/
		txServDistCustomRepository.saveOrUpdate(txServDist);
	}

	public TxServDist findOne(String s) {
		return txServDistRepository.findOne(s);
	}
	
	public TxServDist selectOne(String s) {
		return txServDistCustomRepository.selectOne("id", s);
	}
	
	
	public List<TxServDist> selectExpedisi(List<KeyValue> searchCriteria) {
		return txServDistCustomRepository.selectExpedisi(searchCriteria, null, null, null, null);
	}

	public Integer countTxSubsCheckForGroup(String distId) {
		return txServDistCustomRepository.countTxSubsCheckForGroupDistId(distId);
	}
	

	@Transactional
	public void saveOrUpdate(TxServDist mBrandType) {
		txServDistCustomRepository.saveOrUpdate(mBrandType);
	}
	
	public long countTxGroupDetail(List<KeyValue> searchCriteria) {
		return txGroupDetailCustomRepository.count(searchCriteria);
	}
}
