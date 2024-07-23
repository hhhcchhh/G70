package com.simdo.g73cs.Bean;

import com.dwdbsdk.Bean.UeidBean;

public class MyUeidBean {

    private String name;
    private UeidBean mUeidBean;
    private boolean firstChecked;
    private boolean secondChecked;

    public MyUeidBean(String name, UeidBean mUeidBean, boolean firstChecked, boolean secondChecked) {
        this.name = name;
        this.mUeidBean = mUeidBean;
        this.firstChecked = firstChecked;
        this.secondChecked = secondChecked;
    }

    public String getName() {
        return name;
    }

    public UeidBean getUeidBean() {
        return mUeidBean;
    }

    public boolean isFirstChecked() {
        return firstChecked;
    }

    public boolean isSecondChecked() {
        return secondChecked;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setmUeidBean(UeidBean mUeidBean) {
        this.mUeidBean = mUeidBean;
    }

    public void setFirstChecked(boolean firstChecked) {
        this.firstChecked = firstChecked;
    }

    public void setSecondChecked(boolean secondChecked) {
        this.secondChecked = secondChecked;
    }
}
