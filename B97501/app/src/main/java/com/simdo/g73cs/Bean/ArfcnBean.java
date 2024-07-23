package com.simdo.g73cs.Bean;

public class ArfcnBean {

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public int getArfcn() {
        return arfcn;
    }

    public void setArfcn(int arfcn) {
        this.arfcn = arfcn;
    }

    public ArfcnBean(int arfcn, boolean isChecked) {
        this.isChecked = isChecked;
        this.arfcn = arfcn;
    }

    boolean isChecked;
    int arfcn;

}
