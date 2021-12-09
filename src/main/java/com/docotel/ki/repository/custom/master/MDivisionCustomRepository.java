package com.docotel.ki.repository.custom.master;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.master.MDivision;
import org.springframework.stereotype.Repository;

@Repository
public class MDivisionCustomRepository extends BaseRepository<MDivision> {
    public MDivision selectOne(String by, Object value) {
        return super.selectOne("", by, value, true);
    }
}
