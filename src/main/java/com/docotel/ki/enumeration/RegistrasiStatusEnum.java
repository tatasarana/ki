package com.docotel.ki.enumeration;

public enum RegistrasiStatusEnum {
	PREPARE ("Menunggu"),
	APPROVE ("Aktif"),
    REJECT("Ditolak"),
    EXPIRE("Kadarluarsa");

	private final String label;

	RegistrasiStatusEnum(String label) {
        this.label = label;
    }
    
    public String getLabel() {
    	return this.label;
    }
}
