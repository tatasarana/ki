package com.docotel.ki.controller;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.*;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.NativeQueryRepository;
import com.docotel.ki.repository.custom.transaction.TxServDistCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxSimilarityResultCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmGeneralCustomRepository;
import com.docotel.ki.repository.master.MStatusRepository;
import com.docotel.ki.service.master.ClassService;
import com.docotel.ki.service.master.StatusService;
import com.docotel.ki.service.transaction.PemeriksaanSubstantifService;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.util.Soundex;
import com.docotel.ki.util.ValidationUtil;
import com.docotel.ki.enumeration.GroupDetailStatusEnum;
import com.docotel.ki.model.transaction.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.jaxb.Context;
import org.docx4j.model.structure.PageSizePaper;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.model.table.TblFactory;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.vml.CTFill;
import org.docx4j.wml.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.*;

import javax.management.relation.Relation;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBElement;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.Principal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class PemeriksaanSubstantifController extends BaseController {
    public static final String PATH_AJAX_ADD_SIMILARITY_MARK = "/tandai-kesamaan";
    public static final String PATH_AJAX_REMOVE_SIMILARITY_MARK = "/hapus-tanda-kesamaan";
    public static final String PATH_AJAX_SEARCH_LIST = "/cari-pemeriksaan-substantif";
    public static final String PATH_AJAX_SEARCH_CEK_KESAMAAN = "/cari-cek-kesamaan";
    public static final String PATH_AJAX_SEARCH_RESULT_KESAMAAN = "/cari-result-kesamaan";
    public static final String PATH_PEMERIKSAAN_SUBSTANTIF = "/pemeriksaan-substantif";
    public static final String PATH_CEK_KESAMAAN = "/cek-kesamaan";
    public static final String PATH_TAMBAH_CEK_KESAMAAN = "/tambah-cek-kesamaan";
    public static final String PATH_PHONETIC_CHECK = "/phonetic-check";
    public static final String PATH_PHONETIC_RESULT = "/phonetic-result";
    public static final String PATH_DOWNLOAD_DOCX = "/download-docx";
    public static final String PATH_RELEASE_PEMERIKSAAN_SUBSTANTIF = "/release-pemeriksaan-substantif";
    public static final String PATH_REASSIGN_P1 = "/reassign-p1";
    public static final String PATH_REASSIGN_PEMERIKSAAN_SUBSTANTIF = "/reassign-pemeriksaan-substantif";
    public static final String PATH_VALIDASI_PEMERIKSAAN_SUBSTANTIF = "/validasi-pemeriksaan-substantif";
    public static final String PATH_SELESAI_PEMERIKSAAN_SUBSTANTIF = "/selesai-pemeriksaan-substantif";
    public static final String PATH_PRANTINJAU_PERMOHONAN = "/pratinjau-permohonan";
    public static final String PATH_DELETE_SUBSCHECK = "/delete-subscheck";
    public static final String REQUEST_MAPPING_PEMERIKSAAN_SUBSTANTIF = PATH_PEMERIKSAAN_SUBSTANTIF + "*";
    public static final String REQUEST_MAPPING_CEK_KESAMAAN = PATH_CEK_KESAMAAN + "*";
    public static final String REQUEST_MAPPING_TAMBAH_CEK_KESAMAAN = PATH_TAMBAH_CEK_KESAMAAN + "*";
    public static final String REQUEST_MAPPING_PHONETIC_RESULT = PATH_PHONETIC_RESULT + "*";
    public static final String REQUEST_MAPPING_PHONETIC_CHECK = PATH_PHONETIC_CHECK + "*";
    public static final String REQUEST_RELEASE_PEMERIKSAAN_SUBSTANTIF = PATH_RELEASE_PEMERIKSAAN_SUBSTANTIF + "*";
    public static final String REQUEST_REASSIGN_P1 = PATH_REASSIGN_P1 + "*";
    public static final String REQUEST_REASSIGN_PEMERIKSAAN_SUBSTANTIF = PATH_REASSIGN_PEMERIKSAAN_SUBSTANTIF + "*";
    public static final String REQUEST_VALIDASI_PEMERIKSAAN_SUBSTANTIF = PATH_VALIDASI_PEMERIKSAAN_SUBSTANTIF + "*";
    public static final String REQUEST_SELESAI_PEMERIKSAAN_SUBSTANTIF = PATH_SELESAI_PEMERIKSAAN_SUBSTANTIF + "*";
    public static final String REDIRECT_CEK_SUBSTANTIF = "redirect:" + PATH_AFTER_LOGIN + PATH_PEMERIKSAAN_SUBSTANTIF;
    private static final String ROLE_SUBSTANTIF_ADMIN = "admin";
    private static final String DIRECTORY_PAGE = "pemeriksaan-substantif/";
    private static final String PAGE_PEMERIKSAAN_SUBSTANTIF = DIRECTORY_PAGE + "pemeriksaan-substantif";
    private static final String PAGE_CEK_KESAMAAN = DIRECTORY_PAGE + "cek-kesamaan";
    private static final String REQUEST_MAPPING_AJAX_ADD_SIMILARITY_MARK = PATH_AJAX_ADD_SIMILARITY_MARK + "*";
    private static final String REQUEST_MAPPING_AJAX_REMOVE_SIMILARITY_MARK = PATH_AJAX_REMOVE_SIMILARITY_MARK + "*";
    private static final String REQUEST_MAPPING_AJAX_SEARCH_LIST = PATH_AJAX_SEARCH_LIST + "*";
    private static final String REQUEST_MAPPING_AJAX_SEARCH_CEK_KESAMAAN = PATH_AJAX_SEARCH_CEK_KESAMAAN + "*";
    private static final String REQUEST_MAPPING_AJAX_SEARCH_RESULT_KESAMAAN = PATH_AJAX_SEARCH_RESULT_KESAMAAN + "*";
    private static final String REQUEST_MAPPING_AJAX_DELETE_SUBSCHECK = PATH_DELETE_SUBSCHECK + "*";

    @Autowired
    private PemeriksaanSubstantifService pemeriksaanService;
    @Autowired
    private ClassService classService;

    @Autowired
    NativeQueryRepository nativeQueryRepository ;

    @Autowired
    TxTmGeneralCustomRepository txTmGeneralCustomRepository;

    @Autowired
    TxSimilarityResultCustomRepository txSimilarityResultCustomRepository;

    @Autowired
    StatusService statusService;

    @Value("${upload.file.brand.path:}")
    private String uploadFileBrandPath;

    @Value("${upload.file.ipasimage.path:}")
    private String uploadFileIpasPath;

    @Value("${upload.file.web.image.default.logo:}")
    private String defaultLogo;

    /***************************** - PEMERIKSAAN SUBSTANTIF SECTION - ****************************/
    @ModelAttribute
    public void addModelAttribute(@RequestParam(value = "error", required = false) String error, final Model model, final HttpServletRequest request, final HttpServletResponse response) {
        model.addAttribute("menu", "pemeriksaanSubstantif");
    }
    /***************************** - END PEMERIKSAAN SUBSTANTIF SECTION - ****************************/

    /***************************** - PEMERIKSAAN SUBSTANTIF SECTION - ****************************/
    @RequestMapping(value = REQUEST_MAPPING_PEMERIKSAAN_SUBSTANTIF, method = {RequestMethod.GET})
    public String doShowPagePemeriksaanSubstantif(@RequestParam(value = "error", required = false) String error, final Model model, final HttpServletRequest request, final HttpServletResponse response) {

        model.addAttribute("classList", classService.findAllMClass());
        model.addAttribute("statusList", statusService.selectStatus());

        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("errorMessage", error);
        }

        return PAGE_PEMERIKSAAN_SUBSTANTIF;
    }
    /***************************** - END PEMERIKSAAN SUBSTANTIF SECTION - ****************************/

    /***************************** - CARI PEMERIKSAAN SUBSTANTIF SECTION - ****************************/
    @RequestMapping(value = REQUEST_MAPPING_AJAX_SEARCH_LIST, method = {RequestMethod.POST})
    public void doSearchDataPemeriksaanSubstantif(final HttpServletRequest request, final HttpServletResponse response, Principal principal) throws IOException {
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
            searchCriteria.add(new KeyValue("groupDetailStatus", GroupDetailStatusEnum.RELEASE.name(), true));

            if (searchByArr != null) {
                for (int i = 0; i < searchByArr.length; i++) {
                    String searchBy = searchByArr[i];
                    String value = null;
                    try {
                        value = keywordArr[i];
                    } catch (ArrayIndexOutOfBoundsException e) {
                    }
                    if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                        if (searchBy.equalsIgnoreCase("txTmGeneral.filingDate")) {
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

            String orderBy = request.getParameter("order[0][column]");
            if (orderBy != null) {
                orderBy = orderBy.trim();
                if (orderBy.equalsIgnoreCase("")) {
                    orderBy = null;
                } else {
                    //Tanggal Penerimaan	Nomor Permohonan	Nama Merek	Kelas	Nama Pemohon	Status Distribusi	Pemeriksa

                    switch (orderBy) {
                        case "1":
                            orderBy = "txTmGeneral.applicationNo";
                            break;
                        case "2":
                            orderBy = "txTmGeneral.filingDate";
                            break;
                        case "3":
                            orderBy = "txTmGeneral.txTmBrand.name";
                            break;
                        case "4":
                            orderBy = "txTmGeneral.txTmBrand.name";
                            break;
                        case "5":
                            orderBy = "txTmGeneral.classList";
                            break;
                        case "6":
                            orderBy = "txTmGeneral.ownerList";
                            break;
                        case "7":
                            orderBy = "status";
                            break;
                        case "8":
                            orderBy = "txTmGeneral.priorList";
                            break;
                        case "9":
                            orderBy = "txTmGeneral.txTmPriorList.priorDate";
                            break;
                        case "10":
                            orderBy = "groupDetailStatus";
                            break;
                        default:
                            //orderBy = "mUserCurrent.mEmployee.employeeName";
                        	orderBy = "mUserCurrent.username";
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

            GenericSearchWrapper<TxGroupDetail> searchResult = pemeriksaanService.searchServDist(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxGroupDetail result : searchResult.getList()) {
                    no++;

                    String classList = "-";
                    String ownerNameList = "-";
                    String pemeriksaName = "-";
                    String brandName = "-";
                    boolean subsCheck = false;
                    FileInputStream brandLabel = null;
                    File file = null;
                    byte[] byteImage = null;
                    String sbPriorDateList = "-";
                    ArrayList<String> spriorDate = new ArrayList<String>();

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
                    	brandName = result.getTxTmGeneral().getTxTmBrand().getName();
                    } catch (NullPointerException e) {
                    }

                    try {
                        subsCheck = result.getTxTmGeneral().getTxSubsCheckList().size() > 0 ? true : false;
                    } catch (NullPointerException e){

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

                    try {
                        pemeriksaName = result.getmUserCurrent().getmEmployee().getEmployeeName();
                    } catch (NullPointerException e) {
                    }

                    try {
                        String pathFolder =  DateUtil.formatDate(result.getTxTmGeneral().getTxTmBrand().getCreatedDate(), "yyyy/MM/dd/");
                        //brandLabel = new FileInputStream(uploadFileBrandPath + pathFolder + result.getTxTmGeneral().getTxTmBrand().getId() + ".jpg");
                        file = new File (uploadFileBrandPath + pathFolder + result.getTxTmGeneral().getTxTmBrand().getId() + ".jpg");
                		if (file.isFile() && file.canRead()) {
                			brandLabel = new FileInputStream(uploadFileBrandPath + pathFolder + result.getTxTmGeneral().getTxTmBrand().getId() + ".jpg");
                		} else {
                			file = new File (uploadFileBrandPath + uploadFileIpasPath + result.getTxTmGeneral().getTxTmBrand().getId() + ".jpg");
                			if (file.isFile() && file.canRead()) {
                				brandLabel = new FileInputStream(uploadFileBrandPath + uploadFileIpasPath + result.getTxTmGeneral().getTxTmBrand().getId() + ".jpg");
                			} else {
                                brandLabel = new FileInputStream(defaultLogo);
                				//System.out.println("File gambar " + result.getTxTmGeneral().getTxTmBrand().getId() + ".jpg tidak ditemukan"
                					//	+ " di folder "+ uploadFileBrandPath + pathFolder
                					//	+ " maupun di di folder " + uploadFileBrandPath + uploadFileIpasPath );
                			}
                		}

                        byteImage = new byte[brandLabel.available()];
                        brandLabel.read(byteImage);
                        brandLabel.close();
                    } catch (NullPointerException e) {
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//
                    try {
                        for (TxTmPrior txtmPrior : result.getTxTmGeneral().getTxTmPriorList()) {
                            spriorDate.add("" + txtmPrior.getPriorDateTemp());
                        }
                        Set<String> temp = new LinkedHashSet<String>(spriorDate);
                        String[] unique = temp.toArray(new String[temp.size()]);
                        if (unique.length > 0) {
                            sbPriorDateList = String.join(",", unique);
                        }

                    } catch (NullPointerException e) {
                    }

                    Long sss = Long.valueOf(0);
                    String op = "" ;
                    try{
                        String no_app = result.getTxTmGeneral().getApplicationNo();
                        GenericSearchWrapper <Object[]> Oposisi = nativeQueryRepository.searchOposisi(result.getTxTmGeneral().getId());
                        int sum_oposisi = Oposisi.getList().size();
                        sss = Long.valueOf(sum_oposisi);
                        op =  sss.toString();
                        if (!op.equalsIgnoreCase("0")){
//                            op = "<a href=\"layanan/monitor-oposisi/?no="+no_app+"\" ><b>"+op+"</b></a>";
                            op = "<a target=\"_blank\" href=\"layanan/list-monitor/?no="+no_app+"\" ><b>"+op+"</b></a>";

                        }
                    }catch(Exception e){}

                    data.add(new String[]{
                            "" + no,
                            "<a href=\"" + getPathURLAfterLogin(PATH_CEK_KESAMAAN + "?id=" + result.getId()) + "\">" + result.getTxTmGeneral().getApplicationNo() + "</a>",
                            result.getTxTmGeneral().getFilingDateTemp(),
                            (byteImage != null ? "data:image/jpeg;base64, " + Base64Utils.encodeToString(byteImage) + "" : ""),
                            brandName,//result.getTxTmGeneral().getTxTmBrand().getName(),
                            classList,
                            ownerNameList,
                            result.getTxTmGeneral().getmStatus().getName(),
                            op,
                            sbPriorDateList,
                            GroupDetailStatusEnum.valueOf(result.getStatus()).getLabel(),
                            pemeriksaName,
                            String.valueOf(subsCheck),
                    });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }
    /***************************** - END CARI PEMERIKSAAN SUBSTANTIF SECTION - ****************************/

    /***************************** - CEK KESAMAAN SECTION - ****************************/
    @RequestMapping(value = REQUEST_MAPPING_CEK_KESAMAAN, method = {RequestMethod.GET})
    public String doShowPageCekKesamaan(final Model model, final HttpServletRequest request, final HttpServletResponse response) {
        TxGroupDetail txGroupDetail = pemeriksaanService.selectOneTxGroupDetail("id", request.getParameter("id"));

        if (txGroupDetail == null) {
            return REDIRECT_CEK_SUBSTANTIF + "?error=Nomor tidak ditemukan";
        }
        
        String classNo = "";
        List<TxTmClass> txTmClasses = classService.findTxTmClassByTxTmGeneral(txGroupDetail.getTxTmGeneral());
        Collections.sort(txTmClasses, (o1, o2) -> new Integer(o1.getmClass().getNo()).compareTo(new Integer(o2.getmClass().getNo())));

        if(txTmClasses.size()>0){
            for(TxTmClass txTmClass: txTmClasses){
                MClass mClass = txTmClass.getmClass();
                if(!classNo.contains(mClass.getNo().toString())) { //agar tidak duplicate no class
                    classNo = classNo + mClass.getNo() + ",";
                }
            }
            classNo = classNo.substring(0, classNo.length() - 1);
        }
        model.addAttribute("classNo", classNo);

        List<MClass> listClass = classService.findAllMClass();
        Collections.sort(listClass, (o1, o2) -> new Integer(o1.getNo()).compareTo(new Integer(o2.getNo())));

        model.addAttribute("groupDetailId", txGroupDetail.getId());
        model.addAttribute("txGroupDetail", txGroupDetail);
        model.addAttribute("txTmGeneral", txGroupDetail.getTxTmGeneral());
        model.addAttribute("listClass", listClass);

        if (txGroupDetail.getStatus().equals("P1") && txGroupDetail.getmUser1() != null) {
            model.addAttribute("userRoleSubstantif", txGroupDetail.getmUser1().getId());
        } else if (txGroupDetail.getStatus().equals("P2") && txGroupDetail.getmUser2() != null) {
            model.addAttribute("userRoleSubstantif", txGroupDetail.getmUser2().getId());
        } else {
            model.addAttribute("userRoleSubstantif", new MUser());
        }

        List<MRoleSubstantifDetail> mRoleSubstantifDetail = pemeriksaanService.selectmRoleSubstantifDetail();
        mRoleSubstantifDetail.removeIf(obj -> obj.getmEmployee().getUserId().getId().equals(txGroupDetail.getmUserCurrent().getId()));
        model.addAttribute("userSubsList", mRoleSubstantifDetail);

        boolean isP1 = false;
        boolean isUser = false;
        boolean isRole = false;
        boolean isAdmin = false;

        if (txGroupDetail.getStatus().equals("P1") || txGroupDetail.getStatus().equals("P2")) {
            MUser userP = txGroupDetail.getStatus().equals("P1") ? txGroupDetail.getmUser1() : txGroupDetail.getmUser2();

            if (userP != null) {
                MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                MRoleSubstantifDetail roleSubDetailUser = pemeriksaanService.getRoleSubstantifDetailByUsername(mUser.getUsername());

                if (roleSubDetailUser != null) {
                    MRoleSubstantif roleSubUser = roleSubDetailUser.getmRoleSubstantif();

                    MRoleSubstantifDetail roleSubDetailP = pemeriksaanService.getRoleSubstantifDetailByUsername(userP.getUsername());
                    MRoleSubstantif roleSubP = roleSubDetailP.getmRoleSubstantif();

                    isP1 = txGroupDetail.getStatus().equals("P1");
                    isUser = mUser.getId().equals(userP.getId());
                    isRole = roleSubUser.getId().equals(roleSubP.getId());
                    isAdmin = roleSubUser.getName().equalsIgnoreCase(ROLE_SUBSTANTIF_ADMIN);
                }
            }
        }
        model.addAttribute("isP1", isP1);
        model.addAttribute("isUser", isUser);
        model.addAttribute("isRole", isRole);
        model.addAttribute("isAdmin", isAdmin);

        return PAGE_CEK_KESAMAAN;
    }

    /***************************** - END CEK KESAMAAN SECTION - ****************************/

    /***************************** - RELEASE PEMERIKSAAN SUBSTANTIF - ****************************/
    @RequestMapping(path = REQUEST_RELEASE_PEMERIKSAAN_SUBSTANTIF, method = {RequestMethod.POST})
    public void doReleasePemeriksaanSubstantif(@RequestParam("id") String id, final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            try {
                pemeriksaanService.doRelease(id);
                writeJsonResponse(response, 200);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }
    /***************************** - END RELEASE PEMERIKSAAN SUBSTANTIF - ****************************/

    /***************************** - REASSIGN P1 - ****************************/
    @RequestMapping(path = REQUEST_REASSIGN_P1, method = {RequestMethod.POST})
    public void doReassignP1(@RequestParam("id") String id, final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            try {
                pemeriksaanService.doReassignP1(id);
                writeJsonResponse(response, 200);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }
    /***************************** - END REASSIGN PEMERIKSAAN SUBSTANTIF - ****************************/

    /***************************** - REASSIGN PEMERIKSAAN SUBSTANTIF - ****************************/
    @RequestMapping(path = REQUEST_REASSIGN_PEMERIKSAAN_SUBSTANTIF, method = {RequestMethod.POST})
    public void doReassignPemeriksaanSubstantif(@RequestParam("id") String id, @RequestParam("pemeriksa") String pemeriksa,
                                                final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            try {
                pemeriksaanService.doReassign(id, pemeriksa);
                writeJsonResponse(response, 200);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }
    /***************************** - END REASSIGN PEMERIKSAAN SUBSTANTIF - ****************************/

    /***************************** - VALIDASI PEMERIKSAAN SUBSTANTIF - ****************************/
    @RequestMapping(path = REQUEST_VALIDASI_PEMERIKSAAN_SUBSTANTIF, method = {RequestMethod.POST})
    public void doValidasiPemeriksaanSubstantif(@RequestParam("id") String id, @RequestParam("pemeriksa") String pemeriksa,
                                                final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            try {
                pemeriksaanService.doValidasi(id, pemeriksa);
                writeJsonResponse(response, 200);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }
    /***************************** - END VALIDASI PEMERIKSAAN SUBSTANTIF - ****************************/

    /***************************** - SELESAI PEMERIKSAAN SUBSTANTIF - ****************************/
    @RequestMapping(path = REQUEST_SELESAI_PEMERIKSAAN_SUBSTANTIF, method = {RequestMethod.POST})
    public void doSelesaiPemeriksaanSubstantif(@RequestParam("id") String id, final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            try {
                pemeriksaanService.doSelesai(id);
                writeJsonResponse(response, 200);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }
    /***************************** - END SELESAI PEMERIKSAAN SUBSTANTIF - ****************************/

    /***************************** - CARI CEK KESAMAAN SECTION - ****************************/
    @RequestMapping(value = REQUEST_MAPPING_AJAX_SEARCH_CEK_KESAMAAN, method = {RequestMethod.POST})
    public void doSearchDataCekKesamaan(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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
                        if (StringUtils.isNotBlank(value)) {
                            searchCriteria.add(new KeyValue(searchBy, value, false));
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

            MUser user = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            GenericSearchWrapper<TxSubsCheck> searchResult = pemeriksaanService.searchSubsCheck(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxSubsCheck result : searchResult.getList()) {
                    no++;
                    String classNos = "";
                    String classIds = "";

                    String classListString = result.getClassList();
                    if (classListString != null && !classListString.isEmpty()) {
                        String[] classList  = classListString.split(",");
                        Boolean first = true;
                        for ( String classId : classList) {
                            MClass mClass = classService.findOneMClass(classId);
                            if ( mClass != null ) {
                                if ( first ) {
                                    classNos = ""+mClass.getNo();
                                    classIds = mClass.getId();
                                    first = false;
                                } else {
                                    classNos += ","+mClass.getNo();
                                    classIds += ","+mClass.getId();
                                }

                            }
                        }
                    }

                    data.add(new String[]{
                            result.getId(),
                            "" + no,
                            result.getName(),
                            result.getDescription(),
//                            result.getmClass() == null ? "" : "" + result.getmClass().getNo(),
                            classNos,
//                            result.getmClass() == null ? "" : "" + result.getmClass().getId(),
                            classIds,
                            result.getOwner(),
                            result.getClassDescription(),
                            result.getAppNo(),
                            result.getRegNo(),
                    //result.getClassTranslation(),
                            result.getCreatedBy().getUsername(),
                            user.getUsername().equalsIgnoreCase(result.getCreatedBy().getUsername()) ?
                                    "<a href=\"javascript:void(0)\" onclick=\"deleteSubsCheck('"+ result.getId() +"')\" class='btn btn-danger'>Hapus</a>"
                                    : ""
                    });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }
    /***************************** - END CARI CEK KESAMAAN SECTION - ****************************/

    /***************************** - TAMBAH CEK KESAMAAN SECTION - ****************************/
    @RequestMapping(value = REQUEST_MAPPING_TAMBAH_CEK_KESAMAAN, method = {RequestMethod.POST})
    public @ResponseBody
    String doTambahCekKesamaan(final HttpServletRequest request, final HttpServletResponse response) {
        String msgError = "Success";
        String appId = request.getParameter("appId");
        String userRoleSubstantif = request.getParameter("userRoleSubstantif");
        String brandName = request.getParameter("brandName");
        String brandDesc = request.getParameter("brandDesc");
        String ownerName = request.getParameter("ownerName");
        String classId = request.getParameter("classId");
        String classDesc = request.getParameter("classDesc");
        String applicationNo = request.getParameter("applicationNo");
        String regNo = request.getParameter("regNo");

        TxTmGeneral txTmGeneral = pemeriksaanService.selectOneTxTmGeneral("id", appId);
        MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MRoleSubstantifDetail mRoleSubstantifDetail = pemeriksaanService.getRoleSubstantifDetailByUsername(mUser.getUsername());

        if (brandName.equals("") && brandDesc.equals("") && ownerName.equals("") && classId.equals("") && classDesc.equals("") && applicationNo.equals("") && regNo.equals("")) {
            msgError = "Minimal 1 inputan harus diisi !!!";
        } else if (mRoleSubstantifDetail == null) {
            msgError = "Anda tidak memiliki akses untuk Pengecekan Kesamaan !!!";
        } else {
            MRoleSubstantif mRoleSubstantif = mRoleSubstantifDetail.getmRoleSubstantif();

            if (mUser.getId().equals(userRoleSubstantif) || mRoleSubstantif.getName().equalsIgnoreCase(ROLE_SUBSTANTIF_ADMIN)) {
                TxSubsCheck txSubsCheck = new TxSubsCheck();
                txSubsCheck.setTxTmGeneral(txTmGeneral);
                txSubsCheck.setName(brandName);
                txSubsCheck.setDescription(brandDesc);
                txSubsCheck.setOwner(ownerName);
//                txSubsCheck.setmClass(classService.findOneMClass(classId));
                txSubsCheck.setClassList(classId);
                txSubsCheck.setClassDescription(classDesc);
                txSubsCheck.setCreatedBy(mUser);
                txSubsCheck.setMRoleSubstantif(mRoleSubstantif);
                txSubsCheck.setAppNo(applicationNo);
                txSubsCheck.setRegNo(regNo);
                pemeriksaanService.insertSubsCheck(txSubsCheck);
            } else {
                msgError = "Anda tidak memiliki akses untuk Pengecekan Kesamaan !!!";
            }
        }
        return msgError;
    }
    /***************************** - END TAMBAH CEK KESAMAAN SECTION - ****************************/

    /***************************** - GET PHONETIC RESULT - ****************************/
    @RequestMapping(value = REQUEST_MAPPING_PHONETIC_RESULT, method = {RequestMethod.POST})
    public void doGetPhoneticResult(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (isAjaxRequest(request)) {
            System.out.println("DO GET PHONETIC RESULT");
            setResponseAsJson(response);

            DataTablesSearchResult dataTablesSearchResult = new DataTablesSearchResult();
            try {
                dataTablesSearchResult.setDraw(Integer.parseInt(request.getParameter("draw")));
            } catch (NumberFormatException e) {
                dataTablesSearchResult.setDraw(0);
            }

            boolean isListTandai = false;
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

            String appId = request.getParameter("appId");
            String[] excludeArr = request.getParameterValues("excludeArr[]");
            String[] searchByArr = request.getParameterValues("searchByArr[]");
            String[] keywordArr = request.getParameterValues("keywordArr[]");
            List<KeyValue> searchCriteria = null;
            System.out.println(searchByArr.toString());
            for(String keyw : keywordArr){
                System.out.println(keyw);
            }
            if (searchByArr != null) {
                searchCriteria = new ArrayList<>();
                for (int i = 0; i < searchByArr.length; i++) {
                    String searchBy = searchByArr[i];
                    System.err.println(searchBy);
                    String value = null;
                    try {
                        value = keywordArr[i];
                    } catch (ArrayIndexOutOfBoundsException e) {
                    }
                    if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                        if (StringUtils.isNotBlank(value)) {
                            if (searchBy.equals("txTmBrand.name || c.txTmBrand.keywordMerek") || searchBy.equals("txTmBrand.description")) {
                                String valueStr = value.replaceAll("\\*", "_").replace("%","");
                                // SOUNDEX
                                //value = Soundex.getGode(valueStr);
                                //searchCriteria.add(new KeyValue(searchBy, valueStr, true));
                                //System.out.println("SOUNDEX"+ valueStr);
                            }
                            if (searchBy.equals("txTmClassList.mClass.no")) {
                                searchCriteria.add(new KeyValue(searchBy, value, true));
                            }
                            else if (searchBy.equals("mClassId")) {
//                                String[] classList = value.split(",");
                                List<String> classList = Arrays.asList(value.split(","));
//                                for ( int ii = 0; ii < classList.length; ii++ ) {
//                                    searchCriteria.add(new KeyValue(searchBy, classList[ii], true));
//                                }
                                searchCriteria.add(new KeyValue(searchBy, classList, true));
                            }
                            else if(searchBy.equals("similar")){
                                TxTmGeneral getTxTmGeneral = txTmGeneralCustomRepository.select("id", appId);
                                searchCriteria.add(new KeyValue("similarId", getTxTmGeneral, true));

                                ArrayUtils.removeElement(searchByArr, "similar");
                                ArrayUtils.removeElement(keywordArr, value);
                                appId = null;

                                //isListTandai = true;
                            }
                            else {
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
                        case "2":
                            orderBy = "applicationNo";
                            break;
                        case "3":
                            orderBy = "txTmBrand.name";
                            break;
                        case "4":
                            orderBy = "txTmBrand.name";
                            break;
                        case "5":
                            orderBy = "txTmClassList.mClass.no";
                            break;
                        case "6":
                            orderBy = "txTmOwner.name";
                            break;
                        case "7":
                            orderBy = "mStatus.name";
                            break;
                        case "8":
                            orderBy = "txTmPriorList.priorDate";
                            break;
                        case "9":
                            orderBy = "txRegistration.no";
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

            GenericSearchWrapper<TxTmGeneral> searchResult = pemeriksaanService.searchPhoneticResult(appId, searchCriteria, excludeArr, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxTmGeneral result : searchResult.getList()) {
                    no++;

                    String brandName = "-";
                    String ownerNameList = "-";
                    String classList = "-";
                    boolean isSimilar = false;
                    File file = null;
                    FileInputStream brandLabel = null;
                    byte[] byteImage = null;

                    String registrationNo = "-";
                    String sbPriorDateList = "-";
                    ArrayList<String> spriorDate = new ArrayList<String>();

                    try {
                        registrationNo = result.getTxRegistration().getNo();
                    }catch (NullPointerException e){
                    }

                    try {
                        brandName = result.getTxTmBrand().getName();
                    } catch (NullPointerException e) {
                    }
                    try {
                        Map<String, String> ownerMap = new HashMap<>();
                        StringBuffer sbOwnerList = new StringBuffer();
                        for (TxTmOwner txTmOwner : result.getTxTmOwner()) {
                            ownerMap.put("" + txTmOwner.getName(), "" + txTmOwner.getName());
                        }
                        for (Map.Entry<String, String> map : ownerMap.entrySet()) {
                            sbOwnerList.append(map.getKey());
                            sbOwnerList.append(", ");
                        }
                        if (sbOwnerList.length() > 0) {
                            ownerNameList = sbOwnerList.substring(0, sbOwnerList.length() - 2);
                        }
                        /*ownerName = result.getTxTmOwner().getName();*/
                    } catch (NullPointerException e) {
                    }
                    try {
                        String pathFolder = DateUtil.formatDate(result.getTxTmBrand().getCreatedDate(), "yyyy/MM/dd/");
                        //brandLabel = new FileInputStream(uploadFileBrandPath + pathFolder + result.getTxTmBrand().getId() + ".jpg");
                        file = new File (uploadFileBrandPath + pathFolder + result.getTxTmBrand().getId() + ".jpg");
                        if (file.isFile() && file.canRead()) {
                            brandLabel = new FileInputStream(uploadFileBrandPath + pathFolder + result.getTxTmBrand().getId() + ".jpg");
                        } else {
                            file = new File (uploadFileBrandPath + uploadFileIpasPath + result.getTxTmBrand().getId() + ".jpg");
                            if (file.isFile() && file.canRead()) {
                                brandLabel = new FileInputStream(uploadFileBrandPath + uploadFileIpasPath + result.getTxTmBrand().getId() + ".jpg");
                            } else {
                                brandLabel = new FileInputStream(defaultLogo);
                                //System.out.println("File gambar " + result.getTxTmBrand().getId() + ".jpg tidak ditemukan"
                                //	+ " di folder "+ uploadFileBrandPath + pathFolder
                                //	+ " maupun di di folder " + uploadFileBrandPath + uploadFileIpasPath );
                            }
                        }

                        byteImage = new byte[brandLabel.available()];
                        brandLabel.read(byteImage);
                        brandLabel.close();
                    } catch (NullPointerException e) {
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        Map<String, String> classMap = new HashMap<>();
                        StringBuffer sbClassList = new StringBuffer();

                        for (TxTmClass txTmClass : result.getTxTmClassList()) {
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
                        for (TxTmPrior txtmPrior : result.getTxTmPriorList()) {
                            spriorDate.add("" + txtmPrior.getPriorDateTemp());
                        }
                        Set<String> temp = new LinkedHashSet<String>(spriorDate);
                        String[] unique = temp.toArray(new String[temp.size()]);
                        if (unique.length > 0) {
                            sbPriorDateList = String.join(",", unique);
                        }

                    } catch (NullPointerException e) {
                    }

                    try {
                        MUser createdBy = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

                        List<KeyValue> searchSimilarityResult = new ArrayList<>();
                        searchSimilarityResult.add(new KeyValue("similarTxTmGeneral", result, true));
                        searchSimilarityResult.add(new KeyValue("createdBy", createdBy, true));
                        if(pemeriksaanService.checkAppIdOnSimilarityResult(searchSimilarityResult)){
                            isSimilar = true;
                        }
                    }catch (NullPointerException e) {
                    }

                    data.add(new String[]{
                            "" + no,
                            result.getFilingDateTemp(),
                            "<a target=\"_blank\" href=\"" + getPathURLAfterLogin(PATH_PRANTINJAU_PERMOHONAN + "?no=" + result.getApplicationNo()) + "\">" + result.getApplicationNo() + "</a>",
                            (byteImage != null ? "data:image/jpeg;base64, " + Base64Utils.encodeToString(byteImage) + "" : ""),
                            brandName,
                            classList,
                            ownerNameList,
                            result.getmStatus().getName(),
                            sbPriorDateList,
                            registrationNo,
                            isSimilar ? "Ya" : "Tidak",
                            //(result.isSimilar() ? "Ya" : "Tidak"),

                            "<button type=\"button\" class=\"btn btn-xs " + (isSimilar ? "btn-danger remove-mark" : "btn-success add-mark") + "\" appId=\"" + result.getCurrentId() + "\">" + (isSimilar ? "Hilangkan Tanda" : "Tandai") + "</button>"
                    });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }
    /***************************** - END Get Phonetic Result - ****************************/

    /***************************** - PHONETIC CHECK SECTION - ****************************/
    @RequestMapping(value = REQUEST_MAPPING_PHONETIC_CHECK, method = {RequestMethod.POST})
    public @ResponseBody
    String doPhoneticCheck(final HttpServletRequest request, final HttpServletResponse response) {
        String msgError = "Success";
        String appId = request.getParameter("appId");
        String userRoleSubstantif = request.getParameter("userRoleSubstantif");
        String brandName = request.getParameter("brandName");
        String classNo = request.getParameter("classNo");
        //String brandName = "123 + Jakarta";
        
        TxTmGeneral txTmGeneral = pemeriksaanService.selectOneTxTmGeneral("id", appId);
        MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MRoleSubstantifDetail mRoleSubstantifDetail = pemeriksaanService.getRoleSubstantifDetailByUsername(mUser.getUsername());

        if (mRoleSubstantifDetail == null) {
            msgError = "Anda tidak memiliki akses untuk Pengecekan Kesamaan !!!";
        } else {
            MRoleSubstantif mRoleSubstantif = mRoleSubstantifDetail.getmRoleSubstantif();

            if (mUser.getId().equals(userRoleSubstantif) || mRoleSubstantif.getName().equalsIgnoreCase(ROLE_SUBSTANTIF_ADMIN)) {

                try{
                    int offset = 0;
                    int limit = 0;
                    String orderBy = null;
                    String orderType = "ASC";
                    //String[] excludeArr = null;
                    List<KeyValue> searchCriteria = new ArrayList<>();

                    ArrayList<String> allowedWord = new ArrayList<>();

                    String repSpcChar = brandName.replaceAll("[^a-zA-Z0-9]+"," ");
                    String[] arrWord = repSpcChar.split(" ");

                    String[] arrClassNo = classNo.split(",");
                    ArrayList<String> classIdList = new ArrayList<>();
                    for(String tempClassNo : arrClassNo){
                        classIdList.add(classService.findFirstByNo(Integer.parseInt(tempClassNo)).getId());
                    }
                    String classList = String.join(",", classIdList);

                    /**
                     * check exception word
                     */
                    for(String word : arrWord){
                        MLookup mLookup = pemeriksaanService.selectOneMLookup("name", word);
                        if(mLookup == null){
                            allowedWord.add(word);
                        }
                    }

                    String tempWord = String.join("%", allowedWord);
                    String phoneticWord = "%" + tempWord + "%";

                    TxSubsCheck txSubsCheck = new TxSubsCheck();
                    txSubsCheck.setTxTmGeneral(txTmGeneral);
                    txSubsCheck.setName(phoneticWord);
                    txSubsCheck.setClassList(classList);
                    txSubsCheck.setCreatedBy(mUser);
                    txSubsCheck.setMRoleSubstantif(mRoleSubstantif);
                    pemeriksaanService.insertSubsCheck(txSubsCheck);

                    /*searchCriteria.add(new KeyValue("name", word, false));
                    GenericSearchWrapper<TxTmBrand> searchResult = pemeriksaanService.searchPhoneticBrandName(searchCriteria, orderBy, orderType, offset, limit);

                    int no = offset;
                    for (TxTmBrand result : searchResult.getList()) {

                    }*/

                    response.setStatus(HttpServletResponse.SC_OK);

                }catch (NullPointerException e){
                    //System.out.println(e);
                }
            } else {
                msgError = "Anda tidak memiliki akses untuk Pengecekan Kesamaan !!!";
            }
        }
        return msgError;
        //return null;
    }
    /***************************** - PHONETIC CHECK SECTION - ****************************/

    /***************************** - CARI RESULT KESAMAAN SECTION - ****************************/
    @RequestMapping(value = REQUEST_MAPPING_AJAX_SEARCH_RESULT_KESAMAAN, method = {RequestMethod.POST})
    public void doSearchDataResultKesamaan(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            DataTablesSearchResult dataTablesSearchResult = new DataTablesSearchResult();
            try {
                dataTablesSearchResult.setDraw(Integer.parseInt(request.getParameter("draw")));
            } catch (NumberFormatException e) {
                dataTablesSearchResult.setDraw(0);
            }

            boolean isListTandai = false;
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

            String appId = request.getParameter("appId");
            String[] excludeArr = request.getParameterValues("excludeArr[]");
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
                        if (StringUtils.isNotBlank(value)) {
                            if (searchBy.equals("txTmBrand.name || c.txTmBrand.keywordMerek") || searchBy.equals("txTmBrand.description")) {
                                value = value.replaceAll("\\*", "_");
                            }
                            if (searchBy.equals("txTmClassList.mClass.no")) {
                                searchCriteria.add(new KeyValue(searchBy, value, true));
                            }
                            else if (searchBy.equals("mClassId")) {
//                                String[] classList = value.split(",");
                                List<String> classList = Arrays.asList(value.split(","));
//                                for ( int ii = 0; ii < classList.length; ii++ ) {
//                                    searchCriteria.add(new KeyValue(searchBy, classList[ii], true));
//                                }
                                searchCriteria.add(new KeyValue(searchBy, classList, true));
                            }
                            else if(searchBy.equals("similar")){
                                TxTmGeneral getTxTmGeneral = txTmGeneralCustomRepository.select("id", appId);
                                searchCriteria.add(new KeyValue("similarId", getTxTmGeneral, true));

                                ArrayUtils.removeElement(searchByArr, "similar");
                                ArrayUtils.removeElement(keywordArr, value);
                                appId = null;

                                //isListTandai = true;
                            }
                            else {
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
                        case "2":
                            orderBy = "applicationNo";
                            break;
                        case "3":
                            orderBy = "txTmBrand.name";
                            break;
                        case "4":
                            orderBy = "txTmBrand.name";
                            break;
                        case "5":
                            orderBy = "txTmClassList.mClass.no";
                            break;
                        case "6":
                            orderBy = "txTmOwner.name";
                            break;
                        case "7":
                            orderBy = "mStatus.name";
                            break;
                        case "8":
                            orderBy = "txTmPriorList.priorDate";
                            break;
                        case "9":
                            orderBy = "txRegistration.no";
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

            GenericSearchWrapper<TxTmGeneral> searchResult = pemeriksaanService.searchSimilarityResult(appId, searchCriteria, excludeArr, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxTmGeneral result : searchResult.getList()) {
                    no++;

                    String brandName = "-";
                    String ownerNameList = "-";
                    String classList = "-";
                    boolean isSimilar = false;
                    File file = null;
                    FileInputStream brandLabel = null;
                    byte[] byteImage = null;

                    String registrationNo = "-";
                    String sbPriorDateList = "-";
                    ArrayList<String> spriorDate = new ArrayList<String>();

                    try {
                        registrationNo = result.getTxRegistration().getNo();
                    }catch (NullPointerException e){
                    }

                    try {
                        brandName = result.getTxTmBrand().getName();
                    } catch (NullPointerException e) {
                    }
                    try {
                        Map<String, String> ownerMap = new HashMap<>();
                        StringBuffer sbOwnerList = new StringBuffer();
                        for (TxTmOwner txTmOwner : result.getTxTmOwner()) {
                            ownerMap.put("" + txTmOwner.getName(), "" + txTmOwner.getName());
                        }
                        for (Map.Entry<String, String> map : ownerMap.entrySet()) {
                            sbOwnerList.append(map.getKey());
                            sbOwnerList.append(", ");
                        }
                        if (sbOwnerList.length() > 0) {
                            ownerNameList = sbOwnerList.substring(0, sbOwnerList.length() - 2);
                        }
                        /*ownerName = result.getTxTmOwner().getName();*/
                    } catch (NullPointerException e) {
                    }
                    try {
                        String pathFolder = DateUtil.formatDate(result.getTxTmBrand().getCreatedDate(), "yyyy/MM/dd/");
                        //brandLabel = new FileInputStream(uploadFileBrandPath + pathFolder + result.getTxTmBrand().getId() + ".jpg");
                        file = new File (uploadFileBrandPath + pathFolder + result.getTxTmBrand().getId() + ".jpg");
                		if (file.isFile() && file.canRead()) {
                			brandLabel = new FileInputStream(uploadFileBrandPath + pathFolder + result.getTxTmBrand().getId() + ".jpg");
                		} else {
                			file = new File (uploadFileBrandPath + uploadFileIpasPath + result.getTxTmBrand().getId() + ".jpg");
                			if (file.isFile() && file.canRead()) {
                				brandLabel = new FileInputStream(uploadFileBrandPath + uploadFileIpasPath + result.getTxTmBrand().getId() + ".jpg");
                			} else {
                                brandLabel = new FileInputStream(defaultLogo);
                				//System.out.println("File gambar " + result.getTxTmBrand().getId() + ".jpg tidak ditemukan"
                					//	+ " di folder "+ uploadFileBrandPath + pathFolder
                					//	+ " maupun di di folder " + uploadFileBrandPath + uploadFileIpasPath );
                			}
                		}
                        
                        byteImage = new byte[brandLabel.available()];
                        brandLabel.read(byteImage);
                        brandLabel.close();
                    } catch (NullPointerException e) {
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        Map<String, String> classMap = new HashMap<>();
                        StringBuffer sbClassList = new StringBuffer();

                        for (TxTmClass txTmClass : result.getTxTmClassList()) {
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
                        for (TxTmPrior txtmPrior : result.getTxTmPriorList()) {
                            spriorDate.add("" + txtmPrior.getPriorDateTemp());
                        }
                        Set<String> temp = new LinkedHashSet<String>(spriorDate);
                        String[] unique = temp.toArray(new String[temp.size()]);
                        if (unique.length > 0) {
                            sbPriorDateList = String.join(",", unique);
                        }

                    } catch (NullPointerException e) {
                    }

                    try {
                        MUser createdBy = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

                        List<KeyValue> searchSimilarityResult = new ArrayList<>();
                        searchSimilarityResult.add(new KeyValue("similarTxTmGeneral", result, true));
                        searchSimilarityResult.add(new KeyValue("createdBy", createdBy, true));
                        if(pemeriksaanService.checkAppIdOnSimilarityResult(searchSimilarityResult)){
                            isSimilar = true;
                        }
                    }catch (NullPointerException e) {
                    }

                    data.add(new String[]{
                            "" + no,
                            result.getFilingDateTemp(),
                            "<a target=\"_blank\" href=\"" + getPathURLAfterLogin(PATH_PRANTINJAU_PERMOHONAN + "?no=" + result.getApplicationNo()) + "\">" + result.getApplicationNo() + "</a>",
                            (byteImage != null ? "data:image/jpeg;base64, " + Base64Utils.encodeToString(byteImage) + "" : ""),
                            brandName,
                            classList,
                            ownerNameList,
                            result.getmStatus().getName(),
                            sbPriorDateList,
                            registrationNo,
                            isSimilar ? "Ya" : "Tidak",
                            //(result.isSimilar() ? "Ya" : "Tidak"),

                            "<button type=\"button\" class=\"btn btn-xs " + (isSimilar ? "btn-danger remove-mark" : "btn-success add-mark") + "\" appId=\"" + result.getCurrentId() + "\">" + (isSimilar ? "Hilangkan Tanda" : "Tandai") + "</button>"
                    });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    /***************************** - END CARI RESULT KESAMAAN SECTION - ****************************/

    @PostMapping(REQUEST_MAPPING_AJAX_ADD_SIMILARITY_MARK)
    public void doAjaxAddSimilarityMark(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        doAjaxChangeSimilarityMark(request, response, true);
    }

    @PostMapping(REQUEST_MAPPING_AJAX_REMOVE_SIMILARITY_MARK)
    public void doAjaxRemoveSimilarityMark(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        doAjaxChangeSimilarityMark(request, response, false);
    }

    public List<TxTmGeneral> doSearchTxTmGeneralByTxSubsCheck(TxSubsCheck txSubsCheck, boolean is_listTandai) throws IOException {
        //pemeriksaanService.checkAppIdOnSimilarityResult("similarTxTmGeneral", result)
        String appId = txSubsCheck.getTxTmGeneral().getId();
        List<KeyValue> searchCriteria = new ArrayList<>();
        String name = txSubsCheck.getName();
        String description = txSubsCheck.getDescription();
        String classString = txSubsCheck.getClassList();
        String owner = txSubsCheck.getOwner();
        String classDescription = txSubsCheck.getClassDescription();
        String appNo = txSubsCheck.getAppNo();
        String regNo = txSubsCheck.getRegNo();

        if (  name != null && !name.isEmpty() && !is_listTandai) {
            name = name.replaceAll("\\*", "_");
            searchCriteria.add(new KeyValue("txTmBrand.name || c.txTmBrand.keywordMerek", name, false));
        }
        if ( description != null && !description.isEmpty() && !is_listTandai) {
            description = description.replaceAll("\\*", "_");
            searchCriteria.add(new KeyValue("txTmBrand.description", description, false));
        }
        if ( classString != null && !classString.isEmpty() && !is_listTandai) {
            /*String[] classList = classString.split(",");

            for ( int ii = 0; ii < classList.length; ii++ ) {
                searchCriteria.add(new KeyValue("mClassId", classList[ii], true));
            }*/
            List<String> classList = Arrays.asList(classString.split(","));
            searchCriteria.add(new KeyValue("mClassId", classList, true));

        }
        if ( owner != null && !owner.isEmpty() && !is_listTandai) {
            searchCriteria.add(new KeyValue("txTmOwnerName", owner, false));
        }
        if ( classDescription != null && !classDescription.isEmpty() && !is_listTandai) {
            searchCriteria.add(new KeyValue("mClassDetailDesc", classDescription, false));
        }

        if ( appNo != null && !appNo.isEmpty() && !is_listTandai) {
            searchCriteria.add(new KeyValue("applicationNo", appNo, false));
        }

        if ( regNo != null && !regNo.isEmpty() && !is_listTandai) {
            searchCriteria.add(new KeyValue("regNo", regNo, false));
        }

        if (is_listTandai){
            TxTmGeneral getTxTmGeneral = txTmGeneralCustomRepository.select("id", appId);
            searchCriteria.add(new KeyValue("similarId", getTxTmGeneral, true));
        }

        if ( searchCriteria.size() == 0 ) {
            searchCriteria = null;
        }

        String[] excludeArr = { txSubsCheck.getTxTmGeneral().getId() };

        List<String[]> data = new ArrayList<>();

        GenericSearchWrapper<TxTmGeneral> searchResult = pemeriksaanService.searchSimilarityResult(appId, searchCriteria, excludeArr, "createdDate", "DESC", null, null
        );
        return searchResult.getList();
    }

    @GetMapping(path = PATH_DOWNLOAD_DOCX)
    public void showPageList(final Model model, final HttpServletRequest request, final HttpServletResponse response) {
        String[] noCekKesamaanLogo = new String(Base64.getDecoder().decode(request.getParameter( "no" ))).split(";");

        try {
            OutputStream stream = response.getOutputStream();
            String txSubsCheckId = noCekKesamaanLogo[0];
            boolean is_listTandai = noCekKesamaanLogo.length >= 2  ? Boolean.parseBoolean(noCekKesamaanLogo[2]) : false;
            TxGroupDetail txGroupDetail = pemeriksaanService.selectOneTxGroupDetail("id", noCekKesamaanLogo[1]);
            WordprocessingMLPackage wordPackage = WordprocessingMLPackage.createPackage(PageSizePaper.A4, true);
            MainDocumentPart mainDocumentPart = wordPackage.getMainDocumentPart();
            mainDocumentPart.addStyledParagraphOfText("Title", "PENELUSURAN :  "+ txGroupDetail.getTxTmGeneral().getApplicationNo());
            mainDocumentPart.addStyledParagraphOfText("Subtitle", "MEREK : "+ txGroupDetail.getTxTmGeneral().getTxTmBrand().getName()+", KELAS : "+txGroupDetail.getTxTmGeneral().getClassList());

            List<KeyValue> searchCriteria = new ArrayList<>();

            searchCriteria.add(new KeyValue("txTmGeneral.id", txGroupDetail.getTxTmGeneral().getId(), false));
            GenericSearchWrapper<TxSubsCheck> searchResult = pemeriksaanService.searchSubsCheck(searchCriteria, "createdDate", "DESC", null, null);

            List<TxSubsCheck> txSubsCheckList = searchResult.getList();

            int writableWidthTwips = wordPackage.getDocumentModel()
                    .getSections().get(0).getPageDimensions().getWritableWidthTwips();
            int columnNumber = 7;
            Tbl tbl = TblFactory.createTable(txSubsCheckList.size()+1, columnNumber, writableWidthTwips/columnNumber);
            List<Object> rows = tbl.getContent();
            int count = 0;
            long fontSize = 18;

            for (Object row : rows) {
                Tr tr = (Tr) row;
                List<Object> cells = tr.getContent();

                if ( count == 0 ) {
                    ((Tc)cells.get(0)).getContent().add(addMessage2("No",true, fontSize));
                    ((Tc)cells.get(1)).getContent().add(addMessage2("Merek",true, fontSize));
                    ((Tc)cells.get(2)).getContent().add(addMessage2("Deskripsi Merek",true, fontSize));
                    ((Tc)cells.get(3)).getContent().add(addMessage2("Kelas", true,fontSize));
                    ((Tc)cells.get(4)).getContent().add(addMessage2("Nama Pemohon", true,fontSize));
                    ((Tc)cells.get(5)).getContent().add(addMessage2("Uraian Kelas", true,fontSize));
                    ((Tc)cells.get(6)).getContent().add(addMessage2("Pemeriksa",true, fontSize));
                } else {
                    String classNos = "";
                    TxSubsCheck result = txSubsCheckList.get(count-1);

                    boolean isBold = result.getId().equalsIgnoreCase(txSubsCheckId);

                    String classListString = result.getClassList();
                    if (classListString != null && !classListString.isEmpty()) {
                        String[] classList  = classListString.split(",");
                        Boolean first = true;
                        for ( String classId : classList) {
                            MClass mClass = classService.findOneMClass(classId);
                            if ( mClass != null ) {
                                if ( first ) {
                                    classNos = "" + mClass.getNo();
                                    first = false;
                                } else {
                                    classNos += ","+mClass.getNo();
                                }

                            }
                        }
                    }

                    ((Tc)cells.get(0)).getContent().add(addMessage(""+count, isBold, fontSize));
                    ((Tc)cells.get(1)).getContent().add(addMessage(result.getName() == null ? "" : result.getName(), isBold, fontSize));
                    ((Tc)cells.get(2)).getContent().add(addMessage(result.getDescription(), isBold, fontSize));
                    ((Tc)cells.get(3)).getContent().add(addMessage(classNos, isBold, fontSize));
                    ((Tc)cells.get(4)).getContent().add(addMessage(result.getOwner(), isBold, fontSize));
                    ((Tc)cells.get(5)).getContent().add(addMessage(result.getClassDescription(), isBold, fontSize));
                    ((Tc)cells.get(6)).getContent().add(addMessage(result.getCreatedBy().getUsername(), isBold, fontSize));
                }

                count++;
            }

            mainDocumentPart.getContent().add(tbl);

            List<TxTmGeneral> txTmGeneralList = doSearchTxTmGeneralByTxSubsCheck( pemeriksaanService.selectOneTxSubsCheck("id", txSubsCheckId), is_listTandai);
            mainDocumentPart.getContent().add(addMessage("", fontSize));
            mainDocumentPart.getContent().add(addMessage("Terdapat kesamaan dengan merek lain sejumlah (" + txTmGeneralList.size() + ") Merek", true, fontSize));

            columnNumber = 6;
            count = 0;

            Tbl tbl2 = TblFactory.createTable(txTmGeneralList.size()+1, columnNumber, writableWidthTwips/columnNumber);
            rows = tbl2.getContent();

            for (Object row : rows) {
                Tr tr = (Tr) row;
                List<Object> cells = tr.getContent();

                if ( count == 0 ) {
//                    ((Tc)cells.get(0)).getContent().add(addMessage2("No.", true,fontSize));
                    ((Tc)cells.get(0)).getContent().add(addMessage2("Nomor Permohonan/"+"\n"+"Nomor Pendaftaran", true,fontSize));
                    ((Tc)cells.get(1)).getContent().add(addMessage2("Merek",true, fontSize));
                    ((Tc)cells.get(2)).getContent().add(addMessage2("Pemilik",true, fontSize));
                    ((Tc)cells.get(3)).getContent().add(addMessage2("Kelas & Uraian Barang/Jasa",true, fontSize));
                    ((Tc)cells.get(4)).getContent().add(addMessage2("Status/"+"\n"+"Tanggal Penerimaan/"+"\n"+"Tanggal Pendaftaran/"+"\n"+"Tanggal Kadaluarsa", true,fontSize));
                    ((Tc)cells.get(5)).getContent().add(addMessage2("Etiket",true, fontSize));
                } else {
                    TxTmGeneral result = txTmGeneralList.get(count-1);
                    String pathFolder = DateUtil.formatDate(result.getTxTmBrand().getCreatedDate(), "yyyy/MM/dd/");
                    String fillingDate = DateUtil.formatDate(result.getFilingDate(), "dd MMMM yyyy");
                    String ownerNameList = "-";

                    String registrationNo = "";
                    String regStartDate = "";
                    String regEndDate = "";
                    String brandName = "";
                    String sbPriorDateList = "-";
                    ArrayList<String> spriorDate = new ArrayList<>();

                    int key;
                    String value = "";
                    String desc = "";
//                    String descEn = "";
                    Map<Integer, String> mapClass = new HashMap<>();

                    try {
                        registrationNo = result.getTxRegistration().getNo();
                        regStartDate = DateUtil.formatDate(result.getTxRegistration().getStartDate(), "dd MMMM yyyy");
                        regEndDate = DateUtil.formatDate(result.getTxRegistration().getEndDate(), "dd MMMM yyyy");
                    }catch (NullPointerException e){
                    }

                    try {
                        brandName=result.getTxTmBrand().getName();
                    }catch (NullPointerException e){
                    }

                    for (TxTmClass txTmClass : result.getTxTmClassList()) {
                        MClassDetail mClassDetail = txTmClass.getmClassDetail();
                        key = txTmClass.getmClass().getNo();
                        desc = txTmClass.getmClassDetail().getDesc()==null?"":txTmClass.getmClassDetail().getDesc();
//                        descEn = txTmClass.getmClassDetail().getDesc();
                        if (mapClass.containsKey(key)) {
                            value = mapClass.get(key);
                            value = value + "; " + desc.trim().replaceAll("(\\r\\n|\\n)", "");
                        } else {
                            value = desc.trim().replaceAll("(\\r\\n|\\n)", "");
                            ;
                        }
                        mapClass.put(key, value);
                    }

                    Map<Integer, String> mapClassOrder = mapClass.entrySet().stream().sorted(Map.Entry.comparingByKey())
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                    (oldValue, newValue) -> oldValue, LinkedHashMap::new));
                    StringBuffer sbClassNoList = new StringBuffer();
                    StringBuffer sbClassDescList = new StringBuffer();
//                    StringBuffer sbClassDescEnList = new StringBuffer();
                    for (Map.Entry<Integer, String> map : mapClassOrder.entrySet()) {
                        sbClassNoList.append(map.getKey());
                        sbClassNoList.append(", ");

                        sbClassDescList.append("\n\nKelas " + map.getKey() + " :\n\n");
                        sbClassDescList.append("=== ");
                        sbClassDescList.append(map.getValue());
                        sbClassDescList.append(" ===");
                        sbClassDescList.append("\n");
                    }
                    String classNoList = "";
                    String classDescList = "";
                    if (sbClassNoList.length() > 0) {
                        classNoList = sbClassNoList.substring(0, sbClassNoList.length() - 2);
                    }
                    if (sbClassDescList.length() > 0) {
                        classDescList = sbClassDescList.substring(0, sbClassDescList.length() - 1);
                    }

                    Map<String, String> ownerMap = new HashMap<>();
                    StringBuffer sbOwnerList = new StringBuffer();
                    for (TxTmOwner txTmOwner : result.getTxTmOwner()) {
                        ownerMap.put("" + txTmOwner.getName(), "" + txTmOwner.getName());
                    }
                    for (Map.Entry<String, String> map : ownerMap.entrySet()) {
                        sbOwnerList.append(map.getKey());
                        sbOwnerList.append(", ");
                    }
                    if (sbOwnerList.length() > 0) {
                        ownerNameList = sbOwnerList.substring(0, sbOwnerList.length() - 2);
                    }

                    for (TxTmPrior txtmPrior : result.getTxTmPriorList()) {
                        spriorDate.add("" + txtmPrior.getPriorDateTemp());
                    }
                    Set<String> temp = new LinkedHashSet<>(spriorDate);
                    String[] unique = temp.toArray(new String[temp.size()]);
                    if (unique.length > 0) {
                        sbPriorDateList = String.join(",", unique);
                    }

                    String imagePath = uploadFileBrandPath + pathFolder + result.getTxTmBrand().getId() + ".jpg";
                    File image = new File(imagePath );
                    Inline inline = null;
                    if ( image.exists() && !image.isDirectory() ) {
                        byte[] fileContent = Files.readAllBytes(image.toPath());
                        BinaryPartAbstractImage imagePart = BinaryPartAbstractImage
                                .createImagePart(wordPackage, fileContent);
                        inline = imagePart.createImageInline(
                                result.getTxTmBrand().getId() + ".jpg", imagePath, 1, 2, false, 1000);
                    }

                    boolean isSimilar = false;
                    try{
                        MUser createdBy = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

                        List<KeyValue> searchSimilarityResult = new ArrayList<>();
                        searchSimilarityResult.add(new KeyValue("similarTxTmGeneral", result, true));
                        searchSimilarityResult.add(new KeyValue("createdBy", createdBy, true));
                        if(pemeriksaanService.checkAppIdOnSimilarityResult(searchSimilarityResult)){
                            isSimilar = true;
                        }
                    }catch (NullPointerException e) {
                    }

//                    ((Tc)cells.get(0)).getContent().add(addMessage(""+count, fontSize));
                    ((Tc)cells.get(0)).getContent().add(addMessage(result.getApplicationNo()+"\n\n"+registrationNo+"\n\n"+"(Tandai: "+(isSimilar ? "Ya" : "Tidak")+" )", fontSize));
                    ((Tc)cells.get(1)).getContent().add(addMessage(brandName, fontSize));
                    ((Tc)cells.get(2)).getContent().add(addMessage(ownerNameList, fontSize));
                    ((Tc)cells.get(3)).getContent().add(addMessage(classDescList, fontSize));
//                    ((Tc)cells.get(6)).getContent().add(addMessage(classDescEnList, fontSize));
                    ((Tc)cells.get(4)).getContent().add(addMessage(result.getmStatus().getName()+"/\n\n"+fillingDate+"/\n\n"+regStartDate+"/\n\n"+regEndDate, fontSize));
//                    ((Tc)cells.get(2)).getContent().add(addMessageLink(result.getApplicationNo(), getPathURLAfterLogin(PATH_PRANTINJAU_PERMOHONAN + "?no=" + result.getApplicationNo()), fontSize));
                    ((Tc)cells.get(5)).getContent().add(inline == null ? addMessage("Gambar tidak ditemukan di : " + imagePath, fontSize) : addImageToParagraph(inline));
//                    ((Tc)cells.get(7)).getContent().add(addMessage(result.getmStatus().getName(), fontSize));
//                    ((Tc)cells.get(8)).getContent().add(addMessage(sbPriorDateList, fontSize));
//                    ((Tc)cells.get(9)).getContent().add(addMessage(isSimilar ? "Ya" : "Tidak", fontSize));
                }
                count++;
            }

            mainDocumentPart.getContent().add(tbl2);


            FooterPart footerPart = createFooter(wordPackage);
//            footerPart.getContent().add(addMessage("Footer Message", 18));

            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
//            response.setContentLength((int) wordPackage.get);
            response.setHeader("Content-Disposition", "inline; filename=" + txGroupDetail.getTxTmGeneral().getApplicationNo() + ".docx;");

            wordPackage.save(stream);
            stream.flush();
            stream.close();
        } catch ( Exception e) {
            e.printStackTrace();
        }
    }

    public static FooterPart createFooter(WordprocessingMLPackage word) throws Exception {
        FooterPart footerPart = new FooterPart();
        Relationship rel = word.getMainDocumentPart().addTargetPart(footerPart);
        footerPart.getContent().add(addPageNumberField());
        createFooterReference(word, rel);
        return footerPart;
    }

    public static void createFooterReference(
            WordprocessingMLPackage word,
            Relationship relationship )
            throws InvalidFormatException {
        ObjectFactory factory = new ObjectFactory();
        List<SectionWrapper> sections = word.getDocumentModel().getSections();

        SectPr sectPr = sections.get(sections.size() - 1).getSectPr();
        // There is always a section wrapper, but it might not contain a sectPr
        if (sectPr==null ) {
            sectPr = factory.createSectPr();
            word.getMainDocumentPart().addObject(sectPr);
            sections.get(sections.size() - 1).setSectPr(sectPr);
        }
        FooterReference footerReference = factory.createFooterReference();
        footerReference.setId(relationship.getId());
        footerReference.setType(HdrFtrRef.DEFAULT);
        sectPr.getEGHdrFtrReferences().add(footerReference);
    }

    private static P addImageToParagraph(Inline inline) {
        ObjectFactory factory = new ObjectFactory();
        P p = factory.createP();
        R r = factory.createR();
        p.getContent().add(r);
        Drawing drawing = factory.createDrawing();
        r.getContent().add(drawing);
        drawing.getAnchorOrInline().add(inline);
        return p;
    }

    private static P addMessage2(String message, boolean istTitleBold, long fontSize) {
        ObjectFactory factory = Context.getWmlObjectFactory();
        P p = factory.createP();
        R r = factory.createR();
        Text t = factory.createText();
        t.setValue(message);
        r.getContent().add(t);
        p.getContent().add(r);
        RPr rpr = factory.createRPr();

        PPr paragraphProperties = factory.createPPr();
        Jc justification = factory.createJc();
        justification.setVal(JcEnumeration.CENTER);
        paragraphProperties.setJc(justification);

        p.setPPr(paragraphProperties);
        if ( istTitleBold ) {
            BooleanDefaultTrue b = new BooleanDefaultTrue();
            rpr.setB(b);
//            rpr.setI(b);
//            rpr.setCaps(b);
            Color color = factory.createColor();
            color.setVal("#000000");
            rpr.setColor(color);
            r.setRPr(rpr);
        }
        HpsMeasure size = new HpsMeasure();
        size.setVal(BigInteger.valueOf(fontSize));
        rpr.setSz(size);
        r.setRPr(rpr);
        RFonts font = factory.createRFonts();
        font.setAscii("Arial Narrow");
        font.setHAnsi("Arial Narrow");
        rpr.setRFonts(font);
        return p;
    }
    private static P addMessage(String message, boolean isBold, long fontSize) {
        ObjectFactory factory = Context.getWmlObjectFactory();
        P p = factory.createP();
        R r = factory.createR();
        Text t = factory.createText();
        t.setValue(message);
        r.getContent().add(t);
        p.getContent().add(r);
        RPr rpr = factory.createRPr();
        if ( isBold ) {
            BooleanDefaultTrue b = new BooleanDefaultTrue();
            rpr.setB(b);
            rpr.setI(b);
            rpr.setCaps(b);
            Color color = factory.createColor();
            color.setVal("FF0000");
            rpr.setColor(color);
            r.setRPr(rpr);
        }
        HpsMeasure size = new HpsMeasure();
        size.setVal(BigInteger.valueOf(fontSize));
        rpr.setSz(size);
        r.setRPr(rpr);
        RFonts font = factory.createRFonts();
        font.setAscii("Arial Narrow");
        font.setHAnsi("Arial Narrow");
        rpr.setRFonts(font);
        return p;
    }

    private static P addMessage(String message, long fontSize) {
        ObjectFactory factory = Context.getWmlObjectFactory();
        P p = factory.createP();
        R r = factory.createR();

        PPr paragraphProperties = factory.createPPr();
        Jc justification = factory.createJc();
        justification.setVal(JcEnumeration.BOTH);
        paragraphProperties.setJc(justification);

        p.setPPr(paragraphProperties);
        Text t = factory.createText();
        t.setValue(message);
        r.getContent().add(t);
        p.getContent().add(r);

        RPr rpr = factory.createRPr();
        HpsMeasure size = new HpsMeasure();
        size.setVal(BigInteger.valueOf(fontSize));
        rpr.setSz(size);
        r.setRPr(rpr);
        RFonts font = factory.createRFonts();
        font.setAscii("Arial Narrow");
        font.setHAnsi("Arial Narrow");
        rpr.setRFonts(font);
        return p;
    }

    private static P addMessageLink(String message, String url, long fontSize) {
        ObjectFactory factory = Context.getWmlObjectFactory();
        P p = factory.createP();
        P.Hyperlink h = MainDocumentPart.hyperlinkToBookmark(url, message);
        p.getContent().add(h);
        return p;
    }

    private static P addPageNumberField() {
        ObjectFactory factory = Context.getWmlObjectFactory();
        P p = factory.createP();

        PPr paragraphProperties = factory.createPPr();
        Jc justification = factory.createJc();
        justification.setVal(JcEnumeration.CENTER);
        paragraphProperties.setJc(justification);

        p.setPPr(paragraphProperties);

        R run1 = factory.createR();
        FldChar fldchar = factory.createFldChar();
        fldchar.setFldCharType(STFldCharType.BEGIN);
        run1.getContent().add(fldchar);
        p.getContent().add(run1);

        R run2 = factory.createR();
        Text txt = new Text();
        txt.setSpace("preserve");
        txt.setValue(" PAGE   \\* MERGEFORMAT ");
        run2.getContent().add(factory.createRInstrText(txt));
        p.getContent().add(run2);

        FldChar fldcharend = factory.createFldChar();
        fldcharend.setFldCharType(STFldCharType.END);
        R run3 = factory.createR();
        run3.getContent().add(fldcharend);
        p.getContent().add(run3);

        return p;
    }

    private void doAjaxChangeSimilarityMark(final HttpServletRequest request, final HttpServletResponse response, boolean addMark) throws IOException {
        if (isAjaxRequest(request)) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);

            String originId = request.getParameter("originId");
            String similarId = request.getParameter("similarId");
            String selectedFilterId = request.getParameter("selectedFilterId");
            if (ValidationUtil.isBlank(originId)) {
                result.put("message", "Parameter permohonan tidak ditemukan");
            }

            if (ValidationUtil.isBlank(similarId)) {
                result.put("message", "Parameter kesamaan permohonan tidak ditemukan");
            }

            if (result.size() == 1) {
                try {
                    TxTmGeneral originTxTmGeneral = new TxTmGeneral();
                    originTxTmGeneral.setId(originId);

                    TxTmGeneral similarTxTmGeneral = new TxTmGeneral();
                    similarTxTmGeneral.setId(similarId);

                    TxSubsCheck checkId = new TxSubsCheck();
                    checkId.setId(selectedFilterId);

                    TxSimilarityResult form = new TxSimilarityResult();
                    form.setOriginTxTmGeneral(originTxTmGeneral);
                    form.setSimilarTxTmGeneral(similarTxTmGeneral);
                    form.setTxSubsCheck(checkId);

                    if (addMark) {
                        form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                        form.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                        pemeriksaanService.insertSimilarityResult(form);
                    } else {
                        pemeriksaanService.deleteSimilarityResult(form);
                    }

                    result.put("success", true);
                } catch (DataIntegrityViolationException e) {
                    logger.error(e.getMessage(), e);
                }
            }

            writeJsonResponse(response, result);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @PostMapping(REQUEST_MAPPING_AJAX_DELETE_SUBSCHECK)
    public void doDeleteSubsCheck(final HttpServletRequest request, final HttpServletResponse response) {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);
            String idSubsCheck = request.getParameter("idSubsCheck");
            TxSubsCheck txSubsCheck = new TxSubsCheck();
            txSubsCheck.setId(idSubsCheck);
            List<KeyValue> searchCriteria = new ArrayList<>();
            MUser createdBy = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            searchCriteria.add(new KeyValue("txSubsCheck", txSubsCheck, true));
            searchCriteria.add(new KeyValue("createdBy", createdBy, true));

            boolean checkAppIdOnSimilarityResult = pemeriksaanService.checkAppIdOnSimilarityResult(searchCriteria);
            if (checkAppIdOnSimilarityResult) {
                writeJsonResponse(response, false);
                return;
            }
            pemeriksaanService.deleteSubsCheck(idSubsCheck);
            writeJsonResponse(response, true);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }
}
