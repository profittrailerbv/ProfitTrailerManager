package com.profittrailer.application;

import com.profittrailer.interceptors.MainInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

	@Autowired
	@Order(0)
	MainInterceptor mainInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {

		registry.addInterceptor(mainInterceptor)
				.addPathPatterns("/", "/status", "/api/**")
				.excludePathPatterns("/api/*/login/**", "/api/*/resetPassword/**");
	}

}
