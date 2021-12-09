package com.docotel.ki.repository.custom.master;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.master.MFileTypeDetail;
import com.docotel.ki.pojo.KeyValue;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MFileTypeDetailCustomRepository extends BaseRepository<MFileTypeDetail> {
	public MFileTypeDetail selectOne(String by, Object value) {
		return super.selectOne("", by, value, true);
	}

	public List<MFileTypeDetail> selectAllByFileType(String by, Object value, Integer offset, Integer limit) {
		return super.selectAll("LEFT JOIN FETCH c.mFileType p", by, value, true, offset, limit);
	}
	
	public List<MFileTypeDetail> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		return selectAll("JOIN FETCH c.createdBy cb", searchCriteria, orderBy, orderType, offset, limit);
	}
}
