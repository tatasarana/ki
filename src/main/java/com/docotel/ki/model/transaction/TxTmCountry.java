package com.docotel.ki.model.transaction;

import java.io.Serializable;
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

import com.docotel.ki.audittrail.EntityAuditTrailListener;
import com.docotel.ki.model.BaseModel;
import com.docotel.ki.model.master.MCountry;

@Entity
@Table(name = "tx_tm_country")
@EntityListeners(EntityAuditTrailListener.class)
public class TxTmCountry extends BaseModel implements Serializable {

    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private TxTmGeneral txTmGeneral;
    private MCountry mCountry;
    
    /***************************** - TRANSIENT FIELD SECTION - ****************************/

    /***************************** - CONSTRUCTOR SECTION - ****************************/

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "tm_country_id", length = 36)
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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tm_class_limitation_country")
    public MCountry getmCountry() {
        return mCountry;
    }

    public void setmCountry(MCountry mCountry) {
        this.mCountry = mCountry;
    }

	@Override
	@Transient
	public String getCurrentId() {
		return this.id;
	}

	@Override
	public String logAuditTrail() {
		StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + getCurrentId());
        sb.append("^ Nomor Permohonan: " + getTxTmGeneral().getApplicationNo());
        sb.append("^ Country: " + mCountry.getName());
        return sb.toString();

	}

}
