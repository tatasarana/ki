package com.docotel.ki.pojo;

import java.io.Serializable;

public class KeyValueSelect implements Serializable
{
	private String key;
	private Object value;
	private String type = "AND"; // AND , OR
	private String eq = "=" ; // = , > , < , IS NOT,
	private Boolean exactMatch = true;

	public KeyValueSelect() {
	}

	public KeyValueSelect(String key, Object value,String eq, boolean exactMatch,String type)
	{
		this.type = type ;
		this.key = key;
		this.value = value;
		this.eq = eq;
		this.exactMatch = exactMatch;
	}

	public String getType()
	{
		if (type == null)
			return "AND";
		else
			return type ;
	}

	public void setType(String type){
		this.type = type ;
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

	public String getEq() {
		if (eq == null)
			return "=";
		else
			return eq ;

	}

	public void setEq(String eq) {
		this.eq = eq;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Boolean isExactMatch() {
		return exactMatch;
	}

	public void setExactMatch(Boolean exactMatch) {
		this.exactMatch = exactMatch;
	}
}
