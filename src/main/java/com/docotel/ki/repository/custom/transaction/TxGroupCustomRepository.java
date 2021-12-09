package com.docotel.ki.repository.custom.transaction;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.transaction.TxGroup;
import com.docotel.ki.pojo.KeyValue;
import org.springframework.stereotype.Repository;

@Repository
public class TxGroupCustomRepository extends BaseRepository<TxGroup> {
	@Override
	public List<TxGroup> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		return super.selectAll("LEFT JOIN FETCH c.groupType gt "
				+ "LEFT JOIN FETCH c.createdBy cc "
				+ "LEFT JOIN FETCH cc.mSection ms "
				+ " ", searchCriteria, orderBy, orderType, offset, limit);
	}
	
	public List<TxGroup> selectAllListTxGroup() {
		try {
			return (List<TxGroup>) entityManager.createQuery(
					"SELECT new TxGroup(c.id, c.name) FROM " + getClassName() + " c WHERE c.groupType.name = 'Publikasi'"
							+ "AND c.total > 0 AND c.id NOT IN (SELECT txGroup FROM TxPubsJournal) ORDER BY c.id DESC")
					.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}
	
	public long countGroupPermohonan(List<KeyValue> searchCriteria) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT COUNT(c) FROM " + getClassName() + " c ");
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				StringBuffer txTmGeneral = new StringBuffer();
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (searchBy.equalsIgnoreCase("applicationNo")) {
							txTmGeneral.append(" AND (LOWER(tg." + searchBy + ") = :p" + i + ")");
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
				if(txTmGeneral.length() > 0) {
					sbQuery.append(" AND c.id IN (SELECT tgd.txGroup.id FROM TxGroupDetail tgd LEFT JOIN tgd.txTmGeneral tg WHERE 1 = 1 " + txTmGeneral.toString() + ")");
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
	
	public List<TxGroup> selectAllGroupPermohonan(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c "
			+ " LEFT JOIN FETCH c.groupType gt "
			+ "LEFT JOIN FETCH c.createdBy cc "
			+ "LEFT JOIN FETCH cc.mSection ms ");
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				StringBuffer txTmGeneral = new StringBuffer();
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (searchBy.equalsIgnoreCase("applicationNo")) {
							txTmGeneral.append(" AND (LOWER(tg." + searchBy + ") = :p" + i + ")");
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
				if(txTmGeneral.length() > 0) {
					sbQuery.append(" AND c.id IN (SELECT tgd.txGroup.id FROM TxGroupDetail tgd LEFT JOIN tgd.txTmGeneral tg WHERE 1 = 1 " + txTmGeneral.toString() + ")");
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
	
	public long countDistribusi(List<KeyValue> searchCriteria) {
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
			
			sbQuery.append(" AND c.total > 0");
//			sbQuery.append(" AND status_flag = true");
			sbQuery.append(" AND c.id NOT IN (SELECT txGroup FROM TxServDist)");

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
	
	public List<TxGroup> selectDistribusi(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
			sbQuery.append("LEFT JOIN FETCH c.txGroupDetailList tgd ");
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
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

			sbQuery.append(" AND c.total > 0");
//			sbQuery.append(" AND status_flag = true");
			sbQuery.append(" AND c.id NOT IN (SELECT txGroup FROM TxServDist)");
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
	
	public long countPublikasi(List<KeyValue> searchCriteria) {
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
			
			sbQuery.append(" AND c.total > 0");
//			sbQuery.append(" AND status_flag = true");
			sbQuery.append(" AND c.id NOT IN (SELECT txGroup FROM TxPubsJournal)");

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
	
	public List<TxGroup> selectPublikasi(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
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

			sbQuery.append(" AND c.total > 0");
//			sbQuery.append(" AND status_flag = true");
			sbQuery.append(" AND c.id NOT IN (SELECT txGroup FROM TxPubsJournal)");
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
}
