package com.docotel.ki.repository.custom.master;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.master.MLookup;

import javax.persistence.NoResultException;

import com.docotel.ki.model.transaction.TxTmGeneral;
import org.springframework.stereotype.Repository;

@Repository
public class MLookupCostumRepository extends BaseRepository<MLookup> {
	
	public MLookup selectLookName(String lookId) {
        try {
            return (MLookup) entityManager.createQuery(
                    "SELECT c FROM " + getClassName() + " c WHERE c.id = :p1 ")
                    .setParameter("p1", lookId) 
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    public MLookup selectOne(String by, Object value) {
        return super.selectRealOne(null, by, value, true);
    }
	
}
