package com.docotel.ki.controller;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.*;
import com.docotel.ki.model.transaction.TxMonitor;
import com.docotel.ki.model.transaction.TxPostReceptionDetail;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.master.MUserRoleCustomRepository;
import com.docotel.ki.repository.custom.master.MWorkflowProcessCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxMonitorCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxPostReceptionDetailCustomRepository;
import com.docotel.ki.repository.master.*;
import com.docotel.ki.repository.transaction.TxMonitorRepository;
import com.docotel.ki.repository.transaction.TxTmGeneralRepository;
import com.docotel.ki.service.ReportService;
import com.docotel.ki.service.master.*;
import com.docotel.ki.service.transaction.MonitorService;
import com.docotel.ki.service.transaction.TrademarkService;
import com.docotel.ki.util.DateUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Controller 
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class InboxSuratController extends BaseController {

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
    TxPostReceptionDetailCustomRepository txPostReceptionDetailCustomRepository ;

    @Autowired
    TxMonitorRepository txMonitorRepository ;

    @Value("${download.output.letters.file.path}")
    private String downloadFileLetterMonitorOutput;



    private static final String DIRECTORY_PAGE = "inbox-surat" ;
    private static final String PAGE_LIST = DIRECTORY_PAGE + "/list-inbox-surat";

    private static final String REQUEST_MAPPING_INBOX_SURAT_LIST = "/inbox-surat-role"+"*" ;
    private static final String DOWNLOAD_SURAT_MONITOR = "/download-inbox-surat" + "*" ;
    private static final String REQUEST_MAPPING_AJAX_LIST = "/search-list-inbox-surat" ;
    private static final String REQUEST_MAPPING_AJAX_NOTIFICATION_LIST = "/notification-list-inbox-surat" ;
    private static final String UPDATE_MONITOR = "/update-status-notification" ;
    private static final String REQUEST_MAPPING_MONITOR_CETAK = "cetak-dokumen-inbox-surat" + "*" ;


    /* --------------------------------------- PERMOHONAN SECTION ---------------------------------------------------*/

    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "inbox-suratRole");
        model.addAttribute("subMenu", "inbox-surat");
    }


    @RequestMapping(value = REQUEST_MAPPING_INBOX_SURAT_LIST , method = {RequestMethod.GET})
    public String doGetListInboxSurat(@RequestParam(value = "error", required = false) String error, final Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException
    {
        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("errorMessage", error);
        }
        return PAGE_LIST ;
    }


    @RequestMapping(value = REQUEST_MAPPING_AJAX_NOTIFICATION_LIST, method = {RequestMethod.GET})
    public void doGetListInboxSuratNotification(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            DataTablesSearchResult dataTablesSearchResult = new DataTablesSearchResult();
            try {
                dataTablesSearchResult.setDraw(Integer.parseInt(request.getParameter("draw")));
            } catch (NumberFormatException e) {
                dataTablesSearchResult.setDraw(0);
            }

            int offset = 0;
            int limit = 0;

            try {
                limit = Math.abs(Integer.parseInt(request.getParameter("length")));
            } catch (NumberFormatException e) {
            }

            List<KeyValue> searchCriteria = null;

            searchCriteria = new ArrayList<>();

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

            MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            boolean is_approved = true;
            boolean is_download = false;
            searchCriteria.add(new KeyValue("approved", is_approved, true));
            searchCriteria.add(new KeyValue("is_download", is_download, false));
            //searchCriteria.add(new KeyValue("txTmGeneral.createdBy", mUser, true));
            searchCriteria.add(new KeyValue("createdBy", mUser, true));
            //searchCriteria.add(new KeyValue("txPostReception.createdBy", mUser, true));

            GenericSearchWrapper<TxMonitor> searchResult = trademarkService.searchMonitor(searchCriteria, orderBy, orderType, offset, limit);
            List<String[]> data = new ArrayList<>();

            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                AtomicInteger atomicInteger = new AtomicInteger(offset + 1);
                if (searchResult != null) {
                    searchResult.getList().stream().parallel().forEachOrdered(result -> {
//                    no++;

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

                        String strdate = "";
                        DateUtil date;
                        try {
                            strdate = DateUtil.formatDate(result.getCreatedDate(), "dd-MM-yyyy");
                        } catch (NullPointerException e) {
                        } catch (Exception e) {
                        }

                        data.add(new String[]{
                                "" + atomicInteger.getAndIncrement(),
                                strdate,
                                ApplicationNo,
                                nomorDokumen,
                                "<li id="+result.getId()+"><a href=\"" + getPathURLAfterLogin(REQUEST_MAPPING_INBOX_SURAT_LIST) + "\">" + ApplicationNo + "<br>" + strdate +"</a></li>",
//                                result.getId(),
                                //<li><a href="'+link+'"><span class="tab">' + element[3] + '</span><br/><span class="tab">' + element[2] + '</span><br/><span class="tab">' + element[1] + '</span></a></li>


                        });
                    });
                }
            }
            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        }
    }

    @RequestMapping(value = REQUEST_MAPPING_AJAX_LIST, method = {RequestMethod.POST})
    public void doGetListInboxSuratDataTables(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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

            MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            boolean is_approved = true;
            boolean is_download = false;
            searchCriteria.add(new KeyValue("approved", is_approved, true));
//            searchCriteria.add(new KeyValue("is_download", is_download, false));

            /**
             * ubah ambil user dari txPostReception
             * karena user pada txTmGeneral bukan user pemohon
             */
            //searchCriteria.add(new KeyValue("txTmGeneral.createdBy", mUser, true));
            searchCriteria.add(new KeyValue("createdBy", mUser, true));

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

                        btn_download = "<a href=\"layanan/download-surat?no=" + result.getId() + "\" target=\"_blank\">Download</a>";

                        String nomorTransaksi = "";
                        try {
                            nomorTransaksi = result.getTxPostReception().geteFilingNo();
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

                        String strdate = "";
                        DateUtil date;
                        try {
                            strdate = DateUtil.formatDate(result.getCreatedDate(), "dd-MM-yyyy");
                        } catch (NullPointerException e) {
                        } catch (Exception e) {
                        }

                        /**
                         * cek apakah data ada di tx_tm_general dan tx_post_reception_detail
                         */
                        TxTmGeneral txTmGeneral = txTmGeneralRepository.findOne(result.getTxTmGeneral().getId());
                        TxPostReceptionDetail txPostReceptionDetail = txPostReceptionDetailCustomRepository.selectOne("txTmGeneral", result.getTxTmGeneral());

                        if(txTmGeneral != null || txPostReceptionDetail != null) {
                            data.add(new String[]{
                                    "" + atomicInteger.getAndIncrement(),
                                    strdate,
                                    nomorTransaksi,
                                    nomorDokumen,
                                    ApplicationNo,
                                    "<div class=\"btn-actions\">" +
                                            btn_download +
                                            "</div>"
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

    @RequestMapping(value = UPDATE_MONITOR, method = {RequestMethod.POST})
    public void doUpdateNotificationStatus(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            String monitor_id = request.getParameter("monitorId");

            TxMonitor txMonitor = txMonitorCustomRepository.selectOne("id",monitor_id);
            txMonitor.setIs_download(true);
            monitorService.saveMonitor(txMonitor,null);

            response.setStatus(HttpServletResponse.SC_OK);

        }
    }


    @RequestMapping(value = DOWNLOAD_SURAT_MONITOR, method = {RequestMethod.GET})
    @ResponseBody
    //cetak-pasca-online
    public String doDownloadInboxSuratMonitor(final HttpServletRequest request, final HttpServletResponse response) throws IOException
    {
        String monitorId = request.getParameter("no");
        TxMonitor txMonitor = txMonitorCustomRepository.selectOne("id",monitorId,true);
        if (txMonitor == null){
            response.sendRedirect( PATH_AFTER_LOGIN + REQUEST_MAPPING_INBOX_SURAT_LIST + "?error=" +"Halaman tidak ditemukan" );
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
       fileName = "CetakSurat-"  + monitorId+ ".pdf";

        try (OutputStream output = response.getOutputStream()) {
            Path path = Paths.get(folder + fileName);
            Files.copy(path, output);
            output.flush();
        } catch(IOException e) {
        }


        return no ;
    }

}