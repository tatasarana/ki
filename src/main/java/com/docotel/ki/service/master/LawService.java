package com.docotel.ki.service.master;

import com.docotel.ki.model.master.MLaw;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.master.MLawCustomRepository;
import com.docotel.ki.repository.master.MLawRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LawService {
    @Autowired MLawRepository mLawRepository;
    @Autowired
    MLawCustomRepository MLawCustomRepository;

    public List<MLaw> findAll() {
        return mLawRepository.findAll();
    }

    public List<MLaw> findByStatusFlagTrue() {
        return mLawRepository.findByStatusFlagTrue();
    }

    public MLaw GetMlawById(String s) {
        return mLawRepository.findOne(s);
    }
    
    public MLaw selectOne(String by, String value) {
        return MLawCustomRepository.selectOne(by, value);
    }
    
   
    public GenericSearchWrapper<MLaw> searchGeneral(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        GenericSearchWrapper<MLaw> searchResult = new GenericSearchWrapper<MLaw>();
        searchResult.setCount(MLawCustomRepository.count(searchCriteria));
        if (searchResult.getCount() > 0) {
            searchResult.setList(MLawCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
        }
        return searchResult;
    }

    public void insert(MLaw mLaw){
        mLawRepository.save(mLaw);
    }

    @Transactional
    public void saveOrUpdate(MLaw mLaw) {
        MLawCustomRepository.saveOrUpdate(mLaw);
    }
	
	public long countAll(List<KeyValue> searchCriteria) {
        return MLawCustomRepository.countLaw(searchCriteria);
    }
	
	public MLaw selectOne(List<KeyValue> searchCriteria) {
        return MLawCustomRepository.selectOne(searchCriteria,null, null);
    }

    public List<MLaw> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
        return MLawCustomRepository.selectAllLaw(searchCriteria, orderBy, orderType, offset, limit);
    }
}
