package com.docotel.ki.repository.transaction;

import com.docotel.ki.model.transaction.TxLogHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TxLogHistoryRepository extends JpaRepository<TxLogHistory, String> {
}
