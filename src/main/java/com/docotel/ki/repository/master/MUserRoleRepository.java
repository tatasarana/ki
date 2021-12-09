package com.docotel.ki.repository.master;

import com.docotel.ki.model.master.MUserRole;
import com.docotel.ki.model.master.MWorkflowProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MUserRoleRepository extends JpaRepository<MUserRole, String> {
    // olly@docotel.com -- start
    @Query("SELECT mur FROM MUserRole mur WHERE mur.mUser.id = :userId AND mur.mRole.id = 'ADMINISTRATOR'")
    MUserRole findUserAdministrator(@Param("userId") String userId);
    // olly@docotel.com -- end

    // FIT-- start
    @Query("SELECT mur FROM MUserRole mur WHERE mur.mUser.id = :userId")
    MUserRole findUserRole(@Param("userId") String userId);
    // FIT-- end

}
