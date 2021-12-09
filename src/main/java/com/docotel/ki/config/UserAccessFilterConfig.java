package com.docotel.ki.config;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.filter.UserAccessFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserAccessFilterConfig {

	@Bean
	public FilterRegistrationBean filterRegistrationBean(){
		FilterRegistrationBean registrationBean = new FilterRegistrationBean();

		registrationBean.setFilter(new UserAccessFilter());
		registrationBean.addUrlPatterns(BaseController.PATH_AFTER_LOGIN + "/*");

		return registrationBean;
	}
}
