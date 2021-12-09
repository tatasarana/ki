package com.docotel.ki.controller.laporan;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.MLaw;
import com.docotel.ki.model.transaction.TxGroupDetail;
import com.docotel.ki.model.transaction.TxPubsJournal;
import com.docotel.ki.model.transaction.TxTmClass;
import com.docotel.ki.pojo.*;
import com.docotel.ki.repository.custom.transaction.TxPubsJournalCustomRepository;
import com.docotel.ki.repository.transaction.TxPubsJournalRepository;
import com.docotel.ki.service.master.LawService;
import com.docotel.ki.service.master.UserService;
import com.docotel.ki.service.transaction.TrademarkService;
import com.docotel.ki.util.DateUtil;
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
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class RekapBrmController extends BaseController {
    public static final String PATH_AJAX_SEARCH_LIST = "/cari-laporan-rekap-brm";
    public static final String PATH_LAPORAN_REKAP_BRM = "/laporan-rekap-brm";
    private static final String PATH_EXPORT_DATA_LAPORAN_REKAP_BRM = "/cetak-laporan-rekap-brm";
    private static final String PATH_EXPORT_DATA_LAPORAN_REKAP_BRM_DETAIL = "/cetak-laporan-rekap-brm-detail";
    public static final String REDIRECT_LAPORAN_REKAP_BRM = "redirect:" + PATH_AFTER_LOGIN + PATH_LAPORAN_REKAP_BRM;
    private static final String DIRECTORY_PAGE = "laporan/";
    private static final String PAGE_PUBLIKASI = DIRECTORY_PAGE + "laporan-rekap-brm";
    private static final String REQUEST_MAPPING_AJAX_SEARCH_LIST = PATH_AJAX_SEARCH_LIST + "*";
    private static final String REQUEST_MAPPING_LAPORAN_REKAP_BRM = PATH_LAPORAN_REKAP_BRM + "*"; 
    private static final String REQUEST_EXPORT_LAPORAN_REKAP_BRM = PATH_EXPORT_DATA_LAPORAN_REKAP_BRM + "*";
    private static final String REQUEST_EXPORT_LAPORAN_REKAP_BRM_DETAIL = PATH_EXPORT_DATA_LAPORAN_REKAP_BRM_DETAIL + "*";
    public static final Locale LOCALE_ID = new Locale("in", "ID");
    
    @Autowired
    private TrademarkService trademarkService;
    @Autowired
    private LawService lawService;
    @Autowired
    private TxPubsJournalRepository txPubsJournalRepository;
    @Autowired
    private TxPubsJournalCustomRepository txPubsJournalCustomRepository;
    @Autowired
    private UserService userService;
    
    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request, final HttpServletResponse response) {
        model.addAttribute("menu", "laporan");
        model.addAttribute("subMenu", "laporanRekapBrm");
    }

    public static <T> void removeDuplicate(List <T> list) {
	    HashSet <T> h = new HashSet<T>(list);
	    list.clear();
	    list.addAll(h);
	}
    
    @GetMapping(path = REQUEST_MAPPING_LAPORAN_REKAP_BRM)
    public String doShowPagePublikasi(@RequestParam(value = "error", required = false) String error, final Model model, final HttpServletRequest request, final HttpServletResponse response) {
        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("errorMessage", error);
        }

        model.addAttribute("listMLaw", lawService.findAll());
        
        List<TxPubsJournal> listTxPubsJournal = txPubsJournalRepository.findAll();
        ArrayList<String> journalStartYear = new ArrayList<>();
        for(TxPubsJournal pubsJournalList : listTxPubsJournal) {
        	SimpleDateFormat formatYear = new SimpleDateFormat("YYYY", LOCALE_ID);
            String years = formatYear.format(pubsJournalList.getJournalStart());
            journalStartYear.add(years);
        }
        removeDuplicate(journalStartYear);
        
        model.addAttribute("journalStartYear", journalStartYear);
        
        List<Map<String , Object>> journalStartMonthList =  new ArrayList<>(listTxPubsJournal.size());
        for(TxPubsJournal pubsJournalList : listTxPubsJournal) {
        	Map<String, Object> journalStartMonthMap = new HashMap();
        	SimpleDateFormat formatMonth = new SimpleDateFormat("MMMM", LOCALE_ID);
            String months = formatMonth.format(pubsJournalList.getJournalStart());
            journalStartMonthMap.put("date", pubsJournalList.getJournalStartTemp());
            journalStartMonthMap.put("monthName", months);
            journalStartMonthList.add(journalStartMonthMap);
        }
        
        model.addAttribute("journalStartMonth", journalStartMonthList);
        
        List<UserDetails> mUser = userService.selectListUserByUserType("Karyawan");
		model.addAttribute("mUser", mUser);

        return PAGE_PUBLIKASI;
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
                        if (searchBy.equalsIgnoreCase("journalStartYear") && !(value.isEmpty())) {
                        	if (StringUtils.isNotBlank(value)) {
                                searchCriteria.add(new KeyValue(searchBy, value, true));
                        	} else {
                        		searchCriteria.add(new KeyValue(searchBy, value, false));
                        	}
                        } else if (searchBy.equalsIgnoreCase("journalStartMonth") && !(value.isEmpty())) {
                        	if (StringUtils.isNotBlank(value)) {
                        	try {
                            	String [] dateParts = value.split("/");
                            	String month = dateParts[1];
                                searchCriteria.add(new KeyValue(searchBy, month, true));
                            } catch (Exception e) {
                            }
                        	} else {
                        		searchCriteria.add(new KeyValue(searchBy, value, false));
                        	}
                        } else if ((searchBy.equalsIgnoreCase("mLaw.id") || searchBy.equalsIgnoreCase("createdBy.id")) && !(value.isEmpty())) {
                        	if (StringUtils.isNotBlank(value)) {
                        	try {
                                searchCriteria.add(new KeyValue(searchBy, value, true));
                            } catch (Exception e) {
                            }
                        	} else {
                        		searchCriteria.add(new KeyValue(searchBy, value, false));
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
                        case "2" :
                            orderBy = "journalStart";
                            break;
                        case "3" :
                            orderBy = "journalNo";
                            break;
                        case "4" :
                            orderBy = "journalStart";
                            break;
                        case "5" :
                            orderBy = "journalEnd";
                            break;
                        case "6" :
                            orderBy = "mLaw.desc";
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
            GenericSearchWrapper<TxPubsJournal> searchResult = trademarkService.searchLapBRM(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());
                int no = offset;
                for (TxPubsJournal result : searchResult.getList()) {
                	
                    SimpleDateFormat formatMonth = new SimpleDateFormat("MMMM", LOCALE_ID);
                   	String monthName = formatMonth.format(result.getJournalStart());
                   	long jumlahDetailBrm = txPubsJournalRepository.countTxGroupDetailByPubsId(result.getId());
                    no++;
                    data.add(new String[]{
                    		result.getId(),
                            "" + no,
                            monthName,
                            result.getJournalNo(),
                            result.getJournalStartTemp(),
                            result.getJournalEndTemp(),
                            result.getmLaw().getDesc(),
                            String.valueOf(jumlahDetailBrm),
                            "<a class=\"btn btn-success\" href=\"javascript:void(0)\" id=\"exportDetailToExcel\"><i class=\"fas fa-file-excel\"></i> Export Detail</a>"
                    });
                }
            }
            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }
    
    @GetMapping(REQUEST_EXPORT_LAPORAN_REKAP_BRM)
    public void doExportLapRekapBrm(HttpServletRequest request, HttpServletResponse response) {
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
       
        // Get Parameter filter (search) journalStart (Year)
		String journalStartYear = null;
        try {
        	journalStartYear = request.getParameter("journalStartYear");
        } catch (Exception e) {
        }
        
        // Get Parameter filter (search) lawDesc
        String lawDesc = null;
        try {
        	lawDesc = request.getParameter("lawDesc");
        } catch (Exception e) {
        }
        MLaw mLaw = lawService.selectOne("id", lawDesc);
        if(mLaw != null) {
        	lawDesc = mLaw.getDesc();
        } else {
        	lawDesc = "-";
        }
        
        String orderBy = "3";
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
                	if (searchBy.equalsIgnoreCase("journalStartYear") && value != null) {
                		if (StringUtils.isNotBlank(value)) {
                            searchCriteria.add(new KeyValue(searchBy, value, true));
                        } 
                    } else if (searchBy.equalsIgnoreCase("journalStartMonth") && value != null) {
                    	if (StringUtils.isNotBlank(value)) {
                        	String [] dateParts = value.split("/");
                        	String month = dateParts[1];
                            searchCriteria.add(new KeyValue(searchBy, month, true));
                        } 
                    } else if ((searchBy.equalsIgnoreCase("mLaw.id") || searchBy.equalsIgnoreCase("createdBy.id")) && value != null) {
                    	if (StringUtils.isNotBlank(value)) {
                            searchCriteria.add(new KeyValue(searchBy, value, true));
                        } 
                    } else {
                        if (StringUtils.isNotBlank(value)) {
                            searchCriteria.add(new KeyValue(searchBy, value, false));
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
            	case "1" :
                    orderBy = "journalStart";
                    break;
                case "2" :
                    orderBy = "journalNo";
                    break;
                case "3" :
                    orderBy = "journalStart";
                    break;
                case "4" :
                    orderBy = "journalEnd";
                    break;
                case "5" :
                    orderBy = "mLaw.desc";
                    break;
                default:
                    orderBy = "journalStart";
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
            reportInputStream = getClass().getClassLoader().getResourceAsStream("report/laporan-rekap-brm.xls");
            List<LapDataRekapBRM> dataList = new ArrayList<>();
            int totalRecord = (int) trademarkService.countLaporanBRM(searchCriteria);
            int retrieved = 0;
            LapDataRekapBRM dataRekapBrm = null;
            
            String downloadDate = DateUtil.formatDate(new Timestamp(System.currentTimeMillis()), "dd MMMM YYYY");
            
            int no = 0;
            if (totalRecord > 0) {
                List<TxPubsJournal> retrievedDataList = trademarkService.selectAllLaporanBRM(searchCriteria, orderBy, orderType, retrieved, 1000);
                
                for (TxPubsJournal result : retrievedDataList) {
                    no++;
                    String month = "-";
                    String journalNo = "-";
                    String publicationDate = "-";
                    String endDate = "-";
                    String total = "-";
                   	
                    try {
                    	SimpleDateFormat formatMonth = new SimpleDateFormat("MMMM", LOCALE_ID);
                       	String monthName = formatMonth.format(result.getJournalStart());
                        month = monthName;
                    } catch (NullPointerException e) {
                    }
                    
                    try {
                    	journalNo = result.getJournalNo();
                    } catch(Exception e) {
                    	
                    }
                    
                    try {
                    	publicationDate = result.getJournalStartTemp();
                    } catch(Exception e) {
                    	
                    }
                    
                    try {
                    	endDate = result.getJournalEndTemp();
                    } catch(Exception e) {
                    	
                    }
                    
                    try {
                    	long jumlahDetailBrm = txPubsJournalRepository.countTxGroupDetailByPubsId(result.getId());
                    	total = String.valueOf(jumlahDetailBrm);
                    } catch(Exception e) {
                    	
                    }

                    dataRekapBrm = new LapDataRekapBRM();
                    dataRekapBrm.setNo(no);
                    dataRekapBrm.setMonth(month);
                    dataRekapBrm.setJournalNo(journalNo);
                    dataRekapBrm.setPublicationDate(publicationDate);
                    dataRekapBrm.setEndDate(endDate);
                    dataRekapBrm.setTotal(total);
                    dataList.add(dataRekapBrm);
                }
                retrieved += retrievedDataList.size();
            }

            Context context = new Context();
            context.putVar("dataList", dataList);
            context.putVar("journalStartYear", journalStartYear);
            context.putVar("lawDesc", lawDesc);
            context.putVar("downloadDate", downloadDate);

            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=laporan-rekap-brm.xls");

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
    
    @GetMapping(REQUEST_EXPORT_LAPORAN_REKAP_BRM_DETAIL)
    public void doExportLapRekapBrmDetail(HttpServletRequest request, HttpServletResponse response) {
        InputStream reportInputStream = null;

        String rowId = null;
        try {
        	rowId = request.getParameter("rowId");
        } catch (Exception e) {
        }
		
        List<KeyValue> searchCriteria = new ArrayList<>();
        
        TxPubsJournal existing = trademarkService.selectOnePubJournalById(rowId);
        searchCriteria.add(new KeyValue("txGroup.id", existing.getTxGroup().getId(), false));

        try {
            reportInputStream = getClass().getClassLoader().getResourceAsStream("report/laporan-rekap-brm-detail.xls");
            List<LapDataRekapBRMDetail> dataList = new ArrayList<>();
            int totalRecord = (int) trademarkService.countTxGroupDetail(searchCriteria);
            int retrieved = 0;
            String journalNo = "-";
            String journalStart = "-";
            String journalEnd = "-";
            String filingDateStart = "-";
            String filingDateEnd = "-";
            TxGroupDetail minDate = new TxGroupDetail();
            TxGroupDetail maxDate = new TxGroupDetail();
            LapDataRekapBRMDetail dataRekapBrmDetail = null;
            
            int no = 0;
            if (totalRecord > 0) {
                List<TxGroupDetail> retrievedDataList = trademarkService.selectAllTxGroupDetail(searchCriteria, "txTmGeneral.filingDate", "ASC", retrieved, 100000);
                minDate = retrievedDataList.get(0);
                maxDate = retrievedDataList.get(retrievedDataList.size() - 1);
                
                filingDateStart = DateUtil.formatDate(minDate.getTxTmGeneral().getFilingDate(), "dd/MM/yyyy");
                filingDateEnd = DateUtil.formatDate(maxDate.getTxTmGeneral().getFilingDate(), "dd/MM/yyyy");
                
                for (TxGroupDetail result : retrievedDataList) {
                    no++;
                    String brandType = "-";
                    String brandName = "-";
                    String sbClassList = "-";
                    ArrayList<String> kelas = new ArrayList<String>();
                    journalNo = result.getTxGroup().getTxPubsJournal().getJournalNo();
                    journalStart = result.getTxGroup().getTxPubsJournal().getJournalStartTemp();
                    journalEnd = result.getTxGroup().getTxPubsJournal().getJournalEndTemp();
                    
                    try {
                        brandName = result.getTxTmGeneral().getTxTmBrand().getName();
                    } catch (NullPointerException e) {
                    }
                    
                    try {
                    	brandType = result.getTxTmGeneral().getTxTmBrand().getmBrandType().getName();
                    } catch(Exception e) {
                    	
                    }
                    
                    for (TxTmClass txTmClass : result.getTxTmGeneral().getTxTmClassList()) {
                        kelas.add("" + txTmClass.getmClass().getNo());
                    }
                    Set<String> temp = new LinkedHashSet<String>(kelas);
                    String[] unique = temp.toArray(new String[temp.size()]);
                    if (unique.length > 0) {
                        sbClassList = String.join(",", unique);
                    }

                    dataRekapBrmDetail = new LapDataRekapBRMDetail();
                    dataRekapBrmDetail.setNo(no);
                    dataRekapBrmDetail.setApplicationNo(result.getTxTmGeneral().getApplicationNo());
                    dataRekapBrmDetail.setFilingDate(result.getTxTmGeneral().getFilingDateTemp());
                    dataRekapBrmDetail.setKelas(sbClassList);
                    dataRekapBrmDetail.setBrandType(brandType);
                    dataRekapBrmDetail.setBrandName(brandName);
                    dataList.add(dataRekapBrmDetail);
                }
                retrieved += retrievedDataList.size();
            }

            Context context = new Context();
            context.putVar("dataList", dataList);
            context.putVar("journalNo", journalNo);
            context.putVar("journalStart", journalStart);
            context.putVar("journalEnd", journalEnd);
            context.putVar("filingDateStart", filingDateStart);
            context.putVar("filingDateEnd", filingDateEnd);

            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=laporan-rekap-brm-detail.xls");

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

    /***************************** END PUBLIKASI DETAIL SECTION - ****************************/
}
