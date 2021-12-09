package com.docotel.ki.model.master;

import javax.persistence.*;

import com.docotel.ki.model.BaseModel;
import com.docotel.ki.audittrail.EntityAuditTrailListener;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "m_division")
@EntityListeners(EntityAuditTrailListener.class)
public class MDivision extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private String code;
    private String name;
    private String desc;
    private MUser createdBy;
    private Timestamp createdDate;
    private List<MDepartment> mDepartmentList;

	/***************************** - TRANSIENT FIELD SECTION - ****************************/

    /***************************** - CONSTRUCTOR SECTION - ****************************/
    public MDivision() {
    }

    public MDivision(MDivision mDivision) {
        if (mDivision != null) {
            setId(mDivision.getCurrentId());
            setCode(mDivision.getCode());
            setName(mDivision.getName());
            setDesc(mDivision.getDesc());
            if (mDivision.getCreatedBy() != null) {
            	setCreatedBy(new MUser(mDivision.getCreatedBy()));
            }
            setCreatedDate(mDivision.getCreatedDate());
            if (mDivision.getmDepartmentList() != null) {
            	setmDepartmentList(new ArrayList<>());
            	for (MDepartment mDepartment : mDivision.getmDepartmentList()) {
            		getmDepartmentList().add(new MDepartment(mDepartment));
	            }
            }
        }
    }

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "div_id", length = 36)
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "div_code", length = 10, nullable = false)
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Column(name = "div_name", length = 255, nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "div_desc", length = 1000)
    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
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

    @OneToMany(mappedBy = "mDivision", cascade = CascadeType.DETACH, fetch = FetchType.LAZY, orphanRemoval = true)
    @Fetch(value = FetchMode.SUBSELECT)
    //@Transient
    public List<MDepartment> getmDepartmentList() {
        return mDepartmentList;
    }

    public void setmDepartmentList(List<MDepartment> mDepartmentList) {
        this.mDepartmentList = mDepartmentList;
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
        sb.append("^ Kode Divisi: " + getCode());
        sb.append("^ Nama Divisi: " + getName());
        sb.append("^ Deskripsi: " + getDesc());
        return sb.toString();
    }
}
