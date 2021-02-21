package com.profittrailer.application;

import com.profittrailer.form.ManagerForm;
import com.profittrailer.utils.StaticUtil;
import com.profittrailer.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

@Log4j2
@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.profittrailer")
public class ProfitTrailerManager {

	@Value("${server.port:10000}")
	private String port;
	@Autowired
	private ResourceLoader resourceLoader;

	public static void main(String[] args) {
		String port = Util.readApplicationProperties().getOrDefault("server.port", 10000).toString();
		int timeout = NumberUtils.toInt((String) Util.readApplicationProperties().getOrDefault("server.session.timeout", 7));

		Map<String, Object> props = new TreeMap<>();
		props.put("spring.thymeleaf.cache", false);
		props.put("spring.thymeleaf.enabled", true);
		props.put("spring.thymeleaf.prefix", "classpath:/templates/");
		props.put("spring.thymeleaf.suffix", ".html");

		props.put("server.servlet.session.cookie.http-only", true);
		props.put("server.servlet.session.cookie.max-age", TimeUnit.DAYS.toSeconds(timeout));
		props.put("server.servlet.session.timeout", TimeUnit.DAYS.toSeconds(timeout));
		props.put("server.servlet.session.persistent", true);
		props.put("server.servlet.session.cookie.name", String.format(Locale.US, "JSESSIONID-PTM_%s", port));

		props.put("spring.application.name", "ProfitTrailer Manager");
		props.put("spring.main.banner-mode", "off");
		props.put("server.compression.enabled", true);
		props.put("server.compression.mime-types", "text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json,application/xml");
		props.put("spring.resources.static-locations[0]", "file:src/main/resources/static/");
		props.put("spring.resources.static-locations[1]", "classpath:/static/");
		props.put("spring.resources.static-locations[2]", "file:src/main/js/");
		props.put("spring.resources.static-locations[3]", "file:classpath:/js/");

		System.setProperty("timezone", TimeZone.getDefault().getID());
		StaticUtil.randomNumber = Util.getDateTime().toEpochSecond(ZoneOffset.UTC);


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
				ManagerForm ex = new ManagerForm(resourceLoader, port, StaticUtil.randomSystemId);
				ex.setVisible(true);
			});
		}

		log.info("ProfitTrailer Manager version: {} started", Util.getVersion());
		if (StringUtils.isNotBlank(StaticUtil.randomSystemId)) {
			System.out.println("Random System Id " + StaticUtil.randomSystemId);
		}
	}

	private static boolean hasGui() {
		String os = System.getProperty("os.name").toLowerCase();
		return os.contains("win")
				|| os.contains("mac os x");
	}
}
