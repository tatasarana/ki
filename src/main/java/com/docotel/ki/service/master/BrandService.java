package com.docotel.ki.service.master;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

import javax.imageio.ImageIO;

import com.docotel.ki.model.master.MBrandType;
import com.docotel.ki.model.transaction.TxTmBrand;
import com.docotel.ki.model.transaction.TxTmBrandDetail;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.util.DateUtil;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.docotel.ki.component.LireIndexing;
//import com.docotel.ki.model.transaction.TxLogMenu;
import com.docotel.ki.repository.custom.master.MBrandTypeCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmBrandCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmBrandDetailCustomRepository;
import com.docotel.ki.repository.master.MBrandTypeRepository;
import com.docotel.ki.repository.transaction.TxTmBrandDetailRepository;
import com.docotel.ki.repository.transaction.TxTmBrandRepository;

@Service
@Transactional
public class BrandService {
    public static final String BRAND_ID_JPG_CONNECTOR = ".jpg";
    public static final String BRAND_ID_FILE_NAME_CONNECTOR = "_";
    @Autowired
    private MBrandTypeRepository mBrandTypeRepository;
    @Autowired
    private MBrandTypeCustomRepository mBrandTypeCustomRepository;
    @Autowired
    private TxTmBrandRepository txTmBrandRepository;
    @Autowired
    private TxTmBrandDetailRepository txTmBrandDetailRepository;
    @Autowired
    private TxTmBrandCustomRepository txTmBrandCustomRepository;
    @Autowired
    private TxTmBrandDetailCustomRepository txTmBrandDetailCustomRepository;    
    @Autowired
    private LireIndexing lireIndexing;
    @Value("${upload.file.brand.path:}")
    private String uploadFileBrandPath;
    @Value("${upload.file.branddetail.path:}")
    private String uploadFileBrandDetailPath;

    public List<MBrandType> findAll() {
        return mBrandTypeRepository.findAll();
    }

    public List<MBrandType> findByStatusFlagTrue() {
        return mBrandTypeRepository.findByStatusFlagTrue();
    }

    public List<TxTmBrandDetail> selectTxTmBrandDetailByTxTmBrand(String txTmBrandId) {
        return txTmBrandDetailCustomRepository.selectTxTmBrandDetailByTxTmBrand(txTmBrandId);
    }

    public MBrandType findOneBrandType(String id) {
        return mBrandTypeRepository.findOne(id);
    }

