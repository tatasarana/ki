package com.docotel.ki.service.transaction;

import com.docotel.ki.KiApplication;
import com.docotel.ki.enumeration.StatusEnum;
import com.docotel.ki.enumeration.WfProcessEnum;
import com.docotel.ki.model.counter.TcApplicationNo;
import com.docotel.ki.model.counter.TcFilingNo;
import com.docotel.ki.model.counter.TcPostReceiptNo;
import com.docotel.ki.model.master.*;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.pojo.*;
import com.docotel.ki.repository.custom.counter.TcApplicationNoCustomRepository;
import com.docotel.ki.repository.custom.counter.TcFilingNoCustomRepository;
import com.docotel.ki.repository.custom.counter.TcPostReceiptNoCustomRepository;
import com.docotel.ki.repository.custom.master.MClassDetailCustomRepository;
import com.docotel.ki.repository.custom.master.MCountryCostumRepository;
import com.docotel.ki.repository.custom.master.MWorkflowProcessActionCustomRepository;
import com.docotel.ki.repository.custom.master.MWorkflowProcessCustomRepository;
import com.docotel.ki.repository.custom.pojo.CetakMerekRepository;
import com.docotel.ki.repository.custom.pojo.DataBRMRepository;
import com.docotel.ki.repository.custom.transaction.*;
import com.docotel.ki.repository.master.MFileSeqRepository;
import com.docotel.ki.repository.master.MFileTypeRepository;
import com.docotel.ki.repository.master.MLawRepository;
import com.docotel.ki.repository.master.MWorkflowProcessActionRepository;
import com.docotel.ki.repository.transaction.*;
import com.docotel.ki.service.master.BrandService;
import com.docotel.ki.service.master.ClassService;
import com.docotel.ki.service.master.LookupService;
import com.docotel.ki.util.DateUtil;
import org.apache.catalina.core.ApplicationServletRegistration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;

@Service
public class TrademarkService {
	public static final String PATH_TAMBAH_PUBLIKASI = "/publikasi-buat";

	/***************************** - AUTO INJECT SECTION - ****************************/
	@Autowired private TxPostReceptionCustomRepository txPostReceptionCustomRepository;
	@Autowired private TxPostReceptionRepository txPostReceptionRepository;
	@Autowired private TxPostReceptionDetailRepository txPostReceptionDetailRepository;
	@Autowired private TxPostReceptionDetailCustomRepository txPostReceptionDetailCustomRepository;
	@Autowired private TxPostOwnerCustomRepository txPostOwnerCustomRepository;	
	@Autowired private TxPostDocCustomRepository txPostDocCustomRepository;
	@Autowired private TxTmDocCustomRepository txTmDocCustomRepository;
	@Autowired private TxPostRepresentativeCustomRepository txPostRepresentativeCustomRepository;
	@Autowired private TcApplicationNoCustomRepository tcApplicationNoCustomRepository;
	@Autowired private TcFilingNoCustomRepository tcFilingNoCustomRepository;
	@Autowired private TxReceptionCustomRepository txReceptionCustomRepository;
	@Autowired private TxReceptionRepository txReceptionRepository;
	@Autowired private MFileTypeRepository mFileTypeRepository;
	@Autowired private MFileSeqRepository mFileSeqRepository;
	@Autowired private TxTmGeneralRepository txTmGeneralRepository;
	@Autowired private TxPubsJournalRepository txPubsJournalRepository;
	@Autowired private TxServDistRepository txServDistRepository;
	@Autowired private TxPubsJournalCustomRepository txPubsJournalCustomRepository;
	@Autowired private TxTmGeneralCustomRepository txTmGeneralCustomRepository;
	@Autowired private TxGroupDetailCustomRepository txGroupDetailCustomRepository;
	@Autowired private DataBRMRepository dataBRMRepository;
	@Autowired private CetakMerekRepository cetakmerekRepository;
	@Autowired private MLawRepository mLawRepository;
	@Autowired private TcPostReceiptNoCustomRepository tcPostReceiptNoCustomRepository;
	@Autowired private TxTmOwnerRepository txTmOwnerRepository;
	@Autowired private TxRegistrationCustomRepository txRegistrationCustomRepository;
	@Autowired private TxRegistrationRepository txRegistrationRepository;
	@Autowired private TxMonitorRepository txMonitorRepository;
	@Autowired private TxMonitorCustomRepository txMonitorCustomRepository;
	@Autowired private MWorkflowProcessCustomRepository mWorkflowProcessCustomRepository;
	@Autowired private MWorkflowProcessActionCustomRepository mWorkflowProcessActionCustomRepository;
	@Autowired private MWorkflowProcessActionRepository mWorkflowProcessActionRepository;
	@Autowired private GrupPermohonanService groupService;
	@Autowired private TxTmOwnerCustomRepository txTmOwnerCustomRepository;
	@Autowired private MCountryCostumRepository mCountryCostumRepository;
	@Autowired private TxTmOwnerDetailCustomRepository txTmOwnerDetailCustomRepository;
	@Autowired private TxTmReprsCustomRepository txTmReprsCustomRepository;
	@Autowired private MrepresentativeCustomRepository mrepresentativeCustomRepository;
	@Autowired private ReprsService reprsService; 
	@Autowired private BrandService brandService;
	@Autowired private PriorService priorService;
	@Autowired private ClassService classService;
	@Autowired private MonitorService monitorService;	
	@Autowired MClassDetailCustomRepository mClassDetailCustomRepository;
	@Autowired TxTmBrandCustomRepository txTmBrandCustomRepository;
	@Autowired
    TxTmPriorCustomRepository txTmPriorCustomRepository;
	@Autowired
    TxTmClassCustomRepository txTmClassCustomRepository;
	@Autowired TxTmClassRepository txTmClassRepository;
	@Autowired
    LookupService lookupService;
	@Autowired private TxTmReferenceRepository txTmReferenceRepository;
	@Autowired private TxTmReferenceCustomRepository txTmReferenceCustomRepository;
	
