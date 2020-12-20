package com.profittrailer.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.profittrailer.models.BotInfo;
import com.profittrailer.models.StreamGobbler;
import com.profittrailer.utils.BotInfoSerializer;
import com.profittrailer.utils.HttpClientManager;
import com.profittrailer.utils.StaticUtil;
import com.profittrailer.utils.Util;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jutils.jprocesses.JProcesses;
import org.jutils.jprocesses.model.ProcessInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Getter
@Log4j2
public class ProcessService {

	private static final String githubUrl = "https://api.github.com/repos/taniman/profit-trailer/releases/latest";

	private String managerToken;
	@Value("${server.bots.autostart:false}")
	private boolean autoStartManagedBots;
	@Value("${server.demo:false}")
	private boolean demoServer;
	@Value("${server.bots.max:999}")
	private int maxBots;
	private Map<String, ProcessInfo> processInfoMap = new HashMap<>();
	private Map<String, BotInfo> botInfoMap = new ConcurrentHashMap<>();
	private boolean initialzed = false;
	private boolean processedInitialized = false;
	private Gson parser;
	private String latestVersion = "0.0.0";
	private String downloadUrl;
	@Value("${server.caddy.enabled:false}")
	private boolean caddyEnabled;
	@Value("${server.caddy.domain:}")
	private String caddyDomain;
	@Value("${server.caddy.import:}")
	private String caddyImport;
	@Value("${server.port:10000}")
	private int port;

	@Autowired
	private Environment environment;

	@PostConstruct
	public void init() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(BotInfo.class, new BotInfoSerializer());
		parser = gsonBuilder.create();
		managerToken = RandomStringUtils.randomAlphanumeric(30);
		refreshBotData();
		initialzed = true;
		try {
			reloadCaddy();
		} catch (IOException e) {
			log.error(e);
		}

