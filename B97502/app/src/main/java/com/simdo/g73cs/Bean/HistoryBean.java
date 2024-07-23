package com.simdo.g73cs.Bean;

import com.simdo.g73cs.Util.DateUtil;

import java.util.LinkedList;

public class HistoryBean {

    private boolean checked;
    /** 模式-  0 自动、1 名单、2 专业 3 专业 自配置PCI */
    private int mode;
    /** 建立时间 */
    private String createTime;
    /** 最新开启时间 */
    private String startTime;

    private String imsiFirst;
    private String imsiSecond;
    private String imsiThird;
    private String imsiFourth;

    private LinkedList<ArfcnPciBean> TD1;
    private LinkedList<ArfcnPciBean> TD2;
    private LinkedList<ArfcnPciBean> TD3;
    private LinkedList<ArfcnPciBean> TD4;

    public HistoryBean(int mode, String imsiFirst) {
        this.checked = false;
        this.mode = mode;
        this.imsiFirst = imsiFirst;
        this.imsiSecond = "";
        this.imsiThird = "";
        this.imsiFourth = "";
        this.createTime = DateUtil.getMMddTime();
        this.startTime = "00:00:00";

        TD1 = new LinkedList<>();
        TD2 = new LinkedList<>();
        TD3 = new LinkedList<>();
        TD4 = new LinkedList<>();
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getImsiFirst() {
        return imsiFirst;
    }

    public void setImsiFirst(String imsiFirst) {
        this.imsiFirst = imsiFirst;
    }

    public String getImsiSecond() {
        return imsiSecond;
    }

    public void setImsiSecond(String imsiSecond) {
        this.imsiSecond = imsiSecond;
    }

    public String getImsiThird() {
        return imsiThird;
    }

    public void setImsiThird(String imsiThird) {
        this.imsiThird = imsiThird;
    }

    public String getImsiFourth() {
        return imsiFourth;
    }

    public void setImsiFourth(String imsiFourth) {
        this.imsiFourth = imsiFourth;
    }

    public LinkedList<ArfcnPciBean> getTD1() {
        return TD1;
    }

    public void setTD1(LinkedList<ArfcnPciBean> TD1) {
        this.TD1 = TD1;
    }

    public LinkedList<ArfcnPciBean> getTD2() {
        return TD2;
    }

    public void setTD2(LinkedList<ArfcnPciBean> TD2) {
        this.TD2 = TD2;
    }

    public LinkedList<ArfcnPciBean> getTD3() {
        return TD3;
    }

    public void setTD3(LinkedList<ArfcnPciBean> TD3) {
        this.TD3 = TD3;
    }

    public LinkedList<ArfcnPciBean> getTD4() {
        return TD4;
    }

    public void setTD4(LinkedList<ArfcnPciBean> TD4) {
        this.TD4 = TD4;
    }

}
