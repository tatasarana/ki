package com.docotel.ki.repository.master;

import com.docotel.ki.model.master.MRoleSubstantif;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MRoleSubstantifRepository extends JpaRepository<MRoleSubstantif, String> {

}
