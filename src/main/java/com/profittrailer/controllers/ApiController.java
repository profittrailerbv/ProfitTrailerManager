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
import java.net.MalformedURLException;

@RestController
@RequestMapping("/api/v1")
public class ApiController {

	@Autowired
	private ProcessService processService;
	private Gson parser;

	@PostConstruct
	public void init() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(BotInfo.class, new BotInfoSerializer());
		parser = gsonBuilder.create();
	}

	@GetMapping("/data")
	public String data(HttpServletRequest request) throws MalformedURLException {

		JsonObject object = new JsonObject();
		object.add("bots", parser.toJsonTree(processService.getBotList()));
		object.addProperty("baseUrl", StaticUtil.getBaseUrl(request));

		return object.toString();
	}

	@PostMapping("/startBot")
	public void startBot(String botName) {

		processService.startBot(botName);

	}

	@PostMapping("/stopBot")
	public void stopBot(String botName) {
		processService.stopBot(botName);
	}

	@GetMapping("/status")
	public String checkStatus(String botName) {
		JsonObject object = new JsonObject();
		object.add("bot", parser.toJsonTree(processService.getBotInfoMap().get(botName)));

		return object.toString();
	}
}
