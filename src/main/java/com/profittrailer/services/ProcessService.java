package com.profittrailer.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.profittrailer.models.BotInfo;
import com.profittrailer.utils.BotInfoSerializer;
import com.profittrailer.utils.HttpClientManager;
import com.profittrailer.utils.StaticUtil;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jutils.jprocesses.JProcesses;
import org.jutils.jprocesses.model.ProcessInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Getter
@Log4j2
public class ProcessService {

	private String managerToken;
	@Value("${server.bots.directory}")
	private String botsLocation;
	private Map<String, BotInfo> botInfoMap = new ConcurrentHashMap<>();
	private boolean initialzed = false;
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
	public void refreshBotData() {
		File botsDirectory = new File(botsLocation);
		File[] files = botsDirectory.listFiles();
		if (files != null) {
			Map<String, ProcessInfo> processInfoMap = new HashMap<>();
			if (initialzed) {
				JProcesses.getProcessList()
						.forEach(e -> processInfoMap.put(e.getPid(), e));
			}

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
						getBotData(botInfo);
						botInfo.setInitialized(initialzed);
						botInfoMap.put(e, botInfo);
					});
		}
	}

	public void startBot(String botName) {
		ProcessBuilder builder = new ProcessBuilder("java",
				"-Djava.net.preferIPv4Stack=true",
				"-XX:+UseSerialGC",
				"-XX:+UseStringDeduplication",
				"-Xms64m",
				"-Xmx512m",
				"-XX:CompressedClassSpaceSize=300m",
				"-XX:MaxMetaspaceSize=256m",
				"-jar",
				"ProfitTrailer.jar",
				"--server.manager.token=" + managerToken);

		builder.directory(new File(botsLocation + "/" + botName));

		try {
			if (!botInfoMap.containsKey(botName)
					|| botInfoMap.get(botName).getStatus().equals("OFFLINE")) {
				Process process = builder.start();
				BotInfo botInfo = botInfoMap.getOrDefault(botName, new BotInfo(botName));
				botInfo.setProcess(process);
				botInfoMap.put(botName, botInfo);
			}
		} catch (Exception e) {
			log.error(e);
		}
	}

	public void stopBot(String botName) {
		BotInfo bot = botInfoMap.get(botName);
		boolean managed = Boolean.valueOf((String) bot.getBotProperties().getOrDefault("managed", false));
		if (managed && bot.getProcess() != null) {
			bot.getProcess().destroy();
		} else if (bot.getProcessInfo() != null) {
			JProcesses.killProcessGracefully(Integer.parseInt(bot.getProcessInfo().getPid()));
			JProcesses.killProcess(Integer.parseInt(bot.getProcessInfo().getPid()));
		}
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
			log.error(e);
		}
		return new Properties();
	}

	public Collection<BotInfo> getBotList() {
		return botInfoMap.values();
	}

	public void getBotData(BotInfo botInfo) {
		if (StringUtils.isBlank(StaticUtil.url)) {
			return;
		}
		try {
			boolean managed = Boolean.parseBoolean((String) botInfo.getBotProperties().getOrDefault("managed", false));
			if (managed) {
				String token = managerToken;
				if (botInfo.getProcessInfo() != null) {
					token = botInfo.getProcessInfo().getCommand().split("--server.manager.token=")[1];
				}
				String port = (String) botInfo.getBotProperties().get("port");
				String contextPath = (String) botInfo.getBotProperties().get("context");
				String url = String.format("%s:%s%s", StaticUtil.url, port, contextPath);
				String dataUrl = url + "/api/v2/data/stats?token=" + token;
				String data = HttpClientManager.getHttp(dataUrl, Collections.emptyList());
				botInfo.setStatsData(parser.fromJson(data, JsonObject.class));
			}
		} catch (Exception e) {
			log.error(e);
		}
	}

}
