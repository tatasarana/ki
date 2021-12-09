package com.docotel.ki.model.transaction;

import com.docotel.ki.model.BaseModel;
import com.docotel.ki.model.master.MRoleSubstantifDetail;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.audittrail.EntityAuditTrailListener;
import com.docotel.ki.model.master.*;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.UUID;

@Entity
@Table(name = "tx_serv_dist")
@EntityListeners(EntityAuditTrailListener.class)
public class TxServDist extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private TxGroup txGroup;
    private MRoleSubstantifDetail mRoleSubstantifDetail;
    private String notes;
    private MUser createdBy;
    private Timestamp createdDate;

    /***************************** - TRANSIENT FIELD SECTION - ****************************/
    private String createdDateTemp;

	/***************************** - CONSTRUCTOR SECTION - ****************************/
    public TxServDist() {
    }

    public TxServDist(TxServDist txServDist) {
        if (txServDist != null) {
	        setId(txServDist.getCurrentId());
	        if (txServDist.getTxGroup() != null) {
		        setTxGroup(new TxGroup(txServDist.getTxGroup()));
	        }
	        if (txServDist.getmRoleSubstantifDetail() != null) {
		        setmRoleSubstantifDetail(new MRoleSubstantifDetail(txServDist.getmRoleSubstantifDetail()));
	        }
	        setNotes(txServDist.getNotes());
	        if (txServDist.getCreatedBy() != null) {
		        setCreatedBy(new MUser(txServDist.getCreatedBy()));
	        }
	        setCreatedDate(txServDist.getCreatedDate());
        }
    }

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "dist_id", length = 36) 
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
    @JoinColumn(name = "group_id", nullable = false)
    public TxGroup getTxGroup() {
        return txGroup;
    }

    public void setTxGroup(TxGroup txGroup) {
        this.txGroup = txGroup;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_subs_detail_id", nullable = false)
    public MRoleSubstantifDetail getmRoleSubstantifDetail() {
        return mRoleSubstantifDetail;
    }

    public void setmRoleSubstantifDetail(MRoleSubstantifDetail mRoleSubstantifDetail) {
        this.mRoleSubstantifDetail = mRoleSubstantifDetail;
    }

    @Column(name = "dist_notes", length = 4000)
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
        this.createdDateTemp = DateUtil.formatDate(this.createdDate, "dd/MM/yyyy");
    }

    /***************************** - GETTER SETTER TRANSIENT METHOD SECTION - *****************************/
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

    /***************************** - OTHER METHOD SECTION - ****************************/

    @Transient
    public String getCurrentId() {
        return this.id;
    }

    @Override
    public String logAuditTrail() {
        StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + getCurrentId());
        sb.append("^ Nama Grup: " + getTxGroup().getName());
        sb.append("^ Nama Pemeriksa: " + getmRoleSubstantifDetail().getmEmployee().getEmployeeName());
        return sb.toString();
    }
}
