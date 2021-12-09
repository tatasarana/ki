package com.docotel.ki.repository.custom.transaction;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.transaction.TxMadrid;
import com.docotel.ki.model.transaction.TxReception;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.pojo.MadridDetailInfo;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;

import org.springframework.stereotype.Repository;

@Repository
public class TxMadridCustomRepository extends BaseRepository<TxMadrid> {
    
	public TxMadrid selectOne(String by, Object value){
        return super.selectOne("LEFT JOIN FETCH c.createdBy cb "
//                + "LEFT JOIN FETCH c.txTmGeneral tg "
//                + "LEFT JOIN FETCH c.mRepresentative mr "
//                + "LEFT JOIN FETCH mr.mCountry mc "
//                + "LEFT JOIN FETCH mr.mProvince mp "
//                + "LEFT JOIN FETCH mr.mCity mci "
                + " ", by, value, false);
    }	
	
	public void deleteAllTransactionFileId(String xmlFileId ) {			
		entityManager.createQuery("DELETE FROM TxTmClass c WHERE c.xmlFileId = :p1").setParameter("p1", xmlFileId).executeUpdate();   
		entityManager.createQuery("DELETE FROM TxTmPrior c WHERE c.xmlFileId = :p1").setParameter("p1", xmlFileId).executeUpdate();          
		entityManager.createQuery("DELETE FROM TxTmBrand c WHERE c.xmlFileId = :p1").setParameter("p1", xmlFileId).executeUpdate(); 
		entityManager.createQuery("DELETE FROM TxTmRepresentative c WHERE c.xmlFileId = :p1").setParameter("p1", xmlFileId).executeUpdate(); 
		entityManager.createQuery("DELETE FROM TxTmOwnerDetail c WHERE c.xmlFileId = :p1").setParameter("p1", xmlFileId).executeUpdate(); 
		entityManager.createQuery("DELETE FROM TxTmOwner c WHERE c.xmlFileId = :p1").setParameter("p1", xmlFileId).executeUpdate();	
//		entityManager.createQuery("DELETE FROM TxTmGeneral c WHERE c.xmlFileId = :p1").setParameter("p1", xmlFileId).executeUpdate(); //akan error ketika tx_tm_doc sudah ada isinya
//		entityManager.createQuery("DELETE FROM TxReception c WHERE c.xmlFileId = :p1").setParameter("p1", xmlFileId).executeUpdate();   
    }
	
	public void modifiedXMLFile(String xmlFileId ) { 
		entityManager.createQuery("UPDATE XMLFile c SET processed=:p0 WHERE c.id = :p1")
					.setParameter("p0", true)
					.setParameter("p1", xmlFileId)
					.executeUpdate();  
	}
	
	
	
	/*
	List<TxReception> txrList = new ArrayList<>();
		List<TxMadrid> txmList = new ArrayList<>();
		List<MadridDetailInfo> mdiList = new ArrayList<>();
	
	*/
	public void processMadridProcedure(List<TxReception> TxReceptionList, List<TxMadrid> TxMadridList, List<MadridDetailInfo> MadridDetailInfoList ) {
		StoredProcedureQuery spQuery = entityManager.createNamedStoredProcedureQuery("PROC_MADRID")
				//.registerStoredProcedureParameter(TxReceptionList, List.class, ParameterMode.IN)
				  //.registerStoredProcedureParameter("New Foo",  String.class , ParameterMode.IN )
				;
		spQuery.execute();
	}
	
	
	public long countMadrid(List<KeyValue> searchCriteria) {
		try {
			Timestamp p1 = null;
			Timestamp p2 = null;
			int j=10;
			StringBuffer sbQuery = new StringBuffer("SELECT COUNT(c) FROM " + getClassName() + " c ");			
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (searchBy.equalsIgnoreCase("uploadDate")) {
							SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
							String sValue = sdf.format(value).toString();
							Date appDate = sdf.parse(sValue);							 
							Calendar calendar = Calendar.getInstance();
							calendar.setTime(appDate);
							calendar.set(Calendar.HOUR, 0);
							calendar.set(Calendar.MINUTE, 0);
							calendar.set(Calendar.SECOND, 0);							
							p1 = new Timestamp(calendar.getTimeInMillis());
							
							calendar.set(Calendar.HOUR, 23);
							calendar.set(Calendar.MINUTE, 59);
							calendar.set(Calendar.SECOND, 59);							
							p2 =  new Timestamp(calendar.getTimeInMillis());
							
							sbQuery.append(" AND c.uploadDate >=:p" + j );	
							j++;
							sbQuery.append(" AND c.uploadDate <=:p" + j );
							
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
					if (value != null ) {
						if("uploadDate".equalsIgnoreCase(searchBy)) {
							j=10;
							query.setParameter("p" + j, p1);
							j++;
							query.setParameter("p" + j, p2);
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
			return (Long) query.setMaxResults(1).getSingleResult();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return 0L;
	}
	
	
	public List<TxMadrid> selectAllMadrid( List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		Timestamp p1 = null;
		Timestamp p2 = null;
		int xx=10; 

		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
			//sbQuery.append("LEFT JOIN FETCH c.mFileSequence s  ");
			
			
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				int j = 0;
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (searchBy.equalsIgnoreCase("uploadDate")) {
							SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
							String sValue = sdf.format(value).toString();
							Date appDate = sdf.parse(sValue);							 
							Calendar calendar = Calendar.getInstance();
							calendar.setTime(appDate);
							calendar.set(Calendar.HOUR, 0);
							calendar.set(Calendar.MINUTE, 0);
							calendar.set(Calendar.SECOND, 0);							
							p1 = new Timestamp(calendar.getTimeInMillis());
							
							calendar.set(Calendar.HOUR, 23);
							calendar.set(Calendar.MINUTE, 59);
							calendar.set(Calendar.SECOND, 59);							
							p2 =  new Timestamp(calendar.getTimeInMillis());
							
							sbQuery.append(" AND c.uploadDate >=:p" + xx );	
							xx++;
							sbQuery.append(" AND c.uploadDate <=:p" + xx );
							
                        } else {
                        	if (value == null) {
    							sbQuery.append(" AND c." + searchBy + " IS NULL");
    						} else if (value instanceof String) {
    							if (searchCriteria.get(i).isExactMatch()) {
    								sbQuery.append(" AND LOWER(c." + searchBy + ") = :p" + j++);
    							} else {
    								sbQuery.append(" AND LOWER(c." + searchBy + ") LIKE :p" + j++);
    							}
    						} else if (value instanceof List) {
    							sbQuery.append(" AND LOWER(c." + searchBy + ") IN (");
    							for (int k = 0; k < ((List) value).size(); k++) {
    								sbQuery.append(":p" + j++);
    								if (k < ((List) value).size() - 1) {
    									sbQuery.append(", ");
    								}
    							}
    							sbQuery.append(")");
    						} else {
    							sbQuery.append(" AND c." + searchBy + " = :p" + j++);
    						}
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
					String searchBy = searchCriteria.get(i).getKey();
					
					
					if (value != null) {
						if("uploadDate".equalsIgnoreCase(searchBy)) {
							xx=10;
							query.setParameter("p" + xx, p1);
							xx++;
							query.setParameter("p" + xx, p2);
						} else {
							if (value instanceof String) {
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