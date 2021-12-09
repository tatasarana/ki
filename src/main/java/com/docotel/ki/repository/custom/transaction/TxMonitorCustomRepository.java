package com.docotel.ki.repository.custom.transaction;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.master.MWorkflowProcess;
import com.docotel.ki.model.transaction.TxMonitor;
import com.docotel.ki.pojo.KeyValue;

import com.docotel.ki.service.master.UserService;
import com.ibm.icu.text.SimpleDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

@Repository
public class TxMonitorCustomRepository extends BaseRepository<TxMonitor> {
	@Autowired
	private UserService userService;

	public TxMonitor selectOne(String by, Object value) {
		return super.selectOne("LEFT JOIN FETCH c.txTmGeneral cb ", by, value, true);
	}

	public TxMonitor selectOneByAppId(Object value1, String value2) {
		try {
			return (TxMonitor) entityManager.createQuery(
					"SELECT c FROM " + getClassName() + " c " +
							"JOIN FETCH c.txTmGeneral t " +
							"JOIN FETCH c.mWorkflowProcess mw " +
							"WHERE c.txTmGeneral.id = :p1 AND c.mWorkflowProcess.id = :p2")
					.setParameter("p1", value1)
					.setParameter("p2", value2)
					.setMaxResults(1)
					.getSingleResult();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	public void deleteTxMonitorByBatch(String[] txMonitorList) {
		int oracleLimit = 1000;
		String SQLcmd = "DELETE FROM TX_MONITOR WHERE ";
		String newWhereClause = "";
		for (int i = 0; i <= txMonitorList.length/oracleLimit; i++) {
			if (oracleLimit*i == txMonitorList.length){
				break;
			}
			String tempClause = (i==0?"":" OR ")+"MONITOR_ID IN (";
			for (int j = 0 ; j < (txMonitorList.length>(oracleLimit*(i+1))?oracleLimit:txMonitorList.length); j++) {
				if (j+(oracleLimit*i) >= txMonitorList.length){
					break;
				}
				tempClause += (j==0?"":",")+ "'" +txMonitorList[j+(oracleLimit*i)] + "'";
			}
			newWhereClause += tempClause+")";
		}
		Query query = entityManager.createNativeQuery(SQLcmd+newWhereClause);
		query.executeUpdate();
	}

	public long countLap(List<KeyValue> searchCriteria) {
		try {
			return (long) entityManager.createQuery(
					"SELECT COUNT(c) FROM " + getClassName() + " c " +
							"LEFT JOIN c.mWorkflowProcessActions wfact " +
							"GROUP BY c.mWorkflowProcessActions")
					.setMaxResults(1)
					.getSingleResult();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return 1L;
	}

	public List<TxMonitor> findByDueDate(String duedate) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
			sbQuery.append(" WHERE c.dueDate <= :p1 ORDER BY c.dueDate");

			Query query = entityManager.createQuery(sbQuery.toString());

			query.setParameter("p1", duedate);

			return query.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	public List<TxMonitor> findByTxTmGeneralId(String generalId) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c " +
					"JOIN FETCH c.txTmGeneral tg ");
			sbQuery.append(" WHERE c.txTmGeneral.id = :p1");
			sbQuery.append(" ORDER BY c.createdDate DESC");

			Query query = entityManager.createQuery(sbQuery.toString());
			query.setParameter("p1", generalId);

			return query.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	public List<TxMonitor> findByTxTmGeneralId2(String generalId) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c " +
					"JOIN FETCH c.txTmGeneral tg ");
			sbQuery.append(" WHERE c.txTmGeneral.id = :p1");
			sbQuery.append(" AND c.mWorkflowProcess.status != 'TM_PASCA_PERMOHONAN'");
			sbQuery.append(" AND c.mWorkflowProcess != 'TM_USERDOC'");
			sbQuery.append(" ORDER BY c.createdDate DESC");

			Query query = entityManager.createQuery(sbQuery.toString());
			query.setParameter("p1", generalId);

			return query.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	public long countAllMonitor(List<KeyValue> searchCriteria) {
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
								sbQuery.append(" AND c." + searchBy + " = :p" + i);
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
								query.setParameter("p" + i, ((String) value));
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

	public List<TxMonitor> selectAllMonitor(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (value == null) {
							sbQuery.append(" AND c." + searchBy + " IS NULL");
						} else if (value instanceof String) {
							if (searchCriteria.get(i).isExactMatch()) {
								sbQuery.append(" AND c." + searchBy + " = :p" + j++);
							} else {
								sbQuery.append(" AND LOWER(c." + searchBy + ") LIKE :p" + j++);
							}
						} else if (value instanceof List) {
							if (((List) value).size() > 0) {
								sbQuery.append(" AND LOWER(c." + searchBy + ") IN (");
								for (int k = 0; k < ((List) value).size(); k++) {
									sbQuery.append(":p" + j++);
									if (k < ((List) value).size() - 1) {
										sbQuery.append(", ");
									}
								}
								sbQuery.append(")");
							}
						} else {
							sbQuery.append(" AND c." + searchBy + " = :p" + j++);
						}
					}
				}
			}

			if (orderBy != null && orderType != null) {
				sbQuery.append(" ORDER BY c." + orderBy + " " + orderType);
			}

