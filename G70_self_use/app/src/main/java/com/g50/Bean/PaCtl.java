/**
 * PA控制口
 * <p>
 * 不同PA控制逻辑不一样，需硬件工程师供，参看SDK开发文档附录一表格
 */
package com.g50.Bean;

import com.Logcat.APPLog;
import com.Logcat.SLog;
import com.Util.Util;
import com.g50.G50Activity;
import com.nr70.Arfcn.Bean.NrBand;
import com.nr70.Gnb.Bean.PaBean;
import com.nr70.Socket.MessageControl.MessageController;

public class PaCtl {
    private static PaCtl instance;
    private boolean isVehicle = false;

    public static PaCtl build() {
        synchronized (PaCtl.class) {
            if (instance == null) {
                instance = new PaCtl();
            }
        }
        return instance;
    }

    public PaCtl() {
        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0);
    }

    public boolean isVehicle() {
        return isVehicle;
    }

    public void setVehicle(boolean vehicle) {
        isVehicle = vehicle;

    }

    /**
     * N1|N41: PA通道一
     * N28|N78|N79: PA通道二
     *
     * @param arfcn
     */
    public void openPA(String arfcn, int key) {
        int band = 0;
        if (Util.onlyNumeric(arfcn)) {
            band = NrBand.earfcn2band(Integer.valueOf(arfcn));
        }
        switch (band) {
            case 1:
                if (key == 0) { //g70
                    PaBean.build().setPaGpio(1, 0, 0, 3, 1, 0, 0, 0);//
                } else if (key == 1) { //g73
                    PaBean.build().setPaGpio(1, 0, 0, 0, 1, 0, 1, 0);//G73
                } else if (key == 2) { //c70
                    PaBean.build().setPaGpio(2, 0, 0, 2, 1, 0, 0, 0);//C70
                }
                break;

            case 28:
                if (key == 0) { //g70
                    PaBean.build().setPaGpio(0, 1, 1, 0, 0, 2, 0, 3);
                } else if (key == 1) { //g73
                    PaBean.build().setPaGpio(0, 1, 1, 0, 0, 2, 0, 3);
                } else if (key == 2) { //c70
                    PaBean.build().setPaGpio(0, 2, 1, 0, 0, 2, 2, 2);//C70
                }
                break;
            case 41:
                if (key == 0) { //g70
                    PaBean.build().setPaGpio(1, 0, 0, 3, 2, 0, 0, 0);
                } else if (key == 1) { //g73
                    PaBean.build().setPaGpio(4, 0, 0, 3, 2, 0, 1, 0);
                } else if (key == 2) { //c70
                    PaBean.build().setPaGpio(1, 0, 0, 3, 2, 0, 0, 0);
                }
                break;
            case 78:
                if (key == 0) { //g70
                    PaBean.build().setPaGpio(0, 1, 2, 0, 0, 2, 0, 3);
                } else if (key == 1) { //g73
                    PaBean.build().setPaGpio(0, 1, 2, 0, 0, 2, 0, 3);
                } else if (key == 2) { //c70
                    PaBean.build().setPaGpio(0, 1, 2, 0, 0, 2, 2, 3);//C70
                }
                break;
            case 79:
                if (key == 0) { //g70
                    PaBean.build().setPaGpio(0, 1, 2, 0, 0, 1, 0, 3);
                } else if (key == 1) { //g73
//                    PaBean.build().setPaGpio( 2,1,0 ,2,0,0,3,4);
                    PaBean.build().setPaGpio(0, 1, 2, 0, 0, 1, 0, 3);
                } else if (key == 2) { //c70
                    PaBean.build().setPaGpio(0, 2, 2, 0, 0, 1, 3, 2); //C70
                }
                break;
        }
        APPLog.I("openPA: " + PaBean.build().toString());
        MessageController.build().setGnbPaGpio();
    }

    //初始化空口
    public void initPA(String arfcn, int cell) {
        if (isVehicle) {
            return;
        }
        int band = 0;
        if (Util.onlyNumeric(arfcn)) {
            band = NrBand.earfcn2band(Integer.parseInt(arfcn));
        }

        switch (band) {
            case 1:
                PaBean.build().setPaGpio(1,0,0,0,1,0,1,0);    //B97502
                break;
            case 3:
                PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                break;
            case 41:
                PaBean.build().setPaGpio(2,0,0,2,2,0,1,0);    //B97501
                break;
            case 28:
                PaBean.build().setPaGpio(0,1,1,0,0,2,0,2);    //B97501
                break;
            case 78:
                PaBean.build().setPaGpio(0,1,2,0,0,2,0,2);    //B97502
                break;
            case 79:
                PaBean.build().setPaGpio(0,1,2,0,0,1,0,2);    //B97501
                break;
        }

        SLog.D("initPA: " + PaBean.build().toString());
        MessageController.build().setGnbPaGpio();
    }

    public void closePA() {
        PaBean.build().setPaGpio(2, 2, 2, 2, 2, 2, 2, 2);
        APPLog.D("closePA: " + PaBean.build().toString());
        MessageController.build().setGnbPaGpio();
    }
}
