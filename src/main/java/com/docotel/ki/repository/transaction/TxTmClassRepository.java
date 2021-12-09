package com.docotel.ki.repository.transaction;

import com.docotel.ki.model.master.MClassDetail;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.transaction.TxTmClass;
import com.docotel.ki.model.transaction.TxTmGeneral;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TxTmClassRepository extends JpaRepository<TxTmClass, String> {
    @Query("SELECT tm FROM TxTmClass tm WHERE tm.txTmGeneral=:param1 AND tm.transactionStatus=:param2")
    List<TxTmClass> findTxTmClassByTxTmGeneralAndTransactionStatus(@Param("param1") TxTmGeneral param, @Param("param2") String param2);

    List<TxTmClass> findTxTmClassByTxTmGeneral(TxTmGeneral txTmGeneral);

    List<TxTmClass> findTxTmClassByTxTmGeneral(TxTmGeneral txTmGeneral, Sort sort);

    @Query("SELECT tm FROM TxTmClass tm LEFT JOIN FETCH tm.mClass WHERE tm.txTmGeneral = :p1")
    List<TxTmClass> findTxTmClassByGeneralId(@Param("p1") TxTmGeneral txTmGeneral);
    List<TxTmClass> findByTxTmGeneral(TxTmGeneral txTmGeneral);

    @Modifying
    @Query(value = "UPDATE TxTmClass SET CREATED_BY=:userID, UPDATED_BY=:userID WHERE APPLICATION_ID=:appID")
    void updateUserbyApplicationID(@Param("appID") String appID, @Param("userID") MUser userID);

    @Modifying
    @Query(value = "DELETE FROM TxTmClass WHERE APPLICATION_ID=:appID")
    void deleteByApplicationID(@Param("appID") String appID);

    TxTmClass findOneByMClassDetail(MClassDetail mClassDetail);
    
    @Modifying
    @Query(value = "DELETE FROM TX_TM_CLASS "
    		+ "WHERE APPLICATION_ID = :appNo "
    		+ "AND TM_CLASS_STATUS = 'PENDING'" ,nativeQuery = true)
    void deleteBarangJasa(@Param("appNo") String appNo);
}
