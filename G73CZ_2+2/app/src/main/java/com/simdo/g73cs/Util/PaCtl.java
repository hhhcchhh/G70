/**
 * PA控制口
 * <p>
 * 不同PA控制逻辑不一样，需硬件工程师供，参看SDK开发文档附录一表格
 */
package com.simdo.g73cs.Util;

import com.nr.Arfcn.Bean.LteBand;
import com.nr.Arfcn.Bean.NrBand;
import com.nr.Gnb.Bean.PaBean;
import com.nr.Socket.MessageControl.MessageController;

public class PaCtl {
    private static PaCtl instance;
    private boolean isVehicle = true;
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
        PaBean.build().setPaGpio(2, 2, 2, 2, 2, 2, 2, 2);
    }

    /**
     * 至关重要，应用启动必须先调用此方法设置工作模式，以控制后续业务的功放
     *
     * @param
     */
    public Boolean isVehicle() {
        return isVehicle;
    }

    public void setMode(boolean isVehicle) {
        this.isVehicle = isVehicle;
    }

    /**
     * N1: PA通道一
     * N41: PA通道二
     * N78|N79: PA通道三
     * N28: PA通道四
     *
     * @param arfcn 频点
     */
    //G73CZ_2+2
    public void initPA(String id, String arfcn) {
        int band = 0;
        if (Util.onlyNumeric(arfcn)) {
            band = NrBand.earfcn2band(Integer.parseInt(arfcn));
        }
        switch (band) {
            case 1:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 1, 1, 1, 1);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 41:
                if (isB97502)
                    PaBean.build().setPaGpio(1, 0, 0, 2, 1, 1, 1, 1);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 0, 1, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 28:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 1, 1, 1, 1);    //B97502
                else
                    PaBean.build().setPaGpio(0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 78:
                if (isB97502)
                    PaBean.build().setPaGpio(1, 0, 0, 2, 1, 1, 1, 1);    //B97502
                else
                    PaBean.build().setPaGpio(2, 1, 0, 0, 0, 0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 79:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 1, 2, 0, 1,1,1,1);    //B97502
                else
                    PaBean.build().setPaGpio(2, 1, 0, 0, 0, 0, 0, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
        }
        AppLog.I("initPA: id = " + id + ", arfcn = " + arfcn + ", PaBean = " + PaBean.build().toString());
        MessageController.build().setGnbPaGpio(id); //G73   setGnbTFPaGpio  //G758
    }

    /**
     * B3|B34: PA通道一
     * B5|B39: PA通道二
     * B8|B40: PA通道三
     * B1|B41: PA通道四
     *
     * @param arfcn 频点
     */
    public void initLtePA(String id, String arfcn, int cellId) {
//        int band = 0;
//        if (Util.onlyNumeric(arfcn)) {
//            band = LteBand.earfcn2band(Integer.parseInt(arfcn));
//        }
//        switch (band) {
//            case 1:
//                if (isB97502)
//                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0);    //B97502
//                else
//                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
//                break;
//            case 3:
//                if (isB97502) {
//                    if (cellId == 3) //通道四开B3次频点
//                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0);    //B97502
//                    else
//                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
//                } else
//                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
//                break;
//            case 5:
//                if (isB97502)
//                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0);    //B97502
//                else
//                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
//                break;
//            case 8:
//                if (isB97502)
//                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0);    //B97502
//                else
//                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
//                break;
//            case 34:
//                if (isB97502)
//                    PaBean.build().setPaGpio(0, 0, 1, 2, 0, 0, 0, 0);    //B97502
//                else
//                    PaBean.build().setPaGpio(0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
//                break;
//            case 38:
//            case 41:
//                if (isB97502)
//                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 1, 2);    //B97502
//                else
//                    PaBean.build().setPaGpio(0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
//                break;
//            case 39:
//                if (isB97502)
//                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 1, 2);    //B97502
//                else
//                    PaBean.build().setPaGpio(0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
//                break;
//            case 40:
//                if (isB97502) {
//                    if (cellId == 1) //通道二开B40次频点
//                        PaBean.build().setPaGpio(0, 0, 1, 2, 0, 0, 0, 0);    //B97502
//                    else
//                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 2, 1, 0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
//                } else
//                    PaBean.build().setPaGpio(0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
//                break;
//        }
        PaBean.build().setPaGpio(0, 0, 1, 2, 0, 0, 1, 2);
        AppLog.I("initLtePA: " + PaBean.build().toString());
        MessageController.build().setGnbPaGpio(id);
    }

    /**
     * N1|N41: PA通道一
     * N28|N78|N79: PA通道二
     *
     * @param arfcn
     */
    public void openPA(String id, String arfcn) {
        int band = 0;
        if (Util.onlyNumeric(arfcn)) {
            band = NrBand.earfcn2band(Integer.parseInt(arfcn));
        }
        switch (band) {
            case 1:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 1, 1, 1, 1);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 41:
                if (isB97502)
                    PaBean.build().setPaGpio(4, 0, 0, 3, 1, 1, 1, 1);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 0, 1, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 28:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 1, 1, 1, 1);    //B97502
                else
                    PaBean.build().setPaGpio(0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 78:
                if (isB97502)
                    PaBean.build().setPaGpio(4, 0, 0, 3, 1, 1, 1, 1);    //B97502
                else
                    PaBean.build().setPaGpio(3, 1, 0, 0, 0, 0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 79:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 1, 3, 0, 1, 1, 1, 1);    //B97502
                else
                    PaBean.build().setPaGpio(3, 1, 0, 0, 0, 0, 0, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
        }
        AppLog.I("openPA: id=" + id + ", arfcn=" + arfcn + ", " + PaBean.build().toString());
        MessageController.build().setGnbPaGpio(id);
    }

    /**
     * B34|B38|B39|B40|B41: PA通道一
     * B1|B3|B5|B8|B34|B38|B39|B40|B41: PA通道二
     *
     * @param arfcn 频点
     */
    public void openLtePA(String id, String arfcn, int cellId) {
        int band = 0;
        if (Util.onlyNumeric(arfcn)) {
            band = LteBand.earfcn2band(Integer.parseInt(arfcn));
        }
        switch (band) {
            case 1:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 3:
                if (isB97502) {
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                } else
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 5:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 8:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 34:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 4, 3, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 1, 0, 3, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 38:
            case 41:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 4, 3);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 1, 0, 3, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 39:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 4, 3);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 1, 0, 3, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 40:
                if (isB97502) {
                    PaBean.build().setPaGpio(0, 0, 4, 3, 0, 0, 0, 0);    //B97502
                } else
                    PaBean.build().setPaGpio(0, 0, 1, 0, 3, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
        }
        AppLog.I("openLtePA: id = " + id + ", arfcn = " + arfcn + ", cellId = " + cellId + ", PaBean = " + PaBean.build().toString());
        MessageController.build().setGnbPaGpio(id);
    }

    public void closePA(String id) {
        PaBean.build().setPaGpio(1, 2, 2, 1, 1, 1, 1, 1);
        AppLog.I("closePA: id = " + id + ", PaBean = " + PaBean.build().toString());
        MessageController.build().setGnbPaGpio(id);
    }

    public void closeLtePA(String id) {
        //if (PaBean.build().getGpio1_en1() == 2) PaBean.build().setPaGpio(2, 0, 0, 0, 0, 0, 0, 0);
        //else PaBean.build().setPaGpio(0, 2, 0, 0, 0, 0, 0, 2);
        PaBean.build().setPaGpio(0, 0, 1, 2, 0, 0, 1, 2);
        AppLog.I("closeLtePA: id = " + id + ", PaBean = " + PaBean.build().toString());
        MessageController.build().setGnbPaGpio(id);
    }

    public void openFreqPA(String id, String arfcn) {
        int band = 0;
        if (Util.onlyNumeric(arfcn)) {
            band = NrBand.earfcn2band(Integer.parseInt(arfcn));
        }
        switch (band) {
            case 1:
                if (isB97502)
                    PaBean.build().setPaGpio(1, 1, 1, 1, 1, 1, 1, 1); //G735D FDD扫频不走功放，走ORX4
                else
                    PaBean.build().setPaGpio(0, 2, 2, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 41:
                if (isB97502)
                    PaBean.build().setPaGpio(1, 1, 1, 2, 1, 1, 1, 1); //G735D
                else
                    PaBean.build().setPaGpio(0, 2, 2, 1, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 28:
                if (isB97502)
                    PaBean.build().setPaGpio(1, 2, 2, 1, 1, 1, 1, 1); //G735D FDD扫频不走功放，走ORX4
                else
                    PaBean.build().setPaGpio(0, 2, 2, 2, 0, 2, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 78:
                if (isB97502)
                    PaBean.build().setPaGpio(1, 2, 2, 2, 1, 1, 1, 1); //G735D
                else
                    PaBean.build().setPaGpio(2, 1, 2, 2, 0, 2, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 79:
                if (isB97502)
                    PaBean.build().setPaGpio(1, 1, 2, 1, 1, 1, 1, 1); //G735D
                else
                    PaBean.build().setPaGpio(2, 1, 2, 2, 0, 2, 0, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
        }
        AppLog.I("openFreqPA: id: " + id + ", arfcn: " + arfcn +  ", PaBean: " + PaBean.build().toString());
        MessageController.build().setGnbPaGpio(id);
    }

    public void openFreqLtePA(String id, String arfcn) {
        int band = 0;
        if (Util.onlyNumeric(arfcn)) {
            band = LteBand.earfcn2band(Integer.parseInt(arfcn));
        }
        switch (band) {
            case 1:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0); //G735D FDD扫频不走功放，走ORX4
                else
                    PaBean.build().setPaGpio(0, 2, 2, 2, 0, 2, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 3:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0); //G735D FDD扫频不走功放，走ORX4
                else
                    PaBean.build().setPaGpio(0, 2, 2, 2, 0, 2, 2, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 5:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0); //G735D FDD扫频不走功放，走ORX4
                else
                    PaBean.build().setPaGpio(0, 2, 2, 2, 0, 2, 2, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 8:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0); //G735D FDD扫频不走功放，走ORX4
                else
                    PaBean.build().setPaGpio(0, 2, 2, 2, 0, 2, 2, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 34:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 4, 3, 0, 0, 0, 0); //G735D
                else
                    PaBean.build().setPaGpio(0, 2, 1, 2, 2, 2, 0, 0, 0, 0, 1, 1, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 38:
            case 41:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 4, 3); //G735D
                else
                    PaBean.build().setPaGpio(0, 2, 1, 2, 2, 2, 0, 0, 0, 0, 2, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 39:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 4, 3); //G735D
                else
                    PaBean.build().setPaGpio(0, 2, 1, 2, 2, 2, 0, 0, 0, 0, 1, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 40:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 4, 3, 0, 0, 0, 0); //G735D
                else
                    PaBean.build().setPaGpio(0, 2, 1, 2, 2, 2, 0, 0, 0, 0, 2, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
        }
        AppLog.I("openFreqLtePA: id: " + id + ", arfcn: " + arfcn  +  ", PaBean: " + PaBean.build().toString());
        MessageController.build().setGnbPaGpio(id);
    }

    //根据频点关功放
    public void closePAByArfcn(String id, boolean isLte, String arfcn) {
        int band = 0;
        if (isLte) {
            band = LteBand.earfcn2band(Integer.parseInt(arfcn));
            switch (band) {
                case 3:
                case 8:
                case 34:
                case 40:
                    PaBean.build().setPaGpio(0, 0, 1, 2, 0, 0, 0, 0);
                    break;
                case 1:
                case 5:
                case 41:
                case 39:
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 1, 2);
                    break;
                case 38:
                    PaBean.build().setPaGpio(0, 0, 1, 2, 0, 0, 1, 2);
                    break;
            }
        } else {
            band = NrBand.earfcn2band(Integer.parseInt(arfcn));
            switch (band) {
                case 1:
                case 41:
                    PaBean.build().setPaGpio(1,1,1,1,1,1,1,1);
                    break;
                case 28:
                case 78:
                case 79:
                    PaBean.build().setPaGpio(1, 2, 2, 1,1,1,1,1);
                    break;
            }
        }

        AppLog.I("closePAByArfcn: id = " + id + ", isLte = " + isLte + "arfcn = " + arfcn + ", PaBean = " + PaBean.build().toString());
        MessageController.build().setGnbPaGpio(id);
    }

    public void openFreqPAByBand(String id, String band) {
        switch (band) {
            // 4G
            case "B1":
            case "B3":
            case "B5":
            case "B8":
                PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case "B34":
            case "B40":
                PaBean.build().setPaGpio(0, 0, 4, 3, 0, 0, 0, 0);
                break;
            case "B39":
            case "B41":
                PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 4, 3);
                break;
            // 5G
            case "N1":
                PaBean.build().setPaGpio(1, 1, 1, 1, 1, 1, 1, 1); //G735D FDD扫频不走功放，走ORX4
                break;
            case "N28":
                PaBean.build().setPaGpio(1, 2, 2, 1, 1, 1, 1, 1); //G735D FDD扫频不走功放，走ORX4
                break;
            case "N41":
                PaBean.build().setPaGpio(1, 1, 1, 2, 1, 1, 1, 1); //G735D
                break;
            case "N78":
                PaBean.build().setPaGpio(1, 2, 2, 2, 1, 1, 1, 1); //G735D
                break;
            case "N79":
                PaBean.build().setPaGpio(1, 1, 2, 1, 1, 1, 1, 1);
                break;
        }

        AppLog.I("openPAByBand: id = " + id + ", band = " + band + ", " + PaBean.build().toString());
        MessageController.build().setGnbPaGpio(id);
    }
    public void closeFreqPAByBand(String id, String band) {
        switch (band) {
            case "B3":
            case "B8":
            case "B34":
            case "B40":
                PaBean.build().setPaGpio(0, 0, 1, 2, 0, 0, 0, 0);
                break;
            case "B1":
            case "B5":
            case "B39":
            case "B41":
                PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 1, 2);
                break;
            case "B38":
                PaBean.build().setPaGpio(0, 0, 1, 2, 0, 0, 1, 2);
                break;
            // 5G
            case "N1":
            case "N41":
                PaBean.build().setPaGpio(1,1,1,1,1,1,1,1);
                break;
            case "N28":
            case "N78":
            case "N79":
                PaBean.build().setPaGpio(1, 2, 2, 1,1,1,1,1);
                break;
        }

        AppLog.I("closeFreqPAByBand: id = " + id + ", band = " + band + ", " + PaBean.build().toString());
        MessageController.build().setGnbPaGpio(id);
    }
}
