package com.docotel.ki.enumeration;

import com.docotel.ki.model.master.MUser;

public enum UserEnum {
	SYSTEM,
	;

	public MUser value() {
		return new MUser(name());
	}
}
