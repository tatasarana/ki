package com.docotel.ki.repository.custom.transaction;


import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.transaction.TxPostDoc;
import com.docotel.ki.pojo.KeyValue;

import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;

@Repository
public class TxPostDocCustomRepository extends BaseRepository<TxPostDoc> {

	public List<KeyValue> selectAllNewAddPostDokumen() {
		List<KeyValue> resultList = new ArrayList<>();
		try {
			List<Object[]> objectResultArrList = (List<Object[]>) entityManager.createNativeQuery(" SELECT TX_POST_RECEPTION.POST_RECEPTION_NO, M_DOC_TYPE.DOC_TYPE_NAME " +
					" FROM TX_POST_DOC TX_POST_DOC " +
					" JOIN TX_POST_RECEPTION TX_POST_RECEPTION ON TX_POST_DOC.POST_RECEPTION_ID = TX_POST_RECEPTION.POST_RECEPTION_ID " +
					" JOIN M_DOC_TYPE M_DOC_TYPE ON TX_POST_DOC.DOC_TYPE_ID = M_DOC_TYPE.DOC_TYPE_ID " +
					" WHERE TX_POST_DOC.POST_DOC_STATUS = '0' ")
					.getResultList();
			for (Object[] objectResultArr : objectResultArrList) {
				KeyValue resultMap = new KeyValue();
				resultMap.setKey((String) objectResultArr[0]);
				resultMap.setValue(objectResultArr[1]);
				resultList.add(resultMap);
			}
		} catch (DataIntegrityViolationException e) {
			logger.error(e.getMessage(), e);
		}
		return resultList;
	}

	public void deleteTxPostDoc(String txPostDocId) {
		String queryS = "DELETE FROM TX_POST_DOC WHERE POST_DOC_ID = '" + txPostDocId + "'";
		Query query = entityManager.createNativeQuery(queryS);
		query.executeUpdate();
	}
}
