package com.docotel.ki.controller;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.*;
import com.docotel.ki.model.transaction.TxPostReception;
import com.docotel.ki.model.transaction.TxPostReceptionDetail;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.pojo.PostReception;
import com.docotel.ki.service.SimpakiService;
import com.docotel.ki.service.master.FileService;
import com.docotel.ki.service.master.UserService;
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
import com.docotel.ki.repository.custom.transaction.TxPostReceptionCustomRepository;
import com.docotel.ki.repository.master.MFileTypeRepository;
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
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class LoketDokumenController extends BaseController {
	@Autowired private TrademarkService trademarkService;
	@Autowired private FileService fileService;
	@Autowired private TxPostReceptionCustomRepository txPostReceptionCustomRepository;
	@Autowired private SimpakiService simpakiService;
	@Autowired private MFileTypeRepository mFileTypeRepository;
	@Autowired private UserService userService;
	@Autowired private PermohonanOnlineService permohonanOnlineService;

	
	@Value("${logo.qr.pengayoman}")
	private String logoQRPengayoman;

	
	private static final String DIRECTORY_PAGE = "loket-dokumen/";

	// dokumen baru
	private static final String PAGE_CETAK_DOKUMEN = DIRECTORY_PAGE + "cetak-dokumen-baru";
	private static final String PAGE_EDIT_DOKUMEN = DIRECTORY_PAGE + "edit-dokumen-baru";
	private static final String PAGE_LOKET_DOKUMEN = DIRECTORY_PAGE + "loket-dokumen";
	private static final String PAGE_TAMBAH_DOKUMEN = DIRECTORY_PAGE + "tambah-dokumen-baru";

	//dokumen baru
	public static final String PATH_AJAX_SEARCH_LIST_DOKUMEN = "/cari-dokumen-baru";
	public static final String PATH_AJAX_EXPORT_LIST_DOKUMEN = "/export-dokumen-baru";
	public static final String PATH_CETAK_DOKUMEN = "/cetak-dokumen-baru";
	public static final String PATH_EDIT_DOKUMEN = "/edit-dokumen-baru";
	public static final String PATH_LOKET_DOKUMEN = "/loket-dokumen";
	public static final String PATH_TAMBAH_DOKUMEN = "/tambah-dokumen-baru";
	public static final String PATH_SIMPAN_DOKUMEN = "/simpan-dokumen";


	//	public static final String PATH_HAPUS_PENERIMAAN = "/hapus-penerimaan";
	private static final String PATH_CHECK_CODE_BILLING_DOKUMEN = "/check-code-billing-loket-dokumen";

	//dokumen baru
	public static final String REDIRECT_CETAK_DOKUMEN = "redirect:" + PATH_AFTER_LOGIN + PATH_CETAK_DOKUMEN;
	public static final String REDIRECT_LOKET_DOKUMEN = "redirect:" + PATH_AFTER_LOGIN + PATH_LOKET_DOKUMEN;

	//dokumen baru
	private static final String REQUEST_MAPPING_AJAX_SEARCH_LIST_DOKUMEN = PATH_AJAX_SEARCH_LIST_DOKUMEN + "*";
	private static final String REQUEST_MAPPING_AJAX_EXPORT_LIST_DOKUMEN = PATH_AJAX_EXPORT_LIST_DOKUMEN + "*";

	//dokumen baru
	private static final String REQUEST_MAPPING_CHECK_CODE_BILLING_DOKUMEN = PATH_CHECK_CODE_BILLING_DOKUMEN + "*";

	//dokumen baru
	private static final String REQUEST_MAPPING_CETAK_DOKUMEN = PATH_CETAK_DOKUMEN + "*";
	private static final String REQUEST_MAPPING_EDIT_DOKUMEN = PATH_EDIT_DOKUMEN + "*";
	private static final String REQUEST_MAPPING_LOKET_DOKUMEN = PATH_LOKET_DOKUMEN + "*";
	private static final String REQUEST_MAPPING_TAMBAH_DOKUMEN = PATH_TAMBAH_DOKUMEN + "*";

	private static final String REQUEST_MAPPING_SIMPAN_DOKUMEN = PATH_SIMPAN_DOKUMEN + "*";

	public static final String PATH_AJAX_LIST_REGISTRATION = "/list-registration-permohonan";
	private static final String REQUEST_MAPPING_AJAX_REGISTRATION_LIST = PATH_AJAX_LIST_REGISTRATION + "*";

	public static final String PATH_REGISTRATION_DOKUMEN = "/registrasi-dokumen";
	private static final String REQUEST_MAPPING_REGISTRATION_DOKUMEN = PATH_REGISTRATION_DOKUMEN + "*";

	public static final String PATH_HAPUS_REGISTRATION_DOKUMEN = "/hapus-loket-dokumen";
	private static final String REQUEST_MAPPING_HAPUS_REGISTRATION_DOKUMEN = PATH_HAPUS_REGISTRATION_DOKUMEN + "*";

	/***************************** - LOKET PENERIMAAN SECTION - ****************************/

	// dokumen baru
	@GetMapping(path = REQUEST_MAPPING_LOKET_DOKUMEN)
	public String doShowPageLoketDokumen(@RequestParam(value = "error", required = false) String error, final Model model, final HttpServletRequest request, final HttpServletResponse response) {
		model.addAttribute("menu", "loketPenerimaan");
		model.addAttribute("subMenu", "penerimaanDokumen");

		if (StringUtils.isNotBlank(error)) {
			model.addAttribute("errorMessage", error);
		}

		return PAGE_LOKET_DOKUMEN;
	}

	@RequestMapping(value = REQUEST_MAPPING_AJAX_SEARCH_LIST_DOKUMEN, method = {RequestMethod.POST})
	public void doSearchDataTablesListDokumen(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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

			GenericSearchWrapper<TxPostReception> searchResult = trademarkService.searchPostReception(searchCriteria, orderBy, orderType, offset, limit);
			if (searchResult.getCount() > 0) {
				dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
				dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxPostReception result : searchResult.getList()) {
                	no++;
                    data.add(new String[]{
                            "" + no,
                            "<a href=\"" + getPathURLAfterLogin(PATH_CETAK_DOKUMEN+ "?no=" + result.getPostNo()) + "\">" + result.getPostNo() + "</a>",
                            result.getPostDateTemp(),
                            result.getmFileSequence().getDesc(),
                            result.getmFileType().getDesc(),
                            //result.getmFileTypeDetail().getDesc(),
                            result.getBillingCode(),
                            result.getPostNo(),
                            result.getPaymentDateTemp(),
                            "Rp. " + NumberUtil.formatInteger(result.getTotalPayment()),
                            "<div class=\"btn-actions\">" +
//                                    "<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT_DOKUMEN + "?id=" + result.getId() ) + "\">Ubah</a>"
//                            +"<br/>" +
                            "<a class=\"btn btn-danger btn-xs\" onclick=\"modalDelete('" + result.getId() + "');\">Hapus</a>"
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
	@RequestMapping(value = REQUEST_MAPPING_AJAX_EXPORT_LIST_DOKUMEN, method = {RequestMethod.POST})
	public void doExportDataTablesListDokumen(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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

			GenericSearchWrapper<TxPostReception> searchResult = trademarkService.searchPostReception(searchCriteria, orderBy, orderType, offset, limit);
			if (searchResult.getCount() > 0) {
				dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
				dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

				int no = offset;
				for (TxPostReception result : searchResult.getList()) {
					no++;
					data.add(new String[]{
							"" + no,
							"<a href=\"" + getPathURLAfterLogin(PATH_CETAK_DOKUMEN + "?no=" + result.getPostNo()) + "\">" + result.getPostNo() + "</a>",
							result.getPostDateTemp(),
							result.getmFileSequence().getDesc(),
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
	/***************************** - END LOKET PENERIMAAN SECTION - ****************************/

	/***************************** - TAMBAH PENERIMAAN SECTION - ****************************/
	@GetMapping(REQUEST_MAPPING_TAMBAH_DOKUMEN)
	public String doShowPageTambahDokumen(final Model model, final HttpServletRequest request, final HttpServletResponse response) {
		model.addAttribute("subMenu", "penerimaanDokumen");

		return PAGE_TAMBAH_DOKUMEN;
	}

	@PostMapping(REQUEST_MAPPING_SIMPAN_DOKUMEN)
	//PostReception adalah pojo / sebagai tampungan sementara
	public void doTambahPenerimaanDokumen(@ModelAttribute("form") PostReception form, final BindingResult errors, final Model model,
                                          final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		model.addAttribute("subMenu", "penerimaanDokumen");


		//validate form
		//FieldValidationUtil.required(errors, "mFileSequence.id", form.getFileseq(), "asal permohonan");
		FieldValidationUtil.required(errors, "mFileType.id", form.getmFileType(), "jenis permohonan");
		FieldValidationUtil.required(errors, "codeBilling", form.getCodeBilling(), "Kode Billing");
		FieldValidationUtil.required(errors, "noPermohonan", form.getNoPermohonan(), "Nomor Permohonan");
		//FieldValidationUtil.required(errors, "noPendaftaran", form.getNoPendaftaran(), "Nomor Pendaftaran");
		// validate form end

		TxPostReception formPostRec = new TxPostReception();

		if (!errors.hasErrors()) {
			MUser userLog = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

			MUser user2 = userService.getUserById(userLog.getCurrentId());
			formPostRec.setmFileSequence(user2.getmFileSequence());

			MFileType mFileType = new MFileType();
			mFileType = mFileTypeRepository.findOne(form.getmFileType());
			formPostRec.setmFileType(mFileType);

			MFileTypeDetail mFileTypeDetail = new MFileTypeDetail();
			mFileTypeDetail.setId(form.getmFileTypeDetail());
			formPostRec.setmFileTypeDetail(mFileTypeDetail);

			//formPostRec.setRegNoTemp(form.getNoPendaftaran());
			//formPostRec.setTxTmGeneralTemp(form.getNoPermohonan());
			formPostRec.setBillingCode(form.getCodeBilling());
			formPostRec.setTotalPaymentTemp(form.getTotalPayment());

//        	versi 1.0
//	        if(formPostRec.getmFileTypeDetail().getCurrentId().equals("9df18f49-8f73-45df-91a4-9269b1d2b260")
//		            || formPostRec.getmFileTypeDetail().getCurrentId().equals("74d7f25f-f048-4972-89ed-f0dc98456139")) {
//		    	formPostRec.setPostRecepitNo("R"+ trademarkService.selectLastPostReceiptNo());
//		    } else {
//		    	formPostRec.setPostRecepitNo(trademarkService.selectLastPostReceiptNo());
//		    }        	        	

			formPostRec.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
			Date tempDate = new Date(form.getApplicationDate());
			//Date tempDate = new Date(request.getParameter("applicationDate")); --> versi 1.0 / versi awal
			Timestamp tempTimeStamp = new Timestamp(tempDate.getTime());
			formPostRec.setPostDate(tempTimeStamp);
			//tempDate = new Date(request.getParameter("paymentDate")); --> versi 1.0 / versi awal
			tempDate = new Date(form.getPaymentDate());
			tempTimeStamp = new Timestamp(tempDate.getTime());
			formPostRec.setPaymentDate(tempTimeStamp);
			try {
				trademarkService.insertReception(formPostRec);
			} catch (DataIntegrityViolationException e) {
				logger.error(e.getMessage(), e);
				if (e.getMessage().startsWith(HttpStatus.BAD_REQUEST.getReasonPhrase())) {
					if (e.getMessage().endsWith("mFileTypeDetail")) {

					}
				} else {
					model.addAttribute("errorMessage", "Gagal menambahkan permohonan dokumen");
				}
				if (e.getMessage().startsWith(HttpStatus.NOT_FOUND.getReasonPhrase())) {
					if (e.getMessage().endsWith("MFileSequence")) {
						errors.rejectValue("mFileSequence.id", "field.error.invalid.value", new Object[] {"asal permohonan"}, "");
					} else {
						errors.rejectValue("mFileType.id", "field.error.invalid.value", new Object[] {"jenis permohonan"}, "");
					}
				} else {
					model.addAttribute("errorMessage", "Gagal menambahkan permohonan dokumen");
				}
			}
		}
		response.getWriter().write("Success");
		//response.getWriter().write(formPostRec.getPostReceiptNo());
	}
	/***************************** - END TAMBAH PENERIMAAN SECTION - ****************************/


	/***************************** - EDIT PENERIMAAN SECTION - ****************************/
	@GetMapping(REQUEST_MAPPING_EDIT_DOKUMEN)
	public String doShowPageEditDokumen(final Model model, final HttpServletRequest request) {
		if (doInitiateEditDokumen(model, request)) {
			return PAGE_EDIT_DOKUMEN;
		}

		return REDIRECT_LOKET_DOKUMEN + "?error=Nomor permohonan tidak ditemukan";
	}

	@PostMapping(REQUEST_MAPPING_EDIT_DOKUMEN)
	public String doEditDokumen(@ModelAttribute("form") TxPostReception form, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
		if (!doInitiateEditDokumen(model, request)) {
			return REDIRECT_LOKET_DOKUMEN + "?error=Nomor permohonan " + request.getParameter("no") + " tidak ditemukan";
		}

		TxPostReception existing = (TxPostReception) model.asMap().get("existing");

		form.setId(existing.getCurrentId());
		//form.setApplicationNo(existing.getApplicationNo());
		form.setmFileSequence(existing.getmFileSequence());
		form.setmFileType(existing.getmFileType());
		form.setmFileTypeDetail(existing.getmFileTypeDetail());
		form.setPostDate(existing.getPostDate());
		form.setTotalPayment(existing.getTotalPayment());
		form.setmStatus(existing.getmStatus());
		form.setPostNo(existing.getPostNo());

		// validate form
		FieldValidationUtil.required(errors, "paymentDateTemp", form.getPaymentDate(), "tanggal pembayaran");

		FieldValidationUtil.required(errors, "bankCode", form.getBillingCode(), "kode billing");
		// validate form end

		if (!errors.hasErrors()) {
			form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

			try {
				trademarkService.updatePostReception(form);

				return REDIRECT_CETAK_DOKUMEN + "?no=" + form.getPostNo();
			} catch (DataIntegrityViolationException e) {
				logger.error(e.getMessage(), e);
				model.addAttribute("errorMessage", "Gagal merubah permohonan dokumen");
			}
		}

		return PAGE_EDIT_DOKUMEN;
	}
	/***************************** - END TAMBAH PENERIMAAN SECTION - ****************************/

	/***************************** - CETAK PENERIMAAN SECTION - ****************************/
	@GetMapping(REQUEST_MAPPING_CETAK_DOKUMEN)
	public String doShowPageCetakPenerimaan(@RequestParam(value = "no", required = false) String postNo, final Model model) {
		model.addAttribute("subMenu", "penerimaanDokumen");

		TxPostReception txPostReception = null;
		if (!StringUtils.isEmpty(postNo)) {
			txPostReception = trademarkService.selectOneReceptionByPostNo(postNo);
		}

		if (txPostReception == null) {
			return REDIRECT_LOKET_DOKUMEN + "?error=Nomor permohonan " + postNo + " tidak ditemukan";
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

		return PAGE_CETAK_DOKUMEN;
	}

	/***************************** - END CETAK PENERIMAAN SECTION - ****************************/

	@ModelAttribute
	public void addModelAttribute(final Model model, final HttpServletRequest request) {
		model.addAttribute("menu", "penerimaanDokumen");
		List<KeyValue> searchCriteria = new ArrayList<>();
		searchCriteria.add(new KeyValue("menu", "DAFTAR", true));
		searchCriteria.add(new KeyValue("statusFlag", true, true));

		if (!request.getRequestURI().contains(PATH_CETAK_DOKUMEN)) {
			List<MFileTypeDetail> fileTypeDetailList = fileService.findMFileTypeDetailByStatusFlagTrue();
			model.addAttribute("fileTypeDetailList", fileTypeDetailList);

			List<MFileSequence> fileSequenceList = fileService.findMFileSequenceByStatusFlagTrue();
			model.addAttribute("fileSequenceList", fileSequenceList);

			List<MFileType> fileTypeList = fileService.findMFileTypeByStatusFlagTrue();
			model.addAttribute("fileTypeList", fileTypeList);

			if (request.getRequestURI().contains(PATH_TAMBAH_DOKUMEN)) {

				if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
					TxPostReception txPostReception = new TxPostReception();

					model.addAttribute("form", txPostReception);
				}
			}
		}
	}

	private boolean doInitiateEditDokumen(final Model model, final HttpServletRequest request) {
		model.addAttribute("subMenu", "penerimaanDokumen");
		String id = request.getParameter("id");

		//TxPostReception existing = trademarkService.selectOneReceptionByApplicationNo2(request.getParameter("no"));
		TxPostReception existing = trademarkService.selectOnePostReceptionById(id);
		if (existing != null) {
			TxPostReceptionDetail detail = trademarkService.selectOnePostReceptionDetail(id);
			if (detail != null) {
				//existing.setTxTmGeneralTemp(detail.getAppNo());
				//existing.setRegNoTemp(detail.getRegNo());
			}

			/*TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralBrand(detail.getAppNo());
			if (txTmGeneral != null) {
				//existing.setMerekTemp(txTmGeneral.getTxTmBrand().getName());
			}*/
		}


		if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
			model.addAttribute("form", existing);
		} else {
			model.addAttribute("existing", existing);
		}

		return existing != null;
	}

	@RequestMapping(path = REQUEST_MAPPING_CHECK_CODE_BILLING_DOKUMEN, method = {RequestMethod.POST})
	@ResponseBody
	public void doCheckCodeBilling(final HttpServletRequest request, final HttpServletResponse response,
	                               final HttpSession session) throws JsonProcessingException, IOException {

        /*String bankCode = request.getParameter("bankCode");
        MBillingCode mBillingCode = permohonanOnlineService.selectOneMBillingCode("billingCode", bankCode);
        TxReception txReception = permohonanOnlineService.selectOneTxReception("bankCode", bankCode);
        Map<String, String> dataGeneral = new HashMap<>();;

        if (mBillingCode == null) {
            dataGeneral.put("statusError", "Kode Billing '" + bankCode + "' Tidak Ditemukan");
        } else if (txReception != null) {
            dataGeneral.put("statusError", "Kode Billing '" + bankCode + "' Sudah Digunakan");
        } else {
            dataGeneral.put("mFileSequence", mBillingCode.getmFileSequence());
            dataGeneral.put("mPostFileType", mBillingCode.getmPostFileType());
            dataGeneral.put("mPostFileTypeDetail", mBillingCode.getmPostFileTypeDetail());
            dataGeneral.put("mLaw", "b9e7c8f2-771a-4356-a465-b962b4c46668");
            dataGeneral.put("totalPayment", "" + mBillingCode.getTotalPayment());
            dataGeneral.put("totalPaymentTemp", NumberUtil.formatDecimal(mBillingCode.getTotalPayment()));
            dataGeneral.put("paymentDate", DateUtil.formatDate(mBillingCode.getPaymentDate(),"dd/MM/yyyy HH:mm:ss"));
            dataGeneral.put("statusError", null);
            Date date = new Date();
            Timestamp timestamp = new Timestamp(date.getTime());
            dataGeneral.put("applicationDate", DateUtil.formatDate(timestamp, "dd/MM/yyyy HH:mm:ss"));
        }
        writeJsonResponse(response, dataGeneral);*/

		String statusError = null;
		String bankCode = request.getParameter("bankCode");
		//v1 TxReception txReception = permohonanOnlineService.selectOneTxReception("bankCode", bankCode);
		TxPostReception txReception = permohonanOnlineService.selectOneTxPostReception("billingCode", bankCode);

		Map<String, String> dataGeneral = new HashMap<>();

		if (txReception != null) {
			statusError = "Kode Billing '" + bankCode + "' Sudah Digunakan";
			//return REDIRECT_LOKET_DOKUMEN + "?error= Kode Billing '" + bankCode + " Sudah Digunakan";
		} else {
			String result = simpakiService.getQueryBilling(bankCode);
        	/*sample result :
        	 {"code":"00","message":"SUKSES","data":{"kode_pembayaran":"820180918609475","nomor_transaksi":"D072018000003","kd_tarif":"000680",
        	  "nama":"Amat Faozi (Test Oposisi)","email":"NEEVEEA@YAHOO.COM","no_tlp":"08996123420","tgl_transaksi":"2018-09-18 02:14:27",
        	  "tgl_expired":"2018-09-19 02:14:27","jumlah_billing":"1000000","ntpn":"12342325545545","volume":"1","tgl_pembayaran":"1537203600","flag_pembayaran":"TERBAYAR",
        	  "terpakai":"BELUM"}}
        	
        	*/
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
					String feeCode = dataNode.get("kd_tarif").toString().replaceAll("\"", "");
					MPnbpFeeCode mPnbpFeeCode = simpakiService.getPnbpFeeCodeByCode(feeCode);

					if (mPnbpFeeCode == null) {
						statusError = "Kode Tarif '" + feeCode + "' Tidak Ditemukan";
					} else {
						String totalClass = dataNode.get("volume").toString().replaceAll("\"", "");
						String totalPayment = dataNode.get("jumlah_billing").toString().replaceAll("\"", "");
						//String paymentDate = dataNode.get("tgl_pembayaran").toString().replaceAll("\"","");
						String paymentDateTemp = dataNode.get("tgl_pembayaran").toString().replaceAll("\"", "");
						//Date paymentDate = Date.from(Instant.ofEpochSecond(Long.parseLong(paymentDateTemp)));
						//Date paymentDate = Date.from(Instant.ofEpochSecond(Long.parseLong(paymentDateTemp)));

						Date paymentDate = null;
						try {
							paymentDate = new SimpleDateFormat("yyyy-MM-dd").parse(paymentDateTemp);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						MUser mUserLogin = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
						MUser mUser = userService.getUserById(mUserLogin.getCurrentId());
						dataGeneral.put("mFileSequence",mUser.getmFileSequence()!=null ? mUser.getmFileSequence().getCurrentId() : "");

						/*if (mPnbpFeeCode.getmFileSequence() != null) {
							dataGeneral.put("mFileSequence", mPnbpFeeCode.getmFileSequence().getId());
						} else {
							dataGeneral.put("mFileSequence", "");
						}*/

						if (mPnbpFeeCode.getmFileTypeDetail() != null) {
							dataGeneral.put("mFileTypeDetail", mPnbpFeeCode.getmFileTypeDetail().getId());
						} else {
							dataGeneral.put("mFileTypeDetail", "");
						}

						if (mPnbpFeeCode.getmFileType() != null) {
							dataGeneral.put("mFileType", mPnbpFeeCode.getmFileType().getId());
						} else {
							dataGeneral.put("mFileType", "");
						}


						dataGeneral.put("mLaw", "b9e7c8f2-771a-4356-a465-b962b4c46668");
						dataGeneral.put("totalClass", totalClass);
						dataGeneral.put("totalPayment", totalPayment);
						dataGeneral.put("totalPaymentTemp", NumberUtil.formatDecimal(NumberUtil.parseDecimal(totalPayment)));
						//dataGeneral.put("paymentDate", DateUtil.formatDate(DateUtil.toDate("yyyy-MM-dd HH:mm:ss", paymentDate),"dd/MM/yyyy HH:mm:ss"));
						dataGeneral.put("paymentDate", DateUtil.formatDate(paymentDate, "dd/MM/yyyy HH:mm:ss"));
						dataGeneral.put("applicationDate", DateUtil.formatDate(new Timestamp(System.currentTimeMillis()), "dd/MM/yyyy HH:mm:ss"));
					}
				}
			} else {
				statusError = code.equals("02") ? "Kode Billing '" + bankCode + "' Tidak Ditemukan" : message;
			}
		}
		//remarks dulu : dataGeneral.put("statusError", statusError);

		//for testing only
//        MPnbpFeeCode mPnbpFeeCode = simpakiService.getPnbpFeeCodeByCode(bankCode); 
//        dataGeneral.put("mFileSequence", "00");
//		dataGeneral.put("mFileType", "MEREK_DAGANG");
//		dataGeneral.put("mFileTypeDetail", "2c372c75-e2a7-482e-b6f7-f69b3c230b04");
//		dataGeneral.put("mLaw", "b9e7c8f2-771a-4356-a465-b962b4c46668");
//		dataGeneral.put("totalClass", "10");
//		dataGeneral.put("totalPayment", "100000000");
//		dataGeneral.put("totalPaymentTemp", NumberUtil.formatDecimal(NumberUtil.parseDecimal("100000000")));
//		Date curr = new Date();		
//		dataGeneral.put("paymentDate", DateUtil.formatDate(curr,"dd/MM/yyyy HH:mm:ss"));
//		dataGeneral.put("applicationDate", DateUtil.formatDate(new Timestamp(System.currentTimeMillis()), "dd/MM/yyyy HH:mm:ss"));
		//end testing


		if (statusError != null) {
			dataGeneral.put("statusError", statusError);
		}

		writeJsonResponse(response, dataGeneral);


	}

	@RequestMapping(value = REQUEST_MAPPING_AJAX_REGISTRATION_LIST)
	public void ListRegistrationPermohonan(final Model model, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		model.addAttribute("subMenu", "penerimaanDokumen");

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
			//GenericSearchWrapper<TxRegistration> searchResult = null;
			//searchResult = trademarkService.ListPenerimaanDokumen(searchCriteria, orderBy, orderType, offset, limit);
			GenericSearchWrapper<TxTmGeneral> searchResult = null;
			searchResult = trademarkService.searchGeneralRegistration(searchCriteria, orderBy, orderType, offset, limit);
			if (searchResult.getCount() > 0) {
				dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
				dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

				int no = offset;
				String regNo = "";
				for (TxTmGeneral result : searchResult.getList()) {
					regNo = "-";
					if (result.getTxRegistration() != null) {
						regNo = result.getTxRegistration().getNo();
					}

					no++;
					data.add(new String[]{
							"" + no,
							regNo,
							result.getApplicationNo(),
							"<button type='button' id='pilihBtn' onclick='pilihDokumen(\"" + result.getApplicationNo() + "\")' " +
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

	@RequestMapping(path = REQUEST_MAPPING_REGISTRATION_DOKUMEN)
	public void doGetDataRegistrasiDokumen(Model model, final HttpServletRequest request, final HttpServletResponse response) {
		//TxRegistration txReg = new TxRegistration();
		TxTmGeneral txTmGen = new TxTmGeneral();
		String req = request.getParameter("target");

		if (isAjaxRequest(request)) {
			txTmGen = trademarkService.selectOneRegistration("applicationNo", req);
		}

		Map<String, Object> result = new HashMap<>();
		result.put("noPermohonan", "");
		result.put("noPendaftaran", "");
		result.put("merek", "");
		if (txTmGen != null) {
			result.put("regId", txTmGen.getId());

			if (txTmGen.getTxRegistration() != null) {
				result.put("noPendaftaran", txTmGen.getTxRegistration().getNo());
			} else {
				result.put("noPendaftaran", "");
			}


			result.put("noPermohonan", txTmGen.getApplicationNo());
			result.put("merek", txTmGen.getTxTmBrand().getName());
		}
		writeJsonResponse(response, result);

	}

	/***************************** - HAPUS LOKET DOKUMEN SECTION - ****************************/
	@GetMapping(REQUEST_MAPPING_HAPUS_REGISTRATION_DOKUMEN)
	public void doHapusRegistrasiDokumen(final Model model, final HttpServletRequest request, final HttpServletResponse response) {
		trademarkService.deletePostReception(request.getParameter("receptionId"));
	}
	/***************************** - END GRUP PERMOHONAN SECTION - ****************************/

}