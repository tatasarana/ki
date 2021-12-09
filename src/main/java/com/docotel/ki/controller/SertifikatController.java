package com.docotel.ki.controller;

import com.beust.jcommander.internal.Lists;
import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.MLookup;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.transaction.TxGroupDetailCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxPubsJournalCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxRegistrationDetailCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmGeneralCustomRepository;
import com.docotel.ki.repository.transaction.TxGroupDetailRepository;
import com.docotel.ki.repository.transaction.TxTmGeneralRepository;
import com.docotel.ki.service.master.ClassService;
import com.docotel.ki.service.master.LawService;
import com.docotel.ki.service.master.LookupService;
import com.docotel.ki.service.master.RepresentativeService;
import com.docotel.ki.service.transaction.OwnerService;
import com.docotel.ki.service.transaction.PriorService;
import com.docotel.ki.service.transaction.RegistrationService;
import com.docotel.ki.service.transaction.TrademarkService;
import com.docotel.ki.signature.PDFSignatureFacade;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.util.ZxingUtil;
import com.docotel.ki.enumeration.ClassStatusEnum;
import com.docotel.ki.enumeration.PriorStatusEnum;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import nl.captcha.Captcha;
import nl.captcha.backgrounds.FlatColorBackgroundProducer;
import nl.captcha.noise.CurvedLineNoiseProducer;
import nl.captcha.text.producer.DefaultTextProducer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.LazyInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class SertifikatController extends BaseController {
    public static final String PATH_VALIDATE = "/validate";
    private static final String DIRECTORY_PAGE = "certificate/";
    private static final String PAGE_VALIDATE = DIRECTORY_PAGE + "validate";
	public static final String PATH_SERTIFIKAT_DIGITAL = "/sertifikat-merek";

	public static final String PATH_PUBLIKASI = "/publikasi-merek";

	public static final String PATH_PRINT_SERTIFIKAT_DIGITAL = "/print-sertifikat-merek";
	public static final String PATH_CAPTCHA = "/captcha-sertifikat-merek";
	public static final String PATH_GENERATE_CAPTCHA = "/generate-captcha-sertifikat";

	private static final String PAGE_SERTIFIKAT_DIGITAL = DIRECTORY_PAGE + "sertifikat-digital";
	private static final String PAGE_PUBLIKASI = "publikasi/publikasi-merek" ;

	private static final String PATH_PRINT = BaseController.PATH_AFTER_LOGIN + "/cetak-sertifikat";
    private static final String PATH_GENERATE = BaseController.PATH_AFTER_LOGIN + "/generate-sertifikat";
    private static final String PATH_REGENERATE = BaseController.PATH_AFTER_LOGIN + "/regenerate-sertifikat";
    private static final String PATH_REG_GENERATE = BaseController.PATH_AFTER_LOGIN + "/generate-registrasi-sertifikat";
    private static final String PATH_REG_EXTEND = BaseController.PATH_AFTER_LOGIN + "/extend-registrasi-sertifikat";
    private static final String PATH_REG_EDIT = BaseController.PATH_AFTER_LOGIN + "/edit-registrasi-sertifikat";
    private static final String PATH_REG_REMOVE = BaseController.PATH_AFTER_LOGIN + "/remove-registrasi-sertifikat";
    private static final String PATH_REG_GENERATE_MANUAL=BaseController.PATH_AFTER_LOGIN + "/manual-generate-registrasi-sertifikat";
    private static final String REQUEST_MAPPING_PRINT = PATH_PRINT + "*";
    private static final String REQUEST_MAPPING_GENERATE = PATH_GENERATE + "*";
    private static final String REQUEST_MAPPING_REGENERATE = PATH_REGENERATE + "*";
    private static final String REQUEST_MAPPING_REG_GENERATE = PATH_REG_GENERATE + "*";
    private static final String REQUEST_MAPPING_REG_EXTEND = PATH_REG_EXTEND + "*";
    private static final String REQUEST_MAPPING_REG_EDIT = PATH_REG_EDIT + "*";
    private static final String REQUEST_MAPPING_REG_REMOVE = PATH_REG_REMOVE + "*";
    private static final String REQUEST_MAPPING_VALIDATE = PATH_VALIDATE + "/{no}";
	private static final String REQUEST_AJAX_MAPPING_SERTIFIKAT_DIGITAL = PATH_SERTIFIKAT_DIGITAL + "*";
	private static final String REQUEST_AJAX_MAPPING_PUBLIKASI = PATH_PUBLIKASI + "*";

	private static final String REQUEST_AJAX_MAPPING_PRINT_SERTIFIKAT_DIGITAL = PATH_PRINT_SERTIFIKAT_DIGITAL + "*";
	private static final String REQUEST_AJAX_MAPPING_CAPTCHA = PATH_CAPTCHA + "*";
	private static final String REQUEST_AJAX_MAPPING_GENERATE_CAPTCHA = PATH_GENERATE_CAPTCHA + "*";
	private static final String REQUEST_MAPPING_REG_GENERATE_MANUAL=PATH_REG_GENERATE_MANUAL + "*";

	private static final String CAPTCHA_SECRET_KEY = "#TG*R25#*(HJsgad6a&*_";

    @Autowired
    private TrademarkService trademarkService;
    @Autowired
    private RegistrationService registrationService;
    @Autowired
	private TxRegistrationDetailCustomRepository txRegistrationDetailCustomRepository;
    @Autowired
	private TxPubsJournalCustomRepository txPubsJournalCustomRepository;
    @Autowired
    private RepresentativeService representativeService;
    @Autowired
	private TxTmGeneralRepository txTmGeneralRepository;
    @Autowired
	private TxTmGeneralCustomRepository txTmGeneralCustomRepository;
    @Autowired
	private TxGroupDetailCustomRepository txGroupDetailCustomRepository;
    @Autowired
    private OwnerService ownerService;
    @Autowired
    private ClassService classService;
    @Autowired
    private PriorService priorService;
    @Autowired
    private LookupService lookupService;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	ResourceLoader resourceLoader;
	@Value("${upload.file.web.image:}")
	private String pathImage;
    @Value("${server.contextPath}")
    private String serverContextPath;
    @Value("${server.port}")
    private String serverPort;
    @Value("${logo.qr.pengayoman}")
    private String logoQRPengayoman;
    @Value("${upload.file.path.signature:}")
    private String uploadFilePathSignature;
    @Value("${upload.file.brand.path:}")
    private String uploadFileBrandPath;
    @Value("${download.output.certificate.file.path:}")
    private String downloadOutputCertificatePath;
    @Value(("${certificate.file}"))
    private String CERTIFICATE_FILE;
    @Autowired
    private LawService lawService;

    @RequestMapping(path = REQUEST_MAPPING_REG_GENERATE, method = {RequestMethod.POST})
    public void doCertificateRegGenerate(@RequestBody TxTmGeneral txTmGeneral, final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            try {
                LocalDate startDate = LocalDate.parse(DateUtil.formatDate(txTmGeneral.getFilingDate(), "yyyy-MM-dd"));
                LocalDate endDate = startDate.plusYears(10);

				TxRegistration txRegistrationCurr = registrationService.selectOne(txTmGeneral.getId());
				if (txRegistrationCurr == null) {
					registrationService.insertRegistration(txTmGeneral, Timestamp.valueOf(startDate.atStartOfDay()), Timestamp.valueOf(endDate.atStartOfDay()));
				}
                TxRegistration txRegistration = txTmGeneral.getTxRegistration();
                txRegistration.setTxTmGeneral(null);
                txRegistration.setCreatedBy(null);

                writeJsonResponse(response, txRegistration);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_REG_GENERATE_MANUAL, method = {RequestMethod.POST})
    public void doCertificateRegGenerateManual(
    		@RequestParam("id") String generalId,
    		@RequestParam("noReg") String registrationNo, 
    		final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            try {
            	TxTmGeneral txTmGeneral=trademarkService.selectOneGeneralById(generalId);
                
            	LocalDate startDate = LocalDate.parse(DateUtil.formatDate(txTmGeneral.getFilingDate(), "yyyy-MM-dd"));
                LocalDate endDate = startDate.plusYears(10);

				TxRegistration txRegistrationCurr = registrationService.selectOne(txTmGeneral.getId());
				if (txRegistrationCurr == null) {
					registrationService.insertRegistrationManual(txTmGeneral,registrationNo, Timestamp.valueOf(startDate.atStartOfDay()), Timestamp.valueOf(endDate.atStartOfDay()));
				}
                TxRegistration txRegistration = txTmGeneral.getTxRegistration();
                txRegistration.setTxTmGeneral(null);
                txRegistration.setCreatedBy(null);

                writeJsonResponse(response, txRegistration);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }
    
    @RequestMapping(path = REQUEST_MAPPING_REG_EXTEND, method = {RequestMethod.POST})
    public void doCertificateRegExtend(@RequestBody TxTmGeneral txTmGeneral, final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            try {
                //LocalDate startDate = LocalDate.parse(DateUtil.formatDate(txTmGeneral.getTxRegistration().getEndDate(), "yyyy-MM-dd")).plusDays(1);
            	LocalDate startDate = LocalDate.parse(txTmGeneral.getTxRegistration().getEndDateTemp(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                LocalDate endDate = startDate.plusYears(10);

				if (txTmGeneral.getTxRegistration().getCreatedBy() == null) {
					txTmGeneral.getTxRegistration().setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
					txTmGeneral.getTxRegistration().setUpdatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
				}

                registrationService.extendRegistration(txTmGeneral, Timestamp.valueOf(startDate.atStartOfDay()), Timestamp.valueOf(endDate.atStartOfDay()));

                TxRegistration txRegistration = txTmGeneral.getTxRegistration();
                txRegistration.setTxTmGeneral(null);
                txRegistration.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
				txRegistration.setUpdatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

                writeJsonResponse(response, txRegistration);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_REG_EDIT, method = {RequestMethod.POST})
    public void doCertificateRegEdit(@RequestParam("id") String idGeneral, @RequestParam("no") String noReg,
    		 @RequestParam("startDateReg") String startDateReg, @RequestParam("endDateReg") String endDateReg,
    		final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            try {
                TxRegistration txRegistrationCurr = registrationService.selectOne(idGeneral);
                TxRegistration txRegistrationCheck = registrationService.selectOneBy("no", noReg); 
                
//                if (txRegistrationCheck == null || txRegistrationCurr.getId().equals(txRegistrationCheck.getId())) {
                	txRegistrationCurr.setNo(noReg);
                     
                	String pattern = "dd/MM/yyyy";
                	
                	LocalDate localStartDate = LocalDate.parse(startDateReg, DateTimeFormatter.ofPattern(pattern));
                	LocalDate localEndDate = LocalDate.parse(endDateReg, DateTimeFormatter.ofPattern(pattern));
                	
                	txRegistrationCurr.setStartDate( Timestamp.valueOf(localStartDate.atStartOfDay()) );
                	txRegistrationCurr.setEndDate( Timestamp.valueOf(localEndDate.atStartOfDay()) );
                	
                	registrationService.doUpdateRegistration(txRegistrationCurr);
                	writeJsonResponse(response, 200);
//                } else {
//                	writeJsonResponse(response, "EXIST");
//                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }
    
    @RequestMapping(path = REQUEST_MAPPING_REG_REMOVE, method = {RequestMethod.POST})
    public void doCertificateRegRemove(@RequestBody TxTmGeneral txTmGeneral, final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            try {
            	registrationService.removeRegistration(txTmGeneral);
            	TxRegistration txRegistration = txTmGeneral.getTxRegistration();
            	if (txRegistration != null) {
            		txRegistration.setTxTmGeneral(null);
					txRegistration.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            	}
            	writeJsonResponse(response, txRegistration);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_GENERATE, method = {RequestMethod.POST})
    public void doGenerateSertifikat(@RequestParam("no") String appNo, @RequestParam("downloadFlag") boolean downloadFlag, final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            try {
                if (!downloadFlag) {
                	doProcessGenerateSertifikat(appNo, downloadFlag, request);
                }
                writeJsonResponse(response, 200);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }
    
    @RequestMapping(path = REQUEST_MAPPING_REGENERATE, method = {RequestMethod.POST})
    public void doRegenerateSertifikat(@RequestParam("no") String appNo, final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            try {
            	doProcessGenerateSertifikat(appNo, true, request);
                writeJsonResponse(response, 200);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }
    
    private void doProcessGenerateSertifikat(String appNo, boolean downloadFlag, final HttpServletRequest request) {
    	try {
	    	List<KeyValue> searchCriteria = null;
	        ClassLoader classLoader = getClass().getClassLoader();
	
	        // Get TxTmGeneral Data
	        TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(appNo);

	        // Get Desc Undang Undang
			String lawDesc = txTmGeneral.getmLaw() != null ? txTmGeneral.getmLaw().getDesc() : "";

			//get Pasal name
			MLookup mLookup = lookupService.findByCodeGroups(txTmGeneral.getmLaw().getId(), "CetakSertifikat");
			String lookupName = mLookup != null ? mLookup.getName() : "-";

			// Get List Class
	        searchCriteria = new ArrayList<>();
	        searchCriteria.add(new KeyValue("txTmGeneral.id", txTmGeneral.getId(), true));
	        searchCriteria.add(new KeyValue("transactionStatus", ClassStatusEnum.ACCEPT.name(), true));
	        List<TxTmClass> txTmClassList = classService.selectAllTxTmClass(searchCriteria, "mClass.no", "ASC");
	        Map<Integer, String> mapClass = new HashMap<>();
	        int key;
	        String edition = "";
	        String value = "";
	        String desc = "";
	        for (TxTmClass txTmClass : txTmClassList) {
	        	edition = txTmClass.getmClass().getEdition();
	            key = txTmClass.getmClass().getNo();
	            desc = txTmClass.getmClassDetail().getDesc();
	
	            if (mapClass.containsKey(key)) {
	                value = mapClass.get(key);
	                value = value + "; " + desc;
	            } else {
	                value = desc;
	            }
	
	            mapClass.put(key, value);
	        }
	        Map<Integer, String> mapClassOrder = mapClass.entrySet().stream().sorted(Map.Entry.comparingByKey())
	        	.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
	                (oldValue, newValue) -> oldValue, LinkedHashMap::new));
	        StringBuffer sbClassNoList = new StringBuffer();
	        StringBuffer sbClassDescList = new StringBuffer();
	        for (Map.Entry<Integer, String> map : mapClassOrder.entrySet()) {
	            sbClassNoList.append(map.getKey());
	            sbClassNoList.append(", ");
	            
	            sbClassDescList.append("\nKelas " + map.getKey() + " :\n");
	            sbClassDescList.append("=== ");
	            sbClassDescList.append(map.getValue());
	            sbClassDescList.append(" ===");
	            sbClassDescList.append("\n");
	        }
	        String classNoList = "";
	        String classDescList = "";
	        if (sbClassNoList.length() > 0) {
	            classNoList = sbClassNoList.substring(0, sbClassNoList.length() - 2);
	        }
	        if (sbClassDescList.length() > 0) {
	            classDescList = sbClassDescList.substring(0, sbClassDescList.length() - 1);
	        }
	
	        // Set and Get Registration Data
	        //TxRegistration txRegistration = registrationService.selectOne(txTmGeneral.getId());
	        TxRegistrationDetail txRegistrationDetail = registrationService.selectDetailOne(txTmGeneral.getId());
	        TxRegistration txRegistration = txRegistrationDetail.getTxRegistration();
	        String registrationNo = txRegistration.getNo();
	        String registrationDate = DateUtil.formatDate(txRegistration.getCreatedDate(), "dd MMMM yyyy");
	
	        // Get Filling Date & Expire
	        String fillingDate = DateUtil.formatDate(txTmGeneral.getFilingDate(), "dd MMMM yyyy");
	        String expireDate = DateUtil.formatDate(txRegistration.getEndDate(), "dd MMMM yyyy");
	
	        // Get Owner data
	        searchCriteria = new ArrayList<>();
	        searchCriteria.add(new KeyValue("txTmGeneral.id", txTmGeneral.getId(), true));
	        searchCriteria.add(new KeyValue("status", true, true));
	        TxTmOwner txTmOwner = ownerService.selectOne(searchCriteria);
			String ownerName = txTmOwner.getName();
			// Get Owner Detail
			searchCriteria = new ArrayList<>();
	        searchCriteria.add(new KeyValue("txTmGeneral.id", txTmGeneral.getId(), true));
	        searchCriteria.add(new KeyValue("txTmOwner.id", txTmOwner.getId(), true));
	        searchCriteria.add(new KeyValue("status", true, true));
	        List<TxTmOwnerDetail> txTmOwnerDetailList = ownerService.selectAllDetail(searchCriteria, "name", "ASC");
	        String ownerDetailName = "";
	        int no = 1;
	        for (TxTmOwnerDetail result : txTmOwnerDetailList) {
	        	ownerDetailName += ("\n" + ++no + ". " + result.getName());
	        }
	        if (!ownerDetailName.equals("")) {
	        	ownerName = "1. " + ownerName;
	        }
			// Get Owner Address
			String ownerAddress = txTmOwner.getAddress();
			String ownerCity = txTmOwner.getmCity() != null ? txTmOwner.getmCity().getName() : "";
			String ownerProvince = txTmOwner.getmProvince() != null ? txTmOwner.getmProvince().getName() : "";
			String ownerCountry = txTmOwner.getmCountry() != null ? txTmOwner.getmCountry().getName() : "";
			String ownerZipCode = txTmOwner.getZipCode() != null ? txTmOwner.getZipCode().trim() : "";
			String ownerCityProvince = (ownerCity.equals("") ? "" : ownerCity) + (ownerProvince.equals("") ? "" : (ownerCity.equals("") ? "" : ", " + ownerProvince));
			String ownerCountryZipCode = (ownerCountry.equals("") ? "" : ownerCountry) + (ownerZipCode.equals("") ? "" : (ownerCountry.equals("") ? "" : ", " + ownerZipCode));
			String ownerFullAddress = "\n" + ownerAddress + (ownerCityProvince.equals("") ? "" : (",\n" + ownerCityProvince)) + (ownerCountryZipCode.equals("") ? "" : (",\n" + ownerCountryZipCode));
			// Get Owner Post Address
			String ownerPostAddress = txTmOwner.getPostAddress() != null ? txTmOwner.getPostAddress() : "";
			String ownerPostCity = txTmOwner.getPostCity() != null ? txTmOwner.getPostCity().getName() : "";
			String ownerPostProvince = txTmOwner.getPostProvince() != null ? txTmOwner.getPostProvince().getName() : "";
			String ownerPostCountry = txTmOwner.getPostCountry() != null ? txTmOwner.getPostCountry().getName() : "";
			String ownerPostZipCode = txTmOwner.getPostZipCode() != null ? txTmOwner.getPostZipCode().trim() : "";
			String ownerPostCityProvince = (ownerPostCity.equals("") ? "" : ownerPostCity) + (ownerPostProvince.equals("") ? "" : (ownerPostCity.equals("") ? "" : ", " + ownerPostProvince));
			String ownerPostCountryZipCode = (ownerPostCountry.equals("") ? "" : ownerPostCountry) + (ownerPostZipCode.equals("") ? "" : (ownerPostCountry.equals("") ? "" : ", " + ownerPostZipCode));
			String ownerPostFullAddress = ownerPostAddress + (ownerPostCityProvince.equals("") ? "" : (",\n" + ownerPostCityProvince)) + (ownerPostCountryZipCode.equals("") ? "" : (",\n" + ownerPostCountryZipCode));
			ownerPostFullAddress = ownerPostFullAddress.equals("") ? "" : "\nAlamat Surat Menyurat:\n" + ownerPostFullAddress;
			
	        // Get Representative data (HOLD DULU KARENA IMPACT DENGAN PERUBAHAN MRepresentative di TxTmGeneral menjadi List)
			searchCriteria = new ArrayList<>();
	        searchCriteria.add(new KeyValue("txTmGeneral.id", txTmGeneral.getId(), true));
	        searchCriteria.add(new KeyValue("status", true, true));
	        TxTmRepresentative txTmRepresentative = representativeService.selectOneTxTmRepresentative(searchCriteria, "id", "ASC");
	        String reprsName = txTmRepresentative == null ? "" : txTmRepresentative.getName();
			String reprsAddress = txTmRepresentative == null ? "" : txTmRepresentative.getAddress();
	
	        // Get Brand Data
	        String brandName = txTmGeneral.getTxTmBrand().getName() == null ? "" : txTmGeneral.getTxTmBrand().getName();
	        String brandColor = txTmGeneral.getTxTmBrand().getColor() == null ? "" : txTmGeneral.getTxTmBrand().getColor();
	        String brandTranslation = txTmGeneral.getTxTmBrand().getTranslation() == null ? "" : txTmGeneral.getTxTmBrand().getTranslation();
			String brandDisclaimer = txTmGeneral.getTxTmBrand().getDisclaimer() == null ? "" : txTmGeneral.getTxTmBrand().getDisclaimer();

			/*Format Image untuk brandlogo/ yg menggunakan "uploadFileBrandPath" adalah uuid.jpg ex. 8ad78671-009d-47b6-bb62-29f82fc97242.jpg*/
	        String pathFolder = DateUtil.formatDate(txTmGeneral.getTxTmBrand().getCreatedDate(), "yyyy/MM/dd/");
	        String brandLogo = uploadFileBrandPath + pathFolder + txTmGeneral.getTxTmBrand().getId() + ".jpg" ;
	
	        // Generate QR Code
	        String url = request.getRequestURL().toString();
	        String uri = request.getRequestURI();
	        String qrText = url.replaceAll(uri, "") + getPathURL(PATH_VALIDATE + "/" + txTmGeneral.getApplicationNo());
	        byte[] qrData = ZxingUtil.textToQrCode(qrText, new File(logoQRPengayoman), 125, 125, 30, 30);
	        Path path = Files.createTempFile(txTmGeneral.getApplicationNo(), ".png");
	        FileUtils.writeByteArrayToFile(path.toFile(), qrData);
	        final InputStream qrCode = new FileInputStream(path.toFile());
	
	        // Get Prior data
	        searchCriteria = new ArrayList<>();
	        searchCriteria.add(new KeyValue("txTmGeneral.id", txTmGeneral.getId(), true));
	        searchCriteria.add(new KeyValue("status", PriorStatusEnum.ACCEPT.name(), true));
	        List<TxTmPrior> txTmPriorList = priorService.selectAllTxTmPrior(searchCriteria, "priorDate", "ASC");
	        List<Map<String, String>> priors = new ArrayList<Map<String, String>>();
	        if (txTmPriorList.size() == 0) {
	            Map<String, String> priorMap = new HashMap<String, String>();
	            priorMap.put("count", "");
	            priorMap.put("no", "");
	            priorMap.put("date", "");
	            priorMap.put("country", "");
	            priors.add(priorMap);
	        } else {
	            int counter = 1;
	            for (TxTmPrior prior : txTmPriorList) {
	                Map<String, String> priorMap = new HashMap<String, String>();
	                priorMap.put("count", "" + counter++);
	                priorMap.put("no", prior.getNo());
	                priorMap.put("date", DateUtil.formatDate(prior.getPriorDate(), "dd/MM/yyyy"));
	                priorMap.put("country", prior.getmCountry().getCode());
	                priors.add(priorMap);
	            }
	        }
	
	        List<Map<String, String>> listData = new ArrayList<Map<String, String>>();
	        for (int i = 1; i <= 2; i++) {
	            Map<String, String> rowMap = new HashMap<String, String>();
	            rowMap.put("applicationNo", txTmGeneral.getApplicationNo());
	            rowMap.put("applicationDate", DateUtil.formatDate(txTmGeneral.getTxReception().getApplicationDate(), "dd MMMM yyyy"));
	            rowMap.put("fillingDate", fillingDate);
	            rowMap.put("expireDate", expireDate);
	            rowMap.put("registrationNo", registrationNo);
	            rowMap.put("registrationDate", registrationDate);
	            rowMap.put("ownerData", ownerName + ownerDetailName + ownerFullAddress + ownerPostFullAddress);
	            rowMap.put("reprsName", reprsName);
				rowMap.put("reprsAddress", reprsAddress);
	            rowMap.put("brandName", brandName);
	            rowMap.put("brandColor", brandColor);
	            rowMap.put("brandTranslation", brandTranslation);
	            rowMap.put("brandLogo", brandLogo);
				rowMap.put("brandDisclaimer", brandDisclaimer);
				rowMap.put("classNo", classNoList);
	            rowMap.put("classDesc", classDescList);
	            rowMap.put("classEdition", edition);
				rowMap.put("lawDesc", lawDesc);
				rowMap.put("lookupName", lookupName);
	            rowMap.put("flag", "" + i);
	            listData.add(rowMap);
	        }
	
	        Map<String, Object> params = new HashMap<String, Object>();
	        params.put("qrCode", qrCode);
	        params.put("dataPrior", new JRBeanCollectionDataSource(priors));
	        params.put("uploadFilePath", uploadFilePathSignature);
			params.put("bgSurat", pathImage + "bg-surat-non-garuda.png");
			params.put("garuda", pathImage + "garuda-01b.png");
			params.put("stamp", uploadFilePathSignature + "logo kemenkumham.png");

	        List<MLookup> listDataParam = lookupService.selectAllbyGroup("Dirjen");
	        for (MLookup result : listDataParam) {
	            params.put(result.getCode(), result.getName());
	        }

			Resource resource = resourceLoader.getResource("classpath:report/CetakSertifikat_v2.jasper");
			File file = resource.getFile();
	        InputStream jasperStream1 = new FileInputStream(file);
	        JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream1);
	        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JRBeanCollectionDataSource(listData));
	
	        String folder = downloadOutputCertificatePath + DateUtil.formatDate(txRegistrationDetail.getCreatedDate(), "yyyy/MM/");
	        String fileName = txRegistration.getNo() + ".pdf";
	
	        Path pathDir = Paths.get(folder);
	        Path pathFile = Paths.get(folder + fileName);
	        if (!Files.exists(pathDir)) {
	            Files.createDirectories(pathDir);
	        }
	        if (!Files.exists(pathFile)) {
	            Files.createFile(pathFile);
	        }
	
	        OutputStream outputStream = new FileOutputStream(pathFile.toFile());
	
	        byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
	        InputStream is = new ByteArrayInputStream(pdfBytes);
	        this.signPdf(is, outputStream);
	        outputStream.close();
	        
	        //Set true Registration Download Flag
	        if (!downloadFlag) {
	            txRegistration.setDownloadFlag(true);
	            registrationService.doUpdateRegistration(txRegistration);
	        }
    	} catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    

    @GetMapping(path = REQUEST_MAPPING_PRINT)
    public void doPrintCertificatePDF(@RequestParam(value = "no", required = false) String no, final HttpServletRequest request, final HttpServletResponse response) {
        // Get TxTmGeneral Data
        TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(no);
        //Get Registration Data
        //TxRegistration txRegistration = registrationService.selectOne(txTmGeneral.getId());
        TxRegistrationDetail txRegistrationDetail = registrationService.selectDetailOne(txTmGeneral.getId());
        TxRegistration txRegistration = txRegistrationDetail.getTxRegistration();
        
        response.setContentType("application/pdf");//x-pdf-> download, -pdf->open new window browser
        response.setHeader("Content-disposition", "inline; filename=Sertifikat-" + txTmGeneral.getApplicationNo() + ".pdf");

        String folder = downloadOutputCertificatePath + DateUtil.formatDate(txRegistrationDetail.getCreatedDate(), "yyyy/MM/");
        String fileName = txRegistration.getNo() + ".pdf";

        try (OutputStream out = response.getOutputStream()) {
            Path path = Paths.get(folder + fileName);
            Files.copy(path, out);
            out.flush();
        } catch (IOException e) {
            // handle exception
        }
    }

    private void signPdf(InputStream input, OutputStream output) {
        String key = CERTIFICATE_FILE + "eAdministrasi.p12";
        //System.out.println("PATH : " + key);
        try {
            PDFSignatureFacade facade = new PDFSignatureFacade();
            facade.sign(key, "JakartaPP123!@#", input, output, true, new java.awt.Rectangle(250, 0, 400, 25));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @GetMapping(REQUEST_MAPPING_VALIDATE)
    public String doShowValidatePage(@PathVariable(name = "no", required = false) String no, final Model model) {
        TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(no);
        if (txTmGeneral != null) {
            txTmGeneral.setTxRegistration(registrationService.selectOne(txTmGeneral.getId()));
            // HOLD DULU KARENA IMPACT DENGAN PERUBAHAN MRepresentative di TxTmGeneral menjadi List //
			/*if (txTmGeneral.getTxTmRepresentative() != null && txTmGeneral.getTxTmRepresentative().getmRepresentative() != null) {
				txTmGeneral.getTxTmRepresentative().setmRepresentative(representativeService.selectOne("id", txTmGeneral.getTxTmRepresentative().getmRepresentative().getCurrentId()));
			}*/
        }

        model.addAttribute("txTmGeneral", txTmGeneral);
        return PAGE_VALIDATE;
    }

    @GetMapping(PATH_SERTIFIKAT_DIGITAL)
	public String doShowSetifikatDigitalPage() {

//		model.addAttribute("imgCaptcha", imgCaptcha);
//		model.addAttribute("captchaKey", captchaKey);
    	return PAGE_SERTIFIKAT_DIGITAL;
	}

	@PostMapping(REQUEST_AJAX_MAPPING_SERTIFIKAT_DIGITAL)
	public void doSearchDataTablesList(final HttpServletRequest request, final HttpServletResponse response) {
		if (isAjaxRequest(request)) {
			setResponseAsJson(response);

			DataTablesSearchResult dataTablesSearchResult = new DataTablesSearchResult();
			try {
				dataTablesSearchResult.setDraw(Integer.parseInt(request.getParameter("draw")));
			} catch (NumberFormatException e) {
				dataTablesSearchResult.setDraw(0);
			}

			int offset = 0;
			int limit = 50;
			try {
				offset = Math.abs(Integer.parseInt(request.getParameter("start")));
			} catch (NumberFormatException e) {
			}
			try {
				limit = Math.abs(Integer.parseInt(request.getParameter("length")));
			} catch (NumberFormatException e) {
			}

			String search = request.getParameter("searchVal");
			List<KeyValue> searchCriteria = null;

			searchCriteria = new ArrayList<>();
			if (search != null && !search.equalsIgnoreCase("")) {
				searchCriteria.add(new KeyValue("txTmGeneral.applicationNo", search, false));
				searchCriteria.add(new KeyValue("txTmGeneral.txTmBrand.name", search, false));
				searchCriteria.add(new KeyValue("txTmGeneral.classList", search, false));
				searchCriteria.add(new KeyValue("txTmGeneral.ownerList", search, false));
				searchCriteria.add(new KeyValue("txTmGeneral.represList", search, false));
			}

			String orderBy = request.getParameter("order[0][column]");
			if (orderBy != null) {
				orderBy = orderBy.trim();
				if (orderBy.equalsIgnoreCase("")) {
					orderBy = null;
				} else {
					switch (orderBy) {
						case "1":
							orderBy = "txTmGeneral.applicationNo";
							break;
						case "2":
							orderBy = "txTmGeneral.filingDate";
							break;
						case "3":
							orderBy = "txTmGeneral.txTmBrand.name";
							break;
						case "4":
							orderBy = "txTmGeneral.classList";
							break;
						case "5":
							orderBy = "txTmGeneral.ownerList";
							break;
						case "6":
							orderBy = "txTmGeneral.represList";
							break;
					}
				}
			}

			String orderType = request.getParameter("order[0][dir]");
			if (orderType == null) {
				orderType = "ASC";
			} else {
				orderType = orderType.trim();
				if (!orderType.equalsIgnoreCase("DESC")) {
					orderType = "ASC";
				}
			}

			List<String[]> data = new ArrayList<>();

			GenericSearchWrapper<TxRegistration> searchResult = registrationService.selectAllSertifikatDigital(searchCriteria, orderBy, orderType, offset, limit);
			if (searchResult.getCount() > 0) {
				dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
				dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

				int no = offset;
				if (searchResult != null) {
					for (TxRegistration result: searchResult.getList()) {
						no++;
						String nomorPermohonan = result.getTxTmGeneral().getApplicationNo();
						String tanggalPenerimaan = DateUtil.formatDate(result.getTxTmGeneral().getFilingDate(), "dd/MM/yyyy");
						String merek = result.getTxTmGeneral().getTxTmBrand().getName();
						String kelas = result.getTxTmGeneral().getClassList();
						String namaPemohon = result.getTxTmGeneral().getOwnerList();
						String namaKuasa = result.getTxTmGeneral().getRepresList();

						data.add(new String[]{
								"" + no,
								nomorPermohonan,
								tanggalPenerimaan,
								merek,
								kelas,
								namaPemohon,
								namaKuasa != null ? namaKuasa : "-"
								//"<button onclick=\"showModal('"+ nomorPermohonan +"')\" class='btn btn-primary'>Download</button>"
						});
					}
				}
			}

			dataTablesSearchResult.setData(data);

			writeJsonResponse(response, dataTablesSearchResult);
		} else {
			response.setStatus(HttpServletResponse.SC_FOUND);
		}
	}

	@GetMapping(PATH_PUBLIKASI)
	public String doShowPublikasiPage() {
		return PAGE_PUBLIKASI;
	}

	@RequestMapping(value = REQUEST_AJAX_MAPPING_PUBLIKASI, method = {RequestMethod.POST})
	public void doSearchPubTablesList(final HttpServletRequest request, final HttpServletResponse response) throws ParseException {
    	System.out.println("Publikasi-merek");
		if (isAjaxRequest(request)) {
			System.out.println("isAjaxRequest");
			setResponseAsJson(response);

			DataTablesSearchResult dataTablesSearchResult = new DataTablesSearchResult();
			try {
				dataTablesSearchResult.setDraw(Integer.parseInt(request.getParameter("draw")));
			} catch (NumberFormatException e) {
				dataTablesSearchResult.setDraw(0);
			}

			int offset = 0;
			int limit = 50;
			try {
				offset = Math.abs(Integer.parseInt(request.getParameter("start")));
			} catch (NumberFormatException e) {
			}
			try {
				limit = Math.abs(Integer.parseInt(request.getParameter("length")));
			} catch (NumberFormatException e) {
			}

			String[] searchByArr = request.getParameterValues("searchByArr[]");
			String[] keywordArr = request.getParameterValues("keywordArr[]");

			List<KeyValue> searchCriteria = null;
			String search = request.getParameter("searchVal");

			searchCriteria = new ArrayList<>();
			if (searchByArr != null) {
				for (int i = 0; i < searchByArr.length; i++) {
					String searchBy = searchByArr[i];
					String value = keywordArr[i];
					try {
						value = keywordArr[i];
					} catch (ArrayIndexOutOfBoundsException e) {
					}
					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (searchBy.equalsIgnoreCase("journalEnd")) {
							SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
							Date currDate = new Date();
							try{
								currDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(value);
							} catch (ParseException e) {
								System.out.println("Using:"+currDate.toString());
							}
							searchCriteria.add(new KeyValue(searchBy, currDate, false));
						} else {
							if (StringUtils.isNotBlank(value)) {
								searchCriteria.add(new KeyValue(searchBy, value, false));
							}
						}
					}
				}
			}
			if (search != null && !search.equalsIgnoreCase("")) {
				searchCriteria.add(new KeyValue("applicationNo", search, false));
				searchCriteria.add(new KeyValue("txTmBrand.name", search, false));
				searchCriteria.add(new KeyValue("classList", search, false));
				searchCriteria.add(new KeyValue("ownerList", search, false));
				searchCriteria.add(new KeyValue("represList", search, false));
                searchCriteria.add(new KeyValue("journalNo", search, false));
			}


			String orderBy = request.getParameter("order[0][column]");
			if (orderBy != null) {
				orderBy = orderBy.trim();
				if (orderBy.equalsIgnoreCase("")) {
					orderBy = null;
				} else {
					switch (orderBy) {
						case "1":
							orderBy = "applicationNo";
							break;
						case "2":
							orderBy = "filingDate";
							break;
						case "3":
							orderBy = "txTmBrand.name";
							break;
						case "4":
							orderBy = "classList";
							break;
						case "5":
							orderBy = "ownerList";
							break;
						case "6":
							orderBy = "represList";
							break;
					}
				}
			}

			String orderType = request.getParameter("order[0][dir]");
			if (orderType == null) {
				orderType = "ASC";
			} else {
				orderType = orderType.trim();
				if (!orderType.equalsIgnoreCase("DESC")) {
					orderType = "ASC";
				}
			}

			List<String[]> data = new ArrayList<>();
			//GenericSearchWrapper<TxPubsJournal> searchResult = txPubsJournalCustomRepository.selectAllPub(searchCriteria, orderBy, orderType, offset, limit);
			GenericSearchWrapper<TxTmGeneral> searchResult = txTmGeneralCustomRepository.selectAllPub(searchCriteria, orderBy, orderType, offset, limit);

			if (searchResult != null && searchResult.getCount() > 0) {
				dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
				dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

				int no = 0;
//					for (TxRegistration result: searchResult.getList()) {
//					for (TxPubsJournal journal: searchResult.getList()) {
//						for( TxGroupDetail result: journal.getTxGroup().getTxGroupDetailList() ) {
					for(TxTmGeneral result: searchResult.getList()) {
					    System.out.println("FOUND:" + result.getApplicationNo());
                            TxTmGeneral txTmGeneral = result;
                            TxPubsJournal txPubsJournal = txPubsJournalCustomRepository.selectOneByApplicationNo(result.getApplicationNo());

							if(txPubsJournal==null){
							    System.out.println("NULL JOURNAL");
                                continue;
                            }

                            no++;
							String noBRM = txPubsJournal.getJournalNo();
							String tanggalPublikasiStart = DateUtil.formatDate(txPubsJournal.getJournalStart(), "dd/MM/yyyy");
							String tanggalPublikasiEnd = DateUtil.formatDate(txPubsJournal.getJournalEnd(), "dd/MM/yyyy");
							String nomorPermohonan = txTmGeneral.getApplicationNo();
							String tanggalPenerimaan = DateUtil.formatDate(txTmGeneral.getFilingDate(), "dd/MM/yyyy");
                            String namaPemohon = "";
                            String alamatPemohon = "";
                            String namaKuasa = "";
                            String merek = "";
							String kelas = "";
							List<KeyValue> Criteria = new ArrayList<>();
							Criteria.add(new KeyValue("id", result.getId(), false));
							//List<TxGroupDetail> txGroupDetails = txGroupDetailCustomRepository.selectReportEkspedisi(Criteria, "id", "DESC", 0, null);
							if(txTmGeneral!=null) {
							    TxTmBrand txTmBrand = txTmGeneral.getTxTmBrand();
							    Set<TxTmOwner> txTmOwners = txTmGeneral.getTxTmOwner();
                                Set<TxTmRepresentative> txTmRepresentative = txTmGeneral.getTxTmRepresentative();
								if(txTmBrand!=null) {
									merek = txTmBrand.getName();
								}
								if(txTmGeneral.getClassList()!=null) {
									kelas = txTmGeneral.getClassList();
								}
                                if(txTmOwners!=null) {
                                    namaPemohon = txTmGeneral.getOwnerList();
                                }
                                if(txTmOwners.size()>0) {
                                    alamatPemohon = Lists.newArrayList(txTmOwners).get(0).getAddress();
                                }
                                if(txTmRepresentative!=null) {
                                    namaKuasa = txTmGeneral.getRepresList();
                                }
                            }

							data.add(new String[]{
									"" + no,
									noBRM,
									tanggalPublikasiStart,
									tanggalPublikasiEnd,
									nomorPermohonan,
									tanggalPenerimaan,
									merek,
									kelas,
                                    namaPemohon,
                                    alamatPemohon,
                                    namaKuasa
							});
						}

			}
			dataTablesSearchResult.setData(data);

			writeJsonResponse(response, dataTablesSearchResult);
		} else {
			response.setStatus(HttpServletResponse.SC_FOUND);
		}
	}


	@GetMapping(REQUEST_AJAX_MAPPING_GENERATE_CAPTCHA)
	public void doGenerateCaptcha(final HttpServletResponse response) {
		Captcha captcha = new Captcha.Builder(240, 64)
				.addText(new DefaultTextProducer(6, new char[] {
						'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'K', 'L', 'M', 'N', 'P', 'R',
						'W', 'X', 'Y', '2', '3', '4', '5', '6', '7', '8', '9'
				}))
				.addBackground(new FlatColorBackgroundProducer( Color.WHITE))
				.addNoise(new CurvedLineNoiseProducer())
				.addBorder()
				.build();

		String imgCaptcha = "";
		String captchaKey = "";
		try {
			BufferedImage originalImage = captcha.getImage();
			captchaKey = passwordEncoder.encode(CAPTCHA_SECRET_KEY + captcha.getAnswer().toLowerCase());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(originalImage, "jpg", baos);
			baos.flush();
			byte[] imageInByte = baos.toByteArray();
			baos.close();

			imgCaptcha = "data:image/png;base64, " + Base64.getEncoder().encodeToString(imageInByte);
		} catch (IOException e) {
		}

		Map<String, String> captchaJson = new HashMap<>();
		captchaJson.put("imgCaptcha", imgCaptcha);
		captchaJson.put("captchaKey", captchaKey);

		writeJsonResponse(response, captchaJson);
	}

	@PostMapping(REQUEST_AJAX_MAPPING_CAPTCHA)
	public void checkCaptchaDigitalCertificate(final HttpServletRequest request, final HttpServletResponse response) {
		String captchaAnswer = request.getParameter("captchaAnswer");
		String captchaKey = request.getParameter("captchaKey");

		if (passwordEncoder.matches(CAPTCHA_SECRET_KEY + captchaAnswer.toLowerCase(), captchaKey)) {
			writeJsonResponse(response, true);
		} else {
			writeJsonResponse(response, false);
		}
	}

	@PostMapping(path = REQUEST_AJAX_MAPPING_PRINT_SERTIFIKAT_DIGITAL)
	public void doPrintDigitalCertificatePDF(@RequestParam(value = "no", required = false) String no, final HttpServletRequest request, final HttpServletResponse response) {
		// Get TxTmGeneral Data
		TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(no);
		//Get Registration Data
		TxRegistration txRegistration = registrationService.selectOne(txTmGeneral.getId());

		response.setContentType("application/pdf");//x-pdf-> download, -pdf->open new window browser
		response.setHeader("Content-disposition", "inline; filename=Sertifikat-" + txTmGeneral.getApplicationNo() + ".pdf");
		String folder = downloadOutputCertificatePath + DateUtil.formatDate(txRegistration.getCreatedDate(), "yyyy/MM/");
		String fileName = txRegistration.getNo() + ".pdf";

		try (OutputStream out = response.getOutputStream()) {
			Path path = Paths.get(folder + fileName);
			Files.copy(path, out);
			out.flush();
		} catch (FileNotFoundException e) {
			logger.info("Not found: " + fileName);
		} catch (IOException e) {
			logger.info("error: " + e);
		}

	}
}
