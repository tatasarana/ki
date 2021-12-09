package com.docotel.ki.scheduler.job;

import com.docotel.ki.service.transaction.RegistrationService;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CertificateExpireJob implements Job {
	private static boolean synchOnProgress = false;

	private static Logger logger = LoggerFactory.getLogger(CertificateExpireJob.class);

	@Autowired
	private RegistrationService registrationService;

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		if (synchOnProgress) {
			logger.info("- PREVIOUS " + this.getClass().getSimpleName() + " PROCESS STILL ON GOING NOT EXECUTING JOB PROCESS -");
		} else {
			synchOnProgress = true;
			logger.info("- " + this.getClass().getSimpleName() + " JOB PROCESS STARTING -");
			// do whatever here
			registrationService.batchCertificateExpireJob();
			logger.info("- " + this.getClass().getSimpleName() + " JOB PROCESS DONE -");
			synchOnProgress = false;
		}
	}
}
