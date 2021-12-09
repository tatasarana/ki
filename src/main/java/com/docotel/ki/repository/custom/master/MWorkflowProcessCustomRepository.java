package com.docotel.ki.repository.custom.master;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.master.MWorkflowProcess;
import com.docotel.ki.pojo.KeyValue;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

@Repository
public class MWorkflowProcessCustomRepository extends BaseRepository<MWorkflowProcess> {

    @Override
    public List<MWorkflowProcess> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        return super.selectAll("LEFT JOIN FETCH c.createdBy cb ",
                searchCriteria, orderBy, orderType, offset, limit);
    }

    public List<MWorkflowProcess> findMWorkflowProcessesByWorkflowAndStatusFlagOrderByOrders(String wfId, boolean statusFlag, String pId) {
        try {
            StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c " +
                    "JOIN FETCH c.workflow wf " +
                    "JOIN FETCH c.status st ");
            sbQuery.append(" WHERE c.workflow.id = :p1 AND c.statusFlag = :p2 AND c.id != :p3");
            sbQuery.append(" ORDER BY c.orders");
            Query query = entityManager.createQuery(sbQuery.toString());
            query.setParameter("p1", wfId);
            query.setParameter("p2", statusFlag);
            query.setParameter("p3", pId);

            return query.getResultList();
        } catch (NoResultException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

}
