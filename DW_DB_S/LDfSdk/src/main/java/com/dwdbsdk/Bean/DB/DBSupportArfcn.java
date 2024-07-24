package com.dwdbsdk.Bean.DB;

import com.dwdbsdk.Bean.NrBand;
import com.dwdbsdk.Logcat.SdkLog;
import com.dwdbsdk.Util.SdkPref;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DBSupportArfcn {
    private static DBSupportArfcn instance;

    public static DBSupportArfcn build() {
        synchronized (DBSupportArfcn.class) {
            if (instance == null) {
                instance = new DBSupportArfcn();
            }
        }
        return instance;
    }

    public DBSupportArfcn() {
        init();
    }

    private void init() {
        arfcnList.clear();
        String list = SdkPref.build().getSsbList();
        if (list == null || list.equals("")) {
            //calcFreqCarrier: arfcn = 504990, Freq_carrier = 2565000000
            //calcFreqCarrier: arfcn = 512910, Freq_carrier = 2565000000
            //calcFreqCarrier: arfcn = 516990, Freq_carrier = 2605230000
            //calcFreqCarrier: arfcn = 507150, Freq_carrier = 2565000000
            //calcFreqCarrier: arfcn = 633984, Freq_carrier = 3549960000
            //calcFreqCarrier: arfcn = 627264, Freq_carrier = 3450000000
            //calcFreqCarrier: arfcn = 723360, Freq_carrier = 4850010000
            //ORX载波有优先级，以下顺序请误修改
            // 2.6G
            arfcnList.add(new ArfcnBeanSsb(504990, 3000000, 6, 30, 2565000000L));
            arfcnList.add(new ArfcnBeanSsb(512910, 3000000, 6, 250, 2565000000L));
            arfcnList.add(new ArfcnBeanSsb(516990, 3000000, 4, 140, 2605230000L));
            arfcnList.add(new ArfcnBeanSsb(507150, 3000000, 6, 90, 2565000000L));
            arfcnList.add(new ArfcnBeanSsb(525630, 3000000, 4, 48,2664990000L));
            //3.5G
            arfcnList.add(new ArfcnBeanSsb(633984, 0, 12, 24, 3550800000L));
            arfcnList.add(new ArfcnBeanSsb(627264, 0, 12, 24, 3450000000L));
            //4.9G
            arfcnList.add(new ArfcnBeanSsb(723360, 0, 14, 200, 4850010000L));
            //2.1G freq error
            arfcnList.add(new ArfcnBeanSsb(427250, 0, 6, 23,2177560000L));
            arfcnList.add(new ArfcnBeanSsb(422890, 0, 2, 12,2157800000L));
            arfcnList.add(new ArfcnBeanSsb(422930, 0, 2, 12,2158000000L));
            arfcnList.add(new ArfcnBeanSsb(420710, 0, 6, 15,2157800000L));
            arfcnList.add(new ArfcnBeanSsb(428910, 0, 10, 68,2157800000L));
            //700M freq error
            arfcnList.add(new ArfcnBeanSsb(154810, 0, 6, 23, 4850010000L));
            arfcnList.add(new ArfcnBeanSsb(152890, 0, 6, 22, 4850010000L));
            //arfcnList.add(new ArfcnBeanSsb(156970, 0, 10, 135, 4850010000L));
            arfcnList.add(new ArfcnBeanSsb(152650, 0, 6, 12, 4850010000L));
            arfcnList.add(new ArfcnBeanSsb(154570, 0, 2, 69, 4850010000L));
            //频点156490的pa原本是122，要改成70
            //arfcnList.add(new ArfcnBeanSsb(156490, 0, 6, 70, 4850010000L));
            saveToJson();
        } else {
            try {
                JSONObject jb = new JSONObject(list);
                JSONArray jcity = jb.getJSONArray("ssb_list");
                if (jcity.length() > 0) {
                    for (int i = 0; i < jcity.length(); i++) {
                        JSONObject jbb = jcity.getJSONObject(i);
                        int arfcn = jbb.getInt("arfcn");
                        int timingOffset = jbb.getInt("timingOffset");
                        int kb = jbb.getInt("k1");
                        int o2p = jbb.getInt("k2");
                        long freqCarrier = jbb.getLong("freqCarrier");
                        arfcnList.add(new ArfcnBeanSsb(arfcn, timingOffset, kb, o2p, freqCarrier));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    private void saveToJson() {
        try {
            JSONObject jb = new JSONObject();
            JSONArray jcity = new JSONArray();
            for (int i = 0; i < arfcnList.size(); i++) {
                JSONObject jbb = new JSONObject();
                jbb.put("arfcn", arfcnList.get(i).getArfcn());
                jbb.put("timingOffset", arfcnList.get(i).getTimeOffset());
                jbb.put("k1", arfcnList.get(i).getPk());
                jbb.put("k2", arfcnList.get(i).getPa());
                jbb.put("freqCarrier", arfcnList.get(i).getFreqCarrier());
                jcity.put(jbb);
            }
            jb.put("ssb_list", jcity);
            SdkPref.build().setSsbList(jb.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除频点
     */
    public boolean delete(int arfcn) {
        for (int i = 0; i < arfcnList.size(); i++) { // 相同城市，更换数据
            if (arfcnList.get(i).getArfcn() == arfcn) {
                arfcnList.remove(i);
                SdkLog.D("delete: " + arfcn);
                saveToJson();
                return true;
            }
        }
        return false;
    }

    /**
     * 添加频点
     */
    public boolean add(int arfcn, int pk, int pa, int timingOffset) {
        // k1: kssb k2: offset2pointA
        boolean add = true;
        for (int i = 0; i < arfcnList.size(); i++) { // 相同城市，更换数据
            if (arfcnList.get(i).getArfcn() == arfcn) {
                add = false;
                break;
            }
        }
        if (add) {
            /*int band = NrBand.earfcn2band(arfcn);
            int tOffset = 0;
            if (band == 41 || band == 79|| band == 28) {
                tOffset =  timingOffset;
            }
*/
            //long freqCarrier = SsbUtil.build().OrxFreqCarrier(arfcn, pk, pa);
            long UL_NR_ARFCN = 0;
            boolean is_lte = arfcn < 100000;

            int band = NrBand.earfcn2band(arfcn);
            if (is_lte) {
                if (arfcn <= 9919) UL_NR_ARFCN = arfcn + 18000;
            } else {
                //fdd UL_ARFCN != DL_NR_ARFCN
                // DL_NR_ARFCN >= 600000 表示频段 > 3G
                if (arfcn < 600000) {
                    switch (band) {
                        case 1:
                            // 434000 - 396000 = 38000
                            // 422000 - 384000 = 38000
                            if (arfcn == 422890 || arfcn == 422930) { //422930
                                UL_NR_ARFCN = arfcn - 38798;
                            } else if (arfcn == 427010) {
                                UL_NR_ARFCN = 388092;
                            } else if (arfcn == 428910) { // 重庆
                                UL_NR_ARFCN = 388072;
                            } else {
                                //UL_NR_ARFCN = DL_NR_ARFCN - 38000;
                                //2024/1/17 调整默认则使用391010
                                UL_NR_ARFCN = 391010;
                            }
                            break;
                        case 3:
                            UL_NR_ARFCN = 349010;
                            break;
                        case 5:
                            UL_NR_ARFCN = 167050;
                            break;
                        case 8:
                            UL_NR_ARFCN = 179530;
                            break;
                        case 28:  // N28A: 703 -- 733(748)
                            // 151600 - 140600 = 11000
                            // 160600 - 149600 = 11000
                            UL_NR_ARFCN = 140720; //154810  152890 154810 154570 152650
                            break;
                    }
                }
            }

            arfcnList.add(new ArfcnBeanSsb(arfcn, timingOffset, pk, pa, UL_NR_ARFCN));
            SdkLog.D("add: " + arfcn + ", " + timingOffset + ", " + pk + ", " + pa + ", " + UL_NR_ARFCN);
            saveToJson();
        }
        return add;
    }

    public boolean isSupport(int arfcn) {
        for (int i = 0; i < arfcnList.size(); i++) {
            if (arfcnList.get(i).getArfcn() == arfcn) {
                return true;
            }
        }
        return false;
    }

    public int getTimeOffset(int arfcn) {
        for (int i = 0; i < arfcnList.size(); i++) {
            if (arfcnList.get(i).getArfcn() == arfcn) {
                return arfcnList.get(i).getTimeOffset();
            }
        }
        return -1;
    }
    public int getPk(int arfcn) {
        int band  = NrBand.earfcn2band(arfcn);
        for (int i = 0; i < arfcnList.size(); i++) {
            if (arfcnList.get(i).getArfcn() == arfcn) {
                return arfcnList.get(i).getPk();
            }
        }
        if (band == 1 || band == 28 ||band == 41){
            return 6;
        }else if (band == 78 ){
            return 12;
        }else if (band == 79){
            return 14;
        }else {
            return -1;
        }

    }
    public int getPa(int arfcn) {
        int band  = NrBand.earfcn2band(arfcn);
        for (int i = 0; i < arfcnList.size(); i++) {
            if (arfcnList.get(i).getArfcn() == arfcn) {
                return arfcnList.get(i).getPa();
            }
        }
        if (band == 1 || band == 28 ){
            return 23;
        }else if (band == 78 ){
            return 23;
        }else if (band == 79){
            return 254;
        }else if (band == 41){
            return 30;
        }else {
            return -1;
        }
    }
    public long getFreqCarrier(int arfcn) {
        for (int i = 0; i < arfcnList.size(); i++) {
            if (arfcnList.get(i).getArfcn() == arfcn) {
                return arfcnList.get(i).getFreqCarrier();
            }
        }
        return -1;
    }

    /**
     * LOCK 用于空口同步
     */
    public void resetLock() {
        for (int i = 0; i < arfcnList.size(); i++) {
            arfcnList.get(i).setLock(false);
        }
    }

    public void setLock(int arfcn) {
        int band = NrBand.earfcn2band(arfcn);
        for (int i = 0; i < arfcnList.size(); i++) {
            int band1 = NrBand.earfcn2band(arfcnList.get(i).getArfcn());
            if (band == band1) {
                arfcnList.get(i).setLock(true);
            }
        }
    }
    /**
     * 回传配置空口列表数据
     */
    public List<ArfcnBeanSsb> getOrxList() {
        orxList.clear();
        for (int i = 0; i < arfcnList.size(); i++) {
            if (!arfcnList.get(i).isLock()) {
                orxList.add(arfcnList.get(i));
                SdkLog.D("OrxList:" + arfcnList.get(i).toString());
            }
        }
        return orxList;
    }
    public List<ArfcnTimeOffset> getArfcnList() {
        supportList.clear();
        for (int i = 0; i < arfcnList.size(); i++) {
            supportList.add(new ArfcnTimeOffset(arfcnList.get(i).getArfcn(), arfcnList.get(i).getTimeOffset()));
        }
        return supportList;
    }

    public List<ArfcnBeanSsb> getList() {
        return arfcnList;
    }

    private final List<ArfcnBeanSsb> arfcnList = new ArrayList<>(); // 数据列表
    private final List<ArfcnBeanSsb> orxList = new ArrayList<>(); // 数据列表
    private final List<ArfcnTimeOffset> supportList = new ArrayList<>(); // 数据列表
}
