package com.docotel.ki.controller.online;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.enumeration.ClassStatusEnum;
import com.docotel.ki.enumeration.PriorStatusEnum;
import com.docotel.ki.enumeration.StatusEnum;
import com.docotel.ki.model.master.*;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.pojo.*;
import com.docotel.ki.repository.NativeQueryRepository;
import com.docotel.ki.repository.custom.master.MCityCustomRepository;
import com.docotel.ki.repository.custom.master.MCountryCostumRepository;
import com.docotel.ki.repository.custom.master.MProvinceCustomRepository;
import com.docotel.ki.repository.custom.transaction.*;
import com.docotel.ki.repository.transaction.*;
import com.docotel.ki.service.SimpakiService;
import com.docotel.ki.service.master.*;
import com.docotel.ki.service.transaction.*;
import com.docotel.ki.signature.PDFSignatureFacade;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.util.FieldValidationUtil;
import com.docotel.ki.util.NumberUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BadPdfFormatException;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfReader;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class MadridOOOnlineController extends BaseController {

    private static final String DIRECTORY_PAGE_ONLINE = "madrid-oo-online/";
    private static final String PAGE_PERMOHONAN_MADRID_OO = DIRECTORY_PAGE_ONLINE + "permohonan-madrid-oo";
    private static final String PAGE_TAMBAH_PERMOHONAN_MADRID_OO = DIRECTORY_PAGE_ONLINE + "tambah-permohonan-madrid-oo";
    private static final String PAGE_EDIT_PERMOHONAN_MADRID_OO = DIRECTORY_PAGE_ONLINE + "edit-permohonan-madrid-oo";
    private static final String PAGE_PRATINJAU_PERMOHONAN_MADRID_OO = DIRECTORY_PAGE_ONLINE + "pratinjau-permohonan-madrid-oo";
    private static final String PAGE_EDIT_PRATINJAU_PERMOHONAN_ONLINE = DIRECTORY_PAGE_ONLINE + "edit-pratinjau-permohonan-madrid-oo";
    private static final String PAGE_CALCULATE_CLASS = DIRECTORY_PAGE_ONLINE + "calculate-class";
    private static final String PATH_AJAX_SEARCH_LIST_MADRID_OO = "/cari-permohonan-madrid-oo";
    private static final String PATH_AJAX_SEARCH_LIST_REGISTRATION = "/cari-registration-madrid-oo";
    public static final String PATH_AJAX_LIST_DELETE_DOKUMEN = "/delete-dokumen-madrid-oo";
    private static final String PATH_PERMOHONAN_MADRID_OO = "/list-permohonan-madrid-oo";
    private static final String PATH_DOKUMEN_MADRID_OO = "/list-dokumen-madrid-oo";
    private static final String PATH_PRATINJAU_PERMOHONAN_MADRID_OO = "/pratinjau-permohonan-madrid-oo";
    private static final String PATH_EDIT_PRATINJAU_MADRID_OO = "/edit-pratinjau-madrid-oo";
    private static final String PATH_EDIT_PRATINJAU_MADRID_OO_PEMOHON = PATH_EDIT_PRATINJAU_MADRID_OO + "-pemohon";
    private static final String PATH_EDIT_PRATINJAU_MADRID_OO_KUASA = PATH_EDIT_PRATINJAU_MADRID_OO + "-kuasa";
    private static final String PATH_EDIT_PRATINJAU_MADRID_OO_PRIOR = PATH_EDIT_PRATINJAU_MADRID_OO + "-prior";
    private static final String PATH_EDIT_PRATINJAU_MADRID_OO_MEREK = PATH_EDIT_PRATINJAU_MADRID_OO + "-merek";
    private static final String PATH_EDIT_PRATINJAU_MADRID_OO_KELAS = PATH_EDIT_PRATINJAU_MADRID_OO + "-kelas";
    private static final String PATH_EDIT_PRATINJAU_MADRID_OO_DOKUMEN = "/edit-pratinjau-madrid-oo-dokumen";
    private static final String PATH_MADRID_OO_CHOOSE_REFERENSI = "/choose-referensi-madrid-oo";
    private static final String PATH_CHECK_CODE_BILLING = "/check-code-billing/madrid-oo";
    private static final String PATH_CETAK_SURAT_PERNYATAAN_MADRID_OO = "/cetak-surat-pernyataan-online";
    private static final String PATH_CETAK_MEREK_MADRID_OO_DRAFT = "/cetak-madrid-oo-draft";
    private static final String PATH_CETAK_MEREK_MADRID_OO = "/cetak-madrid-oo";
    private static final String PATH_SAVE_FORM_GENERAL_MADRID_OO = "/save-madrid-oo-form-general";
    private static final String PATH_DELETE_FORM_GENERAL_MADRID_OO = "/delete-madrid-oo-form-general";
    private static final String PATH_SAVE_FORM_PEMOHON_MADRID_OO = "/save-madrid-oo-form-pemohon";
    private static final String PATH_SAVE_FORM_KUASA_MADRID_OO = "/save-madrid-oo-form-kuasa";
    private static final String PATH_SAVE_FORM_PRIOR_MADRID_OO = "/save-madrid-oo-form-prior";
    private static final String PATH_SAVE_FORM_MEREK_MADRID_OO = "/save-madrid-oo-form-merek";
    private static final String PATH_SAVE_FORM_KELAS_MADRID_OO = "/save-madrid-oo-form-kelas";
    private static final String PATH_SAVE_FORM_NEGARA_MADRID_OO = "/save-madrid-oo-form-negara";
    private static final String PATH_SAVE_FORM_BIAYA_MADRID_OO = "/save-madrid-oo-form-biaya";
    private static final String PATH_SAVE_FORM_DOC_MADRID_OO = "/save-madrid-oo-form-dokumen";

    private static final String PATH_TAMBAH_PERMOHONAN_MADRID_OO = "/tambah-permohonan-madrid-oo";
    private static final String PATH_EDIT_PERMOHONAN_MADRID_OO = "/edit-permohonan-madrid-oo";
    private static final String PATH_SELESAI_PERMOHONAN_MADRID_OO = "/selesai-permohonan-madrid-oo";
    private static final String PATH_GET_JUMLAH_KELAS = "/get-jumlah-kelas";

    private static final String REQUEST_MAPPING_PERMOHONAN_MADRID_OO = PATH_PERMOHONAN_MADRID_OO + "*";
    private static final String REQUEST_MAPPING_TAMBAH_PERMOHONAN_MADRID_OO = PATH_TAMBAH_PERMOHONAN_MADRID_OO + "*";
    private static final String REQUEST_MAPPING_MADRID_OO_CHOOSE_REFERENSI = PATH_MADRID_OO_CHOOSE_REFERENSI + "*";;
    private static final String REQUEST_MAPPING_CHECK_CODE_BILLING = PATH_CHECK_CODE_BILLING + "*";
    private static final String REQUEST_MAPPING_EDIT_PERMOHONAN_MADRID_OO = PATH_EDIT_PERMOHONAN_MADRID_OO + "*";
    private static final String REQUEST_MAPPING_AJAX_SEARCH_LIST_MADRID_OO = PATH_AJAX_SEARCH_LIST_MADRID_OO + "*";
    private static final String REQUEST_MAPPING_SAVE_FORM_GENERAL_MADRID_OO = PATH_SAVE_FORM_GENERAL_MADRID_OO + "*";
    private static final String REQUEST_MAPPING_DELETE_FORM_GENERAL_MADRID_OO = PATH_DELETE_FORM_GENERAL_MADRID_OO + "*";
    private static final String REQUEST_MAPPING_SAVE_FORM_PEMOHON_MADRID_OO = PATH_SAVE_FORM_PEMOHON_MADRID_OO + "*";
    private static final String REQUEST_MAPPING_SAVE_FORM_KUASA_MADRID_OO = PATH_SAVE_FORM_KUASA_MADRID_OO + "*";
    private static final String REQUEST_MAPPING_SAVE_FORM_PRIOR_MADRID_OO = PATH_SAVE_FORM_PRIOR_MADRID_OO + "*";
    private static final String REQUEST_MAPPING_SAVE_FORM_MEREK_MADRID_OO = PATH_SAVE_FORM_MEREK_MADRID_OO + "*";
    private static final String REQUEST_MAPPING_SAVE_FORM_KELAS_MADRID_OO = PATH_SAVE_FORM_KELAS_MADRID_OO + "*";
    private static final String REQUEST_MAPPING_SAVE_FORM_BIAYA_MADRID_OO = PATH_SAVE_FORM_BIAYA_MADRID_OO + "*";
    private static final String REQUEST_MAPPING_SAVE_FORM_DOC_MADRID_OO = PATH_SAVE_FORM_DOC_MADRID_OO + "*";
    private static final String REQUEST_MAPPING_SELESAI_PERMOHONAN_MADRID_OO = PATH_SELESAI_PERMOHONAN_MADRID_OO + "*";
    private static final String REQUEST_MAPPING_PRATINJAU_PERMOHONAN_MADRID_OO = PATH_PRATINJAU_PERMOHONAN_MADRID_OO + "*";
    private static final String REQUEST_MAPPING_EDIT_PRATINJAU_MADRID_OO_PEMOHON = PATH_EDIT_PRATINJAU_MADRID_OO_PEMOHON + "*";
    private static final String REQUEST_MAPPING_EDIT_PRATINJAU_MADRID_OO_KUASA = PATH_EDIT_PRATINJAU_MADRID_OO_KUASA + "*";
    private static final String REQUEST_MAPPING_EDIT_PRATINJAU_MADRID_OO_PRIOR = PATH_EDIT_PRATINJAU_MADRID_OO_PRIOR + "*";
    private static final String REQUEST_MAPPING_EDIT_PRATINJAU_MADRID_OO_MEREK = PATH_EDIT_PRATINJAU_MADRID_OO_MEREK + "*";
    private static final String REQUEST_MAPPING_EDIT_PRATINJAU_MADRID_OO_KELAS = PATH_EDIT_PRATINJAU_MADRID_OO_KELAS + "*";
    private static final String REQUEST_MAPPING_EDIT_PRATINJAU_MADRID_OO_DOKUMEN = PATH_EDIT_PRATINJAU_MADRID_OO_DOKUMEN + "*";
    private static final String REQUEST_MAPPING_MADRID_OO_PERMOHONAN_CETAK = PATH_CETAK_MEREK_MADRID_OO + "*";
    private static final String REQUEST_MAPPING_MADRID_OO_PERMOHONAN_CETAK_DRAFT = PATH_CETAK_MEREK_MADRID_OO_DRAFT + "*";
    private static final String REQUEST_MAPPING_GET_JUMLAH_KELAS = PATH_GET_JUMLAH_KELAS + "*";

    @Autowired
    private TrademarkService trademarkService;
    @Autowired
    private LookupService lookupService;
    @Autowired
    private ClassService classService;
    @Autowired
    private RepresentativeService representativeService;
    @Autowired
    private ReprsService reprsService;
    @Autowired
    private DocTypeService docTypeService;
    @Autowired
    private LawService lawService;
    @Autowired
    private FileService fileService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private PermohonanService permohonanService;
    @Autowired
    private RegistrationService registrationService;
    @Autowired
    private CountryService countryService;
    @Autowired
    private ProvinceService provinceService;
    @Autowired
    private CityService cityService;
    @Autowired
    private MadridOoService madridOoService;
    @Autowired
    private SimpakiService simpakiService;
    @Autowired
    private PermohonanOnlineService permohonanOnlineService;
    @Autowired
    private PriorService priorService;
    @Autowired
    private DokumenLampiranService doclampiranService;
    @Autowired
    private TxTmReferenceCustomRepository txTmReferenceCustomRepository;
    @Autowired
    private TxTmGeneralRepository txTmGeneralRepository;
    @Autowired
    private TxTmClassLimitationRepository txTmClassLimitationRepository;
    @Autowired
    private TxTmClassLimitationCustomRepository txTmClassLimitationCustomRepository;
    @Autowired
    private TxTmClassRepository txTmClassRepository;
    @Autowired
    private TxTmBrandCustomRepository txTmBrandCustomRepository ;
    @Autowired
    private TxTmReferenceRepository txTmReferenceRepository;
    @Autowired
    private NativeQueryRepository nativeQueryRepository ;

    @Autowired
    private TxTmMadridFeeDetailRepository txTmMadridFeeDetailRepository ;

    @Autowired
    private TxTmMadridFeeRepository txTmMadridFeeRepository ;

    @Autowired
    private TxTmReprsCustomRepository txTmReprsCustomRepository;

    @Autowired
    private TxTmReprsRepository txTmReprsRepository;

    @Autowired
    private MrepresentativeCustomRepository mrepresentativeCustomRepository;

    @Autowired
    private TxTmOwnerRepository txTmOwnerRepository;
    @Autowired
    private TxTmOwnerCustomRepository txTmOwnerCustomRepository;
    @Autowired
    private MCountryCostumRepository mCountryCostumRepository;
    @Autowired
    private MProvinceCustomRepository mProvinceCustomRepository;
    @Autowired
    private MCityCustomRepository mCityCustomRepository;
    @Autowired
    private TxReceptionRepository txReceptionRepository;

    @Value("${simpaki.api.create.billing}")
    private String urlCreateBilling;
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
    @Value("${upload.file.image.tandatangan.path}")
    private String uploadFileImageTandaTangan;
    @Value("${download.output.madridOO.cetakmerek.file.path}")
    private String downloadFileDocMadridOOCetakPath;


    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "permohonanProtokolMadrid");
        model.addAttribute("subMenu", "madridoo");
    }

    private void doInitiatePermohonan(final Model model, final HttpServletRequest request) {
        if (request.getRequestURI().contains(PATH_PERMOHONAN_MADRID_OO)) {
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
        if (request.getRequestURI().contains(PATH_TAMBAH_PERMOHONAN_MADRID_OO)
                || request.getRequestURI().contains(PATH_EDIT_PERMOHONAN_MADRID_OO)
                || request.getRequestURI().contains(PATH_PRATINJAU_PERMOHONAN_MADRID_OO)
                || request.getRequestURI().contains(PATH_EDIT_PRATINJAU_MADRID_OO)) {
            String form = "";
            if (request.getRequestURI().contains(PATH_EDIT_PRATINJAU_MADRID_OO_PEMOHON)) {
                form = "pemohon";
            } else if (request.getRequestURI().contains(PATH_EDIT_PRATINJAU_MADRID_OO_KUASA)) {
                form = "kuasa";
            } else if (request.getRequestURI().contains(PATH_EDIT_PRATINJAU_MADRID_OO_PRIOR)) {
                form = "prioritas";
            } else if (request.getRequestURI().contains(PATH_EDIT_PRATINJAU_MADRID_OO_MEREK)) {
                form = "merek";
            } else if (request.getRequestURI().contains(PATH_EDIT_PRATINJAU_MADRID_OO_KELAS)) {
                form = "kelas";
            } else if (request.getRequestURI().contains(PATH_EDIT_PRATINJAU_MADRID_OO_DOKUMEN)) {
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
            model.addAttribute("isNew", request.getRequestURI().contains(PATH_TAMBAH_PERMOHONAN_MADRID_OO));
            model.addAttribute("isEditPermohonan", request.getRequestURI().contains(PATH_EDIT_PERMOHONAN_MADRID_OO));
            model.addAttribute("isNewOrEdit", request.getRequestURI().contains(PATH_TAMBAH_PERMOHONAN_MADRID_OO) ||request.getRequestURI().contains(PATH_EDIT_PERMOHONAN_MADRID_OO));
            model.addAttribute("isEdit", request.getRequestURI().contains(PATH_EDIT_PRATINJAU_MADRID_OO));
            model.addAttribute("isReprs", isRepresentative());
            model.addAttribute("applicationDate", new Timestamp(System.currentTimeMillis()));

            List<MLookup> disclaimerMerekList = lookupService.selectAllbyGroup("DisclaimerMerek");
            model.addAttribute("disclaimerMerek", disclaimerMerekList);

            if (form.equals("pemohon") || form.equals("")) {

                if (freebill.equalsIgnoreCase("YES")){
                    model.addAttribute("freebill", 1);
                }
                else
                    model.addAttribute("freebill", 0);

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

                if(request.getRequestURI().contains(PATH_EDIT_PERMOHONAN_MADRID_OO)) {
                    List<MProvince> mProvinceList = provinceService.findByStatusFlagTrue();
                    model.addAttribute("listProvince", mProvinceList);

                    List<MCity> mCityList = cityService.findByStatusFlagTrue();
                    model.addAttribute("listCity", mCityList);
                }
                else if (request.getRequestURI().contains(PATH_TAMBAH_PERMOHONAN_MADRID_OO)) {
                    List<MProvince> mProvinceList = provinceService.findByStatusFlagTrue();
                    model.addAttribute("listProvince", mProvinceList);

                    List<MCity> mCityList = cityService.findByStatusFlagTrue();
                    model.addAttribute("listCity", mCityList);
                }

                List<MLookup> mRightList = lookupService.selectAllbyGroup("HakPengajuan");
                model.addAttribute("listRight", mRightList);
                if(txTmGeneral.getId() != null){

                    String app_id = txTmGeneral.getId();

//                    Could Not Deserialized
                    List <TxTmReference> referenceList = new ArrayList<TxTmReference>();
//                    referenceList = txTmReferenceRepository.getByTxTmGeneralId(app_id);

                    GenericSearchWrapper <Object[]> referenceList2  = nativeQueryRepository.selectReferenceOOMadrid(app_id);

                    List<TxTmReference> referenceListUI = new ArrayList<TxTmReference>();
//                    for(TxTmReference reference : referenceList){


                    for(Object[] referencex : referenceList2.getList()){
                        String ref_id = referencex[0].toString();
                          TxTmReference reference = txTmReferenceCustomRepository.selectOne("id",ref_id,true);

 
                        String ref_app_id = reference.getRefApplicationId().getId() ;
                        TxReception txReception = trademarkService.selectOneReceptionBy("eFilingNo",ref_app_id);
                        
                        if(txReception==null) {
                        	//ambil data reception by ID 
                        	txReception = trademarkService.selectOneReceptionBy("id",ref_app_id);
                        }
                        
                        reference.setAppNo(txReception !=null ? txReception.getApplicationNo():"");
                        TxRegistration txRegistration = trademarkService.selectOnePenerimaanDokumen("id",ref_app_id);
                        // cari nama Brand berdasarkan TxTmGeneral nyambung ke TxTmBrand
//                        TxTmBrand txTmBrand = txTmBrandCustomRepository.selectOne("txTmGeneral.id",ref_app_id);


//                        txReception.setApplicationDate(DateUtil.toDate("dd/MM/yyyy HH:mm:ss", data.getApplicationDate()));

                        try {
                            reference.getTxTmGeneral().setFilingDate(DateUtil.toDate("nullaa"));
                        } catch (ParseException e) {
//                            e.printStackTrace();

                        }
                        reference.setRegNo(txRegistration !=null ? txRegistration.getNo():"(Belum terdaftar)");
                        reference.setRegDate(txRegistration !=null ? txRegistration.getStartDateTemp():"(Belum terdaftar)");

//                        String dateGeneral = reference.getTxTmGeneral().getFilingDate().toString().substring(0,5).replace(":","/") ;
                        referenceList.add(reference);
                        referenceListUI.add(reference);
                    }
                    model.addAttribute("referenceList", referenceListUI);
                }else{
                    model.addAttribute("referenceList", new ArrayList<TxTmReference>());
                }



            }

            if(request.getRequestURI().contains(PATH_TAMBAH_PERMOHONAN_MADRID_OO)
                    || request.getRequestURI().contains(PATH_EDIT_PERMOHONAN_MADRID_OO)
                    || request.getRequestURI().contains(PATH_EDIT_PRATINJAU_MADRID_OO)) {
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
                        txTmRepresentativeList = new ArrayList<TxTmRepresentative>();
                    }

                    model.addAttribute("txTmReprs", txTmRepresentativeList==null ? new TxTmRepresentative() : txTmRepresentativeList);
                }
            }

            if (form.equals("pemohon") || form.equals("prioritas") || form.equals("") && request.getRequestURI().contains(PATH_EDIT_PERMOHONAN_MADRID_OO)) {
//                List<MCountry> mCountryList = countryService.findByStatusFlagTrue();
                // diubah hanya boleh yg is_Madrid
                Boolean ismadrid = true ;
                List<MCountry> MadridCountry = countryService.findByMadrid(ismadrid);
                model.addAttribute("listCountry", MadridCountry);
            }
            else if (form.equals("pemohon") || form.equals("prioritas") || form.equals("") && request.getRequestURI().contains(PATH_TAMBAH_PERMOHONAN_MADRID_OO)) {
//                List<MCountry> mCountryList = countryService.findByStatusFlagTrue();
                Boolean ismadrid = true ;
                List<MCountry> MadridCountry = countryService.findByMadrid(ismadrid);
                model.addAttribute("listCountry", MadridCountry);
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

                List<MLookup> brandTypeList = lookupService.selectAllbyGroup("TipeMerekTambahan");
                model.addAttribute("brandTypeList", brandTypeList);
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
                Collections.sort(listMClass, (o1, o2) -> new Integer(o1.getNo()).compareTo(new Integer(o2.getNo())));
                model.addAttribute("listMClass", listMClass);

                String classStatusEnum = "\"acceptValue\":\"" + ClassStatusEnum.ACCEPT.name() + "\",";
                classStatusEnum += "\"acceptLabel\":\"" + ClassStatusEnum.ACCEPT.getLabel() + "\",";
                classStatusEnum += "\"rejectValue\":\"" + ClassStatusEnum.REJECT.name() + "\",";
                classStatusEnum += "\"rejectLabel\":\"" + ClassStatusEnum.REJECT.getLabel() + "\"";

                model.addAttribute("classStatusEnum", "{" + classStatusEnum + "}");
            }
            if (form.equals("dokumen") || form.equals("")) {
//				List<TxTmDoc> listDocType = new ArrayList<TxTmDoc>();
//		        List<TxTmDoc> txtmDocList = doclampiranService.getAllDocByApplicationId("txTmGeneral.id", txTmGeneral.getId());
//		        List<MDocType> mDocTypeList = docTypeService.findByStatusFlagTrue();
//		        for (MDocType result : mDocTypeList) {
//		        	boolean status = txtmDocList == null ? false : txtmDocList.stream().filter(o -> o.getmDocType().getId().equals(result.getId())).findFirst().isPresent();
//		        	TxTmDoc txTmDoc = new TxTmDoc();
//		        	txTmDoc.setmDocType(result);
//		        	txTmDoc.setStatus(status); 
//		        	listDocType.add(txTmDoc);
//		        }
//		        Collections.sort(listDocType, (o1, o2) -> o1.getmDocType().getName().compareTo(o2.getmDocType().getName()));
//		        model.addAttribute("listDocType", listDocType);
//		        model.addAttribute("docUploadDate", DateUtil.formatDate(new Date(), "dd/MM/yyyy"));
//
//                List<KeyValue> searchCriteria = new ArrayList<>();
//                searchCriteria.add(new KeyValue("mDocType.statusFlag", true, true));
//                if (!(boolean) model.asMap().get("isNewOrEdit")) {
//                    searchCriteria.add(new KeyValue("mFileType.id", txTmGeneral.getTxReception().getmFileType().getId(), true));
//                }
//
////                List<MDocTypeDetail> listDocType = docTypeService.selectAllDetail(searchCriteria);
//                model.addAttribute("listDocType", listDocType);
//                model.addAttribute("docUploadDate", DateUtil.formatDate(new Date(), "dd/MM/yyyy"));
                List<KeyValue> searchCriteria = new ArrayList<>();
                TxReception txReception = txTmGeneral.getTxReception();
                searchCriteria.add(new KeyValue("mDocType.statusFlag", true, true));
                if (!(boolean) model.asMap().get("isNew")) {
                    searchCriteria.add(new KeyValue("mFileType.id", txReception.getmFileType().getId(), true));
                }
                else{
                    searchCriteria.add(new KeyValue("mFileType.id", "MADRID_OO", true));
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

                List<MFileType> fileTypeList = fileService.findByMenu("MADRID_OO");
                model.addAttribute("fileTypeList", fileTypeList);

                List<MFileTypeDetail> fileTypeDetailList = fileService.findByCode("M2");
                model.addAttribute("fileTypeDetailList", fileTypeDetailList);

                MFileSequence fileSequenceList = fileService.getFileSequenceByCode("ID");
                model.addAttribute("fileSequenceList", fileSequenceList);

                List<MLookup> mLanguageList = lookupService.selectAllbyGroup("PilihanBahasa");
                model.addAttribute("listLanguage", mLanguageList);

                List<MLookup> mLanguageList2 = lookupService.selectAllbyGroup("PilihanBahasa2");
                model.addAttribute("listLanguage2", mLanguageList2);

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

                List<TxTmClassLimitation> txTmClassLimitationList = txTmClassLimitationCustomRepository.selectAllClassByGeneralId(txTmGeneral.getId());
                Map<String, String[]> listTxTmClassLimitation = new HashMap<>();
                if (txTmClassLimitationList != null) {
                    for (TxTmClassLimitation result : txTmClassLimitationList) {
                        String negara = result.getmCountry().getName();
                        String kelas = result.getmClass().getNo().toString();
                        String descEn = result.getmClassDetail().getDescEn();

                        if (listTxTmClassLimitation.containsKey(negara)) {
                            kelas = listTxTmClassLimitation.get(negara)[0] + "; " + kelas;
                            descEn = listTxTmClassLimitation.get(negara)[1] + "; " + descEn;
                        }

                        listTxTmClassLimitation.put(negara, new String[]{kelas, descEn});
                    }
                }
                model.addAttribute("listTxTmClassLimitation", listTxTmClassLimitation);


            }

            TxTmMadridFee txTmMadridFee = txTmGeneral.getTxTmMadridFee();
            if (txTmMadridFee == null) {
                txTmMadridFee = new TxTmMadridFee();
            } else {
                txTmMadridFee.setTmpLanguage2(txTmGeneral.getLanguage2());
            }


            List<TxTmMadridFeeDetail> txTmMadridFeeDetails = txTmMadridFee.getTxTmMadridFeeDetails();



            if (txTmMadridFeeDetails == null) {
                txTmMadridFeeDetails = new ArrayList<>();
            }

            model.addAttribute("txTmMadridFee", txTmMadridFee);
            model.addAttribute("txTmMadridFeeDetails", txTmMadridFeeDetails);

            if (request.getRequestURI().contains(PATH_PRATINJAU_PERMOHONAN_MADRID_OO)) {
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

    @RequestMapping(path = REQUEST_MAPPING_PERMOHONAN_MADRID_OO)
    public String doShowPagePermohonan(@RequestParam(value = "error", required = false) String error, Model model, final HttpServletRequest request, final HttpServletResponse response) {
        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("errorMessage", error);
        }
//        doInitiatePermohonan(model, request);
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

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MUser mUser = (MUser) auth.getPrincipal();
        if (mUser.isReprs())
            model.addAttribute("typeuser","(Konsultan)");
        else
            model.addAttribute("typeuser","(Umum)");
        return PAGE_PERMOHONAN_MADRID_OO;
    }

    @RequestMapping(path = REQUEST_MAPPING_TAMBAH_PERMOHONAN_MADRID_OO, method = {RequestMethod.GET})
    public String doShowPageTambahPermohonan(Model model, final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
//    	if(isNotRepresentative()) {
//    		return REDIRECT_PERMOHONAN_ONLINE + "?error=Hanya user konsultan yang dapat mengakses menu ini. Silakan hubungi Administrator.";
//    	}

        doInitiatePermohonan(model, request);


//        //Form 1 - General
//        String eFilingNo = request.getParameter("no");
//        TxTmGeneral txTmGeneral = eFilingNo == null ? new TxTmGeneral() : trademarkService.selectOneGeneralByeFilingNo(eFilingNo);
//
//        model.addAttribute("eFilingNo", eFilingNo);
//        model.addAttribute("noGeneral", txTmGeneral.getApplicationNo());
//        model.addAttribute("isNew", request.getRequestURI().contains(PATH_TAMBAH_PERMOHONAN_MADRID_OO));
//        model.addAttribute("isEditPermohonan", request.getRequestURI().contains(PATH_EDIT_PERMOHONAN_MADRID_OO));
//        model.addAttribute("isNewOrEdit", request.getRequestURI().contains(PATH_TAMBAH_PERMOHONAN_MADRID_OO) ||request.getRequestURI().contains(PATH_EDIT_PERMOHONAN_MADRID_OO));
//        model.addAttribute("isEdit", request.getRequestURI().contains(PATH_EDIT_PRATINJAU_MADRID_OO));
//        model.addAttribute("isReprs", isRepresentative());
//        model.addAttribute("applicationDate", new Timestamp(System.currentTimeMillis()));
//
//        TxReception txReception = txTmGeneral.getTxReception();
//        if (txReception == null) {
//            txReception = new TxReception();
//        }
//
//        txReception.setApplicationDate(new Timestamp(System.currentTimeMillis()));
////        txReception.setLanguangeList(mLanguageList);
//        model.addAttribute("txReception", txReception);
//        model.addAttribute("totalClass", txReception.getTotalClass());
//
//        List<MLookup> mLanguageList = lookupService.selectAllbyGroup("PilihanBahasa");
//        model.addAttribute("listLanguage", mLanguageList);
//
//        //Form 2 - Pemohon
//        TxTmOwner txTmOwner = permohonanService.selectOneOwnerByApplicationNo(txTmGeneral.getId());
//        if (txTmOwner == null) {
//            txTmOwner = new TxTmOwner();
//        }
//        txTmOwner.setTxTmGeneral(txTmGeneral);
//
//        if (txTmOwner.getPostProvince() == null) {
//            MProvince mProvince = new MProvince();
//            txTmOwner.setPostProvince(mProvince);
//        }
//
//        if (txTmOwner.getPostCountry() == null) {
//            MCountry mCountry = new MCountry();
//            txTmOwner.setPostCountry(mCountry);
//        }
//
//        if (txTmOwner.getPostCity() == null) {
//            MCity mCity = new MCity();
//            txTmOwner.setPostCity(mCity);
//        }
//
//        model.addAttribute("txTmOwner", txTmOwner);
//
//        List<TxTmOwnerDetail> txTmOwnerDetails = permohonanService.selectAllOwnerByOwnerId(txTmOwner.getId());
//        model.addAttribute("txTmOwnerDetails", txTmOwnerDetails);
//    	List<MCountry> mCountryList = countryService.findAll();
//        model.addAttribute("listCountry", mCountryList);
//
//    	List<MProvince> mProvinceList = provinceService.findAll();
//        model.addAttribute("listProvince", mProvinceList);
//
//        List<MCity> mCityList = cityService.selectAll();
//        model.addAttribute("listCity", mCityList);
//
//        List<MLookup> mRightList = lookupService.selectAllbyGroup("HakPengajuan");
//        model.addAttribute("listRight", mRightList);
//
//        //Form 3 - Kuasa
//        TxTmRepresentative txTmRepresentative = reprsService.selectOneByApplicationId(txTmGeneral.getId());
//        if (txTmRepresentative == null) {
//            txTmRepresentative = new TxTmRepresentative();
//        }
//        if (txTmRepresentative.getmRepresentative() == null) {
//            MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//            MRepresentative mRepresentative = representativeService.selectOne("userId.id", mUser.getId());
//            if (mRepresentative == null) {
//                mRepresentative = new MRepresentative();
//            }
//            txTmRepresentative.setmRepresentative(mRepresentative);
//        }
//        if (txTmRepresentative.getmRepresentative().getmCountry() == null) {
//            txTmRepresentative.getmRepresentative().setmCountry(new MCountry());
//        }
//        if (txTmRepresentative.getmRepresentative().getmProvince() == null) {
//            txTmRepresentative.getmRepresentative().setmProvince(new MProvince());
//        }
//        if (txTmRepresentative.getmRepresentative().getmCity() == null) {
//            txTmRepresentative.getmRepresentative().setmCity(new MCity());
//        }
//
//        model.addAttribute("txTmReprs", txTmRepresentative);
//
//        //Form 3 - Prioritas
//        List<MLaw> lawList = lawService.findByStatusFlagTrue();
//        model.addAttribute("listLaw", lawList);
//
//        List<TxTmClass> txTmClassList = classService.selectAllClassByIdGeneral(txTmGeneral.getId(), ClassStatusEnum.ACCEPT.name());
//        Map<Integer, String[]> listTxTmClass = new HashMap<>();
//        if (txTmClassList != null) {
//            for (TxTmClass result : txTmClassList) {
//                int key = result.getmClass().getNo();
//                String desc = result.getmClassDetail().getDesc();
//                String descEn = result.getmClassDetail().getDescEn();
//
//                if (listTxTmClass.containsKey(key)) {
//                    desc = listTxTmClass.get(key)[0] + "; " + desc;
//                    descEn = listTxTmClass.get(key)[1] + "; " + descEn;
//                }
//
//                listTxTmClass.put(key, new String[]{desc, descEn,});
//            }
//        }
//        model.addAttribute("listTxTmClass", listTxTmClass);
//
//
//        //Form 4 - Merek
//        TxTmBrand txTmBrand = txTmGeneral.getTxTmBrand();
//        if (txTmBrand == null) {
//            txTmBrand = new TxTmBrand();
//            txTmBrand.setmBrandType(new MBrandType());
//        }
//
//        String imgMerek = "";
//        try {
//            String pathFolder = DateUtil.formatDate(txTmBrand.getCreatedDate(), "yyyy/MM/dd/");
//            File file = new File(uploadFileBrandPath + pathFolder + txTmBrand.getId() + ".jpg");
//            if (file.exists() && !file.isDirectory()) {
//                String formatFile = txTmBrand.getFileName().substring(txTmBrand.getFileName().lastIndexOf(".") + 1);
//                FileInputStream fileInputStreamReader = new FileInputStream(file);
//                byte[] bytes = new byte[(int) file.length()];
//                fileInputStreamReader.read(bytes);
//                imgMerek = "data:image/jpg" + formatFile + ";base64," + Base64.getEncoder().encodeToString(bytes);
//                fileInputStreamReader.close();
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        model.addAttribute("txTmBrand", txTmBrand);
////        model.addAttribute("imgMerek", imgMerek);
//
//        List<MBrandType> listMBrandType = brandService.findByStatusFlagTrue();
//        model.addAttribute("listBrandType", listMBrandType);
//
//
//        txTmGeneral.setTxTmPriorList(permohonanService.selectAllPriorByGeneralId(txTmGeneral.getId()));
//
//        model.addAttribute("txTmGeneral", txTmGeneral);
//
//        List<MLookup> brandTypeList = lookupService.selectAllbyGroup("TipeMerekTambahan");
//        model.addAttribute("brandTypeList", brandTypeList);
//
//        MLookup brandType1 = lookupService.selectOneById("TipeMerek1");
//        model.addAttribute("brandType1", brandType1);
//        MLookup brandType2 = lookupService.selectOneById("TipeMerek2");
//        model.addAttribute("brandType2", brandType2);
//        MLookup brandType3 = lookupService.selectOneById("TipeMerek3");
//        model.addAttribute("brandType3", brandType3);
//
//        //Form 5 - Kelas
//        List<MLookup> mKonfirm = lookupService.selectAllbyGroup("UICariKelas");
//        model.addAttribute("lookupKonfirm", mKonfirm);
//
//        List<MClass> listMClass = classService.findByStatusFlagTrue();
//        Collections.sort(listMClass, (o1, o2) -> new Integer(o1.getNo()).compareTo(new Integer(o2.getNo())));
//        model.addAttribute("listMClass", listMClass);
//
//        String classStatusEnum = "\"acceptValue\":\"" + ClassStatusEnum.ACCEPT.name() + "\",";
//        classStatusEnum += "\"acceptLabel\":\"" + ClassStatusEnum.ACCEPT.getLabel() + "\",";
//        classStatusEnum += "\"rejectValue\":\"" + ClassStatusEnum.REJECT.name() + "\",";
//        classStatusEnum += "\"rejectLabel\":\"" + ClassStatusEnum.REJECT.getLabel() + "\"";
//
//        model.addAttribute("classStatusEnum", "{" + classStatusEnum + "}");
//
//      //List<MFileType> fileTypeList = fileService.getAllFileTypes();
//        //model.addAttribute("fileTypeList", fileTypeList);
//        List<KeyValue> searchCriteria = new ArrayList<>();
//        searchCriteria.add(new KeyValue("menu", "DAFTAR", true));
//        searchCriteria.add(new KeyValue("statusFlag", true, true));
//        List<MFileType> fileTypeList = fileService.findByMenu("MADRID_OO");
//        model.addAttribute("fileTypeList", fileTypeList);
//
//        List<MFileTypeDetail> fileTypeDetailList = fileService.findByCode("M2");
//        model.addAttribute("fileTypeDetailList", fileTypeDetailList);
//
//        MFileSequence fileSequenceList = fileService.getFileSequenceByCode("ID");
////        List<MFileSequence> fileSequenceList = fileService.getAllFileSequences();
//        model.addAttribute("fileSequenceList", fileSequenceList);
//
//        List<TxTmBrandDetail> txTmBrandDetailList = new ArrayList<>();
//        Map<Integer, String[]> listTxTmBrandDetail = new HashMap<>();
//        if (txTmGeneral.getTxTmBrand() != null) {
//            txTmBrandDetailList = txTmGeneral.getTxTmBrand().getTxTmBrandDetailList();
//            int countTxTmClass = 0;
//            if (listTxTmBrandDetail != null) {
//                for (TxTmBrandDetail result : txTmBrandDetailList) {
//                    countTxTmClass++;
//                    int key = countTxTmClass;
//                    String tanggalUpload = result.getUploadDateTemp();
//                    String namaFile = result.getFileName();
//                    String ukuran = result.getSize();
//                    String deskripsi = result.getFileDescription();
//
//                    String imgMerekAdd = "";
//                    try {
//                        String pathFolder = DateUtil.formatDate(result.getUploadDate(), "yyyy/MM/dd/");
//                        File file = new File(uploadFileBrandDetailPath + pathFolder + result.getId() + "." +  FilenameUtils.getExtension(result.getFileName()));
//                        if (file.exists() && !file.isDirectory()) {
//                            String formatFile = result.getFileName().substring(result.getFileName().lastIndexOf(".") + 1);
//                            FileInputStream fileInputStreamReader = new FileInputStream(file);
//                            byte[] bytes = new byte[(int) file.length()];
//                            fileInputStreamReader.read(bytes);
//                            imgMerekAdd = result.getFileDataType() + "," + Base64.getEncoder().encodeToString(bytes);
//                            fileInputStreamReader.close();
//                        }
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                    listTxTmBrandDetail.put(key, new String[]{tanggalUpload, namaFile, ukuran, deskripsi, imgMerekAdd});
//                }
//            }
//        }
//
//
//        model.addAttribute("txTmBrandDetailList", listTxTmBrandDetail);
//
//        //Form 6 - Biaya
//        TxTmMadridFee txTmMadridFee = txTmGeneral.getTxTmMadridFee();
//        if (txTmMadridFee == null) {
//        	txTmMadridFee = new TxTmMadridFee();
//        }
//
//        List<TxTmMadridFeeDetail> txTmMadridFeeDetails = txTmMadridFee.getTxTmMadridFeeDetails();
//        if (txTmMadridFeeDetails == null) {
//        	txTmMadridFeeDetails = new ArrayList<>();
//        }
//
//        model.addAttribute("txTmMadridFee", txTmMadridFee);
//        model.addAttribute("txTmMadridFeeDetails", txTmMadridFeeDetails);

        //Form 7 - Dokumen


        return PAGE_TAMBAH_PERMOHONAN_MADRID_OO;
    }

    @RequestMapping(path = REQUEST_MAPPING_PRATINJAU_PERMOHONAN_MADRID_OO, method = {RequestMethod.GET})
    public String doShowPagePratinjauPermohonan(@RequestParam(value = "error", required = false) String error, @RequestParam(value = "no", required = true) String no, Model model, final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
        MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByeFilingNo(no);

        if (mUser.getUsername() != "super") {
            List<KeyValue> criteriaList = new ArrayList<>();
            criteriaList.add(new KeyValue("txReception.eFilingNo", no, true));
            criteriaList.add(new KeyValue("createdBy", mUser, true));
            txTmGeneral = trademarkService.selectOneGeneralByeFilingNoByUser(criteriaList);

            if (txTmGeneral == null) {
                return "redirect:" + PATH_AFTER_LOGIN + PATH_PERMOHONAN_MADRID_OO + "?error=" +"Halaman tidak ditemukan";
            }
        }

        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("errorMessage", error);
        }

        if (txTmGeneral.getmStatus().getId().equalsIgnoreCase(StatusEnum.IPT_DRAFT.name())) {
            return "redirect:" + PATH_AFTER_LOGIN + PATH_EDIT_PERMOHONAN_MADRID_OO + "?no=" + no;
        } else {
            doInitiatePermohonan(model, request);
            return PAGE_PRATINJAU_PERMOHONAN_MADRID_OO;
        }
    }

    @RequestMapping(path = {REQUEST_MAPPING_EDIT_PRATINJAU_MADRID_OO_PEMOHON, REQUEST_MAPPING_EDIT_PRATINJAU_MADRID_OO_KUASA,
            REQUEST_MAPPING_EDIT_PRATINJAU_MADRID_OO_PRIOR, REQUEST_MAPPING_EDIT_PRATINJAU_MADRID_OO_MEREK,
            REQUEST_MAPPING_EDIT_PRATINJAU_MADRID_OO_KELAS, REQUEST_MAPPING_EDIT_PRATINJAU_MADRID_OO_DOKUMEN},
            method = {RequestMethod.GET})
    public String doShowPageEditPratinjauPermohonan(@RequestParam(value = "no", required = true) String no, Model model, final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
//    	if(isNotRepresentative()) {
//    		return REDIRECT_PRATINJAU_PERMOHONAN_ONLINE + "?error=Hanya user konsultan yang dapat mengakses menu ini. Silakan hubungi Administrator.&no=" + no;
//    	}
        doInitiatePermohonan(model, request);
        return PAGE_EDIT_PRATINJAU_PERMOHONAN_ONLINE;
    }

    @RequestMapping(path = REQUEST_MAPPING_AJAX_SEARCH_LIST_MADRID_OO, method = {RequestMethod.POST})
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
                        if (searchBy.equalsIgnoreCase("applicationDate") || searchBy.equalsIgnoreCase("txReception.applicationDate")) {
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

            MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            searchCriteria.add(new KeyValue("txReception.onlineFlag", Boolean.TRUE, true));
            searchCriteria.add(new KeyValue("txReception.createdBy", mUser, true));
            searchCriteria.add(new KeyValue("txReception.mFileType.menu", "MADRID_OO", true));

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
                            orderBy = "applicationNo";
                            break;
                        case "6":
                            orderBy = "txReception.mFileType.desc";
                            break;
                        case "7":
                            orderBy = "txReception.mFileTypeDetail.desc";
                            break;
                        case "8":
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
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxTmGeneral result : searchResult.getList()) {
                    no++;
                    String brandName = "-";
                    String brandType = "-";
                    String status = "-";

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

                    String appNo = result.getApplicationNo().equals(result.getTxReception().geteFilingNo()) ? "-" : result.getApplicationNo();
                    String button = "";
                    String btnCetakSuratPernyataan = "";
                    if (result.getmStatus().getId().equalsIgnoreCase(StatusEnum.IPT_DRAFT.name())) {
                        button = "<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT_PERMOHONAN_MADRID_OO + "?no=" + result.getTxReception().geteFilingNo()) + "\">Ubah</a>";
                    } else {
                        button = "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin(PATH_CETAK_MEREK_MADRID_OO) + "?no=" + result.getApplicationNo() + "\">Tanda Terima</a>  ";
                    }
                    String fileTypeDesc = "";
                    try {
                        fileTypeDesc = result.getTxReception().getmFileType().getDesc();
                    } catch (Exception ex) {}
                    String fileTypeDetDesc = "";
                    try {
                        fileTypeDetDesc = result.getTxReception().getmFileTypeDetail().getDesc();
                    } catch (Exception ex) {}

                    data.add(new String[]{
                            "" + no,
                            "<a target=\"_blank\" href=\"" + getPathURLAfterLogin(PATH_PRATINJAU_PERMOHONAN_MADRID_OO + "?no=" + result.getTxReception().geteFilingNo()) + "\">" + result.getTxReception().geteFilingNo() + "</a>",
                            result.getTxReception().getApplicationDateTemp(),
                            brandType,
                            brandName,
                            appNo,
                            fileTypeDesc,
                            fileTypeDetDesc,
                            status,
                            "<div class=\"btn-actions\">" + button + "<br/>" + (result.getmStatus().getId().equalsIgnoreCase(StatusEnum.IPT_DRAFT.name()) ? "" : btnCetakSuratPernyataan) + "</div>"
                    });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_EDIT_PERMOHONAN_MADRID_OO, method = {RequestMethod.GET})
    public String doShowPageEditPermohonan(Model model, final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
//    	if(isNotRepresentative()) {
//    		return REDIRECT_PERMOHONAN_ONLINE + "?error=Hanya user konsultan yang dapat mengakses menu ini. Silakan hubungi Administrator.";
//    	}
        MUser userLogin = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userLogin.getUsername() != "super") {
            String eFilingNo = request.getParameter( "no" );

            List<KeyValue> criteriaList = new ArrayList<>();
            criteriaList.add(new KeyValue("txReception.eFilingNo", eFilingNo, true));
            criteriaList.add(new KeyValue("createdBy", userLogin, true));
            TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByeFilingNoByUser(criteriaList);

            if (txTmGeneral == null) {
                return "redirect:" + PATH_AFTER_LOGIN + PATH_PERMOHONAN_MADRID_OO + "?error=" +"Halaman tidak ditemukan";
            } else if(!txTmGeneral.getmStatus().getId().equals("IPT_DRAFT")) {
                return "error-404";
            }
        }
        doInitiatePermohonan(model, request);

//        List<MDocTypeDetail> listDocType = docTypeService.selectAllDetail(searchCriteria);



        return PAGE_EDIT_PERMOHONAN_MADRID_OO;
    }

    @RequestMapping(path = REQUEST_MAPPING_SAVE_FORM_GENERAL_MADRID_OO, method = {RequestMethod.POST})
    @ResponseBody
    public void doSaveFormGeneral(@RequestBody DataForm1 form, Model model,
                                  final HttpServletRequest request, final BindingResult errors, final HttpServletResponse response,
                                  final HttpSession session) {
        FieldValidationUtil.required(errors, "mFileSequence.id", form.getmFileSequence().getCurrentId(), "asal permohonan");
        FieldValidationUtil.required(errors, "mFileType.id", form.getmFileType().getCurrentId(), "tipe permohonan");
        FieldValidationUtil.required(errors, "mFileTypeDetail.id", form.getmFileTypeDetail().getCurrentId(), "jenis permohonan");
        TxReception recep = permohonanOnlineService.selectOneTxReception("bankCode",form.getBankCode());
        String validationError = "errorValid";

        int free = 0 ;
        if (freebill.equalsIgnoreCase("YES")){
            free = 1;
        }

        if (!errors.hasErrors()) {
            try {
                if ((free == 0) && (form.getmFileType().getId().equalsIgnoreCase("MEREK_DAGANG_JASA") && form.getTotalClass() <= 1))  {
                    writeJsonResponse(response, validationError);
                } else if ((free == 0)&&(recep != null && form.getAppid().isEmpty())) {
                    validationError = "usedBilling";
                    writeJsonResponse(response, validationError);
                } else {
            		String application_no = trademarkService.insertReceptionOnline(form);
            		String[] receptionIDs = form.getReceptionIds();
            		// possible multiple reception ID 
            		for(int i=0;i<receptionIDs.length;i++) {
                        madridOoService.saveBrandAndClassFromReference(application_no, receptionIDs[i]);
                    }
                    writeJsonResponse(response, application_no);
                }
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                model.addAttribute("errorMessage", "Gagal menambahkan permohonan madrid online tab general");
                writeJsonResponse(response, 200);
            }
        }
    }
    
    @RequestMapping(path = REQUEST_MAPPING_GET_JUMLAH_KELAS, method = {RequestMethod.POST})
      public void getTotalClass(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
          if (isAjaxRequest(request)) {
              setResponseAsJson(response);

              String appNo = request.getParameter("appNo");
              TxTmGeneral txTmGeneral = trademarkService.selectOne("applicationNo", appNo);
          	  int countClass =  txReceptionRepository.countClass(txTmGeneral);

              writeJsonResponse(response, countClass); 
          } else { 
              response.setStatus(HttpServletResponse.SC_FOUND);
          }
      }
 

    @RequestMapping(path = REQUEST_MAPPING_DELETE_FORM_GENERAL_MADRID_OO, method = {RequestMethod.POST})
    @ResponseBody
    public void doDeleteFormGeneral(@RequestBody DataForm1 form, Model model,
                                  final HttpServletRequest request, final BindingResult errors, final HttpServletResponse response,
                                  final HttpSession session) {
        FieldValidationUtil.required(errors, "mFileSequence.id", form.getmFileSequence().getCurrentId(), "asal permohonan");
        FieldValidationUtil.required(errors, "mFileType.id", form.getmFileType().getCurrentId(), "tipe permohonan");
        FieldValidationUtil.required(errors, "mFileTypeDetail.id", form.getmFileTypeDetail().getCurrentId(), "jenis permohonan");
        TxReception recep = permohonanOnlineService.selectOneTxReception("bankCode",form.getBankCode());
        String validationError = "errorValid";
        if (!errors.hasErrors()) {
            try {
                if(form.getmFileType().getId().equalsIgnoreCase("MEREK_DAGANG_JASA") && form.getTotalClass() <= 1) {
                    writeJsonResponse(response, validationError);
                } else if (recep != null && form.getAppid().isEmpty()) {
                    validationError = "usedBilling";
                    writeJsonResponse(response, validationError);
                } else {
                    String application_no = form.getAppid(); //application id dsini
//                    txReception.setApplicationDate(DateUtil.toDate("dd/MM/yyyy HH:mm:ss", data.getApplicationDate()));
//                    String application_no = trademarkService.insertReceptionOnline(form);
                    String[] receptionIDs = form.getReceptionIds();
                    for(String receptionID: receptionIDs) {
                        madridOoService.deleteBrandAndClassFromReference(application_no, receptionID);
                    }
                    writeJsonResponse(response, application_no);
                }
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                model.addAttribute("errorMessage", "Gagal menghapus permohonan madrid online tab general");
                writeJsonResponse(response, 200);
            }
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

        int free = 0 ;
        if (freebill.equalsIgnoreCase("YES")){
            free = 1 ;
              bankCode = "820200101223284";
        }

        if ((txReception != null)&&(free == 0)) {
            statusError = "Kode Billing '" + bankCode + "' Sudah Digunakan";
        } else {
            try {
                String result = simpakiService.getQueryBilling(bankCode);
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(result);
//                System.out.println("query billing "+result);

                String code = rootNode.get("code").toString().replaceAll("\"", "");

                String message = rootNode.get("message").toString().replaceAll("\"", "");
                if (free == 1){code = "00" ;}
                if (code.equals("00"))  {

                    String data = rootNode.get("data").toString();
                    JsonNode dataNode = mapper.readTree(data);

                    String flagPayment = dataNode.get("flag_pembayaran").toString().replaceAll("\"", "");
                    String flagUsed = dataNode.get("terpakai").toString().replaceAll("\"", "");

                    if ((flagPayment.equalsIgnoreCase("BELUM")) && (free == 0))
                    {
                        statusError = "Kode Billing '" + bankCode + "' Belum Dibayar";
                    } else if ((!flagUsed.equalsIgnoreCase("BELUM")) && (free == 0)) {
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
                        //System.out.println("query billing "+mPnbpFeeCode.toString());

                        if ((mPnbpFeeCode == null) && (free == 0)) {
                            statusError = "Kode Tarif '" + feeCode + "' Tidak Ditemukan";
                        } else if ((!mPnbpFeeCode.getmFileType().getId().equals("MADRID_OO")) && (free == 0)) {
                            statusError = "Silakan klik tombol 'Check' kode billing terlebih dahulu";
                        } else {
                            if (free == 1){
                                statusError = "Anda berada di mode profile Free Billing Code. Jangan Aktifkan saat Live! ";
                            }

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
                            String noTransaksi = dataNode.get("nomor_transaksi").toString().replaceAll("\"", "");

                            dataGeneral.put("mFileSequence", mUser.getmFileSequence().getId());
                            dataGeneral.put("mFileType", null);
                            dataGeneral.put("mFileTypeDetail", mPnbpFeeCode.getmFileTypeDetail().getId());
                            dataGeneral.put("mLaw", lawService.findByStatusFlagTrue().get(0).getId());
                            dataGeneral.put("totalClass", totalClass);
                            dataGeneral.put("totalPayment", totalPayment);
                            dataGeneral.put("totalPaymentTemp", NumberUtil.formatDecimal(NumberUtil.parseDecimal(totalPayment)));
                            dataGeneral.put("paymentDate", DateUtil.formatDate(DateUtil.toDate("yyyy-MM-dd HH:mm:ss", paymentDate), "dd/MM/yyyy HH:mm:ss"));
                            dataGeneral.put("ownerName", ownerName);
                            dataGeneral.put("ownerEmail", ownerEmail);
                            dataGeneral.put("ownerPhone", ownerPhone);
                            dataGeneral.put("noTransaksi", noTransaksi);

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

    @RequestMapping(path = REQUEST_MAPPING_SAVE_FORM_PEMOHON_MADRID_OO, method = {RequestMethod.POST})
    @ResponseBody
    public void doSaveFormPemohon(@RequestBody String input, final HttpServletRequest request, final BindingResult errors, final HttpServletResponse response, final HttpSession session) throws JsonProcessingException, IOException, ParseException {

        //System.out.println(input);

        Gson g = new Gson();
        JsonObject item = g.fromJson(RESTService.toPrettyFormat(input),JsonObject.class);

        String id = item.get("id").toString().replace("\"","") ;



//        Gson gson = new Gson();
////        JsonParser parser = new JsonParser();
////        JsonObject object = (JsonObject) parser.parse(input);
////        // https://stackoverflow.com/questions/10308452/how-to-convert-the-following-json-string-to-java-object
////        TxTmOwner txTmOwner = gson.fromJson(object, TxTmOwner.class);




        TxTmOwner pemohon = txTmOwnerCustomRepository.selectOne("id",id);
        if (pemohon == null){
           pemohon = new TxTmOwner() ;
           pemohon.setId(id);
        }


        String applicationNo = item.get("txTmGeneral").toString().replace("\"","");
             JsonObject app = g.fromJson(RESTService.toPrettyFormat(applicationNo),JsonObject.class);
             applicationNo = app.get("applicationNo").toString().replace("\"","");
             TxTmGeneral txg = txTmGeneralRepository.findOne(applicationNo);


             if(txg != null){
                 pemohon.setTxTmGeneral(txg);

                 String noReps ="" ;
                 try{
                    noReps = item.get("noReps").toString().replace("\"","");
                 }
                 catch(Exception e){

                 }

                 if ( !noReps.equalsIgnoreCase("") ){

                     TxTmRepresentative txTmRepresentative = txTmReprsCustomRepository.selectOne("txTmGeneral.id",applicationNo,true);
                     MRepresentative mrep = representativeService.findOneByNoReps(noReps);

                     Boolean isNew = false ;
                     if (mrep != null){
                         if (txTmRepresentative == null){
                             txTmRepresentative = new TxTmRepresentative();
                             isNew = true ;
                         }
                         txTmRepresentative.setId(txTmRepresentative.getId());
                         txTmRepresentative.setName(mrep.getName());
                         txTmRepresentative.setAddress(mrep.getAddress());
                         txTmRepresentative.setTxTmGeneral(txg);
                         txTmRepresentative.setCreatedDate(txTmRepresentative.getCreatedDate());
                         txTmRepresentative.setmRepresentative(mrep);

                         txTmRepresentative.setEmail(mrep.getEmail());
                         txTmRepresentative.setmCity(mrep.getmCity());
                         txTmRepresentative.setmProvince(mrep.getmProvince());
                         txTmRepresentative.setmCountry(mrep.getmCountry());
                         txTmRepresentative.setOffice(mrep.getOffice());
                         txTmRepresentative.setPhone(mrep.getPhone());
                         txTmRepresentative.setStatus(mrep.isStatusFlag());

                         if (isNew == true)
                             txTmReprsRepository.save(txTmRepresentative);
                         else
                             madridOoService.saveTxTmRepresentative(txTmRepresentative);


                     }
                     // Jika dia ada Reps
                 }

                 // cari berdasarkan TxGeneral nya



                 String nationality = item.get("nationality").toString().replace("\"","");
                 app = g.fromJson(RESTService.toPrettyFormat(nationality),JsonObject.class);
                 nationality = app.get("id").toString().replace("\"","");
                 MCountry mc = mCountryCostumRepository.selectOne("id",nationality,true);
                 if (mc != null){ pemohon.setNationality(mc); }

                 String name = item.get("name").toString().replace("\"","") ; pemohon.setName(name);
//                 String no = item.get("no").toString().replace("\"","") ; pemohon.setNo(no);
                 String ownerType = item.get("ownerType").toString().replace("\"","") ; pemohon.setOwnerType(ownerType);

                 String mCountry = item.get("mCountry").toString() ;
                 app = g.fromJson(RESTService.toPrettyFormat(mCountry),JsonObject.class);
                 mCountry = app.get("id").toString().replace("\"","");
                 MCountry mc2 = mCountryCostumRepository.selectOne("id",mCountry,true);
                 if (mc2 != null){ pemohon.setmCountry(mc2); }

                 String mProvince = item.get("mProvince").toString();
                 app = g.fromJson(RESTService.toPrettyFormat(mProvince),JsonObject.class);
                 mProvince = app.get("id").toString().replace("\"","");
                 MProvince mp = mProvinceCustomRepository.selectOne("id",mProvince,true);
                 if (mp != null){ pemohon.setmProvince(mp);}

                 String mCity = item.get("mCity").toString() ;
                 app = g.fromJson(RESTService.toPrettyFormat(mCity),JsonObject.class);
                 mCity = app.get("id").toString().replace("\"","");
                 MCity mci = mCityCustomRepository.selectOne("id",mCity,true);
                 if (mci != null){ pemohon.setmCity(mci);}

                 String postCountry = item.get("postCountry").toString() ;
                 app = g.fromJson(RESTService.toPrettyFormat(postCountry),JsonObject.class);
                 postCountry = app.get("id").toString().replace("\"","");
                 MCountry c2 = mCountryCostumRepository.selectOne("id",postCountry,true);
                 if (c2 != null){ pemohon.setmCountry(c2); }

                 String postProvince = item.get("postProvince").toString() ;
                 app = g.fromJson(RESTService.toPrettyFormat(postProvince),JsonObject.class);
                 mProvince = app.get("id").toString().replace("\"","");
                 MProvince mp2 = mProvinceCustomRepository.selectOne("id",mProvince,true);
                 if (mp2 != null){ pemohon.setmProvince(mp2);}

                 String postCity = item.get("postCity").toString() ;
                 app = g.fromJson(RESTService.toPrettyFormat(postCity),JsonObject.class);
                 postCity = app.get("id").toString().replace("\"","");
                 MCity mcit = mCityCustomRepository.selectOne("id",postCity,true);
                 if (mcit != null){ pemohon.setmCity(mcit);}

                 String address =   item.get("address").toString().replace("\"","") ; pemohon.setAddress(address);
                 String zipCode =   item.get("zipCode").toString().replace("\"","") ; pemohon.setZipCode(zipCode);
                 String phone =   item.get("phone").toString().replace("\"","") ; pemohon.setPhone(phone);
                 String email =  item.get("email").toString().replace("\"","") ; pemohon.setEmail(email);
                 Boolean addressFlah = item.get("addressFlag").getAsBoolean() ; pemohon.setAddressFlag(addressFlah);
                 String postAddress =   item.get("postAddress").toString().replace("\"","") ; pemohon.setPostAddress(postAddress);
                 String postZipCode =   item.get("postZipCode").toString().replace("\"","") ; pemohon.setPostZipCode(postZipCode);
                 String postPhone =   item.get("postPhone").toString().replace("\"","") ; pemohon.setPostPhone(postPhone);
                 String postEmail =  item.get("postEmail").toString().replace("\"","") ; pemohon.setPostEmail(postEmail);
                 String legalEntity =  item.get("legalEntity").toString().replace("\"","") ; pemohon.setLegalEntity(legalEntity);
                 String commercialAddress =  item.get("commercialAddress").toString().replace("\"","") ; pemohon.setCommercialAddress(commercialAddress);
                 String entitlement =  item.get("entitlement").toString().replace("\"","") ; pemohon.setEntitlement(entitlement);

                 //status Owner
                 pemohon.setStatus(true);

                //      "postProvince":{"id":""},"postCity":{"id":""},"txTmOwnerDetails":[]}

                 String OwnerDetail = item.get("txTmOwnerDetails").toString().replace("\"","") ;
                 if (!OwnerDetail.equalsIgnoreCase("[]")){
                     for (TxTmOwnerDetail txTmOwnerDetail : pemohon.getTxTmOwnerDetails()) {
                         txTmOwnerDetail.setTxTmOwner(pemohon);
                         txTmOwnerDetail.setTxTmGeneral(pemohon.getTxTmGeneral());
                         txTmOwnerDetail.setStatus(true);
                     }
                 }



                 try {

                     permohonanService.insertForm2(pemohon);

                     writeJsonResponse(response, 200);
                 } catch (DataIntegrityViolationException e) {
                     logger.error(e.getMessage(), e);
                     writeJsonResponse(response, 500);
                 }

//                 if(txg != null)
             }







    }

    @RequestMapping(value = REQUEST_MAPPING_SAVE_FORM_KUASA_MADRID_OO, method = {RequestMethod.POST})
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
                txTmRepresentative.setmRepresentative(mRepresentative);
                /********* - ADD NEW COLUMN TX_TM_REPRS - *********/
                txTmRepresentative.setName( mRepresentative.getName() );
                txTmRepresentative.setOffice( mRepresentative.getOffice() );
                txTmRepresentative.setAddress( mRepresentative.getAddress() );
                txTmRepresentative.setmCountry( mRepresentative.getmCountry() );
                txTmRepresentative.setmProvince( mRepresentative.getmProvince() );
                txTmRepresentative.setmCity( mRepresentative.getmCity() );
                txTmRepresentative.setZipCode( mRepresentative.getZipCode() );
                txTmRepresentative.setEmail( mRepresentative.getEmail() );
                txTmRepresentative.setPhone( mRepresentative.getPhone() );
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



    @RequestMapping(value = REQUEST_MAPPING_SAVE_FORM_MEREK_MADRID_OO, method = {RequestMethod.POST})
    @ResponseBody
    public void doSaveFormMerek(@RequestParam("txTmBrand") String stxTmBrand, @RequestParam(value = "fileMerek", required = false) MultipartFile uploadfile,
                                @RequestParam("listImageDetail") String listImageDetail, @RequestParam("listDelete") String[] listDelete,
                                @RequestParam("agreeDisclaimer") String agreeDisclaimers,
                                final HttpServletRequest request, final HttpServletResponse response) throws IOException {

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);

        if (uploadfile != null && uploadfile.getSize() > 0) {
            BufferedImage img = ImageIO.read(uploadfile.getInputStream());

            if (uploadfile.getSize() > 5242880) {
                result.put("message", "Ukuran file label merek maksimal 5MB");
            } else if (img.getWidth() > 1024 || img.getHeight() > 1024) {
                result.put("message", "Dimensi file gambar label merek maksimal adalah 1024 x 1024, File gambar label merek Yang Anda Upload "+ img.getWidth() + " x " + img.getHeight() +". "
                        + "Silahkan melakukan resize gambar terlebih dahulu !!");
            } else {
                String filename = uploadfile.getOriginalFilename().toLowerCase();
                if (!filename.toUpperCase().endsWith(".JPG") && !filename.toUpperCase().endsWith(".JPEG")) {
                    result.put("message", "File label merek harus dalam format JPG / JPEG");
                }
                String mime = URLConnection.guessContentTypeFromStream(new BufferedInputStream(uploadfile.getInputStream()));
                if(mime == null || !mime.startsWith("image")) {
                    result.put("message", "File label merek harus dalam format JPG / JPEG");
                }
            }
        } else if(agreeDisclaimers.equalsIgnoreCase("false")) {
            result.put("message", "Anda belum menyetujui disclaimer");
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
                String agreeDisclaimer = "1";
                String translation = rootNode.get("translation").toString().replaceAll("\"", "");
                String typeCollective = rootNode.get("typeCollective").toString().replaceAll("\"", "");
                String colorCombination = rootNode.get("colorCombination").toString().replaceAll("\"", "");
                String standardChar = rootNode.get("standardChar").toString().replaceAll("\"", "");
                String colorIndication = rootNode.get("colorIndication").toString().replaceAll("\"", "");
                String translationFr = rootNode.get("translationFr").toString().replaceAll("\"", "");
                String translationSp = rootNode.get("translationSp").toString().replaceAll("\"", "");

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
//                String[] tranlationPattern = translation.split(Pattern.quote("\\n"));
//                for(String trnlate : tranlationPattern) {
//                	translations += trnlate + "\n";
//                }
//                if(translations.length() > 0) {
//                	translations = translations.substring(0, translations.length() - 1);
//        		}

                Timestamp tmstm = new Timestamp(System.currentTimeMillis());

                TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(no);

                TxTmBrand txTmBrand = txTmGeneral.getTxTmBrand() == null ? new TxTmBrand() : txTmGeneral.getTxTmBrand();
                MBrandType mBrandType = new MBrandType();
                mBrandType.setId(idmBrandType);

                txTmBrand.setTxTmGeneral(txTmGeneral);
                txTmBrand.setmBrandType(mBrandType);
                txTmBrand.setName(name);
                txTmBrand.setDescription(descMerek);
                txTmBrand.setTranslation(translation);
                txTmBrand.setDescChar(descriptionChar);
                txTmBrand.setColor(colors);
                txTmBrand.setDisclaimer(disclaimers);
                txTmBrand.setAgreeDisclaimer(Boolean.parseBoolean(agreeDisclaimer));
                txTmBrand.setColorIndication(colorIndication);
                txTmBrand.setTranslationFr(translationFr);
                txTmBrand.setTranslationSp(translationSp);
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

    @RequestMapping(value = REQUEST_MAPPING_SAVE_FORM_KELAS_MADRID_OO, method = {RequestMethod.POST})
    public void doSaveFormKelas(Model model, final HttpServletRequest request, final HttpServletResponse response,
                                @RequestParam("appNo") String appNo, @RequestParam("listClassChildId") String[] listClassChildId,
                                @RequestParam("listClassLimitChildId") String[] listClassLimitChildId) throws IOException {
        //String[] listClassChildId = request.getParameterValues("listClassChildId[]");
        //String appNo = request.getParameter("appNo");

        List<KeyValue> searchCriteria = new ArrayList<>();
        TxTmGeneral txTmGeneral = trademarkService.selectOne("applicationNo", appNo);
        searchCriteria.add(new KeyValue("txTmGeneral.id", txTmGeneral.getId(), true));
        List<String> listId = new ArrayList<String>();
        if (listClassChildId != null) {
            for (String classChildId : listClassChildId) {
                String[] classChildTemp = classChildId.split(";");
                listId.add(classChildTemp[0]);
            }
        }

        classService.deleteTxTMClassMultipleNotIn(appNo, listId.toArray(new String[listId.size()]));
//        GenericSearchWrapper<TxTmClass> searchResult = classService.searchGeneralTxClass(searchCriteria, "id", "ASC", 0, null);

//        if (searchResult.getCount() > 0) {
//            for (TxTmClass result : searchResult.getList()) {
//                classService.deleteTxTMClass(result);
//            }
//        }

        Timestamp tmstm = new Timestamp(System.currentTimeMillis());

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
                txTmClass.setCreatedDate(txTmGeneral.getCreatedDate());
                txTmClass.setCreatedBy(txTmGeneral.getCreatedBy());
                txTmClass.setUpdatedDate(tmstm);
                txTmClass.setUpdatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                txTmClass.setTransactionStatus(classChildTemp[1]);
                txTmClass.setCorrectionFlag(classChildTemp[2].equals("TRUE"));
                if(mClass != null) {
                    txTmClass.setEdition(mClass.getEdition());
                    txTmClass.setVersion(mClass.getVersion());
                }
                //insert kedalam txTmClass
                listTxClass[i] = txTmClass;
                i++;
            }
            classService.saveTxTMClass(listTxClass);
        }
        classService.deleteAllTxTmClassLimitationByAppId(txTmGeneral.getId());

        List<TxTmClassLimitation> classLimitations = new ArrayList<>();
        for (String classLimit : listClassLimitChildId) {
            String[] classChildTemp = classLimit.split(";");
            TxTmClassLimitation txTmClassLimitation = txTmClassLimitationRepository
                    .findByTxTmGeneralIdAndMClassDetailIdAndMCountryId(txTmGeneral.getId(),classChildTemp[0],classChildTemp[1]);
            if(txTmClassLimitation == null) {
                MClassDetail mClassDetail = classService.selectOneMClassDetailById(classChildTemp[0]);
                MClass mClass = mClassDetail.getParentClass();
                MCountry mCountry = countryService.findCountryById(classChildTemp[1]);
                txTmClassLimitation = new TxTmClassLimitation(txTmGeneral, mClass, mClassDetail, mCountry);
                classLimitations.add(txTmClassLimitation);
            }
        }
        txTmClassLimitationRepository.save(classLimitations);
    }

    @RequestMapping(value = REQUEST_MAPPING_SAVE_FORM_PRIOR_MADRID_OO, method = {RequestMethod.POST})
    public void doSaveFormPrior(Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ParseException {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);

            String negaraId = request.getParameter("negaraId");
            String no = request.getParameter("no");
            String appNo = request.getParameter("appNo");
            String catatan = request.getParameter("catatan");
            Timestamp tmstm = DateUtil.toDate("dd/MM/yyyy", request.getParameter("tgl"));
            Timestamp today = new Timestamp(System.currentTimeMillis());
            //check date before today till 6 months before
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
                txTmPrior.setNote(catatan);
                txTmPrior.setTxTmGeneral(trademarkService.selectOne("applicationNo", appNo));
                txTmPrior.setStatus(PriorStatusEnum.PENDING.name());
                priorService.doSaveOrUpdate(txTmPrior);
                result.put("success", true);
            }

            writeJsonResponse(response, result);
        }
    }

    @RequestMapping(value = REQUEST_MAPPING_SAVE_FORM_BIAYA_MADRID_OO, method = {RequestMethod.POST})
    public void doSaveFormBiaya(Model model, final HttpServletRequest request, final HttpServletResponse response,
                                @RequestParam("appNo") String appNo, @RequestParam("listBiayaNegara") String[] listBiayaNegara,
                                @RequestParam("basicFee") String basicFee, @RequestParam("volComp") String volComp, @RequestParam("totalComp") String totalComp,
                                @RequestParam("volSup") String volSup, @RequestParam("totalSup") String totalSup,
                                @RequestParam("language2") String language2, @RequestParam("totalBiaya") String totalBiaya) throws IOException, ParseException
    {

//        if (basicFee.equalsIgnoreCase(""))
//            basicFee = "0";
//        if (volComp.equalsIgnoreCase(""))
//            volComp = "0";
//        if (totalComp.equalsIgnoreCase(""))
//            totalComp = "0";
//        if (volSup.equalsIgnoreCase(""))
//            volSup = "0";
//        if (totalSup.equalsIgnoreCase(""))
//            totalSup = "0";
//        if (totalBiaya.equalsIgnoreCase(""))
//            totalBiaya = "0";



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

    @RequestMapping(value = REQUEST_MAPPING_SAVE_FORM_DOC_MADRID_OO, method = {RequestMethod.POST})
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

                if((docId.equalsIgnoreCase("TTDP") || docId.equalsIgnoreCase("TTDK")|| docId.equalsIgnoreCase("TTD")) &&
                        !(docFileName.toUpperCase().endsWith(".JPG") || docFileName.toUpperCase().endsWith(".JPEG"))) {
                    msg.setKey("fileTypeError");
                    msg.setValue("notMatchFileTypeImage");
                } else if (!(docId.equalsIgnoreCase("TTDP") || docId.equalsIgnoreCase("TTDK")|| docId.equalsIgnoreCase("TTD")) &&
                        !docFileName.toUpperCase().endsWith(".PDF")) {
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
                    if (uploadFile != null && (docId.equalsIgnoreCase("TTDP") || docId.equalsIgnoreCase("TTDK") || docId.equalsIgnoreCase("TTD"))) {
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

    @RequestMapping(path = REQUEST_MAPPING_MADRID_OO_CHOOSE_REFERENSI, method = {RequestMethod.GET})
    public void doChooseReferensi(final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) throws JsonProcessingException {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
                       
            String appId = request.getParameter("target"); 
            TxTmGeneral txTmGeneral = madridOoService.selectOneDataReferensi("id", appId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("applicationId", txTmGeneral.getId());
            result.put("applicationNo", txTmGeneral.getApplicationNo());
            result.put("registrationNo", txTmGeneral.getTxRegistration() == null ? "" : txTmGeneral.getTxRegistration().getNo());
            result.put("registrationDate", txTmGeneral.getTxRegistration() == null ? "" : txTmGeneral.getTxRegistration().getStartDateTemp());
            result.put("penerimaanDate", txTmGeneral.getFilingDateTemp() == null ? "" : txTmGeneral.getFilingDateTemp());

            txTmGeneral.getTxTmBrand().toJson(result);
            result.put("classList", txTmGeneral.getTxTmClassList() == null ? "" : txTmGeneral.toJsonClass());
  
            writeJsonResponse(response, result);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @PostMapping(value = REQUEST_MAPPING_SELESAI_PERMOHONAN_MADRID_OO)
    public void doSelesaiPermohonan(Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        String noPermohonan = request.getParameter("noPermohonan");

        if (isAjaxRequest(request)) {
            TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(noPermohonan);
            //List<TxTmDoc> txtmDocList = doclampiranService.getAllDocByApplicationId("txTmGeneral.id", txTmGeneral == null? "" : txTmGeneral.getId());
            KeyValue msg = new KeyValue();
            msg.setKey("Success");
            msg.setValue("Input Data Selesai");

            if (txTmGeneral.getTxTmOwner().size() == 0) {
                msg.setKey("Error");
                msg.setValue("Tab Pemohon Belum Tersimpan");
            } else if (txTmGeneral.getTxTmBrand() == null) {
                msg.setKey("Error");
                msg.setValue("Tab Merek Belum Tersimpan");
            } else if (txTmGeneral.getTxTmClassList().size() == 0) {
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
//                if(check) {
//                    boolean cek = true;
//                    for (TxTmDoc txTmDoc : txTmDocList) {
//                        if(docType.getmDocType().getId().equals(txTmDoc.getmDocType().getId())) {
//                            cek = false;
//                        }
//                    }
//                   if(cek) {
//                        msg.setKey("Error");
//                        msg.setValue("Dokumen " + docType.getmDocType().getName() + " Belum Dilampirkan");
//                        break;
//                    }
//                }
            }

            String docIdTempTTD = "";
            String docIdTempTTDK = "";
            String docFileNameTTD = "";
            String docFileNameTTDK = "";

            String docIdTempDokPendukung = "";

            for (TxTmDoc txTmDoc : txTmDocList) {
                if(txTmDoc.getmDocType().getId().equalsIgnoreCase("TTD")) {
                    docIdTempTTD = txTmDoc.getmDocType().getId();
                    docFileNameTTD = txTmDoc.getFileName();
                }

            }

            try {
            	if(docIdTempTTD.isEmpty()) {
            		msg.setKey("Error");
                    msg.setValue("Dokumen Tanda Tangan Pemohon dan/atau Kuasa Belum Dilampirkan");
               } else if(docFileNameTTD.equalsIgnoreCase(txTmGeneral.getTxTmBrand().getFileName()))
                {
                    msg.setKey("Error");
                    msg.setValue("File Gambar Label Merek Yang Anda Upload Terindikasi Sama Dengan File Gambar Tanda Tangan Kuasa/Pemohon, "
                            + "Mohon Periksa Kembali. Pastikan Gambar, Nama file Label Merek dengan Tanda Tangan Pemohon/Kuasa berbeda "
                            + "dan Kemudian Klik Simpan dan Lanjutkan !!");
                }
            } catch (Exception e) {
            }

            if (msg.getKey().equalsIgnoreCase("Success")) {
                txTmGeneral.setFilingDate(new Timestamp(System.currentTimeMillis()));
                trademarkService.updateGeneralOnline(txTmGeneral);
/*
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
                            *//**---GET FIELD ADDRESS, PHONE, EMAIL FROM TX_TM_REPRS **//*
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
//                            if(txTmDoc.getmDocType().getId().equalsIgnoreCase("TTDK")) {
//                                docIdTTDKTemp = txTmDoc.getFileNameTemp();
//                                txTmDocDateTTDK = DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
//                            } else if (txTmDoc.getmDocType().getId().equalsIgnoreCase("TTDP")) {
                                docIdTTDPTemp = txTmDoc.getFileNameTemp();
                                txTmDocDateTTDP = DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
//                                tandaTanganDigitalSuratPernyataan = txTmDoc.getFileNameTemp();
//                            }

                            dataCetakMerek = new CetakMerek(8, "Dokumen Lampiran", "Attachment");
                            dataCetakMerek.setDocName(txTmDoc.getmDocType().getName());
                            dataListCetakMerek.add(dataCetakMerek);
                        }
                    }
//                    if(txTmReprsList.size() > 0 && !docIdTTDKTemp.isEmpty()) {
//                        tandaTanganDigital = docIdTTDKTemp;
//                        pathFolderTandaTanganDigital = txTmDocDateTTDK;
//                    } else {
                        tandaTanganDigital = docIdTTDPTemp;
                        pathFolderTandaTanganDigital = txTmDocDateTTDP;
//                    }

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
                    outputStreamSuratPernyataan.close();*/

                    /*CETAK TANDA TERIMA MADRID OO*/

                    List<TxTmDoc> txtmDocList = doclampiranService.getAllDocByApplicationId("txTmGeneral.id", txTmGeneral.getId());
                    int f26 = 0;
                    String f27 = "";
                    for(TxTmDoc doc: txtmDocList){
                        if(doc.getmDocType().getId().equalsIgnoreCase("F26")){
                            f26 = f26+1;
                        }
                        if(doc.getmDocType().getId().equalsIgnoreCase("F27")){
                            f27 = "F27";
                        }
                    }

                    try {
                        Map<String, Object> parameters = new HashMap<String, Object>();
                        parameters.put("SUBREPORT_DIR", "report/madrid/");
                        parameters.put("applicationId", txTmGeneral.getId());
                        parameters.put("approve", txTmGeneral.getmStatus().getId().equals(StatusEnum.IPT_PENGAJUAN_MADRID.toString())?"1":"0");

                        String brandLogo = txTmGeneral.getTxTmBrand() !=null ?  txTmGeneral.getTxTmBrand().getId()+ ".jpg":null;
                        String pathFolderLogo = txTmGeneral.getTxTmBrand() !=null ? DateUtil.formatDate(txTmGeneral.getTxTmBrand().getCreatedDate(), "yyyy/MM/dd/"):null;
                        String pathBrandLogo = brandLogo == null ? "" : uploadFileBrandPath + pathFolderLogo + brandLogo;

                        if (!pathBrandLogo.equals("")) {
                        	if(Files.exists(Paths.get(pathBrandLogo)))  {
                                parameters.put("brandLogo", pathBrandLogo);
                            }
                        }                        

                        parameters.put("draftLogo", pathImage+"draft.png");
                        parameters.put("lookup", lookupService.selectAllbyGroup("FormMadridMM2").get(0).getName());
                        parameters.put("mm17", String.valueOf(f26));
                        parameters.put("mm18", f27);

                        parameters.put("priorList", txTmGeneral.getTxTmPriorList().size());
                        List<TxTmClassLimitation> limitations = txTmClassLimitationCustomRepository.selectAllClassByGeneralId(txTmGeneral.getId());
                        int listContract = 0;
                        int contractContinues = 0;
                        long descLim = 0;
                        for(int i=1;i<=limitations.size();i++){
                            TxTmClassLimitation tmClassLimitation = limitations.get(i-1);
                            if(tmClassLimitation.getmClassDetail().getDescEn()!=null){
                                descLim = descLim + tmClassLimitation.getmClassDetail().getDescEn().length();
                            }else{
                                descLim = descLim + tmClassLimitation.getmClassDetail().getDesc().length();
                            }
                            if(descLim>100){
                                listContract = i;
                                contractContinues = 1;
                                break;
                            }
                        }
                        parameters.put("contractContinues", contractContinues);
                        parameters.put("listContract", listContract);

                        List<TxTmClass> txTmClassList = classService.selectAllClassByIdGeneral(txTmGeneral.getId(), ClassStatusEnum.ACCEPT.name());
                        Map<Integer, String> listTxTmClass = new HashMap<>();
                        if (txTmClassList != null) {
                            for (TxTmClass result : txTmClassList) {
                                int key = result.getmClass().getNo();
                                String descEn = result.getmClassDetail().getDescEn();

                                if (listTxTmClass.containsKey(key)) {
                                    descEn = listTxTmClass.get(key) + "; " + descEn;
                                }

                                listTxTmClass.put(key, descEn);
                            }
                        }

                        int listClass = 0;
                        int classContinues = 0;
                        long desc = 0;
                        for(Map.Entry<Integer, String> entry : listTxTmClass.entrySet()){
                            desc = desc+entry.getValue().length();
                            if(desc>800 || listClass>5){
                                classContinues = 1;
                                break;
                            }else{
                                listClass = listClass+1;
                            }
                        }
                        parameters.put("classContinues", classContinues);
                        parameters.put("listClass", listClass);

                        int regCount =  0;
                        List<TxTmReference> txTmReferenceList = txTmReferenceRepository.getByTxTmGeneralId(txTmGeneral.getId());
                        regCount = txTmReferenceList.size();
                        parameters.put("regCount", regCount);

                        int madridCountrys = countryService.findByMadrid(Boolean.TRUE).size();
                        BigDecimal listMadridOneReport = BigDecimal.valueOf(madridCountrys).divide(BigDecimal.valueOf(4), BigDecimal.ROUND_UP);
                        if(listMadridOneReport.intValue()>4){
                            for(int a=0; a<4; a++){
                                parameters.put("start"+(a+1),(a*listMadridOneReport.intValue())+1);
                                parameters.put("end"+(a+1),(a+1)*listMadridOneReport.intValue());
                            }
                        }else{
                            parameters.put("start1",1);
                            parameters.put("start2",2);
                            parameters.put("start3",3);
                            parameters.put("start4",4);
                            parameters.put("end1",1);
                            parameters.put("end2",2);
                            parameters.put("end3",3);
                            parameters.put("end4",4);
                        }

                        for(TxTmDoc txTmDoc : txtmDocList){
                            if(txTmDoc.getmDocType().getId().equalsIgnoreCase("TTD")){
                                String pathTtdp = DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
                                String fileTtdp = txTmDoc.getFileNameTemp();
                                parameters.put("ttdp",uploadFileImageTandaTangan+pathTtdp+fileTtdp);
                                break;
                            }
                        }

                        if(txTmGeneral.getmStatus().getId().equals(StatusEnum.IPT_PENGAJUAN_MADRID.toString()) && txTmGeneral.getApprovedBy()!=null){
                            MUser mUser = txTmGeneral.getApprovedBy();
                            parameters.put("ttEmployee",uploadFilePathSignature+mUser.getId()+"."+mUser.getUsername()+".jpg");
                            parameters.put("logoKI", uploadFilePathSignature+"signimage.jpg");
                        }

                        if(txTmGeneral.getTxTmMadridFee() !=null && txTmGeneral.getTxTmMadridFee().getTxTmMadridFeeDetails()!=null){
                            List<TxTmMadridFeeDetail> feeDetails = txTmGeneral.getTxTmMadridFee().getTxTmMadridFeeDetails();
                            if(feeDetails.size()>20){
                                parameters.put("feeContinues", 1);
                                parameters.put("startFees", 1);
                                parameters.put("endFees", 10);
                                parameters.put("startFees2", 11);
                                parameters.put("endFees2", 20);
                            }else{
                                parameters.put("feeContinues", 0);
                                BigDecimal feeOneReport = BigDecimal.valueOf(feeDetails.size()).divide(BigDecimal.valueOf(2), BigDecimal.ROUND_UP);
                                parameters.put("startFees", 1);
                                parameters.put("endFees", feeOneReport.intValue());
                                parameters.put("startFees2", feeOneReport.intValue()+1);
                                parameters.put("endFees2", feeDetails.size());
                            }
                        }else{
                            parameters.put("feeContinues", 0);
                            parameters.put("startFees", 1);
                            parameters.put("endFees", 1);
                            parameters.put("startFees2", 1);
                            parameters.put("endFees2", 1);
                        }

                        final InputStream reportInputStream = getClass().getResourceAsStream("/report/madrid/CetakMadridOO.jrxml");
                        final JasperDesign jasperDesign = JRXmlLoader.load(reportInputStream);
                        JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
                        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, madridOoService.getConnection());

                        ByteArrayOutputStream output = new ByteArrayOutputStream();

                        JRPdfExporter exporter = new JRPdfExporter();
                        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(output));
                        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                        exporter.exportReport();

                        byte[] result = new byte[0];

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
                        } catch (BadPdfFormatException e) {

                        } catch (DocumentException e) {

                        }
                        String folderDoc = downloadFileDocMadridOOCetakPath + DateUtil.formatDate(txTmGeneral.getCreatedDate(), "yyyy/MM/dd/");
                        String filenameDoc = "CetakMadrid-" + txTmGeneral.getId() + ".pdf";

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
                        response.setHeader("Content-disposition", "attachment; filename=CetakMadrid.pdf");
                        InputStream is = new ByteArrayInputStream(result);
                        this.signPdf(is, outputStream);
                        outputStream.close(); 

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

    @RequestMapping(value = REQUEST_MAPPING_MADRID_OO_PERMOHONAN_CETAK_DRAFT, method = {RequestMethod.GET})
    @ResponseBody
    public String doCetakMadridDraft(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        String applicationNo = request.getParameter("no");
        TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(applicationNo);

        List<TxTmDoc> txtmDocList = doclampiranService.getAllDocByApplicationId("txTmGeneral.id", txTmGeneral.getId());
        int f26 = 0;
        String f27 = "";
        for(TxTmDoc doc: txtmDocList){
            if(doc.getmDocType().getId().equalsIgnoreCase("F26")){
                f26 = f26+1;
            }
            if(doc.getmDocType().getId().equalsIgnoreCase("F27")){
                f27 = "F27";
            }
        }

        try {
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("SUBREPORT_DIR", "report/madrid/");
            parameters.put("applicationId", txTmGeneral.getId());
            parameters.put("approve", txTmGeneral.getmStatus().getId().equals(StatusEnum.IPT_PENGAJUAN_MADRID.toString())?"1":"0");

            String brandLogo = txTmGeneral.getTxTmBrand() !=null ?  txTmGeneral.getTxTmBrand().getId()+ ".jpg":null;
            String pathFolderLogo = txTmGeneral.getTxTmBrand() !=null ? DateUtil.formatDate(txTmGeneral.getTxTmBrand().getCreatedDate(), "yyyy/MM/dd/"):null;
            String pathBrandLogo = brandLogo == null ? "" : uploadFileBrandPath + pathFolderLogo + brandLogo;

            if (!pathBrandLogo.equals("")) {
            	if(Files.exists(Paths.get(pathBrandLogo)))  {
                    parameters.put("brandLogo", pathBrandLogo);
                }
            }            

            parameters.put("draftLogo", pathImage+"draft.png");
            parameters.put("lookup", lookupService.selectAllbyGroup("FormMadridMM2").get(0).getName());
            parameters.put("mm17", String.valueOf(f26));
            parameters.put("mm18", f27);

            parameters.put("priorList", txTmGeneral.getTxTmPriorList().size());
            List<TxTmClassLimitation> limitations = txTmClassLimitationCustomRepository.selectAllClassByGeneralId(txTmGeneral.getId());
            int listContract = 0;
            int contractContinues = 0;
            long descLim = 0;
            for(int i=1;i<=limitations.size();i++){
                TxTmClassLimitation tmClassLimitation = limitations.get(i-1);
                if(tmClassLimitation.getmClassDetail().getDescEn()!=null){
                    descLim = descLim + tmClassLimitation.getmClassDetail().getDescEn().length();
                }else{
                    descLim = descLim + tmClassLimitation.getmClassDetail().getDesc().length();
                }
                if(descLim>100){
                    listContract = i;
                    contractContinues = 1;
                    break;
                }
            }
            parameters.put("contractContinues", contractContinues);
            parameters.put("listContract", listContract);

            List<TxTmClass> txTmClassList = classService.selectAllClassByIdGeneral(txTmGeneral.getId(), ClassStatusEnum.ACCEPT.name());
            Map<Integer, String> listTxTmClass = new HashMap<>();
            if (txTmClassList != null) {
                for (TxTmClass result : txTmClassList) {
                    int key = result.getmClass().getNo();
                    String descEn = result.getmClassDetail().getDescEn();

                    if (listTxTmClass.containsKey(key)) {
                        descEn = listTxTmClass.get(key) + "; " + descEn;
                    }

                    listTxTmClass.put(key, descEn);
                }
            }

            int listClass = 0;
            int classContinues = 0;
            long desc = 0;
            for(Map.Entry<Integer, String> entry : listTxTmClass.entrySet()){
                desc = desc+entry.getValue().length();
                if(desc>800 || listClass>5){
                    classContinues = 1;
                    break;
                }else{
                    listClass = listClass+1;
                }
            }
            parameters.put("classContinues", classContinues);
            parameters.put("listClass", listClass);

            int regCount =  0;
            List<TxTmReference> txTmReferenceList = txTmReferenceRepository.getByTxTmGeneralId(txTmGeneral.getId());
            regCount = txTmReferenceList.size();
            parameters.put("regCount", regCount);

            int madridCountrys = countryService.findByMadrid(Boolean.TRUE).size();
            BigDecimal listMadridOneReport = BigDecimal.valueOf(madridCountrys).divide(BigDecimal.valueOf(4), BigDecimal.ROUND_UP);
            if(listMadridOneReport.intValue()>4){
                for(int a=0; a<4; a++){
                    parameters.put("start"+(a+1),(a*listMadridOneReport.intValue())+1);
                    parameters.put("end"+(a+1),(a+1)*listMadridOneReport.intValue());
                }
            }else{
                parameters.put("start1",1);
                parameters.put("start2",2);
                parameters.put("start3",3);
                parameters.put("start4",4);
                parameters.put("end1",1);
                parameters.put("end2",2);
                parameters.put("end3",3);
                parameters.put("end4",4);
            }

            for(TxTmDoc txTmDoc : txtmDocList){
                if(txTmDoc.getmDocType().getId().equalsIgnoreCase("TTD")){
                    String pathTtdp = DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
                    String fileTtdp = txTmDoc.getFileNameTemp();
                    parameters.put("ttdp",uploadFileImageTandaTangan+pathTtdp+fileTtdp);
                    break;
                }
            }

            if(txTmGeneral.getmStatus().getId().equals(StatusEnum.IPT_PENGAJUAN_MADRID.toString()) && txTmGeneral.getApprovedBy()!=null){
                MUser mUser = txTmGeneral.getApprovedBy();
                parameters.put("ttEmployee",uploadFilePathSignature+mUser.getId()+"."+mUser.getUsername()+".jpg");
                parameters.put("logoKI", uploadFilePathSignature+"signimage.jpg");
            }

            if(txTmGeneral.getTxTmMadridFee() !=null && txTmGeneral.getTxTmMadridFee().getTxTmMadridFeeDetails()!=null){
                List<TxTmMadridFeeDetail> feeDetails = txTmGeneral.getTxTmMadridFee().getTxTmMadridFeeDetails();
                if(feeDetails.size()>20){
                    parameters.put("feeContinues", 1);
                    parameters.put("startFees", 1);
                    parameters.put("endFees", 10);
                    parameters.put("startFees2", 11);
                    parameters.put("endFees2", 20);
                }else{
                    parameters.put("feeContinues", 0);
                    BigDecimal feeOneReport = BigDecimal.valueOf(feeDetails.size()).divide(BigDecimal.valueOf(2), BigDecimal.ROUND_UP);
                    parameters.put("startFees", 1);
                    parameters.put("endFees", feeOneReport.intValue());
                    parameters.put("startFees2", feeOneReport.intValue()+1);
                    parameters.put("endFees2", feeDetails.size());
                }
            }else{
                parameters.put("feeContinues", 0);
                parameters.put("startFees", 1);
                parameters.put("endFees", 1);
                parameters.put("startFees2", 1);
                parameters.put("endFees2", 1);
            }

            final InputStream reportInputStream = getClass().getResourceAsStream("/report/madrid/CetakMadridOO.jrxml");
            final JasperDesign jasperDesign = JRXmlLoader.load(reportInputStream);
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, madridOoService.getConnection());

            ByteArrayOutputStream output = new ByteArrayOutputStream();

            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(output));
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.exportReport();

            byte[] result = new byte[0];

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
            } catch (BadPdfFormatException e) {

            } catch (DocumentException e) {

            }
            String folderDoc = downloadFileDocMadridOOCetakPath + DateUtil.formatDate(txTmGeneral.getCreatedDate(), "yyyy/MM/dd/");
            String filenameDoc = "CetakMadrid-" + txTmGeneral.getId() + ".pdf";

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
            response.setHeader("Content-disposition", "attachment; filename=CetakDraftMadrid-"+applicationNo+".pdf");
            InputStream is = new ByteArrayInputStream(result);
            this.signPdf(is, outputStream);
            outputStream.close();

            try (OutputStream outputF = response.getOutputStream()) {
                Path path = pathFile;
                Files.copy(path, outputF);
                outputF.flush();
            } catch(IOException e) {
            }

        }catch (JRException e) {

        }
        return "";
    }

    @RequestMapping(value = REQUEST_MAPPING_MADRID_OO_PERMOHONAN_CETAK, method = {RequestMethod.GET})
    @ResponseBody
    public String doCetakMadrid(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        String applicationNo = request.getParameter("no");
        TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(applicationNo);

        response.setContentType("application/pdf");
        response.setHeader("Content-disposition", "attachment; filename=CetakMadrid.pdf");

        String folderDoc = downloadFileDocMadridOOCetakPath + DateUtil.formatDate(txTmGeneral.getCreatedDate(), "yyyy/MM/dd/");
        String filenameDoc = "CetakMadrid-" + txTmGeneral.getId() + ".pdf";

        try (OutputStream output = response.getOutputStream()) {
            Path path = Paths.get(folderDoc + filenameDoc);
            Files.copy(path, output);
            output.flush();
        } catch(IOException e) {
        }
        return "";
    }
}
