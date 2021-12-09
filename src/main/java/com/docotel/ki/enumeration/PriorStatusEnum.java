package com.docotel.ki.enumeration;

public enum PriorStatusEnum {
	PENDING("Tunda"),
    ACCEPT("Terlampir"),
    REJECT("Tolak")    ;

	private final String label;

    PriorStatusEnum(String label) {
        this.label = label;
    }
    
    public String getLabel() {
    	return this.label;
    }
}
