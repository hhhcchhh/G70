package com.nr.Socket.MessageControl;

import android.text.TextUtils;

import com.Logcat.SLog;
import com.nr.Gnb.Bean.GnbProtocol;
import com.nr.Gnb.Response.GnbCatchCfgRsp;
import com.nr.Gnb.Response.GnbCmdRsp;
import com.nr.Gnb.Response.GnbFreqScanGetDocumentRsp;
import com.nr.Gnb.Response.GnbFreqScanRsp;
import com.nr.Gnb.Response.GnbFtpRsp;
import com.nr.Gnb.Response.GnbGetSysInfoRsp;
import com.nr.Gnb.Response.GnbGpioRsp;
import com.nr.Gnb.Response.GnbGpsInOutRsp;
import com.nr.Gnb.Response.GnbGpsRsp;
import com.nr.Gnb.Response.GnbMethIpRsp;
import com.nr.Gnb.Response.GnbReadDataFwdRsp;
import com.nr.Gnb.Response.GnbStateRsp;
import com.nr.Gnb.Response.GnbTraceRsp;
import com.nr.Gnb.Response.GnbUserDataRsp;
import com.nr.Gnb.Response.GnbVersionRsp;
import com.nr.Util.Battery;
import com.nr.Util.DataUtil;

import java.util.HashMap;
import java.util.Map;

public class MessageHelper {
    private static MessageHelper instance;
    private String hostIp = "0.0.0.0";
    private String deviceId = "";

    public static MessageHelper build() {
        synchronized (MessageHelper.class) {
            if (instance == null) {
                instance = new MessageHelper();
            }
        }
        return instance;
    }

    public MessageHelper() {
    }

    public String getHostIp() {
        SLog.D("hostIp: " + hostIp);
        return hostIp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        if (!TextUtils.isEmpty(deviceId)){
            this.deviceId = deviceId;
        }
    }

