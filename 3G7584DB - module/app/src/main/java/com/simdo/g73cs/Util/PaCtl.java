/**
 * PA控制口
 * <p>
 * 不同PA控制逻辑不一样，需硬件工程师供，参看SDK开发文档附录一表格
 */
package com.simdo.g73cs.Util;


import android.os.Handler;

import com.dwdbsdk.Bean.LteBand;
import com.dwdbsdk.Bean.NrBand;
import com.dwdbsdk.Bean.PaBean;
import com.dwdbsdk.MessageControl.MessageController;

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
    public final boolean is3G758 = true;

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
    // A低发高收
    // B\C高发高收3->2,4->1
    public void initPA(String id, String arfcn, int channelNum) {
        AppLog.I("initPA id = " + id + ", arfcn = " + arfcn);
        int band = 0;
        if (Util.onlyNumeric(arfcn)) {
            band = NrBand.earfcn2band(Integer.parseInt(arfcn));
        }
        switch (band) {
            case 1:
                if (is3G758)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 3:
                PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
            case 41:
                if (is3G758)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 0, 1, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 28:
                if (is3G758)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 78:
                if (is3G758)
                    if (channelNum == 5)
                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                    else
                        PaBean.build().setPaGpio(2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(2, 1, 0, 0, 0, 0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 79:
                if (is3G758)
                    PaBean.build().setPaGpio(2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
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
    public void initLtePA(String id, String arfcn, int channelNum) {
        AppLog.I("initLtePA id = " + id + ", arfcn = " + arfcn + ", channelNum = " + channelNum);
        int band = 0;
        if (Util.onlyNumeric(arfcn)) {
            band = LteBand.earfcn2band(Integer.parseInt(arfcn));
        }
        switch (band) {
            case 1:
                if (is3G758)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 3:
                if (is3G758) {
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                } else
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 5:
                if (is3G758)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 8:
                if (is3G758)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 34:
                if (is3G758)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 38:
            case 41:
                if (is3G758)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 39:
                if (is3G758)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 40:
                if (is3G758) {
                    if (channelNum == 6) //通道二开B40次频点
                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                    else
                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
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
    public void openPA(String id, String arfcn, int channelNum) {
        AppLog.I("openPA id = " + id + ", arfcn = " + arfcn);
        int band = 0;
        if (Util.onlyNumeric(arfcn)) {
            band = NrBand.earfcn2band(Integer.parseInt(arfcn));
        }
//        switch (band) {
//            case 1:
//                if (is3G758)
//                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
//                else
//                    PaBean.build().setPaGpio(0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
//                break;
//            case 3:
//                PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
//            case 41:
//                if (is3G758)
//                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 4, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
//                else
//                    PaBean.build().setPaGpio(0, 0, 0, 1, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
//                break;
//            case 28:
//                if (is3G758)
//                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
//                else
//                    PaBean.build().setPaGpio(0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
//                break;
//            case 78:
//                if (is3G758)
//                    if (channelNum == 5)
//                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
//                    else
//                        PaBean.build().setPaGpio(3, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
//                else
//                    PaBean.build().setPaGpio(3, 1, 0, 0, 0, 0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
//                break;
//            case 79:
//                if (is3G758)
//                    PaBean.build().setPaGpio(2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
//                else
//                    PaBean.build().setPaGpio(3, 1, 0, 0, 0, 0, 0, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
//                break;
//        }
        switch (band) {
            case 1:
            case 3:
            case 5:
            case 8:
                if (!isVehicle) {
                    PaBean.build().setPaGpio(1, 0, 0, 3, 1, 0, 0, 0);
                } else {
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0);

//                    PaBean.build().setPaGpio( 0,0,0 ,0,1,0,0,0);
                }
                break;
            case 41:
                if (!isVehicle) {
                    PaBean.build().setPaGpio(1, 0, 0, 3, 2, 0, 0, 0);
                } else {
//                    PaBean.build().setPaGpio( 1,0,0 ,3,0,0,0,0);
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 4, 3, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0);

                }

                break;
            case 28:
                if (!isVehicle) {
                    PaBean.build().setPaGpio(0, 1, 1, 0, 0, 2, 0, 3);
                } else {
//                    PaBean.build().setPaGpio( 0,0,1 ,0,0,0,0,0);
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0);

                }
                break;
            case 78:
                if (!isVehicle) {
                    PaBean.build().setPaGpio(0, 1, 2, 0, 0, 2, 0, 3);
                } else {
//                    PaBean.build().setPaGpio( 0,1,0 ,0,0,0,0,3);
                    PaBean.build().setPaGpio(3, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0);

                }
                break;
            case 79:
                if (!isVehicle) {
                    PaBean.build().setPaGpio(0, 1, 2, 0, 0, 1, 0, 3);
                } else {
//                    PaBean.build().setPaGpio( 0,1,2 ,0,0,1,0,3);
                    PaBean.build().setPaGpio(2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0);
                }
                break;
            default:
                PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0);
                break;
        }
        if (band == 78 || band == 28) {
            MessageController.build().setDateTo485(id, 8, 0, 34, 0, 1, "1");
        } else if (band == 79 || band == 41 || band == 5) {
            MessageController.build().setDateTo485(id, 8, 1, 34, 0, 1, "1");
        } else if (band == 1 || band == 8) {
            MessageController.build().setDateTo485(id, 8, 2, 34, 0, 1, "1");
        } else if (band == 3) {
            MessageController.build().setDateTo485(id, 8, 3, 34, 0, 1, "1");
        }
        AppLog.D("openPA: " + PaBean.build().toString());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MessageController.build().setGnbTFPaGpio(id);
            }
        }, 500);
    }

    /**
     * B34|B38|B39|B40|B41: PA通道一
     * B1|B3|B5|B8|B34|B38|B39|B40|B41: PA通道二
     *
     * @param arfcn 频点
     */
    public void openLtePA(String id, String arfcn, int channelNum) {
        AppLog.I("openLtePA id = " + id + ", arfcn = " + arfcn);
        int band = 0;
        if (Util.onlyNumeric(arfcn)) {
            band = LteBand.earfcn2band(Integer.parseInt(arfcn));
        }
//        switch (band) {
//            case 1:
//                if (is3G758)
//                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
//                else
//                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
//                break;
//            case 3:
//                if (is3G758) {
//                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
//                } else
//                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
//                break;
//            case 5:
//                if (is3G758)
//                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
//                else
//                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
//                break;
//            case 8:
//                if (is3G758)
//                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
//                else
//                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
//                break;
//            case 34:
//                if (is3G758)
//                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
//                else
//                    PaBean.build().setPaGpio(0, 0, 1, 0, 3, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
//                break;
//            case 38:
//            case 41:
//                if (is3G758)
//                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
//                else
//                    PaBean.build().setPaGpio(0, 0, 1, 0, 3, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
//                break;
//            case 39:
//                if (is3G758)
//                    PaBean.build().setPaGpio(0, 0, 0, 0, 4, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
//                else
//                    PaBean.build().setPaGpio(0, 0, 1, 0, 3, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
//                break;
//            case 40:
//                if (is3G758) {
//                    if (channelNum == 6) //通道二开B40次频点
//                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
//                    else
//                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
//                } else
//                    PaBean.build().setPaGpio(0, 0, 1, 0, 3, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
//                break;
//        }
        switch (band) {
            case 1:
            case 5:
            case 3:
            case 8:
                PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0);
                break;
            case 34:
                PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 3, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0);
                break;
            case 39:
                PaBean.build().setPaGpio(0, 0, 0, 0, 4, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0);
                break;
            case 40:
                PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 3, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0);
                break;
            case 41:
                PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 4, 3, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0);
                break;
            default:
                PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0);
                break;
        }
        if (band == 39) {
            MessageController.build().setDateTo485(id, 8, 0, 34, 0, 1, "1");
        } else if (band == 5 || band == 41) {
            MessageController.build().setDateTo485(id, 8, 1, 34, 0, 1, "1");
        } else if (band == 1 || band == 40 || band == 8) {
            MessageController.build().setDateTo485(id, 8, 2, 34, 0, 1, "1");
        } else if (band == 3 || band == 34) {
            MessageController.build().setDateTo485(id, 8, 3, 34, 0, 1, "1");
        }
        AppLog.D("openLtePA: " + PaBean.build().toString());
        MessageController.build().setGnbTFPaGpio(id);
    }

    public void closePAById(String id) {
        MessageController.build().setDateTo485(id, 8, 0, 34, 0, 1, "0");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MessageController.build().setDateTo485(id, 8, 1, 34, 0, 1, "0");
            }
        }, 200);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MessageController.build().setDateTo485(id, 8, 2, 34, 0, 1, "0");
            }
        }, 400);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MessageController.build().setDateTo485(id, 8, 3, 34, 0, 1, "0");
            }
        }, 600);
        PaBean.build().setPaGpio(2, 2, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 2, 0, 1, 1, 0, 0, 0, 0, 0, 0);

        int logicIndex = DeviceUtil.getLogicIndexById(id);
        AppLog.D("closePA: logicIndex = " + logicIndex + PaBean.build().toString());
        MessageController.build().setGnbTFPaGpio(id);
    }

