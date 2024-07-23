package com.dwdbsdk.MessageControl;

public class MessageProtocol {
    public static class STATE {
        public final static int STATE_DISCONNECT = 100; // 未连接、连接已断开
        public final static int STATE_CONNECTING = 101; // 连接中
        public static final int STATE_LISTEN = 102;     // ble 等待连接
        public final static int STATE_CONNECTED = 103;  // 已连接
        public static final int STATE_CONNECT_FAIL = 104;  // ble 连接失败
        public static final int STATE_LOST = 105;  // ble 连接丢失
        public final static int STATE_OUT_TIME = 106; // 连接超时
        public final static int STATE_NO_RESPONSE = 107; // 连接无响应

    }
}
