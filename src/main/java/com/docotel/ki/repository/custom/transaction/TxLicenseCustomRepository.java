package com.docotel.ki.repository.custom.transaction;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.transaction.TxLicense;
import org.springframework.stereotype.Repository;

@Repository
public class TxLicenseCustomRepository extends BaseRepository<TxLicense> {
	
	public TxLicense selectOne(String by, Object value) {
        return super.selectOne("LEFT JOIN FETCH c.createdBy cb "
                + "LEFT JOIN FETCH c.txTmGeneral tg "
                + "LEFT JOIN FETCH c.nationality na "
                + "LEFT JOIN FETCH c.mCountry mc "
                + "LEFT JOIN FETCH c.mProvince mp "
                + "LEFT JOIN FETCH c.mCity mci "
                + "LEFT JOIN FETCH c.txLicenseParent tlp "
                + " ", by, value, false);
    }
	
    public List<TxLicense> selectAllLicenseByGeneralId(String generalId) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c " +
					"JOIN FETCH c.txTmGeneral tg " +
                    "JOIN FETCH c.mCountry mc " +
					"LEFT JOIN FETCH c.txLicenseParent p ");
			sbQuery.append(" WHERE c.txTmGeneral.id = :p1");
			sbQuery.append(" ORDER BY c.status DESC");

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
    
    // Scheduler
    public List<TxLicense> selectAllLicenseByEndDate() {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c " +
					"JOIN FETCH c.txTmGeneral tg " +
                    "JOIN FETCH c.mCountry mc " +
					"LEFT JOIN FETCH c.txLicenseParent p ");
			sbQuery.append(" WHERE c.endDate < SYSDATE ");

			Query query = entityManager.createQuery(sbQuery.toString());

			return query.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}
    
    public List<TxLicense> selectAllParentByLicenseId(String id, String generalId) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c " +
					"JOIN FETCH c.txTmGeneral tg " +
					"LEFT JOIN FETCH c.txLicenseParent p ");
			sbQuery.append(" WHERE c.id = :p1 OR c.txTmGeneral.id = :p2 ");

			Query query = entityManager.createQuery(sbQuery.toString());
			query.setParameter("p1", id);
			query.setParameter("p2", generalId);

			return query.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}
    
    // Custom by Andra for ParamSubtitudeService (parameter surat)
    public List<TxLicense> findTxLicenseByTxTmGeneral(String generalId, boolean status) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c "
					+ "LEFT JOIN FETCH c.txTmGeneral tg "
	                + "LEFT JOIN FETCH c.nationality na "
	                + "LEFT JOIN FETCH c.mCountry mc "
	                + "LEFT JOIN FETCH c.mProvince mp "
	                + "LEFT JOIN FETCH c.mCity mci ");
			sbQuery.append(" WHERE c.txTmGeneral.id = :p1 AND c.status = :p2");

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

	public List<TxLicense> findSubLicenseByTxTmGeneral(String generalId, String postId) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c "
					+ "LEFT JOIN FETCH c.txTmGeneral tg "
					+ "LEFT JOIN FETCH c.nationality na "
					+ "LEFT JOIN FETCH c.mCountry mc "
					+ "LEFT JOIN FETCH c.mProvince mp "
					+ "LEFT JOIN FETCH c.mCity mci ");
			sbQuery.append(" WHERE c.txTmGeneral.id = :p1 AND c.txPostReception = :p2 and c.txLicenseParent is null");

			Query query = entityManager.createQuery(sbQuery.toString());
			query.setParameter("p1", generalId);
			query.setParameter("p2", postId);

			return query.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	public List<TxLicense> findSubLicenseChildByTxTmGeneral(String generalId, String postId) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c "
					+ "LEFT JOIN FETCH c.txTmGeneral tg "
					+ "LEFT JOIN FETCH c.nationality na "
					+ "LEFT JOIN FETCH c.mCountry mc "
					+ "LEFT JOIN FETCH c.mProvince mp "
					+ "LEFT JOIN FETCH c.mCity mci ");
			sbQuery.append(" WHERE c.txTmGeneral.id = :p1 AND c.txPostReception = :p2 and c.txLicenseParent is not null");

			Query query = entityManager.createQuery(sbQuery.toString());
			query.setParameter("p1", generalId);
			query.setParameter("p2", postId);

			return query.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}
}
