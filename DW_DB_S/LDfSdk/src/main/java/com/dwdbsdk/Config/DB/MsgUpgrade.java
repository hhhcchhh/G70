/**
 * typedef struct {
 * int msg_header;						//HEADER_MAGIC
 * int msg_sn;							//serial num
 * int msg_len;						//sizeof bt_msg_xxx_t
 * int msg_type;						//GR_MSG_IMG_UPGRADE
 * int param;
 * int server_type;					//0-scp
 * char img_name[64];
 * char img_url[64];
 * char img_md5[64];
 * int msg_footer;						//FOOTER_MAGIC
 * } bt_msg_img_upgrade_t;
 */
package com.dwdbsdk.Config.DB;

import com.dwdbsdk.Bean.DB.DBHeader;
import com.dwdbsdk.Logcat.SdkLog;
import com.dwdbsdk.Util.DataUtil;
import com.dwdbsdk.Util.MD5;

import java.io.File;
import java.io.IOException;

public class MsgUpgrade {
    private final int u16MsgLength = DBHeader.headLength + 8 + 3 * 64;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx = 0;

    public MsgUpgrade(int msg_type, int server_type, String version_name, String version_md5) {
        for (int i = 0; i < u16MsgLength; i++) {
            sendMsg[i] = 0;
        }
        mMsgIdx = 0;
        byte[] headMsg = DBHeader.getHeader(u16MsgLength, msg_type);
        for (byte b : headMsg) {
            sendMsg[mMsgIdx] = b;
            mMsgIdx++;
        }
        // 此处顺序不能变
        int2byte(0);
        handleServerType(server_type);
        handleVersionName(version_name);
        handleVersionUrl(version_name);
        handleVersionMd5(version_md5);

        mMsgIdx = u16MsgLength - 4;
        byte[] footerMsg = DBHeader.getFooter();
        for (byte b : footerMsg) {
            sendMsg[mMsgIdx] = b;
            mMsgIdx++;
        }
    }

    private void handleVersionUrl(String version_name) {
        // 这里不能为空
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
            if (bytes.length < 64) {
                int offset = 64 - bytes.length;
                mMsgIdx += offset;
            }
        } catch (IOException e) {
            SdkLog.E("xxx.md5: " + e);
        }
    }

    private void handleServerType(int server_type) {
        byte[] data = DataUtil.intToBytes(server_type);
        for (byte datum : data) {
            sendMsg[mMsgIdx] = datum;
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
