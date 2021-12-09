package com.docotel.ki.filter;

import com.docotel.ki.model.master.MUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class UserAccessFilter implements Filter {
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();

			MUser mUser = (MUser) auth.getPrincipal();

			String requestUri = request.getRequestURI();
			String contextPath = request.getContextPath();

			if (requestUri.startsWith(contextPath)) {
				requestUri = requestUri.replaceFirst(contextPath, "");
			}

			ServletContext servletContext = servletRequest.getServletContext();
			List<String> protectedUrlList = (List<String>) servletContext.getAttribute("protectedUrlList");

			if (protectedUrlList != null && protectedUrlList.contains(requestUri)) {
				if (!mUser.hasAccessUrl(requestUri)) {
					throw new AccessDeniedException(request.getRequestURI());
				}
			}

			filterChain.doFilter(servletRequest, servletResponse);
		} catch (ClassCastException e) {
			HttpServletResponse response = (HttpServletResponse) servletResponse;
			response.sendRedirect(request.getContextPath());
		}
	}

	@Override
	public void destroy() {

	}
}
