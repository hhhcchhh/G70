package com.dwdbsdk.Bean.DW;

public class DWProtocol {

    /********************************************************************************/
    public static final int XMSG_T_OAM_CFG = 0x30;
    public static final int XMSG_T_OAM_APK = 0x31;
    public static final int XMSG_T_OAM_BT = 0x32;
    public static final int XMSG_T_OAM_CGI = 0x33;
    public static final int XMSG_T_OAM_METH = 0x34;

    public static final int OAM_ACK_OK = 0;  //指令配置成功返回值
    public static final int OAM_ACK_ERROR = 1;  //指令配置失败通用返回值
    public static final int OAM_ACK_E_PARAM = 2;  //指令配置失败参数异常
    public static final int OAM_ACK_E_BUSY = 3;  //指令配置失败系统忙异常
    public static final int OAM_ACK_E_TRANSFER = 4;  //指令配置失败传输异常
    public static final int OAM_ACK_E_SYS_STATE = 5;  //指令配置失败系统状态异常
    public static final int OAM_ACK_E_ASYNC_FAIL = 6;  //指令配置空口同步失败(10号消息)
    public static final int OAM_ACK_E_GPS_UNLOCK = 7;  //指令配置GPS同步失败(10号消息)
    public static final int OAM_ACK_E_HW_CFG_FAIL = 8;  //指令配置硬件配置异常

    public static final int OAM_STR_MAX = 32;
    public static final int MAX_TAC_NUM = 6; //tac 递增上限100
    public static final int MAX_IMSI_LEN = 16; /*IMSI 数据长度*/
    public static final int MAX_IMSI_USE_LEN = MAX_IMSI_LEN - 1; /*有效长度*/
    public static final int MAX_TARGET_UE_NUM = 3;
    public static final int MAX_BLACK_IMSI_NUM = 100;
    public static final int MAX_DROP_SAVE_IMSI = 20; // 下拉菜单最多保存20个历史数据
    public static final int EXT_GPIO_CNT = 8; // PA 8个端口
    /********************************************************************************/
    public static final int UI_NONE = 65535;   // IDLE
    public static final int UI_2_gNB_HEART_BEAT = 1;   // APP发送心跳到单板
    public static final int UI_2_gNB_VERSION_UPGRADE = 3;   // 版本升级
    public static final int UI_2_gNB_GET_LOG_REQ = 4;   // 获取基带板log，获取后将清除单板上的log
    public static final int UI_2_gNB_WRITE_OP_RECORD = 5;   //  write operation record
    public static final int UI_2_gNB_GET_OP_LOG_REQ = 6;   //  get operation log
    public static final int UI_2_gNB_DELETE_OP_LOG_REQ = 7;   //  delete operation log
    public static final int UI_2_gNB_SET_TIME = 9;   // 单板起来后进行配置
    public static final int UI_2_gNB_CFG_gNB = 10; // 可多次配置，例如TA改变
    public static final int UI_2_gNB_LTE_CFG_gNB = 110; // 4G 可多次配置，例如TA改变
    public static final int UI_2_gNB_SET_BLACK_UE_LIST = 11; // 可多次配置，每次覆盖，最多8个UE
    public static final int UI_2_gNB_SET_TX_POWER_OFFSET = 12; // 可多次配置
    public static final int UI_2_gNB_START_CATCH = 13; // 帧码。与定位互斥
    public static final int UI_2_gNB_STOP_CATCH = 14; //
    public static final int UI_2_gNB_START_TRACE = 15; // 定位。与帧码互斥。支持一个目标UE。
    public static final int UI_2_gNB_STOP_TRACE = 16; // 停止5G定位
    public static final int UI_2_gNB_START_LTE_BLACK_UE_LIST = 111; // 定位4G
    public static final int UI_2_gNB_START_LTE_TRACE = 115; // 定位4G
    public static final int UI_2_gNB_STOP_LTE_TRACE = 116; // 停止4G定位
    public static final int UI_2_gNB_REBOOT_gNB = 17; // 重启
    public static final int UI_2_gNB_QUERY_gNB_VERSION = 18; // 可多次请求
    public static final int UI_2_gNB_WIFI_CFG = 20;  // 重启设备生效

