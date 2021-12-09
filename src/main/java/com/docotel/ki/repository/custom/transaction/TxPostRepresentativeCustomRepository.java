package com.docotel.ki.repository.custom.transaction;


import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.transaction.TxPostRepresentative;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;

@Repository
public class TxPostRepresentativeCustomRepository extends BaseRepository<TxPostRepresentative> {
	
	// Custom by Andra for ParamSubtitudeService (parameter surat)
    public List<TxPostRepresentative> findAllTxPostReprsByTxTmGeneral(String generalId) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c "
					+ "LEFT JOIN FETCH c.txPostReception tpr "
					+ "LEFT JOIN FETCH c.mRepresentative mr "
					+ "LEFT JOIN FETCH mr.mCountry mc "
	                + "LEFT JOIN FETCH mr.mProvince mp "
	                + "LEFT JOIN FETCH mr.mCity mci "
            		+ "LEFT JOIN FETCH tpr.txPostReceptionDetailList tprd "
	                + "LEFT JOIN FETCH tprd.txTmGeneral tg ");
			sbQuery.append(" WHERE tprd.txTmGeneral.id = :p1");

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
