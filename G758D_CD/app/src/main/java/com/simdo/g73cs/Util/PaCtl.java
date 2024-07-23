/**
 * PA控制口
 * <p>
 * 不同PA控制逻辑不一样，需硬件工程师供，参看SDK开发文档附录一表格
 */
package com.simdo.g73cs.Util;

import com.Logcat.SLog;
import com.nr.Arfcn.Bean.LteBand;
import com.nr.Arfcn.Bean.NrBand;
import com.nr.Gnb.Bean.PaBean;
import com.nr.Socket.MessageControl.MessageController;

public class PaCtl {
    private static PaCtl instance;
    private boolean isVehicle = false;
    /*
     * isB97502
     *   1、true:B97502 2w功放
     *   2、false:B97501 1w功放
     * 此处修改记得同步修改
     *   1、app build.gradle 下的 isB97502
     *   2、资源 value 下的 app_name
     * */
    public final boolean isB97502 = true;

    public static PaCtl build() {
        synchronized (PaCtl.class) {
            if (instance == null) {
                instance = new PaCtl();
            }
        }
        return instance;
    }

    public PaCtl() {
        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    /**
     * 至关重要，应用启动必须先调用此方法设置工作模式，以控制后续业务的功放
     *
     */
    public Boolean isVehicle() {
        return isVehicle;
    }

    public void setMode(boolean isVehicle) {
        this.isVehicle = isVehicle;
    }

    /**
     * N1|N41: PA通道一
     * N28|N78|N79: PA通道二
     *
     * @param arfcn 频点
     */
    public void initPA(String id, String arfcn) {
        AppLog.I("initPA id = " + id + ", arfcn = " + arfcn);
        int band = 0;
        if (Util.onlyNumeric(arfcn)) {
            band = NrBand.earfcn2band(Integer.parseInt(arfcn));
        }
        switch (band) {
            case 1:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 41:
                if (isB97502)
                    PaBean.build().setPaGpio(2, 1, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 0, 1, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 28:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 78:
                if (isB97502)
                    PaBean.build().setPaGpio(2, 1, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(2, 1, 0, 0, 0, 0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 79:
                if (isB97502)
                    PaBean.build().setPaGpio(2, 1, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(2, 1, 0, 0, 0, 0, 0, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
        }
        AppLog.D("initPA: " + PaBean.build().toString());
        MessageController.build().setGnbTFPaGpio(id);
    }

    /**
     * B34|B38|B39|B40|B41: PA通道一
     * B1|B3|B5|B8|B34|B38|B39|B40|B41: PA通道二
     *
     * @param arfcn 频点
     */
    public void initLtePA(String id, String arfcn, int cellId) {
        AppLog.I("initLtePA id = " + id + ", arfcn = " + arfcn + ", cellId = " + cellId);
        int band = 0;
        if (Util.onlyNumeric(arfcn)) {
            band = LteBand.earfcn2band(Integer.parseInt(arfcn));
        }
        switch (band) {
            case 1:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 3:
                if (isB97502) {
                    if (cellId == 3) //通道四开B3次频点
                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                    else
                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                } else
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 5:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 8:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 34:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 38:
            case 41:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 39:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 40:
                if (isB97502) {
                    if (cellId == 1) //通道二开B40次频点
                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                    else
                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 2, 1, 0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                } else
                    PaBean.build().setPaGpio(0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
        }
        AppLog.D("initLtePA: " + PaBean.build().toString());
        MessageController.build().setGnbTFPaGpio(id);
    }

    /**
     * N1|N41: PA通道一
     * N28|N78|N79: PA通道二
     *
     * @param arfcn
     */
    public void openPA(String id, String arfcn) {
        AppLog.I("openPA id = " + id + ", arfcn = " + arfcn);
        int band = 0;
        if (Util.onlyNumeric(arfcn)) {
            band = NrBand.earfcn2band(Integer.parseInt(arfcn));
        }
        switch (band) {
            case 1:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 41:
                if (isB97502)
                    PaBean.build().setPaGpio(3, 1, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 0, 1, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 28:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 78:
                if (isB97502)
                    PaBean.build().setPaGpio(3, 1, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(3, 1, 0, 0, 0, 0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 79:
                if (isB97502)
                    PaBean.build().setPaGpio(3, 1, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(3, 1, 0, 0, 0, 0, 0, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
        }
        AppLog.D("openPA: " + PaBean.build().toString());
        MessageController.build().setGnbTFPaGpio(id);
    }

    /**
     * B34|B38|B39|B40|B41: PA通道一
     * B1|B3|B5|B8|B34|B38|B39|B40|B41: PA通道二
     *
     * @param arfcn 频点
     */
    public void openLtePA(String id, String arfcn, int cellId) {
        AppLog.I("openLtePA id = " + id + ", arfcn = " + arfcn);
        int band = 0;
        if (Util.onlyNumeric(arfcn)) {
            band = LteBand.earfcn2band(Integer.parseInt(arfcn));
        }
        switch (band) {
            case 1:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 3:
                if (isB97502) {
                    if (cellId == 3) //通道四开B3次频点
                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                    else
                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                } else
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 5:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 8:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 34:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 2, 1, 1, 0, 0, 1, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 1, 0, 3, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 38:
            case 41:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 0, 0, 1, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 1, 0, 3, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 39:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 2, 1, 2, 0, 0, 1, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 1, 0, 3, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 40:
                if (isB97502) {
                    if (cellId == 1) //通道二开B40次频点
                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                    else
                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 2, 2, 1, 0, 0, 1, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                } else
                    PaBean.build().setPaGpio(0, 0, 1, 0, 3, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
        }
        AppLog.D("openLtePA: " + PaBean.build().toString());
        MessageController.build().setGnbTFPaGpio(id);
    }

    public void closePA(String id) {
        AppLog.I("closePA id = " + id);
        PaBean.build().setPaGpio(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2);
        AppLog.D("closePA: " + PaBean.build().toString());
        MessageController.build().setGnbTFPaGpio(id);
    }

    public void closeLtePA(String id) {
        AppLog.I("closePA id = " + id);
        //if (PaBean.build().getGpio1_en1() == 2) PaBean.build().setPaGpio(2, 0, 0, 0, 0, 0, 0, 0);
        //else PaBean.build().setPaGpio(0, 2, 0, 0, 0, 0, 0, 2);
        PaBean.build().setPaGpio(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2);
        AppLog.D("closePA: " + PaBean.build().toString());
        MessageController.build().setGnbTFPaGpio(id);
    }

    public void openFreqPA(String id, String arfcn) {
        AppLog.I("openFreqPA id = " + id+" arfcn = "+arfcn);
        int band = 0;
        if (Util.onlyNumeric(arfcn)) {
            band = NrBand.earfcn2band(Integer.parseInt(arfcn));
        }
        switch (band) {
            case 1:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 2, 2, 0, 0, 2, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0); //G735D FDD扫频不走功放，走ORX4
                else
                    PaBean.build().setPaGpio(0, 2, 2, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 41:
                if (isB97502)
                    PaBean.build().setPaGpio(2, 1, 2, 1, 2, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0); //G735D
                else
                    PaBean.build().setPaGpio(0, 2, 2, 1, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 28:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 2, 2, 0, 0, 1, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0); //G735D FDD扫频不走功放，走ORX4
                else
                    PaBean.build().setPaGpio(0, 2, 2, 2, 0, 2, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 78:
                if (isB97502)
                    PaBean.build().setPaGpio(2, 1, 2, 2, 1, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0); //G735D
                else
                    PaBean.build().setPaGpio(2, 1, 2, 2, 0, 2, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 79:
                if (isB97502)
                    PaBean.build().setPaGpio(2, 1, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0); //G735D
                else
                    PaBean.build().setPaGpio(2, 1, 2, 2, 0, 2, 0, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
        }
        AppLog.D("openFreqPA: " + PaBean.build().toString());
        MessageController.build().setGnbTFPaGpio(id);
    }

    public void openFreqLtePA(String id, String arfcn) {
        AppLog.I("openFreqPA id = " + id+" arfcn = "+arfcn);
        int band = 0;
        if (Util.onlyNumeric(arfcn)) {
            band = LteBand.earfcn2band(Integer.parseInt(arfcn));
        }
        switch (band) {
            case 1:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 2, 2, 0, 0, 2, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0); //G735D FDD扫频不走功放，走ORX4
                else
                    PaBean.build().setPaGpio(0, 2, 2, 2, 0, 2, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 3:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 2, 1, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0); //G735D FDD扫频不走功放，走ORX4
                else
                    PaBean.build().setPaGpio(0, 2, 2, 2, 0, 2, 2, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 5:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 1, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0); //G735D FDD扫频不走功放，走ORX4
                else
                    PaBean.build().setPaGpio(0, 2, 2, 2, 0, 2, 2, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 8:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 1, 1, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0); //G735D FDD扫频不走功放，走ORX4
                else
                    PaBean.build().setPaGpio(0, 2, 2, 2, 0, 2, 2, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 34:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 2, 2, 0, 0, 0, 0, 2, 1, 1, 0, 0, 1, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0); //G735D
                else
                    PaBean.build().setPaGpio(0, 2, 1, 2, 2, 2, 0, 0, 0, 0, 1, 1, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 38:
            case 41:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 2, 2, 0, 0, 0, 0, 2, 2, 2, 0, 0, 1, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0); //G735D
                else
                    PaBean.build().setPaGpio(0, 2, 1, 2, 2, 2, 0, 0, 0, 0, 2, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 39:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 2, 2, 0, 0, 0, 0, 2, 1, 2, 0, 0, 1, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0); //G735D
                else
                    PaBean.build().setPaGpio(0, 2, 1, 2, 2, 2, 0, 0, 0, 0, 1, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 40:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 2, 2, 0, 0, 0, 0, 2, 2, 1, 0, 0, 1, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0); //G735D
                else
                    PaBean.build().setPaGpio(0, 2, 1, 2, 2, 2, 0, 0, 0, 0, 2, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
        }
        AppLog.D("openFreqLtePA: " + PaBean.build().toString());
        MessageController.build().setGnbTFPaGpio(id);
    }

    //根据频点关功放
    public void closePAByArfcn(String id, boolean isLte, String arfcn) {
        AppLog.D("closePAByArfcn: id" + id+" isLte"+isLte+" arfcn"+arfcn);
        int band = 0;
        if (isLte) {
            band = LteBand.earfcn2band(Integer.parseInt(arfcn));
            switch (band) {
                case 1:
                case 3:
                case 5:
                case 8:
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //G735D
                    break;
                case 34:
                case 38:
                case 41:
                case 39:
                case 40:
                    PaBean.build().setPaGpio(0, 0, 2, 0, 2, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);    //G735D
                    break;
            }
        } else {
            band = NrBand.earfcn2band(Integer.parseInt(arfcn));
            switch (band) {
                case 1:
                    PaBean.build().setPaGpio(0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //G735D
                    break;
                case 41:
                    PaBean.build().setPaGpio(0, 0, 0, 2, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //G735D
                    break;
                case 28:
                    PaBean.build().setPaGpio(0, 2, 0, 0, 0, 0, 0, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //G735D
                    break;
                case 78:
                case 79:
                    PaBean.build().setPaGpio(2, 2, 0, 0, 0, 0, 0, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //G735D
                    break;
            }
        }

        AppLog.D("closePAByArfcn: " + PaBean.build().toString());
        MessageController.build().setGnbTFPaGpio(id);
    }

    public void openPAByBand(String id, String band) {
        AppLog.D("openPAByBand: id" + id + " band" + band);
        switch (band) {
            // 4G
            case "B1":
                PaBean.build().setPaGpio(0, 2, 2, 2, 0, 2, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case "B3":
                PaBean.build().setPaGpio(0, 2, 2, 2, 0, 2, 2, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case "B5":
                PaBean.build().setPaGpio(0, 2, 2, 2, 0, 2, 2, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case "B8":
                PaBean.build().setPaGpio(0, 2, 2, 2, 0, 2, 2, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case "B34":
                PaBean.build().setPaGpio(0, 2, 1, 2, 2, 2, 0, 0, 0, 0, 1, 1, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case "B39":
                PaBean.build().setPaGpio(0, 2, 1, 2, 2, 2, 0, 0, 0, 0, 1, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case "B40":
                PaBean.build().setPaGpio(0, 2, 1, 2, 2, 2, 0, 0, 0, 0, 2, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case "B41":
                PaBean.build().setPaGpio(0, 2, 1, 2, 2, 2, 0, 0, 0, 0, 2, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            // 5G
            case "N1":
                PaBean.build().setPaGpio(0, 2, 2, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case "N28":
                PaBean.build().setPaGpio(0, 2, 2, 2, 0, 2, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case "N41":
                PaBean.build().setPaGpio(0, 2, 2, 1, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case "N78":
                PaBean.build().setPaGpio(2, 1, 2, 2, 0, 2, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case "N79":
                PaBean.build().setPaGpio(2, 1, 2, 2, 0, 2, 0, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
        }

        AppLog.D("openPAByBand: band = " + band + ", " + PaBean.build().toString());
        MessageController.build().setGnbTFPaGpio(id);
    }
}
