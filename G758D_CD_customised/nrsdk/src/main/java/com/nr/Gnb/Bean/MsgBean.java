/**
 * 配置UE定位参数表
 */
package com.nr.Gnb.Bean;

public class MsgBean {
    private String id;
    private byte[] msg;

    public MsgBean(String id, byte[] msg) {
        this.id = id;
        this.msg = msg;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte[] getMsg() {
        return msg;
    }

    public void setMsg(byte[] msg) {
        this.msg = msg;
    }
}
