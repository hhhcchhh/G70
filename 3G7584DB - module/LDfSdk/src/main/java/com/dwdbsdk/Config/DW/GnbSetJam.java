/*
typedef struct {
	int sync_header;
	int msg_type;          		//UI_2_gNB_OAM_MSG(in)
	int cmd_type;             	//OAM_MSG_SET_JAM_ARFCN(in)
	int cmd_param;

    int index;
    int enable;
	int jam_arfcn;
	int sync_footer;
} oam_jam_cfg_t;
* */
package com.dwdbsdk.Config.DW;

import com.dwdbsdk.Bean.DW.DWHeader;
import com.dwdbsdk.Util.DataUtil;

public class GnbSetJam {
    private final int u16MsgLength = DWHeader.headLength + 12;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public GnbSetJam(DWHeader header, int cell_id, int enable, int arfcn) {
        mMsgIdx = 0;
        for (int i = 0; i < u16MsgLength; i++) {
            sendMsg[i] = 0;
        }

        byte[] headMsg = header.getHeaderMsg();
        for (int i = 0; i < headMsg.length; i++) {
            sendMsg[i] = headMsg[i];
            mMsgIdx++;
        }

        int2byte(cell_id);
        int2byte(enable);
        int2byte(arfcn);

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
