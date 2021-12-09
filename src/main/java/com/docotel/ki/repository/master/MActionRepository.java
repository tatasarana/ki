package com.docotel.ki.repository.master;

import com.docotel.ki.model.master.MAction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MActionRepository extends JpaRepository<MAction, String> {

    MAction findMActionByCode(String code);
    MAction findMActionByName(String name);
    List<MAction> findMActionByStatusFlag(boolean statusFlag);
    
    @Query(value="SELECT a FROM MAction a " +
            "WHERE a.id = :id")
    MAction findMWorkflowProcessActionsByIdAction(@Param("id") String id);
}
