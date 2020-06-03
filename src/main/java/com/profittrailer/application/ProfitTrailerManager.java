package com.profittrailer.application;

import com.profittrailer.form.ManagerForm;
import com.profittrailer.utils.StaticUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

@Log4j2
@EnableScheduling
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

		props.put("server.servlet.session.cookie.http-only", true);
		props.put("server.servlet.session.cookie.max-age", TimeUnit.DAYS.toSeconds(1));
		props.put("server.servlet.session.timeout", TimeUnit.DAYS.toSeconds(1));
		props.put("server.servlet.session.persistent", true);

		props.put("spring.application.name", "ProfitTrailer Manager");
		props.put("spring.main.banner-mode", "off");
		props.put("server.compression.enabled", true);
		props.put("server.compression.mime-types", "text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json,application/xml");
		props.put("spring.resources.static-locations[0]", "file:src/main/resources/static/");
		props.put("spring.resources.static-locations[1]", "classpath:/static/");
		props.put("spring.resources.static-locations[2]", "file:src/main/js/");
		props.put("spring.resources.static-locations[3]", "file:classpath:/js/");

		new SpringApplicationBuilder()
				.sources(ProfitTrailerManager.class)
				.properties(props)
				.logStartupInfo(false)
				.headless(false)
				.run(args);

	}

	@PostConstruct
	public void init() throws IOException {
		File dataDir = new File("data");
		Files.createDirectories(dataDir.toPath());

		if (hasGui()) {
			EventQueue.invokeLater(() -> {
				ManagerForm ex = new ManagerForm(port, StaticUtil.randomSystemId);
				ex.setVisible(true);
				log.info("ProfitTrailer Manager is started");
				if (StringUtils.isNotBlank(StaticUtil.randomSystemId)) {
					System.out.println("Random System Id " + StaticUtil.randomSystemId);
				}
			});

		}
	}

	private static boolean hasGui() {
		String os = System.getProperty("os.name").toLowerCase();
		return os.contains("win")
				|| os.contains("mac os x");
	}
}
