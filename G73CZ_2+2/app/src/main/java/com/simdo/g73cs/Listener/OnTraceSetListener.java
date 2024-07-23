package com.simdo.g73cs.Listener;

public interface OnTraceSetListener {
    //待修改：载波频点
    //自动定位
    //扫频走完开始定位，使用扫出来的pci算法
    void onTraceConfig(String id);
}
