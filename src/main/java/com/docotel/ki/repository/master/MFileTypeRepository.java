package com.docotel.ki.repository.master;


import com.docotel.ki.model.master.MFileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MFileTypeRepository extends JpaRepository<MFileType, String> {

    @Query
    List<MFileType> findByStatusFlagTrue();
    
    // Untuk Tipe Permohonan di list permohonan 
    @Query("SELECT c FROM MFileType c WHERE c.menu IN ('DAFTAR','MADRID_OO','MADRID_DCP','MIGRASI') ORDER BY c.menu, c.createdDate")
    List<MFileType> findByFileTypeMenu();
    
    @Query("SELECT c FROM MFileType c WHERE LOWER(c.menu) = LOWER(:menu) AND statusFlag=:statusFlag")
    public List<MFileType> findByMenuAndstatusPaidAndstatusFlag(@Param("menu") String menu, @Param("statusFlag") boolean statusFlag);
    
    @Query("SELECT c FROM MFileType c WHERE LOWER(c.menu) = LOWER(:menu) ORDER BY c.desc")
    public List<MFileType> findByMenu(@Param("menu") String menu);
}
