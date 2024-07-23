/**
 * 设置单板工作模式
 * 
 * OAM_MSG_SET_DUAL_CELL = 212;  //
 * typedef struct {
 *  int sync_header;
 *  int msg_type;            //UI_2_gNB_OAM_MSG
 *  int cmd_type;              //OAM_MSG_SET_DUAL_CELL
 *  int cmd_param;
 *
 *  int dual_cell;    //1-singel cell, 2-dual cell(default)
 *
 *  int sync_footer;
 * } oam_cfg_dual_cell_t;  //in
 */
package com.nr.Gnb;

import com.nr.Gnb.Bean.Header;
import com.nr.Util.DataUtil;

public class GnbDualCell {
    public static int u16MsgLength = Header.headLength + 4;/* 定义消息的长度 */
	private byte[] sendMsg = new byte[u16MsgLength];
	private int mMsgIdx;

	public GnbDualCell(Header header, int dual_cell) {
		mMsgIdx = 0;
		for (int i = 0; i < u16MsgLength; i++) {
			sendMsg[i] = 0;
		}
		
		byte headMsg[] = header.getHeaderMsg();
		for (int i = 0; i < headMsg.length; i++) {
			sendMsg[i] = headMsg[i];
			mMsgIdx++;
		}

		int2byte(dual_cell);

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
