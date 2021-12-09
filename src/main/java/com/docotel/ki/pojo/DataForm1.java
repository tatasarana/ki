package com.docotel.ki.pojo;

import java.math.BigDecimal;

import com.docotel.ki.model.master.*;

public class DataForm1 {
    private String appid;
    private String law;
    private String workflow; 
    private String tgl;
    private MStatus status;
    private MAction action;
    private String tipePermohonan;
    
    private String bankCode;
    private String applicationDate;
    private String paymentDate;
    private int totalClass;
    private BigDecimal totalPayment;
    private MFileSequence mFileSequence;
	private MFileType mFileType;
	private MFileTypeDetail mFileTypeDetail;
	private String mLanguage;
	private String[] receptionIds;
	private String applicantNotes;
	private String notesIpas;


	public MStatus getStatus() {
		return status;
	}

	public void setStatus(MStatus status) {
		this.status = status;
	}

	public MAction getAction() {
		return action;
	}

	public void setAction(MAction action) {
		this.action = action;
	}

	public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getLaw() {
        return law;
    }

    public void setLaw(String law) {
        this.law = law;
    }

    public String getWorkflow() {
		return workflow;
	}

	public void setWorkflow(String workflow) {
		this.workflow = workflow;
	}

	public String getTgl() {
        return tgl;
    }

    public void setTgl(String tgl) {
        this.tgl = tgl;
    }

    public String getTipePermohonan() {
        return tipePermohonan;
    }

    public void setTipePermohonan(String tipePermohonan) {
        this.tipePermohonan = tipePermohonan;
    }
    
    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }
    
    public String getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(String applicationDate) {
        this.applicationDate = applicationDate;
    }
    
    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }
    
    public int getTotalClass() {
		return totalClass;
	}

	public void setTotalClass(int totalClass) {
		this.totalClass = totalClass;
	}
	
    public BigDecimal getTotalPayment() {
		return totalPayment;
	}

	public void setTotalPayment(BigDecimal totalPayment) {
		this.totalPayment = totalPayment;
	}
	
	public MFileSequence getmFileSequence() {
		return mFileSequence;
	}

	public void setmFileSequence(MFileSequence mFileSequence) {
		this.mFileSequence = mFileSequence;
	}

	public MFileType getmFileType() {
		return mFileType;
	}

	public void setmFileType(MFileType mFileType) {
		this.mFileType = mFileType;
	}

	public MFileTypeDetail getmFileTypeDetail() {
		return mFileTypeDetail;
	}

	public void setmFileTypeDetail(MFileTypeDetail mFileTypeDetail) {
		this.mFileTypeDetail = mFileTypeDetail;
	}

	public String getmLanguage() {
		return mLanguage;
	}

	public void setmLanguage(String mLanguage) {
		this.mLanguage = mLanguage;
	}

	public String[] getReceptionIds() {
		return receptionIds;
	}

	public void setReceptionIds(String[] receptionIds) {
		this.receptionIds = receptionIds;
	}

	public String getApplicantNotes() {
		return applicantNotes;
	}

	public void setApplicantNotes(String applicantNotes) {
		this.applicantNotes = applicantNotes;
	}

	public String getNotesIpas() {
		return notesIpas;
	}

	public void setNotesIpas(String notesIpas) {
		this.notesIpas = notesIpas;
	}
}
