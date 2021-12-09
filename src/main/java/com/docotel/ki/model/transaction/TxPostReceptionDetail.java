package com.docotel.ki.model.transaction;

import com.docotel.ki.model.BaseModel;
import com.docotel.ki.model.master.MStatus;
import com.docotel.ki.audittrail.EntityAuditTrailListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "tx_post_reception_detail")
@EntityListeners(EntityAuditTrailListener.class)
public class TxPostReceptionDetail extends BaseModel implements Serializable {

    private String id;
    private TxPostReception txPostReception;
    private TxTmGeneral txTmGeneral;
    private MStatus mStatus;

    @Id
    @Column(name = "post_reception_detail_id", length = 36)
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
    @JoinColumn(name = "post_reception_id")
    public TxPostReception getTxPostReception() {
        return txPostReception;
    }

    public void setTxPostReception(TxPostReception txPostReception) {
        this.txPostReception = txPostReception;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    public TxTmGeneral getTxTmGeneral() {
        return txTmGeneral;
    }

    public void setTxTmGeneral(TxTmGeneral txTmGeneral) {
        this.txTmGeneral = txTmGeneral;
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    public MStatus getmStatus() {
		return mStatus;
	}

	public void setmStatus(MStatus mStatus) {
		this.mStatus = mStatus;
	}
    

    @Transient
    public String getCurrentId() {
        return this.id;
    }

    @Override
    public String logAuditTrail() {
        StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + getCurrentId());
        sb.append("^ ID Post Reception: " + getTxPostReception());
        sb.append("^ Nomor Permohonan: " + getTxTmGeneral().getApplicationNo());
        return sb.toString();
    }
    
}
