package com.Wifi;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import android.graphics.Color;
import com.BarChart.DataElement;

public class WifiData {
	public static int MAX_ALARM = 5; // 最多监测个数
	public static int THREAD_TIME = 1; // 5S刷新一次
	public static int DOT_MAX = 180/THREAD_TIME; // 5分钟 = 300S除以刷新频率=多少根柱子
	
	private String SSID;
	private String BSSID;
	private int LEVEL;
	private int mDotCnt;
	private String distance;
	private List<DataElement> mList  = new ArrayList<DataElement>();
	private List<WifiLevel> mData = new ArrayList<WifiLevel>();
	private boolean isDetected = false;
	private boolean upToFirst = false;

	public WifiData(String sSID, String bSSID, int lEVEL) {
		super();
		SSID = sSID;
		BSSID = bSSID;
		LEVEL = lEVEL;
		BSSID = bSSID;
		mDotCnt = 0;
		for (int i = 0; i < DOT_MAX; i++) {
			mData.add(new WifiLevel(-90));
			// 这里Color.YELLOW设置没有用到，直接在BarChartView里用渐变色:290行附近
			mList.add(new DataElement(BSSID, -90, Color.YELLOW));
		}
		
		distance = Distance(LEVEL);
	}
	
	public WifiData(String sSID, String bSSID, int lEVEL, boolean isDetected, boolean upToFirst) {
		super();
		SSID = sSID;
		BSSID = bSSID;
		LEVEL = lEVEL;
		BSSID = bSSID;
		mDotCnt = 0;
		this.isDetected = isDetected;
		this.upToFirst = upToFirst;
		for (int i = 0; i < DOT_MAX; i++) {
			mData.add(new WifiLevel(-90));
			// 这里Color.YELLOW设置没有用到，直接在BarChartView里用渐变色:290行附近
			mList.add(new DataElement(BSSID, -90, Color.YELLOW));
		}
		
		distance = Distance(LEVEL);
	}

	public WifiData(String sSID, String bSSID, List<WifiLevel> mData, boolean isDetected) {
		super();
		SSID = sSID;
		BSSID = bSSID;
		this.isDetected = isDetected;
		this.mData.addAll(mData);
		for (int i = 0; i < DOT_MAX; i++) {
			mList.add(new DataElement(BSSID, (int)mData.get(i).getValue(), Color.YELLOW));
		}
	}
	
	public String Distance(int rssi) {
		/*距离公式：
		refDistance = 1.0   # 1m    == d0 in formula
		pathLoss = 3.0      #       == n in formula 
		此2个参数，可以根据实际情况调整。
		Def  doit_rssi(txPower, rssi):
		    c1 = txPower
		    c2 = pathLoss
		distance=refDistance * math.pow(10, (c1-rssi) / (10 * c2))/100
		根据公式计算距离，其中txPower=21*/
		
		double c1 = 21.0;
		double c2 = 3.0;
		
		double dis = 1.0 * Math.pow(10, (c1 - rssi) / (10 * c2))/100;
		DecimalFormat df = new DecimalFormat("######0.0");   
		
		return df.format(dis);
	}
	
	/**
	 * @return the upToFirst
	 */
	public boolean isUpToFirst() {
		return upToFirst;
	}

	/**
	 * @param upToFirst the upToFirst to set
	 */
	public void setUpToFirst(boolean upToFirst) {
		this.upToFirst = upToFirst;
	}
	
	/**
	 * @return the isDetected
	 */
	public boolean isDetected() {
		return isDetected;
	}

	/**
	 * @param isDetected the isDetected to set
	 */
	public void setDetected(boolean isDetected) {
		this.isDetected = isDetected;
	}

	/**
	 * @return the sSID
	 */
	public String getSSID() {
		return SSID;
	}
	
	/**
	 * @param sSID the sSID to set
	 */
	public void setSSID(String sSID) {
		SSID = sSID;
	}
	
	/**
	 * @return the lEVEL
	 */
	public int getLEVEL() {
		return LEVEL;
	}
	
	/**
	 * @param lEVEL the lEVEL to set
	 */
	public void setLEVEL(int lEVEL) {
		LEVEL = lEVEL;
		
		distance = Distance(lEVEL);
	}
	
	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}
	
	/**
	 * @return the bSSID
	 */
	public String getBSSID() {
		return BSSID;
	}
	
	/**
	 * @param bSSID the bSSID to set
	 */
	public void setBSSID(String bSSID) {
		BSSID = bSSID;
	}
	
	/**
	 * @return the mDotCnt
	 */
	public int getDotCnt() {
		return mDotCnt;
	}
	
	/**
	 * @param dotCnt the mDotCnt to set
	 */
	public void setDotCnt(int dotCnt) {
		this.mDotCnt = dotCnt;
	}
	
	/**
	 * @return the mData
	 */
	public List<WifiLevel> getData() {
		return mData;
	}

	/**
	 * @return the mList
	 */
	public List<DataElement> getList() {
		return mList;
	}
	
	public void handleData(int level) {
		for (int i = 1; i < DOT_MAX; i++) {
			// 数据向右移位，最后一个数放弃
			mList.get(i).setValue((int)mData.get(i - 1).getValue());
		}
		// 最新放第0位
		mList.get(0).setValue((int)level);
		for (int j = 0; j < DOT_MAX; j++) {
			// 获取最新数据
			mData.get(j).setValue(mList.get(j).getValue());
		}
		setLEVEL(-90);
	}
	
	private int getColor(double level) {
		
		int color = Color.YELLOW;
		if (level >= 80) {
			color = Color.YELLOW;
		} else if (level < 80 && level >= 60) {
			color = Color.GREEN;
		} else if (level < 60 && level >= 40) {
			color = Color.RED;
		} else {
			color = Color.BLUE;
		}
		return color;
	}
}
