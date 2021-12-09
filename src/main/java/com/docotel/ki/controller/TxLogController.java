package com.docotel.ki.controller;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.transaction.TxLogHistory;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.service.master.SectionService;
import com.docotel.ki.service.transaction.AuditTrailService;
import com.docotel.ki.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.persistence.Table;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class TxLogController extends BaseController{

    @Autowired private AuditTrailService auditTrailService;
    @Autowired private SectionService sectionService;
    
    private static final String DIRECTORY_PAGE = "master/log/";

    private static final String PAGE_LIST = DIRECTORY_PAGE + "list-log";
    private static final String PAGE_DETAIL = DIRECTORY_PAGE + "detail-log";

    private static final String PATH_LIST = "/list-log";
    private static final String PATH_DETAIL = "/detail-log";
    
    private static final String PATH_AJAX_LIST = "/cari-log";
    
    private static final String REQUEST_MAPPING_AJAX_LIST = PATH_AJAX_LIST + "*";
    
    private static final String REDIRECT_LIST = "redirect:" + PATH_AFTER_LOGIN + PATH_LIST;

    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "listLog");
    }

    @RequestMapping(path = PATH_LIST)
    public String showPageList(@RequestParam(value = "error", required = false) String error, final Model model) {
    	if (StringUtils.isNotBlank(error)) {
			model.addAttribute("errorMessage", error);
		}
        return PAGE_LIST;
    }
    
    @RequestMapping(path = PATH_DETAIL)
    public String showPageDetail(final Model model, final HttpServletRequest request, final HttpServletResponse response) throws ClassNotFoundException {
    	String id = request.getParameter("id");
    	TxLogHistory txLogHistory = auditTrailService.findOneById(id);
    	
    	if (txLogHistory != null) {
	    	Class<?> cls = Class.forName(txLogHistory.getObjectClassName());
	        Table table = cls.getAnnotation(Table.class);
	        String tableName = table.name();
	        
	        String oldData[] = txLogHistory.getOldData() == null ? null : txLogHistory.getOldData().split("\\^ ");
	        String newData[] = txLogHistory.getNewData() == null ? null : txLogHistory.getNewData().split("\\^ ");
	        
	        Map<Integer, String[]> dataLog = new HashMap<>();
	        int total = oldData != null ? oldData.length : newData.length;
	        for (int i = 0; i < total; i++) {
	        	String dataField = oldData == null ? newData[i].substring(0, newData[i].indexOf(":")) : oldData[i].substring(0, oldData[i].indexOf(":"));
	        	String dataOld = oldData == null ? " - " : oldData[i].substring(oldData[i].indexOf(":")+1);
	        	String dataNew = newData == null ? " - " : newData[i].substring(newData[i].indexOf(":")+1);
	        	
	        	dataLog.put(i+1, new String[]{dataField.trim(), dataOld.trim(), dataNew.trim()});
	        }
	        
	        model.addAttribute("activityDate", DateUtil.formatDate(txLogHistory.getActivityDate(), "dd-MM-yyyy HH:mm:ss"));
	        model.addAttribute("activity", txLogHistory.getActivity());
	        model.addAttribute("tableName", tableName);
	        model.addAttribute("userName", txLogHistory.getUserName());
	        model.addAttribute("name", txLogHistory.getName());
	        model.addAttribute("section", txLogHistory.getSection()!=null? sectionService.selectOne("id",  txLogHistory.getSection()).getName() : "");
	        model.addAttribute("dataLog", dataLog);
	        return PAGE_DETAIL;
    	}
    	
    	return REDIRECT_LIST + "?error=Log History tidak ditemukan";
    }

    @PostMapping(value = REQUEST_MAPPING_AJAX_LIST)
    public void doGetListDataTables(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ClassNotFoundException {
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
                        if (searchBy.equalsIgnoreCase("activityDate")) {
                            try {
                                searchCriteria.add(new KeyValue(searchBy, DateUtil.toDate("dd/MM/yyyy", value), true));
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

            String orderBy = request.getParameter("order[0][column]");
            if (orderBy != null) {
                orderBy = orderBy.trim();
                if (orderBy.equalsIgnoreCase("")) {
                    orderBy = null;
                } else {
                    switch (orderBy) {
                        case "2":
                            orderBy = "activityDate";
                            break;
                        case "3":
                            orderBy = "userName";
                            break;
                        case "4":
                            orderBy = "name";
                            break;
                        case "5":
                            orderBy = null;
                            break;
                        case "6":
                            orderBy = "tableName";
                            break;
                        case "7":
                            orderBy = "activity";
                            break;
                        default:
                            orderBy = null;
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

            GenericSearchWrapper<TxLogHistory> searchResult = auditTrailService.searchGeneral(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {            	            	
            	
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());               
                int no = offset;
                
                for (TxLogHistory result : searchResult.getList()) {
                	String sectionName = "";
                	if(result.getSection()!=null) {
                		sectionName = sectionService.selectOne("id", result.getSection()).getName();
                	}                	 
                	
                    no++;
                    data.add(new String[]{
                            "" + no,
                            result.getId(),
                            DateUtil.formatDate(new Date(result.getActivityDate().getTime()), "dd-MM-yyyy HH:mm:ss"),
                            result.getUserName(),
                            result.getName(),                           
                            sectionName,                          
                            result.getTableName(),
                            result.getActivity(),
                            ((result.getOldData() == null) ? "-" : result.getOldData()),
                            ((result.getNewData() == null) ? "-" : result.getNewData()),
                    });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }
}
