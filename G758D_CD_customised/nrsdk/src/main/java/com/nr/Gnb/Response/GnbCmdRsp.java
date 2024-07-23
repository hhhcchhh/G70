/**
 *
 * 指令配置返回信息
 * typedef struct {
 * 		int sync_header;
 * 		int msg_type;          	//UI_2_gNB_OAM_MSG
 * 		int cmd_type;
 * 		int rspValue;
 *
 * 		int sync_footer;
 * } new_oam_msg_t; //in&out
 *
 * 指令配置返回信息带CELL_ID
 * typedef struct {
 * 		int sync_header;
 * 		int msg_type;          	//UI_2_gNB_OAM_MSG
 * 		int cmd_type;
 * 		int rspValue;
 *
 * 		int cellId;
 *
 * 		int sync_footer;
 * } oam_start_catch_t, oam_stop_catch_t; //in&out
 */
package com.nr.Gnb.Response;

public class GnbCmdRsp {
	private int cellId; // 0: 单载波, 1：双载波, -1: 忽略
	private int msgType; // 工作状态: 详见类：gnbState
	private int cmdType; // 指令类型，详见MessageProtocol
	private int rspValue; // 配置成功与否,GnbProtocol: OAM_ACK_OK = 0, OAM_ACK_ERROR = 1, OAM_ACK_E_PARAM = 2, OAM_ACK_E_BUSY = 3


	public GnbCmdRsp(int cellId, int msgType, int cmdType, int rspValue) {
		this.cellId = cellId;
		this.msgType = msgType;
		this.cmdType = cmdType;
		this.rspValue = rspValue;
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

	public int getCmdType() {
		return cmdType;
	}

	public void setCmdType(int cmdType) {
		this.cmdType = cmdType;
	}

	public int getRspValue() {
		return rspValue;
	}

	public void setRspValue(int rspValue) {
		this.rspValue = rspValue;
	}

	public int getCellId() {
		return cellId;
	}

	public void setCellId(int cellId) {
		this.cellId = cellId;
	}

	@Override
	public String toString() {
		return "GnbCmdRsp{" +
				"cellId=" + cellId +
				", msgType=" + msgType +
				", cmdType=" + cmdType +
				", rspValue=" + rspValue +
				'}';
	}
}
