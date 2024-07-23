package com.dwdbsdk.Config.DB;

import com.dwdbsdk.Bean.DB.DBHeader;
import com.dwdbsdk.Util.DataUtil;

/**
 * typedef struct {
 * int msg_header;          //HEADER_MAGIC
 * int msg_sn;            //serial num
 * int msg_len;           //sizeof bt_msg_xxx_t
 * int msg_type;           //BT_MSG_GPIO_CFG(65)
 * <p>
 * int param;
 * int gpio_cfg[6];        //0-no change, 1-low, 2-high
 * <p>
 * int msg_footer;          //FOOTER_MAGIC
 * } bt_msg_gpio_cfg_t; //in
 */

public class MsgGpioCfg {
    private final int u16MsgLength = DBHeader.headLength + 4 * 7;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public MsgGpioCfg(int msg_type, int gpio_1, int gpio_2, int gpio_3, int gpio_4, int gpio_5, int gpio_6) {
        for (int i = 0; i < u16MsgLength; i++) {
            sendMsg[i] = 0;
        }
        mMsgIdx = 0;
        byte[] headMsg = DBHeader.getHeader(u16MsgLength, msg_type);
        for (byte b : headMsg) {
            sendMsg[mMsgIdx] = b;
            mMsgIdx++;
        }
        // 此处顺序不能变
        int2byte(0); // param
        int2byte(gpio_1);
        int2byte(gpio_2);
        int2byte(gpio_3);
        int2byte(gpio_4);
        int2byte(gpio_5);
        int2byte(gpio_6);

        mMsgIdx = u16MsgLength - 4;
        byte[] footerMsg = DBHeader.getFooter();
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
