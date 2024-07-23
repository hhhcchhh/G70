/**
 * 配置定位参数表
 */
package com.dwdbsdk.Bean.DB;

public class ArfcnTimeOffset {
    private int arfcn;
    private int timeOffset;

    public ArfcnTimeOffset(int arfcn, int timingOffset) {
        this.arfcn = arfcn;
        this.timeOffset = timingOffset;
    }

    public int getArfcn() {
        return arfcn;
    }

    public void setArfcn(int arfcn) {
        this.arfcn = arfcn;
    }

    public int getTimeOffset() {
        return timeOffset;
    }

    public void setTimeOffset(int timeOffset) {
        this.timeOffset = timeOffset;
    }
}
