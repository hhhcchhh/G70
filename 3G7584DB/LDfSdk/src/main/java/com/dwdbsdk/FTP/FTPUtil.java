package com.dwdbsdk.FTP;

import android.os.Handler;

import com.dwdbsdk.Interface.FtpListener;
import com.dwdbsdk.Logcat.SdkLog;
import com.dwdbsdk.Socket.ZTcpService;

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
	 * @param localFile 本地文件
	 * @param remotePath 服务器路径
	 */
	private void putFile(String localFile, String remotePath) {
		try {
			if (ftp != null) {
				SdkLog.I("FTPUtil: putFile: localFile = " + localFile + ", remotePath = " + remotePath);
				Result result = ftp.uploading(new File(localFile), remotePath);
                SdkLog.I("FTPUtil: putFile: result = " + result);
			}
		} catch (IOException e) {
			state = false;
			SdkLog.I("FTPUtil: putFile() error: " + e);
		}
	}
	/**
	 * 升级包存放位置
	 * @param id  设备 id
	 * @param filePath 文件路径
	 */
	public void startPutFile(final String id, String filePath) {
		startPutFile("", id, filePath);
	}
	public void startPutFile(final String ip, final String id, final String filePath){
		state = true;
		new Thread() {
			@Override
			public void run() {
				if (openFTP(ip, id)) {
					SdkLog.D("FTPUtil: startPutFile start...");
					putFile(filePath, remotePath);
					SdkLog.D("FTPUtil: startPutFile finish");
					handler.post(new Runnable() {
						@Override
						public void run() {
							if (listener != null) {
								listener.onFtpPutFileRsp(id, state);
							}
						}
					});
				}
			}
		}.start();
	}
	/**
	 * 从服务器获取文件
	 * @param remotePath 服务器路径
	 * @param fileName  文件名
	 * @param localPath  本地路径
	 */
	private void getFile(String id, String remotePath, String fileName, String localPath) {
		try {
			if (ftp != null) {
				SdkLog.I("FTPUtil: getFile: remotePath = " + remotePath + ", fileName = " + fileName + ", localPath = " + localPath);
                Result result = ftp.download(id, remotePath, fileName, localPath, listener);
                SdkLog.I("FTPUtil: getFile: result = " + result);
			}
		} catch (IOException e) {
			state = false;
			SdkLog.I("FTPUtil: getFile() state: " + e.toString());
		}
	}

	/**
	 * 读取基带文件
	 * @param localPath 本地路径
	 * @param fileName 文件名
	 */
	public void startGetFile(String id, String localPath, String fileName) {
		startGetFile( "", id, localPath, fileName);
	}

	public void startGetFile(final String ip, String id, final String localPath, final String fileName){
		state = true;
		new Thread() {
			@Override
			public void run() {
				if (openFTP(ip, id)) {
					SdkLog.D("FTPUtil: startGetFile start...");
					String name = fileName + ".zip";
					getFile(id, remotePath, name, localPath);
					SdkLog.D("FTPUtil: startGetFile finish");
					handler.post(new Runnable() {
						@Override
						public void run() {
							if (listener != null) {
								listener.onFtpGetFileRsp(id, state);
							}
						}
					});
				}
			}
		}.start();
	}

	public boolean openFTP(String ip, String id) {
		String hostIp;
		if (ip.isEmpty()) hostIp = ZTcpService.build().getHostIp(id);
		else hostIp = ip;
		SdkLog.D("FTPUtil: openFTP hostIp: " + hostIp + ", user = " + userName + ", password = " + password);
		try {
			if (ftp != null) {
				// 关闭FTP服务
				ftp.closeConnect();
			}
			// 初始化FTP
			ftp = new FTP(hostIp, userName, password);
			// 打开FTP服务
			ftp.openConnect();
			if (listener != null) {
				listener.onFtpConnectState(id,true);
			}
			return true;
		} catch (IOException e) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					if (listener != null) {
						listener.onFtpConnectState(id,false);
					}
				}
			});
			SdkLog.E("FTPUtil: openFTP FAIL: " + e);
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
			SdkLog.E("FTPUtil: closeFTP FAIL: " + e);
		}
	}

	private void init(String user, String passwd, String path) {
		SdkLog.D("FTP init success");
		this.userName = user;
		this.password = passwd;
		this.remotePath = path;
	}

	public void setFtpListener(FtpListener listener) {
		this.listener = listener;
	}
	public void removeFtpListener() {
		this.listener = null;
	}
	
	private FtpListener listener;

	private static FTPUtil instance;
	private FTP ftp;
	private boolean state = false;
	private final Handler handler = new Handler();
	private String userName, password, remotePath;
}