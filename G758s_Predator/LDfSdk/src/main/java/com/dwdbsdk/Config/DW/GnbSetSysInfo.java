/**
 * 单板IP及路由器IP及端口配置
 * typedef struct {
 * int sync_header;
 * int msg_type;          		//UI_2_gNB_OAM_MSG
 * int cmd_type;             	//OAM_MSG_SET_SYS_INFO, OAM_MSG_GET_SYS_INFO
 * int cmd_param;
 * <p>
 * char dev_name[OAM_STR_MAX];   // OAM_MSG_SET_SYS_INFO: 仅可配置 dev_name
 * char license[256];
 * int sync_footer;
 * } oam_cfg_sysinfo_t; 	//in&out
 */
package com.dwdbsdk.Config.DW;

import com.dwdbsdk.Bean.DW.DWHeader;
import com.dwdbsdk.Bean.DW.DWProtocol;

public class GnbSetSysInfo {

    private final int u16MsgLength = DWHeader.headLength + DWProtocol.OAM_STR_MAX + 256;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public GnbSetSysInfo(DWHeader header, String dev_name) {
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
        handleStr(dev_name);

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
