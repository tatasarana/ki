package com.docotel.ki.model.master;

import com.docotel.ki.model.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.docotel.ki.audittrail.EntityAuditTrailListener;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

/*
FILE_TYPE_ID	INTEGER	NO		1	autonumber
FILE_TYPE_DESC	STRING	NO		2	jenis permohonan based master data
 */
@Entity
@Table(name = "m_file_type")
@EntityListeners(EntityAuditTrailListener.class)
public class MFileType extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private String code;
    private String desc;
    private String menu;
    private boolean statusFlag;
    private MWorkflow mWorkflow;
    private boolean statusPaid;

    /***************************** - TRANSIENT FIELD SECTION - ****************************/
	private String statusFlagTemp;
	private String statusPaidTemp;

    /***************************** - CONSTRUCTOR SECTION - ****************************/

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "file_type_id", length = 36)
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "file_type_code", length = 10, nullable = false)
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Column(name = "file_type_desc", length = 255, nullable = false)
    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        try {
            this.desc = desc.trim();
        } catch (NullPointerException e) {
            this.desc = null;
        }
    }

    @Column(name = "file_type_menu", length = 10, nullable = true)
    public String getMenu() {
        return menu;
    }

    public void setMenu(String menu) {
        this.menu = menu;
    }
    
    @Column(name = "status_flag", nullable = false)
    public boolean isStatusFlag() {
        return statusFlag;
    }

    public void setStatusFlag(boolean statusFlag) {
        this.statusFlag = statusFlag;
        this.statusFlagTemp = "" + statusFlag;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id")
    public MWorkflow getmWorkflow() {
        return mWorkflow;
    }

    public void setmWorkflow(MWorkflow mWorkflow) {
        this.mWorkflow = mWorkflow;
    }
    
    
    @Column(name = "status_paid", nullable = true)
    public boolean isStatusPaid() {
        return statusPaid;
    }

    public void setStatusPaid(boolean statusPaid) {
        this.statusPaid = statusPaid;
        this.statusPaidTemp = "" + statusPaid;
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

	/***************************** - TRANSIENT GETTER SETTER METHOD SECTION - ****************************/
	@Transient
	public String getStatusFlagTemp() {
		return statusFlagTemp;
	}

	public void setStatusFlagTemp(String statusFlagTemp) {
		this.statusFlagTemp = statusFlagTemp;
		try {
			this.statusFlag = statusFlagTemp.equalsIgnoreCase("true");
		} catch (Exception e) {
			this.statusFlag = false;
		}
	}
	
	@Transient
	public String getStatusPaidTemp() {
		return statusPaidTemp;
	}

	public void setStatusPaidTemp(String statusPaidTemp) {
		this.statusPaidTemp = statusPaidTemp;
		try {
			this.statusPaid = statusPaidTemp.equalsIgnoreCase("true");
		} catch (Exception e) {
			this.statusPaid = false;
		}
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

	/***************************** - OTHER METHOD SECTION - ****************************/
    @Transient
    public String getCurrentId() {
        return id;
    }

    @Override
    public String logAuditTrail() {
        StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + getCurrentId());
        sb.append("^ Kode Tipe Permohonan: " + getCode());
        sb.append("^ Tipe Permohonan: " + getDesc());
        sb.append("^ Menu: " + getMenu());
        if(getmWorkflow() != null) {
            sb.append("^ Workflow: " + getmWorkflow().getName());
        } else {
        	MWorkflow wf = new MWorkflow();
        	sb.append("^ Workflow: " + wf.getName());
        	
        }
        sb.append("^ Status: " + (isStatusFlag() ? "Aktif" : "Tidak Aktif"));
        return sb.toString();
    }
}
