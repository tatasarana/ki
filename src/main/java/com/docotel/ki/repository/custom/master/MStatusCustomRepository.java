package com.docotel.ki.repository.custom.master;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.master.MStatus;

import javax.persistence.NoResultException;

import org.springframework.stereotype.Repository;

@Repository
public class MStatusCustomRepository extends BaseRepository<MStatus> {

	public MStatus selectStatusName(String statusname) {
		try {
			return (MStatus) entityManager.createQuery(
					"SELECT c FROM " + getClassName() + " c WHERE c.name = :p1 ")
					.setParameter("p1", statusname)
					.setMaxResults(1)
					.getSingleResult();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}
	
	public MStatus selectOne(String by, Object value) {
		return super.selectOne("LEFT JOIN FETCH c.createdBy cb "				
								+ " ", by, value, true);
	}
	
}
