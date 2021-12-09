package com.docotel.ki.controller;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.enumeration.GroupDetailStatusEnum;
import com.docotel.ki.model.master.MClass;
import com.docotel.ki.model.master.MRoleSubstantif;
import com.docotel.ki.model.master.MRoleSubstantifDetail;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.NativeQueryRepository;
import com.docotel.ki.service.master.ClassService;
import com.docotel.ki.service.master.StatusService;
import com.docotel.ki.service.transaction.PemeriksaanSubstantifService;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.util.ValidationUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Principal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class KertasKerjaSubstantifController extends BaseController {
    public static final String PATH_AJAX_ADD_SIMILARITY_MARK = "/tandai-kertas-kerja-kesamaan";
    public static final String PATH_AJAX_REMOVE_SIMILARITY_MARK = "/hapus-kertas-kerja-tanda-kesamaan";
    public static final String PATH_AJAX_SEARCH_LIST_P1 = "/cari-kertas-kerja-substantif-p1";
    public static final String PATH_AJAX_SEARCH_LIST_P2 = "/cari-kertas-kerja-substantif-p2";
    public static final String PATH_AJAX_SEARCH_LIST_MENUNGGU_TGP = "/cari-kertas-kerja-menunggu-tanggapan";
    public static final String PATH_AJAX_SEARCH_LIST_PEMERIKSAAN_TGP = "/cari-kertas-kerja-pemeriksaan-tanggapan";
    public static final String PATH_AJAX_SEARCH_CEK_KESAMAAN = "/cari-kertas-kerja-cek-kesamaan";
    public static final String PATH_AJAX_SEARCH_RESULT_KESAMAAN = "/cari-kertas-kerja-result-kesamaan";
    public static final String PATH_CEK_KESAMAAN = "/cek-kertas-kerja-kesamaan";
    public static final String PATH_TAMBAH_CEK_KESAMAAN = "/tambah-kertas-kerja-cek-kesamaan";
    public static final String PATH_RELEASE_KERTAS_KERJA_SUBSTANTIF = "/release-kertas-kerja-substantif";
    public static final String PATH_REASSIGN_KERTAS_KERJA_SUBSTANTIF = "/reassign-kertas-kerja-substantif";
    public static final String PATH_VALIDASI_KERTAS_KERJA_SUBSTANTIF = "/validasi-kertas-kerja-substantif";
    public static final String PATH_SELESAI_KERTAS_KERJA_SUBSTANTIF = "/selesai-kertas-kerja-substantif";
    public static final String PATH_PRANTINJAU_PERMOHONAN = "/kertas-kerja-pratinjau-permohonan";
    public static final String PATH_KERTAS_KERJA_SUBSTANTIF1 = "/kertas-kerja-substantif-p1";
    public static final String REQUEST_MAPPING_KERTAS_KERJA_SUBSTANTIF1 = PATH_KERTAS_KERJA_SUBSTANTIF1 + "*";
    public static final String PATH_KERTAS_KERJA_SUBSTANTIF2 = "/kertas-kerja-substantif-p2";
    public static final String REQUEST_MAPPING_KERTAS_KERJA_SUBSTANTIF2 = PATH_KERTAS_KERJA_SUBSTANTIF2 + "*";
    public static final String PATH_KERTAS_KERJA_MENUNGGU_TGP = "/kertas-kerja-menunggu-tanggapan";
    public static final String REQUEST_MAPPING_KERTAS_KERJA_MENUNGGU_TGP = PATH_KERTAS_KERJA_MENUNGGU_TGP + "*";
    public static final String PATH_KERTAS_KERJA_PEMERIKSAAN_TGP = "/kertas-kerja-pemeriksaan-tanggapan";
    public static final String REQUEST_MAPPING_KERTAS_KERJA_PEMERIKSAAN_TGP = PATH_KERTAS_KERJA_PEMERIKSAAN_TGP + "*";

    public static final String REQUEST_MAPPING_CEK_KESAMAAN = PATH_CEK_KESAMAAN + "*";
    public static final String REQUEST_MAPPING_TAMBAH_CEK_KESAMAAN = PATH_TAMBAH_CEK_KESAMAAN + "*";
    public static final String REQUEST_RELEASE_KERTAS_KERJA_SUBSTANTIF = PATH_RELEASE_KERTAS_KERJA_SUBSTANTIF + "*";
    public static final String REQUEST_REASSIGN_KERTAS_KERJA_SUBSTANTIF = PATH_REASSIGN_KERTAS_KERJA_SUBSTANTIF + "*";
    public static final String REQUEST_VALIDASI_KERTAS_KERJA_SUBSTANTIF = PATH_VALIDASI_KERTAS_KERJA_SUBSTANTIF + "*";
    public static final String REQUEST_SELESAI_KERTAS_KERJA_SUBSTANTIF = PATH_SELESAI_KERTAS_KERJA_SUBSTANTIF + "*";
    public static final String REDIRECT_CEK_SUBSTANTIF1 = "redirect:" + PATH_AFTER_LOGIN + PATH_KERTAS_KERJA_SUBSTANTIF1;
    public static final String REDIRECT_CEK_SUBSTANTIF2 = "redirect:" + PATH_AFTER_LOGIN + PATH_KERTAS_KERJA_SUBSTANTIF2;
    public static final String REDIRECT_CEK_MENUNGGU_TGP = "redirect:" + PATH_AFTER_LOGIN + PATH_KERTAS_KERJA_MENUNGGU_TGP;
    public static final String REDIRECT_CEK_PEMERIKSAAN_TGP = "redirect:" + PATH_AFTER_LOGIN + PATH_KERTAS_KERJA_PEMERIKSAAN_TGP;

    private static final String ROLE_SUBSTANTIF_ADMIN = "admin";
    private static final String DIRECTORY_PAGE = "kertas-kerja-substantif/";
    private static final String PAGE_KERTAS_KERJA_SUBSTANTIF1 = DIRECTORY_PAGE + "kertas-kerja-substantif-p1";
    private static final String PAGE_KERTAS_KERJA_SUBSTANTIF2 = DIRECTORY_PAGE + "kertas-kerja-substantif-p2";
    private static final String PAGE_KERTAS_KERJA_MENUNGGU_TGP = DIRECTORY_PAGE + "kertas-kerja-menunggu-tanggapan";
    private static final String PAGE_KERTAS_KERJA_PEMERIKSAAN_TGP = DIRECTORY_PAGE + "kertas-kerja-pemeriksaan-tanggapan";
    private static final String PAGE_CEK_KESAMAAN = DIRECTORY_PAGE + "cek-kesamaan";
    private static final String REQUEST_MAPPING_AJAX_ADD_SIMILARITY_MARK = PATH_AJAX_ADD_SIMILARITY_MARK + "*";
    private static final String REQUEST_MAPPING_AJAX_REMOVE_SIMILARITY_MARK = PATH_AJAX_REMOVE_SIMILARITY_MARK + "*";
    private static final String REQUEST_MAPPING_AJAX_SEARCH_LIST_P1 = PATH_AJAX_SEARCH_LIST_P1 + "*";
    private static final String REQUEST_MAPPING_AJAX_SEARCH_LIST_P2 = PATH_AJAX_SEARCH_LIST_P2 + "*";
    private static final String REQUEST_MAPPING_AJAX_SEARCH_LIST_MENUNGGU_TGP = PATH_AJAX_SEARCH_LIST_MENUNGGU_TGP + "*";
    private static final String REQUEST_MAPPING_AJAX_SEARCH_LIST_PEMERIKSAAN_TGP = PATH_AJAX_SEARCH_LIST_PEMERIKSAAN_TGP + "*";
    private static final String REQUEST_MAPPING_AJAX_SEARCH_CEK_KESAMAAN = PATH_AJAX_SEARCH_CEK_KESAMAAN + "*";
    private static final String REQUEST_MAPPING_AJAX_SEARCH_RESULT_KESAMAAN = PATH_AJAX_SEARCH_RESULT_KESAMAAN + "*";
    @Autowired
    private PemeriksaanSubstantifService pemeriksaanService;
    @Autowired
    private ClassService classService;

    @Autowired
    NativeQueryRepository nativeQueryRepository ;

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
        model.addAttribute("menu", "kertasKerjaSubstantif");
    }
    /***************************** - END PEMERIKSAAN SUBSTANTIF SECTION - ****************************/

    /***************************** - PEMERIKSAAN SUBSTANTIF 1 SECTION - ****************************/
    @RequestMapping(value = REQUEST_MAPPING_KERTAS_KERJA_SUBSTANTIF1, method = {RequestMethod.GET})
    public String doShowPageKertasKerjaSubstantif1(@RequestParam(value = "error", required = false) String error, final Model model, final HttpServletRequest request, final HttpServletResponse response) {
        model.addAttribute("classList", classService.findAllMClass());
        model.addAttribute("statusList", statusService.selectStatus());

        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("errorMessage", error);
        }

        return PAGE_KERTAS_KERJA_SUBSTANTIF1;
    }
    /***************************** - END PEMERIKSAAN SUBSTANTIF 1 SECTION - ****************************/

    /***************************** - PEMERIKSAAN SUBSTANTIF 2 SECTION - ****************************/
    @RequestMapping(value = REQUEST_MAPPING_KERTAS_KERJA_SUBSTANTIF2, method = {RequestMethod.GET})
    public String doShowPageKertasKerjaSubstantif2(@RequestParam(value = "error", required = false) String error, final Model model, final HttpServletRequest request, final HttpServletResponse response) {
        model.addAttribute("classList", classService.findAllMClass());
        model.addAttribute("statusList", statusService.selectStatus());

        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("errorMessage", error);
        }

        return PAGE_KERTAS_KERJA_SUBSTANTIF2;
    }
    /***************************** - END PEMERIKSAAN SUBSTANTIF 2 SECTION - ****************************/

    /***************************** - PEMERIKSAAN SUBSTANTIF MENUNGGU TGP SECTION - ****************************/
    @RequestMapping(value = REQUEST_MAPPING_KERTAS_KERJA_MENUNGGU_TGP, method = {RequestMethod.GET})
    public String doShowPageKertasKerjaMenungguTgp(@RequestParam(value = "error", required = false) String error, final Model model, final HttpServletRequest request, final HttpServletResponse response) {
        model.addAttribute("classList", classService.findAllMClass());
        model.addAttribute("statusList", statusService.selectStatus());

        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("errorMessage", error);
        }

        return PAGE_KERTAS_KERJA_MENUNGGU_TGP;
    }
    /***************************** - END PEMERIKSAAN SUBSTANTIF MENUNGGU TGP SECTION - ****************************/

    /***************************** - PEMERIKSAAN SUBSTANTIF PEMERIKSAAN TGP SECTION - ****************************/
    @RequestMapping(value = REQUEST_MAPPING_KERTAS_KERJA_PEMERIKSAAN_TGP, method = {RequestMethod.GET})
    public String doShowPageKertasKerjaPemeriksaanTgp(@RequestParam(value = "error", required = false) String error, final Model model, final HttpServletRequest request, final HttpServletResponse response) {
        model.addAttribute("classList", classService.findAllMClass());
        model.addAttribute("statusList", statusService.selectStatus());

        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("errorMessage", error);
        }

        return PAGE_KERTAS_KERJA_PEMERIKSAAN_TGP;
    }
    /***************************** - END PEMERIKSAAN SUBSTANTIF PEMERIKSAAN TGP SECTION - ****************************/

    /***************************** - CARI PEMERIKSAAN SUBSTANTIF P1 SECTION - ****************************/
    @RequestMapping(value = REQUEST_MAPPING_AJAX_SEARCH_LIST_P1, method = {RequestMethod.POST})
    public void doSearchDataKertasKerjaSubstantif1(final HttpServletRequest request, final HttpServletResponse response, Principal principal) throws IOException {
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

            MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            String[] searchByArr = request.getParameterValues("searchByArr[]");
            String[] keywordArr = request.getParameterValues("keywordArr[]");
            List<KeyValue> searchCriteria = new ArrayList<>();
            searchCriteria.add(new KeyValue("groupDetailStatus", GroupDetailStatusEnum.RELEASE.name(), true));
//          TM_DISTRIBUSI_DOKUMEN	(TM) Pemeriksa Substantif 1 (DISTDOC)
            searchCriteria.add(new KeyValue("txTmGeneral.mStatus.name", "Pemeriksa Substantif 1", false));
//            107	(TM) Pemeriksa Substantif 1
//            searchCriteria.add(new KeyValue("txTmGeneral.mStatus.id", "107", true));

            if(!mUser.getId().equals("SUPER")){
                searchCriteria.add(new KeyValue("mUserCurrent", mUser, true));
            }

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
                        	orderBy = "mClass.no";
                            break;
                        case "6":
                            orderBy = "txTmGeneral.txTmOwner.name";
                            break;
                        case "7":
                            orderBy = "status";
                            break;
                        case "8":
                            orderBy = "txTmGeneral.txTmPriorList.priorDate";
                            break;
                        case "9":
                            //orderBy = "GroupDetailStatusEnum.valueOf(result.getStatus()).getLabel()";
                        	orderBy = "groupDetailStatus";
                            break;
                        case "10":
                            //orderBy = "mUserCurrent.mEmployee.employeeName";
                        	orderBy = "mUserCurrent.username";
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

                    classList=result.getTxTmGeneral().getClassList();
                    
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
                				//		+ " maupun di di folder " + uploadFileBrandPath + uploadFileIpasPath );
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
                        String no_id = result.getTxTmGeneral().getId();
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
                            "<a href=\"" + getPathURLAfterLogin("/cek-kesamaan" + "?id=" + result.getId()) + "\">" + result.getTxTmGeneral().getApplicationNo() + "</a>",
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
    /***************************** - END CARI PEMERIKSAAN SUBSTANTIF 1 SECTION - ****************************/

    /***************************** - CARI PEMERIKSAAN SUBSTANTIF P2 SECTION - ****************************/
    @RequestMapping(value = REQUEST_MAPPING_AJAX_SEARCH_LIST_P2, method = {RequestMethod.POST})
    public void doSearchDataKertasKerjaSubstantif2(final HttpServletRequest request, final HttpServletResponse response, Principal principal) throws IOException {
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

            MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            String[] searchByArr = request.getParameterValues("searchByArr[]");
            String[] keywordArr = request.getParameterValues("keywordArr[]");
            List<KeyValue> searchCriteria = new ArrayList<>();
            searchCriteria.add(new KeyValue("groupDetailStatus", GroupDetailStatusEnum.RELEASE.name(), true));
//            263	(TM) Pemeriksa Substantif 2
            searchCriteria.add(new KeyValue("txTmGeneral.mStatus.id", "263", true));


            if(!mUser.getId().equals("SUPER")){
                searchCriteria.add(new KeyValue("mUserCurrent", mUser, true));
            }

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
                            orderBy = "mClass.no";
                            break;
                        case "6":
                            orderBy = "txTmGeneral.txTmOwner.name";
                            break;
                        case "7":
                            orderBy = "status";
                            break;
                        case "8":
                            orderBy = "txTmGeneral.txTmPriorList.priorDate";
                            break;
                        case "9":
                            //orderBy = "GroupDetailStatusEnum.valueOf(result.getStatus()).getLabel()";
                            orderBy = "groupDetailStatus";
                            break;
                        case "10":
                            //orderBy = "mUserCurrent.mEmployee.employeeName";
                            orderBy = "mUserCurrent.username";
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

                    classList=result.getTxTmGeneral().getClassList();

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
                                //		+ " maupun di di folder " + uploadFileBrandPath + uploadFileIpasPath );
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
                        String no_id = result.getTxTmGeneral().getId();
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
                            "<a href=\"" + getPathURLAfterLogin("/cek-kesamaan" + "?id=" + result.getId()) + "\">" + result.getTxTmGeneral().getApplicationNo() + "</a>",
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
    /***************************** - END CARI PEMERIKSAAN SUBSTANTIF 2 SECTION - ****************************/

    /***************************** - CARI PEMERIKSAAN SUBSTANTIF MENUNGGU TGP SECTION - ****************************/
    @RequestMapping(value = REQUEST_MAPPING_AJAX_SEARCH_LIST_MENUNGGU_TGP, method = {RequestMethod.POST})
    public void doSearchDataKertasKerjaMenungguTGP(final HttpServletRequest request, final HttpServletResponse response, Principal principal) throws IOException {
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

            MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            String[] searchByArr = request.getParameterValues("searchByArr[]");
            String[] keywordArr = request.getParameterValues("keywordArr[]");
            List<KeyValue> searchCriteria = new ArrayList<>();
            searchCriteria.add(new KeyValue("groupDetailStatus", GroupDetailStatusEnum.RELEASE.name(), true));
//            929	(TM) Menunggu Tanggapan Substantif
//            searchCriteria.add(new KeyValue("txTmGeneral.mStatus.id", "929", true));
//            120	(TM) Menunggu Tanggapan Atas Usul Penolakan
            searchCriteria.add(new KeyValue("txTmGeneral.mStatus.id", "120", true));

            if(!mUser.getId().equals("SUPER")){
                searchCriteria.add(new KeyValue("mUserCurrent", mUser, true));
            }

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
                            orderBy = "mClass.no";
                            break;
                        case "6":
                            orderBy = "txTmGeneral.txTmOwner.name";
                            break;
                        case "7":
                            orderBy = "status";
                            break;
                        case "8":
                            orderBy = "txTmGeneral.txTmPriorList.priorDate";
                            break;
                        case "9":
                            //orderBy = "GroupDetailStatusEnum.valueOf(result.getStatus()).getLabel()";
                            orderBy = "groupDetailStatus";
                            break;
                        case "10":
                            //orderBy = "mUserCurrent.mEmployee.employeeName";
                            orderBy = "mUserCurrent.username";
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

                    classList=result.getTxTmGeneral().getClassList();

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
                                //		+ " maupun di di folder " + uploadFileBrandPath + uploadFileIpasPath );
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
                        String no_id = result.getTxTmGeneral().getId();
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
                            "<a href=\"" + getPathURLAfterLogin("/cek-kesamaan" + "?id=" + result.getId()) + "\">" + result.getTxTmGeneral().getApplicationNo() + "</a>",
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
    /***************************** - END CARI PEMERIKSAAN SUBSTANTIF MENUNGGU TGP SECTION - ****************************/


    /***************************** - CARI PEMERIKSAAN SUBSTANTIF PEMERIKSAAN TGP SECTION - ****************************/
    @RequestMapping(value = REQUEST_MAPPING_AJAX_SEARCH_LIST_PEMERIKSAAN_TGP, method = {RequestMethod.POST})
    public void doSearchDataKertasKerjaPemeriksaanTGP(final HttpServletRequest request, final HttpServletResponse response, Principal principal) throws IOException {
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

            MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            String[] searchByArr = request.getParameterValues("searchByArr[]");
            String[] keywordArr = request.getParameterValues("keywordArr[]");
            List<KeyValue> searchCriteria = new ArrayList<>();
            searchCriteria.add(new KeyValue("groupDetailStatus", GroupDetailStatusEnum.RELEASE.name(), true));
//            930	(TM) Pemeriksaan Substantif Setelah Usulan Penolakan
            searchCriteria.add(new KeyValue("txTmGeneral.mStatus.id", "930", true));

            if(!mUser.getId().equals("SUPER")){
                searchCriteria.add(new KeyValue("mUserCurrent", mUser, true));
            }

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
                            orderBy = "mClass.no";
                            break;
                        case "6":
                            orderBy = "txTmGeneral.txTmOwner.name";
                            break;
                        case "7":
                            orderBy = "status";
                            break;
                        case "8":
                            orderBy = "txTmGeneral.txTmPriorList.priorDate";
                            break;
                        case "9":
                            //orderBy = "GroupDetailStatusEnum.valueOf(result.getStatus()).getLabel()";
                            orderBy = "groupDetailStatus";
                            break;
                        case "10":
                            //orderBy = "mUserCurrent.mEmployee.employeeName";
                            orderBy = "mUserCurrent.username";
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

                    classList=result.getTxTmGeneral().getClassList();

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
                                //		+ " maupun di di folder " + uploadFileBrandPath + uploadFileIpasPath );
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
                        String no_id = result.getTxTmGeneral().getId();
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
                            "<a href=\"" + getPathURLAfterLogin("/cek-kesamaan" + "?id=" + result.getId()) + "\">" + result.getTxTmGeneral().getApplicationNo() + "</a>",
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
    /***************************** - END CARI PEMERIKSAAN TGP SECTION - ****************************/

    /***************************** - CEK KESAMAAN SECTION - ****************************/
    @RequestMapping(value = REQUEST_MAPPING_CEK_KESAMAAN, method = {RequestMethod.GET})
    public String doShowPageCekKertasKerjaKesamaan(final Model model, final HttpServletRequest request, final HttpServletResponse response) {
        TxGroupDetail txGroupDetail = pemeriksaanService.selectOneTxGroupDetail("id", request.getParameter("id"));

        if (txGroupDetail == null) {
            return REDIRECT_CEK_SUBSTANTIF1 + "?error=Nomor tidak ditemukan";
        }
        
        String classNo = "";
        List<TxTmClass> txTmClasses = classService.findTxTmClassByTxTmGeneral(txGroupDetail.getTxTmGeneral());
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
    @RequestMapping(path = REQUEST_RELEASE_KERTAS_KERJA_SUBSTANTIF, method = {RequestMethod.POST})
    public void doReleaseKertasKerjaSubstantif(@RequestParam("id") String id, final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
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

    /***************************** - REASSIGN PEMERIKSAAN SUBSTANTIF - ****************************/
    @RequestMapping(path = REQUEST_REASSIGN_KERTAS_KERJA_SUBSTANTIF, method = {RequestMethod.POST})
    public void doReassignKertasKerjaSubstantif(@RequestParam("id") String id, @RequestParam("pemeriksa") String pemeriksa,
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
    @RequestMapping(path = REQUEST_VALIDASI_KERTAS_KERJA_SUBSTANTIF, method = {RequestMethod.POST})
    public void doValidasiKertasKerjaSubstantif(@RequestParam("id") String id, @RequestParam("pemeriksa") String pemeriksa,
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
    @RequestMapping(path = REQUEST_SELESAI_KERTAS_KERJA_SUBSTANTIF, method = {RequestMethod.POST})
    public void doSelesaiKertasKerjaSubstantif(@RequestParam("id") String id, final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
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
    public void doSearchDataKertasKerjaCekKesamaan(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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

            GenericSearchWrapper<TxSubsCheck> searchResult = pemeriksaanService.searchSubsCheck(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxSubsCheck result : searchResult.getList()) {
                    no++;

                    data.add(new String[]{
                            result.getId(),
                            "" + no,
                            result.getName(),
                            result.getDescription(),
                            result.getmClass() == null ? "" : "" + result.getmClass().getNo(),
                            result.getmClass() == null ? "" : "" + result.getmClass().getId(),
                            result.getOwner(),
                            result.getClassDescription(),
                            //result.getClassTranslation(),
                            result.getCreatedBy().getUsername(),
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
    String doTambahKertasKerjaCekKesamaan(final HttpServletRequest request, final HttpServletResponse response) {
        String msgError = "Success";
        String appId = request.getParameter("appId");
        String userRoleSubstantif = request.getParameter("userRoleSubstantif");
        String brandName = request.getParameter("brandName");
        String brandDesc = request.getParameter("brandDesc");
        String ownerName = request.getParameter("ownerName");
        String classId = request.getParameter("classId");
        String classDesc = request.getParameter("classDesc");

        TxTmGeneral txTmGeneral = pemeriksaanService.selectOneTxTmGeneral("id", appId);
        MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MRoleSubstantifDetail mRoleSubstantifDetail = pemeriksaanService.getRoleSubstantifDetailByUsername(mUser.getUsername());

        if (brandName.equals("") && brandDesc.equals("") && ownerName.equals("") && classId.equals("") && classDesc.equals("")) {
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
                txSubsCheck.setmClass(classService.findOneMClass(classId));
                txSubsCheck.setClassDescription(classDesc);
                txSubsCheck.setCreatedBy(mUser);
                txSubsCheck.setMRoleSubstantif(mRoleSubstantif);
                pemeriksaanService.insertSubsCheck(txSubsCheck);
            } else {
                msgError = "Anda tidak memiliki akses untuk Pengecekan Kesamaan !!!";
            }
        }
        return msgError;
    }
    /***************************** - END TAMBAH CEK KESAMAAN SECTION - ****************************/

    /***************************** - CARI RESULT KESAMAAN SECTION - ****************************/
    @RequestMapping(value = REQUEST_MAPPING_AJAX_SEARCH_RESULT_KESAMAAN, method = {RequestMethod.POST})
    public void doSearchDataResultKertasKerjaKesamaan(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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
                            } else if (searchBy.equals("mClassId")) {
                                searchCriteria.add(new KeyValue(searchBy, value, true));
                            }
                            else {
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
                    File file = null;
                    FileInputStream brandLabel = null;
                    byte[] byteImage = null;

                    String registrationNo = "-";
                    String sbPriorDateList = "-";
                    ArrayList<String> spriorDate = new ArrayList<String>();

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
                            (result.isSimilar() ? "Ya" : "Tidak"),

                            "<button type=\"button\" class=\"btn btn-xs " + (result.isSimilar() ? "btn-danger remove-mark" : "btn-success add-mark") + "\" appId=\"" + result.getCurrentId() + "\">" + (result.isSimilar() ? "Hilangkan Tanda" : "Tandai") + "</button>"
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
    public void doAjaxAddKertasKerjaSimilarityMark(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        doAjaxChangeKertasKerjaSimilarityMark(request, response, true);
    }

    @PostMapping(REQUEST_MAPPING_AJAX_REMOVE_SIMILARITY_MARK)
    public void doAjaxRemoveKertasKerjaSimilarityMark(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        doAjaxChangeKertasKerjaSimilarityMark(request, response, false);
    }

    private void doAjaxChangeKertasKerjaSimilarityMark(final HttpServletRequest request, final HttpServletResponse response, boolean addMark) throws IOException {
        if (isAjaxRequest(request)) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);

            String originId = request.getParameter("originId");
            String similarId = request.getParameter("similarId");
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

                    TxSimilarityResult form = new TxSimilarityResult();
                    form.setOriginTxTmGeneral(originTxTmGeneral);
                    form.setSimilarTxTmGeneral(similarTxTmGeneral);

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
}
