package com.docotel.ki.model.master;

import com.docotel.ki.model.BaseModel;
import com.docotel.ki.audittrail.EntityAuditTrailListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "m_lookup")
@EntityListeners(EntityAuditTrailListener.class)
public class MLookup extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private String code;
    private String name;
    private String groups;

	/***************************** - TRANSIENT FIELD SECTION - ****************************/

    /***************************** - CONSTRUCTOR SECTION - ****************************/
    public MLookup() {
    }

    public MLookup(MLookup groupType) {
        if (groupType != null) {
            setId(groupType.getCurrentId());
            setCode(groupType.getCode());
            setName(groupType.getName());
            setGroups(groupType.getGroups());
        }
    }

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "lookup_id", length = 36)
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "lookup_code", nullable = false, length=1000)
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Column(name = "lookup_name", nullable = false, length=1000)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "lookup_groups", nullable = false, length=1000)
    public String getGroups() {
        return groups;
    }

    public void setGroups(String groups) {
        this.groups = groups;
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
        sb.append("^ Kode: " + getCode());
        sb.append("^ Nama: " + getName());
        sb.append("^ Grup: " + getGroups());
        return sb.toString();
    }
}
