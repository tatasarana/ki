package com.docotel.ki.repository.master;

import com.docotel.ki.model.master.MDocTypeDetail;
import com.docotel.ki.model.master.MFileType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MDocTypeDetailRepository extends JpaRepository<MDocTypeDetail, String>{

    List<MDocTypeDetail> findMDocTypeDetailByMFileType(MFileType mFileType);
}
