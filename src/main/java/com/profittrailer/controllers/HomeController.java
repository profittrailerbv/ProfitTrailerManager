package com.profittrailer.controllers;

import com.profittrailer.services.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
import java.util.Properties;

@Controller
public class HomeController {

	@Value("${spring.application.name}")
	private String appName;
	@Autowired
	private ProcessService processService;

	@GetMapping("/")
	public String homePage(Model model) {

		model.addAttribute("appName", appName);
		model.addAttribute("bots", processService.getBotList());

		return "home";
	}

	@GetMapping("/startBot")
	public String startBot(String botName) {

		processService.startBot(botName);

		return "redirect:/";
	}

	@GetMapping("/status")
	public String checkStatus(String botName,
	                          Model model) {

		Properties botProperties = processService.getBotProperties(botName);
		model.addAttribute("bot", processService.getBotInfoMap().get(botName));
		model.addAttribute("botUrl", botProperties.getProperty("url"));
		model.addAttribute("bots", processService.getBotList());
		model.addAttribute("logs", Collections.emptyList());
		return "status";
	}
}
