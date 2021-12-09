package com.docotel.ki.controller;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.enumeration.StatusEnum;
import com.docotel.ki.model.master.*;
import com.docotel.ki.model.transaction.TxMonitor;
import com.docotel.ki.model.transaction.TxPostDoc;
import com.docotel.ki.model.transaction.TxPostReception;
import com.docotel.ki.model.transaction.TxPostReceptionDetail;
import com.docotel.ki.model.transaction.TxTmBrand;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.pojo.*;
import com.docotel.ki.repository.NativeQueryRepository;
import com.docotel.ki.repository.custom.transaction.TxPostReceptionCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxPostReceptionDetailCustomRepository;
import com.docotel.ki.repository.master.MActionRepository;
import com.docotel.ki.repository.master.MStatusRepository;
import com.docotel.ki.repository.master.MWorkflowProcessActionRepository;
import com.docotel.ki.repository.master.MWorkflowProcessRepository;
import com.docotel.ki.repository.transaction.TxMonitorRepository;
import com.docotel.ki.repository.transaction.TxTmBrandRepository;
import com.docotel.ki.repository.transaction.TxTmGeneralRepository;
import com.docotel.ki.service.EmailService;
import com.docotel.ki.service.master.StatusService;
import com.docotel.ki.service.master.UserRoleService;
import com.docotel.ki.service.transaction.DokumenLampiranService;
import com.docotel.ki.service.transaction.IPASService;
import com.docotel.ki.service.transaction.MonitorService;
import com.docotel.ki.service.transaction.PascaOnlineService;
import com.docotel.ki.service.transaction.TrademarkService;
import com.docotel.ki.util.DateUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Array;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class TxMonitorController extends BaseController {

    @Autowired
    private StatusService statusService;
    @Autowired
    private MonitorService monitorService;
    @Autowired
    private TxTmGeneralRepository txTmGeneralRepository;
    @Autowired
    private MWorkflowProcessRepository mWorkflowProcessRepository;
    @Autowired
    private MWorkflowProcessActionRepository mWorkflowProcessActionRepository;
    @Autowired
    private MStatusRepository mStatusRepository;
    @Autowired
    private MActionRepository mActionRepository;
    @Autowired
    private TxPostReceptionDetailCustomRepository txPostReceptionDetailCustomRepository;
    @Autowired
    private TxPostReceptionCustomRepository txPostReceptionCustomRepository;
    @Autowired
    private PascaOnlineService pascaOnlineService;
    @Autowired
    TrademarkService trademarkService;
    // olly@docotel.com -- start
    @Autowired
    UserRoleService userRoleService;
    // olly@docotel.com -- end
    @Autowired
    private IPASService ipasService ;
    @Autowired
    private EmailService emailService;
    @Autowired
    private DokumenLampiranService doclampiranService;
    
    @Autowired
    private TxMonitorRepository txMonitorRepository;
    @Autowired
    private TxTmBrandRepository txTmBrandRepository;

    @Value("${upload.ipas.file.path}")
    private String uploadIPASDoc;

    @Value("${download.output.letters.file.path:}")
    private String downloadLettersFilePath;

    @Autowired
    NativeQueryRepository nativeQueryRepository;

    private static final String DIRECTORY_PAGE = "monitor/";

    private static final String PAGE_LIST_MONITOR = DIRECTORY_PAGE + "list-monitor";
    private static final String PAGE_ADD_MONITOR = DIRECTORY_PAGE + "tambah-monitor";
    private static final String PAGE_ADD_MONITOR_PERUBAHAN = DIRECTORY_PAGE + "tambah-monitor-perubahan";

    private static final String PAGE_LIST_MONITOR_OPOSISI = DIRECTORY_PAGE + "oposisi-monitor";
    private static final String PATH_LIST_MONITOR_OPOSISI = "/monitor-oposisi" + "*";

    public static final String PATH_LIST_MONITOR = "/list-monitor";
    public static final String PATH_ADD_MONITOR = "/tambah-monitor";
    public static final String PATH_ADD_MONITOR_PERUBAHAN = "/tambah-monitor-perubahan";
    public static final String PATH_REMOVE_MONITOR = "/hapus-monitor";
    public static final String PATH_UNDUH_MONITOR = "/download-monitor";
    public static final String PATH_MADRID_APPROVAL = "/madrid-approval";
    private static final String PATH_DWN_FILE = "/dwn/{isIPAS}/{file}";
    private static final String PATH_DOWNLOAD_OFFIDOC = "/download-offidoc";

    private static final String PATH_AJAX_LIST = "/cari-monitor";
    private static final String PATH_AJAX_MONITOR_OPOSISI = "/cari-monitor-oposisi";
    private static final String PATH_AJAX_APPROVE_MONITOR = "/approve-monitor";
    private static final String PATH_AJAX_STATUS_PASCA = "/update-status-pasca";

    public static final String PATH_PROSES_PERMOHONAN = "/proses-permohonan";
    private static final String PAGE_PROSES_PERMOHONAN = DIRECTORY_PAGE + "proses-permohonan";
    private static final String REQUEST_MAPPING_PROSES_PERMOHONAN = PATH_PROSES_PERMOHONAN + "*";


    private static final String REQUEST_MAPPING_AJAX_LIST = PATH_AJAX_LIST + "*";
    private static final String REQUEST_MAPPING_MADRID_APPROVAL = PATH_MADRID_APPROVAL + "*";

    private static final String REQUEST_MAPPING_AJAX_MONITOR_OPOSISI = PATH_AJAX_MONITOR_OPOSISI + "*";
    private static final String REQUEST_MAPPING_AJAX_APPROVE_MONITOR = PATH_AJAX_APPROVE_MONITOR + "*";
    private static final String REQUEST_MAPPING_AJAX_UPDATE_STATUS_PASCA = PATH_AJAX_STATUS_PASCA + "*";


    public static final String REDIRECT_LIST_MONITOR = "redirect:" + PATH_AFTER_LOGIN + PATH_LIST_MONITOR;
    public static final String REDIRECT_PROCESS_LIST = "redirect:" + PATH_AFTER_LOGIN + PATH_PROSES_PERMOHONAN;
    
    private static final String PATH_KIRIM_EMAIL = "/kirim-email";
    private static final String REQUEST_MAPPING_KIRIM_EMAIL = PATH_KIRIM_EMAIL + "*";

    private SimpleDateFormat formater = new SimpleDateFormat("dd-MM-yyyy");
    @Value("${upload.file.letters.uploaddoc.path}")
    private String uploadFileLettersUploaddocPath;
    
    @Value("${upload.file.logoemail.image:}")
    private String logoEmailImage;
    @Value("${upload.file.web.image:}")
    private String pathImage;

    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "permohonanMerek");
        model.addAttribute("subMenu", "permohonan");
    }

    @GetMapping(PATH_LIST_MONITOR)
    public String getListMonitor(@RequestParam(value = "no", required = true) String no, Model model) {
        TxTmGeneral ttg = txTmGeneralRepository.findTxTmGeneralByApplicationNo(no);
        model.addAttribute("tmGeneral", ttg);

        List<TxMonitor> monitors = monitorService.findByTxTmGeneral(ttg);


        model.addAttribute("monitors", monitors);

        MUser op = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String operator = op.getUsername();

        model.addAttribute("operator", operator);
        if ((monitors.size() > 1 && ttg != null) || (ttg != null && ttg.getmStatus()!=null)) {
            model.addAttribute("currentStatus", ttg.getmStatus().getName());
        } else {
            model.addAttribute("currentStatus", "-");
        }
        return PAGE_LIST_MONITOR;
    }

    @GetMapping(PATH_LIST_MONITOR_OPOSISI)
    public String getListMonitorOposisi(@RequestParam(value = "no", required = true) String no, Model model) {
        // cari list TX_RECEPTION_DETAIL di mana APPLICATION_ID = no dan status Oposisi

        TxTmGeneral ttg = txTmGeneralRepository.findTxTmGeneralByApplicationNo(no);
        model.addAttribute("tmGeneral", ttg);

        List<TxMonitor> monitors = monitorService.findByTxTmGeneral(ttg);
        model.addAttribute("monitors", monitors);

        MUser op = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String operator = op.getUsername();

        model.addAttribute("operator", operator);
        if (monitors.size() > 0) {
            model.addAttribute("currentStatus", ttg.getmStatus().getName());
        } else {
            model.addAttribute("currentStatus", "-");
        }


        return PAGE_LIST_MONITOR_OPOSISI;
    }

    @GetMapping("/kunci")
    public String doLockApplication(@RequestParam(value = "no", required = true) String no) {
        // check apakah aplikasi sudah terkunci, jika sudah, tendang keluar
        TxTmGeneral ttg = txTmGeneralRepository.findTxTmGeneralByApplicationNo(no);
        MUser op = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String operator_id = op.getId();

        if (!ttg.isLocked()) {
            NativeQueryModel queryModel = new NativeQueryModel();
            queryModel.setTable_name("TX_TM_GENERAL");

            // yg mau di Update
            ArrayList<KeyValue> updateQ = new ArrayList<>();
            KeyValue updateq1 = new KeyValue();
            KeyValue updateq2 = new KeyValue();
            updateq1.setKey("IS_LOCKED");
            updateq1.setValue("1");
            updateq2.setKey("LOCKED_BY");
            updateq2.setValue(operator_id);
            updateQ.add(updateq1);
            updateQ.add(updateq2);
            queryModel.setUpdateQ(updateQ);

            //syarat kondisi
            ArrayList<KeyValueSelect> searchBy = new ArrayList<>();
            searchBy.add(new KeyValueSelect("APPLICATION_NO", no, "=", true, null));
            queryModel.setSearchBy(searchBy);

            int i = nativeQueryRepository.updateNavite(queryModel);

        }

        return "redirect:" + BaseController.PATH_AFTER_LOGIN + PATH_LIST_MONITOR + "?no=" + no;
    }

    @GetMapping("/buka-kunci")
    public String doUnlockApplication(@RequestParam(value = "no", required = true) String no) {

        // check apakah orang yg mengunci adalah Principal (yg lagi login) jika BUKAN, tendang keluar
        MUser op = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String operator = op.getUsername();

        TxTmGeneral ttg = txTmGeneralRepository.findTxTmGeneralByApplicationNo(no);
        String locker = ttg.getLockedBy().getUsername();
        if ((locker.equalsIgnoreCase(operator)) || (operator.equalsIgnoreCase("super"))) {
            NativeQueryModel queryModel = new NativeQueryModel();
            queryModel.setTable_name("TX_TM_GENERAL");

            // yg mau di Update
            ArrayList<KeyValue> updateQ = new ArrayList<>();
            KeyValue updateq1 = new KeyValue();
            KeyValue updateq2 = new KeyValue();
            updateq1.setKey("IS_LOCKED");
            updateq1.setValue("0");
            updateq2.setKey("LOCKED_BY");
            updateq2.setValue("");
            updateQ.add(updateq1);
            updateQ.add(updateq2);
            queryModel.setUpdateQ(updateQ);

            //syarat kondisi
            ArrayList<KeyValueSelect> searchBy = new ArrayList<>();
            searchBy.add(new KeyValueSelect("APPLICATION_NO", no, "=", true, null));
            queryModel.setSearchBy(searchBy);

            int i = nativeQueryRepository.updateNavite(queryModel);

        }

        return "redirect:" + BaseController.PATH_AFTER_LOGIN + PATH_LIST_MONITOR + "?no=" + no;
    }


    @PostMapping(value = REQUEST_MAPPING_AJAX_LIST)
    public void doGetListDataTables(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ParseException {
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
                    if (searchBy != null && searchBy.equalsIgnoreCase("mWorkflowProcess.status.name")) {
                        if (org.apache.commons.lang3.StringUtils.isNotBlank(value)) {
                            searchCriteria.add(new KeyValue(searchBy, value, false));
                        }
                    } else {
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

            GenericSearchWrapper<TxMonitor> searchResult = statusService.searchGeneralMonitor(searchCriteria, orderBy, orderType, offset, limit);


            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());
                MUser user = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

                int no = offset;
                TxPostReceptionDetail txPostReceptionDetail = null;


                for (TxMonitor result : searchResult.getList()) {
                    // olly@docotel.com -- start
                    File file = null;
                    boolean isApproved = false;
                    try {
                        isApproved = result.isApproved();
                    } catch (NullPointerException e) {
                    } catch (Exception e) {
                    }

                    String docid = result.getmWorkflowProcessActions() != null ? result.getmWorkflowProcessActions().getAction().getType().equalsIgnoreCase("Download") ? result.getmWorkflowProcessActions().getAction().getDocument().getId() : "" : "";
                    String tombol1 = "";
                    String tombol2 = "";
                    String tombol3 ="";

                    MUserRole mUserRole = userRoleService.findUserAdministrator(user.getId());
                    if ((user.getId().equals(result.getCreatedBy().getId())) || (mUserRole != null)) {
                        tombol3 = result.getmWorkflowProcessActions() != null ? result.getmWorkflowProcessActions().getAction().getType().equalsIgnoreCase("Download") ? "<a class=\"btn btn-xs btn-success\" href=\"" + getPathURLAfterLogin("/download-dokumen?docid=" + result.getmWorkflowProcessActions().getAction().getDocument().getId() + "&no=" + result.getTxTmGeneral().getApplicationNo() + "&monitorId=" + result.getId()) + "\">Buat Surat</a>" : "" : "";
                        tombol1 = result.getmWorkflowProcess() != null ? result.getmWorkflowProcess().getStatus().isStaticFlag() == false ? "<a class=\"btn btn-xs btn-danger\" onclick=\"hapusMonitor('" + result.getId() + "','" + result.getTxTmGeneral().getApplicationNo() + "','" + docid + "')\">Hapus</a>" : "" : "";
                        tombol2 = result.getmWorkflowProcessActions() != null ? result.getmWorkflowProcessActions().getAction().getType().equalsIgnoreCase("Upload") ? "<a class=\"btn btn-xs btn-primary\" href=\"" + getPathURLAfterLogin("/download-monitor?id=" + result.getId() + "&no=" + result.getTxTmGeneral().getApplicationNo()) + "\">Download</a>" : "" : "";
                    }
                    // olly@docotel.com -- end
                    if (txPostReceptionDetail == null) {
                        txPostReceptionDetail = txPostReceptionDetailCustomRepository.selectOneByAppIdBeforeLastGetCetakListMonitor(result.getTxTmGeneral().getId());
                    }
                    no++;
                    //String docid = result.getmWorkflowProcessActions()!=null ? result.getmWorkflowProcessActions().getAction().getType().equalsIgnoreCase("Download")? result.getmWorkflowProcessActions().getAction().getDocument().getId() : "" : "";
                    //String tombol1 = result.getmWorkflowProcess()!=null ? result.getmWorkflowProcess().getStatus().isStaticFlag()==false ? "<a class=\"btn btn-xs btn-danger\" onclick=\"hapusMonitor('"+ result.getId() +"','"+ result.getTxTmGeneral().getApplicationNo() +"','"+ docid +"')\">Hapus</a>" : "" : "";
//                    String tombol3 = result.getmWorkflowProcessActions() != null ? result.getmWorkflowProcessActions().getAction().getType().equalsIgnoreCase("Download") ? "<a class=\"btn btn-xs btn-success\" href=\"" + getPathURLAfterLogin("/download-dokumen?docid=" + result.getmWorkflowProcessActions().getAction().getDocument().getId() + "&no=" + result.getTxTmGeneral().getApplicationNo() + "&monitorId=" + result.getId()) + "\">Buat Surat</a>" : "" : "";
                    String tombol5 = result.getmWorkflowProcessActions() != null ? result.getmWorkflowProcessActions().getAction().getType().equalsIgnoreCase("Download") ? "<a class=\"btn btn-xs btn-warning\" href=\"layanan/download-surat?no=" +result.getId()+"\" target=\"_blank\">Lihat Surat</a>" : "" : "";

                    String tombol4 = "";
                    if (result.getmWorkflowProcess() != null) {
                        if (result.getmWorkflowProcess().getStatus().getId().equalsIgnoreCase("IPT_PENGAJUAN_PERMOHONAN") && result.getCc_notes() == null) {
                            tombol4 = "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin("/cetak-merek?no=" + result.getTxTmGeneral().getApplicationNo()) + "\" target=\"_blank\">Tanda Terima</a>";
                        }
                        if (txPostReceptionDetail != null) {
                            if (result.getmWorkflowProcess().getStatus().getId().equalsIgnoreCase("TM_PASCA_PERMOHONAN")) {
                                tombol4 = txPostReceptionDetail.getId() != null && txPostReceptionDetail.getTxPostReception().getId() != null ?
                                        "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin("/cetak-pasca-permohonan?no=" + result.getTxPostReception().getId()) + "\" target=\"_blank\">Tanda Terima</a>"
                                        : tombol4;
                            }
                        }
                        if (result.getmWorkflowProcess().getStatus().getId().equalsIgnoreCase("IPT_PENGAJUAN_MADRID") && result.getCc_notes() == null) {
                            tombol4 = "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin("/cetak-madrid?no=" + result.getTxTmGeneral().getApplicationNo()) + "\" target=\"_blank\">Tanda Terima</a>";
                        }
                    }
                    String note = "";
                    String ccnote1 = "";

                    ccnote1 = result.getCc_notes() == null ? "" : result.getCc_notes();
                    String stylecatatan = "";
                    stylecatatan = ccnote1.equalsIgnoreCase("TELAH DIPERIKSA") ? "style=\"color:green\"" : "";
                    note = result.getNotes() == null ? "" : result.getNotes();
                    String cc_note = "<br/>______<br/>Catatan Tembusan:<br/><span " + stylecatatan + " >" + ccnote1 + "</span>";

                    String tombol6 = "";
                    String dwnUrl = "";
                    //find edoc
                    if ( result.getNotes() != null &&  result.getNotes().length()>4 && result.getNotes().substring(0, 4).equalsIgnoreCase("edoc")
                            && result.getCc_notes() != null) {
                        String applicationNo = result.getTxTmGeneral().getId();
                        String pathquery = ipasService.findDocIPASS(applicationNo, result.getCc_notes());
                        String fullPath = this.uploadIPASDoc + "/" + pathquery;
//                        System.out.println("++++++++++++++PATH nya: "+  pathquery );

                        file = new File (fullPath);
                        if (file.exists() && !file.isDirectory()) {
                            dwnUrl = "/dwn/1/"  + Base64.getEncoder().encodeToString(pathquery.getBytes());
//                            System.out.println("cekdata: "+  pathquery );
                            tombol6 = "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin(dwnUrl) + "\" target=\"_blank\">Tanda Terima</a>";
                        }
                        else{
//                            System.out.println("<<<<<<<  Maaf File untuk "+result.getCc_notes()+ " Tidak Ditemukan di "+ pathquery );
                        }
                    }
//                    //find userdoc
                    if ( result.getNotes() != null && result.getNotes().length()>7 && result.getNotes().substring(0, 7).equalsIgnoreCase("Userdoc") ) {
                            String applicationNo = result.getTxTmGeneral().getId();
                            String pathquery = ipasService.findUserDocIPASS(applicationNo, result.getCc_notes());
                            String fullPath = this.uploadIPASDoc + "/" + pathquery;
//                            System.out.println("fitria: " + pathquery + fullPath);
                            file = new File(fullPath);
                            if (file.exists() && !file.isDirectory()) {
                                dwnUrl = "/dwn/1/" + Base64.getEncoder().encodeToString(pathquery.getBytes());
//                            System.out.println("cekdata: "+  pathquery );
                                tombol6 = "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin(dwnUrl) + "\" target=\"_blank\">Tanda Terima Userdoc</a>";
                            }
                    }

                    //find userdoc Madrid
                    if ( result.getCc_notes() != null ) {
                        String applicationNo = result.getTxTmGeneral().getId();
                        String pathquery = ipasService.findUserDocIPASS(applicationNo, result.getCc_notes());
                        String fullPathum = this.uploadIPASDoc + "/" + pathquery;
//                        System.out.println("++++++++++++++PATH nya: "+  pathquery );
                        File file4 = null;
                        file4 = new File (fullPathum);
                        if (file4.exists() && !file4.isDirectory()) {
                            dwnUrl = "/dwn/1/"  + Base64.getEncoder().encodeToString(pathquery.getBytes());
//                            System.out.println("cekdata: "+  pathquery );
                            tombol6 = "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin(dwnUrl) + "\" target=\"_blank\">Download</a>";
                        }

                    }

                    String tombol8 = "";
                    // untuk file blob
                    if (result.getFileUploadPath() != null ){

                        String offiDoc = result.getFileUploadPath();
                        String filename = null;
//                        logger.info("ISI TOMBOL 3 = "+tombol3.toString());
//                        logger.info("ISI TOMBOL 5 = "+tombol5.toString());
                        try {
                            String pathFolder = DateUtil.formatDate(result.getCreatedDate(), "yyyy/MM/dd/");
                            file = new File (downloadLettersFilePath + pathFolder + offiDoc);
                            if (file.isFile() && file.canRead()) {
                                filename = downloadLettersFilePath + pathFolder + offiDoc;
//                                logger.info("FILE : "+file);
                                tombol3 = "";
                                tombol5 = "";
                                tombol6 = "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin("/download-offidoc?file=" +filename) + "\" target=\"_blank\">Download</a>";
                            } else {
//                                System.out.println("File doc " + offiDoc + " tidak ditemukan"
//                                        + " di folder "+ downloadLettersFilePath + pathFolder );
                                String pathquery = ipasService.findBlobDocIPASS(offiDoc);
//                                logger.info("OFFICEDOC NO: "+  offiDoc );
//                                logger.info("PATH "+pathquery);

                                file = new File (pathquery);
                                if (file.exists() && !file.isDirectory()) {
                                    tombol3 = "";
                                    tombol5 = "";

                                    filename = downloadLettersFilePath + pathFolder + offiDoc;
                                    tombol6 = "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin("/download-offidoc?file=" + filename) + "\">Download</a>";
                                }
                                else{  // untuk file blob yang pdf
                                        File file3 = null;
                                        String lastOffiDoc = result.getFileUploadPath().substring(result.getFileUploadPath().length() - 10, result.getFileUploadPath().length());
                                        String applicationNo = result.getTxTmGeneral().getId();
                                        String pathquery2 = ipasService.findPDFDOCBlobIPASS(applicationNo,lastOffiDoc);
                                        String pathquery3 = ipasService.findPDFDOCBlobIPASS(applicationNo,lastOffiDoc);
                                        pathquery2 = pathquery2.replace(".doc", ".pdf");
                                        String fullPath2 = this.uploadIPASDoc + "/" + pathquery2;
                                        String fullPath3 = this.uploadIPASDoc + "/" + pathquery3;

//                                        System.out.println("Fitria 1 " + lastOffiDoc +"path2" +pathquery2+"fulpat"+fullPath2);
                                        file = new File (fullPath2);
                                        if (file.exists() && !file.isDirectory() && file.getName().endsWith(".pdf")) {
                                            String dwnUrl2 = "/dwn/1/" + Base64.getEncoder().encodeToString(pathquery2.getBytes());
//                                            System.out.println("Fitria 2 " +dwnUrl2+"ofidoc "+ lastOffiDoc +"path2 " +pathquery2+ "file "+file +"fulpath "+fullPath2);

                                            tombol6 = "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin(dwnUrl2) + "\" target=\"_blank\">Download PDF</a>";
                                        }

                                        file3 = new File (fullPath3);
//                                    System.out.println("Fitria 3 " + lastOffiDoc +"path3 " +pathquery3+ "file3 "+file3 +"fulpath "+fullPath3);
                                        if (file3.exists() && !file3.isDirectory()&& file3.getName().endsWith(".doc"))
                                        {
                                            String dwnUrl3 = "/dwn/1/"  + Base64.getEncoder().encodeToString(pathquery3.getBytes());
                                            tombol8 = "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin(dwnUrl3) + "\" target=\"_blank\">Download DOC</a>";
                                        }

                                }

                            }

                        } catch (NullPointerException e) {
                        }
                    }


                    String tombol7 = "";
                    String tombol9="";
                    if ((user.getId().equals(result.getCreatedBy().getId())) && result.getmWorkflowProcessActions() != null) {
                        if (!result.isApproved() && result.getmWorkflowProcessActions().getAction().getType().equalsIgnoreCase("Download")) {
                            tombol7 = "<button id=\"" + result.getId() + "\" class='btn btn-default btn-xs' onclick=\"kirimKeInbox(this)\">Kirim ke Inbox</button>";
                        }
                        if (result.getmWorkflowProcessActions().getAction().getType().equalsIgnoreCase("Download")) {
                            tombol9 = "<button class='btn btn-default btn-xs' style=\"background-color:#e7e7e7; color:black;\" onclick=\"kirimKeEmail('"+result.getId()+"')\">Kirim ke Email</button>";
                        }
                    }

                    String tombolPascaDone= "";
                     MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

                    if (result.getTxPostReception() != null) {
                            String txpostStatus = result.getTxPostReception().getTxPostReceptionDetail().getmStatus().getId();
                            String txGeneralid= result.getTxPostReception().getTxPostReceptionDetail().getTxTmGeneral().getId();
                            if (!txpostStatus.equalsIgnoreCase("DONE") && mUser.hasAccessMenu("T-MUPDONE")) {
                                tombolPascaDone = "<button id=\"" + result.getTxPostReception().getId() + "\" txgeneralid=\""+txGeneralid+"\" class='btn btn-default btn-xs' onclick=\"updatePascaStsDone(this)\">Sudah Dicek/Dikerjakan</button>";
                            }
                    }
                    
                    String[] rows = new String[]{
                            "" + no,
                            result.getCreatedDate() != null ? formater.format(result.getCreatedDate()) : "",
                            result.getmWorkflowProcess() != null ? result.getmWorkflowProcess().getStatus().getName() : "",
                            result.getmWorkflowProcessActions() != null ? result.getmWorkflowProcessActions().getAction().getName() : "",
//                            result.getDueDate() != null ? parseDate(result.getDueDate(), "yyyy-MM-dd", "dd-MM-yyyy") : "",
//                            result.getTargetWorkflowProcess() != null ? result.getTargetWorkflowProcess().getStatus().getName() : "",
                            ccnote1.equalsIgnoreCase("") ? note : note + cc_note,
                            result.getTxPostReception() != null ? result.getTxPostReception().getPostNo() + "<br/>"+ result.getTxPostReception().geteFilingNo(): "",
                            result.getCreatedBy().getUsername(),
                            result.getApprovedDate() != null ? formater.format(result.getApprovedDate()) : "",
//                            result.getCreatedBy().getmSection() != null ? result.getCreatedBy().getmSection().getName(): "",
                            "<div class=\"btn-actions\">" +
                                    tombol1 +
                                    (tombol2 != "" ? "<br/>" : "") +
                                    tombol2 +
                                    (tombol3 != "" ? "<br/>" : "") +
                                    tombol3 +
                                    (tombol4 != "" ? "<br/>" : "") +
                                    tombol4 +
                                    (tombol5 != "" ? "<br/>" : "") +
                                    tombol5 +
                                    (tombol6 != "" ? "<br/>" : "") +
                                    tombol6 +
                                    (tombol7 != "" ? "<br/>" : "") +
                                    tombol7 +
                                    (tombol8 != "" ? "<br/>" : "") +
                                    tombol8 +
                                    (tombol9 != "" ? "<br/>" : "") +
                                    tombol9 +
                                    (tombolPascaDone != "" ? "<br/>" : "") +
                                    tombolPascaDone +
                                    "</div>"
                    };
                    data.add(rows);
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @PostMapping(value = REQUEST_MAPPING_AJAX_MONITOR_OPOSISI)
    public void doGetMonitorOposisiDataTables(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ParseException {
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
            String Appid = "";
            try {
                offset = Math.abs(Integer.parseInt(request.getParameter("start")));
            } catch (NumberFormatException e) {
            }
            try {
                limit = Math.abs(Integer.parseInt(request.getParameter("length")));
            } catch (NumberFormatException e) {
            }

            String[] searchByArr = request.getParameterValues("searchByArr[]");
//            System.out.println("Search " + searchByArr.toString());
            String[] keywordArr = request.getParameterValues("keywordArr[]");
//            System.out.println("KEY: " + keywordArr.toString());
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
                    if (searchBy != null && searchBy.equalsIgnoreCase("txTmGeneral.id")) {
                        Appid = value;
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


            GenericSearchWrapper<Object[]> searchResult = nativeQueryRepository.selectMonitorOposisi(Appid, limit, offset);

            int sumr = searchResult.getList().size();


            if (sumr > 0) {
                dataTablesSearchResult.setRecordsFiltered(sumr);
                dataTablesSearchResult.setRecordsTotal(sumr);
                MUser user = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

                int no = offset;
                TxPostReceptionDetail txPostReceptionDetail = null;
                for (Object[] result : searchResult.getList()) {
                    // olly@docotel.com -- start

                    no++;

                    // Ubah format tanggal dengan operasi string
                    String date = "";
                    if (result[0] != null) {
                        date = result[0].toString();
                        String[] parts = date.split("-");
                        String tahun = parts[0];
                        String bulan = parts[1];
                        String hari = parts[2];
                        String haria[] = hari.split(" ");
                        hari = haria[0];
                        date = hari + "-" + bulan + "-" + tahun;
                    }


                    String coloumn1 = date;
                    String coloumn2 = result[1] == null ? "" : result[1].toString();
                    String coloumn3 = result[2] == null ? "" : result[2].toString();
                    String coloumn4 = result[3] == null ? "" : result[3].toString();
                    String coloumn5 = result[4] == null ? "" : result[4].toString();
                    String coloumn6 = result[5] == null ? "" : result[5].toString();
                    String coloumn7 = "";
                    coloumn7="<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin("/cetak-pasca-permohonan?no=") + result[1].toString()+ "\" target=\"_blank\">Tanda Terima</a>";

                    String[] rows = new String[]{
                            "" + no,
                            coloumn1,
                            coloumn2,
                            coloumn3,
                            coloumn4,
                            coloumn5,
                            coloumn6,
                            coloumn7,

                    };
                    data.add(rows);
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @GetMapping(PATH_ADD_MONITOR)
    public String addMonitor(@RequestParam(value = "no", required = true) String no, Model model) {
        //adding support multi no, model attribute juga mesti di edit!
        List<MWorkflowProcess> listAvailableWfProcesses = new ArrayList<>(); //this WF status should be equal
        String prevStatusID = "";
        List<TxPostReception> txPostReceptions = new ArrayList<>();
        for (String No : no.split(",")) {
            try {
                TxTmGeneral ttg = txTmGeneralRepository.findTxTmGeneralByApplicationNo(No);
                List<TxMonitor> listCurrentMonitor = monitorService.findByTxTmGeneral(ttg);
                MWorkflow wf = ttg.getTxReception().getmWorkflow();
                List<MWorkflowProcess> listWfProcess = mWorkflowProcessRepository.findMWorkflowProcessesByWorkflowAndStatusFlag(wf, true);
                List<MWorkflowProcess> tempWFProcesses = new ArrayList<>();

                if (listCurrentMonitor.size() > 1 || ttg.getmStatus()!=null) {
                    model.addAttribute("currentStatus", ttg.getmStatus().getName());
                } else {
                    model.addAttribute("currentStatus", "-");
                }
                MUser user = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                MUserRole mUserRoleAdmin = userRoleService.findUserAdministrator(user.getId());
                model.addAttribute("mUserRoleAdmin", mUserRoleAdmin!=null? true: false);
                MUserRole mUserRole2 = userRoleService.findUserRole(user.getId());
//                System.out.println("tiga "+mUserRole2);
                String mUserRoleL=mUserRole2.getmRole().getId();
//                System.out.println("fit test "+mUserRoleL);


                for (MWorkflowProcess mwfp : listWfProcess) {
                    TxMonitor txm = listCurrentMonitor.get(0);
                    if (mwfp.getPrevStatus() != null) {
                        if (txm.getTxTmGeneral().getmStatus().getId().equalsIgnoreCase(mwfp.getPrevStatus().getId())) {
                            if (!mwfp.getStatus().isStaticFlag() && mwfp.getStatus().isStatusFlag()) {
                                if (mUserRoleAdmin != null){
                                    tempWFProcesses.add(mwfp);
                                }
                                else if (mUserRoleL.equalsIgnoreCase(mwfp.getRole().getId())) {
                                    tempWFProcesses.add(mwfp);
                                }
                                else {
                                    model.addAttribute("error", "Informasi, Username Anda tidak dapat melakukan update status.");
                                }
                            }
                        }
                    } else {
                        continue;
                    }
                }

                // validasi WFprocess list
//                if (listAvailableWfProcesses.size() != 0 && listAvailableWfProcesses != tempWFProcesses) {
//                    model.addAttribute("error", "Permohonan yang dipilih harus memiliki Status Permohonan yang sama.");
//                    return "permohonan/proses-permohonan";
//                }
                listAvailableWfProcesses = tempWFProcesses;

                // validasi status txtmgeneral
//                if (prevStatusID != "" && prevStatusID != ttg.getmStatus().getId()) {
//                    model.addAttribute("error", "Permohonan yang dipilih harus memiliki Status Permohonan yang sama.");
//                    return "permohonan/proses-permohonan";
//                }
                prevStatusID = ttg.getmStatus().getId();

                TxMonitor txMonitor = new TxMonitor();
                txMonitor.setTxTmGeneral(ttg);
                model.addAttribute("form", txMonitor);//keep one monitors

                //fetch all pasca txpostreception by all monitor
                for(TxMonitor mon : listCurrentMonitor){
                    if(mon.getTxPostReception()!=null) {
                        txPostReceptions.add(mon.getTxPostReception());
                    }
                }
            } catch (Exception e) {
                model.addAttribute("error", "Informasi, Permohonan ini memiliki Status yang beda, [" + No + "]");
                return "permohonan/proses-permohonan";
            }
        }
        model.addAttribute("wfPascaSelection", txPostReceptions.stream().distinct().collect(Collectors.toList()));
        model.addAttribute("wfProcess", listAvailableWfProcesses);
        model.addAttribute("applicationNo", no);

        return PAGE_ADD_MONITOR;
    }

    @GetMapping(PATH_ADD_MONITOR_PERUBAHAN)
    public String addMonitorPerubahan(@RequestParam(value = "no", required = true) String no, Model model) {
        TxTmGeneral ttg = txTmGeneralRepository.findTxTmGeneralByApplicationNo(no);
        List<TxMonitor> listCurrentMonitor = monitorService.findByTxTmGeneral(ttg);
        MWorkflow wf = ttg.getTxReception().getmWorkflow();

        List<MWorkflowProcess> listWfProcess = mWorkflowProcessRepository.findMWorkflowProcessesByWorkflowAndStatusFlagOrderByOrders(wf, true);
        List<MWorkflowProcess> listAvailableWfProcesses = new ArrayList<>();
        for (MWorkflowProcess mwfp : listWfProcess) {
            boolean found = false;
            for (TxMonitor txM : listCurrentMonitor) {
//                System.out.println(mwfp.getId() + " == " + txM.getmWorkflowProcess().getId());
                if (mwfp.getId().equalsIgnoreCase(txM.getmWorkflowProcess().getId())) {
                    found = true;
                    break;
                }
            }
            if (found && !mwfp.getStatus().isStaticFlag()) {
                listAvailableWfProcesses.add(mwfp);
            }
        }
        model.addAttribute("wfProcess", listAvailableWfProcesses.get(listAvailableWfProcesses.size() - 1));
        model.addAttribute("applicationNo", ttg.getApplicationNo());

        TxMonitor txMonitor = new TxMonitor();
        txMonitor.setTxTmGeneral(ttg);
        model.addAttribute("form", txMonitor);
        return PAGE_ADD_MONITOR_PERUBAHAN;
    }

    private String parseDate(String inputDate, String iFormat, String oFormat) throws ParseException {
        SimpleDateFormat inputFormat = new SimpleDateFormat(iFormat);
        SimpleDateFormat outputFormat = new SimpleDateFormat(oFormat);
        Date outputDate = new Date();
        try {
            outputDate = inputFormat.parse(inputDate);
        } catch (ParseException pe) {
            inputFormat = new SimpleDateFormat("dd/mm/yyyy");
            outputDate = inputFormat.parse(inputDate);
        }
        return outputFormat.format(outputDate);
    }

//    @PostMapping(value = REQUEST_MAPPING_MADRID_APPROVAL)
//    public void approveMadrid(@RequestParam("no") String no, Model model,
//            final HttpServletRequest request, final BindingResult errors, final HttpServletResponse response,
//            final HttpSession session) throws IOException, ParseException {
//    	TxTmGeneral ttg = txTmGeneralRepository.findTxTmGeneralByApplicationNo(no);
//    	ttg.setApprovedDate(new Timestamp(System.currentTimeMillis()));
//    	ttg.setApprovedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
//    	txTmGeneralRepository.save(ttg);
//    	
//    	writeJsonResponse(response, 200);;
//    }

    @RequestMapping(path = REQUEST_MAPPING_MADRID_APPROVAL, method = {RequestMethod.POST})
    public void approveMadrid(@RequestParam("appNo") String appNo, final HttpServletRequest request, final HttpServletResponse response,
                              final HttpSession session) throws IOException, ParseException {
        TxTmGeneral ttg = txTmGeneralRepository.findTxTmGeneralByApplicationNo(appNo);
        ttg.setApprovedDate(ttg.getFilingDate());
        ttg.setApprovedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        txTmGeneralRepository.save(ttg);

        writeJsonResponse(response, 200);
    }

    @PostMapping(PATH_ADD_MONITOR)
    //@Transactional
    public String saveMonitor(@RequestParam("file") MultipartFile file, @RequestParam Map<String, String> parameters, @ModelAttribute("form") TxMonitor data, Model model) throws IOException, ParseException {
        String nos = parameters.get("applicationNo");
        String postreceptionID = parameters.get("txPostReception.id");
        for (String no : nos.split(",")) {
            //re-create txmonitor
            TxTmGeneral ttg = txTmGeneralRepository.findTxTmGeneralByApplicationNo(no);
            TxMonitor txMonitor = new TxMonitor();
            txMonitor.setTxTmGeneral(ttg);
            txMonitor.setCc_notes(data.getCc_notes());
            txMonitor.setCc_role(data.getCc_role());
            txMonitor.setCreatedBy(data.getCreatedBy());
            txMonitor.setCreatedDate(data.getCreatedDate());
            txMonitor.setCreatedDateTemp(data.getCreatedDateTemp());
            txMonitor.setDueDate(data.getDueDate());
            txMonitor.setmWorkflowProcess(data.getmWorkflowProcess());
            txMonitor.setmWorkflowProcessActions(data.getmWorkflowProcessActions());
            txMonitor.setTargetWorkflowProcess(data.getTargetWorkflowProcess());
            txMonitor.setNotes(data.getNotes());
            txMonitor.setSkipLogAuditTrail(data.skipLogAuditTrail);
            //assign post reception to monitor!!!
            TxPostReception txPostReception = txPostReceptionCustomRepository.selectOne("id",postreceptionID);
            txMonitor.setTxPostReception( txPostReception);

            if (!file.isEmpty()) {
                // Get the file and save it
                String pathFolder = DateUtil.formatDate(txMonitor.getCreatedDate(), "yyyy/MM/dd/");
                if (Files.notExists(Paths.get(uploadFileLettersUploaddocPath + pathFolder))) {
                    Files.createDirectories(Paths.get(uploadFileLettersUploaddocPath + pathFolder));
                }
                String filePath = uploadFileLettersUploaddocPath + pathFolder + System.currentTimeMillis() + "-" + file.getOriginalFilename();
                byte[] bytes = file.getBytes();
                Path path = Paths.get(filePath);
                Files.write(path, bytes);
                txMonitor.setFileUploadPath(filePath);
            } else {
                txMonitor.setFileUploadPath(null);
            }

            MWorkflowProcessActions wfProcessActions = mWorkflowProcessActionRepository.findOne(txMonitor.getmWorkflowProcessActions().getId());
            txMonitor.setmWorkflowProcessActions(wfProcessActions);

            if (txMonitor.getDueDate() != null && txMonitor.getmWorkflowProcessActions().getAction().getType().equalsIgnoreCase("Otomatis")) {
                txMonitor.setDueDate(parseDate(txMonitor.getDueDate(), "dd-mm-yyyy", "yyyy-mm-dd"));
            } else {
                txMonitor.setDueDate(null);
            }

            if (txMonitor.getTargetWorkflowProcess() != null && StringUtils.isBlank(txMonitor.getTargetWorkflowProcess().currentId())) {
                txMonitor.setTargetWorkflowProcess(null);
            }

            MWorkflowProcess mWorkflowProcess = txMonitor.getmWorkflowProcess();
            String monitor_id = txMonitor.getId();
            String idWorkFlow = mWorkflowProcess.getId();

            MWorkflowProcess ccWorkProcess = mWorkflowProcessRepository.findOne(idWorkFlow);
            String cc_role = "";
            MRole irole = new MRole();

            if (ccWorkProcess != null) {
                try {
                    irole = ccWorkProcess.getCc_role();
                    String therole = irole.getId();
                    txMonitor.setCc_role(irole);
                } catch (Exception e) {
                }
            }
            MWorkflowProcess wfProcess = mWorkflowProcessRepository.findOne(txMonitor.getmWorkflowProcess().getId());
            txMonitor.setmWorkflowProcess(wfProcess);

            //TxTmGeneral ttg = txTmGeneralRepository.getOne(data.getTxTmGeneral().getId());
            ttg.setmStatus(txMonitor.getmWorkflowProcess().getStatus());
            ttg.setmAction(txMonitor.getmWorkflowProcessActions().getAction());
            ttg.setCreatedDate(new Timestamp(System.currentTimeMillis()));
            ttg.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

            txMonitor.setTxTmGeneral(ttg);
            txMonitor.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            if (txPostReception!=null){
                TxPostReceptionDetail txPostReceptionDetail = txPostReception.getTxPostReceptionDetail() ;
                monitorService.saveMonitor(txMonitor, txPostReceptionDetail);
            }else {
                monitorService.saveMonitor(txMonitor, null);
            }
        }
        if (!nos.contains(","))
            return REDIRECT_LIST_MONITOR + "?no=" +nos;
        else {
             model.addAttribute("error", "Update status berhasil");
//            return "permohonan/proses-permohonan";
            return REDIRECT_PROCESS_LIST;
        }
    }

    @GetMapping(PATH_REMOVE_MONITOR)
    public String removeMonitor(@RequestParam(value = "id", required = true) String id, @RequestParam(value = "no", required = true) String no, @RequestParam(value = "docid", required = true) String docid) {
        TxTmGeneral ttg = txTmGeneralRepository.findTxTmGeneralByApplicationNo(no);
        TxMonitor monitorpasca = monitorService.findOne(id);
        if (monitorpasca.getTxPostReception() != null) {
            TxPostReceptionDetail txPostReceptionDetail = txPostReceptionDetailCustomRepository.selectOneByAppId(monitorpasca.getTxPostReception().getId());
            String txpostStatus = txPostReceptionDetail.getTxPostReception().getmStatus().getId();
            if (!txpostStatus.equalsIgnoreCase("NOTYET")) {
                monitorService.saveTxPostInMonitor(monitorpasca, txPostReceptionDetail);
            }
        }
        monitorService.remove(id, docid, no, monitorpasca.getId());

        List<TxMonitor> monitors = monitorService.findByIdGeneral(ttg.getId());

        if (monitors.size() > 0) {
            TxMonitor monitor = monitors.get(0);
            if (monitor.getTxPostReception() == null) {
                ttg.setmStatus(monitor.getmWorkflowProcess().getStatus());
            }
            else {
                List<TxMonitor> monitors2 = monitorService.findByIdGeneral2(ttg.getId());
                TxMonitor monitor2=monitors2.get(0);
                ttg.setmStatus(monitor2.getmWorkflowProcess().getStatus());
            }

            try {
                if (monitor.getmWorkflowProcessActions().getAction() == null) {
                    ttg.setmAction(null);
                } else {
                    ttg.setmAction(monitor.getmWorkflowProcessActions().getAction());
                }

            } catch (Exception e) {
                ttg.setmAction(null);
            }
            txTmGeneralRepository.save(ttg);

        } else {
            ttg.setmStatus(null);
            txTmGeneralRepository.save(ttg);

        }

        return REDIRECT_LIST_MONITOR + "?no=" + no;
    }

    @PostMapping(REQUEST_MAPPING_AJAX_APPROVE_MONITOR)
    public void approveMonitor(final HttpServletRequest request, final HttpServletResponse response) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            String idMonitor = request.getParameter("idMonitor");
            Timestamp date = new Timestamp(System.currentTimeMillis());
            monitorService.approveMonitor(idMonitor,date);
            writeJsonResponse(response, true);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @PostMapping(REQUEST_MAPPING_AJAX_UPDATE_STATUS_PASCA)
    public void updatePascaStsDone(final HttpServletRequest request, final HttpServletResponse response) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            String postId = request.getParameter("postId");
            TxPostReceptionDetail postReceptionId = txPostReceptionDetailCustomRepository.selectOne("txPostReception.id",postId);

            pascaOnlineService.saveOrUpdatePascaStatus(postReceptionId.getTxPostReception().getId());
            writeJsonResponse(response, true);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }


    @GetMapping(PATH_UNDUH_MONITOR)
    public String unduhFile(HttpServletResponse response, @RequestParam(value = "id", required = true) String id, @RequestParam(value = "no", required = true) String no) throws Exception {
        TxMonitor monitor = monitorService.findOne(id);
        this.downloadFile(response, monitor.getFileUploadPath());
        return null;
    }

    @GetMapping(PATH_DOWNLOAD_OFFIDOC)
    public String unduhOffidocFile(HttpServletResponse response, @RequestParam(value = "file", required = true) String file) throws Exception {
        this.downloadFile(response, file);
        return null;
    }

    private void downloadFile(HttpServletResponse response, String file) throws Exception {

        File downloadFile = new File(file);
        FileInputStream inStream = new FileInputStream(downloadFile);
        response.setContentLength((int) downloadFile.length());
        //response.setContentType("application/pdf");
        // forces download
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());
        response.setHeader(headerKey, headerValue);
        OutputStream outStream = response.getOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead = -1;

        while ((bytesRead = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }

        inStream.close();
        outStream.close();
    }
    
    @RequestMapping(path = REQUEST_MAPPING_KIRIM_EMAIL, method = {RequestMethod.POST})
    public void kirimKeEmail(@RequestBody String id, final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {

    	try {
    		
    		List<Object[]> txMonitor = txMonitorRepository.findByApplicationId(id);
    		
    		String appId = String.valueOf(txMonitor.get(0)[5]);
            String brand = String.valueOf(txMonitor.get(0)[4]);            
            String email = String.valueOf(txMonitor.get(0)[3]);
            String name = String.valueOf(txMonitor.get(0)[2]);
            String monitorId = String.valueOf(txMonitor.get(0)[1]);
            String pathFolder = String.valueOf(txMonitor.get(0)[0]);
            String subject = "DJKI-Notifikasi Email (jangan di balas / no reply)";
            
            boolean status = false;
            
            String logo = "static/img/" + logoEmailImage;
            File file = new File(pathImage + logoEmailImage);
            if (file.exists()) {
                logo = pathImage + logoEmailImage;
            }
            
            String pdfName = "CetakMonitor-" + monitorId + ".pdf";
            String outputFilePdfSign = downloadLettersFilePath + pathFolder + "/" + pdfName;
            File filePdf = new File(outputFilePdfSign);
            if (filePdf.exists()) {
            	status = true;
            	emailService.kirimEmailWithAttacment(email, subject, name, appId, brand, logo, filePdf, pdfName, "eTemplateMonitorKirimEmail");
            }          
            

            writeJsonResponse(response, status);

        } catch (DataIntegrityViolationException e) {
            logger.error(e.getMessage(), e);
            writeJsonResponse(response, 500);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            writeJsonResponse(response, 500);
        }
    }


}
