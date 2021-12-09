package com.docotel.ki.repository.master;

import com.docotel.ki.model.master.MRoleDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MRoleDetailRepository extends JpaRepository<MRoleDetail, String> {
}
