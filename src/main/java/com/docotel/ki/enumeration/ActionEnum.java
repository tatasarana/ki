package com.docotel.ki.enumeration;

import com.docotel.ki.model.master.MAction;

public enum ActionEnum {
	TM_PUBLIKASI,
	;

	public MAction value() {
		return new MAction(name());
	}
}
