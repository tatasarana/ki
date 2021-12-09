package com.docotel.ki.model.master;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

import javax.persistence.*;

import com.docotel.ki.model.BaseModel;
import com.docotel.ki.audittrail.EntityAuditTrailListener;

@Entity
@Table(name = "m_menu")
@EntityListeners(EntityAuditTrailListener.class)
public class MMenu extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private String code;
    private String name;
    private boolean statusFlag;
    private MUser createdBy;
    private Timestamp createdDate;

	/***************************** - TRANSIENT FIELD SECTION - ****************************/

    /***************************** - CONSTRUCTOR SECTION - ****************************/
    public MMenu() {
    }

    public MMenu(MMenu mMenu) {
        if (mMenu != null) {
            setId(mMenu.getCurrentId());
            setCode(mMenu.getCode());
            setName(mMenu.getName());
            setStatusFlag(mMenu.isStatusFlag());
            if (mMenu.getCreatedBy() != null) {
                setCreatedBy(new MUser(mMenu.getCreatedBy()));
            }
            setCreatedDate(mMenu.getCreatedDate());
        }
    }

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "menu_id", length = 36)
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "menu_code", length = 10, nullable = false, unique = true)
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Column(name = "menu_name", length = 255, nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    public MUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(MUser createdBy) {
        this.createdBy = createdBy;
    }

    @Column(name = "status_flag", nullable = false)
    public boolean isStatusFlag() {
        return statusFlag;
    }

    public void setStatusFlag(boolean statusFlag) {
        this.statusFlag = statusFlag;
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
        return id;
    }

    @Override
    public String logAuditTrail() {
        StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + getCurrentId());
        sb.append("^ Kode Menu: " + getCode());
        sb.append("^ Nama Menu: " + getName());
        sb.append("^ Status: " + (isStatusFlag() ? "Aktif" : "Tidak Aktif"));
        return sb.toString();
    }
}
