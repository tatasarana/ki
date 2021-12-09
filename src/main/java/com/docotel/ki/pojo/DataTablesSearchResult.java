package com.docotel.ki.pojo;

import java.util.List;

public class DataTablesSearchResult {
	private int draw;
	private long recordsTotal;
	private long recordsFiltered;
	private List<String[]> data;
	private String error = null;

	public int getDraw() {
		return this.draw;
	}

	public void setDraw(int draw) {
		this.draw = draw;
	}

	public long getRecordsTotal() {
		return this.recordsTotal;
	}

	public void setRecordsTotal(long recordsTotal) {
		this.recordsTotal = recordsTotal;
	}

	public long getRecordsFiltered() {
		return this.recordsFiltered;
	}

	public void setRecordsFiltered(long recordsFiltered) {
		this.recordsFiltered = recordsFiltered;
	}

	public List<String[]> getData() {
		return this.data;
	}

	public void setData(List<String[]> data) {
		this.data = data;
	}

	public String getError() {
		return this.error;
	}

	public void setError(String error) {
		this.error = error;
	}
}