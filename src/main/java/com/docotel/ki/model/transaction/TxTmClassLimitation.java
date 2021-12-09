package com.docotel.ki.model.transaction;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.security.core.context.SecurityContextHolder;

import com.docotel.ki.audittrail.EntityAuditTrailListener;
import com.docotel.ki.model.BaseModel;
import com.docotel.ki.model.master.MClass;
import com.docotel.ki.model.master.MClassDetail;
import com.docotel.ki.model.master.MCountry;
import com.docotel.ki.model.master.MUser;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "tx_tm_class_limitation")
@EntityListeners(EntityAuditTrailListener.class)
public class TxTmClassLimitation extends BaseModel implements Serializable {
	
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private TxTmGeneral txTmGeneral;
    private MClass mClass;
    private MClassDetail mClassDetail;
    private MCountry mCountry;
    
    public TxTmClassLimitation() {}
    
    
    public TxTmClassLimitation(TxTmGeneral txTmGeneral, MClass mClass, MClassDetail mClassDetail, MCountry mCountry) {
		super();
		this.txTmGeneral = txTmGeneral;
		this.mClass = mClass;
		this.mClassDetail = mClassDetail;
		this.mCountry = mCountry;
	}


	/***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "tm_class_limitation_id", length = 36)
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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_detail_id", nullable = false)
    public MClassDetail getmClassDetail() {
        return mClassDetail;
    }

    public void setmClassDetail(MClassDetail mClassDetail) {
        this.mClassDetail = mClassDetail;
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tm_class_limitation_country")
    public MCountry getmCountry() {
        return mCountry;
    }

    public void setmCountry(MCountry mCountry) {
        this.mCountry = mCountry;
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

	@Override
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
        sb.append("^ Country: " + mCountry.getName());
        return sb.toString();
	}

}
