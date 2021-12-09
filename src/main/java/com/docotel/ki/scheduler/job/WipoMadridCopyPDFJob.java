package com.docotel.ki.scheduler.job;

import com.docotel.ki.model.wipo.PDFFile;
import com.docotel.ki.model.wipo.XMLFile;
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
public class WipoMadridCopyPDFJob implements Job {
    private static boolean synchPdfOnProgress = false;

    private static Logger logger = LoggerFactory.getLogger(WipoMadridCopyPDFJob.class);

    @Autowired
    private WipoService wipoService;

    @Autowired
    private MadridService madridService;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if (synchPdfOnProgress) {
            logger.info("- PREVIOUS " + this.getClass().getSimpleName() + " PROCESS STILL ON GOING NOT EXECUTING JOB PROCESS -");
        } else {
            synchPdfOnProgress = true;
            logger.info("- " + this.getClass().getSimpleName() + " JOB PROCESS COPYING STARTING -");

            threadPoolTaskExecutor.execute(() -> {
                List<PDFFile> fileList = wipoService.selectNotProcessedPdfFile();
                if (fileList != null) {
                    List<XMLFile> xmlFiles = wipoService.selectNotProcessedXmlFile();
                    fileList.forEach(file -> {
                        boolean found = xmlFiles.stream().anyMatch(xmlFile -> xmlFile.getWeek() == file.getWeek());
                        if (found) {
                            return;
                        }

                        try {
                            madridService.prosesCopyMadridPDF(file);
                        } catch (IOException e) {
                            logger.info("- PROSES COPY PDF FILE " + file.getFileName() + " FAIL -", e);
                        }
                    });
                }

                synchPdfOnProgress = false;
            });

            logger.info("- " + this.getClass().getSimpleName() + " JOB PROCESS COPYING DONE -");
        }
    }
}
