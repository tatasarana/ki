package com.docotel.ki.model.master;

import com.docotel.ki.model.BaseModel;
import com.docotel.ki.util.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.docotel.ki.audittrail.EntityAuditTrailListener;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "m_law")
@EntityListeners(EntityAuditTrailListener.class)
public class MLaw extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private String desc;
    private int no;
    private int year;
    private boolean statusFlag;

    /***************************** - TRANSIENT FIELD SECTION - ****************************/

    /***************************** - CONSTRUCTOR SECTION - ****************************/
    public MLaw() {
    }

    public MLaw(MLaw mLaw) {
        if (mLaw != null) {
	        setId(mLaw.getCurrentId());
	        setDesc(mLaw.getDesc());
	        setNo(mLaw.getNo());
	        setYear(mLaw.getYear());
	        setStatusFlag(mLaw.isStatusFlag());
	        if (mLaw.getCreatedBy() != null) {
	        	setCreatedBy(new MUser(mLaw.getCreatedBy()));
	        }
	        setCreatedDate(mLaw.getCreatedDate());
        }
    }

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "law_id", length = 36)
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "law_desc", length = 255, nullable = false)
    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        try {
            this.desc = desc.trim();
        } catch (NullPointerException e) {
            this.desc = null;
        }
    }

    @Column(name = "law_number", nullable = false, unique = true)
    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    @Column(name = "law_year", nullable = false)
    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    @Column(name = "status_flag", nullable = false)
    public boolean isStatusFlag() {
        return statusFlag;
    }

    public void setStatusFlag(boolean statusFlag) {
        this.statusFlag = statusFlag;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    @JsonIgnore
    public MUser getCreatedBy() {
        if (createdBy == null) {
            createdBy = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }
        return createdBy;
    }

    public void setCreatedBy(MUser createdBy) {
        this.createdBy = createdBy;
    }

    @Column(name = "created_date", nullable = false, updatable = false)
    public Timestamp getCreatedDate() {
        if (createdDate == null) {
            createdDate = new Timestamp(System.currentTimeMillis());
        }
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    @Column(name = "updated_date")
    public Timestamp getUpdatedDate() {
        if (updatedDate == null) {
            updatedDate = new Timestamp(System.currentTimeMillis());
        }
        return updatedDate;
    }

    public void setUpdatedDate(Timestamp updatedDate) {
        this.updatedDate = updatedDate;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    @JsonIgnore
    public MUser getUpdatedBy() {
        if (updatedBy == null) {
            updatedBy = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }
        return updatedBy;
    }

    public void setUpdatedBy(MUser updatedBy) {
        this.updatedBy = updatedBy;
    }

    /***************************** - OTHER METHOD SECTION - ****************************/

    @Transient
    public String getCurrentId() {
        return id;
    }
    
    @Transient
    public String status() {
        return (isStatusFlag() ? "Aktif" : "Tidak Aktif");
    }

    @Override
    public String logAuditTrail() {
        StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + getCurrentId());
        sb.append("^ Nomor Undang-undang: " + getNo());
        sb.append("^ Deskripsi: " + getDesc());
        sb.append("^ Tahun: " + getYear());
        sb.append("^ Status: " + (isStatusFlag() ? "Aktif" : "Tidak Aktif"));
        return sb.toString();
    }

	public String createdDate() {
    	if (getCreatedDate() != null) {
		    DateUtil.formatDate(getCreatedDate(), "dd-MM-yyyy");
	    }
		return "";
	}
}
