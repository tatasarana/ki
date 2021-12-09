package com.docotel.ki.service;

import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.wipo.ImageFile;
import com.docotel.ki.model.wipo.ImageFileList;
import com.docotel.ki.model.wipo.PDFFile;
import com.docotel.ki.model.wipo.XMLFile;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.util.WinZipInputStream;
import com.docotel.ki.repository.custom.transaction.ImageFileListCustomRepository;
import com.docotel.ki.repository.wipo.WipoImageFileCustomRepository;
import com.docotel.ki.repository.wipo.WipoPDFFileCustomRepository;
import com.docotel.ki.repository.wipo.WipoXMLFileCustomRepository;
import com.docotel.ki.service.master.UserService;
import com.docotel.ki.service.transaction.ImageFileListService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.net.www.protocol.ftp.FtpURLConnection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class WipoService {
	@Value("${wipo.madrid.extract.location.images}")
	private String wipoMadridExtractLocationImages;
	@Value("${wipo.madrid.extract.location.pdf}")
	private String wipoMadridExtractLocationPdf;
	@Value("${wipo.madrid.extract.location.xml}")
	private String wipoMadridExtractLocationXml;

	@Value("${wipo.madrid.url.images:}")
	private String wipoMadridUrlImages;
	@Value("${wipo.madrid.url.pdf:}")
	private String wipoMadridUrlPdf;
	@Value("${wipo.madrid.url.xml:}")
	private String wipoMadridUrlXml;

	private static final int BUFFER_SIZE = 5120;

	@Autowired
	private WipoImageFileCustomRepository wipoImageFileCustomRepository;
	@Autowired
	private WipoPDFFileCustomRepository wipoPDFFileCustomRepository;
	@Autowired
	private WipoXMLFileCustomRepository wipoXMLFileCustomRepository;
	@Autowired 
	private ImageFileListService imageFileListService;
	@Autowired 
	private UserService userService;
	@Autowired
	private ImageFileListCustomRepository imageFileListCustomRepository;

	@Transactional
	public void downloadImageFile(ImageFile file) throws IOException {
		try {
			URL url = new URL(wipoMadridUrlImages + file.getFileName());

			File destDir = new File(wipoMadridExtractLocationImages + file.getModifiedYear());
			if (!destDir.exists()) {
				destDir.mkdirs();
			}

			saveFile(url, destDir, file);
			file.setDownloaded(true);
			wipoImageFileCustomRepository.saveOrUpdate(file);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Transactional
	public void downloadPdfFile(PDFFile file) throws IOException {
		URL url = new URL(wipoMadridUrlPdf + file.getFileName());

		File destDir = new File(wipoMadridExtractLocationPdf + file.getModifiedYear());
		if (!destDir.exists()) {
			destDir.mkdirs();
		}

		downloadFile(url, destDir);
		file.setDownloaded(true);
		wipoPDFFileCustomRepository.saveOrUpdate(file);
	}

	@Transactional
	public void downloadXmlFile(XMLFile file) throws IOException {
		URL url = new URL(wipoMadridUrlXml + file.getFileName());

		File destDir = new File(wipoMadridExtractLocationXml + file.getModifiedYear());
		if (!destDir.exists()) {
			destDir.mkdirs();
		}

		downloadFile(url, destDir);
		file.setDownloaded(true);
		wipoXMLFileCustomRepository.saveOrUpdate(file);
	}

	@Transactional
	public void saveImageFile(List<ImageFile> fileList) {
		if (fileList != null) {
			for (ImageFile file : fileList) {
				List<KeyValue> criteriaList = new ArrayList<>();
				criteriaList.add(new KeyValue("modifiedYear", file.getModifiedYear(), true));
				criteriaList.add(new KeyValue("week", file.getWeek(), true));
				ImageFile existing = wipoImageFileCustomRepository.selectOne(criteriaList, null, null);
				if (existing == null) {
					wipoImageFileCustomRepository.saveOrUpdate(file);
				}
			}
		}
	}
	@Transactional
	public void savePdfFile(PDFFile file) throws IOException {
		wipoPDFFileCustomRepository.saveOrUpdate(file);
	}

	@Transactional
	public void savePdfFile(List<PDFFile> fileList) {
		if (fileList != null) {
			for (PDFFile file : fileList) {
				List<KeyValue> criteriaList = new ArrayList<>();
				criteriaList.add(new KeyValue("modifiedYear", file.getModifiedYear(), true));
				criteriaList.add(new KeyValue("week", file.getWeek(), true));
				PDFFile existing = wipoPDFFileCustomRepository.selectOne(criteriaList, null, null);
				if (existing == null) {
					wipoPDFFileCustomRepository.saveOrUpdate(file);
				}
			}
		}
	}

	@Transactional
	public void saveXmlFile(List<XMLFile> fileList) {
		if (fileList != null) {
			for (XMLFile file : fileList) {
				List<KeyValue> criteriaList = new ArrayList<>();
				criteriaList.add(new KeyValue("modifiedYear", file.getModifiedYear(), true));
				criteriaList.add(new KeyValue("week", file.getWeek(), true));
				XMLFile existing = wipoXMLFileCustomRepository.selectOne(criteriaList, null, null);
				if (existing == null) {
					wipoXMLFileCustomRepository.saveOrUpdate(file);
				}
			}
		}
	}
	
	@Transactional
	public void saveXmlFile(XMLFile file) {
		wipoXMLFileCustomRepository.saveOrUpdate(file);
	}

	public List<ImageFile> selectNotDownloadedImageFile() {
		return wipoImageFileCustomRepository.selectAll("downloaded", false, true,"fileName","asc", 0, 50);
	}
	
	public List<ImageFile> selectNotProcessedImageFile() {
		List<KeyValue> criteriaList = new ArrayList<>();
		criteriaList.add(new KeyValue("processed", false, true));
		criteriaList.add(new KeyValue("downloaded", true, true));
		return wipoImageFileCustomRepository.selectAll(criteriaList, "fileName", "asc", 0, 50);
	}

	public XMLFile selectXmlByWeek(int week) {
		List<KeyValue> criteriaList = new ArrayList<>();
		criteriaList.add(new KeyValue("processed", true, true));
		criteriaList.add(new KeyValue("week", week, true));
		return wipoXMLFileCustomRepository.selectOne(criteriaList, null, null);
	}

	public PDFFile selectPdfByWeek(int week) {
		List<KeyValue> criteriaList = new ArrayList<>();
		criteriaList.add(new KeyValue("downloaded", true, true));
		criteriaList.add(new KeyValue("week", week, true));
		return wipoPDFFileCustomRepository.selectOne(criteriaList, null, null);
	}

	public List<PDFFile> selectNotDownloadedPdfFile() {
		return wipoPDFFileCustomRepository.selectAll("downloaded", false, true,"fileName","asc", 0, 50);
	}

	public List<PDFFile> selectNotProcessedPdfFile() {
		List<KeyValue> criteriaList = new ArrayList<>();
		criteriaList.add(new KeyValue("processed", false, true));
		criteriaList.add(new KeyValue("downloaded", true, true));
		return wipoPDFFileCustomRepository.selectAll(criteriaList ,"fileName", "asc", 0, 50);
	}

	public List<XMLFile> selectNotDownloadedXmlFile() { 
		return wipoXMLFileCustomRepository.selectAll("downloaded", false, true,"fileName","asc", 0, 50);
	}
	
	public List<XMLFile> selectNotProcessedXmlFile() {
		List<KeyValue> criteriaList = new ArrayList<>();
		criteriaList.add(new KeyValue("processed", false, true));
		criteriaList.add(new KeyValue("downloaded", true, true));
		return wipoXMLFileCustomRepository.selectAll(criteriaList, "fileName", "ASC", 0, 50);
	}
	
	public List<ImageFileList> selectNotProcessedCopyImageFile() {
		return imageFileListCustomRepository.selectAll("processed", false, true, 0, 5000);
	}

	private void downloadFile(URL url, File destDir) throws IOException {
		FtpURLConnection conn = new FtpURLConnection(url);
		conn.setConnectTimeout(60000);
		InputStream inputStream = conn.getInputStream();
		ZipInputStream zipInputStream = new ZipInputStream(inputStream);

		byte[] buffer = new byte[5120];

		ZipEntry zipEntry;
		while ((zipEntry = zipInputStream.getNextEntry()) != null) {
			File newFile = newFile(destDir, zipEntry);
			if (zipEntry.isDirectory()) {
				newFile.mkdirs();
			} else {
				if (!newFile.exists()) {
					newFile.createNewFile();
				}
				FileOutputStream fos = new FileOutputStream(newFile);
				int len;
				while ((len = zipInputStream.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				fos.close();
			}
		}
		zipInputStream.close();
		inputStream.close();
	}


	private void saveFile(URL url, File destDir, ImageFile file) throws IOException {
		byte[] buffer = new byte[BUFFER_SIZE];

		File pathSave = new File(destDir.getPath() + File.separator + file.getWeek());
		if (!pathSave.exists()) {
			pathSave.mkdirs();
		}
		URLConnection conn = url.openConnection();
		InputStream inputStream = conn.getInputStream();

		FileOutputStream outputStream = new FileOutputStream(pathSave.getPath() + File.separator + file.getFileName());

		int bytesRead = -1;
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
		}
		outputStream.close();
		inputStream.close();
		
		File folder = new File(pathSave.getPath() + File.separator + "output");
		if (!folder.exists()) {
			folder.mkdir();
		}

		//get the zip file content
		ZipInputStream zipInputStream = new ZipInputStream(new WinZipInputStream(new FileInputStream(pathSave.getPath() + File.separator + file.getFileName())));
		//get the zipped file list entry
		ZipEntry ze = zipInputStream.getNextEntry();
		MUser mUser = userService.getUserByName("SYSTEM");
		while (ze != null) {

			String fileName = ze.getName();
			File newFile = new File(folder.getPath() + File.separator + fileName);
			//create all non exists folders
			//else you will hit FileNotFoundException for compressed folder
			new File(newFile.getParent()).mkdirs();
			FileOutputStream fos = new FileOutputStream(newFile);
			
//			//save path and name file
			ImageFileList imageFileList = new ImageFileList();
			int index = ze.getName().lastIndexOf("\\") > 0 ? ze.getName().lastIndexOf("\\") : ze.getName().lastIndexOf("/");
			imageFileList.setName(ze.getName().substring(index + 1).substring(0, ze.getName().substring(index + 1).lastIndexOf(".")));
			imageFileList.setFilePath(newFile.toString());
			imageFileList.setProcessed(false);
			Timestamp createdDate = new Timestamp(System.currentTimeMillis());
			imageFileList.setCreatedDate(createdDate);
			imageFileList.setCreatedBy(mUser);
			imageFileList.setImageFile(file);
			imageFileListService.saveImageFileList(imageFileList);
			
			
			int len;
			while ((len = zipInputStream.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}
			fos.close();
			ze = zipInputStream.getNextEntry();

		}

		zipInputStream.close();
	}


	private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
		File destFile = new File(destinationDir, zipEntry.getName());

		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();

		if (destFilePath.startsWith(destDirPath + File.separator)) {
			return destFile;
		}

		return null;
	}
}
