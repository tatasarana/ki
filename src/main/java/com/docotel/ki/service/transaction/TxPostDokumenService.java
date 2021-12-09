package com.docotel.ki.service.transaction;

import com.docotel.ki.model.transaction.TxPostDoc;
import com.docotel.ki.repository.custom.transaction.TxPostDocCustomRepository;
import com.docotel.ki.repository.transaction.TxPostDocRepository;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.repository.custom.transaction.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class TxPostDokumenService {
	
    @Autowired
    private TxPostDocCustomRepository txPostDocCustomRepository;

    @Autowired
    private TxPostDocRepository txPostDocRepository;

    @Value("${upload.file.doc.pasca.path:}")
    private String uploadFileDocPascaPath;

    public TxPostDoc selectOnePostDocById(String by, String value) {
        return txPostDocCustomRepository.selectOne(by, value, true);
    }
    
    @Transactional
    public void saveOrUpdateDokumen(TxPostDoc txPostDoc) throws IOException {

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(txPostDoc.getFileDoc());
            String fileName = txPostDoc.getFileName();
            String formatFile = fileName.substring(fileName.lastIndexOf(".") + 1);
            String fileNameTemp = txPostDoc.getTxPostReception().geteFilingNo() + "-" + txPostDoc.getmDocType().getCode() + "." + formatFile;
            String pathFolder = DateUtil.formatDate(txPostDoc.getTxPostReception().getCreatedDate(), "yyyy/MM/dd/");
            String uploadFile = uploadFileDocPascaPath + pathFolder + fileNameTemp;

            if (Files.notExists(Paths.get(uploadFileDocPascaPath + pathFolder))) {
                Files.createDirectories(Paths.get(uploadFileDocPascaPath + pathFolder));
            }

            Files.write(Paths.get(uploadFile), decodedBytes);

            txPostDoc.setFileNameTemp(fileNameTemp);
            txPostDocCustomRepository.saveOrUpdate(txPostDoc);
                
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Transactional
    public void deleteDokumen(String id) throws IOException{

        TxPostDoc txPostDoc = txPostDocRepository.getOne(id);
        String pathFolder = DateUtil.formatDate(txPostDoc.getTxPostReception().getCreatedDate(), "yyyy/MM/dd/");
        String fileNameTemp = txPostDoc.getFileNameTemp();
        String uploadFile = uploadFileDocPascaPath + pathFolder + fileNameTemp;

        try {
            Path pathFile = Paths.get(uploadFile);
            if (Files.exists(pathFile)) {
                Files.delete(pathFile);
            }
        } catch (FileSystemException e) {
        }

        txPostDocCustomRepository.deleteTxPostDoc(id);
    }
}