package com.docotel.ki.model.master;

import com.docotel.ki.model.BaseModel;
import com.docotel.ki.audittrail.EntityAuditTrailListener;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "m_brand_type")
@EntityListeners(EntityAuditTrailListener.class)
public class MBrandType extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private String code;
    private String name;
    private boolean statusFlag;
    private MUser createdBy;
    private Timestamp createdDate;
    private String xmlMadrid;
    
    /***************************** - TRANSIENT FIELD SECTION - ****************************/

    /***************************** - CONSTRUCTOR SECTION - ****************************/
    public MBrandType() {
    }

    public MBrandType(String id) {
        this.id = id;
    }

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "brand_type_id", length = 36)
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "brand_type_code", length = 5, nullable = false, unique = true)
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Column(name = "brand_type_name", length = 255, nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        try {
            this.name = name.trim();
        } catch (NullPointerException e) {
            this.name = null;
        }
    }

    
    @Column(name = "xml_madrid", length = 10, nullable = true)
    public String getXmlMadrid() {
		return xmlMadrid;
	}

	public void setXmlMadrid(String xmlMadrid) {
		this.xmlMadrid = xmlMadrid;
	}

	@Transient
    public String getCurrentId() {
        return id;
    }

    @Column(name = "status_flag", nullable = false)
    public boolean isStatusFlag() {
        return statusFlag;
    }

    public void setStatusFlag(boolean statusFlag) {
        this.statusFlag = statusFlag;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    public MUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(MUser createdBy) {
        this.createdBy = createdBy;
    }

    @Column(name = "created_date", nullable = true)
    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    /***************************** - OTHER METHOD SECTION - ****************************/
    @Override
    public String logAuditTrail() {
        StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + getCurrentId());
        sb.append("^ Kode Tipe Merek: " + getCode());
        sb.append("^ Nama Tipe Merek: " + getName());
        //sb.append(", Status: " + (isStatusFlag() ? "Aktif" : "Tidak Aktif"));
        return sb.toString();
    }
}
