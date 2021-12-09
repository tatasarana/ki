package com.docotel.ki.service.transaction;

import java.util.List;

import com.docotel.ki.model.master.MCity;
import com.docotel.ki.model.master.MCountry;
import com.docotel.ki.model.master.MProvince;
import com.docotel.ki.model.transaction.TxPostReception;
import com.docotel.ki.model.transaction.TxReception;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.transaction.TxTmGeneralCustomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.docotel.ki.repository.custom.master.MCityCustomRepository;
import com.docotel.ki.repository.custom.master.MCountryCostumRepository;
import com.docotel.ki.repository.custom.master.MProvinceCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxPostReceptionCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxReceptionCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmClassCustomRepository;

@Service
public class PermohonanOnlineService {
	@Autowired private MCityCustomRepository mCityCustomRepository;
	@Autowired private MCountryCostumRepository mCountryCostumRepository;
	@Autowired private MProvinceCustomRepository mProvinceCustomRepository;	
	@Autowired private TxTmClassCustomRepository txTmClassCustomRepository;
	@Autowired private TxPostReceptionCustomRepository txPostReceptionCustomRepository;
    @Autowired private TxReceptionCustomRepository txReceptionCustomRepository;
    @Autowired private TxTmGeneralCustomRepository txTmGeneralCustomRepository;

    public TxReception selectOneTxReception(String by, String value) {
    	return txReceptionCustomRepository.selectOne(by, value, true);
    }
    
    public TxPostReception selectOneTxPostReception(String by, String value) {
    	return txPostReceptionCustomRepository.selectOne(by, value, true);
    }
    
    public MCountry selectOneCountryByCode(String code) {
    	return mCountryCostumRepository.selectOne("code", code, true);
    }
    
    public MProvince selectOneProvinceByCode(String code) {
    	return mProvinceCustomRepository.selectOne("code", code, true);
    }
    
    public MCity selectOneCityByCode(String code) {
    	return mCityCustomRepository.selectOne("code", code, true);
    }
    
    
    public long countTxTmClass(List<KeyValue> searchCriteria) {
    	return txTmClassCustomRepository.count(searchCriteria);
    }

    public GenericSearchWrapper<TxTmGeneral> searchRefReception(List<KeyValue> searchCriteria, 
    		String orderBy, String orderType, int offset, int limit) {
        GenericSearchWrapper<TxTmGeneral> searchResult = new GenericSearchWrapper<TxTmGeneral>();
        String RegistrationNo = "";
        for (int i = 0; i < searchCriteria.size(); i++) {
            String searchBy = searchCriteria.get(i).getKey();
            Object value = searchCriteria.get(i).getValue();
            if (searchBy.equalsIgnoreCase("txRegistration.no")) {
                RegistrationNo = (String)value;
            }
        }

            if(!RegistrationNo.equalsIgnoreCase("")){
                String regNos = RegistrationNo;
                String processedStr = "";
                if(regNos.contains(",")){
                    String[] splits = regNos.split(",");
                    for(int j=0;j<splits.length;j++){
                        if(processedStr.equalsIgnoreCase("")){
                            processedStr+= splits[j];
                        }else{
                            processedStr+= "','"+splits[j];
                        }
                    }
                    processedStr = "'"+processedStr+"'";
                }else{
                    processedStr = "'"+regNos+"'";
                }
                List<TxTmGeneral> gens = txTmGeneralCustomRepository.findTxTmGeneralByRegNo(processedStr);
                searchResult.setCount( gens.size() );
                searchResult.setList(gens);
            }else{
                searchResult.setCount(txTmGeneralCustomRepository.count(searchCriteria));
                if (searchResult.getCount() > 0) {
                    searchResult.setList(txTmGeneralCustomRepository.selectAll(
                            "LEFT JOIN FETCH c.txRegistration tr " +
                                    " LEFT JOIN FETCH c.txTmPriorList ttp", searchCriteria, orderBy, orderType, offset, limit));
                }
            }
        return searchResult;
    }
    
    public GenericSearchWrapper<TxTmGeneral> searchRefMadridTransformasi(List<KeyValue> searchCriteria, 
    		String orderBy, String orderType, int offset, int limit) {
        GenericSearchWrapper<TxTmGeneral> searchResult = new GenericSearchWrapper<TxTmGeneral>();
        searchResult.setCount(txTmGeneralCustomRepository.count(searchCriteria));
        if (searchResult.getCount() > 0) {
            searchResult.setList(txTmGeneralCustomRepository.selectAll( 
                            " LEFT JOIN FETCH c.txTmPriorList ttp", searchCriteria, orderBy, orderType, offset, limit));
        }
        return searchResult;
    }
}
