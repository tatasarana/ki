package com.docotel.ki.service.master;

import com.docotel.ki.model.master.MDepartment;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.master.MDepartementCustomRepository;
import com.docotel.ki.repository.master.MDepartementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartementService {
    @Autowired MDepartementRepository mDepartementRepository;
    @Autowired MDepartementCustomRepository mDepartementCustomRepository;

    public List<MDepartment> findAll() {
        return mDepartementRepository.findAll();
    }
    
    public List<MDepartment> selectAllMDepartment() {
        return mDepartementRepository.selectAllMDepartment();
    }
    
    public List<MDepartment> findAllByActiveStatus() {
        return mDepartementCustomRepository.selectAll("statusFlag", true, true, null, null);
    }

    public void insert(MDepartment data){
        mDepartementRepository.save(data);
    }

    public MDepartment findOne(String s) {
        return mDepartementRepository.findOne(s);
    }
    
    public MDepartment selectOne(String by, String value) {
        return mDepartementCustomRepository.selectOne(by, value);
    }

    @Transactional
    public void saveOrUpdate(MDepartment mBrandType) {
        mDepartementCustomRepository.saveOrUpdate(mBrandType);
    }

    public GenericSearchWrapper<MDepartment> searchGeneral(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        GenericSearchWrapper<MDepartment> searchResult = new GenericSearchWrapper<MDepartment>();
        searchResult.setCount(mDepartementCustomRepository.count(searchCriteria));
        if (searchResult.getCount() > 0) {
            searchResult.setList(mDepartementCustomRepository.selectAll("JOIN FETCH c.mDivision md JOIN FETCH c.createdBy cb LEFT JOIN FETCH c.mSectionList ms",
            															searchCriteria, orderBy, orderType, offset, limit));
        }
        return searchResult;
    }
    
    public long countAll(List<KeyValue> searchCriteria) {
        return mDepartementCustomRepository.countDepartemen(searchCriteria);
    }

    public List<MDepartment> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
        return mDepartementCustomRepository.selectAllDepartemen(searchCriteria, orderBy, orderType, offset, limit);
    }
}
