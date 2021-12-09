package com.docotel.ki.repository.master;

import com.docotel.ki.model.master.MLaw;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MLawRepository extends JpaRepository<MLaw, String> {

    @Query
    public List<MLaw> findByStatusFlagTrue();
    //reference : https://docs.spring.io/spring-data/jpa/docs/1.5.1.RELEASE/reference/html/jpa.repositories.html#jpa.sample-app.finders.strategies
}
