package com.docotel.ki.service.master;

import com.docotel.ki.model.master.MEmployee;
import com.docotel.ki.model.master.MRoleSubstantif;
import com.docotel.ki.model.master.MRoleSubstantifDetail;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.master.MRoleSubstantifDetailCustomRepository;
import com.docotel.ki.repository.custom.master.MSectionCustomRepository;
import com.docotel.ki.repository.custom.master.MUserCustomRepository;
import com.docotel.ki.repository.master.MRoleSubstantifDetailRepository;
import com.docotel.ki.repository.master.MRoleSubstantifRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.docotel.ki.repository.custom.master.MEmployeeCustomRepository;
import com.docotel.ki.repository.custom.master.MRoleSubstantifCustomRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoleSubstantifService {
	@Autowired
	MRoleSubstantifRepository mRoleSubstantifRepository;
	@Autowired
	MRoleSubstantifCustomRepository mRoleSubstantifCustomRepository;
	@Autowired
	MRoleSubstantifDetailRepository mRoleSubstantifDetailRepository;
	@Autowired
	MRoleSubstantifDetailCustomRepository mRoleSubstantifDetailCustomRepository;
	@Autowired
	MSectionCustomRepository mSectionCustomRepository;
	@Autowired
	MUserCustomRepository mUserCustomRepository;
	@Autowired
	MEmployeeCustomRepository mEmployeeCustomRepository;
	
	public List<MRoleSubstantif> findAllRoleSub() { return mRoleSubstantifRepository.findAll(); }

	public MRoleSubstantif findOneByIdRoleSub(String id) { return mRoleSubstantifRepository.findOne(id); }

	public List<MRoleSubstantifDetail> findAllRoleSubDetail() { return mRoleSubstantifDetailRepository.findAll(); }

	public MRoleSubstantifDetail findOneByIdRoleSubDetail(String id) { return mRoleSubstantifDetailRepository.findOne(id); }

	public GenericSearchWrapper<MRoleSubstantif> searchRoleSubstantif(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<MRoleSubstantif> searchResult = new GenericSearchWrapper<MRoleSubstantif>();
		searchResult.setCount(mRoleSubstantifCustomRepository.countRoleSubstantif(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(mRoleSubstantifCustomRepository.selectRoleSubstantif(searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}

	public GenericSearchWrapper<MRoleSubstantifDetail> searchGeneral(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<MRoleSubstantifDetail> searchResult = new GenericSearchWrapper<MRoleSubstantifDetail>();
		searchResult.setCount(mRoleSubstantifDetailCustomRepository.count(searchCriteria));
		if (searchResult.getCount() > 0) {			
			searchResult.setList(mRoleSubstantifDetailCustomRepository.selectAll("JOIN FETCH c.mRoleSubstantif mr "
					+ "JOIN FETCH c.mEmployee mu "
					+ "LEFT JOIN FETCH mu.mSection ms "
					+ "JOIN FETCH c.createdBy cb",searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}

	public MRoleSubstantif selectOne(String by, String value){
        return mRoleSubstantifCustomRepository.selectOne(by, value, true);
    }

    @Transactional
	public void insertRoleSubs(MRoleSubstantif data){
		mRoleSubstantifRepository.save(data);
	}

	@Transactional
	public void saveOrUpdateRoleSubs(MRoleSubstantif mRoleSubstantif) {
		mRoleSubstantifCustomRepository.saveOrUpdate(mRoleSubstantif);
	}

	public List<MRoleSubstantifDetail> selectAllByRoleSubs(String by, String value){
		return mRoleSubstantifDetailCustomRepository.selectAllByRoleSubs(by, value, 0, 500);
	}

	public void insertRoleSubsDetail(MRoleSubstantifDetail mRoleSubstantifDetail){
		mRoleSubstantifDetailRepository.save(mRoleSubstantifDetail);
	}

	public void insertSubDetail(MRoleSubstantifDetail dataRoleSubstantifDetail){
/*		dataRoleSubstantifDetail.setmEmployee(dataRoleSubstantifDetail.getmEmployee());
		dataRoleSubstantifDetail.setmRoleSubstantif(dataRoleSubstantifDetail.getmRoleSubstantif());*/
		mRoleSubstantifDetailRepository.save(dataRoleSubstantifDetail);
	}
	
	@Transactional
	public void deleteRoleSubsDetail(String id) {
		mRoleSubstantifDetailCustomRepository.delete(id);
	}

	public List<MUser> selectAllBySection(String target){
		return mUserCustomRepository.selectAll("mSection.id", target, true, null, null);
	}
	
	public List<MUser> selectAllUserNotSubstantif(String section) {
		return mUserCustomRepository.selectAllUserNotSubstantif(section);
	}
	
	public List<MEmployee> selectAllEmployeeNotSubstantif(String section) {
		return mEmployeeCustomRepository.selectAllEmployeeNotSubstantif(section);
	}
	
	public MRoleSubstantifDetail selectOneByIdRoleSubDetail(String id) { 
		return mRoleSubstantifDetailCustomRepository.selectOne("LEFT JOIN FETCH c.txServDistList", "id", id, true);
	}
	
	public void insert(MRoleSubstantifDetail data) {
		MRoleSubstantifDetail existing = mRoleSubstantifDetailCustomRepository.selectOne("name", data.getmRoleSubstantif().getCurrentId(), true);
		if (existing != null) {
			throw new DataIntegrityViolationException(HttpStatus.ALREADY_REPORTED.getReasonPhrase() + "-name");
			
		}
		existing = mRoleSubstantifDetailCustomRepository.selectOne("code", data.getmEmployee().getCurrentId(), true);
		if (existing != null) {
			throw new DataIntegrityViolationException(HttpStatus.ALREADY_REPORTED.getReasonPhrase() + "-code");
		}
		mRoleSubstantifDetailRepository.save(data);
	}
}
