package com.docotel.ki.repository.custom.master;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.master.MAction;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;

@Repository
public class MActionCustomRepository extends BaseRepository<MAction> {

    public MAction selectStatusName(String aksiname) {
        try {
            return (MAction) entityManager.createQuery(
                    "SELECT c FROM " + getClassName() + " c WHERE c.name = :p1 ")
                    .setParameter("p1", aksiname)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    public MAction selectOne(String by, Object value) {
        return super.selectOne("LEFT JOIN FETCH c.createdBy cb "
                + " ", by, value, true);
    }

}
