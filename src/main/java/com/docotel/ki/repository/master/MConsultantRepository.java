package com.docotel.ki.repository.master;

import com.docotel.ki.model.master.MRepresentative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MConsultantRepository extends JpaRepository<MRepresentative,String> {

}
