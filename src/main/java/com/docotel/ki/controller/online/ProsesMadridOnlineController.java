package com.docotel.ki.controller.online;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.enumeration.ClassStatusEnum;
import com.docotel.ki.enumeration.PriorStatusEnum;
import com.docotel.ki.enumeration.StatusEnum;
import com.docotel.ki.model.master.*;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.pojo.*;
import com.docotel.ki.model.master.MBrandType;
import com.docotel.ki.model.master.MCity;
import com.docotel.ki.model.master.MClass;
import com.docotel.ki.model.master.MClassDetail;
import com.docotel.ki.model.master.MCountry;
import com.docotel.ki.model.master.MDocType;
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
import com.docotel.ki.model.transaction.TxPostDoc;
import com.docotel.ki.model.transaction.TxPostReception;
import com.docotel.ki.model.transaction.TxPubsJournal;
import com.docotel.ki.model.transaction.TxReception;
import com.docotel.ki.model.transaction.TxRegistration;
import com.docotel.ki.model.transaction.TxTmBrand;
import com.docotel.ki.model.transaction.TxTmBrandDetail;
import com.docotel.ki.model.transaction.TxTmClass;
import com.docotel.ki.model.transaction.TxTmClassLimitation;
import com.docotel.ki.model.transaction.TxTmCountry;
import com.docotel.ki.model.transaction.TxTmDoc;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.model.transaction.TxTmMadridFee;
import com.docotel.ki.model.transaction.TxTmMadridFeeDetail;
import com.docotel.ki.model.transaction.TxTmOwner;
import com.docotel.ki.model.transaction.TxTmOwnerDetail;
import com.docotel.ki.model.transaction.TxTmPrior;
import com.docotel.ki.model.transaction.TxTmReference;
import com.docotel.ki.model.transaction.TxTmRepresentative;
import com.docotel.ki.pojo.CetakMerek;
import com.docotel.ki.pojo.CetakSuratPernyataan;
import com.docotel.ki.pojo.DataForm1;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.transaction.TxRegistrationCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmGeneralCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmReferenceCustomRepository;
import com.docotel.ki.repository.master.MCountryRepository;
import com.docotel.ki.repository.transaction.TxTmClassLimitationRepository;
import com.docotel.ki.repository.transaction.TxTmClassRepository;
import com.docotel.ki.repository.transaction.TxTmCountryRepository;
import com.docotel.ki.repository.transaction.TxTmMadridFeeRepository;
import com.docotel.ki.repository.transaction.TxTmReferenceRepository;
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
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BadPdfFormatException;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfReader;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class ProsesMadridOnlineController extends BaseController {
	    
    private static final String DIRECTORY_PAGE_ONLINE = "proses-madrid-online/";
    private static final String PAGE_PERMOHONAN_PROSES_MADRID = DIRECTORY_PAGE_ONLINE + "permohonan-proses-madrid";
    private static final String PAGE_TAMBAH_PERMOHONAN_PROSES_MADRID = DIRECTORY_PAGE_ONLINE + "tambah-permohonan-proses-madrid";
    private static final String PAGE_EDIT_PERMOHONAN_PROSES_MADRID = DIRECTORY_PAGE_ONLINE + "edit-permohonan-proses-madrid";
    private static final String PAGE_PRATINJAU_PERMOHONAN_PROSES_MADRID = DIRECTORY_PAGE_ONLINE + "pratinjau-permohonan-proses-madrid";
    private static final String PAGE_EDIT_PRATINJAU_PERMOHONAN_ONLINE = DIRECTORY_PAGE_ONLINE + "edit-pratinjau-permohonan-proses-madrid";
    private static final String PAGE_CALCULATE_CLASS = DIRECTORY_PAGE_ONLINE + "calculate-class";
    private static final String PATH_AJAX_SEARCH_NOMOR_REFERENSI_TRANSFORMASI_REPLACEMENT = "/search-nomor-transformasi-replacement";
    private static final String PATH_AJAX_SEARCH_LIST_PROSES_MADRID = "/cari-permohonan-proses-madrid";
    private static final String PATH_AJAX_SEARCH_LIST_REGISTRATION = "/cari-registration-proses-madrid";
    public static final String PATH_AJAX_LIST_DELETE_DOKUMEN = "/delete-dokumen-proses-madrid";
    private static final String PATH_PERMOHONAN_PROSES_MADRID = "/list-permohonan-proses-madrid";
    private static final String PATH_DOKUMEN_PROSES_MADRID = "/list-dokumen-proses-madrid"; 
    private static final String PATH_SAVE_FORM_TRANS = "/save-reference-transformasi"; 
    private static final String PATH_SAVE_FORM_TRANS_REG="/save-reference-reg-transformasi";
    private static final String PATH_SAVE_FORM_REPL = "/save-reference-replacement";
    
    private static final String PATH_PRATINJAU_PERMOHONAN_PROSES_MADRID = "/pratinjau-permohonan-proses-madrid";
    private static final String PATH_EDIT_PRATINJAU_PROSES_MADRID = "/edit-pratinjau-proses-madrid";
    private static final String PATH_EDIT_PRATINJAU_PROSES_MADRID_PEMOHON = PATH_EDIT_PRATINJAU_PROSES_MADRID + "-pemohon";
    private static final String PATH_EDIT_PRATINJAU_PROSES_MADRID_KUASA = PATH_EDIT_PRATINJAU_PROSES_MADRID + "-kuasa";
    private static final String PATH_EDIT_PRATINJAU_PROSES_MADRID_KELAS = PATH_EDIT_PRATINJAU_PROSES_MADRID + "-kelas";
    private static final String PATH_EDIT_PRATINJAU_PROSES_MADRID_DOKUMEN = "/edit-pratinjau-proses-madrid-dokumen";
	private static final String PATH_PROSES_MADRID_CHOOSE_REFERENSI = "/choose-referensi-proses-madrid";
    private static final String PATH_CHECK_CODE_BILLING = "/check-code-billing/proses-madrid";
	private static final String PATH_CETAK_SURAT_PERNYATAAN_PROSES_MADRID = "/cetak-surat-pernyataan-online";
	private static final String PATH_CETAK_MEREK_PROSES_MADRID = "/cetak-proses-madrid";
	private static final String PATH_SAVE_FORM_GENERAL_PROSES_MADRID = "/save-proses-madrid-form-general";
    private static final String PATH_SAVE_FORM_PEMOHON_PROSES_MADRID = "/save-proses-madrid-form-pemohon";
    private static final String PATH_SAVE_FORM_KUASA_PROSES_MADRID = "/save-proses-madrid-form-kuasa";
    private static final String PATH_SAVE_FORM_KELAS_PROSES_MADRID = "/save-proses-madrid-form-kelas";
    private static final String PATH_SAVE_FORM_DOC_PROSES_MADRID = "/save-proses-madrid-form-dokumen";

    private static final String PATH_TAMBAH_PERMOHONAN_PROSES_MADRID = "/tambah-permohonan-proses-madrid";
    private static final String PATH_EDIT_PERMOHONAN_PROSES_MADRID = "/edit-permohonan-proses-madrid";
    private static final String PATH_SELESAI_PERMOHONAN_PROSES_MADRID = "/selesai-permohonan-proses-madrid";

    private static final String REQUEST_MAPPING_PERMOHONAN_PROSES_MADRID = PATH_PERMOHONAN_PROSES_MADRID + "*";
    private static final String REQUEST_MAPPING_TAMBAH_PERMOHONAN_PROSES_MADRID = PATH_TAMBAH_PERMOHONAN_PROSES_MADRID + "*";
	private static final String REQUEST_MAPPING_PROSES_MADRID_CHOOSE_REFERENSI = PATH_PROSES_MADRID_CHOOSE_REFERENSI + "*";;
    private static final String REQUEST_MAPPING_CHECK_CODE_BILLING = PATH_CHECK_CODE_BILLING + "*";
	private static final String REQUEST_MAPPING_EDIT_PERMOHONAN_PROSES_MADRID = PATH_EDIT_PERMOHONAN_PROSES_MADRID + "*";
    private static final String REQUEST_MAPPING_AJAX_SEARCH_NOMOR_REFERENSI_TRANSFORMASI_REPLACEMENT = PATH_AJAX_SEARCH_NOMOR_REFERENSI_TRANSFORMASI_REPLACEMENT + "*";
	private static final String REQUEST_MAPPING_AJAX_SEARCH_LIST_PROSES_MADRID = PATH_AJAX_SEARCH_LIST_PROSES_MADRID + "*";
	private static final String REQUEST_MAPPING_SAVE_FORM_GENERAL_PROSES_MADRID = PATH_SAVE_FORM_GENERAL_PROSES_MADRID + "*";
    private static final String REQUEST_MAPPING_SAVE_FORM_PEMOHON_PROSES_MADRID = PATH_SAVE_FORM_PEMOHON_PROSES_MADRID + "*";
    private static final String REQUEST_MAPPING_SAVE_FORM_KUASA_PROSES_MADRID = PATH_SAVE_FORM_KUASA_PROSES_MADRID + "*";
    private static final String REQUEST_MAPPING_SAVE_FORM_KELAS_PROSES_MADRID = PATH_SAVE_FORM_KELAS_PROSES_MADRID + "*";
    private static final String REQUEST_MAPPING_SAVE_FORM_DOC_PROSES_MADRID = PATH_SAVE_FORM_DOC_PROSES_MADRID + "*";
	private static final String REQUEST_MAPPING_SELESAI_PERMOHONAN_PROSES_MADRID = PATH_SELESAI_PERMOHONAN_PROSES_MADRID + "*";
    private static final String REQUEST_MAPPING_PRATINJAU_PERMOHONAN_PROSES_MADRID = PATH_PRATINJAU_PERMOHONAN_PROSES_MADRID + "*";
    private static final String REQUEST_MAPPING_EDIT_PRATINJAU_PROSES_MADRID_PEMOHON = PATH_EDIT_PRATINJAU_PROSES_MADRID_PEMOHON + "*";
    private static final String REQUEST_MAPPING_EDIT_PRATINJAU_PROSES_MADRID_KUASA = PATH_EDIT_PRATINJAU_PROSES_MADRID_KUASA + "*";
    private static final String REQUEST_MAPPING_EDIT_PRATINJAU_PROSES_MADRID_KELAS = PATH_EDIT_PRATINJAU_PROSES_MADRID_KELAS + "*";
    private static final String REQUEST_MAPPING_EDIT_PRATINJAU_PROSES_MADRID_DOKUMEN = PATH_EDIT_PRATINJAU_PROSES_MADRID_DOKUMEN + "*";
    private static final String REQUEST_MAPPING_PROSES_MADRID_PERMOHONAN_CETAK = PATH_CETAK_MEREK_PROSES_MADRID + "*";
    private static final String REQUEST_MAPPING_SAVE_FORM_TRANS = PATH_SAVE_FORM_TRANS + "*";
    private static final String REQUEST_MAPPING_SAVE_FORM_REPL=PATH_SAVE_FORM_REPL+ "*";
    private static final String REQUEST_MAPPING_SAVE_FORM_TRANS_REG=PATH_SAVE_FORM_TRANS_REG+"*";
    
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
    private TxTmClassLimitationRepository txTmClassLimitationRepository;
    @Autowired
    TxTmGeneralCustomRepository txTmGeneralCustomRepository;
    @Autowired
    private TxTmReferenceRepository txTmReferenceRepository;
    @Autowired
    private TxRegistrationCustomRepository txRegistrationCustomRepository;
    @Autowired
    private MadridService madridService;
    @Autowired
    private TxTmClassRepository txTmClassRepository;

    @Value("${simpaki.api.create.billing}")
    private String urlCreateBilling;
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
    @Value("${upload.file.path.signature:}")
    private String uploadFileImageTandaTangan;
    @Value("${download.output.madridOO.cetakmerek.file.path}")
    private String downloadFileDocMadridOOCetakPath;


	@ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "prosesMadrid");
        model.addAttribute("subMenu", "prosesMadrid");
    }
	
    private void doInitiatePermohonan(final Model model, final HttpServletRequest request) {
        if (request.getRequestURI().contains(PATH_PERMOHONAN_PROSES_MADRID)) { 
        	
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
        if (request.getRequestURI().contains(PATH_TAMBAH_PERMOHONAN_PROSES_MADRID)
                || request.getRequestURI().contains(PATH_EDIT_PERMOHONAN_PROSES_MADRID)
                || request.getRequestURI().contains(PATH_PRATINJAU_PERMOHONAN_PROSES_MADRID)
                || request.getRequestURI().contains(PATH_EDIT_PRATINJAU_PROSES_MADRID)) {
        	
        	String form = "";
            if (request.getRequestURI().contains(PATH_EDIT_PRATINJAU_PROSES_MADRID_PEMOHON)) {
                form = "pemohon";
            } else if (request.getRequestURI().contains(PATH_EDIT_PRATINJAU_PROSES_MADRID_KUASA)) {
                form = "kuasa";
            } else if (request.getRequestURI().contains(PATH_EDIT_PRATINJAU_PROSES_MADRID_KELAS)) {
                form = "kelas";
            } else if (request.getRequestURI().contains(PATH_EDIT_PRATINJAU_PROSES_MADRID_DOKUMEN)) {
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
            model.addAttribute("isNew", request.getRequestURI().contains(PATH_TAMBAH_PERMOHONAN_PROSES_MADRID));
            model.addAttribute("isEditPermohonan", request.getRequestURI().contains(PATH_EDIT_PERMOHONAN_PROSES_MADRID));
            model.addAttribute("isNewOrEdit", request.getRequestURI().contains(PATH_TAMBAH_PERMOHONAN_PROSES_MADRID) ||request.getRequestURI().contains(PATH_EDIT_PERMOHONAN_PROSES_MADRID));
            model.addAttribute("isEdit", request.getRequestURI().contains(PATH_EDIT_PRATINJAU_PROSES_MADRID));
            model.addAttribute("isReprs", isRepresentative());
            model.addAttribute("applicationDate", new Timestamp(System.currentTimeMillis()));
            
            List<MLookup> disclaimerMerekList = lookupService.selectAllbyGroup("DisclaimerMerek");
            model.addAttribute("disclaimerMerek", disclaimerMerekList);

            if (form.equals("pemohon") || form.equals("")) { 
            	
            	List<MProvince> mProvinceList = provinceService.findByStatusFlagTrue();
                model.addAttribute("listProvince", mProvinceList);
                
                List<MCountry> mCountryList = countryService.findByStatusFlagTrue();
                model.addAttribute("listCountry", mCountryList);

                List<MCity> mCityList = cityService.findByStatusFlagTrue();
                model.addAttribute("listCity", mCityList);
                
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

                if(request.getRequestURI().contains(PATH_EDIT_PERMOHONAN_PROSES_MADRID)) { 
                    model.addAttribute("listProvince", mProvinceList);
 
                    model.addAttribute("listCity", mCityList);
                } 
                else if (request.getRequestURI().contains(PATH_TAMBAH_PERMOHONAN_PROSES_MADRID)) { 
                	 
                    model.addAttribute("listProvince", mProvinceList);
                     
                    model.addAttribute("listCountry", mCountryList);
 
                    model.addAttribute("listCity", mCityList);
                     
                }
                
              List<MLookup> mRightList = lookupService.selectAllbyGroup("HakPengajuan");
              model.addAttribute("listRight", mRightList);

              String referenceAppNo = "";
              String referenceRegNo1 = "";
              List<String> referenceRegNo2 = new ArrayList<String>();
              List<TxTmReference> referenceList = txTmReferenceCustomRepository.selectByTxTmGeneral(txTmGeneral.getId());
              System.out.println("referenceList count: " + referenceList.size());
                if(referenceList.size()>0) {
                    referenceAppNo = referenceList.get(0).getRefApplicationId().getApplicationNo();
                    System.out.println("reference   found: " + referenceAppNo);
                }
                model.addAttribute("referenceAppNo", referenceAppNo);
              List<String> referenceData = new ArrayList<>();
              String transformasiReferenceData = "";
              for(TxTmReference ttr :referenceList){
                  // have to parse own
                  String noPermohonan = "";
                  String tglPenerimaan = "";
                  String namaMerek = "";
                  String regNo = "";
                  String regDate = "";
                  if (ttr.getRefApplicationId_2()!=null){
                      noPermohonan = ttr.getRefApplicationId_2().getId();
                      if(ttr.getRefApplicationId_2().getFilingDate()!=null){
                          SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                          tglPenerimaan = dateFormat.format(ttr.getRefApplicationId_2().getFilingDate());
                      }
                      namaMerek = ttr.getRefApplicationId_2().getTxTmBrand().getName();
                      TxRegistration tr = txRegistrationCustomRepository.selectOne("txTmGeneral.id", ttr.getRefApplicationId_2().getId(), true);
                      if(tr!=null){
                          regNo = tr.getNo();
                      }
                      if(ttr.getRefApplicationId_2().getCreatedDate()!=null) {
                          SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                          regDate = dateFormat.format(ttr.getRefApplicationId_2().getCreatedDate());
                      }
                  }
                  referenceData.add(noPermohonan+";"+namaMerek+";"+regNo+";"+tglPenerimaan+";"+regDate);

                  if(transformasiReferenceData.equalsIgnoreCase("")){
                      noPermohonan = ttr.getRefApplicationId().getId();
                      if(ttr.getRefApplicationId().getFilingDate()!=null){
                          SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                          tglPenerimaan = dateFormat.format(ttr.getRefApplicationId().getFilingDate());
                      }
                      namaMerek = ttr.getRefApplicationId().getTxTmBrand().getName();
                      TxRegistration tr = txRegistrationCustomRepository.selectOne("txTmGeneral.id", ttr.getRefApplicationId().getId(), true);
                      if(tr!=null) {
                          regNo = tr.getNo();
                      }
                      if(ttr.getRefApplicationId().getCreatedDate()!=null) {
                          SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                          regDate = dateFormat.format(ttr.getRefApplicationId().getCreatedDate());
                      }
                      transformasiReferenceData = noPermohonan+";"+namaMerek+";"+regNo+";"+tglPenerimaan+";"+regDate;
                  }
              }
              model.addAttribute("referenceList_replacement", referenceData);
              model.addAttribute("referenceList_transformasi", transformasiReferenceData);
              if(referenceList.size()>0){
                  referenceAppNo = referenceList.get(0).getRefApplicationId().getApplicationNo();
                  if(referenceList.get(0).getRefApplicationId().getTxRegistration()!=null) {
                      referenceRegNo1 = referenceList.get(0).getRefApplicationId().getTxRegistration().getNo();
                  }
                  for(TxTmReference ref : referenceList){
                      if(ref.getRefApplicationId_2()==null) {
                          continue;
                      }
                      if(ref.getRefApplicationId_2().getTxRegistration()!=null) {
                          referenceRegNo2.add(ref.getRefApplicationId_2().getTxRegistration().getNo());
                      }
                  }
              }
              model.addAttribute("referenceRegNo1", referenceRegNo1);
              model.addAttribute("referenceRegNo2", referenceRegNo2);
            }
            if(request.getRequestURI().contains(PATH_TAMBAH_PERMOHONAN_PROSES_MADRID) 
            		|| request.getRequestURI().contains(PATH_EDIT_PERMOHONAN_PROSES_MADRID)
            		|| request.getRequestURI().contains(PATH_EDIT_PRATINJAU_PROSES_MADRID)) {
            	 
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
            
            if (form.equals("pemohon") || form.equals("prioritas") || form.equals("") && request.getRequestURI().contains(PATH_EDIT_PERMOHONAN_PROSES_MADRID)) {
              
                Boolean ismadrid = true ;
                List<MCountry> MadridCountry = countryService.findByMadrid(ismadrid);
                model.addAttribute("listCountry", MadridCountry);
                
            } 
            else if (form.equals("pemohon") || form.equals("prioritas") || form.equals("") && request.getRequestURI().contains(PAGE_TAMBAH_PERMOHONAN_PROSES_MADRID)) {
 
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
 

                model.addAttribute("txTmBrand", txTmBrand); 

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
                Collections.sort(listMClass, (o1, o2) -> new Integer(o1.getNo()).compareTo(new Integer(o2.getNo())));
                model.addAttribute("listMClass", listMClass);

                String classStatusEnum = "\"acceptValue\":\"" + ClassStatusEnum.ACCEPT.name() + "\",";
                classStatusEnum += "\"acceptLabel\":\"" + ClassStatusEnum.ACCEPT.getLabel() + "\",";
                classStatusEnum += "\"rejectValue\":\"" + ClassStatusEnum.REJECT.name() + "\",";
                classStatusEnum += "\"rejectLabel\":\"" + ClassStatusEnum.REJECT.getLabel() + "\"";

                model.addAttribute("classStatusEnum", "{" + classStatusEnum + "}");
            }
            if (form.equals("dokumen") || form.equals("")) { 
            	List<KeyValue> searchCriteria = new ArrayList<>();
            	TxReception txReception = txTmGeneral.getTxReception();
            	List<String> docTypeId = new ArrayList<String>();
            	List<String> fileTypeId = new ArrayList<String>();
            	docTypeId.add("F05");
            	docTypeId.add("F15");            	
            	if (txReception != null) {
            		System.out.println("File Type : "+txReception.getmFileType().getId());
            		fileTypeId.add(txReception.getmFileType().getId());
                	searchCriteria.add(new KeyValue("mFileType.id", fileTypeId, true));
            	}            	
                searchCriteria.add(new KeyValue("mDocType.statusFlag", true, true));
                searchCriteria.add(new KeyValue("mDocType.id", docTypeId, true));                
                if (!(boolean) model.asMap().get("isNew")) {
                    searchCriteria.add(new KeyValue("mFileType.id", txReception.getmFileType().getId(), true)); 
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
                
		        List<MFileType> fileTypeList = fileService.findByMenu("MADRID_TRP");
		        model.addAttribute("fileTypeList", fileTypeList);
		  
		        List<MFileTypeDetail> fileTypeDetailList = fileService.findByCode("MTR");
		        model.addAttribute("fileTypeDetailList", fileTypeDetailList);
		  
		        MFileSequence fileSequenceList = fileService.getFileSequenceByCode("ID");
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
            
            TxTmMadridFee txTmMadridFee = txTmGeneral.getTxTmMadridFee();
            if (txTmMadridFee == null) {
            	txTmMadridFee = new TxTmMadridFee();
            }
            
            List<TxTmMadridFeeDetail> txTmMadridFeeDetails = txTmMadridFee.getTxTmMadridFeeDetails();
            if (txTmMadridFeeDetails == null) {
            	txTmMadridFeeDetails = new ArrayList<>();
            }
            
            model.addAttribute("txTmMadridFee", txTmMadridFee);
            model.addAttribute("txTmMadridFeeDetails", txTmMadridFeeDetails);
            
            if (request.getRequestURI().contains(PATH_PRATINJAU_PERMOHONAN_PROSES_MADRID)) {
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
    
    @RequestMapping(path = REQUEST_MAPPING_PERMOHONAN_PROSES_MADRID)
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
        return PAGE_PERMOHONAN_PROSES_MADRID;
    }

    @RequestMapping(path = REQUEST_MAPPING_TAMBAH_PERMOHONAN_PROSES_MADRID, method = {RequestMethod.GET})
    public String doShowPageTambahPermohonan(Model model, final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
 
        doInitiatePermohonan(model, request);
    
          List<MFileType> fileTypeList = fileService.findByMenu("MADRID_TRP");
          model.addAttribute("fileTypeList", fileTypeList);
 
          List<MFileTypeDetail> fileTypeDetailList = fileService.findByCode("MTR");
          model.addAttribute("fileTypeDetailList", fileTypeDetailList);
         
        return PAGE_TAMBAH_PERMOHONAN_PROSES_MADRID;
    }
    
    @RequestMapping(path = REQUEST_MAPPING_PRATINJAU_PERMOHONAN_PROSES_MADRID, method = {RequestMethod.GET})
    public String doShowPagePratinjauPermohonan(@RequestParam(value = "error", required = false) String error, @RequestParam(value = "no", required = true) String no, Model model, final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
        MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByeFilingNo(no);

        List<TxTmReference> referenceList = txTmReferenceCustomRepository.selectByTxTmGeneral(txTmGeneral.getId());
        System.out.println("referenceList count: " + referenceList.size());
        List<String> referenceData = new ArrayList<>();
        String transformasiReferenceData = "";
        for(TxTmReference ttr :referenceList){
            // have to parse own
            String noPermohonan = "";
            String tglPenerimaan = "";
            String namaMerek = "";
            String regNo = "";
            String regDate = "";
            if (ttr.getRefApplicationId_2()!=null){
                noPermohonan = ttr.getRefApplicationId_2().getId();
                if(ttr.getRefApplicationId_2().getFilingDate()!=null){
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                    tglPenerimaan = dateFormat.format(ttr.getRefApplicationId_2().getFilingDate());
                }
                namaMerek = ttr.getRefApplicationId_2().getTxTmBrand().getName();
                TxRegistration tr = txRegistrationCustomRepository.selectOne("txTmGeneral.id", ttr.getRefApplicationId_2().getId(), true);
                if(tr!=null){
                    regNo = tr.getNo();
                }
                if(ttr.getRefApplicationId_2().getCreatedDate()!=null) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                    regDate = dateFormat.format(ttr.getRefApplicationId_2().getCreatedDate());
                }
            }
            referenceData.add(noPermohonan+";"+namaMerek+";"+regNo+";"+tglPenerimaan+";"+regDate);
            if(transformasiReferenceData.equalsIgnoreCase("")){
                noPermohonan = ttr.getRefApplicationId().getId();
                if(ttr.getRefApplicationId().getFilingDate()!=null){
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                    tglPenerimaan = dateFormat.format(ttr.getRefApplicationId().getFilingDate());
                }
                namaMerek = ttr.getRefApplicationId().getTxTmBrand().getName();
                TxRegistration tr = txRegistrationCustomRepository.selectOne("txTmGeneral.id", ttr.getRefApplicationId().getId(), true);
                if(tr!=null) {
                    regNo = tr.getNo();
                }
                if(ttr.getRefApplicationId().getCreatedDate()!=null) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                    regDate = dateFormat.format(ttr.getRefApplicationId().getCreatedDate());
                }
                transformasiReferenceData = noPermohonan+";"+namaMerek+";"+regNo+";"+tglPenerimaan+";"+regDate;
            }
        }
        model.addAttribute("referenceList_replacement", referenceData);
        model.addAttribute("referenceList_transformasi", transformasiReferenceData);

        if (mUser.getUsername() != "super") {
            List<KeyValue> criteriaList = new ArrayList<>();
            criteriaList.add(new KeyValue("txReception.eFilingNo", no, true));
            criteriaList.add(new KeyValue("createdBy", mUser, true));
            txTmGeneral = trademarkService.selectOneGeneralByeFilingNoByUser(criteriaList);

            if (txTmGeneral == null) {
                return "redirect:" + PATH_AFTER_LOGIN + PATH_PERMOHONAN_PROSES_MADRID + "?error=" +"Halaman tidak ditemukan";
            }
        }

        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("errorMessage", error);
        }

        if (txTmGeneral.getmStatus().getId().equalsIgnoreCase(StatusEnum.IPT_DRAFT.name())) {
            return "redirect:" + PATH_AFTER_LOGIN + PATH_EDIT_PERMOHONAN_PROSES_MADRID + "?no=" + no;
        } else {
            doInitiatePermohonan(model, request);
            return PAGE_PRATINJAU_PERMOHONAN_PROSES_MADRID;
        }
    }

    @RequestMapping(path = {REQUEST_MAPPING_EDIT_PRATINJAU_PROSES_MADRID_PEMOHON, REQUEST_MAPPING_EDIT_PRATINJAU_PROSES_MADRID_KUASA,
            REQUEST_MAPPING_EDIT_PRATINJAU_PROSES_MADRID_KELAS, REQUEST_MAPPING_EDIT_PRATINJAU_PROSES_MADRID_DOKUMEN},
            method = {RequestMethod.GET})
    public String doShowPageEditPratinjauPermohonan(@RequestParam(value = "no", required = true) String no, Model model, final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
//    	if(isNotRepresentative()) {
//    		return REDIRECT_PRATINJAU_PERMOHONAN_ONLINE + "?error=Hanya user konsultan yang dapat mengakses menu ini. Silakan hubungi Administrator.&no=" + no;
//    	}
        doInitiatePermohonan(model, request);
        return PAGE_EDIT_PRATINJAU_PERMOHONAN_ONLINE;
    }

    @RequestMapping(path = REQUEST_MAPPING_AJAX_SEARCH_NOMOR_REFERENSI_TRANSFORMASI_REPLACEMENT, method = {RequestMethod.POST})
    public void doSearchDataTablesReferensiTransformasiReplacement(final Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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

            if (searchByArr != null) {
                searchCriteria = new ArrayList<>();
                for (int i = 0; i < searchByArr.length; i++) {
                    String searchBy = searchByArr[i];
                    String value = null;
                    try {
                        value = keywordArr[i];
                    } catch (ArrayIndexOutOfBoundsException e) {
                    }
                    System.out.println("dump query " + searchBy + " " + value);
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
            GenericSearchWrapper<TxTmGeneral> searchResult = new GenericSearchWrapper<TxTmGeneral>();
            searchResult = permohonanOnlineService.searchRefReception(searchCriteria, orderBy, orderType, offset, limit);
             
            if (searchResult.getCount() > 0) { 
            	
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxTmGeneral result : searchResult.getList()) {
                    String regNo = "";
                    String appID="";
                    String tglPenerimaan = "";
                    String tglPendaftaran = "";
                    String tglPrioritas = "";
                    String namaMerek = "";
                    regNo = "-";
                    tglPendaftaran="-";
                    if (result.getTxRegistration() != null) {
                        regNo = result.getTxRegistration().getNo();
                    }
                    if(result.getCreatedDate()!=null){
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                        String regDateString  = dateFormat.format(result.getCreatedDate());
                        tglPendaftaran = regDateString;
                    }
                    if(result.getTxTmBrand()!=null){
                        namaMerek = result.getTxTmBrand().getName();
                    }
                    if(result.getTxTmPriorList()!=null && result.getTxTmPriorList().size()>0) {
                        tglPrioritas = result.getTxTmPriorList().get(0).getPriorDate().toString();
                    }
                    if (result.getFilingDate() != null){
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                        String dateString  = dateFormat.format(result.getFilingDate());
                        tglPenerimaan = dateString;
                    }
                    appID=result.getApplicationNo();
 
                    no++;
                    data.add(new String[]{
                            "" + no,
                            appID,
                            tglPenerimaan,
                            regNo,
                            tglPendaftaran,
                            tglPrioritas,
                            namaMerek

                            //"<button type='button' class=\"btn-referensi\" paramId=\"" + result.getId() + "\" " +
                            //		" data-dismiss='modal'>Pilih</button>"
                    });
                }
            }
            
            if (searchResult.getCount() < 0) { 
            	
              searchResult = permohonanOnlineService.searchRefMadridTransformasi(searchCriteria, orderBy, orderType, offset, limit);
              if (searchResult.getCount() > 0) {  
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                String regNo = "";
                String tglPenerimaan = "";
                String tglPendaftaran = ""; 

                for (TxTmGeneral result : searchResult.getList()) {
                    regNo = "-"; 

                    if (result.getCreatedDate() != null){
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                        String dateString  = dateFormat.format(result.getCreatedDate());
                        tglPenerimaan = dateString;
                    }
 
                    tglPendaftaran="-";
                    
                    no++;
                    data.add(new String[]{
                            "" + no,
                            result.getApplicationNo(),
                            tglPenerimaan,
                            regNo,
                            tglPendaftaran,

                            //"<button type='button' class=\"btn-referensi\" paramId=\"" + result.getId() + "\" " +
                            //		" data-dismiss='modal'>Pilih</button>"
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
    
    @RequestMapping(path = REQUEST_MAPPING_AJAX_SEARCH_LIST_PROSES_MADRID, method = {RequestMethod.POST})
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
            searchCriteria.add(new KeyValue("txReception.mFileType.menu", "MADRID_TRP", true));
 
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
                    if (result.getmStatus().getId().equalsIgnoreCase(StatusEnum.IPT_DRAFT.name())) {
                        button = "<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT_PERMOHONAN_PROSES_MADRID + "?no=" + result.getTxReception().geteFilingNo()) + "\">Ubah</a>";
						/*
						 * +"<br>" + "<a class=\"btn btn-info btn-xs\" href=\"" +
						 * getPathURLAfterLogin(PATH_CETAK_MEREK_PROSES_MADRID) + "?no=" +
						 * result.getApplicationNo() + "\">Draft Tanda Terima</a>  ";
						 */
                    } else {
                        button = "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin(PATH_CETAK_MEREK_PROSES_MADRID) + "?no=" + result.getApplicationNo() + "\">Tanda Terima</a>  ";
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
                            "<a target=\"_blank\" href=\"" + getPathURLAfterLogin(PATH_PRATINJAU_PERMOHONAN_PROSES_MADRID + "?no=" + result.getTxReception().geteFilingNo()) + "\">" + result.getTxReception().geteFilingNo() + "</a>",
                            result.getTxReception().getApplicationDateTemp(),
                            appNo,
                            fileTypeDesc,
                            fileTypeDetDesc,
                            status,
                            "<div class=\"btn-actions\">" + button  + "</div>"
                    });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }
    
    @RequestMapping(path = REQUEST_MAPPING_EDIT_PERMOHONAN_PROSES_MADRID, method = {RequestMethod.GET})
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
                return "redirect:" + PATH_AFTER_LOGIN + PATH_PERMOHONAN_PROSES_MADRID + "?error=" +"Halaman tidak ditemukan";
            } else if(!txTmGeneral.getmStatus().getId().equals("IPT_DRAFT")) {
            	return "error-404";
            }
        }
        doInitiatePermohonan(model, request);
        return PAGE_EDIT_PERMOHONAN_PROSES_MADRID;
    }
    
    @RequestMapping(path = REQUEST_MAPPING_SAVE_FORM_GENERAL_PROSES_MADRID, method = {RequestMethod.POST})
    @ResponseBody
    public void doSaveFormGeneral(@RequestBody DataForm1 form, Model model,
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
            		String application_no = trademarkService.insertReceptionOnline(form); 
                    writeJsonResponse(response, application_no);
            	}
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                model.addAttribute("errorMessage", "Gagal menambahkan permohonan online tab general");
                writeJsonResponse(response, 200);
            }
        }
    }
    
    @RequestMapping(path = REQUEST_MAPPING_CHECK_CODE_BILLING, method = {RequestMethod.POST})
    @ResponseBody
    public void doCheckCodeBilling(final HttpServletRequest request, final HttpServletResponse response,
                                   final HttpSession session) throws JsonProcessingException,
    IOException, ParseException {
        String statusError = null;
        String bankCode = request.getParameter("bankCode");
        TxReception txReception = permohonanOnlineService.selectOneTxReception("bankCode", bankCode);
        Map<String, String> dataGeneral = new HashMap<>();
         

        if (txReception != null) {
            statusError = "Kode Billing '" + bankCode + "' Sudah Digunakan";
        } else {
            try {
                String result = simpakiService.getQueryBilling(bankCode);
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(result);
                System.out.println("query billing "+result);
                 

                String code = rootNode.get("code").toString().replaceAll("\"", "");
                String message = rootNode.get("message").toString().replaceAll("\"", "");
                if (code.equals("00")) {
                    String data = rootNode.get("data").toString();
                    JsonNode dataNode = mapper.readTree(data);

                    String flagPayment = dataNode.get("flag_pembayaran").toString().replaceAll("\"", "");
                    String flagUsed = dataNode.get("terpakai").toString().replaceAll("\"", "");

                    if (flagPayment.equalsIgnoreCase("BELUM")) {
                        statusError = "Kode Billing '" + bankCode + "' Masih dalam proses verifikasi bank. Mohon tunggu beberapa saat lagi.";
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
                        System.out.println("query billing "+mPnbpFeeCode.toString());
                         

                       if (mPnbpFeeCode == null) {
                            statusError = "Kode Tarif '" + feeCode + "' Tidak Ditemukan";
						} /*
							 * else if (!mPnbpFeeCode.getmFileType().getId().equals("PROSES_MADRID")) {
							 * statusError = "Kode Billing tidak sesuai dengan permohonan"; }
							 */else {
                        	 
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
                            dataGeneral.put("mFileType", mPnbpFeeCode.getmFileType().getId());
                            dataGeneral.put("mFileTypeDetail", mPnbpFeeCode.getmFileTypeDetail().getId());
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
                                                               
                                dataGeneral.put("ownerCountry","ID");
                                dataGeneral.put("ownerProvince",mProvince.getId());
                                dataGeneral.put("ownerCity",mCity.getId());
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
                    statusError = code.equals("02") ? "Kode Billing tidak sesuai dengan permohonan" : message;
                }
            } catch (Exception e) {
                e.printStackTrace();
                statusError = "Pengecekan Kode Billing '" + bankCode + "' Gagal";
            }
        }

        dataGeneral.put("statusError", statusError);

        writeJsonResponse(response, dataGeneral);
    }
    
    @RequestMapping(path = REQUEST_MAPPING_SAVE_FORM_PEMOHON_PROSES_MADRID, method = {RequestMethod.POST})
    @ResponseBody
    public void doSaveFormPemohon(@RequestBody TxTmOwner pemohon, final HttpServletRequest request, final BindingResult errors, final HttpServletResponse response, final HttpSession session) {
    	FieldValidationUtil.required(errors, "zipCode", pemohon.getZipCode(), "kode pos");
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

    @RequestMapping(value = REQUEST_MAPPING_SAVE_FORM_KUASA_PROSES_MADRID, method = {RequestMethod.POST})
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
    

    @RequestMapping(value = REQUEST_MAPPING_SAVE_FORM_KELAS_PROSES_MADRID, method = {RequestMethod.POST})
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
    
    @RequestMapping(value = REQUEST_MAPPING_SAVE_FORM_TRANS, method = {RequestMethod.POST})
    public void doSaveReferenceTrans(@RequestParam("trans_applicationID") String appID,
    								 @RequestParam("trans_refApplicationID") String transRefAppID, 
    final HttpServletRequest request, final HttpServletResponse response ) 
    throws IOException {

    	TxTmGeneral refAppID = txTmGeneralCustomRepository.selectOne("applicationNo", transRefAppID);

        TxTmGeneral txtmRefAppId = null;
        txtmRefAppId = txTmGeneralCustomRepository.selectOne("id", appID);
        
        List<TxTmClass> txTmClasses = classService.findTxTmClassByGeneralId(refAppID);
        
        try{
        	TxTmReference txTmReference = new TxTmReference();
        	txTmReference.setAppNo(appID); 
        	txTmReference.setTxTmGeneral(txtmRefAppId);
			txTmReference.setRefApplicationId(refAppID);
			TxRegistration reg = txRegistrationCustomRepository.selectOne("txTmGeneral.id", refAppID.getId(), true);
			if (reg != null) {
				txTmReference.setRegNo(reg.getNo());
	            txTmReference.setRegDate(reg.getCreatedDate().toString());
	            System.out.println("setTxRegistration:"+reg.getNo()+"_"+reg.getCreatedDate().toString());
			}
			
			madridService.saveOrUpdate(txTmReference, txTmClasses, txtmRefAppId);
        
        	response.setStatus( HttpServletResponse.SC_OK );
        	writeJsonResponse( response, 200 );
        }catch (Exception e){
        	logger.error(e);
        	response.setStatus(HttpServletResponse.SC_FOUND);
        }
        
    }
     
    @RequestMapping(value = REQUEST_MAPPING_SAVE_FORM_TRANS_REG, method = {RequestMethod.POST}) 
    public void doSaveReferenceTransRegistration(@RequestParam("trans_applicationID") String appID,
    								 @RequestParam("trans_refRegNo") String transRefRegNo, 
    final HttpServletRequest request, final HttpServletResponse response ) 
    throws IOException { 


        String tRegis=(txRegistrationCustomRepository.selectOneByRegisNo("no", transRefRegNo)).getId(); 
        TxTmGeneral refAppID = txTmGeneralCustomRepository.selectOne("id", tRegis);
 
        TxTmGeneral txtmRefAppId=null;
        txtmRefAppId=  txTmGeneralCustomRepository.selectOne("id", appID); 
        
        try{
        	TxTmReference txTmReference = new TxTmReference();
        	txTmReference.setAppNo(appID); 
        	txTmReference.setTxTmGeneral(txtmRefAppId);
			txTmReference.setRefApplicationId(refAppID);
            txTmReference.setRefApplicationId(refAppID);
            TxRegistration reg = txRegistrationCustomRepository.selectOne("txTmGeneral.id",refAppID.getId(), true);
            txTmReference.setRegNo(reg.getNo());
            txTmReference.setRegDate(reg.getCreatedDate().toString());
            System.out.println("setTxRegistration:"+reg.getNo()+"_"+reg.getCreatedDate().toString());
			madridService.saveOrUpdate2(txTmReference);
        	//txTmReferenceRepository.save(txTmReference);
        
        	response.setStatus( HttpServletResponse.SC_OK );
        	writeJsonResponse( response, 200 );
        }catch (Exception e){
        	logger.error(e);
        	response.setStatus(HttpServletResponse.SC_FOUND);
        }
        
    }
    
    
    @RequestMapping(value = REQUEST_MAPPING_SAVE_FORM_REPL, method = {RequestMethod.POST})
    @Transactional
    public void doSaveReferenceRepl( @RequestParam("trans_applicationID") String appID,
    								 @RequestParam("repl_refApplicationID") String replAppID,
    								 @RequestParam("repl_refApplicationIDPengganti") String replAppIDPengganti,       
    final HttpServletRequest request, final HttpServletResponse response ) 
    throws IOException { 
        
//        String tRegisRepl=(txRegistrationCustomRepository.selectOneByRegisNo("no", replAppID)).getId(); 
        TxTmGeneral refAppID = txTmGeneralCustomRepository.selectOne("id", appID);

        TxTmGeneral txtmRefAppId=null;
        txtmRefAppId=  txTmGeneralCustomRepository.selectOne("id", appID);

        String processedStr = "";
        if(replAppIDPengganti.contains(",")){
            String[] splits = replAppIDPengganti.split(",");
            for(int j=0;j<splits.length;j++){
                if(processedStr.equalsIgnoreCase("")){
                    processedStr+= splits[j];
                }else{
                    processedStr+= "','"+splits[j];
                }
            }
            processedStr = "'"+processedStr+"'";
        }else{
            processedStr = "'"+replAppIDPengganti+"'";
        }
        System.out.println("check:"+ processedStr);
        List<TxTmReference> txTmReferences = new ArrayList<>();
        List<TxRegistration> regs = (txRegistrationCustomRepository.selectRegByRegisNos(processedStr));
        List<TxTmClass> txTmClasses2 = classService.findTxTmClassByGeneralId(txtmRefAppId);
		if (txTmClasses2 != null) {
			txTmClassRepository.delete(txTmClasses2);
		}
        for(TxRegistration r : regs){
            TxTmGeneral reffAppID_2 = txTmGeneralCustomRepository.selectOne("id", r.getId());
            TxTmReference txTmReference = new TxTmReference();
            txTmReference.setAppNo(appID);
            txTmReference.setTxTmGeneral(txtmRefAppId);
            txTmReference.setRefApplicationId(refAppID);
            txTmReference.setRefApplicationId_2(reffAppID_2);
//            txTmReference.setRefApplicationId(refAppID);
            TxRegistration reg = txRegistrationCustomRepository.selectOne("txTmGeneral.id",reffAppID_2.getId(), true);
            txTmReference.setRegNo(reg.getNo());
            txTmReference.setRegDate(reg.getCreatedDate().toString());
            txTmReferences.add(txTmReference);
            
            List<TxTmClass> txTmClasses = classService.findTxTmClassByGeneralId(reffAppID_2);
			if(txTmClasses != null) {				
				List<TxTmClass> txTmClassNew = new ArrayList<TxTmClass>();				
				for (TxTmClass txTmClassx : txTmClasses) {
					TxTmClass txTmClass = new TxTmClass();
					//txTmClass.setId("");
					txTmClass.setTxTmGeneral(txtmRefAppId);
					txTmClass.setmClass(txTmClassx.getmClass());
					txTmClass.setEdition(txTmClassx.getEdition());
					txTmClass.setVersion(txTmClassx.getVersion());
					txTmClass.setmClassDetail(txTmClassx.getmClassDetail());
					txTmClass.setTransactionStatus(txTmClassx.getTransactionStatus());
					txTmClass.setCorrectionFlag(txTmClassx.isCorrectionFlag());
					txTmClass.setXmlFileId(txTmClassx.getXmlFileId());
					txTmClass.setNotes(txTmClassx.getNotes());
					txTmClassNew.add(txTmClass);
				}
				txTmClassRepository.save(txTmClassNew);
			}
        }
        //TxTmGeneral reffAppID_2 = txTmGeneralCustomRepository.selectOne("id", tRegisReplPengganti);
        try{
            madridService.saveAll(txTmReferences);
            response.setStatus( HttpServletResponse.SC_OK );
            writeJsonResponse( response, 200 );
        }catch (Exception e){
            System.err.println("Error:"+e.toString());
            logger.error(e);
            response.setStatus(HttpServletResponse.SC_FOUND);
        }

        
    }

    @RequestMapping(path = REQUEST_MAPPING_PROSES_MADRID_CHOOSE_REFERENSI, method = {RequestMethod.GET})
    public void doChooseReferensi(final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            String appId = request.getParameter("target");
            TxTmGeneral txTmGeneral = madridOoService.selectOneDataReferensi("id", appId);

            Map<String, String> result = new HashMap<>();
            result.put("applicationId", txTmGeneral.getId());
            result.put("applicationNo", txTmGeneral.getApplicationNo());
            result.put("registrationNo", txTmGeneral.getTxRegistration() == null ? "" : txTmGeneral.getTxRegistration().getNo());
            result.put("brandName", txTmGeneral.getTxTmBrand() == null ? "" : txTmGeneral.getTxTmBrand().getName());

            writeJsonResponse(response, result);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }
    
    @PostMapping(value = REQUEST_MAPPING_SELESAI_PERMOHONAN_PROSES_MADRID)
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
            }  
			 
			 else if (txTmGeneral.getTxTmClassList().size() == 0) { msg.setKey("Error");
			 msg.setValue("Tab Kelas Belum Tersimpan"); }
			  
            
            else if (txTmGeneral.getTxReception().getmFileType().getId().equals("MEREK_DAGANG")) {
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
            
//            String docIdTempTTDP = "";
//            String docIdTempTTDK = "";
//            String docFileNameTTDP = "";
//            String docFileNameTTDK = "";
//
            String docIdTempDokPendukung = "";

            for (TxTmDoc txTmDoc : txTmDocList) {
//                if(txTmDoc.getmDocType().getId().equalsIgnoreCase("TTDP")) {
//                	docIdTempTTDP = txTmDoc.getmDocType().getId();
//                	docFileNameTTDP = txTmDoc.getFileName();
//                } else if(txTmDoc.getmDocType().getId().equalsIgnoreCase("TTDK")) {
//                	docIdTempTTDK = txTmDoc.getmDocType().getId();
//                	docFileNameTTDK = txTmDoc.getFileName();
//                } else
                 if(txTmDoc.getmDocType().getId().equalsIgnoreCase("F05")) {
                    docIdTempDokPendukung = txTmDoc.getmDocType().getId();
                }
//
            }

            try {
            	if(docIdTempDokPendukung.isEmpty()) {
            		msg.setKey("Error");
                    msg.setValue("Dokumen Surat Kuasa Belum Dilampirkan. Hanya Konsultan KI yang dapat mengajukan permohonan Transformasi & Replacement Online");
               }
//               else
//            	   if((!txTmGeneral.getTxTmRepresentative().isEmpty()) && docIdTempDokPendukung.isEmpty()) {
//               		msg.setKey("Error");
//                    msg.setValue("Dokumen Surat Kuasa Belum Dilampirkan");
//                     }
////                  else if(txTmGeneral.getTxReception().getmFileTypeDetail().getId().equalsIgnoreCase("UMKM") && docIdTempDokPendukung.isEmpty()) {
//                    msg.setKey("Error");
//                    msg.setValue("Untuk Jenis Permohonan UMKM, Wajib Melampirkan Jenis Dokumen: Surat UMKM Asli");
//               } else if(docFileNameTTDP.equalsIgnoreCase(txTmGeneral.getTxTmBrand().getFileName())
//               		|| docFileNameTTDK.equalsIgnoreCase(txTmGeneral.getTxTmBrand().getFileName())) {
//               		msg.setKey("Error");
//                    msg.setValue("File Gambar Label Merek Yang Anda Upload Terindikasi Sama Dengan File Gambar Tanda Tangan Kuasa/Pemohon, "
//                   		+ "Mohon Periksa Kembali. Pastikan Gambar, Nama file Label Merek dengan Tanda Tangan Pemohon/Kuasa berbeda "
//                   		+ "dan Kemudian Klik Simpan dan Lanjutkan !!");
//               }
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
    
    private void signPdf(InputStream input, OutputStream output) {
        String key = CERTIFICATE_FILE + "eFiling.p12";
        System.out.println("PATH : " + key);
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

    @RequestMapping(value = REQUEST_MAPPING_PROSES_MADRID_PERMOHONAN_CETAK, method = {RequestMethod.GET})
    @ResponseBody
    public String doCetakMadrid(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        String applicationNo = request.getParameter("no");
        String dwnonly = request.getParameter("dwnonly");

        TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(applicationNo);
        List<TxTmDoc> txtmDocList = doclampiranService.getAllDocByApplicationId("txTmGeneral.id", txTmGeneral.getId());

        try {

            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("APPLICATION_NO", txTmGeneral.getApplicationNo());
            parameters.put("TANDA_TANGAN", uploadFileImageTandaTangan);
            parameters.put("pCapDjki", uploadFilePathSignature+"signimage.jpg");

            List<TxTmClass> txTmClassList = classService.selectAllClassByIdGeneral(txTmGeneral.getId(), ClassStatusEnum.ACCEPT.name());
            if ( txTmClassList.size() > 0){
                Map<Integer, String[]> mapClass = new HashMap<>();
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
                //set value temporary only for report
                List<TxTmClass> tempClassList = new ArrayList<>();
                List<Integer> dedup = new ArrayList<>();
                for (TxTmClass result : txTmClassList) {
                    int key = result.getmClass().getNo();
                    if (dedup.contains(key)){
                        continue;
                    }
                    dedup.add(key);
                    result.getmClass().setDesc(mapClass.get(key)[0]);
                    result.getmClass().setDescEn(mapClass.get(key)[1]);
                    tempClassList.add(result);
                }
                txTmClassList = tempClassList;
            }
            parameters.put("daftarKelas", new JRBeanCollectionDataSource(txTmClassList));

            String referenceRegNo2 = "";
            String noPermohonan = "";            
            String regNo = "";
            String regNo2 = "";
            List<TxTmReference> referenceList = txTmReferenceCustomRepository.selectByTxTmGeneral(txTmGeneral.getId());
            for(TxTmReference ref : referenceList){
            	System.out.println("masuk sini");
                if(ref.getRefApplicationId_2()==null) {
                	System.out.println("masuk sini 1");
                    continue;
                }
                if(ref.getRefApplicationId_2().getTxRegistration()!=null) {
                	System.out.println("masuk sini 2");
                    if(referenceRegNo2.equalsIgnoreCase("")) {
                        referenceRegNo2 += ref.getRefApplicationId_2().getTxRegistration().getNo();
                    } else {
                        referenceRegNo2 += ","+ref.getRefApplicationId_2().getTxRegistration().getNo();
                    }
                }
                
                TxRegistration tr = txRegistrationCustomRepository.selectOne("txTmGeneral.id", ref.getRefApplicationId_2().getId(), true);
                TxRegistration tr2 = txRegistrationCustomRepository.selectOne("txTmGeneral.id", ref.getRefApplicationId().getId(), true);
                
                noPermohonan = ref.getRefApplicationId().getId();
                regNo = tr.getNo();
                regNo2 = tr2.getNo();
            }
            int priorList = 0;
            if(referenceRegNo2!="") {
                List<KeyValue> searchCriteria = new ArrayList<>();
                searchCriteria.add(new KeyValue("txRegistration.no", referenceRegNo2, true));
                List<TxTmGeneral> searchResult = permohonanOnlineService.searchRefReception(searchCriteria, "orderBy", "ASC", 0, -1).getList();
                parameters.put("daftarReplacement", new JRBeanCollectionDataSource(searchResult));
                parameters.put("tglPendaftaran", searchResult.get(0).getCreatedDate());
                parameters.put("tglPengajuan", searchResult.get(0).getFilingDate());
                priorList = searchResult.get(0).getTxTmPriorList().size();
            }
            
            parameters.put("noPermohonan", noPermohonan);
            parameters.put("regNo", regNo);
            parameters.put("regNo2", regNo2);
            
            String pathReport = "/report/madrid/CetakMadridTR.jrxml";
            if (priorList == 0) {
            	pathReport = "/report/madrid/CetakMadridTR-v2.jrxml";
            }
            final InputStream reportInputStream = getClass().getResourceAsStream(pathReport);
            final JasperDesign jasperDesign = JRXmlLoader.load(reportInputStream);
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, madridOoService.getConnection());

            ByteArrayOutputStream output = new ByteArrayOutputStream();

            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(output));
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.exportReport();

            byte[] result = new byte[0];

            try (
                ByteArrayOutputStream msOut = new ByteArrayOutputStream()) {
                Document document = new Document();
                PdfCopy copy = new PdfCopy(document, msOut);
                document.open();

                ByteArrayInputStream bs = new ByteArrayInputStream(output.toByteArray());
                PdfReader jasperReader = new PdfReader(bs); //Reader for Jasper Report
                for (int page = 0; page < jasperReader.getNumberOfPages(); ) {
                    copy.addPage(copy.getImportedPage(jasperReader, ++page));
                }
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
                copy.freeReader(jasperReader);
                jasperReader.close();
                document.close();

                result = msOut.toByteArray();
            } catch (BadPdfFormatException e) {

            } catch (DocumentException e) {

            }
            String folderDoc = downloadFileDocMadridOOCetakPath + DateUtil.formatDate(txTmGeneral.getCreatedDate(), "yyyy/MM/dd/");
            String filenameDoc = "CetakMadrid-TR-" + txTmGeneral.getApplicationNo() + ".pdf";

            /*try {
                Path path = Paths.get(folderDoc + filenameDoc);
                if((Files.exists(path) && Files.size(path) > 0) || dwnonly != null) {
                    response.setContentType("application/pdf");
                    response.setHeader("Content-disposition", "attachment; filename=CetakMadrid-TR-" + txTmGeneral.getApplicationNo() + ".pdf");
                    OutputStream output2 = response.getOutputStream();
                    Files.copy(path, output2);
                    output2.flush();
                    return "";
                }
            } catch(IOException e) {
            }*/
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
            response.setHeader("Content-disposition", "attachment; filename=CetakMadrid-TR"+ txTmGeneral.getApplicationNo() + ".pdf");
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
            logger.error(e.getMessage(), e);
        }
        return "";
    }

}
