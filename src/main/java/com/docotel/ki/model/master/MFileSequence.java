package com.docotel.ki.model.master;

import com.docotel.ki.model.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.docotel.ki.audittrail.EntityAuditTrailListener;
import com.docotel.ki.enumeration.UserEnum;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "m_file_seq")
@EntityListeners(EntityAuditTrailListener.class)
public class MFileSequence extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private String code;
    private String desc;
    private boolean kanwilFlag;
    private boolean statusFlag;

	/***************************** - TRANSIENT FIELD SECTION - ****************************/

    /***************************** - CONSTRUCTOR SECTION - ****************************/
    public MFileSequence() {
    }

    public MFileSequence(MFileSequence mFileSequence) {
        if (mFileSequence != null) {
            setId(mFileSequence.getCurrentId());
            setCode(mFileSequence.getCode());
            setDesc(mFileSequence.getDesc());
            setKanwilFlag(mFileSequence.isKanwilFlag());
            setStatusFlag(mFileSequence.isStatusFlag());
            if (mFileSequence.getCreatedBy() != null) {
	            setCreatedBy(new MUser(mFileSequence.getCreatedBy()));
            }
            setCreatedDate(mFileSequence.getCreatedDate());
        }
    }

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "file_seq_id", length = 36)
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "file_seq_code", length = 10, nullable = false, unique = true)
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Column(name = "file_seq_desc", length = 255, nullable = false)
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

    @Column(name = "kanwil_flag", nullable = false)
    public boolean isKanwilFlag() {
        return kanwilFlag;
    }

    public void setKanwilFlag(boolean kanwilFlag) {
    	this.kanwilFlag = kanwilFlag;
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
			Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			if(principal instanceof  UserDetails) {
				createdBy = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        	} else {
        		createdBy = (MUser) UserEnum.SYSTEM.value();
        	}
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
			Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			if(principal instanceof  UserDetails) {
				updatedBy = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        	} else {
        		updatedBy = (MUser) UserEnum.SYSTEM.value();
        	}
		}
        return updatedBy;
    }

    public void setUpdatedBy(MUser updatedBy) {
        this.updatedBy = updatedBy;
    }

    /***************************** - OTHER METHOD SECTION - ****************************/
    @Transient
    public String getCurrentId() {
        return this.id;
    }

    @Override
    public String logAuditTrail() {
        StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + getCurrentId());
        sb.append("^ Kode Asal Permohonan: " + getCode());
        sb.append("^ Asal Permohonan: " + getDesc());
        sb.append("^ Kanwil: " + (isKanwilFlag() ? "Ya" : "Tidak"));
        sb.append("^ Status: " + (isStatusFlag() ? "Aktif" : "Tidak Aktif"));
        return sb.toString();
    }
}
