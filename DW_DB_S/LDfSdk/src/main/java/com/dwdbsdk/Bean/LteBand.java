/**
 * 5G频点算BADN
 */
package com.dwdbsdk.Bean;

public class LteBand {
    public static final int NMM_NUM_RADIO_BAND = 30;

    public static int earfcn2band(int arfcn) {
        for (int i = 0; i < NMM_NUM_RADIO_BAND; i++) {
            //min DL, max DL
            if (arfcn >= nmm_earfcn_to_freq_lut[i][3] && arfcn <= nmm_earfcn_to_freq_lut[i][4]) {
                return (int) nmm_earfcn_to_freq_lut[i][0];
            }
        }
        return 0;
    }

    public static int earfcn2freq(int earfcn) {
        for (int i = 0; i < NMM_NUM_RADIO_BAND; i++)
            if ((earfcn >= nmm_earfcn_to_freq_lut[i][3]) && (earfcn <= nmm_earfcn_to_freq_lut[i][4]))
                //Fdl = (Fdl_low + earfcn - Noffsdl)/10
                return (int) (nmm_earfcn_to_freq_lut[i][1] * 10 + earfcn - nmm_earfcn_to_freq_lut[i][3]) / 10;
        return 0;
    }

    public static int getUlAddNumByBand(int band) {
        for (int i = 0; i < NMM_NUM_RADIO_BAND; i++) {
            //min DL, max DL
            if (band == nmm_earfcn_to_freq_lut[i][0]) {
                return (int) (nmm_earfcn_to_freq_lut[i][7] - nmm_earfcn_to_freq_lut[i][2]);
            }
        }
        return 18000;
    }

    public static double F_dl_low(int arfcn) {
        for (int i = 0; i < NMM_NUM_RADIO_BAND; i++) {
            //min DL, max DL
            if (arfcn >= nmm_earfcn_to_freq_lut[i][3] && arfcn <= nmm_earfcn_to_freq_lut[i][4]) {
                return nmm_earfcn_to_freq_lut[i][1];
            }
        }
        return 0;
    }

    public static double N_offs_dl(int arfcn) {
        for (int i = 0; i < NMM_NUM_RADIO_BAND; i++) {
            //min DL, max DL
            if (arfcn >= nmm_earfcn_to_freq_lut[i][3] && arfcn <= nmm_earfcn_to_freq_lut[i][4]) {
                return nmm_earfcn_to_freq_lut[i][2];
            }
        }
        return 0;
    }


    private static final double[][] nmm_earfcn_to_freq_lut = {
            /* BAND FDL_low [1MHz], Noffs-dl min DL, max DL, FUL_low [MHz], Noffs-ul min UL, max UL */
            /* 1, */{1, 2110, 0, 0, 599, 1920, 18000, 18000, 18599},
            /* 2, */{2, 1930, 600, 600, 1199, 1850, 18600, 18600, 19199},
            /* 3, */{3, 1805, 1200, 1200, 1949, 1710, 19200, 19200, 19949},
            /* 4, */{4, 2110, 1950, 1950, 2399, 1710, 19950, 19950, 20399},
            /* 5, */{5, 869, 2400, 2400, 2649, 824, 20400, 20400, 20649},
            /* 6, */{6, 875, 2650, 2650, 2749, 830, 20650, 20650, 20749},
            /* 7, */{7, 2620, 2750, 2750, 3449, 2500, 20750, 20750, 21449},
            /* 8, */{8, 925, 3450, 3450, 3799, 880, 21450, 21450, 21799},
            /* 9, */{9, 1844.9, 3800, 3800, 4149, 1749.9, 21800, 21800, 22149},
            /* 10, */{10, 2110, 4150, 4150, 4749, 1710, 22150, 22150, 22749},
            /* 11, */{11, 1475.9, 4750, 4750, 4949, 1427.9, 22750, 22750, 22949},
            /* 12, */{12, 729, 5010, 5010, 5179, 699, 23010, 23010, 23179},
            /* 13, */{13, 746, 5180, 5180, 5279, 777, 23180, 23180, 23279},
            /* 14, */{14, 758, 5280, 5280, 5379, 788, 23280, 23280, 23379},
            /* 17, */{17, 734, 5730, 5730, 5849, 704, 23730, 23730, 23849},
            /* 18, */{18, 860, 5850, 5850, 5999, 815, 23850, 23999},
            /* 19, */{19, 875, 6000, 6000, 6149, 830, 24000, 24000, 24149},
            /* 20, */{20, 791, 6150, 6150, 6449, 832, 24150, 24150, 24449},
            /* 21, */{21, 1495.9, 6450, 6450, 6599, 1447.9, 24450, 24450, 24599},
            /* 33, */{33, 1900, 36000, 36000, 36199, 1900, 36000, 36000, 36199},
            /* 34, */{34, 2010, 36200, 36200, 36349, 2010, 36200, 36200, 36349},
            /* 35, */{35, 1850, 36350, 36350, 36949, 1850, 36350, 36350, 36949},
            /* 36, */{36, 1930, 36950, 36950, 37549, 1930, 36950, 36950, 37549},
            /* 37, */{37, 1910, 37550, 37550, 37749, 1910, 37550, 37550, 37749},
            /* 38, */{38, 2570, 37750, 37750, 38249, 2570, 37750, 37750, 38249},
            /* 39, */{39, 1880, 38250, 38250, 38649, 1880, 38250, 38250, 38649},
            /* 40, */{40, 2300, 38650, 38650, 39649, 2300, 38650, 38650, 39649},
            /* 41, */{41, 2496, 39650, 39650, 41589, 2496, 39650, 39650, 41589},
            /* 42,*/ {42, 3400, 41590, 41590, 43589, 3400, 41590, 41590, 43589},
            /* 66,*/ {66, 2110, 66436, 66436, 67335, 1710, 131972, 131972, 132671}
    };
}
