package com.docotel.ki.pojo;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.docotel.ki.model.master.MBrandType;


public class MadridDetailInfo implements Serializable {
	
	
	/***************************** - FIELD SECTION - ****************************/	
	private String originalLanguage;
	private String documentId;
	private String dateRecord;
	private String notificationDate;
	private String effectiveDate;
	private String officeOrigin;
	private String expiryDate;   
	private String internationalRegistrationNumber;
	private String internationalRegistrationDate;
	private String instrumentContractingPartyDesign;
	private List<MadridHolding> holderList;	
	private String prevHolderDetail;	
	private String correspondenceDetail;
	private List<MadridReprs> reprsList;
	private String vienna;
	private MBrandType brandType;
	private String brandTypeName;
	private String brandName;
	private String fileName;
	private String colorsClaimed;
	private String markinColorEn;
	private String markVoluntary;
	private String markDescription;
	private String markDescriptionChar;
	private String disclaimer;
	private String translation;
	private Map<String, String> basicList;	
	private Map<String, String> basicDetail;	 
	private List<MadridPrior> priorityDetail;	 
	private String protocolDesignation;
	private String limitation;
	private String VRBLNOT;
	private String VIECLA3;
	private String DESPG2;
	private Map<String, String> limtoList;
	
	
	
	public String getOriginalLanguage() {
		return originalLanguage;
	}
	public void setOriginalLanguage(String originalLanguage) {
		this.originalLanguage = originalLanguage;
	}
	
