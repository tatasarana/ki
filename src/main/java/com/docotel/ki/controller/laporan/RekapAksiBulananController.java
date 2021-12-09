package com.docotel.ki.controller.laporan;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.*;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.pojo.LapDataRekapAksiBulanan;
import com.docotel.ki.service.master.*;
import com.docotel.ki.service.transaction.MonitorService;
import com.docotel.ki.model.master.*;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.pojo.*;
import com.docotel.ki.service.master.*;
import com.docotel.ki.service.transaction.*;

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
import java.io.*;
import java.util.*;

@Controller 
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class RekapAksiBulananController extends BaseController {
    public static final String PATH_LAPORAN_REKAP_AKSI_BULANAN = "/laporan-rekap-aksi-bulanan";
    public static final String PATH_AJAX_SEARCH_LIST = "/cari-rekap-aksi-bulanan";
    private static final String PATH_EXPORT_DATA_LAPORAN_REKAP_AKSI_BULANAN = "/cetak-rekap-aksi-bulanan";
    private static final String DIRECTORY_PAGE = "laporan/";
    private static final String PAGE_LAPORAN_REKAP_AKSI_BULANAN = DIRECTORY_PAGE + "laporan-rekap-aksi-bulanan";
    private static final String REQUEST_MAPPING_AJAX_SEARCH_LIST = PATH_AJAX_SEARCH_LIST + "*";
    private static final String REQUEST_MAPPING_LAPORAN_REKAP_AKSI_BULANAN = PATH_LAPORAN_REKAP_AKSI_BULANAN + "*";
    private static final String REQUEST_EXPORT_LAPORAN_REKAP_AKSI_BULANAN = PATH_EXPORT_DATA_LAPORAN_REKAP_AKSI_BULANAN + "*";
    public static final Locale LOCALE_ID = new Locale("in", "ID");
    
    @Autowired
    private MActionService mActionService;
    @Autowired
    private UserService userService;
    @Autowired
    private FileService fileService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private ClassService classService;
    @Autowired
    private MonitorService monitorService;

    /* --------------------------------------- PERMOHONAN SECTION ---------------------------------------------------*/

    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "laporan");
        model.addAttribute("subMenu", "laporanRekapAksiBulanan");
    }

    @RequestMapping(path = REQUEST_MAPPING_LAPORAN_REKAP_AKSI_BULANAN)
    public String doShowPageLapRekapAksiBulanan(@RequestParam(value = "error", required = false) String error, Model model, final HttpServletRequest request, final HttpServletResponse response) {
        List<UserDetails> mUser = userService.selectListUserByUserType("Karyawan");
		model.addAttribute("mUser", mUser);
        
        List<MFileType> fileTypeList = fileService.findMFileTypeByFileTypeMenu();
        Collections.sort(fileTypeList, (o1, o2) -> o1.getCode().compareTo(o2.getCode()));
        model.addAttribute("fileTypeList", fileTypeList);
        
        List<MFileSequence> fileSequenceList = fileService.findMFileSequenceByStatusFlagTrue();
        model.addAttribute("fileSequenceList", fileSequenceList);
        
        List<MClass> classList = classService.findAllMClass();
        Collections.sort(classList, (o1, o2) -> o1.getNo().compareTo(o2.getNo()));
        model.addAttribute("classList", classList);
        
        List<MBrandType> mBrandType = brandService.findAll();
        Collections.sort(mBrandType, (o1, o2) -> o1.getId().compareTo(o2.getId()));
        model.addAttribute("mBrandType", mBrandType);
        
        List<MAction> actionList = mActionService.findAll();
        model.addAttribute("actionList", actionList);
        
        List<Object[]> txMonitorYear = monitorService.findTxMonitorByYear();
        model.addAttribute("createdDateYear", txMonitorYear);

        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("errorMessage", error);
        }

        return PAGE_LAPORAN_REKAP_AKSI_BULANAN;
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
            String month = "";
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
                    	if((searchBy.equalsIgnoreCase("txTmBrand.mBrandType.id") || searchBy.equalsIgnoreCase("createdBy.id") || searchBy.equalsIgnoreCase("txReception.mFileSequence.id")
                    			|| searchBy.equalsIgnoreCase("txReception.mFileType.id")) && !(value.isEmpty())) {
                    		if (StringUtils.isNotBlank(value)) {
                        		searchCriteria.add(new KeyValue(searchBy, value, true));
                        	} else {
                        		searchCriteria.add(new KeyValue(searchBy, value, false));
                        	}
                    	} else if (searchBy.equalsIgnoreCase("createdDateMonth") && value != null) {
                        	if (StringUtils.isNotBlank(value)) {
                            	String [] dateParts = value.split("/");
                            	month = dateParts[0];
                                searchCriteria.add(new KeyValue(searchBy, month, true));
                            } 
                        } else if (searchBy.equalsIgnoreCase("createdDateYear") && value != null) {
                        	if (StringUtils.isNotBlank(value)) {
                            	String [] dateParts = value.split("/");
                            	month = dateParts[0];
                                searchCriteria.add(new KeyValue(searchBy, month, true));
                            } 
                        } else if ((searchBy.equalsIgnoreCase("mWorkflowProcessActions.mAction.id")  || searchBy.equalsIgnoreCase("mWorkflowProcess.status.id")) && !(value.isEmpty())) {
                        	if (StringUtils.isNotBlank(value)) {
                        		searchCriteria.add(new KeyValue(searchBy, value, true));
                        	} else {
                        		searchCriteria.add(new KeyValue(searchBy, value, false));
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
            }

            String orderBy = request.getParameter("order[0][column]");
            if (orderBy != null) {
                orderBy = orderBy.trim();
                if (orderBy.equalsIgnoreCase("")) {
                    orderBy = null;
                } else {
                    switch (orderBy) {
                        case "1":
                            orderBy = "mWorkflowProcessActions.action.name";
                            break;
                        case "2":
                            orderBy = "createdDate";
                            break;
                        case "3":
                            orderBy = "createdDate";
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


            List<String[]> data = new ArrayList<>();

            GenericSearchWrapper<Object[]> searchResult = monitorService.searchMonitorByWfProcessActionsBulanan(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());
 
                int no = offset;
                for (Object[] result : searchResult.getList()) {
                	no++;
                	
                	String aksi = "-";
                    long jumlah = 0;
                    long date1 = Long.parseLong(result[1].toString());
                    long date2 = Long.parseLong(result[2].toString());
                    long date3 = Long.parseLong(result[3].toString());
                    long date4 = Long.parseLong(result[4].toString());
                    long date5 = Long.parseLong(result[5].toString());
                    long date6 = Long.parseLong(result[6].toString());
                    long date7 = Long.parseLong(result[7].toString());
                    long date8 = Long.parseLong(result[8].toString());
                    long date9 = Long.parseLong(result[9].toString());
                    long date10 = Long.parseLong(result[10].toString());
                    long date11 = Long.parseLong(result[11].toString());
                    long date12 = Long.parseLong(result[12].toString());
                    long date13 = Long.parseLong(result[13].toString());
                    long date14 = Long.parseLong(result[14].toString());
                    long date15 = Long.parseLong(result[15].toString());
                    long date16 = Long.parseLong(result[16].toString());
                    long date17 = Long.parseLong(result[17].toString());
                    long date18 = Long.parseLong(result[18].toString());
                    long date19 = Long.parseLong(result[19].toString());
                    long date20 = Long.parseLong(result[20].toString());
                    long date21 = Long.parseLong(result[21].toString());
                    long date22 = Long.parseLong(result[22].toString());
                    long date23 = Long.parseLong(result[23].toString());
                    long date24 = Long.parseLong(result[24].toString());
                    long date25 = Long.parseLong(result[25].toString());
                    long date26 = Long.parseLong(result[26].toString());
                    long date27 = Long.parseLong(result[27].toString());
                    long date28 = Long.parseLong(result[28].toString());
                    long date29 = Long.parseLong(result[29].toString());
                    long date30 = Long.parseLong(result[30].toString());
                    long date31 = Long.parseLong(result[31].toString());
                    
                	try {
                        	aksi = result[0].toString();
                    } catch (NullPointerException e) {
                    }
                	
                	jumlah = date1 + date2 + date3 + date4 + date5 + date6 + date7 + date8 + date9 + date10 + date11 + date12
                			+ date13 + date14 + date15 + date16 + date17 + date18 + date19 + date20 + date21 + date22 + date23 + date24 +
                			date25 + date26 + date27 + date28 + date29 + date30 + date31;
                    	
                    data.add(new String[]{
                            "" + no,
                            aksi,
                            String.valueOf(result[1].toString()),
                            String.valueOf(result[2].toString()),
                            String.valueOf(result[3].toString()),
                            String.valueOf(result[4].toString()),
                            String.valueOf(result[5].toString()),
                            String.valueOf(result[6].toString()),
                            String.valueOf(result[7].toString()),
                            String.valueOf(result[8].toString()),
                            String.valueOf(result[9].toString()),
                            String.valueOf(result[10].toString()),
                            String.valueOf(result[11].toString()),
                            String.valueOf(result[12].toString()),
                            String.valueOf(result[13].toString()),
                            String.valueOf(result[14].toString()),
                            String.valueOf(result[15].toString()),
                            String.valueOf(result[16].toString()),
                            String.valueOf(result[17].toString()),
                            String.valueOf(result[18].toString()),
                            String.valueOf(result[19].toString()),
                            String.valueOf(result[20].toString()),
                            String.valueOf(result[21].toString()),
                            String.valueOf(result[22].toString()),
                            String.valueOf(result[23].toString()),
                            String.valueOf(result[24].toString()),
                            String.valueOf(result[25].toString()),
                            String.valueOf(result[26].toString()),
                            String.valueOf(result[27].toString()),
                            String.valueOf(result[28].toString()),
                            String.valueOf(result[29].toString()),
                            String.valueOf(result[30].toString()),
                            String.valueOf(result[31].toString()),
                            String.valueOf(jumlah)
                    });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @GetMapping(REQUEST_EXPORT_LAPORAN_REKAP_AKSI_BULANAN)
    public void doExportLapRekapAksiBulanan(HttpServletRequest request, HttpServletResponse response) {
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
        String createdDateMonth = "-";
        try {
        	createdDateMonth = request.getParameter("createdDateMonth");
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
                	if((searchBy.equalsIgnoreCase("txTmBrand.mBrandType.id")) && value != null) {
                		if (StringUtils.isNotBlank(value)) {
                    		searchCriteria.add(new KeyValue(searchBy, value, true));
                    	} 
                	} else if (searchBy.equalsIgnoreCase("createdDateMonth") && value != null) {
                    	if (StringUtils.isNotBlank(value)) {
                        	String [] dateParts = value.split("/");
                        	String month = dateParts[0];
                            searchCriteria.add(new KeyValue(searchBy, month, true));
                        } 
                    } else if (searchBy.equalsIgnoreCase("createdDateYear") && value != null) {
                    	if (StringUtils.isNotBlank(value)) {
                        	String [] dateParts = value.split("/");
                        	String month = dateParts[0];
                            searchCriteria.add(new KeyValue(searchBy, month, true));
                        } 
                    } else if ((searchBy.equalsIgnoreCase("createdBy.id")) && value != null) {
                    	if (StringUtils.isNotBlank(value)) {
	                            searchCriteria.add(new KeyValue(searchBy, value, true));
                    	}
                    } else if ((searchBy.equalsIgnoreCase("txReception.mFileSequence.id")) && value != null) {
                    	if (StringUtils.isNotBlank(value)) {
	                            searchCriteria.add(new KeyValue(searchBy, value, true));
                    	}
                    } else if ((searchBy.equalsIgnoreCase("txReception.mFileType.id")) && value != null) {
                    	if (StringUtils.isNotBlank(value)) {
	                            searchCriteria.add(new KeyValue(searchBy, value, true));
                    	}
                    } else if ((searchBy.equalsIgnoreCase("mWorkflowProcessActions.mAction.id")) && value != null) {
                    	if (StringUtils.isNotBlank(value)) {
                    		searchCriteria.add(new KeyValue(searchBy, value, true));
                    	} 
                    } else if ((searchBy.equalsIgnoreCase("mWorkflowProcess.status.id")) && value != null) {
                    	if (StringUtils.isNotBlank(value)) {
                    		searchCriteria.add(new KeyValue(searchBy, value, true));
                    	} 
                    } else if ((searchBy.equalsIgnoreCase("txTmClassList")) && value != null) {
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
            	case "1":
                    orderBy = "mWorkflowProcessActions.action.name";
                    break;
                case "2":
                    orderBy = "createdDate";
                    break;
                case "3":
                    orderBy = "createdDate";
                    break;
                default:
                    orderBy = "createdDate";
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
            reportInputStream = getClass().getClassLoader().getResourceAsStream("report/laporan-rekap-aksi-bulanan.xls");
            List<LapDataRekapAksiBulanan> dataList = new ArrayList<>();
            int totalRecord = (int) monitorService.countLapRekapAksi(searchCriteria);
            int retrieved = 0;
            LapDataRekapAksiBulanan dataRekapAksiBulanan = null;
            
            int no = 0;
            int countTotal = 0;
            int countAction = 0;
            
            int sumDay1 = 0;
            int sumDay2 = 0;
            int sumDay3 = 0;
            int sumDay4 = 0;
            int sumDay5 = 0;
            int sumDay6 = 0;
            int sumDay7 = 0;
            int sumDay8 = 0;
            int sumDay9 = 0;
            int sumDay10 = 0;
            int sumDay11 = 0;
            int sumDay12 = 0;
            int sumDay13 = 0;
            int sumDay14 = 0;
            int sumDay15 = 0;
            int sumDay16 = 0;
            int sumDay17 = 0;
            int sumDay18 = 0;
            int sumDay19 = 0;
            int sumDay20 = 0;
            int sumDay21 = 0;
            int sumDay22 = 0;
            int sumDay23 = 0;
            int sumDay24 = 0;
            int sumDay25 = 0;
            int sumDay26 = 0;
            int sumDay27 = 0;
            int sumDay28 = 0;
            int sumDay29 = 0;
            int sumDay30 = 0;
            int sumDay31 = 0;
            
            if (totalRecord > 0) {
                List<Object[]> retrievedDataList = monitorService.selectAllLapRekapAksiBulanan(searchCriteria, orderBy, orderType, retrieved, 1000);

                	String aksi = "-";
                	long jumlah = 0;
                	
                	long date1 = 0;
                	long date2 = 0;
                	long date3 = 0;
                	long date4 = 0;
                	long date5 = 0;
                	long date6 = 0;
                	long date7 = 0;
                	long date8 = 0;
                	long date9 = 0;
                	long date10 = 0;
                	long date11 = 0;
                	long date12 = 0;
                	long date13 = 0;
                	long date14 = 0;
                	long date15 = 0;
                	long date16 = 0;
                	long date17 = 0;
                	long date18 = 0;
                	long date19 = 0;
                	long date20 = 0;
                	long date21 = 0;
                	long date22 = 0;
                	long date23 = 0;
                	long date24 = 0;
                	long date25 = 0;
                	long date26 = 0;
                	long date27 = 0;
                	long date28 = 0;
                	long date29 = 0;
                	long date30 = 0;
                	long date31 = 0;
                	
	                for (Object[] result : retrievedDataList) {
	                	no++;
	                	
	                    date1 = Long.parseLong(result[1].toString());
	                    date2 = Long.parseLong(result[2].toString());
	                    date3 = Long.parseLong(result[3].toString());
	                    date4 = Long.parseLong(result[4].toString());
	                    date5 = Long.parseLong(result[5].toString());
	                    date6 = Long.parseLong(result[6].toString());
	                    date7 = Long.parseLong(result[7].toString());
	                    date8 = Long.parseLong(result[8].toString());
	                    date9 = Long.parseLong(result[9].toString());
	                    date10 = Long.parseLong(result[10].toString());
	                    date11 = Long.parseLong(result[11].toString());
	                    date12 = Long.parseLong(result[12].toString());
	                    date13 = Long.parseLong(result[13].toString());
	                    date14 = Long.parseLong(result[14].toString());
	                    date15 = Long.parseLong(result[15].toString());
	                    date16 = Long.parseLong(result[16].toString());
	                    date17 = Long.parseLong(result[17].toString());
	                    date18 = Long.parseLong(result[18].toString());
	                    date19 = Long.parseLong(result[19].toString());
	                    date20 = Long.parseLong(result[20].toString());
	                    date21 = Long.parseLong(result[21].toString());
	                    date22 = Long.parseLong(result[22].toString());
	                    date23 = Long.parseLong(result[23].toString());
	                    date24 = Long.parseLong(result[24].toString());
	                    date25 = Long.parseLong(result[25].toString());
	                    date26 = Long.parseLong(result[26].toString());
	                    date27 = Long.parseLong(result[27].toString());
	                    date28 = Long.parseLong(result[28].toString());
	                    date29 = Long.parseLong(result[29].toString());
	                    date30 = Long.parseLong(result[30].toString());
	                    date31 = Long.parseLong(result[31].toString());
	                    
	                	try {
	                        	aksi = result[0].toString();
	                    } catch (NullPointerException e) {
	                    }
	                	
	                	jumlah = date1 + date2 + date3 + date4 + date5 + date6 + date7 + date8 + date9 + date10 + date11 + date12
	                			+ date13 + date14 + date15 + date16 + date17 + date18 + date19 + date20 + date21 + date22 + date23 + date24 +
	                			date25 + date26 + date27 + date28 + date29 + date30 + date31;
	                
                    
                    sumDay1 += date1;
                    sumDay2 += date2;
                    sumDay3 += date3;
                    sumDay4 += date4;
                    sumDay5 += date5;
                    sumDay6 += date6;
                    sumDay7 += date7;
                    sumDay8 += date8;
                    sumDay9 += date9;
                    sumDay10 += date10;
                    sumDay11 += date11;
                    sumDay12 += date12;
                    sumDay13 += date13;
                    sumDay14 += date14;
                    sumDay15 += date15;
                    sumDay16 += date16;
                    sumDay17 += date17;
                    sumDay18 += date18;
                    sumDay19 += date19;
                    sumDay20 += date20;
                    sumDay21 += date21;
                    sumDay22 += date22;
                    sumDay23 += date23;
                    sumDay24 += date24;
                    sumDay25 += date25;
                    sumDay26 += date26;
                    sumDay27 += date27;
                    sumDay28 += date28;
                    sumDay29 += date29;
                    sumDay30 += date30;
                    sumDay31 += date31;
                    
                    dataRekapAksiBulanan = new LapDataRekapAksiBulanan();
                    dataRekapAksiBulanan.setNo(no);
                    dataRekapAksiBulanan.setActionName(aksi);
                    dataRekapAksiBulanan.setDay1((int)date1);
                    dataRekapAksiBulanan.setDay2((int)date2);
                    dataRekapAksiBulanan.setDay3((int)date3);
                    dataRekapAksiBulanan.setDay4((int)date4);
                    dataRekapAksiBulanan.setDay5((int)date5);
                    dataRekapAksiBulanan.setDay6((int)date6);
                    dataRekapAksiBulanan.setDay7((int)date7);
                    dataRekapAksiBulanan.setDay8((int)date8);
                    dataRekapAksiBulanan.setDay9((int)date9);
                    dataRekapAksiBulanan.setDay10((int)date10);
                    dataRekapAksiBulanan.setDay11((int)date11);
                    dataRekapAksiBulanan.setDay12((int)date12);
                    dataRekapAksiBulanan.setDay13((int)date13);
                    dataRekapAksiBulanan.setDay14((int)date14);
                    dataRekapAksiBulanan.setDay15((int)date15);
                    dataRekapAksiBulanan.setDay16((int)date16);
                    dataRekapAksiBulanan.setDay17((int)date17);
                    dataRekapAksiBulanan.setDay18((int)date18);
                    dataRekapAksiBulanan.setDay19((int)date19);
                    dataRekapAksiBulanan.setDay20((int)date20);
                    dataRekapAksiBulanan.setDay21((int)date21);
                    dataRekapAksiBulanan.setDay22((int)date22);
                    dataRekapAksiBulanan.setDay23((int)date23);
                    dataRekapAksiBulanan.setDay24((int)date24);
                    dataRekapAksiBulanan.setDay25((int)date25);
                    dataRekapAksiBulanan.setDay26((int)date26);
                    dataRekapAksiBulanan.setDay27((int)date27);
                    dataRekapAksiBulanan.setDay28((int)date28);
                    dataRekapAksiBulanan.setDay29((int)date29);
                    dataRekapAksiBulanan.setDay30((int)date30);
                    dataRekapAksiBulanan.setDay31((int)date31);
                    dataRekapAksiBulanan.setTotal(String.valueOf(jumlah));
                    dataList.add(dataRekapAksiBulanan);
                    
                    countTotal += jumlah;
	            }
                
                retrieved += retrievedDataList.size();
            }

            Context context = new Context();
            context.putVar("dataList", dataList);
            context.putVar("createdDateMonth", createdDateMonth);
            context.putVar("countAction", countAction);
            context.putVar("countTotal", countTotal);
            context.putVar("sumDay1", sumDay1);
            context.putVar("sumDay2", sumDay2);
            context.putVar("sumDay3", sumDay3);
            context.putVar("sumDay4", sumDay4);
            context.putVar("sumDay5", sumDay5);
            context.putVar("sumDay6", sumDay6);
            context.putVar("sumDay7", sumDay7);
            context.putVar("sumDay8", sumDay8);
            context.putVar("sumDay9", sumDay9);
            context.putVar("sumDay10", sumDay10);
            context.putVar("sumDay11", sumDay11);
            context.putVar("sumDay12", sumDay12);
            context.putVar("sumDay13", sumDay13);
            context.putVar("sumDay14", sumDay14);
            context.putVar("sumDay15", sumDay15);
            context.putVar("sumDay16", sumDay16);
            context.putVar("sumDay17", sumDay17);
            context.putVar("sumDay18", sumDay18);
            context.putVar("sumDay19", sumDay19);
            context.putVar("sumDay20", sumDay20);
            context.putVar("sumDay21", sumDay21);
            context.putVar("sumDay22", sumDay22);
            context.putVar("sumDay23", sumDay23);
            context.putVar("sumDay24", sumDay24);
            context.putVar("sumDay25", sumDay25);
            context.putVar("sumDay26", sumDay26);
            context.putVar("sumDay27", sumDay27);
            context.putVar("sumDay28", sumDay28);
            context.putVar("sumDay29", sumDay29);
            context.putVar("sumDay30", sumDay30);
            context.putVar("sumDay31", sumDay31);

            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=laporan-rekap-aksi-bulanan.xls");

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