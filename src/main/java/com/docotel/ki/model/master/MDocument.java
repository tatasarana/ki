package com.docotel.ki.model.master;

import com.docotel.ki.model.BaseModel;

import com.docotel.ki.audittrail.EntityAuditTrailListener;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name="m_document")
@EntityListeners(EntityAuditTrailListener.class)
public class MDocument extends BaseModel implements Serializable {

    private String id;
    private String name;
    private String filePath;
    private MUser createdBy;
    private Timestamp createdDate;
    private boolean statusFlag;

    @Id
    @Column(name = "document_id", length = 36)
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        if (createdDate == null) {
            createdDate =  new Timestamp(System.currentTimeMillis());
        }
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Column(name = "status_flag", nullable = false)
    public boolean isStatusFlag() {
        return statusFlag;
    }

    public void setStatusFlag(boolean statusFlag) {
        this.statusFlag = statusFlag;
    }
    
    /***************************** - OTHER METHOD SECTION - ****************************/
    
    @Transient
    public String getCurrentId() {
    	return this.id;
    }

	@Override
	public String logAuditTrail() {
		StringBuilder sb = new StringBuilder();
		sb.append("UUID: "+ getCurrentId());
		sb.append("^ Nama File: "+ getName());
		sb.append("^ Lokasi Penyimpanan: "+ getFilePath());
        sb.append("^ Status: " + (isStatusFlag() ? "Aktif" : "Tidak Aktif"));
		return sb.toString();
	}

}
