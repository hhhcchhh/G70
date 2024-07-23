package com.nr.Arfcn.Bean;

import com.Logcat.SLog;

public class ArfcnConvert {
    /**
     * 5G频点转4G频点
     * @param arfcn
     * @return
     */
    public static int parse5to4(String arfcn) {
        StringBuilder asb = new StringBuilder();
        float ΔFGloba;
        float FREF_Offs;
        float NREF_Offs;
        int ARFCN = Integer.valueOf(arfcn);//633984;629952[42082-3];633984[42688]
        asb.append("[ ARFCN: " + ARFCN);
        asb.append(",");
        if (ARFCN > 0 && ARFCN <= 599999) {
            ΔFGloba = 0.005f;
            FREF_Offs = 0;
            NREF_Offs = 0;
        } else if (ARFCN > 599999 && ARFCN <= 2016666) {
            ΔFGloba = 0.015f;
            FREF_Offs = 3000;
            NREF_Offs = 600000;
        } else {
            ΔFGloba = 0.06f;
            FREF_Offs = 24250.08f;
            NREF_Offs = 2016667;
        }
        asb.append("ΔFGloba: " + ΔFGloba);
        asb.append(",");
        asb.append("FREF_Offs: " + FREF_Offs);
        asb.append(",");
        asb.append("NREF_Offs: " + NREF_Offs);
        asb.append(",");
        float f5g = FREF_Offs + ΔFGloba*(ARFCN - NREF_Offs);
        asb.append("NR[C-FRE]: " + f5g);
        asb.append(",");
        int ifdl_low, inoffs_dl;
        if (f5g >= 2496 && f5g < 2570) { //TDD: 2515--2570
            ifdl_low = 2496;
            inoffs_dl = 39650;
        } else if (f5g >= 2570 && f5g < 2675) { // TDD:2570 --2675
            ifdl_low = 2570;
            inoffs_dl = 37750;//67836
        } else if (f5g >= 3400 && f5g < 3550) { // FDD: 3400--3550
            ifdl_low = 3400;
            inoffs_dl = 41590;
        } else { // FDD: 3550--3600
            ifdl_low = 3550;
            inoffs_dl = 55240;//55240 56740
        }
        asb.append("ifdl_low: " + ifdl_low);
        asb.append(",");
        asb.append("inoffs_dl: " + inoffs_dl);
        asb.append(",");

        float ndl = (f5g - ifdl_low) * 10 + inoffs_dl;
        asb.append("ARFCN-L: " + ndl);
        asb.append(",");
        int indl = (int) Math.abs(ndl);
        float oValue = ndl - indl;
        if (oValue >= 0.5) { // 四舍五入
            indl += 1;
            int ret = indl % 2;
            if (ret != 0) {
                indl -= 1; // 取偶数
            }
        }
        asb.append("(" + indl);
        asb.append(")]");
        SLog.D("ArfcnConvert: " + asb.toString());
        return indl;
    }
    /**
     * 根据5G频点算中心频率
     *
     * @param arfcn
     *      5G频点
     * @return
     *      中心频率
     */
    private static float calaMidFrq(int arfcn) {
        float ΔFGloba;
        float FREF_Offs;
        float NREF_Offs;
        if (arfcn > 0 && arfcn <= 599999) {
            ΔFGloba = 0.005f;
            FREF_Offs = 0;
            NREF_Offs = 0;
        } else if (arfcn > 599999 && arfcn <= 2016666) {
            ΔFGloba = 0.015f;
            FREF_Offs = 3000;
            NREF_Offs = 600000;
        } else {
            ΔFGloba = 0.06f;
            FREF_Offs = 24250.08f;
            NREF_Offs = 2016667;
        }
        float f5g = FREF_Offs + ΔFGloba*(arfcn - NREF_Offs);
        return f5g;
    }

    /**
     * 根据中心频点算GSCN
     *
     * @param f5g
     *      中心频点,所有数据单位为: kHz
     * @param type
     *      频点类型
     * @return
     *      gscn
     */
    private static float calcGSCN(float f5g, int type) {
        float n, gscn;
        if (type == 0) {
            //M={1 3 5} GSCN = 3N + (M-3)/2, 以下公式 m = 3
            // f5g = n*1200 + m*50
            n = (f5g*1000 - 150)/1200;
            gscn = 3 * n;
        } else if (type == 1) {
            // f5g = n*1440 + 3000000
            n = (f5g*1000 - 3000000)/1440;
            gscn = 7499 + n;
        } else {
            // f5g = n*17280 + 24250080
            n = (f5g*1000 - 24250080)/17280;
            gscn = 22256 + n;
        }
        return gscn;
    }

    /**
     *  根据频点获取GSCN
     *
     * @param arfcn
     * @return
     */
    public static int getGSCN(int arfcn) {
        float gscn;
        int type;
        if (arfcn > 0 && arfcn <= 599999) {
            type = 0;
        } else if (arfcn > 599999 && arfcn <= 2016666) {
            type = 1;
        } else {
            type = 2;
        }
        return (int)calcGSCN(calaMidFrq(arfcn), type);
    }
}
