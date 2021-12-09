package com.docotel.ki.pojo;

import java.io.Serializable;
import java.sql.Timestamp;

public class PostReception implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/***************************** - FIELD SECTION - ****************************/
	private String codeBilling;
	private String fileseq;	
	private String mFileType;
	private String mFileTypeDetail;
	private String noPermohonan;
	private String noPendaftaran;
	private String merek;
	private String applicationDate;
	private String paymentDate;
	private String mFileTypeCode;
	private String TotalPayment;
	
	public String getCodeBilling() {
		return codeBilling;
	}
	public void setCodeBilling(String codeBilling) {
		this.codeBilling = codeBilling;
	}
	public String getFileseq() {
		return fileseq;
	}
	public void setFileseq(String fileseq) {
		this.fileseq = fileseq;
	}
	public String getmFileType() {
		return mFileType;
	}
	public void setmFileType(String mFileType) {
		this.mFileType = mFileType;
	}
	public String getmFileTypeDetail() {
		return mFileTypeDetail;
	}
	public void setmFileTypeDetail(String mFileTypeDetail) {
		this.mFileTypeDetail = mFileTypeDetail;
	}
	public String getNoPermohonan() {
		return noPermohonan;
	}
	public void setNoPermohonan(String noPermohonan) {
		this.noPermohonan = noPermohonan;
	}
	public String getNoPendaftaran() {
		return noPendaftaran;
	}
	public void setNoPendaftaran(String noPendaftaran) {
		this.noPendaftaran = noPendaftaran;
	}
	public String getMerek() {
		return merek;
	}
	public void setMerek(String merek) {
		this.merek = merek;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
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
	public String getmFileTypeCode() {
		return mFileTypeCode;
	}
	public void setmFileTypeCode(String mFileTypeCode) {
		this.mFileTypeCode = mFileTypeCode;
	}
	public String getTotalPayment() {
		return TotalPayment;
	}
	public void setTotalPayment(String totalPayment) {
		TotalPayment = totalPayment;
	}
	 
}
