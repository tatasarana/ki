package com.docotel.ki.repository.custom.pojo;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.pojo.DataBRM;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class DashboardRegAkunRepository {

	private String generateWhereStatement(Map<String, Object> requestMap) {
		String whereStatement = "";
		String status = requestMap.get("status").toString();
		String tipe = requestMap.get("tipe").toString();
		String start = requestMap.get("start").toString();
		String end = requestMap.get("end").toString();
		start = start.replace('/', '-');
		end = end.replace('/', '-');

		if (!start.equals("") && !end.equals("")) {
			whereStatement += "AND TO_CHAR(tor.CREATED_DATE,'YYYY-MM-DD') BETWEEN '"+start+"' AND '"+end+"' ";
		} else if (!start.equals("")) {
			whereStatement += "AND TO_CHAR(tor.CREATED_DATE,'YYYY-MM-DD') >= '" + start + "' ";
		} else if (!end.equals("")) {
			whereStatement += "AND TO_CHAR(tor.CREATED_DATE,'YYYY-MM-DD') <= '" + end + "' ";
		}
		if (!status.equals("")) {
			whereStatement += "AND tor.APPROVAL_STATUS = '" + status + "' ";
		}
		if (!tipe.equals("")) {
			whereStatement += "AND tor.ONLINE_APPLICANT_TYPE = '" + tipe + "' ";
		}
		return whereStatement;
	}

	@PersistenceContext
	protected EntityManager entityManager;

	public List<Map<String, Object>> selectRegAkun(Map<String, Object> requestMap) {
		String whereStatement = this.generateWhereStatement(requestMap);
		
		List<Object[]> datas = entityManager.createNativeQuery(
			"SELECT tor.ONLINE_APPLICANT_TYPE, COUNT(*) total\r\n"
				+ "FROM TX_ONLINE_REG tor\r\n"
				+ "WHERE tor.ONLINE_APPLICANT_TYPE IS NOT NULL "
				+ whereStatement
				+ "GROUP BY tor.ONLINE_APPLICANT_TYPE"
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
	
	public Map<String, Object> selectGrafikTipePermohonanDaftar(Map<String, Object> requestMap) {
		String whereStatement = this.generateWhereStatement(requestMap);
		
		List<Object[]> datas = entityManager.createNativeQuery(
			"SELECT TO_CHAR(tor.CREATED_DATE,'YYYY-MON') AS bulan, COUNT(*) AS total\r\n"
				+ "FROM TX_ONLINE_REG tor\r\n"
				+ "WHERE tor.ONLINE_APPLICANT_TYPE IS NOT NULL "
				+ whereStatement
				+ "GROUP BY TO_CHAR(tor.CREATED_DATE,'YYYY-MON'), TO_CHAR(tor.CREATED_DATE,'YYYY-MM-MON')\r\n"
				+ "ORDER BY TO_CHAR(tor.CREATED_DATE,'YYYY-MM-MON')"
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
	
	public List<Map<String, Object>> selectGrafikStatus(Map<String, Object> requestMap) {
		String whereStatement = this.generateWhereStatement(requestMap);
		List<Object[]> datas = entityManager.createNativeQuery(
			"SELECT APPROVAL_STATUS, COUNT(*) FROM MEREK.TX_ONLINE_REG tor\r\n"
			+ "WHERE tor.ONLINE_APPLICANT_TYPE IS NOT NULL "
			+ whereStatement
			+ "GROUP BY APPROVAL_STATUS"
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
