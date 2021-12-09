package com.docotel.ki.repository.transaction;


import com.docotel.ki.model.transaction.TxGroupDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TxGroupDetailRepository extends JpaRepository<TxGroupDetail, String> {
	@Query(value="SELECT c FROM TxGroupDetail c LEFT JOIN c.txTmGeneral ttg "
			+ "LEFT JOIN c.txGroup tg WHERE ttg.id = :idGeneral AND tg.groupType.id = :groupType")
     TxGroupDetail findTxGroupDetailByTxTmGeneral(@Param("idGeneral") String idGeneral, @Param("groupType") String groupType);
	
	@Query(value="SELECT c.id FROM TxGroupDetail c LEFT JOIN c.txGroup tg WHERE tg.id = :groupId")
    String[] findTxGroupDetailByTxGroupId(@Param("groupId") String groupId);
	
}
