package com.docotel.ki.service.master;

import com.docotel.ki.model.master.MFileSequence;
import com.docotel.ki.model.master.MFileType;
import com.docotel.ki.model.master.MFileTypeDetail;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.master.MFileSeqRepository;
import com.docotel.ki.repository.master.MFileTypeDetailRepository;
import com.docotel.ki.repository.master.MFileTypeRepository;
import com.docotel.ki.model.master.*;
import com.docotel.ki.repository.custom.master.MFileTypeDetailCustomRepository;
import com.docotel.ki.repository.custom.master.MFileSeqCustomRepository;
import com.docotel.ki.repository.custom.master.MFileTypeCustomRepository;
//import com.docotel.ki.repository.custom.master.MPostFileTypeDetailCustomRepository;
import com.docotel.ki.repository.master.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FileService {
	/***************************** - AUTO INJECT SECTION - ****************************/
	/*@Autowired private MPostFileTypeDetailCustomRepository mPostFileTypeDetailCustomRepository;
	@Autowired private MPostFileTypeRepository mPostFileTypeRepository;
	@Autowired private MPostFileTypeDetailRepository mPostFileTypeDetailRepository;*/

	@Autowired
	private MFileTypeRepository mFileTypeRepository;

	@Autowired
	private MFileTypeCustomRepository mFileTypeCustomRepository;

	@Autowired
	private MFileTypeDetailRepository mFileTypeDetailRepository;

	@Autowired
	private MFileTypeDetailCustomRepository mFileTypeDetailCustomRepository;

	@Autowired
	private MFileSeqRepository mFileSeqRepository;
	
	@Autowired
	private MFileSeqCustomRepository mFileSeqCustomRepository;

	/***************************** - METHOD SECTION - ****************************/
	public List<MFileType> findAll() {
		return mFileTypeRepository.findAll();
	}

	public List<MFileType> getAllFileTypes() {
		return mFileTypeRepository.findAll();
	}

	public List<MFileType> findMFileTypeByStatusFlagTrue() {
		return mFileTypeRepository.findByStatusFlagTrue();
	}
	
	public List<MFileType> findMFileTypeByFileTypeMenu() {
		return mFileTypeRepository.findByFileTypeMenu();
	}
	
	public List<MFileType> findByMenuAndstatusPaidAndstatusFlag(String menu,boolean statusFlag) {
		return mFileTypeRepository.findByMenuAndstatusPaidAndstatusFlag(menu, statusFlag);
	}
	
	public List<MFileType> findByMenu(String menu) {
		return mFileTypeRepository.findByMenu(menu);
	}

	/*public List<MPostFileType> findMPostFileTypeByStatusFlagTrue() {
		return mPostFileTypeRepository.findByStatusFlagTrue();
	}
	public List<MPostFileTypeDetail> findMPostFileTypeDetailByStatusFlagTrue() {
		return mPostFileTypeDetailRepository.findByStatusFlagTrue();
	}*/
	
	public List<MFileTypeDetail> findMFileTypeDetailById() {
		return mFileTypeDetailRepository.findMFileTypeDetailById();
	};

	public List<MFileTypeDetail> getAllFileTypeDetail() {
		return mFileTypeDetailRepository.findAll();
	}

	public List<MFileTypeDetail> findMFileTypeDetailByStatusFlagTrue() {
		return mFileTypeDetailRepository.findByStatusFlagTrue();
	}

	

	public List<MFileTypeDetail> getFileTypeDetailByFileType(String id) {
		return mFileTypeDetailRepository.findAll();
	}

	public MFileType getFileTypeById(String id) {
		return mFileTypeCustomRepository.selectOne(" LEFT JOIN FETCH c.mWorkflow w ", "id", id, true);
	}

	public MFileTypeDetail getMFileTypeDetailById(String id) {
		return mFileTypeDetailRepository.findOne(id);
	}

	public List<MFileSequence> getAllFileSequences() {
		return mFileSeqRepository.findAll();
	}

	public List<MFileSequence> findMFileSequenceByStatusFlagTrue() {
		return mFileSeqRepository.findByStatusFlagTrue();
	}
	
	public List<MFileSequence> findMFileSequenceByKanwilFlagTrue() {
		return mFileSeqRepository.findByKanwilFlagTrue();
	}

	public MFileSequence getFileSequenceById(String id) {
		return mFileSeqRepository.findOne(id);
	}
	
	public MFileSequence getFileSequenceByCode(String code) {
		return mFileSeqCustomRepository.selectOne("code", code);
	}

	public boolean isValidFileTypeDetail(MFileType mFileType, MFileTypeDetail mFileTypeDetail) {
		mFileTypeDetail = mFileTypeDetailCustomRepository.selectOne("id", mFileTypeDetail.getCurrentId());

		return (mFileTypeDetail != null);
	}

	/*public boolean isValidFileTypeDetailPost(MPostFileType mPostFileType, MPostFileTypeDetail mPostFileTypeDetail) {
		mPostFileTypeDetail = mPostFileTypeDetailCustomRepository.selectOneStatus("id", mPostFileTypeDetail.getCurrentId());

		return (mPostFileTypeDetail != null && mPostFileTypeDetail.getmPostFileType().getCurrentId().equalsIgnoreCase(mPostFileType.getCurrentId()));
	}*/

	public List<MFileTypeDetail> getAllFileTypeDetailByFileType(String by, String value){
		return mFileTypeDetailCustomRepository.selectAll(by, value, true ,0,100);
	}

	public GenericSearchWrapper<MFileTypeDetail> searchFileTypeDetailExclude(List<KeyValue> searchCriteria, String[] exclude, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<MFileTypeDetail> searchResult = new GenericSearchWrapper<MFileTypeDetail>();
		searchResult.setCount(mFileTypeCustomRepository.countAllExclude(searchCriteria, exclude));
		searchResult.setList(mFileTypeCustomRepository.selectAllExclude("JOIN FETCH c.mFileType pc", searchCriteria, exclude, orderBy, orderType, offset, limit));

		return searchResult;
	}

	public GenericSearchWrapper<MFileType> searchFileType(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<MFileType> searchResult = new GenericSearchWrapper<MFileType>();
		searchResult.setCount(mFileTypeCustomRepository.count(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(mFileTypeCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}

	public GenericSearchWrapper<MFileTypeDetail> searchFileTypeDetail(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<MFileTypeDetail> searchResult = new GenericSearchWrapper<MFileTypeDetail>();
		searchResult.setCount(mFileTypeDetailCustomRepository.count(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(mFileTypeDetailCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}

	@Transactional
	public void insertMFileType(MFileType mFileType){
		mFileTypeRepository.save(mFileType);
	}

	public MFileType findOne(String s) {
		return mFileTypeRepository.findOne(s);
	}

	public List<MFileTypeDetail> selectAllByFileType(String by, String value){
		return mFileTypeDetailCustomRepository.selectAllByFileType(by, value, 0, 500);
	}
	
	public List<MFileType> selectAllByFileTypeCode(){
		return mFileTypeCustomRepository.selectByFileTypeCode();
	}

	public GenericSearchWrapper<MFileTypeDetail> searchGeneral(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<MFileTypeDetail> searchResult = new GenericSearchWrapper<MFileTypeDetail>();
		searchResult.setCount(mFileTypeDetailCustomRepository.count(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(mFileTypeDetailCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}
	public void insertFileTypeDetail(MFileTypeDetail data){
		mFileTypeDetailRepository.save(data);
	}

	@Transactional
	public void saveOrUpdateChild(MFileTypeDetail mFileTypeDetail) {
		mFileTypeDetailCustomRepository.saveOrUpdate(mFileTypeDetail);
	}

	@Transactional
	public void saveOrUpdateFileType(MFileType mFileType) {
		mFileTypeCustomRepository.saveOrUpdate(mFileType);
	}

	public List<MFileTypeDetail> selectAllByMFileType(String by, String value){
		return mFileTypeDetailCustomRepository.selectAllByFileType(by, value, 0, 500);
	}

	public MFileTypeDetail getFirstByCode(String code){
		return mFileTypeDetailRepository.getFirstByCode(code);
	}

	public List<MFileType> selectAllMFileType(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit){
		return mFileTypeCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit);
	}
	
	public List<MFileTypeDetail> selectAllMFileTypeDetail(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit){
		return mFileTypeDetailCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit);
	}

	public List<MFileTypeDetail> findByCode(String code) {
		return mFileTypeDetailRepository.findMFileTypeDetailByCode(code);
	}
}
