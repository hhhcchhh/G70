package com.dwdbsdk.Bean.DB;

public class DBProtocol {

    public static final int GR_2_UI_CFG_OK = 0;
    public static final int GR_2_UI_CFG_NG = 1;
    // 消息类型
    public static class MsgType {
        public static final int GR_NONE = 65535;   // IDLE

        public static final int GR_MSG_HELLO = 0x0001;
        public static final int GR_MSG_SET_TIME = 0x0002;
        public static final int GR_MSG_GET_VERSION = 0x0003;
        public static final int GR_MSG_BT_NAME = 0x0004;
        public static final int GR_MSG_WIFI_CFG = 0x0005;
        public static final int GR_MSG_REBOOT = 0x0006;
        public static final int GR_MSG_IMG_UPGRADE = 0x0007;
        public static final int GR_MSG_GET_LOG = 0x0008;
        public static final int BT_MSG_DEV_NAME = 0x0009;
        public static final int BT_MSG_DEV_SN = 0x000a;
        public static final int GR_MSG_START_JAM = 0x0020;
        public static final int GR_MSG_STOP_JAM = 0x0021;
        public static final int GR_MSG_GET_JAM = 0x0022;
        public static final int GR_MSG_START_SG = 0x0023;
        public static final int GR_MSG_STOP_SG = 0x0024;
        public static final int GR_MSG_START_POS_SCAN = 0x0030;
        public static final int GR_MSG_START_PWR_SCAN = 0x0032;
        public static final int GR_MSG_STOP_PWR_SCAN = 0x0033;
        public static final int GR_MSG_SCAN_REPORT = 0x0034;
        public static final int GR_MSG_POWER_REPORT = 0x0035;
        public static final int GR_MSG_RX_GAIN = 0x0040;
        public static final int GR_MSG_GPIO_CFG = 0x0041;
        public static final int GR_MSG_DATA_FWD = 0x012D;
        public static final int GR_MSG_START_SENSE_DET = 0x42;
        public static final int GR_MSG_SENSE_REPORT = 0x36;
        public static final int GR_MSG_STOP_SENSE_DET = 0x43;
    }

    public static class SOCKET {
        public static final int STATE_NONE = 0;       // we're doing nothing
        public static final int STATE_LISTEN = 1;     // now listening for incoming connections
        public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
        public static final int STATE_CONNECTED = 3;  // now connected to a remote device
        public static final int STATE_CONNECT_FAIL = 4;  // now connected to a remote device
        public static final int STATE_LOST = 5;  // now connected to a remote device
    }
}
