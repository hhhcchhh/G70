package com.nr.Socket.Bean;

import com.nr.Gnb.Bean.GnbProtocol;

public class MsgTypeBean {
    private String id;
    private String ip;
    private int msgType;
    private int traceType;
    public int getTraceType() {
        return traceType;
    }

    public void setTraceType(int traceType) {
        this.traceType = traceType;
    }

    public MsgTypeBean(String id, String ip) {
        this.id = id;
        this.ip = ip;
        msgType = GnbProtocol.UI_NONE;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }
}
