package com.docotel.ki.pojo;

public class KeyValue  {
	private String key;
	private Object value;
	private boolean exactMatch = true;

	public KeyValue() {
	}

	public KeyValue(String key, Object value, boolean exactMatch) {
		this.key = key;
		this.value = value;
		this.exactMatch = exactMatch;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public boolean isExactMatch() {
		return exactMatch;
	}

	public void setExactMatch(boolean exactMatch) {
		this.exactMatch = exactMatch;
	}
}
