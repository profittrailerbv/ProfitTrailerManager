package com.profittrailer.models;

import lombok.Data;

import java.util.Properties;

@Data
public class BotInfo {

	private String name;
	private Process process;
	private Properties botProperties;

	public BotInfo() {
	}

	public BotInfo(String botName) {
		this.name = botName;
	}

	public String getStatus() {
		return process == null || !process.isAlive()
				? "OFFLINE"
				: "ONLINE";
	}
}
