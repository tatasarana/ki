package com.docotel.ki.service.master;

import com.docotel.ki.model.master.MEmployee;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.master.MEmployeeCustomRepository;
import com.docotel.ki.repository.master.MEmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {
    @Autowired
    MEmployeeRepository mEmployeeRepository;

    @Autowired
    MEmployeeCustomRepository mEmployeeCustomRepository;


    public void insert(MEmployee data) {
        mEmployeeRepository.save(data);
    }
    
    public List<MEmployee> findAllMEmployee() {
		return mEmployeeRepository.findAllMEmployee();
	}

    public MEmployee selectOne(String by, String value){
        return mEmployeeCustomRepository.selectOne(by, value, true);
    }
 
    public GenericSearchWrapper<MEmployee> searchGeneral(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        GenericSearchWrapper<MEmployee> searchResult = new GenericSearchWrapper<MEmployee>();
        searchResult.setCount(mEmployeeCustomRepository.count(searchCriteria));
        if (searchResult.getCount() > 0) {
            searchResult.setList(mEmployeeCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
        }
        return searchResult;
    }

    public MEmployee findOne(String s) {
        return mEmployeeRepository.findOne(s);
    }

    public MEmployee selectOneByUserId(String userId) {
        return mEmployeeCustomRepository.selectOneByUserId(userId);
    }
    
    public MEmployee selectOneByUserIdEmployee(String userId) {
        return mEmployeeCustomRepository.selectOne("LEFT JOIN FETCH c.userId ui LEFT JOIN FETCH ui.mFileSequence mf "
        		+ "LEFT JOIN FETCH c.mSection ms LEFT JOIN FETCH ms.mDepartment md LEFT JOIN FETCH md.mDivision mv "
        		,"userId.id", userId, true); 
    }
}
