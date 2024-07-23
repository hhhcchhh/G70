package com.nr.Util;

import java.util.ArrayList;
import java.util.List;

public class Battery {

	private final List<batteryArray> batteryList = new ArrayList<>();
	private final List<Integer> volList = new ArrayList<>();

	public String percent;

	private static Battery instance;

	public static Battery build() {
		synchronized (Battery.class) {
			if (instance == null) {
				instance = new Battery();
			}
		}
		return instance;
	}

	public Battery() {
		init();
	}

	private void init() {
		percent = "检测中";
		volList.clear();
		batteryList.clear();
		// 实际测试做调整
		batteryList.add(new batteryArray(30000, "100")); 	// 第一个值默认给大点，无所谓
		batteryList.add(new batteryArray(20150, "100"));
		batteryList.add(new batteryArray(20120, "100"));
		batteryList.add(new batteryArray(20091, "100"));

		batteryList.add(new batteryArray(20090, "99"));
		batteryList.add(new batteryArray(20075, "99"));
		batteryList.add(new batteryArray(20050, "99"));

		// 19个
		batteryList.add(new batteryArray(20000, "98"));
		batteryList.add(new batteryArray(19950, "97"));
		batteryList.add(new batteryArray(19900, "96"));
		batteryList.add(new batteryArray(19850, "95"));
		batteryList.add(new batteryArray(19800, "94"));
		batteryList.add(new batteryArray(19750, "93"));
		batteryList.add(new batteryArray(19700, "92"));
		batteryList.add(new batteryArray(19650, "91"));
		batteryList.add(new batteryArray(19600, "90"));
		batteryList.add(new batteryArray(19550, "89"));
		batteryList.add(new batteryArray(19500, "88"));
		batteryList.add(new batteryArray(19450, "87"));
		batteryList.add(new batteryArray(19400, "86"));
		batteryList.add(new batteryArray(19350, "85"));
		batteryList.add(new batteryArray(19300, "84"));
		batteryList.add(new batteryArray(19250, "83"));
		batteryList.add(new batteryArray(19200, "82"));
		batteryList.add(new batteryArray(19150, "81"));
		batteryList.add(new batteryArray(19100, "80"));

		// 25个
		batteryList.add(new batteryArray(19060, "79"));
		batteryList.add(new batteryArray(19020, "78"));
		batteryList.add(new batteryArray(18980, "77"));
		batteryList.add(new batteryArray(18940, "76"));
		batteryList.add(new batteryArray(18900, "75"));
		batteryList.add(new batteryArray(18860, "74"));
		batteryList.add(new batteryArray(18820, "73"));
		batteryList.add(new batteryArray(18780, "72"));
		batteryList.add(new batteryArray(18740, "71"));
		batteryList.add(new batteryArray(18700, "70"));
		batteryList.add(new batteryArray(18660, "69"));
		batteryList.add(new batteryArray(18620, "68"));
		batteryList.add(new batteryArray(18580, "67"));
		batteryList.add(new batteryArray(18540, "66"));
		batteryList.add(new batteryArray(18500, "65"));
		batteryList.add(new batteryArray(18460, "64"));
		batteryList.add(new batteryArray(18420, "63"));
		batteryList.add(new batteryArray(18380, "62"));
		batteryList.add(new batteryArray(18340, "61"));
		batteryList.add(new batteryArray(18300, "60"));
		batteryList.add(new batteryArray(18260, "59"));
		batteryList.add(new batteryArray(18220, "58"));
		batteryList.add(new batteryArray(18180, "57"));
		batteryList.add(new batteryArray(18140, "56"));
		batteryList.add(new batteryArray(18100, "55"));
		batteryList.add(new batteryArray(18060, "54"));
		batteryList.add(new batteryArray(18020, "53"));
		batteryList.add(new batteryArray(17980, "52"));
		batteryList.add(new batteryArray(17940, "51"));
		batteryList.add(new batteryArray(17900, "50"));

		// 29个
		batteryList.add(new batteryArray(17870, "49"));
		batteryList.add(new batteryArray(17840, "48"));
		batteryList.add(new batteryArray(17810, "47"));
		batteryList.add(new batteryArray(17790, "46"));
		batteryList.add(new batteryArray(17760, "45"));
		batteryList.add(new batteryArray(17730, "43"));
		batteryList.add(new batteryArray(17700, "42"));
		batteryList.add(new batteryArray(17670, "41"));
		batteryList.add(new batteryArray(17640, "40"));
		batteryList.add(new batteryArray(17610, "39"));
		batteryList.add(new batteryArray(17590, "38"));
		batteryList.add(new batteryArray(17560, "37"));
		batteryList.add(new batteryArray(17530, "36"));
		batteryList.add(new batteryArray(17500, "35"));
		batteryList.add(new batteryArray(17470, "34"));
		batteryList.add(new batteryArray(17440, "33"));
		batteryList.add(new batteryArray(17410, "32"));
		batteryList.add(new batteryArray(17380, "31"));
		batteryList.add(new batteryArray(17350, "30"));
		batteryList.add(new batteryArray(17320, "29"));
		batteryList.add(new batteryArray(17290, "28"));
		batteryList.add(new batteryArray(17260, "27"));
		batteryList.add(new batteryArray(17230, "26"));
		batteryList.add(new batteryArray(17200, "25"));
		batteryList.add(new batteryArray(17170, "24"));
		batteryList.add(new batteryArray(17140, "23"));
		batteryList.add(new batteryArray(17110, "22"));
		batteryList.add(new batteryArray(17080, "21"));
		batteryList.add(new batteryArray(17050, "20"));

		// 40个
		batteryList.add(new batteryArray(16930, "19"));
		batteryList.add(new batteryArray(16850, "18"));
		batteryList.add(new batteryArray(16730, "17"));
		batteryList.add(new batteryArray(16680, "16"));
		batteryList.add(new batteryArray(16530, "15"));
		batteryList.add(new batteryArray(16480, "14"));
		batteryList.add(new batteryArray(16330, "13"));
		batteryList.add(new batteryArray(16280, "12"));
		batteryList.add(new batteryArray(16130, "11"));
		batteryList.add(new batteryArray(16080, "10"));

		batteryList.add(new batteryArray(16030, "9"));
		batteryList.add(new batteryArray(15980, "8"));
		batteryList.add(new batteryArray(15930, "7"));
		batteryList.add(new batteryArray(15880, "5"));
		batteryList.add(new batteryArray(15830, "4"));
		batteryList.add(new batteryArray(15780, "3"));
		batteryList.add(new batteryArray(15730, "2"));
		batteryList.add(new batteryArray(15580, "2"));
		batteryList.add(new batteryArray(15530, "1"));
		batteryList.add(new batteryArray(14000, "1"));
		batteryList.add(new batteryArray(13000, "0"));
	}

