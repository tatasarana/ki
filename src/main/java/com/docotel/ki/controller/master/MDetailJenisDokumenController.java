package com.docotel.ki.controller.master;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.MDocType;
import com.docotel.ki.model.master.MDocTypeDetail;
import com.docotel.ki.model.master.MFileType;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.service.master.DocTypeDetailService;
import com.docotel.ki.service.master.DocTypeService;
import com.docotel.ki.service.master.FileService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class MDetailJenisDokumenController extends BaseController {
	
    @Autowired
    DocTypeService docTypeService;
    @Autowired
    FileService fileService;
    @Autowired
    DocTypeDetailService docTypeDetailService;
    
    private static final String DIRECTORY_PAGE = "master/jenis-dokumen/";
    private static final String PAGE_TAMBAH = DIRECTORY_PAGE + "detail-dokumen";
    private static final String PATH_TAMBAH = "/detail-dokumen";
    private static final String REQUEST_MAPPING_LIST_TAMBAH = PATH_TAMBAH + "*";
    private static final String REDIRECT_LIST_JENIS_DOKUMEN = "redirect:" + PATH_AFTER_LOGIN + "/list-jenis-dokumen";

    @RequestMapping(path = REQUEST_MAPPING_LIST_TAMBAH)
    public String showPageList(Model model, @RequestParam(value = "no", required = true) String no) {
        try {        	
        	
        	List<MDocTypeDetail> mDocTypeDetailList = docTypeDetailService.selectAllByDocType(no);
        	List<String> existingFileTypeList = new ArrayList<>();
        	for (MDocTypeDetail docTypeDetailExisting : mDocTypeDetailList) {
        		existingFileTypeList.add(docTypeDetailExisting.getmFileType().getCurrentId());
        	}
        	model.addAttribute("existingFileTypeList", existingFileTypeList);
        	
        	MDocType mDocType = docTypeService.findOne(no);
        	if(mDocType != null) {
        		mDocType.getCode();
        		mDocType.getName();
        	}
        	model.addAttribute("mDocType", mDocType);
        	
        	MDocType mDocTypeFile = docTypeService.getFileTypeByDocTypeId(no);
        	if (mDocTypeFile != null) {
        		List<MFileType> mFileTypeList = fileService.findAll();
        		List<Map<String, Object>> fileTypeList = new ArrayList<>(mFileTypeList.size());
        		for (MFileType mFileType : mFileTypeList) {
        			Map<String, Object> fileTypeMap = new HashMap();
        			fileTypeMap.put("id", mFileType.getId());
        			fileTypeMap.put("desc", mFileType.getDesc());
        			fileTypeList.add(fileTypeMap);
        		}
        		
        		model.addAttribute("mFileTypeList", fileTypeList);
        	}
        	
            return PAGE_TAMBAH;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return REDIRECT_LIST_JENIS_DOKUMEN;
        }
    }

   @PostMapping(path = REQUEST_MAPPING_LIST_TAMBAH)
    public String doSave(@RequestParam(value = "ckList", required = false) String[] mFileType, @RequestParam("id") String id ) {
        try {
        	
        	MDocType mDocType = docTypeService.getDocTypeById(id);
        	mDocType.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        	
        	if (mDocType != null) {
        		docTypeDetailService.save(mDocType, mFileType);
        	}
        	
            return REDIRECT_LIST_JENIS_DOKUMEN;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return REDIRECT_LIST_JENIS_DOKUMEN;
        }
    }

}
