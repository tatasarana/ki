package com.docotel.ki.service.transaction;

import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.model.transaction.TxTmOwner;
import com.docotel.ki.model.transaction.TxTmOwnerDetail;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.transaction.TxTmOwnerCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmOwnerDetailCustomRepository;
import com.docotel.ki.repository.transaction.TxTmOwnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OwnerService {
    @Autowired
    TxTmOwnerRepository txTmOwnerRepository;
    @Autowired
    TxTmOwnerCustomRepository txTmOwnerCustomRepository;
    @Autowired
    TxTmOwnerDetailCustomRepository txTmOwnerDetailCustomRepository;

    public List<TxTmOwner> findAll(){
        return txTmOwnerRepository.findAll();
    }
    
    public List<TxTmOwner> selectOneByIdGeneral(String generalId, boolean statusOwner){
        return txTmOwnerCustomRepository.selectTxTmOwnerByTxTmGeneral(generalId, statusOwner);
    }

    public GenericSearchWrapper<TxTmOwner> getAll(Integer offset, Integer limit) {
        GenericSearchWrapper<TxTmOwner> searchResult = new GenericSearchWrapper<TxTmOwner>();
        searchResult.setCount(txTmOwnerRepository.count());
        searchResult.setList(txTmOwnerCustomRepository.selectAll("INNER JOIN FETCH c.txTmGeneral tg" +
                "LEFT JOIN FETCH c.mCountry ci ", null,null,false, offset, limit));

        return searchResult;
    }

    public GenericSearchWrapper<TxTmOwner> searchGeneral(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        GenericSearchWrapper<TxTmOwner> searchResult = new GenericSearchWrapper<TxTmOwner>();
        searchResult.setCount(txTmOwnerCustomRepository.count(searchCriteria));
        searchResult.setList(txTmOwnerCustomRepository.selectAll("INNER JOIN FETCH c.txTmGeneral tg" +
                "LEFT JOIN FETCH c.mCountry ci ", searchCriteria,orderBy,orderType,offset,limit));

        return searchResult;
    }

    //add wsh start
    public TxTmOwner selectOne(String by, String value){
        return txTmOwnerCustomRepository.selectOne("LEFT JOIN FETCH c.mCountry ci " +
                        "LEFT JOIN FETCH c.mProvince p LEFT JOIN FETCH c.mCity ci "
                , by, value, true);
    }
    //add wsh finish

    public TxTmOwner selectActiveOne(TxTmGeneral txTmGeneral){
        return txTmOwnerRepository.findTxTmOwnerByTxTmGeneralAndStatus(txTmGeneral, true);
    }
    
    // For pasca online preview pemohon
    public TxTmOwner selectOneOwner(String by, String value){
        return txTmOwnerCustomRepository.selectOne("LEFT JOIN FETCH c.mCountry ci " 
                      + "LEFT JOIN FETCH c.mProvince p "
                      + "LEFT JOIN FETCH c.nationality na "
                      + "LEFT JOIN FETCH c.mCity ct "
                      + "LEFT JOIN FETCH c.postCity pt "
                      + "LEFT JOIN FETCH pt.mProvince pmp "
                      + "LEFT JOIN FETCH ct.mProvince pmc "
                      + "LEFT JOIN FETCH c.postCountry pc "
                      + "LEFT JOIN FETCH c.postProvince pp ", by, value, true);
    }
    
    public TxTmOwner selectOne(List<KeyValue> searchCriteria){
        return txTmOwnerCustomRepository.selectOne(
        		"LEFT JOIN FETCH c.mCountry co " +
        		"LEFT JOIN FETCH c.mProvince p " +
        		"LEFT JOIN FETCH c.mCity ci " + 
        		"LEFT JOIN FETCH c.postCountry pco " +
        		"LEFT JOIN FETCH c.postProvince pp " +
        		"LEFT JOIN FETCH c.postCity pci "
                , searchCriteria, null, null);
    }
    
    public List<TxTmOwnerDetail> selectAllDetail(List<KeyValue> searchCriteria, String orderBy, String orderType){
        return txTmOwnerDetailCustomRepository.selectAll(searchCriteria, orderBy, orderType, null, null);
    }
}
