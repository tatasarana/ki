package com.docotel.ki.service.transaction;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.docotel.ki.model.counter.TcRegistrationNo;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.master.MWorkflowProcess;
import com.docotel.ki.model.master.MWorkflowProcessActions;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.master.MWorkflowProcessActionCustomRepository;
import com.docotel.ki.repository.custom.transaction.*;

import com.docotel.ki.repository.master.MWorkflowProcessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.docotel.ki.enumeration.StatusEnum;
import com.docotel.ki.repository.custom.counter.TcRegistrationNoCustomRepository;
import com.docotel.ki.repository.transaction.TxRegistrationDetailRepository;
import com.docotel.ki.repository.transaction.TxRegistrationRepository;

@Service
public class RegistrationService {
	@Autowired
	private TcRegistrationNoCustomRepository tcRegistrationNoCustomRepository;
	@Autowired
	private TxRegistrationRepository txRegistrationRepository;
	@Autowired
	private TxRegistrationCustomRepository txRegistrationCustomRepository;
	@Autowired
	private TxRegistrationDetailRepository txRegistrationDetailRepository;
	@Autowired
	private TxRegistrationDetailCustomRepository txRegistrationDetailCustomRepository;
	@Autowired
	private TxTmGeneralCustomRepository txTmGeneralCustomRepository;
	@Autowired
	private MWorkflowProcessRepository mWorkflowProcessRepository;
	@Autowired
	private MWorkflowProcessActionCustomRepository mWorkflowProcessActionCustomRepository;
	@Autowired
	private TxMonitorCustomRepository txMonitorCustomRepository;
	@Autowired private TxGroupDetailCustomRepository txGroupDetailCustomRepository;

	@Value("${download.output.certificate.file.path:}")
    private String downloadOutputCertificatePath;

	private static final String GENERATE_SERTIFIKAT = "GENERATE_SERTIFIKAT";
    
    /***************************** - METHOD SECTION - ****************************/
	@Transactional
	public void insertRegistration(TxTmGeneral txTmGeneral, Timestamp start_date, Timestamp end_date) {
	    TcRegistrationNo tcRegistrationNo = tcRegistrationNoCustomRepository.findOneBy(null, null);
		if (tcRegistrationNo == null) {
			tcRegistrationNo = new TcRegistrationNo();
		}
		tcRegistrationNo.increaseSequence();
		tcRegistrationNoCustomRepository.saveOrUpdate(tcRegistrationNo);

		MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		TxRegistration txRegistration = new TxRegistration();
		txRegistration.setTxTmGeneral(txTmGeneral);
		txRegistration.setNo(tcRegistrationNo.toString());
		txRegistration.setStartDate(new Timestamp(System.currentTimeMillis()));
		txRegistration.setEndDate(end_date);
		txRegistration.setDownloadFlag(false);
		txRegistration.setStatus(true);
		txRegistration.setCreatedBy(mUser);
		txRegistrationRepository.save(txRegistration);
		
		TxRegistrationDetail txRegistrationDetail = new TxRegistrationDetail();
		txRegistrationDetail.setTxRegistration(txRegistration);
		txRegistrationDetail.setStartDate(new Timestamp(System.currentTimeMillis()));
		txRegistrationDetail.setEndDate(end_date);
		txRegistrationDetail.setDownloadFlag(false);
		txRegistrationDetail.setStatus(true);
		txRegistrationDetail.setCreatedBy(mUser);
		txRegistrationDetailRepository.save(txRegistrationDetail);
		
		txTmGeneral.setTxRegistration(txRegistration);

		MWorkflowProcess mWorkflowProcess = mWorkflowProcessRepository.findOne(GENERATE_SERTIFIKAT);
		MWorkflowProcessActions mWorkflowProcessActions = mWorkflowProcessActionCustomRepository.selectOne("process", mWorkflowProcess, true);

		TxMonitor txMonitor = new TxMonitor();
		txMonitor.setTxPostReception(null);
		txMonitor.setTxTmGeneral(txTmGeneral);
		txMonitor.setmWorkflowProcess(mWorkflowProcess);
		txMonitor.setmWorkflowProcessActions(mWorkflowProcessActions);
		txMonitor.setCreatedDate(txMonitor.getCreatedDate());
		txMonitor.setCreatedBy(mUser);
		txMonitorCustomRepository.saveOrUpdate(txMonitor);

		TxTmGeneral updateTxTmGeneral = txTmGeneralCustomRepository.selectOne("id", txTmGeneral.getId());
		updateTxTmGeneral.setmStatus(mWorkflowProcess.getStatus());
		updateTxTmGeneral.setmAction(mWorkflowProcessActions.getAction());
		txTmGeneralCustomRepository.saveOrUpdate(updateTxTmGeneral);
	}
	
