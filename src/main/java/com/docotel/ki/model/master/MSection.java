package com.docotel.ki.model.master;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

import javax.persistence.*;

import com.docotel.ki.model.BaseModel;
import com.docotel.ki.util.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.docotel.ki.audittrail.EntityAuditTrailListener;
import org.springframework.security.core.context.SecurityContextHolder;

@Entity
@Table(name = "m_section")
@EntityListeners(EntityAuditTrailListener.class)
public class MSection extends BaseModel implements Serializable {
	/***************************** - FIELD SECTION - ****************************/
	private String id;
	private MDepartment mDepartment;
	private String code;
	private String name;
	private String desc;
	private boolean statusFlag;

	/***************************** - TRANSIENT FIELD SECTION - ****************************/

	/***************************** - CONSTRUCTOR SECTION - ****************************/
	public MSection() {
	}

	public MSection(MSection mSection) {
		if (mSection != null) {
			setId(mSection.getCurrentId());
			if (mSection.getmDepartment() != null) {
				setmDepartment(new MDepartment(mSection.getmDepartment()));
			}
			setCode(mSection.getCode());
			setName(mSection.getName());
			setDesc(mSection.getDesc());
			setStatusFlag(mSection.isStatusFlag());
			if (mSection.getCreatedBy() != null) {
				setCreatedBy(new MUser(mSection.getCreatedBy()));
			}
			setCreatedDate(mSection.getCreatedDate());
		}
	}

	/***************************** - GETTER SETTER METHOD SECTION - ****************************/
	@Id
	@Column(name = "sec_id", length = 36)
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
	@JoinColumn(name = "dept_id", nullable = false)
	public MDepartment getmDepartment() {
		return mDepartment;
	}

	public void setmDepartment(MDepartment mDepartment) {
		this.mDepartment = mDepartment;
	}
	
	@Column(name = "sec_code", length = 10, nullable = false)
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Column(name = "sec_name", length = 255, nullable = false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "sec_desc", length = 1000)
	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by", nullable = false, updatable = false)
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
		sb.append("^ Kode Seksi: " + getCode());
		sb.append("^ Nama Seksi: " + getName());
		sb.append("^ Deskripsi: " + getDesc());
		sb.append("^ Subdit: " + getmDepartment().getName());
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
