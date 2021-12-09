package com.docotel.ki.repository.custom.master;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.master.MFileSequence;
import com.docotel.ki.pojo.KeyValue;

import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public class MFileSeqCustomRepository extends BaseRepository<MFileSequence> {
	public MFileSequence selectOne(String by, Object value) {
		return super.selectOne("JOIN FETCH c.createdBy cb", by, value, true);
	}
	
	public List<MFileSequence> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		return selectAll("JOIN FETCH c.createdBy cb", searchCriteria, orderBy, orderType, offset, limit);
	}
	
	
}
