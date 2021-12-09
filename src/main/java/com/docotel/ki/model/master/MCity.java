package com.docotel.ki.model.master;

import com.docotel.ki.model.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.docotel.ki.audittrail.EntityAuditTrailListener;
import org.apache.commons.text.WordUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "m_city")
@EntityListeners(EntityAuditTrailListener.class)
public class MCity extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
    public String id;
    public String name;
    public MProvince mProvince;
    private String code;
    private boolean statusFlag;

    /***************************** - TRANSIENT FIELD SECTION - ****************************/
    private String active;

    /***************************** - CONSTRUCTOR SECTION - ****************************/
    public MCity() {
    }

	public MCity(MCity mCity) {
		if (mCity != null) {
			setId(mCity.getCurrentId());
			setCode(mCity.getCode());
			setName(mCity.getName());
			setStatusFlag(mCity.isStatusFlag());
			if (mCity.getmProvince() != null) {
				setmProvince(new MProvince(mCity.getmProvince()));
			}
			if (mCity.getCreatedBy() != null) {
				setCreatedBy(new MUser(mCity.getCreatedBy()));
			}
			setCreatedDate(mCity.getCreatedDate());
		}
	}
	
    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "city_id", length = 36)
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "city_name", length = 255, nullable = false, unique = true)
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
    @JoinColumn(name = "prov_id", nullable = false)
    public MProvince getmProvince() {
        return mProvince;
    }

    public void setmProvince(MProvince mProvince) {
        this.mProvince = mProvince;
    }

	@Column(name = "city_code", length = 6, nullable = false, unique = true)
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

    @Override
    public String logAuditTrail() {
        StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + getCurrentId());
        sb.append("^ Nama Provinsi: " + getmProvince().getName());
        sb.append("^ Kode Kota: " + getCode());
        sb.append("^ Nama Kota: " + getName());
        sb.append("^ Status: " + (isStatusFlag() ? "Aktif" : "Tidak Aktif"));
        return sb.toString();
    }
}
