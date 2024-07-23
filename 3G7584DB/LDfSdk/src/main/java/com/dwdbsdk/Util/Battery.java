package com.dwdbsdk.Util;

import java.util.ArrayList;
import java.util.List;

public class Battery {

	private List<batteryArray> batteryList = new ArrayList<batteryArray>();
	private List<Integer> volList = new ArrayList<Integer>();

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
		batteryList.add(new batteryArray(24018, "100")); 	// 1490
		batteryList.add(new batteryArray(23924, "99")); 	//
		batteryList.add(new batteryArray(23868, "98"));
		batteryList.add(new batteryArray(23818, "97"));
		batteryList.add(new batteryArray(23766, "96"));//2
		batteryList.add(new batteryArray(23724, "95"));//4
		batteryList.add(new batteryArray(23683, "94"));//6
		batteryList.add(new batteryArray(23653, "93"));//20
		batteryList.add(new batteryArray(23623, "92"));//20
		batteryList.add(new batteryArray(23594, "91"));//20
		batteryList.add(new batteryArray(23568, "90"));//20
		batteryList.add(new batteryArray(23534, "89"));//24
		batteryList.add(new batteryArray(23503, "88"));//27
		batteryList.add(new batteryArray(23470, "87"));//27
		batteryList.add(new batteryArray(23401, "86"));//27
		batteryList.add(new batteryArray(23371, "85"));//27
		batteryList.add(new batteryArray(23335, "84"));//30
		batteryList.add(new batteryArray(23297, "83"));//30
		batteryList.add(new batteryArray(23158, "82"));//30
		batteryList.add(new batteryArray(23118, "81"));//30
		batteryList.add(new batteryArray(23074, "80"));//34
		batteryList.add(new batteryArray(22994, "79"));//44
		batteryList.add(new batteryArray(22927, "78"));//44
		batteryList.add(new batteryArray(22874, "77"));//44
		batteryList.add(new batteryArray(22832, "76"));//44
		batteryList.add(new batteryArray(22791, "75"));//44
		batteryList.add(new batteryArray(22734, "74"));//44
		batteryList.add(new batteryArray(22684, "73"));//44
		batteryList.add(new batteryArray(22607, "72"));//54
		batteryList.add(new batteryArray(22560, "71"));//54
		batteryList.add(new batteryArray(22464, "70"));//54
		batteryList.add(new batteryArray(22334, "69"));//54
		batteryList.add(new batteryArray(22230, "68"));//54
		batteryList.add(new batteryArray(22193, "67"));//54
		batteryList.add(new batteryArray(22104, "66"));//54
		batteryList.add(new batteryArray(22055, "65"));//54
		batteryList.add(new batteryArray(21974, "64"));//54
		batteryList.add(new batteryArray(21898, "63"));//54
		batteryList.add(new batteryArray(21794, "62"));//54
		batteryList.add(new batteryArray(21688, "61"));//54
		batteryList.add(new batteryArray(21537, "60"));//54

