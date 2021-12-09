package com.docotel.ki.repository.master;

import com.docotel.ki.model.master.MWorkflowProcess;
import com.docotel.ki.model.master.MWorkflowProcessActions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MWorkflowProcessActionRepository extends JpaRepository<MWorkflowProcessActions, String> {

    @Query(value="SELECT a FROM MWorkflowProcessActions a " +
            "LEFT JOIN FETCH a.createdBy cb " +
            "LEFT JOIN FETCH a.action ma " +
            "LEFT JOIN FETCH ma.document md " +
            "WHERE a.process = :process AND a.action.statusFlag=true ORDER BY ma.name")
    List<MWorkflowProcessActions> findMWorkflowProcessActionsByProcess(@Param("process") MWorkflowProcess process);
    
    @Query(value="SELECT a FROM MWorkflowProcessActions a " +
            "LEFT JOIN FETCH a.action ma " +
            "WHERE ma.id = :actionId AND a.process = :process")
    List<MWorkflowProcessActions> findMWorkflowProcessActionsByActionId(@Param("actionId") String actionId, @Param("process") MWorkflowProcess process);

    @Query(value="SELECT a FROM MWorkflowProcessActions a " +
            "LEFT JOIN FETCH a.createdBy cb " +
            "WHERE a.id = :id")
    MWorkflowProcessActions findMWorkflowProcessActionsById(@Param("id") String id);
    
    @Query(value="SELECT a FROM MWorkflowProcessActions a " +
            "LEFT JOIN FETCH a.createdBy cb " +
            "LEFT JOIN FETCH a.action ma " +
            "WHERE a.process = :process order by a.createdDate DESC ")
    List<MWorkflowProcessActions> findMWorkflowProcessActionsByWfProcess(@Param("process") MWorkflowProcess process);
}
