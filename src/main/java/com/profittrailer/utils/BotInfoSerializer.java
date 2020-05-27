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

		String port = (String) botInfo.getBotProperties().get("port");
		String contextPath = (String) botInfo.getBotProperties().get("context");
		String url = String.format("%s:%s%s", StaticUtil.url, port, contextPath);

		root.addProperty("siteName", siteName);
		root.addProperty("status", botInfo.getStatus());
		root.addProperty("url", url);
		root.add("botProperties", new Gson().toJsonTree(botInfo.getBotProperties()));

		JsonObject data = new JsonObject();
		if (botInfo.getStatsData() != null) {
			data.addProperty("totalProfitToday", botInfo.getStatsData().getAsJsonObject("basic").get("totalProfitToday").getAsDouble());
			data.addProperty("totalProfitPercToday", botInfo.getStatsData().getAsJsonObject("basic").get("totalProfitPercToday").getAsDouble());
			data.addProperty("totalProfitYesterday", botInfo.getStatsData().getAsJsonObject("basic").get("totalProfitYesterday").getAsDouble());
			data.addProperty("totalProfitPercYesterday", botInfo.getStatsData().getAsJsonObject("basic").get("totalProfitPercYesterday").getAsDouble());
		}
		if (botInfo.getMiscData() != null) {
			data.addProperty("version", botInfo.getMiscData().get("version").getAsString());
		}
		if (botInfo.getPropertiesData() != null) {
			data.addProperty("paper", botInfo.getPropertiesData().get("testMode").getAsString());
		}
		root.add("data", data);
		return root;
	}

}
