package com.docotel.ki.repository.transaction;

import com.docotel.ki.model.transaction.TxTmClass;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.model.transaction.TxTmMadridFee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TxTmMadridFeeRepository extends JpaRepository<TxTmMadridFee, String> {

	public TxTmMadridFee findByTxTmGeneral(TxTmGeneral txTmGeneral);
}
