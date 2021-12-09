package com.docotel.ki.pojo;

import java.io.Serializable;
import java.util.List;

public class NativeQueryModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/

    private String table_name ;
    private String type_query;  // SELECT , UPDATE, INSERT , DELETE
    private List<KeyValue> updateQ ;
    private List<KeyValueSelect> searchBy;
    private String[] resultcol;
    private String order_by ;
    private int limit ;
    private int offset ;




    public NativeQueryModel() {
    }



    public NativeQueryModel(String table_name, String type_query, List <KeyValueSelect> searchBy, String[] resultcol, String order_by, int limit, int offset) {
        this.table_name = table_name;
        this.type_query = type_query;
        this.searchBy = searchBy;
        this.resultcol = resultcol;
        this.order_by = order_by;
        this.limit = limit;
        this.offset = offset;
    }

    public List<KeyValue> getUpdateQ() {
        return updateQ;
    }

    public void setUpdateQ(List<KeyValue> updateQ) {
        this.updateQ = updateQ;
    }

    public String getTable_name() {
        return table_name;
    }

    public void setTable_name(String table_name) {
        this.table_name = table_name;
    }



    public String getType_query() {
        return type_query;
    }

    public void setType_query(String type_query) {
        this.type_query = type_query;
    }

    public List<KeyValueSelect> getSearchBy() {
        return searchBy;
    }

    public void setSearchBy(List<KeyValueSelect> searchBy) {
        this.searchBy = searchBy;
    }

    public String[] getResultcol() {
        return resultcol;
    }

    public void setResultcol(String[] resultcol) {
        this.resultcol = resultcol;
    }

    public String getOrder_by() {
        return order_by;
    }

    public void setOrder_by(String order_by) {
        this.order_by = order_by;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
