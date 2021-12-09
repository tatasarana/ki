package com.docotel.ki.controller;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.enumeration.RegistrasiStatusEnum;
import com.docotel.ki.enumeration.StatusEnum;
import com.docotel.ki.model.master.MBrandType;
import com.docotel.ki.model.master.MCity;
import com.docotel.ki.model.master.MClass;
import com.docotel.ki.model.master.MCountry;
import com.docotel.ki.model.master.MFileType;
import com.docotel.ki.model.master.MFileTypeDetail;
import com.docotel.ki.model.master.MProvince;
import com.docotel.ki.model.master.MStatus;
import com.docotel.ki.model.master.MLookup;
import com.docotel.ki.model.transaction.TxPostDoc;
import com.docotel.ki.model.transaction.TxTmDoc;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.pojo.DashboardPascaPermohonanRepository;
import com.docotel.ki.repository.custom.pojo.DashboardPermohonanRepository;
import com.docotel.ki.repository.custom.pojo.DashboardRegAkunRepository;
import com.docotel.ki.repository.custom.pojo.DashboardTop10Repository;
import com.docotel.ki.repository.custom.transaction.TxPostDocCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxPostReceptionDetailCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmDocCustomRepository;
import com.docotel.ki.repository.transaction.TxPostReceptionDetailRepository;
import com.docotel.ki.service.master.BrandService;
import com.docotel.ki.service.master.CityService;
import com.docotel.ki.service.master.ClassService;
import com.docotel.ki.service.master.CountryService;
import com.docotel.ki.service.master.FileService;
import com.docotel.ki.service.master.ProvinceService;
import com.docotel.ki.service.master.StatusService;
import com.docotel.ki.service.master.LookupService;
import com.docotel.ki.service.transaction.DokumenLampiranService;
import com.docotel.ki.service.transaction.PascaOnlineService;
import com.docotel.ki.service.transaction.RegistrasiOnlineService;
import com.docotel.ki.service.transaction.TrademarkService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class DashboardController extends BaseController {

    @Autowired
    TrademarkService trademarkService;
    @Autowired
    RegistrasiOnlineService regOnlineService;
    @Autowired
    TxPostReceptionDetailCustomRepository txPostReceptionDetailCustomRepository;
    @Autowired
    TxPostReceptionDetailRepository txPostReceptionDetailRepository;
    @Autowired
    PascaOnlineService pascaOnlineService;
    @Autowired
    TxTmDocCustomRepository txTmDocCustomRepository;
    @Autowired
    TxPostDocCustomRepository txPostDocCustomRepository;
    @Autowired
    DokumenLampiranService dokumenLampiranService;
    @Autowired
    private FileService fileService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private ClassService classService;
    @Autowired
    private CountryService countryService;
    @Autowired
    private ProvinceService provinceService;
    @Autowired
    private CityService cityService;
    @Autowired
    private StatusService statusService;
    @Autowired
    private LookupService lookupService;
    @Autowired
    private DashboardPermohonanRepository permohonanRepository;
    @Autowired
    private DashboardPascaPermohonanRepository pascaPermohonanRepository;
    @Autowired
    private DashboardTop10Repository top10Repository;
    @Autowired
    private DashboardRegAkunRepository regAkunRepository;

	private static final String PAGE_DASHBOARD = "dashboard";
	public static final String PATH_DASHBOARD = "/dashboard";
	private static final String PATH_AJAX_LIST_NEW_DOKUMEN = "/cari-new-dokumen";
    private static final String PATH_AJAX_LIST_NEW_DOKUMEN_PASCA = "/cari-new-dokumen-pasca";

    public static final String REQUEST_MAPPING_DASHBOARD = PATH_DASHBOARD + "*";
	public static final String REQUEST_MAPPING_AJAX_LIST_NEW_DOKUMEN = PATH_AJAX_LIST_NEW_DOKUMEN + "*";
    public static final String REQUEST_MAPPING_AJAX_LIST_NEW_DOKUMEN_PASCA = PATH_AJAX_LIST_NEW_DOKUMEN_PASCA + "*";

    //dashboard permohonan
    private static final String PAGE_DASHBOARD_PERMOHONAN = "dashboard-permohonan";
    public static final String PATH_DASHBOARD_PERMOHONAN = "/dashboard-permohonan";
    public static final String REQUEST_MAPPING_DASHBOARD_PERMOHONAN = PATH_DASHBOARD_PERMOHONAN + "*";
    public static final String PATH_AJAX_DASHBOARD_PERMOHONAN = "/ajax-dashboard-permohonan*";
    private static final String REQUEST_MAPPING_EXPORT_DASHBOARD_PERMOHONAN = "/cetak-dashboard-permohonan*";

    //dashboard pasca permohonan
    private static final String PAGE_DASHBOARD_PASCA_PERMOHONAN = "dashboard-pasca-permohonan";
    public static final String PATH_DASHBOARD_PASCA_PERMOHONAN = "/dashboard-pasca-permohonan";
    public static final String REQUEST_MAPPING_DASHBOARD_PASCA_PERMOHONAN = PATH_DASHBOARD_PASCA_PERMOHONAN + "*";
    public static final String PATH_AJAX_DASHBOARD_PASCA_PERMOHONAN = "/ajax-dashboard-pasca*";
    private static final String REQUEST_MAPPING_EXPORT_DASHBOARD_PASCA_PERMOHONAN = "/cetak-dashboard-pasca-permohonan*";

    //dashboard pemohon dan kuasa
    private static final String PAGE_DASHBOARD_KUASA = "dashboard-pemohon-kuasa";
    public static final String PATH_DASHBOARD_KUASA = "/dashboard-pemohon-kuasa";
    public static final String REQUEST_MAPPING_DASHBOARD_KUASA = PATH_DASHBOARD_KUASA + "*";
    public static final String PATH_AJAX_DASHBOARD_KUASA = "/dashboard-top-reload-table*";
    private static final String REQUEST_MAPPING_EXPORT_DASHBOARD_KUASA = "/cetak-dashboard-pemohon-kuasa*";

    //dashboard reg akun
    private static final String PAGE_DASHBOARD_REG = "dashboard-reg-akun";
    public static final String PATH_DASHBOARD_REG = "/dashboard-reg-akun";
    public static final String REQUEST_MAPPING_DASHBOARD_REG = PATH_DASHBOARD_REG + "*";
    public static final String PATH_AJAX_DASHBOARD_REG = "/ajax-dashboard-reg-akun*";
    private static final String REQUEST_MAPPING_EXPORT_DASHBOARD_REG = "/cetak-dashboard-reg-akun*";

    @GetMapping(path = REQUEST_MAPPING_DASHBOARD)
    public String doShowPageDashboard(final Model model, final HttpServletRequest request, final HttpServletResponse response) {
    	long countPO = trademarkService.count("mStatus", StatusEnum.IPT_PENGAJUAN_PERMOHONAN.value(), true);
        model.addAttribute("dataPO",countPO);

        long countRO = regOnlineService.count("approvalStatus",RegistrasiStatusEnum.PREPARE.name(), true);
        model.addAttribute("dataRO",countRO);

        long countPASCA = trademarkService.countPascaDetail();
        model.addAttribute("dataPASCA",countPASCA);

        model.addAttribute("txPostReceptionDetail", txPostReceptionDetailCustomRepository.selectAllPendingPostReceptionDetail());

        //model.addAttribute("txTmDoc", txTmDocCustomRepository.selectAllNewAddDokumen());

        model.addAttribute("txPostDoc", txPostDocCustomRepository.selectAllNewAddPostDokumen());

        model.addAttribute("menu", "dashboard");

        return PAGE_DASHBOARD;
    }

    @PostMapping(value = REQUEST_MAPPING_AJAX_LIST_NEW_DOKUMEN)
    public void doGetListDataTablesNewDoc(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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

            List<KeyValue> searchCriteria = new ArrayList<>();
            searchCriteria.add(new KeyValue("status", false, true));

            String orderBy = "createdDate";
            String orderType = "DESC";

            List<String[]> data = new ArrayList<>();

            GenericSearchWrapper<TxTmDoc> searchResult = dokumenLampiranService.searchNewDokumen(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxTmDoc result : searchResult.getList()) {
                    no++;
                    data.add(new String[]{
                            "" + no,
                            "<a target=\"_blank\" href=\"" + getPathURLAfterLogin("/pratinjau-permohonan" + "?no=" + result.getTxTmGeneral().getApplicationNo()) + "\">" + result.getTxTmGeneral().getApplicationNo() + "</a>",
                            result.getmDocType().getName()
                    });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @PostMapping(value = REQUEST_MAPPING_AJAX_LIST_NEW_DOKUMEN_PASCA)
    public void doGetListDataTablesNewDocPasca(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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

            List<KeyValue> searchCriteria = new ArrayList<>();
            searchCriteria.add(new KeyValue("status", false, true));

            String orderBy = "createdDate";
            String orderType = "DESC";

            List<String[]> data = new ArrayList<>();

            GenericSearchWrapper<TxPostDoc> searchResult = dokumenLampiranService.searchNewDokumenPasca(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxPostDoc result : searchResult.getList()) {
                    no++;
                    data.add(new String[]{
                            "" + no,
                            "<a target=\"_blank\" href=\"" + getPathURLAfterLogin("/pratinjau-pasca-permohonan" + "?no=" + result.getTxPostReception().getId()) + "\">" + result.getTxPostReception().getPostNo() + "</a>",
                            result.getmDocType().getName()
                    });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @GetMapping(path = REQUEST_MAPPING_DASHBOARD_PERMOHONAN)
    public String doShowPageDashboardPermohonan(final Model model, final HttpServletRequest request, final HttpServletResponse response) {

    	List<MFileType> fileTypeList = fileService.findMFileTypeByFileTypeMenu();
        Collections.sort(fileTypeList, (o1, o2) -> o1.getCode().compareTo(o2.getCode()));
        model.addAttribute("fileTypeList", fileTypeList);

        List<MFileTypeDetail> fileTypeDetailList = fileService.findMFileTypeDetailByStatusFlagTrue();
        model.addAttribute("fileTypeDetailList", fileTypeDetailList);

        List<MBrandType> listMBrandType = brandService.findByStatusFlagTrue();
        model.addAttribute("listBrandType", listMBrandType);

        List<MStatus> statusList = statusService.selectStatus();
        model.addAttribute("statusList", statusList);

        List<MCountry> mCountryList = countryService.findAll();
        model.addAttribute("listCountry", mCountryList);

        List<MProvince> mProvinceList = provinceService.findAll();
        model.addAttribute("listProvince", mProvinceList);

        List<MCity> mCityList = cityService.selectAll();
        model.addAttribute("listCity", mCityList);

        List<MClass> classList = classService.findAllMClass();
        model.addAttribute("classList", classList);

        model.addAttribute("menu", "dashboardStatistic");
        model.addAttribute("subMenu", "dashboardPermohonan");

        return PAGE_DASHBOARD_PERMOHONAN;
    }

    @PostMapping(path = PATH_AJAX_DASHBOARD_PERMOHONAN)
	public void ajax4(
        @RequestParam("data") String data,
        final HttpServletRequest request,
        final HttpServletResponse response,
        final HttpSession session
    ) throws JsonProcessingException, IOException {

    	ObjectMapper mapper = new ObjectMapper();
    	Map<String, Object> requestMap = mapper.readValue(data, new TypeReference<Map<String,Object>>(){});

    	Map<String, Object> result = new HashMap<String, Object>();
    	List<Map<String, Object>> pemohonMadrid = permohonanRepository.selectPemohon(true, false, requestMap);
        List<Map<String, Object>> pemohonAsing = permohonanRepository.selectPemohon(false, true, requestMap);
        List<Map<String, Object>> pemohonIndonesia = permohonanRepository.selectPemohonIndonesia(requestMap);
        List<Map<String, Object>> pemohonStatus = permohonanRepository.selectStatus(requestMap);
        List<Map<String, Object>> tipePermohonan = permohonanRepository.selectTipePermohonan(requestMap);

        Map<String, Object> grafikTipe = permohonanRepository.selectGrafikTipePermohonan(requestMap);
        List<Map<String, Object>> grafikJenis = permohonanRepository.selectGrafikJenisPermohonan(requestMap);
        List<Map<String, Object>> grafikTipeMerek = permohonanRepository.selectGrafikTipeMerek(requestMap);
        Map<String, Object> grafikKelas = permohonanRepository.selectGrafikKelas(requestMap);
        Map<String, Object> grafikAsal = permohonanRepository.selectAsal(requestMap);

    	result.put("pemohonMadrid", pemohonMadrid);
    	result.put("pemohonAsing", pemohonAsing);
    	result.put("pemohonIndonesia", pemohonIndonesia);
    	result.put("pemohonStatus", pemohonStatus);
    	result.put("tipePermohonan", tipePermohonan);

    	result.put("grafikTipe", grafikTipe);
    	result.put("grafikJenis", grafikJenis);
    	result.put("grafikTipeMerek", grafikTipeMerek);
    	result.put("grafikKelas", grafikKelas);
    	result.put("grafikAsal", grafikAsal);

        writeJsonResponse(response, result);
    }

    @PostMapping(path = REQUEST_MAPPING_EXPORT_DASHBOARD_PERMOHONAN)
    public void doExportDashboardPermohononan(
        @RequestParam("data") String data,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws JsonProcessingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
    	Map<String, Object> requestMap = mapper.readValue(data, new TypeReference<Map<String,Object>>(){});

    	List<Map<String, Object>> pemohonMadrid = permohonanRepository.selectPemohon(true, false, requestMap);
        List<Map<String, Object>> pemohonAsing = permohonanRepository.selectPemohon(false, true, requestMap);
        List<Map<String, Object>> pemohonIndonesia = permohonanRepository.selectPemohonIndonesia(requestMap);
        List<Map<String, Object>> pemohonStatus = permohonanRepository.selectStatus(requestMap);
        List<Map<String, Object>> tipePermohonan = permohonanRepository.selectTipePermohonan(requestMap);

        Map<String, Object> grafikTipe = permohonanRepository.selectGrafikTipePermohonan(requestMap);
        List<Map<String, Object>> grafikJenis = permohonanRepository.selectGrafikJenisPermohonan(requestMap);
        List<Map<String, Object>> grafikTipeMerek = permohonanRepository.selectGrafikTipeMerek(requestMap);
        Map<String, Object> grafikKelas = permohonanRepository.selectGrafikKelas(requestMap);
        Map<String, Object> grafikAsal = permohonanRepository.selectAsal(requestMap);

        InputStream reportInputStream = null;
        Context context = new Context();
        context.putVar("pemohonMadrid", pemohonMadrid);
        context.putVar("pemohonAsing", pemohonAsing);
        context.putVar("pemohonIndonesia", pemohonIndonesia);
        context.putVar("pemohonStatus", pemohonStatus);
        context.putVar("tipePermohonan", tipePermohonan);

        context.putVar("grafikTipe", grafikTipe);
        context.putVar("grafikJenis", grafikJenis);
        context.putVar("grafikTipeMerek", grafikTipeMerek);
        context.putVar("grafikKelas", grafikKelas);
        context.putVar("grafikAsal", grafikAsal);

        try {
            reportInputStream = getClass().getClassLoader().getResourceAsStream("report/dashboard-permohonan.xls");
            JxlsHelper.getInstance().processTemplate(reportInputStream, response.getOutputStream(), context);
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=list-permohonan.xls");
            response.getOutputStream().close();
            response.flushBuffer();
        } catch (Exception ex) {
            logger.error(ex);
        } finally {
            if (reportInputStream != null) {
                try {
                    reportInputStream.close();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }
    }

    @GetMapping(path = REQUEST_MAPPING_DASHBOARD_PASCA_PERMOHONAN)
    public String doShowPageDashboardPascaPermohonan(final Model model, final HttpServletRequest request, final HttpServletResponse response) {

    	List<MFileType> fileTypeList = fileService.findByMenu("PASCA");
        model.addAttribute("fileTypeList", fileTypeList);

        model.addAttribute("menu", "dashboardStatistic");
        model.addAttribute("subMenu", "dashboardPascaPermohonan");

    	return PAGE_DASHBOARD_PASCA_PERMOHONAN;
    }

    @PostMapping(path = PATH_AJAX_DASHBOARD_PASCA_PERMOHONAN)
	public void ajax3(
        @RequestParam("data") String data,
        final HttpServletRequest request,
		final HttpServletResponse response,
        final HttpSession session
    ) throws JsonProcessingException, IOException {

    	ObjectMapper mapper = new ObjectMapper();
    	Map<String, Object> requestMap = mapper.readValue(data, new TypeReference<Map<String,Object>>(){});

    	Map<String, Object> result = new HashMap<String, Object>();
    	Map<String, Object> grafikTipePascaPermohonan = pascaPermohonanRepository.selectGrafikTipePermohonanPasca(requestMap);
    	List<Map<String, Object>> grafikStatus = pascaPermohonanRepository.selectGrafikStatusPasca(requestMap);
        List<Map<String, Object>> tipePermohonan = pascaPermohonanRepository.selectTipePascaPermohonan(requestMap);

    	result.put("grafikTipe", grafikTipePascaPermohonan);
    	result.put("grafikStatus", grafikStatus);
    	result.put("tipePermohonan", tipePermohonan);

        writeJsonResponse(response, result);
    }

    @PostMapping(path = REQUEST_MAPPING_EXPORT_DASHBOARD_PASCA_PERMOHONAN)
    public void doExportDashboardPascaPermohonan(
        @RequestParam("data") String data,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws JsonProcessingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
    	Map<String, Object> requestMap = mapper.readValue(data, new TypeReference<Map<String,Object>>(){});

    	Map<String, Object> grafikTipePascaPermohonan = pascaPermohonanRepository.selectGrafikTipePermohonanPasca(requestMap);
    	List<Map<String, Object>> grafikStatus = pascaPermohonanRepository.selectGrafikStatusPasca(requestMap);
        List<Map<String, Object>> tipePermohonan = pascaPermohonanRepository.selectTipePascaPermohonan(requestMap);

        InputStream reportInputStream = null;
        Context context = new Context();
        context.putVar("grafikTipePascaPermohonan", grafikTipePascaPermohonan);
        context.putVar("grafikStatus", grafikStatus);
        context.putVar("tipePermohonan", tipePermohonan);

        try {
            reportInputStream = getClass().getClassLoader().getResourceAsStream("report/dashboard-pasca-permohonan.xls");
            JxlsHelper.getInstance().processTemplate(reportInputStream, response.getOutputStream(), context);
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=list-permohonan.xls");
            response.getOutputStream().close();
            response.flushBuffer();
        } catch (Exception ex) {
            logger.error(ex);
        } finally {
            if (reportInputStream != null) {
                try {
                    reportInputStream.close();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }
    }

    @GetMapping(path = REQUEST_MAPPING_DASHBOARD_KUASA)
    public String doShowPageDashboardPemohonKuasa(final Model model, final HttpServletRequest request, final HttpServletResponse response) {

    	List<MFileType> fileTypeList = fileService.findMFileTypeByFileTypeMenu();
        Collections.sort(fileTypeList, (o1, o2) -> o1.getCode().compareTo(o2.getCode()));
        model.addAttribute("fileTypeList", fileTypeList);

        List<MFileTypeDetail> fileTypeDetailList = fileService.findMFileTypeDetailByStatusFlagTrue();
        model.addAttribute("fileTypeDetailList", fileTypeDetailList);

        List<MBrandType> listMBrandType = brandService.findByStatusFlagTrue();
        model.addAttribute("listBrandType", listMBrandType);

        List<MStatus> statusList = statusService.selectStatus();
        model.addAttribute("statusList", statusList);

        List<MCountry> mCountryList = countryService.findAll();
        model.addAttribute("listCountry", mCountryList);

        List<MProvince> mProvinceList = provinceService.findAll();
        model.addAttribute("listProvince", mProvinceList);

        List<MCity> mCityList = cityService.selectAll();
        model.addAttribute("listCity", mCityList);

        List<MClass> classList = classService.findAllMClass();
        Collections.sort(classList, (o1, o2) -> o1.getNo().compareTo(o2.getNo()));
        model.addAttribute("classList", classList);

        model.addAttribute("menu", "dashboardStatistic");
        model.addAttribute("subMenu", "dashboardTop10");

    	return PAGE_DASHBOARD_KUASA;
    }

    @PostMapping(path = PATH_AJAX_DASHBOARD_KUASA)
	public void ajax2(
        @RequestParam("data") String data,
        final HttpServletRequest request,
        final HttpServletResponse response,
        final HttpSession session
    ) throws JsonProcessingException, IOException {

    	ObjectMapper mapper = new ObjectMapper();
    	Map<String, Object> requestMap = mapper.readValue(data, new TypeReference<Map<String,Object>>(){});

    	Map<String, Object> result = new HashMap<String, Object>();
    	List<Map<String, Object>> topPemohon = top10Repository.selectTopPemohon(requestMap);
        List<Map<String, Object>> topKuasa = top10Repository.selectTopKuasa(requestMap);

    	result.put("topPemohon", topPemohon);
    	result.put("topKuasa", topKuasa);

        writeJsonResponse(response, result);
    }

    @PostMapping(path = REQUEST_MAPPING_EXPORT_DASHBOARD_KUASA)
    public void doExportDashboardKuasa(
        @RequestParam("data") String data,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws JsonProcessingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
    	Map<String, Object> requestMap = mapper.readValue(data, new TypeReference<Map<String,Object>>(){});
    	List<Map<String, Object>> topPemohon = top10Repository.selectTopPemohon(requestMap);
        List<Map<String, Object>> topKuasa = top10Repository.selectTopKuasa(requestMap);
        InputStream reportInputStream = null;
        Context context = new Context();
        context.putVar("topPemohon", topPemohon);
        context.putVar("topKuasa", topKuasa);
        try {
            reportInputStream = getClass().getClassLoader().getResourceAsStream("report/dashboard-pemohon-kuasa.xls");
            JxlsHelper.getInstance().processTemplate(reportInputStream, response.getOutputStream(), context);
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=list-permohonan.xls");
            response.getOutputStream().close();
            response.flushBuffer();
        } catch (Exception ex) {
            logger.error(ex);
        } finally {
            if (reportInputStream != null) {
                try {
                    reportInputStream.close();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }
    }

    @GetMapping(path = REQUEST_MAPPING_DASHBOARD_REG)
    public String doShowPageDashboardRegAkun(final Model model, final HttpServletRequest request, final HttpServletResponse response) {

    	List<MLookup> lookupList = lookupService.selectAllbyGroup("JenisPemohon");
        model.addAttribute("lookupList", lookupList);

    	model.addAttribute("menu", "dashboardStatistic");
        model.addAttribute("subMenu", "dashboardReg");

        return PAGE_DASHBOARD_REG;
    }

    @PostMapping(path = PATH_AJAX_DASHBOARD_REG)
	public void ajax(
        @RequestParam("data") String data,
        final HttpServletRequest request,
		final HttpServletResponse response,
        final HttpSession session
    ) throws JsonProcessingException, IOException {

    	ObjectMapper mapper = new ObjectMapper();
    	Map<String, Object> requestMap = mapper.readValue(data, new TypeReference<Map<String,Object>>(){});

    	Map<String, Object> result = new HashMap<String, Object>();
    	Map<String, Object> grafikRegistrasi = regAkunRepository.selectGrafikTipePermohonanDaftar(requestMap);
    	List<Map<String, Object>> grafikStatus = regAkunRepository.selectGrafikStatus(requestMap);
    	List<Map<String, Object>> regAkun = regAkunRepository.selectRegAkun(requestMap);

    	result.put("grafikRegistrasi", grafikRegistrasi);
    	result.put("grafikStatus", grafikStatus);
    	result.put("regAkun", regAkun);

        writeJsonResponse(response, result);
    }

    @PostMapping(path = REQUEST_MAPPING_EXPORT_DASHBOARD_REG)
    public void doExportDashboardRegAkun(
        @RequestParam("data") String data,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws JsonProcessingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
    	Map<String, Object> requestMap = mapper.readValue(data, new TypeReference<Map<String,Object>>(){});

    	Map<String, Object> grafikRegistrasi = regAkunRepository.selectGrafikTipePermohonanDaftar(requestMap);
    	List<Map<String, Object>> grafikStatus = regAkunRepository.selectGrafikStatus(requestMap);
    	List<Map<String, Object>> jenisRegs = regAkunRepository.selectRegAkun(requestMap);

        InputStream reportInputStream = null;
        Context context = new Context();
        context.putVar("grafikRegistrasi", grafikRegistrasi);
        context.putVar("grafikStatus", grafikStatus);
        context.putVar("jenisRegs", jenisRegs);

        try {
            reportInputStream = getClass().getClassLoader().getResourceAsStream("report/dashboard-reg-akun.xls");
            JxlsHelper.getInstance().processTemplate(reportInputStream, response.getOutputStream(), context);
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=list-permohonan.xls");
            response.getOutputStream().close();
            response.flushBuffer();
        } catch (Exception ex) {
            logger.error(ex);
        } finally {
            if (reportInputStream != null) {
                try {
                    reportInputStream.close();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }
    }
}
