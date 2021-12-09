package com.docotel.ki.controller.online;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.transaction.TxOnlineReg;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.online.jpa.RegistrasiOnlineRepository;
import com.docotel.ki.service.EmailService;
import com.docotel.ki.service.master.*;
import com.docotel.ki.service.transaction.RegistrasiOnlineService;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.util.FieldValidationUtil;
import com.docotel.ki.enumeration.RegistrasiStatusEnum;
import com.docotel.ki.model.master.MCity;
import com.docotel.ki.model.master.MCountry;
import com.docotel.ki.model.master.MFileSequence;
import com.docotel.ki.model.master.MLookup;
import com.docotel.ki.model.master.MProvince;
import com.docotel.ki.model.master.MRepresentative;
import com.docotel.ki.model.master.MUser;
import com.sun.org.apache.bcel.internal.generic.NEW;
import nl.captcha.Captcha;
import nl.captcha.backgrounds.FlatColorBackgroundProducer;
import nl.captcha.noise.CurvedLineNoiseProducer;
import nl.captcha.text.producer.DefaultTextProducer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Controller
public class RegistrasiOnlineController extends BaseController {
    @Autowired
    LookupService lookupService;
    @Autowired
    CountryService countryService;
    @Autowired
    ProvinceService provinceService;
    @Autowired
    CityService cityService;
    @Autowired
    RepresentativeService representativeService;

    @Autowired
    RegistrasiOnlineRepository registrasiOnlineRepository;

    @Autowired
    RESTService restService;

    @Autowired
    RegistrasiOnlineService registrasiOnlineService;
    @Autowired
    EmailService emailService;
    @Autowired
    UserService userService;
    @Autowired
    private FileService fileService;

    
    @Value("${upload.file.web.image:}")
    private String pathImage;

    @Value("${upload.file.logoemail.image:}")
    private String logoEmailImage;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private static final String DIRECTORY_PAGE = "reg-online/";
    private static final String CAPTCHA_SECRET_KEY = "#TG*R25#*(HJsgad6a&*_";
    
    private static final String TEMPLATE_EMAIL_REGISTRASI = "eTemplateRegistrasi";
    private static final String TEMPLATE_EMAIL_REGISTRASI_VERIFY = "eTemplateRegistrasiVerify";
    private static final String TEMPLATE_EMAIL_REGISTRASI_APPROVE = "eTemplateRegistrasiApprove";

    private static final String SUBJECT_EMAIL_REGISTRASI = "DJKI - Pendaftaran Akun Aplikasi";
    private static final String SUBJECT_EMAIL_REGISTRASI_VERIFY = "DJKI - Verifikasi Akun Aplikasi";
    private static final String SUBJECT_EMAIL_REGISTRASI_APPROVE = "DJKI - Akun Aplikasi Berhasil Didaftarkan";

    private static final String PAGE_REGISTRASI = DIRECTORY_PAGE + "registrasi-online";
    private static final String PAGE_RESEND = DIRECTORY_PAGE + "registrasi-online-resend";

    private static final String PATH_AJAX_SEARCH_LIST = "/cari-daftar-online";
    
    private static final String PATH_LOGIN = "/login";
    private static final String PATH_SEND_EMAIL_SUCCESS = PATH_LOGIN + "?email=ok";
    private static final String PATH_CHANGE_REG_SUCCESS = PATH_LOGIN + "?reg=ok";
    private static final String PATH_CHANGE_REG_EXPIRED = PATH_LOGIN + "?reg=expired";
    private static final String PATH_CHANGE_REG_REJECTED = PATH_LOGIN + "?reg=rejected";
    private static final String PATH_REGISTRASI = "/daftar-online";
    private static final String PATH_RESEND = "/kirim-ulang-aktivasi";

    public static final String PATH_AKTIVASI = "/aktivasi-user";

    private static final String REDIRECT_SEND_EMAIL_SUCCESS = "redirect:" + PATH_SEND_EMAIL_SUCCESS;
    private static final String REDIRECT_LOGIN = "redirect:" + PATH_LOGIN;
    private static final String REDIRECT_REGISTRATION_SUCCESS = "redirect:" + PATH_CHANGE_REG_SUCCESS;
    private static final String REDIRECT_REGISTRATION_EXPIRED = "redirect:" + PATH_CHANGE_REG_EXPIRED;
    private static final String REDIRECT_REGISTRATION_REJECTED = "redirect:" + PATH_CHANGE_REG_REJECTED;

