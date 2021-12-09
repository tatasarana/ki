package com.docotel.ki.controller.master;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.MDepartment;
import com.docotel.ki.model.master.MSection;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.service.master.DepartementService;
import com.docotel.ki.service.master.SectionService;
import com.docotel.ki.util.FieldValidationUtil;
import org.apache.commons.lang.StringUtils;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
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
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class MBagianController extends BaseController {

    @Autowired
    DepartementService departementService;

    @Autowired
    SectionService sectionService;

    private static final String DIRECTORY_PAGE = "master/departemen-bagian/";

    private static final String PAGE_LIST = DIRECTORY_PAGE + "list-bagian";
    private static final String PAGE_TAMBAH = DIRECTORY_PAGE + "tambah-bagian";
    private static final String PAGE_EDIT = DIRECTORY_PAGE + "edit-bagian";

    private static final String PATH_LIST = "/list-bagian";
    private static final String PATH_TAMBAH = "/tambah-bagian";
    private static final String PATH_EDIT = "/edit-bagian";

    private static final String PATH_AJAX_LIST = "/cari-bagian";
    
	private static final String PATH_EXPORT_CLASS = "/ekspor-bagian";

    private static final String REQUEST_MAPPING_AJAX_LIST = PATH_AJAX_LIST + "*";
    private static final String REQUEST_MAPPING_TAMBAH = PATH_TAMBAH + "*";
    private static final String REQUEST_MAPPING_EDIT = PATH_EDIT + "*";

	private static final String REQUEST_EXPORT_CLASS = PATH_EXPORT_CLASS + "*";
    private static final String REDIRECT_TO_LIST = "redirect:" + PATH_AFTER_LOGIN + PATH_LIST;


    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "master");
        model.addAttribute("subMenu", "listBagian");

        if (request.getRequestURI().contains(PATH_TAMBAH)) {
            if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
                model.addAttribute("form", new MSection());
            }
        }
    }

    @RequestMapping(path = PATH_LIST)
    public String showPageList(Model model,  @RequestParam(value = "no", required = true) String no) {
        //MDepartment mDepartment = departementService.findOne(no);
    	MDepartment mDepartment = departementService.selectOne("id", no);
        List<MSection> list = sectionService.selectAllByDepartment("mDepartment.id", no);
        model.addAttribute("list", list);
        model.addAttribute("mDepartment", mDepartment);
        
        return PAGE_LIST;
    }

    @PostMapping(value = REQUEST_MAPPING_AJAX_LIST)
    public void doGetListDataTables(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (isAjaxRequest(request)) {
            String idDepart = null;
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
                            if (searchBy.equals("mDepartment.id")) {
                                idDepart = value;
                            }
                            searchCriteria.add(new KeyValue(searchBy, value, false));
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
						case "1" :
							orderBy = "code";
							break;
						case "2" :
							orderBy = "name";
							break;
						case "3" :
							orderBy = "createdBy.username";
							break;
						case "4" :
							orderBy = "statusFlag";
							break;
						case "5" :
							orderBy = "createdDate";
							break;
						default :
							orderBy = "code";
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

            GenericSearchWrapper<MSection> searchResult = sectionService.searchGeneral(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (MSection result : searchResult.getList()) {
                    String param = idDepart + "|" + result.getId();
                    byte[] encodedBytes = Base64.getEncoder().encode(param.getBytes());
                    Date date = new Date(result.getCreatedDate().getTime());
                    String pDate = new SimpleDateFormat("dd-MM-yyyy").format(date);
                    String stts = ((result.isStatusFlag() == true) ? "Aktif" : "Tidak Aktif");
                    
                    // For user role access button menu
                    MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    String button = "";
                    if(mUser.hasAccessMenu("T-UBG")) {
                    	button = "<div class=\"btn-actions\">"
                                + "<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT + "?no=" + result.getId()) + "\">Ubah</a>"
                                + "</div>";
                    }
                    
                    no++;
                    data.add(new String[]{
                            "" + no,
                            result.getCode(),
                            result.getName(),
                            result.status(),
                            result.getCreatedBy().getUsername(),
                            pDate,
                            button
                            //"<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT + "?no=" + new String(encodedBytes)) + "\">Ubah</a>"
                            /*"<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT + "?no=" + result.getId() ) + "\">Ubah</a>"*/
                    });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    @GetMapping(REQUEST_MAPPING_TAMBAH)
    public String showPageTambah(Model model, @RequestParam(value = "no", required = true) String no) {
        //MDepartment mDepartment = departementService.findOne(no); 
    	MDepartment mDepartment = departementService.selectOne("id", no);
    	model.addAttribute("mDepartment", mDepartment);
        return PAGE_TAMBAH; 
    }

    @PostMapping(REQUEST_MAPPING_TAMBAH)
    public String doProsesTambah(@ModelAttribute("form") MSection form, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
        // validate form
        FieldValidationUtil.required(errors, "mDepartment.id", form.getmDepartment().getCurrentId(), "mDepartment.id");
        FieldValidationUtil.required(errors, "code", form.getCode(), "Kode Seksi");
        FieldValidationUtil.required(errors, "name", form.getName(), "Nama Seksi");
        if (!errors.hasFieldErrors("code")) {
        	MSection mSection = sectionService.selectOne("code", form.getCode());
        	if (mSection != null)
	        	FieldValidationUtil.uniqueValue(errors, "code", form.getCode(), "Kode", mSection.getCode());
        }
        // validate form end

        if (!errors.hasErrors()) {
            form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            form.setCreatedDate(new Timestamp(System.currentTimeMillis()));
            form.setStatusFlag(true);
            try {
                sectionService.insert(form);

                model.asMap().clear();
                return REDIRECT_TO_LIST + "?no=" + form.getmDepartment().getCurrentId();
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                model.addAttribute("errorMessage", "Gagal menambahkan Departemen - Kode " + form.getCode() + " Sudah Ada");
            }
        }
        model.addAttribute("mDepartment", form.getmDepartment());
        return PAGE_TAMBAH;
    }

    @GetMapping(REQUEST_MAPPING_EDIT)
    public String showPageEdit(Model model, @RequestParam(value = "no", required = true) String no) {
       /* byte[] decodedBytes = Base64.getDecoder().decode(no);
        String n1 = new String(decodedBytes);
        String[] array = n1.split("\\|", -1);

        MSection MSection = sectionService.findOne(array[1]);*/
        MSection MSection = sectionService.selectOne("id", no);
        if (MSection!= null){
            model.addAttribute("form", MSection);
            return PAGE_EDIT;
        }
        return REDIRECT_TO_LIST;
    }

    @PostMapping(REQUEST_MAPPING_EDIT)
    public String doProsesEdit(@ModelAttribute("form") MSection form, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
    	 MSection formSect = sectionService.selectOne("id", form.getId());
    	 form.setId(formSect.getCurrentId()); 
     	//form.setCode(formSect.getCode());    	 
    	//String codeExist = formSect.getCode();
    	 
    	// validate form
        FieldValidationUtil.required(errors, "id", formSect.getCurrentId(), "id");
        FieldValidationUtil.required(errors, "code", formSect.getCode(), "Kode Seksi");
        FieldValidationUtil.required(errors, "name", form.getName(), "Nama Seksi");
        // validate form end
        
        if (!errors.hasErrors()) {
            form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            form.setCreatedDate(new Timestamp(System.currentTimeMillis()));

            try {
                sectionService.saveOrUpdate(form);
                model.asMap().clear();
                return REDIRECT_TO_LIST + "?no=" + form.getmDepartment().getCurrentId();
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                model.addAttribute("form", form);
                model.addAttribute("errorMessage", "Gagal Mengubah Departemen - Kode " + form.getCode() + " Sudah Ada");
            }
        }

        return showPageEdit(model, form.getCurrentId());
    }
    
    @GetMapping(REQUEST_EXPORT_CLASS)
	public void doExportClass(HttpServletRequest request, HttpServletResponse response) {
		InputStream reportInputStream = null;

		String[] searchByArr = null;
		try {
			searchByArr = request.getParameter("searchByArr").split(",");
		} catch (Exception e) {
		}
		String[] keywordArr = null;
		try {
			keywordArr = request.getParameter("keywordArr").split(",");
		} catch (Exception e) {
		}
		String orderBy = request.getParameter("orderBy");
		String orderType = request.getParameter("orderType");

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
					if (searchBy.equalsIgnoreCase("desc")) {
						if (StringUtils.isNotBlank(value)) {
							searchCriteria.add(new KeyValue(searchBy, value, false));
						}
					} else if (StringUtils.isNotBlank(value)) {
						searchCriteria.add(new KeyValue(searchBy, value, true));
					}
				}
			}
		}

		if (orderBy != null) {
			orderBy = orderBy.trim();
			if (orderBy.equalsIgnoreCase("")) {
				orderBy = null;
			} else {
				switch (orderBy) {
					case "1" :
						orderBy = "code";
						break;
					case "2" :
						orderBy = "name";
						break;
					case "3" :
						orderBy = "createdBy.username";
						break;
					case "4" :
						orderBy = "statusFlag";
						break;
					case "5" :
						orderBy = "createdDate";
						break;
					default :
						orderBy = "code";
						break;
				}
			}
		}

		if (orderType == null) {
			orderType = "ASC";
		} else {
			orderType = orderType.trim();
			if (!orderType.equalsIgnoreCase("DESC")) {
				orderType = "ASC";
			}
		}

		try {
			
			reportInputStream = getClass().getClassLoader().getResourceAsStream("report/list-bagian.xls");

			List<MSection> dataList = new ArrayList<>();

			int totalRecord = (int) sectionService.countAll(searchCriteria);
			int retrieved = 0;
			while (retrieved < totalRecord) {
				List<MSection> retrievedDataList = sectionService.selectAll(searchCriteria, orderBy, orderType, retrieved, 1000);
				dataList.addAll(retrievedDataList);
				retrieved += retrievedDataList.size();
			}

			Context context = new Context();
			context.putVar("dataList", dataList);

			response.setContentType("application/vnd.ms-excel");
			response.setHeader("Content-Disposition", "attachment; filename=daftar-bagian.xls");

			JxlsHelper.getInstance().processTemplate(reportInputStream, response.getOutputStream(), context);
			response.getOutputStream().close();
			response.flushBuffer();
		} catch (Exception ex) {
			logger.error(ex);
		} finally {
			if (reportInputStream != null) {
				try {
					reportInputStream.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
