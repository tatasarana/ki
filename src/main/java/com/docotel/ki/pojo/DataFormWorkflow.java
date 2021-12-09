package com.docotel.ki.pojo;


import com.docotel.ki.model.master.MLaw;

public class DataFormWorkflow {
    private String id;
    private String code;
    private String name;
    private MLaw mLaw;

    
    
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public MLaw getmLaw() {
		return mLaw;
	}
	public void setmLaw(MLaw mLaw) {
		this.mLaw = mLaw;
	}
    
}
