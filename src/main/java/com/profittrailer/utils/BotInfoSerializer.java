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
		root.addProperty("name", botInfo.getName());
		root.addProperty("status", botInfo.getStatus());
		root.add("botProperties", new Gson().toJsonTree(botInfo.getBotProperties()));

		return root;
	}

}