	/***************************** - METHOD SECTION - ****************************/
	@Transactional
	public void insertReception(TxReception txReception) {
		// check File Sequence
		MFileSequence mFileSequence = mFileSeqRepository.findOne(txReception.getmFileSequence().getCurrentId());
		if (mFileSequence == null || !mFileSequence.isStatusFlag()) {
			throw new DataIntegrityViolationException(HttpStatus.NOT_FOUND.getReasonPhrase() + "-MFileSequence");
		}
		txReception.setmFileSequence(mFileSequence);

		// check File Type
		MFileType mFileType = mFileTypeRepository.findOne(txReception.getmFileType().getId());
		if (mFileType == null || !mFileType.isStatusFlag()) {
			throw new DataIntegrityViolationException(HttpStatus.NOT_FOUND.getReasonPhrase() + "-MFileType");
		}

		txReception.setmFileType(mFileType);
		txReception.setmWorkflow(mFileType.getmWorkflow());

		// generate application no
		Map<String, Object> findCriteria = new HashMap<>();
		int currentYear = Calendar.getInstance().get(Calendar.YEAR);
		findCriteria.put("year", currentYear);

		TcApplicationNo tcApplicationNo = tcApplicationNoCustomRepository.findOneBy(findCriteria, null);
		if (tcApplicationNo == null) {
			tcApplicationNo = new TcApplicationNo();
			tcApplicationNo.setYear(currentYear);
		}
		tcApplicationNo.setmFileSequence(txReception.getmFileSequence());
		tcApplicationNo.setmFileType(txReception.getmFileType());
		tcApplicationNo.increaseSequence();
		tcApplicationNoCustomRepository.saveOrUpdate(tcApplicationNo);

		txReception.setApplicationNo(tcApplicationNo.toString());
		txReception.setOnlineFlag(false);
		txReceptionRepository.save(txReception);

		// insert to general
		TxTmGeneral txTmGeneral = new TxTmGeneral();
		txTmGeneral.setTxReception(txReception);
		txTmGeneral.setApplicationNo(txReception.getApplicationNo());
		txTmGeneral.setFilingDate(txReception.getCreatedDate());
		
		txTmGeneral.setmStatus(StatusEnum.TM_PERMOHONAN_BARU.value());
		txTmGeneral.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		txTmGeneral.setCreatedDate(txReception.getCreatedDate());

		txTmGeneralRepository.save(txTmGeneral);

		TxTmOwner txTmOwner = txReception.getTxTmOwner();
		if (txTmOwner != null) {
			if (StringUtils.isBlank(txTmOwner.getName()) && StringUtils.isBlank(txTmOwner.getAddress()) && StringUtils.isBlank(txTmOwner.getPhone()) && StringUtils.isBlank(txTmOwner.getmCountry().getCurrentId())) {
				txTmOwner = null;
			}
		}
		if (txTmOwner != null) {
			txTmOwner.setTxTmGeneral(txTmGeneral);
			txTmOwner.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
			txTmOwner.setCreatedDate(txReception.getCreatedDate());

			if (StringUtils.isBlank(txTmOwner.getName())) {
				txTmOwner.setName("-");
			}
			if (StringUtils.isBlank(txTmOwner.getAddress())) {
				txTmOwner.setAddress("-");
			}
			if (StringUtils.isBlank(txTmOwner.getPhone())) {
				txTmOwner.setPhone("-");
			}
			if (StringUtils.isBlank(txTmOwner.getmCountry().getCurrentId())) {
				txTmOwner.setmCountry(KiApplication.INDONESIA);
			}
			txTmOwner.setNationality(txTmOwner.getmCountry());
			if (StringUtils.isBlank(txTmOwner.getmProvince().getCurrentId())) {
				txTmOwner.setmProvince(null);
			}
			if (StringUtils.isBlank(txTmOwner.getmCity().getCurrentId())) {
				txTmOwner.setmCity(null);
			}
			
			txTmOwnerRepository.save(txTmOwner);
		}
	}
	
	@Transactional
	public void updateReceptionMadrid(TxReception txReception, MadridDetailInfo madridDetailInfo) {
		txReceptionCustomRepository.saveOrUpdate(txReception);
	}

