package com.docotel.ki.service.master;

import com.docotel.ki.model.master.MDocType;
import com.docotel.ki.model.master.MDocTypeDetail;
import com.docotel.ki.model.master.MFileType;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.master.MDocTypeCustomRepository;
import com.docotel.ki.repository.custom.master.MDocTypeDetailCustomRepository;
import com.docotel.ki.repository.custom.master.MFileTypeCustomRepository;
import com.docotel.ki.repository.master.MDocTypeDetailRepository;
import com.docotel.ki.repository.master.MDocTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DocTypeDetailService {
	
    @Autowired MDocTypeRepository mDocTypeRepository;
    @Autowired MDocTypeCustomRepository mDocTypeCustomRepository;
    @Autowired MDocTypeDetailCustomRepository mDocTypeDetailCustomRepository;
    @Autowired MDocTypeDetailRepository mDocTypeDetailRepository;
    @Autowired MFileTypeCustomRepository mFileTypeCustomRepository;
    
    public List<MDocTypeDetail> findAll() {
        return mDocTypeDetailRepository.findAll();
    }
    
    public List<MDocTypeDetail> selectAllByDocType (String value) {
    	return mDocTypeDetailCustomRepository.selectAll("LEFT JOIN FETCH c.mDocType md LEFT JOIN FETCH c.mFileType mf", "mDocType.id", value, true,null,null );
    }

    public List<MDocType> findByStatusFlagTrue() {
        return mDocTypeRepository.findByStatusFlagTrue();
    }

    public MDocTypeDetail findOne(String s) {
        return mDocTypeDetailRepository.findOne(s);
    }

    public List<MDocTypeDetail> selectAllDetail(List<KeyValue> searchCriteria) {
    	return mDocTypeDetailCustomRepository.selectAll(
    			"JOIN FETCH c.mDocType mdt " +
    			"JOIN FETCH c.mFileType mft ", searchCriteria, "mDocType.name", "ASC", null, null);
    }
    
    public void insert(MDocTypeDetail data){
        mDocTypeDetailRepository.save(data);
    }
    
    public void save(MDocType mDocType, String[] docTypeDetail) {
    	List<MDocTypeDetail> mDocTypeDetailList = selectAllByDocType(mDocType.getId());
    	mDocTypeDetailRepository.delete(mDocTypeDetailList);
    	if (docTypeDetail != null) {
    		for (String doc : docTypeDetail) {
    			MFileType mFileType = mFileTypeCustomRepository.selectOne("id", doc, true);
    			if (mFileType != null) {
    				MDocTypeDetail mDocTypeDetail = new MDocTypeDetail();
    				mDocTypeDetail.setmDocType(mDocType);
    				mDocTypeDetail.setmFileType(mFileType);
    				mDocTypeDetailRepository.save(mDocTypeDetail);
    			}
    		}
    	}
    }

    @Transactional
    public void saveOrUpdate(MDocTypeDetail mDocTypeDetail) {
        mDocTypeDetailCustomRepository.saveOrUpdate(mDocTypeDetail);
    }

    public GenericSearchWrapper<MDocTypeDetail> searchGeneral(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        GenericSearchWrapper<MDocTypeDetail> searchResult = new GenericSearchWrapper<MDocTypeDetail>();
        searchResult.setCount(mDocTypeDetailCustomRepository.count(searchCriteria));
        if (searchResult.getCount() > 0) {
            searchResult.setList(mDocTypeDetailCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
        }
        return searchResult;
    }
}
