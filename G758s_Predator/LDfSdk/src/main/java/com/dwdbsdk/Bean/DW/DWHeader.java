package com.dwdbsdk.Bean.DW;

import com.dwdbsdk.Util.DataUtil;

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

public class DWHeader {
	public static int headLength = 20; // 含数据头尾
	byte[] headerMsg = new byte[16]; // 数据头统一长度：16
	public DWHeader(int msg_type, int cmd_type, int cmd_param) {

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
	}

	public byte[] getHeaderMsg() {
		return headerMsg;
	}

	public byte[] getFooterMsg() {
		byte[] footerMsg = new byte[4]; // 数据头统一长度：4
		footerMsg[0] = (byte)0xFF;
		footerMsg[1] = 0x66;
		footerMsg[2] = 0x00;
		footerMsg[3] = 0x00; /* 0xFF660000 */
		return footerMsg;
	}
}
