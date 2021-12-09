package com.docotel.ki.repository.master;

import com.docotel.ki.model.master.MMenuDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MMenuDetailRepository extends JpaRepository<MMenuDetail, String> {
}
