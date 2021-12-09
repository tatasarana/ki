package com.docotel.ki.controller;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.enumeration.ClassStatusEnum;
import com.docotel.ki.enumeration.PriorStatusEnum;
import com.docotel.ki.model.master.MClassDetail;
import com.docotel.ki.model.master.MCountry;
import com.docotel.ki.model.master.MRepresentative;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.transaction.TxTmOwnerCustomRepository;
import com.docotel.ki.service.EmailService;
import com.docotel.ki.service.master.BrandService;
import com.docotel.ki.service.master.ClassService;
import com.docotel.ki.service.master.CountryService;
import com.docotel.ki.service.master.RepresentativeService;
import com.docotel.ki.service.transaction.*;
import com.docotel.ki.util.DateUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.pdf.PdfReader;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.method.P;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class PermohonanGlobalController extends BaseController {

    //MAPPING DARI PERMOHONAN CONTROLLER
    public static final String PATH_KUASA = "/kuasa";
    public static final String PATH_AJAX_LIST_DOKUMEN = "/list-dokumen";
    public static final String PATH_AJAX_LIST_KUASA = "/list-kuasa";
    public static final String PATH_AJAX_LIST_KELAS = "/list-kelas";
    public static final String PATH_AJAX_LIST_KELAS_PETUGAS = "/list-kelas-petugas";
    public static final String PATH_AJAX_LIST_KELAS_COPY = "/list-kelas-copy";
    public static final String PATH_AJAX_LIST_KELAS_LIMIT = "/list-kelas-limitasi";
    public static final String PATH_AJAX_LIST_PRIORITAS = "/list-prioritas";
    private static final String PATH_AJAX_SAVE_TAMBAH_LISENSI = "/save-tambah-lisensi";
    public static final String PATH_AJAX_LIST_TXCLASS = "/list-txclass";
    public static final String PATH_AJAX_LIST_TXCLASS_LIMIT = "/list-txclasslimitation";
    private static final String PATH_AJAX_SAVE_TAMBAH_PEMOHON = "/save-tambah-pemohon";
    private static final String PATH_AJAX_SAVE_EDIT_DOKUMEN_LAMPIRAN = "/save-edit-dokumen-lampiran";
    private static final String PATH_DOWNLOAD_DOKUMEN_LAMPIRAN_PEMOHON = "/download-dokumen-lampiran-pemohon";
    private static final String PATH_VIEW_DOKUMEN_LAMPIRAN_PEMOHON = "/lihat-dokumen-lampiran-pemohon";
    private static final String PATH_AJAX_DELETE_DOKUMEN_PEMOHON = "/delete-dokumen-pemohon";
    private static final String PATH_VALIDATE_DOKUMEN = "/validasi-dokumen";
    public static final String PATH_AJAX_LIST_TXCLASS_NEW_REQUEST= "/list-txclass-new-request";
    public static final String PATH_AJAX_LIST_TXCLASS_PEMOHON_ONLINE="/list-txclass-pemohon-online";

    private static final String REQUEST_MAPPING_KUASA = PATH_KUASA + "*";
    private static final String REQUEST_MAPPING_AJAX_KUASA_LIST = PATH_AJAX_LIST_KUASA + "*";
    private static final String REQUEST_MAPPING_AJAX_LIST_DOKUMEN = PATH_AJAX_LIST_DOKUMEN + "*";
    private static final String REQUEST_MAPPING_AJAX_LIST_KELAS = PATH_AJAX_LIST_KELAS + "*";
    private static final String REQUEST_MAPPING_AJAX_LIST_KELAS_PETUGAS = PATH_AJAX_LIST_KELAS_PETUGAS + "*";
    private static final String REQUEST_MAPPING_AJAX_LIST_KELAS_COPY = PATH_AJAX_LIST_KELAS_COPY + "*";
    private static final String REQUEST_MAPPING_AJAX_LIST_KELAS_LIMIT = PATH_AJAX_LIST_KELAS_LIMIT + "*";
    private static final String REQUEST_MAPPING_AJAX_LIST_PRIORITAS = PATH_AJAX_LIST_PRIORITAS + "*";
    private static final String REQUEST_MAPPING_AJAX_LIST_TXCLASS = PATH_AJAX_LIST_TXCLASS + "*";
    private static final String REQUEST_MAPPING_AJAX_LIST_TXCLASS_LIMIT = PATH_AJAX_LIST_TXCLASS_LIMIT + "*";
    private static final String REQUEST_MAPPING_AJAX_SAVE_TAMBAH_LISENSI = PATH_AJAX_SAVE_TAMBAH_LISENSI + "*";
    private static final String REQUEST_MAPPING_AJAX_SAVE_TAMBAH_PEMOHON = PATH_AJAX_SAVE_TAMBAH_PEMOHON + "*";
    private static final String REQUEST_MAPPING_AJAX_SAVE_EDIT_DOKUMEN_LAMPIRAN = PATH_AJAX_SAVE_EDIT_DOKUMEN_LAMPIRAN + "*";
    private static final String REQUEST_MAPPING_DOWNLOAD_DOKUMEN_LAMPIRAN_PEMOHON = PATH_DOWNLOAD_DOKUMEN_LAMPIRAN_PEMOHON + "*";
    private static final String REQUEST_MAPPING_VIEW_DOKUMEN_LAMPIRAN_PEMOHON = PATH_VIEW_DOKUMEN_LAMPIRAN_PEMOHON + "*";
    private static final String REQUEST_MAPPING_AJAX_DELETE_DOKUMEN_PEMOHON = PATH_AJAX_DELETE_DOKUMEN_PEMOHON + "*";
    private static final String REQUEST_MAPPING_VALIDATE_DOKUMEN = PATH_VALIDATE_DOKUMEN + "*";
    private static final String REQUEST_MAPPING_AJAX_LIST_TXCLASS_NEW_REQUEST = PATH_AJAX_LIST_TXCLASS_NEW_REQUEST + "*";
    private static final String REQUEST_MAPPING_AJAX_LIST_TXCLASS_PEMOHON_ONLINE=PATH_AJAX_LIST_TXCLASS_PEMOHON_ONLINE+ "*";

    @Autowired
    private ClassService classService;
    @Autowired
    private CountryService countryService;
    @Autowired
    private DokumenLampiranService doclampiranService;
    @Autowired
    private PriorService priorService;
    @Autowired
    private RepresentativeService representativeService;
    @Autowired
    private TrademarkService trademarkService;
    @Autowired
    private LicenseService licenseService;
    @Autowired
    private PascaOnlineService pascaOnlineService;
    @Autowired
    private PermohonanService permohonanService;

    @Autowired
    TxTmOwnerCustomRepository txTmOwnerCustomRepository ;
    @Autowired
    private EmailService emailService;

    private FileInputStream fileInputStreamReader;

    @Value("${upload.file.doc.application.path:}")
    private String uploadFileDocApplicationPath;
    @Value("${upload.file.image.tandatangan.path}")
    private String uploadFileImageTandaTangan;
    @Value("${upload.file.logoemail.image:}")
    private String logoEmailImage;
    @Value("${upload.file.web.image:}")
    private String pathImage;


    /* --------------------------------------- DOKUMEN ------------------------------------------*/
    @RequestMapping(value = REQUEST_MAPPING_AJAX_LIST_DOKUMEN, method = {RequestMethod.GET})
    public void doDataTablesListDocument(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            DataTablesSearchResult dataTablesSearchResult = new DataTablesSearchResult();
            try {
                dataTablesSearchResult.setDraw(Integer.parseInt(request.getParameter("draw")));
            } catch (NumberFormatException e) {
                dataTablesSearchResult.setDraw(0);
            }

            String appNo = request.getParameter("appNo");

            TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(appNo);
            List<TxTmDoc> txtmDocList = doclampiranService.selectAllTxTmDoc("txTmGeneral.id", txTmGeneral == null ? "" : txTmGeneral.getId());
            List<String[]> data = new ArrayList<>();

            if (txtmDocList.size() > 0) {
                dataTablesSearchResult.setRecordsFiltered(txtmDocList.size());
                dataTablesSearchResult.setRecordsTotal(txtmDocList.size());

                int no = 0;
                for (TxTmDoc result : txtmDocList) {
                    no++;

                    data.add(new String[]{
                            result.getmDocType().getId(),
                            " " + no,
                            DateUtil.formatDate(result.getCreatedDate(), "dd-MM-yyyy"),
                            result.getmDocType().getName(),
                            result.getFileName(),
                            result.getDescription(),
                            result.getFileSize(),
                            result.getId(),
                            ""
                    });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }



    @GetMapping(value = "/getrawquery")
//    @RequestMapping(value = REQUEST_MAPPING_AJAX_LIST_KELAS, method = {RequestMethod.POST})
    @ResponseBody
    public GenericSearchWrapper<Object[]> getRawQuery2() {
//        List<Object> Resulttest =

        List<KeyValue> searchCriteria = new ArrayList<>();
        searchCriteria.add(new KeyValue("statusFlag", true, true));
        searchCriteria.add(new KeyValue("parentClass.statusFlag", true, true));

        String[] excludeArr = null;

        String orderBy = "";
        String orderType = " ";
        int offset = 0;

        int limit = 0;


        GenericSearchWrapper<Object[]> Resulttest = classService.searchClassTest(searchCriteria, excludeArr, orderBy, orderType, offset, limit);


        return Resulttest;
    }


    /* --------------------------------------- KELAS --------------------------------------- */
    @RequestMapping(value = REQUEST_MAPPING_AJAX_LIST_KELAS, method = {RequestMethod.POST})
    //@RequestParam(value = "no", required = false) String appno, -> ditambahkan ini aplikasinya gak jalan
    //@RequestBody DataForm1 dataform, -> ditambahkan ini aplikasinya gak jalan
    public void doSearchDataTablesListKelas(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            DataTablesSearchResult dataTablesSearchResult = new DataTablesSearchResult();
            try {
                dataTablesSearchResult.setDraw(Integer.parseInt(request.getParameter("draw")));
            } catch (NumberFormatException e) {
                dataTablesSearchResult.setDraw(0);
            }

            int offset = 0;
            int limit = 100;
            try {
                offset = Math.abs(Integer.parseInt(request.getParameter("start")));
            } catch (NumberFormatException e) {
            }
            try {
                limit = Math.abs(Integer.parseInt(request.getParameter("length")));
            } catch (NumberFormatException e) {
            }

            String actionType = "";

            String[] excludeArr = request.getParameterValues("excludeArr6[]");
            String[] searchByArr = request.getParameterValues("searchByArr6[]");
            String[] keywordArr = request.getParameterValues("keywordArr6[]");
            actionType = request.getParameter("actionType");


            List<KeyValue> searchCriteria = new ArrayList<>();
            searchCriteria.add(new KeyValue("statusFlag", true, true));
            searchCriteria.add(new KeyValue("parentClass.statusFlag", true, true));

            int oneword = 0;
            int emptyDesc = 1;
            int emptyDescEn = 1;
            int multiDesc = 0;
            int multiDescEn = 0;

            int emptyCondition = 0;
            int goSearch = 0;
            int costumSearch = 0;

            if (searchByArr != null) {
                for (int i = 0; i < searchByArr.length; i++) {
                    String searchBy = searchByArr[i];
                    String value = "";
                    try {
                        value = keywordArr[i];
                    } catch (ArrayIndexOutOfBoundsException e) {
                    }

                    if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                        if (StringUtils.isNotBlank(value)) {
                            if (searchBy.equalsIgnoreCase("descEn")) {
                                emptyDesc = 0;

                                value = value.trim();
                                if (value.split("\\s+").length < 2) {
                                    oneword = 1;
                                }

                                multiDescEn++;
                            }
                            if (searchBy.equalsIgnoreCase("desc")) {

                                emptyDescEn = 0;
                                if (value.split("\\s+").length < 2) {
                                    oneword = 1;
                                }

                                multiDesc++;
                            }
                            if (searchBy.toString().equalsIgnoreCase("parentClass.id")){
                                searchCriteria.add(new KeyValue(searchBy, value, true));
                            }else {
                                searchCriteria.add(new KeyValue(searchBy, value, false));
                            }
//                            System.out.println("Custom search criteria: " + searchBy + " " + value);
                        } else {
                            emptyCondition = 1;
                        }
                    }
                }
            }
            String orderBy = request.getParameter("orderBy");
            if (orderBy != null) {
                orderBy = orderBy.trim();
                if (orderBy.equalsIgnoreCase("")) {
                    orderBy = "orderBy";
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

            if (actionType.equalsIgnoreCase("goSearch")) {
                if ((emptyDesc == 1) && (emptyDescEn == 1)) {
                    costumSearch = 0;
                } else {
                    if (oneword == 0) {
                        costumSearch = 1;
                    } else {
                        costumSearch = 0;
                    }
                }
            } else {
                costumSearch = 0;
            }

            if(multiDesc>1 || multiDescEn>1 || (multiDesc==1 && multiDescEn==1)){
                costumSearch = 1;
            }

            //searchResult = classService.searchGeneralTest(searchCriteria, orderBy, orderType, offset, limit);
            if (costumSearch == 1) {
                List<String[]> data = new ArrayList<>();
                GenericSearchWrapper<Object[]> searchResult = null;
                searchResult = classService.searchClassMultiKeyword(searchCriteria, excludeArr, orderBy, orderType, offset, limit);

                if (searchResult.getCount() > 0) {
                    dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                    dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                    int no = offset;
                    for (Object result[] : searchResult.getList()) {
                        no++;
                        data.add(new String[]{
                                result[3] == null ? "" : result[3].toString(),
                                "" + result[0] == null ? "" : result[0].toString().replace("class_",""),
                                "getSerial1()",
                                result[1] == null ? "" : result[1].toString(),
                                result[2] == null ? "" : result[2].toString(),
                                "getEdition()",
                                "getVersion()",
                                "show",
                                "getType()"
                                //"<button type='button' data-parent=\"" + result.getParentClass().getId() + "\" id='pilihKelasBtn' onclick='pilihKelas(\"" + result.getId() + "\")' " +
                                //        " data-dismiss='modal'>Pilih</button>"
                        });
                    }
                }

                dataTablesSearchResult.setData(data);
                writeJsonResponse(response, dataTablesSearchResult);
                // End awalan

                //end goSearch
            } else {
                List<String[]> data = new ArrayList<>();
                GenericSearchWrapper<MClassDetail> searchResult = null;
                searchResult = classService.searchClassChildExclude(searchCriteria, excludeArr, orderBy, orderType, offset, limit);

                if (searchResult.getCount() > 0) {
                    dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                    dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                    int no = offset;
                    for (MClassDetail result : searchResult.getList()) {
                        no++;
                        data.add(new String[]{
                                result.getId(),
                                "" + result.getParentClass().getNo(),
                                result.getSerial1(),
                                result.getDesc(),
                                result.getDescEn(),
                                result.getParentClass().getEdition().toString(),
                                result.getParentClass().getVersion().toString(),
                                "show",
                                result.getParentClass().getType()
                                //"<button type='button' data-parent=\"" + result.getParentClass().getId() + "\" id='pilihKelasBtn' onclick='pilihKelas(\"" + result.getId() + "\")' " +
                                //        " data-dismiss='modal'>Pilih</button>"

                        });
                    }
                }
                dataTablesSearchResult.setData(data);
                writeJsonResponse(response, dataTablesSearchResult);
                // end NOT goSearch
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }


//            //searchResult = classService.searchGeneralTest(searchCriteria, orderBy, orderType, offset, limit);
//            if (emptyCondition == 1){
//                List<String[]> data = new ArrayList<>();
//
//                GenericSearchWrapper<MClassDetail> searchResult = null;
//                searchResult = classService.searchClassChildExclude(searchCriteria, excludeArr, orderBy, orderType, offset, limit);
//                int no = offset;
//
//                for (MClassDetail result : searchResult.getList()) {
//                    no++;
//                    data.add(new String[]{
//                            result.getId(),
//                            "" + result.getParentClass().getNo(),
//                            result.getSerial1(),
//                            result.getDesc(),
//                            result.getDescEn(),
//                            result.getParentClass().getEdition().toString(),
//                            result.getParentClass().getVersion().toString(),
//                            "show",
//                            result.getParentClass().getType()
//                            //"<button type='button' data-parent=\"" + result.getParentClass().getId() + "\" id='pilihKelasBtn' onclick='pilihKelas(\"" + result.getId() + "\")' " +
//                            //        " data-dismiss='modal'>Pilih</button>"
//
//                    });
//                }
//
//                dataTablesSearchResult.setData(data);
//
//                writeJsonResponse(response, dataTablesSearchResult);
//            }
//            else{
//                List<String[]> data = new ArrayList<>();
//                GenericSearchWrapper<Object[]> searchResult = null;
//                searchResult = classService.searchClassTest(inputstring, searchCriteria, excludeArr, orderBy, orderType, offset, limit);
//
//                if (searchResult.getCount() > 0) {
//                    dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
//                    dataTablesSearchResult.setRecordsTotal(searchResult.getCount());
//
//                    int no = offset;
//                    int  noid = 0 ;
//                    for (Object result[] : searchResult.getList()) {
//                        no++; noid++ ;
//                        data.add(new String[]{
//                                String.valueOf(noid),
//                                "" + result[0] == null ? "" : result[0].toString().replace("class_","Kelas "),
//                                "getSerial1()",
//                                result[1]== null ? "" : result[1].toString(),
//                                result[2] == null ? "" : result[2].toString(),
//                                "getEdition()",
//                                "getVersion()",
//                                "show",
//                                "getType()"
//                                //"<button type='button' data-parent=\"" + result.getParentClass().getId() + "\" id='pilihKelasBtn' onclick='pilihKelas(\"" + result.getId() + "\")' " +
//                                //        " data-dismiss='modal'>Pilih</button>"
//
//                        });
//                    }
//                }
//
//                dataTablesSearchResult.setData(data);
//                writeJsonResponse(response, dataTablesSearchResult);
//                // End awalan
//            }
//
//
//
//
//        } else {
//            response.setStatus(HttpServletResponse.SC_FOUND);
//        }
    }


    /* ---------------------------------------PRATINJAU KELAS --------------------------------------- */
    @RequestMapping(value = REQUEST_MAPPING_AJAX_LIST_KELAS_PETUGAS, method = {RequestMethod.POST})
    //@RequestParam(value = "no", required = false) String appno, -> ditambahkan ini aplikasinya gak jalan
    //@RequestBody DataForm1 dataform, -> ditambahkan ini aplikasinya gak jalan
    public void doSearchDataTablesListKelasPetugas(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            DataTablesSearchResult dataTablesSearchResult = new DataTablesSearchResult();
            try {
                dataTablesSearchResult.setDraw(Integer.parseInt(request.getParameter("draw")));
            } catch (NumberFormatException e) {
                dataTablesSearchResult.setDraw(0);
            }

            int offset = 0;
            int limit = 100;
            try {
                offset = Math.abs(Integer.parseInt(request.getParameter("start")));
            } catch (NumberFormatException e) {
            }
            try {
                limit = Math.abs(Integer.parseInt(request.getParameter("length")));
            } catch (NumberFormatException e) {
            }

            String actionType = "";

            String[] excludeArr = request.getParameterValues("excludeArr6[]");
            String[] searchByArr = request.getParameterValues("searchByArr6[]");
            String[] keywordArr = request.getParameterValues("keywordArr6[]");
            actionType = request.getParameter("actionType");


            List<KeyValue> searchCriteria = new ArrayList<>();
//            searchCriteria.add(new KeyValue("statusFlag", true, false));
//            searchCriteria.add(new KeyValue("parentClass.statusFlag", true, false));

            int oneword = 0;
            int emptyDesc = 1;
            int emptyDescEn = 1;
            int multiDesc = 0;
            int multiDescEn = 0;

            int emptyCondition = 0;
            int goSearch = 0;
            int costumSearch = 0;

            if (searchByArr != null) {
                for (int i = 0; i < searchByArr.length; i++) {
                    String searchBy = searchByArr[i];
                    String value = "";
                    try {
                        value = keywordArr[i];
                    } catch (ArrayIndexOutOfBoundsException e) {
                    }

                    if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                        if (StringUtils.isNotBlank(value)) {
                            if (searchBy.equalsIgnoreCase("descEn")) {
                                emptyDesc = 0;

                                value = value.trim();
                                if (value.split("\\s+").length < 2) {
                                    oneword = 1;
                                }

                                multiDescEn++;
                            }
                            if (searchBy.equalsIgnoreCase("desc")) {

                                emptyDescEn = 0;
                                if (value.split("\\s+").length < 2) {
                                    oneword = 1;
                                }

                                multiDesc++;
                            }
                            if (searchBy.toString().equalsIgnoreCase("parentClass.id")){
                                searchCriteria.add(new KeyValue(searchBy, value, false));
                            }else {
                                searchCriteria.add(new KeyValue(searchBy, value, false));
                            }
//                            System.out.println("Custom search criteria: " + searchBy + " " + value);
                        } else {
                            emptyCondition = 1;
                        }
                    }
                }
            }
            String orderBy = request.getParameter("orderBy");
            if (orderBy != null) {
                orderBy = orderBy.trim();
                if (orderBy.equalsIgnoreCase("")) {
                    orderBy = "orderBy";
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

            if (actionType.equalsIgnoreCase("goSearch")) {
                if ((emptyDesc == 1) && (emptyDescEn == 1)) {
                    costumSearch = 0;
                } else {
                    if (oneword == 0) {
                        costumSearch = 1;
                    } else {
                        costumSearch = 0;
                    }
                }
            } else {
                costumSearch = 0;
            }

            if(multiDesc>1 || multiDescEn>1 || (multiDesc==1 && multiDescEn==1)){
                costumSearch = 1;
            }

            //searchResult = classService.searchGeneralTest(searchCriteria, orderBy, orderType, offset, limit);
            if (costumSearch == 1) {
                List<String[]> data = new ArrayList<>();
                GenericSearchWrapper<Object[]> searchResult = null;
                searchResult = classService.searchClassMultiKeywordPratinjau(searchCriteria, excludeArr, orderBy, orderType, offset, limit);

                if (searchResult.getCount() > 0) {
                    dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                    dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                    int no = offset;
                    for (Object result[] : searchResult.getList()) {
                        no++;
                        data.add(new String[]{
                                result[3] == null ? "" : result[3].toString(),
                                "" + result[0] == null ? "" : result[0].toString().replace("class_",""),
                                "getSerial1()",
                                result[1] == null ? "" : result[1].toString(),
                                result[2] == null ? "" : result[2].toString(),
                                "getEdition()",
                                "getVersion()",
                                "show",
                                "getType()"
                                //"<button type='button' data-parent=\"" + result.getParentClass().getId() + "\" id='pilihKelasBtn' onclick='pilihKelas(\"" + result.getId() + "\")' " +
                                //        " data-dismiss='modal'>Pilih</button>"
                        });
                    }
                }

                dataTablesSearchResult.setData(data);
                writeJsonResponse(response, dataTablesSearchResult);
                // End awalan

                //end goSearch
            } else {
                List<String[]> data = new ArrayList<>();
                GenericSearchWrapper<MClassDetail> searchResult = null;
                searchResult = classService.searchClassChildExclude(searchCriteria, excludeArr, orderBy, orderType, offset, limit);

                if (searchResult.getCount() > 0) {
                    dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                    dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                    int no = offset;
                    for (MClassDetail result : searchResult.getList()) {
                        no++;
                        data.add(new String[]{
                                result.getId(),
                                "" + result.getParentClass().getNo(),
                                result.getSerial1(),
                                result.getDesc(),
                                result.getDescEn(),
                                result.getParentClass().getEdition().toString(),
                                result.getParentClass().getVersion().toString(),
                                "show",
                                result.getParentClass().getType()
                                //"<button type='button' data-parent=\"" + result.getParentClass().getId() + "\" id='pilihKelasBtn' onclick='pilihKelas(\"" + result.getId() + "\")' " +
                                //        " data-dismiss='modal'>Pilih</button>"

                        });
                    }
                }
                dataTablesSearchResult.setData(data);
                writeJsonResponse(response, dataTablesSearchResult);
                // end NOT goSearch
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }


//            //searchResult = classService.searchGeneralTest(searchCriteria, orderBy, orderType, offset, limit);
//            if (emptyCondition == 1){
//                List<String[]> data = new ArrayList<>();
//
//                GenericSearchWrapper<MClassDetail> searchResult = null;
//                searchResult = classService.searchClassChildExclude(searchCriteria, excludeArr, orderBy, orderType, offset, limit);
//                int no = offset;
//
//                for (MClassDetail result : searchResult.getList()) {
//                    no++;
//                    data.add(new String[]{
//                            result.getId(),
//                            "" + result.getParentClass().getNo(),
//                            result.getSerial1(),
//                            result.getDesc(),
//                            result.getDescEn(),
//                            result.getParentClass().getEdition().toString(),
//                            result.getParentClass().getVersion().toString(),
//                            "show",
//                            result.getParentClass().getType()
//                            //"<button type='button' data-parent=\"" + result.getParentClass().getId() + "\" id='pilihKelasBtn' onclick='pilihKelas(\"" + result.getId() + "\")' " +
//                            //        " data-dismiss='modal'>Pilih</button>"
//
//                    });
//                }
//
//                dataTablesSearchResult.setData(data);
//
//                writeJsonResponse(response, dataTablesSearchResult);
//            }
//            else{
//                List<String[]> data = new ArrayList<>();
//                GenericSearchWrapper<Object[]> searchResult = null;
//                searchResult = classService.searchClassTest(inputstring, searchCriteria, excludeArr, orderBy, orderType, offset, limit);
//
//                if (searchResult.getCount() > 0) {
//                    dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
//                    dataTablesSearchResult.setRecordsTotal(searchResult.getCount());
//
//                    int no = offset;
//                    int  noid = 0 ;
//                    for (Object result[] : searchResult.getList()) {
//                        no++; noid++ ;
//                        data.add(new String[]{
//                                String.valueOf(noid),
//                                "" + result[0] == null ? "" : result[0].toString().replace("class_","Kelas "),
//                                "getSerial1()",
//                                result[1]== null ? "" : result[1].toString(),
//                                result[2] == null ? "" : result[2].toString(),
//                                "getEdition()",
//                                "getVersion()",
//                                "show",
//                                "getType()"
//                                //"<button type='button' data-parent=\"" + result.getParentClass().getId() + "\" id='pilihKelasBtn' onclick='pilihKelas(\"" + result.getId() + "\")' " +
//                                //        " data-dismiss='modal'>Pilih</button>"
//
//                        });
//                    }
//                }
//
//                dataTablesSearchResult.setData(data);
//                writeJsonResponse(response, dataTablesSearchResult);
//                // End awalan
//            }
//
//
//
//
//        } else {
//            response.setStatus(HttpServletResponse.SC_FOUND);
//        }
    }


    /*--------------------------------------- KELAS COPY --------------------------------------- */
    @RequestMapping(value = REQUEST_MAPPING_AJAX_LIST_KELAS_COPY, method = {RequestMethod.POST})
    public void doSearchDataTablesListKelasCopy(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            DataTablesSearchResult dataTablesSearchResult = new DataTablesSearchResult();
            try {
                dataTablesSearchResult.setDraw(Integer.parseInt(request.getParameter("draw")));
            } catch (NumberFormatException e) {
                dataTablesSearchResult.setDraw(0);
            }

            int offset = 0;
            int limit = 1000;
            try {
                offset = Math.abs(Integer.parseInt(request.getParameter("start")));
            } catch (NumberFormatException e) {
            }
            try {
                limit = Math.abs(Integer.parseInt(request.getParameter("length")));
            } catch (NumberFormatException e) {
            }

            String actionType = "";

            String[] excludeArr = request.getParameterValues("excludeArr6[]");
            String[] searchByArr = request.getParameterValues("searchByArr6[]");
            String[] keywordArr = request.getParameterValues("keywordArr6[]");
            actionType = request.getParameter("actionType");


            List<KeyValue> searchCriteria = new ArrayList<>();
            searchCriteria.add(new KeyValue("transactionStatus", "ACCEPT", true));

            if (searchByArr != null) {
                for (int i = 0; i < searchByArr.length; i++) {
                    String searchBy = searchByArr[i];
                    String value = "";
                    try {
                        value = keywordArr[i];
                    } catch (ArrayIndexOutOfBoundsException e) {
                    }

                    if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                        if (StringUtils.isNotBlank(value)) {
                            if (searchBy.equalsIgnoreCase("mClassDetail.descEn")) {

                                value = value.trim();
                            }
                            if (searchBy.equalsIgnoreCase("mClassDetail.desc")) {

                                value = value.trim();
                            }
                            if (searchBy.equalsIgnoreCase("txTmGeneral.id")){
                                searchCriteria.add(new KeyValue(searchBy, value, true));
                            }else {
                                searchCriteria.add(new KeyValue(searchBy, value, false));
                            }
//                            System.out.println("Custom search criteria: " + searchBy + " " + value);
                        }
                    }
                }
            }
            String orderBy = request.getParameter("orderBy");
            if (orderBy != null) {
                orderBy = orderBy.trim();
                if (orderBy.equalsIgnoreCase("")) {
                    orderBy = "orderBy";
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
            GenericSearchWrapper<TxTmClass> searchResult = null;
            searchResult = classService.searchGeneralTxClass(searchCriteria, orderBy, orderType, offset, limit);

            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxTmClass result : searchResult.getList()) {
                    no++;
                    data.add(new String[]{
                            result.getmClassDetail().getId(),
                            "" + result.getmClass().getNo(),
                            result.getmClassDetail().getDesc(),
                            result.getmClassDetail().getDescEn()
                    });
                }
            }
            dataTablesSearchResult.setData(data);
            writeJsonResponse(response, dataTablesSearchResult);
            // end NOT goSearch

        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }


    /* --------------------------------------- KELAS LIMITASI --------------------------------------- */
    @RequestMapping(value = REQUEST_MAPPING_AJAX_LIST_KELAS_LIMIT, method = {RequestMethod.POST})
    public void doSearchDataTablesListKelasLimitasi(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            DataTablesSearchResult dataTablesSearchResult = new DataTablesSearchResult();
            try {
                dataTablesSearchResult.setDraw(Integer.parseInt(request.getParameter("draw")));
            } catch (NumberFormatException e) {
                dataTablesSearchResult.setDraw(0);
            }

            int offset = 0;
            int limit = 100;
            try {
                offset = Math.abs(Integer.parseInt(request.getParameter("start")));
            } catch (NumberFormatException e) {
            }
            try {
                limit = Math.abs(Integer.parseInt(request.getParameter("length")));
            } catch (NumberFormatException e) {
            }

            String actionType = "";
            String appNo = request.getParameter("appNo");

            String[] excludeArr = request.getParameterValues("excludeArr6[]");
            String[] searchByArr = request.getParameterValues("searchByArr6[]");
            String[] keywordArr = request.getParameterValues("keywordArr6[]");
            actionType = request.getParameter("actionType");


            List<KeyValue> searchCriteria = new ArrayList<>();
//            searchCriteria.add(new KeyValue("statusFlag", true, true));
//            searchCriteria.add(new KeyValue("parentClass.statusFlag", true, true));
//            searchCriteria.add(new KeyValue("g.applicationNo", appNo, true));
            searchCriteria.add(new KeyValue("txTmGeneral.id", appNo, true));

            int oneword = 0;
            int emptyDesc = 1;
            int emptyDescEn = 1;
            int multiDesc = 0;
            int multiDescEn = 0;

            int emptyCondition = 0;
            int goSearch = 0;
            int costumSearch = 0;

            if (searchByArr != null) {
                for (int i = 0; i < searchByArr.length; i++) {
                    String searchBy = searchByArr[i];
                    String value = "";
                    try {
                        value = keywordArr[i];
                    } catch (ArrayIndexOutOfBoundsException e) {
                    }

                    if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                        if (StringUtils.isNotBlank(value)) {
                            if (searchBy.equalsIgnoreCase("descEn")) {
                                emptyDesc = 0;

                                value = value.trim();
                                if (value.split("\\s+").length < 2) {
                                    oneword = 1;
                                }

                                multiDescEn++;
                            }
                            if (searchBy.equalsIgnoreCase("desc")) {

                                emptyDescEn = 0;
                                if (value.split("\\s+").length < 2) {
                                    oneword = 1;
                                }

                                multiDesc++;
                            }
                            if (searchBy.toString().equalsIgnoreCase("parentClass.id")){
                                searchCriteria.add(new KeyValue(searchBy, value, true));
                            }else {
                                searchCriteria.add(new KeyValue(searchBy, value, false));
                            }
//                            System.out.println("Custom search criteria: " + searchBy + " " + value);
                        } else {
                            emptyCondition = 1;
                        }
                    }
                }
            }
            String orderBy = request.getParameter("orderBy");
            if (orderBy != null) {
                orderBy = orderBy.trim();
                if (orderBy.equalsIgnoreCase("")) {
                    orderBy = "orderBy";
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

            if (actionType.equalsIgnoreCase("goSearch")) {
                if ((emptyDesc == 1) && (emptyDescEn == 1)) {
                    costumSearch = 0;
                } else {
                    if (oneword == 0) {
                        costumSearch = 1;
                    } else {
                        costumSearch = 0;
                    }
                }
            } else {
                costumSearch = 0;
            }

            if(multiDesc>1 || multiDescEn>1 || (multiDesc==1 && multiDescEn==1)){
                costumSearch = 1;
            }

            //searchResult = classService.searchGeneralTest(searchCriteria, orderBy, orderType, offset, limit);
           /* if (costumSearch == 1) {
                List<String[]> data = new ArrayList<>();
                GenericSearchWrapper<Object[]> searchResult = null;
                searchResult = classService.searchClassMultiKeyword(searchCriteria, excludeArr, orderBy, orderType, offset, limit);

                if (searchResult.getCount() > 0) {
                    dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                    dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                    int no = offset;
                    for (Object result[] : searchResult.getList()) {
                        no++;
                        data.add(new String[]{
                                result[3] == null ? "" : result[3].toString(),
                                "" + result[0] == null ? "" : result[0].toString().replace("class_",""),
                                "getSerial1()",
                                result[1] == null ? "" : result[1].toString(),
                                result[2] == null ? "" : result[2].toString(),
                                "getEdition()",
                                "getVersion()",
                                "show",
                                "getType()"
                                //"<button type='button' data-parent=\"" + result.getParentClass().getId() + "\" id='pilihKelasBtn' onclick='pilihKelas(\"" + result.getId() + "\")' " +
                                //        " data-dismiss='modal'>Pilih</button>"
                        });
                    }
                }

                dataTablesSearchResult.setData(data);
                writeJsonResponse(response, dataTablesSearchResult);
                // End awalan

                //end goSearch
            } else {*/
            List<String[]> data = new ArrayList<>();
            GenericSearchWrapper<TxTmClass> searchResult = null;
            searchResult = classService.searchClassChildLimitationExclude(searchCriteria, excludeArr, orderBy, orderType, offset, limit);

            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                //swap txtmclass to mclassdetail
                GenericSearchWrapper<MClassDetail> searchClassDetailResult = new GenericSearchWrapper<MClassDetail>();
                for (TxTmClass txtmclass :searchResult.getList()){
                    searchClassDetailResult.getList().add(txtmclass.getmClassDetail());
                }

                int no = offset;
                for (MClassDetail result : searchClassDetailResult.getList()) {
                    no++;
                    data.add(new String[]{
                            result.getId(),
                            "" + result.getParentClass().getNo(),
                            result.getSerial1(),
                            result.getDesc(),
                            result.getDescEn(),
                            result.getParentClass().getEdition().toString(),
                            result.getParentClass().getVersion().toString(),
                            "show",
                            result.getParentClass().getType()
                            //"<button type='button' data-parent=\"" + result.getParentClass().getId() + "\" id='pilihKelasBtn' onclick='pilihKelas(\"" + result.getId() + "\")' " +
                            //        " data-dismiss='modal'>Pilih</button>"

                    });
                }
            }
            dataTablesSearchResult.setData(data);
            writeJsonResponse(response, dataTablesSearchResult);
            // end NOT goSearch
            //}
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }

    }

    /* --------------------------------------- KUASA --------------------------------------- */
    @RequestMapping(path = REQUEST_MAPPING_KUASA)
    public void doGetDataKuasa(Model model, final HttpServletRequest request, final HttpServletResponse response) {
        MRepresentative mReps = new MRepresentative();
        String req = request.getParameter("target");

        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            mReps = representativeService.selectOne("id", req);
            if (mReps.getmCity() != null) {
                mReps.getmCity().setCreatedBy(null);
                mReps.getmCity().setUpdatedBy(null);
            }
            if (mReps.getmProvince() != null) {
                mReps.getmProvince().setCreatedBy(null);
                mReps.getmProvince().setUpdatedBy(null);
            }
            if (mReps.getmCountry() != null) {
                mReps.getmCountry().setCreatedBy(null);
                mReps.getmCountry().setUpdatedBy(null);
            }
            mReps.setUserId(null);
            mReps.setCreatedBy(null);
            mReps.setUpdatedBy(null);
        }

        writeJsonResponse(response, mReps);
    }

    @RequestMapping(value = REQUEST_MAPPING_AJAX_KUASA_LIST, method = {RequestMethod.GET})
    public void editPermohonanKuasa(final Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        List<MCountry> mCountryList = countryService.findByStatusFlagTrue();
        model.addAttribute("listCountry", mCountryList);

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
            List<KeyValue> searchCriteria = new ArrayList<>();
            searchCriteria.add(new KeyValue("statusFlag", true, false));

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
                        if (searchBy.equalsIgnoreCase("applicationDate")) {
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
                    orderBy = "orderBy";
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

            GenericSearchWrapper<MRepresentative> searchResult = null;
//            if (searchCriteria.isEmpty()) {
//                searchResult = representativeService.showListAll(searchCriteria, orderBy, orderType, offset, limit);
//            } else {
//                searchResult = representativeService.searchGeneral(searchCriteria, orderBy, orderType, offset, limit);
//            }
            searchResult = representativeService.searchGeneral(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (MRepresentative result : searchResult.getList()) {
                    no++;
                    data.add(new String[]{
                            "" + no,
                            result.getNo(),
                            result.getName(),
                            result.getAddress(),
                            "<button type='button' class=\"btn btn-primary btn-kuasa\" idkuasa=\"" + result.getId() + "\" " +
                                    " data-dismiss='modal'>Pilih</button>"
                    });
                }
            }
            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    /* --------------------------------------- PRIORITAS --------------------------------------- */
    @RequestMapping(value = REQUEST_MAPPING_AJAX_LIST_PRIORITAS, method = {RequestMethod.GET})
    public void doSearchDataTablesListPrioritas(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            DataTablesSearchResult dataTablesSearchResult = new DataTablesSearchResult();
            try {
                dataTablesSearchResult.setDraw(Integer.parseInt(request.getParameter("draw")));
            } catch (NumberFormatException e) {
                dataTablesSearchResult.setDraw(0);
            }

            int offset = 0;
            int limit = 20;
            try {
                offset = Math.abs(Integer.parseInt(request.getParameter("start")));
            } catch (NumberFormatException e) {
            }
            try {
                limit = Math.abs(Integer.parseInt(request.getParameter("length")));
            } catch (NumberFormatException e) {
            }

            String appNo = request.getParameter("appNo");


            String orderBy = "no";
            if (orderBy != null) {
                orderBy = orderBy.trim();
                if (orderBy.equalsIgnoreCase("")) {
                    orderBy = "orderBy";
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


            List<KeyValue> searchCriteria = new ArrayList<>();
            MUser user = new MUser();
            user = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            searchCriteria.add(new KeyValue("txTmGeneral.applicationNo", appNo, true));
            //searchCriteria.add(new KeyValue("createdBy.id", user.getId(), true));


            List<String[]> data = new ArrayList<>();

            GenericSearchWrapper<TxTmPrior> searchResult = null;

            searchResult = priorService.searchGeneralTest(searchCriteria, orderBy, orderType, offset, limit);

            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxTmPrior result : searchResult.getList()) {
                    no++;

                    String status = PriorStatusEnum.PENDING.getLabel();

                    try {
                        status = PriorStatusEnum.valueOf(result.getStatus()).getLabel();
                    } catch (NullPointerException e) {
                    }

                    data.add(new String[]{
                            result.getNo(),
                            DateUtil.formatDate(result.getPriorDate(), "dd/MM/yyyy"),
                            result.getmCountry().getName(),
                            result.getNote(),
                            status,
                            result.getId()
                    });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    /* --------------------------------------- TXCLASS --------------------------------------- */
    @RequestMapping(value = REQUEST_MAPPING_AJAX_LIST_TXCLASS, method = {RequestMethod.GET})
    public void doSearchDataTablesListTxClass(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            DataTablesSearchResult dataTablesSearchResult = new DataTablesSearchResult();
            try {
                dataTablesSearchResult.setDraw(Integer.parseInt(request.getParameter("draw")));
            } catch (NumberFormatException e) {
                dataTablesSearchResult.setDraw(0);
            }

            int offset = 0;
            int limit = 5000;
            try {
                offset = Math.abs(Integer.parseInt(request.getParameter("start")));
            } catch (NumberFormatException e) {
            }
            try {
                limit = Math.abs(Integer.parseInt(request.getParameter("length")));
            } catch (NumberFormatException e) {
            }

            String appNo = request.getParameter("appNo");
            String status = request.getParameter("status");

            String orderBy = "mClass.no, c.mClassDetail.desc";
            if (orderBy != null) {
                orderBy = orderBy.trim();
                if (orderBy.equalsIgnoreCase("")) {
                    orderBy = "orderBy";
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


            List<KeyValue> searchCriteria = new ArrayList<>();
            MUser user = new MUser();
            user = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            System.out.println("appNo kelas : "+appNo);
            searchCriteria.add(new KeyValue("txTmGeneral.applicationNo", appNo, true));
            if (status != null) {
            	System.out.println("status kelas : "+status);
            	searchCriteria.add(new KeyValue("transactionStatus", status, true));
            }            
            //searchCriteria.add(new KeyValue("createdBy.id", user.getId(), true));

            List<String[]> data = new ArrayList<>();

            GenericSearchWrapper<TxTmClass> searchResult = null;

            searchResult = classService.searchGeneralTxClass(searchCriteria, orderBy, orderType, offset, limit);

            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxTmClass result : searchResult.getList()) {
                    no++;
                    data.add(new String[]{
                            result.getmClassDetail().getId(),
                            "" + no,
                            "" + result.getmClass().getNo(),
                            result.getmClassDetail().getId(),
                            result.getmClassDetail().getDesc(),
                            result.getmClassDetail().getDescEn(),
                            result.getmClass().getEdition(),
                            result.getNotes(),
                            result.getUpdatedBy().getUsername(),
                            ClassStatusEnum.valueOf(result.getTransactionStatus()).getLabel(),
                            result.isCorrectionFlag() ? "Ya" : "Tidak",
                            "<button>Hapus</button>",
                            "<button>Update</button>"
                    });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    /* --------------------------------------- TXCLASS --------------------------------------- */
    @RequestMapping(value = REQUEST_MAPPING_AJAX_LIST_TXCLASS_LIMIT, method = {RequestMethod.GET})
    public void doSearchDataTablesListTxClassLimit(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            DataTablesSearchResult dataTablesSearchResult = new DataTablesSearchResult();
            try {
                dataTablesSearchResult.setDraw(Integer.parseInt(request.getParameter("draw")));
            } catch (NumberFormatException e) {
                dataTablesSearchResult.setDraw(0);
            }

            int offset = 0;
            int limit = 5000;
            try {
                offset = Math.abs(Integer.parseInt(request.getParameter("start")));
            } catch (NumberFormatException e) {
            }
            try {
                limit = Math.abs(Integer.parseInt(request.getParameter("length")));
            } catch (NumberFormatException e) {
            }

            String appNo = request.getParameter("appNo");

            String orderBy = "mClass.no, c.mClassDetail.desc";
            if (orderBy != null) {
                orderBy = orderBy.trim();
                if (orderBy.equalsIgnoreCase("")) {
                    orderBy = "orderBy";
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


            List<KeyValue> searchCriteria = new ArrayList<>();
            searchCriteria.add(new KeyValue("txTmGeneral.applicationNo", appNo, true));
            //searchCriteria.add(new KeyValue("createdBy.id", user.getId(), true));

            List<String[]> data = new ArrayList<>();

            GenericSearchWrapper<TxTmClassLimitation> searchResult = null;

            searchResult = classService.searchGeneralTxClassLimitation(searchCriteria, orderBy, orderType, offset, limit);

            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxTmClassLimitation result : searchResult.getList()) {
                    no++;
                    data.add(new String[]{
                            result.getmClassDetail().getId(),
                            "" + no,
                            result.getmCountry().getName(),
                            "" + result.getmClass().getNo(),
                            result.getmClassDetail().getDescEn(),
                            result.getmClassDetail().getSerial1(),
                            result.getmCountry().getId(),
                            result.getmClass().getEdition().toString(),
                            result.getmClass().getVersion().toString(),
                            "<button>Hapus</button>",
                            "<button>Update</button>"
                    });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    // MAPPING DARI PERMOHONAN CONTROLLER TAMBAH DOKUMEN DI PRATINJAU PERMOHONAN ONLINE
    public static final String PATH_PRATINJAU_SAVE_FORM_DOKUMEN = "/pratinjau-insert-data-dokumen";
    private static final String REQUEST_MAPPING_AJAX_PRATINJAU_INSERT_DATA_DOKUMEN = PATH_PRATINJAU_SAVE_FORM_DOKUMEN + "*";

    @PostMapping(REQUEST_MAPPING_AJAX_PRATINJAU_INSERT_DATA_DOKUMEN)
    public void doAjaxAdd(@RequestParam("appNo") String appNo, @RequestParam("docList") String docList,
                          final Model model, final HttpServletRequest request, final HttpServletResponse response) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(docList);

                TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(appNo);

                String appId = "<belum ada data>";
                String brand = "<belum ada data>";
                String nm = "<belum ada data>";
                String em = "<belum ada data>";
                String filenm = "<belum ada data>";
                String doctype = "<belum ada data>";
                String tglupload = "<belum ada data>";

                //validasi
                for (JsonNode node : rootNode) {
                    String docId = node.get("docId").toString().replaceAll("\"", "");
                    String docDate = node.get("docDate").toString().replaceAll("\"", "");
                    String docFileName = node.get("docFileName").toString().replaceAll("\"", "");
                    String docDesc = node.get("docDesc").toString().replaceAll("\"", "");
                    String docFileSize = node.get("docFileSize").toString().replaceAll("\"", "");
                    String[] docFile = request.getParameter("file-" + docId).split(",");
                    ;

                    TxTmDoc txTmDoc = new TxTmDoc();
                    txTmDoc.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                    txTmDoc.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                    txTmDoc.setTxTmGeneral(txTmGeneral);
                    txTmDoc.setmDocType(doclampiranService.getDocTypeById(docId));
                    txTmDoc.setDescription(docDesc);
//                txTmDoc.setUploadDate(DateUtil.toDate("dd/MM/yyyy", docDate));
                    txTmDoc.setFileName(docFileName);
                    txTmDoc.setFileSize(docFileSize);
                    txTmDoc.setStatus(false);

                    doclampiranService.saveDokumenLampiran(txTmDoc, docFile[1]);

                    filenm = txTmDoc.getFileName() == null ? filenm : txTmDoc.getFileName();
                    doctype = txTmDoc.getmDocType().getName() == null ? doctype : txTmDoc.getmDocType().getName();
                    appId = txTmGeneral.getApplicationNo() == null ? appId : txTmGeneral.getApplicationNo();
                    brand = txTmGeneral.getTxTmBrand() == null ? brand : txTmGeneral.getTxTmBrand().getName();
                    nm = txTmGeneral.getOwnerList() == null ? nm : txTmGeneral.getOwnerList();
                    em = txTmGeneral.getCreatedBy().getEmail();
                    tglupload = txTmDoc.getCreatedDate().toString() == null ? tglupload : txTmDoc.getCreatedDate().toString();

                }

                //return PATH_POST_DOKUMEN;
                writeJsonResponse(response, 200);

                // olly@docotel.com
                String logo = "static/img/" + logoEmailImage;
                File file = new File(pathImage + logoEmailImage);
                if (file.exists()) {
                    logo = pathImage + logoEmailImage;
                }
                emailService.prepareAndSendForTambahLampiran(em, "DJKI-Notifikasi Email", nm == null ? em : nm, filenm, appId, doctype, brand, tglupload, logo, "eTemplateTambahDokumenPermohonanOL");
                // olly@docotel.com


            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                writeJsonResponse(response, 500);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                writeJsonResponse(response, 500);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    // MAPPING DARI POST PERMOHONAN CONTROLLER
    private static final String PATH_AJAX_LIST_IMAGE_DETAIL = "/list-image";
    private static final String REQUEST_MAPPING_AJAX_LIST_IMAGE_DETAIL = PATH_AJAX_LIST_IMAGE_DETAIL + "*";

    @Autowired
    private BrandService brandService;

    @Value("${upload.file.branddetail.path:}")
    private String uploadFileBrandDetailPath;

    @RequestMapping(value = REQUEST_MAPPING_AJAX_LIST_IMAGE_DETAIL, method = {RequestMethod.POST})
    public void doDataTablesImageDetail(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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

            List<KeyValue> searchCriteria = new ArrayList<>();
            searchCriteria.add(new KeyValue("txTmGeneral.applicationNo", request.getParameter("appNo"), true));

            String orderBy = "id";
            String orderType = "ASC";

            List<String[]> data = new ArrayList<>();

            GenericSearchWrapper<TxTmBrandDetail> searchResult = brandService.searchBrandDetail(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxTmBrandDetail result : searchResult.getList()) {
                    no++;

                    String pathFolder = DateUtil.formatDate(result.getUploadDate(), "yyyy/MM/dd/");
                    File file = new File(uploadFileBrandDetailPath + pathFolder + result.getId() + "." + FilenameUtils.getExtension(result.getFileName()));
                    String image = "";

                    if (file.exists() && !file.isDirectory()) {
                        FileInputStream fileInputStreamReader = new FileInputStream(file);
                        byte[] bytes = new byte[(int) file.length()];
                        fileInputStreamReader.read(bytes);
                        image = result.getFileDataType() + "," + Base64.getEncoder().encodeToString(bytes);
                        fileInputStreamReader.close();
                    }

                    data.add(new String[]{
                            result.getId(),
                            "" + no,
                            result.getUploadDateTemp(),
                            result.getFileName(),
                            result.getSize(),
                            result.getFileDescription(),
                            image,
                            ""
                    });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @PostMapping(REQUEST_MAPPING_AJAX_SAVE_TAMBAH_LISENSI)
    @ResponseBody
    public void doSaveTambahLisensi(@RequestBody TxLicense txLicense, final HttpServletRequest request, final HttpServletResponse response) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            TxLicense existingLicense = licenseService.selectOneByApplicationId(txLicense.getTxTmGeneral().getApplicationNo());
            if (existingLicense != null) {
                txLicense.setId(existingLicense.getId());
            }
            Timestamp tmstm = new Timestamp(System.currentTimeMillis());
            txLicense.setCreatedDate(tmstm);
            txLicense.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(txLicense.getTxTmGeneral().getApplicationNo());
            txLicense.setTxTmGeneral(txTmGeneral);
            txLicense.setStatus(true);
            if (txLicense.getTxLicenseParent().getId().equalsIgnoreCase("")) {
                txLicense.setTxLicenseParent(null);
            }
            txLicense.setLevel(txLicense.getLevel() + 1);
            try {
                if (txLicense.getmCity() != null && txLicense.getmCity().getCurrentId() == null || "".equalsIgnoreCase(txLicense.getmCity().getCurrentId())) {
                    txLicense.setmCity(null);
                }
                if (txLicense.getmProvince() != null && txLicense.getmProvince().getCurrentId() == null || "".equalsIgnoreCase(txLicense.getmProvince().getCurrentId())) {
                    txLicense.setmProvince(null);
                }
                licenseService.insertLicense(txLicense);

                writeJsonResponse(response, 200);
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                writeJsonResponse(response, 500);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @PostMapping(REQUEST_MAPPING_AJAX_SAVE_TAMBAH_PEMOHON)
    @ResponseBody
    public void doSaveTambahPemohon(@RequestBody TxTmOwner pemohon, final HttpServletRequest request, final HttpServletResponse response) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            Timestamp tmstm = new Timestamp(System.currentTimeMillis());
            pemohon.setCreatedDate(tmstm);
            pemohon.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            String appNo = pemohon.getTxTmGeneral().getApplicationNo() ;
            TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(appNo);
            pemohon.setTxTmGeneral(txTmGeneral);
            pemohon.setStatus(true);
            for (TxTmOwnerDetail txTmOwnerDetail : pemohon.getTxTmOwnerDetails()) {
                txTmOwnerDetail.setTxTmOwner(pemohon);
                txTmOwnerDetail.setTxTmGeneral(pemohon.getTxTmGeneral());
                txTmOwnerDetail.setStatus(true);
            }
            try {
//                txTmOwnerCustomRepository.hardDelete("TM_OWNER_ID", "KANDIDAT-"+txTmGeneral.getId());
                if (pemohon.getmCity() != null && pemohon.getmCity().getCurrentId() == null || "".equalsIgnoreCase(pemohon.getmCity().getCurrentId())) {
                    pemohon.setmCity(null);
                }
                if (pemohon.getmProvince() != null && pemohon.getmProvince().getCurrentId() == null || "".equalsIgnoreCase(pemohon.getmProvince().getCurrentId())) {
                    pemohon.setmProvince(null);
                }

                if (pemohon.getPostCity() != null && (pemohon.getPostCity().getCurrentId() == null || "".equalsIgnoreCase(pemohon.getPostCity().getCurrentId()))) {
                    pemohon.setPostCity(null);
                }
                if (pemohon.getPostProvince() != null && (pemohon.getPostProvince().getCurrentId() == null || "".equalsIgnoreCase(pemohon.getPostProvince().getCurrentId()))) {
                    pemohon.setPostProvince(null);
                }

                if (pemohon.getPostCountry() != null && (pemohon.getPostCountry().getCurrentId() == null || "".equalsIgnoreCase(pemohon.getPostCountry().getCurrentId()))) {
                    pemohon.setPostCountry(null);
                }
                pemohon.setId(UUID.randomUUID().toString());
                pemohon.setStatus(true);
                //set all owner status to 0 before insert new one
                //List<TxTmOwner> owners = permohonanService.selectAllOwnerByGeneralId(txTmGeneral.getId());
                //for(TxTmOwner o : owners){
                 //   o.setStatus(false);
                  //  permohonanService.save(o);
                //}
                //then insert new pemohon with status 1
                permohonanService.insertPemohon(pemohon);
                writeJsonResponse(response, 200);
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                writeJsonResponse(response, 500);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @RequestMapping(value = REQUEST_MAPPING_AJAX_SAVE_EDIT_DOKUMEN_LAMPIRAN, method = {RequestMethod.POST})
    public void doSaveDocOnline(@RequestParam("appNo") String appNo, @RequestParam("docList") String docList, @RequestParam("docFiles") MultipartFile uploadFile,
                                final Model model, final HttpServletRequest request, final HttpServletResponse response) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(docList);

            TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(appNo);
            KeyValue msg = new KeyValue();
            msg.setKey("Success");
            msg.setValue("Input Data Success");

            // Validasi file
            for (JsonNode node : rootNode) {
                String docId = node.get("docId").toString().replaceAll("\"", "");
                String docFileName = node.get("docFileName").toString().replaceAll("\"", "");

                if ((docId.equalsIgnoreCase("TTDP") || docId.equalsIgnoreCase("TTDK")  || docId.equalsIgnoreCase("TTD")) && !(docFileName.toUpperCase().endsWith(".JPG") || docFileName.toUpperCase().endsWith(".JPEG"))) {
                    msg.setKey("fileTypeError");
                    msg.setValue("notMatchFileTypeImage");
                } else if (!(docId.equalsIgnoreCase("TTDP") || docId.equalsIgnoreCase("TTDK")  || docId.equalsIgnoreCase("TTD")) && !docFileName.toUpperCase().endsWith(".PDF")) {
                    msg.setKey("fileTypeError");
                    msg.setValue("notMatchFileTypePdf");
                }
            }

            if (!msg.getKey().equalsIgnoreCase("fileTypeError")) {
                for (JsonNode node : rootNode) {
                    String docId = node.get("docId").toString().replaceAll("\"", "");
                    String docFileName = node.get("docFileName").toString().replaceAll("\"", "");
                    String docDesc = node.get("docDesc").toString().replaceAll("\"", "");
                    String docFileSize = node.get("docFileSize").toString().replaceAll("\"", "");
                    String[] docFile = request.getParameter("file-" + docId).split(",");
                    ;

                    TxTmDoc txTmDoc = new TxTmDoc();
                    txTmDoc.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                    txTmDoc.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                    txTmDoc.setTxTmGeneral(txTmGeneral);
                    txTmDoc.setmDocType(doclampiranService.getDocTypeById(docId));
                    txTmDoc.setDescription(docDesc);
                    txTmDoc.setFileName(docFileName);
                    txTmDoc.setFileSize(docFileSize);

                    String oldFileName = null;
                    if (uploadFile != null && (docId.equalsIgnoreCase("TTDP") || docId.equalsIgnoreCase("TTDK")
                            || docId.equalsIgnoreCase("TTD"))) {
                        oldFileName = txTmDoc.getFileName();
                        txTmDoc.setFileImageTtd(uploadFile);
                        txTmDoc.setFileName(docFileName);
                    }

                    doclampiranService.saveDocEditPratinjauPermohonan(txTmDoc, docFile[1], oldFileName);
                }
            }

            response.setStatus(HttpServletResponse.SC_OK);
            writeJsonResponse(response, msg);
        } catch (DataIntegrityViolationException e) {
            logger.error(e.getMessage(), e);
            writeJsonResponse(response, 500);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            writeJsonResponse(response, 500);
        }
    }

    @RequestMapping(value = REQUEST_MAPPING_DOWNLOAD_DOKUMEN_LAMPIRAN_PEMOHON, method = {RequestMethod.GET})
    @ResponseBody
    public String doDownloadDokumenPemohon(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

        String docId = request.getParameter("id");
        TxTmDoc txTmDoc = doclampiranService.findOne(docId);

        String folder = "";
        String fileName = "";

        if (txTmDoc.getFileName().toUpperCase().endsWith(".JPG") || txTmDoc.getFileName().toUpperCase().endsWith(".JPEG")) {
            response.setContentType("image/jpeg");
            response.setHeader("Content-disposition", "inline; filename=DokumenLampiran-" + txTmDoc.getFileNameTemp());
            folder = uploadFileImageTandaTangan + DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
            fileName = txTmDoc.getFileNameTemp();
        } else {
            response.setContentType("application/pdf");
            response.setHeader("Content-disposition", "inline; filename=DokumenLampiran-" + txTmDoc.getFileNameTemp());
            folder = uploadFileDocApplicationPath + DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
            fileName = txTmDoc.getFileNameTemp();
        }

        try (OutputStream output = response.getOutputStream()) {
            Path path = Paths.get(folder + fileName);
            Files.copy(path, output);
            output.flush();
        } catch (IOException e) {
        }

        return "";
    }

    @RequestMapping(value = REQUEST_MAPPING_AJAX_DELETE_DOKUMEN_PEMOHON, method = RequestMethod.POST)
    public void doDeleteDocPemohon(Model model, @RequestParam("idDoc") String id, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        doclampiranService.deleteDokumen(id);
    }

    @RequestMapping(value = REQUEST_MAPPING_VIEW_DOKUMEN_LAMPIRAN_PEMOHON, method = {RequestMethod.GET})
    @ResponseBody
    public String doViewDokumenPemohon(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

        String docId = request.getParameter("id");
        TxTmDoc txTmDoc = doclampiranService.findOne(docId);

        String folder = "";
        String fileName = "";

        if (txTmDoc.getFileName().toUpperCase().endsWith(".JPG") || txTmDoc.getFileName().toUpperCase().endsWith(".JPEG")) {
            response.setContentType("image/jpeg");
            response.setHeader("Content-disposition", "inline; filename=DokumenLampiran-" + txTmDoc.getFileNameTemp());
            folder = uploadFileImageTandaTangan + DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
            fileName = txTmDoc.getFileNameTemp();
        } else {
            response.setContentType("application/pdf");
            response.setHeader("Content-disposition", "inline; filename=DokumenLampiran-" + txTmDoc.getFileNameTemp());
            folder = uploadFileDocApplicationPath + DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
            fileName = txTmDoc.getFileNameTemp();
        }

        try (OutputStream output = response.getOutputStream()) {
            Path path = Paths.get(folder + fileName);
            //System.out.println(path);
            Files.copy(path, output);
            output.flush();
        } catch (IOException e) {
        }

        return "";
    }

    @RequestMapping(path = REQUEST_MAPPING_VALIDATE_DOKUMEN, method = {RequestMethod.POST})
    public void doValidateDokumen(@RequestParam("Id") String postId, @RequestParam("docList") String docList,
                                  final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(docList);

                List<TxTmDoc> listTxTmDoc = new ArrayList<>();
                for (JsonNode node : rootNode) {
                    String docId = node.get("docId").toString().replaceAll("\"", "");
                    String docDate = node.get("docDate").toString().replaceAll("\"", "");
                    String docFileName = node.get("docFileName").toString().replaceAll("\"", "");
                    String docDesc = node.get("docDesc").toString().replaceAll("\"", "");
                    String docFileSize = node.get("docFileSize").toString().replaceAll("\"", "");
                    String[] docFile = request.getParameter("file-" + docId).split(",");
                    InputStream stream = new ByteArrayInputStream(Base64.getDecoder().decode((docFile[1].getBytes())));
                    PdfReader reader = new PdfReader(stream);
                    if(reader.isEncrypted()){
                        writeJsonResponse(response, 500);
                        return;
                    }
                }
                writeJsonResponse(response, 200);
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                writeJsonResponse(response, 500);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                writeJsonResponse(response, 500);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    /* --------------------------------------- TXCLASS_NEW_REQUEST --------------------------------------- */
    @RequestMapping(value = REQUEST_MAPPING_AJAX_LIST_TXCLASS_NEW_REQUEST, method = {RequestMethod.GET})
    public void doSearchDataTablesListTxClassNewRequest(final HttpServletRequest request,
                                                        final HttpServletResponse response) throws IOException {

        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            DataTablesSearchResult dataTablesSearchResult = new DataTablesSearchResult();
            try {
                dataTablesSearchResult.setDraw(Integer.parseInt(request.getParameter("draw")));
            } catch (NumberFormatException e) {
                dataTablesSearchResult.setDraw(0);
            }

            int offset = 0;
            int limit = 5000;
            try {
                offset = Math.abs(Integer.parseInt(request.getParameter("start")));
            } catch (NumberFormatException e) {
            }
            try {
                limit = Math.abs(Integer.parseInt(request.getParameter("length")));
            } catch (NumberFormatException e) {
            }

            String appNo = request.getParameter("appNo");

            String orderBy = "mClass.no, c.mClassDetail.desc";
            if (orderBy != null) {
                orderBy = orderBy.trim();
                if (orderBy.equalsIgnoreCase("")) {
                    orderBy = "orderBy";
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

            List<KeyValue> searchCriteria = new ArrayList<>();
            MUser user = new MUser();
            user = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            System.out.println("appNo kelas baru : "+appNo);
            searchCriteria.add(new KeyValue("txTmGeneral.applicationNo", appNo, true));
            searchCriteria.add(new KeyValue("transactionStatus", "PENDING", true));

            List<String[]> data = new ArrayList<>();

            GenericSearchWrapper<TxTmClass> searchResult = null;

            searchResult = classService.searchGeneralTxClass(searchCriteria, orderBy, orderType, offset, limit);

            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxTmClass result : searchResult.getList()) {
                    no++;
                    data.add(new String[]{
                            result.getmClassDetail().getId(),
                            "" + no,
                            "" + result.getmClass().getNo(),
                            result.getmClassDetail().getId(),
                            result.getmClassDetail().getDesc(),
                            result.getmClassDetail().getDescEn(),
                            result.getmClass().getEdition(),
                            result.getmClassDetail().getSerial1(),
                            result.getUpdatedBy().getUsername(),
                            ClassStatusEnum.valueOf(result.getTransactionStatus()).getLabel(),
                            result.isCorrectionFlag() ? "Ya" : "Tidak",
                            "<button>Hapus</button>",
                            "<button>Update</button>"
                    });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }

    }


    /* --------------------------------------- TXCLASS --------------------------------------- */
    @RequestMapping(value =  REQUEST_MAPPING_AJAX_LIST_TXCLASS_PEMOHON_ONLINE, method = {RequestMethod.GET})
    public void doSearchDataTablesListTxClassPemohonOnline(final HttpServletRequest request,
                                                           final HttpServletResponse response) throws IOException {

        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            DataTablesSearchResult dataTablesSearchResult = new DataTablesSearchResult();
            try {
                dataTablesSearchResult.setDraw(Integer.parseInt(request.getParameter("draw")));
            } catch (NumberFormatException e) {
                dataTablesSearchResult.setDraw(0);
            }

            int offset = 0;
            int limit = 5000;
            try {
                offset = Math.abs(Integer.parseInt(request.getParameter("start")));
            } catch (NumberFormatException e) {
            }
            try {
                limit = Math.abs(Integer.parseInt(request.getParameter("length")));
            } catch (NumberFormatException e) {
            }

            String appNo = request.getParameter("appNo");

            String orderBy = "mClass.no, c.mClassDetail.desc";
            if (orderBy != null) {
                orderBy = orderBy.trim();
                if (orderBy.equalsIgnoreCase("")) {
                    orderBy = "orderBy";
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

            List<KeyValue> searchCriteria = new ArrayList<>();
            MUser user = new MUser();
            user = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            searchCriteria.add(new KeyValue("txTmGeneral.applicationNo", appNo, true));
            searchCriteria.add(new KeyValue("transactionStatus", "ACCEPT", true));

            //searchCriteria.add(new KeyValue("createdBy.id", user.getId(), true));

            List<String[]> data = new ArrayList<>();

            GenericSearchWrapper<TxTmClass> searchResult = null;

            searchResult = classService.searchGeneralTxClass(searchCriteria, orderBy, orderType, offset, limit);

            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxTmClass result : searchResult.getList()) {
                    no++;
                    data.add(new String[]{
                            result.getmClassDetail().getId(),
                            "" + no,
                            "" + result.getmClass().getNo(),
                            result.getmClassDetail().getId(),
                            result.getmClassDetail().getDesc(),
                            result.getmClassDetail().getDescEn(),
                            result.getmClass().getEdition(),
                            result.getNotes(),
                            result.getUpdatedBy().getUsername(),
                            ClassStatusEnum.valueOf(result.getTransactionStatus()).getLabel(),
                            result.isCorrectionFlag() ? "Ya" : "Tidak",
                            "<button>Hapus</button>",
                            "<button>Update</button>"
                    });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }

    }
}
