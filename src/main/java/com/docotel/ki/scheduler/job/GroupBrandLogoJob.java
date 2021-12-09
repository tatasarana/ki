package com.docotel.ki.scheduler.job;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.docotel.ki.model.transaction.TxTmBrand;
import com.docotel.ki.util.DateUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.docotel.ki.repository.transaction.TxTmBrandRepository;

@Component
public class GroupBrandLogoJob implements Job{
	private static boolean synchGroupBrandLogoOnProgress = false;

	private static Logger logger = LoggerFactory.getLogger(GroupBrandLogoJob.class);

	@Autowired
    private TxTmBrandRepository txTmBrandRepository;
	
	@Autowired
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;
	
	@Value("${upload.file.ipasimage.path:}")
    private String uploadFileIpasPath;
    
    @Value("${upload.file.brand.path:}")
    private String uploadFileBrandPath;
	
	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		if (synchGroupBrandLogoOnProgress) {
			logger.info("- PREVIOUS " + this.getClass().getSimpleName() + " PROCESS STILL ON GOING NOT EXECUTING JOB PROCESS -");
		} else {
			synchGroupBrandLogoOnProgress = true;
			logger.info("- " + this.getClass().getSimpleName() + " JOB PROCESS COPYING STARTING -");
			
			threadPoolTaskExecutor.execute(() -> {
				List<TxTmBrand> txTmBrandList = txTmBrandRepository.findAll();
				if(txTmBrandList!=null) {
					for (TxTmBrand txTmBrand : txTmBrandList) {
						String brandId = txTmBrand.getId();
						String pathFolder = DateUtil.formatDate(txTmBrand.getCreatedDate(), "yyyy/MM/dd/");
		        		
		        		Path sourceFilePath = Paths.get(uploadFileIpasPath + brandId + ".jpg");
					    Path targetFilePath = Paths.get(uploadFileBrandPath + pathFolder + brandId + ".jpg");
					    
					    if (!Files.exists(targetFilePath)) {
					    	try {
						    	if (Files.exists(sourceFilePath)) {
							    	if (!Files.exists(targetFilePath.getParent())) {
							            Files.createDirectories(targetFilePath.getParent());
							        }
								 
								    Files.move(sourceFilePath, targetFilePath.getParent().resolve(sourceFilePath.getFileName()));
								    logger.info("- PROSES GROUP BRAND LOGO " + brandId + " SUCCESS -");
							    }
					    	} catch (IOException e) {
								logger.info("- PROSES GROUP BRAND LOGO " + brandId + " FAIL -", e);
							}
					    }					    
					}
				}
				
				synchGroupBrandLogoOnProgress = false;
			});

			logger.info("- " + this.getClass().getSimpleName() + " JOB PROCESS COPYING DONE -");
		}
	}
}