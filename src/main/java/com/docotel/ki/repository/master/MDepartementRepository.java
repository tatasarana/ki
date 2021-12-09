package com.docotel.ki.repository.master;

import com.docotel.ki.model.master.MDepartment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MDepartementRepository extends JpaRepository<MDepartment, String> {

	@Query("SELECT a FROM MDepartment a ORDER BY a.code")
	List<MDepartment> selectAllMDepartment();
}
