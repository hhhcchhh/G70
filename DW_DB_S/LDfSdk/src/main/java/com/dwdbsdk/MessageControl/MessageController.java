package com.dwdbsdk.MessageControl;

import com.dwdbsdk.Bean.DB.DBProtocol;
import com.dwdbsdk.Bean.DB.OrxBean;
import com.dwdbsdk.Bean.DB.TxBean;
import com.dwdbsdk.Bean.DW.DWHeader;
import com.dwdbsdk.Bean.DW.DWProtocol;
import com.dwdbsdk.Bean.DW.MsgTypeBean;
import com.dwdbsdk.Bean.LteBand;
import com.dwdbsdk.Bean.NrBand;
import com.dwdbsdk.Bean.DW.TracePara;
import com.dwdbsdk.Bean.GnbTimingOffset;
import com.dwdbsdk.Bean.PaPkUlArfcnBean;
import com.dwdbsdk.Bean.UeidBean;
import com.dwdbsdk.Config.DB.MsgGetLog;
import com.dwdbsdk.Config.DB.MsgGetVersion;
import com.dwdbsdk.Config.DB.MsgGlobal;
import com.dwdbsdk.Config.DB.MsgGpioCfg;
import com.dwdbsdk.Config.DB.MsgRxGain;
import com.dwdbsdk.Config.DB.MsgSetBtName;
import com.dwdbsdk.Config.DB.MsgSetDataFwd;
import com.dwdbsdk.Config.DB.MsgSetDeviceId;
import com.dwdbsdk.Config.DB.MsgSetDeviceName;
import com.dwdbsdk.Config.DB.MsgSetTime;
import com.dwdbsdk.Config.DB.MsgStartGSMDetect;
import com.dwdbsdk.Config.DB.MsgStartJam;
import com.dwdbsdk.Config.DB.MsgStartPwrDetect;
import com.dwdbsdk.Config.DB.MsgStartSG;
import com.dwdbsdk.Config.DB.MsgStartScan;
import com.dwdbsdk.Config.DB.MsgStartSense;
import com.dwdbsdk.Config.DB.MsgUpgrade;
import com.dwdbsdk.Config.DB.MsgWifiCfg;
import com.dwdbsdk.Config.DW.GnbBlackList;
import com.dwdbsdk.Config.DW.GnbCfgGnb;
import com.dwdbsdk.Config.DW.GnbCfgLic;
import com.dwdbsdk.Config.DW.GnbDualCell;
import com.dwdbsdk.Config.DW.GnbGetCellCfg;
import com.dwdbsdk.Config.DW.GnbGetLog;
import com.dwdbsdk.Config.DW.GnbGpioCfg;
import com.dwdbsdk.Config.DW.GnbMethIpCfg;
import com.dwdbsdk.Config.DW.GnbOnlyCellID;
import com.dwdbsdk.Config.DW.GnbOnlyType;
import com.dwdbsdk.Config.DW.GnbRedirectUeCfg;
import com.dwdbsdk.Config.DW.GnbResetPlmnCfg;
import com.dwdbsdk.Config.DW.GnbSetBtName;
import com.dwdbsdk.Config.DW.GnbSetDataFwd;
import com.dwdbsdk.Config.DW.GnbSetDataTo485;
import com.dwdbsdk.Config.DW.GnbSetFanAutoSpeed;
import com.dwdbsdk.Config.DW.GnbSetFanSpeed;
import com.dwdbsdk.Config.DW.GnbSetForwardUdpMsg;
import com.dwdbsdk.Config.DW.GnbSetFtpServer;
import com.dwdbsdk.Config.DW.GnbSetGpioTxRx;
import com.dwdbsdk.Config.DW.GnbSetGps;
import com.dwdbsdk.Config.DW.GnbSetGpsInOut;
import com.dwdbsdk.Config.DW.GnbSetJam;
import com.dwdbsdk.Config.DW.GnbSetPefPwrCfg;
import com.dwdbsdk.Config.DW.GnbSetRxGain;
import com.dwdbsdk.Config.DW.GnbSetSysInfo;
import com.dwdbsdk.Config.DW.GnbSetTime;
import com.dwdbsdk.Config.DW.GnbSetTxPwr;
import com.dwdbsdk.Config.DW.GnbSetUserData;
import com.dwdbsdk.Config.DW.GnbStartBandScan;
import com.dwdbsdk.Config.DW.GnbStartCatch;
import com.dwdbsdk.Config.DW.GnbStartControl;
import com.dwdbsdk.Config.DW.GnbStartFreqScan;
import com.dwdbsdk.Config.DW.GnbStartTdMeasure;
import com.dwdbsdk.Config.DW.GnbStartTrace;
import com.dwdbsdk.Config.DW.GnbUpgrade;
import com.dwdbsdk.Config.DW.GnbWifiCfg;
import com.dwdbsdk.Config.DW.GnbWriteOpLog;
import com.dwdbsdk.Config.DW.TFGnbGpioCfg;
import com.dwdbsdk.Interface.DBBusinessListener;
import com.dwdbsdk.Interface.DWBusinessListener;
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
import com.dwdbsdk.Util.DateUtil;
import com.dwdbsdk.Util.MD5;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MessageController {
    private static MessageController instance;
    private boolean isRun;
    public boolean isDoStop = false; // 发现停止业务时，isEnableChangeTac置为false后，心跳来了又将其置为true, 因此加上是否进入停止业务状态标志

    public static MessageController build() {
        synchronized (MessageController.class) {
            if (instance == null) {
                instance = new MessageController();
            }
        }
        return instance;
    }

    public MessageController() {
        startTacThread();
    }

    /**
     * TAC 业务循环线程，应用一启动就开启此线程，应用退出时才停止
     * 由 isEnableChangeTac 和 isTracing 共同决定是否下发 TAC 或休眠 1s 再继续判断
     * 接口调用开启业务时， 先将 isEnableChangeTac置为 true，待启动成功后， 将 isTracing置为 true
     * 接口调用结束业务时， 先将 isEnableChangeTac置为 false，待启动成功后， 将 isTracing置为 false
     */
    private void startTacThread() {
        isRun = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRun) {
                    try {
                        for (int i = 0; i < traceList.size(); i++) {
                            if (traceList.get(i).isEnableChangeTac() && traceList.get(i).isTracing()) {
                                // 仅4G做处理  pciInit -- 用于记录PCI初始值, pciChangeCount -- 用于PCI变化倒计时数, pciChangeTime -- 用于PCI循环倒计时数;
                                int special = traceList.get(i).getSpecial();
                                if (traceList.get(i).isLte() && special > 0){
                                    int specialCount = traceList.get(i).getSpecialCount();
                                    specialCount--;
                                    if (specialCount < 1){
                                        /*int i_pci = Integer.parseInt(traceList.get(i).getPci()) + 3;
                                        if (i_pci > 503) i_pci = i_pci % 3;
                                        if (i_pci == 0) i_pci = 3;*/
                                        if (traceList.get(i).getPci().equals(traceList.get(i).getSpecialInit())){
                                            int i_pci = Integer.parseInt(traceList.get(i).getPci()) + 3;
                                            if (i_pci > 503) i_pci = i_pci - 6;
                                            traceList.get(i).setPci(String.valueOf(i_pci));
                                        }else traceList.get(i).setPci(traceList.get(i).getSpecialInit());
                                        traceList.get(i).setSpecial(special);
                                        traceList.get(i).setSpecialCount(special);
                                    }else traceList.get(i).setSpecialCount(specialCount);
                                }
                                int tacDelay = traceList.get(i).getTraceTacChangeDelay();
                                tacDelay++;
                                // 如果是侦码，则使用13、113号消息中的tacInterval，因此循环tac间隔时间是可变参数
                                // 如果是定位或管控，则默认为 9, 即10s循环一次
                                if (tacDelay > traceList.get(i).getTacInterval()) {
                                    tacDelay = 0;
                                    int tac = traceList.get(i).getStartTac();
                                    if (tac < traceList.get(i).getMaxTac()) {
                                        tac++;
                                    } else {
                                        // 如果是侦码，则使用13、113号消息中的开始TAC与最大TAC，因此循环间隔是可变参数
                                        // 如果是定位或管控，则默认为 6
                                        tac -= traceList.get(i).getRunTac();
                                    }
                                    traceList.get(i).setStartTac(tac);

                                    gnbCfg(traceList.get(i).getId(), traceList.get(i).isLte(), traceList.get(i).getCellId(), traceList.get(i).getStartTac(),
                                            traceList.get(i).getPlmn(), traceList.get(i).getArfcn(), traceList.get(i).getPci(), traceList.get(i).getUeMaxTxpwr(),
                                            traceList.get(i).getTimingOffset(), traceList.get(i).getWorkMode(), traceList.get(i).getAirSync(), traceList.get(i).getPlmn1(),
                                            traceList.get(i).getUlRbOffset(), traceList.get(i).getCid(), traceList.get(i).getSsbBitmap(), traceList.get(i).getBandWidth(),
                                            traceList.get(i).getCfr(), traceList.get(i).getSwapRf(), traceList.get(i).getRejectCode(), traceList.get(i).getRxLevMin(),
                                            traceList.get(i).getMobRejectCode(), traceList.get(i).getSplitArfcndl(), traceList.get(i).getForceCfg(), traceList.get(i).getSplitPcidl());
                                }
                                traceList.get(i).setTraceTacChangeDelay(tacDelay);
                            }
                        }
                    } catch (Exception e) {
                        SdkLog.E("MessageController traceThread err: " + e.getMessage());
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
    }

    public void setTransceiver(MessageTransceiver transceiver) {
        transceiver.setMessageObserver(new MessageObserver());
    }

    public void close() {
        isRun = false;
    }

    /**
     * 用于上层查询10、110业务配置信息时，恢复TAC循环列表（APP退出重进时）
     * */
    public void recoverTraceList(TracePara para){
        boolean isAdd = true;
        for (TracePara tracePara : traceList) {
            if (tracePara.getId().equals(para.getId()) && tracePara.getCellId() == para.getCellId()) {
                isAdd = false;
                break;
            }
        }
        if (isAdd) traceList.add(para);
    }
    /**
     * 用于上层恢复业务时，启动TAC循环
     * */
    public void recoverTacLoop(String id, int cell_id){
        for (int i = 0; i < traceList.size(); i++) {
            if (traceList.get(i).getCellId() == cell_id && traceList.get(i).getId().equals(id)) {
                traceList.get(i).setEnableChangeTac(true);
                traceList.get(i).setTracing(true);
                break;
            }
        }
    }
    public int getTraceType(String id) {
        for (int i = 0; i < msgTypeList.size(); i++) {
            if (msgTypeList.get(i).getId().equals(id)) {
                return msgTypeList.get(i).getTraceType();
            }
        }
        return -1;
    }

    public void setTraceType(String id, int traceType) {
        for (int i = 0; i < msgTypeList.size(); i++) {
            if (msgTypeList.get(i).getId().equals(id)) {
                SdkLog.I("setTraceType id = " + id + ", traceType = " + traceType);
                msgTypeList.get(i).setTraceType(traceType);
            }
        }
    }

    public synchronized void addMsgTypeList(String id, String ip) {
        boolean add = true;
        for (int i = 0; i < msgTypeList.size(); i++) {
            if (msgTypeList.get(i).getId().equals(id)) {
                add = false;
                break;
            }
        }
        if (add) {
            SdkLog.I("addMsgTypeList id = " + id + ", ip = " + ip);
            msgTypeList.add(new MsgTypeBean(id, ip));
        }
    }

    public synchronized void removeMsgTypeList(String id) {
        for (int i = 0; i < msgTypeList.size(); i++) {
            if (msgTypeList.get(i).getId().equals(id) || msgTypeList.get(i).getIp().equals(id)) {
                SdkLog.I("removeMsgTypeList id = " + id);
                msgTypeList.remove(i);
                break;
            }
        }
        // 断开连接后，将list中同一个id下的所有小区暂停循环tac，待心跳恢复时，再恢复循环
        for (int i = 0; i < traceList.size(); i++) {
            if (traceList.get(i).getId().equals(id)) {
                traceList.get(i).setEnableChangeTac(false);
            }
        }
    }

    public String getIpFromMsgTypeList(String id) {
        for (MsgTypeBean bean : msgTypeList) {
            if (bean.getId().equals(id)) return bean.getIp();
        }
        return "";
    }

    public void setTracing(String id, int cell_id, boolean tracing) {
        for (int i = 0; i < traceList.size(); i++) {
            if (traceList.get(i).getCellId() == cell_id && traceList.get(i).getId().equals(id)) {
                traceList.get(i).setTracing(tracing);
                if (!tracing) removeTraceListById(id, cell_id); // 结束时，移除对应设备对应通道的信息
                break;
            }
        }
    }

    public boolean isTracing(String id, int cell_id) {
        for (int i = 0; i < traceList.size(); i++) {
            if (traceList.get(i).getCellId() == cell_id && traceList.get(i).getId().equals(id)) {
                return traceList.get(i).isTracing();
            }
        }
        return false;
    }

    public void setEnableChangeTac(String id, int cell_id, boolean enable) {
        for (int i = 0; i < traceList.size(); i++) {
            if (traceList.get(i).getId().equals(id) && traceList.get(i).getCellId() == cell_id) {
                traceList.get(i).setEnableChangeTac(enable);
                break;
            }
        }
    }

    public boolean isEnableChangeTac(String id, int cell_id) {
        for (int i = 0; i < traceList.size(); i++) {
            if (traceList.get(i).getId().equals(id) && traceList.get(i).getCellId() == cell_id) {
                return traceList.get(i).isEnableChangeTac();
            }
        }
        return false;
    }

    public void setImsi(String id, int cell_id, String imsi) {
        for (int i = 0; i < traceList.size(); i++) {
            if (traceList.get(i).getId().equals(id) && traceList.get(i).getCellId() == cell_id) {
                traceList.get(i).setImsi(imsi);
                break;
            }
        }
    }

    public String getImsi(String id, int cell_id) {
        for (int i = 0; i < traceList.size(); i++) {
            if (traceList.get(i).getId().equals(id) && traceList.get(i).getCellId() == cell_id) {
                return traceList.get(i).getImsi();
            }
        }
        return null;
    }

    public void setArfcn(String id, int cell_id, String arfcn) {
        for (int i = 0; i < traceList.size(); i++) {
            if (traceList.get(i).getId().equals(id) && traceList.get(i).getCellId() == cell_id) {
                traceList.get(i).setArfcn(arfcn);
                break;
            }
        }
    }

    public String getArfcn(String id, int cell_id) {
        for (int i = 0; i < traceList.size(); i++) {
            if (traceList.get(i).getId().equals(id) && traceList.get(i).getCellId() == cell_id) {
                return traceList.get(i).getArfcn();
            }
        }
        return "";
    }

    public void setPci(String id, int cell_id, String pci) {
        for (int i = 0; i < traceList.size(); i++) {
            if (traceList.get(i).getId().equals(id) && traceList.get(i).getCellId() == cell_id) {
                traceList.get(i).setPci(pci);
                break;
            }
        }
    }

    public String getPci(String id, int cell_id) {
        for (int i = 0; i < traceList.size(); i++) {
            if (traceList.get(i).getId().equals(id) && traceList.get(i).getCellId() == cell_id) {
                return traceList.get(i).getPci();
            }
        }
        return "";
    }

    public void setPk(String id, int cell_id, int pk) {
        for (int i = 0; i < traceList.size(); i++) {
            if (traceList.get(i).getId().equals(id) && traceList.get(i).getCellId() == cell_id) {
                traceList.get(i).setPk(pk);
                break;
            }
        }
    }

    public int getPk(String id, int cell_id) {
        for (int i = 0; i < traceList.size(); i++) {
            if (traceList.get(i).getId().equals(id) && traceList.get(i).getCellId() == cell_id) {
                return traceList.get(i).getPk();
            }
        }
        return 1;
    }

    public void setPa(String id, int cell_id, int pa) {
        for (int i = 0; i < traceList.size(); i++) {
            if (traceList.get(i).getId().equals(id) && traceList.get(i).getCellId() == cell_id) {
                traceList.get(i).setPa(pa);
                break;
            }
        }
    }

    public int getPa(String id, int cell_id) {
        List<TracePara> list = new ArrayList<>();
        for (int i = 0; i < traceList.size(); i++) {
            if (traceList.get(i).getId().equals(id) && traceList.get(i).getCellId() == cell_id) {
                return traceList.get(i).getPa();
            }
        }
        return 1;
    }

    public void setStopCount(String id, int cell_id, int count) {
        for (int i = 0; i < traceList.size(); i++) {
            if (traceList.get(i).getId().equals(id) && traceList.get(i).getCellId() == cell_id) {
                traceList.get(i).setStopCount(count);
                break;
            }
        }
    }

    public int getStopCount(String id, int cell_id) {
        for (int i = 0; i < traceList.size(); i++) {
            if (traceList.get(i).getId().equals(id) && traceList.get(i).getCellId() == cell_id) {
                return traceList.get(i).getStopCount();
            }
        }
        return 0;
    }

    private void removeTraceListById(String id, int cell_id) {
        for (int i = traceList.size() - 1; i >= 0; i--) {
            if (traceList.get(i).getId().equals(id) && traceList.get(i).getCellId() == cell_id) {
                traceList.remove(i);
                break;
            }
        }
    }

    /**
     * 配置UE最大发射功率
     *
     * @param id         设备ID
     * @param cell_id    通道ID
     * @param ueMaxTxPwr 最大发射功率值
     */
    public void setUeMaxTxPwr(String id, int cell_id, String ueMaxTxPwr) {
        for (int i = 0; i < traceList.size(); i++) {
            if (traceList.get(i).getId().equals(id) && traceList.get(i).getCellId() == cell_id) {
                SdkLog.I("setUeMaxTxPwr id = " + id + ", cell_id = " + cell_id + ", ueMaxTxPwr" + ueMaxTxPwr);
                traceList.get(i).setUeMaxTxpwr(ueMaxTxPwr);
            }
        }
    }

    public int getUeMaxTxPwr(String id, int cell_id) {
        for (int i = 0; i < traceList.size(); i++) {
            if (traceList.get(i).getId().equals(id) && traceList.get(i).getCellId() == cell_id) {
                return Integer.parseInt(traceList.get(i).getUeMaxTxpwr());
            }
        }
        return -1;
    }

    public int getMsgType(String id) {
        for (int i = 0; i < msgTypeList.size(); i++) {
            if (msgTypeList.get(i).getId().equals(id)) {
                return msgTypeList.get(i).getMsgType();
            }
        }
        return DWProtocol.UI_NONE;
    }

    private void setMsgType(String id, int type) {
        for (int i = 0; i < msgTypeList.size(); i++) {
            if (msgTypeList.get(i).getId().equals(id)) {
                msgTypeList.get(i).setMsgType(type);
            }
        }
    }

    public PaPkUlArfcnBean getPaPkUlArfcn(int dl_arfcn, int board_bandwidth){
        int UL_NR_ARFCN = dl_arfcn;
        boolean is_lte = dl_arfcn < 100000;

        int band = is_lte ? LteBand.earfcn2band(dl_arfcn) : NrBand.earfcn2band(dl_arfcn);
        if (is_lte) {
            if (band < 33 || band > 53) UL_NR_ARFCN = dl_arfcn + LteBand.getUlAddNumByBand(band);
        } else {
            //fdd UL_ARFCN != DL_NR_ARFCN
            // DL_NR_ARFCN >= 600000 表示频段 > 3G
            if (dl_arfcn < 600000) {
                switch (band) {
                    case 1:
                        // 434000 - 396000 = 38000
                        // 422000 - 384000 = 38000
                        if (dl_arfcn == 422890 || dl_arfcn == 422930) { //422930
                            UL_NR_ARFCN = dl_arfcn - 38798;
                        } else if (dl_arfcn == 427010) {
                            UL_NR_ARFCN = 388092;
                        } else if (dl_arfcn == 428910) { // 重庆
                            UL_NR_ARFCN = 388072;
                        } else {
                            //UL_NR_ARFCN = DL_NR_ARFCN - 38000;
                            //2024/1/17 调整默认则使用391010
                            UL_NR_ARFCN = 391010;
                        }
                        break;
                    case 3:
                        UL_NR_ARFCN = 349500;
                        break;
                    case 5:
                        UL_NR_ARFCN = 165000;
                        break;
                    case 7:
                        UL_NR_ARFCN = 507000;
                        break;
                    case 8:
                        UL_NR_ARFCN = 178000;
                        break;
                    case 28:  // N28A: 703 -- 733(748)
                        // 151600 - 140600 = 11000
                        // 160600 - 149600 = 11000
                        UL_NR_ARFCN = 140720; //154810  152890 154810 154570 152650
                        break;
                }
            }
        }

        int slot_index = 19;
        int frame_type = 9;
        if (is_lte) {
            band = LteBand.earfcn2band(dl_arfcn);
            if (band < 33 || band > 53){
                slot_index = 7;
                frame_type = 2;
            }else {
                slot_index = 7;
                frame_type = 9;
            }
        } else {
            switch (band) {
                case 1:
                case 3:
                case 5:
                case 7:
                case 8:
                case 28:
                    slot_index = 9;
                    frame_type = 9;
                    break;
                case 41:
                    frame_type = 8;
                    break;
                case 78:
                case 79:
                    frame_type = 7;
                    break;
            }
        }

        int offset2pointA = 0;
        int kssb = 0;
        int DL_NR_ARFCN = dl_arfcn;
        if (!is_lte) {
            if (board_bandwidth != 20) board_bandwidth = 100; // 目前仅支持5、10M
            switch (band) {
                case 1:
                    if (DL_NR_ARFCN == 427250) {
                        kssb = 6; //427250
                        offset2pointA = 23;
                    } else if (DL_NR_ARFCN == 422890 || DL_NR_ARFCN == 422930) { //422930
                        kssb = 2;
                        offset2pointA = 12;
                    } else if (DL_NR_ARFCN == 427010) {
                        kssb = 6;
                        offset2pointA = 15;
                    } else if (DL_NR_ARFCN == 428910) {
                        kssb = 10;
                        offset2pointA = 68;
                    } else {
                        if (DL_NR_ARFCN > 422890 && DL_NR_ARFCN < 428170) {
                            kssb = 0; //427250
                            offset2pointA = 12;
                        } else if (DL_NR_ARFCN >= 428170 && DL_NR_ARFCN <= 433010) {
                            kssb = 0; //427250
                            offset2pointA = 70;
                        }
                    }
                    break;
                case 3:
                    if (DL_NR_ARFCN >= 362170 && DL_NR_ARFCN < 368450) {
                        kssb = 0;
                        offset2pointA = 12;
                    } else if (DL_NR_ARFCN >= 368450 && DL_NR_ARFCN < 374930) {
                        kssb = 0;
                        offset2pointA = 70;
                    }
                    break;
                case 5:
                    kssb = 0;
                    offset2pointA = 24;
                    break;
                case 8:
                    if (DL_NR_ARFCN >= 186010 && DL_NR_ARFCN < 188410) {
                        kssb = 0;
                        offset2pointA = 12;
                    } else if (DL_NR_ARFCN >= 188410 && DL_NR_ARFCN < 190850) {
                        kssb = 0;
                        offset2pointA = 70;
                    }
                    break;
                case 7:
                    if (DL_NR_ARFCN >= 525150 && DL_NR_ARFCN < 530190) {
                        kssb = 0;
                        offset2pointA = 12;
                    } else if (DL_NR_ARFCN >= 530190 && DL_NR_ARFCN < 536910) {
                        kssb = 0;
                        offset2pointA = 70;
                    }
                    break;
                case 28:
                    if (DL_NR_ARFCN == 154810) {
                        kssb = 6; //
                        offset2pointA = 23;
                    } else if (DL_NR_ARFCN == 152890) {
                        kssb = 6;
                        offset2pointA = 22;
                    }/* else if (DL_NR_ARFCN == 156970) {
                        kssb = 10; //
                        offset2pointA = 135;
                    } */else if (DL_NR_ARFCN == 152650) {
                        kssb = 6; //
                        offset2pointA = 12;
                    } else if (DL_NR_ARFCN == 154570) {
                        kssb = 2;
                        offset2pointA = 69;
                    }/* else if (DL_NR_ARFCN == 156490) {
                        kssb = 6;
                        //offset2pointA = 122;
                        offset2pointA = 70;
                    } */else {
                        if (DL_NR_ARFCN >= 152410 && DL_NR_ARFCN < 154090) {
                            kssb = 0; //427250
                            offset2pointA = 12;
                        } else if (DL_NR_ARFCN >= 154090 && DL_NR_ARFCN < 155290) {
                            kssb = 0; //427250
                            offset2pointA = 56;
                        } else if (DL_NR_ARFCN >= 155290 && DL_NR_ARFCN <= 156530) {
                            kssb = 0; //427250
                            offset2pointA = 70;
                        }
                    }
                    break;

                case 41:
                    if (DL_NR_ARFCN == 504990) {
                        kssb = 6;
                        offset2pointA = 30;
                    } else if (DL_NR_ARFCN == 533070) {
                        kssb = 6;
                        offset2pointA = 30;
                    } else {
                        if (board_bandwidth == 20) { // 20M
                            if (DL_NR_ARFCN >= 500910 && DL_NR_ARFCN < 531130) {
                                kssb = 0; //427250
                                offset2pointA = 24;
                            } else if (DL_NR_ARFCN >= 531130 && DL_NR_ARFCN <= 535950) {
                                kssb = 22; //427250
                                offset2pointA = 30;
                            }
                        } else { // 100M
                            if (DL_NR_ARFCN >= 500910 && DL_NR_ARFCN <= 519870) {
                                kssb = 0; //427250
                                offset2pointA = 24;
                            } else if (DL_NR_ARFCN > 519870 && DL_NR_ARFCN <= 535950) {
                                kssb = 0; //427250
                                offset2pointA = 474;
                            }
                        }
                    }
                    break;

                case 78:
                    if (DL_NR_ARFCN == 633984 || DL_NR_ARFCN == 627264) {
                        kssb = 12;
                        offset2pointA = 24;
                    } else if (board_bandwidth == 20) { // 20M
                        if (DL_NR_ARFCN >= 620544 && DL_NR_ARFCN < 625248) {
                            kssb = 0; //427250
                            offset2pointA = 24;
                        } else if (DL_NR_ARFCN >= 624248 && DL_NR_ARFCN < 626112) {
                            kssb = 22; //427250
                            offset2pointA = 30;
                        } else if (DL_NR_ARFCN >= 626112 && DL_NR_ARFCN < 627360) {
                            kssb = 22; //427250
                            offset2pointA = 30;
                        } else if (DL_NR_ARFCN >= 627360 && DL_NR_ARFCN < 632064) {
                            kssb = 0; //427250
                            offset2pointA = 24;
                        } else if (DL_NR_ARFCN >= 632064 && DL_NR_ARFCN < 632736) {
                            kssb = 22; //427250
                            offset2pointA = 30;
                        } else if (DL_NR_ARFCN >= 632736 && DL_NR_ARFCN < 633984) {
                            kssb = 22; //427250
                            offset2pointA = 30;
                        } else if (DL_NR_ARFCN >= 633984 && DL_NR_ARFCN < 638668) {
                            kssb = 0; //427250
                            offset2pointA = 24;
                        } else if (DL_NR_ARFCN >= 638668 && DL_NR_ARFCN < 639456) {
                            kssb = 22; //427250
                            offset2pointA = 30;
                        } else if (DL_NR_ARFCN >= 639456 && DL_NR_ARFCN < 640704) {
                            kssb = 22; //427250
                            offset2pointA = 30;
                        } else if (DL_NR_ARFCN >= 640704 && DL_NR_ARFCN < 645408) {
                            kssb = 0; //427250
                            offset2pointA = 24;
                        } else if (DL_NR_ARFCN >= 645408 && DL_NR_ARFCN < 646080) {
                            kssb = 22; //427250
                            offset2pointA = 30;
                        } else if (DL_NR_ARFCN >= 646080 && DL_NR_ARFCN < 647328) {
                            kssb = 22; //427250
                            offset2pointA = 30;
                        } else if (DL_NR_ARFCN >= 647328 && DL_NR_ARFCN < 652032) {
                            kssb = 0; //427250
                            offset2pointA = 24;
                        } else if (DL_NR_ARFCN >= 652032 && DL_NR_ARFCN <= 652704) {
                            kssb = 22; //427250
                            offset2pointA = 30;
                        }
                    } else { // 100M
                        if (DL_NR_ARFCN >= 620544 && DL_NR_ARFCN < 646080) {
                            kssb = 0; //427250
                            offset2pointA = 24;
                        } else if (DL_NR_ARFCN >= 646080 && DL_NR_ARFCN <= 652992) {
                            kssb = 0; //427250
                            offset2pointA = 474;
                        }
                    }
                    break;
                case 79:
                    if (DL_NR_ARFCN == 723360){
                        kssb = 14;
                        //更久远：原来用254  后面改成214
                        //2024/02/03 使用230 会影响下行质量，导致上号距离差，需要改成200
                        offset2pointA = 200;
                    }else if (board_bandwidth != 20) {
                        if (DL_NR_ARFCN >= 694080 && DL_NR_ARFCN <= 726432) {
                            kssb = 0; //427250
                            offset2pointA = 24;
                        } else if (DL_NR_ARFCN > 726432 && DL_NR_ARFCN <= 732672) {
                            kssb = 0; //427250
                            offset2pointA = 474;
                        }
                    }
                    break;
            }
        }
        return new PaPkUlArfcnBean(offset2pointA, kssb, UL_NR_ARFCN, slot_index, frame_type);
    }

    private void setSpecial(String id, int cell_id, int time) {
        for (int i = 0; i < traceList.size(); i++) {
            if (traceList.get(i).getId().equals(id) && traceList.get(i).getCellId() == cell_id) {
                if (time == 0) traceList.get(i).setPci(traceList.get(i).getSpecialInit());
                traceList.get(i).setSpecial(time);
                traceList.get(i).setSpecialCount(time - 10); // 10s以上，第一个TAC不变
                break;
            }
        }
    }

    /**特殊机型策略(高通8Genx及华为相关)*/
    public void setSpecialMode(String id, int cell_id, boolean enable) {
        setSpecial(id, cell_id, enable ? 20 : 0);
    }
    /**----------------------DW控制部分开始----------------------**/

    /**
     * 获取定位设备版本
     *
     * @param id 设备ID
     */
    public void getDWVersion(String id) {
        SdkLog.I("controller getDWVersion, id = " + id);
        setMsgType(id, DWProtocol.UI_2_gNB_QUERY_gNB_VERSION);
        setDWOnlyTypeCmd(id, DWProtocol.UI_2_gNB_QUERY_gNB_VERSION);
    }

    /**
     * 配置定位设备系统时间
     *
     * @param id 设备ID
     */
    public void setDWTime(String id) {
        // 时间戳数据格式为"2016-12-08 20:30:30"
        String time = DateUtil.getCurTimeByFormat("yyyy-MM-dd HH:mm:ss");
        SdkLog.I("controller setDWTime, id = " + id + ", time = " + time);
        setMsgType(id, DWProtocol.UI_2_gNB_SET_TIME);
        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.UI_2_gNB_SET_TIME, 0);
        GnbSetTime gnbTime = new GnbSetTime(head, time);
        MessageTransceiver.build().send(id, gnbTime.getMsg());
    }

    /**
     * 配置定位设备NR黑白名单
     *
     * @param id       设备ID
     * @param cell_id  小区ID
     * @param imsi_num 本次设置黑名单个数，最多100个【DWProtocol.MAX_BLACK_IMSI_NUM】
     * @param ueImsi   黑名单列表
     */
    public void setDWNrBlackList(String id, int cell_id, int imsi_num, List<UeidBean> ueImsi) {
        SdkLog.I("controller setDWNrBlackList, id = " + id + ", cell_id = " + cell_id + ", num = " + imsi_num + ", ueImsi = " + ueImsi);
        setMsgType(id, DWProtocol.UI_2_gNB_SET_BLACK_UE_LIST);
        DWHeader header = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.UI_2_gNB_SET_BLACK_UE_LIST, 0);
        GnbBlackList cfg = new GnbBlackList(header, cell_id, imsi_num, ueImsi);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    /**
     * 配置定位设备Lte黑白名单
     *
     * @param id       设备ID
     * @param cell_id  小区ID
     * @param imsi_num 本次设置黑名单个数，最多100个【DWProtocol.MAX_BLACK_IMSI_NUM】
     * @param ueImsi   黑名单列表
     */
    public void setDWLteBlackList(String id, int cell_id, int imsi_num, List<UeidBean> ueImsi) {
        SdkLog.I("controller setDWLteBlackList, id = " + id + ", cell_id = " + cell_id + ", num = " + imsi_num + ", ueImsi = " + ueImsi);
        setMsgType(id, DWProtocol.UI_2_gNB_START_LTE_BLACK_UE_LIST);
        DWHeader header = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.UI_2_gNB_START_LTE_BLACK_UE_LIST, 0);
        GnbBlackList cfg = new GnbBlackList(header, cell_id, imsi_num, ueImsi);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    public void setGnbTFPaGpio(String id) {
        SdkLog.I("setGnbTFPaGpio() id = " + id);

        DWHeader header = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.OAM_MSG_SET_GPIO_MODE, 0);
        TFGnbGpioCfg cfg = new TFGnbGpioCfg(header);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    public void setGnbTFPaGpio() {
        setGnbTFPaGpio(MessageHelper.build().getDeviceId());
    }

    /**
     * 配置定位设备PA控制IO口
     *
     * @param id 设备ID
     *           备注：调用此接口前，请先设置 PaBean
     */
    public void setDWPaGpio(String id) {
        SdkLog.I("controller setDWPaGpio, id = " + id);
        setMsgType(id, DWProtocol.OAM_MSG_SET_GPIO_MODE);
        DWHeader header = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.OAM_MSG_SET_GPIO_MODE, 0);
        GnbGpioCfg cfg = new GnbGpioCfg(header);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    /**
     * 获取定位设备PA控制IO口信息
     *
     * @param id 设备ID
     */
    public void getDWPaGpioInfo(String id) {
        SdkLog.I("controller getDWPaGpioInfo, id = " + id);
        setMsgType(id, DWProtocol.OAM_MSG_GET_GPIO_MODE);
        setDWOnlyTypeCmd(id, DWProtocol.OAM_MSG_GET_GPIO_MODE);
    }

    /**
     * 定位设备NR参数配置
     *
     * @param id              设备ID
     * @param cell_id         小区ID
     * @param startTac        初始tac， [1000 ～ 3000000]
     * @param plmn            第一个 plmn， IMSI号前五位
     * @param arfcn           频点
     * @param pci             PCI [0 ～ 1007]
     * @param ue_max_pwr      UE最大发射功率 默认 10， 最大 20
     * @param timing_Offset   GPS时偏
     * @param work_mode       工作报值间隔  0：160ms；1：40ms
     * @param air_sync        使能空口  1:enable  0:disable
     * @param sub_plmn        第二个 plmn，默认“0”
     * @param ul_rb_offset    固定值：9
     * @param cid             10000+, 少于5位数据无效
     * @param ssb_bitmap      波束 [0 ～ 255]
     * @param band_width      带宽 100: 100MHz  20: 20MHz  10: 10MHz
     * @param cfr_enable      功率峰均值,是否消峰  1:enable  0:disable 默认：enable
     * @param swap_rf         通道交换  0: noswap  1: swap
     * @param reject_code     拒绝原因值  默认：15
     * @param rx_lev_min      接入电平：[-70~-30]， 默认-70
     * @param mob_reject_code 0: disable; other reject code
     * @param split_arfcn_dl  分裂频点
     * @param force_cfg       是否强制建立小区
     * @param split_pci_dl    分裂PCI
     */
    public void initDWNrTrace(String id, int cell_id, int startTac, String plmn, String arfcn, String pci, String ue_max_pwr,
                              int timing_Offset, int work_mode, int air_sync, String sub_plmn, int ul_rb_offset, long cid, int ssb_bitmap,
                              int band_width, int cfr_enable, int swap_rf, int reject_code, int rx_lev_min, int mob_reject_code,
                              String split_arfcn_dl, int force_cfg, String split_pci_dl) {
        SdkLog.D("controller initDWNrTrace， cell_id = " + cell_id + ", startTac = " + startTac + ", plmn = " + plmn
                + ", arfcn = " + arfcn + ", pci = " + pci + ", ue_max_pwr = " + ue_max_pwr + ", timing_Offset = " + timing_Offset + ", work_mode = " + work_mode
                + ", air_sync = " + air_sync + ", subPlmn = " + sub_plmn + ", ul_rb_offset = " + ul_rb_offset + ", cid = " + cid + ", ssb_bitmap = " + ssb_bitmap
                + ", band_width = " + band_width + ", cfr_enable = " + cfr_enable + ", swap_rf = " + swap_rf + ", reject_code = " + reject_code + ", rxLevMin = " + rx_lev_min
                + ", mob_reject_code = " + mob_reject_code + ", split_arfcn_dl = " + split_arfcn_dl
                + ", force_cfg = " + force_cfg + ", split_pci_dl = " + split_pci_dl);

        if (plmn == null || plmn.isEmpty()) {
            SdkLog.E("initDWNrTrace() plmn param err, plmn = " + plmn);
            return;
        }

        if (arfcn == null || arfcn.isEmpty()) {
            SdkLog.E("initDWNrTrace() arfcn param err, arfcn = " + plmn);
            return;
        }

        if (split_arfcn_dl == null || split_arfcn_dl.isEmpty()) split_arfcn_dl = "0";

        if (cid < 10000) cid += 65535;

        if (sub_plmn.equals("0") || sub_plmn.isEmpty()) {
            switch (plmn) {
                case "46011":
                case "46003":
                case "46005":
                case "46012":
                    plmn = "46011";
                    sub_plmn = "46001";
                    break;
                case "46000":
                case "46002":
                case "46007":
                case "46004":
                case "46008":
                case "46013":
                    plmn = "46000";
                    sub_plmn = "46015";
                    break;
                case "46001":
                case "46009":
                case "46006":
                case "46010":
                    plmn = "46001";
                    sub_plmn = "46011";
                    break;
                case "46015":
                    plmn = "46015";
                    sub_plmn = "46000";
                    break;
            }
        }

        removeTraceListById(id, cell_id); // 删除相同通道的配置信息

        traceList.add(new TracePara(id, false, cell_id, "", plmn, arfcn, pci, ue_max_pwr, startTac, startTac + DWProtocol.MAX_TAC_NUM, timing_Offset, work_mode, air_sync, sub_plmn,
                ul_rb_offset, cid, ssb_bitmap, band_width, cfr_enable, swap_rf, reject_code, rx_lev_min, 0, mob_reject_code, split_arfcn_dl, force_cfg, split_pci_dl));
        gnbCfg(id, false, cell_id, startTac, plmn, arfcn, pci, ue_max_pwr, timing_Offset, work_mode, air_sync, sub_plmn, ul_rb_offset,
                cid, ssb_bitmap, band_width, cfr_enable, swap_rf, reject_code, rx_lev_min, mob_reject_code, split_arfcn_dl, force_cfg, split_pci_dl);
    }

    public void initDWNrTrace(String id, int cell_id, int startTac, String plmn, String arfcn, String pci, String ue_max_pwr,
                              int timing_Offset, int work_mode, int air_sync, String subPlmn, int ul_rb_offset, long cid, int ssb_bitmap,
                              int band_width, int cfr_enable, int swap_rf, int reject_code, int rxLevMin, int mob_reject_code,
                              String split_arfcn_dl, int force_cfg) {
        initDWNrTrace(id, cell_id, startTac, plmn, arfcn, pci, ue_max_pwr, timing_Offset, work_mode, air_sync, subPlmn, ul_rb_offset, cid, ssb_bitmap,
                band_width, cfr_enable, swap_rf, reject_code, rxLevMin, mob_reject_code, split_arfcn_dl, force_cfg, pci);
    }

    public void initDWNrTrace(String id, int cell_id, int startTac, String plmn, String arfcn, String pci, String ue_max_pwr,
                              int timing_Offset, int work_mode, int air_sync, String subPlmn, int ul_rb_offset, long cid, int ssb_bitmap,
                              int band_width, int cfr_enable, int swap_rf, int reject_code, int rxLevMin, int mob_reject_code,
                              String split_arfcn_dl) {
        initDWNrTrace(id, cell_id, startTac, plmn, arfcn, pci, ue_max_pwr, timing_Offset, work_mode, air_sync, subPlmn, ul_rb_offset, cid, ssb_bitmap,
                band_width, cfr_enable, swap_rf, reject_code, rxLevMin, mob_reject_code, split_arfcn_dl, 0, pci);
    }

    /**
     * 定位设备LTE参数配置
     * 提示：以下信息备注待修改，修改后删除此行提示
     *
     * @param id              设备ID
     * @param cell_id         小区ID
     * @param startTac        初始tac， [1000 ～ 3000000]
     * @param plmn            第一个 plmn， IMSI号前五位
     * @param arfcn           频点
     * @param pci             PCI [0 ～ 1007]
     * @param ue_max_pwr      UE最大发射功率 默认 10， 最大 20
     * @param timing_Offset   GPS时偏
     * @param work_mode       工作报值间隔  0：160ms；1：40ms
     * @param air_sync        使能空口  1:enable  0:disable
     * @param sub_plmn        第二个 plmn，默认“0”
     * @param ul_rb_offset    固定值：9
     * @param cid             10000+, 少于5位数据无效
     * @param ssb_bitmap      波束 [0 ～ 255]
     * @param band_width      带宽 100: 100MHz  20: 20MHz  10: 10MHz
     * @param cfr_enable      功率峰均值,是否消峰  1:enable  0:disable 默认：enable
     * @param swap_rf         通道交换  0: noswap  1: swap
     * @param reject_code     拒绝原因值  默认：15
     * @param rx_lev_min      接入电平：[-70~-30]， 默认-70
     * @param mob_reject_code 0: disable; other reject code
     * @param split_arfcn_dl  分裂频点
     */
    public void initDWLteTrace(String id, int cell_id, int startTac, String plmn, String arfcn, String pci, String ue_max_pwr,
                               int timing_Offset, int work_mode, int air_sync, String sub_plmn, int ul_rb_offset, long cid, int ssb_bitmap,
                               int band_width, int cfr_enable, int swap_rf, int reject_code, int rx_lev_min, int mob_reject_code,
                               String split_arfcn_dl, int force_cfg, String split_pci_dl) {
        SdkLog.D("controller initDWLteTrace， cell_id = " + cell_id + ", startTac = " + startTac + ", plmn = " + plmn
                + ", arfcn = " + arfcn + ", pci = " + pci + ", ue_max_pwr = " + ue_max_pwr + ", timing_Offset = " + timing_Offset + ", work_mode = " + work_mode
                + ", air_sync = " + air_sync + ", subPlmn = " + sub_plmn + ", ul_rb_offset = " + ul_rb_offset + ", cid = " + cid + ", ssb_bitmap = " + ssb_bitmap
                + ", band_width = " + band_width + ", cfr_enable = " + cfr_enable + ", swap_rf = " + swap_rf + ", reject_code = " + reject_code + ", rxLevMin = " + rx_lev_min
                + ", mob_reject_code = " + mob_reject_code + ", split_arfcn_dl = " + split_arfcn_dl
                + ", force_cfg = " + force_cfg + ", split_pci_dl = " + split_pci_dl);

        if (plmn == null || plmn.isEmpty()) {
            SdkLog.E("initDWLteTrace() plmn param err, plmn = " + plmn);
            return;
        }

        if (arfcn == null || arfcn.isEmpty()) {
            SdkLog.E("initDWLteTrace() arfcn param err, arfcn = " + plmn);
            return;
        }

        if (split_arfcn_dl == null || split_arfcn_dl.isEmpty()) split_arfcn_dl = "0";

        if (cid < 10000) cid += 65535;

        if (sub_plmn.equals("0") || sub_plmn.isEmpty()) {
            switch (plmn) {
                case "46011":
                case "46003":
                case "46005":
                case "46012":
                    plmn = "46011";
                    sub_plmn = "46001";
                    break;
                case "46000":
                case "46002":
                case "46007":
                case "46004":
                case "46008":
                case "46013":
                    plmn = "46000";
                    sub_plmn = "46015";
                    break;
                case "46001":
                case "46009":
                case "46006":
                case "46010":
                    plmn = "46001";
                    sub_plmn = "46011";
                    break;
                case "46015":
                    plmn = "46015";
                    sub_plmn = "46000";
                    break;
            }
        }

        removeTraceListById(id, cell_id); // 删除相同通道的配置信息

        traceList.add(new TracePara(id, true, cell_id, "", plmn, arfcn, pci, ue_max_pwr, startTac, startTac + DWProtocol.MAX_TAC_NUM, timing_Offset, work_mode, air_sync, sub_plmn,
                ul_rb_offset, cid, ssb_bitmap, band_width, cfr_enable, swap_rf, reject_code, rx_lev_min, 0, mob_reject_code, split_arfcn_dl, force_cfg, split_pci_dl));
        gnbCfg(id, true, cell_id, startTac, plmn, arfcn, pci, ue_max_pwr, timing_Offset, work_mode, air_sync, sub_plmn, ul_rb_offset,
                cid, ssb_bitmap, band_width, cfr_enable, 0, reject_code, rx_lev_min, mob_reject_code, split_arfcn_dl, force_cfg, split_pci_dl);
    }

    public void initDWLteTrace(String id, int cell_id, int startTac, String plmn, String arfcn, String pci, String ue_max_pwr,
                               int timing_Offset, int work_mode, int air_sync, String subPlmn, int ul_rb_offset, long cid, int ssb_bitmap,
                               int band_width, int cfr_enable, int swap_rf, int reject_code, int rxLevMin, int mob_reject_code,
                               String split_arfcn_dl, int force_cfg) {
        initDWLteTrace(id, cell_id, startTac, plmn, arfcn, pci, ue_max_pwr, timing_Offset, work_mode, air_sync, subPlmn, ul_rb_offset, cid, ssb_bitmap,
                band_width, cfr_enable, swap_rf, reject_code, rxLevMin, mob_reject_code, split_arfcn_dl, force_cfg, pci);
    }

    public void initDWLteTrace(String id, int cell_id, int startTac, String plmn, String arfcn, String pci, String ue_max_pwr,
                               int timing_Offset, int work_mode, int air_sync, String subPlmn, int ul_rb_offset, long cid, int ssb_bitmap,
                               int band_width, int cfr_enable, int swap_rf, int reject_code, int rxLevMin, int mob_reject_code,
                               String split_arfcn_dl) {
        initDWLteTrace(id, cell_id, startTac, plmn, arfcn, pci, ue_max_pwr, timing_Offset, work_mode, air_sync, subPlmn, ul_rb_offset, cid, ssb_bitmap,
                band_width, cfr_enable, swap_rf, reject_code, rxLevMin, mob_reject_code, split_arfcn_dl, 0, pci);
    }

    /**
     * 配置定位设备频点
     */
    private void gnbCfg(String id, boolean is_lte, int cell_id, int tac, String plmn, String arfcn, String pci, String ue_max_pwr, int timingOffset,
                        int workMode, int airSync, String sub_plmn, int ul_rb_offset, long cid, int ssb_bitmap, int bandwidth, int cfr_enable,
                        int swap_rf, int reject_code, int rxLevMin, int mob_reject_code, String split_arfcn_dl, int force_cfg, String split_pci_dl) {
        int MCC = Integer.parseInt(plmn.substring(0, 3));
        int MNC = Integer.parseInt(plmn.length() > 5 ? "1" + plmn.substring(3) : plmn.substring(3));
        int MCC2 = 0;
        int MNC2 = 0;
        int frame_type = DWProtocol.FrameType.FRAME_TYPE_TDD_CFG_2D5MS;
        if (sub_plmn.length() > 4) {
            MCC2 = Integer.parseInt(sub_plmn.substring(0, 3));
            MNC2 = Integer.parseInt(sub_plmn.length() > 5 ? "1" + sub_plmn.substring(3) : sub_plmn.substring(3));
        } else {
            SdkLog.D("MCC2 = 0 not avail");
        }
        //tdd DL_NR_ARFCN = UL_NR_ARFCN
        int DL_NR_ARFCN = Integer.parseInt(arfcn);
        int UL_NR_ARFCN = DL_NR_ARFCN;
        if (split_arfcn_dl.isEmpty()) split_arfcn_dl = "0";
        int SplitArfcnDl = Integer.parseInt(split_arfcn_dl);
        int SplitArfcnUl = 0;
        int band = is_lte ? LteBand.earfcn2band(DL_NR_ARFCN) : NrBand.earfcn2band(DL_NR_ARFCN);
        if (is_lte) {
            if (band < 33 || band > 53) {
                frame_type = DWProtocol.FrameType.FRAME_TYPE_LTE_FDD_CFG;
                UL_NR_ARFCN = DL_NR_ARFCN + LteBand.getUlAddNumByBand(band);
            } else frame_type = DWProtocol.FrameType.FRAME_TYPE_CMCC_TDD_CFG_5MS;
        } else {
            //fdd UL_ARFCN != DL_NR_ARFCN
            // DL_NR_ARFCN >= 600000 表示频段 > 3G
            if (DL_NR_ARFCN >= 600000) {
                frame_type = DWProtocol.FrameType.FRAME_TYPE_TDD_CFG_2D5MS;
            } else {
                frame_type = DWProtocol.FrameType.FRAME_TYPE_CMCC_TDD_CFG_5MS;
                switch (band) {
                    case 1:
                        // 434000 - 396000 = 38000
                        // 422000 - 384000 = 38000
                        if (DL_NR_ARFCN == 422890 || DL_NR_ARFCN == 422930) { //422930
                            UL_NR_ARFCN = DL_NR_ARFCN - 38798;
                        } else if (DL_NR_ARFCN == 427010) {
                            UL_NR_ARFCN = 388092;
                        } else if (DL_NR_ARFCN == 428910) { // 重庆
                            UL_NR_ARFCN = 388072;
                        } else {
                            //UL_NR_ARFCN = DL_NR_ARFCN - 38000;
                            //2024/1/17 调整默认则使用391010
                            UL_NR_ARFCN = 391010;
                        }
                        frame_type = DWProtocol.FrameType.FRAME_TYPE_FDD_CFG;
                        break;
                    case 3:
                        UL_NR_ARFCN = 349500;
                        frame_type = DWProtocol.FrameType.FRAME_TYPE_FDD_CFG;
                        break;
                    case 5:
                        UL_NR_ARFCN = 165000;
                        frame_type = DWProtocol.FrameType.FRAME_TYPE_FDD_CFG;
                        break;
                    case 7:
                        UL_NR_ARFCN = 507000;
                        frame_type = DWProtocol.FrameType.FRAME_TYPE_FDD_CFG;
                        break;
                    case 8:
                        UL_NR_ARFCN = 178000;
                        frame_type = DWProtocol.FrameType.FRAME_TYPE_FDD_CFG;
                        break;
                    case 28:  // N28A: 703 -- 733(748)
                        // 151600 - 140600 = 11000
                        // 160600 - 149600 = 11000
                        UL_NR_ARFCN = 140720; //154810  152890 154810 154570 152650

                        frame_type = DWProtocol.FrameType.FRAME_TYPE_FDD_CFG;
                        break;
                }
            }
        }

        int PCI = Integer.parseInt(pci);
        int TAC = tac;
        int ssb_pwr = Integer.parseInt(ue_max_pwr);

        int timing_offset = timingOffset;
        GnbTimingOffset.TimingBean tb = GnbTimingOffset.build().getTimingOffset(DL_NR_ARFCN);
        if (tb != null) {
            if (timing_offset == -1) {
                timing_offset = tb.getTimingOffset();
            } else if (band == 1 || band == 3 || band == 78) {
                // 联通电信用配置表里的频偏，避免客户乱配置
                timing_offset = tb.getTimingOffset();
            }
        }
        int offset2pointA = 0;
        int kssb = 0;
        if (!is_lte) {
            if (bandwidth != 20) bandwidth = 100; // 目前仅支持20、100M
            switch (band) {
                case 1:
                    if (DL_NR_ARFCN == 427250) {
                        kssb = 6; //427250
                        offset2pointA = 23;
                    } else if (DL_NR_ARFCN == 422890 || DL_NR_ARFCN == 422930) { //422930
                        kssb = 2;
                        offset2pointA = 12;
                    } else if (DL_NR_ARFCN == 427010) {
                        kssb = 6;
                        offset2pointA = 15;
                    } else if (DL_NR_ARFCN == 428910) {
                        kssb = 10;
                        offset2pointA = 68;
                    } else {
                        if (DL_NR_ARFCN > 422890 && DL_NR_ARFCN < 428170) {
                            kssb = 0; //427250
                            offset2pointA = 12;
                        } else if (DL_NR_ARFCN >= 428170 && DL_NR_ARFCN <= 433010) {
                            kssb = 0; //427250
                            offset2pointA = 70;
                        }
                    }
                    tb = GnbTimingOffset.build().getTimingOffset(427250);
                    timing_offset = tb.getTimingOffset();
                    break;
                case 3:
                    if (DL_NR_ARFCN >= 362170 && DL_NR_ARFCN < 368450) {
                        kssb = 0;
                        offset2pointA = 12;
                    } else if (DL_NR_ARFCN >= 368450 && DL_NR_ARFCN < 374930) {
                        kssb = 0;
                        offset2pointA = 70;
                    }
                    break;
                case 5:
                    kssb = 0;
                    offset2pointA = 24;
                    break;
                case 8:
                    if (DL_NR_ARFCN >= 186010 && DL_NR_ARFCN < 188410) {
                        kssb = 0;
                        offset2pointA = 12;
                    } else if (DL_NR_ARFCN >= 188410 && DL_NR_ARFCN < 190850) {
                        kssb = 0;
                        offset2pointA = 70;
                    }
                    break;
                case 7:
                    if (DL_NR_ARFCN >= 525150 && DL_NR_ARFCN < 530190) {
                        kssb = 0;
                        offset2pointA = 12;
                    } else if (DL_NR_ARFCN >= 530190 && DL_NR_ARFCN < 536910) {
                        kssb = 0;
                        offset2pointA = 70;
                    }
                    break;
                case 28:
                    // 2024/01/26 由于N28用多波束240，因此SDK强制N28使用单波束128，解决N28和B40干扰问题
                    ssb_bitmap = 128;
                    if (DL_NR_ARFCN == 154810) {
                        kssb = 6; //
                        offset2pointA = 23;
                    } else if (DL_NR_ARFCN == 152890) {
                        kssb = 6;
                        offset2pointA = 22;
                    }/* else if (DL_NR_ARFCN == 156970) {
                        kssb = 10; //
                        offset2pointA = 135;
                    } */else if (DL_NR_ARFCN == 152650) {
                        kssb = 6; //
                        offset2pointA = 12;
                    } else if (DL_NR_ARFCN == 154570) {
                        kssb = 2;
                        offset2pointA = 69;
                    }/* else if (DL_NR_ARFCN == 156490) {
                        kssb = 6;
                        //offset2pointA = 122;
                        offset2pointA = 70;
                    } */else {
                        if (DL_NR_ARFCN >= 152410 && DL_NR_ARFCN < 154090) {
                            kssb = 0; //427250
                            offset2pointA = 12;
                        } else if (DL_NR_ARFCN >= 154090 && DL_NR_ARFCN < 155290) {
                            kssb = 0; //427250
                            offset2pointA = 56;
                        } else if (DL_NR_ARFCN >= 155290 && DL_NR_ARFCN <= 156530) {
                            kssb = 0; //427250
                            offset2pointA = 70;
                        }
                    }
                    //tb = GnbTimingOffset.build().getTimingOffset(154810);
                    //timing_offset = tb.getTimingOffset();
                    break;

                case 41:
                    if (DL_NR_ARFCN == 504990) {
                        kssb = 6;
                        offset2pointA = 30;
                    } else if (DL_NR_ARFCN == 533070) {
                        kssb = 6;
                        offset2pointA = 30;
                    } else {
                        if (bandwidth == 20) { // 20M
                            if (DL_NR_ARFCN >= 500910 && DL_NR_ARFCN < 531130) {
                                kssb = 0; //427250
                                offset2pointA = 24;
                            } else if (DL_NR_ARFCN >= 531130 && DL_NR_ARFCN <= 535950) {
                                kssb = 22; //427250
                                offset2pointA = 30;
                            }
                        } else { // 100M
                            if (DL_NR_ARFCN >= 500910 && DL_NR_ARFCN <= 519870) {
                                kssb = 0; //427250
                                offset2pointA = 24;
                            } else if (DL_NR_ARFCN > 519870 && DL_NR_ARFCN <= 535950) {
                                kssb = 0; //427250
                                offset2pointA = 474;
                            }
                        }
                    }
                    tb = GnbTimingOffset.build().getTimingOffset(504990);
                    break;

                case 78:
                    if (DL_NR_ARFCN == 633984 || DL_NR_ARFCN == 627264) {
                        kssb = 12;
                        offset2pointA = 24;
                    } else if (bandwidth == 20) { // 20M
                        if (DL_NR_ARFCN >= 620544 && DL_NR_ARFCN < 625248) {
                            kssb = 0; //427250
                            offset2pointA = 24;
                        } else if (DL_NR_ARFCN >= 624248 && DL_NR_ARFCN < 626112) {
                            kssb = 22; //427250
                            offset2pointA = 30;
                        } else if (DL_NR_ARFCN >= 626112 && DL_NR_ARFCN < 627360) {
                            kssb = 22; //427250
                            offset2pointA = 30;
                        } else if (DL_NR_ARFCN >= 627360 && DL_NR_ARFCN < 632064) {
                            kssb = 0; //427250
                            offset2pointA = 24;
                        } else if (DL_NR_ARFCN >= 632064 && DL_NR_ARFCN < 632736) {
                            kssb = 22; //427250
                            offset2pointA = 30;
                        } else if (DL_NR_ARFCN >= 632736 && DL_NR_ARFCN < 633984) {
                            kssb = 22; //427250
                            offset2pointA = 30;
                        } else if (DL_NR_ARFCN >= 633984 && DL_NR_ARFCN < 638668) {
                            kssb = 0; //427250
                            offset2pointA = 24;
                        } else if (DL_NR_ARFCN >= 638668 && DL_NR_ARFCN < 639456) {
                            kssb = 22; //427250
                            offset2pointA = 30;
                        } else if (DL_NR_ARFCN >= 639456 && DL_NR_ARFCN < 640704) {
                            kssb = 22; //427250
                            offset2pointA = 30;
                        } else if (DL_NR_ARFCN >= 640704 && DL_NR_ARFCN < 645408) {
                            kssb = 0; //427250
                            offset2pointA = 24;
                        } else if (DL_NR_ARFCN >= 645408 && DL_NR_ARFCN < 646080) {
                            kssb = 22; //427250
                            offset2pointA = 30;
                        } else if (DL_NR_ARFCN >= 646080 && DL_NR_ARFCN < 647328) {
                            kssb = 22; //427250
                            offset2pointA = 30;
                        } else if (DL_NR_ARFCN >= 647328 && DL_NR_ARFCN < 652032) {
                            kssb = 0; //427250
                            offset2pointA = 24;
                        } else if (DL_NR_ARFCN >= 652032 && DL_NR_ARFCN <= 652704) {
                            kssb = 22; //427250
                            offset2pointA = 30;
                        }
                    } else { // 100M
                        if (DL_NR_ARFCN >= 620544 && DL_NR_ARFCN < 646080) {
                            kssb = 0; //427250
                            offset2pointA = 24;
                        } else if (DL_NR_ARFCN >= 646080 && DL_NR_ARFCN <= 652992) {
                            kssb = 0; //427250
                            offset2pointA = 474;
                        }
                    }

                    tb = GnbTimingOffset.build().getTimingOffset(627264);
                    timing_offset = tb.getTimingOffset();
                    break;
                case 79:
                    if (DL_NR_ARFCN == 723360){
                        kssb = 14;
                        //更久远：原来用254  后面改成214
                        //2024/02/03 使用230 会影响下行质量，导致上号距离差，需要改成200
                        offset2pointA = 200;
                    }else if (bandwidth != 20) {
                        if (DL_NR_ARFCN >= 694080 && DL_NR_ARFCN <= 726432) {
                            kssb = 0; //427250
                            offset2pointA = 24;
                        } else if (DL_NR_ARFCN > 726432 && DL_NR_ARFCN <= 732672) {
                            kssb = 0; //427250
                            offset2pointA = 474;
                        }
                    }

                    tb = GnbTimingOffset.build().getTimingOffset(723360);
                    break;
            }
        }

        if (timing_offset == -1) {
            if (tb == null) {
                timing_offset = 0;
            } else {
                timing_offset = tb.getTimingOffset();
            }
        }

        setPk(id, cell_id, kssb);
        setPa(id, cell_id, offset2pointA);
        int split_cid = 0;
        int split_pci = Integer.parseInt(split_pci_dl);
        int split_offset2pointA = 0;
        int split_kssb = 0;
        if (force_cfg != 1) force_cfg = 0;
        setMsgType(id, is_lte ? DWProtocol.UI_2_gNB_LTE_CFG_gNB : DWProtocol.UI_2_gNB_CFG_gNB);
        DWHeader header = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, is_lte ? DWProtocol.UI_2_gNB_LTE_CFG_gNB : DWProtocol.UI_2_gNB_CFG_gNB, 0);
        GnbCfgGnb cfg = new GnbCfgGnb(header, cell_id, MCC, MNC, DL_NR_ARFCN, UL_NR_ARFCN, PCI, TAC, offset2pointA, kssb,
                ssb_pwr, timing_offset, workMode, airSync, MCC2, MNC2, ul_rb_offset, cid, ssb_bitmap, frame_type, bandwidth,
                cfr_enable, swap_rf, reject_code, rxLevMin, 0, mob_reject_code, split_cid, split_pci, split_offset2pointA,
                split_kssb, SplitArfcnDl, SplitArfcnUl, force_cfg);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    /**
     * 配置定位设备功率衰减，掉电恢复默认
     *
     * @param id              设备ID
     * @param cell_id         小区ID
     * @param band_id         频段
     * @param arfcn           频点
     * @param tx_power_offset 功率衰减值
     */
    public void setDWTxPwrOffset(String id, int cell_id, int band_id, int arfcn, int tx_power_offset) {
        SdkLog.I("controller setDWTxPwrOffset, id = " + id + ", cell_id = " + cell_id + ", band_id = " + band_id + ", arfcn = " + arfcn + ", value = " + tx_power_offset);
        if (tx_power_offset > 3) tx_power_offset = 3;
        for (int i = 0; i < traceList.size(); i++) {
            if (traceList.get(i).getId().equals(id)) {
                traceList.get(i).setTraceTacChangeDelay(5);
                break;
            }
        }
        setMsgType(id, DWProtocol.UI_2_gNB_SET_TX_POWER_OFFSET);
        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.UI_2_gNB_SET_TX_POWER_OFFSET, band_id);
        GnbSetTxPwr txPwrOffset = new GnbSetTxPwr(head, cell_id, arfcn, tx_power_offset);
        MessageTransceiver.build().send(id, txPwrOffset.getMsg());
    }
    public void setDWTxPwrOffset(String id, int cell_id, int arfcn, int tx_power_offset){
        setDWTxPwrOffset(id, cell_id, 0, arfcn, tx_power_offset);
    }

    /**
     * 配置定位设备功率衰减校验，掉电不恢复
     *
     * @param id              设备ID
     * @param cell_id         小区ID
     * @param arfcn           频点
     * @param tx_power_offset 功率衰减值
     */
    public void setDWNvTxPwrOffset(String id, int cell_id, int arfcn, int tx_power_offset) {
        SdkLog.I("controller setDWNvTxPwrOffset, id = " + id + ", cell_id = " + cell_id + ", arfcn = " + arfcn + ", value = " + tx_power_offset);
        setMsgType(id, DWProtocol.OAM_MSG_ADJUST_TX_ATTEN);
        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.OAM_MSG_ADJUST_TX_ATTEN, 0);
        GnbSetTxPwr txPwrOffset = new GnbSetTxPwr(head, cell_id, arfcn, tx_power_offset);
        MessageTransceiver.build().send(id, txPwrOffset.getMsg());
    }

    /**
     * 启动定位设备NR定位
     *
     * @param id         设备ID
     * @param cell_id    小区ID
     * @param imsi       IMSI号 长度 15
     * @param target_num 目标数量【1 ~ 3】
     */
    public void startDWNrTrace(String id, int cell_id, int target_num, String imsi) {
        SdkLog.I("controller startDWNrTrace, id = " + id + ", cell_id = " + cell_id + ", target_num = " + target_num + ", imsi =" + imsi);
        if (imsi == null) {
            SdkLog.E("controller startDWNrTrace error, imsi is null, return and end");
            return;
        }
        setTraceType(id, DWProtocol.TraceType.TRACE);
        setMsgType(id, DWProtocol.UI_2_gNB_START_TRACE);
        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.UI_2_gNB_START_TRACE, 0);
        GnbStartTrace start = new GnbStartTrace(head, cell_id, target_num, imsi);
        MessageTransceiver.build().send(id, start.getMsg());

        setImsi(id, cell_id, imsi);
        setEnableChangeTac(id, cell_id, true);
        isDoStop = false;
    }

    public void startDWNrTrace(String id, int cell_id, List<UeidBean> ueid, int cmd_param, int report_phone_type) {
        StringBuilder ue = new StringBuilder();
        for (UeidBean bean : ueid) {
            ue.append(bean.getImsi()).append(",").append(bean.getGuti()).append(";");
        }
        SdkLog.I("startDWNrTrace() id = " + id + ", cell_id = " + cell_id + ", ue = " + ue + ", report_phone_type = " + report_phone_type);

        if (report_phone_type != 1) report_phone_type = 0;

        setTraceType(id, DWProtocol.TraceType.TRACE);
        setMsgType(id, DWProtocol.UI_2_gNB_START_TRACE);

        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.UI_2_gNB_START_TRACE, cmd_param);
        GnbStartTrace start = new GnbStartTrace(head, cell_id, ueid, report_phone_type);
        MessageTransceiver.build().send(id, start.getMsg());

        if (ueid.size() > 0) setImsi(id, cell_id, ueid.get(0).getImsi());
        setEnableChangeTac(id, cell_id, true);
        isDoStop = false;
    }

    /**
     * 结束定位设备NR定位
     *
     * @param id      设备ID
     * @param cell_id 小区ID
     */
    public void stopDWNrTrace(String id, int cell_id) {
        SdkLog.I("controller stopDWNrTrace, id = " + id + ", cell_id = " + cell_id);
        setMsgType(id, DWProtocol.UI_2_gNB_STOP_TRACE);
        setDWCmdAndCellID(id, DWProtocol.UI_2_gNB_STOP_TRACE, cell_id);

        setEnableChangeTac(id, cell_id, false);
        isDoStop = true;
    }

    /**
     * 启动定位设备LTE定位
     *
     * @param id         设备ID
     * @param cell_id    小区ID
     * @param target_num 目标数量【1 ~ 3】
     * @param imsi       IMSI号 长度 15
     */
    public void startDWLteTrace(String id, int cell_id, int target_num, String imsi) {
        SdkLog.I("controller startDWLteTrace, id = " + id + ", cell_id = " + cell_id + ", target_num = " + target_num + ", imsi =" + imsi);
        if (imsi == null) {
            SdkLog.E("controller startDWLteTrace error, imsi is null, return and end");
            return;
        }
        setTraceType(id, DWProtocol.TraceType.LTE_TRACE);
        setMsgType(id, DWProtocol.UI_2_gNB_START_LTE_TRACE);
        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.UI_2_gNB_START_LTE_TRACE, 0);
        GnbStartTrace start = new GnbStartTrace(head, cell_id, target_num, imsi);
        MessageTransceiver.build().send(id, start.getMsg());

        setImsi(id, cell_id, imsi);
        setEnableChangeTac(id, cell_id, true);
        isDoStop = false;
    }

    public void startDWLteTrace(String id, int cell_id, List<UeidBean> ueid, int report_phone_type) {
        StringBuilder ue = new StringBuilder();
        for (UeidBean bean : ueid) {
            ue.append(bean.getImsi()).append(",").append(bean.getGuti()).append(";");
        }
        SdkLog.I("startDWLteTrace() id = " + id + ", cell_id = " + cell_id + ", ue = " + ue + ", report_phone_type = " + report_phone_type);

        if (report_phone_type != 1) report_phone_type = 0;

        setTraceType(id, DWProtocol.TraceType.TRACE);
        setMsgType(id, DWProtocol.UI_2_gNB_START_LTE_TRACE);

        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.UI_2_gNB_START_LTE_TRACE, 0);
        GnbStartTrace start = new GnbStartTrace(head, cell_id, ueid, report_phone_type);
        MessageTransceiver.build().send(id, start.getMsg());

        if (ueid.size() > 0) setImsi(id, cell_id, ueid.get(0).getImsi());
        setEnableChangeTac(id, cell_id, true);
        isDoStop = false;
    }

    /**
     * 结束定位设备LTE定位
     *
     * @param id      设备ID
     * @param cell_id 小区ID
     */
    public void stopDWLteTrace(String id, int cell_id) {
        SdkLog.I("controller stopDWLteTrace, id = " + id + ", cell_id = " + cell_id);
        setMsgType(id, DWProtocol.UI_2_gNB_STOP_LTE_TRACE);
        setDWCmdAndCellID(id, DWProtocol.UI_2_gNB_STOP_LTE_TRACE, cell_id);

        setEnableChangeTac(id, cell_id, false);
        isDoStop = true;
    }

    /**
     * 启动定位设备侦码
     *
     * @param id           设备ID
     * @param cell_id      小区ID
     * @param save_flag    重启后自启动标志， 1：重启设备后,将按照当前配置自启动围栏功能。  0：不自启  注意:save_flag=1时，tac_interval不能配置为0
     * @param start_tac    起始TAC [1000 ～ 3000000]
     * @param end_tac      结束TAC 大于起始TAC
     * @param tac_interval 间隔时间， 值范围 [0,10-9999]， 1、等于0时，设备被动执行； 2、大于9时，设备按上层给出的间隔值自动执行配置TAC自动自增。
     * @param isLte        是否为4G
     */
    public void startDWCatch(String id, int cell_id, int save_flag, int start_tac, int end_tac, int tac_interval, boolean isLte) {
        SdkLog.I("controller startDWCatch, id = " + id + ", cell_id = " + cell_id + ", save_flag = " + save_flag + ", start_tac = " + start_tac + ", end_tac = " + end_tac + ", tac_interva = " + tac_interval + ", isLte =" + isLte);
        setTraceType(id, DWProtocol.TraceType.CATCH);

        int cmd = isLte ? DWProtocol.UI_2_eNB_START_CATCH : DWProtocol.UI_2_gNB_START_CATCH;
        setMsgType(id, cmd);
        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, cmd, 0);
        GnbStartCatch start = new GnbStartCatch(head, cell_id, save_flag, start_tac, end_tac, tac_interval);
        for (int i = 0; i < traceList.size(); i++) {
            if (traceList.get(i).getId().equals(id) && traceList.get(i).getCellId() == cell_id) {
                traceList.get(i).setStartTac(start_tac);
                if (end_tac < start_tac) end_tac = start_tac + 100;
                traceList.get(i).setMaxTac(end_tac);
                traceList.get(i).setRunTac(end_tac - start_tac);
                traceList.get(i).setTacInterval(tac_interval);
                break;
            }
        }
        MessageTransceiver.build().send(id, start.getMsg());

        setEnableChangeTac(id, cell_id, tac_interval < 10); // 小于9时，则允许EnableChangeTac为true，之后SDK自动自增TAC下发
        isDoStop = false;
    }

    /**
     * 结束定位设备侦码
     *
     * @param id         设备ID
     * @param cell_id    小区ID
     * @param save_flag: 0: 不删除 1：删除自启动配置参数
     * @param isLte      是否为4G
     */
    public void stopDWCatch(String id, int cell_id, int save_flag, boolean isLte) {
        SdkLog.I("controller stopDWCatch, id = " + id + ", cell_id = " + cell_id + ", save_flag = " + save_flag + ", isLte =" + isLte);

        int cmd = isLte ? DWProtocol.UI_2_eNB_STOP_CATCH : DWProtocol.UI_2_gNB_STOP_CATCH;
        setMsgType(id, cmd);
        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, cmd, save_flag);
        GnbOnlyCellID cfg = new GnbOnlyCellID(head, cell_id);
        MessageTransceiver.build().send(id, cfg.getMsg());

        setEnableChangeTac(id, cell_id, false);
        isDoStop = true;
    }

    /**
     * 获取定位设备侦码配置信息
     *
     * @param id      设备ID
     * @param cell_id 小区ID
     */
    public void getDWCatchCfg(String id, int cell_id) {
        SdkLog.I("controller getDWCatchCfg, id = " + id + ", cell_id = " + cell_id);
        setMsgType(id, DWProtocol.OAM_MSG_GET_CATCH_CFG);
        setDWCmdAndCellID(id, DWProtocol.OAM_MSG_GET_CATCH_CFG, cell_id);
    }

    /**
     * 启动定位设备管控
     *
     * @param id      设备ID
     * @param cell_id 小区ID
     * @param mode    0:管控  1:压制
     * @param isLte   是否为4G
     */
    public void startDWControl(String id, int cell_id, int mode, boolean isLte) {
        SdkLog.I("controller startDWControl, id = " + id + ", cell_id = " + cell_id + ", mode =" + mode + ", isLte =" + isLte);
        setTraceType(id, DWProtocol.TraceType.CONTROL);
        int cmd = isLte ? DWProtocol.UI_2_eNB_START_CONTROL : DWProtocol.UI_2_gNB_START_CONTROL;
        setMsgType(id, cmd);
        DWHeader header = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, cmd, 0);
        GnbStartControl cfg = new GnbStartControl(header, cell_id, mode);
        setEnableChangeTac(id, cell_id, true);
        MessageTransceiver.build().send(id, cfg.getMsg());

        setEnableChangeTac(id, cell_id, true);
        isDoStop = false;
    }

    /**
     * 结束定位设备管控
     *
     * @param id      设备ID
     * @param cell_id 小区ID
     * @param isLte   是否为4G
     */
    public void stopDWControl(String id, int cell_id, boolean isLte) {
        SdkLog.I("controller stopDWControl, id = " + id + ", cell_id = " + cell_id + ", isLte = " + isLte);

        int cmd = isLte ? DWProtocol.UI_2_eNB_STOP_CONTROL : DWProtocol.UI_2_gNB_STOP_CONTROL;
        setMsgType(id, cmd);
        setDWCmdAndCellID(id, cmd, cell_id);

        setEnableChangeTac(id, cell_id, false);
        isDoStop = true;
    }

    /**
     * 开始定位设备扫频
     *
     * @param id           设备ID
     * @param report_level 报告等级， 0:只传大小区频点相关信息  1：以文件的方式保存SIBx信息
     * @param async_enable 空口使能  1:enable  0:disable
     * @param arfcn_num    频点输出数量  Max = 32
     * @param chan_id      频段对应Rx通道，可设置32组值， 1 （Rx1）  2（Rx2）
     * @param arfcn_list   频点数组，可设置32组值  0 表示无效
     * @param time_offset  GPS 时偏，可设置32组值  默认0
     */
    public void startDWFreqScan(String id, int report_level, int async_enable, int arfcn_num, List<Integer> chan_id
            , List<Integer> arfcn_list, List<Integer> time_offset) {
        SdkLog.I("controller startDWFreqScan, id = " + id + ", report_level = " + report_level + ", async_enable = " + async_enable
                + ", arfcn_num = " + arfcn_num + ", chan_id = " + chan_id + ", arfcn_list = " + arfcn_list.toString()
                + ", time_offset = " + time_offset.toString());

        if (report_level == 0) setMsgType(id, DWProtocol.OAM_MSG_START_FREQ_SCAN);
        else if (report_level == 1) setMsgType(id, DWProtocol.OAM_MSG_START_FREQ_SCAN + 1);
        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.OAM_MSG_START_FREQ_SCAN, 0);
        GnbStartFreqScan cmd = new GnbStartFreqScan(head, report_level, async_enable, arfcn_num, chan_id, arfcn_list, time_offset);
        MessageTransceiver.build().send(id, cmd.getMsg());
    }

    /**
     * 开始定位设备扫频
     *
     * @param id           设备ID
     * @param report_level 报告等级， 0:只传大小区频点相关信息  1：以文件的方式保存SIBx信息
     * @param async_enable 空口使能  1:enable  0:disable
     * @param band_id      频段ID  为区分频点冲突或包含问题，如 N7 和 N41，默认为N41填 0 即可，如需调整为N7则配 7
     * @param arfcn_num    频点输出数量  Max = 32
     * @param chan_id      频段对应Rx通道，可设置32组值， 1 （Rx1）  2（Rx2）
     * @param arfcn_list   频点数组，可设置32组值  0 表示无效
     * @param time_offset  GPS 时偏，可设置32组值  默认0
     */
    public void startDWFreqScan(String id, int report_level, int async_enable, int band_id, int arfcn_num, List<Integer> chan_id
            , List<Integer> arfcn_list, List<Integer> time_offset) {
        // band_id，由此来区分冲突包含频段
        int bandNum = band_id << 23; // 0 means nul1，9(band)+23(arfcn)
        SdkLog.I("controller have band_id = " + band_id + ", bandNum = " + bandNum);
        for (int i = 0; i < arfcn_list.size(); i++){
            arfcn_list.set(i, arfcn_list.get(i) + bandNum);
        }
        startDWFreqScan(id, report_level, async_enable, arfcn_num, chan_id, arfcn_list, time_offset);
    }

    /**
     * 开始定位设备频段扫频
     *
     * @param id           设备ID
     * @param report_level 报告等级， 0:只传大小区频点相关信息  1：以文件的方式保存SIBx信息
     * @param async_enable 空口使能  1:enable  0:disable
     * @param chan_id      频段对应Rx通道， 1 （Rx1）  2（Rx2）
     * @param band_id      频段  1、41、78、79、28
     * @param time_offset  GPS 时偏， 默认0
     */
    public void startBandFreqScan(String id, int report_level, int async_enable, int chan_id, int band_id, int time_offset) {
        SdkLog.I("controller startBandFreqScan, id = " + id + ", report_level = " + report_level + ", async_enable = " + async_enable
                + ", chan_id = " + chan_id + ", band_id = " + band_id + ", time_offset = " + time_offset);
        if (report_level == 0) setMsgType(id, DWProtocol.OAM_MSG_START_BAND_SCAN);
        else if (report_level == 1) setMsgType(id, DWProtocol.OAM_MSG_START_BAND_SCAN + 1);
        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.OAM_MSG_START_BAND_SCAN, 0);
        GnbStartBandScan bandScan = new GnbStartBandScan(head, report_level, async_enable, chan_id, band_id, time_offset);
        MessageTransceiver.build().send(id, bandScan.getMsg());
    }

    /**
     * 结束定位设备扫频
     *
     * @param id 设备ID
     */
    public void stopDWFreqScan(String id) {
        SdkLog.I("controller stopDWFreqScan, id = " + id);
        setMsgType(id, DWProtocol.OAM_MSG_STOP_FREQ_SCAN);
        setDWOnlyTypeCmd(id, DWProtocol.OAM_MSG_STOP_FREQ_SCAN);
    }

    /**
     * 获取定位设备基带LOG
     *
     * @param id       设备ID
     * @param type     tftp server: 0; ftp server: 1; serial: 2; scp: 3
     * @param log_name 日志输出名称
     */
    public void getDWLog(String id, int type, String log_name) {
        SdkLog.I("controller getDWLog, id = " + id + ", type = " + type + ", log_name = " + log_name);
        setMsgType(id, DWProtocol.UI_2_gNB_GET_LOG_REQ);
        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.UI_2_gNB_GET_LOG_REQ, 0);
        GnbGetLog log = new GnbGetLog(head, type, log_name);
        MessageTransceiver.build().send(id, log.getMsg());
    }

    /**
     * 获取定位设备黑匣子数据
     *
     * @param id       设备ID
     * @param type     tftp server: 0; ftp server: 1; serial: 2; scp: 3
     * @param log_name 日志输出名称
     */
    public void getDWOpLog(String id, int type, String log_name) {
        SdkLog.I("controller getDWOpLog, id = " + id + ", type = " + type + ", log_name = " + log_name);
        setMsgType(id, DWProtocol.UI_2_gNB_GET_OP_LOG_REQ);
        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.UI_2_gNB_GET_OP_LOG_REQ, 0);
        GnbGetLog log = new GnbGetLog(head, type, log_name);
        MessageTransceiver.build().send(id, log.getMsg());
    }

    /**
     * 写定位设备黑匣子数据
     *
     * @param id     设备ID
     * @param record 一行文本记录，不超过250字节， 格式示例：[time_stamp] operation record
     */
    public void writeDWOpLog(String id, String record) {
        SdkLog.I("controller writeDWOpLog, id = " + id + ", record = " + record);
        setMsgType(id, DWProtocol.UI_2_gNB_WRITE_OP_RECORD);
        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.UI_2_gNB_WRITE_OP_RECORD, 0);
        GnbWriteOpLog msg = new GnbWriteOpLog(head, record);
        MessageTransceiver.build().send(id, msg.getMsg());
    }

    /**
     * 删除定位设备黑匣子数据
     *
     * @param id 设备ID
     */
    public void deleteDWOpLog(String id) {
        SdkLog.I("controller deleteDWOpLog, id = " + id);
        setMsgType(id, DWProtocol.UI_2_gNB_DELETE_OP_LOG_REQ);
        setDWOnlyTypeCmd(id, DWProtocol.UI_2_gNB_DELETE_OP_LOG_REQ);
    }

    /**
     * 配置定位设备工作模式
     *
     * @param id   设备ID
     * @param type 1-单小区, 2-双小区(默认)
     */
    public void setDWDualCell(String id, int type) {
        SdkLog.I("controller setDWDualCell, id = " + id + ", type = " + type);
        setMsgType(id, DWProtocol.OAM_MSG_SET_DUAL_CELL);
        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.OAM_MSG_SET_DUAL_CELL, 0);
        GnbDualCell dual = new GnbDualCell(head, type);
        MessageTransceiver.build().send(id, dual.getMsg());
    }

    /**
     * 测量定位设备GPS时偏
     *
     * @param id      设备ID
     * @param cell_id 小区ID
     * @param swap_rf 通道交换  0: noswap  1: swap
     * @param arfcn   下行频点
     * @param pk      pk
     * @param pa      pa
     */
    public void startDWTdMeasure(String id, int cell_id, int swap_rf, int arfcn, int pk, int pa) {
        SdkLog.I("controller startDWTdMeasure, id = " + id + ", cell_id = " + cell_id + ", swap_rf = " + swap_rf + ",  " +
                ", arfcn = " + arfcn + ", pk/pa = " + pk + "/" + pa);
        setMsgType(id, DWProtocol.OAM_MSG_START_TD_MEASURE);
        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.OAM_MSG_START_TD_MEASURE, 0);
        GnbStartTdMeasure tdMeasure = new GnbStartTdMeasure(head, cell_id, swap_rf, arfcn, pk, pa);
        MessageTransceiver.build().send(id, tdMeasure.getMsg());
    }

    /**
     * 配置定位设备名称
     *
     * @param id       设备ID
     * @param dev_name 设备新名称
     */
    public void setDWDevName(String id, String dev_name) {
        SdkLog.I("controller setDWDevName, id: " + id + ", dev_name = " + dev_name);
        setMsgType(id, DWProtocol.OAM_MSG_SET_SYS_INFO);
        DWHeader header = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.OAM_MSG_SET_SYS_INFO, 0);
        GnbSetSysInfo cfg = new GnbSetSysInfo(header, dev_name);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    /**
     * 获取设备名称及密钥
     *
     * @param id 设备ID
     */
    public void getDWSysInfo(String id) {
        SdkLog.I("controller getDWSysInfo, id = " + id);
        setMsgType(id, DWProtocol.OAM_MSG_GET_SYS_INFO);
        setDWOnlyTypeCmd(id, DWProtocol.OAM_MSG_GET_SYS_INFO);
    }

    /**
     * 配置定位设备WIFI名称、密码
     *
     * @param id     设备ID
     * @param ssid   wifi 名称
     * @param passwd wifi 密码
     */
    public void setDWWifiInfo(String id, String ssid, String passwd) {
        SdkLog.I("controller setDWWifiInfo, id = " + id + ", ssid =" + ssid + ", passwd =" + passwd);
        setMsgType(id, DWProtocol.UI_2_gNB_WIFI_CFG);
        DWHeader header = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.UI_2_gNB_WIFI_CFG, 0);
        GnbWifiCfg cfg = new GnbWifiCfg(header, ssid, passwd);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    /**
     * 配置定位设备蓝牙名称
     *
     * @param id   设备ID
     * @param name 蓝牙名称
     */
    public void setDWBtName(String id, String name) {
        SdkLog.I("controller setDWBtName, id = " + id + ", name = " + name);
        setMsgType(id, DWProtocol.OAM_MSG_SET_BT_NAME);
        DWHeader header = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.OAM_MSG_SET_BT_NAME, 0);
        GnbSetBtName cfg = new GnbSetBtName(header, name);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    /**
     * 配置定位设备管理网口信息
     *
     * @param id      设备ID
     * @param ip      设备IP，PC网口接上位机用
     * @param mask    子网掩码
     * @param gateway 网关
     * @param mac     mac地址
     */
    public void setDWMethIp(String id, String ip, String mask, String gateway, String mac) {
        SdkLog.I("controller setDWMethIp, id = " + id + ", ip = " + ip + ", mask = " + mask + ", gateway =" + gateway + ", mac = " + mac);
        setMsgType(id, DWProtocol.OAM_MSG_SET_METH_CFG);
        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.OAM_MSG_SET_METH_CFG, 0);
        GnbMethIpCfg start = new GnbMethIpCfg(head, ip, mask, gateway, mac);
        MessageTransceiver.build().send(id, start.getMsg());
    }

    /**
     * 获取定位设备管理网口信息
     *
     * @param id 设备ID
     */
    public void getDWMethIp(String id) {
        SdkLog.I("controller getDWMethIp, id = " + id);
        setMsgType(id, DWProtocol.OAM_MSG_GET_METH_CFG);
        setDWOnlyTypeCmd(id, DWProtocol.OAM_MSG_GET_METH_CFG);
    }

    /**
     * 配置定位设备FTP服务器
     *
     * @param id              设备ID
     * @param ftp_server      ftp服务器地址
     * @param ftp_path        ftp服务器路径
     * @param ftp_user        用户名
     * @param ftp_passwd      密码
     * @param upload_interval 上传间隔时间  【1 ~ 1440 min】
     */
    public void setDWFtpServer(String id, String ftp_server, String ftp_path, String ftp_user, String ftp_passwd, int upload_interval) {
        SdkLog.I("controller setDWFtpServer, id = " + id + ", ftp_server =" + ftp_server + ", ftp_path =" + ftp_path + ", ftp_user =" + ftp_user + "," +
                " ftp_passwd =" + ftp_passwd + ", upload_interval =" + upload_interval);
        setMsgType(id, DWProtocol.OAM_MSG_SET_FTP_SERVER);
        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.OAM_MSG_SET_FTP_SERVER, 0);
        GnbSetFtpServer start = new GnbSetFtpServer(head, ftp_server, ftp_path, ftp_user, ftp_passwd, upload_interval);
        MessageTransceiver.build().send(id, start.getMsg());
    }

    /**
     * 获取定位设备FTP配置信息
     *
     * @param id 设备ID
     */
    public void getDWFtpInfo(String id) {
        SdkLog.I("controller getDWFtpInfo, id = " + id);
        setMsgType(id, DWProtocol.OAM_MSG_GET_FTP_SERVER);
        setDWOnlyTypeCmd(id, DWProtocol.OAM_MSG_GET_FTP_SERVER);
    }

    /**
     * 配置定位设备 GPS
     *
     * @param id          设备ID
     * @param gnss_select 0-gps+北斗，1-gps， 2-北斗
     * @param latitude    纬度， 实际取值*1000
     * @param longitude   经度， 实际取值*1000
     */
    public void setDWGps(String id, int gnss_select, int latitude, int longitude) {
        SdkLog.I("controller setDWGps, id =" + id + ", gnss_select =" + gnss_select + ", latitude =" + latitude + ", longitude =" + longitude);
        setMsgType(id, DWProtocol.OAM_MSG_SET_GPS_CFG);
        DWHeader header = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.OAM_MSG_SET_GPS_CFG, 0);
        GnbSetGps cfg = new GnbSetGps(header, gnss_select, latitude, longitude);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    /**
     * 定位设备GPS信息查询
     *
     * @param id 设备ID
     */
    public void getDWGpsInfo(String id) {
        SdkLog.I("controller getDWGpsInfo, id = " + id);
        setMsgType(id, DWProtocol.OAM_MSG_GET_GPS_CFG);
        setDWOnlyTypeCmd(id, DWProtocol.OAM_MSG_GET_GPS_CFG);
    }

    /**
     * 配置定位设备GPS输入输出端口
     *
     * @param out_gpio_idx 输出口配置，0-no output, 1-8 means gpo1-gpo8
     * @param in_gpio_idx  输入口配置，0-inner gps, 1-6 means gpi1-gpi6
     */
    public void setDWGpsInOut(String id, int out_gpio_idx, int in_gpio_idx) {
        SdkLog.I("controller setDWGpsInOut, id = " + id + out_gpio_idx + ", in_gpio_idx = " + in_gpio_idx);
        setMsgType(id, DWProtocol.OAM_MSG_SET_GPS_IO_CFG);
        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.OAM_MSG_SET_GPS_IO_CFG, 0);
        GnbSetGpsInOut gpsInOut = new GnbSetGpsInOut(head, out_gpio_idx, in_gpio_idx);
        MessageTransceiver.build().send(id, gpsInOut.getMsg());
    }

    /**
     * 获取定位设备GPS输入输出端口
     *
     * @param id 设备ID
     */
    public void getDWGpsInOut(String id) {
        SdkLog.I("controller getDWGpsInOut, id = " + id);
        setMsgType(id, DWProtocol.OAM_MSG_GET_GPS_IO_CFG);
        setDWOnlyTypeCmd(id, DWProtocol.OAM_MSG_GET_GPS_IO_CFG);
    }

    /**
     * 配置定位设备风扇速率
     *
     * @param id        设备ID
     * @param fan_id    风扇ID 默认 0
     * @param fan_speed 0-100风扇功率增益百分比，当前设置值以及转速在心跳数据中查询
     * @param cmd_param 模式  0自动控制   1手动控制
     */
    public void setDWFanSpeed(String id, int fan_id, int fan_speed, int cmd_param) {
        SdkLog.I("controller setDWFanSpeed, id = " + id + ", fan_id = " + fan_id + ", fan_speed =" + fan_speed);
        setMsgType(id, DWProtocol.OAM_MSG_SET_FAN_SPEED);
        DWHeader header = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.OAM_MSG_SET_FAN_SPEED, cmd_param == 1 ? 1 : 0);
        GnbSetFanSpeed cfg = new GnbSetFanSpeed(header, fan_id, fan_speed);
        MessageTransceiver.build().send(id, cfg.getMsg());

    }

    /**
     * 定位设备风扇自动调速
     *
     * @param id         设备ID
     * @param min_temp   最小温度区间 最多20组
     * @param max_temp   最大温度区间 最多20组
     * @param speed_rate 风扇速度区间 最多20组
     *                   备注：三组区间按下标一一对应
     */
    public void setDWFanAutoSpeed(String id, int[] min_temp, int[] max_temp, int[] speed_rate) {
        SdkLog.I("controller setDWFanAutoSpeed, id = " + id + ", min_temp =" + Arrays.toString(min_temp) + ", max_temp =" + Arrays.toString(max_temp) + ", speed_rate =" + Arrays.toString(speed_rate));
        setMsgType(id, DWProtocol.OAM_MSG_FAN_AUTO_CFG);
        DWHeader header = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.OAM_MSG_FAN_AUTO_CFG, 0);
        GnbSetFanAutoSpeed cfg = new GnbSetFanAutoSpeed(header, min_temp, max_temp, speed_rate);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    /**
     * 配置定位设备接收增益衰减
     *
     * @param id      设备ID
     * @param cell_id 小区ID
     * @param rx_gain 默认0，范围：0~-30db， 0为最大功率
     */
    public void setDWGnbRxGain(String id, int cell_id, int rx_gain) {
        SdkLog.I("controller setDWGnbRxGain, id = " + id + ", cell_id: " + cell_id + ", rx_gain =" + rx_gain);
        setMsgType(id, DWProtocol.OAM_MSG_SET_RX_GAIN);
        DWHeader header = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.OAM_MSG_SET_RX_GAIN, 0);
        GnbSetRxGain cfg = new GnbSetRxGain(header, cell_id, rx_gain);
        MessageTransceiver.build().send(id, cfg.getMsg());

    }

    /**
     * 配置定位设备干扰频点
     *
     * @param id      设备ID
     * @param cell_id 小区ID
     * @param enable  使能干扰  1:enable  0:disable
     * @param arfcn   干扰频点
     */
    public void setDWJamArfcn(String id, int cell_id, int enable, int arfcn) {
        SdkLog.I("controller setDWJamArfcn, id = " + id + ", cell_id: " + cell_id + ", enable = " + enable + ", arfcn = " + arfcn);
        setMsgType(id, DWProtocol.OAM_MSG_SET_JAM_ARFCN);
        DWHeader header = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.OAM_MSG_SET_JAM_ARFCN, 0);
        GnbSetJam cfg = new GnbSetJam(header, cell_id, enable, arfcn);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    /**
     * 设置定位设备转发UDP报文
     *
     * @param id       设备ID
     * @param dst_ip   目标IP
     * @param dst_port 目标端口号
     * @param fwd_info 报文消息内容
     */
    public void setDWForwardUdpMsg(String id, String dst_ip, int dst_port, String fwd_info) {
        SdkLog.I("controller setDWForwardUdpMsg, id = " + id + ", dst_ip = " + dst_ip + ", dst_port = " + dst_port + "fwd_info = " + fwd_info);
        setMsgType(id, DWProtocol.OAM_MSG_FWD_UDP_INFO);
        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.OAM_MSG_FWD_UDP_INFO, 0);
        GnbSetForwardUdpMsg forwardUdpMsg = new GnbSetForwardUdpMsg(head, dst_ip, dst_port, fwd_info);
        MessageTransceiver.build().send(id, forwardUdpMsg.getMsg());
    }

    /**
     * 配置PA GPIO TX RX
     *
     * @param id    设备ID
     * @param tx_rx 0：tx 1：rx  固定8组
     */
    public void setDWGpioTxRx(String id, int[] tx_rx) {
        SdkLog.I("controller setDWGpioTxRx, id = " + id + ", tx_rx = " + Arrays.toString(tx_rx));
        setMsgType(id, DWProtocol.OAM_MSG_CFG_PA_TRX);
        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.OAM_MSG_CFG_PA_TRX, 0);
        GnbSetGpioTxRx cfg = new GnbSetGpioTxRx(head, tx_rx);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    /**
     * 读取用户私有数据
     *
     * @param id    设备ID
     * @param index [0 ～ 9] 允许存10私有数据，下标0到9，对应下标进行读取
     */
    public void getDWUserData(String id, int index) {
        SdkLog.I("controller getDWUserData, id = " + id + ", index = " + index);
        setMsgType(id, DWProtocol.OAM_MSG_RW_USER_DATA);
        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.OAM_MSG_RW_USER_DATA, 0);
        GnbSetUserData cfg = new GnbSetUserData(head, 0, index, "");
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    /**
     * 写入用户私有数据
     *
     * @param id        设备ID
     * @param index     [0 ～ 9] 允许存10私有数据，下标0到9，对应下标进行读取
     * @param user_data [0 ～ 9] 用户数据，最大长度：256字节
     */
    public void setDWUserData(String id, int index, String user_data) {
        SdkLog.I("controller setDWUserData, id = " + id + ", index = " + index + ", user_data = " + user_data);
        setMsgType(id, DWProtocol.OAM_MSG_RW_USER_DATA);
        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.OAM_MSG_RW_USER_DATA, 0);
        GnbSetUserData cfg = new GnbSetUserData(head, 1, index, user_data);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    /**
     * 定位设备重启
     *
     * @param id 设备ID
     */
    public void setDWReboot(String id) {
        SdkLog.I("controller setDWReboot, id = " + id);
        setMsgType(id, DWProtocol.UI_2_gNB_REBOOT_gNB);
        setDWOnlyTypeCmd(id, DWProtocol.UI_2_gNB_REBOOT_gNB);
    }

    /**
     * 定位设备升级
     *
     * @param type:        tftp server: 0; ftp server: 1; serial: 2; scp: 3
     * @param version_name 升级包名
     * @param version_path 升级包路径
     */
    public void setDWGnbUpgrade(String id, int type, String version_name, String version_path) {
        SdkLog.I("controller setDWGnbUpgrade, id = " + id + ", type = " + type + ", version_name =" + version_name + ", version_path =" + version_path);
        setMsgType(id, DWProtocol.UI_2_gNB_VERSION_UPGRADE);
        DWHeader header = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.UI_2_gNB_VERSION_UPGRADE, 0);
        GnbUpgrade cfg = new GnbUpgrade(header, type, version_name, version_path);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    /**
     * 设置授权时间
     *
     * @param id           设备ID
     * @param lic_enable:  0-不生效，1-生效
     * @param expired_day: 结束日期，如 20240101
     * @param credit_max:  可用时长，如 10小时
     */
    public void setLic(String id, int lic_enable, int expired_day, int credit_max, String pwd) {
        SdkLog.I("controller setLic() id = " + id + ", lic_enable = " + lic_enable + ", expired_day = " + expired_day + ", credit_max = " + credit_max + ", pwd = " + pwd);

        if (!pwd.contains(id + lic_enable + expired_day + credit_max + "simpie")) return;
        if (!MD5.a(lic_enable, expired_day, credit_max, pwd)) return;
        setMsgType(id, DWProtocol.OAM_MSG_SET_LIC_INFO);

        DWHeader header = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.OAM_MSG_SET_LIC_INFO, 0);
        GnbCfgLic lic = new GnbCfgLic(header, lic_enable, expired_day, credit_max);
        MessageTransceiver.build().send(id, lic.getMsg());
    }

    public void setLic(int lic_enable, int expired_day, int credit_max, String pwd) {
        setLic(MessageHelper.build().getDeviceId(), lic_enable, expired_day, credit_max, pwd);
    }

    public void setDateTo485(String id, int mod_id, int mod_addr, int cmd_id, int ack, int data_len, String data) {
        SdkLog.I("controller setDateTo485() id = " + id + ", mod_id = " + mod_id + ", mod_addr = " + mod_addr +
                ", cmd_id = " + cmd_id + ", ack = " + ack + ", data_len = " + data_len + ", data = " + data);
        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.OAM_MSG_PA_RW_CMD, 0);
        GnbSetDataTo485 cfg = new GnbSetDataTo485(head, mod_id, mod_addr, cmd_id, ack, data_len, data);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    public void setDWDataFwd(String id, String data) {
        SdkLog.I("controller setDWDataFwd() id = " + id + ", data = " + data);
        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.OAM_MSG_SET_DATA_FWD, data.length() + 20 + 24);
        GnbSetDataFwd cfg = new GnbSetDataFwd(head, data);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    public void setDWDataFwd(String id, byte[] data) {
        SdkLog.I("controller setDWDataFwd() id = " + id + ", data = " + Arrays.toString(data));
        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.OAM_MSG_SET_DATA_FWD, data.length + 20 + 24);
        GnbSetDataFwd cfg = new GnbSetDataFwd(head, data);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    public void setDWDataFwd(String id, char[] data) {
        SdkLog.I("controller setDWDataFwd() id = " + id + ", data = " + Arrays.toString(data));
        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.OAM_MSG_SET_DATA_FWD, data.length + 20 + 24);
        GnbSetDataFwd cfg = new GnbSetDataFwd(head, data);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    /**
     * 查询小区业务配置
     *
     * @param id        设备ID
     * @param cell_id   通道ID
     * @param cmd_param 0或其他业务消息（10/110，11/111，12，13/113，14/114，15/115，30/130，161......）
     */
    public void getDWCellCfg(String id, int cell_id, int cmd_param) {
        SdkLog.I("controller getDWCellCfg() id = " + id + ", cell_id = " + cell_id + ", cmd_param = " + cmd_param);
        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.OAM_MSG_GET_CELL_CFG, cmd_param);
        GnbGetCellCfg cfg = new GnbGetCellCfg(head, cell_id);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    /**
     * 配置IMSI目标重定向至指定频点
     *
     * @param id                 设备ID
     * @param cell_id            通道ID
     * @param is_lte             是否4G业务  true:4G  false:5G
     * @param imsi               IMSI目标
     * @param redirect_flag      重定向类型
     * 5G：
     * 0: do nothing, 1: 5g->4g
     * 4G：
     * 0: do nothing, 1: 4g->4g; 2: 4g->2g
     *
     * @param redirect_arfcn     重定向频点
     */
    public void redirectDWUe(String id, int cell_id, boolean is_lte, String imsi, int redirect_flag, int redirect_arfcn) {
        SdkLog.I("controller redirectDWUe() id = " + id + ", cell_id = " + cell_id + ", is_lte = " + is_lte + ", imsi = " + imsi + ", redirect_flag = " + redirect_flag + ", redirect_arfcn = " + redirect_arfcn);
        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, is_lte ? DWProtocol.UI_2_eNB_REDIRECT_UE : DWProtocol.UI_2_gNB_REDIRECT_UE, 0);
        GnbRedirectUeCfg cfg = new GnbRedirectUeCfg(head, cell_id, imsi, redirect_flag, redirect_arfcn);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    /**
     * 业务中配置修改plmn，若配置则10号消息携带的无视
     *
     * @param id                 设备ID
     * @param cell_id            通道ID
     * @param is_lte             是否4G业务  true:4G  false:5G
     * @param plmns               plmn 数组， 4G最多6个，5G最多12个
     */
    public void resetDWPlmn(String id, int cell_id, boolean is_lte, String[] plmns) {
        SdkLog.I("controller resetDWPlmn() id = " + id + ", cell_id = " + cell_id + ", is_lte = " + is_lte + ", plmns = " + Arrays.toString(plmns));
        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, is_lte ? DWProtocol.UI_2_eNB_SET_PLMN : DWProtocol.UI_2_gNB_SET_PLMN, 0);
        GnbResetPlmnCfg cfg = new GnbResetPlmnCfg(head, cell_id, plmns);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    /**
     * 设置参考功率
     *
     * @param id                 设备ID
     * @param cell_id            通道ID
     * @param pusch_p0           [-126..24]
     */
    public void setDWPefPwr(String id, int cell_id, int pusch_p0) {
        SdkLog.I("controller setDWPefPwr() id = " + id + ", cell_id = " + cell_id + ", pusch_p0 = " + pusch_p0);
        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.OAM_MSG_SET_REF_PWR, 0);
        GnbSetPefPwrCfg cfg = new GnbSetPefPwrCfg(head, cell_id, pusch_p0);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    // TODO: 2023/10/18 增加接口在这里

    /**
     * 配置定位不带参数的指令
     * <p>
     * UI_2_gNB_DELETE_OP_LOG_REQ = 7
     * UI_2_gNB_REBOOT_gNB = 17
     * UI_2_gNB_QUERY_gNB_VERSION = 18
     */
    private void setDWOnlyTypeCmd(String id, int type) {
        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, type, 0);
        GnbOnlyType cmd = new GnbOnlyType(head);
        MessageTransceiver.build().send(id, cmd.getMsg());
    }

    /**
     * 配置定位不带参数的指令
     * <p>
     * UI_2_gNB_STOP_TRACE = 16
     * UI_2_gNB_STOP_LTE_TRACE = 116
     * UI_2_gNB_STOP_CONTROL = 31
     * OAM_MSG_GET_CATCH_CFG  = 220 (in);
     */
    private void setDWCmdAndCellID(String id, int type, int cell_id) {
        if (type == DWProtocol.UI_2_gNB_STOP_TRACE || type == DWProtocol.UI_2_gNB_STOP_LTE_TRACE || type == DWProtocol.UI_2_gNB_STOP_CONTROL || type == DWProtocol.UI_2_eNB_STOP_CONTROL) {
            setEnableChangeTac(id, cell_id, false);
        }
        DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, type, 0);
        GnbOnlyCellID cmd = new GnbOnlyCellID(head, cell_id);
        MessageTransceiver.build().send(id, cmd.getMsg());
    }

    /**
     * DW/DB 心跳，仅SDK内部调用
     *
     * @param dev_type 设备类型
     * @param id       设备ID
     */
    public void sendHeartBeat(String dev_type, String id) {
        if (dev_type.equals("G70") || dev_type.equals("G758")) {
            DWHeader head = new DWHeader(DWProtocol.UI_2_gNB_OAM_MSG, DWProtocol.UI_2_gNB_HEART_BEAT, 0);
            GnbOnlyType cmd = new GnbOnlyType(head);
            MessageTransceiver.build().send(id, cmd.getMsg());
        } else if (dev_type.equals("G10") || dev_type.equals("G581")) {
            MsgGlobal msg = new MsgGlobal(DBProtocol.MsgType.GR_MSG_HELLO);
            MessageTransceiver.build().send(id, msg.getMsg());
        }
    }

    /**
     * 发送AT指令
     *
     * @param cmd cmd指令
     */
    private void sendCmd(String id, String cmd) {
        byte[] sendMsg = cmd.getBytes();
        StringBuilder sd = new StringBuilder();
        for (byte value : sendMsg) {
            String b = Integer.toHexString(value);
            sd.append(b);
            sd.append(" ");
        }
        SdkLog.I("controller sendCmd() id = " + id + ", cmd = " + sd);
        MessageTransceiver.build().send(id, sendMsg);
    }


    /**----------------------DW控制部分结束----------------------**/


    /**----------------------DB控制部分开始----------------------**/

    /**
     * 读取单兵设备版本
     *
     * @param id 设备ID
     */
    public void getDBVersion(String id) {
        SdkLog.I("controller getDBVersion, id = " + id);
        setMsgType(id, DBProtocol.MsgType.GR_MSG_GET_VERSION);
        MsgGetVersion msg = new MsgGetVersion(DBProtocol.MsgType.GR_MSG_GET_VERSION);
        MessageTransceiver.build().send(id, msg.getMsg());
    }

    /**
     * 配置单兵设备系统时间
     *
     * @param id 设备ID
     */
    public void setDBTime(String id) {
        // 时间戳数据格式为"2016-12-08 20:30:30"
        String time = DateUtil.getCurTimeByFormat("yyyy-MM-dd HH:mm:ss");
        SdkLog.I("controller setDBTime, id = " + id + ", time = " + time);
        setMsgType(id, DBProtocol.MsgType.GR_MSG_SET_TIME);
        MsgSetTime msg = new MsgSetTime(DBProtocol.MsgType.GR_MSG_SET_TIME, time);
        MessageTransceiver.build().send(id, msg.getMsg());
    }

    /**
     * 获取单兵设备LOG
     *
     * @param id           设备ID
     * @param server_type: scp: 0
     * @param log_name     输出文件名
     */
    public void getDBLog(String id, int server_type, String log_name) {
        SdkLog.I("controller getDBLog, id = " + id + ", server_type = " + server_type + ", log_name = " + log_name);
        setMsgType(id, DBProtocol.MsgType.GR_MSG_GET_LOG);
        MsgGetLog msg = new MsgGetLog(DBProtocol.MsgType.GR_MSG_GET_LOG, server_type, log_name);
        MessageTransceiver.build().send(id, msg.getMsg());
    }

    /**
     * 配置单兵设备蓝牙名称
     *
     * @param id      设备ID
     * @param bt_name 蓝牙名称
     */
    public void setDBBtName(String id, String bt_name) {
        SdkLog.I("controller setDBBtName, id = " + id + ", bt_name = " + bt_name);
        setMsgType(id, DBProtocol.MsgType.GR_MSG_BT_NAME);
        MsgSetBtName msg = new MsgSetBtName(DBProtocol.MsgType.GR_MSG_BT_NAME, bt_name);
        MessageTransceiver.build().send(id, msg.getMsg());
    }

    /**
     * 配置单兵设备名称
     *
     * @param id       设备ID
     * @param dev_name 设备新名称
     */
    public void setDBDevName(String id, String dev_name) {
        SdkLog.I("controller setDBDevName, id = " + id + ", dev_name = " + dev_name);
        setMsgType(id, DBProtocol.MsgType.BT_MSG_DEV_NAME);
        MsgSetDeviceName msg = new MsgSetDeviceName(DBProtocol.MsgType.BT_MSG_DEV_NAME, dev_name);
        MessageTransceiver.build().send(id, msg.getMsg());
    }

    /**
     * 配置单兵设备WIFI名称密码
     *
     * @param id   设备ID
     * @param ssid WIFI名称
     * @param psw  WIFI密码
     */
    public void setDBWifiInfo(String id, String ssid, String psw) {
        SdkLog.I("controller setDBWifiInfo, id = " + id + ", ssid = " + ssid + ", psw = " + psw);
        setMsgType(id, DBProtocol.MsgType.GR_MSG_WIFI_CFG);
        MsgWifiCfg msg = new MsgWifiCfg(DBProtocol.MsgType.GR_MSG_WIFI_CFG, ssid, psw);
        MessageTransceiver.build().send(id, msg.getMsg());
    }

    /**
     * 单兵设备升级
     *
     * @param id           设备ID
     * @param server_type: scp: 0
     * @param version_name 升级包名
     * @param version_path 升级包路径
     */
    public void setDBUpgrade(String id, int server_type, String version_name, String version_path) {
        SdkLog.I("controller setDBUpgrade, id = " + id + ", version_name = " + version_name + ", version_path = " + version_path);
        setMsgType(id, DBProtocol.MsgType.GR_MSG_IMG_UPGRADE);
        MsgUpgrade msg = new MsgUpgrade(DBProtocol.MsgType.GR_MSG_IMG_UPGRADE, server_type, version_name, version_path);
        MessageTransceiver.build().send(id, msg.getMsg());
    }

    /**
     * 配置单兵设备接收增益
     *
     * @param id      设备ID
     * @param rx_gain 增益值
     */
    public void setDBRxGain(String id, int rx_gain) {
        SdkLog.I("controller setDBRxGain, id = " + id + ", rx_gain = " + rx_gain);
        setMsgType(id, DBProtocol.MsgType.GR_MSG_RX_GAIN);
        MsgRxGain msg = new MsgRxGain(DBProtocol.MsgType.GR_MSG_RX_GAIN, rx_gain);
        MessageTransceiver.build().send(id, msg.getMsg());
    }

    /**
     * 开启单兵设备干扰
     *
     * @param id      设备ID
     * @param save    是否自动
     * @param tx1     tx1配置
     * @param tx2     tx2配置
     * @param orxList 空口列表
     */
    public void startDBJam(String id, int save, TxBean tx1, TxBean tx2, List<OrxBean> orxList) {
        SdkLog.I("controller startDBJam, id = " + id + ", save = " + save + ", tx1 = " + tx1 + ", tx2 = " + tx2 + ", orxList = " + orxList);
        setMsgType(id, DBProtocol.MsgType.GR_MSG_START_JAM);
        MsgStartJam msg = new MsgStartJam(DBProtocol.MsgType.GR_MSG_START_JAM, save, tx1, tx2, orxList);
        MessageTransceiver.build().send(id, msg.getMsg());
    }

    /**
     * 停止单兵设备干扰
     *
     * @param id 设备ID
     */
    public void stopDBJam(String id) {
        SdkLog.I("controller stopDBJam, id = " + id);
        setMsgType(id, DBProtocol.MsgType.GR_MSG_STOP_JAM);
        setDBOnlyTypeCmd(id, DBProtocol.MsgType.GR_MSG_STOP_JAM);
    }

    /**
     * 获取单兵设备干扰配置信息
     *
     * @param id 设备ID
     */
    public void getDBJamCfgInfo(String id) {
        SdkLog.I("controller getDBJamCfgInfo, id = " + id);
        setMsgType(id, DBProtocol.MsgType.GR_MSG_GET_JAM);
        setDBOnlyTypeCmd(id, DBProtocol.MsgType.GR_MSG_GET_JAM);
    }

    /**
     * 开启单兵设备扫描频点最佳UL_OFFSET位置
     *
     * @param id           设备ID
     * @param freq_carrier freq_carrier
     * @param pk           pk
     * @param pa           pa
     */
    public void startDBScan(String id, long freq_carrier, int pk, int pa) {
        SdkLog.I("controller startDBScan, id = " + id + ", freq_carrier = " + freq_carrier + ", pk = " + pk + ", pa = " + pa);
        setMsgType(id, DBProtocol.MsgType.GR_MSG_START_POS_SCAN);
        MsgStartScan msg = new MsgStartScan(DBProtocol.MsgType.GR_MSG_START_POS_SCAN, freq_carrier, pk, pa);
        MessageTransceiver.build().send(id, msg.getMsg());
    }

    /**
     * 开启单兵设备白噪声干扰
     *
     * @param id           设备ID
     * @param tx_chan      unused
     * @param lo_frequency 3450000000
     * @param tx_atten     0-30db
     * @param sg_type      0-WhiteNoise_100MHz, 1-Tone_10MHz
     */
    public void startDBSG(String id, int tx_chan, long lo_frequency, int tx_atten, int sg_type) {
        SdkLog.I("controller startDBSG, id = " + id + ", tx_chan = " + tx_chan + ", lo_frequency = " + lo_frequency + ", tx_atten = " + tx_atten + ", sg_type = " + sg_type);
        setMsgType(id, DBProtocol.MsgType.GR_MSG_START_SG);
        MsgStartSG msg = new MsgStartSG(DBProtocol.MsgType.GR_MSG_START_SG, tx_chan, lo_frequency, tx_atten, sg_type);
        MessageTransceiver.build().send(id, msg.getMsg());
    }

    /**
     * 停止 SG jam
     *
     * @param id 设备ID
     */
    public void stopDBSG(String id) {
        SdkLog.I("controller stopDBSG, id = " + id);
        setMsgType(id, DBProtocol.MsgType.GR_MSG_STOP_SG);
        setDBOnlyTypeCmd(id, DBProtocol.MsgType.GR_MSG_STOP_SG);
    }

    /**
     * 启动单兵
     *
     * @param id                 设备ID
     * @param dl_arfcn           下行频点
     * @param ul_arfcn           上行频点
     * @param pk                 pk
     * @param pa                 pa
     * @param time_offset        时偏
     * @param mode               模式  4: (40ms)   16: (160ms)
     * @param par_cfg            par_cfg
     * @param pci                pci
     * @param ed_ul_rb_offset    上行频域位置 【9 ~ 200】
     * @param slot_index         上行时域位置 【0 ~ 19】
     * @param unlock_check_point 【5 ~ 19】, 默认 15
     * @param slot_index2        上行时域位置2
     * @param smooth_type        平滑类型
     * @param smooth_win_len     smooth_win_len
     * @param prb_num            rb
     * @param doa_num            doa
     * @param frame_type         frame_type
     */
    public void startDBPwrDetect(String id, int dl_arfcn, int ul_arfcn, int pk, int pa, int time_offset, int mode, int par_cfg,
                                 int pci, int ed_ul_rb_offset, int slot_index, int unlock_check_point, int bandwidth,
                                 int slot_index2, int smooth_type, int smooth_win_len, int prb_num, int doa_num, int frame_type, int chan_sel) {
        SdkLog.I("controller startDBPwrDetect, id = " + id + ", dl_arfcn = " + dl_arfcn + ", ul_arfcn = " + ul_arfcn + ", pk = " + pk + ", pa = " + pa
                + ", time_offset = " + time_offset + ", mode = " + mode + ", par_cfg = " + par_cfg + ", pci = " + pci
                + ", ed_ul_rb_offset = " + ed_ul_rb_offset + ", slot_index = " + slot_index + ", unlock_check_point = " + unlock_check_point + ", bandwidth = " + bandwidth +
                ", slot_index2= " + slot_index2 + " ,smooth_type = " + smooth_type + " ,smooth_win_len = " + smooth_win_len + " ,prb_num = " + prb_num + " ,doa_num = " + doa_num
                + " ,frame_type = " + frame_type+ " ,chan_sel = " + chan_sel);
        setMsgType(id, DBProtocol.MsgType.GR_MSG_START_PWR_SCAN);
        MsgStartPwrDetect msg = new MsgStartPwrDetect(DBProtocol.MsgType.GR_MSG_START_PWR_SCAN, dl_arfcn, ul_arfcn, pk, pa, time_offset, ed_ul_rb_offset, slot_index, mode, par_cfg, unlock_check_point, pci,
                bandwidth, slot_index2, smooth_type, smooth_win_len, prb_num, doa_num, frame_type, chan_sel);
        MessageTransceiver.build().send(id, msg.getMsg());
    }
    public void startDBPwrDetect(String id, int dl_arfcn, int ul_arfcn, int pk, int pa, int time_offset, int mode, int par_cfg,
                                 int pci, int ed_ul_rb_offset, int slot_index, int unlock_check_point, int bandwidth,
                                 int slot_index2, int smooth_type, int smooth_win_len, int prb_num, int doa_num, int frame_type){
        startDBPwrDetect(id, dl_arfcn, ul_arfcn, pk, pa, time_offset, mode, par_cfg,
        pci, ed_ul_rb_offset, slot_index, unlock_check_point, bandwidth,
        slot_index2, smooth_type, smooth_win_len, prb_num, doa_num, frame_type, 0);
    }

    /**
     * @param id                 设备ID
     * @param dl_arfcn           下行频点
     * @param pci                pci
     * @param time_offset        时偏
     * @param mode               上报周期
     * @param doa_num            测向个数
     * @param board_type         射频板子类型计算rb_start  1：G73 不需要计算的  2：G758 不需要计算的  3：G73 需要计算的  4：G758 需要计算的
     *                           作用于调整（频点上行频域位置）这个参数，一般情况只需要配 1（开启射频的板子为G73）或4（开启射频的板子为G758），（2，3预留）
     * @param board_bandwidth    射频板子带宽
     */
    public void startDBPwrDetect(String id, int dl_arfcn, int pci, int time_offset, int mode, int doa_num, int board_type, int board_bandwidth, int chan_sel) {
        SdkLog.I("controller received parameters, id = " + id + ", dl_arfcn = " + dl_arfcn + ", pci = " + pci + ", time_offset = " + time_offset
                + ", mode = " + mode + ", doa_num = " + doa_num + ", board_type = " + board_type + ", board_bandwidth = " + board_bandwidth);
        boolean is_lte = dl_arfcn < 100000;

        int band = NrBand.earfcn2band(dl_arfcn);

        int slot_index = 19;
        int frame_type = 9;
        if (is_lte) {
            band = LteBand.earfcn2band(dl_arfcn);
            slot_index = 7;
            if (band >= 33 && band <= 53) frame_type = 2;
        } else {
            switch (band) {
                case 1:
                case 3:
                case 5:
                case 7:
                case 8:
                case 28:
                    slot_index = 9;
                    frame_type = 9;
                    break;
                case 41:
                    frame_type = 8;
                    break;
                case 78:
                case 79:
                    frame_type = 7;
                    break;
            }
        }

        PaPkUlArfcnBean paPkUlArfcn = getPaPkUlArfcn(dl_arfcn, board_bandwidth);
        int Pa = paPkUlArfcn.getPa();
        int Pk = paPkUlArfcn.getPk();
        int UL_NR_ARFCN = paPkUlArfcn.getUl_arfcn();
        int rb_start = 9;
        int value = 9;
        int bandwidth = 5;
        if (!is_lte) {
            switch (band) {
                case 1:
                case 3:
                case 5:
                case 8:
                case 28:
                    //fdd
                    value = ((Pa * 12 + Pk) + 120 - (22 - rb_start) * 12) / 12;
                    break;
                case 79:
                    //79
                    value = ((Pa * 12 + Pk) / 2 + 120 - (10 - rb_start) * 12) / 12;
                    break;
                default: // 其他
                    value = ((Pa * 12 + Pk) / 2 + 120 - (22 - rb_start) * 12) / 12;
                    break;
            }
        }
        switch (board_type) {
            case 1: // G73 不需要计算的
                if (is_lte) rb_start = 6;
                bandwidth = 10;
                break;
            case 2: // G758 不需要计算的
                if (is_lte) rb_start = 5;
                break;
            case 3: // G73 需要计算的
                rb_start = is_lte ? 6 : value;
                bandwidth = 10;
                break;
            case 4: // G758 需要计算的
                rb_start = is_lte ? 5 : value;
                break;
        }

        startDBPwrDetect(id, dl_arfcn, UL_NR_ARFCN, Pk, Pa, time_offset, mode, 400,
                pci, rb_start, slot_index, 19, bandwidth,
                -1, 0, 9, 4, doa_num, frame_type, chan_sel);
    }

    public void startDBPwrDetect(String id, int dl_arfcn, int pci, int time_offset, int mode, int doa_num, int board_type, int board_bandwidth){
        startDBPwrDetect(id, dl_arfcn, pci, time_offset, mode, doa_num, board_type, board_bandwidth, 0);
    }
    /**
     * 启动2/3G业务
     *
     * @param id         设备ID
     * @param type       类型 0:2G  1:3G
     * @param band_id    频段
     * @param dl_arfcn   下行频点
     * @param sched_mode sched_mode 调度周期
     * @param doa_num    doa_num 测向个数，0：单兵  2：测四向  3：测六向
     */
    public void startDBSSDetect(String id, int type, int band_id, int dl_arfcn, int sched_mode, int doa_num) {
        SdkLog.I("controller startDBSSDetect, id = " + id + ", type = " + type + ", band_id = " + band_id + ", dl_arfcn = " + dl_arfcn + ", sched_mode = " + sched_mode + ", doa_num = " + doa_num);
        setMsgType(id, DBProtocol.MsgType.GR_MSG_START_SS_SCAN);
        MsgStartGSMDetect msg = new MsgStartGSMDetect(DBProtocol.MsgType.GR_MSG_START_SS_SCAN, type, band_id, dl_arfcn, 0, sched_mode, doa_num);
        MessageTransceiver.build().send(id, msg.getMsg());
    }
    public void startDBSSDetect(String id, int type, int band_id, int dl_arfcn, int sched_mode) {
        startDBSSDetect(id, type, band_id, dl_arfcn, sched_mode, 0);
    }

    /**
     * 停止单兵
     *
     * @param id 设备ID
     */
    public void stopDBPwrDetect(String id) {
        SdkLog.I("controller stopDBPwrDetect, id = " + id);
        setMsgType(id, DBProtocol.MsgType.GR_MSG_STOP_PWR_SCAN);
        setDBOnlyTypeCmd(id, DBProtocol.MsgType.GR_MSG_STOP_PWR_SCAN);
    }

    /**
     * 配置单兵设备GPIO口
     *
     * @param id     设备ID
     * @param gpio_1 gpio 1
     * @param gpio_2 gpio 2
     * @param gpio_3 gpio 3
     * @param gpio_4 gpio 4
     * @param gpio_5 gpio 5
     * @param gpio_6 gpio 6
     */
    public void setDBGpio(String id, int gpio_1, int gpio_2, int gpio_3, int gpio_4, int gpio_5, int gpio_6, int gpio_7, int gpio_8) {
        SdkLog.I("controller setDBGpio, id = " + id + ", gpio_1 = " + gpio_1 + ", gpio_2 = " + gpio_2 + ", gpio_3 = " + gpio_3
                + ", gpio_4 = " + gpio_4 + ", gpio_5 = " + gpio_5 + ", gpio_6 = " + gpio_6 + ", gpio_7 = " + gpio_7 + ", gpio_8 = " + gpio_8);
        setMsgType(id, DBProtocol.MsgType.GR_MSG_GPIO_CFG);
        MsgGpioCfg msg = new MsgGpioCfg(DBProtocol.MsgType.GR_MSG_GPIO_CFG, gpio_1, gpio_2, gpio_3, gpio_4, gpio_5, gpio_6, gpio_7, gpio_8);
        MessageTransceiver.build().send(id, msg.getMsg());
    }

    /**
     * 重启单兵设备
     *
     * @param id 设备ID
     */
    public void setDBReboot(String id) {
        SdkLog.I("controller setDBReboot, id = " + id);
        setMsgType(id, DBProtocol.MsgType.GR_MSG_REBOOT);
        setDBOnlyTypeCmd(id, DBProtocol.MsgType.GR_MSG_REBOOT);
    }

    /**
     * 配置数据传输
     */
    public void setDataFwd(String id, String data) {
        SdkLog.I("setDataFwd data = " + data);
        MsgSetDataFwd msg = new MsgSetDataFwd(DBProtocol.MsgType.GR_MSG_DATA_FWD, data);
        MessageTransceiver.build().send(id, msg.getMsg());
    }

    public void setDataFwd(String id, char[] data) {
        SdkLog.I("setDataFwd data = " + Arrays.toString(data));
        MsgSetDataFwd msg = new MsgSetDataFwd(DBProtocol.MsgType.GR_MSG_DATA_FWD, data);
        MessageTransceiver.build().send(id, msg.getMsg());
    }

    public void setDataFwd(String id, byte[] data) {
        SdkLog.I("setDataFwd data = " + Arrays.toString(data));
        MsgSetDataFwd msg = new MsgSetDataFwd(DBProtocol.MsgType.GR_MSG_DATA_FWD, data);
        MessageTransceiver.build().send(id, msg.getMsg());
    }

    /**
     * 启动灵敏度测试
     */
    public void startDBSense(String ip, int dl_arfcn, int ul_arfcn, int kssb, int offset2pointA, int time_offset) {
        SdkLog.I("startSense dl_arfcn = " + dl_arfcn + " ,ul_arfcn" + ul_arfcn + " ,kssb" + kssb + " ,offset2pointA" + offset2pointA + " ,time_offset" + time_offset);
        MsgStartSense msg = new MsgStartSense(DBProtocol.MsgType.GR_MSG_START_SENSE_DET, dl_arfcn, ul_arfcn, kssb, offset2pointA, time_offset);
        MessageTransceiver.build().send(ip, msg.getMsg());
    }

    /**
     * 启动灵敏度测试
     */
    public void stopDBSense(String ip) {
        SdkLog.I("stopSense");
        setDBOnlyTypeCmd(ip, DBProtocol.MsgType.GR_MSG_STOP_SENSE_DET);
    }

    /**
     * @param
     * @return
     * @description 设置device_id
     * @author
     * @time
     */

    public void setDeviceId(String id, String deviceId) {
        SdkLog.I("setDeviceId data = " + deviceId);
        MsgSetDeviceId msg = new MsgSetDeviceId(DBProtocol.MsgType.BT_MSG_DEV_SN, deviceId);
        MessageTransceiver.build().send(id, msg.getMsg());
    }

    /**
     * 单兵无参数通用指令
     * GR_MSG_REBOOT
     * GR_MSG_STOP_JAM
     * GR_MSG_STOP_PWR_SCAN
     * GR_MSG_GET_JAM
     */
    private void setDBOnlyTypeCmd(String id, int msg_type) {
        setMsgType(id, msg_type);
        MsgGlobal msg = new MsgGlobal(msg_type);
        MessageTransceiver.build().send(id, msg.getMsg());
    }


    /**----------------------DB控制部分结束----------------------**/

    /**
     * 响应类
     */
    public class MessageObserver {

        /**----------------------DW部分开始----------------------**/

        /**
         * 定位报值及侦码数据
         */
        public void onDWHeartStateRsp(String id, GnbStateRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWHeartStateRsp(id, msg);
        }

        /**
         * 基带版本查询
         */
        public void onDWQueryVersionRsp(String id, GnbVersionRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWQueryVersionRsp(id, msg);
        }

        /**
         * 查询侦码配置
         */
        public void onDWGetCatchCfgRsp(String id, GnbCatchCfgRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWGetCatchCfg(id, msg);
        }

        /**
         * 定位报值及侦码数据
         */
        public void onDWStartTraceRsp(String id, GnbTraceRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWStartTraceRsp(id, msg);
        }

        /**
         * 结束定位
         */
        public void onDWStopTraceRsp(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWStopTraceRsp(id, msg);
        }


        /**
         * 开始管控
         */
        public void onDWStartControlRsp(String id, GnbTraceRsp msg) {
            if (mDWBusinessListener != null) {
                mDWBusinessListener.onDWStartControlRsp(id, msg);
            }
        }

        /**
         * 结束管控
         */
        public void onDWStopControlRsp(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null) {
                mDWBusinessListener.onDWStopControlRsp(id, msg);
            }
        }

        /**
         * 开始侦码
         */
        public void onDWStartCatchRsp(String id, GnbTraceRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWStartCatchRsp(id, msg);
        }

        /**
         * 结束侦码
         */
        public void onDWStopCatchRsp(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWStopCatchRsp(id, msg);
        }

        /**
         * 黑名单配置
         */
        public void onDWSetBlackListRsp(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWSetBlackListRsp(id, msg);
        }

        /**
         * 定位参数配置
         */
        public void onDWSetGnbRsp(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWSetGnbRsp(id, msg);
        }

        /**
         * 配置PA控制IO口
         */
        public void onDWSetPaGpioRsp(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWSetPaGpioRsp(id, msg);
        }

        /**
         * 配置PA控制IO口
         */
        public void onDWGetPaGpioRsp(String id, GnbGpioRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWGetPaGpioRsp(id, msg);
        }

        /**
         * 发送功率衰减
         */
        public void onDWSetTxPwrOffsetRsp(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWSetTxPwrOffsetRsp(id, msg);
        }

        /**
         * 发送功率衰减校验证
         */
        public void onDWSetNvTxPwrOffsetRsp(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null) {
                mDWBusinessListener.onDWSetNvTxPwrOffsetRsp(id, msg);
            }
        }

        /**
         * WIFI信息配置
         */
        public void onDWSetWifiInfoRsp(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWSetWifiInfoRsp(id, msg);
        }

        /**
         * 蓝牙名称
         */
        public void onDWSetBtNameRsp(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWSetBtNameRsp(id, msg);
        }

        /**
         * 系统时间
         */
        public void onDWSetTimeRsp(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWSetTimeRsp(id, msg);
        }

        /**
         * 重启基带
         */
        public void onDWRebootRsp(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWRebootRsp(id, msg);
        }

        /**
         * 升级基带
         */
        public void onDWUpgradeRsp(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWUpgradeRsp(id, msg);
        }

        /**
         * 取基带LOG
         */
        public void onDWGetLogRsp(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWGetLogRsp(id, msg);
        }

        /**
         * 取黑匣子文件
         */
        public void onDWGetOpLogRsp(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWGetOpLogRsp(id, msg);
        }

        /**
         * 写黑匣子文件
         */
        public void onDWWriteOpLogRsp(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWWriteOpLogRsp(id, msg);
        }

        /**
         * 删除黑匣子文件
         */
        public void onDWDeleteOpLogRsp(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null) {
                mDWBusinessListener.onDWDeleteOpLogRsp(id, msg);
            }
        }

        /**
         * IP AND PORT 配置回调
         */
        public void onDWSetMethIpRsp(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWSetMethIpRsp(id, msg);
        }

        /**
         * 读取IP
         */
        public void onDWGetMethIpRsp(String id, GnbMethIpRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWGetMethIpRsp(id, msg);
        }

        /**
         * 配置FTP SERVER回调
         */
        public void onDWSetFtpRsp(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWSetFtpRsp(id, msg);
        }

        /**
         * 读取FTP信息
         */
        public void onDWGetFtpRsp(String id, GnbFtpRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWGetFtpRsp(id, msg);
        }

        /**
         * 设置设备名称回调
         */
        public void onDWSetDevNameRsp(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWSetDevNameRsp(id, msg);
        }

        /**
         * 读取设备信息
         */
        public void onDWGetSysInfoRsp(String id, GnbGetSysInfoRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWGetSysInfoRsp(id, msg);
        }

        /**
         * 单板工作模式配置
         */
        public void onDWSetDualCellRsp(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWSetDualCellRsp(id, msg);
        }

        /**
         * 配置配置接收增益衰减
         */
        public void onDWSetRxGainRsp(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWSetRxGainRsp(id, msg);
        }

        /**
         * 配置Gps
         */
        public void onDWSetGpsRsp(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWSetGpsRsp(id, msg);
        }

        /**
         * 获取系统log
         */
        public void onDWGetSysLogRsp(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWGetSysLogRsp(id, msg);
        }

        /**
         * 配置风扇速率
         */
        public void onDWSetFanSpeedRsp(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWSetFanSpeedRsp(id, msg);
        }

        /**
         * 自动配置风扇速率
         */
        public void onDWSetFanAutoSpeedRsp(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWSetFanAutoSpeedRsp(id, msg);
        }

        /**
         * 配置增强频点
         */
        public void onDWSetJamArfcn(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWSetJamArfcn(id, msg);
        }


        /**
         * 扫频上报
         */
        public void onDWFreqScanRsp(String id, GnbFreqScanRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWFreqScanRsp(id, msg);
        }

        /**
         * 扫频上报，Document模式
         */
        public void onDWFreqScanGetDocumentRsp(String id, GnbFreqScanGetDocumentRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWFreqScanGetDocumentRsp(id, msg);
        }

        /**
         * 停止扫频
         */
        public void onDWStopFreqScanRsp(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWStopFreqScanRsp(id, msg);
        }

        /**
         * GPS时偏测量
         */
        public void onDWStartTdMeasure(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWStartTdMeasure(id, msg);
        }

        /**
         * 配置GPS输入输出端口结果回调
         */
        public void onDWSetGpsInOut(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWSetGpsInOut(id, msg);
        }

        /**
         * 读取GPS输入输出端口
         */
        public void onDWGetGpsInOut(String id, GnbGpsInOutRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWGetGpsInOut(id, msg);
        }

        /**
         * GPS信息查询
         */
        public void onDWGetGpsRsp(String id, GnbGpsRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWGetGpsRsp(id, msg);
        }

        /**
         * 设置转发UDP报文
         */
        public void onDWSetForwardUdpMsg(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWSetForwardUdpMsg(id, msg);
        }

        /**
         * 开始频段扫频
         */
        public void onDWStartBandScan(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWStartBandScan(id, msg);
        }

        /**
         * 写入用户私有数据
         */
        public void onDWSetUserData(String id, GnbUserDataRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWSetUserData(id, msg);
        }

        /**
         * 读取用户私有数据
         */
        public void onDWGetUserData(String id, GnbUserDataRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWGetUserData(id, msg);
        }

        /**
         * 配置PA GPIO TX RX
         */
        public void onDWSetGpioTxRx(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWSetGpioTxRx(id, msg);
        }

        public void onDWSetDataTo485(String id, GnbSetDataTo485Rsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWSetDataTo485(id, msg);
        }

        public void onDWSetDataFwd(String id, GnbReadDataFwdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWSetDataFwd(id, msg);
        }

        public void onDWGetCellCfg(String id, JSONObject msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWGetCellCfg(id, msg);
        }

        public void onDWRedirectUeCfg(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWRedirectUeCfg(id, msg);
        }

        public void onDWResetPlmnCfg(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWResetPlmnCfg(id, msg);
        }
        public void onDWSetPerPwrCfg(String id, GnbCmdRsp msg) {
            if (mDWBusinessListener != null)
                mDWBusinessListener.onDWSetPerPwrCfg(id, msg);
        }

        /**----------------------DW部分结束----------------------**/

        /**----------------------DB部分开始----------------------**/

        /**
         * 设备版本信息
         */
        public void onDBHeartStateRsp(String id, MsgStateRsp msg) {
            if (mDBBusinessListener != null)
                mDBBusinessListener.onDBHeartStateRsp(id, msg);
        }

        /**
         * 设备时间
         */
        public void onDBSetTimeRsp(String id, MsgStateRsp msg) {
            if (mDBBusinessListener != null)
                mDBBusinessListener.onDBSetTimeRsp(id, msg);
        }

        /**
         * 配置蓝牙名称
         */
        public void onDBSetBtNameRsp(String id, MsgStateRsp msg) {
            if (mDBBusinessListener != null)
                mDBBusinessListener.onDBSetBtNameRsp(id, msg);
        }

        /**
         * 配置设备名称
         */
        public void onDBSetDevNameRsp(String id, MsgCmdRsp msg) {
            if (mDBBusinessListener != null)
                mDBBusinessListener.onDBSetDevNameRsp(id, msg);
        }

        /**
         * 配置WIFI名称密码
         */
        public void onDBWifiCfgRsp(String id, MsgStateRsp msg) {
            if (mDBBusinessListener != null)
                mDBBusinessListener.onDBWifiCfgRsp(id, msg);
        }

        /**
         * 读取基带LOG
         */
        public void onDBGetLogRsp(String id, MsgStateRsp msg) {
            if (mDBBusinessListener != null)
                mDBBusinessListener.onDBGetLogRsp(id, msg);
        }

        /**
         * 升级基带
         */
        public void onDBUpgradeRsp(String id, MsgStateRsp msg) {
            if (mDBBusinessListener != null)
                mDBBusinessListener.onDBUpgradeRsp(id, msg);
        }

        /**
         * 重启
         */
        public void onDBRebootRsp(String id, MsgStateRsp msg) {
            if (mDBBusinessListener != null)
                mDBBusinessListener.onDBRebootRsp(id, msg);
        }

        /**
         * 启动SG干扰
         */
        public void onDBStartSGRsp(String id, MsgStateRsp msg) {
            if (mDBBusinessListener != null)
                mDBBusinessListener.onDBStartSGRsp(id, msg);
        }

        /**
         * 结束SG干扰
         */
        public void onDBStopSGRsp(String id, MsgStateRsp msg) {
            if (mDBBusinessListener != null)
                mDBBusinessListener.onDBStopSGRsp(id, msg);
        }

        /**
         * 启动干扰
         */
        public void onDBStartJamRsp(String id, MsgStateRsp msg) {
            if (mDBBusinessListener != null)
                mDBBusinessListener.onDBStartJamRsp(id, msg);
        }

        /**
         * 结束干扰
         */
        public void onDBStopJamRsp(String id, MsgStateRsp msg) {
            if (mDBBusinessListener != null)
                mDBBusinessListener.onDBStopJamRsp(id, msg);
        }

        /**
         * 获取自启动干扰参数
         */
        public void onDBGetJamRsp(String id, MsgGetJamRsp msg) {
            if (mDBBusinessListener != null)
                mDBBusinessListener.onDBGetJamRsp(id, msg);
        }

        /**
         * 启动单兵
         */
        public void onDBStartScanRsp(String id, MsgScanRsp msg) {
            if (mDBBusinessListener != null)
                mDBBusinessListener.onDBStartScanRsp(id, msg);
        }

        /**
         * 启动单兵
         */
        public void onDBStartPwrDetectRsp(String id, MsgScanRsp msg) {
            if (mDBBusinessListener != null)
                mDBBusinessListener.onDBStartPwrDetectRsp(id, msg);
        }

        /**
         * 结束单兵
         */
        public void onDBStopPwrDetectRsp(String id, MsgStateRsp msg) {
            if (mDBBusinessListener != null)
                mDBBusinessListener.onDBStopPwrDetectRsp(id, msg);
        }

        /**
         * 设备版本信息
         */
        public void onDBGetVersionRsp(String id, MsgVersionRsp msg) {
            if (mDBBusinessListener != null)
                mDBBusinessListener.onDBGetVersionRsp(id, msg);
        }

        /**
         * 接收增益配置
         */
        public void onDBRxGainCfgRsp(String id, MsgStateRsp msg) {
            if (mDBBusinessListener != null)
                mDBBusinessListener.onDBRxGainCfgRsp(id, msg);
        }

        /**
         * 接收增益配置
         */
        public void onDBSetGpioCfgRsp(String id, MsgStateRsp msg) {
            if (mDBBusinessListener != null)
                mDBBusinessListener.onDBSetGpioCfgRsp(id, msg);
        }

        public void onReadDataFwdRsp(String id, MsgReadDataFwdRsp msg) {
            if (mDBBusinessListener != null)
                mDBBusinessListener.onReadDataFwdRsp(id, msg);
        }

        public void onSetDeviceId(String id, MsgStateRsp msg) {
            if (mDBBusinessListener != null)
                mDBBusinessListener.onSetDeviceId(id, msg);
        }

        public void onStartSenseRsp(String ip, MsgCmdRsp msg) {
            if (mDBBusinessListener != null)
                mDBBusinessListener.onStartSenseRsp(ip, msg);
        }

        public void onStopSenseRsp(String ip, MsgCmdRsp msg) {
            if (mDBBusinessListener != null)
                mDBBusinessListener.onStopSenseRsp(ip, msg);
        }

        /**
         * 灵敏度上报
         */
        public void onSenseReportRsp(String ip, MsgSenseReportRsp msg) {
            if (mDBBusinessListener != null)
                mDBBusinessListener.onSenseReportRsp(ip, msg);
        }

        public void onDBStartSSDetectRsp(String ip, MsgCmdRsp msg) {
            if (mDBBusinessListener != null)
                mDBBusinessListener.onDBStartSSDetectRsp(ip, msg);
        }

        /**----------------------DB部分结束----------------------**/
    }

    public void setDBBusinessListener(DBBusinessListener listener) {
        this.mDBBusinessListener = listener;
    }

    public void setDWBusinessListener(DWBusinessListener listener) {
        this.mDWBusinessListener = listener;
    }

    public void removeDBBusinessListener() {
        this.mDBBusinessListener = null;
    }

    public void removeDWBusinessListener() {
        this.mDWBusinessListener = null;
    }

    private DBBusinessListener mDBBusinessListener;
    private DWBusinessListener mDWBusinessListener;
    private final List<MsgTypeBean> msgTypeList = new ArrayList<>();
    private final List<TracePara> traceList = new ArrayList<>();

}
