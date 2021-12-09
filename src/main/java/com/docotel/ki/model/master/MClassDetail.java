package com.docotel.ki.model.master;

import com.docotel.ki.enumeration.UserEnum;
import com.docotel.ki.model.BaseModel;
import com.docotel.ki.model.transaction.TxTmClass;
import com.docotel.ki.util.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.docotel.ki.audittrail.EntityAuditTrailListener;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "m_class_detail")
@EntityListeners(EntityAuditTrailListener.class)
public class MClassDetail extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private MClass parentClass;
    private String desc;
    private String descEn;
    private String serial1;
    private String serial2;
    private String classBaseNo;
    private boolean statusFlag;
    private String notes;
    private  Boolean translationFlag = true ;

//    private List<TxTmClass> txTmClassList;

    /***************************** - TRANSIENT FIELD SECTION - ****************************/

    /***************************** - CONSTRUCTOR SECTION - ****************************/

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "class_detail_id", length = 36)
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
    @JoinColumn(name = "class_id")
    public MClass getParentClass() {
        return parentClass;
    }

    public void setParentClass(MClass parentClass) {
        this.parentClass = parentClass;
    }

    @Column(name = "class_desc", length = 10000)
    //@Column(name = "class_desc", columnDefinition = "CLOB")
    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        try { 
            this.desc = desc.trim();
        } catch (NullPointerException e) {
            this.desc = "-";
        }
    }

    @Column(name = "class_desc_en", length = 10000)
    public String getDescEn() {
        return descEn;
    }

    public void setDescEn(String descEn) {
        try {
            this.descEn = descEn.trim();
        } catch (NullPointerException e) {
            this.descEn = "-";
        }
    }

    @Column(name = "class_serial_1", length = 255)
    public String getSerial1() {
        return serial1;
    }

    public void setSerial1(String serial1) {
        this.serial1 = serial1;
    }

    @Column(name = "class_serial_2", length = 255)
    public String getSerial2() {
        return serial2;
    }

    public void setSerial2(String serial2) {
        this.serial2 = serial2;
    }

    @Column(name = "class_base_no", length = 255) //, nullable = false
    public String getClassBaseNo() {
        return classBaseNo;
    }

    public void setClassBaseNo(String classBaseNo) {
        this.classBaseNo = classBaseNo;
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
            Object principal = null;
            try {
                principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            } catch (NullPointerException e) {
            }
            if (principal != null && principal instanceof UserDetails) {
                createdBy = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            } else {
                createdBy = UserEnum.SYSTEM.value();
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
            Object principal = null;
            try {
                principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            } catch (NullPointerException e) {
            }
            if (principal != null && principal instanceof UserDetails) {
                updatedBy = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            } else {
                updatedBy = UserEnum.SYSTEM.value();
            }
        }
        return updatedBy;
    }

    public void setUpdatedBy(MUser updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    @Column(name = "notes", nullable = true)
    public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

    @Column(name = "translation_flag")
    public Boolean isTranslationFlag() {
        return translationFlag;
    }

    public void setTranslationFlag(Boolean translationFlag) {
        this.translationFlag = translationFlag;
    }
    
//    @OneToMany(mappedBy = "mClassDetail", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @Fetch(value = FetchMode.SUBSELECT)
//    public List<TxTmClass> getTxTmClassList() {
//        return txTmClassList;
//    }
//
//    public void setTxTmClassList(List<TxTmClass> txTmClassList) {
//        this.txTmClassList = txTmClassList;
//    }

	/***************************** - OTHER METHOD SECTION - ****************************/

    @Transient
    public String getCurrentId() {
        return this.id;
    }

    @Override
    public String logAuditTrail() {
        StringBuilder sb = new StringBuilder();
        sb.append("UUID Kelas: " + getCurrentId());
        sb.append("^ UUID Kelas Detail: " + getId());
        sb.append("^ Kelas: " + getParentClass().getNo());
        sb.append("^ Uraian Kelas Detail: " + getDesc());
        sb.append("^ Uraian Kelas Detail (En): " + getDescEn());
        sb.append("^ Nomor Dasar: " + getClassBaseNo());
        sb.append("^ Serial (E): " + getSerial1());
        sb.append("^ Serial (F): " + getSerial2());
        sb.append("^ Status: " + (isStatusFlag() ? "Aktif" : "Tidak Aktif"));
        return sb.toString();
    }

    public String status() {
        return (isStatusFlag() ? "Aktif" : "Tidak Aktif");
    }

    public String createdDate() {
        if (getCreatedDate() != null) {
            return DateUtil.formatDate(getCreatedDate(), "dd-MM-yyyy");
        }
        return "";
    }
}
