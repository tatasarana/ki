package com.docotel.ki.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ThreadPoolConfig {
	@Value("${thread.pool.corePoolSize:10}")
	private short threadPoolCorePoolSize;
	@Value("${thread.pool.maxPoolSize:50}")
	private short threadPoolMaxPoolSize;

	@Bean
	public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setCorePoolSize(this.threadPoolCorePoolSize);
		threadPoolTaskExecutor.setMaxPoolSize(this.threadPoolMaxPoolSize);
		threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
		return threadPoolTaskExecutor;
	}
}