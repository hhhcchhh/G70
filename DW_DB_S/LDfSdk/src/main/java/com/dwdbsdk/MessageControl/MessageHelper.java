package com.dwdbsdk.MessageControl;

import com.dwdbsdk.Bean.DB.DBProtocol;
import com.dwdbsdk.Bean.DB.OrxBean;
import com.dwdbsdk.Bean.DB.TxBean;
import com.dwdbsdk.Bean.DW.DWProtocol;
import com.dwdbsdk.Bean.DW.TracePara;
import com.dwdbsdk.Config.DB.MsgStartJam;
import com.dwdbsdk.DwDbSdk;
import com.dwdbsdk.Logcat.SdkLog;
import com.dwdbsdk.Response.DB.MsgCmdRsp;
import com.dwdbsdk.Response.DB.MsgGetJamRsp;
import com.dwdbsdk.Response.DB.MsgReadDataFwdRsp;
import com.dwdbsdk.Response.DB.MsgScanRsp;
import com.dwdbsdk.Response.DB.MsgSenseReportRsp;
import com.dwdbsdk.Response.DB.MsgStateRsp;
import com.dwdbsdk.Response.DB.MsgVersionRsp;
import com.dwdbsdk.Response.DW.GnbCatchCfgRsp;
import com.dwdbsdk.Response.DW.GnbCmdRsp;
import com.dwdbsdk.Response.DW.GnbFreqScanGetDocumentRsp;
import com.dwdbsdk.Response.DW.GnbFreqScanRsp;
import com.dwdbsdk.Response.DW.GnbFtpRsp;
import com.dwdbsdk.Response.DW.GnbGetSysInfoRsp;
import com.dwdbsdk.Response.DW.GnbGpioRsp;
import com.dwdbsdk.Response.DW.GnbGpsInOutRsp;
import com.dwdbsdk.Response.DW.GnbGpsRsp;
import com.dwdbsdk.Response.DW.GnbMethIpRsp;
import com.dwdbsdk.Response.DW.GnbReadDataFwdRsp;
import com.dwdbsdk.Response.DW.GnbSetDataTo485Rsp;
import com.dwdbsdk.Response.DW.GnbStateRsp;
import com.dwdbsdk.Response.DW.GnbTraceRsp;
import com.dwdbsdk.Response.DW.GnbUserDataRsp;
import com.dwdbsdk.Response.DW.GnbVersionRsp;
import com.dwdbsdk.Util.AirState;
import com.dwdbsdk.Util.Battery;
import com.dwdbsdk.Util.BatteryPredator;
import com.dwdbsdk.Util.DataUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageHelper {
    private static MessageHelper instance;
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


    /**
     * 心跳数据解析
     */
    public GnbStateRsp DWHeartState(String id, String source_data) {
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
            if (cmd == DWProtocol.UI_2_gNB_HEART_BEAT) { // 心跳标志
                idx = 16;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                int cell_1_state = DataUtil.str2Int(s, 16);
//                MessageController.build().setTracing(id, 0, cell_1_state == 2 || cell_1_state == 3 || cell_1_state == 4);
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                int cell_2_state = DataUtil.str2Int(s, 16);
//                MessageController.build().setTracing(id, 1, cell_2_state == 2 || cell_2_state == 3 || cell_2_state == 4);
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
                for (int i = idx; i < idx + DWProtocol.OAM_STR_MAX; i++) {
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
                String hostIp = sbIp.toString();
                if (hostIp.equals("255.255.255.255"))
                    hostIp = MessageController.build().getIpFromMsgTypeList(id);

                byte[] bssid = new byte[DWProtocol.OAM_STR_MAX];
                int ret = 0;
                idx += DWProtocol.OAM_STR_MAX;
                for (int i = idx; i < idx + DWProtocol.OAM_STR_MAX; i++) {
                    if (i >= data.length) {
                        break;
                    }
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        bssid[ret] = DataUtil.str2Byte(data[i], 16);
                        ret++;
                    }
                }
                String ssid = DataUtil.Asc2String(bssid);
                ssid = ssid.substring(0, ret);
                ssid = ssid.replaceAll("\"", "");
                ssid = ssid.replaceAll("\r|\n", "");
                byte[] device = new byte[36];
                ret = 0;
                idx += DWProtocol.OAM_STR_MAX;
                for (int i = idx; i < idx + 36; i++) {
                    if (i >= data.length) {
                        break;
                    }
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        device[ret] = DataUtil.str2Byte(data[i], 16);
                        ret++;
                    }
                }
                String deviceId = DataUtil.Asc2String(device);
                deviceId = deviceId.substring(0, ret);
                deviceId = deviceId.replaceAll("\r|\n", "");

                byte[] bbt = new byte[DWProtocol.OAM_STR_MAX];
                ret = 0;
                idx += 36;
                for (int i = idx; i < idx + DWProtocol.OAM_STR_MAX; i++) {
                    if (i >= data.length) {
                        break;
                    }
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        bbt[ret] = DataUtil.str2Byte(data[i], 16);
                        ret++;
                    }
                }
                String bt_name = DataUtil.Asc2String(bbt);
                bt_name = bt_name.substring(0, ret);
                //bt_name = bt_name.replaceAll("\"", "");
                bt_name = bt_name.replaceAll("\r|\n", "");

                if (!MessageController.build().isDoStop) {
                    if (cell_1_state == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                        if (!MessageController.build().isEnableChangeTac(id, 0)) {
                            MessageController.build().setEnableChangeTac(id, 0, true);
                            MessageController.build().setTraceType(id, DWProtocol.TraceType.TRACE);
                        }
                    } else if (cell_1_state == GnbStateRsp.gnbState.GNB_STATE_CONTROL) {
                        if (!MessageController.build().isEnableChangeTac(id, 0)) {
                            MessageController.build().setEnableChangeTac(id, 0, true);
                            MessageController.build().setTraceType(id, DWProtocol.TraceType.CONTROL);
                        }
                    } else if (cell_1_state == GnbStateRsp.gnbState.GNB_STATE_CATCH) {
                        if (!MessageController.build().isEnableChangeTac(id, 0)) {
                            MessageController.build().setEnableChangeTac(id, 0, true);
                            MessageController.build().setTraceType(id, DWProtocol.TraceType.CATCH);
                        }
                    }

                    if (cell_2_state == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                        if (!MessageController.build().isEnableChangeTac(id, 1)) {
                            MessageController.build().setEnableChangeTac(id, 1, true);
                            MessageController.build().setTraceType(id, DWProtocol.TraceType.TRACE);
                        }
                    } else if (cell_2_state == GnbStateRsp.gnbState.GNB_STATE_CONTROL) {
                        if (!MessageController.build().isEnableChangeTac(id, 1)) {
                            MessageController.build().setEnableChangeTac(id, 1, true);
                            MessageController.build().setTraceType(id, DWProtocol.TraceType.CONTROL);
                        }
                    } else if (cell_2_state == GnbStateRsp.gnbState.GNB_STATE_CATCH) {
                        if (!MessageController.build().isEnableChangeTac(id, 1)) {
                            MessageController.build().setEnableChangeTac(id, 1, true);
                            MessageController.build().setTraceType(id, DWProtocol.TraceType.CATCH);
                        }
                    }
                }
                state = new GnbStateRsp(cell_1_state, cell_2_state, gps_sync_state, time_sync_state, fair_sync_state,
                        sair_sync_state, hostIp, ssid, deviceId, bt_name);
                int vol = 0;
                idx += DWProtocol.OAM_STR_MAX;
                if ((idx + 3) < data.length) {
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    vol = (int) Long.parseLong(s, 16);
                }
                state.addVol(vol);
                idx += 4;
                if ((idx + 3) < data.length) {
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    vol = (int) Long.parseLong(s, 16);
                    Battery.build().handleVol(vol);
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

                byte[] b_dev_name = new byte[DWProtocol.OAM_STR_MAX];
                ret = 0;
                idx += 4;
                for (int i = idx; i < idx + DWProtocol.OAM_STR_MAX; i++) {
                    if (i >= data.length) {
                        break;
                    }
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        b_dev_name[ret] = DataUtil.str2Byte(data[i], 16);
                        ret++;
                    }
                }
                String dev_name = DataUtil.Asc2String(b_dev_name);
                dev_name = dev_name.substring(0, ret);
                dev_name = dev_name.replaceAll("\"", "");
                dev_name = dev_name.replaceAll("\r|\n", "");
                state.setDevName(dev_name);

                double lon = 0.0;
                idx += DWProtocol.OAM_STR_MAX;
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

                if (!MessageController.build().isDoStop) {
                    if (cell_3_state == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                        if (!MessageController.build().isEnableChangeTac(id, 2)) {
                            MessageController.build().setEnableChangeTac(id, 2, true);
                            MessageController.build().setTraceType(id, DWProtocol.TraceType.TRACE);
                        }
                    } else if (cell_3_state == GnbStateRsp.gnbState.GNB_STATE_CONTROL) {
                        if (!MessageController.build().isEnableChangeTac(id, 2)) {
                            MessageController.build().setEnableChangeTac(id, 2, true);
                            MessageController.build().setTraceType(id, DWProtocol.TraceType.CONTROL);
                        }
                    } else if (cell_3_state == GnbStateRsp.gnbState.GNB_STATE_CATCH) {
                        if (!MessageController.build().isEnableChangeTac(id, 2)) {
                            MessageController.build().setEnableChangeTac(id, 2, true);
                            MessageController.build().setTraceType(id, DWProtocol.TraceType.CATCH);
                        }
                    }

                    if (cell_4_state == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                        if (!MessageController.build().isEnableChangeTac(id, 3)) {
                            MessageController.build().setEnableChangeTac(id, 3, true);
                            MessageController.build().setTraceType(id, DWProtocol.TraceType.TRACE);
                        }
                    } else if (cell_4_state == GnbStateRsp.gnbState.GNB_STATE_CONTROL) {
                        if (!MessageController.build().isEnableChangeTac(id, 3)) {
                            MessageController.build().setEnableChangeTac(id, 3, true);
                            MessageController.build().setTraceType(id, DWProtocol.TraceType.CONTROL);
                        }
                    } else if (cell_4_state == GnbStateRsp.gnbState.GNB_STATE_CATCH) {
                        if (!MessageController.build().isEnableChangeTac(id, 3)) {
                            MessageController.build().setEnableChangeTac(id, 3, true);
                            MessageController.build().setTraceType(id, DWProtocol.TraceType.CATCH);
                        }
                    }
                }

                int tair_sync_state = 0;  //通道三空口
                int hair_sync_state = 0;  //通道四空口
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
                int dual_stack = 0;  // 0-single stack£¬1-dual stack
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

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
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
        if (data.length > 14) {
            int idx = 4;
            String s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
            int msg_type = DataUtil.str2Int(s, 16);

            idx = 8;
            s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
            int cmd_type = DataUtil.str2Int(s, 16);

            if (msg_type == DWProtocol.UI_2_gNB_OAM_MSG) { // 指令响应上报
                idx = 12;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                int cmd_result = DataUtil.str2Int(s, 16);

                int cell_id = -1;
                idx = 16;
                if (data.length > 19) {
                    if (data[idx + 1].equals("66") && data[idx].equals("ff")) {
                        cell_id = -1;
                    } else {
                        s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        cell_id = DataUtil.str2Int(s, 16);
                    }
                }

                //int msgType, int cmdType, int rspValue, int cellId
                state = new GnbCmdRsp(cell_id, msg_type, cmd_type, cmd_result);
                if (id != null && cmd_result == DWProtocol.OAM_ACK_OK) {
                    switch (cmd_type) {
                        case DWProtocol.UI_2_gNB_STOP_CATCH:
                        case DWProtocol.UI_2_eNB_STOP_CATCH:
                        case DWProtocol.UI_2_gNB_STOP_TRACE:
                        case DWProtocol.UI_2_gNB_STOP_LTE_TRACE:
                        case DWProtocol.UI_2_gNB_STOP_CONTROL:
                        case DWProtocol.UI_2_eNB_STOP_CONTROL:
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
        if (msg_type == DWProtocol.gNB_2_UI_REPORT_UE_INFO || msg_type == DWProtocol.gNB_2_UI_REPORT_LTE_UE_INFO) {// 定位103
            idx = 8;
            int cellId;
            if (idx < data.length) {
                String a = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                cellId = DataUtil.str2Int(a, 16);
                traceRsp.setCellId(cellId);
            }
            idx += 4;
            for (int i = idx; i < idx + DWProtocol.MAX_IMSI_LEN; i++) {
                if (i >= data.length) break;
                if (data[i].equals("00")) break;
                else imsi.append(data[i].substring(1));
            }
            for (int i = idx + DWProtocol.MAX_IMSI_LEN; i < idx + DWProtocol.MAX_IMSI_LEN * 2; i++) {
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
                idx += DWProtocol.MAX_IMSI_LEN * 2;
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
        } else if (msg_type == DWProtocol.UI_2_gNB_OAM_MSG) { // 心跳标志
            GnbCmdRsp state = gnbCmdAck(source_data);
            if (null != state) {
                if (state.getCmdType() == DWProtocol.UI_2_gNB_START_CATCH || state.getCmdType() == DWProtocol.UI_2_eNB_START_CATCH) {
                    if (state.getRspValue() == DWProtocol.OAM_ACK_OK) {
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
        int lastMinLength = data.length - 4;
        if (msg_type == DWProtocol.gNB_2_UI_REPORT_UE_INFO || msg_type == DWProtocol.gNB_2_UI_REPORT_LTE_UE_INFO) {// 定位103
            idx = 8;
            int cellId;
            if (idx <= lastMinLength) {
                String a = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                cellId = DataUtil.str2Int(a, 16);
                traceRsp.setCellId(cellId);
            }
            idx += 4;
            for (int i = idx; i < idx + DWProtocol.MAX_IMSI_LEN; i++) {
                if (i > lastMinLength) break;
                if (data[i].equals("00")) break;
                else imsi.append(data[i].substring(1));
            }
            for (int i = idx + DWProtocol.MAX_IMSI_LEN; i < idx + DWProtocol.MAX_IMSI_LEN * 2; i++) {
                if (i > lastMinLength) break;
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
                idx += DWProtocol.MAX_IMSI_LEN * 2;
                if (idx > lastMinLength) return null;
                if (data[idx].equals("ff")) rsrp = -1; // 掉线
                else {
                    String a = data[idx];
                    rsrp = DataUtil.str2Int(a, 16);
                    // 数据处理范围最大值：100
                    double value = rsrp * 1.1 + 3;
                    rsrp = (int) Math.abs(value);
                }
                idx += 4;
                if (idx > lastMinLength) rssi = 0;
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
                if (idx <= lastMinLength) {
                    String a = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    distance = DataUtil.str2Int(a, 16);
                }
                traceRsp.setRsrp(rsrp);
                traceRsp.setRssi(rssi);
                traceRsp.setDistance(distance);
                idx += 4;
                int rnti = 0;
                if (idx <= lastMinLength) {
                    String a = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    rnti = DataUtil.str2Int(a, 16);
                }
                traceRsp.setRnti(rnti);
                traceRsp.addImsi(imsi.toString());
                traceRsp.addGuti(guti.toString());
                idx += 4;
                int phone_type = 0;
                if (idx <= lastMinLength) {
                    if (!data[idx].equals("ff")) {
                        String a = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        phone_type = DataUtil.str2Int(a, 16);
                    }
                }
                traceRsp.setPhone_type(phone_type);
            }
        } else if (msg_type == DWProtocol.UI_2_gNB_OAM_MSG) { // 心跳标志
            GnbCmdRsp state = gnbCmdAck(source_data);
            if (null != state) {
                if (state.getCmdType() == DWProtocol.UI_2_gNB_START_LTE_TRACE ||
                        state.getCmdType() == DWProtocol.UI_2_gNB_START_TRACE) {
                    if (state.getRspValue() == DWProtocol.OAM_ACK_OK) {
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
        if (msg_type == DWProtocol.gNB_2_UI_REPORT_UE_INFO || msg_type == DWProtocol.gNB_2_UI_REPORT_LTE_UE_INFO) {// 定位103
            idx = 8;
            int cellId;
            if (idx < data.length) {
                String a = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                cellId = DataUtil.str2Int(a, 16);
                traceRsp.setCellId(cellId);
            }
            idx += 4;
            for (int i = idx; i < idx + DWProtocol.MAX_IMSI_LEN; i++) {
                if (i >= data.length) break;
                if (data[i].equals("00")) break;
                else imsi.append(data[i].substring(1));
            }
            for (int i = idx + DWProtocol.MAX_IMSI_LEN; i < idx + DWProtocol.MAX_IMSI_LEN * 2; i++) {
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
                idx += DWProtocol.MAX_IMSI_LEN * 2;
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
        } else if (msg_type == DWProtocol.UI_2_gNB_OAM_MSG) { // 心跳标志
            GnbCmdRsp state = gnbCmdAck(source_data);
            if (state.getCmdType() == DWProtocol.UI_2_gNB_START_CONTROL ||
                    state.getCmdType() == DWProtocol.UI_2_eNB_START_CONTROL) {
                if (state.getRspValue() == DWProtocol.OAM_ACK_OK) {
                    MessageController.build().setTracing(id, state.getCellId(), true);
                }
            }
            traceRsp.setCmdRsp(state);
            return traceRsp;
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
            if (cmd_type == DWProtocol.OAM_MSG_GET_CATCH_CFG) { //
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
                Gnb_cfg.put("Cell_id", s);
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("MCC", s);
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("MNC", s);
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("DL_NR_ARFCN", s);
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("UL_NR_ARFCN", s);
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("PCI", s);
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("TAC", s);
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("PA", s);
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("PK", s);
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("ue_max_pwr", s);
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("timing_offset", s);
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("work_mode", s);
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("air_sync_enable", s);
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("MCC2", s);
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("MNC2", s);
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("ul_rb_offset", s);
                idx += 4;
                s = data[idx + 7] + data[idx + 6] + data[idx + 5] + data[idx + 4] + data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("cid", s);
                idx += 8;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("ssb", s);
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("frame_type", s);
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("bandwidth", s);
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("reject_code", s);
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("rxLevMin", s);
                idx += 56; //resv[14]  预留
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("cfr", s);
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                Gnb_cfg.put("swap_rf", s);
                catchCfg.setGnbCfg(Gnb_cfg);
                SdkLog.D(catchCfg.toString());
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
        SdkLog.I("gnbQueryVersion");
        GnbVersionRsp versionRsp = new GnbVersionRsp();
        String[] data = source_data.split(",");
        if (data.length >= 35) {
            int idx = 8;
            String s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
            int cmd_type = DataUtil.str2Int(s, 16);
            if (cmd_type == DWProtocol.UI_2_gNB_QUERY_gNB_VERSION) { //
                //hw_ver
                byte[] hw_buffer = new byte[DWProtocol.OAM_STR_MAX];
                int ret = 0;
                idx = 16;
                for (int i = idx; i < idx + DWProtocol.OAM_STR_MAX; i++) {
                    if (i >= data.length) {
                        break;
                    }
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        hw_buffer[ret] = DataUtil.str2Byte(data[i], 16);
                        ret++;
                    }
                }
                String hw_ver = DataUtil.Asc2String(hw_buffer);
                hw_ver = hw_ver.substring(0, ret);
                versionRsp.setHwVer(hw_ver);
                //fpga_ver
                byte[] fpga_buffer = new byte[DWProtocol.OAM_STR_MAX];
                ret = 0;
                idx += DWProtocol.OAM_STR_MAX;
                for (int i = idx; i < idx + DWProtocol.OAM_STR_MAX; i++) {
                    if (i >= data.length) {
                        break;
                    }
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        fpga_buffer[ret] = DataUtil.str2Byte(data[i], 16);
                        ret++;
                    }
                }
                String fpga_ver = DataUtil.Asc2String(fpga_buffer);
                fpga_ver = fpga_ver.substring(0, ret);
                versionRsp.setFpgaVer(fpga_ver);
                //sw_ver
                byte[] sw_buffer = new byte[DWProtocol.OAM_STR_MAX];
                ret = 0;
                idx += DWProtocol.OAM_STR_MAX;
                for (int i = idx; i < idx + DWProtocol.OAM_STR_MAX; i++) {
                    if (i >= data.length) {
                        break;
                    }
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        sw_buffer[ret] = DataUtil.str2Byte(data[i], 16);
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
        /**
         * typedef struct {
         * 	    int sync_header;
         * 	    int msg_type;          		//UI_2_gNB_OAM_MSG
         * 	    int cmd_type;             	//CGI_MSG_GET_METH_CFG
         * 	    int cmd_param;
         *
         * 	    char meth_ip[OAM_STR_MAX];
         * 	    char meth_mask[OAM_STR_MAX];
         * 	    char meth_gw[OAM_STR_MAX];
         *      char meth_mac[OAM_STR_MAX];
         * 	    int sync_footer;
         * } oam_get_meth_t; 	//out
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
            if (cmd_type == DWProtocol.OAM_MSG_GET_METH_CFG) { //
                //meth_ip
                idx = 16;
                StringBuilder sbIp = new StringBuilder();
                for (int i = idx; i < idx + DWProtocol.OAM_STR_MAX; i++) {
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
                idx += DWProtocol.OAM_STR_MAX;
                sbIp.setLength(0);
                for (int i = idx; i < idx + DWProtocol.OAM_STR_MAX; i++) {
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
                idx += DWProtocol.OAM_STR_MAX;
                sbIp.setLength(0);
                for (int i = idx; i < idx + DWProtocol.OAM_STR_MAX; i++) {
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
                idx += DWProtocol.OAM_STR_MAX;
                sbIp.setLength(0);
                for (int i = idx; i < idx + DWProtocol.OAM_STR_MAX; i++) {
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
        /**
         *  typedef struct {
         *     int sync_header;
         *     int msg_type;      		//UI_2_gNB_OAM_MSG
         *     int cmd_type;    		//OAM_MSG_GET_FTP_SERVER
         *     int cmd_param;
         *
         *     char ftp_server[OAM_STR_MAX]; 192.168.1.100
         *     char ftp_path[OAM_STR_MAX]; DW_ftp
         *     char ftp_user[OAM_STR_MAX]; user
         *     char ftp_passwd[OAM_STR_MAX]; admin
         *     int getUploadInterval; 		// 1-1440 min
         *     int sync_footer;
         *  } oam_get_meth_t; 	//out
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
            if (cmd_type == DWProtocol.OAM_MSG_GET_FTP_SERVER) { //
                idx = 16;
                StringBuilder sbIp = new StringBuilder();
                for (int i = idx; i < idx + DWProtocol.OAM_STR_MAX; i++) {
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
                idx += DWProtocol.OAM_STR_MAX;
                byte[] path_buffer = new byte[DWProtocol.OAM_STR_MAX];
                int ret = 0;
                for (int i = idx; i < idx + DWProtocol.OAM_STR_MAX; i++) {
                    if (i >= data.length) {
                        break;
                    }
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        path_buffer[ret] = DataUtil.str2Byte(data[i], 16);
                        ret++;
                    }
                }
                String ftp_path = DataUtil.Asc2String(path_buffer);
                ftp_path = ftp_path.substring(0, ret);
                ftpRsp.setFtpPath(ftp_path);
                //ftp_user
                idx += DWProtocol.OAM_STR_MAX;
                byte[] user_buffer = new byte[DWProtocol.OAM_STR_MAX];
                ret = 0;
                for (int i = idx; i < idx + DWProtocol.OAM_STR_MAX; i++) {
                    if (i >= data.length) {
                        break;
                    }
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        user_buffer[ret] = DataUtil.str2Byte(data[i], 16);
                        ret++;
                    }
                }
                String ftp_user = DataUtil.Asc2String(user_buffer);
                ftp_user = ftp_user.substring(0, ret);
                ftpRsp.setFtpUser(ftp_user);
                //ftp_passwd
                idx += DWProtocol.OAM_STR_MAX;
                byte[] passwd_buffer = new byte[DWProtocol.OAM_STR_MAX];
                ret = 0;
                for (int i = idx; i < idx + DWProtocol.OAM_STR_MAX; i++) {
                    if (i >= data.length) {
                        break;
                    }
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        passwd_buffer[ret] = DataUtil.str2Byte(data[i], 16);
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
                idx += 4;
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
        GnbFreqScanRsp freqScanRsp = new GnbFreqScanRsp();
        String[] data = source_data.split(",");
        if (data.length >= 104) {
            int idx = 8;
            String s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
            int cmd_type = DataUtil.str2Int(s, 16);
            if (cmd_type == DWProtocol.OAM_MSG_FREQ_SCAN_REPORT) { //
                idx = 12;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                freqScanRsp.setReportStep(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                freqScanRsp.setReportLevel(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                freqScanRsp.setScanResult(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                freqScanRsp.setUl_arfcn(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                freqScanRsp.setDl_arfcn(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                freqScanRsp.setPci(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                //FFFFFFB0
                freqScanRsp.setRsrp((int) Long.parseLong(s, 16));
//                freqScanRsp.setRsrp(DataUtil.StringToInt2(s));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                freqScanRsp.setPrio(DataUtil.str2Int(s, 16));
                idx += 4;
//                s = data[idx + 7] + data[idx + 6] + data[idx + 5] + data[idx + 4] + data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                s = "";
                for (int i = 0; i < 16; i++) {
                    if (data[idx + i].equals("00")) {
                        break;
                    }
                    s += Integer.parseInt(data[idx + i]) - 30;
                }
                freqScanRsp.setTac(s);
                idx += 16;
                s = "";
                for (int i = 0; i < 16; i++) {
                    if (data[idx + i].equals("00")) {
                        break;
                    }
                    s += Integer.parseInt(data[idx + i]) - 30;
                }
                freqScanRsp.setEci(s);
                idx += 16;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                freqScanRsp.setPk(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                freqScanRsp.setPa(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                freqScanRsp.setMCC1(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                freqScanRsp.setMCC2(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                freqScanRsp.setMNC1(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                freqScanRsp.setMNC2(DataUtil.str2Int(s, 16));
                if (data.length >= 140) {
                    idx += 4;
                    s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    freqScanRsp.setBandwidth(DataUtil.str2Int(s, 16));
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
        SdkLog.I("gnbFreqScanGetDocumentRsp = " + source_data);
        GnbFreqScanGetDocumentRsp gnbFreqScanGetDocumentRsp = new GnbFreqScanGetDocumentRsp();
        String[] data = source_data.split(",");
        if (data.length >= 92) {
            int idx = 8;
            String s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
            int cmd_type = DataUtil.str2Int(s, 16);
            if (cmd_type == DWProtocol.OAM_MSG_FREQ_SCAN_REPORT) { //
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
                    if (i >= data.length) {
                        break;
                    }
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        file_name[ret] = DataUtil.str2Byte(data[i], 16);
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
            if (msg_type == DWProtocol.UI_2_gNB_OAM_MSG) { // 指令响应上报
                idx = 12;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                int cmd_result = -1;
                if (!s.equals("ffffffff") && !data[idx + 3].equals("ff")) {
                    cmd_result = DataUtil.str2Int(s, 16);
                }
                int cell_id = -1;
                idx = 16;
                if ((idx + 3) < data.length) {
                    if (data[idx + 1].equals("66") && data[idx + 0].equals("ff")) {
                        cell_id = -1;
                    } else {
                        s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        cell_id = DataUtil.str2Int(s, 16);
                    }
                }
                //int msgType, int cmdType, int rspValue, int cellId
                state = new GnbCmdRsp(cell_id, msg_type, cmd_type, cmd_result);
            }
        }
        return state;
    }

    /**
     * 配置GPS输入输出端口
     *
     * @param source_data
     * @return
     */
    public GnbCmdRsp gnbSetGpsInOut(String source_data) {
        return gnbCmdAck(source_data);
    }


    /**
     * 读取GPS输入输出端口
     *
     * @param source_data
     * @return
     */
    public GnbGpsInOutRsp gnbGetGpsInOutRsp(String source_data) {
        GnbGpsInOutRsp gpsInOutRsp = new GnbGpsInOutRsp();
        String[] data = source_data.split(",");
        if (data.length >= 28) {
            int idx = 8;
            String s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
            int cmd_type = DataUtil.str2Int(s, 16);
            if (cmd_type == DWProtocol.OAM_MSG_GET_GPS_IO_CFG) { //
                idx = 16;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                gpsInOutRsp.setOutGpioIdx(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                gpsInOutRsp.setInGpioIdx(DataUtil.str2Int(s, 16));
            }
        }
        return gpsInOutRsp;
    }

    /**
     * 读取GPS输入输出端口
     *
     * @param source_data
     * @return
     */
    public GnbGpsRsp gnbGetGpsRsp(String source_data) {
        GnbGpsRsp gpsRsp = new GnbGpsRsp();
        String[] data = source_data.split(",");
        if (data.length >= 44) {
            int idx = 8;
            String s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
            int cmd_type = DataUtil.str2Int(s, 16);
            if (cmd_type == DWProtocol.OAM_MSG_GET_GPS_CFG) {
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
            }
        }
        return gpsRsp;
    }

    /**
     * 设置转发UDP报文
     *
     * @param source_data
     * @return
     */

    public GnbCmdRsp gnbSetForwardUdpMsg(String source_data) {
        return gnbCmdAck(source_data);
    }

    /**
     * 开始频段扫频
     *
     * @param source_data
     * @return
     */
    public GnbCmdRsp gnbStartBandScan(String source_data) {
        return gnbCmdAck(source_data);
    }

    /**
     * 读写用户数据
     *
     * @param source_data
     * @return
     */
    public GnbUserDataRsp gnbGetUserDataRsp(String source_data) {
        GnbUserDataRsp userDataRsp = new GnbUserDataRsp();
        String[] data = source_data.split(",");
        if (data.length >= 284) {
            int idx = 8;
            String s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
            int cmd_type = DataUtil.str2Int(s, 16);
            if (cmd_type == DWProtocol.OAM_MSG_RW_USER_DATA) {
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
                        user_data[ret] = DataUtil.str2Byte(data[i], 16);
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

    public GnbGpioRsp gnbGetGpioRsp(String source_data) {
        GnbGpioRsp gpioRsp = new GnbGpioRsp();
        String[] data = source_data.split(",");
        if (data.length >= 35) {
            int idx = 8;
            String s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
            int cmd_type = DataUtil.str2Int(s, 16);
            if (cmd_type == DWProtocol.OAM_MSG_GET_GPIO_MODE) { //
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
                gpioRsp.setGpio7(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                gpioRsp.setGpio8_tddSw2(DataUtil.str2Int(s, 16));

                SdkLog.D(gpioRsp.toString());
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
            if (cmd_type == DWProtocol.OAM_MSG_GET_SYS_INFO) { //
                idx = 16;
                byte[] dev_buffer = new byte[DWProtocol.OAM_STR_MAX];
                int ret = 0;
                for (int i = idx; i < idx + DWProtocol.OAM_STR_MAX; i++) {
                    if (i >= data.length) {
                        break;
                    }
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        dev_buffer[ret] = DataUtil.str2Byte(data[i], 16);
                        ret++;
                    }
                }
                String dev_name = DataUtil.Asc2String(dev_buffer);
                dev_name = dev_name.substring(0, ret);
                sysInfoRsp.setDevName(dev_name);
                //ftp_user
                idx += DWProtocol.OAM_STR_MAX;
                byte[] license_buffer = new byte[256];
                ret = 0;
                for (int i = idx; i < idx + 256; i++) {
                    if (i >= data.length) {
                        break;
                    }
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        license_buffer[ret] = DataUtil.str2Byte(data[i], 16);
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

    public GnbReadDataFwdRsp gnbReadDataFwd(String source_data) {
        GnbReadDataFwdRsp rsp = new GnbReadDataFwdRsp();
        String[] data = source_data.split(",");
        if (source_data.contains("33,33,a5,5a,")) {
            data = source_data.split("33,33,a5,5a,");
        } else if (source_data.contains("33,33,a5,5a"))
            data = source_data.split("33,33,a5,5a");
        if (data.length >= 20) {
            try {
                int idx = 8;
                String s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                int cmd_type = DataUtil.str2Int(s, 16);
                if (cmd_type == DWProtocol.OAM_MSG_SET_DATA_FWD) {
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
                        ver[ret] = DataUtil.str2Byte(data[idx], 16);
                        ret++;
                        idx++;
                    }
                    String str = DataUtil.Asc2String(ver);
                    str = str.substring(0, ret);
                    rsp.setData(str);
                }
            } catch (Exception e) {
                SdkLog.E("readDataFwd Error:" + e.getMessage());
            }
        }
        return rsp;
    }

    /**
     * 通道配置回调
     */
    public GnbCmdRsp gnbSetDualCellRsp(String source_data) {
        return gnbCmdAck(source_data);
    }

    public JSONObject gnbGetCellCfgRsp(String id, String source_data) {
        JSONObject json = new JSONObject();
        String[] data = source_data.split(",");
        if (data.length >= 20) {
            int idx = 8;
            String s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
            int cmd_type = DataUtil.str2Int(s, 16);
            try {
                json.put("cmd_type", cmd_type);

                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                int cmd_param = DataUtil.str2Int(s, 16);
                json.put("cmd_param", cmd_param);

                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                json.put("cell_id", DataUtil.str2Int(s, 16));
                int[] cfg;
                String[] keys;
                JSONObject cfg_data;
                int k;
                switch (cmd_param) {
                    case 0:
                        cfg = parseData(idx, 6, data);
                        keys = new String[]{"is_lte", "gnb_state", "dl_arfcn", "ul_arfcn", "tx_atten", "rx_gain"};
                        for (int i = 0; i < keys.length; i++) json.put(keys[i], cfg[i]);
                        break;
                    case 10:
                    case 110:
                        cfg_data = new JSONObject();
                        idx += 8;
                        cfg = parseData(idx, 41, data);

                        keys = new String[]{"cmd_type", "cmd_param", "cell_id", "plmn", "DL_NR_ARFCN", "UL_NR_ARFCN", "PCI", "TAC", "PA", "PK",
                                "ue_max_pwr", "timing_offset", "work_mode", "air_sync_enable", "plmn2", "ul_rb_offset", "cid", "ssb", "frame_type",
                                "bandwidth", "redirect_cfg", "rxLevMin", "redirect_2_4g_earfcn", "mob_reject_code", "split_cid",
                                "split_pci", "split_pa", "split_pk", "split_arfcn_dl", "split_arfcn_ul", "async_arfcn", "force_cfg", "cfr", "swap_rf"};
                        k = 0;
                        for (String key : keys) {
                            switch (key) {
                                case "plmn":
                                case "plmn2":
                                    int MCC = cfg[k++];
                                    int MNC = cfg[k++];
                                    String plmn_MNC;
                                    if (MNC > 1000) plmn_MNC = "0" + (MNC - 1000);
                                    else plmn_MNC = MNC < 10 ? ("0" + MNC) : ("" + MNC);
                                    cfg_data.put(key, MCC + plmn_MNC);
                                    break;
                                case "cid":
                                    int cid1 = cfg[k++];
                                    int cid2 = cfg[k++];
                                    cfg_data.put(key, ((long) cid2 << 32 & 0xFFFFFFFF00000000L) | ((long) cid1 & 0xFFFFFFFFL));
                                    break;
                                case "async_arfcn":
                                    k += 3; // 跳过4个预留,上一个循环已+1，在这+3
                                    cfg_data.put(key, cfg[k++]);
                                    break;
                                default:
                                    cfg_data.put(key, cfg[k++]);
                                    break;
                            }
                        }
                        // Json叠加cfg_data Json
                        json.put("cfg_data", cfg_data);
                        MessageController.build().recoverTraceList(new TracePara(id, cmd_param == 110, cfg_data.getInt("cell_id"), "",
                                cfg_data.getString("plmn"), String.valueOf(cfg_data.getInt("DL_NR_ARFCN")), String.valueOf(cfg_data.getInt("PCI")),
                                String.valueOf(cfg_data.getInt("ue_max_pwr")), cfg_data.getInt("TAC"), cfg_data.getInt("TAC") + DWProtocol.MAX_TAC_NUM,
                                cfg_data.getInt("timing_offset"), cfg_data.getInt("work_mode"), cfg_data.getInt("air_sync_enable"), cfg_data.getString("plmn2"),
                                cfg_data.getInt("ul_rb_offset"), cfg_data.getInt("cid"), cfg_data.getInt("ssb"), cfg_data.getInt("bandwidth"),
                                cfg_data.getInt("cfr"), cfg_data.getInt("swap_rf"), cfg_data.getInt("redirect_cfg"), cfg_data.getInt("rxLevMin"),
                                cfg_data.getInt("redirect_2_4g_earfcn"), cfg_data.getInt("mob_reject_code"), String.valueOf(cfg_data.getInt("split_arfcn_ul")),
                                cfg_data.getInt("force_cfg"), String.valueOf(cfg_data.getInt("split_pci"))));
                        break;
                    case 11:
                    case 111:
                        cfg_data = new JSONObject();
                        cfg = parseData(idx, 4, data);
                        keys = new String[]{"cmd_type", "cmd_param", "cell_id", "imsi_num"};
                        for (int i = 0; i < keys.length; i++) cfg_data.put(keys[i], cfg[i]);
                        String[] ue_id = parseUe(idx, cfg[3], data);
                        cfg_data.put("ue_id", ue_id);
                        // Json叠加cfg_data Json
                        json.put("cfg_data", cfg_data);
                        break;
                    case 15:
                    case 115:
                        cfg_data = new JSONObject();
                        cfg = parseData(idx, 4, data);
                        keys = new String[]{"cmd_type", "cmd_param", "cell_id", "target_num"};
                        for (int i = 0; i < keys.length; i++) cfg_data.put(keys[i], cfg[i]);
                        String[] target_ue = parseUe(idx, 3, data);
                        cfg_data.put("target_ue", target_ue);
                        // Json叠加cfg_data Json
                        json.put("cfg_data", cfg_data);
                        break;
                    case 13:
                    case 113:
                        cfg_data = new JSONObject();
                        cfg = parseData(idx, 39, data);
                        keys = new String[]{"cmd_type", "cmd_param", "cell_id", "save_flag", "start_tac", "end_tac",
                                "tac_interval", "pci_mode", "pci_list", "cid_mode", "cid_list"};
                        k = 0;
                        for (String key : keys) {
                            switch (key) {
                                case "pci_list":
                                    int[] pci_list = new int[]{cfg[k++], cfg[k++], cfg[k++], cfg[k++], cfg[k++], cfg[k++], cfg[k++], cfg[k++], cfg[k++], cfg[k++]};
                                    cfg_data.put(key, pci_list);
                                    break;
                                case "cid_list":
                                    long[] cid_list = new long[10];
                                    for (int i = 0; i < 10; i++) {
                                        int cid1 = cfg[k++];
                                        int cid2 = cfg[k++];
                                        cid_list[i] = ((long) cid1 << 32 & 0xFFFFFFFF00000000L) | ((long) cid2 & 0xFFFFFFFFL);
                                    }
                                    cfg_data.put(key, cid_list);
                                    break;
                                default:
                                    cfg_data.put(key, cfg[k++]);
                                    break;
                            }
                        }
                        // Json叠加cfg_data Json
                        json.put("cfg_data", cfg_data);
                        break;
                    case 30:
                    case 130:
                        cfg_data = new JSONObject();
                        cfg = parseData(idx, 4, data);
                        keys = new String[]{"cmd_type", "cmd_param", "cell_id", "mode"};
                        for (int i = 0; i < keys.length; i++) cfg_data.put(keys[i], cfg[i]);
                        // Json叠加cfg_data Json
                        json.put("cfg_data", cfg_data);
                        break;
                    case 12:
                        cfg_data = new JSONObject();
                        cfg = parseData(idx, 5, data);
                        keys = new String[]{"cmd_type", "cmd_param", "cell_id", "nr_arfcn", "tx_atten"};
                        for (int i = 0; i < keys.length; i++) cfg_data.put(keys[i], cfg[i]);
                        // Json叠加cfg_data Json
                        json.put("cfg_data", cfg_data);
                        break;
                    case 161:
                        cfg_data = new JSONObject();
                        cfg = parseData(idx, 8, data);
                        keys = new String[]{"cmd_type", "cmd_param", "cell_id", "nr_arfcn", "REJ_CAUSE_CHANGE_ENABLE", "REJ_CAUSE9_COUNT", "REJ_CAUSE9_TIME", "REJ_IMSI_DEDUPLICATE"};
                        for (int i = 0; i < keys.length; i++) cfg_data.put(keys[i], cfg[i]);
                        // Json叠加cfg_data Json
                        json.put("cfg_data", cfg_data);
                        break;
                }

            } catch (Exception e) {
                SdkLog.E("gnbGetCellCfgRsp Error:" + e.getMessage());
            }
        }
        return json;
    }

    private String[] parseUe(int idx, int size, String[] data) {
        String[] ue = new String[size];
        for (int j = 0; j < size; j++) {
            StringBuilder imsi = new StringBuilder();
            StringBuilder guti = new StringBuilder();

            for (int i = idx; i < idx + DWProtocol.MAX_IMSI_LEN; i++) {
                if (i >= data.length) break;
                if (data[i].equals("00")) break;
                else imsi.append(data[i].substring(1));
            }
            for (int i = idx + DWProtocol.MAX_IMSI_LEN; i < idx + DWProtocol.MAX_IMSI_LEN * 2; i++) {
                if (i >= data.length) break;
                if (data[i].equals("00")) break;
                else {
                    int decimal = DataUtil.str2Int(data[i], 16);
                    char character = (char) decimal; // 转换为字符
                    guti.append(character);
                }
            }

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("imsi", imsi.toString());
                jsonObject.put("guti", guti.toString());
                ue[j] = jsonObject.toString();
            } catch (JSONException e) {
                SdkLog.E("parseUe Error:" + e.getMessage());
            }

        }
        return ue;
    }

    private int[] parseData(int idx, int cfgSize, String[] data){
        int[] cfg = new int[cfgSize];
        int i = 0;
        byte[] bytes = new byte[]{0,0,0,0};
        while ((idx += 4) < data.length && i < cfgSize){
            for (int j = 0; j < 4; j++){
                bytes[j] = (byte) Integer.parseInt(data[idx + 3 - j], 16);
            }
            cfg[i] = DataUtil.bytesToInt2(bytes);
            i++;
        }
        return cfg;
    }

    public GnbCmdRsp gnbRedirectUeRsp(String id, String source_data) {
        return gnbCmdAck(id, source_data);
    }
    public GnbCmdRsp gnbResetPlmnRsp(String id, String source_data) {
        return gnbCmdAck(id, source_data);
    }
    public GnbCmdRsp gnbSetPerPwrRsp(String id, String source_data) {
        return gnbCmdAck(id, source_data);
    }

    public MsgCmdRsp gnbCmdRsp(String msg) {
        MsgCmdRsp msgCmdRsp = null;
        String[] uartdata = msg.split("33,33,a5,5a,");
        for (String uartdatum : uartdata) {
            String[] data = uartdatum.split(",");
            if (data.length > 12) {
                int idx = 4;
                String tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                int msg_sn = DataUtil.str2Int(tmp, 16);
                idx += 4;
                tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                int msg_len = DataUtil.str2Int(tmp, 16);
                idx += 4;
                tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                int msg_type = DataUtil.str2Int(tmp, 16);
                idx += 4;
                tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                int rsp_value = DataUtil.str2Int(tmp, 16);
                msgCmdRsp = new MsgCmdRsp(msg_sn, msg_len, msg_type, rsp_value);
            }
        }
        return msgCmdRsp;
    }

    /**
     * 各指令配置反馈信息解析
     */
    public MsgStateRsp DBHeartState(String msg) {
        if (null == msg) {
            return null;
        }
        // HEART: 64
        // 11,11,a5,5a,
        // 05,00,00,00, msg_sn
        // 40,00,00,00, msg_len
        // 01,00,00,00, msg_type
        // 0c,00,00,00, param
        // 01,00,00,00, work_mode
        // 10,79,4d,8a, battery_voltage
        // c0,a8,64,94, wifi_ipaddr
        // c8,c3,f7,8e, board_temp
        // 7f,00,00,00, gps_state;
        // 20,0b,00,74, async state
        // 7f,00,00,00, jam auto_cfg
        // 20,0b,00,74, resv[4]
        // 7f,00,00,00,
        // 00,00,00,00,
        // 33,33,a5,5a

        // TODO: 2023/2/16 新增设备名称
        // device name         dev name[32]

        // CMD GLB RSP : 24
        // 11,11,a5,5a,
        // 01,00,00,00, msg_sn
        // 18,00,00,00, msg_len
        // 01,00,00,00, msg_type
        // 00,00,00,00, rsp_value
        // 33,33,a5,5a,
        MsgStateRsp state = null;
        String[] uartdata = msg.split("33,33,a5,5a,");
        for (String uartdatum : uartdata) {
            String[] data = uartdatum.split(",");
            if (data.length > 12) {
                int idx = 4;
                String tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                int msg_sn = DataUtil.str2Int(tmp, 16);
                idx += 4;
                tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                int msg_len = DataUtil.str2Int(tmp, 16);
                idx += 4;
                tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                int msg_type = DataUtil.str2Int(tmp, 16);
                idx += 4;
                if (msg_type == DBProtocol.MsgType.GR_MSG_HELLO) { // 心跳上报
                    if (idx >= data.length) {
                        break;
                    }
                    //tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    int param = 0;//DataUtil.str2Int(tmp, 16);
                    idx += 4;
                    if (idx >= data.length) {
                        break;
                    }
                    tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    int work_mode = DataUtil.str2Int(tmp, 16);
                    idx += 4;
                    if (idx >= data.length) {
                        break;
                    }
                    tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    int vol = DataUtil.str2Int(tmp, 16);
                    if (DwDbSdk.build().isPredator()) {
                        BatteryPredator.build().handleVol(vol);
                    } else {
                        Battery.build().handleVol(vol);
                    }
                    idx += 4;
                    if (idx >= data.length) {
                        break;
                    }
                    //tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    String hostIp = DataUtil.str2Int(data[idx], 16) + "." + DataUtil.str2Int(data[idx + 1], 16)
                            + "." + DataUtil.str2Int(data[idx + 2], 16) + "." + DataUtil.str2Int(data[idx + 3], 16);
                    idx += 4;
                    if (idx >= data.length) {
                        break;
                    }
                    tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    int temp = DataUtil.str2Int(tmp, 16);

                    idx += 4;
                    if (idx >= data.length) {
                        break;
                    }
                    tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    int gps_state = DataUtil.str2Int(tmp, 16);

                    idx += 4;
                    if (idx >= data.length) {
                        break;
                    }
                    //tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    tmp = data[idx + 1] + data[idx];
                    int async_state = DataUtil.str2Int(tmp, 16);
                    AirState.build().setAirSyncState(async_state);

                    tmp = data[idx + 3] + data[idx + 2]; // pci
                    int pci = DataUtil.str2Int(tmp, 16);
                    AirState.build().setPci(pci);

                    if (msg_len > 68) {
                        idx += 4;
                        int autoCfgState = -1;
                        if (idx < data.length) {
                            tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                            autoCfgState = DataUtil.str2Int(tmp, 16);
                        }
                        idx += 20;
//                    idx +=32;
                        byte[] b_dev_name = new byte[32];
                        int ret = 0;
                        for (int i = idx; i < idx + 32; i++) {
                            if (i >= data.length) {
                                break;
                            }
                            if (data[i].equals("00")) {
                                break;
                            } else {
                                b_dev_name[ret] = DataUtil.str2Byte(data[i], 16);
                                ret++;
                            }
                        }
                        String dev_name = DataUtil.Asc2String(b_dev_name);
                        dev_name = dev_name.substring(0, ret);
                        dev_name = dev_name.replaceAll("\"", "");
                        dev_name = dev_name.replaceAll("\r|\n", "");
                        idx += 32;
                        String dev_sn = "";
                        if (idx + 32 < data.length) {
                            byte[] b_dev_sn = new byte[32];
                            ret = 0;
                            for (int i = idx; i < idx + 32; i++) {
                                if (data[i].equals("00")) {
                                    break;
                                } else {
                                    b_dev_sn[ret] = DataUtil.str2Byte(data[i], 16);
                                    ret++;
                                }
                            }
                            dev_sn = DataUtil.Asc2String(b_dev_sn);
                        }
                        if (!dev_sn.isEmpty()) {
                            dev_sn = dev_sn.substring(0, ret);
                            dev_sn = dev_sn.replaceAll("\"", "");
                            dev_sn = dev_sn.replaceAll("\r|\n", "");
                        }
                        state = new MsgStateRsp(msg_sn, msg_len, msg_type, param, work_mode, vol, temp, gps_state, async_state, pci, autoCfgState, hostIp, dev_name, dev_sn);
                    } else {
                        state = new MsgStateRsp(msg_sn, msg_len, msg_type, param, work_mode, vol, temp, gps_state, async_state, pci, -1, hostIp, "null", "");
                    }
                } else { // GLB CMP RSP
                    if (idx >= data.length) {
                        break;
                    }
                    tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    int rsp_value = DataUtil.str2Int(tmp, 16);
                    state = new MsgStateRsp(msg_sn, msg_len, msg_type, rsp_value);
                }
            }
        }
        return state;
    }

    /**
     * 基带版本信息
     */
    public MsgVersionRsp getVersion(String msg) {
        if (null == msg) {
            return null;
        }
        // length: 280,
        // msg: 11,11,a5,5a,
        // 12,00,00,00,
        // 18,01,00,00,
        // 03,00,00,00,
        // 00,00,00,00,
        // 56,45,52,53,49,4f,4e,3a,20,32,30,32,31,31,30,33,30,0a,20,0a,46,50,47,41,20,30,78,30,32,31,35,61,30,30,31,0a,31,35,30,31,30,30,33,38,34,37,35,34,34,36,33,34,35,32,30,36,31,66,63,65,34,66,38,65,38,37,30,30,0a,0a,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,31,31,2d,30,32,5f,30,34,2d,30,30,2d,33,38,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,30,e6,9c,ad,7f,00,00,00,30,e6,9c,ad,7f,00,00,00,10,e6,9c,ad,7f,00,00,00,e0,ff,ff,ff,80,ff,ff,ff,11,00,00,00,27,00,00,00,25,25,25,25,25,25,25,25,25,25,25,25,25,25,25,25,25,73,00,00,00,00,00,00,6e,65,77,20,73,6f,63,6b,20,25,64,2c,20,61,70,70,5f,6e,61,6d,65,20,25,73,00,00,ff,ff,
        // 33,33,a5,5a
        boolean hasData = false;
        String[] uartdata = msg.split("33,33,a5,5a,");
        for (String uartdatum : uartdata) {
            String[] data = uartdatum.split(",");
            if (data.length > 12) {
                int idx = 4;
                String tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                int msg_sn = DataUtil.str2Int(tmp, 16);
                idx += 4;
                tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                int msg_len = DataUtil.str2Int(tmp, 16);
                idx += 4;
                tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                int msg_type = DataUtil.str2Int(tmp, 16);
                idx += 4;
                if (msg_type == DBProtocol.MsgType.GR_MSG_GET_VERSION) {
                    if (data.length > 20) {
                        hasData = true;
                        if (idx >= data.length) {
                            break;
                        }
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        int param = DataUtil.str2Int(tmp, 16);
                        idx += 4;
                        byte[] ver = new byte[256];
                        int ret = 0;
                        for (int i = 0; i < data.length; i++) {
                            if (idx >= data.length) {
                                break;
                            }
                            if (!data[idx].equals("00") && !data[idx].equals("ff")) {
                                ver[ret] = DataUtil.str2Byte(data[idx], 16);
                                ret++;
                                idx++;
                            }
                        }
                        String version = DataUtil.Asc2String(ver);
                        version = version.substring(0, ret);
                        //version = version.replaceAll(" ", "");
                        //version = version.replaceAll("\r|\n", "");
                        MsgVersionRsp versionRsp = new MsgVersionRsp(msg_sn, msg_len, msg_type, version);
                        SdkLog.D("getVersion: " + version);
                        return versionRsp;
                    }
                }
            }
        }
        MsgVersionRsp stateRsp = new MsgVersionRsp();
        MsgStateRsp ret = DBHeartState(msg);
        if (null != ret) {
            stateRsp.setStateRsp(ret);
            return stateRsp;
        }
        return null;
    }

    public GnbSetDataTo485Rsp gnbSetDataTo485Rsp(String source_data) {
        GnbSetDataTo485Rsp rsp = new GnbSetDataTo485Rsp();
        String[] data = source_data.split(",");
        if (data.length >= 20) {
            int idx = 8;
            String s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
            int cmd_type = DataUtil.str2Int(s, 16);
            rsp.setCmd_type(cmd_type);
            if (cmd_type == DWProtocol.OAM_MSG_PA_RW_CMD) {
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                rsp.setCmd_param(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                rsp.setMod_id(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                rsp.setMod_addr(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                rsp.setCmd_id(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                rsp.setCmd_ack(DataUtil.str2Int(s, 16));
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                rsp.setData_len(DataUtil.str2Int(s, 16));
                idx += 4;
                byte[] user_data = new byte[256];
                int ret = 0;
                for (int i = idx; i < idx + 256; i++) {
                    if (data[i].equals("00")) {
                        break;
                    } else {
                        user_data[ret] = DataUtil.str2Byte(data[i], 16);
                        ret++;
                    }
                }
                String userData = DataUtil.Asc2String(user_data);
                userData = userData.substring(0, ret);
                rsp.setData(userData);
            }
        }
        return rsp;
    }

    /**
     * 时间配置反馈信息
     */
    public MsgStateRsp setTime(String msg) {
        if (null == msg) {
            return null;
        }
        return DBHeartState(msg);
    }

    /**
     * 配置蓝牙名称
     */
    public MsgStateRsp setBtName(String msg) {
        if (null == msg) {
            return null;
        }
        return DBHeartState(msg);
    }

    /**
     * 配置设备名称
     */
    public MsgCmdRsp setDevName(String msg) {
        if (null == msg) {
            return null;
        }
        return gnbCmdRsp(msg);
    }

    /**
     * 配置WIFI名称密码
     */
    public MsgStateRsp configWifi(String msg) {
        if (null == msg) {
            return null;
        }
        return DBHeartState(msg);
    }

    /**
     * 获取基带LOG
     */
    public MsgStateRsp getLog(String msg) {
        if (null == msg) {
            return null;
        }
        return DBHeartState(msg);
    }

    /**
     * 基带升级
     */
    public MsgStateRsp setUpgrade(String msg) {
        if (null == msg) {
            return null;
        }
        return DBHeartState(msg);
    }

    /**
     * 重启基带
     */
    public MsgStateRsp setReboot(String msg) {
        if (null == msg) {
            return null;
        }
        return DBHeartState(msg);
    }

    /**
     * 启动SG干扰
     */
    public MsgStateRsp startSG(String msg) {
        if (null == msg) {
            return null;
        }
        return DBHeartState(msg);
    }

    /**
     * 结束SG干扰
     */
    public MsgStateRsp stopSG(String msg) {
        if (null == msg) {
            return null;
        }
        return DBHeartState(msg);
    }

    /**
     * 启动干扰
     */
    public MsgStateRsp startJam(String msg) {
        if (null == msg) {
            return null;
        }
        return DBHeartState(msg);
    }

    /**
     * 结束干扰
     */
    public MsgStateRsp stopJam(String msg) {
        if (null == msg) {
            return null;
        }
        return DBHeartState(msg);
    }

    /**
     * 启动扫描最佳UE_postion
     */
    public MsgScanRsp startScan(String msg) {
        if (null == msg) {
            return null;
        }
        boolean hasData = false;
        MsgScanRsp scanRsp = new MsgScanRsp();
        String[] uartdata = msg.split("33,33,a5,5a,");
        for (String uartdatum : uartdata) {
            String[] data = uartdatum.split(",");
            if (data.length > 12) {
                int idx = 4;
                String tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                int msg_sn = DataUtil.str2Int(tmp, 16);
                scanRsp.setMsgSn(msg_sn);

                idx += 4;
                tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                int msg_len = DataUtil.str2Int(tmp, 16);
                scanRsp.setMsgLen(msg_len);

                idx += 4;
                tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                int msg_type = DataUtil.str2Int(tmp, 16);
                scanRsp.setMsgType(msg_type);

                if (msg_type == DBProtocol.MsgType.GR_MSG_SCAN_REPORT) {
                    hasData = true;
                    idx += 4;
                    if (idx >= data.length) {
                        break;
                    }
                    tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    int result = DataUtil.str2Int(tmp, 16);

                    idx += 4;
                    if (idx >= data.length) {
                        break;
                    }
                    tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    int lock_state = DataUtil.str2Int(tmp, 16);

                    idx += 4;
                    if (idx >= data.length) {
                        break;
                    }
                    tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    int signal_power = DataUtil.str2Int(tmp, 16);

                    idx += 4;
                    if (idx >= data.length) {
                        break;
                    }
                    tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    int best_position = DataUtil.str2Int(tmp, 16);

                    idx += 4;
                    if (idx >= data.length) {
                        break;
                    }
                    tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    int min_noise_power = DataUtil.str2Int(tmp, 16);

                    scanRsp.setLockState(lock_state);
                    scanRsp.setRsrp(signal_power);
                    scanRsp.setScanResult(result);
                    scanRsp.setBestPositionOrRssi(best_position);
                    scanRsp.setMinNoisePowerOrOverload(min_noise_power);
                    SdkLog.D("startScan: result : " + result + ", best_position: " + best_position + ", min_noise_power: " + min_noise_power);
                }
            }
        }
        if (!hasData) {
            MsgStateRsp ret = DBHeartState(msg);
            if (null != ret) {
                scanRsp.setStateRsp(ret);
            }
        }
        return scanRsp;
    }

    /**
     * 启动单兵检测
     */
    public MsgScanRsp startPwrDetect(String msg) {
        if (null == msg) {
            return null;
        }
        // 11,11,a5,5a,
        // cb,01,00,00,
        // 28,00,00,00,
        // 35,00,00,00,

        // 56,74,00,00, result:SCAN result [0 : fail    1: succ]
        // 01,00,00,00, lock_state:pwr detect [0:both unlock 1: cell_0 lock cell_1 unlock 2: cell_0 unlock cell_1 lock 3: both lock ]
        // 00,00,00,00, signal_power(rsrp)
        // 00,00,00,00, min_noise_power
        // 00,00,00,00, best_position
        // 33,33,a5,5a

        //00,00,00,00,
        //01,00,00,00,
        //3f,00,00,00,
        //00,00,00,00,
        //00,00,00,00,
        boolean hasData = false;
        MsgScanRsp scanRsp = new MsgScanRsp();
        String[] uartdata = msg.split("33,33,a5,5a,");
        for (String uartdatum : uartdata) {
            String[] data = uartdatum.split(",");
            if (data.length > 12) {
                int idx = 4;
                String tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                int msg_sn = DataUtil.str2Int(tmp, 16);
                scanRsp.setMsgSn(msg_sn);

                idx += 4;
                tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                int msg_len = DataUtil.str2Int(tmp, 16);
                scanRsp.setMsgLen(msg_len);

                idx += 4;
                tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                int msg_type = DataUtil.str2Int(tmp, 16);
                scanRsp.setMsgType(msg_type);
                int lastMinLength = data.length - 4;
                if (msg_type == DBProtocol.MsgType.GR_MSG_POWER_REPORT) {
                    hasData = true;
                    idx += 4;
                    if (idx > lastMinLength) break;
                    tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    int result = DataUtil.str2Int(tmp, 16);

                    idx += 4;
                    if (idx > lastMinLength) break;
                    tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    int lock_state = DataUtil.str2Int(tmp, 16);

                    idx += 4;
                    if (idx > lastMinLength) break;
                    tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    int signal_power = DataUtil.str2Int(tmp, 16);

                    idx += 4;
                    if (idx > lastMinLength) break;
                    tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    int best_position = DataUtil.str2Int(tmp, 16);

                    idx += 4;
                    if (idx > lastMinLength) break;
                    tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    int min_noise_power = DataUtil.str2Int(tmp, 16);

                    scanRsp.setLockState(lock_state);
                    scanRsp.setRsrp(signal_power);
                    scanRsp.setScanResult(result);
                    scanRsp.setBestPositionOrRssi(best_position);
                    scanRsp.setMinNoisePowerOrOverload(min_noise_power);
                    //SdkLog.D("startPwrDetect: lock_state : " + lock_state  + ", rsrp: " + signal_power);
                }
            }
        }
        if (!hasData) {
            MsgStateRsp ret = DBHeartState(msg);
            if (null != ret) {
                scanRsp.setStateRsp(ret);
            }
        }
        return scanRsp;
    }

    /**
     * 结束单兵
     */
    public MsgStateRsp stopPwrDetect(String msg) {
        if (null == msg) {
            return null;
        }
        return DBHeartState(msg);
    }

    /**
     * 配置接收增益
     *
     * @param msg
     * @return
     */
    public MsgStateRsp setRxGain(String msg) {
        if (null == msg) {
            return null;
        }
        return DBHeartState(msg);
    }

    /**
     * 配置GPIO
     *
     * @param msg
     * @return
     */
    public MsgStateRsp setGpioCfg(String msg) {
        if (null == msg) {
            return null;
        }
        return DBHeartState(msg);
    }

    public MsgStateRsp onSetDeviceId(String msg) {
        if (null == msg) {
            return null;
        }
        return DBHeartState(msg);
    }

    public MsgGetJamRsp getJam(String msg) {
        /**
         * typedef struct {
         * 	int msg_header;      				//HEADER_MAGIC
         * 	int msg_sn;       					//serial num
         * 	int msg_len;      					//sizeof bt_msg_xxx_t
         * 	int msg_type;      					//BT_MSG_GET_JAM
         * 	int param;
         *
         * 	bt_msg_tx_t tx[2];     				//tx1 tx2
         * 	bt_msg_orx_t orx[8];   				//orx_list max = 8
         * 	int autoStartJam; 						//0-no save, 1-save
         * 	int msg_footer;      				//FOOTER_MAGIC
         * } bt_msg_start_jam_t; //down
         */
        MsgGetJamRsp jamRsp = new MsgGetJamRsp();
        String[] uartdata = msg.split("33,33,a5,5a,");
        /**
         *  typedef struct {
         *      int arfcn;
         *  	long lo_frequency;
         *  	int time_offset;
         *  	int re_cnt;
         *  	int re_list[8]; 				// re_list max = 8
         *     int arfcn[8];52+32=84
         *  } bt_msg_tx_t;
         */
        /**
         *  typedef struct {
         * 	 long freq_carrier; 8
         * 	 int pk;			12
         * 	 int pa;
         * 	 int time_offset;
         *  } bt_msg_orx_t;
         */
        for (String uartdatum : uartdata) {
            String[] data = uartdatum.split(",");
            if (data.length > 12) {
                int idx = 4;
                String tmp;
                //String tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                // msg_sn = DataUtil.str2Int(tmp, 16);
                //jamRsp.setMsgSn(msg_sn);
                idx += 4;
                //tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                //int msg_len = DataUtil.str2Int(tmp, 16);
                //jamRsp.setMsgLen(msg_len);
                idx += 4;
                tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                int msg_type = DataUtil.str2Int(tmp, 16);
                jamRsp.setMsgType(msg_type);
                if (msg_type == DBProtocol.MsgType.GR_MSG_GET_JAM) {
                    /**
                     *  typedef struct {
                     *      int arfcn;
                     *  	long lo_frequency;
                     *  	int time_offset;
                     *  	int re_cnt;
                     *  	int re_list[8]; 				// re_list max = 8
                     *     int arfcn[8];52+32=84
                     *  } bt_msg_tx_t;
                     */
                    long lo_frequency;
                    int time_offset, scsType, re_cnt;
                    List<Integer> arfcnList = new ArrayList<Integer>();
                    List<Integer> relist = new ArrayList<Integer>();
                    List<OrxBean> orxList = new ArrayList<OrxBean>();
                    if ((idx + MsgStartJam.TX_LEN) < data.length) {
                        idx += 4; //msg_type
                        idx += 4; //param
                        tmp = data[idx + 7] + data[idx + 6] + data[idx + 5] + data[idx + 4] + data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        lo_frequency = DataUtil.str2Long(tmp, 16);
                        idx += 8;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        time_offset = DataUtil.str2Int(tmp, 16);
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        scsType = DataUtil.str2Int(tmp, 16);
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        re_cnt = DataUtil.str2Int(tmp, 16);

                        relist.clear();
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        relist.add(DataUtil.str2Int(tmp, 16));
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        relist.add(DataUtil.str2Int(tmp, 16));
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        relist.add(DataUtil.str2Int(tmp, 16));
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        relist.add(DataUtil.str2Int(tmp, 16));
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        relist.add(DataUtil.str2Int(tmp, 16));
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        relist.add(DataUtil.str2Int(tmp, 16));
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        relist.add(DataUtil.str2Int(tmp, 16));
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        relist.add(DataUtil.str2Int(tmp, 16));

                        arfcnList.clear();
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        arfcnList.add(DataUtil.str2Int(tmp, 16));
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        arfcnList.add(DataUtil.str2Int(tmp, 16));
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        arfcnList.add(DataUtil.str2Int(tmp, 16));
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        arfcnList.add(DataUtil.str2Int(tmp, 16));
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        arfcnList.add(DataUtil.str2Int(tmp, 16));
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        arfcnList.add(DataUtil.str2Int(tmp, 16));
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        arfcnList.add(DataUtil.str2Int(tmp, 16));
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        arfcnList.add(DataUtil.str2Int(tmp, 16));
                        jamRsp.setTx1(new TxBean(lo_frequency, time_offset, scsType, re_cnt, relist, arfcnList));
                    }
                    if ((idx + MsgStartJam.TX_LEN) < data.length) {
                        idx += 4;
                        tmp = data[idx + 7] + data[idx + 6] + data[idx + 5] + data[idx + 4] + data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        lo_frequency = DataUtil.str2Long(tmp, 16);
                        idx += 8;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        time_offset = DataUtil.str2Int(tmp, 16);
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        scsType = DataUtil.str2Int(tmp, 16);
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        re_cnt = DataUtil.str2Int(tmp, 16);

                        relist.clear();
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        relist.add(DataUtil.str2Int(tmp, 16));
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        relist.add(DataUtil.str2Int(tmp, 16));
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        relist.add(DataUtil.str2Int(tmp, 16));
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        relist.add(DataUtil.str2Int(tmp, 16));
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        relist.add(DataUtil.str2Int(tmp, 16));
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        relist.add(DataUtil.str2Int(tmp, 16));
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        relist.add(DataUtil.str2Int(tmp, 16));
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        relist.add(DataUtil.str2Int(tmp, 16));

                        arfcnList.clear();
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        arfcnList.add(DataUtil.str2Int(tmp, 16));
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        arfcnList.add(DataUtil.str2Int(tmp, 16));
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        arfcnList.add(DataUtil.str2Int(tmp, 16));
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        arfcnList.add(DataUtil.str2Int(tmp, 16));
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        arfcnList.add(DataUtil.str2Int(tmp, 16));
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        arfcnList.add(DataUtil.str2Int(tmp, 16));
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        arfcnList.add(DataUtil.str2Int(tmp, 16));
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        arfcnList.add(DataUtil.str2Int(tmp, 16));
                        jamRsp.setTx2(new TxBean(lo_frequency, time_offset, scsType, re_cnt, relist, arfcnList));
                    }
                    /**
                     *  typedef struct {
                     * 	 long freq_carrier; 8
                     * 	 int pk;			12
                     * 	 int pa;
                     * 	 int time_offset;
                     *  } bt_msg_orx_t;
                     */
                    long freq_carrier;
                    int pk, pa, arfcn;
                    if ((idx + MsgStartJam.ORX_LEN) < data.length) {
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        arfcn = DataUtil.str2Int(tmp, 16);
                        idx += 4;
                        tmp = data[idx + 7] + data[idx + 6] + data[idx + 5] + data[idx + 4] + data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        freq_carrier = DataUtil.str2Long(tmp, 16);
                        idx += 8;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        pk = DataUtil.str2Int(tmp, 16);
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        pa = DataUtil.str2Int(tmp, 16);
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        time_offset = DataUtil.str2Int(tmp, 16);
                        orxList.add(new OrxBean(arfcn, freq_carrier, pk, pa, time_offset));
                    }
                    if ((idx + MsgStartJam.ORX_LEN) < data.length) {
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        arfcn = DataUtil.str2Int(tmp, 16);
                        idx += 4;
                        tmp = data[idx + 7] + data[idx + 6] + data[idx + 5] + data[idx + 4] + data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        freq_carrier = DataUtil.str2Long(tmp, 16);
                        idx += 8;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        pk = DataUtil.str2Int(tmp, 16);
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        pa = DataUtil.str2Int(tmp, 16);
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        time_offset = DataUtil.str2Int(tmp, 16);
                        orxList.add(new OrxBean(arfcn, freq_carrier, pk, pa, time_offset));
                    }
                    if ((idx + MsgStartJam.ORX_LEN) < data.length) {
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        arfcn = DataUtil.str2Int(tmp, 16);
                        idx += 4;
                        tmp = data[idx + 7] + data[idx + 6] + data[idx + 5] + data[idx + 4] + data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        freq_carrier = DataUtil.str2Long(tmp, 16);
                        idx += 8;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        pk = DataUtil.str2Int(tmp, 16);
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        pa = DataUtil.str2Int(tmp, 16);
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        time_offset = DataUtil.str2Int(tmp, 16);
                        orxList.add(new OrxBean(arfcn, freq_carrier, pk, pa, time_offset));
                    }
                    if ((idx + MsgStartJam.ORX_LEN) < data.length) {
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        arfcn = DataUtil.str2Int(tmp, 16);
                        idx += 4;
                        tmp = data[idx + 7] + data[idx + 6] + data[idx + 5] + data[idx + 4] + data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        freq_carrier = DataUtil.str2Long(tmp, 16);
                        idx += 8;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        pk = DataUtil.str2Int(tmp, 16);
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        pa = DataUtil.str2Int(tmp, 16);
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        time_offset = DataUtil.str2Int(tmp, 16);
                        orxList.add(new OrxBean(arfcn, freq_carrier, pk, pa, time_offset));
                    }
                    if ((idx + MsgStartJam.ORX_LEN) < data.length) {
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        arfcn = DataUtil.str2Int(tmp, 16);
                        idx += 4;
                        tmp = data[idx + 7] + data[idx + 6] + data[idx + 5] + data[idx + 4] + data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        freq_carrier = DataUtil.str2Long(tmp, 16);
                        idx += 8;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        pk = DataUtil.str2Int(tmp, 16);
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        pa = DataUtil.str2Int(tmp, 16);
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        time_offset = DataUtil.str2Int(tmp, 16);
                        orxList.add(new OrxBean(arfcn, freq_carrier, pk, pa, time_offset));
                    }
                    if ((idx + MsgStartJam.ORX_LEN) < data.length) {
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        arfcn = DataUtil.str2Int(tmp, 16);
                        idx += 4;
                        tmp = data[idx + 7] + data[idx + 6] + data[idx + 5] + data[idx + 4] + data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        freq_carrier = DataUtil.str2Long(tmp, 16);
                        idx += 8;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        pk = DataUtil.str2Int(tmp, 16);
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        pa = DataUtil.str2Int(tmp, 16);
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        time_offset = DataUtil.str2Int(tmp, 16);
                        orxList.add(new OrxBean(arfcn, freq_carrier, pk, pa, time_offset));
                    }
                    if ((idx + MsgStartJam.ORX_LEN) < data.length) {
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        arfcn = DataUtil.str2Int(tmp, 16);
                        idx += 4;
                        tmp = data[idx + 7] + data[idx + 6] + data[idx + 5] + data[idx + 4] + data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        freq_carrier = DataUtil.str2Long(tmp, 16);
                        idx += 8;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        pk = DataUtil.str2Int(tmp, 16);
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        pa = DataUtil.str2Int(tmp, 16);
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        time_offset = DataUtil.str2Int(tmp, 16);
                        orxList.add(new OrxBean(arfcn, freq_carrier, pk, pa, time_offset));
                    }
                    if ((idx + MsgStartJam.ORX_LEN) < data.length) {
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        arfcn = DataUtil.str2Int(tmp, 16);
                        idx += 4;
                        tmp = data[idx + 7] + data[idx + 6] + data[idx + 5] + data[idx + 4] + data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        freq_carrier = DataUtil.str2Long(tmp, 16);
                        idx += 8;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        pk = DataUtil.str2Int(tmp, 16);
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        pa = DataUtil.str2Int(tmp, 16);
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        time_offset = DataUtil.str2Int(tmp, 16);
                        orxList.add(new OrxBean(arfcn, freq_carrier, pk, pa, time_offset));
                    }
                    jamRsp.setOrxList(orxList);
                    if ((idx + 4) < data.length) {
                        idx += 4;
                        tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                        jamRsp.setAutoStartJam(DataUtil.str2Int(tmp, 16));
                    }
                    SdkLog.D(jamRsp.toString());
                }
            }
        }
        return jamRsp;
    }

    public MsgReadDataFwdRsp readDataFwd(String msg) {
        if (null == msg) {
            return null;
        }
        // length: 280,
        // msg: 11,11,a5,5a,
        // 12,00,00,00,
        // 18,01,00,00,
        // 03,00,00,00,
        // 00,00,00,00,
        // 56,45,52,53,49,4f,4e,3a,20,32,30,32,31,31,30,33,30,0a,20,0a,46,50,47,41,20,30,78,30,32,31,35,61,30,30,31,0a,31,35,30,31,30,30,33,38,34,37,35,34,34,36,33,34,35,32,30,36,31,66,63,65,34,66,38,65,38,37,30,30,0a,0a,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,31,31,2d,30,32,5f,30,34,2d,30,30,2d,33,38,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,30,e6,9c,ad,7f,00,00,00,30,e6,9c,ad,7f,00,00,00,10,e6,9c,ad,7f,00,00,00,e0,ff,ff,ff,80,ff,ff,ff,11,00,00,00,27,00,00,00,25,25,25,25,25,25,25,25,25,25,25,25,25,25,25,25,25,73,00,00,00,00,00,00,6e,65,77,20,73,6f,63,6b,20,25,64,2c,20,61,70,70,5f,6e,61,6d,65,20,25,73,00,00,ff,ff,
        // 33,33,a5,5a
        try {
            String[] uartdata = msg.split("33,33,a5,5a,");
            for (int m = 0; m < uartdata.length; m++) {
                String[] data = uartdata[m].split(",");
                if (data.length > 12) {
                    int idx = 4;
                    String tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    int msg_sn = DataUtil.str2Int(tmp, 16);
                    idx += 4;
                    tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    int msg_len = DataUtil.str2Int(tmp, 16);
                    idx += 4;
                    tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    int msg_type = DataUtil.str2Int(tmp, 16);
                    idx += 4;
                    if (msg_type == DBProtocol.MsgType.GR_MSG_DATA_FWD) {
                        if (data.length > 20) {
                            tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                            int param = DataUtil.str2Int(tmp, 16);
                            idx += 4;
                            int fixCount = data[data.length - 1].equals("5a") ? 4 : 0;
                            byte[] ver = new byte[data.length - 20 - fixCount];
                            int ret = 0;
                            int size = data.length - fixCount;
                            for (int i = idx; i < size; i++) {
                                ver[ret] = DataUtil.str2Byte(data[idx], 16);
                                ret++;
                                idx++;
                            }
                            String str = DataUtil.Asc2String(ver);
                            str = str.substring(0, ret);
                            MsgReadDataFwdRsp rsp = new MsgReadDataFwdRsp(msg_sn, msg_len, msg_type, str);
                            //SdkLog.D("getData: " + str);
                            return rsp;
                        }
                    }
                }
            }
        } catch (Exception e) {
            SdkLog.D("readDataFwd Error");
        }
        return null;
    }

    public MsgCmdRsp startSense(String msg) {
        if (null == msg) {
            return null;
        }
        MsgCmdRsp ret = gnbCmdRsp(msg);
        if (null != ret) {
            return ret;
        }
        return null;
    }

    /**
     * 结束灵敏度测试
     */
    public MsgCmdRsp stopSense(String msg) {
        if (null == msg) {
            return null;
        }
        return gnbCmdRsp(msg);
    }

    public MsgSenseReportRsp senseReport(String msg) {
        if (null == msg) {
            return null;
        }
        try {
            String[] uartdata = msg.split("33,33,a5,5a,");
            for (int m = 0; m < uartdata.length; m++) {
                String[] data = uartdata[m].split(",");
                if (data.length > 16) {
                    int idx = 4;
                    String tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    int msg_sn = DataUtil.str2Int(tmp, 16);
                    idx += 4;
                    tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    int msg_len = DataUtil.str2Int(tmp, 16);
                    idx += 4;
                    tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    int msg_type = DataUtil.str2Int(tmp, 16);
                    idx += 4;
                    tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];

                    idx += 4;
                    tmp = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                    int sensitivity = DataUtil.str2Int(tmp, 16);
                    MsgSenseReportRsp rsp = new MsgSenseReportRsp(msg_sn, msg_len, msg_type, sensitivity);
                    return rsp;
                }
            }
        } catch (Exception e) {
            SdkLog.D("senseReport Error");
        }
        return null;
    }

    public MsgCmdRsp startSSDetect(String msg) {
        if (null == msg) {
            return null;
        }
        return gnbCmdRsp(msg);
    }

}
