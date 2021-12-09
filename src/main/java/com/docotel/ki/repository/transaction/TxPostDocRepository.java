package com.docotel.ki.repository.transaction;

import com.docotel.ki.model.transaction.TxPostDoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TxPostDocRepository extends JpaRepository<TxPostDoc, String> {

    @Modifying
    @Query(value = "DELETE FROM TxPostDoc WHERE POST_RECEPTION_ID=:postReceptionID")
    void deleteByPostReceptionID(@Param("postReceptionID") String postReceptionID);
}
