package com.docotel.ki.service.transaction;

import java.util.List;

import com.docotel.ki.model.master.MLookup;
import com.docotel.ki.model.master.MRoleSubstantifDetail;
import com.docotel.ki.model.master.MSection;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.master.MLookupCostumRepository;
import com.docotel.ki.repository.custom.transaction.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.docotel.ki.enumeration.GroupDetailStatusEnum;
import com.docotel.ki.repository.custom.master.MRoleSubstantifDetailCustomRepository;
import com.docotel.ki.repository.master.MUserRepository;
import com.docotel.ki.repository.transaction.TxGroupDetailRepository;

@Service
public class PemeriksaanSubstantifService {
	@Autowired
	private MUserRepository mUserRepository;
	@Autowired
	private TxGroupDetailRepository txGroupDetailRepository;
	@Autowired
	private TxSubsCheckCustomRepository txSubsCheckCustomRepository;
	@Autowired
	private TxGroupDetailCustomRepository txGroupDetailCustomRepository;
	@Autowired
	private TxTmGeneralCustomRepository txTmGeneralCustomRepository;
	@Autowired
	private TxTmBrandCustomRepository txTmBrandCustomRepository;
	@Autowired
	private MRoleSubstantifDetailCustomRepository mRoleSubstantifDetailCustomRepository;
	@Autowired
	private TxSimilarityResultCustomRepository txSimilarityResultCustomRepository;
	@Autowired
	private MLookupCostumRepository mLookupCostumRepository;
	
