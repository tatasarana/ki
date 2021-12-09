package com.docotel.ki.model.master;

import javax.persistence.*;

import com.docotel.ki.model.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.docotel.ki.audittrail.EntityAuditTrailListener;
import com.docotel.ki.enumeration.UserEnum;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "m_user_role")
@EntityListeners(EntityAuditTrailListener.class)
public class MUserRole extends BaseModel implements Serializable {
	/***************************** - FIELD SECTION - ****************************/
	private String id;
	private MUser mUser;
	private MRole mRole;
	/***************************** - TRANSIENT FIELD SECTION - ****************************/

	/***************************** - CONSTRUCTOR SECTION - ****************************/

	/***************************** - GETTER SETTER METHOD SECTION - ****************************/
	@Id
	@Column(name = "user_role_id", length = 36)
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
	@JoinColumn(name = "user_id", nullable = false)
	public MUser getmUser() {
		return mUser;
	}

	public void setmUser(MUser mUser) {
		this.mUser = mUser;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "role_id", nullable = false)
	public MRole getmRole() {
		return mRole;
	}

	public void setmRole(MRole mRole) {
		this.mRole = mRole;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by", nullable = false, updatable = false)
	public MUser getCreatedBy() {
		if (createdBy == null) {
			Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			if(principal instanceof  UserDetails) {
				createdBy = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        	} else {
        		createdBy = (MUser) UserEnum.SYSTEM.value();
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
			Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			if(principal instanceof  UserDetails) {
				updatedBy = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        	} else {
        		updatedBy = (MUser) UserEnum.SYSTEM.value();
        	}
		}
        return updatedBy;
    }

    public void setUpdatedBy(MUser updatedBy) {
        this.updatedBy = updatedBy;
    }

	@Transient
	public String getCurrentId() {
		return this.id;
	}

	@Override
	public String logAuditTrail() {
		StringBuilder sb = new StringBuilder();
		sb.append("UUID: " + getCurrentId());
		sb.append("^ Username: " + getmUser().getUsername());
		sb.append("^ Nama Role: " + getmRole().getName());
		return sb.toString();
	}
	
	

	/***************************** - OTHER METHOD SECTION - ****************************/
}
