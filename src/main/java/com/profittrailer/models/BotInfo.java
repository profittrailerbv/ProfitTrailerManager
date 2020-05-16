package com.profittrailer.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Properties;

@Data
public class BotInfo {

	private String name;
	private transient Process process;
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
