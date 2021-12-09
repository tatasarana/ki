package com.docotel.ki.service.transaction;

import com.docotel.ki.enumeration.DocTypeEnumId;
import com.docotel.ki.model.master.MDocType;
import com.docotel.ki.model.master.MDocument;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.transaction.TxPostDoc;
import com.docotel.ki.model.transaction.TxTmDoc;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.transaction.TxPostDocCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmDocCustomRepository;
import com.docotel.ki.repository.master.MDocTypeRepository;
import com.docotel.ki.repository.master.MDocumentRepository;
import com.docotel.ki.repository.transaction.TxTmDocRepository;
import com.docotel.ki.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Service
public class DokumenLampiranService {   
    /***************************** - AUTO INJECT SECTION - ****************************/
    @Autowired
    private TxTmDocRepository txtmdocRepository;
    @Autowired
    private TxTmDocCustomRepository txtmdocCustomRepository;
    @Autowired
    private TxPostDocCustomRepository txPostDocCustomRepository;
    @Autowired
    private MDocumentRepository mDocumentRepository;
    @Autowired
    private MDocTypeRepository mdoctypeRepository;
    @Value("${upload.file.doc.application.path:}")
    private String uploadFileDocApplicationPath;
    @Value("${upload.file.doc.multiupload.path:}")
    private String uploadFileDocMultiuploadPath;
    @Value("${upload.file.image.tandatangan.path}")
    private String uploadFileImageTandaTangan;
    
    @Value("${upload.file.home.image:}")
    private String uploadHomeImagePath;
    @Value("${upload.file.home.video:}")
    private String uploadHomeVideoPath;
    

    /***************************** - METHOD SECTION - ****************************/
    public List<TxTmDoc> getAllDoc() {
        return txtmdocRepository.findAll();
    }

    public TxTmDoc getReceptionById(String id) {
        return txtmdocRepository.findOne(id);
    }

    @Transactional
    public void saveDokumenLampiran(TxTmDoc txtmDoc) {
        txtmdocRepository.save(txtmDoc);
    }

    // Remark untuk perubahan di hal edit permohonan offline dokumen
    @Transactional
    public void saveDokumenLampiranPermohonan(List<TxTmDoc> txtmDocList, String application_id) {
        //todo delete semua berdasar app id
        txtmdocCustomRepository.delete("txTmGeneral.id", application_id);
        for (TxTmDoc txtmDoc : txtmDocList) {
            txtmdocRepository.save(txtmDoc);

            StringBuilder sb = new StringBuilder();
            sb.append("UUID: " + txtmDoc.getCurrentId());
            sb.append(", Application No: " + txtmDoc.getTxTmGeneral().getApplicationNo());
            sb.append(", Tipe: " + txtmDoc.getmDocType().getName());
            sb.append(", Penjelasan: " + txtmDoc.getDescription());

            sb.append(", Status: " + txtmDoc.isStatus());
            sb.append(", Penjelasan: " + txtmDoc.getCreatedDate());
            
        }
    }
    
    public GenericSearchWrapper<TxTmDoc> searchNewDokumen(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        GenericSearchWrapper<TxTmDoc> searchResult = new GenericSearchWrapper<TxTmDoc>();
        searchResult.setCount(txtmdocCustomRepository.countNewDokumen(searchCriteria));
        if (searchResult.getCount() > 0) {
            searchResult.setList(txtmdocCustomRepository.selectAllNewDoc(searchCriteria, orderBy, orderType, offset, limit));
        }
        return searchResult;
    }

