package com.docotel.ki.controller.online;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.*;
import com.docotel.ki.model.transaction.TxOnlineReg;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.service.EmailService;
import com.docotel.ki.service.master.*;
import com.docotel.ki.service.transaction.RegistrasiOnlineService;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.util.FieldValidationUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.docotel.ki.enumeration.RegistrasiStatusEnum;
import com.docotel.ki.enumeration.UserEnum;
import com.docotel.ki.model.master.*;
import com.docotel.ki.service.master.*;
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
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

@Controller
public class RegistrasiOnlineEfilingController extends BaseController {
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

    private static final String DIRECTORY_PAGE = "reg-online-efiling/";
    private static final String CAPTCHA_SECRET_KEY = "#TG*R25#*(HJsgad6a&*_";
    private static final String PATH_REGISTRASI = "/aktivasi-efiling";

    private static final String PAGE_REGISTRASI = DIRECTORY_PAGE + "registrasi-efiling";

    private static final String PATH_CHECK_USER_EFILING = "/check-user-efiling";
    private static final String REQUEST_MAPPING_CHECK_USER_EFILING = PATH_CHECK_USER_EFILING + "*";

    public static final String PATH_AKTIVASI = "/aktivasi-user-efiling";

    private static final String TEMPLATE_EMAIL_REGISTRASI_APPROVE = "eTemplateRegistrasiApprove";

    private static final String SUBJECT_EMAIL_REGISTRASI_APPROVE = "DJKI - Akun Aplikasi Berhasil Didaftarkan";

    private static final String PATH_LOGIN = "/login";
    private static final String PATH_CHANGE_REG_SUCCESS = PATH_LOGIN + "?reg=ok";
    private static final String PATH_CHANGE_REG_EXPIRED = PATH_LOGIN + "?reg=expired";

    private static final String REDIRECT_LOGIN = "redirect:" + PATH_LOGIN;
    private static final String REDIRECT_REGISTRATION_SUCCESS = "redirect:" + PATH_CHANGE_REG_SUCCESS;
    private static final String REDIRECT_REGISTRATION_EXPIRED = "redirect:" + PATH_CHANGE_REG_EXPIRED;

    private static final String REQUEST_MAPPING_AKTIVASI =  PATH_AKTIVASI + "*";

