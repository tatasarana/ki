package com.docotel.ki.service.master;

import com.docotel.ki.model.master.MRepresentative;
import com.docotel.ki.model.transaction.TxTmRepresentative;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.transaction.MrepresentativeCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmReprsCustomRepository;
import com.docotel.ki.repository.master.MRepresentativeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepresentativeService {
    @Autowired MRepresentativeRepository mRepresentativeRepository;
    @Autowired MrepresentativeCustomRepository mrepresentativeCustomRepository;
    @Autowired TxTmReprsCustomRepository txTmReprsCustomRepository;

    public List<MRepresentative> findAll(){
        return mRepresentativeRepository.findAll();
    }
    public List<MRepresentative> findByStatusFlagTrue() {
        return mRepresentativeRepository.findByStatusFlagTrue();
    }
    
    // For list konsultan daftar-online
    public List<MRepresentative> selectAllReprsOrderByName() {
        return mRepresentativeRepository.selectAllOrderByName();
    }


//    public GenericSearchWrapper<MRepresentative> showListAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
//        GenericSearchWrapper<MRepresentative> searchResult = new GenericSearchWrapper<MRepresentative>();
//        searchResult.setCount(mRepresentativeRepository.count());
//        searchResult.setList(mRepresentativeRepository.findAll());
//
//        return searchResult;
//    }

    public GenericSearchWrapper<MRepresentative> searchGeneral(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        GenericSearchWrapper<MRepresentative> searchResult = new GenericSearchWrapper<MRepresentative>();
        searchResult.setCount(mrepresentativeCustomRepository.count(searchCriteria));
        searchResult.setList(mrepresentativeCustomRepository.selectAll(searchCriteria,orderBy,orderType,offset,limit));

        return searchResult;
    }


    //add wsh start
    public MRepresentative selectOne(String by, String value){
        return mrepresentativeCustomRepository.selectOne("LEFT JOIN FETCH c.mCountry ci " +
                        "LEFT JOIN FETCH c.mProvince p LEFT JOIN FETCH c.mCity ci "
                , by, value, true);
    }

	public MRepresentative selectOneByUserId(String userId) {
        return mrepresentativeCustomRepository.selectOneByUserId(userId);
	}
	
	public MRepresentative selectOneByUserIdReprs(String userId) {
        return mrepresentativeCustomRepository.selectOne("LEFT JOIN FETCH c.mCountry ci LEFT JOIN FETCH c.mProvince p "
        		+ " LEFT JOIN FETCH c.mCity ci LEFT JOIN FETCH c.userId ui "
        		, "userId.id", userId, true);
	}

    public MRepresentative findOneByNoReps(String noReps){
        MRepresentative mRepresentative =  mrepresentativeCustomRepository.selectOne("no",  noReps.trim(), false);

        if (mRepresentative == null){
            //System.out.println("NULL!");
            return null;
        }
        else
            return mRepresentative ;
    }



//    public MRepresentative SyncronizePDKKI(String noReps){
//        return mrepresentativeCustomRepository.SyncronizePDKKI(noReps);
//    }

	public TxTmRepresentative selectOneTxTmRepresentative(List<KeyValue> searchCriteria, String orderBy, String orderType) {
		return txTmReprsCustomRepository.selectOne("LEFT JOIN FETCH c.mRepresentative", searchCriteria, orderBy, orderType);
	}
}
