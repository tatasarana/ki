package com.docotel.ki.pojo;

public class MadridReprs {
	public final String ATTR_CLID ="CLID";
	public final String TAG_NAME = "/NAME/NAMEL";
	public final String TAG_ADDRESS = "/ADDRESS/ADDRL";
	public final String TAG_COUNTRY =  "/ADDRESS/COUNTRY";	
	
	private String repId;
	private String repCLID;
	private String repName;
	private String repAddress;
	private String repCountry;
	private boolean repExist;
	
	public String getRepId() {
		return repId;
	}
	public void setRepId(String repId) {
		this.repId = repId;
	}
	public String getRepCLID() {
		return repCLID;
	}
	public void setRepCLID(String repCLID) {
		this.repCLID = repCLID;
	}
	public String getRepName() {
		return repName;
	}
	public void setRepName(String repName) {
		this.repName = repName;
	}
	public String getRepAddress() {
		return repAddress;
	}
	public void setRepAddress(String repAddress) {
		this.repAddress = repAddress;
	}
	public String getRepCountry() {
		return repCountry;
	}
	public void setRepCountry(String repCountry) {
		this.repCountry = repCountry;
	}
	public boolean isRepExist() {
		return this.repExist;
	}
	public void setRepExist(boolean repExist) {
		this.repExist = repExist;
	}
}
