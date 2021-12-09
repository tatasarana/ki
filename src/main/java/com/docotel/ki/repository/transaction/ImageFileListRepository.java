package com.docotel.ki.repository.transaction;

import com.docotel.ki.model.wipo.ImageFileList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.lang.String;




@Repository
public interface ImageFileListRepository extends JpaRepository<ImageFileList, String> {

}
