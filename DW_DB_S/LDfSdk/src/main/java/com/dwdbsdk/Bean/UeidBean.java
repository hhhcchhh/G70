/**
 * 配置UE定位参数表
 */
package com.dwdbsdk.Bean;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UeidBean)) return false;
        UeidBean ueidBean = (UeidBean) o;
        return imsi.equals(ueidBean.imsi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imsi);
    }

    @Override
    public String toString() {
        return "UeidBean{" +
                "imsi='" + imsi + '\'' +
                ", guti='" + guti + '\'' +
                '}';
    }
}
