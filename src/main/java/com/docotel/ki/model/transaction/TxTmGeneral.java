package com.docotel.ki.model.transaction;

import com.docotel.ki.enumeration.UserEnum;
import com.docotel.ki.model.BaseModel;
import com.docotel.ki.model.master.MAction;
import com.docotel.ki.model.master.MLaw;
import com.docotel.ki.model.master.MStatus;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.util.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.docotel.ki.audittrail.EntityAuditTrailListener;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tx_tm_general")
@EntityListeners(EntityAuditTrailListener.class)
public class TxTmGeneral extends BaseModel {
    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private TxReception txReception;
    
    private String applicationNo;
    private MLaw mLaw;
    private Timestamp filingDate;
    private MStatus mStatus;
    private MAction mAction;

    private String ownerList;
    private String represList;
    private String classList;
    private String priorList;
    private boolean onlineFlag;

    private boolean groupJournal;
    private boolean Locked;
    private boolean groupDist;
    private String xmlFileId;
    private String irn;

    private MUser LockedBy ;
    private TxMadrid madrid_id ;
    private Set<TxTmOwner> txTmOwner;

    private boolean isLocked;
    private String language1;
    private String language2;

    private Timestamp approvedDate;
    private MUser approvedBy;

    private Set<TxTmRepresentative> txTmRepresentative;
    private List<TxTmPrior> txTmPriorList;
    private TxTmBrand txTmBrand;
    private List<TxSubsCheck> txSubsCheckList;
    private List<TxTmClass> txTmClassList;
    private List<TxTmDoc> txTmDocList;
    private TxRegistration txRegistration;
    private List<TxLicense> txLicenseList;
    private List<TxGroupDetail> txGroupDetail;
    private TxTmMadridFee txTmMadridFee;
    
    
    /***************************** - TRANSIENT FIELD SECTION - ****************************/
    private String filingDateTemp;
    private boolean similar;
    private String fillingDateTemp2;

    /***************************** - CONSTRUCTOR SECTION - ****************************/

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    public TxTmGeneral() {
    }
    
    public TxTmGeneral(String id, TxReception txReception, String applicationNo, MLaw mLaw, MStatus mStatus, MAction mAction, String ownerList, String represList, String priorList, boolean onlineFlag,
    		boolean groupJournal, boolean Locked, boolean groupDist, String xmlFileId, Set<TxTmOwner> txTmOwner, Set<TxTmRepresentative> txTmRepresentative, List<TxTmPrior> txTmPriorList,
    		TxTmBrand txTmBrand, List<TxTmClass> txTmClassList, List<TxTmDoc> txTmDocList, TxRegistration txRegistration, List<TxLicense> txLicenseList, MUser LockedBy,TxMadrid txMadrid) {
    	setId(id);
    	setTxReception(txReception);
    	setApplicationNo(applicationNo);
    	setmLaw(mLaw);
    	setmStatus(mStatus);
    	setmAction(mAction);
    	setOwnerList(ownerList);
    	setRepresList(represList);
    	setPriorList(priorList);
    	setOnlineFlag(onlineFlag);
    	setGroupJournal(groupJournal);
        setLocked(Locked);
    	setGroupDist(groupDist);
    	setXmlFileId(xmlFileId);
    	setTxTmOwner(txTmOwner);
    	setTxTmRepresentative(txTmRepresentative);
    	setTxTmPriorList(txTmPriorList);
    	setTxTmBrand(txTmBrand);
    	setTxTmClassList(txTmClassList);
    	setTxTmDocList(txTmDocList);
    	setTxRegistration(txRegistration);
    	setTxLicenseList(txLicenseList);
    	setLockedBy(LockedBy);
    	setMadrid_id(txMadrid);
    }

