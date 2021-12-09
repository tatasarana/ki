package com.docotel.ki.model.transaction;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.security.core.context.SecurityContextHolder;

import com.docotel.ki.audittrail.EntityAuditTrailListener;
import com.docotel.ki.model.BaseModel;
import com.docotel.ki.model.master.MUser;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "tx_tm_madrid_fee")
@EntityListeners(EntityAuditTrailListener.class)
public class TxTmMadridFee extends BaseModel implements Serializable {
	
	/***************************** - FIELD SECTION - ****************************/
    private String id;
    private TxTmGeneral txTmGeneral;
    private BigDecimal basicFee;
	private Integer complementaryFeeVolume;
    private BigDecimal complementaryFeeTotal;
    private Integer suplementaryFeeVolume;
    private BigDecimal suplementaryFeeTotal;
    private BigDecimal grandFeeTotal;
    
    private List<TxTmMadridFeeDetail> txTmMadridFeeDetails;
    
    private String tmpLanguage2;
    
    public TxTmMadridFee() {}
    
    public TxTmMadridFee(TxTmGeneral txTmGeneral, BigDecimal basicFee, Integer complementaryFeeVolume,
    		BigDecimal complementaryFeeTotal, Integer suplementaryFeeVolume, BigDecimal suplementaryFeeTotal,
    		BigDecimal grandFeeTotal) {
		super();
		this.id = getId();
		this.createdBy = getCreatedBy();
		this.createdDate = getCreatedDate();
		this.txTmGeneral = txTmGeneral;
		this.basicFee = basicFee;
		this.complementaryFeeVolume = complementaryFeeVolume;
		this.complementaryFeeTotal = complementaryFeeTotal;
		this.suplementaryFeeVolume = suplementaryFeeVolume;
		this.suplementaryFeeTotal = suplementaryFeeTotal;
		this.grandFeeTotal = grandFeeTotal;
	}

	/***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "tm_madrid_fee_id", length = 36)
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

	@Column(name = "basic_fee")
	public BigDecimal getBasicFee() {
		return basicFee;
	}

	public void setBasicFee(BigDecimal basicFee) {
		this.basicFee = basicFee;
	}

	@Column(name = "vol_complementary_fee")
	public Integer getComplentaryFeeVolume() {
		return complementaryFeeVolume;
	}

	public void setComplentaryFeeVolume(Integer complementaryFeeVolume) {
		this.complementaryFeeVolume = complementaryFeeVolume;
	}

	@Column(name = "total_complementary_fee")
	public BigDecimal getComplentaryFeeTotal() {
		return complementaryFeeTotal;
	}

	public void setComplentaryFeeTotal(BigDecimal complementaryFeeTotal) {
		this.complementaryFeeTotal = complementaryFeeTotal;
	}

	@Column(name = "vol_suplementary_fee")
	public Integer getSuplementaryFeeVolume() {
		return suplementaryFeeVolume;
	}

	public void setSuplementaryFeeVolume(Integer suplementaryFeeVolume) {
		this.suplementaryFeeVolume = suplementaryFeeVolume;
	}

	@Column(name = "total_suplementary_fee")
	public BigDecimal getSuplementaryFeeTotal() {
		return suplementaryFeeTotal;
	}

	public void setSuplementaryFeeTotal(BigDecimal suplementaryFeeTotal) {
		this.suplementaryFeeTotal = suplementaryFeeTotal;
	}
	
	@Column(name = "grand_total_fee")
	public BigDecimal getGrandFeeTotal() {
		return grandFeeTotal;
	}

	public void setGrandFeeTotal(BigDecimal grandFeeTotal) {
		this.grandFeeTotal = grandFeeTotal;
	}

	
	@OneToMany(mappedBy = "txTmMadridFee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Fetch(value = FetchMode.SUBSELECT)
    public List<TxTmMadridFeeDetail> getTxTmMadridFeeDetails() {
        return txTmMadridFeeDetails;
    }

    public void setTxTmMadridFeeDetails(List<TxTmMadridFeeDetail> txTmMadridFeeDetails) {
        this.txTmMadridFeeDetails = txTmMadridFeeDetails;
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
    
    @Transient
	public String getTmpLanguage2() {
		return tmpLanguage2;
	}

	public void setTmpLanguage2(String tmpLanguage2) {
		this.tmpLanguage2 = tmpLanguage2;
	}


	@Override
	public String logAuditTrail() {
		StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + getCurrentId());
        sb.append("^ Nomor Permohonan: " + getTxTmGeneral().getApplicationNo());
        sb.append("^ Basic Fee: " + getBasicFee());	
        sb.append("^ Volume Complentary Fee: " + getComplentaryFeeVolume());
        sb.append("^ Total Complentary Fee: " + getComplentaryFeeTotal());
        sb.append("^ Volume Suplementary Fee: " + getSuplementaryFeeVolume());
        sb.append("^ Total Suplementary Fee: " + getSuplementaryFeeTotal());
        return sb.toString();
	}


}
