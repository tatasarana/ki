package com.docotel.ki.scheduler.job;

import com.docotel.ki.model.wipo.FileSystem;
import com.docotel.ki.model.wipo.ImageFile;
import com.docotel.ki.model.wipo.PDFFile;
import com.docotel.ki.model.wipo.XMLFile;
import com.docotel.ki.service.WipoService;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.enumeration.UserEnum;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sun.net.www.protocol.ftp.FtpURLConnection;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Component
public class WipoMadridFileListerJob implements Job {
	private static boolean jobOnProgress = false;

	private static Logger logger = LoggerFactory.getLogger(WipoMadridFileListerJob.class);

	@Autowired
	private WipoService wipoService;

	@Value("${wipo.madrid.url.images:}")
	private String wipoMadridUrlImages;

	@Value("${wipo.madrid.url.pdf:}")
	private String wipoMadridUrlPdf;

	@Value("${wipo.madrid.url.xml:}")
	private String wipoMadridUrlXml;

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		if (jobOnProgress) {
			logger.info("- PREVIOUS " + this.getClass().getSimpleName() + " PROCESS STILL ON GOING NOT EXECUTING JOB PROCESS -");
		} else {
			jobOnProgress = true;
			logger.info("- " + this.getClass().getSimpleName() + " JOB PROCESS STARTING -");
//			FTPClient ftpClient = new FTPClient();
//			try {
////				ftpClient.connect(wipoMadridUrlImages);
//				//ftp://ftpird.wipo.int/wipo/madrid/notif/id/images/
//				ftpClient.connect("ftpird.wipo.int");
//				ftpClient.login("anonymous", "");
//				int reply = ftpClient.getReplyCode();
//				System.out.println(reply); //Output: 220
//
//				System.out.println("Connected");
//				ftpClient.enterRemotePassiveMode();
//				ftpClient.setListHiddenFiles(false);
//				ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
//				ftpClient.changeWorkingDirectory("wipo/madrid/notif/id/images");
//				System.out.println("Current directory is " + ftpClient.printWorkingDirectory());
//
//				FTPFile[] ftpFiles = ftpClient.listFiles();
//
//				if (ftpFiles != null && ftpFiles.length > 0) {
//					// loop thru files
//					for (FTPFile file : ftpFiles) {
//						try {
//							if (!file.isFile()) {
//								continue;
//							}
//							System.out.println("File is " + file.getName());
//							// get output stream
////							OutputStream output;
////							output = new FileOutputStream(toDirectory + file.getName());
////							// get the file from the remote system
////							ftpClient.retrieveFile(file.getName(), output);
//
//							// close output stream
////							output.close();
//						} catch (Exception e) {
//						}
//					}
//				}
//
//				logger.info("- " + this.getClass().getSimpleName() + " JOB PROCESS DONE -");
//			} catch (IOException e) {
//				logger.error("- " + this.getClass().getSimpleName() + " JOB PROCESS FAIL -", e);
//			} finally {
//				if (ftpClient.isConnected()) {
//					try {
//						ftpClient.logout();
//					} catch (IOException e) {
//					}
//					try {
//						ftpClient.disconnect();
//					} catch (IOException e) {
//					}
//				}
//				jobOnProgress = false;
//			}
			try {
				List<FileSystem> xmlFileSystemList = getFileSystem(wipoMadridUrlXml);
				if (xmlFileSystemList != null && xmlFileSystemList.size() > 0) {
					List<XMLFile> xmlFileList = new ArrayList<>();
					for (FileSystem fileSystem : xmlFileSystemList) {
						XMLFile xmlFile = new XMLFile(fileSystem);
						if (dateConditionMet(xmlFile)) {
							xmlFile.setCreatedBy(UserEnum.SYSTEM.value());
							xmlFileList.add(xmlFile);
						}
					}
					wipoService.saveXmlFile(xmlFileList);
				}
				List<FileSystem> pdfFileSystemList = getFileSystem(wipoMadridUrlPdf);
				if (pdfFileSystemList != null && pdfFileSystemList.size() > 0) {
					List<PDFFile> pdfFileList = new ArrayList<>();
					for (FileSystem fileSystem : pdfFileSystemList) {
						PDFFile pdfFile = new PDFFile(fileSystem);
						if (dateConditionMet(pdfFile)) {
							pdfFile.setCreatedBy(UserEnum.SYSTEM.value());
							pdfFileList.add(pdfFile);
						}
					}
					wipoService.savePdfFile(pdfFileList);
				}
				List<FileSystem> imageFileSystemList = getFileSystem(wipoMadridUrlImages);
				if (imageFileSystemList != null && imageFileSystemList.size() > 0) {
					List<ImageFile> imageFileList = new ArrayList<>();
					for (FileSystem fileSystem : imageFileSystemList) {
						ImageFile imageFile = new ImageFile(fileSystem);
						if (dateConditionMet(imageFile)) {
							imageFile.setCreatedBy(UserEnum.SYSTEM.value());
							imageFileList.add(imageFile);
						}
					} 
					wipoService.saveImageFile(imageFileList);
				}
			} finally {
				logger.info("- " + this.getClass().getSimpleName() + " JOB PROCESS DONE -");
				jobOnProgress = false;
			}
		}
	}

	private List<FileSystem> getFileSystem(String urlLocation) {
		List<FileSystem> fileSystemList = new ArrayList<>();
		InputStream inputStream = null;
		try {
			URL url = new URL(urlLocation);
			FtpURLConnection conn = new FtpURLConnection(url);
			conn.setConnectTimeout(60000);
			conn.setReadTimeout(60000);
			inputStream = conn.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

			String line;
			while ((line = reader.readLine()) != null) {
				FileSystem fileSystem = new FileSystem(line) {
				};
				if (fileSystem != null) {
					if (!fileSystem.isDirectory()) {
						fileSystemList.add(fileSystem);
					}
				}
			}
			logger.info("- LISTING FROM " + urlLocation + " IS DONE -");
		} catch (IOException e) {
			logger.error("- LISTING FROM " + urlLocation + " FAILED: " + e.getMessage() + "-", e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
				}
			}
		}
		return fileSystemList;
	}

	private boolean dateConditionMet(FileSystem fileSystem) {

		return true;


		/*Calendar calendar = Calendar.getInstance(DateUtil.LOCALE_ID);
//		IF THREE MONTHS RULE:
//		int currentTotalMonths = (calendar.get(Calendar.YEAR) * 12) + (calendar.get(Calendar.MONTH) + 1);
//		int fileTotalMonths = (fileSystem.getModifiedYear() * 12) + fileSystem.getModifiedMonth();
//		return (currentTotalMonths - fileTotalMonths < 3);
//		IF TWO MONTHS RULE:
		int currentTotalMonths = (calendar.get(Calendar.YEAR) * 12) + (calendar.get(Calendar.MONTH) + 1);
		int fileTotalMonths = (fileSystem.getModifiedYear() * 12) + fileSystem.getModifiedMonth();
		return (currentTotalMonths - fileTotalMonths < 2);
//		IF A MONTH RULE:
//		return (calendar.get(Calendar.YEAR) + "" + (calendar.get(Calendar.MONTH) + 1)).equalsIgnoreCase(fileSystem.getModifiedYear() + "" + fileSystem.getModifiedMonth());
	*/
	}
}
