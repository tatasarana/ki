package com.docotel.ki.controller.laporan;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.*;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.pojo.LapDataRekapAksiTahunan;
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
public class RekapAksiTahunanController extends BaseController {
    public static final String PATH_LAPORAN_REKAP_AKSI_TAHUNAN = "/laporan-rekap-aksi-tahunan";
    public static final String PATH_AJAX_SEARCH_LIST = "/cari-rekap-aksi-tahunan";
    private static final String PATH_EXPORT_DATA_LAPORAN_REKAP_AKSI_TAHUNAN = "/cetak-rekap-aksi-tahunan";
    private static final String DIRECTORY_PAGE = "laporan/";
    private static final String PAGE_LAPORAN_REKAP_AKSI_TAHUNAN = DIRECTORY_PAGE + "laporan-rekap-aksi-tahunan";
    private static final String REQUEST_MAPPING_AJAX_SEARCH_LIST = PATH_AJAX_SEARCH_LIST + "*";
    private static final String REQUEST_MAPPING_LAPORAN_REKAP_AKSI_TAHUNAN = PATH_LAPORAN_REKAP_AKSI_TAHUNAN + "*";
    private static final String REQUEST_EXPORT_LAPORAN_REKAP_AKSI_TAHUNAN = PATH_EXPORT_DATA_LAPORAN_REKAP_AKSI_TAHUNAN + "*";
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
        model.addAttribute("subMenu", "laporanRekapAksiTahunan");
    }
    
    public static <T> void removeDuplicate(List <T> list) {
	    HashSet <T> h = new HashSet<T>(list);
	    list.clear();
	    list.addAll(h);
	}

    @RequestMapping(path = REQUEST_MAPPING_LAPORAN_REKAP_AKSI_TAHUNAN)
    public String doShowPageLapRekapAksiTahunan(@RequestParam(value = "error", required = false) String error, Model model, final HttpServletRequest request, final HttpServletResponse response) {
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

        return PAGE_LAPORAN_REKAP_AKSI_TAHUNAN;
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
                    	} else if (searchBy.equalsIgnoreCase("createdDateYear") && !(value.isEmpty())) {
                    		if (StringUtils.isNotBlank(value)) {
                                searchCriteria.add(new KeyValue(searchBy, value, true));
                            } else {
                        		searchCriteria.add(new KeyValue(searchBy, value, false));
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

            GenericSearchWrapper<Object[]> searchResult = monitorService.searchMonitorByWfProcessActionsTahunan(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());
 
                int no = offset;
                for (Object[] result : searchResult.getList()) {
                	no++;
                	
                	String aksi = "-";
                    long countMonth1 = Long.parseLong(result[1].toString());
                    long countMonth2 = Long.parseLong(result[2].toString());
                    long countMonth3 = Long.parseLong(result[3].toString());
                    long countMonth4 = Long.parseLong(result[4].toString());
                    long countMonth5 = Long.parseLong(result[5].toString());
                    long countMonth6 = Long.parseLong(result[6].toString());
                    long countMonth7 = Long.parseLong(result[7].toString());
                    long countMonth8 = Long.parseLong(result[8].toString());
                    long countMonth9 = Long.parseLong(result[9].toString());
                    long countMonth10 = Long.parseLong(result[10].toString());
                    long countMonth11 = Long.parseLong(result[11].toString());
                    long countMonth12 = Long.parseLong(result[12].toString());
                    long jumlah = 0;
                    
                    try {
                    	  aksi = result[0].toString();
                    	} catch (NullPointerException e) {
                    }
                    
                    jumlah = countMonth1 + countMonth2 + countMonth3 + countMonth4 + countMonth5 + countMonth6 + countMonth7 
                    		+ countMonth8 + countMonth9 + countMonth10 + countMonth11 + countMonth12;
                    
                    data.add(new String[]{
                            "" + no,
                            aksi,
                            String.valueOf(countMonth1),
                            String.valueOf(countMonth2),
                            String.valueOf(countMonth3),
                            String.valueOf(countMonth4),
                            String.valueOf(countMonth5),
                            String.valueOf(countMonth6),
                            String.valueOf(countMonth7),
                            String.valueOf(countMonth8),
                            String.valueOf(countMonth9),
                            String.valueOf(countMonth10),
                            String.valueOf(countMonth11),
                            String.valueOf(countMonth12),
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

    @GetMapping(REQUEST_EXPORT_LAPORAN_REKAP_AKSI_TAHUNAN)
    public void doExportLapRekapAksiTahunan(HttpServletRequest request, HttpServletResponse response) {
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
        String createdDateYear = "-";
        try {
        	createdDateYear = request.getParameter("createdDateYear");
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
                	} else if (searchBy.equalsIgnoreCase("createdDateYear") && value != null) {
                		if (StringUtils.isNotBlank(value)) {
                            searchCriteria.add(new KeyValue(searchBy, value, true));
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
            reportInputStream = getClass().getClassLoader().getResourceAsStream("report/laporan-rekap-aksi-tahunan.xls");
            List<LapDataRekapAksiTahunan> dataList = new ArrayList<>();
            int totalRecord = (int) monitorService.countLapRekapAksi(searchCriteria);
            int retrieved = 0;
            LapDataRekapAksiTahunan dataRekapAksiTahunan = null;
            
            int no = 0;
            long countTotal = 0;
            long countMonth1 = 0;
            long countMonth2 = 0;
            long countMonth3 = 0;
            long countMonth4 = 0;
            long countMonth5 = 0;
            long countMonth6 = 0;
            long countMonth7 = 0;
            long countMonth8 = 0;
            long countMonth9 = 0;
            long countMonth10 = 0;
            long countMonth11 = 0;
            long countMonth12 = 0;
            
            int sumMonth1 = 0;
            int sumMonth2 = 0;
            int sumMonth3 = 0;
            int sumMonth4 = 0;
            int sumMonth5 = 0;
            int sumMonth6 = 0;
            int sumMonth7 = 0;
            int sumMonth8 = 0;
            int sumMonth9 = 0;
            int sumMonth10 = 0;
            int sumMonth11 = 0;
            int sumMonth12 = 0;
            
            if (totalRecord > 0) {
                List<Object[]> retrievedDataList = monitorService.selectAllLapRekapAksiTahunan(searchCriteria, orderBy, orderType, retrieved, 1000);

                for (Object[] result : retrievedDataList) {
                	
                    no++;
                    String actionName = "-";
                    long total = 0;
                    countMonth1 = Long.parseLong(result[1].toString());
                    countMonth2 = Long.parseLong(result[2].toString());
                    countMonth3 = Long.parseLong(result[3].toString());
                    countMonth4 = Long.parseLong(result[4].toString());
                    countMonth5 = Long.parseLong(result[5].toString());
                    countMonth6 = Long.parseLong(result[6].toString());
                    countMonth7 = Long.parseLong(result[7].toString());
                    countMonth8 = Long.parseLong(result[8].toString());
                    countMonth9 = Long.parseLong(result[9].toString());
                    countMonth10 = Long.parseLong(result[10].toString());
                    countMonth11 = Long.parseLong(result[11].toString());
                    countMonth12 = Long.parseLong(result[12].toString());
                    
                    try {
                    	actionName = result[0].toString();
                    } catch (NullPointerException e) {
                    }
                    
                    total = countMonth1 + countMonth2 + countMonth3 + countMonth4 + countMonth5 + countMonth6 + countMonth7 
                    		+ countMonth8 + countMonth9 + countMonth10 + countMonth11 + countMonth12;
                    
                    sumMonth1 += countMonth1;
                    sumMonth2 += countMonth2;
                    sumMonth3 += countMonth3;
                    sumMonth4 += countMonth4;
                    sumMonth5 += countMonth5;
                    sumMonth6 += countMonth6;
                    sumMonth7 += countMonth7;
                    sumMonth8 += countMonth8;
                    sumMonth9 += countMonth9;
                    sumMonth10 += countMonth10;
                    sumMonth11 += countMonth11;
                    sumMonth12 += countMonth12;
                    
                    dataRekapAksiTahunan = new LapDataRekapAksiTahunan();
                    dataRekapAksiTahunan.setNo(no);
                    dataRekapAksiTahunan.setActionName(actionName);
                    dataRekapAksiTahunan.setMonth1((int)countMonth1);
                    dataRekapAksiTahunan.setMonth2((int)countMonth2);
                    dataRekapAksiTahunan.setMonth3((int)countMonth3);
                    dataRekapAksiTahunan.setMonth4((int)countMonth4);
                    dataRekapAksiTahunan.setMonth5((int)countMonth5);
                    dataRekapAksiTahunan.setMonth6((int)countMonth6);
                    dataRekapAksiTahunan.setMonth7((int)countMonth7);
                    dataRekapAksiTahunan.setMonth8((int)countMonth8);
                    dataRekapAksiTahunan.setMonth9((int)countMonth9);
                    dataRekapAksiTahunan.setMonth10((int)countMonth10);
                    dataRekapAksiTahunan.setMonth11((int)countMonth11);
                    dataRekapAksiTahunan.setMonth12((int)countMonth12);
                    dataRekapAksiTahunan.setTotal(String.valueOf(total));
                    dataList.add(dataRekapAksiTahunan);
                    
                    countTotal += total;
                }
                retrieved += retrievedDataList.size();
            }

            Context context = new Context();
            context.putVar("dataList", dataList);
            context.putVar("createdDateYear", createdDateYear);
            context.putVar("countTotal", countTotal);
            context.putVar("sumMonth1", sumMonth1);
            context.putVar("sumMonth2", sumMonth2);
            context.putVar("sumMonth3", sumMonth3);
            context.putVar("sumMonth4", sumMonth4);
            context.putVar("sumMonth5", sumMonth5);
            context.putVar("sumMonth6", sumMonth6);
            context.putVar("sumMonth7", sumMonth7);
            context.putVar("sumMonth8", sumMonth8);
            context.putVar("sumMonth9", sumMonth9);
            context.putVar("sumMonth10", sumMonth10);
            context.putVar("sumMonth11", sumMonth11);
            context.putVar("sumMonth12", sumMonth12);
            
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=laporan-rekap-aksi-tahunan.xls");

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