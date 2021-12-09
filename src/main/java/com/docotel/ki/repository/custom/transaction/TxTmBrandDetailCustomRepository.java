package com.docotel.ki.repository.custom.transaction;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.transaction.TxTmBrandDetail;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;

@Repository
public class TxTmBrandDetailCustomRepository extends BaseRepository<TxTmBrandDetail> {
    public TxTmBrandDetail selectOne(String by, Object value) {
        return super.selectOne("LEFT JOIN FETCH c.txTmBrand tb "
                + "LEFT JOIN FETCH c.txTmGeneral tg "
                + " ", by, value, false);
    }
    
    public List<TxTmBrandDetail> selectTxTmBrandDetailByTxTmBrand(String txTmBrandId) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
			sbQuery.append(" WHERE c.txTmBrand.id = :p1 ");

			Query query = entityManager.createQuery(sbQuery.toString());
			query.setParameter("p1", txTmBrandId);

			return query.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}
}
