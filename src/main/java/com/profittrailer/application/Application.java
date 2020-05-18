package com.profittrailer.application;

import com.profittrailer.services.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.annotation.PostConstruct;

@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.profittrailer")
public class Application {

	@Autowired
	private ProcessService processService;
}
