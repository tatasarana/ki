package com.docotel.ki.repository.custom.master;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.master.MRoleSubstantifDetail;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MRoleSubstantifDetailCustomRepository extends BaseRepository<MRoleSubstantifDetail> {
    public List<MRoleSubstantifDetail> selectAllByRoleSubs(String by, Object value, Integer offset, Integer limit) {
        return super.selectAll("LEFT JOIN FETCH c.mRoleSubstantif p", by, value, true, offset, limit);
    }

}
