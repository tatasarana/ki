package com.docotel.ki.service.master;

import com.docotel.ki.model.master.MFileSequence;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.master.MFileSeqCustomRepository;
import com.docotel.ki.repository.master.MFileSeqRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FileSeqService {
    @Autowired MFileSeqRepository mFileSeqRepository;
    @Autowired MFileSeqCustomRepository mFileSeqCustomRepository;

    public MFileSequence findOne(String s) {
        return mFileSeqRepository.findOne(s);
    } 

    public List<MFileSequence> findAll() {
        return mFileSeqRepository.findAll();
    }
    
    public List<MFileSequence> findByStatusFlagTrue() {
    	return mFileSeqRepository.findByStatusFlagTrue();
    }
    
    public MFileSequence findByCode(String code){
        return mFileSeqCustomRepository.selectOne("code", code, false);
    }
    
    public List<MFileSequence> findAllByActiveStatus() {
        return mFileSeqCustomRepository.selectAll("statusFlag", true, true, null, null);
    }

    public List<MFileSequence> findAllByKanwilStatus() {
    	return mFileSeqCustomRepository.selectAll("kanwilFlag", true, true, null, null);
    }
    
    public GenericSearchWrapper<MFileSequence> searchGeneral(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        GenericSearchWrapper<MFileSequence> searchResult = new GenericSearchWrapper<MFileSequence>();
        searchResult.setCount(mFileSeqCustomRepository.count(searchCriteria));
        if (searchResult.getCount() > 0) {
            searchResult.setList(mFileSeqCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
        }
        return searchResult;
    }

    public void insert(MFileSequence data){
        mFileSeqRepository.save(data);
    }

    @Transactional
    public void saveOrUpdate(MFileSequence mFileSequence) {
        mFileSeqCustomRepository.saveOrUpdate(mFileSequence);
    }
}
