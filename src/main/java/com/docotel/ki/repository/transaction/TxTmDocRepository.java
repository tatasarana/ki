package com.docotel.ki.repository.transaction;

import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.transaction.TxTmDoc;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TxTmDocRepository extends JpaRepository<TxTmDoc, String> {

    @Modifying
    @Query(value = "UPDATE TxTmDoc SET CREATED_BY=:userID WHERE APPLICATION_ID=:appID")
    void updateUserbyApplicationID(@Param("appID") String appID, @Param("userID") MUser userID);

    @Modifying
    @Query(value = "DELETE FROM TxTmDoc WHERE APPLICATION_ID=:appID")
    void deleteByApplicationID(@Param("appID") String appID);
}
