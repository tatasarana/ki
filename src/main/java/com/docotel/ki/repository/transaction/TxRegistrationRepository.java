package com.docotel.ki.repository.transaction;

import com.docotel.ki.model.transaction.TxRegistration;
import com.docotel.ki.model.transaction.TxTmGeneral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TxRegistrationRepository extends JpaRepository<TxRegistration, String> {

    TxRegistration findTxRegistrationByTxTmGeneral(TxTmGeneral txTmGeneral);
    TxRegistration findByTxTmGeneral(TxTmGeneral txTmGeneral);
}
