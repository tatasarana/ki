package com.docotel.ki.repository.master;

import com.docotel.ki.model.master.MBrandType;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MBrandTypeRepository extends JpaRepository<MBrandType, String> {
	
	@Query
    public List<MBrandType> findByStatusFlagTrue();
}
