package com.profittrailer.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.profittrailer.utils.Util;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.jutils.jprocesses.model.ProcessInfo;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Properties;

@Data
public class BotInfo {

	private String directory;
	private Properties botProperties = new Properties();
	private String url;
	private transient boolean initialized;
	private transient boolean lastUnlinkedStatus;
	private transient Process process;
	private transient ProcessInfo processInfo;
	private JsonObject statsData;
	private JsonObject miscData;
	private JsonObject propertiesData;
	private JsonObject accountData;
	private JsonArray pairsData;
	private JsonArray dcaData;
	private JsonArray pendingData;
	private JsonArray salesData;
	private transient LocalDateTime startDate;
	private transient LocalDateTime updateDate;
	private transient double profitToday;

	public BotInfo() {
	}

	public BotInfo(String botName) {
		this.directory = botName;
	}

	public String getStatus() {
		if (!initialized) {
			return "INITIALIZING";
		}

		if (startDate != null && startDate.plusSeconds(30).isAfter(Util.getDateTime())) {
			return "STARTING";
		}

		if (updateDate != null && updateDate.plusSeconds(30).isAfter(Util.getDateTime())) {
			return "UPDATING";
		}

		return (process == null && processInfo == null)
				|| process != null && !process.isAlive()
				|| processInfo != null && !StringUtils.containsIgnoreCase(processInfo.getName(), "java")
				? "OFFLINE"
				: "ONLINE";
	}

	public boolean isManaged() {
		if (lastUnlinkedStatus) {
			return false;
		}

		return Boolean.parseBoolean((String) botProperties.getOrDefault("managed", "false"))
				|| processInfo != null && StringUtils.containsIgnoreCase(processInfo.getName(), "java")
				|| process != null && process.isAlive();
	}

	public boolean isUnlinked(String botsLocation) {
		lastUnlinkedStatus = new File(botsLocation + "/" + directory + "/data/unlinked").exists();
		return lastUnlinkedStatus;
	}

	public boolean isInitialSetup() {
		return Boolean.parseBoolean((String) botProperties.getOrDefault("initialSetup", "false"));
	}

	public String getSiteName() {
		String siteName = (String) botProperties.getOrDefault("siteName", "");

		return StringUtils.isNotBlank(siteName)
				? siteName.equals("PTBOT")
				? siteName + "(" + directory + ")"
				: siteName
				: directory + " (Dir)";
	}

	public void setStatsData(JsonObject statsData) {
		this.statsData = statsData;
		if (statsData != null) {
			profitToday = statsData.getAsJsonObject("basic").get("totalProfitToday").getAsDouble();
		}
	}

	public void clearData() {
		miscData = null;
		statsData = null;
		propertiesData = null;
		pairsData = null;
		dcaData = null;
		pendingData = null;
		salesData = null;
	}
}
