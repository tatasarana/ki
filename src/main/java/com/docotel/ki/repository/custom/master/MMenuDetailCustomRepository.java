package com.docotel.ki.repository.custom.master;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.master.MMenuDetail;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MMenuDetailCustomRepository extends BaseRepository<MMenuDetail> {
	public List<String> findAllMenuDetailUrl() {
		try {
			return entityManager.createQuery("SELECT c.url FROM " + getClassName() + " c").getResultList();
		} catch (Exception e) {
		}
		return null;
	}
}
