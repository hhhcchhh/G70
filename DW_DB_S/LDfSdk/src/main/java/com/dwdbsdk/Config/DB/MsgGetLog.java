/**
 * typedef struct {
 * int msg_header;						//HEADER_MAGIC
 * int msg_sn;							//serial num
 * int msg_len;						//sizeof bt_msg_xxx_t
 * int msg_type;						//GR_MSG_GET_LOG
 * int param;
 * int server_type;					//0-scp
 * char log_name[64];
 * int msg_footer;						//FOOTER_MAGIC
 * } bt_msg_get_log_t;
 */
package com.dwdbsdk.Config.DB;

import com.dwdbsdk.Bean.DB.DBHeader;
import com.dwdbsdk.Util.DataUtil;

public class MsgGetLog {
    //char log_name[64];
    private final int u16MsgLength = DBHeader.headLength + 64 + 8;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx = 0;

    public MsgGetLog(int msg_type, int server_type, String log_name) {
        for (int i = 0; i < u16MsgLength; i++) {
            sendMsg[i] = 0;
        }
        mMsgIdx = 0;
        byte[] headMsg = DBHeader.getHeader(u16MsgLength, msg_type);
        for (int i = 0; i < headMsg.length; i++) {
            sendMsg[i] = headMsg[i];
            mMsgIdx++;
        }
        // 此处顺序不能变
        int2byte(0);
        int2byte(server_type);
        handleLogName(log_name);

        mMsgIdx = u16MsgLength - 4;
        byte[] footerMsg = DBHeader.getFooter();
        for (byte b : footerMsg) {
            sendMsg[mMsgIdx] = b;
            mMsgIdx++;
        }
    }

    private void handleLogName(String log_name) {
        byte[] bytes = log_name.getBytes();
        for (byte aByte : bytes) {
            sendMsg[mMsgIdx] = aByte;
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
