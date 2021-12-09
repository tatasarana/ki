package com.docotel.ki.scheduler.workflow;

import com.docotel.ki.enumeration.UserEnum;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.transaction.TxMonitor;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.repository.custom.transaction.TxMonitorCustomRepository;
import com.docotel.ki.repository.transaction.TxMonitorRepository;
import com.docotel.ki.repository.transaction.TxTmGeneralRepository;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class MonitoringTasks implements Job {
	private static boolean synchXmlOnProgress = false;
	
    @Autowired
    private TxMonitorCustomRepository txMonitorRepository;
    @Autowired
    private TxTmGeneralRepository txTmGeneralRepository;
    @Autowired private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    
    /*@Value("${scheduler.cron.expression.monitoring.job}")
	private String monitoringExpression;*/
    
    private SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");

    private static final Logger logger = LoggerFactory.getLogger(MonitoringTasks.class);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    //@Scheduled(cron = "0 0 8 * * ?")
    //@Scheduled(cron = monitoringExpression) //error The value for annotation attribute Scheduled.cron must be a constant expression
    public void runAutomaticStatusDaily() {
        logger.info("Current MonitoringTasks Thread : {}", Thread.currentThread().getName());
        logger.info("CRON :: Execution Time - {}", dateTimeFormatter.format(LocalDateTime.now()));
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date today = new Date();
        String todayWithZeroTime = formatter.format(today) + " 00:00:00.0";
        logger.info("using dueDate: {}", todayWithZeroTime);
        int processedMonitor = 0;
        try{
            //List<TxMonitor> listMonitor = txMonitorRepository.selectAll("dueDate", todayWithZeroTime, true, null, null);
            List<TxMonitor> listMonitor = txMonitorRepository.findByDueDate(todayWithZeroTime);
            if(listMonitor.size()>0){
                for(TxMonitor mtr: listMonitor){
                    if(mtr.getTxTmGeneral().getmStatus().getId().equalsIgnoreCase("TM_PUBLIKASI")
                      ||    mtr.getTxTmGeneral().getmStatus().getId().equalsIgnoreCase("924")
                        ||   mtr.getTxTmGeneral().getmStatus().getId().equalsIgnoreCase("124")
                            ) {

                        if (mtr.getmWorkflowProcessActions().getAction().getType().equalsIgnoreCase("Otomatis")
                                && mtr.getTargetWorkflowProcess().getId().equalsIgnoreCase("SELESAI_MASA_PENGUMUMAN")
                                && mtr.getmWorkflowProcessActions().getId().equalsIgnoreCase("TM_PUBLIKASI")) {

                            TxMonitor monitor = new TxMonitor();
                            monitor.setTxTmGeneral(mtr.getTxTmGeneral());
                            monitor.setmWorkflowProcess(mtr.getTargetWorkflowProcess());
                            monitor.setmWorkflowProcessActions(null);
                            monitor.setDueDate(null);
                            monitor.setTargetWorkflowProcess(null);
                            monitor.setFileUploadPath(null);
                            monitor.setCreatedBy((MUser) UserEnum.SYSTEM.value());
                            //                    monitor.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

                            TxTmGeneral ttg = mtr.getTxTmGeneral();
                            ttg.setmStatus(mtr.getTargetWorkflowProcess().getStatus());

                            txTmGeneralRepository.save(ttg);
                            txMonitorRepository.saveOrUpdate(monitor);

                            processedMonitor++;
                        }
                    }

                }

                synchXmlOnProgress = false;
            }else{

                synchXmlOnProgress = false;
            }
            logger.info(this.getClass().getSimpleName() + " JOB PROCESS DONE [listMonitor:"+processedMonitor+"]");
        } catch (Exception e) {
            logger.error(this.getClass().getSimpleName() + " JOB PROCESS ERROR - ", e);
            synchXmlOnProgress = false;
        }
    }

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		// TODO Auto-generated method stub
		if (synchXmlOnProgress) {
			logger.info("- MonitoringTasks PREVIOUS " + this.getClass().getSimpleName() + " PROCESS STILL ON GOING NOT EXECUTING JOB PROCESS -");
		} else {
			synchXmlOnProgress = true;
			logger.info("- " + this.getClass().getSimpleName() + " MonitoringTasks JOB PROCESS COPYING STARTING -");

			threadPoolTaskExecutor.execute(() -> {
				runAutomaticStatusDaily();
			} ) ;
		}
	}


}
