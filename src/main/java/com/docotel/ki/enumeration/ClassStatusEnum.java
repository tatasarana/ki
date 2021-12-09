package com.docotel.ki.enumeration;

public enum ClassStatusEnum {
	ACCEPT("Accepted"),
    REJECT("Rejected"),
    PENDING("Pending"),;

	private final String label;

	ClassStatusEnum(String label) {
        this.label = label;
    }
    
    public String getLabel() {
    	return this.label;
    }
}
