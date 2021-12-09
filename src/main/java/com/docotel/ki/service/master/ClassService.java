package com.docotel.ki.service.master;

import com.docotel.ki.model.master.MClass;
import com.docotel.ki.model.master.MClassDetail;
import com.docotel.ki.model.transaction.TxTmClass;
import com.docotel.ki.model.transaction.TxTmClassLimitation;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.master.MClassDetailCustomRepository;
import com.docotel.ki.repository.custom.master.MClassHeaderCustomRepository;
import com.docotel.ki.repository.custom.pojo.MigrasiClassRepository;
import com.docotel.ki.repository.custom.transaction.TxTmClassCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmClassLimitationCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmGeneralCustomRepository;
import com.docotel.ki.repository.master.MClassDetailRepository;
import com.docotel.ki.repository.master.MClassHeaderRepository;
import com.docotel.ki.repository.transaction.TxTmClassLimitationRepository;
import com.docotel.ki.repository.transaction.TxTmClassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ClassService {
    @Autowired
    TxTmClassRepository txTmClassRepository;
    @Autowired
    TxTmClassCustomRepository txTmClassCustomRepository;
    @Autowired
    TxTmGeneralCustomRepository txTmGeneralCustomRepository;
    @Autowired
    MClassHeaderRepository mClassHeaderRepository;
    @Autowired
    MClassDetailRepository mClassDetailRepository;
    @Autowired
    MClassDetailCustomRepository mClassDetailCustomRepository;
    @Autowired
    MClassHeaderCustomRepository mClassHeaderCustomRepository;
    @Autowired
    MigrasiClassRepository migrasiClassRepository;
    @Autowired
    TxTmClassLimitationRepository txTmClassLimitationRepository;
    @Autowired
    TxTmClassLimitationCustomRepository txTmClassLimitationCustomRepository;
    @Autowired
    MClassDetailCustomRepository mClassDetailCustonRepository;


    /* --------  MCass Section -------- */

    public List<MClass> findAllMClass() {
        return mClassHeaderCustomRepository.selectAll(null, null, false, "no", "ASC", null, null);
    }

    public List<TxTmClass> findTxTmClassByTxTmGeneral(TxTmGeneral txTmGeneral) {
        return txTmClassRepository.findTxTmClassByTxTmGeneral(txTmGeneral);
    }

    public List<TxTmClass> findTxTmClassByGeneralId(TxTmGeneral txTmGeneral) {
        return txTmClassRepository.findTxTmClassByGeneralId(txTmGeneral);
    }

    public List<MClass> findByStatusFlagTrue() {
        return mClassHeaderRepository.findByStatusFlagTrue();
    }

    public MClass findOneMClass(String id) {
        return mClassHeaderRepository.findOne(id);
    }

    public MClass findFirstByNo(String no) {
        return mClassHeaderRepository.findFirstByNo(no);
    }

    public MClass findFirstByNo(int no) {
        return mClassHeaderRepository.findFirstByNo(no);
    }

    public void insert(MClass data) {
        mClassHeaderRepository.save(data);
    }

    @Transactional
    public void saveOrUpdate(MClass mClass) {
        mClassHeaderCustomRepository.saveOrUpdate(mClass);
    }

    public GenericSearchWrapper<MClass> searchClassHeader(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        GenericSearchWrapper<MClass> searchResult = new GenericSearchWrapper<>();
        searchResult.setCount(mClassHeaderCustomRepository.countClass(searchCriteria));
        if (searchResult.getCount() > 0) {
            searchResult.setList(mClassHeaderCustomRepository.selectAllClass(searchCriteria, orderBy, orderType, offset, limit));
        }
        return searchResult;
    }

    /*----------------------------------------*/

    /* --------  MClass Child Section -------- */

    public List<MClassDetail> findAllMClassDetail() {
        return mClassDetailRepository.findAll();
    }

    public List<MClassDetail> findClassDetailByStatusFlagTrue() {
        return mClassDetailRepository.findByStatusFlagTrue();
    }

    public List<MClassDetail> selectAllByClassHeader(String by, String value) {
        return mClassDetailCustomRepository.selectAllByClassHeader(by, value, 0, 500);
    }

    public MClassDetail findOneMClassDetail(String id) {
        return mClassDetailRepository.findOne(id);
    }

    public MClassDetail findFirstByStatusFlagTrueAndId(String id) {
        return mClassDetailRepository.findFirstByStatusFlagTrueAndId(id);
    }

    @Transactional
    public void saveClassChild(MClassDetail mClassDetail) {
        mClassDetailCustomRepository.saveOrUpdate(mClassDetail);
    }
    
    @Transactional
    public void deleteBarangJasa(String appNo) {
    	mClassDetailRepository.deleteByMClassBaseNo(appNo);
    	txTmClassRepository.deleteBarangJasa(appNo);    	
    }

    public GenericSearchWrapper<MClassDetail> listClassChild(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        GenericSearchWrapper<MClassDetail> searchResult = new GenericSearchWrapper<>();
        searchResult.setCount(mClassDetailCustomRepository.count(searchCriteria));
        if (searchResult.getCount() > 0) {
            searchResult.setList(mClassDetailCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
        }
        return searchResult;
    }

    public GenericSearchWrapper<Object[]> searchClassTest( List<KeyValue> searchCriteria, String[] exclude, String orderBy, String orderType, Integer offset, Integer limit){
        GenericSearchWrapper<Object[]> searchResult = new GenericSearchWrapper<>();

        String  input = "";
        String stringinput ;
        String class_condition = "";
        int language = 0 ;

        for(KeyValue row : searchCriteria){
            if (row.getKey().toString().equalsIgnoreCase("parentClass.id")){
                class_condition = row.getValue().toString().replace("class_","");
            }
            if (row.getKey().toString().equalsIgnoreCase("desc")) {
                input = row.getValue().toString().replace(" ", ",");
            }
            if (row.getKey().toString().equalsIgnoreCase("descEn")){
                if (row.getValue().toString().equalsIgnoreCase("")){
                    language = 0 ;
                }
                else{
                    language = 1 ;
                    input = row.getValue().toString().replace(" ",",");
                }
            }
        }
        StringBuilder stringq = new StringBuilder();
        stringq.append("SELECT M_CLASS_DETAIL.CLASS_ID, M_CLASS.CLASS_NO, M_CLASS_DETAIL.CLASS_SERIAL_1, M_CLASS_DETAIL.CLASS_DESC, M_CLASS_DETAIL.CLASS_DESC_EN, M_CLASS.CLASS_EDITION, M_CLASS.CLASS_VERSION, M_CLASS.CLASS_TYPE FROM M_CLASS_DETAIL LEFT JOIN M_CLASS ON M_CLASS.CLASS_NO = M_CLASS_DETAIL.CLASS_ID  ") ;
        String strcount ;
        if (language == 0){
            if (class_condition.equalsIgnoreCase("")){
                stringq.append("WHERE CONTAINS(M_CLASS_DETAIL.CLASS_DESC, 'near(("+processStringKelasDetailSearch(input)+",{ }),2)',11) > 0 ORDER BY SCORE(11) ASC ");
            }
            else{
                stringq.append("WHERE CLASS_ID = '"+class_condition+" ");
                stringq.append("AND CONTAINS(M_CLASS_DETAIL.CLASS_DESC, 'near(("+processStringKelasDetailSearch(input)+",{ }),2)',11) > 0 ORDER BY SCORE(11) ASC ");
            }
        }
        else
        {
            if (class_condition.equalsIgnoreCase("")){
                stringq.append("WHERE CONTAINS(M_CLASS_DETAIL.CLASS_DESC_EN, 'near(("+processStringKelasDetailSearch(input)+",{ }),2)',11) > 0 ORDER BY SCORE(11) ASC ");

            }
//                stringinput = "SELECT CLASS_ID , CLASS_DESC , CLASS_DESC_EN FROM M_CLASS_DETAIL WHERE CONTAINS(CLASS_DESC_EN, 'near(("+input+",{ }),2)',11) > 0 ORDER BY SCORE(11) ASC \n";
            else{
                stringq.append("WHERE CLASS_ID = '"+class_condition+" ");
                stringq.append("AND CONTAINS(M_CLASS_DETAIL.CLASS_DESC_EN, 'near(("+processStringKelasDetailSearch(input)+",{ }),2)',11) > 0 ORDER BY SCORE(11) ASC ");
            }
//                stringinput = "SELECT CLASS_ID , CLASS_DESC , CLASS_DESC_EN FROM M_CLASS_DETAIL WHERE CLASS_ID = '"+class_condition+"' AND  CONTAINS(CLASS_DESC_EN, 'near(("+input+",{ }),2)',11) > 0 ORDER BY SCORE(11) ASC \n";
        }
        searchResult.setList(mClassDetailCustomRepository.searchRawQuery(stringq.toString(),0,-1));
        long numlist = searchResult.getList().size();
        searchResult.setCount(numlist)  ;
        return searchResult;
    }

    public String processStringKelasDetailSearch(String input){
        return input
                .replaceAll(",","\",\"")
                .replaceAll(" ","\",\"");
    }

    public static String getOnlyStringsOrNumber(String s) {
        Pattern pattern = Pattern.compile("[^a-z A-Z0-9]");
        Matcher matcher = pattern.matcher(s);
        String replacement = matcher.replaceAll(",");
        return replacement.replaceAll("^(,|\\s)*|(,|\\s)*$", "").replaceAll("(\\,\\s*)+", ",");
    }

    public GenericSearchWrapper<Object[]> searchClassMultiKeyword(List<KeyValue> searchCriteria, String[] exclude, String orderBy, String orderType, Integer offset, Integer limit) {

        GenericSearchWrapper<Object[]> searchResult = new GenericSearchWrapper<>();
        String stringinput = "";
        String class_condition = "";
        int language = 0;
        int MaxIndex= searchCriteria.size();
        int currIndex = 0;
        for (KeyValue row : searchCriteria) {
            if (row.getKey().toString().equalsIgnoreCase("parentClass.id")) {
                class_condition = row.getValue().toString().replace("class_", "");
            }
            String input = "";
            int InputLength = row.getValue().toString().split(" ").length;
            if (row.getKey().toString().equalsIgnoreCase("desc")) { //Bahasa indonesia
                input = row.getValue().toString().replace(" ", "\",\"");
                input = input.replace("\'", "\",\"");
                input = getOnlyStringsOrNumber(input);
                if (stringinput != "") {
                    stringinput += " UNION ";
                }
                String filterClassID = class_condition==""?"":"CLASS_ID = 'class_" + class_condition + "' AND ";
                //while value input is empty, show all
                if(input.length()>0) {
                    stringinput += "SELECT CLASS_ID , CLASS_DESC , CLASS_DESC_EN, CLASS_DETAIL_ID, STATUS_FLAG,"
                            + (MaxIndex - currIndex) + "-SCORE(11)/(" + InputLength
                            + " - LENGTH(CLASS_DESC)) AS score, COUNT(CLASS_DESC) AS total, CREATED_DATE, CREATED_BY FROM M_CLASS_DETAIL WHERE " + filterClassID
                            + " CONTAINS(CLASS_DESC, 'near((\"" + processStringKelasDetailSearch(input) + "\",{ }),2)',11) > 0 AND STATUS_FLAG=1 GROUP BY CLASS_ID , CLASS_DESC, CLASS_DESC_EN, "
                            +"CLASS_DETAIL_ID, STATUS_FLAG, SCORE(11), CREATED_DATE, CREATED_BY\n";
                }
            }
            else if (row.getKey().toString().equalsIgnoreCase("descEn") && row.getValue().toString()!="") { //English
                language = 1;
                if (stringinput != "") {
                    stringinput += " UNION ";
                }
                input = row.getValue().toString().replace(" ", "\",\""); // tambahin kutip buat english saja, supaya tidak bentrok dengan sql query parser!
                input = input.replace("\'", "\",\"");
                input = getOnlyStringsOrNumber(input);
                String filterClassID = class_condition==""?"":"CLASS_ID = 'class_" + class_condition + "' AND ";
                stringinput += "SELECT CLASS_ID , CLASS_DESC , CLASS_DESC_EN, CLASS_DETAIL_ID, STATUS_FLAG,"+ ( MaxIndex - currIndex) +"-SCORE(11)/("+InputLength+" - LENGTH(CLASS_DESC_EN)) AS score, COUNT(CLASS_DESC_EN) AS total, CREATED_DATE, CREATED_BY FROM M_CLASS_DETAIL WHERE "+filterClassID+" CONTAINS(CLASS_DESC_EN, 'near((\"" + processStringKelasDetailSearch(input) + "\",{ }),2)',11) > 0 AND STATUS_FLAG=1 GROUP BY CLASS_ID , CLASS_DESC, CLASS_DESC_EN, CLASS_DETAIL_ID, STATUS_FLAG, SCORE(11), CREATED_DATE, CREATED_BY \n";
            }
            currIndex++;
        }

        if(stringinput==""){
            //show all
            String filterClassID = class_condition==""?"":"CLASS_ID = 'class_" + class_condition + "' AND ";
            stringinput += "SELECT CLASS_ID , CLASS_DESC , CLASS_DESC_EN, CLASS_DETAIL_ID, STATUS_FLAG, 1 AS score, COUNT(CLASS_DESC) AS total, CREATED_DATE, CREATED_BY FROM M_CLASS_DETAIL WHERE " + filterClassID + "STATUS_FLAG=1 GROUP BY CLASS_ID , CLASS_DESC, CLASS_DESC_EN, CLASS_DETAIL_ID, STATUS_FLAG, 1, CREATED_DATE, CREATED_BY\n";
        }

        //EXCLUDED LIST
        String excludeCriteria = "";
        if(exclude != null) {
            // ORACLE 1000 limit!
            int oracleLimit = 1000;
            String excludeList = " WHERE (";
            for (int oID = 0; oID <= exclude.length/oracleLimit; oID++) {
                if (oracleLimit * oID == exclude.length) {
                    break;
                }
                String newWhereClause = (oID==0?"":" OR ")+" CLASS_DETAIL_ID NOT IN (";
                for (int k = 0; k < (exclude.length>(oracleLimit*(oID+1))?oracleLimit:exclude.length); k++) {
                    if (k+(oracleLimit*oID) >= exclude.length){
                        break;
                    }
                    newWhereClause += (k==0?"":",")+"'" + exclude[k+(oracleLimit*oID)] + "'";
                }
                excludeList += newWhereClause + ")";
            }
            excludeCriteria += excludeList+" ) ";
        }
        excludeCriteria += " GROUP BY CLASS_ID, CLASS_DESC, CLASS_DESC_EN, CLASS_DETAIL_ID, STATUS_FLAG, score, total, CREATED_DATE, CREATED_BY ORDER BY score DESC, ";
                if(orderBy!="") {
                    excludeCriteria += orderBy + " " + orderType;
                }



        //UNION multi criteria
        //disini ujungnya pake SUM
        searchResult.setList(mClassDetailCustomRepository.searchRawQuery("SELECT CLASS_ID, CLASS_DESC, CLASS_DESC_EN, CLASS_DETAIL_ID, SUM(total) OVER() AS total1, STATUS_FLAG, CREATED_DATE, CREATED_BY FROM (" + stringinput + " )"+excludeCriteria, offset, limit));
        //long numlist = mClassDetailCustomRepository.searchRawQueryCount(stringinput+excludeCriteria);
        if(searchResult==null || searchResult.getList().size()==0){
            searchResult.setCount(0);
        }else {
            searchResult.setCount(Integer.parseInt(searchResult.getList().get(0)[4].toString()));
        }
        return searchResult;
    }

    public GenericSearchWrapper<Object[]> searchClassMultiKeywordPratinjau(List<KeyValue> searchCriteria, String[] exclude, String orderBy, String orderType, Integer offset, Integer limit) {

        GenericSearchWrapper<Object[]> searchResult = new GenericSearchWrapper<>();
        String stringinput = "";
        String class_condition = "";
        int language = 0;
        int MaxIndex= searchCriteria.size();
        int currIndex = 0;
        for (KeyValue row : searchCriteria) {
            if (row.getKey().toString().equalsIgnoreCase("parentClass.id")) {
                class_condition = row.getValue().toString().replace("class_", "");
            }
            String input = "";
            int InputLength = row.getValue().toString().split(" ").length;
            if (row.getKey().toString().equalsIgnoreCase("desc")) { //Bahasa indonesia
                input = row.getValue().toString().replace(" ", "\",\"");
                input = input.replace("\'", "\",\"");
                input = getOnlyStringsOrNumber(input);
                if (stringinput != "") {
                    stringinput += " UNION ";
                }
                String filterClassID = class_condition==""?"":"CLASS_ID = 'class_" + class_condition + "' AND ";
                //while value input is empty, show all
                if(input.length()>0) {
                    stringinput += "SELECT CLASS_ID , CLASS_DESC , CLASS_DESC_EN, CLASS_DETAIL_ID, STATUS_FLAG,"
                            + (MaxIndex - currIndex) + "-SCORE(11)/(" + InputLength
                            + " - LENGTH(CLASS_DESC)) AS score, COUNT(CLASS_DESC) AS total, CREATED_DATE, CREATED_BY FROM M_CLASS_DETAIL WHERE " + filterClassID
                            + " CONTAINS(CLASS_DESC, 'near((\"" + processStringKelasDetailSearch(input) + "\",{ }),2)',11) > 0 GROUP BY CLASS_ID , CLASS_DESC, CLASS_DESC_EN, "
                            +"CLASS_DETAIL_ID, STATUS_FLAG, SCORE(11), CREATED_DATE, CREATED_BY\n";
                }
            }
            else if (row.getKey().toString().equalsIgnoreCase("descEn") && row.getValue().toString()!="") { //English
                language = 1;
                if (stringinput != "") {
                    stringinput += " UNION ";
                }
                input = row.getValue().toString().replace(" ", "\",\""); // tambahin kutip buat english saja, supaya tidak bentrok dengan sql query parser!
                input = input.replace("\'", "\",\"");
                input = getOnlyStringsOrNumber(input);
                String filterClassID = class_condition==""?"":"CLASS_ID = 'class_" + class_condition + "' AND ";
                stringinput += "SELECT CLASS_ID , CLASS_DESC , CLASS_DESC_EN, CLASS_DETAIL_ID, STATUS_FLAG,"+ ( MaxIndex - currIndex) +"-SCORE(11)/("+InputLength+" - LENGTH(CLASS_DESC_EN)) AS score, COUNT(CLASS_DESC_EN) AS total, CREATED_DATE, CREATED_BY FROM M_CLASS_DETAIL WHERE "+filterClassID+" CONTAINS(CLASS_DESC_EN, 'near((\"" + processStringKelasDetailSearch(input) + "\",{ }),2)',11) > 0 GROUP BY CLASS_ID , CLASS_DESC, CLASS_DESC_EN, CLASS_DETAIL_ID, STATUS_FLAG, SCORE(11), CREATED_DATE, CREATED_BY \n";
            }
            currIndex++;
        }

        if(stringinput==""){
            //show all
            String filterClassID = class_condition==""?"":"CLASS_ID = 'class_" + class_condition + "' AND ";
            stringinput += "SELECT CLASS_ID , CLASS_DESC , CLASS_DESC_EN, CLASS_DETAIL_ID, STATUS_FLAG, 1 AS score, COUNT(CLASS_DESC) AS total, CREATED_DATE, CREATED_BY FROM M_CLASS_DETAIL WHERE " + filterClassID + "GROUP BY CLASS_ID , CLASS_DESC, CLASS_DESC_EN, CLASS_DETAIL_ID, STATUS_FLAG, 1, CREATED_DATE, CREATED_BY\n";
        }

        //EXCLUDED LIST
        String excludeCriteria = "";
        if(exclude != null) {
            // ORACLE 1000 limit!
            int oracleLimit = 1000;
            String excludeList = " WHERE (";
            for (int oID = 0; oID <= exclude.length/oracleLimit; oID++) {
                if (oracleLimit * oID == exclude.length) {
                    break;
                }
                String newWhereClause = (oID==0?"":" OR ")+" CLASS_DETAIL_ID NOT IN (";
                for (int k = 0; k < (exclude.length>(oracleLimit*(oID+1))?oracleLimit:exclude.length); k++) {
                    if (k+(oracleLimit*oID) >= exclude.length){
                        break;
                    }
                    newWhereClause += (k==0?"":",")+"'" + exclude[k+(oracleLimit*oID)] + "'";
                }
                excludeList += newWhereClause + ")";
            }
            excludeCriteria += excludeList+" ) ";
        }
        excludeCriteria += " GROUP BY CLASS_ID, CLASS_DESC, CLASS_DESC_EN, CLASS_DETAIL_ID, STATUS_FLAG, score, total, CREATED_DATE, CREATED_BY ORDER BY score DESC, ";
        if(orderBy!="") {
            excludeCriteria += orderBy + " " + orderType;
        }



        //UNION multi criteria
        //disini ujungnya pake SUM
        searchResult.setList(mClassDetailCustomRepository.searchRawQuery("SELECT CLASS_ID, CLASS_DESC, CLASS_DESC_EN, CLASS_DETAIL_ID, SUM(total) OVER() AS total1, STATUS_FLAG, CREATED_DATE, CREATED_BY FROM (" + stringinput + " )"+excludeCriteria, offset, limit));
        //long numlist = mClassDetailCustomRepository.searchRawQueryCount(stringinput+excludeCriteria);
        if(searchResult==null || searchResult.getList().size()==0){
            searchResult.setCount(0);
        }else {
            searchResult.setCount(Integer.parseInt(searchResult.getList().get(0)[4].toString()));
        }
        return searchResult;
    }

    public GenericSearchWrapper<Object[]> searchClassMultiKeywordAdmin(List<KeyValue> searchCriteria, String[] exclude, String orderBy, String orderType, Integer offset, Integer limit) {

        GenericSearchWrapper<Object[]> searchResult = new GenericSearchWrapper<>();
        String stringinput = "";
        String additionalCriteria = " WHERE 1 = 1";
        String class_condition = "";
        int language = 0;
        int MaxIndex= searchCriteria.size();
        int currIndex = 0;
        String additionalSearchUser = "1=1";
        for (KeyValue row : searchCriteria) {

            if(row.getKey().toString().equalsIgnoreCase("createdBy") && row.getValue().toString()!="")
                additionalSearchUser = " CREATED_BY in (SELECT USER_ID FROM M_USER where LOWER(USERNAME) = '" + row.getValue().toString().toLowerCase() + "')";

            if (row.getKey().toString().equalsIgnoreCase("parentClass.id")) {
                class_condition = row.getValue().toString().replace("class_", "");
            }
            String input = "";
            int InputLength = row.getValue().toString().split(" ").length;
            if (row.getKey().toString().equalsIgnoreCase("desc")) { //Bahasa indonesia
                input = row.getValue().toString().replace(" ", "\",\"");
                input = input.replace("\'", "\",\"");
                input = getOnlyStringsOrNumber(input);
                if (stringinput != "") {
                    stringinput += " UNION ";
                }
                String filterClassID = class_condition==""?"":"CLASS_ID = 'class_" + class_condition + "' AND ";
                filterClassID += additionalSearchUser + " AND ";
                //while value input is empty, show all
                if(input.length()>0) {
                    stringinput += "SELECT CLASS_ID , CLASS_DESC , CLASS_DESC_EN, CLASS_DETAIL_ID, STATUS_FLAG,"
                            + (MaxIndex - currIndex) + "-SCORE(11)/(" + InputLength
                            + " - LENGTH(CLASS_DESC)) AS score, COUNT(CLASS_DESC) AS total, CREATED_DATE, CREATED_BY FROM M_CLASS_DETAIL WHERE " + filterClassID
                            + " CONTAINS(CLASS_DESC, 'near((\"" + processStringKelasDetailSearch(input) + "\",{ }),2)',11) > 0 GROUP BY CLASS_ID , CLASS_DESC, CLASS_DESC_EN, "
                            +"CLASS_DETAIL_ID, STATUS_FLAG, SCORE(11), CREATED_DATE, CREATED_BY\n";
                }
            }
            else if (row.getKey().toString().equalsIgnoreCase("descEn") && row.getValue().toString()!="") { //English
                language = 1;
                if (stringinput != "") {
                    stringinput += " UNION ";
                }
                input = row.getValue().toString().replace(" ", "\",\""); // tambahin kutip buat english saja, supaya tidak bentrok dengan sql query parser!
                input = input.replace("\'", "\",\"");
                input = getOnlyStringsOrNumber(input);
                String filterClassID = class_condition==""?"":"CLASS_ID = 'class_" + class_condition + "' AND ";
                filterClassID += additionalSearchUser + " AND ";
                stringinput += "SELECT CLASS_ID , CLASS_DESC , CLASS_DESC_EN, CLASS_DETAIL_ID, STATUS_FLAG,"+ ( MaxIndex - currIndex) +"-SCORE(11)/("+InputLength+" - LENGTH(CLASS_DESC_EN)) AS score, COUNT(CLASS_DESC_EN) AS total, CREATED_DATE, CREATED_BY FROM M_CLASS_DETAIL WHERE "+filterClassID+" CONTAINS(CLASS_DESC_EN, 'near((\"" + processStringKelasDetailSearch(input) + "\",{ }),2)',11) > 0 GROUP BY CLASS_ID , CLASS_DESC, CLASS_DESC_EN, CLASS_DETAIL_ID, STATUS_FLAG, SCORE(11), CREATED_DATE, CREATED_BY \n";
            }
            /*else if (!row.getKey().toString().equalsIgnoreCase("parentClass.id") && row.getValue().toString()!=""){

                System.out.println("dump_key " + row.getKey().toString());

                if (stringinput != "") {
                    stringinput += " UNION ";
                }

                String field = "";
                String key = row.getKey().toString();

                if(key.equalsIgnoreCase("id")){
                    field = "CLASS_DETAIL_ID";
                } else if(key.equalsIgnoreCase("createdBy")){
                    field = "CREATED_BY";
                } else if(key.equalsIgnoreCase("statusFlag")){
                    field = "STATUS_FLAG";
                }

                String filterClassID = class_condition==""?"":"CLASS_ID = 'class_" + class_condition + "' AND ";
                stringinput += "SELECT CLASS_ID , CLASS_DESC , CLASS_DESC_EN, CLASS_DETAIL_ID, STATUS_FLAG, COUNT(CLASS_DETAIL_ID) AS total, CLASS_DETAIL_ID AS score, CREATED_DATE, CREATED_BY FROM M_CLASS_DETAIL " +
                        "WHERE "+filterClassID + field +  " = '" + row.getValue().toString() + "'" +
                        " GROUP BY CLASS_ID , CLASS_DESC, CLASS_DESC_EN, CLASS_DETAIL_ID, STATUS_FLAG, CREATED_DATE, CREATED_BY \n";
            }*/

            if(row.getKey().toString().equalsIgnoreCase("id") && row.getValue().toString()!=""){
                additionalCriteria += "AND CLASS_DETAIL_ID = '" + row.getValue().toString() + "'";
            }else if(row.getKey().toString().equalsIgnoreCase("statusFlag") && row.getValue().toString()!=""){
                additionalCriteria += "AND STATUS_FLAG = '" + row.getValue().toString() + "'";
            }
            currIndex++;
        }

        if(stringinput==""){
            //show all
            String filterClassID = class_condition==""?"":"CLASS_ID = 'class_" + class_condition + "' AND ";
            filterClassID += additionalSearchUser;
            stringinput += "SELECT CLASS_ID , CLASS_DESC , CLASS_DESC_EN, CLASS_DETAIL_ID, STATUS_FLAG, 1 AS score, COUNT(CLASS_DESC) AS total, CREATED_DATE, CREATED_BY FROM M_CLASS_DETAIL WHERE " + filterClassID + "GROUP BY CLASS_ID , CLASS_DESC, CLASS_DESC_EN, CLASS_DETAIL_ID, STATUS_FLAG, 1, CREATED_DATE, CREATED_BY\n";
        }

        //EXCLUDED LIST
        String excludeCriteria = "";
        if(exclude != null) {
            // ORACLE 1000 limit!
            int oracleLimit = 1000;
            String excludeList = " WHERE (";
            for (int oID = 0; oID <= exclude.length/oracleLimit; oID++) {
                if (oracleLimit * oID == exclude.length) {
                    break;
                }
                String newWhereClause = (oID==0?"":" OR ")+" CLASS_DETAIL_ID NOT IN (";
                for (int k = 0; k < (exclude.length>(oracleLimit*(oID+1))?oracleLimit:exclude.length); k++) {
                    if (k+(oracleLimit*oID) >= exclude.length){
                        break;
                    }
                    newWhereClause += (k==0?"":",")+"'" + exclude[k+(oracleLimit*oID)] + "'";
                }
                excludeList += newWhereClause + ")";
            }
            excludeCriteria += excludeList+" ) ";
        }
        excludeCriteria += " GROUP BY CLASS_ID, CLASS_DESC, CLASS_DESC_EN, CLASS_DETAIL_ID, STATUS_FLAG, score, total, CREATED_DATE, CREATED_BY ORDER BY score DESC, ";
        if(orderBy!="") {
            excludeCriteria += orderBy + " " + orderType;
        }
        String query = "SELECT CLASS_ID, CLASS_DESC, CLASS_DESC_EN, CLASS_DETAIL_ID, SUM(total) OVER() AS total1, " +
                "STATUS_FLAG, CREATED_DATE, CREATED_BY FROM (" + stringinput + " )" + additionalCriteria +excludeCriteria;
//        System.out.println("dump query " + query);
        //UNION multi criteria
        //disini ujungnya pake SUM
        searchResult.setList(mClassDetailCustomRepository.searchRawQuery("SELECT CLASS_ID, CLASS_DESC, CLASS_DESC_EN, CLASS_DETAIL_ID, SUM(total) OVER() AS total1, STATUS_FLAG, CREATED_DATE, CREATED_BY FROM (" + stringinput + " )" +additionalCriteria +excludeCriteria, offset, limit));
        //long numlist = mClassDetailCustomRepository.searchRawQueryCount(stringinput+excludeCriteria);
        if(searchResult==null || searchResult.getList().size()==0){
            searchResult.setCount(0);
        }else {
            searchResult.setCount(Integer.parseInt(searchResult.getList().get(0)[4].toString()));
        }
        return searchResult;
    }

/*
	//    @Cacheable("searchResult")
    public GenericSearchWrapper<MClassDetail> searchFullText(List<KeyValue> searchCriteria, String[] exclude, String orderBy, String orderType, Integer offset, Integer limit) {
        GenericSearchWrapper<MClassDetail> searchResult = new GenericSearchWrapper<>();
        searchResult.setCount(mClassDetailCustomRepository.countFullText(searchCriteria, exclude));
        if (searchResult.getCount() > 0) {
            searchResult.setList(mClassDetailCustomRepository.searchFullText("JOIN FETCH c.parentClass pc ", searchCriteria, exclude, orderBy, orderType, offset, limit));
        }
        if (searchResult.getList() != null)
            return searchResult;
        else
            return null ;
    }
*/

    @Cacheable("searchResult")
    public GenericSearchWrapper<MClassDetail> searchClassChildExclude(List<KeyValue> searchCriteria, String[] exclude, String orderBy, String orderType, Integer offset, Integer limit) {
        GenericSearchWrapper<MClassDetail> searchResult = new GenericSearchWrapper<>();
        searchResult.setCount(mClassDetailCustomRepository.countAllExclude(searchCriteria, exclude));
        if (searchResult.getCount() > 0) {
            searchResult.setList(mClassDetailCustomRepository.selectAllExclude("JOIN FETCH c.parentClass pc ", searchCriteria, exclude, orderBy, orderType, offset, limit));
        }
        return searchResult;
    }

    @Cacheable("searchResult")
    public GenericSearchWrapper<TxTmClass> searchClassChildLimitationExclude(List<KeyValue> searchCriteria, String[] exclude, String orderBy, String orderType, Integer offset, Integer limit) {
        GenericSearchWrapper<TxTmClass> searchResult = new GenericSearchWrapper<TxTmClass>();
        searchResult.setCount(txTmClassCustomRepository.countAllExclude(searchCriteria, exclude));
        searchResult.setList(txTmClassCustomRepository.selectAllExclude(searchCriteria, exclude, orderBy, orderType, offset, limit));
        return searchResult;
    }

    @Transactional
    public MClassDetail saveOrUpdate(MClassDetail mClassDetail) {
        return mClassDetailCustomRepository.saveOrUpdate(mClassDetail);
    }


    @Transactional
    public void reIndex() {
        mClassDetailCustomRepository.reIndex();
    }

    @Transactional(propagation = Propagation.NESTED)
    public void saveOrUpdateN(MClassDetail mClassDetail) {
        mClassDetailCustomRepository.saveOrUpdate(mClassDetail);
    }

    @Transactional(propagation = Propagation.NESTED)
    public void saveOrUpdateTxTmClassN(TxTmClass t) {
        txTmClassCustomRepository.saveOrUpdate(t);
    }

    @Transactional(propagation = Propagation.NESTED)
    public void updateMigrasiClassProcessedN(String class_no, String applicationNo) {
        txTmClassCustomRepository.modifiedMigrasiClass(class_no, applicationNo);
    }

    @Transactional(propagation = Propagation.NESTED)
    public void deleteTxTMClassByValueN(String by, String value) {
        txTmClassCustomRepository.delete(by, value);
    }

    @Transactional(propagation = Propagation.NESTED)
    public void saveClassChildN(MClassDetail mClassDetail) {
        mClassDetailRepository.save(mClassDetail);
    }

    @Transactional
    public void processedMigrasiClass(ArrayList<MClassDetail> listMClassDetail, ArrayList<TxTmClass> listTxTmClass) {
        try {
            int countmClassDetail = 1;
            for (MClassDetail mClassDetail : listMClassDetail) {
//                System.out.println("mClassDetail -" + mClassDetail.getId() + "; total : " + countmClassDetail);
                mClassDetailCustomRepository.saveOrUpdate(mClassDetail);
                countmClassDetail++;
            }

            HashMap<String, String> hMgClass = new HashMap<String, String>();
            int counttxTmClass = 1;
            for (TxTmClass txTmClass : listTxTmClass) {
//                System.out.println("TxTmClass -" + txTmClass.getmClassDetail().getId() + "; total : " + counttxTmClass);
                txTmClassCustomRepository.saveOrUpdate(txTmClass);
                counttxTmClass++;

                //        		System.out.println("Migrasi Class -"+String.valueOf(txTmClass.getmClass().getNo())  + "; " + txTmClass.getTxTmGeneral().getApplicationNo());
                //    			txTmClassCustomRepository.modifiedMigrasiClass(String.valueOf(txTmClass.getmClass().getNo()) ,txTmClass.getTxTmGeneral().getApplicationNo());

                if (!hMgClass.containsKey(String.valueOf(txTmClass.getmClass().getNo()) + "_" + txTmClass.getTxTmGeneral().getApplicationNo())) {
                    hMgClass.put(String.valueOf(txTmClass.getmClass().getNo()) + "_" + txTmClass.getTxTmGeneral().getApplicationNo(),
                            String.valueOf(txTmClass.getmClass().getNo()) + ";" + txTmClass.getTxTmGeneral().getApplicationNo());
                }
            }

            int countMigrasiClass = 1;
            for (Map.Entry<String, String> e : hMgClass.entrySet()) {

                String[] parts = e.getValue().split(";"); //parts[0] -> class_no, parts[1] -> application_no

//                System.out.println("Migrasi Class -" + parts[0] + "; " + parts[1] + "total : " + countMigrasiClass);
                txTmClassCustomRepository.modifiedMigrasiClass(parts[0], parts[1]);
                countMigrasiClass++;
            }
        } catch (Exception e) {
//            System.out.println(e);
        }

    }


    /*----------------------------------------*/

    /* --------  TxTmClass Section -------- */

    @Transactional
    public List<TxTmClass> selectAllClassByGeneralId(String generalId) {
        return txTmClassCustomRepository.selectAll("INNER JOIN FETCH c.txTmGeneral tg" +
                "LEFT JOIN FETCH c.mClass mc " +
                "LEFT JOIN FETCH c.mClassDetail mcd " +
                " ", "txTmGeneral.id", generalId, false, 0, 1000);
    }

    public TxTmClass selectOneByMClassDetailId(String id) {
        return txTmClassCustomRepository.selectOne(
//                "LEFT JOIN FETCH c.txTmGeneral tg" +
//                "LEFT JOIN FETCH c.mClass mc " +
                "LEFT JOIN FETCH c.mClassDetail mcd ", "mClassDetail.id", id, true);
    }

    public TxTmClass selectOneById(String id) {
        return txTmClassCustomRepository.selectOne("LEFT JOIN FETCH c.txTmGeneral tg" +
                "LEFT JOIN FETCH c.mClass mc " +
                "LEFT JOIN FETCH c.mClassDetail mcd ", "id", id, true);
    }

    public List<TxTmClass> selectAllClassByIdGeneral(String generalId, String status) {
        return txTmClassCustomRepository.selectAllClassByGeneralId(generalId, status);
    }

    public TxTmClass selectOneByAppId(String appId) {
        return txTmClassCustomRepository.selectOne("txTmGeneral.id", appId);
    }

    @Transactional
    public void saveOrUpdateTxTmClass(TxTmClass t) {
        txTmClassCustomRepository.saveOrUpdate(t);

        StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + t.getCurrentId());
        sb.append(", Application No: " + t.getTxTmGeneral().getApplicationNo());
        sb.append(", Kelas: " + t.getmClass().getNo() + "-" + t.getmClass().getDesc());
        sb.append(", Kelas Detail: " + t.getmClassDetail().getClassBaseNo() + '-' + t.getmClassDetail().getDesc());

    }

    @Transactional
    public void saveTxTmClass2(TxTmClass txTmClass) {
        txTmClassRepository.save(txTmClass);
    }

    @Transactional
    public void deleteTxTMClassByValue(String by, String value) {
        txTmClassCustomRepository.delete(by, value);
    }

    public void saveTxTMClass(TxTmClass t) {
        txTmClassRepository.save(t);

        StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + t.getCurrentId());
        sb.append(", Application No: " + t.getTxTmGeneral().getApplicationNo());
        sb.append(", Kelas: " + t.getmClass().getNo() + "-" + t.getmClass().getDesc());
        sb.append(", Kelas Detail: " + t.getmClassDetail().getClassBaseNo() + '-' + t.getmClassDetail().getDesc());

    }

    @Transactional
    public void saveTxTMClass(TxTmClass[] t) {
        txTmClassCustomRepository.saveBulk(t);
        if(t.length>0) {
            txTmGeneralCustomRepository.updateTxTmGeneralClassList(t[0].getTxTmGeneral().getId());
        }
    }

    @Transactional
    public void deleteTxTMClass(TxTmClass t) {
        txTmClassCustomRepository.delete("id", t.getId());

        StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + t.getCurrentId());
        sb.append(", Application No: " + t.getTxTmGeneral().getApplicationNo());
        sb.append(", Kelas: " + t.getmClass().getNo() + "-" + t.getmClass().getDesc());
        sb.append(", Kelas Detail: " + t.getmClassDetail().getClassBaseNo() + '-' + t.getmClassDetail().getDesc());

    }

    @Transactional
    public void deleteTxTMClassMultipleNotIn(String app_id, String[] detail_id) {
        txTmClassCustomRepository.deleteByMultipleClassDetail(app_id, detail_id);
    }
    
    @Transactional
    public void deleteTxTMClassMultipleNotIn2(String app_id, String[] detail_id) {
        txTmClassCustomRepository.deleteByMultipleClassDetail2(app_id, detail_id);
    }

    @Transactional
    public void deleteMClassDetailANDTxTMClassMultipleNotIn(String app_id, String[] detail_id) {
        txTmClassCustomRepository.deleteALLByMultipleClassDetail(app_id, detail_id);
    }
    
    @Transactional
    public void deleteMClassDetailANDTxTMClassMultipleNotIn2(String app_id, String[] detail_id) {
        txTmClassCustomRepository.deleteALLByMultipleClassDetail2(app_id, detail_id);
    }

    @Transactional
    public void deleteUnusedMClassDetail(String ownerId) {
        txTmClassCustomRepository.deleteUnusedMClassDetail(ownerId);
    }

    @Transactional
    public void deleteAllTxTmClassLimitationByAppId(String app_id) {
        txTmClassLimitationCustomRepository.deleteByAppId(app_id);
    }

    public List<Object[]> selectTxTMClassMultiple(String app_id) {
        return this.selectTxTMClassMultiple(app_id, new String[]{
                "CLASS_DETAIL_ID"
        });
    }


    public List<Object[]> selectTxTMClassMultiple(String app_id, String[] fields) {
        return txTmClassCustomRepository.selectByMultipleClassDetail(app_id, fields);
    }

    @Transactional
    public void updateTxTMClass(String id, String[] fields, Object[] datas) {
        txTmClassCustomRepository.updateClassDetail(id, fields, datas);
    }

    public GenericSearchWrapper<TxTmClass> searchGeneralTxClass(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        GenericSearchWrapper<TxTmClass> searchResult = new GenericSearchWrapper<TxTmClass>();
        searchResult.setCount(txTmClassCustomRepository.count(searchCriteria));
        searchResult.setList(txTmClassCustomRepository.selectAll("JOIN FETCH c.txTmGeneral tg " +
                " JOIN FETCH c.mClass mc " +
                " JOIN FETCH c.createdBy cb " +
                " JOIN FETCH c.mClassDetail cc ", searchCriteria, orderBy, orderType, offset, limit));

        return searchResult;
    }

    public GenericSearchWrapper<TxTmClassLimitation> searchGeneralTxClassLimitation(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        GenericSearchWrapper<TxTmClassLimitation> searchResult = new GenericSearchWrapper<>();
        searchResult.setCount(txTmClassLimitationCustomRepository.count(searchCriteria));
        searchResult.setList(txTmClassLimitationCustomRepository.selectAll("JOIN FETCH c.txTmGeneral tg " +
                " JOIN FETCH c.mClass mc " +
                " JOIN FETCH c.createdBy cb " +
                " JOIN FETCH c.mCountry mct " +
                " JOIN FETCH c.mClassDetail cc ", searchCriteria, orderBy, orderType, offset, limit));

        return searchResult;
    }

    /*----------------------------------------*/

    /* --------  TxTmClass Child Section -------- */

    public MClassDetail selectOne(String by, String value) {
        return mClassDetailCustomRepository.selectOne("LEFT JOIN FETCH c.parentClass ci ", by, value, true);
    }

    public MClassDetail selectOneMClassDetailById(String id) {
        return mClassDetailRepository.selectOneMClassDetailById(id);
    }

    public TxTmClass selectOne(String[] by, String[] value) {
        return mClassDetailCustomRepository.selectOne("LEFT JOIN FETCH c.parentClass ci ", by, value);
    }

    public List<MClassDetail> selectAllClassDetail(String by, String value) {
        return mClassDetailCustomRepository.selectAll("LEFT JOIN FETCH c.parentClass ci ", by, value, true, null, null);
    }

    public List<Object[]> selectObject() {
        return migrasiClassRepository.selectObject();
    }

