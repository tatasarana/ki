package com.docotel.ki.repository.master;
 
import com.docotel.ki.model.master.MWorkflow;
import com.docotel.ki.model.master.MWorkflowProcess; 
import org.springframework.data.jpa.repository.JpaRepository; 
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MWorkflowProcessRepository extends JpaRepository<MWorkflowProcess, String> {

    MWorkflowProcess findMWorkflowProcessByName(String name);
    List<MWorkflowProcess> findMWorkflowProcessesByWorkflow(MWorkflow workflow);
    List<MWorkflowProcess> findMWorkflowProcessesByWorkflowIdOrderByOrders(String workflowId);
    List<MWorkflowProcess> findMWorkflowProcessesByWorkflowAndStatusFlagOrderByOrders(MWorkflow workflow, boolean statusFlag);
   
    @Query(value="SELECT mwfp FROM MWorkflowProcess mwfp "
    		+ "WHERE mwfp.workflow =:workflow "
    		+ "AND statusFlag=:statusFlag ORDER BY mwfp.status.name")
    List<MWorkflowProcess> findMWorkflowProcessesByWorkflowAndStatusFlag(@Param("workflow")MWorkflow workflow,@Param("statusFlag") boolean statusFlag);
      
    
}
