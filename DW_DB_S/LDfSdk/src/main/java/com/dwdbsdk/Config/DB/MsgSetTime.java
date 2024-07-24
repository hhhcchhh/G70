package com.dwdbsdk.Config.DB;

import com.dwdbsdk.Bean.DB.DBHeader;
import com.dwdbsdk.Util.DataUtil;

/**
 * typedef struct {
 * int msg_header;						//HEADER_MAGIC
 * int msg_sn;							//serial num
 * int msg_len;						//sizeof bt_msg_xxx_t
 * int msg_type;						//GR_MSG_SET_TIME
 * int param;
 * char time_str[32];
 * int msg_footer;						//FOOTER_MAGIC
 * } bt_msg_set_time_t;
 */

public class MsgSetTime {
    // char time_str[32];
    //private String date_time; // 32个字节 设置单板时间。数据格式为"2006-4-20 20:30:30"

    private final int u16MsgLength = DBHeader.headLength + 36;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public MsgSetTime(int msg_type, String date_time) {
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
        str2byte(date_time);

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
