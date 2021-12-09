package com.docotel.ki.repository.custom.transaction;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.transaction.TxTmDoc;
import com.docotel.ki.pojo.KeyValue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
public class TxTmDocCustomRepository extends BaseRepository<TxTmDoc> {
	@Override
	public List<TxTmDoc> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		return super.selectAll("JOIN FETCH c.createdBy cb RIGHT JOIN FETCH c.doc_type_id di", 
							 searchCriteria, orderBy, orderType, offset, limit);
	}
	
	 public TxTmDoc selectOne(String by, Object value) {
	        return super.selectOne("LEFT JOIN FETCH c.createdBy cb ", by, value, true);
	 }
	 
	 public List<KeyValue> selectAllNewAddDokumen() {
			List<KeyValue> resultList = new ArrayList<>();
			try {
				List<Object[]> objectResultArrList = (List<Object[]>) entityManager.createNativeQuery(" SELECT TX_TM_GENERAL.APPLICATION_NO, M_DOC_TYPE.DOC_TYPE_NAME " +
						" FROM TX_TM_DOC TX_TM_DOC " +
						" JOIN TX_TM_GENERAL TX_TM_GENERAL ON TX_TM_DOC.APPLICATION_ID = TX_TM_GENERAL.APPLICATION_ID " +
						" JOIN M_DOC_TYPE M_DOC_TYPE ON TX_TM_DOC.DOC_TYPE_ID = M_DOC_TYPE.DOC_TYPE_ID " +
						" WHERE TX_TM_DOC.TM_DOC_STATUS = '0' ORDER BY TX_TM_DOC.CREATED_DATE DESC")
						.getResultList();
				for (Object[] objectResultArr : objectResultArrList) {
					KeyValue resultMap = new KeyValue();
					resultMap.setKey((String) objectResultArr[0]);
					resultMap.setValue(objectResultArr[1]);
					resultList.add(resultMap);
				}
			} catch (DataIntegrityViolationException e) {
				logger.error(e.getMessage(), e);
			}
			return resultList;
		}
	 
	 public void deleteTxTmDoc(String txTmDocId) {
			String queryS = "DELETE FROM TX_TM_DOC WHERE TM_DOC_ID = '" + txTmDocId + "'";
			Query query = entityManager.createNativeQuery(queryS);
			query.executeUpdate();
    }
	 
	 
	 // Untuk load dataTable Dokumen
	 public List<TxTmDoc> selectAllDoc(String joinCriteria, String by, Object value, boolean exactMatch, Integer offset, Integer limit) {
			return selectAllTxDoc(joinCriteria, by, value, exactMatch, "id", "ASC", offset, limit);
	 }
	 // Untuk load dataTable Dokumen
	 public List<TxTmDoc> selectAllTxDoc(String joinCriteria, String by, Object value, boolean exactMatch, String orderBy, String orderType, Integer offset, Integer limit) {
			List<KeyValue> searchCriteria = null;
			if (by != null) {
				searchCriteria = new ArrayList<>();
				searchCriteria.add(new KeyValue(by, value, exactMatch));
			}
			return selectAllDoc(joinCriteria, searchCriteria, orderBy, orderType, offset, limit);
	}
	 // Untuk load dataTable Dokumen
	 public List<TxTmDoc> selectAllDoc(String joinCriteria, List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
			try {
				StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
				if (StringUtils.isNotBlank(joinCriteria)) {
					sbQuery.append(joinCriteria);
					sbQuery.append(" ");
				}
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
									sbQuery.append(" AND c." + searchBy + " = :p" + j++);
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
									query.setParameter("p" + j++, ((String) value));
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
	 
	 public long countNewDokumen(List<KeyValue> searchCriteria) {
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
	 
	 public List<TxTmDoc> selectAllNewDoc(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
			try {
				StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
				sbQuery.append("LEFT JOIN FETCH c.txTmGeneral ");
				sbQuery.append("LEFT JOIN FETCH c.mDocType ");
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
