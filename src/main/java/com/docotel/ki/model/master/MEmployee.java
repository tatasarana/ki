package com.docotel.ki.model.master;

import com.docotel.ki.model.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.docotel.ki.audittrail.EntityAuditTrailListener;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "m_employee")
@EntityListeners(EntityAuditTrailListener.class)
public class MEmployee extends BaseModel implements Serializable {
    public static boolean flagLoop = false;

    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private String nik;
    private String telp;
    private String employeeName;
    private String email;
    private MSection mSection;
    private MUser userId;
    private MRoleSubstantifDetail mRoleSubstantifDetail;
    private boolean statusFlag;
    private String sign;

	public MEmployee() {
	}

	public MEmployee(MEmployee mEmployee) {
		if (mEmployee != null) {
			setId(mEmployee.getCurrentId());
			setNik(mEmployee.getNik());
			setTelp(mEmployee.getTelp());
			setEmployeeName(mEmployee.getEmployeeName());
			setEmail(mEmployee.getEmail());
			if (mEmployee.getmSection() != null) {
				setmSection(new MSection(mEmployee.getmSection()));
			}
			if (mEmployee.getCreatedBy() != null) {
				setCreatedBy(new MUser(mEmployee.getCreatedBy()));
			}
			setCreatedDate(mEmployee.getCreatedDate());
			if (mEmployee.getUserId() != null) {
				setUserId(new MUser(mEmployee.getUserId()));
			}
			if (mEmployee.getmRoleSubstantifDetail() != null) {
				setmRoleSubstantifDetail(new MRoleSubstantifDetail(mEmployee.getmRoleSubstantifDetail()));
			}
			setStatusFlag(mEmployee.isStatusFlag());
			setSign(mEmployee.getSign());
		}
	}
	
	public MEmployee(String id, String nik, String telp, String employeeName, String email, MSection mSection, MRoleSubstantifDetail mRoleSubstantifDetail, boolean statusFlag, String sign) {
		setId(id);
		setNik(nik);
		setTelp(telp);
		setEmployeeName(employeeName);
		setEmail(email);
		setmSection(mSection);
		setmRoleSubstantifDetail(mRoleSubstantifDetail);
		setStatusFlag(statusFlag);
		setSign(sign);
	}


	@Id
    @Column(name = "employee_id", length = 36)
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "employee_no", length = 500, nullable = true)
    public String getNik() {
        return nik;
    }

    public void setNik(String nik) {
        try {
            this.nik = nik.trim().toLowerCase();
        } catch (NullPointerException e) {
            this.nik = null;
        }
    }

    @Column(name = "employee_phone", length = 20, nullable = true)
    public String getTelp() {
        return telp;
    }

    public void setTelp(String telp) {
        try {
            this.telp = telp.trim().toLowerCase();
        } catch (NullPointerException e) {
            this.telp = null;
        }
    }

    @Column(name = "employee_name", length = 50, nullable = true)
    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        try {
            this.employeeName = employeeName.trim();
        } catch (NullPointerException e) {
            this.employeeName = null;
        }
    }

    @Column(name = "employee_email", length = 255, nullable = true)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = true)
    public MSection getmSection() {
        return mSection;
    }

    public void setmSection(MSection mSection) {
        this.mSection = mSection;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public MUser getUserId() {
        return userId;
    }

    public void setUserId(MUser userId) {
        this.userId = userId;
    }  
	
	@Column(name = "status_flag", nullable = false)
    public boolean isStatusFlag() {
        return statusFlag;
    }

    public void setStatusFlag(boolean statusFlag) {
        this.statusFlag = statusFlag;
    }
    
    @OneToOne(mappedBy = "mEmployee", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
//    @Transient
    public MRoleSubstantifDetail getmRoleSubstantifDetail() {
		return mRoleSubstantifDetail;
	}

	public void setmRoleSubstantifDetail(MRoleSubstantifDetail mRoleSubstantifDetail) {
		this.mRoleSubstantifDetail = mRoleSubstantifDetail;
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

    @Column(name = "signature")
	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	@Transient
    public String getCurrentId() {
        return this.id;
    }

	@Override
	public String logAuditTrail() {
		StringBuilder sb = new StringBuilder();
		sb.append("UUID: " + getCurrentId());
		sb.append("^ NIK: " + getNik());
		sb.append("^ Nama Karyawan: " + getEmployeeName());
		sb.append("^ Seksi: " + (getmSection() == null ? "" : getmSection().getName()));
		sb.append("^ Telepon: " + getTelp());
		sb.append("^ Email: " + getEmail());
		sb.append("^ Username: " + getUserId().getUsername());
		sb.append("^ Status: " + (isStatusFlag() ? "Aktif" : "Tidak Aktif"));
		return sb.toString();
	}

}
