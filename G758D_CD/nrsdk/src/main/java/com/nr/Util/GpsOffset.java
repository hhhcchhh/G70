/**
 * 获取的值是GPS时偏 3000000ns
 */
package com.nr.Util;

import com.Logcat.SLog;

public class GpsOffset {

	private static GpsOffset instance;

	public static GpsOffset build() {
		synchronized (GpsOffset.class) {
			if (instance == null) {
				instance = new GpsOffset();
			}
		}
		return instance;
	}

	public GpsOffset() {
		timing = -1;
	}

	public long getTiming() {
		return timing;
	}

	public void setTiming(long timing) {
		SLog.I("GpsOffset: timing = " + timing);
		this.timing = timing;
	}

	private long timing;
}
