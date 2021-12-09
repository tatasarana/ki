package com.docotel.ki.repository.custom.master;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.master.MFileType;
import com.docotel.ki.model.master.MFileTypeDetail;
import com.docotel.ki.pojo.KeyValue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

@Repository
public class MFileTypeCustomRepository extends BaseRepository<MFileType> {

    public List<MFileType> selectAll(String joinCriteria, String by, Object value, boolean exactMatch, String orderBy, String orderType, Integer offset, Integer limit) {
        return super.selectAll(joinCriteria, by, value, true, orderBy, orderType, offset, limit);
    }

    /*public List<MFileType> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        return super.selectAll(searchCriteria,orderBy,orderType,offset,limit);
    }*/
   
    public List<MFileType> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		return selectAll("JOIN FETCH c.createdBy cb", searchCriteria, orderBy, orderType, offset, limit);
	}
    
    
    public long countAllExclude(List<KeyValue> searchCriteria, String[] exclude) {
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

            if(exclude != null) {
                String excludeList = "";
                for (int i = 0; i < exclude.length; i++) {
                    excludeList = excludeList + (excludeList.equals("") ? "'" + exclude[i] + "'" : ",'" + exclude[i] + "'");
                }
                sbQuery.append(" AND c.id NOT IN (" + excludeList + ")");
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

    public List<MFileTypeDetail> selectAllExclude(String joinCriteria, List<KeyValue> searchCriteria, String[] exclude, String orderBy, String orderType, Integer offset, Integer limit) {
        try {
            StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
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

            if(exclude != null) {
                String excludeList = "";
                for (int i = 0; i < exclude.length; i++) {
                    excludeList = excludeList + (excludeList.equals("") ? "'" + exclude[i] + "'" : ",'" + exclude[i] + "'");
                }
                sbQuery.append(" AND c.id NOT IN (" + excludeList + ")");
            }
            if (orderBy != null && orderType != null) {
                sbQuery.append(" ORDER BY c." + orderBy + " " + orderType);
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
    
    
	public List<MFileType> selectByFileTypeCode() {
        try {
            StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");   	            
            sbQuery.append("WHERE c.code in ('D','J','K')");              
            sbQuery.append(" ORDER BY c.code " );
            Query query = entityManager.createQuery(sbQuery.toString());           
            return query.getResultList();
        } catch (NoResultException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
	}

}
