package com.docotel.ki.repository.custom.counter;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.counter.TcRegistrationNo;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.Map;

@Repository
@Transactional
public class TcRegistrationNoCustomRepository extends BaseRepository<TcRegistrationNo> {
	public TcRegistrationNo findOneBy(Map<String, Object> findCriteria, Map<String, String> orderCriteria) {
		try {
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("SELECT x FROM ");
			sbQuery.append(getClassName());
			sbQuery.append(" x WHERE 1 = 1 ");
			if (findCriteria != null) {
				int i = 0;
				for (String key : findCriteria.keySet()) {
					sbQuery.append(" AND x." + key + " = :x" + i);
					i++;
				}
			}
			if (orderCriteria != null && orderCriteria.size() > 0) {
				sbQuery.append(" ORDER BY ");
				for (String key : orderCriteria.keySet()) {
					sbQuery.append("x.");
					sbQuery.append(key);
					sbQuery.append(" ");
					sbQuery.append(orderCriteria.get(key));
					sbQuery.append(", ");
				}
				sbQuery.deleteCharAt(sbQuery.lastIndexOf(","));
			}
			Query query = entityManager.createQuery(sbQuery.toString().trim());
			if (findCriteria != null) {
				int i = 0;
				for (Object value : findCriteria.values()) {
					query.setParameter("x" + i, value);
					i++;
				}
			}
			return (TcRegistrationNo) query.setMaxResults(1).getSingleResult();
		} catch (NoResultException e) {
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
