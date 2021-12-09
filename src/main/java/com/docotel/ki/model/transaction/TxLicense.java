package com.docotel.ki.model.transaction;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.docotel.ki.model.BaseModel;
import com.docotel.ki.model.master.MCity;
import com.docotel.ki.model.master.MCountry;
import com.docotel.ki.model.master.MProvince;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.util.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.docotel.ki.audittrail.EntityAuditTrailListener;
import org.springframework.security.core.context.SecurityContextHolder;

@Entity
@Table(name = "tx_license")
@EntityListeners(EntityAuditTrailListener.class)
public class TxLicense extends BaseModel implements Serializable {
	/***************************** - FIELD SECTION - ****************************/
    private String id;
    private TxTmGeneral txTmGeneral;
    private TxLicense txLicenseParent;
    private String parent;
    private String no;
    private String name;
    private MCountry nationality;
    private String type;
    private MCountry mCountry;
    private MProvince mProvince;
    private MCity mCity;
    private String address;
    private String zipCode;
    private String phone;
    private String email;    
    private boolean status;
    private String txPostReception;
    
    @JsonFormat(pattern="dd/MM/yyyy")
    private Timestamp startDate;
    
    @JsonFormat(pattern="dd/MM/yyyy")
    private Timestamp endDate;
    private Integer level;
    
    /***************************** - TRANSIENT FIELD SECTION - ****************************/
    private String startDateTemp;
    private String endDateTemp;
    
    /***************************** - CONSTRUCTOR SECTION - ****************************/
    public TxLicense() {
	}

	public TxLicense(String id) {
		this.id = id;
	}

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "license_id", length = 36)
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
    @JoinColumn(name = "application_id", nullable = false)
	public TxTmGeneral getTxTmGeneral() {
		return txTmGeneral;
	}

	public void setTxTmGeneral(TxTmGeneral txTmGeneral) {
		this.txTmGeneral = txTmGeneral;
	}

	@Column(name = "license_no", length = 16)
    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    @Column(name = "license_name", nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "license_nationality")
    public MCountry getNationality() {
        return nationality;
    }

    public void setNationality(MCountry nationality) {
        this.nationality = nationality;
    }

    @Column(name = "license_type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "license_country", nullable = false)
    public MCountry getmCountry() {
        return mCountry;
    }

    public void setmCountry(MCountry mCountry) {
        this.mCountry = mCountry;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "license_province")
    public MProvince getmProvince() {
        return mProvince;
    }

    public void setmProvince(MProvince mProvince) {
        this.mProvince = mProvince;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "license_city")
    public MCity getmCity() {
        return mCity;
    }

    public void setmCity(MCity mCity) {
        this.mCity = mCity;
    }

    @Column(name = "license_address", nullable = false)
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Column(name = "license_zip_code")
    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    @Column(name = "license_phone")
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Column(name = "license_email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    @Column(name = "license_status")
    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
    
    @Column(name = "license_start_date")
    public Timestamp getStartDate() {
		return startDate;
	}

	public void setStartDate(Timestamp startDate) {
		this.startDate = startDate;
		this.startDateTemp = DateUtil.formatDate(this.startDate, "dd/MM/yyyy");
	}

	@Column(name = "license_end_date")
	public Timestamp getEndDate() {
		return endDate;
	}

	public void setEndDate(Timestamp endDate) {
		this.endDate = endDate;
		this.endDateTemp = DateUtil.formatDate(this.endDate, "dd/MM/yyyy");
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

	/*@Column(name = "license_parent")
	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}*/

	@Column(name = "license_level")
	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "license_parent")
    public TxLicense getTxLicenseParent() {
		return txLicenseParent;
	}

	public void setTxLicenseParent(TxLicense txLicenseParent) {
		this.txLicenseParent = txLicenseParent;
	}

    @Column(name = "post_reception_id")
    public String getTxPostReception() {
        return txPostReception;
    }

    public void setTxPostReception(String txPostReception) {
        this.txPostReception = txPostReception;
    }

    /***************************** - GETTER SETTER TRANSIENT METHOD SECTION - *****************************/
	@Transient
	public String getStartDateTemp() {
		return startDateTemp;
	}

	public void setStartDateTemp(String startDateTemp) {
		this.startDateTemp = startDateTemp;
	}

	@Transient
	public String getEndDateTemp() {
		return endDateTemp;
	}

	public void setEndDateTemp(String endDateTemp) {
		this.endDateTemp = endDateTemp;
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
        sb.append("^ Nama: " + getName());
        sb.append("^ Alamat: " + getAddress());
        sb.append("^ Telepon: " + getPhone());
        sb.append("^ Email: " + getEmail());
        return sb.toString();
	}

	@Transient
	public String getApplicationNumber() {
	    String no = "";
	    if (this.getTxTmGeneral() != null) {
	        no = this.getTxTmGeneral().getApplicationNo();
        }
	    return no;
    }
}