			Query query = entityManager.createQuery(sbQuery.toString());
			if (searchCriteria != null) {
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					Object value = searchCriteria.get(i).getValue();
					if (value != null) {
						if (value instanceof String) {
							if (searchCriteria.get(i).isExactMatch()) {
								query.setParameter("p" + j++, ((String) value));
							} else {
								query.setParameter("p" + j++, "%" + ((String) value).toLowerCase() + "%");
							}
						} else if (value instanceof List) {
							for (Object subValue : (List) value) {
								if (subValue instanceof String) {
									query.setParameter("p" + j++, ((String) subValue).toLowerCase());
								} else {
									query.setParameter("p" + j++, subValue);
								}
							}
						} else {
							query.setParameter("p" + j++, value);
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

	public long countLapRekapStatusDanAksiMingguan(List<KeyValue> searchCriteria) {
		Timestamp startDate = null;
		Timestamp endDate = null;
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
						} else if (searchBy.equalsIgnoreCase("mWorkflowProcessActions.mAction.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE mWorkflowProcessActions.action.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("mWorkflowProcess.status.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE mWorkflowProcess.status.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txReception.mFileSequence.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE txTmGeneral.txReception.mFileSequence.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txReception.mFileType.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE txTmGeneral.txReception.mFileType.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txTmBrand.mBrandType.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE txTmGeneral.txTmBrand.mBrandType.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txTmClassList")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c LEFT JOIN c.txTmGeneral tg LEFT JOIN tg.txTmClassList ttc WHERE ttc.mClass.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("createdBy.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE createdBy.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("createdDateMonth")) {
							sbQuery.append(" AND c.id IN (SELECT tpj FROM TxMonitor tpj WHERE TO_CHAR(tpj.createdDate, 'MM') = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("startDate")) {
							sbQuery.append(" AND c.createdDate >= :p" + i);
						} else if (searchBy.equalsIgnoreCase("endDate")) {
							sbQuery.append(" AND c.createdDate <= :p" + i);
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
								if(String.valueOf(value).equals("SUPER")) {
									query.setParameter("p" + i, ((String) value));
								} else {
									query.setParameter("p" + i, ((String) value).toLowerCase());
								}
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

	public List<TxMonitor> selectAllFungsionalPemeriksa(List<KeyValue> searchCriteria,String orderBy,String orderType, Integer offset, Integer limit){
		Timestamp startDate = null;
		Timestamp endDate = null;
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();
					//System.out.println(">>> Class: "+getClassName()+" searchCriteria ==> searchBy:" + searchBy + " , value:" + value.toString());
					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (value != null) {
							if (searchBy.equalsIgnoreCase("startDate")) {
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								Date startStringDate = sdf.parse(value.toString().substring(0,10)+" 00:00:01");
								startDate = new Timestamp(startStringDate.getTime());
								sbQuery.append(" AND c.createdDate >= :startDate");
							} else if (searchBy.equalsIgnoreCase("endDate")) {
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								Date endStringDate = sdf.parse(value.toString().substring(0, 10) + " 23:59:58");
								endDate = new Timestamp(endStringDate.getTime());
								sbQuery.append(" AND c.createdDate <= :endDate");
							} else if (searchBy.equalsIgnoreCase("mAction.id")) {
								sbQuery.append(" AND c.mWorkflowProcessActions.action.id = :p" + i);
							} else if (searchBy.equalsIgnoreCase("mStatus.id")) {
								sbQuery.append(" AND c.mWorkflowProcess.status.id = :p" + i);
							} else if (searchBy.equalsIgnoreCase("createdBy.id")) {
								sbQuery.append(" AND c.createdBy.id = :p" + i);
							} else {
								sbQuery.append(" AND c." + searchBy + " = :p" + i);
							}
						}
					}
					if (value == null) {
						sbQuery.append(" AND c." + searchBy + " IS NULL");
					}
				}
			}

			if (orderBy != null && orderType != null) {
				sbQuery.append(" ORDER BY c." + orderBy + " " + orderType);
			}

			Query query = entityManager.createQuery(sbQuery.toString());
			if (searchCriteria != null) {
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();
					if (value != null) {
						if (searchBy.contentEquals("startDate")) {
							query.setParameter(searchBy, startDate);
						}else if(searchBy.contentEquals("endDate")) {
							query.setParameter(searchBy, endDate);
						} else if (value instanceof String && !(searchBy.contentEquals("endDate")||searchBy.contentEquals("startDate"))) {
							if (searchCriteria.get(i).isExactMatch()) {
								query.setParameter("p" + i, ((String) value));
							} else {
								query.setParameter("p" + i, "%" + ((String) value).toLowerCase() + "%");
							}
						} else if (value instanceof List) {
							for (Object subValue : (List) value) {
								if (subValue instanceof String) {
									query.setParameter("p" + j++, ((String) subValue).toLowerCase());
								} else {
									query.setParameter("p" + j++, subValue);
								}
							}
						} else {
							query.setParameter("p" + j++, value);
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
			//System.out.println("QUERY "+sbQuery.toString());
			return query.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	public long countFungsionalPemeriksa(List<KeyValue> searchCriteria ){
		Timestamp startDate = null;
		Timestamp endDate = null;
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT COUNT(c) FROM " + getClassName() + " c ");
			sbQuery.append("WHERE 1 = 1 ");
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();
					//System.out.println(">>> Class: "+getClassName()+" searchCriteria ==> searchBy:" + searchBy + " , value:" + value.toString());
					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (value != null) {
							if (searchBy.equalsIgnoreCase("startDate")) {
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								Date startStringDate = sdf.parse(value.toString().substring(0,10)+" 00:00:01");
								startDate = new Timestamp(startStringDate.getTime());
								sbQuery.append(" AND c.createdDate >= :startDate");
							} else if (searchBy.equalsIgnoreCase("endDate")) {
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								Date endStringDate = sdf.parse(value.toString().substring(0, 10) + " 23:59:58");
								endDate = new Timestamp(endStringDate.getTime());
								sbQuery.append(" AND c.createdDate <= :endDate");
							} else if (searchBy.equalsIgnoreCase("mAction.id")) {
								sbQuery.append(" AND c.mWorkflowProcessActions.action.id = :p" + i);
							} else if (searchBy.equalsIgnoreCase("mStatus.id")) {
								sbQuery.append(" AND c.mWorkflowProcess.status.id = :p" + i);
							} else if (searchBy.equalsIgnoreCase("createdBy.id")) {
								sbQuery.append(" AND c.createdBy.id = :p" + i);
							}
						}
					}
					if (value == null) {
						sbQuery.append(" AND c." + searchBy + " IS NULL");
					}
				}

			}

			Query query = entityManager.createQuery(sbQuery.toString());
			if (searchCriteria != null ) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();
					if (value != null) {
						if (searchBy.contentEquals("startDate")) {
							query.setParameter(searchBy, startDate);
						} else if(searchBy.contentEquals("endDate")) {
							query.setParameter(searchBy, endDate);
						} else if (value instanceof String && !(searchBy.contentEquals("endDate")||searchBy.contentEquals("startDate"))) {
							if (searchCriteria.get(i).isExactMatch()) {
								query.setParameter("p" + i, ((String) value));
								//System.out.println(" P"+i+" VALUE === " +((String) value).toLowerCase());
							} else {
								query.setParameter("p" + i, "%" + ((String) value).toLowerCase() + "%");
							}
						} else {
							query.setParameter("p" + i, value);
						}
					}

				}
			}
			//System.out.println("QUERY "+sbQuery.toString());
			return (Long) query.setMaxResults(1).getSingleResult();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return 0L;
	}

	public long countLapPasca(List<KeyValue> searchCriteria ){
		Timestamp startDate = null;
		Timestamp endDate = null;
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT COUNT(c) FROM " + getClassName() + " c ");
			sbQuery.append("WHERE 1 = 1 ");
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();
					//System.out.println(">>> Class: "+getClassName()+" searchCriteria ==> searchBy:" + searchBy + " , value:" + value.toString());
					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (value != null) {
							if (searchBy.equalsIgnoreCase("startDate")) {
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								Date startStringDate = sdf.parse(value.toString().substring(0,10)+" 00:00:01");
								startDate = new Timestamp(startStringDate.getTime());
								sbQuery.append(" AND c.createdDate >= :startDate");
							} else if (searchBy.equalsIgnoreCase("endDate")) {
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								Date endStringDate = sdf.parse(value.toString().substring(0, 10) + " 23:59:58");
								endDate = new Timestamp(endStringDate.getTime());
								sbQuery.append(" AND c.createdDate <= :endDate");
							} else if (searchBy.equalsIgnoreCase("mAction.id")) {
								sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE c.mWorkflowProcessActions.action.id = :p" + i + ")");
							} else if (searchBy.equalsIgnoreCase("mFileType.id")) {
								sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE c.txPostReception.mFileType.id = :p" + i + ")");
							} else if (searchBy.equalsIgnoreCase("createdBy.id")) {
								sbQuery.append(" AND c.createdBy.id = :p" + i + ")");
							}
						}
					}
					if (value == null) {
						sbQuery.append(" AND c." + searchBy + " IS NULL");
					}
				}

			}

			Query query = entityManager.createQuery(sbQuery.toString());
			if (searchCriteria != null ) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();
					if (value != null) {
						if (searchBy.contentEquals("startDate")) {
							query.setParameter(searchBy, startDate);
						} else if(searchBy.contentEquals("endDate")) {
							query.setParameter(searchBy, endDate);
						} else if (value instanceof String && !(searchBy.contentEquals("endDate")||searchBy.contentEquals("startDate"))) {
							if (searchCriteria.get(i).isExactMatch()) {
								query.setParameter("p" + i, ((String) value));
								//System.out.println(" P"+i+" VALUE === " +((String) value).toLowerCase());
							} else {
								query.setParameter("p" + i, "%" + ((String) value) + "%");
							}
						} else {
							query.setParameter("p" + i, value);
						}
					}

				}
			}
			//System.out.println("QUERY "+sbQuery.toString());
			return (Long) query.setMaxResults(1).getSingleResult();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return 0L;
	}

	public List<TxMonitor>  selectAllLapPasca(List<KeyValue> searchCriteria,String orderBy,String orderType, Integer offset, Integer limit){		Timestamp startDate = null;
		Timestamp endDate = null;
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();
					//System.out.println(">>> Class: "+getClassName()+" searchCriteria ==> searchBy:" + searchBy + " , value:" + value.toString());
					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (value != null) {
							if (searchBy.equalsIgnoreCase("startDate")) {
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								Date startStringDate = sdf.parse(value.toString().substring(0,10)+" 00:00:01");
								startDate = new Timestamp(startStringDate.getTime());
								sbQuery.append(" AND c.createdDate >= :startDate");
							} else if (searchBy.equalsIgnoreCase("endDate")) {
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								Date endStringDate = sdf.parse(value.toString().substring(0, 10) + " 23:59:58");
								endDate = new Timestamp(endStringDate.getTime());
								sbQuery.append(" AND c.createdDate <= :endDate");
							} else if (searchBy.equalsIgnoreCase("mAction.id")) {
								sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE c.mWorkflowProcessActions.action.id = :p" + i + ")");
							} else if (searchBy.equalsIgnoreCase("mFileType.id")) {
								sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE c.txPostReception.mFileType.id = :p" + i + ")");
							} else if (searchBy.equalsIgnoreCase("createdBy.id")) {
								sbQuery.append(" AND c.createdBy.id = :p" + i + ")");
							} else {
								sbQuery.append(" AND c." + searchBy + " = :p" + j++);
							}
						}
					}
					if (value == null) {
						sbQuery.append(" AND c." + searchBy + " IS NULL");
					}
				}
			}

			if (orderBy != null && orderType != null) {
				sbQuery.append(" ORDER BY c." + orderBy + " " + orderType);
			}

			Query query = entityManager.createQuery(sbQuery.toString());
			if (searchCriteria != null) {
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();
					if (value != null) {
						if (searchBy.contentEquals("startDate")) {
							query.setParameter(searchBy, startDate);
						}else if(searchBy.contentEquals("endDate")) {
							query.setParameter(searchBy, endDate);
						} else if (value instanceof String && !(searchBy.contentEquals("endDate")||searchBy.contentEquals("startDate"))) {
							if (searchCriteria.get(i).isExactMatch()) {
								query.setParameter("p" + i, ((String) value));
							} else {
								query.setParameter("p" + i, "%" + ((String) value) + "%");
							}
						} else if (value instanceof List) {
							for (Object subValue : (List) value) {
								if (subValue instanceof String) {
									query.setParameter("p" + j++, ((String) subValue).toLowerCase());
								} else {
									query.setParameter("p" + j++, subValue);
								}
							}
						} else {
							query.setParameter("p" + j++, value);
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
			System.out.println("QUERY "+sbQuery.toString());
			return query.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	public List<TxMonitor> selectAllLapRekapStatusDanAksiMingguan(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		Timestamp startDate = null;
		Timestamp endDate = null;
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (value == null) {
							sbQuery.append(" AND c." + searchBy + " IS NULL");
						} else if (searchBy.equalsIgnoreCase("mWorkflowProcessActions.mAction.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE mWorkflowProcessActions.action.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("mWorkflowProcess.status.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE mWorkflowProcess.status.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txReception.mFileSequence.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE txTmGeneral.txReception.mFileSequence.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txReception.mFileType.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE txTmGeneral.txReception.mFileType.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txTmBrand.mBrandType.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE txTmGeneral.txTmBrand.mBrandType.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txTmClassList")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c LEFT JOIN c.txTmGeneral tg LEFT JOIN tg.txTmClassList ttc WHERE ttc.mClass.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("createdDateMonth")) {
							sbQuery.append(" AND c.id IN (SELECT tpj FROM TxMonitor tpj WHERE TO_CHAR(tpj.createdDate, 'MM') = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("createdBy.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE createdBy.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("startDate")) {
							sbQuery.append(" AND c.createdDate >= :p" + i);
						} else if (searchBy.equalsIgnoreCase("endDate")) {
							sbQuery.append(" AND c.createdDate <= :p" + i);
						} else if (value instanceof String) {
							if (searchCriteria.get(i).isExactMatch()) {
								sbQuery.append(" AND LOWER(c." + searchBy + ") = :p" + j++);
							} else {
								sbQuery.append(" AND LOWER(c." + searchBy + ") LIKE :p" + j++);
							}
						} else if (value instanceof List) {
							if (((List) value).size() > 0) {
								sbQuery.append(" AND LOWER(c." + searchBy + ") IN (");
								for (int k = 0; k < ((List) value).size(); k++) {
									sbQuery.append(":p" + j++);
									if (k < ((List) value).size() - 1) {
										sbQuery.append(", ");
									}
								}
								sbQuery.append(")");
							}
						} else {
							sbQuery.append(" AND c." + searchBy + " = :p" + j++);
						}
					}
				}
			}

			if (orderBy != null && orderType != null) {
				sbQuery.append(" ORDER BY c." + orderBy + " " + orderType);
			}

			Query query = entityManager.createQuery(sbQuery.toString());
			if (searchCriteria != null) {
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					Object value = searchCriteria.get(i).getValue();
					if (value != null) {
						if (value instanceof String) {
							if (searchCriteria.get(i).isExactMatch()) {
								if(String.valueOf(value).equals("SUPER")) {
									query.setParameter("p" + i, ((String) value));
								} else {
									query.setParameter("p" + i, ((String) value).toLowerCase());
								}
							} else {
								query.setParameter("p" + j++, "%" + ((String) value).toLowerCase() + "%");
							}
						} else if (value instanceof List) {
							for (Object subValue : (List) value) {
								if (subValue instanceof String) {
									query.setParameter("p" + j++, ((String) subValue).toLowerCase());
								} else {
									query.setParameter("p" + j++, subValue);
								}
							}
						} else {
							query.setParameter("p" + j++, value);
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


	public long countLapRekapAksi(List<KeyValue> searchCriteria) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT COUNT(DISTINCT c.mWorkflowProcessActions) FROM " + getClassName() + " c ");
			sbQuery.append("WHERE 1 = 1 ");
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (value == null) {
							sbQuery.append(" AND c." + searchBy + " IS NULL");
						} else if (searchBy.equalsIgnoreCase("mWorkflowProcessActions.mAction.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE mWorkflowProcessActions.action.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("mWorkflowProcess.status.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE mWorkflowProcess.status.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txReception.mFileSequence.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE txTmGeneral.txReception.mFileSequence.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txReception.mFileType.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE txTmGeneral.txReception.mFileType.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txTmBrand.mBrandType.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE txTmGeneral.txTmBrand.mBrandType.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txTmClassList")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c LEFT JOIN c.txTmGeneral tg LEFT JOIN tg.txTmClassList ttc WHERE ttc.mClass.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("createdBy.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE createdBy.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("createdDateMonth")) {
							sbQuery.append(" AND c.id IN (SELECT tpj FROM TxMonitor tpj WHERE TO_CHAR(tpj.createdDate, 'MM') = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("createdDateYear")) {
							sbQuery.append(" AND c.id IN (SELECT tpj FROM TxMonitor tpj WHERE TO_CHAR(tpj.createdDate, 'YYYY') = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("startDate")) {
							sbQuery.append(" AND c.createdDate >= :p" + i);
						} else if (searchBy.equalsIgnoreCase("endDate")) {
							sbQuery.append(" AND c.createdDate <= :p" + i);
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
								if(String.valueOf(value).equals("SUPER") || searchCriteria.get(i).getKey().equalsIgnoreCase("txTmBrand.mBrandType.id")
										|| searchCriteria.get(i).getKey().equalsIgnoreCase("txReception.mFileType.id")) {
									query.setParameter("p" + i, ((String) value));
								} else {
									query.setParameter("p" + i, ((String) value).toLowerCase());
								}
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

	public List<TxMonitor> selectAllLapRekapAksi(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c.mWorkflowProcessActions.id FROM " + getClassName() + " c ");
			sbQuery.append("WHERE 1=1 ");
			sbQuery.append("AND c.mWorkflowProcessActions IS NOT NULL");
			if (searchCriteria != null) {
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (value == null) {
							sbQuery.append(" AND c." + searchBy + " IS NULL");
						} else if (searchBy.equalsIgnoreCase("mWorkflowProcessActions.mAction.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE mWorkflowProcessActions.action.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("mWorkflowProcess.status.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE mWorkflowProcess.status.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txReception.mFileSequence.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE txTmGeneral.txReception.mFileSequence.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txReception.mFileType.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE txTmGeneral.txReception.mFileType.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txTmBrand.mBrandType.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE txTmGeneral.txTmBrand.mBrandType.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txTmClassList")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c LEFT JOIN c.txTmGeneral tg LEFT JOIN tg.txTmClassList ttc WHERE ttc.mClass.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("createdDateMonth")) {
							sbQuery.append(" AND c.id IN (SELECT tpj FROM TxMonitor tpj WHERE TO_CHAR(tpj.createdDate, 'MM') = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("createdDateYear")) {
							sbQuery.append(" AND c.id IN (SELECT tpj FROM TxMonitor tpj WHERE TO_CHAR(tpj.createdDate, 'YYYY') = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("createdBy.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE createdBy.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("startDate")) {
							sbQuery.append(" AND c.createdDate >= :p" + i);
						} else if (searchBy.equalsIgnoreCase("endDate")) {
							sbQuery.append(" AND c.createdDate <= :p" + i);
						} else if (value instanceof String) {
							if (searchCriteria.get(i).isExactMatch()) {
								sbQuery.append(" AND LOWER(c." + searchBy + ") = :p" + j++);
							} else {
								sbQuery.append(" AND LOWER(c." + searchBy + ") LIKE :p" + j++);
							}
						} else if (value instanceof List) {
							if (((List) value).size() > 0) {
								sbQuery.append(" AND LOWER(c." + searchBy + ") IN (");
								for (int k = 0; k < ((List) value).size(); k++) {
									sbQuery.append(":p" + j++);
									if (k < ((List) value).size() - 1) {
										sbQuery.append(", ");
									}
								}
								sbQuery.append(")");
							}
						} else {
							sbQuery.append(" AND c." + searchBy + " = :p" + j++);
						}
					}
				}
				sbQuery.append(" GROUP BY c.mWorkflowProcessActions.id");
			}

			if (orderBy != null && orderType != null) {
				sbQuery.append(" ORDER BY MAX(c." + orderBy + ") " + orderType);
			}

			Query query = entityManager.createQuery(sbQuery.toString());
			if (searchCriteria != null) {
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					Object value = searchCriteria.get(i).getValue();
					if (value != null) {
						if (value instanceof String) {
							if (searchCriteria.get(i).isExactMatch()) {
								if(String.valueOf(value).equals("SUPER")) {
									query.setParameter("p" + i, ((String) value));
								} else {
									query.setParameter("p" + i, ((String) value).toLowerCase());
								}
							} else {
								query.setParameter("p" + j++, "%" + ((String) value).toLowerCase() + "%");
							}
						} else if (value instanceof List) {
							for (Object subValue : (List) value) {
								if (subValue instanceof String) {
									query.setParameter("p" + j++, ((String) subValue).toLowerCase());
								} else {
									query.setParameter("p" + j++, subValue);
								}
							}
						} else {
							query.setParameter("p" + j++, value);
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

	public List<Object[]> selectAllLapRekapAksiBulanan(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c.mWorkflowProcessActions.action.name, count(case when TO_CHAR(c.createdDate, 'DD') = '01' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'DD') = '02' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'DD') = '03' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'DD') = '04' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'DD') = '05' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'DD') = '06' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'DD') = '07' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'DD') = '08' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'DD') = '09' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'DD') = '10' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'DD') = '11' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'DD') = '12' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'DD') = '13' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'DD') = '14' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'DD') = '15' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'DD') = '16' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'DD') = '17' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'DD') = '18' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'DD') = '19' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'DD') = '20' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'DD') = '21' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'DD') = '22' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'DD') = '23' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'DD') = '24' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'DD') = '25' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'DD') = '26' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'DD') = '27' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'DD') = '28' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'DD') = '29' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'DD') = '30' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'DD') = '31' then 1 else null end) FROM " + getClassName() + " c ");
			sbQuery.append("WHERE 1=1 ");
			sbQuery.append("AND c.mWorkflowProcessActions IS NOT NULL");
			if (searchCriteria != null) {
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (value == null) {
							sbQuery.append(" AND c." + searchBy + " IS NULL");
						} else if (searchBy.equalsIgnoreCase("mWorkflowProcessActions.mAction.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE mWorkflowProcessActions.action.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("mWorkflowProcess.status.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE mWorkflowProcess.status.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txReception.mFileSequence.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE txTmGeneral.txReception.mFileSequence.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txReception.mFileType.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE txTmGeneral.txReception.mFileType.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txTmBrand.mBrandType.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE txTmGeneral.txTmBrand.mBrandType.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txTmClassList")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c LEFT JOIN c.txTmGeneral tg LEFT JOIN tg.txTmClassList ttc WHERE ttc.mClass.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("createdDateMonth")) {
							sbQuery.append(" AND c.id IN (SELECT tpj FROM TxMonitor tpj WHERE TO_CHAR(tpj.createdDate, 'MM') = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("createdDateYear")) {
							sbQuery.append(" AND c.id IN (SELECT tpj FROM TxMonitor tpj WHERE TO_CHAR(tpj.createdDate, 'YYYY') = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("createdBy.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE createdBy.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("startDate")) {
							sbQuery.append(" AND c.createdDate >= :p" + i);
						} else if (searchBy.equalsIgnoreCase("endDate")) {
							sbQuery.append(" AND c.createdDate <= :p" + i);
						} else if (value instanceof String) {
							if (searchCriteria.get(i).isExactMatch()) {
								sbQuery.append(" AND LOWER(c." + searchBy + ") = :p" + j++);
							} else {
								sbQuery.append(" AND LOWER(c." + searchBy + ") LIKE :p" + j++);
							}
						} else if (value instanceof List) {
							if (((List) value).size() > 0) {
								sbQuery.append(" AND LOWER(c." + searchBy + ") IN (");
								for (int k = 0; k < ((List) value).size(); k++) {
									sbQuery.append(":p" + j++);
									if (k < ((List) value).size() - 1) {
										sbQuery.append(", ");
									}
								}
								sbQuery.append(")");
							}
						} else {
							sbQuery.append(" AND c." + searchBy + " = :p" + j++);
						}
					}
				}
				sbQuery.append(" GROUP BY c.mWorkflowProcessActions.action.name");
			}

			if (orderBy != null && orderType != null) {
				sbQuery.append(" ORDER BY MAX(c." + orderBy + ") " + orderType);
			}

			Query query = entityManager.createQuery(sbQuery.toString());
			if (searchCriteria != null) {
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					Object value = searchCriteria.get(i).getValue();
					if (value != null) {
						if (value instanceof String) {
							if (searchCriteria.get(i).isExactMatch()) {
								if(String.valueOf(value).equals("SUPER") || searchCriteria.get(i).getKey().equalsIgnoreCase("txTmBrand.mBrandType.id")
										|| searchCriteria.get(i).getKey().equalsIgnoreCase("txReception.mFileType.id")) {
									query.setParameter("p" + i, ((String) value));
								} else {
									query.setParameter("p" + i, ((String) value).toLowerCase());
								}
							} else {
								query.setParameter("p" + j++, "%" + ((String) value).toLowerCase() + "%");
							}
						} else if (value instanceof List) {
							for (Object subValue : (List) value) {
								if (subValue instanceof String) {
									query.setParameter("p" + j++, ((String) subValue).toLowerCase());
								} else {
									query.setParameter("p" + j++, subValue);
								}
							}
						} else {
							query.setParameter("p" + j++, value);
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

	public List<Object[]> selectAllLapRekapAksiTahuan(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c.mWorkflowProcessActions.action.name, count(case when TO_CHAR(c.createdDate, 'MM') = '01' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'MM') = '02' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'MM') = '03' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'MM') = '04' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'MM') = '05' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'MM') = '06' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'MM') = '07' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'MM') = '08' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'MM') = '09' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'MM') = '10' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'MM') = '11' then 1 else null end), " +
					"count(case when TO_CHAR(c.createdDate, 'MM') = '12' then 1 else null end) FROM " + getClassName() + " c ");
			sbQuery.append("WHERE 1=1 ");
			sbQuery.append("AND c.mWorkflowProcessActions IS NOT NULL");
			if (searchCriteria != null) {
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (value == null) {
							sbQuery.append(" AND c." + searchBy + " IS NULL");
						} else if (searchBy.equalsIgnoreCase("mWorkflowProcessActions.mAction.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE mWorkflowProcessActions.action.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("mWorkflowProcess.status.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE mWorkflowProcess.status.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txReception.mFileSequence.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE txTmGeneral.txReception.mFileSequence.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txReception.mFileType.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE txTmGeneral.txReception.mFileType.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txTmBrand.mBrandType.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE txTmGeneral.txTmBrand.mBrandType.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txTmClassList")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c LEFT JOIN c.txTmGeneral tg LEFT JOIN tg.txTmClassList ttc WHERE ttc.mClass.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("createdDateMonth")) {
							sbQuery.append(" AND c.id IN (SELECT tpj FROM TxMonitor tpj WHERE TO_CHAR(tpj.createdDate, 'MM') = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("createdDateYear")) {
							sbQuery.append(" AND c.id IN (SELECT tpj FROM TxMonitor tpj WHERE TO_CHAR(tpj.createdDate, 'YYYY') = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("createdBy.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE createdBy.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("startDate")) {
							sbQuery.append(" AND c.createdDate >= :p" + i);
						} else if (searchBy.equalsIgnoreCase("endDate")) {
							sbQuery.append(" AND c.createdDate <= :p" + i);
						} else if (value instanceof String) {
							if (searchCriteria.get(i).isExactMatch()) {
								sbQuery.append(" AND LOWER(c." + searchBy + ") = :p" + j++);
							} else {
								sbQuery.append(" AND LOWER(c." + searchBy + ") LIKE :p" + j++);
							}
						} else if (value instanceof List) {
							if (((List) value).size() > 0) {
								sbQuery.append(" AND LOWER(c." + searchBy + ") IN (");
								for (int k = 0; k < ((List) value).size(); k++) {
									sbQuery.append(":p" + j++);
									if (k < ((List) value).size() - 1) {
										sbQuery.append(", ");
									}
								}
								sbQuery.append(")");
							}
						} else {
							sbQuery.append(" AND c." + searchBy + " = :p" + j++);
						}
					}
				}
				sbQuery.append(" GROUP BY c.mWorkflowProcessActions.action.name");
			}

			if (orderBy != null && orderType != null) {
				sbQuery.append(" ORDER BY MAX(c." + orderBy + ") " + orderType);
			}

			Query query = entityManager.createQuery(sbQuery.toString());
			if (searchCriteria != null) {
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					Object value = searchCriteria.get(i).getValue();
					if (value != null) {
						if (value instanceof String) {
							if (searchCriteria.get(i).isExactMatch()) {
								if(String.valueOf(value).equals("SUPER") || searchCriteria.get(i).getKey().equalsIgnoreCase("txTmBrand.mBrandType.id")
										|| searchCriteria.get(i).getKey().equalsIgnoreCase("txReception.mFileType.id")) {
									query.setParameter("p" + i, ((String) value));
								} else {
									query.setParameter("p" + i, ((String) value).toLowerCase());
								}
							} else {
								query.setParameter("p" + j++, "%" + ((String) value).toLowerCase() + "%");
							}
						} else if (value instanceof List) {
							for (Object subValue : (List) value) {
								if (subValue instanceof String) {
									query.setParameter("p" + j++, ((String) subValue).toLowerCase());
								} else {
									query.setParameter("p" + j++, subValue);
								}
							}
						} else {
							query.setParameter("p" + j++, value);
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

	public List<Object[]> selectAllLapRekapAksiMingguan(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c.mWorkflowProcessActions.id, c.createdDate, c.mWorkflowProcess.status.name, COUNT(c.mWorkflowProcessActions.id) FROM " + getClassName() + " c ");
			sbQuery.append("WHERE 1=1 ");
			sbQuery.append("AND c.mWorkflowProcessActions IS NOT NULL");
			if (searchCriteria != null) {
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (value == null) {
							sbQuery.append(" AND c." + searchBy + " IS NULL");
						} else if (searchBy.equalsIgnoreCase("mWorkflowProcessActions.mAction.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE mWorkflowProcessActions.action.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("mWorkflowProcess.status.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE mWorkflowProcess.status.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txReception.mFileSequence.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE txTmGeneral.txReception.mFileSequence.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txReception.mFileType.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE txTmGeneral.txReception.mFileType.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txTmBrand.mBrandType.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE txTmGeneral.txTmBrand.mBrandType.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txTmClassList")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c LEFT JOIN c.txTmGeneral tg LEFT JOIN tg.txTmClassList ttc WHERE ttc.mClass.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("createdDateMonth")) {
							sbQuery.append(" AND c.id IN (SELECT tpj FROM TxMonitor tpj WHERE TO_CHAR(tpj.createdDate, 'MM') = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("createdDateYear")) {
							sbQuery.append(" AND c.id IN (SELECT tpj FROM TxMonitor tpj WHERE TO_CHAR(tpj.createdDate, 'YYYY') = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("createdBy.id")) {
							sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE createdBy.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("startDate")) {
							sbQuery.append(" AND c.createdDate >= :p" + i);
						} else if (searchBy.equalsIgnoreCase("endDate")) {
							sbQuery.append(" AND c.createdDate <= :p" + i);
						} else if (value instanceof String) {
							if (searchCriteria.get(i).isExactMatch()) {
								sbQuery.append(" AND LOWER(c." + searchBy + ") = :p" + j++);
							} else {
								sbQuery.append(" AND LOWER(c." + searchBy + ") LIKE :p" + j++);
							}
						} else if (value instanceof List) {
							if (((List) value).size() > 0) {
								sbQuery.append(" AND LOWER(c." + searchBy + ") IN (");
								for (int k = 0; k < ((List) value).size(); k++) {
									sbQuery.append(":p" + j++);
									if (k < ((List) value).size() - 1) {
										sbQuery.append(", ");
									}
								}
								sbQuery.append(")");
							}
						} else {
							sbQuery.append(" AND c." + searchBy + " = :p" + j++);
						}
					}
				}
				sbQuery.append(" GROUP BY c.mWorkflowProcessActions.id, c.createdDate, c.mWorkflowProcess.status.name");
			}

			if (orderBy != null && orderType != null) {
				sbQuery.append(" ORDER BY MAX(c." + orderBy + ") " + orderType);
			}

			Query query = entityManager.createQuery(sbQuery.toString());
			if (searchCriteria != null) {
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					Object value = searchCriteria.get(i).getValue();
					if (value != null) {
						if (value instanceof String) {
							if (searchCriteria.get(i).isExactMatch()) {
								if(String.valueOf(value).equals("SUPER") || searchCriteria.get(i).getKey().equalsIgnoreCase("txTmBrand.mBrandType.id")
										|| searchCriteria.get(i).getKey().equalsIgnoreCase("txReception.mFileType.id")) {
									query.setParameter("p" + i, ((String) value));
								} else {
									query.setParameter("p" + i, ((String) value).toLowerCase());
								}
							} else {
								query.setParameter("p" + j++, "%" + ((String) value).toLowerCase() + "%");
							}
						} else if (value instanceof List) {
							for (Object subValue : (List) value) {
								if (subValue instanceof String) {
									query.setParameter("p" + j++, ((String) subValue).toLowerCase());
								} else {
									query.setParameter("p" + j++, subValue);
								}
							}
						} else {
							query.setParameter("p" + j++, value);
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

	public void saveTxMonitorByBatch(List<TxMonitor> txMonitorList) {
		Timestamp nx = new Timestamp(System.currentTimeMillis());
		MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();



		for (TxMonitor txMonitor : txMonitorList) {
			String nativeQuery = "INSERT INTO TX_MONITOR " +
					"(MONITOR_ID, SKIP_AUDIT_TRAIL, CREATED_DATE, DUE_DATE, FILE_UPLOAD_PATH, NOTES, CREATED_BY, WORKFLOW_PROCESS_ID, WORKFLOW_PROCESS_ACTION_ID, TARGET_WORKFLOW_PROCESS_ID, "
					+ "APPLICATION_ID, CC_ROLE ) " +
					"VALUES " +
					"(:p1, :p2, :p3, :p4, :p5, :p6, :p7, :p8, :p9, :p10, :p11, :p12 )";




			Query query = entityManager.createNativeQuery(nativeQuery)
					.setParameter("p1", txMonitor.getId())
					.setParameter("p2", txMonitor.isSkipLogAuditTrail() == null ? "1" : txMonitor.isSkipLogAuditTrail() )
					.setParameter("p3", txMonitor.getCreatedDate()== null ? nx.toString() : txMonitor.getCreatedDate() )
					.setParameter("p4", txMonitor.getDueDate() == null ? "2200-12-12" : txMonitor.getDueDate())
					.setParameter("p5", txMonitor.getFileUploadPath()== null ? "" : txMonitor.getFileUploadPath())
					.setParameter("p6", txMonitor.getNotes()== null ? "" : txMonitor.getNotes() )
					.setParameter("p7", txMonitor.getCreatedBy() == null ? mUser.getId() : txMonitor.getCreatedBy().getId())
					.setParameter("p8", txMonitor.getmWorkflowProcess() == null ? null : txMonitor.getmWorkflowProcess().getId())
					.setParameter("p9", txMonitor.getmWorkflowProcessActions() == null ? null : txMonitor.getmWorkflowProcessActions().getId())
					.setParameter("p10", txMonitor.getTargetWorkflowProcess() == null ? null : txMonitor.getTargetWorkflowProcess().getId())
					.setParameter("p11", txMonitor.getTxTmGeneral() == null ? null : txMonitor.getTxTmGeneral().getId())
					.setParameter("p12", txMonitor.getCc_role() == null ? null : txMonitor.getCc_role().getId())
					;
			query.executeUpdate();
		}
	}

	public String[] findAllTxMonitorByTxTmGeneralIdAndWfProcessId( String[] appId, MWorkflowProcess wfProcessId)
	{
		int oracleLimit = 1000;
		String SQLcmd = "SELECT txm.MONITOR_ID FROM TX_MONITOR txm LEFT JOIN TX_TM_GENERAL tg ON tg.APPLICATION_ID = txm.APPLICATION_ID WHERE txm.WORKFLOW_PROCESS_ID = :wfProcessId ";
		String newWhereClause = "";
		for (int i = 0; i <= appId.length/oracleLimit; i++) {
			if (oracleLimit*i == appId.length){
				break;
			}
			String tempClause = (i==0?"":" OR ")+"txm.APPLICATION_ID IN (";
			for (int j = 0 ; j < (appId.length>(oracleLimit*(i+1))?oracleLimit:appId.length); j++) {
				if (j+(oracleLimit*i) >= appId.length){
					break;
				}
				tempClause += (j==0?"":",")+ "'" +appId[j+(oracleLimit*i)] + "'";
			}
			newWhereClause += tempClause+")";
		}
		if(newWhereClause.contains("OR")){
			newWhereClause = "(" + newWhereClause + ")";
		}
		if(newWhereClause!=""){
			SQLcmd += " AND ";
		}
		Query query = entityManager.createNativeQuery(SQLcmd+newWhereClause).setParameter("wfProcessId", wfProcessId);
		List<String> resultList = query.getResultList();
		return resultList.toArray(new String[resultList.size()]);
	}

	public List<TxMonitor> findAllTxMonitorByTxTmGeneralId( String[] appId )
	{
		int oracleLimit = 1000;
		String SQLcmd = "SELECT * FROM TX_MONITOR txm LEFT JOIN TX_TM_GENERAL tg ON tg.APPLICATION_ID = txm.APPLICATION_ID  WHERE ";
		String newWhereClause = "";
		for (int i = 0; i <= appId.length/oracleLimit; i++) {
			if (oracleLimit*i == appId.length){
				break;
			}
			String tempClause = (i==0?"":" OR ")+" txm.APPLICATION_ID IN (";
			for (int j = 0 ; j < (appId.length>(oracleLimit*(i+1))?oracleLimit:appId.length); j++) {
				if (j+(oracleLimit*i) >= appId.length){
					break;
				}
				tempClause += (j==0?"":",")+ "'" +appId[j+(oracleLimit*i)] + "'";
			}
			newWhereClause += tempClause+")";
		}
		if(newWhereClause.contains("OR")){
			newWhereClause = "(" + newWhereClause + ")";
		}
		Query query = entityManager.createNativeQuery(SQLcmd+newWhereClause);
		return query.getResultList();
	}

	public List<TxMonitor> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		Timestamp startDate = null;
		Timestamp endDate = null;
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");

			sbQuery.append("LEFT JOIN c.txTmGeneral ttg ");
			sbQuery.append("LEFT JOIN c.txPostReception tpr ");

			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (value == null) {
							sbQuery.append(" AND c." + searchBy + " IS NULL");
						} else if (searchBy.equalsIgnoreCase("startDate")) {
							sbQuery.append(" AND c.createdDate >= :p" + i);
						} else if (searchBy.equalsIgnoreCase("endDate")) {
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
							Date endStringDate = sdf.parse(value.toString().substring(0, 10) + " 23:59:59");
							endDate = new Timestamp(endStringDate.getTime());
							sbQuery.append(" AND c.createdDate <= :endDate");
						} else if (searchBy.equalsIgnoreCase("createdBy")) {
							sbQuery.append(" AND (ttg.createdBy = :p" + i + " OR tpr.createdBy = :p" + i + ")" );
						}
						else if (value instanceof String) {
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

			if (orderBy != null && orderType != null) {
				sbQuery.append(" ORDER BY c." + orderBy + " " + orderType);
			}

			//System.out.println("dump query " + sbQuery.toString());
			Query query = entityManager.createQuery(sbQuery.toString());
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();
					if (value != null && !searchBy.equalsIgnoreCase("endDate")) {
						if (value instanceof String) {
							if (searchCriteria.get(i).isExactMatch()) {
								if(String.valueOf(value).equals("SUPER")) {
									query.setParameter("p" + i, ((String) value));
								} else {
									query.setParameter("p" + i, ((String) value).toLowerCase());
								}
							} else {
								query.setParameter("p" + i, "%" + ((String) value).toLowerCase() + "%");
							}
						} else {
							query.setParameter("p" + i, value);
						}
					}
					//System.out.println("dump param p" + i + "-" + value);
				}

				if (endDate != null) {
					query.setParameter("endDate", endDate);
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

	public Long countMonitor(List<KeyValue> searchCriteria) {
		Timestamp startDate = null;
		Timestamp endDate = null;
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT count(c) FROM " + getClassName() + " c ");

			sbQuery.append("LEFT JOIN c.txTmGeneral ttg ");
			sbQuery.append("LEFT JOIN c.txPostReception tpr ");

			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (value == null) {
							sbQuery.append(" AND c." + searchBy + " IS NULL");
						} else if (searchBy.equalsIgnoreCase("startDate")) {
							sbQuery.append(" AND c.createdDate >= :p" + i);
						} else if (searchBy.equalsIgnoreCase("endDate")) {
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
							Date endStringDate = sdf.parse(value.toString().substring(0, 10) + " 23:59:59");
							endDate = new Timestamp(endStringDate.getTime());
							sbQuery.append(" AND c.createdDate <= :endDate");
						} else if (searchBy.equalsIgnoreCase("createdBy")) {
							sbQuery.append(" AND (ttg.createdBy = :p" + i + " OR tpr.createdBy = :p" + i + ")" );
						}
						else if (value instanceof String) {
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
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();
					if (value != null && !searchBy.equalsIgnoreCase("endDate")) {
						if (value instanceof String) {
							if (searchCriteria.get(i).isExactMatch()) {
								if(String.valueOf(value).equals("SUPER")) {
									query.setParameter("p" + i, ((String) value));
								} else {
									query.setParameter("p" + i, ((String) value).toLowerCase());
								}
							} else {
								query.setParameter("p" + i, "%" + ((String) value).toLowerCase() + "%");
							}
						} else {
							query.setParameter("p" + i, value);
						}
					}
				}

				if (endDate != null) {
					query.setParameter("endDate", endDate);
				}
			}
			return (Long) query.setMaxResults(1).getSingleResult();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}
	
	public long countKeputusanDirektur(List<KeyValue> searchCriteria )
	{
		Timestamp startDate = null;
		Timestamp endDate = null;
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT COUNT(c) FROM " + getClassName() + " c ");
			sbQuery.append("WHERE 1 = 1 ");
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();
					//System.out.println(">>> Class: "+getClassName()+" searchCriteria ==> searchBy:" + searchBy + " , value:" + value.toString());
					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (value != null) {
							if (searchBy.equalsIgnoreCase("startDate")) {
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								Date startStringDate = sdf.parse(value.toString().substring(0,10)+" 00:00:01");
								startDate = new Timestamp(startStringDate.getTime());
								sbQuery.append(" AND c.createdDate >= :startDate");
							} else if (searchBy.equalsIgnoreCase("endDate")) {
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								Date endStringDate = sdf.parse(value.toString().substring(0, 10) + " 23:59:58");
								endDate = new Timestamp(endStringDate.getTime());
								sbQuery.append(" AND c.createdDate <= :endDate");
							} else if (searchBy.equalsIgnoreCase("mAction.id")) {
								sbQuery.append(" AND lower(c.mWorkflowProcessActions.action.id) = :p" + i);
							} else if (searchBy.equalsIgnoreCase("mStatus.id")) {
								sbQuery.append(" AND lower(c.mWorkflowProcess.status.id) = :p" + i);
							} else if (searchBy.equalsIgnoreCase("createdBy.id")) {
								sbQuery.append(" AND lower(c.txTmGeneral.ownerList) = :p" + i);
							}
						}
					}
					if (value == null) {
						sbQuery.append(" AND c." + searchBy + " IS NULL");
					}
				}

			}

			Query query = entityManager.createQuery(sbQuery.toString());
			if (searchCriteria != null ) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();
					if (value != null) {
						if (searchBy.contentEquals("startDate")) {
							query.setParameter(searchBy, startDate);
						} else if(searchBy.contentEquals("endDate")) {
							query.setParameter(searchBy, endDate);
						} else if (value instanceof String && !(searchBy.contentEquals("endDate")||searchBy.contentEquals("startDate"))) {
							if (searchCriteria.get(i).isExactMatch()) {								
								query.setParameter("p" + i, ((String) value).toLowerCase());
								//System.out.println(" P"+i+" VALUE === " +((String) value).toLowerCase());
							} else {
								query.setParameter("p" + i, "%" + ((String) value).toLowerCase() + "%");
							}
						} else {
							query.setParameter("p" + i, value);
						}
					}

				}
			}
			//System.out.println("QUERY "+sbQuery.toString());
			return (Long) query.setMaxResults(1).getSingleResult();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return 0L;
	
	}	
	public long countKasubditPemeriksa(List<KeyValue> searchCriteria )
	{ 
		Timestamp startDate = null;
		Timestamp endDate = null;
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT COUNT(c) FROM " + getClassName() + " c ");
			sbQuery.append("WHERE 1 = 1 ");
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();
					//System.out.println(">>> Class: "+getClassName()+" searchCriteria ==> searchBy:" + searchBy + " , value:" + value.toString());
					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (value != null) {
							if (searchBy.equalsIgnoreCase("startDate")) {
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								Date startStringDate = sdf.parse(value.toString().substring(0,10)+" 00:00:01");
								startDate = new Timestamp(startStringDate.getTime());
								sbQuery.append(" AND c.createdDate >= :startDate");
							} else if (searchBy.equalsIgnoreCase("endDate")) {
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								Date endStringDate = sdf.parse(value.toString().substring(0, 10) + " 23:59:58");
								endDate = new Timestamp(endStringDate.getTime());
								sbQuery.append(" AND c.createdDate <= :endDate");
							} else if (searchBy.equalsIgnoreCase("mAction.id")) {
								sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE c.txTmGeneral.mAction.id = :p" + i + ")");
							} else if (searchBy.equalsIgnoreCase("mStatus.id")) {
								sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE c.txTmGeneral.mStatus.id = :p" + i + ")");
							} else if (searchBy.equalsIgnoreCase("createdBy.id")) {
								sbQuery.append(" AND c.createdBy.id = :p" + i + ")");
							}
						}
					}
					if (value == null) {
						sbQuery.append(" AND c." + searchBy + " IS NULL");
					}
				}

			}

			Query query = entityManager.createQuery(sbQuery.toString());
			if (searchCriteria != null ) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();
					if (value != null) {
						if (searchBy.contentEquals("startDate")) {
							query.setParameter(searchBy, startDate);
						} else if(searchBy.contentEquals("endDate")) {
							query.setParameter(searchBy, endDate);
						} else if (value instanceof String && !(searchBy.contentEquals("endDate")||searchBy.contentEquals("startDate"))) {
							if (searchCriteria.get(i).isExactMatch()) {
								query.setParameter("p" + i, ((String) value).toLowerCase());
								//System.out.println(" P"+i+" VALUE === " +((String) value).toLowerCase());
							} else {
								query.setParameter("p" + i, "%" + ((String) value).toLowerCase() + "%");
							}
						} else {
							query.setParameter("p" + i, value);
						}
					}

				}
			}
			//System.out.println("QUERY "+sbQuery.toString());
			return (Long) query.setMaxResults(1).getSingleResult();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return 0L;
	}


	
	
	public List<TxMonitor>  selectAllKeputusanDirektur(List<KeyValue> searchCriteria,String orderBy,String orderType, Integer offset, Integer limit) {
		Timestamp startDate = null;
		Timestamp endDate = null;
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();
					//System.out.println(">>> Class: "+getClassName()+" searchCriteria ==> searchBy:" + searchBy + " , value:" + value.toString());
					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (value != null) {
							if (searchBy.equalsIgnoreCase("startDate")) {
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								Date startStringDate = sdf.parse(value.toString().substring(0,10)+" 00:00:01");
								startDate = new Timestamp(startStringDate.getTime());
								sbQuery.append(" AND c.createdDate >= :startDate");
							} else if (searchBy.equalsIgnoreCase("endDate")) {
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								Date endStringDate = sdf.parse(value.toString().substring(0, 10) + " 23:59:58");
								endDate = new Timestamp(endStringDate.getTime());
								sbQuery.append(" AND c.createdDate <= :endDate");
							} else if (searchBy.equalsIgnoreCase("mAction.id")) {
								sbQuery.append(" AND lower(c.mWorkflowProcessActions.action.id) = :p" + i);
							} else if (searchBy.equalsIgnoreCase("mStatus.id")) {
								sbQuery.append(" AND lower(c.mWorkflowProcess.status.id) = :p" + i);
							} else if (searchBy.equalsIgnoreCase("createdBy.id")) {
								sbQuery.append(" AND lower(c.txTmGeneral.ownerList) = :p" + i);
							} else {
								sbQuery.append(" AND c." + searchBy + " = :p" + j++);
							}
						}
					}
					if (value == null) {
						sbQuery.append(" AND c." + searchBy + " IS NULL");
					}
				}
			}

			if (orderBy != null && orderType != null) {
				sbQuery.append(" ORDER BY c." + orderBy + " " + orderType);
			}

			Query query = entityManager.createQuery(sbQuery.toString());
			if (searchCriteria != null) {
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();
					if (value != null) {
						if (searchBy.contentEquals("startDate")) {
							query.setParameter(searchBy, startDate);
						}else if(searchBy.contentEquals("endDate")) {
							query.setParameter(searchBy, endDate);
						} else if (value instanceof String && !(searchBy.contentEquals("endDate")||searchBy.contentEquals("startDate"))) {
							if (searchCriteria.get(i).isExactMatch()) {
								query.setParameter("p" + i, ((String) value).toLowerCase());
							} else {
								query.setParameter("p" + i, "%" + ((String) value).toLowerCase() + "%");
							}
						} else if (value instanceof List) {
							for (Object subValue : (List) value) {
								if (subValue instanceof String) {
									query.setParameter("p" + j++, ((String) subValue).toLowerCase());
								} else {
									query.setParameter("p" + j++, subValue);
								}
							}
						} else {
							query.setParameter("p" + j++, value);
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
			//System.out.println("QUERY "+sbQuery.toString());
			return query.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	
	}
	
	public List<TxMonitor>  selectAllKasubditPemeriksa(List<KeyValue> searchCriteria,String orderBy,String orderType, Integer offset, Integer limit) {

		Timestamp startDate = null;
		Timestamp endDate = null;
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();
					//System.out.println(">>> Class: "+getClassName()+" searchCriteria ==> searchBy:" + searchBy + " , value:" + value.toString());
					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (value != null) {
							if (searchBy.equalsIgnoreCase("startDate")) {
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								Date startStringDate = sdf.parse(value.toString().substring(0,10)+" 00:00:01");
								startDate = new Timestamp(startStringDate.getTime());
								sbQuery.append(" AND c.createdDate >= :startDate");
							} else if (searchBy.equalsIgnoreCase("endDate")) {
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								Date endStringDate = sdf.parse(value.toString().substring(0, 10) + " 23:59:58");
								endDate = new Timestamp(endStringDate.getTime());
								sbQuery.append(" AND c.createdDate <= :endDate");
							} else if (searchBy.equalsIgnoreCase("mAction.id")) {
								sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE txTmGeneral.mAction.id = :p" + i + ")");
							} else if (searchBy.equalsIgnoreCase("mStatus.id")) {
								sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE txTmGeneral.mStatus.id = :p" + i + ")");
							} else if (searchBy.equalsIgnoreCase("createdBy.id")) {
								sbQuery.append(" AND c.createdBy.id = :p" + i + ")");
							} else {
								sbQuery.append(" AND c." + searchBy + " = :p" + j++);
							}
						}
					}
					if (value == null) {
						sbQuery.append(" AND c." + searchBy + " IS NULL");
					}
				}
			}

			if (orderBy != null && orderType != null) {
				sbQuery.append(" ORDER BY c." + orderBy + " " + orderType);
			}

			Query query = entityManager.createQuery(sbQuery.toString());
			if (searchCriteria != null) {
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();
					if (value != null) {
						if (searchBy.contentEquals("startDate")) {
							query.setParameter(searchBy, startDate);
						}else if(searchBy.contentEquals("endDate")) {
							query.setParameter(searchBy, endDate);
						} else if (value instanceof String && !(searchBy.contentEquals("endDate")||searchBy.contentEquals("startDate"))) {
							if (searchCriteria.get(i).isExactMatch()) {
								query.setParameter("p" + i, ((String) value).toLowerCase());
							} else {
								query.setParameter("p" + i, "%" + ((String) value).toLowerCase() + "%");
							}
						} else if (value instanceof List) {
							for (Object subValue : (List) value) {
								if (subValue instanceof String) {
									query.setParameter("p" + j++, ((String) subValue).toLowerCase());
								} else {
									query.setParameter("p" + j++, subValue);
								}
							}
						} else {
							query.setParameter("p" + j++, value);
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
			//System.out.println("QUERY "+sbQuery.toString());
			return query.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	} 
	
	public long countProsesPermohonanMonitor(List<KeyValue> searchCriteria) { 
		Timestamp startDate = null;
		Timestamp endDate = null;

		Timestamp tglProsesStartDate = null;
		Timestamp tglProsesEndDate = null;
		
		Timestamp priorDate=null;  
		StringBuffer sbQuery=null;
		String searchAppNo = "";

		try { 

		MUser createdBy = null;
			for(KeyValue sc : searchCriteria){
				String key = sc.getKey();
				if (key.equalsIgnoreCase("createdBy") && sc.getValue()!=null && !sc.getValue().toString().equalsIgnoreCase("")){
					createdBy = userService.getUserByUsername(sc.getValue().toString()); 
				}
			}
			
			if(searchCriteria.isEmpty()) {
 
				sbQuery = new StringBuffer("SELECT COUNT(c) FROM " + "TxTmGeneral" + " c ");
				
			} else 
				if (searchCriteria != null) { 
 
					/*					
					 SELECT * FROM TX_MONITOR tm 
					 JOIN TX_TM_GENERAL tg ON tm.APPLICATION_ID = tg.APPLICATION_ID 
					 JOIN M_WORKFLOW_PROCESS mwp ON tm.WORKFLOW_PROCESS_ID =mwp.WORKFLOW_PROCESS_ID 
					 JOIN M_STATUS ms ON ms.STATUS_ID =mwp.STATUS_ID 
					  WHERE (tg.APPLICATION_NO , tm.CREATED_DATE , tg.STATUS_ID ) in
					 (SELECT g.APPLICATION_NO , Max(m.CREATED_DATE), g.STATUS_ID 
					 FROM TX_MONITOR m
					 JOIN TX_TM_GENERAL g ON m.APPLICATION_ID = g.APPLICATION_ID 
					  JOIN M_WORKFLOW_PROCESS w ON m.WORKFLOW_PROCESS_ID =w.WORKFLOW_PROCESS_ID 
					 JOIN M_STATUS s ON s.STATUS_ID =w.STATUS_ID  
					 WHERE  g.STATUS_ID =s.STATUS_ID 
					 GROUP BY g.APPLICATION_NO, g.STATUS_ID  
					 ) 
					  */

				for (int x = 0; x < searchCriteria.size(); x++) {
					String k = searchCriteria.get(x).getKey();
					Object v = searchCriteria.get(x).getValue();

					if(k.equalsIgnoreCase("applicationNo"))
						searchAppNo = (String)v;
				}

				sbQuery = new StringBuffer("SELECT COUNT(c) FROM " + getClassName() + " c " 
							+ "WHERE (c.txTmGeneral, c.createdDate, c.txTmGeneral.mStatus.id ) in " 
							+ "(SELECT m.txTmGeneral , MAX(m.createdDate),m.txTmGeneral.mStatus.id from " + getClassName() +" m "
							+ " WHERE m.txTmGeneral.mStatus.id=m.mWorkflowProcess.status.id "
							+ (!searchAppNo.isEmpty() ? " AND m.txTmGeneral.applicationNo = :applicationNo" : "")
							+ " GROUP BY m.txTmGeneral , m.txTmGeneral.mStatus.id ) ");

				for (int i = 0; i < searchCriteria.size(); i++) { 
					
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue(); 
					
//					System.out.println(">>> Class: "+getClassName()+" searchCriteria ==> searchBy:" + searchBy + " , value:" + value.toString());
					
					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (value != null) { 
							 if (searchBy.equalsIgnoreCase("createdBy") && createdBy!=null){
								 sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE c.txTmGeneral.createdBy = :createdBy)");
							 } 
							 else if (searchBy.equalsIgnoreCase("mStatus.id")) {
								 sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE c.txTmGeneral.mStatus.id = :p" + i + ")");
							 } 
							 else if (searchBy.equalsIgnoreCase("mAction.id")) {
								sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE c.txTmGeneral.mAction.id = :p" + i + ")");
							 } 
							 else if (searchBy.equalsIgnoreCase("txTmClassList")) {
							     sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c LEFT JOIN c.txTmGeneral tg LEFT JOIN tg.txTmClassList ttc WHERE ttc.mClass.id = :p" + i + ")");
							 }
							 else if (searchBy.equalsIgnoreCase("priorDate")) {
								 
								 if (value != null) {
									 try { 
										 
										 Calendar calendar = Calendar.getInstance();  
										 
										 if (value instanceof Timestamp) {
											 calendar.setTimeInMillis(((Timestamp) value).getTime()); 
										 } else { 
											 SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy"); 
											 calendar.setTime(sdf.parse(value.toString()));
										 }
										  
										 calendar.set(Calendar.HOUR, 0);
										 calendar.set(Calendar.MINUTE, 0);
										 calendar.set(Calendar.SECOND, 0);  
										 
										 priorDate = new Timestamp(calendar.getTimeInMillis());    
										 sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c LEFT JOIN c.txTmGeneral tg LEFT JOIN tg.txTmPriorList ttp WHERE ttp.priorDate = :priorDate)"); 
									
										 
									 }catch (ParseException e) { }
								 } 			 
							 }  
							 else if (searchBy.equalsIgnoreCase("txReception.mFileType.id")) {
								 sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE txTmGeneral.txReception.mFileType.id = :p" + i + ")");
							 }
							 else if (searchBy.equalsIgnoreCase("mFileTypeDetail.id")) {
								  sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE txTmGeneral.txReception.mFileTypeDetail.id = :p" + i + ")");
							 } 
							 
							 else if(searchBy.equalsIgnoreCase("startDate") || searchBy.equalsIgnoreCase("endDate")){
								 
								 if (value != null) {
									 try { 
										 Calendar calendar = Calendar.getInstance();
										 if (value instanceof Timestamp) {
											 calendar.setTimeInMillis(((Timestamp) value).getTime()); 
										 } else { 
											 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.ms");
											 calendar.setTime(sdf.parse(value.toString())); 
										 }
										 if (searchBy.equalsIgnoreCase("startDate")) { 
											 calendar.set(Calendar.HOUR, 0);
											 calendar.set(Calendar.MINUTE, 0);
											 calendar.set(Calendar.SECOND, 0); 
											 
											 startDate = new Timestamp(calendar.getTimeInMillis()); 
											 sbQuery.append(" AND c.txTmGeneral.filingDate >= :startDate");
											 
										 } else if (searchBy.equalsIgnoreCase("endDate")) {
											 calendar.set(Calendar.HOUR, 23);
											 calendar.set(Calendar.MINUTE, 59);
											 calendar.set(Calendar.SECOND, 59);

											 endDate = new Timestamp(calendar.getTimeInMillis()); 
											 sbQuery.append(" AND c.txTmGeneral.filingDate <= :endDate");
										 }
									 } 
									 catch (ParseException e) { }
								 } 
							}   
							else if(searchBy.equalsIgnoreCase("tglProsesStartDate") || searchBy.equalsIgnoreCase("tglProsesEndDate")){
								  
								 if (value != null) {
									 try { 
										 Calendar calendar = Calendar.getInstance();
										 if (value instanceof Timestamp) {  
											 calendar.setTimeInMillis(((Timestamp) value).getTime()); 
										 } else {  
											 SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");  
											 calendar.setTime(sdf.parse(value.toString()));  
										 }
										 if (searchBy.equalsIgnoreCase("tglProsesStartDate")) { 
											 calendar.set(Calendar.HOUR, 0);
											 calendar.set(Calendar.MINUTE, 0);
											 calendar.set(Calendar.SECOND, 0); 
											 
											 tglProsesStartDate = new Timestamp(calendar.getTimeInMillis()); 
											 sbQuery.append(" AND c.createdDate >= :tglProsesStartDate"); 
											 
										 } else if (searchBy.equalsIgnoreCase("tglProsesEndDate")) { 
											 calendar.set(Calendar.HOUR, 23);
											 calendar.set(Calendar.MINUTE, 59);
											 calendar.set(Calendar.SECOND, 59);

											 tglProsesEndDate = new Timestamp(calendar.getTimeInMillis()); 
											 sbQuery.append(" AND c.createdDate <= :tglProsesEndDate"); 
										 }
									 } 
									 catch (ParseException e) { }
								 } 
							}  
							else if (value instanceof String) {
								if (searchCriteria.get(i).isExactMatch()) {  
									sbQuery.append(" AND LOWER(c.txTmGeneral." + searchBy + ") = :p" + i);
								} else {   
									sbQuery.append(" AND LOWER(c.txTmGeneral." + searchBy + ") LIKE :p" + i); 
								}
							} else { 
								sbQuery.append(" AND c.txTmGeneral." + searchBy + " = :p" + i);
							}
						}
					}
					if (value == null) {
						sbQuery.append(" AND c.txTmGeneral." + searchBy + " IS NULL");
					}
				}

			}

			Query query = entityManager.createQuery(sbQuery.toString());
		 
			if (searchCriteria != null ) {

				if (!searchAppNo.isEmpty()) {
					query.setParameter("applicationNo",  searchAppNo);
				}

				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();
					if (value != null) {
						if( searchBy.contentEquals("createdBy") && createdBy!=null) {
							query.setParameter(searchBy, createdBy);
							System.err.println("?"+createdBy);
						}
						else if (searchBy.contentEquals("startDate")) { 
							query.setParameter(searchBy, startDate);
							
						} else if(searchBy.contentEquals("endDate")) {  
							query.setParameter(searchBy, endDate);
						}
						else if (searchBy.contentEquals("tglProsesStartDate")) { 
							query.setParameter(searchBy, tglProsesStartDate);

						} else if(searchBy.contentEquals("tglProsesEndDate")) { 
							query.setParameter(searchBy, tglProsesEndDate);	
							
						} else if(searchBy.contentEquals("priorDate")) { 
							query.setParameter(searchBy, priorDate);							
							
							 
						} else if (value instanceof String 
								&& !(searchBy.contentEquals("endDate") ||searchBy.contentEquals("startDate"))
								&& !(searchBy.contentEquals("tglProsesEndDate") || searchBy.contentEquals("tglProsesStartDate"))
								&& !(searchBy.contentEquals("priorDate"))) { 
							
							if (searchCriteria.get(i).isExactMatch()) {
								if(searchBy.equalsIgnoreCase("mFileType.id") || searchBy.equalsIgnoreCase("mFileTypeDetail.id")) {
									query.setParameter("p" + i, ((String) value));
								} else {
									query.setParameter("p" + i, ((String) value).toLowerCase());
								}
							}else {
								
								if(searchBy.equalsIgnoreCase("txTmBrand.name")
										|| searchBy.equalsIgnoreCase("applicationNo")
										|| searchBy.equalsIgnoreCase("txRegistration.no") ) {
									query.setParameter("p" + i, "%" + ((String) value).toLowerCase() + "%");
									if(((String) value).contains("%")){
										query.setParameter("p" + i, ((String) value).toLowerCase());
									}else {
										query.setParameter("p" + i, "%" + ((String) value).toLowerCase() + "%");
									}
								} 
								else { 
									query.setParameter("p" + i, value); 
								} 
							} 
						} else { 
							query.setParameter("p" + i, value);
						}
					}

				}
			}
			System.out.println("QUERY "+sbQuery.toString());
			return (Long) query.setMaxResults(1).getSingleResult();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return 0L;
	}
	
	
	public List<TxMonitor>  selectAllProsesPermohonanMonitor(List<KeyValue>
	searchCriteria,String orderBy,String orderType, Integer offset, Integer limit) { 
		Timestamp startDate = null;
		Timestamp endDate = null;

		Timestamp tglProsesStartDate = null;
		Timestamp tglProsesEndDate = null; 

		Timestamp priorDate=null; 
		StringBuffer sbQuery=null;
		String searchAppNo = "";
		
		try { 
			MUser createdBy = null;
			for(KeyValue sc : searchCriteria){
				String key = sc.getKey();
				if (key.equalsIgnoreCase("createdBy") && sc.getValue()!=null && !sc.getValue().toString().equalsIgnoreCase("")){
					createdBy = userService.getUserByUsername(sc.getValue().toString());
				}
			} 
			
			//StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");  
 			
			
			if(!searchCriteria.isEmpty()) {

				for (int x = 0; x < searchCriteria.size(); x++) {
					String k = searchCriteria.get(x).getKey();
					Object v = searchCriteria.get(x).getValue();

					if(k.equalsIgnoreCase("applicationNo"))
						searchAppNo = (String)v;
				}

				sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c " 
							+ "WHERE (c.txTmGeneral, c.createdDate, c.txTmGeneral.mStatus.id ) in " 
							+ "(SELECT m.txTmGeneral , MAX(m.createdDate),m.txTmGeneral.mStatus.id from " + getClassName() +" m "
							+ " WHERE m.txTmGeneral.mStatus.id=m.mWorkflowProcess.status.id "
							+ (!searchAppNo.isEmpty() ? " AND m.txTmGeneral.applicationNo = :applicationNo" : "")
							+ " GROUP BY m.txTmGeneral , m.txTmGeneral.mStatus.id ) "); 
				
 
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();
					
//					System.out.println(">>> Class: "+getClassName()+" searchCriteria ==> searchBy:" + searchBy + " , value:" + value.toString());
					
					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (value != null) {
							 if (searchBy.equalsIgnoreCase("createdBy") && createdBy!=null){
								 sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE c.txTmGeneral.createdBy = :createdBy)");
							 }  
							 else if (searchBy.equalsIgnoreCase("mStatus.id")) {
								 sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE c.txTmGeneral.mStatus.id = :p" + i + ")");
								 
							 } 
							 else if (searchBy.equalsIgnoreCase("mAction.id")) {
								 sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE c.txTmGeneral.mAction.id = :p" + i + ")");
							 }
							 else if (searchBy.equalsIgnoreCase("txTmClassList")) { 
							 	 sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c LEFT JOIN c.txTmGeneral tg LEFT JOIN tg.txTmClassList ttc WHERE ttc.mClass.id = :p" + i + ")");								
							 }
							 else if (searchBy.equalsIgnoreCase("txTmPriorList.priorDate")) {
								 sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE c.txTmGeneral.txTmPriorList.priorDate= :p" + i + ")");
							 }  
							 else if (searchBy.equalsIgnoreCase("priorDate")) {
								 
								 if (value != null) {
									 try { 
										 
										 Calendar calendar = Calendar.getInstance();  
										 
										 if (value instanceof Timestamp) {
											 calendar.setTimeInMillis(((Timestamp) value).getTime()); 
										 } else { 
											 SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy"); 
											 calendar.setTime(sdf.parse(value.toString()));
										 }
										  
										 calendar.set(Calendar.HOUR, 0);
										 calendar.set(Calendar.MINUTE, 0);
										 calendar.set(Calendar.SECOND, 0);  
										 
										 priorDate = new Timestamp(calendar.getTimeInMillis());    
										 sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c LEFT JOIN c.txTmGeneral tg LEFT JOIN tg.txTmPriorList ttp WHERE ttp.priorDate = :priorDate)"); 
									
										 
									 }catch (ParseException e) { }
								 }
								 
								 			 
							 }  
							 else if (searchBy.equalsIgnoreCase("txReception.mFileType.id")) {
									sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE txTmGeneral.txReception.mFileType.id = :p" + i + ")");
							 }
							 else if (searchBy.equalsIgnoreCase("mFileTypeDetail.id")) {
									sbQuery.append(" AND c.id IN (SELECT c FROM TxMonitor c WHERE txTmGeneral.txReception.mFileTypeDetail.id = :p" + i + ")");
							 } 
						     
							 else if(searchBy.equalsIgnoreCase("startDate") 
								  || searchBy.equalsIgnoreCase("endDate")){
							 	 
								 if (value != null) {
									 try {
										 Calendar calendar = Calendar.getInstance();
										 if (value instanceof Timestamp) {
											 calendar.setTimeInMillis(((Timestamp) value).getTime());
										 } else {
											 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.ms");
											 calendar.setTime(sdf.parse(value.toString()));
										 }
										 if (searchBy.equalsIgnoreCase("startDate")) {
											 calendar.set(Calendar.HOUR, 0);
											 calendar.set(Calendar.MINUTE, 0);
											 calendar.set(Calendar.SECOND, 0);

											 startDate = new Timestamp(calendar.getTimeInMillis());
											 sbQuery.append(" AND c.txTmGeneral.filingDate >= :startDate");  
										 } else if (searchBy.equalsIgnoreCase("endDate")) {
											 calendar.set(Calendar.HOUR, 23);
											 calendar.set(Calendar.MINUTE, 59);
											 calendar.set(Calendar.SECOND, 59);

											 endDate = new Timestamp(calendar.getTimeInMillis());
											 sbQuery.append(" AND c.txTmGeneral.filingDate <= :endDate");  
										 }
									 } 
									 catch (ParseException e) { }
								 }
 
							}
							else if(searchBy.equalsIgnoreCase("tglProsesStartDate") 
									  || searchBy.equalsIgnoreCase("tglProsesEndDate")){
								 	 
									 if (value != null) {
										 try {
											 Calendar calendar = Calendar.getInstance();
											 if (value instanceof Timestamp) {
												 calendar.setTimeInMillis(((Timestamp) value).getTime());
											 } else {
												 SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
												 calendar.setTime(sdf.parse(value.toString()));
											 }
											 if (searchBy.equalsIgnoreCase("tglProsesStartDate")) {
												 calendar.set(Calendar.HOUR, 0);
												 calendar.set(Calendar.MINUTE, 0);
												 calendar.set(Calendar.SECOND, 0);

												 tglProsesStartDate = new Timestamp(calendar.getTimeInMillis()); 
												 sbQuery.append(" AND c.createdDate >= :tglProsesStartDate");
											 } else if (searchBy.equalsIgnoreCase("tglProsesEndDate")) {
												 calendar.set(Calendar.HOUR, 23);
												 calendar.set(Calendar.MINUTE, 59);
												 calendar.set(Calendar.SECOND, 59);

												 tglProsesEndDate = new Timestamp(calendar.getTimeInMillis());  
												 sbQuery.append(" AND c.createdDate <= :tglProsesEndDate");
											 }
										 } 
										 catch (ParseException e) { }
									 }
	 
						    }
							else if (value instanceof String) {
								if (searchCriteria.get(i).isExactMatch()) { 
									sbQuery.append(" AND LOWER(c.txTmGeneral." + searchBy + ") = :p" + i);
								} else { 
									sbQuery.append(" AND LOWER(c.txTmGeneral." + searchBy + ") LIKE :p" + i);
 
								}
							}
						}
					}
					if (value == null) {
						sbQuery.append(" AND c.txTmGeneral." + searchBy + " IS NULL");
					}
				}

			}

			if (orderBy != null && orderType != null) {
				sbQuery.append(" ORDER BY c." + orderBy + " " + orderType);
			} 
			
			Query query = entityManager.createQuery(sbQuery.toString());
			if (searchCriteria != null) {

				if (!searchAppNo.isEmpty()) {
					query.setParameter("applicationNo", searchAppNo);
				}

				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();
					if (value != null) {
						if( searchBy.contentEquals("createdBy") && createdBy!=null) {
							query.setParameter(searchBy, createdBy);
							System.err.println("?"+createdBy);
						}
						else if (searchBy.contentEquals("startDate")) {
							query.setParameter(searchBy, startDate);
						
						}else if(searchBy.contentEquals("endDate")) {
							query.setParameter(searchBy, endDate);
						
						}else if(searchBy.contentEquals("tglProsesStartDate")) {
							query.setParameter(searchBy, tglProsesStartDate);
						
						}else if(searchBy.contentEquals("tglProsesEndDate")) {
							query.setParameter(searchBy, tglProsesEndDate);
						
						}else if(searchBy.contentEquals("priorDate")) { 
							query.setParameter(searchBy, priorDate);
						
						}else if (value instanceof String 
								&& !(searchBy.contentEquals("endDate") ||searchBy.contentEquals("startDate"))
								&& !(searchBy.contentEquals("tglProsesStartDate") ||searchBy.contentEquals("tglProsesEndDate"))
								&& !(searchBy.contentEquals("priorDate"))) { 
							
							if (searchCriteria.get(i).isExactMatch()) {
								if(searchBy.equalsIgnoreCase("mFileType.id") || searchBy.equalsIgnoreCase("mFileTypeDetail.id")) {
									query.setParameter("p" + i, ((String) value));
								} else {
									query.setParameter("p" + i, ((String) value).toLowerCase());
								}
							}else {
								
								if(searchBy.equalsIgnoreCase("txTmBrand.name") || searchBy.equalsIgnoreCase("applicationNo") || searchBy.equalsIgnoreCase("txRegistration.no") ) { 
									query.setParameter("p" + i, "%" + ((String) value).toLowerCase() + "%");
									if(((String) value).contains("%")){
										query.setParameter("p" + i, ((String) value).toLowerCase());
									}else {
										query.setParameter("p" + i, "%" + ((String) value).toLowerCase() + "%");
									}
								} 
								else { 
									query.setParameter("p" + i, value); 
								}
								
							}
							 
						
						} else if (value instanceof List) {
							for (Object subValue : (List) value) {
								if (subValue instanceof String) {
									query.setParameter("p" + j++, ((String) subValue).toLowerCase());
								} else {
									query.setParameter("p" + j++, subValue);
								}
							}
						} else {
							query.setParameter("p" + j++, value);
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
			System.out.println("QUERY "+sbQuery.toString());
			return query.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null; 
	}
 

}