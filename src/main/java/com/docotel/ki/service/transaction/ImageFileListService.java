package com.docotel.ki.service.transaction;

import com.docotel.ki.model.wipo.ImageFileList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.docotel.ki.repository.custom.transaction.ImageFileListCustomRepository;
import com.docotel.ki.repository.transaction.ImageFileListRepository;

@Service
public class ImageFileListService {
	@Autowired ImageFileListRepository imageFileListRepository;
	@Autowired ImageFileListCustomRepository imageFileListCustomRepository;
    
	@Transactional
    public void saveImageFileList(ImageFileList imageFileList) {
    	imageFileListRepository.save(imageFileList);
    }
	
	@Transactional
    public void saveUpdateImageFileList(ImageFileList imageFileList) {
		imageFileListCustomRepository.saveOrUpdate(imageFileList);
    }
    
    public ImageFileList selectOne(Object value) {
		return imageFileListCustomRepository.selectOne("name", value, true);
	}
}
