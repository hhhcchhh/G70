/**
 * 配置UE定位参数表
 */
package com.dwdbsdk.Bean;

public class PaPkUlArfcnBean {
    private int pa;
    private int pk;
    private int ul_arfcn;

    private int slot_index;
    private int frame_type;

    public PaPkUlArfcnBean(int pa, int pk, int ul_arfcn, int slot_index, int frame_type) {
        this.pa = pa;
        this.pk = pk;
        this.ul_arfcn = ul_arfcn;
        this.slot_index = slot_index;
        this.frame_type = frame_type;
    }

    public int getPa() {
        return pa;
    }

    public void setPa(int pa) {
        this.pa = pa;
    }

    public int getPk() {
        return pk;
    }

    public void setPk(int pk) {
        this.pk = pk;
    }

    public int getUl_arfcn() {
        return ul_arfcn;
    }

    public void setUl_arfcn(int ul_arfcn) {
        this.ul_arfcn = ul_arfcn;
    }

    public int getSlot_index() {
        return slot_index;
    }

    public void setSlot_index(int slot_index) {
        this.slot_index = slot_index;
    }

    public int getFrame_type() {
        return frame_type;
    }

    public void setFrame_type(int frame_type) {
        this.frame_type = frame_type;
    }
}
