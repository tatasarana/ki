package com.docotel.ki.model.transaction;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "tx_document_params")
/*@EntityListeners(EntityAuditTrailListener.class)*/
public class TxDocumentParams implements Serializable {

    private String id;
    private String applicationNo;
    private String documentId;
    private String paramName;
    private String paramValue;
    private String paramType;

    /***************************** - OTHER METHOD SECTION - ****************************/

    @Transient
    public String getCurrentId() {
        return this.getId();
    }

    /*@Override
    public String logAuditTrail() {
        StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + getCurrentId());
        sb.append("Nomor Permohonan: " + getApplicationNo());
        return sb.toString();
    }*/


    @Id
    @Column(name = "id", length = 36)
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }

    public String getApplicationNo() {
        return applicationNo;
    }

    public void setApplicationNo(String applicationNo) {
        this.applicationNo = applicationNo;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    @Column(length = 10000)
    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }
}
