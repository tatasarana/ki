package com.docotel.ki.repository.custom.pojo;

import com.docotel.ki.enumeration.StatusEnum;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class DashboardPermohonanRepository {

	private String generateWhereStatement(Map<String, Object> requestMap, String statisticsType) {
		String country = requestMap.get("country").toString();
		String city = requestMap.get("city").toString();
		String jenis = requestMap.get("jenis").toString();
		String merek = requestMap.get("merek").toString();
		String kelas = String.join("', '", (ArrayList)requestMap.get("kelas"));
		String province = requestMap.get("province").toString();
		String status = requestMap.get("status").toString();
		String tipe = requestMap.get("tipe").toString();
		String start = requestMap.get("start").toString();
		String end = requestMap.get("end").toString();
		String tglProsesStartDate = requestMap.get("tglProsesStartDate").toString();
		String tglProsesEndDate = requestMap.get("tglProsesEndDate").toString();
		start = start.replace('/', '-');
		end = end.replace('/', '-');
		tglProsesStartDate = tglProsesStartDate.replace('/', '-');
		tglProsesEndDate = tglProsesEndDate.replace('/', '-');
		String whereStatement = " ";

		if (!start.equals("") && !end.equals("")) {
			whereStatement += "AND TO_CHAR(ttg.FILING_DATE,'YYYY-MM-DD') BETWEEN '"+start+"' AND '"+end+"' ";
		} else if (!start.equals("")) {
			whereStatement += "AND TO_CHAR(ttg.FILING_DATE,'YYYY-MM-DD') >= '" + start + "' ";
		} else if (!end.equals("")) {
			whereStatement += "AND TO_CHAR(ttg.FILING_DATE,'YYYY-MM-DD') <= '" + end + "' ";
		}

		if (!tglProsesStartDate.equals("") && !tglProsesEndDate.equals("")) {
			whereStatement += "AND TO_CHAR(tm.CREATED_DATE,'YYYY-MM-DD') BETWEEN '"+tglProsesStartDate+"' AND '"+tglProsesEndDate+"' ";
		} else if (!tglProsesStartDate.equals("")) {
			whereStatement += "AND TO_CHAR(tm.CREATED_DATE,'YYYY-MM-DD') >= '" + tglProsesStartDate + "' ";
		} else if (!tglProsesEndDate.equals("")) {
			whereStatement += "AND TO_CHAR(tm.CREATED_DATE,'YYYY-MM-DD') <= '" + tglProsesEndDate + "' ";
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
		} else if (!statisticsType.equals("status")) {
			whereStatement += "AND ttg.STATUS_ID <> '" + StatusEnum.IPT_DRAFT + "' ";
		}
		if (!tipe.equals("")) {
			whereStatement += "AND tr.FILE_TYPE_ID = '" + tipe + "' ";
		}
		return whereStatement;
	}

	// Ada 2 fungsi generateWhereStatement untuk mensimulasikan default value dari parameter ke-2 fungsi ini
	private String generateWhereStatement(Map<String, Object> requestMap) {
		return this.generateWhereStatement(requestMap, "");
	}

	@PersistenceContext
	protected EntityManager entityManager;

	public List<Map<String, Object>> selectPemohon(boolean isMadrid, boolean isAsing, Map<String, Object> requestMap) {
		String whereStatement = this.generateWhereStatement(requestMap);
		String where = "";
		if (isMadrid) {
			where = "mc.IS_MADRID = '1'";
		} else if (isAsing) {
			where = "mc.COUNTRY_ID != 'ID'";
		}

		List<Object[]> datas = entityManager
				.createNativeQuery(
					"SELECT mc.COUNTRY_ID AS id, mc.COUNTRY_NAME AS name, mc.IS_MADRID AS madrid, \r\n"
						+ "count(DISTINCT tr.APPLICATION_NO) AS total FROM M_COUNTRY mc \r\n"
						+ "JOIN TX_TM_OWNER tto ON tto.TM_OWNER_COUNTRY = mc.COUNTRY_ID\r\n"
						+ "JOIN TX_TM_GENERAL ttg ON ttg.APPLICATION_ID = tto.APPLICATION_ID\r\n"
						+ "JOIN TX_TM_CLASS ttc ON ttc.APPLICATION_ID = ttg.APPLICATION_ID \r\n"
						+ "JOIN TX_TM_BRAND ttb ON ttb.APPLICATION_ID = ttg.APPLICATION_ID\r\n"
						+ "JOIN TX_RECEPTION tr ON tr.APPLICATION_NO = ttg.APPLICATION_NO\r\n"
						+ "LEFT JOIN TX_MONITOR tm ON tm.APPLICATION_ID = ttg.APPLICATION_ID\r\n"
						+ "LEFT JOIN M_WORKFLOW_PROCESS mwp ON mwp.WORKFLOW_PROCESS_ID = tm.WORKFLOW_PROCESS_ID AND mwp.STATUS_ID = ttg.STATUS_ID\r\n"
						+ "WHERE tto.TM_OWNER_STATUS = 1 AND "
						+ where
						+ whereStatement
						+ " GROUP BY mc.COUNTRY_ID, mc.COUNTRY_NAME, mc.IS_MADRID\r\n"
						+ "ORDER BY mc.COUNTRY_ID"
				).getResultList();

		List<Map<String, Object>> result = new ArrayList<>();
		for (Object[] object : datas) {
			Map<String, Object> data = new HashMap<>();
			data.put("nama", object[1]);
			data.put("total", object[3]);
			result.add(data);
		}

		return result;
	}

	public List<Map<String, Object>> selectPemohonIndonesia(Map<String, Object> requestMap) {
		String whereStatement = this.generateWhereStatement(requestMap);

		List<Object[]> datas = entityManager
				.createNativeQuery(
					"SELECT mc.COUNTRY_ID AS id, mc.COUNTRY_NAME AS name, mp.PROV_NAME AS prov, \r\n"
						+ "count(DISTINCT tr.APPLICATION_NO) AS total FROM M_COUNTRY mc \r\n"
						+ "JOIN TX_TM_OWNER tto ON tto.TM_OWNER_COUNTRY = mc.COUNTRY_ID\r\n"
						+ "JOIN TX_TM_GENERAL ttg ON ttg.APPLICATION_ID = tto.APPLICATION_ID\r\n"
						+ "JOIN TX_TM_CLASS ttc ON ttc.APPLICATION_ID = ttg.APPLICATION_ID \r\n"
						+ "JOIN TX_TM_BRAND ttb ON ttb.APPLICATION_ID = ttg.APPLICATION_ID\r\n"
						+ "JOIN TX_RECEPTION tr ON tr.APPLICATION_NO = ttg.APPLICATION_NO\r\n"
						+ "LEFT JOIN TX_MONITOR tm ON tm.APPLICATION_ID = ttg.APPLICATION_ID\r\n"
						+ "LEFT JOIN M_WORKFLOW_PROCESS mwp ON mwp.WORKFLOW_PROCESS_ID = tm.WORKFLOW_PROCESS_ID AND mwp.STATUS_ID = ttg.STATUS_ID\r\n"
						+ "JOIN M_PROVINCE mp ON mp.PROV_ID = tto.TM_OWNER_PROVINCE \r\n"
						+ "WHERE mc.COUNTRY_ID = 'ID' AND mp.PROV_ID is NOT null AND tto.TM_OWNER_STATUS = 1\r\n"
						+ whereStatement
						+ "GROUP BY mc.COUNTRY_ID, mc.COUNTRY_NAME, mp.PROV_NAME\r\n"
						+ "ORDER BY mp.PROV_NAME"
				).getResultList();

		List<Map<String, Object>> result = new ArrayList<>();
		for (Object[] object : datas) {
			Map<String, Object> data = new HashMap<>();
			data.put("nama", object[2]);
			data.put("total", object[3]);
			result.add(data);
		}

		return result;
	}

	public List<Map<String, Object>> selectStatus(Map<String, Object> requestMap) {
		String whereStatement = this.generateWhereStatement(requestMap, "status");

		List<Object[]> datas = entityManager
				.createNativeQuery(
					"SELECT ttg.STATUS_ID AS id, ms.STATUS_NAME AS name, count(DISTINCT ttg.APPLICATION_NO) AS total \r\n"
						+ "FROM TX_TM_GENERAL ttg\r\n"
						+ "JOIN M_STATUS ms ON ms.STATUS_ID = ttg.STATUS_ID\r\n"
						+ "JOIN TX_RECEPTION tr ON tr.APPLICATION_NO = ttg.APPLICATION_NO\r\n"
						+ "JOIN TX_TM_CLASS ttc ON ttc.APPLICATION_ID = ttg.APPLICATION_ID \r\n"
						+ "LEFT JOIN TX_MONITOR tm ON tm.APPLICATION_ID = ttg.APPLICATION_ID\r\n"
						+ "LEFT JOIN M_WORKFLOW_PROCESS mwp ON mwp.WORKFLOW_PROCESS_ID = tm.WORKFLOW_PROCESS_ID AND mwp.STATUS_ID = ttg.STATUS_ID\r\n"
						+ "LEFT JOIN TX_TM_BRAND ttb ON ttb.APPLICATION_ID = ttg.APPLICATION_ID \r\n"
						+ "LEFT JOIN TX_TM_OWNER tto ON tto.APPLICATION_ID = ttg.APPLICATION_ID \r\n"
						+ "WHERE ttg.STATUS_ID IS NOT null AND tto.TM_OWNER_STATUS = 1 "
						+ whereStatement
						+ "GROUP BY ttg.STATUS_ID, ms.STATUS_NAME\r\n"
						+ "ORDER BY ms.STATUS_NAME"
					)
				.getResultList();

		List<Map<String, Object>> result = new ArrayList<>();
		for (Object[] object : datas) {
			Map<String, Object> data = new HashMap<>();
			data.put("nama", object[1]);
			data.put("total", object[2]);
			result.add(data);
		}

		return result;
	}

	public List<Map<String, Object>> selectTipePermohonan(Map<String, Object> requestMap) {
		String whereStatement = this.generateWhereStatement(requestMap);

		List<Object[]> datas = entityManager.createNativeQuery(
			"SELECT mft.FILE_TYPE_DESC, count(DISTINCT tr.APPLICATION_NO) total, mft.FILE_TYPE_MENU FROM TX_RECEPTION tr \r\n"
				+ "LEFT JOIN M_FILE_TYPE mft ON mft.FILE_TYPE_ID = tr.FILE_TYPE_ID \r\n"
				+ "JOIN TX_TM_GENERAL ttg ON ttg.APPLICATION_NO = tr.APPLICATION_NO \r\n"
				+ "JOIN TX_TM_CLASS ttc ON ttc.APPLICATION_ID = ttg.APPLICATION_ID \r\n"
				+ "JOIN TX_TM_BRAND ttb ON ttb.APPLICATION_ID = ttg.APPLICATION_ID \r\n"
				+ "LEFT JOIN TX_MONITOR tm ON tm.APPLICATION_ID = ttg.APPLICATION_ID\r\n"
				+ "LEFT JOIN M_WORKFLOW_PROCESS mwp ON mwp.WORKFLOW_PROCESS_ID = tm.WORKFLOW_PROCESS_ID AND mwp.STATUS_ID = ttg.STATUS_ID\r\n"
				+ "LEFT JOIN TX_TM_OWNER tto ON tto.APPLICATION_ID = ttg.APPLICATION_ID \r\n"
				+ "WHERE tr.APPLICATION_NO IS NOT NULL AND tto.TM_OWNER_STATUS = 1 "
				+ whereStatement
				+ "GROUP BY mft.FILE_TYPE_DESC, mft.FILE_TYPE_MENU\r\n"
				+ "ORDER BY mft.FILE_TYPE_MENU"
		).getResultList();

		List<Map<String, Object>> result = new ArrayList<>();
		for (Object[] object : datas) {
			Map<String, Object> data = new HashMap<>();
			data.put("nama", object[0]);
			data.put("total", object[1]);
			result.add(data);
		}

		return result;
	}

	public Map<String, Object> selectGrafikTipePermohonan(Map<String, Object> requestMap) {
		String whereStatement = this.generateWhereStatement(requestMap);

		List<Object[]> datas = entityManager
				.createNativeQuery(
					"SELECT TO_CHAR(ttg.FILING_DATE,'YYYY-MON') AS bulan, COUNT(DISTINCT ttg.APPLICATION_NO) AS total, TO_CHAR(ttg.FILING_DATE,'YYYY-MM-MON')\r\n"
						+ "FROM TX_RECEPTION tr \r\n"
						+ "JOIN TX_TM_GENERAL ttg ON ttg.APPLICATION_NO = tr.APPLICATION_NO \r\n"
						+ "LEFT JOIN M_FILE_TYPE mft ON tr.FILE_TYPE_ID = mft.FILE_TYPE_ID\r\n"
						+ "LEFT JOIN TX_TM_CLASS ttc ON ttc.APPLICATION_ID = ttg.APPLICATION_ID \r\n"
						+ "LEFT JOIN TX_TM_BRAND ttb ON ttb.APPLICATION_ID = ttg.APPLICATION_ID \r\n"
						+ "LEFT JOIN TX_TM_OWNER tto ON tto.APPLICATION_ID = ttg.APPLICATION_ID \r\n"
						+ "LEFT JOIN TX_MONITOR tm ON tm.APPLICATION_ID = ttg.APPLICATION_ID\r\n"
						+ "LEFT JOIN M_WORKFLOW_PROCESS mwp ON mwp.WORKFLOW_PROCESS_ID = tm.WORKFLOW_PROCESS_ID AND mwp.STATUS_ID = ttg.STATUS_ID\r\n"
						+ "WHERE ttg.FILING_DATE IS NOT NULL AND tto.TM_OWNER_STATUS = 1 "
						+ whereStatement
						+ "GROUP BY TO_CHAR(ttg.FILING_DATE,'YYYY-MON'), TO_CHAR(ttg.FILING_DATE,'YYYY-MM-MON')\r\n"
						+ "ORDER BY TO_CHAR(ttg.FILING_DATE,'YYYY-MM-MON')")
				.getResultList();

		Map<String, Object> result = new HashMap<>();
		List<Integer> value = new ArrayList<Integer>();
		List<String> bulan = new ArrayList<String>();
		for (Object[] object : datas) {
			bulan.add(String.valueOf(object[0]));
			value.add(Integer.parseInt(String.valueOf(object[1])));
		}
		result.put("bulan", bulan);
		result.put("value", value);

		return result;
	}

	public List<Map<String, Object>> selectGrafikJenisPermohonan(Map<String, Object> requestMap) {
		String whereStatement = this.generateWhereStatement(requestMap);

		List<Object[]> datas = entityManager.createNativeQuery(
			"SELECT mftd.FILE_TYPE_DETAIL_DESC, count(DISTINCT tr.APPLICATION_NO) AS total FROM TX_RECEPTION tr \r\n"
				+ "JOIN TX_TM_GENERAL ttg ON ttg.APPLICATION_NO = tr.APPLICATION_NO \r\n"
				+ "JOIN M_FILE_TYPE_DETAIL mftd ON mftd.FILE_TYPE_DETAIL_ID = tr.FILE_TYPE_DETAIL_ID \r\n"
				+ "JOIN TX_TM_CLASS ttc ON ttc.APPLICATION_ID = ttg.APPLICATION_ID \r\n"
				+ "JOIN TX_TM_BRAND ttb ON ttb.APPLICATION_ID = ttg.APPLICATION_ID \r\n"
				+ "LEFT JOIN TX_TM_OWNER tto ON tto.APPLICATION_ID = ttg.APPLICATION_ID \r\n"
				+ "LEFT JOIN TX_MONITOR tm ON tm.APPLICATION_ID = ttg.APPLICATION_ID\r\n"
				+ "LEFT JOIN M_WORKFLOW_PROCESS mwp ON mwp.WORKFLOW_PROCESS_ID = tm.WORKFLOW_PROCESS_ID AND mwp.STATUS_ID = ttg.STATUS_ID\r\n"
				+ "WHERE tr.APPLICATION_NO IS NOT NULL AND tto.TM_OWNER_STATUS = 1 " + whereStatement
				+ "GROUP BY mftd.FILE_TYPE_DETAIL_DESC"
		).getResultList();

		Integer count = 0;

		for (Object[] object : datas) {
			Integer jenisCount = Integer.parseInt(String.valueOf(object[1]));
			count += jenisCount;
		}

		List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
		for (Object[] object : datas) {
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("name", String.valueOf(object[0]));
			data.put("y", Integer.parseInt(String.valueOf(object[1])));
			result.add(data);
		}

		return result;
	}

	public List<Map<String, Object>> selectGrafikTipeMerek(Map<String, Object> requestMap) {
		String whereStatement = this.generateWhereStatement(requestMap);

		List<Object[]> datas = entityManager.createNativeQuery(
			"SELECT mbt.BRAND_TYPE_NAME, count(DISTINCT tr.APPLICATION_NO) AS total FROM TX_TM_BRAND ttb \r\n"
				+ "JOIN TX_TM_GENERAL ttg ON ttg.APPLICATION_ID = ttb.APPLICATION_ID \r\n"
				+ "JOIN TX_TM_CLASS ttc ON ttc.APPLICATION_ID = ttg.APPLICATION_ID \r\n"
				+ "JOIN TX_RECEPTION tr ON tr.APPLICATION_NO = ttg.APPLICATION_NO \r\n"
				+ "JOIN M_BRAND_TYPE mbt ON mbt.BRAND_TYPE_ID = ttb.BRAND_TYPE_ID \r\n"
				+ "LEFT JOIN TX_TM_OWNER tto ON tto.APPLICATION_ID = ttg.APPLICATION_ID \r\n"
				+ "LEFT JOIN TX_MONITOR tm ON tm.APPLICATION_ID = ttg.APPLICATION_ID\r\n"
				+ "LEFT JOIN M_WORKFLOW_PROCESS mwp ON mwp.WORKFLOW_PROCESS_ID = tm.WORKFLOW_PROCESS_ID AND mwp.STATUS_ID = ttg.STATUS_ID\r\n"
				+ "WHERE tr.APPLICATION_NO IS NOT NULL AND tto.TM_OWNER_STATUS = 1 " + whereStatement
				+ "GROUP BY mbt.BRAND_TYPE_NAME"
		).getResultList();

		Integer count = 0;

		for (Object[] object : datas) {
			Integer tipeCount = Integer.parseInt(String.valueOf(object[1]));
			count += tipeCount;
		}

		List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
		for (Object[] object : datas) {
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("name", String.valueOf(object[0]));
			data.put("y", Integer.parseInt(String.valueOf(object[1])));
			result.add(data);
		}

		return result;
	}

	public Map<String, Object> selectAsal(Map<String, Object> requestMap) {
		String whereStatement = this.generateWhereStatement(requestMap);

		List<BigDecimal> asalResult = entityManager.createNativeQuery(
			"SELECT count(DISTINCT tr.APPLICATION_NO) AS total FROM TX_RECEPTION tr\r\n"
			+ "JOIN TX_TM_GENERAL ttg ON ttg.APPLICATION_NO = tr.APPLICATION_NO\r\n"
			+ "JOIN TX_TM_CLASS ttc ON ttc.APPLICATION_ID = ttg.APPLICATION_ID\r\n"
			+ "JOIN TX_TM_BRAND ttb ON ttb.APPLICATION_ID = ttg.APPLICATION_ID\r\n"
			+ "LEFT JOIN TX_TM_OWNER tto ON tto.APPLICATION_ID = ttg.APPLICATION_ID\r\n"
			+ "LEFT JOIN TX_MONITOR tm ON tm.APPLICATION_ID = ttg.APPLICATION_ID\r\n"
			+ "LEFT JOIN M_WORKFLOW_PROCESS mwp ON mwp.WORKFLOW_PROCESS_ID = tm.WORKFLOW_PROCESS_ID AND mwp.STATUS_ID = ttg.STATUS_ID\r\n"
			+ "WHERE tto.TM_OWNER_COUNTRY = 'ID' AND tto.TM_OWNER_STATUS = 1\r\n"
			+ whereStatement
			+ "UNION ALL\r\n"
			+ "SELECT count(DISTINCT tr.APPLICATION_NO) AS total FROM TX_RECEPTION tr\r\n"
			+ "JOIN TX_TM_GENERAL ttg ON ttg.APPLICATION_NO = tr.APPLICATION_NO\r\n"
			+ "JOIN TX_TM_CLASS ttc ON ttc.APPLICATION_ID = ttg.APPLICATION_ID\r\n"
			+ "JOIN TX_TM_BRAND ttb ON ttb.APPLICATION_ID = ttg.APPLICATION_ID\r\n"
			+ "LEFT JOIN TX_TM_OWNER tto ON tto.APPLICATION_ID = ttg.APPLICATION_ID\r\n"
			+ "LEFT JOIN TX_MONITOR tm ON tm.APPLICATION_ID = ttg.APPLICATION_ID\r\n"
			+ "LEFT JOIN M_WORKFLOW_PROCESS mwp ON mwp.WORKFLOW_PROCESS_ID = tm.WORKFLOW_PROCESS_ID AND mwp.STATUS_ID = ttg.STATUS_ID\r\n"
			+ "WHERE tto.TM_OWNER_COUNTRY != 'ID' AND tto.TM_OWNER_STATUS = 1\r\n"
			+ whereStatement
			+ "UNION ALL\r\n"
			+ "SELECT count(DISTINCT tr.APPLICATION_NO) AS total FROM TX_RECEPTION tr\r\n"
			+ "JOIN TX_TM_GENERAL ttg ON ttg.APPLICATION_NO = tr.APPLICATION_NO\r\n"
			+ "JOIN TX_TM_CLASS ttc ON ttc.APPLICATION_ID = ttg.APPLICATION_ID\r\n"
			+ "JOIN TX_TM_BRAND ttb ON ttb.APPLICATION_ID = ttg.APPLICATION_ID\r\n"
			+ "LEFT JOIN TX_TM_OWNER tto ON tto.APPLICATION_ID = ttg.APPLICATION_ID\r\n"
			+ "LEFT JOIN M_COUNTRY mc ON mc.COUNTRY_ID = tto.TM_OWNER_COUNTRY\r\n"
			+ "LEFT JOIN TX_MONITOR tm ON tm.APPLICATION_ID = ttg.APPLICATION_ID\r\n"
			+ "LEFT JOIN M_WORKFLOW_PROCESS mwp ON mwp.WORKFLOW_PROCESS_ID = tm.WORKFLOW_PROCESS_ID AND mwp.STATUS_ID = ttg.STATUS_ID\r\n"
			+ "WHERE mc.IS_MADRID = '1' AND tto.TM_OWNER_STATUS = 1\r\n"
			+ whereStatement
			)
			.getResultList();

		Integer asalN = 0;
		Integer asalLN = 0;
		Integer asalMadrid = 0;
		Integer index = 0;
		for (BigDecimal row : asalResult) {
			Integer value = row.intValue();
			if (index == 0) {
				asalN = value;
			} else if (index == 1) {
				asalLN = value;
			} else if (index == 2) {
				asalMadrid = value;
			}
			index++;
		}

		Map<String, Object> result = new HashMap<>();
		result.put("asalN", asalN);
		result.put("asalLN", asalLN);
		result.put("asalMadrid", asalMadrid);

		return result;
	}

	public Map<String, Object> selectGrafikKelas(Map<String, Object> requestMap) {
		String whereStatement = this.generateWhereStatement(requestMap);

		Map<String, Object> result = new HashMap<>();
		List<String> kelasList = new ArrayList<String>();
		List<Integer> pemohonCountList = new ArrayList<Integer>();

		List<Object[]> kelasPemohonData = entityManager.createNativeQuery(
			"SELECT ttc.CLASS_ID, COUNT(DISTINCT tr.APPLICATION_NO) AS total \r\n"
				+ "FROM TX_RECEPTION tr \r\n"
				+ "JOIN TX_TM_GENERAL ttg ON ttg.APPLICATION_NO = tr.APPLICATION_NO \r\n"
				+ "JOIN TX_TM_CLASS ttc ON ttc.APPLICATION_ID = ttg.APPLICATION_ID \r\n"
				+ "JOIN TX_TM_BRAND ttb ON ttb.APPLICATION_ID = ttg.APPLICATION_ID \r\n"
				+ "JOIN TX_TM_OWNER tto ON tto.APPLICATION_ID = ttg.APPLICATION_ID \r\n"
				+ "LEFT JOIN TX_MONITOR tm ON tm.APPLICATION_ID = ttg.APPLICATION_ID\r\n"
				+ "LEFT JOIN M_WORKFLOW_PROCESS mwp ON mwp.WORKFLOW_PROCESS_ID = tm.WORKFLOW_PROCESS_ID AND mwp.STATUS_ID = ttg.STATUS_ID\r\n"
				+ "WHERE ttg.FILING_DATE IS NOT NULL AND SUBSTR(ttc.CLASS_ID, 1, 1) = 'c' AND tto.TM_OWNER_STATUS = 1 "
				+ whereStatement
				+ "GROUP BY ttc.CLASS_ID\r\n"
				+ "ORDER BY TO_NUMBER(SUBSTR(ttc.CLASS_ID, 7))"
		).getResultList();

		for (Object[] kelasPemohon : kelasPemohonData) {
			String kelasString = String.valueOf(kelasPemohon[0]);
			if (kelasString.contains("class")) {
				kelasList.add(kelasString.substring(6));
			} else {
				kelasList.add(kelasString);
			}
			pemohonCountList.add(Integer.parseInt(String.valueOf(kelasPemohon[1])));
		}

		result.put("kelasList", kelasList);
		result.put("pemohonCountList", pemohonCountList);

		return result;
	}
}
