package com.profittrailer.utils;

import lombok.extern.log4j.Log4j2;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;

@Log4j2
public class StaticUtil {

	public static String url = null;
	public static String randomSystemId;

	static {
		try {
			if (StringUtils.isBlank(Util.loadPasswordHash())) {
				randomSystemId = RandomStringUtils.randomAlphanumeric(25);
			}
		} catch (IOException e) {
		}
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

	public static boolean isUpdateAvailable(String userVersion, String latestVersion) {
		String[] userVersionSplit = userVersion.split("-")[0].split("\\.");
		String[] latestVersionSplit = latestVersion.split("-")[0].split("\\.");

		int majorUserVersion = Integer.parseInt(userVersionSplit[0]);
		int minorUserVersion = Integer.parseInt(userVersionSplit[1]);
		int patchUserVersion = Integer.parseInt(userVersionSplit[2]);

		int majorLatestVersion = Integer.parseInt(latestVersionSplit[0]);
		int minorLatestVersion = Integer.parseInt(latestVersionSplit[1]);
		int patchLatestVersion = Integer.parseInt(latestVersionSplit[2]);

		if (majorUserVersion <= majorLatestVersion) {
			if (majorUserVersion < majorLatestVersion) {
				return true;
			} else {
				if (minorUserVersion <= minorLatestVersion) {
					if (minorUserVersion < minorLatestVersion) {
						return true;
					} else {
						return patchUserVersion < patchLatestVersion;
					}
				}
			}
		}

		return false;
	}

	public static void unzip(String sourceUrl) {
		String[] sourceSplitted = sourceUrl.split("/");
		String fileName = sourceSplitted[sourceSplitted.length - 1];
		String destination = "data/tmp" + File.separator + "update";
		File zipFileName = new File(destination + File.separator + fileName);
		File extractedDir = new File(destination + File.separator + fileName.replace(".zip", ""));
		File destinationDir = new File(destination);
		try {

			if (!zipFileName.exists()) {
				//first clear the update directory
				if (destinationDir.isDirectory() && !FileSystemUtils.deleteRecursively(destinationDir)) {
					log.error("Error deleting the destination directory {}", destination);
				}

				Files.createDirectories(destinationDir.toPath());

				URL github = new URL(sourceUrl);
				ReadableByteChannel rbc = Channels.newChannel(github.openStream());

				try (FileOutputStream fos = new FileOutputStream(zipFileName)) {
					fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				}
			}

			if (!extractedDir.exists()) {
				ZipFile zipFile = new ZipFile(zipFileName);
				zipFile.extractAll(destination);
			}
		} catch (Exception e) {
			log.error(e);
		}
	}
}
