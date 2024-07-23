/**
 * 基带升级
 typedef struct {
 	int sync_header;
 	int msg_type;      		//UI_2_gNB_OAM_MSG
 	int cmd_type;    		//UI_2_gNB_VERSION_UPGRADE
 	int cmd_param;

 	int server_type; 		// 1-tftp, 2-ftp, 3-scp
 	char version_name[64];
 	char version_md5[36];
 	int sync_footer;
 } oam_version_upgrade_t; //in
 */
package com.nr.Gnb;

import com.nr.Gnb.Bean.Header;
import com.Logcat.SLog;
import com.nr.Util.DataUtil;
import com.nr.Util.MD5;

import java.io.File;
import java.io.IOException;

public class GnbUpgrade {
	public static int u16MsgLength = Header.headLength + 4 + 36 + 64;/* 定义消息的长度 */
	private byte[] sendMsg = new byte[u16MsgLength];
	private int mMsgIdx;

	public GnbUpgrade(Header header, int server_type, String version_name, String version_md5) {
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
		int2byte(server_type);
		handleVersionName(version_name);
		handleVersionMd5(version_md5);

		mMsgIdx = u16MsgLength - 4;
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
	private void handleVersionName(String version_name) {
		byte[] bytes = version_name.getBytes();
		for (int i = 0; i < bytes.length; i++) {
			sendMsg[mMsgIdx] = bytes[i];
			mMsgIdx++;
		}
		if (bytes.length < 64) {
			int offset = 64 - bytes.length;
			mMsgIdx += offset;
		}
	}

	private void handleVersionMd5(String version_md5) {
		try {
			String md5 = MD5.md5(new File(version_md5));
			byte[] bytes = md5.getBytes();
			for (int i = 0; i < bytes.length; i++) {
				sendMsg[mMsgIdx] = bytes[i];
				mMsgIdx++;
			}
			if (bytes.length < 36) {
				int offset = 36 - bytes.length;
				mMsgIdx += offset;
			}
			SLog.I("xxx.md5: " + md5);
		} catch (IOException e) {
			SLog.E("xxx.md5: " + e.toString());
		}
	}

	public byte[] getMsg() {
		return sendMsg;
	}
}
