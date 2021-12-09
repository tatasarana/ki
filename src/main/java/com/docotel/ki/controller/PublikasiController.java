package com.docotel.ki.controller;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.MLaw;
import com.docotel.ki.model.master.MLookup;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.pojo.DataBRM;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.service.master.EmployeeService;
import com.docotel.ki.service.master.LawService;
import com.docotel.ki.service.master.LookupService;
import com.docotel.ki.service.transaction.GrupPermohonanService;
import com.docotel.ki.service.transaction.TrademarkService;
import com.docotel.ki.signature.PDFSignatureFacade;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.util.FieldValidationUtil;
import com.docotel.ki.util.ZxingUtil;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.LazyInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class PublikasiController extends BaseController {
    public static final String PATH_AJAX_SEARCH_LIST = "/cari-publikasi";
    public static final String PATH_AJAX_SEARCH_DETAIL_LIST = "/cari-xpublikasi-detail";
    public static final String PATH_AJAX_SEARCH_DETAIL_LIST_OPT = "/cari-publikasi-detail-opt";
    public static final String PATH_PUBLIKASI = "/publikasi";
    public static final String PATH_HAPUS_PUBLIKASI = "/publikasi-hapus";
    public static final String PATH_TAMBAH_PUBLIKASI = "/publikasi-buat";
    public static final String REDIRECT_PUBLIKASI = "redirect:" + PATH_AFTER_LOGIN + PATH_PUBLIKASI;
    private static final String DIRECTORY_PAGE = "publikasi/";
    private static final String PAGE_PUBLIKASI = DIRECTORY_PAGE + "publikasi";
    private static final String PAGE_PUBLIKASI_DETAIL = DIRECTORY_PAGE + "publikasi-detail";
    private static final String PAGE_TAMBAH_PUBLIKASI = DIRECTORY_PAGE + "publikasi-buat";
    private static final String PATH_CETAK_PUBLIKASI = "/" + DIRECTORY_PAGE + "publikasi-cetak";
    private static final String PATH_DETAIL_PUBLIKASI = "/" + DIRECTORY_PAGE + "publikasi-detail";
    private static final String REQUEST_MAPPING_AJAX_SEARCH_LIST = PATH_AJAX_SEARCH_LIST + "*";
    private static final String REQUEST_MAPPING_AJAX_DETAIL_CARI_PUBLIKASI = PATH_AJAX_SEARCH_DETAIL_LIST + "*";
    private static final String REQUEST_MAPPING_AJAX_DETAIL_CARI_PUBLIKASI_OPT = PATH_AJAX_SEARCH_DETAIL_LIST_OPT + "*";
    private static final String REQUEST_MAPPING_PUBLIKASI = PATH_PUBLIKASI + "*";
    private static final String REQUEST_MAPPING_TAMBAH_BRM = PATH_TAMBAH_PUBLIKASI + "*";
    private static final String REQUEST_MAPPING_HAPUS_BRM = PATH_HAPUS_PUBLIKASI + "*";
    private static final String REQUEST_MAPPING_CETAK_PUBLIKASI = PATH_CETAK_PUBLIKASI + "*";
    private static final String REQUEST_MAPPING_DETAIL_PUBLIKASI = PATH_DETAIL_PUBLIKASI + "*";
    @Autowired
    private TrademarkService trademarkService;
    @Autowired
    private LawService lawService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private GrupPermohonanService groupService;
    @Autowired
    private LookupService lookupService;
    @Autowired
    ResourceLoader resourceLoader;
    @Value("${upload.file.brand.path:}")
    private String uploadFileBrandPath;
    @Value("${upload.file.web.image:}")
    private String pathImage;
    @Value("${logo.qr.pengayoman}")
    private String logoQRPengayoman;
    @Value("${upload.file.path.signature:}")
    private String uploadFilePathSignature;
    @Value(("${certificate.file}"))
    private String CERTIFICATE_FILE;

    public static List<Date> getDatesBetweenUsingJava7(Date startDate, Date endDate) {
        List<Date> datesInRange = new ArrayList<>();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(startDate);

        Calendar endCalendar = new GregorianCalendar();
        endCalendar.setTime(endDate);

        while (calendar.before(endCalendar)) {
            Date result = calendar.getTime();
            datesInRange.add(result);
            calendar.add(Calendar.DATE, 1);
        }
        return datesInRange;
    }

    @GetMapping(path = REQUEST_MAPPING_PUBLIKASI)
    public String doShowPagePublikasi(@RequestParam(value = "error", required = false) String error, final Model model, final HttpServletRequest request, final HttpServletResponse response) {
        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("errorMessage", error);
        }

        model.addAttribute("listMLaw", lawService.findAll());

        return PAGE_PUBLIKASI;
    }

    /***************************** - TAMBAH PUBLIKASI SECTION - ****************************/
    @GetMapping(REQUEST_MAPPING_TAMBAH_BRM)
    public String doShowPagePublikasibrm(@RequestParam(value = "error", required = false) String error, final Model model, final HttpServletRequest request, final HttpServletResponse response) {
        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("errorMessage", error);
        }
        model.addAttribute("subMenu", "publikasiBaru");

        model.addAttribute("form", new TxPubsJournal());

        List<MLaw> listMLaw = lawService.findByStatusFlagTrue();

        List<TxGroup> txGroupList = groupService.selectAllListTxGroup();
        model.addAttribute("listMLaw", listMLaw);
        model.addAttribute("listTxGroup", txGroupList);

        ArrayList<String> month_ = new ArrayList<String>();
        month_.add("1");
        month_.add("2");
        month_.add("3");
        model.addAttribute("validityMonth", month_);

        List<MLookup> mLookupListJenisBRM = lookupService.selectAllbyGroup("JenisBRM");
        model.addAttribute("listJenisBRM", mLookupListJenisBRM);

        /*List<MEmployee> ListEmployee = employeeService.findAllMEmployee();
        model.addAttribute("listEmployee", ListEmployee);*/

        return PAGE_TAMBAH_PUBLIKASI;
    }

    @PostMapping(REQUEST_MAPPING_PUBLIKASI)
    public String doTambahPublikasi(@ModelAttribute("form") TxPubsJournal form, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
        model.addAttribute("subMenu", "publikasi");

        //FieldValidationUtil.required(errors, "mFileSequence.id", form.getmFileSequence().getCurrentId(), "asal permohonan");
        FieldValidationUtil.required(errors, "journalNo", form.getJournalNo(), "No BRM");
        //FieldValidationUtil.required(errors, "journalNo", form.getJournalNo(), "No BRM");
        FieldValidationUtil.required(errors, "journalType", form.getJournalType(), "Jenis BRM");

        TxPubsJournal txPubsJournal = trademarkService.selectOnePubsJournal("journalNo", form.getJournalNo());

        if (!errors.hasErrors()) {
            form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

            try {
                if (txPubsJournal != null) {
                    model.addAttribute("errorMessage", "Gagal menambahkan Publikasi - Nomor BRM " + form.getJournalNo() + " Sudah Ada");
                } else {
                    form.setValidityPeriod(Integer.valueOf(request.getParameter("validityPeriod")));
                    String c = trademarkService.insertPublikasi(form);
                    if (c == "FAILED") {
                        return "redirect:" + BaseController.PATH_AFTER_LOGIN + PATH_TAMBAH_PUBLIKASI;

                    }

                    return PAGE_PUBLIKASI;
                }
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                if (e.getMessage().startsWith(HttpStatus.BAD_REQUEST.getReasonPhrase())) {
                    if (e.getMessage().endsWith("mLaw")) {
                        errors.rejectValue("mLaw.id", "field.error.invalid.value", new Object[]{"Undang-undang"}, "");
                    }
                } else {
                    model.addAttribute("errorMessage", "Gagal menambahkan publikasi - Kode " + form.getJournalNo() + " Sudah Ada");
                }
            }
        }
        return doShowPagePublikasibrm("", model, request, response);
        //return PAGE_TAMBAH_PUBLIKASI;
    }

    @RequestMapping(value = REQUEST_MAPPING_HAPUS_BRM, method = RequestMethod.GET)
    public void doHapusBRM(Model model, @RequestParam("id") String id, final HttpServletRequest request, final HttpServletResponse response) {
        trademarkService.deletePublikasi(id);
    }

    /***************************** - END TAMBAH PUBLIKASI SECTION - ****************************/

    @RequestMapping(value = REQUEST_MAPPING_AJAX_DETAIL_CARI_PUBLIKASI_OPT, method = {RequestMethod.POST})
    public void doSearchDataTablesListDetailOpt(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        String journalNo = "";
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            DataTablesSearchResult dataTablesSearchResult = new DataTablesSearchResult();
            try {
                dataTablesSearchResult.setDraw(Integer.parseInt(request.getParameter("draw")));
            } catch (NumberFormatException e) {
                dataTablesSearchResult.setDraw(0);
            }

            String[] searchByArr = request.getParameterValues("searchByArr[]");
            String[] keywordArr = request.getParameterValues("keywordArr[]");

            if (searchByArr != null) {
                for (int i = 0; i < searchByArr.length; i++) {
                    String searchBy = searchByArr[i];
                    String value = null;
                    try {
                        value = keywordArr[i];
                    } catch (ArrayIndexOutOfBoundsException e) {
                    }
                    if (searchBy != null && searchBy.equalsIgnoreCase("journalNo")) {
                        journalNo = value;
                    }
                }
            }

            GenericSearchWrapper<Object[]> searchResult = trademarkService.searchGroupDetailByJournalNo(journalNo);
            if (searchResult.getList().size() > 0) {
                List<String[]> dataList = new ArrayList<>();
                int no = 0;
                for (Object[] result : searchResult.getList()) {
                    no++;
                    dataList.add(new String[]{
                            "" + no,
                            (result[0] == null) ? "-" : result[0].toString(),
                            (result[1] == null) ? "-" : result[1].toString(),
                            (result[2] == null) ? "-" : result[2].toString(),
                            (result[3] == null) ? "-" : result[3].toString(),
                            (result[4] == null) ? "-" : result[4].toString(),
                            (result[5] == null) ? "-" : result[5].toString(),
                            (result[6] == null) ? "-" : result[6].toString()
                    });
                }
                dataTablesSearchResult.setData(dataList);
                writeJsonResponse(response, dataTablesSearchResult);
                return;
            }
        }
        response.setStatus(HttpServletResponse.SC_FOUND);
    }

    @RequestMapping(value = REQUEST_MAPPING_AJAX_DETAIL_CARI_PUBLIKASI, method = {RequestMethod.POST})
    public void doSearchDataTablesListDetail2(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        String journalNo = "";

        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            DataTablesSearchResult dataTablesSearchResult = new DataTablesSearchResult();
            try {
                dataTablesSearchResult.setDraw(Integer.parseInt(request.getParameter("draw")));
            } catch (NumberFormatException e) {
                dataTablesSearchResult.setDraw(0);
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
                        if (searchBy.equalsIgnoreCase("journalNo")) {
                            journalNo = value;
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
            TxPubsJournal existing = trademarkService.selectOnePubJournalByJournalNo(journalNo);

            searchCriteria.add(new KeyValue("txGroup.id", existing.getTxGroup().getId(), false));

            List<String[]> dataList = new ArrayList<>();
            GenericSearchWrapper<TxGroupDetail> searchResult = trademarkService.searchGroupDetail(searchCriteria, "id", "DESC", 0, null);
            if (searchResult.getCount() > 0) {
                int no = 0;
                for (TxGroupDetail result : searchResult.getList()) {
                    no++;

                    String brandName = "-";
                    String classList = "-";
                    String ownerNameList = "-";
                    String filetypeDetaildesc = "-";
                    String filetypedesc = "-";
                    String status = "-";

                    try {
                        brandName = result.getTxTmGeneral().getTxTmBrand().getName();
                    } catch (NullPointerException e) {
                    }

                    try {
                        filetypeDetaildesc = result.getTxTmGeneral().getTxReception().getmFileTypeDetail().getDesc();
                        filetypedesc = result.getTxTmGeneral().getTxReception().getmFileType().getDesc();
                    } catch (NullPointerException e) {
                    }

                    try {
                        Map<String, String> classMap = new HashMap<>();
                        StringBuffer sbClassList = new StringBuffer();
                        for (TxTmClass txTmClass : result.getTxTmGeneral().getTxTmClassList()) {
                            classMap.put("" + txTmClass.getmClass().getNo(), "" + txTmClass.getmClass().getNo());
                        }
                        for (Map.Entry<String, String> map : classMap.entrySet()) {
                            sbClassList.append(map.getKey());
                            sbClassList.append(", ");
                        }
                        if (sbClassList.length() > 0) {
                            classList = sbClassList.substring(0, sbClassList.length() - 2);
                        }
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
                            ownerNameList = sbOwnerList.substring(0, sbOwnerList.length() - 2);
                        }
                        /*ownerName = result.getTxTmGeneral().getTxTmOwner().getName();*/
                    } catch (NullPointerException e) {
                    }

                    dataList.add(new String[]{
                            "" + no,
                            result.getTxTmGeneral().getApplicationNo(),
                            result.getTxTmGeneral().getFilingDateTemp(),
                            filetypeDetaildesc,
                            filetypedesc,
                            brandName,
                            classList,
                            ownerNameList,
                    });
                }
            }

            dataTablesSearchResult.setData(dataList);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
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
                        if (searchBy.equalsIgnoreCase("journalStart") || searchBy.equalsIgnoreCase("journalTo") || searchBy.equalsIgnoreCase("journalEnd")) {
                            try {
                                searchCriteria.add(new KeyValue(searchBy, DateUtil.toDate("dd/MM/yyyy", value), true));
                            } catch (ParseException e) {
                            }
                        } else if(StringUtils.isNotBlank(value) && searchBy.equalsIgnoreCase("txGroup.txGroupDetailList.txTmGeneral")){
                            TxPubsJournal getByAppNo = trademarkService.selectOnePubJournalByAppNo(value);

                            if(getByAppNo != null){
                                searchCriteria.add(new KeyValue("id", getByAppNo.getId(), true));
                            }
                            else {
                                searchCriteria.add(new KeyValue(searchBy, value, false));
                            }
                            ArrayUtils.removeElement(searchByArr, "txGroup.txGroupDetailList.txTmGeneral");
                            ArrayUtils.removeElement(keywordArr, value);
                        }
                        else {
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
                        case "1":
                            orderBy = "journalNo";
                            break;
                        case "2":
                            orderBy = "journalStart";
                            break;
                        case "3":
                            orderBy = "journalEnd";
                            break;
                        case "4":
                            orderBy = "txGroup.name";
                            break;
                        default:
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
            GenericSearchWrapper<TxPubsJournal> searchResult = trademarkService.searchPublikasi(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxPubsJournal result : searchResult.getList()) {

                    // For user role access button menu
                    MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    String button = "";
                    if (mUser.hasAccessMenu("T-PBH")) {
                        button = "<button type='button' class='btn btn-danger btn-xs' id='deleteBtnPub' onClick='deletePub(\"" + result.getId() + "\")'>Hapus</button>";
                    }
                    no++;
                    String groupName = "-";
                    try {
                        groupName = result.getTxGroup().getName();
                    } catch (LazyInitializationException ex) {
                        groupName = "-";
                    }
                    data.add(new String[]{
                            "" + no,
                            "<a href=\"" + getPathURLAfterLogin(PATH_DETAIL_PUBLIKASI + "?no=" + result.getJournalNo()) + "\">" + result.getJournalNo() + "</a>",
                            result.getJournalStartTemp(),
                            result.getJournalEndTemp(),
                            groupName,
                            button
                            /*"<button type='button' class='btn btn-danger btn-xs' id='deleteBtnPub' onClick='deletePub(\"" + result.getId() + "\")'>Hapus</button>",*/
                    });
                }
            }
            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request, final HttpServletResponse response) {
        model.addAttribute("menu", "publikasi");
    }

    @GetMapping(REQUEST_MAPPING_DETAIL_PUBLIKASI)
    public String doShowPageTambahGrupPermohonan(@RequestParam(value = "no", required = false) String journalNo,
                                                 final Model model, final HttpServletRequest request, final HttpServletResponse response) {

        TxPubsJournal existing = trademarkService.selectOnePubJournalByJournalNo(journalNo);
        model.addAttribute("form", existing);
        if (existing == null) {
            return REDIRECT_PUBLIKASI + "?error=Nomor tidak ditemukan";
        }

        return PAGE_PUBLIKASI_DETAIL;
    }

    /***************************** - PUBLIKASI DETAIL SECTION - ****************************/
    @PostMapping(REQUEST_MAPPING_DETAIL_PUBLIKASI)
    public void doShowPageDetailPublikasi(@RequestParam(value = "no", required = false) String journalNo, final Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException {

        TxPubsJournal existing = trademarkService.selectOnePubJournalByJournalNo(journalNo);
        model.addAttribute("form", existing);

        List<KeyValue> searchCriteria = new ArrayList<>();

        searchCriteria.add(new KeyValue("txGroup.id", existing.getTxGroup().getId(), false));

        List<String[]> dataList = new ArrayList<>();
        GenericSearchWrapper<TxGroupDetail> searchResult = trademarkService. searchGroupDetail(searchCriteria, "id", "DESC", 0, null);
        if (searchResult.getCount() > 0) {
            int no = 0;
            for (TxGroupDetail result : searchResult.getList()) {
                no++;

                String brandName = "-";
                String classList = "-";
                String ownerNameList = "-";
                String filetypeDetaildesc = "-";
                String filetypedesc = "-";

                try {
                    brandName = result.getTxTmGeneral().getTxTmBrand().getName();
                } catch (NullPointerException e) {
                }

                try {
                    filetypeDetaildesc = result.getTxTmGeneral().getTxReception().getmFileTypeDetail().getDesc();
                    filetypedesc = result.getTxTmGeneral().getTxReception().getmFileType().getDesc();
                } catch (NullPointerException e) {
                }

                try {
                    Map<String, String> classMap = new HashMap<>();
                    StringBuffer sbClassList = new StringBuffer();
                    for (TxTmClass txTmClass : result.getTxTmGeneral().getTxTmClassList()) {
                        classMap.put("" + txTmClass.getmClass().getNo(), "" + txTmClass.getmClass().getNo());
                    }
                    for (Map.Entry<String, String> map : classMap.entrySet()) {
                        sbClassList.append(map.getKey());
                        sbClassList.append(", ");
                    }
                    if (sbClassList.length() > 0) {
                        classList = sbClassList.substring(0, sbClassList.length() - 2);
                    }
                } catch (NullPointerException e) {
                }

                try {
                    Map<String, String> ownerMap = new HashMap<>();
                    StringBuffer sbOwnerList = new StringBuffer();
                    for (TxTmOwner txTmOwner : result.getTxTmGeneral().getTxTmOwner()) {
                        ownerMap.put("" + txTmOwner.getName(), "" + txTmOwner.getName());
                    }
                    for (Map.Entry<String, String> map : ownerMap.entrySet()) {
                        sbOwnerList.append(map.getKey());
                        sbOwnerList.append(", ");
                    }
                    if (sbOwnerList.length() > 0) {
                        ownerNameList = sbOwnerList.substring(0, sbOwnerList.length() - 2);
                    }
                    /*ownerName = result.getTxTmGeneral().getTxTmOwner().getName();*/
                } catch (NullPointerException e) {
                }

                String[] data = new String[9];

                data[0] = "" + no;
                data[1] = result.getTxTmGeneral().getApplicationNo();
                data[2] = result.getTxTmGeneral().getFilingDateTemp();
                data[3] = filetypedesc;//jenis permohonan
                data[4] = filetypeDetaildesc;//tipe permohonan
                data[5] = brandName;
                data[6] = classList;
                data[7] = ownerNameList;

                dataList.add(data);
            }
        }

        model.addAttribute("dataList", dataList);
    }

    /***************************** END PUBLIKASI DETAIL SECTION - ****************************/

    public class ProcessBRM implements Runnable{

        Map<Integer, String> classMap = new HashMap<>();
        Map<Integer, String> classEnMap = new HashMap<>();
        Map<String, String> priorMap = new HashMap<>();

        Object[] before = null;
        StringBuffer sbClassList, sbClassDescList;
        StringBuffer sbClassListEn, sbClassDescListEn;
        StringBuffer sbPriorDescList;

        String brandLogo = "";
        String pathFolder = "";
        String pathBrandLogo = "";

        List<Object[]> brmObj;
        ConcurrentHashMap<String,DataBRM> allDataBRM;
        String appNo;
        int no = 0;

        public ProcessBRM(List<Object[]> brmObj, ConcurrentHashMap<String,DataBRM> allDataBRM,String appNo,int no){
            this.brmObj = brmObj;
            this.allDataBRM = allDataBRM;
            this.appNo = appNo;
            this.no = no;
        }

        @Override
        public void run() {
            long s = System.currentTimeMillis();
            List<String> classDescList = new ArrayList<>();
            List<String> classDescEnList = new ArrayList<>();

            for (Object[] current : brmObj) {
                //current and before comparison might be null error!
                if (current[0] == null) continue;

                //class
                int key = Integer.parseInt(current[2] == null ? "0" : current[2].toString());
                int keyEn = Integer.parseInt(current[2] == null ? "0" : current[2].toString());
                String classDesc = current[13] == null ? "" : current[13].toString();
                String classDescEn = current[18] == null ? "" : current[18].toString();
                if (!classMap.containsKey(key) ) {
                    classDescList.add(classDesc);
                } else if (!classDescList.contains(classDesc)) {
                    classDescList.add(classDesc);
                    classDesc = classMap.get(key) + "; " + classDesc;
                } else  {
                    classDesc = classMap.get(key);
                }

                if (!classEnMap.containsKey(keyEn)) {
                    classDescEnList.add(classDescEn);
                } else if (!classDescEnList.contains(classDescEn)) {
                    classDescEnList.add(classDescEn);
                    classDescEn = classEnMap.get(keyEn) + "; " + classDescEn;
                } else {
                    classDescEn = classEnMap.get(keyEn);
                }

                classMap.put(key, (classDesc));
                classEnMap.put(keyEn, (classDescEn));

                //prior
                String pDesc = "";
                if (current[4] != null) {
                    String keyPrior = current[4] == null ? "" : current[4].toString();
                    Date priorDate = current[16] == null ? null : Timestamp.valueOf(current[16].toString());
                    if (priorDate != null) {
                        String spriorDate = new SimpleDateFormat("dd/MM/yyyy").format(priorDate);
                        pDesc = spriorDate + " " + current[17].toString();
                        if (priorMap.containsKey(keyPrior)) {
                            pDesc = keyPrior + " " + pDesc;
                        }
                    }
                    priorMap.put(keyPrior, pDesc);
                }

                if (current[0] != null) { // current maybe null
                    before = current;
                }
            }

            sbClassList = new StringBuffer();
            sbClassDescList = new StringBuffer();
            sbClassListEn = new StringBuffer();
            sbClassDescListEn = new StringBuffer();
            for (Map.Entry<Integer, String> map : classMap.entrySet()) {
                sbClassList.append(map.getKey());
                sbClassList.append(", ");
                sbClassDescList.append("===");
                sbClassDescList.append(map.getValue());
                sbClassDescList.append("===");
                sbClassDescList.append("\n");
            }
            for (Map.Entry<Integer, String> mapEn : classEnMap.entrySet()) {
                sbClassListEn.append(mapEn.getKey());
                sbClassListEn.append(", ");
                sbClassDescListEn.append("===");
                sbClassDescListEn.append(mapEn.getValue());
                sbClassDescListEn.append("===");
                sbClassDescListEn.append("\n");
            }
            sbPriorDescList = new StringBuffer();
            for (Map.Entry<String, String> map2 : priorMap.entrySet()) {
                sbPriorDescList.append(map2.getValue());
                sbPriorDescList.append(" dan ");
            }
            String finalPrior = StringUtils.removeEnd(sbPriorDescList.toString(), " dan ");

            no++;
            DataBRM dataBRM = new DataBRM();
            dataBRM.setNo(no);
            dataBRM.setAppNo(before[0].toString());
            dataBRM.setIrn(before[0] == null ? "" : before[0].toString().substring(before[0].toString().length() - 7));
            dataBRM.setFillingDate(before[1] == null ? null : Timestamp.valueOf(before[1].toString()));
            dataBRM.setClassNo(sbClassList.substring(0, sbClassList.length() - 2));
            dataBRM.setClassDesc(sbClassDescList.substring(0, sbClassDescList.length() - 1));
            dataBRM.setBrandName(before[3] == null ? "" : before[3].toString());
            dataBRM.setPrior(!sbPriorDescList.toString().isEmpty() ? finalPrior : "");
            dataBRM.setOwnerName(before[5] == null ? "" : before[5].toString());
            dataBRM.setOwnerAddress(before[6] == null ? "" : (before[6].toString() +
                    (before[21] == null ? "" : ", " + before[21]) + //extending city name
                    (before[22] == null ? "" : ", " + before[22]) + //extending province name
                    (before[23] == null ? "" : ", " + before[23]) // extending zip post code
            ));
            dataBRM.setReprsName(before[7] == null ? "" : before[7].toString());
            dataBRM.setReprsAddress(before[8] == null ? "" : before[8].toString());
            dataBRM.setBrandTypeName(before[9] == null ? "" : before[9].toString());
            dataBRM.setBrandTranslation(before[10] == null ? "" : before[10].toString());
            dataBRM.setBrandColor(before[11] == null ? "" : before[11].toString());
            dataBRM.setBrandDescripion(before[12] == null ? "" : before[12].toString());
//			dataBRM.setBrandLogo(before[14]==null ? "" : before[14].toString().concat("_".concat(before[15].toString())) );
            //get Brand Logo
            String eks = ".jpg";
            brandLogo = before[14] == null ? "" : before[14].toString() + eks;
//            TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(before[0].toString());
//            String pathFolder = DateUtil.formatDate(txTmGeneral.getTxTmBrand().getCreatedDate(), "yyyy/MM/dd/");
            pathFolder = before[19] == null ? "" : before[19].toString() + "/";
            pathBrandLogo = brandLogo == null ? "" : uploadFileBrandPath + pathFolder + brandLogo;

            dataBRM.setBrandLogo(before[14] == null ? "" : pathBrandLogo);
            dataBRM.setPriorDate(before[16] == null ? null : Timestamp.valueOf(before[16].toString()));
            dataBRM.setPriorcountry(before[17] == null ? "" : before[17].toString());
            dataBRM.setClassDescEn(sbClassDescListEn.substring(0, sbClassDescListEn.length() - 1));
            allDataBRM.put(appNo, dataBRM);

            long e = System.currentTimeMillis();
            long executionTime = e - s;
            //System.out.println("{Thread "+this.no+"]: "+this.appNo+" -> "+executionTime);
        }
    }

    //cetak BRM
    //export cetak publikasi brm pdf
    @GetMapping(REQUEST_MAPPING_CETAK_PUBLIKASI)
    public void PublikasiCetak(@RequestParam(value = "no", required = false) String journalNo, ModelMap modelMap, @ModelAttribute("form") TxPubsJournal form, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) throws SQLException, JRException, IOException {

        Date date = new Date();
        long start = date.getTime();
        long end = 0;
        try {
            form = trademarkService.selectOnePubJournalByJournalNo(journalNo);
            model.addAttribute("form", form);

            Map<String, Object> params = new HashMap<String, Object>();

            String pattern = "dd MMMM yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(pattern, new Locale("ID"));

            String pattern_penerbitan = "MMMM yyyy";
            SimpleDateFormat sdfpenerbitan = new SimpleDateFormat(pattern_penerbitan, new Locale("ID"));

            Calendar c = Calendar.getInstance();
            c.setTime(form.getJournalStart());
            c.add(Calendar.MONTH, form.getValidityPeriod());
            String JournalEnd = sdf.format(c.getTime()).toUpperCase();

            String patternCetak = "dd/MM/yyyy";
            SimpleDateFormat sdfCetak = new SimpleDateFormat(patternCetak, new Locale("ID"));

            // Generate QR Code
            String url = request.getRequestURL().toString();
            String uri = request.getRequestURI();
//			String qrText = url.replaceAll(uri, "") + getPathURL(PATH_VALIDATE + "/" + txTmGeneral.getApplicationNo());
            byte[] qrData = ZxingUtil.textToQrCode(journalNo, new File(logoQRPengayoman), 125, 125, 30, 30);
            String namaFile = journalNo;
            namaFile = namaFile.replaceAll(" ", "");
            Path path = Files.createTempFile(namaFile.replaceAll("/", "_"), ".png");
            FileUtils.writeByteArrayToFile(path.toFile(), qrData);
            final InputStream qrCode = new FileInputStream(path.toFile());

            params.put("pQrCode", journalNo);
            params.put("qrCode", qrCode);
            params.put("pJournalNo", form.getJournalNo());
            params.put("pPrintDate", sdfCetak.format(new Date()));
            params.put("pCreatedDate", sdfpenerbitan.format(form.getCreatedDate()).toUpperCase());
            params.put("pMonth", String.valueOf(form.getValidityPeriod()) + getIndonesianNumber(form.getValidityPeriod()));
            params.put("pJournalStart", sdf.format(form.getJournalStart()).toUpperCase());
            params.put("pJournalEnd", JournalEnd);
            params.put("pHearing", form.getJournalNotes()  != null ? form.getJournalNotes() : "");

//		    MEmployee mEmployee = null;
		   /* try {
		    	  mEmployee = employeeService.findOne(form.getJournalSignature().getCurrentId());
		    }  catch (NullPointerException e) {
		    }*/

//		    if(mEmployee!=null) {
//		    	params.put("pSignatureName", mEmployee.getEmployeeName());
//		    	params.put("pSignatureNIK", "NIP. " + mEmployee.getNik());
//		    } else {

            List<MLookup> mLookupCetakBRM = lookupService.selectAllbyGroup("CetakBRM");
            for (MLookup ml : mLookupCetakBRM) {
                if ("TTDNAMA".equalsIgnoreCase(ml.getCode())) {
                    params.put("pSignatureName", ml.getName());
                }
                if ("TTDNIP".equalsIgnoreCase(ml.getCode())) {
                    params.put("pSignatureNIK", "NIP. " + ml.getName());
                }
                if ("TTDLOKASI".equalsIgnoreCase(ml.getCode())) {
                    params.put("pSignatureLokasi", ml.getName());
                }
                if ("SIGN".equalsIgnoreCase(ml.getCode())) {
                    params.put("signature", uploadFilePathSignature + ml.getName());
                }
            }

            String mLookupCode = form.getJournalType().getCode();
            String mLawId = form.getmLaw().getId();

            List<MLookup> mLookupJenisBRM = lookupService.selectAllbyGroup("JenisBRM");
            for (MLookup ml : mLookupJenisBRM) {
                if (ml.getCode().equalsIgnoreCase(mLookupCode.trim())) {
                    params.put("pSeri", ml.getName());
                }
            }
            List<MLookup> mKetentuanPasalBRM = lookupService.selectAllbyGroup("BRMPASAL");
            for (MLookup ml : mKetentuanPasalBRM) {
                if (ml.getCode().equalsIgnoreCase(mLawId.trim())) {
                    params.put("pBRMPasal", ml.getName());
                }
            }
            List<MLookup> mUndangUndangBRM = lookupService.selectAllbyGroup("BRMUU");
            for (MLookup ml : mUndangUndangBRM) {
                if (ml.getCode().equalsIgnoreCase(mLawId.trim())) {
                    params.put("pBRMUU", ml.getName());
                }
            }

            //isi brm
            DataBRM dataBRM = null;
            List<Object[]> ObjBRM = trademarkService.selectObject(journalNo);
            date = new Date();
            end = date.getTime();
            //System.out.println("ObjBRM Size "+ObjBRM.size()+" Timespend "+(end-start)); start = date.getTime();
            List<DataBRM> listDataBRM = new ArrayList<DataBRM>();
            /* for threading purposes
             *   1. split all ObjBRM according to appNo - obj[0] and keep the order to appNoList
             *   2. keep all proccesed dataBRM to concurrentMap
             *   3. re-sorting according to appNo order
             * */
            String pathFolder = "";
            String pathBrandLogo = "";
            List<String> appNoList = new ArrayList<String>();
            Map<String, List<Object[]>> objBRMSeparated = new HashMap<>();
            ConcurrentHashMap<String, DataBRM> allDataBRM = new ConcurrentHashMap<>();
            for (Object[] current : ObjBRM) {
                if (appNoList.indexOf(current[0].toString()) == -1) {
                    appNoList.add(current[0].toString());
                }
                if (!objBRMSeparated.containsKey(current[0].toString())) {
                    objBRMSeparated.put(current[0].toString(), new ArrayList<Object[]>());
                }
                List<Object[]> zobjBRM = objBRMSeparated.get(current[0]);
                zobjBRM.add(current);
                objBRMSeparated.put(current[0].toString(), zobjBRM);
                //System.out.println("Timespend Process ObjORM "+(end-start)); start = date.getTime();
            }
            date = new Date(); end = date.getTime();
            //System.out.println(objBRMSeparated.size()+" appNo size "+(end-start)); start = date.getTime();
            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()+3); //auto assign Thread count
            int no = 0;
            for (String appNo : appNoList) {
                executor.execute(new ProcessBRM(objBRMSeparated.get(appNo),allDataBRM,appNo,no++));
            }
            executor.shutdown();
            executor.awaitTermination(1,TimeUnit.DAYS);

            for (String appNo : appNoList) { //sort by appNoList
                if(allDataBRM.containsKey(appNo)) {
                    listDataBRM.add(allDataBRM.get(appNo));
                }
            }

            ClassLoader classLoader = getClass().getClassLoader();
            File imageFile = new File(pathImage + "logo-pengayoman.png");
            if (!imageFile.exists()) {
                imageFile = new File(classLoader.getResource("static/img/logo-pengayoman.png").getFile());
            }
            params.put("pLogo", imageFile.toPath().toString());
            params.put("pUploadFilePath", uploadFileBrandPath + pathFolder);
            params.put("pBrand", pathBrandLogo);
            params.put("daftarIsi", new JRBeanCollectionDataSource(listDataBRM));

            //dari source jasper
            Resource resource ;
            if (mLookupCode.equalsIgnoreCase("BRMMadrid")) {
                resource = resourceLoader.getResource("classpath:report/CetakBRMMadrid.jasper");
            }else{
                resource = resourceLoader.getResource("classpath:report/CetakBRM.jasper");
            }
            File file = resource.getFile();
            InputStream jasperStream1 = new FileInputStream(file);
            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream1);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JRBeanCollectionDataSource(listDataBRM));

            response.setContentType("application/pdf");//x-pdf-> download, -pdf->open new window browser
            response.setHeader("Content-disposition", "inline; filename=" + journalNo + ".pdf");


            byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
            InputStream is = new ByteArrayInputStream(pdfBytes);
            this.signPdf(is, response.getOutputStream());
            is.close();
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } catch (Exception ex) {
            date = new Date(); end = date.getTime();
            //System.out.println("error cetak BRM: "+ex.toString()+(end-start)); start = date.getTime();

            logger.error(ex.getMessage(), ex);
            response.sendRedirect(request.getContextPath() + PATH_AFTER_LOGIN + PATH_PUBLIKASI + "?error=Gagal Cetak BRM : " + ex.getMessage());
        }
    }

    private void signPdf(InputStream input, OutputStream output) {
        String key = CERTIFICATE_FILE + "eAdministrasi.p12";
        //System.out.println("PATH : " + key);
        try {
            PDFSignatureFacade facade = new PDFSignatureFacade();
            facade.sign(key, "JakartaPP123!@#", input, output, true, new java.awt.Rectangle(250, 0, 400, 50));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String getIndonesianNumber(int validityPeriod) {
        if (validityPeriod == 1) {
            return " (SATU) ";
        } else if (validityPeriod == 2) {
            return " (DUA) ";
        } else if (validityPeriod == 3) {
            return " (TIGA) ";
        } else {
            return "( )";
        }
    }

    private boolean doInitiateTambahPublikasi(final Model model, final HttpServletRequest request) {
        TxPubsJournal existing = trademarkService.selectOnePubJournal("id", request.getParameter("id"));
        if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
            model.addAttribute("form", existing);
        } else {
            model.addAttribute("existing", existing);
        }

        if (existing != null) {
            return existing != null;
        } else {
            return false;
        }
    }
}
