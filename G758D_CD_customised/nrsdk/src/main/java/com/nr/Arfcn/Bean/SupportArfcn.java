/**
 * 确定输入的频点是否非法
 */
package com.nr.Arfcn.Bean;

public class SupportArfcn {

    public static boolean support(int arfcn) {
        for (int i = 0; i < arfcn_txpwr.length; i++) {
            if (arfcn == arfcn_txpwr[i][0]) {
                return true;
            }
        }
        return false;
    }

    private static final int[][] arfcn_txpwr = {
            /*ARFCN   FAR   NORMAL   NEAR   */
            {504990, 0, -2, -5},
            {512910, 0, -2, -5},
            {516990, 0, -2, -5},
            {507150, 0, -2, -5},
            {525630, 0, -2, -5},
            {723360, 0, -2, -5},
            {633984, -4, -7, -11},
            {627264, -2, -5, -9},
            {427250, -2, -5, -9},
            {154810, -2, -5, -9},
    };
}
