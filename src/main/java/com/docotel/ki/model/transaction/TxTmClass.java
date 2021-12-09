package com.docotel.ki.model.transaction;

import com.docotel.ki.enumeration.UserEnum;
import com.docotel.ki.model.BaseModel;
import com.docotel.ki.model.master.MClass;
import com.docotel.ki.model.master.MClassDetail;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.util.ObjectMapperUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.docotel.ki.audittrail.EntityAuditTrailListener;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Entity
@Table(name = "tx_tm_class")
@EntityListeners(EntityAuditTrailListener.class)
public class TxTmClass extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private TxTmGeneral txTmGeneral;
    private MClass mClass;
    private String edition;
    private Integer version;    
    private MClassDetail mClassDetail;
    private String transactionStatus;
    private boolean correctionFlag;
    private String xmlFileId;
    private String notes;
    
    /***************************** - TRANSIENT FIELD SECTION - ****************************/

    /***************************** - CONSTRUCTOR SECTION - ****************************/
    
    public TxTmClass() {}
     
    public TxTmClass(TxTmClass txTmClass, TxTmGeneral txTmGeneral) {
    	this.setTxTmGeneral(txTmGeneral);
    	this.setmClass(txTmClass.getmClass());
    	this.setEdition(txTmClass.getEdition());
    	this.setVersion(txTmClass.getVersion());
    	this.setmClassDetail(txTmClass.getmClassDetail());
    	this.setTransactionStatus(txTmClass.getTransactionStatus());
    	this.setCorrectionFlag(txTmClass.isCorrectionFlag());
    	this.setXmlFileId(txTmClass.getXmlFileId());
    	this.setNotes(txTmClass.getNotes());
    }

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "tm_class_id", length = 36)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    public MClass getmClass() {
        return mClass;
    }

    public void setmClass(MClass mClass) {
        this.mClass = mClass;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_detail_id", nullable = false)
    public MClassDetail getmClassDetail() {
        return mClassDetail;
    }

    public void setmClassDetail(MClassDetail mClassDetail) {
        this.mClassDetail = mClassDetail;
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

    @Column(name = "tm_class_status", nullable = true)
    public String getTransactionStatus() {
        if (transactionStatus == null)
            transactionStatus = "ACTIVE";
		return transactionStatus;
	}

	public void setTransactionStatus(String transactionStatus) {
		this.transactionStatus = transactionStatus;
	}
	
	@Column(name = "correction_flag")
    public boolean isCorrectionFlag() {
        return correctionFlag;
    }

    public void setCorrectionFlag(boolean correctionFlag) {
        this.correctionFlag = correctionFlag;
    }
    
    @Column(name = "xml_file_id", nullable = true)
	public String getXmlFileId() {
		return xmlFileId;
	}

	public void setXmlFileId(String xmlFileId) {
		this.xmlFileId = xmlFileId;
	}

    @Column(name = "tm_class_notes", nullable = true, length=1000)
    public String getNotes() {
        if (notes == null)
            notes = "";
        return notes;     }

    public void setNotes(String notes) { this.notes = notes;  }

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
        sb.append("^ Kelas: " + getmClass().getNo());	
        sb.append("^ Deskripsi Kelas: " + getmClassDetail().getDesc());
        sb.append("^ Status: " + getTransactionStatus());
        sb.append("^ Perbaikan: " + isCorrectionFlag());
        sb.append("^ Catatan: " + getNotes());
        return sb.toString();
    }
	
}
