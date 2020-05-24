package com.profittrailer.utils;

import org.apache.commons.lang3.RandomStringUtils;
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

	public static boolean isUnix() {
		String os = System.getProperty("os.name").toLowerCase();
		return os.contains("nix")
				|| os.contains("nux")
				|| os.contains("mac os x");
	}

}
