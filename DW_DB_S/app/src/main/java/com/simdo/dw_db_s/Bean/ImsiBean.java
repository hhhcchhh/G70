package com.simdo.dw_db_s.Bean;

public class ImsiBean {
	public class State {
		public final static int IMSI_NEW = 0;// 新扫到的
		public final static int IMSI_OLD = 1;//上一次定位扫到的
		public final static int IMSI_BL = 2;//黑名单中
		public final static int IMSI_NOW = 3;//当前定位
		public final static int IMSI_NOW_BL = 4;//当前定位且为黑名单
	}

	int state; // 定位标志
	String imsi;
	String arfcn;
	String pci;
	long latestTime;
	long firstTime;
	int lossCount;
	int upCount;

	public int getUpCount() {
		return upCount;
	}

	public void setUpCount(int upCount) {
		this.upCount = upCount;
	}

	public int getRsrp() {
		return rsrp;
	}

	public void setRsrp(int rsrp) {
		this.rsrp = rsrp;
	}

	int rsrp;
	int cellId;
	public ImsiBean(int state, String imsi, String arfcn, String pci, int rsrp, long time, int cellId) {
		this.state = state;
		this.imsi = imsi;
		this.arfcn = arfcn;
		this.pci = pci;
		this.firstTime = time;
		this.latestTime = time;
		this.cellId = cellId;
		this.lossCount = 0;
		this.upCount = 1;
		this.rsrp = rsrp;
	}

	public int getCellId() {
		return cellId;
	}

	public void setCellId(int cellId) {
		this.cellId = cellId;
	}

	public long getFirstTime() {
		return firstTime;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getImsi() {
		return imsi;
	}

	public int getLossCount() {
		return lossCount;
	}

	public void setLossCount(int lossCount) {
		this.lossCount = lossCount;
	}

	public void setImsi(String imsi) {
		this.imsi = imsi;
	}

	public String getArfcn() {
		return arfcn;
	}

	public void setArfcn(String arfcn) {
		this.arfcn = arfcn;
	}

	public String getPci() {
		return pci;
	}

	public void setPci(String pci) {
		this.pci = pci;
	}

	public long getLatestTime() {
		return latestTime;
	}

	public void setLatestTime(long latestTime) {
		this.latestTime = latestTime;
	}
}
