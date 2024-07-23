/**
 *  typedef struct {
 *   int arfcn
 * 	 long freq_carrier; 8
 * 	 int kssb;			12
 * 	 int offset2pointA;
 * 	 int time_offset;
 *  } bt_msg_orx_t;
 */
package com.dwdbsdk.Bean.DB;

public class OrxBean {
    private int arfcn;
    private long freqCarrier;
    private int pk;
    private int pa;
    private int timeOffset;

    public OrxBean(int arfcn, long freqCarrier, int k1, int k2, int timeOffset) {
        this.arfcn = arfcn;
        this.freqCarrier = freqCarrier;
        this.pk = k1;
        this.pa = k2;
        this.timeOffset = timeOffset;
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

    @Override
    public String toString() {
        return "OrxBean{" +
                "arfcn=" + arfcn +
                ", freqCarrier=" + freqCarrier +
                ", pk=" + pk +
                ", pa=" + pa +
                ", timeOffset=" + timeOffset +
                '}';
    }
}
