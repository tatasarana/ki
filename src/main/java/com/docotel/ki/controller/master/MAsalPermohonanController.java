package com.docotel.ki.controller.master;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.MFileSequence;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.service.master.FileSeqService;
import com.docotel.ki.util.FieldValidationUtil;
import org.apache.commons.lang3.StringUtils;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class MAsalPermohonanController extends BaseController {

    @Autowired
    FileSeqService fileSeqService;

    private static final String DIRECTORY_PAGE = "master/asal-permohonan/";

    private static final String PAGE_LIST = DIRECTORY_PAGE + "list-asal-permohonan";
    private static final String PAGE_TAMBAH = DIRECTORY_PAGE + "tambah-asal-permohonan";
    private static final String PAGE_EDIT = DIRECTORY_PAGE + "edit-asal-permohonan";

    private static final String PATH_LIST = "/list-asal-permohonan";
    private static final String PATH_EDIT = "/edit-asal-permohonan";

    private static final String PATH_AJAX_LIST = "/cari-asal-permohonan";
    private static final String PATH_TAMBAH = "/tambah-asal-permohonan";

    private static final String REQUEST_MAPPING_AJAX_LIST = PATH_AJAX_LIST + "*";
    private static final String REQUEST_MAPPING_TAMBAH = PATH_TAMBAH + "*";
    private static final String REQUEST_MAPPING_EDIT = PATH_EDIT + "*";

    private static final String REDIRECT_TO_LIST = "redirect:" + PATH_AFTER_LOGIN + PATH_LIST;


    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "master");
        model.addAttribute("subMenu", "listAsalPermohonan");

        if (request.getRequestURI().contains(PATH_TAMBAH)) {
            if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
                model.addAttribute("form", new MFileSequence());
            }
        }
    }

    @RequestMapping(path = PATH_LIST)
    public String showPageList(@RequestParam(value = "error", required = false) String error,Model model) {
        
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
							orderBy = "code";
							break;
						case "2" :
							orderBy = "desc";
							break;
						case "3" :
							orderBy = "createdBy.username";
							break;
						case "4" :
							orderBy = "kanwilFlag";
							break;
						case "5" :
							orderBy = "statusFlag";
							break;
						case "6" :
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

            GenericSearchWrapper<MFileSequence> searchResult = fileSeqService.searchGeneral(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (MFileSequence result : searchResult.getList()) {
                    Date date = new Date(result.getCreatedDate().getTime());
                    String pDate = new SimpleDateFormat("dd-MM-yyyy").format(date);
                    String stts = ((result.isStatusFlag() == true) ? "Aktif" : "Tidak Aktif");
                    String kanwil = ((result.isKanwilFlag() == true) ? "Ya" : "Tidak");
                    
                    // For user role access button menu
                    MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    String button = "";
                    if(mUser.hasAccessMenu("T-UAP")) {
                    	button = "<div class=\"btn-actions\">"
                                + "<a class=\"btn btn-warning btn-xs\" href=\"" + getPathURLAfterLogin(PATH_EDIT + "?no=" + result.getId()) + "\">Ubah</a>"
                                + "</div>";
                    }
                    no++;
                    data.add(new String[]{
                            "" + no,
                            result.getCode(),
                            result.getDesc(),
                            kanwil,
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
    public String doProsesTambah(@ModelAttribute("form") MFileSequence form, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
        // validate form
        FieldValidationUtil.required(errors, "code", form.getCode(), "code");
        FieldValidationUtil.required(errors, "desc", form.getDesc(), "desc");
        // validate form end

        if (!errors.hasErrors()) {
            form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            form.setKanwilFlag(form.isKanwilFlag());
            form.setStatusFlag(true);

            try {
                form.setCode(form.getCode().toUpperCase());
                fileSeqService.insert(form);

                model.asMap().clear();
                return REDIRECT_TO_LIST;
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                model.addAttribute("errorMessage", "Gagal menambahkan Asal Permohonan - Kode " + form.getCode() + " Sudah Ada");
            }
        }

        return PAGE_TAMBAH;
    }

    @GetMapping(REQUEST_MAPPING_EDIT)
    public String showPageEdit(Model model, @RequestParam(value = "no", required = true) String no) {
        MFileSequence mFileSequence = fileSeqService.findOne(no);
        if (mFileSequence!= null){
            model.addAttribute("form", mFileSequence);
            return PAGE_EDIT;
        }
        return REDIRECT_TO_LIST+ "?error=Data Asal Permohonan tidak ditemukan";
    }

    @PostMapping(REQUEST_MAPPING_EDIT)
    public String doProsesEdit(@ModelAttribute("form") MFileSequence form, final Model model, final BindingResult errors, final HttpServletRequest request, final HttpServletResponse response) {
        //MFileSequence existing = fileSeqService.findOne(form.getId());
        //form.setCode(existing.getCode());

        // validate form
        FieldValidationUtil.required(errors, "id", form.getCurrentId(), "id");
        FieldValidationUtil.required(errors, "code", form.getCode(), "code");
        FieldValidationUtil.required(errors, "desc", form.getDesc(), "desc");
        // validate form end
        
        MFileSequence existing = fileSeqService.findByCode(form.getCode());
        
        if (!errors.hasErrors()) {
            form.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            try {
                fileSeqService.saveOrUpdate(form);

                model.asMap().clear();
                return REDIRECT_TO_LIST;
            } catch (DataIntegrityViolationException e) {
                logger.error(e.getMessage(), e);
                model.addAttribute("form", existing);
                model.addAttribute("errorMessage", "Gagal Mengubah Asal Permohonan - Kode " + form.getCode() + " sudah ada");
            }
        }
        return showPageEdit(model, form.getCurrentId()); 
        //return PATH_EDIT + "?no=" + form.getCurrentId();
    }

}
