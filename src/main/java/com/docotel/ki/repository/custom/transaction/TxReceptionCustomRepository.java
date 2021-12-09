package com.docotel.ki.repository.custom.transaction;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.transaction.TxReception;
import com.docotel.ki.pojo.KeyValue;
import com.ibm.icu.text.SimpleDateFormat;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Repository
public class TxReceptionCustomRepository extends BaseRepository <TxReception> {

	public TxReception selectOne(String by, Object value) {
		return super.selectOne("LEFT JOIN FETCH c.createdBy cb "
				+ "LEFT JOIN FETCH c.mFileSequence ms "
				+ "LEFT JOIN FETCH c.mFileType ft "
				+ "LEFT JOIN FETCH c.mFileTypeDetail ftc "
				+ " ", by, value, true);
	}

	/*@Override
	public List<TxReception> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		return super.selectAll("LEFT JOIN FETCH c.mFileSequence s "
				+ "LEFT JOIN FETCH c.mFileType t " 
				+ "LEFT JOIN FETCH c.mFileTypeDetail tc", searchCriteria, orderBy, orderType, offset, limit);
	}*/
		
	public List<TxReception> selectAllReception(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		Timestamp p1 = null;
		Timestamp p2 = null;
		int xx=10; 

		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
			sbQuery.append("LEFT JOIN FETCH c.mFileSequence s  ");
			sbQuery.append("LEFT JOIN FETCH c.mFileType t ");
			sbQuery.append("LEFT JOIN FETCH c.mFileTypeDetail tc ");
			sbQuery.append("WHERE 1 = 1");

			if (searchCriteria != null) {
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (searchBy.equalsIgnoreCase("applicationDate")) {
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
							
							sbQuery.append(" AND c.applicationDate >=:p" + xx );	
							xx++;
							sbQuery.append(" AND c.applicationDate <=:p" + xx );
							
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
    							sbQuery.append(" AND LOWER(c." + searchBy + ") IN (");
    							for (int k = 0; k < ((List) value).size(); k++) {
    								sbQuery.append(":p" + j++);
    								if (k < ((List) value).size() - 1) {
    									sbQuery.append(", ");
    								}
    							}
    							sbQuery.append(")");
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
						if("applicationDate".equalsIgnoreCase(searchBy)) {
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
	
	public List<TxReception> SelectDaftarBRM(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		return super.selectAll("LEFT JOIN FETCH c.mFileSequence s " 
				+ "LEFT JOIN FETCH c.mFileTypeDetail tc "
				+ "", searchCriteria, orderBy, orderType, offset, limit);
	}
	
	
	public long countReception(String joinCriteria, List<KeyValue> searchCriteria) {
		try {
			Timestamp p1 = null;
			Timestamp p2 = null;
			int j=10;
			StringBuffer sbQuery = new StringBuffer("SELECT COUNT(c) FROM " + getClassName() + " c ");
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
						if (searchBy.equalsIgnoreCase("applicationDate")) {
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
							
							sbQuery.append(" AND c.applicationDate >=:p" + j );	
							j++;
							sbQuery.append(" AND c.applicationDate <=:p" + j );
							
                        } else {
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
			} 
			

			Query query = entityManager.createQuery(sbQuery.toString());
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {					
					Object value = searchCriteria.get(i).getValue();	
					String searchBy = searchCriteria.get(i).getKey();
					if (value != null ) {
						if("applicationDate".equalsIgnoreCase(searchBy)) {
							j=10;
							query.setParameter("p" + j, p1);
							j++;
							query.setParameter("p" + j, p2);
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
	 
}
