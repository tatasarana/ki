package com.docotel.ki.repository.transaction;

import com.docotel.ki.model.master.MStatus;
import com.docotel.ki.model.transaction.TxReception;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.transaction.TxTmGeneral;

import java.util.List;

import com.docotel.ki.model.transaction.TxTmOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TxTmGeneralRepository extends JpaRepository<TxTmGeneral, String> {
    TxTmGeneral findTxTmGeneralByApplicationNo(String no);

    TxTmGeneral findByTxReception(TxReception txReception);

    @Query(value = "SELECT tg.APPLICATION_ID FROM TX_TM_GENERAL tg LEFT JOIN TX_GROUP_DETAIL tgd ON tg.APPLICATION_ID = tgd.APPLICATION_ID "
            + "WHERE tgd.GROUP_DETAIL_ID IN (:groupDetailId)", nativeQuery = true)
    String[] findAllTxTmGeneralByTxGroupDetailIdNative(@Param("groupDetailId") String[] groupDetailId);

    @Query(value = "SELECT tg.APPLICATION_ID FROM TX_TM_GENERAL tg LEFT JOIN TX_GROUP_DETAIL tgd ON tg.APPLICATION_ID = tgd.APPLICATION_ID "
            + "WHERE tgd.GROUP_DETAIL_ID IN (:groupDetailId) AND tg.STATUS_ID = :status", nativeQuery = true)
    String[] findAllTxTmGeneralByTxGroupDetailIdAndStatusNative(@Param("groupDetailId") String[] groupDetailId, @Param("status") MStatus status);

    @Query(value = "SELECT tg FROM TxTmGeneral tg LEFT JOIN FETCH tg.txGroupDetail tgd LEFT JOIN FETCH tg.txRegistration tr LEFT JOIN FETCH tg.txTmBrand tb "
            + "WHERE tgd.id IN (:groupDetailId) AND tg.mStatus = :status")
    List<TxTmGeneral> findAllTxTmGeneralByTxGroupDetailIdAndStatus(@Param("groupDetailId") String[] groupDetailId, @Param("status") MStatus status);

    @Query(value = "SELECT APPLICATION_ID FROM TxTmGeneral WHERE APPLICATION_ID IN (:appId)", nativeQuery = true)
    String[] findAllTxTmGeneralByNative(@Param("appId") String[] appId);

    TxTmGeneral findByApplicationNo(String applicationNo);

    @Modifying
    @Query(value = "UPDATE TxTmGeneral SET CREATED_BY=:userID, UPDATED_BY=:userID WHERE APPLICATION_NO=:appNo")
    void updateUserbyApplicationID(@Param("appNo") String appNo, @Param("userID") MUser userID);

    @Modifying
    @Query(value = "UPDATE TxTmGeneral SET OWNER_LIST=:ownerList WHERE APPLICATION_ID=:appNo")
    void updateOwnerbyApplicationID(@Param("appNo") String appNo, @Param("ownerList") String ownerList);

    @Modifying
    @Query(value = "DELETE FROM TxTmGeneral WHERE APPLICATION_NO=:appNo")
    void deleteByApplicationID(@Param("appNo") String appNo);

    @Query(value = "SELECT * FROM TX_TM_GENERAL  WHERE APPLICATION_NO IN (:appNo) ORDER BY FILING_DATE DESC", nativeQuery = true) 
    List<TxTmGeneral> findAllTxTmGeneralByAppNo(@Param("appNo") String[] appNo);

}
