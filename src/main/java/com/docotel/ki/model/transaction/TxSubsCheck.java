package com.docotel.ki.model.transaction;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

import javax.persistence.*;

import com.docotel.ki.model.BaseModel;
import com.docotel.ki.model.master.MClass;
import com.docotel.ki.model.master.MRoleSubstantif;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.audittrail.EntityAuditTrailListener;

@Entity
@Table(name = "tx_subs_check")
@EntityListeners(EntityAuditTrailListener.class)
public class TxSubsCheck extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private TxTmGeneral txTmGeneral;
    private String name;
    private String description;
    private MClass mClass;
    private String owner;
    private String classDescription;
    private String classTranslation;
    private MUser createdBy;
    private Timestamp createdDate;
    private MRoleSubstantif mRoleSubstantif;
    private String classList;
    private String appNo;
    private String regNo;


    /***************************** - TRANSIENT FIELD SECTION - ****************************/

    /***************************** - CONSTRUCTOR SECTION - ****************************/

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "check_id", length = 36)
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
    @JoinColumn(name = "application_id", nullable = false)
    public TxTmGeneral getTxTmGeneral() {
        return txTmGeneral;
    }

    public void setTxTmGeneral(TxTmGeneral txTmGeneral) {
        this.txTmGeneral = txTmGeneral;
    }

    @Column(name = "check_name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "check_desc")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "check_class")
    public MClass getmClass() {
        return mClass;
    }

    public void setmClass(MClass mClass) {
        this.mClass = mClass;
    }

    @Column(name = "check_owner")
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Column(name = "check_class_desc")
    public String getClassDescription() {
        return classDescription;
    }

    public void setClassDescription(String classDescription) {
        this.classDescription = classDescription;
    }

    @Column(name = "check_class_list")
    public String getClassList() {
        return classList;
    }

    public void setClassList(String classList) {
        this.classList = classList;
    }

    @Column(name = "check_class_desc_en")
    public String getClassTranslation() {
        return classTranslation;
    }

    public void setClassTranslation(String classTranslation) {
        this.classTranslation = classTranslation;
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
        if (createdDate == null) {
            createdDate = new Timestamp(System.currentTimeMillis());
        }
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    @Column(name = "check_appno")
    public String getAppNo() {
        return appNo;
    }

    public void setAppNo(String appNo) {
        this.appNo = appNo;
    }

    @Column(name = "check_regno")
    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_role_subs", nullable = false)
    public MRoleSubstantif getMRoleSubstantif() {
        return mRoleSubstantif;
    }

    public void setMRoleSubstantif(MRoleSubstantif mRoleSubstantif) {
        this.mRoleSubstantif = mRoleSubstantif;
    }

    /***************************** - GETTER SETTER TRANSIENT METHOD SECTION - *****************************/

    /***************************** - OTHER METHOD SECTION - ****************************/

    @Transient
    public String getCurrentId() {
        return this.id;
    }

    @Override
    public String logAuditTrail() {
        StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + getCurrentId());
        sb.append("^ Nomor Permohonan: " + getTxTmGeneral().getApplicationNo());
        if(getName() != null) {
        	sb.append("^ Pencarian Nama Merek: " + getName());
        } else {
        	sb.append("^ Pencarian Nama Merek: - ");
        }
        if(getDescription() != null) {
            sb.append("^ Pencarian Deskripsi Merek: " + getDescription());
        } else {
        	sb.append("^ Pencarian Deskripsi Merek: -");
        }
        if(getOwner() != null) {
            sb.append("^ Pencarian Nama Pemohon: " + getOwner());
        } else {
        	sb.append("^ Pencarian Nama Pemohon: -");
        }
        if(getmClass() != null) {
        	 sb.append("^ Pencarian Kelas: " + getmClass().getDesc());
        } else {
        	sb.append("^ Pencarian Kelas: -");
        }
        if(getmClass() != null) {
        	sb.append("^ Pencarian Uraian Kelas: " + getmClass().getDesc());
        } else {
        	sb.append("^ Pencarian Uraian Kelas: -");
        }
        if(getmClass() != null) {
        	sb.append("^ Pencarian Uraian Kelas (En): " + getmClass().getDescEn());
        } else {
        	sb.append("^ Pencarian Uraian Kelas (En): -");
        }
        
        return sb.toString();
    }
}
