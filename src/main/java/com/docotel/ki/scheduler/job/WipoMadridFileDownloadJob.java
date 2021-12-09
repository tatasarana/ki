package com.docotel.ki.scheduler.job;

import com.docotel.ki.model.wipo.ImageFile;
import com.docotel.ki.model.wipo.PDFFile;
import com.docotel.ki.model.wipo.XMLFile;
import com.docotel.ki.service.WipoService;
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
public class WipoMadridFileDownloadJob implements Job {
    private static boolean synchImageOnProgress = false;
    private static boolean synchPdfOnProgress = false;
    private static boolean synchXmlOnProgress = false;

    private static Logger logger = LoggerFactory.getLogger(WipoMadridFileDownloadJob.class);

    @Autowired
    private WipoService wipoService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if (synchImageOnProgress || synchPdfOnProgress || synchXmlOnProgress) {
            logger.info("- PREVIOUS " + this.getClass().getSimpleName() + " PROCESS STILL ON GOING NOT EXECUTING JOB PROCESS -");
        } else {
            synchImageOnProgress = synchPdfOnProgress = synchXmlOnProgress = true;
            logger.info("- " + this.getClass().getSimpleName() + " JOB PROCESS STARTING -");

            List<XMLFile> xmlList = wipoService.selectNotDownloadedXmlFile();
            if (xmlList != null) {
                for (XMLFile file : xmlList) {
                    try {
                        wipoService.downloadXmlFile(file);
                    } catch (IOException e) {
                        logger.info("- DOWNLOAD XML FILE " + file.getFileName() + " FAIL -", e);
                    }
                }
            }
            synchXmlOnProgress = false;

            List<PDFFile> pdfList = wipoService.selectNotDownloadedPdfFile();
            if (pdfList != null) {
                for (PDFFile file : pdfList) {
                    try {
                        wipoService.downloadPdfFile(file);
                    } catch (IOException e) {
                        logger.info("- DOWNLOAD PDF FILE " + file.getFileName() + " FAIL -", e);
                    }
                }
            }
            synchPdfOnProgress = false;

            List<ImageFile> imageList = wipoService.selectNotDownloadedImageFile();
            if (imageList != null) {
                for (ImageFile file : imageList) {
                    try {
                        wipoService.downloadImageFile(file);
                    } catch (IOException e) {
                        logger.info("- DOWNLOAD IMAGE FILE " + file.getFileName() + " FAIL -", e);
                    }
                }
            }
            synchImageOnProgress = false;

            logger.info("- " + this.getClass().getSimpleName() + " JOB PROCESS DONE -");
        }
    }
}
