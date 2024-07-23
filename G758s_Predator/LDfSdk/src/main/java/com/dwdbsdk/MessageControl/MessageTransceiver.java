package com.dwdbsdk.MessageControl;

import android.os.Handler;

import com.dwdbsdk.Bean.DB.DBProtocol;
import com.dwdbsdk.Bean.DW.DWProtocol;
import com.dwdbsdk.Logcat.SdkLog;
import com.dwdbsdk.Response.DB.MsgStateRsp;
import com.dwdbsdk.Response.DW.GnbCmdRsp;
import com.dwdbsdk.Socket.ZTcpService;
import com.dwdbsdk.Util.DataUtil;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

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

    }

    public void pollQueue(final String id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SdkLog.I("pollQueue  size = " + msgQueue.size());
                do {
                    if (!msgQueue.isEmpty()) {
                        SdkLog.I("pollQueue  size = " + msgQueue.size());
                        client.send(id, msgQueue.poll());
                    }
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } while (true);
            }
        }).start();
    }

    public void setClient(ZTcpService client) {
        this.client = client;
    }

    public void setMessageObserver(MessageController.MessageObserver observer) {
        this.messageObserver = observer;
    }

    private void dispatchDW(final String id, int msgType, int cmdType, final String data) {
        SdkLog.D("dispatch DW data, id = " + id + ", msgType = " + msgType + ", cmdType = " + cmdType + ", data = " + data);
        if (msgType == DWProtocol.gNB_2_UI_REPORT_UE_INFO || msgType == DWProtocol.gNB_2_UI_REPORT_LTE_UE_INFO) {
            switch (MessageController.build().getTraceType(id)) {
                case DWProtocol.TraceType.CONTROL:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWStartControlRsp(id, MessageHelper.build().gnbStartControl(id, data));
                        }
                    });
                    break;
                case DWProtocol.TraceType.CATCH:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWStartCatchRsp(id, MessageHelper.build().gnbStartCatch(data));
                        }
                    });
                    break;
                case DWProtocol.TraceType.TRACE:
                case DWProtocol.TraceType.LTE_TRACE:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWStartTraceRsp(id, MessageHelper.build().gnbStartTrace(id, data));
                        }
                    });
                    break;
                default:
                    break;
            }
        } else {
            switch (cmdType) {
                case DWProtocol.UI_2_gNB_HEART_BEAT:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            ZTcpService.build().setHeartTime(id); //更新心跳上报时间
                            messageObserver.onDWHeartStateRsp(id, MessageHelper.build().DWHeartState(id, data));
                        }
                    });
                    break;
                case DWProtocol.UI_2_gNB_START_CONTROL:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWStartControlRsp(id, MessageHelper.build().gnbStartControl(id, data));
                        }
                    });
                    break;
                case DWProtocol.UI_2_gNB_STOP_CONTROL:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWStopControlRsp(id, MessageHelper.build().gnbStopControl(id, data));
                        }
                    });
                    break;
                case DWProtocol.UI_2_gNB_START_TRACE:
                case DWProtocol.UI_2_gNB_START_LTE_TRACE:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWStartTraceRsp(id, MessageHelper.build().gnbStartTrace(id, data));
                        }
                    });
                    break;
                case DWProtocol.UI_2_gNB_STOP_TRACE:
                case DWProtocol.UI_2_gNB_STOP_LTE_TRACE:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWStopTraceRsp(id, MessageHelper.build().gnbStopTrace(id, data));
                        }
                    });
                    break;
                case DWProtocol.UI_2_gNB_START_CATCH:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWStartCatchRsp(id, MessageHelper.build().gnbStartCatch(data));
                        }
                    });
                    break;

                case DWProtocol.UI_2_gNB_STOP_CATCH:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWStopCatchRsp(id, MessageHelper.build().gnbStopCatch(data));
                        }
                    });
                    break;

                case DWProtocol.UI_2_gNB_SET_BLACK_UE_LIST:
                case DWProtocol.UI_2_gNB_START_LTE_BLACK_UE_LIST:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWSetBlackListRsp(id, MessageHelper.build().gnbBlackListCfg(data));
                        }
                    });
                    break;

                case DWProtocol.UI_2_gNB_CFG_gNB:
                case DWProtocol.UI_2_gNB_LTE_CFG_gNB:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWSetGnbRsp(id, MessageHelper.build().gnbCfgGnb(data));
                        }
                    });
                    break;

                case DWProtocol.UI_2_gNB_QUERY_gNB_VERSION:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWQueryVersionRsp(id, MessageHelper.build().gnbQueryVersion(data));
                        }
                    });
                    break;

                case DWProtocol.UI_2_gNB_SET_TX_POWER_OFFSET:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWSetTxPwrOffsetRsp(id, MessageHelper.build().gnbSetTxPwrOffset(data));
                        }
                    });
                    break;

                case DWProtocol.OAM_MSG_ADJUST_TX_ATTEN:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWSetNvTxPwrOffsetRsp(id, MessageHelper.build().gnbSetNvTxPwrOffset(data));
                        }
                    });
                    break;

                case DWProtocol.UI_2_gNB_REBOOT_gNB:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            GnbCmdRsp gnbCmdRsp = MessageHelper.build().gnbReboot(data);
                            if (gnbCmdRsp.getRspValue() == DWProtocol.OAM_ACK_OK)
                                ZTcpService.build().socketStateChange(id, 100);
                            messageObserver.onDWRebootRsp(id, gnbCmdRsp);
                        }
                    });
                    break;

                case DWProtocol.UI_2_gNB_SET_TIME:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWSetTimeRsp(id, MessageHelper.build().gnbSetTime(data));
                        }
                    });
                    break;

                case DWProtocol.UI_2_gNB_WIFI_CFG:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWSetWifiInfoRsp(id, MessageHelper.build().gnbWifiCfg(data));
                        }
                    });
                    break;

                case DWProtocol.UI_2_gNB_VERSION_UPGRADE:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            GnbCmdRsp gnbCmdRsp = MessageHelper.build().gnbFirmwareUpgrade(data);
                            if (gnbCmdRsp.getRspValue() == DWProtocol.OAM_ACK_OK)
                                ZTcpService.build().socketStateChange(id, 100);
                            messageObserver.onDWUpgradeRsp(id, gnbCmdRsp);
                        }
                    });
                    break;

                case DWProtocol.UI_2_gNB_GET_LOG_REQ:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWGetLogRsp(id, MessageHelper.build().gnbGetLog(data));
                        }
                    });
                    break;
                case DWProtocol.UI_2_gNB_GET_OP_LOG_REQ:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWGetOpLogRsp(id, MessageHelper.build().gnbGetOpLog(data));
                        }
                    });
                    break;
                case DWProtocol.UI_2_gNB_WRITE_OP_RECORD:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWWriteOpLogRsp(id, MessageHelper.build().gnbWriteOpLog(data));
                        }
                    });
                    break;
                case DWProtocol.UI_2_gNB_DELETE_OP_LOG_REQ:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWDeleteOpLogRsp(id, MessageHelper.build().gnbDeleteOpLog(data));
                        }
                    });
                    break;

                case DWProtocol.OAM_MSG_SET_BT_NAME:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWSetBtNameRsp(id, MessageHelper.build().gnbSetBtNameRsp(data));
                        }
                    });
                    break;

                case DWProtocol.OAM_MSG_SET_METH_CFG:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWSetMethIpRsp(id, MessageHelper.build().gnbSetMethIpRsp(data));
                        }
                    });
                    break;

                case DWProtocol.OAM_MSG_GET_METH_CFG:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWGetMethIpRsp(id, MessageHelper.build().gnbGetMethIpRsp(data));
                        }
                    });
                    break;

