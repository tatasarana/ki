package com.docotel.ki.model.transaction;

import com.docotel.ki.model.BaseModel;
import com.docotel.ki.model.master.*;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.util.NumberUtil;
import com.docotel.ki.audittrail.EntityAuditTrailListener;
import com.docotel.ki.model.master.*;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tx_reception")
@EntityListeners(EntityAuditTrailListener.class)
public class TxReception extends BaseModel implements Serializable {
	/***************************** - FIELD SECTION - ****************************/
	private String id;
	private MFileSequence mFileSequence;
	private MFileType mFileType;
	private MFileTypeDetail mFileTypeDetail;
	private String applicationNo;
	private String eFilingNo;
	private Timestamp applicationDate;
	private Timestamp paymentDate;
	private String bankCode;
	private int totalClass;
	private BigDecimal totalPayment;
	private MUser createdBy;
	private Timestamp createdDate;
	private boolean onlineFlag;
	private MWorkflow mWorkflow;
	private String xmlFileId;
	private String applicantNotes;
	private String notesIpas;

	/***************************** - TRANSIENT FIELD SECTION - ****************************/

	//sky
	private String applicationDateTemp;
	private String paymentDateTemp;
	private String totalClassTemp;
	private String totalPaymentTemp;
	private String lawTemp;
	private TxTmOwner txTmOwner;
	private List<MLookup> languangeList;
	private MLookup mLanguage;
	private String applicationNoNew;
	//end

	/***************************** - CONSTRUCTOR SECTION - ****************************/

	/***************************** - GETTER SETTER METHOD SECTION - ****************************/
	@Id
	@Column(name = "reception_id", length = 36)
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
	@JoinColumn(name = "file_seq_id")
	public MFileSequence getmFileSequence() {
		return mFileSequence;
	}

	public void setmFileSequence(MFileSequence mFileSequence) {
		this.mFileSequence = mFileSequence;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "file_type_id")
	public MFileType getmFileType() {
		return mFileType;
	}

	public void setmFileType(MFileType mFileType) {
		this.mFileType = mFileType;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "file_type_detail_id")
	public MFileTypeDetail getmFileTypeDetail() {
		return mFileTypeDetail;
	}

	public void setmFileTypeDetail(MFileTypeDetail mFileTypeDetail) {
		this.mFileTypeDetail = mFileTypeDetail;
	}

	@Column(name = "efiling_no", length = 50, nullable = true)
	public String geteFilingNo() {
		return eFilingNo;
	}

	public void seteFilingNo(String eFilingNo) {
		this.eFilingNo = eFilingNo;
	}

	@Column(name = "application_no", length = 50, nullable = false, unique = true)
	public String getApplicationNo() {
		return applicationNo;
	}

	public void setApplicationNo(String applicationNo) {
		this.applicationNo = applicationNo;
	}

	@Column(name = "application_date")
	public Timestamp getApplicationDate() {
		return applicationDate;
	}

	public void setApplicationDate(Timestamp applicationDate) {
		this.applicationDate = applicationDate;
		this.applicationDateTemp = DateUtil.formatDate(this.applicationDate, "dd/MM/yyyy HH:mm:ss");
	}

	@Column(name = "payment_date")
	public Timestamp getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(Timestamp paymentDate) {
		this.paymentDate = paymentDate;
		this.paymentDateTemp = DateUtil.formatDate(this.paymentDate, "dd/MM/yyyy HH:mm:ss");
	}

