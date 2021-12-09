package com.docotel.ki.repository.custom.master;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.master.MStatus;
import org.springframework.stereotype.Repository;

@Repository
public class MStepCustomRepository extends BaseRepository<MStatus> {
	
	public MStatus selectOne(String by, Object value) {
		return super.selectOne("LEFT JOIN FETCH c.createdBy cb "
				+ "LEFT JOIN FETCH c.mFileSequence ms "				
				+ " ", by, value, true);
	}

}

