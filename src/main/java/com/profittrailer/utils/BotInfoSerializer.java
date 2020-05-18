package com.profittrailer.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.profittrailer.models.BotInfo;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;

public class BotInfoSerializer implements JsonSerializer<BotInfo> {

	@Override
	public JsonElement serialize(BotInfo botInfo, Type type, JsonSerializationContext context) {
		JsonObject root = new JsonObject();
		root.addProperty("directory", botInfo.getDirectory());
		String siteName = (String) botInfo.getBotProperties().getOrDefault("siteName", "");

		siteName = StringUtils.isNotBlank(siteName)
				? siteName
				: botInfo.getDirectory() + " (Dir)";
		root.addProperty("siteName", siteName);
		root.addProperty("status", botInfo.getStatus());
		root.add("botProperties", new Gson().toJsonTree(botInfo.getBotProperties()));
		root.add("statsData", new Gson().toJsonTree(botInfo.getStatsData()));

		return root;
	}

}
