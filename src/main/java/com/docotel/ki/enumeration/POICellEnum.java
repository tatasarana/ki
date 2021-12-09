package com.docotel.ki.enumeration;

public enum POICellEnum {
	COLUMN_A(0),
	COLUMN_B(1),
	COLUMN_C(2),
	COLUMN_D(3),
	COLUMN_E(4),
	COLUMN_F(5),
	COLUMN_G(6),
	COLUMN_H(7),
	COLUMN_I(8),
	COLUMN_J(9),
	COLUMN_K(10),
	;

	private int value;

	POICellEnum(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
