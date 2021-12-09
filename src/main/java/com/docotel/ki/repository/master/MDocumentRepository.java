package com.docotel.ki.repository.master;

import com.docotel.ki.model.master.MDocument;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MDocumentRepository extends JpaRepository<MDocument, String> {

	@Query
    List<MDocument> findMDocumentByStatusFlag(boolean statusFlag);

    @Query(value="SELECT c FROM MDocument c WHERE c.name=:p1")
    List<MDocument> findByName(@Param("p1") String name);
    
    @Query(value="SELECT c FROM MDocument c "
    		+ "WHERE c.filePath like '%home-image%' or c.filePath like '%home-video%' "
    		+ "order by c.createdDate asc")
    List<MDocument> findByPath();
    
    @Query(value="SELECT c FROM MDocument c "
    		+ "WHERE c.filePath like :path "
    		+ "order by c.createdDate asc")
    List<MDocument> findByPathName(@Param("path") String path);
    
    @Query(value="SELECT c FROM MDocument c "
    		+ "WHERE (c.filePath like '%home-image%' or c.filePath like '%home-video%') and "
    		+ "lower(c.name) like coalesce(:name, c.name) "
    		+ "order by c.createdDate asc")
    List<MDocument> findName(@Param("name") String name);
    
    @Query(value="SELECT c FROM MDocument c "
    		+ "WHERE (c.filePath like '%home-image%' or c.filePath like '%home-video%') and "
    		+ "cast(c.createdDate as date) = :date and lower(c.name) like coalesce(:name, c.name) "
    		+ "order by c.createdDate asc")
    List<MDocument> findByDate(@Param("date") String date, @Param("name") String name);
}
