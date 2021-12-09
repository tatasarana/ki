package com.docotel.ki.repository.transaction;

import com.docotel.ki.model.transaction.TxGroup;
import com.docotel.ki.model.transaction.TxPubsJournal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TxPubsJournalRepository extends JpaRepository<TxPubsJournal, String> {
     TxPubsJournal findTxPubsJournalByTxGroup(TxGroup txGroup);
     
     @Query("SELECT COUNT(tpj) FROM TxPubsJournal tpj LEFT JOIN tpj.txGroup tg "
    		 + "LEFT JOIN tg.txGroupDetailList tgd WHERE tpj.id = :idJournal")
     long countTxGroupDetailByPubsId(@Param("idJournal") String idJournal);
}
