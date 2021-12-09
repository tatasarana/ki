package com.docotel.ki.repository.transaction;

import com.docotel.ki.model.transaction.TxPostReception;
import com.docotel.ki.model.transaction.TxPostRepresentative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TxPostRepresentativeRepository extends JpaRepository<TxPostRepresentative, String>{
	TxPostRepresentative findTxPostRepresentativeByTxPostReception(TxPostReception txPostReception);

	@Modifying
	@Query(value = "DELETE FROM TxPostRepresentative WHERE POST_RECEPTION_ID=:postReceptionID")
	void deleteByPostReceptionID(@Param("postReceptionID") String postReceptionID);
}
