package com.dwdbsdk.Config.DB;

import com.dwdbsdk.Bean.DB.DBHeader;

/**
 * typedef struct {
 * int msg_header;						//HEADER_MAGIC
 * int msg_sn;							//serial num
 * int msg_len;						//sizeof bt_msg_xxx_t
 * int msg_type;						//GR_MSG_GET_VERSION
 * int param;
 * char ver_str[256];
 * int msg_footer;						//FOOTER_MAGIC
 * } bt_msg_get_version_t;
 */

public class MsgGetVersion {

    private final int u16MsgLength = DBHeader.headLength + 260;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];

    public MsgGetVersion(int msg_type) {
        for (int i = 0; i < u16MsgLength; i++) {
            sendMsg[i] = 0;
        }
        int mMsgIdx = 0;
        byte[] headMsg = DBHeader.getHeader(u16MsgLength, msg_type);
        for (byte b : headMsg) {
            sendMsg[mMsgIdx] = b;
            mMsgIdx++;
        }

        mMsgIdx = u16MsgLength - 4;
        byte[] footerMsg = DBHeader.getFooter();
        for (byte b : footerMsg) {
            sendMsg[mMsgIdx] = b;
            mMsgIdx++;
        }
    }

    public byte[] getMsg() {
        return sendMsg;
    }
}
