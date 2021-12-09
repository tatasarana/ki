package com.docotel.ki.service.master;

import com.docotel.ki.model.master.MSection;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.master.MSectionCustomRepository;
import com.docotel.ki.repository.master.MSectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SectionService {
    @Autowired MSectionRepository mSectionRepository;
    @Autowired MSectionCustomRepository mSectionCustomRepository;

    public List<MSection> findAll() {
        return mSectionRepository.findAll();
    }

    public List<MSection> findAllByActiveStatus() {
        return mSectionCustomRepository.selectAll("statusFlag", true, true, null, null);
    }
    
    public void insert(MSection data){
        mSectionRepository.save(data);
    }

    public MSection findOne(String s) {
        return mSectionRepository.findOne(s);
    }
    
    public MSection selectOne(String by, String value) {
        return mSectionCustomRepository.selectOne(by, value);
    }

    @Transactional
    public void saveOrUpdate(MSection mBrandType) {
        mSectionCustomRepository.saveOrUpdate(mBrandType);
    }

    public GenericSearchWrapper<MSection> searchGeneral(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        GenericSearchWrapper<MSection> searchResult = new GenericSearchWrapper<MSection>();
        searchResult.setCount(mSectionCustomRepository.count(searchCriteria));
        if (searchResult.getCount() > 0) {
            searchResult.setList(mSectionCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
        }
        return searchResult;
    }

    public List<MSection> selectAllByDepartment(String by, String value){
        return mSectionCustomRepository.selectAllByDepartment(by, value, 0, 500);
    }
    
    public long countAll(List<KeyValue> searchCriteria) {
        return mSectionCustomRepository.countBagian(searchCriteria);
    }

    public List<MSection> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
        return mSectionCustomRepository.selectAllBagian(searchCriteria, orderBy, orderType, offset, limit);
    }

}