    private static final String REQUEST_MAPPING_AJAX_SEARCH_LIST = PATH_AFTER_LOGIN + PATH_AJAX_SEARCH_LIST + "*";


    @ModelAttribute 
	public void addModelAttribute(final Model model, final HttpServletRequest request) {
		model.addAttribute("menu", "usermgmnt");
		model.addAttribute("subMenu", "listRegOnline");
	}
    
    @GetMapping(path = PATH_REGISTRASI)
    public String showPageDaftarOnline(Model model) {
        model.addAttribute("form", new TxOnlineReg());
        //model.addAttribute("imgKtp", "");
        model.addAttribute("imgCard", "");

	    initiatePage(model);

        return PAGE_REGISTRASI;
    }

    @PostMapping(value = PATH_REGISTRASI)
    public String doDaftarOnline(@ModelAttribute("form") TxOnlineReg form,
//                                 @RequestParam(value = "fileKtp", required = false) MultipartFile fileKtp,
                                 @RequestParam(value = "fileCard", required = false) MultipartFile fileCard,
                                 @RequestParam(value = "ckSetuju", required = false) Boolean ckSetuju,
                                 BindingResult errors, Model model, HttpServletRequest request, HttpServletResponse response) throws IOException {
    	boolean isValid = true;

        boolean PASS = true ;
        String ErrorMessage = "" ;
        boolean NEW_REPS = false ;
    	
        String captchaAnswer = request.getParameter("captchaAnswer");
        String captchaKey = request.getParameter("captchaKey");

        if(ckSetuju==null || !ckSetuju) {
        	model.addAttribute("ckSetujuError", getMessage("field.error.required", new Object[]{"Pernyataan"}));
        	isValid = false;
        }
        
        if (StringUtils.isBlank(captchaKey) || !passwordEncoder.matches(CAPTCHA_SECRET_KEY + captchaAnswer.toLowerCase(), captchaKey)) {
	        model.addAttribute("captchaError", getMessage("field.error.invalid.value", new Object[]{"Captcha"}));
	        isValid = false;
		}

		FieldValidationUtil.required(errors, "email", form.getEmail(), "Email");
        if (!errors.hasFieldErrors("email")) {
        	FieldValidationUtil.email(errors, "email", form.getEmail(), "Email");            
        }
        FieldValidationUtil.required(errors, "password", form.getPassword(), "Password");
        FieldValidationUtil.required(errors, "passwordTemp", form.getPasswordTemp(), "Konfirmasi Password");
        if (!errors.hasFieldErrors("passwordTemp")) {
        	FieldValidationUtil.match(errors, "passwordTemp", form.getPasswordTemp(), "Konfirmasi Password", form.getPassword(), "Password");
        }
        FieldValidationUtil.required(errors, "applicantType", form.getApplicantType(), "Jenis Pemohon");
        FieldValidationUtil.required(errors, "name", form.getName(), "Nama");
        FieldValidationUtil.required(errors, "no", form.getNo(), "Nomor KTP");
        FieldValidationUtil.numericOnly(errors, "no", form.getNo(), "Nomor KTP");
        FieldValidationUtil.minLength(errors,  "no", form.getNo(),  "Nomor KTP", 16);
        FieldValidationUtil.maxLength(errors,  "no", form.getNo(),  "Nomor KTP", 16);
        
        FieldValidationUtil.required(errors, "gender", form.getGender().getCurrentId(), "Jenis Kelamin");
        FieldValidationUtil.required(errors, "birthDateTemp", form.getBirthDateTemp(), "Tanggal Lahir");
        FieldValidationUtil.required(errors, "nationality", form.getNo(), "Kewarganegaraan");
        FieldValidationUtil.required(errors, "phone", form.getPhone(), "Telepon");
        FieldValidationUtil.required(errors, "mProvince", form.getmProvince().getCurrentId(), "Provinsi");
        FieldValidationUtil.required(errors, "mCity", form.getmCity().getCurrentId(), "Kabupaten/Kota");
        FieldValidationUtil.required(errors, "address", form.getAddress(), "Alamat");
        FieldValidationUtil.required(errors, "zipCode", form.getZipCode(), "Kode Pos");
		FieldValidationUtil.required(errors, "captchaAnswer",captchaAnswer, "Captcha");
		//FieldValidationUtil.required(errors, "fileKtp", form.getFileKtp(), "File KTP");


		if (!errors.hasFieldErrors("fileKtp")) {
        	FieldValidationUtil.fileSize(errors, "fileKtp", form.getFileKtp().getSize(), "File KTP", 5242880);
        }
        if (form.getApplicantType().equals("Konsultan KI")) {

            // Check apakah Konsultan sudah terdaftar di sistem atau belum
            if (form.getmReprs().getCurrentId() != null){
                String reprsId = form.getmReprs().getCurrentId() ;
                //System.out.println(reprsId);

                // Check ReprsNo , jika  belum terdaftar dan ADA di pdkki, maka harus didaftarkan
                String url = "http://pdkki.dgip.go.id/api/konsultan/merek.dgip.go.id/fcb0c9a28982415d54043f867be7dbd1/"+reprsId;
                String json =  restService.getRESTinJSON(url);
                if ((json == "EMPTY") || (json == "ERROR") )
                {
                    PASS = false ;
                    ErrorMessage = ErrorMessage.concat(" Data Konsultan TIDAK ADA di PDKKI") ;
                }
                else
                {
//                    MRepresentative mtest = null;
//                    mtest = representativeService.findOneByNoReps(reprsId);
//                    if (mtest  == null){
//                        MRepresentative mRep = new MRepresentative();
//                        NEW_REPS = true ;
                        // daftarkan , kerjakan tuntas besok! Pendaftaran harus otomatis
//                        PASS = false ;
//                        ErrorMessage = ErrorMessage.concat(" Konsultan BARU DIDAFTARKAN di Aplikasi! Silahka dicoba kembali") ;
//                        mRep.setId(mRep.getId());
//                        mRep.setNo(reprsId);
//                        form.setmReprs(mRep);
//                    }
                }


            }

            if (NEW_REPS){
                FieldValidationUtil.required(errors, "mReprs", form.getmReprs().getCurrentId(), "Nomor Konsultan");
            }
        	// req by pak dirjen untuk di take out 19-6-2019
//        	FieldValidationUtil.required(errors, "fileCard", form.getFileCard(), "File Kartu Konsultan");
//        	if (!errors.hasFieldErrors("fileCard")) {
//            	FieldValidationUtil.fileSize(errors, "fileCard", form.getFileCard().getSize(), "File Kartu Konsultan", 5242880);
//            }
        }
        if (form.getApplicantType().equals("Kanwil")) {
        	FieldValidationUtil.required(errors, "fileSeqId", form.getFileSeqId(), "Wilayah Kanwil");
        }
        // Untuk upload file pendukung
        if (form.getApplicantType().equals("Lembaga Pendidikan") || form.getApplicantType().equals("Lembaga Penelitian dan Pengembangan")
        		|| form.getApplicantType().equals("Usaha Mikro, Kecil dan Menengah") || form.getApplicantType().equals("Sentra KI")) {
        	
        	FieldValidationUtil.required(errors, "fileCard", form.getFileCard(), "Dokumen Pendukung");
        	if (!errors.hasFieldErrors("fileCard")) {
            	FieldValidationUtil.fileSize(errors, "fileCard", form.getFileCard().getSize(), "Dokumen Pendukung", 5242880);
            }
        }

        if (!errors.hasErrors() && isValid) {
        	MUser UserExisting = userService.getUserByEmail(form.getEmail());
        	
        	List<KeyValue> searchCriteriaEmail1 = new ArrayList<>();
        	searchCriteriaEmail1.add(new KeyValue("email", form.getEmail(), true));
        	searchCriteriaEmail1.add(new KeyValue("approvalStatus", RegistrasiStatusEnum.PREPARE.name(), true));
        	List<KeyValue> searchCriteriaEmail2 = new ArrayList<>();
        	searchCriteriaEmail2.add(new KeyValue("email", form.getEmail(), true));
        	searchCriteriaEmail2.add(new KeyValue("approvalStatus", RegistrasiStatusEnum.APPROVE.name(), true));
        	List<KeyValue> searchCriteriaKtp1 = new ArrayList<>();
        	searchCriteriaKtp1.add(new KeyValue("no", form.getNo(), true));
        	searchCriteriaKtp1.add(new KeyValue("approvalStatus", RegistrasiStatusEnum.PREPARE.name(), true));
        	List<KeyValue> searchCriteriaKtp2 = new ArrayList<>();
        	searchCriteriaKtp2.add(new KeyValue("no", form.getNo(), true));
        	searchCriteriaKtp2.add(new KeyValue("approvalStatus", RegistrasiStatusEnum.APPROVE.name(), true));
        	
        	if (UserExisting != null || registrasiOnlineService.count(searchCriteriaEmail1) > 0
        			|| registrasiOnlineService.count(searchCriteriaEmail2) > 0) {
        		model.addAttribute("errorMessage", "Email sudah digunakan/terdaftar, silakan masukkan alamat email yang lain");
//        	} else if (registrasiOnlineService.count(searchCriteriaKtp1) > 0
//        			|| registrasiOnlineService.count(searchCriteriaKtp2) > 0) {
//        		model.addAttribute("errorMessage", "Nomor KTP sudah digunakan/terdaftar");
        	}
        	else if(PASS == false){
                model.addAttribute("errorMessage", ErrorMessage);
//
            }

        	else {
        		form.setApprovalStatus(RegistrasiStatusEnum.PREPARE.name());
                //form.setFileKtp(fileKtp);
                //form.setFileNameNik(form.getFileKtp().getOriginalFilename());
                form.setFileCard(fileCard);
                form.setFileNameCard(fileCard==null ? null : form.getFileCard().getOriginalFilename());

                if (NEW_REPS){
                    registrasiOnlineService.injectNewPDKKI(form,NEW_REPS);
                }
                else{
                    registrasiOnlineService.insert(form);
                }


                //this.doSendEmail(form, request, SUBJECT_EMAIL_REGISTRASI, TEMPLATE_EMAIL_REGISTRASI, null);
                this.doSendEmailVerification(form, request, SUBJECT_EMAIL_REGISTRASI_VERIFY, TEMPLATE_EMAIL_REGISTRASI_VERIFY);
                return REDIRECT_SEND_EMAIL_SUCCESS;
        	}        	
        }

        initiatePage(model);
        return PAGE_REGISTRASI;
    }
    
