package com.docotel.ki.repository.custom.pojo;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.pojo.DataBRM;

import org.springframework.stereotype.Repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class DataBRMRepository extends BaseRepository<DataBRM> {
	
	@PersistenceContext
	protected EntityManager entityManager;
	
	 public List<Object[]> selectObject(String journalNo) {
			List<Object[]> isi = entityManager.createNativeQuery("select d.application_no AS txReception, " +
					    "d.filing_date AS fillDate,  mc.class_no AS txTmClass, e.tm_brand_name AS txTmBrand " + 
					    ", txpr.tm_prior_no as prior_ "+
					    ",(CASE WHEN towd.tm_owner_detail_name is null then tow.tm_owner_name else (tow.tm_owner_name|| ', ' || towd.tm_owner_detail_name) end ) AS ownerName, tow.tm_owner_address ownerAddress" +
					    ",trep.reprs_name as reprsName, trep.reprs_address as reprsAddress " +
					    ",mb.brand_type_name ,e.tm_brand_translation brandTranslation,e.tm_brand_color brandColor,e.tm_brand_description brandDescripion " +				    
					    ",mcc.class_desc as sortUraian, " +// CONCAT(e.tm_brand_id,'-',e.tm_brand_label) brand_logo "+
					    "e.tm_brand_id,e.tm_brand_label " +					    
					    ",txpr.tm_prior_date, mco.country_code,mcc.class_desc_en, to_char(e.created_date, 'YYYY/MM/dd') "+
						", mcc.class_detail_id as sortID, city.city_name, prov.prov_name, tow.tm_owner_zip_code "+
					    "from tx_pubs_journal a " + 
						"left join tx_group b on a.group_id=b.group_id " + 
						"left join tx_group_detail c on b.group_id=c.group_id " + 
						"left join tx_tm_general d on c.application_id=d.application_id " + 
						"left join tx_tm_owner tow on tow.application_id=d.application_id "+
						"left join tx_tm_owner_detail towd on tow.tm_owner_id=towd.tm_owner_id "+
						"left join tx_tm_reprs trep on trep.application_id=d.application_id " +
						"left join tx_tm_prior txpr on txpr.application_id= d.application_id "+
						"left join m_country mco on mco.country_id=txpr.tm_prior_country "+
						"left join tx_tm_brand e on e.application_id=d.application_id " + 
						"left join m_brand_type mb on e.brand_type_id=mb.brand_type_id " +
						"left join tx_tm_class f on f.application_id=d.application_id " +
						"left join m_class mc on f.class_id=mc.class_id " + 
						"left join m_class_detail mcc on f.class_detail_id=mcc.class_detail_id "+
						"left join m_city city on city.city_id = tow.tm_owner_city " +
						"left join m_province prov on prov.prov_id  = tow.tm_owner_province "+
						"WHERE  a.journal_no= :pjournal_no "+ //d.application_no='D002018000006' and
						"ORDER BY fillDate, txReception, sortUraian, sortID ASC ").setParameter("pjournal_no", journalNo).getResultList();
				 			
				return isi;						 
		 }
	
	 public List<DataBRM> selectAll(String journalNo) {
		List<DataBRM> isi = entityManager.createNativeQuery("select d.application_no AS txReception, " +
				    "d.filing_date AS txTmGeneral,  mc.class_no AS txTmClass, e.tm_brand_name AS txTmBrand " + 
				    ", txpr.tm_prior_no as prior "+
				    ",tow.tm_owner_name ownerName, tow.tm_owner_address ownerAddress" + 
				    ",mrep.reprs_name as reprsName, mrep.reprs_address as reprsAddress " + 
				    ",mb.brand_type_name ,e.tm_brand_translation brandTranslation,e.tm_brand_color brandColor,e.tm_brand_description brandDescripion " +				    
					"from tx_pubs_journal a " + 
					"left join tx_group b on a.group_id=b.group_id " + 
					"left join tx_group_detail c on b.group_id=c.group_id " + 
					"left join tx_tm_general d on c.application_id=d.application_id " + 
					"left join tx_tm_owner tow on tow.application_id=d.application_id "+
					"left join tx_tm_reprs trep on trep.application_id=d.application_id " +
					"left join m_reprs mrep on mrep.reprs_id=trep.reprs_id "+
					"left join tx_tm_prior txpr on txpr.application_id= d.application_id "+
					"left join tx_tm_brand e on e.application_id=d.application_id " + 
					"left join m_brand_type mb on e.brand_type_id=mb.brand_type_id " +
					"left join tx_tm_class f on f.application_id=d.application_id " + 
					"left join m_class mc on f.class_id=mc.class_id " + 
					"left join tx_reception g on g.application_no=d.application_no " +
					"WHERE a.journal_no= :pjournal_no").setParameter("pjournal_no", journalNo).getResultList();
			 			
			return isi;						 
	 }
	
    
}
