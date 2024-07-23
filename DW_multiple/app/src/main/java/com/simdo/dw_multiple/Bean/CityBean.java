/**
 * 时偏列表
 */
package com.simdo.dw_multiple.Bean;

import java.util.List;

public class CityBean {
    private String city;
    private List<ArfcnTimingOffset> arfcnList;


    public CityBean(String city, List<ArfcnTimingOffset> arfcn) {
        this.city = city;
        this.arfcnList = arfcn;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public List<ArfcnTimingOffset> getArfcnList() {
        return arfcnList;
    }

    public void setArfcnList(List<ArfcnTimingOffset> arfcnList) {
        this.arfcnList = arfcnList;
    }
}
