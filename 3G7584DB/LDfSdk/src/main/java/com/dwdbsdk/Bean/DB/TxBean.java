/**
 *  typedef struct {
 *  	long lo_frequency;
 *  	int time_offset;
 *  	int re_cnt;
 *  	int re_list[8]; 				// re_list max = 8
 *      int arfcn[8];
 *  } bt_msg_tx_t;
 */
package com.dwdbsdk.Bean.DB;

import java.util.ArrayList;
import java.util.List;

public class TxBean {
    private long loFreq; // 载波频率
    private int timeOffset; // 时偏
    private int scsType; // 1: nr(30KHz)  0: lte(15KHz)
    private int ssbNum; // 当前配置个数 max = 8
    private List<Integer> ddsList = new ArrayList<Integer>(); // NR = 200MHz   LTE = 100MHz 带宽上SSB的频率位置
    private List<Integer> arfcnList = new ArrayList<Integer>();

    public TxBean(long loFreq, int timeOffset, int scsFlag, int reNum, List<Integer> list, List<Integer> aList) {
        this.loFreq = loFreq;
        this.timeOffset = timeOffset;
        this.scsType = scsFlag;
        this.ssbNum = reNum;
        this.ddsList.clear();
        this.ddsList.addAll(list);
        this.arfcnList.clear();
        this.arfcnList.addAll(aList);
    }

    public List<Integer> getArfcnList() {
        return arfcnList;
    }

    public void setArfcnList(List<Integer> arfcnList) {
        this.arfcnList = arfcnList;
    }

    public int getScsType() {
        return scsType;
    }

    public void setScsType(int scsType) {
        this.scsType = scsType;
    }

    public int getSsbNum() {
        return ssbNum;
    }

    public void setSsbNum(int ssbNum) {
        this.ssbNum = ssbNum;
    }

    public long getLoFreq() {
        return loFreq;
    }

    public void setLoFreq(long loFreq) {
        this.loFreq = loFreq;
    }

    public int getTimeOffset() {
        return timeOffset;
    }

    public void setTimeOffset(int timeOffset) {
        this.timeOffset = timeOffset;
    }

    public List<Integer> getDdsList() {
        return ddsList;
    }

    public void setDdsList(List<Integer> ddsList) {
        this.ddsList = ddsList;
    }

    @Override
    public String toString() {
        return "TxBean{" +
                "loFreq=" + loFreq +
                ", timeOffset=" + timeOffset +
                ", scsType=" + scsType +
                ", ssbNum=" + ssbNum +
                ", ddsList=" + ddsList +
                ", arfcnList=" + arfcnList +
                '}';
    }
}
