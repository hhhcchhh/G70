package com.dwdbsdk.Bean;

import com.dwdbsdk.Util.SdkPref;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GnbTimingOffset {
    private static GnbTimingOffset instance;

    public static GnbTimingOffset build() {
        synchronized (GnbTimingOffset.class) {
            if (instance == null) {
                instance = new GnbTimingOffset();
            }
        }
        return instance;
    }

    public GnbTimingOffset() {
    }

    public void init() {
         /* arfcnList.clear();
        String list = SdkPref.build().getTimingOffset();
        if (list == null || list.equals("")) {*/
        arfcnList.clear();
        //633984,20,28,0;627264,12,24,0;504990,6,30,3000000;
        //  N41（2.6）
        arfcnList.add(new TimingBean(504990, 3000000, 6, 30));
        arfcnList.add(new TimingBean(512910, 3000000, 6, 250));
        arfcnList.add(new TimingBean(516990, 3000000, 4, 140));
        arfcnList.add(new TimingBean(507150, 3000000, 6, 90));
        arfcnList.add(new TimingBean(525630, 3000000, 4, 48));
        // N78（3.5）
        arfcnList.add(new TimingBean(633984, 0, 12, 24));
        arfcnList.add(new TimingBean(627264, 0, 12, 24));
        // N79 功放不支持（4.9）
        arfcnList.add(new TimingBean(723360, 0, 14, 200));
        //N1 功放不支持（2.1）
        arfcnList.add(new TimingBean(427250, 0, 6, 23));
        arfcnList.add(new TimingBean(422890, 0, 2, 12));
        arfcnList.add(new TimingBean(422930, 0, 2, 12));
        arfcnList.add(new TimingBean(427010, 0, 6, 15));
        arfcnList.add(new TimingBean(428910, 0, 10, 68));
        //N28 功放不支持（700）
        arfcnList.add(new TimingBean(154810, 0, 10, 75));
        // arfcnList.add(new TimingBean(154810, 0, 10, 75));
        saveToJson();
        /*} else {
            try {
                JSONObject jb = new JSONObject(list);
                JSONArray jcity = jb.getJSONArray("timing_list");
                if (jcity != null && jcity.length() > 0) {
                    for (int i = 0; i < jcity.length(); i++) {
                        JSONObject jbb = jcity.getJSONObject(i);
                        int arfcn = jbb.getInt("arfcn");
                        int timingOffset = jbb.getInt("timingOffset");
                        int kb = jbb.getInt("k1");
                        int o2p = jbb.getInt("k2");
                        arfcnList.add(new TimingBean(arfcn, timingOffset, kb, o2p));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }*/
    }

    private void saveToJson() {
        try {
            JSONObject jb = new JSONObject();
            JSONArray jcity = new JSONArray();
            for (int i = 0; i < arfcnList.size(); i++) {
                JSONObject jbb = new JSONObject();
                jbb.put("arfcn", arfcnList.get(i).getArfcn());
                jbb.put("timingOffset", arfcnList.get(i).getTimingOffset());
                jbb.put("k1", arfcnList.get(i).getK1());
                jbb.put("k2", arfcnList.get(i).getK2());
                jcity.put(jbb);
            }
            jb.put("timing_list", jcity);
            SdkPref.build().setTimingOffset(jb.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    /**
     * 删除频点
     */
    /*public void deleteArfcn(int arfcn) {
        for (int i = 0; i < arfcnList.size(); i++) { // 相同城市，更换数据
            if (arfcnList.get(i).getArfcn() == arfcn) {
                arfcnList.remove(i);
                break;
            }
        }
        saveToJson();
    }*/

    /**
     * 添加频点
     */
    /*public boolean addArfcn(int arfcn, int k1, int k2, int timingOffset) {
        // k1: ssb k2: k2
        boolean add = true;
        for (int i = 0; i < arfcnList.size(); i++) { // 相同城市，更换数据
            if (arfcnList.get(i).getArfcn() == arfcn) {
                add = false;
                break;
            }
        }
        if (add) {
            arfcnList.add(new TimingBean(arfcn, timingOffset, k1, k2));
            saveToJson();
        }
        return add;
    }*/
    public TimingBean getTimingOffset(int arfcn) {
        for (int i = 0; i < arfcnList.size(); i++) { // 相同城市，更换数据
            if (arfcnList.get(i).getArfcn() == arfcn) {
                return arfcnList.get(i);
            }
        }
        return null;
    }

    public List<TimingBean> getArfcnList() {
        return arfcnList;
    }

    private final List<TimingBean> arfcnList = new ArrayList<>(); // 数据列表

    public class TimingBean {
        private int arfcn;
        private int timingOffset;
        private int k1;
        private int k2;

        public TimingBean(int arfcn, int timingOffset, int kssb, int offset2pointA) {
            this.arfcn = arfcn;
            this.timingOffset = timingOffset;
            this.k1 = kssb;
            this.k2 = offset2pointA;
        }

        public int getArfcn() {
            return arfcn;
        }

        public void setArfcn(int arfcn) {
            this.arfcn = arfcn;
        }

        public int getTimingOffset() {
            return timingOffset;
        }

        public void setTimingOffset(int timingOffset) {
            this.timingOffset = timingOffset;
        }

        public int getK1() {
            return k1;
        }

        public void setK1(int k1) {
            this.k1 = k1;
        }

        public int getK2() {
            return k2;
        }

        public void setK2(int k2) {
            this.k2 = k2;
        }
    }
}
