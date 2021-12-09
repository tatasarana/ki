package com.docotel.ki.repository.master;

import com.docotel.ki.model.master.MSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MSectionRepository extends JpaRepository<MSection, String> {
}