	public String getDocumentId() {
		return documentId;
	}
	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}
	
	public String getDateRecord() {
		return dateRecord;
	}
	public void setDateRecord(String dateRecord) {		
		try {
			Date d = new SimpleDateFormat("yyyyMMdd").parse(dateRecord);
			this.dateRecord = new SimpleDateFormat("dd/MM/yyyy").format(d);
		} catch (ParseException e) {			
			 
		}
	}
	
	public String getNotificationDate() {
		return notificationDate;
	}
	public void setNotificationDate(String notificationDate) {		
		try {
			Date d = new SimpleDateFormat("yyyyMMdd").parse(notificationDate);
			this.notificationDate = new SimpleDateFormat("dd/MM/yyyy").format(d);
		} catch (ParseException e) {			
			 
		}
	}
	
	public String getEffectiveDate() {
		return effectiveDate;
	}
	public void setEffectiveDate(String effectiveDate) {		
		try {
			Date d = new SimpleDateFormat("yyyyMMdd").parse(effectiveDate);
			this.effectiveDate = new SimpleDateFormat("dd/MM/yyyy").format(d);
		} catch (ParseException e) {			
			// 
		}
	}
	
	public String getOfficeOrigin() {
		return officeOrigin;
	}
	public void setOfficeOrigin(String officeOrigin) {
		this.officeOrigin = officeOrigin;
	}
	
	public String getExpiryDate() {
		return expiryDate;
	}
	public void setExpiryDate(String expiryDate) {	
		try {
			Date d = new SimpleDateFormat("yyyyMMdd").parse(expiryDate);
			this.expiryDate = new SimpleDateFormat("dd/MM/yyyy").format(d);
		} catch (ParseException e) {			
			 
		}
	}
	
	public String getInternationalRegistrationNumber() {
		return internationalRegistrationNumber;
	}
	public void setInternationalRegistrationNumber(String internationalRegistrationNumber) {
		this.internationalRegistrationNumber = internationalRegistrationNumber;
	}
	
	public String getInternationalRegistrationDate() {
		return internationalRegistrationDate;
	}
	public void setInternationalRegistrationDate(String internationalRegistrationDate) {
		//this.internationalRegistrationDate = internationalRegistrationDate;
		try {
			Date d = new SimpleDateFormat("yyyyMMdd").parse(internationalRegistrationDate);
			this.internationalRegistrationDate = new SimpleDateFormat("dd/MM/yyyy").format(d);
		} catch (ParseException e) {			
			 
		}
	}
	
	public String getInstrumentContractingPartyDesign() {
		return instrumentContractingPartyDesign;
	}
	public void setInstrumentContractingPartyDesign(String instrumentContractingPartyDesign) {
		this.instrumentContractingPartyDesign = instrumentContractingPartyDesign;
	}
	public List<MadridHolding> getHolderList() {
		return holderList;
	}
	public void setHolderList(List<MadridHolding> holderList) {
		this.holderList = holderList;
	}
	public String getPrevHolderDetail() {
		return prevHolderDetail;
	}
	public void setPrevHolderDetail(String prevHolderDetail) {
		this.prevHolderDetail = prevHolderDetail;
	}
	
	public String getCorrespondenceDetail() {
		return correspondenceDetail;
	}
	public void setCorrespondenceDetail(String correspondenceDetail) {
		this.correspondenceDetail = correspondenceDetail;
	}	
	public List<MadridReprs> getReprsList() {
		return reprsList;
	}
	public void setReprsList(List<MadridReprs> reprsList) {
		this.reprsList = reprsList;
	}
	public String getVienna() {
		return vienna;
	}	
	public void setVienna(String vienna) {
		this.vienna = vienna;
	}
	public MBrandType getBrandType() {
		return brandType;
	}
	public void setBrandType(MBrandType brandType) {
		this.brandType = brandType;
	}
	public String getBrandTypeName() {
		return brandTypeName;
	}
	public void setBrandTypeName(String brandTypeName) {
		this.brandTypeName = brandTypeName;
	}
	public String getBrandName() {
		return brandName;
	}
	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getColorsClaimed() {
		return colorsClaimed;
	}
	public void setColorsClaimed(String colorsClaimed) {
		this.colorsClaimed = colorsClaimed;
	}
	
	public String getMarkinColorEn() {
		return markinColorEn;
	}
	public void setMarkinColorEn(String markinColorEn) {
		this.markinColorEn = markinColorEn;
	}
	public String getMarkVoluntary() {
		return markVoluntary;
	}
	public void setMarkVoluntary(String markVoluntary) {
		this.markVoluntary = markVoluntary;
	}
	public String getMarkDescription() {
		return markDescription;
	}
	public void setMarkDescription(String markDescription) {
		this.markDescription = markDescription;
	}
	public String getMarkDescriptionChar() {
		return markDescriptionChar;
	}
	public void setMarkDescriptionChar(String markDescriptionChar) {
		this.markDescriptionChar = markDescriptionChar;
	}
	public String getDisclaimer() {
		return disclaimer;
	}
	public void setDisclaimer(String disclaimer) {
		this.disclaimer = disclaimer;
	}		
	public String getTranslation() {
		return translation;
	}
	public void setTranslation(String translation) {
		this.translation = translation;
	}
	public Map<String, String> getBasicList() {
		return basicList;
	}
	public void setBasicList(Map<String, String> basicList) {
		this.basicList = basicList;
	}
	
	public Map<String, String> getBasicDetail() {
		return basicDetail;
	}
	public void setBasicDetail(Map<String, String> basicDetail) {
		this.basicDetail = basicDetail;
	}
	 
	public List<MadridPrior> getPriorityDetail() {
		return priorityDetail;
	}
	public void setPriorityDetail(List<MadridPrior> priorityDetail) {
		this.priorityDetail = priorityDetail;
	}
	public String getProtocolDesignation() {
		return protocolDesignation;
	}
	public void setProtocolDesignation(String protocolDesignation) {
		this.protocolDesignation = protocolDesignation;
	}
	public String getLimitation() {
		return limitation;
	}
	public void setLimitation(String limitation) {
		this.limitation = limitation;
	}
	public String getVRBLNOT() {
		return VRBLNOT;
	}
	public void setVRBLNOT(String vRBLNOT) {
		VRBLNOT = vRBLNOT;
	}
	public String getVIECLA3() {
		return VIECLA3;
	}
	public void setVIECLA3(String vIECLA3) {
		VIECLA3 = vIECLA3;
	}
	public String getDESPG2() {
		return DESPG2;
	}
	public void setDESPG2(String dESPG2) {
		DESPG2 = dESPG2;
	}
	public Map<String, String> getLimtoList() {
		return limtoList;
	}
	public void setLimtoList(Map<String, String> limtoList) {
		this.limtoList = limtoList;
	}

	
}
