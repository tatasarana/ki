package com.docotel.ki.model.transaction;

import com.docotel.ki.model.BaseModel;
import com.docotel.ki.model.master.MLaw;
import com.docotel.ki.model.master.MLookup;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.audittrail.EntityAuditTrailListener;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.UUID;

@Entity
@Table(name = "tx_pubs_journal")
@EntityListeners(EntityAuditTrailListener.class)
public class TxPubsJournal extends BaseModel implements Serializable {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private String journalNo;
    private MLaw mLaw;
    private Timestamp journalStart;
    private Timestamp journalEnd;
    private TxGroup txGroup;
    private String journalNotes;
    private MUser createdBy;
    private Timestamp createdDate;
    private int validityPeriod;
    private MLookup journalType;

    /***************************** - TRANSIENT FIELD SECTION - ****************************/
    private String journalStartTemp;
    private String journalEndTemp;

    /***************************** - CONSTRUCTOR SECTION - ****************************/
    public TxPubsJournal() {
    }

    public TxPubsJournal(TxPubsJournal txPubsJournal) {
        if (txPubsJournal != null) {
            setId(txPubsJournal.getCurrentId());
            setJournalNo(txPubsJournal.getJournalNo());
            if (txPubsJournal.getmLaw() != null) {
                setmLaw(new MLaw(txPubsJournal.getmLaw()));
            }
            setJournalStart(txPubsJournal.getJournalStart());
            setJournalEnd(txPubsJournal.getJournalEnd());
            if (txPubsJournal.getTxGroup() != null) {
                setTxGroup(new TxGroup(txPubsJournal.getTxGroup()));
            }
            setJournalNotes(txPubsJournal.getJournalNotes());
            if (txPubsJournal.getCreatedBy() != null) {
                setCreatedBy(txPubsJournal.getCreatedBy());
            }
            setCreatedDate(txPubsJournal.getCreatedDate());
            setValidityPeriod(txPubsJournal.getValidityPeriod());
            if (txPubsJournal.getJournalType() != null) {
                setJournalType(new MLookup(txPubsJournal.getJournalType()));
            }
        }
    }

    /***************************** - GETTER SETTER FIELD SECTION - ****************************/
	@Id
    @Column(name = "journal_id", length = 36)
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(nullable = false, unique = true)
    public String getJournalNo() {
        return journalNo;
    }

    public void setJournalNo(String journalNo) {
        this.journalNo = journalNo;
    }
//	public String getJournalName() {
//		return journalName;
//	}
//	public void setJournalName(String journalName) {
//		this.journalName = journalName;
//	}

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "law_id")
    public MLaw getmLaw() {
        return mLaw;
    }

    public void setmLaw(MLaw mLaw) {
        this.mLaw = mLaw;
    }

    //	public Integer getJournalPeriod() {
//		return journalPeriod;
//	}
//	public void setJournalPeriod(Integer journalPeriod) {
//		this.journalPeriod = journalPeriod;
//	}
//
    @Column(name = "journal_start")
    public Timestamp getJournalStart() {
        return journalStart;
    }

    public void setJournalStart(Timestamp journalStart) {
        this.journalStart = journalStart;
        this.journalStartTemp = DateUtil.formatDate(this.journalStart, "dd/MM/yyyy");
    }

    @Column(name = "journal_end")
    public Timestamp getJournalEnd() {
        return journalEnd;
    }

    public void setJournalEnd(Timestamp journalEnd) {
        this.journalEnd = journalEnd;
        this.journalEndTemp = DateUtil.formatDate(this.journalEnd, "dd/MM/yyyy");
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    public TxGroup getTxGroup() {
        return txGroup;
    }

    public void setTxGroup(TxGroup txGroup) {
        this.txGroup = txGroup;
    }


    public String getJournalNotes() {
        return journalNotes;
    }

    public void setJournalNotes(String journalNotes) {
        this.journalNotes = journalNotes;
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
    }

    //	public String getCreatedDiv() {
//		return createdDiv;
//	}
//	public void setCreatedDiv(String createdDiv) {
//		this.createdDiv = createdDiv;
//	}
//
    @Column(name = "valid_period")
    public int getValidityPeriod() {
        return validityPeriod;
    }

    public void setValidityPeriod(int validityPeriod) {
        this.validityPeriod = validityPeriod;
    }
               
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_type", nullable = true)
    public MLookup getJournalType() {
		return journalType;
	}

	public void setJournalType(MLookup journalType) {
		this.journalType = journalType;
	}
	
	
	

	/***************************** - GETTER SETTER TRANSIENT METHOD SECTION - *****************************/
    @Transient
    public String getJournalStartTemp() {
        return journalStartTemp;
    }

   

	public void setJournalStartTemp(String journalStartTemp) {
        this.journalStartTemp = journalStartTemp;
        try {
            this.journalStart = DateUtil.toDate("dd/MM/yyyy", this.journalStartTemp);
        } catch (ParseException e) {
        }
    }

    @Transient
    public String getJournalEndTemp() {
        return journalEndTemp;
    }

    public void setjournalEndTemp(String journalEndTemp) {
        this.journalEndTemp = journalEndTemp;
        try {
            this.journalEnd = DateUtil.toDate("dd/MM/yyyy", this.journalEndTemp);
        } catch (ParseException e) {
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
        sb.append("^ Nomor BRM: " + getJournalNo());
        sb.append("^ Masa Berlaku: " + getValidityPeriod());
        sb.append("^ Tanggal Publikasi: " + getJournalStart());
        sb.append("^ Tanggal Berakhir: " + getJournalEnd());
        sb.append("^ Dasar Hukum: " + getmLaw().getDesc()); 
        sb.append("^ Nama Grup: " + getTxGroup().getName());
        return sb.toString();
    }

}
