package com.profittrailer.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.profittrailer.models.BotInfo;
import com.profittrailer.services.ProcessService;
import com.profittrailer.utils.BotInfoSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ApiController {

	@Autowired
	private ProcessService processService;

	@GetMapping("/data")
	public String data() {

		JsonObject object = new JsonObject();
		object.add("bots", new Gson().toJsonTree(processService.getBotList()));

		return object.toString();
	}

	@PostMapping("/startBot")
	public void startBot(String botName) {

		processService.startBot(botName);

	}

	@GetMapping("/status")
	public String checkStatus(String botName) {
		GsonBuilder gson = new GsonBuilder();
		gson.registerTypeAdapter(BotInfo.class, new BotInfoSerializer());
		Gson parser = gson.create();

		JsonObject object = new JsonObject();
		object.add("bot", parser.toJsonTree(processService.getBotInfoMap().get(botName)));

		return object.toString();
	}
}
