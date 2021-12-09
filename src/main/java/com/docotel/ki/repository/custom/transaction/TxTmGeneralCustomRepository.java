package com.docotel.ki.repository.custom.transaction;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.master.MStatus;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.transaction.TxMonitor;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.service.master.UserService;
import com.ibm.icu.text.SimpleDateFormat;
import com.docotel.ki.repository.transaction.TxMonitorRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Repository
public class TxTmGeneralCustomRepository extends BaseRepository<TxTmGeneral> {
	private static final Logger logger = LoggerFactory.getLogger(TxTmGeneralCustomRepository.class);
	public static boolean withJoin = true;
	@Autowired
	TxMonitorRepository txMonitorRepository;
	@Autowired
	private UserService userService;

	public TxTmGeneral findOneTxTmGeneralById(String appId) {
		try {
			return (TxTmGeneral) entityManager.createQuery(
					"SELECT new TxTmGeneral(c.id, c.txReception, c.applicationNo, c.mLaw, c.mStatus, c.mAction, c.ownerList, c.represList, "
							+ "c.priorList, c.onlineFlag, c.groupJournal, c.groupDist, c.xmlFileId, c.txTmOwner, c.txTmRepresentative, "
							+ "c.txTmPriorList, c.txTmBrand, c.txTmClassList, c.txTmDocList, c.txRegistration, c.txLicenseList) FROM " + getClassName() + " c "
							+ "WHERE c.id = :appId")
					.setParameter("appId", appId)
					.setMaxResults(1)
					.getSingleResult();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	public List<TxTmGeneral> findTxTmGeneralByRegNo(String regNos) {
		try {
			Query query = entityManager.createQuery(
					"SELECT c FROM " + getClassName() + " c "
							+ "LEFT JOIN FETCH c.txRegistration trg "
							+ "WHERE trg.no in ("+regNos+")");
			return query.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	@Override
	public List<TxTmGeneral> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		Timestamp startDate = null;
		Timestamp endDate = null;
		Timestamp receptionStartDate = null;
		Timestamp receptionEndDate = null;

		boolean orderByMerek = false;

		try {
			MUser createdBy = null;
			for(KeyValue sc : searchCriteria){
				String key = sc.getKey();
				if (key.equalsIgnoreCase("createdBy") && sc.getValue()!=null && !sc.getValue().toString().equalsIgnoreCase("")){
					createdBy = userService.getUserByEmail(sc.getValue().toString());
				}
			}

			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
			if(TxTmGeneralCustomRepository.withJoin) {
//				sbQuery.append("LEFT JOIN FETCH c.txTmBrand ttb ");
//				sbQuery.append("LEFT JOIN FETCH ttb.mBrandType mbt ");
//				sbQuery.append("LEFT JOIN FETCH c.txTmClassList ttc ");
//				sbQuery.append("LEFT JOIN FETCH ttc.mClass mc ");
//				sbQuery.append("LEFT JOIN FETCH c.txTmOwner tto ");
//				sbQuery.append("LEFT JOIN c.txTmPriorList ttp ");
//				sbQuery.append("LEFT JOIN FETCH c.mStatus ms ");
//				sbQuery.append("LEFT JOIN FETCH c.txTmRepresentative ttr ");
				sbQuery.append("LEFT JOIN c.txReception tr ");
//				sbQuery.append("LEFT JOIN FETCH c.txRegistration trg ");
//				sbQuery.append("LEFT JOIN FETCH tr.mFileType mft ");
//				sbQuery.append("LEFT JOIN FETCH tr.mFileTypeDetail mftd ");
				sbQuery.append("LEFT JOIN c.createdBy creatby ");
			} else {
				sbQuery.append("LEFT JOIN c.mStatus ms ");
				sbQuery.append("LEFT JOIN c.txReception tr ");
				TxTmGeneralCustomRepository.withJoin = true;
			}
			if(orderBy!=null) {
				if (orderBy.equalsIgnoreCase("txTmPriorList.priorDate")) {
					sbQuery.append("LEFT JOIN c.txTmPriorList ttp ");
				} else if (orderBy.equalsIgnoreCase("txTmOwner.name")) {
					sbQuery.append("LEFT JOIN c.txTmOwner tto ");
				} else if (orderBy.equalsIgnoreCase("txTmReprs.name")) {
					sbQuery.append("LEFT JOIN c.txTmRepresentative ttr ");
				}
			}

			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (searchBy.equalsIgnoreCase("createdBy") && createdBy!=null){
							sbQuery.append(" AND c.createdBy = :createdBy");
						} else if (searchBy.equalsIgnoreCase("statusOnOff")) {
							if (searchCriteria.get(i).isExactMatch()) {
								sbQuery.append(" AND (c.mStatus.id NOT LIKE 'IPT_DRAFT' OR c.mStatus.id IS NULL OR LOWER(c.mStatus.id) != :p" + i + ") ");
							} else {
								sbQuery.append(" AND (c.mStatus.id NOT LIKE 'IPT_DRAFT' OR c.mStatus.id IS NULL OR LOWER(c.mStatus.id) NOT LIKE :p" + i + ") ");
							}
						} else if (searchBy.equalsIgnoreCase("txTmClassList")) {
							sbQuery.append(" AND c.id IN (SELECT ttc.txTmGeneral FROM TxTmClass ttc LEFT JOIN ttc.mClass mc WHERE mc.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txTmClassDetailDesc")) {
							sbQuery.append(" AND c.id IN (SELECT ttc.txTmGeneral FROM TxTmClass ttc LEFT JOIN ttc.mClassDetail mcd WHERE LOWER(mcd.desc) LIKE :p" + i + " OR LOWER (mcd.descEn) LIKE :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("mClassDetail")) {
							sbQuery.append(" AND c.id IN (SELECT ttc.txTmGeneral FROM TxTmClass ttc LEFT JOIN ttc.mClassDetail mcd WHERE LOWER(mcd.id) LIKE :p" + i + ")");
						}else if (searchBy.equalsIgnoreCase("txTmOwnerName")) {
							sbQuery.append(" AND c.id IN (SELECT tto.txTmGeneral FROM TxTmOwner tto WHERE LOWER(tto.name) LIKE :p" + i + " AND tto.status=true)");
						} else if (searchBy.equalsIgnoreCase("txReprsName")) {
							sbQuery.append(" AND c.id IN (SELECT ttr.txTmGeneral FROM TxTmRepresentative ttr LEFT JOIN ttr.mRepresentative mr WHERE LOWER(mr.name) LIKE :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txTmPriorList.priorDate")) {
							sbQuery.append(" AND c.id IN (SELECT ttp.txTmGeneral FROM TxTmPrior ttp WHERE ttp.priorDate = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("mFileSequence.id")) {
							sbQuery.append(" AND (c.txReception.mFileSequence.id = :p" + i +")");
						} else if (searchBy.equalsIgnoreCase("mFileType.id")) {
							sbQuery.append(" AND (c.txReception.mFileType.id = :p" + i +")");
						} else if (searchBy.equalsIgnoreCase("mFileTypeDetail.id")) {
							sbQuery.append(" AND (c.txReception.mFileTypeDetail.id = :p" + i +")");
						} else if(searchBy.equalsIgnoreCase("startDate") || searchBy.equalsIgnoreCase("endDate")) {
							if (value != null) {
								try {
									Calendar calendar = Calendar.getInstance();
									if (value instanceof Timestamp) {
										calendar.setTimeInMillis(((Timestamp) value).getTime());
									} else {
										SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
										calendar.setTime(sdf.parse(value.toString()));
									}
									if (searchBy.equalsIgnoreCase("startDate")) {
										calendar.set(Calendar.HOUR, 0);
										calendar.set(Calendar.MINUTE, 0);
										calendar.set(Calendar.SECOND, 0);

										startDate = new Timestamp(calendar.getTimeInMillis());
										sbQuery.append(" AND c.filingDate >= :startDate");
									} else if (searchBy.equalsIgnoreCase("endDate")) {
										calendar.set(Calendar.HOUR, 23);
										calendar.set(Calendar.MINUTE, 59);
										calendar.set(Calendar.SECOND, 59);

										endDate = new Timestamp(calendar.getTimeInMillis());
										sbQuery.append(" AND c.filingDate <= :endDate");
									}
								} catch (ParseException e) {
								}
							}
						} else if (searchBy.equalsIgnoreCase("txReception.applicationDate")) {
							if (value != null) {
								try {
									Calendar calendar = Calendar.getInstance();
									if (value instanceof Timestamp) {
										calendar.setTimeInMillis(((Timestamp) value).getTime());
									} else {
										SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
										calendar.setTime(sdf.parse(value.toString()));
									}

									calendar.set(Calendar.HOUR, 0);
									calendar.set(Calendar.MINUTE, 0);
									calendar.set(Calendar.SECOND, 0);

									receptionStartDate = new Timestamp(calendar.getTimeInMillis());

									calendar.set(Calendar.HOUR, 23);
									calendar.set(Calendar.MINUTE, 59);
									calendar.set(Calendar.SECOND, 59);

									receptionEndDate = new Timestamp(calendar.getTimeInMillis());

									sbQuery.append(" AND c." + searchBy + " >= :receptionStartDate");
									sbQuery.append(" AND c." + searchBy + " <= :receptionEndDate");
								} catch (ParseException e) {
								}
							}
						} else if (searchBy.equalsIgnoreCase("applicationNoOnline")) {
							sbQuery.append(" AND c.applicationNo != c.txReception.eFilingNo");
							if (searchCriteria.get(i).isExactMatch()) {
								sbQuery.append(" AND LOWER(c.applicationNo) = :p" + i);
							} else {
								sbQuery.append(" AND LOWER(c.applicationNo) LIKE :p" + i);
							}
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
				} else if (orderBy.equalsIgnoreCase("fillingDate")) {
					sbQuery.append(" ORDER BY c.filingDate " + orderType);
				} else if (orderBy.equalsIgnoreCase("txTmPriorList.priorDate")) {
					sbQuery.append(" ORDER BY ttp.priorDate " + orderType);
				} else if (orderBy.equalsIgnoreCase("txTmOwner.name")) {
					sbQuery.append(" ORDER BY tto.name " + orderType);
				} else if (orderBy.equalsIgnoreCase("txTmReprs.name")) {
					sbQuery.append(" ORDER BY ttr.mRepresentative.name " + orderType);
				}else if (orderBy.equalsIgnoreCase("txTmBrand.name")) {
					sbQuery.append(" ORDER BY LOWER(c.txTmBrand.name) " + orderType);
				} else {
					sbQuery.append(" ORDER BY c." + orderBy + " " + orderType);
				}
			}

			Query query = entityManager.createQuery(sbQuery.toString());
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();
					if (value != null && !(searchBy.equalsIgnoreCase("startDate") || searchBy.equalsIgnoreCase("endDate"))) {
						if (searchBy.equalsIgnoreCase("txReception.applicationDate")) {
							if (receptionStartDate != null) {
								query.setParameter("receptionStartDate", receptionStartDate);
							}
							if (receptionEndDate != null) {
								query.setParameter("receptionEndDate", receptionEndDate);
							}
						} else if( value!=null && searchBy.equalsIgnoreCase("createdBy") && createdBy!=null) {
							query.setParameter("createdBy", createdBy);
							System.err.println("?"+createdBy);
						} else {
							if (value instanceof String) {
								if (searchCriteria.get(i).isExactMatch()) {
									if(searchBy.equalsIgnoreCase("mFileType.id") || searchBy.equalsIgnoreCase("mFileTypeDetail.id")) {
										query.setParameter("p" + i, ((String) value));
									} else {
										query.setParameter("p" + i, ((String) value).toLowerCase());
									}
								} else {
									if ( searchBy.equalsIgnoreCase("txTmBrand.name") ) {
										query.setParameter("p" + i, ((String) value).toLowerCase());
									} else if ( searchBy.equalsIgnoreCase("applicationNo") ) {
										if(((String) value).contains("%")){
											query.setParameter("p" + i, ((String) value).toLowerCase());
										} else {
											query.setParameter("p" + i, "%" + ((String) value).toLowerCase() + "%");}
									} else {
										query.setParameter("p" + i, "%" + ((String) value).toLowerCase() + "%");
									}
								}
							} else {
								query.setParameter("p" + i, value);
							}
						}
					}
				}

				if (startDate != null) {
					query.setParameter("startDate", startDate);
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

	public String processStringKelasDetailSearch(String input){
		return input
				.replaceAll(",","\",\"")
				.replaceAll(" ","\",\"");
	}

	public long countPermohonan(List<KeyValue> searchCriteria) {
		Timestamp startDate = null;
		Timestamp endDate = null;
		Timestamp receptionStartDate = null;
		Timestamp receptionEndDate = null;

		try {
			MUser createdBy = null;
			for(KeyValue sc : searchCriteria){
				String key = sc.getKey();
				if (key.equalsIgnoreCase("createdBy") && sc.getValue()!=null && !sc.getValue().toString().equalsIgnoreCase("")){
					createdBy = userService.getUserByEmail(sc.getValue().toString());
				}
			}
			StringBuffer sbQuery = new StringBuffer("SELECT count(c) FROM " + getClassName() + " c ");
			sbQuery.append("WHERE 1 = 1 ");
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (searchBy.equalsIgnoreCase("createdBy") && createdBy!=null){
							sbQuery.append(" AND c.createdBy = :createdBy");
						} else if (searchBy.equalsIgnoreCase("statusOnOff")) {
							if (searchCriteria.get(i).isExactMatch()) {
								sbQuery.append(" AND (c.mStatus.id NOT LIKE 'IPT_DRAFT' OR c.mStatus.id IS NULL OR LOWER(c.mStatus.id) != :p" + i + ") ");
							} else {
								sbQuery.append(" AND (c.mStatus.id NOT LIKE 'IPT_DRAFT' OR c.mStatus.id IS NULL OR  LOWER(c.mStatus.id) NOT LIKE :p" + i + ") ");
							}
						} else if (searchBy.equalsIgnoreCase("txTmClassList")) {
							sbQuery.append(" AND c.id IN (SELECT ttc.txTmGeneral FROM TxTmClass ttc LEFT JOIN ttc.mClass mc WHERE mc.id = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txTmClassDetailDesc")) {
							sbQuery.append(" AND c.id IN (SELECT ttc.txTmGeneral FROM TxTmClass ttc LEFT JOIN ttc.mClassDetail mcd WHERE LOWER(mcd.desc) LIKE :p" + i + " OR LOWER (mcd.descEn) LIKE :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("mClassDetail")) {
							sbQuery.append(" AND c.id IN (SELECT ttc.txTmGeneral FROM TxTmClass ttc LEFT JOIN ttc.mClassDetail mcd WHERE LOWER(mcd.id) LIKE :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("no")) {
							sbQuery.append(" AND c.id IN (SELECT tr.txTmGeneral FROM TxRegistration tr WHERE tr.no LIKE :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txTmOwnerName")) {
							sbQuery.append(" AND c.id IN (SELECT tto.txTmGeneral FROM TxTmOwner tto WHERE LOWER(tto.name) LIKE :p" + i + " AND tto.status=true)");
						} else if (searchBy.equalsIgnoreCase("txReprsName")) {
							sbQuery.append(" AND c.id IN (SELECT ttr.txTmGeneral FROM TxTmRepresentative ttr LEFT JOIN ttr.mRepresentative mr WHERE LOWER(mr.name) LIKE :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txTmPriorList.priorDate")) {
							sbQuery.append(" AND c.id IN (SELECT ttp.txTmGeneral FROM TxTmPrior ttp WHERE ttp.priorDate = :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("mFileSequence.id")) {
							sbQuery.append(" AND (c.txReception.mFileSequence.id = :p" + i +")");
						}  else if (searchBy.equalsIgnoreCase("mFileType.id")) {
							sbQuery.append(" AND (c.txReception.mFileType.id = :p" + i +")");
						} else if (searchBy.equalsIgnoreCase("mFileTypeDetail.id")) {
							sbQuery.append(" AND (c.txReception.mFileTypeDetail.id = :p" + i +")");
						} else if(searchBy.equalsIgnoreCase("startDate") || searchBy.equalsIgnoreCase("endDate")) {
							if (value != null) {
								try {
									Calendar calendar = Calendar.getInstance();
									if (value instanceof Timestamp) {
										calendar.setTimeInMillis(((Timestamp) value).getTime());
									} else {
										SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
										calendar.setTime(sdf.parse(value.toString()));
									}
									if (searchBy.equalsIgnoreCase("startDate")) {
										calendar.set(Calendar.HOUR, 0);
										calendar.set(Calendar.MINUTE, 0);
										calendar.set(Calendar.SECOND, 0);

										startDate = new Timestamp(calendar.getTimeInMillis());
										sbQuery.append(" AND c.filingDate >= :startDate");
									} else if (searchBy.equalsIgnoreCase("endDate")) {
										calendar.set(Calendar.HOUR, 23);
										calendar.set(Calendar.MINUTE, 59);
										calendar.set(Calendar.SECOND, 59);

										endDate = new Timestamp(calendar.getTimeInMillis());
										sbQuery.append(" AND c.filingDate <= :endDate");
									}
								} catch (ParseException e) {
								}
							}
						} else if (searchBy.equalsIgnoreCase("txReception.applicationDate")) {
							if (value != null) {
								try {
									Calendar calendar = Calendar.getInstance();
									if (value instanceof Timestamp) {
										calendar.setTimeInMillis(((Timestamp) value).getTime());
									} else {
										SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
										calendar.setTime(sdf.parse(value.toString()));
									}

									calendar.set(Calendar.HOUR, 0);
									calendar.set(Calendar.MINUTE, 0);
									calendar.set(Calendar.SECOND, 0);

									receptionStartDate = new Timestamp(calendar.getTimeInMillis());

									calendar.set(Calendar.HOUR, 23);
									calendar.set(Calendar.MINUTE, 59);
									calendar.set(Calendar.SECOND, 59);

									receptionEndDate = new Timestamp(calendar.getTimeInMillis());

									sbQuery.append(" AND c." + searchBy + " >= :receptionStartDate");
									sbQuery.append(" AND c." + searchBy + " <= :receptionEndDate");
								} catch (ParseException e) {
								}
							}
						} else if (searchBy.equalsIgnoreCase("applicationNoOnline")) {
							sbQuery.append(" AND c.applicationNo != c.txReception.eFilingNo");
							if (searchCriteria.get(i).isExactMatch()) {
								sbQuery.append(" AND LOWER(c.applicationNo) = :p" + i);
							} else {
								sbQuery.append(" AND LOWER(c.applicationNo) LIKE :p" + i);
							}
						}  else if (searchBy.equalsIgnoreCase("applicationNo")) {
							String val = (String) value;
							if (val.contains("%") ){
								sbQuery.append(" AND LOWER(c.applicationNo) LIKE :p" + i);
							}else{
								sbQuery.append(" AND LOWER(c.applicationNo) LIKE :p" + i);
							}
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
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();
					if( value!=null && searchBy.equalsIgnoreCase("createdBy") && createdBy!=null) {
						query.setParameter("createdBy", createdBy);
						System.err.println("?"+createdBy);
					}else if (value != null && !(searchBy.equalsIgnoreCase("startDate") || searchBy.equalsIgnoreCase("endDate"))) {
						if (searchBy.equalsIgnoreCase("txReception.applicationDate")) {
							if (receptionStartDate != null) {
								query.setParameter("receptionStartDate", receptionStartDate);
							}
							if (receptionEndDate != null) {
								query.setParameter("receptionEndDate", receptionEndDate);
							}
						}
						else if (searchBy.equalsIgnoreCase("no")) { // di oracle jadi tidak bisa jika di convert to lower case
							query.setParameter("p" + i, "%" + (String) value + "%");
						}
						else {
							if (value instanceof String) {
								if (searchCriteria.get(i).isExactMatch()) {
									if(searchBy.equalsIgnoreCase("mFileType.id") || searchBy.equalsIgnoreCase("mFileTypeDetail.id")) {
										query.setParameter("p" + i, ((String) value));
									} else {
										query.setParameter("p" + i, ((String) value).toLowerCase());
									}
								} else {
									if ( searchBy.equalsIgnoreCase("txTmBrand.name") ) {
										query.setParameter("p" + i, ((String) value).toLowerCase());
									} else if ( searchBy.equalsIgnoreCase("applicationNo") ) {
										query.setParameter("p" + i, "%" + ((String) value).toLowerCase() + "%");
										if(((String) value).contains("%")){
											query.setParameter("p" + i, ((String) value).toLowerCase());
										}else {
											query.setParameter("p" + i, "%" + ((String) value).toLowerCase() + "%");
										}
									} else {
										query.setParameter("p" + i, "%" + ((String) value).toLowerCase() + "%");
									}
								}
							} else {
								query.setParameter("p" + i, value);
							}
						}
					}
				}
			}
			if (startDate != null) {
				query.setParameter("startDate", startDate);
			}
			if (endDate != null) {
				query.setParameter("endDate", endDate);
			}
			return (Long) query.setMaxResults(1).getSingleResult();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
			System.err.println(e.toString());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			System.err.println(e.toString());
		}

		return 0L;
	}

	public long countFromGrupPermohonan(List<KeyValue> searchCriteria, String[] exclude, String[] include) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT COUNT(c) FROM " + getClassName() + " c ");

			sbQuery.append("WHERE 1 = 1 ");
			if (searchCriteria != null) {
				StringBuffer mClass = new StringBuffer();
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (searchBy.equalsIgnoreCase("mClassId")) {
							mClass.append(" AND LOWER(mc.id) LIKE :p" + i);
						} else if (searchBy.equalsIgnoreCase("mClassDetailDesc")) {
							mClass.append(" AND LOWER(mcc.desc) LIKE :p" + i);
						} else if (searchBy.equalsIgnoreCase("startDate")) {
							sbQuery.append(" AND c.filingDate >= :p" + i);
						} else if (searchBy.equalsIgnoreCase("endDate")) {
							sbQuery.append(" AND c.filingDate <= :p" + i);
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
			}

			if(exclude != null) {
				String SQLcmd = "";
				int oracleLimit = 1000;
				for (int i = 0; i <= exclude.length/oracleLimit; i++) {
					if (oracleLimit*i == exclude.length){
						break;
					}
					String excludeList = "";
					for (int j = 0 ; j < (exclude.length>(oracleLimit*(i+1))?oracleLimit:exclude.length); j++) {
						if (j+(oracleLimit*i) >= exclude.length){
							break;
						}
						excludeList += (excludeList.equals("") ? "'" + exclude[j+(oracleLimit*i)] + "'" : ",'" + exclude[j+(oracleLimit*i)] + "'");
					}
					SQLcmd += " AND c.id NOT IN ("+excludeList+")";
				}
				sbQuery.append(SQLcmd);
			}

			if(include != null) {
				String SQLcmd = "";
				int oracleLimit = 1000;
				for (int i = 0; i <= include.length/oracleLimit; i++) {
					if (oracleLimit*i == include.length){
						break;
					}
					String includeList = "";
					for (int j = 0 ; j < (include.length>(oracleLimit*(i+1))?oracleLimit:include.length); j++) {
						if (j+(oracleLimit*i) >= include.length){
							break;
						}
						includeList += (includeList.equals("") ? "'" + include[j+(oracleLimit*i)] + "'" : ",'" + include[j+(oracleLimit*i)] + "'");
					}
					SQLcmd += " OR c.id IN ("+includeList+")";
				}
				sbQuery.append(SQLcmd);
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

	public List<TxTmGeneral> selectAllFromGrupPermohonan(List<KeyValue> searchCriteria, String[] exclude, String[] include, String orderBy, String orderType, Integer offset, Integer limit) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
			sbQuery.append("LEFT JOIN FETCH c.txTmBrand ttb ");
			sbQuery.append("LEFT JOIN FETCH c.txTmClassList ttc ");
			sbQuery.append("LEFT JOIN FETCH ttc.mClass mc ");
			sbQuery.append("LEFT JOIN FETCH c.txTmOwner tto ");
			sbQuery.append("LEFT JOIN FETCH c.mStatus ms ");
			sbQuery.append("LEFT JOIN FETCH c.txTmRepresentative ttr ");
			sbQuery.append("LEFT JOIN FETCH c.txReception tr ");
			sbQuery.append("LEFT JOIN FETCH tr.mFileTypeDetail mftc ");
			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				StringBuffer mClass = new StringBuffer();
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (searchBy.equalsIgnoreCase("mClassId")) {
							mClass.append(" AND LOWER(mc.id) LIKE :p" + i);
						} else if (searchBy.equalsIgnoreCase("mClassDetailDesc")) {
							mClass.append(" AND LOWER(mcc.descEn) LIKE :p" + i);
						} else if (searchBy.equalsIgnoreCase("startDate")) {
							sbQuery.append(" AND c.filingDate >= :p" + i);
						} else if (searchBy.equalsIgnoreCase("endDate")) {
							sbQuery.append(" AND c.filingDate <= :p" + i);
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
			}

			if(exclude != null) {
				String SQLcmd = "";
				int oracleLimit = 1000;
				for (int i = 0; i <= exclude.length/oracleLimit; i++) {
					if (oracleLimit*i == exclude.length){
						break;
					}
					String excludeList = "";
					for (int j = 0 ; j < (exclude.length>(oracleLimit*(i+1))?oracleLimit:exclude.length); j++) {
						if (j+(oracleLimit*i) >= exclude.length){
							break;
						}
						excludeList += (excludeList.equals("") ? "'" + exclude[j+(oracleLimit*i)] + "'" : ",'" + exclude[j+(oracleLimit*i)] + "'");
					}
					SQLcmd += " AND c.id NOT IN ("+excludeList+")";
				}
				sbQuery.append(SQLcmd);
			}

			if(include != null) {
				String SQLcmd = "";
				int oracleLimit = 1000;
				for (int i = 0; i <= include.length/oracleLimit; i++) {
					if (oracleLimit*i == include.length){
						break;
					}
					String includeList = "";
					for (int j = 0 ; j < (include.length>(oracleLimit*(i+1))?oracleLimit:include.length); j++) {
						if (j+(oracleLimit*i) >= include.length){
							break;
						}
						includeList += (includeList.equals("") ? "'" + include[j+(oracleLimit*i)] + "'" : ",'" + include[j+(oracleLimit*i)] + "'");
					}
					SQLcmd += " OR c.id IN ("+includeList+")";
				}
				sbQuery.append(SQLcmd);
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

	public long countFromGrupPermohonan(List<KeyValue> searchCriteria, String[] excludeStatus, String[] exclude, String[] include) {
		Timestamp p1 = null;
		Timestamp p2 = null;
		Timestamp tStart1 = null;
		Timestamp tStart2 = null;
		Timestamp tEnd1 = null;
		Timestamp tEnd2 = null;
		boolean bstartDate=false;
		boolean bendDate=false;

		try {
			StringBuffer sbQuery = new StringBuffer("SELECT COUNT(c) FROM " + getClassName() + " c ");
			//sbQuery.append("LEFT JOIN TxTmClass ttc ");
			//sbQuery.append("LEFT JOIN MClass mc ");

			sbQuery.append("WHERE 1 = 1 ");
			if (searchCriteria != null) {
				StringBuffer mClass = new StringBuffer();
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (searchBy.equalsIgnoreCase("mClassId")) {
							mClass.append(" AND mc.id = :p" + i);
						} else if (searchBy.equalsIgnoreCase("mClassDetailDesc")) {
							mClass.append(" AND LOWER(mcc.desc) LIKE :p" + i);
						} else if (searchBy.equalsIgnoreCase("mReprsName")) {
							sbQuery.append(" AND c.id IN (SELECT ttr.txTmGeneral FROM TxTmRepresentative ttr LEFT JOIN ttr.mRepresentative mr WHERE 1 = 1 AND ttr.status = true AND LOWER(mr.name) LIKE :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txTmOwnerName")) {
							sbQuery.append(" AND c.id IN (SELECT tto.txTmGeneral FROM TxTmOwner tto WHERE LOWER(tto.name) LIKE :p" + i + " AND tto.status=true)");
						} else if (searchBy.equalsIgnoreCase("startDate") || searchBy.equalsIgnoreCase("endDate")) {
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
							if(searchBy.equalsIgnoreCase("startDate") ) {
								bstartDate = true;
								tStart1 = p1;
								tStart2 = p2;
							}
							else if(searchBy.equalsIgnoreCase("endDate") ) {
								bendDate = true;
								tEnd1 = p1;
								tEnd2 = p2;
							}

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

				int i=searchCriteria.size() + 1;
				if(bstartDate && bendDate ) {
					sbQuery.append(" AND c.filingDate >=:p" + i );
					i++;
					sbQuery.append(" AND c.filingDate <=:p" + i );
				} else if(bstartDate) {
					sbQuery.append(" AND c.filingDate >=:p" + i );
					i++;
					sbQuery.append(" AND c.filingDate <=:p" + i );
				}else if(bendDate) {
					sbQuery.append(" AND c.filingDate >=:p" + i );
					i++;
					sbQuery.append(" AND c.filingDate <=:p" + i );
				}

				if(mClass.length() > 0) {
					sbQuery.append(" AND c.id IN (SELECT ttc.txTmGeneral FROM TxTmClass ttc LEFT JOIN ttc.mClass mc LEFT JOIN ttc.mClassDetail mcc WHERE 1 = 1 " + mClass.toString() + ")");
				}
			}

			if(excludeStatus != null) {
				String SQLcmd = "";
				int oracleLimit = 1000;
				for (int i = 0; i <= excludeStatus.length/oracleLimit; i++) {
					if (oracleLimit*i == excludeStatus.length){
						break;
					}
					String excludeStatusList = "";
					for (int j = 0 ; j < (excludeStatus.length>(oracleLimit*(i+1))?oracleLimit:excludeStatus.length); j++) {
						if (j+(oracleLimit*i) >= excludeStatus.length){
							break;
						}
						excludeStatusList += (excludeStatusList.equals("") ? "'" + excludeStatus[j+(oracleLimit*i)] + "'" : ",'" + excludeStatus[j+(oracleLimit*i)] + "'");
					}
					SQLcmd += " AND c.mStatus.id NOT IN ("+excludeStatusList+")";
				}
				sbQuery.append(SQLcmd);
			}

			if(exclude != null) {
				String SQLcmd = "";
				int oracleLimit = 1000;
				for (int i = 0; i <= exclude.length/oracleLimit; i++) {
					if (oracleLimit*i == exclude.length){
						break;
					}
					String excludeList = "";
					for (int j = 0 ; j < (exclude.length>(oracleLimit*(i+1))?oracleLimit:exclude.length); j++) {
						if (j+(oracleLimit*i) >= exclude.length){
							break;
						}
						excludeList += (excludeList.equals("") ? "'" + exclude[j+(oracleLimit*i)] + "'" : ",'" + exclude[j+(oracleLimit*i)] + "'");
					}
					SQLcmd += " AND c.id NOT IN ("+excludeList+")";
				}
				sbQuery.append(SQLcmd);
			}

			if(include != null) {
				String SQLcmd = "";
				int oracleLimit = 1000;
				for (int i = 0; i <= include.length/oracleLimit; i++) {
					if (oracleLimit*i == include.length){
						break;
					}
					String includeList = "";
					for (int j = 0 ; j < (include.length>(oracleLimit*(i+1))?oracleLimit:include.length); j++) {
						if (j+(oracleLimit*i) >= include.length){
							break;
						}
						includeList += (includeList.equals("") ? "'" + include[j+(oracleLimit*i)] + "'" : ",'" + include[j+(oracleLimit*i)] + "'");
					}
					SQLcmd += " OR c.id IN ("+includeList+")";
				}
				sbQuery.append(SQLcmd);
			}

			Query query = entityManager.createQuery(sbQuery.toString());
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					Object value = searchCriteria.get(i).getValue();
					String searchBy = searchCriteria.get(i).getKey();
					if (value != null && !(searchBy.equalsIgnoreCase("startDate") || searchBy.equalsIgnoreCase("endDate"))) {
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

				int i=searchCriteria.size() + 1;

				if(bstartDate && bendDate ) {
					query.setParameter("p" + i, tStart1);
					i++;
					query.setParameter("p" + i, tEnd2);
				} else if(bstartDate) {
					query.setParameter("p" + i, tStart1);
					i++;
					query.setParameter("p" + i, tStart2);
				}else if(bendDate) {
					query.setParameter("p" + i, tEnd1);
					i++;
					query.setParameter("p" + i, tEnd2);
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

	public List<TxTmGeneral> selectAllFromGrupPermohonan(List<KeyValue> searchCriteria, String[] excludeStatus, String[] exclude, String[] include, String orderBy, String orderType, Integer offset, Integer limit) {
		Timestamp p1 = null;
		Timestamp p2 = null;
		Timestamp tStart1 = null;
		Timestamp tStart2 = null;
		Timestamp tEnd1 = null;
		Timestamp tEnd2 = null;
		boolean bstartDate=false;
		boolean bendDate=false;
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
			if(searchCriteria.get(0).getKey().toString().equalsIgnoreCase("groupJurnal") &&
					searchCriteria.get(0).getValue().toString().equalsIgnoreCase("false")){
				sbQuery.append("LEFT JOIN FETCH c.txTmBrand ttb ");
				sbQuery.append("LEFT JOIN FETCH c.txTmClassList ttc ");
				sbQuery.append("LEFT JOIN FETCH ttc.mClass mc ");
				sbQuery.append("LEFT JOIN FETCH c.txTmOwner tto ");
				sbQuery.append("LEFT JOIN FETCH c.mStatus ms ");
				sbQuery.append("LEFT JOIN FETCH c.txTmRepresentative ttr ");
				sbQuery.append("LEFT JOIN FETCH c.txReception tr ");
				sbQuery.append("LEFT JOIN FETCH tr.mFileTypeDetail mftc ");
			}

			if(orderBy.equalsIgnoreCase("txTmOwner.name")) {
				sbQuery.append("LEFT JOIN c.txTmOwner tto ");
			} else if (orderBy.equalsIgnoreCase("txTmRepresentative.mRepresentative.name")) {
				sbQuery.append("LEFT JOIN c.txTmRepresentative ttr ");
			}

			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				StringBuffer mClass = new StringBuffer();
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (searchBy.equalsIgnoreCase("mClassId")) {
							mClass.append(" AND mc.id = :p" + i);
						} else if (searchBy.equalsIgnoreCase("mClassDetailDesc")) {
							mClass.append(" AND LOWER(mcc.descEn) LIKE :p" + i);
						} else if (searchBy.equalsIgnoreCase("mReprsName")) {
							sbQuery.append(" AND c.id IN (SELECT ttr.txTmGeneral FROM TxTmRepresentative ttr LEFT JOIN ttr.mRepresentative mr WHERE 1 = 1 AND ttr.status = true AND LOWER(mr.name) LIKE :p" + i + ")");
						} else if (searchBy.equalsIgnoreCase("txTmOwnerName")) {
							sbQuery.append(" AND c.id IN (SELECT tto.txTmGeneral FROM TxTmOwner tto WHERE LOWER(tto.name) LIKE :p" + i + " AND tto.status=true)");
						} else if (searchBy.equalsIgnoreCase("startDate") || searchBy.equalsIgnoreCase("endDate")) {
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
							if(searchBy.equalsIgnoreCase("startDate") ) {
								bstartDate = true;
								tStart1 = p1;
								tStart2 = p2;
							}
							else if(searchBy.equalsIgnoreCase("endDate") ) {
								bendDate = true;
								tEnd1 = p1;
								tEnd2 = p2;
							}

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

				int i=searchCriteria.size() + 1;
				if(bstartDate && bendDate ) {
					sbQuery.append(" AND c.filingDate >=:p" + i );
					i++;
					sbQuery.append(" AND c.filingDate <=:p" + i );
				} else if(bstartDate) {
					sbQuery.append(" AND c.filingDate >=:p" + i );
					i++;
					sbQuery.append(" AND c.filingDate <=:p" + i );
				}else if(bendDate) {
					sbQuery.append(" AND c.filingDate >=:p" + i );
					i++;
					sbQuery.append(" AND c.filingDate <=:p" + i );
				}

				if(mClass.length() > 0) {
					sbQuery.append(" AND c.id IN (SELECT ttc.txTmGeneral FROM TxTmClass ttc LEFT JOIN ttc.mClass mc LEFT JOIN ttc.mClassDetail mcc WHERE 1 = 1 " + mClass.toString() + ")");
				}
			}

			if(excludeStatus != null) {
				String SQLcmd = "";
				int oracleLimit = 1000;
				for (int i = 0; i <= excludeStatus.length/oracleLimit; i++) {
					if (oracleLimit*i == excludeStatus.length){
						break;
					}
					String excludeStatusList = "";
					for (int j = 0 ; j < (excludeStatus.length>(oracleLimit*(i+1))?oracleLimit:excludeStatus.length); j++) {
						if (j+(oracleLimit*i) >= excludeStatus.length){
							break;
						}
						excludeStatusList += (excludeStatusList.equals("") ? "'" + excludeStatus[j+(oracleLimit*i)] + "'" : ",'" + excludeStatus[j+(oracleLimit*i)] + "'");
					}
					SQLcmd += " AND c.mStatus.id NOT IN ("+excludeStatusList+")";
				}
				sbQuery.append(SQLcmd);
			}

			if(exclude != null) {
				String SQLcmd = "";
				int oracleLimit = 1000;
				for (int i = 0; i <= exclude.length/oracleLimit; i++) {
					if (oracleLimit*i == exclude.length){
						break;
					}
					String excludeList = "";
					for (int j = 0 ; j < (exclude.length>(oracleLimit*(i+1))?oracleLimit:exclude.length); j++) {
						if (j+(oracleLimit*i) >= exclude.length){
							break;
						}
						excludeList += (excludeList.equals("") ? "'" + exclude[j+(oracleLimit*i)] + "'" : ",'" + exclude[j+(oracleLimit*i)] + "'");
					}
					SQLcmd += " AND c.id NOT IN ("+excludeList+")";
				}
				sbQuery.append(SQLcmd);
			}

			if(include != null) {
				String SQLcmd = "";
				int oracleLimit = 1000;
				for (int i = 0; i <= include.length/oracleLimit; i++) {
					if (oracleLimit*i == include.length){
						break;
					}
					String includeList = "";
					for (int j = 0 ; j < (include.length>(oracleLimit*(i+1))?oracleLimit:include.length); j++) {
						if (j+(oracleLimit*i) >= include.length){
							break;
						}
						includeList += (includeList.equals("") ? "'" + include[j+(oracleLimit*i)] + "'" : ",'" + include[j+(oracleLimit*i)] + "'");
					}
					SQLcmd += " OR c.id IN ("+includeList+")";
				}
				sbQuery.append(SQLcmd);
			}

			if (orderBy != null && orderType != null) {
				if (orderBy.equalsIgnoreCase("txTmOwner.name")) {
					sbQuery.append(" ORDER BY tto.name " + orderType);
				} else if (orderBy.equalsIgnoreCase("txTmRepresentative.mRepresentative.name")) {
					sbQuery.append(" ORDER BY ttr.mRepresentative.name " + orderType);
				} else {
					sbQuery.append(" ORDER BY c." + orderBy + " " + orderType);
				}
				/*if (orderBy.equalsIgnoreCase("ownerName")) {
					sbQuery.append(" ORDER BY c.ownerList " + orderType);
				} else if (orderBy.equalsIgnoreCase("reprsName")) {
					sbQuery.append(" ORDER BY c.represList " + orderType);
				} else {
					sbQuery.append(" ORDER BY c." + orderBy + " " + orderType);
				}*/
			}

			Query query = entityManager.createQuery(sbQuery.toString());
			if (searchCriteria != null) {

				for (int i = 0; i < searchCriteria.size(); i++) {
					Object value = searchCriteria.get(i).getValue();
					String searchBy = searchCriteria.get(i).getKey();
					if (value != null && !(searchBy.equalsIgnoreCase("startDate") || searchBy.equalsIgnoreCase("endDate"))) {
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

				int i=searchCriteria.size() + 1;
				if(bstartDate && bendDate ) {
					query.setParameter("p" + i, tStart1);
					i++;
					query.setParameter("p" + i, tEnd2);
				} else if(bstartDate) {
					query.setParameter("p" + i, tStart1);
					i++;
					query.setParameter("p" + i, tStart2);
				}else if(bendDate) {
					query.setParameter("p" + i, tEnd1);
					i++;
					query.setParameter("p" + i, tEnd2);
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

	public TxTmGeneral selectOneImportClass(String by, Object value) {
		return super.selectOne("LEFT JOIN FETCH c.txTmClassList ttc "
				+ "LEFT JOIN FETCH ttc.mClass mc "
				+ "LEFT JOIN FETCH ttc.mClassDetail mcd "
				+ " ", by, value, true);
	}

	public TxTmGeneral selectOne(String by, Object value) {
		return super.selectRealOne("LEFT JOIN FETCH c.createdBy cb "
				+ "LEFT JOIN FETCH c.txReception tr "
				+ " ", by, value, true);
	}

	public TxTmGeneral selectOne(List<KeyValue> criteriaList) {
		return super.selectRealOne("LEFT JOIN FETCH c.createdBy cb "
				+ "LEFT JOIN FETCH c.txReception tr "
				+ " ", criteriaList, "id", "ASC");
	}


	public TxTmGeneral selectOneGeneralBrand(String by, Object value) {
		return super.selectOne("LEFT JOIN FETCH c.createdBy cb "
				+ "LEFT JOIN FETCH c.txTmBrand ttb "
				+ " ", by, value, true);
	}

	public TxTmGeneral select(String by, Object value) {
		return super.selectOne(null, by, value, true);
	}

	public TxTmGeneral selectOneLike(String by, Object value) {
		return super.selectOne(null, by, value, false);
	}

	public TxTmGeneral selectOneByTxMadridDocId(String by, Object value) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");

			sbQuery.append(" JOIN TX_MADRID b ON SUBSTR(c.applicationNo,8,LENGTH(c.applicationNo)-7) = b.intregn");
			sbQuery.append(" ");

			sbQuery.append(" WHERE 1 = 1 ");
			sbQuery.append(" AND SUBSTR(c.applicationNo,1,1) = 'M'");

			Query query = entityManager.createQuery(sbQuery.toString());

			return (TxTmGeneral)query.getResultList().get(0);
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	public TxTmGeneral selectOneGeneralRegistration(String by, Object value) {
		return super.selectOne("LEFT JOIN FETCH c.createdBy cb "
				+ "LEFT JOIN FETCH c.txTmClassList ttc "
				+ "LEFT JOIN FETCH ttc.mClass mc "
				+ "LEFT JOIN FETCH ttc.mClassDetail mcd "
				+ "LEFT JOIN FETCH c.mStatus ms "
				+ "LEFT JOIN FETCH c.txTmBrand ttb "
				+ "LEFT JOIN FETCH c.txRegistration tx "
				+ " ", by, value, true);
	}

	public List<TxTmGeneral> selectAllGeneralRegistration(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c ");
			sbQuery.append("LEFT JOIN FETCH c.txTmBrand ttb ");
			sbQuery.append("LEFT JOIN FETCH c.txRegistration txr ");

			sbQuery.append("WHERE 1 = 1");
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();

					if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
						if (searchBy.equalsIgnoreCase("no")) {
							sbQuery.append(" AND txr.no LIKE :p" + i);
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
				sbQuery.append(" ORDER BY c." + orderBy + " " + orderType);
			}

			Query query = entityManager.createQuery(sbQuery.toString());
			if (searchCriteria != null) {
				for (int i = 0; i < searchCriteria.size(); i++) {
					String searchBy = searchCriteria.get(i).getKey();
					Object value = searchCriteria.get(i).getValue();
					if (searchBy.equalsIgnoreCase("no")) {
						query.setParameter("p" + i, "%" + ((String) value) + "%"); //di oracle convert menjadi lower case (huruf kecil) jadi tidak bisa dibaca
					} else {
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

	public void updateGroupJournalFlagTxTmGeneralByBatch(boolean groupJournal, String[] generalId) {
		int oracleLimit = 1000;
		String SQLcmd = "UPDATE TX_TM_GENERAL SET GROUP_JOURNAL = :groupJournal WHERE ";
		for (int i = 0; i <= generalId.length/oracleLimit; i++) {
			if (oracleLimit*i == generalId.length){
				break;
			}
			String newWhereClause = (i==0?"":" OR ")+"APPLICATION_ID IN (";
			for (int j = 0 ; j < (generalId.length>(oracleLimit*(i+1))?oracleLimit:generalId.length); j++) {
				if (j+(oracleLimit*i) >= generalId.length){
					break;
				}
				newWhereClause += (j==0?"":",")+ "'" +generalId[j+(oracleLimit*i)] + "'";
			}
			SQLcmd += newWhereClause+")";
		}
		Query query = entityManager.createNativeQuery(SQLcmd)
				.setParameter("groupJournal", groupJournal);
		query.executeUpdate();
	}

	public void updateGroupDistFlagTxTmGeneralByBatch(boolean groupDist, String[] generalId) {
		int oracleLimit = 1000;
		String SQLcmd = "UPDATE TX_TM_GENERAL SET GROUP_DIST = :groupDist WHERE ";
		for (int i = 0; i <= generalId.length/oracleLimit; i++) {
			if (oracleLimit*i == generalId.length){
				break;
			}
			String newWhereClause = (i==0?"":" OR ")+"APPLICATION_ID IN (";
			for (int j = 0 ; j < (generalId.length>(oracleLimit*(i+1))?oracleLimit:generalId.length); j++) {
				if (j+(oracleLimit*i) >= generalId.length){
					break;
				}
				newWhereClause += (j==0?"":",")+ "'" +generalId[j+(oracleLimit*i)] + "'";
			}
			SQLcmd += newWhereClause+")";
		}
		Query query = entityManager.createNativeQuery(SQLcmd)
				.setParameter("groupDist", groupDist);
		query.executeUpdate();
	}

	public void updateTxTmGeneralByBatch(MStatus status, String[] generalId) {
		int oracleLimit = 1000;
		String SQLcmd = "UPDATE TX_TM_GENERAL SET STATUS_ID = :status, ACTION_ID='164' WHERE ";
		for (int i = 0; i <= generalId.length/oracleLimit; i++) {
			if (oracleLimit*i == generalId.length){
				break;
			}
			String newWhereClause = (i==0?"":" OR ")+"APPLICATION_ID IN (";
			for (int j = 0 ; j < (generalId.length>(oracleLimit*(i+1))?oracleLimit:generalId.length); j++) {
				if (j+(oracleLimit*i) >= generalId.length){
					break;
				}
				newWhereClause += (j==0?"":",")+ "'" +generalId[j+(oracleLimit*i)] + "'";
			}
			SQLcmd += newWhereClause+")";
		}
		Query query = entityManager.createNativeQuery(SQLcmd)
				.setParameter("status", status);
		query.executeUpdate();
	}

	public void updateStatusTxTmGeneralByBatch(List<TxTmGeneral> listGeneral) {
		for(TxTmGeneral tg : listGeneral) {
			List<TxMonitor> txMonitorList = txMonitorRepository.findTxMonitorByTxTmGeneralOrderByCreatedDateDesc(tg);
			TxMonitor txMonitor = txMonitorList.get(0);

			String queryNative = "UPDATE TX_TM_GENERAL SET STATUS_ID = :status WHERE APPLICATION_ID = '"+ tg.getId() +"'";
			Query query = entityManager.createNativeQuery(queryNative)
					.setParameter("status", txMonitor.getmWorkflowProcess().getStatus().getId());
			query.executeUpdate();
		}
	}

	public void updateActionTxTmGeneralByBatch(List<TxTmGeneral> listGeneral) {
		for(TxTmGeneral tg : listGeneral) {
			List<TxMonitor> txMonitorList = txMonitorRepository.findTxMonitorByTxTmGeneralOrderByCreatedDateDesc(tg);
			TxMonitor txMonitor = txMonitorList.get(0);

			String queryNative = "UPDATE TX_TM_GENERAL SET ACTION_ID = :action WHERE APPLICATION_ID = '"+ tg.getId() +"'";
			Query query = entityManager.createNativeQuery(queryNative)
					.setParameter("action", txMonitor.getmWorkflowProcessActions().getAction() == null ? null : txMonitor.getmWorkflowProcessActions().getAction().getId());
			query.executeUpdate();
		}
	}

	public void updateActionTxTmGeneralByBatchNull(List<TxTmGeneral> listGeneral) {
		for(TxTmGeneral tg : listGeneral) {
			String queryNative = "UPDATE TX_TM_GENERAL SET ACTION_ID = :action WHERE APPLICATION_ID = '"+ tg.getId() +"'";
			Query query = entityManager.createNativeQuery(queryNative)
					.setParameter("action", null);
			query.executeUpdate();
		}
	}

	public void updateTxTmGeneralClassList(String appID){
		String queryNative = "SELECT f.CLASS_NO FROM TX_TM_CLASS e LEFT JOIN M_CLASS f ON e.CLASS_ID = f.CLASS_ID WHERE e.APPLICATION_ID = :p0 GROUP BY f.CLASS_NO";
		List<BigDecimal> classNos = entityManager.createNativeQuery(queryNative)
				.setParameter("p0", appID)
				.getResultList();
		String class_list = "";
		for(BigDecimal res : classNos){
			if(class_list==""){
				class_list += res;
			}else {
				class_list += res + ",";
			}
		}

		// update ke txtmgeneral
		queryNative = "UPDATE TX_TM_GENERAL SET CLASS_LIST = :p0 WHERE APPLICATION_ID = :p1";
		entityManager.createNativeQuery(queryNative).setParameter("p0",class_list).setParameter("p1",appID).executeUpdate();
	}

	public Long  countKelas(TxTmGeneral txTmGeneral) {
		return (Long) entityManager.createQuery("SELECT count(distinct c.mClass.no) FROM TxTmClass c WHERE c.txTmGeneral.id = :p0 AND c.transactionStatus='ACCEPT'")
				.setParameter("p0", txTmGeneral.getId())
				.getSingleResult();
	}

	// fix bug1: oracle limitation (: maximum number of expressions in a list is 1000 :)
	// limiting the expression in-clause list to 1000 each
	// extending the WHERE expression with OR
	// example:
	//    SELECT APPLICATION_ID FROM TX_TM_GENERAL WHERE
	//       APPLICATION_ID IN ( ‘0’ , … , ‘999’)
	//       OR APPLICATION_ID IN ( ‘1000’ , … , ‘1999’)
	//       OR APPLICATION_ID IN ( ‘2000’ , … , ‘2999’) …
	public String[] findAllTxTmGeneralByNative( String[] appId)
	{
		int oracleLimit = 1000;
		String SQLcmd = "SELECT APPLICATION_ID FROM TX_TM_GENERAL WHERE ";
		for (int i = 0; i <= appId.length/oracleLimit; i++) {
			if (oracleLimit*i == appId.length){
				break;
			}
			String newWhereClause = (i==0?"":" OR ")+"APPLICATION_ID IN (";
			for (int j = 0 ; j < (appId.length>(oracleLimit*(i+1))?oracleLimit:appId.length); j++) {
				if (j+(oracleLimit*i) >= appId.length){
					break;
				}
				newWhereClause += (j==0?"":",")+ "'" +appId[j+(oracleLimit*i)] + "'";
			}
			SQLcmd += newWhereClause+")";
		}
		Query query = entityManager.createNativeQuery(SQLcmd);
		List<String> resultList = query.getResultList();
		return resultList.toArray(new String[resultList.size()]);
	}

	public String[] findAllTxTmGeneralByTxGroupDetailIdNative( String[] groupDetailId)
	{
		if (groupDetailId.length == 0){
			return null ;
		}
		int oracleLimit = 1000;
		String SQLcmd = "SELECT tg.APPLICATION_ID FROM TX_TM_GENERAL tg LEFT JOIN TX_GROUP_DETAIL tgd ON tg.APPLICATION_ID = tgd.APPLICATION_ID WHERE ";
		for (int i = 0; i <= groupDetailId.length/oracleLimit; i++) {
			if (oracleLimit*i == groupDetailId.length){
				break;
			}
			String newWhereClause = (i==0?"":" OR ")+"tgd.GROUP_DETAIL_ID IN (";
			for (int j = 0 ; j < (groupDetailId.length>(oracleLimit*(i+1))?oracleLimit:groupDetailId.length); j++) {
				if (j+(oracleLimit*i) >= groupDetailId.length){
					break;
				}
				newWhereClause += (j==0?"":",")+ "'" +groupDetailId[j+(oracleLimit*i)] + "'";
			}
			SQLcmd += newWhereClause+")";
		}
		//System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		//System.out.println("Jumlah item: "+groupDetailId.length);
		//System.out.println(SQLcmd);
		//System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		Query query = entityManager.createNativeQuery(SQLcmd);
		List<String> resultList = query.getResultList();
		return resultList.toArray(new String[resultList.size()]);
	}

	public String[] findAllTxTmGeneralByTxGroupDetailIdAndStatusNative( String[] groupDetailId, MStatus status)
	{
		int oracleLimit = 1000;
		String SQLcmd = "SELECT tg.APPLICATION_ID FROM TX_TM_GENERAL tg LEFT JOIN TX_GROUP_DETAIL tgd ON tg.APPLICATION_ID = tgd.APPLICATION_ID WHERE tg.STATUS_ID = :status AND ";
		String newWhereClause = "";
		for (int i = 0; i <= groupDetailId.length/oracleLimit; i++) {
			if (oracleLimit*i == groupDetailId.length){
				break;
			}
			String tempClause = (i==0?"":" OR ")+"tgd.GROUP_DETAIL_ID IN (";
			for (int j = 0 ; j < (groupDetailId.length>(oracleLimit*(i+1))?oracleLimit:groupDetailId.length); j++) {
				if (j+(oracleLimit*i) >= groupDetailId.length){
					break;
				}
				tempClause += (j==0?"":",")+ "'" +groupDetailId[j+(oracleLimit*i)] + "'";
			}
			newWhereClause += tempClause+")";
		}
		if(newWhereClause.contains("OR")){
			newWhereClause = "(" + newWhereClause + ")";
		}
		Query query = entityManager.createNativeQuery(SQLcmd+newWhereClause)
				.setParameter("status", status);
		List<String> resultList = query.getResultList();
		return resultList.toArray(new String[resultList.size()]);
	}

	public List<TxTmGeneral>  findAllTxTmGeneralByTxGroupDetailIdAndStatus( String[] groupDetailId, MStatus status)
	{
		int oracleLimit = 1000;
		String SQLcmd = "SELECT tg FROM TxTmGeneral tg LEFT JOIN FETCH tg.txGroupDetail tgd LEFT JOIN FETCH tg.txRegistration tr LEFT JOIN FETCH tg.txTmBrand tb WHERE tg.mStatus = :status AND ";
		String newWhereClause = "";
		for (int i = 0; i <= groupDetailId.length/oracleLimit; i++) {
			if (oracleLimit*i == groupDetailId.length){
				break;
			}
			String tempClause = (i==0?"":" OR ")+"tgd.id IN (";
			for (int j = 0 ; j < (groupDetailId.length>(oracleLimit*(i+1))?oracleLimit:groupDetailId.length); j++) {
				if (j+(oracleLimit*i) >= groupDetailId.length){
					break;
				}
				tempClause += (j==0?"":",")+ "'" +groupDetailId[j+(oracleLimit*i)] + "'";
			}
			newWhereClause += tempClause+")";
		}
		if(newWhereClause.contains("OR")){
			newWhereClause = "(" + newWhereClause + ")";
		}
		Query query = entityManager.createQuery(SQLcmd+newWhereClause)
				.setParameter("status", status);
		return query.getResultList();
	}


	public long countPub(List<KeyValue> searchCriteria) {
		try {
			StringBuffer sbQuery = new StringBuffer("SELECT COUNT(ttg) from TxPubsJournal c ");
            sbQuery.append("JOIN c.txGroup tg ");
            sbQuery.append("JOIN tg.txGroupDetailList tgd ");
            sbQuery.append("JOIN tgd.txTmGeneral ttg ");
            sbQuery.append("WHERE 1 = 1 ");
            sbQuery.append("AND TRUNC(c.journalEnd) >= '"+new SimpleDateFormat("dd MMMM yyyy").format(new Date())+"'");
            if (searchCriteria != null) {
                StringBuffer addCriteria = new StringBuffer("");
                for (int i = 0; i < searchCriteria.size(); i++) {
                    String searchBy = searchCriteria.get(i).getKey();
                    Object value = searchCriteria.get(i).getValue();
                    if ( value!= null) {
                        if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                            if (i == 0) {
                                addCriteria.append(" LOWER(ttg." + searchBy + ") LIKE :p" + i);
                            } else if (searchBy.equals("journalNo")) {
                                addCriteria.append(" OR LOWER(c." + searchBy + ") LIKE :p" + i);
                            } else {
                                addCriteria.append(" OR LOWER(ttg." + searchBy + ") LIKE :p" + i);
                            }
                        }
                    }
                }
                if(!addCriteria.toString().equals("")) {
                    sbQuery.append(" AND ( "+addCriteria+") ");
                }
            }
			System.err.println(sbQuery.toString());
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
							query.setParameter("p" + i, ((String) value).toLowerCase());
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

		return 1L;
	}

	public GenericSearchWrapper<TxTmGeneral> selectAllPub(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<TxTmGeneral> searchResult = new GenericSearchWrapper<TxTmGeneral>();
		long Count = countPub(searchCriteria);
		System.out.println(Count);
		if(Count == 0){
			searchResult.setCount(0);
			return searchResult;
		}
		else {
			searchResult.setCount(Count);
		}
		try {
            StringBuffer sbQuery = new StringBuffer("SELECT ttg from TxPubsJournal c ");
            sbQuery.append("JOIN c.txGroup tg ");
            sbQuery.append("JOIN tg.txGroupDetailList tgd ");
            sbQuery.append("JOIN tgd.txTmGeneral ttg ");
            sbQuery.append("WHERE 1 = 1 ");
            sbQuery.append("AND TRUNC(c.journalEnd) >= '"+new SimpleDateFormat("dd MMMM yyyy").format(new Date())+"'");

            if (searchCriteria != null) {
                StringBuffer addCriteria = new StringBuffer("");
                for (int i = 0; i < searchCriteria.size(); i++) {
                    String searchBy = searchCriteria.get(i).getKey();
                    Object value = searchCriteria.get(i).getValue();
                    if ( value!= null) {
                        if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                            if (i == 0) {
                                addCriteria.append(" LOWER(ttg." + searchBy + ") LIKE :p" + i);
                            } else if (searchBy.equals("journalNo")) {
                                addCriteria.append(" OR LOWER(c." + searchBy + ") LIKE :p" + i);
                            } else {
                                addCriteria.append(" OR LOWER(ttg." + searchBy + ") LIKE :p" + i);
                            }
                        }
                    }
                }
                if(!addCriteria.toString().equals("")) {
                    sbQuery.append(" AND ( "+addCriteria+") ");
                }
            }
			System.err.println(sbQuery.toString());
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
							query.setParameter("p" + i, ((String) value).toLowerCase());
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
			searchResult.setList(query.getResultList());
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
		return searchResult;
	}
}
