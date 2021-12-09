package com.docotel.ki.repository.transaction;

import com.docotel.ki.model.wipo.ImageFile;
import com.docotel.ki.model.wipo.ImageFileList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ImageFileRepository extends JpaRepository<ImageFile, String> {

}
