package com.docotel.ki.repository.online.jpa;

import com.docotel.ki.model.transaction.TxOnlineReg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistrasiOnlineRepository extends JpaRepository<TxOnlineReg, String> {

    @Query(value = "SELECT c FROM TxOnlineReg c WHERE c.id=:value")
    TxOnlineReg findByUserEfiling(@Param( "value" ) String user_id);






}
