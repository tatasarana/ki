package com.docotel.ki.repository.custom.master;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.master.MClassDetail;
import com.docotel.ki.model.transaction.TxTmClass;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.service.transaction.TrademarkService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import java.util.List;

@Repository
public class MClassDetailCustomRepository extends BaseRepository<MClassDetail> {

	@Autowired
	TrademarkService trademarkService;

	@Autowired
	private EntityManagerFactory emf;

	@Override
	public List<MClassDetail> selectAll(String joinCriteria, String by, Object value, boolean exactMatch, String orderBy, String orderType, Integer offset, Integer limit) {
		return super.selectAll(joinCriteria, by, value, true, orderBy, orderType, offset, limit);
	}

	public void setujuiSemua(String application_id, String status){

		TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo(application_id);
		application_id = txTmGeneral.getId();



		try{
			EntityManager em = emf.createEntityManager();
			EntityTransaction tx = em.getTransaction();
			tx.begin();

			em.createNativeQuery
					("UPDATE TX_TM_CLASS SET TM_CLASS_STATUS = :p1 WHERE APPLICATION_ID = :px   ")
					.setParameter("p1", status)
					.setParameter("px", application_id)
					.executeUpdate();
					tx.commit();
					em.close();


		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
			//System.out.println("Tidak bisa diupdate");

		}

	}

	public TxTmClass selectOne(String joinCriteria, String[] by, Object[] value) {
		StringBuffer sbQuery = new StringBuffer(joinCriteria);
		sbQuery.append(" WHERE 1 = 1 ");
		for (int i = 0; i < value.length;  i++) {
			sbQuery.append(" AND " + by[i] + " = p" + i + " ");
		}

		Query query = entityManager.createQuery(sbQuery.toString());
		for (int i = 0; i < value.length;  i++) {
			query.setParameter("p"+i, value[i]);
		}

		return (TxTmClass) query.getSingleResult();
	}

   /* public List<MClassDetail> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        return super.selectAll(searchCriteria,orderBy,orderType,offset,limit);
    }*/

