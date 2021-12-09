package com.docotel.ki.repository.custom.master;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.master.MRoleSubstantif;
import com.docotel.ki.pojo.KeyValue;
import org.springframework.stereotype.Repository;

@Repository
public class MRoleSubstantifCustomRepository extends BaseRepository<MRoleSubstantif> {

	public long countRoleSubstantif(List<KeyValue> searchCriteria) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT COUNT(c) FROM " + getClassName() + " c ");
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				StringBuffer mRoleDetails = new StringBuffer();
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (searchBy.equalsIgnoreCase("employeeName")) {
							mRoleDetails.append(" AND LOWER(me.employeeName) LIKE :p" + i);
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
				if(mRoleDetails.length() > 0) {
					sbQuery.append(" AND c.id IN (SELECT mrd.mRoleSubstantif FROM MRoleSubstantifDetail mrd LEFT JOIN mrd.mEmployee me WHERE 1 = 1 " + mRoleDetails.toString() + ")");
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
	
	public List<MRoleSubstantif> selectRoleSubstantif(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
			sbQuery.append("LEFT JOIN FETCH c.mRoleSubstantifDetailList mrd "
					+"LEFT JOIN FETCH mrd.mEmployee me ");
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				StringBuffer mRoleDetails = new StringBuffer();
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (searchBy.equalsIgnoreCase("employeeName")) {
							mRoleDetails.append(" AND LOWER(me.employeeName) LIKE :p" + i);
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
					
					if(mRoleDetails.length() > 0) {
						sbQuery.append(" AND c.id IN (SELECT mrd.mRoleSubstantif FROM MRoleSubstantifDetail mrd LEFT JOIN mrd.mEmployee me WHERE 1 = 1 " + mRoleDetails.toString() + ")");
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