    public static final String REDIRECT_MAPPING_AKTIVASI = "redirect:" + PATH_AKTIVASI;

    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "usermgmnt");
        model.addAttribute("subMenu", "listRegOnline");
    }

    @GetMapping(path = PATH_REGISTRASI)
    public String showPageDaftarEfiling(Model model) {
        model.addAttribute( "form", new TxOnlineReg());
        model.addAttribute( "imgCard", "" );

        initiatePage(model);

        return PAGE_REGISTRASI;
    }

    private void initiatePage(Model model) {
        List<MLookup> mLookupList = lookupService.selectAllbyGroup("Gender");
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

        model.addAttribute("imgCaptcha", imgCaptcha);
        model.addAttribute("captchaKey", captchaKey);
    }

    @RequestMapping(path = REQUEST_MAPPING_CHECK_USER_EFILING, method = {RequestMethod.POST})
    @ResponseBody
    public void doCheckUserEfiling(final HttpServletRequest request, final HttpServletResponse response, final HttpSession session)
            throws JsonProcessingException, IOException, ParseException {

        String statusError = null;
        String userEfiling = request.getParameter( "userEfiling" );
        TxOnlineReg txOnlineReg = registrasiOnlineService.findByUserEfiling(userEfiling.toUpperCase());
        Map<String, String> dataGeneral = new HashMap<>();

        if (txOnlineReg == null) {
            statusError = "User Efiling '" + userEfiling + "' Tidak ditemukan";
        } else {
            //--CEK EMAIL--//
            List<KeyValue> searchCriteriaEmail1 = new ArrayList<>();
            searchCriteriaEmail1.add(new KeyValue("email", txOnlineReg.getEmail(), true));
            searchCriteriaEmail1.add(new KeyValue("approvalStatus", RegistrasiStatusEnum.PREPARE.name(), true));
            Long emailExisting = registrasiOnlineService.count( searchCriteriaEmail1 );

            if (txOnlineReg.getApprovalStatus().equalsIgnoreCase( "PREPARE" )) {
                String name = "";
                String applicantType = "";
                String reprsId = "";

                String no = "";
                String gender = "";
                String birthDateTemp = "";
                String nationality = "";
                String phone = "";
                String mProvince = "";
                String mCity = "";
                String address= "";
                String zipCode = "";

                try {
                    name = txOnlineReg.getName();
                }catch (Exception e) {}
                try {
                    applicantType = txOnlineReg.getApplicantType();
                }catch (Exception e) {}
                try {
                    reprsId = txOnlineReg.getmReprs().getCurrentId();
                }catch (Exception e){}

                try {
                    no = txOnlineReg.getNo();
                }catch (Exception e) {}
                try {
                    gender = txOnlineReg.getGender().getCurrentId();
                }catch (Exception e) {}
                try {
                    birthDateTemp = txOnlineReg.getBirthDateTemp();
                }catch (Exception e) {}
                try {
                    nationality = txOnlineReg.getNationality().getCurrentId();
                }catch (Exception e) {}
                try {
                    phone = txOnlineReg.getPhone();
                }catch (Exception e) {}
                try {
                    mProvince = txOnlineReg.getmProvince().getCurrentId();
                }catch (Exception e) {}
                try {
                    mCity = txOnlineReg.getmCity().getCurrentId();
                }catch (Exception e) {}
                try {
                    address = txOnlineReg.getAddress();
                }catch (Exception e) {}
                try {
                    zipCode = txOnlineReg.getZipCode();
                }catch (Exception e) {}

                dataGeneral.put( "action_id", txOnlineReg.getId() );
                dataGeneral.put( "email", txOnlineReg.getEmail());
                dataGeneral.put( "name", name);
                dataGeneral.put( "applicantType", applicantType );
                dataGeneral.put( "reprsId", reprsId );

                dataGeneral.put( "no", no);
                dataGeneral.put( "gender", gender);
                dataGeneral.put( "birthDateTemp", birthDateTemp);
                dataGeneral.put( "nationality", nationality );
                dataGeneral.put( "phone", phone );
                dataGeneral.put( "mProvince", mProvince );
                dataGeneral.put( "mCity", mCity );
                dataGeneral.put( "address", address );
                dataGeneral.put( "zipCode", zipCode );
                dataGeneral.put( "emailExisting", String.valueOf( emailExisting ) );

            } else {
                statusError = "User Efiling '" + userEfiling + "' Sudah Terdaftar";
            }
        }

        dataGeneral.put( "statusError", statusError );
        writeJsonResponse( response, dataGeneral );
    }

    @PostMapping(value = PATH_REGISTRASI)
    public String doDaftarEfiling(@ModelAttribute("form") TxOnlineReg form,
                                  @RequestParam(value = "fileCard", required = false) MultipartFile fileCard,
                                  @RequestParam(value = "ckSetuju", required = false) Boolean ckSetuju,
                                  BindingResult errors, Model model, HttpServletRequest request, HttpServletResponse response) throws IOException {

    	if (form.getmReprs() != null) {
    		form.getmReprs().setUpdatedBy(UserEnum.SYSTEM.value());
    	}

        boolean isValid = true;
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
            FieldValidationUtil.required(errors, "mReprs", form.getmReprs().getCurrentId(), "Nomor Konsultan");
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

           /******- searchCriteriaEmail1 CEK EMAIL EFILING LEBIH DARI 1 - ****/
            if (UserExisting != null || registrasiOnlineService.count(searchCriteriaEmail1) > 1
                    || registrasiOnlineService.count(searchCriteriaEmail2) > 0) {
                model.addAttribute("errorMessage", "Email sudah digunakan/terdaftar, silakan masukkan email yang lain");
//                } else if (registrasiOnlineService.count(searchCriteriaKtp2) > 0) {
//                    model.addAttribute("errorMessage", "Nomor KTP sudah digunakan/terdaftar");
            } else {
                form.setApprovalStatus(RegistrasiStatusEnum.PREPARE.name());
                form.setFileCard(fileCard);
                form.setFileNameCard(fileCard==null ? null : form.getFileCard().getOriginalFilename());

                registrasiOnlineService.saveOrUpdate(form);
                byte[] encodedBytes = Base64.getEncoder().encode(form.getId().getBytes());
                String param = new String(encodedBytes);
                return REDIRECT_MAPPING_AKTIVASI+"?no="+param;
            }
        }

        initiatePage(model);
        return PAGE_REGISTRASI;
    }

    public void doSendEmail(TxOnlineReg form, HttpServletRequest request, String subject, String template, String url) throws IOException {
        String logo = "static/img/" + logoEmailImage;
        File file = new File(pathImage + logoEmailImage);
        if (file.exists()) {
            logo = pathImage + logoEmailImage;
        }
        emailService.prepareAndSend(form.getEmail(), subject, form.getName(), form.getEmail(), form.getPassword(), url, logo, template);
    }

    @GetMapping(REQUEST_MAPPING_AKTIVASI)
    public String doAktivasiEfiling(@RequestParam("no") String no, Model model,
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
                    Duration duration = Duration.between(locCreatedDate, locCurrentDate);

                    /*if (duration.toHours() > 24) {
                        txOnlineReg.setApprovalStatus(RegistrasiStatusEnum.EXPIRE.name());
                        registrasiOnlineService.doApproveReject(txOnlineReg);
                        return REDIRECT_REGISTRATION_EXPIRED;
                    } else {*/
                        txOnlineReg.setApprovalStatus(RegistrasiStatusEnum.APPROVE.name());
                        registrasiOnlineService.insertForLoginEfiling(txOnlineReg);
                        this.doSendEmail(txOnlineReg, request, SUBJECT_EMAIL_REGISTRASI_APPROVE, TEMPLATE_EMAIL_REGISTRASI_APPROVE, null);
                        return REDIRECT_REGISTRATION_SUCCESS;
                    //}
                }
            }
        }
        return REDIRECT_LOGIN;
    }
}