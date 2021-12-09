package com.docotel.ki.pojo;

import java.sql.Timestamp;

public class CetakPasca {
	private int flag;
	private String headerInd;
	private String headerEng;
	
	private String postNo;
    private String eFilingNo;
    private String note;
    private Timestamp postDate;
    private Timestamp paymentDate;
    
    private String refAppNo;
    private String refRegNo;
    private String refDetail;
    
    private String reprsName;
	private String reprsAddress;
	private String reprsEmailPhone;
	
	private String docName;
	
	public CetakPasca(int flag, String headerInd, String headerEng) {
		this.flag = flag;
		this.headerInd = headerInd;
		this.headerEng = headerEng;
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
	public void setPostNo(String postNo) {
		this.postNo = postNo;
	}
	public String getPostNo() {
		return postNo;
	}
	public void seteFilingNo(String eFilingNo) {
		this.eFilingNo = eFilingNo;
	}
	public String geteFilingNo() {
		return eFilingNo;
	}
    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }
	public Timestamp getPostDate() {
        return postDate;
    }
    public void setPostDate(Timestamp postDate) {
        this.postDate = postDate;
    }
    public Timestamp getPaymentDate() {
        return paymentDate;
    }
    public void setPaymentDate(Timestamp paymentDate) {
        this.paymentDate = paymentDate;
    }
    public String getRefAppNo() {
        return refAppNo;
    }
    public void setRefAppNo(String refAppNo) {
        this.refAppNo = refAppNo;
    }
    public String getRefRegNo() {
        return refRegNo;
    }
    public void setRefRegNo(String refRegNo) {
        this.refRegNo = refRegNo;
    }
    public String getRefDetail() {
        return refDetail;
    }
    public void setRefDetail(String refDetail) {
        this.refDetail = refDetail;
    }
    public void setReprsName(String reprsName) {
		this.reprsName = reprsName;
	}
	public String getReprsName() {
		return reprsName;
	}
	public void setReprsAddress(String reprsAddress) {
		this.reprsAddress = reprsAddress;
	}
	public String getReprsAddress() {
		return reprsAddress;
	}
	public void setReprsEmailPhone(String reprsEmailPhone) {
		this.reprsEmailPhone = reprsEmailPhone;
	}
	public String getReprsEmailPhone() {
		return reprsEmailPhone;
	}
	public void setDocName(String docName) {
		this.docName = docName;
	}
	public String getDocName() {
		return docName;
	}
}
