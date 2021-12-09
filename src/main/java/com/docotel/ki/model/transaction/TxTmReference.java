package com.docotel.ki.model.transaction;

import com.docotel.ki.audittrail.EntityAuditTrailListener;
import com.docotel.ki.model.BaseModel;
import com.docotel.ki.model.master.MUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "tx_tm_reference")
@EntityListeners(EntityAuditTrailListener.class)
public class TxTmReference extends BaseModel implements Serializable {

	/***************************** - FIELD SECTION - ****************************/
    private String id;
    private TxTmGeneral txTmGeneral;
    private TxTmGeneral refApplicationId;
    private TxTmGeneral refApplicationId2;
    private String appNo;
    private String regNo;
    private String regDate;

    
    public TxTmReference(TxTmGeneral txTmGeneral, TxTmGeneral refApplicationId) {
    	//super();
    	this.txTmGeneral = txTmGeneral;
    	this.refApplicationId = refApplicationId;
    	//this.refApplicationId2 = refApplicationId2;
    }

    public TxTmReference() {

    }

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "tm_reference_id", length = 36)
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
    @JoinColumn(name = "ref_application_id")
    public TxTmGeneral getRefApplicationId() {
        return refApplicationId;
    }

    public void setRefApplicationId(TxTmGeneral refApplicationId) {
        this.refApplicationId = refApplicationId;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ref_application_id_2")
    public TxTmGeneral getRefApplicationId_2() {
        return refApplicationId2;
    }

    public void setRefApplicationId_2(TxTmGeneral refApplicationId2) {
        this.refApplicationId2 = refApplicationId2;
    }

	@Override
	@Transient
	public String getCurrentId() {
		return this.id;
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
	public String logAuditTrail() {
		StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + getCurrentId());
        sb.append("^ Nomor Permohonan: " + getRefApplicationId());
        sb.append("^ Nomor Referensi: " + getTxTmGeneral().getApplicationNo());
        return sb.toString();
	}

	@Transient
    public String getRegNo() {
        if(regNo == null){
            regNo = "";
        }
        return regNo;
    }

    public void setRegNo(String noReg) {
        this.regNo = noReg;
    }

    @Transient
    public String getAppNo() {
        if(appNo == null){
            appNo = "";
        }
        return appNo;
    }

    public void setAppNo(String appNo) {
        this.appNo = appNo;
    }

    @Transient
    public String getRegDate() {
        if(regDate == null){
            regDate ="";
        }
        return regDate;
    }

    public void setRegDate(String regDate) {
        this.regDate = regDate;
    }
}
