package com.docotel.ki.model.master;

import com.docotel.ki.model.BaseModel;
import com.docotel.ki.util.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.docotel.ki.audittrail.EntityAuditTrailListener;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "m_department")
@EntityListeners(EntityAuditTrailListener.class)
public class MDepartment extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private MDivision mDivision;
    private String code;
    private String name;
    private String desc;
    private boolean statusFlag;
    private List<MSection> mSectionList;

	/***************************** - TRANSIENT FIELD SECTION - ****************************/

    /***************************** - CONSTRUCTOR SECTION - ****************************/
    public MDepartment() {
    }

    public MDepartment(MDepartment mDepartment) {
        if (mDepartment != null) {
            setId(mDepartment.getCurrentId());
            if (mDepartment.getmDivision() != null) {
                setmDivision(new MDivision(mDepartment.getmDivision()));
            }
            setCode(mDepartment.getCode());
            setName(mDepartment.getName());
            setDesc(mDepartment.getDesc());
            setStatusFlag(mDepartment.isStatusFlag());
            if (mDepartment.getCreatedBy() != null) {
                setCreatedBy(new MUser(mDepartment.getCreatedBy()));
            }
            setCreatedDate(mDepartment.getCreatedDate());
            if (mDepartment.getmSectionList() != null) {
                setmSectionList(new ArrayList<>());
                for (MSection mSection : mDepartment.getmSectionList()) {
                    getmSectionList().add(new MSection(mSection));
                }
            }
        }
    }

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "dept_id", length = 36)
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
    @JoinColumn(name = "div_id", nullable = false)
    public MDivision getmDivision() {
        return mDivision;
    }

    public void setmDivision(MDivision mDivision) {
        this.mDivision = mDivision;
    }

    @Column(name = "dept_code", length = 10, nullable = false, unique = true )
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Column(name = "dept_name", length = 255, nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "dept_desc", length = 1000)
    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
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

    //@OneToMany(mappedBy = "mDepartment", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OneToMany(mappedBy = "mDepartment", cascade = CascadeType.DETACH, fetch = FetchType.LAZY)
    @Fetch(value = FetchMode.SUBSELECT)
//    @Transient
    public List<MSection> getmSectionList() {
        return mSectionList;
    }

    public void setmSectionList(List<MSection> mSectionList) {
        this.mSectionList = mSectionList;
    }

    @Column(name = "status_flag", nullable = false)
    public boolean isStatusFlag() {
        return statusFlag;
    }

    public void setStatusFlag(boolean statusFlag) {
        this.statusFlag = statusFlag;
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
        sb.append("^ Nama Direktorat: " + getmDivision().getName());
        sb.append("^ Kode Subdit: " + getCode());
        sb.append("^ Nama Subdit: " + getName());
		sb.append("^ Deskripsi: " + getDesc());
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
