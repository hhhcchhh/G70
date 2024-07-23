/*
typedef struct {
	int sync_header;
	int msg_type;          		//UI_2_gNB_OAM_MSG
	int cmd_type;             	//OAM_MSG_SET_GPS_CFG(in)
	int cmd_param;

	int gnss_select;			//0-gps+beidou, 1-gps, 2-beidou
	int latitude;				//real*1000, 0 auto detect
	int longitude;				//real*1000, 0 auto detect
	int sync_footer;
} oam_cfg_gps_t;
* */
package com.nr.Gnb;

import com.nr.Gnb.Bean.Header;
import com.nr.Util.DataUtil;

public class GnbSetGps {
    public static int u16MsgLength = Header.headLength + 12;/* 定义消息的长度 */
    private byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public GnbSetGps(Header header, int gnss_select, int latitude, int longitude) {
        mMsgIdx = 0;
        for (int i = 0; i < u16MsgLength; i++) {
            sendMsg[i] = 0;
        }

        byte headMsg[] = header.getHeaderMsg();
        for (int i = 0; i < headMsg.length; i++) {
            sendMsg[i] = headMsg[i];
            mMsgIdx++;
        }

        int2byte(gnss_select);
        int2byte(latitude);
        int2byte(longitude);

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
