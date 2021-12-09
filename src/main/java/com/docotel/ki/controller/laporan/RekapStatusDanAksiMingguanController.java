package com.docotel.ki.controller.laporan;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.*;
import com.docotel.ki.model.transaction.TxMonitor;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.pojo.LapDataRekapStatusDanAksiMingguan;
import com.docotel.ki.service.master.*;
import com.docotel.ki.service.transaction.MonitorService;
import com.docotel.ki.service.transaction.TrademarkService;
import com.docotel.ki.util.DateUtil;
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
import java.text.ParseException;
import java.util.*;

@Controller 
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class RekapStatusDanAksiMingguanController extends BaseController {
    public static final String PATH_LAPORAN_REKAP_STATUS_DAN_AKSI_MINGGUAN = "/laporan-rekap-status-dan-aksi-mingguan";
    public static final String PATH_AJAX_SEARCH_LIST = "/cari-rekap-status-dan-aksi-mingguan";
    private static final String PATH_EXPORT_DATA_LAPORAN_REKAP_STATUS_DAN_AKSI_MINGGUAN = "/cetak-rekap-status-dan-aksi-mingguan";
    private static final String DIRECTORY_PAGE = "laporan/";
    private static final String PAGE_LAPORAN_REKAP_STATUS_DAN_AKSI_MINGGUAN = DIRECTORY_PAGE + "laporan-rekap-status-dan-aksi-mingguan";
    private static final String REQUEST_MAPPING_AJAX_SEARCH_LIST = PATH_AJAX_SEARCH_LIST + "*";
    private static final String REQUEST_MAPPING_LAPORAN_REKAP_STATUS_DAN_AKSI_MINGGUAN = PATH_LAPORAN_REKAP_STATUS_DAN_AKSI_MINGGUAN + "*";
    private static final String REQUEST_EXPORT_LAPORAN_REKAP_STATUS_DAN_AKSI_MINGGUAN = PATH_EXPORT_DATA_LAPORAN_REKAP_STATUS_DAN_AKSI_MINGGUAN + "*";
    
    @Autowired
    private TrademarkService trademarkService;
    @Autowired
    private StatusService statusService;
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
    private GroupService groupService;
    @Autowired
    private MonitorService monitorService;

    /* --------------------------------------- PERMOHONAN SECTION ---------------------------------------------------*/

    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "laporan");
        model.addAttribute("subMenu", "laporanRekapStatusDanAksiMingguan");
    }

    @RequestMapping(path = REQUEST_MAPPING_LAPORAN_REKAP_STATUS_DAN_AKSI_MINGGUAN)
    public String doShowPageLapRekapStatusDanAksiMingguan(@RequestParam(value = "error", required = false) String error, Model model, final HttpServletRequest request, final HttpServletResponse response) {
        List<UserDetails> mUser = userService.selectListUserByUserType("Karyawan");
		model.addAttribute("mUser", mUser);

        List<MStatus> statusList = statusService.selectStatus();
        model.addAttribute("statusList", statusList);
        
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

        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("errorMessage", error);
        }

        return PAGE_LAPORAN_REKAP_STATUS_DAN_AKSI_MINGGUAN;
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
                    	if((searchBy.equalsIgnoreCase("txTmBrand.mBrandType.id") || searchBy.equalsIgnoreCase("createdBy.id") || searchBy.equalsIgnoreCase("txReception.mFileSequence.id")
                    			|| searchBy.equalsIgnoreCase("txReception.mFileType.id")) && !(value.isEmpty())) {
                    		if (StringUtils.isNotBlank(value)) {
                        		searchCriteria.add(new KeyValue(searchBy, value, true));
                        	} else {
                        		searchCriteria.add(new KeyValue(searchBy, value, false));
                        	}
                    	} else if (searchBy.equalsIgnoreCase("startDate") || searchBy.equalsIgnoreCase("endDate")) {
                        	if (StringUtils.isNotBlank(value)) {
    	                    	try {
    	                            searchCriteria.add(new KeyValue(searchBy, DateUtil.toDate("dd/MM/yyyy", value), true));
    	                        } catch (ParseException e) {
    	                        }
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
                            orderBy = "createdDate";
                            break;
                        case "2":
                            orderBy = "mWorkflowProcess.status.name";
                            break;
                        case "3":
                            orderBy = "mWorkflowProcessActions.action.name";
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

            GenericSearchWrapper<Object[]> searchResult = monitorService.searchMonitorByWfProcessActionsMingguan(searchCriteria, orderBy, orderType, offset, limit);
//            GenericSearchWrapper<TxMonitor> searchResult = monitorService.searchMonitorByWfProcessActions(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());
 
                int no = offset;
                for (Object[] result : searchResult.getList()) {
                	no++;
                    String status = "-";
                    String aksi = "-";
                    long jumlah = 0;
                    String date = "";

                    try {
                        date = result[1].toString();
                    }catch (NullPointerException e) {}

                    try {
                        status = result[2].toString();
                    }catch (NullPointerException e) {}

                    try {
                        jumlah = Long.parseLong(result[3].toString());
                    }catch (NullPointerException e) {}

//                    List<TxMonitor> txMonitorList = monitorService.findTxMonitorByMWorkflowProcessActionsId(result.toString());
//                    for(TxMonitor monitorList : txMonitorList) {
//                    	try {
//                            status = monitorList.getmWorkflowProcess().getStatus().getName();
//                        } catch (NullPointerException e) {
//                        }
//
//                    	date = monitorList.getCreatedDateTemp();
//
//                        try {
//                            if(aksi.equalsIgnoreCase("-")) {
//                                jumlah = monitorService.countWfProcess(monitorList.getmWorkflowProcess().getId());
//                            } else {
//                                jumlah = monitorService.countWfProcessAction(monitorList.getmWorkflowProcessActions().getId());
//                            }
//
//                        } catch (NullPointerException e) {
//                        }
//                    }
                    data.add(new String[]{
                            "" + no,
                            date,
                            status,
                            aksi,
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

    @GetMapping(REQUEST_EXPORT_LAPORAN_REKAP_STATUS_DAN_AKSI_MINGGUAN)
    public void doExportLapRekapStatusDanAksiMingguan(HttpServletRequest request, HttpServletResponse response) {
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
                	} else if (searchBy.equalsIgnoreCase("startDate")) {
                    	if (StringUtils.isNotBlank(value)) {
	                    	try {
	                            searchCriteria.add(new KeyValue(searchBy, DateUtil.toDate("dd/MM/yyyy", value), true));
	                        } catch (ParseException e) {
	                        }
                    	}
                    } else if (searchBy.equalsIgnoreCase("endDate")) {
                    	if (StringUtils.isNotBlank(value)) {
	                    	try {
	                            searchCriteria.add(new KeyValue(searchBy, DateUtil.toDate("dd/MM/yyyy", value), true));
	                        } catch (ParseException e) {
	                        }
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
                    orderBy = "createdDate";
                    break;
                case "2":
                    orderBy = "mWorkflowProcess.status.name";
                    break;
                case "3":
                    orderBy = "mWorkflowProcessActions.action.name";
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
            reportInputStream = getClass().getClassLoader().getResourceAsStream("report/laporan-rekap-status-dan-aksi-mingguan.xls");
            List<LapDataRekapStatusDanAksiMingguan> dataList = new ArrayList<>();
            int totalRecord = (int) monitorService.countLapRekapAksi(searchCriteria);
            int retrieved = 0;
            LapDataRekapStatusDanAksiMingguan dataRekapStatusDanAksiMingguan = null;
            
            int no = 0;
            int countStatus = 0;
            int countAction = 0;
            int countTotal = 0;
            long total = 0;
            String createdDate = "-";
            String actionName = "-";
            String statusName = "-";
            if (totalRecord > 0) {
                List<TxMonitor> retrievedDataList = monitorService.selectAllLapRekapAksi(searchCriteria, orderBy, orderType, retrieved, 1000);

                for (Object result : retrievedDataList) {
                    no++;

                    List<TxMonitor> txMonitorList = monitorService.findTxMonitorByMWorkflowProcessActionsId(result.toString());
                    for(TxMonitor monitorList : txMonitorList) {

                        /*try {
                            if (monitorList.getmWorkflowProcess() != null) {
                                if(!monitorList.getmWorkflowProcess().getStatus().getName().isEmpty()) {
                                    countStatus++;
                                }
                            }
                        } catch (NullPointerException e) {

                        }*/

                        /*try {
                            if(monitorList.getmWorkflowProcessActions() != null) {
                                if(!monitorList.getmWorkflowProcessActions().getAction().getName().isEmpty()) {
                                    countAction++;
                                }
                            }
                        } catch (NullPointerException e) {

                        }*/

                        try {
                            createdDate = monitorList.getCreatedDateTemp();
                        } catch (NullPointerException e) {
                        }

                        try {
                            if(monitorList.getmWorkflowProcessActions() != null) {
                                if(!monitorList.getmWorkflowProcessActions().getAction().getName().isEmpty()) {
                                    actionName = monitorList.getmWorkflowProcessActions().getAction().getName();
                                }
                            }
                        } catch (NullPointerException e) {
                        }

                        try {
                            if(monitorList.getmWorkflowProcess() != null) {
                                if(!monitorList.getmWorkflowProcess().getStatus().getName().isEmpty()) {
                                    statusName = monitorList.getmWorkflowProcess().getStatus().getName();
                                }
                            }
                        } catch (NullPointerException e) {
                        }

                        try {
                            if(actionName.equalsIgnoreCase("-")) {
                                total = monitorService.countWfProcess(monitorList.getmWorkflowProcess().getId());
                            } else {
                                total = monitorService.countWfProcessAction(monitorList.getmWorkflowProcessActions().getId());
                            }
                        } catch (NullPointerException e) {
                        }
                    }

                    dataRekapStatusDanAksiMingguan = new LapDataRekapStatusDanAksiMingguan();
                    dataRekapStatusDanAksiMingguan.setNo(no);
                    dataRekapStatusDanAksiMingguan.setCreatedDate(createdDate);
                    dataRekapStatusDanAksiMingguan.setActionName(actionName);
                    dataRekapStatusDanAksiMingguan.setStatusName(statusName);
                    dataRekapStatusDanAksiMingguan.setTotal(String.valueOf(total));
                    dataList.add(dataRekapStatusDanAksiMingguan);
                    countTotal += total;
                    countAction = no;
                    countStatus = no;
                }
                retrieved += retrievedDataList.size();
            }

            Context context = new Context();
            context.putVar("dataList", dataList);
            context.putVar("countAction", countAction);
            context.putVar("countStatus", countStatus);
            context.putVar("countTotal", countTotal);

            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=laporan-rekap-status-dan-aksi-mingguan.xls");

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