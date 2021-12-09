package com.docotel.ki.controller;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.MEmployee;
import com.docotel.ki.model.master.MRoleSubstantif;
import com.docotel.ki.model.master.MRoleSubstantifDetail;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.pojo.CetakEkspedisi;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.service.master.EmployeeService;
import com.docotel.ki.service.transaction.GrupPermohonanService;
import com.docotel.ki.service.transaction.PelayananTeknisService;
import com.docotel.ki.service.transaction.TrademarkService;
import com.docotel.ki.signature.PDFSignatureFacade;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.util.FieldValidationUtil;
import com.docotel.ki.enumeration.GroupDetailStatusEnum;
import com.docotel.ki.model.transaction.TxGroup;
import com.docotel.ki.model.transaction.TxGroupDetail;
import com.docotel.ki.model.transaction.TxServDist;
import com.docotel.ki.model.transaction.TxTmClass;
import com.docotel.ki.model.transaction.TxTmOwner;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class PelayananTeknisController extends BaseController {
	@Autowired
	private PelayananTeknisService technicalService;
	@Autowired
	private GrupPermohonanService groupService;
	@Autowired
	private TrademarkService trademarkService;
	@Autowired
	private EmployeeService employeeService;
	
	@Value(("${certificate.file}"))
    private String CERTIFICATE_FILE;

	private static final String DIRECTORY_PAGE = "pelayanan-teknis/";

	private static final String PAGE_TECHNICAL_SERVICE = DIRECTORY_PAGE + "pelayanan-teknis";
	private static final String PAGE_DISTRIBUTION = DIRECTORY_PAGE + "distribusi";
	private static final String PAGE_EDIT = DIRECTORY_PAGE + "edit-distribusi";

	public static final String PATH_CETAK_EKSEPEDISI =  "/ekspedisi-cetak";
	public static final String PATH_AJAX_SEARCH_LIST = "/cari-pelayanan-teknis";
	public static final String PATH_AJAX_SEARCH_LIST_DETAIL = "/list-detail-edit-distribusi";

	public static final String PATH_TECHNICAL_SERVICE = "/pelayanan-teknis";

	public static final String PATH_DISTRIBUTION = "/distribusi";
	public static final String PATH_EDIT = "/edit-distribusi";
	public static final String PATH_DELETE = "/hapus-distribusi";

	public static final String REDIRECT_TECHNICAL_SERVICE = "redirect:" + PATH_AFTER_LOGIN + PATH_TECHNICAL_SERVICE;
	private static final String REQUEST_MAPPING_DELETE = PATH_DELETE + "*";
	private static final String REQUEST_MAPPING_AJAX_SEARCH_LIST = PATH_AJAX_SEARCH_LIST + "*";
	private static final String REQUEST_MAPPING_AJAX_LIST_PERMOHONAN_DISTRIBUSI_DETAIL = PATH_AJAX_SEARCH_LIST_DETAIL + "*";
	private static final String REQUEST_MAPPING_TECHNICAL_SERVICE = PATH_TECHNICAL_SERVICE + "*";
	private static final String REQUEST_MAPPING_DISTRIBUTION = PATH_DISTRIBUTION + "*";
	private static final String REQUEST_MAPPING_EDIT = PATH_EDIT + "*";
	private static final String REQUEST_MAPPING_CETAK_EKSPEDISI = PATH_CETAK_EKSEPEDISI + "*";

	/***************************** - TECHNICAL SERVICE SECTION - ****************************/
	@RequestMapping(path = REQUEST_MAPPING_TECHNICAL_SERVICE)
    public String doShowPageTechnicalService(@RequestParam(value = "error", required = false) String error, Model model, final HttpServletRequest request, final HttpServletResponse response) {
		if (StringUtils.isNotBlank(error)) {
			model.addAttribute("errorMessage", error);
		}
        
    	return PAGE_TECHNICAL_SERVICE;
    }

	@RequestMapping(value = REQUEST_MAPPING_DELETE, method = RequestMethod.GET)
	public void doHapusDistribusi(Model model, @RequestParam("id") String id, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		TxServDist txServDist = technicalService.findOne(id);
		if ( txServDist == null ) {
			response.setStatus(400);
			response.getWriter().write("Data Distribusi Tidak Ditemukan");
			return;
		}

		if ( technicalService.countTxSubsCheckForGroup(txServDist.getId()) > 0 ) {
			response.setStatus(400);
			response.getWriter().write("Data Distribusi tidak dapat dihapus, karena sudah diperiksa oleh pemeriksa");
			return;
		}
		trademarkService.deleteDistribusi(id);
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
						if ( searchBy.equalsIgnoreCase("applicationNo") )  {
							searchCriteria.add(new KeyValue("applicationNoGroup", value, false));
						}
						else if (searchBy.equalsIgnoreCase("createdDate")) {
							try {
								searchCriteria.add(new KeyValue(searchBy, DateUtil.forceToDate("dd/MM/yyyy", value), true));
							} catch (ParseException e) {
							}
						}
						else {
							if (StringUtils.isNotBlank(value)) {
							searchCriteria.add(new KeyValue(searchBy, value, false));
						}
						}

					}
				}
			}

			/*<th>Tanggal</th>
			<th>Nama Grup</th>
			<th>Jumlah</th>
			<th>Accepted</th>
			<th>Released</th>
			<th>Pemeriksa</th>
			<th>Role Substantif</th>
			<th>Status Permohonan</th>

			result.getmRoleSubstantifDetail().getmRoleSubstantif().getName(),
			statusService.GetMSatusById(result.getmStatus().getId()).getName(),
			*/

			String orderBy = request.getParameter("order[0][column]");
			if (orderBy != null) {
				orderBy = orderBy.trim();
				if (orderBy.equalsIgnoreCase("")) {
					orderBy = null;
				} else {
					switch (orderBy) {
						case "1" :
							orderBy = "txGroup.name";
							break;
						case "2" :
							orderBy = "txGroup.total";
							break;
						case "3" :
							orderBy = "txGroup.total";
							break;
						case "4" :
							orderBy = "mRoleSubstantifDetail.mEmployee.employeeName";
							break;
						case "5" :
							orderBy = "mRoleSubstantifDetail.mRoleSubstantif.name";
							break;
						case "6" :
							orderBy = "createdDate";
							break;
						/*case "8" :
							orderBy = "mStatus.name";
							break;*/
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

			List<KeyValue> searchCriteriaGroupDetail;
			List<String[]> data = new ArrayList<>();
			GenericSearchWrapper<TxServDist> searchResult = technicalService.searchDistribution(searchCriteria, orderBy, orderType, offset, limit);
			if (searchResult.getCount() > 0) {
				dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
				dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

				int no = offset;
				for (TxServDist result : searchResult.getList()) {
					searchCriteriaGroupDetail = new ArrayList<>();
					searchCriteriaGroupDetail.add(new KeyValue("txGroup.id", result.getTxGroup().getId(), true));
					searchCriteriaGroupDetail.add(new KeyValue("status", GroupDetailStatusEnum.RELEASE.name(), true));
					long countRelease = technicalService.countTxGroupDetail(searchCriteriaGroupDetail);

					String button = "";
					MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
					if (mUser.hasAccessMenu("T-PBH")) {
						button = "<button type='button' class='btn btn-danger btn-xs' id='deleteBtnPelayananTeknis' onClick='deleteDistribusi(\"" + result.getId() + "\")'>Hapus</button>";
					}

					no++; 
					data.add(new String[]{
							"" + no,
							//sCreatedDate, //
							"<a href="+ getPathURLAfterLogin(PATH_EDIT + "?no=" + result.getId()) +">"+ result.getTxGroup().getName() + "</a>",
							"" + result.getTxGroup().getTotal(),
							"" + countRelease,
							result.getmRoleSubstantifDetail().getmEmployee().getEmployeeName(),
							result.getmRoleSubstantifDetail().getmRoleSubstantif().getName(),
							/*result.getmStatus().getName(),*/
							result.getCreatedDateTemp(),
							button
					});
				}
			}

			dataTablesSearchResult.setData(data);

			writeJsonResponse(response, dataTablesSearchResult);
		} else {
			response.setStatus(HttpServletResponse.SC_FOUND);
		}
	}
	/***************************** - END TECHNICAL SERVICE SECTION - ****************************/

	/***************************** - DISTRIBUTION SECTION - ****************************/
	@GetMapping(REQUEST_MAPPING_DISTRIBUTION)
	public String doShowPageDistribution(final Model model, final HttpServletRequest request, final HttpServletResponse response) {
		model.addAttribute("form", new TxServDist());
		return PAGE_DISTRIBUTION;
	}

	@PostMapping(REQUEST_MAPPING_DISTRIBUTION)
	public String doSaveDistribution(@ModelAttribute("form") TxServDist form, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
		FieldValidationUtil.required(errors, "txGroup.id", form.getTxGroup().getId(), "nama grup");
		FieldValidationUtil.required(errors, "mRoleSubstantifDetail.id", form.getmRoleSubstantifDetail().getId(), "pemeriksa");

		if (!errors.hasErrors()) {
			form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

			try {
				technicalService.insertDistribution(form);
				return REDIRECT_TECHNICAL_SERVICE;
			} catch (DataIntegrityViolationException e) {
				logger.error(e.getMessage(), e);
				model.addAttribute("errorMessage", "Gagal mendistribusikan grup permohonan");
			}
		}

		return PAGE_DISTRIBUTION;
	}
	/***************************** - END DISTRIBUTION SECTION - ****************************/

	@ModelAttribute
	public void addModelAttribute(final Model model, final HttpServletRequest request, final HttpServletResponse response) {
		model.addAttribute("menu", "pelayananTeknis");

		if (request.getRequestURI().contains(PATH_TECHNICAL_SERVICE) || request.getRequestURI().contains(PATH_DISTRIBUTION)) {
			List<MRoleSubstantifDetail> mRoleSubstantifDetail = technicalService.selectmRoleSubstantifDetail();
			List<MRoleSubstantifDetail> userSubsList = mRoleSubstantifDetail.stream().distinct().collect(Collectors.toList());
			model.addAttribute("userSubsList", userSubsList);
		}
		
		if (request.getRequestURI().contains(PATH_TECHNICAL_SERVICE)) {
			List<MRoleSubstantif> mRoleSubstantif = technicalService.findmRoleSubstantif();
			model.addAttribute("roleSubsList", mRoleSubstantif);
		}

		if (request.getRequestURI().contains(PATH_DISTRIBUTION)) {
			List<KeyValue> searchCriteria = new ArrayList<>();
			searchCriteria.add(new KeyValue("groupType.name", "Pelayanan Teknis", true));

			GenericSearchWrapper<TxGroup> searchResult = groupService.searchGroupDistribusi(searchCriteria, "id", "DESC", 0, null);
			model.addAttribute("groupList", searchResult.getList());
		}
	}

	@GetMapping(REQUEST_MAPPING_EDIT)
	public String showPageEdit(Model model, @RequestParam(value = "no", required = true) String no) {
		TxServDist txServDist = technicalService.selectOne(no);
		if (txServDist != null) {
			model.addAttribute("form", txServDist);
			model.addAttribute("txGroup", txServDist.getTxGroup());					
			
			return PAGE_EDIT;
		}
		return REDIRECT_TECHNICAL_SERVICE + "?error=Distribusi tidak ditemukan";
	}

	@PostMapping(REQUEST_MAPPING_EDIT)
	public String doProsesEdit(@ModelAttribute("form") TxServDist form, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
		// validate form
		FieldValidationUtil.required(errors, "notes", form.getNotes(), "notes");
		FieldValidationUtil.required(errors, "mRoleSubstantifDetail.id", form.getmRoleSubstantifDetail().getId(), "pemeriksa");
		// validate form end

		if (!errors.hasErrors()) {
			TxServDist txServDist = technicalService.findOne(form.getCurrentId());
			txServDist.setNotes(form.getNotes());
			txServDist.setmRoleSubstantifDetail(form.getmRoleSubstantifDetail());
			try {
				technicalService.saveOrUpdate(txServDist);
				model.asMap().clear();
				return PAGE_TECHNICAL_SERVICE;
			} catch (DataIntegrityViolationException e) {
				logger.error(e.getMessage(), e);
				model.addAttribute("errorMessage", "Gagal Mengubah Distribusi ");
			}
		}

		return showPageEdit(model, form.getCurrentId());
	}

	@RequestMapping(value = REQUEST_MAPPING_AJAX_LIST_PERMOHONAN_DISTRIBUSI_DETAIL, method = {RequestMethod.POST})
	public void doShowListPermohonanDistribusiDetail(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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

			List<KeyValue> searchCriteria = new ArrayList<>();;
			searchCriteria.add(new KeyValue("txGroup.id", request.getParameter("groupId"), false));

			String orderBy = request.getParameter("order[0][column]");
			if (orderBy != null) {
				orderBy = orderBy.trim();
				if (orderBy.equalsIgnoreCase("")) {
					orderBy = null;
				} else {
					switch (orderBy) {
						case "1" :
							orderBy = "txTmGeneral.filingDate";
							break;
						case "2" :
							orderBy = "txTmGeneral.applicationNo";
							break;
						case "3" :
							orderBy = "txTmGeneral.txTmBrand.name";
							break;
						case "4" :
							orderBy = "txTmGeneral.txTmCLassList.mClass.no";
							break;
						case "5" :
							orderBy = "txTmGeneral.txTmOwner.name";
							break;
						case "6" :
							orderBy = "status";
							break;
						default :
							orderBy = "txGroup.id";
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

			GenericSearchWrapper<TxGroupDetail> searchResult = technicalService.searchGroupDetail(searchCriteria, orderBy, orderType, offset, limit);
			if (searchResult.getCount() > 0) {
				dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
				dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

				int no = offset;
				for (TxGroupDetail result : searchResult.getList()) {
					no++;

					String brandName = "-";
					String classList = "-";
					String ownerNameList = "-";
					String status = "-";
					String pemeriksa = "-";
					String pemeriksaLanjutan = "-";

					try {
						brandName = result.getTxTmGeneral().getTxTmBrand().getName();
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
                    		if(txTmOwner.isStatus() == true) {
                        		ownerMap.put(""+ txTmOwner.getName(), ""+ txTmOwner.getName());
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
						//status = result.getTxTmGeneral().getmStatus().getName();
						status = GroupDetailStatusEnum.valueOf(result.getStatus()).getLabel();
                    } catch (NullPointerException e) {
	                }
					
					try {
						//serv_dist -> mrole_subs_detail->
						pemeriksa = result.getmUser1().getmEmployee().getEmployeeName();
					} catch (NullPointerException e) {
					}
					
					try {
						pemeriksaLanjutan = result.getmUser2().getmEmployee().getEmployeeName();
					} catch (NullPointerException e) {
					}

					data.add(new String[]{
							"" + no,
							result.getTxTmGeneral().getFilingDateTemp(),
							result.getTxTmGeneral().getApplicationNo(),
							brandName,
							classList,
							ownerNameList,
							status,
							result.getTxTmGeneral().getmStatus().getName(),
							pemeriksa,
							pemeriksaLanjutan
					});
				}
			}

			dataTablesSearchResult.setData(data);

			writeJsonResponse(response, dataTablesSearchResult);
		} else {
			response.setStatus(HttpServletResponse.SC_FOUND);
		}
	}

	//cetak ekspedisi dokumen ke pdf
	@GetMapping(REQUEST_MAPPING_CETAK_EKSPEDISI)
	public String expedisiCetak (@RequestParam(value = "no", required = false) String no,ModelMap modelMap,
			@ModelAttribute("form") TxServDist form,
			 final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {

		try {	    
		    String patternCetak = "dd/MM/YYYY HH:mm:ss"; 	    
		    SimpleDateFormat sdfCetak = new SimpleDateFormat(patternCetak, new Locale("ID"));	
		    
		    String patternData = "dd MMMM YYYY";	    
		    SimpleDateFormat sdfData = new SimpleDateFormat(patternData, new Locale("ID"));	

		    //searchCriteria.add(new KeyValue("id", no, false));
		    TxServDist txServDist = technicalService.selectOne(no);
		    Map<String,Object> params = new HashMap<String,Object>();
		    params.put("pPrintDate", sdfCetak.format(new Date()) );

		    MEmployee mEmpPengirim = employeeService.selectOneByUserIdEmployee(txServDist.getCreatedBy().getId());
		    MEmployee mEmpPenerima= employeeService.selectOneByUserIdEmployee(txServDist.getmRoleSubstantifDetail().getmEmployee().getUserId().getId());
		    try {
		    	if(mEmpPengirim!=null) {
		    		params.put("pBagianPengirim", mEmpPengirim.getEmployeeName().toUpperCase().concat(", ").concat(mEmpPengirim.getmSection().getName())  ) ;
		    	} else {
		    		params.put("pBagianPengirim", "-");
		    	}

            } catch (NullPointerException e) {
            		logger.error(e);
            }

		    try {
		    	if(mEmpPenerima!=null) {
		    		params.put("pBagianPenerima", mEmpPenerima.getEmployeeName().toUpperCase().concat(", ").concat(mEmpPenerima.getmSection().getName()) );
		    		params.put("fBagianPenerima", mEmpPenerima.getEmployeeName().toUpperCase());
		    	} else {
		    		params.put("pBagianPenerima","-");
		    		params.put("fBagianPenerima","-");
		    	}

		    } catch (NullPointerException e) {
           		logger.error(e);
		    }

		    params.put("pNoBundle", txServDist.getTxGroup().getNo());
	    	

		    List<KeyValue> searchCriteria = new ArrayList<>();
		    searchCriteria.add(new KeyValue("txGroup.id", txServDist.getTxGroup().getId(), false));
		    //start close temporary by abdi : biar muncul dulu reportnya
		    //searchCriteria.add(new KeyValue("status", "Accepted", true));
		    //end close
		    
		    List<CetakEkspedisi> listDataEkspedisi = new ArrayList<>();
		    CetakEkspedisi cetakExp = null;
		    GenericSearchWrapper<TxGroupDetail> searchResult = groupService.reportEkspedisi(searchCriteria, "txTmGeneral.applicationNo", "ASC", null, null);
		    params.put("pJumlahDokumen", String.valueOf(searchResult.getCount()) );
		    if (searchResult.getCount() > 0) {
				for (TxGroupDetail result : searchResult.getList()) {
					String brandName = "-";
	                String classList = "-";
	                try {
	                	brandName = result.getTxTmGeneral().getTxTmBrand().getName();
					} catch (NullPointerException e) {
	                }

	                try {
	                	Map<String, String> classMap = new HashMap<>();
	                    StringBuffer sbClassList = new StringBuffer();
	                	for (TxTmClass txTmClass : result.getTxTmGeneral().getTxTmClassList()) {
	                		classMap.put(""+ txTmClass.getmClass().getNo(), ""+ txTmClass.getmClass().getNo());
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

                	 //nomor permohonan - tanggal penerimaan - kelas - nama merek
                    //if(!listDataEkspedisi.get().getNoPermohonan().contains(result) ){

                	cetakExp = new CetakEkspedisi();
 			    	cetakExp.setNoPermohonan(result.getTxTmGeneral().getApplicationNo());
 			    	cetakExp.setTanggalPenerimaan( sdfData.format(result.getTxTmGeneral().getFilingDate()) );
 			    	cetakExp.setMerek(brandName);
 			    	cetakExp.setKelas(classList);
 			    	listDataEkspedisi.add(cetakExp);
				}

		    } else {
		    	model.addAttribute("errorMessage", "Gagal cetak Ekspedisi Dokumen" );
		    	return REDIRECT_TECHNICAL_SERVICE+ "?error=Gagal Cetak Ekspedisi, Silakan Cek status distribusi";
		    }

	        //removeDuplicate(listDataEkspedisi);
		    List<CetakEkspedisi> listReportFinal = new ArrayList();
		    Map<String, Object> map2 = new HashMap<String, Object >();
	        Iterator<CetakEkspedisi> it = listDataEkspedisi.iterator();
	        while (it.hasNext()) {	            
	            //String applicationNo = it.next().getNoPermohonan();	
	            CetakEkspedisi ce = it.next();
	            if (map2.containsKey(ce.getNoPermohonan()) ) {
	                it.remove();
	            } else {
	            	map2.put(ce.getNoPermohonan(), ce.getNoPermohonan());
	            	//listReportFinal.add(it.next());
	            	listReportFinal.add(ce);
	            } 
	        }


		    params.put("IsiDataReport", new JRBeanCollectionDataSource(listReportFinal));

		    ClassLoader classLoader = getClass().getClassLoader();					  
		      
			//dari source jasper
			File file = new File(classLoader.getResource("report/EkspedisiDokumen.jasper").getFile());
			InputStream jasperStream1 = new FileInputStream(file);
			JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream1);
		    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JRBeanCollectionDataSource(listReportFinal));
	
		    response.setContentType("application/pdf");//x-pdf-> download, -pdf->open new window browser
		    response.setHeader("Content-disposition", "inline; filename= EkspedisiDokumen_" + sdfCetak.format(new Date())  + ".pdf");

		    //JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
		    byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
			InputStream is = new ByteArrayInputStream(pdfBytes); 
			this.signPdf(is, response.getOutputStream());
			return null;
	    } catch (Exception ex) {
	    	logger.error(ex.getMessage(), ex);
			model.addAttribute("errorMessage", "Gagal cetak Ekspedisi Dokumen" );
	    	return REDIRECT_TECHNICAL_SERVICE+ "?error=Gagal Cetak Ekspedisi : " + ex.getMessage();
	    }
	}
	
	private void signPdf(InputStream input, OutputStream output){
        String key = CERTIFICATE_FILE + "eAdministrasi.p12";
        //System.out.println("PATH : "+key);
        try {
        	PDFSignatureFacade facade = new PDFSignatureFacade();
            facade.sign(key, "JakartaPP123!@#", input, output, true, new java.awt.Rectangle(250,0,400,50));	
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

	public static <T> void removeDuplicate(List <T> list) {
		Set <T> set = new HashSet <T>();
		List <T> newList = new ArrayList <T>();
		for (Iterator <T>iter = list.iterator();    iter.hasNext(); ) {
		   Object element = iter.next();
		   if (set.add((T) element))
		      newList.add((T) element);
		   }
		   list.clear();
		   list.addAll(newList);
		}
}
