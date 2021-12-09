package com.docotel.ki.repository.transaction;

import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.model.transaction.TxTmOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TxTmOwnerRepository extends JpaRepository<TxTmOwner, String> {

    TxTmOwner findTxTmOwnerByTxTmGeneral(TxTmGeneral txTmGeneral);
    TxTmOwner findTxTmOwnerByTxTmGeneralAndStatus(TxTmGeneral txTmGeneral, boolean status);
    TxTmOwner findByTxTmGeneral(TxTmGeneral txTmGeneral);

    @Modifying
    @Query(value = "UPDATE TxTmOwner SET CREATED_BY=:userID, UPDATED_BY=:userID WHERE APPLICATION_ID=:appID")
    void updateUserbyApplicationID(@Param("appID") String appID, @Param("userID") MUser userID);

    @Modifying
    @Query(value = "DELETE FROM TxTmOwner WHERE APPLICATION_ID=:appID")
    void deleteByApplicationID(@Param("appID") String appID);
}
