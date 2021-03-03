package com.profittrailer.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.profittrailer.utils.StaticUtil;
import com.profittrailer.utils.Util;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.jutils.jprocesses.model.ProcessInfo;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Properties;

@Data
public class BotInfo {

	private String path;
	private String directory;
	private Properties botProperties = new Properties();
	private String url;
	private boolean addOn = false;

	private transient boolean initialized;
	private transient boolean lastUnlinkedStatus;
	private transient Process process;
	private transient ProcessInfo processInfo;
	private transient LocalDateTime startDelay;
	private transient LocalDateTime updateDate;

	//These objects only store the data we actually need
	private transient JsonObject globalStats = new JsonObject();
	private transient JsonObject tcvData = new JsonObject();
	private transient JsonObject botData = new JsonObject();

	public BotInfo() {
	}

	public BotInfo(String path, String botName) {
		this.path = path;
		this.directory = botName;
	}

	public String getStatus() {
		if (isAddOn()) {
			return getAddonStatus();
		}

		if (!initialized) {
			return "INITIALIZING";
		}

		if (startDelay != null && startDelay.isAfter(Util.getDateTime())) {
			return "STARTING";
		}

		if (updateDate != null && updateDate.plusSeconds(5).isAfter(Util.getDateTime())) {
			return "UPDATING";
		}

		return (process == null && processInfo == null)
				|| process != null && !process.isAlive()
				|| processInfo != null && !StringUtils.containsIgnoreCase(processInfo.getName(), "java")
				? "OFFLINE"
				: "ONLINE";
	}

	private String getAddonStatus() {
		if (!initialized) {
			return "INITIALIZING";
		}

		if (startDelay != null && startDelay.isAfter(Util.getDateTime())) {
			return "STARTING";
		}

		if (updateDate != null && updateDate.plusSeconds(30).isAfter(Util.getDateTime())) {
			return "UPDATING";
		}

		return processInfo == null
				|| !StringUtils.containsIgnoreCase(processInfo.getCommand(), "pt-feeder.dll")
				? "OFFLINE"
				: "ONLINE";
	}

	public boolean isManaged() {
		if (isAddOn()) {
			return isManagedAddon();
		}

		if (lastUnlinkedStatus) {
			return false;
		}

		return Boolean.parseBoolean((String) botProperties.getOrDefault("managed", "false"));
	}

	private boolean isManagedAddon() {
		if (lastUnlinkedStatus) {
			return false;
		}

		return processInfo != null
				&& StringUtils.containsIgnoreCase(processInfo.getCommand(), "pt-feeder.dll");
	}

	public boolean isUnlinked() {
		lastUnlinkedStatus = new File(path + "/data/unlinked").exists();
		return lastUnlinkedStatus;
	}

	public boolean isInitialSetup() {
		return Boolean.parseBoolean((String) botProperties.getOrDefault("initialSetup", "false"));
	}

	public String getSiteName() {
		if (isAddOn()) {
			return directory;
		}

		String siteName = (String) botProperties.getOrDefault("siteName", "");

		return StringUtils.isNotBlank(siteName)
				? siteName.equals("PTBOT")
				? siteName + "(" + directory + ")"
				: siteName
				: directory + " (Dir)";
	}

	public void setMiscData(JsonObject miscData) {
		if (miscData != null) {
			globalStats.addProperty("priceDataUSDConversionRate", getJsonObjectData("priceDataUSDConversionRate", miscData));
			globalStats.addProperty("market", getJsonObjectData("market", miscData));
			globalStats.addProperty("accountId", getJsonObjectData("accountId", miscData));

			tcvData.addProperty("realBalance", getJsonObjectData("realBalance", miscData));
			tcvData.addProperty("totalPairsCurrentValue", getJsonObjectData("totalPairsCurrentValue", miscData));
			tcvData.addProperty("totalDCACurrentValue", getJsonObjectData("totalDCACurrentValue", miscData));
			tcvData.addProperty("totalPendingCurrentValue", getJsonObjectData("totalPendingCurrentValue", miscData));
			tcvData.addProperty("totalDustCurrentValue", getJsonObjectData("totalDustCurrentValue", miscData));
			tcvData.addProperty("totalExchangeCurrentValue", getJsonObjectData("totalExchangeCurrentValue", miscData));

			// I know there are some duplicate entries for now
			botData.addProperty("exchange", miscData.get("exchange").getAsString());
			botData.addProperty("version", miscData.get("version").getAsString());
			botData.addProperty("market", miscData.get("market").getAsString());
			botData.addProperty("balance", miscData.get("realBalance").getAsDouble());
			botData.addProperty("tcv", StaticUtil.extractTCV(tcvData));
		}
	}

