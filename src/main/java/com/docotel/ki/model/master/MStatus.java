package com.docotel.ki.model.master;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

import javax.persistence.*;

import com.docotel.ki.model.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.docotel.ki.audittrail.EntityAuditTrailListener;
import org.springframework.security.core.context.SecurityContextHolder;

@Entity
@Table(name = "m_status")
@EntityListeners(EntityAuditTrailListener.class)
public class MStatus extends BaseModel implements Serializable {
	/***************************** - FIELD SECTION - ****************************/
	private String id;
	private String code;
	private String name;
	private boolean statusFlag;
	private boolean staticFlag;
	private boolean lockedFlag;

	/***************************** - TRANSIENT FIELD SECTION - ****************************/

	/***************************** - CONSTRUCTOR SECTION - ****************************/
	public MStatus() {
	}

	public MStatus(String id) {
		this.id = id;
	}

	/***************************** - GETTER SETTER METHOD SECTION - ****************************/
	@Id
	@Column(name = "status_id", length = 36)
	public String getId() {
		if (id == null) {
			id = UUID.randomUUID().toString();
		}
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name = "status_code", length = 10, nullable = false)
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Column(name = "status_name", length = 255, nullable = false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		try {
			this.name = name.trim();
		} catch (NullPointerException e) {
			this.name = null;
		}
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
    
	@Column(name = "status_flag", nullable = false)
	public boolean isStatusFlag() {
		return statusFlag;
	}

	public void setStatusFlag(boolean statusFlag) {
		this.statusFlag = statusFlag;
	}

	@Column(name = "static_flag", nullable = false)
	public boolean isStaticFlag() {
		return staticFlag;
	}

	public void setStaticFlag(boolean staticFlag) {
		this.staticFlag = staticFlag;
	}

	@Column(name = "locked_flag", nullable = false)
	public boolean isLockedFlag() {
		return lockedFlag;
	}

	public void setLockedFlag(boolean lockedFlag) {
		this.lockedFlag = lockedFlag;
	}

	/***************************** - GETTER SETTER TRANSIENT METHOD SECTION - ****************************/

	/***************************** - OTHER METHOD SECTION - ****************************/
	@Transient
	public String getCurrentId() {
		return this.id;
	}

	@Override
	public String logAuditTrail() {
		StringBuilder sb = new StringBuilder();
		sb.append("UUID: " + getCurrentId());
		sb.append("^ Kode Status: " + getCode());
		sb.append("^ Nama Status: " + getName());
		sb.append("^ Static: " + (isStaticFlag() ? "Ya" : "Tidak"));
		sb.append("^ Status: " + (isStatusFlag() ? "Aktif" : "Tidak Aktif"));
		return sb.toString();
	}
}
