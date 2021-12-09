package com.docotel.ki.base;

import com.docotel.ki.util.ObjectMapperUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

public abstract class BaseController implements ServletContextAware {
	protected final Logger logger = LogManager.getLogger(this.getClass());
	protected ServletContext servletContext;

	public static final String PATH_AFTER_LOGIN = "/layanan";

	@Autowired
	private MessageSource messageSource;

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	protected String getMessage(String msgKey) {
		return this.getMessage(msgKey, (Object[])null);
	}

	protected String getMessage(String msgKey, Object... args) {
		return this.getMessage(LocaleContextHolder.getLocale(), msgKey, args);
	}

	protected String getMessage(Locale locale, String msgKey, Object... args) {
		return this.messageSource.getMessage(msgKey, args, locale);
	}

	protected String getPathURL(String path) {
		return this.servletContext.getContextPath() + path;
	}

	protected String getPathURLAfterLogin(String path) {
		return getPathURL(PATH_AFTER_LOGIN + path);
	}

	protected boolean isAjaxRequest(HttpServletRequest request) {
		String ajaxHeader = request.getHeader("X-Requested-With");
		return ajaxHeader != null && "XMLHttpRequest".equalsIgnoreCase(ajaxHeader);
	}

	protected void setResponseAsJson(HttpServletResponse response) {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
	}

	protected void writeJsonResponse(HttpServletResponse response, Object object) {
		try {
			response.getWriter().write(ObjectMapperUtil.toJson(object));
		} catch (IOException var4) {
			logger.error(var4.getMessage(), var4);
		}
	}

	protected void setResponseAs(HttpServletResponse response, String mimeType) {
		response.setContentType(mimeType);
	}
	protected ServletContext getServletContext() {
		return this.servletContext;
	}
}
