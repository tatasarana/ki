package com.docotel.ki.repository.master;


import com.docotel.ki.model.master.MFileTypeDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MFileTypeDetailRepository extends JpaRepository<MFileTypeDetail, String> {

    @Query
    List<MFileTypeDetail> findByStatusFlagTrue();

    @Query
    MFileTypeDetail getFirstByCode(String code);
    
    @Query("SELECT c FROM MFileTypeDetail c WHERE c.id IN ('UMKM','NUMKM') ORDER BY c.id")
    List<MFileTypeDetail> findMFileTypeDetailById();
    
    @Query("SELECT c FROM MFileTypeDetail c WHERE LOWER(c.code) = LOWER(:code)")
    List<MFileTypeDetail> findMFileTypeDetailByCode(@Param("code") String code);

}
