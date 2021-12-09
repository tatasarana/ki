package com.docotel.ki.scheduler.config;

import com.docotel.ki.scheduler.job.*;
import com.docotel.ki.scheduler.workflow.MonitoringTasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "com.docotel.ki.scheduling", name="enabled", havingValue="true", matchIfMissing = true)
public class QuartzConfig {
	@Autowired
	private Environment environment;

	@Autowired
	private ApplicationContext applicationContext;

	// REGISTRATION ONLINE LISTER SCHEDULER BEAN CONFIGS
	/*@Qualifier("regOnlineJobDetailFactoryBean")
	@Bean
	public JobDetailFactoryBean regOnlineJobDetailFactoryBean() {
		JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
		jobDetailFactoryBean.setJobClass(RegistrasiOnlineJob.class);
		jobDetailFactoryBean.setDescription("Invoke " + RegistrasiOnlineJob.class.getSimpleName());
		jobDetailFactoryBean.setDurability(true);
		return jobDetailFactoryBean;
	}
	
	@Qualifier("regOnlineCronTriggerFactoryBean")
	@Bean
	public CronTriggerFactoryBean regOnlineCronTriggerFactoryBean() {
		CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
		cronTriggerFactoryBean.setJobDetail(regOnlineJobDetailFactoryBean().getObject());
		cronTriggerFactoryBean.setCronExpression(environment.getProperty("scheduler.cron.expression.regOnline.job"));
		cronTriggerFactoryBean.setDescription("Registrasi Online Job Trigger");
		return cronTriggerFactoryBean;
	}*/

	// CERTIFICATE EXPIRE LISTER SCHEDULER BEAN CONFIGS
	@Qualifier("certificateExpireJobDetailFactoryBean")
	@Bean
	public JobDetailFactoryBean certificateExpireJobDetailFactoryBean() {
		JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
		jobDetailFactoryBean.setJobClass(CertificateExpireJob.class);
		jobDetailFactoryBean.setDescription("Invoke " + CertificateExpireJob.class.getSimpleName());
		jobDetailFactoryBean.setDurability(true);
		return jobDetailFactoryBean;
	}

//	@Qualifier("certificateExpireCronTriggerFactoryBean")
//	@Bean
//	public CronTriggerFactoryBean certificateExpireCronTriggerFactoryBean() {
//		CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
//		cronTriggerFactoryBean.setJobDetail(certificateExpireJobDetailFactoryBean().getObject());
//		cronTriggerFactoryBean.setCronExpression(environment.getProperty("scheduler.cron.expression.certificate.expire.job"));
//		cronTriggerFactoryBean.setDescription("Certificate ExpireJob Trigger");
//		return cronTriggerFactoryBean;
//	}
	
	//import class 
	// Remark 17-08-2019
	/*@Qualifier("importClassJobDetailFactoryBean")
	@Bean
	public JobDetailFactoryBean importClassJobDetailFactoryBean() {
		JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
		jobDetailFactoryBean.setJobClass(ImportClassJob.class);
		jobDetailFactoryBean.setDescription("Invoke " + ImportClassJob.class.getSimpleName());
		jobDetailFactoryBean.setDurability(true);
		return jobDetailFactoryBean;
	}
	
	@Qualifier("importClassCronTriggerFactoryBean")
	@Bean
	public CronTriggerFactoryBean importClassCronTriggerFactoryBean() {
		CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
		cronTriggerFactoryBean.setJobDetail(importClassJobDetailFactoryBean().getObject());
		cronTriggerFactoryBean.setCronExpression(environment.getProperty("scheduler.cron.expression.import.class.job"));
		cronTriggerFactoryBean.setDescription("Import Class Job Trigger");
		return cronTriggerFactoryBean;
	}*/
	
		
	// WIPO FILE LISTER SCHEDULER BEAN CONFIGS
	@Qualifier("wipoFileListerJobDetailFactoryBean")
	@Bean
	public JobDetailFactoryBean wipoFileListerJobDetailFactoryBean() {
		JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
		jobDetailFactoryBean.setJobClass(WipoMadridFileListerJob.class);
		jobDetailFactoryBean.setDescription("Invoke " + WipoMadridFileListerJob.class.getSimpleName());
		jobDetailFactoryBean.setDurability(true);
		return jobDetailFactoryBean;
	}

	@Qualifier("wipoFileListerCronTriggerFactoryBean")
	@Bean
	public CronTriggerFactoryBean wipoFileListerCronTriggerFactoryBean() {
		CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
		cronTriggerFactoryBean.setJobDetail(wipoFileListerJobDetailFactoryBean().getObject());
		cronTriggerFactoryBean.setCronExpression(environment.getProperty("scheduler.cron.expression.wipo.madrid.file.lister.job"));
		cronTriggerFactoryBean.setDescription("WIPO File Lister Job Trigger");
		return cronTriggerFactoryBean;
	}

