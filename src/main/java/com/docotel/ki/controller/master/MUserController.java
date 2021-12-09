package com.docotel.ki.controller.master;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.*;
import com.docotel.ki.model.transaction.TxOnlineReg;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.pojo.RegUserReprs;
import com.docotel.ki.repository.custom.master.MLookupCostumRepository;
import com.docotel.ki.service.master.*;
import com.docotel.ki.service.transaction.RegistrasiOnlineService;
import com.docotel.ki.util.FieldValidationUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class MUserController extends BaseController {

	@Autowired
    UserService userService;
	@Autowired
    EmployeeService employeeService;
	@Autowired
    DivisionService divisionService;
	@Autowired
    DepartementService departementService;
	@Autowired
    SectionService sectionService;
	@Autowired
	ProvinceService provinceService;
	@Autowired
	FileSeqService fileSeqService;
	@Autowired
	RepresentativeService representativeService;
	@Autowired
	private LookupService lookupService;
	@Autowired
    RegistrasiOnlineService registrasiOnlineService;
	@Autowired
	private CityService cityService;
	@Autowired
	private CountryService countryService;
	@Autowired
	MLookupCostumRepository mLookupCostumRepository;

	@Value("${spring.datasource.driverClassName:}")
	private String driverClassName;

	private static final String DIRECTORY_PAGE = "user-management/user/";

	private static final String PAGE_LIST_USER = DIRECTORY_PAGE + "list-user";
	private static final String PAGE_TAMBAH_USER = DIRECTORY_PAGE + "tambah-user";
	private static final String PAGE_EDIT_USER = DIRECTORY_PAGE + "edit-user";

	public static final String PATH_RESET_PASSWORD = "/reset-password";

	private static final String PATH_USER = "/list-user";
	private static final String PATH_EDIT_USER = "/edit-user";
	private static final String PATH_AJAX_LIST_USER = "/cari-user";
	private static final String PATH_TAMBAH_USER = "/tambah-user";

	private static final String PATH_TAMBAH_ROLE_USER = "/tambah-user-role";

	public static final String REDIRECT_LIST_USER = "redirect:" + PATH_AFTER_LOGIN + PATH_USER;

	private static final String REQUEST_MAPPING_AJAX_LIST_USER = PATH_AJAX_LIST_USER + "*";
	private static final String REQUEST_MAPPING_TAMBAH_USER = PATH_TAMBAH_USER + "*";
	private static final String REQUEST_MAPPING_EDIT_USER = PATH_EDIT_USER + "*";

	@ModelAttribute
	public void addModelAttribute(final Model model, final HttpServletRequest request) {
		List<MDivision> mDivision = divisionService.findAll();
		List<MDepartment> mDepartment = departementService.findAll();
		List<MSection> mSection = sectionService.findAll();

		List<MProvince> mProvince = provinceService.findAll();
		List<MCity> mCity = cityService.findAll();
		List<MCountry> mCountry = countryService.findAll();

		List<MLookup> mLookup = lookupService.selectAllbyGroup("JenisPemohon");

		model.addAttribute("listDivision", mDivision);
		model.addAttribute("listDepartement", mDepartment);
		model.addAttribute("listSection", mSection);

		model.addAttribute("listProvince", mProvince);
		model.addAttribute("listCity", mCity);
		model.addAttribute("listCountry", mCountry);

		model.addAttribute("listApplicantType", mLookup);

		model.addAttribute("menu", "usermgmnt");
		model.addAttribute("subMenu", "listUser");

		if (request.getRequestURI().contains(PATH_TAMBAH_USER)) {
			if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
				RegUserReprs rep = new RegUserReprs();
				model.addAttribute("form", rep);
			}
		} else if (request.getRequestURI().contains(PATH_EDIT_USER)) {
			if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
				model.addAttribute("form", new RegUserReprs());
			}
		}
	}

	@RequestMapping(path = PATH_USER)
	public String showPageList(@RequestParam(value = "error", required = false) String error, Model model) {
//		List<MUser> list = userService.getAllUsers();
		List<MUser> list = new ArrayList<>();
		model.addAttribute("list", list);

		if (StringUtils.isNotBlank(error)) {
			model.addAttribute("errorMessage", error);
		}

		return PAGE_LIST_USER;
	}

	@PostMapping(value = REQUEST_MAPPING_AJAX_LIST_USER)
	public void doGetListDataTables(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		String statusUser = "";

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
						if (searchBy.equalsIgnoreCase("empReprsStatus") && !value.isEmpty()) {
							//oracle convert menjadi 1 (true) dan 0 (false)
							boolean bvalue = false;
							if (driverClassName.contains("oracle")) {
								bvalue = Boolean.parseBoolean(value);
								if ("true".equalsIgnoreCase(value)) {
									bvalue = true;
								} else {
									bvalue = false;
								}
							}
							searchCriteria.add(new KeyValue(searchBy, bvalue, true));
						} else if (searchBy.equalsIgnoreCase("mDivision.id") && !value.isEmpty()) {
							searchCriteria.add(new KeyValue("mSection.mDepartment.mDivision.id", value, true));
						} else if (searchBy.equalsIgnoreCase("mDepartment.id") && !value.isEmpty()) {
							searchCriteria.add(new KeyValue("mSection.mDepartment.id", value, true));
						} else if (searchBy.equalsIgnoreCase("mSection.id") && !value.isEmpty()) {
							searchCriteria.add(new KeyValue("mSection.id", value, true));
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
					switch (orderBy) {
						case "1":
							orderBy = "username";
							break;
						case "2":
							orderBy = "mEmployee.employeeName";
							break;
						case "3":
							orderBy = "userType";
							break;
						case "4":
							orderBy = "mSection.mDepartment.name";
							break;
						case "5":
							orderBy = "mSection.name";
							break;
						case "6":
							orderBy = "enabled";
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

			//v1 --> GenericSearchWrapper<MEmployee> searchResult = employeeService.searchGeneral(searchCriteria, orderBy, orderType, offset, limit);
			GenericSearchWrapper<MUser> searchResult = userService.searchGeneral2(searchCriteria, orderBy, orderType, offset, limit);
			if (searchResult.getCount() > 0) {
				dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
				dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

				int no = offset;
				//v1 --> for (MEmployee result : searchResult.getList()) {
				for (MUser result : searchResult.getList()) {
					String divisiName = "-";
					String deprtName = "-";
					String sectionName = "-";
					String tipeUser = "-";
					Date date = null;
					String pdate = "-";
					try {
						divisiName = result.getmSection() == null ? "-" : result.getmSection().getmDepartment().getmDivision().getName();
					} catch (NullPointerException e) {
					}
					try {
						deprtName = result.getmSection() == null ? "-" : result.getmSection().getmDepartment().getName();
					} catch (NullPointerException e) {
					}
					try {
						sectionName = result.getmSection() == null ? "-" : result.getmSection().getName();
					} catch (NullPointerException e) {
					}

					try {
						tipeUser = result.getUserType() == null ? "-" : result.getUserType();
						if (StringUtils.isBlank(tipeUser)) {
							tipeUser = result.isEmployee() ? "Karyawan" : result.isReprs() ? "Konsultan" : "-";
						}

					} catch (NullPointerException e) {
					}

					try {
						if(result.getCreatedDate() != null) {

							date = new Date(result.getCreatedDate().getTime());
							pdate = new SimpleDateFormat("dd-MM-yyyy").format(date);
						} else {
							date = new Date(date.getTime());
							pdate = new SimpleDateFormat("dd-MM-yyyy").format(date);
						}
					} catch (NullPointerException e) {
					}

					Boolean statusString = false;
					String personName = "";

					if (result.isEmployee()) { //karyawan
						MEmployee mEmployee = employeeService.selectOneByUserId(result.getId());
						if (mEmployee != null) {
							statusString = mEmployee.isStatusFlag();
							personName = mEmployee.getEmployeeName();
						}
					} else if (result.isReprs()) { //konsultan
						MRepresentative mRepresentative = representativeService.selectOneByUserId(result.getId());
						if (mRepresentative != null) {
							statusString = mRepresentative.isStatusFlag();
							personName = mRepresentative.getName();
						}
					} else {
						//registrasi akun
						TxOnlineReg txOnlineReg = registrasiOnlineService.selectByValue("email", result.getUsername());
						if (txOnlineReg != null) {
							if (txOnlineReg.getmReprs() != null) {
								statusString = txOnlineReg.getmReprs().isStatusFlag();
								personName = txOnlineReg.getmReprs().getName();
							} else {
								statusString = "APPROVE".equalsIgnoreCase(txOnlineReg.getApprovalStatus());
								personName = txOnlineReg.getName();
							}
						}
					}

					String statusOke;
					if (statusString == true) {
						statusOke = "Aktif";
					} else {
						statusOke = "Tidak Aktif";
					}

					// For user role access button menu
                    MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    String button = "";
                    if(mUser.hasAccessMenu("T-USRL")) {
                    	button = "<div class=\"btn-actions\">" +
                    			 "<a class=\"btn btn-primary btn-xs\" href=\"" + getPathURLAfterLogin(PATH_TAMBAH_ROLE_USER + "?no=" + result.getId()) + "\">Role</a>" +
                                 "</div>";
                    } if (mUser.hasAccessMenu("T-UUR")) {
                    		button = "<div class=\"btn-actions\">" +
                    				 "<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT_USER + "?no=" + result.getId()) + "\">Ubah</a> " +
                    				 "</div>";
                    } if (mUser.hasAccessMenu("T-USRL") && mUser.hasAccessMenu("T-UUR")) {
                    		button = "<div class=\"btn-actions\">" +
                    				"<a class=\"btn btn-primary btn-xs\" href=\"" + getPathURLAfterLogin(PATH_TAMBAH_ROLE_USER + "?no=" + result.getId()) + "\">Role</a>" +
                                     "<br />" +
                                     "<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT_USER + "?no=" + result.getId()) + "\">Ubah</a> " +
                                    "</div>";
                    } if (mUser.hasAccessMenu("T-RSTP")) {
                            button = "<div class=\"btn-actions\">" +
                                    "<a class=\"btn btn-danger btn-xs\" href=\"" +getPathURLAfterLogin(PATH_RESET_PASSWORD + "?no=" + result.getId()) + "\">Reset Pwd</a> " +
                                    "</div>";
                    } if (mUser.hasAccessMenu("T-USRL") && mUser.hasAccessMenu("T-UUR") && mUser.hasAccessMenu("T-RSTP")) {
                        button = "<div class=\"btn-actions\">" +
                                "<a class=\"btn btn-primary btn-xs\" href=\"" + getPathURLAfterLogin(PATH_TAMBAH_ROLE_USER + "?no=" + result.getId()) + "\">Role</a>" +
                                "<br />" +
                                "<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT_USER + "?no=" + result.getId()) + "\">Ubah</a> " +
                                "<br />" +
                                "<a class=\"btn btn-danger btn-xs\" href=\"" + getPathURLAfterLogin(PATH_RESET_PASSWORD + "?no=" + result.getId()) + "\">Reset Pwd</a> " +
                                "</div>";
                    }

					no++;
					data.add(new String[]{
							"" + no,
							result.getUsername(),// v1 --> result.getUserId().getUsername(),
							personName, //v1 --> result.getEmployeeName(),
							tipeUser,
							deprtName,
							sectionName,
							statusOke,
							pdate,
							button
							/*"<div class=\"btn-actions\">" +
									"<a class=\"btn btn-primary btn-xs\" href=\"" + getPathURLAfterLogin(PATH_TAMBAH_ROLE_USER + "?no=" + result.getId()) + "\">Role</a>" +
									"<br />" +
									"<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT_USER + "?no=" + result.getId()) + "\">Ubah</a> " +
									"</div>"*/
					});
				}
			}

			dataTablesSearchResult.setData(data);

			writeJsonResponse(response, dataTablesSearchResult);
		} else {
			response.setStatus(HttpServletResponse.SC_FOUND);
		}
	}

	@GetMapping(REQUEST_MAPPING_TAMBAH_USER)
	public String showPageTambah(Model model) {
		List<MFileSequence> mFileSequence = fileSeqService.findByStatusFlagTrue();
//		List<MRepresentative> mRepresentative = representativeService.findAll(); //mRepresentativeRepository.findAll();
		List<MLookup> mDataUser = lookupService.selectAllbyGroup("DataUser");

		model.addAttribute("listAsalPermohonan", mFileSequence);
		model.addAttribute("listDataUser", mDataUser);
//		model.addAttribute("listReprs", mRepresentative);
		return PAGE_TAMBAH_USER;
	}

	@PostMapping(REQUEST_MAPPING_TAMBAH_USER)
	public String doProsesTambah(@ModelAttribute("form") RegUserReprs form, final Model model, final BindingResult errors,
	                             final HttpServletRequest request, final HttpServletResponse response) {

		boolean isValid = true;
		// validate form
		FieldValidationUtil.required(errors, "username", form.getUsername(), "username");
//		FieldValidationUtil.required(errors, "birthDate", form.getBirthDate(), "Tanggal Lahir");
//		FieldValidationUtil.required(errors, "nik", form.getNik(), "nik");
//		FieldValidationUtil.required(errors, "nik", form.getNik(), "Nomor KTP");
		FieldValidationUtil.required(errors, "namaKaryawan", form.getNamaKaryawan(), "Nama Karyawan");
		FieldValidationUtil.required(errors, "divisiId", form.getDepartementId(), "Divisi");
//		FieldValidationUtil.required(errors, "provinceId", form.getProvinceId(), "Provinsi");
//		FieldValidationUtil.required(errors, "cityId", form.getCityId(), "Kota / Kabupaten");
//		FieldValidationUtil.required(errors, "nationalityId", form.getNationalityId(), "Negara");
		FieldValidationUtil.required(errors, "departementId", form.getDepartementId(), "Departement");
		FieldValidationUtil.required(errors, "bagianId", form.getDepartementId(), "Bagian");
		FieldValidationUtil.required(errors, "asalPermohonanId", form.getAsalPermohonanId(), "Asal Permohonan");
//		FieldValidationUtil.required(errors, "address", form.getAddress(), "Alamat");
//		FieldValidationUtil.required(errors, "zipCode", form.getZipCode(), "Kode Pos");
	//	FieldValidationUtil.required(errors, "telp", form.getTelp(), "Telepon");
	//	FieldValidationUtil.required(errors, "email", form.getEmail(), "Email");
		FieldValidationUtil.required(errors, "userType", form.getUserType(), "Jenis User");
//		FieldValidationUtil.required(errors, "applicantType", form.getApplicantType(), "Jenis Pemohon");

		if (!errors.hasErrors() && isValid) {
			MUser UserExisting = userService.getUserByUsername(form.getUsername());

			List<KeyValue> searchCriteriaUsername = new ArrayList<>();
			searchCriteriaUsername.add(new KeyValue("username", form.getUsername(), true));

			if (UserExisting != null || userService.count(searchCriteriaUsername) > 0) {
        		model.addAttribute("errorMessage", "Username sudah digunakan/terdaftar, silakan masukkan username yang lain");
			} else if (!errors.hasErrors()){
				try {
					userService.insertUserEmployee(form);
					return PAGE_LIST_USER;
				} catch (DataIntegrityViolationException e) {
					logger.error(e.getMessage(), e);
					model.addAttribute("errorMessage", "Gagal menambahkan konsultan, data sudah Ada");
				}
			}
		}

		// validate form end
		/*if (!errors.hasErrors()) {
			try {
				userService.insertUserEmployee(form);
				return PAGE_LIST_USER;
			} catch (DataIntegrityViolationException e) {
				logger.error(e.getMessage(), e);
				model.addAttribute("errorMessage", "Gagal menambahkan konsultan, data sudah Ada");
			}
		}*/

		return PAGE_TAMBAH_USER;
	}

	@GetMapping(REQUEST_MAPPING_EDIT_USER)
	public String showPageEdit(Model model, @RequestParam(value = "no", required = true) String no, final HttpServletRequest request) {
		//MEmployee mEmployee = employeeService.findOne(no);
		RegUserReprs form = new RegUserReprs();
		MUser mUser = userService.getUserById(no);

		List<MFileSequence> mFileSequence = fileSeqService.findByStatusFlagTrue();
		model.addAttribute("listAsalPermohonan", mFileSequence);

		if (mUser == null) {
			return REDIRECT_LIST_USER + "?error=User tidak ditemukan";
		}

		//K1 : Karyawan, K2: Konsultan
		if (mUser.isEmployee()) {
			MEmployee mEmployee = employeeService.selectOneByUserIdEmployee(no);

			if (mEmployee != null) {
				//form.setId(mEmployee.getId());
				form.setId(mUser.getCurrentId());
				form.setUsername(mEmployee.getUserId().getUsername());
				form.setNik(mEmployee.getNik());
				form.setNamaKaryawan(mEmployee.getEmployeeName());
				form.setTelp(mEmployee.getTelp());
				form.setEmail(mEmployee.getEmail());

				String divisiId = null;
				String deprtId = null;
				String sectionId = null;
				String asalPId = null;

				try {
					divisiId = mEmployee.getmSection().getmDepartment().getmDivision().getId();
				} catch (NullPointerException e) {
				}
				try {
					deprtId = mEmployee.getmSection().getmDepartment().getId();
				} catch (NullPointerException e) {
				}
				try {
					sectionId = mEmployee.getmSection().getId();
				} catch (NullPointerException e) {
				}
				try {
					asalPId = mUser.getmFileSequence().getCurrentId();
				} catch (NullPointerException e) {
				}

				form.setDivisiId(divisiId);
				form.setDepartementId(deprtId);
				//form.setDepartementId(mEmployee.getmSection().getmDepartment());
				form.setBagianId(sectionId);
				form.setAsalPermohonanId(asalPId);
				form.setUserType(mUser.getUserType());
				form.setStatus(mEmployee.isStatusFlag());

				model.addAttribute("form", form);
				return PAGE_EDIT_USER;
			}
//			else {
//
//				form.setId(mUser.getCurrentId());
//				form.setUsername(mUser.getUsername());
//				form.setNik("");
//				form.setNamaKaryawan("");
//				form.setTelp("");
//				form.setEmail(mUser.getEmail());
//
//				String divisiId = null;
//				String deprtId = null;
//				String sectionId = null;
//				String asalPId = null;
//
//				MSection mSection = null;
//				try {
//					sectionId = mUser.getmSection().getId();
//					mSection = sectionService.selectOne("id", mUser.getmSection().getId());
//					divisiId = mSection.getmDepartment().getmDivision().getCurrentId();
//					deprtId = mSection.getmDepartment().getId();
//				} catch (NullPointerException e) {
//				}
//				try {
//					asalPId = mUser.getmFileSequence().getCurrentId();
//				} catch (NullPointerException e) {
//				}
//
//				form.setDivisiId(divisiId);
//				form.setDepartementId(deprtId);
//				form.setBagianId(sectionId);
//				form.setAsalPermohonanId(asalPId);
//				form.setUserType(mUser.getUserType());
//
//				model.addAttribute("form", form);
//				return PAGE_EDIT_USER;
//			}
		} else if (mUser.isReprs()) {
			MRepresentative mRepresentative = representativeService.selectOneByUserIdReprs(no);
			TxOnlineReg txOnlineReg = registrasiOnlineService.selectByValue("email", mUser.getUsername());
			if (mRepresentative != null) {
				String asalPId = null;
				String provinceId = null;
				String cityId = null;

				try {
					asalPId = mUser.getmFileSequence().getCurrentId();
				} catch (NullPointerException e) {
				}
				try {
					provinceId = mRepresentative.getmProvince().getId();
				} catch (NullPointerException e) {
				}
				try {
					cityId = mRepresentative.getmCity().getId();
				} catch (NullPointerException e) {
				}

				form.setNik(txOnlineReg.getNo());
				form.setNamaKaryawan(mRepresentative.getName());
				form.setAddress( mRepresentative.getAddress() );
				form.setZipCode( mRepresentative.getZipCode());
				form.setProvinceId( provinceId );
				form.setCityId( cityId );
				form.setTelp(mRepresentative.getPhone());
				form.setId(mUser.getCurrentId());
				form.setUsername(mUser.getUsername());
				form.setEmail(mUser.getEmail());
				form.setDivisiId("00-NODIV");
				form.setDepartementId("0000-NODEPT");
				form.setBagianId("000000");

				form.setUserType("Konsultan");
				/*form.setBagianId(sectionId);*/
				form.setAsalPermohonanId(asalPId);
				form.setKonsultan(mRepresentative.getNo());
				form.setStatus(mRepresentative.isStatusFlag());
				
				model.addAttribute("form", form);

//				List<MRepresentative> mRepresentativeList = representativeService.selectAllReprsOrderByName();
//				model.addAttribute("listReprs", mRepresentativeList);

				return PAGE_EDIT_USER;
			}
		} else {

			String asalPId = null;
			String provinceId = null;
			String cityId = null;
			String nationalityId = null;
			try {
				asalPId = mUser.getmFileSequence().getCurrentId();
			} catch (NullPointerException e) {
			}
			TxOnlineReg txOnlineReg = registrasiOnlineService.selectByValue("email", mUser.getUsername());
			MLookup mLookup = mLookupCostumRepository.selectOne("name",txOnlineReg.getApplicantType(), true);
			if (txOnlineReg != null) {
				if (txOnlineReg.getmReprs() != null) {
					form.setStatus(txOnlineReg.getmReprs().isStatusFlag());
				} else {
					form.setStatus("APPROVE".equalsIgnoreCase(txOnlineReg.getApprovalStatus()));
				}

			}

			try {
				provinceId = txOnlineReg.getmProvince().getId();
			} catch (NullPointerException e) {
			}
			try {
				cityId = txOnlineReg.getmCity().getId();
			} catch (NullPointerException e) {
			}
			try {
				nationalityId = txOnlineReg.getNationality().getId();
			} catch (NullPointerException e) {
			}

			form.setNik( txOnlineReg.getNo() );
			form.setNamaKaryawan( txOnlineReg.getName() );
			form.setBirthDate( txOnlineReg.getBirthDateTemp() );
			form.setAddress( txOnlineReg.getAddress() );
			form.setZipCode( txOnlineReg.getZipCode());
			form.setProvinceId( provinceId );
			form.setCityId( cityId );
			form.setNationalityId( nationalityId );
			form.setApplicantType( mLookup.getId() );
			form.setTelp( txOnlineReg.getPhone() );
			form.setAsalPermohonanId(asalPId);
			form.setId(mUser.getCurrentId());
			form.setUsername(mUser.getUsername());
			form.setEmail(mUser.getEmail());
			form.setUserType(mUser.getUserType());
			form.setDivisiId("00-NODIV");
			form.setDepartementId("0000-NODEPT");
			form.setBagianId("000000");
			form.setStatus(mUser.isEnabled());

			model.addAttribute("form", form);

			return PAGE_EDIT_USER;
		}

		return REDIRECT_LIST_USER + "?error=User tidak ditemukan";
	}

	@PostMapping(REQUEST_MAPPING_EDIT_USER)
	public String doProsesEdit(@ModelAttribute("form") RegUserReprs form, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
		MUser muser = userService.getUserById(form.getId());
		form.setUsername(muser.getUsername());

		// validate form
		if (muser.isEmployee()) {
			FieldValidationUtil.required(errors, "username", form.getUsername(), "username");
//			FieldValidationUtil.required(errors, "nik", form.getNik(), "NIK");
			FieldValidationUtil.required(errors, "namaKaryawan", form.getNamaKaryawan(), "Nama Karyawan");
			FieldValidationUtil.required(errors, "divisiId", form.getDivisiId(), "Divisi");
			FieldValidationUtil.required(errors, "departementId", form.getDepartementId(), "Departement");
			FieldValidationUtil.required(errors, "bagianId", form.getDepartementId(), "Bagian");
			FieldValidationUtil.required(errors, "asalPermohonanId", form.getAsalPermohonanId(), "Asal Permohonan");
//			FieldValidationUtil.required(errors, "telp", form.getTelp(), "Telepon");
//			FieldValidationUtil.required(errors, "email", form.getEmail(), "Email");
		FieldValidationUtil.required(errors, "userType", form.getUserType(), "Jenis User");
		}else{
			FieldValidationUtil.required(errors, "username", form.getUsername(), "username");
			FieldValidationUtil.required(errors, "nik", form.getNik(), "NIK");
//			FieldValidationUtil.required(errors, "namaKaryawan", form.getNamaKaryawan(), "Nama Karyawan");
//			FieldValidationUtil.required(errors, "divisiId", form.getDivisiId(), "Divisi");
//			FieldValidationUtil.required(errors, "departementId", form.getDepartementId(), "Departement");
//			FieldValidationUtil.required(errors, "bagianId", form.getDepartementId(), "Bagian");
//			FieldValidationUtil.required(errors, "asalPermohonanId", form.getAsalPermohonanId(), "Asal Permohonan");
//			FieldValidationUtil.required(errors, "telp", form.getTelp(), "Telepon");
//			FieldValidationUtil.required(errors, "email", form.getEmail(), "Email");
			FieldValidationUtil.required(errors, "userType", form.getUserType(), "Jenis User");
		}

		if (!errors.hasErrors()) {
			try {
				userService.updateUserEmployee(muser, form);
				return REDIRECT_LIST_USER;
			} catch (DataIntegrityViolationException e) {
				logger.error(e.getMessage(), e);
				model.addAttribute("errorMessage", "Gagal menambahkan username, data sudah Ada");
			}
		}
		return PAGE_EDIT_USER + "?no=" + form.getId();
	}
}
