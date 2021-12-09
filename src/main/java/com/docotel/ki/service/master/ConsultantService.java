package com.docotel.ki.service.master;

import com.docotel.ki.model.master.MRepresentative;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.master.MConsultantCustomRepository;
import com.docotel.ki.repository.master.MConsultantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConsultantService {
    @Autowired
    MConsultantRepository mConsultantRepository;

    @Autowired
    MConsultantCustomRepository mConsultantCustomRepository;

    public List<MRepresentative> findAll() {
        return mConsultantRepository.findAll();
    }

    public MRepresentative findReprsById(String id) {
        return mConsultantRepository.findOne(id);
    }
    
    public MRepresentative findReprsByName(String name) {
        return mConsultantCustomRepository.selectOne("name", name, true);
    }
    
    public MRepresentative findReprsByNo(String no) {
        return mConsultantCustomRepository.selectOne("no", no, true);
    }

    public GenericSearchWrapper<MRepresentative> searchGeneral(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        GenericSearchWrapper<MRepresentative> searchResult = new GenericSearchWrapper<MRepresentative>();
        searchResult.setCount(mConsultantCustomRepository.count(searchCriteria));
        if (searchResult.getCount() > 0) {
            searchResult.setList(mConsultantCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
        }
        return searchResult;
    }

    public void insert(MRepresentative data){
        mConsultantRepository.save(data);
    }

    @Transactional
    public void update(MRepresentative dataUpdate){
    	mConsultantCustomRepository.saveOrUpdate(dataUpdate);
    }

    public MRepresentative selectOneReceptionByNo(String no) {
        return mConsultantCustomRepository.selectOne("JOIN FETCH c.mCountry mCtry JOIN FETCH c.mProvince mProv " +
                "JOIN FETCH c.mCity mCty JOIN FETCH c.createdBy cb", "no", no, true);
    }

}