	// WIPO FILE DOWNLOAD SCHEDULER BEAN CONFIGS 
	@Qualifier("wipoFileDownloadJobDetailFactoryBean")
	@Bean
	public JobDetailFactoryBean wipoFileDownloadJobDetailFactoryBean() {
		JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
		jobDetailFactoryBean.setJobClass(WipoMadridFileDownloadJob.class);
		jobDetailFactoryBean.setDescription("Invoke " + WipoMadridFileDownloadJob.class.getSimpleName());
		jobDetailFactoryBean.setDurability(true);
		return jobDetailFactoryBean;
	}

	@Qualifier("wipoFileDownloadCronTriggerFactoryBean")
	@Bean
	public CronTriggerFactoryBean wipoFileDownloadCronTriggerFactoryBean() {
		CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
		cronTriggerFactoryBean.setJobDetail(wipoFileDownloadJobDetailFactoryBean().getObject());
		cronTriggerFactoryBean.setCronExpression(environment.getProperty("scheduler.cron.expression.wipo.madrid.file.downloader.job"));
		cronTriggerFactoryBean.setDescription("WIPO File Download Job Trigger");
		return cronTriggerFactoryBean;
	}
	
	// WIPO COPY IMAGE SCHEDULER BEAN CONFIGS
	@Qualifier("wipoCopyIMageJobDetailFactoryBean")
	@Bean
	public JobDetailFactoryBean wipoCopyIMageJobDetailFactoryBean() {
		JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
		jobDetailFactoryBean.setJobClass(WipoMadridCopyImageJob.class);
		jobDetailFactoryBean.setDescription("Invoke " + WipoMadridCopyImageJob.class.getSimpleName());
		jobDetailFactoryBean.setDurability(true);
		return jobDetailFactoryBean;
	}

	@Qualifier("wipoCopyImageCronTriggerFactoryBean")
	@Bean
	public CronTriggerFactoryBean wipoCopyImageCronTriggerFactoryBean() {
		CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
		cronTriggerFactoryBean.setJobDetail(wipoCopyIMageJobDetailFactoryBean().getObject());
		cronTriggerFactoryBean.setCronExpression(environment.getProperty("scheduler.cron.expression.wipo.madrid.copy.image.job"));
		cronTriggerFactoryBean.setDescription("WIPO Copy Image Job Trigger");
		return cronTriggerFactoryBean;
	}

	// WIPO COPY XML SCHEDULER BEAN CONFIGS
	@Qualifier("wipoCopyXMLJobDetailFactoryBean")
	@Bean
	public JobDetailFactoryBean wipoCopyXMLJobDetailFactoryBean() {
		JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
		jobDetailFactoryBean.setJobClass(WipoMadridCopyXMLJob.class);
		jobDetailFactoryBean.setDescription("Invoke " + WipoMadridCopyXMLJob.class.getSimpleName());
		jobDetailFactoryBean.setDurability(true);
		return jobDetailFactoryBean;
	}

	@Qualifier("wipoCopyXMLCronTriggerFactoryBean")
	@Bean
	public CronTriggerFactoryBean wipoCopyXMLCronTriggerFactoryBean() {
		CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
		cronTriggerFactoryBean.setJobDetail(wipoCopyXMLJobDetailFactoryBean().getObject());
		cronTriggerFactoryBean.setCronExpression(environment.getProperty("scheduler.cron.expression.wipo.madrid.copy.xml.job"));
		cronTriggerFactoryBean.setDescription("WIPO Copy XML Job Trigger");
		return cronTriggerFactoryBean;
	}

	// WIPO COPY PDF SCHEDULER BEAN CONFIGS
	@Qualifier("wipoCopyPDFJobDetailFactoryBean")
	@Bean
	public JobDetailFactoryBean wipoCopyPDFJobDetailFactoryBean() {
		JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
		jobDetailFactoryBean.setJobClass(WipoMadridCopyPDFJob.class);
		jobDetailFactoryBean.setDescription("Invoke " + WipoMadridCopyPDFJob.class.getSimpleName());
		jobDetailFactoryBean.setDurability(true);
		return jobDetailFactoryBean;
	}

	@Qualifier("wipoCopyPDFCronTriggerFactoryBean")
	@Bean
	public CronTriggerFactoryBean wipoCopyPDFCronTriggerFactoryBean() {
		CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
		cronTriggerFactoryBean.setJobDetail(wipoCopyPDFJobDetailFactoryBean().getObject());
		cronTriggerFactoryBean.setCronExpression(environment.getProperty("scheduler.cron.expression.wipo.madrid.copy.pdf.job"));
		cronTriggerFactoryBean.setDescription("WIPO Copy PDF Job Trigger");
		return cronTriggerFactoryBean;
	}
	
		// UPDATE STATUS LICENSE SCHEDULER BEAN CONFIGS
		@Qualifier("updateStatusLicenseFactoryBean")
		@Bean
		public JobDetailFactoryBean updateStatusLicenseFactoryBean() {
			JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
			jobDetailFactoryBean.setJobClass(LicenseUpdateStatusJob.class);
			jobDetailFactoryBean.setDescription("Invoke " + LicenseUpdateStatusJob.class.getSimpleName());
			jobDetailFactoryBean.setDurability(true);
			return jobDetailFactoryBean;
		}

