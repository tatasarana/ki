package com.docotel.ki.model.master;

import com.docotel.ki.model.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.docotel.ki.audittrail.EntityAuditTrailListener;

import javax.persistence.*;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "m_role_subs")
@EntityListeners(EntityAuditTrailListener.class)
public class MRoleSubstantif extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private String name;
    private String desc;
    private boolean statusFlag;
    
    private List<MRoleSubstantifDetail> mRoleSubstantifDetailList;

	/***************************** - TRANSIENT FIELD SECTION - ****************************/

    /***************************** - CONSTRUCTOR SECTION - ****************************/
    public MRoleSubstantif() {
    }

	public MRoleSubstantif(MRoleSubstantif mRoleSubstantif) {
		if (mRoleSubstantif != null) {
			setId(mRoleSubstantif.getCurrentId());
			setName(mRoleSubstantif.getName());
			setDesc(mRoleSubstantif.getDesc());
			setStatusFlag(mRoleSubstantif.isStatusFlag());
			if (mRoleSubstantif.getCreatedBy() != null) {
				setCreatedBy(new MUser(mRoleSubstantif.getCreatedBy()));
			}
			setCreatedDate(mRoleSubstantif.getCreatedDate());
			if (mRoleSubstantif.getmRoleSubstantifDetailList() != null) {
				setmRoleSubstantifDetailList(new ArrayList<>());
				for (MRoleSubstantifDetail mRoleSubstantifDetail : mRoleSubstantif.getmRoleSubstantifDetailList()) {
					getmRoleSubstantifDetailList().add(new MRoleSubstantifDetail(mRoleSubstantifDetail));
				}
			}
		}
	}

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "role_subs_id", length = 36)
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "role_subs_name", length = 100, nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "role_subs_desc", length = 100, nullable = false)
    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
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
    
    @OneToMany(mappedBy = "mRoleSubstantif", cascade = CascadeType.DETACH, fetch = FetchType.LAZY, orphanRemoval = true)
    @Fetch(value = FetchMode.SUBSELECT)
	public List<MRoleSubstantifDetail> getmRoleSubstantifDetailList() {
		return mRoleSubstantifDetailList;
	}

	public void setmRoleSubstantifDetailList(List<MRoleSubstantifDetail> mRoleSubstantifDetailList) {
		this.mRoleSubstantifDetailList = mRoleSubstantifDetailList;
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
        sb.append("^ Nama Role Substantif: " + getName());
        sb.append("^ Deskripsi Role Substantif: " + getDesc());
        sb.append("^ Status: " + (isStatusFlag() ? "Aktif" : "Tidak Aktif"));
        return sb.toString();
    }
}