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

import com.docotel.ki.model.BaseModel;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.audittrail.EntityAuditTrailListener;

@Entity
@Table(name = "tx_registration_detail")
@EntityListeners(EntityAuditTrailListener.class)
public class TxRegistrationDetail extends BaseModel implements Serializable {
	/***************************** - FIELD SECTION - ****************************/
    private String id;
    private TxRegistration txRegistration;
    private Timestamp startDate;
    private Timestamp endDate;
    private boolean downloadFlag;
    private MUser createdBy;
    private Timestamp createdDate;
    private boolean status;

    /***************************** - TRANSIENT FIELD SECTION - ****************************/
    private String startDateTemp;
    private String endDateTemp;    

    /***************************** - CONSTRUCTOR SECTION - ****************************/

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "reg_detail_id", length = 36)
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
    @JoinColumn(name = "reg_id", nullable = false)
    public TxRegistration getTxRegistration() {
        return txRegistration;
    }

    public void setTxRegistration(TxRegistration txRegistration) {
        this.txRegistration = txRegistration;
    }

    @Column(name = "reg_detail_start_date")
    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
        this.startDateTemp = DateUtil.formatDate(this.startDate, "dd/MM/yyyy");
    }

    @Column(name = "reg_detail_expiration_date")
    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
        this.endDateTemp = DateUtil.formatDate(this.endDate, "dd/MM/yyyy");
    }

    @Column(name = "reg_detail_download_flag", nullable = false)
    public boolean isDownloadFlag() {
        return downloadFlag;
    }

    public void setDownloadFlag(boolean downloadFlag) {
        this.downloadFlag = downloadFlag;
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
    
    @Column(name = "reg_detail_status")
    public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}
	
	/***************************** - GETTER SETTER TRANSIENT METHOD SECTION - *****************************/
    @Transient
    public String getStartDateTemp() {
        return startDateTemp;
    }

    public void setStartDateTemp(String startDateTemp) {
        this.startDateTemp = startDateTemp;
    }
    
    @Transient
    public String getEndDateTemp() {
        return endDateTemp;
    }

    public void setEndDateTemp(String endDateTemp) {
        this.endDateTemp = endDateTemp;
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
        sb.append("^ Nomor Registrasi: " + getTxRegistration().getNo());
        sb.append("^ Tanggal Registrasi: " + getStartDate());
        sb.append("^ Tanggal Perlindungan Berakhir: " + getEndDate());
        sb.append("^ Status: " + (isStatus() ? "Aktif" : "Tidak Aktif"));
        return sb.toString();
    }
}
