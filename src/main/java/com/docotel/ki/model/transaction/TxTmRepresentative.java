package com.docotel.ki.model.transaction;

import com.docotel.ki.audittrail.EntityAuditTrailListener;
import com.docotel.ki.enumeration.UserEnum;
import com.docotel.ki.model.BaseModel;
import com.docotel.ki.model.master.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.text.WordUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "tx_tm_reprs")
@EntityListeners(EntityAuditTrailListener.class)
public class TxTmRepresentative extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private String name;
    private TxTmGeneral txTmGeneral;
    private MRepresentative mRepresentative;
    private boolean status;
    private String xmlFileId;
    private MCountry mCountry;
    private MProvince mProvince;
    private MCity mCity;
    private String office;
    private String address;
    private String zipCode;
    private String phone;
    private String email;
    
    /***************************** - TRANSIENT FIELD SECTION - ****************************/

    /***************************** - CONSTRUCTOR SECTION - ****************************/

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "tm_reprs_id", length = 36)
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
    @JoinColumn(name = "reprs_id", nullable = false)
    public MRepresentative getmRepresentative() {
        return mRepresentative;
    }

    public void setmRepresentative(MRepresentative mRepresentative) {
        this.mRepresentative = mRepresentative;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    @JsonIgnore
    public MUser getCreatedBy() {
        if (createdBy == null) {
            Object principal = null;
            try {
                principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            } catch (NullPointerException e) {
            }
            if (principal != null && principal instanceof UserDetails) {
                createdBy = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            } else {
                createdBy = UserEnum.SYSTEM.value();
            }
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
            Object principal = null;
            try {
                principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            } catch (NullPointerException e) {
            }
            if (principal != null && principal instanceof UserDetails) {
                updatedBy = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            } else {
                updatedBy = UserEnum.SYSTEM.value();
            }
        }
        return updatedBy;
    }

    public void setUpdatedBy(MUser updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Column(name = "tm_reprs_status")
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
        sb.append("^ Nomor Konsultan: " + getmRepresentative().getNo());
        sb.append("^ Nama Konsultan: " + getmRepresentative().getName());
        return sb.toString();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = true)
    public MCountry getmCountry() {
        return mCountry;
    }

    public void setmCountry(MCountry mCountry) {
        this.mCountry = mCountry;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prov_id", nullable = true)
    public MProvince getmProvince() {
        return mProvince;
    }

    public void setmProvince(MProvince mProvince) {
        this.mProvince = mProvince;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = true)
    public MCity getmCity() {
        return mCity;
    }

    public void setmCity(MCity mCity) {
        this.mCity = mCity;
    }

    @Column(name = "reprs_name", length = 255, nullable = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        try {
            this.name = WordUtils.capitalize(name.trim());
        } catch (NullPointerException e) {
            this.name = null;
        }
    }

    @Column(name = "reprs_office", length = 255, nullable = true)
    public String getOffice() {
        return office;
    }

    public void setOffice(String office) {
        try {
            this.office = office.trim();
        } catch (NullPointerException e) {
            this.office = null;
        }
    }

    @Column(name = "reprs_address", length = 500, nullable = false)
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Column(name = "reprs_zipcode", length = 20, nullable = true)
    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    @Column(name = "reprs_phone", length = 100, nullable = true)
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Column(name = "reprs_email", length = 255, nullable = true)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
