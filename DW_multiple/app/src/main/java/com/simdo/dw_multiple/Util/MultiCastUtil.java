/**
 * 1、路由或手机开热点，操作机接入可发送组播信息
 * 2、操作机开热点，则无法发送组播信息
 */
package com.simdo.dw_multiple.Util;

import android.content.Context;
import android.net.wifi.WifiManager;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MultiCastUtil {
    private static MultiCastUtil instance;

    public static MultiCastUtil build() {
        synchronized (MultiCastUtil.class) {
            if (instance == null) {
                instance = new MultiCastUtil();
            }
        }
        return instance;
    }

    public MultiCastUtil() {
        super();
    }

    public void init(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        multicastLock = wifiManager.createMulticastLock("multicast.test");
        multicastLock.acquire();
    }

    public void reInit() {
        if (multicastLock != null) {
            multicastLock.release();
        }
    }

    public void sendData() {
        AppLog.D("sendData");
        try {
            multicastSocket = new MulticastSocket(BROADCAST_PORT);
            serverAddress = InetAddress.getByName(BROADCAST_IP);
            multicastSocket.setTimeToLive(1);
            multicastSocket.joinGroup(serverAddress);
            new Thread() {
                @Override
                public void run() {
                    String ip = "192.168.43.1"; // 获取自己在局域网内的 IP
                    byte data[] = ip.getBytes();
                    DatagramPacket datagramPackage = new DatagramPacket(data, data.length, serverAddress, BROADCAST_PORT);
                    try {
                        while (true) {
                            // 每 2 秒往局域网中发送一次广播
                            multicastSocket.send(datagramPackage);
                            Thread.sleep(2000);
                            AppLog.D("sendData: " + ip);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private WifiManager.MulticastLock multicastLock;
    private static int BROADCAST_PORT = 5001;
    private static String BROADCAST_IP = "239.0.0.1";
    private MulticastSocket multicastSocket = null;
    private InetAddress serverAddress = null;
}