package com.docotel.ki.model.transaction;

import com.docotel.ki.enumeration.UserEnum;
import com.docotel.ki.model.BaseModel;
import com.docotel.ki.model.master.MCountry;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.util.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.docotel.ki.audittrail.EntityAuditTrailListener;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.UUID;

@Entity
@Table(name = "tx_tm_prior")
@EntityListeners(EntityAuditTrailListener.class)
public class TxTmPrior extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private TxTmGeneral txTmGeneral;
    private MCountry mCountry;
    private String no;
    private String note;
    private String status;
    private Timestamp priorDate;
    private String xmlFileId;
    
    /***************************** - TRANSIENT FIELD SECTION - ****************************/

    private String priorDateTemp;

    /***************************** - CONSTRUCTOR SECTION - ****************************/

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "tm_prior_id", length = 36)
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
    @JoinColumn(name = "tm_prior_country", nullable = false)
    public MCountry getmCountry() {
        return mCountry;
    }

    public void setmCountry(MCountry mCountry) {
        this.mCountry = mCountry;
    }

    @Column(name = "tm_prior_no", length = 255)
    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }
    
    @Column(name = "tm_prior_note", length = 1000)
    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
    
    @Column(name = "tm_prior_status", nullable = false)
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    @Column(name = "tm_prior_date", nullable = false)
    public Timestamp getPriorDate() {
        return priorDate;
    }

    public void setPriorDate(Timestamp priorDate) {
        this.priorDate = priorDate;
        this.priorDateTemp = DateUtil.formatDate(this.priorDate, "dd/MM/yyyy");
    }
    
    @Column(name = "xml_file_id", nullable = true)
	public String getXmlFileId() {
		return xmlFileId;
	}

	public void setXmlFileId(String xmlFileId) {
		this.xmlFileId = xmlFileId;
	}

	/***************************** - GETTER SETTER TRANSIENT METHOD SECTION - *****************************/

    @Transient
    public String getPriorDateTemp() {
        return priorDateTemp;
    }

    public void setPriorDateTemp(String priorDateTemp) {
        this.priorDateTemp = priorDateTemp;
        try {
            this.priorDate = DateUtil.toDate("dd/MM/yyyy", this.priorDateTemp);
        } catch (ParseException e) {
        }

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
        sb.append("^ Nomor Permohonan: " + getTxTmGeneral().getApplicationNo());
        sb.append("^ Nomor Prioritas: " + getNo());
        sb.append("^ Tanggal Prioritas: " + getPriorDate());
        sb.append("^ Negara: " + getmCountry().getName());
        sb.append("^ Status: " + getStatus());
        return sb.toString();
    }
}
