package com.nr.Socket.MessageControl;

public class MessageProtocol {
    public static final int REMOTE_PORT = 16593;   // 基带板在此监听以获取热点IP地址
    public static final int LOCAL_PORT = 16595; // 本地监听
    public static final int LOCAL_UDP_PORT = 8001; // 本地udp广播监听
    public static class SOCKET {
        public final static int STATE_DISCONNECT = 100;
        public final static int STATE_CONNECTING = 101;
        public final static int STATE_CONNECTED = 103;
        public final static int STATE_OUT_TIME = 106;
        public final static int STATE_NO_RESPONSE = 107;

    }


}
