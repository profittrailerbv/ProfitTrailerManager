package com.profittrailer.interceptors;

import com.profittrailer.utils.StaticUtil;
import com.profittrailer.utils.constants.SessionType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;

@Component
public class MainInterceptor extends HandlerInterceptorAdapter {

	@Override
	public boolean preHandle(HttpServletRequest request,
	                         HttpServletResponse response,
	                         Object handler) throws IOException {

		SessionType sessionType = (SessionType) request.getSession().getAttribute("sessionType");
		if (!SessionType.ADMIN.equals(sessionType)) {
			//response.sendRedirect(StaticUtil.redirectUrl(request, "/login"));
		}
		StaticUtil.url = StaticUtil.getBaseUrl(request);
		return true;
	}
}