	@Transactional
	public void extendRegistration(TxTmGeneral txTmGeneral, Timestamp start_date, Timestamp end_date) {
		TxRegistrationDetail txRegistrationDetailOld = txRegistrationDetailCustomRepository.selectOne(
				"LEFT JOIN FETCH c.txRegistration", "txRegistration.id", txTmGeneral.getTxRegistration().getId(), true);
		
		txRegistrationDetailOld.setStatus(false);
		txRegistrationDetailCustomRepository.saveOrUpdate(txRegistrationDetailOld);
				
		TxRegistration txRegistration = txRegistrationDetailOld.getTxRegistration();
		txRegistration.setStartDate(new Timestamp(System.currentTimeMillis()));
		txRegistration.setEndDate(end_date);
		txRegistration.setStatus(true);
		txRegistration.setDownloadFlag(false);
		txRegistration.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		txRegistration.setUpdatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		txRegistrationCustomRepository.saveOrUpdate(txRegistration);
		
		TxRegistrationDetail txRegistrationDetailNew = new TxRegistrationDetail();
		txRegistrationDetailNew.setTxRegistration(txRegistration);
		txRegistrationDetailNew.setStartDate(new Timestamp(System.currentTimeMillis()));
		txRegistrationDetailNew.setEndDate(end_date);
		txRegistrationDetailNew.setDownloadFlag(false);
		txRegistrationDetailNew.setStatus(true);
		txRegistrationDetailNew.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		txRegistrationDetailRepository.save(txRegistrationDetailNew);		
		
		txTmGeneral.setTxRegistration(txRegistration);
	}
	
	@Transactional
	public void removeRegistration(TxTmGeneral txTmGeneral) throws IOException {
		List<TxRegistrationDetail> txRegistrationDetailList = txRegistrationDetailCustomRepository.selectAll(
				"LEFT JOIN FETCH c.txRegistration", "txRegistration.id", txTmGeneral.getTxRegistration().getId(), true, "startDate", "DESC", null, null);
		
		TxRegistration txRegistration = null;

		MWorkflowProcess mWorkflowProcess = mWorkflowProcessRepository.findOne(GENERATE_SERTIFIKAT);

		List<KeyValue> searchCriteria = new ArrayList<>();
		searchCriteria.add(new KeyValue("txTmGeneral", txTmGeneral, true));
		searchCriteria.add(new KeyValue("mWorkflowProcess", mWorkflowProcess, true));

		int count = 0;
		for (TxRegistrationDetail txRegistrationDetail : txRegistrationDetailList) {
			count++;
			if (count == 1) {
				txRegistration = txRegistrationDetail.getTxRegistration();
				txRegistrationDetailRepository.delete(txRegistrationDetail);
				//Delete File Sertifikat
//				String folder = downloadOutputCertificatePath + DateUtil.formatDate(txRegistrationDetail.getCreatedDate(), "yyyy/MM/");
//		        String fileName = txRegistration.getNo() + ".pdf";
//		        Path pathFile = Paths.get(folder + fileName);
//		        Files.deleteIfExists(pathFile);
		    } else if (count == 2) {
				txRegistrationDetail.setStatus(true);
				txRegistrationDetailCustomRepository.saveOrUpdate(txRegistrationDetail);
				
				txRegistration = txRegistrationDetail.getTxRegistration();
				txRegistration.setStartDate(txRegistrationDetail.getStartDate());
				txRegistration.setEndDate(txRegistrationDetail.getEndDate());
				txRegistration.setStatus(txRegistrationDetail.isStatus());
				txRegistration.setDownloadFlag(txRegistrationDetail.isDownloadFlag());
				txRegistrationCustomRepository.saveOrUpdate(txRegistration);
				break;
			}
		}
		
		if (count == 1) {
			txRegistrationRepository.delete(txRegistration);
			txTmGeneral.setTxRegistration(null);
		} else {
			txTmGeneral.setTxRegistration(txRegistration);
		}
		
		String txTmGeneralId = txTmGeneral.getId();

		TxMonitor txMonitor = txMonitorCustomRepository.selectOne(searchCriteria, null, null);
		if (txMonitor != null) {
			txMonitorCustomRepository.delete(txMonitor);
		}

		if (count > 1) {
			TxMonitor prevTxMonitor = txMonitorCustomRepository.selectOne("txTmGeneral.id", txTmGeneralId, true, "createdDate", "DESC");
			if (prevTxMonitor != null) {
				TxTmGeneral updateTxTmGeneral = txTmGeneralCustomRepository.selectOne("id", txTmGeneralId);
				updateTxTmGeneral.setmStatus(prevTxMonitor.getmWorkflowProcess().getStatus());
				if (prevTxMonitor.getmWorkflowProcessActions() != null) {
					updateTxTmGeneral.setmAction(prevTxMonitor.getmWorkflowProcessActions().getAction());
					//            System.out.println("test fit masuk if updateTxTmGeneral "+ updateTxTmGeneral);
				} else {
					updateTxTmGeneral.setmAction(null);
				}
				txTmGeneralCustomRepository.saveOrUpdate(updateTxTmGeneral);
			}
		}
	}
	
