/**
 * PA控制口
 *
 * 不同PA控制逻辑不一样，需硬件工程师供，参看SDK开发文档附录一表格
 */
package com.simdo.g758s_predator.Util;


import android.os.Handler;

import com.dwdbsdk.Bean.LteBand;
import com.dwdbsdk.Bean.NrBand;
import com.dwdbsdk.Bean.PaBean;
import com.dwdbsdk.MessageControl.MessageController;

public class PaCtl {
    private static PaCtl instance;
    private boolean isVehicle = true;

    public static PaCtl build() {
        synchronized (PaCtl.class) {
            if (instance == null) {
                instance = new PaCtl();
            }
        }
        return instance;
    }

    public PaCtl() {
//        PaBean.build().setPaGpio(1,1,0,0,0,0,0,0);
        PaBean.build().setPaGpio(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0);

    }

    /**
     * 至关重要，应用启动必须先调用此方法设置工作模式，以控制后续业务的功放
     * @param isVehicle 是否为车载模式
     */
    public void setMode(boolean isVehicle) {
        this.isVehicle = isVehicle;
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
            if (arfcn.length()>5){
                band = NrBand.earfcn2band(Integer.parseInt(arfcn));
                switch (band) {
                    case 1:
                    case 3:
                    case 5:
                    case 8:
                        if(!isVehicle){
                            PaBean.build().setPaGpio( 1,0,0 ,3,1,0,0,0);

                        }else {
                            PaBean.build().setPaGpio(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0);

//                    PaBean.build().setPaGpio( 0,0,0 ,0,1,0,0,0);
                        }
                        break;
                    case 41:
                        if(!isVehicle){
                            PaBean.build().setPaGpio( 1,0,0 ,3,2,0,0,0);
                        }else {
//                    PaBean.build().setPaGpio( 1,0,0 ,3,0,0,0,0);
                            PaBean.build().setPaGpio(0,0,0,0,0,0,0,0,4,3,0,0,0,0,0,0,1,1,0,0,0,0,0,0);

                        }

                        break;
                    case 28:
                        if(!isVehicle){
                            PaBean.build().setPaGpio( 0,1,1 ,0,0,2,0,3);
                        }else {
//                    PaBean.build().setPaGpio( 0,0,1 ,0,0,0,0,0);
                            PaBean.build().setPaGpio(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0);

                        }
                        break;
                    case 78:
                        if(!isVehicle){
                            PaBean.build().setPaGpio( 0,1,2 ,0,0,2,0,3);
                        }else {
//                    PaBean.build().setPaGpio( 0,1,0 ,0,0,0,0,3);
                            PaBean.build().setPaGpio(3,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0);

                        }
                        break;
                    case 79:
                        if(!isVehicle){
                            PaBean.build().setPaGpio( 0,1,2 ,0,0,1,0,3);
                        }else {
//                    PaBean.build().setPaGpio( 0,1,2 ,0,0,1,0,3);
                            PaBean.build().setPaGpio(2,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0);
                        }
                        break;
                    default:
                        PaBean.build().setPaGpio(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0);
                        break;
                }
                if (band ==78||band == 28){
                    MessageController.build().setDateTo485(id,8,0,34,0,1,"1");
                }else if (band == 79||band == 41||band == 5){
                    MessageController.build().setDateTo485(id,8,1,34,0,1,"1");
                }else if (band == 1||band == 8){
                    MessageController.build().setDateTo485(id,8,2,34,0,1,"1");
                }else if (band == 3){
                    MessageController.build().setDateTo485(id,8,3,34,0,1,"1");
                }
            }else {
                band = LteBand.earfcn2band(Integer.parseInt(arfcn));
                switch (band){
                    case 1:
                    case 5:
                    case 3:
                    case 8:
                        PaBean.build().setPaGpio(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0);
                        break;
                    case 34:
                        PaBean.build().setPaGpio(0,0,0,0,0,0,0,0,0,0,0,0,4,3,0,0,1,1,0,0,0,0,0,0);
                        break;
                    case 39:
                        PaBean.build().setPaGpio(0,0,0,0,4,3,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0);
                        break;
                    case 40:
                        // 2024/02/29 按需求把3改成6
                        PaBean.build().setPaGpio(0,0,0,0,0,0,0,0,0,0,0,0,4,6,0,0,1,1,0,0,0,0,0,0);
                        break;
                    case 41:
                        PaBean.build().setPaGpio(0,0,0,0,0,0,0,0,4,3,0,0,0,0,0,0,1,1,0,0,0,0,0,0);
                        break;
                    default:
                        PaBean.build().setPaGpio(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0);
                        break;
                }
                if (band == 39){
                    MessageController.build().setDateTo485(id,8,0,34,0,1,"1");
                }else if (band == 5||band == 41){
                    MessageController.build().setDateTo485(id,8,1,34,0,1,"1");
                }else if (band == 1||band ==40 || band == 8){
                    MessageController.build().setDateTo485(id,8,2,34,0,1,"1");
                }else if (band == 3|| band == 34){
                    MessageController.build().setDateTo485(id,8,3,34,0,1,"1");
                }
            }
        }
        AppLog.D("openPA: " + PaBean.build().toString());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MessageController.build().setGnbTFPaGpio(id);
            }
        },500);

    }

    public void closePA(String id) {
            MessageController.build().setDateTo485(id,8,0,34,0,1,"0");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    MessageController.build().setDateTo485(id,8,1,34,0,1,"0");
                }
            },200);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    MessageController.build().setDateTo485(id,8,2,34,0,1,"0");
                }
            },400);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    MessageController.build().setDateTo485(id,8,3,34,0,1,"0");
                }
            },600);

//        PaBean.build().setPaGpio( 2,2,2,2,2,2,2,2);
        PaBean.build().setPaGpio(2,2,0,0,1,1,0,0,1,1,0,0,1,1,2,0,1,1,0,0,0,0,0,0);
        AppLog.D("closePA: " + PaBean.build().toString());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MessageController.build().setGnbTFPaGpio(id);
            }
        },800);

    }

    //初始化空口
    public void initAirPA(String id, int cell) {
        if (cell==0){
            PaBean.build().setPaGpio( 1,0,0 ,0,0,0,0,0);
        }else {
            PaBean.build().setPaGpio( 0,1,0 ,0,0,0,0,0);
        }

        AppLog.D("initAirPA: " + PaBean.build().toString());
        MessageController.build().setGnbTFPaGpio(id);
    }
}