	@Transactional
	public void insertReception(TxPostReception txPostReception) {
		// check File Sequence
		MFileSequence mFileSequence = mFileSeqRepository.findOne(txPostReception.getmFileSequence().getCurrentId());
		if (mFileSequence == null || !mFileSequence.isStatusFlag()) {
			throw new DataIntegrityViolationException(HttpStatus.NOT_FOUND.getReasonPhrase() + "-MFileSequence");
		}
		txPostReception.setmFileSequence(mFileSequence);

		// check File Type
		MFileType mFileType = mFileTypeRepository.findOne(txPostReception.getmFileType().getId());
		if (mFileType == null || !mFileType.isStatusFlag()) {
			throw new DataIntegrityViolationException(HttpStatus.NOT_FOUND.getReasonPhrase() + "-MFileType");
		}

		txPostReception.setmFileType(mFileType);
//		txPostReception.setmWorkflow(mFileType.getmWorkflow());

		// set default status penerimaan
		txPostReception.setmStatus(StatusEnum.TM_PASCA_PERMOHONAN.value());

		// generate application no
		Map<String, Object> findCriteria = new HashMap<>();
		int currentYear = Calendar.getInstance().get(Calendar.YEAR);
		findCriteria.put("year", currentYear);

		TcApplicationNo tcApplicationNo = tcApplicationNoCustomRepository.findOneBy(findCriteria, null);
		if (tcApplicationNo == null) {
			tcApplicationNo = new TcApplicationNo();
			tcApplicationNo.setYear(currentYear);
		}
		tcApplicationNo.setmFileSequence(txPostReception.getmFileSequence());
		tcApplicationNo.setmFileType(txPostReception.getmFileType());
		tcApplicationNo.increaseSequence();
		tcApplicationNoCustomRepository.saveOrUpdate(tcApplicationNo);

		TcPostReceiptNo tcPostReceiptNo = tcPostReceiptNoCustomRepository.findOneBy(findCriteria, null);
		if (tcPostReceiptNo == null) {
			tcPostReceiptNo = new TcPostReceiptNo();
			tcPostReceiptNo.setYear(currentYear);
		}
		tcPostReceiptNo.setmFileType(mFileTypeRepository.findOne(txPostReception.getmFileType().getId()));
		tcPostReceiptNo.increaseSequence();
		tcPostReceiptNoCustomRepository.saveOrUpdate(tcPostReceiptNo);

		//txPostReception.setPostReceiptNo(tcPostReceiptNo.toString());
		txPostReception.setOnlineFlag(false);
		txPostReception.setmFileTypeDetail(null);
		txPostReceptionRepository.save(txPostReception);

		//insert txPostReceptionDetail
		TxPostReceptionDetail txPostReceptionDetail = new TxPostReceptionDetail();
		//txPostReceptionDetail.setAppNo(txPostReception.getTxTmGeneralTemp());
		//txPostReceptionDetail.setRegNo(txPostReception.getRegNoTemp());
		txPostReceptionDetail.setTxPostReception(txPostReception);
		txPostReceptionDetailRepository.save(txPostReceptionDetail);
	}

