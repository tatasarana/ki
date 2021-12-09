package com.docotel.ki.repository.transaction;


import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.transaction.TxReception;
import com.docotel.ki.model.transaction.TxTmGeneral;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TxReceptionRepository extends JpaRepository<TxReception, String> {

    TxReception findTxReceptionByApplicationNo(String applicationNo);
    TxReception findByApplicationNo(String applicationNo);

    List<TxReception> findByBankCode(String bankCode);

    @Modifying
    @Query(value = "UPDATE TxReception SET CREATED_BY=:userID WHERE APPLICATION_NO=:appNo")
    void updateUserbyApplicationID(@Param("appNo") String appNo, @Param("userID") MUser userID);

    @Modifying
    @Query(value = "DELETE FROM TxReception WHERE APPLICATION_NO=:appNo")
    void deleteByApplicationID(@Param("appNo") String appNo);

	@Query(nativeQuery= true,
			value="SELECT TOTAL_CLASS "
					+ " FROM  TX_RECEPTION   "
					+ " WHERE APPLICATION_NO = :generalId ")
	int countClass(@Param("generalId") TxTmGeneral generalId);

}
