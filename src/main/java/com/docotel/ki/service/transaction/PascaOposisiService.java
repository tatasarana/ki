package com.docotel.ki.service.transaction;

import com.docotel.ki.enumeration.StatusEnum;
import com.docotel.ki.model.counter.TcFilingNo;
import com.docotel.ki.model.counter.TcPostReceiptNo;
import com.docotel.ki.model.master.*;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.pojo.DataGeneral;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.counter.TcFilingNoCustomRepository;
import com.docotel.ki.repository.custom.counter.TcPostReceiptNoCustomRepository;
import com.docotel.ki.repository.custom.master.*;
import com.docotel.ki.repository.custom.transaction.*;
import com.docotel.ki.repository.master.MFileSeqRepository;
import com.docotel.ki.repository.master.MFileTypeRepository;
import com.docotel.ki.repository.transaction.*;
import com.docotel.ki.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.*;

@Service
public class PascaOposisiService {
    @Autowired
    private MCityCustomRepository mCityCustomRepository;
    @Autowired
    private MCountryCostumRepository mCountryCostumRepository;
    @Autowired
    private MFileSeqRepository mFileSeqRepository;
    @Autowired
    private MFileTypeRepository mFileTypeRepository;
    @Autowired
    private MProvinceCustomRepository mProvinceCustomRepository;
    @Autowired
    private MrepresentativeCustomRepository mRepresentativeCustomRepository;
    @Autowired
    private TcFilingNoCustomRepository tcFilingNoCustomRepository;
    @Autowired
    private TcPostReceiptNoCustomRepository tcPostReceiptNoCustomRepository;
    @Autowired
    private TxTmClassCustomRepository txTmClassCustomRepository;
    @Autowired
    private TxTmGeneralCustomRepository txTmGeneralCustomRepository;
    @Autowired
    private TxPostReceptionCustomRepository txPostReceptionCustomRepository;
    @Autowired
    private TxPostReceptionDetailCustomRepository txPostReceptionDetailCustomRepository;
    @Autowired
    private TxPostOwnerCustomRepository txPostOwnerCustomRepository;
    @Autowired
    private TxPostRepresentativeCustomRepository txPostRepresentativeCustomRepository;
    @Autowired
    private TxPostDocCustomRepository txPostDocCustomRepository;
    @Autowired
    private TxMonitorRepository txMonitorRepository;
    @Autowired
    private MWorkflowProcessCustomRepository mWorkflowProcessCustomRepository;

    @Autowired
    private MWorkflowProcessActionCustomRepository mWorkflowProcessActionCustomRepository;

    @Autowired
    private TxTmOwnerCustomRepository txTmOwnerCustomRepository;

    @Autowired
    private TxPostReceptionRepository txPostReceptionRepository;
    @Autowired
    private TxPostReceptionDetailRepository txPostReceptionDetailRepository;
    @Autowired
    private TxPostOwnerRepository txPostOwnerRepository;
    @Autowired
    private TxPostDocRepository txPostDocRepository;
    @Autowired
    private TxPostRepresentativeRepository txPostRepresentativeRepository;

    @Value("${upload.file.doc.pasca.path:}")
    private String uploadFileDocPascaPath;

    public GenericSearchWrapper<TxPostReception> searchPostReception(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
        GenericSearchWrapper<TxPostReception> searchResult = new GenericSearchWrapper<TxPostReception>();
        searchResult.setCount(txPostReceptionCustomRepository.countPasca(null, searchCriteria));
        if (searchResult.getCount() > 0) {
            searchResult.setList(txPostReceptionCustomRepository.selectAllPasca(
                    //"LEFT JOIN FETCH c.txTmGeneral ttg " +
                    "LEFT JOIN FETCH c.mFileSequence mfs " +
                            "LEFT JOIN FETCH c.mFileType mft " +
                            "LEFT JOIN FETCH c.mStatus ms " +
                            "LEFT JOIN FETCH c.txPostReceptionDetailList tprd ", searchCriteria, orderBy, orderType, offset, limit));
        }
        return searchResult;
    }

