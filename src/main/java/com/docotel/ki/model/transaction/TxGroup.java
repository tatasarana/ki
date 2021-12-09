package com.docotel.ki.model.transaction;

import com.docotel.ki.model.BaseModel;
import com.docotel.ki.model.master.MLookup;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.audittrail.EntityAuditTrailListener;

import javax.persistence.*;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tx_group")
@EntityListeners(EntityAuditTrailListener.class)
public class TxGroup extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private String no;
    private String name;
    private String description;
    private int total;
    private Boolean statusFlag;
    private MLookup groupType;
    private MUser createdBy;
    private Timestamp createdDate;
    private TxServDist txServDist;
    private TxPubsJournal txPubsJournal;
    private List<TxGroupDetail> txGroupDetailList;

    /***************************** - TRANSIENT FIELD SECTION - ****************************/

    private String createdDateTemp;

	/***************************** - CONSTRUCTOR SECTION - ****************************/
    public TxGroup() {
    }

    public TxGroup(TxGroup txGroup) {
        if (txGroup != null) {
            setId(txGroup.getCurrentId());
            setNo(txGroup.getNo());
            setName(txGroup.getName());
            setDescription(txGroup.getDescription());
            setTotal(txGroup.getTotal());
            setStatusFlag(txGroup.isStatusFlag());
            if (txGroup.getGroupType() != null) {
	            setGroupType(new MLookup(txGroup.getGroupType()));
            }
            if (txGroup.getCreatedBy() != null) {
            	setCreatedBy(new MUser(txGroup.getCreatedBy()));
            }
            setCreatedDate(txGroup.getCreatedDate());
            if (txGroup.getTxServDist() != null) {
            	setTxServDist(new TxServDist(txGroup.getTxServDist()));
            }
            if (txGroup.getTxPubsJournal() != null) {
            	setTxPubsJournal(new TxPubsJournal(txGroup.getTxPubsJournal()));
            }
            if (txGroup.getTxGroupDetailList() != null) {
            	setTxGroupDetailList(new ArrayList<>());
            	for (TxGroupDetail txGroupDetail : txGroup.getTxGroupDetailList()) {
            		getTxGroupDetailList().add(new TxGroupDetail(txGroupDetail));
	            }
            }
        }
    }
    
    public TxGroup(String id, String name) {
    	setId(id);
    	setName(name);
    }

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "group_id", length = 36)
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    @Column(name = "group_no", length = 50)
    //@Column(name = "group_no", length = 50, nullable = false, unique = true)
    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    @Column(name = "group_name", length = 100, nullable = false, unique = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "group_desc", length = 1000)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "group_total", nullable = false)
    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    @Column(name = "status_flag")
    public Boolean isStatusFlag() {
        return statusFlag;
    }

    public void setStatusFlag(Boolean statusFlag) {
        this.statusFlag = statusFlag;
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_type", nullable = false)
	public MLookup getGroupType() {
		return groupType;
	}

	public void setGroupType(MLookup groupType) {
		this.groupType = groupType;
	}
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    public MUser getCreatedBy() {
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
        this.createdDateTemp = DateUtil.formatDate(this.createdDate, "dd/MM/yyyy");
    }

    @OneToOne(mappedBy = "txGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    public TxServDist getTxServDist() {
        return txServDist;
    }

    public void setTxServDist(TxServDist txServDist) {
        this.txServDist = txServDist;
    }
    
    @OneToOne(mappedBy = "txGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    public TxPubsJournal getTxPubsJournal() {
        return txPubsJournal;
    }

    public void setTxPubsJournal(TxPubsJournal txPubsJournal) {
        this.txPubsJournal = txPubsJournal;
    }

    @OneToMany(mappedBy = "txGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Fetch(value = FetchMode.SUBSELECT)
    public List<TxGroupDetail> getTxGroupDetailList() {
        return txGroupDetailList;
    }

    public void setTxGroupDetailList(List<TxGroupDetail> txGroupDetailList) {
        this.txGroupDetailList = txGroupDetailList;
    }


    /***************************** - GETTER SETTER TRANSIENT METHOD SECTION - *****************************/
    @Transient
    public String getCreatedDateTemp() {
        return createdDateTemp;
    }

    public void setCreatedDateTemp(String createdDateTemp) {
        this.createdDateTemp = createdDateTemp;
        try {
            this.createdDate = DateUtil.toDate("dd/MM/yyyy", this.createdDateTemp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
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
        sb.append("^ Tipe Grup: " + getGroupType().getName());
        sb.append("^ Nomor Grup: " + getNo());
        sb.append("^ Nama Grup: " + getName());
        sb.append("^ Deskripsi Grup: " + getDescription());
        sb.append("^ Total Permohonan: " + getTotal());
        return sb.toString();
    }

    
    
    

}
