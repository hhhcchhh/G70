/**
 * GPS同步信息
 */
package com.dwdbsdk.Util;

import java.util.ArrayList;
import java.util.List;

public class GpsState {

    private static GpsState instance;

    public static GpsState build() {
        synchronized (GpsState.class) {
            if (instance == null) {
                instance = new GpsState();
            }
        }
        return instance;
    }

    public GpsState() {
        gpsList.clear();
    }

    public boolean isGpsSync(String id) {
        //GPS 同步情况 1： 同步， 0：失步
        int gpsSync = 0;
        for (int i = 0; i < gpsList.size(); i++) {
            if (gpsList.get(i).getId().equals(id)) {
                gpsSync =  gpsList.get(i).getGpsSync();
            }
        }
        //GPS 同步情况 1： 同步， 0：失步
        if (gpsSync == 1) {
            return true;
        } else {
            return false;
        }
    }

    public void setGpsSync(String id, int sync) {
        if (gpsList.size() == 0) {
            gpsList.add(new GpsStateBean(id, sync));
        } else {
            boolean add = true;
            for (int i = 0; i < gpsList.size(); i++) {
                if (gpsList.get(i).getId().equals(id)) {
                    gpsList.get(i).setGpsSync(sync);
                    add = false;
                    break;
                }
            }
            if (add) {
                gpsList.add(new GpsStateBean(id, sync));
            }
        }
    }

    private final List<GpsStateBean> gpsList = new ArrayList<>();
    class GpsStateBean {
        private String id;
        private int gpsSync;

        public GpsStateBean(String id, int gpsSync) {
            this.id = id;
            this.gpsSync = gpsSync;
        }

        public String getId() {
            return id;
        }

        public int getGpsSync() {
            return gpsSync;
        }

        public void setGpsSync(int gpsSync) {
            this.gpsSync = gpsSync;
        }
    }
}
