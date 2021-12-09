package com.docotel.ki.service.master;

import com.docotel.ki.model.master.MMenu;
import com.docotel.ki.model.master.MMenuDetail;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.master.MMenuCustomRepository;
import com.docotel.ki.repository.custom.master.MMenuDetailCustomRepository;
import com.docotel.ki.repository.master.MMenuDetailRepository;
import com.docotel.ki.repository.master.MMenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MenuService {
    @Autowired
    MMenuRepository mMenuRepository;
    @Autowired
    MMenuCustomRepository mMenuCustomRepository;
    @Autowired
    MMenuDetailRepository mMenuDetailRepository;
    @Autowired
    MMenuDetailCustomRepository mMenuDetailCustomRepository;

    public MMenu findOne(String s) {
        return mMenuRepository.findOne(s);
    }

    public List<MMenu> findMMenuByStatusFlagTrue() {
        return mMenuRepository.findMMenuByStatusFlagTrue();
    }

    public List<MMenuDetail> findAllMenuDetail() {
    	//return mMenuDetailCustomRepository.selectAll("JOIN FETCH c.mMenu m","", "", true, null, null);
        return mMenuDetailRepository.findAll();
    }

    public GenericSearchWrapper<MMenu> searchGeneral(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        GenericSearchWrapper<MMenu> searchResult = new GenericSearchWrapper<MMenu>();
        searchResult.setCount(mMenuCustomRepository.count(searchCriteria));
        if (searchResult.getCount() > 0) {
            searchResult.setList(mMenuCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
        }
        return searchResult;
    }

    @Transactional
    public void insert(MMenu data) {
        mMenuRepository.save(data);
    }

    @Transactional
    public void saveOrUpdate(MMenu MMenu) {
        mMenuCustomRepository.saveOrUpdate(MMenu);
    }

    @Transactional
    public MMenuDetail findOneMenuDetail(String s) {
        //return mMenuDetailRepository.findOne(s);
    	return mMenuDetailCustomRepository.selectOne("LEFT JOIN FETCH c.mMenu mm JOIN FETCH mm.createdBy cb","id",s,true);
    	
    }

    @Transactional
    public List<String> findAllMenuDetailUrl() {
        return mMenuDetailCustomRepository.findAllMenuDetailUrl();
    }
}
