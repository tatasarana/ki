package com.docotel.ki.controller;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.MStatus;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.service.master.StatusService;
import com.docotel.ki.service.transaction.PermohonanOnlineService;
import com.docotel.ki.service.transaction.TrademarkService;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.util.NumberUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class CekPermohonanOnlineController extends BaseController {
    @Autowired
    private TrademarkService trademarkService;

    @Value("${logo.qr.pengayoman}")
    private String logoQRPengayoman;

    @Autowired
    private PermohonanOnlineService permohonanOnlineService;

    @Autowired
    private StatusService statusService;

    private static final String DIRECTORY_PAGE = "cek-permohonan/";

    private static final String PAGE_DETAIL_CEK_PERMOHONAN_ONLINE = DIRECTORY_PAGE + "detail-cek-permohonan-online";
    private static final String PAGE_PINDAH_PERMOHONAN = DIRECTORY_PAGE + "pindah-permohonan";
    private static final String PAGE_CEK_PERMOHONAN_ONLINE = DIRECTORY_PAGE + "cek-permohonan-online";

    public static final String PATH_AJAX_SEARCH_LIST = "/cari-cek-permohonan-online";
    public static final String PATH_AJAX_EXPORT_LIST = "/export-cek-permohonan-online";
    public static final String PATH_DETAIL_CEK_PERMOHONAN_ONLINE = "/detail-cek-permohonan-online";
    public static final String PATH_PINDAH_PERMOHONAN = "/pindah-permohonan-online";
    public static final String PATH_CEK_PERMOHONAN_ONLINE = "/cek-permohonan-online";
    public static final String PATH_PRANTINJAU_PERMOHONAN = "/pratinjau-permohonan";

    public static final String REDIRECT_CEK_PERMOHONAN_ONLINE = "redirect:" + PATH_AFTER_LOGIN + PATH_CEK_PERMOHONAN_ONLINE;
    public static final String REDIRECT_PINDAH_PERMOHONAN = "redirect:" + PATH_AFTER_LOGIN + PATH_PINDAH_PERMOHONAN;

    private static final String REQUEST_MAPPING_AJAX_SEARCH_LIST = PATH_AJAX_SEARCH_LIST + "*";
    private static final String REQUEST_MAPPING_AJAX_EXPORT_LIST = PATH_AJAX_EXPORT_LIST + "*";

    private static final String REQUEST_MAPPING_DETAIL_CEK_PERMOHONAN_ONLINE = PATH_DETAIL_CEK_PERMOHONAN_ONLINE + "*";
    private static final String REQUEST_MAPPING_PINDAH_PERMOHONAN = PATH_PINDAH_PERMOHONAN + "*";

    private static final String REQUEST_MAPPING_CEK_PERMOHONAN_ONLINE = PATH_CEK_PERMOHONAN_ONLINE + "*";
//	private static final String REQUEST_MAPPING_HAPUS_CEK_PERMOHONAN_ONLINE = PATH_HAPUS_CEK_PERMOHONAN_ONLINE + "*";

    /***************************** - CEK PERMOHONAN ONLINE SECTION - ****************************/

    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "cekPermohonan");
        model.addAttribute("subMenu", "cekPermohonanOnline");

        List<MStatus> statusList = statusService.selectStatus();
        model.addAttribute("statusList", statusList);
    }

    @GetMapping(path = REQUEST_MAPPING_CEK_PERMOHONAN_ONLINE)
    public String doShowPageCekPermohonanOnline(@RequestParam(value = "error", required = false) String error, final Model model, final HttpServletRequest request, final HttpServletResponse response) {
        model.addAttribute("subMenu", "cekPermohonanOnline");

        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("errorMessage", error);
        }

        return PAGE_CEK_PERMOHONAN_ONLINE;
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

