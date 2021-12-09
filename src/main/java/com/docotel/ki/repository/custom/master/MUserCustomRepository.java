package com.docotel.ki.repository.custom.master;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.enumeration.OperationEnum;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

@Repository
public class MUserCustomRepository extends BaseRepository<MUser> {
	public MUser selectOne(String by, Object value) {
		return super.selectOne(null, by, value, true);
	}

	public UserDetails selectOneUser(String username) {
		try {
			return (UserDetails) entityManager.createQuery(
					"SELECT new MUser(c.id, c.username, c.password, c.email, c.enabled, c.accountNonExpired, c.accountNonLocked, c.credentialsNonExpired, c.mFileSequence, c.employee, c.reprs, c.userType) FROM " + getClassName() + " c WHERE LOWER(c.username) = :p1 AND c.operation = :p2")
					.setParameter("p1", username.toLowerCase())
					.setParameter("p2", OperationEnum.USER.value())
					.setMaxResults(1)
					.getSingleResult();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
//		}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	public MUser selectOneUserByUsername(String username) {
		try {
			return (MUser) entityManager.createQuery(
					"SELECT new MUser(c.id, c.username, c.password, c.email, c.enabled, c.accountNonExpired, c.accountNonLocked, c.credentialsNonExpired, c.mFileSequence, c.employee, c.reprs, c.userType) FROM " + getClassName() + " c WHERE LOWER(c.username) = :p1")
					.setParameter("p1", username.toLowerCase())
					.setMaxResults(1)
					.getSingleResult();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	public MUser selectOneUserByUserId(String userId) {
		try {
			return (MUser) entityManager.createQuery(
					"SELECT new MUser(c.id, c.username, c.password, c.email, c.enabled, c.accountNonExpired, c.accountNonLocked, c.credentialsNonExpired, c.mFileSequence, c.employee, c.reprs, c.userType) FROM " + getClassName() + " c WHERE c.id = :p1")
					.setParameter("p1", userId)
					.setMaxResults(1)
					.getSingleResult();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}
	
	public List<UserDetails> selectListUserByUserType(String userType) {
		try {
			return (List<UserDetails>) entityManager.createQuery(
					"SELECT new MUser(c.id, c.username, c.password, c.email, c.enabled, c.accountNonExpired, c.accountNonLocked, c.credentialsNonExpired, c.mFileSequence, c.employee, c.reprs, c.userType) FROM " + getClassName() + " c WHERE LOWER(c.userType) = :p1")
					.setParameter("p1", userType.toLowerCase())
					.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}
	
	public List<MUser> selectAllUserNotSubstantif(String section) {
		try {
			return entityManager.createQuery(
					"SELECT c FROM " + getClassName() + " c LEFT JOIN FETCH c.mRoleSubstantifDetail mrsd WHERE mrsd.id IS NULL AND c.mSection.id = :p1")
					.setParameter("p1", section)
					.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}	
	
	//public long countUser(List<KeyValue> searchCriteria, String[] exclude, String[] include, String orderBy, String orderType, Integer offset, Integer limit) {
	public long countUser(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT count(c) FROM " + getClassName() + " c ");						
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
			return (Long) query.setMaxResults(1).getSingleResult();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return 0L;
	}
	
	public List<MUser> selectAllUser(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        try {
            StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
            sbQuery.append("JOIN FETCH c.operation o ");
            sbQuery.append("JOIN FETCH c.mSection ms ");
            sbQuery.append("JOIN FETCH ms.mDepartment mdep ");
            sbQuery.append("JOIN FETCH mdep.mDivision mdiv ");            
            sbQuery.append("LEFT JOIN FETCH c.mRoleSubstantifDetail mrsd ");
            sbQuery.append("JOIN FETCH c.updatedBy ub ");
            sbQuery.append("JOIN FETCH c.mFileSequence ms ");
//            sbQuery.append("LEFT JOIN FETCH c.mEmployee me ");
//            sbQuery.append("LEFT JOIN FETCH c.mRepresentative mr ");
            
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
	
	
	public long countAllUserEmployeeReprs(List<KeyValue> searchCriteria) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT count(c) FROM " + getClassName() + " c ");				
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {				
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {	
						if (searchBy.equalsIgnoreCase("employeeName")) { 
                            sbQuery.append(" AND ( c.id IN (SELECT mr.userId FROM MRepresentative mr WHERE LOWER(mr.name) LIKE :p" + i + ")" );
                            sbQuery.append(" OR c.id IN (SELECT me.userId FROM MEmployee me WHERE LOWER(me.employeeName) LIKE :p" + i + ") ) " );
						} else if (searchBy.equalsIgnoreCase("empReprsStatus")) {							
                            boolean bvalue = (Boolean) value;
							if(bvalue==true) { 
								sbQuery.append(" AND ( c.id IN (SELECT mr.userId FROM MRepresentative mr WHERE mr.statusFlag = :p" + i + ")" );
	                            sbQuery.append(" OR c.id IN (SELECT me.userId FROM MEmployee me WHERE me.statusFlag = :p" + i + ")  " );
	                            sbQuery.append(" OR c.email IN (SELECT tor.email FROM TxOnlineReg tor WHERE tor.email=c.email AND tor.approvalStatus='Aktif') ) " );	                            
							} else {												
								sbQuery.append(" AND (NOT EXISTS (SELECT mr.userId FROM MRepresentative mr WHERE mr.userId=c.id OR c.id IN (SELECT mr.userId FROM MRepresentative mr WHERE mr.statusFlag= :p" + i + ") ) " );
	                            sbQuery.append(" AND NOT EXISTS (SELECT me.userId FROM MEmployee me WHERE me.userId=c.id OR me.userId IN (SELECT me.userId FROM MEmployee me WHERE me.statusFlag = :p" + i + " ) ) ) " );	                            
	                            sbQuery.append(" AND c.email NOT IN (SELECT tor.email FROM TxOnlineReg tor WHERE tor.email=c.email ) " );	                            
							}
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
	
	public List<MUser> selectAllUserEmployeeReprs(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        try { 
            StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
            sbQuery.append("LEFT JOIN FETCH c.operation o ");
            sbQuery.append("LEFT JOIN FETCH c.mSection ms ");
            sbQuery.append("LEFT JOIN FETCH ms.mDepartment mdep ");
            sbQuery.append("LEFT JOIN FETCH mdep.mDivision mdiv ");            
            sbQuery.append("LEFT JOIN FETCH c.updatedBy ub ");
            sbQuery.append("LEFT JOIN FETCH c.mFileSequence ms ");
           /* sbQuery.append("LEFT JOIN FETCH c.mRepresentative mr ");
            sbQuery.append("LEFT JOIN FETCH c.mEmployee me ");*/ 
            sbQuery.append("WHERE 1 = 1");
            if (searchCriteria != null) {            
                for (int i = 0; i < searchCriteria.size(); i++) {
                    String searchBy = searchCriteria.get(i).getKey();
                    Object value = searchCriteria.get(i).getValue();

                    if (searchBy != null && !searchBy.equalsIgnoreCase("")) { 
                    	if (searchBy.equalsIgnoreCase("employeeName")) {
                    		 sbQuery.append(" AND ( c.id IN (SELECT mr.userId FROM MRepresentative mr WHERE LOWER(mr.name) LIKE :p" + i + ")" );
                             sbQuery.append(" OR c.id IN (SELECT me.userId FROM MEmployee me WHERE LOWER(me.employeeName) LIKE :p" + i + ") ) " );
                    	} else if (searchBy.equalsIgnoreCase("empReprsStatus")) {							
                    		boolean bvalue = (Boolean) value;
                    		if(bvalue==true) {
								sbQuery.append(" AND ( c.id IN (SELECT mr.userId FROM MRepresentative mr WHERE mr.statusFlag = :p" + i + ")" );
	                            sbQuery.append(" OR c.id IN (SELECT me.userId FROM MEmployee me WHERE me.statusFlag = :p" + i + ")  " );
	                            sbQuery.append(" OR c.email IN (SELECT tor.email FROM TxOnlineReg tor WHERE tor.email=c.email AND tor.approvalStatus='Aktif') ) " );	                            
							} else {												
								sbQuery.append(" AND (NOT EXISTS (SELECT mr.userId FROM MRepresentative mr WHERE mr.userId=c.id OR c.id IN (SELECT mr.userId FROM MRepresentative mr WHERE mr.statusFlag= :p" + i + ") ) " );
	                            sbQuery.append(" AND NOT EXISTS (SELECT me.userId FROM MEmployee me WHERE me.userId=c.id OR me.userId IN (SELECT me.userId FROM MEmployee me WHERE me.statusFlag = :p" + i + " ) ) ) " );	                            
	                            sbQuery.append(" AND c.email NOT IN (SELECT tor.email FROM TxOnlineReg tor WHERE tor.email=c.email ) " );                 
							}
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
	        if (orderBy != null && orderType != null) {	        	
	        	sbQuery.append(" ORDER BY c." + orderBy + " " + orderType);
	        }
	        
            Query query = entityManager.createQuery(sbQuery.toString());
            if (searchCriteria != null) {
                for (int i = 0; i < searchCriteria.size(); i++) {
                	String searchBy = searchCriteria.get(i).getKey();
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
	
	
}
