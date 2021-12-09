package com.docotel.ki.service.master;

import com.docotel.ki.model.master.MProvince;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.master.MProvinceCustomRepository;
import com.docotel.ki.repository.master.MProvinceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProvinceService {
    @Autowired
    MProvinceRepository mProvinceRepository;
    @Autowired
    MProvinceCustomRepository mProvinceCustomRepository;

    public MProvince findOne(String s) {
        return mProvinceRepository.findOne(s);
    }

    public List<MProvince> findAll() {
        return mProvinceRepository.findAll();
    }
    
    public List<MProvince> findByStatusFlagTrue() {
        return mProvinceRepository.findByStatusFlagTrue();
    }
    
    public List<MProvince> findByStatusFlagTrueOrderByName() {
        return mProvinceRepository.findByStatusFlagTrueOrderByName();
    }

    public GenericSearchWrapper<MProvince> searchGeneral(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        GenericSearchWrapper<MProvince> searchResult = new GenericSearchWrapper<MProvince>();
        searchResult.setCount(mProvinceCustomRepository.count(searchCriteria));
        if (searchResult.getCount() > 0) {
            searchResult.setList(mProvinceCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
        }
        return searchResult;
    }

    public void insert(MProvince data) {
	    MProvince existing = mProvinceCustomRepository.selectOne("name", data.getName(), true);
	    if (existing != null) {
	    	throw new DataIntegrityViolationException(HttpStatus.ALREADY_REPORTED.getReasonPhrase() + "-name");
	    }
	    existing = mProvinceCustomRepository.selectOne("code", data.getCode(), true);
	    if (existing != null) {
	    	throw new DataIntegrityViolationException(HttpStatus.ALREADY_REPORTED.getReasonPhrase() + "-code");
	    }
        mProvinceRepository.save(data);
    }

    @Transactional
    public void saveOrUpdate(MProvince mProvince) {
	    MProvince existing = mProvinceCustomRepository.selectOne("name", mProvince.getName(), true);
	    if (existing != null && !existing.getId().equalsIgnoreCase(mProvince.getId())) {
		    throw new DataIntegrityViolationException(HttpStatus.ALREADY_REPORTED.getReasonPhrase() + "-name");
	    }
	    existing = mProvinceCustomRepository.selectOne("code", mProvince.getCode(), true);
	    if (existing != null && !existing.getId().equalsIgnoreCase(mProvince.getId())) {
		    throw new DataIntegrityViolationException(HttpStatus.ALREADY_REPORTED.getReasonPhrase() + "-code");
	    }
        mProvinceCustomRepository.saveOrUpdate(mProvince);
    }
	
	public long countAll(List<KeyValue> searchCriteria) {
        return mProvinceCustomRepository.countProvinsi(searchCriteria);
    }

    public List<MProvince> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
        return mProvinceCustomRepository.selectAllProvinsi(searchCriteria, orderBy, orderType, offset, limit);
    }
}
