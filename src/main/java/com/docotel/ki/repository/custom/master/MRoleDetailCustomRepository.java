package com.docotel.ki.repository.custom.master;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.master.MRoleDetail;
import com.docotel.ki.model.master.MUserRole;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

@Repository
public class MRoleDetailCustomRepository extends BaseRepository<MRoleDetail> {

    public List<MRoleDetail> selectAllByRole(String value) {
		try {
			StringBuffer sbQuery = new StringBuffer(
					"SELECT c" +
							" FROM " + getClassName() + " c" +
                            " LEFT JOIN FETCH c.mRole p" +
                            " LEFT JOIN FETCH c.mMenuDetail md "+
                            " JOIN FETCH md.mMenu mm "+
                            " JOIN FETCH c.createdBy cb "+
                            " WHERE c.mRole.id = :p0 AND mm.statusFlag = true "
			);

			Query query = entityManager.createQuery(sbQuery.toString());
			query.setParameter("p0", value);
			return query.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
    }

	public List<MRoleDetail> selectAllByUser(String mUserId) {
		try {
			StringBuffer sbQuery = new StringBuffer(
					"SELECT c" +
					" FROM " + getClassName() + " c" +
					" LEFT JOIN FETCH c.mMenuDetail mmd " +
					" LEFT JOIN FETCH mmd.mMenu mm " + 
					" WHERE c.mRole.id IN (" +
						" SELECT r.mRole.id FROM " + MUserRole.class.getName() + " r WHERE r.mUser.id = :p0" +
					")"
			);

			Query query = entityManager.createQuery(sbQuery.toString());
			query.setParameter("p0", mUserId);
			return query.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}
}

