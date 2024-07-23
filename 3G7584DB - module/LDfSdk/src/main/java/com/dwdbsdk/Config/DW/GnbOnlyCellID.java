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
package com.dwdbsdk.Config.DW;
import com.dwdbsdk.Bean.DW.DWHeader;
import com.dwdbsdk.Util.DataUtil;

public class GnbOnlyCellID {
    private final int u16MsgLength = DWHeader.headLength + 4;/* 定义消息的长度 */
	private final byte[] sendMsg = new byte[u16MsgLength];
	private int mMsgIdx;

	public GnbOnlyCellID(DWHeader header, int cell_id) {
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
