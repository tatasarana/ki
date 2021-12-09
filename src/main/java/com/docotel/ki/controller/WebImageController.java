package com.docotel.ki.controller;

import com.docotel.ki.base.BaseController;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UriUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Controller
public class WebImageController extends BaseController {

    private static final String DIRECTORY_IMG = "/img/";
    private static final String REQUEST_MAPPING_IMAGE = DIRECTORY_IMG + "*";
    private static final String REQUEST_MAPPING_DOC = "/doc/*";

    @Value("${upload.file.web.image:}")
    private String pathImage;

    @Value("${upload.file.web.doc:}")
    private String pathDoc;

    @RequestMapping(path = REQUEST_MAPPING_IMAGE, method = {RequestMethod.GET})
    public void doGetImage(final HttpServletRequest request, final HttpServletResponse response) {
        String str = request.getServletPath();
        String imageDefault = str.substring(request.getServletPath().lastIndexOf("/") + 1);
        getImage(request, response, imageDefault);
    }

    @RequestMapping(path = REQUEST_MAPPING_DOC, method = {RequestMethod.GET})
    public void doGetDoc(final HttpServletRequest request, final HttpServletResponse response) {
//        System.out.println("getServletPath : " + request.getServletPath());
        String str = request.getServletPath();
        String docDefault = str.substring(request.getServletPath().lastIndexOf("/") + 1);
        getDoc(request, response, docDefault);
    }

    public void getDoc(HttpServletRequest request, HttpServletResponse response, String docDefault) {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        byte[] fileBytes = null;
        try {
            File file = new File(pathDoc + docDefault);
            setResponseAs(response, FilenameUtils.getExtension(docDefault));
            if (file.exists()) {
                bis = new BufferedInputStream(new FileInputStream(file));
            } else {
//                file = new File(getClass().getClassLoader().getResource("static" + request.getServletPath()).getFile());
//                bis = new BufferedInputStream(getClass().getClassLoader().getResourceAsStream("static" + request.getServletPath()));
                String path = UriUtils.decode(getClass().getClassLoader().getResource("static/doc").getPath().toString(),"UTF-8");
                file = new File(path + "/" + docDefault);
                bis = new BufferedInputStream(new FileInputStream(file));
            }
            bos = new BufferedOutputStream(response.getOutputStream());
            fileBytes = FileUtils.readFileToByteArray(file);
            int length = bis.read(fileBytes);
            bos.write(fileBytes, 0, length);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }  finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                }
            }
        }
    }


    public void getImage(HttpServletRequest request, HttpServletResponse response, String imageDefault) {
        if (!FilenameUtils.getExtension(imageDefault).equalsIgnoreCase("ico")) {
            setResponseAs(response, "image/" + FilenameUtils.getExtension(imageDefault));
        }else{
            setResponseAs(response, "image/x-icon");
        }
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            byte[] fileBytes = null;
            try {
                File file = new File(pathImage + imageDefault);
                if (file.exists()) {
                    bis = new BufferedInputStream(new FileInputStream(file));
                } else {
                    String path = UriUtils.decode(getClass().getClassLoader().getResource("static/img").getPath().toString(),"UTF-8");
                    file = new File(path + "/" + imageDefault);
                    bis = new BufferedInputStream(new FileInputStream(file));
                }
                bos = new BufferedOutputStream(response.getOutputStream());
                fileBytes = FileUtils.readFileToByteArray(file);
                int length = bis.read(fileBytes);
                bos.write(fileBytes, 0, length);
                /* byte[] buffer = new byte[2048];
                int length;
                while ((length = bis.read(buffer)) > 0) {
                    bos.write(buffer, 0, length);
                }*/
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            } finally {
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                    }
                }
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e) {
                    }
                }
            }

    }
}
