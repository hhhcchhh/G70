/**
 * 设置单板工作模式
 * <p>
 * OAM_MSG_SET_DUAL_CELL = 212;  //
 * typedef struct {
 * int sync_header;
 * int msg_type;            //UI_2_gNB_OAM_MSG
 * int cmd_type;              //OAM_MSG_SET_DUAL_CELL
 * int cmd_param;
 * <p>
 * int dual_cell;    //1-singel cell, 2-dual cell(default)
 * <p>
 * int sync_footer;
 * } oam_cfg_dual_cell_t;  //in
 */
package com.dwdbsdk.Config.DW;

import com.dwdbsdk.Bean.DW.DWHeader;
import com.dwdbsdk.Util.DataUtil;

public class GnbDualCell {
    public int u16MsgLength = DWHeader.headLength + 4;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public GnbDualCell(DWHeader header, int dual_cell) {
        mMsgIdx = 0;
        for (int i = 0; i < u16MsgLength; i++) {
            sendMsg[i] = 0;
        }

        byte[] headMsg = header.getHeaderMsg();
        for (int i = 0; i < headMsg.length; i++) {
            sendMsg[i] = headMsg[i];
            mMsgIdx++;
        }

        int2byte(dual_cell);

        byte[] footerMsg = header.getFooterMsg();
        for (byte b : footerMsg) {
            sendMsg[mMsgIdx] = b;
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
