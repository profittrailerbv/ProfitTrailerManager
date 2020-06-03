package com.profittrailer.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.profittrailer.models.BotInfo;
import com.profittrailer.services.ProcessService;
import com.profittrailer.utils.BotInfoSerializer;
import com.profittrailer.utils.StaticUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;

@RestController
@RequestMapping("/api/v1")
public class ApiController {

	@Autowired
	private ProcessService processService;
	private Gson parser;
	private boolean onlyManaged = true;

	@PostConstruct
	public void init() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(BotInfo.class, new BotInfoSerializer());
		parser = gsonBuilder.create();
	}

	@GetMapping("/data")
	public String data(HttpServletRequest request) throws MalformedURLException {

		JsonObject object = new JsonObject();
		object.add("bots", parser.toJsonTree(processService.getBotList(onlyManaged)));
		object.addProperty("baseUrl", StaticUtil.getBaseUrl(request));
		object.addProperty("demoServer", processService.isDemoServer());

		return object.toString();
	}

	@PostMapping("/restartBot")
	public void startBot(String directoryName) throws InterruptedException {
		if (processService.isDemoServer()) {
			return;
		}
		processService.stopBot(directoryName);
		Thread.sleep(5000);
		processService.startBot(directoryName);

	}

	@PostMapping("/stopBot")
	public void stopBot(String directoryName) {
		if (processService.isDemoServer()) {
			return;
		}
		processService.stopBot(directoryName);
	}

	@GetMapping("/status")
	public String checkStatus(String directoryName) {
		JsonObject object = new JsonObject();
		object.add("bot", parser.toJsonTree(processService.getBotInfoMap().get(directoryName)));

		return object.toString();
	}

	@GetMapping("/toggleCards")
	public String getToggle() {
		JsonObject object = new JsonObject();
		object.addProperty("onlyManaged", onlyManaged);
		return object.toString();
	}

	@PostMapping("/toggleCards")
	public String postToggle() {
		if (!processService.isDemoServer()) {
			onlyManaged = !onlyManaged;
		}
		JsonObject object = new JsonObject();
		object.addProperty("onlyManaged", onlyManaged);
		return object.toString();
	}

	@PostMapping("/shutdown")
	public void shutdown() {
		if (processService.isDemoServer()) {
			return;
		}
		System.exit(0);
	}

	@GetMapping("/toggleData")
	public String toggleData() {

		JsonObject object = new JsonObject();
		object.addProperty("latestVersion", processService.getLatestVersion());

		return object.toString();
	}

	@PostMapping("/updateBot")
	public void updateBot(String directoryName,
	                      String forceUrl) {

		processService.updateBot(directoryName, forceUrl);
		//startBot(directoryName);
	}

	@GetMapping("/linkout")
	public void linkOut(String directoryName,
	                    HttpServletResponse response) throws Exception {
		JsonObject object = new JsonObject();
		object.add("bot", parser.toJsonTree(processService.getBotInfoMap().get(directoryName)));

		BotInfo botInfo = processService.getBotInfoMap().get(directoryName);
		String token = processService.getSSOKey(directoryName);
		String redirectUrl = processService.createUrl(botInfo, "?sso=" + token, false);
		response.sendRedirect(redirectUrl);
	}
}
