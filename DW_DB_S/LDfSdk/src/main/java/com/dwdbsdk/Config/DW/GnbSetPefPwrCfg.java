/* 设置参考功率
typedef struct {
	int sync_header;
	int msg_type;    			//UI_2_gNB_OAM_MSG
	int cmd_type;   			//OAM_MSG_SET_REF_PWR = 64（4g，5g都是64）
	int cmd_param;

	int cell_id;
	int pusch_p0;             // [-126..24]
	int sync_footer;
} oam_set_refer_pwr_t;
* */
package com.dwdbsdk.Config.DW;

import com.dwdbsdk.Bean.DW.DWHeader;
import com.dwdbsdk.Util.DataUtil;

public class GnbSetPefPwrCfg {
    private final int u16MsgLength = DWHeader.headLength + 8;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public GnbSetPefPwrCfg(DWHeader header, int cell_id, int pusch_p0) {
        mMsgIdx = 0;
        for (int i = 0; i < u16MsgLength; i++) {
            sendMsg[i] = 0;
        }

        byte[] headMsg = header.getHeaderMsg();
        for (int i = 0; i < headMsg.length; i++) {
            sendMsg[i] = headMsg[i];
            mMsgIdx++;
        }

        int2byte(cell_id);
        int2byte(pusch_p0);

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
