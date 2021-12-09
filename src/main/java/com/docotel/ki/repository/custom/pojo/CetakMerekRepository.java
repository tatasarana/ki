package com.docotel.ki.repository.custom.pojo;


import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.pojo.CetakMerek;

import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public class CetakMerekRepository extends BaseRepository<CetakMerek> {
	    
	 public List<Object[]> selectHeader(String applicationNo) {
			List<Object[]>  daftar = entityManager.createNativeQuery("select txr.application_date AS applicationDate, " + 
					"mfs.file_seq_desc, mft.file_type_desc , mftc.file_type_detail_desc, ttg.filing_date, " + 
					"mbt.brand_type_name, ttb.tm_brand_name,  ttb.tm_brand_description, ttb.tm_brand_color, ttb.tm_brand_translation, ttb.tm_brand_disclaimer, " + 
					"tto.tm_owner_name, tto.tm_owner_type, pmc.country_name nationality, " +
					"tto.tm_owner_address, mct.city_name,  mpr.prov_name , tto.tm_owner_phone, tto.tm_owner_email, " + 
					"tto.tm_owner_zip_code, mc.country_name mCountry, " + 
					"mrep.reprs_name, mrep.reprs_office, mrep.reprs_address, mrep.reprs_phone, mrep.reprs_email, mrep.reprs_no, ttg.application_id "+					 
					"from tx_reception txr " + 
					"left join m_file_seq mfs on txr.file_seq_id=mfs.file_seq_id " + 
					"left join m_file_type mft on txr.file_type_id=mft.file_type_id " + 
					"left join m_file_type_detail mftc on txr.file_type_detail_id=mftc.file_type_detail_id " +
					"left join tx_tm_general ttg on ttg.application_no=txr.application_no " + 
					"left join tx_tm_brand ttb on ttb.application_id=ttg.application_id " + 
					"left join m_brand_type mbt on mbt.brand_type_id=ttb.brand_type_id " + 
					"left join tx_tm_owner tto on tto.application_id=ttg.application_id " + 
					"left join m_country mc on mc.country_id=tto.tm_owner_country " + 
					"left join m_country pmc on pmc.country_id=tto.tm_owner_nationality " + 
					"left join m_city mct on mct.city_id=tto.tm_owner_city " + 
					"left join m_province mpr on mpr.country_id=tto.tm_owner_country and mpr.prov_id=tto.tm_owner_province " + 
					"left join tx_tm_reprs ttr on ttr.application_id=ttg.application_id " + 
					"left join m_reprs mrep on mrep.reprs_id=ttr.reprs_id " + 					
					"WHERE txr.application_no= :papplicationNo").setParameter("papplicationNo", applicationNo).getResultList();
			return daftar;						 
		 }
	
	 public List<Object[]> selectDetail(String applicationId) {
		List<Object[]> resultList = entityManager.createNativeQuery(
			"select '1' flag_stat,  tm_prior_date, mc.country_name, ttp.tm_prior_no,'' no_kelas, '' desc_kelas,false as doc_status, '' doc_desc " + 
			"from tx_tm_prior ttp " + 
			"left join m_country mc on ttp.tm_prior_country=mc.country_id " + 
			"where application_id=:papplicationId " + 
			"union all " + 
			"select '2' flag_stat , null tm_prior_date, '' country_name, '' tm_prior_no,  class_no as no_kelas, class_desc as desc_kelas, false as doc_status, '' doc_desc " + 
			"from tx_tm_class ttc " + 
			"left join m_class mcl on ttc.class_id=mcl.class_id " + 
			"where application_id=:papplicationId " + 
			"union all " + 
			"select '3' flag_stat, null tm_prior_date, '' country_name, '' tm_prior_no, '' no_kelas, '' desc_kelas, tm_doc_status, tm_doc_desc " + 
			"from tx_tm_doc " + 
			"where application_id=:papplicationId")
				.setParameter("papplicationId", applicationId)				
				.getResultList();		
		return resultList;						 
	}
	}
