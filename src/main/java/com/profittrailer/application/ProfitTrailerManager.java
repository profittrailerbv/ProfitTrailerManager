package com.profittrailer.application;

import com.profittrailer.form.ManagerForm;
import org.apache.catalina.Manager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.util.Map;
import java.util.TreeMap;

@SpringBootApplication(scanBasePackages = "com.profittrailer")
public class ProfitTrailerManager {

	@Value("${server.port:10000}")
	private String port;

	public static void main(String[] args) {
		Map<String, Object> props = new TreeMap<>();
		props.put("spring.thymeleaf.cache", false);
		props.put("spring.thymeleaf.enabled", true);
		props.put("spring.thymeleaf.prefix", "classpath:/templates/");
		props.put("spring.thymeleaf.suffix", ".html");
		props.put("spring.application.name", "ProfitTrailer Manager");
		props.put("spring.main.banner-mode", "off");
		props.put("server.compression.enabled", true);
		props.put("server.compression.mime-types", "text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json,application/xml");

		new SpringApplicationBuilder()
				.sources(ProfitTrailerManager.class)
				.properties(props)
				.logStartupInfo(false)
				.headless(false)
				.run(args);

	}

	@PostConstruct
	public void init() {
		if (hasGui()) {
			EventQueue.invokeLater(() -> {
				ManagerForm ex = new ManagerForm(port);
				ex.setVisible(true);
			});
		}
	}

	private static boolean hasGui() {
		String os = System.getProperty("os.name").toLowerCase();
		return os.contains("win")
				|| os.contains("nix")
				|| os.contains("nux") ||
				os.contains("mac os x");
	}
}
