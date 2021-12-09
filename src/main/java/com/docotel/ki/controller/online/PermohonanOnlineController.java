package com.docotel.ki.controller.online;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.enumeration.ClassStatusEnum;
import com.docotel.ki.enumeration.PriorStatusEnum;
import com.docotel.ki.enumeration.StatusEnum;
import com.docotel.ki.model.master.MBrandType;
import com.docotel.ki.model.master.MCity;
import com.docotel.ki.model.master.MClass;
import com.docotel.ki.model.master.MClassDetail;
import com.docotel.ki.model.master.MCountry;
import com.docotel.ki.model.master.MDocTypeDetail;
import com.docotel.ki.model.master.MFileSequence;
import com.docotel.ki.model.master.MFileType;
import com.docotel.ki.model.master.MFileTypeDetail;
import com.docotel.ki.model.master.MLaw;
import com.docotel.ki.model.master.MLookup;
import com.docotel.ki.model.master.MPnbpFeeCode;
import com.docotel.ki.model.master.MProvince;
import com.docotel.ki.model.master.MRepresentative;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.transaction.TxPubsJournal;
import com.docotel.ki.model.transaction.TxReception;
import com.docotel.ki.model.transaction.TxRegistration;
import com.docotel.ki.model.transaction.TxRegistrationDetail;
import com.docotel.ki.model.transaction.TxTmBrand;
import com.docotel.ki.model.transaction.TxTmBrandDetail;
import com.docotel.ki.model.transaction.TxTmClass;
import com.docotel.ki.model.transaction.TxTmDoc;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.model.transaction.TxTmOwner;
import com.docotel.ki.model.transaction.TxTmOwnerDetail;
import com.docotel.ki.model.transaction.TxTmPrior;
import com.docotel.ki.model.transaction.TxTmRepresentative;
import com.docotel.ki.pojo.CetakMerek;
import com.docotel.ki.pojo.CetakSuratPernyataan;
import com.docotel.ki.pojo.DataForm1;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.master.MClassDetailCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxReceptionCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmClassCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmGeneralCustomRepository;
import com.docotel.ki.repository.master.MClassDetailRepository;
import com.docotel.ki.repository.master.MClassHeaderRepository;
import com.docotel.ki.repository.transaction.TxTmClassRepository;
import com.docotel.ki.service.EmailService;
import com.docotel.ki.service.SimpakiService;
import com.docotel.ki.service.master.BrandService;
import com.docotel.ki.service.master.CityService;
import com.docotel.ki.service.master.ClassService;
import com.docotel.ki.service.master.CountryService;
import com.docotel.ki.service.master.DocTypeService;
import com.docotel.ki.service.master.FileService;
import com.docotel.ki.service.master.LawService;
import com.docotel.ki.service.master.LookupService;
import com.docotel.ki.service.master.ProvinceService;
import com.docotel.ki.service.master.RepresentativeService;
import com.docotel.ki.service.master.UserService;
import com.docotel.ki.service.transaction.DokumenLampiranService;
import com.docotel.ki.service.transaction.OwnerService;
import com.docotel.ki.service.transaction.PermohonanOnlineService;
import com.docotel.ki.service.transaction.PermohonanService;
import com.docotel.ki.service.transaction.PriorService;
import com.docotel.ki.service.transaction.RegistrationService;
import com.docotel.ki.service.transaction.ReprsService;
import com.docotel.ki.service.transaction.TrademarkService;
import com.docotel.ki.signature.PDFSignatureFacade;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.util.FieldValidationUtil;
import com.docotel.ki.util.NumberUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfReader;