//                case DWProtocol.OAM_MSG_SET_FTP_SERVER:
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            messageObserver.onSetFtpRsp(id,MessageHelper.build().gnbSetFtpRsp(data));
//                        }
//                    });
//                    break;
//
//                case DWProtocol.OAM_MSG_GET_FTP_SERVER:
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            messageObserver.onGetFtpRsp(id,MessageHelper.build().gnbGetFtpRsp(data));
//                        }
//                    });
//                    break;
                case DWProtocol.OAM_MSG_SET_GPIO_MODE:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWSetPaGpioRsp(id, MessageHelper.build().gnbSetGpioRsp(data));
                        }
                    });
                    break;
                case DWProtocol.OAM_MSG_GET_GPIO_MODE:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWGetPaGpioRsp(id, MessageHelper.build().gnbGetGpioRsp(data));
                        }
                    });
                    break;
                case DWProtocol.OAM_MSG_SET_SYS_INFO:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWSetDevNameRsp(id, MessageHelper.build().gnbSetSysInfoRsp(data));
                        }
                    });
                    break;
                case DWProtocol.OAM_MSG_GET_SYS_INFO:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWGetSysInfoRsp(id, MessageHelper.build().gnbGetSysInfoRsp(data));
                        }
                    });
                    break;
                case DWProtocol.OAM_MSG_SET_DUAL_CELL:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWSetDualCellRsp(id, MessageHelper.build().gnbSetDualCellRsp(data));
                        }
                    });
                    break;
                case DWProtocol.OAM_MSG_GET_CATCH_CFG:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWGetCatchCfgRsp(id, MessageHelper.build().gnbGetCatchCfg(data));
                        }
                    });
                    break;
                case DWProtocol.OAM_MSG_SET_RX_GAIN:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWSetRxGainRsp(id, MessageHelper.build().gnbSetRxGain(data));
                        }
                    });
                    break;
                case DWProtocol.OAM_MSG_SET_GPS_CFG:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWSetGpsRsp(id, MessageHelper.build().gnbSetGps(data));
                        }
                    });
                    break;
                case DWProtocol.OAM_MSG_GET_SYS_LOG:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWGetSysLogRsp(id, MessageHelper.build().gnbGetSysLog(data));
                        }
                    });
                    break;
                case DWProtocol.OAM_MSG_SET_FAN_SPEED:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWSetFanSpeedRsp(id, MessageHelper.build().gnbSetFanSpeed(data));
                        }
                    });
                    break;
                case DWProtocol.OAM_MSG_FAN_AUTO_CFG:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWSetFanAutoSpeedRsp(id, MessageHelper.build().gnbSetFanAutoSpeed(data));
                        }
                    });
                    break;
                case DWProtocol.OAM_MSG_SET_JAM_ARFCN:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWSetJamArfcn(id, MessageHelper.build().gnbSetJamArfcn(data));
                        }
                    });
                    break;
                case DWProtocol.OAM_MSG_FREQ_SCAN_REPORT:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (MessageController.build().getMsgType(id) == DWProtocol.OAM_MSG_START_FREQ_SCAN)
                                messageObserver.onDWFreqScanRsp(id, MessageHelper.build().gnbFreqScanRsp(data));
                            else if (MessageController.build().getMsgType(id) == DWProtocol.OAM_MSG_START_FREQ_SCAN + 1)
                                messageObserver.onDWFreqScanGetDocumentRsp(id, MessageHelper.build().gnbFreqScanGetDocumentRsp(data));
                        }
                    });
                    break;
                case DWProtocol.OAM_MSG_STOP_FREQ_SCAN:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWStopFreqScanRsp(id, MessageHelper.build().gnbStopFreqScanRsp(data));
                        }
                    });
                    break;
                case DWProtocol.OAM_MSG_START_BAND_SCAN:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWStartBandScan(id, MessageHelper.build().gnbStartBandScan(data));
                        }
                    });
                    break;
                case DWProtocol.OAM_MSG_START_TD_MEASURE:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWStartTdMeasure(id, MessageHelper.build().startTdMeasure(data));
                        }
                    });
                    break;
                case DWProtocol.OAM_MSG_FWD_UDP_INFO:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWSetForwardUdpMsg(id, MessageHelper.build().gnbSetForwardUdpMsg(data));
                        }
                    });
                    break;
                case DWProtocol.OAM_MSG_SET_GPS_IO_CFG:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWSetGpsInOut(id, MessageHelper.build().gnbSetGpsInOut(data));
                        }
                    });
                    break;
                case DWProtocol.OAM_MSG_GET_GPS_IO_CFG:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWGetGpsInOut(id, MessageHelper.build().gnbGetGpsInOutRsp(data));
                        }
                    });
                    break;
                case DWProtocol.OAM_MSG_GET_GPS_CFG:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWGetGpsRsp(id, MessageHelper.build().gnbGetGpsRsp(data));
                        }
                    });
                    break;
                case DWProtocol.OAM_MSG_RW_USER_DATA:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (MessageHelper.build().gnbGetUserDataRsp(data).getRW() == 0) {
                                messageObserver.onDWSetUserData(id,MessageHelper.build().gnbGetUserDataRsp(data));
                            } else {
                                messageObserver.onDWGetUserData(id,MessageHelper.build().gnbGetUserDataRsp(data));
                            }

                        }
                    });
                    break;
                case DWProtocol.OAM_MSG_CFG_PA_TRX:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageObserver.onDWSetGpioTxRx(id,MessageHelper.build().gnbSetGpioTxRx(data));

                        }
                    });
                    break;
                default:
                    break;
            }

        }
    }

    private void dispatchDB(final String id, final String msg) {
        int msg_type = -1;
        String[] uartdata = msg.split("33,33,a5,5a,");
        for (String uartdatum : uartdata) {
            String[] data = uartdatum.split(",");
            if (data.length > 12) {
                int idx = 12;
                String s = data[idx + 3] + data[idx + 2] + data[idx + 1] + data[idx];
                msg_type = DataUtil.str2Int(s, 16);
            }
        }
        SdkLog.D("dispatch DB data, id = " + id + ", msg_type: " + msg_type + ", msg = " + msg);

        switch (msg_type) {
            case DBProtocol.MsgType.GR_MSG_HELLO:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ZTcpService.build().setHeartTime(id);  //更新心跳上报时间
                        messageObserver.onDBHeartStateRsp(id, MessageHelper.build().DBHeartState(msg));
                    }
                });
                break;
            case DBProtocol.MsgType.GR_MSG_SET_TIME:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        messageObserver.onDBSetTimeRsp(id, MessageHelper.build().setTime(msg));
                    }
                });
                break;
            case DBProtocol.MsgType.GR_MSG_BT_NAME:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        messageObserver.onDBSetBtNameRsp(id, MessageHelper.build().setBtName(msg));
                    }
                });
                break;
            case DBProtocol.MsgType.BT_MSG_DEV_NAME:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        messageObserver.onDBSetDevNameRsp(id, MessageHelper.build().setDevName(msg));
                    }
                });
                break;
            case DBProtocol.MsgType.GR_MSG_WIFI_CFG:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        messageObserver.onDBWifiCfgRsp(id, MessageHelper.build().configWifi(msg));
                    }
                });
                break;
            case DBProtocol.MsgType.GR_MSG_GET_LOG:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        messageObserver.onDBGetLogRsp(id, MessageHelper.build().getLog(msg));
                    }
                });
                break;
            case DBProtocol.MsgType.GR_MSG_IMG_UPGRADE:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MsgStateRsp msgStateRsp = MessageHelper.build().setUpgrade(msg);
                        if (msgStateRsp.getRspValue() == DBProtocol.GR_2_UI_CFG_OK)
                            ZTcpService.build().socketStateChange(id, 100);
                        messageObserver.onDBUpgradeRsp(id, msgStateRsp);
                    }
                });
                break;
            case DBProtocol.MsgType.GR_MSG_REBOOT:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MsgStateRsp msgStateRsp = MessageHelper.build().setReboot(msg);
                        if (msgStateRsp.getRspValue() == DBProtocol.GR_2_UI_CFG_OK)
                            ZTcpService.build().socketStateChange(id, 100);
                        messageObserver.onDBRebootRsp(id, msgStateRsp);
                    }
                });
                break;
            case DBProtocol.MsgType.GR_MSG_START_SG:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        messageObserver.onDBStartSGRsp(id, MessageHelper.build().startSG(msg));
                    }
                });
                break;
            case DBProtocol.MsgType.GR_MSG_STOP_SG:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        messageObserver.onDBStopSGRsp(id, MessageHelper.build().stopSG(msg));
                    }
                });
                break;
            case DBProtocol.MsgType.GR_MSG_START_JAM:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        messageObserver.onDBStartJamRsp(id, MessageHelper.build().startJam(msg));
                    }
                });
                break;
            case DBProtocol.MsgType.GR_MSG_STOP_JAM:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        messageObserver.onDBStopJamRsp(id, MessageHelper.build().stopJam(msg));
                    }
                });
                break;
            case DBProtocol.MsgType.GR_MSG_GET_JAM:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        messageObserver.onDBGetJamRsp(id, MessageHelper.build().getJam(msg));
                    }
                });
                break;
            case DBProtocol.MsgType.GR_MSG_START_POS_SCAN:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        messageObserver.onDBStartScanRsp(id, MessageHelper.build().startScan(msg));
                    }
                });
                break;
            case DBProtocol.MsgType.GR_MSG_START_PWR_SCAN:
            case DBProtocol.MsgType.GR_MSG_POWER_REPORT:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        messageObserver.onDBStartPwrDetectRsp(id, MessageHelper.build().startPwrDetect(msg));
                    }
                });
                break;
            case DBProtocol.MsgType.GR_MSG_STOP_PWR_SCAN:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        messageObserver.onDBStopPwrDetectRsp(id, MessageHelper.build().stopPwrDetect(msg));
                    }
                });
                break;
            case DBProtocol.MsgType.GR_MSG_GET_VERSION:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        messageObserver.onDBGetVersionRsp(id, MessageHelper.build().getVersion(msg));
                    }
                });
                break;
            case DBProtocol.MsgType.GR_MSG_RX_GAIN:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        messageObserver.onDBRxGainCfgRsp(id, MessageHelper.build().setRxGain(msg));
                    }
                });
                break;
            case DBProtocol.MsgType.GR_MSG_GPIO_CFG:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        messageObserver.onDBSetGpioCfgRsp(id, MessageHelper.build().setGpioCfg(msg));
                    }
                });
                break;
            case DBProtocol.MsgType.GR_MSG_DATA_FWD:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        messageObserver.onReadDataFwdRsp(id, MessageHelper.build().readDataFwd(msg));
                    }
                });
                break;
            case DBProtocol.MsgType.BT_MSG_DEV_SN:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        messageObserver.onSetDeviceId(id, MessageHelper.build().onSetDeviceId(msg));
                    }
                });
                break;
            case DBProtocol.MsgType.GR_MSG_START_SENSE_DET:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        messageObserver.onStartSenseRsp(id,MessageHelper.build().startSense(msg));
                    }
                });
                break;
            case DBProtocol.MsgType.GR_MSG_STOP_SENSE_DET:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        messageObserver.onStopSenseRsp(id,MessageHelper.build().stopSense(msg));
                    }
                });
                break;
            case DBProtocol.MsgType.GR_MSG_SENSE_REPORT:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        messageObserver.onSenseReportRsp(id,MessageHelper.build().senseReport(msg));
                    }
                });
                break;
        }
    }

    public void handleMessage(String type, String id, String tmpStr) {
        if (type.equals("DB")) {
            dispatchDB(id, tmpStr);
        } else if (type.equals("DW")) {
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
                    dispatchDW(id, msg_type, cmd_type, uartdatum);
                }
            }
        }
    }

    public void handleMessage(String type, String id, byte[] msg) {
        // 数据转换
        StringBuilder sb = new StringBuilder();
        for (byte value : msg) {
            String b = Integer.toHexString(value);
            if (b.length() == 1) {
                b = "0" + b;
            } else if (b.length() > 2) {
                b = b.substring(b.length() - 2);
            }
            sb.append(b);
            sb.append(",");
        }
        String tmpStr = sb.toString();
        tmpStr = tmpStr.substring(0, tmpStr.length() - 1);
        handleMessage(type, id, tmpStr);
    }

    public void offerMsgQueue(String id, byte[] msg) {
        SdkLog.I("offerMsgQueue msg =" + Arrays.toString(msg));
        msgQueue.offer(msg);
    }

    public boolean send(final String id, final byte[] msg) {
        if (client != null && client.isConnected())

            sendHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        client.send(id, msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 50);

        return true;
    }

    private final ConcurrentLinkedQueue<byte[]> msgQueue = new ConcurrentLinkedQueue<>();
    private ZTcpService client;
    private final Handler handler = new Handler();
    private final Handler sendHandler = new Handler();
    private MessageController.MessageObserver messageObserver;
    private static MessageTransceiver instance;
}
