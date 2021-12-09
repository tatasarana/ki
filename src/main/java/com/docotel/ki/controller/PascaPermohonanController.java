package com.docotel.ki.controller;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.enumeration.StatusEnum;
import com.docotel.ki.model.master.*;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.pojo.CetakPasca;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.master.MWorkflowProcessRepository;
import com.docotel.ki.repository.transaction.TxPostDocRepository;
import com.docotel.ki.service.master.DocTypeService;
import com.docotel.ki.service.master.FileService;
import com.docotel.ki.service.master.StatusService;
import com.docotel.ki.service.transaction.*;
import com.docotel.ki.signature.PDFSignatureFacade;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.util.FieldValidationUtil;
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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
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
import java.util.*;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class PascaPermohonanController extends BaseController {
    private static final String DIRECTORY_PAGE = "pasca-permohonan/";
    private static final String PAGE_PASCA_PERMOHONAN_LIST = DIRECTORY_PAGE + "list-pasca-permohonan";
    private static final String PAGE_PASCA_PERMOHONAN_PRATINJAU = DIRECTORY_PAGE + "pratinjau-pasca-permohonan";
    private static final String PATH_PASCA_PERMOHONAN_AJAX_SEARCH_LIST = "/cari-pasca-permohonan";
    private static final String PATH_PASCA_PERMOHONAN_AJAX_SEARCH_LIST_DOKUMEN = "/cari-dokumen-pasca-permohonan";
    private static final String PATH_PASCA_PERMOHONAN_LIST = "/list-pasca-permohonan";
    public static final String REDIRECT_PASCA_PERMOHONAN_LIST = "redirect:" + PATH_AFTER_LOGIN + PATH_PASCA_PERMOHONAN_LIST;
    private static final String PATH_PASCA_PERMOHONAN_PRATINJAU = "/pratinjau-pasca-permohonan";
    public static final String PATH_PRANTINJAU_PERMOHONAN = "/pratinjau-permohonan";
    private static final String PATH_PASCA_PERMOHONAN_CETAK = "/cetak-pasca-permohonan";
    private static final String PATH_HAPUS_PASCA_PERMOHONAN = "/hapus-pasca-permohonan";
    private static final String PATH_PASCA_PERMOHONAN_CETAK_REBUILD = "/cetak-pasca-permohonan-rebuild";
    private static final String PATH_PASCA_UPDATE_DATA_DOWNLOAD_DOKUMEN = "/update-data-download-dokumen-pasca";
    private static final String PATH_UBAH_PASCA_PERMOHONAN_DOKUMEN_LAMPIRAN = "/form-edit-dokumen-pasca-permohonan";
    private static final String PATH_AJAX_DELETE_DOKUMEN_PASCA_PERMOHONAN = "/delete-dokumen-pasca-permohonan";
    private static final String PATH_VIEW_DOKUMEN_LAMPIRAN_PASCA_PERMOHONAN = "/lihat-dokumen-lampiran-pasca-permohonan";
    private static final String PATH_UBAH_PASCA_PERMOHONAN_PAGE_DOKUMEN_LAMPIRAN = DIRECTORY_PAGE + "form-edit-dokumen-pasca-permohonan";
    public static final String PATH_MONITOR = "/list-monitor";
    private static final String REQUEST_MAPPING_UBAH_DOKUMEN_PASCA_PERMOHONAN = PATH_UBAH_PASCA_PERMOHONAN_DOKUMEN_LAMPIRAN + "*";
    private static final String REQUEST_MAPPING_PASCA_PERMOHONAN_AJAX_SEARCH_LIST = PATH_PASCA_PERMOHONAN_AJAX_SEARCH_LIST + "*";
    private static final String REQUEST_MAPPING_PASCA_PERMOHONAN_AJAX_SEARCH_LIST_DOKUMEN = PATH_PASCA_PERMOHONAN_AJAX_SEARCH_LIST_DOKUMEN + "*";
    private static final String REQUEST_MAPPING_PASCA_PERMOHONAN_LIST = PATH_PASCA_PERMOHONAN_LIST + "*";
    private static final String REQUEST_MAPPING_PASCA_PERMOHONAN_PRATINJAU = PATH_PASCA_PERMOHONAN_PRATINJAU + "*";
    private static final String REQUEST_MAPPING_PASCA_UPDATE_DATA_DOWNLOAD_DOKUMEN = PATH_PASCA_UPDATE_DATA_DOWNLOAD_DOKUMEN + "*";
    private static final String REQUEST_MAPPING_PASCA_PERMOHONAN_CETAK = PATH_PASCA_PERMOHONAN_CETAK + "*";
    private static final String REQUEST_MAPPING_HAPUS_PERMOHONAN_PASCA_POST = PATH_HAPUS_PASCA_PERMOHONAN + "*";
    private static final String REQUEST_MAPPING_PASCA_PERMOHONAN_CETAK_REBUILD = PATH_PASCA_PERMOHONAN_CETAK_REBUILD + "*";
    private static final String REQUEST_MAPPING_AJAX_DELETE_DOKUMEN_PASCA_PERMOHONAN = PATH_AJAX_DELETE_DOKUMEN_PASCA_PERMOHONAN + "*";
    private static final String REQUEST_MAPPING_VIEW_DOKUMEN_LAMPIRAN_PASCA_PERMOHONAN = PATH_VIEW_DOKUMEN_LAMPIRAN_PASCA_PERMOHONAN + "*";

    @Autowired
    private FileService fileService;
    @Autowired
    private OwnerService ownerService;
    @Autowired
    private DocTypeService docTypeService;
    @Autowired
    private PascaOnlineService pascaOnlineService;
    @Autowired
    private MonitorService monitorService;
    @Autowired
    private TxPostDokumenService txPostDokumenService;
    @Autowired
    private DokumenLampiranService doclampiranService;
    @Autowired
    private MWorkflowProcessRepository mWorkflowProcessRepository;
    @Autowired
    private TxPostDocRepository txPostDocRepository;
    @Autowired
    private StatusService statusService;

    @Value("${upload.file.doc.pasca.path:}")
    private String uploadFileDocPascaPath;
    @Value(("${certificate.file}"))
    private String CERTIFICATE_FILE;
    private FileInputStream fileInputStreamReader;
    @Value("${download.output.pasca.cetakmerek.file.path:}")
    private String downloadFileDocPascaCetakMerekPath;
    @Value("${upload.file.path.signature:}")
    private String downloadImgPath;

    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "permohonanMerek");
        model.addAttribute("subMenu", "pascaPermohonan");
    }

    private void doInitiatePasca(final Model model, final HttpServletRequest request) {
        List<MFileSequence> fileSequenceList = fileService.findMFileSequenceByStatusFlagTrue();
        model.addAttribute("fileSequenceList", fileSequenceList);

        List<MFileType> fileTypeList = fileService.findByMenu("PASCA"); //fileService.findMFileTypeByStatusFlagTrue();
        model.addAttribute("fileTypeList", fileTypeList);

        List<MStatus> statusList = statusService.selectStatus();
        model.addAttribute("statusList", statusList);

        if (request.getRequestURI().contains(PATH_PASCA_PERMOHONAN_PRATINJAU)) {
            String postId = request.getParameter("no");

            TxPostReception txPostReception = pascaOnlineService.selectOneTxPostReceptionById(postId);
            if (txPostReception == null) {
                txPostReception = new TxPostReception();
                txPostReception.setPostDate(new Timestamp(System.currentTimeMillis()));
            }

            model.addAttribute("dataGeneral", txPostReception);

            List<TxPostReceptionDetail> txPostReceptionDetail = pascaOnlineService.selectAllPostDetail(txPostReception.getId());
            model.addAttribute("dataGeneralDetail", txPostReceptionDetail);

            if(txPostReception.getmFileType().getId().equalsIgnoreCase("PERPANJANGAN") || txPostReception.getmFileType().getId().equalsIgnoreCase("PERPANJANGAN_6_BULAN_KADALUARSA")) {
                if(txPostReceptionDetail!=null) {
                    //get list tx monitor
                    List<TxMonitor> listCurrentMonitor = monitorService.findByTxTmGeneral(txPostReceptionDetail.get(0).getTxTmGeneral());
                    MWorkflow wf = txPostReceptionDetail.get(0).getTxTmGeneral().getTxReception().getmWorkflow();

                    List<MWorkflowProcess> listWfProcess= mWorkflowProcessRepository.findMWorkflowProcessesByWorkflowAndStatusFlagOrderByOrders(wf, true);
                    for(MWorkflowProcess mwfp: listWfProcess){
                        for(TxMonitor txM: listCurrentMonitor){
                            if(txM.getmWorkflowProcessActions()!=null) {
                                if(txM.getmWorkflowProcessActions().getAction().getId()!=null) {
                                    if(txM.getmWorkflowProcessActions().getAction().getId().equalsIgnoreCase("PERPANJANGAN")) {
                                        //System.out.println(txM.getmWorkflowProcessActions().getAction().getDocument().getId()+File.separator+txM.getTxTmGeneral().getApplicationNo()+File.separator+txM.getId());
                                        model.addAttribute("paramCetak", txM.getmWorkflowProcessActions().getAction().getDocument().getId()+"/"+txM.getTxTmGeneral().getApplicationNo()+"/"+txM.getId());
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
                txPostRepresentative.setmRepresentative(new MRepresentative());
            }
            if (txPostRepresentative.getmRepresentative().getmCity() == null)
                txPostRepresentative.getmRepresentative().setmCity(new MCity());
            if (txPostRepresentative.getmRepresentative().getmProvince() == null)
                txPostRepresentative.getmRepresentative().setmProvince(new MProvince());
            if (txPostRepresentative.getmRepresentative().getmCountry() == null)
                txPostRepresentative.getmRepresentative().setmCountry(new MCountry());

            model.addAttribute("dataKuasa", txPostRepresentative);
        }
    }

    @RequestMapping(value = REQUEST_MAPPING_PASCA_PERMOHONAN_LIST, method = {RequestMethod.GET})
    public String doShowPagePascaPermohonan(@RequestParam(value = "error", required = false) String error, final Model model, final HttpServletRequest request, final HttpServletResponse response) {
        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("errorMessage", error);
        }

        doInitiatePasca(model, request);
        return PAGE_PASCA_PERMOHONAN_LIST;
    }

    @RequestMapping(value = REQUEST_MAPPING_PASCA_PERMOHONAN_PRATINJAU, method = {RequestMethod.GET})
    public String doShowPagePratinjauPascaOnline(final Model model, final HttpServletRequest request, final HttpServletResponse response) {
        String postId = request.getParameter("no");

        TxPostReception txPostReception = pascaOnlineService.selectOneTxPostReceptionById(postId);
        if (txPostReception == null) {
            return REDIRECT_PASCA_PERMOHONAN_LIST + "?error=Pasca Permohonan tidak ditemukan";
        }

        doInitiatePasca(model, request);
        return PAGE_PASCA_PERMOHONAN_PRATINJAU;
    }

    @RequestMapping(value = REQUEST_MAPPING_PASCA_PERMOHONAN_AJAX_SEARCH_LIST, method = {RequestMethod.POST})
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
            String[] listExactMatchFields = new String[] {"mFileType.id"};
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
                        if (searchBy.equalsIgnoreCase("postDate")) {
                            try {
                                searchCriteria.add(new KeyValue(searchBy, DateUtil.toDate("dd/MM/yyyy", value), true));
                            } catch (ParseException e) {
                            }
                        } else {
                            if (StringUtils.isNotBlank(value)) {
                                if (ArrayUtils.contains(listExactMatchFields, searchBy)) {
                                    searchCriteria.add(new KeyValue(searchBy, value, true));
                                } else {
                                    searchCriteria.add(new KeyValue(searchBy, value, false));
                                }
                            }
                        }
                    }
                }
            }

            //searchCriteria.add(new KeyValue("onlineFlag", Boolean.TRUE, true));
            searchCriteria.add(new KeyValue("statusExclude", StatusEnum.IPT_DRAFT.name(), true));

            String orderBy = request.getParameter("order[0][column]");
            if (orderBy != null) {
                orderBy = orderBy.trim();
                if (orderBy.equalsIgnoreCase("")) {
                    orderBy = null;
                } else {
                    switch (orderBy) {
                        case "1" :
                            orderBy = "eFilingNo";
                            break;
                        case "2" :
                            orderBy = "postDate";
                            break;
                        case "3" :
                            orderBy = "postNo";
                            break;
                        case "4" :
                            orderBy = "mFileType.desc";
                            break;
                        case "5" :
                            orderBy = "txPostReceptionDetail.txTmGeneral.applicationNo";
                            break;
                        case "6" :
                            orderBy = "billingCode";
                            break;
                        case "7" :
                            orderBy = "totalPayment";
                            break;
                        case "8" :
                            orderBy = "paymentDate";
                            break;
                        default :
                            orderBy = "postDate";
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
//            String sFileSeq = "-";
//            String refNoList = "-";
//            ArrayList<String> refNo = null; //new ArrayList<String>();
                String aksiList = "-";
                ArrayList<String> aksi = null; //new ArrayList<String>();
                for (TxPostReception result : searchResult.getList()) {
                    aksi = new ArrayList<String>();
                    for (TxPostReceptionDetail txPostReceptionDetail : result.getTxPostReceptionDetailList()) {
                        String aksiTemp = FieldValidationUtil.isNotNull(txPostReceptionDetail.getTxTmGeneral().getmAction()) ? txPostReceptionDetail.getTxTmGeneral().getmAction().getName()  : "";
                        aksi.add("" + aksiTemp);
                    }
                    Set<String> tempAksi = new LinkedHashSet<String>(aksi);
                    String[] uniqueAksi = tempAksi.toArray(new String[tempAksi.size()]);
                    if (uniqueAksi.length > 0) {
                        aksiList = String.join(",", uniqueAksi);
                    }

                    String refPermohonan = "";
                    try {
                        refPermohonan = result.getTxPostReceptionDetail().getTxTmGeneral().getApplicationNo();
                    } catch (NullPointerException e) {
                    } catch (Exception e) {
                    }

                    String regNo = "-";
                    try {
                        regNo = result.getTxPostReceptionDetail().getTxTmGeneral().getTxRegistration().getNo();
                    } catch (NullPointerException e) {
                    } catch (Exception e) {
                    }

                    String btnCetak = "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin(PATH_PASCA_PERMOHONAN_CETAK) + "?no=" + result.getId() + "\" target=\"_blank\">Tanda Terima</a>";
                    String btnMonitor = "<a class=\"btn btn-success btn-xs\" href=\"" + getPathURLAfterLogin(PATH_MONITOR) + "?no=" + refPermohonan + "\" target=\"_blank\">Monitor</a> ";

                    no++;
                    data.add(new String[]{
                            "" + no,
                            "<a target=\"_blank\" href=\"" + getPathURLAfterLogin(PATH_PASCA_PERMOHONAN_PRATINJAU + "?no=" + result.getId()) + "\">" + result.geteFilingNo() + "</a>",
                            result.getPostDateTemp(),
                            result.getPostNo(),
//                            sFileSeq,
                            result.getmFileType() == null ? "" : result.getmFileType().getDesc(),
                            "<a  target=\"_blank\" href=\"" + getPathURLAfterLogin(PATH_PRANTINJAU_PERMOHONAN + "?no=" + refPermohonan) + "\">" + refPermohonan + "</a>",
                            regNo,
                            result.getBillingCode(),
//                            result.getPaymentDateTemp(),
                            "Rp. " + result.getTotalPaymentTemp(),
                            result.getTxPostReceptionDetail().getmStatus().getName(),
                            aksiList,
                            "<div class=\"btn-actions\">" + ( result.getNote()!= null && result.getNote().equalsIgnoreCase("MIGRASI USERDOC IPAS")  ? "" : btnCetak) +  "<br/>" + btnMonitor
                    });
                }
            }
            dataTablesSearchResult.setData(data);
            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_PASCA_PERMOHONAN_AJAX_SEARCH_LIST_DOKUMEN, method = {RequestMethod.GET})
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
                 /*   String folder = "";
                    String fileName = "";
                    response.setContentType("application/pdf");
                    response.setHeader("Content-disposition", "inline; filename=DokumenLampiran-" + result.getFileNameTemp());
                    folder =  uploadFileDocPascaPath + DateUtil.formatDate(result.getCreatedDate(), "yyyy/MM/dd/");
                    fileName = result.getFileNameTemp();

                try (OutputStream output = response.getOutputStream()) {
                    Path path = Paths.get(folder + fileName);
                    Files.copy(path, output);
                    output.flush();
                } catch(IOException e) {
                }*/

                    data.add(new String[]{
                            result.getId(),
                            result.getmDocType().getId(),
                            " " + no,
                            DateUtil.formatDate(result.getUploadDate(), "dd/MM/yyyy"),
                            result.getmDocType().getName(),
                            result.getFileName(),
//                            FieldValidationUtil.isNotNull(result.getDescription()) ? result.getDescription() : "",
                            result.getDescription(),
                            result.getFileSize(),
                            "",
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

    @RequestMapping(path = REQUEST_MAPPING_PASCA_UPDATE_DATA_DOWNLOAD_DOKUMEN, method = {RequestMethod.POST})
    public void doUpdateDataDownloadFormDokumen(@RequestParam("txPostDocId") String txPostDocId, @RequestParam("postId") String postId, @RequestParam("docList") String docList,
                                                final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            try {
                TxPostReception txPostReception = pascaOnlineService.selectOneTxPostReception("id", postId);
                TxPostDoc txPostDocs = txPostDokumenService.selectOnePostDocById("id", txPostDocId);
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(docList);

                for (JsonNode node : rootNode) {
                    String docId = node.get("docId").toString().replaceAll("\"", "");
                    String docDate = node.get("docDate").toString().replaceAll("\"", "");
                    String docFileName = node.get("docFileName").toString().replaceAll("\"", "");
                    String docDesc = node.get("docDesc").toString().replaceAll("\"", "");
                    String docFileSize = node.get("docFileSize").toString().replaceAll("\"", "");
                    String[] docFile = request.getParameter("file-" + docId).split(",");

                    TxPostDoc txPostDoc = new TxPostDoc();
                    txPostDoc.setId(txPostDocs == null ? UUID.randomUUID().toString() :txPostDocs.getId());
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
                    txPostDokumenService.saveOrUpdateDokumen(txPostDoc);
                }

                writeJsonResponse(response, 200);
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

    @RequestMapping(path = REQUEST_MAPPING_PASCA_PERMOHONAN_CETAK_REBUILD, method = {RequestMethod.GET})
    public void doCetakPascaRebuilt(final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) throws IOException {
        String postId = request.getParameter("no");
        TxPostReception txPostReception = pascaOnlineService.selectOneTxPostReception("id", postId);

        //CETAK MEREK PASCA SAVE TO PATH
        try {
            List<CetakPasca> listData = new ArrayList<CetakPasca>();
            CetakPasca data = null;

            data = new CetakPasca(1, "Data Permohonan", "Application");
            data.setPostNo(txPostReception.getPostNo());
            data.seteFilingNo(txPostReception.geteFilingNo());
            // ubah post_reception_id menjadi sama dengan eFillingNo
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
                    TxTmOwner txTmOwner = ownerService.selectOne("txTmGeneral.id", txTmGeneral.getId());
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

            File file = new File(classLoader.getResource("report/CetakPasca.jasper").getFile());
            InputStream jasperStream1 = new FileInputStream(file);
            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream1);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JRBeanCollectionDataSource(listData));

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

                result = msOut.toByteArray();
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

            InputStream is = new ByteArrayInputStream(result);
            this.signPdf(is, outputStream);
            outputStream.close();



        } catch (Exception ex) {
            logger.error(ex);
        }

//            writeJsonResponse(response, "SUCCESS");
        doCetakPasca(request,response);
    }


    @RequestMapping(value = REQUEST_MAPPING_PASCA_PERMOHONAN_CETAK, method = {RequestMethod.GET})
    @ResponseBody
    public String doCetakPasca(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        String postId = request.getParameter("no");
        TxPostReception txPostReception = pascaOnlineService.selectOneTxPostReception("id", postId);

        response.setContentType("application/pdf");
        response.setHeader("Content-disposition", "inline; filename=CetakPasca-" + txPostReception.geteFilingNo() + ".pdf");

        String folder =  downloadFileDocPascaCetakMerekPath + DateUtil.formatDate(txPostReception.getCreatedDate(), "yyyy/MM/dd/");
        String fileName = "CetakPasca-" + txPostReception.geteFilingNo() + ".pdf";

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

    @RequestMapping(value = REQUEST_MAPPING_HAPUS_PERMOHONAN_PASCA_POST, method = {RequestMethod.POST})
    @ResponseBody
    public void doHapusPermohonanPost(Model model, @RequestParam("listId") String listId,
                                      final HttpServletRequest request, final HttpServletResponse response) {
        //System.out.println("doHapusPermohonanPascaPost "+ listId.join(","));
        if (pascaOnlineService.deletePermohonanPasca(listId)){
            writeJsonResponse(response, 200);
        }else{
            writeJsonResponse(response, 201);
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_UBAH_DOKUMEN_PASCA_PERMOHONAN)
    public String doShowEditDokumenPascaPermohonan(Model model, @RequestParam(value = "no", required = true) String no) {
        // DOC SECTION //
        List<MDocType> mDocTypeList = docTypeService.findByStatusFlagTrue();

        model.addAttribute("noGeneral", no);
        model.addAttribute("listDocType", mDocTypeList);
        model.addAttribute("docUploadDate", DateUtil.formatDate(new Date(), "dd/MM/yyyy"));

        return PATH_UBAH_PASCA_PERMOHONAN_PAGE_DOKUMEN_LAMPIRAN;
    }

    @RequestMapping(value = REQUEST_MAPPING_AJAX_DELETE_DOKUMEN_PASCA_PERMOHONAN, method = RequestMethod.POST)
    public void doDeleteDocPascaPermohonan(Model model, @RequestParam("idDoc") String id, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        txPostDokumenService.deleteDokumen(id);
        writeJsonResponse(response, 200);
    }

    @RequestMapping(value = REQUEST_MAPPING_VIEW_DOKUMEN_LAMPIRAN_PASCA_PERMOHONAN, method = {RequestMethod.GET})
    @ResponseBody
    public String doViewDokumenPascaPermohonan(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

        String docId = request.getParameter("id");
        TxPostDoc txPostDoc = txPostDocRepository.findOne(docId);

        String folder = "";
        String fileName = "";

        if (txPostDoc.getFileName().toUpperCase().endsWith(".JPG") || txPostDoc.getFileName().toUpperCase().endsWith(".JPEG")) {
            response.setContentType("image/jpeg");
            response.setHeader("Content-disposition", "inline; filename=DokumenLampiran-" + txPostDoc.getFileNameTemp());
            folder = uploadFileDocPascaPath + DateUtil.formatDate(txPostDoc.getTxPostReception().getCreatedDate(), "yyyy/MM/dd/");
            fileName = txPostDoc.getFileNameTemp();
        } else {
            response.setContentType("application/pdf");
            response.setHeader("Content-disposition", "inline; filename=DokumenLampiran-" + txPostDoc.getFileNameTemp());
            folder = uploadFileDocPascaPath + DateUtil.formatDate(txPostDoc.getTxPostReception().getCreatedDate(), "yyyy/MM/dd/");
            fileName = txPostDoc.getFileNameTemp();
        }

        try (OutputStream output = response.getOutputStream()) {
            Path path = Paths.get(folder + fileName);
            Files.copy(path, output);
            output.flush();
        } catch (IOException e) {
        }

        return "";
    }
}