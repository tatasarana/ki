package com.docotel.ki.repository.custom.pojo;


import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.pojo.MigrasiClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.docotel.ki.repository.custom.transaction.TxTmClassCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmGeneralCustomRepository;

@Repository
public class MigrasiClassRepository extends BaseRepository<MigrasiClass> {
	@PersistenceContext protected EntityManager entityManager;	

	@Autowired TxTmGeneralCustomRepository txTmGeneralCustomRepository;	
	@Autowired TxTmClassCustomRepository txTmClassCustomRepository;
	
	@Value("${table.migrasi.class.name:}")
    private String tableMigrasiName;
	
	 public List<Object[]> selectObject() {
		/*List<Object[]> resultList = entityManager.createNativeQuery(
			"select class_, uraian, application_no " + 
			"from migrasi_class "  +
			"where is_processed=0 OR IS_PROCESSED IS NULL ")
			.getResultList();	*/
		 
		 List<Object[]> resultList = entityManager.createNativeQuery(
					"  select * from " + 
					"				     (select class_,uraian, " + 
					"				             a.application_no, " + 
					"				             row_number() over(order by a.application_no desc) rnm " + 
					"				    from " + tableMigrasiName + " a INNER JOIN tx_tm_general b ON a.application_no=b.application_no  " + 
					"					WHERE IS_PROCESSED=0 OR IS_PROCESSED IS NULL AND a.APPLICATION_NO IS NOT NULL ) "+		
				 	"				where rnm<=1000 ")
					.getResultList();
		return resultList;				 			
	 }
	 
	
	 @Transactional(readOnly=true)
	 public HashMap<String, TxTmGeneral> selectGeneralByApplicationNo() {
		 HashMap<String,TxTmGeneral> listGeneral = new HashMap<String,TxTmGeneral>();
		
		List<String> ListApplicationNo = entityManager.createNativeQuery(
				"  select application_no from " + 
				"				     (select class_,uraian, " + 
				"				             a.application_no, " + 
				"				             row_number() over(order by a.application_no desc) rnm " + 
				"				    from " + tableMigrasiName + " a INNER JOIN tx_tm_general b ON a.application_no=b.application_no " + 
				"				where IS_PROCESSED=0 OR IS_PROCESSED IS NULL AND a.APPLICATION_NO IS NOT NULL ) " +				
				"where rnm<=1000 ")
				.getResultList();		
		
		int count = 0;
		for(String applicationNo: ListApplicationNo) {
			//System.out.println("from table : " + tableMigrasiName + " applicationNo : " + applicationNo + "; total permohonan : " +count + "; ");
			TxTmGeneral txTmGeneral = txTmGeneralCustomRepository.select("applicationNo", applicationNo) ;
			listGeneral.put(applicationNo, txTmGeneral);
			count++;
		}
		
		return listGeneral;
	}
}
