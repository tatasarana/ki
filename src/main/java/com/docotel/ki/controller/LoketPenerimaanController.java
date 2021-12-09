package com.docotel.ki.controller;

import com.docotel.ki.KiApplication;
import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.*;
import com.docotel.ki.model.transaction.TxReception;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.service.SimpakiService;
import com.docotel.ki.service.master.FileService;
import com.docotel.ki.service.transaction.PermohonanOnlineService;
import com.docotel.ki.service.transaction.TrademarkService;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.util.FieldValidationUtil;
import com.docotel.ki.util.NumberUtil;
import com.docotel.ki.util.ZxingUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.docotel.ki.model.master.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class LoketPenerimaanController extends BaseController {
	@Autowired
	private TrademarkService trademarkService;

	@Autowired
	private FileService fileService;
	
	@Autowired 
	private SimpakiService simpakiService;
	
	@Value("${logo.qr.pengayoman}")
	private String logoQRPengayoman;

	@Autowired
	private PermohonanOnlineService permohonanOnlineService;

	private static final String DIRECTORY_PAGE = "loket-penerimaan/";

	private static final String PAGE_CETAK_PENERIMAAN = DIRECTORY_PAGE + "cetak-penerimaan";
	private static final String PAGE_DETAIL_PENERIMAAN = DIRECTORY_PAGE + "detail-penerimaan";
	private static final String PAGE_LOKET_PENERIMAAN = DIRECTORY_PAGE + "loket-penerimaan";
	private static final String PAGE_TAMBAH_PENERIMAAN = DIRECTORY_PAGE + "tambah-penerimaan";

	public static final String PATH_AJAX_SEARCH_LIST = "/cari-penerimaan";
	public static final String PATH_AJAX_EXPORT_LIST = "/export-penerimaan";
	public static final String PATH_CETAK_PENERIMAAN = "/cetak-penerimaan";
	public static final String PATH_DETAIL_PENERIMAAN = "/detail-penerimaan";
	public static final String PATH_LOKET_PENERIMAAN = "/loket-penerimaan";
	public static final String PATH_TAMBAH_PENERIMAAN = "/tambah-penerimaan";
	private static final String PATH_CHECK_CODE_BILLING = "/check-code-billing-loket";

	public static final String REDIRECT_CETAK_PENERIMAAN = "redirect:" + PATH_AFTER_LOGIN + PATH_CETAK_PENERIMAAN;
	public static final String REDIRECT_LOKET_PENERIMAAN = "redirect:" + PATH_AFTER_LOGIN + PATH_LOKET_PENERIMAAN;

	private static final String REQUEST_MAPPING_AJAX_SEARCH_LIST = PATH_AJAX_SEARCH_LIST + "*";
	private static final String REQUEST_MAPPING_AJAX_EXPORT_LIST = PATH_AJAX_EXPORT_LIST + "*";

	private static final String REQUEST_MAPPING_CHECK_CODE_BILLING = PATH_CHECK_CODE_BILLING + "*";

	private static final String REQUEST_MAPPING_CETAK_PENERIMAAN = PATH_CETAK_PENERIMAAN + "*";
	private static final String REQUEST_MAPPING_DETAIL_PENERIMAAN = PATH_DETAIL_PENERIMAAN + "*";
	private static final String REQUEST_MAPPING_LOKET_PENERIMAAN = PATH_LOKET_PENERIMAAN + "*";
	private static final String REQUEST_MAPPING_TAMBAH_PENERIMAAN = PATH_TAMBAH_PENERIMAAN + "*";
//	private static final String REQUEST_MAPPING_HAPUS_PENERIMAAN = PATH_HAPUS_PENERIMAAN + "*";

	/***************************** - LOKET PENERIMAAN SECTION - ****************************/
	@GetMapping(path = REQUEST_MAPPING_LOKET_PENERIMAAN)
	public String doShowPageLoketPenerimaan(@RequestParam(value = "error", required = false) String error, final Model model, final HttpServletRequest request, final HttpServletResponse response) {
		model.addAttribute("subMenu", "permohonanBaru");

		if (StringUtils.isNotBlank(error)) {
			model.addAttribute("errorMessage", error);
		}

		return PAGE_LOKET_PENERIMAAN;
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

			searchCriteria.add(new KeyValue("onlineFlag", Boolean.FALSE, true));

			String orderBy = request.getParameter("order[0][column]");
			if (orderBy != null) {
				orderBy = orderBy.trim();
				if (orderBy.equalsIgnoreCase("")) {
					orderBy = null;
				} else {
					switch (orderBy) {
						case "1" :
							orderBy = "applicationNo";
							break;
						case "2" :
							orderBy = "applicationDate"; 
							break;
						case "3" :
							orderBy = "mFileSequence.desc";
							break;
						case "4" :
							orderBy = "mFileType.desc";
							break;
						case "5" :
							orderBy = "mFileTypeDetail.desc";
							break;					
						case "6" :
							orderBy = "bankCode";
							break;
						case "7" :
							orderBy = "totalClass";
							break;
						case "8" :
							orderBy = "paymentDate";
							break;	
						case "9" :
							orderBy = "totalPayment";
							break;	
							
						default :
							orderBy = "no";
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
			
			GenericSearchWrapper<TxReception> searchResult = trademarkService.searchReception(searchCriteria, orderBy, orderType, offset, limit);
			if (searchResult.getCount() > 0) {
				dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
				dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

				int no = offset;
				for (TxReception result : searchResult.getList()) {
					Date dateApplication = new Date(result.getApplicationDate().getTime());
					String sApplicationDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(dateApplication);
					
					Date datePayment = new Date(result.getPaymentDate().getTime());
					String sPaymentDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(datePayment);
					
					no++;
					data.add(new String[]{
							"" + no,
							"<a href=\"" + getPathURLAfterLogin(PATH_CETAK_PENERIMAAN + "?no=" + result.getApplicationNo()) + "\">" + result.getApplicationNo() + "</a>",
							//sApplicationDate,
							result.getApplicationDateTemp(),														
							result.getmFileSequence().getDesc(),
							result.getmFileType().getDesc(),
							result.getmFileTypeDetail()!=null?result.getmFileTypeDetail().getDesc():"",
							result.getBankCode(),
							result.getTotalClassTemp(),							
							//sPaymentDate, //
							result.getPaymentDateTemp(),
							"Rp. " + NumberUtil.formatInteger(result.getTotalPayment()),
							"<a class=\"btn btn-primary btn-xs\" href=\"" + getPathURLAfterLogin(PATH_DETAIL_PENERIMAAN + "?no=" + result.getApplicationNo()) + "\">Detail</a>",
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

			GenericSearchWrapper<TxReception> searchResult = trademarkService.searchReception(searchCriteria, orderBy, orderType, offset, limit);
			if (searchResult.getCount() > 0) {
				dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
				dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

				int no = offset;
				for (TxReception result : searchResult.getList()) {
					no++;
					data.add(new String[]{
							"" + no,
							"<a href=\"" + getPathURLAfterLogin(PATH_CETAK_PENERIMAAN + "?no=" + result.getApplicationNo()) + "\">" + result.getApplicationNo() + "</a>",
							result.getApplicationDateTemp(),
							result.getmFileSequence().getDesc(),
							result.getmFileTypeDetail().getDesc(),
							result.getBankCode(),
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
	/***************************** - END LOKET PENERIMAAN SECTION - ****************************/

	/***************************** - TAMBAH PENERIMAAN SECTION - ****************************/
	@GetMapping(REQUEST_MAPPING_TAMBAH_PENERIMAAN)
	public String doShowPageTambahPenerimaan(Model model) {
		model.addAttribute("subMenu", "permohonanBaru");

		TxReception form = new TxReception();
		form.setmFileType(new MFileType());
		form.setmFileSequence(new MFileSequence());
		form.setmFileTypeDetail(new MFileTypeDetail());
		model.addAttribute("form", form);

		return PAGE_TAMBAH_PENERIMAAN;
	}

	@PostMapping(REQUEST_MAPPING_TAMBAH_PENERIMAAN)
	public String doTambahPenerimaan(@ModelAttribute("form") TxReception form, final BindingResult errors, final Model model, final HttpServletRequest request, final HttpServletResponse response) throws ParseException {
		model.addAttribute("subMenu", "permohonanBaru");

		// validate form
		FieldValidationUtil.required(errors, "mFileSequence.id", form.getmFileSequence() == null ? null : form.getmFileSequence().getCurrentId(), "asal permohonan");

		FieldValidationUtil.required(errors, "mFileType.id", form.getmFileType() == null ? null : form.getmFileType().getCurrentId(), "jenis permohonan");

		FieldValidationUtil.required(errors, "mFileTypeDetail.id", form.getmFileTypeDetail() == null ? null : form.getmFileTypeDetail().getCurrentId(), "tipe permohonan");

		FieldValidationUtil.required(errors, "applicationDate", form.getApplicationDate(), "tanggal permohonan");

		if (FieldValidationUtil.required(errors, "totalPayment", form.getTotalPayment(), "jumlah pembayaran")) {
			FieldValidationUtil.minValue(errors, "totalPayment", form.getTotalPayment(), "jumlah pembayaran", BigDecimal.ZERO);
		}

		FieldValidationUtil.required(errors, "paymentDate", form.getPaymentDate(), "tanggal pembayaran");

		FieldValidationUtil.required(errors, "bankCode", form.getBankCode(), "kode bank");
		// validate form end

		if (!errors.hasErrors()) {
			form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
			try {
				trademarkService.insertReception(form);
				return REDIRECT_CETAK_PENERIMAAN + "?no=" + form.getApplicationNo();
			} catch (DataIntegrityViolationException e) {
				logger.error(e.getMessage(), e);
				if (e.getMessage().startsWith(HttpStatus.NOT_FOUND.getReasonPhrase())) {
					if (e.getMessage().endsWith("MFileSequence")) {
						errors.rejectValue("mFileSequence.id", "field.error.invalid.value", new Object[] {"asal permohonan"}, "");
					} else {
						errors.rejectValue("mFileType.id", "field.error.invalid.value", new Object[] {"jenis permohonan"}, "");
					}
				} else {
					model.addAttribute("errorMessage", "Gagal menambahkan permohonan penerimaan");
				}
			}
		}

		return PAGE_TAMBAH_PENERIMAAN;
	}
	/***************************** - END TAMBAH PENERIMAAN SECTION - ****************************/

	/***************************** - DETAIL PENERIMAAN SECTION - ****************************/
	@GetMapping(REQUEST_MAPPING_DETAIL_PENERIMAAN)
	public String doShowPageDetailPenerimaan(final Model model, final HttpServletRequest request) {
		if (doInitiateEditPenerimaan(model, request)) {
			return PAGE_DETAIL_PENERIMAAN;
		}

		return REDIRECT_LOKET_PENERIMAAN + "?error=Nomor permohonan " + request.getParameter("no") + " tidak ditemukan";
	}
	/***************************** - END TAMBAH PENERIMAAN SECTION - ****************************/

	/***************************** - CETAK PENERIMAAN SECTION - ****************************/
	@GetMapping(REQUEST_MAPPING_CETAK_PENERIMAAN)
	public String doShowPageCetakPenerimaan(@RequestParam(value = "no", required = false) String applicationNo, final Model model) {
		model.addAttribute("subMenu", "permohonanBaru");

		TxReception txReception = null;
		if (!StringUtils.isEmpty(applicationNo)) {
			txReception = trademarkService.selectOneReceptionByApplicationNo(applicationNo);
		}

		if (txReception == null) {
			return REDIRECT_LOKET_PENERIMAAN + "?error=Nomor permohonan " + applicationNo + " tidak ditemukan";
		}

		String qrText = txReception.getQrText();
		String qrCode = "data:image/png;base64, ";

		byte[] qrData = ZxingUtil.textToQrCode(qrText, new File(logoQRPengayoman), 125, 125, 30, 30);
		if (qrData != null) {
			qrCode += Base64.getEncoder().encodeToString(qrData);
		}

		model.addAttribute("qrText", qrText);
		model.addAttribute("qrCode", qrCode);

		return PAGE_CETAK_PENERIMAAN;
	}

	/***************************** - END CETAK PENERIMAAN SECTION - ****************************/

	@ModelAttribute
	public void addModelAttribute(final Model model, final HttpServletRequest request) {
		model.addAttribute("menu", "loketPenerimaan");
		List<KeyValue> searchCriteria = new ArrayList<>();
		searchCriteria.add(new KeyValue("menu", "DAFTAR", true));
		searchCriteria.add(new KeyValue("statusFlag", true, true));	
		
		if (!request.getRequestURI().contains(PATH_CETAK_PENERIMAAN)) {
			List<MFileTypeDetail> fileTypeDetailList = fileService.findMFileTypeDetailByStatusFlagTrue();
			model.addAttribute("fileTypeDetailList", fileTypeDetailList);

			List<MFileSequence> fileSequenceList = fileService.findMFileSequenceByStatusFlagTrue();
			model.addAttribute("fileSequenceList", fileSequenceList);

			List<MFileType> fileTypeList = fileService.selectAllMFileType(searchCriteria, "desc", "ASC", null, null);
			model.addAttribute("fileTypeList", fileTypeList);
			
			if (request.getRequestURI().contains(PATH_TAMBAH_PENERIMAAN)) {
				if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
					TxReception txReception = new TxReception();

					model.addAttribute("form", txReception);
				}
			}
		}
	}

	private boolean doInitiateEditPenerimaan(final Model model, final HttpServletRequest request) {
		model.addAttribute("subMenu", "permohonanBaru");

		TxReception existing = trademarkService.selectOneReceptionByApplicationNo(request.getParameter("no"));

		if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
			model.addAttribute("form", existing);
		} else {
			model.addAttribute("existing", existing);
		}

		return existing != null;
	}

	@RequestMapping(path = REQUEST_MAPPING_CHECK_CODE_BILLING, method = {RequestMethod.POST})
	@ResponseBody
	public void doCheckCodeBilling (final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) throws JsonProcessingException, IOException, ParseException {
		if (isAjaxRequest(request)) {
			setResponseAsJson(response);
			String statusError = null;
			String bankCode = request.getParameter("bankCode");
			TxReception txReception = permohonanOnlineService.selectOneTxReception("bankCode", bankCode);
			Map<String, String> dataGeneral = new HashMap<>();

			if (txReception != null) {
				statusError = "Kode Billing '" + bankCode + "' Sudah Digunakan";
			} else {
				try {
					String result = simpakiService.getQueryBilling(bankCode);
					ObjectMapper mapper = new ObjectMapper();
					JsonNode rootNode = mapper.readTree(result);

					String code = rootNode.get("code").toString().replaceAll("\"", "");
					String message = rootNode.get("message").toString().replaceAll("\"", "");
					if (code.equals("00")) {
						String data = rootNode.get("data").toString();
						JsonNode dataNode = mapper.readTree(data);

						String flagPayment = dataNode.get("flag_pembayaran").toString().replaceAll("\"", "");
						String flagUsed = dataNode.get("terpakai").toString().replaceAll("\"", "");

						if (flagPayment.equalsIgnoreCase("BELUM")) {
							statusError = "Kode Billing '" + bankCode + "' Belum Dibayar";
						} else if (!flagUsed.equalsIgnoreCase("BELUM")) {
							statusError = "Kode Billing '" + bankCode + "' Sudah Digunakan";
						} else {
							String paymentDate = dataNode.get("tgl_pembayaran").toString().replaceAll("\"", "");
							String currentDate = DateUtil.formatDate(new Timestamp(System.currentTimeMillis()), "yyyy-MM-dd HH:mm:ss");
							DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
							LocalDateTime locPaymentDate = LocalDateTime.parse(paymentDate, format);
							LocalDateTime locCurrentDate = LocalDateTime.parse(currentDate, format);
							Duration duration = Duration.between(locPaymentDate, locCurrentDate);
							String feeCode = dataNode.get("kd_tarif").toString().replaceAll("\"", "");
							MPnbpFeeCode mPnbpFeeCode = simpakiService.getPnbpFeeCodeByCode(feeCode);

						   if (mPnbpFeeCode == null) {
								statusError = "Kode Tarif '" + feeCode + "' Tidak Ditemukan";
							} else {
								MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

								String totalClass = dataNode.get("volume").toString().replaceAll("\"", "");
								String totalPayment = dataNode.get("total_pembayaran").toString().replaceAll("\"", "");
								String ownerName = dataNode.get("nama").toString().replaceAll("\"", "");
								String ownerEmail = dataNode.get("email").toString().replaceAll("\"", "");
								String ownerPhone = dataNode.get("no_tlp").toString().replaceAll("\"", "");
								String ownerProvince = dataNode.get("kd_provinsi").toString().replaceAll("\"", "");
								String ownerCity = dataNode.get("kd_kabupaten").toString().replaceAll("\"", "");
								String ownerRegion = dataNode.get("kd_kecamatan").toString().replaceAll("\"", "");
								String ownerAddress = dataNode.get("alamat").toString().replaceAll("\"", "");
								String ownerFlag = dataNode.get("flag_warga").toString().replaceAll("\"", "");

								dataGeneral.put("mFileSequence", mUser.getmFileSequence().getId());
								dataGeneral.put("mFileType", null);
								dataGeneral.put("mFileTypeDetail", mPnbpFeeCode.getmFileTypeDetail().getId());
								dataGeneral.put("totalClass", totalClass);
								dataGeneral.put("totalPayment", totalPayment);
								dataGeneral.put("totalPaymentTemp", NumberUtil.formatDecimal(NumberUtil.parseDecimal(totalPayment)));
								dataGeneral.put("paymentDate", DateUtil.formatDate(DateUtil.toDate("yyyy-MM-dd HH:mm:ss", paymentDate), "dd/MM/yyyy HH:mm:ss"));
								dataGeneral.put("applicationDate", DateUtil.formatDate(new Timestamp(System.currentTimeMillis()), "dd/MM/yyyy HH:mm:ss"));
								dataGeneral.put("ownerName", ownerName);
								dataGeneral.put("ownerEmail", ownerEmail);
								dataGeneral.put("ownerPhone", ownerPhone);

								if (ownerFlag.equalsIgnoreCase("WNI")) {
									MCountry mCountry = permohonanOnlineService.selectOneCountryByCode("ID");
									MProvince mProvince = permohonanOnlineService.selectOneProvinceByCode(ownerProvince);
									MCity mCity = permohonanOnlineService.selectOneCityByCode(ownerCity);
									
									dataGeneral.put("ownerCountry", mCountry == null ? null : mCountry.getCurrentId());
									dataGeneral.put("ownerProvince", mProvince == null ? null : mProvince.getCurrentId());
									dataGeneral.put("ownerCity", mCity == null ? null : mCity.getCurrentId());									
									dataGeneral.put("ownerAddress", ownerAddress);
								} else {
									dataGeneral.put("ownerCountry", KiApplication.INDONESIA.getCurrentId());
									dataGeneral.put("ownerProvince", null);
									dataGeneral.put("ownerCity", null);
									dataGeneral.put("ownerRegion", null);
									dataGeneral.put("ownerAddress", null);
								}
							}
						}
					} else {
						statusError = code.equals("02") ? "Kode Billing '" + bankCode + "' Tidak Ditemukan" : message;
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					statusError = "Gagal melakukan pengecekan Kode Billing '" + bankCode + "' ke Aplikasi Simpaki";
				}
			}

			dataGeneral.put("statusError", statusError);

			writeJsonResponse(response, dataGeneral);
		} else {
			response.setStatus(HttpServletResponse.SC_FOUND);
		}
	}
}