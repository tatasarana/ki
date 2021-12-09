package com.docotel.ki.model.transaction;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

import javax.persistence.*;

import com.docotel.ki.model.BaseModel;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.util.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.docotel.ki.audittrail.EntityAuditTrailListener;
import org.springframework.security.core.context.SecurityContextHolder;

@Entity
@Table(name = "tx_registration")
@EntityListeners(EntityAuditTrailListener.class)
public class TxRegistration extends BaseModel implements Serializable {
    
	/***************************** - FIELD SECTION - ****************************/
    private String id;
    private TxTmGeneral txTmGeneral;
    private String no;
    private Timestamp startDate;
    private Timestamp endDate;
    private Timestamp createdDate;
    private boolean downloadFlag;
    private boolean status;

    /***************************** - TRANSIENT FIELD SECTION - ****************************/
    private String startDateTemp;
    private String endDateTemp;    

    /***************************** - CONSTRUCTOR SECTION - ****************************/

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "reg_id", length = 36)
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    public TxTmGeneral getTxTmGeneral() {
        return txTmGeneral;
    }

    public void setTxTmGeneral(TxTmGeneral txTmGeneral) {
        this.txTmGeneral = txTmGeneral;
    }

    @Column(name = "reg_no", length = 50, nullable = false)
    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    @Column(name = "reg_start_date")
    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
        this.startDateTemp = DateUtil.formatDate(this.startDate, "dd/MM/yyyy");
    }

    @Column(name = "expiration_date")
    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
        this.endDateTemp = DateUtil.formatDate(this.endDate, "dd/MM/yyyy");
    }

    @Column(name = "download_flag", nullable = false)
    public boolean isDownloadFlag() {
        return downloadFlag;
    }

    public void setDownloadFlag(boolean downloadFlag) {
        this.downloadFlag = downloadFlag;
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

    @Column(name = "reg_status")
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
    public String endDate() {
        if (this.endDate != null) {
            return DateUtil.formatDate(this.endDate, "dd/MM/yyyy HH:mm:ss");
        }
        return "";
    }

    @Transient
    public String getCurrentId() {
        return this.id;
    }

    @Override
    public String logAuditTrail() {
        StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + getCurrentId());
        sb.append("^ Nomor Permohonan: " + getTxTmGeneral().getApplicationNo());
        sb.append("^ Nomor Registrasi: " + getNo());
        sb.append("^ Tanggal Registrasi: " + getStartDate());
        sb.append("^ Tanggal Perlindungan Berakhir: " + getEndDate());
        return sb.toString();
    }
}
