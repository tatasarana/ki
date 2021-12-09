package com.docotel.ki.repository.transaction;

import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.model.transaction.TxTmRepresentative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TxTmReprsRepository extends JpaRepository<TxTmRepresentative, String> {
    @Modifying
    @Transactional
    @Query("delete from TxTmRepresentative u where u.id = ?1")
    void deleteReps(String id);

    TxTmRepresentative findTxTmRepresentativeByTxTmGeneral(TxTmGeneral txTmGeneral);

    TxTmRepresentative findTxTmRepresentativeByTxTmGeneralAndStatusTrue (TxTmGeneral txTmGeneral);

    TxTmRepresentative findByTxTmGeneral(TxTmGeneral txTmGeneral);

    @Modifying
    @Query(value = "UPDATE TxTmRepresentative SET CREATED_BY=:userID, UPDATED_BY=:userID WHERE APPLICATION_ID=:appID")
    void updateUserbyApplicationID(@Param("appID") String appID, @Param("userID") MUser userID);

    @Modifying
    @Query(value = "DELETE FROM TxTmRepresentative WHERE APPLICATION_ID=:appID")
    void deleteByApplicationID(@Param("appID") String appID);
}
