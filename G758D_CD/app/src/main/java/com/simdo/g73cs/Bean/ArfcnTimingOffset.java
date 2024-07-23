/**
 * 配置定位参数表
 */
package com.simdo.g73cs.Bean;

public class ArfcnTimingOffset {
    private String arfcn;
    private int timingOffset;

    public ArfcnTimingOffset(String arfcn, int timingOffset) {
        this.arfcn = arfcn;
        this.timingOffset = timingOffset;
    }
    public ArfcnTimingOffset(int timingOffset) {
        this.timingOffset = timingOffset;
    }

    public String getArfcn() {
        return arfcn;
    }

    public void setArfcn(String arfcn) {
        this.arfcn = arfcn;
    }

    public int getTimingOffset() {
        return timingOffset;
    }

    public void setTimingOffset(int timingOffset) {
        this.timingOffset = timingOffset;
    }
}
