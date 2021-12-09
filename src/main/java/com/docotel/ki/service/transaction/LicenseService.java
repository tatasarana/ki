package com.docotel.ki.service.transaction;

import java.util.List;

import com.docotel.ki.model.transaction.TxLicense;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.docotel.ki.repository.custom.transaction.TxLicenseCustomRepository;
import com.docotel.ki.repository.transaction.TxLicenseRepository;

@Service
public class LicenseService {

	@Autowired
	private TxLicenseRepository txLicenseRepository;
	@Autowired
	private TxLicenseCustomRepository txLicenseCustomRepository;
	
	@Transactional
    public List<TxLicense> selectAllLicenseByIdGeneral(String generalId) {
        return txLicenseCustomRepository.selectAllLicenseByGeneralId(generalId);
    }
	
	@Transactional
    public List<TxLicense> selectAllParentByLicenseId(String id, String generalId) {
        return txLicenseCustomRepository.selectAllParentByLicenseId(id, generalId);
    }

    public TxLicense selectOneByApplicationId(String id) {
		return txLicenseCustomRepository.selectOne("txTmGeneral.id", id);
	}
	
	public TxLicense selectOneLicenseById(String id) {
        return txLicenseCustomRepository.selectOne("id", id);
    }
	
	public List<TxLicense> findAll() {
        return txLicenseRepository.findAll();
    }
	
	public List<TxLicense> selectAllLicenseByEndDate() {
        return txLicenseCustomRepository.selectAllLicenseByEndDate();
    }
	
	public GenericSearchWrapper<TxLicense> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<TxLicense> searchResult = new GenericSearchWrapper<TxLicense>();
		searchResult.setCount(txLicenseCustomRepository.count(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(txLicenseCustomRepository.selectAll(
					"LEFT JOIN FETCH c.txTmGeneral tm ", searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}
	
	@Transactional
	public void insertLicense(TxLicense txLicense) {
		
		txLicenseCustomRepository.saveOrUpdate(txLicense);
		
		StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + txLicense.getCurrentId());
        sb.append(", Application No: " + txLicense.getApplicationNumber());
        sb.append(", Nama: " + txLicense.getName());
        sb.append(", Negara: " + txLicense.getmCountry().getName());
        sb.append(", Alamat: " + txLicense.getAddress());
        sb.append(", Telepon: " + txLicense.getPhone());
        sb.append(", Email: " + txLicense.getEmail());
        
	}
}
