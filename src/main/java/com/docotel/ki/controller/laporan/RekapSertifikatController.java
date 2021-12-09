package com.docotel.ki.controller.laporan;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.MAction;
import com.docotel.ki.model.master.MCountry;
import com.docotel.ki.model.master.MStatus;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.transaction.TxMonitor;
import com.docotel.ki.model.transaction.TxTmClass;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.model.transaction.TxTmOwner;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.pojo.LapDataRekapSertifikat;
import com.docotel.ki.service.master.CountryService;
import com.docotel.ki.service.master.MActionService;
import com.docotel.ki.service.master.StatusService;
import com.docotel.ki.service.master.UserService;
import com.docotel.ki.service.transaction.OwnerService;
import com.docotel.ki.service.transaction.PermohonanService;
import com.docotel.ki.service.transaction.TrademarkService;
import com.docotel.ki.service.transaction.MonitorService;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.enumeration.StatusEnum;
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
import java.text.ParseException;
import java.util.*;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class RekapSertifikatController extends BaseController {
    public static final String PATH_LAPORAN_REKAP_SERTIFIKAT = "/laporan-rekap-sertifikat";
    public static final String PATH_AJAX_SEARCH_LIST = "/cari-rekap-sertifikat";
    public static final String PATH_PRANTINJAU_PERMOHONAN = "/pratinjau-permohonan";
    private static final String PATH_EXPORT_DATA_LAPORAN_REKAP_SERTIFIKAT = "/cetak-rekap-sertifikat";
    private static final String DIRECTORY_PAGE = "laporan/";
    private static final String PAGE_LAPORAN_REKAP_SERTIFIKAT = DIRECTORY_PAGE + "laporan-rekap-sertifikat";
    private static final String REQUEST_MAPPING_AJAX_SEARCH_LIST = PATH_AJAX_SEARCH_LIST + "*";
    private static final String REQUEST_MAPPING_LAPORAN_REKAP_SERTIFIKAT = PATH_LAPORAN_REKAP_SERTIFIKAT + "*";
    private static final String REQUEST_EXPORT_LAPORAN_REKAP_SERTIFIKAT = PATH_EXPORT_DATA_LAPORAN_REKAP_SERTIFIKAT + "*";

    @Autowired
    private StatusService statusService;
    @Autowired
    private MActionService mActionService;
    @Autowired
    private UserService userService;
    @Autowired
    private CountryService negaraService;
    @Autowired
    private TrademarkService trademarkService;
    @Autowired
    private OwnerService ownerService;
    @Autowired
    private PermohonanService permohonanService;
    @Autowired
    private MonitorService monitorService ;

    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request){
        model.addAttribute("menu", "laporan");
        model.addAttribute("subMenu","rekapSertifikat");
    }

    @RequestMapping(path = REQUEST_MAPPING_LAPORAN_REKAP_SERTIFIKAT)
    public String doShowPageLapRekapSertifikat(@RequestParam(value = "error", required = false) String error, Model model, final HttpServletRequest request, final HttpServletResponse response){
        List<UserDetails> mUsers = userService.selectListUserByUserType("Karyawan");
        model.addAttribute("mUser", mUsers);
        Collections.sort(mUsers, (o1, o2) -> o1.getUsername().compareTo(o2.getUsername()));

        List<MStatus> mStatusList = statusService.selectStatus();
        model.addAttribute("mStatusList", mStatusList);

        List<MAction> actionList = mActionService.findAll();
        model.addAttribute("actionList", actionList);

        List<MCountry> contryList = negaraService.findByStatusFlagTrue();
        model.addAttribute("contryList", contryList);

        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("error", error);
        }

        return PAGE_LAPORAN_REKAP_SERTIFIKAT;
    }

    @RequestMapping(value = REQUEST_MAPPING_AJAX_SEARCH_LIST, method = {RequestMethod.POST})
    public void doSearchDataTableList(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            DataTablesSearchResult dataTablesSearchResult = new DataTablesSearchResult();
            try {
                dataTablesSearchResult.setDraw(Integer.parseInt(request.getParameter("draw")));
            }catch (NumberFormatException e) {
                dataTablesSearchResult.setDraw(0);
            }

            int offset = 0;
            int limit = 50;
            try {
                offset = Math.abs(Integer.parseInt(request.getParameter("start")));
            }catch (NumberFormatException e) {}

            try {
                limit = Math.abs(Integer.parseInt(request.getParameter("length")));
            }catch (NumberFormatException e) {}

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
                    }catch (ArrayIndexOutOfBoundsException e){}

                    if (searchBy != null && !searchBy.equalsIgnoreCase(""))
                    {
                        if ((searchBy.equalsIgnoreCase("mAction.id") || searchBy.equalsIgnoreCase("mStatus.id") || searchBy.equalsIgnoreCase("createdBy.id") || searchBy.equalsIgnoreCase("txTmOwner.mCountry.id")) && (!value.isEmpty()))
                        {
                            if (StringUtils.isNotBlank(value)) {
                                searchCriteria.add(new KeyValue(searchBy, value, true));
                            }
                        } else if (searchBy.equalsIgnoreCase("startDate") || searchBy.equalsIgnoreCase("endDate")) {
                            if (StringUtils.isNotBlank(value)) {
                                try {
                                    searchCriteria.add(new KeyValue(searchBy, DateUtil.toDate("dd/MM/yyyy", value), true));
                                }catch (ParseException e) {}
                            }
                        } else {
                            if (StringUtils.isNotBlank(value)) {
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

            GenericSearchWrapper<TxMonitor> searchResult = monitorService.searchMonitorKeputusanDirektur(searchCriteria, "createdDate", orderType, offset, limit);

            if (searchResult.getCount() > 0)
            {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxMonitor result : searchResult.getList()) {
                    no++;
                    String brandName = "-";
                    String brandType = "-";
                    String sbClassList = "-";
                    String ownerNameList = "-";
                    String negara = "-";
                    String status = "-";
                    String aksi = "-";
                    ArrayList<String> kelass = new ArrayList<String>();
                    ArrayList<String> owners = new ArrayList<String>();
                    ArrayList<String> countries = new ArrayList<String>();

                    try {
                        brandName = result.getTxTmGeneral().getTxTmBrand().getName();
                    }catch (NullPointerException e) {}

                    try {
                        brandType = result.getTxTmGeneral().getTxTmBrand().getmBrandType().getName();
                    }catch (NullPointerException e) {}

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

                        try {
                        for (TxTmOwner txTmOwner : result.getTxTmGeneral().getTxTmOwner()) {
                            owners.add("" + txTmOwner.getName());
                            countries.add("" + txTmOwner.getmCountry().getCode());
                        }
                        Set<String> temp = new LinkedHashSet<String>(owners);
                        String[] unique = temp.toArray(new String[temp.size()]);
                        if (unique.length > 0) {
                            ownerNameList = String.join(",", unique);
                        }
                        temp = new LinkedHashSet<String>(countries);
                        unique = temp.toArray(new String[temp.size()]);
                        if (unique.length > 0) {
                            negara = String.join(",", unique);
                        }

                    } catch (NullPointerException e) {
                    }
                    String nomorregister;
                    try {
                        nomorregister = result.getTxTmGeneral().getTxRegistration() != null ? result.getTxTmGeneral().getTxRegistration().getNo() : "-";
                    }catch (NullPointerException e){
                        nomorregister = "-";
                    }

                    String data_applicationNo = result.getTxTmGeneral().getApplicationNo();
                    String tgl_proses = result.getCreatedDate().toString();

                    if (result.getTxTmGeneral().getmStatus() != null) {
                        data.add(new String[]{
                                "" + no,
                                result.getTxTmGeneral().getApplicationNo(),
                                tgl_proses,
                                sbClassList,
                                brandName,
                                nomorregister,
                                negara,
                                ownerNameList,
                                status,
                                aksi
                        });
                    }
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        }else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @GetMapping(REQUEST_EXPORT_LAPORAN_REKAP_SERTIFIKAT)
    public void doExportLapRekapSertifikat(HttpServletRequest request, HttpServletResponse response) {
        InputStream reportInputStream = null;

        String[] searchByArr = null;
        try {
            searchByArr = request.getParameter("searchByArr").split(",");
        }catch (Exception e) {}

        String[] keywordArr = null;
        try {
            keywordArr = request.getParameter("keywordArr").split(",");
        }catch (Exception e) {}

        String orderBy = request.getParameter("orderBy");
        String orderType = request.getParameter("orderType");

        String statusName = null;
        try {
            statusName = request.getParameter("statusName");
        }catch (Exception e){}
        MStatus mStatus = statusService.selectOneBy(statusName);
        statusName = mStatus != null ? mStatus.getName() : "-";

        String actionName = "-";
        try {
            actionName = request.getParameter("actionName");
        }catch (Exception e) {}
        MAction mAction = mActionService.selectOneBy(actionName);
        actionName = mAction != null ? mAction.getName() : "-";

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


        List<KeyValue> searchCriteria = new ArrayList<>();
        if (searchByArr != null)
        {
            for (int i = 0; i < searchByArr.length; i++)
            {
                String searchBy = searchByArr[i];
                String value = null;
                try {
                    value = keywordArr[i];
                }catch (ArrayIndexOutOfBoundsException e){}

                if ((searchBy.equalsIgnoreCase("mAction.id") || searchBy.equalsIgnoreCase("mStatus.id") || searchBy.equalsIgnoreCase("createdBy.id") || searchBy.equalsIgnoreCase("txTmGeneral.txTmOwner.mCountry.id")))
                {
                    if (StringUtils.isNotBlank(value)) {
                        searchCriteria.add(new KeyValue(searchBy, value, true));
                    }
                } else if (searchBy.equalsIgnoreCase("startDate") || searchBy.equalsIgnoreCase("endDate"))
                {
                    if (StringUtils.isNotBlank(value)) {
                        try {
                            searchCriteria.add(new KeyValue(searchBy, DateUtil.toDate("dd/MM/yyyy", value), true));
                        }catch (ParseException e) {}
                    }
                }else {
                    if (StringUtils.isNotBlank(value)) {
                        if (searchBy.equalsIgnoreCase("txTmClassList")) {
                            searchCriteria.add(new KeyValue(searchBy, value, true));
                        }else {
                            searchCriteria.add(new KeyValue(searchBy, value, false));
                        }
                    }
                }
            }
        }

        try
        {
            reportInputStream = getClass().getClassLoader().getResourceAsStream("report/laporan-sertifikat.xls");
            List<List> dataList = new ArrayList<>();
            List<LapDataRekapSertifikat> list = new ArrayList<>();
            int retrieved = 0;
            GenericSearchWrapper<TxMonitor> searchResult = monitorService.searchMonitorKeputusanDirektur(searchCriteria, "createdDate", orderType, retrieved, 1000000);
            int limit = 50;
            LapDataRekapSertifikat dataRekapSertifikat = null;

            int totalRecord = (int) searchResult.getCount();

//            int totalRecord = (int) trademarkService.countAll(searchCriteria);
//            int retrived = 0;

            String downloadDate = DateUtil.formatDate(new Timestamp(System.currentTimeMillis()), "dd MMMM YYYY");

            int no = 0;
            if (totalRecord > 0)
            {
                List<TxMonitor> retrievedMonitorList = searchResult.getList();
                List<TxTmGeneral> retrievedDataList = new ArrayList<>();

                for (TxMonitor txMonitor : retrievedMonitorList){
                    if(txMonitor!= null){
                        TxTmGeneral txTmGeneral = new TxTmGeneral();
                        txTmGeneral = txMonitor.getTxTmGeneral();
                        retrievedDataList.add(txTmGeneral);
                    }
                }

                for (TxTmGeneral result : retrievedDataList) {
                    no++;
                    String brandName = "-";
                    String brandType = "-";
                    String sbClassList = "-";
                    String ownerNameList = "-";
                    String negara = "-";
                    ArrayList<String> kelass = new ArrayList<String>();
                    ArrayList<String> owners = new ArrayList<String>();
                    ArrayList<String> countries = new ArrayList<String>();

                    try {
                        brandName = result.getTxTmBrand().getName();
                    }catch (NullPointerException e){}

                    try {
                        brandType = result.getTxTmBrand().getmBrandType().getName();
                    }catch (NullPointerException e) {}

                    try {
                        for (TxTmClass txTmClass : result.getTxTmClassList()) {
                            kelass.add("" + txTmClass.getmClass().getNo());
                        }
                        Set<String> tamp = new LinkedHashSet<String>(kelass);
                        String[] unique = tamp.toArray(new String[tamp.size()]);
                        if (unique.length > 0){
                            sbClassList = String.join(",", unique);
                        }
                    }catch (NullPointerException e) {}

                    try {
                        for (TxTmOwner txTmOwner : result.getTxTmOwner()){
                            if (txTmOwner.isStatus() == true) {
                                owners.add("" + txTmOwner.getName());
                                countries.add("" + txTmOwner.getmCountry().getCode());
                            }
                            Set<String> tamp = new LinkedHashSet<String>(owners);
                            String[] unique = tamp.toArray(new String[tamp.size()]);
                            if (unique.length > 0) {
                                ownerNameList = String.join(",", unique);
                            }
                            tamp = new LinkedHashSet<String>(countries);
                            unique = tamp.toArray(new String[tamp.size()]);
                            if (unique.length > 0) {
                                negara = String.join(",", unique);
                            }

                        }
                    }catch (NullPointerException e){}

                    String nomorregister;
                    try {
                        nomorregister = result.getTxRegistration() != null ? result.getTxRegistration().getNo() : "-";
                    }catch (Exception e) {
                        nomorregister = "-";
                    }

                    dataRekapSertifikat = new LapDataRekapSertifikat();
                    dataRekapSertifikat.setNo(no);
                    dataRekapSertifikat.setApplicationNo(result.getApplicationNo());
                    dataRekapSertifikat.setBranchName(brandName);
                    dataRekapSertifikat.setKelas(sbClassList);
                    dataRekapSertifikat.setRegistrationNo(nomorregister);
                    dataRekapSertifikat.setNegara(negara);
                    dataRekapSertifikat.setPemilik(ownerNameList);
                    list.add(dataRekapSertifikat);
                    if (list.size() == limit) {
                        dataList.add(list);
                        list = new ArrayList<>();
                    }
                }
                retrieved += retrievedDataList.size();
            }
            if (list.size() > 0) {
                dataList.add(list);
            }

            Context context = new Context();
            context.putVar("dataList", dataList);
            context.putVar("downloadDate", downloadDate);

            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=laporan-sertifikat.xls");

            JxlsHelper.getInstance().processTemplate(reportInputStream, response.getOutputStream(), context);
            response.getOutputStream().close();
            response.flushBuffer();
        }catch (Exception ex) {
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
