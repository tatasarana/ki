package com.docotel.ki.service.transaction;

import com.docotel.ki.model.master.MRepresentative;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.model.transaction.TxTmRepresentative;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.transaction.MrepresentativeCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmGeneralCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmReprsCustomRepository;
import com.docotel.ki.repository.transaction.TxTmReprsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReprsService {
    @Autowired
    private TxTmReprsRepository txTmReprsRepository;
    @Autowired
    private TxTmReprsCustomRepository txTmReprsCustomRepository;
    @Autowired
    private MrepresentativeCustomRepository mrepresentativeCustomRepository;
    @Autowired
    private TxTmGeneralCustomRepository txTmGeneralCustomRepository;

    @Transactional
    public void simpanKuasa(TxTmRepresentative txTmRepresentative, String idOld){
        txTmReprsCustomRepository.saveOrUpdate(txTmRepresentative);
    }
    
    public TxTmRepresentative selectOneKriteria(String joinCriteria, List<KeyValue> searchCriteria, String orderBy, String orderType){
    	return txTmReprsCustomRepository.selectOneT(joinCriteria, searchCriteria, orderBy, orderType);
    }
    
    @Transactional
    public void simpanEditKuasa(TxTmRepresentative tmRepresentative, String idOld){
        txTmReprsCustomRepository.saveOrUpdate(tmRepresentative);
    }

    public TxTmRepresentative selectOneByApplicationId(String appId){
        return txTmReprsCustomRepository.selectOne("txTmGeneral.id", appId);
    }
    
    public List<TxTmRepresentative> selectOneByTxGeneralId(String appId){
        return txTmReprsCustomRepository.selectTxTmReprsByGeneral(appId);
    }
    
    public TxTmRepresentative selectOne(String by, String value){
        return txTmReprsCustomRepository.selectOne(by, value, true);
    }
    
    // Custom by Andra for List pratinjau permohonan (form kuasa)
//    @Transactional
    public List<TxTmRepresentative> selectAllReprsByIdGeneral(String generalId) {
    	return txTmReprsCustomRepository.selectAllReprsByIdGeneral(generalId);
    }
    
    public  TxTmRepresentative selectOneById(String id){
        return txTmReprsCustomRepository.selectOne("id", id);
    }

    @Transactional
    public void hapusKuasa(TxTmRepresentative txTmRepresentative){

        // olly@docotel.com //fix hapus kuasa dan represlist jika ada nama kuasa yg sama
        TxTmGeneral tgeneral = txTmRepresentative.getTxTmGeneral();
        MRepresentative mr = txTmRepresentative.getmRepresentative();
        String mrname = mr.getName();
        String grListDB = "";
        int grListDBlength = tgeneral.getTxTmRepresentative().toArray().length;

        for (int i = 0; i < grListDBlength; i++) {
            grListDB = grListDB + ", " + ((TxTmRepresentative)tgeneral.getTxTmRepresentative().toArray()[i]).getmRepresentative().getName();
            grListDB = grListDB.startsWith(",") ? grListDB.substring(1).trim() : grListDB.trim();
        }

        if (mrname.contentEquals(grListDB)) {
            grListDB = null;
        } else {
            grListDB = grListDB.replaceFirst(mrname, "").replace("\n", "").replace(",,", ",").replace(", ,", ",").trim();
            grListDB = grListDB.startsWith(",") ? grListDB.substring(1).trim() : grListDB.trim();
        }

        tgeneral.setRepresList(grListDB);
        txTmGeneralCustomRepository.saveOrUpdate(tgeneral);
        txTmReprsRepository.deleteReps(txTmRepresentative.getId());

        // olly@docotel.com
    }


    @Transactional
    public void hapusMasterKuasa(MRepresentative mRepresentative){
    	mrepresentativeCustomRepository.delete(mRepresentative);
    }
    

    public MRepresentative findOneByName(String name) {
       return mrepresentativeCustomRepository.selectOne("name",  name.toUpperCase().trim(), false);
    }



}
