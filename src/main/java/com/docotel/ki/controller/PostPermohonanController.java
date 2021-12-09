package com.docotel.ki.controller;


import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.*;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.pojo.*;
import com.docotel.ki.repository.NativeQueryRepository;
import com.docotel.ki.repository.custom.master.MCityCustomRepository;
import com.docotel.ki.repository.custom.master.MCountryCostumRepository;
import com.docotel.ki.repository.custom.master.MProvinceCustomRepository;
import com.docotel.ki.repository.transaction.TxTmGeneralRepository;
import com.docotel.ki.service.master.BrandService;
import com.docotel.ki.service.transaction.*;
import com.docotel.ki.util.DateUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class PostPermohonanController extends BaseController {

    private static final String PATH_AJAX_SAVE_FORM_1 = "/save-form-1";
    private static final String PATH_AJAX_SAVE_FORM_2 = "/save-form-2";
    private static final String PATH_AJAX_SAVE_FORM_5 = "/save-form-5";
    private static final String PATH_AJAX_SAVE_BIAYA = "/save-biaya";
    private static final String PATH_POST_DOKUMEN = "/post-dokumen-lampiran";
    /*private static final String PATH_AJAX_SAVE_TAMBAH_PEMOHON = "/save-tambah-pemohon";*/
    private static final String PATH_AJAX_SAVE_EDIT_PERMOHONAN = "/save-edit-form-2";
    private static final String PATH_AJAX_SAVE_EDIT_LISENSI = "/save-edit-lisensi";
    private static final String PATH_AJAX_SAVE_NONAKTIF_LISENSI = "/save-nonaktif-lisensi";
    private static final String PATH_AJAX_SAVE_TAMBAH_LISENSI_PETUGAS = "/save-tambah-lisensi-petugas";
    private static final String PATH_AJAX_LIST_PEMOHON = "/list-pemohon2";
    //    private static final String PATH_AJAX_LIST_IMAGE_DETAIL = "/list-image";
    private static final String PATH_AJAX_PATH_PEMOHON = "/pemohon";
    private static final String REQUEST_MAPPING_PEMOHON = PATH_AJAX_PATH_PEMOHON + "*";
    private static final String REQUEST_MAPPING_AJAX_PEMOHON_LIST = PATH_AJAX_LIST_PEMOHON + "*";
    //    private static final String REQUEST_MAPPING_AJAX_LIST_IMAGE_DETAIL = PATH_AJAX_LIST_IMAGE_DETAIL + "*";
    private static final String REQUEST_MAPPING_AJAX_SAVE_FORM_1 = PATH_AJAX_SAVE_FORM_1 + "*";
    private static final String REQUEST_MAPPING_AJAX_SAVE_FORM_2 = PATH_AJAX_SAVE_FORM_2 + "*";
    private static final String REQUEST_MAPPING_AJAX_SAVE_FORM_5 = PATH_AJAX_SAVE_FORM_5 + "*";
    private static final String REQUEST_MAPPING_AJAX_SAVE_BIAYA = PATH_AJAX_SAVE_BIAYA + "*";
    /*private static final String REQUEST_MAPPING_AJAX_SAVE_TAMBAH_PEMOHON = PATH_AJAX_SAVE_TAMBAH_PEMOHON + "*";*/
    private static final String REQUEST_MAPPING_TAMBAH_DOKUMEN = PATH_POST_DOKUMEN + "*";
    private static final String REQUEST_MAPPING_AJAX_SAVE_EDIT_PERMOHONAN = PATH_AJAX_SAVE_EDIT_PERMOHONAN + "*";
    private static final String REQUEST_MAPPING_AJAX_SAVE_EDIT_LISENSI = PATH_AJAX_SAVE_EDIT_LISENSI + "*";
    private static final String REQUEST_MAPPING_AJAX_SAVE_NONAKTIF_LISENSI = PATH_AJAX_SAVE_NONAKTIF_LISENSI + "*";
    private static final String REQUEST_MAPPING_AJAX_SAVE_TAMBAH_LISENSI_PETUGAS = PATH_AJAX_SAVE_TAMBAH_LISENSI_PETUGAS + "*";
    @Autowired
    private PermohonanService permohonanService;
    @Autowired
    private TrademarkService trademarkService;
    @Autowired
    private OwnerService ownerService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private DokumenLampiranService doclampiranService;
    @Autowired
    private LicenseService licenseService;
    @Autowired
    private TxTmGeneralRepository txTmGeneralRepository;
    @Autowired
    private MadridOoService madridOoService;
    @Autowired
    private NativeQueryRepository nativeQueryRepository ;
    @Autowired
    private MCountryCostumRepository mCountryCostumRepository;
    @Autowired
    private MProvinceCustomRepository mProvinceCustomRepository;
    @Autowired
    private MCityCustomRepository mCityCustomRepository;

    @Value("${upload.file.branddetail.path:}")
    private String uploadFileBrandDetailPath;

    @PostMapping(REQUEST_MAPPING_AJAX_SAVE_FORM_1)
    @ResponseBody
    public void doSaveForm1(@RequestBody DataForm1 data, final HttpServletRequest request, final HttpServletResponse response) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            try {
                permohonanService.insertForm1(data);
                writeJsonResponse(response, 200);
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                writeJsonResponse(response, 500);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @PostMapping(REQUEST_MAPPING_AJAX_SAVE_FORM_2)
    @ResponseBody
    public void doSaveForm2(@RequestBody TxTmOwner pemohon, final HttpServletRequest request, final HttpServletResponse response) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            Timestamp tmstm = new Timestamp(System.currentTimeMillis());
            pemohon.setCreatedDate(tmstm);
            pemohon.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(pemohon.getTxTmGeneral().getApplicationNo());
            pemohon.setTxTmGeneral(txTmGeneral);
            pemohon.setStatus(true);
            for (TxTmOwnerDetail txTmOwnerDetail : pemohon.getTxTmOwnerDetails()) {
                txTmOwnerDetail.setTxTmOwner(pemohon);
                txTmOwnerDetail.setTxTmGeneral(pemohon.getTxTmGeneral());
                txTmOwnerDetail.setStatus(true);
            }
            try {
                if (pemohon.getmCity() != null && pemohon.getmCity().getCurrentId() == null || "".equalsIgnoreCase(pemohon.getmCity().getCurrentId())) {
                    pemohon.setmCity(null);
                }
                if (pemohon.getmProvince() != null && pemohon.getmProvince().getCurrentId() == null || "".equalsIgnoreCase(pemohon.getmProvince().getCurrentId())) {
                    pemohon.setmProvince(null);
                }

                if (pemohon.getPostCity() != null && (pemohon.getPostCity().getCurrentId() == null || "".equalsIgnoreCase(pemohon.getPostCity().getCurrentId()))) {
                    pemohon.setPostCity(null);
                }
                if (pemohon.getPostProvince() != null && (pemohon.getPostProvince().getCurrentId() == null || "".equalsIgnoreCase(pemohon.getPostProvince().getCurrentId()))) {
                    pemohon.setPostProvince(null);
                }

                if (pemohon.getPostCountry() != null && (pemohon.getPostCountry().getCurrentId() == null || "".equalsIgnoreCase(pemohon.getPostCountry().getCurrentId()))) {
                    pemohon.setPostCountry(null);
                }

                permohonanService.insertForm2(pemohon);

                writeJsonResponse(response, 200);
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                writeJsonResponse(response, 500);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @PostMapping(REQUEST_MAPPING_AJAX_SAVE_EDIT_PERMOHONAN)
    @ResponseBody
    public void doSaveEditForm2(@RequestBody TxTmOwner pemohon, final HttpServletRequest request, final HttpServletResponse response) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            Timestamp tmstm = new Timestamp(System.currentTimeMillis());

            // For old pemohon set status false
            TxTmOwner oldPemohon = permohonanService.selectOneOwnerById(pemohon.getId());
            oldPemohon.setStatus(false);


            // for old detail pemohon set status false
            for (TxTmOwnerDetail txTmOwnerDetail : oldPemohon.getTxTmOwnerDetails()) {
                txTmOwnerDetail.setTxTmOwner(oldPemohon);
                txTmOwnerDetail.setTxTmGeneral(oldPemohon.getTxTmGeneral());
                txTmOwnerDetail.setStatus(false);
            }

            //insert new pemohon
            pemohon.setCreatedDate(tmstm);
            pemohon.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(pemohon.getTxTmGeneral().getApplicationNo());
            pemohon.setTxTmGeneral(txTmGeneral);
            pemohon.isStatus();

            //insert new detail pemohon
            for (TxTmOwnerDetail txTmOwnerDetail : pemohon.getTxTmOwnerDetails()) {
                txTmOwnerDetail.setTxTmOwner(pemohon);
                txTmOwnerDetail.setTxTmGeneral(pemohon.getTxTmGeneral());
                txTmOwnerDetail.setStatus(true);
            }
            try {
                if (pemohon.getmCity() != null && pemohon.getmCity().getCurrentId() == null || "".equalsIgnoreCase(pemohon.getmCity().getCurrentId())) {
                    pemohon.setmCity(null);
                }
                if (pemohon.getmProvince() != null && pemohon.getmProvince().getCurrentId() == null || "".equalsIgnoreCase(pemohon.getmProvince().getCurrentId())) {
                    pemohon.setmProvince(null);
                }

                if (pemohon.getPostCity() != null && (pemohon.getPostCity().getCurrentId() == null || "".equalsIgnoreCase(pemohon.getPostCity().getCurrentId()))) {
                    pemohon.setPostCity(null);
                }
                if (pemohon.getPostProvince() != null && (pemohon.getPostProvince().getCurrentId() == null || "".equalsIgnoreCase(pemohon.getPostProvince().getCurrentId()))) {
                    pemohon.setPostProvince(null);
                }

                if (pemohon.getPostCountry() != null && (pemohon.getPostCountry().getCurrentId() == null || "".equalsIgnoreCase(pemohon.getPostCountry().getCurrentId()))) {
                    pemohon.setPostCountry(null);
                }

                permohonanService.SaveEditPemohon(pemohon, oldPemohon);

                writeJsonResponse(response, 200);
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                writeJsonResponse(response, 500);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @PostMapping(REQUEST_MAPPING_AJAX_SAVE_EDIT_LISENSI)
    @ResponseBody
    public void doSaveEditLicense(@RequestBody TxLicense txLicense, final HttpServletRequest request, final HttpServletResponse response) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            try {

                Timestamp tmstm = new Timestamp(System.currentTimeMillis());
                txLicense.setUpdatedDate(tmstm);
                txLicense.setUpdatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(txLicense.getTxTmGeneral().getApplicationNo());
                txLicense.setTxTmGeneral(txTmGeneral);
                txLicense.setStatus(txLicense.isStatus());
                txLicense.setLevel(txLicense.getLevel() != null ? txLicense.getLevel()+1 : 1);

                txLicense.setNationality(txLicense.getNationality());
                txLicense.setAddress(txLicense.getAddress());
                txLicense.setmCountry(txLicense.getmCountry());
                txLicense.setEmail(txLicense.getEmail());
                txLicense.setPhone(txLicense.getPhone());
                txLicense.setZipCode(txLicense.getZipCode());
                txLicense.setStartDate(txLicense.getStartDate());
                txLicense.setEndDate(txLicense.getEndDate());
                txLicense.setType(txLicense.getType());
                txLicense.setTxLicenseParent(txLicense.getTxLicenseParent());
                txLicense.setName(txLicense.getName());
                txLicense.setNo(txLicense.getNo());
                txLicense.setTxPostReception(txLicense.getTxPostReception());

                /*if (txLicense.getTxLicenseParent().getName().equalsIgnoreCase("Utama")) {
                    txLicense.setTxLicenseParent(null);
                }*/

                if (txLicense.getmCity() != null && txLicense.getmCity().getCurrentId() == null || "".equalsIgnoreCase(txLicense.getmCity().getCurrentId())) {
                    txLicense.setmCity(null);
                }
                if (txLicense.getmProvince() != null && txLicense.getmProvince().getCurrentId() == null || "".equalsIgnoreCase(txLicense.getmProvince().getCurrentId())) {
                    txLicense.setmProvince(null);
                }

                licenseService.insertLicense(txLicense);

                writeJsonResponse(response, 200);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                //writeJsonResponse(response, 500);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @PostMapping(REQUEST_MAPPING_AJAX_SAVE_TAMBAH_LISENSI_PETUGAS)
    @ResponseBody
    public void doSaveTambahLisensi(@RequestBody TxLicense txLicense, final HttpServletRequest request, final HttpServletResponse response) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            Timestamp tmstm = new Timestamp(System.currentTimeMillis());
            txLicense.setCreatedDate(tmstm);
            txLicense.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            txLicense.setUpdatedDate(tmstm);
            txLicense.setUpdatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(txLicense.getTxTmGeneral().getApplicationNo());
            txLicense.setTxTmGeneral(txTmGeneral);
            txLicense.setStatus(true);
            if(txLicense.getTxLicenseParent().getId().equalsIgnoreCase("")) {
                txLicense.setTxLicenseParent(null);
            }
            txLicense.setLevel(txLicense.getLevel()+1);
            try {
                if (txLicense.getmCity() != null && txLicense.getmCity().getCurrentId() == null || "".equalsIgnoreCase(txLicense.getmCity().getCurrentId())) {
                    txLicense.setmCity(null);
                }
                if (txLicense.getmProvince() != null && txLicense.getmProvince().getCurrentId() == null || "".equalsIgnoreCase(txLicense.getmProvince().getCurrentId())) {
                    txLicense.setmProvince(null);
                }
                licenseService.insertLicense(txLicense);

                writeJsonResponse(response, 200);
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                writeJsonResponse(response, 500);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @PostMapping(REQUEST_MAPPING_AJAX_SAVE_NONAKTIF_LISENSI)
    @ResponseBody
    public void doSaveNonAktifLicense(@RequestBody TxLicense txLicense, final HttpServletRequest request, final HttpServletResponse response) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            Timestamp tmstm = new Timestamp(System.currentTimeMillis());
            txLicense.setUpdatedDate(tmstm);
            txLicense.setUpdatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(txLicense.getTxTmGeneral().getApplicationNo());
            txLicense.setTxTmGeneral(txTmGeneral);
            txLicense.setStatus(false);

            try {
                if (txLicense.getmCity() != null && txLicense.getmCity().getCurrentId() == null || "".equalsIgnoreCase(txLicense.getmCity().getCurrentId())) {
                    txLicense.setmCity(null);
                }
                if (txLicense.getmProvince() != null && txLicense.getmProvince().getCurrentId() == null || "".equalsIgnoreCase(txLicense.getmProvince().getCurrentId())) {
                    txLicense.setmProvince(null);
                }

                licenseService.insertLicense(txLicense);

                writeJsonResponse(response, 200);
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                writeJsonResponse(response, 500);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    // Move to PermohonanGlobalController
//    @PostMapping(REQUEST_MAPPING_AJAX_SAVE_TAMBAH_PEMOHON)
//    @ResponseBody
//    public void doSaveTambahPemohon(@RequestBody TxTmOwner pemohon, final HttpServletRequest request, final HttpServletResponse response) {
//        if (isAjaxRequest(request)) {
//            setResponseAsJson(response);
//            Timestamp tmstm = new Timestamp(System.currentTimeMillis());
//            pemohon.setCreatedDate(tmstm);
//            pemohon.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
//            TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(pemohon.getTxTmGeneral().getApplicationNo());
//            pemohon.setTxTmGeneral(txTmGeneral);
//            pemohon.setStatus(true);
//            for (TxTmOwnerDetail txTmOwnerDetail : pemohon.getTxTmOwnerDetails()) {
//                txTmOwnerDetail.setTxTmOwner(pemohon);
//                txTmOwnerDetail.setTxTmGeneral(pemohon.getTxTmGeneral());
//                txTmOwnerDetail.setStatus(true);
//            }
//            try {
//                if (pemohon.getmCity() != null && pemohon.getmCity().getCurrentId() == null || "".equalsIgnoreCase(pemohon.getmCity().getCurrentId())) {
//                    pemohon.setmCity(null);
//                }
//                if (pemohon.getmProvince() != null && pemohon.getmProvince().getCurrentId() == null || "".equalsIgnoreCase(pemohon.getmProvince().getCurrentId())) {
//                    pemohon.setmProvince(null);
//                }
//
//                if (pemohon.getPostCity() != null && (pemohon.getPostCity().getCurrentId() == null || "".equalsIgnoreCase(pemohon.getPostCity().getCurrentId()))) {
//                    pemohon.setPostCity(null);
//                }
//                if (pemohon.getPostProvince() != null && (pemohon.getPostProvince().getCurrentId() == null || "".equalsIgnoreCase(pemohon.getPostProvince().getCurrentId()))) {
//                    pemohon.setPostProvince(null);
//                }
//
//                if (pemohon.getPostCountry() != null && (pemohon.getPostCountry().getCurrentId() == null || "".equalsIgnoreCase(pemohon.getPostCountry().getCurrentId()))) {
//                    pemohon.setPostCountry(null);
//                }
//
//                permohonanService.insertPemohon(pemohon);
//
//                writeJsonResponse(response, 200);
//            } catch (DataIntegrityViolationException e) {
//                logger.error(e.getMessage(), e);
//                writeJsonResponse(response, 500);
//            }
//        } else {
//            response.setStatus(HttpServletResponse.SC_FOUND);
//        }
//    }


    /*@PostMapping(REQUEST_MAPPING_AJAX_SAVE_FORM_5)*/
    @RequestMapping(value = REQUEST_MAPPING_AJAX_SAVE_FORM_5, method = {RequestMethod.POST})
    @ResponseBody
    public void doSaveForm5(@RequestParam("txTmBrand") String stxTmBrand, @RequestParam(value = "fileMerek", required = false) MultipartFile uploadfile,
                            @RequestParam("listImageDetail") String listImageDetail, @RequestParam("listDelete") String[] listDelete,
                            final HttpServletRequest request, final HttpServletResponse response) throws IOException {

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);

        if (uploadfile != null && uploadfile.getSize() > 0) {
            if (uploadfile.getSize() > 5242880) {
                result.put("message", "Ukuran file merek maksimal 5MB");
            } else {
                String filename = uploadfile.getOriginalFilename().toLowerCase();
                if (!filename.endsWith(".jpg") && !filename.endsWith(".jpeg")) {
                    result.put("message", "File merek harus dalam format JPG /JPEG");
                }
            }
        }

        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(stxTmBrand);
            try {
                String no = rootNode.get("txTmGeneral").toString().replaceAll("\"", "");
                String idmBrandType = rootNode.get("mBrandType").toString().replaceAll("\"", "");
                String name = rootNode.get("name").toString() != "" ? rootNode.get("name").toString().replaceAll("\"", "") : null;
                String keywordMerek = rootNode.get("keywordMerek").toString() != "" ? rootNode.get("keywordMerek").toString().replaceAll("\"", "") : null;
                String color = rootNode.get("color").toString().replaceAll("\"", "");
                String description = rootNode.get("description").toString().replaceAll("\"", "");
                String descChar = rootNode.get("descChar").toString().replaceAll("\"", "");
                String disclaimer = rootNode.get("disclaimer").toString().replaceAll("\"", "");
                String translation = rootNode.get("translation").toString().replaceAll("\"", "");
                String typeCollective = rootNode.get("typeCollective").toString().replaceAll("\"", "");
                String colorCombination = rootNode.get("colorCombination").toString().replaceAll("\"", "");
                String standardChar = rootNode.get("standardChar").toString().replaceAll("\"", "");
                String colorIndication = rootNode.get("colorIndication").toString().replaceAll("\"", "");
                String translationFr = rootNode.get("translationFr").toString().replaceAll("\"", "");
                String translationSp = rootNode.get("translationSp").toString().replaceAll("\"", "");

                String descMerek = "";
                if(description.indexOf("\\n") >= 0) {
                    String[] descPattern = description.split(Pattern.quote("\\n"));
                    for(String dsc : descPattern) {
                        descMerek += dsc + "\n";
                    }
                    if(descMerek.length() > 0) {
                        descMerek = descMerek.substring(0, descMerek.length() - 1);
                    }
                } else {
                    descMerek = description;
                }

                String keyMerek = "";
                if(keywordMerek.indexOf("\\n") >= 0) {
                    String[] keyMerekPattern = keywordMerek.split(Pattern.quote("\\n"));
                    for(String ky : keyMerekPattern) {
                        keyMerek += ky + "\n";
                    }
                    if(keyMerek.length() > 0) {
                        keyMerek = keyMerek.substring(0, keyMerek.length() - 1);
                    }
                } else {
                    keyMerek = keywordMerek;
                }

                String descriptionChar = "";
                if(descChar.indexOf("\\n") >= 0) {
                    String[] descCharPattern = descChar.split(Pattern.quote("\\n"));
                    for(String dscChr : descCharPattern) {
                        descriptionChar += dscChr + "\n";
                    }
                    if(descriptionChar.length() > 0) {
                        descriptionChar = descriptionChar.substring(0, descriptionChar.length() - 1);
                    }
                } else {
                    descriptionChar = descChar;
                }

                String disclaimers = "";
                if(disclaimer.indexOf("\\n") >= 0) {
                    String[] disclaimerPattern = disclaimer.split(Pattern.quote("\\n"));
                    for(String dscChr : disclaimerPattern) {
                        disclaimers += dscChr + "\n";
                    }
                    if(disclaimers.length() > 0) {
                        disclaimers = disclaimers.substring(0, disclaimers.length() - 1);
                    }
                } else {
                    disclaimers = disclaimer;
                }

                String colors = "";
                if(color.indexOf("\\n") >= 0) {
                    String[] colorPattern = color.split(Pattern.quote("\\n"));
                    for(String clr : colorPattern) {
                        colors += clr + "\n";
                    }
                    if(colors.length() > 0) {
                        colors = colors.substring(0, colors.length() - 1);
                    }
                } else {
                    colors = color;
                }

                String translations = "";
                if(translation.indexOf("\\n") >= 0) {
                    String[] tranlationPattern = translation.split(Pattern.quote("\\n"));
                    for(String trnlate : tranlationPattern) {
                        translations += trnlate + "\n";
                    }
                    if(translations.length() > 0) {
                        translations = translations.substring(0, translations.length() - 1);
                    }
                } else {
                    translations = translation;
                }

                String translationFrs = "";
                if(translationFr.indexOf("\\n") >= 0) {
                    String[] tranlationFrPattern = translationFr.split(Pattern.quote("\\n"));
                    for(String trnlateFr : tranlationFrPattern) {
                        translationFrs += trnlateFr + "\n";
                    }
                    if(translationFrs.length() > 0) {
                        translationFrs = translationFrs.substring(0, translationFrs.length() - 1);
                    }
                } else {
                    translationFrs = translationFr;
                }

                String translationSps = "";
                if(translationSp.indexOf("\\n") >= 0) {
                    String[] tranlationSpPattern = translationSp.split(Pattern.quote("\\n"));
                    for(String trnlateSp : tranlationSpPattern) {
                        translationSps += trnlateSp + "\n";
                    }
                    if(translationSps.length() > 0) {
                        translationSps = translationSps.substring(0, translationSps.length() - 1);
                    }
                } else {
                    translationSps = translationSp;
                }

                Timestamp tmstm = new Timestamp(System.currentTimeMillis());

                TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(no);

                TxTmBrand txTmBrand = txTmGeneral.getTxTmBrand() == null ? new TxTmBrand() : txTmGeneral.getTxTmBrand();
                MBrandType mBrandType = new MBrandType();
                mBrandType.setId(idmBrandType);

                txTmBrand.setTxTmGeneral(txTmGeneral);
                txTmBrand.setmBrandType(mBrandType);
                txTmBrand.setName(name);
                txTmBrand.setKeywordMerek(keyMerek);
                txTmBrand.setDescription(descMerek);
                txTmBrand.setTranslation(translations);
                txTmBrand.setDescChar(descriptionChar);
                txTmBrand.setColor(colors);
                txTmBrand.setDisclaimer(disclaimers);
                txTmBrand.setColorIndication(colorIndication);
                txTmBrand.setTranslationFr(translationFrs);
                txTmBrand.setTranslationSp(translationSps);
                txTmBrand.setTypeCollective(Boolean.parseBoolean(typeCollective));
                txTmBrand.setStandardChar(Boolean.parseBoolean(standardChar));
                txTmBrand.setColorCombination(Boolean.parseBoolean(colorCombination));
                if (txTmGeneral.getTxTmBrand() == null) {
                    txTmBrand.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                    txTmBrand.setCreatedDate(tmstm);
                }

                String oldFileName = null;
                if (uploadfile != null) {
                    oldFileName = txTmBrand.getFileName();
                    txTmBrand.setFileMerek(uploadfile);
                    txTmBrand.setFileName(txTmBrand.getFileMerek().getOriginalFilename());
                }

                brandService.insertHanyaTxTmBrand(txTmBrand, oldFileName);

                //Start delete and insert TxTmBrandDetail
                for (String delete : listDelete) {
                    brandService.deleteTxTmBrandDetail(delete, txTmBrand);
                }

                rootNode = mapper.readTree(listImageDetail);
                for (JsonNode node : rootNode) {
                    if (node.get(0).toString().replaceAll("\"", "").equals("")) {
                        String[] fileData = node.get(6).toString().replaceAll("\"", "").split(",");

                        TxTmBrandDetail txTmBrandDetail = new TxTmBrandDetail();
                        txTmBrandDetail.setTxTmGeneral(txTmGeneral);
                        txTmBrandDetail.setTxTmBrand(txTmBrand);
                        txTmBrandDetail.setFileName(node.get(3).toString().replaceAll("\"", ""));
                        txTmBrandDetail.setSize(node.get(4).toString().replaceAll("\"", ""));
                        txTmBrandDetail.setFileDescription(node.get(5).toString().replaceAll("\"", ""));
                        txTmBrandDetail.setUploadDate(tmstm);
                        txTmBrandDetail.setFileDataType(fileData[0]);

                        brandService.insertHanyaTxTmBrandDetail(txTmBrandDetail, fileData[1]);
                    }
                }
                // End delete and insert TxTmBrandDetail
                result.put("success", true);
                /*writeJsonResponse(response, 200);*/

            } catch (NullPointerException | IOException e) {
                logger.error(e.getMessage(), e);
                result.put("message", "Gagal menyimpan logo merek");
            }
        }
        writeJsonResponse(response, result);
    }

//    @RequestMapping(value = REQUEST_MAPPING_AJAX_LIST_IMAGE_DETAIL, method = {RequestMethod.POST})
//    public void doDataTablesImageDetail(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
//        if (isAjaxRequest(request)) {
//            setResponseAsJson(response);
//
//            DataTablesSearchResult dataTablesSearchResult = new DataTablesSearchResult();
//            try {
//                dataTablesSearchResult.setDraw(Integer.parseInt(request.getParameter("draw")));
//            } catch (NumberFormatException e) {
//                dataTablesSearchResult.setDraw(0);
//            }
//
//            int offset = 0;
//            int limit = 50;
//
//            List<KeyValue> searchCriteria = new ArrayList<>();
//            searchCriteria.add(new KeyValue("txTmGeneral.applicationNo", request.getParameter("appNo"), true));
//
//            String orderBy = "id";
//            String orderType = "ASC";
//
//            List<String[]> data = new ArrayList<>();
//
//            GenericSearchWrapper<TxTmBrandDetail> searchResult = brandService.searchBrandDetail(searchCriteria, orderBy, orderType, offset, limit);
//            if (searchResult.getCount() > 0) {
//                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
//                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());
//
//                int no = offset;
//                for (TxTmBrandDetail result : searchResult.getList()) {
//                    no++;
//
//                    String pathFolder = DateUtil.formatDate(result.getUploadDate(), "yyyy/MM/dd/");
//                    File file = new File(uploadFileBrandDetailPath + pathFolder + result.getId() + "." + FilenameUtils.getExtension(result.getFileName()));
//                    String image = "";
//
//                    if (file.exists() && !file.isDirectory()) {
//                        FileInputStream fileInputStreamReader = new FileInputStream(file);
//                        byte[] bytes = new byte[(int) file.length()];
//                        fileInputStreamReader.read(bytes);
//                        image = result.getFileDataType() + "," + Base64.getEncoder().encodeToString(bytes);
//                        fileInputStreamReader.close();
//                    }
//
//                    data.add(new String[]{
//                            result.getId(),
//                            "" + no,
//                            result.getUploadDateTemp(),
//                            result.getFileName(),
//                            result.getSize(),
//                            result.getFileDescription(),
//                            image,
//                            ""
//                    });
//                }
//            }
//
//            dataTablesSearchResult.setData(data);
//
//            writeJsonResponse(response, dataTablesSearchResult);
//        } else {
//            response.setStatus(HttpServletResponse.SC_FOUND);
//        }
//    }

    @RequestMapping(value = REQUEST_MAPPING_AJAX_SAVE_BIAYA, method = {RequestMethod.POST})
    public void doSaveFormBiaya(Model model, final HttpServletRequest request, final HttpServletResponse response,
                                @RequestParam("appNo") String appNo, @RequestParam("listBiayaNegara") String[] listBiayaNegara,
                                @RequestParam("basicFee") String basicFee, @RequestParam("volComp") String volComp, @RequestParam("totalComp") String totalComp,
                                @RequestParam("volSup") String volSup, @RequestParam("totalSup") String totalSup,
                                @RequestParam("language2") String language2, @RequestParam("totalBiaya") String totalBiaya) throws IOException, ParseException
    {

        TxTmGeneral txTmGeneral = trademarkService.selectOne("applicationNo", appNo);
        txTmGeneral.setLanguage2(language2);
        txTmGeneralRepository.save(txTmGeneral);

        // Check apakah TxTmGeneral ADA di TxTmMadridFee , jika tidak ada , buat segera
        String madrid_fee_id = "" ;

        TxTmMadridFee madridFee = new TxTmMadridFee();
        madridFee = madridOoService.saveMadridFee(txTmGeneral, new BigDecimal(basicFee), Integer.parseInt(volComp), new BigDecimal(totalComp), Integer.parseInt(volSup), new BigDecimal(totalSup), new BigDecimal(totalBiaya));
        madrid_fee_id = madridFee.getId();

        try{
            NativeQueryModel queryModel = new NativeQueryModel() ;
            queryModel.setTable_name("TX_TM_MADRID_FEE_DETAIL");
            ArrayList<KeyValueSelect> searchBy = new ArrayList <>();
            searchBy.add(new KeyValueSelect("TM_MADRID_FEE_ID",madrid_fee_id,"=", true,null));
            queryModel.setSearchBy(searchBy);
            nativeQueryRepository.deleteNative(queryModel);
        }
        catch (NullPointerException n){

        }


        madridOoService.saveMadridFeeDetail(madridFee, listBiayaNegara);

        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            // Algoritma mulai di sini


            writeJsonResponse(response, result);
        }

    }

    @PostMapping(REQUEST_MAPPING_TAMBAH_DOKUMEN)
    public void doSaveForm7(@ModelAttribute("form") TxTmDoc form, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            if (!errors.hasErrors()) {
                try {
                    Timestamp tmstm = new Timestamp(System.currentTimeMillis());
                    List<TxTmDoc> txTmDocList = new ArrayList<>();

                    request.getParameterValues("docid");
                    Map<String, String[]> hDoc = request.getParameterMap();
                    for (Map.Entry<String, String[]> entry : hDoc.entrySet()) {
                        String key = entry.getKey();
                        String[] value = entry.getValue();
                        if (key.contains("docid")) {
                            //System.out.println(key +" "+ value);
                            form = new TxTmDoc();
                            form.setCreatedDate(tmstm);
                            form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                            form.setTxTmGeneral(trademarkService.selectOneGeneralByApplicationNo(request.getParameter("_appNo")));
                            form.setmDocType(doclampiranService.getDocTypeById(value[0]));
                            form.setDescription(doclampiranService.getDocTypeById(value[0]).getName());
                            form.setStatus(value[1].equals("true"));
                            if (value[1].equals("true")) {
                                txTmDocList.add(form);
                            }
                        }
                    }

                    doclampiranService.saveDokumenLampiranPermohonan(txTmDocList, trademarkService.selectOneGeneralByApplicationNo(request.getParameter("_appNo")).getId());
                    //return PATH_POST_DOKUMEN;
                    writeJsonResponse(response, 200);
                } catch (DataIntegrityViolationException e) {
                    logger.error(e.getMessage(), e);
                    model.addAttribute("errorMessage", "Gagal menambahkan data lampiran");
                    writeJsonResponse(response, 500);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    writeJsonResponse(response, 500);
                }
            }

        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(value = REQUEST_MAPPING_AJAX_PEMOHON_LIST, method = {RequestMethod.GET})
    public void listPemohon(final Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            DataTablesSearchResult dataTablesSearchResult = new DataTablesSearchResult();
            try {
                dataTablesSearchResult.setDraw(Integer.parseInt(request.getParameter("draw")));
            } catch (NumberFormatException e) {
                dataTablesSearchResult.setDraw(0);
            }

            int offset = 0;
            int limit = 100;
            try {
                offset = Math.abs(Integer.parseInt(request.getParameter("start")));
            } catch (NumberFormatException e) {
            }
            try {
                limit = Math.abs(Integer.parseInt(request.getParameter("length")));
            } catch (NumberFormatException e) {
            }

            String[] searchByArr = request.getParameterValues("searchByArr2[]");
            String[] keywordArr = request.getParameterValues("keywordArr2[]");
            List<KeyValue> searchCriteria = null;

            if (searchByArr != null) {
                searchCriteria = new ArrayList<>();

//                if (!mUser.getId().equalsIgnoreCase("SUPER")) {
//                    searchCriteria.add(new KeyValue("createdBy", mUser, false));
//                }

                for (int i = 0; i < searchByArr.length; i++) {
                    String searchBy = searchByArr[i];
                    String value = null;
                    try {
                        value = keywordArr[i];
                    } catch (ArrayIndexOutOfBoundsException e) {
                    }
                    if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                        if (searchBy.equalsIgnoreCase("applicationDate")) {
                            try {
                                searchCriteria.add(new KeyValue(searchBy, DateUtil.toDate("dd/MM/yyyy", value), true));
                            } catch (ParseException e) {
                            }
                        } else {
                            if (StringUtils.isNotBlank(value)) {
                                searchCriteria.add(new KeyValue(searchBy, value, false));
                            }
                        }
                    }
                }
            }

            String orderBy = request.getParameter("orderBy");
            if (orderBy != null) {
                orderBy = orderBy.trim();
                if (orderBy.equalsIgnoreCase("")) {
                    orderBy = "orderBy";
                }
            }

            String orderType = request.getParameter("orderType");
            if (orderType == null) {
                orderType = "ASC";
            } else {
                orderType = orderType.trim();
                if (!orderType.equalsIgnoreCase("DESC")) {
                    orderType = "ASC";
                }
            }

            List<String[]> data = new ArrayList<>();

            GenericSearchWrapper<TxTmOwner> searchResult = null;
            if (searchCriteria.isEmpty()) {
                searchResult = ownerService.getAll(offset, limit);
            } else {
                searchResult = ownerService.searchGeneral(searchCriteria, orderBy, orderType, offset, limit);
            }
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxTmOwner result : searchResult.getList()) {
                    no++;
                    data.add(new String[]{
                            "" + no,
//                            result.getTxTmGeneral().getApplicationNo(),
                            result.getName(),
                            result.getmCountry().getName(),
                            result.getAddress(),
                            result.getPhone(),
                            "<button type='button' data-dismiss='modal' onClick='pilihPemohon(\"" + result.getId() + "\")'>Pilih</button>"
                    });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_PEMOHON)
    public void doGetDataPemohon(Model model, final HttpServletRequest request, final HttpServletResponse response) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            TxTmOwner txTmOwner = new TxTmOwner();
            TxTmOwner ownerResponse = new TxTmOwner();
            String req = request.getParameter("target");

            txTmOwner = permohonanService.selectOneOwnerById(req);
            ownerResponse.setNo(txTmOwner.getNo());
            ownerResponse.setName(txTmOwner.getName());
            ownerResponse.setNationality(new MCountry());
            ownerResponse.getNationality().setId(txTmOwner.getNationality().getId());
            ownerResponse.setOwnerType(txTmOwner.getOwnerType());
            ownerResponse.setLegalEntity(txTmOwner.getLegalEntity());
            ownerResponse.setEntitlement(txTmOwner.getEntitlement());
            ownerResponse.setCommercialAddress(txTmOwner.getCommercialAddress());
            ownerResponse.setmCountry(new MCountry());
            ownerResponse.getmCountry().setId(txTmOwner.getmCountry().getId());
            if (txTmOwner.getmProvince() != null) {
                ownerResponse.setmProvince(new MProvince());
                ownerResponse.getmProvince().setId(txTmOwner.getmProvince().getId());
            }
            if (txTmOwner.getmCity() != null) {
                ownerResponse.setmCity(new MCity());
                ownerResponse.getmCity().setId(txTmOwner.getmCity().getId());
            }
            ownerResponse.setAddress(txTmOwner.getAddress());
            ownerResponse.setZipCode(txTmOwner.getZipCode());
            ownerResponse.setPhone(txTmOwner.getPhone());
            ownerResponse.setEmail(txTmOwner.getEmail());

            writeJsonResponse(response, ownerResponse);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

}
