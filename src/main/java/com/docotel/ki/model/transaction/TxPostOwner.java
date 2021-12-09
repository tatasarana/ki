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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.docotel.ki.audittrail.EntityAuditTrailListener;
import org.springframework.security.core.context.SecurityContextHolder;

@Entity
@Table(name = "tx_post_owner")
@EntityListeners(EntityAuditTrailListener.class)
public class TxPostOwner extends BaseModel implements Serializable {
	/***************************** - FIELD SECTION - ****************************/
    private String id;
    private TxPostReception txPostReception;
    private String no;
    private String name;
    private MCountry nationality;
    private String ownerType;
    private MCountry mCountry;
    private MProvince mProvince;
    private MCity mCity;
    private String address;
    private String zipCode;
    private String phone;
    private String email;    
    
    /***************************** - TRANSIENT FIELD SECTION - ****************************/
    
    /***************************** - CONSTRUCTOR SECTION - ****************************/

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "post_owner_id", length = 36)
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
    @JoinColumn(name = "post_reception_id", nullable = false)
    public TxPostReception getTxPostReception() {
        return txPostReception;
    }

    public void setTxPostReception(TxPostReception txPostReception) {
        this.txPostReception = txPostReception;
    }

    @Column(name = "post_owner_no", length = 16)
    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    @Column(name = "post_owner_name", nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_owner_nationality", nullable = false)
    public MCountry getNationality() {
        return nationality;
    }

    public void setNationality(MCountry nationality) {
        this.nationality = nationality;
    }

    @Column(name = "post_owner_type")
    public String getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(String ownerType) {
        this.ownerType = ownerType;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_owner_country", nullable = false)
    public MCountry getmCountry() {
        return mCountry;
    }

    public void setmCountry(MCountry mCountry) {
        this.mCountry = mCountry;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_owner_province")
    public MProvince getmProvince() {
        return mProvince;
    }

    public void setmProvince(MProvince mProvince) {
        this.mProvince = mProvince;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_owner_city")
    public MCity getmCity() {
        return mCity;
    }

    public void setmCity(MCity mCity) {
        this.mCity = mCity;
    }

    @Column(name = "post_owner_address", nullable = false)
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Column(name = "post_owner_zip_code")
    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    @Column(name = "post_owner_phone", nullable = false)
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Column(name = "post_owner_email")
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
        sb.append("^ Nomor Dokumen: " + getTxPostReception().getPostNo());
        sb.append("^ Nama Pemohon: " + getName());
        sb.append("^ Alamat Pemohon: " + getAddress());
        sb.append("^ Telepon: " + getPhone());
        sb.append("^ Email: " + getEmail());
        return sb.toString();
	}
}
