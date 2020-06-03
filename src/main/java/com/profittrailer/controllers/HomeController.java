package com.profittrailer.controllers;

import com.profittrailer.models.BotInfo;
import com.profittrailer.services.ProcessService;
import com.profittrailer.utils.StaticUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.util.Collections;

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

	@GetMapping("/resetPassword")
	public String resetPassword() {
		return "reset";
	}

	@GetMapping("/status")
	public String checkStatus(HttpServletRequest request,
	                          @RequestParam(defaultValue = "") String directoryName,
	                          Model model) throws MalformedURLException {

		BotInfo botInfo = processService.getBotInfoMap().get(directoryName);
		String port = (String) botInfo.getBotProperties().get("port");
		String contextPath = (String) botInfo.getBotProperties().get("context");
		String url = String.format("%s:%s%s", StaticUtil.url, port, contextPath);
		model.addAttribute("siteName", processService.convertBotInfo(botInfo).getAsJsonObject().get("siteName").getAsString());
		model.addAttribute("botUrl", url);
		return "status";
	}
}