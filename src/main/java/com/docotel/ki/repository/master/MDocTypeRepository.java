package com.docotel.ki.repository.master;

import com.docotel.ki.model.master.MDocType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MDocTypeRepository extends JpaRepository<MDocType, String> {

    @Query
    List<MDocType> findByStatusFlagTrue();


}
