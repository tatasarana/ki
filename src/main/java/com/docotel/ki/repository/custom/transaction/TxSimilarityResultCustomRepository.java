package com.docotel.ki.repository.custom.transaction;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.master.MCity;
import com.docotel.ki.model.master.MSection;
import com.docotel.ki.model.transaction.TxSimilarityResult;
import com.docotel.ki.model.transaction.TxTmBrand;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.util.Soundex;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

@Repository
public class TxSimilarityResultCustomRepository extends BaseRepository<TxSimilarityResult> {

	public long countSimilarityResult(List<KeyValue> searchCriteria, String[] exclude, boolean phonetic) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT count(c) FROM " + TxTmGeneral.class.getName() + " c ");
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				StringBuffer mClass = new StringBuffer();
				StringBuffer ownerName = new StringBuffer();
				StringBuffer classList = new StringBuffer();
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();
					
					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (searchBy.equalsIgnoreCase("mClassId")) {
							sbQuery.append(" AND c.id IN (SELECT ttc.txTmGeneral FROM TxTmClass ttc LEFT JOIN ttc.mClass mc WHERE mc.id IN (");
						  
									for (int j = 0; j < ((List) value).size(); j++) {
										if (j == ((List) value).size() - 1) {
											sbQuery.append(":r" + j + "))");
										} else {
											sbQuery.append(":r" + j + ",");
										}
									} 
							
						} else if (searchBy.equalsIgnoreCase("mClassDetailDesc")) {
							mClass.append(" AND LOWER(mcc.desc) LIKE :p" + i + " OR LOWER(mcc.descEn) LIKE :p" + i);
						} else if (searchBy.equalsIgnoreCase("txTmOwnerName")) { 
							ownerName.append(" AND LOWER(tto.name) LIKE :p" + i);
						} else if (searchBy.equalsIgnoreCase("similarId")) {
							sbQuery.append(" AND c.id IN (SELECT tsr.similarTxTmGeneral FROM TxSimilarityResult tsr WHERE tsr.originTxTmGeneral = :p" + i + ")");
						} else if (phonetic){ //(searchBy.contains("txTmBrand.name || c.txTmBrand.keywordMerek")) {
							//if{
								sbQuery.append(" AND c.id IN (SELECT ttb.txTmGeneral.id FROM TxTmBrand ttb WHERE SOUNDEX(ttb.name) = :p" + i+ ") ");

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
				if(mClass.length() > 0) {  
					sbQuery.append(" AND c.id IN (SELECT ttc.txTmGeneral FROM TxTmClass ttc LEFT JOIN ttc.mClass mc LEFT JOIN ttc.mClassDetail mcc WHERE 1 = 1 " + mClass.toString() + ")");
				}
				if(classList.length() > 0) { 
					sbQuery.append( classList.toString());
				}
				if(ownerName.length() > 0) { 
					sbQuery.append(" AND c.id IN (SELECT tto.txTmGeneral FROM TxTmOwner tto WHERE 1 = 1  " + ownerName.toString() + ")");
				}
			}

			if(exclude != null) {
				String excludeList = "";
				for (int i = 0; i < exclude.length; i++) {
					excludeList = excludeList + (excludeList.equals("") ? "'" + exclude[i] + "'" : ",'" + exclude[i] + "'");
				}
				sbQuery.append(" AND c.id NOT IN (" + excludeList + ")");
			}

//			System.out.println("dump query " + sbQuery.toString());
			Query query = entityManager.createQuery(sbQuery.toString());
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();
					
