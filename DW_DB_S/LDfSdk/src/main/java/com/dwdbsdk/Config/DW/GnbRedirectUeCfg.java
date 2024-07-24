package com.dwdbsdk.Config.DW;

import com.dwdbsdk.Bean.DW.DWHeader;
import com.dwdbsdk.Bean.DW.DWProtocol;
import com.dwdbsdk.Util.DataUtil;

/**
 typedef struct {
 int sync_header;
 int msg_type;          	//UI_2_gNB_OAM_MSG
 int cmd_type;
 int cmd_result;
 int cell_id;
 int sync_footer;
 } new_oam_msg2_t
 */


public class GnbRedirectUeCfg {
    private final int u16MsgLength = DWHeader.headLength  + 28;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public GnbRedirectUeCfg(DWHeader header, int cell_id, String imsi, int redirect_flag, int redirect_arfcn) {
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
        int2byte(cell_id);
        handleStr(imsi);
        int2byte(redirect_flag);
        int2byte(redirect_arfcn);

        mMsgIdx = u16MsgLength - 4;
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

    private void handleStr(String imsi) {
        byte[] bytes = imsi.getBytes();
        for (byte aByte : bytes) {
            sendMsg[mMsgIdx] = aByte;
            mMsgIdx++;
        }
        if (bytes.length < DWProtocol.MAX_IMSI_LEN) {
            int offset = DWProtocol.MAX_IMSI_LEN - bytes.length;
            mMsgIdx += offset;
        }
    }

    public byte[] getMsg() {
        return sendMsg;
    }
}
