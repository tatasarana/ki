package com.docotel.ki.controller.online;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.enumeration.StatusEnum;
import com.docotel.ki.model.master.*;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.pojo.*;
import com.docotel.ki.repository.NativeQueryRepository;
import com.docotel.ki.repository.custom.transaction.TxPostReceptionCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxPostReceptionDetailCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmGeneralCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmOwnerCustomRepository;
import com.docotel.ki.repository.master.MWorkflowProcessRepository;
import com.docotel.ki.repository.transaction.TxTmGeneralRepository;
import com.docotel.ki.service.EmailService;
import com.docotel.ki.service.SimpakiService;
import com.docotel.ki.service.master.*;
import com.docotel.ki.service.transaction.*;
import com.docotel.ki.signature.PDFSignatureFacade;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.util.NumberUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfReader;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class PascaOnlineController extends BaseController {
    private static final String DIRECTORY_PAGE = "pasca-online/";
    private static final String PAGE_PASCA_ONLINE_LIST = DIRECTORY_PAGE + "list-pasca-online";
    private static final String PAGE_PASCA_ONLINE_TAMBAH_EDIT = DIRECTORY_PAGE + "tambah-edit-pasca-online";
    private static final String PAGE_PASCA_ONLINE_PRATINJAU = DIRECTORY_PAGE + "pratinjau-pasca-online";
    private static final String PATH_PASCA_ONLINE_AJAX_SEARCH_LIST = "/cari-pasca-online";
    private static final String PATH_PASCA_ONLINE_PENGHAPUSAN = "/cari-pasca-online-penghapusan";
    private static final String PATH_PASCA_ONLINE_AJAX_SEARCH_LIST_REFERENSI = "/cari-referensi-pasca-online";
    private static final String PATH_PASCA_ONLINE_AJAX_SEARCH_LIST_PREVIEW_REFERENSI = "/cari-preview-referensi-pasca-online";
    private static final String PATH_PASCA_ONLINE_AJAX_SEARCH_LIST_KUASA = "/cari-kuasa-pasca-online";
    private static final String PATH_PASCA_ONLINE_AJAX_SEARCH_LIST_DOKUMEN = "/cari-dokumen-pasca-online";
    private static final String PATH_PASCA_ONLINE_AJAX_SEARCH_LIST_PEMOHON = "/cari-data-pemohon-pasca-online";
    private static final String PATH_PASCA_ONLINE_AJAX_SEARCH_LIST_LICENSE = "/cari-data-license-pasca-online";
    private static final String PATH_PASCA_ONLINE_LIST = "/list-pasca-online";
    private static final String PATH_AJAX_SAVE_PEMOHON_PASCA = "/save-pemohon-pasca";
    public static final String REDIRECT_PASCA_ONLINE_LIST = "redirect:" + PATH_AFTER_LOGIN + PATH_PASCA_ONLINE_LIST;
    private static final String PATH_PASCA_ONLINE_TAMBAH = "/tambah-pasca-online";
    private static final String PATH_PASCA_ONLINE_EDIT = "/edit-pasca-online";
    private static final String PATH_PASCA_ONLINE_PRATINJAU = "/pratinjau-pasca-online";
    private static final String PATH_PASCA_ONLINE_CETAK = "/cetak-pasca-online";
    private static final String PATH_PASCA_ONLINE_CHECK_BILLING = "/check-billing-pasca-online";
    private static final String PATH_PASCA_ONLINE_CHOOSE_REFERENSI = "/choose-referensi-pasca-online";
    private static final String PATH_PASCA_ONLINE_CHOOSE_KUASA = "/choose-kuasa-pasca-online";
    private static final String PATH_PREVIEW_PASCA_ONLINE_CHOOSE_PEMOHON = "/choose-preview-pemohon-pasca-online";
    private static final String PATH_PASCA_ONLINE_SAVE_GENERAL = "/save-general-pasca-online";
    private static final String PATH_PASCA_ONLINE_SAVE_GENERAL_V2 = "/save-general-pasca-online_v2";
    private static final String PATH_PASCA_ONLINE_SAVE_PEMOHON = "/save-pemohon-pasca-online";
    private static final String PATH_PASCA_ONLINE_SAVE_KUASA = "/save-kuasa-pasca-online";
    private static final String PATH_PASCA_ONLINE_SAVE_DOKUMEN = "/save-dokumen-pasca-online";
    private static final String PATH_PASCA_ONLINE_VALIDATE_DOKUMEN = "/validasi-dokumen-pasca-online";
    private static final String PATH_PRATINJAU_PASCA_ONLINE_SAVE_DOKUMEN = "/save-pratinjau-dokumen-pasca-online";
    private static final String PATH_PASCA_ONLINE_SAVE_RESUME = "/save-resume-pasca-online";
    private static final String PATH_PASCA_ONLINE_SAVE_NOTE_PENGHAPUSAN = "/save-note-penghapusan-pasca-online";
    private static final String REQUEST_MAPPING_PASCA_ONLINE_AJAX_SEARCH_LIST = PATH_PASCA_ONLINE_AJAX_SEARCH_LIST + "*";
    private static final String REQUEST_MAPPING_PASCA_ONLINE_AJAX_SEARCH_LIST_REFERENSI = PATH_PASCA_ONLINE_AJAX_SEARCH_LIST_REFERENSI + "*";
    private static final String REQUEST_MAPPING_PASCA_ONLINE_AJAX_SEARCH_LIST_PREVIEW_REFERENSI = PATH_PASCA_ONLINE_AJAX_SEARCH_LIST_PREVIEW_REFERENSI + "*";
    private static final String REQUEST_MAPPING_PASCA_ONLINE_AJAX_SEARCH_LIST_KUASA = PATH_PASCA_ONLINE_AJAX_SEARCH_LIST_KUASA + "*";
    private static final String REQUEST_MAPPING_PASCA_ONLINE_AJAX_SEARCH_LIST_DOKUMEN = PATH_PASCA_ONLINE_AJAX_SEARCH_LIST_DOKUMEN + "*";
    private static final String REQUEST_MAPPING_PASCA_ONLINE_AJAX_SEARCH_LIST_PEMOHON = PATH_PASCA_ONLINE_AJAX_SEARCH_LIST_PEMOHON + "*";
    private static final String REQUEST_MAPPING_PASCA_ONLINE_AJAX_SEARCH_LIST_LICENSE = PATH_PASCA_ONLINE_AJAX_SEARCH_LIST_LICENSE + "*";
    private static final String REQUEST_MAPPING_PASCA_ONLINE_LIST = PATH_PASCA_ONLINE_LIST + "*";
    private static final String REQUEST_MAPPING_PASCA_ONLINE_PENGHAPUSAN = PATH_PASCA_ONLINE_PENGHAPUSAN + "*";
    private static final String REQUEST_MAPPING_PASCA_ONLINE_TAMBAH = PATH_PASCA_ONLINE_TAMBAH + "*";
    private static final String REQUEST_MAPPING_PASCA_ONLINE_EDIT = PATH_PASCA_ONLINE_EDIT + "*";
    private static final String REQUEST_MAPPING_AJAX_SAVE_TAMBAH_PEMOHON = PATH_AJAX_SAVE_PEMOHON_PASCA + "*";
    private static final String REQUEST_MAPPING_PASCA_ONLINE_PRATINJAU = PATH_PASCA_ONLINE_PRATINJAU + "*";
    private static final String REQUEST_MAPPING_PASCA_ONLINE_CETAK = PATH_PASCA_ONLINE_CETAK + "*";
    private static final String REQUEST_MAPPING_PASCA_ONLINE_CHECK_BILLING = PATH_PASCA_ONLINE_CHECK_BILLING + "*";
    private static final String REQUEST_MAPPING_PASCA_ONLINE_CHOOSE_REFERENSI = PATH_PASCA_ONLINE_CHOOSE_REFERENSI + "*";
    private static final String REQUEST_MAPPING_PASCA_ONLINE_CHOOSE_KUASA = PATH_PASCA_ONLINE_CHOOSE_KUASA + "*";
    private static final String REQUEST_MAPPING_PREVIEW_PASCA_ONLINE_CHOOSE_PEMOHON = PATH_PREVIEW_PASCA_ONLINE_CHOOSE_PEMOHON + "*";
    private static final String REQUEST_MAPPING_PASCA_ONLINE_SAVE_GENERAL = PATH_PASCA_ONLINE_SAVE_GENERAL + "*";
    private static final String REQUEST_MAPPING_PASCA_ONLINE_SAVE_GENERAL_V2 = PATH_PASCA_ONLINE_SAVE_GENERAL_V2 + "*";
    private static final String REQUEST_MAPPING_PASCA_ONLINE_SAVE_PEMOHON = PATH_PASCA_ONLINE_SAVE_PEMOHON + "*";
    private static final String REQUEST_MAPPING_PASCA_ONLINE_SAVE_KUASA = PATH_PASCA_ONLINE_SAVE_KUASA + "*";
    private static final String REQUEST_MAPPING_PASCA_ONLINE_SAVE_DOKUMEN = PATH_PASCA_ONLINE_SAVE_DOKUMEN + "*";
    private static final String REQUEST_MAPPING_PASCA_ONLINE_VALIDATE_DOKUMEN = PATH_PASCA_ONLINE_VALIDATE_DOKUMEN + "*";
    private static final String REQUEST_MAPPING_PRATINJAU_PASCA_ONLINE_SAVE_DOKUMEN = PATH_PRATINJAU_PASCA_ONLINE_SAVE_DOKUMEN + "*";
    private static final String REQUEST_MAPPING_PASCA_ONLINE_SAVE_RESUME = PATH_PASCA_ONLINE_SAVE_RESUME + "*";
    private static final String REQUEST_MAPPING_PASCA_ONLINE_SAVE_NOTE_PENGHAPUSAN = PATH_PASCA_ONLINE_SAVE_NOTE_PENGHAPUSAN + "*";
    private static final String DISPENSASI_PERPANJANGAN_ID = "14acf099-f0bc-40d1-9f9b-2063d23d7e1d";
    @Autowired
    private CityService cityService;
    @Autowired
    private CountryService countryService;
    @Autowired
    private DocTypeService docTypeService;
    @Autowired
    private DokumenLampiranService doclampiranService;
    @Autowired
    private FileService fileService;
    @Autowired
    private LawService lawService;
    @Autowired
    private OwnerService ownerService;
    @Autowired
    private RepresentativeService representativeService;
    @Autowired
    private SimpakiService simpakiService;
    @Autowired
    private TrademarkService trademarkService;
    @Autowired
    private PascaOnlineService pascaOnlineService;
    @Autowired
    private ProvinceService provinceService;
    @Autowired
    private MonitorService monitorService;
    @Autowired
    private LicenseService licenseService;
    @Autowired
    private LookupService lookupService;
    @Autowired
    private OwnerService pemohonService;
    @Autowired
    private MWorkflowProcessRepository mWorkflowProcessRepository;
    @Autowired
    private TxTmGeneralCustomRepository txTmGeneralCustomRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private PermohonanService permohonanService;

    @Autowired
    TxTmOwnerCustomRepository txTmOwnerCustomRepository;

    @Autowired
    NativeQueryRepository nativeQueryRepository;

    @Autowired
    TxPostReceptionCustomRepository txPostReceptionCustomRepository;

    @Autowired
    TxPostReceptionDetailCustomRepository txPostReceptionDetailCustomRepository;

    @Autowired
    ResourceLoader resourceLoader;

    @Value("${upload.file.logoemail.image:}")
    private String logoEmailImage;
    @Value("${upload.file.web.image:}")
    private String pathImage;
    @Autowired
    TxTmGeneralRepository txTmGeneralRepository;

    @Value("${app.freebill}")
    private String freebill;
    @Value("${upload.file.doc.pasca.path:}")
    private String uploadFileDocPascaPath;
    @Value(("${certificate.file}"))
    private String CERTIFICATE_FILE;
    private FileInputStream fileInputStreamReader;
    @Value("${download.output.pasca.cetakmerek.file.path:}")
    private String downloadFileDocPascaCetakMerekPath;
    @Value("${upload.file.path.signature:}")
    private String downloadImgPath;

    private SimpleDateFormat fmtYear = new SimpleDateFormat("yyyy");

    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "permohonanPasca");
        model.addAttribute("subMenu", "pascaOnline");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MUser mUser = (MUser) auth.getPrincipal();
        if (mUser.isReprs())
            model.addAttribute("typeuser", "(Konsultan)");
        else
            model.addAttribute("typeuser", "(Umum)");

    }

    private void doInitiatePasca(final Model model, final HttpServletRequest request) {
        List<MFileSequence> fileSequenceList = fileService.findMFileSequenceByStatusFlagTrue();
        model.addAttribute("fileSequenceList", fileSequenceList);

        List<MFileType> fileTypeList = fileService.findByMenuAndstatusPaidAndstatusFlag("PASCA", true); //fileService.findMFileTypeByStatusFlagTrue();
        Collections.sort(fileTypeList, (o1, o2) -> o1.getDesc().compareTo(o2.getDesc()));

        model.addAttribute("fileTypeList", fileTypeList);

        if (request.getRequestURI().contains(PATH_PASCA_ONLINE_TAMBAH)
                || request.getRequestURI().contains(PATH_PASCA_ONLINE_EDIT)
                || request.getRequestURI().contains(PATH_PASCA_ONLINE_PRATINJAU)) {
            String postId = request.getParameter("no");

            model.addAttribute("isNew", request.getRequestURI().contains(PATH_PASCA_ONLINE_TAMBAH));

            TxPostReception txPostReception = pascaOnlineService.selectOneTxPostReceptionById(postId);
            if (txPostReception == null) {
                txPostReception = new TxPostReception();
                txPostReception.setPostDate(new Timestamp(System.currentTimeMillis()));
            }

			/*if (txPostReception.getTxTmGeneral() == null)
				txPostReception.setTxTmGeneral(new TxTmGeneral());
			if (txPostReception.getTxTmGeneral().getTxRegistration() == null)
				txPostReception.getTxTmGeneral().setTxRegistration(new TxRegistration());
			if (txPostReception.getTxTmGeneral().getTxTmBrand() == null)
				txPostReception.getTxTmGeneral().setTxTmBrand(new TxTmBrand());
			*/
            model.addAttribute("dataGeneral", txPostReception);
            model.addAttribute("txPostDetail_id", txPostReception.getId());

            List<TxPostReceptionDetail> txPostReceptionDetail = pascaOnlineService.selectAllPostDetail(txPostReception.getId());
            model.addAttribute("dataGeneralDetail", txPostReceptionDetail);

            TxTmOwner txTmOwner = new TxTmOwner();
            List<TxTmOwnerDetail> txTmOwnerDetailList = new ArrayList<>();

            model.addAttribute("pemohon", txTmOwner);
            model.addAttribute("listPemohonChild", txTmOwnerDetailList);

            TxLicense txLicense = new TxLicense();
            txLicense.setLevel(0);
            model.addAttribute("txLicense", txLicense);

            List<MLookup> penghapusanCode = lookupService.selectAllbyGroup("JenisPenghapusan");
            model.addAttribute("listPenghapusan", penghapusanCode);

            List<MLookup> jenisPerubahanCode = lookupService.selectAllbyGroup("JenisPerubahan");
            model.addAttribute("listPerubahan", jenisPerubahanCode);

            /*List<MCountry> mCountryList = countryService.findByStatusFlagTrue();
            List<MProvince> mProvinceList = provinceService.findByStatusFlagTrue();
            List<MCity> mCityList = cityService.findByStatusFlagTrue();*/

            if (request.getRequestURI().contains(PATH_PASCA_ONLINE_PRATINJAU)) {
                if (txPostReception.getmFileType().getId().equalsIgnoreCase("PERPANJANGAN") || txPostReception.getmFileType().getId().equalsIgnoreCase("PERPANJANGAN_6_BULAN_KADALUARSA")) {
                    if (txPostReceptionDetail != null) {
                        //get list tx monitor
                        List<TxMonitor> listCurrentMonitor = monitorService.findByTxTmGeneral(txPostReceptionDetail.get(0).getTxTmGeneral());
                        MWorkflow wf = txPostReceptionDetail.get(0).getTxTmGeneral().getTxReception().getmWorkflow();

                        List<MWorkflowProcess> listWfProcess = mWorkflowProcessRepository.findMWorkflowProcessesByWorkflowAndStatusFlagOrderByOrders(wf, true);
                        for (MWorkflowProcess mwfp : listWfProcess) {
                            for (TxMonitor txM : listCurrentMonitor) {
                                if (txM.getmWorkflowProcessActions() != null) {
                                    if (txM.getmWorkflowProcessActions().getAction().getId() != null) {
                                        if (txM.getmWorkflowProcessActions().getAction().getId().equalsIgnoreCase("PERPANJANGAN")) {
                                            //System.out.println(txM.getmWorkflowProcessActions().getAction().getDocument().getId()+File.separator+txM.getTxTmGeneral().getApplicationNo()+File.separator+txM.getId());
                                            model.addAttribute("paramCetak", txM.getmWorkflowProcessActions().getAction().getDocument().getId() + "/" + txM.getTxTmGeneral().getApplicationNo() + "/" + txM.getId());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            TxPostOwner txPostOwner = txPostReception.getTxPostOwner() == null ? new TxPostOwner() : txPostReception.getTxPostOwner();
            if (txPostOwner.getmCity() == null)
                txPostOwner.setmCity(new MCity());
            if (txPostOwner.getmProvince() == null)
                txPostOwner.setmProvince(new MProvince());
            if (txPostOwner.getmCountry() == null)
                txPostOwner.setmCountry(new MCountry());
            if (txPostOwner.getNationality() == null)
                txPostOwner.setNationality(new MCountry());

            model.addAttribute("dataPemohon", txPostOwner);

            TxPostRepresentative txPostRepresentative = txPostReception.getTxPostRepresentative() == null ? new TxPostRepresentative() : txPostReception.getTxPostRepresentative();
            if (txPostRepresentative.getmRepresentative() == null) {
                MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                MRepresentative mRepresentative = representativeService.selectOne("userId.id", mUser.getId());
                if (mRepresentative == null) {
                    mRepresentative = new MRepresentative();
                }
                txPostRepresentative.setmRepresentative(mRepresentative);
            }
            if (txPostRepresentative.getmRepresentative().getmCity() == null)
                txPostRepresentative.getmRepresentative().setmCity(new MCity());
            if (txPostRepresentative.getmRepresentative().getmProvince() == null)
                txPostRepresentative.getmRepresentative().setmProvince(new MProvince());
            if (txPostRepresentative.getmRepresentative().getmCountry() == null)
                txPostRepresentative.getmRepresentative().setmCountry(new MCountry());

            model.addAttribute("dataKuasa", txPostRepresentative);

            List<MFileTypeDetail> fileTypeDetailList = fileService.findMFileTypeDetailByStatusFlagTrue();
            model.addAttribute("fileTypeDetailList", fileTypeDetailList);

            MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            MRepresentative mRepresentative = representativeService.selectOne("userId.id", mUser.getId());

            if (request.getRequestURI().contains(PATH_PASCA_ONLINE_EDIT)) {
                List<MCountry> mCountryList = countryService.findAll();
                model.addAttribute("listCountry", mCountryList);

                List<MProvince> mProvinceList = provinceService.findAll();
                model.addAttribute("listProvince", mProvinceList);

                List<MCity> mCityList = cityService.selectAll();
                model.addAttribute("listCity", mCityList);

                String repsId = "";
                if (mRepresentative != null) {
                    if (mRepresentative.getId() != null) {
                        repsId = mRepresentative.getId();
                    }
                }
                model.addAttribute("repsId", repsId);

            } else if (request.getRequestURI().contains(PATH_PASCA_ONLINE_TAMBAH)) {
                List<MCountry> mCountryList = countryService.findByStatusFlagTrue();
                model.addAttribute("listCountry", mCountryList);

                List<MProvince> mProvinceList = provinceService.findByStatusFlagTrue();
                model.addAttribute("listProvince", mProvinceList);

                List<MCity> mCityList = cityService.selectAll();
                model.addAttribute("listCity", mCityList);

                String repsId = "";
                if (mRepresentative != null) {
                    if (mRepresentative.getId() != null) {
                        repsId = mRepresentative.getId();
                    }
                }
                model.addAttribute("repsId", repsId);


            }

            //List<MDocType> mDocTypeList = docTypeService.findByStatusFlagTrue();
            //model.addAttribute("listDocType", mDocTypeList);

            List<KeyValue> searchCriteria = new ArrayList<>();
            searchCriteria.add(new KeyValue("mDocType.statusFlag", true, true));
            if (!(boolean) model.asMap().get("isNew")) {
                searchCriteria.add(new KeyValue("mFileType.id", txPostReception.getmFileType().getId(), true));
            }

            List<MDocTypeDetail> listDocType = docTypeService.selectAllDetail(searchCriteria);

            model.addAttribute("listDocType", listDocType);
            model.addAttribute("docUploadDate", DateUtil.formatDate(new Date(), "dd/MM/yyyy"));
        }
    }

    @RequestMapping(value = REQUEST_MAPPING_PASCA_ONLINE_LIST, method = {RequestMethod.GET})
    public String doShowPagePascaOnline(@RequestParam(value = "error", required = false) String error, final Model model, final HttpServletRequest request, final HttpServletResponse response) {
        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("errorMessage", error);
        }

        doInitiatePasca(model, request);
        return PAGE_PASCA_ONLINE_LIST;
    }

    @RequestMapping(value = REQUEST_MAPPING_PASCA_ONLINE_TAMBAH, method = {RequestMethod.GET})
    public String doShowPageTambahPascaOnline(final Model model, final HttpServletRequest request, final HttpServletResponse response) {
        doInitiatePasca(model, request);
        model.addAttribute("operation_type", "ADD");
        return PAGE_PASCA_ONLINE_TAMBAH_EDIT;
    }

    @RequestMapping(value = REQUEST_MAPPING_PASCA_ONLINE_EDIT, method = {RequestMethod.GET})
    public String doShowPageEditPascaOnline(final Model model, final HttpServletRequest request, final HttpServletResponse response) {
        String postId = request.getParameter("no");

        TxPostReception txPostReception = pascaOnlineService.selectOneTxPostReceptionById(postId);
        String type_mfile = txPostReception.getmFileType().getDesc();

        // cari jenis Permohonan
        TxPostReceptionDetail txPostReceptionDetail = txPostReceptionDetailCustomRepository.selectOneByAppId(postId);

        String txPostDetail_id = txPostReceptionDetail.getId();
        String application_id = txPostReceptionDetail.getTxTmGeneral().getId();
        String application_no = txPostReceptionDetail.getTxTmGeneral().getApplicationNo();


        MUser userLogin = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userLogin.getUsername() != "super") {
            List<KeyValue> criteriaList = new ArrayList<>();
            criteriaList.add(new KeyValue("id", postId, true));
            criteriaList.add(new KeyValue("createdBy", userLogin, true));
            txPostReception = pascaOnlineService.selectOneTxPostReceptionById(criteriaList);
        }

        if (txPostReception == null) {
            return REDIRECT_PASCA_ONLINE_LIST + "?error=Pasca Online tidak ditemukan";
        } else if(!txPostReception.getmStatus().getId().equals("IPT_DRAFT")) {
            return "error-404";
        }
        doInitiatePasca(model, request);

        model.addAttribute("type_mfile", type_mfile);
        model.addAttribute("asal_application_id", application_id);
        model.addAttribute("asal_application_no", application_no);
        model.addAttribute("operation_type", "EDIT");
        model.addAttribute("txPostDetail_id", txPostDetail_id);
        return PAGE_PASCA_ONLINE_TAMBAH_EDIT;
    }

    @RequestMapping(value = REQUEST_MAPPING_PASCA_ONLINE_PRATINJAU, method = {RequestMethod.GET})
    public String doShowPagePratinjauPascaOnline(final Model model, final HttpServletRequest request, final HttpServletResponse response) {
        String postId = request.getParameter("no");

        TxPostReception txPostReception = pascaOnlineService.selectOneTxPostReceptionById(postId);

        MUser userLogin = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userLogin.getUsername() != "super") {
            List<KeyValue> criteriaList = new ArrayList<>();
            criteriaList.add(new KeyValue("id", postId, true));
            criteriaList.add(new KeyValue("createdBy", userLogin, true));
            txPostReception = pascaOnlineService.selectOneTxPostReceptionById(criteriaList);

        }
        if (txPostReception == null) {
            return REDIRECT_PASCA_ONLINE_LIST + "?error=Pasca Online tidak ditemukan";
        }

        doInitiatePasca(model, request);
        return PAGE_PASCA_ONLINE_PRATINJAU;
    }

    @RequestMapping(value = REQUEST_MAPPING_PASCA_ONLINE_AJAX_SEARCH_LIST, method = {RequestMethod.POST})
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

            MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            searchCriteria.add(new KeyValue("onlineFlag", Boolean.TRUE, true));
            searchCriteria.add(new KeyValue("createdBy", mUser, true));

            if (searchByArr != null) {
                for (int i = 0; i < searchByArr.length; i++) {
                    String searchBy = searchByArr[i];
                    String value = null;
                    try {
                        value = keywordArr[i];
                    } catch (ArrayIndexOutOfBoundsException e) {
                    }
                    if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                        if (searchBy.equalsIgnoreCase("postDate")) {
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

            String orderBy = request.getParameter("order[0][column]");
            if (orderBy != null) {
                orderBy = orderBy.trim();
                if (orderBy.equalsIgnoreCase("")) {
                    orderBy = null;
                } else {
                    switch (orderBy) {
                        case "1":
                            orderBy = "eFilingNo";
                            break;
                        case "2":
                            orderBy = "postDate";
                            break;
                        case "3":
                            orderBy = "postNo";
                            break;
                        case "4":
                            orderBy = "mFileType.desc";
                            break;
                        case "5":
                            orderBy = null;
//                            orderBy = "txPostReceptionDetailList.txTmGeneral.applicationNo";
                            break;
                        case "6":
                            orderBy = "billingCode";
                            break;
                        default:
                            orderBy = null;
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

            GenericSearchWrapper<TxPostReception> searchResult = pascaOnlineService.searchPostReception(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxPostReception result : searchResult.getList()) {
                    String refNoList = "-";
                    String refStatusList = "-";
                    ArrayList<String> refNo = new ArrayList<String>();
                    ArrayList<String> refStatus = new ArrayList<String>();

                    String merekList = "-";
                    ArrayList<String> smerek = new ArrayList<String>();
                    try {
                        for (TxPostReceptionDetail txPostReceptionDetail : result.getTxPostReceptionDetailList()) {
                            refNo.add("" + txPostReceptionDetail.getTxTmGeneral().getApplicationNo());
                            refStatus.add("" + txPostReceptionDetail.getmStatus().getId());
                            smerek.add("" + txPostReceptionDetail.getTxTmGeneral().getTxTmBrand().getName());
                        }

                        Set<String> temp2 = new LinkedHashSet<String>(refStatus);
                        String[] unique2 = temp2.toArray(new String[temp2.size()]);
                        if (unique2.length > 0) {
                            refStatusList = String.join(",", unique2);
                        }

                        Set<String> temp = new LinkedHashSet<String>(refNo);
                        String[] unique = temp.toArray(new String[temp.size()]);
                        if (unique.length > 0) {
                            refNoList = String.join(",", unique);
                        }

                        Set<String> tempMerek = new LinkedHashSet<String>(smerek);
                        String[] uniqueMerek = tempMerek.toArray(new String[tempMerek.size()]);
                        if (uniqueMerek.length > 0) {
                            merekList = String.join(",", uniqueMerek);
                        }

                    } catch (NullPointerException e) {
                    }

                    no++;
//                    untuk mendownload saja
                    String buttonCetak = "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin(PATH_PASCA_ONLINE_CETAK) + "?no=" + result.getId() + "\">Tanda Terima</a>";
                    data.add(new String[]{
                            "" + no,
                            "<a target=\"_blank\" href=\"" + getPathURLAfterLogin(PATH_PASCA_ONLINE_PRATINJAU + "?no=" + result.getId()) + "\">" + result.geteFilingNo() + "</a>",
                            result.getPostDateTemp(),
                            result.getPostNo(),
                            result.getmFileType() == null ? "" : result.getmFileType().getDesc(),
                            refNoList,
                            result.getBillingCode(),
                            "<div class=\"btn-actions\">" +
                                    (result.getmStatus().getId().equals(StatusEnum.IPT_DRAFT.name()) ? "<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_PASCA_ONLINE_EDIT + "?no=" + result.getId()) + "\">Ubah</a>" : buttonCetak)

                    });
                }
            }
            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(value = REQUEST_MAPPING_PASCA_ONLINE_PENGHAPUSAN, method = {RequestMethod.POST})
    public void doSearchDataTablesPenghapusan(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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
                            searchCriteria.add(new KeyValue(searchBy, value, false));
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

            GenericSearchWrapper<TxPostReception> searchResult = pascaOnlineService.searchPostReceptionNote(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxPostReception result : searchResult.getList()) {
                    no++;
                    data.add(new String[]{
                            result.getNote(),
                    });
                }
            }
            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_PASCA_ONLINE_AJAX_SEARCH_LIST_REFERENSI, method = {RequestMethod.GET})
    public void doSearchDataTablesListReferensi(final Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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
            GenericSearchWrapper<TxTmGeneral> searchResult = null;
            searchResult = pascaOnlineService.searchRefReception(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                String regNo = "";
                for (TxTmGeneral result : searchResult.getList()) {
                    regNo = "-";
                    if (result.getTxRegistration() != null) {
                        regNo = result.getTxRegistration().getNo();
                    }

                    no++;
                    data.add(new String[]{
                            "" + no,
                            result.getApplicationNo(),
                            regNo,
                            result.getId()
                            //"<button type='button' class=\"btn-referensi\" paramId=\"" + result.getId() + "\" " +
                            //		" data-dismiss='modal'>Pilih</button>"
                    });
                }
            }
            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_PASCA_ONLINE_AJAX_SEARCH_LIST_PREVIEW_REFERENSI, method = {RequestMethod.POST})
    public void doSearchDataTablesListPreviewReferensi(final Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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
            String isNew = "";
            String input = "";

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
                    if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                        if (StringUtils.isNotBlank(value)) {
                            if (searchBy.equalsIgnoreCase("txPostReception.id")) {
                                TxPostReceptionDetail txPostReceptionDetail = txPostReceptionDetailCustomRepository.selectOne("id", value);
                                if (txPostReceptionDetail == null) {
                                    isNew = "1";
                                } else {
                                    isNew = "0";
                                    value = txPostReceptionDetail.getTxPostReception().getId();
                                }
                            }
                            searchCriteria.add(new KeyValue(searchBy, value, true));
                            input = value;
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
            GenericSearchWrapper<TxPostReceptionDetail> searchResult = null;

            if (isNew.equalsIgnoreCase("0")) {
                searchResult = pascaOnlineService.searchPostReceptionDetail(searchCriteria, orderBy, orderType, offset, limit);
                if (searchResult.getCount() > 0) {
                    dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                    dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                    int no = offset;
                    String regNo = "";


                    for (TxPostReceptionDetail result : searchResult.getList()) {
                        regNo = "";
                        if (result.getTxTmGeneral().getTxRegistration() != null) {
                            regNo = result.getTxTmGeneral().getTxRegistration().getNo();
                        }

                        no++;
                        data.add(new String[]{
                                result.getTxTmGeneral().getId(),
                                "" + no,
                                result.getTxTmGeneral().getApplicationNo(),
                                result.getTxTmGeneral().getTxTmBrand() == null ? "" : result.getTxTmGeneral().getTxTmBrand().getName(),
                                regNo
                                //"<button type='button' class=\"btn-referensi\" paramId=\"" + result.getId() + "\" " +
                                //		" data-dismiss='modal'>Pilih</button>"
                        });
                    }
                }

            } else {
                data.add(new String[]{
                        "NEW",
                        "NEW2",
                        "-",
                        "-",
                        "-"
                        //"<button type='button' class=\"btn-referensi\" paramId=\"" + result.getId() + "\" " +
                        //		" data-dismiss='modal'>Pilih</button>"
                });
            }


            dataTablesSearchResult.setData(data);


            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_PASCA_ONLINE_AJAX_SEARCH_LIST_KUASA, method = {RequestMethod.GET})
    public void doSearchDataTablesListKuasa(final Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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
            List<KeyValue> searchCriteria = new ArrayList<>();
            searchCriteria.add(new KeyValue("statusFlag", true, false));

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
                            searchCriteria.add(new KeyValue(searchBy, value, false));
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
            GenericSearchWrapper<MRepresentative> searchResult = null;
            searchResult = pascaOnlineService.searchRepresentative(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (MRepresentative result : searchResult.getList()) {
                    no++;
                    data.add(new String[]{
                            "" + no,
                            result.getNo(),
                            result.getName(),
                            result.getAddress(),
                            "<button type='button' class=\"btn btn-primary btn-kuasa\" paramId=\"" + result.getId() + "\" " +
                                    " data-dismiss='modal'>Pilih</button>"
                    });
                }
            }
            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_PASCA_ONLINE_AJAX_SEARCH_LIST_PEMOHON, method = {RequestMethod.POST})
    public void doSearchDataTablesListPemohon(final Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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
            String txtmGeneralID = "";
            List<KeyValue> searchCriteria = new ArrayList<>();
//            searchCriteria.add(new KeyValue("status", true, false));

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
                            searchCriteria.add(new KeyValue(searchBy, value, false));
                        }

                        if (searchBy.equalsIgnoreCase("txTmGeneral.id")){
                            txtmGeneralID = value;
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
            /*List<TxTmOwner> txTmOwnerList = permohonanService.selectAllOwnerByIdGeneral(txTmGeneral.getId());*/
            GenericSearchWrapper<TxTmOwner> searchResult = null;
            searchResult = pascaOnlineService.searchOwner(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                String detail_name = "";
                for (TxTmOwner result : searchResult.getList()) {
                    detail_name = "-";
                    DataOwner dOwner = new DataOwner();
                    dOwner.setOwnerName(result.getName());
                    for (TxTmOwnerDetail txTmOwnerDetail : result.getTxTmOwnerDetails()) {
                        if (detail_name.equalsIgnoreCase("-")) {
                            detail_name = txTmOwnerDetail.getName();
                        } else {
                            detail_name = detail_name + "; " + txTmOwnerDetail.getName();
                        }
                    }

                    dOwner.setId(result.getId());
                    dOwner.setOwnerDetailName(detail_name);
                    dOwner.setOwnerAddress(result.getAddress());
                    dOwner.setOwnerCountry(result.getmCountry() != null ? result.getmCountry().getName() : "");
                    dOwner.setOwnerPhone(result.getPhone());
                    String status = String.valueOf(result.isStatus() ? "Aktif" : "Tidak Aktif");

                    no++;
                    data.add(new String[]{
                            "" + no,
                            dOwner.getId(),
                            dOwner.getOwnerName(),
                            dOwner.getOwnerAddress(),
                            dOwner.getOwnerPhone(),
                            dOwner.getOwnerCountry()
                    });
                }
            }
            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_PASCA_ONLINE_AJAX_SEARCH_LIST_LICENSE, method = {RequestMethod.POST})
    public void doSearchDataTablesListLicense(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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

            GenericSearchWrapper<TxLicense> searchResult = licenseService.selectAll(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxLicense result : searchResult.getList()) {
                    no++;

                    data.add(new String[]{
                            "" + no,
                            result.getName(),
                            result.getTxLicenseParent() == null ? "Utama" : result.getTxLicenseParent().getName(),
                            result.getAddress(),
                            result.getPhone(),
                            result.getmCountry().getName()
                    });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_PASCA_ONLINE_AJAX_SEARCH_LIST_DOKUMEN, method = {RequestMethod.GET})
    public void doSearchDataTablesListDokumen(final Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            DataTablesSearchResult dataTablesSearchResult = new DataTablesSearchResult();
            try {
                dataTablesSearchResult.setDraw(Integer.parseInt(request.getParameter("draw")));
            } catch (NumberFormatException e) {
                dataTablesSearchResult.setDraw(0);
            }

            String postId = request.getParameter("postId");

            List<TxPostDoc> txPostDocList = pascaOnlineService.getAllDocByPostId("txPostReception.id", postId);
            List<String[]> data = new ArrayList<>();

            if (txPostDocList.size() > 0) {
                dataTablesSearchResult.setRecordsFiltered(txPostDocList.size());
                dataTablesSearchResult.setRecordsTotal(txPostDocList.size());

                int no = 0;
                for (TxPostDoc result : txPostDocList) {
                    no++;
                    String pathFolder = DateUtil.formatDate(result.getTxPostReception().getCreatedDate(), "yyyy/MM/dd/");
                    File file = new File(uploadFileDocPascaPath + pathFolder + result.getFileNameTemp());
                    String image = "";

                    if (file.exists() && !file.isDirectory()) {
                        fileInputStreamReader = new FileInputStream(file);
                        byte[] bytes = new byte[(int) file.length()];
                        fileInputStreamReader.read(bytes);
                        image = "data:application/pdf;base64" + "," + Base64.getEncoder().encodeToString(bytes);
                    }

                    data.add(new String[]{
                            result.getmDocType().getId(),
                            " " + no,
                            DateUtil.formatDate(result.getUploadDate(), "dd/MM/yyyy"),
                            result.getmDocType().getName(),
                            result.getFileName(),
                            result.getDescription(),
                            result.getFileSize(),
                            image
                    });
                }
            }

            dataTablesSearchResult.setData(data);
            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_PASCA_ONLINE_CHECK_BILLING, method = {RequestMethod.POST})
    public void doCheckCodeBilling(final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            String statusError = null;
            String billingCode = request.getParameter("billingCode");
            TxPostReception txPostReception = pascaOnlineService.selectOneTxPostReception("billingCode", billingCode);
            Map<String, String> dataGeneral = new HashMap<>();

            if (txPostReception != null) {
                statusError = "Kode Billing '" + billingCode + "' Sudah Digunakan oleh Nomor Transaksi Pasca "+txPostReception.geteFilingNo();
            } else {
                try {
                    String result = simpakiService.getQueryBilling(billingCode);
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
                            statusError = "Kode Billing '" + billingCode + "' Masih dalam proses verifikasi bank. Mohon tunggu beberapa saat lagi";
                        } else if (!flagUsed.equalsIgnoreCase("BELUM")) {
                            statusError = "Kode Billing '" + billingCode + "' Sudah Digunakan";
                        } else {
                            String paymentDate = dataNode.get("tgl_pembayaran").toString().replaceAll("\"", "");
                            String currentDate = DateUtil.formatDate(new Timestamp(System.currentTimeMillis()), "yyyy-MM-dd HH:mm:ss");
                            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            LocalDateTime locPaymentDate = LocalDateTime.parse(paymentDate, format);
                            LocalDateTime locCurrentDate = LocalDateTime.parse(currentDate, format);
                            Duration duration = Duration.between(locPaymentDate, locCurrentDate);
                            String feeCode = dataNode.get("kd_tarif").toString().replaceAll("\"", "");
                            MPnbpFeeCode mPnbpFeeCode = simpakiService.getPnbpFeeCodeByCode(feeCode);

                            if (mPnbpFeeCode == null) {
                                statusError = "Kode Tarif '" + feeCode + "' Tidak Ditemukan";
                            } else if (mPnbpFeeCode.getmFileType() == null) {
                                statusError = "Kode Billing '" + billingCode + "' adalah untuk Permohonan Online";
                            } else {
                                MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

                                String transactionNo = dataNode.get("nomor_transaksi").toString().replaceAll("\"", "");
                                String unit = dataNode.get("satuan").toString().replaceAll("\"", "");

                                String totalClass = dataNode.get("volume").toString().replaceAll("\"", "");
                                String totalPayment = dataNode.get("total_pembayaran").toString().replaceAll("\"", "");
                                String ownerName = dataNode.get("nama").toString().replaceAll("\"", "");
                                String ownerEmail = dataNode.get("email").toString().replaceAll("\"", "");
                                String ownerPhone = dataNode.get("no_tlp").toString().replaceAll("\"", "");
                                String ownerProvince = dataNode.get("kd_provinsi").toString().replaceAll("\"", "");
                                String ownerCity = dataNode.get("kd_kabupaten").toString().replaceAll("\"", "");
                                String ownerAddress = dataNode.get("alamat").toString().replaceAll("\"", "");
                                String ownerFlag = dataNode.get("flag_warga").toString().replaceAll("\"", "");

                                dataGeneral.put("mFileSequence", mUser.getmFileSequence().getId());
                                dataGeneral.put("mFileType", mPnbpFeeCode.getmFileType() == null ? null : mPnbpFeeCode.getmFileType().getId());
                                dataGeneral.put("mFileTypeDetail", mPnbpFeeCode.getmFileTypeDetail() == null ? null : mPnbpFeeCode.getmFileTypeDetail().getId());
                                dataGeneral.put("mLaw", lawService.findByStatusFlagTrue().get(0).getId());

                                dataGeneral.put("totalClass", totalClass);
                                dataGeneral.put("transactionNo", transactionNo.equalsIgnoreCase("null") ? null : transactionNo.toString());
                                dataGeneral.put("unit", unit);

                                dataGeneral.put("totalPayment", totalPayment);
                                dataGeneral.put("totalPaymentTemp", NumberUtil.formatDecimal(NumberUtil.parseDecimal(totalPayment)));
                                dataGeneral.put("paymentDate", DateUtil.formatDate(DateUtil.toDate("yyyy-MM-dd HH:mm:ss", paymentDate), "dd/MM/yyyy HH:mm:ss"));
                                dataGeneral.put("ownerName", ownerName);
                                dataGeneral.put("ownerEmail", ownerEmail);
                                dataGeneral.put("ownerPhone", ownerPhone);

                                if (ownerFlag.equalsIgnoreCase("WNI")) {
                                    MCountry mCountry = pascaOnlineService.selectOneCountryByCode("ID");
                                    MProvince mProvince = pascaOnlineService.selectOneProvinceByCode(ownerProvince);
                                    MCity mCity = pascaOnlineService.selectOneCityByCode(ownerCity);

                                    dataGeneral.put("ownerCountry", mCountry == null ? null : mCountry.getId());
                                    dataGeneral.put("ownerProvince", mProvince == null ? null : mProvince.getId());
                                    dataGeneral.put("ownerCity", mCity == null ? null : mCity.getId());
                                    dataGeneral.put("ownerAddress", ownerAddress);
                                } else {
                                    dataGeneral.put("ownerCountry", "99");
                                    dataGeneral.put("ownerProvince", null);
                                    dataGeneral.put("ownerCity", null);
                                    dataGeneral.put("ownerAddress", null);
                                }
                            }
                        }
                    } else {
                        //statusError = code.equals("02") ? "Kode Billing '" + billingCode + "' Tidak Ditemukan" : message;
                        statusError = code.equals("02") ? "Klik tombol 'Check' Kode Billing dan periksa data kode billing" : message;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    statusError = "Pengecekan Kode Billing '" + billingCode + "' Gagal";
                }
            }

            if (statusError != null) {
                dataGeneral.put("statusError", statusError);
            }

            writeJsonResponse(response, dataGeneral);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_PASCA_ONLINE_CHOOSE_REFERENSI, method = {RequestMethod.GET})
    public void doChooseReferensi(final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            String appId = request.getParameter("target");
            TxTmGeneral txTmGeneral = pascaOnlineService.selectOneDataReferensi("id", appId);

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

    @RequestMapping(path = REQUEST_MAPPING_PASCA_ONLINE_CHOOSE_KUASA, method = {RequestMethod.GET})
    public void doChooseKuasa(final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            String reprsId = request.getParameter("target");
            MRepresentative mRepresentative = representativeService.selectOne("id", reprsId);

            if (mRepresentative.getmCountry() != null) {
                mRepresentative.getmCountry().setCreatedBy(null);
            }
            if (mRepresentative.getmProvince() != null) {
                mRepresentative.getmProvince().setCreatedBy(null);
            }
            if (mRepresentative.getmCity() != null) {
                mRepresentative.getmCity().setCreatedBy(null);
            }
            mRepresentative.setUserId(null);
            mRepresentative.setCreatedBy(null);

            writeJsonResponse(response, mRepresentative);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_PREVIEW_PASCA_ONLINE_CHOOSE_PEMOHON, method = {RequestMethod.GET})
    public void doChoosePemohon(final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            String ownerId = request.getParameter("target");
            TxTmOwner txTmOwners = ownerService.selectOneOwner("id", ownerId);

            if (txTmOwners.getmCountry() != null) {
                txTmOwners.getmCountry().setCreatedBy(null);
            }
            if (txTmOwners.getNationality() != null) {
                txTmOwners.getNationality().setCreatedBy(null);
            }
            if (txTmOwners.getmProvince() != null) {
                txTmOwners.getmProvince().setCreatedBy(null);
            }
            if (txTmOwners.getmCity() != null) {
                txTmOwners.getmCity().setCreatedBy(null);
                txTmOwners.getmCity().getmProvince().setCreatedBy(null);
            }
            if (txTmOwners.getPostCountry() != null) {
                txTmOwners.getPostCountry().setCreatedBy(null);
            }
            if (txTmOwners.getPostProvince() != null) {
                txTmOwners.getPostProvince().setCreatedBy(null);
            }
            if (txTmOwners.getPostCity() != null) {
                txTmOwners.getPostCity().setCreatedBy(null);
                txTmOwners.getPostCity().getmProvince().setCreatedBy(null);
            }
            txTmOwners.setTxTmOwnerDetails(null);
            txTmOwners.setCreatedBy(null);
            txTmOwners.setTxTmGeneral(null);

            writeJsonResponse(response, txTmOwners);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_PASCA_ONLINE_SAVE_GENERAL, method = {RequestMethod.POST})
    @ResponseBody
    public void doSaveFormGeneral(@RequestBody TxPostReception txPostReception, final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            Map<String, Object> result = new HashMap<>();
            try {
                if ("PERPANJANGAN".equalsIgnoreCase(txPostReception.getmFileType().getCurrentId()) ||
                        "PERPANJANGAN_6_BULAN_KADALUARSA".equalsIgnoreCase(txPostReception.getmFileType().getCurrentId()) ||
                        "PENGAJUAN_KEBERATAN".equalsIgnoreCase(txPostReception.getmFileType().getCurrentId())) {
                    if (txPostReception.getApplicationIdListTemp().size() > 1) {
                        result.put("errorMessage", "Hanya dapat diisi 1 (satu) Nomor Permohonan");
                        throw new Exception();
                    }
                } else {
                    if (txPostReception.getApplicationIdListTemp().size() > 1) {
                        result.put("errorMessage", "Hanya dapat diisi 1 (satu) Nomor Permohonan");
                        throw new Exception();
                    }
                }

                for (DataGeneral dataGeneral : txPostReception.getApplicationIdListTemp()) {
                    TxTmGeneral txTmGeneral = pascaOnlineService.selectOneDataReferensi("id", dataGeneral.getApplicationId());
                    TxRegistration txRegistration = txTmGeneral.getTxRegistration();
                    String mFileTypeId =txPostReception.getmFileType().getCurrentId();

                    if (txRegistration != null) {
                        //Perpanjangan Jangka Waktu Perlindungan Merek/Merek Kolektif (IDM)
                        if ("PERPANJANGAN".equalsIgnoreCase(txPostReception.getmFileType().getCurrentId())) {
							/*for testing :
							Date earlyPayment = DateUtils.addDays(backmonths6, -1) ;
							if(earlyPayment.after(backmonths6)) {
								throw new Exception();
							}*/

                           //Jumlah kelas dari simpaki sesuai dg total class di tx_reception,
                            //if (Integer.valueOf(txPostReception.getTotalClassTemp()) != txTmGeneral.getTxReception().getTotalClass()) {
                            int totalKelas = txTmGeneralCustomRepository.countKelas(txTmGeneral).intValue();
                            if (Integer.valueOf(txPostReception.getTotalClassTemp()) != totalKelas) {
                                result.put("errorMessage", "Total kelas yang dibayarkan tidak sesuai dengan Permohonan yang terdaftar.");
                                throw new Exception();
                            }

                            //Tanggal pembayaran harus <= 6 bulan dari reg_end_date (tx_registration)
							/*Jika tanggal pembayaran simpaki > reg_end_date, maka muncul popup tidak bisa disimpan
							-> Tanggal pembayaran simpaki adalah field tanggal pembayaran di form general
							-> Tanggal reg_end_date adalah tanggal batas akhir registrasi dari tx_registration yg didapat dari generate / klik sertifikat di menu monitor. */

						    if (txPostReception.getCreatedDate().before(AddOneDay(DateUtils.addMonths(txRegistration.getEndDate(), -7).toInstant()))) {
                                result.put("errorMessage", "Permohonan ini tidak dapat diperpanjang karena perpanjangan dapat dilakukan jika <= 6 bulan sebelum Tanggal Akhir Perlindungan ");
                                throw new Exception();
                            }
                            if (txPostReception.getCreatedDate().after(AddOneDay(txRegistration.getEndDate().toInstant()))) {
                                result.put("errorMessage", "Silakan lakukan perpanjangan dengan Tipe Permohonan : Perpanjangan 6 Bulan Kadaluarsa");
                                throw new Exception();
                            }
                        }

                        /*Perpanjangan Jangka Waktu Perlindungan Merek/Merek Kolektif (IDM) 6 Bulan Kadaluarsa */
                        if ("PERPANJANGAN_6_BULAN_KADALUARSA".equalsIgnoreCase(txPostReception.getmFileType().getCurrentId())) {
                            Date nextmonths6 = DateUtils.addMonths(txRegistration.getEndDate(), 6);
                            //for testing :
							/*Date latePayment = DateUtils.addDays(nextmonths6, 1) ;
							if(latePayment.after(nextmonths6)) {
								result.put("errorMessage", "Tanggal Pembayaran simpaki >= 6 bulan dari Tanggal Akhir Registrasi");
								throw new Exception();
							}*/
                            //end testing
//
                            if (txPostReception.getCreatedDate().before(AddOneDay(DateUtils.addMonths(txRegistration.getEndDate(), 0).toInstant()))) {
                                result.put("errorMessage", "Permohonan tidak dapat diperpanjang menggunakan Kode Billing ini");
                                throw new Exception();
                            }

                            if (txPostReception.getCreatedDate().before(AddOneDay(DateUtils.addMonths(txRegistration.getEndDate(), -7).toInstant()))) {
                                result.put("errorMessage", "Permohonan ini tidak dapat diperpanjang karena perpanjangan dapat dilakukan jika <= 6 bulan sebelum Tanggal Akhir Perlindungan ");
                                throw new Exception();
                            }


                            //Jika tanggal pembayaran simpaki > reg_end_date, maka muncul popup tidak bisa disimpan
                            if (txPostReception.getCreatedDate().after(AddOneDay(nextmonths6.toInstant()))) {
                                result.put("errorMessage", "Permohonan Merek ini sudah Expired / Habis Masa Perlindungan.");
                                throw new Exception();
                            }

                            //Reg_end_date>=Tanggal pembayaran harus >= 6 bulan (tx_registration). Tidak boleh >= 6 bulan dari reg_end_date
//                            if (txPostReception.getPaymentDate().after(nextmonths6)) {
//                                result.put("errorMessage", "Tanggal Pembayaran simpaki >= 6 bulan dari Tanggal Akhir Registrasi");
//                                throw new Exception();
//                            }

                           //Jumlah kelas dari simpaki sesuai dg total class di tx_tm_class
                            //if (Integer.valueOf(txPostReception.getTotalClassTemp()) != txTmGeneral.getTxReception().getTotalClass()) {
                            int totalKelas = txTmGeneralCustomRepository.countKelas(txTmGeneral).intValue();
                            if (Integer.valueOf(txPostReception.getTotalClassTemp()) != totalKelas) {
                                result.put("errorMessage", "Total kelas yang dibayarkan tidak sesuai dengan Permohonan yang terdaftar.");
                                throw new Exception();
                            }
                        }
                        if (!"PERMOHONAN_PETIKAN_RESMI".equalsIgnoreCase(txPostReception.getmFileType().getCurrentId()) && txRegistration.getTxTmGeneral().getmStatus().getId().equalsIgnoreCase("229")) {
                            result.put("errorMessage", "Nomor Permohonan ini kadaluarsa, tidak dapat mengajukan pasca permohonan");
                            throw new Exception();
                        }
                        if(mFileTypeId.equalsIgnoreCase(DISPENSASI_PERPANJANGAN_ID)) {
                            if (txRegistration.getEndDate().before(DateUtil.toDate("23-03-2020")) || txRegistration.getEndDate().after(DateUtil.toDate("29-05-2020"))) {
                                result.put("errorMessage", "Tidak dapat menggunakan tipe permohonan ini, karena Tanggal Berakhir Perlindungan tidak mendapatkan dispensasi.");
                                throw new Exception();
                            }
                            int totalKelas = txTmGeneralCustomRepository.countKelas(txTmGeneral).intValue();
                            if (Integer.valueOf(txPostReception.getTotalClassTemp()) != totalKelas) {
                                result.put("errorMessage", "Total kelas yang dibayarkan tidak sesuai dengan Permohonan yang terdaftar.");
                                throw new Exception();
                            }
                        }

                    } else {
                        if ("PERPANJANGAN".equalsIgnoreCase(txPostReception.getmFileType().getCurrentId()) ||
                                "PERPANJANGAN_6_BULAN_KADALUARSA".equalsIgnoreCase(txPostReception.getmFileType().getCurrentId())) {
                            result.put("errorMessage", "Permohonan Merek ini tidak dapat diperpanjang.");
                            throw new Exception();
                        }
                    }

                    TxPubsJournal txPubsJournal = trademarkService.selectOnePubJournalByAppId(dataGeneral.getApplicationId());
                    /*Pengajuan Keberatan atas Permohonan Merek/Merek Kolektif/IG */
                    if ("PENGAJUAN_KEBERATAN".equalsIgnoreCase(txPostReception.getmFileType().getCurrentId())) {
                        //Tanggal pembayaran harus<= tx_pubs_journal.journal_end_date, jika tidak muncul popup
                        if (txPubsJournal == null) {
                            result.put("errorMessage", "Pengajuan tidak dapat dilakukan karena permohonan belum dipublikasikan.");
                            throw new Exception();
                        } else if (txPostReception.getPaymentDate().after(AddOneDay(txPubsJournal.getJournalEnd().toInstant()))) {
                            result.put("errorMessage", "Pengajuan tidak dapat dilakukan karena masa pengumuman telah berakhir.");
                            throw new Exception();
                        }

                      /*  //Nomor permohonan simpaki harus sama dengan nomor permohonan di tx_reception
                        if (!txPostReception.getTransactionNoTemp().equals(txTmGeneral.getTxReception().getApplicationNo())) {
                            result.put("errorMessage", "Nomor Transaksi tidak sama dengan Nomor Permohonan");
                            throw new Exception();
                        } comment by fitria, nomor transaksi boleh beda antara simpaki & aplikasi merek*/
                    }
                }

                String txPostDetail = txPostReception.getId();

                MUser user = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

                txPostReception.setOnlineFlag(true);
                txPostReception.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                txPostReception.setCreatedBy(user);

                if (txPostReception.getmFileTypeDetail() != null && (txPostReception.getmFileTypeDetail().getCurrentId() == null || txPostReception.getmFileTypeDetail().getCurrentId().equals(""))) {
                    txPostReception.setmFileTypeDetail(null);
                }
                if (txPostReception.getmFileType() != null && (txPostReception.getmFileType().getCurrentId() == null || txPostReception.getmFileType().getCurrentId().equals(""))) {
                    txPostReception.setmFileType(null);
                }
                if (txPostReception.getmFileSequence() != null && (txPostReception.getmFileSequence().getCurrentId() == null || txPostReception.getmFileSequence().getCurrentId().equals(""))) {
                    txPostReception.setmFileSequence(user.getmFileSequence());
                }

                pascaOnlineService.saveOrUpdateGeneral(txPostReception);

                String postresult = pascaOnlineService.selectOneTxPostReception("id", txPostReception.getId()).getPostNo();


                if (postresult != null) {
                    pascaOnlineService.saveOrUpdateGeneralDetail2(txPostReception, txPostDetail);
                    String noIPT = txPostReception.getId();

                    TxPostReception txP = txPostReceptionCustomRepository.selectOne("eFilingNo", noIPT);
                    TxPostReceptionDetail txD = txPostReceptionDetailCustomRepository.selectOne("txPostReception.id", txP.getId());


                    TxTmGeneral txG = txD.getTxTmGeneral();
                    String application_id = txG.getId();
                    String application_no = txG.getApplicationNo();


                    Map<String, String> resultsuccess = new HashMap<>();
                    resultsuccess.put("noIPT", noIPT);
                    resultsuccess.put("ref_application_id", application_id);
                    resultsuccess.put("ref_application_no", application_no);
                    writeJsonResponse(response, resultsuccess);
                } else
                    writeJsonResponse(response, 500);

//                TxPostReception txPostReception2 = pascaOnlineService.selectOneTxPostReception("id", postId);


            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                writeJsonResponse(response, 500);
            } catch (Exception e) {
                if (!result.isEmpty()) {
                    writeJsonResponse(response, result);
                } else {
                    logger.error(e.getMessage(), e);
                    writeJsonResponse(response, 500);
                }
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }
    
    @RequestMapping(path = REQUEST_MAPPING_PASCA_ONLINE_SAVE_GENERAL_V2, method = {RequestMethod.POST})
    @ResponseBody
    public void doSaveFormGeneral2(@RequestBody TxPostReception txPostReception, final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            Map<String, Object> result = new HashMap<>();
            try {
            	
            	pascaOnlineService.saveOrUpdateGeneralDetail3(txPostReception);
                
                Map<String, String> resultsuccess = new HashMap<>();
                resultsuccess.put("message", "success");
                writeJsonResponse(response, resultsuccess);
                
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                writeJsonResponse(response, 500);
            } catch (Exception e) {
                if (!result.isEmpty()) {
                    writeJsonResponse(response, result);
                } else {
                    logger.error(e.getMessage(), e);
                    writeJsonResponse(response, 500);
                }
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_PASCA_ONLINE_SAVE_PEMOHON, method = {RequestMethod.POST})
    public void doSaveFormPemohon(@RequestBody TxPostOwner txPostOwner, final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            try {

                String xx = txPostOwner.getTxPostReception().getId();

                TxPostReception txPostReception = txPostReceptionCustomRepository.selectOne("id", xx);
                txPostOwner.setTxPostReception(txPostReception);

                txPostOwner.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                txPostOwner.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

                if (txPostOwner.getmCity() != null && (txPostOwner.getmCity().getCurrentId() == null || "".equalsIgnoreCase(txPostOwner.getmCity().getCurrentId()))) {
                    txPostOwner.setmCity(null);
                }
                if (txPostOwner.getmProvince() != null && (txPostOwner.getmProvince().getCurrentId() == null || "".equalsIgnoreCase(txPostOwner.getmProvince().getCurrentId()))) {
                    txPostOwner.setmProvince(null);
                }
                pascaOnlineService.saveOrUpdatePemohon(txPostOwner);

                writeJsonResponse(response, 200);
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                writeJsonResponse(response, 500);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_PASCA_ONLINE_SAVE_KUASA, method = {RequestMethod.POST})
    public void doSaveFormKuasa(@RequestBody TxPostRepresentative txPostRepresentative, final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            try {
                txPostRepresentative.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                txPostRepresentative.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

                if (txPostRepresentative.getmRepresentative() != null && (txPostRepresentative.getmRepresentative().getCurrentId() == null || "".equalsIgnoreCase(txPostRepresentative.getmRepresentative().getCurrentId()))) {
                    txPostRepresentative.setmRepresentative(null);
                }
                pascaOnlineService.saveOrUpdateKuasa(txPostRepresentative);

                writeJsonResponse(response, 200);
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                writeJsonResponse(response, 500);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_PRATINJAU_PASCA_ONLINE_SAVE_DOKUMEN, method = {RequestMethod.POST})
    public void doSaveFormPratinjauDokumen(@RequestParam("postId") String postId, @RequestParam("docList") String docList,
                                           final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {

        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            try {
                TxPostReception txPostReception = pascaOnlineService.selectOneTxPostReception("id", postId);

                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(docList);

                String appId = "<belum ada data>";
                String brand = "<belum ada data>";
                String nm = "<belum ada data>";
                String em = "<belum ada data>";
                String filenm = "<belum ada data>";
                String doctype = "<belum ada data>";
                String tglupload = "<belum ada data>";

                //validasi
                for (JsonNode node : rootNode) {
                    String docId = node.get("docId").toString().replaceAll("\"", "");
                    String docDate = node.get("docDate").toString().replaceAll("\"", "");
                    String docFileName = node.get("docFileName").toString().replaceAll("\"", "");
                    String docDesc = node.get("docDesc").toString().replaceAll("\"", "");
                    String docFileSize = node.get("docFileSize").toString().replaceAll("\"", "");
                    String[] docFile = request.getParameter("file-" + docId).split(",");

                    TxPostDoc txPostDoc = new TxPostDoc();
                    txPostDoc.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                    txPostDoc.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                    txPostDoc.setTxPostReception(txPostReception);
                    txPostDoc.setmDocType(doclampiranService.getDocTypeById(docId));
                    txPostDoc.setDescription(docDesc);
                    txPostDoc.setUploadDate(DateUtil.toDate("dd/MM/yyyy", docDate));
                    txPostDoc.setFileDoc(docFile[1]);
                    txPostDoc.setFileName(docFileName);
                    txPostDoc.setFileSize(docFileSize);
                    txPostDoc.setStatus(false);

                    pascaOnlineService.savePratinjauPascaDokumen(txPostDoc);
                    filenm = txPostDoc.getFileName() == null ? filenm : txPostDoc.getFileName();
                    doctype = txPostDoc.getmDocType().getName() == null ? doctype : txPostDoc.getmDocType().getName();
                    appId = txPostReception.getPostNo() == null ? appId : txPostReception.getPostNo();
                    nm = txPostReception.getTxPostOwner() == null ? nm : txPostReception.getTxPostOwner().getName();
                    em = txPostDoc.getCreatedBy().getEmail();
                    tglupload = txPostDoc.getCreatedDate() == null ? tglupload : txPostDoc.getCreatedDate().toString();

                }

                writeJsonResponse(response, 200);

                // olly@docotel.com
                String logo = "static/img/" + logoEmailImage;
                File file = new File(pathImage + logoEmailImage);
                if (file.exists()) {
                    logo = pathImage + logoEmailImage;
                }
                emailService.prepareAndSendForTambahLampiran(em, "DJKI-Notifikasi Email", nm == null ? em : nm, filenm, appId, doctype, brand, tglupload, logo, "eTemplateTambahDokumenPascaPermohonan");
                // olly@docotel.com


            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                writeJsonResponse(response, 500);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                writeJsonResponse(response, 500);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_PASCA_ONLINE_VALIDATE_DOKUMEN, method = {RequestMethod.POST})
    public void doValidateDokumen(@RequestParam("postId") String postId, @RequestParam("docList") String docList,
                                  final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(docList);

                List<TxPostDoc> listTxPostDoc = new ArrayList<>();
                for (JsonNode node : rootNode) {
                    String docId = node.get("docId").toString().replaceAll("\"", "");
                    String[] docFile = request.getParameter("file-" + docId).split(",");
                    InputStream stream = new ByteArrayInputStream(Base64.getDecoder().decode((docFile[1].getBytes())));
                    com.itextpdf.text.pdf.PdfReader reader = new com.itextpdf.text.pdf.PdfReader(stream);
                    if (reader.isEncrypted()) {
                        writeJsonResponse(response, 405);
                        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                        return;
                    }
                }
                writeJsonResponse(response, 200);
                response.setStatus(HttpServletResponse.SC_OK);
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                writeJsonResponse(response, 501);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                writeJsonResponse(response, 502);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_PASCA_ONLINE_SAVE_DOKUMEN, method = {RequestMethod.POST})
    public void doSaveFormDokumen(@RequestParam("postId") String postId, @RequestParam("docList") String docList,
                                  final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            try {
                TxPostReception txPostReception = pascaOnlineService.selectOneTxPostReception("id", postId);

                KeyValue msg = new KeyValue();
                msg.setKey("Success");
                msg.setValue("Input Data Success");

                String docIdTempF19 = "";

                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(docList);

                List<TxPostDoc> listTxPostDoc = new ArrayList<>();
                for (JsonNode node : rootNode) {
                    String docId = node.get("docId").toString().replaceAll("\"", "");
                    String docDate = node.get("docDate").toString().replaceAll("\"", "");
                    String docFileName = node.get("docFileName").toString().replaceAll("\"", "");
                    String docDesc = node.get("docDesc").toString().replaceAll("\"", "");
                    String docFileSize = node.get("docFileSize").toString().replaceAll("\"", "");
                    String[] docFile = request.getParameter("file-" + docId).split(",");

                    TxPostDoc txPostDoc = new TxPostDoc();
                    txPostDoc.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                    txPostDoc.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                    txPostDoc.setTxPostReception(txPostReception);
                    txPostDoc.setmDocType(doclampiranService.getDocTypeById(docId));
                    txPostDoc.setDescription(docDesc);
                    txPostDoc.setUploadDate(DateUtil.toDate("dd/MM/yyyy", docDate));
                    txPostDoc.setFileDoc(docFile[1]);
                    txPostDoc.setFileName(docFileName);
                    txPostDoc.setFileSize(docFileSize);
                    txPostDoc.setStatus(true);

                    if(docId.equalsIgnoreCase("F19")) {
                        docIdTempF19 = docId;
                    }

                    listTxPostDoc.add(txPostDoc);
                }

                pascaOnlineService.saveOrUpdateDokumen(postId, listTxPostDoc);

                if(txPostReception.getmFileTypeDetail().getId().equalsIgnoreCase("UMKM") && (docIdTempF19.isEmpty()) ) {
                    msg.setKey("fileTypeError");
                    msg.setValue("requiredDocTypeDP");
                }

                writeJsonResponse(response, msg);
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                writeJsonResponse(response, 500);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                writeJsonResponse(response, 500);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_PASCA_ONLINE_SAVE_RESUME, method = {RequestMethod.POST})
    public void doSaveFormResume(@RequestParam("postId") String postId, @RequestParam("postNote") String postNote, @RequestParam("mFileType") String FileType,
                                 final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) throws IOException {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);

            TxPostReception txPostReception = pascaOnlineService.selectOneTxPostReception("id", postId);
            List<KeyValue> searchCriteria = new ArrayList<>();
            searchCriteria.add(new KeyValue("mDocType.statusFlag", true, true));
            searchCriteria.add(new KeyValue("mFileType.id", txPostReception.getmFileType().getId(), true));

            List<MDocTypeDetail> listDocType = docTypeService.selectAllDetail(searchCriteria);
            List<TxPostDoc> txTmDocList = pascaOnlineService.getAllDocByPostId("txPostReception.id", postId);
            for (MDocTypeDetail docType : listDocType) {
                boolean check = false;
                if (docType.getmDocType().getMandatoryType() == null) {
                    continue;
                } else if (docType.getmDocType().getMandatoryType().equals("POSTRECEPT")) {
                    check = true;
                } else if (docType.getmDocType().getMandatoryType().equals("POSTOWNER")) {
                    if (txPostReception.getTxPostOwner() == null) {
                        check = true;
                    }
                } else if (docType.getmDocType().getMandatoryType().equals("POSTREPRS")) {
                    if (txPostReception.getTxPostRepresentative() == null) {
                        check = true;
                    }
                }
                if (check) {
                    boolean cek = true;
                    for (TxPostDoc txTmDoc : txTmDocList) {
                        if (docType.getmDocType().getId().equals(txTmDoc.getmDocType().getId())) {
                            cek = false;
                        }
                    }
                    if (cek) {
                        result.put("success", true);
                        result.put("message", "Dokumen " + docType.getmDocType().getName() + " Belum Dilampirkan");
                        writeJsonResponse(response, result);
                        return;
                    }
                }
            }

            txPostReception.setNote(postNote);
            txPostReception.setmStatus(StatusEnum.TM_PASCA_PERMOHONAN.value());
            pascaOnlineService.saveOrUpdateResume(txPostReception);

            //Set used blling code
            simpakiService.setUseBilling(txPostReception.getBillingCode(), txPostReception.getPostNo());

            //CETAK MEREK PASCA SAVE TO PATH
            try {
                List<CetakPasca> listData = new ArrayList<CetakPasca>();
                CetakPasca data = null;

                data = new CetakPasca(1, "Data Permohonan", "Application");
                data.setPostNo(txPostReception.getPostNo());
                data.seteFilingNo(txPostReception.geteFilingNo());
                data.setPostDate(txPostReception.getPostDate());
                data.setPaymentDate(txPostReception.getPaymentDate());
                data.setNote(txPostReception.getNote());
                listData.add(data);

                List<TxPostReceptionDetail> txPostReceptionDetailList = pascaOnlineService.selectAllPostDetail(txPostReception.getId());
                if (txPostReceptionDetailList.size() == 0) {
                    data = new CetakPasca(2, "Pemohon Terkait", "Related Applicant");
                    listData.add(data);
                } else {
                    for (TxPostReceptionDetail txPostReceptionDetail : txPostReceptionDetailList) {
                        TxTmGeneral txTmGeneral = txPostReceptionDetail.getTxTmGeneral();
                        TxRegistration txRegistration = txTmGeneral.getTxRegistration();
                        TxTmBrand txTmBrand = txTmGeneral.getTxTmBrand();
                        TxTmOwner txTmOwner = ownerService.selectActiveOne(txTmGeneral);
                        List<TxTmClass> txTmClassList = pascaOnlineService.getAllTxTmClass("txTmGeneral.id", txTmGeneral.getId());
                        String brandName = "Nama Merek : " + txTmBrand.getName();
                        String ownerName = "Nama Pemilik : " + txTmOwner.getName();
                        String classList = "Rincian Kelas Barang/Jasa : ";

                        Map<Integer, Integer> mapClass = new HashMap<>();
                        for (TxTmClass txTmClass : txTmClassList) {
                            mapClass.put(txTmClass.getmClass().getNo(), txTmClass.getmClass().getNo());
                        }

                        StringBuffer sbClassNoList = new StringBuffer();
                        for (Map.Entry<Integer, Integer> map : mapClass.entrySet()) {
                            sbClassNoList.append(map.getKey());
                            sbClassNoList.append(", ");
                        }

                        if (sbClassNoList.length() > 0) {
                            classList += sbClassNoList.substring(0, sbClassNoList.length() - 2);
                        }

                        data = new CetakPasca(2, "Pemohon Terkait", "Related Applicant");
                        data.setRefAppNo(txTmGeneral.getApplicationNo());
                        data.setRefRegNo(txRegistration == null ? "" : txRegistration.getNo());
                        data.setRefDetail(brandName + "\n" + ownerName + "\n" + classList);
                        listData.add(data);
                    }
                }

                data = new CetakPasca(3, "Kuasa/Konsultan KI", "Representative/IP Consultant");
                if (txPostReception.getTxPostRepresentative() != null) {
                    data.setReprsName(txPostReception.getTxPostRepresentative().getmRepresentative().getName());
                    data.setReprsAddress(txPostReception.getTxPostRepresentative().getmRepresentative().getAddress());
                    data.setReprsEmailPhone(txPostReception.getTxPostRepresentative().getmRepresentative().getEmail() + "\n" + txPostReception.getTxPostRepresentative().getmRepresentative().getPhone());
                }
                listData.add(data);

                data = new CetakPasca(4, "Lampiran", "Attachments");
                List<TxPostDoc> txPostDocList = pascaOnlineService.getAllDocByPostId("txPostReception.id", txPostReception.getId());
                if (txPostDocList.size() == 0) {
                    data.setDocName("\n");
                } else {
                    Collections.sort(txPostDocList, (o1, o2) -> o1.getmDocType().getName().compareTo(o2.getmDocType().getName()));
                    for (TxPostDoc txPostDoc : txPostDocList) {
                        data.setDocName(data.getDocName() == null ? txPostDoc.getmDocType().getName() + "\n" : data.getDocName() + txPostDoc.getmDocType().getName() + "\n");
                    }
                }
                listData.add(data);

                ClassLoader classLoader = getClass().getClassLoader();
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("pTitleReport", txPostReception.getmFileType() == null ? "" : txPostReception.getmFileType().getDesc());
                params.put("pPrintDate", DateUtil.formatDate(new Date(), "dd-MM-yyyy"));
                params.put("pSignName", "");
                params.put("pUploadImgPath", downloadImgPath);
                params.put("pCapDjki", "signimage.jpg");

                Resource resource = resourceLoader.getResource("classpath:report/CetakPasca.jasper");
                File file = resource.getFile();
                InputStream jasperStream1 = new FileInputStream(file);
                JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream1);
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JRBeanCollectionDataSource(listData));

                ByteArrayOutputStream output = new ByteArrayOutputStream();

                JRPdfExporter exporter = new JRPdfExporter();
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(output));
                exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                exporter.exportReport();

                byte[] resultBytes;
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

                    for (TxPostDoc txPostDoc : txPostDocList) {
                        if (txPostDoc.getFileName() != null) {
                            String pathFolder = DateUtil.formatDate(txPostDoc.getTxPostReception().getCreatedDate(), "yyyy/MM/dd/");
                            PdfReader nonJasperReader = new PdfReader(uploadFileDocPascaPath + pathFolder + txPostDoc.getFileNameTemp()); // Reader for the Pdf to be attached (not .jasper file)
                            for (int page = 0; page < nonJasperReader.getNumberOfPages(); ) {
                                copy.addPage(copy.getImportedPage(nonJasperReader, ++page));
                            }
                            nonJasperReader.close();
                        }
                    }
                    document.close();

                    resultBytes = msOut.toByteArray();
                }

                String folderDoc = downloadFileDocPascaCetakMerekPath + DateUtil.formatDate(txPostReception.getCreatedDate(), "yyyy/MM/dd/");
                String filenameDoc = "CetakPasca-" + txPostReception.geteFilingNo() + ".pdf";

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
                response.setHeader("Content-disposition", "attachment; filename=CetakPasca-" + txPostReception.geteFilingNo() + ".pdf");
                //response.getOutputStream().write(result);
                InputStream is = new ByteArrayInputStream(resultBytes);
                this.signPdf(is, outputStream);
                outputStream.close();
//                System.out.println("file type fit"+ FileType+"postid fit"+postId);
                // Last Function
                if (txPostReception.getmFileType().getId().equalsIgnoreCase("PENCATATAN_PENGALIHAN_HAK")
                        || txPostReception.getmFileType().getId().equalsIgnoreCase("PENCATATAN_PERUBAHAN_NAMA_ALAMAT")
                        ) {
                    TxPostReceptionDetail txPostReceptionDetail = txPostReceptionDetailCustomRepository.selectOneByAppId(postId);
                    String application_id = txPostReceptionDetail.getTxTmGeneral().getId();
                    doSelesaikanPengalihanHak(application_id,postId,txPostReception.getmFileType().getDesc());
                }


            } catch (Exception ex) {
                logger.error(ex);
            }

            writeJsonResponse(response, result);

        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @PostMapping(REQUEST_MAPPING_AJAX_SAVE_TAMBAH_PEMOHON)
    @ResponseBody
    public void doSaveTambahPemohon(@RequestBody TxTmOwner txTmOwner, final HttpServletRequest request, final HttpServletResponse response) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            Timestamp tmstm = new Timestamp(System.currentTimeMillis());
            txTmOwner.setCreatedDate(tmstm);
            txTmOwner.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            String appNo = txTmOwner.getTxTmGeneral().getApplicationNo() ;
            TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(appNo);
            txTmOwner.setTxTmGeneral(txTmGeneral);
            txTmOwner.setStatus(false);
            txTmOwner.setId("KANDIDAT-"+txTmGeneral.getId());
            try {
                if (txTmOwner.getmCity() != null && txTmOwner.getmCity().getCurrentId() == null || "".equalsIgnoreCase(txTmOwner.getmCity().getCurrentId())) {
                    txTmOwner.setmCity(null);
                }
                if (txTmOwner.getmProvince() != null && txTmOwner.getmProvince().getCurrentId() == null || "".equalsIgnoreCase(txTmOwner.getmProvince().getCurrentId())) {
                    txTmOwner.setmProvince(null);
                }

                if (txTmOwner.getPostCity() != null && (txTmOwner.getPostCity().getCurrentId() == null || "".equalsIgnoreCase(txTmOwner.getPostCity().getCurrentId()))) {
                    txTmOwner.setPostCity(null);
                }
                if (txTmOwner.getPostProvince() != null && (txTmOwner.getPostProvince().getCurrentId() == null || "".equalsIgnoreCase(txTmOwner.getPostProvince().getCurrentId()))) {
                    txTmOwner.setPostProvince(null);
                }

                if (txTmOwner.getPostCountry() != null && (txTmOwner.getPostCountry().getCurrentId() == null || "".equalsIgnoreCase(txTmOwner.getPostCountry().getCurrentId()))) {
                    txTmOwner.setPostCountry(null);
                }
                permohonanService.insertPemohonEditPasca(txTmOwner);
                writeJsonResponse(response, 200);
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                writeJsonResponse(response, 500);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_PASCA_ONLINE_SAVE_NOTE_PENGHAPUSAN, method = {RequestMethod.POST})
    public void doSaveFormPenghapusan(@RequestParam("postId") String postId, @RequestParam("postNote") String postNote,
                                      final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            try {
                TxPostReception txPostReception = pascaOnlineService.selectOneTxPostReception("id", postId);
                txPostReception.setNote(postNote);
                pascaOnlineService.saveOrUpdatePenghapusan(txPostReception);

                writeJsonResponse(response, 200);
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                writeJsonResponse(response, 500);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(value = REQUEST_MAPPING_PASCA_ONLINE_CETAK, method = {RequestMethod.GET})
    @ResponseBody
    public String doCetakPasca(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        String postId = request.getParameter("no");
        TxPostReception txPostReception = pascaOnlineService.selectOneTxPostReception("id", postId);

        MUser userLogin = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userLogin.getUsername() != "super") {
            List<KeyValue> criteriaList = new ArrayList<>();
            criteriaList.add(new KeyValue("id", postId, true));
            criteriaList.add(new KeyValue("createdBy", userLogin, true));
            txPostReception = pascaOnlineService.selectOneTxPostReception(criteriaList);

            if (txPostReception == null) {
                response.sendRedirect(PATH_AFTER_LOGIN + PATH_PASCA_ONLINE_LIST + "?error=" + "Halaman tidak ditemukan");
                return "";
            }
        }


        response.setContentType("application/pdf");
        response.setHeader("Content-disposition", "attachment; filename=CetakPasca-" + txPostReception.geteFilingNo() + ".pdf");

        String folder = downloadFileDocPascaCetakMerekPath + DateUtil.formatDate(txPostReception.getCreatedDate(), "yyyy/MM/dd/");
//        System.out.println(folder);
        String fileName = "CetakPasca-" + txPostReception.geteFilingNo() + ".pdf";

        try (OutputStream output = response.getOutputStream()) {
            Path path = Paths.get(folder + fileName);
            Files.copy(path, output);
            output.flush();
        } catch (IOException e) {
        }

        return "";
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

    private String doEditPengalihanHak(String postId) {
        String result;

        TxPostReceptionDetail txPostReceptionDetail = txPostReceptionDetailCustomRepository.selectOneByAppId(postId);
        String application_id = txPostReceptionDetail.getTxTmGeneral().getId();

        try {
            NativeQueryModel queryModel = new NativeQueryModel();
            queryModel.setTable_name("TX_TM_OWNER");
            ArrayList<KeyValue> updateQ = new ArrayList<>();
            KeyValue updateq1 = new KeyValue();
            updateq1.setKey("TM_OWNER_STATUS");
            updateq1.setValue("0");
            updateQ.add(updateq1);
            queryModel.setUpdateQ(updateQ);
            ArrayList<KeyValueSelect> searchBy = new ArrayList<>();
            searchBy.add(new KeyValueSelect("APPLICATION_ID", application_id, "=", true, null));
            queryModel.setSearchBy(searchBy);
            nativeQueryRepository.updateNavite(queryModel);

            result = application_id;
        } catch (NullPointerException n) {
            result = "0";
        } catch (Exception e) {
            result = "0";
        }

        return result;
    }

    private int doSelesaikanPengalihanHak(String application_id, String post_reception_id, String fileType) {
        int result;
        // diambil dari E-Filling No alias /*[[${dataGeneral.id}]]*/'',


        try {
            NativeQueryModel queryModela = new NativeQueryModel();
            queryModela.setTable_name("TX_TM_OWNER");
            ArrayList<KeyValue> updateQ = new ArrayList<>();
            KeyValue updateq1a = new KeyValue();
            updateq1a.setKey("TM_OWNER_STATUS");
            updateq1a.setValue("0");
            KeyValue updateq2a = new KeyValue();
            updateq2a.setKey("TM_OWNER_ID");
            updateq2a.setValue(UUID.randomUUID().toString());
            KeyValue updateq3a = new KeyValue();
            updateq3a.setKey("POST_RECEPTION_ID");
            updateq3a.setValue(post_reception_id);
            KeyValue updateq4a = new KeyValue();
            updateq4a.setKey("FILE_TYPE_DESC");
            updateq4a.setValue(fileType);

            updateQ.add(updateq1a);
            updateQ.add(updateq2a);
            updateQ.add(updateq3a);
            updateQ.add(updateq4a);

            queryModela.setUpdateQ(updateQ);
            ArrayList<KeyValueSelect> searchBya = new ArrayList<>();
            searchBya.add(new KeyValueSelect("TM_OWNER_ID", "KANDIDAT-" + application_id, "=", true, null));
            queryModela.setSearchBy(searchBya);
            nativeQueryRepository.updateNavite(queryModela);

            result = 1;
        } catch (NullPointerException n) {
            result = 0;
        } catch (Exception e) {
            result = 0;
        }

        return result;
    }

    private Timestamp AddOneDay(Instant timestampInstant) {
        return (Timestamp.from(timestampInstant.plus(1, ChronoUnit.DAYS)));
    }

}