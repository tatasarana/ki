package com.docotel.ki.model.transaction;


import com.docotel.ki.model.BaseModel;
import com.docotel.ki.model.master.MRole;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.master.MWorkflowProcess;
import com.docotel.ki.model.master.MWorkflowProcessActions;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.audittrail.EntityAuditTrailListener;

import javax.persistence.*;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.UUID;

@Entity
@Table(name = "tx_monitor")
@EntityListeners(EntityAuditTrailListener.class)
public class TxMonitor extends BaseModel implements Serializable {

    private String id;
    private Timestamp createdDate;
    private TxTmGeneral txTmGeneral;
    private MWorkflowProcess mWorkflowProcess;
    private MWorkflowProcessActions mWorkflowProcessActions;
    private String dueDate;
    private MWorkflowProcess targetWorkflowProcess;
    private MUser createdBy;
    private String fileUploadPath;
    private String notes;
    private MRole cc_role ;
    private String cc_notes ;
    private Boolean approved = false ;
    private TxPostReception txPostReception ;
    private Boolean is_download = false ;
    private Timestamp approvedDate;
    private Timestamp sendDate;
    private MUser approvedBy;


    private String createdDateTemp;

    @Id
    @Column(name = "monitor_id", length = 36)
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
            createdDate =  new Timestamp(System.currentTimeMillis());
        }
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
        this.createdDateTemp = DateUtil.formatDate(this.createdDate, "dd/MM/yyyy");
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    public TxTmGeneral getTxTmGeneral() {
        return txTmGeneral;
    }

    public void setTxTmGeneral(TxTmGeneral txTmGeneral) {
        this.txTmGeneral = txTmGeneral;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_process_id")
    public MWorkflowProcess getmWorkflowProcess() {
        return mWorkflowProcess;
    }

    public void setmWorkflowProcess(MWorkflowProcess mWorkflowProcess) {
        this.mWorkflowProcess = mWorkflowProcess;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_process_action_id", nullable = true)
    public MWorkflowProcessActions getmWorkflowProcessActions() {
        return mWorkflowProcessActions;
    }

    public void setmWorkflowProcessActions(MWorkflowProcessActions mWorkflowProcessActions) {
        this.mWorkflowProcessActions = mWorkflowProcessActions;
    }

    @Column(length = 100, nullable = true)
    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_workflow_process_id", nullable = true)
    public MWorkflowProcess getTargetWorkflowProcess() {
        return targetWorkflowProcess;
    }

    public void setTargetWorkflowProcess(MWorkflowProcess targetWorkflowProcess) {
        this.targetWorkflowProcess = targetWorkflowProcess;
    }

    @Column(length = 255, nullable = true, name = "file_upload_path")
    public String getFileUploadPath() {
        return fileUploadPath;
    }

    public void setFileUploadPath(String fileUploadPath) {
        this.fileUploadPath = fileUploadPath;
    }

    @Column(length = 4000, nullable = true, name = "notes")
    public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cc_role")
    public MRole getCc_role() {
        return cc_role;
    }

    public void setCc_role(MRole cc_role) {
        this.cc_role = cc_role;
    }

    @Column(length = 4000, nullable = true, name = "cc_notes")
    public String getCc_notes() {
        return cc_notes;
    }

    public void setCc_notes(String cc_notes) {
        this.cc_notes = cc_notes;
    }

    @Column(columnDefinition="BOOLEAN DEFAULT false", nullable = true, name ="approved" )
    public Boolean isApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    @Column(columnDefinition="BOOLEAN DEFAULT false", nullable = true, name ="is_download" )
    public Boolean getIs_download() {
        return is_download;
    }

    public void setIs_download(Boolean is_download) {
        this.is_download = is_download;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_reception_id")
    public TxPostReception getTxPostReception() {
        return txPostReception;
    }

    public void setTxPostReception(TxPostReception txPostReception) {
        this.txPostReception = txPostReception;
    }

    @Column(name = "approved_date")
    public Timestamp getApprovedDate() {
        return approvedDate;
    }
    public void setApprovedDate(Timestamp approvedDate) {
        this.approvedDate = approvedDate;
    }

    @Column(name = "send_date")
    public Timestamp getSendDate() {
        return sendDate;
    }

    public void setSendDate(Timestamp sendDate) {
        this.sendDate = sendDate;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by", nullable = false)
    public MUser getApprovedBy() {
        return approvedBy;
    }
    public void setApprovedBy(MUser approvedBy) {
        this.approvedBy = approvedBy;
    }

    /***************************** - GETTER SETTER TRANSIENT METHOD SECTION - *****************************/

    @Transient
    public String getCreatedDateTemp() {
        return createdDateTemp;
    }

    public void setCreatedDateTemp(String createdDateTemp) {
        this.createdDateTemp = createdDateTemp;
        try {
            this.createdDate = DateUtil.toDate("dd/MM/yyyy HH:mm:ss", this.createdDateTemp + DateUtil.formatDate(new Timestamp(System.currentTimeMillis()), " HH:mm:ss"));
        } catch (ParseException e) {
        	e.printStackTrace();
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
        sb.append("Tanggal: " + getCreatedDate());
        sb.append("Username: " + getCreatedBy().getUsername());
        sb.append("Nomor Permohonan: " + getTxTmGeneral().getApplicationNo());
        if(getmWorkflowProcess() != null) {
        	sb.append("Status Baru: " + getmWorkflowProcess().getStatus().getName());
        } else {
        	sb.append("");
        }

        if(getmWorkflowProcessActions() != null){
            sb.append("Aksi Baru: " + getmWorkflowProcessActions().getAction().getName());
        }
        return sb.toString();
    }
}