	/***************************** - METHOD SECTION - ****************************/
	@Transactional
	public String insertReceptionOnline(DataForm1 data) {

		String application_no = "";
		
		try {
			if (data.getAppid() == null || data.getAppid().equals("")) {
				// set default status penerimaan
				TxReception txReception = new TxReception();
				txReception.setBankCode(data.getBankCode());
				txReception.setApplicationDate(DateUtil.toDate("dd/MM/yyyy HH:mm:ss", data.getApplicationDate()));
				txReception.setPaymentDate(DateUtil.toDate("dd/MM/yyyy HH:mm:ss", data.getPaymentDate()));
				txReception.setTotalPayment(data.getTotalPayment());
				txReception.setTotalClass(data.getTotalClass());
				//txReception.setmFileSequence(data.getmFileSequence());
				//txReception.setmFileType(data.getmFileType());
				txReception.setmFileTypeDetail(data.getmFileTypeDetail());
				txReception.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
				txReception.setApplicantNotes(data.getApplicantNotes());

				// check File Sequence
				MFileSequence mFileSequence = mFileSeqRepository.findOne(data.getmFileSequence().getId());
				if (mFileSequence == null || !mFileSequence.isStatusFlag()) {
					throw new DataIntegrityViolationException(HttpStatus.NOT_FOUND.getReasonPhrase() + "-MFileSequence");
				}
				txReception.setmFileSequence(mFileSequence);

				// check File Type
				MFileType mFileType = mFileTypeRepository.findOne(data.getmFileType().getId());
				if (mFileType == null || !mFileType.isStatusFlag()) {
					throw new DataIntegrityViolationException(HttpStatus.NOT_FOUND.getReasonPhrase() + "-MFileType");
				}

				txReception.setmFileType(mFileType);
				txReception.setmWorkflow(mFileType.getmWorkflow());
				
				MLaw mLaw = null;
				if(data.getLaw() != null) {
					mLaw = mLawRepository.findOne(data.getLaw());
				}
				

				// generate application no
				Map<String, Object> findCriteria = new HashMap<>();
				int currentYear = Calendar.getInstance().get(Calendar.YEAR);

				findCriteria.put("year", currentYear);

				TcFilingNo tcFilingNo = tcFilingNoCustomRepository.findOneBy(findCriteria, null);

				if (tcFilingNo == null) {
					tcFilingNo = new TcFilingNo();
				}
				tcFilingNo.setYear(currentYear);
				tcFilingNo.increaseSequence();
				tcFilingNoCustomRepository.saveOrUpdate(tcFilingNo);
				txReception.setApplicationNo(tcFilingNo.toString());
				txReception.seteFilingNo(tcFilingNo.toString());
				txReception.setOnlineFlag(true);
				txReceptionRepository.save(txReception);

				//save into txReceptionOnline
				TxTmGeneral txTmGeneral = new TxTmGeneral();
				txTmGeneral.setApplicationNo(txReception.getApplicationNo());
				txTmGeneral.setTxReception(txReception);
				txTmGeneral.setmStatus(StatusEnum.IPT_DRAFT.value());
				txTmGeneral.setFilingDate(txReception.getCreatedDate());		
				txTmGeneral.setmLaw(mLaw);
				txTmGeneral.setLanguage1(data.getmLanguage());

				txTmGeneral.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
				txTmGeneral.setCreatedDate(txReception.getCreatedDate());
				txTmGeneralRepository.save(txTmGeneral);

				if(data.getReceptionIds()!=null && data.getReceptionIds().length > 0) { 
					for(String receptionId : data.getReceptionIds()) {
						TxReception txReception2 = selectOneReceptionByApplicationNo(receptionId);
						TxTmReference txTmReference = txTmReferenceRepository.getReffByTxTmGeneralIdAndEFilingNo(txTmGeneral.getId(), txReception2.geteFilingNo());
						if(txTmReference == null) { 
							TxRegistration txRegistration = txRegistrationRepository.findOne(receptionId);
							//txTmReference = new TxTmReference(txTmGeneral, txRegistration);
							TxTmGeneral reftxTmGeneral = txTmGeneralCustomRepository.select("id",txReception2.geteFilingNo());

							txTmReference = new TxTmReference(txTmGeneral, reftxTmGeneral);
							txTmReferenceRepository.save(txTmReference);
						}
					}
				}

				application_no = txTmGeneral.getApplicationNo();
			} else {
				TxReception txReception = selectOneReceptionByApplicationNo(data.getAppid());
				TxTmGeneral txTmGeneral = selectOneGeneralByApplicationNo(data.getAppid());
				MLaw mLaw = null;
				if(data.getLaw() != null) {
					mLaw = mLawRepository.findOne(data.getLaw());
				}

				txReception.setBankCode(data.getBankCode());
				txReception.setApplicationDate(DateUtil.toDate("dd/MM/yyyy HH:mm:ss", data.getApplicationDate()));
				txReception.setPaymentDate(DateUtil.toDate("dd/MM/yyyy HH:mm:ss", data.getPaymentDate()));
				txReception.setTotalPayment(data.getTotalPayment());
				txReception.setmFileSequence(data.getmFileSequence());
				txReception.setmFileType(data.getmFileType());
				txReception.setmFileTypeDetail(data.getmFileTypeDetail());
				txReception.setApplicantNotes(data.getApplicantNotes());
				txReceptionCustomRepository.saveOrUpdate(txReception);

				if(data.getReceptionIds()!=null && data.getReceptionIds().length > 0) {
					for(String receptionId : data.getReceptionIds()) {
						TxReception txReception2 = selectOneReceptionByApplicationNo(receptionId);
						TxTmReference txTmReference = txTmReferenceRepository.getReffByTxTmGeneralIdAndEFilingNo(txTmGeneral.getId(), txReception2.geteFilingNo());
						TxTmReference txTmReference2 = txTmReferenceRepository.getReffByTxTmGeneralIdAndEFilingNo(txTmGeneral.getId(), receptionId);
						
						
						if(txTmReference == null) {
							if(txTmReference2 == null) {
								TxTmGeneral reftxTmGeneral = txTmGeneralCustomRepository.select("id",txReception2.geteFilingNo());
								
								//select txTmGeneral by TxReception Id
								if(reftxTmGeneral==null) {
									  reftxTmGeneral = txTmGeneralCustomRepository.select("id",txReception2.getId());    
								}
								txTmReference = new TxTmReference(txTmGeneral, reftxTmGeneral);  
								txTmReferenceRepository.save(txTmReference);	
							}
						}
						//count total class
						int countClass =  txTmReferenceRepository.countClass(txTmGeneral);
						txReception.setTotalClass(countClass);
						txReceptionCustomRepository.saveOrUpdate(txReception);			
					}
				}

				txTmGeneral.setmLaw(mLaw);
				txTmGeneralCustomRepository.saveOrUpdate(txTmGeneral); //do on last to prevent deadlock
				application_no = txTmGeneral.getApplicationNo();
			}
		} catch (Exception ex) {
			//System.out.println(ex);
		}

		return application_no;
	}

	@Transactional
	public void deleteReception(String reception_id) {
		txTmGeneralCustomRepository.delete("txReception.id", reception_id);
		txReceptionRepository.delete(reception_id);

	}

