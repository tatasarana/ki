package com.docotel.ki.repository.custom.transaction;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.transaction.TxGroupDetail;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.util.DateUtil;
import org.springframework.stereotype.Repository;

@Repository
public class TxGroupDetailCustomRepository extends BaseRepository<TxGroupDetail> {
	@Override
	public List<TxGroupDetail> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		return super.selectAll("JOIN FETCH c.txGroup tg " +
				"LEFT JOIN FETCH tg.groupType tgt " +
				"JOIN FETCH c.txTmGeneral ttg " +
				"LEFT JOIN FETCH ttg.txTmBrand ttb " +
				"LEFT JOIN FETCH ttg.txTmClassList ttc " +
				"LEFT JOIN FETCH ttc.mClass mc " +
				"LEFT JOIN FETCH ttg.txTmOwner tto " +
				"LEFT JOIN FETCH ttg.mStatus ms " +
				"LEFT JOIN FETCH ttg.txReception tr " +
				"LEFT JOIN FETCH tr.mFileType mft " +
				"LEFT JOIN FETCH tr.mFileTypeDetail mftd ", searchCriteria, orderBy, orderType, offset, limit);
	}

	public List<TxGroupDetail> selectReportEkspedisi(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		return super.selectAll("JOIN FETCH c.txGroup tg " +
						"JOIN FETCH c.txTmGeneral ttg " +
						"LEFT JOIN FETCH ttg.txTmBrand ttb " +
						"LEFT JOIN FETCH ttg.txTmClassList ttc " +
						"LEFT JOIN FETCH ttc.mClass mc "
				, searchCriteria, orderBy, orderType, offset, limit);
	}

	public TxGroupDetail selectOne(String by, Object value) {
		return super.selectOne("JOIN FETCH c.txGroup tg JOIN FETCH c.txTmGeneral ttg", by, value, true);
	}

	public void deleteTxGroupDetailByBatch(String[] groupDetailId) {
		int oracleLimit = 1000;
		String SQLcmd = "DELETE FROM TX_GROUP_DETAIL WHERE ";
		for (int i = 0; i <= groupDetailId.length/oracleLimit; i++) {
			if (oracleLimit*i == groupDetailId.length){
				break;
			}
			String newWhereClause = (i==0?"":" OR ")+"GROUP_DETAIL_ID IN (";
			for (int j = 0 ; j < (groupDetailId.length>(oracleLimit*(i+1))?oracleLimit:groupDetailId.length); j++) {
				if (j+(oracleLimit*i) >= groupDetailId.length){
					break;
				}
				newWhereClause += (j==0?"":",")+ "'" +groupDetailId[j+(oracleLimit*i)] + "'";
			}
			SQLcmd += newWhereClause+")";
		}
		Query query = entityManager.createNativeQuery(SQLcmd);
		query.executeUpdate();
	}

	public void saveTxGroupDetailByBatch(List<TxGroupDetail> txGroupDetailList) {
		for (TxGroupDetail txGroupDetail : txGroupDetailList) {
			String nativeQuery = "INSERT INTO TX_GROUP_DETAIL " +
					"(GROUP_DETAIL_ID, SKIP_AUDIT_TRAIL, GROUP_DETAIL_STATUS, GROUP_DETAIL_USER_ID_1, GROUP_DETAIL_USER_ID_2, GROUP_DETAIL_USER_ID_CURRENT, GROUP_ID, APPLICATION_ID) " +
					"VALUES " +
					"(:p1, :p2, :p3, :p4, :p5, :p6, :p7, :p8)";

			Query query = entityManager.createNativeQuery(nativeQuery)
					.setParameter("p1", txGroupDetail.getId())
					.setParameter("p2", txGroupDetail.isSkipLogAuditTrail())
					.setParameter("p3", txGroupDetail.getStatus())
					.setParameter("p4", txGroupDetail.getmUser1() == null ? null : txGroupDetail.getmUser1().getId())
					.setParameter("p5", txGroupDetail.getmUser2() == null ? null : txGroupDetail.getmUser2().getId())
					.setParameter("p6", txGroupDetail.getmUserCurrent() == null ? null : txGroupDetail.getmUserCurrent().getId())
					.setParameter("p7", txGroupDetail.getTxGroup() == null ? null : txGroupDetail.getTxGroup().getId())
					.setParameter("p8", txGroupDetail.getTxTmGeneral().getId());
			query.executeUpdate();
		}
	}

	public long countIncServDist(List<KeyValue> searchCriteria) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT COUNT(c) FROM " + getClassName() + " c ");
			sbQuery.append("JOIN c.txGroup tg ");
			sbQuery.append("JOIN c.txTmGeneral ttg ");
