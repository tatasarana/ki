package com.docotel.ki.repository.custom.transaction;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.model.transaction.TxTmOwner;
import com.docotel.ki.pojo.KeyValue;

import java.util.List;

import javax.persistence.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TxTmOwnerCustomRepository extends BaseRepository<TxTmOwner> {

	@Autowired
	private EntityManagerFactory emf;



	@Override
    public List<TxTmOwner> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		return selectAll("LEFT JOIN FETCH c.mCountry mc "
				+ "LEFT JOIN FETCH c.mProvince mp "
				+ "LEFT JOIN FETCH c.mCity mci " 
				+ "LEFT JOIN FETCH c.txTmOwnerDetails owd "
				+ "JOIN FETCH c.createdBy cb "
				+ "JOIN FETCH c.txTmGeneral tg ", searchCriteria, orderBy, orderType, offset, limit);
	}
	
    public TxTmOwner selectOne(String by, Object value) {
        return super.selectOne("LEFT JOIN FETCH c.createdBy cb "
                + "LEFT JOIN FETCH c.txTmGeneral tg "
                + "LEFT JOIN FETCH c.nationality na "
                + "LEFT JOIN FETCH c.mCountry mc "
                + "LEFT JOIN FETCH c.mProvince mp "
                + "LEFT JOIN FETCH c.mCity mci "
                + "LEFT JOIN FETCH c.txTmOwnerDetails ttod "
                + " ", by, value, false);
    }
    
    // Custom by Andra for list pratinjau permohonan (form pemohon)
    public List<TxTmOwner> selectAllOwnerByIdGeneral(String generalId) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c " +
					"LEFT JOIN c.txTmGeneral tg "
                    //"LEFT JOIN c.mCountry mc "
			);
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
    
    // Custom by Andra for ParamSubtitudeService (parameter surat)
    public List<TxTmOwner> selectTxTmOwnerByTxTmGeneral(String generalId, boolean status) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c " +
					"JOIN FETCH c.txTmGeneral tg "
                    //"JOIN FETCH c.mCountry mc "
			);
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



	public List<TxTmOwner> selectTxTmOwnerByPostId(String generalId, String postId) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c " +
					"JOIN FETCH c.txTmGeneral tg "
					//"JOIN FETCH c.mCountry mc "
			);
			sbQuery.append(" WHERE c.txTmGeneral.id = :p1 AND c.txPostReception = :p2 AND c.status = '0' ");
//			System.out.println("fit generalId " + generalId + "post id " + postId + "query " + sbQuery);
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


	public void hardDelete(String bycondition, String value){


		try{
			EntityManager em = emf.createEntityManager();
			EntityTransaction tx = em.getTransaction();
			tx.begin();

			em.createNativeQuery
					("DELETE TX_TM_OWNER  WHERE :p1 = :px   ")
					.setParameter("p1", bycondition)
					.setParameter("px", value)
					.executeUpdate();
			tx.commit();
			em.close();


		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			//System.out.println("Tidak bisa diupdate");

		}

	}
  
}
