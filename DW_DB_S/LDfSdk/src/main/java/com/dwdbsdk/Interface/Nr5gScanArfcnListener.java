package com.dwdbsdk.Interface;

import com.dwdbsdk.Bean.DW.LocBean;

import java.util.List;

public interface Nr5gScanArfcnListener {
    // SDK 扫频上报
    void onNr5gScanArfcnRsp(List<LocBean> cells);
}
