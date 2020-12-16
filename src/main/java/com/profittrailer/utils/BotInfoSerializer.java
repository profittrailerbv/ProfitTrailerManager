package com.profittrailer.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.profittrailer.models.BotInfo;

import java.lang.reflect.Type;
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
		root.addProperty("managed", botInfo.isManaged());
		root.addProperty("url", url);
		root.addProperty("cpu", botInfo.getProcessInfo() != null ? botInfo.getProcessInfo().getCpuUsage() : "0");
		root.addProperty("ram", botInfo.getProcessInfo() != null ? botInfo.getProcessInfo().getPhysicalMemory() : "0");
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
			double tcv = StaticUtil.extractTCV(botInfo.getMiscData());

			data.addProperty("exchange", botInfo.getMiscData().get("exchange").getAsString());
			data.addProperty("version", botInfo.getMiscData().get("version").getAsString());
			data.addProperty("market", botInfo.getMiscData().get("market").getAsString());

			data.addProperty("balance", realBalance);
			data.addProperty("tcv", tcv);
			data.addProperty("conversionRate", 1);
			if (botInfo.getMiscData().has("priceDataUSDConversionRate")) {
				data.addProperty("conversionRate", botInfo.getMiscData().get("priceDataUSDConversionRate").getAsDouble());
			}
		}
		if (botInfo.getPropertiesData() != null) {
			data.addProperty("paper", botInfo.getPropertiesData().get("testMode").getAsBoolean() || botInfo.getPropertiesData().get("testnet").getAsBoolean());
			data.addProperty("config", botInfo.getPropertiesData().get("activeConfig").getAsString());
			data.addProperty("sellOnlyMode", botInfo.getPropertiesData().get("sellOnlyMode").getAsBoolean());
		}

		if (botInfo.getAccountData() != null) {
			int guiPrecision = 5;
			if (botInfo.getAccountData().has("GUI_PRECISION")) {
				guiPrecision = Math.min (guiPrecision, botInfo.getAccountData().get("GUI_PRECISION").getAsInt());
			}
			data.addProperty("guiPrecision", guiPrecision);
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

		if (botInfo.getPendingData() != null) {
			data.addProperty("pendingTotal", botInfo.getPendingData().size());
			jsonArray.addAll(botInfo.getPendingData());
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
			double fee = element.getAsJsonObject().get("fee").getAsDouble();
			double currentValue = element.getAsJsonObject().get("currentValue").getAsDouble() * (1 - fee / 100);
			totalDown += (currentValue - element.getAsJsonObject().get("totalCost").getAsDouble());
		}

		data.addProperty("diff", totalDown);

		root.add("data", data);
		return root;
	}

}
