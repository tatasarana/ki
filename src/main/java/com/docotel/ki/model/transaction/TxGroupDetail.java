package com.docotel.ki.model.transaction;

import com.docotel.ki.model.BaseModel;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.audittrail.EntityAuditTrailListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "tx_group_detail")
@EntityListeners(EntityAuditTrailListener.class)
public class TxGroupDetail extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private TxGroup txGroup;
    private TxTmGeneral txTmGeneral;
    private String status; 
    private MUser mUser1;
    private MUser mUser2;
    private MUser mUserCurrent;

	/***************************** - TRANSIENT FIELD SECTION - ****************************/

    /***************************** - CONSTRUCTOR SECTION - ****************************/
    public TxGroupDetail() {
    }

    public TxGroupDetail(TxGroupDetail txGroupDetail) {
        if (txGroupDetail != null) {
            setId(txGroupDetail.getCurrentId());
            if (txGroupDetail.getTxGroup() != null) {
                setTxGroup(new TxGroup(txGroupDetail.getTxGroup()));
            }
            if (txGroupDetail.getTxTmGeneral() != null) {
                // TODO reworked
                setTxTmGeneral(txGroupDetail.getTxTmGeneral());
            }
            setStatus(txGroupDetail.getStatus());
            if (txGroupDetail.getmUser1() != null) {
                setmUser1(new MUser(txGroupDetail.getmUser1()));
            }
            if (txGroupDetail.getmUser2() != null) {
                setmUser2(new MUser(txGroupDetail.getmUser2()));
            }
            if (txGroupDetail.getmUserCurrent() != null) {
                setmUserCurrent(new MUser(txGroupDetail.getmUserCurrent()));
            }
        }
    }

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "group_detail_id", length = 36)
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
    @JoinColumn(name = "group_id", nullable = false)
    public TxGroup getTxGroup() {
        return txGroup;
    }

    public void setTxGroup(TxGroup txGroup) {
        this.txGroup = txGroup;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    public TxTmGeneral getTxTmGeneral() {
        return txTmGeneral;
    }

    public void setTxTmGeneral(TxTmGeneral txTmGeneral) {
        this.txTmGeneral = txTmGeneral;
    }

    @Column(name = "group_detail_status") 
    public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_detail_user_id_1")
    public MUser getmUser1() {
        return mUser1;
    }

    public void setmUser1(MUser mUser1) {
        this.mUser1 = mUser1;
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_detail_user_id_2")
    public MUser getmUser2() {
        return mUser2;
    }

    public void setmUser2(MUser mUser2) {
        this.mUser2 = mUser2;
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_detail_user_id_current")
    public MUser getmUserCurrent() {
        return mUserCurrent;
    }

    public void setmUserCurrent(MUser mUserCurrent) {
        this.mUserCurrent = mUserCurrent;
    }
    
    /***************************** - GETTER SETTER TRANSIENT METHOD SECTION - *****************************/
    
	/***************************** - OTHER METHOD SECTION - ****************************/

    @Transient
    public String getCurrentId() {
        return this.id;
    }

    @Override
    public String logAuditTrail() {
        StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + getCurrentId());
        sb.append("^ Group: " + getTxGroup().getName());
        sb.append("^ Nomor Permohonan: " + getTxTmGeneral().getApplicationNo());
        sb.append("^ Status: " + getStatus());
        return sb.toString();
    }
}
