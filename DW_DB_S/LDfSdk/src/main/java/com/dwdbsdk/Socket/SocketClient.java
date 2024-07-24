package com.dwdbsdk.Socket;

import com.dwdbsdk.Logcat.SdkLog;
import com.dwdbsdk.MessageControl.MessageProtocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketClient {
    private String bsId = "", ip = ""; // 基带版本ID
    private boolean isReading = false;
    private Thread readThread;
    private Socket socket;
    private InputStream socketInputStream;
    private InputStreamReader socketInputStreamReader;
    private BufferedReader socketBufferedReader;
    private OutputStream socketOutputStream;
    private PrintWriter socketPrintWriter;
    private int connectState = MessageProtocol.STATE.STATE_DISCONNECT;
    private int heartCnt;

    public int getConnectState() {
        return connectState;
    }

    public void setConnectState(int connectState) {
        this.connectState = connectState;
    }

    public int getHeartCnt() {
        return heartCnt;
    }

    public void setHeartCnt(int heartCnt) {
        this.heartCnt = heartCnt;
    }

    public void incHeartCnt() {
        this.heartCnt++;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getBsId() {
        return bsId;
    }

    public void setBsId(String bsId) {
        this.bsId = bsId;
    }

    public void setReading(boolean reading) {
        isReading = reading;
    }

    public Thread getReadThread() {
        return readThread;
    }

    public void setReadThread(Thread readThread) {
        this.readThread = readThread;
    }

    public InputStream getSocketInputStream() {
        return socketInputStream;
    }

    public void setSocketInputStream(InputStream socketInputStream) {
        this.socketInputStream = socketInputStream;
    }

    public InputStreamReader getSocketInputStreamReader() {
        return socketInputStreamReader;
    }

    public void setSocketInputStreamReader(InputStreamReader socketInputStreamReader) {
        this.socketInputStreamReader = socketInputStreamReader;
    }

    public BufferedReader getSocketBufferedReader() {
        return socketBufferedReader;
    }

    public void setSocketBufferedReader(BufferedReader socketBufferedReader) {
        this.socketBufferedReader = socketBufferedReader;
    }

    public OutputStream getSocketOutputStream() {
        return socketOutputStream;
    }

    public void setSocketOutputStream(OutputStream socketOutputStream) {
        this.socketOutputStream = socketOutputStream;
    }

    public PrintWriter getSocketPrintWriter() {
        return socketPrintWriter;
    }

    public void setSocketPrintWriter(PrintWriter socketPrintWriter) {
        this.socketPrintWriter = socketPrintWriter;
    }

    public void stopReadThread() {
        if (readThread != null) {
            isReading = false;
            readThread.interrupt();
            readThread = null;
        } else {
            SdkLog.D("stop read thread is null");
        }
    }

    public void startReadThread(Thread readThread) {
        if (readThread != null) {
            isReading = true;
            this.readThread = readThread;
            this.readThread.start();
        } else {
            SdkLog.D("start read thread is null");
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public SocketClient() {

    }

    public boolean isReading() {
        return isReading;
    }

    public void CloseIO() {
        try {
            if (socketBufferedReader != null) {
                socketBufferedReader.close();
                socketBufferedReader = null;
            }
            if (socketPrintWriter != null) {
                socketPrintWriter.close();
                socketPrintWriter = null;
            }

            if (socketInputStreamReader != null) {
                socketInputStreamReader.close();
                socketInputStreamReader = null;
            }

            if (socketOutputStream != null) {
                socketOutputStream.close();
                socketOutputStream = null;
            }

            if (socketInputStream != null) {
                socketInputStream.close();
                socketInputStream = null;
            }

            if (socket != null) {
                socket.close();
                socket = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 循环遍历客户端集合，给每个客户端都发送信息。
     */
    public void send(final String id, final byte[] command) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (socket != null) {
                    StringBuilder sb = new StringBuilder();
                    for (byte value : command) {
                        String b = Integer.toHexString(value);
                        if (b.length() == 1) {
                            b = "0" + b;
                        } else if (b.length() > 2) {
                            b = b.substring(b.length() - 2);
                        }
                        sb.append(b);
                        sb.append(",");
                    }
                    SdkLog.D("send data, id = " + id + ", msg = " + sb);

                    try {
                        OutputStream os = socket.getOutputStream();
                        os.write(command);
                        os.flush();
                    } catch (Exception e) {
                        SdkLog.E("send data error, id = " + id +  ", error = " + e);
                        ZTcpService.build().socketStateChange(id,100);
                    }
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
    /**
     * 循环遍历客户端集合，给每个客户端都发送字串[String]。
     */
    public void send(String s) {
        if (socket != null) {
            SdkLog.D("send data, msg = " + s);
            try {
                OutputStream os = socket.getOutputStream();
                PrintWriter pw = new PrintWriter(os);
                pw.write(s);
                pw.flush();
            } catch (IOException e) {
                SdkLog.E("send data error = " + e);
            }
        }
    }

    @Override
    public String toString() {
        return "SocketClient{" +
                "bsId='" + bsId + '\'' +
                ", ip='" + ip + '\'' +
                '}';
    }
}
