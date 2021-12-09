package com.docotel.ki.service.master;

import com.docotel.ki.model.master.MDocType;
import com.docotel.ki.model.master.MDocTypeDetail;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.master.MDocTypeCustomRepository;
import com.docotel.ki.repository.custom.master.MDocTypeDetailCustomRepository;
import com.docotel.ki.repository.master.MDocTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DocTypeService {
    @Autowired
    MDocTypeRepository mDocTypeRepository;

    @Autowired
    MDocTypeCustomRepository mDocTypeCustomRepository;

    @Autowired
    MDocTypeDetailCustomRepository mDocTypeDetailCustomRepository;
    
    public List<MDocType> findAll() {
        return mDocTypeRepository.findAll();
    }

    public List<MDocType> findByStatusFlagTrue() {
        return mDocTypeRepository.findByStatusFlagTrue();
    }
    
    public MDocType getFileTypeByDocTypeId(String id) {
		return mDocTypeRepository.findOne(id);
	}

    public MDocType findOne(String s) {
        return mDocTypeRepository.findOne(s);
    }
    
    public MDocType getDocTypeById(String id) {
    	return mDocTypeCustomRepository.selectOne("id", id);
    }

    public List<MDocTypeDetail> selectAllDetail(List<KeyValue> searchCriteria) {
    	return mDocTypeDetailCustomRepository.selectAll(
    			"JOIN FETCH c.mDocType mdt " +
    			"JOIN FETCH c.mFileType mft ", searchCriteria, "mDocType.name", "ASC", null, null);
    }
    
    public void insert(MDocType data){
        mDocTypeRepository.save(data);
    }

    @Transactional
    public void saveOrUpdate(MDocType mDocType) {
        mDocTypeCustomRepository.saveOrUpdate(mDocType);
    }

    public GenericSearchWrapper<MDocType> searchGeneral(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        GenericSearchWrapper<MDocType> searchResult = new GenericSearchWrapper<MDocType>();
        searchResult.setCount(mDocTypeCustomRepository.count(searchCriteria));
        if (searchResult.getCount() > 0) {
            searchResult.setList(mDocTypeCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
        }
        return searchResult;
    }
}
