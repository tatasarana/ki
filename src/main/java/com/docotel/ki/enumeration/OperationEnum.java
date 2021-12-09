package com.docotel.ki.enumeration;

import com.docotel.ki.model.master.MOperation;

public enum OperationEnum {
	SYSTEM,
	USER;

	public MOperation value() {
		return new MOperation(name());
	}
}