		if (StringUtils.isNotBlank(caddyDomain)) {
			StaticUtil.url = "https://" + caddyDomain;
		}
	}

	@Scheduled(initialDelay = 10000, fixedDelay = 900000)
	public void updateGithub() {
		try {
			Pair<Integer, String> data = HttpClientManager.getHttp(githubUrl, Collections.emptyList());
			if (data.getKey() < 202) {
				JsonObject object = parser.fromJson(data.getValue(), JsonObject.class);
				latestVersion = object.get("tag_name").getAsString();
				String assetUrl = object.get("assets_url").getAsString();
				Pair<Integer, String> assetData = HttpClientManager.getHttp(assetUrl, Collections.emptyList());
				if (assetData.getKey() < 202) {
					JsonArray assetsArray = parser.fromJson(assetData.getValue(), JsonArray.class);
					if (assetsArray.size() > 0) {
						downloadUrl = assetsArray.get(0).getAsJsonObject().get("browser_download_url").getAsString();
					}
				}
			} else {
				log.error("Github error code {}", data.getValue());
			}
		} catch (Exception e) {
			log.error("Error updating github ", e);
		}
	}

	@Scheduled(initialDelay = 10000, fixedDelay = 10000)
	public void readProcesses() {
		//log.info("Start");
		try {
			if (initialzed) {
				Map<String, ProcessInfo> tmpProcessInfoMap = new HashMap<>();
				JProcesses.getProcessList()
						.forEach(e -> tmpProcessInfoMap.put(e.getPid(), e));
				processInfoMap = tmpProcessInfoMap;
				processedInitialized = true;
			}
		} catch (Exception e) {
			log.error("Error reading process ", e);
		}
		//log.info("Done");
	}

	@Scheduled(initialDelay = 10000, fixedDelay = 10000)
	public void refreshBotData() {
		String[] botsLocations = environment.getProperty("server.bots.directory", "")
				.replace("\\", "/")
				.split(",");

		for (String location : botsLocations) {
			if (StringUtils.isBlank(location)) {
				continue;
			}

			try {
				File botsDirectory = new File(location);
				File[] files = botsDirectory.listFiles();
				if (files != null) {
					Arrays.stream(files)
							.filter(File::isDirectory)
							.filter(e -> e.listFiles() != null
									&& Arrays.stream(Objects.requireNonNull(e.listFiles()))
									.anyMatch(f -> f.getName().equalsIgnoreCase("ProfitTrailer.jar")))
							.forEach(directory -> {
								String directoryName = directory.getName();
								String path = directory.getPath();
								BotInfo botInfo = botInfoMap.getOrDefault(directoryName, new BotInfo(path, directoryName));
								botInfo.setBotProperties(getBotProperties(path));
								String pid = (String) botInfo.getBotProperties().get("process");
								if (pid != null) {
									botInfo.setProcessInfo(processInfoMap.get("" + pid));
								}
								if (processedInitialized) {
									getBotData(botInfo);
									botInfo.setInitialized(initialzed);
								}
								botInfo.isUnlinked();
								botInfoMap.put(directoryName, botInfo);
							});
				}

				if (autoStartManagedBots && processedInitialized) {
					for (BotInfo botInfo : botInfoMap.values()) {

						boolean offline = botInfo.getStatus().equals("OFFLINE");
						boolean startingUpdating = botInfo.getStatus().equals("STARTING") || botInfo.getStatus().equals("UPDATING");
						boolean managed = botInfo.isManaged();
						boolean unlinked = botInfo.isUnlinked();
						String port = (String) botInfo.getBotProperties().get("port");

						if (unlinked) {
							continue;
						}

						if (!offline
								&& port != null
								&& !startingUpdating
								&& StringUtils.isNotBlank(StaticUtil.url)
								&& processedInitialized && managed) {
							String healthUrl = createUrl(botInfo, "/api/v2/health");
							try {
								Pair<Integer, String> data = HttpClientManager.getHttp(healthUrl, Collections.emptyList());
								if (data.getKey() < 202) {
									offline = !StringUtils.equalsIgnoreCase(data.getValue(), "true");
								}
							} catch (Exception e) {
								log.error("Error pinging health {}: {}", healthUrl, e.getMessage());
							}
						}
						if (offline && managed) {
							stopBot(botInfo.getDirectory());
							Thread.sleep(3000);
							startBot(botInfo);
							Thread.sleep(30000);
						}
					}
				}
			} catch (Exception e) {
				log.error("Error auto starting bots? ", e);
			}
		}
		generateCaddyFile();
	}

	private void generateCaddyFile() {
		//log.info("1. {} -- 2.{} -- 3.{}", processedInitialized, StaticUtil.url, caddyEnabled);
		if (!processedInitialized) {
			return;
		}
		if (StringUtils.isBlank(StaticUtil.url)) {
			return;
		}

		if (!caddyEnabled) {
			return;
		}

		Map<String, String> reverseBots = new HashMap<>();
		reverseBots.put("/*", String.format("\treverse_proxy\t%s\t%s", "", "localhost:" + port));
		for (BotInfo botInfo : botInfoMap.values()) {
			String port = (String) botInfo.getBotProperties().get("port");
			String contextPath = (String) botInfo.getBotProperties().get("context");

			if (port == null) {
				continue;
			}

			if (StringUtils.isBlank(contextPath) || contextPath.equals("/")) {
				continue;
			}

			String localUrl = createCaddyLocalUrl(botInfo);
			reverseBots.put(contextPath + "*", String.format("\treverse_proxy\t%s", localUrl));
		}

		if (reverseBots.size() == 0) {
			return;
		}

		StringBuilder caddyString = new StringBuilder();
		if (StringUtils.isNotBlank(caddyImport)) {
			caddyString.append("import ").append(caddyImport).append("\r\n\r\n");
		}

		String httpsUrl = StaticUtil.url.replace("http://", "").replace("https://", "");
		if (!StringUtils.isNotBlank(caddyDomain)) {
			httpsUrl = "https://" + caddyDomain;
		}

		for (Map.Entry<String, String> entry : reverseBots.entrySet()) {
			caddyString.append(httpsUrl).append(entry.getKey()).append(" {\r\n");
			caddyString.append(entry.getValue());
			caddyString.append("\r\n}\r\n\r\n");
		}

		File caddyFile = new File("data/Caddyfile");
		try {
			String oldCaddyString = "";
			if (caddyFile.exists()) {
				oldCaddyString = FileUtils.readFileToString(caddyFile, StandardCharsets.UTF_8);
			}
			if (!oldCaddyString.equalsIgnoreCase(caddyString.toString())) {
				FileUtils.writeStringToFile(caddyFile, caddyString.toString(), StandardCharsets.UTF_8);
				reloadCaddy();
			}
		} catch (IOException e) {
			log.error("Error writing caddy file {}", e.getMessage());
		}
	}

	private void reloadCaddy() throws IOException {
		if (!caddyEnabled) {
			return;
		}

		List<String> commands = new ArrayList<>();
		commands.add("caddy");
		commands.add("reload");

		ProcessBuilder builder = new ProcessBuilder(commands);
		builder.directory(new File("data"));
		builder.redirectErrorStream(true);
		Process process = builder.start();
		readError(process);
	}

	public void startBot(BotInfo originalBotInfo) {
		if (originalBotInfo == null) {
			return;
		}

		String directoryName = originalBotInfo.getDirectory();

		List<String> commands = new ArrayList<>();
		commands.add("java");
		commands.add("-Djava.net.preferIPv4Stack=true");
		commands.add("-XX:+UseSerialGC");
		commands.add("-XX:+UseStringDeduplication");
		commands.add("-Xms64m");
		commands.add(String.format("-Xmx%s", environment.getProperty(directoryName + ".startup.xmx", "512m")));
		commands.add("-XX:CompressedClassSpaceSize=300m");
		commands.add("-XX:MaxMetaspaceSize=256m");
		commands.add("-jar");
		commands.add("ProfitTrailer.jar");
		commands.add("--server.port.forcenew");
		if (caddyEnabled) {
			commands.add("--server.manager.context=/" + Util.cleanValue(directoryName));
		}
		commands.add("--server.manager.token=" + managerToken);

		String path = originalBotInfo.getPath();
		ProcessBuilder builder = new ProcessBuilder(commands);
		builder.directory(new File(originalBotInfo.getPath()));
		builder.redirectErrorStream(true);

		try {
			if (!botInfoMap.containsKey(directoryName)
					|| botInfoMap.get(directoryName).getStatus().equals("OFFLINE")) {
				Process process = builder.start();
				BotInfo botInfo = botInfoMap.getOrDefault(directoryName, new BotInfo(path, directoryName));
				botInfo.setProcess(process);
				botInfo.setStartDate(Util.getDateTime());

				File file = new File(botInfo.getPath() + "/data/unlinked");
				FileSystemUtils.deleteRecursively(file.toPath());

				botInfoMap.put(directoryName, botInfo);
				Thread.sleep(5000);
				readError(process);
			} else {
				log.info("Skipping bot start {} -- {}", botInfoMap.containsKey(directoryName), botInfoMap.get(directoryName).getStatus());
			}
		} catch (Exception e) {
			log.error("Error starting bot", e);
		}
	}

	public void stopBot(String botName) {
		BotInfo bot = botInfoMap.get(botName);
		if (bot == null) {
			return;
		}

		boolean managed = bot.isManaged();
		int processId = NumberUtils.toInt((String) bot.getBotProperties().getOrDefault("process", "0"));
		if (managed && bot.getProcess() != null) {
			bot.getProcess().destroy();
			if (processId > 0) {
				JProcesses.killProcess(processId);
			}
		} else if (bot.getProcessInfo() != null) {
			JProcesses.killProcess(Integer.parseInt(bot.getProcessInfo().getPid()));
		}

		if (processId > 0) {
			processInfoMap.remove("" + processId);
		}

		bot.setProcessInfo(null);
		bot.setProcess(null);
	}

	public void unlinkBot(String botName) {
		BotInfo bot = botInfoMap.get(botName);
		if (bot == null) {
			return;
		}

		bot.clearData();

		try {
			File file = new File(bot.getPath() + "/data/unlinked");
			if (!file.exists() && !file.createNewFile()) {
				log.error("Error unlinking bot {}", bot.getSiteName());
			}
		} catch (Exception e) {
			log.error("Error unlinking bot {} -- {}", bot.getSiteName(), e.getMessage());
		}

	}

	public Properties getBotProperties(String path) {
		try {
			File file = new File(path + "/data/details");
			if (file.exists()) {
				FileReader reader = new FileReader(file);
				Properties properties = new Properties();
				properties.load(reader);
				return properties;
			}
		} catch (Exception e) {
			log.error("Error getting bot properties ", e);
		}
		return new Properties();
	}

	public Collection<BotInfo> getBotList(boolean onlyManaged) {

		return botInfoMap.values()
				.stream()
				.sorted(Comparator.comparing(BotInfo::isManaged, Comparator.reverseOrder())
						.thenComparing(BotInfo::getSiteName))
				.filter(e -> {
					if (!onlyManaged) {
						return true;
					}
					return e.isManaged();
				}).collect(Collectors.toList());

	}

	public void getBotData(BotInfo botInfo) {
		if (StringUtils.isBlank(StaticUtil.url)) {
			return;
		}
		String port = (String) botInfo.getBotProperties().get("port");
		if (port == null) {
			return;
		}

		String dataUrl = "";
		try {
			boolean managed = botInfo.isManaged();
			boolean online = botInfo.getStatus().equals("ONLINE");

			if (managed && online) {
				dataUrl = createUrl(botInfo, "/api/v2/data/stats");
				String miscUrl = createUrl(botInfo, "/api/v2/data/misc");
				String propertiesUrl = createUrl(botInfo, "/api/v2/data/properties");
				String accountUrl = createUrl(botInfo, "/api/v2/account/settings");
				String pairsUrl = createUrl(botInfo, "/api/v2/data/pairs");
				String dcaUrl = createUrl(botInfo, "/api/v2/data/dca");
				String pendingUrl = createUrl(botInfo, "/api/v2/data/pending");
				String salesUrl = createUrl(botInfo, "/api/v2/data/sales");
				Pair<Integer, String> data = HttpClientManager.getHttp(dataUrl, Collections.emptyList());
				if (data.getKey() < 202) {
					botInfo.setStatsData(parser.fromJson(data.getValue(), JsonObject.class));
				}
				Pair<Integer, String> misc = HttpClientManager.getHttp(miscUrl, Collections.emptyList());
				if (misc.getKey() < 202) {
					botInfo.setMiscData(parser.fromJson(misc.getValue(), JsonObject.class));
				}
				Pair<Integer, String> properties = HttpClientManager.getHttp(propertiesUrl, Collections.emptyList());
				if (properties.getKey() < 202) {
					botInfo.setPropertiesData(parser.fromJson(properties.getValue(), JsonObject.class));
				}
				Pair<Integer, String> accountSettings = HttpClientManager.getHttp(accountUrl, Collections.emptyList());
				if (accountSettings.getKey() < 202) {
					botInfo.setAccountData(parser.fromJson(accountSettings.getValue(), JsonObject.class));
				}
				Pair<Integer, String> pairs = HttpClientManager.getHttp(pairsUrl, Collections.emptyList());
				if (pairs.getKey() < 202) {
					botInfo.setPairsData(parser.fromJson(pairs.getValue(), JsonArray.class));
				}
				Pair<Integer, String> dca = HttpClientManager.getHttp(dcaUrl, Collections.emptyList());
				if (dca.getKey() < 202) {
					botInfo.setDcaData(parser.fromJson(dca.getValue(), JsonArray.class));
				}
				Pair<Integer, String> pending = HttpClientManager.getHttp(pendingUrl, Collections.emptyList());
				if (dca.getKey() < 202) {
					botInfo.setPendingData(parser.fromJson(pending.getValue(), JsonArray.class));
				}
				Pair<Integer, String> sales = HttpClientManager.getHttp(salesUrl, Collections.emptyList());
				if (sales.getKey() < 202) {
					JsonObject object = parser.fromJson(sales.getValue(), JsonObject.class);
					if (object.has("data")) {
						botInfo.setSalesData(object.getAsJsonArray("data"));
					}
				}
			}
		} catch (Exception e) {
			log.error("Error getting bot data {} - {}", dataUrl, e.getMessage());
		}
	}

	public String createUrl(BotInfo botInfo,
	                        String endPoint) {

		return createUrl(botInfo, endPoint, true);
	}

	public String createUrl(BotInfo botInfo,
	                        String endPoint,
	                        boolean includeManagerToken) {

		String token = managerToken;
		if (botInfo.getProcessInfo() != null) {
			String[] splitted = botInfo.getProcessInfo().getCommand().split("--server.manager.token=");
			if (splitted.length > 1) {
				String[] finalSplitted = splitted[1].split("--server.manager.dummy");
				token = finalSplitted[0].trim();
			}
		}
		String sslEnabledValue = (String) botInfo.getBotProperties().get("sslEnabled");
		boolean sslEnabled = Boolean.parseBoolean(sslEnabledValue);

		String port = "";
		if (!caddyEnabled || (sslEnabledValue != null && sslEnabled)) {
			port = ":" + botInfo.getBotProperties().get("port");
		}
		String contextPath = (String) botInfo.getBotProperties().get("context");
		String url = String.format("%s%s%s", StaticUtil.url, port, contextPath);
		url = url + endPoint;
		if (includeManagerToken) {
			url = url + "?token=" + token;
		}

		if (!caddyEnabled && sslEnabledValue != null) {
			url = url.replace("https:", "http:");
			if (sslEnabled) {
				url = url.replace("http:", "https:");
			}
		}
		if (caddyEnabled && !url.contains("https:")) {
			url = url.replace("http:", "https:");
		}
		return url;
	}

	public String createCaddyLocalUrl(BotInfo botInfo) {
		String port = (String) botInfo.getBotProperties().get("port");
		return String.format("localhost:%s", port);
	}

	private void readError(Process process) {
		if (process != null) {
			StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());
			outputGobbler.start();
		}
	}

	public JsonElement convertBotInfo(BotInfo botInfo) {
		return parser.toJsonTree(botInfo);
	}

	public void updateBot(BotInfo botInfo,
	                      String forceUrl,
	                      boolean forceUpdate,
	                      boolean newBot) throws IOException, InterruptedException {

		if (forceUrl != null && forceUrl.trim().equalsIgnoreCase(downloadUrl)) {
			forceUrl = null;
		}

		if (!newBot && (!botInfo.isManaged() || botInfo.isUnlinked())) {
			log.info("{} is not a managed bot, skipping", botInfo.getSiteName());
			return;
		}

		String version = "0.0.0";
		if (botInfo.getMiscData() == null && !forceUpdate) {
			log.info("{} -- Version not known, skipping", botInfo.getSiteName());
			return;
		} else if (botInfo.getMiscData() != null) {
			version = botInfo.getMiscData().get("version").getAsString();
		}

		String updateMessage = latestVersion;
		String updateUrl = downloadUrl;
		if (StringUtils.isNotBlank(forceUrl)) {
			updateMessage = "from url: " + forceUrl;
			updateUrl = forceUrl;
		}

		if (StaticUtil.isUpdateAvailable(version, latestVersion) || StringUtils.isNotBlank(forceUrl) || forceUpdate) {
			String updateFolder = StaticUtil.unzip(updateUrl);
			if (updateFolder != null) {
				botInfo.setUpdateDate(Util.getDateTime());
				stopBot(botInfo.getDirectory());
				Thread.sleep(2000);
				log.info("Updating {} to version {}", botInfo.getSiteName(), updateMessage);
				StaticUtil.copyJar(updateFolder, botInfo.getPath());
				startBot(botInfo);
				Thread.sleep(20000);
				log.info("{} update complete, bot is starting", botInfo.getSiteName());
			}
		} else {
			log.info("{} is using a newer version, try forcing an update", botInfo.getSiteName());
		}
	}

	public String getSSOKey(String directoryName) throws Exception {
		BotInfo botInfo = botInfoMap.get(directoryName);
		String ssoToken = "";
		String ssoTokenUrl = createUrl(botInfo, "/api/v2/sso/token");
		Pair<Integer, String> tokenRequest = HttpClientManager.getHttp(ssoTokenUrl, Collections.emptyList());
		if (tokenRequest.getKey() < 202) {
			JsonObject jsonObject = parser.fromJson(tokenRequest.getValue(), JsonObject.class);
			ssoToken = jsonObject.get("token").getAsString();
		}
		return ssoToken;
	}

	public String toggleSOM(String directoryName, boolean enabled) throws Exception {
		BotInfo botInfo = botInfoMap.get(directoryName);
		String somUrl = createUrl(botInfo, "/api/v2/globalSellOnlyMode");
		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("enabled", String.valueOf(enabled)));
		Pair<Integer, String> tokenRequest = HttpClientManager.postHttp(somUrl, params, Collections.emptyList());
		if (tokenRequest.getKey() <= 204) {
			return String.valueOf(true);
		}
		return String.valueOf(false);
	}

	public void pushGlobalSettings(JsonObject jsonObject) {
		for (BotInfo botInfo : botInfoMap.values()) {
			if (!botInfo.isManaged()) {
				continue;
			}
			String settingsUrl = createUrl(botInfo, "/api/v2/account/settings");
			try {
				HttpClientManager.postHttpJson(settingsUrl, jsonObject, Collections.emptyList());
			} catch (Exception e) {
				log.error("Error pushing settings {}: {}", settingsUrl, e.getMessage());
			}
		}
	}

	public JsonObject generateGlobalStats() {
		Set<String> accountIds = new HashSet<>();
		double totalProfitTodayLive = 0;
		double totalProfitTodayTest = 0;
		double totalProfitLastMonthLive = 0;
		double totalProfitLastMonthTest = 0;
		double totalProfitThisMonthLive = 0;
		double totalProfitThisMonthTest = 0;
		double totalTCVLive = 0;
		double totalTCVTest = 0;

		try {
			for (BotInfo botInfo : botInfoMap.values()) {
				if (botInfo.getMiscData() == null || !botInfo.getMiscData().has("priceDataUSDConversionRate")) {
					continue;
				}
				if (botInfo.getStatsData() == null) {
					continue;
				}
				if (!botInfo.getStatsData().getAsJsonObject("basic").has("totalProfitLastMonth")) {
					continue;
				}

				double cr = botInfo.getMiscData().get("priceDataUSDConversionRate").getAsDouble();
				String accountId = "";
				String market = "";
				if (botInfo.getMiscData().has("market")) {
					market = botInfo.getMiscData().get("market").getAsString();
				}
				if (botInfo.getMiscData().has("accountId")) {
					accountId = botInfo.getMiscData().get("accountId").getAsString();
				}

				double profitToday = botInfo.getStatsData().getAsJsonObject("basic").get("totalProfitToday").getAsDouble() * cr;
				double profitLastMonth = botInfo.getStatsData().getAsJsonObject("basic").get("totalProfitLastMonth").getAsDouble() * cr;
				double profitThisMonth = botInfo.getStatsData().getAsJsonObject("basic").get("totalProfitThisMonth").getAsDouble() * cr;
				double tcv = StaticUtil.extractTCV(botInfo.getMiscData()) * cr;

				if (botInfo.getPropertiesData().get("testMode").getAsBoolean() || botInfo.getPropertiesData().get("testnet").getAsBoolean()) {
					totalProfitTodayTest += profitToday;
					totalProfitLastMonthTest += profitLastMonth;
					totalProfitThisMonthTest += profitThisMonth;
					totalTCVTest += tcv;
				} else {
					totalProfitTodayLive += profitToday;
					totalProfitLastMonthLive += profitLastMonth;
					totalProfitThisMonthLive += profitThisMonth;
					if (StringUtils.isBlank(accountId) || !accountIds.contains(market + accountId)) {
						totalTCVLive += tcv;

						if (StringUtils.isNotBlank(accountId)) {
							accountIds.add(market + accountId);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
		}

		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("totalProfitTodayLive", totalProfitTodayLive);
		jsonObject.addProperty("totalProfitTodayTest", totalProfitTodayTest);
		jsonObject.addProperty("totalProfitLastMonthLive", totalProfitLastMonthLive);
		jsonObject.addProperty("totalProfitLastMonthTest", totalProfitLastMonthTest);
		jsonObject.addProperty("totalProfitThisMonthLive", totalProfitThisMonthLive);
		jsonObject.addProperty("totalProfitThisMonthTest", totalProfitThisMonthTest);
		jsonObject.addProperty("totalTCVLive", totalTCVLive);
		jsonObject.addProperty("totalTCVTest", totalTCVTest);

		return jsonObject;
	}
}
