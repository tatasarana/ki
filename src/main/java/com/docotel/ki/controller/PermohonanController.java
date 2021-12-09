package com.docotel.ki.controller;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.enumeration.ClassStatusEnum;
import com.docotel.ki.enumeration.PriorStatusEnum;
import com.docotel.ki.enumeration.StatusEnum;
import com.docotel.ki.model.master.*;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.pojo.*;
import com.docotel.ki.pojo.CetakJSON.JSONTemplate;
import com.docotel.ki.pojo.CetakXML.Other;
import com.docotel.ki.pojo.CetakXML.TradeMark;
import com.docotel.ki.pojo.CetakXML.Transaction;
import com.docotel.ki.repository.NativeQueryRepository;
import com.docotel.ki.repository.custom.master.MClassDetailCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxMonitorCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmClassCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmGeneralCustomRepository;
import com.docotel.ki.repository.transaction.TxPubsJournalRepository;
import com.docotel.ki.service.master.*;
import com.docotel.ki.service.transaction.*;
import com.docotel.ki.signature.PDFSignatureFacade;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.util.FieldValidationUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class PermohonanController extends BaseController {
    public static final String PATH_MONITOR = "/list-monitor";
    public static final String PATH_CETAK_SERTIFIKAT = "/cetak-sertifikat";
    public static final String PATH_CETAK_MEREK = "/cetak-merek";
    public static final String PATH_CETAK_MADRID = "/cetak-madrid";
    public static final String PATH_CETAK_SURAT_PERNYATAAN = "/cetak-surat-pernyataan";
    private static final String PATH_DOWNLOAD_DOKUMEN_LAMPIRAN_PETUGAS = "/download-dokumen-lampiran-petugas";
    /*private static final String PATH_DOWNLOAD_DOKUMEN = "/download-dokumen";*/
    public static final String PATH_AJAX_LIST_KOTA = "/list-kota";
    public static final String PATH_AJAX_SEARCH_LIST = "/cari-permohonan";
    private static final String PATH_PROSES_PERMOHONAN_MONITOR_SEARCH_LIST="/cari-proses-permohonan-monitor";
    
    //    public static final String PATH_AJAX_LIST_KUASA = "/list-kuasa";
//    public static final String PATH_AJAX_LIST_KELAS = "/list-kelas";
    public static final String PATH_EDIT_PERMOHONAN = "/edit-permohonan";
    public static final String PATH_EDIT_UBAH_TIPE = "/detail-ubah-tipe-permohonan";
    public static final String PATH_PRANTINJAU_PERMOHONAN = "/pratinjau-permohonan";
    public static final String PATH_EDIT_PERMOHONAN_SELESAI = "/edit-permohonan-selesai";
    public static final String PATH_PERMOHONAN = "/list-data-permohonan";
    public static final String PATH_PROSES_PERMOHONAN = "/proses-permohonan";
    public static final String PATH_PINDAH_PERMOHONAN_POST = "/pindah-permohonan-post";
    public static final String PATH_HAPUS_PERMOHONAN_POST = "/hapus-permohonan-post";
    //    public static final String PATH_KUASA = "/kuasa";
    public static final String PATH_AJAX_INSERT_KELAS = "/insert-kelas";
    public static final String PATH_AJAX_INSERT_URAIAN = "/insert-uraian";
    public static final String PATH_AJAX_INSERT_DATA_TX_TM_PRIOR = "/insert-data-prioritas";
    public static final String PATH_SAVE_FORM_DOKUMEN = "/insert-data-dokumen";
    public static final String PATH_UPDATE_DATA_DOWNLOAD_DOKUMEN = "/update-data-download-dokumen";
    //    public static final String PATH_PRATINJAU_SAVE_FORM_DOKUMEN = "/pratinjau-insert-data-dokumen";
    public static final String PATH_AJAX_INSERT_DATA_TX_TM_BRAND = "/insert-data-brand";
    public static final String PATH_AJAX_INSERT_DATA_TX_TM_BRAND_CHILD = "/insert-data-brand-child";
    public static final String PATH_AJAX_INSERT_DATA_FORM3 = "/insert-data-form3";
    public static final String PATH_AJAX_SAVE_EDIT_FORM3 = "/save-edit-form3";
    public static final String PATH_AJAX_SAVE_TAMBAH_KUASA = "/save-tambah-kuasa";
    public static final String PATH_AJAX_HAPUS_DATA_FORM3 = "/hapus-data-form3";
    //    public static final String PATH_AJAX_LIST_PRIORITAS = "/list-prioritas";
//    public static final String PATH_AJAX_LIST_DOKUMEN = "/list-dokumen";
    public static final String PATH_AJAX_CARI_LIST_DATA_DOKUMEN = "/cari-list-data-dokumen";
    public static final String PATH_AJAX_LIST_DELETE_PRIORITAS = "/delete-prioritas";
    //    public static final String PATH_AJAX_LIST_TXCLASS = "/list-txclass";
    public static final String PATH_AJAX_INSERT_MEREK = "/insert-merek";
    public static final String PATH_REDIRECT = "/layanan/edit-permohonan";
    public static final String REDIRECT_PATH_PERMOHONAN = "redirect:" + PATH_AFTER_LOGIN + PATH_PERMOHONAN;
    private static final String DIRECTORY_PAGE = "permohonan/";
    private static final String PAGE_EDIT_PERMOHONAN = DIRECTORY_PAGE + "edit-permohonan";
    private static final String PAGE_PERMOHONAN = DIRECTORY_PAGE + "permohonan";
    private static final String PAGE_PROSES_PERMOHONAN = DIRECTORY_PAGE + "proses-permohonan";
    private static final String PAGE_DETAIL_UBAH_TIPE = DIRECTORY_PAGE + "detail-ubah-tipe-permohonan";
    private static final String PATH_EXPORT_DATA_PERMOHONAN = "/cetak-permohonan";
    private static final String PATH_EXPORT_XML_DATA_PERMOHONAN = "/cetak-xml-permohonan";
    private static final String PATH_EXPORT_JSON_DATA_PERMOHONAN = "/cetak-json-permohonan";
    private static final String PATH_EXPORT_DATA_PROSES_PERMOHONAN = "/cetak-proses-permohonan";
    private static final String PATH_EXPORT_DATA_PROSES_PERMOHONAN_PILIHAN ="/cetak-pilih-proses-permohonan";
    
    private static final String REQUEST_MAPPING_AJAX_LIST_KOTA = PATH_AJAX_LIST_KOTA + "*";
    private static final String REQUEST_MAPPING_AJAX_SEARCH_LIST = PATH_AJAX_SEARCH_LIST + "*";
    //    private static final String REQUEST_MAPPING_AJAX_KUASA_LIST = PATH_AJAX_LIST_KUASA + "*";
//    private static final String REQUEST_MAPPING_AJAX_LIST_KELAS = PATH_AJAX_LIST_KELAS + "*";
    private static final String REQUEST_MAPPING_AJAX_INSERT_KELAS = PATH_AJAX_INSERT_KELAS + "*";
    private static final String REQUEST_MAPPING_AJAX_INSERT_URAIAN = PATH_AJAX_INSERT_URAIAN + "*";
    private static final String REQUEST_MAPPING_AJAX_INSERT_DATA_TX_TM_PRIOR = PATH_AJAX_INSERT_DATA_TX_TM_PRIOR + "*";
    private static final String REQUEST_MAPPING_AJAX_INSERT_DATA_DOKUMEN = PATH_SAVE_FORM_DOKUMEN + "*";
    private static final String REQUEST_MAPPING_AJAX_UPDATE_DATA_DOWNLOAD_DOKUMEN = PATH_UPDATE_DATA_DOWNLOAD_DOKUMEN + "*";
    //    private static final String REQUEST_MAPPING_AJAX_PRATINJAU_INSERT_DATA_DOKUMEN = PATH_PRATINJAU_SAVE_FORM_DOKUMEN + "*";
    private static final String REQUEST_MAPPING_AJAX_INSERT_DATA_TX_TM_BRAND = PATH_AJAX_INSERT_DATA_TX_TM_BRAND + "*";
    private static final String REQUEST_MAPPING_AJAX_INSERT_DATA_TX_TM_BRAND_CHILD = PATH_AJAX_INSERT_DATA_TX_TM_BRAND_CHILD + "*";
    private static final String REQUEST_MAPPING_AJAX_INSERT_DATA_FORM3 = PATH_AJAX_INSERT_DATA_FORM3 + "*";
    private static final String REQUEST_MAPPING_AJAX_SAVE_EDIT_FORM3 = PATH_AJAX_SAVE_EDIT_FORM3 + "*";
    private static final String REQUEST_MAPPING_AJAX_SAVE_TAMBAH_KUASA = PATH_AJAX_SAVE_TAMBAH_KUASA + "*";
    private static final String REQUEST_MAPPING_AJAX_HAPUS_DATA_FORM3 = PATH_AJAX_HAPUS_DATA_FORM3 + "*";
    //    private static final String REQUEST_MAPPING_AJAX_LIST_PRIORITAS = PATH_AJAX_LIST_PRIORITAS + "*";
//    private static final String REQUEST_MAPPING_AJAX_LIST_DOKUMEN = PATH_AJAX_LIST_DOKUMEN + "*";
    private static final String REQUEST_MAPPING_AJAX_CARI_LIST_DATA_DOKUMEN = PATH_AJAX_CARI_LIST_DATA_DOKUMEN + "*";
    private static final String REQUEST_MAPPING_AJAX_DELETE_PRIORITAS = PATH_AJAX_LIST_DELETE_PRIORITAS + "*";
    private static final String REQUEST_MAPPING_EDIT_PERMOHONAN = PATH_EDIT_PERMOHONAN + "*";
    private static final String REQUEST_MAPPING_EDIT_PERMOHONAN_SELESAI = PATH_EDIT_PERMOHONAN_SELESAI + "*";
    private static final String REQUEST_MAPPING_PERMOHONAN = PATH_PERMOHONAN + "*";
    private static final String REQUEST_MAPPING_PROSES_PERMOHONAN = PATH_PROSES_PERMOHONAN + "*";
    private static final String REQUEST_MAPPING_PINDAH_PERMOHONAN_POST = PATH_PINDAH_PERMOHONAN_POST + "*";
    private static final String REQUEST_MAPPING_HAPUS_PERMOHONAN_POST = PATH_HAPUS_PERMOHONAN_POST + "*";
    /*private static final String REQUEST_MAPPING_DOWNLOAD_DOKUMEN = PATH_DOWNLOAD_DOKUMEN + "*";*/
//    private static final String REQUEST_MAPPING_KUASA = PATH_KUASA + "*";
//    private static final String REQUEST_MAPPING_AJAX_LIST_TXCLASS = PATH_AJAX_LIST_TXCLASS + "*";
    private static final String REQUEST_MAPPING_AJAX_INSERT_MEREK = PATH_AJAX_INSERT_MEREK + "*";
    private static final String REQUEST_MAPPING_DETAIL_UBAH_TIPE = PATH_EDIT_UBAH_TIPE + "*";
    private static final String REQUEST_EXPORT_PERMOHONAN = PATH_EXPORT_DATA_PERMOHONAN + "*";
    private static final String REQUEST_EXPORT_XML_PERMOHONAN = PATH_EXPORT_XML_DATA_PERMOHONAN + "*";
    private static final String REQUEST_EXPORT_JSON_PERMOHONAN = PATH_EXPORT_JSON_DATA_PERMOHONAN + "*";
    private static final String REQUEST_MAPPING_CETAK_MEREK = PATH_CETAK_MEREK + "*";
    private static final String REQUEST_MAPPING_CETAK_MADRID = PATH_CETAK_MADRID + "*";
    private static final String REQUEST_MAPPING_CETAK_SURAT_PERNYATAAN = PATH_CETAK_SURAT_PERNYATAAN + "*";
    private static final String REQUEST_MAPPING_DOWNLOAD_DOKUMEN_LAMPIRAN_PETUGAS = PATH_DOWNLOAD_DOKUMEN_LAMPIRAN_PETUGAS + "*";
    private static final String REQUEST_MAPPING_PROSES_PERMOHONAN_MONITOR_SEARCH_LIST=PATH_PROSES_PERMOHONAN_MONITOR_SEARCH_LIST+ "*";
    private static final String REQUEST_EXPORT_PROSES_PERMOHONAN = PATH_EXPORT_DATA_PROSES_PERMOHONAN+ "*";
    private static final String REQUEST_EXPORT_SELECTED_PROSES_PERMOHONAN = PATH_EXPORT_DATA_PROSES_PERMOHONAN_PILIHAN+ "*";
    
    
    @Autowired
    private NativeQueryRepository nativeQueryRepository;

    @Autowired
    private TxTmClassCustomRepository txTmClassCustomRepository ;

    @Autowired
    private MClassDetailCustomRepository mClassDetailCustomRepository;

    @Autowired
    private TrademarkService trademarkService;
    @Autowired
    private StatusService statusService;
    @Autowired
    private LawService lawService;
    @Autowired
    private FileService fileService;
    @Autowired
    private MActionService mActionService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private ClassService classService;
    @Autowired
    private RepresentativeService representativeService;
    @Autowired
    private CountryService countryService;
    @Autowired
    private DokumenLampiranService dokumenLampiranService;
    @Autowired
    private ProvinceService provinceService;
    @Autowired
    private CityService cityService;
    @Autowired
    private DocTypeService docTypeService;
    @Autowired
    private PermohonanService permohonanService;
    @Autowired
    private PriorService priorService;
    @Autowired
    private DokumenLampiranService doclampiranService;
    @Autowired
    private ReprsService reprsService;
    @Autowired
    private RepresentativeService mReprsService;
    @Autowired
    private OwnerService ownerService;
    @Autowired
    private LookupService lookupService;
    @Autowired
    private MonitorService monitorService ;
    @Autowired
    TxMonitorCustomRepository txMonitorCustomRepository;
    @Autowired
    private TxTmGeneralCustomRepository txTmGeneralCustomRepository;

    @Value("${upload.file.path:}")
    private String uploadFilePathTemp;
    @Value("${upload.file.path.signature:}")
    private String uploadFilePathSignature;

    @Value("${upload.file.ipasimage.path:}")
    private String uploadFileIpasPath;
    @Value("${download.output.cetakmerek.file.path:}")
    private String downloadFileDocCetakMerekPath;
    @Value("${upload.file.brand.path:}")
    private String uploadFileBrandPath;
    @Value("${upload.file.branddetail.path:}")
    private String uploadFileBrandDetailPath;
    @Value("${upload.file.doc.application.path:}")
    private String uploadFileDocApplicationPath;
    @Value("${upload.file.doc.application.external.path:}")
    private String uploadFileDocApplicationExternalPath;
    @Value(("${certificate.file}"))
    private String CERTIFICATE_FILE;
    private FileInputStream fileInputStreamReader;
    @Value("${download.output.permohonan.cetakmerek.file.path:}")
    private String downloadFileDocPermohonanCetakMerekPath;
    @Value("${upload.file.image.tandatangan.path}")
    private String uploadFileImageTandaTangan;
    @Value("${download.output.madridOO.cetakmerek.file.path}")
    private String downloadFileDocMadridOOCetakPath;

    @Value("${upload.file.web.image.default.logo:}")
    private String defaultLogo;


    /* --------------------------------------- PERMOHONAN SECTION ---------------------------------------------------*/

    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "permohonanMerek");
        model.addAttribute("subMenu", "permohonan");
    }

    @PostMapping(value = REQUEST_MAPPING_EDIT_PERMOHONAN_SELESAI)
    public void permohonanSelesai(Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        String noPermohonan = request.getParameter("noPermohonan");

        if (isAjaxRequest(request)) {
            TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(noPermohonan);
            TxTmOwner txTmOwner = permohonanService.selectOneOwnerByApplicationNo(txTmGeneral.getId());
            TxTmBrand txTmBrand = brandService.selectOneByAppId(txTmGeneral.getId());
            TxTmClass txTmClass = classService.selectOneByAppId(txTmGeneral.getId());
            TxTmDoc txTmDoc = doclampiranService.selectOneDocByApplicationNo(txTmGeneral.getId());
            KeyValue msg = new KeyValue();
            if (txTmOwner == null) {
                msg.setKey("Error");
                msg.setValue("Tab Pemohon Belum Tersimpan");
                response.setStatus(HttpServletResponse.SC_OK);
                writeJsonResponse(response, msg);
            } else if (txTmBrand == null) {
                msg.setKey("Error");
                msg.setValue("Tab Merek Belum Tersimpan");
                response.setStatus(HttpServletResponse.SC_OK);
                writeJsonResponse(response, msg);
            } else if (txTmClass == null) {
                msg.setKey("Error");
                msg.setValue("Tab Kelas Belum Tersimpan");
                response.setStatus(HttpServletResponse.SC_OK);
                writeJsonResponse(response, msg);
            } else if (txTmDoc == null) {
                msg.setKey("Error");
                msg.setValue("Tab Dokumen Belum Tersimpan");
                response.setStatus(HttpServletResponse.SC_OK);
                writeJsonResponse(response, msg);
            } else {
                txTmGeneral.setmStatus(StatusEnum.TM_DATA_CAPTURE_SELESAI.value());
                trademarkService.updateGeneral(txTmGeneral);
                msg.setKey("Success");
                msg.setValue("Data Capture Dokumen Selesai");
                response.setStatus(HttpServletResponse.SC_OK);
                writeJsonResponse(response, msg);
            }
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_PERMOHONAN)
    public String doShowPagePermohonan(@RequestParam(value = "error", required = false) String error, Model model, final HttpServletRequest request, final HttpServletResponse response) {
        List<MFileSequence> fileSequenceList = fileService.findMFileSequenceByStatusFlagTrue();
        model.addAttribute("fileSequenceList", fileSequenceList);

        List<MFileType> fileTypeList = fileService.findMFileTypeByFileTypeMenu();
        Collections.sort(fileTypeList, (o1, o2) -> o1.getCode().compareTo(o2.getCode()));
        model.addAttribute("fileTypeList", fileTypeList);

        List<MFileTypeDetail> fileTypeDetailList = fileService.findMFileTypeDetailByStatusFlagTrue();
        model.addAttribute("fileTypeDetailList", fileTypeDetailList);

        List<MAction> actionList = mActionService.findAll();
        model.addAttribute("actionList", actionList);

        List<MClass> classList = classService.findAllMClass();
        Collections.sort(classList, (o1, o2) -> o1.getNo().compareTo(o2.getNo()));
        model.addAttribute("classList", classList);

        List<MStatus> statusList = statusService.selectStatus();
        model.addAttribute("statusList", statusList);

        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("errorMessage", error);
        }

        return PAGE_PERMOHONAN;
    }


    //@RequestMapping(path = REQUEST_MAPPING_PROSES_PERMOHONAN)
    @RequestMapping(value = PATH_PROSES_PERMOHONAN, method = {RequestMethod.GET})
    public String doShowPagePindahPermohonan(@RequestParam(value = "error", required = false) String error, Model model, final HttpServletRequest request, final HttpServletResponse response) {
        //System.out.println("doShowPagePindahPermohonan ");

        List<MFileSequence> fileSequenceList = fileService.findMFileSequenceByStatusFlagTrue();
        model.addAttribute("fileSequenceList", fileSequenceList);

        List<MFileType> fileTypeList = fileService.findMFileTypeByFileTypeMenu();
        Collections.sort(fileTypeList, (o1, o2) -> o1.getCode().compareTo(o2.getCode()));
        model.addAttribute("fileTypeList", fileTypeList);

        List<MAction> actionList = mActionService.findAll();
        model.addAttribute("actionList", actionList);

        List<MFileTypeDetail> fileTypeDetailList = fileService.findMFileTypeDetailByStatusFlagTrue();
        model.addAttribute("fileTypeDetailList", fileTypeDetailList);

        List<MClass> classList = classService.findAllMClass();
        Collections.sort(classList, (o1, o2) -> o1.getNo().compareTo(o2.getNo()));
        model.addAttribute("classList", classList);

        List<MStatus> statusList = statusService.selectStatus();
        model.addAttribute("statusList", statusList);
 

        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("errorMessage", error);
        }

        return PAGE_PROSES_PERMOHONAN;
 
    } 

    @RequestMapping(value = REQUEST_MAPPING_AJAX_SEARCH_LIST, method = {RequestMethod.POST})
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
            String orderBy = request.getParameter("order[0][column]");
            String orderType = request.getParameter("order[0][dir]");
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
                        if ((searchBy.equalsIgnoreCase("mFileSequence.id") || searchBy.equalsIgnoreCase("mFileType.id") || searchBy.equalsIgnoreCase("mFileTypeDetail.id") || searchBy.equalsIgnoreCase("mAction.name")) && !value.isEmpty()) {
                            searchCriteria.add(new KeyValue(searchBy, value, true));
                        } else if (searchBy.equalsIgnoreCase("startDate") || searchBy.equalsIgnoreCase("endDate") || searchBy.equalsIgnoreCase("txTmPriorList.priorDate")) {
                            try {
                                searchCriteria.add(new KeyValue(searchBy, DateUtil.toDate("dd/MM/yyyy", value), true));
                            } catch (ParseException e) {
                            }
                        } else {
                            if (StringUtils.isNotBlank(value)) {
                                if (searchBy.equals("txTmBrand.name")) {
                                    value = value.replaceAll("\\*", "_");
                                    orderBy = "4";
                                    orderType = "ASC";
                                }
                                if(searchBy.equalsIgnoreCase("mClassDetail")){
                                    MClassDetail mClassDetail = mClassDetailCustomRepository.selectOne("id", value, true);
                                    TxTmClass txTmClass = txTmClassCustomRepository.selectOne("mClassDetail", mClassDetail, true);
//                                    //System.out.println("dump class " + txTmClass.getmClassDetail().getId());
                                    searchCriteria.add(new KeyValue("mClassDetail", txTmClass.getmClassDetail().getId(), false));
                                }
                                if (searchBy.equalsIgnoreCase("txTmClassList")) {
                                    String[] classList = value.split(",");
                                    for ( int ii = 0; ii < classList.length; ii++ ) {
                                        searchCriteria.add(new KeyValue(searchBy, classList[ii], true));
                                    }
                                } else {
                                    searchCriteria.add(new KeyValue(searchBy, value, false));
                                }
                            }
                        }
                    }
                }
            }

            searchCriteria.add(new KeyValue("statusOnOff", StatusEnum.IPT_DRAFT.name(), true));

            /*
            <th>Nomor Permohonan</th>
            <th>Tanggal Penerimaan</th>
            <th>Tipe Merek</th>
            <th>Nama Merek</th>
            <th>Kelas</th>
            <th>Nama Pemohon</th>
            <th>Status</th>*/

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
                            orderBy = null;
                            break;
                        case "4":
                            orderBy = "txTmBrand.name";
                            break;
                        case "5":
                            orderBy = null;
                            break;
                        case "6":
                            orderBy = "ownerList";
                            break;
                        case "7":
                            orderBy = "represList";
                            break;
                        case "8":
                            orderBy = "mStatus.name";
                            break;
                        case "9":
                            orderBy = "txTmPriorList.priorDate";
                            break;
                        case "10":
                            orderBy = "txRegistration.no";
                            break;
                        case "11":
                            orderBy = "txReception.mFileTypeDetail.id";
                            break;
                        default:
                            orderBy = "filingDate";
                            break;
                    }
                }
            }

            if (orderType == null) {
                orderType = "ASC";
            } else {
                orderType = orderType.trim();
                if (!orderType.equalsIgnoreCase("DESC")) {
                    orderType = "ASC";
                }
            }


            List<String[]> data = new ArrayList<>();

            GenericSearchWrapper<TxTmGeneral> searchResult = trademarkService.searchGeneral(searchCriteria, orderBy, orderType, offset, limit, false);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                AtomicInteger atomicInteger = new AtomicInteger(offset+1);
                if (searchResult !=null){
                searchResult.getList().stream().parallel().forEachOrdered(result -> {
//                    no++;
                    String brandName = "-";
                    FileInputStream brandLabel = null;
                    File file = null;
                    byte[] byteImage = null;
                    String status = "-";
                    String sbOwnerNameList = "-";
                    String sbReprsNameList = "-";
                    String registrationNo = "-";
                    String sbPriorDateList = "-";
                    ArrayList<String> owners = new ArrayList<String>();
                    ArrayList<String> reprs = new ArrayList<String>();
                    ArrayList<String> spriorDate = new ArrayList<String>();
                    String sbClassList = "-";
                    ArrayList<String> kelass = new ArrayList<String>();

                    String filingDate = "";
                    try {
                        filingDate = result.getFilingDateTemp();
                    } catch (NullPointerException e) {
                    } catch (Exception e) {
                    }

                    String jenisPermohonan = " ";
                    try {
                        jenisPermohonan = result.getTxReception().getmFileTypeDetail().getDesc();
                    } catch (NullPointerException e) {
                        jenisPermohonan = " ";
                    } catch (Exception e) {
                    }

                    try {
                        brandName = result.getTxTmBrand().getName();
                    } catch (NullPointerException e) { brandName = "-";} catch (Exception e) { brandName = "-";}

                    try {
                        String pathFolder ="";
                        if(result.getTxTmBrand().getFileName() != null) {
                            pathFolder = DateUtil.formatDate(result.getTxTmBrand().getCreatedDate(), "yyyy/MM/dd/");
                            file = new File (uploadFileBrandPath + pathFolder + result.getTxTmBrand().getId() + ".jpg");
                            if (file.isFile() && file.canRead()) {
                                brandLabel = new FileInputStream(uploadFileBrandPath + pathFolder + result.getTxTmBrand().getId() + ".jpg");
                            } else {
                                file = new File (uploadFileBrandPath + uploadFileIpasPath + result.getTxTmBrand().getId() + ".jpg");
                                if (file.isFile() && file.canRead()) {
                                    brandLabel = new FileInputStream(uploadFileBrandPath + uploadFileIpasPath + result.getTxTmBrand().getId() + ".jpg");
                                } else {
                                    brandLabel = new FileInputStream(defaultLogo);
                                    //System.out.println("File gambar " + result.getTxTmBrand().getId() + ".jpg tidak ditemukan"
                                          //  + " di folder "+ uploadFileBrandPath + pathFolder
                                           // + " maupun di di folder " + uploadFileBrandPath + uploadFileIpasPath );
                                }
                            }



                            byteImage = new byte[brandLabel.available()];
                            brandLabel.read(byteImage);
                            brandLabel.close();
                        }
                        /*String pathFolder = DateUtil.formatDate(result.getTxTmBrand().getCreatedDate(), "yyyy/MM/dd/");
                        ;
                        brandLabel = new FileInputStream(uploadFileBrandPath + pathFolder + result.getTxTmBrand().getId() + ".jpg");
                        byteImage = new byte[brandLabel.available()];
                        brandLabel.read(byteImage);
                        brandLabel.close();*/
                    } catch (NullPointerException e) {
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        status = result.getmStatus() != null ? result.getmStatus().getName() : "-";
                    } catch (NullPointerException e) {
                    }

                    try {
                        registrationNo = result.getTxRegistration().getNo();
                    } catch (NullPointerException e) {
                    }

//                    List<TxTmOwner> txOwnerList =  ownerService.selectOneByIdGeneral(result.getId(), true);
//                    try {
//                        for (TxTmOwner txTmOwner : txOwnerList) {
//                            owners.add("" + txTmOwner.getName());
//                        }
//                        Set<String> temp = new LinkedHashSet<String>(owners);
//                        String[] unique = temp.toArray(new String[temp.size()]);
//                        if (unique.length > 0) {
//                            sbOwnerNameList = String.join(",", unique);
//                        }
//                        /*ownerName = result.getTxTmOwner().getName();*/
//                    } catch (NullPointerException e) {
//                    }

                    sbOwnerNameList = result.getOwnerList();
                    if (sbOwnerNameList == null) {
                        List<TxTmOwner> txOwnerList =  ownerService.selectOneByIdGeneral(result.getId(), true);
                        try {
                            for (TxTmOwner txTmOwner : txOwnerList) {
                                owners.add("" + txTmOwner.getName());
                            }
                            Set<String> temp = new LinkedHashSet<String>(owners);
                            String[] unique = temp.toArray(new String[temp.size()]);
                            if (unique.length > 0) {
                                sbOwnerNameList = String.join(",", unique);
                            }
                        } catch (NullPointerException e) {
                        }
                    }

//                    List<TxTmRepresentative> txTmReprsList =  reprsService.selectOneByTxGeneralId(result.getId());
//                    try {
//                        for (TxTmRepresentative txTmReprs : txTmReprsList) {
//                        	reprs.add("" + txTmReprs.getmRepresentative().getName());
//                        }
//                        Set<String> temp = new LinkedHashSet<String>(reprs);
//                        String[] unique = temp.toArray(new String[temp.size()]);
//                        if (unique.length > 0) {
//                        	sbReprsNameList = String.join(",", unique);
//                        }
//                    } catch (NullPointerException e) {
//                    }
                    sbReprsNameList = result.getRepresList();

//                    try {
//                        for (TxTmClass txTmClass : result.getTxTmClassList()) {
//                            kelass.add("" + txTmClass.getmClass().getNo());
//                        }
//                        Set<String> temp = new LinkedHashSet<String>(kelass);
//                        String[] unique = temp.toArray(new String[temp.size()]);
//                        if (unique.length > 0) {
//                            sbClassList = String.join(",", unique);
//                        }
//
//                    } catch (NullPointerException e) {
//                    }
                    sbClassList = result.getClassList();

//                    try {
//                        for (TxTmPrior txtmPrior : result.getTxTmPriorList()) {
//                            spriorDate.add("" + txtmPrior.getPriorDateTemp());
//                        }
//                        Set<String> temp = new LinkedHashSet<String>(spriorDate);
//                        String[] unique = temp.toArray(new String[temp.size()]);
//                        if (unique.length > 0) {
//                            sbPriorDateList = String.join(",", unique);
//                        }
//
//                    } catch (NullPointerException e) {
//                    }
                    sbPriorDateList = result.getPriorList();

                    // For user role access button menu
//                    MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    String btnUbah;
                    String btnUbahPermohonan = "";
                    if(mUser.hasAccessMenu("P-UT")) {
                        btnUbahPermohonan = "<a class=\"btn btn-default btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT_UBAH_TIPE + "?no=" + result.getApplicationNo()) + "\">Ubah Tipe</a>";
                    }
                    String sFillingDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(new Date(result.getFilingDate().getTime()));
                    if (result.getmStatus() != null) {
//						if (result.getmStatus().getName().equals("(TM) Permohonan Baru")) {
                        if (result.getmStatus().getId().equalsIgnoreCase(StatusEnum.TM_PERMOHONAN_BARU.name())) {
                            btnUbah = "<a class=\"btn btn-default btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT_PERMOHONAN + "?no=" + result.getApplicationNo()) + "\">Ubah</a>";
                            //String btnMonitor = "<a class=\"btn btn-success btn-xs\" href=\"" + getPathURLAfterLogin(PATH_MONITOR) + "?no=" + result.getApplicationNo() + "\">Monitor</a>";

                            data.add(new String[]{
                                    "" + atomicInteger.getAndIncrement(),
                                    "<a  target=\"_blank\" href=\"" + getPathURLAfterLogin(PATH_PRANTINJAU_PERMOHONAN + "?no=" + result.getApplicationNo()) + "\">" + result.getApplicationNo() + "</a>",
                                    filingDate,
                                    (byteImage != null ? "data:image/jpeg;base64, " + Base64Utils.encodeToString(byteImage) + "" : ""),
                                    brandName,
                                    sbClassList,
                                    sbOwnerNameList,
                                    sbReprsNameList,
                                    status,
                                    sbPriorDateList,
                                    registrationNo,
                                    jenisPermohonan,
                                    result.getTxReception().getmFileTypeDetail().getDesc(),
                                    result.getCreatedBy().getUsername(),
                                    "<div class=\"btn-actions\">" + btnUbah + "<br/>" + // "<br/>" + btnMonitor+"<br/>" +
                                            btnUbahPermohonan + "</div>",

                            });
                        } else {
                            String btnMonitor = "<a class=\"btn btn-success btn-xs\" href=\"" + getPathURLAfterLogin(PATH_MONITOR) + "?no=" + result.getApplicationNo() + "\" target=\"_blank\">Monitor</a> ";
                            String btnCetak = "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin(PATH_CETAK_MEREK) + "?no=" + result.getApplicationNo() + "\" target=\"_blank\">Tanda Terima</a>  ";
                            if (jenisPermohonan.equalsIgnoreCase("Madrid OO")) {
                                btnCetak =
                                        "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin(PATH_CETAK_MADRID) + "?no=" + result.getApplicationNo() + "\" target=\"_blank\">Tanda Terima Madrid</a>  ";
                            }
                            String btnCetakSuratPernyataan = "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin(PATH_CETAK_SURAT_PERNYATAAN) + "?no=" + result.getApplicationNo() + "\" target=\"_blank\">Surat Pernyataan</a>";
                            data.add(new String[]{
                                    "" + atomicInteger.getAndIncrement(),
                                    "<a target=\"_blank\" href=\"" + getPathURLAfterLogin(PATH_PRANTINJAU_PERMOHONAN + "?no=" + result.getApplicationNo()) + "\">" + result.getApplicationNo() + "</a>",
                                    filingDate,
                                    (byteImage != null ? "data:image/jpeg;base64, " + Base64Utils.encodeToString(byteImage) + "" : ""),
                                    brandName,
                                    sbClassList,
                                    sbOwnerNameList,
                                    sbReprsNameList,
                                    status,
                                    sbPriorDateList,
                                    registrationNo,
                                    jenisPermohonan,
                                    "<div class=\"btn-actions\">" + ( result.getTxReception().getmFileTypeDetail().getId().equalsIgnoreCase("MD") || (result.getTxReception().getApplicantNotes()!=null && result.getTxReception().getApplicantNotes().equalsIgnoreCase("MIGRATION DATA")) ? "" : btnCetak) + "<br/>" + (result.getTxReception().getmFileTypeDetail().getId().equalsIgnoreCase("MD") || result.getTxReception().getmFileTypeDetail().getId().equalsIgnoreCase("MO") || result.getTxReception().getmFileTypeDetail().getId().equalsIgnoreCase("MADRID_TRP") || (result.getTxReception().getApplicantNotes()!=null && result.getTxReception().getApplicantNotes().equalsIgnoreCase("MIGRATION DATA")) ? "" :  btnCetakSuratPernyataan) + "<br/>" + btnMonitor + "<br/>" +
                                            btnUbahPermohonan + "</div>",
                                    result.getCreatedBy().getUsername()
                            });
                        }
                    } else {
                        String btnMonitor = "<a class=\"btn btn-success btn-xs\" href=\"" + getPathURLAfterLogin(PATH_MONITOR) + "?no=" + result.getApplicationNo() + "\" target=\"_blank\">Monitor</a>";
                        String btnCetak = "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin(PATH_CETAK_MEREK) + "?no=" + result.getApplicationNo() + "\" target=\"_blank\">Tanda Terima</a> ";
                        if (jenisPermohonan.equalsIgnoreCase("Madrid OO")) {
                            btnCetak =
                                    "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin(PATH_CETAK_MADRID) + "?no=" + result.getApplicationNo() + "\" target=\"_blank\">Tanda Terima Madrid</a>  ";
                        }
                        String btnCetakSuratPernyataan = "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin(PATH_CETAK_SURAT_PERNYATAAN) + "?no=" + result.getApplicationNo() + "\" target=\"_blank\">Surat Pernyataan</a>";
                        data.add(new String[]{
                                "" + atomicInteger.getAndIncrement(),
                                "<a target=\"_blank\" href=\"" + getPathURLAfterLogin(PATH_PRANTINJAU_PERMOHONAN + "?no=" + result.getApplicationNo()) + "\">" + result.getApplicationNo() + "</a>",
                                filingDate,
                                (byteImage != null ? "data:image/jpeg;base64, " + Base64Utils.encodeToString(byteImage) + "" : ""),
                                brandName,
                                sbClassList,
                                sbOwnerNameList,
                                sbReprsNameList,
                                status,
                                sbPriorDateList,
                                registrationNo,
                                jenisPermohonan,
                                "<div class=\"btn-actions\">" + (result.getTxReception().getmFileTypeDetail().getId().equalsIgnoreCase("MD") || (result.getTxReception().getApplicantNotes()!=null && result.getTxReception().getApplicantNotes().equalsIgnoreCase("MIGRATION DATA")) ? "" : btnCetak) + "<br/>" + (result.getTxReception().getmFileTypeDetail().getId().equalsIgnoreCase("MD") || result.getTxReception().getmFileTypeDetail().getId().equalsIgnoreCase("MO") || result.getTxReception().getmFileTypeDetail().getId().equalsIgnoreCase("MADRID_TRP") || (result.getTxReception().getApplicantNotes()!=null && result.getTxReception().getApplicantNotes().equalsIgnoreCase("MIGRATION DATA")) ? "" : btnCetakSuratPernyataan) + "<br/>"
                                        + btnMonitor + "<br/>" + btnUbahPermohonan + "</div>",
                                result.getCreatedBy().getUsername()
                        });
                    }
                });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_EDIT_PERMOHONAN, method = RequestMethod.GET)
    public String showEditProduct(Model model, @RequestParam(value = "no", required = true) String no, final HttpServletRequest request, final HttpServletResponse response) {
        TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(no);
        if (txTmGeneral == null) {
            return REDIRECT_PATH_PERMOHONAN + "?error=Nomor permohonan " + no + " tidak ditemukan";
        }
        MLaw mLaw = new MLaw();
        if (txTmGeneral.getmLaw() == null) {
            txTmGeneral.setmLaw(mLaw);
        }
        TxReception txReception = txTmGeneral.getTxReception();
        TxTmOwner txTmOwner = permohonanService.selectOneOwnerByApplicationNo(txTmGeneral.getId());
        List<TxTmOwnerDetail> txTmOwnerDetailList = null;
        if (txTmOwner == null) {
            txTmOwner = new TxTmOwner();
        } else {
            txTmOwnerDetailList = permohonanService.selectAllOwnerByOwnerId(txTmOwner.getId());
        }
        txTmOwner.setTxTmGeneral(txTmGeneral);

        TxTmClass txTmClass = classService.selectOneByAppId(txTmGeneral.getId());

        //Validasi TxTmRepresentative jika data sudah ada dihapus lalu di ganti yang baru
        TxTmRepresentative txTmRepresentative = reprsService.selectOneByApplicationId(txTmGeneral.getId());
        MCountry mCountry = new MCountry();
        MProvince mProvince = new MProvince();
        MCity mCity = new MCity();
        if (txTmClass == null) {
            MClass mClass = new MClass();
            MClassDetail mClassDetail = new MClassDetail();

            txTmClass = new TxTmClass();

            txTmClass.setmClass(mClass);
            txTmClass.setmClassDetail(mClassDetail);
        }
        if (txTmRepresentative == null) {
            MRepresentative mRepresentative = new MRepresentative();

            mRepresentative.setmCity(mCity);
            mRepresentative.setmProvince(mProvince);
            mRepresentative.setmCountry(mCountry);

            txTmRepresentative = new TxTmRepresentative();
            txTmRepresentative.setmRepresentative(mRepresentative);

        } else {
            if (txTmRepresentative.getmRepresentative().getmCountry() == null) {
                txTmRepresentative.getmRepresentative().setmCountry(mCountry);
            }
            if (txTmRepresentative.getmRepresentative().getmProvince() == null) {
                txTmRepresentative.getmRepresentative().setmProvince(mProvince);
            }
            if (txTmRepresentative.getmRepresentative().getmCity() == null) {
                txTmRepresentative.getmRepresentative().setmCity(mCity);
            }
        }
        //List<MFileTypeDetail> mFileTypeDetailList1 = fileService.getAllFileTypeDetailByFileType("mFileType.id", txReception.getmFileType().getId());

        List<MLaw> mLawList = lawService.findByStatusFlagTrue();
        List<MCountry> mCountryList = countryService.findAll();
        List<MProvince> mProvinceList = provinceService.findAll();
        List<MCity> mCityList = cityService.selectAll();
        List<MDocType> mDocTypeList = docTypeService.findByStatusFlagTrue();

        // DOC SECTION //
        List<String[]> listDocType = new ArrayList<>();
        List<TxTmDoc> txtmDocList = doclampiranService.selectAllTxTmDoc("txTmGeneral.id", txTmGeneral.getId());
        for (MDocType result : mDocTypeList) {
            boolean status = txtmDocList == null ? false : txtmDocList.stream().filter(o -> o.getmDocType().getId().equals(result.getId())).findFirst().isPresent();

            listDocType.add(new String[]{
                    result.getId(),
                    result.getName(),
                    status ? "1" : "0"
            });
        }
        /* Sort statement*/
        Collections.sort(listDocType, new Comparator<String[]>() {
            public int compare(String[] strings, String[] otherStrings) {
                return strings[1].compareTo(otherStrings[1]);
            }
        });

        //-----------------------------------------------
        //Brand Section
        List<MBrandType> listMBrandType = brandService.findByStatusFlagTrue();
        TxTmBrand txTmBrand = brandService.selectOneByAppId(txTmGeneral.getId());
        if (txTmBrand == null) {
            txTmBrand = new TxTmBrand();
            txTmBrand.setmBrandType(new MBrandType());
        }
        TxTmBrandDetail txTmBrandDetail = new TxTmBrandDetail();
        try {
            String pathFolder = DateUtil.formatDate(txTmBrand.getCreatedDate(), "yyyy/MM/dd/");
            File file = new File(uploadFileBrandPath + pathFolder + txTmBrand.getId() + ".jpg");
            if (file.exists() && !file.isDirectory()) {
                FileInputStream fileInputStreamReader = new FileInputStream(file);
                byte[] bytes = new byte[(int) file.length()];
                fileInputStreamReader.read(bytes);
                model.addAttribute("imgMerek", "data:image/jpg;base64," + Base64.getEncoder().encodeToString(bytes));
            } else {
                model.addAttribute("imgMerek", "");
            }
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        //End of Brand Section

        //Kelas Section
        List<MClass> listMClass = classService.findByStatusFlagTrue();
        List<MLookup> mKonfirm = lookupService.selectAllbyGroup("UICariKelas");
        List<MClassDetail> listMClassDetail = classService.findClassDetailByStatusFlagTrue();
        Collections.sort(listMClass, (o1, o2) -> new Integer(o1.getNo()).compareTo(new Integer(o2.getNo())));

        String classStatusEnum = "\"acceptValue\":\"" + ClassStatusEnum.ACCEPT.name() + "\",";
        classStatusEnum += "\"acceptLabel\":\"" + ClassStatusEnum.ACCEPT.getLabel() + "\",";
        classStatusEnum += "\"rejectValue\":\"" + ClassStatusEnum.REJECT.name() + "\",";
        classStatusEnum += "\"rejectLabel\":\"" + ClassStatusEnum.REJECT.getLabel() + "\"";
        //End of Kelas Section

        List<MRepresentative> mRepresentatives = representativeService.findByStatusFlagTrue();

        Map<Integer, String[]> listTxTmClass = new HashMap<>();
        int key;
        String desc = "";
        String descEn = "";
        for (TxTmClass result : txTmGeneral.getTxTmClassList()) {
            key = result.getmClass().getNo();
            desc = result.getmClassDetail().getDesc();
            descEn = result.getmClassDetail().getDescEn();

            if (listTxTmClass.containsKey(key)) {
                desc = listTxTmClass.get(key)[0] + ", " + desc;
                descEn = listTxTmClass.get(key)[1] + ", " + descEn;
            }

            listTxTmClass.put(key, new String[]{desc, descEn});
        }
        txTmGeneral.setTxTmPriorList(permohonanService.selectAllPriorByGeneralId(txTmGeneral.getId()));

        List<KeyValue> searchCriteria = new ArrayList<>();
        searchCriteria.add(new KeyValue("menu", "DAFTAR", true));
        searchCriteria.add(new KeyValue("statusFlag", true, true));
        List<MFileType> fileTypeList = fileService.selectAllMFileType(searchCriteria, "desc", "ASC", null, null);
        List<MFileTypeDetail> mFileTypeDetailList1 = fileService.getAllFileTypeDetail();

        model.addAttribute("fileTypeList", fileTypeList);
        model.addAttribute("noGeneral", no);
        model.addAttribute("dataGeneral", txTmGeneral);
        model.addAttribute("dataLoketPenerimaan", txReception);
        model.addAttribute("pemohon", txTmOwner);
        model.addAttribute("fileTypeDetailList", mFileTypeDetailList1);
        model.addAttribute("listLaw", mLawList);
        model.addAttribute("listCountry", mCountryList);
        model.addAttribute("listProvince", mProvinceList);
        model.addAttribute("listCity", mCityList);
        model.addAttribute("listPemohonChild", txTmOwnerDetailList);
        model.addAttribute("txTmReprs", txTmRepresentative);
        model.addAttribute("txTmClass", txTmClass);
        model.addAttribute("txTmBrandDetail", txTmBrandDetail);
        model.addAttribute("txTmBrand", txTmBrand);
        model.addAttribute("listBrandType", listMBrandType);
        model.addAttribute("listDocType", listDocType);
        model.addAttribute("listRepresentative", mRepresentatives);
        model.addAttribute("listMClass", listMClass);
        model.addAttribute("classStatusEnum", "{" + classStatusEnum + "}");
        model.addAttribute("lookupKonfirm", mKonfirm);
        model.addAttribute("listMClassChild", listMClassDetail);
        model.addAttribute("listTxTmClass", listTxTmClass);

        //return PAGE_EDIT_PERMOHONAN+"?error=Nomor permohonan " + no + " tidak ditemukan";
        return PAGE_EDIT_PERMOHONAN;
    }

    @RequestMapping(path = REQUEST_MAPPING_AJAX_LIST_KOTA)
    public void doGetListKota(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            List<MCity> listCity = new ArrayList<>();
            String target = request.getParameter("target");
            GenericSearchWrapper<MClass> searchResult = null;

            if (target != null) {
                target = target.trim();
                if (!target.equalsIgnoreCase("")) {
                    listCity = cityService.selectAllByProvinci("mProvince.id", target);
                }
            }
            List<MCity> data = new ArrayList<>();
            MCity city = null;
            for (MCity result : listCity) {
                city = new MCity();
                city.setId(result.id);
                city.setName(result.name);
                data.add(city);
            }
            writeJsonResponse(response, data);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    /* --------------------------------------- KUASA  --------------------------------------- */
//    @RequestMapping(path = REQUEST_MAPPING_KUASA)
//    public void doGetDataKuasa(Model model, final HttpServletRequest request, final HttpServletResponse response) {
//        MRepresentative mReps = new MRepresentative();
//        String req = request.getParameter("target");
//
//        if (isAjaxRequest(request)) {
//            mReps = representativeService.selectOne("id", req);
//            if (mReps.getmCountry() != null) {
//                mReps.getmCountry().setCreatedBy(null);
//            }
//            if (mReps.getmProvince() != null) {
//                mReps.getmProvince().setCreatedBy(null);
//            }
//            if (mReps.getmCity() != null) {
//                mReps.getmCity().setCreatedBy(null);
//            }
//            mReps.setUserId(null);
//            mReps.setCreatedBy(null);
//        }
//
//        writeJsonResponse(response, mReps);
//    }

//    @RequestMapping(value = REQUEST_MAPPING_AJAX_KUASA_LIST, method = {RequestMethod.GET})
//    public void editPermohonanKuasa(final Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
//        List<MCountry> mCountryList = countryService.findByStatusFlagTrue();
//        model.addAttribute("listCountry", mCountryList);
//
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
//            try {
//                offset = Math.abs(Integer.parseInt(request.getParameter("start")));
//            } catch (NumberFormatException e) {
//            }
//            try {
//                limit = Math.abs(Integer.parseInt(request.getParameter("length")));
//            } catch (NumberFormatException e) {
//            }
//
//            String[] searchByArr = request.getParameterValues("searchByArr[]");
//            String[] keywordArr = request.getParameterValues("keywordArr[]");
//            List<KeyValue> searchCriteria = null;
//
//            if (searchByArr != null) {
//                searchCriteria = new ArrayList<>();
//                for (int i = 0; i < searchByArr.length; i++) {
//                    String searchBy = searchByArr[i];
//                    String value = null;
//                    try {
//                        value = keywordArr[i];
//                    } catch (ArrayIndexOutOfBoundsException e) {
//                    }
//                    if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
//                        if (searchBy.equalsIgnoreCase("applicationDate")) {
//                            try {
//                                searchCriteria.add(new KeyValue(searchBy, DateUtil.toDate("dd/MM/yyyy", value), true));
//                            } catch (ParseException e) {
//                            }
//                        } else {
//                            if (StringUtils.isNotBlank(value)) {
//                                searchCriteria.add(new KeyValue(searchBy, value, false));
//                            }
//                        }
//                    }
//                }
//            }
//
//            String orderBy = request.getParameter("orderBy");
//            if (orderBy != null) {
//                orderBy = orderBy.trim();
//                if (orderBy.equalsIgnoreCase("")) {
//                    orderBy = "orderBy";
//                }
//            }
//
//            String orderType = request.getParameter("orderType");
//            if (orderType == null) {
//                orderType = "ASC";
//            } else {
//                orderType = orderType.trim();
//                if (!orderType.equalsIgnoreCase("DESC")) {
//                    orderType = "ASC";
//                }
//            }
//
//            List<String[]> data = new ArrayList<>();
//
//            GenericSearchWrapper<MRepresentative> searchResult = null;
//            if (searchCriteria.isEmpty()) {
//                searchResult = representativeService.showListAll(searchCriteria, orderBy, orderType, offset, limit);
//            } else {
//                searchResult = representativeService.searchGeneral(searchCriteria, orderBy, orderType, offset, limit);
//            }
//            if (searchResult.getCount() > 0) {
//                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
//                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());
//
//                int no = offset;
//                for (MRepresentative result : searchResult.getList()) {
//                    no++;
//                    data.add(new String[]{
//                            "" + no,
//                            result.getNo(),
//                            result.getName(),
//                            result.getAddress(),
//                            "<button type='button' class=\"btn btn-primary btn-kuasa\" idkuasa=\"" + result.getId() + "\" " +
//                                    " data-dismiss='modal'>Pilih</button>"
//                    });
//                }
//            }
//            dataTablesSearchResult.setData(data);
//
//            writeJsonResponse(response, dataTablesSearchResult);
//        } else {
//            response.setStatus(HttpServletResponse.SC_FOUND);
//        }
//    }


    @RequestMapping(value = REQUEST_MAPPING_AJAX_INSERT_DATA_FORM3, method = {RequestMethod.POST})
    public void simpanKuasa(Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        try {
            String reprsId = request.getParameter("reprsId");
            String appNo = request.getParameter("appNo");
            String txTmReprsId = request.getParameter("txTmReprsId");
            String status = request.getParameter("status");
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            MRepresentative mRepresentative = mReprsService.selectOne("id", reprsId);

            TxTmRepresentative txTmRepresentative = reprsService.selectOneByApplicationId(appNo);
            if (txTmRepresentative == null) {
                txTmRepresentative = new TxTmRepresentative();
                txTmRepresentative.setId(txTmReprsId);
            } else {
            	txTmRepresentative = new TxTmRepresentative();
            	txTmRepresentative = reprsService.selectOne("id", txTmReprsId);
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

            txTmRepresentative.setTxTmGeneral(trademarkService.selectOne("applicationNo", appNo));
            txTmRepresentative.setStatus(Boolean.parseBoolean(status));

            reprsService.simpanKuasa(txTmRepresentative, txTmReprsId);
            writeJsonResponse(response, 200);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            writeJsonResponse(response, 500);
        }
    }

    @RequestMapping(value = REQUEST_MAPPING_AJAX_SAVE_EDIT_FORM3, method = {RequestMethod.POST})
    public void doEditKuasa(Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        try {
            String reprsId = request.getParameter("reprsId");
            String appNo = request.getParameter("appNo");
            String txTmReprsId = request.getParameter("txTmReprsId");
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            MRepresentative mRepresentative = mReprsService.selectOne("id", reprsId);

            TxTmRepresentative txTmRepresentative = reprsService.selectOneByApplicationId(appNo);
            if (txTmRepresentative == null) {
                txTmRepresentative = new TxTmRepresentative();
                txTmRepresentative.setId(txTmReprsId);
            }
            txTmRepresentative.setCreatedDate(timestamp);
            txTmRepresentative.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            txTmRepresentative.setmRepresentative(mRepresentative);
            txTmRepresentative.setTxTmGeneral(trademarkService.selectOne("applicationNo", appNo));
            txTmRepresentative.setStatus(false);


            reprsService.simpanEditKuasa(txTmRepresentative, txTmReprsId);
            writeJsonResponse(response, 200);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            writeJsonResponse(response, 500);
        }
    }

    @RequestMapping(value = REQUEST_MAPPING_AJAX_SAVE_TAMBAH_KUASA, method = {RequestMethod.POST})
    public void doSaveTambah(Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException { //, Map<Integer, String[]> result

        if(isAjaxRequest(request)) {
            setResponseAsJson(response);

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);

            try {
                String reprsId = request.getParameter("reprsId");
                String appNo = request.getParameter("appNo");
                String txTmReprsId = request.getParameter("txTmReprsId");
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());

                MRepresentative mRepresentative = mReprsService.selectOne("id", reprsId);

                TxTmRepresentative txTmRepresentative = reprsService.selectOneByApplicationId(appNo);
                if (txTmRepresentative == null) {
                    txTmRepresentative = new TxTmRepresentative();
                    txTmRepresentative.setId(txTmReprsId);
                }

                TxTmGeneral txTmGeneral = trademarkService.selectOne("applicationNo", appNo);

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
                txTmRepresentative.setCreatedDate(timestamp);
                txTmRepresentative.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                txTmRepresentative.setUpdatedDate(timestamp);
                txTmRepresentative.setUpdatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                txTmRepresentative.setTxTmGeneral(txTmGeneral);
                txTmRepresentative.setStatus(true);

                reprsService.simpanKuasa(txTmRepresentative, txTmReprsId);

                // Note: Ini untuk memperbaiki penambahan kuasa mengubah 'repres_list' pada tx_tm_general menjadi hanya kuasa yang baru ditambah
                if (txTmGeneral.getRepresList() != null) {
                    txTmGeneral.setRepresList(mRepresentative.getName() + ", " + txTmGeneral.getRepresList());
                    txTmGeneralCustomRepository.saveOrUpdate(txTmGeneral);
                }
                result.put("success", true);

                //}
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                result.put("message", e.getMessage());
            }
            writeJsonResponse(response, result);
        }

    }

    @RequestMapping(value = REQUEST_MAPPING_AJAX_HAPUS_DATA_FORM3, method = {RequestMethod.POST})
    public void hapusKuasa(Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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
    /* --------------------------------------- END KUASA --------------------------------------- */

//    /* --------------------------------------- KELAS --------------------------------------- */
//    @RequestMapping(value = REQUEST_MAPPING_AJAX_LIST_KELAS, method = {RequestMethod.POST})
//    //@RequestParam(value = "no", required = false) String appno, -> ditambahkan ini aplikasinya gak jalan
//    //@RequestBody DataForm1 dataform, -> ditambahkan ini aplikasinya gak jalan
//    public void doSearchDataTablesListKelas(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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
//            int limit = 0;
//            try {
//                offset = Math.abs(Integer.parseInt(request.getParameter("start")));
//            } catch (NumberFormatException e) {
//            }
//            try {
//                limit = Math.abs(Integer.parseInt(request.getParameter("length")));
//            } catch (NumberFormatException e) {
//            }
//
//            String[] excludeArr = request.getParameterValues("excludeArr6[]");
//            String[] searchByArr = request.getParameterValues("searchByArr6[]");
//            String[] keywordArr = request.getParameterValues("keywordArr6[]");
//
//            List<KeyValue> searchCriteria = new ArrayList<>();
//            searchCriteria.add(new KeyValue("statusFlag", true, true));
//            searchCriteria.add(new KeyValue("parentClass.statusFlag", true, true));
//
//            if (searchByArr != null) {
//                for (int i = 0; i < searchByArr.length; i++) {
//                    String searchBy = searchByArr[i];
//                    String value = null;
//                    try {
//                        value = keywordArr[i];
//                    } catch (ArrayIndexOutOfBoundsException e) {
//                    }
//                    if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
//                        if (StringUtils.isNotBlank(value)) {
//                            searchCriteria.add(new KeyValue(searchBy, value, false));
//                        }
//                    }
//                }
//            }
//
//            String orderBy = request.getParameter("orderBy");
//            if (orderBy != null) {
//                orderBy = orderBy.trim();
//                if (orderBy.equalsIgnoreCase("")) {
//                    orderBy = "orderBy";
//                }
//            }
//
//            String orderType = request.getParameter("orderType");
//            if (orderType == null) {
//                orderType = "ASC";
//            } else {
//                orderType = orderType.trim();
//                if (!orderType.equalsIgnoreCase("DESC")) {
//                    orderType = "ASC";
//                }
//            }
//
//            List<String[]> data = new ArrayList<>();
//
//            GenericSearchWrapper<MClassDetail> searchResult = null;
//            //searchResult = classService.searchGeneralTest(searchCriteria, orderBy, orderType, offset, limit);
//            searchResult = classService.searchClassChildExclude(searchCriteria, excludeArr, orderBy, orderType, offset, limit);
//
//            if (searchResult.getCount() > 0) {
//                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
//                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());
//
//                int no = offset;
//                for (MClassDetail result : searchResult.getList()) {
//                    no++;
//                    data.add(new String[]{
//                            result.getId(),
//                            "" + result.getParentClass().getNo(),
//                            result.getSerial1(),
//                            result.getDesc(),
//                            result.getDescEn(),
//                            result.getParentClass().getEdition().toString(),
//                            result.getParentClass().getVersion().toString(),
//                            "show",
//                            result.getParentClass().getType()
//                            //"<button type='button' data-parent=\"" + result.getParentClass().getId() + "\" id='pilihKelasBtn' onclick='pilihKelas(\"" + result.getId() + "\")' " +
//                            //        " data-dismiss='modal'>Pilih</button>"
//
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

//    @RequestMapping(value = REQUEST_MAPPING_AJAX_LIST_TXCLASS, method = {RequestMethod.GET})
//    public void doSearchDataTablesListTxClass(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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
//            int limit = 5000;
//            try {
//                offset = Math.abs(Integer.parseInt(request.getParameter("start")));
//            } catch (NumberFormatException e) {
//            }
//            try {
//                limit = Math.abs(Integer.parseInt(request.getParameter("length")));
//            } catch (NumberFormatException e) {
//            }
//
//            String appNo = request.getParameter("appNo");
//
//            String orderBy = "mClass.no, c.mClassDetail.desc";
//            if (orderBy != null) {
//                orderBy = orderBy.trim();
//                if (orderBy.equalsIgnoreCase("")) {
//                    orderBy = "orderBy";
//                }
//            }
//
//            String orderType = request.getParameter("orderType");
//            if (orderType == null) {
//                orderType = "ASC";
//            } else {
//                orderType = orderType.trim();
//                if (!orderType.equalsIgnoreCase("DESC")) {
//                    orderType = "ASC";
//                }
//            }
//
//
//            List<KeyValue> searchCriteria = new ArrayList<>();
//            MUser user = new MUser();
//            user = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//            searchCriteria.add(new KeyValue("txTmGeneral.applicationNo", appNo, true));
//            //searchCriteria.add(new KeyValue("createdBy.id", user.getId(), true));
//
//            List<String[]> data = new ArrayList<>();
//
//            GenericSearchWrapper<TxTmClass> searchResult = null;
//
//            searchResult = classService.searchGeneralTxClass(searchCriteria, orderBy, orderType, offset, limit);
//
//            if (searchResult.getCount() > 0) {
//                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
//                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());
//
//                int no = offset;
//                for (TxTmClass result : searchResult.getList()) {
//                    no++;
//                    data.add(new String[]{
//                            result.getmClassDetail().getId(),
//                            "" + no,
//                            "" + result.getmClass().getNo(),
//                            result.getmClassDetail().getSerial1(),
//                            result.getmClassDetail().getDesc(),
//                            result.getmClassDetail().getDescEn(),
//                            result.getmClass().getEdition().toString(),
//                            result.getmClass().getVersion().toString(),
//                            ClassStatusEnum.valueOf(result.getTransactionStatus()).getLabel(),
//                            result.isCorrectionFlag() ? "TRUE" : "FALSE",
//                            "<button>Hapus</button>"
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

    @RequestMapping(value = "", method = {RequestMethod.POST})
    public void doInsertKelasTable1(Model model, final HttpServletRequest request, final HttpServletResponse response, @RequestParam("appNo") String appNo,
    		@RequestParam("listChildId") String[] listChildId, @RequestParam("listChildStatus") String[] listChildStatus,
    		@RequestParam("listChildNote") String[] listChildNote, @RequestParam("listChildCorretion") String[] listChildCorretion, @RequestParam("listChildDesc")
    		String[] listChildDesc, @RequestParam("listChildDescEn") String[] listChildDescEn) throws IOException {

        List<KeyValue> searchCriteria = new ArrayList<>();
        TxTmGeneral txTmGeneral = trademarkService.selectOne("applicationNo", appNo);
        searchCriteria.add(new KeyValue("txTmGeneral.id", txTmGeneral.getId(), true));
//        GenericSearchWrapper<TxTmClass> searchResult = classService.searchGeneralTxClass(searchCriteria, "id", "ASC", 0, null);

        List<String> listId = new ArrayList<String>();
        if (listChildId != null) {
            for (String classChildId : listChildId) {
                listId.add(classChildId);
            }
        }
        classService.deleteTxTMClassMultipleNotIn(txTmGeneral.getId(), listId.toArray(new String[listId.size()]));
//        if (searchResult.getCount() > 0) {
//            for (TxTmClass result : searchResult.getList()) {
//                classService.deleteTxTMClass(result);
//            }
//        }

        Timestamp tmstm = new Timestamp(System.currentTimeMillis());

        Map<String, MClassDetail> detailMap = new HashMap();

        List<Object[]> listIdDetail = classService.selectTxTMClassMultiple(txTmGeneral.getId(), new String[]{"CLASS_DETAIL_ID", "TM_CLASS_ID", "TM_CLASS_NOTES", "TM_CLASS_STATUS"});




        List<Integer> listNew = new ArrayList();
        List<TxTmClass> listUpdate = new ArrayList();
        int ix = 0;
        for (String idx : listChildId) {
            boolean exist = false;
            TxTmClass txTmClass = new TxTmClass();
            for ( Object[] id_db : listIdDetail) {
                if(idx.equals(id_db[0].toString())) {
                    //System.out.println(listChildNote[ix].equals(id_db[2].toString())  );

                	if(!listChildNote[ix].equals(id_db[2] == null ? "" : id_db[2].toString())) {

                        txTmClass.setNotes(listChildNote[ix]);
                        txTmClass.setId(id_db[1].toString());
                        if(!listChildStatus[ix].equals(id_db[3] == null ? "" : id_db[3].toString())) {
                            txTmClass.setTransactionStatus(listChildStatus[ix]);
                            txTmClass.setId(id_db[1].toString());
                        } else {
                            txTmClass.setTransactionStatus(id_db[3] == null ? "" : id_db[3].toString());
                            txTmClass.setId(id_db[1].toString());
                        }
                        listUpdate.add(txTmClass);
                    } else if (!listChildStatus[ix].equals(id_db[3] == null ? "" : id_db[3].toString())) {
                        txTmClass.setTransactionStatus(listChildStatus[ix]);
                        txTmClass.setId(id_db[1].toString());
                        if(!listChildNote[ix].equals(id_db[2] == null ? "" : id_db[2].toString())) {
                            txTmClass.setNotes(listChildNote[ix]);
                            txTmClass.setId(id_db[1].toString());
                        } else {
                            txTmClass.setNotes(id_db[2] == null ? "" : id_db[2].toString());
                            txTmClass.setId(id_db[1].toString());
                        }
                        listUpdate.add(txTmClass);
                    }

                    MClassDetail mClassDetail;
                    if(detailMap.containsKey(listChildId[ix])) {
                        mClassDetail = detailMap.get(listChildId[ix]);
                    } else {
                        mClassDetail = classService.selectOne("id", listChildId[ix]);
                        detailMap.put(listChildId[ix], mClassDetail);
                    }

//                    if(!mClassDetail.getDesc().equals(listChildDesc[ix]) || mClassDetail.getDescEn().equals(listChildDescEn[ix])) {
//                        mClassDetail.setDesc(listChildDesc[ix]);
//                        mClassDetail.setDescEn(listChildDescEn[ix]);
//                        classService.saveClassChild(mClassDetail);
//                    }
                    exist = true;
                }
            }
            if(!exist) {
                listNew.add(ix);
            }
            ix++;
        }

        if (listChildId != null) {
            //data update
            for (TxTmClass classUpdate : listUpdate) {
                Object[] data = new Object[] {
                        classUpdate.getNotes(),
                        classUpdate.getTransactionStatus()
                };
                classService.updateTxTMClass(classUpdate.getId(),
                        new String[]{
                                "TM_CLASS_NOTES", "TM_CLASS_STATUS"
                        }, new Object[]{
                                classUpdate.getNotes(),
                                classUpdate.getTransactionStatus()
                        }
                );
            }

            // data baru
            int i = 0;
            TxTmClass[] listTxClass =  new TxTmClass[listNew.size()];
            for (int id : listNew) {
                MClassDetail mClassDetail;
                if(detailMap.containsKey(listChildId[id])) {
                    mClassDetail = detailMap.get(listChildId[id]);
                } else {
                    mClassDetail = classService.selectOneMClassDetailById(listChildId[id]);
                    detailMap.put(listChildId[id], mClassDetail);
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
                txTmClass.setTransactionStatus(listChildStatus[id]);
                txTmClass.setCorrectionFlag(listChildCorretion[id].equals("TRUE"));
                txTmClass.setEdition(mClass.getEdition());
                txTmClass.setNotes(listChildNote[id]);
                txTmClass.setVersion(mClass.getVersion());
                //insert kedalam txTmClass
                listTxClass[i] = txTmClass;
                i++;
            }
            classService.saveTxTMClass(listTxClass);
        }
    }
    /* --------------------------------------- END KELAS --------------------------------------- */

    /**
     *
     * Insert Uraian
     *
     * @param model
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = REQUEST_MAPPING_AJAX_INSERT_URAIAN, method = {RequestMethod.POST})
    public void doInsertUraian(Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException {

        if(isAjaxRequest(request)){
            setResponseAsJson(response);

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);

            String appNo = request.getParameter("appNo");
            String serialE = request.getParameter("serialE");
            String serialF = request.getParameter("serialF");
            String classBaseNo = request.getParameter("classBaseNo");
            String kelas = request.getParameter("kelas");
            String status = request.getParameter("status");
            String listChildDesc = request.getParameter("listChildDesc");
            String listChildDescEn = request.getParameter("listChildDescEn");

            List<KeyValue> searchCriteria = new ArrayList<>();
            TxTmGeneral txTmGeneral = trademarkService.selectOne("applicationNo", appNo);
            searchCriteria.add(new KeyValue("txTmGeneral.id", txTmGeneral.getId(), true));
            Timestamp tmstm = new Timestamp(System.currentTimeMillis());
//        GenericSearchWrapper<TxTmClass> searchResult = classService.searchGeneralTxClass(searchCriteria, "id", "ASC", 0, null);


            //MClass mClass = mClassDetail.getParentClass();

        /*String note = "";
        try{
            note = listChildNote ;
        }
        catch(Exception e){
            note = "";
        }*/

            try {
                MClass mClass = classService.findOneMClass(kelas);;

                MClassDetail mClassDetail = new MClassDetail();
                mClassDetail.setSerial1(serialE);
                mClassDetail.setSerial2(serialF);
                mClassDetail.setParentClass(mClass);
                mClassDetail.setDesc(listChildDesc);
                mClassDetail.setDescEn(listChildDescEn);
                mClassDetail.setStatusFlag(false);
                mClassDetail.setClassBaseNo(classBaseNo);
                mClassDetail.setNotes("Perbaikan data kelas");
                classService.saveOrUpdate(mClassDetail);


                TxTmClass txTmClass = new TxTmClass();
                txTmClass.setId(txTmClass.getId());
                txTmClass.setTxTmGeneral(txTmGeneral);
                txTmClass.setmClass(mClass);
                txTmClass.setmClassDetail(mClassDetail);
                txTmClass.setCreatedDate(txTmGeneral.getCreatedDate());
                txTmClass.setCreatedBy(txTmGeneral.getCreatedBy());
                txTmClass.setUpdatedDate(tmstm);
                txTmClass.setUpdatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                txTmClass.setTransactionStatus("ACCEPT");
                //txTmClass.setCorrectionFlag(listChildCorretion.equals("TRUE"));
                txTmClass.setEdition(mClass.getEdition());
                //txTmClass.setNotes(note);
                txTmClass.setVersion(mClass.getVersion());

                classService.saveTxTMClass(txTmClass);
                result.put("success", true);

                writeJsonResponse(response, result);

            }catch (Exception e){
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                e.printStackTrace();
            }
        }else {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }

    }

    /* --------------------------------------- INSERT KELAS ---------------------------------------*/
    @RequestMapping(value = REQUEST_MAPPING_AJAX_INSERT_KELAS, method = {RequestMethod.POST})
    public void doInsertKelasTable(Model model, final HttpServletRequest request, final HttpServletResponse response, @RequestParam("appNo") String appNo,
                                   @RequestParam("listChildId") String[] listChildId, @RequestParam("listChildStatus") String[] listChildStatus,
                                   @RequestParam("listChildNote") String[] listChildNote, @RequestParam("listChildCorretion") String[] listChildCorretion,
                                   @RequestParam("listChildDesc") String[] listChildDesc, @RequestParam("listChildDescEn") String[] listChildDescEn) throws IOException {


        List<KeyValue> searchCriteria = new ArrayList<>();
        TxTmGeneral txTmGeneral = trademarkService.selectOne("applicationNo", appNo);
        searchCriteria.add(new KeyValue("txTmGeneral.id", txTmGeneral.getId(), true));
//        GenericSearchWrapper<TxTmClass> searchResult = classService.searchGeneralTxClass(searchCriteria, "id", "ASC", 0, null);

        List<String> listId = new ArrayList<String>();
        List<Integer> listNew = new ArrayList(); int u =0;

        if (listChildId != null) {
            for (String classChildId : listChildId) {
                listId.add(classChildId);
                listNew.add(u);
                u++;
            }
        }

        int sizeClass = listId.size() ;
        //System.out.println(sizeClass);
        //System.out.println("indeks terakhir: "+u);

        try{
            NativeQueryModel queryModel = new NativeQueryModel() ;
            queryModel.setTable_name("TX_TM_CLASS");

            ArrayList<KeyValueSelect> searchBy = new ArrayList <>();
            searchBy.add(new KeyValueSelect("APPLICATION_ID",txTmGeneral.getId(),"=", true,null));
            queryModel.setSearchBy(searchBy);
            nativeQueryRepository.deleteNative(queryModel);
        }
        catch (NullPointerException n){
        }

        Timestamp tmstm = new Timestamp(System.currentTimeMillis());

        // List<Object[]> listIdDetail = classService.selectTxTMClassMultiple(txTmGeneral.getId(), new String[]{"CLASS_DETAIL_ID", "TM_CLASS_ID", "TM_CLASS_NOTES", "TM_CLASS_STATUS"});

        // List<TxTmClass> listUpdate = new ArrayList();

        if (listChildId != null) {
            //data update
            /* for (TxTmClass classUpdate : listUpdate) {
                Object[] data = new Object[] {
                        classUpdate.getNotes(),
                        classUpdate.getTransactionStatus()
                };
                classService.updateTxTMClass(classUpdate.getId(),
                        new String[]{
                                "TM_CLASS_NOTES", "TM_CLASS_STATUS"
                        }, new Object[]{
                                classUpdate.getNotes(),
                                classUpdate.getTransactionStatus()
                        }
                );
            } */

            // data baru
            int i = 0;
            Map<String, MClassDetail> detailMap = new HashMap();

//            Map<String, String> noteMap = new HashMap();
            TxTmClass[] listTxClass =  new TxTmClass[listNew.size()];
            for (int id : listNew) {
                MClassDetail mClassDetail;
                if(detailMap.containsKey(listChildId[id])) {
                    mClassDetail = detailMap.get(listChildId[id]);
                } else {
                    mClassDetail = classService.selectOneMClassDetailById(listChildId[id]);
                    detailMap.put(listChildId[id], mClassDetail);
                }
                MClass mClass = mClassDetail.getParentClass();

                String note = "";
                try{
                    note = listChildNote[id] ;
                }
                catch(Exception e){
                    note = "";
                }

                mClassDetail.setDesc(listChildDesc[id].replace("|||", ","));
                mClassDetail.setDescEn(listChildDescEn[id].replace("|||", ","));
                classService.saveOrUpdate(mClassDetail);

                TxTmClass txTmClass = new TxTmClass();
                txTmClass.setId(txTmClass.getId());
                txTmClass.setTxTmGeneral(txTmGeneral);
                txTmClass.setmClass(mClass);
                txTmClass.setmClassDetail(mClassDetail);
                txTmClass.setCreatedDate(txTmGeneral.getCreatedDate());
                txTmClass.setCreatedBy(txTmGeneral.getCreatedBy());
                txTmClass.setUpdatedDate(tmstm);
                txTmClass.setUpdatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                txTmClass.setTransactionStatus(listChildStatus[id]);
                txTmClass.setCorrectionFlag(listChildCorretion[id].equals("TRUE"));
                txTmClass.setEdition(mClass.getEdition());
                txTmClass.setNotes(note);
                txTmClass.setVersion(mClass.getVersion());
                //insert kedalam txTmClass
                listTxClass[i] = txTmClass;
                i++;
            }
            classService.saveTxTMClass(listTxClass);
        }

    }
    
    @RequestMapping(value = REQUEST_MAPPING_AJAX_CARI_LIST_DATA_DOKUMEN, method = {RequestMethod.GET})
    public void doDataTablesCariListDocument(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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
            List<TxTmDoc> txtmDocList = doclampiranService.getAllDocByApplicationId("txTmGeneral.id", txTmGeneral == null ? "" : txTmGeneral.getId());
            List<String[]> data = new ArrayList<>();

            if (txtmDocList.size() > 0) {
                dataTablesSearchResult.setRecordsFiltered(txtmDocList.size());
                dataTablesSearchResult.setRecordsTotal(txtmDocList.size());

                int no = 0;
                for (TxTmDoc result : txtmDocList) {
                    no++;

                    data.add(new String[]{
                            result.getId(),
                            result.getmDocType().getId(),
                            " " + no,
                            DateUtil.formatDate(result.getCreatedDate(), "dd-MM-yyyy"),
                            result.getmDocType().getName(),
                            result.getFileName(),
                            result.getDescription(),
                            result.getFileSize(),
                            result.getCreatedBy().getUsername(),
                            ""
                    });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(value = REQUEST_MAPPING_AJAX_INSERT_DATA_DOKUMEN, method = {RequestMethod.POST})
    @ResponseBody
    public void doSaveFormDokumen(@RequestParam("appNo") String appNo, @RequestParam("docList") String docList,
                                  final Model model, final HttpServletRequest request, final HttpServletResponse response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(docList);

            TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(appNo);

            doclampiranService.deleteDokumenLampiranByApplicationId(txTmGeneral.getId());

            for (JsonNode node : rootNode) {
                String docId = node.get("docId").toString().replaceAll("\"", "");
                String docDate = node.get("docDate").toString().replaceAll("\"", "");
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
//                txTmDoc.setUploadDate(DateUtil.toDate("dd/MM/yyyy", docDate));
                txTmDoc.setFileName(docFileName);
                txTmDoc.setFileSize(docFileSize);
                txTmDoc.setStatus(true);

                doclampiranService.saveDokumenLampiran(txTmDoc, docFile[1]);
            }

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

    @RequestMapping(value = REQUEST_MAPPING_AJAX_UPDATE_DATA_DOWNLOAD_DOKUMEN, method = {RequestMethod.POST})
    public void doUpdateDownloadDokumen(final HttpServletRequest request, final HttpServletResponse response) {
        try {
            String txDocId = request.getParameter("txTmDocId");

            TxTmDoc txTmDocs = dokumenLampiranService.selectOneDocAppId("id", txDocId);
            txTmDocs.setStatus(true);
            doclampiranService.saveDocUpdatePermohonan(txTmDocs);

        } catch (NullPointerException e) {
        }
    }

//    @PostMapping(REQUEST_MAPPING_AJAX_PRATINJAU_INSERT_DATA_DOKUMEN)
//    public void doAjaxAdd(@RequestParam("appNo") String appNo, @RequestParam("docList") String docList,
//            final Model model, final HttpServletRequest request, final HttpServletResponse response) {
//    	if (isAjaxRequest(request)) {
//            setResponseAsJson(response);
//    	try {
//            ObjectMapper mapper = new ObjectMapper();
//            JsonNode rootNode = mapper.readTree(docList);
//
//            TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(appNo);
//
//            for (JsonNode node : rootNode) {
//                String docId = node.get("docId").toString().replaceAll("\"", "");
//                String docDate = node.get("docDate").toString().replaceAll("\"", "");
//                String docFileName = node.get("docFileName").toString().replaceAll("\"", "");
//                String docDesc = node.get("docDesc").toString().replaceAll("\"", "");
//                String docFileSize = node.get("docFileSize").toString().replaceAll("\"", "");
//                String[] docFile = request.getParameter("file-" + docId).split(",");
//                ;
//
//                TxTmDoc txTmDoc = new TxTmDoc();
//                txTmDoc.setCreatedDate(new Timestamp(System.currentTimeMillis()));
//                txTmDoc.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
//                txTmDoc.setTxTmGeneral(txTmGeneral);
//                txTmDoc.setmDocType(doclampiranService.getDocTypeById(docId));
//                txTmDoc.setDescription(docDesc);
////                txTmDoc.setUploadDate(DateUtil.toDate("dd/MM/yyyy", docDate));
//                txTmDoc.setFileName(docFileName);
//                txTmDoc.setFileSize(docFileSize);
//                txTmDoc.setStatus(false);
//
//                doclampiranService.saveDokumenLampiran(txTmDoc, docFile[1]);
//            }
//
//            //return PATH_POST_DOKUMEN;
//            writeJsonResponse(response, 200);
//        } catch (DataIntegrityViolationException e) {
//            logger.error(e.getMessage(), e);
//            writeJsonResponse(response, 500);
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//            writeJsonResponse(response, 500);
//        }
//      } else {
//            response.setStatus(HttpServletResponse.SC_FOUND);
//      }
//    }
    /* --------------------------------------- END DOKUMEN ------------------------------------------*/

//    /* --------------------------------------- PRIORITAS --------------------------------------- */
//    @RequestMapping(value = REQUEST_MAPPING_AJAX_LIST_PRIORITAS, method = {RequestMethod.GET})
//    public void doSearchDataTablesListPrioritas(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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
//            int limit = 20;
//            try {
//                offset = Math.abs(Integer.parseInt(request.getParameter("start")));
//            } catch (NumberFormatException e) {
//            }
//            try {
//                limit = Math.abs(Integer.parseInt(request.getParameter("length")));
//            } catch (NumberFormatException e) {
//            }
//
//            String appNo = request.getParameter("appNo");
//
//
//            String orderBy = "no";
//            if (orderBy != null) {
//                orderBy = orderBy.trim();
//                if (orderBy.equalsIgnoreCase("")) {
//                    orderBy = "orderBy";
//                }
//            }
//
//            String orderType = request.getParameter("orderType");
//            if (orderType == null) {
//                orderType = "ASC";
//            } else {
//                orderType = orderType.trim();
//                if (!orderType.equalsIgnoreCase("DESC")) {
//                    orderType = "ASC";
//                }
//            }
//
//
//            List<KeyValue> searchCriteria = new ArrayList<>();
//            MUser user = new MUser();
//            user = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//
//            searchCriteria.add(new KeyValue("txTmGeneral.applicationNo", appNo, true));
//            //searchCriteria.add(new KeyValue("createdBy.id", user.getId(), true));
//
//
//            List<String[]> data = new ArrayList<>();
//
//            GenericSearchWrapper<TxTmPrior> searchResult = null;
//
//            searchResult = priorService.searchGeneralTest(searchCriteria, orderBy, orderType, offset, limit);
//
//            if (searchResult.getCount() > 0) {
//                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
//                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());
//
//                int no = offset;
//                for (TxTmPrior result : searchResult.getList()) {
//                    no++;
//
//                    String status = PriorStatusEnum.PENDING.getLabel();
//
//                    try {
//                        status = PriorStatusEnum.valueOf(result.getStatus()).getLabel();
//                    } catch (NullPointerException e) {
//                    }
//
//                    data.add(new String[]{
//                            result.getNo(),
//                            DateUtil.formatDate(result.getPriorDate(), "dd/MM/yyyy"),
//                            result.getmCountry().getName(),
//                            result.getNote(),
//                            status,
//                            result.getId()
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

    @RequestMapping(value = REQUEST_MAPPING_AJAX_INSERT_DATA_TX_TM_PRIOR, method = {RequestMethod.GET})
    public void doInsertPrioritas(Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ParseException {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);

            String negaraId = request.getParameter("negaraId");
            String no = request.getParameter("no");
            String appNo = request.getParameter("appNo");
            Timestamp tmstm = DateUtil.toDate("dd/MM/yyyy", request.getParameter("tgl"));

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
            }
            writeJsonResponse(response, result);
        }
    }

    @RequestMapping(value = REQUEST_MAPPING_AJAX_DELETE_PRIORITAS, method = RequestMethod.POST)
    public void doDeletePrioritas(Model model, @RequestParam("idPrior") String id, final HttpServletRequest request, final HttpServletResponse response) {
        priorService.deletePrioritas(id);
    }
    /* --------------------------------------- END PRIORITAS --------------------------------------- */

    /* --------------------------------------- BRAND --------------------------------------- */
    @RequestMapping(value = REQUEST_MAPPING_AJAX_INSERT_DATA_TX_TM_BRAND, method = {RequestMethod.GET})
    public void doInsertTxTmBrandOnly(Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        //get parameter value ajax txtmBrand
        String tes = "tes";
        //MBrandVar
        String jenisId = request.getParameter("jenisId");
        String jenis = request.getParameter("jenis");
        //MBrandType
        String tipeId = request.getParameter("tipeId");
        String tipe = request.getParameter("tipe");
        //get MBrand
        String nama = request.getParameter("nama");
        String deskripsi = request.getParameter("deskripsi");
        String terjemahan = request.getParameter("terjemahan");
        String pengucapan = request.getParameter("pengucapan");
        String warna = request.getParameter("warna");
        String disclaimer = request.getParameter("disclaimer");
        String appNum = request.getParameter("appNum");
        //get MBrandChild
        String deskripsiChild = request.getParameter("deskripsiChild");

        //insert new
        //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        //MBrandVar mBrandVar = new MBrandVar();
        MBrandType mBrandType = new MBrandType();
        TxTmBrand txTmBrand = new TxTmBrand();
        TxTmBrandDetail txTmBrandDetail = new TxTmBrandDetail();
        TxTmGeneral txTmGeneral = new TxTmGeneral();

        //set value txtmbrand
        //mBrandVar.setId(jenisId);
        mBrandType.setId(tipeId);

        txTmBrand.setmBrandType(mBrandType);
        txTmBrand.setId(txTmBrand.getId());
        txTmBrand.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        txTmBrand.setCreatedDate(timestamp);
        txTmBrand.setmBrandType(mBrandType);

        txTmBrand.setName(nama);
        txTmBrand.setDescription(deskripsi);
        txTmBrand.setTranslation(terjemahan);
        txTmBrand.setDescChar(pengucapan);
        txTmBrand.setColor(warna);
        txTmBrand.setDisclaimer(disclaimer);
        //set value txtmbrandchild
        txTmBrandDetail.setId(txTmBrandDetail.getId());
        txTmBrandDetail.setUploadDate(timestamp);
        txTmBrandDetail.setFileDescription(deskripsiChild);

        txTmBrand.setTxTmGeneral(trademarkService.selectOne("applicationNo", appNum));
        txTmBrandDetail.setTxTmGeneral(trademarkService.selectOne("applicationNo", appNum));

//        brandService.insertHanyaTxTmBrand(txTmBrand);
        brandService.insertBrand(txTmBrand, txTmBrandDetail);
    }

    @RequestMapping(value = REQUEST_MAPPING_AJAX_INSERT_DATA_TX_TM_BRAND_CHILD, method = {RequestMethod.GET})
    public void doInsertTxTmBrandChildOnly(Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException {

        //get parameter value ajax txtmBrandchild
        String deskripsiChild = request.getParameter("deskripsiChild");

        //insert new
        //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd");
        //Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        //MBrandVar mBrandVar = new MBrandVar();
        MBrandType mBrandType = new MBrandType();
        TxTmBrand txTmBrand = new TxTmBrand();
        TxTmBrandDetail txTmBrandDetail = new TxTmBrandDetail();
        TxTmGeneral txTmGeneral = new TxTmGeneral();

        //brandService.insertHanyaTxTmBrand(txTmBrand);
        brandService.insertBrand(txTmBrand, txTmBrandDetail);
    }
    /* --------------------------------------- END BRAND --------------------------------------- */

    /* --------------------------------------- MEREK --------------------------------------- */
    @RequestMapping(value = REQUEST_MAPPING_AJAX_INSERT_MEREK, method = {RequestMethod.POST})
    public String doInsertMerekTable(@ModelAttribute("txTmBrand") final TxTmBrand txTmBrand, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        String appNo = request.getParameter("no");
        //SimpleDateFormat df = new SimpleDateFormat("yyyy-mm-dd");
        Timestamp tmstm = new Timestamp(System.currentTimeMillis());
        //TxTmGeneral txTmGeneral = new TxTmGeneral();
        TxTmGeneral txTmGeneral = trademarkService.selectOne("applicationNo", appNo);
        txTmBrand.setFileName(txTmBrand.getFileMerek().getOriginalFilename());
        txTmBrand.setId(txTmBrand.getId());
        txTmBrand.setTxTmGeneral(txTmGeneral);
        txTmBrand.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        txTmBrand.setCreatedDate(tmstm);

        brandService.insertHanyaTxTmBrand(txTmBrand, null);

        return "redirect:" + PATH_REDIRECT + "?no=" + txTmGeneral.getApplicationNo();
    }
    /* --------------------------------------- END MEREK --------------------------------------- */

    /* ---------------------------------------CETAK MEREK --------------------------------------- */
    @RequestMapping(value = REQUEST_MAPPING_CETAK_MEREK, method = {RequestMethod.GET})
    @ResponseBody
    public String doCetakMerek(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        String applicationNo = request.getParameter("no");
        String dwnonly = request.getParameter("dwnonly");
        TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(applicationNo);

        response.setContentType("application/pdf");
        response.setHeader("Content-disposition", "inline; filename=CetakMerek-" + applicationNo + ".pdf");

        String folder =  downloadFileDocPermohonanCetakMerekPath + DateUtil.formatDate(txTmGeneral.getCreatedDate(), "yyyy/MM/dd/");
        String fileName = "CetakMerek-" + txTmGeneral.getApplicationNo() + ".pdf";

        try {
            Path path = Paths.get(folder + fileName);
            if((Files.exists(path) && Files.size(path) > 0) || dwnonly != null) {
                response.setContentType("application/pdf");
                response.setHeader("Content-disposition", "inline; filename=CetakMerek-" + applicationNo + ".pdf");
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
                    String pathFolder = DateUtil.formatDate(txTmGeneral.getTxTmBrand().getCreatedDate(), "yyyy/MM/dd/");
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

            response.sendRedirect(request.getRequestURL().toString() + "?" + request.getQueryString() + "&dwnonly=1");
        } catch (Exception ex) {
            logger.error(ex);
        }

        return "";
    }

    @RequestMapping(value = REQUEST_MAPPING_CETAK_SURAT_PERNYATAAN, method = {RequestMethod.GET})
    @ResponseBody
    public String doCetakSuratPernyataan(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        String applicationNo = request.getParameter("no");
        TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(applicationNo);

        response.setContentType("application/pdf");
        response.setHeader("Content-disposition", "inline; filename=CetakSuratPernyataan-" + applicationNo + ".pdf");

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

    @RequestMapping(value = REQUEST_MAPPING_CETAK_MADRID, method = {RequestMethod.GET})
    @ResponseBody
    public String doCetakMadrid(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        String applicationNo = request.getParameter("no");
        TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(applicationNo);

        response.setContentType("application/pdf");
        response.setHeader("Content-disposition", "inline; filename=CetakMadrid.pdf");

        String folder = downloadFileDocMadridOOCetakPath + DateUtil.formatDate(txTmGeneral.getCreatedDate(), "yyyy/MM/dd/");
        String fileName = "CetakMadrid-" + txTmGeneral.getId() + ".pdf";

        try (OutputStream output = response.getOutputStream()) {
            Path path = Paths.get(folder + fileName);
            Files.copy(path, output);
            output.flush();
        } catch(IOException e) {
        }

        return "";

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
    /* --------------------------------------- END CETAK MEREK --------------------------------------- */

    @GetMapping(REQUEST_MAPPING_DETAIL_UBAH_TIPE)
    public String doShowPageUbahTipePermohonan(final Model model, final HttpServletRequest request) {
        List<KeyValue> searchCriteria = new ArrayList<>();

        List<MFileType> fileTypeList = fileService.selectAllByFileTypeCode();
        model.addAttribute("fileTypeList", fileTypeList);

        if (doInitiateEditPenerimaan(model, request)) {
            return PAGE_DETAIL_UBAH_TIPE;

        }

        return REDIRECT_PATH_PERMOHONAN + "?error=Nomor permohonan " + request.getParameter("no") + " tidak ditemukan";
    }

    @PostMapping(REQUEST_MAPPING_DETAIL_UBAH_TIPE)
    public String doEditPageUbahTipePermohonan(
            @ModelAttribute("form") final TxReception form,
            final Model model, final BindingResult errors,
            final HttpServletRequest request, final HttpServletResponse response) {

        if (!doInitiateEditPenerimaan(model, request)) {
            return REDIRECT_PATH_PERMOHONAN + "?error=Nomor permohonan " + request.getParameter("no") + " tidak ditemukan";
        }

        FieldValidationUtil.required(errors, "idNomorPermohonanNew", form.getApplicationNoNew(), "Nomor Permohonan Baru");

        DataForm1 frmGeneral = new DataForm1();
        frmGeneral.setAppid(form.getApplicationNo());
        frmGeneral.setmFileType(form.getmFileType());

        TxReception existing = trademarkService.selectOneReceptionByApplicationNo(form.getApplicationNo());
        existing.setmFileType(form.getmFileType());
        existing.setApplicationNo(form.getApplicationNoNew());

        if (!errors.hasErrors()) {
            form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

            try {
                permohonanService.updateForm1(frmGeneral, form.getApplicationNoNew());
                trademarkService.updateReceptionNewApplicationNo(existing);

            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                model.addAttribute("errorMessage", "Gagal merubah nomor permohonan");
            }
        }

        return REDIRECT_PATH_PERMOHONAN;
    }

    private boolean doInitiateEditPenerimaan(final Model model, final HttpServletRequest request) {
        model.addAttribute("subMenu", "permohonanBaru");

        TxReception existing = trademarkService.selectOneReceptionByApplicationNo(request.getParameter("no"));
        //TxTmGeneral existing = trademarkService.selectOneGeneralByApplicationNo(request.getParameter("no"))
        if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
            model.addAttribute("form", existing);
        } else {
            model.addAttribute("existing", existing);
        }

        return existing != null;
    }

    /* ---------------------------------------- END PERMOHONAN SECTION ---------------------------------------- */


    @GetMapping(REQUEST_EXPORT_PERMOHONAN)
    public void doExportPermohonan(HttpServletRequest request, HttpServletResponse response) {
        InputStream reportInputStream = null;

        String[] searchByArr = null;
        try {
            searchByArr = request.getParameter("searchByArr").split(",");
        } catch (Exception e) {
        }
        String[] keywordArr = null;
        try {
            keywordArr = request.getParameter("keywordArr").split(",");
        } catch (Exception e) {
        }
        String orderBy = request.getParameter("orderBy");
        String orderType = request.getParameter("orderType");

        List<KeyValue> searchCriteria = new ArrayList<>();

        if (searchByArr != null) {
            for (int i = 0; i < searchByArr.length; i++) {
                String searchBy = searchByArr[i];
                String value = null;
                try {
                    value = keywordArr[i];
                } catch (ArrayIndexOutOfBoundsException e) {
                }
                if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                    if ((searchBy.equalsIgnoreCase("mFileSequence.id") || searchBy.equalsIgnoreCase("mFileType.id") || searchBy.equalsIgnoreCase("mFileTypeDetail.id")) && value != null) {
                        if (StringUtils.isNotBlank(value)) {
                            searchCriteria.add(new KeyValue(searchBy, value, true));
                        }
                    } else if (searchBy.equalsIgnoreCase("startDate") || searchBy.equalsIgnoreCase("endDate") || searchBy.equalsIgnoreCase("txTmPriorList.priorDate")) {
                        if (StringUtils.isNotBlank(value)) {
                            try {
                                searchCriteria.add(new KeyValue(searchBy, DateUtil.toDate("dd/MM/yyyy", value), true));
                            } catch (ParseException e) {
                            }
                        }
                    }
                    else {
                        if (StringUtils.isNotBlank(value)) {
                            if (searchBy.equals("txTmBrand.name")) {
                                value = value.replaceAll("\\*", "_");
                                orderBy = "4";
                                orderType = "ASC";
                            }

                            if (searchBy.equalsIgnoreCase("txTmClassList")) {
                                String[] classList = value.split(",");
                                for ( int ii = 0; ii < classList.length; ii++ ) {
                                    searchCriteria.add(new KeyValue(searchBy, classList[ii], true));
                                }
                            } else {
                                searchCriteria.add(new KeyValue(searchBy, value, false));
                            }
                        }
                    }
                }
            }
        }


        /*if (searchByArr != null) {
            for (int i = 0; i < searchByArr.length; i++) {
                String searchBy = searchByArr[i];
                String value = null;
                try {
                    value = keywordArr[i];
                } catch (ArrayIndexOutOfBoundsException e) {
                }
                if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                    if ((searchBy.equalsIgnoreCase("mFileSequence.id") || searchBy.equalsIgnoreCase("mFileType.id") || searchBy.equalsIgnoreCase("mFileTypeDetail.id")) && value != null) {
                        if (StringUtils.isNotBlank(value)) {
                            searchCriteria.add(new KeyValue(searchBy, value, true));
                        }
                    } else if (searchBy.equalsIgnoreCase("startDate") || searchBy.equalsIgnoreCase("endDate") || searchBy.equalsIgnoreCase("txTmPriorList.priorDate")) {
                        if (StringUtils.isNotBlank(value)) {
                            try {
                                searchCriteria.add(new KeyValue(searchBy, DateUtil.toDate("dd/MM/yyyy", value), true));
                            } catch (ParseException e) {
                            }
                        }
                    } else {
                        if (StringUtils.isNotBlank(value)) {
                            if (searchBy.equalsIgnoreCase("txTmClassList")) {
                                searchCriteria.add(new KeyValue(searchBy, value, true));
                            } else {
                                searchCriteria.add(new KeyValue(searchBy, value, false));
                            }
                        }
                    }
                }
            }
        }*/

        searchCriteria.add(new KeyValue("statusOnOff", StatusEnum.IPT_DRAFT.name(), true));

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
                        orderBy = "txTmBrand.mBrandType.name";
                        break;
                    case "4":
                        orderBy = "txTmBrand.name";
                        break;
                    case "5":
                        orderBy = "mClass.no";
                        break;
                    case "6":
                        orderBy = "txTmOwner.name";
                        break;
                    case "7":
                        orderBy = "txTmReprs.name";
                        break;
                    case "8":
                        orderBy = "mStatus.name";
                        break;
                    case "9":
                        orderBy = "txTmPriorList.priorDate";
                        break;
                    case "10":
                        orderBy = "txRegistration.no";
                        break;
                    default:
                        orderBy = "txReception.mFileTypeDetail.id";
                        break;
                }
            }
        }

        if (orderType == null) {
            orderType = "ASC";
        } else {
            orderType = orderType.trim();
            if (!orderType.equalsIgnoreCase("DESC")) {
                orderType = "ASC";
            }
        }

        try {
            reportInputStream = getClass().getClassLoader().getResourceAsStream("report/list-permohonan.xls");
            //List<TxTmGeneral> dataList = new ArrayList<>();
            List<DataGeneral> dataList = new ArrayList<>();
            int totalRecord = (int) trademarkService.countAll(searchCriteria);
            int retrieved = 0;
            DataGeneral datagen = null;
            //No.	Nomor_Permohonan	Tanggal_Penerimaan	Tipe_Merek	Nama_Merek	Kelas	Nama_Pemohon	Status
            int no = 0;
            //while (retrieved < totalRecord) {
            //System.out.println("dump total " + totalRecord);
            if (totalRecord > 0) {
                TxTmGeneralCustomRepository.withJoin = false;
                List<TxTmGeneral> retrievedDataList = trademarkService.selectAll(searchCriteria, orderBy, orderType, retrieved, 100000);

                //dataList.addAll(retrievedDataList);

                for (TxTmGeneral result : retrievedDataList) {
                    no++;
                    String brandName = "-";
                    String brandType = "-";
                    String status = "-";
                    String aksi = "-";
                    String sbOwnerNameList = "-";
                    String sbClassList = "-";
                    String reprsList = "-";
                    ArrayList<String> kelas = new ArrayList<String>();
                    ArrayList<String> owners = new ArrayList<String>();
                    ArrayList<String> reprs = new ArrayList<String>();
                    //String sFillingDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(result.getFilingDate().getTime()));

                    String mFileTypeDesc = "-";
                    String mFileTypeDetailDesc = "-";
                    try {
                        mFileTypeDesc = result.getTxReception().getmFileType().getDesc();
                    } catch (Exception e) {
                    }

                    try {
                        mFileTypeDetailDesc = result.getTxReception().getmFileTypeDetail().getDesc();
                    } catch (Exception e) {
                    }

                    String username = "-";
                    try {
                        username = result.getCreatedBy().getUsername();
                    } catch (NullPointerException e) {
                    }

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
                        aksi = result.getmAction().getName();
                    } catch (NullPointerException e) {
                    }

                    String regNo = "-";
                    try {
                        regNo = result.getTxRegistration().getNo();
                    } catch (NullPointerException e) {
                    }

                    try {
                        for (TxTmOwner txTmOwner : result.getTxTmOwner()) {
                            owners.add("" + txTmOwner.getName());
                        }
                        Set<String> temp = new LinkedHashSet<String>(owners);
                        String[] unique = temp.toArray(new String[temp.size()]);
                        if (unique.length > 0) {
                            sbOwnerNameList = String.join(",", unique);
                        }
                        /*ownerName = result.getTxTmOwner().getName();*/
                    } catch (NullPointerException e) {
                    }

                    try {
                        for (TxTmRepresentative txTmRepresentative : result.getTxTmRepresentative()) {
                            reprs.add("" + txTmRepresentative.getName());
                        }
                        Set<String> tempr = new LinkedHashSet<String>(reprs);
                        String[] uniquer = tempr.toArray(new String[tempr.size()]);
                        if (uniquer.length > 0) {
                            reprsList = String.join(",", uniquer);
                        }
                        /*ownerName = result.getTxTmOwner().getName();*/
                    } catch (NullPointerException e) {
                    }

                    for (TxTmClass txTmClass : result.getTxTmClassList()) {
                        kelas.add("" + txTmClass.getmClass().getNo());
                    }
                    Set<String> temp = new LinkedHashSet<String>(kelas);
                    String[] unique = temp.toArray(new String[temp.size()]);
                    if (unique.length > 0) {
                        sbClassList = String.join(",", unique);
                    }

                    String bankCode = "-";
                    try {
                        bankCode = result.getTxReception().getBankCode();
                    } catch (NullPointerException e) {
                    }


                    datagen = new DataGeneral();
                    datagen.setNo(no);
                    datagen.setApplicationNo(result.getApplicationNo());
                    datagen.setBankCode(bankCode);
                    datagen.setApplicationDate(result.getTxReception().getApplicationDateTemp());
                    datagen.setFillingDate(result.getFilingDateTemp());
                    datagen.setmFileType(mFileTypeDesc);
                    datagen.setBrandType(brandType);
                    datagen.setBrandName(brandName);
                    datagen.setKelas(sbClassList);
                    datagen.setPriorName(sbOwnerNameList);
                    datagen.setStatus(status);
                    datagen.setUsername(username);
                    datagen.setmFileTypeDetail(mFileTypeDetailDesc);
                    datagen.setRegNo(regNo);
                    datagen.setReprsName(reprsList);
                    datagen.setAksi(aksi);
                    dataList.add(datagen);
                }
                retrieved += retrievedDataList.size();
            }

            Context context = new Context();
            context.putVar("dataList", dataList);

            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=list-permohonan.xls");

            JxlsHelper.getInstance().processTemplate(reportInputStream, response.getOutputStream(), context);
            response.getOutputStream().close();
            response.flushBuffer();
        } catch (Exception ex) {
            logger.error(ex);
        } finally {
            if (reportInputStream != null) {
                try {
                    reportInputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }
    
    @GetMapping(REQUEST_EXPORT_XML_PERMOHONAN)
    public void doExportXMLPermohonan(HttpServletRequest request, HttpServletResponse response) {
        InputStream reportInputStream = null;

        String[] searchByArr = null;
        try {
            searchByArr = request.getParameter("searchByArr").split(",");
        } catch (Exception e) {
        }
        String[] keywordArr = null;
        try {
            keywordArr = request.getParameter("keywordArr").split(",");
        } catch (Exception e) {
        }
        String orderBy = request.getParameter("orderBy");
        String orderType = request.getParameter("orderType");

        List<KeyValue> searchCriteria = new ArrayList<>();

        if (searchByArr != null) {
            for (int i = 0; i < searchByArr.length; i++) {
                String searchBy = searchByArr[i];
                String value = null;
                try {
                    value = keywordArr[i];
                } catch (ArrayIndexOutOfBoundsException e) {
                }
                if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                    if ((searchBy.equalsIgnoreCase("mFileSequence.id") || searchBy.equalsIgnoreCase("mFileType.id") || searchBy.equalsIgnoreCase("mFileTypeDetail.id")) && value != null) {
                        if (StringUtils.isNotBlank(value)) {
                            searchCriteria.add(new KeyValue(searchBy, value, true));
                        }
                    } else if (searchBy.equalsIgnoreCase("startDate") || searchBy.equalsIgnoreCase("endDate") || searchBy.equalsIgnoreCase("txTmPriorList.priorDate")) {
                        if (StringUtils.isNotBlank(value)) {
                            try {
                                searchCriteria.add(new KeyValue(searchBy, DateUtil.toDate("dd/MM/yyyy", value), true));
                            } catch (ParseException e) {
                            }
                        }
                    }
                    else {
                        if (StringUtils.isNotBlank(value)) {
                            if (searchBy.equals("txTmBrand.name")) {
                                value = value.replaceAll("\\*", "_");
                                orderBy = "4";
                                orderType = "ASC";
                            }

                            if (searchBy.equalsIgnoreCase("txTmClassList")) {
                                String[] classList = value.split(",");
                                for ( int ii = 0; ii < classList.length; ii++ ) {
                                    searchCriteria.add(new KeyValue(searchBy, classList[ii], true));
                                }
                            } else {
                                searchCriteria.add(new KeyValue(searchBy, value, false));
                            }
                        }
                    }
                }
            }
        }


        /*if (searchByArr != null) {
            for (int i = 0; i < searchByArr.length; i++) {
                String searchBy = searchByArr[i];
                String value = null;
                try {
                    value = keywordArr[i];
                } catch (ArrayIndexOutOfBoundsException e) {
                }
                if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                    if ((searchBy.equalsIgnoreCase("mFileSequence.id") || searchBy.equalsIgnoreCase("mFileType.id") || searchBy.equalsIgnoreCase("mFileTypeDetail.id")) && value != null) {
                        if (StringUtils.isNotBlank(value)) {
                            searchCriteria.add(new KeyValue(searchBy, value, true));
                        }
                    } else if (searchBy.equalsIgnoreCase("startDate") || searchBy.equalsIgnoreCase("endDate") || searchBy.equalsIgnoreCase("txTmPriorList.priorDate")) {
                        if (StringUtils.isNotBlank(value)) {
                            try {
                                searchCriteria.add(new KeyValue(searchBy, DateUtil.toDate("dd/MM/yyyy", value), true));
                            } catch (ParseException e) {
                            }
                        }
                    } else {
                        if (StringUtils.isNotBlank(value)) {
                            if (searchBy.equalsIgnoreCase("txTmClassList")) {
                                searchCriteria.add(new KeyValue(searchBy, value, true));
                            } else {
                                searchCriteria.add(new KeyValue(searchBy, value, false));
                            }
                        }
                    }
                }
            }
        }*/

        searchCriteria.add(new KeyValue("statusOnOff", StatusEnum.IPT_DRAFT.name(), true));

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
                        orderBy = "txTmBrand.mBrandType.name";
                        break;
                    case "4":
                        orderBy = "txTmBrand.name";
                        break;
                    case "5":
                        orderBy = "mClass.no";
                        break;
                    case "6":
                        orderBy = "txTmOwner.name";
                        break;
                    case "7":
                        orderBy = "txTmReprs.name";
                        break;
                    case "8":
                        orderBy = "mStatus.name";
                        break;
                    case "9":
                        orderBy = "txTmPriorList.priorDate";
                        break;
                    case "10":
                        orderBy = "txRegistration.no";
                        break;
                    default:
                        orderBy = "txReception.mFileTypeDetail.id";
                        break;
                }
            }
        }

        if (orderType == null) {
            orderType = "ASC";
        } else {
            orderType = orderType.trim();
            if (!orderType.equalsIgnoreCase("DESC")) {
                orderType = "ASC";
            }
        }

        // keep xml temp file and delete at last
        List<File> needToDelete = new ArrayList<>();
        try {
            reportInputStream = getClass().getClassLoader().getResourceAsStream("report/list-permohonan.xls");
            //List<TxTmGeneral> dataList = new ArrayList<>();
            List<TradeMark> dataList = new ArrayList<>();
            Map<String,File> zipFiles = new HashMap<>();
            int totalRecord = (int) trademarkService.countAll(searchCriteria);
            int retrieved = 0;
            Transaction transaction = new Transaction();
            //No.	Nomor_Permohonan	Tanggal_Penerimaan	Tipe_Merek	Nama_Merek	Kelas	Nama_Pemohon	Status
            int no = 0;
            //while (retrieved < totalRecord) {
            //System.out.println("dump total " + totalRecord);
            if (totalRecord > 0) {
                TxTmGeneralCustomRepository.withJoin = false;
                List<TxTmGeneral> retrievedDataList = trademarkService.selectAll(searchCriteria, orderBy, orderType, retrieved, 100000);

                //dataList.addAll(retrievedDataList);

                for (TxTmGeneral result : retrievedDataList) {
                    no++;
                    String brandName = "-";
                    String brandType = "-";
                    String status = "-";
                    String sbOwnerNameList = "-";
                    String sbClassList = "-";
                    String reprsList = "-";
                    ArrayList<String> kelas = new ArrayList<String>();
                    ArrayList<String> owners = new ArrayList<String>();
                    ArrayList<String> reprs = new ArrayList<String>();
                    //String sFillingDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(result.getFilingDate().getTime()));

                    String mFileTypeDesc = "-";
                    String mFileTypeDetailDesc = "-";
                    try {
                        mFileTypeDesc = result.getTxReception().getmFileType().getDesc();
                    } catch (Exception e) {
                       // e.printStackTrace();
                    }

                    try {
                        mFileTypeDetailDesc = result.getTxReception().getmFileTypeDetail().getDesc();
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }

                    String username = "-";
                    try {
                        username = result.getCreatedBy().getUsername();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    try {
                        brandName = result.getTxTmBrand().getName();
                    } catch (NullPointerException e) {
                        //e.printStackTrace();
                    }

                    try {
                        brandType = result.getTxTmBrand().getmBrandType().getName();
                    } catch (NullPointerException e) {
                        //e.printStackTrace();
                    }

                    try {
                        status = result.getmStatus().getName();
                    } catch (NullPointerException e) {
                        //e.printStackTrace();
                    }

                    String regNo = "-";
                    try {
                        regNo = result.getTxRegistration().getNo();
                    } catch (NullPointerException e) {
                        //e.printStackTrace();
                    }

                    try {
                        for (TxTmOwner txTmOwner : result.getTxTmOwner()) {
                            owners.add("" + txTmOwner.getName());
                        }
                        Set<String> temp = new LinkedHashSet<String>(owners);
                        String[] unique = temp.toArray(new String[temp.size()]);
                        if (unique.length > 0) {
                            sbOwnerNameList = String.join(",", unique);
                        }
                        /*ownerName = result.getTxTmOwner().getName();*/
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    try {
                        for (TxTmRepresentative txTmRepresentative : result.getTxTmRepresentative()) {
                            reprs.add("" + txTmRepresentative.getName());
                        }
                        Set<String> tempr = new LinkedHashSet<String>(reprs);
                        String[] uniquer = tempr.toArray(new String[tempr.size()]);
                        if (uniquer.length > 0) {
                            reprsList = String.join(",", uniquer);
                        }
                        /*ownerName = result.getTxTmOwner().getName();*/
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    for (TxTmClass txTmClass : result.getTxTmClassList()) {
                        kelas.add("" + txTmClass.getmClass().getNo());
                    }
                    Set<String> temp = new LinkedHashSet<String>(kelas);
                    String[] unique = temp.toArray(new String[temp.size()]);
                    if (unique.length > 0) {
                        sbClassList = String.join(",", unique);
                    }

                    String bankCode = "-";
                    try {
                        bankCode = result.getTxReception().getBankCode();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    List<TxMonitor> monitors = txMonitorCustomRepository.findByTxTmGeneralId(result.getId());
                    String currentStatusDate = "";
                    if (monitors.size() > 0) {
                        currentStatusDate = monitors.get(monitors.size() - 1).getCreatedDate().toString();
                    }

                    String ownerType = "";
                    List<TxTmOwner> txTmOwners = Lists.newArrayList(result.getTxTmOwner());
                    for (TxTmOwner owner : txTmOwners) {
                        ownerType = owner.getOwnerType();
                    }

                    Other other = new Other();

                    TradeMark tradeMark = new TradeMark();
                    tradeMark.setApplicationNumber(result.getApplicationNo());
                    if (result.getTxReception() != null) {
                        tradeMark.setApplicationDate(result.getTxReception().getApplicationDateTemp()); // TX_RECEPTION.APPLICATION_DATE
                    }
                    tradeMark.setApplicationLanguageCode("ID"); // ID (HARDCODE)]
                    if (result.getTxRegistration() != null) {
                        tradeMark.setRegistrationDate(result.getTxRegistration().getStartDate().toString()); // TX_REGISTRATION.REG_START_DATE
                        tradeMark.setExpiryDate(result.getTxRegistration().getEndDate().toString());// TX_REGISTRATION.EXPIRATION_DATE]
                    }
                    tradeMark.setMarkCurrentStatusCode("Registered");
                    tradeMark.setRegistrationOfficeCode("ID");
                    tradeMark.setApplicationNumber(result.getApplicationNo().substring(result.getApplicationNo().length()-7)); // TX_TM_GENERAL.APPLICATION_NO (TANPA D/J 00 YYYY, HANYA NOMOR TERAKHIR)
                    tradeMark.setMarkCurrentStatusDate(currentStatusDate);   // TX_MONITOR.CREATED_DATE PALING TERAKHIR]
                    tradeMark.setApplicantDetails(other.getApplicantDetails(result));
                    tradeMark.setGoodsServicesDetails(other.getGoodsServicesDetails(result));
                    tradeMark.setKindMark(ownerType); // TX_TM_OWNER.OWNER_TYPE, JIKA Perorangan=Individual, Badan Hukum=Corporation]
                    tradeMark.setMarkImageDetails(other.getMarkImageDetails(result));
                    tradeMark.setPublicationDetails(other.getPublicationDetails(result));
                    tradeMark.setRepresentativeDetails(other.getRepresentativeDetails(result));
                    tradeMark.setWordMarkSpecification(other.getWordMarkSpecification(result));
                    tradeMark.setMarkEventDetails(other.getMarkEventDetails(result));
                    dataList.add(tradeMark);

                    // create transaction
                    Transaction.TradeMarkTransactionBody TradeMarkTransactionBody = new Transaction.TradeMarkTransactionBody();
                    Transaction.TransactionContentDetails TransactionContentDetails = new Transaction.TransactionContentDetails();
                    Transaction.TransactionData TransactionData = new Transaction.TransactionData();
                    Transaction.TradeMarkApplication TradeMarkApplication = new Transaction.TradeMarkApplication();
                    Transaction.TradeMarkDetails TradeMarkDetails = new Transaction.TradeMarkDetails();
                    TransactionContentDetails.setTransactionCode("Service mark");
                    TransactionContentDetails.setTransactionIdentifier("");
                    TransactionContentDetails.setTransactionSubCode("Direct Filing");
                    TransactionContentDetails.setTransactionData(TransactionData);
                    TransactionData.setTradeMarkApplication(TradeMarkApplication);
                    TradeMarkApplication.setTradeMarkDetails(TradeMarkDetails);
                    TradeMarkDetails.setTradeMark(dataList);
                    TradeMarkTransactionBody.setTransactionContentDetails(TransactionContentDetails);
                    transaction.setXmlns("");
                    transaction.setXmlnswo("");
                    transaction.setTradeMarkTransactionBody(TradeMarkTransactionBody);

                    String xmlString = "";
                    try {
                        JAXBContext ctx = JAXBContext.newInstance(Transaction.class);
                        Marshaller m = ctx.createMarshaller();
                        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE); // To format XML
                        //StringWriter sw = new StringWriter();
                        //m.marshal(transaction, sw);
                        //xmlString = sw.toString();
                        //m.marshal(transaction, System.out);
                        File xmlFile = new File(Paths.get(uploadFilePathTemp) +"/"+result.getApplicationNo()+"-biblo.xml");
                        m.marshal(transaction, xmlFile);
                        zipFiles.put(result.getApplicationNo()+"-biblo.xml", xmlFile);
                        //xmlFile.delete();
                        needToDelete.add(xmlFile);
                    } catch (JAXBException e) {
                        e.printStackTrace();
                    }

                    //append logo
                    TxTmBrand txTmBrand = result.getTxTmBrand();
                    if(txTmBrand!=null) {
                        String pathFolder = DateUtil.formatDate(txTmBrand.getCreatedDate(), "yyyy/MM/dd/");
                        String imgFile = uploadFileBrandPath + pathFolder + txTmBrand.getId() + ".jpg";
                        System.out.println(imgFile);
                        File logoFile = new File(imgFile);
                        zipFiles.put("ATTACHMENT/" + txTmBrand.getFileName(), logoFile);
                    }
                }
                retrieved += retrievedDataList.size();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);
            //zos.setLevel(ZipOutputStream.DEFLATED);
            try {
                //Converting all xmlString to xml file and pass to zip
                byte[] buffer = new byte[2048];
                for (String fileName : zipFiles.keySet()) {
                    try {
                        // add zip-entry descriptor
                        FileInputStream fis = new FileInputStream(zipFiles.get(fileName));
                        BufferedInputStream bis = new BufferedInputStream(fis);
                        ZipEntry entry = new ZipEntry(fileName);
                        zos.putNextEntry(entry);
                        int len;
                        while ((len = bis.read(buffer)) > 0) {
                            zos.write(buffer, 0, len);
                        }
                        zos.closeEntry();
                        bis.close();
                        fis.close();
                    } catch(Exception e){
                        //skip not found logo
                        e.printStackTrace();
                    }
                }
                zos.flush();
                baos.flush();
                zos.close();
                baos.close();
            } catch (Exception e){
                e.printStackTrace();
            }

            // export as xml file format
            //response.setContentType("application/xml");
            //response.setHeader("Content-Disposition", "attachment; filename=list-permohonan.xml");
            //outStream.println(xmlString);
            response.setContentType("application/zip");
            response.addHeader("Content-Disposition", "attachment; filename=\"zip.zip\"");
            //response.addHeader("Content-Transfer-Encoding", "binary");
            //outStream.write(bos.toByteArray());
            ServletOutputStream outStream = response.getOutputStream();
            outStream.write(baos.toByteArray());
            outStream.flush();
            outStream.close();
            zos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex);
        } finally {
            if (reportInputStream != null) {
                try {
                    reportInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            for( File xmlTempfile : needToDelete) {
                xmlTempfile.delete();
            }
        }
    }

    @GetMapping(REQUEST_EXPORT_JSON_PERMOHONAN)
    public void doExportJSONPermohonan(HttpServletRequest request, HttpServletResponse response) {
        String[] searchByArr = null;
        try {
            searchByArr = request.getParameter("searchByArr").split(",");
        } catch (Exception e) {
        }
        String[] keywordArr = null;
        try {
            keywordArr = request.getParameter("keywordArr").split(",");
        } catch (Exception e) {
        }
        String orderBy = request.getParameter("orderBy");
        String orderType = request.getParameter("orderType");

        List<KeyValue> searchCriteria = new ArrayList<>();

        if (searchByArr != null) {
            for (int i = 0; i < searchByArr.length; i++) {
                String searchBy = searchByArr[i];
                String value = null;
                try {
                    value = keywordArr[i];
                } catch (ArrayIndexOutOfBoundsException e) {
                }
                if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                    if ((searchBy.equalsIgnoreCase("mFileSequence.id") || searchBy.equalsIgnoreCase("mFileType.id") || searchBy.equalsIgnoreCase("mFileTypeDetail.id")) && value != null) {
                        if (StringUtils.isNotBlank(value)) {
                            searchCriteria.add(new KeyValue(searchBy, value, true));
                        }
                    } else if (searchBy.equalsIgnoreCase("startDate") || searchBy.equalsIgnoreCase("endDate") || searchBy.equalsIgnoreCase("txTmPriorList.priorDate")) {
                        if (StringUtils.isNotBlank(value)) {
                            try {
                                searchCriteria.add(new KeyValue(searchBy, DateUtil.toDate("dd/MM/yyyy", value), true));
                            } catch (ParseException e) {
                            }
                        }
                    }
                    else {
                        if (StringUtils.isNotBlank(value)) {
                            if (searchBy.equals("txTmBrand.name")) {
                                value = value.replaceAll("\\*", "_");
                                orderBy = "4";
                                orderType = "ASC";
                            }

                            if (searchBy.equalsIgnoreCase("txTmClassList")) {
                                String[] classList = value.split(",");
                                for ( int ii = 0; ii < classList.length; ii++ ) {
                                    searchCriteria.add(new KeyValue(searchBy, classList[ii], true));
                                }
                            } else {
                                searchCriteria.add(new KeyValue(searchBy, value, false));
                            }
                        }
                    }
                }
            }
        }

        searchCriteria.add(new KeyValue("statusOnOff", StatusEnum.IPT_DRAFT.name(), true));

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
                        orderBy = "txTmBrand.mBrandType.name";
                        break;
                    case "4":
                        orderBy = "txTmBrand.name";
                        break;
                    case "5":
                        orderBy = "mClass.no";
                        break;
                    case "6":
                        orderBy = "txTmOwner.name";
                        break;
                    case "7":
                        orderBy = "txTmReprs.name";
                        break;
                    case "8":
                        orderBy = "mStatus.name";
                        break;
                    case "9":
                        orderBy = "txTmPriorList.priorDate";
                        break;
                    case "10":
                        orderBy = "txRegistration.no";
                        break;
                    default:
                        orderBy = "txReception.mFileTypeDetail.id";
                        break;
                }
            }
        }

        if (orderType == null) {
            orderType = "ASC";
        } else {
            orderType = orderType.trim();
            if (!orderType.equalsIgnoreCase("DESC")) {
                orderType = "ASC";
            }
        }


        List<JSONTemplate> results = new ArrayList<>();
        try {
            int retrieved = 0;
            int totalRecord = (int) trademarkService.countAll(searchCriteria);
            Transaction transaction = new Transaction();
            if (totalRecord > 0) {
                TxTmGeneralCustomRepository.withJoin = false;
                List<TxTmGeneral> retrievedDataList = trademarkService.selectAll(searchCriteria, orderBy, orderType, retrieved, 100000);

                for (TxTmGeneral result : retrievedDataList) {
                    String brandName = "-";
                    String brandType = "-";
                    String status = "-";
                    String sbOwnerNameList = "-";
                    String sbClassList = "-";
                    String reprsList = "-";
                    ArrayList<String> kelas = new ArrayList<String>();
                    ArrayList<String> owners = new ArrayList<String>();
                    ArrayList<String> reprs = new ArrayList<String>();
                    String mFileTypeDesc = "-";
                    String mFileTypeDetailDesc = "-";
                    try {
                        mFileTypeDesc = result.getTxReception().getmFileType().getDesc();
                    } catch (Exception e) {
                        // e.printStackTrace();
                    }

                    try {
                        mFileTypeDetailDesc = result.getTxReception().getmFileTypeDetail().getDesc();
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }

                    String reception_date = "-";
                    try {
                        reception_date =  new SimpleDateFormat("dd/MM/yyyy").format(result.getTxReception().getApplicationDate());
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }

                    String username = "-";
                    try {
                        username = result.getCreatedBy().getUsername();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    try {
                        brandName = result.getTxTmBrand().getName();
                    } catch (NullPointerException e) {
                        //e.printStackTrace();
                    }

                    try {
                        brandType = result.getTxTmBrand().getmBrandType().getName();
                    } catch (NullPointerException e) {
                        //e.printStackTrace();
                    }

                    String brandTranslation = "-";
                    try {
                        brandTranslation = result.getTxTmBrand().getTranslation();
                    } catch (NullPointerException e) {
                        //e.printStackTrace();
                    }
                    String brandDescription = "-";
                    try {
                        if(result.getTxTmBrand().getDescription()!=null) {
                            brandDescription = result.getTxTmBrand().getDescription();
                        }
                    } catch (NullPointerException e) {
                        //e.printStackTrace();
                    }
                    try {
                        status = result.getmStatus().getName();
                    } catch (NullPointerException e) {
                        //e.printStackTrace();
                    }

                    String regNo = "-";
                    try {
                        regNo = result.getTxRegistration().getNo();
                    } catch (NullPointerException e) {
                        //e.printStackTrace();
                    }
                    String regStartDate = "-";
                    try {
                        regStartDate = new SimpleDateFormat("dd/MM/yyyy").format(result.getTxRegistration().getStartDate());
                    } catch (NullPointerException e) {
                        //e.printStackTrace();
                    }
                    String regEndDate = "-";
                    try {
                        regEndDate = new SimpleDateFormat("dd/MM/yyyy").format(result.getTxRegistration().getEndDate());
                    } catch (NullPointerException e) {
                        //e.printStackTrace();
                    }
                    try {
                        for (TxTmOwner txTmOwner : result.getTxTmOwner()) {
                            owners.add("" + txTmOwner.getName());
                        }
                        Set<String> temp = new LinkedHashSet<String>(owners);
                        String[] unique = temp.toArray(new String[temp.size()]);
                        if (unique.length > 0) {
                            sbOwnerNameList = String.join(",", unique);
                        }
                        /*ownerName = result.getTxTmOwner().getName();*/
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    try {
                        for (TxTmRepresentative txTmRepresentative : result.getTxTmRepresentative()) {
                            reprs.add("" + txTmRepresentative.getName());
                        }
                        Set<String> tempr = new LinkedHashSet<String>(reprs);
                        String[] uniquer = tempr.toArray(new String[tempr.size()]);
                        if (uniquer.length > 0) {
                            reprsList = String.join(",", uniquer);
                        }
                        /*ownerName = result.getTxTmOwner().getName();*/
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    for (TxTmClass txTmClass : result.getTxTmClassList()) {
                        if(!kelas.contains(""+txTmClass.getmClass().getNo())) {
                            kelas.add(""+txTmClass.getmClass().getNo());
                        }
                    }
                    Set<String> temp = new LinkedHashSet<String>(kelas);
                    String[] unique = temp.toArray(new String[temp.size()]);
                    if (unique.length > 0) {
                        sbClassList = String.join(",", unique);
                    }

                    String journalNo = "-";
                    String journalPubDAte = "-";
                    if( result.getTxGroupDetail().size()>0){
                        if(result.getTxGroupDetail().get(0).getTxGroup().getTxPubsJournal()!=null){
                            journalNo = result.getTxGroupDetail().get(0).getTxGroup().getTxPubsJournal().getJournalNo();
                            journalPubDAte = new SimpleDateFormat("dd/MM/yyyy").format(result.getTxGroupDetail().get(0).getTxGroup().getTxPubsJournal().getJournalStart());
                        }
                    }

                    String classDetailsDesc = "-";
                    for (TxTmClass r :  result.getTxTmClassList()) {
                        int key = r.getmClass().getNo();
                        String desc = r.getmClassDetail().getDesc();
                        String descEn = r.getmClassDetail().getDescEn();

                        if(classDetailsDesc.equalsIgnoreCase("")){

                        } else {
                            classDetailsDesc += "; " + desc;
                        }
                    }

                    JSONTemplate jtemp = new JSONTemplate();
                    jtemp.setNo_permohonan(result.getApplicationNo());
                    jtemp.setNo_sertifikat(regNo);
                    jtemp.setPublication_nbr(journalNo);
                    jtemp.setPemohon(sbOwnerNameList);
                    jtemp.setTitle("-");
                    jtemp.setReception_date(reception_date);
                    jtemp.setFiling_date(new SimpleDateFormat("dd/MM/yyyy").format(result.getFilingDate()));
                    jtemp.setRegistration_date(regStartDate);
                    jtemp.setExpiration_date(regEndDate);
                    jtemp.setStatus(status);
                    jtemp.setPublication_date(journalPubDAte);
                    jtemp.setEntitlement_date("0000-00-00");
                    jtemp.setTranslation(brandTranslation);
                    jtemp.setJenis(brandDescription);
                    jtemp.setNice_class_code(String.join(",",kelas));
                    jtemp.setNice_class_descr(classDetailsDesc);
                    results.add(jtemp);
                }
                retrieved += retrievedDataList.size();
            }
            HashMap<String, List<JSONTemplate>> map = new HashMap<>();
            map.put("results", results);
            writeJsonResponse(response, map);
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex);
        } finally {

        }
    }

    @RequestMapping(value = REQUEST_MAPPING_DOWNLOAD_DOKUMEN_LAMPIRAN_PETUGAS, method = {RequestMethod.GET})
    @ResponseBody
    public String doDownloadDokumenPetugas(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

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


    @RequestMapping(value = REQUEST_MAPPING_PINDAH_PERMOHONAN_POST, method = {RequestMethod.POST})
    @ResponseBody
    public void doPindahPermohonanPost(Model model, @RequestParam("listId") String listId,@RequestParam("userBaru") String UserEmailBaru,
                                       final HttpServletRequest request, final HttpServletResponse response) {
        //System.out.println("doPindahPermohonanPost "+ listId.join(",")+" "+UserEmailBaru);
        if (permohonanService.updatePermohonanEmail(listId,UserEmailBaru)){
            writeJsonResponse(response, 200);
        }else{
            writeJsonResponse(response, 201);
        }
    }

    @RequestMapping(value = REQUEST_MAPPING_HAPUS_PERMOHONAN_POST, method = {RequestMethod.POST})
    @ResponseBody
    public void doHapusPermohonanPost(Model model, @RequestParam("listId") String listId,
                                       final HttpServletRequest request, final HttpServletResponse response) {
        //System.out.println("doHapusPermohonanPost "+ listId.join(","));
        if (permohonanService.deletePermohonan(listId)){
            writeJsonResponse(response, 200);
        }else{
            writeJsonResponse(response, 201);
        }
    }
    
    @RequestMapping(value = REQUEST_MAPPING_PROSES_PERMOHONAN_MONITOR_SEARCH_LIST, method = {RequestMethod.POST})
    public void doSearchProsesPermohonanMonitorTablesList(final HttpServletRequest request, 
    		final HttpServletResponse response, TxTmOwner txMonitor) 
            throws IOException {
    	


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
            String orderBy = request.getParameter("order[0][column]");
            String orderType = request.getParameter("order[0][dir]");
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
                        if ((searchBy.equalsIgnoreCase("mFileSequence.id") || searchBy.equalsIgnoreCase("mFileType.id") || searchBy.equalsIgnoreCase("mFileTypeDetail.id") || searchBy.equalsIgnoreCase("mAction.name")) && !value.isEmpty()) {
                            searchCriteria.add(new KeyValue(searchBy, value, true));
                        } else if (searchBy.equalsIgnoreCase("startDate") || searchBy.equalsIgnoreCase("endDate") ) {
                            try {
                                searchCriteria.add(new KeyValue(searchBy, DateUtil.toDate("dd/MM/yyyy", value), true));
                            } catch (ParseException e) {
                            }
                        } else {
                            if (StringUtils.isNotBlank(value)) {
                                if (searchBy.equals("txTmBrand.name")) {
                                    value = value.replaceAll("\\*", "_");
                                    orderBy = "4";
                                    orderType = "ASC";
                                }
                                if(searchBy.equalsIgnoreCase("mClassDetail")){
                                    MClassDetail mClassDetail = mClassDetailCustomRepository.selectOne("id", value, true);
                                    TxTmClass txTmClass = txTmClassCustomRepository.selectOne("mClassDetail", mClassDetail, true);
//                                  System.out.println("dump class " + txTmClass.getmClassDetail().getId());
                                    searchCriteria.add(new KeyValue("mClassDetail", txTmClass.getmClassDetail().getId(), false));
                                }
                                if (searchBy.equalsIgnoreCase("txTmClassList")) {
                                    String[] classList = value.split(",");
                                    for ( int ii = 0; ii < classList.length; ii++ ) {
                                        searchCriteria.add(new KeyValue(searchBy, classList[ii], true));
                                    }
                                } else {
                                    searchCriteria.add(new KeyValue(searchBy, value, false));
                                }
                            }
                        }
                    }
                }
            }
 
 
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
                            orderBy = null;
                            break;
                        case "4":
                            orderBy = "txTmGeneral.txTmBrand.name";
                            break;
                        case "5":
                            orderBy = null;
                            break;
                        case "6":
                            orderBy = "txTmGeneral.ownerList";
                            break;
                        case "7":
                            orderBy = "txTmGeneral.represList";
                            break;
                        case "8":
                            orderBy = "txTmGeneral.mStatus.name";
                            break;
                        case "9":
                            orderBy = "txTmGeneral.txTmPriorList.priorDate";
                            break;
                        case "10":
                            orderBy = "txTmGeneral.txRegistration.no";
                            break;
                        case "11":
                            orderBy = "txTmGeneral.txReception.mFileTypeDetail.id";
                            break;
                        default:
                            orderBy = "txTmGeneral.filingDate";
                            break;
                    }
                }
            }

            if (orderType == null) {
                orderType = "ASC";
            } else {
                orderType = orderType.trim();
                if (!orderType.equalsIgnoreCase("DESC")) {
                    orderType = "ASC";
                }
            }

  
            List<String[]> data = new ArrayList<>(); 
            
            
           //ganti to txmonitor
           // GenericSearchWrapper<TxTmGeneral> searchResult = trademarkService.searchGeneral(searchCriteria, orderBy, orderType, offset, limit, false);
          
            if(searchCriteria.size() == 0) { 

                searchCriteria.add(new KeyValue("statusOnOff", StatusEnum.IPT_DRAFT.name(), true));
            	GenericSearchWrapper<TxTmGeneral> searchResult = trademarkService.searchGeneral(searchCriteria, "filingDate", orderType, offset, limit, false);
                
            	 if (searchResult.getCount() > 0) {
            		 

                     dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                     dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                     int no = offset;
                     MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                     AtomicInteger atomicInteger = new AtomicInteger(offset+1);
                     
                     for (TxTmGeneral result : searchResult.getList()) { 
                    	 
		                    //no++;
		                    String brandName = "-";
		                    FileInputStream brandLabel = null;
		                    File file = null;
		                    byte[] byteImage = null;
		                    String status = "-";
		                    String aksi="-";
		                    String sbOwnerNameList = "-";
		                    String sbReprsNameList = "-";
		                    String registrationNo = "-";
		                    String sbPriorDateList = "-";
		                    String username="-";
		                    ArrayList<String> owners = new ArrayList<String>();
		                    ArrayList<String> reprs = new ArrayList<String>();
		                    ArrayList<String> spriorDate = new ArrayList<String>();
		                    String classList = "-";
		                    ArrayList<String> kelass = new ArrayList<String>();
		
		                    String filingDate = "";
		                    try {
		                        filingDate = result.getFilingDateTemp(); 
		                    } catch (NullPointerException e) {
		                    } catch (Exception e) {
		                    }
		
		                    String jenisPermohonan = " ";
		                    try {
		                        jenisPermohonan = result.getTxReception().getmFileTypeDetail().getDesc();
		                    } catch (NullPointerException e) {
		                        jenisPermohonan = " ";
		                    } catch (Exception e) {
		                    }
		
		                    try {
		                        brandName = result.getTxTmBrand().getName();
		                    } catch (NullPointerException e) { brandName = "-";} catch (Exception e) { brandName = "-";}
		
		                    try {
		                        String pathFolder ="";
		                        if(result.getTxTmBrand().getFileName() != null) {
		                            pathFolder = DateUtil.formatDate(result.getTxTmBrand().getCreatedDate(), "yyyy/MM/dd/");
		                            file = new File (uploadFileBrandPath + pathFolder + result.getTxTmBrand().getId() + ".jpg");
		                            if (file.isFile() && file.canRead()) {
		                                brandLabel = new FileInputStream(uploadFileBrandPath + pathFolder + result.getTxTmBrand().getId() + ".jpg");
		                            } else {
		                                file = new File (uploadFileBrandPath + uploadFileIpasPath + result.getTxTmBrand().getId() + ".jpg");
		                                if (file.isFile() && file.canRead()) {
		                                    brandLabel = new FileInputStream(uploadFileBrandPath + uploadFileIpasPath + result.getTxTmBrand().getId() + ".jpg");
		                                } else {
		                                    brandLabel = new FileInputStream(defaultLogo);
		                                    System.out.println("File gambar " + result.getTxTmBrand().getId() + ".jpg tidak ditemukan"
		                                            + " di folder "+ uploadFileBrandPath + pathFolder
		                                            + " maupun di di folder " + uploadFileBrandPath + uploadFileIpasPath );
		                                }
		                            }
		
		
		
		                            byteImage = new byte[brandLabel.available()];
		                            brandLabel.read(byteImage);
		                            brandLabel.close();
		                        } 
		                    } catch (NullPointerException e) {
		                    } catch (FileNotFoundException e) {
		                        e.printStackTrace();
		                    } catch (IOException e) {
		                        e.printStackTrace();
		                    }
		
		                    try { 
		                    	status=result.getmStatus()!= null ? result.getmStatus().getName():"-";
		                    	//status = result.getmWorkflowProcess().getStatus() != null ? result.getmWorkflowProcess().getStatus().getName() : "-";
		                    } 
		                    	catch (NullPointerException e) {
		                    }
		                    
		                    try { 
		                    	aksi=result.getmAction() != null ? result.getmAction().getName():"-";
		                    	//aksi = result.getmWorkflowProcessActions().getAction() != null ? result.getmWorkflowProcessActions().getAction().getName() : "-";
		                    } 
		                    	catch (NullPointerException e) {
		                    }
		
		                    try {
		                        registrationNo = result.getTxRegistration().getNo();
		                    } catch (NullPointerException e) {
		                    }
		                    
		                    try {
		                        username = result.getCreatedBy()!= null ?  result.getCreatedBy().getUsername() : "-" ;
		                    } catch (NullPointerException e) {
		                    }
		
		                    sbOwnerNameList = result.getOwnerList();
		                    if (sbOwnerNameList == null) {
		                        List<TxTmOwner> txOwnerList =  ownerService.selectOneByIdGeneral(result.getId(), true);
		                        try {
		                            for (TxTmOwner txTmOwner : txOwnerList) {
		                                owners.add("" + txTmOwner.getName());
		                            }
		                            Set<String> temp = new LinkedHashSet<String>(owners);
		                            String[] unique = temp.toArray(new String[temp.size()]);
		                            if (unique.length > 0) {
		                                sbOwnerNameList = String.join(",", unique);
		                            }
		                        } catch (NullPointerException e) {
		                        }
		                    }
		 
		                    sbReprsNameList = result.getRepresList();
		 
		                    //sbClassList = result.getTxTmGeneral().getTxTmClassList();
		                    
		                    try {
		                        Map<String, String> classMap = new HashMap<>();
		                        StringBuffer sbClassList = new StringBuffer();
		                        for (TxTmClass txTmClass : result.getTxTmClassList()) {
		                            classMap.put("" + txTmClass.getmClass().getNo(), "" + txTmClass.getmClass().getNo());
		                        }
		                        for (Map.Entry<String, String> map : classMap.entrySet()) {
		                            sbClassList.append(map.getKey());
		                            sbClassList.append(", ");
		                        }
		                        if (sbClassList.length() > 0) {
		                            classList = sbClassList.substring(0, sbClassList.length() - 2);
		                        }
		                    } catch (NullPointerException e) {
		                    }
							 
		 
		                    sbPriorDateList = result.getPriorList();
		
		                    // For user role access button menu
		//                    MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		                    String btnUbah;
		                    String btnUbahPermohonan = "";
		                    if(mUser.hasAccessMenu("P-UT")) {
		                        btnUbahPermohonan = "<a class=\"btn btn-default btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT_UBAH_TIPE + "?no=" + result.getApplicationNo()) + "\">Ubah Tipe</a>";
		                    }
		                    String sFillingDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(new Date(result.getFilingDate().getTime()));
		                     
		                    if (result.getmStatus() != null) { 
		                        if (result.getmStatus().getId().equalsIgnoreCase(StatusEnum.TM_PERMOHONAN_BARU.name())) {
		                        	
		                            btnUbah = "<a class=\"btn btn-default btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT_PERMOHONAN + "?no=" + result.getApplicationNo()) + "\">Ubah</a>";
		                            //String btnMonitor = "<a class=\"btn btn-success btn-xs\" href=\"" + getPathURLAfterLogin(PATH_MONITOR) + "?no=" + result.getApplicationNo() + "\">Monitor</a>";
		
		                            data.add(new String[]{
		                                    "" + atomicInteger.getAndIncrement(),
		                                    "<a  target=\"_blank\" href=\"" + getPathURLAfterLogin(PATH_PRANTINJAU_PERMOHONAN + "?no=" + result.getApplicationNo()) + "\">" + result.getApplicationNo() + "</a>",
		                                    filingDate,
		                                    (byteImage != null ? "data:image/jpeg;base64, " + Base64Utils.encodeToString(byteImage) + "" : ""),
		                                    brandName,
		                                    classList,
		                                    sbOwnerNameList,
		                                    sbReprsNameList,
		                                    status,
		                                    aksi,
		                                    sbPriorDateList,
		                                    registrationNo,
		                                    jenisPermohonan,
		                                    result.getTxReception().getmFileTypeDetail().getDesc(),
		                                    username,
		                                    "<div class=\"btn-actions\">" + btnUbah + "<br/>" + // "<br/>" + btnMonitor+"<br/>" +
		                                            btnUbahPermohonan + "</div>",
		
		                            });
		                        } else { 
		                        	
		                            String btnMonitor = "<a class=\"btn btn-success btn-xs\" href=\"" + getPathURLAfterLogin(PATH_MONITOR) + "?no=" + result.getApplicationNo() + "\" target=\"_blank\">Monitor</a> ";
		                            String btnCetak = "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin(PATH_CETAK_MEREK) + "?no=" + result.getApplicationNo() + "\" target=\"_blank\">Tanda Terima</a>  ";
		                            if (jenisPermohonan.equalsIgnoreCase("Madrid OO")) {
		                                btnCetak =
		                                        "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin(PATH_CETAK_MADRID) + "?no=" + result.getApplicationNo() + "\" target=\"_blank\">Tanda Terima Madrid</a>  ";
		                            }
		                            String btnCetakSuratPernyataan = "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin(PATH_CETAK_SURAT_PERNYATAAN) + "?no=" + result.getApplicationNo() + "\" target=\"_blank\">Surat Pernyataan</a>";
		                            data.add(new String[]{
		                                    "" + atomicInteger.getAndIncrement(),
		                                    "<a target=\"_blank\" href=\"" + getPathURLAfterLogin(PATH_PRANTINJAU_PERMOHONAN + "?no=" + result.getApplicationNo()) + "\">" + result.getApplicationNo() + "</a>",
		                                    filingDate,
		                                    (byteImage != null ? "data:image/jpeg;base64, " + Base64Utils.encodeToString(byteImage) + "" : ""),
		                                    brandName,
		                                    classList,
		                                    sbOwnerNameList,
		                                    sbReprsNameList,
		                                    status,
		                                    aksi,
		                                    sbPriorDateList,
		                                    registrationNo,
		                                    jenisPermohonan,
		                                    "<div class=\"btn-actions\">" + (result.isOnlineFlag() ? btnCetak : "") + "<br/>" + (result.isOnlineFlag() ? btnCetakSuratPernyataan : "") + "<br/>" + btnMonitor + "<br/>" +
		                                            btnUbahPermohonan + "</div>",
		                                    username
		                            });
		                        }
		                    } else { 
		                    	
		                        String btnMonitor = "<a class=\"btn btn-success btn-xs\" href=\"" + getPathURLAfterLogin(PATH_MONITOR) + "?no=" + result.getApplicationNo() + "\" target=\"_blank\">Monitor</a>";
		                        String btnCetak = "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin(PATH_CETAK_MEREK) + "?no=" + result.getApplicationNo() + "\" target=\"_blank\">Tanda Terima</a> ";
		                        if (jenisPermohonan.equalsIgnoreCase("Madrid OO")) {
		                            btnCetak =
		                                    "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin(PATH_CETAK_MADRID) + "?no=" + result.getApplicationNo() + "\" target=\"_blank\">Tanda Terima Madrid</a>  ";
		                        }
		                        String btnCetakSuratPernyataan = "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin(PATH_CETAK_SURAT_PERNYATAAN) + "?no=" + result.getApplicationNo() + "\" target=\"_blank\">Surat Pernyataan</a>";
		                        data.add(new String[]{
		                                "" + atomicInteger.getAndIncrement(),
		                                "<a target=\"_blank\" href=\"" + getPathURLAfterLogin(PATH_PRANTINJAU_PERMOHONAN + "?no=" + result.getApplicationNo()) + "\">" + result.getApplicationNo() + "</a>",
		                                filingDate,
		                                (byteImage != null ? "data:image/jpeg;base64, " + Base64Utils.encodeToString(byteImage) + "" : ""),
		                                brandName,
		                                classList,
		                                sbOwnerNameList,
		                                sbReprsNameList,
		                                status,
		                                aksi,
		                                sbPriorDateList,
		                                registrationNo,
		                                jenisPermohonan,
		                                "<div class=\"btn-actions\">" + (result.getTxReception().isOnlineFlag() ? btnCetak : "") + "<br/>" + (result.isOnlineFlag() ? btnCetakSuratPernyataan : "") + "<br/>"
		                                        + btnMonitor + "<br/>" + btnUbahPermohonan + "</div>",
		                                username
		                        });
		                    } 
		                
                    	 
                     }
                 
            		 
            	 }
            
            
            
            
            }
            else  {
            	GenericSearchWrapper<TxMonitor> searchResult = monitorService.searchProsesPermohonanMonitor(searchCriteria, orderBy, orderType, offset, limit);

			            if (searchResult.getCount() > 0) {
			                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
			                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());
			
			                int no = offset;
			                MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			                AtomicInteger atomicInteger = new AtomicInteger(offset+1);
			                 
			
			                for (TxMonitor result : searchResult.getList()) {
			
			                    //no++;
			                    String brandName = "-";
			                    FileInputStream brandLabel = null;
			                    File file = null;
			                    byte[] byteImage = null;
			                    String status = "-";
			                    String aksi="-";
			                    String sbOwnerNameList = "-";
			                    String sbReprsNameList = "-";
			                    String registrationNo = "-";
			                    String sbPriorDateList = "-";
			                    String username="-";
			                    ArrayList<String> owners = new ArrayList<String>();
			                    ArrayList<String> reprs = new ArrayList<String>();
			                    ArrayList<String> spriorDate = new ArrayList<String>();
			                    String classList = "-";
			                    ArrayList<String> kelass = new ArrayList<String>();
			
			                    String filingDate = "";
			                    try {
			                        filingDate = result.getTxTmGeneral().getFilingDateTemp(); 
			                    } catch (NullPointerException e) {
			                    } catch (Exception e) {
			                    }
			
			                    String jenisPermohonan = " ";
			                    try {
			                        jenisPermohonan = result.getTxTmGeneral().getTxReception().getmFileTypeDetail().getDesc();
			                    } catch (NullPointerException e) {
			                        jenisPermohonan = " ";
			                    } catch (Exception e) {
			                    }
			
			                    try {
			                        brandName = result.getTxTmGeneral().getTxTmBrand().getName();
			                    } catch (NullPointerException e) { brandName = "-";} catch (Exception e) { brandName = "-";}
			
			                    try {
			                        String pathFolder ="";
			                        if(result.getTxTmGeneral().getTxTmBrand().getFileName() != null) {
			                            pathFolder = DateUtil.formatDate(result.getTxTmGeneral().getTxTmBrand().getCreatedDate(), "yyyy/MM/dd/");
			                            file = new File (uploadFileBrandPath + pathFolder + result.getTxTmGeneral().getTxTmBrand().getId() + ".jpg");
			                            if (file.isFile() && file.canRead()) {
			                                brandLabel = new FileInputStream(uploadFileBrandPath + pathFolder + result.getTxTmGeneral().getTxTmBrand().getId() + ".jpg");
			                            } else {
			                                file = new File (uploadFileBrandPath + uploadFileIpasPath + result.getTxTmGeneral().getTxTmBrand().getId() + ".jpg");
			                                if (file.isFile() && file.canRead()) {
			                                    brandLabel = new FileInputStream(uploadFileBrandPath + uploadFileIpasPath + result.getTxTmGeneral().getTxTmBrand().getId() + ".jpg");
			                                } else {
			                                    brandLabel = new FileInputStream(defaultLogo);
			                                    System.out.println("File gambar " + result.getTxTmGeneral().getTxTmBrand().getId() + ".jpg tidak ditemukan"
			                                            + " di folder "+ uploadFileBrandPath + pathFolder
			                                            + " maupun di di folder " + uploadFileBrandPath + uploadFileIpasPath );
			                                }
			                            }
			
			
			
			                            byteImage = new byte[brandLabel.available()];
			                            brandLabel.read(byteImage);
			                            brandLabel.close();
			                        } 
			                    } catch (NullPointerException e) {
			                    } catch (FileNotFoundException e) {
			                        e.printStackTrace();
			                    } catch (IOException e) {
			                        e.printStackTrace();
			                    }
			
			                    try { 
			                    	status=result.getTxTmGeneral().getmStatus()!= null ? result.getTxTmGeneral().getmStatus().getName():"-";
			                    	//status = result.getmWorkflowProcess().getStatus() != null ? result.getmWorkflowProcess().getStatus().getName() : "-";
			                    } 
			                    	catch (NullPointerException e) {
			                    }
			                    
			                    try { 
			                    	aksi=result.getTxTmGeneral().getmAction() != null ? result.getTxTmGeneral().getmAction().getName():"-";
			                    	//aksi = result.getmWorkflowProcessActions().getAction() != null ? result.getmWorkflowProcessActions().getAction().getName() : "-";
			                    } 
			                    	catch (NullPointerException e) {
			                    }
			
			                    try {
			                        registrationNo = result.getTxTmGeneral().getTxRegistration().getNo();
			                    } catch (NullPointerException e) {
			                    }
			                    
			                    try {
			                        username = result.getTxTmGeneral().getCreatedBy()!= null ?  result.getTxTmGeneral().getCreatedBy().getUsername() : "-" ;
			                    } catch (NullPointerException e) {
			                    }
			
			                    sbOwnerNameList = result.getTxTmGeneral().getOwnerList();
			                    if (sbOwnerNameList == null) {
			                        List<TxTmOwner> txOwnerList =  ownerService.selectOneByIdGeneral(result.getId(), true);
			                        try {
			                            for (TxTmOwner txTmOwner : txOwnerList) {
			                                owners.add("" + txTmOwner.getName());
			                            }
			                            Set<String> temp = new LinkedHashSet<String>(owners);
			                            String[] unique = temp.toArray(new String[temp.size()]);
			                            if (unique.length > 0) {
			                                sbOwnerNameList = String.join(",", unique);
			                            }
			                        } catch (NullPointerException e) {
			                        }
			                    }
			 
			                    sbReprsNameList = result.getTxTmGeneral().getRepresList();
			 
			                    //sbClassList = result.getTxTmGeneral().getTxTmClassList();
			                    
			                    try {
			                        Map<String, String> classMap = new HashMap<>();
			                        StringBuffer sbClassList = new StringBuffer();
			                        for (TxTmClass txTmClass : result.getTxTmGeneral().getTxTmClassList()) {
			                            classMap.put("" + txTmClass.getmClass().getNo(), "" + txTmClass.getmClass().getNo());
			                        }
			                        for (Map.Entry<String, String> map : classMap.entrySet()) {
			                            sbClassList.append(map.getKey());
			                            sbClassList.append(", ");
			                        }
			                        if (sbClassList.length() > 0) {
			                            classList = sbClassList.substring(0, sbClassList.length() - 2);
			                        }
			                    } catch (NullPointerException e) {
			                    }
								 
			 
			                    sbPriorDateList = result.getTxTmGeneral().getPriorList();
			
			                    // For user role access button menu
			                    // MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			                    String btnUbah;
			                    String btnUbahPermohonan = "";
			                    if(mUser.hasAccessMenu("P-UT")) {
			                        btnUbahPermohonan = "<a class=\"btn btn-default btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT_UBAH_TIPE + "?no=" + result.getTxTmGeneral().getApplicationNo()) + "\">Ubah Tipe</a>";
			                    }
			                    String sFillingDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(new Date(result.getTxTmGeneral().getFilingDate().getTime()));
			                     
			                    if (result.getTxTmGeneral().getmStatus() != null) { 
			                        if (result.getTxTmGeneral().getmStatus().getId().equalsIgnoreCase(StatusEnum.TM_PERMOHONAN_BARU.name())) {
			                        	
			                            btnUbah = "<a class=\"btn btn-default btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT_PERMOHONAN + "?no=" + result.getTxTmGeneral().getApplicationNo()) + "\">Ubah</a>";
			                            //String btnMonitor = "<a class=\"btn btn-success btn-xs\" href=\"" + getPathURLAfterLogin(PATH_MONITOR) + "?no=" + result.getApplicationNo() + "\">Monitor</a>";
			
			                            data.add(new String[]{
			                                    "" + atomicInteger.getAndIncrement(),
			                                    "<a  target=\"_blank\" href=\"" + getPathURLAfterLogin(PATH_PRANTINJAU_PERMOHONAN + "?no=" + result.getTxTmGeneral().getApplicationNo()) + "\">" + result.getTxTmGeneral().getApplicationNo() + "</a>",
			                                    filingDate,
			                                    (byteImage != null ? "data:image/jpeg;base64, " + Base64Utils.encodeToString(byteImage) + "" : ""),
			                                    brandName,
			                                    classList,
			                                    sbOwnerNameList,
			                                    sbReprsNameList,
			                                    status,
			                                    aksi,
			                                    sbPriorDateList,
			                                    registrationNo,
			                                    jenisPermohonan,
			                                    result.getTxTmGeneral().getTxReception().getmFileTypeDetail().getDesc(),
			                                    username,
			                                    "<div class=\"btn-actions\">" + btnUbah + "<br/>" + // "<br/>" + btnMonitor+"<br/>" +
			                                            btnUbahPermohonan + "</div>",
			
			                            });
			                        } else { 
			                        	
			                            String btnMonitor = "<a class=\"btn btn-success btn-xs\" href=\"" + getPathURLAfterLogin(PATH_MONITOR) + "?no=" + result.getTxTmGeneral().getApplicationNo() + "\" target=\"_blank\">Monitor</a> ";
			                            String btnCetak = "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin(PATH_CETAK_MEREK) + "?no=" + result.getTxTmGeneral().getApplicationNo() + "\" target=\"_blank\">Tanda Terima</a>  ";
			                            if (jenisPermohonan.equalsIgnoreCase("Madrid OO")) {
			                                btnCetak =
			                                        "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin(PATH_CETAK_MADRID) + "?no=" + result.getTxTmGeneral().getApplicationNo() + "\" target=\"_blank\">Tanda Terima Madrid</a>  ";
			                            }
			                            String btnCetakSuratPernyataan = "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin(PATH_CETAK_SURAT_PERNYATAAN) + "?no=" + result.getTxTmGeneral().getApplicationNo() + "\" target=\"_blank\">Surat Pernyataan</a>";
			                            data.add(new String[]{
			                                    "" + atomicInteger.getAndIncrement(),
			                                    "<a target=\"_blank\" href=\"" + getPathURLAfterLogin(PATH_PRANTINJAU_PERMOHONAN + "?no=" + result.getTxTmGeneral().getApplicationNo()) + "\">" + result.getTxTmGeneral().getApplicationNo() + "</a>",
			                                    filingDate,
			                                    (byteImage != null ? "data:image/jpeg;base64, " + Base64Utils.encodeToString(byteImage) + "" : ""),
			                                    brandName,
			                                    classList,
			                                    sbOwnerNameList,
			                                    sbReprsNameList,
			                                    status,
			                                    aksi,
			                                    sbPriorDateList,
			                                    registrationNo,
			                                    jenisPermohonan,
			                                    "<div class=\"btn-actions\">" + (result.getTxTmGeneral().isOnlineFlag() ? btnCetak : "") + "<br/>" + (result.getTxTmGeneral().isOnlineFlag() ? btnCetakSuratPernyataan : "") + "<br/>" + btnMonitor + "<br/>" +
			                                            btnUbahPermohonan + "</div>",
			                                    username
			                            });
			                        }
			                    } else { 
			                    	
			                        String btnMonitor = "<a class=\"btn btn-success btn-xs\" href=\"" + getPathURLAfterLogin(PATH_MONITOR) + "?no=" + result.getTxTmGeneral().getApplicationNo() + "\" target=\"_blank\">Monitor</a>";
			                        String btnCetak = "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin(PATH_CETAK_MEREK) + "?no=" + result.getTxTmGeneral().getApplicationNo() + "\" target=\"_blank\">Tanda Terima</a> ";
			                        if (jenisPermohonan.equalsIgnoreCase("Madrid OO")) {
			                            btnCetak =
			                                    "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin(PATH_CETAK_MADRID) + "?no=" + result.getTxTmGeneral().getApplicationNo() + "\" target=\"_blank\">Tanda Terima Madrid</a>  ";
			                        }
			                        String btnCetakSuratPernyataan = "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin(PATH_CETAK_SURAT_PERNYATAAN) + "?no=" + result.getTxTmGeneral().getApplicationNo() + "\" target=\"_blank\">Surat Pernyataan</a>";
			                        data.add(new String[]{
			                                "" + atomicInteger.getAndIncrement(),
			                                "<a target=\"_blank\" href=\"" + getPathURLAfterLogin(PATH_PRANTINJAU_PERMOHONAN + "?no=" + result.getTxTmGeneral().getApplicationNo()) + "\">" + result.getTxTmGeneral().getApplicationNo() + "</a>",
			                                filingDate,
			                                (byteImage != null ? "data:image/jpeg;base64, " + Base64Utils.encodeToString(byteImage) + "" : ""),
			                                brandName,
			                                classList,
			                                sbOwnerNameList,
			                                sbReprsNameList,
			                                status,
			                                aksi,
			                                sbPriorDateList,
			                                registrationNo,
			                                jenisPermohonan,
			                                "<div class=\"btn-actions\">" + (result.getTxTmGeneral().getTxReception().isOnlineFlag() ? btnCetak : "") + "<br/>" + (result.getTxTmGeneral().isOnlineFlag() ? btnCetakSuratPernyataan : "") + "<br/>"
			                                        + btnMonitor + "<br/>" + btnUbahPermohonan + "</div>",
			                                username
			                        });
			                    } 
			                }
			            }
                  }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
 
    
    	
    }
    
    @GetMapping(REQUEST_EXPORT_PROSES_PERMOHONAN) 
    public void doExportProsesPermohonan(HttpServletRequest request, HttpServletResponse response) {
        InputStream reportInputStream = null;

        String[] searchByArr = null;
        try {
            searchByArr = request.getParameter("searchByArr").split(",");
        } catch (Exception e) {
        }
        String[] keywordArr = null;
        try {
            keywordArr = request.getParameter("keywordArr").split(",");
        } catch (Exception e) {
        }
        String orderBy = request.getParameter("orderBy");
        String orderType = request.getParameter("orderType");

        List<KeyValue> searchCriteria = new ArrayList<>();

        if (searchByArr != null) {
            for (int i = 0; i < searchByArr.length; i++) {
                String searchBy = searchByArr[i];
                String value = null;
                try {
                    value = keywordArr[i];
                } catch (ArrayIndexOutOfBoundsException e) {
                }
                if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                    if ((searchBy.equalsIgnoreCase("mFileSequence.id") || searchBy.equalsIgnoreCase("mFileType.id") || searchBy.equalsIgnoreCase("mFileTypeDetail.id")) && value != null) {
                        if (StringUtils.isNotBlank(value)) {
                            searchCriteria.add(new KeyValue(searchBy, value, true));
                        }
                    } else if (searchBy.equalsIgnoreCase("startDate") || searchBy.equalsIgnoreCase("endDate") || searchBy.equalsIgnoreCase("txTmPriorList.priorDate")) {
                        if (StringUtils.isNotBlank(value)) {
                            try {
                                searchCriteria.add(new KeyValue(searchBy, DateUtil.toDate("dd/MM/yyyy", value), true));
                            } catch (ParseException e) {
                            }
                        }
                    }
                    else {
                        if (StringUtils.isNotBlank(value)) {
                            if (searchBy.equals("txTmBrand.name")) {
                                value = value.replaceAll("\\*", "_");
                                orderBy = "4";
                                orderType = "ASC";
                            }

                            if (searchBy.equalsIgnoreCase("txTmClassList")) {
                                String[] classList = value.split(",");
                                for ( int ii = 0; ii < classList.length; ii++ ) {
                                    searchCriteria.add(new KeyValue(searchBy, classList[ii], true));
                                }
                            } else {
                                searchCriteria.add(new KeyValue(searchBy, value, false));
                            }
                        }
                    }
                }
            }
        }

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
	                    orderBy = null;
	                    break;
	                case "4":
	                    orderBy = "txTmGeneral.txTmBrand.name";
	                    break;
	                case "5":
	                    orderBy = null;
	                    break;
	                case "6":
	                    orderBy = "txTmGeneral.ownerList";
	                    break;
	                case "7":
	                    orderBy = "txTmGeneral.represList";
	                    break;
	                case "8":
	                    orderBy = "txTmGeneral.mStatus.name";
	                    break;
	                case "9":
	                    orderBy = "txTmGeneral.txTmPriorList.priorDate";
	                    break;
	                case "10":
	                    orderBy = "txTmGeneral.txRegistration.no";
	                    break;
	                case "11":
	                    orderBy = "txTmGeneral.txReception.mFileTypeDetail.id";
	                    break;
	                default:
	                    orderBy = "txTmGeneral.filingDate";
	                    break;
                }
            }
        }

        if (orderType == null) {
            orderType = "ASC";
        } else {
            orderType = orderType.trim();
            if (!orderType.equalsIgnoreCase("DESC")) {
                orderType = "ASC";
            }
        }
 
        try {
            reportInputStream = getClass().getClassLoader().getResourceAsStream("report/list-proses-permohonan.xls");
 
            List<DataGeneral> dataList = new ArrayList<>();
            int retrieved = 0;
            
            GenericSearchWrapper<TxMonitor> searchResult = monitorService.searchProsesPermohonanMonitor(searchCriteria, orderBy, orderType, retrieved, 100000);
            
            DataGeneral datagen = null; 

            int totalRecord = (int) searchResult.getCount(); 
            String downloadDate = DateUtil.formatDate(new Timestamp(System.currentTimeMillis()), "dd MMMM YYYY");
            //No.	Nomor_Permohonan	Tanggal_Penerimaan	Tipe_Merek	Nama_Merek	Kelas	Nama_Pemohon	Status
            int no = 0; 
            if (totalRecord > 0) {
                TxTmGeneralCustomRepository.withJoin = false;
                 
                List<TxMonitor> retrievedMonitorList = searchResult.getList();
                List<TxTmGeneral> retrievedDataList = new ArrayList<>();

                 for (TxMonitor txMonitor : retrievedMonitorList){
                     if(txMonitor!= null){
                         TxTmGeneral txTmGeneral = new TxTmGeneral();
                         txTmGeneral = txMonitor.getTxTmGeneral();
                         retrievedDataList.add(txTmGeneral);
                     }
                 }


                 for (TxMonitor result : retrievedMonitorList) {
                    no++;
                    String brandName = "-";
                    String brandType = "-";
                    String status = "-";
                    String sbOwnerNameList = "-";
                    String sbClassList = "-";
                    String reprsList = "-";
                    ArrayList<String> kelas = new ArrayList<String>();
                    ArrayList<String> owners = new ArrayList<String>();
                    ArrayList<String> reprs = new ArrayList<String>();
                    //String sFillingDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(result.getFilingDate().getTime()));

                    String mFileTypeDesc = "-";
                    String mFileTypeDetailDesc = "-";
                    try {
                        mFileTypeDesc = result.getTxTmGeneral().getTxReception().getmFileType().getDesc();
                    } catch (Exception e) {
                    }

                    try {
                        mFileTypeDetailDesc = result.getTxTmGeneral().getTxReception().getmFileTypeDetail().getDesc();
                    } catch (Exception e) {
                    }

                    String username = "-";
                    try {
                        username = result.getTxTmGeneral().getCreatedBy().getUsername();
                    } catch (NullPointerException e) {
                    }

                    try {
                        brandName = result.getTxTmGeneral().getTxTmBrand().getName();
                    } catch (NullPointerException e) {
                    }

                    try {
                        brandType = result.getTxTmGeneral().getTxTmBrand().getmBrandType().getName();
                    } catch (NullPointerException e) {
                    }

                    try {
                        status = result.getTxTmGeneral().getmStatus().getName();
                    } catch (NullPointerException e) {
                    }

                    String regNo = "-";
                    try {
                        regNo = result.getTxTmGeneral().getTxRegistration().getNo();
                    } catch (NullPointerException e) {
                    }

                    try {
                        for (TxTmOwner txTmOwner : result.getTxTmGeneral().getTxTmOwner()) {
                            owners.add("" + txTmOwner.getName());
                        }
                        Set<String> temp = new LinkedHashSet<String>(owners);
                        String[] unique = temp.toArray(new String[temp.size()]);
                        if (unique.length > 0) {
                            sbOwnerNameList = String.join(",", unique);
                        }
                        /*ownerName = result.getTxTmOwner().getName();*/
                    } catch (NullPointerException e) {
                    }

                    try {
                        for (TxTmRepresentative txTmRepresentative : result.getTxTmGeneral().getTxTmRepresentative()) {
                            reprs.add("" + txTmRepresentative.getName());
                        }
                        Set<String> tempr = new LinkedHashSet<String>(reprs);
                        String[] uniquer = tempr.toArray(new String[tempr.size()]);
                        if (uniquer.length > 0) {
                            reprsList = String.join(",", uniquer);
                        }
                        /*ownerName = result.getTxTmOwner().getName();*/
                    } catch (NullPointerException e) {
                    }

                    for (TxTmClass txTmClass : result.getTxTmGeneral().getTxTmClassList()) {
                        kelas.add("" + txTmClass.getmClass().getNo());
                    }
                    Set<String> temp = new LinkedHashSet<String>(kelas);
                    String[] unique = temp.toArray(new String[temp.size()]);
                    if (unique.length > 0) {
                        sbClassList = String.join(",", unique);
                    }

                    String bankCode = "-";
                    try {
                        bankCode = result.getTxTmGeneral().getTxReception().getBankCode();
                    } catch (NullPointerException e) {
                    }


                    datagen = new DataGeneral();
                    datagen.setNo(no);
                    datagen.setApplicationNo(result.getTxTmGeneral().getApplicationNo());
                    datagen.setBankCode(bankCode);
                    datagen.setFillingDate(result.getTxTmGeneral().getFilingDateTemp());
                    datagen.setmFileType(mFileTypeDesc);
                    datagen.setBrandType(brandType);
                    datagen.setBrandName(brandName);
                    datagen.setKelas(sbClassList);
                    datagen.setPriorName(sbOwnerNameList);
                    datagen.setStatus(status);
                    datagen.setUsername(username);
                    datagen.setmFileTypeDetail(mFileTypeDetailDesc);
                    datagen.setRegNo(regNo);
                    datagen.setReprsName(reprsList);
                    dataList.add(datagen);
                }
                retrieved += retrievedDataList.size();
            }

            Context context = new Context();
            context.putVar("dataList", dataList);
            context.putVar("downloadDate", downloadDate);

            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=list-proses-permohonan.xls");

            JxlsHelper.getInstance().processTemplate(reportInputStream, response.getOutputStream(), context);
            response.getOutputStream().close();
            response.flushBuffer();
        } catch (Exception ex) {
            logger.error(ex);
        } finally {
            if (reportInputStream != null) {
                try {
                    reportInputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }
    

    @GetMapping(REQUEST_EXPORT_SELECTED_PROSES_PERMOHONAN)
    public void doExportSelectedProsesPermohonan(HttpServletRequest request, HttpServletResponse response) {
        InputStream reportInputStream = null; 

        String[] generalAppNoList = null;
        try {
        	generalAppNoList = request.getParameter("selectedIdArr").split(",");
        } catch (Exception e) {
        }
        
        if (generalAppNoList != null) {
            for (int i = 0; i < generalAppNoList.length; i++) {
                String selectedId = generalAppNoList[i];
                System.out.println("selectedId "+ selectedId);
            }
        }
         
	        
	    try { 
            reportInputStream = getClass().getClassLoader().getResourceAsStream("report/list-proses-permohonan.xls");
 
            List<DataGeneral> dataList = new ArrayList<>();  
            List<TxTmGeneral> retrievedDataList =  permohonanService.findAllTxTmGeneralByAppNo(generalAppNoList); 
	        
            DataGeneral datagen = null;
            int retrieved = 0;

            int totalRecord =  retrievedDataList.size();

            String downloadDate = DateUtil.formatDate(new Timestamp(System.currentTimeMillis()), "dd MMMM YYYY");
            
            //No.	Nomor_Permohonan	Tanggal_Penerimaan	Tipe_Merek	Nama_Merek	Kelas	Nama_Pemohon	Status
            int no = 0; 
            if (totalRecord > 0) { 
 
                 
                 for (TxTmGeneral result : retrievedDataList) {
 
                    no++;
                    String brandName = "-";
                    String brandType = "-";
                    String status = "-";
                    String sbOwnerNameList = "-";
                    String sbClassList = "-";
                    String reprsList = "-";
                    ArrayList<String> kelas = new ArrayList<String>();
                    ArrayList<String> owners = new ArrayList<String>();
                    ArrayList<String> reprs = new ArrayList<String>();
                    //String sFillingDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(result.getFilingDate().getTime()));

                    String mFileTypeDesc = "-";
                    String mFileTypeDetailDesc = "-";
                    try {
                        mFileTypeDesc = result.getTxReception().getmFileType().getDesc();
                    } catch (Exception e) {
                    }

                    try {
                        mFileTypeDetailDesc = result.getTxReception().getmFileTypeDetail().getDesc();
                    } catch (Exception e) {
                    }

                    String username = "-";
                    try {
                        username = result.getCreatedBy().getUsername();
                    } catch (NullPointerException e) {
                    }

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

                    String regNo = "-";
                    try {
                        regNo = result.getTxRegistration().getNo();
                    } catch (NullPointerException e) {
                    }

                    try {
                        for (TxTmOwner txTmOwner : result.getTxTmOwner()) {
                            owners.add("" + txTmOwner.getName());
                        }
                        Set<String> temp = new LinkedHashSet<String>(owners);
                        String[] unique = temp.toArray(new String[temp.size()]);
                        if (unique.length > 0) {
                            sbOwnerNameList = String.join(",", unique);
                        }
                        /*ownerName = result.getTxTmOwner().getName();*/
                    } catch (NullPointerException e) {
                    }

                    try {
                        for (TxTmRepresentative txTmRepresentative : result.getTxTmRepresentative()) {
                            reprs.add("" + txTmRepresentative.getName());
                        }
                        Set<String> tempr = new LinkedHashSet<String>(reprs);
                        String[] uniquer = tempr.toArray(new String[tempr.size()]);
                        if (uniquer.length > 0) {
                            reprsList = String.join(",", uniquer);
                        }
                        /*ownerName = result.getTxTmOwner().getName();*/
                    } catch (NullPointerException e) {
                    }

                    for (TxTmClass txTmClass : result.getTxTmClassList()) {
                        kelas.add("" + txTmClass.getmClass().getNo());
                    }
                    Set<String> temp = new LinkedHashSet<String>(kelas);
                    String[] unique = temp.toArray(new String[temp.size()]);
                    if (unique.length > 0) {
                        sbClassList = String.join(",", unique);
                    }

                    String bankCode = "-";
                    try {
                        bankCode = result.getTxReception().getBankCode();
                    } catch (NullPointerException e) {
                    }

                    datagen = new DataGeneral();
                    datagen.setNo(no);
                    datagen.setApplicationNo(result.getApplicationNo());
                    datagen.setBankCode(bankCode);
                    datagen.setFillingDate(result.getFilingDateTemp());
                    datagen.setmFileType(mFileTypeDesc);
                    datagen.setBrandType(brandType);
                    datagen.setBrandName(brandName);
                    datagen.setKelas(sbClassList);
                    datagen.setPriorName(sbOwnerNameList);
                    datagen.setStatus(status);
                    datagen.setUsername(username);
                    datagen.setmFileTypeDetail(mFileTypeDetailDesc);
                    datagen.setRegNo(regNo);
                    datagen.setReprsName(reprsList);
                    dataList.add(datagen);
                }
                retrieved += retrievedDataList.size();
            }

            Context context = new Context();
            context.putVar("dataList", dataList);
            context.putVar("downloadDate", downloadDate);

            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=list-proses-permohonan.xls");

            JxlsHelper.getInstance().processTemplate(reportInputStream, response.getOutputStream(), context);
            response.getOutputStream().close();
            response.flushBuffer();
	    	
    	} catch (Exception ex) {
	        logger.error(ex);
	    } finally {
	        if (reportInputStream != null) {
	            try {
	                reportInputStream.close();
	            } catch (IOException e) {
	            }
	        }
	    } 
    }
    
    
}