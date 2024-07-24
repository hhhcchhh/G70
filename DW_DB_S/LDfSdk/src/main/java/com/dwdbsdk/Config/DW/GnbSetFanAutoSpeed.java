/*
* typedef struct {
 int sync_header;
 int msg_type;           //UI_2_gNB_OAM_MSG(in)
 int cmd_type;             //OAM_MSG_SET_FAN_SPEED
 int cmd_param;

 int fan_id;           //0：风扇 ID
 int fan_speed;         //0-100  风扇功率增益百分比，当前设置 值以及转速在心跳数据中查询。
 int sync_footer;
} oam_rx_gain_t;
* */
package com.dwdbsdk.Config.DW;
import com.dwdbsdk.Bean.DW.DWHeader;
import com.dwdbsdk.Util.DataUtil;

public class GnbSetFanAutoSpeed {
    private final int u16MsgLength = DWHeader.headLength + 240;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public GnbSetFanAutoSpeed(DWHeader header, int[] min_temp,int[] max_temp, int[] speed_rate) {
        mMsgIdx = 0;
        for (int i = 0; i < u16MsgLength; i++) {
            sendMsg[i] = 0;
        }

        byte[] headMsg = header.getHeaderMsg();
        for (int i = 0; i < headMsg.length; i++) {
            sendMsg[i] = headMsg[i];
            mMsgIdx++;
        }

        int[] sure_min_temp = getSureData(min_temp);
        int[] sure_max_temp = getSureData(max_temp);
        int[] sure_speed_rate = getSureData(speed_rate);

        for (int data : sure_min_temp) int2byte(data);
        for (int data : sure_max_temp) int2byte(data);
        for (int data : sure_speed_rate) int2byte(data);

        byte[] footerMsg = header.getFooterMsg();
        for (byte b : footerMsg) {
            sendMsg[mMsgIdx] = b;
            mMsgIdx++;
        }
    }

    /**
     * 将传进的int数组大小 都转为 20个
     */
    private int[] getSureData(int[] data){
        int[] result = new int[20];
        if (data.length >= 20) System.arraycopy(data, 0, result, 0, 20);
        else {
            System.arraycopy(data, 0, result, 0, data.length);
            for (int i = data.length; i < 20; i++) result[i] = 0;
        }
        return result;
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
