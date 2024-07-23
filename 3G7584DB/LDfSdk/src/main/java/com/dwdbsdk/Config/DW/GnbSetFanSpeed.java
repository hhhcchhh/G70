package com.dwdbsdk.Config.DW;

import com.dwdbsdk.Bean.DW.DWHeader;
import com.dwdbsdk.Util.DataUtil;

public class GnbSetFanSpeed {
    private final int u16MsgLength = DWHeader.headLength + 8;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public GnbSetFanSpeed(DWHeader header, int fan_id, int fan_speed) {
        mMsgIdx = 0;
        for (int i = 0; i < u16MsgLength; i++) {
            sendMsg[i] = 0;
        }

        byte[] headMsg = header.getHeaderMsg();
        for (int i = 0; i < headMsg.length; i++) {
            sendMsg[i] = headMsg[i];
            mMsgIdx++;
        }

        int2byte(fan_id);
        int2byte(fan_speed);

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
