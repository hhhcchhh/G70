package com.dwdbsdk.Interface;

import com.dwdbsdk.Response.DB.MsgCmdRsp;
import com.dwdbsdk.Response.DB.MsgGetJamRsp;
import com.dwdbsdk.Response.DB.MsgReadDataFwdRsp;
import com.dwdbsdk.Response.DB.MsgScanRsp;
import com.dwdbsdk.Response.DB.MsgSenseReportRsp;
import com.dwdbsdk.Response.DB.MsgStateRsp;
import com.dwdbsdk.Response.DB.MsgVersionRsp;

public interface DBBusinessListener {

    /** 心跳、时间同步、查询版本 */
    void onDBHeartStateRsp(String id, MsgStateRsp rsp);
    void onDBSetTimeRsp(String id, MsgStateRsp rsp);
    void onDBGetVersionRsp(String id, MsgVersionRsp rsp);

    /** Log类 */
    void onDBGetLogRsp(String id, MsgStateRsp rsp);

    /** 设置类 */
    void onDBSetBtNameRsp(String id, MsgStateRsp rsp);
    void onDBSetDevNameRsp(String id, MsgCmdRsp rsp);
    void onDBWifiCfgRsp(String id, MsgStateRsp rsp);
    void onDBRxGainCfgRsp(String id, MsgStateRsp rsp);

    /** for SG jam */
    void onDBStartSGRsp(String id, MsgStateRsp rsp);
    void onDBStopSGRsp(String id, MsgStateRsp rsp);

    /** 干扰 */
    void onDBStartJamRsp(String id, MsgStateRsp rsp);
    void onDBStopJamRsp(String id, MsgStateRsp rsp);
    void onDBGetJamRsp(String id, MsgGetJamRsp rsp);

    /** 单兵 */
    void onDBStartScanRsp(String id, MsgScanRsp rsp);
    void onDBStartPwrDetectRsp(String id, MsgScanRsp rsp);
    void onDBStopPwrDetectRsp(String id, MsgStateRsp rsp);
    void onDBSetGpioCfgRsp(String id, MsgStateRsp rsp);
    void onReadDataFwdRsp(String id,MsgReadDataFwdRsp rsp);

    /** 基带重启、升级 */
    void onDBRebootRsp(String id, MsgStateRsp rsp);
    void onDBUpgradeRsp(String id, MsgStateRsp rsp);
    void onSetDeviceId(String id, MsgStateRsp rsp);
    void onStartSenseRsp(String id,MsgCmdRsp rsp);
    void onStopSenseRsp(String id,MsgCmdRsp rsp);
    void onSenseReportRsp(String id, MsgSenseReportRsp rsp);
    void onDBStartSSDetectRsp(String id, MsgCmdRsp msg);
}
