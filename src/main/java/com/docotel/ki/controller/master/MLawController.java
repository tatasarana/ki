package com.docotel.ki.controller.master;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.MLaw;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.service.master.LawService;
import com.docotel.ki.util.FieldValidationUtil;
import org.apache.commons.lang3.StringUtils;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class MLawController extends BaseController {
    @Autowired
    LawService lawService;

    private static final String DIRECTORY_PAGE = "master/law/";

    private static final String PAGE_LIST = DIRECTORY_PAGE + "list-law";
    private static final String PAGE_TAMBAH = DIRECTORY_PAGE + "tambah-law";
    private static final String PAGE_EDIT = DIRECTORY_PAGE + "edit-law";

    private static final String PATH_LIST = "/list-undang-undang";
    private static final String PATH_EDIT = "/edit-undang-undang";

    private static final String PATH_AJAX_LIST = "/cari-law";
    private static final String PATH_TAMBAH = "/tambah-undang-undang";
	private static final String PATH_EXPORT_CLASS = "/ekspor-law";

    private static final String REQUEST_MAPPING_AJAX_LIST = PATH_AJAX_LIST + "*";
    private static final String REQUEST_MAPPING_TAMBAH = PATH_TAMBAH + "*";
    private static final String REQUEST_MAPPING_EDIT = PATH_EDIT + "*";

	private static final String REQUEST_EXPORT_CLASS = PATH_EXPORT_CLASS + "*";
    private static final String REDIRECT_TO_LIST = "redirect:" + PATH_AFTER_LOGIN + PATH_LIST;

    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "master");
        model.addAttribute("subMenu", "listUndangUndang");

        if (request.getRequestURI().contains(PATH_TAMBAH)) {
            if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
                model.addAttribute("form", new MLaw());
            }
        }
    }

    @RequestMapping(path = PATH_LIST)
    public String showPageList(@RequestParam(value = "error", required = false) String error, Model model) {
        List<MLaw> mLaws = lawService.findAll();
        model.addAttribute("mLaws", mLaws);

        if (StringUtils.isNotBlank(error)) {
			model.addAttribute("errorMessage", error);
		}
        
        return PAGE_LIST;
    }

    @PostMapping(value = REQUEST_MAPPING_AJAX_LIST)
    public void doGetListDataTables(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
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

            String orderBy = request.getParameter("order[0][column]");
			if (orderBy != null) {
				orderBy = orderBy.trim();
				if (orderBy.equalsIgnoreCase("")) {
					orderBy = null;
				} else {
					switch (orderBy) {
						case "1" :
							orderBy = "no";
							break;
						case "2" :
							orderBy = "year";
							break;
						case "3" :
							orderBy = "desc";
							break;
						case "4" :
							orderBy = "createdBy.username";
							break;
						case "5" :
							orderBy = "statusFlag";
							break;
						case "6" :
							orderBy = "createdDate";
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

            GenericSearchWrapper<MLaw> searchResult = lawService.searchGeneral(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (MLaw result : searchResult.getList()) {
                    Date date = new Date(result.getCreatedDate().getTime());
                    String pDate = new SimpleDateFormat("dd-MM-yyyy").format(date);
                    String stts = ((result.isStatusFlag() == true) ? "Aktif" : "Tidak Aktif");
                    
                    // For user role access button menu
                    MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    String button = "";
                    if(mUser.hasAccessMenu("T-UUG")) {
                    	button = "<div class=\"btn-actions\">"
                                + "<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT + "?no=" + result.getId()) + "\">Ubah</a>"
                                + "</div>";
                    }
                    no++;
                    data.add(new String[]{
                            "" + no,
                            String.valueOf(result.getNo()),
                            String.valueOf(result.getYear()),
                            result.getDesc(),
                            stts,
                            result.getCreatedBy().getUsername(),
                            pDate,
                            button
                            /*"<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT + "?no=" + result.getId()) + "\">Ubah</a>"*/
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
    public String showPageTambah(Model model) {
        return PAGE_TAMBAH;
    }

    @PostMapping(REQUEST_MAPPING_TAMBAH)
    public String doProsesTambah(@ModelAttribute("form") MLaw form, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
        // validate form
        FieldValidationUtil.required(errors, "no", form.getNo(), "no");

        FieldValidationUtil.required(errors, "year", form.getYear(), "year");

        FieldValidationUtil.required(errors, "desc", form.getDesc(), "desc");
        // validate form end

        //MFileSequence existing = fileSeqService.findByCode(form.getCode());
        
        List<KeyValue> searchCriteria = new ArrayList<>();
        searchCriteria.add(new KeyValue("no", form.getNo(), true));
        searchCriteria.add(new KeyValue("year", form.getYear(), true));
        
        MLaw existing = lawService.selectOne(searchCriteria);	  		
    	if(existing!=null) {    		        		    		
    		model.addAttribute("form", existing);
    		model.addAttribute("errorMessage", "Undang-undang No " + form.getNo() + " dan Tahun " + form.getYear() + " sudah ada");
    		return PAGE_EDIT;
    	}
        
        
        if (!errors.hasErrors()) {
            form.setStatusFlag(true);
            form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

            try {
                lawService.insert(form);

                model.asMap().clear();
                return REDIRECT_TO_LIST;
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                model.addAttribute("errorMessage", "Gagal menambahkan undang-undang");
            }
        }

        return PAGE_TAMBAH;
    }

    @GetMapping(REQUEST_MAPPING_EDIT)
    public String showPageEdit(Model model, @RequestParam(value = "no", required = true) String no) {
        MLaw mLaw = lawService.GetMlawById(no);
        if (mLaw != null){
            model.addAttribute("form", mLaw);
            return PAGE_EDIT;
        }
        return REDIRECT_TO_LIST+ "?error=Data Dasar Hukum tidak ditemukan";
    }

    @PostMapping(REQUEST_MAPPING_EDIT)
    public String doProsesEdit(@ModelAttribute("form") MLaw form, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
        // validate form
        FieldValidationUtil.required(errors, "id", form.getCurrentId(), "no");
        FieldValidationUtil.required(errors, "no", form.getNo(), "no");
        FieldValidationUtil.required(errors, "year", form.getYear(), "year");
        FieldValidationUtil.required(errors, "desc", form.getDesc(), "desc");
        
        if (!errors.hasErrors()) {
            form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            try {
                lawService.saveOrUpdate(form);

                model.asMap().clear();
                return REDIRECT_TO_LIST;
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                model.addAttribute("errorMessage", "Gagal Mengubah undang-undang " + form.getNo() + " Sudah Ada");
                model.addAttribute("form", form);  	
            }
        }
        return showPageEdit(model, form.getCurrentId()); 
        //return PAGE_EDIT + "?no=" + form.getCurrentId();
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
						orderBy = "no";
						break;
					case "2" :
						orderBy = "year";
						break;
					case "3" :
						orderBy = "desc";
						break;
					case "4" :
						orderBy = "createdBy.username";
						break;
					case "5" :
						orderBy = "statusFlag";
						break;
					case "6" :
						orderBy = "createdDate";
						break;
					default :
						orderBy = "no";
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
			
			reportInputStream = getClass().getClassLoader().getResourceAsStream("report/list-law.xls");

			List<MLaw> dataList = new ArrayList<>();

			int totalRecord = (int) lawService.countAll(searchCriteria);
			int retrieved = 0;
			while (retrieved < totalRecord) {
				List<MLaw> retrievedDataList = lawService.selectAll(searchCriteria, orderBy, orderType, retrieved, 1000);
				dataList.addAll(retrievedDataList);
				retrieved += retrievedDataList.size();
			}

			Context context = new Context();
			context.putVar("dataList", dataList);

			response.setContentType("application/vnd.ms-excel");
			response.setHeader("Content-Disposition", "attachment; filename=daftar-law.xls");

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

