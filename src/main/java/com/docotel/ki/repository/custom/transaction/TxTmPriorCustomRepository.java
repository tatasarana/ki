package com.docotel.ki.repository.custom.transaction;


import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.transaction.TxTmPrior;
import com.docotel.ki.pojo.KeyValue;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

@Repository
public class TxTmPriorCustomRepository extends BaseRepository<TxTmPrior> {

	public List<TxTmPrior> selectTxTmPriorByTxTmGeneral(String generalId, String status) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c "
					+ "INNER JOIN FETCH c.txTmGeneral tg");
			sbQuery.append(" WHERE c.txTmGeneral.id = :p1");
			sbQuery.append(" AND c.status = :p2 ");

			Query query = entityManager.createQuery(sbQuery.toString());
			query.setParameter("p1", generalId);
			query.setParameter("p2", status);

			return query.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}
	
	public List<TxTmPrior> selectAllT(String join, List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		return super.selectAll("LEFT JOIN FETCH c.mFileSequence s LEFT JOIN FETCH c.mFileTypeDetail tc", searchCriteria, orderBy, orderType, offset, limit);
	}
	
	public TxTmPrior selectOneT(String join,List<KeyValue> searchCriteria, String orderBy, String orderType) {
		return super.selectOne(join, searchCriteria, orderBy, orderType);
	}

	public TxTmPrior selectOne(String by, Object value) {
		return super.selectOne("LEFT JOIN FETCH c.createdBy cb ", by, value, true);
	}
	 
}
