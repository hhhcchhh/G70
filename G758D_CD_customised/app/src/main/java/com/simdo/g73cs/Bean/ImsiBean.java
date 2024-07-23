package com.simdo.g73cs.Bean;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class ImsiBean {
	public ImsiBean() {
		this.state = 0;
		this.imsi = "000000000000000";
		this.arfcn = "000000";
		this.pci = "000";
		this.firstTime = 0;
		this.latestTime = 0;
		this.cellId = 0;
		this.lossCount = 0;
		this.upCount = 1;
		this.rsrp = 0;
	}

	public boolean matchesConstraint(CharSequence constraint) {
		// 如果名称包含了搜索条件（constraint），则返回 true，否则返回 false
		return imsi.contains(constraint.toString());
	}

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
	int rsrp;
	int cellId;

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

	public void setFirstTime(long firstTime) {
		this.firstTime = firstTime;
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

	public static ImsiBean fromJson(JSONObject jsonObject) {
		ImsiBean imsiBean = new ImsiBean();
		try {
			if (jsonObject.has("IMSI")) {
				imsiBean.setImsi(jsonObject.getString("IMSI"));
			}
			if (jsonObject.has("ARFCN")) {
				imsiBean.setArfcn(jsonObject.getString("ARFCN"));
			}
			if (jsonObject.has("PCI")) {
				imsiBean.setPci(jsonObject.getString("PCI"));
			}
			if (jsonObject.has("FIRSTTIME")) {
				imsiBean.setFirstTime(jsonObject.getLong("FIRSTTIME"));
			}
			if (jsonObject.has("LATESTTIME")) {
				imsiBean.setLatestTime(jsonObject.getLong("LATESTTIME"));
			}
			if (jsonObject.has("STATE")) {
				imsiBean.setState(jsonObject.getInt("STATE"));
			}
			if (jsonObject.has("CELLID")) {
				imsiBean.setCellId(jsonObject.getInt("CELLID"));
			}
			if (jsonObject.has("LOSSCOUNT")) {
				imsiBean.setLossCount(jsonObject.getInt("LOSSCOUNT"));
			}
			if (jsonObject.has("UPCOUNT")) {
				imsiBean.setUpCount(jsonObject.getInt("UPCOUNT"));
			}
			if (jsonObject.has("RSRP")) {
				imsiBean.setRsrp(jsonObject.getInt("RSRP"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return imsiBean;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ImsiBean)) return false;
		ImsiBean imsiBean = (ImsiBean) o;
		return getState() == imsiBean.getState() && getLatestTime() == imsiBean.getLatestTime() && getFirstTime() == imsiBean.getFirstTime() && getLossCount() == imsiBean.getLossCount() && getUpCount() == imsiBean.getUpCount() && getRsrp() == imsiBean.getRsrp() && getCellId() == imsiBean.getCellId() && Objects.equals(getImsi(), imsiBean.getImsi()) && Objects.equals(getArfcn(), imsiBean.getArfcn()) && Objects.equals(getPci(), imsiBean.getPci());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getState(), getImsi(), getArfcn(), getPci(), getLatestTime(), getFirstTime(), getLossCount(), getUpCount(), getRsrp(), getCellId());
	}
}
