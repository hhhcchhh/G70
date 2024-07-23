package com.dwdbsdk.Config.DB;

import com.dwdbsdk.Bean.DB.DBHeader;
import com.dwdbsdk.Util.DataUtil;

/**
 * typedef struct {
 * int msg_header;						//HEADER_MAGIC
 * int msg_sn;							//serial num
 * int msg_len;						//sizeof bt_msg_xxx_t
 * int msg_type;						//GR_MSG_WIFI_CFG
 * int param;
 * char ssid_name[32];
 * char pwd[32];
 * int msg_footer;						//FOOTER_MAGIC
 * } bt_msg_wifi_cfg_t;
 */

public class MsgWifiCfg {
    // char ssid_name[32];
    // char pwd[32];
    private final int u16MsgLength = DBHeader.headLength + 4 + 32 * 2;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public MsgWifiCfg(int msg_type, String ssid, String pwd) {
        for (int i = 0; i < u16MsgLength; i++) {
            sendMsg[i] = 0;
        }
        mMsgIdx = 0;
        byte[] headMsg = DBHeader.getHeader(u16MsgLength, msg_type);
        for (byte b : headMsg) {
            sendMsg[mMsgIdx] = b;
            mMsgIdx++;
        }
        // 此处顺序不能变
        int2byte(0); // param
        str2byte(ssid);
        mMsgIdx = 52; // 16 + 4 + 32
        str2byte(pwd);

        mMsgIdx = u16MsgLength - 4;
        byte[] footerMsg = DBHeader.getFooter();
        for (byte b : footerMsg) {
            sendMsg[mMsgIdx] = b;
            mMsgIdx++;
        }
    }

    private void str2byte(String content) {
        byte[] data = content.getBytes();
        for (byte datum : data) {
            sendMsg[mMsgIdx] = datum;
            mMsgIdx++;
        }
    }

    private void int2byte(int idata) {
        byte[] data = DataUtil.intToBytes(idata);
        for (byte datum : data) {
            sendMsg[mMsgIdx] = datum;
            mMsgIdx++;
        }
    }

    public byte[] getMsg() {
        return sendMsg;
    }
}