import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class PermohonanOnlineController extends BaseController {
    private static final String DIRECTORY_PAGE_ONLINE = "permohonan-online/";
    private static final String PAGE_PERMOHONAN_ONLINE = DIRECTORY_PAGE_ONLINE + "permohonan-online";
    private static final String PAGE_TAMBAH_PERMOHONAN_ONLINE = DIRECTORY_PAGE_ONLINE + "tambah-permohonan-online";
    private static final String PAGE_EDIT_PERMOHONAN_ONLINE = DIRECTORY_PAGE_ONLINE + "edit-permohonan-online";
    private static final String PAGE_PRATINJAU_PERMOHONAN_ONLINE = DIRECTORY_PAGE_ONLINE + "pratinjau-permohonan-online";
    private static final String PAGE_EDIT_PRATINJAU_PERMOHONAN_ONLINE = DIRECTORY_PAGE_ONLINE + "edit-pratinjau-permohonan-online";
    private static final String PAGE_CALCULATE_CLASS = DIRECTORY_PAGE_ONLINE + "calculate-class";
    private static final String PATH_AJAX_SEARCH_LIST_ONLINE = "/cari-permohonan-online";
    private static final String PATH_AJAX_SEARCH_LIST_REGISTRATION = "/cari-registration-online";
    public static final String PATH_AJAX_LIST_DELETE_DOKUMEN = "/delete-dokumen-online";
    private static final String PATH_PERMOHONAN_ONLINE = "/list-data-permohonan-online";
    private static final String PATH_DOKUMEN_ONLINE = "/list-dokumen-online";
    private static final String PATH_AJAX_LIST_PEMOHON = "/list-pemohon";
    private static final String PATH_AJAX_PATH_PEMOHONOL = "/pemohonOL";
    private static final String REQUEST_MAPPING_PEMOHONOL = PATH_AJAX_PATH_PEMOHONOL + "*";
    private static final String PATH_PRATINJAU_PERMOHONAN_ONLINE = "/pratinjau-permohonan-online";
    private static final String PATH_EDIT_PRATINJAU_ONLINE = "/edit-pratinjau-online";
    private static final String PATH_EDIT_PRATINJAU_ONLINE_PEMOHON = PATH_EDIT_PRATINJAU_ONLINE + "-pemohon";
    private static final String PATH_EDIT_PRATINJAU_ONLINE_KUASA = PATH_EDIT_PRATINJAU_ONLINE + "-kuasa";
    private static final String PATH_EDIT_PRATINJAU_ONLINE_PRIOR = PATH_EDIT_PRATINJAU_ONLINE + "-prior";
    private static final String PATH_EDIT_PRATINJAU_ONLINE_MEREK = PATH_EDIT_PRATINJAU_ONLINE + "-merek";
    private static final String PATH_EDIT_PRATINJAU_ONLINE_KELAS = PATH_EDIT_PRATINJAU_ONLINE + "-kelas";
    private static final String PATH_EDIT_PRATINJAU_ONLINE_DOKUMEN = "/edit-pratinjau-online-dokumen";
    private static final String PATH_CETAK_MEREK_ONLINE = "/cetak-merek-online";
    private static final String PATH_CETAK_PERMOHONAN_ONLINE_DRAFT = "/cetak-permohonan-online";
    private static final String PATH_DOWNLOAD_DOC_ONLINE = "/download-dokumen-online";
    private static final String PATH_CETAK_SURAT_PERNYATAAN_ONLINE = "/cetak-surat-pernyataan-online";
    private static final String PATH_TAMBAH_PERMOHONAN_ONLINE = "/tambah-permohonan-online";
    private static final String PATH_EDIT_PERMOHONAN_ONLINE = "/edit-permohonan-online";
    private static final String PATH_SELESAI_PERMOHONAN_ONLINE = "/selesai-permohonan-online";
    private static final String PATH_CHECK_CODE_BILLING = "/check-code-billing";
    private static final String PATH_SAVE_FORM_GENERAL_ONLINE = "/save-online-form-1";
    private static final String PATH_SAVE_FORM_PEMOHON_ONLINE = "/save-online-form-2";
    private static final String PATH_SAVE_FORM_KUASA_ONLINE = "/save-online-form-3";
    private static final String PATH_SAVE_FORM_PRIOR_ONLINE = "/save-online-form-4";
    private static final String PATH_SAVE_FORM_MEREK_ONLINE = "/save-online-form-5";
    private static final String PATH_SAVE_FORM_KELAS_ONLINE = "/save-online-form-6";
    private static final String PATH_SAVE_FORM_KELAS_ONLINE_NEW_BARANGJASA = "/save-online-form-6-new-barangjasa";
    private static final String PATH_LIST_TABEL_PENOLAKAN_PERMINTAAN="/list-tabel-penolakan-permintaan";

    //    private static final String PATH_SAVE_FORM_TRANS_REPL = "/save-reference-trans-repl";
    private static final String PATH_SAVE_FORM_AJUKAN_KELAS = "/save-ajukan-kelas";
    private static final String PATH_SAVE_FORM_DOKUMEN_ONLINE = "/save-online-form-7";
    private static final String PATH_DELETE_FORM_KUASA_ONLINE = "/delete-online-form-3";
    private static final String PATH_DELETE_FORM_PRIOR_ONLINE = "/delete-online-form-4";
    private static final String PATH_CALCULATE_CLASS = "/calculate-class-online";
    private static final String PATH_SIMPAN_LANJUTKAN_FORM_DOKUMEN_ONLINE = "/simpan-lanjutkan-online-form-7";
    private static final String REDIRECT_PERMOHONAN_ONLINE = "redirect:" + PATH_AFTER_LOGIN + PATH_PERMOHONAN_ONLINE;
    private static final String REDIRECT_PRATINJAU_PERMOHONAN_ONLINE = "redirect:" + PATH_AFTER_LOGIN + PATH_PRATINJAU_PERMOHONAN_ONLINE;
    private static final String REQUEST_MAPPING_AJAX_SEARCH_LIST_ONLINE = PATH_AJAX_SEARCH_LIST_ONLINE + "*";
    private static final String REQUEST_MAPPING_AJAX_SEARCH_LIST_REGISTRATION = PATH_AJAX_SEARCH_LIST_REGISTRATION + "*";
    private static final String REQUEST_MAPPING_PERMOHONAN_ONLINE = PATH_PERMOHONAN_ONLINE + "*";
    private static final String REQUEST_MAPPING_DOKUMEN_ONLINE = PATH_DOKUMEN_ONLINE + "*";
    private static final String REQUEST_MAPPING_AJAX_PEMOHON_LIST = PATH_AJAX_LIST_PEMOHON + "*";
    private static final String REQUEST_MAPPING_PRATINJAU_PERMOHONAN_ONLINE = PATH_PRATINJAU_PERMOHONAN_ONLINE + "*";
    private static final String REQUEST_MAPPING_EDIT_PRATINJAU_ONLINE_PEMOHON = PATH_EDIT_PRATINJAU_ONLINE_PEMOHON + "*";
    private static final String REQUEST_MAPPING_EDIT_PRATINJAU_ONLINE_KUASA = PATH_EDIT_PRATINJAU_ONLINE_KUASA + "*";
    private static final String REQUEST_MAPPING_EDIT_PRATINJAU_ONLINE_PRIOR = PATH_EDIT_PRATINJAU_ONLINE_PRIOR + "*";
    private static final String REQUEST_MAPPING_EDIT_PRATINJAU_ONLINE_MEREK = PATH_EDIT_PRATINJAU_ONLINE_MEREK + "*";
    private static final String REQUEST_MAPPING_EDIT_PRATINJAU_ONLINE_KELAS = PATH_EDIT_PRATINJAU_ONLINE_KELAS + "*";
    private static final String REQUEST_MAPPING_EDIT_PRATINJAU_ONLINE_DOKUMEN = PATH_EDIT_PRATINJAU_ONLINE_DOKUMEN + "*";
    private static final String REQUEST_MAPPING_AJAX_DELETE_DOKUMEN = PATH_AJAX_LIST_DELETE_DOKUMEN + "*";
    private static final String REQUEST_MAPPING_CETAK_MEREK_ONLINE = PATH_CETAK_MEREK_ONLINE + "*";
    private static final String REQUEST_MAPPING_CETAK_PERMOHONAN_ONLINE = PATH_CETAK_PERMOHONAN_ONLINE_DRAFT + "*";
    private static final String REQUEST_MAPPING_DOWNLOAD_DOC_ONLINE = PATH_DOWNLOAD_DOC_ONLINE + "*";
    private static final String REQUEST_MAPPING_CETAK_SURAT_PERNYATAAN_ONLINE = PATH_CETAK_SURAT_PERNYATAAN_ONLINE + "*";
    private static final String REQUEST_MAPPING_TAMBAH_PERMOHONAN_ONLINE = PATH_TAMBAH_PERMOHONAN_ONLINE + "*";
    private static final String REQUEST_MAPPING_EDIT_PERMOHONAN_ONLINE = PATH_EDIT_PERMOHONAN_ONLINE + "*";
    private static final String REQUEST_MAPPING_SELESAi_PERMOHONAN_ONLINE = PATH_SELESAI_PERMOHONAN_ONLINE + "*";
    private static final String REQUEST_MAPPING_CHECK_CODE_BILLING = PATH_CHECK_CODE_BILLING + "*";
    private static final String REQUEST_MAPPING_SAVE_FORM_GENERAL_ONLINE = PATH_SAVE_FORM_GENERAL_ONLINE + "*";
    private static final String REQUEST_MAPPING_SAVE_FORM_PEMOHON_ONLINE = PATH_SAVE_FORM_PEMOHON_ONLINE + "*";
    private static final String REQUEST_MAPPING_SAVE_FORM_KUASA_ONLINE = PATH_SAVE_FORM_KUASA_ONLINE + "*";
    private static final String REQUEST_MAPPING_SAVE_FORM_PRIOR_ONLINE = PATH_SAVE_FORM_PRIOR_ONLINE + "*";
    private static final String REQUEST_MAPPING_SAVE_FORM_MEREK_ONLINE = PATH_SAVE_FORM_MEREK_ONLINE + "*";
    private static final String REQUEST_MAPPING_SAVE_FORM_KELAS_ONLINE = PATH_SAVE_FORM_KELAS_ONLINE + "*";
    private static final String REQUEST_MAPPING_SAVE_FORM_AJUKAN_KELAS = PATH_SAVE_FORM_AJUKAN_KELAS + "*";
//    private static final String REQUEST_MAPPING_SAVE_FORM_TRANS_REPL = PATH_SAVE_FORM_TRANS_REPL + "*";
    private static final String REQUEST_MAPPING_SAVE_FORM_DOKUMEN_ONLINE = PATH_SAVE_FORM_DOKUMEN_ONLINE + "*";
    private static final String REQUEST_MAPPING_DELETE_FORM_KUASA_ONLINE = PATH_DELETE_FORM_KUASA_ONLINE + "*";
    private static final String REQUEST_MAPPING_DELETE_FORM_PRIOR_ONLINE = PATH_DELETE_FORM_PRIOR_ONLINE + "*";
    private static final String REQUEST_CALCULATE_CLASS = PATH_CALCULATE_CLASS + "*";
    private static final String REQUEST_MAPPING_SIMPAN_LANJUTKAN_FORM_DOKUMEN_ONLINE = PATH_SIMPAN_LANJUTKAN_FORM_DOKUMEN_ONLINE + "*";
    private static final String REQUEST_MAPPING_SAVE_FORM_KELAS_ONLINE_NEW_BARANGJASA=PATH_SAVE_FORM_KELAS_ONLINE_NEW_BARANGJASA + "*";
    private static final String REQUEST_MAPPING_LIST_TABEL_PENOLAKAN_PERMINTAAN=PATH_LIST_TABEL_PENOLAKAN_PERMINTAAN+ "*";

    @Autowired
    private BrandService brandService;
    @Autowired
    private ClassService classService;
    @Autowired
    private CountryService countryService;
    @Autowired
    private DocTypeService docTypeService;
    @Autowired
    private DokumenLampiranService doclampiranService;
    @Autowired
    private FileService fileService;
    @Autowired
    private PermohonanService permohonanService;
    @Autowired
    private PermohonanOnlineService permohonanOnlineService;
    @Autowired
    private PriorService priorService;
    @Autowired
    private ProvinceService provinceService;
    @Autowired
    private CityService cityService;
    @Autowired
    private ReprsService reprsService;
    @Autowired
    private RepresentativeService representativeService;
    @Autowired
    private SimpakiService simpakiService;
    @Autowired
    private TrademarkService trademarkService;
    @Autowired
    private LawService lawService;
    @Autowired
    private LookupService lookupService;
    @Autowired
    private RegistrationService registrationService;
    @Autowired
    EmailService emailService;
    @Autowired
    UserService userService;
    @Autowired
    TxTmGeneralCustomRepository txTmGeneralCustomRepository;
    @Autowired
    TxTmClassCustomRepository txTmClassCustomRepository;
    @Autowired
    MClassDetailCustomRepository mClassDetailCustomRepository;
    @Autowired
    MClassHeaderRepository mClassHeaderRepository;
    @Autowired
    TxReceptionCustomRepository txReceptionCustomRepository;
    @Autowired
    MClassDetailRepository mClassDetailRepository;
    @Autowired
    TxTmClassRepository txTmClassRepository;
    @Autowired
    private OwnerService ownerService;

    @Value("${app.freebill}")
    private String freebill;
    @Value("${upload.file.web.image:}")
    private String pathImage;
    @Value("${upload.file.logoemail.image:}")
    private String logoEmailImage;
    @Value("${upload.file.path.signature:}")
    private String uploadFilePathSignature;
    @Value("${upload.file.brand.path:}")
    private String uploadFileBrandPath;
    @Value("${upload.file.branddetail.path:}")
    private String uploadFileBrandDetailPath;
    @Value("${upload.file.doc.application.path:}")
    private String uploadFileDocApplicationPath;
    @Value("${download.output.permohonan.cetakmerek.file.path:}")
    private String downloadFileDocPermohonanCetakMerekPath;
    @Value(("${certificate.file}"))
    private String CERTIFICATE_FILE;
    @Value("${simpaki.api.create.billing}")
    private String urlCreateBilling;
    @Value("${upload.file.image.tandatangan.path}")
    private String uploadFileImageTandaTangan;

    private FileInputStream fileInputStreamReader;

    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "permohonanonlineMerek");
        model.addAttribute("subMenu", "permohonan-online");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MUser mUser = (MUser) auth.getPrincipal();
        if (mUser.isReprs())
            model.addAttribute("typeuser","(Konsultan)");
        else
            model.addAttribute("typeuser","(Umum)");

    }

    private void doInitiatePermohonan(final Model model, final HttpServletRequest request) {
        if (request.getRequestURI().contains(PATH_PERMOHONAN_ONLINE)) {
            model.addAttribute("urlCreateBilling", this.urlCreateBilling);

            List<MFileTypeDetail> fileTypeDetailList = fileService.getAllFileTypeDetail();
            model.addAttribute("fileTypeDetailList", fileTypeDetailList);

            List<KeyValue> searchCriteria = new ArrayList<>();
            searchCriteria.add(new KeyValue("menu", "DAFTAR", true));
            searchCriteria.add(new KeyValue("statusFlag", true, true));
            List<MFileType> fileTypeListByTypeMenuDaftar = fileService.selectAllMFileType(searchCriteria, "desc", "ASC", null, null);
            model.addAttribute("fileTypeListByTypeMenuDaftar", fileTypeListByTypeMenuDaftar);

            List<MFileTypeDetail> fileTypeDetailListById = fileService.findMFileTypeDetailById();
            model.addAttribute("fileTypeDetailListById", fileTypeDetailListById);

            List<MLookup> mKonfirm = lookupService.selectAllbyGroup("UICariKelas");
            model.addAttribute("lookupKonfirm", mKonfirm);

            List<MClass> listMClass = classService.findByStatusFlagTrue();
            Collections.sort(listMClass, (o1, o2) -> new Integer(o1.getNo()).compareTo(new Integer(o2.getNo())));
            model.addAttribute("listMClass", listMClass);

        }
        if (request.getRequestURI().contains(PATH_TAMBAH_PERMOHONAN_ONLINE)
                || request.getRequestURI().contains(PATH_EDIT_PERMOHONAN_ONLINE)
                || request.getRequestURI().contains(PATH_PRATINJAU_PERMOHONAN_ONLINE)
                || request.getRequestURI().contains(PATH_EDIT_PRATINJAU_ONLINE)) {
        	String form = "";
            if (request.getRequestURI().contains(PATH_EDIT_PRATINJAU_ONLINE_PEMOHON)) {
                form = "pemohon";
            } else if (request.getRequestURI().contains(PATH_EDIT_PRATINJAU_ONLINE_KUASA)) {
                form = "kuasa";
            } else if (request.getRequestURI().contains(PATH_EDIT_PRATINJAU_ONLINE_PRIOR)) {
                form = "prioritas";
            } else if (request.getRequestURI().contains(PATH_EDIT_PRATINJAU_ONLINE_MEREK)) {
                form = "merek";
            } else if (request.getRequestURI().contains(PATH_EDIT_PRATINJAU_ONLINE_KELAS)) {
                form = "kelas";
            } else if (request.getRequestURI().contains(PATH_EDIT_PRATINJAU_ONLINE_DOKUMEN)) {
                form = "dokumen";
            }
            model.addAttribute("form", form);

            String eFilingNo = request.getParameter("no");
            TxTmGeneral txTmGeneral = eFilingNo == null ? new TxTmGeneral() : trademarkService.selectOneGeneralByeFilingNo(eFilingNo);

            if (txTmGeneral == null) {
                txTmGeneral = new TxTmGeneral();
            }

            model.addAttribute("eFilingNo", eFilingNo);
            model.addAttribute("noGeneral", txTmGeneral.getApplicationNo());
            model.addAttribute("isNew", request.getRequestURI().contains(PATH_TAMBAH_PERMOHONAN_ONLINE));
            model.addAttribute("isEditPermohonan", request.getRequestURI().contains(PATH_EDIT_PERMOHONAN_ONLINE));
            model.addAttribute("isNewOrEdit", request.getRequestURI().contains(PATH_TAMBAH_PERMOHONAN_ONLINE) ||request.getRequestURI().contains(PATH_EDIT_PERMOHONAN_ONLINE));
            model.addAttribute("isEdit", request.getRequestURI().contains(PATH_EDIT_PRATINJAU_ONLINE));
            model.addAttribute("isReprs", isRepresentative());

            List<MLookup> disclaimerMerekList = lookupService.selectAllbyGroup("DisclaimerMerek");
            model.addAttribute("disclaimerMerek", disclaimerMerekList);

            if (form.equals("pemohon") || form.equals("")) {
                TxTmOwner txTmOwner = permohonanService.selectOneOwnerByApplicationNo(txTmGeneral.getId());
                if (txTmOwner == null) {
                    txTmOwner = new TxTmOwner();
                }
                txTmOwner.setTxTmGeneral(txTmGeneral);

                if (txTmOwner.getPostProvince() == null) {
                    MProvince mProvince = new MProvince();
                    txTmOwner.setPostProvince(mProvince);
                }

                if (txTmOwner.getPostCountry() == null) {
                    MCountry mCountry = new MCountry();
                    txTmOwner.setPostCountry(mCountry);
                }

                if (txTmOwner.getPostCity() == null) {
                    MCity mCity = new MCity();
                    txTmOwner.setPostCity(mCity);
                }

                model.addAttribute("txTmOwner", txTmOwner);

                List<TxTmOwnerDetail> txTmOwnerDetails = permohonanService.selectAllOwnerByOwnerId(txTmOwner.getId());
                model.addAttribute("txTmOwnerDetails", txTmOwnerDetails);

                if(request.getRequestURI().contains(PATH_EDIT_PERMOHONAN_ONLINE)) {
                	List<MProvince> mProvinceList = provinceService.findAll();
                    model.addAttribute("listProvince", mProvinceList);

                    List<MCity> mCityList = cityService.selectAll();
                    model.addAttribute("listCity", mCityList);
                }
                else if (request.getRequestURI().contains(PATH_TAMBAH_PERMOHONAN_ONLINE)) {
                	List<MProvince> mProvinceList = provinceService.findByStatusFlagTrue();
                    model.addAttribute("listProvince", mProvinceList);

                    List<MCity> mCityList = cityService.findByStatusFlagTrue();
                    model.addAttribute("listCity", mCityList);
                }

            }
            if(request.getRequestURI().contains(PATH_TAMBAH_PERMOHONAN_ONLINE)
            		|| request.getRequestURI().contains(PATH_EDIT_PERMOHONAN_ONLINE)
            		|| request.getRequestURI().contains(PATH_EDIT_PRATINJAU_ONLINE)) {
            	if (form.equals("kuasa") || form.equals("")) {
                    TxTmRepresentative txTmRepresentative = reprsService.selectOneByApplicationId(txTmGeneral.getId());
                    if (txTmRepresentative == null) {
                        txTmRepresentative = new TxTmRepresentative();
                    }
                    if (txTmRepresentative.getmRepresentative() == null) {
                        MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                        MRepresentative mRepresentative = representativeService.selectOne("userId.id", mUser.getId());
                        if (mRepresentative == null) {
                            mRepresentative = new MRepresentative();
                        }
                        txTmRepresentative.setmRepresentative(mRepresentative);
                    }
                    if (txTmRepresentative.getmRepresentative().getmCountry() == null) {
                        txTmRepresentative.getmRepresentative().setmCountry(new MCountry());
                    }
                    if (txTmRepresentative.getmRepresentative().getmProvince() == null) {
                        txTmRepresentative.getmRepresentative().setmProvince(new MProvince());
                    }
                    if (txTmRepresentative.getmRepresentative().getmCity() == null) {
                        txTmRepresentative.getmRepresentative().setmCity(new MCity());
                    }

                    model.addAttribute("txTmReprs", txTmRepresentative);
                }
            } else {
            	if (form.equals("kuasa") || form.equals("")) {
                	List<TxTmRepresentative> txTmRepresentativeList = reprsService.selectAllReprsByIdGeneral(txTmGeneral.getId());
                	if (txTmRepresentativeList == null) {
                		txTmRepresentativeList = new ArrayList<>() ;
                    }

                	model.addAttribute("txTmReprs", txTmRepresentativeList==null ? new TxTmRepresentative() : txTmRepresentativeList);
                }
            }

            if (form.equals("pemohon") || form.equals("prioritas") || form.equals("") && request.getRequestURI().contains(PATH_EDIT_PERMOHONAN_ONLINE)) {
                //List<MCountry> mCountryList = countryService.findAll();
                List<MCountry> mCountryList = countryService.findByStatusFlagTrue();
                model.addAttribute("listCountry", mCountryList);
            }
            else if (form.equals("pemohon") || form.equals("prioritas") || form.equals("") && request.getRequestURI().contains(PATH_TAMBAH_PERMOHONAN_ONLINE)) {
                List<MCountry> mCountryList = countryService.findByStatusFlagTrue();
                model.addAttribute("listCountry", mCountryList);
            }

            if (form.equals("merek") || form.equals("")) {
                TxTmBrand txTmBrand = txTmGeneral.getTxTmBrand();
                if (txTmBrand == null) {
                    txTmBrand = new TxTmBrand();
                    txTmBrand.setmBrandType(new MBrandType());
                }

                String imgMerek = "";
                try {
                    String pathFolder = DateUtil.formatDate(txTmBrand.getCreatedDate(), "yyyy/MM/dd/");
                    File file = new File(uploadFileBrandPath + pathFolder + txTmBrand.getId() + ".jpg");
                    if (file.exists() && !file.isDirectory()) {
                        String formatFile = txTmBrand.getFileName().substring(txTmBrand.getFileName().lastIndexOf(".") + 1);
                        FileInputStream fileInputStreamReader = new FileInputStream(file);
                        byte[] bytes = new byte[(int) file.length()];
                        fileInputStreamReader.read(bytes);
                        imgMerek = "data:image/jpg" + formatFile + ";base64," + Base64.getEncoder().encodeToString(bytes);
                        fileInputStreamReader.close();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                model.addAttribute("txTmBrand", txTmBrand);
                model.addAttribute("imgMerek", imgMerek);

                List<MBrandType> listMBrandType = brandService.findByStatusFlagTrue();
                model.addAttribute("listBrandType", listMBrandType);
            }
            if (form.equals("kelas") || form.equals("")) {
                TxReception txReception = txTmGeneral.getTxReception();
                if (txReception == null) {
                    txReception = new TxReception();
                    txReception.setApplicationDate(new Timestamp(System.currentTimeMillis()));
                }
                model.addAttribute("txReception", txReception);
                model.addAttribute("totalClass", txReception.getTotalClass());

                List<MLookup> mKonfirm = lookupService.selectAllbyGroup("UICariKelas");
                model.addAttribute("lookupKonfirm", mKonfirm);

                List<MClass> listMClass = classService.findByStatusFlagTrue();
                Collections.sort(listMClass, Comparator.comparing(o -> new Integer(o.getNo())));
                model.addAttribute("listMClass", listMClass);

                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                MUser mUser = (MUser) auth.getPrincipal();
                List<KeyValue> searchCriteria = new ArrayList<>();
                searchCriteria.add(new KeyValue("createdBy", mUser, true));

                List<TxTmGeneral> listPermohonan = trademarkService.selectAll(searchCriteria, null, null, 0, 100000);
                Collections.sort(listPermohonan, Comparator.comparing(TxTmGeneral::getApplicationNo));
                Collections.reverse(listPermohonan);
                model.addAttribute("listPermohonan", listPermohonan);

//                List<TxTmClass> listTxTmKelas = classService.findTxTmClassByCreated(mUser);
//                Collections.sort(listTxTmKelas, Comparator.comparing(o -> new Integer(o.getmClass().getNo())));
//                List<TxTmClass> newList = listTxTmKelas.stream().distinct().collect(Collectors.toList());
//                model.addAttribute("listTxTmKelas", listTxTmKelas);

                String classStatusEnum = "\"acceptValue\":\"" + ClassStatusEnum.ACCEPT.name() + "\",";
                classStatusEnum += "\"acceptLabel\":\"" + ClassStatusEnum.ACCEPT.getLabel() + "\",";
                classStatusEnum += "\"rejectValue\":\"" + ClassStatusEnum.REJECT.name() + "\",";
                classStatusEnum += "\"rejectLabel\":\"" + ClassStatusEnum.REJECT.getLabel() + "\",";
                classStatusEnum += "\"pendingValue\":\"" + ClassStatusEnum.PENDING.name() + "\",";
                classStatusEnum += "\"pendingLabel\":\"" + ClassStatusEnum.PENDING.getLabel() + "\"";

                model.addAttribute("classStatusEnum", "{" + classStatusEnum + "}");
            }
            if (form.equals("dokumen") || form.equals("")) {
				/*List<TxTmDoc> listDocType = new ArrayList<TxTmDoc>();
		        List<TxTmDoc> txtmDocList = doclampiranService.getAllDocByApplicationId("txTmGeneral.id", txTmGeneral.getId());
		        List<MDocType> mDocTypeList = docTypeService.findByStatusFlagTrue();
		        for (MDocType result : mDocTypeList) {
		        	boolean status = txtmDocList == null ? false : txtmDocList.stream().filter(o -> o.getmDocType().getId().equals(result.getId())).findFirst().isPresent();
		        	TxTmDoc txTmDoc = new TxTmDoc();
		        	txTmDoc.setmDocType(result);
		        	txTmDoc.setStatus(status);
		        	listDocType.add(txTmDoc);
		        }
		        Collections.sort(listDocType, (o1, o2) -> o1.getmDocType().getName().compareTo(o2.getmDocType().getName()));
		        model.addAttribute("listDocType", listDocType);
		        model.addAttribute("docUploadDate", DateUtil.formatDate(new Date(), "dd/MM/yyyy"));*/

                List<KeyValue> searchCriteria = new ArrayList<>();
                searchCriteria.add(new KeyValue("mDocType.statusFlag", true, true));
                if (!(boolean) model.asMap().get("isNewOrEdit")) {
                    searchCriteria.add(new KeyValue("mFileType.id", txTmGeneral.getTxReception().getmFileType().getId(), true));
                }

                List<MDocTypeDetail> listDocType = docTypeService.selectAllDetail(searchCriteria);
                model.addAttribute("listDocType", listDocType);
                model.addAttribute("docUploadDate", DateUtil.formatDate(new Date(), "dd/MM/yyyy"));
            }
            if (form.equals("")) {
                txTmGeneral.setTxTmPriorList(permohonanService.selectAllPriorByGeneralId(txTmGeneral.getId()));

                model.addAttribute("txTmGeneral", txTmGeneral);

                //List<MFileType> fileTypeList = fileService.getAllFileTypes();
                //model.addAttribute("fileTypeList", fileTypeList);
                List<KeyValue> searchCriteria = new ArrayList<>();
                searchCriteria.add(new KeyValue("menu", "DAFTAR", true));
                searchCriteria.add(new KeyValue("statusFlag", true, true));
                List<MFileType> fileTypeList = fileService.selectAllMFileType(searchCriteria, "desc", "ASC", null, null);
                model.addAttribute("fileTypeList", fileTypeList);

                List<MFileTypeDetail> fileTypeDetailList = fileService.getAllFileTypeDetail();
                model.addAttribute("fileTypeDetailList", fileTypeDetailList);

                //MFileSequence fileSequenceList = fileService.getFileSequenceByCode("ID");
                List<MFileSequence> fileSequenceList = fileService.getAllFileSequences();
                model.addAttribute("fileSequenceList", fileSequenceList);

                List<TxTmBrandDetail> txTmBrandDetailList = new ArrayList<>();
                Map<Integer, String[]> listTxTmBrandDetail = new HashMap<>();
                if (txTmGeneral.getTxTmBrand() != null) {
                    txTmBrandDetailList = txTmGeneral.getTxTmBrand().getTxTmBrandDetailList();
                    int countTxTmClass = 0;
                    if (listTxTmBrandDetail != null) {
                        for (TxTmBrandDetail result : txTmBrandDetailList) {
                            countTxTmClass++;
                            int key = countTxTmClass;
                            String tanggalUpload = result.getUploadDateTemp();
                            String namaFile = result.getFileName();
                            String ukuran = result.getSize();
                            String deskripsi = result.getFileDescription();

                            String imgMerekAdd = "";
                            try {
                                String pathFolder = DateUtil.formatDate(result.getUploadDate(), "yyyy/MM/dd/");
                                File file = new File(uploadFileBrandDetailPath + pathFolder + result.getId() + "." +  FilenameUtils.getExtension(result.getFileName()));
                                if (file.exists() && !file.isDirectory()) {
                                    String formatFile = result.getFileName().substring(result.getFileName().lastIndexOf(".") + 1);
                                    FileInputStream fileInputStreamReader = new FileInputStream(file);
                                    byte[] bytes = new byte[(int) file.length()];
                                    fileInputStreamReader.read(bytes);
                                    imgMerekAdd = result.getFileDataType() + "," + Base64.getEncoder().encodeToString(bytes);
                                    fileInputStreamReader.close();
                                }
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            listTxTmBrandDetail.put(key, new String[]{tanggalUpload, namaFile, ukuran, deskripsi, imgMerekAdd});
                        }
                    }
                }


                model.addAttribute("txTmBrandDetailList", listTxTmBrandDetail);

                List<MLaw> lawList = lawService.findByStatusFlagTrue();
                model.addAttribute("listLaw", lawList);

                List<TxTmClass> txTmClassList = classService.selectAllClassByIdGeneral(txTmGeneral.getId(), ClassStatusEnum.ACCEPT.name());
                Map<Integer, String[]> listTxTmClass = new HashMap<>();
                if (txTmClassList != null) {
                    for (TxTmClass result : txTmClassList) {
                        int key = result.getmClass().getNo();
                        String desc = result.getmClassDetail().getDesc();
                        String descEn = result.getmClassDetail().getDescEn();

                        if (listTxTmClass.containsKey(key)) {
                            desc = listTxTmClass.get(key)[0] + "; " + desc;
                            descEn = listTxTmClass.get(key)[1] + "; " + descEn;
                        }

                        listTxTmClass.put(key, new String[]{desc, descEn,});
                    }
                }
                model.addAttribute("listTxTmClass", listTxTmClass);
            }

            if (request.getRequestURI().contains(PATH_PRATINJAU_PERMOHONAN_ONLINE)) {
        		TxPubsJournal txPubsJournal = trademarkService.selectOnePubJournalByAppId(txTmGeneral.getId());
                if (txPubsJournal == null) {
                    txPubsJournal = new TxPubsJournal();
                }
                model.addAttribute("txPubsJournal", txPubsJournal);
                TxRegistration txRegistration = registrationService.selectOne(txTmGeneral.getId());
                if (txRegistration == null) {
                    txRegistration = new TxRegistration();
                }
                model.addAttribute("txRegistration", txRegistration);
        	}
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_PERMOHONAN_ONLINE)
    public String doShowPagePermohonan(@RequestParam(value = "error", required = false) String error, Model model, final HttpServletRequest request, final HttpServletResponse response) {
        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("errorMessage", error);
        }

        doInitiatePermohonan(model, request);
        model.addAttribute("version","Version 1.50");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MUser mUser = (MUser) auth.getPrincipal();
        if (mUser.isReprs())
            model.addAttribute("typeuser","(Konsultan)");
        else
            model.addAttribute("typeuser","(Umum)");
        return PAGE_PERMOHONAN_ONLINE;
    }

    @RequestMapping(path = REQUEST_MAPPING_TAMBAH_PERMOHONAN_ONLINE, method = {RequestMethod.GET})
    public String doShowPageTambahPermohonan(Model model, final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
//    	if(isNotRepresentative()) {
//    		return REDIRECT_PERMOHONAN_ONLINE + "?error=Hanya user konsultan yang dapat mengakses menu ini. Silakan hubungi Administrator.";
//    	}

        doInitiatePermohonan(model, request);

        return PAGE_TAMBAH_PERMOHONAN_ONLINE;
    }

    @RequestMapping(path = REQUEST_MAPPING_EDIT_PERMOHONAN_ONLINE, method = {RequestMethod.GET})
    public String doShowPageEditPermohonan(Model model, final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
//    	if(isNotRepresentative()) {
//    		return REDIRECT_PERMOHONAN_ONLINE + "?error=Hanya user konsultan yang dapat mengakses menu ini. Silakan hubungi Administrator.";
//    	}
        MUser userLogin = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        if (userLogin.getUsername() != "super") {
            String eFilingNo = request.getParameter( "no" );

            List<KeyValue> criteriaList = new ArrayList<>();
            criteriaList.add(new KeyValue("txReception.eFilingNo", eFilingNo, true));
            criteriaList.add(new KeyValue("createdBy", userLogin, true));
            TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByeFilingNoByUser(criteriaList);

            if (txTmGeneral == null) {
                return "redirect:" + PATH_AFTER_LOGIN + PATH_PERMOHONAN_ONLINE + "?error=" +"Halaman tidak ditemukan";
            } else if(!txTmGeneral.getmStatus().getId().equals("IPT_DRAFT")) {
            	return "error-404";
            }
//        }
        doInitiatePermohonan(model, request);
        return PAGE_EDIT_PERMOHONAN_ONLINE;
    }

    @RequestMapping(path = REQUEST_MAPPING_PRATINJAU_PERMOHONAN_ONLINE, method = {RequestMethod.GET})
    public String doShowPagePratinjauPermohonan(@RequestParam(value = "error", required = false) String error, @RequestParam(value = "no", required = true) String no, Model model, final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
        MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByeFilingNo(no);

        if (mUser.getUsername() != "super") {
            List<KeyValue> criteriaList = new ArrayList<>();
            criteriaList.add(new KeyValue("txReception.eFilingNo", no, true));
            criteriaList.add(new KeyValue("createdBy", mUser, true));
            txTmGeneral = trademarkService.selectOneGeneralByeFilingNoByUser(criteriaList);

            if (txTmGeneral == null) {
                return "redirect:" + PATH_AFTER_LOGIN + PATH_PERMOHONAN_ONLINE + "?error=" +"Halaman tidak ditemukan";
            }
        }

        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("errorMessage", error);
        }

        //PRIORITAS
        List<TxTmPrior> txTmPriorList = permohonanService.selectAllPriorByGeneralId(txTmGeneral.getId());
        List<TxTmPrior> tempTxTmPriorList = new ArrayList<>();
        if (txTmPriorList != null) {
            for (TxTmPrior temp : txTmPriorList){
                TxTmPrior addingPrior = temp;
                addingPrior.setStatus(PriorStatusEnum.valueOf(temp.getStatus().trim().toUpperCase()).getLabel());
                tempTxTmPriorList.add(addingPrior);
                logger.info("STATUS txtmprior "+addingPrior.getStatus());

            }
        }
        model.addAttribute("txTmPrior", tempTxTmPriorList==null ? new TxTmPrior() : tempTxTmPriorList );

        if (txTmGeneral.getmStatus().getId().equalsIgnoreCase(StatusEnum.IPT_DRAFT.name())) {
            return "redirect:" + PATH_AFTER_LOGIN + PATH_EDIT_PERMOHONAN_ONLINE + "?no=" + no;
        } else {
            doInitiatePermohonan(model, request);
            return PAGE_PRATINJAU_PERMOHONAN_ONLINE;
        }
    }

    @RequestMapping(path = {REQUEST_MAPPING_EDIT_PRATINJAU_ONLINE_PEMOHON, REQUEST_MAPPING_EDIT_PRATINJAU_ONLINE_KUASA,
            REQUEST_MAPPING_EDIT_PRATINJAU_ONLINE_PRIOR, REQUEST_MAPPING_EDIT_PRATINJAU_ONLINE_MEREK,
            REQUEST_MAPPING_EDIT_PRATINJAU_ONLINE_KELAS, REQUEST_MAPPING_EDIT_PRATINJAU_ONLINE_DOKUMEN},
            method = {RequestMethod.GET})
    public String doShowPageEditPratinjauPermohonan(@RequestParam(value = "no", required = true) String no, Model model, final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
//    	if(isNotRepresentative()) {
//    		return REDIRECT_PRATINJAU_PERMOHONAN_ONLINE + "?error=Hanya user konsultan yang dapat mengakses menu ini. Silakan hubungi Administrator.&no=" + no;
//    	}
        doInitiatePermohonan(model, request);
        return PAGE_EDIT_PRATINJAU_PERMOHONAN_ONLINE;
    }

    @RequestMapping(path = REQUEST_MAPPING_AJAX_SEARCH_LIST_ONLINE, method = {RequestMethod.POST})
    public void doSearchDataTablesList(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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

            String[] searchByArr = request.getParameterValues("searchByArr[]");
            String[] keywordArr = request.getParameterValues("keywordArr[]");
            List<KeyValue> searchCriteria = null;
            List<KeyValue> searchCriteria2 = null;
            
            searchCriteria = new ArrayList<>();            
			searchCriteria2 = new ArrayList<>();
			boolean search = false;
            if (searchByArr != null) {
                for (int i = 0; i < searchByArr.length; i++) {
                    String searchBy = searchByArr[i];
                    String value = null;
                    try {
                        value = keywordArr[i];
                    } catch (ArrayIndexOutOfBoundsException e) {
                    }
                    if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                        if (searchBy.equalsIgnoreCase("applicationDate") || searchBy.equalsIgnoreCase("txReception.applicationDate")) {
                            try {
                                searchCriteria.add(new KeyValue(searchBy, DateUtil.toDate("dd/MM/yyyy", value), true));
                                searchCriteria2.add(new KeyValue("txTmGeneral.txReception.applicationDate", DateUtil.toDate("dd/MM/yyyy", value), true));
                            } catch (ParseException e) {
                            }
                        } else {
                            if (StringUtils.isNotBlank(value)) {
                            	search = true;
                                searchCriteria.add(new KeyValue(searchBy, value, true));
                                if (searchBy.equalsIgnoreCase("applicationNoOnline")) {
                        			searchCriteria2.add(new KeyValue("txTmGeneral.applicationNo", value, false));                    				
                                }
                                if (searchBy.equalsIgnoreCase("txReception.eFilingNo")) {                                	
                        			searchCriteria2.add(new KeyValue("txTmGeneral.txReception.eFilingNo", value, false));                    				
                                }
                        		if (searchBy.equalsIgnoreCase("txTmBrand.name")) {
                        			searchCriteria2.add(new KeyValue("txTmGeneral.txTmBrand.name", value, false));
                        		}
                        		if (searchBy.equalsIgnoreCase("classList")) {
                    				searchCriteria2.add(new KeyValue("txTmGeneral.classList", value, false));                        			
                        		}
                        		if (searchBy.equalsIgnoreCase("txReception.bankCode")) {
                    				searchCriteria2.add(new KeyValue("txTmGeneral.txReception.bankCode", value, false));                        			
                        		}
                            }
                        }
                    }
                }
            }

            MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            searchCriteria.add(new KeyValue("txReception.onlineFlag", Boolean.TRUE, true));
            searchCriteria.add(new KeyValue("txReception.createdBy", mUser, true));
            searchCriteria.add(new KeyValue("txReception.mFileType.menu", "DAFTAR", true));
            
            String orderBy = request.getParameter("order[0][column]");
            if (orderBy != null) {
                orderBy = orderBy.trim();
                if (orderBy.equalsIgnoreCase("")) {
                    orderBy = null;
                } else {
                    switch (orderBy) {
                        case "1":
                            orderBy = "txReception.eFilingNo";
                            break;
                        case "2":
                            orderBy = "txReception.applicationDate";
                            break;
                        case "3":
                            orderBy = "txTmBrand.mBrandType.name";
                            break;
                        case "4":
                            orderBy = "txTmBrand.name";
                            break;
                        case "5":
                            orderBy = "classList";
                            break;
                        case "6":
                            orderBy = "applicationNo";
                            break;
                        case "7":
                            orderBy = "txReception.mFileType.desc";
                            break;
                        case "8":
                            orderBy = "txReception.mFileTypeDetail.desc";
                            break;
                        case "9":
                            orderBy = "mStatus.name";
                            break;
                        default:
                            orderBy = "applicationNo";
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

            GenericSearchWrapper<TxTmGeneral> searchResult = trademarkService.searchGeneral(searchCriteria, orderBy, orderType, offset, limit);
            List<String> applicationNo = new ArrayList<String>();
            for (TxTmGeneral txTmGeneral : searchResult.getList()) {
            	applicationNo.add(txTmGeneral.getApplicationNo());
            }
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());
                if (search == false) {
                	searchCriteria2.add(new KeyValue("txTmGeneral.applicationNo", applicationNo, false));
                }
                GenericSearchWrapper<TxRegistration> searchResult2 = registrationService.selectAllSertifikatDigital(searchCriteria2, null, null, null, null);
                List<TxRegistration> txRegistrations = searchResult2.getList();
                List<String> merekNo = new ArrayList<String>();
                for (TxRegistration txRegistration : txRegistrations) {
                	merekNo.add(txRegistration.getTxTmGeneral().getApplicationNo());
                }
                
                int no = offset;
                for (TxTmGeneral result : searchResult.getList()) {
                    no++;
                    String brandName = "-";
                    String brandType = "-";
                    String status = "-";
                    String kelas = "-";

                    try {
                        brandName = result.getTxTmBrand().getName();
                    } catch (NullPointerException e) {
                    }

                    try {
                        brandType = result.getTxTmBrand().getmBrandType().getName();
                    } catch (NullPointerException e) {
                    }

                    try {
                        status = result.getmStatus().getName();
                    } catch (NullPointerException e) {
                    }

                    try {
                        kelas = result.getClassList();
                    } catch (NullPointerException e) {
                    }

                    String appNo = result.getApplicationNo().equals(result.getTxReception().geteFilingNo()) ? "-" : result.getApplicationNo();
                    String button = "";
                    String buttonSertifikatMerek = "";
                    String btnCetakSuratPernyataan = "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin(PATH_CETAK_SURAT_PERNYATAAN_ONLINE) + "?no=" + result.getApplicationNo() + "\">Surat Pernyataan</a>";
                    if (result.getmStatus()!= null && result.getmStatus().getId().equalsIgnoreCase(StatusEnum.IPT_DRAFT.name())) {
                        button = "<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT_PERMOHONAN_ONLINE + "?no=" + result.getTxReception().geteFilingNo()) + "\">Ubah</a>";
                    } else {
                        button = "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin(PATH_CETAK_MEREK_ONLINE) + "?no=" + result.getApplicationNo() + "\">Tanda Terima</a>  ";
                    }
                    if (merekNo.contains(appNo)) {
                    	buttonSertifikatMerek = "<br/>" + "<button onclick=\"showModal('"+appNo+"')\" class=\"btn btn-info btn-xs\">Download Sertifikat Merek</a>";
                    }
                    String fileTypeDesc = "";
                    try {
                        fileTypeDesc = result.getTxReception().getmFileType().getDesc();
                    } catch (Exception ex) {}
                    String fileTypeDetDesc = "";
                    try {
                        fileTypeDetDesc = result.getTxReception().getmFileTypeDetail().getDesc();
                    } catch (Exception ex) {}

                    if(result.getmStatus()!=null) {
                        data.add(new String[]{
                                "" + no,
                                "<a target=\"_blank\" href=\"" + getPathURLAfterLogin(PATH_PRATINJAU_PERMOHONAN_ONLINE + "?no=" + result.getTxReception().geteFilingNo()) + "\">" + result.getTxReception().geteFilingNo() + "</a>",
                                result.getTxReception().getApplicationDateTemp(),
                                brandType,
                                brandName,
                                kelas,
                                appNo,
                                fileTypeDesc,
                                fileTypeDetDesc,
                                status,
                                "<div class=\"btn-actions\">" + button + "<br/>" + 
                                (result.getmStatus().getId().equalsIgnoreCase(StatusEnum.IPT_DRAFT.name()) ? "" : btnCetakSuratPernyataan) + 
                                buttonSertifikatMerek + "</div>"
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

    @RequestMapping(path = REQUEST_MAPPING_AJAX_SEARCH_LIST_REGISTRATION, method = {RequestMethod.POST})
    public void doSearchDataTablesListRegistration(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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

            String[] searchByArr = request.getParameterValues("searchByArr[]");
            String[] keywordArr = request.getParameterValues("keywordArr[]");
            List<KeyValue> searchCriteria = null;

            searchCriteria = new ArrayList<>();
            if (searchByArr != null) {
                for (int i = 0; i < searchByArr.length; i++) {
                    String searchBy = searchByArr[i];
                    String value = null;
                    try {
                        value = keywordArr[i];
                    } catch (ArrayIndexOutOfBoundsException e) {
                    }
                    if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                        if (StringUtils.isNotBlank(value)) {
                            searchCriteria.add(new KeyValue(searchBy, value, true));
                        }
                    }
                }
            }

            String orderBy = request.getParameter("orderBy");
            if (orderBy != null) {
                orderBy = orderBy.trim();
                if (orderBy.equalsIgnoreCase("")) {
                    orderBy = null;
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

            GenericSearchWrapper<TxRegistrationDetail> searchResult = registrationService.selectAll(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxRegistrationDetail result : searchResult.getList()) {
                    no++;

                    data.add(new String[]{
                            "" + no,
                            result.getTxRegistration().getNo(),
                            result.getStartDateTemp(),
                            result.getEndDateTemp(),
                            result.isStatus() ? "Aktif" : "Tidak Aktif",
                            result.getTxRegistration().getCreatedBy().getUsername()
                    });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_CHECK_CODE_BILLING, method = {RequestMethod.POST})
    @ResponseBody
    public void doCheckCodeBilling(final HttpServletRequest request, final HttpServletResponse response,
                                   final HttpSession session) throws JsonProcessingException, IOException, ParseException {
        String statusError = null;
        String bankCode = request.getParameter("bankCode");
        TxReception txReception = permohonanOnlineService.selectOneTxReception("bankCode", bankCode);
        Map<String, String> dataGeneral = new HashMap<>();

        if (txReception != null) {
            statusError = "Kode Billing '" + bankCode + "' Sudah Digunakan oleh Nomor Transaksi "+txReception.geteFilingNo();
        } else {
            try {
                String result = simpakiService.getQueryBilling(bankCode);
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(result);

                String code = rootNode.get("code").toString().replaceAll("\"", "");
                String message = rootNode.get("message").toString().replaceAll("\"", "");
                if (code.equals("00")) {
                    String data = rootNode.get("data").toString();
                    JsonNode dataNode = mapper.readTree(data);

                    String flagPayment = dataNode.get("flag_pembayaran").toString().replaceAll("\"", "");
                    String flagUsed = dataNode.get("terpakai").toString().replaceAll("\"", "");

                    if (flagPayment.equalsIgnoreCase("BELUM")) {
                        statusError = "Kode Billing '" + bankCode + "' Masih dalam proses verifikasi bank. Mohon tunggu beberapa saat lagi";
                    } else if (!flagUsed.equalsIgnoreCase("BELUM")) {
                        statusError = "Kode Billing '" + bankCode + "' Sudah Digunakan";
                    } else {
                        String paymentDate = dataNode.get("tgl_pembayaran").toString().replaceAll("\"", "");
                        String currentDate = DateUtil.formatDate(new Timestamp(System.currentTimeMillis()), "yyyy-MM-dd HH:mm:ss");
                        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        LocalDateTime locPaymentDate = LocalDateTime.parse(paymentDate, format);
                        LocalDateTime locCurrentDate = LocalDateTime.parse(currentDate, format);
                        Duration duration = Duration.between(locPaymentDate, locCurrentDate);
                        String feeCode = dataNode.get("kd_tarif").toString().replaceAll("\"", "");
                        MPnbpFeeCode mPnbpFeeCode = simpakiService.getPnbpFeeCodeByCode(feeCode);

//                        System.out.println("dump kode " + feeCode);
                       if (mPnbpFeeCode == null) {
                            statusError = "Kode Tarif '" + feeCode + "' Tidak Ditemukan";
                        } else if (mPnbpFeeCode.getmFileType() != null) {
                        	statusError = "Silakan klik tombol 'Check' kode billing terlebih dahulu";
                        } else {
                            MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

                            String totalClass = dataNode.get("volume").toString().replaceAll("\"", "");
                            String totalPayment = dataNode.get("total_pembayaran").toString().replaceAll("\"", "");
                            String ownerName = dataNode.get("nama").toString().replaceAll("\"", "");
                            String ownerEmail = dataNode.get("email").toString().replaceAll("\"", "");
                            String ownerPhone = dataNode.get("no_tlp").toString().replaceAll("\"", "");
                            String ownerProvince = dataNode.get("kd_provinsi").toString().replaceAll("\"", "");
                            String ownerCity = dataNode.get("kd_kabupaten").toString().replaceAll("\"", "");
                            String ownerRegion = dataNode.get("kd_kecamatan").toString().replaceAll("\"", "");
                            String ownerAddress = dataNode.get("alamat").toString().replaceAll("\"", "");
                            String ownerFlag = dataNode.get("flag_warga").toString().replaceAll("\"", "");

                            dataGeneral.put("mFileSequence", mUser.getmFileSequence().getId());
                            dataGeneral.put("mFileType", mPnbpFeeCode.getmFileType() == null ? null : mPnbpFeeCode.getmFileType().getId());
                            dataGeneral.put("mFileTypeDetail", mPnbpFeeCode.getmFileTypeDetail() == null ? null : mPnbpFeeCode.getmFileTypeDetail().getId());
                            dataGeneral.put("mLaw", lawService.findByStatusFlagTrue().get(0).getId());
                            dataGeneral.put("totalClass", totalClass);
                            dataGeneral.put("totalPayment", totalPayment);
                            dataGeneral.put("totalPaymentTemp", NumberUtil.formatDecimal(NumberUtil.parseDecimal(totalPayment)));
                            dataGeneral.put("paymentDate", DateUtil.formatDate(DateUtil.toDate("yyyy-MM-dd HH:mm:ss", paymentDate), "dd/MM/yyyy HH:mm:ss"));
                            dataGeneral.put("ownerName", ownerName);
                            dataGeneral.put("ownerEmail", ownerEmail);
                            dataGeneral.put("ownerPhone", ownerPhone);

                            if (ownerFlag.equalsIgnoreCase("WNI")) {
                                MCountry mCountry = permohonanOnlineService.selectOneCountryByCode("ID");
                                MProvince mProvince = permohonanOnlineService.selectOneProvinceByCode(ownerProvince);
                                MCity mCity = permohonanOnlineService.selectOneCityByCode(ownerCity);


                                dataGeneral.put("ownerCountry", mCountry == null ? null : mCountry.getId());
                                dataGeneral.put("ownerProvince", mProvince == null ? null : mProvince.getId());
                                dataGeneral.put("ownerCity", mCity == null ? null : mCity.getId());
                                dataGeneral.put("ownerAddress", ownerAddress);
                            } else {
                                dataGeneral.put("ownerCountry", "99");
                                dataGeneral.put("ownerProvince", null);
                                dataGeneral.put("ownerCity", null);
                                dataGeneral.put("ownerRegion", null);
                                dataGeneral.put("ownerAddress", null);
                            }
                        }
                    }
                } else {
                    //statusError = code.equals("02") ? "Kode Billing '" + bankCode + "' Tidak Ditemukan" : message;
                    statusError = code.equals("02") ? "Klik tombol 'Check' Kode Billing dan periksa data kode billing" : message;
                }
            } catch (Exception e) {
                e.printStackTrace();
                statusError = "Pengecekan Kode Billing '" + bankCode + "' Gagal";
            }
        }

        dataGeneral.put("statusError", statusError);

        writeJsonResponse(response, dataGeneral);
    }

    @RequestMapping(value = REQUEST_MAPPING_DOKUMEN_ONLINE, method = {RequestMethod.POST})
    public void doDataTablesListDocument(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            DataTablesSearchResult dataTablesSearchResult = new DataTablesSearchResult();
            try {
                dataTablesSearchResult.setDraw(Integer.parseInt(request.getParameter("draw")));
            } catch (NumberFormatException e) {
                dataTablesSearchResult.setDraw(0);
            }

            String appNo = request.getParameter("appNo");

            TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(appNo);
            List<TxTmDoc> txtmDocList = doclampiranService.selectAllTxTmDoc("txTmGeneral.id", txTmGeneral == null ? "" : txTmGeneral.getId());
            List<String[]> data = new ArrayList<>();

            if (txtmDocList.size() > 0) {
                dataTablesSearchResult.setRecordsFiltered(txtmDocList.size());
                dataTablesSearchResult.setRecordsTotal(txtmDocList.size());

                int no = 0;
                for (TxTmDoc result : txtmDocList) {
                    no++;

                    data.add(new String[]{
                            result.getmDocType().getId(),
                            " " + no,
                            DateUtil.formatDate(result.getCreatedDate(), "dd-MM-yyyy"),
                            result.getmDocType().getName(),
                            result.getFileName(),
                            result.getDescription() == null ? "" : result.getDescription(),
                            result.getFileSize(),
                            "",
                            result.getId()
                    });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_SAVE_FORM_GENERAL_ONLINE, method = {RequestMethod.POST})
    @ResponseBody
    public void doSaveFormGeneral(@RequestBody DataForm1 form, Model model,
                                  final HttpServletRequest request, final BindingResult errors, final HttpServletResponse response,
                                  final HttpSession session) {

        FieldValidationUtil.required(errors, "mFileSequence.id", form.getmFileSequence().getCurrentId(), "asal permohonan");

        FieldValidationUtil.required(errors, "mFileType.id", form.getmFileType().getCurrentId(), "tipe permohonan");

        //FieldValidationUtil.required(errors, "mFileTypeDetail.id", form.getmFileTypeDetail().getCurrentId(), "jenis permohonan");

        TxReception recep = permohonanOnlineService.selectOneTxReception("bankCode",form.getBankCode());

        String validationError = "errorValid";
        //System.out.println(errors.hasErrors());
        if (!errors.hasErrors()) {
            try {
                if(form.getmFileType().getId().equalsIgnoreCase("MEREK_DAGANG_JASA") && form.getTotalClass() <= 1) {
                    writeJsonResponse(response, validationError);
                } else if (recep != null && form.getAppid().isEmpty()) {
            		validationError = "usedBilling";
                    writeJsonResponse(response, validationError);
                } else {
            		String application_no = trademarkService.insertReceptionOnline(form);
                    writeJsonResponse(response, application_no);
            	}
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                model.addAttribute("errorMessage", "Gagal menambahkan permohonan online tab general");
                //writeJsonResponse(response, 200);
            }
        }else{
            logger.error(errors.getAllErrors());
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

                if (!mUser.getId().equalsIgnoreCase("super")) {
                    searchCriteria.add(new KeyValue("createdBy", mUser, true));
                }

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

    @RequestMapping(path = REQUEST_MAPPING_PEMOHONOL)
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

    @RequestMapping(path = REQUEST_MAPPING_SAVE_FORM_PEMOHON_ONLINE, method = {RequestMethod.POST})
    @ResponseBody
    public void doSaveFormPemohon(@RequestBody TxTmOwner pemohon, final HttpServletRequest request, final BindingResult errors, final HttpServletResponse response, final HttpSession session) {
//    	FieldValidationUtil.required(errors, "zipCode", pemohon.getZipCode(), "kode pos");
    	Timestamp tmstm = new Timestamp(System.currentTimeMillis());
        pemohon.setCreatedDate(tmstm);
        pemohon.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        pemohon.setStatus(true);
        TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(pemohon.getTxTmGeneral().getApplicationNo());
        pemohon.setTxTmGeneral(txTmGeneral);
        for (TxTmOwnerDetail txTmOwnerDetail : pemohon.getTxTmOwnerDetails()) {
            txTmOwnerDetail.setTxTmOwner(pemohon);
            txTmOwnerDetail.setTxTmGeneral(pemohon.getTxTmGeneral());
            txTmOwnerDetail.setStatus(true);
        }
        try {
            if (pemohon.getmCity() != null && (pemohon.getmCity().getCurrentId() == null || "".equalsIgnoreCase(pemohon.getmCity().getCurrentId()))) {
                pemohon.setmCity(null);
            }
            if (pemohon.getmProvince() != null && (pemohon.getmProvince().getCurrentId() == null || "".equalsIgnoreCase(pemohon.getmProvince().getCurrentId()))) {
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
    }

    @RequestMapping(value = REQUEST_MAPPING_SAVE_FORM_KUASA_ONLINE, method = {RequestMethod.POST})
    public void doSaveFormKuasa(Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        try {
            String reprsId = request.getParameter("reprsId");
            String appNo = request.getParameter("appNo");
            String txTmReprsId = request.getParameter("txTmReprsId");
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            TxTmGeneral txTmGeneral = trademarkService.selectOne("applicationNo", appNo);
            TxTmRepresentative txTmRepresentative = reprsService.selectOneByApplicationId(txTmGeneral.getId());

            if (reprsId.equals(null) || reprsId.equals("")) {
                if (txTmRepresentative != null) {
                    reprsService.hapusKuasa(txTmRepresentative);
                }
            } else {
                MRepresentative mRepresentative = representativeService.selectOne("id", reprsId);

                if (txTmRepresentative == null) {
                    txTmRepresentative = new TxTmRepresentative();
                    txTmRepresentative.setId(txTmReprsId);
                }
                txTmRepresentative.setCreatedDate(timestamp);
                txTmRepresentative.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

                //skip mRepresentative for Konsultan role!
                if(mRepresentative!=null) {
                    txTmRepresentative.setmRepresentative(mRepresentative);
                    /********* - ADD NEW COLUMN TX_TM_REPRS - *********/
                    txTmRepresentative.setAddress(mRepresentative.getAddress());
                    txTmRepresentative.setmCountry(mRepresentative.getmCountry());
                    txTmRepresentative.setmProvince(mRepresentative.getmProvince());
                    txTmRepresentative.setmCity(mRepresentative.getmCity());
                    txTmRepresentative.setZipCode(mRepresentative.getZipCode());
                    txTmRepresentative.setEmail(mRepresentative.getEmail());
                    txTmRepresentative.setPhone(mRepresentative.getPhone());
                    txTmRepresentative.setName(mRepresentative.getName());
                    txTmRepresentative.setOffice(mRepresentative.getOffice());
                }
                /********* - AND NEW COLUMN TX_TM_REPRS - *********/
                txTmRepresentative.setTxTmGeneral(txTmGeneral);
                txTmRepresentative.setStatus(true);
                reprsService.simpanKuasa(txTmRepresentative, txTmReprsId);
            }
            writeJsonResponse(response, 200);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            writeJsonResponse(response, 500);
        }
    }

    @RequestMapping(value = REQUEST_MAPPING_DELETE_FORM_KUASA_ONLINE, method = {RequestMethod.GET})
    public void doDeleteFormKuasa(Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (isAjaxRequest(request)) {
            try {
                String txTmReprsId = request.getParameter("txTmReprsId");

                TxTmRepresentative txTmRepresentative = reprsService.selectOneById(txTmReprsId);
                reprsService.hapusKuasa(txTmRepresentative);
                response.setStatus(HttpServletResponse.SC_OK);
            } catch (NullPointerException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    @RequestMapping(value = REQUEST_MAPPING_SAVE_FORM_PRIOR_ONLINE, method = {RequestMethod.GET})
    public void doSaveFormPrior(Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ParseException {
    	  if (isAjaxRequest(request)) {
              setResponseAsJson(response);

	    	Map<String, Object> result = new HashMap<>();
	    	result.put("success", false);

	    	String negaraId = request.getParameter("negaraId");
	        String no = request.getParameter("no");
	        String appNo = request.getParameter("appNo");
	        Timestamp tmstm = DateUtil.toDate("dd/MM/yyyy", request.getParameter("tgl"));
              //check date before today till 6 months before
              Timestamp today = new Timestamp(System.currentTimeMillis());
              if( tmstm.before(DateUtils.addMonths(Timestamp.from(today.toInstant().minus(1, ChronoUnit.DAYS)), -6))){
                  result.put("message", "Hak Prioritas Anda sudah melebihi 6 bulan");
                  result.put("success", false);
                  writeJsonResponse(response, result);
                  return;
              }

	        MCountry mCountry = new MCountry();
	        mCountry.setId(negaraId);

	        List<KeyValue> searchCriteria = new ArrayList<>();
			searchCriteria.add(new KeyValue("txTmGeneral", trademarkService.selectOne("applicationNo", appNo), true));
		    searchCriteria.add(new KeyValue("no", no, true));
		    TxTmPrior existOnePrior = priorService.selectOneKriteria(null, searchCriteria, null, null);
		    if(existOnePrior!=null) {
		    	result.put("message", "Gagal Menyimpan Data Prioritas, Nomor Prioritas tidak boleh sama.");
		    }
		     if (result.size() == 1) {
		    	 TxTmPrior txTmPrior = new TxTmPrior();
		         txTmPrior.setId(txTmPrior.getId());
		         txTmPrior.setNo(no);
		         txTmPrior.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		         txTmPrior.setCreatedDate(tmstm);
		         txTmPrior.setPriorDate(tmstm);
		         txTmPrior.setmCountry(mCountry);
		         txTmPrior.setTxTmGeneral(trademarkService.selectOne("applicationNo", appNo));
		         txTmPrior.setStatus(PriorStatusEnum.PENDING.name());
		         priorService.doSaveOrUpdate(txTmPrior);
		         result.put("success", true);
		     }

		     writeJsonResponse(response, result);
    	  }
    }

    @RequestMapping(value = REQUEST_MAPPING_DELETE_FORM_PRIOR_ONLINE, method = {RequestMethod.POST})
    public void doDeleteFormPrior(Model model, @RequestParam("idPrior") String id, final HttpServletRequest request, final HttpServletResponse response) {
        priorService.deletePrioritas(id);
    }

    @RequestMapping(value = REQUEST_MAPPING_SAVE_FORM_MEREK_ONLINE, method = {RequestMethod.POST})
    @ResponseBody
    public void doSaveFormMerek(@RequestParam("txTmBrand") String stxTmBrand, @RequestParam(value = "fileMerek", required = false) MultipartFile uploadfile,
                                @RequestParam("listImageDetail") String listImageDetail, @RequestParam("listDelete") String[] listDelete,
                                @RequestParam("agreeDisclaimer") String agreeDisclaimers, final HttpServletRequest request, final HttpServletResponse response) throws IOException {

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);



        try {
            String mime;

//          mime =   URLConnection.guessContentTypeFromStream(new BufferedInputStream(uploadfile.getInputStream()));

            /*if (mime == null || !mime.startsWith("image")) {
                result.put("message", "File label merek harus dalam format JPG / JPEG");
            }
            else */if (uploadfile != null && uploadfile.getSize() > 0) {
                BufferedImage img = ImageIO.read(uploadfile.getInputStream());

                if (uploadfile.getSize() > 5242880) {
                    result.put("message", "Ukuran file label merek maksimal 5MB");
                } else if (img.getWidth() > 1024 || img.getHeight() > 1024) {
                    result.put("message", "Dimensi file gambar label merek maksimal adalah 1024 x 1024, File gambar label merek Yang Anda Upload " + img.getWidth() + " x " + img.getHeight() + ". "
                            + "Silahkan melakukan resize gambar terlebih dahulu !!");
                } else {
                    /*String filename = uploadfile.getOriginalFilename().toLowerCase();
                    if (!filename.toUpperCase().endsWith(".JPG") && !filename.toUpperCase().endsWith(".JPEG")) {
                        result.put("message", "File label merek harus dalam format JPG / JPEG");
                    }*/
                    mime = URLConnection.guessContentTypeFromStream(new BufferedInputStream(uploadfile.getInputStream()));
                    /*if (mime == null || !mime.startsWith("image")) {
                        result.put("message", "File label merek harus dalam format JPG / JPEG");
                    }*/
                    if (mime.startsWith("image/png")) {
                        result.put("message", "Format file label merek tidak valid. Format file terdeteksi bukan file asli JPG/JPEG (rename dari file PNG / GIF / selain JPG). Silakan lakukan crop/simpan ulang Label Merek tersebut ke dalam bentuk file JPG.");
                    }
                }
            } else if (agreeDisclaimers.equalsIgnoreCase("false")) {
                result.put("message", "Anda belum menyetujui disclaimer");
            }
        } catch(Exception e){
            //System.out.println("============== ERROR-nya: "); e.printStackTrace();
            result.put("message", "Format file label merek tidak valid. Format file terdeteksi bukan file asli JPG/JPEG. Silakan lakukan crop/simpan ulang Label Merek tersebut ke dalam bentuk file JPG.");
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(listImageDetail);

        for (JsonNode node : rootNode) {
            String[] fileData = node.get(3).toString().replaceAll("\"", "").split(",");
            for(String file : fileData) {
            	if(!(file.toUpperCase().endsWith(".JPG") || file.toUpperCase().endsWith(".JPEG") || file.toUpperCase().endsWith(".MP3")
            			|| file.toUpperCase().endsWith(".DOCX") || file.toUpperCase().endsWith(".DOC") || file.toUpperCase().endsWith(".PDF"))) {
            		result.put("message", "File Label Merek Tambahan hanya boleh upload file dengan extensi .jpg, .mp3, .doc, .docx atau .pdf");
            	}
            }
        }

        if (result.size() == 1) {
        	rootNode = mapper.readTree(stxTmBrand);
            try {
                String no = rootNode.get("txTmGeneral").toString().replaceAll("\"", "");
                String idmBrandType = rootNode.get("mBrandType").toString().replaceAll("\"", "");
                String name = rootNode.get("name").toString().replaceAll("\"", "");
                String color = rootNode.get("color").toString().replaceAll("\"", "");
                String description = rootNode.get("description").toString().replaceAll("\"", "");
                String descChar = rootNode.get("descChar").toString().replaceAll("\"", "");
                String disclaimer = rootNode.get("disclaimer").toString().replaceAll("\"", "");
                String agreeDisclaimer = rootNode.get("agreeDisclaimerVal").toString().replaceAll("\"", "");
                String translation = rootNode.get("translation").toString().replaceAll("\"", "");

                String descMerek = "";
                String[] desc = description.split(Pattern.quote("\\n"));
                for(String dsc : desc) {
                	descMerek += dsc + "\n";
                }
                if(descMerek.length() > 0) {
                	descMerek = descMerek.substring(0, descMerek.length() - 1);
        		}

                String descriptionChar = "";
                String[] descCharPattern = descChar.split(Pattern.quote("\\n"));
                for(String dscChr : descCharPattern) {
                	descriptionChar += dscChr + "\n";
                }
                if(descriptionChar.length() > 0) {
                	descriptionChar = descriptionChar.substring(0, descriptionChar.length() - 1);
        		}

                String disclaimers = "";
                String[] disclaimerPattern = disclaimer.split(Pattern.quote("\\n"));
                for(String dscChr : disclaimerPattern) {
                	disclaimers += dscChr + "\n";
                }
                if(disclaimers.length() > 0) {
                	disclaimers = disclaimers.substring(0, disclaimers.length() - 1);
        		}

                String colors = "";
                String[] colorPattern = color.split(Pattern.quote("\\n"));
                for(String clr : colorPattern) {
                	colors += clr + "\n";
                }
                if(colors.length() > 0) {
                	colors = colors.substring(0, colors.length() - 1);
        		}

                String translations = "";
                String[] tranlationPattern = translation.split(Pattern.quote("\\n"));
                for(String trnlate : tranlationPattern) {
                	translations += trnlate + "\n";
                }
                if(translations.length() > 0) {
                	translations = translations.substring(0, translations.length() - 1);
        		}

                Timestamp tmstm = new Timestamp(System.currentTimeMillis());

                TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(no);

                TxTmBrand txTmBrand = txTmGeneral.getTxTmBrand() == null ? new TxTmBrand() : txTmGeneral.getTxTmBrand();
                MBrandType mBrandType = new MBrandType();
                mBrandType.setId(idmBrandType);

                txTmBrand.setTxTmGeneral(txTmGeneral);
                txTmBrand.setmBrandType(mBrandType);
                txTmBrand.setName(name);
                txTmBrand.setDescription(descMerek);
                txTmBrand.setTranslation(translations);
                txTmBrand.setDescChar(descriptionChar);
                txTmBrand.setColor(colors);
                txTmBrand.setDisclaimer(disclaimers);
                txTmBrand.setAgreeDisclaimer(Boolean.parseBoolean(agreeDisclaimer));
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
                        txTmBrandDetail.setFileDescription(node.get(5).toString() == null ? node.get(5).toString().replaceAll("\"", "") : "-");
                        txTmBrandDetail.setUploadDate(tmstm);
                        txTmBrandDetail.setFileDataType(fileData[0]);

                        brandService.insertHanyaTxTmBrandDetail(txTmBrandDetail, fileData[1]);
                    }
                }
                // End delete and insert TxTmBrandDetail

                result.put("success", true);
            } catch (NullPointerException | IOException e) {
                logger.error(e.getMessage(), e);
                result.put("message", "Gagal menyimpan logo merek");
            }
        }

        writeJsonResponse(response, result);
    }

    @RequestMapping(value = REQUEST_MAPPING_SAVE_FORM_KELAS_ONLINE, method = {RequestMethod.POST})
    public void doSaveFormKelas(Model model, final HttpServletRequest request, final HttpServletResponse response, @RequestParam("appNo") String appNo, @RequestParam("listClassChildId") String[] listClassChildId) throws IOException {
    	System.out.println("appNo save kelas : "+appNo);
    	TxTmGeneral txTmGeneral = trademarkService.selectOne("applicationNo", appNo);
        List<String> listId = new ArrayList<String>();
        if (listClassChildId != null) {
            for (String classChildId : listClassChildId) {
                String[] classChildTemp = classChildId.split(";");
                listId.add(classChildTemp[0]);
            }
        }
        classService.deleteTxTMClassMultipleNotIn2(appNo, listId.toArray(new String[listId.size()]));        
        List listIdDetail = classService.selectTxTMClassMultiple(appNo);
        List<String> listNew = new ArrayList();
        int ix = 0;
        for (String idx : listId) {
            boolean exist = false;
            for ( Object id_db  : listIdDetail) {
                if(idx.equals(id_db.toString())) {
                    exist = true;
                }
            }
            if(!exist) {
                listNew.add(listClassChildId[ix]);
            }
            ix++;
        }
        Timestamp tmstm = new Timestamp(System.currentTimeMillis());
        if (listClassChildId != null) {
            Map<String, MClassDetail> detailMap = new HashMap();
            int i = 0;
            TxTmClass[] listTxClass =  new TxTmClass[listNew.size()];
            for (String classChildId : listNew) {
                String[] classChildTemp = classChildId.split(";");
                MClassDetail mClassDetail;
                if(detailMap.containsKey(classChildTemp[0])) {
                    mClassDetail = detailMap.get(classChildTemp[0]);
                } else {
                    mClassDetail = classService.selectOneMClassDetailById(classChildTemp[0]);
                    detailMap.put(classChildTemp[0], mClassDetail);
                }
                MClass mClass = mClassDetail.getParentClass();
                TxTmClass txTmClass = new TxTmClass();
                txTmClass.setId(txTmClass.getId());
                txTmClass.setTxTmGeneral(txTmGeneral);
                txTmClass.setmClass(mClass);
                txTmClass.setmClassDetail(mClassDetail);
                txTmClass.setCreatedDate(tmstm);
                txTmClass.setCreatedBy(txTmGeneral.getCreatedBy());
//                txTmClass.setUpdatedDate(tmstm);
                txTmClass.setUpdatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                txTmClass.setTransactionStatus(classChildTemp[1]);
                txTmClass.setCorrectionFlag(classChildTemp[2].equals("TRUE"));
                if(mClass != null) {
                    txTmClass.setEdition(mClass.getEdition());
                    txTmClass.setVersion(mClass.getVersion());
                }
                listTxClass[i] = txTmClass;
                i++;
            }
            classService.saveTxTMClass(listTxClass);
//            MUser user = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//            classService.deleteUnusedMClassDetail(user.getId());
        }
    }

    @RequestMapping(value = REQUEST_MAPPING_SAVE_FORM_AJUKAN_KELAS, method = {RequestMethod.POST})
    public void doSaveFormAjukanKelas(@RequestParam("appNo") String appNo, @RequestParam("requestData") String[] requestData, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        MClassDetail mClassDetail = new MClassDetail();
        MClass existingMClass = classService.findOneMClass(requestData[0]);
        mClassDetail.setParentClass(existingMClass);
        mClassDetail.setDesc(requestData[1]);
        mClassDetail.setDescEn(requestData[2]);
        mClassDetail.setSerial1("");
        mClassDetail.setSerial2("");
        mClassDetail.setClassBaseNo("");
        mClassDetail.setStatusFlag(false);
        mClassDetail.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        mClassDetail.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        MClassDetail result = classService.saveOrUpdate(mClassDetail);

        String[] data = new String[]{
                result.getId(),
                "" + result.getParentClass().getNo(),
                result.getSerial1(),
                result.getDesc(),
                result.getDescEn(),
                result.getParentClass().getEdition(),
                result.getParentClass().getVersion().toString(),
                "show",
                result.getParentClass().getType()

        };

        response.setStatus( HttpServletResponse.SC_OK );
        writeJsonResponse( response, data );
    }

    @RequestMapping(value = REQUEST_MAPPING_SIMPAN_LANJUTKAN_FORM_DOKUMEN_ONLINE, method = {RequestMethod.POST})
    @ResponseBody
    public void doSaveFormDokumen(@RequestParam("appNo") String appNo, @RequestParam("docList") String docList,
                                  final Model model, final HttpServletRequest request, final HttpServletResponse response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(docList);

            TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(appNo);

            KeyValue msg = new KeyValue();
            msg.setKey("Success");
            msg.setValue("Input Data Success");
            if (txTmGeneral.getTxTmClassList().size() == 0) {
                msg.setKey("Error");
                msg.setValue("kelasError");
            } else if (txTmGeneral.getTxReception().getmFileType().getId().equals("MEREK_DAGANG_JASA")) {
                List<KeyValue> searchCriteria = null;

                searchCriteria = new ArrayList<>();
                searchCriteria.add(new KeyValue("mClass.type", "Dagang", true));
                searchCriteria.add(new KeyValue("txTmGeneral.id", txTmGeneral.getId(), true));
                long countDagang = permohonanOnlineService.countTxTmClass(searchCriteria);

                searchCriteria = new ArrayList<>();
                searchCriteria.add(new KeyValue("mClass.type", "Jasa", true));
                searchCriteria.add(new KeyValue("txTmGeneral.id", txTmGeneral.getId(), true));
                long countJasa = permohonanOnlineService.countTxTmClass(searchCriteria);

                if (countDagang == 0 || countJasa == 0) {
                    msg.setKey("Error");
                    msg.setValue("countDagangDanJasa");
                }
            }
            
            // Validasi file
            String docIdTempTTDK = "";
            String docIdTempF19 = "";
            String docIdTempF28 = "";
            for (JsonNode node : rootNode) {
                String docId = node.get("docId").toString().replaceAll("\"", "");
                String docFileName = node.get("docFileName").toString().replaceAll("\"", "");
                
                if((docId.equalsIgnoreCase("TTDP") || docId.equalsIgnoreCase("TTDK") || docId.equalsIgnoreCase("TTD")) && !(docFileName.toUpperCase().endsWith(".JPG") || docFileName.toUpperCase().endsWith(".JPEG"))) {
                	msg.setKey("fileTypeError");
                    msg.setValue("notMatchFileTypeImage");
                } else if (!(docId.equalsIgnoreCase("TTDP") || docId.equalsIgnoreCase("TTDK") || docId.equalsIgnoreCase("TTD")) && !docFileName.toUpperCase().endsWith(".PDF")) {
                	msg.setKey("fileTypeError");
                    msg.setValue("notMatchFileTypePdf");
                } else if(docId.equalsIgnoreCase("TTDK")) {
                	docIdTempTTDK = docId;
                } else if(docId.equalsIgnoreCase("F28")) {
                    docIdTempF28 = docId;
                } else if(docId.equalsIgnoreCase("F19")) {
                    docIdTempF19 = docId;
                }
            }

            if(txTmGeneral.getTxReception().getmFileTypeDetail().getId().equalsIgnoreCase("UMKM") && (docIdTempF19.isEmpty() && docIdTempF28.isEmpty())) {
                msg.setKey("fileTypeError");
                msg.setValue("requiredDocTypeDP");
            }

            if(!txTmGeneral.getTxTmRepresentative().isEmpty() && docIdTempTTDK.isEmpty()) {
            	msg.setKey("fileTypeError");
                msg.setValue("requiredTTDK");
            }
            
            response.setStatus( HttpServletResponse.SC_OK );
            writeJsonResponse( response, msg );
        } catch (DataIntegrityViolationException e) {
            logger.error(e.getMessage(), e);
            model.addAttribute("errorMessage", "Gagal menambahkan data lampiran");
            writeJsonResponse(response, 500);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            writeJsonResponse(response, 500);
        }
    }
    
    @RequestMapping(value = REQUEST_MAPPING_SAVE_FORM_DOKUMEN_ONLINE, method = {RequestMethod.POST})
    public void doSaveDocOnline(@RequestParam("appNo") String appNo, @RequestParam("docList") String docList, @RequestParam("docFiles") MultipartFile uploadFile, 
            final Model model, final HttpServletRequest request, final HttpServletResponse response) {
    	
    	try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(docList);

            TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(appNo);
            KeyValue msg = new KeyValue();
            msg.setKey("Success");
            msg.setValue("Input Data Success");

            // Validasi file
            for (JsonNode node : rootNode) {
                String docId = node.get("docId").toString().replaceAll("\"", "");
                String docFileName = node.get("docFileName").toString().replaceAll("\"", "");
                
                if((docId.equalsIgnoreCase("TTDP") || docId.equalsIgnoreCase("TTDK")|| docId.equalsIgnoreCase("TTD")) && !(docFileName.toUpperCase().endsWith(".JPG") || docFileName.toUpperCase().endsWith(".JPEG"))) {
                	msg.setKey("fileTypeError");
                    msg.setValue("notMatchFileTypeImage");
                } else if (!(docId.equalsIgnoreCase("TTDP") || docId.equalsIgnoreCase("TTDK")|| docId.equalsIgnoreCase("TTD")) && !docFileName.toUpperCase().endsWith(".PDF")) {
                	msg.setKey("fileTypeError");
                    msg.setValue("notMatchFileTypePdf");
                }
            }

            if(!msg.getKey().equalsIgnoreCase("fileTypeError")) {
	            for (JsonNode node : rootNode) {
	                String docId = node.get("docId").toString().replaceAll("\"", "");
	                String docFileName = node.get("docFileName").toString().replaceAll("\"", "");
	                String docDesc = node.get("docDesc").toString().replaceAll("\"", "");
	                String docFileSize = node.get("docFileSize").toString().replaceAll("\"", "");
	                String[] docFile = request.getParameter("file-" + docId).split(",");
	                ;
	
	                TxTmDoc txTmDoc = new TxTmDoc();
	                txTmDoc.setCreatedDate(new Timestamp(System.currentTimeMillis()));
	                txTmDoc.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
	                txTmDoc.setTxTmGeneral(txTmGeneral);
	                txTmDoc.setmDocType(doclampiranService.getDocTypeById(docId));
	                txTmDoc.setDescription(docDesc);
	                txTmDoc.setFileName(docFileName);
	                txTmDoc.setFileSize(docFileSize);
	                txTmDoc.setStatus(false);

	                String oldFileName = null;
	                if (uploadFile != null && (docId.equalsIgnoreCase("TTDP") || docId.equalsIgnoreCase("TTDK")|| docId.equalsIgnoreCase("TTD"))) {
	                    oldFileName = txTmDoc.getFileName();
	                    txTmDoc.setFileImageTtd(uploadFile);
	                    txTmDoc.setFileName(docFileName);
	                }
	                if (!doclampiranService.saveDocPermohonanOnline(txTmDoc, docFile[1], oldFileName)){
                        msg.setKey("fileTypeError");
                        msg.setValue("notMatchFileTypeImage");
                        response.setStatus( HttpServletResponse.SC_OK );
                        writeJsonResponse(response, msg);
                        return;
                    }
	            }
            }

            response.setStatus( HttpServletResponse.SC_OK );
            writeJsonResponse(response, msg);


        } catch (DataIntegrityViolationException e) {
            logger.error(e.getMessage(), e);
            writeJsonResponse(response, 500);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            writeJsonResponse(response, 500);
        } 
    }
    
    @RequestMapping(value = REQUEST_MAPPING_AJAX_DELETE_DOKUMEN, method = RequestMethod.POST)
    public void doDeleteDocOnline(Model model, @RequestParam("idDoc") String id, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        doclampiranService.deleteDokumen(id);
    }

    @PostMapping(value = REQUEST_MAPPING_SELESAi_PERMOHONAN_ONLINE)
    public void doSelesaiPermohonan(Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        String noPermohonan = request.getParameter("noPermohonan");

        if (isAjaxRequest(request)) {
            TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(noPermohonan);
            //List<TxTmDoc> txtmDocList = doclampiranService.getAllDocByApplicationId("txTmGeneral.id", txTmGeneral == null? "" : txTmGeneral.getId());
            KeyValue msg = new KeyValue();
            msg.setKey("Success");
            msg.setValue("Input Data Selesai");

            if (txTmGeneral.getTxTmOwner()==null) {
                msg.setKey("Error");
                msg.setValue("Tab Pemohon Belum Tersimpan");
            } else if (txTmGeneral.getTxTmBrand() == null) {
                msg.setKey("Error");
                msg.setValue("Tab Merek Belum Tersimpan");
            } else if (txTmGeneral.getTxTmClassList()==null) {
                msg.setKey("Error");
                msg.setValue("Tab Kelas Belum Tersimpan");
            } else if (txTmGeneral.getTxReception().getmFileType().getId().equals("MEREK_DAGANG")) {
                List<KeyValue> searchCriteria = null;

                searchCriteria = new ArrayList<>();
                searchCriteria.add(new KeyValue("mClass.type", "Dagang", true));
                searchCriteria.add(new KeyValue("txTmGeneral.id", txTmGeneral.getId(), true));
                long countDagang = permohonanOnlineService.countTxTmClass(searchCriteria);

                if (countDagang == 0) {
                    msg.setKey("Error");
                    msg.setValue("Untuk Tipe Permohonan: Merek Dagang, Pilihan Kelas harus meliputi Kelas Dagang (Kelas : 1-34).");
                }
            } else if (txTmGeneral.getTxReception().getmFileType().getId().equals("MEREK_JASA")) {
                List<KeyValue> searchCriteria = null;

                searchCriteria = new ArrayList<>();
                searchCriteria.add(new KeyValue("mClass.type", "Jasa", true));
                searchCriteria.add(new KeyValue("txTmGeneral.id", txTmGeneral.getId(), true));
                long countJasa = permohonanOnlineService.countTxTmClass(searchCriteria);

                if (countJasa == 0) {
                    msg.setKey("Error");
                    msg.setValue("Untuk Tipe Permohonan: Merek Jasa, Pilihan Kelas harus meliputi Kelas Jasa (Kelas : 35-45).");
                }
            } else if (txTmGeneral.getTxReception().getmFileType().getId().equals("MEREK_DAGANG_JASA")) {
                List<KeyValue> searchCriteria = null;

                searchCriteria = new ArrayList<>();
                searchCriteria.add(new KeyValue("mClass.type", "Dagang", true));
                searchCriteria.add(new KeyValue("txTmGeneral.id", txTmGeneral.getId(), true));
                long countDagang = permohonanOnlineService.countTxTmClass(searchCriteria);

                searchCriteria = new ArrayList<>();
                searchCriteria.add(new KeyValue("mClass.type", "Jasa", true));
                searchCriteria.add(new KeyValue("txTmGeneral.id", txTmGeneral.getId(), true));
                long countJasa = permohonanOnlineService.countTxTmClass(searchCriteria);

                if (countDagang == 0 || countJasa == 0) {
                    msg.setKey("Error");
                    msg.setValue("Untuk Tipe Permohonan: Merek Dagang dan Jasa, Pilihan Kelas harus meliputi Kelas Dagang (Kelas : 1-34) dan Kelas Jasa (Kelas : 35-45).");
                }
            }

            List<KeyValue> searchCriteria = new ArrayList<>();
            searchCriteria.add(new KeyValue("mDocType.statusFlag", true, true));
            searchCriteria.add(new KeyValue("mFileType.id", txTmGeneral.getTxReception().getmFileType().getId(), true));

            // Query txTmClass untuk mencari jika ada PENDING permintaan kelas khusus!
            List<TxTmClass> pendingList = new ArrayList<>();
            List<TxTmClass> txTmClasses = classService.findTxTmClassByGeneralId(txTmGeneral);
            for(TxTmClass txTmClass: txTmClasses) {
                if(txTmClass.getTransactionStatus().equalsIgnoreCase("PENDING")) {
                    pendingList.add(txTmClass);
                }
            }

            List<MDocTypeDetail> listDocType = docTypeService.selectAllDetail(searchCriteria);
            List<TxTmDoc> txTmDocList = txTmGeneral.getTxTmDocList();
            for (MDocTypeDetail docType : listDocType) {
                boolean check = false;
                if(docType.getmDocType().getMandatoryType() == null) {
                    continue;
                } else if(docType.getmDocType().getMandatoryType().equals("GENERAL")) {
                    check = true;
                } else if(docType.getmDocType().getMandatoryType().equals("OWNER")) {
                    if(txTmGeneral.getTxTmOwner().size() > 0) {
                        check = true;
                    }
                } else if(docType.getmDocType().getMandatoryType().equals("REPRS")) {
                    if(txTmGeneral.getTxTmRepresentative().size() > 0) {
                        check = true;
                    }
                } else if(docType.getmDocType().getMandatoryType().equals("PRIOR")) {
                    if(txTmGeneral.getTxTmPriorList().size() > 0) {
                        check = true;
                    }
                } else if(docType.getmDocType().getMandatoryType().equals("BRAND")) {
                    if(txTmGeneral.getTxTmBrand() != null) {
                        check = true;
                    }
                } else if(docType.getmDocType().getMandatoryType().equals("CLASS")) {
                    if(txTmGeneral.getTxTmClassList().size() > 0) {
                        check = true;
                    }
                }
                if(check) {
                    boolean cek = true;
                    for (TxTmDoc txTmDoc : txTmDocList) {
                        if(docType.getmDocType().getId().equals(txTmDoc.getmDocType().getId())) {
                            cek = false;
                        }
                    }
                    if(cek) {
                        msg.setKey("Error");
                        msg.setValue("Dokumen " + docType.getmDocType().getName() + " Belum Dilampirkan");
                        break;
                    }
                }
            }
            
            String docIdTempTTDP = "";
            String docIdTempTTDK = "";
            String docFileNameTTDP = "";
            String docFileNameTTDK = "";

            String docIdTempDokPendukung = "";

            for (TxTmDoc txTmDoc : txTmDocList) {
                if(txTmDoc.getmDocType().getId().equalsIgnoreCase("TTDP")) {
                	docIdTempTTDP = txTmDoc.getmDocType().getId();
                	docFileNameTTDP = txTmDoc.getFileName();
                } else if(txTmDoc.getmDocType().getId().equalsIgnoreCase("TTDK")) {
                	docIdTempTTDK = txTmDoc.getmDocType().getId();
                	docFileNameTTDK = txTmDoc.getFileName();
                } else if(txTmDoc.getmDocType().getId().equalsIgnoreCase("F28")) {
                    docIdTempDokPendukung = txTmDoc.getmDocType().getId();
                } else if(txTmDoc.getmDocType().getId().equalsIgnoreCase("F19")) {
                    docIdTempDokPendukung = txTmDoc.getmDocType().getId();
                }

            }
            
            try {
            	if(docIdTempTTDP.isEmpty()) {
            		msg.setKey("Error");
                    msg.setValue("Dokumen Tanda Tangan Pemohon Belum Dilampirkan");
               } else if((!txTmGeneral.getTxTmRepresentative().isEmpty()) && docIdTempTTDK.isEmpty()) {
               		msg.setKey("Error");
                    msg.setValue("Dokumen Tanda Tangan Kuasa Belum Dilampirkan");
               } else if(txTmGeneral.getTxReception().getmFileTypeDetail().getId().equalsIgnoreCase("UMKM") && docIdTempDokPendukung.isEmpty()) {
                    msg.setKey("Error");
                    msg.setValue("Untuk Jenis Permohonan UMKM, Wajib Melampirkan Jenis Dokumen: Surat UMKM Asli dan Surat Pernyataan UKM");
               } else if(docFileNameTTDP.equalsIgnoreCase(txTmGeneral.getTxTmBrand().getFileName()) 
               		|| docFileNameTTDK.equalsIgnoreCase(txTmGeneral.getTxTmBrand().getFileName())) {
               		msg.setKey("Error");
                    msg.setValue("File Gambar Label Merek Yang Anda Upload Terindikasi Sama Dengan File Gambar Tanda Tangan Kuasa/Pemohon, "
                   		+ "Mohon Periksa Kembali. Pastikan Gambar, Nama file Label Merek dengan Tanda Tangan Pemohon/Kuasa berbeda "
                   		+ "dan Kemudian Klik Simpan dan Lanjutkan !!");
               } else if(pendingList.size()>0){
            	    msg.setKey("Error");
            	    msg.setValue("Gagal submit pengajuan permohonan, karena masih ada permintaan jenis barang/jasa yang belum diperiksa oleh petugas. Silakan cek kembali Tab 6 (Kelas).");
                }
            } catch (Exception e) {
            }

            if (msg.getKey().equalsIgnoreCase("Success")) {
            	txTmGeneral.setFilingDate(new Timestamp(System.currentTimeMillis()));	
                trademarkService.updateGeneralOnline(txTmGeneral);
                
                //CETAK MEREK AND SURAT PERNYATAAN SAVE ON PATH
                try {
                    List<CetakMerek> dataListCetakMerek = new ArrayList<CetakMerek>();
                    CetakMerek dataCetakMerek = null;
                    
                    List<CetakSuratPernyataan> dataListSuratPernyataan = new ArrayList<CetakSuratPernyataan>();
                    CetakSuratPernyataan dataSuratPernyataan = new CetakSuratPernyataan();
                    Timestamp createdDate = new Timestamp(System.currentTimeMillis());
                    SimpleDateFormat fmtDate= new SimpleDateFormat("dd MMMM yyyy", new Locale("ID"));

                    TxTmBrand txTmBrand  =  trademarkService.selectOneTxtmBrandBy("txTmGeneral.id",txTmGeneral.getId());

                    dataCetakMerek = new CetakMerek(1, "Data Permohonan", "Application");
                    dataCetakMerek.setApplicationNo(txTmGeneral.getApplicationNo());
                    dataCetakMerek.seteFilingNo(txTmGeneral.getTxReception().geteFilingNo());
                    dataCetakMerek.setFileSequence(txTmGeneral.getTxReception().getmFileSequence().getDesc());
                    dataCetakMerek.setFileType(txTmGeneral.getTxReception().getmFileType().getDesc());
                    dataCetakMerek.setFileTypeDetail(txTmGeneral.getTxReception().getmFileTypeDetail().getDesc());
                    dataCetakMerek.setApplicationDate(txTmGeneral.getTxReception().getApplicationDate());
                    dataCetakMerek.setFillingDate(txTmGeneral.getFilingDate());
                    dataListCetakMerek.add(dataCetakMerek);

                    dataCetakMerek = new CetakMerek(2, "Data Merek", "Description of Mark");
                    if (txTmGeneral.getTxTmBrand() != null) {
                    	dataCetakMerek.setBrandName(txTmGeneral.getTxTmBrand().getName());
                    	dataCetakMerek.setBrandType(txTmGeneral.getTxTmBrand().getmBrandType().getName());
                    	dataCetakMerek.setBrandColor(txTmGeneral.getTxTmBrand().getColor());
                    	dataCetakMerek.setBrandDescription(txTmGeneral.getTxTmBrand().getDescription());
                    	dataCetakMerek.setBrandDescChar(txTmGeneral.getTxTmBrand().getDescChar());
                    	dataCetakMerek.setBrandTranslation(txTmGeneral.getTxTmBrand().getTranslation()); 
                    	dataCetakMerek.setBrandDisclaimer(txTmGeneral.getTxTmBrand().getDisclaimer());
                    	dataCetakMerek.setBrandFilename(txTmGeneral.getTxTmBrand().getId() + ".jpg");
                    	
                    	try {
            	            String brandLogo = txTmGeneral.getTxTmBrand().getId() + ".jpg";
            	            String pathFolderLogo = DateUtil.formatDate(txTmGeneral.getTxTmBrand().getCreatedDate(), "yyyy/MM/dd/");
            	        	String pathBrandLogo = brandLogo == null ? "" : uploadFileBrandPath + pathFolderLogo + brandLogo;
            	        	
            	        	dataSuratPernyataan.setBrandName(txTmGeneral.getTxTmBrand() == null ? "" : txTmGeneral.getTxTmBrand().getName());
            	        	dataSuratPernyataan.setBrandLogo(txTmGeneral.getTxTmBrand().getId() == null ? "" : pathBrandLogo);
                	 	}catch (NullPointerException e) {
                        }
                    	dataSuratPernyataan.setCreatedDate(fmtDate.format(createdDate).toString());
                    }
                    dataListCetakMerek.add(dataCetakMerek);

                    List<TxTmOwner> txTmOwnerList = permohonanService.selectAllOwnerByIdGenerals(txTmGeneral.getId(), true);
                    if (txTmOwnerList.size() == 0) {
                    	dataCetakMerek = new CetakMerek(3, "Data Pemohon", "Applicant");
                    	dataListCetakMerek.add(dataCetakMerek);
                    } else {
                    	for (TxTmOwner owners : txTmOwnerList) {
                    		dataCetakMerek = new CetakMerek(3, "Data Pemohon", "Applicant");
                    		dataCetakMerek.setOwnerName(owners.getName());
                    		dataCetakMerek.setOwnerType(owners.getOwnerType());
                    		dataCetakMerek.setOwnerNationality(owners.getNationality().getName());
                    		dataCetakMerek.setOwnerAddress(owners.getAddress());
                    		dataCetakMerek.setOwnerCity(owners.getmCity() == null ? null : owners.getmCity().getName());
                    		dataCetakMerek.setOwnerProvince(owners.getmProvince() == null ? null : owners.getmProvince().getName());
                    		dataCetakMerek.setOwnerCountry(owners.getmCountry() == null ? null : owners.getmCountry().getName());
                    		dataCetakMerek.setOwnerZipCode(owners.getZipCode());
                    		dataCetakMerek.setOwnerPhone(owners.getPhone());
                    		dataCetakMerek.setOwnerEmail(owners.getEmail());
                    		dataListCetakMerek.add(dataCetakMerek);
                    		
                    		dataSuratPernyataan.setOwnerName(owners.getName());
                    		dataSuratPernyataan.setOwnerAddress(owners.getAddress());
                    		dataSuratPernyataan.setOwnerOrReprsName(owners.getName());
                    	}
                    }
                    
                    if (txTmOwnerList.size() == 0) {
                    	dataCetakMerek = new CetakMerek(4, "Alamat Surat Menyurat", "Mailing Address");
                    	dataListCetakMerek.add(dataCetakMerek);
                    }
                    else {
                    	for (TxTmOwner owners : txTmOwnerList) {
                    		dataCetakMerek = new CetakMerek(4, "Alamat Surat Menyurat", "Mailing Address");
                    		dataCetakMerek.setOwnerPostAddress(owners.getPostAddress());
                    		dataCetakMerek.setOwnerPostCity(owners.getPostCity() == null ? null : owners.getPostCity().getName());
                    		dataCetakMerek.setOwnerPostProvince(owners.getPostProvince() == null ? null : owners.getPostProvince().getName());
                    		dataCetakMerek.setOwnerPostCountry(owners.getPostCountry() == null ? null : owners.getPostCountry().getName());
                    		dataCetakMerek.setOwnerPostZipCode(owners.getPostZipCode());
                    		dataCetakMerek.setOwnerPostPhone(owners.getPostPhone());
                    		dataCetakMerek.setOwnerPostEmail(owners.getPostEmail());
                    		dataListCetakMerek.add(dataCetakMerek);
                    	}
                    }

                    List<TxTmRepresentative> txTmReprsList = permohonanService.selectTxTmReprsByGeneralId(txTmGeneral.getId(), true);
                    if (txTmReprsList.size() == 0) {
                    	dataCetakMerek = new CetakMerek(5, "Data Kuasa", "Representative/IP Consultant");
                    	dataListCetakMerek.add(dataCetakMerek);
                    } else {
                    	for(TxTmRepresentative txTmReprs : txTmReprsList) {
                    		dataCetakMerek = new CetakMerek(5, "Data Kuasa", "Representative/IP Consultant");
                    		dataCetakMerek.setReprsNo(txTmReprs.getmRepresentative().getNo());
                    		dataCetakMerek.setReprsName(txTmReprs.getmRepresentative().getName());
                    		dataCetakMerek.setReprsName(txTmReprs.getmRepresentative().getName());
                    		dataCetakMerek.setReprsOffice(txTmReprs.getmRepresentative().getOffice());

//                    		dataCetakMerek.setReprsAddress(txTmReprs.getmRepresentative().getAddress());
//                    		dataCetakMerek.setReprsPhone(txTmReprs.getmRepresentative().getPhone());
//                            dataCetakMerek.setReprsEmail(txTmReprs.getmRepresentative().getEmail());
                            /**---GET FIELD ADDRESS, PHONE, EMAIL FROM TX_TM_REPRS **/
                            dataCetakMerek.setReprsAddress( txTmReprs.getAddress() );
                            dataCetakMerek.setReprsPhone( txTmReprs.getPhone() );
                            dataCetakMerek.setReprsEmail( txTmReprs.getEmail() );
                            dataListCetakMerek.add(dataCetakMerek);
                    	}
                    }

                    List<TxTmPrior> txTmPriorList = permohonanService.selectAllPriorByGeneralId(txTmGeneral.getId());
                    if (txTmPriorList.size() == 0) {
                    	dataCetakMerek = new CetakMerek(6, "Data Prioritas", "Priority Data");
                    	dataListCetakMerek.add(dataCetakMerek);
                    } else {
                        int count = 1;
                        for (TxTmPrior txTmPrior : txTmPriorList) {
                        	dataCetakMerek = new CetakMerek(6, "Data Prioritas", "Priority Data");
                            dataCetakMerek.setPriorOrder("" + count++);
                            dataCetakMerek.setPriorNo(txTmPrior.getNo());
                            dataCetakMerek.setPriorCountry(txTmPrior.getmCountry().getName());
                            dataCetakMerek.setPriorDate(txTmPrior.getPriorDate());
                            dataListCetakMerek.add(dataCetakMerek);
                        }
                    }

                    if (txTmGeneral.getTxTmClassList().size() == 0) {
                    	dataCetakMerek = new CetakMerek(7, "Data Kelas", "Class");
                    	dataListCetakMerek.add(dataCetakMerek);
                    } else {
                        Map<Integer, String[]> mapClass = new HashMap<>();
                        List<TxTmClass> txTmClassList = classService.selectAllClassByIdGeneral(txTmGeneral.getId(), ClassStatusEnum.ACCEPT.name());
                        for (TxTmClass result : txTmClassList) {
                            int key = result.getmClass().getNo();
                            String desc = result.getmClassDetail().getDesc();
                            String descEn = result.getmClassDetail().getDescEn();

                            if (mapClass.containsKey(key)) {
                                desc = mapClass.get(key)[0] + "; " + desc;
                                descEn = mapClass.get(key)[1] + "; " + descEn;
                            }

                            mapClass.put(key, new String[]{desc, descEn});
                        }
                        for (Map.Entry<Integer, String[]> entry : mapClass.entrySet()) {
                            String desc = entry.getValue()[0].replaceAll(" ", "").replaceAll(";", "");
                            String descEn = entry.getValue()[1] == null ? "" : entry.getValue()[1].replaceAll(" ", "").replaceAll(";", "");

                            dataCetakMerek = new CetakMerek(7, "Data Kelas", "Class");
                            dataCetakMerek.setClassNo("" + entry.getKey());
                            dataCetakMerek.setClassDesc(desc.length() == 0 ? " - " : entry.getValue()[0]);
                            dataCetakMerek.setClassDescEn(descEn.length() == 0 ? " - " : entry.getValue()[1]);
                            dataListCetakMerek.add(dataCetakMerek);
                        }
                    }
    	        	
                    String tandaTanganDigital = "";
                	String pathFolderTandaTanganDigital = "";
                	String tandaTanganDigitalSuratPernyataan = "";
                	
                	String docIdTTDPTemp = "";
                	String docIdTTDKTemp = "";
                	String txTmDocDateTTDK = "";
                	String txTmDocDateTTDP = "";
                    List<TxTmDoc> txtmDocList = doclampiranService.getAllDocByApplicationId("txTmGeneral.id", txTmGeneral.getId());
                    if (txtmDocList.size() == 0) {
                    	dataCetakMerek = new CetakMerek(8, "Dokumen Lampiran", "Attachment");
                        dataListCetakMerek.add(dataCetakMerek);
                    } else {
                        Collections.sort(txtmDocList, (o1, o2) -> o1.getmDocType().getName().compareTo(o2.getmDocType().getName()));
                        for (TxTmDoc txTmDoc : txtmDocList) {
                        	if(txTmDoc.getmDocType().getId().equalsIgnoreCase("TTDK")) {
                        		docIdTTDKTemp = txTmDoc.getFileNameTemp();
                        		txTmDocDateTTDK = DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
                        	} else if (txTmDoc.getmDocType().getId().equalsIgnoreCase("TTDP")) {
                        		docIdTTDPTemp = txTmDoc.getFileNameTemp();
                        		txTmDocDateTTDP = DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
                            	tandaTanganDigitalSuratPernyataan = txTmDoc.getFileNameTemp();
                        	}
                    		
                        	dataCetakMerek = new CetakMerek(8, "Dokumen Lampiran", "Attachment");
                        	dataCetakMerek.setDocName(txTmDoc.getmDocType().getName());
                        	dataListCetakMerek.add(dataCetakMerek);
                        }
                    }
                    if(txTmReprsList.size() > 0 && !docIdTTDKTemp.isEmpty()) {
                    	tandaTanganDigital = docIdTTDKTemp; 
                    	pathFolderTandaTanganDigital = txTmDocDateTTDK;
                    } else {
                    	tandaTanganDigital = docIdTTDPTemp; 
                    	pathFolderTandaTanganDigital = txTmDocDateTTDP;
                    }
                    
                    // Data Pemohon Tambahan
                    List<TxTmOwnerDetail> txOwnerDetail = null;
                    for (TxTmOwner owners : txTmOwnerList) {
                    	txOwnerDetail = permohonanService.selectTxTmOwnerDetailByTxTmOwner(owners.getId(), true);
                    	if (txOwnerDetail.size() == 0) {
                    		dataCetakMerek = new CetakMerek(9, "Identitas pemohon jika pemohon lebih dari satu pihak", "Additional Applicant");
                        	try {
                        		dataCetakMerek.setNo(null);
                        	} catch (NumberFormatException e) {
                        	}
                        	dataCetakMerek.setOwnerDetailName("");
                        	dataListCetakMerek.add(dataCetakMerek);
                        } else {
                        	int no = 0;
                        	for (TxTmOwnerDetail ownerDetail : txOwnerDetail) {
                        		no++;
                        		dataCetakMerek = new CetakMerek(9, "Identitas pemohon jika pemohon lebih dari satu pihak", "Additional Applicant");
                        		dataCetakMerek.setNo(no);
                        		dataCetakMerek.setOwnerDetailName(ownerDetail.getName());
                        		dataListCetakMerek.add(dataCetakMerek);
                        	}
                        }
                    }
                    
                    // Data Gambar Logo Merek Tambahan
                    List<TxTmBrandDetail> txTmBrandDetail = brandService.selectTxTmBrandDetailByTxTmBrand(txTmBrand.getId());
                    if (txTmBrandDetail.size() == 0) {
                    	dataCetakMerek = new CetakMerek(10, "Gambar Merek Tambahan", "Additional Mark");
                    	try {
                    		dataCetakMerek.setNoBrand(null);
                    	} catch (NumberFormatException e) {
                    	}
                    	dataCetakMerek.setBrandLogo("");
                    	dataListCetakMerek.add(dataCetakMerek);
                    } else {
                    	int no = 0;
                    	for(TxTmBrandDetail brandDetail : txTmBrandDetail) {
                    		no++;
                        	String brandLogo = brandDetail.getId() + ".jpg";
                        	String pathFolder = DateUtil.formatDate(txTmGeneral.getTxTmBrand().getCreatedDate(), "yyyy/MM/dd/");
                        	String pathBrandLogo = brandLogo == null ? "" : uploadFileBrandDetailPath + pathFolder + brandLogo;
                        	
                        	dataCetakMerek = new CetakMerek(10, "Gambar Merek Tambahan", "Additional Mark");
                        	dataCetakMerek.setNoBrand(no);
                        	dataCetakMerek.setBrandLogo(brandDetail.getId() == null ? "" : pathBrandLogo);
                        	dataListCetakMerek.add(dataCetakMerek);
                        }
                    }
                    
                    String pathFolder = DateUtil.formatDate(txTmGeneral.getTxTmBrand().getCreatedDate(), "yyyy/MM/dd/");
                    dataListSuratPernyataan.add(dataSuratPernyataan);

                    ClassLoader classLoader = getClass().getClassLoader();
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("pUploadFilePath", uploadFilePathSignature);
                    params.put("pUploadFileBrandPath", uploadFileBrandPath + pathFolder);
                    params.put("pSignImage", "signimage.jpg");
                    
                    params.put("pUploadFileTandaTanganDigital", uploadFileImageTandaTangan + pathFolderTandaTanganDigital);
                    params.put("pTtdImage", tandaTanganDigital);
                    
                    List<Object> addListOwnerDetail = new ArrayList<>();
                    for (CetakMerek datas : dataListCetakMerek) {
                    	if(datas.getOwnerDetailName() != null) {
                    		addListOwnerDetail.add(datas);
                    		params.put("daftarPemohonTambahan", new JRBeanCollectionDataSource(addListOwnerDetail));
                    	}
                    }
                    
                    List<Object> addListBrandDetail = new ArrayList<>();
                    for (CetakMerek datas : dataListCetakMerek) {
                    	if(datas.getBrandLogo() != null) {
                    		addListBrandDetail.add(datas);
                    		params.put("daftarMerekTambahan", new JRBeanCollectionDataSource(addListBrandDetail));
                    	}
                    }
                    if(txTmReprsList.size() > 0) {
                    	for(TxTmRepresentative txTmReprs : txTmReprsList) {
                        	params.put("pSignFullName", txTmReprs == null ? "" : txTmReprs.getmRepresentative().getName());
                        }
                    } else {
                    	for(TxTmOwner txTmOwner : txTmOwnerList) {
                    		params.put("pSignFullName", txTmOwner == null ? "" : txTmOwner.getName());
                    	}
                    }
                    
                    params.put("pSignPlaceDate", "Jakarta, " + DateUtil.formatDate(txTmGeneral.getTxReception().getApplicationDate(), "dd-MM-yyyy"));

                    File file = new File(classLoader.getResource("report/CetakMerek.jasper").getFile());
                    InputStream jasperStream1 = new FileInputStream(file);
                    JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream1);
                    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JRBeanCollectionDataSource(dataListCetakMerek));

                    ByteArrayOutputStream output = new ByteArrayOutputStream();

                    JRPdfExporter exporter = new JRPdfExporter();
                    exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(output));
                    exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    exporter.exportReport();

                    byte[] result;
                    
                    try (ByteArrayOutputStream msOut = new ByteArrayOutputStream()) {
                        Document document = new Document();
                        PdfCopy copy = new PdfCopy(document, msOut);
                        document.open();

                        ByteArrayInputStream bs = new ByteArrayInputStream(output.toByteArray());
                        PdfReader jasperReader = new PdfReader(bs); //Reader for Jasper Report
                        for (int page = 0; page < jasperReader.getNumberOfPages(); ) {
                            copy.addPage(copy.getImportedPage(jasperReader, ++page));
                        }
                        jasperReader.close();
                        
                        for (TxTmDoc txTmDoc : txtmDocList) {
                            if (txTmDoc.getFileName() != null) {
                            	if(txTmDoc.getFileName().toUpperCase().endsWith(".JPG") || txTmDoc.getFileName().toUpperCase().endsWith(".JPEG")) {
                            		continue;
                            	}
                                String pathFolderDoc = DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
                                PdfReader nonJasperReader = new PdfReader(uploadFileDocApplicationPath + pathFolderDoc + txTmDoc.getFileNameTemp()); // Reader for the Pdf to be attached (not .jasper file)
                                for (int page = 0; page < nonJasperReader.getNumberOfPages(); ) {
                                    copy.addPage(copy.getImportedPage(nonJasperReader, ++page));
                                }
                                nonJasperReader.close();
                            }
                        }
                        document.close();

                        result = msOut.toByteArray();
                    }
                    String folderDoc = downloadFileDocPermohonanCetakMerekPath + DateUtil.formatDate(txTmGeneral.getCreatedDate(), "yyyy/MM/dd/");
                    String filenameDoc = "CetakMerek-" + txTmGeneral.getApplicationNo() + ".pdf";
                    
        	        Path pathDir = Paths.get(folderDoc);
        	        Path pathFile = Paths.get(folderDoc + filenameDoc);
        	        if (!Files.exists(pathDir)) {
        	            Files.createDirectories(pathDir);
        	        }
        	        if (!Files.exists(pathFile)) {
        	            Files.createFile(pathFile);
        	        }
        	        OutputStream outputStream = new FileOutputStream(pathFile.toFile());
        	        
                    response.setContentType("application/pdf");
                    response.setHeader("Content-disposition", "attachment; filename=CetakMerek.pdf");
                    //response.getOutputStream().write(result);
                    InputStream is = new ByteArrayInputStream(result);
                    //this.signPdf(is, response.getOutputStream());
                    this.signPdf(is, outputStream);
                    outputStream.close();

                    ClassLoader classLoaderSuratPernyataan = getClass().getClassLoader();
                    Map<String, Object> paramsSrtPrnyataan = new HashMap<String, Object>();
                    paramsSrtPrnyataan.put("pUploadFileTandaTanganDigital", uploadFileImageTandaTangan + pathFolderTandaTanganDigital);
                    paramsSrtPrnyataan.put("pTtdImage", tandaTanganDigitalSuratPernyataan);
                    
                    File fileSuratPernyataan = new File(classLoaderSuratPernyataan.getResource("report/CetakSuratPernyataan.jasper").getFile());
                    InputStream jasperStream1SuratPernyataan = new FileInputStream(fileSuratPernyataan);
                    JasperReport jasperReportSuratPernyataan = (JasperReport) JRLoader.loadObject(jasperStream1SuratPernyataan);
                    JasperPrint jasperPrintSuratPernyataan = JasperFillManager.fillReport(jasperReportSuratPernyataan, paramsSrtPrnyataan, new JRBeanCollectionDataSource(dataListSuratPernyataan));
                	
                    String folderDocSuratPernyataan = downloadFileDocPermohonanCetakMerekPath + DateUtil.formatDate(txTmGeneral.getCreatedDate(), "yyyy/MM/dd/");
                    String filenameDocSuratPernyataan = "CetakSuratPernyataan-" + txTmGeneral.getApplicationNo() + ".pdf";
                    
                    Path pathDirSuratPernyataan = Paths.get(folderDocSuratPernyataan);
        	        Path pathFileSuratPernyataan = Paths.get(folderDocSuratPernyataan + filenameDocSuratPernyataan);
        	        if (!Files.exists(pathDirSuratPernyataan)) {
        	            Files.createDirectories(pathDirSuratPernyataan);
        	        }
        	        if (!Files.exists(pathFileSuratPernyataan)) {
        	            Files.createFile(pathFileSuratPernyataan);
        	        }
        	        
        	        OutputStream outputStreamSuratPernyataan = new FileOutputStream(pathFileSuratPernyataan.toFile());
                    response.setContentType("application/pdf");
                    response.setHeader("Content-disposition", "attachment; filename=CetakSuratPernyataan.pdf");
        	        JasperExportManager.exportReportToPdfStream(jasperPrintSuratPernyataan, outputStreamSuratPernyataan);
        	        outputStreamSuratPernyataan.close();
                } catch (Exception ex) {
                    logger.error(ex);
                }
                //Set used blling code
                simpakiService.setUseBilling(txTmGeneral.getTxReception().getBankCode(), noPermohonan);
            }

            response.setStatus(HttpServletResponse.SC_OK);
            writeJsonResponse(response, msg);
        }
    }

    /* ---------------------------------------CETAK MEREK --------------------------------------- */
    @RequestMapping(value = REQUEST_MAPPING_CETAK_MEREK_ONLINE, method = {RequestMethod.GET})
    @ResponseBody
    public String doCetakMerek(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

        String applicationNo = request.getParameter("no");
        String dwnonly = request.getParameter("dwnonly");
    	TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(applicationNo);

        MUser userLogin = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userLogin.getUsername() != "super") {
            List<KeyValue> criteriaList = new ArrayList<>();
            criteriaList.add(new KeyValue("applicationNo", applicationNo, true));
            criteriaList.add(new KeyValue("createdBy", userLogin, true));
            txTmGeneral = trademarkService.selectOneGeneralByeFilingNoByUser(criteriaList);

            if (txTmGeneral == null) {
                response.sendRedirect( PATH_AFTER_LOGIN + PATH_PERMOHONAN_ONLINE + "?error=" +"Halaman tidak ditemukan" );
                return "";
            }
        }
    	

        String folder =  downloadFileDocPermohonanCetakMerekPath + DateUtil.formatDate(txTmGeneral.getCreatedDate(), "yyyy/MM/dd/");
        String fileName = "CetakMerek-" + txTmGeneral.getApplicationNo() + ".pdf";

        try {
            Path path = Paths.get(folder + fileName);
            if((Files.exists(path) && Files.size(path) > 0) || dwnonly != null) {
                response.setContentType("application/pdf");
                response.setHeader("Content-disposition", "attachment; filename=CetakMerek4-" + applicationNo + ".pdf");
                OutputStream output = response.getOutputStream();
                Files.copy(path, output);
                output.flush();
                return "";
            }
        } catch(IOException e) {
        }

        try {

            List<CetakMerek> dataListCetakMerek = new ArrayList<CetakMerek>();
            CetakMerek dataCetakMerek = null;

            TxTmBrand txTmBrand  =  trademarkService.selectOneTxtmBrandBy("txTmGeneral.id",txTmGeneral.getId());

            dataCetakMerek = new CetakMerek(1, "Data Permohonan", "Application");
            dataCetakMerek.setApplicationNo(txTmGeneral.getApplicationNo());
            dataCetakMerek.seteFilingNo(txTmGeneral.getTxReception().geteFilingNo());
            dataCetakMerek.setFileSequence(txTmGeneral.getTxReception().getmFileSequence().getDesc());
            dataCetakMerek.setFileType(txTmGeneral.getTxReception().getmFileType().getDesc());
            dataCetakMerek.setFileTypeDetail(txTmGeneral.getTxReception().getmFileTypeDetail().getDesc());
            dataCetakMerek.setApplicationDate(txTmGeneral.getTxReception().getApplicationDate());
            dataCetakMerek.setFillingDate(txTmGeneral.getFilingDate());
            dataListCetakMerek.add(dataCetakMerek);

            dataCetakMerek = new CetakMerek(2, "Data Merek", "Description of Mark");
            if (txTmGeneral.getTxTmBrand() != null) {
                dataCetakMerek.setBrandName(txTmGeneral.getTxTmBrand().getName());
                dataCetakMerek.setBrandType(txTmGeneral.getTxTmBrand().getmBrandType().getName());
                dataCetakMerek.setBrandColor(txTmGeneral.getTxTmBrand().getColor());
                dataCetakMerek.setBrandDescription(txTmGeneral.getTxTmBrand().getDescription());
                dataCetakMerek.setBrandDescChar(txTmGeneral.getTxTmBrand().getDescChar());
                dataCetakMerek.setBrandTranslation(txTmGeneral.getTxTmBrand().getTranslation());
                dataCetakMerek.setBrandDisclaimer(txTmGeneral.getTxTmBrand().getDisclaimer());
                dataCetakMerek.setBrandFilename(txTmGeneral.getTxTmBrand().getId() + ".jpg");

            }
            dataListCetakMerek.add(dataCetakMerek);

            List<TxTmOwner> txTmOwnerList = permohonanService.selectAllOwnerByIdGenerals(txTmGeneral.getId(), true);
            if (txTmOwnerList.size() == 0) {
                dataCetakMerek = new CetakMerek(3, "Data Pemohon", "Applicant");
                dataListCetakMerek.add(dataCetakMerek);
            } else {
                for (TxTmOwner owners : txTmOwnerList) {
                    dataCetakMerek = new CetakMerek(3, "Data Pemohon", "Applicant");
                    dataCetakMerek.setOwnerName(owners.getName());
                    dataCetakMerek.setOwnerType(owners.getOwnerType());
                    dataCetakMerek.setOwnerNationality(owners.getNationality().getName());
                    dataCetakMerek.setOwnerAddress(owners.getAddress());
                    dataCetakMerek.setOwnerCity(owners.getmCity() == null ? null : owners.getmCity().getName());
                    dataCetakMerek.setOwnerProvince(owners.getmProvince() == null ? null : owners.getmProvince().getName());
                    dataCetakMerek.setOwnerCountry(owners.getmCountry() == null ? null : owners.getmCountry().getName());
                    dataCetakMerek.setOwnerZipCode(owners.getZipCode());
                    dataCetakMerek.setOwnerPhone(owners.getPhone());
                    dataCetakMerek.setOwnerEmail(owners.getEmail());
                    dataListCetakMerek.add(dataCetakMerek);
                }
            }

            if (txTmOwnerList.size() == 0) {
                dataCetakMerek = new CetakMerek(4, "Alamat Surat Menyurat", "Mailing Address");
                dataListCetakMerek.add(dataCetakMerek);
            }
            else {
                for (TxTmOwner owners : txTmOwnerList) {
                    dataCetakMerek = new CetakMerek(4, "Alamat Surat Menyurat", "Mailing Address");
                    dataCetakMerek.setOwnerPostAddress(owners.getPostAddress());
                    dataCetakMerek.setOwnerPostCity(owners.getPostCity() == null ? null : owners.getPostCity().getName());
                    dataCetakMerek.setOwnerPostProvince(owners.getPostProvince() == null ? null : owners.getPostProvince().getName());
                    dataCetakMerek.setOwnerPostCountry(owners.getPostCountry() == null ? null : owners.getPostCountry().getName());
                    dataCetakMerek.setOwnerPostZipCode(owners.getPostZipCode());
                    dataCetakMerek.setOwnerPostPhone(owners.getPostPhone());
                    dataCetakMerek.setOwnerPostEmail(owners.getPostEmail());
                    dataListCetakMerek.add(dataCetakMerek);
                }
            }

            List<TxTmRepresentative> txTmReprsList = permohonanService.selectTxTmReprsByGeneralId(txTmGeneral.getId(), true);
            if (txTmReprsList.size() == 0) {
                dataCetakMerek = new CetakMerek(5, "Data Kuasa", "Representative/IP Consultant");
                dataListCetakMerek.add(dataCetakMerek);
            } else {
                for(TxTmRepresentative txTmReprs : txTmReprsList) {
                    dataCetakMerek = new CetakMerek(5, "Data Kuasa", "Representative/IP Consultant");
                    dataCetakMerek.setReprsNo(txTmReprs.getmRepresentative().getNo());
                    dataCetakMerek.setReprsName(txTmReprs.getmRepresentative().getName());
                    dataCetakMerek.setReprsName(txTmReprs.getmRepresentative().getName());
                    dataCetakMerek.setReprsOffice(txTmReprs.getmRepresentative().getOffice());

//            		dataCetakMerek.setReprsAddress(txTmReprs.getmRepresentative().getAddress());
//            		dataCetakMerek.setReprsPhone(txTmReprs.getmRepresentative().getPhone());
//                    dataCetakMerek.setReprsEmail(txTmReprs.getmRepresentative().getEmail());
                    /**---GET FIELD ADDRESS, PHONE, EMAIL FROM TX_TM_REPRS **/
                    dataCetakMerek.setReprsAddress( txTmReprs.getAddress() );
                    dataCetakMerek.setReprsPhone( txTmReprs.getPhone() );
                    dataCetakMerek.setReprsEmail( txTmReprs.getEmail() );
                    dataListCetakMerek.add(dataCetakMerek);
                }
            }

            List<TxTmPrior> txTmPriorList = permohonanService.selectAllPriorByGeneralId(txTmGeneral.getId());
            if (txTmPriorList.size() == 0) {
                dataCetakMerek = new CetakMerek(6, "Data Prioritas", "Priority Data");
                dataListCetakMerek.add(dataCetakMerek);
            } else {
                int count = 1;
                for (TxTmPrior txTmPrior : txTmPriorList) {
                    dataCetakMerek = new CetakMerek(6, "Data Prioritas", "Priority Data");
                    dataCetakMerek.setPriorOrder("" + count++);
                    dataCetakMerek.setPriorNo(txTmPrior.getNo());
                    dataCetakMerek.setPriorCountry(txTmPrior.getmCountry().getName());
                    dataCetakMerek.setPriorDate(txTmPrior.getPriorDate());
                    dataListCetakMerek.add(dataCetakMerek);
                }
            }

            if (txTmGeneral.getTxTmClassList().size() == 0) {
                dataCetakMerek = new CetakMerek(7, "Data Kelas", "Class");
                dataListCetakMerek.add(dataCetakMerek);
            } else {
                Map<Integer, String[]> mapClass = new HashMap<>();
                List<TxTmClass> txTmClassList = classService.selectAllClassByIdGeneral(txTmGeneral.getId(), ClassStatusEnum.ACCEPT.name());
                for (TxTmClass result : txTmClassList) {
                    int key = result.getmClass().getNo();
                    String desc = result.getmClassDetail().getDesc();
                    String descEn = result.getmClassDetail().getDescEn();

                    if (mapClass.containsKey(key)) {
                        desc = mapClass.get(key)[0] + "; " + desc;
                        descEn = mapClass.get(key)[1] + "; " + descEn;
                    }

                    mapClass.put(key, new String[]{desc, descEn});
                }
                for (Map.Entry<Integer, String[]> entry : mapClass.entrySet()) {
                    String desc = entry.getValue()[0].replaceAll(" ", "").replaceAll(";", "");
                    String descEn = entry.getValue()[1].replaceAll(" ", "").replaceAll(";", "");

                    dataCetakMerek = new CetakMerek(7, "Data Kelas", "Class");
                    dataCetakMerek.setClassNo("" + entry.getKey());
                    dataCetakMerek.setClassDesc(desc.length() == 0 ? " - " : entry.getValue()[0]);
                    dataCetakMerek.setClassDescEn(descEn.length() == 0 ? " - " : entry.getValue()[1]);
                    dataListCetakMerek.add(dataCetakMerek);
                }
            }

            String tandaTanganDigital = "";
            String pathFolderTandaTanganDigital = "";

            String docIdTTDPTemp = "";
            String docIdTTDKTemp = "";
            String txTmDocDate = "";
            List<TxTmDoc> txtmDocList = doclampiranService.getAllDocByApplicationId("txTmGeneral.id", txTmGeneral.getId());
            if (txtmDocList.size() == 0) {
                dataCetakMerek = new CetakMerek(8, "Dokumen Lampiran", "Attachment");
                dataListCetakMerek.add(dataCetakMerek);
            } else {
                Collections.sort(txtmDocList, (o1, o2) -> o1.getmDocType().getName().compareTo(o2.getmDocType().getName()));
                for (TxTmDoc txTmDoc : txtmDocList) {
                    if(txTmDoc.getmDocType().getId().equalsIgnoreCase("TTDK")) {
                        docIdTTDKTemp = txTmDoc.getFileNameTemp();
                        txTmDocDate = DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
                    } else if (txTmDoc.getmDocType().getId().equalsIgnoreCase("TTDP")) {
                        docIdTTDPTemp = txTmDoc.getFileNameTemp();
                        txTmDocDate = DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
                    }

                    dataCetakMerek = new CetakMerek(8, "Dokumen Lampiran", "Attachment");
                    dataCetakMerek.setDocName(txTmDoc.getmDocType().getName());
                    dataListCetakMerek.add(dataCetakMerek);
                }
            }
            if(txTmReprsList.size() > 0 && !docIdTTDKTemp.isEmpty()) {
                tandaTanganDigital = docIdTTDKTemp;
                pathFolderTandaTanganDigital = txTmDocDate;
            } else {
                tandaTanganDigital = docIdTTDPTemp;
                pathFolderTandaTanganDigital = txTmDocDate;
            }

            // Data Pemohon Tambahan
            List<TxTmOwnerDetail> txOwnerDetail = null;
            for (TxTmOwner owners : txTmOwnerList) {
                txOwnerDetail = permohonanService.selectTxTmOwnerDetailByTxTmOwner(owners.getId(), true);
                if (txOwnerDetail.size() == 0) {
                    dataCetakMerek = new CetakMerek(9, "Identitas pemohon jika pemohon lebih dari satu pihak", "Additional Applicant");
                    try {
                        dataCetakMerek.setNo(null);
                    } catch (NumberFormatException e) {
                    }
                    dataCetakMerek.setOwnerDetailName("");
                    dataListCetakMerek.add(dataCetakMerek);
                } else {
                    int no = 0;
                    for (TxTmOwnerDetail ownerDetail : txOwnerDetail) {
                        no++;
                        dataCetakMerek = new CetakMerek(9, "Identitas pemohon jika pemohon lebih dari satu pihak", "Additional Applicant");
                        dataCetakMerek.setNo(no);
                        dataCetakMerek.setOwnerDetailName(ownerDetail.getName());
                        dataListCetakMerek.add(dataCetakMerek);
                    }
                }
            }

            // Data Gambar Logo Merek Tambahan
            List<TxTmBrandDetail> txTmBrandDetail = brandService.selectTxTmBrandDetailByTxTmBrand(txTmBrand.getId());
            if (txTmBrandDetail.size() == 0) {
                dataCetakMerek = new CetakMerek(10, "Gambar Merek Tambahan", "Additional Mark");
                try {
                    dataCetakMerek.setNoBrand(null);
                } catch (NumberFormatException e) {
                }
                dataCetakMerek.setBrandLogo("");
                dataListCetakMerek.add(dataCetakMerek);
            } else {
                int no = 0;
                for(TxTmBrandDetail brandDetail : txTmBrandDetail) {
                    no++;
                    String brandLogo = brandDetail.getId() + ".jpg";
                    String pathFolder = DateUtil.formatDate(brandDetail.getUploadDate(), "yyyy/MM/dd/");
                    String pathBrandLogo = brandLogo == null ? "" : uploadFileBrandDetailPath + pathFolder + brandLogo;

                    dataCetakMerek = new CetakMerek(10, "Gambar Merek Tambahan", "Additional Mark");
                    dataCetakMerek.setNoBrand(no);
                    dataCetakMerek.setBrandLogo(brandDetail.getId() == null ? "" : pathBrandLogo);
                    dataListCetakMerek.add(dataCetakMerek);
                }
            }

            String pathFolder = DateUtil.formatDate(txTmGeneral.getTxTmBrand().getCreatedDate(), "yyyy/MM/dd/");

            ClassLoader classLoader = getClass().getClassLoader();
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("pUploadFilePath", uploadFilePathSignature);
            params.put("pUploadFileBrandPath", uploadFileBrandPath + pathFolder);
            params.put("pSignImage", "signimage.jpg");

            params.put("pUploadFileTandaTanganDigital", uploadFileImageTandaTangan + pathFolderTandaTanganDigital);
            params.put("pTtdImage", tandaTanganDigital);

            List<Object> addListOwnerDetail = new ArrayList<>();
            for (CetakMerek datas : dataListCetakMerek) {
                if(datas.getOwnerDetailName() != null) {
                    addListOwnerDetail.add(datas);
                    params.put("daftarPemohonTambahan", new JRBeanCollectionDataSource(addListOwnerDetail));
                }
            }

            List<Object> addListBrandDetail = new ArrayList<>();
            for (CetakMerek datas : dataListCetakMerek) {
                if(datas.getBrandLogo() != null) {
                    addListBrandDetail.add(datas);
                    params.put("daftarMerekTambahan", new JRBeanCollectionDataSource(addListBrandDetail));
                }
            }
            if(txTmReprsList.size() > 0) {
                for(TxTmRepresentative txTmReprs : txTmReprsList) {
                    params.put("pSignFullName", txTmReprs == null ? "" : txTmReprs.getmRepresentative().getName());
                }
            } else {
                for(TxTmOwner txTmOwner : txTmOwnerList) {
                    params.put("pSignFullName", txTmOwner == null ? "" : txTmOwner.getName());
                }
            }

            params.put("pSignPlaceDate", "Jakarta, " + DateUtil.formatDate(txTmGeneral.getTxReception().getApplicationDate(), "dd-MM-yyyy"));

            File file = new File(classLoader.getResource("report/CetakMerek.jasper").getFile());
            InputStream jasperStream1 = new FileInputStream(file);
            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream1);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JRBeanCollectionDataSource(dataListCetakMerek));

            ByteArrayOutputStream output = new ByteArrayOutputStream();

            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(output));
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.exportReport();

            byte[] result;

            try (ByteArrayOutputStream msOut = new ByteArrayOutputStream()) {
                Document document = new Document();
                PdfCopy copy = new PdfCopy(document, msOut);
                document.open();

                ByteArrayInputStream bs = new ByteArrayInputStream(output.toByteArray());
                PdfReader jasperReader = new PdfReader(bs); //Reader for Jasper Report
                for (int page = 0; page < jasperReader.getNumberOfPages(); ) {
                    copy.addPage(copy.getImportedPage(jasperReader, ++page));
                }
                jasperReader.close();

                for (TxTmDoc txTmDoc : txtmDocList) {
                    if (txTmDoc.getFileName() != null) {
                        if(txTmDoc.getFileName().toUpperCase().endsWith(".JPG") || txTmDoc.getFileName().toUpperCase().endsWith(".JPEG")) {
                            continue;
                        }
                        String pathFolderDoc = DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
                        PdfReader nonJasperReader = new PdfReader(uploadFileDocApplicationPath + pathFolderDoc + txTmDoc.getFileNameTemp()); // Reader for the Pdf to be attached (not .jasper file)
                        for (int page = 0; page < nonJasperReader.getNumberOfPages(); ) {
                            copy.addPage(copy.getImportedPage(nonJasperReader, ++page));
                        }
                        nonJasperReader.close();
                    }
                }
                document.close();

                result = msOut.toByteArray();
            }
            InputStream is = new ByteArrayInputStream(result);
            OutputStream outputStreamSuratPernyataan = new FileOutputStream(folder + fileName);

            this.signPdf(is, outputStreamSuratPernyataan);
            outputStreamSuratPernyataan.close();

            response.sendRedirect(PATH_AFTER_LOGIN + PATH_CETAK_MEREK_ONLINE + "?no=" + applicationNo + "&dwnonly=1");
        } catch (Exception ex) {
            logger.error(ex);
        }

        return "";
    }

    /* ---------------------------------------CETAK PERMOHONAN DRAFT --------------------------------------- */
    @RequestMapping(value = REQUEST_MAPPING_CETAK_PERMOHONAN_ONLINE, method = {RequestMethod.GET})
    @ResponseBody
    public String doCetakPermohonanDraft(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

        try {
            List<CetakMerek> dataListCetakMerek = new ArrayList<CetakMerek>();
            CetakMerek dataCetakMerek = null;

            String applicationNo = request.getParameter("no");
            TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(applicationNo);
            TxTmBrand txTmBrand  =  trademarkService.selectOneTxtmBrandBy("txTmGeneral.id",txTmGeneral.getId());

            dataCetakMerek = new CetakMerek(1, "Data Permohonan", "Application");
            dataCetakMerek.setApplicationNo(txTmGeneral.getApplicationNo());
            dataCetakMerek.seteFilingNo(txTmGeneral.getTxReception().geteFilingNo());
            dataCetakMerek.setFileSequence(txTmGeneral.getTxReception().getmFileSequence().getDesc());
            dataCetakMerek.setFileType(txTmGeneral.getTxReception().getmFileType().getDesc());
            dataCetakMerek.setFileTypeDetail(txTmGeneral.getTxReception().getmFileTypeDetail().getDesc());
            dataCetakMerek.setApplicationDate(txTmGeneral.getTxReception().getApplicationDate());
            dataCetakMerek.setFillingDate(txTmGeneral.getFilingDate());
            dataListCetakMerek.add(dataCetakMerek);

            dataCetakMerek = new CetakMerek(2, "Data Merek", "Description of Mark");
            if (txTmGeneral.getTxTmBrand() != null) {
                dataCetakMerek.setBrandName(txTmGeneral.getTxTmBrand().getName());
                dataCetakMerek.setBrandType(txTmGeneral.getTxTmBrand().getmBrandType().getName());
                dataCetakMerek.setBrandColor(txTmGeneral.getTxTmBrand().getColor());
                dataCetakMerek.setBrandDescription(txTmGeneral.getTxTmBrand().getDescription());
                dataCetakMerek.setBrandDescChar(txTmGeneral.getTxTmBrand().getDescChar());
                dataCetakMerek.setBrandTranslation(txTmGeneral.getTxTmBrand().getTranslation());
                dataCetakMerek.setBrandDisclaimer(txTmGeneral.getTxTmBrand().getDisclaimer());
                dataCetakMerek.setBrandFilename(txTmGeneral.getTxTmBrand().getId() + ".jpg");
            }
            dataListCetakMerek.add(dataCetakMerek);

            List<TxTmOwner> txTmOwnerList = permohonanService.selectAllOwnerByIdGenerals(txTmGeneral.getId(), true);
            if (txTmOwnerList.size() == 0) {
                dataCetakMerek = new CetakMerek(3, "Data Pemohon", "Applicant");
                dataListCetakMerek.add(dataCetakMerek);
            } else {
                for (TxTmOwner owners : txTmOwnerList) {
                    dataCetakMerek = new CetakMerek(3, "Data Pemohon", "Applicant");
                    dataCetakMerek.setOwnerName(owners.getName());
                    dataCetakMerek.setOwnerType(owners.getOwnerType());
                    dataCetakMerek.setOwnerNationality(owners.getNationality().getName());
                    dataCetakMerek.setOwnerAddress(owners.getAddress());
                    dataCetakMerek.setOwnerCity(owners.getmCity() == null ? null : owners.getmCity().getName());
                    dataCetakMerek.setOwnerProvince(owners.getmProvince() == null ? null : owners.getmProvince().getName());
                    dataCetakMerek.setOwnerCountry(owners.getmCountry() == null ? null : owners.getmCountry().getName());
                    dataCetakMerek.setOwnerZipCode(owners.getZipCode());
                    dataCetakMerek.setOwnerPhone(owners.getPhone());
                    dataCetakMerek.setOwnerEmail(owners.getEmail());
                    dataListCetakMerek.add(dataCetakMerek);
                }
            }

            if (txTmOwnerList.size() == 0) {
                dataCetakMerek = new CetakMerek(4, "Alamat Surat Menyurat", "Mailing Address");
                dataListCetakMerek.add(dataCetakMerek);
            }
            else {
                for (TxTmOwner owners : txTmOwnerList) {
                    dataCetakMerek = new CetakMerek(4, "Alamat Surat Menyurat", "Mailing Address");
                    dataCetakMerek.setOwnerPostAddress(owners.getPostAddress());
                    dataCetakMerek.setOwnerPostCity(owners.getPostCity() == null ? null : owners.getPostCity().getName());
                    dataCetakMerek.setOwnerPostProvince(owners.getPostProvince() == null ? null : owners.getPostProvince().getName());
                    dataCetakMerek.setOwnerPostCountry(owners.getPostCountry() == null ? null : owners.getPostCountry().getName());
                    dataCetakMerek.setOwnerPostZipCode(owners.getPostZipCode());
                    dataCetakMerek.setOwnerPostPhone(owners.getPostPhone());
                    dataCetakMerek.setOwnerPostEmail(owners.getPostEmail());
                    dataListCetakMerek.add(dataCetakMerek);
                }
            }

            List<TxTmRepresentative> txTmReprsList = permohonanService.selectTxTmReprsByGeneralId(txTmGeneral.getId(), true);
            if (txTmReprsList.size() == 0) {
                dataCetakMerek = new CetakMerek(5, "Data Kuasa", "Representative/IP Consultant");
                dataListCetakMerek.add(dataCetakMerek);
            } else {
                for(TxTmRepresentative txTmReprs : txTmReprsList) {
                    dataCetakMerek = new CetakMerek(5, "Data Kuasa", "Representative/IP Consultant");
                    dataCetakMerek.setReprsNo(txTmReprs.getmRepresentative().getNo());
                    dataCetakMerek.setReprsName(txTmReprs.getmRepresentative().getName());
                    dataCetakMerek.setReprsName(txTmReprs.getmRepresentative().getName());
                    dataCetakMerek.setReprsOffice(txTmReprs.getmRepresentative().getOffice());

//            		dataCetakMerek.setReprsAddress(txTmReprs.getmRepresentative().getAddress());
//            		dataCetakMerek.setReprsPhone(txTmReprs.getmRepresentative().getPhone());
//                    dataCetakMerek.setReprsEmail(txTmReprs.getmRepresentative().getEmail());
                    /**---GET FIELD ADDRESS, PHONE, EMAIL FROM TX_TM_REPRS **/
                    dataCetakMerek.setReprsAddress( txTmReprs.getAddress() );
                    dataCetakMerek.setReprsPhone( txTmReprs.getPhone() );
                    dataCetakMerek.setReprsEmail( txTmReprs.getEmail() );
                    dataListCetakMerek.add(dataCetakMerek);
                }
            }

            List<TxTmPrior> txTmPriorList = permohonanService.selectAllPriorByGeneralId(txTmGeneral.getId());
            if (txTmPriorList.size() == 0) {
                dataCetakMerek = new CetakMerek(6, "Data Prioritas", "Priority Data");
                dataListCetakMerek.add(dataCetakMerek);
            } else {
                int count = 1;
                for (TxTmPrior txTmPrior : txTmPriorList) {
                    dataCetakMerek = new CetakMerek(6, "Data Prioritas", "Priority Data");
                    dataCetakMerek.setPriorOrder("" + count++);
                    dataCetakMerek.setPriorNo(txTmPrior.getNo());
                    dataCetakMerek.setPriorCountry(txTmPrior.getmCountry().getName());
                    dataCetakMerek.setPriorDate(txTmPrior.getPriorDate());
                    dataListCetakMerek.add(dataCetakMerek);
                }
            }

            if (txTmGeneral.getTxTmClassList().size() == 0) {
                dataCetakMerek = new CetakMerek(7, "Data Kelas", "Class");
                dataListCetakMerek.add(dataCetakMerek);
            } else {
                Map<Integer, String[]> mapClass = new HashMap<>();
                List<TxTmClass> txTmClassList = classService.selectAllClassByIdGeneral(txTmGeneral.getId(), ClassStatusEnum.ACCEPT.name());
                for (TxTmClass result : txTmClassList) {
                    int key = result.getmClass().getNo();
                    String desc = result.getmClassDetail().getDesc();
                    String descEn = result.getmClassDetail().getDescEn();

                    if (mapClass.containsKey(key)) {
                        desc = mapClass.get(key)[0] + "; " + desc;
                        descEn = mapClass.get(key)[1] + "; " + descEn;
                    }

                    mapClass.put(key, new String[]{desc, descEn});
                }
                for (Map.Entry<Integer, String[]> entry : mapClass.entrySet()) {
                    String desc = entry.getValue()[0].replaceAll(" ", "").replaceAll(";", "");
                    String descEn = entry.getValue()[1].replaceAll(" ", "").replaceAll(";", "");

                    dataCetakMerek = new CetakMerek(7, "Data Kelas", "Class");
                    dataCetakMerek.setClassNo("" + entry.getKey());
                    dataCetakMerek.setClassDesc(desc.length() == 0 ? " - " : entry.getValue()[0]);
                    dataCetakMerek.setClassDescEn(descEn.length() == 0 ? " - " : entry.getValue()[1]);
                    dataListCetakMerek.add(dataCetakMerek);
                }
            }

            String tandaTanganDigital = "";
            String pathFolderTandaTanganDigital = "";

            String docIdTTDPTemp = "";
            String docIdTTDKTemp = "";
            String txTmDocDateTTDK = "";
            String txTmDocDateTTDP = "";
            List<TxTmDoc> txtmDocList = doclampiranService.getAllDocByApplicationId("txTmGeneral.id", txTmGeneral.getId());
            if (txtmDocList.size() == 0) {
                dataCetakMerek = new CetakMerek(8, "Dokumen Lampiran", "Attachment");
                dataListCetakMerek.add(dataCetakMerek);
            } else {
                Collections.sort(txtmDocList, (o1, o2) -> o1.getmDocType().getName().compareTo(o2.getmDocType().getName()));
                for (TxTmDoc txTmDoc : txtmDocList) {
                    if(txTmDoc.getmDocType().getId().equalsIgnoreCase("TTDK")) {
                        docIdTTDKTemp = txTmDoc.getFileNameTemp();
                        txTmDocDateTTDK = DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
                    } else if (txTmDoc.getmDocType().getId().equalsIgnoreCase("TTDP")) {
                        docIdTTDPTemp = txTmDoc.getFileNameTemp();
                        txTmDocDateTTDP = DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
                    }

                    dataCetakMerek = new CetakMerek(8, "Dokumen Lampiran", "Attachment");
                    dataCetakMerek.setDocName(txTmDoc.getmDocType().getName());
                    dataListCetakMerek.add(dataCetakMerek);
                }
            }
            if(txTmReprsList.size() > 0 && !docIdTTDKTemp.isEmpty()) {
                tandaTanganDigital = docIdTTDKTemp;
                pathFolderTandaTanganDigital = txTmDocDateTTDK;
            } else {
                tandaTanganDigital = docIdTTDPTemp;
                pathFolderTandaTanganDigital = txTmDocDateTTDP;
            }

            // Data Pemohon Tambahan
            List<TxTmOwnerDetail> txOwnerDetail = null;
            for (TxTmOwner owners : txTmOwnerList) {
                txOwnerDetail = permohonanService.selectTxTmOwnerDetailByTxTmOwner(owners.getId(), true);
                if (txOwnerDetail.size() == 0) {
                    dataCetakMerek = new CetakMerek(9, "Identitas pemohon jika pemohon lebih dari satu pihak", "Additional Applicant");
                    try {
                        dataCetakMerek.setNo(null);
                    } catch (NumberFormatException e) {
                    }
                    dataCetakMerek.setOwnerDetailName("");
                    dataListCetakMerek.add(dataCetakMerek);
                } else {
                    int no = 0;
                    for (TxTmOwnerDetail ownerDetail : txOwnerDetail) {
                        no++;
                        dataCetakMerek = new CetakMerek(9, "Identitas pemohon jika pemohon lebih dari satu pihak", "Additional Applicant");
                        dataCetakMerek.setNo(no);
                        dataCetakMerek.setOwnerDetailName(ownerDetail.getName());
                        dataListCetakMerek.add(dataCetakMerek);
                    }
                }
            }

            // Data Gambar Logo Merek Tambahan
            List<TxTmBrandDetail> txTmBrandDetail = brandService.selectTxTmBrandDetailByTxTmBrand(txTmBrand != null ? txTmBrand.getId() : null);
            if (txTmBrandDetail.size() == 0) {
                dataCetakMerek = new CetakMerek(10, "Gambar Merek Tambahan", "Additional Mark");
                try {
                    dataCetakMerek.setNoBrand(null);
                } catch (NumberFormatException e) {
                }
                dataCetakMerek.setBrandLogo("");
                dataListCetakMerek.add(dataCetakMerek);
            } else {
                int no = 0;
                for(TxTmBrandDetail brandDetail : txTmBrandDetail) {
                    no++;
                    String brandLogo = brandDetail.getId() + ".jpg";
                    String pathFolder = DateUtil.formatDate(brandDetail.getUploadDate(), "yyyy/MM/dd/");
                    String pathBrandLogo = brandLogo == null ? "" : uploadFileBrandDetailPath + pathFolder + brandLogo;

                    dataCetakMerek = new CetakMerek(10, "Gambar Merek Tambahan", "Additional Mark");
                    dataCetakMerek.setNoBrand(no);
                    dataCetakMerek.setBrandLogo(brandDetail.getId() == null ? "" : pathBrandLogo);
                    dataListCetakMerek.add(dataCetakMerek);
                }
            }

            String pathFolder = txTmGeneral.getTxTmBrand() != null ? DateUtil.formatDate(txTmGeneral.getTxTmBrand().getCreatedDate(), "yyyy/MM/dd/") : "";

            ClassLoader classLoader = getClass().getClassLoader();
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("pUploadFilePath", uploadFilePathSignature);
            params.put("pUploadFileBrandPath", uploadFileBrandPath + pathFolder);
//            params.put("pSignImage", "signimage.jpg");
            params.put("draftLogo", pathImage + "draft.png");

            params.put("pUploadFileTandaTanganDigital", uploadFileImageTandaTangan + pathFolderTandaTanganDigital);
            params.put("pTtdImage", tandaTanganDigital);

            List<Object> addListOwnerDetail = new ArrayList<>();
            for (CetakMerek datas : dataListCetakMerek) {
                if(datas.getOwnerDetailName() != null) {
                    addListOwnerDetail.add(datas);
                    params.put("daftarPemohonTambahan", new JRBeanCollectionDataSource(addListOwnerDetail));
                }
            }

            List<Object> addListBrandDetail = new ArrayList<>();
            for (CetakMerek datas : dataListCetakMerek) {
                if(datas.getBrandLogo() != null) {
                    addListBrandDetail.add(datas);
                    params.put("daftarMerekTambahan", new JRBeanCollectionDataSource(addListBrandDetail));
                }
            }
            if(txTmReprsList.size() > 0) {
                for(TxTmRepresentative txTmReprs : txTmReprsList) {
                    params.put("pSignFullName", txTmReprs == null ? "" : txTmReprs.getmRepresentative().getName());
                }
            } else {
                for(TxTmOwner txTmOwner : txTmOwnerList) {
                    params.put("pSignFullName", txTmOwner == null ? "" : txTmOwner.getName());
                }
            }

            params.put("pSignPlaceDate", "Jakarta, " + DateUtil.formatDate(txTmGeneral.getTxReception().getApplicationDate(), "dd-MM-yyyy"));

            /*File file = new File(classLoader.getResource("report/CetakMerek4.jasper").getFile());
            InputStream jasperStream1 = new FileInputStream(file);
            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream1);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JRBeanCollectionDataSource(dataListCetakMerek));
*/
            final InputStream reportInputStream = getClass().getResourceAsStream("/report/CetakMerek4.jrxml");
            final JasperDesign jasperDesign = JRXmlLoader.load(reportInputStream);
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JRBeanCollectionDataSource(dataListCetakMerek));

            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(output));
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.exportReport();

            byte[] result;

            try (ByteArrayOutputStream msOut = new ByteArrayOutputStream()) {
                Document document = new Document();
                PdfCopy copy = new PdfCopy(document, msOut);
                document.open();

                ByteArrayInputStream bs = new ByteArrayInputStream(output.toByteArray());
                PdfReader jasperReader = new PdfReader(bs); //Reader for Jasper Report
                for (int page = 0; page < jasperReader.getNumberOfPages(); ) {
                    copy.addPage(copy.getImportedPage(jasperReader, ++page));
                }
                jasperReader.close();

                for (TxTmDoc txTmDoc : txtmDocList) {
                    if (txTmDoc.getFileName() != null) {
                        if(txTmDoc.getFileName().toUpperCase().endsWith(".JPG") || txTmDoc.getFileName().toUpperCase().endsWith(".JPEG")) {
                            continue;
                        }
                        String pathFolderDoc = DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
                        PdfReader nonJasperReader = new PdfReader(uploadFileDocApplicationPath + pathFolderDoc + txTmDoc.getFileNameTemp()); // Reader for the Pdf to be attached (not .jasper file)
                        for (int page = 0; page < nonJasperReader.getNumberOfPages(); ) {
                            copy.addPage(copy.getImportedPage(nonJasperReader, ++page));
                        }
                        nonJasperReader.close();
                    }
                }
                document.close();

                result = msOut.toByteArray();
            }

            String folderDoc = downloadFileDocPermohonanCetakMerekPath + DateUtil.formatDate(txTmGeneral.getCreatedDate(), "yyyy/MM/dd/");
            String filenameDoc = "CetakDraftPermohonan-" + txTmGeneral.getApplicationNo() + ".pdf";

            Path pathDir = Paths.get(folderDoc);
            Path pathFile = Paths.get(folderDoc + filenameDoc);
            if (!Files.exists(pathDir)) {
                Files.createDirectories(pathDir);
            }
            if (Files.exists(pathFile)) {
                Files.delete(pathFile);
            }
            if (!Files.exists(pathFile)) {
                Files.createFile(pathFile);
            }
            OutputStream outputStream = new FileOutputStream(pathFile.toFile());

            response.setContentType("application/pdf");
            response.setHeader("Content-disposition", "attachment; filename=" + "CetakDraftPermohonan-" + applicationNo + ".pdf");

            InputStream is = new ByteArrayInputStream(result);
            this.signPdf(is, outputStream);
            outputStream.close();

            try (OutputStream outputs = response.getOutputStream()) {
                Path path = Paths.get(folderDoc + filenameDoc);
                Files.copy(path, outputs);
                outputs.flush();
            } catch(IOException e) {
                logger.error(e);
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
        return "";
    }
    
    @RequestMapping(value = REQUEST_MAPPING_CETAK_SURAT_PERNYATAAN_ONLINE, method = {RequestMethod.GET})
    @ResponseBody
    public String doCetakSuratPernyataanOnline(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
    	
    	String applicationNo = request.getParameter("no");
    	TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(applicationNo);

        MUser userLogin = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (userLogin.getUsername() != "super") {
            List<KeyValue> criteriaList = new ArrayList<>();
            criteriaList.add(new KeyValue("applicationNo", applicationNo, true));
            criteriaList.add(new KeyValue("createdBy", userLogin, true));
            txTmGeneral = trademarkService.selectOneGeneralByeFilingNoByUser(criteriaList);

            if (txTmGeneral == null) {
                response.sendRedirect( PATH_AFTER_LOGIN + PATH_PERMOHONAN_ONLINE + "?error=" +"Halaman tidak ditemukan" );
                return "";
            }
        }
    	
    	response.setContentType("application/pdf");
        response.setHeader("Content-disposition", "attachment; filename=CetakSuratPernyataan-" + applicationNo + ".pdf");
        
        String folder =  downloadFileDocPermohonanCetakMerekPath + DateUtil.formatDate(txTmGeneral.getCreatedDate(), "yyyy/MM/dd/");
        String fileName = "CetakSuratPernyataan-" + txTmGeneral.getApplicationNo() + ".pdf";
        
        try (OutputStream output = response.getOutputStream()) {
        	Path path = Paths.get(folder + fileName);
        	Files.copy(path, output);
        	output.flush();
        } catch(IOException e) {
        }
        
        return "";
    }
    
    @RequestMapping(value = REQUEST_MAPPING_DOWNLOAD_DOC_ONLINE, method = {RequestMethod.GET})
    @ResponseBody
    public String doDownloadDocOnline(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
    	
    	String docId = request.getParameter("id");
    	TxTmDoc txTmDoc = doclampiranService.findOne(docId);

    	String folder = "";
    	String fileName = "";
        
        if(txTmDoc.getFileName().toUpperCase().endsWith(".JPG") || txTmDoc.getFileName().toUpperCase().endsWith(".JPEG")) {
        	response.setContentType("image/jpeg");
            response.setHeader("Content-disposition", "inline; filename=DokumenLampiran-" + txTmDoc.getFileNameTemp());
        	folder =  uploadFileImageTandaTangan + DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
            fileName = txTmDoc.getFileNameTemp();
        } else {
        	response.setContentType("application/pdf");
            response.setHeader("Content-disposition", "inline; filename=DokumenLampiran-" + txTmDoc.getFileNameTemp());
        	folder =  uploadFileDocApplicationPath + DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
            fileName = txTmDoc.getFileNameTemp();
        }
        
        try (OutputStream output = response.getOutputStream()) {
        	Path path = Paths.get(folder + fileName);
        	Files.copy(path, output);
        	output.flush();
        } catch(IOException e) {
        }
        
        return "";
    }

    @RequestMapping(value = REQUEST_CALCULATE_CLASS, method = {RequestMethod.GET})
    public String doCalculateClassOnline(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        
        return PAGE_CALCULATE_CLASS;
    }

    private void signPdf(InputStream input, OutputStream output) {
        String key = CERTIFICATE_FILE + "eFiling.p12";
        //System.out.println("PATH : " + key);
        try {
            PDFSignatureFacade facade = new PDFSignatureFacade();
            facade.sign(key, "JakartaPP123!@#", input, output, true, new java.awt.Rectangle(250, 0, 400, 50));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean isRepresentative() {
        MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MRepresentative mRepresentative = representativeService.selectOne("userId.id", mUser.getId());

        return mRepresentative != null;
    }
    
    //  save-online-form-6-new-barangjasa
    @RequestMapping(value = REQUEST_MAPPING_SAVE_FORM_KELAS_ONLINE_NEW_BARANGJASA, method = {RequestMethod.POST})
    public void doSaveFormKelasNewBarangJasa(Model model, final HttpServletRequest request, final HttpServletResponse response,
                                             @RequestParam("appNo") String appNo,
                                             @RequestParam("listClassChildIdNewRequest") String[] listClassChildIdNewRequest) throws IOException {
    	
    	System.out.println("appNo save kelas baru : "+appNo);
    	TxTmGeneral txTmGeneral = trademarkService.selectOne("applicationNo", appNo);
        List<String> oldTxTmClassID = new ArrayList<>();
        List<String> listId = new ArrayList<String>();
        List<String> listUraian = new ArrayList<String>();
        List<String> listUraianEn = new ArrayList<String>();
        List<String> listCatatan = new ArrayList<String>();
        List<String> UniqueClassIDs = new ArrayList<>();
        List<String> listClassId = new ArrayList<>();
        if (listClassChildIdNewRequest != null) {
            for (String classChildId : listClassChildIdNewRequest) {
                String[] classChildTemp = classChildId.split(";");
                if(!UniqueClassIDs.contains(classChildTemp[1])){
                    UniqueClassIDs.add(classChildTemp[1]);
                }
                //keep txtmclass away
                if(!classChildTemp[0].equalsIgnoreCase("")) {
                    System.out.println("old one:"+classChildId);
                    oldTxTmClassID.add(classChildTemp[0]);
                    continue;
                }
                System.out.println("new:"+classChildId);
                listId.add(classChildTemp[1]);
                listClassId.add(classChildTemp[0]);
                if(classChildTemp.length<5){
                    listUraian.add("-");
                } else {
                    listUraian.add(classChildTemp[4]);
                }
                if(classChildTemp.length<6){
                    listUraianEn.add("-");
                } else {
                    listUraianEn.add(classChildTemp[5]);
                }
                listCatatan.add(classChildTemp[6]);
            }
        }

        //Do check TotalClass first
        TxReception trec = txReceptionCustomRepository.selectOne("applicationNo", txTmGeneral.getApplicationNo());
        if(UniqueClassIDs.size() > trec.getTotalClass()){
            response.sendError(HttpServletResponse.SC_CONFLICT,"Total Class melebihi yang diBayar" );
            return;
        }
        
        if (oldTxTmClassID.size() > 0) {
        	classService.deleteMClassDetailANDTxTMClassMultipleNotIn2(appNo, oldTxTmClassID.toArray(new String[oldTxTmClassID.size()]));
        } else if (listClassChildIdNewRequest.length == 0) {
        	classService.deleteBarangJasa(appNo);
        }
//          GenericSearchWrapper<TxTmClass> searchResult = classService.searchGeneralTxClass(searchCriteria, "id", "ASC", 0, null);

//          if (searchResult.getCount() > 0) {
//              for (TxTmClass result : searchResult.getList()) {
//                  classService.deleteTxTMClass(result);
//              }
//          }

        Timestamp tmstm = new Timestamp(System.currentTimeMillis());
        if (listClassChildIdNewRequest != null) {
            for (int i = 0; i<listId.size();i++) {
            	String classChildId = listId.get(i);
                String uraian = listUraian.get(i);
                String uraianEn = listUraianEn.get(i);
                String catatan = listCatatan.get(i);
                String classId = listClassId.get(i);
                System.err.println(classChildId+"  "+uraian+"  "+uraianEn+"  "+catatan);
            	if (classId.equals("")) {            		
                    if(classChildId.equalsIgnoreCase("")){
                        continue;
                    }
                    if (uraian.contains("|")) {
                    	uraian = uraian.replace("|", ";");
                    }
                    if (uraianEn.contains("|")) {
                    	uraianEn = uraianEn.replace("|", ";");
                    }
                    MClass mClass = mClassHeaderRepository.findFirstByNo(Integer.valueOf(classChildId));
                    MClassDetail mClassDetail = new MClassDetail();
                    mClassDetail.setClassBaseNo(txTmGeneral.getApplicationNo());
                    mClassDetail.setParentClass(mClass);
                    mClassDetail.setDesc(uraian);
                    mClassDetail.setDescEn(uraianEn);
                    mClassDetail.setStatusFlag(false);
//                    mClassDetail.setNotes("kelas detail khusus");
                    mClassDetail.setSerial1(catatan);
                    mClassDetail.setSerial2("Dari permohonan online petugas");
                    //mClassDetail.setClassBaseNo(classChildId);
                    mClassDetail.setCreatedDate(tmstm);
                    mClassDetail.setCreatedBy(txTmGeneral.getCreatedBy());
                    //mClassDetail.setUpdatedDate(tmstm);
                    //mClassDetail.setUpdatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                    mClassDetailCustomRepository.saveOrUpdate(mClassDetail);

                    TxTmClass txTmClass = new TxTmClass();
                    txTmClass.setTxTmGeneral(txTmGeneral);
                    txTmClass.setmClass(mClass);
                    txTmClass.setmClassDetail(mClassDetail);
                    //txTmClass.setmClassDetail(mClassDetail);
                    txTmClass.setCreatedBy(txTmGeneral.getCreatedBy());
                    txTmClass.setCreatedDate(tmstm);
                    txTmClass.setTransactionStatus("PENDING");
                    txTmClass.setCorrectionFlag(false);
                    txTmClassCustomRepository.saveOrUpdate(txTmClass);
                }
            }

            // MUser user = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            // classService.deleteUnusedMClassDetail(user.getId());
        }
    }



    @RequestMapping(value = REQUEST_MAPPING_LIST_TABEL_PENOLAKAN_PERMINTAAN, method = {RequestMethod.GET})
    public void doSearchDataTablesListPenolakanPermintaan(final HttpServletRequest request,
                                                          final HttpServletResponse response) throws IOException {

        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            DataTablesSearchResult dataTablesSearchResult = new DataTablesSearchResult();
            try {
                dataTablesSearchResult.setDraw(Integer.parseInt(request.getParameter("draw")));
            } catch (NumberFormatException e) {
                dataTablesSearchResult.setDraw(0);
            }

            MUser user = new MUser();
            user = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            
            List<String[]> data = new ArrayList<>();
            
            String appNo = request.getParameter("appNo");
            System.out.println("appNo kelas di tolak : "+appNo);
            int countClassDetail = mClassDetailRepository.countPenolakan(appNo);
                        
            if (countClassDetail > 0) {
            	
            	List<MClassDetail> listClassDetail = mClassDetailRepository.selectPenolakan(appNo);
            	            	
                dataTablesSearchResult.setRecordsFiltered(countClassDetail);
                dataTablesSearchResult.setRecordsTotal(countClassDetail);

                int no = 0;
                for (MClassDetail result : listClassDetail) {
                    no++;
                    data.add(new String[]{
                            result.getId(),
                            "" + no,
                            "" + result.getParentClass().getNo(),
                            result.getId(),
                            result.getDesc(),
                            result.getDescEn(),
                            result.getParentClass().getEdition(),
                            result.getNotes(),
                            result.getUpdatedBy().getUsername(),
                            //ClassStatusEnum.valueOf(result.getTransactionStatus()).getLabel(),
//                            result.isCorrectionFlag() ? "Ya" : "Tidak",
//                            "<button>Hapus</button>",
//                            "<button>Update</button>"
                    });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }

    }
}