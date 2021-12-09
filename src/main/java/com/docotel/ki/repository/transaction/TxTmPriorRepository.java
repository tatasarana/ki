package com.docotel.ki.repository.transaction;

import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.model.transaction.TxTmPrior;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TxTmPriorRepository extends JpaRepository<TxTmPrior, String> {
	List<TxTmPrior> findTxTmPriorByTxTmGeneral(TxTmGeneral txTmGeneral);

	TxTmPrior findByTxTmGeneral(TxTmGeneral txTmGeneral);

	@Modifying
	@Query(value = "UPDATE TxTmPrior SET CREATED_BY=:userID, UPDATED_BY=:userID WHERE APPLICATION_ID=:appID")
	void updateUserbyApplicationID(@Param("appID") String appID, @Param("userID") MUser userID);

	@Modifying
	@Query(value = "DELETE FROM TxTmPrior WHERE APPLICATION_ID=:appID")
	void deleteByApplicationID(@Param("appID") String appID);
}
