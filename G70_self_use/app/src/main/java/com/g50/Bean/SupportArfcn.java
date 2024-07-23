/**
 * 确定输入的频点是否非法
 */
package com.g50.Bean;

public class SupportArfcn {

    public static boolean support(int arfcn) {
        for (int i = 0; i < ARFCN_NUM; i++) {
            if (arfcn == arfcn_txpwr[i][0]) {
                return true;
            }
        }
        return false;
    }

    public static final int ARFCN_NUM = 3;
    private static final int[][] arfcn_txpwr = {
            /*ARFCN   FAR   NORMAL   NEAR   */
            {504990, 0, -2, -5},
            {633984, -4, -7, -11},
            {627264, -2, -5, -9},
    };
}