    @RequestMapping(value = {PATH_RESEND}, method = {RequestMethod.GET})
    public String doShowPageResendEmailVerification() {
        return PAGE_RESEND;
    }
    
    @PostMapping(value = {PATH_RESEND})
    public String doResendEMailVerification(@RequestParam("email") String email, Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
    	List<KeyValue> searchCriteria = new ArrayList<>();
    	searchCriteria.add(new KeyValue("email", email, true));
    	searchCriteria.add(new KeyValue("approvalStatus", RegistrasiStatusEnum.PREPARE.name(), true));
    	TxOnlineReg txOnlineReg = registrasiOnlineService.selectOne(searchCriteria);
    	
    	if(txOnlineReg != null) {
			doSendEmailVerification(txOnlineReg, request, SUBJECT_EMAIL_REGISTRASI_VERIFY, TEMPLATE_EMAIL_REGISTRASI_VERIFY);
			return REDIRECT_SEND_EMAIL_SUCCESS;
    	} else {
    		model.addAttribute("errorMessage", getMessage("form.error.get.email"));
    		return PAGE_RESEND;
    	}
    }
    
    public void doSendEmailVerification(TxOnlineReg form, HttpServletRequest request, String subject, String template) throws IOException {
        byte[] encodedBytes = Base64.getEncoder().encode(form.getId().getBytes());
        String param = new String(encodedBytes);
        String baseUrl = String.format("%s://%s:%d%s/aktivasi-user?no=%s", request.getScheme(), request.getServerName(), request.getServerPort(), request.getContextPath(), param);
        if (baseUrl.contains(":80/")) {
        	baseUrl.replace(":80/", "/");
        }
        doSendEmail(form, request, subject, template, baseUrl);
    }

