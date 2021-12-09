package com.docotel.ki.repository.master;

import com.docotel.ki.model.master.MCity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MCityRepository extends JpaRepository<MCity,String> {
	@Query
    List<MCity> findByStatusFlagTrue();
	
	@Query
    List<MCity> findByStatusFlagTrueOrderByName();
}
