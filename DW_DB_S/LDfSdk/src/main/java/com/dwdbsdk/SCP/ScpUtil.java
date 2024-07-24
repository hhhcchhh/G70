package com.dwdbsdk.SCP;

import android.os.Handler;

import com.dwdbsdk.Logcat.SdkLog;

import com.dwdbsdk.Socket.ZTcpService;

import java.io.IOException;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;

public class ScpUtil {
    public static ScpUtil build() {
        synchronized (ScpUtil.class) {
            if (instance == null) {
                instance = new ScpUtil();
            }
        }
        return instance;
    }

    private ScpUtil() {
        this.pu = "root";
       this.pp = "nro";
    }

    private void connect() {
        SdkLog.I("ScpUtil: connect() hostIp = " + hostIp);
        close();
        connection = new Connection(hostIp);
        try {
            connection.connect();
            isAuthed = connection.authenticateWithPassword(pu, pp + "5g");
            SdkLog.I("ScpUtil: connect() isAuthed = " + isAuthed);
            // scp 连接
            scpClient = connection.createSCPClient();
            SdkLog.I("ScpUtil: connect() connection.createSCPClient");
        } catch (IOException e) {
            SdkLog.I("ScpUtil: connect() state: " + e.toString());
            close();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.OnScpConnectFail();
                    }
                }
            });

        }
    }

    private void close() {
        if (connection != null) {
            connection.close();

            connection = null;
        }
    }

    private boolean getIsAuthed() {
        return isAuthed;
    }

    /**
     * 拷贝文件到服务器
     *
     * @param filePath
     * @param aimPath
     */
    private void putFile(String filePath, String aimPath) {
        try {
            if (scpClient != null) {
                SdkLog.I("ScpUtil: putFile: filePath = " + filePath + ",aimPath = " + aimPath);
                scpClient.put(filePath, aimPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
            state = false;
            SdkLog.I("ScpUtil: putFile() state: " + e.toString());
        }
    }
    /**
     * 从服务器获取文件
     *
     * @param filePath
     * @param aimPath
     */
    private void getFile(String filePath, String aimPath) {
        try {
            if (scpClient != null) {
                SdkLog.I("ScpUtil: getFile: filePath = " + filePath + ", aimPath = " + aimPath);
                scpClient.get(aimPath, filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
            state = false;
            SdkLog.I("ScpUtil: getFile() state: " + e.toString());
        }
    }
    /**
     * 升级包存放位置
     *
     * @param filePath
     */
    public void startPutFile(String id, final String filePath) {
        state = true;
        hostIp = ZTcpService.build().getHostIp(id);
        new Thread() {
            @Override
            public void run() {
                connect();
                if(getIsAuthed()){
                    SdkLog.D("ScpUtil: startPutFile start...");
                    putFile(filePath,"/home/upgrade");
                    SdkLog.D("ScpUtil: startPutFile finish");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.OnScpUpgradeFileRsp(state);
                            }
                        }
                    });
                }
            }
        }.start();
    }
    /**
     * 获取基带LOG
     *
     * @param localPath
     * @param name
     */
    public void startGetFile(String id, final String localPath, final String name) {
        state = true;
        hostIp = ZTcpService.build().getHostIp(id);
        new Thread() {
            @Override
            public void run() {
                connect();
                if(getIsAuthed()){
                    SdkLog.D("ScpUtil: startGetFile start...");
                    String path = "/home/upgrade/" + name + ".zip";
                    getFile(localPath, path);
                    SdkLog.D("ScpUtil: startGetFile finish");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.OnScpGetLogRsp(state);
                            }
                        }
                    });
                }
            }
        }.start();
    }

    public void setOnScpListener(OnScpListener listener) {
        this.listener = listener;
    }
    public void removeOnScpListener() {
        this.listener = null;
    }

    public interface OnScpListener {
        void OnScpConnectFail();
        void OnScpGetLogRsp(boolean state);
        void OnScpUpgradeFileRsp(boolean state);
    }
    private OnScpListener listener;

    private static ScpUtil instance;
    private String pu;
    private String pp;
    private String hostIp;
    private Connection connection;
    private SCPClient scpClient;
    private boolean isAuthed = false;
    private boolean state = false;
    private Handler handler = new Handler();
}