    public void doSendEmail(TxOnlineReg form, HttpServletRequest request, String subject, String template, String url) throws IOException {
        String logo = "static/img/" + logoEmailImage;
        File file = new File(pathImage + logoEmailImage);    	
        if (file.exists()) {
            logo = pathImage + logoEmailImage;
        }
        emailService.prepareAndSend(form.getEmail(), subject, form.getName(), form.getEmail(), form.getPassword(), url, logo, template);
    }

    @GetMapping(path = PATH_AKTIVASI)
    public String doAktivasiUser(@RequestParam("no") String no, Model model,
    		final HttpServletRequest request) throws IOException {
        byte[] decodedBytes = Base64.getDecoder().decode(no);
        String id = new String(decodedBytes);
        
        TxOnlineReg txOnlineReg = registrasiOnlineService.findOne(id);
        if (txOnlineReg != null) {
        	if (txOnlineReg.getApprovalStatus().equals(RegistrasiStatusEnum.EXPIRE.name())) {
        		txOnlineReg.setApprovalStatus(RegistrasiStatusEnum.APPROVE.name());
	        	registrasiOnlineService.insertForLogin(txOnlineReg);
	        	this.doSendEmail(txOnlineReg, request, SUBJECT_EMAIL_REGISTRASI_APPROVE, TEMPLATE_EMAIL_REGISTRASI_APPROVE, null);
	        	return REDIRECT_REGISTRATION_SUCCESS;
        	} else if (txOnlineReg.getApprovalStatus().equals(RegistrasiStatusEnum.REJECT.name())) {
        		txOnlineReg.setApprovalStatus(RegistrasiStatusEnum.APPROVE.name());
	        	registrasiOnlineService.insertForLogin(txOnlineReg);
	        	this.doSendEmail(txOnlineReg, request, SUBJECT_EMAIL_REGISTRASI_APPROVE, TEMPLATE_EMAIL_REGISTRASI_APPROVE, null);
	        	return REDIRECT_REGISTRATION_SUCCESS;
        	} else {
	        	MUser existing = userService.getUserByEmail(txOnlineReg.getEmail());
		        if (existing == null){
		        	String createdDate = DateUtil.formatDate(txOnlineReg.getCreatedDate(), "yyyy-MM-dd HH:mm:ss");
		        	String currentDate = DateUtil.formatDate(new Timestamp(System.currentTimeMillis()), "yyyy-MM-dd HH:mm:ss");
		        	DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		        	LocalDateTime locCreatedDate = LocalDateTime.parse(createdDate, format);
		        	LocalDateTime locCurrentDate = LocalDateTime.parse(currentDate, format);
//		        	Duration duration = Duration.between(locCreatedDate, locCurrentDate);
		        	
		        	/*if (duration.toHours() > 24) {
		        		txOnlineReg.setApprovalStatus(RegistrasiStatusEnum.EXPIRE.name());
			        	registrasiOnlineService.doApproveReject(txOnlineReg);
		        		return REDIRECT_REGISTRATION_EXPIRED;
		        	} else {*/
			    		txOnlineReg.setApprovalStatus(RegistrasiStatusEnum.APPROVE.name());
			        	registrasiOnlineService.insertForLogin(txOnlineReg);
			        	this.doSendEmail(txOnlineReg, request, SUBJECT_EMAIL_REGISTRASI_APPROVE, TEMPLATE_EMAIL_REGISTRASI_APPROVE, null);
			        	return REDIRECT_REGISTRATION_SUCCESS;
		        	//}
		        } 
        	}
        }

        return REDIRECT_LOGIN;
    }

