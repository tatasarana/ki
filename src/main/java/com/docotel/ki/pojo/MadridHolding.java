package com.docotel.ki.pojo;

public class MadridHolding {
	public final String ATTR_CLID ="CLID";
	public final String TAG_NAME = "/NAME/NAMEL";
	public final String TAG_ADDRESS = "/ADDRESS/ADDRL";
	public final String TAG_COUNTRY =  "/ADDRESS/COUNTRY";
	public final String TAG_ENTITLEMENT = "/ENTEST";
	public final String TAG_ENTITLEMENT_ADDRESS = "/ENTADDR/ENTADDR";
	public final String TAG_ENTITLEMENT_COUNTRY = "/ENTADDR/COUNTRY";
	public final String TAG_LEGAL_NATURE = "/LEGNATU/LEGNATT";
	public final String TAG_PLACE_INCORPORATED = "/LEGNATU/PLAINCO";
	public final String TAG_CORRESPONDENCE_ADDRESS = "/CORRIND";
	
	
	private String holdCLID;
	private String holdName;
	private String holdAddress;
	private String holdCountry;
	private String holdEntitlement;
	private String holdEntitlementAddress;
	private String holdEntitlementCountry;
	private String holdLegalNature;
	private String holdPlaceIncorporated;
	private String holdCorrsAddress;
	
	
	
	public String getHoldCLID() {
		return holdCLID;
	}
	public void setHoldCLID(String holdCLID) {
		this.holdCLID = holdCLID;
	}
	public String getHoldName() {
		return holdName;
	}
	public void setHoldName(String holdName) {
		this.holdName = holdName;
	}
	public String getHoldAddress() {
		return holdAddress;
	}
	public void setHoldAddress(String holdAddress) {
		this.holdAddress = holdAddress;
	}
	public String getHoldCountry() {
		return holdCountry;
	}
	public void setHoldCountry(String holdCountry) {
		this.holdCountry = holdCountry;
	}
	public String getHoldEntitlement() {
		return holdEntitlement;
	}
	public void setHoldEntitlement(String holdEntitlement) {
		this.holdEntitlement = holdEntitlement;
	}
	public String getHoldEntitlementAddress() {
		return holdEntitlementAddress;
	}
	public void setHoldEntitlementAddress(String holdEntitlementAddress) {
		this.holdEntitlementAddress = holdEntitlementAddress;
	}
	
	
	public String getHoldEntitlementCountry() {
		return holdEntitlementCountry;
	}
	public void setHoldEntitlementCountry(String holdEntitlementCountry) {
		this.holdEntitlementCountry = holdEntitlementCountry;
	}
	public String getHoldLegalNature() {
		return holdLegalNature;
	}
	public void setHoldLegalNature(String holdLegalNature) {
		this.holdLegalNature = holdLegalNature;
	}
	public String getHoldPlaceIncorporated() {
		return holdPlaceIncorporated;
	}
	public void setHoldPlaceIncorporated(String holdPlaceIncorporated) {
		this.holdPlaceIncorporated = holdPlaceIncorporated;
	}
	public String getHoldCorrsAddress() {
		return holdCorrsAddress;
	}
	public void setHoldCorrsAddress(String holdCorrsAddress) {
		this.holdCorrsAddress = holdCorrsAddress;
	}
	
	

}
