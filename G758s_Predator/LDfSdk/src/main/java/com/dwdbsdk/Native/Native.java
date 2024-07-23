/**
 * Auto compiler ***.h file
 * DIR: cd app/src/main/java
 * CMD: javah -jni -encoding UTF-8 com.jni.Native.Native
 *
 * 命令自动生成 ***.h文件，避免包名出错【1、先写Native.java 编译生成 ****.h 然后复制改成 ***.c文件再编译函数内容】
 */

package com.dwdbsdk.Native;

public class Native {
    static {
        System.loadLibrary("bs_service");
    }
    // 关闭串口通讯
    public static native int jniUartClose();
    // 串口初始化，波特率：115200
    public static native int jniUartInit(int baudrate);
    // 串口数据传送：第三方客户可不关注 uart_port = 0: 串口0； =1：串口1
    public static native int jniUartDataWrite(int uart_port, int[] commond, int length);
    // 串口数据读取 uart_port = 0: 串口0； =1：串口1
    public static native int jniUartDataRead(int uart_port, int[] commond);
    // 设备初始化
    public static native boolean LcInit();
    // 关闭设备
    public static native boolean LcClose();
    // 平台LED灯： 1 ： 开  0： 关
    public static native boolean SetStatusLed0(int state);
    // 平台LED灯： 1 ： 开  0： 关
    public static native boolean SetStatusLed1(int state);
    // OTG使能开关： 1 有效 TYPEC_CONNECT_ON_OFF
    public static native boolean EnableTypec(int state);
    // 控制USB连接到5G模块，读信令用： 1  有效  MTK_CTL_5G
    public static native boolean EnableNrUsb(int state);
    // 控制USB连接到5G模块，读信令用： 1  有效  MTK_CTL_5G
    public static native boolean EnableOTG(int state);
    public static native boolean EnableExOTG(int state);
    public static native boolean EnableHOST(int state);
    //// Card 0 = 核心板   1：5G模块
    public static native boolean SetCardTo5G(int state);
    /**
     ** set module power
     ** LTE = 0
     ** NR = 1
     **/
    public static native boolean SetModulePwr(int type, int state);
}
