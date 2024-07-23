/**
 * 时偏列表
 */
package com.simdo.g73cs.Bean;

import java.util.ArrayList;
import java.util.List;

public class CheckBoxBean {
    private String text;
    private boolean isChecked;
    private List<Integer> arfcnList;
    private int cell_id;
    private int time_offset;
    private String dev_type;

    @Override
    public String toString() {
        return "CheckBoxBean{" +
                "text='" + text + '\'' +
                ", isChecked=" + isChecked +
                ", arfcnList=" + arfcnList +
                ", chan_id=" + cell_id +
                ", time_offset=" + time_offset +
                ", dev_type='" + dev_type + '\'' +
                '}';
    }


    public CheckBoxBean(String text, int cell_id, String dev_type) {
        this.text = text;
        this.isChecked = false;
        this.cell_id = cell_id;
        this.dev_type = dev_type;
        this.arfcnList = new ArrayList<>();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public List<Integer> getArfcnList() {
        return arfcnList;
    }

    public void addArfcnList(Integer arfcn) {
        this.arfcnList.add(arfcn);
    }

    public void addAllArfcnList(List<Integer> arfcnList) {
        this.arfcnList.addAll(arfcnList);
    }
    public void cleanArfcnList() {
        this.arfcnList.clear();
    }
    public int getCell_id() {
        return cell_id;
    }

    public void setCell_id(int cell_id) {
        this.cell_id = cell_id;
    }

    public int getTime_offset() {
        return time_offset;
    }

    public void setTime_offset(int time_offset) {
        this.time_offset = time_offset;
    }

    public String getDev_type() {
        return dev_type;
    }

    public void setDev_type(String dev_type) {
        this.dev_type = dev_type;
    }
}
