/**
 * 设置基站测量UE 配置（上位机下发）
 * 配置测量UE 的工作模式。
 * <p>
 * typedef struct {
 * int sync_header;
 * int msg_type;    			//UI_2_gNB_OAM_MSG
 * int cmd_type;             	//UI_2_gNB_SET_BLACK_UE_LIST
 * int cmd_param;
 * <p>
 * int cell_id;
 * int imsi_num;             // 本次设置黑名单个数
 * ue_id_t ue_id[MAX_BLACK_IMSI_NUM]; // max black list 100 UE. 填充实际imsi个数，后面填sync_footer。
 * <p>
 * int sync_footer;
 * } oam_black_list_setting_t; //in
 */
package com.dwdbsdk.Config.DW;


import com.dwdbsdk.Bean.DW.DWHeader;
import com.dwdbsdk.Bean.DW.DWProtocol;
import com.dwdbsdk.Bean.UeidBean;
import com.dwdbsdk.Util.DataUtil;

import java.util.List;

public class GnbBlackList {
    private final byte[] sendMsg;// = new int[u16MsgLength];
    private int mMsgIdx;

    public GnbBlackList(DWHeader header, int cell_id, int imsiNum, List<UeidBean> ueid) {
        /* 定义消息的长度 */
        // = Header.headLength + 4 + DWProtocol.MAX_IMSI_LEN * DWProtocol.MAX_BLACK_IMSI_NUM *2;
        int u16MsgLength = DWHeader.headLength + 8 + DWProtocol.MAX_IMSI_LEN * imsiNum * 2;
        sendMsg = new byte[u16MsgLength];

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
        int2byte(imsiNum);
        handleImsi(imsiNum, ueid);

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

    private void handleImsi(int imsiNum, List<UeidBean> ueid) {
        int idx_start, idx_end;
        for (int i = 0; i < imsiNum; i++) {
            String imsi = ueid.get(i).getImsi();
            idx_start = 0;
            idx_end = 1;
            while (idx_end <= imsi.length()) {
                String a = "3" + imsi.substring(idx_start, idx_end);
                sendMsg[mMsgIdx] = Byte.valueOf(a, 16);
                mMsgIdx++;
                idx_start++;
                idx_end++;
            }
            sendMsg[mMsgIdx] = 0x00;
            mMsgIdx++;
            // guti
            for (int j = 0; j < DWProtocol.MAX_IMSI_LEN; j++) {
                sendMsg[mMsgIdx] = 0x00;
                mMsgIdx++;
            }
        }
    }

    public byte[] getMsg() {
        return sendMsg;
    }
}
