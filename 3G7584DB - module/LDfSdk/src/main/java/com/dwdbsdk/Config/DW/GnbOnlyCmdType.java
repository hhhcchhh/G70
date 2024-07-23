/**
 * 配置不带参数的指令
 * //UI_2_gNB_HEART_BEAT
 * //UI_2_gNB_DELETE_OP_LOG_REQ
 * //UI_2_gNB_REBOOT_gNB
 * //CGI_MSG_GET_METH_CFG
 * //UI_2_gNB_QUERY_gNB_VERSION
 * typedef struct {
 * int sync_header;
 * int msg_type;          	//UI_2_gNB_OAM_MSG
 * int cmd_type;
 * int cmd_param;
 * <p>
 * int sync_footer;
 * } new_oam_msg_t; //in
 */
package com.dwdbsdk.Config.DW;

import com.dwdbsdk.Bean.DW.DWHeader;

public class GnbOnlyCmdType {
    private final int u16MsgLength = DWHeader.headLength;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];

    public GnbOnlyCmdType(DWHeader header) {
        int mMsgIdx = 0;
        for (int i = 0; i < u16MsgLength; i++) {
            sendMsg[i] = 0;
        }

        byte[] headMsg = header.getHeaderMsg();
        for (int i = 0; i < headMsg.length; i++) {
            sendMsg[i] = headMsg[i];
            mMsgIdx++;
        }

        byte[] footerMsg = header.getFooterMsg();
        for (byte b : footerMsg) {
            sendMsg[mMsgIdx] = b;
            mMsgIdx++;
        }
    }

    public byte[] getMsg() {
        return sendMsg;
    }
}
