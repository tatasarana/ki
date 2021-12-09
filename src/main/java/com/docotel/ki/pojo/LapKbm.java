package com.docotel.ki.pojo;

import oracle.jdbc.driver.DatabaseError;

import java.util.Date;

public class LapKbm {

	private Integer no;
	private String applicationNo;
	private String fillingDate;
	private String kelas;
	private String brandName;
	private String brandType;
	private String brmNo;
	private String status;
	private String aksi;
	private String tglPermohonanBanding;
	private String noDokPermohonanBanding;
	private String tmOwnerName;
	private String tmOwnerAddress;
	private String reprsName;
	private String reprsAddress;
	private String notes;
	private String tglPenerimaanBanding;
	private Date currentDate;


	
	public Integer getNo() {
		return no;
	}
	public void setNo(Integer no) {
		this.no = no;
	}
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
	public String getKelas() {
		return kelas;
	}
	public void setKelas(String kelas) {
		this.kelas = kelas;
	}
	public String getBrandName() {
		return brandName;
	}
	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}
	public String getBrandType() {
		return brandType;
	}
	public void setBrandType(String brandType) {
		this.brandType = brandType;
	}
	public String getBrmNo() {
		return brmNo;
	}
	public void setBrmNo(String brmNo) {
		this.brmNo = brmNo;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getAksi() {
		return aksi;
	}
	public void setAksi(String aksi) {
		this.aksi = aksi;
	}
	public String getTglPermohonanBanding() {
		return tglPermohonanBanding;
	}

	public void setTglPermohonanBanding(String tglPermohonanBanding) {
		this.tglPermohonanBanding = tglPermohonanBanding;
	}

	public String getTmOwnerName() {
		return tmOwnerName;
	}

	public void setTmOwnerName(String tmOwnerName) {
		this.tmOwnerName = tmOwnerName;
	}

	public String getTmOwnerAddress() {
		return tmOwnerAddress;
	}

	public void setTmOwnerAddress(String tmOwnerAddress) {
		this.tmOwnerAddress = tmOwnerAddress;
	}

	public String getReprsName() {
		return reprsName;
	}

	public void setReprsName(String reprsName) {
		this.reprsName = reprsName;
	}

	public String getReprsAddress() {
		return reprsAddress;
	}

	public void setReprsAddress(String reprsAddress) {
		this.reprsAddress = reprsAddress;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getTglPenerimaanBanding() {
		return tglPenerimaanBanding;
	}

	public void setTglPenerimaanBanding(String tglPenerimaanBanding) {
		this.tglPenerimaanBanding = tglPenerimaanBanding;
	}

	public Date getCurrentDate() {
		return currentDate;
	}

	public void setCurrentDate(Date currentDate) {
		this.currentDate = currentDate;
	}

	public String getNoDokPermohonanBanding() {
		return noDokPermohonanBanding;
	}

	public void setNoDokPermohonanBanding(String noDokPermohonanBanding) {
		this.noDokPermohonanBanding = noDokPermohonanBanding;
	}
}
