package com.docotel.ki.controller.master;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.enumeration.ImportClassDetailActionEnum;
import com.docotel.ki.enumeration.LanguageTranslatorEnum;
import com.docotel.ki.enumeration.POICellEnum;
import com.docotel.ki.model.master.MClass;
import com.docotel.ki.model.master.MClassDetail;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.transaction.TxTmClass;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.service.master.ClassService;
import com.docotel.ki.service.master.UserService;
import com.docotel.ki.util.FieldValidationUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class MKelasDetailController extends BaseController {

    @Autowired
    private ClassService classService;
    @Autowired
    private UserService userService;

    private static final String DIRECTORY_PAGE = "master/kelas/";

    private static final String PAGE_LIST = DIRECTORY_PAGE + "list-kelas-detail";
    private static final String PAGE_TAMBAH = DIRECTORY_PAGE + "tambah-kelas-detail";
    private static final String PAGE_EDIT = DIRECTORY_PAGE + "edit-kelas-detail";
    private static final String PAGE_PERMINTAAN_JENIS_BARANG_JASA = DIRECTORY_PAGE + "permintaan-jenis-barang-jasa";


    public static final String PATH_LIST = "/list-master-kelas";
    private static final String PATH_EDIT = "/edit-kelas-detail";
    private static final String PATH_DELETE = "/delete-kelas-detail";

    private static final String PATH_TAMBAH = "/tambah-kelas-detail";
    private static final String PATH_TAMBAH_KD = "/tambah-kelas-detail-baru";

    private static final String PATH_EXPORT = "/ekspor-kelas-detail";

    private static final String PATH_AJAX_LIST = "/cari-kelas-detail";
    private static final String PATH_AJAX_UPLOAD = "/upload-list-kelas-detail";
    private static final String PATH_AJAX_LIST_PERMINTAAN = "/cari-kelas-detail-permintaan";

    private static final String PATH_TERIMA_PERMINTAAN_JENIS_BARANG_JASA = "/terima-permintaan-jenis-barang-jasa";
    private static final String PATH_TOLAK_PERMINTAAN_JENIS_BARANG_JASA = "/tolak-permintaan-jenis-barang-jasa";

    public static final String PATH_PERMINTAAN_JENIS_BARANG_JASA = "/permintaan-jenis-barang-jasa";
    public static final String PATH_COUNT_PERMINTAAN_JENIS_BARANG_JASA = "/count-permintaan-jenis-barang-jasa";

    private static final String REQUEST_MAPPING_AJAX_LIST = PATH_AJAX_LIST + "*";
    private static final String REQUEST_MAPPING_AJAX_LIST_PERMINTAAN = PATH_AJAX_LIST_PERMINTAAN + "*";
    private static final String REQUEST_MAPPING_AJAX_UPLOAD = PATH_AJAX_UPLOAD + "*";
    private static final String REQUEST_MAPPING_TAMBAH = PATH_TAMBAH + "*";
    private static final String REQUEST_MAPPING_TAMBAH_DETAIL = PATH_TAMBAH_KD + "*";
    private static final String REQUEST_MAPPING_EDIT = PATH_EDIT + "*";
    private static final String REQUEST_MAPPING_DELETE = PATH_DELETE + "*";
    private static final String REQUEST_MAPPING_TERIMA_PERMINTAAN_JENIS_BARANG_JASA = PATH_TERIMA_PERMINTAAN_JENIS_BARANG_JASA + "*";
    private static final String REQUEST_MAPPING_TOLAK_PERMINTAAN_JENIS_BARANG_JASA = PATH_TOLAK_PERMINTAAN_JENIS_BARANG_JASA + "*";

    private static final String PATH_PRATINJAU_PERMOHONAN = "/pratinjau-permohonan";

    private static final String REQUEST_EXPORT = PATH_EXPORT + "*";

    private static final String REDIRECT_LIST_TAMBAH_DETAIL = "redirect:" + PATH_AFTER_LOGIN + PATH_TAMBAH;
    public static final String REDIRECT_TO_LIST = "redirect:" + PATH_AFTER_LOGIN + PATH_LIST;


    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "maintenance");

        if (request.getRequestURI().contains(PATH_PERMINTAAN_JENIS_BARANG_JASA)) {
            model.addAttribute("subMenu", "permintaanBarangJasa");
        } else {
            model.addAttribute("subMenu", "listKelas");

            if (request.getRequestURI().contains(PATH_TAMBAH_KD)) {
                if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
                    model.addAttribute("form", new MClassDetail());
                }
            }
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_TAMBAH)
    public String showPageList(Model model, @RequestParam(value = "no", required = false) String no) {
        MClass mClass = null;
        if (StringUtils.isNotBlank(no)) {
            mClass = classService.findOneMClass(no);
        }
        if (mClass == null) {
            return MKelasController.REDIRECT_TO_LIST + "?error=Kelas tidak ditemukan";
        }

        model.addAttribute("mClass", mClass);
        model.addAttribute("translatorList", LanguageTranslatorEnum.values());

        return PAGE_LIST;
    }

    @RequestMapping(value = REQUEST_MAPPING_AJAX_LIST)
    public void doGetListDataTables(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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
            } catch (NumberFormatException ignored) {
            }
            try {
                limit = Math.abs(Integer.parseInt(request.getParameter("length")));
            } catch (NumberFormatException ignored) {
            }

            String[] excludeArr = request.getParameterValues("excludeArr6[]");
            String[] searchByArr = request.getParameterValues("searchByArr6[]");
            String[] keywordArr = request.getParameterValues("keywordArr6[]");

            List<KeyValue> searchCriteria = new ArrayList<>();

            MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (searchByArr != null) {
                for (int i = 0; i < searchByArr.length; i++) {
                    String searchBy = searchByArr[i];
                    String value = "";
                    try {
                        value = keywordArr[i];
                    } catch (ArrayIndexOutOfBoundsException ignored) {
                    }
                    if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                        if (StringUtils.isNotBlank(value)) {
                            searchCriteria.add(new KeyValue(searchBy, value, true));
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
                        case "2":
                            orderBy = "CLASS_ID";
                            break;
                        case "3":
                            orderBy = "CLASS_DESC";
                            break;
                        case "4":
                            orderBy = "CLASS_DESC_EN";
                            break;
                        case "5":
                            orderBy = "STATUS_FLAG";
                            break;
                        case "6":
                            orderBy = "CREATED_BY";
                            break;
                        case "7":
                            orderBy = "CREATED_DATE";
                            break;
                        default:
                            orderBy = "CLASS_ID";
                            break;
                    }
                }
            }

            String orderType = request.getParameter("order[0][dir]");
            if (orderType == null) {
                orderType = "DESC";
            } else {
                orderType = orderType.trim();
                if (!orderType.equalsIgnoreCase("ASC")) {
                    orderType = "DESC";
                }
            }

            List<String[]> data = new ArrayList<>();
            GenericSearchWrapper<Object[]> searchResult = null;
            if(mUser.hasAccessMenu("T-UKLSD")) {
                searchResult = classService.searchClassMultiKeywordAdmin(searchCriteria, excludeArr, orderBy, orderType, offset, limit);
            } else {
                searchResult = classService.searchClassMultiKeyword(searchCriteria, excludeArr, orderBy, orderType, offset, limit);
            }
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (Object result[] : searchResult.getList()) {
                    no++;
                    Date Date = null;
                    String pDate = "";
                    try {
                        Date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(result[6].toString());
                        pDate = new SimpleDateFormat("dd-MM-yyyy").format(Date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    String KelasID = "" + result[0] == null ? "" : result[0].toString().replace("class_", "");
                    String KelasDetailID = result[3] == null ? "" : result[3].toString();
//                    MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    String button = "";
                    if (mUser.hasAccessMenu("T-UKLSD")) {
                        button = "<div class=\"btn-actions\">"
                                + "<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT + "?no=" + KelasDetailID) + "\">Ubah</a>"
//                                + "<br />" +
//                                "<a class=\"btn btn-danger btn-xs\" href=\"" + getPathURLAfterLogin(PATH_DELETE + "?no=" + result.getId() ) + "\">Hapus</a>"
//                                "<a class=\"btn btn-xs btn-danger\" onclick=\"hapusKelasDetail('" + KelasID + "', '" + KelasDetailID + "')\">Hapus</a>"
                                + "</div>";
                    }
                        if (mUser.hasAccessMenu("T-HKLSD")) {
                            button = "<div class=\"btn-actions\">"
                                    + "<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT + "?no=" + KelasDetailID) + "\">Ubah</a>"
                                + "<br />" +
                                "<a class=\"btn btn-xs btn-danger\" onclick=\"hapusKelasDetail('" + KelasID + "', '" + KelasDetailID + "')\">Hapus</a>"
                                    + "</div>";

                    }

                    String username = "";
                    try {
                        String userid = result[7].toString();
                        username = userService.getUserById(userid).getUsername();
                    } catch (NullPointerException e) {
                    } catch (Exception e) {
                    }

                    data.add(new String[]{
                            "" + no, //No.
                            KelasID, //ID Kelas Detail
                            KelasDetailID, // Kelas
                            result[1] == null ? "" : result[1].toString(), // Uraian
                            result[2] == null ? "" : result[2].toString(), // Uraian En
                            result[5] == null ? "" : result[5].toString().equals("0") ? "Tidak Aktif" : "Aktif", // Status flag
                            username, // username
                            pDate, // created time
                            button,
                    });
                }
            }
            dataTablesSearchResult.setData(data);
            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @GetMapping(REQUEST_MAPPING_TAMBAH_DETAIL)
    public String showPageTambah(Model model, @RequestParam(value = "no", required = true) String no) {
        MClass mClass = classService.findOneMClass(no);
        model.addAttribute("mClass", mClass);
        return PAGE_TAMBAH;
    }

    @PostMapping(REQUEST_MAPPING_TAMBAH_DETAIL)
    public String doProsesTambah(@ModelAttribute("form") MClassDetail form, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
        // validate form
        FieldValidationUtil.required(errors, "parentClass.id", form.getParentClass().getCurrentId(), "parentClass.id");
        FieldValidationUtil.required(errors, "desc", form.getDesc(), "desc");
        // validate form end

        if (!errors.hasErrors()) {
            form.setStatusFlag(true);
            form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            if (form.getDesc().isEmpty()) {
                form.setDesc("-");
            } else if (form.getDescEn().isEmpty()) {
                form.setDescEn("-");
            } else if (form.isTranslationFlag().equals(null)) {
                form.setTranslationFlag(true);
            }

                try {
                    classService.saveClassChild(form);
                    classService.reIndex(); //for fulltext search indexing

                    model.asMap().clear();
                    return REDIRECT_LIST_TAMBAH_DETAIL + "?no=" + form.getParentClass().getCurrentId();
                } catch (DataIntegrityViolationException e) {
                    logger.error(e.getMessage(), e);
                    model.addAttribute("errorMessage", "Gagal menambahkan Kelas Detail");
                }
            }

        return PATH_TAMBAH_KD + "?no=" + form.getParentClass().getCurrentId();
    }

    @GetMapping(REQUEST_MAPPING_EDIT)
    public String showPageEdit(Model model, @RequestParam(value = "no", required = true) String no, @RequestParam(value = "redirect", required = false) String redirect) {
//        byte[] decodedBytes = Base64.getDecoder().decode(no);
//        String n1 = new String(decodedBytes);
//        String[] array = n1.split("\\|", -1);

//        MClassDetail mClassDetail = classService.findOneMClassDetail(array[1]);
        MClassDetail mClassDetail = classService.selectOne("id", no);
        if (mClassDetail != null) {
            model.addAttribute("form", mClassDetail);
            model.addAttribute("redirect", redirect);
            return PAGE_EDIT;
        }

        return REDIRECT_LIST_TAMBAH_DETAIL + "?no=" + no;
    }

    @PostMapping(REQUEST_MAPPING_EDIT)
    public String doProsesEdit(@ModelAttribute("form") MClassDetail form, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {

        String redirectURL = request.getParameter("redirectURL").toString();
        System.out.println("redirectURL:"+ redirectURL);

        MClassDetail existing = classService.selectOne("id", form.getId());
        // validate form
        FieldValidationUtil.required(errors, "parentClass.id", form.getParentClass().getCurrentId(), "parentClass.id");
        FieldValidationUtil.required(errors, "desc", form.getDesc(), "desc");
        // validate form end

        if (!errors.hasErrors()) {
            form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            if (form.getDesc().isEmpty()) {
                form.setDesc("-");
            } else if (form.getDescEn().isEmpty()) {
                form.setDescEn("-");
            } else if (existing.isTranslationFlag().equals(true)) {
                form.setTranslationFlag(true);
            } else if (existing.isTranslationFlag().equals(false)) {
                form.setTranslationFlag(false);
            }

            MClassDetail detailExisted = classService.findExistedClassDetail(form.getDesc());
            MClassDetail detailExistedEn = classService.findExistedClassDetailEn(form.getDescEn());

//            if ((detailExisted == null && detailExistedEn == null)
//                    || (detailExisted == null && form.getDescEn() != null)
//                    || (detailExisted != null && detailExistedEn == null)
//                    || (existing.isStatusFlag() != form.isStatusFlag())) {
                try {
                    classService.saveOrUpdate(form);
                    if(redirectURL=="") {
                        classService.reIndex(); //for fulltext search indexing
                        model.asMap().clear();
                        return REDIRECT_LIST_TAMBAH_DETAIL + "?no=" + form.getParentClass().getCurrentId();
                    } else{
                        return "redirect:" + PATH_AFTER_LOGIN + redirectURL;
                    }
                } catch (DataIntegrityViolationException e) {
                    logger.error(e.getMessage(), e);
                    model.addAttribute("errorMessage", "Gagal mengubah Kelas Detail");
                }
//            } else {
//                return REDIRECT_TO_LIST + "?error=Gagal mengubah Kelas Detail, ditemukan Kelas Detail dengan Uraian yang sama";
//            }
        }
        if(redirectURL!="") {
            return "redirect:" + PATH_AFTER_LOGIN + redirectURL;
        }

        return PATH_TAMBAH_KD + "?no=" + form.getParentClass().getCurrentId();
    }

    @RequestMapping(value = REQUEST_MAPPING_DELETE, method = {RequestMethod.POST})
    @ResponseBody
    public void doHapusKelasDetail(Model model, @RequestParam("listId") String listId,
                                   final HttpServletRequest request, final HttpServletResponse response) {

        TxTmClass existing = classService.selectOneByMClassDetailId(listId);

        if (existing == null) {
            if (classService.deleteMClassDetail(listId)) {
                model.asMap().clear();
                writeJsonResponse(response, 200);
            } else {
                writeJsonResponse(response, 201);
            }
        } else {
            writeJsonResponse(response, 201);
        }
    }

    @GetMapping(REQUEST_EXPORT)
    public void doExportClass(HttpServletRequest request, HttpServletResponse response) {
        InputStream reportInputStream = null;
        String idClassHeader = null;

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
                    if (StringUtils.isNotBlank(value)) {
                        if (searchBy.equals("parentClass.id")) {
                            idClassHeader = value;
                            searchCriteria.add(new KeyValue(searchBy, value, true));
                        } else {
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
                    case "2":
                        orderBy = "id";
                        break;
                    case "3":
                        orderBy = "desc";
                        break;
                    case "4":
                        orderBy = "descEn";
                        break;
                    case "5":
                        orderBy = "statusFlag";
                        break;
                    case "6":
                        orderBy = "createdBy.username";
                        break;
                    case "7":
                        orderBy = "createdDate";
                        break;
                    default:
                        orderBy = "id";
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
            MClass parentClass = null;
            if (idClassHeader != null) {
                parentClass = classService.findOneMClass(idClassHeader);
            }

            response.setContentType("text/csv");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader("Content-Disposition", "attachment; filename=list-master-class-detail.csv");
            try (Writer writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
                writer.write("No,Kelas,No Dasar,Uraian Barang/Jasa,Uraian Barang/Jasa (E),Serial No (E),Serial No (F),Status,User,Tanggal\n");
                int rowNo = 1;
                if (parentClass != null) {
                    int totalRecord = (int) classService.countAllClassDetail(searchCriteria);
                    int retrieved = 0;
                    int classNo = parentClass.getNo();
                    while (retrieved < totalRecord - 1) {
                        System.out.println("======== RETRIEVED: " + retrieved + " | TOTAL: " + totalRecord + " ========");
                        List<Object[]> retrievedDataList = classService.selectAllClassDetailForExport(idClassHeader, searchCriteria, orderBy, orderType, retrieved, 50000);
                        retrieved += retrievedDataList.size();
                        for (Object[] object : retrievedDataList) {
                            writer.write(rowNo++ + "," + classNo + "," + object[1] + ",\"" + object[4] + "\",\"" + object[5] + "\"," + object[6] + "," + object[7] + "," + object[8] + "," + object[2] + "," + object[3] + "\n");
                        }
                    }
                }
                writer.flush(); // DONNOT forget to flush writer after everything done
            } // writer will be closed automatically
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

    @PostMapping(REQUEST_MAPPING_AJAX_UPLOAD)
    public void doAjaxUpload(@RequestParam(value = "file", required = false) MultipartFile uploadedFile, @RequestParam(value = "translator", required = false) String translator, final HttpServletRequest request, final HttpServletResponse response) throws IOException, InvalidFormatException {
        if (isAjaxRequest(request)) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);

            if (uploadedFile == null || uploadedFile.getBytes().length == 0) {
                result.put("message", "File daftar master kelas detail harus diisi");
            } else if (!uploadedFile.getOriginalFilename().toLowerCase().endsWith(".xls")) {
                result.put("message", "File daftar master kelas detail harus dalam format excel (.xls)");
            }

            LanguageTranslatorEnum languageTranslatorEnum = null;

            if (translator == null) {
                result.put("message", "Pustaka penerjemah harus diisi");
            } else {
                try {
                    languageTranslatorEnum = LanguageTranslatorEnum.valueOf(translator);
                } catch (IllegalArgumentException e) {
                    result.put("message", "Pustaka penerjemah harus sesuai opsi yang disediakan");
                }
            }

            if (result.size() == 1) {
                String validateResult = validateBatchUploadFile(uploadedFile.getInputStream());
                //validate file before applying in advance
                if (validateResult != "") {
                    result.put("message", validateResult);
                    writeJsonResponse(response, result);
                    return;
                }
                try {
                    Workbook workbook = WorkbookFactory.create(uploadedFile.getInputStream());
                    Sheet mySheet = workbook.getSheetAt(0);
                    Iterator<Row> rowIter = mySheet.rowIterator();
                    rowIter.next(); // skip first row

                    Map<Integer, MClass> mClassMap = new HashMap<>();
                    while (rowIter.hasNext()) {
                        Row row = rowIter.next();
                        int classNo = 0;
                        try {
                            Cell cell = row.getCell(POICellEnum.COLUMN_A.value());
                            if (cell == null) {
                                continue;
                            }

                            if (cell.getCellTypeEnum().compareTo(CellType.NUMERIC) == 0) {
                                classNo = (int) cell.getNumericCellValue();
                            } else {
                                classNo = Integer.parseInt(cell.toString());
                            }
                        } catch (IllegalStateException | NumberFormatException e) {
                        }

                        MClass existingMClass = null;
                        if (mClassMap.containsKey(classNo)) {
                            existingMClass = mClassMap.get(classNo);
                        } else {
                            existingMClass = classService.findFirstByNo(classNo);
                            if (existingMClass == null) {
                                continue;
                            } else {
                                mClassMap.put(classNo, existingMClass);
                            }
                        }

                        String baseNo = null;
                        try {
                            Cell cell = row.getCell(POICellEnum.COLUMN_B.value());
                            if (cell.getCellTypeEnum().compareTo(CellType.NUMERIC) == 0) {
                                baseNo = "" + ((long) cell.getNumericCellValue());
                            } else {
                                baseNo = cell.toString();
                            }
                        } catch (NullPointerException e) {
                        }
                        String descEn = null;
                        try {
                            descEn = row.getCell(POICellEnum.COLUMN_C.value()).getStringCellValue().trim();
                        } catch (NullPointerException e) {
                        }
                        String updatedDescEn = null;
                        try {
                            updatedDescEn = row.getCell(POICellEnum.COLUMN_D.value()).getStringCellValue().trim();
                        } catch (NullPointerException e) {
                        }
                        String descId = null;
                        try {
                            descId = row.getCell(POICellEnum.COLUMN_E.value()).getStringCellValue().trim();
                        } catch (NullPointerException e) {
                        }
                        String updatedDescId = null;
                        try {
                            updatedDescId = row.getCell(POICellEnum.COLUMN_F.value()).getStringCellValue().trim();
                        } catch (NullPointerException e) {
                        }
                        String action = row.getCell(POICellEnum.COLUMN_G.value()).getStringCellValue();
                        if (classNo < 1) {
                            break;
                        }

                        if (action != null) {
                            String sourceLang = null;
                            String translateLang = null;
                            String query = null;
                            if (action.equalsIgnoreCase(ImportClassDetailActionEnum.NEW.name())) {
                                if (descEn != null) {
                                    // insert english class detail
                                    if (descId == null) {
                                        // desc in indonesian not supplied, translate it
                                        sourceLang = "en";
                                        translateLang = "id";
                                        query = descEn;
                                    }
                                } else if (descId != null) {
                                    // insert indonesian class detail
                                    if (descEn == null) {
                                        // desc in english not supplied, translate it
                                        sourceLang = "id";
                                        translateLang = "en";
                                        query = descId;
                                    }
                                } else {
                                    continue;
                                }
                            } else if (action.equalsIgnoreCase(ImportClassDetailActionEnum.UPDATE.name())) {
                                List<MClassDetail> mcdexist = new ArrayList<MClassDetail>();
                                if (descId != null) {
                                    mcdexist = classService.selectAllClassDetail("desc", descId);
                                }

                                if (descEn != null) {
                                    mcdexist = classService.selectAllClassDetail("descEn", descEn);
                                }

                                if (mcdexist == null || mcdexist.isEmpty()) {
                                    continue;
                                }

                                if (descEn == null || descId == null) {
                                    continue;
                                }

                                if (updatedDescEn != null) {
                                    // insert english class detail
                                    if (updatedDescId == null) {
                                        // desc in indonesian not supplied, translate it
                                        sourceLang = "en";
                                        translateLang = "id";
                                        query = updatedDescEn;
                                    }
                                } else if (updatedDescId != null) {
                                    // insert indonesian class detail
                                    if (updatedDescEn == null) {
                                        // desc in english not supplied, translate it
                                        sourceLang = "id";
                                        translateLang = "en";
                                        query = updatedDescId;
                                    }
                                } else {
                                    continue;
                                }
                            } else if (action.equalsIgnoreCase(ImportClassDetailActionEnum.DELETE.name())) {
                                if (descEn == null || descId == null) {
                                    continue;
                                }
                            } else {
                                continue;
                            }


                            if (action.equalsIgnoreCase(ImportClassDetailActionEnum.DELETE.name())) {
                                // do delete class detail ( set status as inactive )
                                if (descEn != null) {
                                    classService.updateInactiveClassDetail("descEn", descEn);
                                } else {
                                    classService.updateInactiveClassDetail("desc", descId);
                                }
                            } else {
                                MClassDetail existingMClassDetail = null;
                                if (descEn != null) {
                                    existingMClassDetail = classService.selectOneClassDetail("descEn", descEn);
                                } else {
                                    existingMClassDetail = classService.selectOneClassDetail("desc", descId);
                                }

                                if (action.equalsIgnoreCase(ImportClassDetailActionEnum.NEW.name())) {
                                    if (existingMClassDetail != null) {
                                        continue;
                                    }
                                } else {
                                    if (existingMClassDetail == null) {
                                        continue;
                                    }
                                }

                                if (sourceLang != null) {
                                    if (action.equalsIgnoreCase(ImportClassDetailActionEnum.NEW.name())) {
                                        if (descEn != null) {
                                            descId = languageTranslatorEnum.getTranslation(sourceLang, translateLang, query);
                                        } else if (descId != null) {
                                            descEn = languageTranslatorEnum.getTranslation(sourceLang, translateLang, query);
                                        }
                                    } else if (action.equalsIgnoreCase(ImportClassDetailActionEnum.UPDATE.name())) {
                                        if (updatedDescEn != null) {
                                            updatedDescId = languageTranslatorEnum.getTranslation(sourceLang, translateLang, query);
                                        } else {
                                            updatedDescEn = languageTranslatorEnum.getTranslation(sourceLang, translateLang, query);
                                        }
                                    }
                                }

                                Timestamp tmstm = new Timestamp(System.currentTimeMillis());

                                if (action.equalsIgnoreCase(ImportClassDetailActionEnum.NEW.name())) {
                                    MClassDetail mClassDetail = new MClassDetail();
                                    mClassDetail.setParentClass(existingMClass);
                                    mClassDetail.setDesc(descId);
                                    mClassDetail.setDescEn(descEn);
                                    mClassDetail.setSerial1("");
                                    mClassDetail.setSerial2("");
                                    mClassDetail.setClassBaseNo(baseNo);
                                    mClassDetail.setStatusFlag(true);
                                    mClassDetail.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                                    mClassDetail.setCreatedDate(tmstm);
                                    classService.saveOrUpdate(mClassDetail);
                                } else {
                                    existingMClassDetail.setDesc(updatedDescId);
                                    existingMClassDetail.setDescEn(updatedDescEn);
                                    existingMClassDetail.setUpdatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                                    existingMClassDetail.setUpdatedDate(tmstm);
                                    classService.saveOrUpdate(existingMClassDetail);
                                }
                            }
                        }
                    }
                    classService.reIndex(); //for fulltext search indexing
                    workbook.close();
                    result.put("success", true);
                } catch (IOException e) {
                    if (result == null) {
                        result.put("message", "Gagal membaca file master kelas detail. Pastikan file dalam format yang disediakan.");
                    }
                }
            }

            writeJsonResponse(response, result);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    public String validateBatchUploadFile(InputStream inputStream) {
        try {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet mySheet = workbook.getSheetAt(0);
            Iterator<Row> rowIter = mySheet.rowIterator();
            rowIter.next(); // skip first row

            Map<Integer, MClass> mClassMap = new HashMap<>();
            while (rowIter.hasNext()) {
                Row row = rowIter.next();
                int classNo = 0;
                try {
                    Cell cell = row.getCell(POICellEnum.COLUMN_A.value());
                    if (cell == null) {
                        return "Gagal membaca file master kelas detail. Kolom Kelas tidak boleh kosong";
                    }

                    if (cell.getCellTypeEnum().compareTo(CellType.NUMERIC) == 0) {
                        classNo = (int) cell.getNumericCellValue();
                    } else {
                        classNo = Integer.parseInt(cell.toString());
                    }
                } catch (IllegalStateException | NumberFormatException e) {
                }

                MClass existingMClass = null;
                if (mClassMap.containsKey(classNo)) {
                    existingMClass = mClassMap.get(classNo);
                } else {
                    existingMClass = classService.findFirstByNo(classNo);
                    if (existingMClass == null) {
                        continue;
                    } else {
                        mClassMap.put(classNo, existingMClass);
                    }
                }

                String baseNo = null;
                try {
                    Cell cell = row.getCell(POICellEnum.COLUMN_B.value());
                    if (cell.getCellTypeEnum().compareTo(CellType.NUMERIC) == 0) {
                        baseNo = "" + ((long) cell.getNumericCellValue());
                    } else {
                        baseNo = cell.toString();
                    }
                } catch (NullPointerException e) {
                }
                String descEn = null;
                try {
                    descEn = row.getCell(POICellEnum.COLUMN_C.value()).getStringCellValue().trim();
                } catch (NullPointerException e) {
                }
                String updatedDescEn = null;
                try {
                    updatedDescEn = row.getCell(POICellEnum.COLUMN_D.value()).getStringCellValue().trim();
                } catch (NullPointerException e) {
                }
                String descId = null;
                try {
                    descId = row.getCell(POICellEnum.COLUMN_E.value()).getStringCellValue().trim();
                } catch (NullPointerException e) {
                }
                String updatedDescId = null;
                try {
                    updatedDescId = row.getCell(POICellEnum.COLUMN_F.value()).getStringCellValue().trim();
                } catch (NullPointerException e) {
                }
                String action = null;
                try{
                    action = row.getCell(POICellEnum.COLUMN_G.value()).getStringCellValue();
                } catch (NullPointerException e) {
                }
                if (classNo < 1) {
                    break;
                }
                if (action != null) {
                    if (action.equalsIgnoreCase(ImportClassDetailActionEnum.NEW.name())) {
                        if (descEn != null) {
                        } else if (descId != null) {
                        } else {
                            return "Gagal membuat kelas detail baru. Kolom Uraian Indonesia atau Inggris harus disediakan! [" + descId + "] [" + descEn + "]";
                        }
                    } else if (action.equalsIgnoreCase(ImportClassDetailActionEnum.UPDATE.name())) {
                        List<MClassDetail> mcdexist = new ArrayList<MClassDetail>();
                        if (descId != null) {
                            mcdexist = classService.selectAllClassDetail("desc", descId);
                        }

                        if (descEn != null) {
                            mcdexist = classService.selectAllClassDetail("descEn", descEn);
                        }

                        if (mcdexist == null || mcdexist.isEmpty()) {
                            return "Gagal membaca file master kelas detail. Kolom Uraian Indonesia / Inggris belum terdaftar";
                        }

                        if (descEn == null || descId == null) {
                            return "Gagal meng-Update kelas detail. Kolom Uraian Indonesia dan Inggris harus disediakan! [" + descId + "] [" + descEn + "]";
                        }

                        if (updatedDescEn != null) {
                        } else if (updatedDescId != null) {
                        } else {
                            return "Gagal meng-Update kelas detail. Kolom Uraian Indonesia Baru dan Uraian Inggris Baru harus disediakan! [" + descId + "] [" + descEn + "]";
                        }
                    } else if (action.equalsIgnoreCase(ImportClassDetailActionEnum.DELETE.name())) {
                        /*if (descEn == null || descId == null) {
                            return "Gagal meng-Hapus kelas detail. Kolom Uraian Indonesia dan Inggris harus disediakan! [" + descId + "] [" + descEn + "]";
                        }*/
                        //allow delete for uraian indonesia or uraian english only
                    } else {
                        return "Gagal, Action tidak diketahui! [" + descId + "] [" + descEn + "]";
                    }

                    if (action.equalsIgnoreCase(ImportClassDetailActionEnum.DELETE.name())) {
                        // do delete class detail ( set status as inactive )
                        if (descEn != null) {
                            classService.updateInactiveClassDetail("descEn", descEn);
                        } else {
                            classService.updateInactiveClassDetail("desc", descId);
                        }
                    } else {
                        MClassDetail existingMClassDetail = null;
                        if (descEn != null) {
                            existingMClassDetail = classService.selectOneClassDetail("descEn", descEn);
                        } else {
                            existingMClassDetail = classService.selectOneClassDetail("desc", descId);
                        }

                        if (action.equalsIgnoreCase(ImportClassDetailActionEnum.NEW.name())) {
                            if (existingMClassDetail != null) {
                                return "Gagal membuat Kelas Detail baru, record telah ada! [" + descId + "] [" + descEn + "]";
                            }
                        } else {
                            if (existingMClassDetail == null) {
                                return "Gagal, Meng-Update kelas detail, record tidak ditemukan! [" + descId + "] [" + descEn + "]";
                            }
                        }
                    }
                }else{
                    return "unknown action!";
                }
            }
            workbook.close();
        } catch (IOException e) {
            return "Gagal membaca file master kelas detail. Pastikan file dalam format yang disediakan.";
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }
        return "";
    }

    @RequestMapping(path = PATH_PERMINTAAN_JENIS_BARANG_JASA)
    public String doShowPermintaanJenisBarangJasa (final Model model) {
        List<MClass> mClassList = classService.findAllMClass();
        model.addAttribute("listKelas", mClassList);

        return PAGE_PERMINTAAN_JENIS_BARANG_JASA;
    }

    @RequestMapping(value = REQUEST_MAPPING_AJAX_LIST_PERMINTAAN)
    public void doGetListDataTablesPermintaan(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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
            } catch (NumberFormatException ignored) {
            }
            try {
                limit = Math.abs(Integer.parseInt(request.getParameter("length")));
            } catch (NumberFormatException ignored) {
            }

            String[] searchByArr = request.getParameterValues("searchByArr6[]");
            String[] keywordArr = request.getParameterValues("keywordArr6[]");

            List<KeyValue> searchCriteria = new ArrayList<>();

            MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (searchByArr != null) {
                for (int i = 0; i < searchByArr.length; i++) {
                    String searchBy = searchByArr[i];
                    String value = "";
                    try {
                        value = keywordArr[i];
                    } catch (ArrayIndexOutOfBoundsException ignored) {
                    }
                    if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                        if (StringUtils.isNotBlank(value)) {
                            if (searchBy.equalsIgnoreCase("mClassDetail.desc") || searchBy.equalsIgnoreCase("mClassDetail.descEn")) {
                                searchCriteria.add(new KeyValue(searchBy, value, false));
                            } else if (searchBy.equalsIgnoreCase("createdBy")) {
                                //MUser userId = userService.getUserByUsername(value);
                                searchCriteria.add(new KeyValue("createdBy.username", value, false));
                            } else {
                                searchCriteria.add(new KeyValue(searchBy, value, true));
                            }
                        }
                    }
                }
            }

            String orderBy = request.getParameter("orderBy");
            if (orderBy != "") {
                orderBy = orderBy.trim();
            }

            String orderType = request.getParameter("orderType");
            if (orderType == null) {
                orderType = "DESC";
            } else {
                orderType = orderType.trim();
                if (!orderType.equalsIgnoreCase("ASC")) {
                    orderType = "DESC";
                }
            }

            searchCriteria.add(new KeyValue("mClassDetail.statusFlag", false, true));
            searchCriteria.add(new KeyValue("transactionStatus", "PENDING", true));

            List<String[]> data = new ArrayList<>();
            GenericSearchWrapper<TxTmClass> searchResult = classService.selectAllClassDetailPermintaan(searchCriteria, orderBy, orderType, offset, limit);

            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxTmClass result : searchResult.getList()) {
                    no++;
                    int KelasID = result.getmClass().getNo();
                    String KelasDetailID = result.getmClassDetail().getId();
                    String desc = result.getmClassDetail().getDesc();
                    String descEn = result.getmClassDetail().getDescEn();
                    String serial1 = result.getmClassDetail().getSerial1();
                    String username = result.getCreatedBy().getUsername();
                    String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date(result.getCreatedDate().getTime()));

                    data.add(new String[]{
                            "" + no,
                            "" + KelasID,
                            KelasDetailID,
                            desc,
                            descEn,
                            serial1,
                            username,
                            date,
                            "<a target=\"_blank\" class=\"btn btn-primary btn-xs\" style='margin-bottom: 5px;' href=\"" + getPathURLAfterLogin(PATH_PRATINJAU_PERMOHONAN) + "?no=" + result.getTxTmGeneral().getApplicationNo() + "\">Pratinjau</a>"
                            + "<br/>"
                            + "<a href='" + getPathURLAfterLogin(PATH_EDIT + "?no=" + KelasDetailID)+"&redirect=/permintaan-jenis-barang-jasa" + "' style='margin-bottom: 5px;' class='btn btn-warning btn-xs'>Ubah</a>"
                            + "<br/>"
                            + "<button onclick=\"acceptPermintaan('" + KelasDetailID + "');\" style='margin-bottom: 5px;' class='btn btn-info btn-xs'>Terima</button>"
                            + "<br/>"
                            + "<button onclick=\"deletePermintaan('" + KelasDetailID + "');\" class='btn btn-danger btn-xs'>Tolak</button>"
                    });

                }
            }

            dataTablesSearchResult.setData(data);
            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(path = PATH_COUNT_PERMINTAAN_JENIS_BARANG_JASA)
    private void getCountPermintaanAktifTidakAKtif(final HttpServletRequest request, final HttpServletResponse response) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            List<KeyValue> searchCriteria = new ArrayList<>();

            searchCriteria.add(new KeyValue("mClassDetail.statusFlag", false, true));
            searchCriteria.add(new KeyValue("transactionStatus", "PENDING", true));

            long countTidakAktif = classService.countStatusTxTmClass(searchCriteria);

            NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);

            Map<String, Object> result = new HashMap<>();

            result.put("tidakAktif", numberFormat.format(countTidakAktif));

            writeJsonResponse(response, result);
        }
    }

    @RequestMapping(value = REQUEST_MAPPING_TERIMA_PERMINTAAN_JENIS_BARANG_JASA, method = RequestMethod.POST)
    public void doTerimaPermintaanJenisBarangJasa(final Model model, @RequestParam("idKelas") String id, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        boolean terimaPermintaan = classService.terimaPermintaanJenisBarangJasa(id);
        writeJsonResponse(response, terimaPermintaan ? terimaPermintaan : "");
    }

    @RequestMapping(value = REQUEST_MAPPING_TOLAK_PERMINTAAN_JENIS_BARANG_JASA, method = RequestMethod.POST)
    public void doTolakPermintaanJenisBarangJasa(final Model model, @RequestParam("idKelas") String id, @RequestParam("alasanPenolakan") String alasan, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        boolean tolakPermintaan = classService.tolakPermintaanJenisBarangJasa(id, alasan);
        writeJsonResponse(response, tolakPermintaan ? tolakPermintaan : "");
    }
}
