package com.simdo.g73cs.Bean;

import java.util.List;

public class FreqArfcnBean {
    String band;
    boolean isChecked = true;
    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
    private List<ArfcnBean> arfcnList;
    public String getBand() {
        return band;
    }

    public void setBand(String band) {
        this.band = band;
    }
    public List<ArfcnBean> getArfcnList() {
        return arfcnList;
    }

    public void setArfcnList(List<ArfcnBean> arfcnList) {
        this.arfcnList = arfcnList;
    }

    public FreqArfcnBean(String band, List<ArfcnBean> arfcnList) {
        this.band = band;
        this.arfcnList = arfcnList;
        for (ArfcnBean arfcnBean : arfcnList) {
            if (!arfcnBean.isChecked()){
                isChecked = false;
                break;
            }
        }
    }
}
