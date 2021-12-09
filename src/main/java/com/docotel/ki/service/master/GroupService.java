package com.docotel.ki.service.master;

import java.util.List;

import com.docotel.ki.model.transaction.TxGroup;
import com.docotel.ki.model.transaction.TxGroupDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.docotel.ki.repository.transaction.TxGroupDetailRepository;
import com.docotel.ki.repository.transaction.TxGroupRepository;

@Service
public class GroupService {
    @Autowired
    TxGroupRepository txGroupRepository;
    @Autowired
    TxGroupDetailRepository txGroupDetailRepository;
       
    public List<TxGroup> findAll() {
        return txGroupRepository.findAll();
    }

    public TxGroup GetGroupById(String s) {
        return txGroupRepository.findOne(s);
    }
    
    public TxGroupDetail findTxGroupDetailByTxTmGeneral(String idGeneral, String groupType) {
    	return txGroupDetailRepository.findTxGroupDetailByTxTmGeneral(idGeneral, groupType);
    }
    
}
