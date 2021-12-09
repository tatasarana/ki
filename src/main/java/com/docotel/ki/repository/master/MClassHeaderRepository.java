package com.docotel.ki.repository.master;

import com.docotel.ki.model.master.MClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MClassHeaderRepository extends JpaRepository<MClass,String> {

    @Query
    List<MClass> findByStatusFlagTrue();

    @Query
    MClass findFirstByNo(String no);
    
    @Query
    MClass findFirstByNo(int no);

    MClass findByNo(int no);
}
