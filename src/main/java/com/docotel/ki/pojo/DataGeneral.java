package com.docotel.ki.pojo;

import java.io.Serializable;
import java.sql.Timestamp;


public class DataGeneral implements Serializable {
	/***************************** - FIELD SECTION - ****************************/
	
	
	private Integer no; 
	private String applicationNo; //nomor permohonan
	private String fillingDate; //filling date/ tgl penerimaan
	private String brandType; //tipe merek
	private String brandName; //nama merek
	private String kelas; //kelas		
	private String priorName; //prioritas
	private String status; //status
	private String aksi; //status
	private String applicationId;
	private String mFileType; // tipe permohonan ( txTmGeneral.txReception.mFileType.id )
	private String mFileTypeDetail; // tipe permohonan ( txTmGeneral.txReception.mFileTypeDetail.id )
	private String username; // username ( txTmGeneral.createdBy.username )
	private String regNo; // ( txTmGeneral.createdBy.username )
	private String reprsName;
	private String bankCode;
	private String applicationDate; // txReception.application_date

	public Integer getNo() {
		return no;
	}
	public void setNo(Integer no) { this.no = no; }
	public String getApplicationNo() {
		return applicationNo;
	}
	public void setApplicationNo(String applicationNo) {
		this.applicationNo = applicationNo;
	}
	public String getFillingDate() {
		return fillingDate;
	}
	public void setFillingDate(String fillingDate) {
		this.fillingDate = fillingDate;
	}
	public String getBrandType() {
		return brandType;
	}
	public void setBrandType(String brandType) {
		this.brandType = brandType;
	}
	public String getBrandName() {
		return brandName;
	}
	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}
	public String getKelas() {
		return kelas;
	}
	public void setKelas(String kelas) {
		this.kelas = kelas;
	}
	public String getPriorName() {
		return priorName;
	}
	public void setPriorName(String priorName) {
		this.priorName = priorName;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getApplicationId() {
		return applicationId;
	}
	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}
	public String getmFileTypeDetail() {
		return mFileTypeDetail;
	}
	public void setmFileTypeDetail(String mFileTypeDetail) {
		this.mFileTypeDetail = mFileTypeDetail;
	}
	public String getmFileType() { return mFileType; }
	public void setmFileType(String mFileType) { this.mFileType = mFileType; }
	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }

	public String getRegNo() {
		return regNo;
	}

	public void setRegNo(String regNo) {
		this.regNo = regNo;
	}

	public String getReprsName() {
		return reprsName;
	}

	public void setReprsName(String reprsName) {
		this.reprsName = reprsName;
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

	public String getAksi() {
		return aksi;
	}

	public void setAksi(String aksi) {
		this.aksi = aksi;
	}
}
