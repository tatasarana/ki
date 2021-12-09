package com.docotel.ki.pojo;

import java.io.Serializable;
import java.sql.Timestamp;

public class TxTmGeneralOnline implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/***************************** - FIELD SECTION - ****************************/
	private String id;
	private String txReception;
	private String applicationNo;
	private String mLaw;
	private Timestamp filingDate;
	private String mStatus;
	private int totalClass;
	private boolean kanwilFlag;
	private boolean groupFlag;
	private boolean printFlag;
	private String createdBy;
	private Timestamp createdDate;
	private String lastUpdateStatus;
	private String lastUpdateBy;
	private Timestamp lastUpdateDate;
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTxReception() {
		return txReception;
	}
	public void setTxReception(String txReception) {
		this.txReception = txReception;
	}
	public String getApplicationNo() {
		return applicationNo;
	}
	public void setApplicationNo(String applicationNo) {
		this.applicationNo = applicationNo;
	}
	public String getmLaw() {
		return mLaw;
	}
	public void setmLaw(String mLaw) {
		this.mLaw = mLaw;
	}
	public Timestamp getFilingDate() {
		return filingDate;
	}
	public void setFilingDate(Timestamp filingDate) {
		this.filingDate = filingDate;
	}
	public String getmStatus() {
		return mStatus;
	}
	public void setmStatus(String mStatus) {
		this.mStatus = mStatus;
	}
	public int getTotalClass() {
		return totalClass;
	}
	public void setTotalClass(int totalClass) {
		this.totalClass = totalClass;
	}
	public boolean isKanwilFlag() {
		return kanwilFlag;
	}
	public void setKanwilFlag(boolean kanwilFlag) {
		this.kanwilFlag = kanwilFlag;
	}
	public boolean isGroupFlag() {
		return groupFlag;
	}
	public void setGroupFlag(boolean groupFlag) {
		this.groupFlag = groupFlag;
	}
	public boolean isPrintFlag() {
		return printFlag;
	}
	public void setPrintFlag(boolean printFlag) {
		this.printFlag = printFlag;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public Timestamp getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Timestamp createdDate) {
		this.createdDate = createdDate;
	}
	public String getLastUpdateStatus() {
		return lastUpdateStatus;
	}
	public void setLastUpdateStatus(String lastUpdateStatus) {
		this.lastUpdateStatus = lastUpdateStatus;
	}
	public String getLastUpdateBy() {
		return lastUpdateBy;
	}
	public void setLastUpdateBy(String lastUpdateBy) {
		this.lastUpdateBy = lastUpdateBy;
	}
	public Timestamp getLastUpdateDate() {
		return lastUpdateDate;
	}
	public void setLastUpdateDate(Timestamp lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}
	

}
