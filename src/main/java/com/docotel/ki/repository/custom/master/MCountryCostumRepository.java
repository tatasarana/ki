package com.docotel.ki.repository.custom.master;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.master.MCountry;
import com.docotel.ki.model.transaction.TxTmOwner;
import com.docotel.ki.pojo.KeyValue;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;

@Repository
public class MCountryCostumRepository extends BaseRepository<MCountry> {
	
	 @Override
		public List<MCountry> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
	        try {
	            StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");   	            
	            sbQuery.append("JOIN FETCH c.createdBy cb ");    
	            
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
	            logger.error(e.getMessage(), e);
	        } catch (Exception e) {
	            logger.error(e.getMessage(), e);
	        }

	        return null;
		}

	public MCountry selectOne(String by, Object value) {
		return super.selectOne(by, value, false);
	}
}
