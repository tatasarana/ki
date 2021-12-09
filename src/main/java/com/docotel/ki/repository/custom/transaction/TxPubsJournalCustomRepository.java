package com.docotel.ki.repository.custom.transaction;


import java.util.List;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.master.MWorkflow;
import com.docotel.ki.model.transaction.TxPubsJournal;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.swing.plaf.ComponentInputMapUIResource;

@Repository
public class TxPubsJournalCustomRepository extends BaseRepository<TxPubsJournal> {

    public TxPubsJournal selectOne(String by, Object value) {
        return super.selectOne("LEFT JOIN FETCH c.createdBy cb "
                + "LEFT JOIN FETCH m.mStatus ms "
                + "LEFT JOIN FETCH l.mLaw ml "
                + "LEFT JOIN FETCH g.txGroup tg "
                + " ", by, value, true);
    }
    
    public TxPubsJournal selectOneBy(String by, Object value) {
        return super.selectOne("LEFT JOIN FETCH c.createdBy cb ", by, value, true);
    }

    public TxPubsJournal selectOne(List<KeyValue> searchCriteria, String orderBy, String orderType) {
        return super.selectOne(searchCriteria, orderBy, orderType);
    }

	public long countPub(List<KeyValue> searchCriteria) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT COUNT(c) FROM " + getClassName() + " c ");
			sbQuery.append("JOIN c.txGroup tg ");
			/*sbQuery.append("JOIN tg.txGroupDetailList tgd ");
			sbQuery.append("JOIN tgd.txTmGeneral ttg ");*/

			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (value == null) {
							sbQuery.append(" AND c." + searchBy + " IS NULL");
						} else if(searchBy.equalsIgnoreCase("txTmGeneral.applicationNo") ) {
							sbQuery.append(" AND tg.txGroupDetailList.txTmGeneral = :p" + i);
							//sbQuery.append(" AND tg.txGroupDetailList IN (SELECT tg.txGroupDetailList.txTmGeneral FROM TxGroupDetail tgd LEFT JOIN tg.txGroupDetailList.txTmGeneral ttg WHERE LOWER(ttg.applicationNo) LIKE :p" + i + ")");
						}
						else if (value instanceof String) {
							if (searchCriteria.get(i).isExactMatch()) {
								sbQuery.append(" AND LOWER(c." + searchBy + ") = :p" + i);
							} else {
								sbQuery.append(" AND LOWER(c." + searchBy + ") LIKE :p" + i);
							}
						}
						else if (searchBy.equalsIgnoreCase("journalEnd")){
							sbQuery.append(" AND c." + searchBy + " >= :p" + i);
						}
						else {
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

		return 1L;
	}

	public GenericSearchWrapper<TxPubsJournal> selectAllPub(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<TxPubsJournal> searchResult = new GenericSearchWrapper<TxPubsJournal>();
		long Count = countPub(searchCriteria);
		System.out.println(Count);
    	if(Count == 0){
    		searchResult.setCount(0);
    		return searchResult;
		}
		else {
    		searchResult.setCount(Count);
		}
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
			sbQuery.append("JOIN c.txGroup tg ");

			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (value == null) {
							sbQuery.append(" AND c." + searchBy + " IS NULL");
						} else if(searchBy.equalsIgnoreCase("txTmGeneral.applicationNo") ) {
							sbQuery.append(" AND tg.txGroupDetailList.txTmGeneral = :p" + i);
							//sbQuery.append(" AND tg.txGroupDetailList IN (SELECT tg.txGroupDetailList.txTmGeneral FROM TxGroupDetail tgd LEFT JOIN tg.txGroupDetailList.txTmGeneral ttg WHERE LOWER(ttg.applicationNo) LIKE :p" + i + ")");
						}
						else if (value instanceof String) {
							if (searchCriteria.get(i).isExactMatch()) {
								sbQuery.append(" AND LOWER(c." + searchBy + ") = :p" + i);
							} else {
								sbQuery.append(" AND LOWER(c." + searchBy + ") LIKE :p" + i);
							}
						}
						else if (searchBy.equalsIgnoreCase("journalEnd")){
							sbQuery.append(" AND c." + searchBy + " >= :p" + i);
						}
						else {
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
			searchResult.setList(query.getResultList());
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
    		e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
		return searchResult;
	}

    @Override
    public List<TxPubsJournal> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        return super.selectAll( searchCriteria, orderBy, orderType, offset, limit);

    }

