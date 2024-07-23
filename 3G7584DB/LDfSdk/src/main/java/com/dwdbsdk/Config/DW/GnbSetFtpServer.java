/**
 * 配置FTP服务器
 * <p>
 * typedef struct {
 * int sync_header;
 * int msg_type;      		//UI_2_gNB_OAM_MSG
 * int cmd_type;    		//OAM_MSG_SET_METH_CFG
 * int cmd_param;
 * <p>
 * char ftp_server[OAM_STR_MAX]; 192.168.1.100
 * char ftp_path[OAM_STR_MAX]; DW_ftp
 * char ftp_user[OAM_STR_MAX]; user
 * char ftp_passwd[OAM_STR_MAX]; admin
 * int upload_interval; 		// 1-1440min
 * int sync_footer;
 * } oam_cfg_ftpserver_t; //in&out
 */
package com.dwdbsdk.Config.DW;

import com.dwdbsdk.Bean.DW.DWHeader;
import com.dwdbsdk.Bean.DW.DWProtocol;
import com.dwdbsdk.Util.DataUtil;

public class GnbSetFtpServer {
    private final int u16MsgLength = DWHeader.headLength + 4 + 4 * DWProtocol.OAM_STR_MAX;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public GnbSetFtpServer(DWHeader header, String ftp_server, String ftp_path, String ftp_user, String ftp_passwd, int upload_interval) {
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
        handleChar(ftp_server);
        handleChar(ftp_path);
        handleChar(ftp_user);
        handleChar(ftp_passwd);
        int2byte(upload_interval);
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

    private void handleChar(String sftp) {
        byte[] bytes = sftp.getBytes();
        for (byte aByte : bytes) {
            sendMsg[mMsgIdx] = aByte;
            mMsgIdx++;
        }
        if (bytes.length < DWProtocol.OAM_STR_MAX) {
            int offset = DWProtocol.OAM_STR_MAX - bytes.length;
            mMsgIdx += offset;
        }
    }

    public byte[] getMsg() {
        return sendMsg;
    }
}
