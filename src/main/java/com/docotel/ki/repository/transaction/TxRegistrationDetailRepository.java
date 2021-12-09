package com.docotel.ki.repository.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.docotel.ki.model.transaction.TxRegistrationDetail;

@Repository
public interface TxRegistrationDetailRepository extends JpaRepository<TxRegistrationDetail, String> {
}
