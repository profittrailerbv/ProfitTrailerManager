package com.profittrailer.controllers;

import com.google.gson.JsonObject;
import com.profittrailer.models.BotInfo;
import com.profittrailer.services.ProcessService;
import com.profittrailer.utils.HttpClientManager;
import com.profittrailer.utils.StaticUtil;
import com.profittrailer.utils.Util;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@Controller
public class HomeController {

	@Autowired
	private ProcessService processService;

	@GetMapping("/")
	public String homePage() {
		return "home";
	}

	@GetMapping("/login")
	public String login() {
		return "login";
	}

	@GetMapping("/settings")
	public String settings() {
		return "settings";
	}

	@GetMapping("/setPassword")
	public String setPassword() throws IOException {
		String currentPassword = Util.loadPasswordHash();
		if (StringUtils.isNotBlank(currentPassword)){
			return "redirect:/";
		}
		return "setpw";
	}

	@GetMapping("/resetPassword")
	public String resetPassword() throws IOException {
		return "resetpw";
	}

	@GetMapping("/status")
	public String checkStatus(@RequestParam(defaultValue = "") String directoryName,
	                          Model model) {

		BotInfo botInfo = processService.getBotInfoMap().get(directoryName);
		String port = (String) botInfo.getBotProperties().get("port");
		String contextPath = (String) botInfo.getBotProperties().get("context");
		String url = String.format("%s:%s%s", StaticUtil.url, port, contextPath);
		model.addAttribute("siteName", processService.convertBotInfo(botInfo).getAsJsonObject().get("siteName").getAsString());
		model.addAttribute("botUrl", url);
		return "status";
	}

	@ResponseBody
	@GetMapping("/testDiscord")
	public String testDiscord() throws IOException {
		Properties properties = Util.readApplicationProperties();
		String webHookUrl = (String) properties.get("server.settings.discord.webhook");

		List<NameValuePair> headers = new ArrayList<>();
		headers.add(new BasicNameValuePair("Content-type", "application/json"));

		JsonObject object = new JsonObject();
		object.addProperty("content", "**Password Reset Code**\nUse this code to reset your manager Password\n\nelroy is testing");
		HttpClientManager.postHttpJson(webHookUrl, object, headers);

		return "Done, Check Discord";
	}
}