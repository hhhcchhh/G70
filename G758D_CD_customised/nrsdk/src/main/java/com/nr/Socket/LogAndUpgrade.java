package com.nr.Socket;

import android.os.Handler;

import com.Logcat.SLog;
import com.nr.FTP.FTP;
import com.nr.FTP.Result;
import com.nr.Socket.MessageControl.MessageHelper;

import java.io.File;
import java.io.IOException;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;

public class LogAndUpgrade {
    private FTP ftp;

    public static LogAndUpgrade build() {
        synchronized (LogAndUpgrade.class) {
            if (instance == null) {
                instance = new LogAndUpgrade();
            }
        }
        return instance;
    }

    private LogAndUpgrade() {
        this.username = "ftpuser";
 //       this.pp = "nr5g";
        this.password = "simpie";
    }

    private void connect(final String id) {
        SLog.I("LogAndUpgrade: connect()");
        close();
        connection = new Connection(hostIp);
        try {
            connection.connect();
            isAuthed = connection.authenticateWithPassword(username, password);
            //isAuthed = connection.authenticateWithPassword(pu, pp);
            SLog.I("LogAndUpgrade: connect() isAuthed = " + isAuthed);
            // scp 连接
            scpClient = connection.createSCPClient();
            SLog.I("LogAndUpgrade: connect() connection.createSCPClient");
        } catch (IOException e) {
            state =false;
            e.printStackTrace();
            SLog.I("LogAndUpgrade: connect() state: " + e.toString());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.OnScpConnectFail(id,state);
                    }
                }
            });
            close();
        }
    }
    public boolean openFTP(final String id) {
//        hostIp = MessageHelper.build().getHostIp();
        SLog.D("FTPUtil: openFTP hostIp: " + hostIp + ", user = " + username + ", password = " + password);
        try {
            if (ftp != null) {
                // 关闭FTP服务
                ftp.closeConnect();
            }
            // 初始化FTP
            ftp = new FTP(hostIp, username, password);
            // 打开FTP服务
            ftp.openConnect();
            return true;
        } catch (IOException e) {
            state =false;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.OnScpConnectFail(id,state);
                    }
                }
            });
            SLog.E("FTPUtil: openFTP FAIL: " + e.toString());
            e.printStackTrace();
        }
        return false;
    }
    private void close() {
//        if (connection != null) {
//            connection.close();
//            connection = null;
//        }
        // 关闭服务
        try {
            if (ftp != null) {
                ftp.closeConnect();
                ftp = null;
            }
        } catch (IOException e) {
            SLog.E("FTPUtil: closeFTP FAIL: " + e.toString());
            e.printStackTrace();
        }
    }

    private boolean getIsAuthed() {
        return isAuthed;
    }

    /**
     * 拷贝文件到服务器
     *
     * @param localFile
     * @param remotePath
     */
    private void putFile(String localFile, String remotePath) {
        try {
            if (ftp != null) {
                SLog.I("FTPUtil: putFile: localFile = " + localFile);
                SLog.I("FTPUtil: putFile: remotePath = " + remotePath);
                Result result = ftp.uploading(new File(localFile), remotePath);
                SLog.I("FTPUtil: putFile: result = " + result);
            }
        } catch (IOException e) {
            e.printStackTrace();
            state = false;
            SLog.I("FTPUtil: putFile() state: " + e.toString());
        }
    }
    /**
     * 从服务器获取文件
     *
     * @param remotePath
     * @param fileName
     * @param localPath
     */
    private void getFile(String remotePath, String fileName, String localPath) {
        try {
            if (ftp != null) {
                SLog.I("FTPUtil: getFile: remotePath = " + remotePath + "/" + fileName);
                SLog.I("FTPUtil: getFile: localPath = " + localPath);
                Result result = ftp.download(remotePath, fileName, localPath);
                SLog.I("FTPUtil: getFile: result = " + result);
            }
        } catch (IOException e) {
            e.printStackTrace();
            state = false;
            SLog.I("FTPUtil: getFile() state: " + e.toString());
        }
    }
    /**
     * 升级包存放位置
     *
     * @param filePath
     */
    public void startPutFile(final String id, final String filePath) {
        state = true;
        hostIp = ZTcpService.build().getHostIp(id);
        SLog.D("LogAndUpgrade: startPutFile hostIp = " + hostIp);
        if (hostIp == null) {
            state = false;
			handler.post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.OnScpConnectFail(id,state);
                    }
                }
            });
            return;
        }
        new Thread() {
            @Override
            public void run() {
//                connect();
                if(openFTP(id)){
                    SLog.D("LogAndUpgrade: startPutFile start...");
                    putFile(filePath,"");
                    SLog.D("LogAndUpgrade: startPutFile finish");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.OnScpUpgradeFileRsp(id,state);
                            }
                        }
                    });
                }
            }
        }.start();
    }
    public void startPutFile(final String filePath) {
        startPutFile(MessageHelper.build().getDeviceId(),  filePath);
    }
    /**
     * 获取基带LOG
     *
     * @param localPath
     * @param name
     */
    public void startGetFile(final String id, final String localPath, final String name) {
        state = true;
        hostIp = ZTcpService.build().getHostIp(id);
        SLog.D("LogAndUpgrade: startGetFile hostIp = " + hostIp);
        if (hostIp == null) {
			handler.post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.OnScpConnectFail(id,state);
                    }
                }
            });
            return;
        }
        new Thread() {
            @Override
            public void run() {
                if(openFTP(id)){
                    SLog.D("FTPUtil: startGetFile start...");
                    String fname = name + ".zip";
                    getFile("", fname, localPath);
                    SLog.D("FTPUtil: startGetFile finish");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.OnScpGetLogRsp(id,state);
                            }
                        }
                    });
                }
            }
        }.start();
    }
    public void startGetFile( final String localPath, final String name) {
        startGetFile(MessageHelper.build().getDeviceId(), localPath, name);
    }
    /**
     * 获取黑匣子文件
     *
     * @param localPath
     * @param name
     */
    public void startGetOpFile(final String id, final String localPath, final String name) {
        state = true;
        hostIp = ZTcpService.build().getHostIp(id);
        SLog.D("LogAndUpgrade: startGetOpFile hostIp = " + hostIp);
        if (hostIp == null) {
			handler.post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.OnScpConnectFail(id,state);
                    }
                }
            });
            return;
        }
        new Thread() {
            @Override
            public void run() {
                if(openFTP(id)){
                    SLog.D("LogAndUpgrade: startGetOpFile start...");
                    String path = "/home/upgrade/" + name + ".zip";
                    getFile("", path,localPath);
                    SLog.D("LogAndUpgrade: startGetOpFile finish");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.OnScpGetLogRsp(id,state);
                            }
                        }
                    });
                }
            }
        }.start();
    }
    public void startGetOpFile(final String localPath, final String name) {
        startGetOpFile(MessageHelper.build().getDeviceId(), localPath, name);
    }
    public void setOnScpListener(OnScpListener listener) {
        this.listener = listener;
    }
    public void removeOnScpListener() {
        this.listener = null;
    }

    public interface OnScpListener {
        void OnScpConnectFail(String id,boolean state);
        void OnScpGetLogRsp(String id,boolean state);
        void OnScpUpgradeFileRsp(String id,boolean state);
    }
    private OnScpListener listener;

    private static LogAndUpgrade instance;
    private final String username;
    private final String password;
    private String hostIp;
    private Connection connection;
    private SCPClient scpClient;
    private boolean isAuthed = false;
    private boolean state = false;
    private String path;
    private Handler handler = new Handler();
}