//    @Transactional
//    public void deleteMClassDetail(MClassDetail mClassDetail) {
//        txTmClassCustomRepository.delete("id", mClassDetail.getId());
//    }

    public MClassDetail findOneBydescMClassDetail(String desc) {
        return mClassDetailCustomRepository.selectOne("desc", desc.toUpperCase().trim(), false);
    }

    public MClassDetail findOneBydescEnMClassDetail(String descEn) {
        return mClassDetailCustomRepository.selectOne("descEn", descEn.toUpperCase().trim(), false);
    }

    public MClassDetail findExistedClassDetail(String desc) {
        return mClassDetailCustomRepository.selectOne("desc", desc.toLowerCase().trim(), true);
    }

    public MClassDetail findExistedClassDetailEn(String descEn) {
        return mClassDetailCustomRepository.selectOne("descEn", descEn.toLowerCase().trim(), true);
    }

    /*public  List<Object[]> selectOneClass_(String classno){
    	return migrasiClassRepository.selectOneClass_(classno);
	}*/

    public long countAll(List<KeyValue> searchCriteria) {
        return mClassHeaderCustomRepository.countClass(searchCriteria);
    }

    public List<MClass> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
        return mClassHeaderCustomRepository.selectAllClass(searchCriteria, orderBy, orderType, offset, limit);
    }

    public long countAllClassDetail(List<KeyValue> searchCriteria) {
        return mClassDetailCustomRepository.count(searchCriteria);
    }

    public List<MClassDetail> selectAllClassDetail(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
        return mClassDetailCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit);
    }

    public MClassDetail selectOneClassDetail(String by, String value) {
        return mClassDetailCustomRepository.selectOne(by, value, true);
    }

    @Transactional
    public void updateInactiveClassDetail(String by, String value) {
        mClassDetailCustomRepository.updateInactive(by, value);
    }

    public List<TxTmClass> selectAllTxTmClass(List<KeyValue> searchCriteria, String orderBy, String orderType) {
        return txTmClassCustomRepository.selectAll(
                "LEFT JOIN FETCH c.mClass " +
                        "LEFT JOIN FETCH c.mClassDetail ",
                searchCriteria, orderBy, orderType, null, null);
    }

    @Transactional
    public boolean deleteMClassDetail(String noIds) {
        try {
            for (String noId : noIds.split(",")) {
//                System.out.println("START DELETE MCLASSDETAIL "+ noId);
                String mClassDetailID = mClassDetailRepository.findOne(noId).getId();
                if(mClassDetailID != "") {
                    mClassDetailRepository.deleteByMClassDetailID(mClassDetailID);
                }
            }
//            System.out.println("END DELETE MCLASSDETAIL");
        }catch(Exception e){
            //System.out.println("ERROR:"+e.toString());
            return false;
        }
        return true;
    }

    public GenericSearchWrapper<TxTmClass> selectAllClassDetailPermintaan(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
        String joinCriteria = "LEFT JOIN c.mClassDetail mcd LEFT JOIN c.mClass mc";
        GenericSearchWrapper<TxTmClass> searchResult = new GenericSearchWrapper<TxTmClass>();
        searchResult.setCount(txTmClassCustomRepository.count(joinCriteria, searchCriteria));
        if (searchResult.getCount() > 0) {
            searchResult.setList(txTmClassCustomRepository.selectAll(joinCriteria, searchCriteria, orderBy, orderType, offset, limit));
        }
        return searchResult;
    }

    public long countStatusTxTmClass(List<KeyValue> searchCriteria) {
        String joinCriteria = "LEFT JOIN c.mClassDetail mcd LEFT JOIN c.mClass mc";
        return txTmClassCustomRepository.count(joinCriteria, searchCriteria);
    }

    public boolean terimaPermintaanJenisBarangJasa(String id) {
        MClassDetail mClassDetail = mClassDetailRepository.findOne(id);
        TxTmClass txTmClass = txTmClassRepository.findOneByMClassDetail(mClassDetail);

        if (!mClassDetail.getId().isEmpty() && !txTmClass.getId().isEmpty()) {
            txTmClass.setTransactionStatus("ACCEPT");
            txTmClassRepository.save(txTmClass);

            mClassDetail.setStatusFlag(true);
            mClassDetailRepository.save(mClassDetail);

            return true;
        }

        return false;
    }

    public boolean tolakPermintaanJenisBarangJasa(String id, String alasan) {
        MClassDetail mClassDetail = mClassDetailRepository.findOne(id);
        TxTmClass txTmClass = txTmClassRepository.findOneByMClassDetail(mClassDetail);
        if (txTmClass!=null && mClassDetail!=null) {
            if (!mClassDetail.getId().isEmpty() && !txTmClass.getId().isEmpty()) {
                txTmClassRepository.delete(txTmClass.getId());
                // tidak hapus mClassDetail
                //mClassDetailRepository.delete(id);
                mClassDetail.setNotes(alasan);
                mClassDetail.setStatusFlag(false); //status = 0
                mClassDetailRepository.save(mClassDetail);

                return true;
            }
        }

        return false;
    }

    public List<Object[]> selectAllClassDetailForExport(String classId, List<KeyValue> searchCriteria, String orderBy, String orderType, int retrieved, int limit) {
        String filterQuery = "";
        if (searchCriteria != null) {
            for (int i = 0; i < searchCriteria.size(); i++) {
                String searchBy = searchCriteria.get(i).getKey();
                Object value = searchCriteria.get(i).getValue();
                if (searchBy.equals("parentClass.id")) {
                    continue;
                }

                if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                    if (value == null) {
                        filterQuery += " AND " + searchBy + " IS NULL";
                    } else if (value instanceof String) {
                        if (searchCriteria.get(i).isExactMatch()) {
                            filterQuery += " AND LOWER(" + searchBy + ") = '" + value + "' ";
                        } else {
                            if (searchBy == "desc") {
                                searchBy = "CLASS_DESC";
                            }
                            filterQuery += " AND LOWER(" + searchBy + ") LIKE '%" + value + "%' ";
                        }
                    }
                }
            }
        }
        if (orderBy == "createdDate") {
            orderBy = "TO_CHAR(m_class_detail.CREATED_DATE, 'YYYY-MM-DD')";
        }
        String rawQuery ="SELECT * FROM (SELECT rownum AS rnum, t.* FROM (SELECT "
            + "COALESCE(CLASS_BASE_NO, '-') as classBaseNo, "
            + "m_user.USERNAME as createdBy, "
            + "TO_CHAR(m_class_detail.CREATED_DATE,'DD-MM-YYYY') as createdDate, "
            + "CLASS_DESC AS \"desc\", "
            + "CLASS_DESC_EN AS descEn, "
            + "COALESCE(CLASS_SERIAL_1, '-') AS serial1, "
            + "COALESCE(CLASS_SERIAL_2, '-') AS serial2, "
            + "(CASE STATUS_FLAG WHEN 1 THEN 'Aktif' ELSE 'Tidak Aktif' END) AS status "
            + "FROM m_class_detail \r\n"
            + "LEFT JOIN m_user ON m_user.USER_ID = m_class_detail.CREATED_BY "
            + "WHERE lower(m_class_detail.class_id)='"+ classId +"' "
            + filterQuery
            + "ORDER BY " + orderBy + " " + orderType + ") t "
            + "WHERE rownum <= " + retrieved + " + " + limit + ") WHERE rnum > " + retrieved;
        return mClassDetailCustomRepository.searchRawQuery(rawQuery, 0, 0);
    }
}