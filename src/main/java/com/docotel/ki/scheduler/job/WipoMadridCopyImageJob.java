package com.docotel.ki.scheduler.job;

import com.docotel.ki.model.wipo.ImageFileList;
import com.docotel.ki.service.WipoService;
import com.docotel.ki.service.transaction.MadridService;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class WipoMadridCopyImageJob implements Job {
	private static boolean synchImageCopyOnProgress = false;

	private static Logger logger = LoggerFactory.getLogger(WipoMadridCopyImageJob.class);

	@Autowired
	private WipoService wipoService;
	
	@Autowired
	private MadridService madridService;

	@Autowired
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;

	@Override 
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		if (synchImageCopyOnProgress) {
			logger.info("- PREVIOUS " + this.getClass().getSimpleName() + " PROCESS STILL ON GOING NOT EXECUTING JOB PROCESS -");
		} else {
			synchImageCopyOnProgress = true;
			logger.info("- " + this.getClass().getSimpleName() + " JOB PROCESS COPYING STARTING -");

			threadPoolTaskExecutor.execute(() -> {
				List<ImageFileList> fileList = wipoService.selectNotProcessedCopyImageFile();
				if(fileList!=null) {
					for (ImageFileList file : fileList) {
						if (!fileList.isEmpty()) {
							try {
								madridService.prosesCopyImageFromXML(file);
							} catch (IOException e) {
								logger.info("- PROSES COPY IMAGE FILE " + file.getName() + " FAIL -", e);
							}
						}
						
					}
				}
				
				synchImageCopyOnProgress = false;
			});

			logger.info("- " + this.getClass().getSimpleName() + " JOB PROCESS COPYING DONE -");
		}
	}
}
