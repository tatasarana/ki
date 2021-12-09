package com.docotel.ki.pojo;

import java.io.Serializable;
import java.util.List;

import com.docotel.ki.model.transaction.TxMadrid;
import com.docotel.ki.model.transaction.TxReception;


public class MadridListObject implements Serializable {
	private List<TxMadrid> txMadridList;
	private List<TxReception> txReceptionList;
	private List<MadridDetailInfo> madridDetailInfoList;
	
	
	public List<TxMadrid> getTxMadridList() {
		return txMadridList;
	}
	public void setTxMadridList(List<TxMadrid> txMadridList) {
		this.txMadridList = txMadridList;
	}
	public List<TxReception> getTxReceptionList() {
		return txReceptionList;
	}
	public void setTxReceptionList(List<TxReception> txReceptionList) {
		this.txReceptionList = txReceptionList;
	}
	public List<MadridDetailInfo> getMadridDetailInfoList() {
		return madridDetailInfoList;
	}
	public void setMadridDetailInfoList(List<MadridDetailInfo> madridDetailInfoList) {
		this.madridDetailInfoList = madridDetailInfoList;
	}

}
