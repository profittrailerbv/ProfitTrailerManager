package com.profittrailer.interceptors;

import com.profittrailer.services.ProcessService;
import com.profittrailer.utils.StaticUtil;
import com.profittrailer.utils.Util;
import com.profittrailer.utils.constants.SessionType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class MainInterceptor extends HandlerInterceptorAdapter {

	private final ProcessService processService;

	public MainInterceptor(ProcessService processService) {
		this.processService = processService;
	}

	@Override
	public boolean preHandle(HttpServletRequest request,
	                         HttpServletResponse response,
	                         Object handler) throws IOException {

		// if caddy is enabled we only accept domain name access not ip
		if (processService.isCaddyEnabled() && StaticUtil.validIP(StaticUtil.getHost(request))) {
			return false;
		}

		if (StringUtils.isBlank(processService.getCaddyDomain())) {
			StaticUtil.url = StaticUtil.getBaseUrl(request);
		}

		if (processService.isDemoServer()) {
			return true;
		}

		if (StringUtils.isBlank(Util.loadPasswordHash())) {
			response.sendRedirect(StaticUtil.redirectUrl(request, "/resetPassword"));
			return false;
		}

		SessionType sessionType = (SessionType) request.getSession().getAttribute("sessionType");
		if (!SessionType.ADMIN.equals(sessionType)) {
			response.sendRedirect(StaticUtil.redirectUrl(request, "/login"));
			return false;
		}
		return true;
	}
}
