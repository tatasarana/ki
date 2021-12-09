package com.docotel.ki.controller.laporan;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.MAction;
import com.docotel.ki.model.master.MStatus;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.pojo.LapKbm;
import com.docotel.ki.repository.custom.transaction.TxTmReprsCustomRepository;
import com.docotel.ki.repository.transaction.TxPostRepresentativeRepository;
import com.docotel.ki.repository.transaction.TxPostOwnerRepository;
import com.docotel.ki.repository.transaction.TxPubsJournalRepository;
import com.docotel.ki.service.master.GroupService;
import com.docotel.ki.service.master.MActionService;
import com.docotel.ki.service.master.StatusService;
import com.docotel.ki.service.master.UserService;
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
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class LaporanKBMController extends BaseController {

    public static final String PATH_LAPORAN_KBM = "/laporan-kbm";
    public static final String PATH_AJAX_SEARCH_LIST = "/cari-kbm";
    private static final String PATH_EXPORT_DATA_LAPORAN_KBM = "/cetak-kbm";
    private static final String DIRECTORY_PAGE = "laporan/";
    private static final String PAGE_LAPORAN_KBM = DIRECTORY_PAGE + "laporan-kbm";
    private static final String REQUEST_MAPPING_AJAX_SEARCH_LIST = PATH_AJAX_SEARCH_LIST + "*";
    private static final String REQUEST_MAPPING_LAPORAN_KBM = PATH_LAPORAN_KBM + "*";
    private static final String REQUEST_EXPORT_LAPORAN_KBM = PATH_EXPORT_DATA_LAPORAN_KBM + "*";

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
    private MonitorService monitorService ;
    @Autowired
    private TxPostRepresentativeRepository txPostRepresentativeRepository;
    @Autowired
    private TxPostOwnerRepository txPostOwnerRepository;

    /* --------------------------------------- PERMOHONAN SECTION ---------------------------------------------------*/

    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "laporan");
        model.addAttribute("subMenu", "laporanKbm");
    }

    /*
   rian@docotel.com , BUGFIX:https://gitlab.docotel.net/neviim/ki/merk/issues/220
    */
    @RequestMapping(path = REQUEST_MAPPING_LAPORAN_KBM)
    public String doShowPageLapKbm(@RequestParam(value = "error", required = false) String error, Model model, final HttpServletRequest request, final HttpServletResponse response) {
        List<UserDetails> mUser = userService.selectListUserByUserType("Karyawan");
        model.addAttribute("mUser", mUser);
        Collections.sort(mUser, (o1, o2) -> o1.getUsername().compareTo(o2.getUsername()));

        List<MStatus> statusList = statusService.selectStatus();
        model.addAttribute("statusList", statusList);

        List<MAction> actionList = mActionService.findAll();
        model.addAttribute("actionList", actionList);

        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("errorMessage", error);
        }

        return PAGE_LAPORAN_KBM;
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
                        if((searchBy.equalsIgnoreCase("mAction.id") || searchBy.equalsIgnoreCase("createdBy.id") || searchBy.equalsIgnoreCase("mStatus.id")) && !(value.isEmpty())) {
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

            List<String[]> data = new ArrayList<>();
            String orderType = request.getParameter("order[0][dir]");
            if (orderType == null) {
                orderType = "ASC";
            } else {
                orderType = orderType.trim();
                if (!orderType.equalsIgnoreCase("ASC")) {
                    orderType = "DESC";
                }
            }

            GenericSearchWrapper<TxMonitor> searchResult = monitorService.searchMonitorKbm(searchCriteria, "createdDate", orderType, offset, limit);
            
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
                    String sbClassList = "-";
                    String brmNo = "-";
                    String Owner="-";
                    String Reprs="-";
                    ArrayList<String> kelass = new ArrayList<String>();
                    ArrayList<String> OwnerList = new ArrayList<String>();
                    ArrayList<String> ReprsList = new ArrayList<String>();

                    try {
                        brandName = result.getTxTmGeneral().getTxTmBrand().getName();
                    } catch (NullPointerException e) {
                    }

                    try {
                        status = result.getTxTmGeneral().getmStatus().getName();
                    } catch (NullPointerException e) {

                    }

                    try {
                        aksi = result.getTxTmGeneral().getmAction().getName();
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

                    try {
                        for (TxTmOwner txTmOwner : result.getTxTmGeneral().getTxTmOwner()) {
                            OwnerList.add("" + txTmOwner.getName());
                        }
                        Set<String> temp = new LinkedHashSet<String>(OwnerList);
                        String[] unique = temp.toArray(new String[temp.size()]);
                        if (unique.length > 0) {
                            Owner = String.join(",", unique);
                        }

                    } catch (NullPointerException e) {
                    }

                    try {
                    	System.out.println("TxPostReception : "+result.getTxPostReception());
                    	if (result.getTxPostReception() != null) {                    		
                    		TxPostRepresentative txPostRepresentative = txPostRepresentativeRepository.findTxPostRepresentativeByTxPostReception(result.getTxPostReception());                            
                            Reprs = txPostRepresentative.getmRepresentative().getName();
                    	}
                        

                    } catch (NullPointerException e) {
                    }

//                    try {
//
//                        for (TxTmRepresentative txTmReprs : result.getTxTmGeneral().getTxTmRepresentative()) {
//                            ReprsList.add("" + txTmReprs.getName());
//                        }
//                        Set<String> temp = new LinkedHashSet<String>(ReprsList);
//                        String[] unique = temp.toArray(new String[temp.size()]);
//                        if (unique.length > 0) {
//                            Reprs = String.join(",", unique);
//                        }
//
//                    } catch (NullPointerException e) {
//                    }


                    String data_applicationNo = result.getTxTmGeneral().getApplicationNo();
                    String data_process_date = result.getCreatedDate().toString();

                    // if (result.getTxTmGeneral().getmStatus() != null) {
                    if (status != null) {

                        data.add(new String[]{
                                "" + no,
                                data_applicationNo, // Nomor Permohonan
                                data_process_date,
                                sbClassList,
                                brandName,
                                Owner,
                                Reprs,
                                status,
                                aksi

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

    @GetMapping(REQUEST_EXPORT_LAPORAN_KBM)
    public void doExportLapKbm(HttpServletRequest request, HttpServletResponse response) {
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
//                    if((searchBy.equalsIgnoreCase("mAction.id") || searchBy.equalsIgnoreCase("createdBy.id") || searchBy.equalsIgnoreCase("mStatus.id")) && !(value.isEmpty())) {
//                        if (StringUtils.isNotBlank(value)) {
//                            searchCriteria.add(new KeyValue(searchBy, value, true));
//                        }
//                    } else
                        if (searchBy.equalsIgnoreCase("startDate") || searchBy.equalsIgnoreCase("endDate")) {
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
            reportInputStream = getClass().getClassLoader().getResourceAsStream("report/laporan-kbm.xls");
            List<LapKbm> dataList = new ArrayList<>();
            int retrieved = 0;
            String orderType = request.getParameter("order[0][dir]");
            if (orderType == null) {
                orderType = "ASC";
            } else {
                orderType = orderType.trim();
                if (!orderType.equalsIgnoreCase("ASC")) {
                    orderType = "DESC";
                }
            }

            GenericSearchWrapper<TxMonitor> searchResult = monitorService.searchMonitorKbm(searchCriteria, "createdDate", orderType, retrieved, 1000);
            LapKbm dataKbm = null;
            int totalRecord = (int) searchResult.getCount();

            int no = 0;
            if (totalRecord > 0) {
                List<TxMonitor> retrievedMonitorList = searchResult.getList();
                List<TxTmGeneral> retrievedDataList = new ArrayList<>();

                for (TxMonitor result : retrievedMonitorList){
//                    if(result!= null){

                        no++;
                        String brandName = "-";
                        String catatan = "-";
                        String status = "-";
                        String aksi = "-";
                        String sbClassList = "-";
                        String tmOwnerName = "-";
                        String tmOwnerAddress = "-";
                        String reprsName = "-";
                        String reprsAddress = "-";
                        String tglPermohonanBanding = "-";
                        String noDokPermohonanBanding = "-";

                    String tglPenerimaanBanding = "-";
                        ArrayList<String> kelas = new ArrayList<String>();

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
                            Map<String, String> ownerMap = new HashMap<>();
                            StringBuffer sbOwnerList = new StringBuffer();
                            for (TxTmOwner txTmOwner : result.getTxTmGeneral().getTxTmOwner()) {
                                if (txTmOwner.isStatus() == true) {
                                    ownerMap.put("" + txTmOwner.getName(), "" + txTmOwner.getName());
                                }
                            }
                            for (Map.Entry<String, String> map : ownerMap.entrySet()) {
                                sbOwnerList.append(map.getKey());
                                sbOwnerList.append(", ");
                            }
                            if (sbOwnerList.length() > 0) {
                                tmOwnerName = sbOwnerList.substring(0, sbOwnerList.length() - 2);
                            }
                        } catch (NullPointerException e) {
                        }

                        try {
                            Map<String, String> ownerMap = new HashMap<>();
                            StringBuffer sbOwnerList = new StringBuffer();
                            for (TxTmOwner txTmOwner : result.getTxTmGeneral().getTxTmOwner()) {
                                if (txTmOwner.isStatus() == true) {
                                    ownerMap.put("" + txTmOwner.getAddress(), "" + txTmOwner.getAddress());
                                }
                            }
                            for (Map.Entry<String, String> map : ownerMap.entrySet()) {
                                sbOwnerList.append(map.getKey());
                                sbOwnerList.append(", ");
                            }
                            if (sbOwnerList.length() > 0) {
                                tmOwnerAddress = sbOwnerList.substring(0, sbOwnerList.length() - 2);
                            }
                        } catch (NullPointerException e) {
                        }


                    try {
                            TxPostRepresentative txPostRepresentative = txPostRepresentativeRepository.findTxPostRepresentativeByTxPostReception(result.getTxPostReception());

                            reprsName = txPostRepresentative.getmRepresentative().getName();
                            reprsAddress = txPostRepresentative.getmRepresentative().getAddress();


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

                    try {
                        catatan = result.getNotes();
                    } catch (NullPointerException e) {
                    }

                        try{
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                            tglPenerimaanBanding  = dateFormat.format(result.getCreatedDate());
                        }catch (NullPointerException e){

                        }

                        try{
                            TxMonitor txMonitor = monitorService.findTxMonitorKBM03(result.getTxTmGeneral().getId());
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                            tglPermohonanBanding  = dateFormat.format(txMonitor.getCreatedDate());
                            noDokPermohonanBanding  = txMonitor.getTxPostReception().getPostNo();
                        }catch (NullPointerException e){

                        }

                        dataKbm = new LapKbm();
                        dataKbm.setNo(no);
                        dataKbm.setApplicationNo(result.getTxTmGeneral().getApplicationNo());
                        dataKbm.setFillingDate(result.getTxTmGeneral().getFilingDateTemp());
                        dataKbm.setKelas(sbClassList);
                        dataKbm.setBrandName(brandName);
                        dataKbm.setStatus(status);
                        dataKbm.setAksi(aksi);
                        dataKbm.setTmOwnerName(tmOwnerName);
                        dataKbm.setTmOwnerAddress(tmOwnerAddress);
                        dataKbm.setNotes(catatan);
                        dataKbm.setTglPermohonanBanding(tglPermohonanBanding);
                        dataKbm.setNoDokPermohonanBanding(noDokPermohonanBanding);
                        dataKbm.setTglPenerimaanBanding(tglPenerimaanBanding);
                        dataKbm.setReprsName(reprsName);
                        dataKbm.setReprsAddress(reprsAddress);
                        dataList.add(dataKbm);

                        /*TxTmGeneral txTmGeneral = new TxTmGeneral();
                        txTmGeneral = txMonitor.getTxTmGeneral();
                        retrievedDataList.add(txTmGeneral);*/
//                    }
                }

                /*for (TxTmGeneral result : retrievedDataList) {

                }*/
                retrieved += retrievedMonitorList.size();
            }

            String currentDate = DateUtil.formatDate(new Timestamp(System.currentTimeMillis()), "dd MMMM YYYY");


            Context context = new Context();
            context.putVar("dataList", dataList);
            context.putVar("employeeName", userName);
            context.putVar("currentDate", currentDate);

            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=laporan-kbm.xls");

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