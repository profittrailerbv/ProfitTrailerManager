package com.profittrailer.controllers;

import static com.profittrailer.utils.Util.getDateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.profittrailer.models.BotInfo;
import com.profittrailer.services.ProcessService;
import com.profittrailer.utils.BotInfoSerializer;
import com.profittrailer.utils.StaticUtil;
import com.profittrailer.utils.Util;
import com.profittrailer.utils.constants.SessionType;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Locale;

@Log4j2
@RestController
@RequestMapping("/api/v1")
public class ApiController {

	@Autowired
	private ProcessService processService;
	private Gson parser;
	private boolean onlyManaged = true;

	private static int failedAttempts;
	private static LocalDateTime timeout = getDateTime();

	@PostConstruct
	public void init() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(BotInfo.class, new BotInfoSerializer());
		parser = gsonBuilder.create();
	}

	@GetMapping("/data")
	public String data(HttpServletRequest request) throws MalformedURLException {

		JsonObject object = new JsonObject();
		object.add("bots", parser.toJsonTree(processService.getBotList(onlyManaged)));
		object.addProperty("baseUrl", StaticUtil.getBaseUrl(request));
		object.addProperty("demoServer", processService.isDemoServer());

		return object.toString();
	}

	@PostMapping("/restartBot")
	public void startBot(String directoryName) throws InterruptedException {
		if (processService.isDemoServer()) {
			return;
		}
		processService.stopBot(directoryName);
		Thread.sleep(5000);
		processService.startBot(directoryName);

	}

	@PostMapping("/stopBot")
	public void stopBot(String directoryName) {
		if (processService.isDemoServer()) {
			return;
		}
		processService.stopBot(directoryName);
	}

	@GetMapping("/status")
	public String checkStatus(String directoryName) {
		JsonObject object = new JsonObject();
		object.add("bot", parser.toJsonTree(processService.getBotInfoMap().get(directoryName)));

		return object.toString();
	}

	@GetMapping("/toggleCards")
	public String getToggle() {
		JsonObject object = new JsonObject();
		object.addProperty("onlyManaged", onlyManaged);
		return object.toString();
	}

	@PostMapping("/toggleCards")
	public String postToggle() {
		if (!processService.isDemoServer()) {
			onlyManaged = !onlyManaged;
		}
		JsonObject object = new JsonObject();
		object.addProperty("onlyManaged", onlyManaged);
		return object.toString();
	}

	@PostMapping("/shutdown")
	public void shutdown() {
		if (processService.isDemoServer()) {
			return;
		}
		System.exit(0);
	}

	@GetMapping("/toggleData")
	public String toggleData() {

		JsonObject object = new JsonObject();
		object.addProperty("latestVersion", processService.getLatestVersion());
		object.addProperty("downloadUrl", processService.getDownloadUrl());
		object.addProperty("demoServer", processService.isDemoServer());

		return object.toString();
	}

	@PostMapping("/updateBots")
	public void updateBots(String forceUrl,
	                       @RequestParam(defaultValue = "false") boolean forceUpdate) {
		if (processService.isDemoServer()) {
			return;
		}
		new Thread(() -> {
			for (String dir : processService.getBotInfoMap().keySet()) {
				try {
					BotInfo botInfo = processService.getBotInfoMap().get(dir);
					processService.updateBot(botInfo, forceUrl, forceUpdate, false);
				} catch (IOException | InterruptedException e) {
					log.error(e);
				}
			}
		}).start();
	}

	@GetMapping("/linkout")
	public void linkOut(String directoryName,
	                    HttpServletResponse response) throws Exception {
		JsonObject object = new JsonObject();
		object.add("bot", parser.toJsonTree(processService.getBotInfoMap().get(directoryName)));

		BotInfo botInfo = processService.getBotInfoMap().get(directoryName);
		String token = processService.getSSOKey(directoryName);
		String redirectUrl = processService.createUrl(botInfo, "?sso=" + token, false);
		response.sendRedirect(redirectUrl);
	}

	@PostMapping("/login")
	public void login(String password,
	                      HttpServletRequest request,
	                      HttpServletResponse response) throws IOException, NoSuchAlgorithmException {

		if (timeout.isAfter(getDateTime())) {
			String formattedTime = timeout.format(Util.getDateFormatter());
			response.sendError(HttpStatus.UNAUTHORIZED.value(), String.format("Locked out until %s", formattedTime));
			return;
		}

		String hashedPassword = Util.simpleHash(password);

		String currentPassword = Util.loadPasswordHash();

		if (hashedPassword.equals(currentPassword)) {
			request.getSession(true).setAttribute("sessionType", SessionType.ADMIN);
			return;
		}

		failedAttempts++;
		if (failedAttempts % 3 == 0) {
			timeout = getDateTime().plusMinutes((failedAttempts / 3) * 5);
		}

		response.sendError(HttpStatus.UNAUTHORIZED.value(), "Incorrect password");

	}

	@PostMapping("/resetPassword")
	public void resetPassword(String randomSystemId,
	                          String password,
	                          String passwordConfirm,
	                          HttpServletResponse response,
	                          HttpServletRequest request) throws IOException, NoSuchAlgorithmException {

		if (!randomSystemId.equals(StaticUtil.randomSystemId)) {
			response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid system Id");
			return;
		}

		if (!password.equals(passwordConfirm)) {
			response.sendError(HttpStatus.UNAUTHORIZED.value(), "Passwords do not match");
			return;
		}

		if (Util.createPassword(password)) {
			request.getSession(true).setAttribute("sessionType", SessionType.ADMIN);
			return;
		}

		response.sendError(HttpStatus.UNAUTHORIZED.value(), "something went wrong creating your password. Try again");
	}

	@PostMapping("/createNewBot")
	public void createNewBot(String directoryName,
	                         HttpServletResponse response) throws IOException {

		if (processService.isDemoServer()) {
			return;
		}

		directoryName = Util.cleanValue(directoryName).toLowerCase(Locale.ROOT);
		File newBot = new File(processService.getBotsLocation() + File.separator + directoryName);
		if (newBot.exists()) {
			response.sendError(HttpStatus.BAD_REQUEST.value(), "Directory Already Exists");
			return;
		}

		String finalDirectoryName = directoryName;
		new Thread(() -> {
			try {
				if (newBot.mkdir()) {
					BotInfo botInfo = new BotInfo(finalDirectoryName);
					processService.updateBot(botInfo, null, true, true);
				}
			} catch (IOException | InterruptedException e) {
				log.error(e);
			}
		}).start();
	}
}
