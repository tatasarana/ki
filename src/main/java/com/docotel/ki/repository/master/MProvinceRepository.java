package com.docotel.ki.repository.master;

import com.docotel.ki.model.master.MProvince;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MProvinceRepository extends JpaRepository<MProvince, String> {

    @Query
    List<MProvince> findByStatusFlagTrue();
    
    @Query
    List<MProvince> findByStatusFlagTrueOrderByName();
}
