/**
 * 单板IP及路由器IP及端口配置
 * <p>
 * typedef struct {
 * int sync_header;
 * int msg_type;          		//UI_2_gNB_OAM_MSG
 * int cmd_type;             	//CGI_MSG_SET_METH_CFG
 * int cmd_param;
 * <p>
 * char meth_ip[OAM_STR_MAX]; // "192.168.1.33"
 * char meth_mask[OAM_STR_MAX]; // "255.255.255.0"
 * char meth_gw[OAM_STR_MAX]; // "192.168.1.1"
 * <p>
 * int sync_footer;
 * } oam_set_meth_t; 	//in
 */
package com.dwdbsdk.Config.DW;

import com.dwdbsdk.Bean.DW.DWHeader;
import com.dwdbsdk.Bean.DW.DWProtocol;

public class GnbMethIpCfg {

    private final int u16MsgLength = DWHeader.headLength + 4 * DWProtocol.OAM_STR_MAX;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public GnbMethIpCfg(DWHeader header, String meth_ip, String meth_mask, String meth_gw, String meth_mac) {
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
        handleStr(meth_ip);
        handleStr(meth_mask);
        handleStr(meth_gw);
        handleStr(meth_mac);

        mMsgIdx = u16MsgLength - 4;
        byte[] footerMsg = header.getFooterMsg();
        for (byte b : footerMsg) {
            sendMsg[mMsgIdx] = b;
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
