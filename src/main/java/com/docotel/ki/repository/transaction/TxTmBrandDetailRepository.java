package com.docotel.ki.repository.transaction;

import com.docotel.ki.model.transaction.TxTmBrandDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TxTmBrandDetailRepository extends JpaRepository<TxTmBrandDetail, String> {
}
