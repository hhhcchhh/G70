/**
 * 基带板参数配置
 * OAM_MSG_SET_GPIO_MODE = 207
 * 此命令放在开启或结束定位之后执行
 * 通道一：前4个IO口控制
 * 通道二：后4个IP口控制
 * typedef struct {
 *      int sync_header;
 *      int msg_type;         			//UI_2_gNB_OAM_MSG
 *      int cmd_type;            		//0AM MSG GET GPIO MODE(out)，OAM MSG SET GPIO MODE(in)
 *      int cmd_param;
 *      int gpio_mode[EXT_GPIO_CNT]; 	//0-no change,1-static low.2-static high, 3-tx low active, 4-tx high active, 5-rx low active, 6-rx high_active
 *      int sync_footer;
 } oam_gpio_cfg_t;		//in&out
 */
package com.dwdbsdk.Config.DW;

import com.dwdbsdk.Bean.DW.DWHeader;
import com.dwdbsdk.Bean.PaBean;
import com.dwdbsdk.Util.DataUtil;

public class TFGnbGpioCfg {
    public int u16MsgLength = DWHeader.headLength + 24 * 4;
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public TFGnbGpioCfg(DWHeader header) {
        mMsgIdx = 0;
        for (int i = 0; i < u16MsgLength; i++) {
            sendMsg[i] = 0;
        }

        byte[] headMsg = header.getHeaderMsg();
        for (int i = 0; i < headMsg.length; i++) {
            sendMsg[i] = headMsg[i];
            mMsgIdx++;
        }
        // 以下顺序不能改变
        int2byte(PaBean.build().getGpio1());
        int2byte(PaBean.build().getGpio2());
        int2byte(PaBean.build().getGpio3());
        int2byte(PaBean.build().getGpio4());
        int2byte(PaBean.build().getGpio5());
        int2byte(PaBean.build().getGpio6());
        int2byte(PaBean.build().getGpio7());
        int2byte(PaBean.build().getGpio8());
        int2byte(PaBean.build().getGpio9());
        int2byte(PaBean.build().getGpio10());
        int2byte(PaBean.build().getGpio11());
        int2byte(PaBean.build().getGpio12());
        int2byte(PaBean.build().getGpio13());
        int2byte(PaBean.build().getGpio14());
        int2byte(PaBean.build().getGpio15());
        int2byte(PaBean.build().getGpio16());
        int2byte(PaBean.build().getGpio17());
        int2byte(PaBean.build().getGpio18());
        int2byte(PaBean.build().getGpio19());
        int2byte(PaBean.build().getGpio20());
        int2byte(PaBean.build().getGpio21());
        int2byte(PaBean.build().getGpio22());
        int2byte(PaBean.build().getGpio23());
        int2byte(PaBean.build().getGpio24());

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

    public byte[] getMsg() {
        return sendMsg;
    }
}