		batteryList.add(new batteryArray(21479, "50"));//58
		batteryList.add(new batteryArray(21445, "49"));//58
		batteryList.add(new batteryArray(21395, "48"));//58
		batteryList.add(new batteryArray(21335, "47"));//58
		batteryList.add(new batteryArray(21295, "46"));//58
		batteryList.add(new batteryArray(21245, "45"));//58
		batteryList.add(new batteryArray(21195, "43"));//58
		batteryList.add(new batteryArray(21145, "42"));//58
		batteryList.add(new batteryArray(21095, "41"));//58
		batteryList.add(new batteryArray(21045, "40"));//58
		batteryList.add(new batteryArray(20980, "39"));//58
		batteryList.add(new batteryArray(20920, "38"));//58
		batteryList.add(new batteryArray(20860, "37"));//58
		batteryList.add(new batteryArray(20800, "36"));//58
		batteryList.add(new batteryArray(20750, "35"));//58
		batteryList.add(new batteryArray(20680, "34"));//58
		batteryList.add(new batteryArray(20620, "33"));//58
		batteryList.add(new batteryArray(20570, "32"));//58
		batteryList.add(new batteryArray(20510, "31"));//58
		batteryList.add(new batteryArray(20452, "30"));//64
		batteryList.add(new batteryArray(20400, "29"));//71
		batteryList.add(new batteryArray(20350, "28"));//71
		batteryList.add(new batteryArray(20300, "27"));//71
		batteryList.add(new batteryArray(20250, "26"));//71
		batteryList.add(new batteryArray(20200, "25"));//71
		batteryList.add(new batteryArray(20150, "23"));//71
		batteryList.add(new batteryArray(20100, "22"));//75
		batteryList.add(new batteryArray(20053, "21"));//75
		batteryList.add(new batteryArray(19999, "20"));//95
		batteryList.add(new batteryArray(19848, "19"));//95
		batteryList.add(new batteryArray(19758, "18"));//95
		batteryList.add(new batteryArray(19688, "17"));//95
		batteryList.add(new batteryArray(19618, "16"));//100
		batteryList.add(new batteryArray(19578, "15"));//100
		batteryList.add(new batteryArray(19538, "14"));//100
		batteryList.add(new batteryArray(19498, "13"));//100
		batteryList.add(new batteryArray(19458, "12"));//100
		batteryList.add(new batteryArray(19418, "11"));//100
		batteryList.add(new batteryArray(19377, "10"));//105
		batteryList.add(new batteryArray(19277, "9"));//110
		batteryList.add(new batteryArray(19177, "8"));//110
		batteryList.add(new batteryArray(19032, "7"));//110
		batteryList.add(new batteryArray(18888, "5"));//110
		batteryList.add(new batteryArray(18657, "4"));//115
		batteryList.add(new batteryArray(18000, "3"));//115
		batteryList.add(new batteryArray(17814, "2"));//120
		batteryList.add(new batteryArray(17680, "1"));//121
		batteryList.add(new batteryArray(17425, "1"));//122
		batteryList.add(new batteryArray(17218, "1"));//123
		batteryList.add(new batteryArray(17030, "0"));//124
		batteryList.add(new batteryArray(16779, "0"));//125
		batteryList.add(new batteryArray(16779, "0"));
	}

	public void handleVol(int vol) {
		// 小于3V，电池就会断电保护，对应值为1142，此处避免出现极端用1000处理
		// 3V = 1142.857142857143 * 315 / 120 = 3000 mV
		// SLog.D("getBattery() vol = " + vol);
		if (vol < 17218) {
			vol = 17218;
		}
		if (volList.size() < 9) { // 取10次数据做处理
			volList.add(vol);
		} else {
			volList.add(vol);
			int sum = 0;
			int max = volList.get(0);
			int min = volList.get(0);
			for (int i = 0; i < volList.size(); i++) {
				sum += volList.get(i);
				if (max < volList.get(i)) {
					max = volList.get(i);
				}
				if (min > volList.get(i)) {
					min = volList.get(i);
				}
			}
			// 减去一个最大值、最小值，再取平均值
			sum -= max;
			sum -= min;
			int bettery = sum / (volList.size() - 2);
			//bettery = bettery * 315 / 120; // 分压
			// SLog.I("getBattery() bettery = " + bettery);
			// 取百分比
			for (int i = 0; i < batteryList.size() - 1; i++) {
				if (bettery < batteryList.get(i).vol
						&& bettery >= batteryList.get(i + 1).vol) {
					percent = batteryList.get(i + 1).getPercent();
					break;
				}
			}
			volList.clear();
		}
		//SLog.I("handleVol() vol = " + vol + ", pecent = " + percent);
	}

	public String getPercent() {
		return percent;
	}

	class batteryArray {
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
