package com.docotel.ki.controller;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.*;
import com.docotel.ki.model.transaction.TxPostReception;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.transaction.TxPostReceptionCustomRepository;
import com.docotel.ki.repository.master.MFileTypeRepository;
import com.docotel.ki.service.SimpakiService;
import com.docotel.ki.service.master.FileService;
import com.docotel.ki.service.master.StatusService;
import com.docotel.ki.service.master.UserService;
import com.docotel.ki.service.transaction.PascaOnlineService;
import com.docotel.ki.service.transaction.PermohonanOnlineService;
import com.docotel.ki.service.transaction.TrademarkService;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.util.NumberUtil;
import com.docotel.ki.util.ZxingUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class CekPascaPermohonanController extends BaseController {
    @Autowired private TrademarkService trademarkService;
    @Autowired private FileService fileService;
    @Autowired private TxPostReceptionCustomRepository txPostReceptionCustomRepository;
    @Autowired private SimpakiService simpakiService;
    @Autowired private MFileTypeRepository mFileTypeRepository;
    @Autowired private UserService userService;
    @Autowired private PermohonanOnlineService permohonanOnlineService;
    @Autowired
    private PascaOnlineService pascaOnlineService;
    @Autowired
    private StatusService statusService;

    @Value("${logo.qr.pengayoman}")
    private String logoQRPengayoman;


    private static final String DIRECTORY_PAGE = "cek-permohonan/";

    private static final String PAGE_CETAK_CEK_PASCA_PERMOHONAN = DIRECTORY_PAGE + "cetak-cek-pasca-permohonan";
    private static final String PAGE_EDIT_CEK_PASCA_PERMOHONAN = DIRECTORY_PAGE + "edit-cek-pasca-permohonan";
    private static final String PAGE_CEK_PASCA_PERMOHONAN = DIRECTORY_PAGE + "cek-pasca-permohonan";

    public static final String PATH_AJAX_SEARCH_LIST_CEK_PASCA_PERMOHONAN = "/cari-cek-pasca-permohonan";
    public static final String PATH_AJAX_EXPORT_LIST_CEK_PASCA_PERMOHONAN = "/export-cek-pasca-permohonan";
    public static final String PATH_CETAK_CEK_PASCA_PERMOHONAN = "/cetak-cek-pasca-permohonan";
    public static final String PATH_EDIT_CEK_PASCA_PERMOHONAN = "/edit-cek-pasca-permohonan";
    public static final String PATH_CEK_PASCA_PERMOHONAN = "/cek-pasca-permohonan";
    public static final String PATH_SIMPAN_CEK_PASCA_PERMOHONAN = "/simpan-cek-pasca-permohonan";

    public static final String REDIRECT_CETAK_CEK_PASCA_PERMOHONAN = "redirect:" + PATH_AFTER_LOGIN + PATH_CETAK_CEK_PASCA_PERMOHONAN;
    public static final String REDIRECT_CEK_PASCA_PERMOHONAN = "redirect:" + PATH_AFTER_LOGIN + PATH_CEK_PASCA_PERMOHONAN;

    private static final String REQUEST_MAPPING_AJAX_SEARCH_LIST_CEK_PASCA_PERMOHONAN = PATH_AJAX_SEARCH_LIST_CEK_PASCA_PERMOHONAN + "*";
    private static final String REQUEST_MAPPING_AJAX_EXPORT_LIST_CEK_PASCA_PERMOHONAN = PATH_AJAX_EXPORT_LIST_CEK_PASCA_PERMOHONAN + "*";

    private static final String REQUEST_MAPPING_CETAK_CEK_PASCA_PERMOHONAN = PATH_CETAK_CEK_PASCA_PERMOHONAN + "*";
    private static final String REQUEST_MAPPING_EDIT_CEK_PASCA_PERMOHONAN = PATH_EDIT_CEK_PASCA_PERMOHONAN + "*";
    private static final String REQUEST_MAPPING_CEK_PASCA_PERMOHONAN = PATH_CEK_PASCA_PERMOHONAN + "*";

    private static final String REQUEST_MAPPING_SIMPAN_CEK_PASCA_PERMOHONAN = PATH_SIMPAN_CEK_PASCA_PERMOHONAN + "*";

    private static final String PATH_PASCA_PERMOHONAN_PRATINJAU = "/pratinjau-pasca-permohonan";

    /***************************** - CEK PASCA PERMOHONAN SECTION - ****************************/

    @GetMapping(path = REQUEST_MAPPING_CEK_PASCA_PERMOHONAN)
    public String doShowPageCekPascaPermohonan(@RequestParam(value = "error", required = false) String error, final Model model, final HttpServletRequest request, final HttpServletResponse response) {
        model.addAttribute("menu", "cekPermohonan");
        model.addAttribute("subMenu", "cekPascaPermohonan");

        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("errorMessage", error);
        }

        return PAGE_CEK_PASCA_PERMOHONAN;
    }

    @RequestMapping(value = REQUEST_MAPPING_AJAX_SEARCH_LIST_CEK_PASCA_PERMOHONAN, method = {RequestMethod.POST})
    public void doSearchDataTablesListCekPascaPermohonan(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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
            List<KeyValue> searchCriteria;

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
                        if (searchBy.equalsIgnoreCase("postDate")) {
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

//            searchCriteria.add(new KeyValue("onlineFlag", Boolean.FALSE, true));

            String orderBy = request.getParameter("order[0][column]");
            if (orderBy != null) {
                orderBy = orderBy.trim();
                if (orderBy.equalsIgnoreCase("")) {
                    orderBy = null;
                } else {
                    switch (orderBy) {
                        case "1":
                            orderBy = "eFilingNo";
                            break;
                        case "2":
                            orderBy = "postDate";
                            break;
                        case "3":
                            orderBy = "postNo";
                            break;
                        case "4":
                            orderBy = "mFileType.desc";
                            break;
                        case "5":
                            orderBy = "billingCode";
                            break;
                        case "6":
                            orderBy = "paymentDate";
                            break;
                        case "7":
                            orderBy = "totalPayment";
                            break;
                        case "8":
                            orderBy = "mStatus.name";
                            break;

                        default:
                            orderBy = "postDate";
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

            GenericSearchWrapper<TxPostReception> searchResult = pascaOnlineService.searchPostReception(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxPostReception result : searchResult.getList()) {
                    no++;

                    String postDate = "";
                    try {
                        postDate = result.getPostDateTemp();
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

                    String tombolPindahPermohonan ="";
                    String tombolHapus = "";
                    if(mUser.hasAccessMenu("T-HPSPASCA") && mStatus_id.equals("IPT_DRAFT")) {
                        tombolHapus ="<a class=\"btn btn-danger btn-xs\" onclick=\"modalDelete('" + result.getId() + "');\">Hapus</a>";
                    }
                    data.add(new String[]{
                            "" + no,
                            "<a target=\"_blank\" href=\"" + getPathURLAfterLogin(PATH_PASCA_PERMOHONAN_PRATINJAU + "?no=" + result.getId()) + "\">" + result.geteFilingNo() + "</a>",
                            postDate,
                            result.getPostNo(),
                            result.getmFileType().getDesc(),
                            result.getBillingCode(),
                            result.getPaymentDateTemp(),
                            "Rp. " + NumberUtil.formatInteger(result.getTotalPayment()),
                            result.getmStatus().getName(),
                            "<div class=\"btn-actions\">" +
                                    (tombolHapus == "" ? "" : tombolHapus )
                                    + "</div>"
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
    @RequestMapping(value = REQUEST_MAPPING_AJAX_EXPORT_LIST_CEK_PASCA_PERMOHONAN, method = {RequestMethod.POST})
    public void doExportDataTablesListCekPascaPermohonan(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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
                        if (searchBy.equalsIgnoreCase("postDate")) {
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

            GenericSearchWrapper<TxPostReception> searchResult = pascaOnlineService.searchPostReception(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxPostReception result : searchResult.getList()) {
                    no++;
                    data.add(new String[]{
                            "" + no,
                            "<a href=\"" + getPathURLAfterLogin(PATH_CETAK_CEK_PASCA_PERMOHONAN + "?no=" + result.getPostNo()) + "\">" + result.getPostNo() + "</a>",
                            result.getPostDateTemp(),
                            result.getPostNo(),
                            result.getmFileTypeDetail().getDesc(),
                            result.getBillingCode(),
                            result.getPaymentDateTemp(),
                            "Rp. " + NumberUtil.formatInteger(result.getTotalPayment())
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
    /***************************** - END CEK PASCA PERMOHONAN SECTION - ****************************/


    /***************************** - CETAK CEK PASCA PERMOHONAN SECTION - ****************************/
    @GetMapping(REQUEST_MAPPING_CETAK_CEK_PASCA_PERMOHONAN)
    public String doShowPageCetakCekPascaPermohonan(@RequestParam(value = "no", required = false) String postNo, final Model model) {
        model.addAttribute("subMenu", "cekPascaPermohonan");

        TxPostReception txPostReception = null;
        if (!StringUtils.isEmpty(postNo)) {
            txPostReception = trademarkService.selectOneReceptionByPostNo(postNo);
        }

        if (txPostReception == null) {
            return REDIRECT_CEK_PASCA_PERMOHONAN + "?error=Nomor permohonan " + postNo + " tidak ditemukan";
        }

        String qrText = txPostReception.getQrText();
        String qrCode = "data:image/png;base64, ";

        byte[] qrData = ZxingUtil.textToQrCode(qrText, new File(logoQRPengayoman), 125, 125, 30, 30);
//		byte[] qrData = ZxingUtil.textToQrCode(qrText, 300, 300);
        if (qrData != null) {
            qrCode += Base64.getEncoder().encodeToString(qrData);
        }

        model.addAttribute("qrText", qrText);
        model.addAttribute("qrCode", qrCode);

        return PAGE_CETAK_CEK_PASCA_PERMOHONAN;
    }

    /***************************** - END CETAK CEK PASCA PERMOHONAN SECTION - ****************************/

    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "cekPascaPermohonan");
        List<KeyValue> searchCriteria = new ArrayList<>();
        searchCriteria.add(new KeyValue("menu", "DAFTAR", true));
        searchCriteria.add(new KeyValue("statusFlag", true, true));

        if (!request.getRequestURI().contains(PATH_CETAK_CEK_PASCA_PERMOHONAN)) {

            List<MFileTypeDetail> fileTypeDetailList = fileService.findMFileTypeDetailByStatusFlagTrue();
            model.addAttribute("fileTypeDetailList", fileTypeDetailList);

            List<MFileSequence> fileSequenceList = fileService.findMFileSequenceByStatusFlagTrue();
            model.addAttribute("fileSequenceList", fileSequenceList);

            List<MFileType> fileTypeList = fileService.findByMenu("PASCA");
            model.addAttribute("fileTypeList", fileTypeList);

            List<MStatus> statusList = statusService.selectStatus();
            model.addAttribute("statusList", statusList);
        }
    }

}
