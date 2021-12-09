package com.docotel.ki.repository.master;

import com.docotel.ki.model.master.MCountry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MCountryRepository extends JpaRepository<MCountry,String> {

    @Query
    public List<MCountry> findByStatusFlagTrue();
    
    @Query
    public List<MCountry> findByStatusFlagTrueOrderByName();

    @Query
    public List<MCountry> findByMadrid(Boolean isMadrid);
}
