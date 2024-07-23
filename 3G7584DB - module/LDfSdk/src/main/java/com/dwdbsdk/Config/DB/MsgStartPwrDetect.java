/**
 * typedef struct {
 * int msg_header;      				//HEADER_MAGIC
 * int msg_sn;       					//serial num
 * int msg_len;      					//sizeof gr_msg_xxx_t
 * int msg_type;      				//GR_MSG_START_PWR_SCAN
 * <p>
 * int param;
 * int dl_arfcn;
 * int ul_arfcn
 * int kssb;
 * int offset2pointA;
 * int time_offset;
 * int rb_start;    					// 9--200
 * int slot_index						// 0-19
 * int sched_mode;    				// 0: vehicle(40ms)   1: normal(160ms)
 * int par_cfg; 						// real*100, 0 means default(300)
 * int unlock_check_point; 			// 5-19, default is 15
 * int pci;
 * int bandwidth                    //18,28,58,188
 * int slot_index2;                 //0-19, -1 means null
 * int smooth_type;                    //0-2
 * int smooth_win_len;                 //0-1023
 * int prb_num                      //1-5
 * int doa_num                      //0-单通道，2-4通道，3-6通道
 * int resv[3];                     //预留
 * <p>
 * int msg_footer;      				//FOOTER_MAGIC
 * } gr_msg_start_scan_t;
 */
package com.dwdbsdk.Config.DB;

import com.dwdbsdk.Bean.DB.DBHeader;
import com.dwdbsdk.Logcat.SdkLog;
import com.dwdbsdk.Util.DataUtil;

public class MsgStartPwrDetect {
    private final int u16MsgLength = DBHeader.headLength + 4 * 20 + 8;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public MsgStartPwrDetect(int msg_type, int dl_arfcn, int ul_arfcn, int kssb, int offset2pointA, int time_offset, int rb_offset,
                             int slot_index, int sched_mode, int par_cfg, int unlock_check_point, int pci, int bandwidth,int slot_index2,
                             int  smooth_type,int smooth_win_len,int prb_num,int doa_num) {
        SdkLog.D("MsgStartScan  dl_arfcn = " + dl_arfcn + ", ul_arfcn = " + ul_arfcn + ", " + kssb + ", "
                + offset2pointA + ", " + time_offset + ", " + rb_offset + "," + slot_index + ", " + sched_mode + ", "
                + par_cfg + ", " + unlock_check_point + ", " + pci+", " + bandwidth+", " + slot_index2+", "+smooth_type+
                ", "+smooth_win_len+", "+prb_num+", "+doa_num);
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
        int2byte(dl_arfcn);
        int2byte(ul_arfcn);
        int2byte(kssb);
        int2byte(offset2pointA);
        int2byte(time_offset);
        int2byte(rb_offset);
        int2byte(slot_index);
        int2byte(sched_mode);
        int2byte(par_cfg);
        int2byte(unlock_check_point);
        int2byte(pci);
        int2byte(bandwidth);
        int2byte(slot_index2);
        int2byte(smooth_type);
        int2byte(smooth_win_len);
        int2byte(prb_num);
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
