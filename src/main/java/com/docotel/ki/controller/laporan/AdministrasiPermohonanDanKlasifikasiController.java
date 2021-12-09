package com.docotel.ki.controller.laporan;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.enumeration.StatusEnum;
import com.docotel.ki.model.master.*;
import com.docotel.ki.model.transaction.TxGroupDetail;
import com.docotel.ki.model.transaction.TxPubsJournal;
import com.docotel.ki.model.transaction.TxTmClass;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.pojo.LapDataFormalitas;
import com.docotel.ki.repository.transaction.TxPubsJournalRepository;
import com.docotel.ki.service.master.*;
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
import java.text.ParseException;
import java.util.*;

@Controller 
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class AdministrasiPermohonanDanKlasifikasiController extends BaseController {
    public static final String PATH_LAPORAN_ADMINISTRASI_PERMOHONAN = "/laporan-administrasi-permohonan-klasifikasi";
    public static final String PATH_AJAX_SEARCH_LIST = "/cari-administrasi-permohonan-klasifikasi";
    private static final String PATH_EXPORT_DATA_LAPORAN_ADMINISTRASI_PERMOHONAN = "/cetak-administrasi-permohonan-klasifikasi";
    private static final String DIRECTORY_PAGE = "laporan/";
    private static final String PAGE_LAPORAN_ADMINISTRASI_PERMOHONAN = DIRECTORY_PAGE + "laporan-administrasi-permohonan-klasifikasi";
    private static final String REQUEST_MAPPING_AJAX_SEARCH_LIST = PATH_AJAX_SEARCH_LIST + "*";
    private static final String REQUEST_MAPPING_LAPORAN_ADMINISTRASI_PERMOHONAN = PATH_LAPORAN_ADMINISTRASI_PERMOHONAN + "*";
    private static final String REQUEST_EXPORT_LAPORAN_ADMINISTRASI_PERMOHONAN = PATH_EXPORT_DATA_LAPORAN_ADMINISTRASI_PERMOHONAN + "*";
    
    @Autowired
    private TrademarkService trademarkService;
    @Autowired
    private StatusService statusService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private FileService fileService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private ClassService classService;
    @Autowired
    private MActionService mActionService;
    @Autowired
    private UserService userService;
    @Autowired
    private TxPubsJournalRepository txPubsJournalRepository;

    /* --------------------------------------- PERMOHONAN SECTION ---------------------------------------------------*/

    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "laporan");
        model.addAttribute("subMenu", "laporanAdministrasiPermohonanKlasifikasi");
    }
    
    public static <T> void removeDuplicate(List <T> list) {
	    HashSet <T> h = new HashSet<T>(list);
	    list.clear();
	    list.addAll(h);
	}

    @RequestMapping(path = REQUEST_MAPPING_LAPORAN_ADMINISTRASI_PERMOHONAN)
    public String doShowPageLapAdminstrasiPermohonan(@RequestParam(value = "error", required = false) String error, Model model, final HttpServletRequest request, final HttpServletResponse response) {
        List<MFileSequence> fileSequenceList = fileService.findMFileSequenceByStatusFlagTrue();
        model.addAttribute("fileSequenceList", fileSequenceList);

        List<MFileType> fileTypeList = fileService.findMFileTypeByFileTypeMenu();
        Collections.sort(fileTypeList, (o1, o2) -> o1.getCode().compareTo(o2.getCode()));
        model.addAttribute("fileTypeList", fileTypeList);
        
        List<MBrandType> mBrandType = brandService.findAll();
        Collections.sort(mBrandType, (o1, o2) -> o1.getId().compareTo(o2.getId()));
        model.addAttribute("mBrandType", mBrandType);

        List<MClass> classList = classService.findAllMClass();
        Collections.sort(classList, (o1, o2) -> o1.getNo().compareTo(o2.getNo()));
        model.addAttribute("classList", classList);
        
        List<UserDetails> mUser = userService.selectListUserByUserType("Karyawan");
		model.addAttribute("mUser", mUser);

        List<MStatus> statusList = statusService.selectStatus();
        model.addAttribute("statusList", statusList);
        
        List<MAction> actionList = mActionService.findAll();
        model.addAttribute("actionList", actionList);

        List<MFileTypeDetail> fileTypeDetailList = fileService.getAllFileTypeDetail();
        model.addAttribute("fileTypeDetailList", fileTypeDetailList);

        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("errorMessage", error);
        }

        return PAGE_LAPORAN_ADMINISTRASI_PERMOHONAN;
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
                    	if((searchBy.equalsIgnoreCase("txReception.mFileTypeDetail.id") || (searchBy.equalsIgnoreCase("mAction.id") || searchBy.equalsIgnoreCase("txTmBrand.mBrandType.id") || searchBy.equalsIgnoreCase("createdBy.id")) && !(value.isEmpty()))) {
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

            searchCriteria.add(new KeyValue("statusOnOff", StatusEnum.IPT_DRAFT.name(), true));

            String orderBy = request.getParameter("order[0][column]");
            if (orderBy != null) {
                orderBy = orderBy.trim();
                if (orderBy.equalsIgnoreCase("")) {
                    orderBy = null;
                } else {
                    switch (orderBy) {
                        case "1":
                            orderBy = "applicationNo";
                            break;
                        case "2":
                            orderBy = "filingDate";
                            break;
                        case "3":
                            orderBy = "mClass.no";
                            break;
                        case "4":
                            orderBy = "txTmBrand.mBrandType.name";
                            break;
                        case "5":
                            orderBy = "txTmBrand.name";
                            break;
                        case "6":
                            orderBy = "txTmOwner.name";
                            break;
                        case "7":
                            orderBy = "txReception.mFileTypeDetail.desc";
                            break;
                        default:
                            orderBy = "filingDate";
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

            GenericSearchWrapper<TxTmGeneral> searchResult = trademarkService.searchGeneral(searchCriteria, orderBy, orderType, offset, limit, false);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());
 
                int no = offset;
                for (TxTmGeneral result : searchResult.getList()) {
                    no++;
                    String brandName = "-";
                    String brandType = "-";
                    String sbClassList = "-";
                    String brmNo = "-";
                    String fileTypeDetDesc = "-";
                    ArrayList<String> kelass = new ArrayList<String>();

                    try {
                        brandName = result.getTxTmBrand().getName();
                    } catch (NullPointerException e) {
                    }
                    
                    try {
                    	brandType = result.getTxTmBrand().getmBrandType().getName();
                    } catch (NullPointerException e) {
                    	
                    }
                    
                    try {
                        for (TxTmClass txTmClass : result.getTxTmClassList()) {
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
                    	TxGroupDetail txGroupDetail = groupService.findTxGroupDetailByTxTmGeneral(result.getId(), "GrupPublikasi");
                        TxPubsJournal txPubsJournal = txPubsJournalRepository.findTxPubsJournalByTxGroup(txGroupDetail.getTxGroup());
                        brmNo = txPubsJournal.getJournalNo();
                    } catch (NullPointerException e) {
                    }

                    try {
                        fileTypeDetDesc = result.getTxReception().getmFileTypeDetail().getDesc();
                    } catch (Exception ex) {}
                    
                    if (result.getmStatus() != null) {
                            data.add(new String[]{
                                    "" + no,
                                    result.getApplicationNo(),
                                    result.getFilingDateTemp(),
                                    sbClassList,
                                    brandType,
                                    brandName,
                                    brmNo,
                                    fileTypeDetDesc
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

    @GetMapping(REQUEST_EXPORT_LAPORAN_ADMINISTRASI_PERMOHONAN)
    public void doExportLapAdministrasiPermohonan(HttpServletRequest request, HttpServletResponse response) {
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
		
		// Get Parameter filter (search) ActionName
		String actionName = "-";
		try {
			actionName = request.getParameter("actionName");
		} catch (Exception e) {
			
		}
		MAction mAction = mActionService.selectOneBy(actionName);
		if(mAction != null) {
			actionName = mAction.getName();
		} else {
			actionName = "-";
		}
		
		// Get Parameter filter (search) StartDate
		String startDate = null;
		try {
			startDate = request.getParameter("startDate");
		} catch (Exception e) {
			
		}
		if(startDate.isEmpty() || startDate.equalsIgnoreCase("undefined")) {
			startDate = "...";
		}
		
		// Get Parameter filter (search) StartDate
		String endDate = null;
		try {
			endDate = request.getParameter("endDate");
		} catch (Exception e) {
			
		}
		if(endDate.isEmpty() || endDate.equalsIgnoreCase("undefined")) {
			endDate = "...";
		}
		
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
                	if(searchBy.equalsIgnoreCase("mAction.id") || searchBy.equalsIgnoreCase("mStatus.id") || searchBy.equalsIgnoreCase("txTmBrand.mBrandType.id") 
                			|| searchBy.equalsIgnoreCase("createdBy.id") && value != null) {
                		if (StringUtils.isNotBlank(value)) {
                    		searchCriteria.add(new KeyValue(searchBy, value, true));
                    	}
                	} else if (searchBy.equalsIgnoreCase("startDate") || searchBy.equalsIgnoreCase("endDate") && value != null) {
                    	if (StringUtils.isNotBlank(value)) {
	                    	try {
	                            searchCriteria.add(new KeyValue(searchBy, DateUtil.toDate("dd/MM/yyyy", value), true));
	                        } catch (ParseException e) {
	                        }
                    	}
                    } else {
                        if (StringUtils.isNotBlank(value)) {
                        	if (searchBy.equalsIgnoreCase("txTmClassList") && value != null) {
                        		searchCriteria.add(new KeyValue(searchBy, value, true));
                        	} else {
                        		searchCriteria.add(new KeyValue(searchBy, value, false));
                        	}
                        }
                    }
                }
            }
        }

        searchCriteria.add(new KeyValue("statusOnOff", StatusEnum.IPT_DRAFT.name(), true));
        
        if (orderBy != null) {
            orderBy = orderBy.trim();
            if (orderBy.equalsIgnoreCase("")) {
                orderBy = null;
            } else {
            	switch (orderBy) {
            	case "1":
                    orderBy = "applicationNo";
                    break;
                case "2":
                    orderBy = "fillingDate";
                    break;
                case "3":
                    orderBy = "mClass.no";
                    break;
                case "4":
                    orderBy = "txTmBrand.mBrandType.name";
                    break;
                case "5":
                    orderBy = "txTmBrand.name";
                    break;
                case "6":
                    orderBy = "txTmOwner.name";
                    break;
                case "7":
                    orderBy = "txReception.mFileTypeDetail.desc";
                    break;
                default:
                    orderBy = "filingDate";
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
            reportInputStream = getClass().getClassLoader().getResourceAsStream("report/laporan-formalitas.xls");
            //List<TxTmGeneral> dataList = new ArrayList<>();
            List<List> dataList = new ArrayList<>();
            List<LapDataFormalitas> list = new ArrayList<>();
            int totalRecord = (int) trademarkService.countAll(searchCriteria);
            int retrieved = 0;
            int limit = 50;
            LapDataFormalitas dataFormalitas = null;
            
            String downloadDate = DateUtil.formatDate(new Timestamp(System.currentTimeMillis()), "dd MMMM YYYY");
            
            int no = 0;
            if (totalRecord > 0) {
                List<TxTmGeneral> retrievedDataList = trademarkService.selectAll(searchCriteria, orderBy, orderType, retrieved, 100000);

                for (TxTmGeneral result : retrievedDataList) {
                    no++;
                    String brandName = "-";
                    String brandType = "-";
                    String brmNo = "-";
                    String fileTypeDetDesc = "-";
                    String sbClassList = "-";
                    ArrayList<String> kelas = new ArrayList<String>();

                    //String sFillingDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(result.getFilingDate().getTime()));

                    try {
                        brandName = result.getTxTmBrand().getName();
                    } catch (NullPointerException e) {
                    }

                    try {
                        brandType = result.getTxTmBrand().getmBrandType().getName();
                    } catch (NullPointerException e) {
                    }

                    for (TxTmClass txTmClass : result.getTxTmClassList()) {
                        kelas.add("" + txTmClass.getmClass().getNo());
                    }
                    Set<String> temp = new LinkedHashSet<String>(kelas);
                    String[] unique = temp.toArray(new String[temp.size()]);
                    if (unique.length > 0) {
                        sbClassList = String.join(",", unique);
                    }
                    
                    
                    try {
                    	TxGroupDetail txGroupDetail = groupService.findTxGroupDetailByTxTmGeneral(result.getId(), "GrupPublikasi");
                        TxPubsJournal txPubsJournal = txPubsJournalRepository.findTxPubsJournalByTxGroup(txGroupDetail.getTxGroup());
                        brmNo = txPubsJournal.getJournalNo();
                    } catch (NullPointerException e) {
                    }

                    try {
                        fileTypeDetDesc = result.getTxReception().getmFileTypeDetail().getDesc();
                    } catch (NullPointerException e) {

                    }

                    dataFormalitas = new LapDataFormalitas();
                    dataFormalitas.setNo(no);
                    dataFormalitas.setApplicationNo(result.getApplicationNo());
                    dataFormalitas.setFillingDate(result.getFilingDateTemp());
                    dataFormalitas.setBrandType(brandType);
                    dataFormalitas.setBrandName(brandName);
                    dataFormalitas.setKelas(sbClassList);
                    dataFormalitas.setBrmNo(brmNo);
                    dataFormalitas.setJenisPermohonan(fileTypeDetDesc);

                    list.add(dataFormalitas);
                    if(list.size() == limit) {
                        dataList.add(list);
                        list = new ArrayList();
                    }
                }
                retrieved += retrievedDataList.size();
            }
            if(list.size() > 0){
                dataList.add(list);
            }
            Context context = new Context();
            context.putVar("dataList", dataList);
            context.putVar("action", actionName);
            context.putVar("startDate", startDate);
            context.putVar("endDate", endDate);
            context.putVar("downloadDate", downloadDate);
            context.putVar("jumlah", no);

            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=laporan-formalitas.xls");

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