	public GenericSearchWrapper<TxGroupDetail> searchServDist(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<TxGroupDetail> searchResult = new GenericSearchWrapper<TxGroupDetail>();
		searchResult.setCount(txGroupDetailCustomRepository.countIncServDist(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(txGroupDetailCustomRepository.selectIncServDist(searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}	

	@Transactional
	public void insertSubsCheck(TxSubsCheck txSubsCheck) {
		txSubsCheckCustomRepository.saveOrUpdate(txSubsCheck);
	}

	public GenericSearchWrapper<TxTmBrand> searchPhoneticBrandName(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<TxTmBrand> searchResult = new GenericSearchWrapper<TxTmBrand>();
		searchResult.setCount(txTmBrandCustomRepository.countPhoneticCheck(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(txTmBrandCustomRepository.selectPhoneticData(
					searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}
	
	public GenericSearchWrapper<TxSubsCheck> searchSubsCheck(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<TxSubsCheck> searchResult = new GenericSearchWrapper<TxSubsCheck>();
		searchResult.setCount(txSubsCheckCustomRepository.count(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(txSubsCheckCustomRepository.selectAll(
					"LEFT JOIN FETCH c.txTmGeneral LEFT JOIN FETCH c.mClass LEFT JOIN FETCH c.createdBy",
					searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	} 
	 
	public GenericSearchWrapper<TxTmGeneral> searchSimilarityResult(String appId, List<KeyValue> searchCriteria, String[] exclude, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<TxTmGeneral> searchResult = new GenericSearchWrapper<TxTmGeneral>();
		searchResult.setCount(txSimilarityResultCustomRepository.countSimilarityResult(searchCriteria, exclude, false));
		if (searchResult.getCount() > 0) {
			searchResult.setList(txSimilarityResultCustomRepository.selectAllSimilarityResult(searchCriteria, exclude, orderBy, orderType, offset, limit, false));
			if (appId != null) {
				List<TxSimilarityResult> similarityResultList = txSimilarityResultCustomRepository.selectAll("JOIN FETCH c.similarTxTmGeneral d","originTxTmGeneral.id", appId, true, null, null);
				if (similarityResultList != null && similarityResultList.size() > 0) {
					List<TxTmGeneral> searchResultList = searchResult.getList();
					for (TxTmGeneral txTmGeneral : searchResultList) {
						for (TxSimilarityResult txSimilarityResult : similarityResultList) {
							if (txTmGeneral.getCurrentId().equalsIgnoreCase(txSimilarityResult.getSimilarTxTmGeneral().getCurrentId())) {
								txTmGeneral.setSimilar(true);
							}
						}
					}
				}
			}
		}
		return searchResult;
	}

	public GenericSearchWrapper<TxTmGeneral> searchPhoneticResult(String appId, List<KeyValue> searchCriteria, String[] exclude, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<TxTmGeneral> searchResult = new GenericSearchWrapper<TxTmGeneral>();
		searchResult.setCount(txSimilarityResultCustomRepository.countSimilarityResult(searchCriteria, exclude, true));
		if (searchResult.getCount() > 0) {
			searchResult.setList(txSimilarityResultCustomRepository.selectAllSimilarityResult(searchCriteria, exclude, orderBy, orderType, offset, limit, true));
			if (appId != null) {
				List<TxSimilarityResult> similarityResultList = txSimilarityResultCustomRepository.selectAll("JOIN FETCH c.similarTxTmGeneral d","originTxTmGeneral.id", appId, true, null, null);
				if (similarityResultList != null && similarityResultList.size() > 0) {
					List<TxTmGeneral> searchResultList = searchResult.getList();
					for (TxTmGeneral txTmGeneral : searchResultList) {
						for (TxSimilarityResult txSimilarityResult : similarityResultList) {
							if (txTmGeneral.getCurrentId().equalsIgnoreCase(txSimilarityResult.getSimilarTxTmGeneral().getCurrentId())) {
								txTmGeneral.setSimilar(true);
							}
						}
					}
				}
			}
		}
		return searchResult;
	}
	
	public List<MRoleSubstantifDetail> selectmRoleSubstantifDetail(){
		return mRoleSubstantifDetailCustomRepository.selectAll(
				"JOIN FETCH c.mEmployee me " +
				"JOIN FETCH me.userId mu " +
				"JOIN FETCH c.mRoleSubstantif ms", null, null, true, "mRoleSubstantif.name, c.mEmployee.employeeName", "ASC", null, null);
	}
	
	public MRoleSubstantifDetail getRoleSubstantifDetailByUsername(String name) { 
		return  mRoleSubstantifDetailCustomRepository.selectOne(
				"LEFT JOIN FETCH c.mEmployee me " +
				"LEFT JOIN FETCH me.userId mu " +
				"LEFT JOIN FETCH c.mRoleSubstantif mrs", "mEmployee.userId.username", name, true);
	}
	
	public TxGroupDetail selectOneTxGroupDetail(String by, String value) { 
		return  txGroupDetailCustomRepository.selectOne(
				"JOIN FETCH c.txGroup tg " +
				"JOIN FETCH tg.txServDist tsd "+
				"JOIN FETCH tsd.mRoleSubstantifDetail mrsd "+
				"JOIN FETCH mrsd.mEmployee me "+
				"JOIN FETCH me.userId mu "+
				"JOIN FETCH mrsd.mRoleSubstantif mrs "+
				"JOIN FETCH c.txTmGeneral ttg "+
				"LEFT JOIN FETCH ttg.txTmBrand ttb "+
				"LEFT JOIN FETCH ttg.txTmOwner tto "+
				"LEFT JOIN FETCH ttg.mStatus ms " +
				"LEFT JOIN FETCH c.mUser1 mu1 " +
				"LEFT JOIN FETCH c.mUser2 mu2", by, value, true);
	}

	public TxTmGeneral selectOneTxTmGeneral(String by, String value){
		return txTmGeneralCustomRepository.selectOne(by, value, true);
	}

	public MLookup selectOneMLookup(String by, String value) {
		return mLookupCostumRepository.selectOne(by, value, true);
	}
	public TxSubsCheck selectOneTxSubsCheck(String by, String value){
		return txSubsCheckCustomRepository.selectOne(by, value, true);
	}

	public boolean checkAppIdOnSimilarityResult(String by, Object value){
		if (txSimilarityResultCustomRepository.count(by, value, true) > 0) {
			return true;
		}
		return false;
		//return txSimilarityResultCustomRepository.selectOne(by, value, true);
	}

	public boolean checkAppIdOnSimilarityResult(List<KeyValue> searchCriteria) {
		if (txSimilarityResultCustomRepository.count(searchCriteria) > 0) {
			return true;
		}
		return false;
	}

	@Transactional
	public void deleteSubsCheck(String idSubsCheck) {
		txSubsCheckCustomRepository.delete(idSubsCheck);
	}
	
	@Transactional
	public void doRelease(String id) {
		TxGroupDetail txGroupDetail = txGroupDetailRepository.findOne(id);
		
		TxTmGeneral txTmGeneral = txGroupDetail.getTxTmGeneral();
		txTmGeneral.setGroupDist(false);
		txTmGeneralCustomRepository.saveOrUpdate(txTmGeneral);
		
		txGroupDetail.setStatus(GroupDetailStatusEnum.RELEASE.name());
		txGroupDetailCustomRepository.saveOrUpdate(txGroupDetail);
		
		TxSubsCheck txSubsCheck = txSubsCheckCustomRepository.selectOne("txTmGeneral.id", txTmGeneral.getId(), true);
		if (txSubsCheck != null) {
			txSubsCheckCustomRepository.delete(txSubsCheck.getId());
		}
	}
	
	@Transactional
	public void doReassign(String id, String pemeriksa) {
		TxGroupDetail txGroupDetail = txGroupDetailRepository.findOne(id);
		MUser mUser = mUserRepository.findOne(pemeriksa);
		
		if (txGroupDetail.getStatus().equals(GroupDetailStatusEnum.P1.name())) {
			txGroupDetail.setmUser1(mUser);
		} else {
			txGroupDetail.setmUser2(mUser);
		}
		txGroupDetail.setmUserCurrent(mUser);
		txGroupDetailCustomRepository.saveOrUpdate(txGroupDetail);
	}

	@Transactional
	public void doReassignP1(String id) {
		TxGroupDetail txGroupDetail = txGroupDetailRepository.findOne(id);
		MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		//MUser mUser = mUserRepository.findOne(pemeriksa);

		txGroupDetail.setStatus("P1");
		txGroupDetail.setmUser1(mUser);
		txGroupDetail.setmUserCurrent(mUser);
		txGroupDetail.setmUser2(null);

		txGroupDetailCustomRepository.saveOrUpdate(txGroupDetail);
	}

	@Transactional
	public void doValidasi(String id, String pemeriksa) {
		TxGroupDetail txGroupDetail = txGroupDetailRepository.findOne(id);
		MUser mUser = mUserRepository.findOne(pemeriksa);
		
		txGroupDetail.setmUser2(mUser);
		txGroupDetail.setmUserCurrent(mUser);
		txGroupDetail.setStatus(GroupDetailStatusEnum.P2.name());
		txGroupDetailCustomRepository.saveOrUpdate(txGroupDetail);
	}
	
	@Transactional
	public void doSelesai(String id) {
		TxGroupDetail txGroupDetail = txGroupDetailRepository.findOne(id);
		txGroupDetail.setStatus(GroupDetailStatusEnum.DONE.name());
		txGroupDetailCustomRepository.saveOrUpdate(txGroupDetail);
	}
	
	@Transactional
	public void insertSimilarityResult(TxSimilarityResult form) {
		txSimilarityResultCustomRepository.saveOrUpdate(form);
	}

	@Transactional
	public void deleteSimilarityResult(TxSimilarityResult form) {
		txSimilarityResultCustomRepository.delete(form);
	}
}
