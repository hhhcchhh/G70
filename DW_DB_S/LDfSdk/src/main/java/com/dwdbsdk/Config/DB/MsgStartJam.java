/**
 * typedef struct {
 * long lo_frequency; 8
 * int time_offset;   12
 * int scsType;		16
 * int re_cnt;20
 * int re_list[8]; 52				// re_list max = 8
 * int arfcn[8];52+32=84
 * } bt_msg_tx_t;
 * <p>
 * typedef struct {
 * int arfcn
 * long freq_carrier; 8
 * int kssb;			12
 * int offset2pointA;
 * int time_offset;
 * } bt_msg_orx_t;
 * <p>
 * typedef struct {
 * int msg_header;						//HEADER_MAGIC
 * int msg_sn;							//serial num
 * int msg_len;						//sizeof bt_msg_xxx_t
 * int msg_type;						//GR_MSG_START_JAM
 * int param;
 * <p>
 * bt_msg_tx_t tx[2];   		// tx1 tx2
 * bt_msg_orx_t orx[8]; 		// orx_list max = 8
 * int save_flag; 		    //0-no save, 1-save
 * <p>
 * int msg_footer;						//FOOTER_MAGIC
 * } bt_msg_start_jam_t;
 */
package com.dwdbsdk.Config.DB;

import com.dwdbsdk.Bean.DB.DBHeader;
import com.dwdbsdk.Bean.DB.OrxBean;
import com.dwdbsdk.Bean.DB.TxBean;
import com.dwdbsdk.Util.DataUtil;

import java.util.List;

public class MsgStartJam {
    public static int TX_LEN = 84;/* 定义消息的长度 */
    public static int ORX_LEN = 24;/* 定义消息的长度 */
    private final int u16MsgLength = DBHeader.headLength + 4 * 2 + 2 * TX_LEN + 8 * ORX_LEN;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public MsgStartJam(int msg_type, int save, TxBean tx1, TxBean tx2, List<OrxBean> orxList) {
        for (int i = 0; i < u16MsgLength; i++) {
            sendMsg[i] = 0;
        }
        mMsgIdx = 0;
        byte[] headMsg = DBHeader.getHeader(u16MsgLength, msg_type);
        for (byte value : headMsg) {
            sendMsg[mMsgIdx] = value;
            mMsgIdx++;
        }
        // 此处顺序不能变
        int2byte(0); // 20
        if (tx1 != null) {
            long2byte(tx1.getLoFreq());
            int2byte(tx1.getTimeOffset());
            int2byte(tx1.getScsType());
            int2byte(tx1.getSsbNum());
            List<Integer> relist = tx1.getDdsList();
            for (int i = 0; i < 8; i++) {
                if (i >= tx1.getSsbNum()) {
                    int2byte(0);
                } else {
                    int2byte(relist.get(i));
                }
            }
            List<Integer> arfcnlist = tx1.getArfcnList();
            for (int i = 0; i < 8; i++) {
                if (i >= tx1.getSsbNum()) {
                    int2byte(0);
                } else {
                    int2byte(arfcnlist.get(i));
                }
            }
        }
        mMsgIdx = 20 + TX_LEN;  // 20 + 84
        if (tx2 != null) {
            long2byte(tx2.getLoFreq());
            int2byte(tx2.getTimeOffset());
            int2byte(tx2.getScsType());
            int2byte(tx2.getSsbNum());
            List<Integer> relist = tx2.getDdsList();
            for (int i = 0; i < 8; i++) {
                if (i >= tx2.getSsbNum()) {
                    int2byte(0);
                } else {
                    int2byte(relist.get(i));
                }
            }
            List<Integer> arfcnlist = tx2.getArfcnList();
            for (int i = 0; i < 8; i++) {
                if (i >= tx2.getSsbNum()) {
                    int2byte(0);
                } else {
                    int2byte(arfcnlist.get(i));
                }
            }
        }
        mMsgIdx = 20 + TX_LEN + TX_LEN;
        for (int i = 0; i < orxList.size(); i++) {
            int2byte(orxList.get(i).getArfcn());
            long2byte(orxList.get(i).getFreqCarrier());
            int2byte(orxList.get(i).getPk());
            int2byte(orxList.get(i).getPa());
            int2byte(orxList.get(i).getTimeOffset());
        }
        mMsgIdx = u16MsgLength - 8;
        int2byte(save);
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
