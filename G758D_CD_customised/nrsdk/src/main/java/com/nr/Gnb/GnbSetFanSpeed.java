/*
* typedef struct {
 int sync_header;
 int msg_type;           //UI_2_gNB_OAM_MSG(in)
 int cmd_type;             //OAM_MSG_SET_FAN_SPEED
 int cmd_param;

 int fan_id;           //0：风扇 ID
 int fan_speed;         //0-100  风扇功率增益百分比，当前设置 值以及转速在心跳数据中查询。
 int sync_footer;
} oam_rx_gain_t;
* */
package com.nr.Gnb;

import com.nr.Gnb.Bean.Header;
import com.nr.Util.DataUtil;

public class GnbSetFanSpeed {
    public static int u16MsgLength = Header.headLength + 8;/* 定义消息的长度 */
    private byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public GnbSetFanSpeed(Header header, int fan_id, int fan_speed) {
        mMsgIdx = 0;
        for (int i = 0; i < u16MsgLength; i++) {
            sendMsg[i] = 0;
        }

        byte headMsg[] = header.getHeaderMsg();
        for (int i = 0; i < headMsg.length; i++) {
            sendMsg[i] = headMsg[i];
            mMsgIdx++;
        }

        int2byte(fan_id);
        int2byte(fan_speed);

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