//    public void closeLtePA(String id) {
//        AppLog.I("closeLtePA id = " + id);
//        //if (PaBean.build().getGpio1_en1() == 2) PaBean.build().setPaGpio(2, 0, 0, 0, 0, 0, 0, 0);
//        //else PaBean.build().setPaGpio(0, 2, 0, 0, 0, 0, 0, 2);
//        PaBean.build().setPaGpio(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2);
//        AppLog.D("closePA: " + PaBean.build().toString());
//        MessageController.build().setGnbTFPaGpio(id);
//    }

    public void openFreqPA(String id, String arfcn, int channelNum) {
        AppLog.I("openFreqPA id = " + id + " arfcn = " + arfcn);
        int band = 0;
        if (Util.onlyNumeric(arfcn)) {
            band = NrBand.earfcn2band(Integer.parseInt(arfcn));
        }
        switch (band) {
            case 1:
                if (is3G758)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 2, 2, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 41:
                if (is3G758)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0); //G735D
                else
                    PaBean.build().setPaGpio(0, 2, 2, 1, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 28:
                if (is3G758)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 2, 2, 2, 0, 2, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 78:
                if (is3G758)
                    PaBean.build().setPaGpio(2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0); //G735D
                else
                    PaBean.build().setPaGpio(2, 1, 2, 2, 0, 2, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 79:
                if (is3G758)
                    PaBean.build().setPaGpio(2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0); //G735D
                else
                    PaBean.build().setPaGpio(2, 1, 2, 2, 0, 2, 0, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
        }
        AppLog.D("openFreqPA: " + PaBean.build().toString());
        MessageController.build().setGnbTFPaGpio(id);
    }

    public void openFreqLtePA(String id, String arfcn, int channelNum) {
        AppLog.I("openFreqPA id = " + id + " arfcn = " + arfcn);
        int band = 0;
        if (Util.onlyNumeric(arfcn)) {
            band = LteBand.earfcn2band(Integer.parseInt(arfcn));
        }
        switch (band) {
            case 1:
                if (is3G758)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 2, 2, 2, 0, 2, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 3:
                if (is3G758)
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                else
                    PaBean.build().setPaGpio(0, 2, 2, 2, 0, 2, 2, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 5:
                if (is3G758)
                    PaBean.build().setPaGpio(0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 1, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0); //G735D FDD扫频不走功放，走ORX4
                else
                    PaBean.build().setPaGpio(0, 2, 2, 2, 0, 2, 2, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 8:
                if (is3G758)
                    PaBean.build().setPaGpio(0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 1, 1, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0); //G735D FDD扫频不走功放，走ORX4
                else
                    PaBean.build().setPaGpio(0, 2, 2, 2, 0, 2, 2, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 34:
                if (is3G758)
                    PaBean.build().setPaGpio(0, 2, 2, 0, 0, 0, 0, 2, 1, 1, 0, 0, 1, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0); //G735D
                else
                    PaBean.build().setPaGpio(0, 2, 1, 2, 2, 2, 0, 0, 0, 0, 1, 1, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 38:
            case 41:
                if (is3G758)
                    PaBean.build().setPaGpio(0, 2, 2, 0, 0, 0, 0, 2, 2, 2, 0, 0, 1, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0); //G735D
                else
                    PaBean.build().setPaGpio(0, 2, 1, 2, 2, 2, 0, 0, 0, 0, 2, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 39:
                if (is3G758)
                    PaBean.build().setPaGpio(0, 2, 2, 0, 0, 0, 0, 2, 1, 2, 0, 0, 1, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0); //G735D
                else
                    PaBean.build().setPaGpio(0, 2, 1, 2, 2, 2, 0, 0, 0, 0, 1, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
            case 40:
                if (is3G758)
                    if (channelNum == 6)
                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                    else
                        PaBean.build().setPaGpio(0, 2, 2, 0, 0, 0, 0, 2, 2, 1, 0, 0, 1, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0); //G735D
                else
                    PaBean.build().setPaGpio(0, 2, 1, 2, 2, 2, 0, 0, 0, 0, 2, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0);    //B97501
                break;
        }
        AppLog.D("openFreqLtePA: " + PaBean.build().toString());
        MessageController.build().setGnbTFPaGpio(id);
    }

    //根据频点关功放
    public void closePAByArfcn(String id, boolean isLte, String arfcn, int channelNum) {
        AppLog.D("closePAByArfcn: id" + id + " isLte" + isLte + " arfcn" + arfcn);
        int band = 0;
        if (isLte) {
            band = LteBand.earfcn2band(Integer.parseInt(arfcn));
            if (band == 39) {
                MessageController.build().setDateTo485(id, 8, 0, 34, 0, 1, "1");
            } else if (band == 5 || band == 41) {
                MessageController.build().setDateTo485(id, 8, 1, 34, 0, 1, "1");
            } else if (band == 1 || band == 40 || band == 8) {
                MessageController.build().setDateTo485(id, 8, 2, 34, 0, 1, "1");
            } else if (band == 3 || band == 34) {
                MessageController.build().setDateTo485(id, 8, 3, 34, 0, 1, "1");
            }
            switch (band) {
                case 1:
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                    break;
                case 3:
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                    break;
                case 5:
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                    break;
                case 8:
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                    break;
                case 34:
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                    break;
                case 38:
                case 41:
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                    break;
                case 39:
                    PaBean.build().setPaGpio(0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                    break;
                case 40:
                    if (channelNum == 6) {
                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                    } else {
                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                    }
                    break;
            }
        } else {
            band = NrBand.earfcn2band(Integer.parseInt(arfcn));
            if (band == 78 || band == 28) {
                MessageController.build().setDateTo485(id, 8, 0, 34, 0, 1, "1");
            } else if (band == 79 || band == 41 || band == 5) {
                MessageController.build().setDateTo485(id, 8, 1, 34, 0, 1, "1");
            } else if (band == 1 || band == 8) {
                MessageController.build().setDateTo485(id, 8, 2, 34, 0, 1, "1");
            } else if (band == 3) {
                MessageController.build().setDateTo485(id, 8, 3, 34, 0, 1, "1");
            }
            switch (band) {
                case 1:
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                    break;
                case 3:
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                    break;
                case 41:
                    PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                    break;
                case 28:
                    PaBean.build().setPaGpio(0, 2, 0, 0, 0, 0, 0, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //G735D
                    break;
                case 78:
                    if (channelNum == 5) {
                        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                    } else {
                        PaBean.build().setPaGpio(2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                    }
                    break;
                case 79:
                    PaBean.build().setPaGpio(2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                    break;
            }
        }

        AppLog.D("closePAByArfcn: " + PaBean.build().toString());
        MessageController.build().setGnbTFPaGpio(id);
    }

    //扫频不需考虑次频点
    public void openFreqPAByBand(String id, String band) {
        AppLog.D("openPAByBand: id" + id + " band" + band);
        switch (band) {
            // 4G
            case "B1":
            case "N1":
            case "B3":
            case "N28":
            case "N3":
            case "B5":
            case "B8":
                PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                break;
            case "B41":
                PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                break;
            case "B34":
                PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                break;
            case "B39":
                PaBean.build().setPaGpio(0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                break;
            case "B40":
                PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                break;
            // 5G
            case "N41":
                PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                break;
            case "N78":
                PaBean.build().setPaGpio(2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                break;
            case "N79":
                PaBean.build().setPaGpio(2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);    //B97502
                break;
        }

        switch (band) {
            case "N78":
            case "N28":
            case "B39":
                MessageController.build().setDateTo485(id, 8, 0, 34, 0, 1, "0");
                break;
            case "N79":
            case "N41":
            case "B5":
            case "B41":
                MessageController.build().setDateTo485(id, 8, 1, 34, 0, 1, "0");
                break;
            case "N1":
            case "N3":
            case "B1":
            case "B40":
            case "B8":
                MessageController.build().setDateTo485(id, 8, 2, 34, 0, 1, "0");
                break;
            case "B34":
            case "B3":
                MessageController.build().setDateTo485(id, 8, 3, 34, 0, 1, "0");
                break;
        }
        AppLog.D("openPAByBand: band = " + band + ", " + PaBean.build().toString());
        MessageController.build().setGnbTFPaGpio(id);
    }
}
