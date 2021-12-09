package com.docotel.ki.model.master;

import com.docotel.ki.model.BaseModel;
import com.docotel.ki.util.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.docotel.ki.audittrail.EntityAuditTrailListener;

import org.apache.commons.text.WordUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "m_province")
@EntityListeners(EntityAuditTrailListener.class)
public class MProvince extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private String name;
    private MCountry mCountry;
    private String code;
    private boolean statusFlag;

    /***************************** - TRANSIENT FIELD SECTION - ****************************/
    private String active;

	/***************************** - CONSTRUCTOR SECTION - ****************************/
    public MProvince() {
    }

    public MProvince(MProvince mProvince) {
        if (mProvince != null) {
            setId(mProvince.getCurrentId());
            setCode(mProvince.getCode());
            setName(mProvince.getName());
            setStatusFlag(mProvince.isStatusFlag());
            if (mProvince.getmCountry() != null) {
            	setmCountry(new MCountry(mProvince.getmCountry()));
            }
            if (mProvince.getCreatedBy() != null) {
                setCreatedBy(new MUser(mProvince.getCreatedBy()));
            }
            setCreatedDate(mProvince.getCreatedDate());
        }
    }

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "prov_id", length = 36)
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "prov_name", length = 150, nullable = false, unique = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        try {
            this.name = WordUtils.capitalize(name.trim());
        } catch (NullPointerException e) {
            this.name = null;
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    public MCountry getmCountry() {
        return mCountry;
    }

    public void setmCountry(MCountry mCountry) {
        this.mCountry = mCountry;
    }

    @Column(name = "prov_code", length = 2, nullable = false, unique = true)
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        try {
            this.code = code.trim().toUpperCase();
        } catch (NullPointerException e) {
            this.code = null;
        }
    }

    @Column(name = "status_flag", nullable = false)
    public boolean isStatusFlag() {
        return statusFlag;
    }

    public void setStatusFlag(boolean statusFlag) {
        this.statusFlag = statusFlag;
        this.active = "" + this.statusFlag;
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
    
    /***************************** - TRANSIENT METHOD SECTION - ****************************/
    @Transient
	public String getActive() {
		return active;
	}

	public void setActive(String active) {
		this.active = active;
		this.statusFlag = this.active.equalsIgnoreCase("true");
	}

	/***************************** - OTHER METHOD SECTION - ****************************/
    @Transient
    public String getCurrentId() {
        return this.id;
    }
	
	@Transient
    public String status() {
        return (isStatusFlag() ? "Aktif" : "Tidak Aktif");
    }

    @Override
    public String logAuditTrail() {
        StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + getCurrentId());
        sb.append("^ Kode Provinsi: " + getCode());
        sb.append("^ Name: " + getName());
        sb.append("^ Negara: " + getmCountry().getName());
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
