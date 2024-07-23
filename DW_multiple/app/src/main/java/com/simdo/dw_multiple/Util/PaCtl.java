/**
 * PA控制口
 *
 * 不同PA控制逻辑不一样，需硬件工程师供，参看SDK开发文档附录一表格
 */
package com.simdo.dw_multiple.Util;


import com.nr.Arfcn.Bean.LteBand;
import com.nr.Arfcn.Bean.NrBand;
import com.nr.Gnb.Bean.PaBean;
import com.nr.Socket.MessageControl.MessageController;

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
        PaBean.build().setPaGpio(0,0,0,0,0,0,0,0);
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
            if (Integer.parseInt(arfcn) <= 99999 && Integer.parseInt(arfcn) >= 0) {
                band = LteBand.earfcn2band(Integer.parseInt(arfcn));
                switch (band) {
                    case 1:
                        if (!isVehicle) {
                            PaBean.build().setPaGpio(1, 0, 0, 0, 1, 1, 0, 0);    //G73
                        } else {
                            PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0);
                        }
                        break;
                    case 3:
                        if (!isVehicle) {
                            PaBean.build().setPaGpio(1, 0, 0, 0, 1, 2, 0, 0);     //g73
                        } else {
                            PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0);
                        }
                        break;
                    case 5:
                        if (!isVehicle) {
                            PaBean.build().setPaGpio(1, 0, 0, 0, 2, 1, 0, 0);
                        } else {
                            PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0);
                        }
                        break;
                    case 8:
                        if (!isVehicle) {
                            PaBean.build().setPaGpio(1, 0, 0, 0, 2, 2, 0, 0);
                        } else {
                            PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0);
                        }
                        break;
                    case 34:
                        if (!isVehicle) {
                            PaBean.build().setPaGpio(0, 1, 1, 0, 0, 0, 1, 3);
                        } else {
                            PaBean.build().setPaGpio(0, 0, 4, 3, 0, 0, 0, 0);
                        }
                        break;
                    case 39:
                        if (!isVehicle) {
                            PaBean.build().setPaGpio(0, 1, 1, 0, 0, 0, 2, 3);
                        } else {
                            PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 4, 3);
//                            PaBean.build().setPaGpio(0, 0, 4, 3, 0, 0, 0, 0);
                        }
                        break;
                    case 40:
                        if (!isVehicle) {
                            PaBean.build().setPaGpio(0, 1, 2, 0, 0, 0, 1, 3);
                        } else {
                            PaBean.build().setPaGpio(0, 0, 4, 3, 0, 0, 0, 0);
//                            PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 4, 3);
                        }
                        break;
                    case 38:
                    case 41:
                        if (!isVehicle) {
                            PaBean.build().setPaGpio(0, 1, 2, 0, 0, 0, 2, 3);
                        } else {
//                            PaBean.build().setPaGpio(0, 0, 4, 3, 0, 0, 0, 0);
                            PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 4, 3);
                        }
                        break;
                    default:
                        break;
                }

            } else {
                band = NrBand.earfcn2band(Integer.parseInt(arfcn));
                switch (band) {
                    case 1:
                        if (!isVehicle) {
                            PaBean.build().setPaGpio(1, 0, 0, 3, 1, 0, 0, 0);

                        } else {
                            PaBean.build().setPaGpio(2, 0, 0, 2, 1, 0, 0, 0);
                        }
                        break;
                    case 41:
                        if (!isVehicle) {
                            PaBean.build().setPaGpio(1, 0, 0, 3, 2, 0, 0, 0);
                        } else {
                            PaBean.build().setPaGpio(1, 0, 0, 3, 2, 0, 0, 0);
                        }
                        break;
                    case 28:
                        if (!isVehicle) {
                            PaBean.build().setPaGpio(0, 1, 1, 0, 0, 2, 0, 3);
                        } else {
                            PaBean.build().setPaGpio(0, 2, 1, 0, 0, 2, 2, 2);
                        }
                        break;
                    case 78:
                        if (!isVehicle) {
                            PaBean.build().setPaGpio(0, 1, 2, 0, 0, 2, 0, 3);
                        } else {
                            PaBean.build().setPaGpio(0, 1, 2, 0, 0, 2, 2, 3);
                        }
                        break;
                    case 79:
                        if (!isVehicle) {
                            PaBean.build().setPaGpio(0, 1, 2, 0, 0, 1, 0, 3);
                        } else {
                            PaBean.build().setPaGpio(0, 2, 2, 0, 0, 1, 3, 2);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        AppLog.D("openPA: " + PaBean.build().toString());
        MessageController.build().setGnbPaGpio(id);
    }

    public void closePA(String id) {

        PaBean.build().setPaGpio( 0,0,1,2,0,0,1,2);
        AppLog.D("closePA: " + PaBean.build().toString());
        MessageController.build().setGnbPaGpio(id);
    }
    public void closePA(String id,boolean isNR) {
        if (isNR){
            PaBean.build().setPaGpio( 2,2,2,2,2,2,2,2);
        }else {
            PaBean.build().setPaGpio( 0,0,1,2,0,0,1,2);
        }

        AppLog.D("closePA: " + PaBean.build().toString());
        MessageController.build().setGnbPaGpio(id);
    }

    //初始化空口
    public void initAirPA(String id, int cell) {
        if (cell==0){
            PaBean.build().setPaGpio( 1,0,0 ,0,0,0,0,0);
        }else {
            PaBean.build().setPaGpio( 0,1,0 ,0,0,0,0,0);
        }

        AppLog.D("initAirPA: " + PaBean.build().toString());
        MessageController.build().setGnbPaGpio(id);
    }
}
