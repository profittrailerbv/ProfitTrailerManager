package com.profittrailer.models;

import com.google.gson.JsonObject;
import lombok.Data;
import org.jutils.jprocesses.model.ProcessInfo;

import java.time.LocalDateTime;
import java.util.Properties;

@Data
public class BotInfo {

	private String directory;
	private Properties botProperties;
	private String url;
	private transient boolean initialized;
	private transient Process process;
	private transient ProcessInfo processInfo;
	private JsonObject statsData;
	private JsonObject miscData;
	private transient LocalDateTime startDate;

	public BotInfo() {
	}

	public BotInfo(String botName) {
		this.directory = botName;
	}

	public String getStatus() {
		if (!initialized) {
			return "INITIALIZING";
		}

		if (startDate != null && startDate.plusSeconds(30).isAfter(LocalDateTime.now())) {
			return "STARTING";
		}

		return (process == null && processInfo == null)
				|| process != null && !process.isAlive()
				? "OFFLINE"
				: "ONLINE";
	}
}
