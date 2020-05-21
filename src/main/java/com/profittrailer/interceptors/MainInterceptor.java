package com.profittrailer.interceptors;

import com.profittrailer.utils.StaticUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;

@Component
public class MainInterceptor extends HandlerInterceptorAdapter {

	@Override
	public boolean preHandle(HttpServletRequest request,
	                         HttpServletResponse response,
	                         Object handler) throws MalformedURLException {

		StaticUtil.url = StaticUtil.getBaseUrl(request);
		return true;
	}
}