    public GenericSearchWrapper<TxPostReception> searchPostReceptionNote(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
        GenericSearchWrapper<TxPostReception> searchResult = new GenericSearchWrapper<TxPostReception>();
        searchResult.setCount(txPostReceptionCustomRepository.count(searchCriteria));
        if (searchResult.getCount() > 0) {
            searchResult.setList(txPostReceptionCustomRepository.selectPostNote(searchCriteria, orderBy, orderType, offset, limit));
        }
        return searchResult;
    }

    public GenericSearchWrapper<TxTmGeneral> searchRefReception(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
        GenericSearchWrapper<TxTmGeneral> searchResult = new GenericSearchWrapper<TxTmGeneral>();
        searchResult.setCount(txTmGeneralCustomRepository.count(searchCriteria));
        if (searchResult.getCount() > 0) {
            searchResult.setList(txTmGeneralCustomRepository.selectAll(
                    "LEFT JOIN FETCH c.txRegistration tr", searchCriteria, orderBy, orderType, offset, limit));
        }
        return searchResult;
    }

    public GenericSearchWrapper<TxPostReceptionDetail> searchPostReceptionDetail(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
        GenericSearchWrapper<TxPostReceptionDetail> searchResult = new GenericSearchWrapper<TxPostReceptionDetail>();
        searchResult.setCount(txPostReceptionDetailCustomRepository.count(searchCriteria));
        if (searchResult.getCount() > 0) {
            searchResult.setList(txPostReceptionDetailCustomRepository.selectAll(
                    "LEFT JOIN FETCH c.txPostReception tpr" +
                            "LEFT JOIN FETCH c.txTmGeneral tg", searchCriteria, orderBy, orderType, offset, limit));
        }
        return searchResult;
    }

