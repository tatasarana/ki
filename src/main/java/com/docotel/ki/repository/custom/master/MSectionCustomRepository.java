package com.docotel.ki.repository.custom.master;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.master.MSection;
import com.docotel.ki.pojo.KeyValue;

import org.springframework.stereotype.Repository;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

@Repository
public class MSectionCustomRepository extends BaseRepository<MSection> {
    public MSection selectOne(String by, Object value) {
        return super.selectOne("LEFT JOIN FETCH c.mDepartment p "
        					 + "LEFT JOIN FETCH p.mDivision md "
        					 + "LEFT JOIN FETCH c.createdBy cb", by, value, true); 
    }

    public List<MSection> selectAllByDepartment(String by, Object value, Integer offset, Integer limit) {
        return super.selectAll("LEFT JOIN FETCH c.mDepartment p", by, value, true, offset, limit);
    }
    
    @Override    
    public List<MSection> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		return selectAll("LEFT JOIN FETCH c.mDepartment p JOIN FETCH c.createdBy cb", searchCriteria, orderBy, orderType, offset, limit);
	}
    
    public long countBagian(List<KeyValue> searchCriteria) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT COUNT(c) FROM " + getClassName() + " c ");
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (searchBy.equalsIgnoreCase("desc")) {
							sbQuery.append(" AND (LOWER(c." + searchBy + ") LIKE :p" + i + " OR LOWER(c." + searchBy + "En) LIKE :p" + i + ")");
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
			return (Long) query.getSingleResult();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return 0L;
	}
    
    public List<MSection> selectAllBagian(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c JOIN FETCH c.createdBy cb ");
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (searchBy.equalsIgnoreCase("desc")) {
							sbQuery.append(" AND (LOWER(c." + searchBy + ") LIKE :p" + i + " OR LOWER(c." + searchBy + "En) LIKE :p" + i + ")");
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
