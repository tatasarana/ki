package com.docotel.ki.scheduler.job;

import com.docotel.ki.model.transaction.TxLicense;
import com.docotel.ki.service.transaction.LicenseService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LicenseUpdateStatusJob implements Job {
	private static boolean synchPdfOnProgress = false;

	private static Logger logger = LoggerFactory.getLogger(LicenseUpdateStatusJob.class);

	@Autowired
	private LicenseService licenseService;

	@Autowired
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		if (synchPdfOnProgress) {
			logger.info("- PREVIOUS " + this.getClass().getSimpleName() + " PROCESS STILL ON GOING NOT EXECUTING JOB PROCESS -");
		} else {
			synchPdfOnProgress = true;
			logger.info("- " + this.getClass().getSimpleName() + " JOB PROCESS UPDATE STATUS STARTING -");

			threadPoolTaskExecutor.execute(() -> {
				List<TxLicense> txLicense = licenseService.selectAllLicenseByEndDate();
				if(txLicense!=null) {
					for (TxLicense license : txLicense) {
						if (!txLicense.isEmpty()) {
							license.setStatus(false);
							try {
								licenseService.insertLicense(license);
							} catch (Exception e) {
								logger.info("- PROSES UPDATE STATUS LICENSE " + license.getName() + " FAIL -", e);
							}
						}

					}
				}
				
				synchPdfOnProgress = false;
			});

			logger.info("- " + this.getClass().getSimpleName() + " JOB PROCESS UPDATE STATUS DONE -");
		}
	}
}
