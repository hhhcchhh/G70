/**
 * 设置单板WIFI工作模式、名称、密码
 * <p>
 * typedef struct {
 * int sync_header;
 * int msg_type;    			//UI_2_gNB_OAM_MSG
 * int cmd_type;            	//UI_2_gNB_WIFI_CFG
 * int cmd_param;
 * <p>
 * char ssid[OAM_STR_MAX];
 * char passwd[OAM_STR_MAX];
 * <p>
 * int sync_footer;
 * } oam_wifi_cfg_t; //in
 */
package com.dwdbsdk.Config.DW;

import com.dwdbsdk.Bean.DW.DWHeader;
import com.dwdbsdk.Bean.DW.DWProtocol;

public class GnbWifiCfg {
    private final int u16MsgLength = DWHeader.headLength + 2 * DWProtocol.OAM_STR_MAX;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public GnbWifiCfg(DWHeader header, String ssid, String passwd) {
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
        handleSsid(ssid);
        handlePasswd(passwd);

        mMsgIdx = u16MsgLength - 4;
        byte[] footerMsg = header.getFooterMsg();
        for (byte b : footerMsg) {
            sendMsg[mMsgIdx] = b;
            mMsgIdx++;
        }
    }

    private void handleSsid(String ssid) {
        byte[] bytes = ssid.getBytes();
        for (byte aByte : bytes) {
            sendMsg[mMsgIdx] = aByte;
            mMsgIdx++;
        }
        if (bytes.length < DWProtocol.OAM_STR_MAX) {
            int offset = DWProtocol.OAM_STR_MAX - bytes.length;
            mMsgIdx += offset;
        }
    }

    private void handlePasswd(String passwd) {
        byte[] bytes = passwd.getBytes();
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
