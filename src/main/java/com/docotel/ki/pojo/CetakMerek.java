package com.docotel.ki.pojo;

import java.io.Serializable;
import java.sql.Timestamp;

public class CetakMerek implements Serializable {
	private int flag;
	private Integer no;
	private Integer noBrand;
	private String headerInd;
	private String headerEng;
	
	private String applicationNo;
	private String eFilingNo;
	private String fileSequence;
	private String fileType;
	private String fileTypeDetail;
	private Timestamp applicationDate;
	private Timestamp fillingDate;
	private String brandLogo;
	
	private String brandName;
	private String brandType;
	private String brandColor;
	private String brandDescription;
	private String brandTranslation;
	private String brandDescChar;
	private String brandDisclaimer;
	private String brandFilename;
	
	private String ownerName;
	private String ownerDetailName;
	private String ownerType;
	private String ownerNationality;
	private String ownerAddress;
	private String ownerCity;
	private String ownerProvince;
	private String ownerCountry;
	private String ownerZipCode;
	private String ownerPhone;
	private String ownerEmail;
	private String ownerPostAddress;
	private String ownerPostCity;
	private String ownerPostProvince;
	private String ownerPostCountry;
	private String ownerPostZipCode;
	private String ownerPostPhone;
	private String ownerPostEmail;
	
	private String reprsNo;
	private String reprsName;
	private String reprsOffice;
	private String reprsAddress;
	private String reprsPhone;
	private String reprsEmail;
	
	private String priorOrder;
	private String priorNo;
	private String priorCountry;
	private Timestamp priorDate;
	
	private String classNo;
	private String classDesc;
	private String classDescEn;
	
	private String docName;
	
	public CetakMerek(int flag, String headerInd, String headerEng) {
		this.flag = flag;
		this.headerInd = headerInd;
		this.headerEng = headerEng;
	}
	
