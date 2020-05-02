package com.profittrailer.application;

import com.profittrailer.services.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.annotation.PostConstruct;

@SpringBootApplication(scanBasePackages = "com.profittrailer")
@EnableWebMvc
public class Application {

	@Autowired
	private ProcessService processService;

	public static void main(String... args) {
		SpringApplication.run(Application.class, args);
	}

	@PostConstruct
	public void init() {
		processService.init();
	}
}
