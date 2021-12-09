package com.docotel.ki.repository.master;

import com.docotel.ki.model.master.MClassDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MClassDetailRepository extends JpaRepository<MClassDetail, String> {

    @Query
    List<MClassDetail> findByStatusFlagTrue();

    @Query
    MClassDetail findFirstByStatusFlagTrueAndId(String id);
    
    @Query("SELECT mc FROM MClassDetail mc LEFT JOIN FETCH mc.parentClass m WHERE mc.id = :mclassid")
    MClassDetail selectOneMClassDetailById(@Param("mclassid") String mclassid);

    public static String mystring = "peralatan,radiologis,industri" ;

//   "SELECT CLASS_DESC, SCORE(11) from M_CLASS_DETAIL WHERE CONTAINS(CLASS_DESC, 'near((peralatan,radiologis,industri),2)',11) > 0 ORDER BY SCORE(11) ASC \n")
    @Query(value = "SELECT CLASS_DESC, SCORE(11) from M_CLASS_DETAIL WHERE CONTAINS(CLASS_DESC, 'near(("+mystring+"),2)',11) > 0 ORDER BY SCORE(11) ASC \n ",nativeQuery = true)
    List<MClassDetail> rawQuery();

    @Modifying
    @Query(value = "DELETE FROM MClassDetail WHERE CLASS_DETAIL_ID=:mClassDetailID")
    void deleteByMClassDetailID(@Param("mClassDetailID") String mClassDetailID);
    
    @Query(value = "SELECT * FROM M_CLASS_DETAIL "
    		+ "WHERE CLASS_BASE_NO = :appNo "
    		+ "AND CLASS_DETAIL_ID NOT IN "
    		+ "(SELECT CLASS_DETAIL_ID FROM TX_TM_CLASS "
    		+ "WHERE APPLICATION_ID = :appNo)" ,nativeQuery = true)
    List<MClassDetail> selectPenolakan(@Param("appNo") String appNo);
    
    @Query(value = "SELECT count(*) FROM M_CLASS_DETAIL "
    		+ "WHERE CLASS_BASE_NO = :appNo "
    		+ "AND CLASS_DETAIL_ID NOT IN "
    		+ "(SELECT CLASS_DETAIL_ID FROM TX_TM_CLASS "
    		+ "WHERE APPLICATION_ID = :appNo)" ,nativeQuery = true)
    int countPenolakan(@Param("appNo") String appNo);
    
    @Modifying
    @Query(value = "DELETE M_CLASS_DETAIL WHERE CLASS_BASE_NO = :appNo "
    		+ "AND CLASS_DETAIL_ID IN "
    		+ "(SELECT CLASS_DETAIL_ID FROM TX_TM_CLASS "
    		+ "WHERE APPLICATION_ID = :appNo "
    		+ "AND TM_CLASS_STATUS = 'PENDING')" ,nativeQuery = true)
    void deleteByMClassBaseNo(@Param("appNo") String appNo);

}
