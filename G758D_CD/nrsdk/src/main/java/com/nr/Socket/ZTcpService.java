package com.nr.Socket;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.Logcat.SLog;
import com.nr.Gnb.Bean.GnbProtocol;
import com.nr.Gnb.Bean.Header;
import com.nr.Gnb.Bean.MsgBean;
import com.nr.Gnb.GnbOnlyType;
import com.nr.Gnb.Response.GnbStateRsp;
import com.nr.Socket.MessageControl.MessageController;
import com.nr.Socket.MessageControl.MessageHelper;
import com.nr.Socket.MessageControl.MessageProtocol;
import com.nr.Socket.MessageControl.MessageTransceiver;

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
        SLog.E("[ZTcpService] launch() launching = " + launching);
        if (launching) {
            SLog.D("[ZTcpService] 管理服务器正在运行");
            return;
        }
        // 初始配置
        launching = true;
        if (serverSocketThread != null) {
            serverSocketThread.interrupt();
            serverSocketThread = null;
        }
        serverSocketThread = new Thread() {
            @Override
            public void run() {
                while (mServerSocketManager.isAccept()) {
                    try {
                        if (mServerSocketManager.getServerSocket() == null) {
                            mServerSocketManager.setServerSocket(new ServerSocket(MessageProtocol.LOCAL_PORT));
                        }
                        Socket clientSocket = mServerSocketManager.getServerSocket().accept();

                        String address = clientSocket.getInetAddress().getHostAddress();
                        SLog.I("[ZTcpService] launch address = " + address);
                        for (int i = 0; i < clientSocketList.size(); i++) {//判断是否存在相同的客户端IP
                            if (clientSocketList.get(i).getSocket().getInetAddress().getHostAddress().equals(address)) {
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
                        clientSocketList.add(client);
                        client.startReadThread(new SocketReadThread(client));
                        // 偶然概率发生阻塞式 read 一直读到 len = -1，导致一直无法正常通信，因此在连接上之后，先发一次心跳
                        Header head = new Header(GnbProtocol.UI_2_gNB_OAM_MSG, GnbProtocol.UI_2_gNB_HEART_BEAT, 0);
                        GnbOnlyType cmd = new GnbOnlyType(head);
                        client.send("socket_" + address, cmd.getMsg());
                        SLog.I("[ZTcpService] clientSocketList size = " + clientSocketList.size() + ", address = " + address);
                    } catch (IOException e) {
                        SLog.E("[ZTcpService]: Exception:" + e);
                    }
                }
            }
        };
        serverSocketThread.start();
    }

    /**
     * 发送数据
     *
     * @param id： 基带板连接的IP
     * @param msg
     */
    public void send(String id, byte[] msg) {
        for (int i = 0; i < clientSocketList.size(); i++) {
            if (clientSocketList.get(i).getBsId().equals(id)) {
                clientSocketList.get(i).send(id, msg);
                break;
            }
        }
    }

    /**
     * 读16进制数据
     */
    class SocketReadThread extends Thread {
        SocketClient socket;
        private SocketReadThread(SocketClient socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            startHeartBeat();
            try {
                while (!socket.getSocket().isClosed()) {
                    Thread.sleep(1);
                    byte[] msg = null;
                    int len;
                    // 发现上号速度很快时，读取1024存在取不完整数据
                    // 这里调整为68字节(带有iphone标志)和64字节(不带有iphone标志)的公倍数次上号外加一次心跳的字节， 再加 1，读取多出1说明数据不完整
                    // 即：68与64公倍数为1088 + 心跳176 = 1264 + 1 = 1265
                    int readBufferSize = 1265;
                    byte[] buffer = new byte[readBufferSize];
                    do {
                        DataInputStream dis = new DataInputStream(socket.getSocketInputStream());
                        len = dis.read(buffer, 0, readBufferSize);
                        if (len <= 0) break;
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
                    } while (len == readBufferSize);

                    if (msg == null) continue;

                    if (msg.length >= GENERAL_MESSAGE_HEADER) {
                        int offset = 0;
                        int currentMsgSize = msg.length;
                        //当条命令的数组

                        byte[] tmpMsgBuf = new byte[currentMsgSize];
                        System.arraycopy(msg, offset, tmpMsgBuf, 0, currentMsgSize);
                        // 数据待处理
                        String id = socket.getBsId();
                        if (id.contains("socket_")) { // 先解析设备ID
                            if (currentMsgSize > 332) return;
                            GnbStateRsp rsp = MessageHelper.build().gnbHeartState(id,parseMsg(tmpMsgBuf));
                            if (rsp != null) {
                                if (rsp.getDeviceId() != null && !rsp.getDeviceId().equals("")) {
                                    socket.setBsId(rsp.getDeviceId());
                                    SLog.I("[ZTcpService] rsp.getDeviceId(): " + rsp.getDeviceId());
                                    socketStateChange(rsp.getDeviceId(), MessageProtocol.SOCKET.STATE_CONNECTED);
                                    MessageController.build().addMsgTypeList(rsp.getDeviceId(), socket.getIp());
                                    for (int i = 0; i < clientSocketList.size(); i++) {
                                        SLog.D("[ZTcpService] List Id " + i + " : " + clientSocketList.get(i).getBsId());
                                    }
                                    handleMessageBuf(rsp.getDeviceId(), tmpMsgBuf);
                                }
                            }
                        } else {
                            Message message = new Message();
                            message.obj = new MsgBean(id, tmpMsgBuf);
                            mMessageHandler.sendMessage(message);
                        }
                    }
                }
            } catch (Exception e) {
                SLog.E("[ZTcpService] SocketReadThread() error : " + e.getMessage());
                //socketStateChange(socket.getBsId(), MessageProtocol.SOCKET.STATE_DISCONNECT);
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
            if (b.length() == 1) b = "0" + b;
            else if (b.length() > 2) b = b.substring(b.length() - 2);
            sb.append(b).append(",");
        }
        String tmpStr = sb.toString();
        tmpStr = tmpStr.substring(0, tmpStr.length() - 1);
        //Log.d("tmpMsgBuf", "parseMsg: " + tmpStr);
        return tmpStr;
    }

    public boolean isConnected() {
        return clientSocketList.size() > 0;
    }

    private void reconnect(String id, String ip) { // 掉线时清理记录，进入初始阶段，等待广播接收然后重连
        SLog.D("[ZTcpService] reconnect id: " + id);
        UdpControl.build().removeIP(ip);
        heartTimeMap.remove(id);
        MessageController.build().removeMsgTypeList(id);
        for (int i = 0; i < clientSocketList.size(); i++) {
            if (clientSocketList.get(i).getBsId().equals(id)) {
                clientSocketList.get(i).stopReadThread();
                clientSocketList.get(i).CloseIO();
                clientSocketList.remove(i);
                break;
            }
        }
    }

    public void setHeartTime(String id) {
        heartTimeMap.put(id, System.currentTimeMillis());
    }

    /**
     * 数据解析
     */
    private void handleMessageBuf(String id, byte[] tmpMsgBuf) {
        transceiver.handleMessage(id, tmpMsgBuf);
    }
    public void socketStateChange( final int state) {
        socketStateChange(MessageHelper.build().getDeviceId(),state);
    }
    public void socketStateChange(final String id, final int state) {
        if (handler == null) {
            //不加Looper.getMainLooper()会导致postDelayed\post不执行
            handler = new Handler(Looper.getMainLooper());
        }
        if (clientSocketList.size() == 0) {
            return;
        }
        if (state == MessageProtocol.SOCKET.STATE_DISCONNECT){
            MessageHelper.build().setDeviceId("");
        }
        try {
            for (int i = clientSocketList.size() - 1; i >= 0; i--) {
                SLog.D("socketStateChange clientSocketList.get(i).getBsId() = " + clientSocketList.get(i).getBsId() + ", id = " + id);
                if (clientSocketList.get(i).getBsId().equals(id)) {
                    final int lastState = clientSocketList.get(i).getConnectState();
                    if (lastState != state) {
                        clientSocketList.get(i).setConnectState(state);
                        UdpControl.build().setConnectState(clientSocketList.get(i).getIp(), state == MessageProtocol.SOCKET.STATE_CONNECTED);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                for (OnSocketChangeListener connectListener : connectListenerList) {
                                    if (connectListener != null) {
                                        SLog.D("[ZTcpService] onSocketStateChange() id = " + id + ", lastState = " + lastState + ", state = " + state);
                                        connectListener.onSocketStateChange(id, lastState, state);
                                    }
                                }
                            }
                        });
                        if (state == MessageProtocol.SOCKET.STATE_DISCONNECT)
                            reconnect(id, clientSocketList.get(i).getIp());
                    }
                    break;
                }
            }
        } catch (IndexOutOfBoundsException e) {
            SLog.D("[ZTcpService] onSocketStateChange() id = " + id + ", error " + e.getMessage());
        }
    }

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
                            for (int i = 0; i < clientSocketList.size(); i++) {
                                String id = clientSocketList.get(i).getBsId();
                                if (!id.contains("socket_")) {
                                    if (heartTimeMap.containsKey(id)) {
                                        Long lastTime = heartTimeMap.get(id);
                                        if (lastTime != null) {
                                            if (System.currentTimeMillis() - lastTime > 30000) {
                                                heartTimeMap.put(id, System.currentTimeMillis());
                                                SLog.I(id + "  device heart time out  ");
                                                socketStateChange(id, 100);
                                            }
                                        }
                                    }
                                    MessageController.build().setOnlyCmd(id, GnbProtocol.UI_2_gNB_HEART_BEAT);
                                }
                                try {
                                    Thread.sleep(100); // 跑完一个休眠100毫秒，避免心跳发生冲突而不发到单板
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        } catch (Exception e) {
                            SLog.E("[ZTcpService] startHeartBeat error = " + e.getMessage());
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
        if (clientSocketList.size() > 0) {
            for (int i = clientSocketList.size() - 1; i >= 0; i--) {
//                socketStateChange(clientSocketList.get(i).getBsId(), MessageProtocol.SOCKET.STATE_DISCONNECT);
                clientSocketList.get(i).stopReadThread();
                clientSocketList.get(i).CloseIO();
                clientSocketList.remove(i);
            }
        }

        if (mServerSocketManager.getServerSocket() != null) {
            try {
                mServerSocketManager.getServerSocket().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getHostIp(String id) {
        for (int i = 0; i < clientSocketList.size(); i++) {
            if (clientSocketList.get(i).getBsId().equals(id)) {
                return clientSocketList.get(i).getIp();
            }
        }
        return null;
    }

    public void onDestroy() {
        isRun = false;
        if (heartThread!=null){
            heartThread = null;
        }
        MessageController.build().close();

        if (serverSocketThread != null) {
            serverSocketThread.interrupt();
            serverSocketThread = null;
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
        if (mMessageHandler != null) {
            mMessageHandler.removeCallbacksAndMessages(null);
            mMessageHandler = null;
        }
        closeSocket();
        MessageTransceiver.build().stopThread();
    }

    public void addConnectListener(OnSocketChangeListener connectListener) {
        if (!connectListenerList.contains(connectListener))
            connectListenerList.add(connectListener);
    }

    public void removeConnectListener(OnSocketChangeListener connectListener) {
        connectListenerList.remove(connectListener);
    }

    private static final int GENERAL_MESSAGE_HEADER = 4;
    private final List<OnSocketChangeListener> connectListenerList = new ArrayList<>();
    private Handler handler;
    @SuppressLint("HandlerLeak")
    private Handler mMessageHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            MsgBean bean = (MsgBean) msg.obj;
            handleMessageBuf(bean.getId(), bean.getMsg());
        }
    };
    private MessageTransceiver transceiver;
    private Thread serverSocketThread;
    private boolean launching = false;
    private final ServerSocketManager mServerSocketManager;
    private final ArrayList<SocketClient> clientSocketList = new ArrayList<>();
    private final HashMap<String,Long> heartTimeMap = new HashMap<>();
}