	@Transactional
	public void doUpdateRegistration(TxRegistration txRegistration) {  
		
		List<TxRegistrationDetail> txRegistrationDetailList = txRegistrationDetailCustomRepository.selectAllRegistrationDetail(txRegistration);
		 
		
		for (TxRegistrationDetail txRegistrationDetail : txRegistrationDetailList) { 
		
		    
		    if(txRegistrationDetailList.indexOf(txRegistrationDetail)==0) { 
				Timestamp startDate=txRegistration.getStartDate();
				Timestamp expDate=txRegistration.getEndDate();
 
				txRegistrationDetail.setStartDate(startDate);  
				txRegistrationDetail.setEndDate(expDate);  
				
		    	
		    }else { 
		    	Timestamp startDate=txRegistration.getStartDate();
		    	LocalDate startDateLocal=startDate.toLocalDateTime().toLocalDate();
		    	
		    	Timestamp expDate=txRegistration.getEndDate();
		    	LocalDate expDateLocal=expDate.toLocalDateTime().toLocalDate(); 
                
		    	LocalDate prevStartDate = startDateLocal.minusYears(txRegistrationDetailList.indexOf(txRegistrationDetail)*10);
		    	LocalDate prevExpDate = expDateLocal.minusYears(txRegistrationDetailList.indexOf(txRegistrationDetail)*10);
		    	
		    	txRegistrationDetail.setStartDate(Timestamp.valueOf(prevStartDate.atStartOfDay()));
		        txRegistrationDetail.setEndDate(Timestamp.valueOf(prevExpDate.atStartOfDay()));
		    }
			txRegistrationDetailCustomRepository.saveOrUpdate(txRegistrationDetail); 
		}
		txRegistrationCustomRepository.saveOrUpdate(txRegistration);
	}

	public TxRegistrationDetail selectDetailOne(Object value) {
		List<KeyValue> searchCriteria = new ArrayList<>();
		searchCriteria.add(new KeyValue("txRegistration.txTmGeneral.id", value, true));
		searchCriteria.add(new KeyValue("status", true, true));
		return txRegistrationDetailCustomRepository.selectOne("LEFT JOIN c.txRegistration", searchCriteria, null, null);
	}
	
	public long countDetail(String by, String value) {
		return txRegistrationDetailCustomRepository.count(by, value, true);
	}
	
	public TxRegistration selectOne(Object value) {
		return txRegistrationCustomRepository.selectOne("txTmGeneral.id", value, true);
	}
	
	public TxRegistration selectOneBy(String by, Object value) {
		return txRegistrationCustomRepository.selectOne(by, value, true);
	}
	