	public GenericSearchWrapper<TxGroupDetail> searchGroupDetail(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<TxGroupDetail> searchResult = new GenericSearchWrapper<TxGroupDetail>();
		searchResult.setCount(txGroupDetailCustomRepository.count(searchCriteria));
		System.out.println(searchResult.getCount());
		if (searchResult.getCount() > 0) {
			searchResult.setList(txGroupDetailCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}

	public GenericSearchWrapper<TxTmGeneral> searchGeneral(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<TxTmGeneral> searchResult = new GenericSearchWrapper<TxTmGeneral>();
		searchResult.setCount(txTmGeneralCustomRepository.countPermohonan(searchCriteria));
//		System.out.println("dump count " + searchResult.getCount());
		if (searchResult.getCount() > 0) {
			searchResult.setList(txTmGeneralCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}

	public GenericSearchWrapper<TxTmGeneral> searchGeneral(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit, boolean withJoin) {
		TxTmGeneralCustomRepository.withJoin = withJoin;
		return this.searchGeneral(searchCriteria, orderBy, orderType, offset, limit);
	}

    public GenericSearchWrapper<TxReception> searchReception(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
        GenericSearchWrapper<TxReception> searchResult = new GenericSearchWrapper<TxReception>();
        searchResult.setCount(txReceptionCustomRepository.countReception(null,searchCriteria));
        if (searchResult.getCount() > 0) {
            searchResult.setList(txReceptionCustomRepository.selectAllReception(searchCriteria, orderBy, orderType, offset, limit));
        }
        return searchResult;
    }

	public GenericSearchWrapper<TxPostReception> searchPostReception(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
		GenericSearchWrapper<TxPostReception> searchResult = new GenericSearchWrapper<TxPostReception>();
		searchResult.setCount(txPostReceptionCustomRepository.count(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(txPostReceptionCustomRepository.selectAll(""
					+ "LEFT JOIN FETCH c.mFileSequence mfs "
					+ "LEFT JOIN FETCH c.mFileType mft "
					+ "LEFT JOIN FETCH c.mFileTypeDetail mftd"
					+ "LEFT JOIN FETCH c.mStatus ms "
					+ "LEFT JOIN FETCH c.createdBy cb", searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}

	public GenericSearchWrapper<TxPostReceptionDetail> searchPostReceptionDetail(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<TxPostReceptionDetail> searchResult = new GenericSearchWrapper<TxPostReceptionDetail>();
		searchResult.setCount(txPostReceptionDetailCustomRepository.count(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(txPostReceptionDetailCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}

	public GenericSearchWrapper<TxMonitor> searchMonitor(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<TxMonitor> searchResult = new GenericSearchWrapper<>();
		searchResult.setCount(txMonitorCustomRepository.countMonitor(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(txMonitorCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}

	public TxReception selectOneReceptionByApplicationNo(String applicationNo) {
		return txReceptionCustomRepository.selectOne("LEFT JOIN FETCH c.mFileSequence fs LEFT JOIN FETCH c.mFileType ft LEFT JOIN FETCH c.mFileTypeDetail ftc LEFT JOIN FETCH c.createdBy cb", "applicationNo", applicationNo, true);
	}
	
	public long countTxGroupDetail(List<KeyValue> searchCriteria) {
        return txGroupDetailCustomRepository.count(searchCriteria);
    }
	
	public TxReception selectOneReceptionBy(String by, String value){
        return txReceptionCustomRepository.selectOne(by, value, true);
    }

    public TxTmBrand selectOneTxtmBrandBy(String by, String value){
        return txTmBrandCustomRepository.selectOne(by, value, true);
    }

	public TxPostReception selectOneReceptionByPostNo(String postNo) {
		return txPostReceptionCustomRepository.selectOne("LEFT JOIN FETCH c.mFileSequence fs LEFT JOIN FETCH c.mFileType ft LEFT JOIN FETCH c.createdBy cb", "postNo", postNo, true);
	}

	public TxPostReception selectOnePostReceptionById(String id) {
		return txPostReceptionCustomRepository.selectOne("JOIN FETCH c.mFileSequence fs JOIN FETCH c.mFileType ft JOIN FETCH c.createdBy cb", "id", id, true);
	}

	public TxPostReceptionDetail selectOnePostReceptionDetail(String postReceptionId) {
		return txPostReceptionDetailCustomRepository.selectOneByAppId(postReceptionId);
	}

	
	
	// by andra
	public TxTmGeneral selectOneBy(String by, String value){
        return txTmGeneralCustomRepository.selectOne(by, value, true);
    }
	
	public TxTmGeneral selectOneGeneralByApplicationNo(String applicationNo) {
		return txTmGeneralCustomRepository.selectOne("applicationNo", applicationNo);
	}
	
	public TxTmGeneral selectOneImportClass(String applicationNo) {
		return txTmGeneralCustomRepository.selectOneImportClass("applicationNo", applicationNo);
	}

	public TxTmGeneral selectOneGeneralBrand(String applicationNo) {
		return txTmGeneralCustomRepository.selectOneGeneralBrand("applicationNo", applicationNo);
	}

	public TxTmGeneral selectOneGeneralByeFilingNo(String eFilingNo) {
		return txTmGeneralCustomRepository.selectOne("txReception.eFilingNo", eFilingNo);
	}

	public TxTmGeneral selectOneGeneralByeFilingNoByUser(List<KeyValue> criteriaList) {
		return txTmGeneralCustomRepository.selectOne(criteriaList);
	}

	public TxTmGeneral selectOneGeneralById(String id) {
		return txTmGeneralCustomRepository.selectOne("id", id);
	}

	public TxReception selectOneReceptionById(String id) {
		return txReceptionRepository.findOne(id);
	}

	@Transactional
	public void updateReception(TxReception txReception) {
		txReceptionRepository.save(txReception);
	}

	@Transactional
	public void updateReceptionNewApplicationNo(TxReception txReception) {
		txReceptionCustomRepository.saveOrUpdate(txReception);
	}

	@Transactional
	public void updatePostReception(TxPostReception txPostReception) {
		txPostReceptionRepository.save(txPostReception);
	}

	/* Publikasi */
	@Transactional
	public String insertPublikasi(TxPubsJournal txPubsJournal) {
		TxGroup txGroup = groupService.selectOneGroupById(txPubsJournal.getTxGroup().getId());
		String[] txGroupDetailList = groupService.selectAllGroupDetail(txGroup.getId());
		String[] txTmGeneralList = txTmGeneralCustomRepository.findAllTxTmGeneralByTxGroupDetailIdNative(txGroupDetailList);

		if (txTmGeneralList  != null) {

			boolean flagStatusPermohonan = true;
			MLookup mLookup = lookupService.selectOneById(txPubsJournal.getJournalType().getId());
//			if (mLookup.getCode().equalsIgnoreCase("BRMDefault") || mLookup.getCode().equalsIgnoreCase("BRMMadrid")) {
//				txTmGeneralCustomRepository.updateTxTmGeneralByBatch(StatusEnum.TM_PUBLIKASI.value(), txTmGeneralList);
//			}
//			else if (mLookup.getCode().equalsIgnoreCase("BRMMadrid")) {
//				txTmGeneralCustomRepository.updateTxTmGeneralByBatch(StatusEnum.M_PUBLICATION.value(), txTmGeneralList);
//				flagStatusPermohonan = false;
//			}

			MWorkflowProcess mWorkflowProcess = new MWorkflowProcess();
			if (flagStatusPermohonan) {
				txTmGeneralCustomRepository.updateTxTmGeneralByBatch(StatusEnum.TM_PUBLIKASI.value(), txTmGeneralList);
				mWorkflowProcess = mWorkflowProcessCustomRepository.selectOne("id", WfProcessEnum.TM_PUBLIKASI.toString(), true);
			}
			//else {
			//	mWorkflowProcess = mWorkflowProcessCustomRepository.selectOne("id", WfProcessEnum.M_PUBLICATION.toString(), true);
			//}
			MWorkflowProcessActions mWorkflowProcessActions = mWorkflowProcessActionCustomRepository.selectOne("process", mWorkflowProcess, true);

			List <TxMonitor> txMonitorList = new ArrayList <>();
			for (String appId : txTmGeneralList) {
				TxMonitor txMonitor = new TxMonitor();
				txMonitor.setTxTmGeneral(groupService.findOneGeneralById(appId));
				txMonitor.setCreatedDate(new Timestamp(System.currentTimeMillis()));
				txMonitor.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
				txMonitor.setmWorkflowProcess(mWorkflowProcess);
				txMonitor.setmWorkflowProcessActions(mWorkflowProcessActions);
				txMonitor.setDueDate(txPubsJournal.getJournalEnd().toString());
				txMonitor.setNotes("Nomor BRM : "+txPubsJournal.getJournalNo()+"/Nama Grup : "+txGroup.getName());
//				if (mLookup.getCode().equalsIgnoreCase("BRMDefault") || mLookup.getCode().equalsIgnoreCase("BRMMadrid")) {
				txMonitor.setTargetWorkflowProcess(WfProcessEnum.SELESAI_MASA_PENGUMUMAN.value());
//				}
//				else if (mLookup.getCode().equalsIgnoreCase("BRMMadrid")) {
//					txMonitor.setTargetWorkflowProcess(WfProcessEnum.M_DOC_DIST.value());
//				}

				txMonitorList.add(txMonitor);
			}

			txMonitorCustomRepository.saveTxMonitorByBatch(txMonitorList);
			txPubsJournalCustomRepository.saveOrUpdate(txPubsJournal);

			return "SUCCESS" ;
		}
		else{
			return "FAILED"  ;

		}

	}

	@Transactional//(noRollbackFor = Exception.class)
	public void deletePublikasi(String id) {
		TxPubsJournal txPubsJournal = txPubsJournalCustomRepository.selectOneBy("id", id);
		
		TxGroup txGroup = groupService.selectOneGroupById(txPubsJournal.getTxGroup().getId());
		String[] txGroupDetailList = groupService.selectAllGroupDetail(txGroup.getId());

		String[] txTmGeneralList = txTmGeneralCustomRepository.findAllTxTmGeneralByTxGroupDetailIdAndStatusNative(txGroupDetailList, StatusEnum.TM_PUBLIKASI.value());
		List<TxTmGeneral> listGeneralCurrent = txTmGeneralCustomRepository.findAllTxTmGeneralByTxGroupDetailIdAndStatus(txGroupDetailList, StatusEnum.TM_PUBLIKASI.value());
        String[] txMonitorList = txMonitorCustomRepository.findAllTxMonitorByTxTmGeneralIdAndWfProcessId(txTmGeneralList, WfProcessEnum.TM_PUBLIKASI.value());
		if(txMonitorList.length > 0) {
			try {
				txMonitorCustomRepository.deleteTxMonitorByBatch(txMonitorList);
				
				txTmGeneralCustomRepository.updateStatusTxTmGeneralByBatch(listGeneralCurrent);
            
            	txTmGeneralCustomRepository.updateActionTxTmGeneralByBatch(listGeneralCurrent);
			} catch (Exception e) {
				txTmGeneralCustomRepository.updateActionTxTmGeneralByBatchNull(listGeneralCurrent);
//				System.out.println("ERROR!!!!!!!!!");
//				System.out.println(e.getMessage());
			}
		}
        
		txPubsJournalCustomRepository.delete(txPubsJournal.getId());
	}

	@Transactional
	public void deleteDistribusi(String id) {
		TxServDist txServDist = txServDistRepository.findOne(id);
		TxGroup txGroup = txServDist.getTxGroup();
		List<TxGroupDetail> groupDetails = txGroup.getTxGroupDetailList();
		if (groupDetails.size() > 0){
			String applicationId = groupDetails.get(0).getTxTmGeneral().getId();
			if (applicationId != null){
				List<TxMonitor> txMonitorList = txMonitorCustomRepository.findByTxTmGeneralId(applicationId);
				for (TxMonitor txMonitor : txMonitorList) {
					if (txMonitor.getmWorkflowProcess().getId().equalsIgnoreCase(StatusEnum.TM_DISTRIBUSI_DOKUMEN.toString())){
						txMonitorRepository.delete(txMonitor.getId());
					}
				}
				TxTmGeneral ttg = groupDetails.get(0).getTxTmGeneral();
				List<TxMonitor> monitors = monitorService.findByTxTmGeneral(ttg);
				if (monitors.size() > 0) {
					TxMonitor monitor = monitors.get(0);
					ttg.setmStatus(monitor.getmWorkflowProcess().getStatus());
					try {
						if (monitor.getmWorkflowProcessActions().getAction() == null) {
							ttg.setmAction(null);
						} else {
							ttg.setmAction(monitor.getmWorkflowProcessActions().getAction());
						}

					} catch (Exception e) {
						ttg.setmAction(null);
					}
					txTmGeneralRepository.save(ttg);
				}else {
                    ttg.setmStatus(null);
                    txTmGeneralRepository.save(ttg);
                }
			}
		}
		txServDistRepository.deleteTxServDist(id);
	}

	@Transactional
	public void updateGeneral(TxTmGeneral txTmGeneral) {
		txTmGeneralCustomRepository.saveOrUpdate(txTmGeneral);
	}

	@Transactional
	public void updateGeneralOnline(TxTmGeneral txTmGeneral) {
		int currentYear = Calendar.getInstance().get(Calendar.YEAR);
		TcApplicationNo tcApplicationNo = tcApplicationNoCustomRepository.findOneBy(new HashMap<String, Object>(), null);
		if (tcApplicationNo == null) {
			tcApplicationNo = new TcApplicationNo();
		}
		tcApplicationNo.setmFileSequence(txTmGeneral.getTxReception().getmFileSequence());
		tcApplicationNo.setmFileType(txTmGeneral.getTxReception().getmFileType());
		tcApplicationNo.setYear(currentYear);
		tcApplicationNo.increaseSequence();
		tcApplicationNoCustomRepository.saveOrUpdate(tcApplicationNo);

		if(txTmGeneral.getTxReception().getmFileType().getMenu().equals("DAFTAR")) {
			txTmGeneral.setmStatus(StatusEnum.IPT_PENGAJUAN_PERMOHONAN.value());
		} else if(txTmGeneral.getTxReception().getmFileType().getMenu().equals("MADRID_OO")) {
			txTmGeneral.setmStatus(StatusEnum.IPT_PENGAJUAN_MADRID.value());
		} else if(txTmGeneral.getTxReception().getmFileType().getId().equals("MADRID_TRANSFORMASI")) {
			txTmGeneral.setmStatus(StatusEnum.IPT_PENGAJUAN_MADRID_TR.value());
		} else
		{ 	txTmGeneral.setmStatus(StatusEnum.IPT_PENGAJUAN_MADRID_RP.value());}

		txTmGeneral.setApplicationNo(tcApplicationNo.toString());
		txTmGeneral.getTxReception().setApplicationNo(tcApplicationNo.toString());

		txTmClassRepository.save(txTmGeneral.getTxTmClassList());
		txTmGeneralCustomRepository.saveOrUpdate(txTmGeneral);
		
		MWorkflowProcess mWorkflowProcess = mWorkflowProcessCustomRepository.selectOne("status.id", txTmGeneral.getmStatus().getId(), true);
		
		TxMonitor txMonitor = new TxMonitor();
		txMonitor.setTxTmGeneral(txTmGeneral);
		txMonitor.setCreatedDate(new Timestamp(System.currentTimeMillis()));
		txMonitor.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		txMonitor.setmWorkflowProcess(mWorkflowProcess);
		
		//MWorkflowProcessActions mWorkflowProcessActions = mWorkflowProcessActionCustomRepository.selectOne("process.id", mWorkflowProcess.currentId());
        //txMonitor.setmWorkflowProcessActions(mWorkflowProcessActions); 
        txMonitor.setNotes(txTmGeneral.getTxReception().getmFileType().getDesc());
        
		txMonitorRepository.save(txMonitor);
	}

	public TxPubsJournal selectOnePubJournal(String kriteria, String value) {
		return txPubsJournalCustomRepository.selectOne(kriteria, value);
	}
	
	public TxPubsJournal selectOnePubsJournal(String by, String value) {
		return txPubsJournalCustomRepository.selectOne(by, value, true);
	}

	public TxPubsJournal selectOnePubJournalByJournalNo(String journalNo) {
		//return txPubsJournalCustomRepository.selectOneStatus("JOIN FETCH c.mLaw ml JOIN FETCH c.createdBy cb", "journalNo", journalNo, true);
		return txPubsJournalCustomRepository.selectOne(
				"LEFT JOIN FETCH c.mLaw ml " +
				"LEFT JOIN FETCH c.journalType jt " +
				"LEFT JOIN FETCH c.createdBy cb", "journalNo", journalNo, true);
		// (Skip) "LEFT JOIN FETCH c.mStatus ms " +
	}

	public TxPubsJournal selectOnePubJournalById(String id) {
		return txPubsJournalCustomRepository.selectOne(
				"LEFT JOIN FETCH c.mLaw ml " +
				"LEFT JOIN FETCH c.journalType jt " +
				"LEFT JOIN FETCH c.createdBy cb", "id", id, true);
	}
	
	public TxPubsJournal selectOnePubJournalByAppId(String applicationId) {
		return txPubsJournalCustomRepository.selectOneByAppId(applicationId);
	}

	public TxPubsJournal selectOnePubJournalByAppNo(String applicationNo) {
		return txPubsJournalCustomRepository.selectOneByApplicationNo(applicationNo);
	}

	public GenericSearchWrapper<TxPubsJournal> searchPublikasi(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
		GenericSearchWrapper<TxPubsJournal> searchResult = new GenericSearchWrapper<TxPubsJournal>();
		searchResult.setCount(txPubsJournalCustomRepository.count(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(txPubsJournalCustomRepository.selectAll( searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}
	
	public GenericSearchWrapper<TxPubsJournal> searchLapBRM(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
		GenericSearchWrapper<TxPubsJournal> searchResult = new GenericSearchWrapper<TxPubsJournal>();
		searchResult.setCount(txPubsJournalCustomRepository.countLapBRM(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(txPubsJournalCustomRepository.selectAllLapBRM( searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}

	public TxTmGeneral selectOne(String kriteria, String value) {
		return txTmGeneralCustomRepository.select(kriteria, value);
	}
	
	public TxTmGeneral selectOne2(String kriteria, String value) {
		return txTmGeneralCustomRepository.selectOne(kriteria, value);
	}

	public TxTmGeneral selectOneRegistration(String kriteria, String value) {
		return txTmGeneralCustomRepository.selectOneGeneralRegistration(kriteria, value);
	}

	public List<DataBRM> selectIsi(String journalNo) {
		return dataBRMRepository.selectAll(journalNo);
	}

	public List<Object[]> selectObject(String journalNo) {
		return dataBRMRepository.selectObject(journalNo);
	}

	public List<Object[]> selectHeaderMerek(String applicationNo) {
		return cetakmerekRepository.selectHeader(applicationNo);
	}

	public List<Object[]> selectDetailMerek(String applicationNo) {
		return cetakmerekRepository.selectDetail(applicationNo);
	}

//	public Long selectCountGeneral() {
//		return txTmGeneralCustomRepository.countPermohonan(null);
//	}
//
//	public Long count(List<KeyValue> searchCriteria) {
//		return txReceptionCustomRepository.count(searchCriteria);
//	}

	public Long count(String by, Object value, boolean exactMatch) {
		return txTmGeneralCustomRepository.count(by, value, exactMatch);
	}
	
	public Long countPasca(String by, Object value, boolean exactMatch) {
		return txPostReceptionCustomRepository.count(by, value, exactMatch);
	}
	
	public Long countPascaDetail() {
//		return txPostReceptionDetailCustomRepository.count(by, value, exactMatch);
		return txPostReceptionDetailCustomRepository.countPascaDetail();
	}

	/*public String selectLastPostReceiptNo() {
		TxPostReception postReceiptNo = txPostReceptionCustomRepository.selectLastPostReceiptNo();
		if (postReceiptNo == null) {
			return "00000";
		} else if (postReceiptNo.getPostReceiptNo().length() > 5) {
			String temInt = Integer.toString(Integer.parseInt(postReceiptNo.getPostReceiptNo().substring(1) + 1));
			while (temInt.length() < 4) {
				temInt = "0" + temInt;
			}
			return temInt;
		} else {
			String temInt = Integer.toString(Integer.parseInt(postReceiptNo.getPostReceiptNo()) + 1);
			while (temInt.length() < 4) {
				temInt = "0" + temInt;
			}
			return temInt;
		}
	}*/

	/*-- Loket Dokumen Section --*/
	public GenericSearchWrapper<TxRegistration> ListPenerimaanDokumen(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
		GenericSearchWrapper<TxRegistration> searchResult = new GenericSearchWrapper<TxRegistration>();
		searchResult.setCount(txRegistrationCustomRepository.count(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(txRegistrationCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}

	public TxRegistration selectOnePenerimaanDokumen(String by, String value) {
		return txRegistrationCustomRepository.selectOne("LEFT JOIN FETCH c.txTmGeneral ttg ", by, value, true);
	}
	
	public long countLaporanBRM(List<KeyValue> searchCriteria) {
        return txPubsJournalCustomRepository.countLapBRM(searchCriteria);
    }
	
	public List<TxPubsJournal> selectAllLaporanBRM(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
        return txPubsJournalCustomRepository.selectAllLapBRM(searchCriteria, orderBy, orderType, offset, limit);
    }
	
	public List<TxGroupDetail> selectAllTxGroupDetail(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
        return txGroupDetailCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit);
    }

	@Transactional
	public void deletePostReception(String id) {
		txPostRepresentativeCustomRepository.delete("txPostReception.id", id);
		txPostOwnerCustomRepository.delete("txPostReception.id", id);
		txPostDocCustomRepository.delete("txPostReception.id", id);		
		txPostReceptionDetailCustomRepository.delete("txPostReception.id", id);
		txPostReceptionRepository.delete(id);
	}

	public GenericSearchWrapper<TxTmGeneral> searchGeneralRegistration(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<TxTmGeneral> searchResult = new GenericSearchWrapper<TxTmGeneral>();
		searchResult.setCount(txTmGeneralCustomRepository.countPermohonan(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(txTmGeneralCustomRepository.selectAllGeneralRegistration(searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}
	
	
	public long countAll(List<KeyValue> searchCriteria) {
        return txTmGeneralCustomRepository.countPermohonan(searchCriteria);
    }

    public List<TxTmGeneral> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
        return txTmGeneralCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit);
    }

	public GenericSearchWrapper<Object[]> searchGroupDetailByJournalNo(String JournalNo) {
		GenericSearchWrapper<Object[]> searchResult = new GenericSearchWrapper<Object[]>();
		searchResult.setList(txGroupDetailCustomRepository.searchGroupDetailByJournalNo(JournalNo));
		return searchResult;
	}
}
