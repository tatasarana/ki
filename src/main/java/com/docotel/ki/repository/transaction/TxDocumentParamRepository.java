package com.docotel.ki.repository.transaction;

import com.docotel.ki.model.transaction.TxDocumentParams;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TxDocumentParamRepository extends JpaRepository<TxDocumentParams, String> {
    @Query(value="SELECT a FROM TxDocumentParams a where a.applicationNo=:p1 and a.documentId=:p2 ORDER BY a.paramType")
    List<TxDocumentParams> findTxDocumentParamsByApplicationNoAndDocumentId(@Param("p1")String applicationNo,@Param("p2")String documentId);
}
