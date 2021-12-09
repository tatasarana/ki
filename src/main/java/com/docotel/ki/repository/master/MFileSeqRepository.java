package com.docotel.ki.repository.master;


import com.docotel.ki.model.master.MFileSequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MFileSeqRepository extends JpaRepository<MFileSequence, String> {

    @Query("SELECT c FROM MFileSequence c WHERE c.statusFlag = true ORDER BY c.createdDate")
    List<MFileSequence> findByStatusFlagTrue();
    
    @Query("SELECT m FROM MFileSequence m WHERE m.kanwilFlag = true ORDER BY m.desc")
    List<MFileSequence> findByKanwilFlagTrue();
}