	@Column(name = "bank_code", nullable = true, unique = true)
	public String getBankCode() {
		return bankCode;
	}

	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}

	@Column(name = "total_class")
	public int getTotalClass() {
		return totalClass;
	}

	public void setTotalClass(int totalClass) {
		this.totalClass = totalClass;
		this.totalClassTemp = NumberUtil.formatInteger(this.totalClass);
	}

	@Column(name = "total_payment")
	public BigDecimal getTotalPayment() {
		return totalPayment;
	}

	public void setTotalPayment(BigDecimal totalPayment) {
		this.totalPayment = totalPayment;
		this.totalPaymentTemp = NumberUtil.formatDecimal(this.totalPayment);
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by"/*, nullable = false, insertable=false, updatable=false*/)
	public MUser getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(MUser createdBy) {
		this.createdBy = createdBy;
	}

	@Column(name = "created_date")
	public Timestamp getCreatedDate() {
		if (createdDate == null) {
			createdDate = new Timestamp(System.currentTimeMillis());
		}
		return createdDate;
	}

	public void setCreatedDate(Timestamp createdDate) {
		this.createdDate = createdDate;
	}

	@Column(name = "online_flag", nullable = false)
	public boolean isOnlineFlag() {
		return onlineFlag;
	}

	public void setOnlineFlag(boolean onlineFlag) {
		this.onlineFlag = onlineFlag;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workflow_id")
	public MWorkflow getmWorkflow() {
		return mWorkflow;
	}

	public void setmWorkflow(MWorkflow mWorkflow) {
		this.mWorkflow = mWorkflow;
	}

	@Column(name = "xml_file_id", nullable = true)
	public String getXmlFileId() {
		return xmlFileId;
	}

	public void setXmlFileId(String xmlFileId) {
		this.xmlFileId = xmlFileId;
	}
	
	@Column(name = "applicant_notes", nullable = true)
	public String getApplicantNotes() {
		return applicantNotes;
	}

	public void setApplicantNotes(String applicantNotes) {
		this.applicantNotes = applicantNotes;
	}

	@Lob
	@Column(name = "notes_ipas")
	public String getNotesIpas() {
		return notesIpas;
	}

	public void setNotesIpas(String notesIpas) {
		this.notesIpas = notesIpas;
	}

	/***************************** - GETTER SETTER TRANSIENT METHOD SECTION - *****************************/
	@Transient
	public String getApplicationDateTemp() {
		return applicationDateTemp;
	}

	public void setApplicationDateTemp(String applicationDateTemp) {
		this.applicationDateTemp = applicationDateTemp;
		try {
			this.applicationDate = DateUtil.toDate("dd/MM/yyyy HH:mm:ss", this.applicationDateTemp);
		} catch (ParseException e) {
		}
	}

	@Transient
	public String getPaymentDateTemp() {
		return paymentDateTemp;
	}

	public void setPaymentDateTemp(String paymentDateTemp) {
		this.paymentDateTemp = paymentDateTemp;
		try {
			this.paymentDate = DateUtil.toDate("dd/MM/yyyy HH:mm:ss", this.paymentDateTemp);
		} catch (ParseException e) {
		}
	}

	@Transient
	public String getTotalClassTemp() {
		return totalClassTemp;
	}

	public void setTotalClassTemp(String totalClassTemp) {
		this.totalClassTemp = totalClassTemp;
		this.totalClass = NumberUtil.safeParseInteger(this.totalClassTemp, BigInteger.ZERO).intValue();
	}

	@Transient
	public String getTotalPaymentTemp() {
		return totalPaymentTemp;
	}

	public void setTotalPaymentTemp(String totalPaymentTemp) {
		this.totalPaymentTemp = totalPaymentTemp;
		this.totalPayment = NumberUtil.safeParseDecimal(this.totalPaymentTemp, BigDecimal.ZERO);
	}

	@Transient
	public TxTmOwner getTxTmOwner() {
		return txTmOwner;
	}

	public void setTxTmOwner(TxTmOwner txTmOwner) {
		this.txTmOwner = txTmOwner;
	}
	
	@Transient
	public List<MLookup> getLanguangeList() {
		return languangeList;
	}

	public void setLanguangeList(List<MLookup> languangeList) {
		this.languangeList = languangeList;
	}

	@Transient
	public MLookup getmLanguage() {
		return mLanguage;
	}

	public void setmLanguage(MLookup mLanguage) {
		this.mLanguage = mLanguage;
	}


	/***************************** - OTHER METHOD SECTION - ****************************/
	@Transient
	public String getQrText() {
//		nomor permohonan""tgl penerimaan""login""total_byr""kode_billing""tgl_byr
//		J002018000013**20/03/2018**Raysa**10000000**1234567890**21/02/2018
		String formattedPaymentDate = DateUtil.formatDate(getPaymentDate(), "yyyy-MM-dd HH:mm:ss");
		return getApplicationNo() + "***" + getApplicationDate() + "***" + getCreatedBy().getUsername() + "***" + getTotalPayment().toBigInteger().toString() + "***" + getBankCode() + "***" + formattedPaymentDate;
	}

	@Override
	public String toString() {
		return "TxReception [id=" + id + ", createdBy=" + createdBy + "]";
	}

	@Transient
	public String getLawTemp() {
		return lawTemp;
	}

	public void setLawTemp(String lawTemp) {
		this.lawTemp = lawTemp;
	}

	@Transient
	public String getApplicationNoNew() {
		return applicationNoNew;
	}

	public void setApplicationNoNew(String applicationNoNew) {
		this.applicationNoNew = applicationNoNew;
	}

	@Transient
	public String getCurrentId() {
		return this.id;
	}

	@Override
	public String logAuditTrail() {
		StringBuilder sb = new StringBuilder();
		sb.append("UUID: " + getCurrentId());
		if (getBankCode() != null) {
			sb.append("^ Kode Billing: " + getBankCode());
		}
		if (getApplicationDate() != null) {
			sb.append("^ Tanggal Pengajuan: " + getApplicationDate());
		}
		if (getApplicationNo() != null) {
			sb.append("^ Nomor Permohonan: " + getApplicationNo());
		}
		if (geteFilingNo() != null) {
			sb.append("^ Nomor Transaksi: " + geteFilingNo());
		}
		if (getPaymentDate() != null) {
			sb.append("^ Tanggal Pembayaran: " + getPaymentDate());
		}
		if (getTotalClass() != 0) {
			sb.append("^ Total Kelas: " + getTotalClass());
		}
		if (getTotalPayment() != null) {
			sb.append("^ Total Pembayaran: " + getTotalPayment());
		}
		if (getmFileType() != null) {
			sb.append("^ Tipe Permohonan: " + getmFileType().getDesc());
		}
		if (getmFileTypeDetail() != null) {
			sb.append("^ Jenis Permohonan: " + getmFileTypeDetail().getDesc());
		}
		if (getmWorkflow() != null) {
			sb.append("^ Nama Workflow: " + getmWorkflow().getName());
		}
		
		return sb.toString();
	}

}
