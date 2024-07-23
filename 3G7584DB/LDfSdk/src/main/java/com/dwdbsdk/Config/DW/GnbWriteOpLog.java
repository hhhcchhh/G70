/**
 * typedef struct {
 * int sync_header;
 * int msg_type;    		//UI_2_gNB_OAM_MSG
 * int cmd_type;           //UI_2_gNB_WRITE_OP_RECORD
 * int cmd_param;
 * <p>
 * char record[256];        // 一行文本记录，不超过250字节: [time_stamp] operation record
 * <p>
 * int sync_footer;
 * } oam_record_write_t; //in
 */
package com.dwdbsdk.Config.DW;

import com.dwdbsdk.Bean.DW.DWHeader;

public class GnbWriteOpLog {
    private final int u16MsgLength = DWHeader.headLength + 256;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public GnbWriteOpLog(DWHeader header, String record) {
        mMsgIdx = 0;
        for (int i = 0; i < u16MsgLength; i++) {
            sendMsg[i] = 0;
        }

        byte[] headMsg = header.getHeaderMsg();
        for (int i = 0; i < headMsg.length; i++) {
            sendMsg[i] = headMsg[i];
            mMsgIdx++;
        }

        // 此处顺序不能变
        handleRecord(record);

        mMsgIdx = u16MsgLength - 4;
        byte[] footerMsg = header.getFooterMsg();
        for (byte b : footerMsg) {
            sendMsg[mMsgIdx] = b;
            mMsgIdx++;
        }
    }

    private void handleRecord(String record) {
        byte[] bytes = record.getBytes();
        for (byte aByte : bytes) {
            sendMsg[mMsgIdx] = aByte;
            mMsgIdx++;
        }
    }

    public byte[] getMsg() {
        return sendMsg;
    }
}
