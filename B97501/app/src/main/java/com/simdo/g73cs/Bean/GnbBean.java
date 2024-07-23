package com.simdo.g73cs.Bean;

public class GnbBean {
	// 工作状态
	public static class State {
		public final static int NONE = -1; // 不在线
		public final static int IDLE = 0; // 空闲
		public final static int BLACKLIST = 1; // 配置黑名单
		public final static int GNB_CFG = 2; // 定位参数配置
		public final static int CFG_TRACE = 3; // TRACE|CATCH|CONTROL，但还未启动
		public final static int TRACE = 4; // 定位中
		public final static int CATCH = 5; // 侦码中
		public final static int CONTROL = 6; // 搜寻中
		public final static int STOP = 7; // 结束 TRACE|CATCH|CONTROL
		public final static int UPDATE = 8; // 升级中
		public final static int GET_LOG = 9; // 读取LOG
		public final static int REBOOT = 10; // 重启中
		public final static int PHY_ABNORMAL = 11; // 基带异常
		public final static int CHANGE_WORK_MODE = 12; // 切换工作模式：单双通道
		public final static int FREQ_SCAN = 13; // 扫频中
	}
}