//			searchCriteria.add(new KeyValue("onlineFlag", Boolean.FALSE, true));

            String orderBy = request.getParameter("order[0][column]");
            if (orderBy != null) {
                orderBy = orderBy.trim();
                if (orderBy.equalsIgnoreCase("")) {
                    orderBy = null;
                } else {
                    switch (orderBy) {
                        case "1":
                            orderBy = "txReception.eFilingNo";
                            break;
                        case "2":
                            orderBy = "applicationNo";
                            break;
                        case "3":
                            orderBy = "filingDate";
                            break;
                        case "4":
                            orderBy = "txReception.mFileType.desc";
                            break;
                        case "5":
                            orderBy = "txReception.mFileTypeDetail.desc";
                            break;
                        case "6":
                            orderBy = "txReception.bankCode";
                            break;
                        case "7":
                            orderBy = "txReception.totalClass";
                            break;
                        case "8":
                            orderBy = "txReception.paymentDate";
                            break;
                        case "9":
                            orderBy = "txReception.totalPayment";
                            break;
                        case "10":
                            orderBy = "mStatus.name";
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

                    String appNo = "";
                    try {
                        appNo = result.getApplicationNo();
                    } catch (NullPointerException e) {
                    } catch (Exception e) {
                    }

                    String mStatus_id = "";
                    try {
                        mStatus_id = result.getmStatus().getId();
                    } catch (NullPointerException e) {
                    } catch (Exception e) {
                    }
                    MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    String tombolHapus = "";
                    String tombolPindahPermohonan ="";

                    if(mUser.hasAccessMenu("T-HPSPND")) {
                        tombolHapus = mStatus_id.equals("IPT_DRAFT") ?
                                "<a class=\"btn btn-xs btn-danger\" onclick=\"hapusPermohonan('" + getPathURLAfterLogin(PATH_DETAIL_CEK_PERMOHONAN_ONLINE + "?no=" + appNo) + "', '" + appNo + "')\">Hapus</a>" :
//                                mStatus_id.equals("IPT_PENGAJUAN_PERMOHONAN") ?
//                                        "<a class=\"btn btn-xs btn-danger\" onclick=\"hapusPermohonan('" + getPathURLAfterLogin(PATH_DETAIL_CEK_PERMOHONAN_ONLINE + "?no=" + appNo) + "', '" + appNo + "')\">Hapus</a>" :
                                      "";
                    }

                    if(mUser.hasAccessMenu("T-PNDPOL") && !mStatus_id.equals("IPT_DRAFT")) {
                        tombolPindahPermohonan = "<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_PINDAH_PERMOHONAN + "?no=" + appNo) + "\">Pindah</a>";
                    }
                    String nomorTransaksi = "";
                    try {
                        nomorTransaksi = result.getTxReception().geteFilingNo();
                    } catch (NullPointerException e) {
                    } catch (Exception e) {
                    }

                    String filingDate = "";
                    try {
                        filingDate = result.getFilingDateTemp();
                    } catch (NullPointerException e) {
                    } catch (Exception e) {
                    }

                    String tglBayar = "";
                    try {
                        tglBayar = result.getTxReception().getPaymentDateTemp();
                    } catch (NullPointerException e) {
                    } catch (Exception e) {
                    }

                    String totalPayment = "0";
                    try {
                        totalPayment = NumberUtil.formatInteger(result.getTxReception().getTotalPayment());
                    } catch (NullPointerException e) {
                    } catch (Exception e) {
                    }

                    String tipe = "";
                    try {
                        tipe = result.getTxReception().getmFileType().getDesc();
                    } catch (NullPointerException e) {
                    } catch (Exception e) {
                    }

                    String jenis = "";
                    try {
                        jenis = result.getTxReception().getmFileTypeDetail().getDesc();
                    } catch (NullPointerException e) {
                    } catch (Exception e) {
                    }

                    String kodeBilling = "";
                    try {
                        kodeBilling = result.getTxReception().getBankCode();
                    } catch (NullPointerException e) {
                    } catch (Exception e) {
                    }

                    String jumlahKelas = "";
                    try {
                        jumlahKelas = result.getTxReception().getTotalClassTemp();
                    } catch (NullPointerException e) {
                    } catch (Exception e) {
                    }

                    data.add(new String[]{
                            "" + no,
                            nomorTransaksi,
                            "<a  target=\"_blank\" href=\"" + getPathURLAfterLogin(PATH_PRANTINJAU_PERMOHONAN + "?no=" + appNo) + "\">" + appNo + "</a>",
                            filingDate,
                            tipe,
                            jenis,
                            kodeBilling,
                            jumlahKelas,
                            tglBayar,
                            "Rp. " + totalPayment,
                            result.getmStatus().getName(),
                            "<div class=\"btn-actions\">" +
                                    (tombolHapus == "" ? "" : tombolHapus + "<br/>") +
                                    tombolPindahPermohonan +
                                    "</div>"
                    });
                }
            }
            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    //export to excel
    @RequestMapping(value = REQUEST_MAPPING_AJAX_EXPORT_LIST, method = {RequestMethod.POST})
    public void doExportDataTablesList(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            DataTablesSearchResult dataTablesSearchResult = new DataTablesSearchResult();
            try {
                dataTablesSearchResult.setDraw(Integer.parseInt(request.getParameter("draw")));
            } catch (NumberFormatException e) {
                dataTablesSearchResult.setDraw(0);
            }

            int offset = 0;
            int limit = 100000;
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

            String orderBy = request.getParameter("orderBy");
            if (orderBy != null) {
                orderBy = orderBy.trim();
                if (orderBy.equalsIgnoreCase("")) {
                    orderBy = null;
                }
            }

            String orderType = request.getParameter("orderType");
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
                    data.add(new String[]{
                            "" + no,
                            result.getTxReception().geteFilingNo() == null ? "-" : result.getTxReception().geteFilingNo(),
                            "<a href=\"" + getPathURLAfterLogin(PATH_DETAIL_CEK_PERMOHONAN_ONLINE + "?no=" + result.getApplicationNo()) + "\">" + result.getApplicationNo() + "</a>",
                            result.getFilingDateTemp() == null ? "-" : result.getFilingDateTemp(),
                            result.getTxReception().getmFileTypeDetail() != null ? result.getTxReception().getmFileTypeDetail().getDesc() : "-",
                            result.getTxReception().getBankCode() != null ? result.getTxReception().getBankCode() : "-",
                            result.getTxReception().getPaymentDateTemp() != null ? result.getTxReception().getPaymentDateTemp() : "-",
                            NumberUtil.formatInteger(result.getTxReception().getTotalPayment()) == null ? "Rp. 0" : "Rp. " + NumberUtil.formatInteger(result.getTxReception().getTotalPayment()),
                    });
                }
            }
            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }
    //end
    /***************************** - END CEK PERMOHONAN ONLINE SECTION - ****************************/


    /***************************** - DETAIL CEK PERMOHONAN ONLINE SECTION - ****************************/
    @GetMapping(REQUEST_MAPPING_DETAIL_CEK_PERMOHONAN_ONLINE)
    public String doShowPageDetailCekPermohonanOnline(final Model model, final HttpServletRequest request) {
        if (doInitiateEditCekPermohonanOnline(model, request)) {
            return PAGE_DETAIL_CEK_PERMOHONAN_ONLINE;
        }

        return REDIRECT_CEK_PERMOHONAN_ONLINE + "?error=Nomor permohonan " + request.getParameter("no") + " tidak ditemukan";
    }

    /***************************** - END DETAIL CEK PERMOHONAN ONLINE SECTION - ****************************/

    @ModelAttribute
    private boolean doInitiateEditCekPermohonanOnline(final Model model, final HttpServletRequest request) {
        model.addAttribute("subMenu", "cekPermohonanOnline");

        TxTmGeneral existing = trademarkService.selectOneGeneralByApplicationNo(request.getParameter("no"));

        if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
            model.addAttribute("form", existing);
        } else {
            model.addAttribute("existing", existing);
        }

        return existing != null;
    }

    /***************************** - PINDAH PERMOHONAN SECTION - ****************************/
    @GetMapping(REQUEST_MAPPING_PINDAH_PERMOHONAN)
    public String doShowPagePindahPermohonan(final Model model, final HttpServletRequest request) {
        if (doInitiatePindahPermohonan(model, request)) {
            return PAGE_PINDAH_PERMOHONAN;
        }

        return REDIRECT_PINDAH_PERMOHONAN + "?error=Nomor permohonan " + request.getParameter("no") + " tidak ditemukan";
    }

    /***************************** - END PINDAH PERMOHONAN SECTION - ****************************/

    @ModelAttribute
    private boolean doInitiatePindahPermohonan(final Model model, final HttpServletRequest request) {
        model.addAttribute("subMenu", "cekPermohonanOnline");

        TxTmGeneral existing = trademarkService.selectOneGeneralByApplicationNo(request.getParameter("no"));

        //if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
            model.addAttribute("form", existing);
        //} else {
            model.addAttribute("existing", existing);
        //}

        return existing != null;
    }

}