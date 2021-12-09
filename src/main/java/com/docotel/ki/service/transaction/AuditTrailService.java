package com.docotel.ki.service.transaction;

import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.transaction.TxLogHistory;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.service.master.UserService;
import com.docotel.ki.repository.custom.transaction.TxLogHistoryCustomRepository;
import com.docotel.ki.repository.transaction.TxLogHistoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import java.util.List;

@Service
public class AuditTrailService {
    /***************************** - AUTO INJECT SECTION - ****************************/
    @Autowired private TxLogHistoryRepository txLogHistoryRepository;

    @Autowired private TxLogHistoryCustomRepository txLogHistoryCustomRepository;

    @Autowired private UserService userService;
    
    /***************************** - METHOD SECTION - ****************************/
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TxLogHistory findOneLogHistory(String objectClassName, String objectId) {
        try {
            return txLogHistoryCustomRepository.selectOneLastByObjectClassNameAndObjectId(objectClassName, objectId);
        } catch (NoResultException e) {
        }
        return null;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLogHistory(TxLogHistory txLogHistory) {
		MUser existingUser = userService.getUserByName(txLogHistory.getUserName());
		if(existingUser != null) {
			if(existingUser.isEmployee()) {
				if (existingUser.getmSection() != null) {
					txLogHistory.setSection(existingUser.getmSection().getId());
				}
			}				
		}
    	
        txLogHistoryRepository.save(txLogHistory);
    }

    public GenericSearchWrapper<TxLogHistory> searchGeneral(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        GenericSearchWrapper<TxLogHistory> searchResult = new GenericSearchWrapper<>();
        searchResult.setCount(txLogHistoryCustomRepository.countLog(searchCriteria));
        if (searchResult.getCount() > 0) {
            searchResult.setList(txLogHistoryCustomRepository.selectAllLog(searchCriteria, orderBy, orderType, offset, limit));
        }
        return searchResult;
    }

    public TxLogHistory findOneById(String id) {
    	return txLogHistoryRepository.findOne(id);
    }
}