//			sbQuery.append("LEFT JOIN ttg.txTmPriorList ttp ");
			sbQuery.append("JOIN tg.txServDist tsd ");
			sbQuery.append("WHERE 1 = 1 ");
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (searchBy.equalsIgnoreCase("mClassId")) {
							sbQuery.append(" AND c.txTmGeneral IN (SELECT ttc.txTmGeneral FROM TxTmClass ttc LEFT JOIN ttc.mClass mc WHERE LOWER(mc.id) LIKE :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("groupDetailStatus")) {
							sbQuery.append(" AND LOWER(c.status) != :p" + i);
						} else if(searchBy.equalsIgnoreCase("txTmGeneral.filingDate") ) {
							sbQuery.append(" AND (c.txTmGeneral.filingDate >= :startDate AND c.txTmGeneral.filingDate <= :endDate)");
						} else {
							if (value == null) {
								sbQuery.append(" AND c." + searchBy + " IS NULL");
							} else if (value instanceof String) {
								if (searchCriteria.get(i).isExactMatch()) {
									sbQuery.append(" AND LOWER(c." + searchBy + ") = :p" + i);
								} else {
									sbQuery.append(" AND LOWER(c." + searchBy + ") LIKE :p" + i);
								}
							} else {
								sbQuery.append(" AND c." + searchBy + " = :p" + i);
							}
						}
					}
				}
			}

			Query query = entityManager.createQuery(sbQuery.toString());
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					Object value = searchCriteria.get(i).getValue();
					String searchBy = searchCriteria.get(i).getKey();
					if (value != null) {
						if ("txTmGeneral.filingDate".equalsIgnoreCase(searchBy)) {
							query.setParameter("startDate", value);

							Calendar calendar = Calendar.getInstance(DateUtil.LOCALE_ID);
							calendar.setTimeInMillis(((Timestamp) value).getTime());
							calendar.set(Calendar.HOUR_OF_DAY, 23);
							calendar.set(Calendar.MINUTE, 59);
							calendar.set(Calendar.SECOND, 59);
							calendar.set(Calendar.MILLISECOND, 999);
							query.setParameter("endDate", calendar.getTime());
						} else if("mUserCurrent".equalsIgnoreCase(searchBy)){
							query.setParameter("p" + i, (MUser) value );
						}
						else {
							if (value instanceof String) {
								if (searchCriteria.get(i).isExactMatch()) {
									query.setParameter("p" + i, ((String) value).toLowerCase());
								} else {
									query.setParameter("p" + i, "%" + ((String) value).toLowerCase() + "%");
								}
							} else {
								query.setParameter("p" + i, value);
							}
						}
					}
				}
			}
			return (Long) query.setMaxResults(1).getSingleResult();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return 0L;
	}

	public long countTxGroupDetail(List<KeyValue> searchCriteria) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT COUNT(c) FROM " + getClassName() + " c ");
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (value == null) {
							sbQuery.append(" AND c." + searchBy + " IS NULL");
						} else if (value instanceof String) {
							if (searchCriteria.get(i).isExactMatch()) {
								sbQuery.append(" AND LOWER(c." + searchBy + ") = :p" + i);
							} else {
								sbQuery.append(" AND LOWER(c." + searchBy + ") LIKE :p" + i);
							}
						} else {
							sbQuery.append(" AND c." + searchBy + " = :p" + i);
						}
					}
				}
			}

			Query query = entityManager.createQuery(sbQuery.toString());
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					Object value = searchCriteria.get(i).getValue();
					if (value != null) {
						if (value instanceof String) {
							if (searchCriteria.get(i).isExactMatch()) {
								query.setParameter("p" + i, ((String) value).toLowerCase());
							} else {
								query.setParameter("p" + i, "%" + ((String) value).toLowerCase() + "%");
							}
						} else {
							query.setParameter("p" + i, value);
						}
					}
				}
			}
			return (Long) query.setMaxResults(1).getSingleResult();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return 0L;
	}

	public List<Object[]> searchGroupDetailByGroupId(String groupId) {
		String appendQuery = "select GROUP_DETAIL_ID, APPLICATION_ID, FILING_DATE, APPLICATION_NO, TM_BRAND_NAME, STATUS_NAME, " +
				"LISTAGG(CLASS_NO, ', ') WITHIN GROUP (ORDER BY rownum) CLASS_NO," +
				"LISTAGG(TM_OWNER_NAME, ', ') WITHIN GROUP (ORDER BY rownum) TM_OWNER_NAME " +
				"FROM (" +
				" select tgd.GROUP_DETAIL_ID, tg.APPLICATION_ID, tg.FILING_DATE, tg.APPLICATION_NO, tb.TM_BRAND_NAME, ms.STATUS_NAME, " +
				" mc.CLASS_NO, tto.TM_OWNER_NAME" +
				" from TX_GROUP_DETAIL tgd" +
				" LEFT JOIN TX_GROUP tgp ON tgd.GROUP_ID = tgp.GROUP_ID" +
				" LEFT JOIN TX_TM_GENERAL tg ON tg.APPLICATION_ID = tgd.APPLICATION_ID" +
				" LEFT JOIN TX_TM_BRAND tb on tb.APPLICATION_ID = tg.APPLICATION_ID" +
				" LEFT JOIN TX_TM_CLASS tc ON tc.APPLICATION_ID = tg.APPLICATION_ID" +
				" LEFT JOIN M_CLASS mc ON mc.CLASS_ID = tc.CLASS_ID" +
				" LEFT JOIN TX_TM_OWNER tto ON tto.APPLICATION_ID = tg.APPLICATION_ID" +
				" LEFT JOIN M_STATUS ms ON ms.STATUS_ID = tg.STATUS_ID" +
				" where tgd.GROUP_ID = :pGroupId " +
				" GROUP BY tgd.GROUP_DETAIL_ID, tg.APPLICATION_ID, tg.FILING_DATE, tg.APPLICATION_NO, tb.TM_BRAND_NAME, ms.STATUS_NAME, mc.CLASS_NO, tto.TM_OWNER_NAME" +
				") X " +
				"group by GROUP_DETAIL_ID, APPLICATION_ID, FILING_DATE, APPLICATION_NO, TM_BRAND_NAME, STATUS_NAME" +
				" ORDER BY FILING_DATE ASC";
		Query query = entityManager.createNativeQuery(appendQuery)
				.setParameter("pGroupId", groupId);
		return query.getResultList();
	}

	public List<TxGroupDetail> selectIncServDist(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
			sbQuery.append("JOIN c.txGroup tg ");
			sbQuery.append("JOIN c.txTmGeneral ttg ");
//			sbQuery.append("LEFT JOIN ttg.txTmPriorList ttp ");
			sbQuery.append("JOIN tg.txServDist tsd ");
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (searchBy.equalsIgnoreCase("mClassId")) {
							sbQuery.append(" AND c.txTmGeneral IN (SELECT ttc.txTmGeneral FROM TxTmClass ttc LEFT JOIN ttc.mClass mc WHERE LOWER(mc.id) LIKE :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("groupDetailStatus")) {
							sbQuery.append(" AND LOWER(c.status) != :p" + i);
						} else if(searchBy.equalsIgnoreCase("txTmGeneral.filingDate") ) {
							sbQuery.append(" AND (c.txTmGeneral.filingDate >= :startDate AND c.txTmGeneral.filingDate <= :endDate)");
						} else {
							if (value == null) {
								sbQuery.append(" AND c." + searchBy + " IS NULL");
							} else if (value instanceof String) {
								if (searchCriteria.get(i).isExactMatch()) {
									sbQuery.append(" AND LOWER(c." + searchBy + ") = :p" + i);
								} else {
									sbQuery.append(" AND LOWER(c." + searchBy + ") LIKE :p" + i);
								}
							} else {
								sbQuery.append(" AND c." + searchBy + " = :p" + i);
							}
						}
					}
				}
			}
			if (orderBy != null && orderType != null) {
				if (orderBy.equalsIgnoreCase("mClass.no")) {
					sbQuery.append(" ORDER BY mc.no " + orderType);
				} else if (orderBy.equalsIgnoreCase("txTmGeneral.priorList")) {
					sbQuery.append(" ORDER BY ttg.priorList " + orderType);
				} else if (orderBy.equalsIgnoreCase("txTmGeneral.txTmOwner.name")) {
					sbQuery.append(" ORDER BY tto.name " + orderType);
				} else if (orderBy.equalsIgnoreCase("groupDetailStatus")) {
					sbQuery.append(" ORDER BY c.status " + orderType);
				} else {
					sbQuery.append(" ORDER BY c." + orderBy + " " + orderType);
				}
			}

			Query query = entityManager.createQuery(sbQuery.toString());
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					Object value = searchCriteria.get(i).getValue();
					String searchBy = searchCriteria.get(i).getKey();
					if (value != null) {
						if (searchBy.equalsIgnoreCase("txTmGeneral.filingDate")) {
							query.setParameter("startDate", value);

							Calendar calendar = Calendar.getInstance(DateUtil.LOCALE_ID);
							calendar.setTimeInMillis(((Timestamp) value).getTime());
							calendar.set(Calendar.HOUR_OF_DAY, 23);
							calendar.set(Calendar.MINUTE, 59);
							calendar.set(Calendar.SECOND, 59);
							calendar.set(Calendar.MILLISECOND, 999);
							query.setParameter("endDate", calendar.getTime());
						} else {
							if (value instanceof String) {
								if (searchCriteria.get(i).isExactMatch()) {
									query.setParameter("p" + i, ((String) value).toLowerCase());
								} else {
									query.setParameter("p" + i, "%" + ((String) value).toLowerCase() + "%");
								}
							} else {
								query.setParameter("p" + i, value);
							}
						}
					}
				}
			}

			if (offset != null) {
				query.setFirstResult(offset);
			}
			if (limit != null) {
				query.setMaxResults(limit);
			}
			return query.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	public List<Object[]> searchGroupDetailByJournalNo(String journalNo) {
		String queryS = "select APPLICATION_NO, FILING_DATE, FILE_TYPE_DESC, FILE_TYPE_DETAIL_DESC, TM_BRAND_NAME, " +
				"CLASS_LIST," +
				"LISTAGG(TM_OWNER_NAME, ', ') WITHIN GROUP (ORDER BY rownum) TM_OWNER_NAME " +
				"FROM (" +
				"    select distinct c.APPLICATION_NO, c.FILING_DATE, d.TM_BRAND_NAME, h.FILE_TYPE_DETAIL_DESC, f.FILE_TYPE_DESC, " +
				"    c.CLASS_LIST," +
				"    j.TM_OWNER_NAME" +
				"    from TX_PUBS_JOURNAL a" +
				"    left join TX_GROUP_DETAIL b on b.GROUP_ID = a.GROUP_ID" +
				"    left join TX_TM_GENERAL c on c.APPLICATION_ID = b.APPLICATION_ID" +
				"    left join TX_TM_BRAND d on d.APPLICATION_ID = c.APPLICATION_ID" +
				"    left join TX_RECEPTION e on e.RECEPTION_ID = c.RECEPTION_ID" +
				"    left join M_FILE_TYPE f on f.FILE_TYPE_ID = e.FILE_TYPE_ID" +
				"    left join M_FILE_TYPE_DETAIL h on h.FILE_TYPE_DETAIL_ID = e.FILE_TYPE_DETAIL_ID" +
				"    left join TX_TM_OWNER j on j.APPLICATION_ID = c.APPLICATION_ID" +
				"    where a.JOURNAL_NO = :pJournal " +
				"    group by c.APPLICATION_NO, c.FILING_DATE, d.TM_BRAND_NAME, h.FILE_TYPE_DETAIL_DESC, f.FILE_TYPE_DESC, c.CLASS_LIST, j.TM_OWNER_NAME" +
				") X " +
				"group by APPLICATION_NO, FILING_DATE, TM_BRAND_NAME, FILE_TYPE_DETAIL_DESC, FILE_TYPE_DESC, CLASS_LIST "+
				"order by FILING_DATE ASC";
		Query query = entityManager.createNativeQuery(queryS)
				.setParameter("pJournal", journalNo);
		return query.getResultList();
	}
}
