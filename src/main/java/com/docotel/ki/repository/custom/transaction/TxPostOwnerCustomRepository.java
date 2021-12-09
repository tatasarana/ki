package com.docotel.ki.repository.custom.transaction;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.transaction.TxPostOwner;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

@Repository
public class TxPostOwnerCustomRepository extends BaseRepository<TxPostOwner> {

    public List<String> findAllTxPostOwnerNameByApplicationID(String applicationID) {
        Query query = entityManager.createNativeQuery("SELECT POST_RECEPTION_ID FROM TX_POST_RECEPTION_DETAIL WHERE APPLICATION_ID=:p1");
        query.setParameter("p1", applicationID.toString());
        List postReceptionID = query.getResultList();
        if(postReceptionID.size()>0){
            query = entityManager.createNativeQuery("SELECT POST_OWNER_NAME FROM TX_POST_OWNER WHERE POST_RECEPTION_ID=:p1");
            query.setParameter("p1",postReceptionID.get(0).toString());
            List<String> postOwnerName = query.getResultList();
            if(postOwnerName.size()>0){
                return postOwnerName;
            }
        }
        return null;
    }

    public List<String> findAllTxPostOwnerAddressByApplicationID(String applicationID) {
        Query query = entityManager.createNativeQuery("SELECT POST_RECEPTION_ID FROM TX_POST_RECEPTION_DETAIL WHERE APPLICATION_ID=:p1");
        query.setParameter("p1", applicationID.toString());
        List postReceptionID = query.getResultList();
        if(postReceptionID.size()>0){
            query = entityManager.createNativeQuery("SELECT POST_OWNER_ADDRESS FROM TX_POST_OWNER WHERE POST_RECEPTION_ID=:p1");
            query.setParameter("p1",postReceptionID.get(0).toString());
            List<String> postOwnerAddress = query.getResultList();
            if(postOwnerAddress.size()>0){
                return postOwnerAddress;
            }
        }
        return null;
    }
}
