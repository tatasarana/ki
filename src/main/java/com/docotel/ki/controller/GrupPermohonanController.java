package com.docotel.ki.controller;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.enumeration.GroupDetailStatusEnum;
import com.docotel.ki.model.master.*;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.NativeQueryRepository;
import com.docotel.ki.service.master.*;
import com.docotel.ki.service.transaction.GrupPermohonanService;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.util.FieldValidationUtil;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.LazyInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class GrupPermohonanController extends BaseController {
	@Autowired private GrupPermohonanService groupService;
	@Autowired private FileService fileService;
	@Autowired private ClassService classService;
	@Autowired private StatusService statusService;
	@Autowired private LookupService lookupService;
	@Autowired private UserRoleService userRoleService;
	@Autowired private NativeQueryRepository nativeQueryRepository ;
	@Autowired private MActionService mActionService;

	private static final String DIRECTORY_PAGE = "grup-permohonan/";

	private static final String PAGE_GRUP_PERMOHONAN = DIRECTORY_PAGE + "grup-permohonan";
	private static final String PAGE_TAMBAH_GRUP_PERMOHONAN = DIRECTORY_PAGE + "tambah-grup-permohonan";
	private static final String PAGE_DETAIL_GRUP_PERMOHONAN = DIRECTORY_PAGE + "detail-grup-permohonan";

	public static final String PATH_AJAX_SEARCH_LIST = "/cari-grup-permohonan";
	public static final String PATH_AJAX_LIST_PERMOHONAN = "/list-detail-grup-permohonan";
	public static final String PATH_AJAX_SEARCH_PERMOHONAN = "/cari-detail-grup-permohonan";
	public static final String PATH_GRUP_PERMOHONAN = "/grup-permohonan";
	public static final String PATH_TAMBAH_GRUP_PERMOHONAN = "/tambah-grup-permohonan";
	public static final String PATH_HAPUS_GRUP_PERMOHONAN = "/hapus-grup-permohonan";

	public static final String PATH_RELEASE_GRUP_PERMOHONAN = "/release-grup-permohonan";

	public static final String PATH_DETAIL_GRUP_PERMOHONAN = "/detail-grup-permohonan";
	public static final String PATH_SIMPAN_DETAIL_GRUP_PERMOHONAN = "/simpan-detail-grup-permohonan";
	public static final String PATH_AJAX_LIST_DELETE_TXGROUPDETAIL = "/delete-txgroupdetail";

	public static final String REDIRECT_GRUP_PERMOHONAN = "redirect:" + PATH_AFTER_LOGIN + PATH_GRUP_PERMOHONAN;

	private static final String REQUEST_MAPPING_AJAX_SEARCH_LIST = PATH_AJAX_SEARCH_LIST + "*";
	private static final String REQUEST_MAPPING_AJAX_LIST_PERMOHONAN = PATH_AJAX_LIST_PERMOHONAN + "*";
	private static final String REQUEST_MAPPING_AJAX_SEARCH_PERMOHONAN = PATH_AJAX_SEARCH_PERMOHONAN + "*";

	private static final String REQUEST_MAPPING_GRUP_PERMOHONAN = PATH_GRUP_PERMOHONAN + "*";
	private static final String REQUEST_MAPPING_TAMBAH_GRUP_PERMOHONAN = PATH_TAMBAH_GRUP_PERMOHONAN + "*";
	private static final String REQUEST_MAPPING_HAPUS_GRUP_PERMOHONAN = PATH_HAPUS_GRUP_PERMOHONAN + "*";
	private static final String REQUEST_MAPPING_RELEASE_GRUP_PERMOHONAN = PATH_RELEASE_GRUP_PERMOHONAN + "*";
	private static final String REQUEST_MAPPING_AJAX_DELETE_TXGROUPDETAIL = PATH_AJAX_LIST_DELETE_TXGROUPDETAIL + "*";

	private static final String REQUEST_MAPPING_DETAIL_GRUP_PERMOHONAN = PATH_DETAIL_GRUP_PERMOHONAN + "*";
	private static final String REQUEST_MAPPING_SIMPAN_DETAIL_GRUP_PERMOHONAN = PATH_SIMPAN_DETAIL_GRUP_PERMOHONAN + "*";

	/***************************** - GRUP PERMOHONAN SECTION - ****************************/
	@GetMapping(path = REQUEST_MAPPING_GRUP_PERMOHONAN)
	public String doShowPageGrupPermohonan(@RequestParam(value = "error", required = false) String error, final Model model, final HttpServletRequest request, final HttpServletResponse response) {
		List<MLookup> mLookupList = lookupService.selectAllbyGroup("GrupPermohonan");
		model.addAttribute("listTipeGrup", mLookupList);

		if (StringUtils.isNotBlank(error)) {
			model.addAttribute("errorMessage", error);
		}

		return PAGE_GRUP_PERMOHONAN;
	}

	/***************************** - END GRUP PERMOHONAN SECTION - ****************************/

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
						if (searchBy.equalsIgnoreCase("groupType.id") && !value.isEmpty()) {
							searchCriteria.add(new KeyValue(searchBy, value, true));
						} else if (searchBy.equalsIgnoreCase("applicationNo") && !value.isEmpty()) {
							searchCriteria.add(new KeyValue(searchBy, value, true));
						} else {
							if (StringUtils.isNotBlank(value)) {
								searchCriteria.add(new KeyValue(searchBy, value, false));
							}
						}
						/*if (StringUtils.isNotBlank(value)) {
							searchCriteria.add(new KeyValue(searchBy, value, false));
						}*/
					}
				}
			}

			MUser user = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			MUserRole mUserRole = userRoleService.findUserAdministrator(user.getId());
			if(mUserRole==null){ //not admin
				searchCriteria.add(new KeyValue("createdBy.username", user.getUsername(), true));
			}

			String orderBy = request.getParameter("order[0][column]");
			if (orderBy != null) {
				orderBy = orderBy.trim();
				if (orderBy.equalsIgnoreCase("")) {
					orderBy = null;
				} else {
					switch (orderBy) {
						case "1" :
							orderBy = "createdDate";
							break;
						case "2" :
							orderBy = "groupType.name";
							break;
						case "3" :
							orderBy = "no";
							break;
						case "4" :
							orderBy = "name";
							break;
						case "5" :
							orderBy = "total";
							break;
						case "6" :
							orderBy = "description";
							break;
						case "7" :
							orderBy = "createdBy.username";
							break;
						case "8" :
							orderBy = "statusFlag";
							break;
						default :
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

			GenericSearchWrapper<TxGroup> searchResult = groupService.searchGroup(searchCriteria, orderBy, orderType, offset, limit);
			if (searchResult.getCount() > 0) {
				dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
				dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

				int no = offset;
				for (TxGroup result : searchResult.getList()) {
					no++;

					String btnDetail = "<a class=\"btn btn-default btn-xs\" href=\"" + getPathURLAfterLogin(PATH_DETAIL_GRUP_PERMOHONAN + "?id=" + result.getId()) + "\">Detail</a>";
					String btnDelete = "<a class=\"btn btn-danger btn-xs\" onclick=\"return false\" disabled=\"true\">Hapus</a>";
					String btnRelease = "<a class=\"btn btn-info btn-xs\" onclick=\"return false\" disabled=\"true\">Release</a>";

					if (result.getTxServDist() == null && result.getTxPubsJournal() == null ) { //&& result.isStatusFlag()
						btnDelete = "<a class=\"btn btn-danger btn-xs\" onclick=\"modalDelete('" + result.getId() + "');\">Hapus</a>";
					}
//					if ((result.getTxServDist() != null || result.getTxPubsJournal() != null)  && result.isStatusFlag()) {
//						btnRelease = "<a class=\"btn btn-warning btn-xs\" onclick=\"modalRelease('" + result.getId() + "');\">Release</a>";
//					}

					data.add(new String[]{
							"" + no,
							//							result.getGroupType() == null ? "-" : result.getGroupType().getName(),
							"<a href="+  getPathURLAfterLogin(PATH_DETAIL_GRUP_PERMOHONAN + "?id=" + result.getId())  +">" + result.getName() + "</a>",
//							"<a href="+  getPathURLAfterLogin(PATH_DETAIL_GRUP_PERMOHONAN + "?id=" + result.getId())  +">" + result.getGroupType().getName() + "</a>",
							result.getNo() == null ? "-" : result.getNo(),
							result.getGroupType().getName(),
							"" + result.getTotal(),
							result.getDescription(),
							result.getCreatedBy().getUsername(),
							result.getCreatedDateTemp(),
//							result.isStatusFlag() ? "Aktif" : "Tidak Aktif",
//							btnDetail + "<br/>"	+
							btnDelete + "<br/>"
//							+ btnRelease
					});
				}
			}

			dataTablesSearchResult.setData(data);

			writeJsonResponse(response, dataTablesSearchResult);
		} else {
			response.setStatus(HttpServletResponse.SC_FOUND);
		}
	}

	@RequestMapping(value = REQUEST_MAPPING_AJAX_LIST_PERMOHONAN, method = {RequestMethod.POST})
	public void doShowListPermohonan(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		if (isAjaxRequest(request)) {
			setResponseAsJson(response);

			DataTablesSearchResult dataTablesSearchResult = new DataTablesSearchResult();
			try {
				dataTablesSearchResult.setDraw(Integer.parseInt(request.getParameter("draw")));
			} catch (NumberFormatException e) {
				dataTablesSearchResult.setDraw(0);
			}

			List<String[]> data = new ArrayList<>();
			String groupId = request.getParameter("groupId");

			GenericSearchWrapper<Object[]> searchResult = groupService.searchGroupDetailList(groupId);
			if (searchResult.getList().size() > 0) {
				int no = 0;
				for (Object[] result : searchResult.getList()) {
					no++;

					data.add(new String[]{
							"" + no,
							result[1].toString(),
							result[2].toString(),
							result[3].toString(),
							result[4] == null ? "-" : result[4].toString(),
							result[6] == null ? "-" : result[6].toString(),
							result[7] == null ? "-" : result[7].toString(),
							result[5] == null ? "-" : result[5].toString(),
							"<a class=\"btn btn-danger btn-xs\" onclick=\"modalDelete('" + result[0].toString() + "');\">Hapus</a>"
					});
				}
			}
			dataTablesSearchResult.setData(data);
			writeJsonResponse(response, dataTablesSearchResult);
		} else {
			response.setStatus(HttpServletResponse.SC_FOUND);
		}
	}

	@RequestMapping(value = REQUEST_MAPPING_AJAX_SEARCH_PERMOHONAN, method = {RequestMethod.POST})
	public void doSearchDataTablesPermohonan(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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

			String[] excludeStatus = {"IPT_DRAFT", "TM_PERMOHONAN_BARU", "IPT_PENGAJUAN_PERMOHONAN", "TM_PASCA_PERMOHONAN"};
			String[] excludeArr = request.getParameterValues("excludeArr[]");
			String[] includeArr = request.getParameterValues("includeArr[]");
			String[] searchByArr = request.getParameterValues("searchByArr[]");
			String[] keywordArr = request.getParameterValues("keywordArr[]");
			List<KeyValue> searchCriteria = null;

            if (searchByArr != null) {
				searchCriteria = new ArrayList<>();

				searchCriteria.add(new KeyValue("mStatus.lockedFlag", false, true));

				for (int i = 0; i < searchByArr.length; i++) {
					String searchBy = searchByArr[i];
					String value = null;
					try {
						value = keywordArr[i];
					} catch (ArrayIndexOutOfBoundsException e) {
					}
					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (searchBy.equalsIgnoreCase("startDate") || searchBy.equalsIgnoreCase("endDate")) {
							try {
								searchCriteria.add(new KeyValue(searchBy, DateUtil.toDate("dd/MM/yyyy", value), true));
							} catch (ParseException e) {
							}
						} else if (searchBy.equalsIgnoreCase("groupTypeFlag")) {
							if (value.equals("Publikasi")) {
								searchCriteria.add(new KeyValue("groupJournal", false, false));
							} else if (value.equals("Pelayanan Teknis")) {
								searchCriteria.add(new KeyValue("groupDist", false, false));
							}
						}
						else {
							if (StringUtils.isNotBlank(value)) {
								if (searchBy.equalsIgnoreCase("mClassId")) {
									searchCriteria.add(new KeyValue(searchBy, value, true));
								} else if (searchBy.equalsIgnoreCase("mAction.name")) {
									searchCriteria.add(new KeyValue(searchBy, value, true));
								}
								else {
									searchCriteria.add(new KeyValue(searchBy, value, false));
								}
							}
						}
					}
				}
			}
			//searchCriteria.add(new KeyValue("mStatus.name", "(TM) Data Capture Dokumen Selesai", true));

			String orderBy = request.getParameter("order[0][column]");
			if (orderBy != null) {
				orderBy = orderBy.trim();
				if (orderBy.equalsIgnoreCase("")) {
					orderBy = null;
				} else {
					switch (orderBy) {
						case "1" :
							orderBy = "filingDate";
							break;
						case "2" :
							orderBy = "applicationNo";
							break;
						case "3" :
							orderBy = "txTmBrand.name";
							break;
						case "4" :
							orderBy = "classList"; //txTmGeneral.txTmCLassList.mClass.no
							break;
						case "5" :
							orderBy = "txTmOwner.name";
							break;
						case "6" :
							orderBy = "txTmRepresentative.mRepresentative.name";
							break;
						case "7" :
							orderBy = "mStatus.name";
							break;
						case "8" :
							orderBy = "mAction.name";
							break;
						default :
							orderBy = "id";
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

			GenericSearchWrapper<TxTmGeneral> searchResult = groupService.searchGeneralFromGrupPermohonan(searchCriteria, excludeStatus, excludeArr, includeArr, orderBy, orderType, offset, limit);
			if (searchResult.getCount() > 0) {
				dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
				dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

				int no = offset;
				//searchResult.getList().stream().parallel().forEach(result ->{
				for(TxTmGeneral result : searchResult.getList()) {
					String brandName = "-";
					String classList = "-";
					String ownerNameList = "-";
					String reprsName = "-";
					String status = "-";
					String aksi = "-";

					try {
						//brandName = result.getTxTmBrand().getDescChar();
						brandName = result.getTxTmBrand().getName();
					} catch (NullPointerException e) {
					}

					try {
						Map<String, String> classMap = new HashMap<>();
						StringBuffer sbClassList = new StringBuffer();
						for (TxTmClass txTmClass : result.getTxTmClassList()) {
							classMap.put(""+txTmClass.getmClass().getNo(), ""+txTmClass.getmClass().getNo());
						}
						for (Map.Entry<String, String> map : classMap.entrySet()) {
							sbClassList.append(map.getKey());
							sbClassList.append(", ");
						}
						if (sbClassList.length() > 0) {
							classList = sbClassList.substring(0, sbClassList.length() - 2);
						}
					} catch (NullPointerException | LazyInitializationException e) {
					}

					try {
						Map<String, String> ownerMap = new HashMap<>();
						StringBuffer sbOwnerList = new StringBuffer();
						for (TxTmOwner txTmOwner : result.getTxTmOwner()) {
							ownerMap.put(""+ txTmOwner.getName(), ""+ txTmOwner.getName());
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
						for (TxTmRepresentative txTmRepresentative : result.getTxTmRepresentative()) {
							if (txTmRepresentative.isStatus()) {
								reprsName = txTmRepresentative.getmRepresentative().getName();
							}
						}
					} catch (NullPointerException e) {
					}

					try {
						status = result.getmStatus().getName();
					} catch (NullPointerException e) {
					}

					try {
						aksi = result.getmAction().getName();
					} catch (NullPointerException e) {
					}

					Long sss = Long.valueOf(0);
					String op = "" ;
					try{
						String no_id = result.getId();
						String no_app = result.getApplicationNo();
						GenericSearchWrapper <Object[]> Oposisi = nativeQueryRepository.searchOposisi(no_id);
						int sum_oposisi = Oposisi.getList().size();
						sss = Long.valueOf(sum_oposisi);
						op =  sss.toString();
						if (!op.equalsIgnoreCase("0")){
							op = "<a href=\"layanan/monitor-oposisi/?no="+no_app+"\" target=\"_blank\"><b>"+op+"</b></a>";
						}
					}catch(Exception e){}



					data.add(new String[]{
							result.getId(),
							result.getFilingDateTemp(),
							result.getApplicationNo(),
							brandName,
							classList,
							ownerNameList,
							reprsName,
							status,
							aksi,
							op,
							//"<button type=\"button\" onclick=\"choosePermohonan('" + result.getId() + "')\" " +
							//        " data-dismiss=\"modal\">Pilih</button>"
					});
					//});
				}
				/*for (TxTmGeneral result : searchResult.getList()) {

				}*/
			}

			dataTablesSearchResult.setData(data);

			writeJsonResponse(response, dataTablesSearchResult);
		} else {
			response.setStatus(HttpServletResponse.SC_FOUND);
		}
	}

	/***************************** - TAMBAH GRUP PERMOHONAN SECTION - ****************************/
	@GetMapping(REQUEST_MAPPING_TAMBAH_GRUP_PERMOHONAN)
	public String doShowPageTambahGrupPermohonan(final Model model, final HttpServletRequest request, final HttpServletResponse response) {
		model.addAttribute("form", new TxGroup());

		List<MLookup> mLookupList = lookupService.selectAllbyGroup("GrupPermohonan");
		model.addAttribute("listTipeGrup", mLookupList);

		return PAGE_TAMBAH_GRUP_PERMOHONAN;
	}

	@PostMapping(REQUEST_MAPPING_TAMBAH_GRUP_PERMOHONAN)
	public String doTambahGrupPermohonan(@ModelAttribute("form") TxGroup form, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
		FieldValidationUtil.required(errors, "tipegrup", form.getGroupType(), "tipe grup");
		if (FieldValidationUtil.required(errors, "name", form.getName(), "nama grup")) {
			FieldValidationUtil.maxLength(errors, "name", form.getName(), "nama grup", 100);
		}
		if (StringUtils.isNotBlank(form.getDescription())) {
			FieldValidationUtil.maxLength(errors, "description", form.getDescription(), "deskripsi grup", 1000);
		}

		if (!errors.hasErrors()) {
			form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

			try {
				groupService.insertGroup(form);
				return REDIRECT_GRUP_PERMOHONAN;
			} catch (DataIntegrityViolationException e) {
				logger.error(e.getMessage(), e);
				model.addAttribute("errorMessage", "Gagal menambahkan grup permohonan");
			}
		}

		List<MLookup> mLookupList = lookupService.selectAllbyGroup("GrupPermohonan");
		model.addAttribute("listTipeGrup", mLookupList);

		return PAGE_TAMBAH_GRUP_PERMOHONAN;
	}
	/***************************** - END TAMBAH GRUP PERMOHONAN SECTION - ****************************/

	/***************************** - RELEASE GRUP PERMOHONAN SECTION - ****************************/
	@GetMapping(REQUEST_MAPPING_RELEASE_GRUP_PERMOHONAN)
	public void doReleaseGrupPermohonan(final Model model, final HttpServletRequest request, final HttpServletResponse response) {
		String group_id = request.getParameter("groupId");

		List<KeyValue> searchCriteria = new ArrayList<>();
		searchCriteria.add(new KeyValue("txGroup.id", group_id, false));
		GenericSearchWrapper<TxGroupDetail> searchResult = groupService.searchGroupDetail(searchCriteria, "id", "DESC", 0, null);
		if (searchResult.getCount() > 0) {
			for (TxGroupDetail result : searchResult.getList()) {
				groupService.releaseGroupDetail(result);
			}
			groupService.releaseGroup(groupService.selectOneGroupById(group_id));
		}
		//groupService.deleteGroup(group_id);
	}


	/***************************** - HAPUS GRUP PERMOHONAN SECTION - ****************************/
	@GetMapping(REQUEST_MAPPING_HAPUS_GRUP_PERMOHONAN)
	public void doHapusGrupPermohonan(final Model model, final HttpServletRequest request, final HttpServletResponse response) {
		String group_id = request.getParameter("groupId");

		List<KeyValue> searchCriteria = new ArrayList<>();
		searchCriteria.add(new KeyValue("txGroup.id", group_id, false));
		GenericSearchWrapper<TxGroupDetail> searchResult = groupService.searchGroupDetail(searchCriteria, "id", "DESC", 0, null);
		if (searchResult.getCount() > 0) {
			for (TxGroupDetail result : searchResult.getList()) {
				if(result.getTxTmGeneral().getTxReception()!=null) { // skip while txreception is null
					groupService.deleteGroupDetail(result);
				}
			}
		}
		groupService.deleteGroup(group_id);
	}

	@RequestMapping(value = REQUEST_MAPPING_AJAX_DELETE_TXGROUPDETAIL, method = RequestMethod.POST)
	public void doDeleteTxGroupDetail(Model model, @RequestParam("idTxGroupDetail") String id, @RequestParam("groupId") String groupId, final HttpServletRequest request, final HttpServletResponse response) {
		groupService.deleteGroupDetailById(id, groupId);
	}
	/***************************** - END GRUP PERMOHONAN SECTION - ****************************/

	/***************************** - TAMBAH PERMOHONAN GRUP SECTION - ****************************/
	@GetMapping(REQUEST_MAPPING_DETAIL_GRUP_PERMOHONAN)
	public String doShowPageTambahDetailGrupPermohonan(final Model model, final HttpServletRequest request, final HttpServletResponse response) {
		if (doInitiateTambahPermohonanGrup(model, request)) {

//			https://stackoverflow.com/questions/17955777/redirect-to-an-external-url-from-controller-action-in-spring-mvc
//			String redirectUrl = request.getScheme() + "://ahlicoding.com" ;
////			return 	"redirect:" +redirectUrl;
			return PAGE_DETAIL_GRUP_PERMOHONAN;
		}
		return REDIRECT_GRUP_PERMOHONAN + "?error=Grup Id " + request.getParameter("id") + " tidak ditemukan";
	}

	@PostMapping(REQUEST_MAPPING_SIMPAN_DETAIL_GRUP_PERMOHONAN)
	public void doSimpanDetailGrupPermohonan(final Model model, final HttpServletRequest request, final HttpServletResponse response) {
		String group_id = request.getParameter("groupId");
		int totalPermohonan = Integer.parseInt(request.getParameter("totalPermohonan"));
		String[] listAppId = request.getParameterValues("listAppId[]");

		String[] txGroupDetailList = groupService.selectAllGroupDetail(group_id);
		TxGroup txGroup = groupService.findOneGroupById(group_id);
		if(txGroupDetailList.length > 0) {
			groupService.deleteTxGroupDetailByBatch(totalPermohonan, group_id, txGroupDetailList);
		}

		ArrayList<TxGroupDetail> addTxGroupDetail =  new ArrayList<>();
		if (listAppId != null) {
			for(String appId : listAppId) {
				TxGroupDetail txGroupDetail = new TxGroupDetail();
				txGroupDetail.setTxGroup(txGroup);
				txGroupDetail.setTxTmGeneral(groupService.findOneGeneralById(appId));
				txGroupDetail.setStatus(GroupDetailStatusEnum.PREPARE.name());

				addTxGroupDetail.add(txGroupDetail);
			}
			groupService.insertGroupDetail(totalPermohonan, listAppId, group_id, addTxGroupDetail);
		}
	}
	/***************************** - END TAMBAH PERMOHONAN GRUP SECTION - ****************************/

	@ModelAttribute
	public void addModelAttribute(final Model model, final HttpServletRequest request) {
//		model.addAttribute("menu", "permohonanMerek"); ==comment by fitria 24/12/2018===
//		model.addAttribute("subMenu", "grupPermohonan"); ==comment by fitria 24/12/2018, mengeluarkan grup dari menu Permohonan Merek===
		model.addAttribute("menu", "grupPermohonan");

		if (request.getRequestURI().contains(PATH_TAMBAH_GRUP_PERMOHONAN)) {
			if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
				TxGroup txGroup = new TxGroup();

				model.addAttribute("form", txGroup);
			}
		}

		if (request.getRequestURI().contains(PATH_DETAIL_GRUP_PERMOHONAN)) {
			List<MFileTypeDetail> fileTypeDetailList = fileService.getAllFileTypeDetail();
			model.addAttribute("fileTypeDetailList", fileTypeDetailList);

			List<MClass> classList = classService.findAllMClass();
			Collections.sort(classList, (o1, o2) -> new Integer(o1.getNo()).compareTo(new Integer(o2.getNo())));
			model.addAttribute("classList", classList);
		}
	}

	private boolean doInitiateTambahPermohonanGrup(final Model model, final HttpServletRequest request) {
		TxGroup existing = groupService.selectOneGroupById(request.getParameter("id"));

		if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
			model.addAttribute("form", existing);
		} else {
			model.addAttribute("existing", existing);
		}

		List<MStatus> statusList = statusService.selectStatus();
		model.addAttribute("statusList", statusList);

		List<MAction> actionList = mActionService.findAll();
		model.addAttribute("actionList", actionList);

		if(existing != null) {
			model.addAttribute("isDetail",  existing.getTxServDist() != null); // || existing.getTxPubsJournal() != null); //!existing.isStatusFlag() ||
			return existing != null;
		} else {
			return false;
		}
	}
}
