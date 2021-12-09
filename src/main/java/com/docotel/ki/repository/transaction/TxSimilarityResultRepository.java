package com.docotel.ki.repository.transaction;

import com.docotel.ki.model.transaction.TxSimilarityResult;
import com.docotel.ki.model.transaction.TxTmGeneral;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TxSimilarityResultRepository extends JpaRepository<TxSimilarityResult, String> {

    List<TxSimilarityResult> findTxSimilarityResultByOriginTxTmGeneral(TxTmGeneral txTmGeneral);
}
