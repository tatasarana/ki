package com.docotel.ki.repository.master;

import com.docotel.ki.model.master.MRole;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MRoleRepository extends JpaRepository<MRole, String> {
	@Query
    public List<MRole> findByStatusFlagTrue();
}
