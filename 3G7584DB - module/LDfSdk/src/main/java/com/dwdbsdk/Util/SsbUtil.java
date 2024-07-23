package com.dwdbsdk.Util;

import com.dwdbsdk.Bean.DB.ArfcnBeanSsb;
import com.dwdbsdk.Bean.DB.DBSupportArfcn;
import com.dwdbsdk.Bean.DB.SsbBean;
import com.dwdbsdk.Bean.LteBand;
import com.dwdbsdk.Logcat.SdkLog;

import java.util.ArrayList;
import java.util.List;

public class SsbUtil {
    private static SsbUtil instance;

    public static SsbUtil build() {
        synchronized (SsbUtil.class) {
            if (instance == null) {
                instance = new SsbUtil();
            }
        }
        return instance;
    }

    public SsbUtil() {
        SdkLog.createFile();
    }

    /**
     * NR 频点数据转换
     */
    public SsbBean NrSsb(List<Integer> arfcnList, int time_offset) {
        List<Long> Freq_ssb_cent = new ArrayList<>();
        List<Long> Freq_ssb_begin = new ArrayList<>();
        List<Long> Freq_ssb_end = new ArrayList<>();
        for (int i = 0; i < arfcnList.size(); i++) {
            long ssb_cent = NrCenterFreq(arfcnList.get(i));
            if (ssb_cent != -1) {
                Freq_ssb_cent.add(ssb_cent);
                Freq_ssb_begin.add(ssb_cent - 3600000);//10*12*30*1000);
                Freq_ssb_end.add(ssb_cent + 3600000);//10*12*30*1000);
            } else {
                SdkLog.D("NrSsb ssb_cent = -1,arfcn = " + arfcnList.get(i));
            }
        }
        long max_ssb_freq = Freq_ssb_end.get(0);
        for (int i = 1; i < Freq_ssb_end.size(); i++) {
            if (max_ssb_freq < Freq_ssb_end.get(i)) {
                max_ssb_freq = Freq_ssb_end.get(i);
            }
        }
        long min_ssb_freq = Freq_ssb_begin.get(0);
        for (int i = 1; i < Freq_ssb_begin.size(); i++) {
            if (min_ssb_freq > Freq_ssb_begin.get(i)) {
                min_ssb_freq = Freq_ssb_begin.get(i);
            }
        }
        long band_width = max_ssb_freq - min_ssb_freq;
        long bw = band_width / 1000000;
        SdkLog.D("NrSsb band_width(MHz) = " + bw);
        if (bw > 200) {
            SdkLog.D("NrSsb The band width between max ssb_freq and min ssb_freq is bigger than 200MHz");
            return null;
        }
        long mod = band_width % 30000;// band_width,30e3
        double DIFF_RE = 0;
        if (mod == 0) {
            DIFF_RE = band_width / 30000;//double(band_width)/30e3;
        } else {
            //error('The band width between max ssb_freq and min ssb_freq can not Divided by 30KHz !!!');
            SdkLog.D("NrSsb The band width between max ssb_freq and min ssb_freq can not Divided by 30KHz !!!");
            return null;
        }
        long LO_frequency = (long) (min_ssb_freq + Math.ceil(DIFF_RE / 2) * 30000);//30e3;
        SdkLog.D("NrSsb LO_frequency = " + LO_frequency);
        SdkLog.D("NrSsb time_offset = " + time_offset);
        int LO_RE = 4097;//8192/2 + 1;
        List<Integer> RB_start_list = new ArrayList<Integer>();
        for (int i = 0; i < Freq_ssb_begin.size(); i++) {
            double tmp = LO_frequency - Freq_ssb_begin.get(i);
            tmp = tmp / 30000;//30e3;
            long tmp1 = (long) (LO_RE - tmp - 1);
            RB_start_list.add((int) tmp1);
            int idx = i + 1;
            SdkLog.D("NrSsb RB_start_list " + idx + ": " + tmp1);
        }
        // 锁定频点，不用于空口同步
        for (int i = 0; i < arfcnList.size(); i++) {
            DBSupportArfcn.build().setLock(arfcnList.get(i));
        }
        SsbBean txBean = new SsbBean(LO_frequency, time_offset, 1, RB_start_list);
        return txBean;
    }

    /**
     * NR计算载波中心频点
     */
    private long NrCenterFreq(int arfcn) {
        long F_ref_offs = 0;
        long delta_F_global = 0;
        long N_ref_offs = 0;
        if (arfcn < 600000) {
            F_ref_offs = 0;
            delta_F_global = 5000;//5e3;
            N_ref_offs = 0;
        } else if (arfcn >= 600000 && arfcn < 2016667) {
            F_ref_offs = 3000000000L;//3e9;
            delta_F_global = 15000;//15e3;
            N_ref_offs = 600000;
        } else if (arfcn >= 2016667 && arfcn < 3279166) {
            F_ref_offs = 24250080000L;//24250.08e6;
            delta_F_global = 60000;//60e3;
            N_ref_offs = 2016667;
        } else {
            SdkLog.D("NrCenterFreq return -1");
            return -1; // error
        }
        long ssb_cent = F_ref_offs + delta_F_global * (arfcn - N_ref_offs);
        SdkLog.D("NrCenterFreq " + arfcn + ": " + ssb_cent);
        return ssb_cent;
    }

