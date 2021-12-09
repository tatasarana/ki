package com.docotel.ki.base;

import com.docotel.ki.pojo.KeyValue;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class BaseRepository<T> {
	protected final Logger logger = LogManager.getLogger(this.getClass());

	private String className;

	@PersistenceContext
	protected EntityManager entityManager;

	protected String getClassName() {
		if (className == null) {
			className = ((Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0]).getName();
		}
		return className;
	}

	public long count(String by, Object value, boolean exactMatch) {
		return count(null, by, value, exactMatch);
	}

	public long count(String joinCriteria, String by, Object value, boolean exactMatch) {
		List<KeyValue> searchCriteria = new ArrayList<>();
		searchCriteria.add(new KeyValue(by, value, exactMatch));
		return count(joinCriteria, searchCriteria);
	}

	public Long count(List<KeyValue> searchCriteria) {
		return count(null, searchCriteria);
	}

	public long count(String joinCriteria, List<KeyValue> searchCriteria) {
		try {
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
//			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return 0L;
	}

	public void delete(T t) {
		this.entityManager.remove(t);
	}

	public void delete(String id) {
		delete("id", id);
	}

	public void delete(String by, Object value) {
		entityManager.createQuery("DELETE FROM " + getClassName() + " c WHERE c." + by + " = :p").setParameter("p", value).executeUpdate();
	}

	@Transactional
	public T saveOrUpdate(T t) {
		return this.entityManager.merge(t);
	}

	public List<T> selectAll(String by, Object value, boolean exactMatch, Integer offset, Integer limit) {
		return selectAll(null, by, value, exactMatch, offset, limit);
	}

	public List<T> selectAll(String joinCriteria, String by, Object value, boolean exactMatch, Integer offset, Integer limit) {
		return selectAll(joinCriteria, by, value, exactMatch, "id", "ASC", offset, limit);
	}

	public List<T> selectAll(String by, Object value, boolean exactMatch, String orderBy, String orderType, Integer offset, Integer limit) {
		return selectAll(null, by, value, exactMatch, orderBy, orderType, offset, limit);
	}

	public List<T> selectAll(String joinCriteria, String by, Object value, boolean exactMatch, String orderBy, String orderType, Integer offset, Integer limit) {
		List<KeyValue> searchCriteria = null;
		if (by != null) {
			searchCriteria = new ArrayList<>();
			searchCriteria.add(new KeyValue(by, value, exactMatch));
		}
		return selectAll(joinCriteria, searchCriteria, orderBy, orderType, offset, limit);
	}
//
	public List<T> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		return selectAll(null, searchCriteria, orderBy, orderType, offset, limit);
	}
	
	public List<T> selectAll(String joinCriteria, List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
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
						} else if (searchBy.equalsIgnoreCase("applicationNoGroup")) {
							sbQuery.append(" AND c.txGroup IN (SELECT tgd.txGroup FROM TxGroupDetail tgd LEFT JOIN tgd.txTmGeneral ttg WHERE LOWER(ttg.applicationNo) LIKE :p" + i + ")");
						} else if (value instanceof Date) {
							sbQuery.append(" AND trunc(c." + searchBy + ") = :p" + j++);
						} else if (value instanceof String) {
							if (searchCriteria.get(i).isExactMatch()) {
								sbQuery.append(" AND LOWER(c." + searchBy + ") = :p" + j++);
							} else {
								sbQuery.append(" AND LOWER(c." + searchBy + ") LIKE :p" + j++);
							}
						} else if (value instanceof List) {
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
							sbQuery.append(" AND c." + searchBy + " = :p" + j++);
						}
					}
				}
			}

			if (orderBy != null && orderType != null) {
				sbQuery.append(" ORDER BY c." + orderBy + " " + orderType);
			}

//			System.err.println(sbQuery.toString());

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
						} else if (value instanceof Date) {
							query.setParameter("p" + j++, (Date)value, TemporalType.DATE);
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
//			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	public T selectOne(String by, Object value, boolean exactMatch) {
		return selectOne(null, by, value, exactMatch);
	}

	public T selectOne(String joinCriteria, String by, Object value, boolean exactMatch) {
		return selectOne(joinCriteria, by, value, exactMatch, "id", "ASC");
	}

	public T selectOne(String by, Object value, boolean exactMatch, String orderBy, String orderType) {
		return selectOne(null, by, value, exactMatch, orderBy, orderType);
	}

	public T selectOne(String joinCriteria, String by, Object value, boolean exactMatch, String orderBy, String orderType) {
		List<KeyValue> searchCriteria = new ArrayList<>();
		searchCriteria.add(new KeyValue(by, value, exactMatch));
		return selectOne(joinCriteria, searchCriteria, orderBy, orderType);
	}

	public T selectOne(List<KeyValue> searchCriteria, String orderBy, String orderType) {
		return selectOne(null, searchCriteria, orderBy, orderType);
	}

	public T selectOne(String joinCriteria, List<KeyValue> searchCriteria, String orderBy, String orderType) {
		try {
			return selectAll(joinCriteria, searchCriteria, orderBy, orderType, 0, 1).get(0);
		} catch (NullPointerException | IndexOutOfBoundsException e) {
		}
		return null;
	}

	public T selectRealOne(String joinCriteria, String by, Object value, boolean exactMatch) {
		return selectRealOne(joinCriteria, by, value, exactMatch, "id", "ASC");
	}

	public T selectRealOne(String joinCriteria, String by, Object value, boolean exactMatch, String orderBy, String orderType) {
		List<KeyValue> searchCriteria = new ArrayList<>();
		searchCriteria.add(new KeyValue(by, value, exactMatch));
		return selectRealOne(joinCriteria, searchCriteria, orderBy, orderType);
	}

	public T selectRealOne(String joinCriteria, List<KeyValue> searchCriteria, String orderBy, String orderType) {
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
								sbQuery.append(" AND LOWER(c." + searchBy + ") = :p" + j++);
							} else {
								sbQuery.append(" AND LOWER(c." + searchBy + ") LIKE :p" + j++);
							}
						} else if (value instanceof List) {
							if (((List) value).size() > 0) {
								sbQuery.append(" AND (");

								// ORACLE 1000 limit!
								int oracleLimit = 1000;
								for (int oID = 0; oID <= ((List) value).size() / oracleLimit; oID++) {
									if (oracleLimit * oID == ((List) value).size()) {
										break;
									}
									String newWhereClause = (oID == 0 ? "" : " OR ") + " LOWER(c." + searchBy + ") IN (";
									for (int k = 0; k < (((List) value).size() > (oracleLimit * (oID + 1)) ? oracleLimit : ((List) value).size()); k++) {
										if (k + (oracleLimit * oID) >= ((List) value).size()) {
											break;
										}
										newWhereClause += ((k == 0 ? "" : ",") + ":p" + j++);
									}
									sbQuery.append(newWhereClause + ")");
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

			return (T) query.getSingleResult();
		} catch (NoResultException e) {
//			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;

	}

}
