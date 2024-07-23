package com.dwdbsdk.Config.DW;

import com.dwdbsdk.Bean.DW.DWHeader;
import com.dwdbsdk.Bean.DW.DWProtocol;
import com.dwdbsdk.Util.DataUtil;

public class GnbSetForwardUdpMsg {
    private final int u16MsgLength = DWHeader.headLength + 2 * DWProtocol.OAM_STR_MAX + 4;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public GnbSetForwardUdpMsg(DWHeader header, String dst_ip, int dst_port, String fwd_info) {
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
        handleStr(dst_ip);
        int2byte(dst_port);
        handleStr(fwd_info);

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

    private void handleStr(String meth_ip) {
        byte[] bytes = meth_ip.getBytes();
        for (byte aByte : bytes) {
            sendMsg[mMsgIdx] = aByte;
            mMsgIdx++;
        }
        if (bytes.length < DWProtocol.OAM_STR_MAX) {
            int offset = DWProtocol.OAM_STR_MAX - bytes.length;
            mMsgIdx += offset;
        }
    }

    public byte[] getMsg() {
        return sendMsg;
    }
}
