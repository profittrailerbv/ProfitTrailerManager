package com.profittrailer.utils;

import com.google.gson.JsonObject;
import lombok.extern.log4j.Log4j2;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@Log4j2
public class StaticUtil {

	public static String url = null;
	public static String randomSystemId;
	public static Map<String, String> timeZones = new LinkedHashMap<>();
	public static long randomNumber = 0;

	static {
		try {
			if (StringUtils.isBlank(Util.loadPasswordHash())) {
				randomSystemId = RandomStringUtils.randomAlphanumeric(25);
			}
		} catch (IOException ignore) {
		}

		String[] ids = TimeZone.getAvailableIDs();
		for (String id : ids) {
			StaticUtil.timeZones.put(TimeZone.getTimeZone(id).getID(), displayTimeZone(TimeZone.getTimeZone(id)));
		}
	}

	public static String getHost(HttpServletRequest request) throws MalformedURLException {
		URL requestURL = new URL(request.getRequestURL().toString());
		return requestURL.getHost();
	}

	public static String getBaseUrl(HttpServletRequest request) throws MalformedURLException {

		HttpRequest httpRequest = new ServletServerHttpRequest(request);
		UriComponents uriComponents = UriComponentsBuilder.fromHttpRequest(httpRequest).build();
		String scheme = uriComponents.getScheme();
		return scheme + "://" + getHost(request);
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

	public static String unzip(String sourceUrl) {
		String[] sourceSplitted = sourceUrl.split("/");
		String fileName = sourceSplitted[sourceSplitted.length - 1];
		String destination = "data/tmp" + File.separator + "update";
		File zipFileName = new File(destination + File.separator + fileName);
		File extractedDir = new File(destination + File.separator + fileName.replace(".zip", ""));
		File destinationDir = new File(destination);
		String updateFolder = null;
		try {

			if (!zipFileName.exists()) {
				//first clear the update directory
				if (destinationDir.isDirectory() && !FileSystemUtils.deleteRecursively(destinationDir)) {
					log.error("Error deleting the destination directory {}", destination);
				}

				Files.createDirectories(destinationDir.toPath());

				URL github = new URL(sourceUrl);
				ReadableByteChannel rbc = Channels.newChannel(github.openStream());

				log.info("Downloading...");
				try (FileOutputStream fos = new FileOutputStream(zipFileName)) {
					fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				}
			}

			if (!extractedDir.exists()) {
				ZipFile zipFile = new ZipFile(zipFileName);
				zipFile.extractAll(destination);
			}
			updateFolder = fileName.replace(".zip", "");
		} catch (Exception e) {
			log.error(e);
		}

		return updateFolder;
	}

	public static void copyJar(String fileName, String botLocation) throws IOException {
		String destination = "data/tmp" + File.separator + "update";
		File updateFile = new File(destination + File.separator + fileName + File.separator + "ProfitTrailer.jar");
		File botDir = new File(botLocation);
		FileUtils.copyFileToDirectory(updateFile, botDir);
	}

	public static boolean validIP(String ip) {
		try {
			if (ip == null || ip.isEmpty()) {
				return false;
			}

			String[] parts = ip.split("\\.");
			if (parts.length != 4) {
				return false;
			}

			for (String s : parts) {
				int i = Integer.parseInt(s);
				if ((i < 0) || (i > 255)) {
					return false;
				}
			}
			return !ip.endsWith(".");
		} catch (NumberFormatException nfe) {
			return false;
		}
	}

	private static String displayTimeZone(TimeZone tz) {

		long hours = TimeUnit.MILLISECONDS.toHours(tz.getRawOffset());
		long minutes = TimeUnit.MILLISECONDS.toMinutes(tz.getRawOffset())
				- TimeUnit.HOURS.toMinutes(hours);
		// avoid -4:-30 issue
		minutes = Math.abs(minutes);

		String result;
		if (hours > 0) {
			result = String.format("%s (GMT+%d:%02d) ", tz.getID(), hours, minutes);
		} else if (hours < 0) {
			result = String.format("%s (GMT%d:%02d)", tz.getID(), hours, minutes);
		} else {
			result = String.format("%s (GMT)", tz.getID());
		}

		return result;

	}

	public static double extractTCV(JsonObject tcvData){
		double realBalance = tcvData.get("realBalance").getAsDouble();
		double tcv = tcvData.get("totalPairsCurrentValue").getAsDouble()
				+ tcvData.get("totalDCACurrentValue").getAsDouble()
				+ tcvData.get("totalPendingCurrentValue").getAsDouble()
				+ tcvData.get("totalDustCurrentValue").getAsDouble();
		double exchangeTcv = tcvData.get("totalExchangeCurrentValue").getAsDouble();
		if (exchangeTcv != 0) {
			tcv = exchangeTcv;
		}

		return realBalance + tcv;
	}

	public static double roundPercentage(double value) {
		return Math.round(value * 100) / 100.0;
	}

	public static boolean isVersionNewer(String latestVersion, String botVersion) {
		String[] latestVersionParts = latestVersion.split("[-.]");
		String[] botVersionParts = botVersion.split("[-.]");

		int length = Math.max(latestVersionParts.length, botVersionParts.length);

		for (int i = 0; i < length; i++) {
			int latestVersionPart = i < latestVersionParts.length ? NumberUtils.toInt(latestVersionParts[i]) : 0;
			int botVersionPart = i < botVersionParts.length ? NumberUtils.toInt(botVersionParts[i]) : 0;

			if (latestVersionPart != botVersionPart) {
				return latestVersionPart > botVersionPart;
			}
		}

		// IF we come here the version parts are identical. The part without a prerelease tag is newer
		if (latestVersionParts.length != botVersionParts.length) {
			return latestVersionParts.length < botVersionParts.length;
		}

		return false;
	}
}
