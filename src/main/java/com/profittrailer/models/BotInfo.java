package com.profittrailer.models;

import com.google.gson.JsonObject;
import lombok.Data;
import org.jutils.jprocesses.model.ProcessInfo;

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

	public BotInfo() {
	}

	public BotInfo(String botName) {
		this.directory = botName;
	}

	public String getStatus() {
		if (!initialized) {
			return "INITIALIZING";
		}
		return (process == null && processInfo == null)
				|| process != null && !process.isAlive()
				? "OFFLINE"
				: "ONLINE";
	}
}
