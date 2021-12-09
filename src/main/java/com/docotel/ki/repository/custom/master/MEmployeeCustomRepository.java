package com.docotel.ki.repository.custom.master;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.master.MEmployee;
import com.docotel.ki.pojo.KeyValue;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;

@Repository
public class MEmployeeCustomRepository extends BaseRepository<MEmployee> {
	
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
	
	public List<MEmployee> selectAllEmployeeNotSubstantif(String section) {
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
	
	public MEmployee selectOneByUserId(String userId) {
		try {
			
			List<MEmployee> employees = entityManager.createQuery(
					//"SELECT new MEmployee(c.id, c.nik, c.telp, c.employeeName, c.email, c.mSection, c.mRoleSubstantifDetail, c.statusFlag, c.sign) FROM " + getClassName() + " c WHERE c.userId.id = :p0")
					"SELECT c FROM MEmployee c WHERE c.userId.id= :p0")
					.setParameter("p0", userId)
					.getResultList();
			
			MEmployee employee = null;			
			
			if(employees.size() > 0) {
				employee = employees.get(0);
			}
			
			return employee;
			/*return (MEmployee) entityManager.createQuery(
					"SELECT new MEmployee(c.id, c.nik, c.telp, c.employeeName, c.email, c.mSection, c.mRoleSubstantifDetail, c.statusFlag, c.sign) FROM " + getClassName() + " c WHERE c.userId.id = :p0")
					//"SELECT c.id, c.nik, c.telp, c.employeeName, c.email, c.mSection, c.mRoleSubstantifDetail, c.statusFlag, c.sign FROM MEmployee c WHERE c.userId.id= :p0")
					.setParameter("p0", userId)
					.getResultList().get(0);*/
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
}