    public GenericSearchWrapper<MRepresentative> searchRepresentative(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
        GenericSearchWrapper<MRepresentative> searchResult = new GenericSearchWrapper<MRepresentative>();
        searchResult.setCount(mRepresentativeCustomRepository.count(searchCriteria));
        if (searchResult.getCount() > 0) {
            searchResult.setList(mRepresentativeCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
        }
        return searchResult;
    }

    public GenericSearchWrapper<TxTmOwner> searchOwner(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
        GenericSearchWrapper<TxTmOwner> searchResult = new GenericSearchWrapper<TxTmOwner>();
        searchResult.setCount(txTmOwnerCustomRepository.count(searchCriteria));
        if (searchResult.getCount() > 0) {
            searchResult.setList(txTmOwnerCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
        }
        return searchResult;
    }

    public TxTmGeneral selectOneDataReferensi(String by, String value) {
        return txTmGeneralCustomRepository.selectOne(
                "LEFT JOIN FETCH c.txReception " +
                        "LEFT JOIN FETCH c.txRegistration " +
                        "LEFT JOIN FETCH c.txTmBrand", by, value, true);
    }

    public TxPostReception selectOneTxPostReceptionById(String postId) {
        return txPostReceptionCustomRepository.selectOne(
                "LEFT JOIN FETCH c.mFileSequence mfs " +
                        "LEFT JOIN FETCH c.mFileType mft " +
                        "LEFT JOIN FETCH c.mStatus ms " +
                        "LEFT JOIN FETCH c.txPostOwner tpo " +
                        "LEFT JOIN FETCH tpo.nationality " +
                        "LEFT JOIN FETCH tpo.mCountry " +
                        "LEFT JOIN FETCH tpo.mProvince " +
                        "LEFT JOIN FETCH tpo.mCity " +
                        "LEFT JOIN FETCH c.txPostRepresentative tpr " +
                        "LEFT JOIN FETCH tpr.mRepresentative mr " +
                        "LEFT JOIN FETCH mr.mCountry " +
                        "LEFT JOIN FETCH mr.mProvince " +
                        "LEFT JOIN FETCH mr.mCity " +
                        "LEFT JOIN c.createdBy cb ", "id", postId, true);
    }

    public TxPostReception selectOneTxPostReceptionById(List<KeyValue> criteriaList) {
        return txPostReceptionCustomRepository.selectOne(
                "LEFT JOIN FETCH c.mFileSequence mfs " +
                        "LEFT JOIN FETCH c.mFileType mft " +
                        "LEFT JOIN FETCH c.mStatus ms " +
                        "LEFT JOIN FETCH c.txPostOwner tpo " +
                        "LEFT JOIN FETCH tpo.nationality " +
                        "LEFT JOIN FETCH tpo.mCountry " +
                        "LEFT JOIN FETCH tpo.mProvince " +
                        "LEFT JOIN FETCH tpo.mCity " +
                        "LEFT JOIN FETCH c.txPostRepresentative tpr " +
                        "LEFT JOIN FETCH tpr.mRepresentative mr " +
                        "LEFT JOIN FETCH mr.mCountry " +
                        "LEFT JOIN FETCH mr.mProvince " +
                        "LEFT JOIN FETCH mr.mCity " +
                        "LEFT JOIN c.createdBy cb ", criteriaList, "id", "ASC");
    }

    public List<TxPostReceptionDetail> selectAllPostDetail(String postReceptionId) {
        return txPostReceptionDetailCustomRepository.selectAll(
                "LEFT JOIN FETCH c.txTmGeneral ttm " +
                        "LEFT JOIN FETCH ttm.txTmBrand " +
                        "LEFT JOIN FETCH ttm.txRegistration", "txPostReception.id", postReceptionId, true, null, null);
    }

    public TxPostReception selectOneTxPostReception(String by, String value) {
        return txPostReceptionCustomRepository.selectOne(by, value, true);
    }

    public TxPostReception selectOneTxPostReception(List<KeyValue> criteriaList) {
        return txPostReceptionCustomRepository.selectOne(criteriaList, "id", "ASC");
    }

    public TxPostReceptionDetail selectOneTxPostReceptionDetail(String by, String value) {
        return txPostReceptionDetailCustomRepository.selectOne(by, value, true);
    }

    public MCountry selectOneCountryByCode(String code) {
        return mCountryCostumRepository.selectOne("code", code, true);
    }

    public MProvince selectOneProvinceByCode(String code) {
        return mProvinceCustomRepository.selectOne("code", code, true);
    }

    public MCity selectOneCityByCode(String code) {
        return mCityCustomRepository.selectOne("code", code, true);
    }

    public List<TxPostDoc> getAllDocByPostId(String by, String value) {
        return txPostDocCustomRepository.selectAll("LEFT JOIN FETCH c.mDocType mdt ", by, value, true, null, null);
    }

    public List<TxTmClass> getAllTxTmClass(String by, String value) {
        return txTmClassCustomRepository.selectAll("LEFT JOIN FETCH c.mClass", by, value, true, "mClass.no", "ASC", null, null);
    }

    @Transactional
    public void saveOrUpdateGeneral(TxPostReception txPostReception) {
        // check File Sequence
        if (txPostReception.getmFileType().isStatusPaid() == true) {
            MFileSequence mFileSequence = mFileSeqRepository.findOne(txPostReception.getmFileSequence().getCurrentId());

            if (mFileSequence == null || !mFileSequence.isStatusFlag()) {
                throw new DataIntegrityViolationException(HttpStatus.NOT_FOUND.getReasonPhrase() + "-MFileSequence");
            }

            txPostReception.setmFileSequence(mFileSequence);
        }

        // check File Type
		/*MFileType mFileType = mFileTypeRepository.findOne(txPostReception.getmFileType().getId());
		if (mFileType == null || !mFileType.isStatusFlag()) {
			throw new DataIntegrityViolationException(HttpStatus.NOT_FOUND.getReasonPhrase() + "-MFileType");
		}
		txPostReception.setmFileType(mFileType);
		txPostReception.setmWorkflow(mFileType.getmWorkflow());*/

        // generate application no
        if (txPostReception.geteFilingNo() == null || txPostReception.geteFilingNo().equals("")) {
            Map<String, Object> findCriteria = new HashMap<>();
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            findCriteria.put("year", currentYear);

            TcPostReceiptNo tcPostReceiptNo = tcPostReceiptNoCustomRepository.findOneBy(findCriteria, null);
            if (tcPostReceiptNo == null) {
                tcPostReceiptNo = new TcPostReceiptNo();
                tcPostReceiptNo.setYear(currentYear);
            }
            tcPostReceiptNo.setmFileType(mFileTypeRepository.findOne(txPostReception.getmFileType().getId()));
            tcPostReceiptNo.increaseSequence();
            tcPostReceiptNo.increaseExtensionSequence();
            tcPostReceiptNoCustomRepository.saveOrUpdate(tcPostReceiptNo);

//            String f = selectOneTxPostReception("id",txPostReception.getId()).getPostNo();

            txPostReception.setPostNo(tcPostReceiptNo.toString());
            TcFilingNo tcFilingNo = tcFilingNoCustomRepository.findOneBy(findCriteria, null);
            if (tcFilingNo == null) {
                tcFilingNo = new TcFilingNo();
            }

            tcFilingNo.setYear(currentYear);
            tcFilingNo.increaseSequence();
            tcFilingNoCustomRepository.saveOrUpdate(tcFilingNo);

            txPostReception.seteFilingNo(tcFilingNo.toString());

            txPostReception.setId(tcFilingNo.toString());
            txPostReception.setPostNo(tcPostReceiptNo.toString());
            txPostReception.setmStatus(StatusEnum.IPT_DRAFT.value());
        }

        txPostReceptionCustomRepository.saveOrUpdate(txPostReception);


    }

    @Transactional
    public void saveOrUpdateGeneralDetail(TxPostReception txPostReception){

        for (DataGeneral dg : txPostReception.getApplicationIdListTemp()) {
            TxPostReceptionDetail txPostReceptionDetail = new TxPostReceptionDetail();
            txPostReceptionDetail.setTxTmGeneral(txTmGeneralCustomRepository.selectOne("id", dg.getApplicationId()));
            txPostReceptionDetail.setTxPostReception(txPostReception);
            txPostReceptionDetail.setmStatus(StatusEnum.IPT_DRAFT.value());
            txPostReceptionDetailCustomRepository.saveOrUpdate(txPostReceptionDetail);
        }

    }


    @Transactional
    public void saveOrUpdateGeneralDetail2(TxPostReception txPostReception, String uuid){

        for (DataGeneral dg : txPostReception.getApplicationIdListTemp()) {
            TxPostReceptionDetail txPostReceptionDetail = new TxPostReceptionDetail();
            txPostReceptionDetail.setTxTmGeneral(txTmGeneralCustomRepository.selectOne("id", dg.getApplicationId()));
            txPostReceptionDetail.setTxPostReception(txPostReception);
            txPostReceptionDetail.setmStatus(StatusEnum.IPT_DRAFT.value());
            txPostReceptionDetail.setId(uuid);
            txPostReceptionDetailCustomRepository.saveOrUpdate(txPostReceptionDetail);
        }


    }


    @Transactional
    public void saveOrUpdatePemohon(TxPostOwner txPostOwner) {
        txPostOwnerCustomRepository.saveOrUpdate(txPostOwner);
    }

    @Transactional
    public void saveOrUpdateKuasa(TxPostRepresentative txPostRepresentative) {
        if (txPostRepresentative.getmRepresentative() == null) {
            txPostRepresentativeCustomRepository.delete(txPostRepresentative.getId());
        } else {
            txPostRepresentativeCustomRepository.saveOrUpdate(txPostRepresentative);
        }
    }

    @Transactional
    public void saveOrUpdateDokumen(String postId, List<TxPostDoc> listTxPostDoc) throws IOException {
        txPostDocCustomRepository.delete("txPostReception.id", postId);

        try {
            for (TxPostDoc txPostDoc : listTxPostDoc) {
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
            }
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Transactional
    public void savePratinjauPascaDokumen(TxPostDoc txPostDoc) throws IOException {

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
    public void saveOrUpdateResume(TxPostReception txPostReception) {
        txPostReceptionCustomRepository.saveOrUpdate(txPostReception);

        List<TxPostReceptionDetail> txPostReceptionDetailList = selectAllPostDetail(txPostReception.getId());
        for (TxPostReceptionDetail txPostReceptionDetail : txPostReceptionDetailList) {
            TxMonitor txMonitor = new TxMonitor();
            txMonitor.setTxTmGeneral(txPostReceptionDetail.getTxTmGeneral());
            txMonitor.setCreatedDate(new Timestamp(System.currentTimeMillis()));
            txMonitor.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            //MWorkflowProcess mWorkflowProcess = mWorkflowProcessCustomRepository.selectOne("status.id", txPostReceptionDetail.getTxTmGeneral().getmStatus().getId(), true);
            MWorkflowProcess mWorkflowProcess = mWorkflowProcessCustomRepository.selectOne("status.id", StatusEnum.TM_PASCA_PERMOHONAN.name(), true);
            txMonitor.setmWorkflowProcess(mWorkflowProcess);

            MWorkflowProcessActions mWorkflowProcessActions = mWorkflowProcessActionCustomRepository.selectOne("process.id", mWorkflowProcess.currentId());
//            txMonitor.setmWorkflowProcessActions(mWorkflowProcessActions);
            txMonitor.setNotes(txPostReception.getmFileType().getDesc());
            txMonitorRepository.save(txMonitor);
            txPostReceptionDetail.setmStatus(StatusEnum.TM_PASCA_PERMOHONAN.value());
            txPostReceptionDetailCustomRepository.saveOrUpdate(txPostReceptionDetail);
        }
    }

    @Transactional
    public void saveOrUpdatePenghapusan(TxPostReception txPostReception) {
        txPostReceptionCustomRepository.saveOrUpdate(txPostReception);
    }

    @Transactional
    public void saveOrUpdateGeneralFromMonitor(TxPostReceptionDetail txPostReceptionDetail) {
        txPostReceptionDetail.setTxTmGeneral(txTmGeneralCustomRepository.selectOne("id", txPostReceptionDetail.getTxTmGeneral().getId()));
        txPostReceptionDetail.setmStatus(StatusEnum.TM_PASCA_PERMOHONAN.value());
        txPostReceptionDetailCustomRepository.saveOrUpdate(txPostReceptionDetail);
    }

    public Long count(String by, Object value, boolean exactMatch) {
        return txPostReceptionDetailCustomRepository.count(by, value, exactMatch);
    }

    public List<TxPostReceptionDetail> listAll() {
        List<TxPostReceptionDetail> txPostReceptionDetail = new ArrayList<>();
        txPostReceptionDetailRepository.findAll().forEach(txPostReceptionDetail::add);
        return txPostReceptionDetail;
    }

    @Transactional
    public boolean deletePermohonanPasca(String noPermohonans) {
        try {
            for (String noPermohonan : noPermohonans.split(",")) {
                //System.out.println("START DELETE PASCA "+ noPermohonan);
                String postReceptionID = txPostReceptionRepository.findOne(noPermohonan).getId();
                if(postReceptionID != "") {
                    txPostOwnerRepository.deleteByPostReceptionID(postReceptionID);
                    txPostRepresentativeRepository.deleteByPostReceptionID(postReceptionID);
                    txPostDocRepository.deleteByPostReceptionID(postReceptionID);
                    txPostReceptionDetailRepository.deleteByPostReceptionID(postReceptionID);
                    txPostReceptionRepository.deleteByPostReceptionID(postReceptionID);
                }
            }
            //System.out.println("END DELETE PASCA");
        }catch(Exception e){
            //System.out.println("ERROR:"+e.toString());
            return false;
        }
        return true;
    }
}