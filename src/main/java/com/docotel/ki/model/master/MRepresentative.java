package com.docotel.ki.model.master;

import com.docotel.ki.model.BaseModel;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.docotel.ki.audittrail.EntityAuditTrailListener;
import com.docotel.ki.enumeration.UserEnum;
import org.apache.commons.text.WordUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "m_reprs")
@EntityListeners(EntityAuditTrailListener.class)
public class MRepresentative extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private String no;
    private String name;
    private String office;
    private MCountry mCountry;
    private MProvince mProvince;
    private MCity mCity;
    private String address;
    private String zipCode;
    private String phone;
    private String email;
    private boolean statusFlag;
    private MUser userId;

	/***************************** - TRANSIENT FIELD SECTION - ****************************/
    
//    private String nik;
//    private String employeeName;

    /***************************** - CONSTRUCTOR SECTION - ****************************/
    public MRepresentative(){
    }

    public MRepresentative(MRepresentative mRepresentative) {
        if (mRepresentative != null) {
            setId(mRepresentative.getCurrentId());
            setNo(mRepresentative.getNo());
            setName(mRepresentative.getName());
            setOffice(mRepresentative.getOffice());
            if (mRepresentative.getmCountry() != null) {
                setmCountry(new MCountry(mRepresentative.getmCountry()));
            }
            if (mRepresentative.getmProvince() != null) {
                setmProvince(new MProvince(mRepresentative.getmProvince()));
            }
            if (mRepresentative.getmCity() != null) {
                setmCity(new MCity(mRepresentative.getmCity()));
            }
            setAddress(mRepresentative.getAddress());
            setZipCode(mRepresentative.getZipCode());
            setPhone(mRepresentative.getPhone());
            setEmail(mRepresentative.getEmail());
            setStatusFlag(mRepresentative.isStatusFlag());
            if (mRepresentative.getCreatedBy() != null) {
                setCreatedBy(new MUser(mRepresentative.getCreatedBy()));
            }
            setCreatedDate(mRepresentative.getCreatedDate());
            if (mRepresentative.getUserId() != null) {
                setUserId(new MUser(mRepresentative.getUserId()));
            }
        }
    }
    
    public MRepresentative(String id, String no, String name, String office, MCountry mCountry, MProvince mProvince, MCity mCity, String address, String zipCode, String phone, String email, boolean statusFlag) {
    	setId(id);
    	setNo(no);
    	setName(name);
    	setOffice(office);
    	setmCountry(mCountry);
    	setmProvince(mProvince);
    	setmCity(mCity);
    	setAddress(address);
    	setZipCode(zipCode);
    	setPhone(phone);
    	setEmail(email);
    	setStatusFlag(statusFlag);
    }

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "reprs_id", length = 36)
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "reprs_no", length = 20, nullable = true)
    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        try {
            this.no = no.trim();
        } catch (NullPointerException e) {
            this.no = null;
        }
    }

    @Column(name = "reprs_name", length = 255, nullable = false)
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

    @Column(name = "reprs_address", length = 500, nullable = true)
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

    @Column(name = "status_flag", nullable = false)
    public boolean isStatusFlag() {
        return statusFlag;
    }

    public void setStatusFlag(boolean statusFlag) {
        this.statusFlag = statusFlag;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    @JsonBackReference
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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public MUser getUserId() {
        return userId;
    }

    public void setUserId(MUser userId) {
        this.userId = userId;
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
		sb.append("^ Nomor Konsultan: " + getNo());
		sb.append("^ Nama Konsultan: " + getName());
		sb.append("^ Kantor Konsultan: " + getOffice());
		sb.append("^ Alamat Konsultan: " + getAddress());
		sb.append("^ Telepon: " + getPhone());
		sb.append("^ Email: " + getEmail());
		sb.append("^ Status: " + (isStatusFlag() ? "Aktif" : "Tidak Aktif"));
		return sb.toString();
	}
    
	
}
