package com.docotel.ki.repository.custom.transaction;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.transaction.TxRegistration;
import com.docotel.ki.model.transaction.TxRegistrationDetail;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.docotel.ki.pojo.KeyValue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class TxRegistrationDetailCustomRepository extends BaseRepository<TxRegistrationDetail> {
	public List<TxRegistrationDetail> selectAllExpireRegistration(Timestamp current) {			
		try {
			return entityManager.createQuery(
					"SELECT c FROM " + getClassName() + " c " +
					"LEFT JOIN FETCH c.TxRegistration tr " +
					"LEFT JOIN FETCH tr.txTmGeneral tm " + 
					"WHERE c.status=true AND c.endDate < :p1")
					.setParameter("p1", current)
					.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
    }

	public List<TxRegistrationDetail> selectAllSertifikatDigital(String joinCriteria, List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
			if (StringUtils.isNotBlank(joinCriteria)) {
				sbQuery.append(joinCriteria);
				sbQuery.append(" ");
			}
			sbQuery.append("WHERE c.downloadFlag = true");
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

	public long countSertifikatDigital(List<KeyValue> searchCriteria) {
		String joinCriteria = null;
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT COUNT(c) FROM " + getClassName() + " c ");
			if (StringUtils.isNotBlank(joinCriteria)) {
				sbQuery.append(joinCriteria);
				sbQuery.append(" ");
			}
			sbQuery.append("WHERE c.downloadFlag = true");
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
	
	public List<TxRegistrationDetail> selectAllRegistrationDetail(TxRegistration txRegistration) {			
		try {
			return entityManager.createQuery(
					"SELECT c FROM " + getClassName() + " c " + 
					"WHERE c.txRegistration =:p1 "
					+ "ORDER BY c.endDate DESC")
					.setParameter("p1", txRegistration)
					.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
    }
}
