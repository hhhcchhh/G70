/**
 * 配置带CELL_ID带参数的指令
 //UI_2_gNB_START_CATCH
 //UI_2_gNB_STOP_CATCH
 //UI_2_gNB_STOP_TRACE
 //UI_2_gNB_START_CONTROL
 //UI_2_gNB_STOP_CONTROL
 typedef struct {
 	int sync_header;
 	int msg_type;          	//UI_2_gNB_OAM_MSG
 	int cmd_type;
 	int cmd_param;

 	int cell_id;

 	int sync_footer;
 } oam_start_catch_t, oam_stop_catch_t; //in&out
 */
package com.nr.Gnb;

import com.nr.Gnb.Bean.Header;
import com.nr.Util.DataUtil;

public class GnbOnlyCellID {
    public static int u16MsgLength = Header.headLength + 4;/* 定义消息的长度 */
	private byte[] sendMsg = new byte[u16MsgLength];
	private int mMsgIdx;

	public GnbOnlyCellID(Header header, int cell_id) {
		mMsgIdx = 0;
		for (int i = 0; i < u16MsgLength; i++) {
			sendMsg[i] = 0;
		}

		byte headMsg[] = header.getHeaderMsg();
		for (int i = 0; i < headMsg.length; i++) {
			sendMsg[i] = headMsg[i];
			mMsgIdx++;
		}
		// 此处顺序不能变
		int2byte(cell_id);

		byte footerMsg[] = header.getFooterMsg();
		for (int i = 0; i < footerMsg.length; i++) {
			sendMsg[mMsgIdx] = footerMsg[i];
			mMsgIdx++;
		}
	}

	private void int2byte(int idata) {
		byte[] data = DataUtil.intToBytes(idata);
		for (int i = 0; i < data.length; i++) {
			sendMsg[mMsgIdx] = data[i];
			mMsgIdx++;
		}
	}
	public byte[] getMsg() {
		return sendMsg;
	}
}
