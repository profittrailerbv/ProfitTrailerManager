package com.profittrailer.application;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

@Configuration
public class ExecutorConfig {


	@Getter
	private ScheduledExecutorService scheduledExecutor;

	@Bean(name = "taskScheduler")
	public TaskScheduler taskScheduler() {
		ThreadFactory namedThreadFactory =	new ThreadFactoryBuilder().setNameFormat("ptmanager-%d").build();
		scheduledExecutor = Executors.newScheduledThreadPool(10, namedThreadFactory);
		return new ConcurrentTaskScheduler(scheduledExecutor);
	}

}
