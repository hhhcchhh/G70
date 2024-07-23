/**
 * 获取的值是fpga核心温度*1000
 */
package com.nr.Util;
import java.util.ArrayList;
import java.util.List;

public class Temperature {

    private static Temperature instance;

    public static Temperature build() {
        synchronized (Temperature.class) {
            if (instance == null) {
                instance = new Temperature();
            }
        }
        return instance;
    }

    public Temperature() {
        tempList.clear();
    }

   public void addTemp(String id, double temp) {
        if (tempList.size() == 0) {
            tempList.add(new TempBean(id, temp));
        } else {
            boolean add = true;
            for (int i = 0; i < tempList.size(); i++) {
                if (tempList.get(i).getId().equals(id)) {
                    tempList.get(i).setTemp(temp);
                    add = false;
                    break;
                }
            }
            if (add) {
                tempList.add(new TempBean(id, temp));
            }
        }
   }

   public double getTemp(String id) {
       for (int i = 0; i < tempList.size(); i++) {
           if (tempList.get(i).getId().equals(id)) {
               return tempList.get(i).getTemp();
           }
       }
       return 0;
   }

    private List<TempBean> tempList = new ArrayList<TempBean>();

    class TempBean {
        private String id;
        private double temp;

        public TempBean(String id, double temp) {
            this.id = id;
            this.temp = temp;
        }

        public String getId() {
            return id;
        }

        public double getTemp() {
            return temp;
        }

        public void setTemp(double temp) {
            this.temp = temp;
        }
    }
}
