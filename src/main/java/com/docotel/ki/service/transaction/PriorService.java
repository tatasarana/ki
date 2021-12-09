package com.docotel.ki.service.transaction;

import com.docotel.ki.model.master.MCountry;
import com.docotel.ki.model.transaction.TxTmPrior;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.service.master.CountryService;
import com.docotel.ki.repository.custom.transaction.TxTmPriorCustomRepository;
import com.docotel.ki.repository.transaction.TxTmPriorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// use trademarkservice
@Service
public class PriorService {
    @Autowired private TxTmPriorRepository txTmPriorRepository;
    @Autowired private TxTmPriorCustomRepository txTmPriorCustomRepository;
    @Autowired
    CountryService countryService;
    
    @Transactional
    public void doSaveOrUpdate(TxTmPrior t){
    	txTmPriorCustomRepository.saveOrUpdate(t);
    	
    	StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + t.getCurrentId());
        sb.append(", Application No: " + t.getTxTmGeneral().getApplicationNo());
        sb.append(", Prioritas Nomor: " +  t.getNo());
        MCountry mCountry = countryService.findCountryById(t.getmCountry().getId());
        sb.append(", Negara: " + mCountry.getCode()  +'-'+ mCountry.getName());
        sb.append(", Tanggal Prioritas: " + t.getPriorDateTemp());
        
    }

    public void delete(String id) {
        txTmPriorRepository.delete(id);
    }

    public void deletePrioritas(List<TxTmPrior> listTxTmPrior, String application_id){
        txTmPriorCustomRepository.delete("txTmGeneral.id", application_id);
    }
    
    public void deletePrioritasByGeneral(String application_id){
        txTmPriorCustomRepository.delete("txTmGeneral.id", application_id);
    }

    @Transactional
    public void deletePrioritas(String id){
    	TxTmPrior t = new TxTmPrior();
        t = txTmPriorCustomRepository.selectOne("id", id);
        
        StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + t.getCurrentId());
        sb.append(", Application No: " + t.getTxTmGeneral().getApplicationNo());
        sb.append(", Prioritas Nomor: " +  t.getNo());
        MCountry mCountry = countryService.findCountryById(t.getmCountry().getId());
        sb.append(", Negara: " + mCountry.getCode()  +'-'+ mCountry.getName());
        sb.append(", Tanggal Prioritas: " + t.getPriorDateTemp());
        
        txTmPriorCustomRepository.delete(id);
    }

    public List<TxTmPrior> selectAllKriteria(String joinCriteria, List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit){
    	return txTmPriorCustomRepository.selectAllT(joinCriteria, searchCriteria, orderBy, orderType, offset, limit);
    }

    public TxTmPrior selectOneKriteria(String joinCriteria, List<KeyValue> searchCriteria, String orderBy, String orderType){
    	return txTmPriorCustomRepository.selectOneT(joinCriteria, searchCriteria, orderBy, orderType);
    }
    
    public GenericSearchWrapper<TxTmPrior> searchGeneralTest(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        GenericSearchWrapper<TxTmPrior> searchResult = new GenericSearchWrapper<TxTmPrior>();
        searchResult.setCount(txTmPriorCustomRepository.count(searchCriteria));
        searchResult.setList(txTmPriorCustomRepository.selectAll("JOIN FETCH c.txTmGeneral pc " +
        				  " JOIN FETCH c.mCountry mc " +
        				  " JOIN FETCH c.createdBy cb " , searchCriteria, orderBy, orderType, offset, limit));

        return searchResult;
    }

    public TxTmPrior selectOnebyId(String id) {
    	return txTmPriorCustomRepository.selectOne("LEFT JOIN FETCH c.txTmGeneral LEFT JOIN FETCH c.mCountry", "id", id, true);
    }
    
    public List<TxTmPrior> selectAllTxTmPrior(List<KeyValue> searchCriteria, String orderBy, String orderType) {
        return txTmPriorCustomRepository.selectAll("LEFT JOIN FETCH c.mCountry", searchCriteria, orderBy, orderType, null, null);
    }
}
