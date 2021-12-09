package com.docotel.ki.controller;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.service.transaction.DokumenLampiranService;
import com.docotel.ki.service.transaction.TrademarkService;
import com.docotel.ki.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class PermohonanMultiuploadController extends BaseController {
	@Autowired private TrademarkService trademarkService;
	@Autowired private DokumenLampiranService dokumenLampiranService;
    
    private static final String DIRECTORY_PAGE = "permohonan-multiupload/";
    
    private static final String PAGE_MULTIUPLOAD = DIRECTORY_PAGE + "permohonan-multiupload";
    
    private static final String PATH_MULTIUPLOAD = "/permohonan-multiupload";

    @ModelAttribute
	public void addModelAttribute(final Model model, final HttpServletRequest request) {
		model.addAttribute("menu", "permohonanMerek");
		model.addAttribute("subMenu", "multiupload");
	}

	@RequestMapping(path = PATH_MULTIUPLOAD)
    public String showPageMultiupload(Model model) {
        return PAGE_MULTIUPLOAD;
    }
    
    @PostMapping(path = PATH_MULTIUPLOAD)
    public void doMultiUpload(@RequestParam("photo") MultipartFile uploadFile, 
			final HttpServletRequest request, 
			final HttpServletResponse response) throws IOException, ParseException {
    	String message = "Success";
    	String fileName = uploadFile.getOriginalFilename();
    	String applicationNo = fileName.substring(0, fileName.lastIndexOf(".")).trim();
    	String mimeType = fileName.substring(fileName.lastIndexOf("."), fileName.length()).trim();
    	String createdDate = DateUtil.formatDate(new Timestamp(System.currentTimeMillis()), "dd/MM/yyyy");
    	
    	List<KeyValue> searchCriteria = new ArrayList<>();
    	searchCriteria.add(new KeyValue("txTmGeneral.applicationNo", applicationNo, true));
    	searchCriteria.add(new KeyValue("createdDate", DateUtil.toDate("dd/MM/yyyy", createdDate), true));
    	
    	TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(applicationNo);
    	
    	if(txTmGeneral == null)
    		message = "Permohonan tidak di temukan, Nama File PDF harus sesuai dengan No Permohonan";
    	
    	if(dokumenLampiranService.selectOne(searchCriteria, "id", "ASC") != null)
    		message = "File permohonan untuk tanggal " + createdDate + " sudah ada";
    	
    	if(message.equals("Success")) {
    		dokumenLampiranService.tambahMulti(uploadFile, txTmGeneral, applicationNo, mimeType, createdDate);
    	}
    	
    	writeJsonResponse(response, message);
	}
}
