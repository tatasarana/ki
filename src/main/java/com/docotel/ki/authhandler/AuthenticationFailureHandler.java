package com.docotel.ki.authhandler;

import com.docotel.ki.component.CantAccessException;
import com.docotel.ki.controller.LoginController;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component("authenticationFailureHandler")
public class AuthenticationFailureHandler implements org.springframework.security.web.authentication.AuthenticationFailureHandler {
	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
	
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException authExc) throws IOException, ServletException {
        if (authExc instanceof LockedException) {
            redirectStrategy.sendRedirect(request, response, LoginController.PATH_LOGIN_ERROR_ACCOUNT_LOCKED);
        } else if (authExc instanceof DisabledException) {
            redirectStrategy.sendRedirect(request, response, LoginController.PATH_LOGIN_ERROR_ACCOUNT_DISABLED);
        } else if (authExc instanceof CredentialsExpiredException) {
            redirectStrategy.sendRedirect(request, response, LoginController.PATH_LOGIN_ERROR_CREDENTIAL_EXPIRED);
        } else if (authExc instanceof AccountExpiredException) {
            redirectStrategy.sendRedirect(request, response, LoginController.PATH_LOGIN_ERROR_ACCOUNT_EXPIRED);
        } else if (authExc.getCause() instanceof CantAccessException) {
            redirectStrategy.sendRedirect(request, response, LoginController.PATH_LOGIN_ERROR_WEEKEND);
        } else {
            redirectStrategy.sendRedirect(request, response, LoginController.PATH_LOGIN_ERROR_ACCOUNT_INVALID);
        }
	}
	
}