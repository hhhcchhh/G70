package com.simdo.dw_multiple.Util;


import com.nr.Arfcn.Bean.LocBean;

import java.util.ArrayList;
import java.util.List;

public class Mode3 {
	public static int autoParsePci(String arfcn, String spci, List<LocBean> locList) {
		List<LocBean> tmpList = new ArrayList<LocBean>();
		// 取同频点数据
		for (int i = 0; i < locList.size(); i++) {
			if (arfcn.equals(locList.get(i).getArfcn())) {
				tmpList.add(locList.get(i));
			}
		}
		// 取PCI 值MODE3
		boolean has_1 = false;
		boolean has_2 = false;
		int pci = Integer.parseInt(spci);
		for (int i = 0; i < tmpList.size(); i++) {
			String sspci = tmpList.get(i).getPci();
			if (DataUtil.isNumeric(sspci)) {
				int offset = pci - Integer.parseInt(sspci);
				if (offset == 1 || offset == -1) has_1 = true;
				else if (offset == 2 || offset == -2) has_2 = true;
			}
		}
		if (!has_1) {
			pci += 1;
		} else if (!has_2) {
			pci += 2;
		} else {
			pci += 3;
		}
		if (pci > 1007) {
			pci -= 2;
		}
		if (pci <= 0) {
			pci += 1;
		}
		return pci;
	}
	// 自动处理避开模3干扰，若无法避开，则取同频点最弱的PCI值
	public static String autoParsePci(String arfcn, List<LocBean> locList) {
		String strPci;
		List<LocBean> tmpList = new ArrayList<LocBean>();
		// 取同频点数据
		for (int i = 0; i < locList.size(); i++) {
			if (arfcn.equals(locList.get(i).getArfcn())) {
				tmpList.add(locList.get(i));
			}
		}
		// PCI解析，去模3或取信号最弱数据
		boolean lowest_rx = false;
		boolean mode_3_0 = false;
		boolean mode_3_1 = false;
		boolean mode_3_2 = false;
		int pci;
		if (tmpList.size() == 0) {
			strPci = "199";
		} else if (tmpList.size() == 1) { // 只有一个
			pci = Integer.valueOf(tmpList.get(0).getPci()) + 1;
			strPci = String.valueOf(pci);
		} else if (tmpList.size() == 2) { // 两个
			strPci = have2mode(tmpList);
		} else { // 大于等于3个
			int mode_3 = 0;
			for (int i = 0; i < tmpList.size(); i++) {
				mode_3 = mode(Integer.valueOf(tmpList.get(i).getPci()), 3);
				switch (mode_3) {
				case 0:
					mode_3_0 = true;
					break;
				case 1:
					mode_3_1 = true;
					break;
				case 2:
					mode_3_2 = true;
					break;
				}
				if (mode_3_0 && mode_3_1 && mode_3_2) {
					lowest_rx = true;
					break; // 3模都有，取信号最弱的PCI
				}
			}

			if (lowest_rx) { // 3模都有，取信号最弱的PCI
				strPci = getLowestRx(tmpList);
			} else { // 只有两模或一模
				int mode_cnt = 0;
				if (mode_3_0) {
					mode_cnt++;
				}
				if (mode_3_1) {
					mode_cnt++;
				}
				if (mode_3_2) {
					mode_cnt++;
				}
				if (mode_cnt == 1) { // 只有1模
					pci = Integer.valueOf(tmpList.get(0).getPci()) + 1;
					strPci = String.valueOf(pci);
				} else { // 有2模
					strPci = have2mode(tmpList);
				}
			}
		}
		int tmpPci = Integer.valueOf(strPci);
		if (tmpPci > 1007) {
			tmpPci -= 2;
		}
		return String.valueOf(tmpPci);
	}

	private static String getLowestRx(List<LocBean> tmpList) {
		int max = Math.abs(Integer.valueOf(tmpList.get(0).getRx()));
		int idx = 0;
		for (int i = 1; i < tmpList.size(); i++) {
			int rx = Math.abs(Integer.valueOf(tmpList.get(i).getRx()));
			if (max < rx) {
				max = rx;
				idx = i;
			}
		}
		return String.valueOf(Integer.valueOf(tmpList.get(idx).getPci()));
	}

	private static String have2mode(List<LocBean> tmpList) {
		int pci;
		boolean mode_3_0 = false;
		boolean mode_3_1 = false;
		boolean mode_3_2 = false;
		String mStrPci;
		int mode_3 = mode(Integer.valueOf(tmpList.get(0).getPci()), 3);
		switch (mode_3) {
		case 0:
			mode_3_0 = true;
			break;
		case 1:
			mode_3_1 = true;
			break;
		case 2:
			mode_3_2 = true;
			break;

		}
		mode_3 = mode(Integer.valueOf(tmpList.get(1).getPci()), 3);
		switch (mode_3) {
		case 0:
			mode_3_0 = true;
			break;
		case 1:
			mode_3_1 = true;
			break;
		case 2:
			mode_3_2 = true;
			break;
		}
		if (mode_3_0 && mode_3_1) {// 取模2
			pci = Integer.valueOf(tmpList.get(1).getPci());
			if (mode(pci, 3) == 0) {// 为模0
				pci += 2;
			} else { // 为模1
				pci += 1;
			}
			mStrPci = String.valueOf(pci);
		} else if (mode_3_0 && mode_3_2) {// 取模1
			pci = Integer.valueOf(tmpList.get(1).getPci());
			if (mode(pci, 3) == 0) {// 为模0
				pci += 1;
			} else { // 为模2
				pci -= 1;
			}
			mStrPci = String.valueOf(pci);
		} else { // (mode_3_1 && mode_3_2)// 取模0
			pci = Integer.valueOf(tmpList.get(1).getPci());
			if (mode(pci, 3) == 1) {// 为模1
				pci -= 1;
			} else { // 为模2
				pci -= 2;
			}
			mStrPci = String.valueOf(pci);
		}
		return mStrPci;
	}

	private static int mode(int x, int y) {
		int ret = x % y;

		return ret;
	}
}