	public TxPubsJournal selectOneByApplicationNo(String value) {
		try {
			return (TxPubsJournal) entityManager.createQuery(
					"SELECT c FROM " + getClassName() + " c " +
							"JOIN FETCH c.txGroup g " +
							"JOIN FETCH g.txGroupDetailList tgd " +
							"WHERE tgd.txTmGeneral.applicationNo = :p1")
					.setParameter("p1", value)
					.setMaxResults(1)
					.getSingleResult();
		} catch (NoResultException e) {
			//logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

    public TxPubsJournal selectOneByAppId(String value) {
        try {
            return (TxPubsJournal) entityManager.createQuery(
                    "SELECT c FROM " + getClassName() + " c " +
                            "JOIN FETCH c.txGroup g " +
                            "JOIN FETCH g.txGroupDetailList tgd " +
                            "WHERE tgd.txTmGeneral.id = :p1")
                    .setParameter("p1", value)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException e) {
            //logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }
    
    
    public List<TxPubsJournal> selectAllByFilter(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
//			sbQuery.append("LEFT JOIN FETCH c.mStatus ms ");
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				StringBuffer mCondition = new StringBuffer(); 
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (searchBy.equalsIgnoreCase("journalStart") ) {
							mCondition.append(" AND journalStart=:p" + i);
						} else if (searchBy.equalsIgnoreCase("journalStart") && searchBy.equalsIgnoreCase("journalTo") ) {
							mCondition.append(" AND journalStart between :p and :q " + i);
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
    
    public long countLapBRM(List<KeyValue> searchCriteria) {
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
						} else if (searchBy.equalsIgnoreCase("journalStartYear")) {
							sbQuery.append(" AND c.id IN (SELECT tpj FROM TxPubsJournal tpj WHERE TO_CHAR(tpj.journalStart, 'YYYY') = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("journalStartMonth")) {
							sbQuery.append(" AND c.id IN (SELECT tpj FROM TxPubsJournal tpj WHERE TO_CHAR(tpj.journalStart, 'MM') = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("mLaw.id")) {
							sbQuery.append(" AND (c.mLaw.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("createdBy.id")) {
							sbQuery.append(" AND (c.createdBy.id = :p" + i + ")");
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
								if(String.valueOf(value).equals("SUPER")) {
									query.setParameter("p" + i, ((String) value));
								} else {
									query.setParameter("p" + i, ((String) value).toLowerCase());
								}
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
    
    public List<TxPubsJournal> selectAllLapBRM(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (value == null) {
							sbQuery.append(" AND c." + searchBy + " IS NULL");
						} else if (searchBy.equalsIgnoreCase("journalStartYear")) {
							sbQuery.append(" AND c.id IN (SELECT tpj FROM TxPubsJournal tpj WHERE TO_CHAR(tpj.journalStart, 'YYYY') = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("journalStartMonth")) {
							sbQuery.append(" AND c.id IN (SELECT tpj FROM TxPubsJournal tpj WHERE TO_CHAR(tpj.journalStart, 'MM') = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("mLaw.id")) {
							sbQuery.append(" AND (c.mLaw.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("createdBy.id")) {
							sbQuery.append(" AND (c.createdBy.id = :p" + i + ")");
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
								if (String.valueOf(value).equals("SUPER")) {
									query.setParameter("p" + j++, ((String) value));
								} else {
									query.setParameter("p" + j++, ((String) value).toLowerCase());
								}
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
}
