package com.docotel.ki.controller.laporan;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.MAction;
import com.docotel.ki.model.master.MStatus;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.transaction.TxMonitor;
import com.docotel.ki.model.transaction.TxTmClass;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.pojo.LapDataKeputusanDirektur;
import com.docotel.ki.service.master.MActionService;
import com.docotel.ki.service.master.StatusService;
import com.docotel.ki.service.master.UserService;
import com.docotel.ki.service.transaction.MonitorService;
import com.docotel.ki.service.transaction.TrademarkService;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.enumeration.StatusEnum;
import com.docotel.ki.pojo.*;
import com.docotel.ki.service.master.*;

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
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class KeputusanDirekturController extends BaseController {
    public static final String PATH_LAPORAN_KEPUTUSAN_DIREKTUR = "/laporan-keputusan-direktur";
    public static final String PATH_AJAX_SEARCH_LIST = "/cari-keputusan-direktur";
    public static final String PATH_PRANTINJAU_PERMOHONAN = "/pratinjau-permohonan";
    private static final String DIRECTORY_PAGE = "laporan/";
    private static final String PATH_EXPORT_DATA_LAPORAN_KEPUTUSAN_DIREKTUR = "/cetak-keputusan-direktur";
    private static final String PAGE_LAPORAN_KEPUTUSAN_DIREKTUR = DIRECTORY_PAGE + "laporan-keputusan-direktur";
    private static final String REQUEST_MAPPING_AJAX_SEARCH_LIST = PATH_AJAX_SEARCH_LIST + "*";
    private static final String REQUEST_MAPPING_LAPORAN_KEPUTUSAN_DIREKTUR = PATH_LAPORAN_KEPUTUSAN_DIREKTUR + "*";
    private static final String REQUEST_EXPORT_LAPORAN_KEPUTUSAN_DIREKTUR = PATH_EXPORT_DATA_LAPORAN_KEPUTUSAN_DIREKTUR + "*";

    @Autowired
    private TrademarkService trademarkService;
    @Autowired
    private StatusService statusService;
    @Autowired
    private MActionService mActionService;
    @Autowired
    private UserService userService;
    @Autowired
    private MonitorService monitorService ;

    /* --------------------------------------- PERMOHONAN SECTION ---------------------------------------------------*/

    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request){
        model.addAttribute("menu","laporan");
        model.addAttribute("subMenu", "laporanKeputusanDirektur");
    }

    @RequestMapping(path = REQUEST_MAPPING_LAPORAN_KEPUTUSAN_DIREKTUR)
    public String doShowLaporanKeputusanDirektur(@RequestParam(value = "error", required = false) String error, Model model, final HttpServletRequest request, final HttpServletResponse response){
        List<UserDetails> mUser = userService.selectListUserByUserType("Karyawan");
        model.addAttribute("mUser", mUser);

        List<MStatus> statusList = statusService.selectStatus();
        model.addAttribute("statusList", statusList);

        List<MAction> actionList = mActionService.findAll();
        model.addAttribute("actionList", actionList);

        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("errorMessage", error);
        }

        return PAGE_LAPORAN_KEPUTUSAN_DIREKTUR;
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

            if (searchResult.getCount() > 0) {
                //System.out.println("Jumlah Line:"+ searchResult.getCount());
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxMonitor result : searchResult.getList()) {
                    // cari model Brand nya
                    no++;
                    String brandName = "-";
                    String brandType = "-";
                    String status = "-";
                    String aksi = "-";
                    String sbClassList = "-";
                    String brmNo = "-";
                    ArrayList<String> kelass = new ArrayList<String>();

                    try {
                        brandName = result.getTxTmGeneral().getTxTmBrand().getName();
                    } catch (NullPointerException e) {
                    }

                    try {
                        brandType = result.getTxTmGeneral().getTxTmBrand().getmBrandType().getName();
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

                  /*  try {
                        TxGroupDetail txGroupDetail = groupService.findTxGroupDetailByTxTmGeneral(result.getTxTmGeneral().getId(), "GrupPublikasi");
                        TxPubsJournal txPubsJournal = txPubsJournalRepository.findTxPubsJournalByTxGroup(txGroupDetail.getTxGroup());
                        brmNo = txPubsJournal.getJournalNo();
                    } catch (NullPointerException e) {

                    }*/

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

                 /*   try {
                        TxGroupDetail txGroupDetail = groupService.findTxGroupDetailByTxTmGeneral(result.getId(), "GrupPublikasi");
                        TxPubsJournal txPubsJournal = txPubsJournalRepository.findTxPubsJournalByTxGroup(txGroupDetail.getTxGroup());
                        brmNo = txPubsJournal.getJournalNo();
                    } catch (NullPointerException e) {

                    }*/


                    String data_applicationNo = result.getTxTmGeneral().getApplicationNo();
                    String data_filling_date = result.getCreatedDate().toString();

                    // if (result.getTxTmGeneral().getmStatus() != null) {
                    if (status != null) {

                        data.add(new String[]{
                                "" + no,
                                data_applicationNo, // Nomor Permohonan
                                data_filling_date,
                                sbClassList,
                                brandType,
                                brandName

                                // brmNo,
                                // status,
                                // aksi

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

  
    @GetMapping(REQUEST_EXPORT_LAPORAN_KEPUTUSAN_DIREKTUR)
    public void doExportLapKeputusanDirektur(HttpServletRequest request, HttpServletResponse response){

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

          // Get Parameter filter (search) statusName
           String statusName = null;
           try {
               statusName = request.getParameter("statusName");
           } catch (Exception e) {
           }
           MStatus mStatus = statusService.selectOneBy(statusName);
           if(mStatus != null) {
               statusName = mStatus.getName();
           } else {
               statusName = "-";
           }

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
                               || searchBy.equalsIgnoreCase("createdBy.id")) {
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

        /*   searchCriteria.add(new KeyValue("statusOnOff", StatusEnum.IPT_DRAFT.name(), true));

           if (orderBy != null) {
               orderBy = orderBy.trim();
               if (orderBy.equalsIgnoreCase("")) {
                   orderBy = null;
               } else {
                   switch (orderBy) {
                       case "1":
                           orderBy = "filingDate";
                           break;
                       case "2":
                           orderBy = "applicationNo";
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
           }*/

           try {
               reportInputStream = getClass().getClassLoader().getResourceAsStream("report/laporan-TU.xls");
               //List<TxTmGeneral> dataList = new ArrayList<>();
               List<List> dataList = new ArrayList<>();
               List<LapDataKeputusanDirektur> list = new ArrayList<>();
               int retrieved = 0;
               GenericSearchWrapper<TxMonitor> searchResult = monitorService.searchMonitorKeputusanDirektur(searchCriteria, "createdDate", orderType, retrieved, 1000);
               //int totalRecord = (int) trademarkService.countAll(searchCriteria); 
               int limit = 50;
               LapDataKeputusanDirektur dataKeputusanDirektur = null;
               int totalRecord = (int) searchResult.getCount();

               int no = 0;
               if (totalRecord > 0) {
                  // List<TxTmGeneral> retrievedDataList = trademarkService.selectAll(searchCriteria, orderBy, orderType, retrieved, 1000);

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

                       dataKeputusanDirektur = new LapDataKeputusanDirektur();
                       dataKeputusanDirektur.setNo(no);
                       dataKeputusanDirektur.setApplicationNo(result.getApplicationNo());
                       dataKeputusanDirektur.setFillingDate(result.getFilingDateTemp());
                       dataKeputusanDirektur.setKelas(sbClassList);
                       dataKeputusanDirektur.setBrandType(brandType);
                       dataKeputusanDirektur.setBrandName(brandName);
                       list.add(dataKeputusanDirektur);
                        
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

               Date date = new Date();
               String downlodDate = new SimpleDateFormat("dd-MM-yyyy").format(date);

               Context context = new Context();
               context.putVar("dataList", dataList);
               context.putVar("statusName", statusName);
               context.putVar("actionName", actionName);
               context.putVar("downloadDate", downlodDate);

               response.setContentType("application/vnd.ms-excel");
               response.setHeader("Content-Disposition", "attachment; filename=laporan-TU.xls");

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
