package com.docotel.ki.service.master;

import com.docotel.ki.model.master.MRole;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.master.MUserRole;
import com.docotel.ki.model.master.*;
import com.docotel.ki.repository.custom.master.MEmployeeCustomRepository;
import com.docotel.ki.repository.custom.master.MRoleCustomRepository;
import com.docotel.ki.repository.custom.master.MUserRoleCustomRepository;
import com.docotel.ki.repository.custom.transaction.MrepresentativeCustomRepository;
import com.docotel.ki.repository.master.MEmployeeRepository;
import com.docotel.ki.repository.master.MRoleRepository;
import com.docotel.ki.repository.master.MUserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserRoleService {
    @Autowired MUserRoleRepository mUserRoleRepository;
    @Autowired MUserRoleCustomRepository mUserRoleCustomRepository;
    @Autowired MRoleRepository mRoleRepository;
    @Autowired MRoleCustomRepository mRoleCustomRepository;
    @Autowired MEmployeeRepository mEmployeeRepository;
    @Autowired MEmployeeCustomRepository mEmployeeCustomRepository;
    @Autowired MrepresentativeCustomRepository mRepresentativeCustomRepository;
    @Autowired UserService userService;

    public List<MUserRole> findAll() {
        return mUserRoleRepository.findAll();
    }

    public MUserRole findOne(String id) {
        return mUserRoleRepository.findOne(id);
    }

    // olly@docotel.com -- start
    public MUserRole findUserAdministrator(String userId) {
        return mUserRoleRepository.findUserAdministrator(userId);
    }
    // olly@docotel.com -- end
    // -- start
    public MUserRole findUserRole(String userId) {
        return mUserRoleRepository.findUserRole(userId);
    }
    // -- end
   
    public void save(MUser mUser, String[] role) {
    	List<MUserRole> mUserRoleList = selectAllByUser(mUser.getId());
        mUserRoleRepository.delete(mUserRoleList);
        if (role != null){
            for (String rl : role) {
                //v1 MRole mRole = mRoleRepository.getOne(rl);
                MRole mRole = mRoleCustomRepository.selectOne("id", rl, true);
                if (mRole != null) {
                    MUserRole mUserRole = new MUserRole();
                    mUserRole.setmUser(mUser);
                    mUserRole.setmRole(mRole);
                    mUserRoleRepository.save(mUserRole);
                }
            }
        }
        
        
        
        
        //public void save(String id, String[] role) {
		/*MEmployee mEmployee = mEmployeeCustomRepository.selectOneStatus("userId.id", mUser, true);
		//MEmployee mEmployee = mEmployeeRepository.findOne(id);
        List<MUserRole> mUserRoleList = selectAllByUser(mEmployee.getUserId().getId());
        mUserRoleRepository.delete(mUserRoleList);
        if (role != null){
            for (String rl : role) {
                MRole mRole = mRoleRepository.getOne(rl);
                if (mRole != null) {
                    MUserRole mUserRole = new MUserRole();
                    mUserRole.setmUser(mEmployee.getUserId());
                    mUserRole.setmRole(mRole);
                    mUserRoleRepository.save(mUserRole);
                }
            }
        }*/
    }

    public List<MUserRole> selectAllByUser(String value) {
        //v1 return mUserRoleCustomRepository.selectAll("mUser.id", value, true, 0, 1000);
    	return mUserRoleCustomRepository.selectAll("LEFT JOIN FETCH c.mUser mu LEFT JOIN FETCH c.mRole mr", "mUser.id", value, true,null,null );
    }
}