    public GenericSearchWrapper<MBrandType> searchGeneral(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        GenericSearchWrapper<MBrandType> searchResult = new GenericSearchWrapper<MBrandType>();
        searchResult.setCount(mBrandTypeCustomRepository.count(searchCriteria));
        if (searchResult.getCount() > 0) {
            searchResult.setList(mBrandTypeCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
        }
        return searchResult;
    }

    public void insert(MBrandType data) {
        mBrandTypeRepository.save(data);
    }

    public MBrandType findOne(String s) {
        return mBrandTypeRepository.findOne(s);
    }

    @Transactional
    public void saveOrUpdate(MBrandType mBrandType) {
        mBrandTypeCustomRepository.saveOrUpdate(mBrandType);
    }

    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public void insertBrand(TxTmBrand txTmBrand, TxTmBrandDetail txTmBrandDetail) {
        txTmBrandRepository.save(txTmBrand);
        txTmBrandDetailRepository.save(txTmBrandDetail);
    }

    @Transactional
    public void saveOrUpdate(TxTmBrand txTmBrand) {
        txTmBrandCustomRepository.saveOrUpdate(txTmBrand);
    }

    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public void insertHanyaTxTmBrand(TxTmBrand txTmBrand, String oldFileName) throws IOException {
        txTmBrandCustomRepository.saveOrUpdate(txTmBrand);


        String pathFolder = DateUtil.formatDate(txTmBrand.getCreatedDate(), "yyyy/MM/dd/");
        if (oldFileName != null) {
            File f = new File(uploadFileBrandPath + pathFolder + txTmBrand.getId() + ".jpg");
            if (f.exists() && !f.isDirectory()) {
                f.delete();
            }
        }

        if (txTmBrand.getFileMerek() != null) {
            File file = new File(uploadFileBrandPath + pathFolder + txTmBrand.getId() + ".jpg");
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            BufferedInputStream bis = new BufferedInputStream(txTmBrand.getFileMerek().getInputStream());
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            
            BufferedImage image = ImageIO.read(bis);
            ImageIO.write(image, "jpg", bos);
            
            bos.close();
            bis.close();
          //indexing lire
          // lireIndexing.startIndexing(file.getAbsolutePath());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + txTmBrand.getCurrentId());
        sb.append(", Application No: " + txTmBrand.getTxTmGeneral().getApplicationNo());
        sb.append(", Tipe: " + txTmBrand.getmBrandType().getName());
        sb.append(", Nama: " + txTmBrand.getName());

        sb.append(", Nama File: " + txTmBrand.getFileName());
        sb.append(", Penjelasan: " + txTmBrand.getDescription());
        sb.append(", Warna: " + txTmBrand.getColor());

        sb.append(", Disclaimer: " + txTmBrand.getDisclaimer());
        sb.append(", Terjemahan: " + txTmBrand.getTranslation());
        sb.append(", Pengucapan: " + txTmBrand.getDescChar());

       
    }

    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public void insertHanyaTxTmBrandDetail(TxTmBrandDetail txTmBrandDetail, String sourceData) {
        txTmBrandDetailRepository.save(txTmBrandDetail);

        String pathFolder = DateUtil.formatDate(txTmBrandDetail.getUploadDate(), "yyyy/MM/dd/");
        try {
            if (Files.notExists(Paths.get(uploadFileBrandDetailPath + pathFolder))) {
                Files.createDirectories(Paths.get(uploadFileBrandDetailPath + pathFolder));
            }

            byte[] decodedBytes = Base64.getDecoder().decode(sourceData);
            String uploadFile = uploadFileBrandDetailPath + pathFolder + txTmBrandDetail.getId() + "." + FilenameUtils.getExtension(txTmBrandDetail.getFileName());

            Files.write(Paths.get(uploadFile), decodedBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public void deleteTxTmBrandDetail(String id, TxTmBrand txTmBrand) {
        TxTmBrandDetail detailField = txTmBrandDetailCustomRepository.selectOne("id", id);
        TxTmBrandDetail txTmBrandDetail = txTmBrandDetailRepository.getOne(id);

        String pathFolder = DateUtil.formatDate(detailField.getUploadDate(), "yyyy/MM/dd/");
        String uploadFile = uploadFileBrandDetailPath + pathFolder + id + "." + FilenameUtils.getExtension(detailField.getFileName());

        txTmBrandDetailRepository.delete(txTmBrandDetail);

        File f = new File(uploadFile);
        if (f.exists() && !f.isDirectory()) {
            f.delete();
        }
    }

    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public void deleteTxTmBrand(TxTmBrand txTmBrand) {
        txTmBrandRepository.delete(txTmBrand);
    }

    //    add by deni
    public TxTmBrand selectOneByAppId(String appId) {
        return txTmBrandCustomRepository.selectOne("txTmGeneral.id", appId);
    }
//    end

    public MBrandType selectOne(String by, String value) {
        return mBrandTypeCustomRepository.selectOne(by, value);
    }

    public GenericSearchWrapper<TxTmBrandDetail> searchBrandDetail(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        GenericSearchWrapper<TxTmBrandDetail> searchResult = new GenericSearchWrapper<TxTmBrandDetail>();
        searchResult.setCount(txTmBrandDetailCustomRepository.count(searchCriteria));
        if (searchResult.getCount() > 0) {
            searchResult.setList(txTmBrandDetailCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
        }
        return searchResult;
    }

    public List<TxTmBrand> selectAllBrand(List<String> brandIdList) {
        return txTmBrandCustomRepository.selectAll("LEFT JOIN FETCH c.txTmGeneral tg"
                + " LEFT JOIN FETCH c.mBrandType bt "
                + " LEFT JOIN FETCH tg.txTmClassList ttc"
                + " LEFT JOIN FETCH ttc.mClass mc"
                + " LEFT JOIN FETCH tg.txTmOwner tto"
                + " LEFT JOIN FETCH tg.mStatus ms", "id", brandIdList, true, null, null);
    }
    
    public List<Object[]> selectAllBrandList(List<String> brandIdList) {
        return txTmBrandCustomRepository.selectAllTxTmBrandList(brandIdList);
    }
}
