package com.nr.Arfcn.Bean;

import java.util.ArrayList;
import java.util.List;

public class LocBean {
    private String tac;
    private String eci;
    private String arfcn;
    private String pci;
    private String pri;
    private String rx;

    private List<BsBeam> beamList = new ArrayList<BsBeam>();

    public LocBean(String tac, String eci, String arfcn, String pci, String rx) {
        this.tac = tac;
        this.eci = eci;
        this.arfcn = arfcn;
        this.pci = pci;
        this.pri = "";
        this.rx = rx;
        this.beamList = null;
    }

    public LocBean(String tac, String eci, String arfcn, String pci, String pri, String rx, List<BsBeam> beamList) {
        this.tac = tac;
        this.eci = eci;
        this.arfcn = arfcn;
        this.pci = pci;
        this.rx = rx;
        this.pri = pri;
        if (beamList != null) {
            this.beamList.clear();
            beamList.addAll(beamList);
        }
    }

    public List<BsBeam> getBeamList() {
        return beamList;
    }

    public void setBeamList(List<BsBeam> beamList) {
        if (this.beamList == null) {
            this.beamList = new ArrayList<BsBeam>();
        } else {
            this.beamList.clear();
        }
        this.beamList.addAll(beamList);
    }

    public String getTac() {
        return tac;
    }

    public void setTac(String tac) {
        this.tac = tac;
    }

    public String getEci() {
        return eci;
    }

    public void setEci(String eci) {
        this.eci = eci;
    }

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

    public String getPri() {
        return pri;
    }

    public void setPri(String pri) {
        this.pri = pri;
    }

    public String getRx() {
        return rx;
    }

    public void setRx(String rx) {
        this.rx = rx;
    }

    @Override
    public String toString() {
        return "\nLocBean{" +
                "tac='" + tac + '\'' +
                ", eci='" + eci + '\'' +
                ", arfcn='" + arfcn + '\'' +
                ", pci='" + pci + '\'' +
                ", rx='" + rx + '\'' +
                '}';
    }
}