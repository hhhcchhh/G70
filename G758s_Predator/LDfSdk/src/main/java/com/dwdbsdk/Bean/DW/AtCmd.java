package com.dwdbsdk.Bean.DW;

public class AtCmd {

	private static AtCmd instance;
	public static AtCmd build() {
		synchronized (AtCmd.class) {
			if (instance == null) {
				instance = new AtCmd();
			}
		}
		return instance;
	}

	// 以下数值不可变换
	public class PWR {
		public static final int LTE = 0;
		public static final int NR = 1;
	}
	// for 5G
	public class NR {

	}
	// PLMN
	public class OP {
		public final static int MOBILE = 0;
		public final static int UNICOM = 1;
		public final static int TELECOM = 2;
	}
}