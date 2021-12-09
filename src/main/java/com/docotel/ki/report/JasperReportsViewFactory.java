package com.docotel.ki.report;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.view.jasperreports.AbstractJasperReportsSingleFormatView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsCsvView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsHtmlView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsPdfView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsXlsView;

public class JasperReportsViewFactory {

	protected static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";

	public AbstractJasperReportsSingleFormatView getJasperReportsView(HttpServletRequest httpServletRequest,
	        DataSource dataSource, String url, String format, String fileName){
	     String viewFormat = format==null?"pdf":format;
	   
	     // set possible content headers
	     Properties availableHeaders = new Properties();
	     availableHeaders.put("html", "inline; filename="+fileName+".html");
	     availableHeaders.put("csv", "inline; filename="+fileName+".csv");
	     availableHeaders.put("pdf", "inline; filename="+fileName+".pdf");
	     availableHeaders.put("xls", "inline; filename="+fileName+".xls");
	 
	     // get jasperView class based on the format supplied
	     // defaults to pdf
	     AbstractJasperReportsSingleFormatView jasperView = null;
	     if(viewFormat.equals("csv")) {
	         jasperView = new JasperReportsCsvView();
	     }else if(viewFormat.equals("html")){
	         jasperView = new JasperReportsHtmlView();
	     }else if(viewFormat.equals("xls")){
	         jasperView = new JasperReportsXlsView();
	     }else{
	         jasperView = new JasperReportsPdfView();
	     }
	 
	     // get appContext. required by the view
	     WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(
	              httpServletRequest.getSession().getServletContext());
	 
	     // set the appropriate content disposition header.
	     Properties headers = new Properties();
	     headers.put(HEADER_CONTENT_DISPOSITION, availableHeaders.get(viewFormat));
	 
	     // set the relevant jasperView properties
	     jasperView.setJdbcDataSource(dataSource);
	     jasperView.setUrl(url);
	     jasperView.setApplicationContext(ctx);
	     jasperView.setHeaders(headers);
	 
	     // return view
	     return jasperView;
	 }
	
	
}
