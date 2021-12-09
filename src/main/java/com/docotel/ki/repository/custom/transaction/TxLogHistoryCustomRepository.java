package com.docotel.ki.repository.custom.transaction;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.transaction.TxLogHistory;
import com.docotel.ki.pojo.KeyValue;
import com.ibm.icu.text.SimpleDateFormat;

import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Repository
public class TxLogHistoryCustomRepository extends BaseRepository<TxLogHistory> {
    public TxLogHistory selectOneLastByObjectClassNameAndObjectId(String objectClassName, String objectId) {
        List<KeyValue> searchCriteria = new ArrayList<>();
        searchCriteria.add(new KeyValue("objectClassName", objectClassName, true));
        searchCriteria.add(new KeyValue("objectId", objectId, true));
        return super.selectOne(searchCriteria, "activityDate", "DESC");
    }

    public long countLog(List<KeyValue> searchCriteria) {
    	Timestamp p1 = null;
		Timestamp p2 = null;
		int xx=10; 
		
    	try {
            StringBuffer sbQuery = new StringBuffer("SELECT COUNT(c) FROM " + getClassName() + " c ");
            sbQuery.append("WHERE 1 = 1 ");
            if (searchCriteria != null) {
                for (int i = 0; i < searchCriteria.size(); i++) {
                    String searchBy = searchCriteria.get(i).getKey();
                    Object value = searchCriteria.get(i).getValue();

                    if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                        if(searchBy.equalsIgnoreCase("activityDate")) {                        	
                        	SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
							String sValue = sdf.format(value).toString();
							Date appDate = sdf.parse(sValue);							 
							Calendar calendar = Calendar.getInstance();
							calendar.setTime(appDate);
							calendar.set(Calendar.HOUR, 0);
							calendar.set(Calendar.MINUTE, 0);
							calendar.set(Calendar.SECOND, 0);							
							p1 = new Timestamp(calendar.getTimeInMillis());
							
							calendar.set(Calendar.HOUR, 23);
							calendar.set(Calendar.MINUTE, 59);
							calendar.set(Calendar.SECOND, 59);							
							p2 =  new Timestamp(calendar.getTimeInMillis());
							
							sbQuery.append(" AND c.activityDate >=:p" + xx );	
							xx++;
							sbQuery.append(" AND c.activityDate <=:p" + xx );
                        } else if (searchBy.equalsIgnoreCase("activityDateStart")) {
                            sbQuery.append(" AND c.activityDate >= :p" + i);
                        } else if (searchBy.equalsIgnoreCase("activityDateEnd")) {
                            sbQuery.append(" AND c.activityDate <= :p" + i);
                        } else {
                            if (value instanceof String) {
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
            }

            Query query = entityManager.createQuery(sbQuery.toString());
            if (searchCriteria != null) {
                for (int i = 0; i < searchCriteria.size(); i++) {
                    Object value = searchCriteria.get(i).getValue();
                    String searchBy = searchCriteria.get(i).getKey();
                    
                    if (value != null) {
                    	if("activityDate".equalsIgnoreCase(searchBy)) {
                    		xx=10;
							query.setParameter("p" + xx, p1);
							xx++;
							query.setParameter("p" + xx, p2);
						} else {
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
            }
            return (Long) query.setMaxResults(1).getSingleResult();
        } catch (NoResultException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return 0L;
    }
    
    public List<TxLogHistory> selectAllLog(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
    	Timestamp p1 = null;
		Timestamp p2 = null;
		int xx=10; 
    	
    	try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");			
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						 if(searchBy.equalsIgnoreCase("activityDate")) {                        	
	                        	SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
								String sValue = sdf.format(value).toString();
								Date appDate = sdf.parse(sValue);							 
								Calendar calendar = Calendar.getInstance();
								calendar.setTime(appDate);
								calendar.set(Calendar.HOUR, 0);
								calendar.set(Calendar.MINUTE, 0);
								calendar.set(Calendar.SECOND, 0);							
								p1 = new Timestamp(calendar.getTimeInMillis());
								
								calendar.set(Calendar.HOUR, 23);
								calendar.set(Calendar.MINUTE, 59);
								calendar.set(Calendar.SECOND, 59);							
								p2 =  new Timestamp(calendar.getTimeInMillis());
								
								sbQuery.append(" AND c.activityDate >=:p" + xx );	
								xx++;
								sbQuery.append(" AND c.activityDate <=:p" + xx );
	                        } else {
	                        	if (value == null) {
	    							sbQuery.append(" AND c." + searchBy + " IS NULL");
	    						} else if (value instanceof String) {
	    							if (searchCriteria.get(i).isExactMatch()) {
	    								sbQuery.append(" AND LOWER(c." + searchBy + ") = :p" + j++);
	    							} else {
	    								sbQuery.append(" AND LOWER(c." + searchBy + ") LIKE :p" + j++);
	    							}
	    						} else if (value instanceof List) {
	    							if (((List) value).size() > 0) {
	    								sbQuery.append(" AND LOWER(c." + searchBy + ") IN (");
	    								for (int k = 0; k < ((List) value).size(); k++) {
	    									sbQuery.append(":p" + j++);
	    									if (k < ((List) value).size() - 1) {
	    										sbQuery.append(", ");
	    									}
	    								}
	    								sbQuery.append(")");
	    							}
	    						} else {
	    							sbQuery.append(" AND c." + searchBy + " = :p" + j++);
	    						}
	                        }
					}
				}
			}

			if (orderBy != null && orderType != null) {
				sbQuery.append(" ORDER BY c." + orderBy + " " + orderType);
			}
 
			Query query = entityManager.createQuery(sbQuery.toString());
			if (searchCriteria != null) {
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					Object value = searchCriteria.get(i).getValue();
					String searchBy = searchCriteria.get(i).getKey();
					
					if (value != null) {						
						if("activityDate".equalsIgnoreCase(searchBy)) {
                    		xx=10;
							query.setParameter("p" + xx, p1);
							xx++;
							query.setParameter("p" + xx, p2);
						} else {
							if (value instanceof String) {
								if (searchCriteria.get(i).isExactMatch()) {
									query.setParameter("p" + j++, ((String) value).toLowerCase());
								} else {
									query.setParameter("p" + j++, "%" + ((String) value).toLowerCase() + "%");
								}
							} else if (value instanceof List) {
								for (Object subValue : (List) value) {
									if (subValue instanceof String) {
										query.setParameter("p" + j++, ((String) subValue).toLowerCase());
									} else {
										query.setParameter("p" + j++, subValue);
									}
								}
							} else {
								query.setParameter("p" + j++, value);
							}
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

}
