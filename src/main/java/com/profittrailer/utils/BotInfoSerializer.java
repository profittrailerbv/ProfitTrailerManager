package com.profittrailer.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.profittrailer.models.BotInfo;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

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
		root.addProperty("url", url);
		root.add("botProperties", new Gson().toJsonTree(botInfo.getBotProperties()));

		JsonObject data = new JsonObject();
		if (botInfo.getStatsData() != null) {
			data.addProperty("totalSalesToday", botInfo.getStatsData().getAsJsonObject("basic").get("totalSalesToday").getAsDouble());
			data.addProperty("totalProfitToday", botInfo.getStatsData().getAsJsonObject("basic").get("totalProfitToday").getAsDouble());
			data.addProperty("totalProfitPercToday", botInfo.getStatsData().getAsJsonObject("basic").get("totalProfitPercToday").getAsDouble());
			data.addProperty("totalSalesYesterday", botInfo.getStatsData().getAsJsonObject("basic").get("totalSalesYesterday").getAsDouble());
			data.addProperty("totalProfitYesterday", botInfo.getStatsData().getAsJsonObject("basic").get("totalProfitYesterday").getAsDouble());
			data.addProperty("totalProfitPercYesterday", botInfo.getStatsData().getAsJsonObject("basic").get("totalProfitPercYesterday").getAsDouble());
			data.addProperty("totalSalesAllTime", botInfo.getStatsData().getAsJsonObject("basic").get("totalSales").getAsDouble());
			data.addProperty("totalProfitAllTime", botInfo.getStatsData().getAsJsonObject("basic").get("totalProfit").getAsDouble());
			data.addProperty("totalProfitPercAllTime", botInfo.getStatsData().getAsJsonObject("basic").get("totalProfitPerc").getAsDouble());
		}
		if (botInfo.getMiscData() != null) {
			double realBalance = botInfo.getMiscData().get("realBalance").getAsDouble();
			double tcv = botInfo.getMiscData().get("totalPairsCurrentValue").getAsDouble()
					+ botInfo.getMiscData().get("totalDCACurrentValue").getAsDouble()
					+ botInfo.getMiscData().get("totalPendingCurrentValue").getAsDouble()
					+ botInfo.getMiscData().get("totalDustCurrentValue").getAsDouble();
			double exchangeTcv = botInfo.getMiscData().get("totalExchangeCurrentValue").getAsDouble();
			if (exchangeTcv > 0) {
				tcv = exchangeTcv;
			}

			data.addProperty("exchange", botInfo.getMiscData().get("exchange").getAsString());
			data.addProperty("version", botInfo.getMiscData().get("version").getAsString());
			data.addProperty("market", botInfo.getMiscData().get("market").getAsString());

			data.addProperty("balance", realBalance);
			data.addProperty("tcv", realBalance + tcv);
		}
		if (botInfo.getPropertiesData() != null) {
			data.addProperty("paper", botInfo.getPropertiesData().get("testMode").getAsBoolean());
		}

		JsonArray jsonArray = new JsonArray();
		if (botInfo.getPairsData() != null) {
			data.addProperty("pairsTotal", botInfo.getPairsData().size());
			jsonArray.addAll(botInfo.getPairsData());
		}

		if (botInfo.getDcaData() != null) {
			data.addProperty("dcaTotal", botInfo.getDcaData().size());
			jsonArray.addAll(botInfo.getDcaData());
		}

		if (botInfo.getSalesData() != null) {
			long date = 0;
			double profit = 0;
			for (JsonElement element : botInfo.getSalesData()) {
				if (element.getAsJsonObject().get("soldDate").getAsLong() > date) {
					date = element.getAsJsonObject().get("soldDate").getAsLong();
					profit = element.getAsJsonObject().get("profit").getAsDouble();
				}
			}
			if (date > 0) {
				long minutes = StaticUtil.minutesLeft(date, Util.getUTCDateTime().toEpochSecond(ZoneOffset.UTC));
				data.addProperty("lastSaleMinutes", minutes);
				data.addProperty("lastSaleProfit", profit);
			}
		}

		double totalDown = 0;
		for (JsonElement element : jsonArray) {
			totalDown += (element.getAsJsonObject().get("currentValue").getAsDouble() - element.getAsJsonObject().get("totalCost").getAsDouble());
		}

		data.addProperty("diff", totalDown);

		root.add("data", data);
		return root;
	}

}
