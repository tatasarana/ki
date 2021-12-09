package com.docotel.ki.service.master;

import java.util.List;

import com.docotel.ki.model.master.MMenuDetail;
import com.docotel.ki.model.master.MRole;
import com.docotel.ki.model.master.MRoleDetail;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.pojo.RoleDetail;
import com.docotel.ki.model.master.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.docotel.ki.repository.custom.master.MMenuDetailCustomRepository;
import com.docotel.ki.repository.custom.master.MRoleCustomRepository;
import com.docotel.ki.repository.custom.master.MRoleDetailCustomRepository;
import com.docotel.ki.repository.master.MRoleDetailRepository;
import com.docotel.ki.repository.master.MRoleRepository;

@Service
public class RoleService {
	@Autowired
	MRoleRepository mRoleRepository;
	@Autowired
	MRoleCustomRepository mRoleCustomRepository;

	@Autowired
	MRoleDetailRepository mRoleDetailRepository;
	@Autowired
	MRoleDetailCustomRepository mRoleDetailCustomRepository;

	@Autowired
	MMenuDetailCustomRepository mMenuDetailCustomRepository;

	@Value("${upload.file.path:}")
	private String uploadFilePath;

	public List<MRole> findAll() {
		//return mRoleRepository.findAll();
		return mRoleRepository.findByStatusFlagTrue();
	}

	public MRole selectMroleById(String s) {
		return mRoleRepository.findOne(s);
	}

	public GenericSearchWrapper<MRole> searchGeneral(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<MRole> searchResult = new GenericSearchWrapper<MRole>();
		searchResult.setCount(mRoleCustomRepository.count(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(mRoleCustomRepository.selectAll("LEFT JOIN FETCH c.createdBy cb", searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}

	public void insert(MRole data) {
		mRoleRepository.save(data);
	}

	@Transactional
	public void saveOrUpdate(MRole mRole) {
		mRoleCustomRepository.saveOrUpdate(mRole);
	}

	public List<MMenuDetail> selectAllByMenu(String target) {
		return mMenuDetailCustomRepository.selectAll("mMenu.id", target, true, null, null);

	}

	@Transactional
	public void saveRoleDetail(RoleDetail roleDetail) {

		MRole mRole = selectMroleById(roleDetail.getIdRole());
		if (mRole != null) {
			List<MRoleDetail> mRoleDetailList = selectAllByRole(roleDetail.getIdRole());
			mRoleDetailRepository.delete(mRoleDetailList);
			for (String id : roleDetail.getIdRoleDetail()) {
				MRoleDetail mRoleDetail = new MRoleDetail();
				mRoleDetail.setmRole(mRole);
				mRoleDetail.setmMenuDetail(new MMenuDetail() {
					{
						setId(id);
					}
				});
				mRoleDetailRepository.save(mRoleDetail);
			}
		}
	}

	public List<MRole> selectAllRole() {
		return mRoleRepository.findAll();
		//return mRoleCustomRepository.selectAll("LEFT JOIN FETCH c.createdBy cb","",null,true, null,null);
	}


	public MRole selectOne(String by, String value){
		return mRoleCustomRepository.selectOne(by, value, true);
	}

	public List<MRoleDetail> selectAllByRole(String value) {
		return mRoleDetailCustomRepository.selectAllByRole(value);
	}

	public List<MRoleDetail> selectAllRoleDetailByUser(MUser mUser) {
		return mRoleDetailCustomRepository.selectAllByUser(mUser.getId());
	}
}
