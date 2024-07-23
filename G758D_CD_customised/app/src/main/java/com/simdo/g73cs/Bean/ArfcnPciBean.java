package com.simdo.g73cs.Bean;

public class ArfcnPciBean {
    public String getArfcn() {
        return arfcn;
    }

    public void setArfcn(String arfcn) {
        this.arfcn = arfcn;
    }

    public String getPci() {
        return pci;
    }

    public void setPci(String pci) {
        this.pci = pci;
    }

    private String arfcn;

    private String pci;

    public int getRsrp() {
        return rsrp;
    }

    public void setRsrp(int rsrp) {
        this.rsrp = rsrp;
    }

    private int rsrp;

    public ArfcnPciBean(String arfcn, String pci) {
        this.arfcn = arfcn;
        this.pci = pci;
    }

    public ArfcnPciBean(String arfcn, String pci, int rsrp) {
        this.arfcn = arfcn;
        this.pci = pci;
        this.rsrp = rsrp;
    }

    @Override
    public String toString() {
        return arfcn + "/" + pci;
    }
}
