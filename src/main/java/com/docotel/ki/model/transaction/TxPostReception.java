package com.docotel.ki.model.transaction;

import com.docotel.ki.audittrail.EntityAuditTrailListener;
import com.docotel.ki.model.BaseModel;
import com.docotel.ki.model.master.*;
import com.docotel.ki.pojo.DataGeneral;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.util.NumberUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tx_post_reception")
@EntityListeners(EntityAuditTrailListener.class)
public class TxPostReception extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private String postNo;
    private String eFilingNo;
    private String billingCode;
    private String note;
    private Timestamp postDate;
    private Timestamp paymentDate;
    private BigDecimal totalPayment;    
    private boolean onlineFlag;

    private TxPostReceptionDetail txPostReceptionDetail;
    private TxPostOwner txPostOwner;
    private TxPostRepresentative txPostRepresentative;
    private MFileSequence mFileSequence;
    private MFileType mFileType;
    private MFileTypeDetail mFileTypeDetail;
    private MStatus mStatus;
    
    private List<TxPostReceptionDetail> txPostReceptionDetailList;   
    /***************************** - TRANSIENT FIELD SECTION - ****************************/

    //sky
    private String postDateTemp;
    private String paymentDateTemp;
    private String totalPaymentTemp;  
    private String totalClassTemp;
    private String unitTemp;
    private String transactionNoTemp;
    private List<DataGeneral> applicationIdListTemp;
    
    /*@JsonProperty("appIdListTemp")
    private ArrayList<String> appIdListTemp;*/
    
    //end

    /***************************** - CONSTRUCTOR SECTION - ****************************/

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "post_reception_id", length = 36)
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
//            id =  eFilingNo;

        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "post_reception_no", length = 20, nullable = false)
    public String getPostNo() {
        return postNo;
    }

    public void setPostNo(String postNo) {
        this.postNo = postNo;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "file_seq_id", nullable=true)
    public MFileSequence getmFileSequence() {
        return mFileSequence;
    }

    public void setmFileSequence(MFileSequence mFileSequence) {
        this.mFileSequence = mFileSequence;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_type_id")
    public MFileType getmFileType() {
        return mFileType;
    }

    public void setmFileType(MFileType mFileType) {
        this.mFileType = mFileType;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_type_detail_id", nullable=true)
    public MFileTypeDetail getmFileTypeDetail() {
        return mFileTypeDetail;
    }

    public void setmFileTypeDetail(MFileTypeDetail mFileTypeDetail) {
        this.mFileTypeDetail = mFileTypeDetail;
    }

    @OneToOne(mappedBy = "txPostReception", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    public TxPostReceptionDetail getTxPostReceptionDetail() {
        return txPostReceptionDetail;
    }

    public void setTxPostReceptionDetail(TxPostReceptionDetail txPostReceptionDetail) {
        this.txPostReceptionDetail = txPostReceptionDetail;
    }
    
    @OneToOne(mappedBy = "txPostReception", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    public TxPostOwner getTxPostOwner() {
        return txPostOwner;
    }

    public void setTxPostOwner(TxPostOwner txPostOwner) {
        this.txPostOwner = txPostOwner;
    }

    @OneToOne(mappedBy = "txPostReception", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true, orphanRemoval = true)
    public TxPostRepresentative getTxPostRepresentative() {
        return txPostRepresentative;
    }
    
    public void setTxPostRepresentative(TxPostRepresentative txPostRepresentative) {
        this.txPostRepresentative = txPostRepresentative;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    public MStatus getmStatus() {
        return mStatus;
    }

    public void setmStatus(MStatus mStatus) {
        this.mStatus = mStatus;
    }

    @Column(name = "efiling_no", length = 50, nullable = true)
    public String geteFilingNo() {
        return eFilingNo;
    }

    public void seteFilingNo(String eFilingNo) {
        this.eFilingNo = eFilingNo;
    }
    
    @Column(name = "post_reception_date")
    public Timestamp getPostDate() {
        return postDate;
    }

    public void setPostDate(Timestamp postDate) {
        this.postDate = postDate;
        this.postDateTemp = DateUtil.formatDate(this.postDate, "dd/MM/yyyy HH:mm:ss");
    }

    @Column(name = "post_reception_note", length = 1000)
    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
    
    @Column(name = "payment_date")
    public Timestamp getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Timestamp paymentDate) {
        this.paymentDate = paymentDate;
        this.paymentDateTemp = DateUtil.formatDate(this.paymentDate, "dd/MM/yyyy HH:mm:ss");
    }

    @Column(name = "billing_code")
    public String getBillingCode() {
        return billingCode;
    }

    public void setBillingCode(String billingCode) {
        this.billingCode = billingCode;
    }

    @Column(name = "total_payment")
    public BigDecimal getTotalPayment() {
        return totalPayment;
    }

    public void setTotalPayment(BigDecimal totalPayment) {
        this.totalPayment = totalPayment;
        this.totalPaymentTemp = NumberUtil.formatDecimal(this.totalPayment);
    }
    
    @Column(name = "online_flag", nullable = false)
    public boolean isOnlineFlag() {
        return onlineFlag;
    }

    public void setOnlineFlag(boolean onlineFlag) {
        this.onlineFlag = onlineFlag;
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

	@OneToMany(mappedBy = "txPostReception", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Fetch(value = FetchMode.SUBSELECT)    
    public List<TxPostReceptionDetail> getTxPostReceptionDetailList() {
		return txPostReceptionDetailList;
	}

	public void setTxPostReceptionDetailList(List<TxPostReceptionDetail> txPostReceptionDetailList) {
		this.txPostReceptionDetailList = txPostReceptionDetailList;
	}

	/***************************** - GETTER SETTER TRANSIENT METHOD SECTION - *****************************/
    @Transient
    public String getPostDateTemp() {
        return postDateTemp;
    }

    public void setPostDateTemp(String postDateTemp) {
        this.postDateTemp = postDateTemp;
        try {
            this.postDate = DateUtil.toDate("dd/MM/yyyy HH:mm:ss", this.postDateTemp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Transient
    public String getPaymentDateTemp() {
        return paymentDateTemp;
    }

    public void setPaymentDateTemp(String paymentDateTemp) {
        this.paymentDateTemp = paymentDateTemp;
        try {
            this.paymentDate = DateUtil.toDate("dd/MM/yyyy HH:mm:ss", this.paymentDateTemp);
        } catch (ParseException e) {
        }
    }
    
    
    @Transient
    public String getUnitTemp() {
		return unitTemp;
	}

	public void setUnitTemp(String unitTemp) {
		this.unitTemp = unitTemp;
	}

	@Transient
	public String getTransactionNoTemp() {
		return transactionNoTemp;
	}

	public void setTransactionNoTemp(String transactionNoTemp) {
		this.transactionNoTemp = transactionNoTemp;
	}
    
    @Transient
    public String getTotalClassTemp() {
		return totalClassTemp;
	}

	public void setTotalClassTemp(String totalClassTemp) {
		this.totalClassTemp = totalClassTemp;
	}

	@Transient
    public String getTotalPaymentTemp() {
        return totalPaymentTemp;
    }

	public void setTotalPaymentTemp(String totalPaymentTemp) {
        this.totalPaymentTemp = totalPaymentTemp;
        this.totalPayment = NumberUtil.safeParseDecimal(this.totalPaymentTemp, BigDecimal.ZERO);
    }		
		
	@Transient
	public List<DataGeneral> getApplicationIdListTemp() {
		return applicationIdListTemp;
	}

	public void setApplicationIdListTemp(List<DataGeneral> applicationIdListTemp) {
		this.applicationIdListTemp = applicationIdListTemp;
	}

	/***************************** - OTHER METHOD SECTION - ****************************/
    @Transient
    public String getQrText() {
//		nomor permohonan""tgl penerimaan""login""total_byr""kode_billing""tgl_byr
//		J002018000013**20/03/2018**Raysa**10000000**1234567890**21/02/2018
        return getPostNo() + "***" + getPostDate() + "***" + getCreatedBy().getUsername() + "***" + getTotalPayment() + "***" + "***" + getPaymentDate();
    }

	@Override
    public String toString() {
        return "TxReception [id=" + id + ", createdBy=" + createdBy + "]";
    }

    @Transient
    public String getCurrentId() {
        return this.id;
    }

	@Override
    public String logAuditTrail() {
        StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + getCurrentId());
        if( getmFileSequence() !=null ) {
        	sb.append("^ Asal Permohonan: " + getmFileSequence().getDesc());
        }
        
        //sb.append(", mFileType: " + getmFileType().getCurrentId());
        //sb.append(", mFileTypeDetail: " + getmFileTypeDetail().getCurrentId());
        sb.append("^ Status: " + getmStatus().getName());
        //sb.append(", applicationNo: " + getApplicationNo());
        sb.append("^ No eFiling: " + geteFilingNo());
        sb.append("^ Tanggal Pengajuan: " + getPostDate());
        sb.append("^ Tanggal Pembayaran: " + getPaymentDate());
        if( getBillingCode() != null ) {
        	sb.append("^ Kode Billing: " + getBillingCode());
        }
        
        sb.append("^ Total Pembayaran: " + getTotalPayment());
        return sb.toString();
    }
}