	public Integer getNo() {
		return no;
	}
	public void setNo(Integer no) {
		this.no = no;
	}
	public Integer getNoBrand() {
		return noBrand;
	}
	public void setNoBrand(Integer noBrand) {
		this.noBrand = noBrand;
	}
	public void setFlag(int flag) {
		this.flag = flag;
	}
	public int getFlag() {
		return flag;
	}
	public void setHeaderInd(String headerInd) {
		this.headerInd = headerInd;
	}
	public String getHeaderInd() {
		return headerInd;
	}
	public void setHeaderEng(String headerEng) {
		this.headerEng = headerEng;
	}
	public String getHeaderEng() {
		return headerEng;
	}
	public void setApplicationNo(String applicationNo) {
		this.applicationNo = applicationNo;
	}
	public String getApplicationNo() {
		return applicationNo;
	}
	public void seteFilingNo(String eFilingNo) {
		this.eFilingNo = eFilingNo;
	}
	public String geteFilingNo() {
		return eFilingNo;
	}
	public void setFileSequence(String fileSequence) {
		this.fileSequence = fileSequence;
	}
	public String getFileSequence() {
		return fileSequence;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileTypeDetail(String fileTypeDetail) {
		this.fileTypeDetail =fileTypeDetail;
	}
	public String getFileTypeDetail() {
		return fileTypeDetail;
	}
	public Timestamp getApplicationDate() {
		return applicationDate;
	}
	public void setApplicationDate(Timestamp applicationDate) {
		this.applicationDate = applicationDate;
	}
	public Timestamp getFillingDate() {
		return fillingDate;
	}
	public void setFillingDate(Timestamp fillingDate) {
		this.fillingDate = fillingDate;
	}
	public String getBrandLogo() {
		return brandLogo;
	}

	public void setBrandLogo(String brandLogo) {
		this.brandLogo = brandLogo;
	}

	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}
	public String getBrandName() {
		return brandName;
	}
	public void setBrandType(String brandType) {
		this.brandType = brandType;
	}
	public String getBrandType() {
		return brandType;
	}
	public void setBrandColor(String brandColor) {
		this.brandColor = brandColor;
	}
	public String getBrandColor() {
		return brandColor;
	}
	public void setBrandDescription(String brandDescription) {
		this.brandDescription = brandDescription;
	}
	public String getBrandDescription() {
		return brandDescription;
	}
	public void setBrandTranslation(String brandTranslation) {
		this.brandTranslation = brandTranslation;
	}
	public String getBrandTranslation() {
		return brandTranslation;
	}
	public void setBrandDescChar(String brandDescChar) {
		this.brandDescChar = brandDescChar;
	}
	public String getBrandDescChar() {
		return brandDescChar;
	}
	public void setBrandDisclaimer(String brandDisclaimer) {
		this.brandDisclaimer = brandDisclaimer;
	}
	public String getBrandDisclaimer() {
		return brandDisclaimer;
	}
	public void setBrandFilename(String brandFilename) {
		this.brandFilename = brandFilename;
	}
	public String getBrandFilename() {
		return brandFilename;
	}
	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}
	public String getOwnerName() {
		return ownerName;
	}
	public String getOwnerDetailName() {
		return ownerDetailName;
	}
	public void setOwnerDetailName(String ownerDetailName) {
		this.ownerDetailName = ownerDetailName;
	}
	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}
	public String getOwnerType() {
		return ownerType;
	}
	public void setOwnerNationality(String ownerNationality) {
		this.ownerNationality = ownerNationality;
	}
	public String getOwnerNationality() {
		return ownerNationality;
	}
	public void setOwnerAddress(String ownerAddress) {
		this.ownerAddress = ownerAddress;
	}
	public String getOwnerAddress() {
		return ownerAddress;
	}
	public void setOwnerCity(String ownerCity) {
		this.ownerCity = ownerCity;
	}
	public String getOwnerCity() {
		return ownerCity;
	}
	public void setOwnerProvince(String ownerProvince) {
		this.ownerProvince = ownerProvince;
	}
	public String getOwnerProvince() {
		return ownerProvince;
	}
	public void setOwnerCountry(String ownerCountry) {
		this.ownerCountry = ownerCountry;
	}
	public String getOwnerCountry() {
		return ownerCountry;
	}
	public void setOwnerZipCode(String ownerZipCode) {
		this.ownerZipCode = ownerZipCode;
	}
	public String getOwnerZipCode() {
		return ownerZipCode;
	}
	public void setOwnerPhone(String ownerPhone) {
		this.ownerPhone = ownerPhone;
	}
	public String getOwnerPhone() {
		return ownerPhone;
	}
	public void setOwnerEmail(String ownerEmail) {
		this.ownerEmail = ownerEmail;
	}
	public String getOwnerEmail() {
		return ownerEmail;
	}
	public void setOwnerPostAddress(String ownerPostAddress) {
		this.ownerPostAddress = ownerPostAddress;
	}
	public String getOwnerPostAddress() {
		return ownerPostAddress;
	}
	public void setOwnerPostCity(String ownerPostCity) {
		this.ownerPostCity = ownerPostCity;
	}
	public String getOwnerPostCity() {
		return ownerPostCity;
	}
	public void setOwnerPostProvince(String ownerPostProvince) {
		this.ownerPostProvince = ownerPostProvince;
	}
	public String getOwnerPostProvince() {
		return ownerPostProvince;
	}
	public void setOwnerPostCountry(String ownerPostCountry) {
		this.ownerPostCountry = ownerPostCountry;
	}
	public String getOwnerPostCountry() {
		return ownerPostCountry;
	}
	public void setOwnerPostZipCode(String ownerPostZipCode) {
		this.ownerPostZipCode = ownerPostZipCode;
	}
	public String getOwnerPostZipCode() {
		return ownerPostZipCode;
	}
	public void setOwnerPostPhone(String ownerPostPhone) {
		this.ownerPostPhone = ownerPostPhone;
	}
	public String getOwnerPostPhone() {
		return ownerPostPhone;
	}
	public void setOwnerPostEmail(String ownerPostEmail) {
		this.ownerPostEmail = ownerPostEmail;
	}
	public String getOwnerPostEmail() {
		return ownerPostEmail;
	}
	public void setReprsNo(String reprsNo) {
		this.reprsNo = reprsNo;
	}
	public String getReprsNo() {
		return reprsNo;
	}
	public void setReprsName(String reprsName) {
		this.reprsName = reprsName;
	}
	public String getReprsName() {
		return reprsName;
	}
	public void setReprsOffice(String reprsOffice) {
		this.reprsOffice = reprsOffice;
	}
	public String getReprsOffice() {
		return reprsOffice;
	}
	public void setReprsAddress(String reprsAddress) {
		this.reprsAddress = reprsAddress;
	}
	public String getReprsAddress() {
		return reprsAddress;
	}
	public void setReprsPhone(String reprsPhone) {
		this.reprsPhone = reprsPhone;
	}
	public String getReprsPhone() {
		return reprsPhone;
	}
	public void setReprsEmail(String reprsEmail) {
		this.reprsEmail = reprsEmail;
	}
	public String getReprsEmail() {
		return reprsEmail;
	}
	public void setPriorOrder(String priorOrder) {
		this.priorOrder = priorOrder;
	}
	public String getPriorOrder() {
		return priorOrder;
	}
	public void setPriorNo(String priorNo) {
		this.priorNo = priorNo;
	}
	public String getPriorNo() {
		return priorNo;
	}
	public void setPriorCountry(String priorCountry) {
		this.priorCountry = priorCountry;
	}
	public String getPriorCountry() {
		return priorCountry;
	}
	public void setPriorDate(Timestamp priorDate) {
		this.priorDate = priorDate;
	}
	public Timestamp getPriorDate() {
		return priorDate;
	}
	public void setClassNo(String classNo) {
		this.classNo = classNo;
	}
	public String getClassNo() {
		return classNo;
	}
	public void setClassDesc(String classDesc) {
		this.classDesc = classDesc;
	}
	public String getClassDesc() {
		return classDesc;
	}
	public void setClassDescEn(String classDescEn) {
		this.classDescEn = classDescEn;
	}
	public String getClassDescEn() {
		return classDescEn;
	}
	public void setDocName(String docName) {
		this.docName = docName;
	}
	public String getDocName() {
		return docName;
	}
}
