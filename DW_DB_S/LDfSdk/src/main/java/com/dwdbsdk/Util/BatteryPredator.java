package com.dwdbsdk.Util;

import java.util.ArrayList;
import java.util.List;

public class BatteryPredator {

	private final List<batteryArray> batteryList = new ArrayList<>();
	private final List<Integer> volList = new ArrayList<>();

	public String percent;

	private static BatteryPredator instance;

	public static BatteryPredator build() {
		synchronized (BatteryPredator.class) {
			if (instance == null) {
				instance = new BatteryPredator();
			}
		}
		return instance;
	}

	public BatteryPredator() {
		init();
	}

	private void init() {
		percent = "检测中";
		volList.clear();
		batteryList.clear();
		// 实际测试做调整
		batteryList.add(new batteryArray(15000, "100")); 	// 第一个值默认给大点，无所谓
		batteryList.add(new batteryArray(12470, "100"));
		batteryList.add(new batteryArray(12440, "100"));

		batteryList.add(new batteryArray(12430, "99"));
		batteryList.add(new batteryArray(12410, "99"));

		// 19个
		batteryList.add(new batteryArray(12380, "98"));
		batteryList.add(new batteryArray(12350, "97"));
		batteryList.add(new batteryArray(12320, "96"));
		batteryList.add(new batteryArray(12290, "95"));
		batteryList.add(new batteryArray(12260, "94"));
		batteryList.add(new batteryArray(12230, "93"));
		batteryList.add(new batteryArray(12200, "92"));
		batteryList.add(new batteryArray(12170, "91"));
		batteryList.add(new batteryArray(12140, "90"));
		batteryList.add(new batteryArray(12110, "89"));
		batteryList.add(new batteryArray(12080, "88"));
		batteryList.add(new batteryArray(12050, "87"));
		batteryList.add(new batteryArray(12020, "86"));
		batteryList.add(new batteryArray(11990, "85"));
		batteryList.add(new batteryArray(11960, "84"));
		batteryList.add(new batteryArray(11930, "83"));
		batteryList.add(new batteryArray(11900, "82"));
		batteryList.add(new batteryArray(11870, "81"));
		batteryList.add(new batteryArray(11840, "80"));

		// 25个
		batteryList.add(new batteryArray(11810, "79"));
		batteryList.add(new batteryArray(11785, "78"));
		batteryList.add(new batteryArray(11760, "77"));
		batteryList.add(new batteryArray(11735, "76"));
		batteryList.add(new batteryArray(11710, "75"));
		batteryList.add(new batteryArray(11685, "74"));
		batteryList.add(new batteryArray(11660, "73"));
		batteryList.add(new batteryArray(11635, "72"));
		batteryList.add(new batteryArray(11610, "71"));
		batteryList.add(new batteryArray(11585, "70"));
		batteryList.add(new batteryArray(11560, "69"));
		batteryList.add(new batteryArray(11535, "68"));
		batteryList.add(new batteryArray(11510, "67"));
		batteryList.add(new batteryArray(11485, "66"));
		batteryList.add(new batteryArray(11460, "65"));
		batteryList.add(new batteryArray(11435, "64"));
		batteryList.add(new batteryArray(11410, "63"));
		batteryList.add(new batteryArray(11385, "62"));
		batteryList.add(new batteryArray(11360, "61"));
		batteryList.add(new batteryArray(11335, "60"));
		batteryList.add(new batteryArray(11310, "59"));
		batteryList.add(new batteryArray(11285, "58"));
		batteryList.add(new batteryArray(11260, "57"));
		batteryList.add(new batteryArray(11235, "56"));
		batteryList.add(new batteryArray(11210, "55"));
		batteryList.add(new batteryArray(11185, "54"));
		batteryList.add(new batteryArray(11160, "53"));
		batteryList.add(new batteryArray(11135, "52"));
		batteryList.add(new batteryArray(11110, "51"));
		batteryList.add(new batteryArray(11085, "50"));

		// 23个
		batteryList.add(new batteryArray(11060, "49"));
		batteryList.add(new batteryArray(11035, "48"));
		batteryList.add(new batteryArray(11010, "47"));
		batteryList.add(new batteryArray(10985, "46"));
		batteryList.add(new batteryArray(10960, "45"));
		batteryList.add(new batteryArray(10935, "43"));
		batteryList.add(new batteryArray(10910, "42"));
		batteryList.add(new batteryArray(10885, "41"));
		batteryList.add(new batteryArray(10860, "40"));
		batteryList.add(new batteryArray(10835, "39"));
		batteryList.add(new batteryArray(10810, "38"));
		batteryList.add(new batteryArray(10785, "37"));
		batteryList.add(new batteryArray(10760, "36"));
		batteryList.add(new batteryArray(10735, "35"));
		batteryList.add(new batteryArray(10710, "34"));
		batteryList.add(new batteryArray(10685, "33"));
		batteryList.add(new batteryArray(10660, "32"));
		batteryList.add(new batteryArray(10635, "31"));
		batteryList.add(new batteryArray(10610, "30"));
		batteryList.add(new batteryArray(10585, "29"));
		batteryList.add(new batteryArray(10560, "28"));
		batteryList.add(new batteryArray(10535, "27"));
		batteryList.add(new batteryArray(10510, "26"));
		batteryList.add(new batteryArray(10485, "25"));
		batteryList.add(new batteryArray(10460, "24"));
		batteryList.add(new batteryArray(10435, "23"));
		batteryList.add(new batteryArray(10410, "22"));
		batteryList.add(new batteryArray(10385, "21"));
		batteryList.add(new batteryArray(10360, "20"));
		batteryList.add(new batteryArray(10335, "19"));
		batteryList.add(new batteryArray(10310, "18"));

		// 38个
		batteryList.add(new batteryArray(10290, "17"));
		batteryList.add(new batteryArray(10270, "16"));
		batteryList.add(new batteryArray(10250, "15"));
		batteryList.add(new batteryArray(10230, "14"));
		batteryList.add(new batteryArray(10210, "13"));
		batteryList.add(new batteryArray(10190, "12"));
		batteryList.add(new batteryArray(10170, "11"));
		batteryList.add(new batteryArray(10150, "10"));

		batteryList.add(new batteryArray(10130, "9"));
		batteryList.add(new batteryArray(10110, "8"));
		batteryList.add(new batteryArray(10090, "7"));
		batteryList.add(new batteryArray(10070, "5"));
		batteryList.add(new batteryArray(10050, "4"));
		batteryList.add(new batteryArray(10030, "3"));
		batteryList.add(new batteryArray(10015, "2"));
		batteryList.add(new batteryArray(10010, "2"));
		batteryList.add(new batteryArray(9990, "1"));
		batteryList.add(new batteryArray(9970, "1"));
		batteryList.add(new batteryArray(9950, "0"));
	}

	public void handleVol(int vol) {
		// 小于3V，电池就会断电保护，对应值为1142，此处避免出现极端用1000处理
		// 3V = 1142.857142857143 * 315 / 120 = 3000 mV
		// SLog.D("getBattery() vol = " + vol);
		if (vol < 9950) vol = 9950;

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