    public GenericSearchWrapper<TxPostDoc> searchNewDokumenPasca(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        GenericSearchWrapper<TxPostDoc> searchResult = new GenericSearchWrapper<TxPostDoc>();
        searchResult.setCount(txPostDocCustomRepository.count(searchCriteria));
        if (searchResult.getCount() > 0) {
            searchResult.setList(txPostDocCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
        }
        return searchResult;
    }

    @Transactional
    public void saveDokumenLampiran(TxTmDoc txTmDoc, String sourceFile) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(sourceFile);
            String fileName = txTmDoc.getFileName();
            String formatFile = fileName.substring(fileName.lastIndexOf(".") + 1);
            String fileNameTemp = txTmDoc.getTxTmGeneral().getId() + "-" + txTmDoc.getmDocType().getCode() + "." + formatFile;
            String pathFolder = DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
            String uploadFile = uploadFileDocApplicationPath + pathFolder + fileNameTemp;

            if (Files.notExists(Paths.get(uploadFileDocApplicationPath + pathFolder))) {
                Files.createDirectories(Paths.get(uploadFileDocApplicationPath + pathFolder));
            }

            Files.write(Paths.get(uploadFile), decodedBytes);

            txTmDoc.setFileNameTemp(fileNameTemp);
            txtmdocRepository.save(txTmDoc);

            StringBuilder sb = new StringBuilder();
            sb.append("UUID: " + txTmDoc.getCurrentId());
            sb.append(", Application No: " + txTmDoc.getTxTmGeneral().getApplicationNo());
            sb.append(", Tipe: " + txTmDoc.getmDocType().getName());
            sb.append(", Penjelasan: " + txTmDoc.getDescription());

            sb.append(", Status: " + txTmDoc.isStatus());
            sb.append(", Penjelasan: " + txTmDoc.getCreatedDate());
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Transactional
    public void deleteDokumen(String id) throws IOException{
    	
    	TxTmDoc txTmDoc = txtmdocRepository.getOne(id);
    	String folderDoc = DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
        String uploadFile = "";
        String fileNameDoc = "";
        
        if(txTmDoc.getmDocType().getId().equalsIgnoreCase("TTDP") || txTmDoc.getmDocType().getId().equalsIgnoreCase("TTDK") || txTmDoc.getmDocType().getId().equalsIgnoreCase("TTD")) {
        	fileNameDoc = txTmDoc.getFileNameTemp();
        	uploadFile = uploadFileImageTandaTangan + folderDoc + fileNameDoc;
        	try {
        		Path pathFile = Paths.get(uploadFile);
                if (Files.exists(pathFile)) {
                    Files.delete(pathFile);
                }
        	} catch (FileSystemException e) {
        	}
        } else {
        	fileNameDoc = txTmDoc.getFileNameTemp();
        	uploadFile = uploadFileDocApplicationPath + folderDoc + fileNameDoc;

        	try {
        		Path pathFile = Paths.get(uploadFile);
                if (Files.exists(pathFile)) {
                    Files.delete(pathFile);
                }
        	} catch (FileSystemException e) {
        	}
        }
        txtmdocCustomRepository.deleteTxTmDoc(id);
    }
    
    private static BufferedImage resize(BufferedImage img, int height, int width) {
        Image tmp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }
    
    @Transactional
    public boolean saveDocPermohonanOnline(TxTmDoc txTmDoc, String sourceFile, String oldFileName) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(sourceFile);
            String pathFolder = DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
            String uploadFile = "";
            String fileNameTemp = "";
            
            if(txTmDoc.getmDocType().getId().equalsIgnoreCase("TTDP") || txTmDoc.getmDocType().getId().equalsIgnoreCase("TTDK") || txTmDoc.getmDocType().getId().equalsIgnoreCase("TTD")) {
            	fileNameTemp = txTmDoc.getTxTmGeneral().getApplicationNo() + "-" + txTmDoc.getmDocType().getCode() + ".jpg";
            	uploadFile = uploadFileImageTandaTangan + pathFolder + fileNameTemp;
            	
            	if(oldFileName != null) {
            		File f = new File(uploadFile);
                    if (f.exists() && !f.isDirectory()) {
                        f.delete();
                    }
            	}
            	
            	if (txTmDoc.getFileImageTtd() != null) {
                    File file = new File(uploadFile);
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    BufferedInputStream bis = new BufferedInputStream(txTmDoc.getFileImageTtd().getInputStream());
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                    
                    while (bis.available() > 0) {
                        BufferedImage image = ImageIO.read(bis);
                        if(image == null){
                            return false;
                        }
                        BufferedImage resized = null;
                        try{
                            resized = resize(image, 115, 115);
                        }catch (Exception e){
                            resized = image;
                        }
                        ImageIO.write(resized, "jpg", bos);
                    }
                    bos.close();
                    bis.close();
                }
            } else {
            	fileNameTemp = txTmDoc.getTxTmGeneral().getApplicationNo() + "-" + txTmDoc.getmDocType().getCode() + "." + "pdf";
            	uploadFile = uploadFileDocApplicationPath + pathFolder + fileNameTemp;

                if (Files.notExists(Paths.get(uploadFileDocApplicationPath + pathFolder))) {
                    Files.createDirectories(Paths.get(uploadFileDocApplicationPath + pathFolder));
                }
                
                Files.write(Paths.get(uploadFile), decodedBytes);
            }

            txTmDoc.setStatus(true);
            txTmDoc.setFileNameTemp(fileNameTemp);
            txtmdocRepository.save(txTmDoc);

            StringBuilder sb = new StringBuilder();
            sb.append("UUID: " + txTmDoc.getCurrentId());
            sb.append(", Application No: " + txTmDoc.getTxTmGeneral().getApplicationNo());
            sb.append(", Tipe: " + txTmDoc.getmDocType().getName());
            sb.append(", Penjelasan: " + txTmDoc.getDescription());
            sb.append(", Status: " + txTmDoc.isStatus());
            sb.append(", Penjelasan: " + txTmDoc.getCreatedDate());

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoClassDefFoundError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }
    
