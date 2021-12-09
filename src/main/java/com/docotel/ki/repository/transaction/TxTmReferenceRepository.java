package com.docotel.ki.repository.transaction;

import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.model.transaction.TxTmReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TxTmReferenceRepository extends JpaRepository<TxTmReference, String> {

	@Query(nativeQuery = true,
	value = "SELECT * " +
			"FROM TX_TM_REFERENCE  " +
			"WHERE APPLICATION_ID = :generalId  " +
			"AND REF_APPLICATION_ID = :eFilingNo ")
	TxTmReference getReffByTxTmGeneralIdAndEFilingNo(@Param("generalId") String generalId, @Param("eFilingNo") String eFilingNo);

	@Query(nativeQuery = true,
			value = "SELECT * " +
					"FROM TX_TM_REFERENCE  " +
					"WHERE APPLICATION_ID = :generalId  ")
	List<TxTmReference> getByTxTmGeneralId(@Param("generalId") String generalId);

	@Query(nativeQuery = true,
			value = "SELECT * " +
					"FROM TX_TM_REFERENCE  " +
					"WHERE ref_application_id = :generalId ")
	List<TxTmReference> searchRefId(@Param("generalId") String generalId);
	
	
	@Query(nativeQuery= true,
			value="SELECT COUNT (DISTINCT ttc.CLASS_ID) "
					+ " FROM TX_TM_REFERENCE ttr JOIN TX_TM_CLASS ttc ON ttr.REF_APPLICATION_ID = ttc.APPLICATION_ID "
					+ " WHERE ttr.APPLICATION_ID = :generalId ")
	int countClass(@Param("generalId") TxTmGeneral txTmGeneral);
	
	
 
}
