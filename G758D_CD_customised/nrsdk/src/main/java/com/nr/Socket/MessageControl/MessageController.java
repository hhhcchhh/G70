package com.nr.Socket.MessageControl;

import com.Logcat.SLog;
import com.nr.Arfcn.Bean.LteBand;
import com.nr.Arfcn.Bean.NrBand;
import com.nr.Gnb.Bean.GnbProtocol;
import com.nr.Gnb.Bean.GnbTimingOffset;
import com.nr.Gnb.Bean.GnbTimingOffset.TimingBean;
import com.nr.Gnb.Bean.Header;
import com.nr.Gnb.Bean.UeidBean;
import com.nr.Gnb.GnbBlackList;
import com.nr.Gnb.GnbCfgGnb;
import com.nr.Gnb.GnbCfgLic;
import com.nr.Gnb.GnbDualCell;
import com.nr.Gnb.GnbGetLog;
import com.nr.Gnb.GnbGpioCfg;
import com.nr.Gnb.GnbGpsOffsetParaCfg;
import com.nr.Gnb.GnbIpPortCfg;
import com.nr.Gnb.GnbMethIpCfg;
import com.nr.Gnb.GnbOnlyCellID;
import com.nr.Gnb.GnbOnlyType;
import com.nr.Gnb.GnbPhoneType;
import com.nr.Gnb.GnbSetBtName;
import com.nr.Gnb.GnbSetDataFwd;
import com.nr.Gnb.GnbSetFanAutoSpeed;
import com.nr.Gnb.GnbSetFanSpeed;
import com.nr.Gnb.GnbSetForwardUdpMsg;
import com.nr.Gnb.GnbSetFuncCfg;
import com.nr.Gnb.GnbSetGpioTxRx;
import com.nr.Gnb.GnbSetGps;
import com.nr.Gnb.GnbSetGpsInOut;
import com.nr.Gnb.GnbSetJam;
import com.nr.Gnb.GnbSetRxGain;
import com.nr.Gnb.GnbSetSysInfo;
import com.nr.Gnb.GnbSetTime;
import com.nr.Gnb.GnbSetTxPwr;
import com.nr.Gnb.GnbSetUserData;
import com.nr.Gnb.GnbStartBandScan;
import com.nr.Gnb.GnbStartCatch;
import com.nr.Gnb.GnbStartControl;
import com.nr.Gnb.GnbStartFreqScan;
import com.nr.Gnb.GnbStartTdMeasure;
import com.nr.Gnb.GnbStartTrace;
import com.nr.Gnb.GnbUpgrade;
import com.nr.Gnb.GnbWifiCfg;
import com.nr.Gnb.GnbWriteOpLog;
import com.nr.Gnb.GndAirSyncParaCfg;
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
import com.nr.Gnb.TFGnbGpioCfg;
import com.nr.Socket.Bean.MsgTypeBean;
import com.nr.Socket.Bean.TracePara;
import com.nr.Util.DateUtil;
import com.nr.Util.MD5;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MessageController {
    private static MessageController instance;
    private boolean isRun;
    public boolean isDoStop = false; // 发现停止业务时，isEnableChangeTac置为false后，心跳来了又将其置为true, 因此加上是否进入停止业务状态标志
    private OnSetGnbListener setGnbListener;
    private OnSetGnbIn setGnbListener_in;
    private final List<MsgTypeBean> msgTypeList = new ArrayList<>();
    private final List<TracePara> traceList = new ArrayList<>();

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
                                    gnbCfg(false, traceList.get(i).getId(), traceList.get(i).isLte(), traceList.get(i).getCellId(), traceList.get(i).getStartTac(),
                                            traceList.get(i).getPlmn(), traceList.get(i).getArfcn(), traceList.get(i).getPci(), traceList.get(i).getUeMaxTxpwr(),
                                            traceList.get(i).getTimingOffset(), traceList.get(i).getWorkMode(), traceList.get(i).getAirSync(), traceList.get(i).getPlmn1(),
                                            traceList.get(i).getUlRbOffset(), traceList.get(i).getCid(), traceList.get(i).getSsbBitmap(), traceList.get(i).getBandWidth(),
                                            traceList.get(i).getCfr(), traceList.get(i).getSwapRf(), traceList.get(i).getRejectCode(), traceList.get(i).getRxLevMin(),
                                            traceList.get(i).getMobRejectCode(), traceList.get(i).getSplitArfcndl(), traceList.get(i).getForceCfg());
                                }
                                traceList.get(i).setTraceTacChangeDelay(tacDelay);
                            }
                        }
                    } catch (IndexOutOfBoundsException e) {
                        SLog.E("MessageController traceThread err: " + e.getMessage());
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

    /**
     * 为业务响应设置回调转换，初始化SDK后，由APP层调用
     *
     * @param transceiver MessageTransceiver
     */
    public void setTransceiver(MessageTransceiver transceiver) {
        MessageObserver observer = new MessageObserver();
        transceiver.setMessageObserver(observer);
    }

    /**
     * 为业务响应设置回调监听，初始化SDK后，由APP层调用
     *
     * @param listener 业务回调监听
     */
    public void setOnSetGnbListener(OnSetGnbListener listener) {
        this.setGnbListener = listener;
    }

    /**
     * 为业务响应设置回调监听，初始化SDK后，由APP层调用
     *
     * @param listener 特殊功能回调监听，内部自使用
     */
    public void setOnSetGnbIn(OnSetGnbIn listener) {
        this.setGnbListener_in = listener;
    }

    /**
     * 为业务响应移除回调监听，应用结束后，由APP层调用
     */
    public void removeOnSetGnbListener() {
        this.setGnbListener = null;
    }
    /**
     * 为业务响应移除回调监听，应用结束后，由APP层调用
     */
    public void removeOnSetGnbIn() {
        this.setGnbListener_in = null;
    }

    public void close() {
        isRun = false;
    }

    public int getTraceType(String id) {
        for (int i = 0; i < msgTypeList.size(); i++) {
            if (msgTypeList.get(i).getId().equals(id)) {
                return msgTypeList.get(i).getTraceType();
            }
        }
        return -1;
    }

    public int getTraceType() {
        return getTraceType(MessageHelper.build().getDeviceId());
    }

    public void setTraceType(String id, int traceType) {
        for (int i = 0; i < msgTypeList.size(); i++) {
            if (msgTypeList.get(i).getId().equals(id)) {
                SLog.I("setTraceType id = " + id + ", traceType = " + traceType);
                msgTypeList.get(i).setTraceType(traceType);
            }
        }
    }

    public void setTraceType(int traceType) {
        setTraceType(MessageHelper.build().getDeviceId(), traceType);
    }

    public void addMsgTypeList(String id, String ip) {
        boolean add = true;
        for (int i = 0; i < msgTypeList.size(); i++) {
            if (msgTypeList.get(i).getId().equals(id)) {
                add = false;
                break;
            }
        }
        if (add) {
            SLog.I("addMsgTypeList id = " + id + ", ip = " + ip);
            msgTypeList.add(new MsgTypeBean(id, ip));
        }
    }

    public void removeMsgTypeList(String id) {
        for (int i = 0; i < msgTypeList.size(); i++) {
            if (msgTypeList.get(i).getId().equals(id)) {
                SLog.I("removeMsgTypeList id = " + id);
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

    public String getIpFromMsgTypeList(String id){
        for (MsgTypeBean bean : msgTypeList) {
            if (bean.getId().equals(id)) return bean.getIp();
        }
        return "";
    }

    public int getMsgType(String id) {
        for (int i = 0; i < msgTypeList.size(); i++) {
            if (msgTypeList.get(i).getId().equals(id)) {
                return msgTypeList.get(i).getMsgType();
            }
        }
        return GnbProtocol.UI_NONE;
    }

    public void setMsgType(String id, int type) {
        for (int i = 0; i < msgTypeList.size(); i++) {
            if (msgTypeList.get(i).getId().equals(id)) {
                msgTypeList.get(i).setMsgType(type);
                break;
            }
        }
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

    /****************************************以下为APP调用接口****************************************/
    /**
     * 配置单板工作模式
     *
     * @param id    设备ID
     * @param type: 1-single cell, 2-dual cell(default)
     */
    public void setDualCell(String id, int type) {
        SLog.I("setDualCell() id = " + id + ", type = " + type);

        setMsgType(id, GnbProtocol.OAM_MSG_SET_DUAL_CELL);

        Header head = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.OAM_MSG_SET_DUAL_CELL, 0);
        GnbDualCell dual = new GnbDualCell(head, type);
        MessageTransceiver.build().send(id, dual.getMsg());
    }

    public void setDualCell(int type) {
        setDualCell(MessageHelper.build().getDeviceId(), type);
    }

    /**
     * 配置基带时间
     *
     * @param id 设备ID
     */
    public void setGnbTime(String id) {
        SLog.I("setGnbTime() id = " + id);
        // 时间戳数据格式为"2016-12-08 20:30:30"
        String time = DateUtil.getCurTimeByFormat("yyyy-MM-dd HH:mm:ss");

        setMsgType(id, GnbProtocol.UI_2_gNB_SET_TIME);

        Header head = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.UI_2_gNB_SET_TIME, 0);
        GnbSetTime enbTime = new GnbSetTime(head, time);
        MessageTransceiver.build().send(id, enbTime.getMsg());
    }

    public void setGnbTime() {
        setGnbTime(MessageHelper.build().getDeviceId());
    }

    /**
     * 写黑匣子数据
     *
     * @param id     设备ID
     * @param record 一行文本记录，不超过250字节: 格式：[time_stamp] operation record
     */
    public void writeOpLog(String id, String record) {
        SLog.I("writeOpLog() id = " + id + ", record = " + record);
        setMsgType(id, GnbProtocol.UI_2_gNB_WRITE_OP_RECORD);

        Header head = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.UI_2_gNB_WRITE_OP_RECORD, 0);
        GnbWriteOpLog msg = new GnbWriteOpLog(head, record);
        MessageTransceiver.build().send(id, msg.getMsg());
    }

    public void writeOpLog(String record) {
        writeOpLog(MessageHelper.build().getDeviceId(), record);
    }

    /**
     * 配置功率衰减，掉电恢复默认
     *
     * @param id              设备ID
     * @param cell_id         通道ID
     * @param arfcn           频点
     * @param tx_power_offset 衰减值
     */
    public void setTxPwrOffset(String id, int cell_id, int arfcn, int tx_power_offset) {
        SLog.I("setTxPwrOffset() id = " + id + ", cell_id = " + cell_id + ", arfcn = " + arfcn + ", tx_p = " + tx_power_offset);
        for (int i = 0; i < traceList.size(); i++) {
            if (traceList.get(i).getId().equals(id) && traceList.get(i).getCellId() == cell_id) {
                traceList.get(i).setTraceTacChangeDelay(5);
                break;
            }
        }
        setMsgType(id, GnbProtocol.UI_2_gNB_SET_TX_POWER_OFFSET);

        Header head = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.UI_2_gNB_SET_TX_POWER_OFFSET, 0);
        GnbSetTxPwr txPwrOffset = new GnbSetTxPwr(head, cell_id, arfcn, tx_power_offset);
        MessageTransceiver.build().send(id, txPwrOffset.getMsg());
    }

    public void setTxPwrOffset(int cell_id, int arfcn, int tx_power_offset) {
        setTxPwrOffset(MessageHelper.build().getDeviceId(), cell_id, arfcn, tx_power_offset);
    }

    /**
     * 配置功率衰减校验，掉电不恢复
     *
     * @param id              设备ID
     * @param cell_id         通道ID
     * @param arfcn           频点
     * @param tx_power_offset 衰减值
     */
    public void setNvTxPwrOffset(String id, int cell_id, int arfcn, int tx_power_offset) {
        SLog.I("setNvTxPwrOffset() id = " + id + ", cell_id = " + cell_id + ", arfcn = " + arfcn + ", tx_p = " + tx_power_offset);
        for (int i = 0; i < traceList.size(); i++) {
            if (traceList.get(i).getId().equals(id) && traceList.get(i).getCellId() == cell_id) {
                traceList.get(i).setTraceTacChangeDelay(5);
                break;
            }
        }
        setMsgType(id, GnbProtocol.OAM_MSG_ADJUST_TX_ATTEN);

        Header head = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.OAM_MSG_ADJUST_TX_ATTEN, 0);
        GnbSetTxPwr txPwrOffset = new GnbSetTxPwr(head, cell_id, arfcn, tx_power_offset);
        MessageTransceiver.build().send(id, txPwrOffset.getMsg());
    }

    public void setNvTxPwrOffset(int cell_id, int arfcn, int tx_power_offset) {
        setNvTxPwrOffset(MessageHelper.build().getDeviceId(), cell_id, arfcn, tx_power_offset);
    }

    /**
     * 启动5G定位
     *
     * @param cell_id    通道ID
     * @param imsi       目标
     * @param target_num 个数， 默认 1 个
     */
    public void startTrace(int cell_id, String imsi, int target_num) {
        startTrace(MessageHelper.build().getDeviceId(), cell_id, imsi, target_num, 0, 0);
    }

    public void startTrace(String id, int cell_id, String imsi, int target_num) {
        startTrace(id, cell_id, imsi, target_num, 0, 0);
    }

    public void startTrace(String id, int cell_id, String imsi, int target_num, int report_phone_type) {
        startTrace(id, cell_id, imsi, target_num, 0, report_phone_type);
    }

    /**
     * 启动5G定位
     *
     * @param id         设备ID
     * @param cell_id    通道ID
     * @param imsi       目标
     * @param target_num 个数， 默认 1 个
     * @param cmd_param  重定向标志， 是否重定向至4G， 0：不重定向  1：重定向至4G
     * @param report_phone_type  是否上报目标手机类型标志， 0：不上报  1：上报
     */
    public void startTrace(String id, int cell_id, String imsi, int target_num, int cmd_param, int report_phone_type) {
        SLog.I("startTrace() id = " + id + ", cell_id = " + cell_id + ", imsi = " + imsi + ", target_num =" + target_num + ", report_phone_type = " + report_phone_type);
        if (imsi == null) {
            SLog.E("startTrace() imsi param error.");
            return;
        }
        if (report_phone_type != 1) report_phone_type = 0;

        setTraceType(id, GnbProtocol.TraceType.TRACE);
        setMsgType(id, GnbProtocol.UI_2_gNB_START_TRACE);

        Header head = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.UI_2_gNB_START_TRACE, cmd_param);
        GnbStartTrace start = new GnbStartTrace(head, cell_id, 1, imsi, report_phone_type);
        MessageTransceiver.build().send(id, start.getMsg());

        setImsi(id, cell_id, imsi);
        setEnableChangeTac(id, cell_id, true);
        isDoStop = false;
    }

    public void startTrace(String id, int cell_id, List<UeidBean> ueid) {
        startTrace(id, cell_id, ueid, 0, 0);
    }

    public void startTrace(String id, int cell_id, List<UeidBean> ueid, int cmd_param, int report_phone_type) {
        StringBuilder ue = new StringBuilder();
        for (UeidBean bean : ueid) {
            ue.append(bean.getImsi()).append(",").append(bean.getGuti()).append(";");
        }
        SLog.I("startTrace() id = " + id + ", cell_id = " + cell_id + ", ue = " + ue + ", report_phone_type = " + report_phone_type);

        if (report_phone_type != 1) report_phone_type = 0;

        setTraceType(id, GnbProtocol.TraceType.TRACE);
        setMsgType(id, GnbProtocol.UI_2_gNB_START_TRACE);

        Header head = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.UI_2_gNB_START_TRACE, cmd_param);
        GnbStartTrace start = new GnbStartTrace(head, cell_id, ueid, report_phone_type);
        MessageTransceiver.build().send(id, start.getMsg());

        if (ueid.size() > 0) setImsi(id, cell_id, ueid.get(0).getImsi());
        setEnableChangeTac(id, cell_id, true);
        isDoStop = false;
    }

    /**
     * 启动4G定位
     *
     * @param id         设备ID
     * @param cell_id    通道ID
     * @param imsi       目标
     * @param target_num 个数， 默认 1 个
     */
    public void startLteTrace(String id, int cell_id, String imsi, int target_num, int report_phone_type) {
        SLog.I("startLteTrace() id = " + id + ", cell_id = " + cell_id + ", imsi = " + imsi + ", target_num =" + target_num + ", report_phone_type = " + report_phone_type);
        if (imsi == null) {
            SLog.E("startTrace() imsi param error.");
            return;
        }
        setTraceType(id, GnbProtocol.TraceType.TRACE);
        setMsgType(id, GnbProtocol.UI_2_gNB_START_LTE_TRACE);

        Header head = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.UI_2_gNB_START_LTE_TRACE, 0);
        GnbStartTrace start = new GnbStartTrace(head, cell_id, target_num, imsi, report_phone_type);
        MessageTransceiver.build().send(id, start.getMsg());

        setImsi(id, cell_id, imsi);
        setEnableChangeTac(id, cell_id, true);
        isDoStop = false;
    }

    public void startLteTrace(String id, int cell_id, String imsi, int target_num) {
        startLteTrace(id, cell_id, imsi, target_num, 0);
    }

    public void startLteTrace(int cell_id, String imsi, int target_num) {
        startLteTrace(MessageHelper.build().getDeviceId(), cell_id, imsi, target_num, 0);
    }

    public void startLteTrace(String id, int cell_id, List<UeidBean> ueid) {
        startLteTrace(id, cell_id, ueid, 0);
    }

    public void startLteTrace(String id, int cell_id, List<UeidBean> ueid, int report_phone_type) {
        StringBuilder ue = new StringBuilder();
        for (UeidBean bean : ueid) {
            ue.append(bean.getImsi()).append(",").append(bean.getGuti()).append(";");
        }
        SLog.I("startTrace() id = " + id + ", cell_id = " + cell_id + ", ue = " + ue + ", report_phone_type = " + report_phone_type);

        if (report_phone_type != 1) report_phone_type = 0;

        setTraceType(id, GnbProtocol.TraceType.TRACE);
        setMsgType(id, GnbProtocol.UI_2_gNB_START_LTE_TRACE);

        Header head = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.UI_2_gNB_START_LTE_TRACE, 0);
        GnbStartTrace start = new GnbStartTrace(head, cell_id, ueid, report_phone_type);
        MessageTransceiver.build().send(id, start.getMsg());

        if (ueid.size() > 0) setImsi(id, cell_id, ueid.get(0).getImsi());
        setEnableChangeTac(id, cell_id, true);
        isDoStop = false;
    }

    /**
     * 开启5G管控
     *
     * @param id   设备ID
     * @param mode 0:管控  1:压制  3:反管控
     */
    public void startControl(String id, int cell_id, int mode) {
        startControl(id, cell_id, mode, 0);
    }

    public void startControl(int cell_id, int mode) {
        startControl(MessageHelper.build().getDeviceId(), cell_id, mode, 0);
    }

    public void startControl(String id, int cell_id, int mode, int report_phone_type) {
        SLog.I("startControl() id = " + id + ", cell_id = " + cell_id + ", mode = " + mode + ", report_phone_type = " + report_phone_type);

        setTraceType(id, GnbProtocol.TraceType.CONTROL);
        setMsgType(id, GnbProtocol.UI_2_gNB_START_CONTROL);

        Header header = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.UI_2_gNB_START_CONTROL, 0);
        GnbStartControl cfg = new GnbStartControl(header, cell_id, mode, report_phone_type);
        MessageTransceiver.build().send(id, cfg.getMsg());

        setEnableChangeTac(id, cell_id, true);
        isDoStop = false;
    }

    /**
     * 开启4G管控
     *
     * @param id   设备ID
     * @param mode 0:管控  1:压制  3:反管控
     */
    public void startLteControl(String id, int cell_id, int mode) {
        startLteControl(id, cell_id, mode, 0);
    }

    public void startLteControl(int cell_id, int mode) {
        startLteControl(MessageHelper.build().getDeviceId(), cell_id, mode, 0);
    }

    public void startLteControl(String id, int cell_id, int mode, int report_phone_type) {
        SLog.I("startLteControl() id = " + id + ", cell_id = " + cell_id + ", mode = " + mode + ", report_phone_type = " + report_phone_type);

        setTraceType(id, GnbProtocol.TraceType.CONTROL);
        setMsgType(id, GnbProtocol.UI_2_eNB_START_CONTROL);

        Header header = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.UI_2_eNB_START_CONTROL, 0);
        // 与风扇使用对象一致，暂时借用GnbSetFanSpeed
        GnbStartControl cfg = new GnbStartControl(header, cell_id, mode, report_phone_type);
        MessageTransceiver.build().send(id, cfg.getMsg());

        setEnableChangeTac(id, cell_id, true);
        isDoStop = false;
    }

    /**
     * 启动5G侦码
     *
     * @param id           设备ID
     * @param cell_id      通道ID
     * @param save_flag    0- no save save for autorun 当等于1时，重启设备后,将按照当前配置自启动围栏功能。等于0表示不保存，维持原配置。
     *                     注意:save_flag=1时，tac_interval不能配置为0
     * @param start_tac    起始TAC
     * @param end_tac      结束TAC
     * @param tac_interval 1、等于0时，上位机必须发10号指令配置频点参数切换TAC,设备被动执行，此SDK已主动完成循环线程，Web端无需循环发10号指令；
     *                     2、大于9时，设备按上位机给出的间隔值自动执行配置TAC自动自增。
     */
    public void startCatch(String id, int cell_id, int save_flag, int start_tac, int end_tac, int tac_interval) {
        SLog.I("startCatch() id = " + id + ", cell_id = " + cell_id + ", start_tac = " + start_tac + ", end_tac = " + end_tac + ", tac_interval = " + tac_interval);

        setTraceType(id, GnbProtocol.TraceType.CATCH);
        setMsgType(id, GnbProtocol.UI_2_gNB_START_CATCH);

        Header head = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.UI_2_gNB_START_CATCH, 0);
        GnbStartCatch start = new GnbStartCatch(head, cell_id, save_flag, start_tac, end_tac, tac_interval);
        for (int i = 0; i < traceList.size(); i++) {
            if (traceList.get(i).getId().equals(id) && traceList.get(i).getCellId() == cell_id) {
                traceList.get(i).setStartTac(start_tac);
                traceList.get(i).setMaxTac(end_tac);
                if (end_tac < start_tac) end_tac = start_tac + 100;
                traceList.get(i).setRunTac(end_tac - start_tac);
                traceList.get(i).setTacInterval(tac_interval);
                break;
            }
        }
        MessageTransceiver.build().send(id, start.getMsg());

        setEnableChangeTac(id, cell_id, tac_interval < 10); // 小于9时，则允许EnableChangeTac为true，之后SDK自动自增TAC下发
        isDoStop = false;
    }

    public void startCatch(int cell_id, int save_flag, int start_tac, int end_tac, int tac_interval) {
        startCatch(MessageHelper.build().getDeviceId(), cell_id, save_flag, start_tac, end_tac, tac_interval);
    }

    /**
     * 结束5G侦码
     *
     * @param id         设备ID
     * @param cell_id    通道ID
     * @param save_flag: 0: 不删除 1：删除自启动配置参数
     */
    public void stopCatch(String id, int cell_id, int save_flag) {
        SLog.I("stopCatch()  id = " + id + ", cell_id = " + cell_id + ", save_flag = " + save_flag);

        setMsgType(id, GnbProtocol.UI_2_gNB_STOP_CATCH);

        Header head = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.UI_2_gNB_STOP_CATCH, save_flag);
        GnbOnlyCellID cmd = new GnbOnlyCellID(head, cell_id);
        MessageTransceiver.build().send(id, cmd.getMsg());

        setEnableChangeTac(id, cell_id, false);
        isDoStop = true;
    }

    public void stopCatch(int cell_id, int save_flag) {
        stopCatch(MessageHelper.build().getDeviceId(), cell_id, save_flag);
    }

    /**
     * 启动4G侦码
     *
     * @param id           设备ID
     * @param cell_id      通道ID
     * @param save_flag    0- no save save for autorun 当等于1时，重启设备后,将按照当前配置自启动围栏功能。等于0表示不保存，维持原配置。
     *                     注意:save_flag=1时，tac_interval不能配置为0
     * @param start_tac    起始TAC
     * @param end_tac      结束TAC
     * @param tac_interval 1、等于0时，上位机必须发10号指令配置频点参数切换TAC,设备被动执行，此SDK已主动完成循环线程，Web端无需循环发10号指令；
     *                     2、大于9时，设备按上位机给出的间隔值自动执行配置TAC自动自增。
     */
    public void startLteCatch(String id, int cell_id, int save_flag, int start_tac, int end_tac, int tac_interval) {
        SLog.I("startLteCatch() id = " + id + ", cell_id = " + cell_id + ", start_tac = " + start_tac + ", end_tac = " + end_tac + ", tac_interval = " + tac_interval);

        setTraceType(id, GnbProtocol.TraceType.CATCH);
        setMsgType(id, GnbProtocol.UI_2_eNB_START_CATCH);

        Header head = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.UI_2_eNB_START_CATCH, 0);
        GnbStartCatch start = new GnbStartCatch(head, cell_id, save_flag, start_tac, end_tac, tac_interval);
        for (int i = 0; i < traceList.size(); i++) {
            if (traceList.get(i).getId().equals(id) && traceList.get(i).getCellId() == cell_id) {
                traceList.get(i).setStartTac(start_tac);
                traceList.get(i).setMaxTac(end_tac);
                if (end_tac < start_tac) end_tac = start_tac + 100;
                traceList.get(i).setRunTac(end_tac - start_tac);
                traceList.get(i).setTacInterval(tac_interval);
                break;
            }
        }
        MessageTransceiver.build().send(id, start.getMsg());

        setEnableChangeTac(id, cell_id, tac_interval < 10); // 小于9时，则允许EnableChangeTac为true，之后SDK自动自增TAC下发
        isDoStop = false;
    }

    public void startLteCatch(int cell_id, int save_flag, int start_tac, int end_tac, int tac_interval) {
        startLteCatch(MessageHelper.build().getDeviceId(), cell_id, save_flag, start_tac, end_tac, tac_interval);
    }

    /**
     * 结束4G侦码
     *
     * @param id         设备ID
     * @param cell_id    通道ID
     * @param save_flag: 0: 不删除 1：删除自启动配置参数
     */
    public void stopLteCatch(String id, int cell_id, int save_flag) {
        SLog.I("stopLteCatch()  id = " + id + ", cell_id = " + cell_id + ", save_flag = " + save_flag);

        setMsgType(id, GnbProtocol.UI_2_eNB_STOP_CATCH);

        Header head = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.UI_2_eNB_STOP_CATCH, save_flag);
        GnbOnlyCellID cmd = new GnbOnlyCellID(head, cell_id);
        MessageTransceiver.build().send(id, cmd.getMsg());

        setEnableChangeTac(id, cell_id, false);
        isDoStop = true;
    }

    public void stopLteCatch(int cell_id, int save_flag) {
        stopLteCatch(MessageHelper.build().getDeviceId(), cell_id, save_flag);
    }

    /**
     * 配置IP AND PORT
     *
     * @param id   设备ID
     * @param type 消息类型 UI_2_gNB_SET_BOARD_IP_PORT OR UI_2_gNB_SET_UI_IP_PORT
     * @param ip   ip
     * @param port port
     */
    public void setIpPort(String id, int type, int ip, int port) {
        SLog.I("setIpPort()  id = " + id + ", type = " + type + ", ip = " + ip + ", port = " + port);

        setMsgType(id, type); // UI_2_gNB_SET_BOARD_IP_PORT OR UI_2_gNB_SET_UI_IP_PORT

        Header head = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, type, 0);
        GnbIpPortCfg start = new GnbIpPortCfg(head, ip, port);
        MessageTransceiver.build().send(id, start.getMsg());
    }

    public void setIpPort(int type, int ip, int port) {
        setIpPort(MessageHelper.build().getDeviceId(), type, ip, port);
    }

    /**
     * GPS时偏获取参数配置
     *
     * @param id             设备ID
     * @param NR_ARFCN       下行频点
     * @param pci            下行PCI
     * @param sync_threshold sync_threshold
     * @param beam_num       beam_num
     * @param ssb_idx        ssb_idx
     */
    public void gpsOffsetParaCfg(String id, int NR_ARFCN, int pci, int sync_threshold, int beam_num, int ssb_idx) {
        SLog.I("gpsOffsetParaCfg()  id = " + id + ", NR_ARFCN = " + NR_ARFCN + ", pci = " + pci + ", sync_threshold = " + sync_threshold + ", beam_num = " + beam_num + ", ssb_idx = " + ssb_idx);

        setMsgType(id, GnbProtocol.UI_2_gNB_GET_GPS_OFFSET);

        Header header = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.UI_2_gNB_GET_GPS_OFFSET, 0);
        // 读表
        int offset2pointA = 0;
        int kssb = 0;
        TimingBean tb = GnbTimingOffset.build().getTimingOffset(NR_ARFCN);
        if (tb != null) {
            offset2pointA = tb.getK2();
            kssb = tb.getK1();
        } else {
            SLog.D("Who are you arfcn = " + NR_ARFCN);
        }

        SLog.I("offset2pointA = " + offset2pointA + ", kssb = " + kssb);

        GnbGpsOffsetParaCfg cfg = new GnbGpsOffsetParaCfg(header, NR_ARFCN, offset2pointA, kssb, pci, beam_num, ssb_idx, sync_threshold);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    public void gpsOffsetParaCfg(int NR_ARFCN, int pci, int sync_threshold, int beam_num, int ssb_idx) {
        gpsOffsetParaCfg(MessageHelper.build().getDeviceId(), NR_ARFCN, pci, sync_threshold, beam_num, ssb_idx);
    }

    /**
     * 空口同步参数配置
     *
     * @param id                 设备ID
     * @param sub_cmd:           1-airsync, 2
     * @param gps_sync_peroid    gps_sync_peroid
     * @param nr_sync_peroid     nr_sync_peroid
     * @param sync_threshold     sync_threshold
     * @param start_frame_index  start_frame_index
     * @param start_slot_index   start_slot_index
     * @param start_symbol_index start_symbol_index
     * @param end_frame_index    end_frame_index
     * @param end_slot_index     end_slot_index
     * @param end_symbol_index   end_symbol_index
     */
    public void airSyncParaCfg(String id, int sub_cmd, int gps_sync_peroid, int nr_sync_peroid, int sync_threshold, int start_frame_index,
                               int start_slot_index, int start_symbol_index, int end_frame_index, int end_slot_index, int end_symbol_index) {

        SLog.I("airSyncParaCfg() id = " + id + ", " + sub_cmd + "," + gps_sync_peroid + ", " + nr_sync_peroid
                + ", " + sync_threshold + ", " + start_frame_index + ", " + start_slot_index + ", " + start_symbol_index
                + ",  " + end_frame_index + ", " + end_slot_index + ", " + end_symbol_index);

        setMsgType(id, GnbProtocol.UI_2_gNB_SET_NWL_PARA);

        Header header = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.UI_2_gNB_SET_NWL_PARA, 0);

        GndAirSyncParaCfg cfg = new GndAirSyncParaCfg(header, sub_cmd, gps_sync_peroid, nr_sync_peroid, sync_threshold,
                start_frame_index, start_slot_index, start_symbol_index, end_frame_index, end_slot_index, end_symbol_index);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    public void airSyncParaCfg(int sub_cmd, int gps_sync_peroid, int nr_sync_peroid, int sync_threshold, int start_frame_index,
                               int start_slot_index, int start_symbol_index, int end_frame_index, int end_slot_index, int end_symbol_index) {
        airSyncParaCfg(MessageHelper.build().getDeviceId(), sub_cmd, gps_sync_peroid, nr_sync_peroid, sync_threshold, start_frame_index,
                start_slot_index, start_symbol_index, end_frame_index, end_slot_index, end_symbol_index);
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
                SLog.I("setUeMaxTxPwr id = " + id + ", cell_id = " + cell_id + ", ueMaxTxPwr" + ueMaxTxPwr);
                traceList.get(i).setUeMaxTxpwr(ueMaxTxPwr);
            }
        }
    }

    public void setUeMaxTxPwr(int cell_id, String ueMaxTxPwr) {
        setUeMaxTxPwr(MessageHelper.build().getDeviceId(), cell_id, ueMaxTxPwr);
    }

    public int getUeMaxTxPwr(String id, int cell_id) {
        for (int i = 0; i < traceList.size(); i++) {
            if (traceList.get(i).getId().equals(id) && traceList.get(i).getCellId() == cell_id) {
                return Integer.parseInt(traceList.get(i).getUeMaxTxpwr());
            }
        }
        return -1;
    }

    public int getUeMaxTxPwr(int cell_id) {
        return getUeMaxTxPwr(MessageHelper.build().getDeviceId(), cell_id);
    }

    /**
     * 定位参数初始化
     *
     * @param id              设备ID
     * @param cell_id         通道ID  0: 通道一  1: 通道二  2: 通道三  3: 通道四
     * @param startTac        起始TAC 【1000 ～ 3000000】
     * @param maxTac          最大TAC
     * @param plmn            做区分国地区、营业厅
     * @param arfcn           下行频点
     * @param pci             pci 5G【0~1007】 4G 【0 ~ 503】
     * @param ue_max_pwr      最大发射功率
     * @param timing_Offset   城市时偏
     * @param work_mode       工作报值间隔 0：160ms；1：40ms
     * @param air_sync        空口使能 1:enable  0:disable
     * @param subPlmn         第二个plmn, 选配
     * @param ul_rb_offset    固定值：9
     * @param cid             大于5位数据
     * @param ssb_bitmap      波束 5G TDD:255  FDD:240   4G 固定 128
     * @param band_width      带宽 100: 100MHz  20: 20MHz  10: 10MHz
     * @param cfr_enable      功率峰均值,是否消峰 1:enable  0:disable  Default：enable
     * @param swap_rf         0: noswap  1: swap
     * @param reject_code     拒绝原因值  Default：15
     * @param rxLevMin        接入电平：【-70~-30】Default：-70
     * @param mob_reject_code 0: disable; other reject code  Default:0; Enable:9
     * @param split_arfcn_dl  下行分裂频点
     * @param force_cfg       是否强制下发建立小区 0:正常（默认） 1：强制建立小区 若为强制建立小区模式，则不关注GPS和空口同步状态，强制下发建立小区
     */
    public void initGnbTrace(String id, int cell_id, int startTac, int maxTac, String plmn, String arfcn, String pci, String ue_max_pwr,
                             int timing_Offset, int work_mode, int air_sync, String subPlmn, int ul_rb_offset, long cid, int ssb_bitmap,
                             int band_width, int cfr_enable, int swap_rf, int reject_code, int rxLevMin, int mob_reject_code,
                             String split_arfcn_dl, int force_cfg) {

        SLog.D("initGnbTrace() id = " + id + ", cell_id = " + cell_id + ", startTac = " + startTac + ", maxTac = " + maxTac + ", plmn = " + plmn
                + ", arfcn = " + arfcn + ", pci = " + pci + ", ue_max_pwr = " + ue_max_pwr + ", timing_Offset = " + timing_Offset + ", work_mode = " + work_mode
                + ", air_sync = " + air_sync + ", subPlmn = " + subPlmn + ", ul_rb_offset = " + ul_rb_offset + ", cid = " + cid + ", ssb_bitmap = " + ssb_bitmap
                + ", band_width = " + band_width + ", cfr_enable = " + cfr_enable + ", swap_rf = " + swap_rf + ", reject_code = " + reject_code + ", rxLevMin = " + rxLevMin
                + ", mob_reject_code = " + mob_reject_code + ", split_arfcn_dl = " + split_arfcn_dl + ", force_cfg = " + force_cfg);

        if (plmn == null || plmn.isEmpty()) {
            SLog.E("initGnbLteTrace() plmn param err, plmn = " + plmn);
            return;
        }
        if (arfcn == null || arfcn.isEmpty()) {
            SLog.E("initGnbLteTrace() arfcn param err, arfcn = " + plmn);
            return;
        }

        if (split_arfcn_dl == null || split_arfcn_dl.isEmpty()) split_arfcn_dl = "0";

        if (cid < 10000) cid += 65535;

        int band = NrBand.earfcn2band(Integer.parseInt(arfcn));
        if (subPlmn.equals("0")) {
            if (band == 28 || band == 41 || band == 79) {
                if (plmn.equals("46000")) {
                    subPlmn = "46015";
                } else {
                    subPlmn = "46000";
                }
            } else if (band == 1 || band == 3 || band == 78) {
                if (plmn.equals("46001")) {
                    subPlmn = "46011";
                } else if (plmn.equals("46011")) {
                    subPlmn = "46001";
                } else {
                    subPlmn = "46001";
                }
            }
        }

        removeTraceListById(id, cell_id); // 删除相同通道的配置信息
        traceList.add(new TracePara(id, false, cell_id, "", plmn, arfcn, pci, ue_max_pwr, startTac, maxTac, timing_Offset, work_mode, air_sync, subPlmn,
                ul_rb_offset, cid, ssb_bitmap, band_width, cfr_enable, swap_rf, reject_code, rxLevMin, 0, mob_reject_code, split_arfcn_dl, force_cfg));

        gnbCfg(true, id, false, cell_id, startTac, plmn, arfcn, pci, ue_max_pwr, timing_Offset, work_mode, air_sync, subPlmn, ul_rb_offset,
                cid, ssb_bitmap, band_width, cfr_enable, swap_rf, reject_code, rxLevMin, mob_reject_code, split_arfcn_dl, force_cfg);
    }

    public void initGnbTrace(String id, int cell_id, int startTac, int maxTac, String plmn, String arfcn, String pci, String ue_max_pwr,
                             int timing_Offset, int work_mode, int air_sync, String subPlmn, int ul_rb_offset, long cid, int ssb_bitmap,
                             int band_width, int cfr_enable, int swap_rf, int reject_code, int rxLevMin, int mob_reject_code,
                             String split_arfcn_dl) {
        initGnbTrace(id, cell_id, startTac, maxTac, plmn, arfcn, pci, ue_max_pwr, timing_Offset, work_mode, air_sync, subPlmn, ul_rb_offset, cid, ssb_bitmap,
                band_width, cfr_enable, swap_rf, reject_code, rxLevMin, mob_reject_code, split_arfcn_dl, 0);
    }

    public void initGnbTrace(int cell_id, int startTac, int maxTac, String plmn, String arfcn, String pci, String ue_max_pwr,
                             int timing_Offset, int work_mode, int air_sync, String subPlmn, int ul_rb_offset, long cid, int ssb_bitmap,
                             int band_width, int cfr_enable, int swap_rf, int reject_code, int rxLevMin, int mob_reject_code,
                             String split_arfcn_dl, int force_cfg) {
        initGnbTrace(MessageHelper.build().getDeviceId(), cell_id, startTac, maxTac, plmn, arfcn, pci, ue_max_pwr,
                timing_Offset, work_mode, air_sync, subPlmn, ul_rb_offset, cid, ssb_bitmap,
                band_width, cfr_enable, swap_rf, reject_code, rxLevMin, mob_reject_code, split_arfcn_dl, force_cfg);
    }

    public void initGnbTrace(int cell_id, int startTac, int maxTac, String plmn, String arfcn, String pci, String ue_max_pwr,
                             int timing_Offset, int work_mode, int air_sync, String subPlmn, int ul_rb_offset, long cid, int ssb_bitmap,
                             int band_width, int cfr_enable, int swap_rf, int reject_code, int rxLevMin, int mob_reject_code,
                             String split_arfcn_dl) {
        initGnbTrace(MessageHelper.build().getDeviceId(), cell_id, startTac, maxTac, plmn, arfcn, pci, ue_max_pwr, timing_Offset, work_mode, air_sync, subPlmn, ul_rb_offset, cid, ssb_bitmap,
                band_width, cfr_enable, swap_rf, reject_code, rxLevMin, mob_reject_code, split_arfcn_dl, 0);
    }

    /**
     * 定位参数初始化
     *
     * @param id              设备ID
     * @param cell_id         通道ID  0: 通道一  1: 通道二  2: 通道三  3: 通道四
     * @param startTac        起始TAC 【1000 ～ 3000000】
     * @param maxTac          最大TAC
     * @param plmn            做区分国地区、营业厅
     * @param arfcn           下行频点
     * @param pci             pci 5G【0~1007】 4G 【0 ~ 503】
     * @param ue_max_pwr      最大发射功率
     * @param timing_Offset   城市时偏
     * @param work_mode       工作报值间隔 0：160ms；1：40ms
     * @param air_sync        空口使能 1:enable  0:disable
     * @param subPlmn         第二个plmn, 选配
     * @param ul_rb_offset    固定值：9
     * @param cid             大于5位数据
     * @param ssb_bitmap      波束 5G TDD:255  FDD:240   4G 固定 128
     * @param band_width      带宽 100: 100MHz  20: 20MHz  10: 10MHz
     * @param cfr_enable      功率峰均值,是否消峰 1:enable  0:disable  Default：enable
     * @param swap_rf         0: noswap  1: swap
     * @param reject_code     拒绝原因值  Default：15
     * @param rxLevMin        接入电平：【-70~-30】Default：-70
     * @param mob_reject_code 0: disable; other reject code  Default:0; Enable:9
     * @param split_arfcn_dl  下行分裂频点
     * @param force_cfg       是否强制下发建立小区 0:正常（默认） 1：强制建立小区 若为强制建立小区模式，则不关注GPS和空口同步状态，强制下发建立小区
     */
    public void initGnbLteTrace(String id, int cell_id, int startTac, int maxTac, String plmn, String arfcn, String pci, String ue_max_pwr,
                                int timing_Offset, int work_mode, int air_sync, String subPlmn, int ul_rb_offset, long cid, int ssb_bitmap,
                                int band_width, int cfr_enable, int swap_rf, int reject_code, int rxLevMin, int mob_reject_code,
                                String split_arfcn_dl, int force_cfg) {
        SLog.D("initGnbLteTrace() id = " + id + ", cell_id = " + cell_id + ", startTac = " + startTac + ", maxTac = " + maxTac + ", plmn = " + plmn
                + ", arfcn = " + arfcn + ", pci = " + pci + ", ue_max_pwr = " + ue_max_pwr + ", timing_Offset = " + timing_Offset + ", work_mode = " + work_mode
                + ", air_sync = " + air_sync + ", subPlmn = " + subPlmn + ", ul_rb_offset = " + ul_rb_offset + ", cid = " + cid + ", ssb_bitmap = " + ssb_bitmap
                + ", band_width = " + band_width + ", cfr_enable = " + cfr_enable + ", swap_rf = " + swap_rf + ", reject_code = " + reject_code + ", rxLevMin = " + rxLevMin
                + ", mob_reject_code = " + mob_reject_code + ", split_arfcn_dl = " + split_arfcn_dl + ", force_cfg = " + force_cfg);

        if (plmn == null || plmn.isEmpty()) {
            SLog.E("initGnbLteTrace() plmn param err, plmn = " + plmn);
            return;
        }

        if (arfcn == null || arfcn.isEmpty()) {
            SLog.E("initGnbLteTrace() arfcn param err, arfcn = " + plmn);
            return;
        }

        if (split_arfcn_dl == null || split_arfcn_dl.isEmpty()) split_arfcn_dl = "0";

        if (cid < 10000) cid += 65535;

        if (subPlmn.equals("0")) {
            if (plmn.equals("46000")) {
                subPlmn = "46015";
            } else if (plmn.equals("46001")) {
                subPlmn = "46011";
            } else if (plmn.equals("46011")) {
                subPlmn = "46001";
            } else {
                subPlmn = "46000";
            }
        }

        removeTraceListById(id, cell_id); // 删除相同通道的配置信息
        traceList.add(new TracePara(id, true, cell_id, "", plmn, arfcn, pci, ue_max_pwr, startTac, maxTac, timing_Offset, work_mode, air_sync, subPlmn,
                ul_rb_offset, cid, ssb_bitmap, band_width, cfr_enable, 0, reject_code, rxLevMin, 0, mob_reject_code, split_arfcn_dl, force_cfg));

        gnbCfg(true, id, true, cell_id, startTac, plmn, arfcn, pci, ue_max_pwr, timing_Offset, work_mode, air_sync, subPlmn, ul_rb_offset,
                cid, ssb_bitmap, band_width, cfr_enable, 0, reject_code, rxLevMin, mob_reject_code, split_arfcn_dl, force_cfg);
    }

    public void initGnbLteTrace(String id, int cell_id, int startTac, int maxTac, String plmn, String arfcn, String pci, String ue_max_pwr,
                                int timing_Offset, int work_mode, int air_sync, String subPlmn, int ul_rb_offset, long cid, int ssb_bitmap,
                                int band_width, int cfr_enable, int swap_rf, int reject_code, int rxLevMin, int mob_reject_code,
                                String split_arfcn_dl) {
        initGnbLteTrace(id, cell_id, startTac, maxTac, plmn, arfcn, pci, ue_max_pwr, timing_Offset, work_mode, air_sync, subPlmn, ul_rb_offset, cid, ssb_bitmap,
                band_width, cfr_enable, swap_rf, reject_code, rxLevMin, mob_reject_code, split_arfcn_dl, 0);
    }

    public void initGnbLteTrace(int cell_id, int startTac, int maxTac, String plmn, String arfcn, String pci, String ue_max_pwr,
                                int timing_Offset, int work_mode, int air_sync, String subPlmn, int ul_rb_offset, long cid, int ssb_bitmap,
                                int band_width, int cfr_enable, int swap_rf, int reject_code, int rxLevMin, int mob_reject_code,
                                String split_arfcn_dl) {
        initGnbLteTrace(MessageHelper.build().getDeviceId(), cell_id, startTac, maxTac, plmn, arfcn, pci, ue_max_pwr, timing_Offset, work_mode, air_sync, subPlmn, ul_rb_offset, cid, ssb_bitmap,
                band_width, cfr_enable, swap_rf, reject_code, rxLevMin, mob_reject_code, split_arfcn_dl, 0);
    }

    public void initGnbLteTrace(int cell_id, int startTac, int maxTac, String plmn, String arfcn, String pci, String ue_max_pwr,
                                int timing_Offset, int work_mode, int air_sync, String subPlmn, int ul_rb_offset, long cid, int ssb_bitmap,
                                int band_width, int cfr_enable, int swap_rf, int reject_code, int rxLevMin, int mob_reject_code,
                                String split_arfcn_dl, int force_cfg) {
        initGnbLteTrace(MessageHelper.build().getDeviceId(), cell_id, startTac, maxTac, plmn, arfcn, pci, ue_max_pwr, timing_Offset, work_mode, air_sync, subPlmn, ul_rb_offset, cid, ssb_bitmap,
                band_width, cfr_enable, swap_rf, reject_code, rxLevMin, mob_reject_code, split_arfcn_dl, force_cfg);
    }

    /**
     * 循环下发配置频点
     */
    private void gnbCfg(boolean first, String id, boolean is_lte, int cell_id, int tac, String plmn, String arfcn, String pci, String ue_max_pwr, int timingOffset,
                        int workMode, int airSync, String plmn1, int ul_rb_offset, long cid, int ssb_bitmap, int bandwidth, int cfr_enable,
                        int swap_rf, int reject_code, int rxLevMin, int mob_reject_code, String split_arfcn_dl, int force_cfg) {
        int MCC = Integer.parseInt(plmn.substring(0, 3));
        int MNC = Integer.parseInt(plmn.substring(3));
        int MCC2 = 0;
        int MNC2 = 0;
        int frame_type = GnbProtocol.FrameType.FRAME_TYPE_TDD_CFG_2D5MS;
        if (!plmn1.equals("0")) {
            MCC2 = Integer.parseInt(plmn1.substring(0, 3));
            MNC2 = Integer.parseInt(plmn1.substring(3));
        } else {
            SLog.D("MCC2 = 0 not avail");
        }
        //tdd DL_NR_ARFCN = UL_NR_ARFCN
        int DL_NR_ARFCN = Integer.parseInt(arfcn);
        int UL_NR_ARFCN = DL_NR_ARFCN;
        int SplitArfcnDl = Integer.parseInt(split_arfcn_dl);
        int SplitArfcnUl = SplitArfcnDl;
        int band = is_lte ? LteBand.earfcn2band(DL_NR_ARFCN) : NrBand.earfcn2band(DL_NR_ARFCN);
        if (is_lte) {
            if (DL_NR_ARFCN <= 9919) {
                frame_type = GnbProtocol.FrameType.FRAME_TYPE_LTE_FDD_CFG;
                UL_NR_ARFCN = DL_NR_ARFCN + 18000;
            } else frame_type = GnbProtocol.FrameType.FRAME_TYPE_CMCC_TDD_CFG_5MS;
        } else {
            //fdd UL_NR_ARFCN != UL_NR_ARFCN
            // DL_ARFCN >= 600000 表示频段 > 3G
            if (DL_NR_ARFCN >= 600000) {
                frame_type = GnbProtocol.FrameType.FRAME_TYPE_TDD_CFG_2D5MS;
            } else {
                //frame_type = GnbProtocol.FrameType.FRAME_TYPE_TDD_CFG_2D5MS;
                frame_type = GnbProtocol.FrameType.FRAME_TYPE_CMCC_TDD_CFG_5MS;
                if (band == 1) {
                    // 434000 - 396000 = 38000
                    // 422000 - 384000 = 38000
                    if (DL_NR_ARFCN == 422890 || DL_NR_ARFCN == 422930) { //422930
                        UL_NR_ARFCN = DL_NR_ARFCN - 38798;
                    } else if (DL_NR_ARFCN == 427010) {
                        UL_NR_ARFCN = 388092;
                    } else if (DL_NR_ARFCN == 428910) { // 重庆
                        UL_NR_ARFCN = 388072;
                    } else {
                        UL_NR_ARFCN = DL_NR_ARFCN - 38000;
                    }
                    frame_type = GnbProtocol.FrameType.FRAME_TYPE_FDD_CFG;

                } else if (band == 28) { // N28A: 703 -- 733(748)
                    // 151600 - 140600 = 11000
                    // 160600 - 149600 = 11000
                    UL_NR_ARFCN = 140720;//154810  152890 154810 154570 152650
                    frame_type = GnbProtocol.FrameType.FRAME_TYPE_FDD_CFG;
                }
            }
        }

        int PCI = Integer.parseInt(pci);
        int TAC = tac;
        int ssb_pwr = Integer.parseInt(ue_max_pwr);

        int offset2pointA = 0;
        int kssb = 0;
        int timing_offset = timingOffset;
        TimingBean tb = GnbTimingOffset.build().getTimingOffset(DL_NR_ARFCN);
        if (tb != null) {
            if (timing_offset == -1) {
                timing_offset = tb.getTimingOffset();
            } else if (band == 1 || band == 3 || band == 78) {
                // 联通电信用配置表里的频偏，避免客户乱配置
                timing_offset = tb.getTimingOffset();
            }
//			if (timing_offset != tb.getTimingOffset()) {
//				SLog.D("APP timing_offset != timing list, Pls CHECK !!! timingOffset = " + timingOffset + ", sdk = " + tb.getTimingOffset());
//			}
        }
        //SLog.D("Who are you arfcn = " + arfcn);
        if (!is_lte) {
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

                case 28:
                    if (DL_NR_ARFCN == 154810) {
                        kssb = 6; //
                        offset2pointA = 23;
                    } else if (DL_NR_ARFCN == 152890) {
                        kssb = 6;
                        offset2pointA = 22;
                    } else if (DL_NR_ARFCN == 156970) {
                        kssb = 10; //
                        offset2pointA = 135;
                    } else if (DL_NR_ARFCN == 152650) {
                        kssb = 6; //
                        offset2pointA = 12;
                    } else if (DL_NR_ARFCN == 154570) {
                        kssb = 2;
                        offset2pointA = 69;
                    } else if (DL_NR_ARFCN == 156490) {
                        kssb = 6;
                        //offset2pointA = 122;
                        offset2pointA = 70;
                    } else {
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
                        if (DL_NR_ARFCN >= 500910 && DL_NR_ARFCN < 531130) {
                            kssb = 0; //427250
                            offset2pointA = 24;
                        } else if (DL_NR_ARFCN >= 531130 && DL_NR_ARFCN <= 535950) {
                            kssb = 22; //427250
                            offset2pointA = 30;
                        }
                    }
                    tb = GnbTimingOffset.build().getTimingOffset(504990);
                    break;

                case 78:
                    if (DL_NR_ARFCN == 633984 || DL_NR_ARFCN == 627264) {
                        kssb = 12;
                        offset2pointA = 24;
                    } else if (DL_NR_ARFCN >= 620544 && DL_NR_ARFCN < 625248) {
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
                    tb = GnbTimingOffset.build().getTimingOffset(627264);
                    timing_offset = tb.getTimingOffset();
                    break;
                case 79:
                    kssb = 14;
                    //原来用254  后面改成214
                    offset2pointA = 230;
                    tb = GnbTimingOffset.build().getTimingOffset(723360);
                    bandwidth = 100;
                    break;
            }
        }

        if (timing_offset == -1) timing_offset = tb == null ? 0 : tb.getTimingOffset();

        int cmdType = is_lte ? GnbProtocol.UI_2_eNB_CFG_gNB : GnbProtocol.UI_2_gNB_CFG_gNB;

        setMsgType(id, cmdType);

        setPk(id, cell_id, kssb);
        setPa(id, cell_id, offset2pointA);
        int split_arfcn_ul = 0;
        int split_cid = 0;
        int split_pci = PCI;
        int split_offset2pointA = 0;
        int split_kssb = 0;
        if (force_cfg != 1) force_cfg = 0;
        Header header = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, cmdType, 0);
        GnbCfgGnb cfg = new GnbCfgGnb(header, id, cell_id, MCC, MNC, DL_NR_ARFCN, UL_NR_ARFCN, PCI, TAC, offset2pointA, kssb,
                ssb_pwr, timing_offset, workMode, airSync, MCC2, MNC2, ul_rb_offset, cid, ssb_bitmap, frame_type, bandwidth,
                cfr_enable, swap_rf, reject_code, rxLevMin, 0, mob_reject_code, split_cid, split_pci, split_offset2pointA,
                split_kssb, SplitArfcnDl, SplitArfcnUl, force_cfg);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }


    /**
     * 配置黑白名单
     *
     * @param id       设备ID
     * @param isLTE    此业务流程是否为4G
     * @param cell_id  通道ID
     * @param imsi_num 本次设置黑名单个数，最多100个【MAX_BLACK_IMSI_NUM】
     * @param ueImsi   黑名单imsi列表
     */
    public void setBlackList(String id, boolean isLTE, int cell_id, int imsi_num, List<UeidBean> ueImsi) {
        SLog.I("setBlackList() id = " + id + ", cell_id = " + cell_id + ", imsi_num = " + imsi_num + ", ueImsi =" + ueImsi);

        int cmdType = isLTE ? GnbProtocol.UI_2_gNB_START_LTE_BLACK_UE_LIST : GnbProtocol.UI_2_gNB_SET_BLACK_UE_LIST;

        setMsgType(id, cmdType);

        Header header = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, cmdType, 0);
        GnbBlackList cfg = new GnbBlackList(header, cell_id, imsi_num, ueImsi);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    public void setBlackList(boolean isLTE, int cell_id, int imsi_num, List<UeidBean> ueImsi) {
        setBlackList(MessageHelper.build().getDeviceId(), isLTE, cell_id, imsi_num, ueImsi);
    }

    /**
     * 配置基带WIFI名称、密码、工作模式
     *
     * @param id     设备ID
     * @param ssid   WIFI 别名
     * @param passwd WIFI 密码
     */
    public void setGnbWifiInfo(String id, String ssid, String passwd) {
        SLog.I("setGnbWifiInfo() id = " + id + ", ssid =" + ssid + ", passwd =" + passwd);

        setMsgType(id, GnbProtocol.UI_2_gNB_WIFI_CFG);

        Header header = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.UI_2_gNB_WIFI_CFG, 0);
        GnbWifiCfg cfg = new GnbWifiCfg(header, ssid, passwd);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    public void setGnbWifiInfo(String ssid, String passwd) {
        setGnbWifiInfo(MessageHelper.build().getDeviceId(), ssid, passwd);
    }

    /**
     * 配置基带蓝牙名称
     *
     * @param id   设备ID
     * @param name 设备蓝牙新名称
     */
    public void setBtName(String id, String name) {
        SLog.I("setBtName() id = " + id + ", name =" + name);

        setMsgType(id, GnbProtocol.OAM_MSG_SET_BT_NAME);

        Header header = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.OAM_MSG_SET_BT_NAME, 0);
        GnbSetBtName cfg = new GnbSetBtName(header, name);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    public void setBtName(String name) {
        setBtName(MessageHelper.build().getDeviceId(), name);
    }

    /**
     * 基带升级
     *
     * @param id           设备ID
     * @param type:        tftp server: 0; ftp server: 1; serial: 2; scp: 3
     * @param version_name 文件名
     * @param version_path 文件路径
     */
    public void setGnbUpgrade(String id, int type, String version_name, String version_path) {
        SLog.I("setGnbUpgrade() id = " + id + ", type = " + type + ", version_name =" + version_name + ", version_path =" + version_path);

        setMsgType(id, GnbProtocol.UI_2_gNB_VERSION_UPGRADE);

        Header header = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.UI_2_gNB_VERSION_UPGRADE, 0);
        GnbUpgrade cfg = new GnbUpgrade(header, type, version_name, version_path);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    public void setGnbUpgrade(int type, String version_name, String version_path) {
        setGnbUpgrade(MessageHelper.build().getDeviceId(), type, version_name, version_path);
    }

    /**
     * 配置不带参数的指令
     * <p>
     * UI_2_gNB_DELETE_OP_LOG_REQ = 7
     * UI_2_gNB_REBOOT_gNB = 17
     * UI_2_gNB_QUERY_gNB_VERSION = 18
     * ...
     */
    public void setOnlyCmd(String id, int type) {
        Header head = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, type, 0);
        GnbOnlyType cmd = new GnbOnlyType(head);
        MessageTransceiver.build().send(id, cmd.getMsg());
    }

    public void setOnlyCmd(int type) {
        setOnlyCmd(MessageHelper.build().getDeviceId(), type);
    }

    /**
     * 配置不带参数的指令
     *
     * @param id      设备ID
     * @param type    消息类型
     *                UI_2_gNB_STOP_TRACE = 16
     *                UI_2_gNB_STOP_LTE_TRACE = 116
     *                UI_2_gNB_STOP_CONTROL = 31
     *                UI_2_eNB_STOP_CONTROL = 131
     *                OAM_MSG_GET_CATCH_CFG  = 220;
     * @param cell_id 通道ID
     */
    public void setCmdAndCellID(String id, int type, int cell_id) {
        SLog.E("setCmdAndCellID() id = " + id + ", cmdType = " + type + ", cell_id = " + cell_id);
        if (type == GnbProtocol.UI_2_gNB_STOP_TRACE || type == GnbProtocol.UI_2_gNB_STOP_LTE_TRACE || type == GnbProtocol.UI_2_gNB_STOP_CONTROL
                || type == GnbProtocol.UI_2_eNB_STOP_CONTROL || type == GnbProtocol.UI_2_gNB_STOP_CATCH || type == GnbProtocol.UI_2_eNB_STOP_CATCH) {
            setEnableChangeTac(id, cell_id, false);
            isDoStop = true;
        }

        Header head = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, type, 0);
        GnbOnlyCellID cmd = new GnbOnlyCellID(head, cell_id);
        MessageTransceiver.build().send(id, cmd.getMsg());
    }

    public void setCmdAndCellID(int type, int cell_id) {
        setCmdAndCellID(MessageHelper.build().getDeviceId(), type, cell_id);
    }

    /**
     * 开始频点扫频
     *
     * @param id           设备ID
     * @param report_level 报告等级: 0:只传大小区频点相关信息  1：以文件的方式保存SIBx信息
     * @param async_enable 空口使能 1:enable  0:disable
     * @param arfcn_num    频点输出数量
     * @param chan_id      频段对应Rx通道，可设置32组值。举例：N41接收连接Rx1，则设置为1；N78接收连接Rx2，则配置为2
     * @param arfcn_list   频点数组，可设置32组值
     * @param time_offset  GPS 时偏，可设置32组值
     */
    public void startFreqScan(String id, int report_level, int async_enable, int arfcn_num, List<Integer> chan_id
            , List<Integer> arfcn_list, List<Integer> time_offset) {
        SLog.I("startFreqScan() id = " + id + ", report_level = " + report_level + ", async_enable = " + async_enable
                + ", arfcn_num = " + arfcn_num + ", chan_id = " + chan_id.toString() + ", arfcn_list = " + arfcn_list.toString()
                + ", time_offset = " + time_offset.toString());

        setMsgType(id, GnbProtocol.OAM_MSG_START_FREQ_SCAN);

        Header head = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.OAM_MSG_START_FREQ_SCAN, 0);
        GnbStartFreqScan cmd = new GnbStartFreqScan(head, report_level, async_enable, arfcn_num, chan_id, arfcn_list, time_offset);
        MessageTransceiver.build().send(id, cmd.getMsg());
        MessageTransceiver.reportLevel = report_level;
    }

    public void startFreqScan(int report_level, int async_enable, int arfcn_num, List<Integer> chan_id
            , List<Integer> arfcn_list, List<Integer> time_offset) {
        startFreqScan(MessageHelper.build().getDeviceId(), report_level, async_enable, arfcn_num, chan_id, arfcn_list, time_offset);
    }

    /**
     * 测量GPS时偏
     *
     * @param id      设备ID
     * @param cell_id 通道ID
     * @param swap_rf 0: noswap 1: swap
     *                需根据频点所在频段与功放接单板通道配置此参数，
     *                举例：
     *                1）N28、N78、N79接单板通道二：swap_rf = 1；
     *                2）N1、N41接单板通道一：swap_rf = 0。
     * @param arfcn   下行频点
     * @param pk      未启用，传0
     * @param pa      未启用，传0
     */
    public void startTdMeasure(String id, int cell_id, int swap_rf, int arfcn, int pk, int pa) {
        SLog.I("onStartTdMeasure() id = " + id + ", cell_id = " + cell_id + ", swap_rf = " + swap_rf + ", arfcn = " + arfcn + ", pk/pa = " + pk + "/" + pa);

        setMsgType(id, GnbProtocol.OAM_MSG_START_TD_MEASURE);

        Header head = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.OAM_MSG_START_TD_MEASURE, 0);
        GnbStartTdMeasure tdMeasure = new GnbStartTdMeasure(head, cell_id, swap_rf, arfcn, pk, pa);
        MessageTransceiver.build().send(id, tdMeasure.getMsg());
    }

    public void startTdMeasure(int cell_id, int swap_rf, int arfcn, int pk, int pa) {
        startTdMeasure(MessageHelper.build().getDeviceId(), cell_id, swap_rf, arfcn, pk, pa);
    }

    /**
     * 配置PA控制IO口
     *
     * @param id 设备ID
     */
    public void setGnbPaGpio(String id) {
        SLog.I("setGnbPaGpio() id = " + id);
        setMsgType(id, GnbProtocol.OAM_MSG_SET_GPIO_MODE);
        Header header = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.OAM_MSG_SET_GPIO_MODE, 0);
        GnbGpioCfg cfg = new GnbGpioCfg(header);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    public void setGnbPaGpio() {
        setGnbPaGpio(MessageHelper.build().getDeviceId());
    }

    /**
     * 配置SYS INFO
     *
     * @param id       设备ID
     * @param dev_name 设备新名称
     */
    public void setGnbSysInfo(String id, String dev_name) {
        SLog.I("setGnbSysInfo() id = " + id + ", dev_name: " + dev_name);

        setMsgType(id, GnbProtocol.OAM_MSG_SET_SYS_INFO);

        Header header = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.OAM_MSG_SET_SYS_INFO, 0);
        GnbSetSysInfo cfg = new GnbSetSysInfo(header, dev_name);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    public void setGnbSysInfo(String dev_name) {
        setGnbSysInfo(MessageHelper.build().getDeviceId(), dev_name);
    }

    /**
     * 配置接收增益衰减
     *
     * @param id      设备ID
     * @param cell_id 通道ID
     * @param rx_gain 接收增益值
     */
    public void setGnbRxGain(String id, int cell_id, int rx_gain) {
        SLog.I("setGnbRxGain() id = " + id + ", cell_id: " + cell_id + ", rx_gain =" + rx_gain);

        setMsgType(id, GnbProtocol.OAM_MSG_SET_RX_GAIN);

        Header header = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.OAM_MSG_SET_RX_GAIN, 0);
        GnbSetRxGain cfg = new GnbSetRxGain(header, cell_id, rx_gain);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    public void setGnbRxGain(int cell_id, int rx_gain) {
        setGnbRxGain(MessageHelper.build().getDeviceId(), cell_id, rx_gain);
    }

    /**
     * 配置gps
     *
     * @param id          设备ID
     * @param gnss_select 0：gps+北斗， 1：gps， 2：北斗
     * @param latitude    纬度
     * @param longitude   经度
     */
    public void setGps(String id, int gnss_select, int latitude, int longitude) {
        SLog.I("setGps() id = " + id + ", gnss_select =" + gnss_select + ", latitude =" + latitude + ", longitude =" + longitude);

        setMsgType(id, GnbProtocol.OAM_MSG_SET_GPS_CFG);

        Header header = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.OAM_MSG_SET_GPS_CFG, 0);
        GnbSetGps cfg = new GnbSetGps(header, gnss_select, latitude, longitude);
        MessageTransceiver.build().send(id, cfg.getMsg());

    }

    public void setGps(int gnss_select, int latitude, int longitude) {
        setGps(MessageHelper.build().getDeviceId(), gnss_select, latitude, longitude);
    }

    /**
     * 配置风扇速率
     *
     * @param id        设备ID
     * @param fan_id    风扇ID 默认0
     * @param fan_speed 风扇功率增益百分比 0 - 100，当前设置值以及转速在心跳数据中查询
     * @param cmd_param 0：为默认自动控制模式，1分钟内将恢复自动控制；  1：为手动控制，自动调速失效
     */
    public void setFanSpeed(String id, int fan_id, int fan_speed, int cmd_param) {
        SLog.I("setFanSpeed() id = " + id + ", fan_id =" + fan_id + ", fan_speed =" + fan_speed);

        setMsgType(id, GnbProtocol.OAM_MSG_SET_FAN_SPEED);

        Header header = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.OAM_MSG_SET_FAN_SPEED, cmd_param == 0 ? 0 : 1);
        GnbSetFanSpeed cfg = new GnbSetFanSpeed(header, fan_id, fan_speed);
        MessageTransceiver.build().send(id, cfg.getMsg());

    }

    public void setFanSpeed(int fan_id, int fan_speed, int cmd_param) {
        setFanSpeed(MessageHelper.build().getDeviceId(), fan_id, fan_speed, cmd_param);
    }

    /**
     * 风扇自动调速
     *
     * @param id         设备ID
     * @param min_temp   [20]  最小温度区间
     * @param max_temp   [20]  最大温度区间
     * @param speed_rate [20]  风扇速度区间
     */
    public void setFanAutoSpeed(String id, int[] min_temp, int[] max_temp, int[] speed_rate) {
        SLog.I("setFanAutoSpeed() id = " + id + ", min_temp =" + Arrays.toString(min_temp) + ", max_temp =" + Arrays.toString(max_temp) + ", speed_rate =" + Arrays.toString(speed_rate));

        setMsgType(id, GnbProtocol.OAM_MSG_FAN_AUTO_CFG);

        Header header = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.OAM_MSG_FAN_AUTO_CFG, 0);
        GnbSetFanAutoSpeed cfg = new GnbSetFanAutoSpeed(header, min_temp, max_temp, speed_rate);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    public void setFanAutoSpeed(int[] min_temp, int[] max_temp, int[] speed_rate) {
        setFanAutoSpeed(MessageHelper.build().getDeviceId(), min_temp, max_temp, speed_rate);
    }

    /**
     * 配置单板IP，网口专用
     *
     * @param id      设备ID
     * @param ip      网口IP
     * @param mask    网口掩码
     * @param gateway 网口网关
     * @param mac     网口MAC地址
     */
    public void setMethIp(String id, String ip, String mask, String gateway, String mac) {
        SLog.I("setMethIp() id = " + id + " ip = " + ip + ", mask = " + mask + ", gateway =" + gateway + ", mac = " + mac);

        setMsgType(id, GnbProtocol.OAM_MSG_SET_METH_CFG);

        Header head = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.OAM_MSG_SET_METH_CFG, 0);
        GnbMethIpCfg cfg = new GnbMethIpCfg(head, ip, mask, gateway, mac);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    public void setMethIp(String ip, String mask, String gateway, String mac) {
        setMethIp(MessageHelper.build().getDeviceId(), ip, mask, gateway, mac);
    }

    /**
     * 获取基带LOG
     *
     * @param id       设备ID
     * @param type:    tftp server: 0; ftp server: 1; serial: 2; scp: 3
     * @param log_name 日志文件名
     */
    public void getLog(String id, int type, String log_name) {
        SLog.I("getLog() id = " + id + " type = " + type + ", log_name = " + log_name);

        setMsgType(id, GnbProtocol.UI_2_gNB_GET_LOG_REQ);

        Header head = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.UI_2_gNB_GET_LOG_REQ, 0);
        GnbGetLog log = new GnbGetLog(head, type, log_name);
        MessageTransceiver.build().send(id, log.getMsg());
    }

    public void getLog(int type, String log_name) {
        getLog(MessageHelper.build().getDeviceId(), type, log_name);
    }

    /**
     * 获取黑匣子数据
     *
     * @param id       设备ID
     * @param type:    tftp server: 0; ftp server: 1; serial: 2; scp: 3
     * @param log_name 日志文件名
     */
    public void getOpLog(String id, int type, String log_name) {
        SLog.I("getOpLog() id = " + id + " type = " + type + ", log_name = " + log_name);

        setMsgType(id, GnbProtocol.UI_2_gNB_GET_OP_LOG_REQ);

        Header head = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.UI_2_gNB_GET_OP_LOG_REQ, 0);
        GnbGetLog log = new GnbGetLog(head, type, log_name);
        MessageTransceiver.build().send(id, log.getMsg());
    }

    public void getOpLog(int type, String log_name) {
        getOpLog(MessageHelper.build().getDeviceId(), type, log_name);
    }

    /**
     * 配置GPS输入输出端口
     *
     * @param id           设备ID
     * @param out_gpio_idx 输出口配置 0-no output, 1-8 means gpo1-gpo8
     * @param in_gpio_idx  输入口配置 0-inner gps, 1-6 means gpi1-gpi6
     */
    public void setGpsInOut(String id, int out_gpio_idx, int in_gpio_idx) {
        SLog.I("setGpsInOut() " + id + " , out_gpio_idx = " + out_gpio_idx + ", in_gpio_idx = " + in_gpio_idx);

        setMsgType(id, GnbProtocol.OAM_MSG_SET_GPS_IO_CFG);

        Header head = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.OAM_MSG_SET_GPS_IO_CFG, 0);
        GnbSetGpsInOut gpsInOut = new GnbSetGpsInOut(head, out_gpio_idx, in_gpio_idx);
        MessageTransceiver.build().send(id, gpsInOut.getMsg());
    }

    public void setGpsInOut(int out_gpio_idx, int in_gpio_idx) {
        setGpsInOut(MessageHelper.build().getDeviceId(), out_gpio_idx, in_gpio_idx);
    }

    /**
     * 读取GPS输入输出端口
     *
     * @param id 设备ID
     */
    public void getGpsInOut(String id) {
        SLog.I("getGpsInOut id = " + id);

        setMsgType(id, GnbProtocol.OAM_MSG_GET_GPS_IO_CFG);

        setOnlyCmd(id, GnbProtocol.OAM_MSG_GET_GPS_IO_CFG);
    }

    public void getGpsInOut() {
        getGpsInOut(MessageHelper.build().getDeviceId());
    }

    /**
     * GPS信息查询
     *
     * @param id 设备ID
     */
    public void getGpsInfo(String id) {
        SLog.I("getGpsInfo id = " + id);

        setMsgType(id, GnbProtocol.OAM_MSG_GET_GPS_CFG);

        setOnlyCmd(id, GnbProtocol.OAM_MSG_GET_GPS_CFG);
    }

    public void getGpsInfo() {
        getGpsInfo(MessageHelper.build().getDeviceId());
    }

    /**
     * 设置转发UDP报文
     *
     * @param id       设备ID
     * @param dst_ip   转发地址IP
     * @param dst_port 转发地址端口
     * @param fwd_info 转发报文
     */
    public void setForwardUdpMsg(String id, String dst_ip, int dst_port, String fwd_info) {
        SLog.I("setForwardUdpMsg() id = " + id + ", dst_ip = " + dst_ip + ", dst_port = " + dst_port + ", fwd_info = " + fwd_info);

        setMsgType(id, GnbProtocol.OAM_MSG_FWD_UDP_INFO);

        Header head = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.OAM_MSG_FWD_UDP_INFO, 0);
        GnbSetForwardUdpMsg forwardUdpMsg = new GnbSetForwardUdpMsg(head, dst_ip, dst_port, fwd_info);
        MessageTransceiver.build().send(id, forwardUdpMsg.getMsg());
    }

    public void setForwardUdpMsg(String dst_ip, int dst_port, String fwd_info) {
        setForwardUdpMsg(MessageHelper.build().getDeviceId(), dst_ip, dst_port, fwd_info);
    }

    /**
     * 配置异步增强频点
     *
     * @param id      设备ID
     * @param cell_id 通道ID
     * @param enable  使能开关 0关闭，1开启
     * @param arfcn   增强频点
     */
    public void setJamArfcn(String id, int cell_id, int enable, int arfcn) {
        SLog.I("setJamArfcn() id = " + id + " ,cell_id: " + cell_id + ", enable =" + enable + ", arfcn =" + arfcn);

        setMsgType(id, GnbProtocol.OAM_MSG_SET_JAM_ARFCN);

        Header header = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.OAM_MSG_SET_JAM_ARFCN, 0);
        GnbSetJam cfg = new GnbSetJam(header, cell_id, enable, arfcn);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    public void setJamArfcn(int cell_id, int enable, int arfcn) {
        setJamArfcn(MessageHelper.build().getDeviceId(), cell_id, enable, arfcn);
    }

    /**
     * 开始频段扫频
     *
     * @param id           设备ID
     * @param report_level 报告等级: 0:只传大小区频点相关信息  1：以文件的方式保存SIBx信息
     * @param async_enable 空口使能 1:enable  0:disable
     * @param chan_id      频段对应Rx通道，可设置32组值。举例：N41接收连接Rx1，则设置为1；N78接收连接Rx2，则配置为2
     * @param band_id      频段
     * @param time_offset  时偏
     */
    public void startBandScan(String id, int report_level, int async_enable, int chan_id, int band_id, int time_offset) {
        SLog.I("startBandScan() id = " + id + " ,report_level = " + report_level + ", async_enable = " + async_enable +
                ", chan_id = " + chan_id + ", band_id = " + band_id + ", time_offset = " + time_offset);

        setMsgType(id, GnbProtocol.OAM_MSG_START_BAND_SCAN);

        Header head = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.OAM_MSG_START_BAND_SCAN, 0);
        GnbStartBandScan bandScan = new GnbStartBandScan(head, report_level, async_enable, chan_id, band_id, time_offset);
        MessageTransceiver.build().send(id, bandScan.getMsg());
    }

    public void startBandScan(int report_level, int async_enable, int chan_id, int band_id, int time_offset) {
        startBandScan(MessageHelper.build().getDeviceId(), report_level, async_enable, chan_id, band_id, time_offset);
    }

    /**
     * 在基带上存储私有数据
     *
     * @param id        设备ID
     * @param index     允许存10私有数据，下标0到9，对应下标进行读写
     * @param user_data 需要保存的数据
     */
    public void setUserData(String id, int index, String user_data) {
        SLog.I("setUserData() id = " + id + ", index = " + index + " , user_data = " + user_data);

        setMsgType(id, GnbProtocol.OAM_MSG_RW_USER_DATA);

        Header header = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.OAM_MSG_RW_USER_DATA, 0);
        GnbSetUserData cfg = new GnbSetUserData(header, 1, index, user_data);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    public void setUserData(int index, String user_data) {
        setUserData(MessageHelper.build().getDeviceId(), index, user_data);
    }

    /**
     * 获取在基带上存储的私有数据
     *
     * @param id    设备ID
     * @param index 允许存10私有数据，下标0到9，对应下标进行读写
     */
    public void getUserData(String id, int index) {
        SLog.I("setUserData() id = " + id + ", index = " + index);

        setMsgType(id, GnbProtocol.OAM_MSG_RW_USER_DATA);

        Header header = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.OAM_MSG_RW_USER_DATA, 0);
        GnbSetUserData cfg = new GnbSetUserData(header, 0, index, "");
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    public void getUserData(int index) {
        getUserData(MessageHelper.build().getDeviceId(), index);
    }

    /**
     * 设置PA GPIO RXTX
     * 1）该命令和207配合决定gpio的输出
     * 2）在配置业务前设置，每次应用启动只需要设置一次，若多次设置则以最后一次为准
     * 3）设置会保存，掉电不丢失
     *
     * @param id   设备ID
     * @param gpio 输出模式 0-tx，1-rx
     */
    public void setGpioTxRx(String id, int[] gpio) {
        SLog.I("setGpioTxRx() id = " + id + ", gpio = " + Arrays.toString(gpio));

        setMsgType(id, GnbProtocol.OAM_MSG_CFG_PA_TRX);

        Header header = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.OAM_MSG_CFG_PA_TRX, 0);
        GnbSetGpioTxRx cfg = new GnbSetGpioTxRx(header, gpio);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    public void setGpioTxRx(int[] gpio) {
        setGpioTxRx(MessageHelper.build().getDeviceId(), gpio);
    }

    /**
     * 配置单板单/双栈模式
     *
     * @param id     设备ID
     * @param stack: 0-single(default)，1-dual
     */
    public void setDualStack(String id, int stack) {
        SLog.I("setDualStack() id = " + id + ", stack = " + stack);

        setMsgType(id, GnbProtocol.OAM_MSG_SET_DUAL_STACK);

        Header header = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.OAM_MSG_SET_DUAL_STACK, 0);
        // 和设置单双小区一样，同用 GnbDualCell
        GnbDualCell dual = new GnbDualCell(header, stack);
        MessageTransceiver.build().send(id, dual.getMsg());
    }

    public void setDualStack(int stack) {
        setDualStack(MessageHelper.build().getDeviceId(), stack);
    }

    /**
     * 配置G758 PA控制IO口
     *
     * @param id 设备ID
     */
    public void setGnbTFPaGpio(String id) {
        SLog.I("setGnbTFPaGpio() id = " + id);

        setMsgType(id, GnbProtocol.OAM_MSG_SET_GPIO_MODE);

        Header header = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.OAM_MSG_SET_GPIO_MODE, 0);
        TFGnbGpioCfg cfg = new TFGnbGpioCfg(header);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    public void setGnbTFPaGpio() {
        setGnbTFPaGpio(MessageHelper.build().getDeviceId());
    }

    /**
     * 配置数据传输
     *
     * @param id   设备ID
     * @param data String类型数据
     */
    public void setDataFwd(String id, String data) {
        SLog.I("setDataFwd() id = " + id + ", data = " + data);

        setMsgType(id, GnbProtocol.OAM_MSG_DATA_FWD);

        Header header = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.OAM_MSG_DATA_FWD, 20 + data.getBytes().length);
        GnbSetDataFwd cfg = new GnbSetDataFwd(header, data);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    public void setDataFwd(String data) {
        setDataFwd(MessageHelper.build().getDeviceId(), data);
    }

    /**
     * 配置数据传输
     *
     * @param id   设备ID
     * @param data char[]类型数据
     */
    public void setDataFwd(String id, char[] data) {
        SLog.I("setDataFwd() id = " + id + ", data = " + Arrays.toString(data));

        setMsgType(id, GnbProtocol.OAM_MSG_DATA_FWD);

        Header header = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.OAM_MSG_DATA_FWD, 20 + data.length * 2);
        GnbSetDataFwd cfg = new GnbSetDataFwd(header, data);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    public void setDataFwd(char[] data) {
        setDataFwd(MessageHelper.build().getDeviceId(), data);
    }

    /**
     * 配置数据传输
     *
     * @param id   设备ID
     * @param data byte[]类型数据
     */
    public void setDataFwd(String id, byte[] data) {
        SLog.I("setDataFwd() id = " + id + ", data = " + Arrays.toString(data));

        setMsgType(id, GnbProtocol.OAM_MSG_DATA_FWD);

        Header header = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.OAM_MSG_DATA_FWD, 20 + data.length);
        GnbSetDataFwd cfg = new GnbSetDataFwd(header, data);
        MessageTransceiver.build().send(id, cfg.getMsg());
    }

    public void setDataFwd(byte[] data) {
        setDataFwd(MessageHelper.build().getDeviceId(), data);
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
        SLog.I("setLic() id = " + id + ", lic_enable = " + lic_enable + ", expired_day = " + expired_day + ", credit_max = " + credit_max + ", pwd = " + pwd);

        if (!pwd.contains(id + lic_enable + expired_day + credit_max + "simpie")) return;
        if (!MD5.a(lic_enable, expired_day, credit_max, pwd)) return;
        setMsgType(id, GnbProtocol.OAM_MSG_SET_LIC_INFO);

        Header header = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.OAM_MSG_SET_LIC_INFO, 0);
        GnbCfgLic lic = new GnbCfgLic(header, lic_enable, expired_day, credit_max);
        MessageTransceiver.build().send(id, lic.getMsg());
    }

    public void setLic(int lic_enable, int expired_day, int credit_max, String pwd) {
        setLic(MessageHelper.build().getDeviceId(), lic_enable, expired_day, credit_max, pwd);
    }

    /**
     * 添加目标到IPHONE库
     *
     * @param id         设备ID
     * @param cell_id    通道ID
     * @param rnti:      rnti
     * @param isLte:     是否为4G业务
     */
    public void setPhoneType(String id, int cell_id, int rnti, boolean isLte) {
        SLog.I("setPhoneType() id = " + id + ", cell_id = " + cell_id + ", rnti = " + rnti);

        setMsgType(id, isLte ? GnbProtocol.UI_2_eNB_SET_PHONE_TYPE : GnbProtocol.UI_2_gNB_SET_PHONE_TYPE);

        Header head = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, isLte ? GnbProtocol.UI_2_eNB_SET_PHONE_TYPE : GnbProtocol.UI_2_gNB_SET_PHONE_TYPE, 0);
        GnbPhoneType cmd = new GnbPhoneType(head, cell_id, rnti);
        MessageTransceiver.build().send(id, cmd.getMsg());
    }

    public void setPhoneType(int cell_id, int rnti, boolean isLte) {
        setPhoneType(MessageHelper.build().getDeviceId(), cell_id, rnti, isLte);
    }

    /**
     * 万能钥匙和大杂烩
     *
     * @param id           设备ID
     * @param func_type    PDT_TYPE
     * @param cfg_value    cfg_value
     * @param cfg_str      cfg_str
     */
    public void setFuncCfg(String id, int func_type, int cfg_value, String cfg_str) {
        SLog.I("setFuncCfg() " + id + " , func_type = " + func_type + ", cfg_value = " + cfg_value + ", cfg_str = " + cfg_str);

        setMsgType(id, GnbProtocol.OAM_MSG_SET_FUNC_CFG);

        Header head = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.OAM_MSG_SET_FUNC_CFG, 0);
        GnbSetFuncCfg funcCfg = new GnbSetFuncCfg(head, func_type, cfg_value, cfg_str);
        MessageTransceiver.build().send(id, funcCfg.getMsg());
    }

    public void setFuncCfg(int func_type, int cfg_value, String cfg_str) {
        setFuncCfg(MessageHelper.build().getDeviceId(), func_type, cfg_value, cfg_str);
    }

    //todo 增加接口在这里添加

    /**
     * 发送AT指令
     *
     * @param cmd 指令
     */
    public void sendCmd(String id, String cmd) {
        byte[] sendMsg = cmd.getBytes();
        StringBuilder sb = new StringBuilder();
        for (byte value : sendMsg) sb.append(Integer.toHexString(value)).append(" ");
        SLog.I("sendCmd(): cmd = " + cmd + "  [ " + sb + "]");
        MessageTransceiver.build().send(id, sendMsg);
    }

    public void sendCmd(String cmd) {
        sendCmd(MessageHelper.build().getDeviceId(), cmd);
    }

    /**
     * 响应类
     */
    public class MessageObserver {
        /**
         * 心跳
         */
        public void onHeartStateRsp(GnbStateRsp msg) {
            if (setGnbListener != null) setGnbListener.onHeartStateRsp(msg);
        }

        /**
         * 基带版本查询
         */
        public void onQueryVersionRsp(String id, GnbVersionRsp msg) {
            if (setGnbListener != null) setGnbListener.onQueryVersionRsp(id, msg);
        }

        /**
         * 开始管控
         */
        public void onStartControlRsp(String id, GnbTraceRsp msg) {
            if (setGnbListener != null) setGnbListener.onStartControlRsp(id, msg);
        }

        /**
         * 结束管控
         */
        public void onStopControlRsp(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onStopControlRsp(id, msg);
        }

        /**
         * 定位报值及侦码数据
         */
        public void onStartTraceRsp(String id, GnbTraceRsp msg) {
            if (setGnbListener != null) setGnbListener.onStartTraceRsp(id, msg);
        }

        /**
         * 4G定位报值及侦码数据
         */
        public void onStartLteTraceRsp(String id, GnbTraceRsp msg) {
            if (setGnbListener != null) setGnbListener.onStartTraceRsp(id, msg);
        }

        /**
         * 结束定位
         */
        public void onStopTraceRsp(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onStopTraceRsp(id, msg);
        }

        /**
         * 结束4G定位
         */
        public void onStopLteTraceRsp(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onStopTraceRsp(id, msg);
        }

        /**
         * 侦码数据
         */
        public void onStartCatchRsp(String id, GnbTraceRsp msg) {
            if (setGnbListener != null) setGnbListener.onStartCatchRsp(id, msg);
        }

        /**
         * 结束侦码
         */
        public void onStopCatchRsp(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onStopCatchRsp(id, msg);
        }

        /**
         * 黑名单配置
         */
        public void onSetBlackListRsp(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onSetBlackListRsp(id, msg);
        }

        /**
         * 参数配置
         */
        public void onSetGnbRsp(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onSetGnbRsp(id, msg);
        }

        /**
         * 发送功率衰减
         */
        public void onSetTxPwrOffsetRsp(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onSetTxPwrOffsetRsp(id, msg);
        }

        /**
         * 发送功率衰减校验证
         */
        public void onSetNvTxPwrOffsetRsp(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onSetNvTxPwrOffsetRsp(id, msg);
        }

        /**
         * 系统时间
         */
        public void onSetTimeRsp(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onSetTimeRsp(id, msg);
        }

        /**
         * WIFI信息配置
         */
        public void onSetWifiInfoRsp(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onSetWifiInfoRsp(id, msg);
        }

        /**
         * 重启基带
         */
        public void onSetRebootRsp(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onSetRebootRsp(id, msg);
        }

        /**
         * 升级基带
         */
        public void onFirmwareUpgradeRsp(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onFirmwareUpgradeRsp(id, msg);
        }

        /**
         * 取基带LOG
         */
        public void onGetLogRsp(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onGetLogRsp(id, msg);
        }

        /**
         * 取黑匣子文件
         */
        public void onGetOpLogRsp(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onGetOpLogRsp(id, msg);
        }

        /**
         * 写黑匣子文件
         */
        public void onWriteOpLogRsp(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onWriteOpLogRsp(id, msg);
        }

        /**
         * 删除黑匣子文件
         */
        public void onDeleteOpLogRsp(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onDeleteOpLogRsp(id, msg);
        }

        /**
         * 蓝牙名称
         */
        public void onSetBtNameRsp(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onSetBtNameRsp(id, msg);
        }

        /**
         * IP AND PORT 配置回调
         */
        public void onSetMethIpRsp(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onSetMethIpRsp(id, msg);
        }

        /**
         * 读取IP
         */
        public void onGetMethIpRsp(String id, GnbMethIpRsp msg) {
            if (setGnbListener != null) setGnbListener.onGetMethIpRsp(id, msg);
        }

        /**
         * 配置PA控制IO口
         */
        public void onSetPaGpioRsp(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onSetPaGpioRsp(id, msg);
        }

        /**
         * 配置PA控制IO口
         */
        public void onGetPaGpioRsp(String id, GnbGpioRsp msg) {
            if (setGnbListener != null) setGnbListener.onGetPaGpioRsp(id, msg);
        }

        /**
         * 配置设备名称回调
         */
        public void onSetSysInfoRsp(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onSetSysInfoRsp(id, msg);
        }

        /**
         * 读取设备信息
         */
        public void onGetSysInfoRsp(String id, GnbGetSysInfoRsp msg) {
            if (setGnbListener != null) setGnbListener.onGetSysInfoRsp(id, msg);
        }

        /**
         * 单板工作模式配置
         */
        public void onSetDualCellRsp(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onSetDualCellRsp(id, msg);
        }

        /**
         * 单板工作模式配置
         */
        public void onSetRxGainRsp(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onSetRxGainRsp(id, msg);
        }

        /**
         * 查询侦码配置
         */
        public void onGetCatchCfgRsp(String id, GnbCatchCfgRsp msg) {
            if (setGnbListener != null) setGnbListener.onGetCatchCfg(id, msg);
        }

        /**
         * 配置Gps
         */
        public void onSetGpsRsp(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onSetGpsRsp(id, msg);
        }

        /**
         * 获取系统log
         */
        public void onGetSysLogRsp(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onGetSysLogRsp(id, msg);
        }

        /**
         * 配置风扇速率
         */
        public void onSetFanSpeedRsp(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onSetFanSpeedRsp(id, msg);
        }

        /**
         * 自动配置风扇速率
         */
        public void onSetFanAutoSpeedRsp(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onSetFanAutoSpeedRsp(id, msg);
        }

        /**
         * 配置增强频点
         */
        public void onSetJamArfcn(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onSetJamArfcn(id, msg);
        }


        /**
         * 扫频上报
         */
        public void onFreqScanRsp(String id, GnbFreqScanRsp msg) {
            if (setGnbListener != null) setGnbListener.onFreqScanRsp(id, msg);
        }

        /**
         * 扫频上报
         */
        public void onFreqScanGetDocumentRsp(String id, GnbFreqScanGetDocumentRsp msg) {
            if (setGnbListener != null) setGnbListener.onFreqScanGetDocumentRsp(id, msg);
        }

        /**
         * 停止扫频
         */
        public void onStopFreqScanRsp(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onStopFreqScanRsp(id, msg);
        }

        /**
         * GPS时偏测量
         */
        public void onStartTdMeasure(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onStartTdMeasure(id, msg);
        }

        /**
         * GPS信息查询
         */
        public void onGetGpsRsp(String id, GnbGpsRsp msg) {
            if (setGnbListener != null) setGnbListener.onGetGpsRsp(id, msg);
        }

        /**
         * 设置转发UDP报文
         */
        public void onSetForwardUdpMsg(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onSetForwardUdpMsg(id, msg);
        }

        /**
         * 配置GPS输入输出端口结果回调
         */
        public void onSetGpsInOut(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onSetGpsInOut(id, msg);
        }

        /**
         * 读取GPS输入输出端口
         */
        public void onGetGpsInOut(String id, GnbGpsInOutRsp msg) {
            if (setGnbListener != null) setGnbListener.onGetGpsInOut(id, msg);
        }

        /**
         * 开始频段扫频
         */
        public void onStartBandScan(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onStartBandScan(id, msg);
        }

        /**
         * 获取私有数据
         */
        public void onGetUserData(String id, GnbUserDataRsp msg) {
            if (setGnbListener != null) setGnbListener.onGetUserData(id, msg);
        }

        /**
         * 保存私有数据
         */
        public void onSetUserData(String id, GnbUserDataRsp msg) {
            if (setGnbListener != null) setGnbListener.onSetUserData(id, msg);
        }

        /**
         * 配置 GpioTxRx
         */
        public void onSetGpioTxRx(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onSetGpioTxRx(id, msg);
        }

        /**
         * 配置FTP SERVER
         */
        public void onSetFtpRsp(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onSetFtpRsp(id, msg);
        }

        /**
         * 获取FTP 信息
         */
        public void onGetFtpRsp(String id, GnbFtpRsp msg) {
            if (setGnbListener != null) setGnbListener.onGetFtpRsp(id, msg);
        }

        /**
         * 单板单双栈模式配置
         */
        public void onSetDualStackRsp(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onSetDualStackRsp(id, msg);
        }

        /**
         * 数传收到消息应答
         */
        public void onReadDataFwdRsp(String id, GnbReadDataFwdRsp msg) {
            if (setGnbListener != null) setGnbListener.onReadDataFwdRsp(id, msg);
        }

        /**
         * 设置授权时间
         */
        public void onSetLicRsp(String id, GnbCmdRsp msg) {
            if (setGnbListener != null) setGnbListener.onSetLicRsp(id, msg);
        }

        public void onSetPhoneTypeRsp(String id, GnbCmdRsp msg) {
            if (setGnbListener_in != null) setGnbListener_in.onSetPhoneTypeRsp(id, msg);
        }

        public void onSetFuncFcgRsp(String id, GnbCmdRsp msg) {
            if (setGnbListener_in != null) setGnbListener_in.onSetFuncCfgRsp(id, msg);
        }

        //todo 增加接口回调在这里添加

        /**
         * IP AND PORT 配置回调
         *
         * @param msg
         */
        /*public void onIpPortCfgRsp(String id, GnbStateRsp msg) {
        	if (setGnbListener != null)
        		setGnbListener.onIpPortCfgRsp(String id, msg);
        }*/
        /**
         * 发送功率衰减校验证
         *
         * @param msg
         */
        /*public void onSetNvTxPwrOffsetRsp(String id, GnbStateRsp msg) {
        	if (setGnbListener != null)
        		setGnbListener.onSetNvTxPwrOffsetRsp(id, msg);
        }*/
    }

    public interface OnSetGnbListener {
        void onHeartStateRsp(GnbStateRsp rsp);

        void onQueryVersionRsp(String id, GnbVersionRsp rsp);

        void onStartTraceRsp(String id, GnbTraceRsp rsp);

        void onStopTraceRsp(String id, GnbCmdRsp rsp);

        void onStartCatchRsp(String id, GnbTraceRsp rsp);

        void onStopCatchRsp(String id, GnbCmdRsp rsp);

        void onSetGnbRsp(String id, GnbCmdRsp rsp);

        void onSetBlackListRsp(String id, GnbCmdRsp rsp);

        void onSetTxPwrOffsetRsp(String id, GnbCmdRsp rsp);

        void onSetNvTxPwrOffsetRsp(String id, GnbCmdRsp rsp);// 外发删除

        void onSetTimeRsp(String id, GnbCmdRsp rsp);

        void onSetRebootRsp(String id, GnbCmdRsp rsp);

        void onFirmwareUpgradeRsp(String id, GnbCmdRsp rsp);

        void onSetWifiInfoRsp(String id, GnbCmdRsp rsp);

        void onGetLogRsp(String id, GnbCmdRsp rsp);

        void onGetOpLogRsp(String id, GnbCmdRsp rsp);

        void onWriteOpLogRsp(String id, GnbCmdRsp rsp);

        void onDeleteOpLogRsp(String id, GnbCmdRsp rsp);

        void onSetBtNameRsp(String id, GnbCmdRsp rsp);

        void onSetMethIpRsp(String id, GnbCmdRsp rsp);

        void onGetMethIpRsp(String id, GnbMethIpRsp rsp);

        void onGpsOffsetRsp(String id, GnbCmdRsp rsp);

        void onSetPaGpioRsp(String id, GnbCmdRsp rsp);

        void onGetPaGpioRsp(String id, GnbGpioRsp rsp);

        void onSetSysInfoRsp(String id, GnbCmdRsp rsp);

        void onGetSysInfoRsp(String id, GnbGetSysInfoRsp rsp);

        void onSetDualCellRsp(String id, GnbCmdRsp rsp);

        void onSetRxGainRsp(String id, GnbCmdRsp rsp);

        void onGetCatchCfg(String id, GnbCatchCfgRsp rsp);

        void onSetGpsRsp(String id, GnbCmdRsp rsp);

        void onGetSysLogRsp(String id, GnbCmdRsp rsp);

        void onSetFanSpeedRsp(String id, GnbCmdRsp rsp);

        void onSetFanAutoSpeedRsp(String id, GnbCmdRsp rsp);

        void onSetJamArfcn(String id, GnbCmdRsp rsp);

        void onFreqScanRsp(String id, GnbFreqScanRsp rsp);

        void onFreqScanGetDocumentRsp(String id, GnbFreqScanGetDocumentRsp rsp);

        void onStopFreqScanRsp(String id, GnbCmdRsp rsp);

        void onStartTdMeasure(String id, GnbCmdRsp rsp);

        void onGetGpsRsp(String id, GnbGpsRsp rsp);

        void onSetForwardUdpMsg(String id, GnbCmdRsp rsp);

        void onSetGpsInOut(String id, GnbCmdRsp rsp);

        void onGetGpsInOut(String id, GnbGpsInOutRsp rsp);

        void onStartBandScan(String id, GnbCmdRsp rsp);

        void onSetUserData(String id, GnbUserDataRsp rsp);

        void onGetUserData(String id, GnbUserDataRsp rsp);

        void onSetGpioTxRx(String id, GnbCmdRsp rsp);

        void onStartControlRsp(String id, GnbTraceRsp rsp);

        void onStopControlRsp(String id, GnbCmdRsp rsp);

        void onSetFtpRsp(String id, GnbCmdRsp rsp);

        void onGetFtpRsp(String id, GnbFtpRsp rsp);

        void onSetDualStackRsp(String id, GnbCmdRsp rsp);

        void onReadDataFwdRsp(String id, GnbReadDataFwdRsp rsp);

        void onSetLicRsp(String id, GnbCmdRsp rsp);
    }

    /**
     * 此接口类专用于对内部开放一些接口以及回调
     * */
    public interface OnSetGnbIn {
        void onSetPhoneTypeRsp(String id, GnbCmdRsp rsp);
        void onSetFuncCfgRsp(String id, GnbCmdRsp rsp);
//		void onIpPortCfgRsp(String id, GnbStateRsp rsp);
    }
}
