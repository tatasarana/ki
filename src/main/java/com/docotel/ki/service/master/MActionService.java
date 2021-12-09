package com.docotel.ki.service.master;

import java.util.List;

import com.docotel.ki.model.master.MAction;
import com.docotel.ki.model.master.MStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.docotel.ki.repository.custom.master.MActionCustomRepository;
import com.docotel.ki.repository.master.MActionRepository;

@Service
public class MActionService {
	@Autowired
	private MActionRepository mActionRepository;
	@Autowired
	private MActionCustomRepository mActionCustomRepository;
	
	public List<MAction> findAll() {
		return mActionRepository.findAll();
	}
	
	public MAction selectOneBy(String id) {
		return mActionCustomRepository.selectOne("id", id);
	}

	public List<MAction> selectActionActive() {
		return mActionCustomRepository.selectAll("statusFlag", true, true, null, null);
	}
}
