package com.nr.Socket.MessageControl;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.Logcat.SLog;
import com.nr.Gnb.Bean.GnbProtocol;
import com.nr.Gnb.Bean.MsgBean;
import com.nr.Gnb.Response.GnbCmdRsp;
import com.nr.Socket.ZTcpService;
import com.nr.Util.DataUtil;

public class MessageTransceiver {

    public static MessageTransceiver build() {
        synchronized (MessageTransceiver.class) {
            if (instance == null) {
                instance = new MessageTransceiver();
            }
        }
        return instance;
    }

    private MessageTransceiver() {
        startThread();
    }

    public void startThread(){
        if (mSendTread == null) {
            mSendTread = new SendThread();
        }
        mSendTread.start();
    }
    public void stopThread(){
        if (mSendTread != null) {
            mSendTread.interrupt();
            mSendTread = null;
        }
    }

    /**
     * 数据收发后台服务，初始化SDK后，由APP层调用
     * @param client ZTcpService
     */
    public void setClient(ZTcpService client) {
        this.client = client;
    }

    public void setMessageObserver(MessageController.MessageObserver observer) {
        this.messageObserver = observer;
    }

    private void dispatch(final String id, int msgType, int cmdType, final String data) {
        if (msgType == GnbProtocol.gNB_2_UI_REPORT_UE_INFO || msgType == GnbProtocol.gNB_2_UI_LTE_REPORT_UE_INFO) {
            switch (MessageController.build().getTraceType(id)) {

                case GnbProtocol.TraceType.CONTROL:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onStartControlRsp(id, MessageHelper.build().gnbStartControl(id, data));
                        }
                    });
                    break;
                case GnbProtocol.TraceType.CATCH:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onStartCatchRsp(id, MessageHelper.build().gnbStartCatch(id, data));
                        }
                    });
                    break;
                case GnbProtocol.TraceType.TRACE:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onStartTraceRsp(id, MessageHelper.build().gnbStartTrace(id, data));
                        }
                    });
                    break;
                default:
                    break;
            }
        } else if (msgType == GnbProtocol.UI_2_gNB_OAM_MSG){
            SLog.D("dispatch id " + id + "[" + msgType + ", " + cmdType + "] " + data);
            switch (cmdType) {
                case GnbProtocol.UI_2_gNB_HEART_BEAT:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            ZTcpService.build().setHeartTime(id);
                            messageObserver.onHeartStateRsp(MessageHelper.build().gnbHeartState(id, data));
                        }
                    });
                    break;
                case GnbProtocol.UI_2_gNB_START_CONTROL:
                case GnbProtocol.UI_2_eNB_START_CONTROL:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onStartControlRsp(id, MessageHelper.build().gnbStartControl(id, data));
                        }
                    });
                    break;
                case GnbProtocol.UI_2_gNB_STOP_CONTROL:
                case GnbProtocol.UI_2_eNB_STOP_CONTROL:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onStopControlRsp(id, MessageHelper.build().gnbStopControl(id, data));
                        }
                    });
                    break;
                case GnbProtocol.UI_2_gNB_START_TRACE:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onStartTraceRsp(id, MessageHelper.build().gnbStartTrace(id, data));
                        }
                    });
                    break;
                case GnbProtocol.UI_2_gNB_START_LTE_TRACE:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onStartLteTraceRsp(id, MessageHelper.build().gnbStartTrace(id, data));
                        }
                    });
                    break;
                case GnbProtocol.UI_2_gNB_STOP_TRACE:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onStopTraceRsp(id, MessageHelper.build().gnbStopTrace(id, data));
                        }
                    });
                    break;
                case GnbProtocol.UI_2_gNB_STOP_LTE_TRACE:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onStopLteTraceRsp(id, MessageHelper.build().gnbStopTrace(id, data));
                        }
                    });
                    break;
                case GnbProtocol.UI_2_gNB_START_CATCH:
                case GnbProtocol.UI_2_eNB_START_CATCH:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onStartCatchRsp(id, MessageHelper.build().gnbStartCatch(id, data));
                        }
                    });
                    break;
                case GnbProtocol.UI_2_gNB_STOP_CATCH:
                case GnbProtocol.UI_2_eNB_STOP_CATCH:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onStopCatchRsp(id, MessageHelper.build().gnbStopCatch(id, data));
                        }
                    });
                    break;
                case GnbProtocol.UI_2_gNB_SET_BLACK_UE_LIST:
                case GnbProtocol.UI_2_gNB_START_LTE_BLACK_UE_LIST:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onSetBlackListRsp(id, MessageHelper.build().gnbBlackListCfg(data));
                        }
                    });
                    break;
                case GnbProtocol.UI_2_gNB_CFG_gNB:
                case GnbProtocol.UI_2_eNB_CFG_gNB:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onSetGnbRsp(id, MessageHelper.build().gnbCfgGnb(data));
                        }
                    });
                    break;
                case GnbProtocol.UI_2_gNB_QUERY_gNB_VERSION:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onQueryVersionRsp(id, MessageHelper.build().gnbQueryVersion(data));
                        }
                    });
                    break;

                case GnbProtocol.UI_2_gNB_SET_TX_POWER_OFFSET:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onSetTxPwrOffsetRsp(id, MessageHelper.build().gnbSetTxPwrOffset(data));
                        }
                    });
                    break;
                case GnbProtocol.OAM_MSG_ADJUST_TX_ATTEN:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onSetNvTxPwrOffsetRsp(id, MessageHelper.build().gnbSetNvTxPwrOffset(data));
                        }
                    });
                    break;
                case GnbProtocol.UI_2_gNB_REBOOT_gNB:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            GnbCmdRsp gnbCmdRsp = MessageHelper.build().gnbReboot(data);
                            if (gnbCmdRsp.getRspValue() == GnbProtocol.OAM_ACK_OK)
                                ZTcpService.build().socketStateChange(id, 100);
                            messageObserver.onSetRebootRsp(id, gnbCmdRsp);
                        }
                    });
                    break;
                case GnbProtocol.UI_2_gNB_SET_TIME:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onSetTimeRsp(id, MessageHelper.build().gnbSetTime(data));
                        }
                    });
                    break;
                case GnbProtocol.UI_2_gNB_WIFI_CFG:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onSetWifiInfoRsp(id, MessageHelper.build().gnbWifiCfg(data));
                        }
                    });
                    break;
                case GnbProtocol.UI_2_gNB_VERSION_UPGRADE:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            GnbCmdRsp gnbCmdRsp = MessageHelper.build().gnbFirmwareUpgrade(data);
                            if (gnbCmdRsp.getRspValue() == GnbProtocol.OAM_ACK_OK)
                                ZTcpService.build().socketStateChange(id, 100);
                            messageObserver.onFirmwareUpgradeRsp(id, gnbCmdRsp);
                        }
                    });
                    break;
                case GnbProtocol.UI_2_gNB_GET_LOG_REQ:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onGetLogRsp(id, MessageHelper.build().gnbGetLog(data));
                        }
                    });
                    break;
                case GnbProtocol.UI_2_gNB_GET_OP_LOG_REQ:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onGetOpLogRsp(id, MessageHelper.build().gnbGetOpLog(data));
                        }
                    });
                    break;
                case GnbProtocol.UI_2_gNB_WRITE_OP_RECORD:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onWriteOpLogRsp(id, MessageHelper.build().gnbWriteOpLog(data));
                        }
                    });
                    break;
                case GnbProtocol.UI_2_gNB_DELETE_OP_LOG_REQ:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDeleteOpLogRsp(id, MessageHelper.build().gnbDeleteOpLog(data));
                        }
                    });
                    break;
                case GnbProtocol.OAM_MSG_SET_BT_NAME:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onSetBtNameRsp(id, MessageHelper.build().gnbSetBtNameRsp(data));
                        }
                    });
                    break;
                case GnbProtocol.OAM_MSG_SET_METH_CFG:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onSetMethIpRsp(id, MessageHelper.build().gnbSetMethIpRsp(data));
                        }
                    });
                    break;
                case GnbProtocol.OAM_MSG_GET_METH_CFG:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onGetMethIpRsp(id, MessageHelper.build().gnbGetMethIpRsp(data));
                        }
                    });
                    break;

                case GnbProtocol.OAM_MSG_SET_FTP_SERVER:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onSetFtpRsp(id,MessageHelper.build().gnbSetFtpRsp(data));
                        }
                    });
                    break;
                case GnbProtocol.OAM_MSG_GET_FTP_SERVER:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onGetFtpRsp(id,MessageHelper.build().gnbGetFtpRsp(data));
                        }
                    });
                    break;
                case GnbProtocol.OAM_MSG_SET_GPIO_MODE:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onSetPaGpioRsp(id, MessageHelper.build().gnbSetGpioRsp(data));
                        }
                    });
                    break;
                case GnbProtocol.OAM_MSG_GET_GPIO_MODE:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onGetPaGpioRsp(id, MessageHelper.build().gnbGetGpioRsp(data));
                        }
                    });
                    break;
                case GnbProtocol.OAM_MSG_SET_SYS_INFO:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onSetSysInfoRsp(id, MessageHelper.build().gnbSetSysInfoRsp(data));
                        }
                    });
                    break;
                case GnbProtocol.OAM_MSG_GET_SYS_INFO:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onGetSysInfoRsp(id, MessageHelper.build().gnbGetSysInfoRsp(data));
                        }
                    });
                    break;
                case GnbProtocol.OAM_MSG_SET_DUAL_CELL:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onSetDualCellRsp(id, MessageHelper.build().gnbSetDualCellRsp(data));
                        }
                    });
                    break;
                case GnbProtocol.OAM_MSG_GET_CATCH_CFG:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onGetCatchCfgRsp(id, MessageHelper.build().gnbGetCatchCfg(data));
                        }
                    });
                    break;
                case GnbProtocol.OAM_MSG_SET_RX_GAIN:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onSetRxGainRsp(id, MessageHelper.build().gnbSetRxGain(data));
                        }
                    });
                    break;
                case GnbProtocol.OAM_MSG_SET_GPS_CFG:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onSetGpsRsp(id, MessageHelper.build().gnbSetGps(data));
                        }
                    });
                    break;
                case GnbProtocol.OAM_MSG_GET_SYS_LOG:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onGetSysLogRsp(id, MessageHelper.build().gnbGetSysLog(data));
                        }
                    });
                    break;
                case GnbProtocol.OAM_MSG_SET_FAN_SPEED:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onSetFanSpeedRsp(id, MessageHelper.build().gnbSetFanSpeed(data));
                        }
                    });
                    break;
                case GnbProtocol.OAM_MSG_FAN_AUTO_CFG:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onSetFanAutoSpeedRsp(id, MessageHelper.build().gnbSetFanAutoSpeed(data));
                        }
                    });
                    break;
                case GnbProtocol.OAM_MSG_SET_JAM_ARFCN:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onSetJamArfcn(id, MessageHelper.build().gnbSetJamArfcn(data));
                        }
                    });
                    break;
                case GnbProtocol.OAM_MSG_FREQ_SCAN_REPORT:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (reportLevel == 0) {
                                messageObserver.onFreqScanRsp(id, MessageHelper.build().gnbFreqScanRsp(data));
                            } else if (reportLevel == 1) {
                                messageObserver.onFreqScanGetDocumentRsp(id, MessageHelper.build().gnbFreqScanGetDocumentRsp(data));
                            }
                        }
                    });
                    break;
                case GnbProtocol.OAM_MSG_STOP_FREQ_SCAN:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onStopFreqScanRsp(id, MessageHelper.build().gnbStopFreqScanRsp(data));
                        }
                    });
                    break;
                case GnbProtocol.OAM_MSG_START_TD_MEASURE:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onStartTdMeasure(id, MessageHelper.build().startTdMeasure(data));
                        }
                    });
                    break;
                case GnbProtocol.OAM_MSG_GET_GPS_CFG:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onGetGpsRsp(id, MessageHelper.build().gnbGetGpsRsp(data));
                        }
                    });
                    break;
                case GnbProtocol.OAM_MSG_FWD_UDP_INFO:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onSetForwardUdpMsg(id, MessageHelper.build().gnbSetForwardUdpMsg(data));
                        }
                    });
                    break;
                case GnbProtocol.OAM_MSG_SET_GPS_IO_CFG:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onSetGpsInOut(id, MessageHelper.build().gnbSetGpsInOut(data));
                        }
                    });
                    break;
                case GnbProtocol.OAM_MSG_GET_GPS_IO_CFG:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onGetGpsInOut(id, MessageHelper.build().gnbGetGpsInOutRsp(data));
                        }
                    });
                    break;
                case GnbProtocol.OAM_MSG_START_BAND_SCAN:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onStartBandScan(id, MessageHelper.build().gnbStartBandScan(data));
                        }
                    });
                    break;
                case GnbProtocol.OAM_MSG_RW_USER_DATA:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (MessageHelper.build().gnbGetUserDataRsp(data).getRW() == 0) {
                                messageObserver.onGetUserData(id, MessageHelper.build().gnbGetUserDataRsp(data));
                            } else {
                                messageObserver.onSetUserData(id, MessageHelper.build().gnbGetUserDataRsp(data));
                            }
                        }
                    });
                    break;
                case GnbProtocol.OAM_MSG_CFG_PA_TRX:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onSetGpioTxRx(id, MessageHelper.build().gnbSetGpioTxRx(data));

                        }
                    });
                    break;
                case GnbProtocol.OAM_MSG_SET_DUAL_STACK:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onSetDualStackRsp(id, MessageHelper.build().gnbSetDualStackRsp(data));
                        }
                    });
                    break;
                case GnbProtocol.OAM_MSG_DATA_FWD:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onReadDataFwdRsp(id, MessageHelper.build().gnbReadDataFwd(data));
                        }
                    });
                    break;
                case GnbProtocol.OAM_MSG_SET_LIC_INFO:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onSetLicRsp(id, MessageHelper.build().gnbSetLicRsp(data));
                        }
                    });
                    break;
                case GnbProtocol.UI_2_gNB_SET_PHONE_TYPE:
                case GnbProtocol.UI_2_eNB_SET_PHONE_TYPE:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onSetPhoneTypeRsp(id, MessageHelper.build().gnbSetPhoneTypeRsp(data));
                        }
                    });
                    break;
                case GnbProtocol.OAM_MSG_SET_FUNC_CFG:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onSetFuncFcgRsp(id, MessageHelper.build().gnbSetFuncCfgRsp(data));
                        }
                    });
                    break;
                case GnbProtocol.UI_2_gNB_OAM_MSG:
                default:
                    break;
            }
        }else {
            SLog.D("dispatch error id " + id + "[" + msgType + ", " + cmdType + "] " + data);
        }
    }

    public void handleMessage(String id, byte[] msg) {
        // 数据转换
        StringBuilder sb = new StringBuilder();
        for (byte value : msg) {
            String b = Integer.toHexString(value);
            int len = b.length();
            if (len == 1) b = "0" + b;
            else if (len > 2) b = b.substring(len - 2);
            sb.append(b).append(",");
        }
        String tmpStr = sb.toString();
        tmpStr = tmpStr.substring(0, tmpStr.length() - 1);
        /* 指令配置返回信息带CELL_ID
         * typedef struct {
         * 		int 0：sync_header;
         * 		int 4：msg_type;          	//UI_2_gNB_OAM_MSG
         * 		int 8：cmd_type;
         * 		int 12：cmd_result;
         *
         *      ......
         *
         * 		int sync_footer;
         * } oam_start_catch_t, oam_stop_catch_t; //in&out
         * */
        String[] uartdata = tmpStr.split("ff,66,00,00,");
        for (String uartdatum : uartdata) {
            String[] data = uartdatum.split(",");
            if (data.length > 12) {
                int idx = 4;
                String s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                int msg_type = DataUtil.str2Int(s, 16);
                idx += 4;
                s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                int cmd_type = DataUtil.str2Int(s, 16);
                dispatch(id, msg_type, cmd_type, uartdatum);
            }
        }
    }

    /*public void send(final String id, final byte[] msg) {
        if (client != null && client.isConnected())
            sendHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        client.send(id, msg);
                    } catch (Exception e) {
                        SLog.E("message transceiver send err, id: " + id + ", msg: " + e.getMessage());
                    }
                }
            }, 100);
    }*/
    public synchronized void send(final String id, final byte[] msg) {
        if (client != null && client.isConnected()){
            Message mMsg = new Message();
            mMsg.obj = new MsgBean(id, msg);
            mSendTread.sendHandler.sendMessage(mMsg);
        }
    }
    SendThread mSendTread = new SendThread();
    class SendThread extends Thread{
        public Handler sendHandler;
        @SuppressLint("HandlerLeak")
        @Override
        public void run() {
            super.run();
            Looper.prepare();
            sendHandler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(@NonNull Message msg) {
                    if (msg.obj!=null){
                        MsgBean bean = (MsgBean) msg.obj;
                        client.send(bean.getId(), bean.getMsg());
                    }
                    return false;
                }
            });
            Looper.loop();
        }
    }
    private ZTcpService client;
    private final Handler handler = new Handler();
    private MessageController.MessageObserver messageObserver;
    private static MessageTransceiver instance;
    public static int reportLevel = 0;
}
