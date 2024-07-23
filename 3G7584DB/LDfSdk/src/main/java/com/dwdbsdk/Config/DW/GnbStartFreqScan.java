/**
 * typedef struct {
 * int sync_header;
 * int msg_type;               	//UI_2_gNB_OAM_MSG
 * int cmd_type;                 //OAM_MSG_FREQ_SCAN_REPORT(in)
 * int cmd_param;                  //0-start, 1-data, 2-end
 * int report_level;               //0-brief
 * int async_enable				1:enable; 0:disable	空口开关
 * int arfcn_num					Max = 32 	频点输出数量
 * int chan_id[32]					chan_id[32]	U32	1 （Rx1）或 2（Rx2）	频段对应Rx通道，可设置32组值。举例：N41接收连接Rx1，则设置为1；N78接收连接Rx2，则配置为2。
 * int arfcn_list[32]				0 means null	频点数组，可设置32组值
 * int time_offset[32]				GPS 时偏，可设置32组值
 * int sync_footer;
 * } oam_freq_scan_report_t;
 */
package com.dwdbsdk.Config.DW;

import com.dwdbsdk.Bean.DW.DWHeader;
import com.dwdbsdk.Util.DataUtil;

import java.util.List;

public class GnbStartFreqScan {
    private final int u16MsgLength = DWHeader.headLength + 3 * 4 + 32 * 4 + 32 * 4 + 32 * 4; /* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public GnbStartFreqScan(DWHeader header, int report_level, int async_enable, int arfcn_num, List<Integer> chan_id
            , List<Integer> arfcn_list, List<Integer> time_offset) {

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
        int2byte(report_level);
        int2byte(async_enable);
        int2byte(arfcn_num);
        handleChan_id(arfcn_num, chan_id);
        handleArfcn(arfcn_num, arfcn_list);
        handleTimeOffset(arfcn_num, time_offset);
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

    private void handleChan_id(int arfcn_num, List<Integer> chan_id) {
        for (int i = 0; i < 32; i++) {
            if (i >= arfcn_num) {
                int2byte(0);
            } else {
                int2byte(chan_id.get(i));
            }

        }
    }

    private void handleArfcn(int arfcn_num, List<Integer> arfcnList) {
        for (int i = 0; i < 32; i++) {
            if (i >= arfcn_num) {
                int2byte(0);
            } else {
                int2byte(arfcnList.get(i));
            }

        }
    }

    private void handleTimeOffset(int arfcn_num, List<Integer> timeOffsetList) {
        for (int i = 0; i < 32; i++) {
            if (i >= arfcn_num) {
                int2byte(0);
            } else {
                int2byte(timeOffsetList.get(i));
            }

        }
    }

    public byte[] getMsg() {
        return sendMsg;
    }
}
