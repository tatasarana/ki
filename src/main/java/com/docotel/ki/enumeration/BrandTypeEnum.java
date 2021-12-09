package com.docotel.ki.enumeration;

import com.docotel.ki.model.master.MBrandType;

public enum BrandTypeEnum {
	MEREK_KATA,
	MEREK_KATA_DAN_LUKISAN,
	MEREK_LUKISAN,
	MEREK_TIGA_DIMENSI,
	MEREK_HOLOGRAM,
	MEREK_SUARA;
	public MBrandType value() {
		return new MBrandType(name());
	}
}
