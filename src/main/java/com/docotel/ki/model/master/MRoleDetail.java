package com.docotel.ki.model.master;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

import com.docotel.ki.model.BaseModel;
import com.docotel.ki.audittrail.EntityAuditTrailListener;
import org.springframework.security.core.context.SecurityContextHolder;

@Entity
@Table(name = "m_role_detail")
@EntityListeners(EntityAuditTrailListener.class)
public class MRoleDetail extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private MRole mRole;
    private MMenuDetail mMenuDetail;
    private MUser createdBy;
    private Timestamp createdDate;

    /***************************** - TRANSIENT FIELD SECTION - ****************************/

    /***************************** - CONSTRUCTOR SECTION - ****************************/
    public MRoleDetail() {
    }

    public MRoleDetail(MRoleDetail mRoleDetail) {
        if (mRoleDetail != null) {
            setId(mRoleDetail.getCurrentId());
            if (mRoleDetail.getmRole() != null) {
                setmRole(new MRole(mRoleDetail.getmRole()));
            }
            if (mRoleDetail.getmMenuDetail() != null) {
                setmMenuDetail(new MMenuDetail(mRoleDetail.getmMenuDetail()));
            }
            if (mRoleDetail.getCreatedBy() != null) {
                setCreatedBy(new MUser(mRoleDetail.getCreatedBy()));
            }
            setCreatedDate(mRoleDetail.getCreatedDate());
        }
    }

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "role_detail_id", length = 36)
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }


    public void setmRole(MRole mRole) {
        this.mRole = mRole;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    public MRole getmRole() {
        return mRole;
    }

    public void setmAccessGroup(MRole mRole) {
        this.mRole = mRole;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_detail_id")
    public MMenuDetail getmMenuDetail() {
        return mMenuDetail;
    }

	public void setmMenuDetail(MMenuDetail mMenuDetail) {
		this.mMenuDetail = mMenuDetail;
	}

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    public MUser getCreatedBy() {
        if (createdBy == null) {
            createdBy = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }
        return createdBy;
    }

    public void setCreatedBy(MUser createdBy) {
        this.createdBy = createdBy;
    }

    @Column(name = "created_date", nullable = false)
    public Timestamp getCreatedDate() {
        if (createdDate == null) {
            createdDate = new Timestamp(System.currentTimeMillis());
        }
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
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
        sb.append("^ Nama Role: " + getmRole().getName());
        sb.append("^ Menu: " + getmMenuDetail().getDesc());
        return sb.toString();
    }
}
