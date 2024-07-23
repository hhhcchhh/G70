package com.dwdbsdk.Bean.DB;

public class ArfcnBeanSsb {
    private int arfcn;
    private int timeOffset;
    private int pk;
    private int pa;
    private long freqCarrier;
    private boolean lock;

    public ArfcnBeanSsb(int arfcn, int timingOffset, int kssb, int offset2pointA, long freqCarrier) {
        this.arfcn = arfcn;
        this.timeOffset = timingOffset;
        this.pk = kssb;
        this.pa = offset2pointA;
        this.freqCarrier = freqCarrier;
        lock = false;
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

    public int getPk() {
        return pk;
    }

    public void setPk(int pk) {
        this.pk = pk;
    }

    public int getPa() {
        return pa;
    }

    public void setPa(int pa) {
        this.pa = pa;
    }

    public long getFreqCarrier() {
        return freqCarrier;
    }

    public void setFreqCarrier(long freqCarrier) {
        this.freqCarrier = freqCarrier;
    }

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    @Override
    public String toString() {
        return "ArfcnBean{" +
                "arfcn=" + arfcn +
                ", freqCarrier=" + freqCarrier +
                ", pk=" + pk +
                ", pa=" + pa +
                ", timeOffset=" + timeOffset +
                '}';
    }
}
