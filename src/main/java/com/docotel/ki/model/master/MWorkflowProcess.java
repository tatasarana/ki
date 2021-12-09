package com.docotel.ki.model.master;

import com.docotel.ki.model.BaseModel;
import com.docotel.ki.util.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.UUID;

@Entity
@Table(name = "m_workflow_process")
public class MWorkflowProcess extends BaseModel implements Serializable {

    private String id;
    private MWorkflow workflow;
    private MStatus status;
    private MStatus prevStatus;
    private boolean statusFlag;
    private boolean mandatory;
    private boolean approval_need = false;
    private MRole cc_role ;
    private int orders;
    private String descriptions;
    private String name;
    private MRole role ;
    /***************************** - CONSTRUCTOR SECTION - ****************************/
    public MWorkflowProcess() {
	}

	public MWorkflowProcess(String id) {
		this.id = id;
	}
	
	/***************************** - GETTER SETTER METHOD SECTION - ****************************/

    @Id
    @Column(name = "workflow_process_id", length = 36)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    public MWorkflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(MWorkflow workflow) {
        this.workflow = workflow;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    public MStatus getStatus() {
        return status;
    }

    public void setStatus(MStatus status) {
        this.status = status;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public int getOrders() {
        return orders;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prev_status_id", nullable = false)
    @JsonIgnore
    public MStatus getPrevStatus() {
        return prevStatus;
    }

    public void setPrevStatus(MStatus prevStatus) {
        this.prevStatus = prevStatus;
    }

    @Column(length = 255, nullable = true)
    public String getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(String descriptions) {
        this.descriptions = descriptions;
    }

    @Column(length = 200, nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "status_flag", nullable = false)
    public boolean isStatusFlag() {
        return statusFlag;
    }

    public void setStatusFlag(boolean statusFlag) {
        this.statusFlag = statusFlag;
    }

    public String currentId() {
        return this.id;
    }



    /***************************** - GETTER SETTER TRANSIENT METHOD SECTION - *****************************/
    private String createdDateTemp;

    @Transient
    public String getCreatedDateTemp() {
        return createdDateTemp;
    }

    public void setCreatedDateTemp(String createdDateTemp) {
        this.createdDateTemp = createdDateTemp;
        try {
            this.createdDate = DateUtil.toDate("dd/MM/yyyy", this.createdDateTemp);
        } catch (ParseException e) {
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    @JsonIgnore
    public MRole getRole() {
        return role;
    }
    public void setRole(MRole role) {
        this.role = role;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cc_role")
    @JsonIgnore
    public MRole getCc_role() {
        return cc_role;
    }

    public void setCc_role(MRole cc_role) {
        this.cc_role = cc_role;
    }

    @Column(name = "approval_need")
    public boolean isApproval_need() {
        return approval_need;
    }

    public void setApproval_need(boolean approval_need) {
        this.approval_need = approval_need;
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
        sb.append("Nama Proses: " + getName());
        sb.append("Status: " + getStatus().getId());
        sb.append("Urutan: " + getOrders());
        sb.append("Mandatory: " + (isMandatory() ? "Ya": "Tidak"));
        sb.append("Status: " + (isStatusFlag() ? "Aktif": "Tidak Aktif"));
        return sb.toString();
    }
}
