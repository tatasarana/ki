package com.docotel.ki.repository.transaction;

import com.docotel.ki.model.transaction.TxPostDoc;
import com.docotel.ki.model.transaction.TxPostOwner;
import com.docotel.ki.model.transaction.TxPostReception;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TxPostOwnerRepository extends JpaRepository<TxPostDoc, String> {
    TxPostOwner findTxPostOwnerByTxPostReception(TxPostReception txPostReception);

    @Modifying
    @Query(value = "DELETE FROM TxPostOwner WHERE POST_RECEPTION_ID=:postReceptionID")
    void deleteByPostReceptionID(@Param("postReceptionID") String postReceptionID);
}
