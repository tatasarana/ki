package com.docotel.ki.repository.master;

import com.docotel.ki.model.master.MMenu;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MMenuRepository extends JpaRepository<MMenu, String> {
	
	List<MMenu> findMMenuByStatusFlagTrue();
}
