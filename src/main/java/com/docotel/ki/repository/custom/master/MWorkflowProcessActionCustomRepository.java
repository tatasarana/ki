package com.docotel.ki.repository.custom.master;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.master.MWorkflowProcessActions;
import org.springframework.stereotype.Repository;

@Repository
public class MWorkflowProcessActionCustomRepository extends BaseRepository<MWorkflowProcessActions> {

    public MWorkflowProcessActions selectOne(String by, Object value) {
        return super.selectOne("LEFT JOIN FETCH c.createdBy cb "
                + " ", by, value, true);
    }


}
