package com.docotel.ki.repository.online.custom;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.transaction.TxOnlineReg;
import com.docotel.ki.pojo.KeyValue;
import com.ibm.icu.text.SimpleDateFormat;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Calendar;
import java.util.List;

@Repository
public class RegistrasiOnlineCustomRepository extends BaseRepository<TxOnlineReg> {
    public long countData(List<KeyValue> searchCriteria) {
        Timestamp createdDate = null;
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
                        } else if (searchBy.equalsIgnoreCase("createdDate")) {
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

                                    createdDate = new Timestamp(calendar.getTimeInMillis());

                                    sbQuery.append(" AND TRUNC(c." + searchBy + ") = :createdDate");
                                } catch (ParseException e) {
                                    //System.out.println(" " + e);
                                }
                            }
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
                            if (searchCriteria.get(i).getKey().equalsIgnoreCase("createdDate")) {
                                if (createdDate != null) {
                                    query.setParameter("createdDate", createdDate);
                                }
                            } else if (searchCriteria.get(i).isExactMatch()) {
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


    public List<TxOnlineReg> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        Timestamp createdDate = null;
        try {
            StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
            sbQuery.append(" LEFT JOIN FETCH c.nationality n LEFT JOIN FETCH c.gender g LEFT JOIN FETCH c.mProvince mp LEFT JOIN FETCH c.mCity mc LEFT JOIN FETCH c.mReprs mr ");
            sbQuery.append(" WHERE 1 = 1 ");
            if (searchCriteria != null) {
                int j = 0;
                for (int i = 0; i < searchCriteria.size(); i++) {
                    String searchBy = searchCriteria.get(i).getKey();
                    Object value = searchCriteria.get(i).getValue();

                    if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                        if (value == null) {
                            sbQuery.append(" AND c." + searchBy + " IS NULL");
                        } else if (searchBy.equalsIgnoreCase("createdDate")) {
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

                                    createdDate = new Timestamp(calendar.getTimeInMillis());

                                    sbQuery.append(" AND TRUNC(c." + searchBy + ") = :createdDate");
                                } catch (ParseException e) {
                                    System.err.println(e);
                                }
                            }
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
                        if (searchCriteria.get(i).getKey().equalsIgnoreCase("createdDate")) {
                            if (createdDate != null) {
                                query.setParameter("createdDate", createdDate);
                            }
                        } else if (value instanceof String) {
                            if (searchCriteria.get(i).isExactMatch()) {
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
}
