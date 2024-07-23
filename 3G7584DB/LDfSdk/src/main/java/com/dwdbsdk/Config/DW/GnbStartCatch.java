//开始帧码
/*typedef struct {
	int sync_header;
	int msg_type;          	//UI_2_gNB_OAM_MSG
	int cmd_type;			//UI_2_gNB_START_CATCH 13 启动侦码
	int cmd_param;
	int cell_id;
	int save_flag; 			//0-no save, 1-save for autorun
	int start_tac;			//min TAC index
	int end_tac;			//max TAC index
	int tac_interval;		//10~9999(second)
	int sync_footer;        //固定值 4位 0x000066FF
} oam_start_catch_t;  		//in
 */
package com.dwdbsdk.Config.DW;

import com.dwdbsdk.Bean.DW.DWHeader;
import com.dwdbsdk.Util.DataUtil;

public class GnbStartCatch {
    private final int u16MsgLength = DWHeader.headLength + 5 * 4;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public GnbStartCatch(DWHeader header, int cell_id, int save_flag, int start_tac, int end_tac, int tac_interval) {
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
        int2byte(cell_id);
        int2byte(save_flag);
        int2byte(start_tac);
        int2byte(end_tac);
        int2byte(tac_interval);
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
