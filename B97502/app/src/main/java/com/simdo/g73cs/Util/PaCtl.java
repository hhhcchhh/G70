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
     * @param isVehicle 是否为车载模式
     */
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
        int band = 0;
        if (Util.onlyNumeric(arfcn)) {
            band = NrBand.earfcn2band(Integer.parseInt(arfcn));
        }
        switch (band) {
            case 1:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case 41:
                if (isB97502)
                    PaBean.build().setPaGpio(2, 1, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 0, 1, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case 28:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case 78:
                if (isB97502)
                    PaBean.build().setPaGpio(2, 1, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(2, 1, 0, 0, 0, 0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case 79:
                if (isB97502)
                    PaBean.build().setPaGpio(2, 1, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(2, 1, 0, 0, 0, 0, 0, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            default:
                PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
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

        int band = 0;
        if (Util.onlyNumeric(arfcn)) {
            band = LteBand.earfcn2band(Integer.parseInt(arfcn));
        }
        switch (band) {
            case 1:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case 3:
                if (isB97502){
                    if (cellId == 3) //通道四开B3次频点
                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                    else
                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                }
                else
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case 5:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case 8:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case 34:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case 38:
            case 41:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case 39:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case 40:
                if (isB97502){
                    if (cellId == 1) //通道二开B40次频点
                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                    else 
                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 2, 1, 0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                }
                else
                    PaBean.build().setPaGpio(0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            default:
                PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
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
        int band = 0;
        if (Util.onlyNumeric(arfcn)) {
            band = NrBand.earfcn2band(Integer.parseInt(arfcn));
        }
        switch (band) {
            case 1:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case 41:
                if (isB97502)
                    PaBean.build().setPaGpio(3, 1, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 0, 1, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case 28:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case 78:
                if (isB97502)
                    PaBean.build().setPaGpio(3, 1, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(3, 1, 0, 0, 0, 0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case 79:
                if (isB97502)
                    PaBean.build().setPaGpio(3, 1, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(3, 1, 0, 0, 0, 0, 0, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            default:
                PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
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
        int band = 0;
        if (Util.onlyNumeric(arfcn)) {
            band = LteBand.earfcn2band(Integer.parseInt(arfcn));
        }
        switch (band) {
            case 1:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case 3:
                if (isB97502){
                    if (cellId == 3) //通道四开B3次频点
                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                    else{
                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                        /*int freq = LteBand.earfcn2freq(Integer.parseInt(arfcn));
                        int gpo15 = ((freq > 1709 && freq < 1735) || (freq > 1804 && freq < 1830) || (freq > 888 && freq < 904) || (freq > 933 && freq < 949)) ? 1 : 2;
                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 1, 0, 1, 0, gpo15, 0, 0, 0, 0, 0, 0, 0, 0);*/    //B97502
                    }
                }
                else
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case 5:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case 8:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case 34:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 2, 1, 1, 0, 0, 1, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 1, 0, 3, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case 38:
            case 41:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 0, 0, 1, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 1, 0, 3, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case 39:
                if (isB97502)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 2, 1, 2, 0, 0, 1, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 1, 0, 3, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case 40:
                if (isB97502){
                    if (cellId == 1) //通道二开B40次频点
                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                    else
                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 2, 2, 1, 0, 0, 1, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                }
                else
                    PaBean.build().setPaGpio(0, 0, 1, 0, 3, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            default:
                PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
        }
        AppLog.D("openLtePA: " + PaBean.build().toString());
        MessageController.build().setGnbTFPaGpio(id);
    }

    public void closePA(String id) {
        PaBean.build().setPaGpio(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2);
        //PaBean.build().setPaGpio(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
        AppLog.D("closePA: " + PaBean.build().toString());
        MessageController.build().setGnbTFPaGpio(id);
    }

    public void closeLtePA(String id) {
        //if (PaBean.build().getGpio1_en1() == 2) PaBean.build().setPaGpio(2, 0, 0, 0, 0, 0, 0, 0);
        //else PaBean.build().setPaGpio(0, 2, 0, 0, 0, 0, 0, 2);
        PaBean.build().setPaGpio(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2);
        AppLog.D("closePA: " + PaBean.build().toString());
        MessageController.build().setGnbTFPaGpio(id);
    }

    public void openPAByBand(String id, String band) {
        switch (band) {
            // 4G
            case "B1":
                if (isB97502)
                    PaBean.build().setPaGpio(0, 2, 2, 0, 0, 2, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
//                    PaBean.build().setPaGpio(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2);
                else
                    PaBean.build().setPaGpio(0, 2, 2, 2, 0, 2, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case "B3":
                if (isB97502)
                    PaBean.build().setPaGpio(0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 2, 1, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                else
                    PaBean.build().setPaGpio(0, 2, 2, 2, 0, 2, 2, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case "B5":
                if (isB97502)
                    PaBean.build().setPaGpio(0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 1, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                else
                    PaBean.build().setPaGpio(0, 2, 2, 2, 0, 2, 2, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case "B8":
                if (isB97502)
                    PaBean.build().setPaGpio(0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 1, 1, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                else
                    PaBean.build().setPaGpio(0, 2, 2, 2, 0, 2, 2, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case "B34":
                if (isB97502)
                    PaBean.build().setPaGpio(0, 2, 2, 0, 0, 0, 0, 2, 1, 1, 0, 0, 1, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                else
                    PaBean.build().setPaGpio(0, 2, 1, 2, 2, 2, 0, 0, 0, 0, 1, 1, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case "B39":
                if (isB97502)
                    PaBean.build().setPaGpio(0, 2, 2, 0, 0, 0, 0, 2, 1, 2, 0, 0, 1, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                else
                    PaBean.build().setPaGpio(0, 2, 1, 2, 2, 2, 0, 0, 0, 0, 1, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case "B40":
                if (isB97502)
                    PaBean.build().setPaGpio(0, 2, 2, 0, 0, 0, 0, 2, 2, 1, 0, 0, 1, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                else
                    PaBean.build().setPaGpio(0, 2, 1, 2, 2, 2, 0, 0, 0, 0, 2, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case "B41":
                if (isB97502)
                    PaBean.build().setPaGpio(0, 2, 2, 0, 0, 0, 0, 2, 2, 2, 0, 0, 1, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                else
                    PaBean.build().setPaGpio(0, 2, 1, 2, 2, 2, 0, 0, 0, 0, 2, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            // 5G
            case "N1":
                if (isB97502)
                    PaBean.build().setPaGpio(0, 2, 2, 0, 0, 2, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                else
                    PaBean.build().setPaGpio(0, 2, 2, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case "N28":
                if (isB97502)
                    PaBean.build().setPaGpio(0, 2, 2, 0, 0, 1, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                else
                    PaBean.build().setPaGpio(0, 2, 2, 2, 0, 2, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case "N41":
                if (isB97502)
                    PaBean.build().setPaGpio(2, 1, 2, 1, 2, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                else
                    PaBean.build().setPaGpio(0, 2, 2, 1, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case "N78":
                if (isB97502)
                    PaBean.build().setPaGpio(2, 1, 2, 2, 1, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                else
                    PaBean.build().setPaGpio(2, 1, 2, 2, 0, 2, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
            case "N79":
                if (isB97502)
                    PaBean.build().setPaGpio(2, 1, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                else
                    PaBean.build().setPaGpio(2, 1, 2, 2, 0, 2, 0, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                break;
        }

        AppLog.D("openPAByBand: band = " + band + ", " + PaBean.build().toString());
        MessageController.build().setGnbTFPaGpio(id);
    }

    public void openPAByArfcn(String id, String arfcn){
        boolean isLte = arfcn.length() < 6;
        int band = isLte ? LteBand.earfcn2band(Integer.parseInt(arfcn)) : NrBand.earfcn2band(Integer.parseInt(arfcn));
        openPAByBand(id, (isLte ? "B" : "N") + band);
    }
    //根据频点关功放
    public void closePAByArfcn(String id, String arfcn) {
        int band = 0;
        boolean isLte = arfcn.length() < 6;
        if (isLte){
            band = LteBand.earfcn2band(Integer.parseInt(arfcn));
            if (isB97502){
                switch (band) {
                    case 3:// 此频点第八位为1，在此设为2(因为其他的都为0，这里放2关掉也不要紧)
                    case 5:
                    case 8:
                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 2, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                        break;
                    case 1:
                        PaBean.build().setPaGpio(0, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);   
                        break;
                    case 34:
                    case 38:
                    case 41:
                    case 39:
                    case 40:
                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                        break;
                }
            }else {
                switch (band) {
                    case 1:
                    case 3:
                    case 5:
                    case 8:
                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);   
                        break;
                    case 34:
                    case 38:
                    case 41:
                    case 39:
                    case 40:
                        PaBean.build().setPaGpio(0, 0, 2, 0, 2, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);   
                        break;
                }
            }
        }else {
            band = NrBand.earfcn2band(Integer.parseInt(arfcn));
            if (isB97502){
                switch (band) {
                    case 1:
                    case 28:
                        PaBean.build().setPaGpio(0, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);   
                        break;
                    case 41:
                    case 78:
                    case 79:
                        PaBean.build().setPaGpio(2, 2, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                        break;
                }
            }else {
                switch (band) {
                    case 1:
                        PaBean.build().setPaGpio(0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0);   
                        break;
                    case 41:
                        PaBean.build().setPaGpio(0, 0, 0, 2, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0);   
                        break;
                    case 28:
                        PaBean.build().setPaGpio(0, 2, 0, 0, 0, 0, 0, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);   
                        break;
                    case 78:
                    case 79:
                        PaBean.build().setPaGpio(2, 2, 0, 0, 0, 0, 0, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);   
                        break;
                }
            }
        }

        AppLog.D("closePAByArfcn: " + PaBean.build().toString());
        MessageController.build().setGnbTFPaGpio(id);
    }
}