    /**
     * 心跳数据解析
     */
    public GnbStateRsp gnbHeartState(String id, String source_data) {
        // 0: 90 00 00 00  sync_header
        // 4: e8 03 00 00  msg_type
        // 8: 01 00 00 00  cmd_type
        // 12: 00 00 00 00 cmd_param
        // 16: 00 00 00 00 00 00 00 00  gnb_state
        // 24: 00 00 00 00  gps_sync_state
        // 28: 01 00 00 00  time_sync_state
        // 32: 00 00 00 00  air_sync_state
        // 36: 32 35 35 2e 32 35 35 2e 32 35 35 2e 32 35 35 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 wifi_ip
        // 68: 22 73 6f 66 74 5f 63 61 69 22 0a 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 wifi_ssid
        // 100: 39 30 30 31 34 61 36 38 34 34 34 35 36 31 35 30 33 33 30 33 31 30 33 62 37 37 38 65 36 38 30 30 0a 00 00 00  dev_id
        // 136: 22 73 6f 66 74 5f 63 61 69 22 0a 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 bt_name
        // 168: 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00  voltage
        // 184: 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00  temp
        // 200: 01 00 00 00  work_mode
        // 204: 00 00 00 00  dev_state
        // 208: 00 00 66 ff
        GnbStateRsp state = null;
        String[] data = source_data.split(",");
        if (data.length > 20) {
            int idx = 8;
            String s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
            int cmd = DataUtil.str2Int(s, 16);
            if (cmd == GnbProtocol.UI_2_gNB_HEART_BEAT) { // 心跳标志
                idx = 16;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                int cell_1_state = DataUtil.str2Int(s, 16);

                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                int cell_2_state = DataUtil.str2Int(s, 16);

                // GPS 同步状态
                int gps_sync_state = 0;
                idx += 4;
                if ((idx + 3) < data.length) {
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    gps_sync_state = DataUtil.str2Int(s, 16);
                }

                int time_sync_state = 0;
                idx += 4;
                if ((idx + 3) < data.length) {
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    time_sync_state = DataUtil.str2Int(s, 16);
                }

                int fair_sync_state = 0;  //通道一空口状态
                int sair_sync_state = 0;  //通道二空口状态
                idx += 4;
                if ((idx + 3) < data.length) {
                    s = data[idx + 3] + data[idx + 2];// + data[idx + 1] + data[idx];
                    sair_sync_state = DataUtil.str2Int(s, 16); // cell 1
                    s = data[idx + 1] + data[idx];
                    fair_sync_state = DataUtil.str2Int(s, 16); // cell 0
                }

                StringBuilder sbIp = new StringBuilder();
                idx += 4;
                for (int i = idx; i < idx + GnbProtocol.OAM_STR_MAX; i++) {
                    if (i >= data.length) {
                        break;
                    }
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        if (data[i].equals("2e")) {
                            sbIp.append(".");
                        } else {
                            sbIp.append(data[i].substring(1));
                        }
                    }
                }
                hostIp = sbIp.toString();
                if (hostIp.equals("255.255.255.255")) hostIp = MessageController.build().getIpFromMsgTypeList(id);

                byte[] bssid = new byte[GnbProtocol.OAM_STR_MAX];
                int ret = 0;
                idx += GnbProtocol.OAM_STR_MAX;
                for (int i = idx; i < idx + GnbProtocol.OAM_STR_MAX; i++) {
                    if (i >= data.length) {
                        break;
                    }
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        bssid[ret] = Byte.valueOf(data[i], 16);
                        ret++;
                    }
                }
                String ssid = DataUtil.Asc2String(bssid);
                ssid = ssid.substring(0, ret);
                ssid = ssid.replaceAll("\"", "");
                ssid = ssid.replaceAll("\r|\n", "");
                byte[] device = new byte[36];
                ret = 0;
                idx += GnbProtocol.OAM_STR_MAX;
                for (int i = idx; i < idx + 36; i++) {
                    if (i >= data.length) {
                        break;
                    }
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        device[ret] = Byte.valueOf(data[i], 16);
                        ret++;
                    }
                }
                String deviceId = DataUtil.Asc2String(device);
                deviceId = deviceId.substring(0, ret);
                deviceId = deviceId.replaceAll("\r|\n", "");
                setDeviceId(deviceId);
                byte[] bbt = new byte[GnbProtocol.OAM_STR_MAX];
                ret = 0;
                idx += 36;
                for (int i = idx; i < idx + GnbProtocol.OAM_STR_MAX; i++) {
                    if (i >= data.length) {
                        break;
                    }
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        bbt[ret] = Byte.valueOf(data[i], 16);
                        ret++;
                    }
                }
                String bt_name = DataUtil.Asc2String(bbt);
                bt_name = bt_name.substring(0, ret);
                //bt_name = bt_name.replaceAll("\"", "");
                bt_name = bt_name.replaceAll("\r|\n", "");

                if (!MessageController.build().isDoStop){
                    if (cell_1_state == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                        if (!MessageController.build().isEnableChangeTac(id, 0)) {
                            MessageController.build().setEnableChangeTac(id, 0, true);
                            MessageController.build().setTraceType(id, GnbProtocol.TraceType.TRACE);
                        }
                    } else if (cell_1_state == GnbStateRsp.gnbState.GNB_STATE_CONTROL) {
                        if (!MessageController.build().isEnableChangeTac(id, 0)) {
                            MessageController.build().setEnableChangeTac(id, 0, true);
                            MessageController.build().setTraceType(id, GnbProtocol.TraceType.CONTROL);
                        }
                    } else if (cell_1_state == GnbStateRsp.gnbState.GNB_STATE_CATCH) {
                        if (!MessageController.build().isEnableChangeTac(id, 0)) {
                            MessageController.build().setEnableChangeTac(id, 0, true);
                            MessageController.build().setTraceType(id, GnbProtocol.TraceType.CATCH);
                        }
                    }

                    if (cell_2_state == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                        if (!MessageController.build().isEnableChangeTac(id, 1)) {
                            MessageController.build().setEnableChangeTac(id, 1, true);
                            MessageController.build().setTraceType(id, GnbProtocol.TraceType.TRACE);
                        }
                    } else if (cell_2_state == GnbStateRsp.gnbState.GNB_STATE_CONTROL) {
                        if (!MessageController.build().isEnableChangeTac(id, 1)) {
                            MessageController.build().setEnableChangeTac(id, 1, true);
                            MessageController.build().setTraceType(id, GnbProtocol.TraceType.CONTROL);
                        }
                    } else if (cell_2_state == GnbStateRsp.gnbState.GNB_STATE_CATCH) {
                        if (!MessageController.build().isEnableChangeTac(id, 1)) {
                            MessageController.build().setEnableChangeTac(id, 1, true);
                            MessageController.build().setTraceType(id, GnbProtocol.TraceType.CATCH);
                        }
                    }
                }

                state = new GnbStateRsp(cell_1_state, cell_2_state, gps_sync_state, time_sync_state, fair_sync_state,
                        sair_sync_state, hostIp, ssid, deviceId, bt_name);
                int vol = 0;
                idx += GnbProtocol.OAM_STR_MAX;
                if ((idx + 3) < data.length) {
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    vol = (int) Long.parseLong(s, 16);
                }
                state.addVol(vol);
                idx += 4;
                if ((idx + 3) < data.length) {
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    vol = (int) Long.parseLong(s, 16);
                    // 多个板子的心跳都走这里，就会影响电压电量检测的唯一性，在此注释，由上层来调用
                    //Battery.build().handleVol(vol);
                }
                state.addVol(vol);
                idx += 4;
                if ((idx + 3) < data.length) {
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    vol = (int) Long.parseLong(s, 16);
                }
                state.addVol(vol);
                idx += 4;
                if ((idx + 3) < data.length) {
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    vol = (int) Long.parseLong(s, 16);
                }
                state.addVol(vol);
                int temp = 0;
                idx += 4;
                if ((idx + 3) < data.length) {
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    temp = (int) Long.parseLong(s, 16);
                }
                state.addTemp(temp);
                idx += 4;
                if ((idx + 3) < data.length) {
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    temp = (int) Long.parseLong(s, 16);
                }
                state.addTemp(temp);
                idx += 4;
                if ((idx + 3) < data.length) {
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    temp = (int) Long.parseLong(s, 16);
                }
                state.addTemp(temp);
                idx += 4;
                if ((idx + 3) < data.length) {
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    temp = (int) Long.parseLong(s, 16);
                }
                state.addTemp(temp);

                int work_mode = 0;
                idx += 4;
                if ((idx + 3) < data.length) {
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    work_mode = DataUtil.str2Int(s, 16);
                }
                state.setWorkMode(work_mode);

                int dev_state = 0;
                idx += 4;
                if ((idx + 3) < data.length) {
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    dev_state = DataUtil.str2Int(s, 16);
                }
                state.setDevState(dev_state);

                byte[] b_dev_name = new byte[GnbProtocol.OAM_STR_MAX];
                ret = 0;
                idx += 4;
                for (int i = idx; i < idx + GnbProtocol.OAM_STR_MAX; i++) {
                    if (i >= data.length) {
                        break;
                    }
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        b_dev_name[ret] = Byte.valueOf(data[i], 16);
                        ret++;
                    }
                }
                String dev_name = DataUtil.Asc2String(b_dev_name);
                dev_name = dev_name.substring(0, ret);
                dev_name = dev_name.replaceAll("\"", "");
                dev_name = dev_name.replaceAll("\r|\n", "");
                state.setDevName(dev_name);

                double lon = 0.0;
                idx += GnbProtocol.OAM_STR_MAX;
                if ((idx + 3) < data.length) {
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    temp = (int) Long.parseLong(s, 16);
                    lon = temp / 1000.0;
                }
                state.setLongitude(lon);

                double lat = 0.0;
                idx += 4;
                if ((idx + 3) < data.length) {
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    temp = (int) Long.parseLong(s, 16);
                    lat = temp / 1000.0;
                }
                state.setLatitude(lat);

                int dual_cell = -1;    // 1-single, 2-dual
                idx += 4;
                if ((idx + 3) < data.length) {
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    dual_cell = DataUtil.str2Int(s, 16);
                }
                state.setDualCell(dual_cell);
                int gnss_select = 0;    // 0-GpsAndBeidou, 1-GPS, 2-Beidou
                idx += 4;
                if ((idx + 3) < data.length) {
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    gnss_select = DataUtil.str2Int(s, 16);
                }
                state.setGnss_select(gnss_select);
                int sys_kick_off = 0;  // 0-init，1-kickoff  状态等于 0，系统处于启动中，不处理 指令
                idx += 4;
                if ((idx + 3) < data.length) {
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    sys_kick_off = DataUtil.str2Int(s, 16);
                }
                state.setSysKickOff(sys_kick_off);
                //0-15: 0-100, 16-31: RPM  风扇转速和风扇控制百分比值
                int[] fan_struct = new int[2];
                idx += 4;
                if ((idx + 3) < data.length) {
                    //s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    s = data[idx + 1] + data[idx];
                    int fan_setting = DataUtil.str2Int(s, 16);
                    fan_struct[0] = fan_setting;
                    s = data[idx + 3] + data[idx + 2];
                    int fan_speed = DataUtil.str2Int(s, 16);
                    fan_struct[1] = fan_speed;
                }
                state.setFanSpeed(fan_struct);

                idx += 4;
                int cell_3_state = -1;
                if ((idx + 3) < data.length) {
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    cell_3_state = DataUtil.str2Int(s, 16);
                }
                state.setThirdState(cell_3_state);
                idx += 4;
                int cell_4_state = -1;
                if ((idx + 3) < data.length) {
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    cell_4_state = DataUtil.str2Int(s, 16);
                }
                state.setFourthState(cell_4_state);

                if (!MessageController.build().isDoStop){
                    if (cell_3_state == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                        if (!MessageController.build().isEnableChangeTac(id, 2)) {
                            MessageController.build().setEnableChangeTac(id, 2, true);
                            MessageController.build().setTraceType(id, GnbProtocol.TraceType.TRACE);
                        }
                    } else if (cell_3_state == GnbStateRsp.gnbState.GNB_STATE_CONTROL) {
                        if (!MessageController.build().isEnableChangeTac(id, 2)) {
                            MessageController.build().setEnableChangeTac(id, 2, true);
                            MessageController.build().setTraceType(id, GnbProtocol.TraceType.CONTROL);
                        }
                    } else if (cell_3_state == GnbStateRsp.gnbState.GNB_STATE_CATCH) {
                        if (!MessageController.build().isEnableChangeTac(id, 2)) {
                            MessageController.build().setEnableChangeTac(id, 2, true);
                            MessageController.build().setTraceType(id, GnbProtocol.TraceType.CATCH);
                        }
                    }

                    if (cell_4_state == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                        if (!MessageController.build().isEnableChangeTac(id, 3)) {
                            MessageController.build().setEnableChangeTac(id, 3, true);
                            MessageController.build().setTraceType(id, GnbProtocol.TraceType.TRACE);
                        }
                    } else if (cell_4_state == GnbStateRsp.gnbState.GNB_STATE_CONTROL) {
                        if (!MessageController.build().isEnableChangeTac(id, 3)) {
                            MessageController.build().setEnableChangeTac(id, 3, true);
                            MessageController.build().setTraceType(id, GnbProtocol.TraceType.CONTROL);
                        }
                    } else if (cell_4_state == GnbStateRsp.gnbState.GNB_STATE_CATCH) {
                        if (!MessageController.build().isEnableChangeTac(id, 3)) {
                            MessageController.build().setEnableChangeTac(id, 3, true);
                            MessageController.build().setTraceType(id, GnbProtocol.TraceType.CATCH);
                        }
                    }
                }

                int tair_sync_state = 0;  //通道三空口状态
                int hair_sync_state = 0;  //通道四空口状态
                idx += 4;
                // 00 01 00 02
                if ((idx + 3) < data.length) {
                    s = data[idx + 3] + data[idx + 2];// + data[idx + 1] + data[idx];
                    hair_sync_state = DataUtil.str2Int(s, 16); // cell 3
                    s = data[idx + 1] + data[idx];
                    tair_sync_state = DataUtil.str2Int(s, 16); // cell 2
                }
                state.setThirdAirState(tair_sync_state);
                state.setFourthAirState(hair_sync_state);
                int dual_stack = 0;  // 0-single stack，1-dual stack
                idx += 4;
                if ((idx + 3) < data.length) {
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    dual_stack = DataUtil.str2Int(s, 16);
                }
                state.setDualStack(dual_stack);
            }
        }
        return state;
    }

    /**
     * 各指令配置反馈信息解析
     */
    public GnbCmdRsp gnbCmdAck(String source_data) {
        return gnbCmdAck(null, source_data);
    }

    public GnbCmdRsp gnbCmdAck(String id, String source_data) {
        /* 指令配置返回信息带CELL_ID
         * typedef struct {
         * 		int sync_header;
         * 		int msg_type;          	//UI_2_gNB_OAM_MSG
         * 		int cmd_type;
         * 		int cmd_result;
         *
         * 		int cell_id; // 此字段不一定有
         *
         * 		int sync_footer;
         * } oam_start_catch_t, oam_stop_catch_t; //in&out
         * */
        GnbCmdRsp state = null;
        String[] data = source_data.split(",");
        if (data.length > 19) {
            int idx = 4;
            String s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
            int msg_type = DataUtil.str2Int(s, 16);

            idx = 8;
            s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
            int cmd_type = DataUtil.str2Int(s, 16);

            if (msg_type == GnbProtocol.UI_2_gNB_OAM_MSG) { // 指令响应上报
                idx = 12;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                int cmd_result = DataUtil.str2Int(s, 16);

                int cell_id;
                idx = 16;
                if (data[idx + 1].equals("66") && data[idx].equals("ff")) {
                    cell_id = -1;
                } else {
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    cell_id = DataUtil.str2Int(s, 16);
                }
                //int msgType, int cmdType, int rspValue, int cellId
                state = new GnbCmdRsp(cell_id, msg_type, cmd_type, cmd_result);
                if (id != null && cmd_result == GnbProtocol.OAM_ACK_OK) {
                    switch (cmd_type) {
                        case GnbProtocol.UI_2_gNB_STOP_CATCH:
                        case GnbProtocol.UI_2_eNB_STOP_CATCH:
                        case GnbProtocol.UI_2_gNB_STOP_TRACE:
                        case GnbProtocol.UI_2_gNB_STOP_LTE_TRACE:
                        case GnbProtocol.UI_2_gNB_STOP_CONTROL:
                        case GnbProtocol.UI_2_eNB_STOP_CONTROL:
                            MessageController.build().setTracing(id, cell_id, false);
                            break;
                    }
                }
            }
        }
        return state;
    }

    /**
     * 配置定位参数反馈信息
     */
    public GnbCmdRsp gnbCfgGnb(String source_data) {
        return gnbCmdAck(source_data);
    }

    /**
     * 配置发射功率反馈信息
     */
    public GnbCmdRsp gnbSetTxPwrOffset(String source_data) {
        return gnbCmdAck(source_data);
    }

    /**
     * 配置NV发射功率反馈信息
     */
    public GnbCmdRsp gnbSetNvTxPwrOffset(String source_data) {
        return gnbCmdAck(source_data);
    }

    /**
     * 配置开启侦码反馈信息
     */
    public GnbTraceRsp gnbStartCatch(String id, String source_data) {
        /*
          typedef struct {
          	int sync_header;
          	int msg_type; // gNB_2_UI_REPORT_UE_INFO
          	int cell_id;
          	ue_id_t ue;
          	int rsrp;     // -1为掉线
          	int rssi;
          	int distance; // m
          	int sync_footer;
          } ue_report_t;
         */
        // 3c,00,00,00,
        // 67,00,00,00,msg_type
        // 00,00,00,00,cell_id
        //
        // 34,36,30,30,30,35,31,30,32,31,36,36,35,32,30,00,imsi
        // 63,65,34,37,38,38,66,34,00,00,00,00,00,00,00,00,guti
        // 40,00,00,00,rsrp
        // 00,00,00,00,rssi
        // 0a,00,00,00,distance
        // 0a,00,00,00,rnti
        // ff,66,00,00
        String[] data = source_data.split(",");
        StringBuilder imsi = new StringBuilder();
        GnbTraceRsp traceRsp = new GnbTraceRsp();
        String curImsi;
        int idx = 4;
        String s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
        int msg_type = DataUtil.str2Int(s, 16);
        if (msg_type == GnbProtocol.gNB_2_UI_REPORT_UE_INFO || msg_type == GnbProtocol.gNB_2_UI_LTE_REPORT_UE_INFO) {// 定位103
            idx = 8;
            int cellId;
            if (idx < data.length) {
                String a = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                cellId = DataUtil.str2Int(a, 16);
                traceRsp.setCellId(cellId);
            }
            idx += 4;
            for (int i = idx; i < idx + GnbProtocol.MAX_IMSI_LEN; i++) {
                if (i >= data.length) {
                    break;
                }
                if (data[i].equals("00")) {
                    break;
                } else {
                    imsi.append(data[i].substring(1));
                }
            }
            int rsrp, rssi, distance;
            curImsi = imsi.toString();
            if (curImsi.length() == 15) {
                idx += GnbProtocol.MAX_IMSI_LEN * 2;
                if (idx >= data.length) return null;
                if (data[idx].equals("ff")) rsrp = -1; // 掉线
                else {
                    String a = data[idx];
                    rsrp = DataUtil.str2Int(a, 16);
                    // 数据处理范围最大值：100
                    double value = rsrp * 1.1 + 3;
                    rsrp = (int) Math.abs(value);
                }
                idx += 4;
                if (idx >= data.length) rssi = 0;
                else {
                    if (data[idx].equals("ff")) rssi = -1; // 掉线
                    else {
                        String a = data[idx];
                        rssi = DataUtil.str2Int(a, 16);
                        double value = rssi * 1.1 + 3;
                        rssi = (int) Math.abs(value);
                    }
                }
                idx += 4;
                distance = 0;
                if (idx < data.length) {
                    String a = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    distance = DataUtil.str2Int(a, 16);
                }
                traceRsp.setRsrp(rsrp);
                traceRsp.setRssi(rssi);
                traceRsp.setDistance(distance);
                idx += 4;
                int rnti = 0;
                if (idx < data.length) {
                    String a = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    rnti = DataUtil.str2Int(a, 16);
                }
                traceRsp.setRnti(rnti);
                traceRsp.addImsi(imsi.toString());
                idx += 4;
                int phone_type = 0;
                if (idx < data.length) {
                    if (!data[idx].equals("ff")) {
                        String a = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        phone_type = DataUtil.str2Int(a, 16);
                    }
                }
                traceRsp.setPhone_type(phone_type);
            }
        } else if (msg_type == GnbProtocol.UI_2_gNB_OAM_MSG) { // 心跳标志
            GnbCmdRsp state = gnbCmdAck(source_data);
            if (null != state) {
                if (state.getCmdType() == GnbProtocol.UI_2_gNB_START_CATCH || state.getCmdType() == GnbProtocol.UI_2_eNB_START_CATCH) {
                    if (state.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                        MessageController.build().setTracing(id, state.getCellId(), true);
                    }
                }
                traceRsp.setCmdRsp(state);
                return traceRsp;
            }
        }
        return traceRsp;
    }

    /**
     * 结束侦码反馈信息
     */
    public GnbCmdRsp gnbStopCatch(String id, String source_data) {
        return gnbCmdAck(id, source_data);
    }

    /**
     * 解析定位反馈数据
     */
    public GnbTraceRsp gnbStartTrace(String id, String source_data) {
        /*
          typedef struct {
          	int sync_header;
          	int msg_type; // gNB_2_UI_REPORT_UE_INFO
          	int cell_id;
          	ue_id_t ue;
          	int rsrp;     // -1为掉线
          	int rssi;
          	int distance; // m
          	int sync_footer;
          	int rnti;
          } ue_report_t;
         */
        // 3c,00,00,00,
        // 67,00,00,00,msg_type
        // 00,00,00,00,cell_id
        //
        // 34,36,30,30,30,35,31,30,32,31,36,36,35,32,30,00,imsi
        // 63,65,34,37,38,38,66,34,00,00,00,00,00,00,00,00,guti
        // 40,00,00,00,rsrp
        // 00,00,00,00,rssi
        // 0a,00,00,00,distance
        // 0a,00,00,00,rnti
        // ff,66,00,00
        String[] data = source_data.split(",");
        StringBuilder imsi = new StringBuilder();
        StringBuilder guti = new StringBuilder();
        GnbTraceRsp traceRsp = new GnbTraceRsp();
        String curImsi;
        int idx = 4;
        String s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
        int msg_type = DataUtil.str2Int(s, 16);
        if (msg_type == GnbProtocol.gNB_2_UI_REPORT_UE_INFO || msg_type == GnbProtocol.gNB_2_UI_LTE_REPORT_UE_INFO) {// 定位103
            idx = 8;
            int cellId;
            if (idx < data.length) {
                String a = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                cellId = DataUtil.str2Int(a, 16);
                traceRsp.setCellId(cellId);
            }
            idx += 4;
            for (int i = idx; i < idx + GnbProtocol.MAX_IMSI_LEN; i++) {
                if (i >= data.length) break;
                if (data[i].equals("00")) break;
                else imsi.append(data[i].substring(1));
            }
            for (int i = idx + GnbProtocol.MAX_IMSI_LEN; i < idx + GnbProtocol.MAX_IMSI_LEN * 2; i++) {
                if (i >= data.length) break;
                if (data[i].equals("00")) break;
                else {
                    int decimal = DataUtil.str2Int(data[i], 16);
                    char character = (char) decimal; // 转换为字符
                    guti.append(character);
                }
            }
            int rsrp, rssi, distance;
            curImsi = imsi.toString();
            if (curImsi.length() == 15 || guti.length() > 7) {
                idx += GnbProtocol.MAX_IMSI_LEN * 2;
                if (idx >= data.length) return null;
                if (data[idx].equals("ff")) rsrp = -1; // 掉线
                else {
                    String a = data[idx];
                    rsrp = DataUtil.str2Int(a, 16);
                    // 数据处理范围最大值：100
                    double value = rsrp * 1.1 + 3;
                    rsrp = (int) Math.abs(value);
                }
                idx += 4;
                if (idx >= data.length) rssi = 0;
                else {
                    if (data[idx].equals("ff")) rssi = -1; // 掉线
                    else {
                        String a = data[idx];
                        rssi = DataUtil.str2Int(a, 16);
                        double value = rssi * 1.1 + 3;
                        rssi = (int) Math.abs(value);
                    }
                }
                idx += 4;
                distance = 0;
                if (idx < data.length) {
                    String a = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    distance = DataUtil.str2Int(a, 16);
                }
                traceRsp.setRsrp(rsrp);
                traceRsp.setRssi(rssi);
                traceRsp.setDistance(distance);
                idx += 4;
                int rnti = 0;
                if (idx < data.length) {
                    String a = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    rnti = DataUtil.str2Int(a, 16);
                }
                traceRsp.setRnti(rnti);
                traceRsp.addImsi(imsi.toString());
                traceRsp.addGuti(guti.toString());
                idx += 4;
                int phone_type = 0;
                if (idx < data.length) {
                    if (!data[idx].equals("ff")) {
                        String a = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        phone_type = DataUtil.str2Int(a, 16);
                    }
                }
                traceRsp.setPhone_type(phone_type);
            }
        } else if (msg_type == GnbProtocol.UI_2_gNB_OAM_MSG) { // 心跳标志
            GnbCmdRsp state = gnbCmdAck(source_data);
            if (null != state) {
                if (state.getCmdType() == GnbProtocol.UI_2_gNB_START_LTE_TRACE || state.getCmdType() == GnbProtocol.UI_2_gNB_START_TRACE) {
                    if (state.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                        MessageController.build().setTracing(id, state.getCellId(), true);
                    }
                }
                traceRsp.setCmdRsp(state);
                return traceRsp;
            }
        }
        return traceRsp;
    }

    /**
     * 配置结束定位反馈信息
     */
    public GnbCmdRsp gnbStopTrace(String id, String source_data) {
        return gnbCmdAck(id, source_data);
    }

    /**
     * 管控模式反馈信息
     */
    public GnbTraceRsp gnbStartControl(String id, String source_data) {
        /*
         * typedef struct {
         * 	int sync_header;
         * 	int msg_type; // gNB_2_UI_REPORT_UE_INFO
         * 	int cell_id;
         * 	ue_id_t ue;
         * 	int rsrp;     // -1为掉线
         * 	int rssi;
         * 	int distance; // m
         * 	int sync_footer;
         * } ue_report_t;
         */
        // 3c,00,00,00,
        // 67,00,00,00,msg_type
        // 00,00,00,00,cell_id
        //
        // 34,36,30,30,30,35,31,30,32,31,36,36,35,32,30,00,imsi
        // 63,65,34,37,38,38,66,34,00,00,00,00,00,00,00,00,guti
        // 40,00,00,00,rsrp
        // 00,00,00,00,rssi
        // 0a,00,00,00,distance
        // ff,66,00,00
        String[] data = source_data.split(",");
        StringBuilder imsi = new StringBuilder();
        GnbTraceRsp traceRsp = new GnbTraceRsp();
        String curImsi;
        int idx = 4;
        String s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
        int msg_type = DataUtil.str2Int(s, 16);
        if (msg_type == GnbProtocol.gNB_2_UI_REPORT_UE_INFO || msg_type == GnbProtocol.gNB_2_UI_LTE_REPORT_UE_INFO) {// 定位103
            idx = 8;
            int cellId;
            if (idx < data.length) {
                String a = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                cellId = DataUtil.str2Int(a, 16);
                traceRsp.setCellId(cellId);
            }
            idx += 4;
            for (int i = idx; i < idx + GnbProtocol.MAX_IMSI_LEN; i++) {
                if (i >= data.length) break;
                if (data[i].equals("00")) break;
                else imsi.append(data[i].substring(1));
            }
            int rsrp, rssi, distance;
            curImsi = imsi.toString();
            if (curImsi.length() == 15) {
                idx += GnbProtocol.MAX_IMSI_LEN * 2;
                if (idx >= data.length) return null;
                if (data[idx].equals("ff")) rsrp = -1; // 掉线
                else {
                    String a = data[idx];
                    rsrp = DataUtil.str2Int(a, 16);
                    // 数据处理范围最大值：100
                    double value = rsrp * 1.1 + 3;
                    rsrp = (int) Math.abs(value);
                }
                idx += 4;
                if (idx >= data.length) rssi = 0;
                else {
                    if (data[idx].equals("ff")) rssi = -1; // 掉线
                    else {
                        String a = data[idx];
                        rssi = DataUtil.str2Int(a, 16);
                        double value = rssi * 1.1 + 3;
                        rssi = (int) Math.abs(value);
                    }
                }
                idx += 4;
                distance = 0;
                if (idx < data.length) {
                    String a = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    distance = DataUtil.str2Int(a, 16);
                }
                traceRsp.setRsrp(rsrp);
                traceRsp.setRssi(rssi);
                traceRsp.setDistance(distance);
                traceRsp.addImsi(imsi.toString());
                idx += 4;
                int rnti = 0;
                if (idx < data.length) {
                    String a = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    rnti = DataUtil.str2Int(a, 16);
                }
                traceRsp.setRnti(rnti);
                idx += 4;
                int phone_type = 0;
                if (idx < data.length) {
                    if (!data[idx].equals("ff")) {
                        String a = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        phone_type = DataUtil.str2Int(a, 16);
                    }
                }
                traceRsp.setPhone_type(phone_type);
            }
        } else if (msg_type == GnbProtocol.UI_2_gNB_OAM_MSG) { // 心跳标志
            GnbCmdRsp state = gnbCmdAck(source_data);
            if (null != state) {
                if (state.getCmdType() == GnbProtocol.UI_2_gNB_START_CONTROL || state.getCmdType() == GnbProtocol.UI_2_eNB_START_CONTROL) {
                    if (state.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                        MessageController.build().setTracing(id, state.getCellId(), true);
                    }
                }
                traceRsp.setCmdRsp(state);
                return traceRsp;
            }
        }
        return traceRsp;
    }

    /**
     * 结束管控模式
     */
    public GnbCmdRsp gnbStopControl(String id, String source_data) {
        return gnbCmdAck(id, source_data);
    }

    /**
     * 配置基带重启反馈信息
     */
    public GnbCmdRsp gnbReboot(String source_data) {
        return gnbCmdAck(source_data);
    }

    /**
     * 查询侦码配置
     */
    public GnbCatchCfgRsp gnbGetCatchCfg(String source_data) {
        GnbCatchCfgRsp catchCfg = new GnbCatchCfgRsp();
        String[] data = source_data.split(",");
        if (data.length >= 35) {
            int idx = 8;
            String s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
            int cmd_type = DataUtil.str2Int(s, 16);
            if (cmd_type == GnbProtocol.OAM_MSG_GET_CATCH_CFG) { //
                idx = 12;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                catchCfg.setRspValue(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                catchCfg.setCellId(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                catchCfg.setSaveFlag(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                catchCfg.setStartTac(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                catchCfg.setEndTac(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                catchCfg.setTacInterval(DataUtil.str2Int(s, 16));
                Map<String, String> Gnb_cfg = new HashMap<>();
                idx += 20;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("Cell_id", DataUtil.str2str(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("MCC", DataUtil.str2str(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("MNC", DataUtil.str2str(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("DL_NR_ARFCN", DataUtil.str2str(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("UL_NR_ARFCN", DataUtil.str2str(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("PCI", DataUtil.str2str(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("TAC", DataUtil.str2str(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("PA", DataUtil.str2str(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("PK", DataUtil.str2str(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("ue_max_pwr", DataUtil.str2str(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("timing_offset", DataUtil.str2str(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("work_mode", DataUtil.str2str(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("air_sync_enable", DataUtil.str2str(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("MCC2", DataUtil.str2str(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("MNC2", DataUtil.str2str(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("ul_rb_offset", DataUtil.str2str(s, 16));
                idx += 4;
                s = data[idx + 7] + data[idx + 6] + data[idx + 5] + data[idx + 4] + data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("cid", DataUtil.str2str(s, 16));
                idx += 8;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("ssb", DataUtil.str2str(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("frame_type", DataUtil.str2str(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("bandwidth", DataUtil.str2str(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("reject_code", DataUtil.str2str(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("rxLevMin", DataUtil.str2str(s, 16));
                idx += 56; //resv[14]  预留
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("cfr", DataUtil.str2str(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("swap_rf", DataUtil.str2str(s, 16));
                catchCfg.setGnbCfg(Gnb_cfg);
                SLog.D(catchCfg.toString());
            }
        }
        return catchCfg;
    }

    /**
     * 解析基带版本
     */
    public GnbVersionRsp gnbQueryVersion(String source_data) {
        /*
         * typedef struct {
         * int sync_header;
         * int msg_type;          		//UI_2_gNB_OAM_MSG
         * int cmd_type;				//UI_2_gNB_QUERY_gNB_VERSION
         * int cmd_param;
         *
         * char hw_ver[OAM_STR_MAX];
         * char fpga_ver[OAM_STR_MAX];
         * char sw_ver[OAM_STR_MAX];
         *
         * int sync_footer;
         * } oam_get_version_t; //out
         * */
        // 0: 11 11 a5 5a sync_header
        // 4: e8 03 00 00 msg_type
        // 8: 12 00 00 00 cmd_type
        // 12: 61 63 6b 62 cmd_param
        // 16: 56 31 2e 30 00 00 00 00 00 00 00 ff ff ff ff ff 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00  hw_ver
        // 48: 30 78 30 33 31 36 34 30 35 30 00 40 01 04 10 40 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00  fpga_ver
        // 80: 47 37 30 5f 56 31 52 31 5f 32 30 32 32 30 34 31 39 5f 31 34 35 30 0a 00 12 00 00 00 00 00 00 00  sw_ver
        // 00 00 66 ff
        SLog.I("gnbQueryVersion");
        GnbVersionRsp versionRsp = new GnbVersionRsp();
        String[] data = source_data.split(",");
        if (data.length >= 35) {
            int idx = 8;
            String s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
            int cmd_type = DataUtil.str2Int(s, 16);
            if (cmd_type == GnbProtocol.UI_2_gNB_QUERY_gNB_VERSION) { //
                //hw_ver
                byte[] hw_buffer = new byte[GnbProtocol.OAM_STR_MAX];
                int ret = 0;
                idx = 16;
                for (int i = idx; i < idx + GnbProtocol.OAM_STR_MAX; i++) {
                    if (i >= data.length) {
                        break;
                    }
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        hw_buffer[ret] = Byte.valueOf(data[i], 16);
                        ret++;
                    }
                }
                String hw_ver = DataUtil.Asc2String(hw_buffer);
                hw_ver = hw_ver.substring(0, ret);
                versionRsp.setHwVer(hw_ver);
                //fpga_ver
                byte[] fpga_buffer = new byte[GnbProtocol.OAM_STR_MAX];
                ret = 0;
                idx += GnbProtocol.OAM_STR_MAX;
                for (int i = idx; i < idx + GnbProtocol.OAM_STR_MAX; i++) {
                    if (i >= data.length) {
                        break;
                    }
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        fpga_buffer[ret] = Byte.valueOf(data[i], 16);
                        ret++;
                    }
                }
                String fpga_ver = DataUtil.Asc2String(fpga_buffer);
                fpga_ver = fpga_ver.substring(0, ret);
                versionRsp.setFpgaVer(fpga_ver);
                //sw_ver
                byte[] sw_buffer = new byte[GnbProtocol.OAM_STR_MAX];
                ret = 0;
                idx += GnbProtocol.OAM_STR_MAX;
                for (int i = idx; i < idx + GnbProtocol.OAM_STR_MAX; i++) {
                    if (i >= data.length) {
                        break;
                    }
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        sw_buffer[ret] = Byte.valueOf(data[i], 16);
                        ret++;
                    }
                }
                String sw_ver = DataUtil.Asc2String(sw_buffer);
                sw_ver = sw_ver.substring(0, ret);
                versionRsp.setSwVer(sw_ver);
            }
        }
        return versionRsp;
    }

    /**
     * 时间配置反馈信息
     */
    public GnbCmdRsp gnbSetTime(String source_data) {
        return gnbCmdAck(source_data);
    }

    /**
     * WIFI工作模式及SSID\PSW
     */
    public GnbCmdRsp gnbWifiCfg(String source_data) {
        return gnbCmdAck(source_data);
    }

    /**
     * 黑名单配置反馈信息
     */
    public GnbCmdRsp gnbBlackListCfg(String source_data) {
        return gnbCmdAck(source_data);
    }

    /**
     * 读取基带LOG配置反馈信息
     */
    public GnbCmdRsp gnbGetLog(String source_data) {
        return gnbCmdAck(source_data);
    }

    /**
     * 读取黑匣子文件
     */
    public GnbCmdRsp gnbGetOpLog(String source_data) {
        return gnbCmdAck(source_data);
    }

    /**
     * 写黑匣子文件
     */
    public GnbCmdRsp gnbWriteOpLog(String source_data) {
        return gnbCmdAck(source_data);
    }

    /**
     * 删除黑匣子文件
     */
    public GnbCmdRsp gnbDeleteOpLog(String source_data) {
        return gnbCmdAck(source_data);
    }

    /**
     * 基带升级配置反馈信息
     */
    public GnbCmdRsp gnbFirmwareUpgrade(String source_data) {
        return gnbCmdAck(source_data);
    }

    /**
     * 配置蓝牙名称
     */
    public GnbCmdRsp gnbSetBtNameRsp(String source_data) {
        return gnbCmdAck(source_data);
    }

    /**
     * 配置IP
     */
    public GnbCmdRsp gnbSetMethIpRsp(String source_data) {
        return gnbCmdAck(source_data);
    }

    /**
     * 读取IP
     */
    public GnbMethIpRsp gnbGetMethIpRsp(String source_data) {
        /*
          typedef struct {
          	    int sync_header;
          	    int msg_type;          		//UI_2_gNB_OAM_MSG
          	    int cmd_type;             	//CGI_MSG_GET_METH_CFG
          	    int cmd_param;

          	    char meth_ip[OAM_STR_MAX];
          	    char meth_mask[OAM_STR_MAX];
          	    char meth_gw[OAM_STR_MAX];
               char meth_mac[OAM_STR_MAX];
          	    int sync_footer;
          } oam_get_meth_t; 	//out
         */
        // 11 11 a5 5a
        // e8 03 00 00 msg_type
        // ca 00 00 00 cmd_type
        // 20 73 73 69
        //
        // 31 39 32 2e 31 36 38 2e 31 2e 33 33 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 meth_ip
        // 32 35 35 2e 32 35 35 2e 32 35 35 2e 30 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 meth_mask
        // 31 39 32 2e 31 36 38 2e 31 2e 31 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 meth_gw
        // 00 00 66 ff
        GnbMethIpRsp methRsp = new GnbMethIpRsp();
        String[] data = source_data.split(",");
        if (data.length >= 35) {
            int idx = 8;
            String s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
            int cmd_type = DataUtil.str2Int(s, 16);
            if (cmd_type == GnbProtocol.OAM_MSG_GET_METH_CFG) { //
                //meth_ip
                idx = 16;
                StringBuilder sbIp = new StringBuilder();
                for (int i = idx; i < idx + GnbProtocol.OAM_STR_MAX; i++) {
                    if (i >= data.length) {
                        break;
                    }
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        if (data[i].equals("2e")) {
                            sbIp.append(".");
                        } else {
                            sbIp.append(data[i].substring(1));
                        }
                    }
                }
                methRsp.setIp(sbIp.toString());
                //meth_mask
                idx += GnbProtocol.OAM_STR_MAX;
                sbIp.setLength(0);
                for (int i = idx; i < idx + GnbProtocol.OAM_STR_MAX; i++) {
                    if (i >= data.length) {
                        break;
                    }
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        if (data[i].equals("2e")) {
                            sbIp.append(".");
                        } else {
                            sbIp.append(data[i].substring(1));
                        }
                    }
                }
                methRsp.setMask(sbIp.toString());
                //meth_gw
                idx += GnbProtocol.OAM_STR_MAX;
                sbIp.setLength(0);
                for (int i = idx; i < idx + GnbProtocol.OAM_STR_MAX; i++) {
                    if (i >= data.length) {
                        break;
                    }
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        if (data[i].equals("2e")) {
                            sbIp.append(".");
                        } else {
                            sbIp.append(data[i].substring(1));
                        }
                    }
                }
                methRsp.setGateway(sbIp.toString());
                idx += GnbProtocol.OAM_STR_MAX;
                sbIp.setLength(0);
                for (int i = idx; i < idx + GnbProtocol.OAM_STR_MAX; i++) {
                    if (i >= data.length) {
                        break;
                    }
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        if (data[i].equals("3a")) {
                            sbIp.append(":");
                        } else {
                            sbIp.append(data[i].substring(1));
                        }
                    }
                }
                methRsp.setMac(sbIp.toString());
            }
        }
        return methRsp;
    }

    /**
     * 配置Ftp服务相关信息
     */
    public GnbCmdRsp gnbSetFtpRsp(String source_data) {
        return gnbCmdAck(source_data);
    }

    /**
     * 读取FTP服务相关信息
     */
    public GnbFtpRsp gnbGetFtpRsp(String source_data) {
        /*
           typedef struct {
              int sync_header;
              int msg_type;      		//UI_2_gNB_OAM_MSG
              int cmd_type;    		//OAM_MSG_GET_FTP_SERVER
              int cmd_param;

              char ftp_server[OAM_STR_MAX]; 192.168.1.100
              char ftp_path[OAM_STR_MAX]; g70_ftp
              char ftp_user[OAM_STR_MAX]; user
              char ftp_passwd[OAM_STR_MAX]; admin
              int getUploadInterval; 		// 1-1440 min
              int sync_footer;
           } oam_get_meth_t; 	//out
         */
        // 11 11 a5 5a
        // e8 03 00 00 msg_type
        // cc 00 00 00 cmd_type
        // 20 73 73 69
        // 01 00 00 00 server_type
        //
        // 31 39 32 2e 31 36 38 2e 31 2e 33 33 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ftp_server
        // 32 35 35 2e 32 35 35 2e 32 35 35 2e 30 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ftp_path
        // 31 39 32 2e 31 36 38 2e 31 2e 31 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ftp_user
        // 31 39 32 2e 31 36 38 2e 31 2e 31 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ftp_passwd
        // 00 00 66 ff
        GnbFtpRsp ftpRsp = new GnbFtpRsp();
        String[] data = source_data.split(",");
        if (data.length >= 35) {
            int idx = 8;
            String s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
            int cmd_type = DataUtil.str2Int(s, 16);
            if (cmd_type == GnbProtocol.OAM_MSG_GET_FTP_SERVER) { //
                idx = 16;
                StringBuilder sbIp = new StringBuilder();
                for (int i = idx; i < idx + GnbProtocol.OAM_STR_MAX; i++) {
                    if (i >= data.length) {
                        break;
                    }
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        if (data[i].equals("2e")) {
                            sbIp.append(".");
                        } else {
                            sbIp.append(data[i].substring(1));
                        }
                    }
                }
                ftpRsp.setFtpServer(sbIp.toString());
                //ftp_path
                idx += GnbProtocol.OAM_STR_MAX;
                byte[] path_buffer = new byte[GnbProtocol.OAM_STR_MAX];
                int ret = 0;
                for (int i = idx; i < idx + GnbProtocol.OAM_STR_MAX; i++) {
                    if (i >= data.length) {
                        break;
                    }
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        path_buffer[ret] = Byte.valueOf(data[i], 16);
                        ret++;
                    }
                }
                String ftp_path = DataUtil.Asc2String(path_buffer);
                ftp_path = ftp_path.substring(0, ret);
                ftpRsp.setFtpPath(ftp_path);
                //ftp_user
                idx += GnbProtocol.OAM_STR_MAX;
                byte[] user_buffer = new byte[GnbProtocol.OAM_STR_MAX];
                ret = 0;
                for (int i = idx; i < idx + GnbProtocol.OAM_STR_MAX; i++) {
                    if (i >= data.length) {
                        break;
                    }
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        user_buffer[ret] = Byte.valueOf(data[i], 16);
                        ret++;
                    }
                }
                String ftp_user = DataUtil.Asc2String(user_buffer);
                ftp_user = ftp_user.substring(0, ret);
                ftpRsp.setFtpUser(ftp_user);
                //ftp_passwd
                idx += GnbProtocol.OAM_STR_MAX;
                byte[] passwd_buffer = new byte[GnbProtocol.OAM_STR_MAX];
                ret = 0;
                for (int i = idx; i < idx + GnbProtocol.OAM_STR_MAX; i++) {
                    if (i >= data.length) {
                        break;
                    }
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        passwd_buffer[ret] = Byte.valueOf(data[i], 16);
                        ret++;
                    }
                }
                String ftp_passwd = DataUtil.Asc2String(passwd_buffer);
                ftp_passwd = ftp_passwd.substring(0, ret);
                ftpRsp.setFtpPasswd(ftp_passwd);
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                int upload_interval = DataUtil.str2Int(s, 16);
                ftpRsp.setUploadInterval(upload_interval);
                //ftp_server
            }
        }
        return ftpRsp;
    }

    /**
     * 配置PA控制IO口
     */
    public GnbCmdRsp gnbSetGpioRsp(String source_data) {
        return gnbCmdAck(source_data);
    }

    public GnbCmdRsp gnbSetRxGain(String source_data) {
        return gnbCmdAck(source_data);
    }

    public GnbCmdRsp gnbSetGps(String source_data) {
        return gnbCmdAck(source_data);
    }

    public GnbCmdRsp gnbGetSysLog(String source_data) {
        return gnbCmdAck(source_data);
    }

    public GnbCmdRsp gnbSetFanSpeed(String source_data) {
        return gnbCmdAck(source_data);
    }

    public GnbCmdRsp gnbSetFanAutoSpeed(String source_data) {
        return gnbCmdAck(source_data);
    }

    /**
     * 扫频上报
     */
    public GnbFreqScanRsp gnbFreqScanRsp(String source_data) {
        SLog.I("gnbFreqScanRsp = " + source_data);
        GnbFreqScanRsp freqScanRsp = new GnbFreqScanRsp();
        String[] data = source_data.split(",");
        if (data.length >= 104) {
            int idx = 8;
            StringBuilder s = new StringBuilder(data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx]);
            int cmd_type = DataUtil.str2Int(s.toString(), 16);
            if (cmd_type == GnbProtocol.OAM_MSG_FREQ_SCAN_REPORT) { //
                idx = 12;
                s = new StringBuilder(data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx]);
                freqScanRsp.setReportStep(DataUtil.str2Int(s.toString(), 16));
                idx += 4;
                s = new StringBuilder(data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx]);
                freqScanRsp.setReportLevel(DataUtil.str2Int(s.toString(), 16));
                idx += 4;
                s = new StringBuilder(data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx]);
                freqScanRsp.setScanResult(DataUtil.str2Int(s.toString(), 16));
                idx += 4;
                s = new StringBuilder(data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx]);
                freqScanRsp.setUl_arfcn(DataUtil.str2Int(s.toString(), 16));
                idx += 4;
                s = new StringBuilder(data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx]);
                freqScanRsp.setDl_arfcn(DataUtil.str2Int(s.toString(), 16));
                idx += 4;
                s = new StringBuilder(data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx]);
                freqScanRsp.setPci(DataUtil.str2Int(s.toString(), 16));
                idx += 4;
                s = new StringBuilder(data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx]);
                //FFFFFFB0
                freqScanRsp.setRsrp((int) Long.parseLong(s.toString(), 16));
//                freqScanRsp.setRsrp(DataUtil.StringToInt2(s));
                idx += 4;
                s = new StringBuilder(data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx]);
                freqScanRsp.setPrio(DataUtil.str2Int(s.toString(), 16));
                idx += 4;
//                s = data[idx + 7] + data[idx + 6] + data[idx + 5] + data[idx + 4] + data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                s = new StringBuilder();
                for (int i = 0; i < 16; i++) {
                    if (data[idx + i].equals("00")) {
                        break;
                    }
                    s.append(Integer.parseInt(data[idx + i]) - 30);
                }
                freqScanRsp.setTac(s.toString());
                idx += 16;
                s = new StringBuilder();
                for (int i = 0; i < 16; i++) {
                    if (data[idx + i].equals("00")) {
                        break;
                    }
                    s.append(Integer.parseInt(data[idx + i]) - 30);
                }
                freqScanRsp.setEci(s.toString());
                idx += 16;
                s = new StringBuilder(data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx]);
                freqScanRsp.setPk(DataUtil.str2Int(s.toString(), 16));
                idx += 4;
                s = new StringBuilder(data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx]);
                freqScanRsp.setPa(DataUtil.str2Int(s.toString(), 16));
                idx += 4;
                s = new StringBuilder(data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx]);
                freqScanRsp.setMCC1(DataUtil.str2Int(s.toString(), 16));
                idx += 4;
                s = new StringBuilder(data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx]);
                freqScanRsp.setMCC2(DataUtil.str2Int(s.toString(), 16));
                idx += 4;
                s = new StringBuilder(data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx]);
                freqScanRsp.setMNC1(DataUtil.str2Int(s.toString(), 16));
                idx += 4;
                s = new StringBuilder(data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx]);
                freqScanRsp.setMNC2(DataUtil.str2Int(s.toString(), 16));
                if (data.length >= 140) {
                    idx += 4;
                    s = new StringBuilder(data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx]);
                    freqScanRsp.setBandwidth(DataUtil.str2Int(s.toString(), 16));
                } else {
                    freqScanRsp.setBandwidth(0);
                }
            }
        }
        return freqScanRsp;
    }

    /**
     * 扫频上报
     */
    public GnbFreqScanGetDocumentRsp gnbFreqScanGetDocumentRsp(String source_data) {
        SLog.I("gnbFreqScanGetDocumentRsp = " + source_data);
        GnbFreqScanGetDocumentRsp gnbFreqScanGetDocumentRsp = new GnbFreqScanGetDocumentRsp();
        String[] data = source_data.split(",");
        if (data.length >= 92) {
            int idx = 8;
            String s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
            int cmd_type = DataUtil.str2Int(s, 16);
            if (cmd_type == GnbProtocol.OAM_MSG_FREQ_SCAN_REPORT) { //
                idx = 12;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                gnbFreqScanGetDocumentRsp.setReportStep(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                gnbFreqScanGetDocumentRsp.setReportLevel(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                gnbFreqScanGetDocumentRsp.setScanResult(DataUtil.str2Int(s, 16));
                idx += 4;
                byte[] file_name = new byte[64];
                int ret = 0;
                for (int i = idx; i < idx + 64; i++) {
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        try {
                            file_name[ret] = Byte.valueOf(data[i], 16);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                        ret++;
                    }
                }
                String name = DataUtil.Asc2String(file_name);
                name = name.substring(0, ret);
                gnbFreqScanGetDocumentRsp.setFileName(name);
            }
        }
        return gnbFreqScanGetDocumentRsp;
    }

    public GnbCmdRsp gnbStopFreqScanRsp(String source_data) {
        return gnbCmdAck(source_data);
    }

    /**
     * 干扰频点
     */
    public GnbCmdRsp gnbSetJamArfcn(String source_data) {
        return gnbCmdAck(source_data);
    }

    /**
     * GPS帧偏测量
     */
    public GnbCmdRsp startTdMeasure(String source_data) {
        /* 指令配置返回信息带CELL_ID
         * typedef struct {
         * 		int sync_header;
         * 		int msg_type;          	//UI_2_gNB_OAM_MSG
         * 		int cmd_type;
         * 		int cmd_result;   // -1： 返回失败，其它：帧偏
         *
         * 		int cell_id; // 此字段不一定有
         *
         * 		int sync_footer;
         * } oam_start_catch_t, oam_stop_catch_t; //in&out
         * */
        GnbCmdRsp state = null;
        String[] data = source_data.split(",");
        if (data.length > 19) {
            int idx = 4;
            String s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
            int msg_type = DataUtil.str2Int(s, 16);

            idx = 8;
            s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
            int cmd_type = DataUtil.str2Int(s, 16);
            if (msg_type == GnbProtocol.UI_2_gNB_OAM_MSG) { // 指令响应上报
                idx = 12;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                int cmd_result = -1;
                if (!s.equals("ffffffff") && !data[idx + 3].equals("ff")) {
                    cmd_result = DataUtil.str2Int(s, 16);
                }
                int cell_id = -1;
                idx = 16;
                if (data[idx + 1].equals("66") && data[idx].equals("ff")) {
                } else {
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    cell_id = DataUtil.str2Int(s, 16);
                }
                //int msgType, int cmdType, int rspValue, int cellId
                state = new GnbCmdRsp(cell_id, msg_type, cmd_type, cmd_result);
            }
        }
        return state;
    }

    public GnbGpioRsp gnbGetGpioRsp(String source_data) {
        GnbGpioRsp gpioRsp = new GnbGpioRsp();
        String[] data = source_data.split(",");
        if (data.length >= 35) {
            int idx = 8;
            String s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
            int cmd_type = DataUtil.str2Int(s, 16);
            if (cmd_type == GnbProtocol.OAM_MSG_GET_GPIO_MODE) { //
                idx = 16;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                gpioRsp.setGpio1_en1(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                gpioRsp.setGpio2_en2(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                gpioRsp.setGpio3_bs3(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                gpioRsp.setGpio4_tddSw1(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                gpioRsp.setGpio5_bs1(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                gpioRsp.setGpio6_bs2(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                gpioRsp.setGpio_7(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                gpioRsp.setGpio8_tddSw2(DataUtil.str2Int(s, 16));
                gpioRsp.setG758(false);
                if (data.length >= 110){
                    gpioRsp.setG758(true);
                    idx += 4;
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    gpioRsp.setGpio9(DataUtil.str2Int(s, 16));
                    idx += 4;
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    gpioRsp.setGpio10(DataUtil.str2Int(s, 16));
                    idx += 4;
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    gpioRsp.setGpio11(DataUtil.str2Int(s, 16));
                    idx += 4;
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    gpioRsp.setGpio12(DataUtil.str2Int(s, 16));
                    idx += 4;
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    gpioRsp.setGpio13(DataUtil.str2Int(s, 16));
                    idx += 4;
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    gpioRsp.setGpio14(DataUtil.str2Int(s, 16));
                    idx += 4;
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    gpioRsp.setGpio15(DataUtil.str2Int(s, 16));
                    idx += 4;
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    gpioRsp.setGpio16(DataUtil.str2Int(s, 16));
                    idx += 4;
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    gpioRsp.setGpio17(DataUtil.str2Int(s, 16));
                    idx += 4;
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    gpioRsp.setGpio18(DataUtil.str2Int(s, 16));
                    idx += 4;
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    gpioRsp.setGpio19(DataUtil.str2Int(s, 16));
                    idx += 4;
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    gpioRsp.setGpio20(DataUtil.str2Int(s, 16));
                    idx += 4;
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    gpioRsp.setGpio21(DataUtil.str2Int(s, 16));
                    idx += 4;
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    gpioRsp.setGpio22(DataUtil.str2Int(s, 16));
                    idx += 4;
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    gpioRsp.setGpio23(DataUtil.str2Int(s, 16));
                    idx += 4;
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    gpioRsp.setGpio24(DataUtil.str2Int(s, 16));
                }
                SLog.D(gpioRsp.toString());
            }
        }
        return gpioRsp;
    }

    public GnbCmdRsp gnbSetSysInfoRsp(String source_data) {
        return gnbCmdAck(source_data);
    }

    public GnbGetSysInfoRsp gnbGetSysInfoRsp(String source_data) {
        GnbGetSysInfoRsp sysInfoRsp = new GnbGetSysInfoRsp();
        String[] data = source_data.split(",");
        if (data.length >= 35) {
            int idx = 8;
            String s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
            int cmd_type = DataUtil.str2Int(s, 16);
            if (cmd_type == GnbProtocol.OAM_MSG_GET_SYS_INFO) { //
                idx = 16;
                byte[] dev_buffer = new byte[GnbProtocol.OAM_STR_MAX];
                int ret = 0;
                for (int i = idx; i < idx + GnbProtocol.OAM_STR_MAX; i++) {
                    if (i >= data.length) {
                        break;
                    }
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        dev_buffer[ret] = Byte.valueOf(data[i], 16);
                        ret++;
                    }
                }
                String dev_name = DataUtil.Asc2String(dev_buffer);
                dev_name = dev_name.substring(0, ret);
                sysInfoRsp.setDevName(dev_name);
                //ftp_user
                idx += GnbProtocol.OAM_STR_MAX;
                byte[] license_buffer = new byte[256];
                ret = 0;
                for (int i = idx; i < idx + 256; i++) {
                    if (i >= data.length) {
                        break;
                    }
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        license_buffer[ret] = Byte.valueOf(data[i], 16);
                        ret++;
                    }
                }
                String license = DataUtil.Asc2String(license_buffer);
                license = license.substring(0, ret);
                sysInfoRsp.setLicense(license);

            }
        }
        return sysInfoRsp;
    }

    /**
     * 通道配置回调
     */
    public GnbCmdRsp gnbSetDualCellRsp(String source_data) {
        return gnbCmdAck(source_data);
    }

    /**
     * 读取GPS输入输出端口
     */
    public GnbGpsRsp gnbGetGpsRsp(String source_data) {
        GnbGpsRsp gpsRsp = new GnbGpsRsp();
        String[] data = source_data.split(",");
        if (data.length >= 44) {
            int idx = 8;
            String s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
            int cmd_type = DataUtil.str2Int(s, 16);
            if (cmd_type == GnbProtocol.OAM_MSG_GET_GPS_CFG) {
                idx = 16;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                gpsRsp.setGnssSelect(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                gpsRsp.setLatitude(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                gpsRsp.setLongitude(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                gpsRsp.setGpsState(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                gpsRsp.setGpsTDay(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                gpsRsp.setGpsTTime(DataUtil.str2Int(s, 16));

                SLog.D(gpsRsp.toString());
            }
        }
        return gpsRsp;
    }

    /**
     * 读写用户数据
     */
    public GnbUserDataRsp gnbGetUserDataRsp(String source_data) {
        GnbUserDataRsp userDataRsp = new GnbUserDataRsp();
        String[] data = source_data.split(",");
        if (data.length >= 284) {
            int idx = 8;
            String s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
            int cmd_type = DataUtil.str2Int(s, 16);
            if (cmd_type == GnbProtocol.OAM_MSG_RW_USER_DATA) {
                idx = 12;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                userDataRsp.setResult(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                userDataRsp.setRW(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                userDataRsp.setIndex(DataUtil.str2Int(s, 16));
                idx += 4;
                byte[] user_data = new byte[256];
                int ret = 0;
                for (int i = idx; i < idx + 256; i++) {
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        try {
                            user_data[ret] = Byte.valueOf(data[i], 16);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                        ret++;
                    }
                }
                String userData = DataUtil.Asc2String(user_data);
                userData = userData.substring(0, ret);
                userDataRsp.setUser_data(userData);
            }
        }
        return userDataRsp;
    }

    public GnbCmdRsp gnbSetGpioTxRx(String source_data) {
        return gnbCmdAck(source_data);
    }

    public GnbCmdRsp gnbSetForwardUdpMsg(String source_data) {
        return gnbCmdAck(source_data);
    }

    /**
     * 配置GPS输入输出端口
     */
    public GnbCmdRsp gnbSetGpsInOut(String source_data) {
        return gnbCmdAck(source_data);
    }

    public GnbCmdRsp gnbStartBandScan(String source_data) {
        return gnbCmdAck(source_data);
    }

    /**
     * 读取GPS输入输出端口
     */
    public GnbGpsInOutRsp gnbGetGpsInOutRsp(String source_data) {
        GnbGpsInOutRsp gpsInOutRsp = new GnbGpsInOutRsp();
        String[] data = source_data.split(",");
        if (data.length >= 28) {
            int idx = 8;
            String s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
            int cmd_type = DataUtil.str2Int(s, 16);
            if (cmd_type == GnbProtocol.OAM_MSG_GET_GPS_IO_CFG) { //
                idx = 16;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                gpsInOutRsp.setOutGpioIdx(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                gpsInOutRsp.setInGpioIdx(DataUtil.str2Int(s, 16));

                SLog.D(gpsInOutRsp.toString());
            }
        }
        return gpsInOutRsp;
    }

    public GnbCmdRsp gnbSetDualStackRsp(String source_data) {
        return gnbCmdAck(source_data);
    }

    /**
     * 读写用户数据
     */
    public GnbReadDataFwdRsp gnbReadDataFwd(String source_data) {
        GnbReadDataFwdRsp rsp = new GnbReadDataFwdRsp();
        String[] data = source_data.split(",");
        if (data.length >= 20) {
            try {
                int idx = 8;
                String s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                int cmd_type = DataUtil.str2Int(s, 16);
                if (cmd_type == GnbProtocol.OAM_MSG_DATA_FWD) {
                    idx += 4;
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    int msg_len = DataUtil.str2Int(s, 16);
                    rsp.setMsgLen(msg_len);
                    idx += 4;
                    int fixCount = data[data.length - 4].equals("ff") ? 4 : 0;
                    byte[] ver = new byte[data.length - 16 - fixCount]; // 16 头数据长度  fixCount尾数据长度
                    int ret = 0;
                    int size = data.length - fixCount;
                    for (int i = idx; i < size; i++) {
                        ver[ret] = Byte.valueOf(data[idx], 16);
                        ret++;
                        idx++;
                    }
                    String str = DataUtil.Asc2String(ver);
                    str = str.substring(0, ret);
                    rsp.setData(str);
                }
            }catch (Exception e){
                SLog.E("readDataFwd Error:" + e.getMessage());
            }
        }
        return rsp;
    }
    public GnbCmdRsp gnbSetLicRsp(String source_data) {
        return gnbCmdAck(source_data);
    }

    public GnbCmdRsp gnbSetPhoneTypeRsp(String source_data) {
        return gnbCmdAck(source_data);
    }
    public GnbCmdRsp gnbSetFuncCfgRsp(String source_data) {
        return gnbCmdAck(source_data);
    }
}
