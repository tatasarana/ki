package com.docotel.ki.repository.master;

import com.docotel.ki.model.master.MEmployee;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MEmployeeRepository extends JpaRepository<MEmployee, String> {
	
	@Query("SELECT c FROM MEmployee c LEFT JOIN FETCH c.mRoleSubstantifDetail")
	List<MEmployee> findAllMEmployee();
}
