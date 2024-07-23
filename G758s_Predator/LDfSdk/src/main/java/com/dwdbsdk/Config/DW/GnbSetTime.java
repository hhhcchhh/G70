/**
 * 基带板时间配置同步LOG时间用
 * typedef struct {
 * int sync_header;
 * int msg_type;    			//UI_2_gNB_OAM_MSG
 * int cmd_type;            	//UI_2_gNB_SET_TIME
 * int cmd_param;
 * // OAM_STR_MAX = 32
 * char date_time[OAM_STR_MAX];      // 设置单板时间。数据格式为"2006-4-20 20:30:30"
 * <p>
 * int sync_footer;
 * } oam_set_time_t; //in
 */
package com.dwdbsdk.Config.DW;

import com.dwdbsdk.Bean.DW.DWHeader;
import com.dwdbsdk.Bean.DW.DWProtocol;

public class GnbSetTime {
    private final int u16MsgLength = DWHeader.headLength + DWProtocol.OAM_STR_MAX;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public GnbSetTime(DWHeader header, String date_time) {
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
        handleDateTime(date_time);

        mMsgIdx = u16MsgLength - 4;
        byte[] footerMsg = header.getFooterMsg();
        for (byte b : footerMsg) {
            sendMsg[mMsgIdx] = b;
            mMsgIdx++;
        }
    }

    private void handleDateTime(String date_time) {
        int idx_start = 0, idx_end = 1;
        String a;
        while (idx_end <= date_time.length()) {
            String tmp = date_time.substring(idx_start, idx_end);
            switch (tmp) {
                case ".":
                    a = "2e";
                    break;
                case "-":
                    a = "2d";
                    break;
                case ":":
                    a = "3a";
                    break;
                case " ":
                    a = "20";
                    break;
                default:
                    a = "3" + tmp;
                    break;
            }
            sendMsg[mMsgIdx] = Byte.valueOf(a, 16);
            mMsgIdx++;
            idx_start++;
            idx_end++;
        }
        sendMsg[mMsgIdx] = 0;
        mMsgIdx++;
    }

    public byte[] getMsg() {
        return sendMsg;
    }
}
