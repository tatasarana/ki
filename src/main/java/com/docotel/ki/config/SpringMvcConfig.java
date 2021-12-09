package com.docotel.ki.config;

import com.docotel.ki.util.NumberUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.thymeleaf.extras.conditionalcomments.dialect.ConditionalCommentsDialect;

@Configuration
public class SpringMvcConfig extends WebMvcConfigurationSupport {
	@Value("${spring.resources.cache-period:1}")
	private String cachePeriod;

	

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		int cachePeriod = 0;
		try {
			cachePeriod = NumberUtil.parseInteger(this.cachePeriod).intValue();
		} catch (NumberFormatException e) {
		}
		registry.addResourceHandler(new String[]{"/css/**"}).setCachePeriod(cachePeriod).addResourceLocations(new String[]{"classpath:/static/css/"});
		registry.addResourceHandler(new String[]{"/doc/**"}).setCachePeriod(cachePeriod).addResourceLocations(new String[]{"classpath:/static/doc/"});
		registry.addResourceHandler(new String[]{"/font/**"}).setCachePeriod(cachePeriod).addResourceLocations(new String[]{"classpath:/static/font/"});
		registry.addResourceHandler(new String[]{"/img/**"}).setCachePeriod(cachePeriod).addResourceLocations(new String[]{"classpath:/static/img/"});
		registry.addResourceHandler(new String[]{"/js/**"}).setCachePeriod(cachePeriod).addResourceLocations(new String[]{"classpath:/static/js/"});
	}

	@Bean
	public ConditionalCommentsDialect conditionalCommentDialect() {
		return new ConditionalCommentsDialect();
	}
}
