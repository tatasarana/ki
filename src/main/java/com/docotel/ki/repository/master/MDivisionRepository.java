package com.docotel.ki.repository.master;

import com.docotel.ki.model.master.MDivision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MDivisionRepository extends JpaRepository<MDivision, String> {
}
