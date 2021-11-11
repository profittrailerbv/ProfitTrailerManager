package com.profittrailer.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.profittrailer.models.BotInfo;
import com.profittrailer.services.ProcessService;
import com.profittrailer.utils.BotInfoSerializer;
import com.profittrailer.utils.StaticUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.util.Collection;

@Log4j2
@RestController
@RequestMapping("/api/v1/addon")
public class AddonApiController {

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
	public String addon(HttpServletRequest request) throws MalformedURLException {

		Collection<BotInfo> bots = processService.getAddonList(processService.isOnlyManaged());
		JsonObject object = new JsonObject();
		object.add("addons", parser.toJsonTree(bots));
		object.addProperty("baseUrl", StaticUtil.getBaseUrl(request));
		object.addProperty("demoServer", processService.isDemoServer());

		return object.toString();
	}

	@PostMapping("/restart")
	public void startBot(String directoryName) throws InterruptedException {
		if (processService.isDemoServer()) {
			return;
		}
		processService.stopBot(processService.getAddonInfoMap(), directoryName);
		Thread.sleep(5000);
		processService.startAddon(processService.getAddonInfoMap().get(directoryName));

	}

	@PostMapping("/stopAndUnlink")
	public void stopBot(String directoryName) {
		if (processService.isDemoServer()) {
			return;
		}
		processService.stopBot(processService.getAddonInfoMap(), directoryName);
		processService.unlinkBot(processService.getAddonInfoMap(), directoryName);
	}

	@GetMapping("/status")
	public String checkStatus(String directoryName) {
		JsonObject object = new JsonObject();
		object.add("bot", parser.toJsonTree(processService.getAddonInfoMap().get(directoryName)));

		return object.toString();
	}
}
