/**
 * typedef struct {
 * 	int msg_header;      				//HEADER_MAGIC
 * 	int msg_sn;       					//serial num
 * 	int msg_len;      					//sizeof bt_msg_xxx_t
 * 	int msg_type;      					//BT_MSG_GET_JAM
 * 	int param;
 *
 * 	bt_msg_tx_t tx[2];     				//tx1 tx2
 * 	bt_msg_orx_t orx[8];   				//orx_list max = 8
 * 	int autoStartJam; 						//0-no save, 1-save
 * 	int msg_footer;      				//FOOTER_MAGIC
 * } bt_msg_start_jam_t; //down
 */
package com.dwdbsdk.Response.DB;

import com.dwdbsdk.Bean.DB.OrxBean;
import com.dwdbsdk.Bean.DB.TxBean;

import java.util.List;

public class MsgGetJamRsp {

	public static final int AUTO = 1; // 自启动干扰
	public static final int NORMAL = 0; // 正常手动配置

	int msgType;
	int autoStartJam;
	TxBean tx1, tx2;
	List<OrxBean> orxList;

	public MsgGetJamRsp() {
		this.msgType = -1;
		this.autoStartJam = -1;
		this.tx1 = null;
		this.tx2 = null;
		this.orxList = null;
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

	public int getAutoStartJam() {
		return autoStartJam;
	}

	public void setAutoStartJam(int autoStartJam) {
		this.autoStartJam = autoStartJam;
	}

	public TxBean getTx1() {
		return tx1;
	}

	public void setTx1(TxBean tx1) {
		this.tx1 = tx1;
	}

	public TxBean getTx2() {
		return tx2;
	}

	public void setTx2(TxBean tx2) {
		this.tx2 = tx2;
	}

	public List<OrxBean> getOrxList() {
		return orxList;
	}

	public void setOrxList(List<OrxBean> orxList) {
		this.orxList = orxList;
	}

	@Override
	public String toString() {
		return "MsgGetJamRsp{" +
				"msgType=" + msgType +
				", autoStartJam=" + autoStartJam +
				", \ntx1=" + tx1 +
				", \ntx2=" + tx2 +
				", \norxList=" + orxList +
				'}';
	}
}
