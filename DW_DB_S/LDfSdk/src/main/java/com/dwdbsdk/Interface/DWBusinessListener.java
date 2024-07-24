package com.dwdbsdk.Interface;

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

import org.json.JSONObject;

public interface DWBusinessListener {

    /** 心跳、时间同步、查询版本 */
    void onDWHeartStateRsp(String id, GnbStateRsp rsp);
    void onDWSetTimeRsp(String id, GnbCmdRsp rsp);
    void onDWQueryVersionRsp(String id, GnbVersionRsp rsp);

    /** 业务流程--配置黑名单、配置频点参数、配置Gpio口、功率 */
    void onDWSetBlackListRsp(String id, GnbCmdRsp rsp);
    void onDWSetGnbRsp(String id, GnbCmdRsp rsp);
    void onDWSetPaGpioRsp(String id, GnbCmdRsp rsp);
    void onDWGetPaGpioRsp(String id, GnbGpioRsp rsp);
    void onDWSetTxPwrOffsetRsp(String id, GnbCmdRsp rsp);
    void onDWSetNvTxPwrOffsetRsp(String id, GnbCmdRsp rsp);// 外发删除

    /** 定位 */
    void onDWStartTraceRsp(String id, GnbTraceRsp rsp);
    void onDWStopTraceRsp(String id, GnbCmdRsp rsp);

    /** 侦码 */
    void onDWStartCatchRsp(String id, GnbTraceRsp rsp);
    void onDWStopCatchRsp(String id, GnbCmdRsp rsp);
    void onDWGetCatchCfg(String id, GnbCatchCfgRsp rsp);

    /** 管控 */
    void onDWStartControlRsp(String id, GnbTraceRsp rsp); // 外发删除
    void onDWStopControlRsp(String id, GnbCmdRsp rsp); // 外发删除

    /** 扫频 */
    void onDWFreqScanRsp(String id, GnbFreqScanRsp rsp);
    void onDWFreqScanGetDocumentRsp(String id, GnbFreqScanGetDocumentRsp rsp);
    void onDWStopFreqScanRsp(String id, GnbCmdRsp rsp);
    void onDWStartBandScan(String id, GnbCmdRsp rsp);

    /** Log类 */
    void onDWGetLogRsp(String id, GnbCmdRsp rsp);
    void onDWGetOpLogRsp(String id, GnbCmdRsp rsp);
    void onDWGetSysLogRsp(String id, GnbCmdRsp rsp);
    void onDWWriteOpLogRsp(String id, GnbCmdRsp rsp);
    void onDWDeleteOpLogRsp(String id, GnbCmdRsp rsp); // 外发删除

    /** 设置类 */
    void onDWSetDualCellRsp(String id, GnbCmdRsp rsp);
    void onDWStartTdMeasure(String id, GnbCmdRsp rsp);
    void onDWSetDevNameRsp(String id, GnbCmdRsp rsp);
    void onDWGetSysInfoRsp(String id, GnbGetSysInfoRsp rsp);
    void onDWSetWifiInfoRsp(String id, GnbCmdRsp rsp);
    void onDWSetBtNameRsp(String id, GnbCmdRsp rsp);
    void onDWSetMethIpRsp(String id, GnbCmdRsp rsp);
    void onDWGetMethIpRsp(String id, GnbMethIpRsp rsp);
    void onDWSetFtpRsp(String id, GnbCmdRsp rsp);
    void onDWGetFtpRsp(String id, GnbFtpRsp rsp);
    void onDWSetGpsRsp(String id, GnbCmdRsp rsp);
    void onDWGetGpsRsp(String id, GnbGpsRsp rsp);
    void onDWSetGpsInOut(String id, GnbCmdRsp rsp);
    void onDWGetGpsInOut(String id, GnbGpsInOutRsp rsp);
    void onDWSetFanSpeedRsp(String id, GnbCmdRsp rsp);
    void onDWSetFanAutoSpeedRsp(String id, GnbCmdRsp rsp);
    void onDWSetRxGainRsp(String id, GnbCmdRsp rsp);
    void onDWSetJamArfcn(String id, GnbCmdRsp rsp);
    void onDWSetForwardUdpMsg(String id, GnbCmdRsp rsp);
    void onDWSetGpioTxRx(String id,GnbCmdRsp msg);
    void onDWGetUserData(String id,GnbUserDataRsp msg);
    void onDWSetUserData(String id,GnbUserDataRsp msg);
    void onDWSetDataTo485(String id, GnbSetDataTo485Rsp msg);
    void onDWSetDataFwd(String id, GnbReadDataFwdRsp msg);

    /** 基带重启、升级 */
    void onDWRebootRsp(String id, GnbCmdRsp rsp);
    void onDWUpgradeRsp(String id, GnbCmdRsp rsp);

    void onDWGetCellCfg(String id, JSONObject rsp);

    void onDWRedirectUeCfg(String id, GnbCmdRsp rsp);

    void onDWResetPlmnCfg(String id, GnbCmdRsp rsp);
    void onDWSetPerPwrCfg(String id, GnbCmdRsp rsp);
}
