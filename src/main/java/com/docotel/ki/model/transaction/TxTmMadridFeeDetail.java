package com.docotel.ki.model.transaction;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
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
import com.docotel.ki.model.master.MCountry;
import com.docotel.ki.model.master.MUser;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "tx_tm_madrid_fee_detail")
@EntityListeners(EntityAuditTrailListener.class)
public class TxTmMadridFeeDetail extends BaseModel implements Serializable {
	
	/***************************** - FIELD SECTION - ****************************/
    private String id;
    private MCountry mCountry;
    private TxTmMadridFee txTmMadridFee;
    private BigDecimal totalFee;
    
    /***************************** - TRANSIENT FIELD SECTION - ****************************/

    /***************************** - CONSTRUCTOR SECTION - ****************************/

    public TxTmMadridFeeDetail() {}
    
	public TxTmMadridFeeDetail(TxTmMadridFee txTmMadridFee, MCountry mCountry, BigDecimal totalFee) {
		super();
		this.id = getId();
		this.createdBy = getCreatedBy();
		this.createdDate = getCreatedDate();
		this.mCountry = mCountry;
		this.txTmMadridFee = txTmMadridFee;
		this.totalFee = totalFee;
	}

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/

    @Id
    @Column(name = "tm_madrid_fee_detail_id", length = 36)
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
    @JoinColumn(name = "tm_madrid_fee_detail_country")
    public MCountry getmCountry() {
		return mCountry;
	}

	public void setmCountry(MCountry mCountry) {
		this.mCountry = mCountry;
	}

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tm_madrid_fee_id", nullable = false)
    public TxTmMadridFee getTxTmMadridFee() {
		return txTmMadridFee;
	}

	public void setTxTmMadridFee(TxTmMadridFee txTmMadridFee) {
		this.txTmMadridFee = txTmMadridFee;
	}

	@Column(name = "total_fee")
	public BigDecimal getTotalFee() {
		return totalFee;
	}

	public void setTotalFee(BigDecimal totalFee) {
		this.totalFee = totalFee;
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
        sb.append("^ Negara: " + getmCountry().getName());
        sb.append("Total Fee: " + getTotalFee());
        return sb.toString();
	}

}