    private void initiatePage(Model model) {
	    List<MLookup> mLookupList = lookupService.selectAllbyGroup("Gender");
//		List<MLookup> mLookupJPList = lookupService.selectAllbyGroup("JenisPemohon");
        List<MLookup> mLookupJPList = lookupService.selectAllByOrderToNumber("JenisPemohon");
		List<MCountry> mCountryList = countryService.findByStatusFlagTrue();
	    List<MProvince> mProvinceList = provinceService.findByStatusFlagTrue();
	    List<MCity> mCityList = cityService.findAll();
	    List<MRepresentative> mRepresentativeList = representativeService.selectAllReprsOrderByName();
	    List<MFileSequence> fileSequenceList = fileService.findMFileSequenceByKanwilFlagTrue();

	    model.addAttribute("listCountry", mCountryList);
	    model.addAttribute("listProvince", mProvinceList);
	    model.addAttribute("listCity", mCityList);
		model.addAttribute("listJenisPemohon", mLookupJPList);
	    model.addAttribute("listGender", mLookupList);
	    model.addAttribute("listReprs", mRepresentativeList);
	    model.addAttribute("fileSequenceList", fileSequenceList);

	    Captcha captcha = new Captcha.Builder(240, 64)
			    .addText(new DefaultTextProducer(6, new char[] {
					    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'K', 'L', 'M', 'N', 'P', 'R',
					    'W', 'X', 'Y', '2', '3', '4', '5', '6', '7', '8', '9'
			    }))
//			    .addText()
			    .addBackground(new FlatColorBackgroundProducer(Color.WHITE))
//			    .addBackground(new GradiatedBackgroundProducer())
//			    .addNoise(new StraightLineNoiseProducer())
			    .addNoise(new CurvedLineNoiseProducer())
//			    .gimp(new FishEyeGimpyRenderer())
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

	    model.addAttribute("imgCaptcha", imgCaptcha);
	    model.addAttribute("captchaKey", captchaKey);
    }

}
