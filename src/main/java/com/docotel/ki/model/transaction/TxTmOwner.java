package com.docotel.ki.model.transaction;

import com.docotel.ki.enumeration.UserEnum;
import com.docotel.ki.model.BaseModel;
import com.docotel.ki.model.master.MCity;
import com.docotel.ki.model.master.MCountry;
import com.docotel.ki.model.master.MProvince;
import com.docotel.ki.model.master.MUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.docotel.ki.audittrail.EntityAuditTrailListener;
import com.docotel.ki.model.master.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tx_tm_owner")
@EntityListeners(EntityAuditTrailListener.class)
public class TxTmOwner extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private TxTmGeneral txTmGeneral;
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
    private boolean addressFlag;
    private MCountry postCountry;
    private MProvince postProvince;
    private MCity postCity;
    private String postAddress;
    private String postZipCode;
    private String postPhone;
    private String postEmail;
    private boolean status;
    private String xmlFileId;

    private String whatsapp;
    private String telegram;
    private String instagram;
    private String twitter;
    private String facebook;
    private String entitlement;
    private String commercialAddress;
    private String legalEntity;

    private String txPostReception;
    private String pascaFileTypeDesc;


    private List<TxTmOwnerDetail> txTmOwnerDetails;

    /***************************** - TRANSIENT FIELD SECTION - ****************************/

    /***************************** - CONSTRUCTOR SECTION - ****************************/
    public TxTmOwner(){}



    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "tm_owner_id", length = 36)
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

    @Column(name = "tm_owner_no", length = 16)
    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    @Column(name = "tm_owner_name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tm_owner_nationality")
    public MCountry getNationality() {
        return nationality;
    }

    public void setNationality(MCountry nationality) {
        this.nationality = nationality;
    }

    @Column(name = "tm_owner_type")
    public String getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(String ownerType) {
        this.ownerType = ownerType;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tm_owner_country")
    public MCountry getmCountry() {
        return mCountry;
    }

    public void setmCountry(MCountry mCountry) {
        this.mCountry = mCountry;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tm_owner_province")
    public MProvince getmProvince() {
        return mProvince;
    }

    public void setmProvince(MProvince mProvince) {
        this.mProvince = mProvince;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tm_owner_city")
    public MCity getmCity() {
        return mCity;
    }

    public void setmCity(MCity mCity) {
        this.mCity = mCity;
    }     

    @Column(name = "tm_owner_address", nullable = true, length=1200)
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Column(name = "tm_owner_zip_code")
    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    @Column(name = "tm_owner_phone", nullable = true)
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Column(name = "tm_owner_email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Column(name = "tm_owner_add_flag")
    public boolean isAddressFlag() {
        return addressFlag;
    }

    public void setAddressFlag(boolean addressFlag) {
        this.addressFlag = addressFlag;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tm_owner_post_country")
    //@Column(name = "tm_owner_post_country")
    public MCountry getPostCountry() {
        return postCountry;
    }

    public void setPostCountry(MCountry postCountry) {
        this.postCountry = postCountry;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tm_owner_post_province")
    //@Column(name = "tm_owner_post_province")
    public MProvince getPostProvince() {
        return postProvince;
    }

    public void setPostProvince(MProvince postProvince) {
        this.postProvince = postProvince;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tm_owner_post_city")
    public MCity getPostCity() {
        return postCity;
    }

    public void setPostCity(MCity postCity) {
        this.postCity = postCity;
    }
      
    @Column(name = "tm_owner_post_address")
    public String getPostAddress() {
        return postAddress;
    }

    public void setPostAddress(String postAddress) {
        this.postAddress = postAddress;
    }

    @Column(name = "tm_owner_post_zip_code")
    public String getPostZipCode() {
        return postZipCode;
    }

    public void setPostZipCode(String postZipCode) {
        this.postZipCode = postZipCode;
    }

    @Column(name = "tm_owner_post_phone")
    public String getPostPhone() {
        return postPhone;
    }

    public void setPostPhone(String postPhone) {
        this.postPhone = postPhone;
    }

    @Column(name = "tm_owner_post_email")
    public String getPostEmail() {
        return postEmail;
    }

    public void setPostEmail(String postEmail) {
        this.postEmail = postEmail;
    }

    @OneToMany(mappedBy = "txTmOwner", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Fetch(value = FetchMode.SUBSELECT)
    public List<TxTmOwnerDetail> getTxTmOwnerDetails() {
        return txTmOwnerDetails;
    }

    public void setTxTmOwnerDetails(List<TxTmOwnerDetail> txTmOwnerDetails) {
        this.txTmOwnerDetails = txTmOwnerDetails;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    @JsonIgnore
    public MUser getCreatedBy() {
        if (createdBy == null) {
            Object principal = null;
            try {
                principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            }catch (NullPointerException e){
            }
            if(principal != null && principal instanceof UserDetails) {
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

    @Column(name = "tm_owner_status")
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
	
	@Column(name = "tm_entitlement")
    public String getEntitlement() {
		return entitlement;
	}

	public void setEntitlement(String entitlement) {
		this.entitlement = entitlement;
	}

	@Column(name = "tm_commercial_address")
	public String getCommercialAddress() {
		return commercialAddress;
	}

	public void setCommercialAddress(String commercialAddress) {
		this.commercialAddress = commercialAddress;
	}
	
	@Column(name = "tm_legal_entity")
	public String getLegalEntity() {
		return legalEntity;
	}

	public void setLegalEntity(String legalEntity) {
		this.legalEntity = legalEntity;
	}

    @Column(name = "post_reception_id")
    public String getTxPostReception() {
        return txPostReception;
    }

    public void setTxPostReception(String txPostReception) {
        this.txPostReception = txPostReception;
    }

    @Column(name = "file_type_desc")
    public String getPascaFileTypeDesc() {
        return pascaFileTypeDesc;
    }

    public void setPascaFileTypeDesc(String pascaFileTypeDesc) {
        this.pascaFileTypeDesc = pascaFileTypeDesc;
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
        sb.append("^ Nama Pemohon: " + getName());
        sb.append("^ Alamat Pemohon: " + getAddress());
        sb.append("^ Telepon: " + getPhone());
        sb.append("^ Email: " + getEmail());
        sb.append("^ Alamat Pengiriman Berbeda: " + (isAddressFlag() ? "Ya" : "Tidak"));
        return sb.toString();
    }

    @Column(name = "tm_owner_whatsapp")
    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }

    @Column(name = "tm_owner_telegram")
    public String getTelegram() {
        return telegram;
    }

    public void setTelegram(String telegram) {
        this.telegram = telegram;
    }

    @Column(name = "tm_owner_twitter")
    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    @Column(name = "tm_owner_facebook")
    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    @Column(name = "tm_owner_instagram")
    public String getInstagram() {
        return instagram;
    }

    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }
}
