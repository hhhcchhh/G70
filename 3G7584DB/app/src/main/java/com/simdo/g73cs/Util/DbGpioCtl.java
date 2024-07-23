/**
 * PA控制口
 * <p>
 * 不同PA控制逻辑不一样，需硬件工程师供，参看SDK开发文档附录一表格
 */
package com.simdo.g73cs.Util;

import com.dwdbsdk.Bean.LteBand;
import com.dwdbsdk.Bean.NrBand;
import com.dwdbsdk.MessageControl.MessageController;

public class DbGpioCtl {
    private static DbGpioCtl instance;

    public static DbGpioCtl build() {
        synchronized (DbGpioCtl.class) {
            if (instance == null) {
                instance = new DbGpioCtl();
            }
        }
        return instance;
    }

    public DbGpioCtl() {
//        PaBean.build().setPaGpio(1,1,0,0,0,0,0,0);
    }

    /**
     * N1|N41: PA通道一
     * N28|N78|N79: PA通道二
     *
     * @param arfcn
     */
    public void openGPIO(String id, int arfcn, int doa) {
        if (doa == 0) return;
        int band;
        if (arfcn > 99999) {
            band = NrBand.earfcn2band(arfcn);
            switch (band) {
                case 1:
                    MessageController.build().setDBGpio(id, 6, 1, 1, 1, 1, 2, 1, 0);
                    break;
                case 28:
                    MessageController.build().setDBGpio(id, 1, 2, 2, 1, 2, 1, 1, 0);
                    break;
                case 41:
                    MessageController.build().setDBGpio(id, 6, 2, 1, 2, 1, 2, 1, 0);
                    break;
                case 78:
                    MessageController.build().setDBGpio(id, 6, 1, 1, 1, 1, 1, 2, 0);
                    break;
                case 79:
                    MessageController.build().setDBGpio(id, 6, 1, 1, 2, 1, 1, 2, 0);
                    break;
                default:
                    MessageController.build().setDBGpio(id, 0, 0, 0, 0, 0, 0, 0, 0);
                    break;
            }
        } else {
            band = LteBand.earfcn2band(arfcn);
            switch (band) {
                case 1:
                    MessageController.build().setDBGpio(id, 6, 1, 1, 1, 1, 2, 1, 0);
                    break;
                case 3:
                    MessageController.build().setDBGpio(id, 6, 1, 1, 2, 1, 2, 1, 0);
                    break;
                case 5:
                    MessageController.build().setDBGpio(id, 1, 1, 2, 1, 2, 1, 1, 0);
                    break;
                case 8:
                    MessageController.build().setDBGpio(id, 1, 2, 1, 1, 2, 1, 1, 0);
                    break;
                case 34:
                    MessageController.build().setDBGpio(id, 6, 1, 2, 1, 1, 2, 1, 0);
                    break;
                case 39:
                    MessageController.build().setDBGpio(id, 6, 1, 2, 2, 1, 2, 1, 0);
                    break;
                case 40:
                    MessageController.build().setDBGpio(id, 6, 2, 1, 1, 1, 2, 1, 0);
                    break;
                case 41:
                    MessageController.build().setDBGpio(id, 6, 2, 1, 2, 1, 2, 1, 0);
                    break;
                default:
                    break;
            }
        }

    }

    public void closePA(String id) {
        MessageController.build().setDBGpio(id, 1, 1, 1, 1, 1, 1, 1, 0);

    }

}
