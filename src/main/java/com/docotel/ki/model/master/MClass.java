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
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "m_class")
@EntityListeners(EntityAuditTrailListener.class)
public class MClass extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private Integer no;
    private String desc;
    private String descEn;
    private String edition;
    private Integer version;
    private String type;
    private boolean statusFlag;
    
    private List<MClassDetail> mClassDetailList;

    /***************************** - TRANSIENT FIELD SECTION - ****************************/

    /***************************** - CONSTRUCTOR SECTION - ****************************/

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "class_id", length = 36)
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "class_no", nullable = false, unique = true)
    public Integer getNo() { return no; }

    public void setNo(Integer no) { this.no = no; }

    @Column(name = "class_desc", length = 4000, nullable = false)
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

    @Column(name = "class_desc_en", length = 4000)
    public String getDescEn() {
        return descEn;
    }

    public void setDescEn(String descEn) {
        try {
            this.descEn = descEn.trim();
        } catch (NullPointerException e) {
            this.descEn = null;
        }
    }

    @Column(name = "class_edition")
    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    @Column(name = "class_version")
    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

   @Column(name = "class_type", length = 255, nullable = true)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Column(name = "status_flag", nullable = true)
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

    
    /*@OneToMany(mappedBy = "parentClass", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)*/
    @OneToMany(mappedBy = "parentClass", cascade = CascadeType.DETACH, fetch = FetchType.LAZY)
    @Fetch(value = FetchMode.SUBSELECT)
    public List<MClassDetail> getmClassDetailList() {
		return mClassDetailList;
	}

	public void setmClassDetailList(List<MClassDetail> mClassDetailList) {
		this.mClassDetailList = mClassDetailList;
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

    @Transient
    public String type() {
        if (getType() != null) {
            if ("Dagang".equalsIgnoreCase(getType().toString())) {
                return "Dagang";
            } else if ("Jasa".equalsIgnoreCase(getType().toString())) {
                return  "Jasa";
            }
        }
        return "-";
    }

    @Override
    public String logAuditTrail() {
        StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + getCurrentId());
        sb.append("^ Kelas: " + getNo());
        sb.append("^ Uraian: " + getDesc());
        sb.append("^ Uraian (E): " + getDescEn());
        sb.append("^ Edition: " + getEdition());
        sb.append("^ Version: " + getVersion());
        sb.append("^ Tipe Kelas: " + getType());
        sb.append("^ Status: " + status());
        return sb.toString();
    }

	public String createdDate() {
    	if (getCreatedDate() != null) {
            return DateUtil.formatDate(getCreatedDate(), "dd-MM-yyyy");
	    }
		return "";
	}
}
