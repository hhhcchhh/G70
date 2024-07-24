package com.simdo.dw_4db_s.Bean;


import com.dwdbsdk.Bean.NrBand;

public class ArfcnBean {
    private int DL_ARFCN, UL_ARFCN, Kssb, Rb_Offset, solt_index;

    public ArfcnBean(int ARFCN) {
        this.DL_ARFCN = ARFCN;
        this.UL_ARFCN = ARFCN;
        if (ARFCN<=70655&&ARFCN>=0){
            if (ARFCN<=9919){
                UL_ARFCN = UL_ARFCN + 18000;
            }
            this.Kssb = 0;
            this.Rb_Offset = 0;
            this.solt_index = 0;
        }else {
            int band = NrBand.earfcn2band(ARFCN);
            if (band == 1) {
                if (DL_ARFCN == 422890 || DL_ARFCN == 422930) { //422930
                    UL_ARFCN = UL_ARFCN - 38798;
                } else {
                    UL_ARFCN = UL_ARFCN - 38000;
                }
                /*
                *
                * UL_ARFCN = UL_ARFCN - 360 - var2 * 36 - var3 * 3 - 38000;
                *
                * */
            } else if (band == 28) {
                UL_ARFCN = 140720;//154810  152890 154810
            }
            switch (band) {
                case 1:
                    //427250
                    this.Kssb = 6;
                    this.Rb_Offset = 23;
                    this.solt_index = 9;
                    if (DL_ARFCN == 422890 || DL_ARFCN == 422930) { //422930
                        this.Kssb = 2;
                        this.Rb_Offset = 12;
                    }
                    break;

                case 28:
                    this.Kssb = 6; //
                    this.Rb_Offset = 23;
                    this.solt_index = 9;
                    if (DL_ARFCN == 152890) {
                        this.Rb_Offset = 22;
                    }

                    break;

                case 41:
                    this.Kssb = 6;
                    this.Rb_Offset = 30;
                    this.solt_index = 19;
                    break;

                case 78:
                    this.Kssb = 12;
                    this.Rb_Offset = 24;
                    this.solt_index = 19;
                    break;

                case 79:
                    this.Kssb = 14;
                    this.Rb_Offset = 254;
                    this.solt_index = 19;
                    break;
            }
        }

    }

    public int getDLArfcn() {
        return DL_ARFCN;
    }

    public int getULArfcn() {
        return UL_ARFCN;
    }

    public int getKssb() {
        return Kssb;
    }

    public int getSoltIndex() {
        return solt_index;
    }

    public int getRbOffset() {
        return Rb_Offset;
    }
}
