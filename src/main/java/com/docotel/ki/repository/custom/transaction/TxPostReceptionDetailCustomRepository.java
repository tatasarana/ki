package com.docotel.ki.repository.custom.transaction;


import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.transaction.TxPostReceptionDetail;
import com.docotel.ki.pojo.KeyValue;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import java.util.ArrayList;
import java.util.List;

@Repository
public class TxPostReceptionDetailCustomRepository extends BaseRepository<TxPostReceptionDetail> {

    public TxPostReceptionDetail selectOne(String by, Object value) {
        return super.selectOne("JOIN FETCH c.txPostReception tpr", by, value, true);
    }

    
    public List<TxPostReceptionDetail> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        return super.selectAll(//"JOIN FETCH txPostReception tpr " +
        					   "LEFT JOIN FETCH c.txTmGeneral ttg " + 
        					   "LEFT JOIN FETCH ttg.txTmBrand " +
        					   "LEFT JOIN FETCH ttg.txRegistration ", searchCriteria, orderBy, orderType, offset, limit);
    }

    public TxPostReceptionDetail selectOneByTtgAppId(String value) {
        try {
            return (TxPostReceptionDetail) entityManager.createQuery(
                    "SELECT c FROM " + getClassName() + " c " +
                            "JOIN FETCH c.txTmGeneral t " +
                            "WHERE c.txTmGeneral.id = :p1")
                    .setParameter("p1", value)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    public TxPostReceptionDetail selectOneByAppId(String value) {
        try {
            return (TxPostReceptionDetail) entityManager.createQuery(
                    "SELECT c FROM " + getClassName() + " c " +
                            "JOIN FETCH c.txPostReception t " +
                            "WHERE c.txPostReception.id = :p1")
                    .setParameter("p1", value)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }
    
    
    public TxPostReceptionDetail selectOneByAppIdLast(String value) {
        try { 
            return (TxPostReceptionDetail) entityManager.createQuery(
            		"SELECT c FROM " + getClassName() + " c " +
                            " JOIN FETCH c.txPostReception t " +  
                            " JOIN FETCH c.txTmGeneral ttg " + 
                            " WHERE c.txTmGeneral.id = :p1 " +
                            " AND (c.mStatus.id = 'TM_PASCA_PERMOHONAN' OR  c.mStatus.id = 'NOTYET')" +
                            " ORDER BY t.createdDate DESC ")
                    .setParameter("p1", value)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    public long countPascaDetail() {
        try {
            return (Long) entityManager.createQuery(
                    "SELECT COUNT(*) FROM " + getClassName() + " c " +
                            " WHERE c.mStatus.id = 'TM_PASCA_PERMOHONAN' OR  c.mStatus.id = 'NOTYET' ")
                            .setMaxResults(1).getSingleResult();
        } catch (NoResultException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return 0;
    }
    
    public TxPostReceptionDetail selectOneByAppIdBeforeLast(String value) {
        try { 
            return (TxPostReceptionDetail) entityManager.createQuery(
            		"SELECT c FROM " + getClassName() + " c " +
                            " JOIN FETCH c.txPostReception t " +  
                            " JOIN FETCH c.txTmGeneral ttg " + 
                            " WHERE c.txTmGeneral.id = :p1 " +
                            " AND c.mStatus.id != 'DONE' " +
                            " ORDER BY t.createdDate DESC ")
                    .setParameter("p1", value)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    public TxPostReceptionDetail selectOneByAppIdBeforeLastGetCetakListMonitor(String value) {
        try {
            return (TxPostReceptionDetail) entityManager.createQuery(
                    "SELECT c FROM " + getClassName() + " c " +
                            " JOIN FETCH c.txPostReception t " +
                            " JOIN FETCH c.txTmGeneral ttg " +
                            " WHERE c.txTmGeneral.id = :p1 " +
//                            " AND c.mStatus.id = 'TM_PASCA_PERMOHONAN' " +
                            " ORDER BY t.createdDate DESC ")
                    .setParameter("p1", value)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException e) {
//            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }
    
	/*public List<TxPostReceptionDetail> listAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
    	try { 
            return (List<TxPostReceptionDetail>) entityManager.createQuery(
            		"SELECT c FROM " + getClassName() + " c " +
                            " JOIN FETCH c.txPostReception t " +  
                            " JOIN FETCH c.txTmGeneral ttg " + 
                            " WHERE c.txTmGeneral.id = :p1 " +
                            " AND c.mStatus.id != 'TM_PASCA_PERMOHONAN' " +
                            " ORDER BY t.createdDate DESC ").getResultList();
        } catch (NoResultException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }*/
	
	public List<TxPostReceptionDetail> selectAllInList() {
        try {
            StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");   	            
            sbQuery.append(" JOIN FETCH c.txPostReception tpr ");    
            sbQuery.append(" JOIN FETCH tpr.mStatus msts ");    
            sbQuery.append(" JOIN FETCH tpr.mFileType cb ");
//            sbQuery.append(" WHERE tpr.mStatus = 'TM_PASCA_PERMOHONAN' ");
//            sbQuery.append(" GROUP BY cb.desc ");

            Query query = entityManager.createQuery(sbQuery.toString());
            return query.getResultList();
        } catch (NoResultException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
	}
	
	public List<KeyValue> selectAllPendingPostReceptionDetail() {
		List<KeyValue> resultList = new ArrayList<>();
		try {
			List<Object[]> objectResultArrList = (List<Object[]>) entityManager.createNativeQuery(" SELECT M_FILE_TYPE.FILE_TYPE_DESC, COUNT(TX_POST_RECEPTION_DETAIL.APPLICATION_ID) " +
					" FROM TX_POST_RECEPTION_DETAIL TX_POST_RECEPTION_DETAIL, TX_POST_RECEPTION TX_POST_RECEPTION " +
					" JOIN M_FILE_TYPE M_FILE_TYPE " +
					"	ON TX_POST_RECEPTION.FILE_TYPE_ID = M_FILE_TYPE.FILE_TYPE_ID " +
					" WHERE TX_POST_RECEPTION_DETAIL.POST_RECEPTION_ID = TX_POST_RECEPTION.POST_RECEPTION_ID " +
					" AND (TX_POST_RECEPTION_DETAIL.STATUS_ID = 'TM_PASCA_PERMOHONAN' OR TX_POST_RECEPTION_DETAIL.STATUS_ID = 'NOTYET' )" +
					" GROUP BY M_FILE_TYPE.FILE_TYPE_DESC ORDER BY M_FILE_TYPE.FILE_TYPE_DESC")
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

}
