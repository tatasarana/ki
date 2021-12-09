package com.docotel.ki.repository.transaction;

import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.master.MWorkflowProcess;
import com.docotel.ki.model.transaction.TxMonitor;
import com.docotel.ki.model.transaction.TxTmGeneral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface TxMonitorRepository extends JpaRepository<TxMonitor, String> {

    List<TxMonitor> findTxMonitorByTxTmGeneralOrderByCreatedDateDesc(TxTmGeneral txTmGeneral);

    List<TxMonitor> findTxMonitorByDueDate(String dueDate);
    
    @Query("SELECT COUNT(wfac) FROM TxMonitor c LEFT JOIN c.mWorkflowProcessActions wfac WHERE c.mWorkflowProcessActions.id = :idWfActions")
    long countWfProcessAction(@Param("idWfActions") String idWfActions);
    
    @Query("SELECT COUNT(wfp) FROM TxMonitor c LEFT JOIN c.mWorkflowProcess wfp WHERE c.mWorkflowProcess.id = :idWfProcess")
    long countWfProcess(@Param("idWfProcess") String idWfProcess);
    
    List<TxMonitor> findTxMonitorByMWorkflowProcessActionsId(String id);
    
    @Query(value = "SELECT txm.MONITOR_ID FROM TX_MONITOR txm LEFT JOIN TX_TM_GENERAL tg ON tg.APPLICATION_ID = txm.APPLICATION_ID "
    		+ "WHERE txm.APPLICATION_ID IN (:appId) AND txm.WORKFLOW_PROCESS_ID = :wfProcessId", nativeQuery = true)
    String[] findAllTxMonitorByTxTmGeneralIdAndWfProcessId(@Param("appId") String[] appId, @Param("wfProcessId") MWorkflowProcess wfProcessId);
    
    @Query(value = "SELECT * FROM TX_MONITOR txm LEFT JOIN TX_TM_GENERAL tg ON tg.APPLICATION_ID = txm.APPLICATION_ID "
    		+ "WHERE txm.APPLICATION_ID IN (:appId) ORDER BY txm.CREATED_DATE DESC", nativeQuery = true)
    List<TxMonitor> findAllTxMonitorByTxTmGeneralId(@Param("appId") String[] appId);
    
    @Query("SELECT c FROM TxMonitor c WHERE c.mWorkflowProcessActions.id = :id AND TO_CHAR(c.createdDate, 'MM') = :month")
    List<TxMonitor> findTxMonitorByMWorkflowProcessActionsId(@Param("id") String id, @Param("month") String month);
    
    @Query("SELECT DISTINCT TO_CHAR(c.createdDate, 'YYYY') FROM TxMonitor c ORDER BY TO_CHAR(c.createdDate, 'YYYY')")
    List<Object[]> findTxMonitorByYear();

    @Query(value = "SELECT * FROM TX_MONITOR txm "
            + "WHERE txm.APPLICATION_ID = (:appId) AND txm.NOTES ='03. Permohonan Banding Merek/Merek Kolektif/lndikasi Geografis'", nativeQuery = true)
    TxMonitor findTxMonitorKBM03(@Param("appId") String appId);
    
    @Query(value = "SELECT TO_CHAR(tm.CREATED_DATE ,'YYYY/MM/DD'), tm.MONITOR_ID, mu.USERNAME, mu.EMAIL, "
    		+ "ttb.TM_BRAND_NAME, ttg.APPLICATION_NO "
    		+ "FROM MEREK.TX_MONITOR tm "
    		+ "JOIN TX_TM_GENERAL ttg ON ttg.APPLICATION_ID = tm.APPLICATION_ID "
    		+ "JOIN TX_TM_BRAND ttb ON ttb.APPLICATION_ID = tm.APPLICATION_ID "
    		+ "JOIN M_USER mu ON mu.USER_ID = ttg.CREATED_BY "
    		+ "WHERE tm.MONITOR_ID = :id", nativeQuery = true)
    List<Object[]> findByApplicationId(@Param("id") String id);
    
    @Query(value = "select DISTINCT mu.USER_ID, mu.USERNAME from TX_MONITOR tm "
    		+ "JOIN M_USER mu ON mu.USER_ID = tm.CREATED_BY "
    		+ "WHERE mu.USER_TYPE = 'Karyawan'", nativeQuery = true)
    List<Object[]> userPascaPermohonan();
}
