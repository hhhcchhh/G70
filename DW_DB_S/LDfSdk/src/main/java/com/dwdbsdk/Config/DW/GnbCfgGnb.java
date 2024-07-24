/**
 * 基带板参数配置
 * UI_2_gNB_CFG_gNB = 10
 * typedef struct {
 * int sync_header;
 * int msg_type;         	//UI_2_gNB_OAM_MSG
 * int cmd_type;           //UI_2_gNB_CFG_gNB
 * int cmd_param;
 * <p>
 * //below sync to gnb_cfg_t
 * int cell_id;             // 0: first carrier id, 1: second carrier id
 * int MCC;                // 460
 * int MNC;				// 00/01/11
 * int DL_NR_ARFCN;
 * int UL_NR_ARFCN;
 * int PCI;                // 0~1007
 * int TAC;
 * int offset2pointA;
 * int kssb;
 * int ue_max_pwr;         //
 * int timing_offset;      // ct/cu:0, cm: 300 us
 * int vehicle_mode;       // vehicle:1, single:2, other: 0
 * int air_sync_enable;
 * int MCC2;               // 0: not avail; other: 460
 * int MNC2;				// 00/01/11
 * int ul_rb_offset;       // 9~200
 * int cid[2];             // 8 byte, lower 5 byte valid
 * int ssb_bitmap; 		// 0b1(#0)1(#1)1(#2)1(#3)1(#4)1(#5)1(#6)1(#7) beam, 4.9g or 10MHz: 0xb10000000
 * int frame_type;         // frame_type_e default: FRAME_TYPE_TDD_CFG_2D5MS
 * int bandwidth;          // 100Mhz: 100, 20MHz: 20, 10Mhz: 10
 * int reject_code;         // nas reject code
 * int rxLevMin;            // Q-RxLevMin * 2 [dBm], (-70~-30) * 2
 * int redirect_2_4g_earfcn;   // 0: disable; lte earfcn, e.g.: 37900
 * int mob_reject_code;        // 0: disable; other reject code
 * int split_cid
 * int split_pci
 * int split_offset2pointA
 * int split_kssb
 * String split_arfcn_dl
 * String split_arfcn_ul
 * int resv[6];           	// 0801-12
 * int cfr_enable;			// 0-disable, 1-enable
 * int swap_rf;			// 0-noswap, 1-swap     1）N28、N78、N79 在 PA 通道二：swap_rf = 1； 2）N1、N41 在 PA 通道一：swap_rf = 0。
 * int sync_footer;
 * } oam_gnb_cfg_t; //in
 */
package com.dwdbsdk.Config.DW;

import com.dwdbsdk.Bean.DW.DWHeader;
import com.dwdbsdk.Logcat.SdkLog;
import com.dwdbsdk.Util.DataUtil;

public class GnbCfgGnb {
    private final int u16MsgLength = DWHeader.headLength + 27 * 4 + 12 * 4;
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public GnbCfgGnb(DWHeader header, int cell_id, int MCC, int MNC, int DL_NR_ARFCN, int UL_NR_ARFCN, int PCI, int TAC,
                     int offset2pointA, int kssb, int ue_max_pwr, int timing_offset, int work_mode, int air_sync_enable,
                     int MCC2, int MNC2, int ul_rb_offset, long cid, int ssb_bitmap, int frame_type, int bandwidth,
                     int cfr_enable, int swap_rf, int reject_code, int rxLevMin, int redirect_2_4g_earfcn, int mob_reject_code,
                     int split_cid, int split_pci, int split_offset2pointA, int split_kssb, int SplitArfcnDl, int SplitArfcnUl, int force_cfg) {

        SdkLog.I("gnbCfg[" + cell_id + "]: " + MCC + ", " + MNC + ", " + DL_NR_ARFCN + ", " + UL_NR_ARFCN + ", " + PCI + ", " + TAC + ", "
                + offset2pointA + ",  " + kssb + ", " + ue_max_pwr + ", " + timing_offset + ", " + work_mode + ", " + air_sync_enable + ", "
                + MCC2 + ", " + MNC2 + ", " + ul_rb_offset + ", " + cid + ", " + ssb_bitmap + ", " + frame_type + ", " + bandwidth + ", " + cfr_enable + ", " + swap_rf
                + ", " + reject_code + ", " + rxLevMin + ", " + redirect_2_4g_earfcn + ", " + mob_reject_code + ", " + split_cid + ", " + split_pci + ", " + split_offset2pointA
                + ", " + split_kssb + ", " + SplitArfcnDl + ", " + SplitArfcnUl + ", " + force_cfg);
        mMsgIdx = 0;
        for (int i = 0; i < u16MsgLength; i++) {
            sendMsg[i] = 0;
        }

        byte[] headMsg = header.getHeaderMsg();
        for (int i = 0; i < headMsg.length; i++) {
            sendMsg[i] = headMsg[i];
            mMsgIdx++;
        }
        // 以下顺序不能改变
        int2byte(cell_id);
        int2byte(MCC);
        int2byte(MNC);
        int2byte(DL_NR_ARFCN);
        int2byte(UL_NR_ARFCN);
        int2byte(PCI);
        int2byte(TAC);
        int2byte(offset2pointA);
        int2byte(kssb);
        int2byte(ue_max_pwr);
        int2byte(timing_offset);
        int2byte(work_mode);
        int2byte(air_sync_enable);
        int2byte(MCC2);
        int2byte(MNC2);
        int2byte(ul_rb_offset);
        long2byte(cid);
        int2byte(ssb_bitmap);
        int2byte(frame_type);
        int2byte(bandwidth);
        int2byte(reject_code);
        int2byte(rxLevMin);
        int2byte(redirect_2_4g_earfcn);
        int2byte(mob_reject_code);
        int2byte(split_cid);
        int2byte(split_pci);
        int2byte(split_offset2pointA);
        int2byte(split_kssb);
        int2byte(SplitArfcnDl);
        int2byte(SplitArfcnUl);
        //12*4
        for (int i = 0; i < 5 * 4; i++) {
            sendMsg[mMsgIdx] = 0;
            mMsgIdx++;
        }
        int2byte(force_cfg);
        int2byte(cfr_enable);
        int2byte(swap_rf);

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

    private void long2byte(long cid) {
        byte[] data = DataUtil.long2Bytes(cid);
        for (byte datum : data) {
            sendMsg[mMsgIdx] = datum;
            mMsgIdx++;
        }
    }

    public byte[] getMsg() {
        return sendMsg;
    }
}
