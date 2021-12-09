package com.docotel.ki.repository.master;

import com.docotel.ki.model.master.MRepresentative;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MRepresentativeRepository extends JpaRepository<MRepresentative, String> {

    @Query
    List<MRepresentative> findByStatusFlagTrue();
    
    // For list konsultan daftar-online
    @Query("SELECT c FROM MRepresentative c WHERE c.statusFlag = true ORDER BY c.name")
    List<MRepresentative> selectAllOrderByName();
}