    public static final int UI_2_gNB_START_CONTROL = 30;  //
    public static final int UI_2_eNB_START_CONTROL = 130;  //
    public static final int UI_2_gNB_STOP_CONTROL = 31;  //
    public static final int UI_2_eNB_STOP_CONTROL = 131;  //


    public static final int gNB_2_UI_REPORT_UE_INFO = 103; // 定位上报
    public static final int gNB_2_UI_REPORT_LTE_UE_INFO = 203; // 4G定位上报

    /********************************************************************************/
    public static final int UI_2_gNB_OAM_MSG = 1000;
    public static final int OAM_MSG_SET_BT_NAME = 201;
    public static final int OAM_MSG_GET_METH_CFG = 202;
    public static final int OAM_MSG_SET_METH_CFG = 203;
    public static final int OAM_MSG_GET_FTP_SERVER = 204;
    public static final int OAM_MSG_SET_FTP_SERVER = 205;
    public static final int OAM_MSG_GET_GPIO_MODE = 206;
    public static final int OAM_MSG_SET_GPIO_MODE = 207;
    public static final int OAM_MSG_GET_SYS_INFO = 208;
    public static final int OAM_MSG_SET_SYS_INFO = 209;
    public static final int OAM_MSG_ADJUST_TX_ATTEN = 210;
    public static final int OAM_MSG_SET_GPS_CFG = 211;
    public static final int OAM_MSG_SET_DUAL_CELL = 212;
    public static final int OAM_MSG_SET_RX_GAIN = 214;
    public static final int OAM_MSG_GET_SYS_LOG = 215;
    public static final int OAM_MSG_SET_FAN_SPEED = 216;
    public static final int OAM_MSG_GET_CATCH_CFG = 220;
    public static final int OAM_MSG_SET_GPS_IO_CFG = 224;
    public static final int OAM_MSG_GET_GPS_IO_CFG = 225;
    public static final int OAM_MSG_START_FREQ_SCAN = 226;
    public static final int OAM_MSG_FREQ_SCAN_REPORT = 227;
    public static final int OAM_MSG_STOP_FREQ_SCAN = 228;
    public static final int OAM_MSG_SET_JAM_ARFCN = 229;
    public static final int OAM_MSG_START_TD_MEASURE = 230;
    public static final int OAM_MSG_FWD_UDP_INFO = 231;
    public static final int OAM_MSG_START_BAND_SCAN = 232;
    public static final int OAM_MSG_FAN_AUTO_CFG = 233;
    public static final int OAM_MSG_GET_GPS_CFG = 236;
    public static final int OAM_MSG_CFG_PA_TRX=237;
    public static final int OAM_MSG_SET_LIC_INFO = 239;
    public static final int OAM_MSG_I2C_RW = 240;
    public static final int OAM_MSG_RW_USER_DATA=242;

    /********************************************************************************/

    public class FrameType {
        public static final int FRAME_TYPE_TDD_CFG_2D5MS = 0; // tdd：默认2.5ms dddsudddsu
        public static final int FRAME_TYPE_CMCC_TDD_CFG_5MS = 1; // 仅2.6G：dddddddsuu
        public static final int FRAME_TYPE_LTE_FDD_CFG = 0; // 4G fdd
        public static final int FRAME_TYPE_FDD_CFG = 2; // fdd
    }

    // 定位通道
    public class CellId {
        public static final int FIRST = 0;
        public static final int SECOND = 1;
        public static final int THIRD = 2;
        public static final int FOURTH = 3;
    }
    // 单双通道工作模式
    public class DualCell {
        public static final int SINGLE = 1;
        public static final int DUAL = 2;
    }
    public class TraceType {
        public static final int TRACE = 1;
        public static final int LTE_TRACE = 6;
        public static final int CATCH = 2;
        public static final int CONTROL = 3;
        public static final int STARTTRACE = 4;
        public static final int STARTCATCH = 5;

    }
}
