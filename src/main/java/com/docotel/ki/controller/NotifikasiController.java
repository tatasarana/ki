package com.docotel.ki.controller;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.*;
import com.docotel.ki.model.transaction.TxDocumentParams;
import com.docotel.ki.model.transaction.TxMonitor;
import com.docotel.ki.model.transaction.TxPostReception;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.master.MUserRoleCustomRepository;
import com.docotel.ki.repository.custom.master.MWorkflowProcessCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxMonitorCustomRepository;
import com.docotel.ki.repository.master.MRoleRepository;
import com.docotel.ki.repository.master.MStatusRepository;
import com.docotel.ki.repository.master.MWorkflowProcessRepository;
import com.docotel.ki.repository.master.MWorkflowRepository;
import com.docotel.ki.repository.custom.transaction.TxTmGeneralCustomRepository;
import com.docotel.ki.repository.master.*;
import com.docotel.ki.repository.transaction.TxMonitorRepository;
import com.docotel.ki.repository.master.MRoleRepository;
import com.docotel.ki.repository.master.MStatusRepository;
import com.docotel.ki.repository.master.MWorkflowProcessRepository;
import com.docotel.ki.repository.master.MWorkflowRepository;
import com.docotel.ki.repository.transaction.TxTmGeneralRepository;
import com.docotel.ki.service.ReportService;
import com.docotel.ki.service.master.LawService;
import com.docotel.ki.service.master.MenuService;
import com.docotel.ki.service.master.WorkFlowProcessService;
import com.docotel.ki.service.master.WorkflowProcessActionsService;
import com.docotel.ki.service.transaction.MonitorService;
import com.docotel.ki.service.transaction.TrademarkService;
import com.docotel.ki.util.DateUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Controller 
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class NotifikasiController extends BaseController {

    @Autowired
    private TxTmGeneralRepository txTmGeneralRepository;
    @Autowired
    WorkFlowProcessService workFlowProcessService;
    @Autowired
    MenuService menuService;
    @Autowired private LawService lawService;
    @Autowired private WorkflowProcessActionsService workflowProcessActionsService;
    @Autowired
    private MWorkflowProcessRepository mWorkflowProcessRepository;
    @Autowired
    private MWorkflowProcessCustomRepository mWorkflowProcessCustomRepository;
    @Autowired
    private MWorkflowRepository mWorkflowRepository;
    @Autowired
    private MStatusRepository mStatusRepository;
    @Autowired
    private MonitorService monitorService;
    @Autowired
    private MRoleRepository mRoleRepository ;
    @Autowired
    TrademarkService trademarkService ;
    @Autowired
    MDocumentRepository documentRepository ;
    @Autowired
    ReportService reportService ;

    @Autowired
    MUserRoleCustomRepository mUserRoleCustomRepositoryRepository ;

    @Autowired
    TxMonitorCustomRepository txMonitorCustomRepository ;

    @Autowired
    TxMonitorRepository txMonitorRepository ;

    @Value("${download.output.letters.file.path}")
    private String downloadFileLetterMonitorOutput;



    private static final String DIRECTORY_PAGE = "notifikasi" ;
    private static final String PAGE_LIST = DIRECTORY_PAGE + "/list-notifikasi";

    private static final String REQUEST_MAPPING_LIST = "/notifikasi-role"+"*" ;
    private static final String DOWNLOAD_SURAT_MONITOR = "/download-surat" + "*" ;
    private static final String REQUEST_MAPPING_AJAX_LIST = "/search-list-notifikasi" ;
    private static final String PATH_EDIT_WORKFLOW_PROCESS ="" ;
    private static final String AJAX_REQUEST_CATATAN_TEMBUSAN = "catatan-notifikasi-tembusan" ;
    private static final String REQUEST_MAPPING_MONITOR_CETAK = "cetak-dokumen-monitor" + "*" ;
    private static final String PATH_PASCA_ONLINE_LIST = "/list-pasca-online";




    /* --------------------------------------- PERMOHONAN SECTION ---------------------------------------------------*/

    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "notifikasiRole");
