package com.docotel.ki.repository.transaction;

import com.docotel.ki.model.transaction.TxPostReceptionDetail;
import com.docotel.ki.model.transaction.TxTmGeneral;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TxPostReceptionDetailRepository extends JpaRepository<TxPostReceptionDetail, String> {
	TxPostReceptionDetail findTxPostReceptionDetailByTxTmGeneral(TxTmGeneral txTmGeneral);

	@Modifying
	@Query(value = "DELETE FROM TxPostReceptionDetail WHERE POST_RECEPTION_ID=:postReceptionID")
	void deleteByPostReceptionID(@Param("postReceptionID") String postReceptionID);

	@Modifying
	@Query(value = "UPDATE FROM TxPostReceptionDetail set STATUS_ID='DONE' WHERE POST_RECEPTION_ID=:postReceptionID")
	void updateStatusPasca(@Param("postReceptionID") String postReceptionID);
}
