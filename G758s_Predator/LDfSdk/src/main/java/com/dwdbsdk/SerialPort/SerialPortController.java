package com.dwdbsdk.SerialPort;

import android.os.Handler;

import com.dwdbsdk.Arfcn.Nr5g;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

public class SerialPortController {
    private static SerialPortController instance;

    public static SerialPortController build() {
        synchronized (SerialPortController.class) {
            if (instance == null) {
                instance = new SerialPortController();
            }
        }
        return instance;
    }

    public SerialPortController() {
       // String[] entries = mSerialPortFinder.getAllDevices();
       // String[] entryValues = mSerialPortFinder.getAllDevicesPath();
        try {
            mSerialPort = getSerialPort("/dev/ttyS0",115200);
            //mSerialPort1 = getSerialPort(ttyS1,115200);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mOutputStream = mSerialPort.getOutputStream();
        mInputStream = mSerialPort.getInputStream();
        /* Create a receiving thread */
        ttyS0Read = new ReadThread();
        ttyS0Read.start();
    }

    private SerialPort getSerialPort(String path, int baudrate) throws SecurityException, IOException, InvalidParameterException {
        SerialPort serialPort = new SerialPort(new File(path), baudrate, 0);
        return serialPort;
    }

    public void closeSerialPort() {
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }

    public void onStop() {
        if (ttyS0Read != null) {
            ttyS0Read.cancel();
            ttyS0Read = null;
        }
        closeSerialPort();
        instance = null;
    }
    /**
     * 发送指令到串口
     * @param cmd
     */
    public void sendAtCmd(String cmd) {
        try {
           /* if (isHex) {
                byte[] strByte = toByteArray(str);
                mOutputStream.write(strByte);
                mOutputStream.flush();
            } else {*/
                mOutputStream.write(cmd.getBytes());
                mOutputStream.flush();
            //}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ReadThread extends Thread {
        private final StringBuilder sb;
        private boolean stop;

        public ReadThread() {
            stop = false;
            sb = new StringBuilder();
        }
        public void cancel() {
            stop = true;
        }
        @Override
        public void run() {
            while(!stop) {
                try {
                    if (mInputStream == null) {
                        return;
                    }
                    byte[] buffer = new byte[100];
                    int size = mInputStream.read(buffer);
                    if (size > 0) {
                        //String res = new String(buffer,0,size,"UTF-8");
                        String res = new String(buffer, 0, size);
                        res = res.replaceAll(" ", "");
                        res = res.replaceAll("\r|\n", "");
                        //SLog.E("res: " + res);
                        if (Nr5g.build().isQsFlag()) {
                            /*if (res.contains("mcc:")) {
                                String ss = res;
                                int idx = res.indexOf("mcc");
                                if (idx > 0) {
                                    String s = res.substring(0, idx);
                                    ss = res.substring(idx);
                                    sb.append(s);
                                }
                                final String info = sb.toString();
                                sb.setLength(0);
                                sb.append(ss);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        UartReadMsg(info);
                                    }
                                });
                            } else */if (res.contains("OK")) { // 正常指令反馈
                                int idx = res.indexOf("OK") + 2;
                                String s = res.substring(0, idx);
                                String ss = res.substring(idx);
                                sb.append(s);
                                final String info = sb.toString();
                                sb.setLength(0);
                                sb.append(ss);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        UartReadMsg(info);
                                    }
                                });
                            } else {
                                sb.append(res);
                            }
                        } else {
                            if (res.contains("OK") || res.contains("READY")
                                    || res.contains("6ERROR")
                                    || res.contains("\"unlock\"ERROR")
                                    || res.contains("SIMnotinserted")) {
                                if (res.contains("OK")) { // 正常指令反馈
                                    int idx = res.indexOf("OK") + 2;
                                    String s = res.substring(0, idx);
                                    String ss = res.substring(idx);
                                    sb.append(s);
                                    final String info = sb.toString();
                                    sb.setLength(0);
                                    sb.append(ss);
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            UartReadMsg(info);
                                        }
                                    });
                                } else if (res.contains("SIMnotinserted")) {
                                    int idx = res.indexOf("SIMnotinserted") + 14;
                                    String s = res.substring(0, idx);
                                    String ss = res.substring(idx);
                                    sb.append(s);
                                    final String info = sb.toString();
                                    sb.setLength(0);
                                    sb.append(ss);
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            UartReadMsg(info);
                                        }
                                    });
                                } else if (res.contains("6ERROR")) {
                                    int idx = res.indexOf("6ERROR") + 6;
                                    String s = res.substring(0, idx);
                                    String ss = res.substring(idx);
                                    sb.append(s);
                                    final String info = sb.toString();
                                    sb.setLength(0);
                                    sb.append(ss);
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            UartReadMsg(info);
                                        }
                                    });
                                } else if (res.contains("\"unlock\"ERROR")) {
                                    int idx = res.indexOf("\"unlock\"ERROR") + 13;
                                    String s = res.substring(0, idx);
                                    String ss = res.substring(idx);
                                    sb.append(s);
                                    final String info = sb.toString();
                                    sb.setLength(0);
                                    sb.append(ss);
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            UartReadMsg(info);
                                        }
                                    });
                                } else {// 检测是否插卡
                                    sb.append(res);
                                    final String info = sb.toString();
                                    sb.setLength(0);
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            UartReadMsg(info);
                                        }
                                    });
                                }
                            } else {
                                sb.append(res);
                            }
                        }
                    }
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void UartReadMsg(String info) {
        if (info!= null) {
            if (Nr5g.build().isQsFlag()) {
                Nr5g.build().parseQsData(info);
            } else {
                Nr5g.build().parseNormalData(info);
            }
        }
    }

    public SerialPort mSerialPort = null; // ttyS0
    public OutputStream mOutputStream;
    public InputStream mInputStream;
    private ReadThread ttyS0Read;
    private final Handler handler = new Handler();
}
