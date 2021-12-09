package com.docotel.ki.repository.custom.transaction;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.transaction.TxTmRepresentative;
import com.docotel.ki.pojo.KeyValue;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;

@Repository
public class TxTmReprsCustomRepository extends BaseRepository<TxTmRepresentative> {
    public TxTmRepresentative selectOne(String by, Object value){
        return super.selectOne("LEFT JOIN FETCH c.createdBy cb "
                + "LEFT JOIN FETCH c.txTmGeneral tg "
                + "LEFT JOIN FETCH c.mRepresentative mr "
                + "LEFT JOIN FETCH mr.mCountry mc "
                + "LEFT JOIN FETCH mr.mProvince mp "
                + "LEFT JOIN FETCH mr.mCity mci "
                + " ", by, value, false);
    }
    
    // Custom by Andra for list pratinjau permohonan (form pemohon)
    public List<TxTmRepresentative> selectAllReprsByIdGeneral(String generalId) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c "
					+ "LEFT JOIN FETCH c.txTmGeneral tg "
	                + "LEFT JOIN FETCH c.mRepresentative mr "
	                + "LEFT JOIN FETCH mr.mCountry mc "
	                + "LEFT JOIN FETCH mr.mProvince mp "
	                + "LEFT JOIN FETCH mr.mCity mci ");
			sbQuery.append(" WHERE c.txTmGeneral.id = :p1");
			sbQuery.append(" ORDER BY c.createdDate ASC");

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
    
    // Custom by Andra for ParamSubtitudeService (parameter surat)
    public List<TxTmRepresentative> selectTxTmReprsByTxTmGeneral(String generalId) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c "
					+ "LEFT JOIN FETCH c.txTmGeneral tg "
	                + "LEFT JOIN FETCH c.mRepresentative mr "
	                + "LEFT JOIN FETCH mr.mCountry mc "
	                + "LEFT JOIN FETCH mr.mProvince mp "
	                + "LEFT JOIN FETCH mr.mCity mci ");
			sbQuery.append(" WHERE c.txTmGeneral.id = :p1");

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
    
    public TxTmRepresentative selectOneT(String join, List<KeyValue> searchCriteria, String orderBy, String orderType) {
		return super.selectOne(join, searchCriteria, orderBy, orderType);
	}
    
    // for Cetak Merek 
    public List<TxTmRepresentative> selectTxTmReprsByGeneral(String generalId, boolean status) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c "
					+ "LEFT JOIN FETCH c.txTmGeneral tg "
	                + "LEFT JOIN FETCH c.mRepresentative mr "
	                + "LEFT JOIN FETCH c.mCountry mc "
	                + "LEFT JOIN FETCH c.mProvince mp "
	                + "LEFT JOIN FETCH c.mCity mci ");
			sbQuery.append(" WHERE c.txTmGeneral.id = :p1 AND c.status = :p2 ");

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
    
    // for list table permohonan
    public List<TxTmRepresentative> selectTxTmReprsByGeneral(String generalId) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c "
					+ "LEFT JOIN FETCH c.txTmGeneral tg "
	                + "LEFT JOIN FETCH c.mRepresentative mr ");
			sbQuery.append(" WHERE c.txTmGeneral.id = :p1 ");

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