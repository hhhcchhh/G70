package com.simdo.g758s_predator.Util;

import java.util.ArrayList;
import java.util.List;

public class BatteryUtil {

	private List<batteryArray> batteryList = new ArrayList<batteryArray>();
	private List<Integer> volList = new ArrayList<Integer>();
	public String percent;
	public BatteryUtil() {
		init();
	}

	private void init() {
		percent = "检测中";
		volList.clear();
		batteryList.clear();
		// 实际测试做调整
		batteryList.add(new batteryArray(12020, "100")); 	// 1490
		batteryList.add(new batteryArray(12010, "99")); 	//
		batteryList.add(new batteryArray(12000, "98"));
		batteryList.add(new batteryArray(11990, "97"));
		batteryList.add(new batteryArray(11980, "96"));//2
		batteryList.add(new batteryArray(11970, "95"));//4
		batteryList.add(new batteryArray(11960, "94"));//6
		batteryList.add(new batteryArray(11950, "93"));//20
		batteryList.add(new batteryArray(11940, "92"));//20
		batteryList.add(new batteryArray(11930, "91"));//20
		batteryList.add(new batteryArray(11920, "90"));//20

		batteryList.add(new batteryArray(11906, "89"));//24
		batteryList.add(new batteryArray(11883, "88"));//27
		batteryList.add(new batteryArray(11863, "87"));//27
		batteryList.add(new batteryArray(11843, "86"));//27
		batteryList.add(new batteryArray(11823, "85"));//27
		batteryList.add(new batteryArray(11803, "84"));//30
		batteryList.add(new batteryArray(11783, "83"));//30
		batteryList.add(new batteryArray(11763, "82"));//30
		batteryList.add(new batteryArray(11743, "81"));//30
		batteryList.add(new batteryArray(11723, "80"));//34

		batteryList.add(new batteryArray(11707, "79"));//44
		batteryList.add(new batteryArray(11681, "78"));//44
		batteryList.add(new batteryArray(11671, "77"));//44
		batteryList.add(new batteryArray(11661, "76"));//44
		batteryList.add(new batteryArray(11651, "75"));//44
		batteryList.add(new batteryArray(11631, "74"));//44
		batteryList.add(new batteryArray(11611, "73"));//44
		batteryList.add(new batteryArray(11601, "72"));//54
		batteryList.add(new batteryArray(11581, "71"));//54
		batteryList.add(new batteryArray(11561, "70"));//54

		batteryList.add(new batteryArray(11535, "69"));//54
		batteryList.add(new batteryArray(11514, "68"));//54
		batteryList.add(new batteryArray(11494, "67"));//54
		batteryList.add(new batteryArray(11474, "66"));//54
		batteryList.add(new batteryArray(11454, "65"));//54
		batteryList.add(new batteryArray(11434, "64"));//54
		batteryList.add(new batteryArray(11404, "63"));//54
		batteryList.add(new batteryArray(11384, "62"));//54
		batteryList.add(new batteryArray(11364, "61"));//54
		batteryList.add(new batteryArray(11344, "60"));//54

		batteryList.add(new batteryArray(11328, "59"));//58
		batteryList.add(new batteryArray(11318, "58"));//58
		batteryList.add(new batteryArray(11308, "57"));//58
		batteryList.add(new batteryArray(11298, "56"));//58
		batteryList.add(new batteryArray(11278, "55"));//58
		batteryList.add(new batteryArray(11268, "54"));//58
		batteryList.add(new batteryArray(11258, "53"));//58
		batteryList.add(new batteryArray(11248, "52"));//58
		batteryList.add(new batteryArray(11238, "51"));//58
		batteryList.add(new batteryArray(11226, "50"));//58

		batteryList.add(new batteryArray(11209, "49"));//58
		batteryList.add(new batteryArray(11199, "48"));//58
		batteryList.add(new batteryArray(11189, "47"));//58
		batteryList.add(new batteryArray(11179, "46"));//58
		batteryList.add(new batteryArray(11169, "45"));//58
		batteryList.add(new batteryArray(11159, "43"));//58
		batteryList.add(new batteryArray(11149, "42"));//58
		batteryList.add(new batteryArray(11139, "41"));//58
		batteryList.add(new batteryArray(11130, "40"));//58

		batteryList.add(new batteryArray(11120, "39"));//58
		batteryList.add(new batteryArray(11110, "38"));//58
		batteryList.add(new batteryArray(11100, "37"));//58
		batteryList.add(new batteryArray(11090, "36"));//58
		batteryList.add(new batteryArray(11080, "35"));//58
		batteryList.add(new batteryArray(11070, "34"));//58
		batteryList.add(new batteryArray(11060, "33"));//58
		batteryList.add(new batteryArray(11050, "32"));//58
		batteryList.add(new batteryArray(11040, "31"));//58
		batteryList.add(new batteryArray(11020, "30"));//64

		batteryList.add(new batteryArray(11000, "29"));//71
		batteryList.add(new batteryArray(10991, "28"));//71
		batteryList.add(new batteryArray(10981, "27"));//71
		batteryList.add(new batteryArray(10971, "26"));//71
		batteryList.add(new batteryArray(10951, "25"));//71
		batteryList.add(new batteryArray(10941, "23"));//71
		batteryList.add(new batteryArray(10931, "22"));//75
		batteryList.add(new batteryArray(10921, "21"));//75
		batteryList.add(new batteryArray(10901, "20"));//95

		batteryList.add(new batteryArray(10880, "19"));//95
		batteryList.add(new batteryArray(10870, "18"));//95
		batteryList.add(new batteryArray(10850, "17"));//95
		batteryList.add(new batteryArray(10840, "16"));//100
		batteryList.add(new batteryArray(10820, "15"));//100
		batteryList.add(new batteryArray(10800, "14"));//100
		batteryList.add(new batteryArray(10790, "13"));//100
		batteryList.add(new batteryArray(10770, "12"));//100
		batteryList.add(new batteryArray(10750, "11"));//100
		batteryList.add(new batteryArray(10730, "10"));//105

		batteryList.add(new batteryArray(10714, "9"));//110
		batteryList.add(new batteryArray(10514, "8"));//110
		batteryList.add(new batteryArray(10314, "7"));//110
		batteryList.add(new batteryArray(10114, "5"));//110
		batteryList.add(new batteryArray(9900, "4"));//115
		batteryList.add(new batteryArray(9700, "3"));//115
		batteryList.add(new batteryArray(9500, "2"));//120
		batteryList.add(new batteryArray(9300, "1"));//121
		batteryList.add(new batteryArray(9000, "0"));//124
		batteryList.add(new batteryArray(8659, "0"));//125
	}

	public void handleVol(int vol) {
		if (vol < 9000) {
			vol = 9000;
		}
		if (volList.size() < 6) { // 取10次数据做处理
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
