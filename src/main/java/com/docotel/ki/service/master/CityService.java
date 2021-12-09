package com.docotel.ki.service.master;

import com.docotel.ki.model.master.MCity;
import com.docotel.ki.model.master.MProvince;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.master.MCityCustomRepository;
import com.docotel.ki.repository.master.MCityRepository;
import com.docotel.ki.repository.master.MProvinceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CityService {
	@Autowired
	private MProvinceRepository mProvinceRepository;

	@Autowired
	private MCityRepository mCityRepository;

	@Autowired
	private MCityCustomRepository mCityCustomRepository;

	public List<MCity> selectAllByProvinci(String by, String value) {
		return mCityCustomRepository.selectAllByProvince(by, value, 0, 500);
	}
	// by andra
	public List<MCity> findByStatusFlagTrue() {
        return mCityRepository.findByStatusFlagTrue();
    }
	
	public List<MCity> findByStatusFlagTrueOrderByName() {
        return mCityRepository.findByStatusFlagTrueOrderByName();
    }
	
	public List<MCity> selectAll() {
		return mCityCustomRepository.selectAll("LEFT JOIN c.mProvince", null, null, true, null, null);
	}

	public List<MCity> selectAllOrderByName() {
		return mCityCustomRepository.selectAll("LEFT JOIN c.mProvince", null, "name", "asc", null, null);
	}

	public GenericSearchWrapper<MCity> searchGeneral(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<MCity> searchResult = new GenericSearchWrapper<MCity>();
		searchResult.setCount(mCityCustomRepository.count(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(mCityCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}

	@Transactional
	public void insert(MCity data) {
		MProvince province = null;
		if (data.getmProvince() != null) {
			if (data.getmProvince().getCurrentId() != null) {
				province = mProvinceRepository.findOne(data.getmProvince().getCurrentId());
			}
		}

		if (province == null) {
			throw new DataIntegrityViolationException(HttpStatus.NOT_FOUND.getReasonPhrase() + "-mProvince");
		}

		MCity existing = mCityCustomRepository.selectOne("name", data.getName(), true);
		if (existing != null) {
			throw new DataIntegrityViolationException(HttpStatus.ALREADY_REPORTED.getReasonPhrase() + "-name");
		}
		existing = mCityCustomRepository.selectOne("code", data.getCode(), true);
		if (existing != null) {
			throw new DataIntegrityViolationException(HttpStatus.ALREADY_REPORTED.getReasonPhrase() + "-code");
		}
		mCityRepository.save(data);
	}

	public List<MCity> findAll () {
		return mCityRepository.findAll();
	}

	public MCity findOne(String id) {
		return mCityCustomRepository.selectOne("JOIN FETCH c.mProvince p", "id", id, true);
	}

	public MCity selectOne(String by, String value) {
		return mCityCustomRepository.selectOne(by, value);
	}

	@Transactional
	public void saveOrUpdate(MCity mCity) {
		MCity existing = mCityCustomRepository.selectOne("name", mCity.getName(), true);
		if (existing != null && !existing.getId().equalsIgnoreCase(mCity.getId())) {
			throw new DataIntegrityViolationException(HttpStatus.ALREADY_REPORTED.getReasonPhrase() + "-name");
		}
		existing = mCityCustomRepository.selectOne("code", mCity.getCode(), true);
		if (existing != null && !existing.getId().equalsIgnoreCase(mCity.getId())) {
			throw new DataIntegrityViolationException(HttpStatus.ALREADY_REPORTED.getReasonPhrase() + "-code");
		}
		mCityCustomRepository.saveOrUpdate(mCity);
	}
}
