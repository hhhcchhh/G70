/**
 * 基带板参数配置
 * OAM_MSG_SET_GPIO_MODE = 207
 * 此命令放在开启或结束定位之后执行
 * 通道一：前4个IO口控制
 * 通道二：后4个IP口控制
 * typedef struct {
 *      int sync_header;
 *      int msg_type;         			//UI_2_gNB_OAM_MSG
 *      int cmd_type;            		//OAM_MSG_GET_GPIO_MODE, OAM_MSG_SET_GPIO_MODE
 *      int cmd_param;
 *      int gpio_mode[EXT_GPIO_CNT]; 	//0-no_change， 1-static_low, 2-static_high, 3-tdd(tx=low, rx=high), 4-tdd(tx=high, rx=low)
 *      int sync_footer;
 } oam_gpio_cfg_t;		//in&out
 */
package com.dwdbsdk.Config.DW;
import com.dwdbsdk.Bean.DW.DWHeader;
import com.dwdbsdk.Bean.DW.DWProtocol;
import com.dwdbsdk.Bean.PaBean;
import com.dwdbsdk.Util.DataUtil;

public class GnbGpioCfg {
    private final int u16MsgLength = DWHeader.headLength + DWProtocol.EXT_GPIO_CNT * 4;
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public GnbGpioCfg(DWHeader header) {
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
        int2byte(PaBean.build().getGpio1_en1());
        int2byte(PaBean.build().getGpio2_en2());
        int2byte(PaBean.build().getGpio3_bs3());
        int2byte(PaBean.build().getGpio4_tddSw1());
        int2byte(PaBean.build().getGpio5_bs1());
        int2byte(PaBean.build().getGpio6_bs2());
        int2byte(PaBean.build().getGpio7());
        int2byte(PaBean.build().getGpio8_tddSw2());

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
