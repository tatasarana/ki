package com.docotel.ki.repository.master;


import com.docotel.ki.model.master.MWorkflow;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MWorkflowRepository extends JpaRepository<MWorkflow, String> {

}
