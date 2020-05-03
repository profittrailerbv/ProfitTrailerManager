package com.profittrailer.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.profittrailer.services.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Properties;

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
		Gson gson = new Gson();
		JsonObject object = new JsonObject();
		Properties botProperties = processService.getBotProperties(botName);
		object.add("bot", gson.toJsonTree(processService.getBotInfoMap().get(botName)));
		object.addProperty("botUrl", botProperties.getProperty("url"));
		object.add("bots", gson.toJsonTree(processService.getBotList()));

		return object.toString();
	}
}
