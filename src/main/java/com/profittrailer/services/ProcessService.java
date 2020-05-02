package com.profittrailer.services;

import com.profittrailer.models.BotInfo;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
@Getter
@Log4j2
public class ProcessService {

	@Value("${server.bots.directory}")
	private String botsLocation;
	private Map<String, BotInfo> botInfoMap = new HashMap<>();

	public void init() {
		File botsDirectory = new File(botsLocation);
		File[] files = botsDirectory.listFiles();
		if (files != null) {
			Arrays.stream(files)
					.map(File::getName)
					.forEach(e -> {
						BotInfo botInfo = new BotInfo(e);
						botInfo.setBotProperties(getBotProperties(e));
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
				"ProfitTrailer.jar");

		builder.directory(new File(botsLocation + "/" + botName));

		try {
			if (!botInfoMap.containsKey(botName)
					|| botInfoMap.get(botName).getProcess() == null
					|| !botInfoMap.get(botName).getProcess().isAlive()) {
				Process process = builder.start();
				BotInfo botInfo = botInfoMap.getOrDefault(botName, new BotInfo(botName));
				botInfo.setProcess(process);
				botInfoMap.put(botName, botInfo);
			}
		} catch (Exception e) {
			log.error(e);
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
}
