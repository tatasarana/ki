package com.docotel.ki.repository.custom.pojo;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class DashboardPascaPermohonanRepository {

	private String generateWhereStatement(Map<String, Object> requestMap) {
		String whereStatement = "";
		String status = requestMap.get("status").toString();
		String tipe = requestMap.get("tipe").toString();
		String start = requestMap.get("start").toString();
		String end = requestMap.get("end").toString();
		start = start.replace('/', '-');
		end = end.replace('/', '-');

		if (!start.equals("") && !end.equals("")) {
			whereStatement += "AND TO_CHAR(POST_RECEPTION_DATE,'YYYY-MM-DD') BETWEEN '"+start+"' AND '"+end+"' ";
		} else if (!start.equals("")) {
			whereStatement += "AND TO_CHAR(POST_RECEPTION_DATE,'YYYY-MM-DD') >= '" + start + "' ";
		} else if (!end.equals("")) {
			whereStatement += "AND TO_CHAR(POST_RECEPTION_DATE,'YYYY-MM-DD') <= '" + end + "' ";
		}
		if (!status.equals("")) {
			whereStatement += "AND tprd.STATUS_ID = '" + status + "' ";
		}
		if (!tipe.equals("")) {
			whereStatement += "AND tpr.FILE_TYPE_ID = '" + tipe + "' ";
		}
		return whereStatement;
	}

	@PersistenceContext
	protected EntityManager entityManager;

	public List<Map<String, Object>> selectTipePascaPermohonan(Map<String, Object> requestMap) {
		String whereStatement = this.generateWhereStatement(requestMap);

		List<Object[]> datas = entityManager
				.createNativeQuery(
					"SELECT mft.FILE_TYPE_DESC, COUNT(*) FROM MEREK.TX_POST_RECEPTION tpr \r\n"
						+ "JOIN MEREK.TX_POST_RECEPTION_DETAIL tprd ON tprd.POST_RECEPTION_ID = tpr.POST_RECEPTION_ID\r\n"
						+ "JOIN MEREK.M_FILE_TYPE mft ON mft.FILE_TYPE_ID = tpr.FILE_TYPE_ID \r\n"
						+ "WHERE tprd.STATUS_ID in ('DONE', 'NOTYET') "
						+ whereStatement
						+ " "
						+ "GROUP BY mft.FILE_TYPE_DESC \r\n"
						+ "ORDER BY mft.FILE_TYPE_DESC"
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

	public Map<String, Object> selectGrafikTipePermohonanPasca(Map<String, Object> requestMap) {
		String whereStatement = this.generateWhereStatement(requestMap);

		List<Object[]> datas = entityManager.createNativeQuery(
			"SELECT TO_CHAR(tpr.POST_RECEPTION_DATE,'YYYY-MON') AS bulan, COUNT(DISTINCT tpr.EFILING_NO) AS total\r\n"
				+ "FROM TX_POST_RECEPTION tpr\r\n"
				+ "JOIN MEREK.TX_POST_RECEPTION_DETAIL tprd ON tprd.POST_RECEPTION_ID = tpr.POST_RECEPTION_ID\r\n"
				+ "WHERE tpr.POST_RECEPTION_DATE IS NOT NULL AND tprd.STATUS_ID in ('DONE', 'NOTYET') "
				+ whereStatement
				+ "GROUP BY TO_CHAR(tpr.POST_RECEPTION_DATE,'YYYY-MON'), TO_CHAR(tpr.POST_RECEPTION_DATE,'YYYY-MM-MON')\r\n"
				+ "ORDER BY TO_CHAR(tpr.POST_RECEPTION_DATE,'YYYY-MM-MON')"
		).getResultList();

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

	public List<Map<String, Object>> selectGrafikStatusPasca(Map<String, Object> requestMap) {
		String whereStatement = this.generateWhereStatement(requestMap);
		List<Object[]> datas = entityManager.createNativeQuery(
			"SELECT (CASE WHEN tprd.STATUS_ID = 'DONE' THEN 'Sudah Dikerjakan' ELSE 'Belum Dikerjakan' END), COUNT(*) FROM MEREK.TX_POST_RECEPTION tpr \r\n"
				+ "JOIN MEREK.TX_POST_RECEPTION_DETAIL tprd ON tprd.POST_RECEPTION_ID = tpr.POST_RECEPTION_ID\r\n"
				+ "WHERE tprd.STATUS_ID in ('DONE', 'NOTYET') "
				+ whereStatement
				+ "GROUP BY tprd.STATUS_ID"
		).getResultList();

		List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
		for (Object[] object : datas) {
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("name", String.valueOf(object[0]));
			data.put("y", Integer.parseInt(String.valueOf(object[1])));
			result.add(data);
		}

		return result;
	}
}
