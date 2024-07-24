package com.dwdbsdk.Bean.DB;

import com.dwdbsdk.Bean.LteBand;
import com.dwdbsdk.Bean.NrBand;
import com.dwdbsdk.Bean.PaPkUlArfcnBean;
import com.dwdbsdk.MessageControl.MessageController;

public class ArfcnBean {
    private int DL_ARFCN, UL_ARFCN, Kssb, Rb_Offset, solt_index,Rb_Start;

    public ArfcnBean(int dl_arfcn) {
        setParam(dl_arfcn, false);
    }
    public ArfcnBean(int dl_arfcn, boolean isCount) {
        setParam(dl_arfcn, isCount);
    }
    private void setParam(int dl_arfcn, boolean isCount) {
        int board_bandwidth = 20;
        int band = 0;
        if (dl_arfcn > 100000) {
            band = NrBand.earfcn2band(dl_arfcn);
            if (band > 28) board_bandwidth = 100;
        }
        PaPkUlArfcnBean paPkUlArfcn = MessageController.build().getPaPkUlArfcn(dl_arfcn, board_bandwidth);
        DL_ARFCN = dl_arfcn;
        if (isCount) {
            if (band == 1) {
                UL_ARFCN = (DL_ARFCN - 360 - paPkUlArfcn.getPa() * 36 - paPkUlArfcn.getPk() * 3) - (190 * 1000 / 5);
            } else if (band == 28) {
                UL_ARFCN = (DL_ARFCN - 360 - paPkUlArfcn.getPa() * 36 - paPkUlArfcn.getPk() * 3) - (55 * 1000 / 5);
            }
        } else UL_ARFCN = paPkUlArfcn.getUl_arfcn();

        this.Kssb = paPkUlArfcn.getPk();
        this.Rb_Offset = paPkUlArfcn.getPa();
        this.solt_index = paPkUlArfcn.getSlot_index();
    }

    public ArfcnBean(int dl_arfcn, int board_bandwidth) {
        PaPkUlArfcnBean paPkUlArfcn = MessageController.build().getPaPkUlArfcn(dl_arfcn, board_bandwidth);
        this.DL_ARFCN = dl_arfcn;
        this.UL_ARFCN = paPkUlArfcn.getUl_arfcn();
        this.Kssb = paPkUlArfcn.getPk();
        this.Rb_Offset = paPkUlArfcn.getPa();
        this.solt_index = paPkUlArfcn.getSlot_index();
    }

    public ArfcnBean(int ARFCN, int Pa, int Pk) {
        this.DL_ARFCN = ARFCN;
        this.UL_ARFCN = ARFCN;
        int band;
        if (ARFCN <= 70655 && ARFCN >= 0) {
            band = LteBand.earfcn2band(ARFCN);
            if (band < 33 || band > 53) UL_ARFCN = DL_ARFCN + LteBand.getUlAddNumByBand(band);
            this.Kssb = 0;
            this.Rb_Start = 0;
            this.solt_index = 0;
        } else {
            band = NrBand.earfcn2band(ARFCN);
            if (band == 1) UL_ARFCN = (DL_ARFCN - 360 - Pa * 36 - Pk * 3) - (190 * 1000 / 5);
            else if (band == 28) UL_ARFCN = (DL_ARFCN - 360 - Pa * 36 - Pk * 3) - (55 * 1000 / 5);
            switch (band) {
                case 1:
                    //427250
                    this.Kssb = 6;
                    this.Rb_Start = 23;
                    this.solt_index = 9;
                    if (DL_ARFCN == 422890 || DL_ARFCN == 422930) { //422930
                        this.Kssb = 2;
                        this.Rb_Start = 12;
                    }
                    break;
                case 28:
                    this.Kssb = 6; //
                    this.Rb_Start = 23;
                    this.solt_index = 9;
                    if (DL_ARFCN == 152890) this.Rb_Start = 22;
                    break;
                case 41:
                    this.Kssb = 6;
                    this.Rb_Start = 30;
                    this.solt_index = 19;
                    break;
                case 78:
                    this.Kssb = 12;
                    this.Rb_Start = 24;
                    this.solt_index = 19;
                    break;
                case 79:
                    this.Kssb = 14;
                    this.Rb_Start = 254;
                    this.solt_index = 19;
                    break;
            }
        }
    }

    public int getRb_Start(int Arfcn, int Pa, int Pk,int rb_Start) {
        if (Arfcn>99999){  //nr
            int band = NrBand.earfcn2band(Arfcn);
            if(band == 1||band == 28){  //fdd
                Rb_Start = ((Pa*12+Pk)+120-(22-rb_Start)*12)/12;
            }else {
                if (band == 79){
                    Rb_Start = ((Pa*12+Pk)/2+120-(10-rb_Start)*12)/12;
                }else {
                    Rb_Start = ((Pa*12+Pk)/2+120-(22-rb_Start)*12)/12;
                }
            }
            return Rb_Start;
        }else {
            return rb_Start;
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
