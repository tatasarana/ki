package com.docotel.ki.repository.custom.transaction;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.transaction.TxGroupDetail;
import com.docotel.ki.model.transaction.TxTmBrand;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.util.DateUtil;
import org.springframework.stereotype.Repository;

@Repository
public class TxTmBrandCustomRepository extends BaseRepository<TxTmBrand> {
    //	add by deni
    public TxTmBrand selectOne(String by, Object value) {
        return super.selectOne("LEFT JOIN FETCH c.createdBy cb "
                + "LEFT JOIN FETCH c.txTmGeneral tg "
                + "LEFT JOIN FETCH c.mBrandType bt "
                + " ", by, value, false);
    }
// end deni

	public long countPhoneticCheck(List<KeyValue> searchCriteria) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT COUNT(c) FROM " + getClassName() + " c ");
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if(value != null){
							sbQuery.append(" AND LOWER(c." + searchBy + ") LIKE :p" + i);
						}
					}
				}
			}
//			System.out.println("sql_dump " + sbQuery.toString());
			Query query = entityManager.createQuery(sbQuery.toString());
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					Object value = searchCriteria.get(i).getValue();
					String searchBy = searchCriteria.get(i).getKey();
					if (value != null) {
						query.setParameter("p" + i, "%" + ((String) value).toLowerCase() + "%");
					}
				}
			}
			return (Long) query.setMaxResults(1).getSingleResult();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return 0L;
	}

	public List<TxTmBrand> selectPhoneticData(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if(value != null){
							sbQuery.append(" AND LOWER(c." + searchBy + ") LIKE :p" + i);
						}
					}
				}
			}
			/*if (orderBy != null && orderType != null) {
				if (orderBy.equalsIgnoreCase("mClass.no")) {
					sbQuery.append(" ORDER BY mc.no " + orderType);
				} else if (orderBy.equalsIgnoreCase("txTmGeneral.txTmPriorList.priorDate")) {
					sbQuery.append(" ORDER BY ttp.priorDate " + orderType);
				} else if (orderBy.equalsIgnoreCase("txTmGeneral.txTmOwner.name")) {
					sbQuery.append(" ORDER BY tto.name " + orderType);
				} else if (orderBy.equalsIgnoreCase("groupDetailStatus")) {
					sbQuery.append(" ORDER BY c.status " + orderType);
				} else if (orderBy.equalsIgnoreCase("mUserCurrent.username")) {
					//sbQuery.append(" ORDER BY c.status " + orderType);
				} else {
					sbQuery.append(" ORDER BY c." + orderBy + " " + orderType);
				}
			}*/

			Query query = entityManager.createQuery(sbQuery.toString());
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					Object value = searchCriteria.get(i).getValue();
					String searchBy = searchCriteria.get(i).getKey();
					if (value != null) {
						query.setParameter("p" + i, "%" + ((String) value).toLowerCase() + "%");
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
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}
    /*public List<Object[]> selectAllTxTmBrandList(List<String> brandIdList) {
		String queryS = "SELECT TM_BRAND_ID, TM_BRAND_NAME, BRAND_TYPE_NAME, APPLICATION_NO, " +
				"STATUS_NAME, LISTAGG(CLASS_NO, ', ') WITHIN GROUP (ORDER BY rownum) CLASS_NO," +
				"LISTAGG(TM_OWNER_NAME, ', ') WITHIN GROUP (ORDER BY rownum) TM_OWNER_NAME " +
				"FROM (" +
				"    SELECT tb.TM_BRAND_ID, tb.TM_BRAND_NAME, mb.BRAND_TYPE_NAME, tg.APPLICATION_NO, mc.CLASS_NO, tto.TM_OWNER_NAME, ms.STATUS_NAME " +
				"    from TX_TM_BRAND tb" +
				"    LEFT JOIN M_BRAND_TYPE mb on mb.BRAND_TYPE_ID = tb.BRAND_TYPE_ID" +
				"    LEFT JOIN TX_TM_GENERAL tg on tg.APPLICATION_ID = tb.APPLICATION_ID" +
				"    LEFT JOIN TX_TM_CLASS tc ON tc.APPLICATION_ID = tg.APPLICATION_ID" +
				"    LEFT JOIN M_CLASS mc ON mc.CLASS_ID = tc.CLASS_ID" +
				"    LEFT JOIN TX_TM_OWNER tto ON tto.APPLICATION_ID = tg.APPLICATION_ID" +
				"    LEFT JOIN M_STATUS ms ON ms.STATUS_ID = tg.STATUS_ID" +
				"    where tb.TM_BRAND_ID IN (:brandIdList) " +
				"    group by tb.TM_BRAND_ID, tb.TM_BRAND_NAME, mb.BRAND_TYPE_NAME, tg.APPLICATION_NO, mc.CLASS_NO, tto.TM_OWNER_NAME, ms.STATUS_NAME) X " +
				"    group by TM_BRAND_ID, TM_BRAND_NAME, BRAND_TYPE_NAME, APPLICATION_NO, STATUS_NAME";
		Query query = entityManager.createNativeQuery(queryS)
				.setParameter("brandIdList", brandIdList);
		return query.getResultList();
	}*/ 
      
    /*public List<Object[]> selectAllTxTmBrandList(List<String> brandIdList) {
    	String sbQuery2 =  "tb.TM_BRAND_ID IN (:brandIdList) ";
		String queryS = "SELECT TM_BRAND_ID, TM_BRAND_NAME, BRAND_TYPE_NAME, APPLICATION_NO, " +
				"STATUS_NAME, LISTAGG(CLASS_NO, ', ') WITHIN GROUP (ORDER BY rownum) CLASS_NO," +
				"LISTAGG(TM_OWNER_NAME, ', ') WITHIN GROUP (ORDER BY rownum) TM_OWNER_NAME " +
				"FROM (" +
				"    SELECT tb.TM_BRAND_ID, tb.TM_BRAND_NAME, mb.BRAND_TYPE_NAME, tg.APPLICATION_NO, mc.CLASS_NO, tto.TM_OWNER_NAME, ms.STATUS_NAME " +
				"    from TX_TM_BRAND tb" +
				"    LEFT JOIN M_BRAND_TYPE mb on mb.BRAND_TYPE_ID = tb.BRAND_TYPE_ID" +
				"    LEFT JOIN TX_TM_GENERAL tg on tg.APPLICATION_ID = tb.APPLICATION_ID" +
				"    LEFT JOIN TX_TM_CLASS tc ON tc.APPLICATION_ID = tg.APPLICATION_ID" +
				"    LEFT JOIN M_CLASS mc ON mc.CLASS_ID = tc.CLASS_ID" +
				"    LEFT JOIN TX_TM_OWNER tto ON tto.APPLICATION_ID = tg.APPLICATION_ID" +
				"    LEFT JOIN M_STATUS ms ON ms.STATUS_ID = tg.STATUS_ID" +
				"    where " + sbQuery2.replaceAll(", $", "") +
				"    group by tb.TM_BRAND_ID, tb.TM_BRAND_NAME, mb.BRAND_TYPE_NAME, tg.APPLICATION_NO, mc.CLASS_NO, tto.TM_OWNER_NAME, ms.STATUS_NAME) X " +
				"    group by TM_BRAND_ID, TM_BRAND_NAME, BRAND_TYPE_NAME, APPLICATION_NO, STATUS_NAME";
		Query query = entityManager.createNativeQuery(queryS)
				.setParameter("brandIdList", brandIdList);
		return query.getResultList();
	}*/
    
    public List<Object[]> selectAllTxTmBrandList(List<String> brandIdList) {
    	StringBuffer sbQuery = new StringBuffer();
    	if(brandIdList.size() > 0) {
    		for(int id = 0; id < brandIdList.size(); id++) {
    			sbQuery.append("tb.TM_BRAND_ID = '" + brandIdList.get(id) +"' OR ");
    			if(!(id + 1 < brandIdList.size())) {
    				sbQuery.append("tb.TM_BRAND_ID = '" + brandIdList.get(id) +"')");
    			}
    		}
    	} else {
    		sbQuery.append("tb.TM_BRAND_ID = '')");
    	}
    	
		String queryS = "SELECT TM_BRAND_ID, TM_BRAND_NAME, BRAND_TYPE_NAME, APPLICATION_NO, " +
				"STATUS_NAME, LISTAGG(CLASS_NO, ', ') WITHIN GROUP (ORDER BY rownum) CLASS_NO," +
				"LISTAGG(TM_OWNER_NAME, ', ') WITHIN GROUP (ORDER BY rownum) TM_OWNER_NAME " +
				"FROM (" +
				"    SELECT tb.TM_BRAND_ID, tb.TM_BRAND_NAME, mb.BRAND_TYPE_NAME, tg.APPLICATION_NO, mc.CLASS_NO, tto.TM_OWNER_NAME, ms.STATUS_NAME " +
				"    from TX_TM_BRAND tb" +
				"    LEFT JOIN M_BRAND_TYPE mb on mb.BRAND_TYPE_ID = tb.BRAND_TYPE_ID" +
				"    LEFT JOIN TX_TM_GENERAL tg on tg.APPLICATION_ID = tb.APPLICATION_ID" +
				"    LEFT JOIN TX_TM_CLASS tc ON tc.APPLICATION_ID = tg.APPLICATION_ID" +
				"    LEFT JOIN M_CLASS mc ON mc.CLASS_ID = tc.CLASS_ID" +
				"    LEFT JOIN TX_TM_OWNER tto ON tto.APPLICATION_ID = tg.APPLICATION_ID" +
				"    LEFT JOIN M_STATUS ms ON ms.STATUS_ID = tg.STATUS_ID" +
				"    WHERE (" + sbQuery + 
				"    group by tb.TM_BRAND_ID, tb.TM_BRAND_NAME, mb.BRAND_TYPE_NAME, tg.APPLICATION_NO, mc.CLASS_NO, tto.TM_OWNER_NAME, ms.STATUS_NAME) X " +
				"    group by TM_BRAND_ID, TM_BRAND_NAME, BRAND_TYPE_NAME, APPLICATION_NO, STATUS_NAME";
		Query query = entityManager.createNativeQuery(queryS);
		return query.getResultList();
	}
}
