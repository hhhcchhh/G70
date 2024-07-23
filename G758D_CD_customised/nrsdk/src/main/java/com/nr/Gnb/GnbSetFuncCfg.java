/*
typedef struct {
        int sync_header;
        int msg_type;                          //UI_2_gNB_OAM_MSG(in)
        int cmd_type;                     //OAM_MSG_SET_FUNC_CFG(in)
        int cmd_param;

        int func_type;              //PDT_TYPE(1)
        int cfg_value;
        int ext_value[16];
        char ext_str[128];
        int sync_footer;
} oam_cfg_func_t;
* */
package com.nr.Gnb;

import com.nr.Gnb.Bean.Header;
import com.nr.Util.DataUtil;

public class GnbSetFuncCfg {
    public static int u16MsgLength = Header.headLength + 18 * 4 + 128;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public GnbSetFuncCfg(Header header, int func_type, int cfg_value, String cfg_str) {
        mMsgIdx = 0;
        for (int i = 0; i < u16MsgLength; i++) {
            sendMsg[i] = 0;
        }

        byte[] headMsg = header.getHeaderMsg();
        for (int i = 0; i < headMsg.length; i++) {
            sendMsg[i] = headMsg[i];
            mMsgIdx++;
        }

        int2byte(func_type);
        int2byte(cfg_value);

        mMsgIdx = u16MsgLength - 4 - 128;
        handleStr(cfg_str);

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

    private void handleStr(String cfg_str) {
        byte[] bytes = cfg_str.getBytes();
        for (byte aByte : bytes) {
            sendMsg[mMsgIdx] = aByte;
            mMsgIdx++;
        }
    }

    public byte[] getMsg() {
        return sendMsg;
    }
}
