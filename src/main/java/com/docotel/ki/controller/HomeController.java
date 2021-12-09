package com.docotel.ki.controller;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.MDocument;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.pojo.KeyValueSelect;
import com.docotel.ki.pojo.NativeQueryModel;
import com.docotel.ki.repository.NativeQueryRepository;
import com.docotel.ki.repository.master.MDocumentRepository;
import com.docotel.ki.repository.master.MUserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.apache.tomcat.jni.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class HomeController extends BaseController {
    public static final String PATH_HOME = "/home";
    public static final String REQUEST_MAPPING_HOME = PATH_HOME + "*";
    private static final String PAGE_HOME = "home";
    private static final String PATH_AJAX_PATH_SETLOGIN = "/berhasil-login";
    private static final String REQUEST_MAPPING_PATH_SETLOGIN = PATH_AJAX_PATH_SETLOGIN + "*";
    private static final String PATH_AJAX_DOWNLOAD = "/home-download*";

    @Autowired
    private NativeQueryRepository nativeQueryRepository ;

    @Autowired
    private MUserRepository mUserRepository;
    
    @Autowired
	private MDocumentRepository mDocumentRepository;
    
    public String setImage(String nameFileImage, String fileType) {
        /*Load Image From Web-Image*/
        String image = "";
        try {
            File file = new File(nameFileImage);
            if (file.exists() && !file.isDirectory()) {
                FileInputStream fileInputStreamReader = new FileInputStream(file);
                byte[] bytes = new byte[(int) file.length()];
                fileInputStreamReader.read(bytes);
                image = fileType+"," + Base64.getEncoder().encodeToString(bytes);
            } else {
                image = "img" + nameFileImage;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

    @RequestMapping(path = {BaseController.PATH_AFTER_LOGIN, REQUEST_MAPPING_HOME})
    public String doShowPageHome(final Model model, final HttpServletRequest request, final HttpServletResponse response) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MUser mUser = (MUser) auth.getPrincipal();


        NativeQueryModel querymodel = new NativeQueryModel() ;
        querymodel.setTable_name("M_USER");
        String[] resultcol = {"user_id","enabled"};
        querymodel.setResultcol(resultcol);
        ArrayList<KeyValueSelect> searchBy = new ArrayList <>() ;
        searchBy.add(new KeyValueSelect("USERNAME",mUser.getUsername(),"=",true,null));
        querymodel.setSearchBy(searchBy);
        GenericSearchWrapper<Object[]> Result = nativeQueryRepository.selectNative2(querymodel);

        //ini di comment dulu karena bikin error dimenu petugas
        List<Object[]> res = Result.getList() ;
        if(res.size() > 0) {
            Object[] single = res.get(0);
            String lastlogin = single[1].toString();
            if (lastlogin.equalsIgnoreCase("2")) {
                model.addAttribute("islogin", 1);
            } else {
                model.addAttribute("islogin", 0);
            }
        } else{
            model.addAttribute("islogin", 0);
        }

        List<String> informasi = new ArrayList<>() ;
        informasi.add("Test Informasi 1");
        informasi.add("Test 2");
        informasi.add("Test 3") ;

        model.addAttribute("informasi",informasi);
        
        List<MDocument> fileImages = mDocumentRepository.findByPathName("%home-image%");
        List<String> list = new ArrayList<String>();
        List<Integer> nums = new ArrayList<Integer>();
        int i = 1;
        for (MDocument fileImage : fileImages) {
        	list.add(setImage(fileImage.getFilePath(), "data:image/jpg;base64"));
        	nums.add(i++);
        }
        
        List<MDocument> fileVideo = mDocumentRepository.findByPathName("%home-video%");
        
        model.addAttribute("fileVideo", fileVideo);
        model.addAttribute("imgPaths", list);
        model.addAttribute("nums", nums);

        if (mUser.isReprs())
            model.addAttribute("typeuser","(Konsultan)");
        else
            model.addAttribute("typeuser","(Umum)");
        return PAGE_HOME;
    }

    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request, final HttpServletResponse response) {

    }

    @RequestMapping(path = REQUEST_MAPPING_PATH_SETLOGIN)
    public void doGetDataPemohon(Model model, final HttpServletRequest request, final HttpServletResponse response) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            MUser mUser = (MUser) auth.getPrincipal();

            NativeQueryModel querymodel = new NativeQueryModel() ;
            querymodel.setTable_name("M_USER");
            ArrayList<KeyValue> updateq = new ArrayList <>();
            KeyValue updateq1 = new KeyValue();
            updateq1.setKey("enabled");
            updateq1.setValue(1);

            updateq.add(updateq1);
            querymodel.setUpdateQ(updateq);
            ArrayList<KeyValueSelect> searchBy = new ArrayList <>() ;
            searchBy.add(new KeyValueSelect("USER_ID",mUser.getId(),"=", true,null));
            querymodel.setSearchBy(searchBy);

            int exe = nativeQueryRepository.updateNavite(querymodel);

        }
    }

    @PostMapping(path = PATH_AJAX_DOWNLOAD)
	public void ajax(@RequestBody String id, final HttpServletRequest request, 
			final HttpServletResponse response, final HttpSession session) throws JsonProcessingException, IOException {
    	
    	MDocument fileVideo = mDocumentRepository.getOne(id);
    	String download = setImage(fileVideo.getFilePath(), "data:video/mp4;base64");
    	    	
		writeJsonResponse(response, download);
	}
}