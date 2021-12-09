package com.docotel.ki.repository.transaction;

import com.docotel.ki.model.transaction.TxTmGeneral;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.docotel.ki.model.transaction.TxTmClassLimitation;

import java.util.List;

@Repository
public interface TxTmClassLimitationRepository extends JpaRepository<TxTmClassLimitation, String> {
   
	public TxTmClassLimitation findByTxTmGeneralIdAndMClassDetailIdAndMCountryId(String generalId, String classDetailId, String countryId);

	List<TxTmClassLimitation> findByTxTmGeneral(TxTmGeneral txTmGeneral, Sort sort);
}
