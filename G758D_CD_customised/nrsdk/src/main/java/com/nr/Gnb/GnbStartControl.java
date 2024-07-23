/*
* typedef struct {
 int sync_header;
 int msg_type;           //UI_2_gNB_OAM_MSG(in)
 int cmd_type;             //UI_2_gNB_START_CONTROL(in)
 int cmd_param;

 int cell_id;
 int mode;              // 0:管控  1：压制  3：反管控
 int report_phone_type;;  // 1: report phone type in ue_report_t; 0: doing nothing
 int sync_footer;
} oam_rx_gain_t;
* */
package com.nr.Gnb;

import com.nr.Gnb.Bean.Header;
import com.nr.Util.DataUtil;

public class GnbStartControl {
    public static int u16MsgLength = Header.headLength + 3 * 4;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public GnbStartControl(Header header, int cell_id, int mode, int report_phone_type) {
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
        int2byte(mode);
        int2byte(report_phone_type);

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
