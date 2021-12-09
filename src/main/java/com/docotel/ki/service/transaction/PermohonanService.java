package com.docotel.ki.service.transaction;

import com.docotel.ki.model.master.MFileType;
import com.docotel.ki.model.master.MLaw;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.master.MWorkflow;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.pojo.DataForm1;
import com.docotel.ki.repository.custom.transaction.*;
import com.docotel.ki.repository.transaction.*;
import com.docotel.ki.service.master.UserService;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.repository.custom.transaction.*;
import com.docotel.ki.repository.master.MFileTypeRepository;
import com.docotel.ki.repository.master.MLawRepository;
import com.docotel.ki.repository.master.MWorkflowRepository;

import org.jfree.util.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class PermohonanService {

    @Autowired
    private TxTmGeneralRepository txTmGeneralRepository;
    @Autowired
    private TxTmOwnerCustomRepository txTmOwnerCustomRepository;
    @Autowired
    private TxTmOwnerRepository txTmOwnerRepository;
    @Autowired
    private TxTmOwnerDetailCustomRepository txTmOwnerDetailCustomRepository;
    @Autowired
    private TxTmReprsCustomRepository txTmReprsCustomRepository;
    @Autowired
    private TxTmReprsRepository txTmReprsRepository;
    @Autowired
    private MFileTypeRepository mFileTypeRepository;
    @Autowired
    private MLawRepository mLawRepository;
    @Autowired
    private TrademarkService trademarkService;
    @Autowired
    private TxReceptionCustomRepository txReceptionCustomRepository;
    @Autowired
    private TxReceptionRepository txReceptionRepository;
    @Autowired
    private TxTmGeneralCustomRepository txTmGeneralCustomRepository;
    @Autowired
    private TxTmPriorCustomRepository txTmPriorCustomRepository;
    @Autowired
    private TxTmPriorRepository txTmPriorRepository;
    @Autowired
    private MWorkflowRepository mWorkflowRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private TxTmBrandCustomRepository txTmBrandCustomRepository;
    @Autowired
    private TxTmBrandRepository txTmBrandRepository;
    @Autowired
    private TxTmClassCustomRepository txTmClassCustomRepository;
    @Autowired
    private TxTmClassRepository txTmClassRepository;
    @Autowired
    private TxTmDocCustomRepository txTmDocCustomRepository;
    @Autowired
    private TxTmDocRepository txTmDocRepository;


    @Transactional
    public List<TxTmGeneral> findAll() {
        return txTmGeneralRepository.findAll();
    }

    public <S extends TxTmOwner> S save(S s) {
        return txTmOwnerRepository.save(s);
    }

    @Transactional
    public void insertForm2(TxTmOwner pemohon) {
        String application_id =  pemohon.getTxTmGeneral().getId();
//        txTmOwnerDetailCustomRepository.delete("txTmOwner.id", pemohon.getId());
//        txTmOwnerDetailCustomRepository.delete("txTmGeneral.id",application_id);
//        txTmOwnerCustomRepository.delete("txTmGeneral.id",application_id);
//        long isOke = txTmOwnerCustomRepository.count("id", pemohon.getId(), false);
//
//        if (isOke != 0) {
//            txTmOwnerRepository.delete(pemohon);
//        }
//        txTmOwnerCustomRepository.saveOrUpdate(pemohon);
        /*
           FIXBUG https://gitlab.docotel.net/neviim/ki/merk/issues/223
           menghindari data ter-insert lebih dari sekali
         */
//        TxTmOwner newpemohon = txTmOwnerCustomRepository.selectOne("txTmGeneral.id",application_id,true);
//        if (newpemohon == null){
            txTmOwnerCustomRepository.saveOrUpdate(pemohon);
            txTmGeneralRepository.updateOwnerbyApplicationID(application_id,pemohon.getName());
//        }


        if (pemohon.getTxTmOwnerDetails() != null) {
            for (TxTmOwnerDetail txTmOwnerDetail : pemohon.getTxTmOwnerDetails()) {
                txTmOwnerDetailCustomRepository.saveOrUpdate(txTmOwnerDetail);
            }
        }

        /*StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + pemohon.getCurrentId());
        sb.append(", Application No: " + pemohon.getTxTmGeneral().getApplicationNo());
        sb.append(", Nama: " + pemohon.getName());
        sb.append(", Negara: " + pemohon.getmCountry().getName());
        sb.append(", Alamat: " + pemohon.getAddress());
        sb.append(", Telepon: " + pemohon.getPhone());
        sb.append(", Email: " + pemohon.getEmail());
*/

    }

    @Transactional
    public void SaveEditPemohon(TxTmOwner pemohon, TxTmOwner oldPemohon) {
        txTmOwnerDetailCustomRepository.delete("txTmOwner.id", pemohon.getId());
        String application_id =  pemohon.getTxTmGeneral().getId();
        List<TxTmOwner> txTmOwner = txTmOwnerCustomRepository.selectTxTmOwnerByTxTmGeneral(pemohon.getTxTmGeneral().getId(), true);
        String ownerName = "";
        ArrayList<String> ownerTemp = new ArrayList<String>();
        for (TxTmOwner owners : txTmOwner) {
            ownerTemp.add("" + owners.getName() == null ? "" : owners.getName());
        }
        Set<String> temp = new LinkedHashSet<String>(ownerTemp);
        String[] unique = temp.toArray(new String[temp.size()]);
        if (txTmOwner.size() > 0) {
            ownerName = String.join(", ", unique);
        }

//        txTmOwnerCustomRepository.saveOrUpdate(oldPemohon);
        txTmOwnerCustomRepository.saveOrUpdate(pemohon);
        txTmGeneralRepository.updateOwnerbyApplicationID(application_id,ownerName);
//
//        if (oldPemohon.getTxTmOwnerDetails() != null) {
//            for (TxTmOwnerDetail txTmOwnerDetail : oldPemohon.getTxTmOwnerDetails()) {
//                txTmOwnerDetailCustomRepository.saveOrUpdate(txTmOwnerDetail);
//            }
//        }

        if (pemohon.getTxTmOwnerDetails() != null) {
            for (TxTmOwnerDetail txTmOwnerDetail : pemohon.getTxTmOwnerDetails()) {
                txTmOwnerDetailCustomRepository.saveOrUpdate(txTmOwnerDetail);
            }
        }


        StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + pemohon.getCurrentId());
        sb.append(", Application No: " + pemohon.getTxTmGeneral().getApplicationNo());
        sb.append(", Nama: " + pemohon.getName());
        sb.append(", Negara: " + pemohon.getmCountry().getName());
        sb.append(", Alamat: " + pemohon.getAddress());
        sb.append(", Telepon: " + pemohon.getPhone());
        sb.append(", Email: " + pemohon.getEmail());


    }

    public List<TxTmOwner> selectAllByOwner(String value) {
        return txTmOwnerCustomRepository.selectAll("LEFT JOIN FETCH c.txTmGeneral tg", "txTmGeneral.id", value, true, null, null);
    }

    @Transactional
    public void insertPemohon(TxTmOwner pemohon) {
//        txTmOwnerDetailCustomRepository.delete("txTmOwner.id", pemohon.getId());
        String application_id =  pemohon.getTxTmGeneral().getId();

        TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(pemohon.getTxTmGeneral().getApplicationNo());
        List<TxTmOwner> owners = txTmOwnerCustomRepository.selectTxTmOwnerByTxTmGeneral(txTmGeneral.getId(), true);
        for (TxTmOwner txOwners : owners) {
//        	txOwners.setStatus(false);
        	txTmOwnerCustomRepository.saveOrUpdate(txOwners);
        }
        txTmOwnerCustomRepository.saveOrUpdate(pemohon);
        txTmGeneralRepository.updateOwnerbyApplicationID(application_id,pemohon.getName());

        if (pemohon.getTxTmOwnerDetails() != null) {
            for (TxTmOwnerDetail txTmOwnerDetail : pemohon.getTxTmOwnerDetails()) {
                txTmOwnerDetailCustomRepository.saveOrUpdate(txTmOwnerDetail);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + pemohon.getCurrentId());
        sb.append(", Application No: " + pemohon.getTxTmGeneral().getApplicationNo());
        sb.append(", Nama: " + pemohon.getName());
        sb.append(", Negara: " + pemohon.getmCountry().getName());
        sb.append(", Alamat: " + pemohon.getAddress());
        sb.append(", Telepon: " + pemohon.getPhone());
        sb.append(", Email: " + pemohon.getEmail());
    }

    @Transactional
    public void insertPemohonEditPasca(TxTmOwner pemohon) {
        txTmOwnerDetailCustomRepository.delete("txTmOwner.id", pemohon.getId());
        String application_id =  pemohon.getTxTmGeneral().getId();
        txTmOwnerCustomRepository.saveOrUpdate(pemohon);
        txTmGeneralRepository.updateOwnerbyApplicationID(application_id,pemohon.getName());

        if (pemohon.getTxTmOwnerDetails() != null) {
            for (TxTmOwnerDetail txTmOwnerDetail : pemohon.getTxTmOwnerDetails()) {
                txTmOwnerDetailCustomRepository.saveOrUpdate(txTmOwnerDetail);
            }
        }

    }

    @Transactional
    public boolean updatePermohonanEmail(String noPermohonans, String emailUserBaru) {
        try {
            MUser mUser = userService.getUserByEmail(emailUserBaru);
            //System.out.println("Pemindahan permohonan ke: "+mUser.getId());
            for (String noPermohonan : noPermohonans.split(",")) {
                //System.out.println("START SAVE "+ noPermohonan);
                String appID = txTmGeneralRepository.findTxTmGeneralByApplicationNo(noPermohonan).getId();
                txTmClassRepository.updateUserbyApplicationID(appID, mUser);
                txReceptionRepository.updateUserbyApplicationID(noPermohonan, mUser);
                txTmOwnerRepository.updateUserbyApplicationID(appID, mUser);
                txTmReprsRepository.updateUserbyApplicationID(appID, mUser);
                txTmPriorRepository.updateUserbyApplicationID(appID, mUser);
                txTmBrandRepository.updateUserbyApplicationID(appID, mUser);
                txTmDocRepository.updateUserbyApplicationID(appID, mUser);
                txTmGeneralRepository.updateUserbyApplicationID(noPermohonan, mUser);
            }
            //System.out.println("END SAVE");
        }catch(Exception e){
            //System.out.println(e.toString());
            return false;
        }
        return true;
    }


    @Transactional
    public boolean deletePermohonan(String noPermohonans) {
        try {
            for (String noPermohonan : noPermohonans.split(",")) {
                //System.out.println("START DELETE "+ noPermohonan);
                String appID = txTmGeneralRepository.findTxTmGeneralByApplicationNo(noPermohonan).getId();
                txTmClassRepository.deleteByApplicationID(appID);
                txReceptionRepository.deleteByApplicationID(noPermohonan);
                txTmOwnerRepository.deleteByApplicationID(appID);
                txTmReprsRepository.deleteByApplicationID(appID);
                txTmPriorRepository.deleteByApplicationID(appID);
                txTmBrandRepository.deleteByApplicationID(appID);
                txTmDocRepository.deleteByApplicationID(appID);
                txTmGeneralRepository.deleteByApplicationID(noPermohonan);
            }
            //System.out.println("END DELETE");
        }catch(Exception e){
            //System.out.println("ERROR:"+e.toString());
            return false;
        }
        return true;
    }

    @Transactional
    public void updateForm1(DataForm1 data, String newApplicationNo) {
        TxReception txReception = trademarkService.selectOneReceptionByApplicationNo(data.getAppid());
        TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(data.getAppid());

        txTmGeneral.setApplicationNo(newApplicationNo);
        txTmGeneralCustomRepository.saveOrUpdate(txTmGeneral);

        StringBuilder sb = new StringBuilder();
        sb.append(", Tipe Permohonan: " + txReception.getmFileType().getDesc());
    }


    @Transactional
    public void insertForm1(DataForm1 data) {
        TxReception txReception = trademarkService.selectOneReceptionByApplicationNo(data.getAppid());
        TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(data.getAppid());
        MFileType mFileType = mFileTypeRepository.findOne(data.getTipePermohonan());
        MLaw mLaw = mLawRepository.findOne(data.getLaw());
        MWorkflow mWorkflow = mWorkflowRepository.findOne(data.getWorkflow());
        txReception.setmFileType(mFileType);
        txReception.setmWorkflow(mWorkflow);
        txReception.setTotalPayment(data.getTotalPayment());
        txReception.setTotalClass(data.getTotalClass());
        txReception.setNotesIpas(data.getNotesIpas());
        txReceptionCustomRepository.saveOrUpdate(txReception);

        txTmGeneral.setFilingDateTemp(data.getTgl() + DateUtil.formatDate(new Timestamp(System.currentTimeMillis()), " HH:mm:ss"));
        txTmGeneral.setmLaw(mLaw);
        txTmGeneral.setmStatus(data.getStatus());
        txTmGeneral.setmAction(data.getAction());
        txTmGeneralCustomRepository.saveOrUpdate(txTmGeneral);

        StringBuilder sb = new StringBuilder();
        //sb.append("UUID: " + txTmGeneral.getCurrentId());
        //sb.append(", Application No: " + txTmGeneral.getApplicationNo());
        sb.append(", Tipe Permohonan: " + txReception.getmFileType().getDesc());
        //sb.append(", Tgl Penerimaan: " + txTmGeneral.getFilingDateTemp());
        //sb.append(", Dasar Hukum: " + txTmGeneral.getmLaw().getDesc());


    }

    public TxTmOwner selectOneOwnerByApplicationNo(String applicationNo) {
        return txTmOwnerCustomRepository.selectOne("txTmGeneral.id", applicationNo);
    }

    @Transactional
    public List<TxTmOwner> selectAllOwnerByGeneralId(String generalId) {
        return txTmOwnerCustomRepository.selectAll("INNER JOIN FETCH c.txTmGeneral tg"
                + "LEFT JOIN FETCH c.mCountry mc "
                + " ", "txTmGeneral.id", generalId, false, 0, 1000);
    }

    // Custom by Andra for list pratinjau permohonan (form pemohon)
    @Transactional
    public List<TxTmOwner> selectAllOwnerByIdGeneral(String generalId) {
        return txTmOwnerCustomRepository.selectAllOwnerByIdGeneral(generalId);
    }

    public TxTmOwner selectOneOwnerById(String id) {
        return txTmOwnerCustomRepository.selectOne("id", id);
    }

    public TxTmOwnerDetail selectOneOwnerDetailById(String id) {
        return txTmOwnerDetailCustomRepository.selectOne("id", id);
    }

    @Transactional
    public List<TxTmOwnerDetail> selectAllOwnerByOwnerId(String OwnerId) {
        return txTmOwnerDetailCustomRepository.selectAll("INNER JOIN FETCH c.txTmGeneral tg"
                + "INNER JOIN FETCH c.txTmOwner to "
                + " ", "txTmOwner.id", OwnerId, false, 0, 1000);
    }

    @Transactional
    public List<TxTmPrior> selectAllPriorByGeneralId(String generalId) {
        return txTmPriorCustomRepository.selectAll("INNER JOIN FETCH c.txTmGeneral tg"
                + "LEFT JOIN FETCH c.mCountry mc "
                + " ", "txTmGeneral.id", generalId, false, 0, 1000);
    }

    @Transactional
    public List<TxTmOwner> selectAllOwnerByIdGenerals(String generalId, boolean status) {
        return txTmOwnerCustomRepository.selectTxTmOwnerByTxTmGeneral(generalId, status);
    }

    @Transactional
    public List<TxTmOwnerDetail> selectTxTmOwnerDetailByTxTmOwner(String generalId, boolean status) {
        return txTmOwnerDetailCustomRepository.selectTxTmOwnerDetailByTxTmOwner(generalId, status);
    }

    @Transactional
    public List<TxTmRepresentative> selectTxTmReprsByGeneralId(String generalId, boolean status) {
        return txTmReprsCustomRepository.selectTxTmReprsByGeneral(generalId, status);
    }

    
    public List<TxTmGeneral> findAllTxTmGeneralByAppNo(String[] generalAppNoList){
		return txTmGeneralRepository.findAllTxTmGeneralByAppNo(generalAppNoList);
	}
	
}
