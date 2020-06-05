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
import org.jutils.jprocesses.JProcesses;
import org.jutils.jprocesses.model.ProcessInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Getter
@Log4j2
public class ProcessService {

	private static final String githubUrl = "https://api.github.com/repos/taniman/profit-trailer/releases/latest";

	private String managerToken;
	@Value("${server.bots.directory:}")
	private String botsLocation;
	@Value("${server.bots.autostart:false}")
	private boolean autoStartManagedBots;
	@Value("${server.demo:false}")
	private boolean demoServer;
	private Map<String, ProcessInfo> processInfoMap = new HashMap<>();
	private Map<String, BotInfo> botInfoMap = new ConcurrentHashMap<>();
	private boolean initialzed = false;
	private boolean processedInitialized = false;
	private Gson parser;
	private String latestVersion = "0.0.0";
	private String downloadUrl;
	@Value("${server.caddy.enabled:false}")
	private boolean caddyEnabled;
	@Value("${server.caddy.import:}")
	private String caddyImport;
	@Value("${server.port:10000}")
	private int port;

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
						StaticUtil.unzip(downloadUrl);
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
		try {
			File botsDirectory = new File(botsLocation);
			File[] files = botsDirectory.listFiles();
			if (files != null) {
				Arrays.stream(files)
						.filter(File::isDirectory)
						.filter(e -> e.listFiles() != null
								&& Arrays.stream(Objects.requireNonNull(e.listFiles()))
								.anyMatch(f -> f.getName().equalsIgnoreCase("ProfitTrailer.jar")))
						.map(File::getName)
						.forEach(e -> {
							BotInfo botInfo = botInfoMap.getOrDefault(e, new BotInfo(e));
							botInfo.setBotProperties(getBotProperties(e));
							String pid = (String) botInfo.getBotProperties().get("process");
							if (pid != null) {
								botInfo.setProcessInfo(processInfoMap.get("" + pid));
							}
							if (processedInitialized) {
								getBotData(botInfo);
								botInfo.setInitialized(initialzed);
							}
							botInfoMap.put(e, botInfo);
						});
			}

			if (autoStartManagedBots && processedInitialized) {
				for (BotInfo botInfo : botInfoMap.values()) {
					boolean offline = botInfo.getStatus().equals("OFFLINE");
					boolean startingUpdating = botInfo.getStatus().equals("STARTING") || botInfo.getStatus().equals("UPDATING");
					boolean managed = botInfo.isManaged();
					String port = (String) botInfo.getBotProperties().get("port");
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
						startBot(botInfo.getDirectory());
						Thread.sleep(30000);
					}
				}
			}
		} catch (Exception e) {
			log.error("Error auto starting bots? ", e);
		}

		generateCaddyFile();
	}

	private void generateCaddyFile() {
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
//		if (!httpsUrl.contains("https:")) {
//			httpsUrl = httpsUrl.replace("http:", "https:");
//		}
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

	public void startBot(String directoryName) {
		List<String> commands = new ArrayList<>();
		commands.add("java");
		commands.add("-Djava.net.preferIPv4Stack=true");
		commands.add("-XX:+UseSerialGC");
		commands.add("-XX:+UseStringDeduplication");
		commands.add("-Xms64m");
		commands.add("-Xmx512m");
		commands.add("-XX:CompressedClassSpaceSize=300m");
		commands.add("-XX:MaxMetaspaceSize=256m");
		commands.add("-jar");
		commands.add("ProfitTrailer.jar");
		commands.add("--server.port.forcenew");
		if (caddyEnabled) {
			commands.add("--server.manager.context=/" + Util.cleanValue(directoryName));
		}
		commands.add("--server.manager.token=" + managerToken);

		ProcessBuilder builder = new ProcessBuilder(commands);
		builder.directory(new File(botsLocation + "/" + directoryName));
		builder.redirectErrorStream(true);

		try {
			if (!botInfoMap.containsKey(directoryName)
					|| botInfoMap.get(directoryName).getStatus().equals("OFFLINE")) {
				Process process = builder.start();
				BotInfo botInfo = botInfoMap.getOrDefault(directoryName, new BotInfo(directoryName));
				botInfo.setProcess(process);
				botInfo.setStartDate(Util.getDateTime());
				botInfoMap.put(directoryName, botInfo);
				Thread.sleep(5000);
				readError(process);
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
		bot.setProcessInfo(null);
		bot.setProcess(null);
	}

	public Properties getBotProperties(String botName) {
		try {
			File file = new File(botsLocation + "/" + botName + "/data/details");
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
						.thenComparing(e-> e.getSiteName().substring(0,1))
						.thenComparing(BotInfo::getProfitToday, Comparator.reverseOrder()))
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
				String pairsUrl = createUrl(botInfo, "/api/v2/data/pairs");
				String dcaUrl = createUrl(botInfo, "/api/v2/data/dca");
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
				Pair<Integer, String> pairs = HttpClientManager.getHttp(pairsUrl, Collections.emptyList());
				if (pairs.getKey() < 202) {
					botInfo.setPairsData(parser.fromJson(pairs.getValue(), JsonArray.class));
				}
				Pair<Integer, String> dca = HttpClientManager.getHttp(dcaUrl, Collections.emptyList());
				if (dca.getKey() < 202) {
					botInfo.setDcaData(parser.fromJson(dca.getValue(), JsonArray.class));
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

		if (!newBot && !botInfo.isManaged()) {
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
				StaticUtil.copyJar(updateFolder, botsLocation + "/" + botInfo.getDirectory());
				startBot(botInfo.getDirectory());
				Thread.sleep(35000);
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
}
