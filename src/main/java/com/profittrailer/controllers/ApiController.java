package com.profittrailer.controllers;

import static com.profittrailer.utils.Util.getDateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.profittrailer.models.BotInfo;
import com.profittrailer.models.UpdateBotData;
import com.profittrailer.services.ProcessService;
import com.profittrailer.utils.BotInfoSerializer;
import com.profittrailer.utils.StaticUtil;
import com.profittrailer.utils.Util;
import com.profittrailer.utils.constants.SessionType;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Locale;
import java.util.Properties;

@Log4j2
@RestController
@RequestMapping("/api/v1")
public class ApiController {

	@Autowired
	private ProcessService processService;
	private Gson parser;
	private boolean showPercentage = true;
	private boolean showAmount = true;

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

		Collection<BotInfo> bots = processService.getBotList(processService.isOnlyManaged());
		Collection<BotInfo> allBots = processService.getBotList(false);
		Collection<BotInfo> allAddons = processService.getAddonList(false);
		JsonObject object = new JsonObject();
		object.add("bots", parser.toJsonTree(bots));
		object.addProperty("baseUrl", StaticUtil.getBaseUrl(request));
		object.addProperty("demoServer", processService.isDemoServer());
		object.addProperty("downloadUrl", processService.getDownloadUrl());
		object.addProperty("version", Util.getVersion());
		object.addProperty("maxBotsReached", allBots.size() + allAddons.size() >= processService.getMaxBots());