	public void handleVol(int vol) {
		// 小于3V，电池就会断电保护，对应值为1142，此处避免出现极端用1000处理
		// 3V = 1142.857142857143 * 315 / 120 = 3000 mV
		// SLog.D("getBattery() vol = " + vol);
		if (vol < 13000) vol = 13000;

		volList.add(vol);
		int size = volList.size();
		if (size > 5){
			if (size == 6){
				// 一开始的数据不稳定，在此过滤一次，替换前2个
				volList.set(0, vol);
				volList.set(1, vol);
			}
			if (size > 10) volList.remove(0); // 最多取10次数据做处理

			int sum = 0;
			int max = volList.get(0);
			int min = volList.get(0);
			for (int i = 0; i < volList.size(); i++) {
				sum += volList.get(i);
				if (max < volList.get(i)) max = volList.get(i);
				if (min > volList.get(i)) min = volList.get(i);
			}
			// 减去一个最大值、最小值，再取平均值
			sum -= max;
			sum -= min;
			int battery = sum / (volList.size() - 2);
			//battery = battery * 315 / 120; // 分压
			// SLog.I("getBattery() battery = " + battery);
			// 取百分比
			for (int i = 0; i < batteryList.size() - 1; i++) {
				if (battery < batteryList.get(i).vol && battery >= batteryList.get(i + 1).vol) {
					percent = batteryList.get(i + 1).getPercent();
					break;
				}
			}
		}
		//SLog.I("handleVol() vol = " + vol + ", pecent = " + percent);
	}

	public String getPercent() {
		return percent;
	}

	static class batteryArray {
		int vol;
		String percent;

		public batteryArray(int vol, String percent) {
			super();
			this.vol = vol;
			this.percent = percent;
		}

		public int getVol() {
			return vol;
		}

		public String getPercent() {
			return percent;
		}
	}
}
