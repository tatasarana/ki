package com.docotel.ki.repository.custom.transaction;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.transaction.TxServDist;
import com.docotel.ki.pojo.KeyValue;
import org.springframework.stereotype.Repository;

@Repository
public class TxServDistCustomRepository extends BaseRepository<TxServDist> {
	@Override
	public List<TxServDist> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		try {
			Timestamp p1 = null;
			Timestamp p2 = null;		
			int xx = 10; 
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");		
				sbQuery.append("LEFT JOIN FETCH c.txGroup tg ");
				sbQuery.append("LEFT JOIN FETCH c.mRoleSubstantifDetail msd ");				
				sbQuery.append("LEFT JOIN FETCH msd.mRoleSubstantif ms ");
				sbQuery.append("LEFT JOIN FETCH msd.mEmployee me ");
				sbQuery.append("LEFT JOIN FETCH me.userId mu ");
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if(searchBy.equalsIgnoreCase("createdDate")) {
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
							
							sbQuery.append(" AND c.createdDate >=:p" + xx );	
							xx++;
							sbQuery.append(" AND c.createdDate <=:p" + xx );
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
					if (value != null ) {
						if("createdDate".equalsIgnoreCase(searchBy)) {
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
	
	
	public long countAll(List<KeyValue> searchCriteria) {
		try {
			Timestamp p1 = null;
			Timestamp p2 = null;		
			int j = 10;
			StringBuffer sbQuery = new StringBuffer("SELECT COUNT(c) FROM " + getClassName() + " c ");
			
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if(searchBy.equalsIgnoreCase("createdDate")) {
							/*sbQuery.append(" AND (c.createdDate >='" + value.toString().concat(" 00:00") + "' "
									  + "AND c.createdDate <='" + value.toString().concat(" 23:59")+"' ) " );	*/
                            try {
                            Calendar calendar = Calendar.getInstance();
                            if (value instanceof Timestamp) {
                                calendar.setTimeInMillis(((Timestamp) value).getTime());
                            } else {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                                calendar.setTime(sdf.parse(value.toString()));
                            }

//							SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
							//String sValue = sdf.format(value).toString();
//							Date appDate = sdf.parse(value.toString());
//							Calendar calendar = Calendar.getInstance();
//							calendar.setTime(appDate);
							calendar.set(Calendar.HOUR, 0);
							calendar.set(Calendar.MINUTE, 0);
							calendar.set(Calendar.SECOND, 0);							
							p1 = new Timestamp(calendar.getTimeInMillis());
							
//							calendar.set(Calendar.HOUR, 23);
//							calendar.set(Calendar.MINUTE, 59);
//							calendar.set(Calendar.SECOND, 59);
//							p2 =  new Timestamp(calendar.getTimeInMillis());
//                            sbQuery.append(" AND TRUNC(c." + searchBy + ") >= :postDate");

//                            sbQuery.append(" AND TRUNC(c.createdDate) >=:p1"  );
                            sbQuery.append(" AND TRUNC(c." + searchBy + ") >= :p1");

//							j++;
//							sbQuery.append(" AND c.createdDate <=:p" + j );
                            } catch (ParseException e) {
//									System.out.println(" " + e);
                            }
						} else if (searchBy.equalsIgnoreCase("applicationNoGroup")) {
							sbQuery.append(" AND c.txGroup IN (SELECT tgd.txGroup FROM TxGroupDetail tgd LEFT JOIN tgd.txTmGeneral ttg WHERE LOWER(ttg.applicationNo) LIKE :p" + i + ")");
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
					if (value != null) {
                        if (value instanceof String) {
                            if (searchCriteria.get(i).getKey().equalsIgnoreCase("createdDate")) {

//                            if("createdDate".equalsIgnoreCase(searchBy)) {
//							j=10;
//							query.setParameter("p" + j, p1);
//							j++;
//							query.setParameter("p" + j, p2);
                                if (p1 != null) {
                                    query.setParameter("createdDate", p1);
                                }
                            }
//						else {
//							if (value instanceof String) {
                            else if (searchCriteria.get(i).isExactMatch()) {
                                query.setParameter("p" + i, ((String) value).toLowerCase());
                            } else {
                                query.setParameter("p" + i, "%" + ((String) value).toLowerCase() + "%");
                            }
//							}
                        }
							else {
                            if (searchCriteria.get(i).getKey().equalsIgnoreCase("createdDate")) {
                                if (p1 != null) {
                                    query.setParameter("p1", p1);
                                }
                            } else {
                                query.setParameter("p" + i, value);
                            }
//						}
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
	
	
	 
	public List<TxServDist> selectExpedisi(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		return super.selectAll("LEFT JOIN FETCH c.createdBy cb "
				+ "LEFT JOIN FETCH cb.mUser mus "
				+ "LEFT JOIN FETCH mus.mEmployee mem "
				+ "LEFT JOIN FETCH c.txGroup tg"
				+ " LEFT JOIN FETCH c.mRoleSubstantifDetail msd"
				+ " LEFT JOIN FETCH msd.mRoleSubstantif ms"
				+ " LEFT JOIN FETCH msd.mEmployee me"
				+ " LEFT JOIN FETCH me.userId mu"
				+ " ", searchCriteria, orderBy, orderType, offset, limit);
	}
	
	public TxServDist selectOne(String by, Object value) {
		return super.selectOne(" LEFT JOIN FETCH c.txGroup tg"
				+ " LEFT JOIN FETCH c.mRoleSubstantifDetail msd"
				+ " LEFT JOIN FETCH msd.mRoleSubstantif ms"
				+ " LEFT JOIN FETCH msd.mEmployee me"
				+ " LEFT JOIN FETCH me.userId mu", by, value, true);
	}

	public Integer countTxSubsCheckForGroupDistId(String distId) {
		String nativeQuery = "SELECT count(c.CHECK_ID) FROM TX_SERV_DIST a " +
				"LEFT JOIN TX_GROUP_DETAIL b ON a.GROUP_ID=b.GROUP_ID " +
				"LEFT JOIN  TX_SUBS_CHECK c  ON b.APPLICATION_ID=c.APPLICATION_ID " +
				"WHERE a.DIST_ID = :distId";

		Query query = entityManager.createNativeQuery(nativeQuery);
		query.setParameter("distId", distId);
		List<Object> result = query.getResultList();
		return Integer.parseInt(result.get(0).toString());
	}
}
