/**
 * 
 */
package com.dwdbsdk.FTP;

import java.math.BigDecimal;

/**
 * 执行每一个动作后响应的结果，包括成功的和失败的.
 * 
 * @author hao_yujie, cui_tao
 *
 */
public class Result {
    /**
     * 存储单位.
     */
    private static final int STOREUNIT = 1024;

    /**
     * 时间毫秒单位.
     */
    private static final int TIMEMSUNIT = 1000;

    /**
     * 时间单位.
     */
    private static final int TIMEUNIT = 60;
    /**
     * 响应的结果.
     */
    private boolean succeed;
    /**
     * 响应的时间.
     */
    private String time;
    /**
     * 响应的内容.
     */
    private String response;
    /**
     * 无参的构造方法.
     */
    public Result() {
    }

    /**
     * 构造方法.
     * 
     * @param res 响应的内容
     */
    public Result(String res) {
        this.response = res;
    }

    /**
     * 构造方法.
     * 
     * @param suc 响应的结果
     * @param ti 响应的时间
     * @param res 响应的内容
     */
    public Result(boolean suc, String ti, String res) {
        this.succeed = suc;
        this.time = ti;
        this.response = res;
    }

    /**
     * 得到相应内容.
     * 
     * @return 相应内容
     */
    public String getResponse() {
        return response;
    }

    /**
     * 设置相应内容.
     * 
     * @param response 响应内容
     */
    public void setResponse(String response) {
        this.response = response;
    }

    /**
     * 得到相应结果.
     * 
     * @return 相应结果
     */
    public boolean isSucceed() {
        return succeed;
    }

    /**
     * 设置响应结果.
     * 
     * @param succeed 响应结果
     */
    public void setSucceed(boolean succeed) {
        this.succeed = succeed;
    }

    /**
     * 得到响应时间.
     * 
     * @return 响应时间
     */
    public String getTime() {
        return time;
    }

    /**
     * 设置响应时间.
     * 
     * @param time 响应时间
     */
    public void setTime(String time) {
        this.time = time;
    }

    /**
     * 转化时间单位.
     * @param time 转化前大小(MS)
     * @return 转化后大小
     */
    public static String getFormatTime(long time) {
        double second = (double) time / TIMEMSUNIT;
        if (second < 1) {
            return time + " MS";
        }

        double minute = second / TIMEUNIT;
        if (minute < 1) {
            BigDecimal result = new BigDecimal(Double.toString(second));
            return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + " SEC";
        }

        double hour = minute / TIMEUNIT;
        if (hour < 1) {
            BigDecimal result = new BigDecimal(Double.toString(minute));
            return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + " MIN";
        }

        BigDecimal result = new BigDecimal(Double.toString(hour));
        return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + " H";
    }
    /**
     * 转化文件单位.
     * @param size 转化前大小(byte)
     * @return 转化后大小
     */
    public static String getFormatSize(double size) {
        double kiloByte = size / STOREUNIT;
        if (kiloByte < 1) {
            return size + " Byte";
        }

        double megaByte = kiloByte / STOREUNIT;
        if (megaByte < 1) {
            BigDecimal result = new BigDecimal(Double.toString(kiloByte));
            return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + " KB";
        }

        double gigaByte = megaByte / STOREUNIT;
        if (gigaByte < 1) {
            BigDecimal result = new BigDecimal(Double.toString(megaByte));
            return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + " MB";
        }

        double teraBytes = gigaByte / STOREUNIT;
        if (teraBytes < 1) {
            BigDecimal result = new BigDecimal(Double.toString(gigaByte));
            return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + " GB";
        }
        BigDecimal result = new BigDecimal(teraBytes);
        return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + " TB";
    }

    @Override
    public String toString() {
        return "Result{" +
                "succeed=" + succeed +
                ", time='" + time + '\'' +
                ", response='" + response + '\'' +
                '}';
    }
}