//        model.addAttribute("subMenu", "notifikasi");
    }


    @RequestMapping(value = REQUEST_MAPPING_LIST , method = {RequestMethod.GET})
    public String doGetListNotifikasi(@RequestParam(value = "error", required = false) String error, final Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException
    {
        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("errorMessage", error);
        }
        return PAGE_LIST ;
    }


    @PostMapping(value = AJAX_REQUEST_CATATAN_TEMBUSAN)
    public void doCatatanNotifikasi(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            String no = request.getParameter("monitor_id");
            String catatan = request.getParameter("catatan");
            TxMonitor txMonitor = txMonitorCustomRepository.selectOne("id",no);
            txMonitor.setCc_notes(catatan);
            monitorService.saveMonitor(txMonitor,null);

            response.setStatus(1);

        }
    }


    @PostMapping(value = REQUEST_MAPPING_AJAX_LIST)
    public void doGetListDataTables(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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
                        if (searchBy.equalsIgnoreCase("startDate") || searchBy.equalsIgnoreCase("endDate")) {
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

//			searchCriteria.add(new KeyValue("onlineFlag", Boolean.FALSE, true));

            String orderBy = request.getParameter("order[0][column]");
            if (orderBy != null) {
                orderBy = orderBy.trim();
                if (orderBy.equalsIgnoreCase("")) {
                    orderBy = null;
                } else {
                    switch (orderBy) {
                        case "2":
                            orderBy = "txPostReception.postNo";
                            break;
                        case "3":
                            orderBy = "txTmGeneral.txReception.eFilingNo";
                            break;
                        case "4":
                            orderBy = "txTmGeneral.applicationNo";
                            break;
                        case "5":
                            orderBy = "mWorkflowProcess.status";
                            break;
                        case "6":
                            orderBy = "mWorkflowProcessActions.action.name";
                            break;
                        case "7":
                            orderBy = "createdBy.username";
                            break;
                        default:
                            orderBy = "createdDate";
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

            // cari berdasarkan Tx Monitor di role itu
            MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            MUserRole mUserRole = mUserRoleCustomRepositoryRepository.selectOne("mUser.id", mUser.getId(),true);

            MRole mRole = mUserRole.getmRole();

            searchCriteria.add(new KeyValue("cc_role.id", mRole.getId(), true));

//            List<TxMonitor> searchResult = new ArrayList <>();
//            searchResult = txMonitorCustomRepository.selectAll("cc_role.id",mRole.getId(),true,"createdDate","DESC",offset,limit);
            GenericSearchWrapper<TxMonitor> searchResult = trademarkService.searchMonitor(searchCriteria, orderBy, orderType, offset, limit);
            List<String[]> data = new ArrayList<>();

            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                AtomicInteger atomicInteger = new AtomicInteger(offset+1);
                if (searchResult !=null) {
                    searchResult.getList().stream().parallel().forEachOrdered(result -> {
//                    no++;

                        //tombol download
                        String folder = reportService.getFolderDownloadMonitor(result, true);


                        String btn_download = "";
                        String btn_generate = "";
                        if (result.getmWorkflowProcessActions() != null) {
                            if (result.getmWorkflowProcessActions().getAction().getType().equalsIgnoreCase("Download")) {
                                btn_download = "<a href=\"layanan/download-surat?no=" + result.getId() + "\" target=\"_blank\">Lihat Surat</a>";
                            }
                        }
//                     btn_generate = result.getmWorkflowProcessActions()!=null ? result.getmWorkflowProcessActions().getAction().getType().equalsIgnoreCase("Download")? "<a class=\"btn btn-xs btn-success\" href=\""+getPathURLAfterLogin("/download-dokumen?docid="+result.getmWorkflowProcessActions().getAction().getDocument().getId()+"&no="+result.getTxTmGeneral().getApplicationNo()+"&monitorId="+result.getId())+"\">Preview</a>":"" : "";

                        boolean isApproved = false;
                        try {
                            isApproved = result.isApproved();
                        } catch (NullPointerException e) {
                        } catch (Exception e) {
                        }

                        String nomorTransaksi = "";
                        try {
                            nomorTransaksi = result.getTxTmGeneral().getId();
                        } catch (NullPointerException e) {
                        } catch (Exception e) {
                        }

                        String nomorDokumen = "";
                        try {
                            nomorDokumen = result.getTxPostReception().getPostNo();
                        } catch (NullPointerException e) {
                        } catch (Exception e) {
                        }

                        String ApplicationNo = "";
                        try {
                            ApplicationNo = result.getTxTmGeneral().getApplicationNo();
                        } catch (NullPointerException e) {
                        } catch (Exception e) {
                        }

                        String status = "";
                        try {
                            MStatus mStatus = result.getmWorkflowProcess().getStatus();
                            status = mStatus.getName();
                        } catch (NullPointerException e) {
                            status = "No Status";
                        } catch (Exception e) {
                            status = "No status";
                        }

                        String TargetWorkflowProcess = "";
                        try {
                            TargetWorkflowProcess = result.getTargetWorkflowProcess().getId();
                        } catch (NullPointerException e) {
                        } catch (Exception e) {
                        }

                        String WorkflowProcessAction = "";
                        try {
                            WorkflowProcessAction = result.getmWorkflowProcessActions().getAction().getName();
                        } catch (NullPointerException e) {
                        } catch (Exception e) {
                        }

                        String strdate = "";
                        DateUtil date;
                        try {
                            strdate = DateUtil.formatDate(result.getCreatedDate(), "dd-MM-yyyy");
                        } catch (NullPointerException e) {
                        } catch (Exception e) {
                        }

                        String note = "";
                        String ccnote1 = "";

                        ccnote1 = result.getCc_notes() == null ? "" : result.getCc_notes();
                        String stylecatatan = "";
                        stylecatatan = ccnote1.equalsIgnoreCase("TELAH DIPERIKSA") ? "style=\"color:green\"" : "";
                        note = result.getNotes() == null ? "" : result.getNotes();
                        String monitor_id = result.getId();
                        String cc_note = "<br/>______<br/>Catatan tembusan:<br/>" + "<span " + stylecatatan + " id=\"catatan-" + monitor_id + "\">" + ccnote1 + "</span>";

//                    String buttonCetak =  "<a class=\"btn btn-info btn-xs\" href=\"" + getPathURLAfterLogin(PATH_PASCA_ONLINE_CETAK) + "?no=" + result.getId() + "\">Tanda Terima</a>";

                        String btn_approve = isApproved ? "" : "<br/><a class=\"btn btn-xs btn-success\" href=\"layanan/approve-ccnotifikasi?no=" + monitor_id + "\">Setujui</a>";
                        String btn_catatan = "";
//                    if(!result.isApproved()){
                        btn_catatan = ccnote1.equalsIgnoreCase("") ? "" : "<br/><button type=\"button\" class=\"btn btn-xs btn-default\"   onclick=\"modalUpdate('" + monitor_id + "')\" >Beri Catatan</button>";
//                    }
                        data.add(new String[]{
                                "" + atomicInteger.getAndIncrement(),
                                strdate,
                                nomorDokumen,
                                nomorTransaksi,
                                ApplicationNo,
                                status,
                                WorkflowProcessAction,
//                            workflow,
                                result.getCreatedBy().getUsername(),
                                ccnote1.equalsIgnoreCase("") ? note : note + cc_note,
                                "<div class=\"btn-actions\">" +
                                        btn_download +
                                        btn_approve +
                                        btn_catatan + "</div>"
//                            btn_download +" - "+ btn_generate
//                                   +btn_approve
//                                    +btn_catatan,

                        });
                    });
                }
            }
            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }


    @GetMapping("/approve-ccnotifikasi")
    public String doApproveCCNotifikasi(@RequestParam(value = "no", required = true) String no){
        Timestamp date = new Timestamp(System.currentTimeMillis());
        MUser user = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        TxMonitor txMonitor = txMonitorCustomRepository.selectOne("id",no);
        txMonitor.setApproved(true);
        txMonitor.setApprovedDate(date);
//        txMonitor.setSendDate(date);
        txMonitor.setCc_notes("TELAH DIPERIKSA");
        txMonitor.setApprovedBy(user);
        monitorService.saveMonitor(txMonitor,null);

        return "redirect:" + BaseController.PATH_AFTER_LOGIN + REQUEST_MAPPING_LIST  ;
    }



    @RequestMapping(value = DOWNLOAD_SURAT_MONITOR, method = {RequestMethod.GET})
    @ResponseBody
    //cetak-pasca-online
    public String doDownloadSuratMonitor(final HttpServletRequest request, final HttpServletResponse response) throws IOException
    {
        String monitorId = request.getParameter("no");
        TxMonitor txMonitor = txMonitorCustomRepository.selectOne("id",monitorId,true);
        if (txMonitor == null){
            response.sendRedirect( PATH_AFTER_LOGIN + REQUEST_MAPPING_LIST + "?error=" +"Halaman tidak ditemukan" );
            return "";
        }
//        btn_download = result.getmWorkflowProcessActions()!=null ? result.getmWorkflowProcessActions().getAction().getType().equalsIgnoreCase("Download")? "<a class=\"btn btn-xs btn-success\" href=\""+getPathURLAfterLogin("/download-dokumen?docid="+result.getmWorkflowProcessActions().getAction().getDocument().getId()+"&no="+result.getTxTmGeneral().getApplicationNo()+"&monitorId="+result.getId())+"\">Preview</a>":"" : "";
        String docid = txMonitor.getmWorkflowProcessActions().getAction().getDocument().getId();
        String no = txMonitor.getTxTmGeneral().getApplicationNo();

        TxTmGeneral txTmGeneral = txTmGeneralRepository.findOne(txMonitor.getTxTmGeneral().getId());

        MDocument document = documentRepository.findOne(docid);
        FileInputStream fis = new FileInputStream(new File(document.getFilePath()));
        String fileTemplate = FilenameUtils.getName(document.getFilePath());
        fileTemplate = fileTemplate.substring(0, fileTemplate.length() - 5);

        response.setContentType("application/pdf");
//        response.setHeader("Content-disposition", "attachment; filename=CetakPasca-" + txPostReception.geteFilingNo() + ".pdf");

        String folder = "";
        String fileName ="" ;
        String pathFolder = DateUtil.formatDate(txMonitor.getCreatedDate(), "yyyy/MM/dd/");

        String outputFile = downloadFileLetterMonitorOutput + pathFolder + txTmGeneral.getApplicationNo() + "-" + fileTemplate + ".docx";
        String outputFilePdf = downloadFileLetterMonitorOutput + pathFolder + txTmGeneral.getApplicationNo() + "-" + fileTemplate + "_noSIGN.pdf";
        String outputFilePdfSign = downloadFileLetterMonitorOutput + pathFolder + txTmGeneral.getApplicationNo() + "-" + fileTemplate + ".pdf";
        String outputFilePdfSignWithwatermark = downloadFileLetterMonitorOutput + pathFolder + txTmGeneral.getApplicationNo() + "-" + fileTemplate + ".pdf";

        response.setContentType("application/pdf");
        response.setHeader("Content-disposition", "inline; filename=CetakMonitor-"  + monitorId + ".pdf");

       folder =  downloadFileLetterMonitorOutput + pathFolder;
        //System.out.println(folder);
       fileName = "CetakMonitor-"  + monitorId+ ".pdf";

        try (OutputStream output = response.getOutputStream()) {
            Path path = Paths.get(folder + fileName);
            Files.copy(path, output);
            output.flush();
        } catch(IOException e) {
        }


        return no ;
    }


    @RequestMapping(value = REQUEST_MAPPING_MONITOR_CETAK, method = {RequestMethod.GET})
    @ResponseBody
    public String doCetakPasca(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        String postId = request.getParameter("no");


        return null ;
    }



}