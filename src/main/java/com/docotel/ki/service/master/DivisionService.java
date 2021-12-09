package com.docotel.ki.service.master;

import com.docotel.ki.model.master.MDivision;
import com.docotel.ki.repository.custom.master.MDivisionCustomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.docotel.ki.repository.master.MDivisionRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DivisionService {
	@Autowired MDivisionRepository mDivisionRepository;
	@Autowired MDivisionCustomRepository mDivisionCustomRepository;

	public MDivision findOneMDivision(String id) {
        return mDivisionRepository.findOne(id);
    }
	
	 public MDivision selectOneMDivision(String by, String value){
	        return mDivisionCustomRepository.selectOne(by, value, false);
	 }

    public List<MDivision> findAll() {
        return mDivisionRepository.findAll();
    }
    
    
    @Transactional
    public void saveOrUpdate(MDivision mBrandType) {
        mDivisionCustomRepository.saveOrUpdate(mBrandType);
    }

}
