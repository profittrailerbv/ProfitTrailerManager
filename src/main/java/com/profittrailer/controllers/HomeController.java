package com.profittrailer.controllers;

import com.profittrailer.models.BotInfo;
import com.profittrailer.services.ProcessService;
import com.profittrailer.utils.StaticUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.util.Collections;

@Controller
public class HomeController {

	@Value("${spring.application.name}")
	private String appName;
	@Autowired
	private ProcessService processService;

	@GetMapping("/")
	public String homePage(HttpServletRequest request) throws MalformedURLException {
		StaticUtil.url = StaticUtil.getBaseUrl(request);
		return "home";
	}


	@GetMapping("/status")
	public String checkStatus(HttpServletRequest request,
	                          String botName,
	                          Model model) throws MalformedURLException {

		BotInfo botInfo = processService.getBotInfoMap().get(botName);
		String port = (String) botInfo.getBotProperties().get("port");
		String contextPath = (String) botInfo.getBotProperties().get("context");
		String url = String.format("%s:%s%s", StaticUtil.getBaseUrl(request), port, contextPath);
		model.addAttribute("bot", botInfo);
		model.addAttribute("botUrl", url);
		model.addAttribute("bots", processService.getBotList());
		model.addAttribute("logs", Collections.emptyList());
		return "status";
	}
}