    @Transactional
    public void saveDocEditPratinjauPermohonan(TxTmDoc txTmDoc, String sourceFile, String oldFileName) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(sourceFile);
            String pathFolder = DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
            String uploadFile = "";
            String fileNameTemp = "";
            
            if(txTmDoc.getmDocType().getId().equalsIgnoreCase("TTDP") || txTmDoc.getmDocType().getId().equalsIgnoreCase("TTDK") || txTmDoc.getmDocType().getId().equalsIgnoreCase("TTD")) {
            	fileNameTemp = txTmDoc.getTxTmGeneral().getId() + "-" + txTmDoc.getmDocType().getCode() + ".jpg";
            	uploadFile = uploadFileImageTandaTangan + pathFolder + fileNameTemp;
            	
            	if(oldFileName != null) {
            		File f = new File(uploadFile);
                    if (f.exists() && !f.isDirectory()) {
                        f.delete();
                    }
            	}
            	
            	if (txTmDoc.getFileImageTtd() != null) {
                    File file = new File(uploadFile);
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    BufferedInputStream bis = new BufferedInputStream(txTmDoc.getFileImageTtd().getInputStream());
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                    
                    while (bis.available() > 0) {
                        BufferedImage image = ImageIO.read(bis);
                        BufferedImage resized = resize(image, 115, 115);
                        ImageIO.write(resized, "jpg", bos);
                    }
                    bos.close();
                    bis.close();
                }
            } else {
            	fileNameTemp = txTmDoc.getTxTmGeneral().getId() + "-" + txTmDoc.getmDocType().getCode() + "." + "pdf";
            	uploadFile = uploadFileDocApplicationPath + pathFolder + fileNameTemp;

                if (Files.notExists(Paths.get(uploadFileDocApplicationPath + pathFolder))) {
                    Files.createDirectories(Paths.get(uploadFileDocApplicationPath + pathFolder));
                }
                
                Files.write(Paths.get(uploadFile), decodedBytes);
            }

            txTmDoc.setStatus(true);
            txTmDoc.setFileNameTemp(fileNameTemp);
            txtmdocRepository.save(txTmDoc);

