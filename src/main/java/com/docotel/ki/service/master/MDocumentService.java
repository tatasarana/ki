package com.docotel.ki.service.master;

import java.util.List;

import com.docotel.ki.model.master.MDocument;
import com.docotel.ki.model.master.MDocumentParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.docotel.ki.repository.custom.master.MDocumentCustomRepository;
import com.docotel.ki.repository.master.MDocumentParamRepository;
import com.docotel.ki.repository.master.MDocumentRepository;

@Service
public class MDocumentService {

	@Autowired
	MDocumentRepository mDocumentRepository;
	@Autowired
	MDocumentCustomRepository mDocumentCustomRepository;
	@Autowired
	MDocumentParamRepository mDocumentParamRepository;
	
	@Transactional
    public MDocument saveMDoc(MDocument mDocument) {
		return mDocumentRepository.save(mDocument);
    }
	
	@Transactional
    public void deleteMDocumentParamByMDocumentId(String id) {
		mDocumentParamRepository.deleteMDocumentParamByMDocumentId(id);
    }
	
	@Transactional
    public List<MDocumentParam> saveMDocParam(List<MDocumentParam> mDocumentParam) {
		return mDocumentParamRepository.save(mDocumentParam);
    }
	
	@Transactional
    public void delete(String id) {
		mDocumentRepository.delete(id);
    }
}
