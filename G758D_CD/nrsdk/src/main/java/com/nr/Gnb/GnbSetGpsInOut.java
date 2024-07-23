/*
1、打开对应频点功放
2、启动GPS帧偏测量
typedef struct {
	int sync_header;
	int msg_type;         		//UI_2_gNB_OAM_MSG
	int cmd_type;            	//OAM_MSG_START_TD_MEASURE
	int cmd_param;

	int cell_id;    // 可以固定为 cell_id = 0
    int swap_rf;    // 根据功放接法对应配置，0：TX1 RX1, 1: TX2, RX2
	int arfcn;
	int kssb;
	int rb_offset;
	int sync_footer;
} oam_tdm_cfg_t;
* */
package com.nr.Gnb;

import com.nr.Gnb.Bean.Header;
import com.nr.Util.DataUtil;

public class GnbSetGpsInOut {
    public static int u16MsgLength = Header.headLength + 8;/* 定义消息的长度 */
    private byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public GnbSetGpsInOut(Header header, int out_gpio_idx, int in_gpio_idx) {
        mMsgIdx = 0;
        for (int i = 0; i < u16MsgLength; i++) {
            sendMsg[i] = 0;
        }

        byte headMsg[] = header.getHeaderMsg();
        for (int i = 0; i < headMsg.length; i++) {
            sendMsg[i] = headMsg[i];
            mMsgIdx++;
        }

        int2byte(out_gpio_idx);
        int2byte(in_gpio_idx);

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
