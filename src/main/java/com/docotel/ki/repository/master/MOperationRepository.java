package com.docotel.ki.repository.master;

import com.docotel.ki.model.master.MOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MOperationRepository extends JpaRepository<MOperation, String> {
}
