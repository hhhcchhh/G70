/**
 * 基带板时间配置同步LOG时间用
 * typedef struct {
 * int sync_header;
 * int msg_type;    			//UI_2_gNB_OAM_MSG
 * int cmd_type;            	//UI_2_gNB_SET_TIME
 * int cmd_param;
 * // OAM_STR_MAX = 32
 * char adapter_name[OAM_STR_MAX];
 * <p>
 * int sync_footer;
 * } oam_set_time_t; //in
 */
package com.dwdbsdk.Config.DW;

import android.text.TextUtils;

import com.dwdbsdk.Bean.DW.DWHeader;
import com.dwdbsdk.Util.DataUtil;

public class GnbSetUserData {
    public static int u16MsgLength = DWHeader.headLength + 4 + 4 + 256;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public GnbSetUserData(DWHeader header, int RW, int index, String user_data) {
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
        int2byte(RW); //read = 0,write = 1
        int2byte(index);
        if (!TextUtils.isEmpty(user_data)) {
            handleUserData(user_data);
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

    private void handleUserData(String user_data) {
        byte[] bytes = user_data.getBytes();
        for (byte aByte : bytes) {
            sendMsg[mMsgIdx] = aByte;
            mMsgIdx++;
        }
        if (bytes.length < 256) {
            int offset = 256 - bytes.length;
            mMsgIdx += offset;
        }
    }

    public byte[] getMsg() {
        return sendMsg;
    }
}
