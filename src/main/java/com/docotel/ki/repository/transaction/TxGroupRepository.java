package com.docotel.ki.repository.transaction;


import com.docotel.ki.model.transaction.TxGroup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TxGroupRepository extends JpaRepository<TxGroup, String> {

}
