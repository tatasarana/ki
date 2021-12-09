package com.docotel.ki.repository.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.docotel.ki.model.transaction.TxTmCountry;

@Repository
public interface TxTmCountryRepository extends JpaRepository<TxTmCountry, String> {

}
