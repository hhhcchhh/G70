package com.dwdbsdk.Socket;

import android.os.Handler;
import android.os.Looper;

import com.dwdbsdk.Interface.SocketStateListener;
import com.dwdbsdk.Logcat.SdkLog;
import com.dwdbsdk.MessageControl.MessageController;
import com.dwdbsdk.MessageControl.MessageHelper;
import com.dwdbsdk.MessageControl.MessageProtocol;
import com.dwdbsdk.MessageControl.MessageTransceiver;
import com.dwdbsdk.Response.DB.MsgStateRsp;
import com.dwdbsdk.Response.DW.GnbStateRsp;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ZTcpService {
    private static ZTcpService instance;
    public static ZTcpService build() {
        synchronized (ZTcpService.class) {
            if (instance == null) {
                instance = new ZTcpService();
            }
        }
        return instance;
    }
    public ZTcpService() {
        //不加Looper.getMainLooper()会导致postDelayed\post不执行
        handler = new Handler(Looper.getMainLooper());
        mServerSocketManager = new ServerSocketManager();
        mServerSocketManager.setAccept(true);
    }

    public void registerHandler(MessageTransceiver transceiver) {
        this.transceiver = transceiver;
    }

    public void launch() {
        if (launching) {
            SdkLog.D("service launched");
            return;
        }
        SdkLog.I("service launch");
        launching = true;
        launchDW();
        launchDB();
    }

    private void launchDW(){
        if (serverSocketThread != null) {
            serverSocketThread.interrupt();
            serverSocketThread = null;
        }
        serverSocketThread = new Thread() {
            @Override
            public void run() {
                while (mServerSocketManager.isAccept()) {
                    try {
                        if(mServerSocketManager.getServerSocket() == null){
                            mServerSocketManager.setServerSocket(new ServerSocket(ConnectProtocol.DW_LOCAL_PORT));
                        }
                        Socket clientSocket = mServerSocketManager.getServerSocket().accept();

                        String address = clientSocket.getInetAddress().getHostAddress();
                        SdkLog.I("service accept address = " + address);
                        for(int i = 0; i < clientSocketList.size(); i++){//判断是否存在相同的客户端IP
                            if(clientSocketList.get(i).getIp().equals(address)){
                                MessageController.build().removeMsgTypeList(address);
                                clientSocketList.get(i).stopReadThread();
                                clientSocketList.get(i).CloseIO();
                                clientSocketList.remove(i);
                                break;
                            }
                        }
                        //获取客户端输入流
                        InputStream is = clientSocket.getInputStream();
                        InputStreamReader isr = new InputStreamReader(is);
                        BufferedReader br = new BufferedReader(isr);
                        //获取客户端输出流
                        OutputStream os = clientSocket.getOutputStream();
                        PrintWriter pw = new PrintWriter(os);
                        //设置输入流
                        SocketClient client = new SocketClient();
                        client.setBsId("socket_" + address);

                        client.setSocket(clientSocket);
                        client.setSocketInputStream(is);
                        client.setSocketInputStreamReader(isr);
                        client.setSocketBufferedReader(br);
                        client.setHeartCnt(5); // 心跳计时
                        //设置输出流
                        client.setSocketOutputStream(os);
                        client.setSocketPrintWriter(pw);

                        client.setIp(address);
                        client.startReadThread(new DWSocketReadThread(client));
                        clientSocketList.add(client);
                        MessageController.build().sendHeartBeat("G70", client.getBsId());  // 连接上先发送一次心跳
                        SdkLog.I("service add client ok, client socket list = " + clientSocketList);

                    } catch (IOException e) {
                        SdkLog.E("service add client error: " + e.getMessage());
                    }
                }
            }
        };
        serverSocketThread.start();
    }

    private void launchDB(){
        if (clientSocketThread != null) {
            clientSocketThread.interrupt();
            clientSocketThread = null;
        }
        clientSocketThread = new Thread() {
            @Override
            public void run() {
                while (launching) { // 多个终端连接需打开，然后建立一个客户端列表
                    SocketClient socketClient = null;
                    try {
                        ArrayList<String> ipList = new ArrayList<>(UdpControl.build().getDBIpList());
                        if (serverSocketList.size() < ipList.size()) {
                            for (int i = 0; i < ipList.size(); i++) {
                                boolean isAdd = true;
                                for (int j = 0; j < serverSocketList.size(); j++) {
                                    if (ipList.get(i).equals(serverSocketList.get(j).getIp())) {
                                        isAdd = false;
                                        break;
                                    }
                                }
                                if (isAdd) {
                                    SdkLog.I("service start add ip = " + ipList.get(i));
                                    socketClient = new SocketClient();
                                    socketClient.setBsId("socket_" + ipList.get(i));
                                    socketClient.setIp(ipList.get(i));

                                    Socket socket = new Socket(ipList.get(i), ConnectProtocol.DB_REMOTE_PORT);

                                    OutputStream os = socket.getOutputStream();
                                    PrintWriter pw = new PrintWriter(os);

                                    InputStream is = socket.getInputStream();
                                    InputStreamReader isr = new InputStreamReader(is);
                                    BufferedReader br = new BufferedReader(isr);

                                    socketClient.setSocketInputStream(is);
                                    socketClient.setSocketInputStreamReader(isr);
                                    socketClient.setSocketBufferedReader(br);
                                    socketClient.setSocketOutputStream(os);
                                    socketClient.setSocketPrintWriter(pw);
                                    socketClient.setHeartCnt(5);

                                    socketClient.setSocket(socket);
                                    socketClient.startReadThread(new DBSocketReadThread(socketClient));
                                    serverSocketList.add(socketClient);
                                    MessageController.build().sendHeartBeat("G10", socketClient.getBsId());  // 连接上先发送一次心跳
                                    SdkLog.I("service add server ok, server socket list = " + serverSocketList);
                                }
                            }
                        }
                    } catch (IOException e) {
                        SdkLog.E("service add server error: " + e);
                        if (socketClient != null) {
                            socketClient.stopReadThread();
                            socketClient.CloseIO();
                            serverSocketList.remove(socketClient);
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        clientSocketThread.start();
    }

    /**
     * 发送数据
     */
    public void send(String id, byte[] msg) {

        for (int i = 0; i < clientSocketList.size(); i++) {
            if (clientSocketList.get(i).getBsId().equals(id)) {
                clientSocketList.get(i).send(id, msg);
                return;
            }
        }
        for (int i = 0; i < serverSocketList.size(); i++) {
            if (serverSocketList.get(i).getBsId().equals(id)) {
                serverSocketList.get(i).send(id, msg);
                return;
            }
        }
    }
    /**
     * 读16进制数据
     */
    class DWSocketReadThread extends Thread {
        SocketClient socket;
        private DWSocketReadThread(SocketClient socket) {
            this.socket = socket;
        }
        @Override
        public void run() {
            startHeartBeat();   //5秒发一次
            try {
                byte[] msg;
                int len;
                byte[] buffer = new byte[1024];
                int currentMsgSize;
                while (!socket.getSocket().isClosed()) {
                    Thread.sleep(10);
                    DataInputStream dis = new DataInputStream(socket.getSocketInputStream());
                    len = dis.read(buffer, 0, 1024);
                    if (len <= 0) {
                        break;
                    }
                    msg = new byte[len];
                    System.arraycopy(buffer, 0, msg, 0, msg.length);

                    if (msg.length >= GENERAL_MESSAGE_HEADER) {
                        int offset = 0;
                        currentMsgSize = msg.length;
                        //当条命令的数组
                        byte[] tmpMsgBuf = new byte[currentMsgSize];
                        System.arraycopy(msg, offset, tmpMsgBuf, 0, currentMsgSize);
                        String parseMsg = parseMsg(tmpMsgBuf);
                        GnbStateRsp rsp = MessageHelper.build().DWHeartState(socket.getBsId(), parseMsg);

                        // 数据待处理
                        //1分钟走1次
                        if (socket.getBsId().contains("socket_")) { // 先解析设备ID
                            if (rsp != null) {
                                if (rsp.getDeviceId() != null && !rsp.getDeviceId().equals("")) {
                                    socket.setBsId(rsp.getDeviceId());
                                    SdkLog.I("service client get deviceId: " + rsp.getDeviceId());
                                    socketStateChange(rsp.getDeviceId(), MessageProtocol.STATE.STATE_CONNECTED);
                                    MessageController.build().addMsgTypeList(rsp.getDeviceId());

                                    StringBuilder sb = new StringBuilder();

                                    for (int i = 0; i < clientSocketList.size(); i++) {
                                        sb.append(clientSocketList.get(i).getBsId()).append("\t");
                                    }
                                    SdkLog.D("service client list all Id: " + sb);

                                    handleMessage("DW", rsp.getDeviceId(), parseMsg);
                                }
                            }
                        } else {
                            //跟上面的再走4次（20秒），然后停止40秒
                            handleMessage("DW", socket.getBsId(), parseMsg);
                        }
                    }
                }
            } catch (Exception e) {
                SdkLog.E("service DW read error : " + e);
                socketStateChange(socket.getBsId(), MessageProtocol.STATE.STATE_DISCONNECT);
            }
        }
    }

    /**
     * 读16进制数据
     */
    class DBSocketReadThread extends Thread {
        SocketClient socket;
        private DBSocketReadThread(SocketClient socket) {
            this.socket = socket;
        }
        @Override
        public void run() {
            startHeartBeat(); // 启动每10S发送心跳包
            try {
                byte[] msg = null;
                int len;
                byte[] buffer = new byte[1024];
                int currentMsgSize;
                while (socket != null && !socket.getSocket().isClosed()) {

                    Thread.sleep(10);
                    do {
                        DataInputStream dis = new DataInputStream(socket.getSocketInputStream());
                        len = dis.read(buffer, 0, 1024);
                        if (len <= 0) {
                            break;
                        }
                        if (msg != null) {
                            byte[] temp = new byte[len];
                            byte[] result = new byte[msg.length + len];
                            System.arraycopy(buffer, 0, temp, 0, temp.length);
                            System.arraycopy(msg, 0, result, 0, msg.length);
                            System.arraycopy(temp, 0, result, msg.length, temp.length);
                            msg = result;
                        } else {
                            msg = new byte[len];
                            System.arraycopy(buffer, 0, msg, 0, msg.length);
                        }
                    } while (len == 1024);

                    if (msg == null) {
                        continue;
                    }
                    if (msg.length >= GENERAL_MESSAGE_HEADER) {
                        int offset = 0;
                        currentMsgSize = msg.length;
                        //当条命令的数组
                        byte[] tmpMsgBuf = new byte[currentMsgSize];
                        System.arraycopy(msg, offset, tmpMsgBuf, 0, currentMsgSize);
                        String parseMsg = parseMsg(tmpMsgBuf);
                        MsgStateRsp rsp = MessageHelper.build().DBHeartState(parseMsg(tmpMsgBuf));
                        // 数据待处理
                        if (socket.getBsId().contains("socket_")) { // 先解析设备ID
                            if (rsp != null) {
                                if (rsp.getDeviceId() != null && !rsp.getDeviceId().equals("")) {
                                    socket.setBsId(rsp.getDeviceId());
                                    SdkLog.I("service server get deviceId: " + rsp.getDeviceId());
                                    socketStateChange(rsp.getDeviceId(), MessageProtocol.STATE.STATE_CONNECTED);
                                    MessageController.build().addMsgTypeList(rsp.getDeviceId());

                                    StringBuilder sb = new StringBuilder();

                                    for (int i = 0; i < serverSocketList.size(); i++) {
                                        sb.append(serverSocketList.get(i).getBsId()).append("\t");
                                    }
                                    SdkLog.D("service server list all Id: " + sb);

                                    handleMessage("DB", rsp.getDeviceId(), parseMsg);
                                }
                            }
                        } else {
                            handleMessage("DB", socket.getBsId(), parseMsg);
                        }

                    }
                    msg = null;
                }
            } catch (Exception e) {
                SdkLog.E("service DB read error : " + e);
                socketStateChange(socket.getIp(), MessageProtocol.STATE.STATE_DISCONNECT);
            }
        }
    }
    /**
     * 转换成可解析数据
     */
    private String parseMsg(byte[] msg) {
        StringBuilder sb = new StringBuilder();
        for (byte value : msg) {
            String b = Integer.toHexString(value);
            if (b.length() == 1) {
                b = "0" + b;
            } else if (b.length() > 2) {
                b = b.substring(b.length() - 2);
            }
            sb.append(b);
            sb.append(",");
        }
        String tmpStr = sb.toString();
        tmpStr = tmpStr.substring(0, tmpStr.length() - 1);
        //SdkLog.D("[ZTcpService] readMsg[ZTcpService]: " + tmpStr);
        return tmpStr;
    }

    public boolean isConnected() {
        return clientSocketList.size() > 0 || serverSocketList.size() > 0;
    }
    private void reconnect(String id, String ip){ // 掉线时清理记录，进入初始阶段，等待广播接收然后重连
        SdkLog.D("service reconnect id: " + id);
        UdpControl.build().removeIP(ip);
        heartTimeMap.remove(id);
        MessageController.build().removeMsgTypeList(id);
        for(int i = 0; i < clientSocketList.size(); i++){
            if (clientSocketList.get(i).getBsId().equals(id)){
                clientSocketList.get(i).stopReadThread();
                clientSocketList.get(i).CloseIO();
                clientSocketList.remove(i);
                return;
            }
        }
        for(int i = 0; i < serverSocketList.size(); i++){
            if (serverSocketList.get(i).getBsId().equals(id)){
                serverSocketList.get(i).stopReadThread();
                serverSocketList.get(i).CloseIO();
                serverSocketList.remove(i);
                break;
            }
        }
    }
    public void setHeartTime(String id){
        heartTimeMap.put(id, System.currentTimeMillis());
    }
    /**
     * 数据解析
     */
    private void handleMessage(String type, String id, byte[] tmpMsgBuf) {
        transceiver.handleMessage(type, id, tmpMsgBuf);
    }
    private void handleMessage(String type, String id, String tmpMsg) {
        transceiver.handleMessage(type, id, tmpMsg);
    }
	public void socketStateChange(final String id, final int state) {
        if (handler == null) {
            //不加Looper.getMainLooper()会导致postDelayed\post不执行
            handler = new Handler(Looper.getMainLooper());
        }
        for (int i = 0; i < clientSocketList.size(); i++) {
            if (clientSocketList.get(i).getBsId().equals(id)) {
                final int lastState = clientSocketList.get(i).getConnectState();
                if (lastState != state) {
                    clientSocketList.get(i).setConnectState(state);
                    UdpControl.build().setConnectState(clientSocketList.get(i).getIp(), state == MessageProtocol.STATE.STATE_CONNECTED);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            for (SocketStateListener connectListener : connectListenerList) {
                                if (connectListener != null) {
                                    SdkLog.D("service socket state change , id = " + id + ", lastState = " + lastState + ", state = " + state);
                                    connectListener.OnSocketStateChange(id, lastState, state);
                                }
                            }
                        }
                    });

                }
                if (state == MessageProtocol.STATE.STATE_DISCONNECT) reconnect(id, clientSocketList.get(i).getIp());
                break;
            }
        }
        //单兵
        for (int i = 0; i < serverSocketList.size(); i++) {
            if (serverSocketList.get(i).getBsId().equals(id)) {
                final int lastState = serverSocketList.get(i).getConnectState();
                if (lastState != state) {
                    serverSocketList.get(i).setConnectState(state);
                    UdpControl.build().setConnectState(serverSocketList.get(i).getIp(), state == MessageProtocol.STATE.STATE_CONNECTED);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            for (SocketStateListener connectListener : connectListenerList) {
                                if (connectListener != null) {
                                    SdkLog.D("service socket state change , id = " + id + ", lastState = " + lastState + ", state = " + state);
                                    connectListener.OnSocketStateChange(id, lastState, state);
                                }
                            }
                        }
                    });

                }
                if (state == MessageProtocol.STATE.STATE_DISCONNECT) reconnect(id, serverSocketList.get(i).getIp());
                break;
            }
        }
    }
    /**
     * 同步时间，并开始发送心跳数据
     */
    private boolean isRun = true;
    private Thread heartThread;

    /**
     * 发送心跳数据
     */
    public void startHeartBeat() {
        //发送心跳
        if (heartThread == null) {
            heartThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isRun) {
                        try {
                            for(int i = 0; i < clientSocketList.size(); i++){
                                String id = clientSocketList.get(i).getBsId();
                                if (!id.contains("socket_")) {
                                    if (heartTimeMap.containsKey(id)) {
                                        Long lastTime = heartTimeMap.get(id);
                                        if (lastTime != null) {
                                            if (System.currentTimeMillis() - lastTime > 30000) {
                                                heartTimeMap.put(id, System.currentTimeMillis());
                                                socketStateChange(id, 100);
                                            }
                                        }
                                    }
//                                    if (clientSocketList.get(i).getHeartCnt() > 4) {
//                                        clientSocketList.get(i).setHeartCnt(0);
                                        MessageController.build().sendHeartBeat("G70", id);
//                                    } else {
//                                        clientSocketList.get(i).incHeartCnt();
//                                    }
                                }
                            }

                            for (int i = 0; i < serverSocketList.size(); i++) {
                                String id = serverSocketList.get(i).getBsId();
                                if (!id.contains("socket_")){
                                    if (heartTimeMap.containsKey(id)) {
                                        Long lastTime = heartTimeMap.get(id);
                                        if (lastTime != null) {
                                            if (System.currentTimeMillis() - lastTime > 30000) {
                                                heartTimeMap.put(id, System.currentTimeMillis());
                                                socketStateChange(id, 100);
                                            }
                                        }
                                    }
//                                    if (serverSocketList.get(i).getHeartCnt() > 4) {
//                                        serverSocketList.get(i).setHeartCnt(0);
                                        MessageController.build().sendHeartBeat("G10", id);
//                                    } else {
//                                        serverSocketList.get(i).incHeartCnt();
//                                    }
                                }
                            }
                        }catch (Exception e){
                            SdkLog.E("service heart beat error: " + e.getMessage());
                        }
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
            heartThread.start();
        }
    }
    /**
     * 关闭所有SOCKET
     */
    private void closeSocket() {
        mServerSocketManager.setAccept(false);
        //关闭SocketIO
        for(int i = clientSocketList.size() - 1; i >= 0; i--){
//            socketStateChange(clientSocketList.get(i).getBsId(), MessageProtocol.STATE.STATE_DISCONNECT);
            clientSocketList.get(i).stopReadThread();
            clientSocketList.get(i).CloseIO();
            clientSocketList.remove(i);
        }
        for(int i = serverSocketList.size() - 1; i >= 0; i--){
//            socketStateChange(serverSocketList.get(i).getBsId(), MessageProtocol.STATE.STATE_DISCONNECT);
            serverSocketList.get(i).stopReadThread();
            serverSocketList.get(i).CloseIO();
            serverSocketList.remove(i);
        }
        if(mServerSocketManager.getServerSocket() != null){
            try {
                mServerSocketManager.getServerSocket().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getHostIp(String id) {
        for(int i = 0; i < clientSocketList.size(); i++){
            if (clientSocketList.get(i).getBsId().equals(id)) {
                return clientSocketList.get(i).getIp();
            }
        }
        for(int i = 0; i < serverSocketList.size(); i++){
            if (serverSocketList.get(i).getBsId().equals(id)) {
                return serverSocketList.get(i).getIp();
            }
        }
        return null;
    }

    public void onDestroy() {
        launching = false;
        isRun = false;
        if (heartThread!=null){
            heartThread = null;
        }
        MessageController.build().close();
        if (timerHeart != null) {
            timerHeart.cancel();
            timerHeart = null;
        }
        if (serverSocketThread != null) {
            serverSocketThread.interrupt();
            serverSocketThread = null;
        }
        if (clientSocketThread != null) {
            clientSocketThread.interrupt();
            clientSocketThread = null;
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        closeSocket();
        removeListener();
    }

    public void addConnectListener(SocketStateListener connectListener) {
        if (!connectListenerList.contains(connectListener))
            connectListenerList.add(connectListener);
    }

    public void removeConnectListener(SocketStateListener connectListener){
        connectListenerList.remove(connectListener);
    }

    public void removeListener(){
        connectListenerList.clear();
    }

    private static final int GENERAL_MESSAGE_HEADER = 4;
    private final List<SocketStateListener> connectListenerList = new ArrayList<>();
    private Handler handler;
    private MessageTransceiver transceiver;
    private Thread serverSocketThread;
    private Thread clientSocketThread;
    private Timer timerHeart;
    private boolean launching = false;
    private final ServerSocketManager mServerSocketManager;
    private final ArrayList<SocketClient> clientSocketList = new ArrayList<>();
    private final ArrayList<SocketClient> serverSocketList = new ArrayList<>();
    private final HashMap<String, Long> heartTimeMap = new HashMap<>();
}
