package com.docotel.ki.service.master;

import com.docotel.ki.model.master.MLookup;
import com.docotel.ki.repository.custom.master.MLookupCostumRepository;
import com.docotel.ki.repository.master.MLookupRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LookupService {
    @Autowired
    MLookupCostumRepository mLookupCostumRepository;
    @Autowired
    MLookupRepository mLookupRepository;
    
    public List<MLookup> selectAllbyGroup(Object value) {
        return mLookupCostumRepository.selectAll("groups", value, true, 0, 1000);
    }
    
    public MLookup selectOneById(String id) {
    	return mLookupRepository.findOne(id);
    }

    public List<MLookup> selectAllByOrderToNumber(String jenisPemohon) {
        return mLookupRepository.selectAllByOrderToNumber(jenisPemohon);
    }

    public MLookup findByCodeGroups(String code, String groups){
        return mLookupRepository.findByCodeGroups(code, groups);
    }
}