	public GenericSearchWrapper<TxRegistrationDetail> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<TxRegistrationDetail> searchResult = new GenericSearchWrapper<TxRegistrationDetail>();
		searchResult.setCount(txRegistrationDetailCustomRepository.count(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(txRegistrationDetailCustomRepository.selectAll(
					"LEFT JOIN c.txRegistration tx " +
					"LEFT JOIN tx.txTmGeneral tm ", searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}

	public GenericSearchWrapper<TxRegistration> selectAllSertifikatDigital(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<TxRegistration> searchResult = new GenericSearchWrapper<>();
		searchResult.setCount(txRegistrationCustomRepository.countSertifikatDigital(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(txRegistrationCustomRepository.selectAllSertifikatDigital(
					"LEFT JOIN c.txTmGeneral tm", searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}

	public GenericSearchWrapper<TxGroupDetail> selectAllPublikasi(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<TxGroupDetail> searchResult = new GenericSearchWrapper<>();
		searchResult.setCount(txGroupDetailCustomRepository.count(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(txGroupDetailCustomRepository.selectAll(
					"LEFT JOIN c.txTmGeneral tm", searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}

	public TxRegistration selectJoinReception(String txtmgeneralId) {
		return txRegistrationCustomRepository.selectOne("LEFT JOIN txTmGeneral ttg LEFT JOIN txReception tr","txTmGeneral.id", txtmgeneralId, true);
	}
	
	@Transactional
    public void batchCertificateExpireJob() {
		List<TxRegistrationDetail> txRegistrationDetailList = txRegistrationDetailCustomRepository.selectAllExpireRegistration(Timestamp.valueOf(LocalDate.now().atStartOfDay()));
		for (TxRegistrationDetail txRegistrationDetail : txRegistrationDetailList) {
			txRegistrationDetail.setStatus(false);
			txRegistrationDetailCustomRepository.saveOrUpdate(txRegistrationDetail);
			
			TxRegistration txRegistration = txRegistrationDetail.getTxRegistration();
			txRegistration.setStatus(false);
			txRegistrationCustomRepository.saveOrUpdate(txRegistration);
			
			TxTmGeneral txTmGeneral = txRegistration.getTxTmGeneral();
			txTmGeneral.setmStatus(StatusEnum.EXPIRED.value());
			txTmGeneralCustomRepository.saveOrUpdate(txTmGeneral);
		}
	}
	
	@Transactional
	public void insertRegistrationManual(TxTmGeneral txTmGeneral, String registrationNo, Timestamp start_date, Timestamp end_date) {
 

		MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		TxRegistration txRegistration = new TxRegistration();
		txRegistration.setTxTmGeneral(txTmGeneral);
		txRegistration.setNo(registrationNo);
		txRegistration.setStartDate(new Timestamp(System.currentTimeMillis()));
		txRegistration.setEndDate(end_date);
		txRegistration.setDownloadFlag(false);
		txRegistration.setStatus(true);
		txRegistration.setCreatedBy(mUser);
		txRegistrationRepository.save(txRegistration);
		
		TxRegistrationDetail txRegistrationDetail = new TxRegistrationDetail();
		txRegistrationDetail.setTxRegistration(txRegistration);
		txRegistrationDetail.setStartDate(new Timestamp(System.currentTimeMillis()));
		txRegistrationDetail.setEndDate(end_date);
		txRegistrationDetail.setDownloadFlag(false);
		txRegistrationDetail.setStatus(true);
		txRegistrationDetail.setCreatedBy(mUser);
		txRegistrationDetailRepository.save(txRegistrationDetail);
		
		txTmGeneral.setTxRegistration(txRegistration);

		MWorkflowProcess mWorkflowProcess = mWorkflowProcessRepository.findOne(GENERATE_SERTIFIKAT);
		MWorkflowProcessActions mWorkflowProcessActions = mWorkflowProcessActionCustomRepository.selectOne("process", mWorkflowProcess, true);

		TxMonitor txMonitor = new TxMonitor();
		txMonitor.setTxPostReception(null);
		txMonitor.setTxTmGeneral(txTmGeneral);
		txMonitor.setmWorkflowProcess(mWorkflowProcess);
		txMonitor.setmWorkflowProcessActions(mWorkflowProcessActions);
		txMonitor.setCreatedDate(txMonitor.getCreatedDate());
		txMonitor.setCreatedBy(mUser);
		txMonitorCustomRepository.saveOrUpdate(txMonitor);

		TxTmGeneral updateTxTmGeneral = txTmGeneralCustomRepository.selectOne("id", txTmGeneral.getId());
		updateTxTmGeneral.setmStatus(mWorkflowProcess.getStatus());
		updateTxTmGeneral.setmAction(mWorkflowProcessActions.getAction());
		txTmGeneralCustomRepository.saveOrUpdate(updateTxTmGeneral);
	}
}
