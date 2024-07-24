package com.simdo.dw_4db_s.Util;

import android.annotation.SuppressLint;
import android.util.Log;

import com.simdo.dw_4db_s.File.FileProtocol;
import com.simdo.dw_4db_s.File.FileSizeUtil;
import com.simdo.dw_4db_s.File.FileUtil;
import java.text.SimpleDateFormat;

@SuppressLint("SimpleDateFormat") 
public class AppLog {
	private static final boolean D = true;
	private static final String TAG = "DW_DB";
	
	private static String mLogFilePath;

	public static void D(String msg) {
		if (D) {
			Log.d(TAG, msg);
			saveLog(msg);
		}
	}
	
	public static void W(String msg) {
		if (D) {
			Log.w(TAG, msg);
			saveLog(msg);
		}
	}
	
	public static void E(String msg) {
		if (D) {
			Log.e(TAG, msg);
			saveLog(msg);
		}
	}
	
	public static void I(String msg) {
		if (D) {
			Log.i(TAG, msg);
			saveLog(msg);
		}
	}
	
	public static synchronized void createLogFile(String msg) {
		FileUtil.build().createFolder(FileProtocol.DIR_UL);
		FileUtil.build().createFolder(FileProtocol.DIR_UPGRADE);
		FileUtil.build().createFolder(FileProtocol.DIR_OP);
		/*File file = new File(FileUtil.build().getSDPath() + "/" + FileProtocol.FILE_UPGRADE);
		if (file.exists()) { //删除上一次升级包
			file.delete();
		}
		file = new File(FileUtil.build().getSDPath() + "/" + FileProtocol.FILE_BS_LOG);
		if (file.exists()) { //删除上一次升级包
			file.delete();
		}*/
		String fileName = FileUtil.build().getNewLogFileName(FileProtocol.DIR_LOG);
		mLogFilePath = FileUtil.build().createOrAppendFile(msg, FileProtocol.DIR_LOG, fileName, 0);
		if (mLogFilePath == null) {
			I("createLogFile(): Log文件未创建成功： mLogFilePath == null");
		}
	}

	public static synchronized void saveLog(String msg) {
		if (mLogFilePath == null) {
			return;
		}
		
		double fileSize = FileSizeUtil.getFileOrFilesSize(
				mLogFilePath, FileSizeUtil.SIZETYPE_MB);
		
		if (fileSize > 10.00) {
			createLogFile(msg + "\n");
		} else {
			SimpleDateFormat sDateFormat = null;
			sDateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
			String stime = sDateFormat.format(new java.util.Date());
			String slog = stime + "    " + msg + "\n";
			FileUtil.build().appendFile(mLogFilePath, slog);
		}
	}
	

}
