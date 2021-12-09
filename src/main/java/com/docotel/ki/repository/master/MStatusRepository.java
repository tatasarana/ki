package com.docotel.ki.repository.master;


import com.docotel.ki.model.master.MStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MStatusRepository extends JpaRepository<MStatus, String> {

    MStatus findMStatusByCode(String code);
    MStatus findMStatusByName(String name);

    List<MStatus> findMStatusByStatusFlag(boolean statusFlag);
}
