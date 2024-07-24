/**
 * 获取的值是GPS时偏 3000000ns
 */
package com.dwdbsdk.Util;

import com.dwdbsdk.Logcat.SdkLog;

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
		SdkLog.I("GpsOffset: timing = " + timing);
		this.timing = timing;
	}

	private long timing;
}
