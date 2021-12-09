package com.docotel.ki.repository.custom.transaction;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.transaction.TxTmClass;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.pojo.KeyValue;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

@Repository
public class TxTmClassCustomRepository extends BaseRepository<TxTmClass> {

    @Value("${table.migrasi.class.name:}")
    private String tableMigrasiName;

    //	add by deni
    public TxTmClass selectOne(String by, Object value) {
        return super.selectOne("LEFT JOIN FETCH c.createdBy cb "
                + "LEFT JOIN FETCH c.txTmGeneral tg "
                + " ", by, value, false);
    }
// end deni

    public TxTmClass selectOneByAppId(String appId) {
        try {
            return (TxTmClass) entityManager.createQuery(
                    "SELECT c FROM " + getClassName() + " c " +
                            "LEFT JOIN FETCH c.createdBy cb " +
                            "LEFT JOIN FETCH c.txTmGeneral tg " +
                            "WHERE c.txTmGeneral.id = :p1 AND c.transactionStatus != 'REJECT'")
                    .setParameter("p1", appId)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException e) {
//            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    public List<TxTmClass> selectAllTxTmClass(String joinCriteria, List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        return super.selectAll(joinCriteria, searchCriteria, orderBy, orderType, offset, limit);
    }

    public List<TxTmClass> selectAllClassByGeneralId(String generalId, String status) {
        try {
            StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c "
                    + "INNER JOIN FETCH c.txTmGeneral tg "
                    + "LEFT JOIN FETCH c.mClass mc "
                    + "LEFT JOIN FETCH c.mClassDetail mcd ");
            sbQuery.append(" WHERE c.txTmGeneral.id = :p1");
            sbQuery.append(" AND c.transactionStatus = :p2");
            sbQuery.append(" ORDER BY mc.no, mcd.desc");

            Query query = entityManager.createQuery(sbQuery.toString());
            query.setParameter("p1", generalId);
            query.setParameter("p2", status);

            return query.getResultList();
        } catch (NoResultException e) {
//            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }


    public int countAllExclude(List<KeyValue> searchCriteria, String[] exclude) {
        try {
            String appID = "";
            String DESC = "";
            String DESCEN = "";
            if (searchCriteria != null) {
                for (int i = 0; i < searchCriteria.size(); i++) {
                    String searchBy = searchCriteria.get(i).getKey();
                    Object value = searchCriteria.get(i).getValue();
                    if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                        if(searchBy.contains("txTmGeneral")){
                            appID = value.toString();
                        }
                        else if(searchBy.equalsIgnoreCase("DESC")){
                            DESC = value.toString();
                        }
                        else if(searchBy.equalsIgnoreCase("DESCEN")){
                            DESCEN = value.toString();
                        }
                    }
                }
            }

            String nativeQuery = "SELECT COUNT(*) FROM TX_TM_CLASS a, M_CLASS_DETAIL b " +
                    " WHERE a.CLASS_DETAIL_ID = b.CLASS_DETAIL_ID " +
                    " AND a.APPLICATION_ID = :app_id";
            if (DESC.length()>0)
                nativeQuery += " AND b.CLASS_DESC like '%"+DESC+"%' ";
            if (DESCEN.length()>0)
                nativeQuery += " AND b.CLASS_DESC_EN like '%"+DESCEN+"%' ";

            if(exclude != null) {
                // ORACLE 1000 limit!
                int oracleLimit = 1000;
                String excludeList = " AND (";
                for (int oID = 0; oID <= exclude.length/oracleLimit; oID++) {
                    if (oracleLimit * oID == exclude.length) {
                        break;
                    }
                    String newWhereClause = (oID==0?"":" OR ")+" b.CLASS_DETAIL_ID NOT IN (";
                    for (int k = 0; k < (exclude.length>(oracleLimit*(oID+1))?oracleLimit:exclude.length); k++) {
                        if (k+(oracleLimit*oID) >= exclude.length){
                            break;
                        }
                        newWhereClause += (k==0?"":",")+"'" + exclude[k+(oracleLimit*oID)] + "'";
                    }
                    excludeList += newWhereClause + ")";
                }
                nativeQuery+=excludeList+")";
            }
            Query query = entityManager.createNativeQuery( nativeQuery);
            query.setParameter("app_id", appID);
            int count = Integer.parseInt(query.setMaxResults(1).getSingleResult().toString());
//            System.out.println(appID+" COUNT: "+count);
            return count;
        } catch (NoResultException e) {
//            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return 0;
    }

    public List<TxTmClass> selectAllExclude(List<KeyValue> searchCriteria, String[] exclude, String orderBy, String orderType, Integer offset, Integer limit) {
        try {
            String appID = "";
            String DESC = "";
            String DESCEN = "";
            if (searchCriteria != null) {
                for (int i = 0; i < searchCriteria.size(); i++) {
                    String searchBy = searchCriteria.get(i).getKey();
                    Object value = searchCriteria.get(i).getValue();
                    if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                        if(searchBy.contains("txTmGeneral")){
                            appID = value.toString();
                        }
                        else if(searchBy.equalsIgnoreCase("DESC")){
                            DESC = value.toString();
                        }
                        else if(searchBy.equalsIgnoreCase("DESCEN")){
                            DESCEN = value.toString();
                        }
                    }
                }
            }
            StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c "
                    + "LEFT JOIN FETCH c.txTmGeneral tg "
                    + "LEFT JOIN FETCH c.mClassDetail mcd "
                    + "LEFT JOIN FETCH mcd.parentClass pc ");
            sbQuery.append(" WHERE c.txTmGeneral.id = :p1");
            if(DESC.length()>0)
                sbQuery.append(" AND mcd.desc LIKE :p2");
            if (DESCEN.length()>0)
                sbQuery.append(" AND mcd.descEn LIKE :p3");
            if(exclude != null) {
                // ORACLE 1000 limit!
                int oracleLimit = 1000;
                String excludeList = " AND (";
                for (int oID = 0; oID <= exclude.length/oracleLimit; oID++) {
                    if (oracleLimit * oID == exclude.length) {
                        break;
                    }
                    String newWhereClause = (oID==0?"":" OR ")+" mcd.id NOT IN (";
                    for (int k = 0; k < (exclude.length>(oracleLimit*(oID+1))?oracleLimit:exclude.length); k++) {
                        if (k+(oracleLimit*oID) >= exclude.length){
                            break;
                        }
                        newWhereClause += (k==0?"":",")+"'" + exclude[k+(oracleLimit*oID)] + "'";
                    }
                    excludeList += newWhereClause + ")";
                }
                sbQuery.append(excludeList+")");
            }
            sbQuery.append(" ORDER BY mcd.parentClass.no ");
//System.out.println("QUERY: "+sbQuery);
            Query query = entityManager.createQuery(sbQuery.toString());
            query.setParameter("p1", appID);
            if(DESC.length()>0)
                query.setParameter("p2", "%"+DESC+"%");
            if(DESCEN.length()>0)
                query.setParameter("p3", "%" + DESCEN + "%");

            if (offset != null) {
                query.setFirstResult(offset);
            }
            if (limit != null) {
                query.setMaxResults(limit);
            }
            return query.getResultList();
        } catch (NoResultException e) {
//            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    // for param surat deskripsi class
    public List<TxTmClass> selectAllClassByTxGeneral(TxTmGeneral generalId, Object transactionStatus, boolean correctionFlag) {
        try {
            StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c "
                    + "LEFT JOIN FETCH c.txTmGeneral tg"
                    + "LEFT JOIN FETCH c.mClassDetail mcd "
                    + "LEFT JOIN FETCH mcd.parentClass pc ");
            sbQuery.append(" WHERE c.txTmGeneral = :p1");
            sbQuery.append(" AND c.transactionStatus = :p2 AND c.correctionFlag = :p3 ORDER BY mcd.parentClass.no ");

            Query query = entityManager.createQuery(sbQuery.toString());
            query.setParameter("p1", generalId);
            query.setParameter("p2", transactionStatus);
            query.setParameter("p3", correctionFlag);

            return query.getResultList();
        } catch (NoResultException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    // for param surat deskripsi class
    public List<TxTmClass> selectAllClassByTxGeneral(TxTmGeneral generalId, Object transactionStatus) {
        try {
            StringBuffer sbQuery = new StringBuffer("SELECT c FROM " + getClassName() + " c "
                    + "LEFT JOIN FETCH c.txTmGeneral tg"
                    + "LEFT JOIN FETCH c.mClassDetail mcd "
                    + "LEFT JOIN FETCH mcd.parentClass pc ");
            sbQuery.append(" WHERE c.txTmGeneral = :p1");
            sbQuery.append(" AND c.transactionStatus = :p2 ORDER BY mcd.parentClass.no ");

            Query query = entityManager.createQuery(sbQuery.toString());
            query.setParameter("p1", generalId);
            query.setParameter("p2", transactionStatus);

            return query.getResultList();
        } catch (NoResultException e) {
//            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    public void modifiedMigrasiClass(String class_no, String applicationNo ) {
        entityManager.createNativeQuery("UPDATE " + tableMigrasiName + " SET IS_PROCESSED=:p0 WHERE CLASS_=:p1 and APPLICATION_NO=:p2")
                .setParameter("p0", 1)
                .setParameter("p1", class_no)
                .setParameter("p2", applicationNo)
                .executeUpdate();
    }

    public void updateClassDetail(String id, String[] fields, Object[] datas) {
        String nativeQuery = "UPDATE TX_TM_CLASS" +
                " SET";
        for (int i = 0; i < fields.length; i++) {
            if(i == fields.length - 1) {
                nativeQuery += " " + fields[i] + " = :p"+i;
            } else {
                nativeQuery += " " + fields[i] + " = :p"+i+ ",";
            }
        }

        nativeQuery += " WHERE TM_CLASS_ID = :id";

        Query query = entityManager.createNativeQuery( nativeQuery);
        for (int i = 0; i < datas.length; i++) {
            query.setParameter("p"+i, datas[i]);
        }

        query.setParameter("id", id);
        query.executeUpdate();
    }
    
    /*public void deleteByMultipleClassDetail(String app_id, String[] detail_id) {
    	String nativeQuery = "DELETE TX_TM_CLASS" +
				" WHERE" +
				" APPLICATION_ID = :app_id AND NOT EXISTS (SELECT 1 FROM(";
    	String whereDetail = "";
		for (int i = 0; i < detail_id.length; i++) {
			//whereDetail += ":det" + i;
			if(i == detail_id.length - 1) {
				nativeQuery +="SELECT " + ":det" + i + " as class_det_id FROM DUAL";
			} else {
				nativeQuery +="SELECT " + ":det" + i + " as class_det_id FROM DUAL UNION ";
			}
			//whereDetail += ":det" + i + ",";
		}
		nativeQuery +=") A WHERE CLASS_DETAIL_ID = class_det_id)";
		if(whereDetail.length() > 0) {
			whereDetail = whereDetail.substring(0, whereDetail.length() - 1);
		}
		//nativeQuery += " AND CLASS_DETAIL_ID NOT IN (" + whereDetail + ")";

		Query query = entityManager.createNativeQuery( nativeQuery);
		for (int i = 0; i < detail_id.length; i++) {
			query.setParameter("det"+i, detail_id[i]);
		}
		query.setParameter("app_id", app_id);
		query.executeUpdate();
	}*/

    public void deleteALLByMultipleClassDetail(String app_id, String[] detail_id) {
        String nativeQuery = "DELETE M_CLASS_DETAIL" +
                " WHERE" +
                " CLASS_DETAIL_ID in ( ";
        String whereDetail = " SELECT CLASS_DETAIL_ID FROM TX_TM_CLASS WHERE" +
                " APPLICATION_ID = :app_id AND (";
        // ORACLE 1000 limit!
        int oracleLimit = 1000;
        for (int oID = 0; oID <= detail_id.length/oracleLimit; oID++) {
            if (oracleLimit * oID == detail_id.length) {
                break;
            }
            String newWhereClause = (oID==0?"":" OR ")+"CLASS_DETAIL_ID NOT IN (";
            for (int k = 0; k < (detail_id.length>(oracleLimit*(oID+1))?oracleLimit:detail_id.length); k++) {
                if (k+(oracleLimit*oID) >= detail_id.length){
                    break;
                }
                newWhereClause += (k==0?"":",")+":det" + (k+(oracleLimit*oID));
            }
            whereDetail += newWhereClause + ")";
        }
        if(detail_id.length>0) {
            nativeQuery += whereDetail + ")";
        } else {
            nativeQuery += " AND TM_CLASS_STATUS = 'PENDING'";
        }
        nativeQuery +=  ")";
        System.err.println("nativeQuery: "+nativeQuery);
        Query query = entityManager.createNativeQuery( nativeQuery);
        for (int i = 0; i < detail_id.length; i++) {
            query.setParameter("det"+i, detail_id[i]);
            System.err.println("det"+i+" :"+ detail_id[i]);
        }
        query.setParameter("app_id", app_id);
        query.executeUpdate();


        nativeQuery = "DELETE TX_TM_CLASS" +
                " WHERE" +
                " APPLICATION_ID = :app_id ";
        whereDetail = " AND (";
        // ORACLE 1000 limit!
        for (int oID = 0; oID <= detail_id.length/oracleLimit; oID++) {
            if (oracleLimit * oID == detail_id.length) {
                break;
            }
            String newWhereClause = (oID==0?"":" OR ")+"CLASS_DETAIL_ID NOT IN (";
            for (int k = 0; k < (detail_id.length>(oracleLimit*(oID+1))?oracleLimit:detail_id.length); k++) {
                if (k+(oracleLimit*oID) >= detail_id.length){
                    break;
                }
                newWhereClause += (k==0?"":",")+":det" + (k+(oracleLimit*oID));
            }
            whereDetail += newWhereClause + ")";
        }
        if(detail_id.length>0) {
            nativeQuery += whereDetail + ")";
        } else {
            nativeQuery += " AND TM_CLASS_STATUS = 'PENDING'";
        }
        System.err.println("nativeQuery: "+nativeQuery);
        query = entityManager.createNativeQuery( nativeQuery);
        for (int i = 0; i < detail_id.length; i++) {
            query.setParameter("det"+i, detail_id[i]);
            System.err.println("det"+i+" :"+ detail_id[i]);
        }
        query.setParameter("app_id", app_id);
        query.executeUpdate();
    }
    
    public void deleteALLByMultipleClassDetail2(String app_id, String[] detail_id) {
        String nativeQuery = "DELETE M_CLASS_DETAIL" +
                " WHERE" +
                " CLASS_DETAIL_ID in ( ";
        String whereDetail = " SELECT CLASS_DETAIL_ID FROM TX_TM_CLASS WHERE" +
                " APPLICATION_ID = :app_id AND (";
        // ORACLE 1000 limit!
        int oracleLimit = 1000;
        for (int oID = 0; oID <= detail_id.length/oracleLimit; oID++) {
            if (oracleLimit * oID == detail_id.length) {
                break;
            }
            String newWhereClause = (oID==0?"":" OR ")+"CLASS_DETAIL_ID NOT IN (";
            for (int k = 0; k < (detail_id.length>(oracleLimit*(oID+1))?oracleLimit:detail_id.length); k++) {
                if (k+(oracleLimit*oID) >= detail_id.length){
                    break;
                }
                newWhereClause += (k==0?"":",")+":det" + (k+(oracleLimit*oID));
            }
            whereDetail += newWhereClause + ")";
        }
        nativeQuery += whereDetail + ") AND TM_CLASS_STATUS = 'PENDING'";
        nativeQuery +=  ")";
        System.err.println("nativeQuery: "+nativeQuery);
        Query query = entityManager.createNativeQuery( nativeQuery);
        for (int i = 0; i < detail_id.length; i++) {
            query.setParameter("det"+i, detail_id[i]);
            System.err.println("det"+i+" :"+ detail_id[i]);
        }
        query.setParameter("app_id", app_id);
        query.executeUpdate();


        nativeQuery = "DELETE TX_TM_CLASS" +
                " WHERE" +
                " APPLICATION_ID = :app_id ";
        whereDetail = " AND (";
        // ORACLE 1000 limit!
        for (int oID = 0; oID <= detail_id.length/oracleLimit; oID++) {
            if (oracleLimit * oID == detail_id.length) {
                break;
            }
            String newWhereClause = (oID==0?"":" OR ")+"CLASS_DETAIL_ID NOT IN (";
            for (int k = 0; k < (detail_id.length>(oracleLimit*(oID+1))?oracleLimit:detail_id.length); k++) {
                if (k+(oracleLimit*oID) >= detail_id.length){
                    break;
                }
                newWhereClause += (k==0?"":",")+":det" + (k+(oracleLimit*oID));
            }
            whereDetail += newWhereClause + ")";
        }
        nativeQuery += whereDetail + ") AND TM_CLASS_STATUS = 'PENDING'";
        System.err.println("nativeQuery: "+nativeQuery);
        query = entityManager.createNativeQuery( nativeQuery);
        for (int i = 0; i < detail_id.length; i++) {
            query.setParameter("det"+i, detail_id[i]);
            System.err.println("det"+i+" :"+ detail_id[i]);
        }
        query.setParameter("app_id", app_id);
        query.executeUpdate();
    }

    public void deleteByMultipleClassDetail(String app_id, String[] detail_id) {
        String nativeQuery = "DELETE TX_TM_CLASS" +
                " WHERE" +
                " APPLICATION_ID = :app_id ";
        String whereDetail = " AND (";
        // ORACLE 1000 limit!
        int oracleLimit = 1000;
        for (int oID = 0; oID <= detail_id.length/oracleLimit; oID++) {
            if (oracleLimit * oID == detail_id.length) {
                break;
            }
            String newWhereClause = (oID==0?"":" OR ")+"CLASS_DETAIL_ID NOT IN (";
            for (int k = 0; k < (detail_id.length>(oracleLimit*(oID+1))?oracleLimit:detail_id.length); k++) {
                if (k+(oracleLimit*oID) >= detail_id.length){
                    break;
                }
                newWhereClause += (k==0?"":",")+":det" + (k+(oracleLimit*oID));
            }
            whereDetail += newWhereClause + ")";
        }
        if(detail_id.length>0) {
            nativeQuery += whereDetail + ")";
        } else {
            nativeQuery += " AND TM_CLASS_STATUS = 'PENDING'";
        }
        System.err.println("nativeQuery: "+nativeQuery);
        Query query = entityManager.createNativeQuery( nativeQuery);
        for (int i = 0; i < detail_id.length; i++) {
            query.setParameter("det"+i, detail_id[i]);
            System.err.println("det"+i+" :"+ detail_id[i]);
        }
        query.setParameter("app_id", app_id);
        query.executeUpdate();
    }
    
    public void deleteByMultipleClassDetail2(String app_id, String[] detail_id) {
        String nativeQuery = "DELETE TX_TM_CLASS" +
                " WHERE" +
                " APPLICATION_ID = :app_id ";
        String whereDetail = " AND (";
        // ORACLE 1000 limit!
        int oracleLimit = 1000;
        for (int oID = 0; oID <= detail_id.length/oracleLimit; oID++) {
            if (oracleLimit * oID == detail_id.length) {
                break;
            }
            String newWhereClause = (oID==0?"":" OR ")+"CLASS_DETAIL_ID NOT IN (";
            for (int k = 0; k < (detail_id.length>(oracleLimit*(oID+1))?oracleLimit:detail_id.length); k++) {
                if (k+(oracleLimit*oID) >= detail_id.length){
                    break;
                }
                newWhereClause += (k==0?"":",")+":det" + (k+(oracleLimit*oID));
            }
            whereDetail += newWhereClause + ")";
        }
        if(detail_id.length>0) {
            nativeQuery += whereDetail + ") AND TM_CLASS_STATUS = 'ACCEPT'";
        } else {
            nativeQuery += " AND TM_CLASS_STATUS = 'ACCEPT'";
        }
        System.err.println("nativeQuery: "+nativeQuery);
        Query query = entityManager.createNativeQuery( nativeQuery);
        for (int i = 0; i < detail_id.length; i++) {
            query.setParameter("det"+i, detail_id[i]);
            System.err.println("det"+i+" :"+ detail_id[i]);
        }
        query.setParameter("app_id", app_id);
        query.executeUpdate();
    }

    public void deleteUnusedMClassDetail(String ownerId) {
        String nativeQuery = "DELETE M_CLASS_DETAIL" +
                " WHERE CREATED_BY = :created_by " +
                " AND STATUS_FLAG = '0' " +
                " AND (CLASS_DETAIL_ID NOT IN (SELECT DISTINCT CLASS_DETAIL_ID FROM TX_TM_CLASS))";

        Query query = entityManager.createNativeQuery( nativeQuery);
        query.setParameter("created_by", ownerId);
        query.executeUpdate();
    }

//	public List<String> selectByMultipleClassDetail(String app_id) {
//		String nativeQuery = "SELECT CLASS_DETAIL_ID FROM TX_TM_CLASS" +
//				" WHERE" +
//				" APPLICATION_ID = :app_id";
//		Query query = entityManager.createNativeQuery( nativeQuery);
//		query.setParameter("app_id", app_id);
//		return query.getResultList();
//	}

    public List<Object[]> selectByMultipleClassDetail(String app_id, String[] fields) {
        String field = "";
        for (String fiel : fields) {
            field += fiel + ", ";
        }
        if(field.length() > 1) {
            field = field.substring(0, field.length() - 2);
        }

        String nativeQuery = "SELECT "+field.toString()+" FROM TX_TM_CLASS" +
                " WHERE" +
                " APPLICATION_ID = :app_id";
        Query query = entityManager.createNativeQuery( nativeQuery);
        query.setParameter("app_id", app_id);
        return query.getResultList();
    }


    public void saveBulk(TxTmClass[] ts) {
        for (TxTmClass t : ts) {
            String nativeQuery = "INSERT INTO TX_TM_CLASS " +
                    "(TM_CLASS_ID, SKIP_AUDIT_TRAIL, CORRECTION_FLAG, CREATED_DATE, CLASS_EDITION, TM_CLASS_STATUS, CLASS_VERSION, XML_FILE_ID, CREATED_BY, CLASS_ID, CLASS_DETAIL_ID, APPLICATION_ID, TM_CLASS_NOTES, UPDATED_DATE, UPDATED_BY) " +
                    "VALUES " +
                    "(:p1, :p2, :p3, :p4, :p5, :p6, :p7, :p8, :p9, :p10, :p11, :p12, :p13, :p14, :p15)";

            Query query = entityManager.createNativeQuery( nativeQuery)
                    .setParameter("p1", t.getId())
                    .setParameter("p2", t.isSkipLogAuditTrail())
                    .setParameter("p3", t.isCorrectionFlag())
                    .setParameter("p4", t.getCreatedDate())
                    .setParameter("p5", t.getEdition())
                    .setParameter("p6", t.getTransactionStatus())
                    .setParameter("p7", t.getVersion())
                    .setParameter("p8", t.getXmlFileId())
                    .setParameter("p9", t.getCreatedBy().getId())
                    .setParameter("p10", t.getmClass().getId())
                    .setParameter("p11", t.getmClassDetail().getId())
                    .setParameter("p12", t.getTxTmGeneral().getId())
                    .setParameter("p13", t.getNotes())
                    .setParameter("p14", t.getUpdatedDate())
                    .setParameter("p15", t.getUpdatedBy());
            query.executeUpdate();
        }
    }

    public List<String> findClassIDTxTmClassByApplicationID(String applicationID){
        Query query = entityManager.createNativeQuery("SELECT DISTINCT CLASS_ID FROM TX_TM_CLASS WHERE APPLICATION_ID=:p1 AND TM_CLASS_STATUS='ACCEPT'");
        query.setParameter("p1", applicationID);
        return query.getResultList();
    }

    public List<String> findClassEditionTxTmClassByApplicationID(String applicationID){
        Query query = entityManager.createNativeQuery("SELECT DISTINCT CLASS_EDITION FROM TX_TM_CLASS WHERE APPLICATION_ID=:p1 AND TM_CLASS_STATUS='ACCEPT'");
        query.setParameter("p1", applicationID);
        return query.getResultList();
    }


}