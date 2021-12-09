package com.docotel.ki.repository.custom.master;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.master.MRepresentative;
import com.docotel.ki.pojo.KeyValue;

import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public class MConsultantCustomRepository  extends BaseRepository<MRepresentative> {
	
	@Override
    public List<MRepresentative> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		return selectAll("LEFT JOIN FETCH c.mCountry mco LEFT JOIN FETCH c.mProvince mp LEFT JOIN FETCH c.mCity mc JOIN FETCH c.createdBy cb LEFT JOIN FETCH c.userId ui",
				searchCriteria, orderBy, orderType, offset, limit);
	}
}
