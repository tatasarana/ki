package com.docotel.ki.repository.transaction;

import com.docotel.ki.model.transaction.TxMadrid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TxMadridRepository extends JpaRepository<TxMadrid, String> {

    TxMadrid findByIntregn(String intregn);
   /* @Modifying
    @Transactional
    @Query("delete from TxTmRepresentative u where u.id = ?1")
    void deleteReps(String id);

    TxTmRepresentative findTxTmRepresentativeByTxTmGeneral(TxTmGeneral txTmGeneral);*/

   @Query(value = "SELECT DISTINCT TM.tranTyp FROM TxMadrid TM")
    List<String> findDistinctTrantype();

    @Query(value = "SELECT DISTINCT TM.no FROM TxMadrid TM")
    List<String> findDistinctNo();
}
