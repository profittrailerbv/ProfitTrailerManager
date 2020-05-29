package com.profittrailer.utils;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;

public class StaticUtil {

	public static String url = null;
	public static String defaultPassword;

	static {
		defaultPassword = RandomStringUtils.randomAlphanumeric(25);
		//If password file exists, load the password
	}
	public static String getBaseUrl(HttpServletRequest request) throws MalformedURLException {

		HttpRequest httpRequest = new ServletServerHttpRequest(request);
		UriComponents uriComponents = UriComponentsBuilder.fromHttpRequest(httpRequest).build();

		String scheme = uriComponents.getScheme();
		URL requestURL = new URL(request.getRequestURL().toString());
		return scheme + "://" + requestURL.getHost();
	}

	public static String getBaseUrlWithPort(HttpServletRequest request) throws MalformedURLException {

		HttpRequest httpRequest = new ServletServerHttpRequest(request);
		UriComponents uriComponents = UriComponentsBuilder.fromHttpRequest(httpRequest).build();

		String scheme = uriComponents.getScheme();
		URL requestURL = new URL(request.getRequestURL().toString());
		String port = requestURL.getPort() == -1 ? "" : ":" + requestURL.getPort();
		return scheme + "://" + requestURL.getHost() + port + request.getContextPath();
	}

	public static String redirectUrl(HttpServletRequest request, String path) {
		try {
			return getBaseUrlWithPort(request) + StringUtils.stripEnd(path, "/");
		} catch (Exception e) {
			return path;
		}
	}

	public static boolean isUnix() {
		String os = System.getProperty("os.name").toLowerCase();
		return os.contains("nix")
				|| os.contains("nux")
				|| os.contains("mac os x");
	}

	public static long minutesLeft(long currentTime, long untilTime) {
		long seconds = untilTime - currentTime;
		if (seconds > 0 && seconds < 60) {
			return 1;
		} else if (seconds > 0) {
			return (seconds / 60) + 1;
		}
		return 0;
	}
}
