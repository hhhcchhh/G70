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
package com.nr.Gnb;

import com.nr.Gnb.Bean.Header;
import com.nr.Util.DataUtil;

public class GnbSetJam {
    public static int u16MsgLength = Header.headLength + 12;/* 定义消息的长度 */
    private byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public GnbSetJam(Header header, int cell_id, int enable, int arfcn) {
        mMsgIdx = 0;
        for (int i = 0; i < u16MsgLength; i++) {
            sendMsg[i] = 0;
        }

        byte headMsg[] = header.getHeaderMsg();
        for (int i = 0; i < headMsg.length; i++) {
            sendMsg[i] = headMsg[i];
            mMsgIdx++;
        }

        int2byte(cell_id);
        int2byte(enable);
        int2byte(arfcn);

        byte footerMsg[] = header.getFooterMsg();
        for (int i = 0; i < footerMsg.length; i++) {
            sendMsg[mMsgIdx] = footerMsg[i];
            mMsgIdx++;
        }
    }

    private void int2byte(int idata) {
        byte[] data = DataUtil.intToBytes(idata);
        for (int i = 0; i < data.length; i++) {
            sendMsg[mMsgIdx] = data[i];
            mMsgIdx++;
        }
    }

    public byte[] getMsg() {
        return sendMsg;
    }
}
