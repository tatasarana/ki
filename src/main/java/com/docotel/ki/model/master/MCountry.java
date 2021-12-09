package com.docotel.ki.model.master;

import com.docotel.ki.model.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.docotel.ki.audittrail.EntityAuditTrailListener;
import org.apache.commons.text.WordUtils;
import org.hibernate.annotations.SelectBeforeUpdate;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

@SelectBeforeUpdate(false)
@Entity
@Table(name = "m_country")
@EntityListeners(EntityAuditTrailListener.class)
public class MCountry extends BaseModel implements Serializable {

    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private String code;
    private String name;
    private boolean statusFlag;
    private Boolean european;
    private Boolean madrid;

    /***************************** - TRANSIENT FIELD SECTION - ****************************/

    /***************************** - CONSTRUCTOR SECTION - ****************************/
    public MCountry() {
    }

    public MCountry(MCountry mCountry) {
        if (mCountry != null) {
            setId(mCountry.getCurrentId());
            setCode(mCountry.getCode());
            setName(mCountry.getName());
            setStatusFlag(mCountry.isStatusFlag());
            if (mCountry.getCreatedBy() != null) {
                setCreatedBy(new MUser(mCountry.getCreatedBy()));
            }
            setCreatedDate(mCountry.getCreatedDate());
        }
    }

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "country_id", length = 36)
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "country_code", length = 2, nullable = false, unique = true)
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Column(name = "country_name", length = 1000, nullable = false, unique = false)
    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }

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

    @Column(name = "is_european")
    public Boolean getEuropean() {
        if (european == null) {
            european = false;
        }
        return european;
    }
    public void setEuropean(Boolean european) {
        this.european = european;
    }

    @Column(name = "is_madrid")
    public Boolean getMadrid() {
        if (madrid == null) {
            madrid = true;
        }
        return madrid;
    }

    public void setMadrid(Boolean madrid) {
        this.madrid = madrid;
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
        sb.append("^ Kode Negara: " + getCode());
        sb.append("^ Nama Negara: " + getName());
        sb.append("^ Status: " + (isStatusFlag() ? "Aktif" : "Tidak Aktif"));
        return sb.toString();
    }

    @PreUpdate
    public void preUpdate() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        this.updatedBy = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        this.updatedDate = timestamp;
    }

    @PrePersist
    public void prePersist() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        this.createdBy = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        this.createdDate = timestamp;
    }
}
