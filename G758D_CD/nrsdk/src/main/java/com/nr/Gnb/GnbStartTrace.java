/**
 * 开始定位
 * UI_2_gNB_START_TRACE = 15;  // 定位与帧码互斥，支持一个目标UE。
 * typedef struct {
 * int sync_header;
 * int msg_type;    			//UI_2_gNB_OAM_MSG
 * int cmd_type;   			//UI_2_gNB_START_TRACE
 * int cmd_param;
 * <p>
 * int cell_id;
 * int target_num; // 同时追踪imsi数
 * ue_id_t target_ue[MAX_TARGET_UE_NUM]; // 同时追踪imsi号
 * <p>
 * int sync_footer;
 * } oam_start_trace_t; //in
 */
package com.nr.Gnb;

import com.nr.Gnb.Bean.Header;
import com.Logcat.SLog;
import com.nr.Gnb.Bean.GnbProtocol;
import com.nr.Gnb.Bean.UeidBean;
import com.nr.Util.DataUtil;

import java.util.List;


public class GnbStartTrace {
    public static int u16MsgLength = Header.headLength + 12 + 2 * GnbProtocol.MAX_TARGET_UE_NUM * GnbProtocol.MAX_IMSI_LEN;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public GnbStartTrace(Header header, int cell_id, int target_num, String target_ue, int report_phone_type) {
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
        int2byte(cell_id);
        int2byte(target_num);
        handleImsi(target_num, target_ue);
        int2byte(report_phone_type);

        mMsgIdx = u16MsgLength - 4;
        byte[] footerMsg = header.getFooterMsg();
        for (byte b : footerMsg) {
            sendMsg[mMsgIdx] = b;
            mMsgIdx++;
        }
    }

    public GnbStartTrace(Header header, int cell_id, List<UeidBean> ueid, int report_phone_type) {
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
        int2byte(cell_id);
        int2byte(ueid.size());
        handleImsi(ueid);
        int2byte(report_phone_type);

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

    private void handleImsi(List<UeidBean> ueid) {
        for (int i = 0; i < ueid.size(); i++) {
            // imsi
            byte[] imsiBytes = ueid.get(i).getImsi().getBytes();
            for (int j = 0; j < GnbProtocol.MAX_IMSI_LEN && j < imsiBytes.length; j++) {
                sendMsg[mMsgIdx] = imsiBytes[j];
                mMsgIdx++;
            }
            if (imsiBytes.length < 16) {
                int offset = 16 - imsiBytes.length;
                mMsgIdx += offset;
            }

            // guti
            byte[] gutiBytes = ueid.get(i).getGuti().getBytes();
            for (int j = 0; j < GnbProtocol.MAX_IMSI_LEN && j < gutiBytes.length; j++) {
                sendMsg[mMsgIdx] = gutiBytes[j];
                mMsgIdx++;
            }
            if (gutiBytes.length < 16) {
                int offset = 16 - gutiBytes.length;
                mMsgIdx += offset;
            }
        }
    }

    private void handleImsi(int target_num, String target_ue) {
        int idx_start, idx_end;
        String[] data = target_ue.split(";");
        for (int i = 0; i < target_num; i++) {
            idx_start = 0;
            idx_end = 1;
            // 460110557568080I:183243009
            if (data[i].length() > 15) {
                data[i] = data[i].substring(0, 15);
                SLog.I("GnbStartTrace handleImsi() data[i].length() > 15: data[i] = " + data[i]);
            }
            while (idx_end <= data[i].length()) {
                String a = "3" + data[i].substring(idx_start, idx_end);
                sendMsg[mMsgIdx] = Byte.valueOf(a, 16);
                mMsgIdx++;
                idx_start++;
                idx_end++;
            }
            sendMsg[mMsgIdx] = 0x00;
            mMsgIdx++;
            // guti
            for (int j = 0; j < GnbProtocol.MAX_IMSI_LEN; j++) {
                sendMsg[mMsgIdx] = 0x00;
                mMsgIdx++;
            }
        }
    }

    public byte[] getMsg() {
        return sendMsg;
    }
}
