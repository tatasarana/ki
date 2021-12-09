package com.docotel.ki.repository.master;


import com.docotel.ki.model.master.MUser;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MUserRepository extends JpaRepository<MUser, String> {

	List<MUser> findMUserByUserType(String userType);
}
