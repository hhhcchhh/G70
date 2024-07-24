package com.simdo.dw_db_s.Bean;

public class GnbBean {
    public final static int MAX_TAC_NUM = 6;
    // 工作状态
    public static class DW_State {
        public final static int NONE = -1; // 不在线
        public final static int IDLE = 0; // 空闲
        public final static int BLACKLIST = 1; // 配置黑名单
        public final static int GNB_CFG = 2; // 定位参数配置
        public final static int CFG_TRACE = 3; // TRACE|CATCH|CONTROL，但还未启动
        public final static int TRACE = 4; // 定位中
        public final static int CATCH = 5; // 侦码中
        public final static int CONTROL = 6; // 管控中
        public final static int STOP = 7; // 结束 TRACE|CATCH|CONTROL
        public final static int UPDATE = 8; // 升级中
        public final static int GET_LOG = 9; // 读取LOG
        public final static int REBOOT = 10; // 重启中
        public final static int PHY_ABNORMAL = 11; // 基带异常
        public final static int CHANGE_WORK_MODE = 12; // 切换工作模式：单双通道
        public final static int FREQ_SCAN = 13; // 扫频中
        public final static int GETOPLOG = 14; //
    }

    public static class DB_State {
        public final static int NONE = 0; // 开机或连接中
        public final static int READY = 1; // 准备就绪
        public final static int START = 2; // 启动干扰|单兵
        public final static int JAMING = 3; // 干扰中
        public final static int STOP = 4; // 结束干扰
        public final static int REBOOT = 5; // 重启
        public final static int GETLOG = 6; // 读取LOG
        public final static int UPGRADE = 7; // 升级
        public final static int PWR_DETECT = 8; // 单兵工作中
    }
    public class CellId {
        public static final int FIRST = 0;
        public static final int SECOND = 1;
        public static final int THIRD = 2;
        public static final int FOURTH = 3;
    }
    public class DualCell{
        public static final int SINGLE = 1;
        public static final int DUAL = 2;
    }

}

