package com.docotel.ki.controller;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.enumeration.ClassStatusEnum;
import com.docotel.ki.enumeration.PriorStatusEnum;
import com.docotel.ki.model.master.*;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.NativeQueryRepository;
import com.docotel.ki.repository.custom.master.MClassDetailCustomRepository;
import com.docotel.ki.repository.master.MWorkflowRepository;
import com.docotel.ki.service.master.*;
import com.docotel.ki.service.transaction.*;
import com.docotel.ki.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class EditPermohonanController extends BaseController {
    private static final String DIRECTORY_PAGE = "permohonan-ubah/";
    private static final String PATH_UBAH_PERMOHONAN_GENERAL = "/form-edit-general";
    private static final String PATH_UBAH_PERMOHONAN_PAGE_GENERAL = DIRECTORY_PAGE + "form-1-edit-general";
    private static final String PATH_UBAH_PERMOHONAN_PEMOHON = "/form-edit-pemohon";
    private static final String PATH_UBAH_PERMOHONAN_PAGE_PEMOHON = DIRECTORY_PAGE + "form-2-edit-pemohon";
    private static final String PATH_UBAH_PERMOHONAN_KUASA = "/form-edit-kuasa";
    private static final String PATH_UBAH_PERMOHONAN_PAGE_KUASA = DIRECTORY_PAGE + "form-3-edit-kuasa";
    private static final String PATH_UBAH_PERMOHONAN_PRIORITAS = "/form-edit-prioritas";
    private static final String PATH_UBAH_PERMOHONAN_PAGE_PRIORITAS = DIRECTORY_PAGE + "form-4-edit-prioritas";
    private static final String PATH_UBAH_PERMOHONAN_MEREK = "/form-edit-merek";
    private static final String PATH_UBAH_PERMOHONAN_PAGE_MEREK = DIRECTORY_PAGE + "form-5-edit-merek";
    private static final String PATH_UBAH_PERMOHONAN_KELAS = "/form-edit-kelas";
    private static final String PATH_UBAH_PERMOHONAN_PAGE_KELAS = DIRECTORY_PAGE + "form-6-edit-kelas";
    private static final String PATH_TAMBAH_PERMOHONAN_PEMOHON = "/tambah-pemohon";
    private static final String PATH_TAMBAH_PERMOHONAN_PAGE_PEMOHON = DIRECTORY_PAGE + "/tambah-pemohon";
    private static final String PATH_TAMBAH_PERMOHONAN_KUASA = "/tambah-kuasa";
    private static final String PATH_TAMBAH_PERMOHONAN_PAGE_KUASA = DIRECTORY_PAGE + "/tambah-kuasa";
    private static final String PATH_UBAH_PERMOHONAN_DOKUMEN_LAMPIRAN = "/form-edit-dokumen-lampiran";
    private static final String PATH_UBAH_PERMOHONAN_PAGE_DOKUMEN_LAMPIRAN = DIRECTORY_PAGE + "form-7-edit-dokumen-lampiran";
    private static final String PATH_UBAH_PERMOHONAN_LICENSE = "/form-ubah-lisensi";
    private static final String PATH_UBAH_PERMOHONAN_PAGE_LICENSE = DIRECTORY_PAGE + "ubah-lisensi";
    private static final String PATH_UBAH_PERMOHONAN_BIAYA = "/form-edit-biaya";
    private static final String PATH_UBAH_PERMOHONAN_PAGE_BIAYA = DIRECTORY_PAGE + "ubah-biaya";
    private static final String PATH_TAMBAH_LICENSE = "/tambah-lisensi";
    private static final String PATH_TAMBAH_LICENSE_PAGE_LICENSE = DIRECTORY_PAGE + "tambah-lisensi";
    private static final String PATH_PRIOR_GET_DATA = "/get-data-prior";
    private static final String PATH_TXCLASS_GET_DATA = "/get-data-txclass";
    private static final String PATH_PRIOR_UPDATE_DATA = "/update-data-prioritas";
    private static final String PATH_UPDATE_DATA_KELAS = "/update-data-kelas";
    private static final String REQUEST_MAPPING_UBAH_GENERAL = PATH_UBAH_PERMOHONAN_GENERAL + "*";
    private static final String REQUEST_MAPPING_UBAH_PEMOHON = PATH_UBAH_PERMOHONAN_PEMOHON + "*";
    private static final String REQUEST_MAPPING_UBAH_KUASA = PATH_UBAH_PERMOHONAN_KUASA + "*";
    private static final String REQUEST_MAPPING_UBAH_PRIORITAS = PATH_UBAH_PERMOHONAN_PRIORITAS + "*";
    private static final String REQUEST_MAPPING_UBAH_MEREK = PATH_UBAH_PERMOHONAN_MEREK + "*";
    private static final String REQUEST_MAPPING_UBAH_KELAS = PATH_UBAH_PERMOHONAN_KELAS + "*";
    private static final String REQUEST_MAPPING_SETUJUI_SEMUA = PATH_UBAH_PERMOHONAN_KELAS + "/setujui-semua" + "*";
    private static final String REQUEST_MAPPING_TOLAK_SEMUA = PATH_UBAH_PERMOHONAN_KELAS + "/tolak-semua" + "*";
    private static final String REQUEST_MAPPING_UBAH_DOKUMEN_LAMPIRAN = PATH_UBAH_PERMOHONAN_DOKUMEN_LAMPIRAN + "*";
    private static final String REQUEST_MAPPING_UBAH_LICENSE = PATH_UBAH_PERMOHONAN_LICENSE + "*";
    private static final String REQUEST_MAPPING_UBAH_BIAYA = PATH_UBAH_PERMOHONAN_BIAYA + "*";
    private static final String REQUEST_MAPPING_TAMBAH_PEMOHON = PATH_TAMBAH_PERMOHONAN_PEMOHON + "*";
    private static final String REQUEST_MAPPING_TAMBAH_LICENSE = PATH_TAMBAH_LICENSE + "*";
    private static final String REQUEST_MAPPING_TAMBAH_KUASA = PATH_TAMBAH_PERMOHONAN_KUASA + "*";
    private static final String REQUEST_MAPPING_PRIOR_GET_DATA = PATH_PRIOR_GET_DATA + "*";
    private static final String REQUEST_MAPPING_TXCLASS_GET_DATA = PATH_TXCLASS_GET_DATA + "*";
    private static final String REQUEST_MAPPING_PRIOR_UPDATE_DATA = PATH_PRIOR_UPDATE_DATA + "*";
    private static final String REQUEST_MAPPING_UPDATE_DATA_KELAS = PATH_UPDATE_DATA_KELAS + "*";
    @Autowired
    private TrademarkService trademarkService;
    @Autowired
    private LawService lawService;
    @Autowired
    private FileService fileService;
    @Autowired
    private PermohonanService permohonanService;
    @Autowired
    private CountryService countryService;
    @Autowired
    private ProvinceService provinceService;
    @Autowired
    private CityService cityService;
    @Autowired
    private DokumenLampiranService doclampiranService;
    @Autowired
    private DocTypeService docTypeService;
    @Autowired
    private ClassService classService;
    @Autowired
    private ReprsService reprsService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private PriorService priorService;
    @Autowired
    private LookupService lookupService;
    @Autowired
    private LicenseService licenseService;
    @Autowired
    private UserService userService;
    @Autowired
    private MClassDetailCustomRepository mClassDetailCustomRepository ;
    @Autowired
    private StatusService statusService;
    @Autowired
    private MActionService mActionService;

    @Autowired
    private NativeQueryRepository nativeQueryRepository ;

    @Autowired
    private MWorkflowRepository mWorkflowRepository;
    @Value("${upload.file.brand.path:}")
    private String uploadFileBrandPath;

    /*------General Section-------*/
    @RequestMapping(path = REQUEST_MAPPING_UBAH_GENERAL)
    public String doShowEditGeneral(Model model, @RequestParam(value = "no", required = true) String no) {
        TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(no);
        TxReception txReception = txTmGeneral.getTxReception();

        //List<MFileTypeDetail> mFileTypeDetailList1 = fileService.getAllFileTypeDetailByFileType("mFileType.id", txReception.getmFileType().getId());
        List<MFileTypeDetail> mFileTypeDetailList1 = fileService.getAllFileTypeDetail();
        List<MLaw> listMLaw = lawService.findAll();
        List<MWorkflow> listWorkflow = mWorkflowRepository.findAll();
        List<MAction> actionList = mActionService.selectActionActive();
        List<MStatus> statusList = statusService.selectStatusActive();

        //MLaw mLaw = lawService.GetMlawById("b9e7c8f2-771a-4356-a465-b962b4c46668");

        List<KeyValue> searchCriteria = new ArrayList<>();
        searchCriteria.add(new KeyValue("menu", "DAFTAR", true));
        searchCriteria.add(new KeyValue("statusFlag", true, true));
        List<MFileType> fileTypeList = fileService.selectAllMFileType(searchCriteria, "desc", "ASC", null, null);

        model.addAttribute("fileTypeList", fileTypeList);
        model.addAttribute("listMLaw", listMLaw);
        model.addAttribute("listFileTypeDetail", mFileTypeDetailList1);
        model.addAttribute("noGeneral", no);
        model.addAttribute("listMWorkflow", listWorkflow);
        model.addAttribute("dataGeneral", txTmGeneral);
        model.addAttribute("dataLoketPenerimaan", txReception);
        model.addAttribute("statusList", statusList);
        model.addAttribute("actionList", actionList);

        return PATH_UBAH_PERMOHONAN_PAGE_GENERAL;
    }
    /*------ End of General Section-------*/

    /*------Pemohon Section-------*/
    @RequestMapping(path = REQUEST_MAPPING_UBAH_PEMOHON)
    public String doShowEditPemohon(Model model, @RequestParam(value = "id", required = true) String id, @RequestParam(value = "no", required = true) String no) {
        TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(no);
        TxTmOwner txTmOwner = permohonanService.selectOneOwnerById(id);
        List<TxTmOwnerDetail> txTmOwnerDetailList = null;
        if (txTmOwner == null) {
            txTmOwner = new TxTmOwner();
        } else {
            txTmOwnerDetailList = permohonanService.selectAllOwnerByOwnerId(txTmOwner.getId());
        }
        txTmOwner.setTxTmGeneral(txTmGeneral);
        List<MCountry> mCountryList = countryService.findAll();
        Collections.sort(mCountryList, (o1, o2) -> o1.getName().compareTo(o2.getName()));
        List<MProvince> mProvinceList = provinceService.findAll();
        List<MCity> mCityList = cityService.selectAllOrderByName();

        // cari secara DISTINCT dari table TX_TM_OWNER, tampilkan apa adanya - F
        List<String> listownerType = nativeQueryRepository.selectDistinct("TX_TM_OWNER","TM_OWNER_TYPE");
        model.addAttribute("lownerType",listownerType);

        List<MLookup> mRightList = lookupService.selectAllbyGroup("HakPengajuan");
        model.addAttribute("listRight", mRightList);

        model.addAttribute("noGeneral", no);
        model.addAttribute("dataGeneral", txTmGeneral);
        model.addAttribute("pemohon", txTmOwner);
        model.addAttribute("listCountry", mCountryList);

        model.addAttribute("listProvince", mProvinceList);
        model.addAttribute("listCity", mCityList);
        model.addAttribute("listPemohonChild", txTmOwnerDetailList);

        return PATH_UBAH_PERMOHONAN_PAGE_PEMOHON;
    }
    /*------ End of Pemohon Section-------*/

    /*------Kuasa Section-------*/
    @RequestMapping(path = REQUEST_MAPPING_UBAH_KUASA)
    public String doShowEditGeneralKuasa(Model model, @RequestParam(value = "id", required = true) String id, @RequestParam(value = "no", required = true) String no) {
        TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(no);
        /*TxTmRepresentative txTmRepresentative = reprsService.selectOneByApplicationId(txTmGeneral.getId());*/

        TxTmRepresentative txTmRepresentative = reprsService.selectOneById(id);
        MCountry mCountry = new MCountry();
        MProvince mProvince = new MProvince();
        MCity mCity = new MCity();
        if (txTmRepresentative == null) {
            MRepresentative mRepresentative = new MRepresentative();

            mRepresentative.setmCity(mCity);
            mRepresentative.setmProvince(mProvince);
            mRepresentative.setmCountry(mCountry);

            /**-- GET TX_TM_REPRS --- */
            txTmRepresentative.setmCity( mCity );
            txTmRepresentative.setmProvince( mProvince );
            txTmRepresentative.setmCountry( mCountry );

            txTmRepresentative = new TxTmRepresentative();
            txTmRepresentative.setmRepresentative(mRepresentative);

        } else {
            if (txTmRepresentative.getmRepresentative().getmCountry() == null) {
                txTmRepresentative.getmRepresentative().setmCountry(mCountry);
            }
            if (txTmRepresentative.getmRepresentative().getmProvince() == null) {
                txTmRepresentative.getmRepresentative().setmProvince(mProvince);
            }
            if (txTmRepresentative.getmRepresentative().getmCity() == null) {
                txTmRepresentative.getmRepresentative().setmCity(mCity);
            }

            /**-- GET TX_TM_REPRS --- */
            if (txTmRepresentative.getmCountry() == null) {
                txTmRepresentative.setmCountry( mCountry );
            }
            if (txTmRepresentative.getmProvince() == null) {
                txTmRepresentative.setmProvince( mProvince );
            }
            if (txTmRepresentative.getmCity() == null) {
                txTmRepresentative.setmCity( mCity );
            }

        }

        model.addAttribute("noGeneral", no);
        model.addAttribute("txTmReprs", txTmRepresentative);

        return PATH_UBAH_PERMOHONAN_PAGE_KUASA;
    }
    /*------ End of Kuasa Section-------*/

    /*------Tambah Kuasa Section-------*/
    @RequestMapping(path = REQUEST_MAPPING_TAMBAH_KUASA)
    public String doShowTambahKuasa(Model model, @RequestParam(value = "no", required = true) String no) {
        TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(no);

        TxTmRepresentative txTmRepresentative = new TxTmRepresentative();
        MRepresentative mRepresentative = new MRepresentative();

        MCountry mCountry = new MCountry();
        MProvince mProvince = new MProvince();
        MCity mCity = new MCity();

        txTmRepresentative.setTxTmGeneral(txTmGeneral);
        txTmRepresentative.setmRepresentative(mRepresentative);

        mRepresentative.setmCity(mCity);
        mRepresentative.setmProvince(mProvince);
        mRepresentative.setmCountry(mCountry);

        if (txTmRepresentative.getmRepresentative().getmCountry() == null) {
            txTmRepresentative.getmRepresentative().setmCountry(mCountry);
        }
        if (txTmRepresentative.getmRepresentative().getmProvince() == null) {
            txTmRepresentative.getmRepresentative().setmProvince(mProvince);
        }
        if (txTmRepresentative.getmRepresentative().getmCity() == null) {
            txTmRepresentative.getmRepresentative().setmCity(mCity);
        }

        model.addAttribute("noGeneral", no);
        model.addAttribute("txTmReprs", txTmRepresentative);

        return PATH_TAMBAH_PERMOHONAN_PAGE_KUASA;
    }
    /*------ End of Tambah Kuasa Section-------*/

    /*------Prioritas Section-------*/
    @RequestMapping(path = REQUEST_MAPPING_UBAH_PRIORITAS)
    public String doShowEditPrioritas(Model model, @RequestParam(value = "no", required = true) String no) {
        TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(no);
        TxTmRepresentative txTmRepresentative = reprsService.selectOneByApplicationId(txTmGeneral.getId());
        List<MCountry> mCountryList = countryService.findByStatusFlagTrue();

        model.addAttribute("noGeneral", no);
        model.addAttribute("listCountry", mCountryList);
        model.addAttribute("txTmReprs", txTmRepresentative);
        model.addAttribute("listStatus", PriorStatusEnum.values());

        // For user role access button menu
        MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isButtonUpdate = false;
        if (mUser.hasAccessMenu("T-PUPPR")) {
            isButtonUpdate = true;
        }
        model.addAttribute("isButtonUpdate", isButtonUpdate);

        return PATH_UBAH_PERMOHONAN_PAGE_PRIORITAS;
    }

    @RequestMapping(path = REQUEST_MAPPING_PRIOR_GET_DATA, method = {RequestMethod.POST})
    public void getDataPrior(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            String idPrior = request.getParameter("idPrior");
            TxTmPrior txTmPrior = priorService.selectOnebyId(idPrior);

            txTmPrior.setTxTmGeneral(null);
            txTmPrior.setCreatedBy(null);
            txTmPrior.getmCountry().setCreatedBy(null);

            if (txTmPrior.getStatus().equals(null) || txTmPrior.getStatus().equals("")) {
                txTmPrior.setStatus(PriorStatusEnum.PENDING.name());
            }

            writeJsonResponse(response, txTmPrior);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }
    
    @RequestMapping(path = REQUEST_MAPPING_TXCLASS_GET_DATA, method = {RequestMethod.POST})
    public void getDataTxClass(final HttpServletRequest request, final HttpServletResponse response) {
    	if(isAjaxRequest(request)) {
    		setResponseAsJson(response);

            String idClass = request.getParameter("idClass");
            MClassDetail detail = classService.selectOne("id", idClass);
            detail.setCreatedBy(null);
            detail.getParentClass().setmClassDetailList(null);
            detail.getParentClass().setCreatedBy(null);
            writeJsonResponse(response, detail);
    	} else {
    		response.setStatus(HttpServletResponse.SC_FOUND);
    	}
    }

    @RequestMapping(value = REQUEST_MAPPING_PRIOR_UPDATE_DATA, method = {RequestMethod.GET})
    public void doInsertPrioritas(Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ParseException {
        if(isAjaxRequest(request)) {
        	setResponseAsJson(response);
        
        Map<String, Object> result = new HashMap<>();
 	   	result.put("success", false);
 	    	
 	   	String user = request.getParameter("user");
    	String priorId = request.getParameter("priorId");
        String negaraId = request.getParameter("negaraId");
        String no = request.getParameter("no");
        String appNo = request.getParameter("appNo");
        String status = request.getParameter("status");
        String note = request.getParameter("note");
        Timestamp tmstm = DateUtil.toDate("dd/MM/yyyy", request.getParameter("tgl"));

        MCountry mCountry = new MCountry();
        mCountry.setId(negaraId);
        
        List<KeyValue> searchCriteria = new ArrayList<>();       
		searchCriteria.add(new KeyValue("txTmGeneral", trademarkService.selectOne("applicationNo", appNo), true));
	    searchCriteria.add(new KeyValue("no", no, true));  
	    TxTmPrior existOnePrior = priorService.selectOneKriteria(null, searchCriteria, null, null);
	    if(existOnePrior!=null && priorId.equalsIgnoreCase("-")) {
	    	result.put("message", "Gagal Menyimpan Data Prioritas, Nomor Prioritas tidak boleh sama.");         	
	    } 
	    
	    if (result.size() == 1) {
        TxTmPrior txTmPrior = priorService.selectOnebyId(priorId);
        if (!priorId.equalsIgnoreCase("-") && txTmPrior != null) {
            txTmPrior.setId(txTmPrior.getId());
        } else {
            txTmPrior = new TxTmPrior();
            txTmPrior.setTxTmGeneral(trademarkService.selectOne("applicationNo", appNo));
            txTmPrior.setCreatedDate(tmstm);
            txTmPrior.setUpdatedBy(userService.selectOneUserByUserId(user));
        }
       		txTmPrior.setNo(no);
       		txTmPrior.setPriorDate(tmstm);
       		txTmPrior.setmCountry(mCountry);
       		txTmPrior.setStatus(status);
       		txTmPrior.setNote(note);
       		txTmPrior.setUpdatedBy(userService.selectOneUserByUserId(user));

       		priorService.doSaveOrUpdate(txTmPrior);
       		result.put("success", true);
	    }
	    
	    writeJsonResponse(response, result);
     }
  }
    /*------ End of Prioritas Section-------*/
    
    @RequestMapping(value = REQUEST_MAPPING_UPDATE_DATA_KELAS, method = {RequestMethod.GET})
    public void doUpdateKelas(Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ParseException {
        if(isAjaxRequest(request)) {
        	setResponseAsJson(response);
        
        Map<String, Object> result = new HashMap<>();
 	   	result.put("success", false);
 	    	
    	String txIdClass = request.getParameter("txIdClass");
    	String classId = request.getParameter("classId");
        String desc = request.getParameter("desc");
        String descEn = request.getParameter("descEn");
        String serialE = request.getParameter("serialE");
        String serialF = request.getParameter("serialF");
        String appNo = request.getParameter("appNo");
        String nomorDasar = request.getParameter("nomorDasar");
        String statusFlag = request.getParameter("statusFlag");
        
        Timestamp tmstm = new Timestamp(System.currentTimeMillis());
        
	    if (result.size() == 1) {
            if(txIdClass != null && txIdClass.length() != 0) {
                MClassDetail mClassDetail = classService.selectOne("id", txIdClass);

                mClassDetail.getParentClass().setId(classId);
                mClassDetail.setCreatedDate(tmstm);
                mClassDetail.setDesc(desc);
                mClassDetail.setDescEn(descEn);
                mClassDetail.setSerial1(serialE);
                mClassDetail.setSerial2(serialF);
                mClassDetail.setClassBaseNo(nomorDasar);
                mClassDetail.setStatusFlag(Boolean.parseBoolean(statusFlag));
                mClassDetail.setUpdatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                mClassDetail.setUpdatedDate(tmstm);

                classService.saveClassChild(mClassDetail);

                TxTmClass txTmClass = classService.selectOne(new String[]{"txTmGeneral.id", "mClassDetail.id"}, new String[]{appNo, classId});
                if(txTmClass != null) {
                    MClass mClass = classService.findOneMClass(classId);
                    txTmClass.setTxTmGeneral(trademarkService.selectOne("applicationNo", appNo));
                    txTmClass.setmClass(mClass);
                    txTmClass.setmClassDetail(mClassDetail);
                    txTmClass.setUpdatedDate(tmstm);
                    txTmClass.setUpdatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                    txTmClass.setEdition(mClass.getEdition());
                    txTmClass.setVersion(mClass.getVersion());
                    //insert kedalam txTmClass
                    classService.saveTxTMClass(txTmClass);
                }
            }
            result.put("success", true);
	    }
	    
	    writeJsonResponse(response, result);
     }
  }

    /*------Merek Section-------*/
    @RequestMapping(path = REQUEST_MAPPING_UBAH_MEREK)
    public String doShowEditMerek(Model model, @RequestParam(value = "no", required = true) String no) {
        TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(no);
        List<MBrandType> listMBrandType = brandService.findByStatusFlagTrue();
        TxTmBrand txTmBrand = brandService.selectOneByAppId(txTmGeneral.getId());
        if (txTmBrand == null) {
            txTmBrand = new TxTmBrand();
            txTmBrand.setmBrandType(new MBrandType());
        }
        TxTmBrandDetail txTmBrandDetail = new TxTmBrandDetail();
        try {
            String pathFolder = DateUtil.formatDate(txTmBrand.getCreatedDate(), "yyyy/MM/dd/");
            File file = new File(uploadFileBrandPath + pathFolder + txTmBrand.getId() + ".jpg");
            if (file.exists() && !file.isDirectory()) {
                FileInputStream fileInputStreamReader = new FileInputStream(file);
                byte[] bytes = new byte[(int) file.length()];
                fileInputStreamReader.read(bytes);
                model.addAttribute("imgMerek", "data:image/jpg;base64," + Base64.getEncoder().encodeToString(bytes));
            } else {
                model.addAttribute("imgMerek", "");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        model.addAttribute("noGeneral", no);
        model.addAttribute("txTmBrandDetail", txTmBrandDetail);
        model.addAttribute("txTmBrand", txTmBrand);
        model.addAttribute("listBrandType", listMBrandType);
        return PATH_UBAH_PERMOHONAN_PAGE_MEREK;
    }
    /*------ End of Merek Section-------*/

    /*------Kelas Section-------*/
    @RequestMapping(path = REQUEST_MAPPING_UBAH_KELAS)
    //public String doShowEditKelas(Model model, @RequestParam(value = "no", required = true) String no) {
    public String doShowEditKelas(Model model, @RequestParam(value = "no", required = true) String no,
                                  final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {

        TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(no);
        TxReception txReception = trademarkService.selectOneReceptionByApplicationNo(no);
        TxTmClass txTmClass = classService.selectOneByAppId(txTmGeneral.getId());

        if (txTmClass == null) {
            MClass mClass = new MClass();
            MClassDetail mClassDetail = new MClassDetail();

            txTmClass = new TxTmClass();

            txTmClass.setmClass(mClass);
            txTmClass.setmClassDetail(mClassDetail);
        }

        List<MClass> listMClass = classService.findByStatusFlagTrue();
        List<MLookup> mKonfirm = lookupService.selectAllbyGroup("UICariKelas");
        //List<MClassDetail> listMClassDetail = classService.findAllMClassDetail();
        //Stream<MClassDetail> listMClassDetail = classService.findAllMClassStream();

        Collections.sort(listMClass, (o1, o2) -> new Integer(o1.getNo()).compareTo(new Integer(o2.getNo())));

        String classStatusEnum = "\"acceptValue\":\"" + ClassStatusEnum.ACCEPT.name() + "\",";
        classStatusEnum += "\"acceptLabel\":\"" + ClassStatusEnum.ACCEPT.getLabel() + "\",";
        classStatusEnum += "\"rejectValue\":\"" + ClassStatusEnum.REJECT.name() + "\",";
        classStatusEnum += "\"rejectLabel\":\"" + ClassStatusEnum.REJECT.getLabel() + "\"";


        model.addAttribute("txTmGeneral",txTmGeneral);
        model.addAttribute("listMClass", listMClass);
        //model.addAttribute("listMClassChild", listMClassDetail);
        model.addAttribute("lookupKonfirm", mKonfirm);
        model.addAttribute("classStatusEnum", "{" + classStatusEnum + "}");

        model.addAttribute("dataLoketPenerimaan", txReception);
        model.addAttribute("noGeneral", no);
//        model.addAttribute("txTmClass", txTmClass);

        return PATH_UBAH_PERMOHONAN_PAGE_KELAS;
    }

    @GetMapping(path = REQUEST_MAPPING_SETUJUI_SEMUA)
    public String doSetujuiSemuaKelas(Model model, @RequestParam(value = "no", required = true) String no){
        mClassDetailCustomRepository.setujuiSemua(no,"ACCEPT");
        return "redirect:" + BaseController.PATH_AFTER_LOGIN + PATH_UBAH_PERMOHONAN_KELAS +"?no="+no ;
    }
    @GetMapping(path = REQUEST_MAPPING_TOLAK_SEMUA)
    public String doTolakSemuaKelas(Model model, @RequestParam(value = "no", required = true) String no){
        mClassDetailCustomRepository.setujuiSemua(no,"REJECT");
        return "redirect:" + BaseController.PATH_AFTER_LOGIN + PATH_UBAH_PERMOHONAN_KELAS +"?no="+no ;
    }




    /*------ End of Kelas Section-------*/

    /*------Dokumen Lampiran Section-------*/
    @RequestMapping(path = REQUEST_MAPPING_UBAH_DOKUMEN_LAMPIRAN)
    public String doShowEditDokumenLampiran(Model model, @RequestParam(value = "no", required = true) String no) {
        // DOC SECTION //
        TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(no);
        /*List<TxTmDoc> txtmDocList = doclampiranService.getAllDocByApplicationId("txTmGeneral.id", txTmGeneral.getId());*/
        List<TxTmDoc> txtmDocList = doclampiranService.selectAllDocByGeneralId(txTmGeneral.getId());
        List<MDocType> mDocTypeList = docTypeService.findByStatusFlagTrue();

        List<TxTmDoc> listDocType = new ArrayList<TxTmDoc>();

        for (MDocType result : mDocTypeList) {
            boolean status = txtmDocList == null ? false : txtmDocList.stream().filter(o -> o.getmDocType().getId().equals(result.getId())).findFirst().isPresent();
            TxTmDoc txTmDoc = new TxTmDoc();
            txTmDoc.setmDocType(result);
            txTmDoc.setStatus(status);
            listDocType.add(txTmDoc);
        }

        Collections.sort(listDocType, (o1, o2) -> o1.getmDocType().getName().compareTo(o2.getmDocType().getName()));
        model.addAttribute("noGeneral", no);
        model.addAttribute("listDocType", listDocType);
        model.addAttribute("docUploadDate", DateUtil.formatDate(new Date(), "dd/MM/yyyy"));

        return PATH_UBAH_PERMOHONAN_PAGE_DOKUMEN_LAMPIRAN;
    }
    /*------ End of Dokumen Lampiran Section-------*/

    /*------Pemohon Section-------*/
    @RequestMapping(path = REQUEST_MAPPING_TAMBAH_PEMOHON)
    public String doShowTambahPemohon(Model model, @RequestParam(value = "no", required = true) String no) {
        TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(no);
        /*TxTmOwner txTmOwner = permohonanService.selectOneOwnerByApplicationNo(txTmGeneral.getId());*/
        TxTmOwner txTmOwner = new TxTmOwner();
        List<TxTmOwnerDetail> txTmOwnerDetailList = new ArrayList<>();
		/*if (txTmOwner == null) {
			txTmOwner = new TxTmOwner();
		} else {
			txTmOwnerDetailList = permohonanService.selectAllOwnerByOwnerId(txTmOwner.getId());
		}*/
        txTmOwner.setTxTmGeneral(txTmGeneral);
        List<MCountry> mCountryList = countryService.findByStatusFlagTrue();
        List<MProvince> mProvinceList = provinceService.findByStatusFlagTrue();
        List<MCity> mCityList = cityService.findByStatusFlagTrue();

        ArrayList<String> lownerType = new ArrayList <>();

        // cari secara DISTINCT dari table TX_TM_OWNER, tampilkan apa adanya - F
        List<String> listownerType = nativeQueryRepository.selectDistinct("TX_TM_OWNER","TM_OWNER_TYPE");
        model.addAttribute("lownerType",listownerType);
        model.addAttribute("noGeneral", no);
        model.addAttribute("dataGeneral", txTmGeneral);
        model.addAttribute("pemohon", txTmOwner);
        model.addAttribute("listCountry", mCountryList);


        model.addAttribute("listProvince", mProvinceList);
        model.addAttribute("listCity", mCityList);
        model.addAttribute("listPemohonChild", txTmOwnerDetailList);

        return PATH_TAMBAH_PERMOHONAN_PAGE_PEMOHON;
    }
    /*------ End of Pemohon Section-------*/

    /*------License Section-------*/
    @RequestMapping(path = REQUEST_MAPPING_UBAH_LICENSE)
    public String doShowEditLisensi(Model model, @RequestParam(value = "id", required = true) String id, @RequestParam(value = "no", required = true) String no) {
        TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(no);
        TxLicense txLicense = licenseService.selectOneLicenseById(id);

        txLicense.setTxTmGeneral(txTmGeneral);
        List<MCountry> mCountryList = countryService.findAll();
        Collections.sort(mCountryList, (o1, o2) -> o1.getName().compareTo(o2.getName()));

        List<MProvince> mProvinceList = provinceService.findAll();

        List<MCity> mCityList = cityService.selectAll();
        Collections.sort(mCityList, (o1, o2) -> o1.getName().compareTo(o2.getName()));

        List<TxLicense> txLicenseList = licenseService.selectAllLicenseByIdGeneral(txTmGeneral.getId());
        
        model.addAttribute("noGeneral", no);
        model.addAttribute("dataGeneral", txTmGeneral);
        model.addAttribute("listCountry", mCountryList);
        model.addAttribute("txLicense", txLicense);
        model.addAttribute("listLicense", txLicenseList);
        model.addAttribute("listProvince", mProvinceList);
        model.addAttribute("listCity", mCityList);

        return PATH_UBAH_PERMOHONAN_PAGE_LICENSE;
    }
    /*------ End of License Section-------*/

    /*------Biaya Section-------*/

    @RequestMapping(path = REQUEST_MAPPING_UBAH_BIAYA)
    public String doShowUbahBiaya(Model model, @RequestParam(value = "no", required = true) String no) {
        TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(no);
        model.addAttribute("isEdit", true);


        TxTmMadridFee txTmMadridFee = txTmGeneral.getTxTmMadridFee();
        if (txTmMadridFee == null) {
            txTmMadridFee = new TxTmMadridFee();
        } else {
            txTmMadridFee.setTmpLanguage2(txTmGeneral.getLanguage2());
        }

        List<TxTmMadridFeeDetail> txTmMadridFeeDetails = txTmMadridFee.getTxTmMadridFeeDetails();

        if (txTmMadridFeeDetails == null) {
            txTmMadridFeeDetails = new ArrayList<>();
        }

        model.addAttribute("noGeneral", no);
        model.addAttribute("dataGeneral", txTmGeneral);
        model.addAttribute("txTmMadridFee", txTmMadridFee);
        model.addAttribute("txTmMadridFeeDetails", txTmMadridFeeDetails);

        Boolean ismadrid = true ;
        List<MCountry> MadridCountry = countryService.findByMadrid(ismadrid);
        model.addAttribute("listCountry", MadridCountry);

        List<MLookup> mLanguageList2 = lookupService.selectAllbyGroup("PilihanBahasa2");
        model.addAttribute("listLanguage2", mLanguageList2);


        return PATH_UBAH_PERMOHONAN_PAGE_BIAYA;
    }

    /*------ End of Biaya Section-------*/

    /*-----Tambah License Section-------*/
    @RequestMapping(path = REQUEST_MAPPING_TAMBAH_LICENSE)
    public String doShowTambahLisensi(Model model, @RequestParam(value = "no", required = true) String no) {
        TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(no);

        TxLicense txLicense = new TxLicense();
        txLicense.setLevel(0);
        txLicense.setTxTmGeneral(txTmGeneral);

        List<MCountry> mCountryList = countryService.findByStatusFlagTrue();
        List<MProvince> mProvinceList = provinceService.findByStatusFlagTrue();
        List<MCity> mCityList = cityService.findByStatusFlagTrue();
        List<TxLicense> txLicenseList = licenseService.selectAllLicenseByIdGeneral(txTmGeneral.getId());

        model.addAttribute("noGeneral", no);
        model.addAttribute("dataGeneral", txTmGeneral);
        model.addAttribute("txLicense", txLicense);
        model.addAttribute("listCountry", mCountryList);
        model.addAttribute("licenseList", txLicenseList);

        model.addAttribute("listProvince", mProvinceList);
        model.addAttribute("listCity", mCityList);

        return PATH_TAMBAH_LICENSE_PAGE_LICENSE;
    }
    /*------ End of Tambah License Section-------*/

}
