/**
 * 基带升级
 * typedef struct {
 * int sync_header;
 * int msg_type;      		//UI_2_gNB_OAM_MSG
 * int cmd_type;    		//UI_2_gNB_VERSION_UPGRADE
 * int cmd_param;
 * <p>
 * int server_type; 		// 1-tftp, 2-ftp, 3-scp
 * char version_name[64];
 * char version_md5[36];
 * int sync_footer;
 * } oam_version_upgrade_t; //in
 */
package com.dwdbsdk.Config.DW;

import com.dwdbsdk.Bean.DW.DWHeader;
import com.dwdbsdk.Util.DataUtil;
import com.dwdbsdk.Util.MD5;

import java.io.File;
import java.io.IOException;

public class GnbUpgrade {
    private final int u16MsgLength = DWHeader.headLength + 4 + 36 + 64;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public GnbUpgrade(DWHeader header, int server_type, String version_name, String version_md5) {
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
        int2byte(server_type);
        handleVersionName(version_name);
        handleVersionMd5(version_md5);

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

    private void handleVersionName(String version_name) {
        byte[] bytes = version_name.getBytes();
        for (byte aByte : bytes) {
            sendMsg[mMsgIdx] = aByte;
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
            for (byte aByte : bytes) {
                sendMsg[mMsgIdx] = aByte;
                mMsgIdx++;
            }
            if (bytes.length < 36) {
                int offset = 36 - bytes.length;
                mMsgIdx += offset;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] getMsg() {
        return sendMsg;
    }
}
