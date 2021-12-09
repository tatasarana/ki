package com.docotel.ki.repository.transaction;

import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.model.transaction.TxTmOwner;
import com.docotel.ki.model.transaction.TxTmOwnerDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TxTmOwnerDetailRepository extends JpaRepository<TxTmOwnerDetail, String> {

    List<TxTmOwnerDetail> findByTxTmGeneral(TxTmGeneral txTmGeneral);
}
