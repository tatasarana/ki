package com.docotel.ki.pojo;

import java.io.Serializable;
import java.sql.Timestamp;


public class CetakEkspedisi implements Serializable {
	/***************************** - FIELD SECTION - ****************************/	
	private String noPermohonan; //prioritas
	private String tanggalPenerimaan; //tanggal penerimaan
	private String kelas; //kelas
	private String merek; //merek
	
	
	public String getNoPermohonan() {
		return noPermohonan;
	}
	public void setNoPermohonan(String noPermohonan) {
		this.noPermohonan = noPermohonan;
	}
	public String getTanggalPenerimaan() {
		return tanggalPenerimaan;
	}
	public void setTanggalPenerimaan(String tanggalPenerimaan) {
		this.tanggalPenerimaan = tanggalPenerimaan;
	}
	public String getKelas() {
		return kelas;
	}
	public void setKelas(String kelas) {
		this.kelas = kelas;
	}
	public String getMerek() {
		return merek;
	}
	public void setMerek(String merek) {
		this.merek = merek;
	}
	
	
	
	
}
