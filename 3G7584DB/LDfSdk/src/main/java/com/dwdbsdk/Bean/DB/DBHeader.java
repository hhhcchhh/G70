package com.dwdbsdk.Bean.DB;

import com.dwdbsdk.Util.DataUtil;

/**
 * typedef struct {
 * int msg_header;  //0x5AA51111
 * int msgSn; // 序列号，递增，确认是否有丢消息
 * int msg_len;  //sizeof bt_msg_xxx_t
 * int msg_type;
 * <p>
 * int msg_footer;  //0x55AA3333
 * } bt_msg_glb_t;
 */

public class DBHeader {
    public static int headLength = 20; // 含数据头尾

    public DBHeader() {
    }

    public static byte[] getHeader(int msg_len, int msg_type) {
        byte[] headerMsg = new byte[16]; // 数据头统一长度：16
        headerMsg[0] = 0x11;
        headerMsg[1] = 0x11;
        headerMsg[2] = (byte) 0xA5;
        headerMsg[3] = (byte) 0x5A; /* 0x5AA51111 */
        int idx = 4;
        byte[] data = DataUtil.intToBytes(MsgSn.build().getMsgSn());
        for (byte datum : data) {
            headerMsg[idx] = datum;
            idx++;
        }
        data = DataUtil.intToBytes(msg_len);
        for (byte datum : data) {
            headerMsg[idx] = datum;
            idx++;
        }
        data = DataUtil.intToBytes(msg_type);
        for (byte datum : data) {
            headerMsg[idx] = datum;
            idx++;
        }
        return headerMsg;
    }

    public static byte[] getFooter() {
        byte[] footerMsg = new byte[4]; // 数据头统一长度：4
        footerMsg[0] = 0x33;
        footerMsg[1] = 0x33;
        footerMsg[2] = (byte) 0xA5;
        footerMsg[3] = (byte) 0x5A; /* 0x5AA53333 */
        return footerMsg;
    }
}