	public List<MClassDetail> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		return selectAll("JOIN FETCH c.parentClass pc JOIN FETCH c.createdBy cb", searchCriteria, orderBy, orderType, offset, limit);
	}
	
	public long countAllExclude(List<KeyValue> searchCriteria, String[] exclude) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT COUNT(c) FROM " + getClassName() + " c ");
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (value == null) {
							sbQuery.append(" AND c." + searchBy + " IS NULL");
						} else if (value instanceof String) {
							if (searchCriteria.get(i).isExactMatch()) {
								sbQuery.append(" AND LOWER(c." + searchBy + ") = :p" + i);
							} else {
								sbQuery.append(" AND LOWER(c." + searchBy + ") LIKE :p" + i);
							}
						} else {
							sbQuery.append(" AND c." + searchBy + " = :p" + i);
						}
					}
				}
			}

			if(exclude != null) {
				// ORACLE 1000 limit!
				int oracleLimit = 1000;
				String excludeList = " AND (";
				for (int oID = 0; oID <= exclude.length/oracleLimit; oID++) {
					if (oracleLimit * oID == exclude.length) {
						break;
					}
					String newWhereClause = (oID==0?"":" OR ")+" c.id NOT IN (";
					for (int k = 0; k < (exclude.length>(oracleLimit*(oID+1))?oracleLimit:exclude.length); k++) {
						if (k+(oracleLimit*oID) >= exclude.length){
							break;
						}
						newWhereClause += (k==0?"":",")+"'" + exclude[k+(oracleLimit*oID)] + "'";
					}
					excludeList += newWhereClause + ")";
				}
				sbQuery.append(excludeList+")");
			}

			Query query = entityManager.createQuery(sbQuery.toString());
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					Object value = searchCriteria.get(i).getValue();
					if (value != null) {
						if (value instanceof String) {
							if (searchCriteria.get(i).isExactMatch()) {
								query.setParameter("p" + i, ((String) value).toLowerCase());
							} else {
								query.setParameter("p" + i, "%" + ((String) value).toLowerCase() + "%");
							}
						} else {
							query.setParameter("p" + i, value);
						}
					}
				}
			}
			return (Long) query.setMaxResults(1).getSingleResult();
		} catch (NoResultException e) {
//			logger.error(e.getMessage(), e);
		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
		}

		return 0L;
	}

	public List<MClassDetail> selectAllExclude(String joinCriteria, List<KeyValue> searchCriteria, String[] exclude, String orderBy, String orderType, Integer offset, Integer limit) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
			if (StringUtils.isNotBlank(joinCriteria)) {
				sbQuery.append(joinCriteria);
				sbQuery.append(" ");
			}
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (value == null) {
							sbQuery.append(" AND c." + searchBy + " IS NULL");
						} else if (value instanceof String) {
							if (searchCriteria.get(i).isExactMatch()) {
								sbQuery.append(" AND LOWER(c." + searchBy + ") = :p" + i);
							} else {
								sbQuery.append(" AND LOWER(c." + searchBy + ") LIKE :p" + i);
							}
						} else {
							sbQuery.append(" AND c." + searchBy + " = :p" + i);
						}
					}
				}
			}

			if(exclude != null) {
				// ORACLE 1000 limit!
				int oracleLimit = 1000;
				String excludeList = " AND (";
				for (int oID = 0; oID <= exclude.length/oracleLimit; oID++) {
					if (oracleLimit * oID == exclude.length) {
						break;
					}
					String newWhereClause = (oID==0?"":" OR ")+" c.id NOT IN (";
					for (int k = 0; k < (exclude.length>(oracleLimit*(oID+1))?oracleLimit:exclude.length); k++) {
						if (k+(oracleLimit*oID) >= exclude.length){
							break;
						}
						newWhereClause += (k==0?"":",")+"'" + exclude[k+(oracleLimit*oID)] + "'";
					}
					excludeList += newWhereClause + ")";
				}
				sbQuery.append(excludeList+")");
			}
			if (orderBy != null && orderType != null) {
				sbQuery.append(" ORDER BY c." + orderBy + " " + orderType);
			}

			Query query = entityManager.createQuery(sbQuery.toString());
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					Object value = searchCriteria.get(i).getValue();
					if (value != null) {
						if (value instanceof String) {
							if (searchCriteria.get(i).isExactMatch()) {
								query.setParameter("p" + i, ((String) value).toLowerCase());
							} else {
								query.setParameter("p" + i, "%" + ((String) value).toLowerCase() + "%");
							}
						} else {
							query.setParameter("p" + i, value);
						}
					}
				}
			}

			if (offset != null) {
				query.setFirstResult(offset);
			}
			if (limit != null) {
				query.setMaxResults(limit);
			}
			return query.getResultList();
		} catch (NoResultException e) {
//			logger.error(e.getMessage(), e);
		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
		}

		return null;
	}
	
	public List<MClassDetail> selectAllExcludeLimitation(String joinCriteria, List<KeyValue> searchCriteria, String[] exclude, String orderBy, String orderType, Integer offset, Integer limit) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
			if (StringUtils.isNotBlank(joinCriteria)) {
				sbQuery.append(joinCriteria);
				sbQuery.append(" ");
			}
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (value == null) {
							sbQuery.append(" AND c." + searchBy + " IS NULL");
						} else if (value instanceof String) {
							if(searchBy.equals("g.applicationNo")) {
								sbQuery.append(" AND ID IN (SELECT cl.id FROM TxTmGeneral g JOIN FETCH g.txTmClassList cl WHERE g.applicationNo = :p)");
							} else if (searchCriteria.get(i).isExactMatch()) {
								sbQuery.append(" AND LOWER(c." + searchBy + ") = :p" + i);
							} else {
								sbQuery.append(" AND LOWER(c." + searchBy + ") LIKE :p" + i);
							}
						} else {
							sbQuery.append(" AND c." + searchBy + " = :p" + i);
						}
					}
				}
			}

			if(exclude != null) {
				// ORACLE 1000 limit!
				int oracleLimit = 1000;
				String excludeList = " AND (";
				for (int oID = 0; oID <= exclude.length/oracleLimit; oID++) {
					if (oracleLimit * oID == exclude.length) {
						break;
					}
					String newWhereClause = (oID==0?"":" OR ")+" c.id NOT IN (";
					for (int k = 0; k < (exclude.length>(oracleLimit*(oID+1))?oracleLimit:exclude.length); k++) {
						if (k+(oracleLimit*oID) >= exclude.length){
							break;
						}
						newWhereClause += (k==0?"":",")+"'" + exclude[k+(oracleLimit*oID)] + "'";
					}
					excludeList += newWhereClause + ")";
				}
				sbQuery.append(excludeList+")");
			}
			if (orderBy != null && orderType != null) {
				sbQuery.append(" ORDER BY c." + orderBy + " " + orderType);
			}

			Query query = entityManager.createQuery(sbQuery.toString());
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					Object value = searchCriteria.get(i).getValue();
					if (value != null) {
						if (value instanceof String) {
							if (searchCriteria.get(i).isExactMatch()) {
								query.setParameter("p" + i, ((String) value).toLowerCase());
							} else {
								query.setParameter("p" + i, "%" + ((String) value).toLowerCase() + "%");
							}
						} else {
							query.setParameter("p" + i, value);
						}
					}
				}
			}

			if (offset != null) {
				query.setFirstResult(offset);
			}
			if (limit != null) {
				query.setMaxResults(limit);
			}
			return query.getResultList();
		} catch (NoResultException e) {
//			logger.error(e.getMessage(), e);
		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
		}

		return null;
	}

	public List<MClassDetail> selectAllByClassHeader(String by, Object value, Integer offset, Integer limit) {
		return super.selectAll("LEFT JOIN FETCH c.parentClass p", by, value, true, offset, limit);
	}

	public void updateInactive(String by, String value) {
		try {
			StringBuffer sbQuery = new StringBuffer("UPDATE " + getClassName() + " c SET c.statusFlag = :p0 ");
			sbQuery.append("WHERE LOWER(c." + by + ") = :p1");

			Query query = entityManager.createQuery(sbQuery.toString());
			query.setParameter("p0", false);
			query.setParameter("p1", value.toLowerCase());
			query.executeUpdate();
		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
		}
	}

	public int searchRawQueryCount(String stringinput){
		try {
			Query query = entityManager.createNativeQuery("SELECT COUNT(*) FROM ("+stringinput+")");
			int count = Integer.parseInt(query.getSingleResult().toString());
			if(count > 0){
				return count;
			}
		}
		catch (Exception e){
//			logger.error(e.getMessage(), e);
		}
		return 0;
	}

	public List<Object[]> searchRawQuery(String stringinput, Integer offset, Integer limit){
		if (limit == 0) {
			limit = null;
		}
		try {
			Query query = entityManager.createNativeQuery(stringinput);
			if (offset!=null){
				query.setFirstResult(offset);
			}
			if(limit!=null){
				query.setMaxResults(limit);
			}
			return query.getResultList();
		}
		catch (Exception e){
//			logger.error(e.getMessage(), e);
		}

		return null ;
	}

	public long countRawQuery(List<KeyValue> searchCriteria, String[] exclude, String stringinput) {

		return 0 ;
	}

	public void reIndex(){
		entityManager.createNativeQuery("BEGIN CTX_DDL.SYNC_INDEX('INDEX_CLASS_DETAIL', '2M'); END;").executeUpdate();
		entityManager.createNativeQuery("BEGIN CTX_DDL.SYNC_INDEX('INDEX_CLASS_DETAIL_EN', '2M'); END;").executeUpdate();
	}
}
