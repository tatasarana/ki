package com.docotel.ki.repository;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.pojo.NativeQueryModel;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.pojo.KeyValueSelect;
import com.docotel.ki.service.master.RepresentativeService;
import com.ibm.icu.text.SimpleDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Repository
public class NativeQueryRepository  extends BaseRepository<Object> {

    @Autowired
    private EntityManagerFactory emf;

    @Autowired
    RepresentativeService representativeService;


    public StringBuffer getstringQuery(StringBuffer sbQuery, String tablename, ArrayList <KeyValueSelect> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        Timestamp createdDate = null;
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

        return sbQuery;
    }

    public GenericSearchWrapper <Object[]> selectNative(String SELECT, String tablename, String[] targetcoloumn, List <KeyValueSelect> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        GenericSearchWrapper <Object[]> searchResult = new GenericSearchWrapper <>();


        Timestamp createdDate = null;
        if (targetcoloumn.length != 0) {
            int it = 0;
            try {

                // append dengan parameternya ;
                EntityManager em = emf.createEntityManager();
                EntityTransaction tx = em.getTransaction();
                tx.begin();

                String stringquery = "SELECT ";

                int ii = 0;
                for (String tarcol : targetcoloumn) {
                    if (ii == 0)
                        stringquery = stringquery.concat(tarcol);
                    else
                        stringquery = stringquery.concat(" , " + tarcol);
                    ii++;
                }
                stringquery = stringquery.concat(" FROM " + tablename + " WHERE 1 = 1");
                for (KeyValueSelect row : searchCriteria) {
                    stringquery = stringquery.concat(" AND " + row.getKey() + " = '" + row.getValue().toString() + "' ");
                }
//                System.out.println(stringquery);


                if (SELECT == "GET") {
                    searchResult.setList(em.createNativeQuery(stringquery).getResultList());
                    tx.commit();
                    em.close();
                } else if (SELECT == "COUNT")
                    searchResult.setCount(em.createNativeQuery(stringquery).getResultList().size());

                return searchResult;
            } catch (NoResultException e) {
                logger.error(e.getMessage(), e);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }


        return null;
    }


    public String doSearchBy(List <KeyValueSelect> searchBy, String querystring) {
        if (searchBy.size() != 0) {
            for (KeyValueSelect searchby : searchBy) {
                querystring = querystring.concat(" AND " + searchby.getKey() + " " + searchby.getEq() + " '" + searchby.getValue().toString() + "' ");
            }
        }

        return querystring;
    }


//      ======================== SELECT ALL NATIVE QUERY =======================

    public GenericSearchWrapper <Object[]> selectNative2(NativeQueryModel querymodel) {
        GenericSearchWrapper <Object[]> searchResult = new GenericSearchWrapper <>();
        String tablename = querymodel.getTable_name();
        String querystring = "SELECT ";

        try {
            EntityManager em = emf.createEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();

            int ii = 0;
            if (querymodel.getResultcol().length != 0) {

                for (String resultcol : querymodel.getResultcol()) {
                    if (ii == 0)
                        querystring = querystring.concat(resultcol);
                    else
                        querystring = querystring.concat(" , " + resultcol);

                    ii++;
                }
            }

            querystring = querystring.concat(" FROM " + tablename + " WHERE 1=1 ");
            List <KeyValueSelect> searchBy = querymodel.getSearchBy();

            //System.out.println(searchBy.size());

            querystring = doSearchBy(searchBy, querystring);

//            System.out.println(querystring);

            searchResult.setList(em.createNativeQuery(querystring).getResultList());
            tx.commit();

            em.close();
            return searchResult;
        } catch (NoResultException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }


//      ======================== DELETE NATIVE QUERY =======================

    public int deleteNative(NativeQueryModel querymodel) {
        GenericSearchWrapper <Object[]> searchResult = new GenericSearchWrapper <>();
        String tablename = querymodel.getTable_name();
        String querystring = "DELETE ";

        try {
            EntityManager em = emf.createEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();

            querystring = querystring.concat(" FROM " + tablename + " WHERE 1=1 ");
            List <KeyValueSelect> searchBy = querymodel.getSearchBy();


            querystring = doSearchBy(searchBy, querystring);


            em.createNativeQuery(querystring).executeUpdate();
            tx.commit();

            em.close();
            return 1;
        } catch (NoResultException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return 0;
    }

//      ======================== UPDATE NATIVE QUERY =======================

    public int updateNavite(NativeQueryModel querymodel) {

        String tablename = querymodel.getTable_name();
        String querystring = "UPDATE " + tablename + " SET ";

        try {
            EntityManager em = emf.createEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();

            List <KeyValue> updateQ;
            updateQ = querymodel.getUpdateQ();

            int ij = 0;
            if (updateQ.size() != 0) {
                for (KeyValue updateq : updateQ) {
                    if (ij == 0) {
                        querystring = querystring.concat(updateq.getKey() + " = '" + updateq.getValue().toString() + "' ");
                    } else
                        querystring = querystring.concat(" , " + updateq.getKey() + " = '" + updateq.getValue().toString() + "' ");

                    ij++;
                }

            }

            querystring = querystring.concat(" WHERE 1=1 ");
            List <KeyValueSelect> searchBy;
            searchBy = querymodel.getSearchBy();
            querystring = doSearchBy(searchBy, querystring);

            em.createNativeQuery(querystring).executeUpdate();
            tx.commit();
            em.close();

            return 1;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

    }

    public GenericSearchWrapper <Object[]> selectReferenceOOMadrid(String app_id){

        GenericSearchWrapper <Object[]> searchResult = new GenericSearchWrapper <>();
        String querystring = "SELECT c.TM_REFERENCE_ID , c.REF_APPLICATION_ID FROM TX_TM_REFERENCE c WHERE 1 = 1 "+
                " AND c.APPLICATION_ID = :p  " ;

        try {
            EntityManager em = emf.createEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            searchResult.setList(em.createNativeQuery(querystring).
                    setParameter("p", app_id).
                    getResultList());

            tx.commit();
            em.close();

            return searchResult;

        } catch (NoResultException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null ;
    }


    public GenericSearchWrapper <Object[]> selectMonitorOposisi(String no, int limit, int offset) {

        GenericSearchWrapper <Object[]> searchResult = new GenericSearchWrapper <>();
        String syarat1 = "PENGAJUAN_KEBERATAN";
        String syarat2 = "SURAT_HEARING";
        String syarat3 = "SURAT_SANGGAHAN_ATAS_KEBERATAN";

        String querystring = "SELECT b.CREATED_DATE, b.EFILING_NO , c.POST_OWNER_NAME,  t.FILE_TYPE_DESC, f.STATUS_NAME , b.POST_RECEPTION_NOTE " +
                "FROM TX_POST_RECEPTION_DETAIL a , TX_POST_RECEPTION b, TX_POST_OWNER c , M_STATUS f , M_FILE_TYPE t " +
                "WHERE 1 = 1 " +
                "AND a.APPLICATION_ID = :p2 " +
                "AND a.POST_RECEPTION_ID=b.POST_RECEPTION_ID " +
                "AND b.POST_RECEPTION_ID=c.POST_RECEPTION_ID " +
                "AND b.STATUS_ID=f.STATUS_ID " +
                "AND b.FILE_TYPE_ID=t.FILE_TYPE_ID " +
                "AND b.FILE_TYPE_ID IN ( :syarat1  ,  :syarat2   ,  :syarat3  ) ";

        try {
            EntityManager em = emf.createEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            searchResult.setList(em.createNativeQuery(querystring).
                    setParameter("p2", no).
                    setParameter("syarat1", syarat1).
                    setParameter("syarat2", syarat2).
                    setParameter("syarat3", syarat3).
                    getResultList());

            tx.commit();
            em.close();

            return searchResult;

        } catch (NoResultException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }


    public GenericSearchWrapper <Object[]> selectPascaOposisiAdmin(int limit, int offset) {
        GenericSearchWrapper <Object[]> searchResult = new GenericSearchWrapper <>();
        String syarat1 = "PENGAJUAN_KEBERATAN";
        String syarat2 = "SURAT_HEARING";
        String syarat3 = "SURAT_SANGGAHAN_ATAS_KEBERATAN";

        String querystring = "SELECT  b.EFILING_NO , b.POST_RECEPTION_NO, a.APPLICATION_ID,b.FILE_TYPE_ID, c.POST_OWNER_NAME " +
                "FROM TX_POST_RECEPTION_DETAIL a JOIN TX_TM_GENERAL g ON g.APPLICATION_ID = a.APPLICATION_ID " +
                ", TX_POST_RECEPTION b, TX_POST_OWNER c" +
                " WHERE 1=1 " +
                " AND a.POST_RECEPTION_ID=b.POST_RECEPTION_ID " +
                " AND b.POST_RECEPTION_ID=c.POST_RECEPTION_ID " +
                " AND b.FILE_TYPE_ID IN ( :syarat1  ,  :syarat2   ,  :syarat3  ) ";


        try {
            EntityManager em = emf.createEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            searchResult.setList(em.createNativeQuery(querystring).
                    setParameter("syarat1", syarat1).
                    setParameter("syarat2", syarat2).
                    setParameter("syarat3", syarat3).
                    getResultList());

            tx.commit();
            em.close();


            return searchResult;

        } catch (NoResultException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }


        return null;
    }


    public GenericSearchWrapper <Object[]> searchOposisi(String id) {


        String syarat1 = "PENGAJUAN_KEBERATAN";
        String syarat2 = "SURAT_HEARING";
        String syarat3 = "SURAT_SANGGAHAN_ATAS_KEBERATAN";

        GenericSearchWrapper <Object[]> searchResult = new GenericSearchWrapper <>();
        String querystring = "SELECT c.POST_RECEPTION_DETAIL_ID " +
                "FROM TX_POST_RECEPTION_DETAIL c JOIN TX_POST_RECEPTION d " +
                "ON c.POST_RECEPTION_ID = d.POST_RECEPTION_ID " +
                "WHERE 1 = 1 " +
                "AND c.APPLICATION_ID = :p2 " +
                "AND d.STATUS_ID <> 'IPT_DRAFT' " +
                "AND d.FILE_TYPE_ID IN ( :syarat1  ,  :syarat2   ,  :syarat3 ) ";

        try {
            EntityManager em = emf.createEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            searchResult.setList(em.createNativeQuery(querystring).
                    setParameter("p2", id).
                    setParameter("syarat1", syarat1).
                    setParameter("syarat2", syarat2).
                    setParameter("syarat3", syarat3).
                    getResultList());

            tx.commit();
            em.close();
            return searchResult;

        } catch (NoResultException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    public List <String> selectDistinct(String table, String coloumn) {

        String querystring = "SELECT DISTINCT " + coloumn + "  FROM " + table + " WHERE " + coloumn + " IS NOT NULL GROUP BY " + coloumn;


        try {
            EntityManager em = emf.createEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();

            List <String> searchResult = em.createNativeQuery(querystring).getResultList();
            tx.commit();
            em.close();
            return searchResult;

        } catch (NoResultException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }


    public GenericSearchWrapper <Object[]> selectNotifikasiPemohon(int limit, int offset, String user_id) {
        String querystring = "SELECT  t.APPLICATION_ID, t.CC_NOTES, t.POST_RECEPTION_ID, t.DUE_DATE, t.WORKFLOW_PROCESS_ACTION_ID,  MAX(t.CREATED_DATE) ,MAX(t.MONITOR_ID) FROM TX_MONITOR t \n" +
                "WHERE 1 = 1 " +
                "AND t.CREATED_BY = " + user_id + " " +
                "GROUP BY t.APPLICATION_ID, t.CC_NOTES ,POST_RECEPTION_ID , t.DUE_DATE , t.WORKFLOW_PROCESS_ACTION_ID\n" +
                "ORDER BY t.APPLICATION_ID DESC";


        return null ;
    }


    public GenericSearchWrapper <Object[]> selectNotifikasiPemohonDownload(int limit, int offset, String user_id, String action_type) {
        String querystring =
                "SELECT t.APPLICATION_ID , u.USER_ID, u.USERNAME, t.CREATED_DATE, a.ACTION_TYPE  FROM TX_MONITOR t , M_WORKFLOW_PROCESS_ACTIONS w , M_ACTION a ,\n" +
                        "M_USER u , TX_TM_GENERAL g \n" +
                        "--khusus pasca\n" +
                        "--, TX_POST_RECEPTION p\n" +
                        "WHERE 1 = 1\n" +
                        "AND t.WORKFLOW_PROCESS_ACTION_ID = w.ID\n" +
                        "AND w.ACTION_ID = a.ACTION_ID\n" +
                        "AND g.CREATED_BY = u.USER_ID\n" +
                        "AND u.USERNAME = 'neeveea@gmail.com'\n" +
                        "AND a.ACTION_TYPE = 'Download'\n" +
                        "--khusus untuk Pasca\n" +
                        "--AND g.APPLICATION_ID = p.POST_RECEPTION_ID\n" +
                        "\n" +
                        "ORDER BY t.CREATED_DATE DESC";


        return null ;
    }






}
