package com.docotel.ki.repository.custom.master;

import java.util.List;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.master.MWorkflow;
import com.docotel.ki.pojo.KeyValue;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.Query;

@Repository
public class MWorkflowCustomRepository extends BaseRepository<MWorkflow> {
	
	public MWorkflow selectOne(String by, Object value) {
		return super.selectOne("LEFT JOIN FETCH c.createdBy cb "
				+ "LEFT JOIN FETCH c.mFileSequence ms "				
				+ " ", by, value, true);
	}

	@Override
    public List<MWorkflow> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        return super.selectAll("LEFT JOIN FETCH c.createdBy cb ",
        		searchCriteria, orderBy, orderType, offset, limit);
    }

	public List<MWorkflow> selectActivesOrUsed(String workflowId) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c");
			sbQuery.append(" WHERE c.statusFlag = :p0");
			if (workflowId != null) {
				sbQuery.append(" OR c.id = :p1");
			}
			sbQuery.append(" ORDER BY c.code ASC");

			Query query = entityManager.createQuery(sbQuery.toString());
			query.setParameter("p0", true);
			if (workflowId != null) {
				query.setParameter("p1", workflowId);
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

