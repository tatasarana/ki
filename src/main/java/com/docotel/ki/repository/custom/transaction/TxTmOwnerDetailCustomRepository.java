package com.docotel.ki.repository.custom.transaction;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.transaction.TxTmOwnerDetail;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;

@Repository
public class TxTmOwnerDetailCustomRepository extends BaseRepository<TxTmOwnerDetail> {
	
	public TxTmOwnerDetail selectOne(String by, Object value) {
        return super.selectOne("LEFT JOIN FETCH c.txTmGeneral tg "
                + "LEFT JOIN FETCH c.txTmOwner tto "
                + " ", by, value, false);
    }
	
	public List<TxTmOwnerDetail> selectTxTmOwnerDetailByTxTmOwner(String txTmOwnerId, boolean status) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c " +
					"JOIN FETCH c.txTmOwner tg ");
			sbQuery.append(" WHERE c.txTmOwner.id = :p1 AND c.status = :p2 ");

			Query query = entityManager.createQuery(sbQuery.toString());
			query.setParameter("p1", txTmOwnerId);
			query.setParameter("p2", status);

			return query.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}
}


