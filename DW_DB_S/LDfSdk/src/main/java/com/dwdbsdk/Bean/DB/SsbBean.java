package com.dwdbsdk.Bean.DB;

import java.util.ArrayList;
import java.util.List;

public class SsbBean {
    private long loFreq; // 载波频率
    private int timeOffset; // 时偏
    private int scsType; // 1: nr(30KHz)  0: lte(15KHz)
    private List<Integer> ddsList = new ArrayList<>(); // NR = 200MHz   LTE = 100MHz 带宽上SSB的频率位置

    public SsbBean(long loFreq, int timeOffset, int scsFlag, List<Integer> list) {
        this.loFreq = loFreq;
        this.timeOffset = timeOffset;
        this.scsType = scsFlag;
        this.ddsList.clear();
        this.ddsList.addAll(list);
    }

    public int getScsType() {
        return scsType;
    }

    public void setScsType(int scsType) {
        this.scsType = scsType;
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
        return "SsbBean{" +
                "loFreq=" + loFreq +
                ", timeOffset=" + timeOffset +
                ", scsType=" + scsType +
                ", ddsList=" + ddsList +
                '}';
    }
}
