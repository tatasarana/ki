package com.docotel.ki.model.master;

import com.docotel.ki.model.BaseModel;
import com.docotel.ki.audittrail.EntityAuditTrailListener;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.*;

@Entity
@Table(name = "m_menu_detail")
@EntityListeners(EntityAuditTrailListener.class)
public class MMenuDetail extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private MMenu mMenu;
    private String desc;
    private String url;

	/***************************** - TRANSIENT FIELD SECTION - ****************************/

    /***************************** - CONSTRUCTOR SECTION - ****************************/
    public MMenuDetail() {
    }

    public MMenuDetail(MMenuDetail mMenuDetail) {
        if (mMenuDetail != null) {
            setId(mMenuDetail.getCurrentId());
            if (mMenuDetail.getmMenu() != null) {
                setmMenu(new MMenu(mMenuDetail.getmMenu()));
            }
            setDesc(mMenuDetail.getDesc());
            setUrl(mMenuDetail.getUrl());
        }
    }

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "menu_detail_id", length = 36)
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
    @JoinColumn(name = "menu_id", nullable = false)
    public MMenu getmMenu() {
        return mMenu;
    }

    public void setmMenu(MMenu mMenu) {
        this.mMenu = mMenu;
    }

    @Column(name = "menu_detail_desc", length = 255, nullable = false)
    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Column(name = "menu_detail_url", length = 1000, nullable = true)
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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
        sb.append("^ Menu: " + getmMenu().getCurrentId());
        sb.append("^ Deskripsi: " + getDesc());
        sb.append("^ URL: " + getUrl());
        return sb.toString();
    }
}
