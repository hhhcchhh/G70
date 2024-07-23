package com.simdo.g73cs.Bean;

import java.util.LinkedList;

public class ArfcnBean {

    public LinkedList<Integer> getArfcnList() {
        return arfcnList;
    }

    public void setArfcnList(LinkedList<Integer> arfcnList) {
        this.arfcnList = arfcnList;
    }

    public int getArfcnFromList(int index) {
        return arfcnList.get(index);
    }

    private int index;
    private LinkedList<Integer> arfcnList;

    public ArfcnBean(LinkedList<Integer> arfcnList) {
        this.index = -1;
        this.arfcnList = arfcnList;
    }

}
