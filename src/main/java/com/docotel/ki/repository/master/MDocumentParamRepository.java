package com.docotel.ki.repository.master;

import com.docotel.ki.model.master.MDocumentParam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MDocumentParamRepository extends JpaRepository<MDocumentParam, String> {

    List<MDocumentParam> findMDocumentParamByMDocumentId(String id);
    List<MDocumentParam> findMDocumentParamByName(String name);

    void deleteMDocumentParamByMDocumentId(String id);
}
