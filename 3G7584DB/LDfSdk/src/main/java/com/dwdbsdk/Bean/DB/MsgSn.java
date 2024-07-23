package com.dwdbsdk.Bean.DB;

/**
 * int msgSn; // 序列号，递增，确认是否有丢消息
 */

public class MsgSn {
    private static MsgSn instance;

    public static MsgSn build() {
        synchronized (MsgSn.class) {
            if (instance == null) {
                instance = new MsgSn();
            }
        }
        return instance;
    }

    public MsgSn() {

    }

    public void init() {
        msgSn = 0;
    }

    public int getMsgSn() {
        msgSn++;
        if (msgSn >= Integer.MAX_VALUE) {
            msgSn = 0;
        }
        return msgSn;
    }

    public int msgSn; // 序列号，递增，确认是否有丢消息
}
