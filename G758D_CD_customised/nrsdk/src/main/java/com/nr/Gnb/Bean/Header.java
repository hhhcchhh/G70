package com.nr.Gnb.Bean;

import com.nr.Util.DataUtil;

/**
 * typedef struct {
 *  int sync_header;	//0x5AA51111
 * 	int msg_type;      //UI_2_gNB_OAM_MSG
 * 	int cmd_type;
 * 	int cmd_param;
 *
 * 	int sync_footer; 	//0xFF660000
 * } new_oam_msg_t;
 */

public class Header {
	public static int headLength = 20; // 含数据头尾
	private final byte[] headerMsg = new byte[16]; // 数据头统一长度：16
	private final byte[] footerMsg = new byte[4]; // 数据头统一长度：4
	private final int u32MsgType;/* 定义消息的类型名称 */
	public Header(int msg_type, int cmd_type, int cmd_param) {
		this.u32MsgType = msg_type;
		headerMsg[0] = 0x11;
		headerMsg[1] = 0x11;
		headerMsg[2] = (byte)0xA5;
		headerMsg[3] = (byte)0x5A; /* 0x5AA51111 */
		int idx = 4;
		byte[] data = DataUtil.intToBytes(msg_type);
		for (byte datum : data) {
			headerMsg[idx] = datum;
			idx++;
		}
		data = DataUtil.intToBytes(cmd_type);
		for (byte datum : data) {
			headerMsg[idx] = datum;
			idx++;
		}
		//stopCatch 0: 不删除 1：删除自启动配置参数
		data = DataUtil.intToBytes(cmd_param);
		for (byte datum : data) {
			headerMsg[idx] = datum;
			idx++;
		}

		footerMsg[0] = (byte)0xFF;
		footerMsg[1] = 0x66;
		footerMsg[2] = 0x00;
		footerMsg[3] = 0x00; /* 0xFF660000 */
	}
	public byte[] getHeaderAndMsgType(int length) {
		byte[] data = DataUtil.intToBytes(length);
		int idx = 0;
		// 整个数据长度
		for (byte datum : data) {
			headerMsg[idx] = datum;
			idx++;
		}
		byte[] msgtype = DataUtil.intToBytes(u32MsgType);
		for (byte b : msgtype) {
			headerMsg[idx] = b;
			idx++;
		}
		return headerMsg;
	}
	public byte[] getHeaderMsg() {
		return headerMsg;
	}

	public byte[] getFooterMsg() {
		return footerMsg;
	}
}
