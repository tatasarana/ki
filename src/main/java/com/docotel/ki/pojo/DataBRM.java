package com.docotel.ki.pojo;

import java.io.Serializable;
import java.sql.Timestamp;


public class DataBRM implements Serializable {
	/***************************** - FIELD SECTION - ****************************/
	private Integer no;
	private String appNo; //nomor permohonan
	private Timestamp fillingDate; //filling date/ tgl penerimaan
	private String classNo; //kelas
	private String classDesc; //kelas deskripsi
	private String brandName; //merek
	private String prior; //prioritas
	private String ownerName; //Nama Pemilik
	private String ownerAddress; //Alamat Pemilik
	
	private String reprsName; //Nama Kuasa
	private String reprsAddress; //Alamat Kuasa	
	private String brandTypeName;
	private String brandTranslation; //Arti Bahasa/huruf
	private String brandColor; //uraian warna
	private String brandDescripion; //uraian barang/jasa
	private String brandLogo; //logo merek
	private Timestamp priorDate;
	private String priorcountry;
	private String classDescEn; //kelas deskripsi English
	private String irn; //irn madrid


	public Integer getNo() {
		return no;
	}

	public void setNo(Integer no) {
		this.no = no;
	}

	public String getPrior() {
		return prior;
	}

	public void setPrior(String prior) {
		this.prior = prior;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public String getOwnerAddress() {
		return ownerAddress;
	}

	public void setOwnerAddress(String ownerAddress) {
		this.ownerAddress = ownerAddress;
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
	 
	public String getBrandTranslation() {
		return brandTranslation;
	}

	public void setBrandTranslation(String brandTranslation) {
		this.brandTranslation = brandTranslation;
	}

	public String getBrandColor() {
		return brandColor;
	}

	public void setBrandColor(String brandColor) {
		this.brandColor = brandColor;
	}

	public String getBrandDescripion() {
		return brandDescripion;
	}

	public void setBrandDescripion(String brandDescripion) {
		this.brandDescripion = brandDescripion;
	}

	public String getAppNo() {
		return appNo;
	}
	 
	public void setAppNo(String appNo) {
		this.appNo = appNo;
	}
	public Timestamp getFillingDate() {
		return fillingDate;
	}
	public void setFillingDate(Timestamp fillingDate) {
		this.fillingDate = fillingDate;
	}
	public String getClassNo() {
		return classNo;
	}
	public void setClassNo(String classNo) {
		this.classNo = classNo;
	}
	public String getClassDesc() {
		return classDesc;
	}
	public void setClassDesc(String classDesc) {
		this.classDesc = classDesc;
	}
	public String getBrandName() {
		return brandName;
	}
	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}

	public String getBrandTypeName() {
		return brandTypeName;
	}

	public void setBrandTypeName(String brandTypeName) {
		this.brandTypeName = brandTypeName;
	}
	
	public String getBrandLogo() {
		return brandLogo;
	}

	public void setBrandLogo(String brandLogo) {
		this.brandLogo = brandLogo;
	}

	public Timestamp getPriorDate() {
		return priorDate;
	}

	public void setPriorDate(Timestamp priorDate) {
		this.priorDate = priorDate;
	}

	public String getPriorcountry() {
		return priorcountry;
	}

	public void setPriorcountry(String priorcountry) {
		this.priorcountry = priorcountry;
	}

	public String getClassDescEn() {
		return classDescEn;
	}

	public void setClassDescEn(String classDescEn) {
		this.classDescEn = classDescEn;
	}

	public String getIrn() {
		return irn;
	}

	public void setIrn(String irn) {
		this.irn = irn;
	}

	/***************************** - TRANSIENT FIELD SECTION - ****************************/
	
	/*@Transient
	public String getNomorTemp() {
		return NomorTemp;
	}

	public void setNomorTemp(String NomorTemp) {
		this.NomorTemp = NomorTemp;
		this.Nomor = NumberUtil.safeParseInteger(this.NomorTemp, BigInteger.ZERO).intValue();
	}*/

	/***************************** - CONSTRUCTOR SECTION - ****************************/

	/***************************** - GETTER SETTER METHOD SECTION - ****************************/
	
}