            StringBuilder sb = new StringBuilder();
            sb.append("UUID: " + txTmDoc.getCurrentId());
            sb.append(", Application No: " + txTmDoc.getTxTmGeneral().getApplicationNo());
            sb.append(", Tipe: " + txTmDoc.getmDocType().getName());
            sb.append(", Penjelasan: " + txTmDoc.getDescription());
            sb.append(", Status: " + txTmDoc.isStatus());
            sb.append(", Penjelasan: " + txTmDoc.getCreatedDate());

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoClassDefFoundError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Transactional
    public void saveDocUpdatePermohonan(TxTmDoc txTmDoc) {
    	try {
            txtmdocCustomRepository.saveOrUpdate(txTmDoc);
        } catch (NullPointerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Transactional
    public void saveDocUpdatePermohonan(TxTmDoc txTmDoc, String sourceFile) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(sourceFile);
            String fileName = txTmDoc.getFileName();
            String formatFile = fileName.substring(fileName.lastIndexOf(".") + 1);
            String fileNameTemp = txTmDoc.getTxTmGeneral().getApplicationNo() + "-" + txTmDoc.getmDocType().getCode() + "." + formatFile;
            String pathFolder = DateUtil.formatDate(txTmDoc.getCreatedDate(), "yyyy/MM/dd/");
            String uploadFile = uploadFileDocApplicationPath + pathFolder + fileNameTemp;

            if (Files.notExists(Paths.get(uploadFileDocApplicationPath + pathFolder))) {
                Files.createDirectories(Paths.get(uploadFileDocApplicationPath + pathFolder));
            }

            Files.write(Paths.get(uploadFile), decodedBytes);

            txTmDoc.setFileNameTemp(fileNameTemp);
            txtmdocCustomRepository.saveOrUpdate(txTmDoc);
        } catch (NullPointerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Transactional
    public void tambahMulti(MultipartFile uploadFile, TxTmGeneral txTmGeneral, String applicationNo, String mimeType, String createdDate) throws IOException, ParseException {
        String createdDateTemp = DateUtil.formatDate(DateUtil.toDate("dd/MM/yyyy", createdDate), "dd.MM.yyyy");
        String fileNameTemp = applicationNo + "-" + DocTypeEnumId.EDMS.value().getId() + mimeType;
        long fileSize = uploadFile.getSize();
        Date dateDoc = DateUtil.toDate("dd/MM/yyyy", createdDate);
        String pathFolder = DateUtil.formatDate(dateDoc, "yyyy/MM/dd/");
        File file = new File(uploadFileDocMultiuploadPath + pathFolder + fileNameTemp);

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        byte[] buff = new byte[5120];
        BufferedInputStream bis = new BufferedInputStream(uploadFile.getInputStream());
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        while (bis.available() > 0) {
            bis.read(buff);
            bos.write(buff);
        }
        bos.close();
        bis.close();

        // set default
        TxTmDoc txTmDoc = new TxTmDoc();
        txTmDoc.setTxTmGeneral(txTmGeneral);
        txTmDoc.setFileName(uploadFile.getOriginalFilename());
        txTmDoc.setmDocType(DocTypeEnumId.EDMS.value());
        txTmDoc.setStatus(true);
        txTmDoc.setFileNameTemp(fileNameTemp);
        txTmDoc.setFileSize(String.valueOf(fileSize));
//        txTmDoc.setUploadDate(DateUtil.toDate("dd/MM/yyyy", createdDate));
        txTmDoc.setCreatedDate(DateUtil.toDate("dd/MM/yyyy", createdDate));
        txTmDoc.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        txtmdocRepository.save(txTmDoc);
    }
    
    @Transactional
    public void uploadHomeImage(MultipartFile uploadFile) throws IOException, ParseException {
        
    	File file = new File(uploadHomeImagePath + uploadFile.getOriginalFilename());

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        byte[] buff = new byte[5120];
        BufferedInputStream bis = new BufferedInputStream(uploadFile.getInputStream());
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        while (bis.available() > 0) {
            bis.read(buff);
            bos.write(buff);
        }
        bos.close();
        bis.close();
        
        String createdDate = DateUtil.formatDateTime(new Timestamp(System.currentTimeMillis()));

        MDocument doc = new MDocument();
		doc.setCreatedDate(DateUtil.toDateTime(createdDate));
		doc.setName(uploadFile.getOriginalFilename());
		doc.setFilePath(uploadHomeImagePath + uploadFile.getOriginalFilename());
		doc.setStatusFlag(true); doc.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		 
		mDocumentRepository.save(doc);
		
    }
    
    @Transactional
    public void uploadHomeVideo(MultipartFile uploadFile) throws IOException, ParseException {
        
    	File file = new File(uploadHomeVideoPath + uploadFile.getOriginalFilename());

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        byte[] buff = new byte[5120];
        BufferedInputStream bis = new BufferedInputStream(uploadFile.getInputStream());
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        while (bis.available() > 0) {
            bis.read(buff);
            bos.write(buff);
        }
        bos.close();
        bis.close();
        
        String createdDate = DateUtil.formatDateTime(new Timestamp(System.currentTimeMillis()));

        MDocument doc = new MDocument();
		doc.setCreatedDate(DateUtil.toDateTime(createdDate));
		doc.setName(uploadFile.getOriginalFilename());
		doc.setFilePath(uploadHomeVideoPath + uploadFile.getOriginalFilename());
		doc.setStatusFlag(true); doc.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		 
		mDocumentRepository.save(doc);
		
    }
    
    @Transactional
    public void deleteFile(String id) throws IOException{
    	
    	MDocument doc = mDocumentRepository.getOne(id);
    	
    	try {
    		Path pathFile = Paths.get(doc.getFilePath());
            if (Files.exists(pathFile)) {
                Files.delete(pathFile);
            }
    	} catch (FileSystemException e) {
    		
    	}
        
        mDocumentRepository.delete(doc);
    }

    public TxTmDoc findOne(String s) {
        return txtmdocRepository.findOne(s);
    }

    @Transactional
    public <S extends TxTmDoc> S save(S s) {
        return txtmdocRepository.save(s);
    }

    @Transactional
    public void deleteDokumenLampiran(TxTmDoc txtmDoc) {
        txtmdocRepository.delete(txtmDoc);
    }

    public MDocType getDocTypeById(String id) {
        return mdoctypeRepository.findOne(id);
    }

    @Transactional
    public void deleteDokumenLampiranByApplicationId(String application_id) {
        txtmdocCustomRepository.delete("txTmGeneral.id", application_id);
    }

    @Transactional
    public List<TxTmDoc> selectAllDocByGeneralId(String generalId) {
        return txtmdocCustomRepository.selectAll("INNER JOIN FETCH c.txTmGeneral tg"
                + "LEFT JOIN FETCH c.mDocType mc "
                + " ", "txTmGeneral.id", generalId, false, 0, 1000);
    }

    public TxTmDoc selectOneDocByApplicationNo(String application_id) {
        return txtmdocCustomRepository.selectOne("txTmGeneral.id", application_id);
    }
    
    public TxTmDoc selectOneDocAppId(String by, String value) {
        return txtmdocCustomRepository.selectOne(by, value, true);
    }

    public List<TxTmDoc> getAllDocByApplicationId(String by, String value) {
        return txtmdocCustomRepository.selectAll("LEFT JOIN FETCH c.mDocType mdt ", by, value, true, 0, 100);
    }
    
    public List<TxTmDoc> selectAllTxTmDoc(String by, String value) {
        return txtmdocCustomRepository.selectAllDoc("LEFT JOIN FETCH c.mDocType mdt ", by, value, true, 0, 100);
    }

    public List<TxTmDoc> getDokumenByMaster(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        return txtmdocCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit);
    }


    public List<TxTmDoc> getAllDokumenLampiran(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        return txtmdocCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit);
    }

    public GenericSearchWrapper<TxTmDoc> searchDokumen(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
        GenericSearchWrapper<TxTmDoc> searchResult = new GenericSearchWrapper<TxTmDoc>();
        searchResult.setCount(txtmdocCustomRepository.count(searchCriteria));
        if (searchResult.getCount() > 0) {
            searchResult.setList(txtmdocCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
        }
        return searchResult;
    }

    public TxTmDoc selectOne(List<KeyValue> searchCriteria, String orderBy, String orderType) {
        return txtmdocCustomRepository.selectOne(searchCriteria, orderBy, orderType);
    }

}