					if (value != null) {
						if (searchBy.equalsIgnoreCase("mClassId")) {
								for (int j = 0; j < ((List) value).size(); j++) {  
									query.setParameter("r" + j, ((List) value).get(j)  );
								}
						}
						else if (phonetic){//(searchBy.contains("txTmBrand.name || c.txTmBrand.keywordMerek")) {
							String valueStr = value.toString().replaceAll("\\*", "_").replace("%","");
							//System.out.println("VALUE:"+value);
							query.setParameter("p" + i, (valueStr));
							// SOUNDEX
							//if {
								value = Soundex.getGode(valueStr);
								System.out.println("SOUNDEX: "+value);
								query.setParameter("p" + i, (value));
							//}
						}
						else if (value instanceof String) {
							if (searchCriteria.get(i).isExactMatch()) {
								query.setParameter("p" + i, ((String) value).toLowerCase());
							} else {
								query.setParameter("p" + i, ((String) value).toLowerCase());
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
	
	public List<TxTmGeneral> selectAllSimilarityResult(List<KeyValue> searchCriteria,  String[] exclude, String orderBy, String orderType, Integer offset, Integer limit, boolean phonetic) {
        try {
            StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + TxTmGeneral.class.getName() + " c ");
//            sbQuery.append("LEFT JOIN FETCH c.txTmBrand ttb ");
//			sbQuery.append("LEFT JOIN FETCH c.txTmClassList ttc ");
//			sbQuery.append("LEFT JOIN FETCH ttc.mClass mc ");
//			sbQuery.append("LEFT JOIN FETCH c.txTmOwner tto ");
			sbQuery.append("LEFT JOIN FETCH c.mStatus ms ");

			if(orderBy!=null) {
				if (orderBy.equalsIgnoreCase("txTmClassList.mClass.no")) {
					sbQuery.append("LEFT JOIN c.txTmClassList ttc ");
				} else if (orderBy.equalsIgnoreCase("txTmOwner.name")) {
					sbQuery.append("LEFT JOIN c.txTmOwner tto ");
				} else if (orderBy.equalsIgnoreCase("txTmReprs.name")) {
					sbQuery.append("LEFT JOIN c.txTmRepresentative ttr ");
				} else if (orderBy.equalsIgnoreCase("txTmPriorList.priorDate")) {
					sbQuery.append("LEFT JOIN c.txTmPriorList ttp ");
				}
			}

			sbQuery.append("WHERE 1 = 1");

            if (searchCriteria != null) {
            	StringBuffer mClass = new StringBuffer();
            	StringBuffer ownerName = new StringBuffer();
            	StringBuffer classList = new StringBuffer();
            	for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (searchBy.equalsIgnoreCase("mClassId")) { 
							
							sbQuery.append(" AND c.id IN (SELECT ttc.txTmGeneral FROM TxTmClass ttc LEFT JOIN ttc.mClass mc WHERE mc.id IN (");
						 
								for (int j = 0; j < ((List) value).size(); j++) {
									if (j == ((List) value).size() - 1) {
										sbQuery.append(":r" + j + "))");
									} else {
										sbQuery.append(":r" + j + ",");
									}
								}
								 
							
						} else if (searchBy.equalsIgnoreCase("mClassDetailDesc")) {
							mClass.append(" AND LOWER(mcc.desc) LIKE :p" + i + " OR LOWER(mcc.descEn) LIKE :p" + i);
						} else if (searchBy.equalsIgnoreCase("txTmOwnerName")) {
							ownerName.append(" AND LOWER(tto.name) LIKE :p" + i);
						} else if (searchBy.equalsIgnoreCase("similarId")) {
							sbQuery.append(" AND c.id IN (SELECT tsr.similarTxTmGeneral FROM TxSimilarityResult tsr WHERE tsr.originTxTmGeneral = :p" + i + ")");
						} else if (phonetic){//(searchBy.contains("txTmBrand.name || c.txTmBrand.keywordMerek")) {
							//if{
								sbQuery.append(" AND c.id IN (SELECT ttb.txTmGeneral.id FROM TxTmBrand ttb WHERE SOUNDEX(ttb.name) = :p" + i+ ") ");
						}
					else {
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
            	if(mClass.length() > 0) {
					sbQuery.append(" AND c.id IN (SELECT ttc.txTmGeneral FROM TxTmClass ttc LEFT JOIN ttc.mClass mc LEFT JOIN ttc.mClassDetail mcc WHERE 1 = 1 " + mClass.toString() + ")");
				}
            	if(classList.length() > 0) {
            		sbQuery.append(classList.toString());
				}
            	if(ownerName.length() > 0) {
					sbQuery.append(" AND c.id IN (SELECT tto.txTmGeneral FROM TxTmOwner tto WHERE 1 = 1 " + ownerName.toString() + ")");
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
				if (orderBy.equalsIgnoreCase("txTmClassList.mClass.no")) {
					sbQuery.append(" ORDER BY ttc.mClass.no " + orderType);
				} else if (orderBy.equalsIgnoreCase("txTmOwner.name")) {
					sbQuery.append(" ORDER BY tto.name " + orderType);
				} else if (orderBy.equalsIgnoreCase("txTmPriorList.priorDate")) {
					sbQuery.append(" ORDER BY ttp.priorDate " + orderType);
				} else {
					sbQuery.append(" ORDER BY c." + orderBy + " " + orderType);
				}
	        }

//	        System.out.println("dump query " + sbQuery.toString());
            Query query = entityManager.createQuery(sbQuery.toString());
            if (searchCriteria != null) {
                for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
                    Object value = searchCriteria.get(i).getValue();
                    if (value != null) {
                    	if (searchBy.equalsIgnoreCase("mClassId")) { 
								  
							for (int j = 0; j < ((List) value).size(); j++) { 
								query.setParameter("r" + j, ((List) value).get(j)  );
							} 	
							
						} else if (phonetic) { //searchBy.contains("txTmBrand.name || c.txTmBrand.keywordMerek")) {
							String valueStr = value.toString().replaceAll("\\*", "_").replace("%","");
							query.setParameter("p" + i, (valueStr));
							//System.out.println("VALUE:"+value);
							// SOUNDEX
							//if() {
								value = Soundex.getGode(valueStr);
								//System.out.println("SOUNDEX: "+value);
								query.setParameter("p" + i, (value));
							//}
						}
						else if (value instanceof String) {
							if (searchCriteria.get(i).isExactMatch()) {
								query.setParameter("p" + i, ((String) value).toLowerCase());
							} else {
								query.setParameter("p" + i, ((String) value).toLowerCase());
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

	public List<TxSimilarityResult> selectAllByOriginAppId(String by, Object value, Integer offset, Integer limit) {
		return super.selectAll(by, value, true, offset, limit);
	}

	public void delete(TxSimilarityResult form) {
		entityManager.createQuery("DELETE FROM " + getClassName() + " c WHERE c.originTxTmGeneral.id = :p0 AND c.similarTxTmGeneral.id = :p1")
				.setParameter("p0", form.getOriginTxTmGeneral().getCurrentId())
				.setParameter("p1", form.getSimilarTxTmGeneral().getCurrentId())
				.executeUpdate();
	}
}
