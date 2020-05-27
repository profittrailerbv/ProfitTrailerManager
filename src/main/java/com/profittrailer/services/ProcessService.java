package com.profittrailer.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.profittrailer.models.BotInfo;
import com.profittrailer.models.StreamGobbler;
import com.profittrailer.utils.BotInfoSerializer;
import com.profittrailer.utils.HttpClientManager;
import com.profittrailer.utils.StaticUtil;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Getter
@Log4j2
public class ProcessService {

	private String managerToken;
	@Value("${server.bots.directory}")
	private String botsLocation;
	@Value("${server.bots.autostart:false}")
	private boolean autoStartManagedBots;
	private Map<String, ProcessInfo> processInfoMap = new HashMap<>();
	private Map<String, BotInfo> botInfoMap = new ConcurrentHashMap<>();
	private boolean initialzed = false;
	private boolean processedInitialized = false;
	private Gson parser;

	@PostConstruct
	public void init() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(BotInfo.class, new BotInfoSerializer());
		parser = gsonBuilder.create();
		managerToken = RandomStringUtils.randomAlphanumeric(30);
		refreshBotData();
		initialzed = true;
	}

	@Scheduled(initialDelay = 10000, fixedDelay = 10000)
	public void readProcesses() {
		//log.info("Start");
		if (initialzed) {
			Map<String, ProcessInfo> tmpProcessInfoMap = new HashMap<>();
			JProcesses.getProcessList()
					.forEach(e -> tmpProcessInfoMap.put(e.getPid(), e));
			processInfoMap = tmpProcessInfoMap;
			processedInitialized = true;
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
					boolean managed = botInfo.isManaged();
					if (managed) {
						log.info("Checking {} -- offline?: {} -- managed?: {}", botInfo.getSiteName(), offline, managed);
					}

					if (!offline && StringUtils.isNotBlank(StaticUtil.url) && processedInitialized && managed) {
						String healthUrl = createUrl(botInfo, managerToken, "/api/v2/health");
						try {
							log.info("Health url: {}", healthUrl);
							Pair<Integer, String> data = HttpClientManager.getHttp(healthUrl, Collections.emptyList());
							log.info("Result {} -- code: {} -- data: {}", botInfo.getSiteName(), data.getKey(), data.getValue());
							if (data.getKey() < 202) {
								offline = !StringUtils.equalsIgnoreCase(data.getValue(), "true");
							}
						} catch (Exception e) {
							log.error("Erorr pinging health" + e.getMessage());
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
			e.printStackTrace();
		}
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
				botInfo.setStartDate(LocalDateTime.now());
				botInfoMap.put(directoryName, botInfo);
				Thread.sleep(5000);
				readError(process);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
		}
	}

	public void stopBot(String botName) {
		BotInfo bot = botInfoMap.get(botName);
		boolean managed = bot.isManaged();
		int processId = NumberUtils.toInt((String) bot.getBotProperties().getOrDefault("process", 0));
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
			e.printStackTrace();
			log.error(e);
		}
		return new Properties();
	}

	public Collection<BotInfo> getBotList(boolean onlyManaged) {

		return botInfoMap.values()
				.stream()
				.sorted(Comparator.comparing(BotInfo::isManaged, Comparator.reverseOrder())
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
		try {
			boolean managed = botInfo.isManaged();
			if (managed) {
				String dataUrl = createUrl(botInfo, managerToken, "/api/v2/data/stats");
				String miscUrl = createUrl(botInfo, managerToken, "/api/v2/data/misc");
				Pair<Integer, String> data = HttpClientManager.getHttp(dataUrl, Collections.emptyList());
				if (data.getKey() < 202) {
					botInfo.setStatsData(parser.fromJson(data.getValue(), JsonObject.class));
				}
				Pair<Integer, String> misc = HttpClientManager.getHttp(miscUrl, Collections.emptyList());
				if (misc.getKey() < 202) {
					botInfo.setMiscData(parser.fromJson(misc.getValue(), JsonObject.class));
				}
			}
		} catch (Exception e) {
			log.error(e);
		}
	}

	private String createUrl(BotInfo botInfo,
	                         String managerToken,
	                         String endPoint) {

		String token = managerToken;
		if (botInfo.getProcessInfo() != null) {
			String[] splitted = botInfo.getProcessInfo().getCommand().split("--server.manager.token=");
			if (splitted.length > 1) {
				String[] finalSplitted = splitted[1].split("--server.manager.dummy");
				token = finalSplitted[0].trim();
			}
		}
		String port = (String) botInfo.getBotProperties().get("port");
		String contextPath = (String) botInfo.getBotProperties().get("context");
		String url = String.format("%s:%s%s", StaticUtil.url, port, contextPath);
		return url + endPoint + "?token=" + token;
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
}
