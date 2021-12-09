package com.docotel.ki.enumeration;

import com.docotel.ki.model.master.MDocType;

public enum DocTypeEnumId {
		EDMS,
		;
		
	public MDocType value() {
		return new MDocType(name());
	}
}
