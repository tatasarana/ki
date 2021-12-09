package com.docotel.ki.model.transaction;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.docotel.ki.model.BaseModel;
import com.docotel.ki.model.master.MDocType;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.audittrail.EntityAuditTrailListener;

@Entity
@Table(name = "tx_post_doc")
@EntityListeners(EntityAuditTrailListener.class)
public class TxPostDoc extends BaseModel implements Serializable {
	/***************************** - FIELD SECTION - ****************************/
    private String id;
    private TxPostReception txPostReception;
    private MDocType mDocType;
    private String description;
    private boolean status;
    private Timestamp uploadDate;
    private String fileName;
    private String fileNameTemp;
    private String fileSize;
    private MUser createdBy;
    private Timestamp createdDate;

    /***************************** - TRANSIENT FIELD SECTION - ****************************/
    private String fileDoc;

    /***************************** - CONSTRUCTOR SECTION - ****************************/

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "post_doc_id", length = 36)
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_reception_id", nullable = false)
    public TxPostReception getTxPostReception() {
        return txPostReception;
    }

    public void setTxPostReception(TxPostReception txPostReception) {
        this.txPostReception = txPostReception;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_type_id", nullable = false)
    public MDocType getmDocType() {
        return mDocType;
    }

    public void setmDocType(MDocType mDocType) {
        this.mDocType = mDocType;
    }

    @Column(name = "post_doc_desc", length = 4000)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "post_doc_status", nullable = false)
    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    @Column(name = "post_doc_upload_date")
    public Timestamp getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Timestamp uploadDate) {
        this.uploadDate = uploadDate;
    }

    @Column(name = "post_doc_filename")
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Column(name = "post_doc_filename_temp")
    public String getFileNameTemp() {
        return fileNameTemp;
    }

    public void setFileNameTemp(String fileNameTemp) {
        this.fileNameTemp = fileNameTemp;
    }

    @Column(name = "post_doc_file_size")
    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    public MUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(MUser createdBy) {
        this.createdBy = createdBy;
    }

    @Column(name = "created_date", nullable = false)
    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    /***************************** - GETTER SETTER TRANSIENT METHOD SECTION - *****************************/
    @Transient
    public String getCurrentId() {
        return this.id;
    }
    
    @Transient
    public String getFileDoc() {
        return this.fileDoc;
    }
    
    public void setFileDoc(String fileDoc) {
    	this.fileDoc = fileDoc;
    }
    
    /***************************** - OTHER METHOD SECTION - ****************************/

    @Override
    public String logAuditTrail() {
        StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + getCurrentId());
        sb.append("^ Nomor Dokumen: " + getTxPostReception().getPostNo());
        sb.append("^ Tipe Dokumen: " + getmDocType().getName());
        sb.append("^ Nama File: " + getFileName());
        sb.append("^ Deskripsi: " + getDescription());
        return sb.toString();
    }
}
