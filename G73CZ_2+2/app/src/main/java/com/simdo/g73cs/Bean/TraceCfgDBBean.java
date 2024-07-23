package com.simdo.g73cs.Bean;

public class TraceCfgDBBean {
    private String arfcnJsonArray;
    private String pci;
    private String imsi;

    public TraceCfgDBBean(String arfcnJsonArray, String pci, String imsi) {
        this.arfcnJsonArray = arfcnJsonArray;
        this.pci = pci;
        this.imsi = imsi;
    }

    public TraceCfgDBBean() {
    }

    public String getArfcnJsonArray() {
        return arfcnJsonArray;
    }

    public void setArfcnJsonArray(String arfcnJsonArray) {
        this.arfcnJsonArray = arfcnJsonArray;
    }

    public String getPci() {
        return pci;
    }

    public void setPci(String pci) {
        this.pci = pci;
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }
}
