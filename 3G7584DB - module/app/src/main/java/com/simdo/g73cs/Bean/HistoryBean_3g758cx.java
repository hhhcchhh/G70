package com.simdo.g73cs.Bean;

import com.simdo.g73cs.Util.DateUtil;

import java.util.LinkedList;

public class HistoryBean_3g758cx {

    private boolean checked;
    /** 模式-  0 自动、1 名单、2 手动 */
    private int mode;
    /** 建立时间 */
    private String createTime;
    /** 最新开启时间 */
    private String startTime;

    private String imsi_yg;
    private String imsi_ld;

    private LinkedList<ArfcnPciBean> ygList;
    private LinkedList<ArfcnPciBean> ldList;
    private boolean ifAdaptive = false; //是否勾选自适应

    public HistoryBean_3g758cx(int mode, String imsi_yg) {
        this.checked = false;
        this.mode = mode;
        this.imsi_yg = imsi_yg;
        this.imsi_ld = "";
        this.createTime = DateUtil.getMMddTime();
        this.startTime = "00:00:00";

        ygList = new LinkedList<>();
        ldList = new LinkedList<>();
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

    public String getImsi_yg() {
        return imsi_yg;
    }

    public void setImsi_yg(String imsi_yg) {
        this.imsi_yg = imsi_yg;
    }

    public String getImsi_ld() {
        return imsi_ld;
    }

    public void setImsi_ld(String imsi_ld) {
        this.imsi_ld = imsi_ld;
    }

    public LinkedList<ArfcnPciBean> getYgList() {
        return ygList;
    }

    public void setYgList(LinkedList<ArfcnPciBean> ygList) {
        this.ygList = ygList;
    }

    public LinkedList<ArfcnPciBean> getLdList() {
        return ldList;
    }

    public void setLdList(LinkedList<ArfcnPciBean> ldList) {
        this.ldList = ldList;
    }

    public boolean getIfAdaptive() {
        return ifAdaptive;
    }

    public void setIfAdaptive(boolean ifAdaptive) {
        this.ifAdaptive = ifAdaptive;
    }

}
