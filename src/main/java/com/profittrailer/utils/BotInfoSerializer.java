package com.profittrailer.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.profittrailer.models.BotInfo;

import java.lang.reflect.Type;

public class BotInfoSerializer implements JsonSerializer<BotInfo> {

	@Override
	public JsonElement serialize(BotInfo botInfo, Type type, JsonSerializationContext context) {
		JsonObject root = new JsonObject();
		root.addProperty("directory", botInfo.getDirectory());

		String port = (String) botInfo.getBotProperties().get("port");
		String contextPath = (String) botInfo.getBotProperties().get("context");
		String url = String.format("%s:%s%s", StaticUtil.url, port, contextPath);

		root.addProperty("siteName", botInfo.getSiteName());
		root.addProperty("status", botInfo.getStatus());
		root.addProperty("initialSetup", botInfo.isInitialSetup());
		root.addProperty("managed", botInfo.isManaged());
		root.addProperty("url", url);
		root.addProperty("cpu", botInfo.getProcessInfo() != null ? botInfo.getProcessInfo().getCpuUsage() : "0");
		root.addProperty("ram", botInfo.getProcessInfo() != null ? botInfo.getProcessInfo().getPhysicalMemory() : "0");
		root.add("botProperties", new Gson().toJsonTree(botInfo.getBotProperties()));

		root.add("data", botInfo.getBotData());
		return root;
	}

}
