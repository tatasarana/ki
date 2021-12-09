package com.docotel.ki.enumeration;

public enum GroupDetailStatusEnum {
	PREPARE ("Prepare"),
	RELEASE ("Release"),
    P1("P1"),
    P2("P2"),
    DONE("Selesai");

	private final String label;

	GroupDetailStatusEnum(String label) {
        this.label = label;
    }
    
    public String getLabel() {
    	return this.label;
    }
}
