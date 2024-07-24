package com.dwdbsdk.Config.DW;

import com.dwdbsdk.Bean.DW.DWHeader;
import com.dwdbsdk.Util.DataUtil;

/**
 typedef struct {
 int sync_header;
 int msg_type;          	//UI_2_gNB_SET_PLMN = 60
 int cmd_type;
 int cmd_param;
 int cell_id;
 int plmn_num;
 int plmn[12];
 int sync_footer;
 } new_oam_msg2_t
 */


public class GnbResetPlmnCfg {
    private final int u16MsgLength = DWHeader.headLength  + 12 * 8 + 8;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public GnbResetPlmnCfg(DWHeader header, int cell_id, String[] plmns) {
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
        int2byte(plmns.length);

        for (String plmn : plmns) {
            int MCC = 0, MNC = 0;
            if (plmn != null && plmn.length() > 4) {
                MCC = Integer.parseInt(plmn.substring(0, 3));
                MNC = Integer.parseInt(plmn.length() > 5 ? "1" + plmn.substring(3) : plmn.substring(3));
            }
            int2byte(MCC);
            int2byte(MNC);
        }

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

    public byte[] getMsg() {
        return sendMsg;
    }
}
