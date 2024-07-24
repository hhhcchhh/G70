/**
 typedef struct {
 int msg_header;      		//HEADER_MAGIC
 int msg_sn;       		//serial num
 int msg_len;      		//sizeof bt_msg_xxx_t
 int msg_type;      		//BT_MSG_START_GSM_SCAN=0x37
 int param;

 //https://www.sqimway.com/gsm_arfcn.php
 //0(gsm450) 1(gsm480) 2(gsm850) 3(pgsm900) 4(egsm900) 5(rgsm900)
 //6(ergsm900) 7(dcs1800) 8(pcs1900)
 int band_id;
 int arfcn;          	//0-1023
 int sched_mode;    	//1-512
 int resv[20];

 int msg_footer;      		//FOOTER_MAGIC
 } bt_msg_gsm_scan_t; //in

 应答：

 typedef struct {
 int msg_header;		//HEADER_MAGIC
 int msg_sn;		//serial num
 int msg_len;		//sizeof bt_msg_xxx_t
 int msg_type;		//BT_MSG_START_GSM_SCAN=0x37
 int param;			//param, flags, ack
 int msg_footer;		//FOOTER_MAGIC
 } bt_msg_glb_t; //in&out
 */
package com.dwdbsdk.Config.DB;

import com.dwdbsdk.Bean.DB.DBHeader;
import com.dwdbsdk.Util.DataUtil;

public class MsgStartGSMDetect {
    private final int u16MsgLength = DBHeader.headLength + 4 * 25;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public MsgStartGSMDetect(int msg_type, int type, int band_id, int dl_arfcn, int ul_arfcn, int sched_mode, int doa_num) {

        for (int i = 0; i < u16MsgLength; i++) {
            sendMsg[i] = 0;
        }
        mMsgIdx = 0;
        byte[] headMsg = DBHeader.getHeader(u16MsgLength, msg_type);
        for (int i = 0; i < headMsg.length; i++) {
            sendMsg[i] = headMsg[i];
            mMsgIdx++;
        }
        // 此处顺序不能变
        int2byte(0);
        int2byte(type);
        int2byte(band_id);
        int2byte(dl_arfcn);
        int2byte(ul_arfcn);
        int2byte(sched_mode);
        int2byte(doa_num);

        mMsgIdx = u16MsgLength - 4;
        byte[] footerMsg = DBHeader.getFooter();
        for (byte b : footerMsg) {
            sendMsg[mMsgIdx] = b;
            mMsgIdx++;
        }
    }

    private void int2byte(int para) {
        byte[] data = DataUtil.intToBytes(para);
        for (byte datum : data) {
            sendMsg[mMsgIdx] = datum;
            mMsgIdx++;
        }
    }

    private void long2byte(long para) {
        byte[] data = DataUtil.long2Bytes(para);
        for (byte datum : data) {
            sendMsg[mMsgIdx] = datum;
            mMsgIdx++;
        }
    }

    public byte[] getMsg() {
        return sendMsg;
    }
}
