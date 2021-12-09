package com.docotel.ki.repository.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.docotel.ki.model.master.MCountry;
import com.docotel.ki.model.transaction.TxTmMadridFee;
import com.docotel.ki.model.transaction.TxTmMadridFeeDetail;

@Repository
public interface TxTmMadridFeeDetailRepository extends JpaRepository<TxTmMadridFeeDetail, String> {
	
	public TxTmMadridFeeDetail findByTxTmMadridFeeAndMCountry(TxTmMadridFee madridFee, MCountry country);

}
