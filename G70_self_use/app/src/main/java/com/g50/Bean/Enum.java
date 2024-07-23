package com.g50.Bean;

public class Enum {
	// 工作状态
	public class State {
		public final static int IDLE = 0; // 开机但未准备好
		public final static int BLACKLIST = 1; // 配置黑名单
		public final static int GNBCFG = 2; // 定位参数配置
		public final static int TRACE = 3; // 定位中
		public final static int CATCH = 4; // 侦码中
		public final static int REBOOT = 5; // 重启中
		public final static int UPDATE = 6; // 升级中
		public final static int GETLOG = 7; // 读取LOG
		public final static int STOP = 8; // 结束定位
	}
	// 工作模式: 0: no[便捷]; 1: yes [车载]
	public class WorkMode {
		public final static int NORMAL = 0;
		public final static int VEHICLE = 1;
	}
}
