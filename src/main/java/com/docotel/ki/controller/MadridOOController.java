package com.docotel.ki.controller;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.*;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.service.master.FileService;
import com.docotel.ki.signature.PDFSignatureFacade;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.util.FieldValidationUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.docotel.ki.enumeration.StatusEnum;
import com.docotel.ki.model.master.*;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.repository.master.MWorkflowProcessRepository;
import com.docotel.ki.service.transaction.DokumenLampiranService;
import com.docotel.ki.service.transaction.MonitorService;
import com.docotel.ki.service.transaction.OwnerService;
import com.docotel.ki.service.transaction.PascaOnlineService;
import com.docotel.ki.service.transaction.TxPostDokumenService;
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
public class MadridOOController extends BaseController {
    private static final String DIRECTORY_PAGE = "permohonan-madrid-oo/";
    private static final String PAGE_MADRID_OO_PERMOHONAN_LIST = DIRECTORY_PAGE + "list-permohonan-madrid-oo";
    private static final String PAGE_MADRID_OO_PERMOHONAN_PRATINJAU = DIRECTORY_PAGE + "pratinjau-madrid-oo-permohonan";
    private static final String PATH_MADRID_OO_PERMOHONAN_AJAX_SEARCH_LIST = "/cari-madrid-oo-permohonan";
    private static final String PATH_MADRID_OO_PERMOHONAN_AJAX_SEARCH_LIST_DOKUMEN = "/cari-dokumen-madrid-oo-permohonan";
    private static final String PATH_MADRID_OO_PERMOHONAN_LIST = "/list-madrid-oo-permohonan";
    public static final String REDIRECT_MADRID_OO_PERMOHONAN_LIST = "redirect:" + PATH_AFTER_LOGIN + PATH_MADRID_OO_PERMOHONAN_LIST;
    private static final String PATH_MADRID_OO_PERMOHONAN_PRATINJAU = "/pratinjau-madrid-oo-permohonan";
    private static final String PATH_MADRID_OO_PERMOHONAN_CETAK = "/cetak-madrid-oo-permohonan";
    private static final String PATH_MADRID_OO_UPDATE_DATA_DOWNLOAD_DOKUMEN = "/update-data-download-dokumen-madrid-oo";
    private static final String REQUEST_MAPPING_MADRID_OO_PERMOHONAN_AJAX_SEARCH_LIST = PATH_MADRID_OO_PERMOHONAN_AJAX_SEARCH_LIST + "*";
    private static final String REQUEST_MAPPING_MADRID_OO_PERMOHONAN_AJAX_SEARCH_LIST_DOKUMEN = PATH_MADRID_OO_PERMOHONAN_AJAX_SEARCH_LIST_DOKUMEN + "*";
    private static final String REQUEST_MAPPING_MADRID_OO_PERMOHONAN_LIST = PATH_MADRID_OO_PERMOHONAN_LIST + "*";
    private static final String REQUEST_MAPPING_MADRID_OO_PERMOHONAN_PRATINJAU = PATH_MADRID_OO_PERMOHONAN_PRATINJAU + "*";
    private static final String REQUEST_MAPPING_MADRID_OO_UPDATE_DATA_DOWNLOAD_DOKUMEN = PATH_MADRID_OO_UPDATE_DATA_DOWNLOAD_DOKUMEN + "*";
    private static final String REQUEST_MAPPING_MADRID_OO_PERMOHONAN_CETAK = PATH_MADRID_OO_PERMOHONAN_CETAK + "*";
    @Autowired
    private FileService fileService;
    @Autowired
    private OwnerService ownerService;
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
    @Value("${upload.file.doc.pasca.path:}")
    private String uploadFileDocPascaPath;
    @Value(("${certificate.file}"))
    private String CERTIFICATE_FILE;
    private FileInputStream fileInputStreamReader;
    @Value("${download.output.pasca.cetakmerek.file.path:}")
    private String downloadFileDocPascaCetakMerekPath;

    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "permohonanMerek");
        model.addAttribute("subMenu", "pascaPermohonan");
    }

    private void doInitiatePasca(final Model model, final HttpServletRequest request) {
        List<MFileSequence> fileSequenceList = fileService.findMFileSequenceByStatusFlagTrue();
        model.addAttribute("fileSequenceList", fileSequenceList);

        List<MFileType> fileTypeList = fileService.findByMenu("MADRID_OO"); //fileService.findMFileTypeByStatusFlagTrue();
        model.addAttribute("fileTypeList", fileTypeList);

        if (request.getRequestURI().contains(PATH_MADRID_OO_PERMOHONAN_PRATINJAU)) {
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

    @RequestMapping(value = REQUEST_MAPPING_MADRID_OO_PERMOHONAN_LIST, method = {RequestMethod.GET})
    public String doShowPagePascaPermohonan(@RequestParam(value = "error", required = false) String error, final Model model, final HttpServletRequest request, final HttpServletResponse response) {
        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("errorMessage", error);
        }

        doInitiatePasca(model, request);
        return PAGE_MADRID_OO_PERMOHONAN_LIST;
    }

    @RequestMapping(value = REQUEST_MAPPING_MADRID_OO_PERMOHONAN_PRATINJAU, method = {RequestMethod.GET})
    public String doShowPagePratinjauPascaOnline(final Model model, final HttpServletRequest request, final HttpServletResponse response) {
        String postId = request.getParameter("no");

        TxPostReception txPostReception = pascaOnlineService.selectOneTxPostReceptionById(postId);
        if (txPostReception == null) {
            return REDIRECT_MADRID_OO_PERMOHONAN_LIST + "?error=Pasca Permohonan tidak ditemukan";
        }

        doInitiatePasca(model, request);
        return PAGE_MADRID_OO_PERMOHONAN_PRATINJAU;
    }

    @RequestMapping(value = REQUEST_MAPPING_MADRID_OO_PERMOHONAN_AJAX_SEARCH_LIST, method = {RequestMethod.POST})
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

            //searchCriteria.add(new KeyValue("onlineFlag", Boolean.TRUE, true));
            searchCriteria.add(new KeyValue("statusExclude", StatusEnum.IPT_DRAFT.name(), true));
            searchCriteria.add(new KeyValue("txReception.mFileTypeDetail.desc", "Madrid OO", true));

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
                            orderBy = "postNo";
                            break;
                        case "3" :
                            orderBy = "postDate";
                            break;
                        case "4" :
                            orderBy = "mFileSequence.desc";
                            break;
                        case "5" :
                            orderBy = "mFileType.desc";
                            break;
                        case "6" :
                            orderBy = "billingCode";
                            break;
                        case "7" :
                            orderBy = "paymentDate";
                            break;
                        case "8" :
                            orderBy = "totalPayment";
                            break;
                        default :
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
                String sFileSeq = "-";
                String refNoList = "-";
                String aksiList = "-";
                ArrayList<String> refNo = null; //new ArrayList<String>();
                ArrayList<String> aksi = null; //new ArrayList<String>();

                for (TxPostReception result : searchResult.getList()) {
                    if (result.getmFileSequence() != null) {
                        sFileSeq = result.getmFileSequence().getDesc();
                    } else {
                        sFileSeq = "-";
                    }
                    refNo = new ArrayList<String>();
                    aksi = new ArrayList<String>();
                    for (TxPostReceptionDetail txPostReceptionDetail : result.getTxPostReceptionDetailList()) {
                        String aksiTemp = FieldValidationUtil.isNotNull(txPostReceptionDetail.getTxTmGeneral().getmAction()) ? txPostReceptionDetail.getTxTmGeneral().getmAction().getName()  : "";
                        refNo.add("" + txPostReceptionDetail.getTxTmGeneral().getApplicationNo());
                        aksi.add("" + aksiTemp);
                    }
                    Set<String> temp = new LinkedHashSet<String>(refNo);
                    Set<String> tempAksi = new LinkedHashSet<String>(aksi);
                    String[] unique = temp.toArray(new String[temp.size()]);
                    if (unique.length > 0) {
                        refNoList = String.join(",", unique);
                    }
                    String[] uniqueAksi = tempAksi.toArray(new String[tempAksi.size()]);
                    if (uniqueAksi.length > 0) {
                        aksiList = String.join(",", uniqueAksi);
                    }

                    no++;
                    data.add(new String[]{
                            "" + no,
                            "<a target=\"_blank\" href=\"" + getPathURLAfterLogin(PATH_MADRID_OO_PERMOHONAN_PRATINJAU + "?no=" + result.getId()) + "\">" + result.geteFilingNo() + "</a>",
                            result.getPostDateTemp(),
                            result.getPostNo(),
//                            sFileSeq,
                            result.getmFileType() == null ? "" : result.getmFileType().getDesc(),
                            refNoList,
                            result.getBillingCode(),
                            result.getPaymentDateTemp(),
                            result.getTotalPaymentTemp(),
                            aksiList,
                            "<div class=\"btn-actions\">" +
                                    "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin(PATH_MADRID_OO_PERMOHONAN_CETAK) + "?no=" + result.getId() + "\" target=\"_blank\">Tanda Terima</a>"
                    });
                }
            }
            dataTablesSearchResult.setData(data);
            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_MADRID_OO_PERMOHONAN_AJAX_SEARCH_LIST_DOKUMEN, method = {RequestMethod.GET})
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
                    /*File file = new File(uploadFileDocPascaPath + pathFolder + result.getFileNameTemp());
                    String image = "";

                    if (file.exists() && !file.isDirectory()) {
                        fileInputStreamReader = new FileInputStream(file);
                        byte[] bytes = new byte[(int) file.length()];
                        fileInputStreamReader.read(bytes);
                        image = "data:application/pdf;base64" + "," + Base64.getEncoder().encodeToString(bytes);
                    }*/

                    data.add(new String[]{
                    		result.getId(),
                            result.getmDocType().getId(),
                            " " + no,
                            DateUtil.formatDate(result.getUploadDate(), "dd/MM/yyyy"),
                            result.getmDocType().getName(),
                            result.getFileName(),
                            FieldValidationUtil.isNotNull(result.getDescription()) ? result.getDescription() : "",
                            result.getFileSize(),
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
    
    @RequestMapping(path = REQUEST_MAPPING_MADRID_OO_UPDATE_DATA_DOWNLOAD_DOKUMEN, method = {RequestMethod.POST})
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
                    txPostDoc.setId(txPostDocs.getId());
                    txPostDoc.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                    txPostDoc.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                    txPostDoc.setTxPostReception(txPostReception);
                    ;
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

    @RequestMapping(value = REQUEST_MAPPING_MADRID_OO_PERMOHONAN_CETAK, method = {RequestMethod.GET})
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
}