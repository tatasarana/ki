package com.docotel.ki.model.master;

import javax.persistence.*;

import com.docotel.ki.model.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.docotel.ki.audittrail.EntityAuditTrailListener;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.docotel.ki.model.transaction.TxServDist;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "m_role_subs_detail")
@EntityListeners(EntityAuditTrailListener.class)
public class MRoleSubstantifDetail extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private MRoleSubstantif mRoleSubstantif;
    private MEmployee mEmployee;

    private List<TxServDist> txServDistList;

	/***************************** - TRANSIENT FIELD SECTION - ****************************/

    /***************************** - CONSTRUCTOR SECTION - ****************************/

    public MRoleSubstantifDetail() {
    }

	public MRoleSubstantifDetail(MRoleSubstantifDetail mRoleSubstantifDetail) {
		if (mRoleSubstantifDetail != null) {
			setId(mRoleSubstantifDetail.getCurrentId());
			if (mRoleSubstantifDetail.getmRoleSubstantif() != null) {
				setmRoleSubstantif(new MRoleSubstantif(mRoleSubstantifDetail.getmRoleSubstantif()));
			}
			if (mRoleSubstantifDetail.getmEmployee() != null) {
				setmEmployee(new MEmployee(mRoleSubstantifDetail.getmEmployee()));
			}
			if (mRoleSubstantifDetail.getCreatedBy() != null) {
				setCreatedBy(new MUser(mRoleSubstantifDetail.getCreatedBy()));
			}
			setCreatedDate(mRoleSubstantifDetail.getCreatedDate());
			if (mRoleSubstantifDetail.getTxServDistList() != null) {
				setTxServDistList(new ArrayList<>());
				for (TxServDist txServDist : mRoleSubstantifDetail.getTxServDistList()) {
					getTxServDistList().add(new TxServDist(txServDist));
				}
			}
		}
	}

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "role_subs_detail_id", length = 36)
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
    @JoinColumn(name = "role_subs_id")
    public MRoleSubstantif getmRoleSubstantif() {
        return mRoleSubstantif;
    }
   
	public void setmRoleSubstantif(MRoleSubstantif mRoleSubstantif) {
        this.mRoleSubstantif = mRoleSubstantif;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    public MEmployee getmEmployee() {
        return mEmployee;
    }

	public void setmEmployee(MEmployee mEmployee) {
		this.mEmployee = mEmployee;
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
    
    @OneToMany(mappedBy = "mRoleSubstantifDetail", cascade = CascadeType.DETACH, fetch = FetchType.LAZY, orphanRemoval = true)
    @Fetch(value = FetchMode.SUBSELECT)
//    @Transient
    public List<TxServDist> getTxServDistList() {
        return txServDistList;
    }

    public void setTxServDistList(List<TxServDist> txServDistList) {
        this.txServDistList = txServDistList;
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
        sb.append("^ Nama Karyawan: " + getmEmployee().getEmployeeName());
        sb.append("^ Nama Role Subtantif: " + getmRoleSubstantif().getName());
        return sb.toString();
    }
}