package com.docotel.ki.repository.custom.transaction;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.master.MClassDetail;
import com.docotel.ki.model.transaction.TxTmOwner;
import com.docotel.ki.model.transaction.TxTmReference;
import com.docotel.ki.pojo.KeyValue;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class TxTmReferenceCustomRepository extends BaseRepository<TxTmReference> {

	
	@Override
    public List<TxTmReference> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		return selectAll("JOIN FETCH c.txTmGeneral tg ", searchCriteria, orderBy, orderType, offset, limit);
	}
	
    public List<TxTmReference> selectByTxTmGeneral(String generalId) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c "
					+"LEFT JOIN FETCH c.txTmGeneral tg "
                    +"LEFT JOIN FETCH tg.txRegistration mc "
					+"LEFT JOIN FETCH tg.txTmBrand tb "
					);
			sbQuery.append(" WHERE tg.id = :p1");

			Query query = entityManager.createQuery(sbQuery.toString());
			query.setParameter("p1", generalId);

			return query.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}
  
}
