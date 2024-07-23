package com.nr.FTP;

import android.os.Handler;

import com.Logcat.SLog;
import com.nr.Socket.MessageControl.MessageHelper;

import java.io.File;
import java.io.IOException;

public class FTPUtil {
    public static FTPUtil build() {
        synchronized (FTPUtil.class) {
            if (instance == null) {
                instance = new FTPUtil();
            }
        }
        return instance;
    }

    public FTPUtil() {
        init("ftpuser", "simpie", "");
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
     * 升级包存放位置
     *
     * @param filePath
     */
    public void startPutFile(String filePath) {
        startPutFile(MessageHelper.build().getDeviceId(), "", filePath);
    }

    public void startPutFile(String id, String filePath) {
        startPutFile(id, "", filePath);
    }

    public void startPutFile(final String id, final String ip, final String filePath) {
        state = true;
        new Thread() {
            @Override
            public void run() {
                if (openFTP(id, ip)) {
                    SLog.D("FTPUtil: startPutFile start...");
                    putFile(filePath, remotePath);
                    SLog.D("FTPUtil: startPutFile finish");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.OnFtpPutFileRsp(id, state);
                            }
                        }
                    });
                }
            }
        }.start();
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
                if (result == null) state = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            state = false;
            SLog.I("FTPUtil: getFile() state: " + e.toString());
        }
    }

    /**
     * 读取基带文件
     *
     * @param localPath
     * @param name
     */
    public void startGetFile(String localPath, String name) {
        startGetFile(MessageHelper.build().getDeviceId(), "", localPath, name);
    }

    public void startGetFile(String id, String localPath, String name) {
        startGetFile(id, "", localPath, name);
    }

    public void startGetFile(final String id, final String ip, final String localPath, final String name) {
        state = true;
        new Thread() {
            @Override
            public void run() {
                if (openFTP(id, ip)) {
                    SLog.D("FTPUtil: startGetFile start...");
                    String fname = name + ".zip";
                    getFile(remotePath, fname, localPath);
                    SLog.D("FTPUtil: startGetFile finish");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.OnFtpGetFileRsp(id, state);
                            }
                        }
                    });
                }
            }
        }.start();
    }

    public boolean openFTP(final String id, String ip) {
        if (ip.isEmpty()) hostIp = MessageHelper.build().getHostIp();
        else hostIp = ip;
        SLog.D("FTPUtil: openFTP hostIp: " + hostIp + ", user = " + userName + ", password = " + password);
        try {
            if (ftp != null) {
                // 关闭FTP服务
                ftp.closeConnect();
            }
            // 初始化FTP
            ftp = new FTP(hostIp, userName, password);

            // 打开FTP服务
            ftp.openConnect();
            return true;
        } catch (IOException e) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.OnFtpConnectFail(id, state);
                    }
                }
            });
            SLog.E("FTPUtil: openFTP FAIL: " + e.toString());
            e.printStackTrace();
        }
        return false;
    }

    public void closeFTP() {
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

    private void init(String user, String passwd, String path) {
        SLog.D("FTPUtil: init user: " + user + ", passwd = " + passwd + ", path = " + path);
        this.userName = user;
        this.password = passwd;
        this.remotePath = path;
    }

    public void setFtpListener(OnFtpListener listener) {
        this.listener = listener;
    }

    public void removeFtpListener() {
        this.listener = null;
    }

    public interface OnFtpListener {
        void OnFtpConnectFail(String id, boolean state);

        void OnFtpGetFileRsp(String id, boolean state);

        void OnFtpPutFileRsp(String id, boolean state);
    }

    private OnFtpListener listener;

    private static FTPUtil instance;
    private FTP ftp;
    private boolean state = false;
    private Handler handler = new Handler();
    private String hostIp, userName, password, remotePath;
}