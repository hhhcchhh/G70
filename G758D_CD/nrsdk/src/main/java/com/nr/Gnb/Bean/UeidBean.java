/**
 * 配置UE定位参数表
 */
package com.nr.Gnb.Bean;

public class UeidBean {
    private String imsi;
    private String guti;

    public UeidBean(String imsi, String guti) {
        this.imsi = imsi;
        this.guti = guti;
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public String getGuti() {
        return guti;
    }

    public void setGuti(String guti) {
        this.guti = guti;
    }
}
