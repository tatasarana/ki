package com.docotel.ki.scheduler.job;

import com.docotel.ki.model.master.MClassDetail;
import com.docotel.ki.repository.custom.master.MClassDetailCustomRepository;
import com.docotel.ki.service.BingTranslationService;
import com.docotel.ki.service.GoogleTranslationService;
import com.docotel.ki.service.transaction.MadridService;
import org.apache.commons.lang.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.List;

@Component
@Transactional
public class madridClassTranslationJob implements Job {
	private static boolean jobOnProgress = false;

	private static Logger logger = LoggerFactory.getLogger(madridClassTranslationJob.class);

	@Autowired
	private MClassDetailCustomRepository mClassDetailCustomRepository;

	@Autowired
	private MadridService service;

	@Autowired
	private GoogleTranslationService googleTranslationService;

	@Autowired
	private BingTranslationService bingTranslationService;

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		if (jobOnProgress) {
			logger.info("- PREVIOUS " + this.getClass().getSimpleName() + " PROCESS STILL ON GOING NOT EXECUTING JOB PROCESS -");
		} else {
			jobOnProgress = true;
			logger.info("- " + this.getClass().getSimpleName() + " JOB PROCESS STARTING -");
			try {
				List<MClassDetail> mClassDetails = mClassDetailCustomRepository.selectAll("translationFlag", false, true, null, null, null, 1000);
				logger.info("Total data: " + mClassDetails.size());
				for (MClassDetail mClassDetail: mClassDetails) {
					String translator = null;
					String translation = null;
					String temp_translation = null;
					if(!mClassDetail.getDescEn().equals("-")){
						temp_translation = googleTranslationService.getTranslation(mClassDetail.getDescEn(), true);
						translation = temp_translation;
						translator = "Google";
						mClassDetail.setDesc(translation);
					} else if (!mClassDetail.getDesc().equals("-")) {
						temp_translation = googleTranslationService.getTranslation(mClassDetail.getDesc(), false);
						translation = temp_translation;
						translator = "Google";
						mClassDetail.setDescEn(translation);
					}

					if (StringUtils.isNotBlank(translation) && translation.trim() != "") {
						//System.out.println("translate " + translation);
						mClassDetail.setTranslationFlag(true);						
						mClassDetail.setNotes("Diterjemahkan oleh " + translator);
						mClassDetail.setUpdatedDate(new Timestamp(System.currentTimeMillis()));
						service.saveTranslatedClassDesc(mClassDetail);
					}
				}

				logger.info(this.getClass().getSimpleName() + " JOB PROCESS DONE");
				jobOnProgress = false;
			} catch (Exception e) {
				logger.error(this.getClass().getSimpleName() + " JOB PROCESS ERROR - ", e);
				jobOnProgress = false;
			}
		}
	}
}
