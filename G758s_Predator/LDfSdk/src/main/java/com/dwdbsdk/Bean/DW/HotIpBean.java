package com.dwdbsdk.Bean.DW;

import java.util.Objects;

public class HotIpBean {
    String ip;
    boolean send;
    boolean connect;

    public HotIpBean(String ip) {
        this.ip = ip;
        this.send = false;
        this.connect = false;
    }

    public boolean isConnect() {
        return connect;
    }

    public void setConnect(boolean connect) {
        this.connect = connect;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public boolean isSend() {
        return send;
    }

    public void setSend(boolean send) {
        this.send = send;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HotIpBean hotIpBean = (HotIpBean) o;
        return ip.equals(hotIpBean.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip);
    }
}
