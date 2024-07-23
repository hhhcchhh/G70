/**
 * 时偏列表
 */
package com.simdo.g73cs.Bean;

public class CityBean {
    public CityBean(String city, String offset_5g, String offset_b34, String offset_b39, String offset_b40, String offset_b41) {
        this.city = city;
        this.offset_5g = offset_5g;
        this.offset_b34 = offset_b34;
        this.offset_b39 = offset_b39;
        this.offset_b40 = offset_b40;
        this.offset_b41 = offset_b41;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getOffset_5g() {
        return offset_5g;
    }

    public void setOffset_5g(String offset_5g) {
        this.offset_5g = offset_5g;
    }

    public String getOffset_b34() {
        return offset_b34;
    }

    public void setOffset_b34(String offset_b34) {
        this.offset_b34 = offset_b34;
    }

    public String getOffset_b39() {
        return offset_b39;
    }

    public void setOffset_b39(String offset_b39) {
        this.offset_b39 = offset_b39;
    }

    public String getOffset_b40() {
        return offset_b40;
    }

    public void setOffset_b40(String offset_b40) {
        this.offset_b40 = offset_b40;
    }

    public String getOffset_b41() {
        return offset_b41;
    }

    public void setOffset_b41(String offset_b41) {
        this.offset_b41 = offset_b41;
    }

    private String city;
    private String offset_5g;
    private String offset_b34;
    private String offset_b39;
    private String offset_b40;
    private String offset_b41;

}
