/**
 * TCP和UDP的区别：
 * <p>
 * TCP（Transimission Control Protocol）即 传输控制协议：是面向连接的协议， 强调稳定可靠的连接，建立连接“三次握手”，
 * 断开连接需要“四次分手”，现常用于网络间视频与语音通话，及相应的网络间可靠的信息流通。
 * <p>
 * UDP（User Datagram Protocol）即 用户数据报协议：是面向报文的协议，两端无需建立连接即可传输数据，
 * 速度和效率都是TCP所不能比拟的，常用于局域网内数据互通，缓存读取，游戏及程序内高精度文件传输，NAT穿透等。
 * <p>
 * <p>
 * UDP在Android中的应用：
 * <p>
 * 两端若需要建立UDP连接，
 * <p>
 * 对于发送方：需要获取当前网络的host IP及自定的端口（建议大于1024）。
 * <p>
 * 对于接收方：需要设定UDP连接的端口（与发送方端口保持一致），即可自动接收消息，其中该接收过程是阻塞式的，
 * 也就是说它一直在接收，当接收到了才会进行下一步（若接收到消息后还想继续接收，这时候需要把socket关闭，再能重复进行接收动作）
 * <p>
 * 注意事项：由于接收动作是阻塞式的，因此不能放在主线程里（ANR），需要新建一个线程完成该操作。
 * 同时接收和发送两个动作不能放在同一个设备上（因为端口一致会导致冲突），建议两台设备或虚拟机进行调试。
 */
package com.nr.Socket;

import android.os.Handler;
import android.os.Looper;

import com.Logcat.SLog;
import com.nr.Socket.MessageControl.MessageProtocol;
import com.nr.Socket.Bean.HotIpBean;

import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class UdpControl {
    public static UdpControl build() {
        synchronized (UdpControl.class) {
            if (instance == null) {
                instance = new UdpControl();
            }
        }
        return instance;
    }

    public UdpControl() {
    }

    public void startUdp() {
        SLog.I("[UdpControl]  startUdp()");
        if (receiveUdpThread != null) {
            receiveUdpThread.interrupt();
            receiveUdpThread = null;
        }
        receiveUdpThread = new UdpReceiveThread();
        receiveUdpThread.start();
    }

    public void restartUdp() {
        SLog.I("[UdpControl]  restartUdp()");
        closeSocket();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                startUdp();
            }
        }, 3000);
    }

    public void closeSocket() {
        SLog.I("[UdpControl] closeSocket()");
        if (udpSocketReceive != null) {
            if (udpSocketReceive.isConnected()){
                udpSocketReceive.disconnect();
                udpSocketReceive.close();
                udpSocketReceive = null;
            }
        }
        if (receiveUdpThread != null) {
            receiveUdpThread.interrupt();
            receiveUdpThread = null;
        }

    }

    /**
     * UPD接收线程
     */
    public class UdpReceiveThread extends Thread {
        @Override
        public void run() {
            while (isAlive()) { //循环接收，isAlive() 判断防止无法预知的错误
                try {
                    sleep(100); //让它好好休息一会儿
                    byte[] data = new byte[1024];
                    if (udpSocketReceive == null) {
                        udpSocketReceive = new DatagramSocket(null);
                        udpSocketReceive.setReuseAddress(true);
                        udpSocketReceive.bind(new InetSocketAddress(MessageProtocol.LOCAL_UDP_PORT));
                    }
                    DatagramPacket packet = new DatagramPacket(data, data.length);
                    SLog.D("UdpReceiveThread receive wait...");
                    udpSocketReceive.receive(packet); //阻塞式，接收发送方的 packet
                    String result = new String(packet.getData(), packet.getOffset(), packet.getLength()); //packet 转换
                    SLog.D("UdpReceiveThread receive: " + result);
                    parseData(result);
                } catch (Exception e) {
                    SLog.D("UdpReceiveThread receive error : " + e);
                    restartUdp();
                    break;
                }
            }
        }
    }

    private synchronized void parseData(String message) {
        ////{"type":1,"dev_name":"simpie_g70","bt_name":"g70bt1111","wifi_ip":"192.168.100.130"}
        try {
            JSONObject js = new JSONObject(message);

            int type = js.getInt("type");
            String dev_name = js.getString("dev_name");
            String bt_name = js.getString("bt_name");
            String wifi_ip = js.getString("wifi_ip");

            SLog.I("UdpReceiveThread device info: type = " + type + ", dev_name = " + dev_name + ", bt_name = " + bt_name + ", wifi_ip = " + wifi_ip);

            if (checkIP(wifi_ip)) {
                boolean isAdd = true;
                for (HotIpBean bean : ipList) {
                    if (bean.getIp().equals(wifi_ip)) {
                        isAdd = false;
                        if (!bean.isConnect()) udpSendDataToBroad(wifi_ip);
                        break;
                    }
                }
                if (isAdd) {
                    ipList.add(new HotIpBean(wifi_ip));
                    udpSendDataToBroad(wifi_ip);
                }
            }
        } catch (Exception e) {
            SLog.E("UdpReceiveThread parseData error: " + e);
        }
    }

    public synchronized void setConnectState(String ip, boolean state) {
        for (int i = 0; i < ipList.size(); i++) {
            if (ipList.get(i).getIp().equals(ip)) {
                ipList.get(i).setConnect(state);
                break;
            }
        }
    }
    public void removeIP(String ip) {
        for (int i = 0; i < ipList.size(); i++) {
            if (ipList.get(i).getIp().equals(ip)) {
                ipList.remove(ipList.get(i));
                break;
            }
        }
    }

    private void udpSendDataToBroad(String ip) {
        try {
            DatagramSocket socket = new DatagramSocket(ConnectProtocol.LOCAL_PORT); //本地口号
            InetAddress address = InetAddress.getByName(ip); //对端IP
            String data = "I love you: " + ip;
            byte[] dataByte = data.getBytes(); //建立数据
            DatagramPacket packet = new DatagramPacket(dataByte, dataByte.length, address, ConnectProtocol.REMOTE_PORT); //通过该数据建包
            socket.send(packet); //开始发送该包
            socket.close();
            SLog.D("UdpReceiveThread SendData To Broad, data: " + data);
        } catch (Exception e) {
            SLog.E("UdpReceiveThread SendData To Broad, error: " + e);
        }
    }

    private boolean checkIP(String str) {
        Pattern pattern = Pattern.compile("^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]"
                + "|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$");
        return pattern.matcher(str).matches();
    }

    private final ArrayList<HotIpBean> ipList = new ArrayList<>();
    private static UdpControl instance;
    private UdpReceiveThread receiveUdpThread;
    private DatagramSocket udpSocketReceive;
}
