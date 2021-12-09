package com.docotel.ki.repository.transaction;

import com.docotel.ki.model.transaction.TxServDist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface TxServDistRepository extends JpaRepository<TxServDist, String> {

    @Transactional
    @Modifying
    @Query("delete from TxServDist t where t.id=:id")
    void deleteTxServDist(@Param("id") String id);
}
