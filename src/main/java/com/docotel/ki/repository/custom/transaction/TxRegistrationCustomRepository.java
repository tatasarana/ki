package com.docotel.ki.repository.custom.transaction;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.transaction.TxGroupDetail;
import com.docotel.ki.model.transaction.TxRegistration;
import com.docotel.ki.model.transaction.TxRegistrationDetail;
import com.docotel.ki.pojo.KeyValue;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class TxRegistrationCustomRepository extends BaseRepository<TxRegistration> {
	@Override
	public List<TxRegistration> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        try {
            StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
            sbQuery.append("LEFT JOIN FETCH c.txTmGeneral ttg ");
            sbQuery.append("LEFT JOIN FETCH ttg.txTmBrand ttb ");          
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

	public long countPermohonan(List<KeyValue> searchCriteria) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT COUNT(c) FROM " + getClassName() + " c ");
			sbQuery.append("WHERE 1 = 1 ");
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
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return 0L;
	}
	
	public TxRegistration selectActiveRegNo(String application_id, boolean state) {
        try {
            return (TxRegistration) entityManager.createQuery(
                    "SELECT c FROM " + getClassName() + " c " +
                            "JOIN FETCH c.txTmGeneral t " +
                            "WHERE c.txTmGeneral.id = :p1 AND c.status = :p2")
                    .setParameter("p1", application_id)
                    .setParameter("p2", state)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    public long countSertifikatDigital(List<KeyValue> searchCriteria) {
        String joinCriteria = null;
        try {
            StringBuffer sbQuery = new StringBuffer("SELECT COUNT(c) FROM " + getClassName() + " c ");
            if (StringUtils.isNotBlank(joinCriteria)) {
                sbQuery.append(joinCriteria);
                sbQuery.append(" ");
            }
            sbQuery.append("WHERE c.downloadFlag = true "
            		+ "AND c.txTmGeneral.mStatus.id NOT LIKE '229' "
            		+ "AND ( c.txTmGeneral.filingDate NOT BETWEEN TO_DATE('2010-01-01', 'YYYY-MM-DD') AND TO_DATE('2014-07-03', 'YYYY-MM-DD'))");
            if (searchCriteria != null) {
            	int j = 0;
                for (int i = 0; i < searchCriteria.size(); i++) {
                    String searchBy = searchCriteria.get(i).getKey();
                    Object value = searchCriteria.get(i).getValue();
                    if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                    	if (value instanceof List) {
							if (((List) value).size() > 0) {
								sbQuery.append(" AND (");
								// ORACLE 1000 limit!
								int oracleLimit = 1000;
								for (int oID = 0; oID <= ((List) value).size()/oracleLimit; oID++) {
									if (oracleLimit * oID == ((List) value).size()) {
										break;
									}
									String newWhereClause = (oID==0?"":" OR ")+" LOWER(c." + searchBy + ") IN (";
									for (int k = 0; k < (((List) value).size()>(oracleLimit*(oID+1))?oracleLimit:((List) value).size()); k++) {
										if (k+(oracleLimit*oID) >= ((List) value).size()){
											break;
										}
										newWhereClause += ((k==0?"":",")+":p" + j++);
									}
									sbQuery.append(newWhereClause+")");
								}
								sbQuery.append(")");
							}
						} else {
							if (i == 0) {
	                            sbQuery.append(" AND ( LOWER(c." + searchBy + ") LIKE :p" + j++);
	                        } else {
	                            sbQuery.append(" OR LOWER(c." + searchBy + ") LIKE :p" + j++);
	                        }
	                        if (i == searchCriteria.size() - 1) {
	                            sbQuery.append(")");
	                        }
						}                        
                    }
                }
            }

            Query query = entityManager.createQuery(sbQuery.toString());
            if (searchCriteria != null) {
            	int j = 0;
                for (int i = 0; i < searchCriteria.size(); i++) {
                    Object value = searchCriteria.get(i).getValue();
                    if (value != null) {
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
            return (Long) query.setMaxResults(1).getSingleResult();
        } catch (NoResultException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return 0L;
    }


    public long countPublikasi(List<KeyValue> searchCriteria) {
        String joinCriteria = null;
        try {
            StringBuffer sbQuery = new StringBuffer("SELECT COUNT(c) FROM " + getClassName() + " c ");
            if (StringUtils.isNotBlank(joinCriteria)) {
                sbQuery.append(joinCriteria);
                sbQuery.append(" ");
            }
//            sbQuery.append("WHERE c.downloadFlag = true AND c.createdDate > TO_DATE('2020-05-02', 'YYYY-MM-DD') AND  c.txTmGeneral.mStatus.id NOT LIKE '229' AND ( c.txTmGeneral.filingDate NOT BETWEEN TO_DATE('2010-01-01', 'YYYY-MM-DD') AND TO_DATE('2014-07-03', 'YYYY-MM-DD'))");
            if (searchCriteria != null) {
                for (int i = 0; i < searchCriteria.size(); i++) {
                    String searchBy = searchCriteria.get(i).getKey();
                    if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                        if (i == 0) {
                            sbQuery.append(" AND ( LOWER(c." + searchBy + ") LIKE :p" + i);
                        } else {
                            sbQuery.append(" OR LOWER(c." + searchBy + ") LIKE :p" + i);
                        }
                        if (i == searchCriteria.size() - 1) {
                            sbQuery.append(")");
                        }
                    }
                }
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
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return 0L;
    }

    public List<TxRegistration> selectAllSertifikatDigital(String joinCriteria, List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        try {
            StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
            if (StringUtils.isNotBlank(joinCriteria)) {
                sbQuery.append(joinCriteria);
                sbQuery.append(" ");
            }
            // AND c.createdDate > TO_DATE('2020-05-02', 'YYYY-MM-DD') di takeout (request fitria)
            sbQuery.append("WHERE c.downloadFlag = true "
            		+ "AND c.txTmGeneral.mStatus.id NOT LIKE '229' "
            		+ "AND (c.txTmGeneral.filingDate NOT BETWEEN TO_DATE('2010-01-01', 'YYYY-MM-DD') AND TO_DATE('2014-07-03', 'YYYY-MM-DD'))");
            if (searchCriteria != null) {
            	int j = 0;
                for (int i = 0; i < searchCriteria.size(); i++) {                	
                    String searchBy = searchCriteria.get(i).getKey();
                    Object value = searchCriteria.get(i).getValue();
                    if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                    	if (value instanceof List) {
							if (((List) value).size() > 0) {
								sbQuery.append(" AND (");
								// ORACLE 1000 limit!
								int oracleLimit = 1000;
								for (int oID = 0; oID <= ((List) value).size()/oracleLimit; oID++) {
									if (oracleLimit * oID == ((List) value).size()) {
										break;
									}
									String newWhereClause = (oID==0?"":" OR ")+" LOWER(c." + searchBy + ") IN (";
									for (int k = 0; k < (((List) value).size()>(oracleLimit*(oID+1))?oracleLimit:((List) value).size()); k++) {
										if (k+(oracleLimit*oID) >= ((List) value).size()){
											break;
										}
										newWhereClause += ((k==0?"":",")+":p" + j++);
									}
									sbQuery.append(newWhereClause+")");
								}
								sbQuery.append(")");
							}
						} else {
							if (i == 0) {
	                            sbQuery.append(" AND ( LOWER(c." + searchBy + ") LIKE :p" + j++);
	                        } else {
	                            sbQuery.append(" OR LOWER(c." + searchBy + ") LIKE :p" + j++);
	                        }
	                        if (i == searchCriteria.size() - 1) {
	                            sbQuery.append(")");
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
                    if (value != null) {
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

    public List<TxGroupDetail> SearchAllPublikasi(String joinCriteria, List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        try {
            StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
            if (StringUtils.isNotBlank(joinCriteria)) {
                sbQuery.append(joinCriteria);
                sbQuery.append(" ");
            }
//            sbQuery.append("WHERE c.downloadFlag = true AND c.createdDate > TO_DATE('2020-05-02', 'YYYY-MM-DD') AND  c.txTmGeneral.mStatus.id NOT LIKE '229' AND ( c.txTmGeneral.filingDate NOT BETWEEN TO_DATE('2010-01-01', 'YYYY-MM-DD') AND TO_DATE('2014-07-03', 'YYYY-MM-DD'))");
            if (searchCriteria != null) {
                for (int i = 0; i < searchCriteria.size(); i++) {
                    String searchBy = searchCriteria.get(i).getKey();
                    Object value = searchCriteria.get(i).getValue();
                    if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                        if (i == 0) {
                            sbQuery.append(" AND ( LOWER(c." + searchBy + ") LIKE :p" + i);
                        } else {
                            sbQuery.append(" OR LOWER(c." + searchBy + ") LIKE :p" + i);
                        }
                        if (i == searchCriteria.size() - 1) {
                            sbQuery.append(")");
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

    public TxRegistration selectOneByRegisNo(String by, Object value) {
		return super.selectRealOne("LEFT JOIN FETCH c.createdBy cb " 
				+ " ", by, value, true);
	}

    public List<TxRegistration> selectRegByRegisNos(String regNos) {
        try {
            Query query = entityManager.createQuery(
                    "SELECT c FROM " + getClassName() + " c "
                            + "WHERE c.no in ("+regNos+")");
            return query.getResultList();
        } catch (NoResultException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }
}
