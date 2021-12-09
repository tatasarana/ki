package com.docotel.ki.model.master;

import com.docotel.ki.model.BaseModel;
import com.docotel.ki.audittrail.EntityAuditTrailListener;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "m_file_type_detail")
@EntityListeners(EntityAuditTrailListener.class)
public class MFileTypeDetail extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private String code;
    private String desc;
    private boolean statusFlag;
    private MUser createdBy;
    private Timestamp createdDate;

    /***************************** - TRANSIENT FIELD SECTION - ****************************/

    /***************************** - CONSTRUCTOR SECTION - ****************************/

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "file_type_detail_id", length = 36)
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "file_type_detail_code", length = 10, nullable = false, unique = true)
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Column(name = "file_type_detail_desc", length = 255, nullable = false)
    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        try {
            this.desc = desc.trim();
        } catch (NullPointerException e) {
            this.desc = null;
        }
    }
    
    @Column(name = "status_flag", nullable = false)
    public boolean isStatusFlag() {
        return statusFlag;
    }

    public void setStatusFlag(boolean statusFlag) {
        this.statusFlag = statusFlag;
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
        return id;
    }

    @Override
    public String logAuditTrail() {
        StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + getCurrentId());
        sb.append("^ Kode Jenis Permohonan: " + getCode());
        sb.append("^ Jenis Permohonan: " + getDesc());
        sb.append("^ Status: " + (isStatusFlag() ? "Aktif" : "Tidak Aktif"));
        return sb.toString();
    }
}
