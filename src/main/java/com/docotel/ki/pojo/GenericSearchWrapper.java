package com.docotel.ki.pojo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GenericSearchWrapper<T> implements Serializable {
	private List<T> list = new ArrayList();
	private long count;

	public List<T> getList() {
		return this.list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

	public long getCount() {
		return this.count;
	}

	public void setCount(long count) {
		this.count = count;
	}
}
