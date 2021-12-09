package com.docotel.ki.controller.laporan;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.MAction;
import com.docotel.ki.model.master.MFileType;
import com.docotel.ki.model.master.MStatus;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.pojo.LapPascaPermohonan;
import com.docotel.ki.repository.custom.master.MUserCustomRepository;
import com.docotel.ki.repository.transaction.TxMonitorRepository;
import com.docotel.ki.repository.transaction.TxPubsJournalRepository;
import com.docotel.ki.service.master.*;
import com.docotel.ki.service.transaction.MonitorService;
import com.docotel.ki.service.transaction.TrademarkService;
import com.docotel.ki.util.DateUtil;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class LapPascaPermohonanController extends BaseController {

    public static final String PATH_LAPORAN_PASCA_PERMOHONAN = "/laporan-pasca-permohonan";
    public static final String PATH_AJAX_SEARCH_LIST = "/cari-lap-pasca-permohonan";
    private static final String PATH_EXPORT_DATA_LAPORAN_PASCA_PERMOHONAN = "/cetak-lap-pasca-permohonan";
    private static final String DIRECTORY_PAGE = "laporan/";
    private static final String PAGE_LAPORAN_PASCA_PERMOHONAN = DIRECTORY_PAGE + "laporan-pasca-permohonan";
    private static final String REQUEST_MAPPING_AJAX_SEARCH_LIST = PATH_AJAX_SEARCH_LIST + "*";
    private static final String REQUEST_MAPPING_LAPORAN_PASCA_PERMOHONAN = PATH_LAPORAN_PASCA_PERMOHONAN + "*";
    private static final String REQUEST_EXPORT_LAPORAN_PASCA_PERMOHONAN = PATH_EXPORT_DATA_LAPORAN_PASCA_PERMOHONAN + "*";

    @Autowired
    private FileService fileService;
    @Autowired
    private TrademarkService trademarkService;
    @Autowired
    private StatusService statusService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private MActionService mActionService;
    @Autowired
    private UserService userService;
    @Autowired
    private TxPubsJournalRepository txPubsJournalRepository;
    @Autowired
    private MonitorService monitorService;
    @Autowired
    private TxMonitorRepository txMonitorRepository ;

    /* --------------------------------------- PERMOHONAN SECTION ---------------------------------------------------*/

    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "laporan");
        model.addAttribute("subMenu", "laporanPascaPermohonan");
    }

    @RequestMapping(path = REQUEST_MAPPING_LAPORAN_PASCA_PERMOHONAN)
    public String doShowPageLapFungsionalPemeriksa(@RequestParam(value = "error", required = false) String error, Model model, final HttpServletRequest request, final HttpServletResponse response) {
        
    	List<Object[]> data = txMonitorRepository.userPascaPermohonan();
    	List<MUser> mUser = new ArrayList<MUser>();
		for(Object[] obj : data) {
			MUser user = new MUser();
			user.setId(String.valueOf(obj[0]));
			user.setUsername(String.valueOf(obj[1]));
			mUser.add(user);			
		}
        model.addAttribute("mUser", mUser);
        Collections.sort(mUser, (o1, o2) -> o1.getUsername().compareTo(o2.getUsername()));

        List<MFileType> fileTypeList = fileService.findByMenu("PASCA"); //fileService.findMFileTypeByStatusFlagTrue();
        model.addAttribute("fileTypeList", fileTypeList);
        Collections.sort(fileTypeList, (o1, o2) -> o1.getDesc().compareTo(o2.getDesc()));

        List<MAction> actionList = mActionService.findAll();
        model.addAttribute("actionList", actionList);

        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("errorMessage", error);
        }

        return PAGE_LAPORAN_PASCA_PERMOHONAN;
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
                        if((searchBy.equalsIgnoreCase("mAction.id") || searchBy.equalsIgnoreCase("createdBy.id") || searchBy.equalsIgnoreCase("mStatus.id")) && value != null) {
                            if (StringUtils.isNotBlank(value)) {
                                searchCriteria.add(new KeyValue(searchBy, value, true));
                            }
                        } else if (searchBy.equalsIgnoreCase("startDate") || searchBy.equalsIgnoreCase("endDate")) {
                            if (StringUtils.isNotBlank(value)) {
                                try {
                                    searchCriteria.add(new KeyValue(searchBy, DateUtil.toDate("dd/MM/yyyy", value), true));
                                } catch (ParseException e) {
                                }
                            }
                        } else {
                            if (StringUtils.isNotBlank(value)) {
                            	System.out.println("fileType : "+value);
                                searchCriteria.add(new KeyValue(searchBy, value, true));
                            }
                        }
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

            GenericSearchWrapper<TxMonitor> searchResult = monitorService.searchMonitorPascaPermohonan(searchCriteria, "createdDate", orderType, offset, limit);

            if (searchResult.getCount() > 0) {
                //System.out.println("Jumlah Line:"+ searchResult.getCount());
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxMonitor result : searchResult.getList()) {
                    // cari model Brand nya
                    no++;
                    String brandName = "-";
                    String status = "-";
                    String aksi = "-";
                    String regNo = "-";
                    String sbClassList = "-";
                    String username = "-";
                    ArrayList<String> kelass = new ArrayList<String>();

                    try{
                        username = result.getCreatedBy().getUsername();
                    } catch (NullPointerException e){

                    }

                    try {
                        regNo = result.getTxTmGeneral().getTxRegistration().getNo();
                    } catch (NullPointerException e) {
                    }


                    try {
                        brandName = result.getTxTmGeneral().getTxTmBrand().getName();
                    } catch (NullPointerException e) {
                    }

                    try {
                        status = result.getmWorkflowProcess().getStatus().getName();
                    } catch (NullPointerException e) {

                    }

                    try {
                        aksi = result.getmWorkflowProcessActions().getAction().getName();
                    } catch (NullPointerException e) {

                    }


                    try {
                        for (TxTmClass txTmClass : result.getTxTmGeneral().getTxTmClassList()) {
                            kelass.add("" + txTmClass.getmClass().getNo());
                        }
                        Set<String> temp = new LinkedHashSet<String>(kelass);
                        String[] unique = temp.toArray(new String[temp.size()]);
                        if (unique.length > 0) {
                            sbClassList = String.join(",", unique);
                        }

                    } catch (NullPointerException e) {
                    }


                    String data_applicationNo = result.getTxTmGeneral().getApplicationNo();
                    String data_process_date = result.getCreatedDate().toString();
                    String data_post_date = "-";
                    String data_post_filetype = "-";
                    String data_post_no = "-";
                    String data_post_efilingno = "-";
                    if(result.getTxPostReception()!=null) {
                        data_post_date = result.getTxPostReception().getPostDate().toString();
                        data_post_filetype = result.getTxPostReception().getmFileType().getDesc();
                        data_post_no = result.getTxPostReception().getPostNo();
                        data_post_efilingno = result.getTxPostReception().geteFilingNo();
                    }

                    // if (result.getTxTmGeneral().getmStatus() != null) {
                    if (status != null) {

                        data.add(new String[]{
                                "" + no,
                                data_post_date,
                                data_process_date,
                                data_post_efilingno,
                                data_post_no,
                                data_applicationNo, // Nomor Permohonan
                                regNo,
                                brandName,
                                data_post_filetype,
                                status,
                                aksi,
                                username
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

    @GetMapping(REQUEST_EXPORT_LAPORAN_PASCA_PERMOHONAN)
    public void doExportLapFungsionalPemeriksa(HttpServletRequest request, HttpServletResponse response) {
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

        // Get Parameter filter (search) username
        String userName = null;
        try {
            userName = request.getParameter("mUser");
        } catch (Exception e) {
        }
        MUser mUsers = userService.selectOneById(userName);
        if(mUsers != null) {
            if(mUsers.getmEmployee() != null) {
                userName = mUsers.getmEmployee().getEmployeeName();
            } else {
                userName = "-";
            }
        } else {
            userName = "-";
        }

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
                    if((searchBy.equalsIgnoreCase("mAction.id") || searchBy.equalsIgnoreCase("createdBy.id") || searchBy.equalsIgnoreCase("mStatus.id")) && value != null) {
                        if (StringUtils.isNotBlank(value)) {
                            searchCriteria.add(new KeyValue(searchBy, value, true));
                        }
                    } else if (searchBy.equalsIgnoreCase("startDate") || searchBy.equalsIgnoreCase("endDate")) {
                        if (StringUtils.isNotBlank(value)) {
                            try {
                                searchCriteria.add(new KeyValue(searchBy, DateUtil.toDate("dd/MM/yyyy", value), true));
                            } catch (ParseException e) {
                            }
                        }
                    } else {
                        if (StringUtils.isNotBlank(value)) {
                            searchCriteria.add(new KeyValue(searchBy, value, true));
                        }
                    }
                }
            }
        }

//        searchCriteria.add(new KeyValue("statusOnOff", StatusEnum.IPT_DRAFT.name(), true));

        try {
            reportInputStream = getClass().getClassLoader().getResourceAsStream("report/laporan-pasca-permohonan.xls");
            List<LapPascaPermohonan> dataList = new ArrayList<>();
            int retrieved = 0;
            GenericSearchWrapper<TxMonitor> searchResult = monitorService.searchMonitorPascaPermohonan(searchCriteria, "createdDate", orderType, retrieved, 1000);
            LapPascaPermohonan dataLapPasca = null;
            int totalRecord = (int) searchResult.getCount();

            int no = 0;
            if (totalRecord > 0) {
            	
                List<TxMonitor> retrievedMonitorList = searchResult.getList();
                
                for (TxMonitor result : retrievedMonitorList){
                	
                    no++;
                    String brandName = "-";
                    String regNo = "-";
                    String username = "-";

                    try{
                        username = result.getCreatedBy().getUsername();
                    } catch (NullPointerException e){

                    }

                    try {
                        regNo = result.getTxTmGeneral().getTxRegistration().getNo();
                    } catch (NullPointerException e) {
                    }


                    try {
                        brandName = result.getTxTmGeneral().getTxTmBrand().getName();
                    } catch (NullPointerException e) {
                    }


                    String data_applicationNo = result.getTxTmGeneral().getApplicationNo();
                    String data_process_date = result.getCreatedDate().toString();
                    String data_post_date = "-";
                    String data_post_no = "-";
                    if(result.getTxPostReception() != null) {
                        data_post_date = result.getTxPostReception().getPostDate().toString();
                        data_post_no = result.getTxPostReception().getPostNo();
                    }

                    dataLapPasca = new LapPascaPermohonan();
                    dataLapPasca.setNo(no);
                    dataLapPasca.setPostAppDate(data_post_date);
                    dataLapPasca.setProcessDate(data_process_date);
                    dataLapPasca.setPostNo(data_post_no);
                    dataLapPasca.setApplicationNo(data_applicationNo);
                    dataLapPasca.setRegNo(regNo);
                    dataLapPasca.setBrandName(brandName);
                    dataLapPasca.setUsername(username);
                    dataList.add(dataLapPasca);
                }
            }

            Context context = new Context();
            context.putVar("dataList", dataList);

            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=laporan-pasca-permohonan.xls");

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