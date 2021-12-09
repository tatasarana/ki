package com.docotel.ki.repository.master;

import com.docotel.ki.model.master.MLookup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MLookupRepository extends JpaRepository<MLookup, String> {

    @Query(value = "SELECT c FROM MLookup c WHERE c.groups = :groups ORDER BY c.id)")
    List<MLookup> findMLookupByGroups(@Param("groups") String groups);

    @Query(value = "SELECT c FROM MLookup c WHERE c.groups = :val ORDER BY TO_NUMBER(c.code)")
    List<MLookup> selectAllByOrderToNumber(@Param("val") String jenisPemohon);

    @Query(value = "SELECT c FROM MLookup c WHERE c.code = :code AND c.groups = :groups")
    MLookup findByCodeGroups(@Param("code") String code, @Param("groups") String groups);
}
