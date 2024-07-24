/**
 * typedef struct {
 * 	int sync_header;
 * 	int msg_type;         		    //UI_2_gNB_OAM_MSG
 * 	int cmd_type;            	    //301
 * 	int param;                      //payload + 20
 *
 *     char payload[256];
 * 	int sync_footer;
 * } oam_tty_fwd_data_t;
* */

package com.dwdbsdk.Response.DW;

public class GnbReadDataFwdRsp {
	String data;
	int msgLen;
	public GnbReadDataFwdRsp() {
		this.data = "";
		this.msgLen = 0;
	}

	public int getMsgLen() {
		return msgLen;
	}

	public void setMsgLen(int msgLen) {
		this.msgLen = msgLen;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
	@Override
	public String toString() {
		return "GnbReadDataFwdRsp{" +
				"data='" + data + '\'' +
				", msgLen=" + msgLen +
				'}';
	}
}
