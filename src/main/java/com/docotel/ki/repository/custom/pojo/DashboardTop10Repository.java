package com.docotel.ki.repository.custom.pojo;

import com.docotel.ki.enumeration.StatusEnum;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class DashboardTop10Repository {

	private String generateWhereStatement(Map<String, Object> requestMap) {
		String country = requestMap.get("country").toString();
		String city = requestMap.get("city").toString();
		String end = requestMap.get("end").toString();
		String jenis = requestMap.get("jenis").toString();
		String merek = requestMap.get("merek").toString();
		String kelas = String.join("', '", (ArrayList)requestMap.get("kelas"));
		String province = requestMap.get("province").toString();
		String status = requestMap.get("status").toString();
		String start = requestMap.get("start").toString();
		String tipe = requestMap.get("tipe").toString();
		start = start.replace('/', '-');
		end = end.replace('/', '-');
		String whereStatement = " ";

		if (!start.equals("") && !end.equals("")) {
			whereStatement += "AND TO_CHAR(ttg.FILING_DATE,'YYYY-MM-DD') BETWEEN '" + start + "' AND '" + end + "' ";
		} else if (!start.equals("")) {
			whereStatement += "AND TO_CHAR(ttg.FILING_DATE,'YYYY-MM-DD') >= '" + start + "' ";
		} else if (!end.equals("")) {
			whereStatement += "AND TO_CHAR(ttg.FILING_DATE,'YYYY-MM-DD') <= '" + end + "' ";
		}
		if (!city.equals("")) {
			whereStatement += "AND tto.TM_OWNER_CITY = '" + city + "' ";
		}
		if (!country.equals("")) {
			whereStatement += "AND tto.TM_OWNER_COUNTRY = '" + country + "' ";
		}
		if (!jenis.equals("")) {
			whereStatement += "AND tr.FILE_TYPE_DETAIL_ID = '" + jenis + "' ";
		}
		if (!merek.equals("")) {
			whereStatement += "AND ttb.BRAND_TYPE_ID = '" + merek + "' ";
		}
		if (!province.equals("")) {
			whereStatement += "AND tto.TM_OWNER_PROVINCE = '" + province + "' ";
		}
		if (!kelas.equals("")) {
			whereStatement += "AND ttc.CLASS_ID IN ('" + kelas + "') "; 
		}
		if (!status.equals("")) {
			whereStatement += "AND ttg.STATUS_ID = '" + status + "' ";
		} else {
			whereStatement += "AND ttg.STATUS_ID <> '" + StatusEnum.IPT_DRAFT + "' ";
		}
		if (!tipe.equals("")) {
			whereStatement += "AND tr.FILE_TYPE_ID = '" + tipe + "' ";
		}
		return whereStatement;
	}

	@PersistenceContext
	protected EntityManager entityManager;

	public List<Map<String, Object>> selectTopPemohon(Map<String, Object> requestMap) {
		String whereStatement = this.generateWhereStatement(requestMap);
		List<Object[]> datas = entityManager
				.createNativeQuery("SELECT * FROM (\r\n" + "	SELECT tto.TM_OWNER_NAME, count(DISTINCT tr.RECEPTION_ID) AS total\r\n"
						+ "	FROM MEREK.TX_RECEPTION tr\r\n"
						+ "	JOIN MEREK.TX_TM_GENERAL ttg ON ttg.RECEPTION_ID = tr.RECEPTION_ID \r\n"
						+ " JOIN MEREK.TX_TM_CLASS ttc ON ttc.APPLICATION_ID = ttg.APPLICATION_ID \r\n"
						+ "	JOIN MEREK.TX_TM_OWNER tto ON tto.APPLICATION_ID = ttg.APPLICATION_ID \r\n"
						+ " JOIN MEREK.TX_TM_BRAND ttb ON ttb.APPLICATION_ID = ttg.APPLICATION_ID \r\n"
						+ "	WHERE tto.TM_OWNER_NAME IS NOT null "
						+ whereStatement + "	"
						+ "GROUP BY tto.TM_OWNER_NAME\r\n"
						+ "	ORDER BY total DESC)\r\n" + "WHERE ROWNUM <= 10")
				.getResultList();

		List<Map<String, Object>> result = new ArrayList<>();
		for (Object[] object : datas) {
			Map<String, Object> data = new HashMap<>();
			data.put("nama", object[0]);
			data.put("total", object[1]);
			result.add(data);
		}

		return result;
	}

	public List<Map<String, Object>> selectTopKuasa(Map<String, Object> requestMap) {
		String whereStatement = this.generateWhereStatement(requestMap);
		List<Object[]> datas = entityManager
				.createNativeQuery("SELECT * FROM (\r\n" + "	SELECT mr.REPRS_NAME, count(*) AS total\r\n"
						+ "	FROM MEREK.TX_TM_REPRS a\r\n"
						+ "	JOIN MEREK.M_REPRS mr ON mr.REPRS_ID = a.REPRS_ID	\r\n"
						+ "	JOIN MEREK.TX_TM_GENERAL ttg ON ttg.APPLICATION_ID = a.APPLICATION_ID\r\n"
						+ " JOIN MEREK.TX_TM_CLASS ttc ON ttc.APPLICATION_ID = ttg.APPLICATION_ID \r\n"
						+ "	JOIN MEREK.TX_RECEPTION tr ON tr.RECEPTION_ID = ttg.RECEPTION_ID\r\n"
						+ "	JOIN MEREK.TX_TM_BRAND ttb ON ttb.APPLICATION_ID = ttg.APPLICATION_ID\r\n"
						+ "	WHERE a.REPRS_NAME IS NOT NULL "
						+ whereStatement
						+ "	GROUP BY mr.REPRS_NAME\r\n"
						+ "	ORDER BY total DESC)\r\n" + "WHERE ROWNUM <= 10")
				.getResultList();

		List<Map<String, Object>> result = new ArrayList<>();
		for (Object[] object : datas) {
			Map<String, Object> data = new HashMap<>();
			data.put("nama", object[0]);
			data.put("total", object[1]);
			result.add(data);
		}

		return result;
	}
}
