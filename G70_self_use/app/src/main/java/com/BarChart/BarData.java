package com.BarChart;

import java.util.ArrayList;
import java.util.List;
import android.graphics.Color;

public class BarData {
	public static int DOT_MAX = 100;///THREAD_TIME; // 5分钟 = 300S除以刷新频率=多少根柱子
	public static int MIN_LEVEL = 20; 
	public static int MAX_LEVEL = 110; 
	
	private List<DataElement> mList = new ArrayList<DataElement>();
	private List<DataElement> mOldList = new ArrayList<DataElement>();

	public BarData() {
		super();
		mList.clear();
		mOldList.clear();
		for (int i = 0; i < DOT_MAX; i++) {
			mOldList.add(new DataElement(String.valueOf(i), MIN_LEVEL, Color.YELLOW));
			mList.add(new DataElement(String.valueOf(i), MIN_LEVEL, Color.YELLOW));
		}
	}

	/**
	 * @return the mList
	 */
	public List<DataElement> getList() {
		return mList;
	}
	
	public void reInit() {
		mList.clear();
		mOldList.clear();
		for (int i = 0; i < DOT_MAX; i++) {
			mOldList.add(new DataElement(String.valueOf(i), MIN_LEVEL, Color.YELLOW));
			mList.add(new DataElement(String.valueOf(i), MIN_LEVEL, Color.YELLOW));
		}
	}

	public void handleData(int level) {
		if (level == 0) {
			level = MIN_LEVEL + 1; 
		} else if (level < MIN_LEVEL) {
			level = MIN_LEVEL;
		} else if (level > MAX_LEVEL) {
			level = MAX_LEVEL;
		}
		for (int i = 1; i < DOT_MAX; i++) {
			// 数据向右移位，最后一个数放弃
			mList.get(i).setValue((int) mOldList.get(i - 1).getValue());
		}
		// 最新放第0位
	
		mList.get(0).setValue((int) level);
		for (int j = 0; j < DOT_MAX; j++) {
			// 获取最新数据
			mOldList.get(j).setValue(mList.get(j).getValue());
		}
	}
}
