package com.docotel.ki.service.master;

import com.docotel.ki.model.master.MCountry;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.master.MCountryCostumRepository;
import com.docotel.ki.repository.master.MCountryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CountryService {
    @Autowired MCountryRepository mCountryRepository;
    @Autowired MCountryCostumRepository mCountryCostumRepository;

    public List<MCountry> findAll() {
        return mCountryRepository.findAll();
    }

    public List<MCountry> findByStatusFlagTrue() {
        return mCountryRepository.findByStatusFlagTrue();
    }
    
    public List<MCountry> findByStatusFlagTrueOrderByName() {
        return mCountryRepository.findByStatusFlagTrueOrderByName();
    }

    public List<MCountry> findByMadrid(Boolean isMadrid){
        return mCountryRepository.findByMadrid(isMadrid);
    }

    public MCountry findCountryById (String id) {
        return mCountryRepository.findOne(id);
    }

    public MCountry findByCode(String code){
        return mCountryCostumRepository.selectOne("code", code, false);
    }

    public GenericSearchWrapper<MCountry> searchGeneral(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        GenericSearchWrapper<MCountry> searchResult = new GenericSearchWrapper<MCountry>();
        searchResult.setCount(mCountryCostumRepository.count(searchCriteria));
        if (searchResult.getCount() > 0) {
            searchResult.setList(mCountryCostumRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
        }
        return searchResult;
    }

    public void insert(MCountry data){
        mCountryRepository.save(data);
    }

    @Transactional
    public void saveOrUpdate(MCountry mCountry) {
        mCountryCostumRepository.saveOrUpdate(mCountry);
    }
    
    public MCountry findByCode2(String code){
        return mCountryCostumRepository.selectOne("code", code, true);
    }

    public MCountry findByName2(String name){
        return mCountryCostumRepository.selectOne("name", name, true);
    }
}
