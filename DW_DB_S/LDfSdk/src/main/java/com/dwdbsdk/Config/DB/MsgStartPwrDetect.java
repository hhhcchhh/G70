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
 * int doa_num                      //0:单通道，2:4通道，3:6通道
 * int frame_type                   //0-9
 * int chan_sel                     //0-15: ch1 sel，16-31: ch2 sel
 * int resv[1];                     //预留
 * <p>
 * int msg_footer;      				//FOOTER_MAGIC
 * } gr_msg_start_scan_t;
 */
package com.dwdbsdk.Config.DB;

import com.dwdbsdk.Bean.DB.DBHeader;
import com.dwdbsdk.Util.DataUtil;

public class MsgStartPwrDetect {
    private final int u16MsgLength = DBHeader.headLength + 4 * 21;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public MsgStartPwrDetect(int msg_type, int dl_arfcn, int ul_arfcn, int kssb, int offset2pointA, int time_offset, int rb_offset,
                             int slot_index, int sched_mode, int par_cfg, int unlock_check_point, int pci, int bandwidth, int slot_index2,
                             int smooth_type, int smooth_win_len, int prb_num, int doa_num, int frame_type, int chan_sel) {

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
        int2byte(frame_type);
        if (chan_sel > 3 && chan_sel < 7){
            // 4、5、6
            chan_sel = (chan_sel - 3) << 16;
        } else if (chan_sel > 9) {
            // 高16位： chan_sel % 10 << 16  低16位： chan_sel / 10
            int max = chan_sel % 10;
            if (max > 3) max = max - 3;
            chan_sel = max << 16 + chan_sel / 10;
        } else if (chan_sel < 0) chan_sel = 0;
        int2byte(chan_sel);

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

    public byte[] getMsg() {
        return sendMsg;
    }
}
