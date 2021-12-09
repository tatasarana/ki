package com.docotel.ki.repository.custom.transaction;


import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.transaction.TxMonitor;
import com.docotel.ki.model.transaction.TxPostReception;
import com.docotel.ki.pojo.KeyValue;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

@Repository
public class TxPostReceptionCustomRepository extends BaseRepository<TxPostReception> {
	public long countPasca(String joinCriteria, List<KeyValue> searchCriteria) {
		Timestamp postDate = null;
		Timestamp postEndDate = null;

		try {
			StringBuffer sbQuery = new StringBuffer("SELECT COUNT(c) FROM " + getClassName() + " c ");
			if (StringUtils.isNotBlank(joinCriteria)) {
				sbQuery.append(joinCriteria);
				sbQuery.append(" ");
			}
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (searchBy.equalsIgnoreCase("statusExclude")) {
							if (searchCriteria.get(i).isExactMatch()) {
								sbQuery.append(" AND LOWER(c.mStatus.id) != :p" + i + ") ");
							} else {
								sbQuery.append(" AND LOWER(c.mStatus.id) NOT LIKE :p" + i + ") ");
							}
						} else if (searchBy.equalsIgnoreCase("postDate")) {
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

									postDate = new Timestamp(calendar.getTimeInMillis());

									sbQuery.append(" AND TRUNC(c." + searchBy + ") >= :postDate");
								} catch (ParseException e) {
//									System.out.println(" " + e);
								}
							}
						}else if (searchBy.equalsIgnoreCase("postEndDate")) {
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

									postEndDate = new Timestamp(calendar.getTimeInMillis());

									sbQuery.append(" AND TRUNC(c.postDate) <= :postEndDate");
								} catch (ParseException e) {
									//System.out.println(" " + e);
								}
							}
						} else if (value == null) {
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
							if (searchCriteria.get(i).getKey().equalsIgnoreCase("postDate")) {
								if (postDate != null) {
									query.setParameter("postDate", postDate);
								}
							} else if (searchCriteria.get(i).getKey().equalsIgnoreCase("postEndDate")) {
								if (postEndDate != null) {
									query.setParameter("postEndDate", postEndDate);
								}
							} else if (searchCriteria.get(i).isExactMatch()) {
								query.setParameter("p" + i, ((String) value).toLowerCase());
							} else {
								query.setParameter("p" + i, "%" + ((String) value).toLowerCase() + "%");
							}
						} else {
							if (searchCriteria.get(i).getKey().equalsIgnoreCase("postDate")) {
								if (postDate != null) {
									query.setParameter("postDate", postDate);
								}
							} else if (searchCriteria.get(i).getKey().equalsIgnoreCase("postEndDate")) {
								if (postEndDate != null) {
									query.setParameter("postEndDate", postEndDate);
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
	
	public List<TxPostReception> selectAllPasca(String joinCriteria, List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		Timestamp postDate = null;
		Timestamp postEndDate = null;

		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
			if (StringUtils.isNotBlank(joinCriteria)) {
				//sbQuery.append(joinCriteria);
				sbQuery.append(" ");
			}
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (searchBy.equalsIgnoreCase("statusExclude")) {
							if (searchCriteria.get(i).isExactMatch()) {
								sbQuery.append(" AND LOWER(c.mStatus.id) != LOWER(:p" + i + ") ");
							} else {
								sbQuery.append(" AND LOWER(c.mStatus.id) NOT LIKE LOWER(:p" + i + ") ");
							}
						} else if (searchBy.equalsIgnoreCase("postDate")) {
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

									postDate = new Timestamp(calendar.getTimeInMillis());

									sbQuery.append(" AND TRUNC(c." + searchBy + ") >= :p" + i);
								} catch (ParseException e) {
									//System.out.println(" " + e);
								}
							}
						} else if (searchBy.equalsIgnoreCase("postEndDate")) {
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

									postEndDate = new Timestamp(calendar.getTimeInMillis());

									sbQuery.append(" AND TRUNC(c.postDate) <= :p" + i);
								} catch (ParseException e) {
									//System.out.println(" " + e);
								}
							}
						} else if (value == null) {
							sbQuery.append(" AND c." + searchBy + " IS NULL");
						} else if (value instanceof String) {
							if (searchCriteria.get(i).isExactMatch()) {
								sbQuery.append(" AND LOWER(c." + searchBy + ") = :p" + i);
							} else {
								sbQuery.append(" AND LOWER(c." + searchBy + ") LIKE :p" + i);
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
				sbQuery.append(" ORDER BY c." + orderBy  + " " + orderType + ", c.eFilingNo " +
						"");
			}

			Query query = entityManager.createQuery(sbQuery.toString());
			if (searchCriteria != null) {
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					Object value = searchCriteria.get(i).getValue();
					if (value != null) {
						if (value instanceof String) {
							if (searchCriteria.get(i).getKey().equalsIgnoreCase("postDate")) {
								if (postDate != null) {
									query.setParameter("p" + j++, postDate);
								}
							} else if (searchCriteria.get(i).getKey().equalsIgnoreCase("postEndDate")) {
								if (postEndDate != null) {
									query.setParameter("p" + j++, postEndDate);
								}
							} else if (searchCriteria.get(i).isExactMatch()) {
								query.setParameter("p" + j++, ((String) value).toLowerCase());
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
							if (searchCriteria.get(i).getKey().equalsIgnoreCase("postDate")) {
								if (postDate != null) {
									query.setParameter("p" + j++, postDate);
								}
							} else if (searchCriteria.get(i).getKey().equalsIgnoreCase("postEndDate")) {
								if (postEndDate != null) {
									query.setParameter("p" + j++, postEndDate);
								}
							} else {
								query.setParameter("p" + j++, value);
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


	public List<TxPostReception> selectAllPascaDup(String joinCriteria, List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		Timestamp postDate = null;
		Timestamp postEndDate = null;

		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c. FROM tx_post_reception c ");
			if (StringUtils.isNotBlank(joinCriteria)) {
				sbQuery.append(joinCriteria);
				sbQuery.append(" ");
			}
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (searchBy.equalsIgnoreCase("statusExclude")) {
							if (searchCriteria.get(i).isExactMatch()) {
								sbQuery.append(" AND LOWER(c.mStatus.id) != LOWER(:p" + i + ") ");
							} else {
								sbQuery.append(" AND LOWER(c.mStatus.id) NOT LIKE LOWER(:p" + i + ") ");
							}
						} else if (searchBy.equalsIgnoreCase("postDate")) {
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

									postDate = new Timestamp(calendar.getTimeInMillis());

									sbQuery.append(" AND TRUNC(c." + searchBy + ") >= :p" + i);
								} catch (ParseException e) {
									//System.out.println(" " + e);
								}
							}
						} else if (searchBy.equalsIgnoreCase("postEndDate")) {
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

									postEndDate = new Timestamp(calendar.getTimeInMillis());

									sbQuery.append(" AND TRUNC(c.postDate) <= :p" + i);
								} catch (ParseException e) {
									//System.out.println(" " + e);
								}
							}
						} else if (value == null) {
							sbQuery.append(" AND c." + searchBy + " IS NULL");
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
							if (searchCriteria.get(i).getKey().equalsIgnoreCase("postDate")) {
								if (postDate != null) {
									query.setParameter("p" + j++, postDate);
								}
							} else if (searchCriteria.get(i).getKey().equalsIgnoreCase("postEndDate")) {
								if (postEndDate != null) {
									query.setParameter("p" + j++, postEndDate);
								}
							} else if (searchCriteria.get(i).isExactMatch()) {
								query.setParameter("p" + j++, ((String) value).toLowerCase());
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
							if (searchCriteria.get(i).getKey().equalsIgnoreCase("postDate")) {
								if (postDate != null) {
									query.setParameter("p" + j++, postDate);
								}
							} else if (searchCriteria.get(i).getKey().equalsIgnoreCase("postEndDate")) {
								if (postEndDate != null) {
									query.setParameter("p" + j++, postEndDate);
								}
							} else {
								query.setParameter("p" + j++, value);
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
	
    public TxPostReception selectOne(String by, Object value) {
        return super.selectOne("LEFT JOIN FETCH c.createdBy cb "
                + "LEFT JOIN FETCH c.mFileSequence ms "
                + "LEFT JOIN FETCH c.mFileType ft "
                + "LEFT JOIN FETCH c.mFileTypeDetail ftc "
                + " ", by, value, true);
    }

    @Override
    public List<TxPostReception> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        return super.selectAll("LEFT JOIN FETCH c.mFileSequence s LEFT JOIN FETCH c.mFileTypeDetail tc", searchCriteria, orderBy, orderType, offset, limit);
    }
    
    public List<TxPostReception> selectPostNote(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        return super.selectAll(searchCriteria, orderBy, orderType, offset, limit);
    }

    public List<TxPostReception> SelectDaftarBRM(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        return super.selectAll("LEFT JOIN FETCH c.mFileSequence s " +
                "LEFT JOIN FETCH c.mFileTypeDetail tc "
                + "", searchCriteria, orderBy, orderType, offset, limit);
    }

    public TxPostReception selectLastPostReceiptNo() {
       return (TxPostReception) super.selectAll(null, null, null, true, "postReceiptNo", "DESC", 0, 1);
    }


	public String getPostReceptionNobyApplicationNo(String applicationID){
			Query query = entityManager.createNativeQuery("SELECT POST_RECEPTION_ID FROM TX_POST_RECEPTION_DETAIL WHERE APPLICATION_ID=:p1");
			query.setParameter("p1", applicationID.toString());
			List postReceptionID = query.getResultList();
			if(postReceptionID.size()>0){
				String nos = "";
				for(Object recID : postReceptionID) {
					query = entityManager.createNativeQuery("SELECT POST_RECEPTION_NO FROM TX_POST_RECEPTION WHERE POST_RECEPTION_ID=:p1");
					query.setParameter("p1", recID.toString());
					List postReceptionNo = query.getResultList();
					if (postReceptionNo.size() > 0) {
						for (Object no : postReceptionNo) {
							if (nos == "") {
								nos += no.toString();
							} else {
								nos += "," + no.toString();
							}
						}
					}
				}
				return nos;
			}
		return "";
	}

	public String getPostReceptionDatebyApplicationNo(String applicationID){
		Query query = entityManager.createNativeQuery("SELECT POST_RECEPTION_ID FROM TX_POST_RECEPTION_DETAIL WHERE APPLICATION_ID=:p1");
		query.setParameter("p1", applicationID.toString());
		List postReceptionID = query.getResultList();
		if(postReceptionID.size()>0){
			query = entityManager.createNativeQuery("SELECT POST_RECEPTION_DATE FROM TX_POST_RECEPTION WHERE POST_RECEPTION_ID=:p1");
			query.setParameter("p1",postReceptionID.get(0).toString());
			List postReceptionDate = query.getResultList();
			if(postReceptionDate.size()>0){
				return postReceptionDate.get(0).toString();
			}
		}
		return "";
	}

}
