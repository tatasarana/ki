package com.docotel.ki.config;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.controller.HttpErrorController;
import com.docotel.ki.controller.LoginController;
import com.docotel.ki.controller.SertifikatController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {
	@Autowired
	private AuthenticationSuccessHandler authenticationSuccessHandler;

	@Autowired
	private AuthenticationFailureHandler authenticationFailureHandler;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserDetailsService userService;

	@Autowired
	private GlobalPasswordProvider globalPasswordProvider;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.antMatchers(
						"/aktivasi-user**",
						LoginController.PATH_FORGOT_PASSWORD + "**",
						LoginController.PATH_NEW_PASSWORD + "**",
						SertifikatController.PATH_VALIDATE + "**",
						SertifikatController.PATH_SERTIFIKAT_DIGITAL + "**",
						LoginController.PATH_LOGIN + "**",
						"/css/**", "/font/**", "/img/**", "/js/**",
						"/**",
						"/"
				).permitAll()
				.antMatchers(BaseController.PATH_AFTER_LOGIN + "**").hasAnyAuthority()
				.anyRequest().fullyAuthenticated()
				.and()
					.formLogin()
					.loginPage(LoginController.PATH_LOGIN)
					.successHandler(authenticationSuccessHandler)
					.failureHandler(authenticationFailureHandler)
					.permitAll()
				.and()
					.exceptionHandling().accessDeniedHandler(accessDeniedHandler())
				.and()
					.sessionManagement()
					.invalidSessionUrl(LoginController.PATH_LOGIN_ERROR_SESSION_EXPIRED)
			;
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userService).passwordEncoder(passwordEncoder);
		auth.authenticationProvider(globalPasswordProvider);
	}

	@Bean
	public AccessDeniedHandler accessDeniedHandler(){
		return new AccessDeniedHandler() {
			@Override
			public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
				if(e instanceof MissingCsrfTokenException || e instanceof InvalidCsrfTokenException) {
					response.sendRedirect(request.getServletContext().getContextPath() + LoginController.PATH_LOGIN_ERROR_ACCOUNT_EXPIRED);
				} else {
					request.setAttribute("url", request.getRequestURI());
					request.getRequestDispatcher(HttpErrorController.PATH_ACCESS_DENIED).forward(request, response);
				}
			}
		};
	}
}