		return object.toString();
	}

	@PostMapping("/restartBot")
	public void startBot(String directoryName) throws InterruptedException {
		if (processService.isDemoServer()) {
			return;
		}
		processService.stopBot(processService.getBotInfoMap(), directoryName);
		Thread.sleep(5000);
		processService.startBot(processService.getBotInfoMap().get(directoryName));

	}

	@PostMapping("/deleteBot")
	public void deleteBot(String directoryName) throws InterruptedException {
		if (processService.isDemoServer()) {
			return;
		}

		processService.deleteBot(processService.getBotInfoMap(), directoryName);
	}

	@PostMapping("/restartBots")
	public void restartBots() throws InterruptedException {
		if (processService.isDemoServer()) {
			return;
		}
		new Thread(() -> {
			for (String dir : processService.getBotInfoMap().keySet()) {
				if (!processService.getBotInfoMap().get(dir).isManaged()) {
					continue;
				}
				try {
					processService.stopBot(processService.getBotInfoMap(), dir);
					Thread.sleep(20000);
					processService.startBot(processService.getBotInfoMap().get(dir));
				} catch (Exception e) {
					log.error(e);
				}
			}
		}).start();
	}

	@PostMapping("/stopBotAndUnlink")
	public void stopBot(String directoryName) {
		if (processService.isDemoServer()) {
			return;
		}
		processService.stopBot(processService.getBotInfoMap(), directoryName);
		processService.unlinkBot(processService.getBotInfoMap(), directoryName);
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
		object.addProperty("onlyManaged", processService.isOnlyManaged());
		return object.toString();
	}

	@PostMapping("/toggleCards")
	public String postToggle() {
		if (!processService.isDemoServer()) {
			processService.setOnlyManaged(!processService.isOnlyManaged());
		}
		JsonObject object = new JsonObject();
		object.addProperty("onlyManaged", processService.isOnlyManaged());
		return object.toString();
	}

	@GetMapping("/togglePercentage")
	public String getTogglePercentage() {
		JsonObject object = new JsonObject();
		object.addProperty("showPercentage", showPercentage);
		return object.toString();
	}

	@PostMapping("/togglePercentage")
	public String postTogglePercentage() {
		if (!processService.isDemoServer()) {
			showPercentage = !showPercentage;
		}
		JsonObject object = new JsonObject();
		object.addProperty("showPercentage", showPercentage);
		return object.toString();
	}

	@GetMapping("/toggleAmount")
	public String getToggleAmount() {
		JsonObject object = new JsonObject();
		object.addProperty("showAmount", showAmount);
		return object.toString();
	}

	@PostMapping("/toggleAmount")
	public String postToggleAmount() {
		if (!processService.isDemoServer()) {
			showAmount = !showAmount;
		}
		JsonObject object = new JsonObject();
		object.addProperty("showAmount", showAmount);
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
	                       String directoryName) {

		if (processService.isDemoServer()) {
			return;
		}

		UpdateBotData updateBotData = new UpdateBotData(forceUrl);

		for (String dir : processService.getBotInfoMap().keySet()) {
			if (directoryName != null && !directoryName.equalsIgnoreCase(dir)) {
				continue;
			}
			updateBotData.getBotsToUpdate().add(dir);
		}
		processService.setUpdateBotData(updateBotData);
	}

	@GetMapping("/linkout")
	public void linkOut(String directoryName,
	                    HttpServletResponse response) throws Exception {
		JsonObject object = new JsonObject();
		object.add("bot", parser.toJsonTree(processService.getBotInfoMap().get(directoryName)));

		BotInfo botInfo = processService.getBotInfoMap().get(directoryName);
		String token = processService.getSSOKey(directoryName);
		String redirectUrl = processService.createUrl(botInfo, "/?sso=" + token, false, false);
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
			timeout = getDateTime().plusMinutes((failedAttempts / 3) * 5L);
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

		String[] botsLocations = Util.readApplicationProperties().getProperty("server.bots.directory", "")
				.replace("\\", "/")
				.split(",");

		if (processService.isDemoServer()) {
			return;
		}

		Collection<BotInfo> allBots = processService.getBotList(false);
		if (allBots.size() >= processService.getMaxBots()) {
			log.error("You have reached max bots limit");
			return;
		}

		if (botsLocations.length > 1) {
			log.error("Creation of bot over multiple directories not yet support");
			return;
		}

		directoryName = Util.cleanValue(directoryName).toLowerCase(Locale.ROOT);
		String path = botsLocations[0] + File.separator + directoryName;
		File newBot = Paths.get(path).toFile();
		if (newBot.exists()) {
			response.sendError(HttpStatus.BAD_REQUEST.value(), "Directory Already Exists");
			return;
		}

		String finalDirectoryName = directoryName;
		new Thread(() -> {
			try {
				Files.createDirectories(newBot.toPath());
				BotInfo botInfo = new BotInfo(path, finalDirectoryName);
				processService.updateBot(botInfo, null, true);
			} catch (IOException | InterruptedException e) {
				log.error(e);
			}
		}).start();
	}

	@GetMapping("/globalSettings")
	public String getGlobalSettings() {
		JsonObject object = new JsonObject();
		object.addProperty("demoServer", processService.isDemoServer());
		object.addProperty("maxBots", processService.getMaxBots());

		JsonArray timezones = new JsonArray();
		StaticUtil.timeZones.forEach((x, y) -> {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("id", x);
			jsonObject.addProperty("value", y);
			timezones.add(jsonObject);
		});
		object.add("timezones", timezones);

		JsonArray currencies = new JsonArray();
		processService.getCoinGeckoSupportedCurrencies().forEach(x -> {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("id", x);
			currencies.add(jsonObject);
		});
		object.add("currencies", currencies);

		try {

			Properties properties = Util.readApplicationProperties();

			String timeZone = (String) properties.getOrDefault("server.settings.timezone", "Test");
			String currency = (String) properties.getOrDefault("server.settings.currency", "");
			String discordToken = (String) properties.getOrDefault("server.settings.discord.token", "");
			String xmx = (String) properties.getOrDefault("default.startup.xmx", "512m");
			String updateDelay = (String) properties.getOrDefault("default.update.delay", "60");
			boolean autoUpdate = Boolean.parseBoolean((String) properties.getOrDefault("server.bots.autoupdate", "false"));

			object.addProperty("timezone", timeZone);
			object.addProperty("currency", currency);
			object.addProperty("token", discordToken);
			object.addProperty("ram", xmx);
			object.addProperty("updateDelay", updateDelay);
			object.addProperty("autoUpdate", autoUpdate);

		} catch (Exception e) {
			log.error("Error getting global properties ", e);
		}

		return object.toString();
	}

	@PostMapping("/globalSettings")
	public void saveGlobalSettings(String timezone,
	                               String currency,
	                               String token,
	                               String ram,
	                               String updateDelay,
	                               boolean autoUpdate,
	                               HttpServletResponse response) throws IOException {
		try {
			JsonObject object = new JsonObject();

			Properties properties = Util.readApplicationProperties();

			addOrRemoveProperty("server.settings.timezone", timezone, properties);
			addOrRemoveProperty("server.settings.currency", currency, properties);
			addOrRemoveProperty("server.settings.discord.token", token, properties);
			addOrRemoveProperty("default.startup.xmx", ram, properties);
			addOrRemoveProperty("server.bots.autoupdate", Boolean.toString(autoUpdate), properties);
			addOrRemoveProperty("default.update.delay", updateDelay, properties);

			//send to bots...?
			if (properties.containsKey("server.settings.timezone")) {
				object.addProperty("TIMEZONE", (String) properties.get("server.settings.timezone"));
			}
			if (properties.containsKey("server.settings.currency")) {
				object.addProperty("CURRENCY", (String) properties.get("server.settings.currency"));
			}
			if (properties.containsKey("server.settings.discord.token")) {
				object.addProperty("DISCORD_TOKEN_1", (String) properties.get("server.settings.discord.token"));
			}
			processService.pushGlobalSettings(object);

			//save file
			try (OutputStream outputStream = new FileOutputStream("application.properties")) {
				properties.store(outputStream, null);
			}
		} catch (Exception e) {
			response.sendError(HttpStatus.NOT_MODIFIED.value(), e.getMessage());
			log.error("Error saving global properties ", e);
		}
	}

	private void addOrRemoveProperty(String property, String value, Properties properties){
		if (StringUtils.isNotBlank(property)) {
			properties.put(property, value);
		} else {
			properties.remove(property);
		}
	}
	@PostMapping("/toggleSOM")
	public void toggleSOM(String directoryName, boolean enabled) throws Exception {
		if (processService.isDemoServer()) {
			return;
		}
		processService.toggleSOM(directoryName, enabled);
	}

	@GetMapping("/globalStats")
	public String globalStats() {
		return processService.generateGlobalStats().toString();
	}
}