		@Qualifier("updateStatusLicenseCronTriggerFactoryBean")
		@Bean
		public CronTriggerFactoryBean updateStatusLicenseCronTriggerFactoryBean() {
			CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
			cronTriggerFactoryBean.setJobDetail(updateStatusLicenseFactoryBean().getObject());
			cronTriggerFactoryBean.setCronExpression(environment.getProperty("scheduler.cron.expression.license.update.status.job"));
			cronTriggerFactoryBean.setDescription("Update Status License");
			return cronTriggerFactoryBean;
		}

	// MONITORING SCHEDULER BEAN CONFIGS
	@Qualifier("monitoringTasksFactoryBean")
	@Bean
	public JobDetailFactoryBean monitoringTasksFactoryBean() {
		JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
		jobDetailFactoryBean.setJobClass(MonitoringTasks.class);
		jobDetailFactoryBean.setDescription("Invoke " + MonitoringTasks.class.getSimpleName());
		jobDetailFactoryBean.setDurability(true);
		return jobDetailFactoryBean;
	}

	@Qualifier("monitoringTasksCronTriggerFactoryBean")
	@Bean
	public CronTriggerFactoryBean monitoringTasksCronTriggerFactoryBean() {
		CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
		cronTriggerFactoryBean.setJobDetail(monitoringTasksFactoryBean().getObject());
		cronTriggerFactoryBean.setCronExpression(environment.getProperty("scheduler.cron.expression.monitoring.job"));
		cronTriggerFactoryBean.setDescription("Monitoring Tasks");
		return cronTriggerFactoryBean;
	}

	// MADRID CLASS TRANSLATION SCHEDULER BEAN CONFIGS
	@Qualifier("madridClassTranslationJobDetailFactoryBean")
	@Bean
	public JobDetailFactoryBean madridClassTranslationJobDetailFactoryBean() {
		JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
		jobDetailFactoryBean.setJobClass(madridClassTranslationJob.class);
		jobDetailFactoryBean.setDescription("Invoke " + madridClassTranslationJob.class.getSimpleName());
		jobDetailFactoryBean.setDurability(true);
		return jobDetailFactoryBean;
	}

	@Qualifier("madridClassTranslationCronTriggerFactoryBean")
	@Bean
	public CronTriggerFactoryBean madridClassTranslationCronTriggerFactoryBean() {
		CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
		cronTriggerFactoryBean.setJobDetail(madridClassTranslationJobDetailFactoryBean().getObject());
		cronTriggerFactoryBean.setCronExpression(environment.getProperty("scheduler.cron.expression.madrid.translation.job"));
		cronTriggerFactoryBean.setDescription("Madrid Class Translation Job Trigger");
		return cronTriggerFactoryBean;
	}

	@Bean
	public SchedulerFactoryBean schedulerFactoryBean() {
		SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
		schedulerFactory.setConfigLocation(new ClassPathResource("quartz.properties"));

		AutowiringSpringBeanJobFactory autowiringSpringBeanJobFactory = new AutowiringSpringBeanJobFactory();
		autowiringSpringBeanJobFactory.setApplicationContext(applicationContext);

		schedulerFactory.setJobFactory(autowiringSpringBeanJobFactory);
		schedulerFactory.setJobDetails(
				//regOnlineJobDetailFactoryBean().getObject()
				//certificateExpireJobDetailFactoryBean().getObject()
				//, importClassJobDetailFactoryBean().getObject() //sementara diremarks, sambil cun migrasi class detail dan txtmclas. -> 05-04-2019
				wipoFileListerJobDetailFactoryBean().getObject()
				, wipoFileDownloadJobDetailFactoryBean().getObject()
				, wipoCopyXMLJobDetailFactoryBean().getObject()
				, wipoCopyIMageJobDetailFactoryBean().getObject()
				, updateStatusLicenseFactoryBean().getObject()
      			, monitoringTasksFactoryBean().getObject()
				, madridClassTranslationJobDetailFactoryBean().getObject()
				, wipoCopyPDFJobDetailFactoryBean().getObject()
		);
		
		schedulerFactory.setTriggers(
				//regOnlineCronTriggerFactoryBean().getObject()
				//certificateExpireCronTriggerFactoryBean().getObject()
				//,importClassCronTriggerFactoryBean().getObject() //sementara diremarks, sambil cun migrasi class detail dan txtmclas. -> 05-04-2019
				wipoFileListerCronTriggerFactoryBean().getObject()
				,wipoFileDownloadCronTriggerFactoryBean().getObject()
				, wipoCopyImageCronTriggerFactoryBean().getObject()
				, wipoCopyXMLCronTriggerFactoryBean().getObject()
				, updateStatusLicenseCronTriggerFactoryBean().getObject()
				, monitoringTasksCronTriggerFactoryBean().getObject()
				, madridClassTranslationCronTriggerFactoryBean().getObject()
				, wipoCopyPDFCronTriggerFactoryBean().getObject()
		);
		return schedulerFactory;
	}
}