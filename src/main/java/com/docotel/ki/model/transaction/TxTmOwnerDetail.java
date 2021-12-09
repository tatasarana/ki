package com.docotel.ki.model.transaction;

import com.docotel.ki.model.BaseModel;
import com.docotel.ki.audittrail.EntityAuditTrailListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "tx_tm_owner_detail")
@EntityListeners(EntityAuditTrailListener.class)
public class TxTmOwnerDetail extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private TxTmGeneral txTmGeneral;
    private TxTmOwner txTmOwner;
    private String name;
    private boolean status;
    private String xmlFileId;
    
    /***************************** - TRANSIENT FIELD SECTION - ****************************/

    /***************************** - CONSTRUCTOR SECTION - ****************************/

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "tm_owner_detail_id", length = 36)
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
    @JoinColumn(name = "tm_owner_id", nullable = false)
    public TxTmOwner getTxTmOwner() {
        return txTmOwner;
    }

    public void setTxTmOwner(TxTmOwner txTmOwner) {
        this.txTmOwner = txTmOwner;
    }

    @Column(name = "tm_owner_detail_name", nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "tm_owner_detail_status")
    public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}
	
	@Column(name = "xml_file_id", nullable = true)
	public String getXmlFileId() {
		return xmlFileId;
	}

	public void setXmlFileId(String xmlFileId) {
		this.xmlFileId = xmlFileId;
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
        sb.append("^ Nomor Permohonan: " + getTxTmGeneral().getApplicationNo());
        sb.append("^ Nama Pemohon Tambahan: " + getName());
        return sb.toString();
    }

}
