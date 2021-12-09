package com.docotel.ki.repository.transaction;

import com.docotel.ki.model.transaction.TxLicense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TxLicenseRepository extends JpaRepository<TxLicense, String>{

}
