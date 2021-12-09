package com.docotel.ki.controller;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.MCountry;
import com.docotel.ki.model.master.MDocument;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.transaction.TxPostReception;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.master.MDocumentRepository;
import com.docotel.ki.service.master.CountryService;
import com.docotel.ki.service.transaction.DokumenLampiranService;
import com.docotel.ki.service.transaction.TrademarkService;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.util.FieldValidationUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class HomeDisplayController extends BaseController {

	@Autowired
	private DokumenLampiranService dokumenLampiranService;
	@Autowired
	private MDocumentRepository mDocumentRepository;

	private static final String DIRECTORY_PAGE = "master/";
	private static final String PAGE_HOME_DISPLAY = DIRECTORY_PAGE + "home-display";
	private static final String PATH_HOME_DISPLAY = "/home-display";
	private static final String PATH_HOME_DISPLAY_UPLOAD_IMAGE = "/home-display-upload-image";
	private static final String PATH_HOME_DISPLAY_UPLOAD_VIDEO = "/home-display-upload-video";
	private static final String AJAX_TABLE = "/home-display-reload-table*";
	private static final String AJAX_DELETE = "/home-display-delete*";

	@ModelAttribute
	public void addModelAttribute(final Model model, final HttpServletRequest request) {
		model.addAttribute("menu", "master");
		model.addAttribute("subMenu", "homeDisplay");
	}

	@RequestMapping(path = PATH_HOME_DISPLAY)
	public String showPageMultiupload(Model model) {
		
		return PAGE_HOME_DISPLAY;
	}

	@PostMapping(path = PATH_HOME_DISPLAY_UPLOAD_IMAGE)
	public void uploadImage(@RequestParam("photo") MultipartFile uploadFile, final HttpServletRequest request,
			final HttpServletResponse response) throws IOException, ParseException {

		String message = "Success";

		if (message.equals("Success")) {
			dokumenLampiranService.uploadHomeImage(uploadFile);
		}

		writeJsonResponse(response, message);
	}

	@PostMapping(path = PATH_HOME_DISPLAY_UPLOAD_VIDEO)
	public void uploadVideo(@RequestParam("video") MultipartFile uploadFile, final HttpServletRequest request,
			final HttpServletResponse response) throws IOException, ParseException {

		String message = "Success";

		if (message.equals("Success")) {
			dokumenLampiranService.uploadHomeVideo(uploadFile);
		}

		writeJsonResponse(response, message);
	}

	@PostMapping(path = AJAX_TABLE)
	public void ajaxTable(@RequestParam("data") String data, final HttpServletRequest request, 
			final HttpServletResponse response, final HttpSession session) throws JsonProcessingException, IOException {
		
		ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(data);
        
        List<MDocument> documents = new ArrayList<MDocument>();
        for (JsonNode node : rootNode) {
            String judul = node.get("judul").toString().replaceAll("\"", "");
            String tanggal = node.get("tanggal").toString().replaceAll("\"", "");            
            if (judul.equals("") || judul == null) {
        		judul = "%%";
        	} else {
        		judul = "%"+judul+"%";
        	}
            if (tanggal.equals("") || tanggal == null) {  
            	System.out.println("no tanggal");
            	documents = mDocumentRepository.findName(judul);
            } else {
            	System.out.println("ada tanggal");
            	documents = mDocumentRepository.findByDate(tanggal, judul);
            }
        }
        
        List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
        for (MDocument doc : documents) {
        	Map<String, Object> data2 = new HashMap<>();
        	data2.put("id", doc.getId());
        	data2.put("tanggal", String .valueOf(doc.getCreatedDate()).substring(0, 10));
        	data2.put("judul", doc.getName());
        	list.add(data2);
        }

		writeJsonResponse(response, list);
	}
	
	@PostMapping(path = AJAX_DELETE)
	public void ajaxDelete(@RequestBody String data, final HttpServletRequest request, 
			final HttpServletResponse response, final HttpSession session) throws JsonProcessingException, IOException {
		
		dokumenLampiranService.deleteFile(data);

		writeJsonResponse(response, 200);
	}
}
