package com.docotel.ki.pojo;

import java.io.Serializable;
import java.sql.Timestamp;


public class DataOwner implements Serializable {
	/***************************** - FIELD SECTION - ****************************/	
	
	private String id; 
	private String ownerName;  
	private String ownerDetailName;
	private String ownerAddress;
	private String ownerPhone;
	private String ownerEmail;
	private String ownerCountry;
	private String ownerUpdatedBy;
	private boolean ownerStatus;
	
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getOwnerName() {
		return ownerName;
	}
	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}
	public String getOwnerDetailName() {
		return ownerDetailName;
	}
	public void setOwnerDetailName(String ownerDetailName) {
		this.ownerDetailName = ownerDetailName;
	}
	public String getOwnerAddress() {
		return ownerAddress;
	}
	public void setOwnerAddress(String ownerAddress) {
		this.ownerAddress = ownerAddress;
	}
	public String getOwnerPhone() {
		return ownerPhone;
	}
	public void setOwnerPhone(String ownerPhone) {
		this.ownerPhone = ownerPhone;
	}
	public String getOwnerEmail() {		return ownerEmail;	}
	public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail;	}
	public String getOwnerCountry() {
		return ownerCountry;
	}
	public void setOwnerCountry(String ownerCountry) {
		this.ownerCountry = ownerCountry;
	}
	public boolean isOwnerStatus() {
		return ownerStatus;
	}
	public void setOwnerStatus(boolean ownerStatus) {
		this.ownerStatus = ownerStatus;
	}
	public String getOwnerUpdatedBy() {
		return ownerUpdatedBy;
	}
	public void setOwnerUpdatedBy(String ownerUpdatedBy) {
		this.ownerUpdatedBy = ownerUpdatedBy;
	}
}