    /**
     * LTE 频点数据转换(含5G频点N28(700M)|N1(2.1G))
     */
    public SsbBean LteSsb(List<Integer> arfcnList, int time_offset) {
        List<Long> Freq_ssb_cent = new ArrayList<Long>();
        for (int i = 0; i < arfcnList.size(); i++) {
            long ssb_cent = -1;
            int arfcn = arfcnList.get(i);
            if (arfcn < 99999) { // lte 频点
                ssb_cent = LteFreqCarrier(arfcnList.get(i));
            } else {
                ssb_cent = NrCenterFreq(arfcnList.get(i));
            }
            if (ssb_cent != -1) {
                Freq_ssb_cent.add(ssb_cent);
            } else {
                SdkLog.D("LteSsb ssb_cent = -1,arfcn = " + arfcnList.get(i));
            }
        }
        long max_ssb_freq = Freq_ssb_cent.get(0);
        long min_ssb_freq = Freq_ssb_cent.get(0);
        for (int i = 1; i < Freq_ssb_cent.size(); i++) {
            if (max_ssb_freq < Freq_ssb_cent.get(i)) {
                max_ssb_freq = Freq_ssb_cent.get(i);
            }
            if (min_ssb_freq > Freq_ssb_cent.get(i)) {
                min_ssb_freq = Freq_ssb_cent.get(i);
            }
        }
        long band_width = max_ssb_freq - min_ssb_freq;
        long bw = band_width / 1000000;
        SdkLog.D("LteSsb band_width(MHz) = " + bw);
        if (bw > 100) {
            SdkLog.D("LteSsb The band width between max ssb_freq and min ssb_freq is bigger than 100MHz");
            return null;
        }
        long LO_frequency = min_ssb_freq + band_width / 2;
        SdkLog.D("LteSsb LO_frequency = " + LO_frequency + ", time_offset = " + time_offset);
        List<Integer> dds_list = new ArrayList<Integer>();
        for (int i = 0; i < Freq_ssb_cent.size(); i++) {
            double tmp = LO_frequency - Freq_ssb_cent.get(i);
            double diff = tmp / 20000;
            if (diff < 0) {
                //SdkLog.D("LteSsb diff = " + diff);
                diff = 12288 + diff;
            }
            int idiff = (int) Math.ceil(diff);
            dds_list.add(idiff);
            SdkLog.D("LteSsb dds_list " + i + ": " + idiff);
        }
        SsbBean txBean = new SsbBean(LO_frequency, time_offset, 0, dds_list);
        return txBean;
    }

    /**
     * LTE频点载波频率
     */
    public long LteFreqCarrier(int arfcn) {
        double f_dl_low = LteBand.F_dl_low(arfcn);
        double N_offs_dl = LteBand.N_offs_dl(arfcn);
        long freq_carrier = (long) ((f_dl_low + 0.1 * (arfcn - N_offs_dl)) * 1000000);
        SdkLog.D("LteFreqCarrier " + arfcn + ": " + f_dl_low + ", " + N_offs_dl + ", " + freq_carrier);
        return freq_carrier;
    }

    /**
     * 计算载波频率
     */
    public long OrxFreqCarrier(int arfcn, int kssb, int RB_Offset) {
        long F_ref_offs = 0;
        long delta_F_global = 0;
        long N_ref_offs = 0;
        if (arfcn < 600000) {
            F_ref_offs = 0;
            delta_F_global = 5000;//5e3;
            N_ref_offs = 0;
        } else if (arfcn >= 600000 && arfcn < 2016667) {
            F_ref_offs = 3000000000L;//3e9;
            delta_F_global = 15000;//15e3;
            N_ref_offs = 600000;
        } else if (arfcn >= 2016667 && arfcn < 3279166) {
            F_ref_offs = 24250080000L;//24250.08e6;
            delta_F_global = 60000;//60e3;
            N_ref_offs = 2016667;
        } else {
            SdkLog.D("OrxFreqCarrier return -1");
            return -1; // error
        }
        // SSB中心频率（编号：arfcn），如果Freq_carrier有编号也可以用下面公式算
        long ssb_cent = F_ref_offs + delta_F_global * (arfcn - N_ref_offs);
        SdkLog.D("OrxFreqCarrier：ssb_cent = " + ssb_cent);
        // 空口同步用
        // KSSB、OffsetToPointA : 一个子载波间隔（SCS） = 15kHz (FDD)
        // KSSB: 单位：子载波，一个子载波间隔（SCS） = 15kHz (FDD)
        // RB_Offset: 单位：RB，1RB = 12*SCS = 12*15kHz [FR1: 15kHz, FR2(毫米波): 60kHz]
        // 载波偏移
        //double SSBOffsetToPointA = (RB_Offset*12*15 + kssb*15)/30;
        double SSBOffsetToPointA = (RB_Offset * 180 + kssb * 15) / 30;
        // 算载波频率（2.6G、3.5G）: 一个子载波间隔（SCS） = 30kHz，注：700MHz根据实际情况再做调整
        // 5G载波共有273个RB，即：273*12*30 = 92.28MHz，故：中心频率为：49.14MHz
        // SSB固定占用20RB = 20*12*30kHz = 7.2MHz
        //long Freq_carrier = uint64(ssb_cent - (SSBOffsetToPointA+10*12)*30e3 + 49.14e6);
        // |_________……_____|SSB:_10*12___+___10*12___|__KSSB*15__|__RB_offset*12*15_|PointA
        long Freq_carrier = (long) (ssb_cent - (SSBOffsetToPointA + 120) * 30000 + 49140000);
        SdkLog.D("OrxFreqCarrier：Freq_carrier = " + Freq_carrier + " [" + arfcn + ": " + kssb + ", " + RB_Offset + "]");
        return Freq_carrier;
    }

    /**
     * 回传配置空口列表数据
     */
    public List<ArfcnBeanSsb> getOrxList() {
        return DBSupportArfcn.build().getOrxList();
    }

    /**
     * 复位参数列表
     */
    public void resetGnbPara() {
        DBSupportArfcn.build().resetLock();
    }

    // 单通首最多配置8个频点
    public static final int MAX_ARFCN_NUM = 4;
}