	public void setPropertiesData(JsonObject propertiesData) {
		if (propertiesData != null) {
			globalStats.addProperty("testMode", getJsonObjectData("testMode", propertiesData));
			globalStats.addProperty("testnet", getJsonObjectData("testnet", propertiesData));

			botData.addProperty("paper", propertiesData.get("testMode").getAsBoolean() || propertiesData.get("testnet").getAsBoolean());
			botData.addProperty("config", propertiesData.get("activeConfig").getAsString());
			botData.addProperty("sellOnlyMode", propertiesData.get("sellOnlyMode").getAsBoolean());
		}
	}

	public void setAccountData(JsonObject accountData) {
		if (accountData != null) {
			int guiPrecision = 5;
			if (accountData.has("GUI_PRECISION")) {
				guiPrecision = Math.min(guiPrecision, accountData.get("GUI_PRECISION").getAsInt());
			}
			botData.addProperty("guiPrecision", guiPrecision);
		}
	}

	public void setCoinsData(JsonArray pairsData,
	                         JsonArray dcaData,
	                         JsonArray pendingData) {

		JsonArray jsonArray = new JsonArray();
		if (pairsData != null) {
			botData.addProperty("pairsTotal", pairsData.size());
			jsonArray.addAll(pairsData);
		}

		if (dcaData != null) {
			botData.addProperty("dcaTotal", dcaData.size());
			jsonArray.addAll(dcaData);
		}

		if (pendingData != null) {
			botData.addProperty("pendingTotal", pendingData.size());
			jsonArray.addAll(pendingData);
		}

		double totalDown = 0;
		for (JsonElement element : jsonArray) {
			double fee = element.getAsJsonObject().get("fee").getAsDouble();
			double currentValue = element.getAsJsonObject().get("currentValue").getAsDouble() * (1 - fee / 100);
			totalDown += (currentValue - element.getAsJsonObject().get("totalCost").getAsDouble());
		}

		botData.addProperty("diff", totalDown);
	}

	public void setSalesData(JsonArray salesData) {
		if (salesData != null) {
			long date = 0;
			double profit = 0;
			for (JsonElement element : salesData) {
				if (element.getAsJsonObject().get("soldDate").getAsLong() > date) {
					date = element.getAsJsonObject().get("soldDate").getAsLong();
					profit = element.getAsJsonObject().get("profit").getAsDouble();
				}
			}
			if (date > 0) {
				long minutes = StaticUtil.minutesLeft(date, Util.getUTCDateTime().toEpochSecond(ZoneOffset.UTC));
				botData.addProperty("lastSaleMinutesString", Util.minutesToHoursAndDays(minutes));
				botData.addProperty("lastSaleProfit", profit);
			}
		}
	}

	public void setStatsData(JsonObject statsData) {
		if (statsData != null) {
			globalStats.addProperty("totalProfitLastMonth", getJsonObjectData("totalProfitLastMonth", statsData.getAsJsonObject("basic")));
			globalStats.addProperty("totalProfitToday", getJsonObjectData("totalProfitToday", statsData.getAsJsonObject("basic")));
			globalStats.addProperty("totalProfitThisMonth", getJsonObjectData("totalProfitThisMonth", statsData.getAsJsonObject("basic")));
			globalStats.addProperty("totalProfit", getJsonObjectData("totalProfit", statsData.getAsJsonObject("basic")));

			botData.addProperty("totalSalesToday", statsData.getAsJsonObject("basic").get("totalSalesToday").getAsDouble());
			botData.addProperty("totalProfitToday", statsData.getAsJsonObject("basic").get("totalProfitToday").getAsDouble());
			botData.addProperty("totalProfitPercToday", statsData.getAsJsonObject("basic").get("totalProfitPercToday").getAsDouble());
			botData.addProperty("totalSalesYesterday", statsData.getAsJsonObject("basic").get("totalSalesYesterday").getAsDouble());
			botData.addProperty("totalProfitYesterday", statsData.getAsJsonObject("basic").get("totalProfitYesterday").getAsDouble());
			botData.addProperty("totalProfitPercYesterday", statsData.getAsJsonObject("basic").get("totalProfitPercYesterday").getAsDouble());
			botData.addProperty("totalSalesAllTime", statsData.getAsJsonObject("basic").get("totalSales").getAsDouble());
			botData.addProperty("totalProfitAllTime", statsData.getAsJsonObject("basic").get("totalProfit").getAsDouble());
			botData.addProperty("totalProfitPercAllTime", statsData.getAsJsonObject("basic").get("totalProfitPerc").getAsDouble());
		}
	}

	private String getJsonObjectData(String key, JsonObject object) {
		if (object.has(key)) {
			return object.get(key).getAsString();
		}
		return null;
	}

	public void clearData() {
		globalStats = new JsonObject();
		tcvData = new JsonObject();
		botData = new JsonObject();
	}
}