    @Id
    @Column(name = "application_id", length = 36)
    public String getId() {
        if (id == null) {
            id = applicationNo; //UUID.randomUUID().toString();
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)    
    @JoinColumn(name = "reception_id")
    public TxReception getTxReception() {
        return txReception;
    }

    public void setTxReception(TxReception txReception) {
        this.txReception = txReception;
    }

    @Column(name = "application_no", length = 50, nullable = false, unique = true)
    public String getApplicationNo() {
        return applicationNo;
    }

    public void setApplicationNo(String applicationNo) {
        this.applicationNo = applicationNo;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "law_id")
    public MLaw getmLaw() {
        return mLaw;
    }

    public void setmLaw(MLaw mLaw) {
        this.mLaw = mLaw;
    }

    @Column(name = "filing_date")
    public Timestamp getFilingDate() {
        return filingDate;
    }

    public void setFilingDate(Timestamp filingDate) {
        this.filingDate = filingDate;
        this.filingDateTemp = DateUtil.formatDate(this.filingDate, "dd/MM/yyyy HH:mm:ss");
        this.fillingDateTemp2 = DateUtil.formatDate(this.filingDate, "dd/MM/yyyy");
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    public MStatus getmStatus() {
        return mStatus;
    }
    
    public void setmStatus(MStatus mStatus) {
        this.mStatus = mStatus;
    }
    

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id")
    public MAction getmAction() {
        return mAction;
    }

    public void setmAction(MAction mAction) {
        this.mAction = mAction;
    }

    @Column(name = "group_journal")
    public boolean isGroupJournal() {
        return groupJournal;
    }

    public void setGroupJournal(boolean groupJournal) {
        this.groupJournal = groupJournal;
    }

    @Column(name = "is_locked")
    public boolean isLocked() {
        return Locked;
    }

    public void setLocked (boolean Locked) {
        this.Locked = Locked;
    }



    @Column(name = "group_dist")
    public boolean isGroupDist() {
        return groupDist;
    }

    public void setGroupDist(boolean groupDist) {
        this.groupDist = groupDist;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    @JsonIgnore
    public MUser getCreatedBy() {
        if (createdBy == null) {
            Object principal = null;
            try {
                principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            } catch (NullPointerException e) {
            }
            if (principal != null && principal instanceof UserDetails) {
                createdBy = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            } else {
                createdBy = UserEnum.SYSTEM.value();
            }
        }
        return createdBy;
    }

    public void setCreatedBy(MUser createdBy) {
        this.createdBy = createdBy;
    }


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locked_by")
    @JsonIgnore
    public MUser getLockedBy() {
        return LockedBy;
    }

    public void setLockedBy(MUser LockedBy) {
        this.LockedBy = LockedBy;
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
            Object principal = null;
            try {
                principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            }catch (NullPointerException e){
            }
            if(principal != null && principal instanceof UserDetails) {
                updatedBy = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            } else {
                updatedBy = UserEnum.SYSTEM.value();
            }
        }
        return updatedBy;
    }

    public void setUpdatedBy(MUser updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Column(name = "xml_file_id", nullable = true)
	public String getXmlFileId() {
		return xmlFileId;
	}

	public void setXmlFileId(String xmlFileId) {
		this.xmlFileId = xmlFileId;
	}

    public String getIrn() {
        return irn;
    }

    public void setIrn(String irn) {
        this.irn = irn;
    }

    //	@Column(name = "is_locked",columnDefinition = "int default 0" )
//	public boolean isLocked() {
//		return isLocked;
//	}
//
//	public void setLocked(boolean isLocked) {
//		this.isLocked = isLocked;
//	}

	/*@OneToOne(mappedBy = "txTmGeneral", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)*/
    @OneToMany(mappedBy = "txTmGeneral", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Fetch(value = FetchMode.SUBSELECT)
    public Set<TxTmOwner> getTxTmOwner() {
        return txTmOwner;
    }

    public void setTxTmOwner(Set<TxTmOwner> txTmOwner) {
        this.txTmOwner = txTmOwner;
    }
    

    /*@OneToOne(mappedBy = "txTmGeneral", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true, orphanRemoval = true)*/
    @OneToMany(mappedBy = "txTmGeneral", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Fetch(value = FetchMode.SUBSELECT)
    public Set<TxTmRepresentative> getTxTmRepresentative() {
		return txTmRepresentative;
	}

	public void setTxTmRepresentative(Set<TxTmRepresentative> txTmRepresentative) {
		this.txTmRepresentative = txTmRepresentative;
	}

	@OneToMany(mappedBy = "txTmGeneral", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Fetch(value = FetchMode.SUBSELECT)
    public List<TxTmPrior> getTxTmPriorList() {
        return txTmPriorList;
    }

    public void setTxTmPriorList(List<TxTmPrior> txTmPriorList) {
        this.txTmPriorList = txTmPriorList;
    }

    @OneToOne(mappedBy = "txTmGeneral", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    public TxTmBrand getTxTmBrand() {
        return txTmBrand;
    }

    public void setTxTmBrand(TxTmBrand txTmBrand) {
        this.txTmBrand = txTmBrand;
    }

    @OneToMany(mappedBy = "txTmGeneral", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Fetch(value = FetchMode.SUBSELECT)
    public List<TxSubsCheck> getTxSubsCheckList() {
        return txSubsCheckList;
    }

    public void setTxSubsCheckList(List<TxSubsCheck> txSubsCheckList) {
        this.txSubsCheckList = txSubsCheckList;
    }

    @OneToMany(mappedBy = "txTmGeneral", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Fetch(value = FetchMode.SUBSELECT)
    public List<TxTmClass> getTxTmClassList() {
        return txTmClassList;
    }

    public void setTxTmClassList(List<TxTmClass> txTmClassList) {
        this.txTmClassList = txTmClassList;
    }

    @OneToMany(mappedBy = "txTmGeneral", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Fetch(value = FetchMode.SUBSELECT)
    public List<TxTmDoc> getTxTmDocList() {
        return txTmDocList;
    }

    public void setTxTmDocList(List<TxTmDoc> txTmDocList) {
        this.txTmDocList = txTmDocList;
    }

    @OneToOne(mappedBy = "txTmGeneral", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    public TxRegistration getTxRegistration() {
        return txRegistration;
    }

    public void setTxRegistration(TxRegistration txRegistration) {
        this.txRegistration = txRegistration;
    }
    
    @OneToMany(mappedBy = "txTmGeneral", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Fetch(value = FetchMode.SUBSELECT)
	public List<TxLicense> getTxLicenseList() {
		return txLicenseList;
	}

	public void setTxLicenseList(List<TxLicense> txLicenseList) {
		this.txLicenseList = txLicenseList;
	}

	@OneToMany(mappedBy = "txTmGeneral", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Fetch(value = FetchMode.SUBSELECT)
	public List<TxGroupDetail> getTxGroupDetail() {
		return txGroupDetail;
	}

	public void setTxGroupDetail(List<TxGroupDetail> txGroupDetail) {
		this.txGroupDetail = txGroupDetail;
	}
	
	@OneToOne(mappedBy = "txTmGeneral", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    public TxTmMadridFee getTxTmMadridFee() {
        return txTmMadridFee;
    }

    public void setTxTmMadridFee(TxTmMadridFee txTmMadridFee) {
        this.txTmMadridFee = txTmMadridFee;
    }
	
	@Column(name = "language_1")
    public String getLanguage1() {
		return language1;
	}

	public void setLanguage1(String language1) {
		this.language1 = language1;
	}

	@Column(name = "language_2")
	public String getLanguage2() {
		return language2;
	}

	public void setLanguage2(String language2) {
		this.language2 = language2;
	}

	/***************************** - GETTER SETTER TRANSIENT METHOD SECTION - *****************************/
    @Transient
    public String getFilingDateTemp() {
        filingDateTemp = DateUtil.formatDate(this.filingDate, "dd/MM/yyyy HH:mm:ss");
        return filingDateTemp;
    }

    public void setFilingDateTemp(String filingDateTemp) {
        this.filingDateTemp = filingDateTemp;
        try {
            this.filingDate = DateUtil.toDate("dd/MM/yyyy HH:mm:ss", this.filingDateTemp);
        } catch (ParseException e) {
        }
    }
    
    @Transient
    public String getFillingDateTemp2() {
        return fillingDateTemp2;
    }

    public void setFillingDateTemp2(String fillingDateTemp2) {
        this.fillingDateTemp2 = fillingDateTemp2;
    }

	@Transient
	public boolean isSimilar() {
		return similar;
	}

	public void setSimilar(boolean similar) {
		this.similar = similar;
	}

	/***************************** - OTHER METHOD SECTION - ****************************/

    @Transient
    public String getCurrentId() {
        return this.id;
    }
    
//    public String toClassListJson() {
//    	txTmClassList.stream();
//    }

    @Override
    public String logAuditTrail() {
        StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + getCurrentId());
        if (getApplicationNo() != null) {
        	sb.append("^ Nomor Permohonan: " + getApplicationNo());
        } else {
        	sb.append("^ Nomor Permohonan: -");
        }
        if (getFilingDate() != null) {
        	sb.append("^ Tanggal Penerimaan: " + getFilingDate());
        } else {
        	sb.append("^ Tanggal Penerimaan: -");
        }
        if (isGroupDist()) {
        	sb.append("^ Grup Pelayanan Teknis: " + (isGroupDist() ? "Ya" : "Tidak"));
        } 
        if (isGroupJournal()) {
        	sb.append("^ Grup Publikasi: " + (isGroupJournal() ? "Ya" : "Tidak"));
        }
        if (getmStatus()!= null) {
        	sb.append("^ Status: " + getmStatus().getName());
        } else {
        	sb.append("^ Status: -");
        }
        if (getmAction()!= null) {
            sb.append("^ Aksi: " + getmAction().getName());
        } else {
            sb.append("^ Aksi: -");
        }
        if (getmLaw()!= null) {
            sb.append("^ Dasar Hukum: " + getmLaw().getDesc());
        } else {
            sb.append("^ Dasar Hukum: -");
        }
        return sb.toString();
    }

    @Column(name = "owner_list")
    public String getOwnerList() {
        return ownerList;
    }

    public void setOwnerList(String ownerList) {
        this.ownerList = ownerList;
    }

    @Column(name = "repres_list")
    public String getRepresList() {
        return represList;
    }

    public void setRepresList(String represList) {
        this.represList = represList;
    }

    @Column(name = "class_list")
    public String getClassList() {
        return classList;
    }

    public void setClassList(String classList) {
        this.classList = classList;
    }

    @Column(name = "prior_list")
    public String getPriorList() {
        return priorList;
    }

    public void setPriorList(String priorList) {
        this.priorList = priorList;
    }

    @Column(name = "reception_online_flag")
    public boolean isOnlineFlag()  {
        try {
            if(this.getTxReception()==null){
                return false;
            }
            onlineFlag = this.getTxReception().isOnlineFlag();
        } catch (Exception e) {
            onlineFlag = false;
        }
        return onlineFlag;
    }

    public void setOnlineFlag(boolean onlineFlag) {
        this.onlineFlag = onlineFlag;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "madrid_id")
    @JsonIgnore
    public TxMadrid getMadrid_id() {
        return madrid_id;
    }

    public void setMadrid_id(TxMadrid madrid_id) {
        this.madrid_id = madrid_id;
    }
    
    @Column(name = "approved_date")
    public Timestamp getApprovedDate() {
		return approvedDate;
	}

	public void setApprovedDate(Timestamp approvedDate) {
		this.approvedDate = approvedDate;
	}

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    @JsonIgnore
	public MUser getApprovedBy() {
		return approvedBy;
	}

	public void setApprovedBy(MUser approvedBy) {
		this.approvedBy = approvedBy;
	}
    
    public String[][] toJsonClass() {
    	List<String[]> classes = new ArrayList<>();
    	for(TxTmClass txTmClass : txTmClassList) {
    		String[] kelas = new String[9];
    		kelas[0] = txTmClass.getmClassDetail().getId();
    		kelas[1] = txTmClass.getmClass().getNo().toString();
    		kelas[2] = txTmClass.getmClassDetail().getSerial1();
    		kelas[3] = txTmClass.getmClassDetail().getDesc();
    		kelas[4] = txTmClass.getmClassDetail().getDescEn();
    		kelas[5] = txTmClass.getmClass().getEdition();
    		kelas[6] = txTmClass.getmClass().getVersion().toString();
    		kelas[7] = "show";
    		kelas[8] = "Dagang";
    		classes.add(kelas);
    	}
    	return classes.toArray(new String[txTmClassList.size()][9]);
    }

}
