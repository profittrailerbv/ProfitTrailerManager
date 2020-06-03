package com.profittrailer.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class Util {

	private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

	public static DateTimeFormatter getDateFormatter() {
		return dateFormatter;
	}

	public static LocalDateTime getDateTime() {
//		return LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
		return LocalDateTime.now();
	}

	public static Long getDateTimeSeconds() {
//		return getDateTime().toEpochSecond(ZoneOffset.UTC);
		return getDateTime().toEpochSecond(ZoneOffset.of(ZoneOffset.systemDefault().getId()));
	}

	public static String simpleHash(String input) throws NoSuchAlgorithmException {
		String result = input;
		String salt = "PTMANAGERSE123CRET56342";
		if (StringUtils.isNotBlank(input)) {
			input += salt;
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(input.getBytes(Charset.defaultCharset()));
			BigInteger hash = new BigInteger(1, md.digest());
			StringBuilder resultBuilder = new StringBuilder(hash.toString(16));
			while (resultBuilder.length() < 32) {
				resultBuilder.insert(0, "0");
			}
			result = resultBuilder.toString();
		}
		return result;
	}

	public static String loadPasswordHash() throws IOException {
		File passwdFile = new File("data/passwd");

		String hashedPassword = "";
		if (passwdFile.exists()) {
			hashedPassword = FileUtils.readFileToString(passwdFile, StandardCharsets.UTF_8);
		}

		return hashedPassword;
	}

	public static boolean createPassword(String password) throws IOException, NoSuchAlgorithmException {
		File passwordFile = new File("data/passwd");

		FileUtils.deleteQuietly(passwordFile);
		if (passwordFile.createNewFile()) {
			FileUtils.writeStringToFile(passwordFile, simpleHash(password), StandardCharsets.UTF_8);
			return true;
		}

		return false;
	}
}